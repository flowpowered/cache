package org.spout.downpour;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

/**
 * The default URLConnector
 * 
 * Opens the URL with
 * <pre>url.openStream();</pre>
 */
public class DefaultURLConnector implements URLConnector {

	public InputStream openURL(URL url) throws IOException {
		return url.openStream();
	}

}
