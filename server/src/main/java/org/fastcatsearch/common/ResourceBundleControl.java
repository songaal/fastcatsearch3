package org.fastcatsearch.common;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;
import java.util.Enumeration;
import java.util.Locale;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;
import java.util.ResourceBundle.Control;

import org.fastcatsearch.util.DynamicClassLoader;

public class ResourceBundleControl extends Control {
	
	private Charset charset;
	
	public ResourceBundleControl(Charset charset){
		this.charset = charset;
	}
	@Override
	public ResourceBundle newBundle(String baseName, Locale locale, String format, ClassLoader loader, boolean reload)
			throws IllegalAccessException, InstantiationException, IOException {
		// The below is a copy of the default implementation.
		String bundleName = toBundleName(baseName, locale);
		String resourceName = toResourceName(bundleName, "txt");
		ResourceBundle bundle = null;
		InputStream stream = null;
		Enumeration<URL> resources = DynamicClassLoader.getResources(resourceName);
		URL url = null;
		for(;resources.hasMoreElements();) {
			url = resources.nextElement();
			if (url != null) {
				URLConnection connection = url.openConnection();
				if (connection != null) {
					connection.setUseCaches(false);
					stream = connection.getInputStream();
					break;
				}
			}
		}
		if (stream != null) {
			try {
				bundle = new PropertyResourceBundle(new InputStreamReader(stream, charset));
			} finally {
				stream.close();
			}
		}
		return bundle;
	}
}
