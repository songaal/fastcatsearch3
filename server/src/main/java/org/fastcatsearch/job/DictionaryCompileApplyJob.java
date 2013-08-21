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

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

import org.fastcatsearch.db.dao.DAOBase;
import org.fastcatsearch.db.vo.SetDictionaryVO;
import org.fastcatsearch.exception.FastcatSearchException;
import org.fastcatsearch.plugin.AnalysisPlugin;
import org.fastcatsearch.plugin.AnalysisPluginSetting;
import org.fastcatsearch.plugin.PluginService;
import org.fastcatsearch.service.ServiceManager;

public class DictionaryCompileApplyJob extends Job {

	private static final long serialVersionUID = 8615645248824825498L;

	@Override
	public JobResult doRun() throws FastcatSearchException {
		String[] args = getStringArrayArgs();
		String category = args[0];
		String dictListStr = args[1];
		String[] dictList = null;
		if (dictListStr != null && dictListStr.length() > 0) {
			dictList = dictListStr.split(",");
		}

		PluginService pluginService = ServiceManager.getInstance().getService(PluginService.class);
		AnalysisPlugin analysisPlugin = (AnalysisPlugin) pluginService.getPlugin(category);
		AnalysisPluginSetting setting = (AnalysisPluginSetting) analysisPlugin.getPluginSetting();

		try {
			for (String dictionaryId : dictList) {
				String tableName = setting.getKey(dictionaryId);
				DAOBase daoBase = pluginService.db().getDAO(tableName);
				analysisPlugin.compileDictionaryFromDAO(dictionaryId, daoBase);
			}
		} catch (IOException e) {
			throw new FastcatSearchException("", e);
		}

		logger.debug("사전컴파일후 플러그인 {}를 재로딩합니다.", category);
		analysisPlugin.reload();

		return new JobResult(0);
	}

	private void compileSetDictionary(List<SetDictionaryVO> result, String filePath) throws Exception {
		org.fastcatsearch.ir.dictionary.HashSetDictionary dictionary = new org.fastcatsearch.ir.dictionary.HashSetDictionary();
		for (int i = 0; i < result.size(); i++) {
			SetDictionaryVO vo = result.get(i);
			dictionary.addEntry(vo.keyword);
		}
		OutputStream out = null;
		try {
			out = new FileOutputStream(filePath);
			dictionary.writeTo(out);
		} finally {
			if (out != null) {
				try {
					out.close();
				} catch (IOException ignore) {
				}
			}
		}

	}

}
