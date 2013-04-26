package org.fastcatsearch.servlet;

import java.io.PrintWriter;
import java.io.Writer;

import org.fastcatsearch.ir.group.GroupResults;
import org.fastcatsearch.ir.query.Result;
import org.fastcatsearch.ir.query.Row;
import org.fastcatsearch.ir.util.Formatter;
import org.fastcatsearch.util.ResultStringer;
import org.fastcatsearch.util.StringifyException;

public class SearchResultWriter {
	
	Writer writer;
	boolean isAdmin;
	String[] fieldNames = null;
	
	public SearchResultWriter(Writer writer, boolean isAdmin) {
		this.writer = writer;
		this.isAdmin = isAdmin;
	}

	public void writeResult(Object obj, ResultStringer rStringer, long searchTime, boolean isSuccess) throws StringifyException {
		if(!isSuccess){
			String errorMsg = null;
			if(obj == null){
				errorMsg = "null";
			}else{
				errorMsg = obj.toString();
			}
			rStringer.object()
				.key("status").value(1)
				.key("time").value(Formatter.getFormatTime(searchTime))
				.key("total_count").value(0)
				.key("error_msg").value(errorMsg).endObject();
			return;
		}else{
			Result result = (Result)obj;
			
			fieldNames = result.getFieldNameList();
			rStringer.object()
			.key("status").value(0)
			.key("time").value(Formatter.getFormatTime(searchTime))
			.key("total_count").value(result.getTotalCount())
			.key("count").value(result.getCount())
			.key("field_count").value(fieldNames.length)
			.key("fieldname_list")
			.array("name");

			if(result.getCount() == 0){
				rStringer.value("_no_");
			}else{
				rStringer.value("_no_");
				for (int i = 0; i < fieldNames.length; i++) {
					rStringer.value(fieldNames[i]);
				}
			}
			rStringer.endArray();
			writeBody(result,rStringer, searchTime);
			rStringer.endObject();
		}
	}
	
	public void writeBody(Result result, ResultStringer rStringer, long searchTime) throws StringifyException {
		if(isAdmin){
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
					rStringer.object()
					.key("name").value(fieldNames[i])
					.endObject();
				}
				rStringer.endArray();
			}
		}

		rStringer.key("result");
		//data
		Row[] rows = result.getData();
		int start = result.getMetadata().start();

		if(rows.length == 0){
			rStringer.array("item").object()
			.key("_no_").value("No result found!")
			.endObject().endArray();
		}else{
			rStringer.array("item");
			for (int i = 0; i < rows.length; i++) {
				Row row = rows[i];

				rStringer.object()
				.key("_no_").value(start+i);

				for(int k = 0; k < fieldNames.length; k++) {
					char[] f = row.get(k);
					String fdata = new String(f).trim();
					rStringer.key(fieldNames[k]).value(fdata);
				}
				rStringer.endObject();
			}
			rStringer.endArray();
			
			GroupResults groupResult = result.getGroupResult();
			
			new GroupResultWriter(writer).writeBody(groupResult, rStringer);
		}
	}
}
