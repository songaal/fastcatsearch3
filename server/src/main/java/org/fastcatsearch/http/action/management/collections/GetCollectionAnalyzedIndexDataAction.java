package org.fastcatsearch.http.action.management.collections;

import java.io.IOException;
import java.io.StringReader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.CharsRefTermAttribute;
import org.apache.lucene.analysis.tokenattributes.PositionIncrementAttribute;
import org.fastcatsearch.http.ActionAuthority;
import org.fastcatsearch.http.ActionAuthorityLevel;
import org.fastcatsearch.http.ActionMapping;
import org.fastcatsearch.http.action.ActionRequest;
import org.fastcatsearch.http.action.ActionResponse;
import org.fastcatsearch.http.action.AuthAction;
import org.fastcatsearch.ir.IRService;
import org.fastcatsearch.ir.analysis.AnalyzerPool;
import org.fastcatsearch.ir.config.DataInfo.RevisionInfo;
import org.fastcatsearch.ir.config.DataInfo.SegmentInfo;
import org.fastcatsearch.ir.document.Document;
import org.fastcatsearch.ir.field.Field;
import org.fastcatsearch.ir.search.CollectionHandler;
import org.fastcatsearch.ir.search.SegmentReader;
import org.fastcatsearch.ir.search.SegmentSearcher;
import org.fastcatsearch.ir.settings.IndexSetting;
import org.fastcatsearch.ir.settings.PrimaryKeySetting;
import org.fastcatsearch.ir.settings.RefSetting;
import org.fastcatsearch.ir.settings.Schema;
import org.fastcatsearch.ir.settings.SchemaSetting;
import org.fastcatsearch.service.ServiceManager;
import org.fastcatsearch.util.ResponseWriter;
import org.fastcatsearch.util.ResultWriterException;

@ActionMapping(value="/management/collections/index-data-analyzed", authority = ActionAuthority.Collections, authorityLevel = ActionAuthorityLevel.READABLE)
public class GetCollectionAnalyzedIndexDataAction extends AuthAction {

