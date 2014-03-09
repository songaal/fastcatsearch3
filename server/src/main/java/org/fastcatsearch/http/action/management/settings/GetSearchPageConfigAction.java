package org.fastcatsearch.http.action.management.settings;

import java.io.Writer;

import org.fastcatsearch.http.ActionAuthority;
import org.fastcatsearch.http.ActionAuthorityLevel;
import org.fastcatsearch.http.ActionMapping;
import org.fastcatsearch.http.action.ActionRequest;
import org.fastcatsearch.http.action.ActionResponse;
import org.fastcatsearch.http.action.AuthAction;
import org.fastcatsearch.ir.IRService;
import org.fastcatsearch.service.ServiceManager;
import org.fastcatsearch.settings.SearchPageSettings;
import org.fastcatsearch.util.JAXBConfigs;

@ActionMapping (value="/settings/search-config", authority=ActionAuthority.Settings, authorityLevel=ActionAuthorityLevel.READABLE)
public class GetSearchPageConfigAction extends AuthAction {

	@Override
	public void doAuthAction(ActionRequest request, ActionResponse response)
			throws Exception {
		
		IRService irService = ServiceManager.getInstance().getService(IRService.class);
		
		Writer writer = response.getWriter();
		JAXBConfigs.writeRawConfig(writer, irService.getSearchPageSettings(), SearchPageSettings.class);
		
		writer.close();
	}
}
