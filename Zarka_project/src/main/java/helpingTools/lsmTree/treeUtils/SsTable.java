package helpingTools.lsmTree.treeUtils;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;

import java.io.Closeable;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.charset.StandardCharsets;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.TreeMap;

public class SsTable implements Closeable {
    public static final String RW = "rw";
    public static final int segmentMaxSize = 500;

    private TableMetaInfo tableMetaInfo;

    private TreeMap<String, Position> sparseIndex;

    private final RandomAccessFile segmentFile;

    private final String filePath;


    private SsTable(String filePath, int partSize) {
        this.tableMetaInfo = new TableMetaInfo();
        this.tableMetaInfo.setPartSize(partSize);
        this.filePath = filePath;
        try {
            this.segmentFile = new RandomAccessFile(filePath, RW);
            segmentFile.seek(0);
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
        sparseIndex = new TreeMap<>();
    }

    public TreeMap<String, Position> getSparseIndex() {
        return sparseIndex;
    }

    public RandomAccessFile getSegmentFile() {
        return segmentFile;
    }

    public String getFilePath() {
        return filePath;
    }

    /**
     * create SStable from memtable
     * @param filePath
     * @param partSize
     * @param memtable
     * @return
     */
    public static SsTable createFromMemtable(String filePath, int partSize, TreeMap<String, Command> memtable) {
        SsTable ssTable = new SsTable(filePath, partSize);
        ssTable.initFromMemtable(memtable);
        return ssTable;
    }

    /**
     * initialize Sstable from memtable
     * @param memtable
     */
    private void initFromMemtable(TreeMap<String, Command> memtable) {
        try {
            JSONObject partData = new JSONObject(true);
            tableMetaInfo.setDataStart(segmentFile.getFilePointer());
            for (Command command : memtable.values()) {
                if (command instanceof SetCommand) {
                    SetCommand set = (SetCommand) command;
                    partData.put(set.getKey(), set);
                }
                if (command instanceof RmCommand) {
                    RmCommand rm = (RmCommand) command;
                    partData.put(rm.getKey(), rm);
                }

                // if we reach the partition size, write this part
                if (partData.size() >= tableMetaInfo.getPartSize()) {
                    writeDataPart(partData);
                }
            }
            // write the last a partition of data
            if (partData.size() > 0) {
                writeDataPart(partData);
            }
            long dataPartLen = segmentFile.getFilePointer() - tableMetaInfo.getDataStart();
            tableMetaInfo.setDataLen(dataPartLen);

            // write sparse index
            byte[] indexBytes = JSONObject.toJSONString(sparseIndex).getBytes(StandardCharsets.UTF_8);
            tableMetaInfo.setIndexStart(segmentFile.getFilePointer());
            segmentFile.write(indexBytes);
            tableMetaInfo.setIndexLen(indexBytes.length);

            // write table info
            tableMetaInfo.writeToFile(segmentFile);

        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }

    /**
     * create Sstable form file
     * @param filePath
     * @return
     */
    public static SsTable createFromFile(String filePath) {
        SsTable ssTable = new SsTable(filePath, 0);
        ssTable.restoreFromFile();
        return ssTable;
    }

    /**
     * read Sstable form file
     */
    private void restoreFromFile() {
        try {
            // read table info and sparse index from file
            TableMetaInfo tableMetaInfo = TableMetaInfo.readFromFile(segmentFile);
            byte[] indexBytes = new byte[(int) tableMetaInfo.getIndexLen()];
            segmentFile.seek(tableMetaInfo.getIndexStart());
            segmentFile.read(indexBytes);
            String indexStr = new String(indexBytes, StandardCharsets.UTF_8);
            sparseIndex = JSONObject.parseObject(indexStr,
                    new TypeReference<TreeMap<String, Position>>() {
                    });
            this.tableMetaInfo = tableMetaInfo;
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }

    /**
     * write a partition of data in the segment
     * @param partData
     * @throws IOException
     */
    private void writeDataPart(JSONObject partData) throws IOException {
        byte[] partDataBytes = partData.toJSONString().getBytes(StandardCharsets.UTF_8);
        long start = segmentFile.getFilePointer();
        segmentFile.write(partDataBytes);

        // write index associated to this DataPart
        Optional<String> firstKey = partData.keySet().stream().findFirst();
        firstKey.ifPresent(s -> sparseIndex.put(s, new Position(start, partDataBytes.length)));
        partData.clear();
    }

    /**
     * query from SsTable
     * @param key
     * @return
     */
    public Command query(String key) {
        try {
            LinkedList<Position> sparseKeyPositionList = new LinkedList<>();

            Position lastSmallPosition = null; // last position before or equal the key
            Position firstBigPosition = null; // first position after the key

            for (String k : sparseIndex.keySet()) {
                if (k.compareTo(key) <= 0) {
                    lastSmallPosition = sparseIndex.get(k);
                } else {
                    firstBigPosition = sparseIndex.get(k);
                    break;
                }
            }
            if (lastSmallPosition != null) {
                sparseKeyPositionList.add(lastSmallPosition);
            }
            if (firstBigPosition != null) {
                sparseKeyPositionList.add(firstBigPosition);
            }
            // if the key that does not exist in the segment
            if (sparseKeyPositionList.size() == 0) {
                return null;
            }

            Position firstKeyPosition = sparseKeyPositionList.getFirst();
            Position lastKeyPosition = sparseKeyPositionList.getLast();
            long start = 0;
            long len = 0;
            start = firstKeyPosition.getStart();
            if (firstKeyPosition.equals(lastKeyPosition)) {
                len = firstKeyPosition.getLen();
            } else {
                len = lastKeyPosition.getStart() + lastKeyPosition.getLen() - start;
            }
            // read dataPart with start and length pointed to by the index
            byte[] dataPart = new byte[(int) len];
            segmentFile.seek(start);
            segmentFile.read(dataPart);
            int pStart = 0;
            //
            for (Position position : sparseKeyPositionList) {
                JSONObject dataPartJson = JSONObject.parseObject(new String(dataPart, pStart, (int) position.getLen()));
                if (dataPartJson.containsKey(key)) {
                    JSONObject value = dataPartJson.getJSONObject(key);
                    return ConvertUtil.jsonToCommand(value);
                }
                pStart += (int) position.getLen();
            }
            return null;
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }

    public List<JSONObject> getSegmentRecords() throws IOException {
        LinkedList<JSONObject> segmentRecords = new LinkedList<>();

        long start;
        long len;
        for(String k : this.getSparseIndex().keySet()) {
            Position partPosition = this.getSparseIndex().get(k);
            start = partPosition.getStart();
            len = partPosition.getLen();

            byte[] dataPart = new byte[(int) len];
            this.getSegmentFile().seek(start);
            this.getSegmentFile().read(dataPart);
            JSONObject dataPartJson = JSONObject.parseObject(new String(dataPart));
            segmentRecords.add(dataPartJson);
        }
        return segmentRecords;
    }

    @Override
    public void close() throws IOException {
        segmentFile.close();
    }
}
