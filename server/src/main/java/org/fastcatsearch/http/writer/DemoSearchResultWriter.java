package org.fastcatsearch.http.writer;

import java.io.IOException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.fastcatsearch.ir.field.ScoreField;
import org.fastcatsearch.ir.query.Result;
import org.fastcatsearch.ir.query.Row;
import org.fastcatsearch.ir.util.Formatter;
import org.fastcatsearch.settings.SearchPageSettings.SearchCategorySetting;
import org.fastcatsearch.util.ResponseWriter;
import org.fastcatsearch.util.ResultWriterException;

public class DemoSearchResultWriter extends AbstractSearchResultWriter {

	private SearchCategorySetting setting;
	
	public DemoSearchResultWriter(ResponseWriter resultStringer, SearchCategorySetting setting) {
		super(resultStringer);
		this.setting = setting;
	}
	
	@Override
	public void writeResult(Object obj, long searchTime, boolean isSuccess) throws ResultWriterException, IOException {
		if(!isSuccess){
			String errorMsg = null;
			if(obj == null){
				errorMsg = "null";
			}else{
				errorMsg = obj.toString();
			}
			resultWriter.object()
				.key("status").value(1)
				.key("time").value(Formatter.getFormatTime(searchTime))
				.key("total_count").value(0)
				.key("error_msg").value(errorMsg).endObject();
		}else{
			Result result = (Result)obj;
			
//			fieldNames = result.getFieldNameList();
			resultWriter.object()
			.key("status").value(0)
			.key("time").value(Formatter.getFormatTime(searchTime))
			.key("start").value(result.getStart())
			.key("total_count").value(result.getTotalCount())
			.key("count").value(result.getCount());
//			.key("field_count").value(fieldNames.length)
//			.key("fieldname_list")
//			.array("name");
//
//			for (int i = 0; i < fieldNames.length; i++) {
//				resultWriter.value(fieldNames[i]);
//			}
//			resultWriter.endArray();
			writeBody(result, resultWriter, searchTime);
			resultWriter.endObject();
			
			resultWriter.done();
		}
		
	}
	
	Pattern patt = Pattern.compile("\\$[a-zA-Z_-]+");
	private Set<String> findIdList(String source) {
		Set<String> set = new HashSet<String>();
		Matcher matcher = patt.matcher(source);
		while(matcher.find()){
			String g = matcher.group();
			set.add(g.substring(1));
		}
		return set;
	}
	
	public void writeBody(Result result, ResponseWriter resultWriter, long searchTime) throws ResultWriterException {
		String[] fieldNames = result.getFieldNameList();
		
		String thumbnailField = setting.getThumbnailField();
		String titleField = setting.getTitleField();
		String bodyField = setting.getBodyField();
		
		Set<String> thumbnailIdSet = findIdList(thumbnailField);
		Set<String> titleIdSet = findIdList(titleField);
		Set<String> bodyIdSet = findIdList(bodyField);
		
		resultWriter.key("result");
		//data
		Row[] rows = result.getData();

		if(rows.length == 0){
			resultWriter.array("item").endArray();
		}else{
			resultWriter.array("item");
			for (int i = 0; i < rows.length; i++) {
				Row row = rows[i];
				
				resultWriter.object();

				/*
				 * 1. title
				 * */
				resultWriter.key("thumbnail");
				Iterator<String> iter = thumbnailIdSet.iterator();
				String thumbnailData = thumbnailField;
				while(iter.hasNext()) {
					String fieldId = iter.next();
					String fieldData = getFieldData(fieldId, row, fieldNames);
					thumbnailData = thumbnailData.replaceAll("\\$"+fieldId, fieldData);
				}
				resultWriter.value(thumbnailData);
				
				/*
				 * 2. title
				 * */
				resultWriter.key("title");
				iter = titleIdSet.iterator();
				String titleData = titleField;
				while(iter.hasNext()) {
					String fieldId = iter.next();
					String fieldData = getFieldData(fieldId, row, fieldNames);
					titleData = titleData.replaceAll("\\$"+fieldId, fieldData);
				}
				resultWriter.value(titleData);
				
				/*
				 * 3. body
				 * */
				String bodyData = bodyField;
				resultWriter.key("body");
				iter = bodyIdSet.iterator();
				while(iter.hasNext()) {
					String fieldId = iter.next();
					String fieldData = getFieldData(fieldId, row, fieldNames);
					bodyData = bodyData.replaceAll("\\$"+fieldId, fieldData);
				}
				resultWriter.value(bodyData);
				resultWriter.endObject();
			}
			resultWriter.endArray();
			
//			GroupResults groupResult = result.getGroupResult();
//			
//			new GroupResultWriter(null).writeBody(groupResult, resultWriter);
			
		}
		
	}

	private String getFieldData(String fieldId, Row row, String[] fieldNames) {
		if(fieldId.equalsIgnoreCase(ScoreField.fieldName)){
			return String.valueOf(row.getScore());
		}
		
		for(int k = 0; k < fieldNames.length; k++) {
			if(fieldId.equalsIgnoreCase(fieldNames[k])){
				return new String(row.get(k)).trim();
			}
			
		}
		return "";
	}

}
