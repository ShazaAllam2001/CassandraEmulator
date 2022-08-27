package helpingTools.lsmTree.model;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import helpingTools.lsmTree.treeUtils.*;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.charset.StandardCharsets;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.TreeMap;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class LSMTree implements ILSMTree {
    private final static String dir = "C:\\Users\\Blu-Ray\\Documents\\OOP Assignments\\Zarka_project\\src\\main\\java\\files\\trees\\";

    public static final String SEGMENT = ".segment";
    public static final String WAL = "wal";
    public static final String WAL_TMP = "walTmp";
    public static final String RW_MODE = "rw"; // Read Write mode

    // directory where we write the LSM Tree
    private String directory;

    // threshold that when we reach it we dump the memtable to a file
    private int storeThreshold;

    // size of segments on the LSM Tree
    private int indextRange;

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

    public LSMTree(String directory, int storeThreshold, int indextRange) {
        try {
            this.directory = dir + directory + "\\";
            // make directory for tree
            File theDir = new File(this.directory);
            if (!theDir.exists()){
                theDir.mkdirs();
            }
            this.storeThreshold = storeThreshold;
            this.indextRange = indextRange;
            this.memtableLock = new ReentrantReadWriteLock();
            this.ssTables = new LinkedList<SsTable>();
            this.memtable = new TreeMap<String, Command>();

            // get all the previous segments of the LSM Tree
            File dir = new File(this.directory);
            File[] files = dir.listFiles();
            if (files == null || files.length == 0) {
                // if there is no segments, construct a new WAL file to store logs to it
                this.walFile = new File(this.directory + WAL);
                this.wal = new RandomAccessFile(walFile, RW_MODE);
                return;
            }

            // put all previous SsTables on a TreeMap and sort them in reverse order
            TreeMap<Long, SsTable> ssTableTreeMap = new TreeMap<Long, SsTable>(Comparator.reverseOrder());
            for (File file : files) {
                String fileName = file.getName();
                //
                if (file.isFile() && fileName.equals(WAL_TMP)) {
                    restoreFromWal(new RandomAccessFile(file, RW_MODE));
                }
                if (file.isFile() && fileName.equals(WAL)) {
                    // load the WAL file to main memory
                    this.walFile = file;
                    this.wal = new RandomAccessFile(file, RW_MODE);
                    restoreFromWal(this.wal);
                }
                else if (file.isFile() && fileName.endsWith(SEGMENT)) {
                    // load the segments into main memory & put them into a TreeMap
                    int dotIndex = fileName.indexOf(".");
                    Long time = Long.parseLong(fileName.substring(0, dotIndex));
                    ssTableTreeMap.put(time, SsTable.createFromFile(file.getAbsolutePath()));
                }
            }
            // put the segments on the TreeMap to a LinkedList
            this.ssTables.addAll(ssTableTreeMap.values());

        } catch (Throwable e) {
            throw new RuntimeException(e);
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
                }
                start += 4; // the length of int
                start += valueLen;
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
            memtableLock.writeLock().lock();
            immutableMemtable = memtable;
            memtable = new TreeMap<String, Command>();
            wal.close();

            File tmpWal = new File(directory + WAL_TMP);
            if (tmpWal.exists()) {
                if (!tmpWal.delete()) {
                    throw new RuntimeException("Can not delete: walTmp");
                }
            }
            // rename the current WAL to "walTmp"
            if (!walFile.renameTo(tmpWal)) {
                throw new RuntimeException("Can not rename: walTmp");
            }
            // reference a new WAL file for the new segment
            walFile = new File(directory + WAL);
            wal = new RandomAccessFile(walFile, RW_MODE);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        } finally {
            memtableLock.writeLock().unlock();
        }
    }

    /**
     * Store the current memtable as an SsTable
     */
    private void storeToSsTable() {
        try {
            SsTable ssTable = SsTable.createFromIndex(directory + System.currentTimeMillis() + SEGMENT, indextRange, immutableMemtable);
            // add the last added Segment as the first to look on for keys
            ssTables.addFirst(ssTable);
            immutableMemtable = null;

            File tmpWal = new File(directory + WAL_TMP);
            if (tmpWal.exists()) {
                if (!tmpWal.delete()) {
                    throw new RuntimeException("Can not delete: walTmp");
                }
            }
        } catch (Throwable e) {
            throw new RuntimeException(e);
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

            // if memtable reaches its threshold dump it to a file
            if (memtable.size() > storeThreshold) {
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
                for (SsTable ssTable : ssTables) {
                    command = ssTable.query(key);
                    if (command != null) {
                        break;
                    }
                }
            }
            if (command instanceof SetCommand) {
                return ((SetCommand) command).getValue();
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
            if (memtable.size() > storeThreshold) {
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
