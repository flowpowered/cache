package org.spout.downpour;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

/**
 * Returns an InputStream for a URL.
 * 
 * For a default implementation, see {@link DefaultURLConnector}.
 */
public interface URLConnector {
	
	/**
	 * Opens the URL and returns an InputStream on that URL
	 * @param url the url to open
	 * @return the InputStream on said URL
	 * @throws IOException when an error occurs while opening the connection
	 */
	public InputStream openURL(URL url) throws IOException;
}
