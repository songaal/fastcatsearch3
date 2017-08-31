/*
 * Copyright (c) 2013 Websquared, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 * 
 * Contributors:
 *     swsong - initial API and implementation
 */

package org.fastcatsearch.job.indexing;

import org.fastcatsearch.cluster.Node;
import org.fastcatsearch.cluster.NodeService;
import org.fastcatsearch.common.io.Streamable;
import org.fastcatsearch.datasource.reader.DataSourceReader;
import org.fastcatsearch.datasource.reader.DefaultDataSourceReaderFactory;
import org.fastcatsearch.db.mapper.IndexingResultMapper.ResultStatus;
import org.fastcatsearch.exception.FastcatSearchException;
import org.fastcatsearch.ir.DynamicIndexModule;
import org.fastcatsearch.ir.IRService;
import org.fastcatsearch.ir.common.IndexingType;
import org.fastcatsearch.ir.config.CollectionContext;
import org.fastcatsearch.ir.config.CollectionIndexStatus;
import org.fastcatsearch.ir.config.DataInfo;
import org.fastcatsearch.ir.config.DataSourceConfig;
import org.fastcatsearch.ir.document.Document;
import org.fastcatsearch.ir.index.DeleteIdSet;
import org.fastcatsearch.ir.index.PrimaryKeys;
import org.fastcatsearch.ir.settings.PrimaryKeySetting;
import org.fastcatsearch.ir.settings.RefSetting;
import org.fastcatsearch.ir.settings.Schema;
import org.fastcatsearch.job.result.IndexingJobResult;
import org.fastcatsearch.job.state.IndexingTaskState;
import org.fastcatsearch.service.ServiceManager;
import org.fastcatsearch.transport.vo.StreamableThrowable;
import org.fastcatsearch.util.CollectionContextUtil;
import org.json.JSONException;
import org.json.JSONStringer;
import org.json.JSONWriter;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * 특정 collection의 index node에서 수행되는 job.
 * index node가 아닌 노드에 전달되면 색인을 수행하지 않는다.
 *  
 * */
public class CollectionAddIndexingJob extends IndexingJob {

	private static final long serialVersionUID = 7898036370433248984L;

