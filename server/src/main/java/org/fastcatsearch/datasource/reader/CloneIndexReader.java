///*
// * Copyright (c) 2013 Websquared, Inc.
// * All rights reserved. This program and the accompanying materials
// * are made available under the terms of the GNU Public License v2.0
// * which accompanies this distribution, and is available at
// * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
// *
// * Contributors:
// *     lupfeliz - initial API and implementation
// */
//
//package org.fastcatsearch.datasource.reader;
//
//import java.io.File;
//import java.io.IOException;
//import java.util.HashMap;
//import java.util.Map;
//
//import org.fastcatsearch.datasource.SourceModifier;
//import org.fastcatsearch.datasource.reader.annotation.SourceReader;
//import org.fastcatsearch.ir.IRService;
//import org.fastcatsearch.ir.common.IRException;
//import org.fastcatsearch.ir.config.CollectionContext;
//import org.fastcatsearch.ir.config.DataInfo;
//import org.fastcatsearch.ir.config.SingleSourceConfig;
//import org.fastcatsearch.ir.document.Document;
//import org.fastcatsearch.ir.field.Field;
//import org.fastcatsearch.ir.io.BitSet;
//import org.fastcatsearch.ir.search.CollectionHandler;
//import org.fastcatsearch.ir.search.SegmentReader;
//import org.fastcatsearch.ir.search.SegmentSearcher;
//import org.fastcatsearch.ir.settings.SchemaSetting;
//import org.fastcatsearch.service.ServiceManager;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//
//@SourceReader(name="INDEX_CLONE")
//public class CloneIndexReader extends SingleSourceReader<Map<String, Object>> {
//
//	private static Logger logger = LoggerFactory.getLogger(CloneIndexReader.class);
//
//	private Map<String, Object> dataRecord;
//
//	private int currentSegment = 0;
//
//	private int totalSegments = 0;
//
//	private int lastSegmentDocNo;
//
//	private int docNo;
//
//	private int totalCnt = 0;
//
//	private CollectionHandler collectionHandler;
//	private SegmentSearcher segmentSearcher;
//	private BitSet currentDeleteSet;
//
//	public CloneIndexReader() {
//		super();
//	}
//
//	public CloneIndexReader(String collectionId, File filePath, SingleSourceConfig singleSourceConfig, SourceModifier<Map<String, Object>> sourceModifier, String lastIndexTime)
//			throws IRException {
//		super(collectionId, filePath, singleSourceConfig, sourceModifier, lastIndexTime);
//	}
//
//	@Override
//	protected void initParameters() {
//		registerParameter(new SourceReaderParameter("collection", "collection", "Select Collection's Id For Dump", SourceReaderParameter.TYPE_STRING, true, null));
//	}
//
//	@Override
//	public void init() throws IRException {
//		String collection = getConfigString("collection");
//		IRService irService = ServiceManager.getInstance().getService(IRService.class);
//		collectionHandler = irService.collectionHandler(collection);
//		CollectionContext collectionContext = collectionHandler.collectionContext();
//		DataInfo dataInfo = collectionContext.dataInfo();
//
//		totalCnt = dataInfo.getDocuments();
//		totalSegments = dataInfo.getSegmentSize();
//		docNo = 0;
//	}
//
//	@Override
//	public void close() throws IRException {
//	}
//
//	@Override
//	public boolean hasNext() throws IRException {
//		if(dataRecord == null) {
//			fill();
//		}
//		if(dataRecord == null) {
//			return false;
////throw new IRException("");
//		} else {
//			return true;
//		}
//	}
//
//	@Override
//	protected final Map<String, Object> next() throws IRException {
//		if(dataRecord == null) {
//			fill();
//		}
//		Map<String, Object>ret = dataRecord;
//		dataRecord = null;
//		return ret;
//	}
//
//	private void fill() throws IRException {
//		try {
//			while(true) {
//				boolean isContinue = false;
//				if(segmentSearcher == null) {
//					logger.trace("CURRENTSEGMENT:{}/{}", currentSegment, totalSegments);
//					if(currentSegment < totalSegments) {
//						SegmentReader segmentReader = collectionHandler.segmentReader(currentSegment);
//						segmentSearcher = collectionHandler.segmentSearcher(currentSegment);
//						currentDeleteSet = segmentReader.deleteSet();
//						if(currentSegment + 1 == totalSegments) {
//							//if last segment, lastDocNo = totalCnt
//							lastSegmentDocNo = totalCnt;
//						} else {
//							//else lastDocNo = nextBase
//
//
//                            //FIXME
////							lastSegmentDocNo = segmentReader.segmentInfo().getNextSegmentInfo().getBaseNumber();
//
//
//
//
//						}
//						currentSegment++;
//					} else {
//						break;
//					}
//				}
//				logger.trace("DOCNO:{}/{}", docNo, lastSegmentDocNo);
//				//if(docNo < lastSegmentDocNo) {
//					if(logger.isTraceEnabled()) {
//						logger.trace("DOC is Deleted?:{}", currentDeleteSet.isSet(docNo));
//					}
//					if(!currentDeleteSet.isSet(docNo)) {
//						Document document = segmentSearcher.getDocument(docNo);
//						if(document != null) {
//							Map<String, Object>newData = new HashMap<String, Object>();
//							int fsize = document.size();
//							for (int finx = 0; finx < fsize; finx++) {
//								Field field = document.get(finx);
//								newData.put(field.getId(), field.toString());
//							}
//							dataRecord = newData;
//							logger.trace("DOCNO:{} / DATA:{}", docNo,dataRecord);
//						} else {
//							segmentSearcher = null;
//							isContinue = true;
//						}
//					} else {
//						isContinue = true;
//					}
//				//} else {
//				//	segmentSearcher = null;
//				//	isContinue = true;
//				//}
//				docNo++;
//				if(isContinue) {
//					continue;
//				}
//
//				logger.trace("DOCNO:{} / ISCONT:{} / DATA:{}", docNo,isContinue,dataRecord);
//				break;
//			}
//		} catch (IOException e) {
//			logger.error("", e);
//		} finally {
//		}
//	}
//
//	@Override
//	public SchemaSetting getAutoGeneratedSchemaSetting() {
//		Map<String, String> properties = singleSourceConfig.getProperties();
//		String collection = properties.get("collection");
//		IRService irService = ServiceManager.getInstance().getService(IRService.class);
//		CollectionHandler collectionHandler = irService.collectionHandler(collection);
//		return collectionHandler.schema().schemaSetting();
//	}
//}