	@Override
	public void doAuthAction(ActionRequest request, ActionResponse response) throws Exception {

		String collectionId = request.getParameter("collectionId");
		int start = Integer.parseInt(request.getParameter("start", "0"));
		int end = Integer.parseInt(request.getParameter("end", "0"));
		
		IRService irService = ServiceManager.getInstance().getService(IRService.class);

		Writer writer = response.getWriter();
		ResponseWriter resultWriter = getDefaultResponseWriter(writer);
		
		
		CollectionHandler collectionHandler = irService.collectionHandler(collectionId);
		if(collectionHandler == null || !collectionHandler.isLoaded()){
			resultWriter.object()
			.key("collectionId").value(collectionId)
			.key("documentSize").value(0)
			.key("fieldList").array().endArray()
			.key("indexData").array().endArray()
			.endObject();
			
			resultWriter.done();
			return;
		}
		int documentSize = collectionHandler.collectionContext().dataInfo().getDocuments();
		
		
		int segmentSize = collectionHandler.segmentSize();
		//이 배열의 index번호는 세그먼트번호.
		int[] segmentEndNumbers = new int[segmentSize];
		for (int segmentNumber = 0; segmentNumber < segmentSize; segmentNumber++) {
			SegmentReader reader = collectionHandler.segmentReader(segmentNumber);
			SegmentInfo segmentInfo = reader.segmentInfo();
			RevisionInfo revisionInfo = segmentInfo.getRevisionInfo();
			segmentEndNumbers[segmentNumber] = segmentInfo.getBaseNumber() + revisionInfo.getDocumentCount() - 1;
		}
		
		//여러세그먼트에 걸쳐있을 경우를 고려한다.
		int[][] matchSegmentList = matchSegment(segmentEndNumbers, start, end - start + 1);

		Schema schema = collectionHandler.schema();
		SchemaSetting schemaSetting = collectionHandler.schema().schemaSetting();
		PrimaryKeySetting primaryKeySetting = schemaSetting.getPrimaryKeySetting();
		List<RefSetting> primaryKeyIdList = primaryKeySetting.getFieldList();
		List<IndexSetting> indexSettingList = schemaSetting.getIndexSettingList();
		
		//per index field id
		List<Analyzer> analyzerList = new ArrayList<Analyzer>();
		
//		for (int i = 0; i < indexSettingList.size(); i++) {
//			IndexSetting indexSetting = indexSettingList.get(i);
//			indexSetting.getId();
//			indexSetting.getFieldList();
//			String indexAnalyzer = indexSetting.getIndexAnalyzer();
//			indexSetting.isIgnoreCase();
//			
//			AnalyzerPool pool = collectionHandler.getAnalyzerPool(indexAnalyzer);
//			Analyzer analyzer = pool.getFromPool();
//			analyzerList.add(analyzer); 
//		}
		
		try{
			resultWriter.object()
			.key("collectionId").value(collectionId)
			.key("documentSize").value(documentSize);
			
			resultWriter.key("primaryKeyList").array();
			for(RefSetting refSetting : primaryKeyIdList){
				resultWriter.value(refSetting.getRef());
			}
			resultWriter.endArray();
			
			//write field list
			resultWriter.key("fieldList").array();
			if(matchSegmentList.length > 0){
				int segmentNumber = matchSegmentList[0][0];
				int startNo = matchSegmentList[0][1];
				SegmentReader segmentReader = collectionHandler.segmentReader(segmentNumber);
				SegmentSearcher segmentSearcher = segmentReader.segmentSearcher();
				Document headerDocument = segmentSearcher.getDocument(startNo);
//				for (int index = 0; index < headerDocument.size(); index++) {
//					Field field = headerDocument.get(index);
//					resultWriter.value(field.getId());
//				}
				
				for (int i = 0; i < indexSettingList.size(); i++) {
					IndexSetting indexSetting = indexSettingList.get(i);
					String indexId = indexSetting.getId();
					indexSetting.getFieldList();
					String indexAnalyzer = indexSetting.getIndexAnalyzer();
					indexSetting.isIgnoreCase();
					
					AnalyzerPool pool = collectionHandler.getAnalyzerPool(indexAnalyzer);
					Analyzer analyzer = pool.getFromPool();
					analyzerList.add(analyzer); 
					
					resultWriter.value(indexId);
				}
			}
			resultWriter.endArray();
			
			//write data
			resultWriter.key("indexData").array();
			for (int i = 0; i < matchSegmentList.length; i++) {
				int segmentNumber = matchSegmentList[i][0];
				int startNo = matchSegmentList[i][1];
				int endNo = matchSegmentList[i][2];
				
				SegmentReader segmentReader = collectionHandler.segmentReader(segmentNumber);
				
				if (segmentReader != null) {
					SegmentInfo segmentInfo = segmentReader.segmentInfo();
					String segmentId = segmentInfo.getId();
					SegmentSearcher segmentSearcher = segmentReader.segmentSearcher();
					
					for (int docNo = startNo; docNo <= endNo; docNo++) {
						Document document = segmentSearcher.getDocument(docNo);
						if(document == null){
							//문서의 끝에 다다름.
							break;
						}
						
						resultWriter.object()
						.key("segmentId").value(segmentId);
						
						resultWriter.key("primaryKeys").object();
						for(RefSetting refSetting : primaryKeyIdList){
							String fieldId = refSetting.getRef();
							int pkFieldSequence = schema.getFieldSequence(fieldId);
							Field field = document.get(pkFieldSequence);
							String fieldData = field.toString();
							
							resultWriter.key(fieldId).value(fieldData);
						}
						resultWriter.endObject();
						
						resultWriter.key("row").object();
						
						for (int k = 0; k < indexSettingList.size(); k++) {
							Analyzer analyzer = analyzerList.get(k);
							
							StringBuffer analyzedBuffer = new StringBuffer();
							
							IndexSetting indexSetting = indexSettingList.get(k);
							String indexId = indexSetting.getId();
							List<RefSetting> refList = indexSetting.getFieldList();
							boolean isIgnoreCase = indexSetting.isIgnoreCase();
							boolean isStorePosition = indexSetting.isStorePosition();
							int positionIncrementGap = indexSetting.getPositionIncrementGap();
							int gapOffset = 0;
							
							StringBuffer allFieldData = new StringBuffer();
							for(RefSetting refSetting : refList){
								String fieldId = refSetting.getRef();
								int fieldSequence = schema.getFieldSequence(fieldId);
								Field field = document.get(fieldSequence);
								String fieldData = field.toString();
								if(isIgnoreCase){
									fieldData = fieldData.toUpperCase();
								}
								
								allFieldData.append(fieldData);
								TokenStream tokenStream = analyzer.tokenStream(fieldId, new StringReader(fieldData));
								CharTermAttribute termAttribute = tokenStream.getAttribute(CharTermAttribute.class);
								CharsRefTermAttribute refTermAttribute = null;
								PositionIncrementAttribute positionAttribute = null;
								if(tokenStream.hasAttribute(CharsRefTermAttribute.class)){
									refTermAttribute = tokenStream.getAttribute(CharsRefTermAttribute.class);
								}
								if (tokenStream.hasAttribute(PositionIncrementAttribute.class)) {
									positionAttribute = tokenStream.getAttribute(PositionIncrementAttribute.class);
								}
								
								
								while(tokenStream.incrementToken()){
									String value = null;
									if (refTermAttribute != null) {
										value = refTermAttribute.charsRef().toString();
									} else {
										value = termAttribute.toString();
									}
									int position = -1;
									if (isStorePosition && positionAttribute != null) {
										position = positionAttribute.getPositionIncrement() + gapOffset;
									}
									
									if(analyzedBuffer.length() > 0){
										analyzedBuffer.append(", ");
									}
									analyzedBuffer.append(value);
									if(position != -1){
										analyzedBuffer.append(" [");
										analyzedBuffer.append(position);
										analyzedBuffer.append("]");
									}
								}
								
								//필드가 바뀌면 positionIncrementGap 만큼 포지션이 증가한다.
								gapOffset += positionIncrementGap;
							}
							
							resultWriter.key(indexId+"-ANALYZED").value(analyzedBuffer.toString());
							resultWriter.key(indexId).value(allFieldData.toString());
							
						}//for
						
						resultWriter.endObject().endObject();
						
					}
				} else {
					logger.debug("segmentReader is NULL");
				}
			}
			
			resultWriter.endArray()
			.endObject();
			
			resultWriter.done();
		}finally{
			for (int i = 0; i < indexSettingList.size(); i++) {
				IndexSetting indexSetting = indexSettingList.get(i);
				String indexAnalyzer = indexSetting.getIndexAnalyzer();
				
				AnalyzerPool pool = collectionHandler.getAnalyzerPool(indexAnalyzer);
				pool.releaseToPool(analyzerList.get(i));
			}
			analyzerList.clear();
		}
	}
	
	
	protected boolean makeResult(ResponseWriter resultWriter, SegmentSearcher segmentSearcher, int docNo, String segmentId, CollectionHandler collectionHandler) throws ResultWriterException, IOException{
		Document document = segmentSearcher.getDocument(docNo);
		if(document == null){
			//문서의 끝에 다다름.
			return false;
		}
		
		int fieldSize = document.size();
//		logger.debug("document >> {}", document);
		for (int index = 0; index < fieldSize; index++) {
			Field field = document.get(index);
			String fieldData = field.toString();
			StringBuffer sb = new StringBuffer();
			AnalyzerPool pool = collectionHandler.getAnalyzerPool("");
			Analyzer analyzer = pool.getFromPool();
			try{
				TokenStream tokenStream = analyzer.tokenStream(field.getId(), new StringReader(fieldData));
				
				while(tokenStream.incrementToken()){
					
				}
			}finally{
				pool.releaseToPool(analyzer);
			}
			resultWriter.key(field.getId()).value(sb.toString());
		}
		resultWriter.endObject().endObject();
		
		return true;
	}
	
