package org.fastcatsearch.env;

import org.fastcatsearch.exception.FastcatSearchException;
import org.fastcatsearch.settings.Settings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.UUID;

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

    private final String serverId;

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
		logger.info("JAVA >> {} {}", System.getProperty("java.vendor"), System.getProperty("java.version"));
		logger.info("Setting Home = {}", home);
		logger.info("logback.configurationFile = {}", new File(new File(homeFile, "conf"), "logback.xml").getAbsolutePath());
        this.serverId = generateServerId();
        logger.info("Server ID = {}", serverId);
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

        //
        int bundleHashBucket = settingManager().getSystemSettings().getInt("bundleHashBucket", -1);
        int bundleMemMaxCount = settingManager().getSystemSettings().getInt("bundleMemMaxCount", -1);
        if(bundleHashBucket > 0) {
            System.setProperty("bundleHashBucket", String.valueOf(bundleHashBucket));
        }
        if(bundleMemMaxCount > 0) {
            System.setProperty("bundleMemMaxCount", String.valueOf(bundleMemMaxCount));
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

    public String getServerId() {
        return serverId;
    }
    private String generateServerId() {
        //
        //enumerate ethernet card list
        //
        try {
            Enumeration<NetworkInterface> nienum = NetworkInterface.getNetworkInterfaces();
            while (nienum.hasMoreElements()) {
                NetworkInterface ni = nienum.nextElement();
                if(logger.isTraceEnabled()) {
                    logger.trace("NetworkInterface {}({}) status : [{}:{}:{}]", ni.getName(),
                            ni.getInterfaceAddresses(), ni.isUp(), ni.isLoopback(), ni.isVirtual());
                }
                if(!ni.isUp() || ni.isLoopback() || ni.isVirtual()) {
                    continue;
                }
                boolean validInet = true;
                Enumeration<InetAddress> inetEnum = ni.getInetAddresses();
                while(inetEnum.hasMoreElements()) {
                    InetAddress inet = inetEnum.nextElement();
                    if(logger.isTraceEnabled()) {
                        logger.trace("InetAddress {} valid : !inet.isAnyLocalAddress()={}, ", ni.getName(), !inet.isAnyLocalAddress());
                    }

                    if(inet.isAnyLocalAddress()) {
                        validInet = false;
                    }
                }
                if(!validInet) { continue; }
                byte[] hardwareAddress = ni.getHardwareAddress();
                if (hardwareAddress != null) {
                    UUID uuid = UUID.nameUUIDFromBytes(hardwareAddress);
                    logger.trace("Valid uuid = {}", uuid);
                    return uuid.toString();
                }
            }
        } catch (SocketException e) {
            logger.error("",e);
        } catch (IOException e) {
            logger.error("",e);
        }
        return "";
    }
}
