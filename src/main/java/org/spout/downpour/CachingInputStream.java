package org.spout.downpour;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;

/**
 * An InputStream implementation that reads from another InputStream while caching the data to an OutputStream
 */
public class CachingInputStream extends InputStream {
	private InputStream readFrom = null;
	private OutputStream writeTo = null;
	
	private ByteBuffer buffer = ByteBuffer.allocate(1024);

	/**
	 * Creates a new caching InputStream
	 * @param readFrom the stream to read data from
	 * @param writeTo the stream to cache the read data to
	 */
	public CachingInputStream(InputStream readFrom, OutputStream writeTo) {
		super();
		this.readFrom = readFrom;
		this.writeTo = writeTo;
	}

	@Override
	public int read() throws IOException {
		int data = readFrom.read();
		if (!buffer.hasRemaining()) { // Buffer is full
			// Write buffer to output
			writeTo.write(buffer.array(), 0, buffer.capacity());
			// Reset buffer
			buffer.position(0);
		}
		buffer.put((byte) data);
		return data;
	}
	
	/**
	 * Closes the stream it reads from and the stream it caches to
	 */
	@Override
	public void close() throws IOException {
		readFrom.close();
		super.close();
		
		// Write remaining stuff to output
		writeTo.write(buffer.array(), 0, buffer.position() + 1);
		buffer = null;
		writeTo.close();
	}

}
