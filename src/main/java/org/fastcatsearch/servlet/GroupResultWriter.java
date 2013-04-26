package org.fastcatsearch.servlet;

import org.fastcatsearch.util.ResultStringer;

public class GroupResultWriter {

	
	private void writeFooter() {
		
	}

	public boolean writeHeader(ResultStringer rStringer) {
		
		return false;
	}
	
	public void writeBody(ResultStringer rStringer) {
		group
		GroupResults aggregationResult = result.getGroupResult();
		if(aggregationResult == null){
    		rStringer.key("group_result").value("null");
		} else {
			GroupResult[] groupResultList = aggregationResult.groupResultList();
    		rStringer.key("group_result").array("group_list");
    		for (int i = 0; i < groupResultList.length; i++) {
    			rStringer.array("group_list");
				GroupResult groupResult = groupResultList[i];
				int size = groupResult.size();
				for (int k = 0; k < size; k++) {
					GroupEntry e = groupResult.getEntry(k);
					String keyData = e.key.getKeyString();
					String functionName = groupResult.functionName();
					
					rStringer.object()
						.key("_no_").value(k+1)
						.key("key").value(keyData)
						.key("freq").value(e.count());
					
					if(groupResult.hasAggregationData()) {
						String r = e.getGroupingObjectResultString();
						if(r == null){
							r = "";
						}
						rStringer.key(functionName).value(r);
					}
					rStringer.endObject();
				}//for
				rStringer.endArray();
    		}//for
    		rStringer.endArray();
		}//if else
	}
}
