package org.fastcatsearch.servlet;

import java.io.IOException;
import java.io.Writer;

import org.fastcatsearch.common.Strings;
import org.fastcatsearch.ir.group.GroupEntry;
import org.fastcatsearch.ir.group.GroupResult;
import org.fastcatsearch.ir.group.GroupResults;
import org.fastcatsearch.util.ResultStringer;
import org.fastcatsearch.util.StringifyException;

public class GroupResultWriter extends AbstractSearchResultWriter {
	
	public GroupResultWriter(Writer writer) {
		super(writer);
	}
	
	@Override
	public void writeResult(Object result, ResultStringer stringer, long searchTime, boolean isSuccess) throws StringifyException, IOException{
		
		stringer.object();
		
		if(!isSuccess){
			String errorMsg = null;
			if(result == null){
				errorMsg = "null";
			}else{
				errorMsg = result.toString();
			}
			stringer.key("status").value(1)
			.key("time").value(Strings.getHumanReadableTimeInterval(searchTime))
			.key("total_count").value(0)
			.key("count").value(0)
			.key("error_msg").value(errorMsg);
			
		}else{
			
			GroupResults groupResults = (GroupResults) result;
			stringer.key("status").value(0)
			.key("time").value(Strings.getHumanReadableTimeInterval(searchTime))
			.key("total_count").value(groupResults.totalSearchCount())
			.key("count").value(groupResults.totalSearchCount());
			
			writeBody(groupResults, stringer);
		}
		
		stringer.endObject();
			
		writer.write(stringer.toString());
	}
	
	public void writeBody(GroupResults groupResults, ResultStringer stringer) throws StringifyException {
		
		if(groupResults == null){
    		stringer.key("group_result").value("null");
		} else {
			GroupResult[] groupResultList = groupResults.groupResultList();
    		stringer.key("group_result").array("group_list");
    		for (int i = 0; i < groupResultList.length; i++) {
    			stringer.array("group_item");
				GroupResult groupResult = groupResultList[i];
				int size = groupResult.size();
				for (int k = 0; k < size; k++) {
					GroupEntry e = groupResult.getEntry(k);
					String keyData = e.key.getKeyString();
					String functionName = groupResult.headerNameList();
					
					stringer.object()
						.key("_no_").value(k+1)
						.key("key").value(keyData)
						.key("freq").value(e.count());
					
					if(groupResult.hasAggregationData()) {
						String r = e.getGroupingObjectResultString();
						if(r == null){
							r = "";
						}
						stringer.key(functionName).value(r);
					}
					stringer.endObject();
				}//for
				stringer.endArray();
    		}//for
    		stringer.endArray();
		}//if else
	}
}
