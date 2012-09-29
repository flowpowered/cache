package org.spout.downpour;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

public interface URLConnector {
	public InputStream openURL(URL url) throws IOException;
}
