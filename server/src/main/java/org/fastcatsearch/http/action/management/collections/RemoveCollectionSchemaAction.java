package org.fastcatsearch.http.action.management.collections;

import java.io.File;

import org.fastcatsearch.http.ActionAuthority;
import org.fastcatsearch.http.ActionAuthorityLevel;
import org.fastcatsearch.http.ActionMapping;
import org.fastcatsearch.http.action.ActionRequest;
import org.fastcatsearch.http.action.ActionResponse;
import org.fastcatsearch.http.action.AuthAction;
import org.fastcatsearch.ir.IRService;
import org.fastcatsearch.ir.config.CollectionContext;
import org.fastcatsearch.ir.settings.Schema;
import org.fastcatsearch.ir.settings.SchemaSetting;
import org.fastcatsearch.service.ServiceManager;
import org.fastcatsearch.settings.SettingFileNames;
import org.fastcatsearch.util.FilePaths;
import org.fastcatsearch.util.JAXBConfigs;
import org.fastcatsearch.util.ResponseWriter;

@ActionMapping(value = "/management/collections/schema/remove", authority = ActionAuthority.Collections, authorityLevel = ActionAuthorityLevel.WRITABLE)
public class RemoveCollectionSchemaAction extends AuthAction {

	@Override
	public void doAuthAction(ActionRequest request, ActionResponse response) throws Exception {

		String collectionId = request.getParameter("collectionId");
		String type = request.getParameter("type");

		IRService irService = ServiceManager.getInstance().getService(IRService.class);

		CollectionContext collectionContext = irService.collectionContext(collectionId);

		FilePaths collectionFilePaths = collectionContext.collectionFilePaths();
		File collectionDir = collectionFilePaths.file();
		boolean isSuccess = true;
		String errorMessage = null;
		try {
			if ("workSchema".equalsIgnoreCase(type)) {
				collectionContext.setWorkSchemaSetting(null);

				File workSchemaFile = new File(collectionDir, SettingFileNames.workSchema);
				if (workSchemaFile.exists()) {
					workSchemaFile.delete();
				}

			} else {
				// 운영중인 schema는 null로 만들지 않고 비워준다.
				SchemaSetting emptySchemaSetting = new SchemaSetting();
				Schema newSchema = new Schema(emptySchemaSetting);
				collectionContext.setSchema(newSchema);

				File schemaFile = new File(collectionDir, SettingFileNames.schema);
				JAXBConfigs.writeConfig(schemaFile, emptySchemaSetting, SchemaSetting.class);
			}
		} catch (Throwable e) {
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
