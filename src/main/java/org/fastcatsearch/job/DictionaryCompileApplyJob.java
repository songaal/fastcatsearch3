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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;

import org.fastcatsearch.control.JobException;
import org.fastcatsearch.db.DBService;
import org.fastcatsearch.db.dao.SetDictionary;
import org.fastcatsearch.db.vo.SetDictionaryVO;
import org.fastcatsearch.ir.dictionary.ListMapDictionary;
import org.fastcatsearch.plugin.Plugin;
import org.fastcatsearch.plugin.PluginService;
import org.fastcatsearch.service.ServiceException;

public class DictionaryCompileApplyJob extends Job {

	@Override
	public JobResult doRun() throws JobException, ServiceException {
		String[] args = getStringArrayArgs();
		String category = args[0];
		String dicType = args[1];

		String synonymDictionaryId = category + "SynonymDictionary";
		String userDictionaryId = category + "UserDictionary";
		String stopDictionaryId = category + "StopDictionary";

		DBService dbService = DBService.getInstance();
		SetDictionary synonymDictionary = dbService.getDAO(synonymDictionaryId);
		SetDictionary userDictionary = dbService.getDAO(userDictionaryId);
		SetDictionary stopDictionary = dbService.getDAO(stopDictionaryId);

		Plugin plugin = PluginService.getInstance().getPlugin(category);
		File pluginDir = plugin.getPluginDir();
		Map<String, String> properties= plugin.getPluginSetting().getProperties();
		String synonymDictPath = new File(pluginDir, properties.get("synonym.dict.path")).getAbsolutePath();
		String userDictPath = new File(pluginDir, properties.get("user.dict.path")).getAbsolutePath();
		String stopDictPath = new File(pluginDir, properties.get("stop.dict.path")).getAbsolutePath();

		logger.debug("compile dict {}, {}, {}", synonymDictPath, userDictPath, stopDictPath);
		//
		// 1. synonymDictionary
		//
		if (dicType == null || dicType.equals("synonymDict")) {
			List<SetDictionaryVO> result = synonymDictionary.selectPage(-1, -1);
			ListMapDictionary dictionary = new ListMapDictionary();
			for (int i = 0; i < result.size(); i++) {
				SetDictionaryVO vo = result.get(i);
				dictionary.addEntry(vo.keyword);
			}
			OutputStream out = null;
			try {
				out = new FileOutputStream(synonymDictPath);
				dictionary.writeTo(out);
			} catch (Exception e) {
				return new JobResult(e);
			} finally {
				if (out != null) {
					try {
						out.close();
					} catch (IOException ignore) {
					}
				}
			}
			logger.debug("Dictionary write to {}", synonymDictPath);
		}
		//
		// 2. userDictionary
		//
		if (dicType == null || dicType.equals("userDict")) {
			List<SetDictionaryVO> result = userDictionary.selectPage(-1, -1);
			try {
				compileSetDictionary(result, userDictPath);
			} catch (Exception e) {
				return new JobResult(e);
			}
			logger.debug("Dictionary write to {}", userDictPath);
		}

		//
		// 3. stopDictionary
		//
		if (dicType == null || dicType.equals("stopDict")) {
			List<SetDictionaryVO> result = stopDictionary.selectPage(-1, -1);
			try {
				compileSetDictionary(result, stopDictPath);
			} catch (Exception e) {
				return new JobResult(e);
			}
			logger.debug("Dictionary write to {}", stopDictPath);
		}

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
