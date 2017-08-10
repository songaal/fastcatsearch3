package org.fastcatsearch.job.management;

import java.io.IOException;
import java.io.StringReader;
import java.util.*;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.core.AnalyzerOption;
import org.apache.lucene.analysis.tokenattributes.AdditionalTermAttribute;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.CharsRefTermAttribute;
import org.apache.lucene.analysis.tokenattributes.PositionIncrementAttribute;
import org.fastcatsearch.common.io.Streamable;
import org.fastcatsearch.exception.FastcatSearchException;
import org.fastcatsearch.ir.IRService;
import org.fastcatsearch.ir.analysis.AnalyzerPool;
import org.fastcatsearch.ir.config.DataInfo.SegmentInfo;
import org.fastcatsearch.ir.document.Document;
import org.fastcatsearch.ir.document.DocumentReader;
import org.fastcatsearch.ir.field.Field;
import org.fastcatsearch.ir.io.BytesDataOutput;
import org.fastcatsearch.ir.io.DataInput;
import org.fastcatsearch.ir.io.DataOutput;
import org.fastcatsearch.ir.search.CollectionHandler;
import org.fastcatsearch.ir.search.SegmentReader;
import org.fastcatsearch.ir.search.SegmentSearcher;
import org.fastcatsearch.ir.settings.IndexRefSetting;
import org.fastcatsearch.ir.settings.IndexSetting;
import org.fastcatsearch.ir.settings.PrimaryKeySetting;
import org.fastcatsearch.ir.settings.RefSetting;
import org.fastcatsearch.ir.settings.Schema;
import org.fastcatsearch.ir.settings.SchemaSetting;
import org.fastcatsearch.job.Job;
import org.fastcatsearch.service.ServiceManager;
import org.fastcatsearch.vo.CollectionAnalyzedIndexData;
import org.fastcatsearch.vo.CollectionIndexData.RowData;

public class GetCollectionAnalyzedIndexDataJob extends Job implements Streamable {

	private static final long serialVersionUID = 5821814699500442825L;
	
	private String collectionId;
	private int start;
	private int end;
	private String pkValue;
	
	private AnalyzerOption indexingAnalyzerOption;
	
	public GetCollectionAnalyzedIndexDataJob() {}
	
	public GetCollectionAnalyzedIndexDataJob(String collectionId, int start, int end, String pkValue) {
		this.collectionId = collectionId;
		this.start = start;
		this.end = end;
		this.pkValue = pkValue;
	}
	

