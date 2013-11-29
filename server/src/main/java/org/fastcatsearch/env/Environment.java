package org.fastcatsearch.env;

import java.io.File;

import org.fastcatsearch.exception.FastcatSearchException;
import org.fastcatsearch.settings.Settings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Environment {
	
	private static Logger logger;
	
	public static final String FILE_SEPARATOR = System.getProperty("file.separator");
	public static final String PATH_SEPARATOR = System.getProperty("path.separator");
	public static final String LINE_SEPARATOR = System.getProperty("line.separator");
	public static final String OS_NAME = System.getProperty("os.name");
	
	private String home = "";
	private File homeFile;
	
	private SettingManager settingManager;
	
	private String myNodeId;
	private String masterNodeId;
	private boolean isMasterNode;
	
	public Environment(String homeDirPath){
		home = homeDirPath;
		homeFile= new File(homeDirPath);
		
		if (home.length() > 0 && !home.endsWith(FILE_SEPARATOR)) {
			home = home + FILE_SEPARATOR;
		}
		
		System.setProperty("fastcatsearch.home", homeFile.getAbsolutePath());
		System.setProperty("logback.configurationFile", new File(new File(homeFile, "conf"), "logback.xml").getAbsolutePath());
		System.setProperty("log.path", new File(homeFile, "logs").getAbsolutePath());
		
		logger = LoggerFactory.getLogger(Environment.class);
		 
		logger.info("Setting Home = {}", home);
		logger.info("logback.configurationFile = {}", new File(new File(homeFile, "conf"), "logback.xml").getAbsolutePath());
	}
	
	public Environment init() throws FastcatSearchException {
		settingManager = new SettingManager(this);
		
		Settings idSettings = settingManager.getIdSettings();
		
		myNodeId = idSettings.getString("me");
		masterNodeId = idSettings.getString("master");
		int servicePort = idSettings.getInt("servicePort");
		
		
		if(myNodeId == null || myNodeId.length() == 0 || masterNodeId == null || masterNodeId.length() == 0 || servicePort == -1){
			throw new FastcatSearchException("ID 셋팅이 잘못되었습니다. me="+myNodeId+", master="+masterNodeId+", servicePort="+servicePort);
		}
		if(myNodeId.equals(masterNodeId)){
			isMasterNode = true;
		}
		
		logger.info("[ID] me[{}] master[{}] servicePort[{}]", myNodeId, masterNodeId, servicePort);
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
	
	public Path filePaths(){
		return new Path(homeFile);
	}

	public String myNodeId(){
		return myNodeId;
	}
	public String masterNodeId(){
		return masterNodeId;
	}
	public boolean isMasterNode(){
		return isMasterNode;
	}
}
