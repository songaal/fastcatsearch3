package org.fastcatsearch.http.action.management.collections;

import java.io.File;
import java.io.IOException;
import java.io.Writer;

import org.apache.commons.io.FileUtils;
import org.fastcatsearch.http.ActionAuthority;
import org.fastcatsearch.http.ActionAuthorityLevel;
import org.fastcatsearch.http.ActionMapping;
import org.fastcatsearch.http.action.ActionRequest;
import org.fastcatsearch.http.action.ActionResponse;
import org.fastcatsearch.http.action.AuthAction;
import org.fastcatsearch.util.ResponseWriter;

@ActionMapping(value = "/management/collections/create-temp-collection", authority = ActionAuthority.Collections, authorityLevel = ActionAuthorityLevel.WRITABLE)
public class CreateTemporaryCollectionAction extends AuthAction {
	
	private static final String TMP_PREFIX = "tmpCollection";
	private static final String TMP_SUFFIX = ".tmp";

	@Override
	public void doAuthAction(ActionRequest request, ActionResponse response)
			throws Exception {
		
		boolean success = false;
		String ticket = "";
		File file = null;
		try {
			//한 세션내 컬렉션을 동시에 여러개 만들수도 있으므로, 최초에 티켓을 만들고 시작한다.
			file = File.createTempFile(TMP_PREFIX, TMP_SUFFIX);
			FileUtils.forceDelete(file);
			file.mkdir();
			String[] fileNameArray = file.getAbsolutePath().split("/");
			String fileName = fileNameArray[fileNameArray.length - 1];
			//temp 파일을 이용해 티켓을 만든다.
			ticket = fileName.split("[.]")[0].substring(TMP_PREFIX.length());
			success = true;
		} catch (IOException e) {
			if(file!=null && file.exists()) {
				FileUtils.forceDelete(file);
			}
		} finally {
			
		}
		
		Writer writer = response.getWriter();
		ResponseWriter responseWriter = getDefaultResponseWriter(writer);
		responseWriter.object().key("success").value(success)
			.key("ticket").value(ticket).done();
	}
}