    private static final int BULK_INDEX_SIZE = 10000;
	@Override
	public JobResult doRun() throws FastcatSearchException {

        prepare(IndexingType.ADD, "ALL");

		Throwable throwable = null;
        ResultStatus resultStatus = ResultStatus.RUNNING;
		Object result = null;
        long startTime = System.currentTimeMillis();
		try {
			IRService irService = ServiceManager.getInstance().getService(IRService.class);
			CollectionContext collectionContext = irService.collectionContext(collectionId);
			if(collectionContext == null) {
				throw new FastcatSearchException("Collection [" + collectionId + "] is not exist.");
			}
			String indexNodeId = collectionContext.collectionConfig().getIndexNode();
			NodeService nodeService = ServiceManager.getInstance().getService(NodeService.class);
			Node indexNode = nodeService.getNodeById(indexNodeId);
			
			if(!nodeService.isMyNode(indexNode)){
				//작업수행하지 않음.
				throw new RuntimeException("Invalid index node collection[" + collectionId + "] node[" + indexNodeId + "]");
			}

            if(!updateIndexingStatusStart()) {
                logger.error("Cannot start indexing job. {} : {}", collectionId, indexNodeId);
                resultStatus = ResultStatus.CANCEL;
                return new JobResult();
            }

            int documentSize = 0;
            int deleteSize = 0;

            try {
                DynamicIndexModule dynamicIndexModule = irService.getDynamicIndexModule(collectionId);
                if (dynamicIndexModule == null) {
                    throw new FastcatSearchException("Collection [" + collectionId + "] is not exist.");
                }

                DataSourceConfig dataSourceConfig = collectionContext.dataSourceConfig();
                String lastIndexTime = collectionContext.getLastIndexTime();
                File filePath = collectionContext.collectionFilePaths().file();
                Schema schema = collectionContext.schema();
                DataSourceReader dataSourceReader = DefaultDataSourceReaderFactory.createAddIndexingSourceReader(collectionContext.collectionId(), filePath, schema.schemaSetting(), dataSourceConfig, lastIndexTime);

                List<String> jsonList = new ArrayList<String>();

                /*
                * 1. 문서추가 처리.
                * */
                while (dataSourceReader.hasNext()) {
                    if (stopRequested) {
                        break;
                    }
                    Document document = dataSourceReader.nextDocument();
                    try {
                        String jsonString = document.toJsonString();
                        jsonList.add(jsonString);
                        documentSize++;
                        indexingTaskState.incrementDocumentCount();
                    } catch(JSONException e) {
                        logger.error("error make json document", e);
                        continue;
                    }

                    if(jsonList.size() == BULK_INDEX_SIZE) {
                        dynamicIndexModule.insertDocument(jsonList);
                        jsonList.clear();
                    }
                }
                if(jsonList.size() > 0) {
                    dynamicIndexModule.insertDocument(jsonList);
                    jsonList.clear();
                }

                /*
                * 2. 삭제처리.
                * */
                PrimaryKeySetting primaryKeySetting = schema.schemaSetting().getPrimaryKeySetting();
                List<RefSetting> list = primaryKeySetting.getFieldList();
                DeleteIdSet deleteIdSet = dataSourceReader.getDeleteList();
                if(deleteIdSet != null) {
                    deleteSize = deleteIdSet.size();
                    for (PrimaryKeys pks : deleteIdSet) {
                        JSONStringer json = new JSONStringer();
                        JSONWriter w = json.object();
                        for (int i = 0; i < list.size(); i++) {
                            w.key(list.get(i).getRef()).value(pks.getKey(i));
                        }
                        w.endObject();
                        jsonList.add(w.toString());
                        if (jsonList.size() == BULK_INDEX_SIZE) {
                            dynamicIndexModule.deleteDocument(jsonList);
                            jsonList.clear();
                        }
                    }
                } else {
                    deleteSize = 0;
                }
                if(jsonList.size() > 0) {
                    dynamicIndexModule.deleteDocument(jsonList);
                    jsonList.clear();
                }
            } catch (Throwable e) {
                throw new FastcatSearchException(e);
            }

            indexingTaskState.setStep(IndexingTaskState.STEP_FINALIZE);
            long endTime = System.currentTimeMillis();
            int duration = (int) (endTime - startTime);

            CollectionIndexStatus.IndexStatus indexStatus = collectionContext.indexStatus().getAddIndexStatus();
            if(indexStatus == null) {
                indexStatus = new CollectionIndexStatus.IndexStatus();
            }
            indexStatus.setDocumentCount(documentSize);
            indexStatus.setDeleteCount(deleteSize);
            indexingLogger.info("[{}] Collection Add Indexing Finished! time = {}", collectionId, duration);

            result = new IndexingJobResult(collectionId, indexStatus, duration);
            resultStatus = ResultStatus.SUCCESS;
            indexingTaskState.setStep(IndexingTaskState.STEP_END);

            collectionContext.updateCollectionStatus(IndexingType.ADD, documentSize, deleteSize, startTime, endTime);
            CollectionContextUtil.saveCollectionAfterIndexing(collectionContext);

			return new JobResult(result);
        } catch (Throwable e) {
			indexingLogger.error("[" + collectionId + "] Indexing", e);
			throwable = e;
            resultStatus = ResultStatus.FAIL;
			throw new FastcatSearchException("ERR-00501", throwable, collectionId); // 색인실패.
		} finally {
            Streamable streamableResult = null;
            if (throwable != null) {
                streamableResult = new StreamableThrowable(throwable);
            } else if (result instanceof Streamable) {
                streamableResult = (Streamable) result;
            }

            updateIndexingStatusFinish(resultStatus, streamableResult);
        }

	}

}
