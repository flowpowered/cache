package org.spout.downpour;

import java.io.IOException;

/**
 * An exception that is thrown when the cache file was not found
 */
public class NoCacheException extends IOException {
	private static final long serialVersionUID = 1L;
	private final String message;
	public NoCacheException(String message) {
		this.message = message;
	}

	@Override
	public String getMessage() {
		return message;
	}
}
