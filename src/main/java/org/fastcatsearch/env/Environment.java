package org.fastcatsearch.env;

import java.io.File;

import org.fastcatsearch.common.DynamicClassLoader;
import org.fastcatsearch.module.ModuleException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Environment {
	
	private static final Logger logger = LoggerFactory.getLogger(Environment.class);
	
	public static final String FILE_SEPARATOR = System.getProperty("file.separator");
	public static final String PATH_SEPARATOR = System.getProperty("path.separator");
	public static final String LINE_SEPARATOR = System.getProperty("line.separator");
	public static final String OS_NAME = System.getProperty("os.name");
	
	private String home = "";
	private File homeFile;
	
//	private DynamicClassLoader classLoader;
	private SettingManager settingManager;
	private FilePaths filePaths;
	
	public Environment(String homeDirPath){
		home = homeDirPath;
		homeFile= new File(homeDirPath);
		
		if (home.length() > 0 && !home.endsWith(FILE_SEPARATOR)) {
			home = home + FILE_SEPARATOR;
		}
		logger.info("Setting Home = {}", home);
	}
	
	public Environment init(){
		settingManager = new SettingManager(this);
		filePaths = new FilePaths(this);
		
//		classLoader = new DynamicClassLoader(this, settingManager.getSettings().getSubSettings("module").getSubSettings("dynamic_classloader"));
//		classLoader.load();
//		classLoader.asSingleton();
		
		return this;
	}
	
	public String home() {
		return home;
	}
	public File homeFile() {
		return homeFile;
	}
	
	public SettingManager settingManager(){
		return settingManager;
	}
	
	public FilePaths filePaths(){
		return filePaths;
	}
	
//	public DynamicClassLoader classLoader(){
//		return classLoader;
//	}
	
}
