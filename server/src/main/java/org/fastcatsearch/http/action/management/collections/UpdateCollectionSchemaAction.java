package org.fastcatsearch.http.action.management.collections;

import java.io.File;
import java.lang.reflect.Constructor;
import java.util.Collection;
import java.util.List;

import org.fastcatsearch.datasource.SourceModifier;
import org.fastcatsearch.datasource.reader.SingleSourceReader;
import org.fastcatsearch.http.ActionAuthority;
import org.fastcatsearch.http.ActionAuthorityLevel;
import org.fastcatsearch.http.ActionMapping;
import org.fastcatsearch.http.action.ActionRequest;
import org.fastcatsearch.http.action.ActionResponse;
import org.fastcatsearch.http.action.AuthAction;
import org.fastcatsearch.ir.IRService;
import org.fastcatsearch.ir.config.CollectionContext;
import org.fastcatsearch.ir.config.DataSourceConfig;
import org.fastcatsearch.ir.config.SingleSourceConfig;
import org.fastcatsearch.ir.settings.AnalyzerSetting;
import org.fastcatsearch.ir.settings.FieldSetting;
import org.fastcatsearch.ir.settings.Schema;
import org.fastcatsearch.ir.settings.SchemaInvalidateException;
import org.fastcatsearch.ir.settings.SchemaSetting;
import org.fastcatsearch.plugin.Plugin;
import org.fastcatsearch.plugin.PluginService;
import org.fastcatsearch.plugin.analysis.AnalysisPlugin;
import org.fastcatsearch.plugin.analysis.AnalysisPluginSetting;
import org.fastcatsearch.plugin.analysis.AnalysisPluginSetting.Analyzer;
import org.fastcatsearch.service.ServiceManager;
import org.fastcatsearch.settings.SettingFileNames;
import org.fastcatsearch.util.DynamicClassLoader;
import org.fastcatsearch.util.FilePaths;
import org.fastcatsearch.util.JAXBConfigs;
import org.fastcatsearch.util.ResponseWriter;
import org.fastcatsearch.util.SchemaSettingUtil;
import org.json.JSONObject;

/**
 * 
 * */
@ActionMapping(value = "/management/collections/schema/update", authority = ActionAuthority.Collections, authorityLevel = ActionAuthorityLevel.WRITABLE)
public class UpdateCollectionSchemaAction extends AuthAction {

	@Override
	public void doAuthAction(ActionRequest request, ActionResponse response) throws Exception {

		String collectionId = request.getParameter("collectionId");
		String type = request.getParameter("type");
		String schemaJSONString = request.getParameter("schemaObject");
		boolean makeAutoSchema = "y".equals(request.getParameter("autoSchema"));
		
		boolean isSuccess = true;
		String errorMessage = "";
		
		try{
			
			IRService irService = ServiceManager.getInstance().getService(IRService.class);
			
			CollectionContext collectionContext = irService.collectionContext(collectionId);
			FilePaths collectionFilePaths = collectionContext.collectionFilePaths();
			File collectionDir = collectionFilePaths.file();
			File schemaFile = null;
			
			SchemaSetting schemaSetting = null;
			
			if(makeAutoSchema) {
				DataSourceConfig dataSourceConfig = collectionContext.dataSourceConfig();
				List<SingleSourceConfig> sourceConfigs = dataSourceConfig.getFullIndexingSourceConfig();
				if(sourceConfigs.size() > 0) {
					SingleSourceConfig config = sourceConfigs.get(0);
					config.getSourceReader();
					
					try {
						Class<?> readerClass = DynamicClassLoader.loadClass(config.getSourceReader());
						Constructor<?> constructor = readerClass.getConstructor(
							File.class, DataSourceConfig.class,
								SingleSourceConfig.class, SourceModifier.class,
								String.class);
							SingleSourceReader<?> sreader = (SingleSourceReader<?>)constructor.newInstance(null,dataSourceConfig,config,null,null);
							SchemaSetting tempSchema = sreader.surmiseSchema();
							if(tempSchema!=null) {
								schemaSetting = tempSchema;
								List<FieldSetting> fieldSettingList = tempSchema.getFieldSettingList();
								if(logger.isTraceEnabled()) {
									logger.trace("- field setting list -");
									for(FieldSetting fs : fieldSettingList) {
										logger.trace("fs:{}[{}]/{}", fs.getId(), fs.getName(), fs.getSize());
									}
								}
								//schemaFile = new File(collectionDir, SettingFileNames.workSchema);
								schemaFile = new File(collectionDir, SettingFileNames.workSchema);
								collectionContext.setWorkSchemaSetting(schemaSetting);
								
								JAXBConfigs.writeConfig(schemaFile, schemaSetting, SchemaSetting.class);
							}
					} finally {
					}
				}
				//dataSourceConfig.getFullIndexingSourceConfig().
			} else {
			
				JSONObject schemaObject = new JSONObject(schemaJSONString);
				
				logger.debug("schemaObject > {}", schemaObject.toString(4));
				
				// schema json string으로	 SchemaSetting을 만든다.
				schemaSetting = SchemaSettingUtil.convertSchemaSetting(schemaObject);
			}
			
			//일단 json object를 schema validation체크수행한다.
			schemaSetting.isValid();
			
			isValidPlugin(schemaSetting);
			
			schemaSetting.getAnalyzerSettingList();
			
			if ("workSchema".equalsIgnoreCase(type)) {
				schemaFile = new File(collectionDir, SettingFileNames.workSchema);
			}else{
				schemaFile = new File(collectionDir, SettingFileNames.schema);
			}
			
			//저장한다.
			JAXBConfigs.writeConfig(schemaFile, schemaSetting, SchemaSetting.class);
		
			//
			if ("workSchema".equalsIgnoreCase(type)) {
				collectionContext.setWorkSchemaSetting(schemaSetting);
			}else{
				collectionContext.setSchema(new Schema(schemaSetting));
			}
			
		}catch(SchemaInvalidateException e){
			//스키마 오류의 각종 정보를 추출하여 호출자에 전달해 준다.
			isSuccess = false;
			errorMessage = e.getMessage();
			logger.error("{}", errorMessage);
		}catch(Exception e){
			logger.error("",e);
			isSuccess = false;
			errorMessage = e.getMessage();
		}
		
		ResponseWriter responseWriter = getDefaultResponseWriter(response.getWriter());
		responseWriter.object();
		responseWriter.key("success").value(isSuccess);
		if(errorMessage != null){
			responseWriter.key("errorMessage").value(errorMessage);
		}
		responseWriter.endObject();
		responseWriter.done();

	}

