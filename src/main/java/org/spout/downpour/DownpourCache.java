package org.spout.downpour;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.LinkedHashMap;
import java.util.Map.Entry;

public class DownpourCache {
	private boolean offlineMode = false;
	private long maxAge = 1000 * 60 * 60 * 24 * 7; // Keep for one week
	private File cacheDb = null;
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
		}
	}
	
	public void setMaxAge(long maxAge) {
		this.maxAge = maxAge;
	}
	
	public long getMaxAge() {
		return maxAge;
	}
	
	public void setOfflineMode(boolean offlineMode) {
		this.offlineMode = offlineMode;
	}
	
	public boolean isOfflineMode() {
		return offlineMode;
	}
	
	public InputStream get(URL url, URLConnector connector) throws NoCacheException, IOException {
		File cacheFile = getCachedFile(url);
		if (isOfflineMode()) {
			if (cacheFile.exists()) {
				return new FileInputStream(cacheFile);
			} else {
				throw new NoCacheException();
			}
		} else {
			InputStream readFrom = connector.openURL(url);
			OutputStream writeTo = new FileOutputStream(cacheFile);
			
			return new CachingInputStream(readFrom, writeTo);
		}
	}
	
	public InputStream get(URL url) throws NoCacheException, IOException {
		return get(url, DEFAULT_CONNECTOR);
	}
	
	private File getCachedFile(URL url) {
		return new File(cacheDb, getCacheKey(url) + CACHE_FILE_SUFFIX);
	}
	
	private String getCacheKey(URL url) {
		// TODO make better keys
		return url.hashCode() + "";
	}
}
