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
package org.spout.downpour;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.DateTimeFormatterBuilder;

/**
 * The default URLConnector
 * 
 * Opens the URL with
 * <pre>url.openStream();</pre>
 */
public class DefaultURLConnector implements URLConnector {
	// Sat, 29 Oct 1994 19:43:31 GMT
	public static final DateTimeFormatter HTTP_DATE_TIME = (new DateTimeFormatterBuilder()).appendDayOfWeekShortText().appendLiteral(", ")
			.appendDayOfMonth(2).appendLiteral(' ')
			.appendMonthOfYearShortText().appendLiteral(' ')
			.appendYear(4,4).appendLiteral(' ')
			.appendHourOfDay(2).appendLiteral(':')
			.appendMinuteOfHour(2).appendLiteral(':')
			.appendSecondOfMinute(2).appendLiteral(" GMT").toFormatter();

	public InputStream openURL(URL url, final File temp, final File writeTo) throws IOException {
		URLConnection conn = url.openConnection();

		HttpURLConnection httpconn = null;
		if (url.getProtocol().equalsIgnoreCase("http")) {
			httpconn = (HttpURLConnection) conn;
		}

		// Check modified date
		DateTime modified = null;
		if (writeTo.exists()) {
			modified = new DateTime(writeTo.lastModified());
			conn.setRequestProperty("If-Modified-Since", modified.toString(HTTP_DATE_TIME));
		}
		setHeaders(conn);

		conn.connect();

		onConnected(conn);

		// Modified date handling. If server copy isn't newer than our cache, don't download again and use cached copy instead.

		// This checks if the server has replied with 304 NOT MODIFIED
		if (httpconn != null && httpconn.getResponseCode() == 304) { // not modified
			conn.getInputStream().close();
			conn.getOutputStream().close();
			return new FileInputStream(writeTo);
		}

		if (modified != null) {

			// This checks for the last modified date
			long i = conn.getHeaderFieldDate("Last-Modified", -1);
			DateTime serverModified = new DateTime(i, DateTimeZone.forOffsetHours(0));
			if (serverModified.isBefore(modified) || serverModified.isEqual(modified)) { // file hasn't changed
				try {
					conn.getInputStream().close();
				} catch (IOException ignore) { }
				try {
					conn.getOutputStream().close();
				} catch (IOException ignore) { }
				return new FileInputStream(writeTo);
			}
		}

		// Actually download the server copy
		CachingInputStream cache = new CachingInputStream(conn.getInputStream(), new FileOutputStream(temp));
		cache.setExpectedBytes(conn.getContentLength());

		// When successfully downloaded, move temp file to normal location
		cache.setOnFinish(new Runnable() {
			public void run() {
				if (writeTo.exists()) {
					writeTo.delete();
				}
				temp.renameTo(writeTo);
			}
		});

		// When failed, delete temp file
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
		// Nothing to do here
	}
}
