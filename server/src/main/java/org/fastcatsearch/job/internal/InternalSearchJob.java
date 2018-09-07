package org.fastcatsearch.job.internal;

import java.io.IOException;
import java.util.Map;

import org.fastcatsearch.common.io.Streamable;
import org.fastcatsearch.error.SearchAbortError;
import org.fastcatsearch.error.SearchError;
import org.fastcatsearch.error.ServerErrorCode;
import org.fastcatsearch.exception.FastcatSearchException;
import org.fastcatsearch.ir.IRService;
import org.fastcatsearch.ir.io.DataInput;
import org.fastcatsearch.ir.io.DataOutput;
import org.fastcatsearch.ir.query.InternalSearchResult;
import org.fastcatsearch.ir.query.Metadata;
import org.fastcatsearch.ir.query.Query;
import org.fastcatsearch.ir.query.QueryModifier;
import org.fastcatsearch.ir.search.CollectionHandler;
import org.fastcatsearch.ir.search.CollectionSearcher;
import org.fastcatsearch.ir.search.HitElement;
import org.fastcatsearch.ir.search.PkScore;
import org.fastcatsearch.ir.search.PkScoreList;
import org.fastcatsearch.job.Job;
import org.fastcatsearch.query.QueryMap;
import org.fastcatsearch.query.QueryParser;
import org.fastcatsearch.service.ServiceManager;
import org.fastcatsearch.transport.vo.StreamableInternalSearchResult;
import org.fastcatsearch.util.SearchLogger;

public class InternalSearchJob extends Job implements Streamable {
    private static final long serialVersionUID = 4998297114497342795L;
    private QueryMap queryMap;
    private boolean forMerging;

    private CollectionSearcher boostCollectionSearcher;
    private CollectionSearcher mainCollectionSearcher;

    public InternalSearchJob(){}

    public InternalSearchJob(QueryMap queryMap){
        this.queryMap = queryMap;
    }

    public InternalSearchJob(QueryMap queryMap, boolean forMerging){
        this.queryMap = queryMap;
        this.forMerging = forMerging;
    }

    protected void whenAborted() {
        if(boostCollectionSearcher != null) {
            boostCollectionSearcher.abort();
        }
        if(mainCollectionSearcher != null) {
            mainCollectionSearcher.abort();
        }
    }

    @Override
    public JobResult doRun() throws FastcatSearchException {

        long st = System.nanoTime();
        Query q = QueryParser.getInstance().parseQuery(queryMap);
        String collectionId = queryMap.collectionId();
        Metadata meta = q.getMeta();
        String searchKeyword = meta.getUserData("KEYWORD");
        InternalSearchResult result = null;
        boolean isCache = false;
        String tagString = "";
        String errorMsg = null;
        try {
            QueryModifier queryModifier = meta.queryModifier();
            //쿼리모디파이.
            if (queryModifier != null) {
                q = queryModifier.modify(collectionId, q);
            }
            logger.debug("q > {}", q);

            Map<String, String> userDataMap = meta.userData();
            if(userDataMap != null) {
                StringBuilder sb = new StringBuilder();
                for(Map.Entry<String, String> e : userDataMap.entrySet()) {
                    if(! "KEYWORD".equals(e.getKey())) {
                        if(sb.length() > 0) {
                            sb.append(",");
                        }
                        sb.append(e.getKey()).append("=").append(e.getValue());
                    }
                }

                tagString = sb.toString();
            }

            IRService irService = ServiceManager.getInstance().getService(IRService.class);

            //Not Exist in Cache
            if(result == null){
                CollectionHandler collectionHandler = irService.collectionHandler(collectionId);
                if(collectionHandler == null){
                    throw new SearchError(ServerErrorCode.COLLECTION_NOT_FOUND, collectionId);
                }
                Query boostQuery = q.getBoostQuery();
                PkScoreList pkScoreList = null;
                if(boostQuery != null) {
                    try {
                        String boostKeyword = boostQuery.getMeta().getUserData("KEYWORD");
                        pkScoreList = new PkScoreList(boostKeyword);
                        String boostCollectionId = boostQuery.getMeta().collectionId();
                        CollectionHandler boostCollectionHandler = irService.collectionHandler(boostCollectionId);
                        boostCollectionSearcher = boostCollectionHandler.searcher();
                        InternalSearchResult r = boostCollectionSearcher.searchInternal(boostQuery, forMerging);
                        for (HitElement e : r.getHitElementList()) {
                            if (e == null) {
                                continue;
                            }
                            /**
                             * 첫번째 필드가 ID 이어야 한다.
                             */
//                            logger.debug("e.docNo() > {}", e.docNo());
                            String id = boostCollectionSearcher.requestDocument(e.segmentId(), e.docNo()).get(1).toString();
//                            logger.debug("field > {}", id);
                            int score = e.score();
                            pkScoreList.add(new PkScore(id, score));
                        }
                    } catch(Throwable t) {
                        logger.error("error while boosting query > " + boostQuery, t);
                    }
                }
                mainCollectionSearcher = collectionHandler.searcher();
                result = mainCollectionSearcher.searchInternal(q, forMerging, pkScoreList);
                long elapsed = (System.nanoTime() - st) / 1000000;
                if(elapsed > getTimeout()) {
                    throw new SearchError(ServerErrorCode.SEARCH_TIMEOUT_ERROR, String.valueOf(getTimeout()));
                }
            }

            return new JobResult(new StreamableInternalSearchResult(result));

        } catch (SearchError e){
            errorMsg = e.getMessage();
            throw e;
        } catch (SearchAbortError e){
            errorMsg = e.getMessage();
            throw e;
        } catch(Exception e){
            throw new FastcatSearchException(e);
        } finally {
            SearchLogger.writeSearchLog(collectionId, searchKeyword, result, (System.nanoTime() - st) / 1000000, isCache, errorMsg, tagString);
        }

    }

    @Override
    public void readFrom(DataInput input) throws IOException {
        setTimeout(input.readLong(), input.readBoolean()); //타임아웃.
        this.queryMap = new QueryMap();
        queryMap.readFrom(input);
        this.forMerging = input.readBoolean();
    }
    @Override
    public void writeTo(DataOutput output) throws IOException {
        output.writeLong(getTimeout());
        output.writeBoolean(isForceAbortWhenTimeout());
        queryMap.writeTo(output);
        output.writeBoolean(forMerging);
    }
}
