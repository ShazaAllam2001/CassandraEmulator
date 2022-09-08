package helpingTools.lsmTree.model;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.google.common.hash.BloomFilter;
import com.google.common.hash.Funnels;
import helpingTools.lsmTree.treeUtils.*;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.TreeMap;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class LSMTree implements ILSMTree {
    public final static String dirTree = "C:\\Users\\Blu-Ray\\Documents\\OOP Assignments\\CassandraEmulator\\Zarka_project\\src\\main\\java\\clientServer\\files\\trees\\";

    public static final String SEGMENT = ".segment";
    public static final String WAL = "wal";
    public static final String BLOOM_FILTER = "bloomFilter";
    public static final String RW_MODE = "rw"; // Read Write mode

    // directory where we write the LSM Tree
    private String directory;

    // threshold that when we reach it we dump the memtable to a file
    private int storeThreshold;

    // size of partitions in segments on the LSM Tree
    private int partSize;

    private BloomFilter<String> filter;

    // memtable before it reaches the store Threshold
    private TreeMap<String, Command> memtable;

    // memtable after it reaches the store Threshold
    private TreeMap<String, Command> immutableMemtable;

    // lock of the memtable after it reaches the store Threshold
    private final ReadWriteLock memtableLock;

    // list of all the previous ssTables
    private final LinkedList<SsTable> ssTables;

    // temporary WAL file
    private RandomAccessFile wal;

    // WAL file
    private File walFile;

    public LSMTree(String directory, int storeThreshold, int partSize) {
        try {
            this.directory = dirTree + directory + "\\";
            // make directory for tree
            File theDir = new File(this.directory);
            if (!theDir.exists()){
                theDir.mkdirs();
            }
            this.storeThreshold = storeThreshold;
            this.partSize = partSize;
            this.memtableLock = new ReentrantReadWriteLock();
            this.ssTables = new LinkedList<SsTable>();
            this.memtable = new TreeMap<String, Command>();
            this.filter = BloomFilter.create(Funnels.stringFunnel(Charset.forName("UTF-8")),10000);

            // get all the previous segments of the LSM Tree
            File dir = new File(this.directory);
            File[] files = dir.listFiles();
            if (files == null || files.length == 0) {
                // if there is no segments, construct a new WAL file to store logs to it
                this.walFile = new File(this.directory + WAL);
                this.wal = new RandomAccessFile(walFile, RW_MODE);
                return;
            }
            // restore Bloom filter
            for (File file : files) {
                String fileName = file.getName();
                if (file.isFile() && fileName.equals(BLOOM_FILTER)) {
                    deserializeBloomFilter();
                    break;
                }
            }
            getPreviousSsTables(files);

        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    public LinkedList<SsTable> getSsTables() {
        return ssTables;
    }

    /**
     *  get all previous SsTables
     *
     * @param files
     */
    private void getPreviousSsTables(File[] files) {
        try {
            // put all previous SsTables on a TreeMap and sort them in reverse order
            TreeMap<Long, SsTable> ssTableTreeMap = new TreeMap<Long, SsTable>(Comparator.reverseOrder());
            for (File file : files) {
                String fileName = file.getName();
                // restore last written commands to memtable
                if (file.isFile() && fileName.equals(WAL)) {
                    this.walFile = file;
                    this.wal = new RandomAccessFile(file, RW_MODE);
                    restoreFromWal(this.wal);
                }
                // get SsTables
                else if (file.isFile() && fileName.endsWith(SEGMENT)) {
                    // load the segments into main memory & put them into a TreeMap
                    int dotIndex = fileName.indexOf(".");
                    Long time = Long.parseLong(fileName.substring(0, dotIndex));
                    ssTableTreeMap.put(time, SsTable.createFromFile(file.getAbsolutePath()));
                }
            }
            // put the segments on the TreeMap to a LinkedList
            this.ssTables.addAll(ssTableTreeMap.values());

        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }

    /**
     *  restore the Key-Value pairs written in the WAL & load it to memtable
     *
     * @param wal
     */
    private void restoreFromWal(RandomAccessFile wal) {
        try {
            long len = wal.length();
            long start = 0;
            wal.seek(start);
            while (start < len) {
                int valueLen = wal.readInt();
                byte[] bytes = new byte[valueLen];
                wal.read(bytes);
                JSONObject value = JSON.parseObject(new String(bytes, StandardCharsets.UTF_8));
                Command command = ConvertUtil.jsonToCommand(value);
                if (command != null) {
                    this.memtable.put(command.getKey(), command);
                    this.filter.put(command.getKey());
                }
                start += 4; // the length of int
                start += valueLen; // the length of command
            }
            wal.seek(wal.length());

        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }

    /**
     * The current memtable reaches its threshold, so we need to switch to new one.
     * The read and write are locked while dumping the memtable. Therefore, no one will be able
     * to add tuples to the new mmemtable and make it reach its max threshold
     * as we won't be able to dump two memtable at the same time
     *
     */
    private void switchMemtable() {
        try {
            immutableMemtable = memtable;
            memtable = new TreeMap<String, Command>();
            wal.close();

        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Store the current memtable as an SsTable
     */
    private void storeToSsTable() {
        try {
            SsTable ssTable = SsTable.createFromMemtable(directory + System.currentTimeMillis() + SEGMENT, partSize, immutableMemtable);
            serializeBloomFilter();
            // add the last added Segment as the first to look on for keys
            ssTables.addFirst(ssTable);
            immutableMemtable = null;

            // reference a new WAL file for the new segment
            walFile = new File(directory + WAL);
            wal = new RandomAccessFile(walFile, RW_MODE);

        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Store the current bloom filter as a file
     */
    private void serializeBloomFilter() {
        try {
            File filterFile = new File(directory + BLOOM_FILTER);
            OutputStream filterStream = new FileOutputStream(filterFile);
            filter.writeTo(filterStream);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * read the current bloom filter from file
     */
    private void deserializeBloomFilter() {
        try {
            File filterFile = new File(directory + BLOOM_FILTER);
            InputStream filterStream = new FileInputStream(filterFile);
            filter = BloomFilter.readFrom(filterStream,Funnels.stringFunnel(Charset.forName("UTF-8")));
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * search for k in segmentParts & return its value
     *
     * @param k
     * @param segmentParts
    * */
    private Command searchSstable(String k, List<JSONObject> segmentParts) {
        for(JSONObject part : segmentParts) {
            for(String key : part.keySet()) {
                if(k.equals(key)) {
                    return ConvertUtil.jsonToCommand(part.getJSONObject(k));
                }
            }
        }
        return null;
    }

    /**
     * create the compacted segment from ssTable1 and ssTable2 from newMemTable
     *
     * @param ssTable1
     * @param ssTable2
     * @param newMemTable
     */
    private void createCompactedSegment(SsTable ssTable1, SsTable ssTable2, TreeMap<String,Command> newMemTable) {
        try {
            /* lock Reading while replacing files */
            memtableLock.readLock().lock();
            SsTable.createFromMemtable(ssTable1.getFilePath(), partSize, newMemTable);
            // close & delete SsTable2
            ssTable2.getSegmentFile().close();
            this.getSsTables().remove(ssTable2);
            File SsTable2Path = new File(ssTable2.getFilePath());
            if (SsTable2Path.exists()) {
                if (!SsTable2Path.delete()) {
                    throw new RuntimeException("Can not delete: " + SsTable2Path.getName());
                }
            }

        } catch(Throwable t) {
            throw new RuntimeException(t);
        } finally {
            memtableLock.readLock().unlock();
        }
    }

    /**
     * compact two ssTables
     *
     * @param ssTable1 the previous to the most recent
     * @param ssTable2 the most recent
     */
    public void compact(SsTable ssTable1, SsTable ssTable2) {
        try {
            List<JSONObject> segmentParts1 = ssTable1.getSegmentRecords();
            List<JSONObject> segmentParts2 = ssTable2.getSegmentRecords();

            TreeMap<String,Command> newMemTable = new TreeMap<>();

            // search key on ssTable1 on that appear in ssTable2
            for(JSONObject part : segmentParts1) {
                for(String key : part.keySet()) {
                    Command value = searchSstable(key, segmentParts2);
                    if(value != null) {
                        continue;
                    }
                    newMemTable.put(key, ConvertUtil.jsonToCommand(part.getJSONObject(key)));
                }
            }
            for(JSONObject part : segmentParts2) {
                for(String key : part.keySet()) {
                    newMemTable.put(key, ConvertUtil.jsonToCommand(part.getJSONObject(key)));
                }
            }

            createCompactedSegment(ssTable1, ssTable2, newMemTable);

        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }

    /**
     * set a certain value for a key
     *
     * @param key
     * @param value
     */
    @Override
    public void set(String key, String value) {
        try {
            SetCommand command = new SetCommand(key, value);
            byte[] commandBytes = JSONObject.toJSONBytes(command);
            // lock write before writing
            memtableLock.writeLock().lock();
            // write the write command into WAL before executing it
            wal.writeInt(commandBytes.length);
            wal.write(commandBytes);
            memtable.put(key, command);
            filter.put(key);

            // if memtable reaches its threshold dump it to a file
            if (memtable.size() >= storeThreshold) {
                switchMemtable();
                storeToSsTable();
            }
        } catch (Throwable t) {
            throw new RuntimeException(t);
        } finally {
            memtableLock.writeLock().unlock();
        }
    }

    /**
     * get a value of a key
     *
     * @param key
     * @return the value specified with that key
     */
    @Override
    public String get(String key) {
        try {
            // lock read before reading
            memtableLock.readLock().lock();
            Command command = memtable.get(key);
            // if the key does not exist in the current memtable
            if (command == null && immutableMemtable != null) {
                command = immutableMemtable.get(key);
            }
            if (command == null) {
                if(filter.mightContain(key)) {
                    for (SsTable ssTable : ssTables) {
                        command = ssTable.query(key);
                        if (command != null) {
                            break;
                        }
                    }
                }
            }
            if (command instanceof SetCommand) {
                return ((SetCommand) command).getValue() + "&" + ((SetCommand) command).getVersion();
            }
            if (command instanceof RmCommand) {
                return null;
            }
            return null;

        } catch (Throwable t) {
            throw new RuntimeException(t);
        } finally {
            memtableLock.readLock().unlock();
        }
    }

    /**
     * Remove a key (add a tombstone)
     *
     * @param key
     */
    @Override
    public void remove(String key) {
        try {
            // lock write before removing
            memtableLock.writeLock().lock();
            RmCommand rmCommand = new RmCommand(key);
            byte[] commandBytes = JSONObject.toJSONBytes(rmCommand);
            // write the remove command into WAL before executing it
            wal.writeInt(commandBytes.length);
            wal.write(commandBytes);
            memtable.put(key, rmCommand);

            // if memtable reaches its threshold dump it to a file
            if (memtable.size() >= storeThreshold) {
                switchMemtable();
                storeToSsTable();
            }
        } catch (Throwable e) {
            throw new RuntimeException(e);
        } finally {
            memtableLock.writeLock().unlock();
        }
    }

    @Override
    public void close() throws IOException {
        wal.close();
        for (SsTable ssTable : ssTables) {
            ssTable.close();
        }
    }
}
