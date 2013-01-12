package org.spout.downpour;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;

/**
 * The default URLConnector
 * 
 * Opens the URL with
 * <pre>url.openStream();</pre>
 */
public class DefaultURLConnector implements URLConnector {
	public CachingInputStream openURL(URL url, final File temp, final File writeTo) throws IOException {
		URLConnection conn = url.openConnection();
		CachingInputStream cache = new CachingInputStream(conn.getInputStream(), new FileOutputStream(temp));
		cache.setExpectedBytes(conn.getContentLength());
		cache.setOnFinish(new Runnable() {
			public void run() {
				if (writeTo.exists()) {
					writeTo.delete();
				}
				temp.renameTo(writeTo);
			}
		});
		cache.setOnFailure(new Runnable() {
			public void run() {
				temp.delete();
			}
		});
		return cache;
	}
}
