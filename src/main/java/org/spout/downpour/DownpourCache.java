package org.spout.downpour;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

public class DownpourCache {
	private boolean offlineMode = false;
	private long maxAge = 1000 * 60 * 60 * 24 * 7; // Keep for one week
	private File cacheDb = null;
	private File tempDir = null;
	public static final String CACHE_FILE_SUFFIX = ".downpurcache";
	public static final DefaultURLConnector DEFAULT_CONNECTOR = new DefaultURLConnector();
	
	/**
	 * Creates a new cache db
	 * 
	 * @param db the directory to put the caches in. The files will have a .downpourcache suffix
	 * 
	 * You should call {@link cleanup()} after instancing the DB.
	 */
	public DownpourCache(File db) {
		this.cacheDb = db;
		if (db.isFile()) {
			throw new IllegalStateException("DB needs to be a directory");
		} else if (!db.exists()) {
			db.mkdirs();
		}
		tempDir = new File(db, "temp");
		if (!tempDir.exists()) {
			tempDir.mkdirs();
		}
	}
	
	/**
	 * Deletes all caches older than {@link getMaxAge()}
	 * <br/>Does not do anything in offline mode
	 */
	public void cleanup() {
		if (!isOfflineMode()) {
			long currentTime = System.currentTimeMillis();
			File[] contents = cacheDb.listFiles();
			for (File file:contents) {
				if (file.isFile() && file.getAbsolutePath().endsWith(CACHE_FILE_SUFFIX)) {
					long lastModified = file.lastModified();
					if (currentTime - getMaxAge() > lastModified) {
						file.delete();
					}
				}
			}
			contents = tempDir.listFiles();
			for (File file:contents) {
				if (file.isFile()) {
					file.delete();
				}
			}
		}
	}
	
	/**
	 * Sets the maximum age a cached URL will survive in this cache
	 * 
	 * @param maxAge the maximum age of a cache file
	 * 
	 * Note that if in offline mode, no cache file will be deleted so longer offline trips are possible
	 */
	public void setMaxAge(long maxAge) {
		this.maxAge = maxAge;
	}
	
	/**
	 * Gets the maximum age a cached URL will survive in this cache
	 * @return the maximum age of a cache file
	 */
	public long getMaxAge() {
		return maxAge;
	}
	
	/**
	 * Sets whether this cache is offline, i.e. read from cache files instead of looking for data online
	 * @param offlineMode if this cache is in offline mode
	 */
	public void setOfflineMode(boolean offlineMode) {
		this.offlineMode = offlineMode;
	}
	
	/**
	 * Gets if this cache is in offline mode
	 * @return if this cache is in offline mode
	 */
	public boolean isOfflineMode() {
		return offlineMode;
	}
	
	/**
	 * If online and the cache file exists, reads from the cache file
	 * If online and the cache file doesn't exist, connects to the host and opens an InputStream that reads the url.
	 * If offline, reads from the cache file.
	 * @param url the URL to connect to
	 * @param connector the URLConnector to open an InputStream from an URL {@link URLConnector}
	 * @param force if true, doesn't use the cache file when online
	 * @return an InputStream that reads from the URL or the cached File
	 * @throws NoCacheException if offline and the cache file is missing
	 * @throws IOException if an IOException occurs during connecting or reading the cache file
	 */
	public InputStream get(URL url, URLConnector connector, boolean force) throws NoCacheException, IOException {
		File cacheFile = getCachedFile(url);
		if (isOfflineMode()) {
			if (cacheFile.exists()) {
				return new FileInputStream(cacheFile);
			} else {
				throw new NoCacheException("Cache does not contain expected file: [" + cacheFile.getPath() + "]");
			}
		} else {
			if (cacheFile.exists() && !force) {
				return new FileInputStream(cacheFile);
			} else {
				File temp = new File(tempDir, getCacheKey(url) + CACHE_FILE_SUFFIX);
				return connector.openURL(url, temp, cacheFile);
			}
		}
	}
	
	/**
	 * If online and the cache file exists, reads from the cache file
	 * If online and the cache file doesn't exist, connects to the host and opens an InputStream that reads the url.
	 * If offline, reads from the cache file.
	 * @param url the URL to connect to
	 * @param connector the URLConnector to open an InputStream from an URL {@link URLConnector}
	 * @return an InputStream that reads from the URL or the cached File
	 * @throws NoCacheException if offline and the cache file is missing
	 * @throws IOException if an IOException occurs during connecting or reading the cache file
	 */
	public InputStream get(URL url, URLConnector connector) throws NoCacheException, IOException {
		return get(url, connector, false);
	}
	
	/**
	 * If online, connects to the host and opens an InputStream that reads the url.
	 * If offline, reads from the cache file.
	 * @param url the URL to connect to
	 * @return an InputStream that reads from the URL or the cached File
	 * @throws NoCacheException if offline and the cache file is missing
	 * @throws IOException if an IOException occurs during connecting or reading the cache file
	 */
	public InputStream get(URL url) throws NoCacheException, IOException {
		return get(url, DEFAULT_CONNECTOR);
	}
	
	private File getCachedFile(URL url) {
		return new File(cacheDb, getCacheKey(url) + CACHE_FILE_SUFFIX);
	}
	
	private String getCacheKey(URL url) {
		// Sanitize string
		String path = url.toString();
		path = path.replaceAll("[^a-zA-Z]", "-");
		return (new StringBuilder()).append(path).append('-').append(url.toString().hashCode()).toString();
	}
}
