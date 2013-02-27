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
import java.net.URL;
import java.net.URLConnection;

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
	public InputStream openURL(URL url, File temp, File writeTo) throws IOException;

	/**
	 * You can override this method to set your own header values when needed. This will be called before connection.
	 * @param connection
	 */
	public void setHeaders(URLConnection connection);
}
