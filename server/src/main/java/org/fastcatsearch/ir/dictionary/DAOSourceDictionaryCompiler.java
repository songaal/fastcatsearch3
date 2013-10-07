package org.fastcatsearch.ir.dictionary;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;

import org.fastcatsearch.db.dao.AbstractDictionaryDAO;

public class DAOSourceDictionaryCompiler {
	
	public static void compile(File targetFile, AbstractDictionaryDAO dictionaryDAO, SourceDictionary dictionaryType) throws Exception{
		
		List<Map<String,Object>> result = dictionaryDAO.getEntryList(-1, -1, null, false);
		for (int i = 0; i < result.size(); i++) {
			Map<String,Object> vo = result.get(i);
			dictionaryType.addMapEntry(vo);
		}
		OutputStream out = null;
		try {
			out = new FileOutputStream(targetFile);
			dictionaryType.writeTo(out);
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
