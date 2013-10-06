package org.fastcatsearch.plugin;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.fastcatsearch.alert.ClusterAlertService;
import org.fastcatsearch.db.ConnectionManager;
import org.fastcatsearch.db.InternalDBModule;
import org.fastcatsearch.db.dao.DAOBase;
import org.fastcatsearch.env.Environment;
import org.fastcatsearch.exception.FastcatSearchException;
import org.fastcatsearch.plugin.AnalysisPluginSetting.Dictionary;
import org.fastcatsearch.service.AbstractService;
import org.fastcatsearch.service.ServiceManager;
import org.fastcatsearch.settings.Settings;
import org.fastcatsearch.util.DynamicClassLoader;

public class PluginService extends AbstractService {

	private Map<String, Plugin> pluginMap;

	public PluginService(Environment environment, Settings settings, ServiceManager serviceManager) {
		super(environment, settings, serviceManager);
	}

	@Override
	protected boolean doStart() throws FastcatSearchException {
		pluginMap = new HashMap<String, Plugin>();
		// 플러그인을 검색하여
		// 무작위로 시작한다.
		File pluginRootDir = environment.filePaths().file("plugin");
		if (!pluginRootDir.exists()) {
			logger.info("플러그인 디렉토리가 없어서 플러그인을 로딩하지 않습니다. {}", pluginRootDir.getAbsolutePath());
			return true;
		}
		List<File> pluginDirList = new ArrayList<File>();
		findPluginDirectory(pluginRootDir, pluginDirList);

		// 모든 plugin의 jar파일을 로딩.
		int i = 0;
		List<File> analysisFiles = new ArrayList<File>();
		for (File dir : pluginDirList) {
			File[] jarFiles = findFiles(dir, "jar");
			logger.debug("FOUND plugin {}, jar={}", dir.getAbsolutePath(), jarFiles);
			//analysis 라이브러리는 모두 묶어서 한번에 로딩한다. 서로 dependency가 존재할수 있기때문에..
			if (dir.getAbsolutePath().contains("analysis")) {
				// analysis
				logger.debug("analysis >> {}", dir.getAbsolutePath());
				for (File f : jarFiles) {
					analysisFiles.add(f);
				}
			} else {
				String tag = i++ + "_plugin_" + dir.getName();
				DynamicClassLoader.add(tag, jarFiles);
				logger.debug("Add plugin {}, jar={}", tag, jarFiles);
			}
		}

		// analyzer는 class가 서로의존관계가 있을수 있으므로, 한번에 묶어서 클래스패스잡음.
		if (analysisFiles.size() > 0) {
			DynamicClassLoader.add("plugin_analysis", analysisFiles);
			logger.debug("Add plugin plugin_analysis, jar={}", analysisFiles);
		}

		try {
			JAXBContext jc = JAXBContext.newInstance(DefaultPluginSetting.class);
			JAXBContext analysisJc = JAXBContext.newInstance(AnalysisPluginSetting.class);
			Unmarshaller unmarshaller = jc.createUnmarshaller();
			Unmarshaller analysisUnmarshaller = analysisJc.createUnmarshaller();
			for (File dir : pluginDirList) {
				boolean isAnalysis = dir.getAbsolutePath().contains("analysis");
				File pluginConfigFile = new File(dir, "plugin.xml");
				if(!pluginConfigFile.exists()){
					continue;
				}
				/*
				 * 1. Plugin 객체생성.
				 */
				PluginSetting setting = null;
				try {
					InputStream is = new FileInputStream(pluginConfigFile);
					if(isAnalysis){
						setting = (PluginSetting) analysisUnmarshaller.unmarshal(is);
					}else{
						setting = (PluginSetting) unmarshaller.unmarshal(is);
						
					}
					is.close();
					logger.debug("PluginSetting >>> {}, {}", setting, pluginConfigFile.getAbsolutePath());
					String className = setting.getClassName();
					Plugin plugin = null;
					if (className != null && className.length() > 0) {
						plugin = DynamicClassLoader.loadObject(className, Plugin.class, new Class<?>[] { File.class, PluginSetting.class },
								new Object[] { dir, setting });
						plugin.load();
						logger.debug("PLUGIN {} >> {}", setting.getId(), plugin.getClass().getName());
						pluginMap.put(setting.getId(), plugin);
//					} else {
//						plugin = new Plugin(dir, setting);
					}

				} catch (FileNotFoundException e) {
					logger.error("{} plugin 설정파일을 읽을수 없음.", dir.getName());
					ClusterAlertService.getInstance().alert(new FastcatSearchException());
				} catch (JAXBException e) {
					logger.error("plugin 설정파일을 읽는중 에러. {}", e);
				} catch (IOException e) {
					logger.error("{}", e);
				}

				/*
				 * 2. Analysis db로딩.
				 */
//				try {
//					if (setting.getNamespace().equalsIgnoreCase("Analysis")) {
//						AnalysisPluginSetting analysisPluginSetting = (AnalysisPluginSetting) setting;
//						List<Dictionary> dictionaryList = analysisPluginSetting.getDictionaryList();
//						for (Dictionary dictionary : dictionaryList) {
//							String tableName = setting.getKey(dictionary.getId());
//							String daoClassName = dictionary.getDaoClass();
//							if (daoClassName != null && daoClassName.length() > 0) {
//								DAOBase daoBase = DynamicClassLoader.loadObject(daoClassName, DAOBase.class, new Class<?>[] { String.class,
//										ConnectionManager.class }, new Object[] { tableName, null });
//								internalDBModule.addDAO(tableName, daoBase);
//							}
//
//						}
//					}else{
//						//이곳은 다른 타입의 plugin에서 필요할수도 있다.
//					}
//				} catch (SQLException e) {
//					logger.error(setting.getId() +" Plugin DAO 생성중 에러. {}", e);
//				}
			}

		} catch (JAXBException e) {
			throw new FastcatSearchException("ERR-00200", e);
		}

		return true;
	}

	private void findPluginDirectory(File pluginRootDir, List<File> pluginList) {
		File[] files = pluginRootDir.listFiles();

		if (files == null) {
			return;
		}

		for (File file : files) {
			if (file.isDirectory()) {
				logger.debug("check dir {}", file.getAbsolutePath());
				if (new File(file, "plugin.xml").exists()) {
					pluginList.add(file);
				}
				findPluginDirectory(file, pluginList);
			}
		}
	}

	private File[] findFiles(File dir, String extension) {
		final String pattern = "." + extension;
		return dir.listFiles(new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				if (name.endsWith(pattern)) {
					return true;
				}
				return false;
			}
		});
	}

	@Override
	protected boolean doStop() throws FastcatSearchException {
		if (pluginMap != null) {
			pluginMap.clear();
			pluginMap = null;
		}
		return true;
	}

	@Override
	protected boolean doClose() throws FastcatSearchException {
		doStop();

		return true;
	}

	public Collection<Plugin> getPlugins() {
		return pluginMap.values();
	}

	public Plugin getPlugin(String pluginId) {
		return pluginMap.get(pluginId);
	}

}
