package org.fastcatsearch.http.action.management.collections;

import java.io.File;
import java.util.Collection;
import java.util.List;

import org.fastcatsearch.http.ActionAuthority;
import org.fastcatsearch.http.ActionAuthorityLevel;
import org.fastcatsearch.http.ActionMapping;
import org.fastcatsearch.http.action.ActionRequest;
import org.fastcatsearch.http.action.ActionResponse;
import org.fastcatsearch.http.action.AuthAction;
import org.fastcatsearch.ir.IRService;
import org.fastcatsearch.ir.config.CollectionContext;
import org.fastcatsearch.ir.settings.AnalyzerSetting;
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

		boolean isSuccess = true;
		String errorMessage = "";

		try {

			IRService irService = ServiceManager.getInstance().getService(IRService.class);

			CollectionContext collectionContext = irService.collectionContext(collectionId);
			FilePaths collectionFilePaths = collectionContext.collectionFilePaths();
			File collectionDir = collectionFilePaths.file();
			File schemaFile = null;

			SchemaSetting schemaSetting = null;

			JSONObject schemaObject = new JSONObject(schemaJSONString);

//			logger.debug("schemaObject > {}", schemaObject.toString(4));

			// schema json string으로 SchemaSetting을 만든다.
			schemaSetting = SchemaSettingUtil.convertSchemaSetting(schemaObject);

			// 일단 json object를 schema validation체크수행한다.
			schemaSetting.isValid();

			isValidAnalyzer(schemaSetting);
//			schemaSetting.getAnalyzerSettingList();

			if ("workSchema".equalsIgnoreCase(type)) {
				schemaFile = new File(collectionDir, SettingFileNames.workSchema);
			} else {
				schemaFile = new File(collectionDir, SettingFileNames.schema);
			}

			// 저장한다.
			JAXBConfigs.writeConfig(schemaFile, schemaSetting, SchemaSetting.class);

			//
			if ("workSchema".equalsIgnoreCase(type)) {
				collectionContext.setWorkSchemaSetting(schemaSetting);
			} else {
				collectionContext.setSchema(new Schema(schemaSetting));
			}

		} catch (SchemaInvalidateException e) {
			// 스키마 오류의 각종 정보를 추출하여 호출자에 전달해 준다.
//			logger.debug("", e);
			isSuccess = false;
			errorMessage = e.getMessage();
		} catch (Exception e) {
			logger.error("", e);
			isSuccess = false;
			errorMessage = e.getMessage();
		}

		ResponseWriter responseWriter = getDefaultResponseWriter(response.getWriter());
		responseWriter.object();
		responseWriter.key("success").value(isSuccess);
		if (errorMessage != null) {
			responseWriter.key("errorMessage").value(errorMessage);
		}
		responseWriter.endObject();
		responseWriter.done();

	}

	/**
	 * 플러그인 세팅이 정상적인지 확인한다.
	 * 
	 * @param schemaSetting
	 * @throws SchemaInvalidateException
	 */
	private void isValidAnalyzer(SchemaSetting schemaSetting) throws SchemaInvalidateException {

		boolean found = false;

		String section = "Analyzers";
		String fieldId = "";
		String attributeId = "analyzer";
		String value = null;

		List<AnalyzerSetting> analyzerSettingList = schemaSetting.getAnalyzerSettingList();

		if(analyzerSettingList == null || analyzerSettingList.size() == 0){
			//설정이 없을수도 있다.
			return;
		}
		
		PluginService pluginService = ServiceManager.getInstance().getService(PluginService.class);

		Collection<Plugin> plugins = pluginService.getPlugins();

		for (AnalyzerSetting analyzerSetting : analyzerSettingList) {
			fieldId = analyzerSetting.getId();
			value = analyzerSetting.getClassName();
			String[] className = value.split("[.]");

			logger.trace("class name : {}", value);

			// 플러그인 세팅 중 className 은 {플러그인ID}.{분석기ID} 로 이루어 져 있으며,
			// 이 둘을 모두 확인 하려면 다음과 같은 과정을 거친다.

			if (className.length == 2) {
				String pluginId = className[0];
				String analyzerId = className[1];

				for (Plugin plugin : plugins) {

					if (plugin instanceof AnalysisPlugin) {

						@SuppressWarnings("rawtypes")
						AnalysisPlugin analysisPlugin = (AnalysisPlugin) plugin;
						AnalysisPluginSetting pluginSetting = analysisPlugin.getPluginSetting();
						List<Analyzer> analyzerList = pluginSetting.getAnalyzerList();

						logger.trace("compare plugin {} : setting {}", pluginId, pluginSetting.getId());

						if (pluginId.equalsIgnoreCase(pluginSetting.getId())) {

							for (Analyzer analyzer : analyzerList) {

								logger.trace("compare analyzer {} : setting {}", analyzerId, analyzer.getId());

								value = analyzerId;

								if (analyzerId.equalsIgnoreCase(analyzer.getId())) {

									found = true;
									break;
								}
							}
						}
					}
					if (found) {
						break;
					}
				}
			}
		}

		if (!found) {
			throw new SchemaInvalidateException(section, fieldId, attributeId, value, "is not found");
		}
	}

}
