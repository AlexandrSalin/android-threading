package chan.android.threading.cache;

import java.io.*;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Cache implementation that caches files directly onto the hard disk in the specified
 * directory. The default disk usage size is 5MB, but is configurable.
 */
public class DiskBasedCache {

  /**
   * Default maximum disk usage in bytes.
   */
  private static final int DEFAULT_DISK_USAGE_BYTES = 5 * 1024 * 1024;
  /**
   * High water mark percentage for the cache
   */
  private static final float HYSTERESIS_FACTOR = 0.9f;
  /**
   * Magic number for current version of cache file format.
   */
  private static final int CACHE_MAGIC = "CACHE_MAGIC".hashCode();
  private final Map<String, CacheHeader> entries = new LinkedHashMap<String, CacheHeader>(16, 0.75f, true);
  private final File rootDirectory;
  private final int maxCacheSizeInBytes;
  private long totalSize = 0;

  public DiskBasedCache(File rootDirectory, int maxCacheSizeInBytes) {
    this.rootDirectory = rootDirectory;
    this.maxCacheSizeInBytes = maxCacheSizeInBytes;
  }

  public DiskBasedCache(File rootDirectory) {
    this(rootDirectory, DEFAULT_DISK_USAGE_BYTES);
  }

  /**
   * Reads the contents of an InputStream into a byte[].
   */
  private static byte[] streamToBytes(InputStream in, int length) throws IOException {
    byte[] bytes = new byte[length];
    int count;
    int pos = 0;
    while (pos < length && ((count = in.read(bytes, pos, length - pos)) != -1)) {
      pos += count;
    }
    if (pos != length) {
      throw new IOException("Expected " + length + " bytes, read " + pos + " bytes");
    }
    return bytes;
  }

  /**
   * Simple wrapper around {@link java.io.InputStream#read()} that throws EOFException
   * instead of returning -1.
   */
  private static int read(InputStream is) throws IOException {
    int b = is.read();
    if (b == -1) {
      throw new EOFException();
    }
    return b;
  }

  static void writeInt(OutputStream os, int n) throws IOException {
    os.write((n >> 0) & 0xff);
    os.write((n >> 8) & 0xff);
    os.write((n >> 16) & 0xff);
    os.write((n >> 24) & 0xff);
  }

  static int readInt(InputStream is) throws IOException {
    int n = 0;
    n |= (read(is) << 0);
    n |= (read(is) << 8);
    n |= (read(is) << 16);
    n |= (read(is) << 24);
    return n;
  }

  static void writeLong(OutputStream os, long n) throws IOException {
    os.write((byte) (n >>> 0));
    os.write((byte) (n >>> 8));
    os.write((byte) (n >>> 16));
    os.write((byte) (n >>> 24));
    os.write((byte) (n >>> 32));
    os.write((byte) (n >>> 40));
    os.write((byte) (n >>> 48));
    os.write((byte) (n >>> 56));
  }

  static long readLong(InputStream is) throws IOException {
    long n = 0;
    n |= ((read(is) & 0xFFL) << 0);
    n |= ((read(is) & 0xFFL) << 8);
    n |= ((read(is) & 0xFFL) << 16);
    n |= ((read(is) & 0xFFL) << 24);
    n |= ((read(is) & 0xFFL) << 32);
    n |= ((read(is) & 0xFFL) << 40);
    n |= ((read(is) & 0xFFL) << 48);
    n |= ((read(is) & 0xFFL) << 56);
    return n;
  }

  static void writeString(OutputStream os, String s) throws IOException {
    byte[] b = s.getBytes("UTF-8");
    writeLong(os, b.length);
    os.write(b, 0, b.length);
  }

  static String readString(InputStream is) throws IOException {
    int n = (int) readLong(is);
    byte[] b = streamToBytes(is, n);
    return new String(b, "UTF-8");
  }

  static void writeStringStringMap(Map<String, String> map, OutputStream os) throws IOException {
    if (map != null) {
      writeInt(os, map.size());
      for (Map.Entry<String, String> entry : map.entrySet()) {
        writeString(os, entry.getKey());
        writeString(os, entry.getValue());
      }
    } else {
      writeInt(os, 0);
    }
  }

  static Map<String, String> readStringStringMap(InputStream is) throws IOException {
    int size = readInt(is);
    Map<String, String> result = (size == 0) ? Collections.<String, String>emptyMap() : new HashMap<String, String>(size);
    for (int i = 0; i < size; i++) {
      String key = readString(is).intern();
      String value = readString(is).intern();
      result.put(key, value);
    }
    return result;
  }

  /**
   * Clears the cache. Deletes all cached files from disk.
   */
  public synchronized void clear() {
    File[] files = rootDirectory.listFiles();
    if (files != null) {
      for (File file : files) {
        file.delete();
      }
    }
    entries.clear();
    totalSize = 0;
  }

  public synchronized Cache.Entry get(String key) {
    CacheHeader entry = entries.get(key);
    if (entry == null) {
      return null;
    }
    // TODO:
    return null;
  }

  /**
   * Handles holding onto the cache headers for an entry.
   */
  static class CacheHeader {
    public long size;

    public String key;

    public String etag;

    public long serverDate;

    public long timeToLive;

    public long softTimeToLive;

    public Map<String, String> responseHeaders;

    private CacheHeader() {
    }

    /**
     * Instantiates a new CacheHeader object
     *
     * @param key   The key that identifies the cache entry
     * @param entry The cache entry.
     */
    public CacheHeader(String key, Cache.Entry entry) {
      this.key = key;
      this.size = entry.data.length;
      this.etag = entry.etag;
      this.serverDate = entry.serverDate;
      this.timeToLive = entry.timeToLive;
      this.softTimeToLive = entry.softTimeToLive;
      this.responseHeaders = entry.responseHeaders;
    }
  }

  private static class CountingInputStream extends FilterInputStream {
    private int bytesRead = 0;

    private CountingInputStream(InputStream in) {
      super(in);
    }

    @Override
    public int read() throws IOException {
      int result = super.read();
      if (result != -1) {
        bytesRead++;
      }
      return result;
    }

    @Override
    public int read(byte[] buffer, int offset, int count) throws IOException {
      int result = super.read(buffer, offset, count);
      if (result != -1) {
        bytesRead += result;
      }
      return result;
    }
  }
}
