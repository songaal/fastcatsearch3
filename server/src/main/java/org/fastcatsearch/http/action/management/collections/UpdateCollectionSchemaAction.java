package org.fastcatsearch.http.action.management.collections;

import java.io.File;

import org.fastcatsearch.http.ActionMapping;
import org.fastcatsearch.http.action.ActionRequest;
import org.fastcatsearch.http.action.ActionResponse;
import org.fastcatsearch.http.action.AuthAction;
import org.fastcatsearch.ir.IRService;
import org.fastcatsearch.ir.config.CollectionContext;
import org.fastcatsearch.ir.settings.Schema;
import org.fastcatsearch.ir.settings.SchemaInvalidateException;
import org.fastcatsearch.ir.settings.SchemaSetting;
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
@ActionMapping("/management/collections/schema/update")
public class UpdateCollectionSchemaAction extends AuthAction {

	@Override
	public void doAuthAction(ActionRequest request, ActionResponse response) throws Exception {

		String collectionId = request.getParameter("collectionId");
		String type = request.getParameter("type");
		String schemaJSONString = request.getParameter("schemaObject");
		JSONObject schemaObject = new JSONObject(schemaJSONString);
		
		//logger.debug("schemaJSONString > {}", schemaJSONString);
		logger.debug("schemaObject > {}", schemaObject.toString(4));
		boolean isSuccess = true;
		String errorMessage = "";
		
		try{
			// schema json string으로	 SchemaSetting을 만든다.
			SchemaSetting schemaSetting = SchemaSettingUtil.convertSchemaSetting(schemaObject);
			
			//일단 json object를 schema validation체크수행한다.
			schemaSetting.isValid();
			
			IRService irService = ServiceManager.getInstance().getService(IRService.class);
	
			CollectionContext collectionContext = irService.collectionContext(collectionId);
			FilePaths collectionFilePaths = collectionContext.collectionFilePaths();
			File collectionDir = collectionFilePaths.file();
			
			File schemaFile = null;
			if ("workSchema".equalsIgnoreCase(type)) {
				schemaFile = new File(collectionDir, SettingFileNames.workSchema);
			}else{
				schemaFile = new File(collectionDir, SettingFileNames.schema);
			}
			
			//저장한다.
			JAXBConfigs.writeConfig(schemaFile, schemaSetting, SchemaSetting.class);
		
			//
			if ("workSchema".equalsIgnoreCase(type)) {
				collectionContext.setSchema(new Schema(schemaSetting));
			}else{
				collectionContext.setWorkSchemaSetting(schemaSetting);
			}
			
		}catch(SchemaInvalidateException e){
			logger.error("",e);
			isSuccess = false;
			errorMessage = e.getMessage();
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

}
