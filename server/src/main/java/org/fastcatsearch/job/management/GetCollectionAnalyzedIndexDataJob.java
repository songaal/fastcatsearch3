package org.fastcatsearch.job.management;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.CharsRefTermAttribute;
import org.apache.lucene.analysis.tokenattributes.PositionIncrementAttribute;
import org.fastcatsearch.common.io.Streamable;
import org.fastcatsearch.exception.FastcatSearchException;
import org.fastcatsearch.ir.IRService;
import org.fastcatsearch.ir.analysis.AnalyzerPool;
import org.fastcatsearch.ir.config.DataInfo.RevisionInfo;
import org.fastcatsearch.ir.config.DataInfo.SegmentInfo;
import org.fastcatsearch.ir.document.Document;
import org.fastcatsearch.ir.field.Field;
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
	
	public GetCollectionAnalyzedIndexDataJob() {}
	
	public GetCollectionAnalyzedIndexDataJob(String collectionId, int start, int end) {
		this.collectionId = collectionId;
		this.start = start;
		this.end = end;
	}
	

	@Override
	public JobResult doRun() throws FastcatSearchException {
		IRService irService = ServiceManager.getInstance().getService(IRService.class);

		CollectionHandler collectionHandler = irService.collectionHandler(collectionId);
		if(collectionHandler == null || !collectionHandler.isLoaded()){
			CollectionAnalyzedIndexData data = new CollectionAnalyzedIndexData(collectionId, 0, null, null, null, null);
			return new JobResult(data);
		}
		
		List<String> fieldList = new ArrayList<String>();
		List<RowData> pkDataList = new ArrayList<RowData>();
		List<RowData> indexDataList = new ArrayList<RowData>();
		List<RowData> analyzedDataList = new ArrayList<RowData>();
		
		int documentSize = 0;
		try{
			documentSize = collectionHandler.collectionContext().dataInfo().getDocuments();
			
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
			
			for (int i = 0; i < indexSettingList.size(); i++) {
				IndexSetting indexSetting = indexSettingList.get(i);
				String indexId = indexSetting.getId();
				fieldList.add(indexId);
			}
			
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
						
						int pkSize = (primaryKeyIdList != null && primaryKeyIdList.size() > 0) ? primaryKeyIdList.size() : 0;
						String[][] pkData = new String[pkSize][];
						for (int index = 0; index < pkSize; index++) {
							RefSetting refSetting = primaryKeyIdList.get(index);
							String fieldId = refSetting.getRef();
							int pkFieldSequence = schema.getFieldSequence(fieldId);
							Field field = document.get(pkFieldSequence);
							String fieldData = field.toString();
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
								
								allFieldData.append(data);
								
								AnalyzerPool analyzerPool = collectionHandler.getAnalyzerPool(indexAnalyzerId);
								Analyzer analyzer = analyzerPool.getFromPool();
								try{
									TokenStream tokenStream = analyzer.tokenStream(fieldId, new StringReader(data));
									tokenStream.reset();
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
				} else {
					logger.debug("segmentReader is NULL");
				}
			}
			
		} catch (IOException e) {
			logger.error("", e);
		}
		
		CollectionAnalyzedIndexData data = new CollectionAnalyzedIndexData(collectionId, documentSize, fieldList, pkDataList, indexDataList, analyzedDataList);
		return new JobResult(data);
	}
	
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
	
	@Override
	public void readFrom(DataInput input) throws IOException {
		collectionId = input.readString();
		start = input.readInt();
		end = input.readInt();
	}

	@Override
	public void writeTo(DataOutput output) throws IOException {
		output.writeString(collectionId);
		output.writeInt(start);
		output.writeInt(end);
	}

}
