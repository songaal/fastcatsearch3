package org.fastcatsearch.ir.search.clause;

import java.io.IOException;
import java.util.Iterator;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.core.AnalyzerOption;
import org.apache.lucene.analysis.tokenattributes.AdditionalTermAttribute;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.CharsRefTermAttribute;
import org.apache.lucene.analysis.tokenattributes.PositionIncrementAttribute;
import org.apache.lucene.analysis.tokenattributes.StopwordAttribute;
import org.apache.lucene.analysis.tokenattributes.SynonymAttribute;
import org.apache.lucene.analysis.tokenattributes.TypeAttribute;
import org.apache.lucene.util.CharsRef;
import org.fastcatsearch.ir.io.CharVector;
import org.fastcatsearch.ir.query.HighlightInfo;
import org.fastcatsearch.ir.query.RankInfo;
import org.fastcatsearch.ir.query.Term;
import org.fastcatsearch.ir.query.Term.Option;
import org.fastcatsearch.ir.query.Term.Type;
import org.fastcatsearch.ir.search.PostingReader;
import org.fastcatsearch.ir.search.SearchIndexReader;
import org.fastcatsearch.ir.search.method.NormalSearchMethod;
import org.fastcatsearch.ir.search.method.SearchMethod;
import org.fastcatsearch.ir.settings.IndexRefSetting;
import org.fastcatsearch.ir.settings.IndexSetting;

public class BooleanClause extends OperatedClause {

	private String termString;
	private SearchIndexReader searchIndexReader;
	private OperatedClause operatedClause;
	private float weight;
	
