package org.fastcatsearch.server;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.LinkedHashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Bootstrap {

	private static Bootstrap daemon;
	private Object serverDaemon = null;

	protected ClassLoader serverLoader = null;
	private final String serverClass = "com.fastcatsearch.server.CatServer";

	private String serverHome;
	private final String LIB_PATH = "/lib/"; 

	public static void main(String[] args) {

		if (daemon == null) {
			// Don't set daemon until init() has completed
			Bootstrap bootstrap = new Bootstrap();
			try {
				bootstrap.init();
			} catch (Throwable t) {
				t.printStackTrace();
				return;
			}
			daemon = bootstrap;
		} else {
			// When running as a service the call to stop will be on a new
			// thread so make sure the correct class loader is used to prevent
			// a range of class not found exceptions.
			Thread.currentThread().setContextClassLoader(daemon.serverLoader);
		}

		try {
			String command = "start";
			if (args.length > 0) {
				command = args[args.length - 1];
			}

			if (command.equals("run")) {
				args[args.length - 1] = "start";
				daemon.load(args);
				daemon.start();
			} else if (command.equals("start")) {
				daemon.load(args);
				daemon.start();
			} else if (command.equals("stop")) {
				daemon.stop();
			} else if (command.equals("configtest")) {
				daemon.load(args);
				System.exit(0);
			} else {
				System.err.println("Bootstrap: command \"" + command + "\" does not exist.");
			}
		} catch (Throwable t) {
			t.printStackTrace();
			System.exit(1);
		}

	}

	protected void usage() {
		System.out.println("usage: java " + getClass().getName() + " [ -help ]" + " {HomePath}");

	}

	public void init() throws Exception {
		serverHome = System.getProperty("server.home");

		if (serverHome == null) {
			System.err.println("Warning! Please set env variable \"server.home\".");
			usage();
			System.exit(1);
		}

		File f = new File(serverHome);
		if (!f.exists()) {
			System.err.println("Warning! Path \"" + serverHome + "\" is not exist!");
			usage();
			System.exit(1);
		}

		serverLoader = initClassLoader();

		Thread.currentThread().setContextClassLoader(serverLoader);

		// Load our startup class and call its process() method
		System.out.println("Loading startup class");
		Class<?> startupClass = serverLoader.loadClass(serverClass);
		Constructor<?> constructor = startupClass.getConstructor(new Class[] { String.class });
		serverDaemon = constructor.newInstance(new Object[] { serverHome });
	}

	public void load(String[] arguments) throws Exception {
		// 검색엔진으로 전달되는 args를 받아서 셋팅해준다.
		// 대부분 -D옵션을 통해 전달받으므로 아직까지는 셋팅할 내용은 없다.
		// Call the load() method
		String methodName = "load";
		Object param[];
		Class<?>[] paramTypes = null;
		if (arguments == null || arguments.length == 0) {
			paramTypes = null;
			param = null;
		} else {
			paramTypes = new Class[1];
			paramTypes[0] = arguments.getClass();
			param = new Object[1];
			param[0] = arguments;
		}
		Method method = serverDaemon.getClass().getMethod(methodName, paramTypes);
		method.invoke(serverDaemon, param);
	}

	public void initLibUrls(String libDir, Set<URL> set) throws IOException{
		File[] files = new File(libDir).listFiles();
		for (File file : files) {
			if (file.getName().matches(".*\\.jar$")) {
				file = file.getCanonicalFile();
				URL url = file.toURI().toURL();
				set.add(url);
			}
			if (file.isDirectory()) {
				initLibUrls(file.getAbsolutePath(), set);
			}
		}
	}

	private URLClassLoader initClassLoader() {
		String libDir = serverHome + LIB_PATH;

		Set<URL> set = new LinkedHashSet<URL>();
		try {
			initLibUrls(libDir, set);
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
		
		final URL[] urls = set.toArray(new URL[set.size()]);
		return new URLClassLoader(urls);
	}

	public void start() throws Exception {
		if (serverDaemon == null)
			init();

		Method method = serverDaemon.getClass().getMethod("start", (Class[]) null);
		method.invoke(serverDaemon, (Object[]) null);

	}

	public void stop() throws Exception {
		Method method = serverDaemon.getClass().getMethod("stop", (Class[]) null);
		method.invoke(serverDaemon, (Object[]) null);
	}
}
