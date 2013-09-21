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
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.ByteBuffer;

import org.junit.Assert;
import org.junit.Test;

public class TestDownpourCache {
	public static final String TEST_URL = "https://raw.github.com/SpoutDev/Downpour/master/src/test/resources/test.json";

	@Test
	public void testCache() throws MalformedURLException, NoCacheException, IOException {
		File cacheDb = new File("cachedb");
		if (cacheDb.exists()) {
			for (File file : cacheDb.listFiles()) {
				file.delete();
			}
		}
		DownpourCache cache = new DownpourCache(cacheDb);
		InputStream in = cache.get(new URL(TEST_URL), DownpourCache.DEFAULT_CONNECTOR, true);
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