	@Override
	public JobResult doRun() throws FastcatSearchException {
		
		//색인시는 stopword만 본다.
		indexingAnalyzerOption = new AnalyzerOption();
		indexingAnalyzerOption.useStopword(true);
				
		IRService irService = ServiceManager.getInstance().getService(IRService.class);

		CollectionHandler collectionHandler = irService.collectionHandler(collectionId);
		if(collectionHandler == null || !collectionHandler.isLoaded()){
			CollectionAnalyzedIndexData data = new CollectionAnalyzedIndexData(collectionId, 0, 0, null, null, null, null, null);
			return new JobResult(data);
		}
		
		int segmentSize = collectionHandler.segmentSize();
		
		List<String> fieldList = new ArrayList<String>();
		List<RowData> pkDataList = new ArrayList<RowData>();
		List<RowData> indexDataList = new ArrayList<RowData>();
		List<RowData> analyzedDataList = new ArrayList<RowData>();
		List<Boolean> isDeletedList = new ArrayList<Boolean>();
		
		int documentSize = 0;
        int deleteSize = 0;
		try{

			Schema schema = collectionHandler.schema();
			SchemaSetting schemaSetting = collectionHandler.schema().schemaSetting();
			PrimaryKeySetting primaryKeySetting = schemaSetting.getPrimaryKeySetting();
			List<RefSetting> primaryKeyIdList = primaryKeySetting.getFieldList();
			List<IndexSetting> indexSettingList = schemaSetting.getIndexSettingList();
			
			for (int i = 0; i < indexSettingList.size(); i++) {
				IndexSetting indexSetting = indexSettingList.get(i);
				String indexId = indexSetting.getId();
				fieldList.add(indexId);
			}
			
			if(pkValue != null && pkValue.length() > 0) {
				if(primaryKeyIdList != null && primaryKeyIdList.size() > 0) {
					String[] pkList = pkValue.split("\\W");
					BytesDataOutput tempOutput = new BytesDataOutput();
					int count = 0;
					Set<String> dupSet = new HashSet<String>();
					for(String pk : pkList) {
						pk = pk.trim();
						if(pk.length() == 0) {
							continue;
						}
						if(dupSet.contains(pk)){
							continue;
						}else{
							dupSet.add(pk);
						}
						for(SegmentReader segmentReader : collectionHandler.segmentReaders()) {
							int docNo = segmentReader.newSearchIndexesReader().getPrimaryKeyIndexesReader().getDocNo(pk, tempOutput);
							if (docNo != -1) {
								if(count >= start && count <= end) {
									Document document = segmentReader.segmentSearcher().getDocument(docNo);
									if(document != null) {
										isDeletedList.add(segmentReader.deleteSet().isSet(docNo));
										add(document, primaryKeyIdList, schema, collectionHandler, segmentReader.segmentInfo().getId(), indexSettingList, pkDataList, indexDataList, analyzedDataList);

									}
								}
								documentSize++;
								count++;
							}
						}

						for(SegmentReader segmentReader : collectionHandler.segmentReaders()) {
							int docNo = segmentReader.newSearchIndexesReader().getPrimaryKeyIndexesReader().getDocNo(pk, tempOutput);
							if (docNo != -1) {
								if(count >= start && count <= end) {
									Document document = segmentReader.segmentSearcher().getDocument(docNo);
									if(document != null) {
										isDeletedList.add(segmentReader.deleteSet().isSet(docNo));
										add(document, primaryKeyIdList, schema, collectionHandler, segmentReader.segmentInfo().getId(), indexSettingList, pkDataList, indexDataList, analyzedDataList);
									}
								}
								documentSize++;
								count++;
							}
						}
					}
				}
			} else {
				
				//이 배열의 index번호는 세그먼트번호.
				SegmentReader[] segmentReaderList = new SegmentReader[segmentSize];
				int[] segmentEndNumbers = new int[segmentSize];
				TreeSet treeSet = new TreeSet<SegmentReader>(collectionHandler.segmentReaders());
				//descendingIterator 로 세그먼트 이름 내림차순으로 최신문서순. 하지만 세그먼트 이름이 한바퀴 다 돌면 최신순이 아니다.
				Iterator<SegmentReader> iterator = treeSet.descendingIterator();
				for (int segmentNumber = 0; iterator.hasNext(); segmentNumber++) {
					SegmentReader reader = iterator.next();
					DocumentReader documentReader = reader.newDocumentReader();
					int count = documentReader.getDocumentCount();
					documentSize += count;
                    deleteSize += reader.deleteSet().getOnCount();
					segmentReaderList[segmentNumber] = reader;
					segmentEndNumbers[segmentNumber] = documentSize - 1;
					logger.debug("segmentEndNumbers[{}]={}", segmentNumber, segmentEndNumbers[segmentNumber]);
				}

				//여러세그먼트에 걸쳐있을 경우를 고려한다.
				List<Integer[]> matchSegmentList = matchSegment(segmentEndNumbers, start, end - start + 1);
				for(Integer[] matchSegment : matchSegmentList) {
					int segmentSequence = matchSegment[0];
					int startNo = matchSegment[1];
					int endNo = matchSegment[2];

					SegmentReader segmentReader = segmentReaderList[segmentSequence];

					if (segmentReader != null) {
						SegmentInfo segmentInfo = segmentReader.segmentInfo();
//						String segmentId = segmentInfo.getId();
						SegmentSearcher segmentSearcher = segmentReader.segmentSearcher();

						for (int docNo = startNo; docNo <= endNo; docNo++) {

							Document document = segmentSearcher.getDocument(docNo);
							if(document == null){
								//문서의 끝에 다다름.
								break;
							}
							isDeletedList.add(segmentReader.deleteSet().isSet(docNo));
							add(document, primaryKeyIdList, schema, collectionHandler, segmentReader.segmentInfo().getId(), indexSettingList, pkDataList, indexDataList, analyzedDataList);
						}


					} else {
						logger.debug("segmentReader is NULL");
					}
				}
			}
		} catch (Throwable e) {
			logger.error("", e);
		}
		
		CollectionAnalyzedIndexData data = new CollectionAnalyzedIndexData(collectionId, documentSize, deleteSize, fieldList, pkDataList, indexDataList, analyzedDataList, isDeletedList);
		return new JobResult(data);
	}
	
