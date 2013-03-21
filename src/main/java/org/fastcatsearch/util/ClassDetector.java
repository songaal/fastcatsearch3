package org.fastcatsearch.util;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public abstract class ClassDetector<E> {
	
	public List<E> detectClass(String pkg) {
		List<E> classes = new ArrayList<E>();
		ClassLoader clsldr = getClass().getClassLoader();
		String path = pkg.replace(".", "/");
		try {
			Enumeration<URL> em = clsldr.getResources(path);
			while(em.hasMoreElements()) {
				String urlstr = em.nextElement().toString();
				if(urlstr.startsWith("jar:file:")) {
					String jpath = urlstr.substring(9);
					int st = jpath.indexOf("!/");
					jpath = jpath.substring(0,st);
					JarFile jf = new JarFile(jpath);
					Enumeration<JarEntry>jee = jf.entries();
					while(jee.hasMoreElements()) {
						JarEntry je = jee.nextElement();
						String ename = je.getName();
						E ar = classify(ename,pkg);
						if(ar!=null) { classes.add(ar); }
						
					}
				} else  if(urlstr.startsWith("file:")) {
					File file = new File(urlstr.substring(5));
					File[] dir = file.listFiles();
					for(int i=0;i<dir.length;i++) {
						E ar = classify(pkg+dir[i].getName(),pkg);
						if(ar!=null) { classes.add(ar); }
					}
				}
			}
			return classes;
		} catch (IOException e) { }
		return null;
	}
	
	public abstract E classify(String ename, String pkg);
}