	/*
	AnalyzerPool pool = collectionHandler.getAnalyzerPool("");
			Analyzer analyzer = pool.getFromPool();
			try{
				TokenStream tokenStream = analyzer.tokenStream(field.getId(), new StringReader(fieldData));
			}finally{
				pool.releaseToPool(analyzer);
			}
	 * */
	private int[][] matchSegment(int[] segEndNums, int start, int rows) {
		// [][세그먼트번호,시작번호,끝번호]
		ArrayList<int[]> list = new ArrayList<int[]>();
		for (int i = 0; i < segEndNums.length; i++) {
			if (start > segEndNums[i]) {
				start = start - segEndNums[i] - 1;
			} else {
				int[] res = new int[3];
				int emptyCount = segEndNums[i] - start + 1;
				res[0] = i;// 세그먼트번호
				if (emptyCount < rows) {
					res[1] = start;// 시작번호
					res[2] = segEndNums[i];
					start = 0;
					rows = rows - emptyCount;
					list.add(res);
				} else {
					res[1] = start;// 시작번호
					res[2] = start + rows - 1;// 끝번호
					list.add(res);
					break;
				}
			}
		}
		int[][] result = new int[list.size()][3];
		for (int i = 0; i < list.size(); i++) {
			int[] tmp = list.get(i);
			result[i][0] = tmp[0];
			result[i][1] = tmp[1];
			result[i][2] = tmp[2];
		}

		return result;
	}

}