	private void add(Document document, List<RefSetting> primaryKeyIdList, Schema schema, CollectionHandler collectionHandler, String segmentId, List<IndexSetting> indexSettingList, List<RowData> pkDataList, List<RowData> indexDataList, List<RowData> analyzedDataList){
		
		
		int pkSize = (primaryKeyIdList != null && primaryKeyIdList.size() > 0) ? primaryKeyIdList.size() : 0;
		String[][] pkData = new String[pkSize][];
		for (int index = 0; index < pkSize; index++) {
			RefSetting refSetting = primaryKeyIdList.get(index);
			String fieldId = refSetting.getRef();
			int pkFieldSequence = schema.getFieldSequence(fieldId);
			Field field = document.get(pkFieldSequence);
			String fieldData = field.toString();
//			logger.debug("PK {} > {} > {}", refSetting, pkFieldSequence, field);
			pkData[index] = new String[] { fieldId, fieldData };
		}
		RowData pkRowData = new RowData(segmentId, pkData);
		pkDataList.add(pkRowData);
		
		String[][] indexData = new String[indexSettingList.size()][];
		String[][] analyzedData = new String[indexSettingList.size()][];
		
		for (int k = 0; k < indexSettingList.size(); k++) {
			StringBuffer analyzedBuffer = new StringBuffer();
			
			IndexSetting indexSetting = indexSettingList.get(k);
			String indexId = indexSetting.getId();
			List<IndexRefSetting> refList = indexSetting.getFieldList();
			boolean isIgnoreCase = indexSetting.isIgnoreCase();
			boolean isStorePosition = indexSetting.isStorePosition();
			int positionIncrementGap = indexSetting.getPositionIncrementGap();
			boolean noAdditional = indexSetting.isNoAdditional();
			int gapOffset = 0;
			
			StringBuffer allFieldData = new StringBuffer();
			for (int m = 0; m < refList.size(); m++) {
				IndexRefSetting refSetting = refList.get(m);
				String fieldId = refSetting.getRef();
				String indexAnalyzerId = refSetting.getIndexAnalyzer();
				int fieldSequence = schema.getFieldSequence(fieldId);
				Field field = document.get(fieldSequence);
				String data = field.toString();
				if(isIgnoreCase){
					data = data.toUpperCase();
				}
				if(allFieldData.length() > 0) {
					allFieldData.append(" ");
				}
				allFieldData.append(data);
				
				AnalyzerPool analyzerPool = collectionHandler.getAnalyzerPool(indexAnalyzerId);
				Analyzer analyzer = analyzerPool.getFromPool();
				try{
					TokenStream tokenStream = analyzer.tokenStream(fieldId, new StringReader(data), indexingAnalyzerOption);
					tokenStream.reset();
					CharsRefTermAttribute refTermAttribute = null;
					PositionIncrementAttribute positionAttribute = null;
					CharTermAttribute termAttribute = null;
					AdditionalTermAttribute addTermAttribute = null;
					if(tokenStream.hasAttribute(CharsRefTermAttribute.class)){
						refTermAttribute = tokenStream.getAttribute(CharsRefTermAttribute.class);
					}
					if (tokenStream.hasAttribute(PositionIncrementAttribute.class)) {
						positionAttribute = tokenStream.getAttribute(PositionIncrementAttribute.class);
					}
					if (tokenStream.hasAttribute(CharTermAttribute.class)) {
						termAttribute = tokenStream.getAttribute(CharTermAttribute.class);
					}
					if (tokenStream.hasAttribute(AdditionalTermAttribute.class)) {
						addTermAttribute = tokenStream.getAttribute(AdditionalTermAttribute.class);
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
						if (addTermAttribute != null) {
							Iterator<String> addTerms = addTermAttribute.iterateAdditionalTerms();
							if(addTerms.hasNext()) {
								analyzedBuffer.append(" (");
								int j = 0;
								while (addTerms.hasNext()) {
									String addTerm = addTerms.next();
									if (j > 0) {
										analyzedBuffer.append(", ");
									}
									analyzedBuffer.append(addTerm);
									j++;
								}
								analyzedBuffer.append(") ");
							}
						}
						if(position != -1){
							analyzedBuffer.append(" [");
							analyzedBuffer.append(position);
							analyzedBuffer.append("]");
						}
					}
				} catch (IOException e) {
					logger.error("", e);
				}finally{
					analyzerPool.releaseToPool(analyzer);
				}
				
				//필드가 바뀌면 positionIncrementGap 만큼 포지션이 증가한다.
				gapOffset += positionIncrementGap;
			}
			
			indexData[k] = new String[] {indexId, allFieldData.toString()};
			analyzedData[k] = new String[] {indexId, analyzedBuffer.toString()};
			
		}//for
		
		RowData indexRowData = new RowData(segmentId, indexData);
		indexDataList.add(indexRowData);
		
		RowData analyzedRowData = new RowData(segmentId, analyzedData);
		analyzedDataList.add(analyzedRowData);
	}
	private List<Integer[]> matchSegment(int[] segEndNums, int start, int rows) {
		// [][세그먼트번호,시작번호,끝번호]
		ArrayList<Integer[]> list = new ArrayList<Integer[]>();
		for (int i = 0; i < segEndNums.length; i++) {
			if (start > segEndNums[i]) {
				start = start - segEndNums[i] - 1;
			} else {
				Integer[] res = new Integer[3];
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

		return list;
	}
	
	@Override
	public void readFrom(DataInput input) throws IOException {
		collectionId = input.readString();
		start = input.readInt();
		end = input.readInt();
		pkValue = input.readString();
	}

	@Override
	public void writeTo(DataOutput output) throws IOException {
		output.writeString(collectionId);
		output.writeInt(start);
		output.writeInt(end);
		output.writeString(pkValue);
	}

}
