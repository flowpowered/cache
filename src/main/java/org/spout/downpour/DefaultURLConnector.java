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
	//  Sat, 29 Oct 1994 19:43:31 GMT
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
		DateTime modified = null;
		if (writeTo.exists()) {
			modified = new DateTime(writeTo.lastModified());
			conn.setRequestProperty("If-Modified-Since", modified.toString(HTTP_DATE_TIME));
		}
		conn.connect();
		if (modified != null) {
			long i = conn.getHeaderFieldDate("Last-Modified", -1);
			DateTime serverModified = new DateTime(i, DateTimeZone.forOffsetHours(0));
			if (serverModified.isBefore(modified) || serverModified.isEqual(modified)) { // file hasn't changed
				conn.getInputStream().close();
				conn.getOutputStream().close();
				return new FileInputStream(writeTo);
			}
		}
		if (httpconn != null && httpconn.getResponseCode() == 304) { // not modified
			conn.getInputStream().close();
			conn.getOutputStream().close();
			return new FileInputStream(writeTo);
		} else {
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
}
