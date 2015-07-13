package chan.android.threading.cache;

import java.util.Collections;
import java.util.Map;

/**
 * An interface for a cache keyed by a String with a byte array as data
 */
public interface Cache {

  /**
   * Retrieves an entry from the cache.
   *
   * @param key Cache key
   * @return An cache entry or null in the event of a cache miss
   */
  public Entry get(String key);

  /**
   * Adds or replaces an entry to the cache.
   *
   * @param key   Cache key
   * @param entry Data to store and metadata for cache coherency, TTL, etc.
   */
  public void put(String key, Entry entry);

  /**
   * Performs any potentially long-running actions needed to initialize the cache;
   * will be called from a worker thread
   */
  public void initialize();

  /**
   * Invalidates an entry in the cache.
   *
   * @param key        Cache key
   * @param fullExpire True to fully expire the entry, false to soft expire
   */
  public void invalidate(String key, boolean fullExpire);

  /**
   * Remove an entry from the cache.
   *
   * @param key Cache key
   */
  public void remove(String key);

  /**
   * Empties the cache.
   */
  public void clear();

  public static class Entry {
    public byte[] data;

    public String etag;

    public long serverDate;

    public long timeToLive;

    public long softTimeToLive;

    public Map<String, String> responseHeaders = Collections.emptyMap();

    public boolean isExpired() {
      return this.timeToLive < System.currentTimeMillis();
    }

    public boolean refreshNeeded() {
      return this.softTimeToLive < System.currentTimeMillis();
    }
  }
}
