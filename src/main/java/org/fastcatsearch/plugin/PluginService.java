package org.fastcatsearch.plugin;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.fastcatsearch.common.DynamicClassLoader;
import org.fastcatsearch.env.Environment;
import org.fastcatsearch.service.AbstractService;
import org.fastcatsearch.service.ServiceException;
import org.fastcatsearch.service.ServiceManager;
import org.fastcatsearch.settings.Settings;

public class PluginService extends AbstractService {

	private static PluginService instance;
	
	private List<Plugin> pluginList;
	
	public static PluginService getInstance(){
		return instance;
	}
	
	public PluginService(Environment environment, Settings settings, ServiceManager serviceManager) {
		super(environment, settings, serviceManager);
	} 

	@Override
	public void asSingleton() {
		instance = this;
	}

	@Override
	protected boolean doStart() throws ServiceException {
		pluginList = new ArrayList<Plugin>();
		//플러그인을 검색하여 
		//무작위로 시작한다.
		File pluginRootDir = environment.filePaths().getFile("plugin");
		
		List<File> pluginDirList = new ArrayList<File>(); 
		findPluginDirectory(pluginRootDir, pluginDirList);
		try {
			JAXBContext jc = JAXBContext.newInstance(PluginSetting.class);
			int i = 0;
			for (File dir : pluginDirList) {
				File[] jarFiles = findFiles(dir, "jar");
				logger.debug("FOUND plugin {}, jar={}", dir.getAbsolutePath(), jarFiles);
				File[] warFiles = findFiles(dir, "war");
				DynamicClassLoader.add("plugin_" + i +"_"+ dir.getName(), jarFiles);
				i++;
				
				//
				//TODO war를 압축을 풀어서 webapp 디렉토리에 복사해준다.
				//
				File pluginConfigFile = new File(dir, "plugin.xml");
		        Unmarshaller unmarshaller = jc.createUnmarshaller();
		        try {
					PluginSetting setting = (PluginSetting) unmarshaller.unmarshal(new FileInputStream(pluginConfigFile));
					String className = setting.getClassName();
					Plugin plugin = null;
					if(className != null && className.length() > 0){
						plugin = DynamicClassLoader.loadObject(className, Plugin.class, new Class<?>[]{File.class, PluginSetting.class}, new Object[]{dir, setting});
					}else{
						plugin = new Plugin(dir, setting);
					}
					plugin.load();
					logger.debug("PLUGIN >> {}", plugin.getClass().getName());
					pluginList.add(plugin);
				} catch (FileNotFoundException e) {
					throw new ServiceException("plugin 설정파일을 읽을수 없음."); 
				} catch (JAXBException e) {
					throw new ServiceException("plugin 설정파일을 읽는중 에러.", e);
				}
			}
		} catch (JAXBException e) {
			throw new ServiceException("plugin설정파일 unmarshall 에러.", e);
		}
		
		return false;
	}

	
	private void findPluginDirectory(File pluginRootDir, List<File> pluginList) {
		File[] files = pluginRootDir.listFiles();
		for(File file : files) {
			if(file.isDirectory()){
				logger.debug("check dir {}", file.getAbsolutePath());
				if(new File(file, "plugin.xml").exists()){
					pluginList.add(file);
				}
				findPluginDirectory(file, pluginList);
			}
		}
	}
	
	private File[] findFiles(File dir, String extension){
		final String pattern = "."+extension;
		return dir.listFiles(new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				if(name.endsWith(pattern)){
					return true;
				}
				return false;
			}
		});
	}
	
	@Override
	protected boolean doStop() throws ServiceException {
		pluginList.clear();
		pluginList = null;
		return false;
	}

	@Override
	protected boolean doClose() throws ServiceException {
		// TODO Auto-generated method stub
		return false;
	}

	public List<Plugin> getPlugins() {
		return pluginList;
	}

}
