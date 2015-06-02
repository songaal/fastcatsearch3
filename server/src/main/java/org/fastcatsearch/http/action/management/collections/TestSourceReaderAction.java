package org.fastcatsearch.http.action.management.collections;

import java.io.File;
import java.io.Writer;
import java.lang.reflect.Constructor;
import java.util.List;
import java.util.Map;

import org.fastcatsearch.datasource.SourceModifier;
import org.fastcatsearch.datasource.reader.DBReader;
import org.fastcatsearch.datasource.reader.DefaultDataSourceReader;
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
import org.fastcatsearch.ir.document.Document;
import org.fastcatsearch.ir.field.Field;
import org.fastcatsearch.ir.settings.FieldSetting;
import org.fastcatsearch.ir.settings.SchemaSetting;
import org.fastcatsearch.service.ServiceManager;
import org.fastcatsearch.util.DynamicClassLoader;
import org.fastcatsearch.util.ResponseWriter;

@ActionMapping(value = "/management/collections/test-source-reader", authority = ActionAuthority.Collections, authorityLevel = ActionAuthorityLevel.WRITABLE)
public class TestSourceReaderAction extends AuthAction {

	@Override
	public void doAuthAction(ActionRequest request, ActionResponse response)
			throws Exception {
		String collectionId = request.getParameter("collectionId");
		IRService irService = ServiceManager.getInstance().getService(IRService.class);
		CollectionContext collectionContext = irService.collectionContext(collectionId);
		DataSourceConfig dataSourceConfig = collectionContext.dataSourceConfig();
		List<SingleSourceConfig> singleSourceConfigList = dataSourceConfig.getFullIndexingSourceConfig();
		DefaultDataSourceReader dataReader = null;
		
		Writer writer = response.getWriter();
		ResponseWriter resultWriter = getDefaultResponseWriter(writer);
		resultWriter.object();
		
		int maxRows = 10;
		
		try {
			resultWriter.key("mappingResult").array();
			if(singleSourceConfigList.size() > 0) {
				SingleSourceConfig singleSourceConfig = singleSourceConfigList.get(0);
				Class<?> sourceReaderCls = DynamicClassLoader.loadClass(singleSourceConfig.getSourceReader());
				Constructor<?> constructor = sourceReaderCls.getConstructor(String.class,
						File.class, SingleSourceConfig.class, SourceModifier.class,
						String.class);
				@SuppressWarnings("unchecked")
				SingleSourceReader<Map<String, Object>> sreader = (SingleSourceReader<Map<String, Object>>) constructor
						.newInstance(collectionId, null, singleSourceConfig, null, null);
				sreader.setMaxRows(maxRows);
				
				SchemaSetting workSchemaSetting = collectionContext.workSchemaSetting();
				List<FieldSetting> fieldSettingList = workSchemaSetting.getFieldSettingList();
				dataReader = new DefaultDataSourceReader(workSchemaSetting);
				dataReader.addSourceReader(sreader);
				dataReader.init();
				
				while (dataReader.hasNext()) {
					Document document = dataReader.nextDocument();
					resultWriter.array();
					for (int finx = 0; finx < fieldSettingList.size(); finx++) {
						FieldSetting fieldSetting = fieldSettingList.get(finx);
						Field field = document.get(finx);
						resultWriter.object().key("field").value(fieldSetting.getName())
							.key("value").value(field.getDataString()).endObject();
					}
					resultWriter.endArray();
					logger.trace("document:{}", document);
				}
				dataReader.close();
			}
			
			resultWriter.endArray().endObject();
		} finally {
			if(dataReader!=null) {
				dataReader.close();
			}
		}
		resultWriter.done();
	}
}