	/**
	 * 플러그인 세팅이 정상적인지 확인한다.
	 * @param schemaSetting
	 * @throws SchemaInvalidateException
	 */
	private void isValidPlugin(SchemaSetting schemaSetting) throws SchemaInvalidateException {
		
		boolean found = false;
		
		String section = AnalyzerSetting.class.getName();
		String field = "className";
		String data = null;
		String type = "";
		
		List<AnalyzerSetting> analyzerSettingList = schemaSetting.getAnalyzerSettingList();
		
		PluginService pluginService = ServiceManager.getInstance().getService(PluginService.class);
		
		Collection<Plugin> plugins = pluginService.getPlugins();
		
		for (AnalyzerSetting analyzerSetting : analyzerSettingList) {
			
			data = analyzerSetting.getClassName();
			String[] classNames = data.split("[.]");
			
			logger.trace("class name : {}", data);
			
			//플러그인 세팅 중 className 은 {플러그인ID}.{분석기ID} 로 이루어 져 있으며, 
			//이 둘을 모두 확인 하려면 다음과 같은 과정을 거친다.
			
			if(classNames.length >= 2) {
				String pluginId = classNames[0];
				String analyzerId = classNames[1];
				
				for(Plugin plugin : plugins) {
					
					if(plugin instanceof AnalysisPlugin) {
						
						@SuppressWarnings("rawtypes")
						AnalysisPlugin analysisPlugin = (AnalysisPlugin)plugin;
						AnalysisPluginSetting pluginSetting = analysisPlugin.getPluginSetting();
						List<Analyzer> analyzerList = pluginSetting.getAnalyzerList();
						
						logger.trace("compare plugin {} : setting {}", pluginId, pluginSetting.getId());
						
						if(pluginId.equalsIgnoreCase(pluginSetting.getId())) {
							
							for(Analyzer analyzer : analyzerList) {
								
								logger.trace("compare analyzer {} : setting {}", analyzerId, analyzer.getId());
								
								data = analyzerId;
								
								if(analyzerId.equalsIgnoreCase(analyzer.getId())) {
									
									found = true;
									break;
								}
							}
						}
					}
					if(found) {
						break;
					}
				}
			} else {
				
				throw new SchemaInvalidateException(section, field, data, "NO_PLUGIN");
			}
		}
		
		if(!found) {
			
			throw new SchemaInvalidateException(section, field, data, "NO_ANALYZER");
		}
	}

}
