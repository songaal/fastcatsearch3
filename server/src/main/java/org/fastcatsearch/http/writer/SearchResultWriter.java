package org.fastcatsearch.http.writer;

import org.fastcatsearch.ir.field.*;
import org.fastcatsearch.ir.group.GroupResults;
import org.fastcatsearch.ir.query.Result;
import org.fastcatsearch.ir.query.Row;
import org.fastcatsearch.ir.query.RowExplanation;
import org.fastcatsearch.ir.search.ClauseExplanation;
import org.fastcatsearch.ir.search.Explanation;
import org.fastcatsearch.ir.util.Formatter;
import org.fastcatsearch.util.ResponseWriter;
import org.fastcatsearch.util.ResultWriterException;

import java.io.IOException;
import java.util.List;

public class SearchResultWriter extends AbstractSearchResultWriter {
	
	private String[] fieldNames;

	/**
	 * 결과값이 없을때 빈결과값을 리턴해야 하는 경우 사용
	 */
	private static final Result BLANK_RESULT = 
		new Result(new Row[0], new Row[0][0], new int[0], null, new String[0], 0, 0, 0, null, null);
	 
	public SearchResultWriter(ResponseWriter resultStringer) {
		super(resultStringer);
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
			if (result == null) {
				result = BLANK_RESULT;
			}
			fieldNames = result.getFieldNameList();
			resultWriter.object()
			.key("status").value(0)
			.key("time").value(Formatter.getFormatTime(searchTime))
			.key("start").value(result.getStart())
			.key("total_count").value(result.getTotalCount())
			.key("count").value(result.getCount())
			.key("field_count").value(fieldNames.length)
			.key("fieldname_list")
			.array("name");

			for (int i = 0; i < fieldNames.length; i++) {
				resultWriter.value(fieldNames[i]);
			}
			resultWriter.endArray();
			writeBody(result, resultWriter, searchTime);
			resultWriter.endObject();
			
			resultWriter.done();
		}
		
	}
	public void writeBody(Result result, ResponseWriter resultWriter, long searchTime) throws ResultWriterException {
		resultWriter.key("result");
		//data
		Row[] rows = result.getData();
		Row[][] bundleRowsList = result.getBundleData();
        int[] bundleTotalSizeList = result.getBundleTotalSizeList();
		List<RowExplanation>[] rowExplanationsList = result.getRowExplanationsList();

		if(rows.length == 0){
			resultWriter.array("item").endArray();
		}else{
			resultWriter.array("item");
			for (int i = 0; i < rows.length; i++) {
				Row row = rows[i];

				resultWriter.object();

				writeRowObject(row, (bundleRowsList != null && bundleRowsList[i] != null) ? bundleRowsList[i].length : 0);
				
				if(bundleRowsList != null && bundleRowsList[i] != null) {
					resultWriter.key("_bundleSize").value(bundleTotalSizeList[i]);
					resultWriter.key("_bundle");
                    resultWriter.array("item");
					for(Row bundleRow : bundleRowsList[i]){
						resultWriter.object();
						writeRowObject(bundleRow);
						resultWriter.endObject();
					}
					resultWriter.endArray();
				}
				
				if(rowExplanationsList != null) {
					resultWriter.key("_explain");
					resultWriter.array("item");
					List<RowExplanation> explanations = rowExplanationsList[i];
					for(RowExplanation exp : explanations){
						resultWriter.object()
						.key("id").value(exp.getId()).key("score").value(exp.getScore()).key("detail").value(exp.getDescription())
						.endObject();
					}
					resultWriter.endArray();
				}
				
				resultWriter.endObject();
			}
			resultWriter.endArray();
			
			GroupResults groupResult = result.getGroupResult();
			
			new GroupResultWriter(null).writeBody(groupResult, resultWriter);
			
			
		}
		
		List<Explanation> explanations = result.getExplanations();
		if(explanations != null) {
			resultWriter.key("_explain");
			resultWriter.array("item");
			for(Explanation exp : explanations) {
				resultWriter.object();
				resultWriter.key("nodeId").value(exp.getNodeId());
				resultWriter.key("collectionId").value(exp.getCollectionId());
				resultWriter.key("segmentId").value(exp.getSegmentId());
				
				ClauseExplanation clauseExplanation = exp.clauseExplanation();
				resultWriter.key("clause");
				writeClauseExplanation(clauseExplanation, resultWriter);
				resultWriter.endObject();
			}
			resultWriter.endArray();
		}
	}
	private void writeRowObject(Row row) throws ResultWriterException{
		writeRowObject(row, 0);
	}
	private void writeRowObject(Row row, int bundleSize) throws ResultWriterException{
		for(int k = 0; k < fieldNames.length; k++) {
			String fdata = null;
			if(fieldNames[k].equalsIgnoreCase(ScoreField.fieldName)) {
                fdata = String.valueOf(row.getScore());
            } else if(fieldNames[k].equalsIgnoreCase(HitField.fieldName)){
                fdata = String.valueOf(row.getHit());
			}else if(fieldNames[k].equalsIgnoreCase(BundleSizeField.fieldName)){
				fdata = String.valueOf(bundleSize);
            }else if(fieldNames[k].equalsIgnoreCase(DistanceField.fieldName)){
				fdata = String.valueOf(row.getDistance());
			}else if(fieldNames[k].equalsIgnoreCase(MatchOrderField.fieldName)){
				fdata = String.valueOf(row.getFilterMatchOrder());
			}else{
				char[] f = row.get(k);
				fdata = new String(f).trim();
			}
			resultWriter.key(fieldNames[k]).value(fdata);
		}
	}
	
	private void writeClauseExplanation(ClauseExplanation clauseExplanation, ResponseWriter resultWriter) throws ResultWriterException {
		resultWriter.object()
		.key("id").value(clauseExplanation.getId()).key("term").value(clauseExplanation.getTerm())
		.key("rows").value(clauseExplanation.getRows()).key("time").value(clauseExplanation.getTime());
		List<ClauseExplanation> list = clauseExplanation.getSubExplanations();
		resultWriter.key("sub-explanation").array("item");
		if(list != null){
			for(ClauseExplanation exp : list) {
				writeClauseExplanation(exp, resultWriter);
			}
		}
		resultWriter.endArray();
		resultWriter.endObject();
	}
}
