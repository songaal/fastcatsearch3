package org.fastcatsearch.http.action.management.collections;

import java.io.File;
import java.io.OutputStream;

import org.fastcatsearch.http.ActionAuthority;
import org.fastcatsearch.http.ActionAuthorityLevel;
import org.fastcatsearch.http.ActionMapping;
import org.fastcatsearch.http.action.ActionRequest;
import org.fastcatsearch.http.action.ActionResponse;
import org.fastcatsearch.http.action.AuthAction;
import org.fastcatsearch.ir.IRService;
import org.fastcatsearch.ir.config.CollectionContext;
import org.fastcatsearch.ir.settings.SchemaSetting;
import org.fastcatsearch.service.ServiceManager;
import org.fastcatsearch.settings.SettingFileNames;
import org.fastcatsearch.util.FilePaths;
import org.fastcatsearch.util.JAXBConfigs;

@ActionMapping(value = "/management/collections/schema", authority = ActionAuthority.Collections, authorityLevel = ActionAuthorityLevel.READABLE)
public class GetCollectionSchemaAction extends AuthAction {

	@Override
	public void doAuthAction(ActionRequest request, ActionResponse response) throws Exception {
		
		String collectionId = request.getParameter("collectionId");
		String type = request.getParameter("type");
		String mode = request.getParameter("mode");
		
		
		IRService irService = ServiceManager.getInstance().getService(IRService.class);
		
		CollectionContext collectionContext = irService.collectionContext(collectionId);
		SchemaSetting schemaSetting = collectionContext.schema().schemaSetting();
		SchemaSetting workSchemaSetting = null;
		
		if("workSchema".equalsIgnoreCase(type)){
			workSchemaSetting = collectionContext.workSchemaSetting();
			if(workSchemaSetting == null){
				if("copyCurrentSchema".equalsIgnoreCase(mode)){
					
					FilePaths collectionFilePaths = collectionContext.collectionFilePaths();
					FilePaths dataFilePaths = collectionFilePaths.dataPaths();
					File collectionDir = collectionFilePaths.file();
					
					File workSchemaFile = new File(collectionDir, SettingFileNames.workSchema);
					//schema.xml을 workschema로 기록.
					JAXBConfigs.writeConfig(workSchemaFile, schemaSetting, SchemaSetting.class);
					//읽어들인다.
					workSchemaSetting = JAXBConfigs.readConfig(workSchemaFile, SchemaSetting.class);
					
					collectionContext.setWorkSchemaSetting(workSchemaSetting);
				}
			}
		}else{
			workSchemaSetting = schemaSetting;
		}
		
		
		if(workSchemaSetting == null){
			workSchemaSetting = new SchemaSetting();
		}
		OutputStream os = response.getOutputStream();
		JAXBConfigs.writeRawConfig(os, workSchemaSetting, SchemaSetting.class);
		
	}

}
