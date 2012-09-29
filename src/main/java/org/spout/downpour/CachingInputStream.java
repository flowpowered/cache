package org.spout.downpour;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;

public class CachingInputStream extends InputStream {
	private InputStream readFrom = null;
	private OutputStream writeTo = null;
	
	private ByteBuffer buffer = ByteBuffer.allocate(1024);

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
	
	@Override
	public void close() throws IOException {
		super.close();
		
		// Write remaining stuff to output
		writeTo.write(buffer.array(), 0, buffer.position() + 1);
		buffer = null;
		writeTo.close();
	}

}
