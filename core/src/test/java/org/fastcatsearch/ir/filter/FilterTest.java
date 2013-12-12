package org.fastcatsearch.ir.filter;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.fastcatsearch.ir.common.IRException;
import org.fastcatsearch.ir.document.Document;
import org.fastcatsearch.ir.field.Field;
import org.fastcatsearch.ir.index.FieldIndexWriter;
import org.fastcatsearch.ir.io.CharVector;
import org.fastcatsearch.ir.io.DataRef;
import org.fastcatsearch.ir.query.Filter;
import org.fastcatsearch.ir.query.RankInfo;
import org.fastcatsearch.ir.search.FieldIndexReader;
import org.fastcatsearch.ir.settings.FieldIndexSetting;
import org.fastcatsearch.ir.settings.FieldSetting;
import org.fastcatsearch.ir.settings.FieldSetting.Type;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FilterTest {
	
	private static final Logger logger = LoggerFactory.getLogger(FilterTest.class);
	
	public void testSingleReadWriteAndFilterOnTheFly(String fieldId, 
			int filterType, Type fieldType, int fieldSize,
			List<Object> fieldData, String pattern, String endPattern, 
			int boostScore, List<Boolean> filterResult, List<Integer> boostResult) 
					throws IOException, IRException {
		
		RankInfo rankInfo = new RankInfo();
		
		File dir = File.createTempFile("dir", ".tmp");
		dir.delete();
		dir.mkdir();
		FieldSetting fieldSetting = new FieldSetting(fieldId,"",fieldType);
		
		if(fieldSize>0) {
			fieldSetting.setSize(fieldSize);
		}
		
		FieldIndexSetting fieldIndexSetting = new FieldIndexSetting(fieldId, 
				fieldId, fieldId, fieldSetting.getSize(), false);
		Map<String, FieldSetting> fieldSettingMap = 
				new HashMap<String, FieldSetting>();
		Map<String, Integer> fieldSequenceMap = 
				new HashMap<String, Integer>();
		fieldSettingMap.put(fieldId.toUpperCase(), fieldSetting);
		fieldSequenceMap.put(fieldId.toUpperCase(), 0);
		
		FieldIndexWriter writer = new FieldIndexWriter(fieldIndexSetting,
				fieldSettingMap,fieldSequenceMap, dir);
		
		for(int docNo=0; docNo<fieldData.size(); docNo++) {
			Field field = fieldSetting.createEmptyField();
			field.setFieldsData(fieldData.get(docNo));
			Document document = new Document(docNo);
			document.add(field);
			writer.write(document);
		}
		writer.close();
		
		Filter filter = new Filter(fieldId, Filter.MATCH, pattern,endPattern,boostScore);
		FilterFunction func = filter.createFilterFunction(fieldIndexSetting, fieldSetting);
		
		FieldIndexReader reader = new FieldIndexReader(fieldIndexSetting,
				fieldSettingMap, dir );
		DataRef dataRef = reader.getRef();
		
		for(int docNo=0;docNo<fieldData.size();docNo++) {
			reader.read(docNo);
			filterResult.add(func.filtering(rankInfo, dataRef));
			boostResult.add(rankInfo.hit());
		}
		reader.close();
		FileUtils.forceDelete(dir);
	}
	
	@Test
	public void testMatchFilterOnTheFly() throws IOException, IRException {
		
		String fieldId = "CATEGORY";
		int filterType = Filter.MATCH;
		Type fieldType = Type.ASTRING;
		List<Object> fieldData = null;
		String pattern = "01001";
		String endPattern = "";
		int boostScore = 0;
		List<Boolean> filterResult = new ArrayList<Boolean>();
		List<Integer> boostResult = new ArrayList<Integer>();
		
		fieldData = Arrays.asList(new Object[] {
			"00000", "00001", "01001", "00004"
		});
		
		for(int inx=0;inx<fieldData.size();inx++) {
			fieldData.set(inx, 
					new CharVector((String)fieldData.get(inx)));
		}
		
		testSingleReadWriteAndFilterOnTheFly(fieldId, filterType, 
			fieldType, 5, fieldData, pattern, 
			endPattern, boostScore, filterResult,
			boostResult);
		
		logger.debug("filter result : {}", filterResult);
		
		List<Boolean> actual = Arrays.asList(new Boolean[] {
				false,false,true,false
		});
		
		org.junit.Assert.assertEquals(filterResult, actual);
	}

	@Test
	public void testSectionFilterOnTheFly() throws IOException, IRException {
		
		String fieldId = "CNT_READ";
		int filterType = Filter.SECTION;
		Type fieldType = Type.INT;
		List<Object> fieldData = null;
		String pattern = "128";
		String endPattern = "256";
		int boostScore = 0;
		List<Boolean> filterResult = new ArrayList<Boolean>();
		List<Integer> boostResult = new ArrayList<Integer>();
		
		fieldData = Arrays.asList(new Object[] {
			1, 999, 130, 1024 
		});
		
		testSingleReadWriteAndFilterOnTheFly(fieldId, filterType, 
			fieldType, 5, fieldData, pattern, 
			endPattern, boostScore, filterResult,
			boostResult);
		
		logger.debug("filter result : {}", filterResult);
		
		List<Boolean> actual = Arrays.asList(new Boolean[] {
				false,false,true,false
		});
		
		org.junit.Assert.assertEquals(filterResult, actual);
	}
}
