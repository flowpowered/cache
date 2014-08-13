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
package com.flowpowered.jsoncache;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.ByteBuffer;

import org.junit.Assert;
import org.junit.Test;

public class TestJsonCache {
    public static final String TEST_URL = "https://raw.github.com/flow/flow-json-cache/master/src/test/resources/test.json";

    @Test
    public void testCache() throws MalformedURLException, NoCacheException, IOException {
        File cacheDb = new File("cachedb");
        if (cacheDb.exists()) {
            for (File file : cacheDb.listFiles()) {
                file.delete();
            }
        }
        JsonCache cache = new JsonCache(cacheDb);
        InputStream in = cache.get(new URL(TEST_URL), JsonCache.DEFAULT_CONNECTOR, true);
        ByteBuffer onlineTest = readFrom(in);
        cache.setOfflineMode(true);
        in = cache.get(new URL(TEST_URL));
        ByteBuffer offlineTest = readFrom(in);

        Assert.assertEquals("The offline copy doesn't match the online copy", onlineTest, offlineTest);
    }

    public ByteBuffer readFrom(InputStream in) throws IOException {
        ByteBuffer buffer = ByteBuffer.allocate(1024);
        int data;
        while ((data = in.read()) != -1) {
            if (!buffer.hasRemaining()) {
                ByteBuffer replace = ByteBuffer.allocate(buffer.capacity() + 1024);
                replace.put(buffer.array());
                buffer = replace;
            }
            buffer.put((byte) data);
        }
        in.close();
        return buffer;
    }
}
