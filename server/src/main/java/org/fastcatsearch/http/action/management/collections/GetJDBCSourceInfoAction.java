package org.fastcatsearch.http.action.management.collections;

import java.io.OutputStream;

import org.fastcatsearch.http.ActionAuthority;
import org.fastcatsearch.http.ActionAuthorityLevel;
import org.fastcatsearch.http.ActionMapping;
import org.fastcatsearch.http.action.ActionRequest;
import org.fastcatsearch.http.action.ActionResponse;
import org.fastcatsearch.http.action.AuthAction;
import org.fastcatsearch.ir.IRService;
import org.fastcatsearch.ir.config.JDBCSourceConfig;
import org.fastcatsearch.service.ServiceManager;
import org.fastcatsearch.util.JAXBConfigs;

@ActionMapping(value = "/management/collections/jdbc-source", authority = ActionAuthority.Collections, authorityLevel = ActionAuthorityLevel.READABLE)
public class GetJDBCSourceInfoAction extends AuthAction {

	@Override
	public void doAuthAction(ActionRequest request, ActionResponse response)
			throws Exception {

		IRService irService = ServiceManager.getInstance().getService(IRService.class);
		
		JDBCSourceConfig jdbcSourceConfig = irService.getJDBCSourceConfig();
		
		OutputStream os = response.getOutputStream();
		JAXBConfigs.writeRawConfig(os, jdbcSourceConfig, JDBCSourceConfig.class);
		
	}
}
