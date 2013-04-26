package org.fastcatsearch.servlet;

import java.io.PrintWriter;
import java.io.Writer;
import java.util.List;

import org.fastcatsearch.ir.common.SettingException;
import org.fastcatsearch.ir.config.FieldSetting;
import org.fastcatsearch.ir.config.IRSettings;
import org.fastcatsearch.ir.config.Schema;
import org.fastcatsearch.ir.field.ScoreField;
import org.fastcatsearch.ir.group.GroupEntry;
import org.fastcatsearch.ir.group.GroupResult;
import org.fastcatsearch.ir.group.GroupResults;
import org.fastcatsearch.ir.io.AsciiCharTrie;
import org.fastcatsearch.ir.query.Result;
import org.fastcatsearch.ir.query.Row;
import org.fastcatsearch.ir.util.Formatter;
import org.fastcatsearch.util.ResultStringer;
import org.fastcatsearch.util.StringifyException;

public class SearchResultWriter {
	
	Writer writer;
	String charset;
	
	public SearchResultWriter(PrintWriter writer, String responseCharset) {
	}

	public void writeResult(Result result, ResultStringer rStringer, long searchTime, boolean isSuccess) {
		if(writeHeader(rStringer, searchTime, isSuccess)){
			writeBody(rStringer);
		}
		writeFooter();
		
		writeResult(writer, charset, rStringer);
	}
	
	private void writeFooter() {
		
	}

	public boolean writeHeader(ResultStringer rStringer, long searchTime, boolean isSuccess) {
		
		searchTime = (System.currentTimeMillis() - st);
		if(!jobResult.isSuccess()){
			String errorMsg = null;
			if(obj == null){
				errorMsg = "null";
			}else{
				errorMsg = obj.toString();
			}
			searchLogger.info(seq+", -1, "+errorMsg);
				rStringer.object()
				.key("status").value(1)
				.key("time").value(Formatter.getFormatTime(searchTime))
				.key("total_count").value(0)
				.key("error_msg").value(errorMsg).endObject();
    		return;
		}else{
			
		}
		return false;
	}
	
