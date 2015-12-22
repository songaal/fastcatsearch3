package org.fastcatsearch.http.writer;

import java.io.IOException;
import java.io.Writer;
import java.util.HashSet;
import java.util.Set;

import org.fastcatsearch.common.Strings;
import org.fastcatsearch.ir.dic.HashSetDictionary;
import org.fastcatsearch.ir.group.GroupEntry;
import org.fastcatsearch.ir.group.GroupResult;
import org.fastcatsearch.ir.group.GroupResults;
import org.fastcatsearch.ir.group.GroupingValue;
import org.fastcatsearch.util.ResponseWriter;
import org.fastcatsearch.util.ResultWriterException;

public class GroupResultWriter extends AbstractSearchResultWriter {
	
	public GroupResultWriter(ResponseWriter resultWriter) {
		super(resultWriter);
	}
	
	@Override
	public void writeResult(Object result, long searchTime, boolean isSuccess) throws ResultWriterException, IOException{
		
		resultWriter.object();
		
		if(!isSuccess){
			String errorMsg = null;
			if(result == null){
				errorMsg = "null";
			}else{
				errorMsg = result.toString();
			}
			resultWriter.key("status").value(1)
			.key("time").value(Strings.getHumanReadableTimeInterval(searchTime))
			.key("total_count").value(0)
			.key("count").value(0)
			.key("error_msg").value(errorMsg);
			
		}else{
			
			GroupResults groupResults = (GroupResults) result;
			resultWriter.key("status").value(0)
			.key("time").value(Strings.getHumanReadableTimeInterval(searchTime))
			.key("total_count").value(groupResults.totalSearchCount())
			.key("count").value(groupResults.totalSearchCount());
			
			writeBody(groupResults, resultWriter);
		}
		
		resultWriter.endObject();
		resultWriter.done();
	}
	
	public void writeBody(GroupResults groupResults, ResponseWriter resultStringer) throws ResultWriterException {
		
		if(groupResults == null){
    		resultStringer.key("group_result").array("group_list").endArray();
		} else {
			GroupResult[] groupResultList = groupResults.groupResultList();
    		resultStringer.key("group_result").array("group_list");
    		for (int i = 0; i < groupResultList.length; i++) {
    			GroupResult groupResult = groupResultList[i];
    			resultStringer.object()
    			.key("label").value(groupResult.fieldId())
    			.key("functionNameList").array("function_item");
    			
    			String[] functionName = groupResult.headerNameList();
    			for (int k = 0; k < functionName.length; k++) {
    				resultStringer.value(functionName[k]);
    			}
    			resultStringer.endArray();

    			resultStringer.key("result").array("group_item");
				int size = groupResult == null ? 0 : groupResult.size();
				for (int k = 0; k < size; k++) {
					GroupEntry e = groupResult.getEntry(k);
					String keyData = e.key;
					
					resultStringer.object()
						.key("_KEY").value(keyData);

                    Set<String> dupSet = new HashSet<String>();
					for (int j = 0; j < functionName.length; j++) {
                        //동일그룹기능에 동일컬럼으로 중복되게 호출했을 경우, 헤더이름이 동일해서 json에러가 발생할수 있다.
                        if(dupSet.add(functionName[j])) {
                            GroupingValue val = e.groupingValue(j);
                            resultStringer.key(functionName[j]).value(val != null ? val.toString() : "");
                        }
					}
					
					resultStringer.endObject();
				}//for
				resultStringer.endArray();//group_item
				resultStringer.endObject();
    		}//for
    		resultStringer.endArray();
		}//if else
	}
}