	public BooleanClause(SearchIndexReader searchIndexReader, Term term, HighlightInfo highlightInfo) {
		this(searchIndexReader, term, highlightInfo, null);
	}
	public BooleanClause(SearchIndexReader searchIndexReader, Term term, HighlightInfo highlightInfo, String requestTypeAttribute) {
		super(searchIndexReader.indexId());
		this.searchIndexReader = searchIndexReader;
		String indexId = searchIndexReader.indexId();
		String termString = term.termString();
		this.termString = termString;
		this.weight = term.weight();
		Option searchOption = term.option();
		CharVector fullTerm = new CharVector(termString);
		Analyzer analyzer = searchIndexReader.getQueryAnalyzerFromPool();

		IndexSetting indexSetting = searchIndexReader.indexSetting();
		if (highlightInfo != null) {
			String queryAnalyzerId = indexSetting.getQueryAnalyzer();
			for (IndexRefSetting refSetting : indexSetting.getFieldList()) {
				highlightInfo.add(refSetting.getRef(), refSetting.getIndexAnalyzer(), queryAnalyzerId, term.termString(), searchOption.value());
			}
		}
		try {
			
			//검색옵션에 따라 analyzerOption도 수정.
			AnalyzerOption analyzerOption = new AnalyzerOption();
			analyzerOption.useStopword(searchOption.useStopword());
			analyzerOption.useSynonym(searchOption.useSynonym());
			
			operatedClause = search(indexId, fullTerm, term.type(), indexSetting, analyzer, analyzerOption, requestTypeAttribute);
			
		} catch (IOException e) {
			logger.error("", e);
		} finally {
			searchIndexReader.releaseQueryAnalyzerToPool(analyzer);
		}
	}

	
	private OperatedClause search(String indexId, CharVector fullTerm, Type type, IndexSetting indexSetting, Analyzer analyzer, AnalyzerOption analyzerOption, String requestTypeAttribute) throws IOException {
		logger.debug("############ search Term > {}", fullTerm);
		OperatedClause operatedClause = null;
		
		CharTermAttribute termAttribute = null;
		CharsRefTermAttribute refTermAttribute = null;
		PositionIncrementAttribute positionAttribute = null;
		StopwordAttribute stopwordAttribute = null;
		TypeAttribute typeAttribute = null;
		AdditionalTermAttribute additionalTermAttribute = null;
		
		SynonymAttribute synonymAttribute = null;
		
		TokenStream tokenStream = analyzer.tokenStream(indexId, fullTerm.getReader(), analyzerOption);
		tokenStream.reset();

		CharTermAttribute charTermAttribute = tokenStream.getAttribute(CharTermAttribute.class);
		
		if (tokenStream.hasAttribute(CharsRefTermAttribute.class)) {
			refTermAttribute = tokenStream.getAttribute(CharsRefTermAttribute.class);
		}
		if (tokenStream.hasAttribute(CharTermAttribute.class)) {
			termAttribute = tokenStream.getAttribute(CharTermAttribute.class);
		}
		if (tokenStream.hasAttribute(PositionIncrementAttribute.class)) {
			positionAttribute = tokenStream.getAttribute(PositionIncrementAttribute.class);
		}
		if (tokenStream.hasAttribute(StopwordAttribute.class)) {
			stopwordAttribute = tokenStream.getAttribute(StopwordAttribute.class);
		}
		if (tokenStream.hasAttribute(TypeAttribute.class)) {
			typeAttribute = tokenStream.getAttribute(TypeAttribute.class);
		}
		if (tokenStream.hasAttribute(AdditionalTermAttribute.class)) {
			additionalTermAttribute = tokenStream.getAttribute(AdditionalTermAttribute.class);
		}
		if (tokenStream.hasAttribute(SynonymAttribute.class)) {
			synonymAttribute = tokenStream.getAttribute(SynonymAttribute.class);
		}

		CharVector token = null;
		int termSequence = 0;
		while (tokenStream.incrementToken()) {

			//요청 타입이 존재할때 타입이 다르면 단어무시.
			if(requestTypeAttribute != null && typeAttribute != null){
				if(requestTypeAttribute != typeAttribute.type()){
					continue;
				}
			}
			/* 
			 * stopword 
			 * */
			if (stopwordAttribute != null && stopwordAttribute.isStopword()) {
//				logger.debug("stopword");
				continue;
			}

			/*
			 * Main 단어는 tf를 적용하고, 나머지는 tf를 적용하지 않는다.
			 * */
			if (refTermAttribute != null) {
				CharsRef charRef = refTermAttribute.charsRef();

				if (charRef != null) {
					char[] buffer = new char[charRef.length()];
					System.arraycopy(charRef.chars, charRef.offset, buffer, 0, charRef.length);
					token = new CharVector(buffer, 0, buffer.length, indexSetting.isIgnoreCase());
				} else if (termAttribute != null && termAttribute.buffer() != null) {
					token = new CharVector(termAttribute.buffer(), indexSetting.isIgnoreCase());
				}
			} else {
				token = new CharVector(charTermAttribute.buffer(), 0, charTermAttribute.length(), indexSetting.isIgnoreCase());
			}
			
			logger.debug("token > {}, isIgnoreCase = {}", token, token.isIgnoreCase());
			int queryPosition = positionAttribute != null ? positionAttribute.getPositionIncrement() : 0;
//			logger.debug("token = {} : {}", token, queryPosition);

			SearchMethod searchMethod = searchIndexReader.createSearchMethod(new NormalSearchMethod());
			PostingReader postingReader = searchMethod.search(indexId, token, queryPosition, weight);

			OperatedClause clause = new TermOperatedClause(indexId, postingReader, termSequence++);
			
			// 유사어 처리
			// analyzerOption에 synonym확장여부가 들어가 있으므로, 여기서는 option을 확인하지 않고,
			// 있으면 그대로 색인하고 유사어가 없으면 색인되지 않는다.
			//
			//isSynonym 일 경우 다시한번 유사어확장을 하지 않는다.
			if(synonymAttribute != null) {
				CharVector[] synonyms = synonymAttribute.getSynonym();
				if(synonyms != null) {
					OperatedClause synonymClause = null;
					for(CharVector synonym : synonyms) {
						logger.debug("############ synonym > {}", synonym);
						SearchMethod localSearchMethod = searchIndexReader.createSearchMethod(new NormalSearchMethod());
						PostingReader localPostingReader = localSearchMethod.search(indexId, synonym, queryPosition, weight);
						OperatedClause localClause = new TermOperatedClause(indexId, localPostingReader, termSequence++);
//						OperatedClause localClause = search(indexId, synonym, type, indexSetting, analyzer, analyzerOption, requestTypeAttribute, true);
						if(synonymClause == null) {
							synonymClause = localClause;
						} else {
							synonymClause = new OrOperatedClause(synonymClause, localClause);
						}
					}
					
					if(synonymClause != null) {
						clause = new OrOperatedClause(clause, synonymClause);
					}
				}
			}
			
			if (operatedClause == null) {
				operatedClause = clause;
			} else {
				if(type == Type.ALL){
					operatedClause = new AndOperatedClause(operatedClause, clause);
				}else if(type == Type.ANY){
					operatedClause = new OrOperatedClause(operatedClause, clause);
				}
			}
		}

		//추가 확장 단어들.
		if(additionalTermAttribute != null) {
			Iterator<String[]> iterator = additionalTermAttribute.iterateAdditionalTerms();
			OperatedClause additionalClause = null;
			
			if(iterator != null) {
				while(iterator.hasNext()) {
					String[] str = iterator.next();
					
					CharVector localToken = new CharVector(str[0].toCharArray(), indexSetting.isIgnoreCase());
					
					int queryPosition = positionAttribute != null ? positionAttribute.getPositionIncrement() : 0;
					SearchMethod searchMethod = searchIndexReader.createSearchMethod(new NormalSearchMethod());
					PostingReader postingReader = searchMethod.search(indexId, localToken, queryPosition, weight);
					OperatedClause clause = new TermOperatedClause(indexId, postingReader, termSequence++);
					
					if(additionalClause == null) {
						additionalClause = clause;
					} else {
						additionalClause = new OrOperatedClause(additionalClause, clause);
					}
				}
				
				if(additionalClause != null) {
					operatedClause = new OrOperatedClause(operatedClause, additionalClause);
				}
			}
		}
		
		return operatedClause;
	}
	@Override
	protected boolean nextDoc(RankInfo rankInfo) {
		if (operatedClause == null) {
			return false;
		}
		return operatedClause.next(rankInfo);
	}

	@Override
	public void close() {
		if (operatedClause != null) {
			operatedClause.close();
		}
	}
	@Override
	protected void initClause(boolean explain) {
		if (operatedClause != null) {
			operatedClause.init(explanation != null ? explanation.createSubExplanation() : null);
		}
	}
	
	@Override
	public String term() {
		return termString;
	}

}
