package org.fastcatsearch.plugin;

import org.fastcatsearch.db.InternalDBModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public abstract class Plugin {
	protected static final Logger logger = LoggerFactory.getLogger(Plugin.class);
	
	protected File pluginDir;
	protected String pluginId;
	protected PluginSetting pluginSetting;
	protected InternalDBModule internalDBModule;
	protected boolean isLoaded;
    protected final String serverId;
    protected final String licenseKey;
	protected String licenseStatus;
    private final static String licenseFileName = "license.key";

    protected PluginLicenseInfo licenseInfo;

	public Plugin(File pluginDir, PluginSetting pluginSetting, String serverId) {
		this.pluginDir = pluginDir;
		this.pluginSetting = pluginSetting;
		this.pluginId = pluginSetting.getId();
        this.serverId = serverId;
        this.licenseKey = readLicenseKey();
	}

    protected void setLicenseInfo(PluginLicenseInfo licenseInfo) {
        this.licenseInfo = licenseInfo;
    }

    public PluginLicenseInfo getLicenseInfo() {
        return licenseInfo;
    }

    private String readLicenseKey() {
        File licenseKeyFile = new File(pluginDir, licenseFileName);
        if(licenseKeyFile.exists()) {
            BufferedReader br = null;
            try {
                br = new BufferedReader(new FileReader(licenseKeyFile));
                StringBuilder sb = new StringBuilder();
                String line = br.readLine();

                while (line != null) {
                    sb.append(line);
                    sb.append("\n");
                    line = br.readLine();
                }
                return sb.toString();
            } catch (IOException e) {
                logger.error("", e);
            } finally {
                if(br != null) {
                    try {
                        br.close();
                    } catch (IOException e) {
                        //ignore
                    }
                }
            }
        }
        return null;
    }

	public String pluginId(){
		return pluginId;
	}

	public String getLicenseStatus() {
		return licenseStatus != null ? licenseStatus : "";
	}
	public final void load(boolean isMasterNode) throws LicenseInvalidException {
		boolean isLoadDb = isMasterNode && pluginSetting.isUseDB();
		if(isLoadDb){
			loadDB();
		}
		try {
			doLoad(isLoadDb);
		}catch (LicenseInvalidException e) {
			licenseStatus = e.getMessage();
			throw e;
		}
        licenseStatus = "Valid";
        if(licenseInfo != null) {
            licenseStatus = licenseStatus + " (" + licenseInfo.getLicenseExpireDate() +  ")";
        }
		isLoaded = true;
	}

	public final void unload(){
		doUnload();
		if(pluginSetting.isUseDB()){
			unloadDB();
		}
		isLoaded = false;
	}
	
	public final void reload(boolean isMaster) throws LicenseInvalidException {
		unload();
		load(isMaster);
	}
	
	protected abstract void doLoad(boolean isLoadDb) throws LicenseInvalidException;
	
	protected abstract void doUnload();
	
	public boolean isLoaded(){
		return isLoaded;
	}
	
	private final void loadDB() {
		String dbPath = getPluginDBDataDir().getAbsolutePath();
		new File(dbPath).getParentFile().mkdirs();
		List<URL> mapperFileList = new ArrayList<URL>();
		addMapperFile(mapperFileList);
		internalDBModule = new InternalDBModule(dbPath, mapperFileList, null, null);
		internalDBModule.load();
	}

	protected abstract void addMapperFile(List<URL> mapperFileList);
	
	private void unloadDB() {
		if(internalDBModule != null){
			internalDBModule.unload();
		}
	}
	
	public InternalDBModule internalDBModule(){
		return internalDBModule;
	}
	
	public File getPluginDir(){
		return pluginDir;
	}
	protected File getPluginDBDir(){
		return new File(pluginDir, "db");
	}
	protected File getPluginDBDataDir(){
		return new File(getPluginDBDir(), "data");
	}
	protected File getPluginDBConfigDir(){
		return new File(getPluginDBDir(), "config");
	}
	public PluginSetting getPluginSetting(){
		return pluginSetting;
	}

}
