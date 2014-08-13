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
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

/**
 * Returns an InputStream for a URL.
 *
 * For a default implementation, see {@link DefaultURLConnector}.
 */
public interface URLConnector {
    /**
     * Opens the URL and returns an InputStream on that URL.
     *
     * @param url the url to open.
     * @return the InputStream on said URL.
     * @throws IOException when an error occurs while opening the connection.
     */
    public InputStream openURL(URL url, File temp, File writeTo) throws IOException;

    /**
     * You can override this method to set your own header values when needed. This will be called before connection.
     */
    public void setHeaders(URLConnection connection);

    public void onConnected(URLConnection connection);
}
