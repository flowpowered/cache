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
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

import com.flowpowered.jsoncache.CachingInputStream;

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
