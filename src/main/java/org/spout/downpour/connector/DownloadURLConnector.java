/*
 * This file is part of Downpour.
 *
 * Copyright (c) 2012 Spout LLC <http://www.spout.org/>
 * Downpour is licensed under the GNU Lesser General Public License.
 *
 * Downpour is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Downpour is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.spout.downpour.connector;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

import org.spout.downpour.CachingInputStream;

/**
 * The default URLConnector.
 *
 * Opens the URL with <pre>url.openStream();</pre>
 */
public class DownloadURLConnector implements URLConnector {
	public InputStream openURL(URL url, File temp, File writeTo) throws IOException {
		URLConnection conn = url.openConnection();

		setHeaders(conn);

		// Set the user agent for the request.
		System.setProperty("http.agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/535.19 (KHTML, like Gecko) Chrome/18.0.1025.162 Safari/535.19");
		conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/535.19 (KHTML, like Gecko) Chrome/18.0.1025.162 Safari/535.19");

		conn.connect();

		onConnected(conn);

		return download(conn, temp, writeTo);
	}

	protected CachingInputStream download(URLConnection conn, final File temp, final File writeTo) throws IOException {
		// Download the server copy.
		CachingInputStream cache = new CachingInputStream(conn.getInputStream(), new FileOutputStream(temp));
		cache.setExpectedBytes(conn.getContentLength());

		// When successfully downloaded, move temp file to normal location.
		cache.setOnFinish(new Runnable() {
			public void run() {
				if (writeTo.exists()) {
					writeTo.delete();
				}
				temp.renameTo(writeTo);
			}
		});

		// When failed, delete temp file.
		cache.setOnFailure(new Runnable() {
			public void run() {
				temp.delete();
			}
		});

		return cache;
	}

	public void setHeaders(URLConnection connection) {
		connection.setConnectTimeout(5000);
		connection.setReadTimeout(5000);
	}

	public void onConnected(URLConnection connection) {
		// Nothing to do here.
	}
}
