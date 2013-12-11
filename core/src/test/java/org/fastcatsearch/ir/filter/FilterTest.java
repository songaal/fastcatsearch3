package org.fastcatsearch.ir.filter;

import static org.junit.Assert.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.apache.lucene.store.RAMFile;
import org.apache.lucene.store.RAMInputStream;
import org.apache.lucene.store.RAMOutputStream;
import org.fastcatsearch.ir.common.IRException;
import org.fastcatsearch.ir.io.BufferedFileInput;
import org.fastcatsearch.ir.io.DataRef;
import org.fastcatsearch.ir.io.IndexInput;
import org.fastcatsearch.ir.io.IndexOutput;
import org.fastcatsearch.ir.io.StreamInputRef;
import org.fastcatsearch.ir.query.Filter;
import org.fastcatsearch.ir.query.RankInfo;
import org.fastcatsearch.ir.search.FieldIndexReader;
import org.fastcatsearch.ir.search.FieldIndexesReader;
import org.fastcatsearch.ir.search.IndexRef;
import org.fastcatsearch.ir.settings.FieldIndexSetting;
import org.fastcatsearch.ir.settings.FieldSetting;
import org.fastcatsearch.ir.settings.FieldSetting.Type;
import org.fastcatsearch.ir.settings.Schema;
import org.fastcatsearch.ir.settings.SchemaSetting;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FilterTest {
	
	private static final Logger logger = LoggerFactory.getLogger(FilterTest.class);
	
	public void testReader() throws IOException {
		
		File file = new File("/home/lupfeliz/Documents/joara/collections/book/data/index0/0");
		IndexInput dataInput = new BufferedFileInput(new File(file,"field.redate.index"));
		
		
	}

	@Test
	public void testMatchFilter() throws IOException, IRException, JAXBException {
		
		String fieldId = "REDATE";
		
		InputStream is = new FileInputStream("/home/lupfeliz/Documents/joara/collections/book/schema.xml");
		SchemaSetting setting = readConfig(is, SchemaSetting.class);
		is.close();
		
		Schema schema = new Schema(setting);
		
		File file = new File("/home/lupfeliz/Documents/joara/collections/book/data/index0/0");
		
		FieldIndexesReader readers = new FieldIndexesReader(schema, file);
		
		String[] fieldList = new String[]{fieldId};
		
		IndexRef<FieldIndexReader> fieldIndexRef= readers.selectIndexRef(fieldList);
		
		List<DataRef> dataRefList = fieldIndexRef.getDataRefList();
		
		FieldIndexReader reader = fieldIndexRef.getReader(0);
		
		Filter filter = new Filter(fieldId, Filter.MATCH, "20010323232736000","",0);
		
		List<FieldSetting>fieldSettingList = setting.getFieldSettingList();		
		FieldSetting fieldSetting = null;
		for(int inx=0;inx<fieldSettingList.size();inx++) {
			if(fieldId.equalsIgnoreCase(fieldSettingList.get(inx).getId())) {
				logger.debug("found fieldSetting..");
				fieldSetting = fieldSettingList.get(inx);
			}
		}
		
		List<FieldIndexSetting>fieldIndexSettingList = setting.getFieldIndexSettingList();
		FieldIndexSetting fieldIndexSetting = null;
		for(int inx=0;inx<fieldIndexSettingList.size();inx++) {
			if(fieldId.equalsIgnoreCase(fieldIndexSettingList.get(inx).getId())) {
				logger.debug("found fieldIndexSetting..");
				fieldIndexSetting = fieldIndexSettingList.get(inx);
			}
		}
		
		FilterFunction filterFunction = filter.createFilterFunction(fieldIndexSetting, fieldSetting);
		
		RankInfo rankInfo = new RankInfo();
		DataRef dataRef = dataRefList.get(0);
		
		for(int inx=0;inx<10;inx++) {
		
			reader.read(inx);
			boolean bool = filterFunction.filtering(rankInfo, dataRef);
			logger.debug("result:{}", bool);
		}
		
//		RAMFile f = new RAMFile();
//		IndexOutput outptut = new RAMOutputStream(f);
//		IndexInput input = new RAMInputStream("test",f);
//		DataRef dataRef = new StreamInputRef(input,4);
//		RankInfo rankInfo = new RankInfo();
//		FieldIndexSetting fieldIndexSetting = new FieldIndexSetting("test", "test", "test", 4, false);
//		FieldSetting fieldSetting = new FieldSetting("test", "test", Type.INT);
//		FilterFunction filterFunction = filter.createFilterFunction(fieldIndexSetting, fieldSetting);
//		filterFunction.filtering(rankInfo, dataRef);
		//fail("Not yet implemented");
	}
	
	public static <T> T readConfig(InputStream is, Class<T> jaxbConfigClass) throws JAXBException {
		if(is == null){
			return null;
		}
		JAXBContext context = JAXBContext.newInstance(jaxbConfigClass);
		
		Unmarshaller unmarshaller = context.createUnmarshaller();
		
		T config = (T) unmarshaller.unmarshal(is);
		return config;
	}

}
