package helpingTools.lsmTree.model;

import java.io.Closeable;

public interface ILSMTree extends Closeable {

    /**
     * set a certain value for a key
     *
     * @param key
     * @param value
     */
    void set(String key, String value);

    /**
     * get a value of a key
     *
     * @param key
     * @return the value specified with that key
     */
    String get(String key);

    /**
     * Remove a key (add a tombstone)
     *
     * @param key
     */
    void remove(String key);
}
