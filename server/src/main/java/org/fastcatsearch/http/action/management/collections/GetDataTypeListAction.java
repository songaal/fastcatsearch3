package org.fastcatsearch.http.action.management.collections;

import java.io.Writer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.fastcatsearch.http.ActionAuthority;
import org.fastcatsearch.http.ActionAuthorityLevel;
import org.fastcatsearch.http.ActionMapping;
import org.fastcatsearch.http.action.ActionRequest;
import org.fastcatsearch.http.action.ActionResponse;
import org.fastcatsearch.http.action.AuthAction;
import org.fastcatsearch.util.ResponseWriter;

@ActionMapping(value = "/management/collections/data-type-list", authority = ActionAuthority.Collections, authorityLevel = ActionAuthorityLevel.READABLE)
public class GetDataTypeListAction extends AuthAction {

	@Override
	public void doAuthAction(ActionRequest request, ActionResponse response)
			throws Exception {
		
		org.fastcatsearch.ir.settings.FieldSetting.Type[] types = 
				org.fastcatsearch.ir.settings.FieldSetting.Type.values();
		
		
		List<String> typeList = new ArrayList<String>();
		for(org.fastcatsearch.ir.settings.FieldSetting.Type type : types){
			if(type == org.fastcatsearch.ir.settings.FieldSetting.Type._DOCNO
				|| type == org.fastcatsearch.ir.settings.FieldSetting.Type._HIT
				|| type == org.fastcatsearch.ir.settings.FieldSetting.Type._SCORE
				|| type == org.fastcatsearch.ir.settings.FieldSetting.Type.UNKNOWN) {
				continue;
			}
			typeList.add(type.toString());
		}
		
		Collections.sort(typeList);
		
		Writer writer = response.getWriter();
		ResponseWriter responseWriter = getDefaultResponseWriter(writer);
		responseWriter.object().key("typeList").array("types");
		for(String typeStr : typeList) {
			responseWriter.value(typeStr);
		}
		responseWriter.endArray().endObject();
		responseWriter.done();
		
	}
}