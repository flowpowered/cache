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
	public static final String TEST_URL = "http://get.spout.org/about.yml";
	
	@Test
	public void testCache() throws MalformedURLException, NoCacheException, IOException {
		DownpourCache cache = new DownpourCache(new File("cachedb"));
		InputStream in = cache.get(new URL(TEST_URL));
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
