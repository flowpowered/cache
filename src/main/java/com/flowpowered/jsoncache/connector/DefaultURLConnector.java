/*
 * This file is part of Flow JSON Cache, licensed under the MIT License (MIT).
 *
 * Copyright (c) 2012 Spout LLC <https://spout.org/>
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package com.flowpowered.jsoncache.connector;

import java.io.File;
import java.io.FileInputStream;
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
 * The default URLConnector.
 *
 * Opens the URL with <pre>url.openStream();</pre>
 */
public class DefaultURLConnector extends DownloadURLConnector implements URLConnector {
    // Sat, 29 Oct 1994 19:43:31 GMT
    public static final DateTimeFormatter HTTP_DATE_TIME = (new DateTimeFormatterBuilder()).appendDayOfWeekShortText().appendLiteral(", ")
            .appendDayOfMonth(2).appendLiteral(' ')
            .appendMonthOfYearShortText().appendLiteral(' ')
            .appendYear(4, 4).appendLiteral(' ')
            .appendHourOfDay(2).appendLiteral(':')
            .appendMinuteOfHour(2).appendLiteral(':')
            .appendSecondOfMinute(2).appendLiteral(" GMT").toFormatter();

    @Override
    public InputStream openURL(URL url, final File temp, final File writeTo) throws IOException {
        URLConnection conn = url.openConnection();

        HttpURLConnection httpconn = null;
        if (url.getProtocol().equalsIgnoreCase("http")) {
            httpconn = (HttpURLConnection) conn;
        }

        // Check modified date.
        DateTime modified = null;
        if (writeTo.exists()) {
            modified = new DateTime(writeTo.lastModified());
            conn.setRequestProperty("If-Modified-Since", modified.toString(HTTP_DATE_TIME));
        }

        setHeaders(conn);

        // Set the user agent for the request.
        System.setProperty("http.agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/535.19 (KHTML, like Gecko) Chrome/18.0.1025.162 Safari/535.19");
        conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/535.19 (KHTML, like Gecko) Chrome/18.0.1025.162 Safari/535.19");

        conn.connect();

        onConnected(conn);

        // Modified date handling. If server copy isn't newer than the cache, don't download again and use cached copy instead.

        // This checks if the server has replied with 304 NOT MODIFIED.
        if (httpconn != null && httpconn.getResponseCode() == 304) { // Not modified.
            try {
                conn.getInputStream().close();
            } catch (IOException ignore) {
            }
            try {
                conn.getOutputStream().close();
            } catch (IOException ignore) {
            }
            return new FileInputStream(writeTo);
        }

        if (modified != null) {
            // This checks for the last modified date.
            long i = conn.getHeaderFieldDate("Last-Modified", -1);
            DateTime serverModified = new DateTime(i, DateTimeZone.forOffsetHours(0));
            if (serverModified.isBefore(modified) || serverModified.isEqual(modified)) { // File hasn't changed.
                try {
                    conn.getInputStream().close();
                } catch (IOException ignore) {
                }
                try {
                    conn.getOutputStream().close();
                } catch (IOException ignore) {
                }
                return new FileInputStream(writeTo);
            }
        }

        return download(conn, temp, writeTo);
    }
}
