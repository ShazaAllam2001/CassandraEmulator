package helpingTools.lsmTree.treeUtils;

import java.io.RandomAccessFile;

public class TableMetaInfo {

    private long version;

    private long dataStart;

    private long dataLen;

    private long indexStart;

    private long indexLen;

    private long partSize;

    public long getVersion() {
        return version;
    }

    public void setVersion(long version) {
        this.version = version;
    }

    public long getDataStart() {
        return dataStart;
    }

    public void setDataStart(long dataStart) {
        this.dataStart = dataStart;
    }

    public long getDataLen() {
        return dataLen;
    }

    public void setDataLen(long dataLen) {
        this.dataLen = dataLen;
    }

    public long getIndexStart() {
        return indexStart;
    }

    public void setIndexStart(long indexStart) {
        this.indexStart = indexStart;
    }

    public long getIndexLen() {
        return indexLen;
    }

    public void setIndexLen(long indexLen) {
        this.indexLen = indexLen;
    }

    public long getPartSize() {
        return partSize;
    }

    public void setPartSize(long partSize) {
        this.partSize = partSize;
    }

    /**
     * write TableMetaInfo into a file
     * @param file
     */
    public void writeToFile(RandomAccessFile file) {
        try {
            file.writeLong(partSize);
            file.writeLong(dataStart);
            file.writeLong(dataLen);
            file.writeLong(indexStart);
            file.writeLong(indexLen);
            file.writeLong(version);
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }

    /**
     * read TableMetaInfo from a file
     * @param file
     * @return TableMetaInfo
     */
    public static TableMetaInfo readFromFile(RandomAccessFile file) {
        try {
            TableMetaInfo tableMetaInfo = new TableMetaInfo();
            long fileLen = file.length();

            file.seek(fileLen - 8);
            tableMetaInfo.setVersion(file.readLong());

            file.seek(fileLen - 8 * 2);
            tableMetaInfo.setIndexLen(file.readLong());

            file.seek(fileLen - 8 * 3);
            tableMetaInfo.setIndexStart(file.readLong());

            file.seek(fileLen - 8 * 4);
            tableMetaInfo.setDataLen(file.readLong());

            file.seek(fileLen - 8 * 5);
            tableMetaInfo.setDataStart(file.readLong());

            file.seek(fileLen - 8 * 6);
            tableMetaInfo.setPartSize(file.readLong());

            return tableMetaInfo;
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }

    }

}
