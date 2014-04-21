package org.fastcatsearch.job.internal;

import java.io.IOException;
import java.util.List;

import org.fastcatsearch.common.io.Streamable;
import org.fastcatsearch.exception.FastcatSearchException;
import org.fastcatsearch.ir.IRService;
import org.fastcatsearch.ir.document.Document;
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
import org.fastcatsearch.query.QueryParseException;
import org.fastcatsearch.query.QueryParser;
import org.fastcatsearch.service.ServiceManager;
import org.fastcatsearch.transport.vo.StreamableInternalSearchResult;

public class InternalSearchJob extends Job implements Streamable {
	private QueryMap queryMap;
	private boolean forMerging;
	
	public InternalSearchJob(){}
	
	public InternalSearchJob(QueryMap queryMap){
		this.queryMap = queryMap;
	}
	
	public InternalSearchJob(QueryMap queryMap, boolean forMerging){
		this.queryMap = queryMap;
		this.forMerging = forMerging;
	}
	
	@Override
	public JobResult doRun() throws FastcatSearchException {
		
		Query q = null;
		try {
			q = QueryParser.getInstance().parseQuery(queryMap);
		} catch (QueryParseException e) {
			throw new FastcatSearchException("ERR-01000", e.getMessage());
		}
		
		String collectionId = queryMap.collectionId();
		try {
			Metadata meta = q.getMeta();
			QueryModifier queryModifier = meta.queryModifier();
			//쿼리모디파이.
			if (queryModifier != null) {
				q = queryModifier.modify(q);
				meta = q.getMeta();
			}
			
			InternalSearchResult result = null;
			
			IRService irService = ServiceManager.getInstance().getService(IRService.class);
			
			//Not Exist in Cache
			if(result == null){
				CollectionHandler collectionHandler = irService.collectionHandler(collectionId);
				if(collectionHandler == null){
					throw new FastcatSearchException("ERR-00520", collectionId);
				}
				Query boostQuery = q.getBoostQuery();
				PkScoreList pkScoreList = null;
				if(boostQuery != null) {
					String boostKeyword = boostQuery.getMeta().getUserData("KEYWORD");
					pkScoreList = new PkScoreList(boostKeyword);
					String boostCollectionId= boostQuery.getMeta().collectionId();
					CollectionHandler boostCollectionHandler = irService.collectionHandler(boostCollectionId);
					CollectionSearcher boostCollectionSearcher = boostCollectionHandler.searcher();
					InternalSearchResult r = boostCollectionSearcher.searchInternal(boostQuery, forMerging);
					for(HitElement e : r.getHitElementList()) {
						if(e == null) {
							continue;
						}
						//첫번째 필드가 id이다.
						logger.debug("e.docNo() > {}", e.docNo());
						logger.debug("field > {}", boostCollectionSearcher.requestDocument(e.docNo()).get(1));
						String id = boostCollectionSearcher.requestDocument(e.docNo()).get(1).toString();
						int score = e.score();
						pkScoreList.add(new PkScore(id, score));
					}
				}
				result = collectionHandler.searcher().searchInternal(q, forMerging, pkScoreList);
			}

			return new JobResult(new StreamableInternalSearchResult(result));
			
		} catch (FastcatSearchException e){
			throw e;
		} catch(Exception e){
			logger.error("", e);
			throw new FastcatSearchException("ERR-00552", e, collectionId);
		}
		
	}
	@Override
	public void readFrom(DataInput input) throws IOException {
		this.queryMap = new QueryMap();
		queryMap.readFrom(input);
		this.forMerging = input.readBoolean();
	}
	@Override
	public void writeTo(DataOutput output) throws IOException {
		queryMap.writeTo(output);
		output.writeBoolean(forMerging);
	}
}