	public void writeBody(ResultStringer rStringer) {

		try {
		
			searchTime = (System.currentTimeMillis() - st);
			if(!jobResult.isSuccess()){
				String errorMsg = null;
				if(obj == null){
					errorMsg = "null";
				}else{
					errorMsg = obj.toString();
				}
				searchLogger.info(seq+", -1, "+errorMsg);
					rStringer.object()
					.key("status").value(1)
					.key("time").value(Formatter.getFormatTime(searchTime))
					.key("total_count").value(0)
					.key("error_msg").value(errorMsg).endObject();
	    		return;
			}else{
				result = (Result)obj;
				
				String logStr = searchTime+", "+result.getCount()+", "+result.getTotalCount()+", "+result.getFieldCount();
				if(result.getGroupResult() != null){
					String grStr = ", [";
					GroupResults aggregationResult = result.getGroupResult();
					GroupResult[] gr = aggregationResult.groupResultList();
					for (int i = 0; i < gr.length; i++) {
						if(i > 0)
							grStr += ", ";
						grStr += gr[i].size();
					}
					grStr += "]";
					logStr += grStr;
				}
				searchLogger.info(seq+", "+logStr);
				
				int fieldCount = result.getFieldCount();
				String[] fieldNames = null;
				rStringer.object()
				.key("status").value(0)
				.key("time").value(Formatter.getFormatTime(searchTime))
				.key("total_count").value(result.getTotalCount())
				.key("count").value(result.getCount())
				.key("field_count").value(fieldCount)
				.key("fieldname_list").array("name");
				
				if(result.getCount() == 0){
					rStringer.value("_no_");
				}else{
					rStringer.value("_no_");
					fieldNames = result.getFieldNameList();
		    		for (int i = 0; i < fieldNames.length; i++) {
						rStringer.value(fieldNames[i]);
					}
				}
				rStringer.endArray();
		    	
				if(isAdmin != null && isAdmin.equalsIgnoreCase("true")){
					if(result.getCount() == 0){
						rStringer.key("colmodel_list").array("colmodel")
							.object()
							.key("name").value("_no_")
							.key("index").value("_no_")
							.key("width").value("100%")
							.key("sorttype").value("int")
							.key("align").value("center")
							.endObject()
						.endArray();
					}else{
						rStringer.key("colmodel_list").array("colmodel");
						
//		        		AsciiCharTrie fieldNamesTrie = null;
		        		List<FieldSetting> fieldSettingList = null;
		        		try {
//		        			Schema schema = IRSettings.getSchema(collectionName, false);
//		        			fieldNamesTrie = schema.fieldnames;
//		        			fieldSettingList = schema.getFieldSettingList();
						} catch (SettingException e) { }
						
						//write _no_
						if(fieldNames.length > 0){
							rStringer.object()
							.key("name").value("_no_")
							.key("index").value("_no_")
							.key("width").value("20")
							.key("sorttype").value("int")
							.key("align").value("center")
							.endObject();
						}
		        		for (int i = 0; i < fieldNames.length; i++) {
//		        			int idx = fieldNamesTrie.get(fieldNames[i]);
//		        			if(idx < 0){
//		        				if(fieldNames[i].equalsIgnoreCase(ScoreField.fieldName)){
//		        					rStringer.object()
//		        					.key("name").value(fieldNames[i])
//		        					.key("index").value(fieldNames[i])
//		        					.key("width").value("20")
//		        					.key("sorttype").value("int")
//		        					.key("align").value("right")
//		        					.endObject();
//		        				}else{
//		        					rStringer.object()
//		        					.key("name").value(fieldNames[i])
//		        					.key("index").value(fieldNames[i])
//		        					.key("width").value("20")
//		        					.endObject();
//		        				}
//		        			}else{
		        				
//		        				int colWidth = 20;
//		        				String sortType="int";
//		        				String align="right";
//		            			FieldSetting fs = fieldSettingList.get(idx);
//		            			if(fs.type == FieldSetting.Type.Int || fs.type == FieldSetting.Type.Long || 
//	            					fs.type == FieldSetting.Type.Float || fs.type == FieldSetting.Type.Double || 
//	            					fs.type == FieldSetting.Type.DateTime) {
//		            			} else {
//		            				if(fs.size > 0 && fs.size < 50) {
//		            					colWidth=fs.size*2;
//		            				} else {
//		            					colWidth=100;
//		            				}
//		            				sortType="";
//		            				align="";
//		            			}
		        				
		        				rStringer.object()
		        				.key("name").value(fieldNames[i])
//		        				.key("index").value(fieldNames[i])
//		        				.key("width").value(colWidth)
//		            			.key("sorttype").value(sortType)
//		            			.key("align").value(align)
		            			.endObject();
//		        			}
						}
		        		rStringer.endArray();
					}
				}
				
				rStringer.key("result");
				//data
				Row[] rows = result.getData();
				int start = result.getMetadata().start();
				
				if(rows.length == 0){
					rStringer.array("row").object()
						.key("_no_").value("No result found!")
						.endObject().endArray();
				}else{
					rStringer.array("row");
		    		for (int i = 0; i < rows.length; i++) {
						Row row = rows[i];
						
		    			rStringer.object()
		    				.key("_no_").value(start+i);
		    			
						for(int k = 0; k < fieldCount; k++) {
							char[] f = row.get(k);
							String fdata = new String(f).trim();
							//For Json validity
							//1. replace single back-slash quote with double back-slash
							//2. replace double quote with character '\"' 
							//4. replace linefeed with character '\n' 
							rStringer.key(fieldNames[k]).value(fdata);
						}
						rStringer.endObject();
		    		}
		    		rStringer.endArray();
		    		
//		    		
		    		
		    		
		    		
		    		
		    		new GroupResultWriter().writeFormat(rStringer);
				}
				rStringer.endObject();
			}
			
		} catch (StringifyException e) {
			logger.error("",e);
		} finally {
		}		
	}

}
