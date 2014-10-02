package org.fastcatsearch.server;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Bootstrap for WAS(Tomcat..)
 * Place bootstrap.jar file in <web app>/</web>WEB-INF/lib directory, and install fastcatsearch package at <web app>/WEB-INF/fastcatsearch.
 * Created by swsong on 2014. 10. 2..
 */
public class BootstrapServlet extends HttpServlet {

    private Object serverDaemon;

    protected ClassLoader serverLoader;
    private final String serverClass = "org.fastcatsearch.server.CatServer";

    private String serverHome;
    private final String SEARCH_HOME_PATH = "/WEB-INF/fastcatsearch";
    private final String LIB_PATH = "/lib/";

    @Override
    public void init() throws ServletException {
        String serverHome = getServletContext().getRealPath(SEARCH_HOME_PATH);
        File f = new File(serverHome);
        if (!f.exists()) {
            System.err.println("Warning! Path \"" + serverHome + "\" is not exist!");
            return;
        }

        try {
            serverLoader = initClassLoader();
            Thread.currentThread().setContextClassLoader(serverLoader);
            // Load our startup class and call its process() method
            Class<?> startupClass = serverLoader.loadClass(serverClass);
            Constructor<?> constructor = startupClass.getConstructor(new Class[]{String.class});
            serverDaemon = constructor.newInstance(new Object[]{serverHome});

            Thread.currentThread().setContextClassLoader(serverLoader);

            Method method = serverDaemon.getClass().getMethod("start", (Class[]) null);
            method.invoke(serverDaemon, (Object[]) null);
        } catch (Exception e) {
            throw new ServletException(e);
        }

    }

    @Override
    public void destroy() {
        try {
            Method method = serverDaemon.getClass().getMethod("stop", (Class[]) null);
            method.invoke(serverDaemon, (Object[]) null);
            serverLoader = null;
            serverDaemon = null;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void initLibUrls(String libDir, Set<URL> set) throws IOException {
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
}

