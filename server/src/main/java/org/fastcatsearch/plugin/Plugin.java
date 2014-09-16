package org.fastcatsearch.plugin;

import org.fastcatsearch.db.InternalDBModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
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
	protected File licenseFile;

	public Plugin(File pluginDir, PluginSetting pluginSetting){
		this.pluginDir = pluginDir;
		this.pluginSetting = pluginSetting;
		this.pluginId = pluginSetting.getId();
        this.licenseFile = new File(pluginDir, "license.txt");
	}
	
	public String pluginId(){
		return pluginId;
	}
	
	public final void load(boolean isMaster) throws LicenseInvalidException {
        if(licenseFile != null) {

            InputStream licenseInputStream = null;
            try{
                try {
                    licenseInputStream = new FileInputStream(licenseFile);
                } catch (FileNotFoundException e) {
                    //ignore
                }

                validateLicense(licenseInputStream);
            } finally {
                if (licenseInputStream != null) {
                    try {
                        licenseInputStream.close();
                    } catch (IOException e) {
                        //ignore
                    }
                }
            }
        }

		boolean isLoadDb = isMaster && pluginSetting.isUseDB();
		if(isLoadDb){
			loadDB();
		}
		doLoad(isLoadDb);
		isLoaded = true;
	}

    /**
    * To be override for license validation
    * @Param licenseInputStream License file inputstream. It can be null if file is not exist.
    * */
    protected boolean validateLicense(InputStream licenseInputStream) throws LicenseInvalidException {
        return true;
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
	
	protected abstract void doLoad(boolean isLoadDb);
	
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
