/*
 * Copyright (c) 2013 Websquared, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 * 
 * Contributors:
 *     swsong - initial API and implementation
 */

package org.fastcatsearch.job;

import org.fastcatsearch.exception.FastcatSearchException;
import org.fastcatsearch.plugin.LicenseInvalidException;
import org.fastcatsearch.plugin.PluginService;
import org.fastcatsearch.plugin.analysis.AnalysisPlugin;
import org.fastcatsearch.service.ServiceManager;

import java.io.IOException;
import java.util.Map;

public class DictionaryCompileApplyJob extends MasterNodeJob {

	private static final long serialVersionUID = 8615645248824825498L;

	@Override
	public JobResult doRun() throws FastcatSearchException {
		Map<String, Object> args = getMapArgs();
		String pluginId = (String) args.get("pluginId");
		String dictListStr = (String) args.get("dictionary");
		String[] dictList = null;
		if (dictListStr != null && dictListStr.length() > 0) {
			dictList = dictListStr.split(",");
		}else{
			return new JobResult("dictionary가 전달되지 않았습니다. >> "+dictListStr);
		}

		PluginService pluginService = ServiceManager.getInstance().getService(PluginService.class);
		AnalysisPlugin analysisPlugin = (AnalysisPlugin) pluginService.getPlugin(pluginId);
		if(analysisPlugin == null){
			logger.error("Plugin을 찾을수 없습니다. >> {}", pluginId);
			return new JobResult("Plugin을 찾을수 없습니다. >> "+pluginId);
		}
		
		try {
			for (String dictionaryId : dictList) {
				analysisPlugin.compileDictionaryFromDAO(dictionaryId);
			}
		} catch (IOException e) {
			throw new FastcatSearchException(e);
		}

		logger.debug("사전컴파일후 플러그인 {}를 재로딩합니다.", pluginId);


        try {
            analysisPlugin.reload(environment.isMasterNode());
        } catch (LicenseInvalidException e) {
            throw new FastcatSearchException(e.getMessage());
        }

        return new JobResult(true);
	}

//	private void compileSetDictionary(List<SetDictionaryVO> result, String filePath) throws Exception {
//		org.fastcatsearch.ir.dictionary.HashSetDictionary dictionary = new org.fastcatsearch.ir.dictionary.HashSetDictionary();
//		for (int i = 0; i < result.size(); i++) {
//			SetDictionaryVO vo = result.get(i);
//			dictionary.addEntry(vo.keyword);
//		}
//		OutputStream out = null;
//		try {
//			out = new FileOutputStream(filePath);
//			dictionary.writeTo(out);
//		} finally {
//			if (out != null) {
//				try {
//					out.close();
//				} catch (IOException ignore) {
//				}
//			}
//		}
//
//	}

}
