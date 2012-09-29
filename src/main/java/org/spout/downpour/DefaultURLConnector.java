package org.spout.downpour;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

public class DefaultURLConnector implements URLConnector {

	public InputStream openURL(URL url) throws IOException {
		return url.openStream();
	}

}
