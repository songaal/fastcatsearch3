package org.fastcatsearch.ir.search.clause;

import java.io.IOException;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.CharsRefTermAttribute;
import org.apache.lucene.analysis.tokenattributes.PositionIncrementAttribute;
import org.apache.lucene.analysis.tokenattributes.StopwordAttribute;
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
import org.fastcatsearch.ir.settings.IndexSetting;
import org.fastcatsearch.ir.settings.RefSetting;

public class BooleanClause extends OperatedClause {

	private String termString;
	private OperatedClause operatedClause;

	public BooleanClause(SearchIndexReader searchIndexReader, Term term, HighlightInfo highlightInfo) {
		this(searchIndexReader, term, highlightInfo, null);
	}
	public BooleanClause(SearchIndexReader searchIndexReader, Term term, HighlightInfo highlightInfo, String requestTypeAttribute) {
		super(searchIndexReader.indexId());
		String indexId = searchIndexReader.indexId();
		String termString = term.termString();
		this.termString = termString;
		float weight = term.weight();
		Option option = term.option();
		CharVector fullTerm = new CharVector(termString);
		Analyzer analyzer = searchIndexReader.getQueryAnalyzerFromPool();

		IndexSetting indexSetting = searchIndexReader.indexSetting();
		if (highlightInfo != null) {
			String queryAnalyzerName = indexSetting.getQueryAnalyzer();
			for (RefSetting refSetting : indexSetting.getFieldList()) {
				highlightInfo.add(refSetting.getRef(), queryAnalyzerName, term.termString(), term.option().useHighlight());
			}
		}
		try {
			CharTermAttribute termAttribute = null;
			CharsRefTermAttribute refTermAttribute = null;
			PositionIncrementAttribute positionAttribute = null;
			StopwordAttribute stopwordAttribute = null;
			TypeAttribute typeAttribute = null;
			
//			SynonymAttribute synonymAttribute = null;
//			FeatureAttribute featureAttribute = null;
			
			TokenStream tokenStream = analyzer.tokenStream(indexId, fullTerm.getReader());
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
			
//			if (tokenStream.hasAttribute(SynonymAttribute.class)) {
//				synonymAttribute = tokenStream.getAttribute(SynonymAttribute.class);
//			}
//			if (tokenStream.hasAttribute(FeatureAttribute.class)) {
//				featureAttribute = tokenStream.getAttribute(FeatureAttribute.class);
//			}

			CharVector token = null;
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
				if (option.useStopword() && stopwordAttribute != null && stopwordAttribute.isStopword()) {
//					logger.debug("stopword");
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
//				logger.debug("token = {} : {}", token, queryPosition);

				SearchMethod searchMethod = searchIndexReader.createSearchMethod(new NormalSearchMethod());
				PostingReader postingReader = searchMethod.search(indexId, token, queryPosition, weight);

				OperatedClause clause = null;
				
//				if (featureAttribute != null && featureAttribute.type() == FeatureType.APPEND) {
//					//TODO append 한다.
//					clause = new TermOperatedClause(postingReader);
//					if(operatedClause != null){
//						clause = new AppendOperatedClause(operatedClause, clause);
//					}
//				}else{
					clause = new TermOperatedClause(indexId, postingReader);
//				}
				
				if (operatedClause == null) {
					operatedClause = clause;
				} else {
					if(term.type() == Type.ALL){
						operatedClause = new AndOperatedClause(operatedClause, clause);
					}else if(term.type() == Type.ANY){
						operatedClause = new OrOperatedClause(operatedClause, clause);
					}
				}


			}
		} catch (IOException e) {
			logger.error("", e);
		} finally {
			searchIndexReader.releaseQueryAnalyzerToPool(analyzer);
		}
	}

	@Override
	protected boolean nextDoc(RankInfo docInfo) {
		if (operatedClause == null) {
			return false;
		}
		return operatedClause.next(docInfo);
	}

	@Override
	public void close() {
		if (operatedClause != null) {
			operatedClause.close();
		}
	}
	@Override
	protected void initClause() {
		operatedClause.initClause();
	}
	
	@Override
	protected void initExplanation() {
		explanation.setTerm(termString);
	}

}
