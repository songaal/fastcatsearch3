package org.fastcatsearch.ir.search.clause;

import java.io.IOException;
import java.io.PrintStream;
import java.io.StringWriter;
import java.io.Writer;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.core.AnalyzerOption;
import org.apache.lucene.analysis.tokenattributes.*;
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
import org.fastcatsearch.ir.util.CharVectorUtils;

public class BooleanClause extends OperatedClause {

    private String termString;
    private SearchIndexReader searchIndexReader;
    private OperatedClause operatedClause;
    private int weight;

    public BooleanClause(SearchIndexReader searchIndexReader, Term term, HighlightInfo highlightInfo) throws IOException {
        this(searchIndexReader, term, highlightInfo, null);
    }
    public BooleanClause(SearchIndexReader searchIndexReader, Term term, HighlightInfo highlightInfo, String requestTypeAttribute) throws IOException {
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
        if (highlightInfo != null && searchOption.useHighlight()) {
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
            analyzerOption.setForQuery();

            operatedClause = search(indexId, fullTerm, term.getProximity(), term.type(), indexSetting, analyzer, analyzerOption, requestTypeAttribute, term.isDisableAdditionalTerm());

//            StringWriter writer = new StringWriter();
//            printTrace(writer, 4, 0);
//            logger.debug(">>> {}", writer.toString());
        } catch (IOException e) {
            throw e;
        } finally {
            searchIndexReader.releaseQueryAnalyzerToPool(analyzer);
        }
    }

    private OperatedClause search(String indexId, CharVector fullTerm, int proximity, Type type, IndexSetting indexSetting,
                                  Analyzer analyzer, AnalyzerOption analyzerOption, String requestTypeAttribute, boolean disableaAditionalTerm) throws IOException {
        logger.debug("############ search Term > {}", fullTerm);
        OperatedClause operatedClause = null;
        OperatedClause finalClause = null;

        CharTermAttribute termAttribute = null;
        CharsRefTermAttribute refTermAttribute = null;
        PositionIncrementAttribute positionAttribute = null;
        StopwordAttribute stopwordAttribute = null;
        TypeAttribute typeAttribute = null;
        AdditionalTermAttribute additionalTermAttribute = null;

        SynonymAttribute synonymAttribute = null;
        OffsetAttribute offsetAttribute = null;

        TokenStream tokenStream = analyzer.tokenStream(indexId, fullTerm.getReader(), analyzerOption);
        tokenStream.reset();

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

        if (tokenStream.hasAttribute(OffsetAttribute.class)) {
            offsetAttribute = tokenStream.getAttribute(OffsetAttribute.class);
        }

        CharVector token = null;
        AtomicInteger termSequence = new AtomicInteger();

        int queryPosition = 0;

        Deque<OperatedClause> clauseDeque = new ArrayDeque<OperatedClause>();
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
                token = new CharVector(termAttribute.buffer(), 0, termAttribute.length(), indexSetting.isIgnoreCase());
            }

            queryPosition = positionAttribute != null ? positionAttribute.getPositionIncrement() : 0;
            logger.debug("token > {} queryPosition = {}, isIgnoreCase = {} analyzer= {}", token, queryPosition, token.isIgnoreCase(), analyzer.getClass().getSimpleName());
//			logger.debug("token = {} : {}", token, queryPosition);

            SearchMethod searchMethod = searchIndexReader.createSearchMethod(new NormalSearchMethod());
            PostingReader postingReader = searchMethod.search(indexId, token, queryPosition, weight);

            OperatedClause clause = new TermOperatedClause(indexId, token.toString(), postingReader, termSequence.getAndIncrement());
//			logger.debug("Term :{}", token.toString());
            // 유사어 처리
            // analyzerOption에 synonym확장여부가 들어가 있으므로, 여기서는 option을 확인하지 않고,
            // 있으면 그대로 색인하고 유사어가 없으면 색인되지 않는다.
            //
            //isSynonym 일 경우 다시한번 유사어확장을 하지 않는다.
            if(synonymAttribute != null) {
                logger.debug(">>>>>>>>>>>>> [Synonym] {}", synonymAttribute.getSynonyms());
                clause = applySynonym(clause, searchIndexReader, synonymAttribute, indexId, queryPosition, termSequence, type);
            }

            clauseDeque.addLast(clause);

            //추가 확장 단어들.
            if(!disableaAditionalTerm && additionalTermAttribute != null && additionalTermAttribute.size() > 0) {
                Iterator<String> termIter = additionalTermAttribute.iterateAdditionalTerms();
                OperatedClause additionalClause = null;
                while(termIter.hasNext()) {
                    CharVector localToken = new CharVector(termIter.next().toCharArray(), indexSetting.isIgnoreCase());
                    queryPosition = positionAttribute != null ? positionAttribute.getPositionIncrement() : 0;
                    searchMethod = searchIndexReader.createSearchMethod(new NormalSearchMethod());
                    postingReader = searchMethod.search(indexId, localToken, queryPosition, weight);
                    OperatedClause termClause = new TermOperatedClause(indexId, localToken.toString(), postingReader, termSequence.getAndIncrement());

                    //복합명사 타입. 예를들어 유아동->유아,아동 으로 분리될때 "유아" 와 "아동" 이 이곳으로 들어온다.
                    boolean isCompoundNoun = typeAttribute != null && typeAttribute.type().equals("<COMPOUND>");

                    //복합명사의 경우, 원래 단어의 start, length를 가지기 때문에 복합명사 개별의 start, length 정보는 없다.
                    //전제단어로 나올리도 없고, 판단할수도 없다.
                    if (!isCompoundNoun && (offsetAttribute.startOffset() == 0 &&
                            offsetAttribute.endOffset() == fullTerm.length())) {
                        // 풀텀에 대한 유사어 확장. 2018.7.2 swsong
                        // 풀텀이 additionalTermAttribute 로 들어오므로, 풀텀이 확인되면 유사어확장을 한다.
                        if(synonymAttribute != null && synonymAttribute.getSynonyms() != null && synonymAttribute.getSynonyms().size() > 0) {
                            logger.debug(">>>>>>>>>>>>> [Synonym] {}", synonymAttribute.getSynonyms());
                            termClause = applySynonym(termClause, searchIndexReader, synonymAttribute, indexId, queryPosition, termSequence, type);
                        }
                        finalClause = termClause;
                    } else {
                        //일반확장단어들
                        if (additionalClause == null) {
                            additionalClause = termClause;
                        } else {

                            /*
                             * 2017.11.11 swsong
                             * 복합명사의 경우 서로 and 로 연결해야 한다.
                             * */
                            if (isCompoundNoun) {
                                additionalClause = new AndOperatedClause(additionalClause, termClause);
                            } else {
                                additionalClause = new OrOperatedClause(additionalClause, termClause);
                            }
                        }
                }
                }

                /**
                 * swsong 2018.6.5  additionalClause 가 여러단어에 연결될수도 있으므로 deque 를 도입하여
                 * 엮어준다.
                 */
                int subSize = additionalTermAttribute.subSize();
                //2개 이상의 단어에 연결되었는지 확인.
                if(subSize > 1) {
                    OperatedClause c = null;
                    for (int i = 0; i < subSize; i++) {
                        if(c == null) {
                            //처음에는 그냥 지나감
                            c = clauseDeque.pollLast();
                        } else {
                            //앞에 하나더 빼내서 all, any 로 엮어준다음 다시 넣어준다.
                            OperatedClause b = clauseDeque.pollLast();
                            if(type == Type.ALL){
                                c = new AndOperatedClause(b, c, proximity);
                            }else if(type == Type.ANY){
                                c = new OrOperatedClause(b, c, proximity);
                            }
                        }
                    }
                    c = new OrOperatedClause(c, additionalClause);
                    clauseDeque.addLast(c);
                } else {
                    //원 단어에 or 로 연결.
                    OperatedClause c = clauseDeque.pollLast();
                    c = new OrOperatedClause(c, additionalClause);
                    clauseDeque.addLast(c);
                }
            }
        }

        /**
         * swsong 2018.6.1 예전에는 이 로직이 추가텀 보다 먼저 나왔으나 추가텀을 해당 단어에 먼저 적용하고 전체 clause 에 붙이도록 함.
         * 여기까지 왔다면 clauseDeque 에 op 들이 모두 들어있다.
         */
        while(clauseDeque.size() > 0) {
            if(operatedClause == null) {
                operatedClause = clauseDeque.poll();
            } else {
                if(type == Type.ALL){
                    operatedClause = new AndOperatedClause(operatedClause, clauseDeque.poll(), proximity);
                }else if(type == Type.ANY) {
                    operatedClause = new OrOperatedClause(operatedClause, clauseDeque.poll(), proximity);
                }
            }
        }

        if (finalClause != null) {
            operatedClause = new OrOperatedClause(operatedClause, finalClause);
        }
        if(logger.isTraceEnabled()) {
            logger.trace("clause:{}", dumpClause(operatedClause));
        }

        return operatedClause;
    }
    public static String dumpClause(OperatedClause clause) {
        if (clause == null)
            return null;

        StringBuilder sb = new StringBuilder();
        sb.append(clause.toString());
        sb.append("\n");
        OperatedClause[] children = clause.children();
        if(children!=null) {
            if(children.length == 2) {
                if(clause instanceof AndOperatedClause) {
                    sb.append("(");
                    if(children[0] instanceof TermOperatedClause) {
                        sb.append(children[0].term());
                    } else if(children[0].children()!=null) {
                        sb.append(dumpClause(children[0]));
                    }

                    if(children[1] != null && children[1] instanceof TermOperatedClause) {
                        sb.append(" and ");
                        sb.append(children[1].term());
                    } else if(children[1] != null && children[1].children()!=null) {
                        sb.append(" and ");
                        sb.append(dumpClause(children[1]));
                    }
                    sb.append(")");
                } else if(clause instanceof OrOperatedClause) {
                    sb.append("(");
                    if(children[0] instanceof TermOperatedClause) {
                        sb.append(children[0].term());
                    } else if(children[0].children()!=null) {
                        sb.append(dumpClause(children[0]));
                    }

                    if(children[1] != null && children[1] instanceof TermOperatedClause) {
                        sb.append(" or ");
                        sb.append(children[1].term());
                    } else if(children[1] != null && children[1].children()!=null) {
                        sb.append(" or ");
                        sb.append(dumpClause(children[1]));
                    }
                    sb.append(")");

                } else {
                    sb.append("(");
                    if(children[0] instanceof TermOperatedClause) {
                        sb.append(((TermOperatedClause)children[0]).term());
                    } else if(children[0].children()!=null) {
                        sb.append(dumpClause(children[0]));
                    }
                    sb.append(" ? ");
                    if(children[1] != null && children[1] instanceof TermOperatedClause) {
                        sb.append(((TermOperatedClause)children[1]).term());
                    } else if(children[1] != null && children[1].children()!=null) {
                        sb.append(dumpClause(children[1]));
                    }
                    sb.append(")");
                }
            } else if(children.length == 1) {

                if(clause instanceof TermOperatedClause) {
                    sb.append(((TermOperatedClause)clause).term());
                } else {
                    sb.append("[").append(dumpClause(children[0])).append("]");
                }
            }
        }

        return sb.toString();
    }
    @Override
    protected boolean nextDoc(RankInfo rankInfo) throws IOException {
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
    protected void initClause(boolean explain) throws IOException {
        if (operatedClause != null) {
            operatedClause.init(explanation != null ? explanation.createSubExplanation() : null);
        }
    }

    @Override
    public String term() {
        return termString;
    }
    @Override
    public void printTrace(Writer writer, int indent, int depth) throws IOException {
        String indentSpace = "";
        if(depth > 0){
            for (int i = 0; i < (depth - 1) * indent; i++) {
                indentSpace += " ";
            }

            for (int i = (depth - 1) * indent, p = 0; i < depth * indent; i++, p++) {
                if(p == 0){
                    indentSpace += "|";
                }else{
                    indentSpace += "-";
                }
            }
        }
        writer.append(indentSpace).append("[BOOLEAN]\n");
        operatedClause.printTrace(writer, indent, depth + 1);

    }


    private OperatedClause applySynonym(OperatedClause clause,
                                        SearchIndexReader searchIndexReader,
                                        SynonymAttribute synonymAttribute, String indexId, int queryPosition,
                                        AtomicInteger termSequence, Type type) throws IOException {

        @SuppressWarnings("unchecked")
        List<Object> synonymObj = synonymAttribute.getSynonyms();
        if(synonymObj != null) {
            OperatedClause synonymClause = null;
            for(Object obj : synonymObj) {
                if(obj instanceof CharVector) {
                    CharVector localToken = (CharVector)obj;
                    localToken.setIgnoreCase();
                    if(localToken.hasWhitespaces()) {
                        List<CharVector> synonyms = CharVectorUtils.splitByWhitespace(localToken);
                        OperatedClause extractedClause = null;
                        /*
                         * 유사어에 공백이 포함된 경우 여러단어로 나누어 AND 관계로 추가한다.
                         * '서울대 => 서울 대학교'
                         * 와 같은 관계가 해당된다.
                         */

                        for(CharVector synonym : synonyms) {
                            SearchMethod localSearchMethod = searchIndexReader.createSearchMethod(new NormalSearchMethod());
                            PostingReader localPostingReader = localSearchMethod.search(indexId, synonym, queryPosition, weight);
                            OperatedClause localClause = new TermOperatedClause(indexId, synonym.toString(), localPostingReader, termSequence.getAndIncrement());

                            if(extractedClause == null) {
                                extractedClause = localClause;
                            } else {
                                //공백구분 유사어는 AND 관계가 맞다. 15.12.24 swsong
                                extractedClause = new AndOperatedClause(extractedClause, localClause);
                            }
                        }
                        if(synonymClause == null) {
                            synonymClause = extractedClause;
                        } else {
                            synonymClause = new OrOperatedClause(synonymClause, extractedClause);
                        }
                    } else {
                        SearchMethod localSearchMethod = searchIndexReader.createSearchMethod(new NormalSearchMethod());
                        PostingReader localPostingReader = localSearchMethod.search(indexId, localToken, queryPosition, weight);
                        OperatedClause localClause = new TermOperatedClause(indexId, localToken.toString(), localPostingReader, termSequence.getAndIncrement());

                        if (synonymClause == null) {
                            synonymClause = localClause;
                        } else {
                            synonymClause = new OrOperatedClause(synonymClause, localClause);
                        }
                    }
                } else if(obj instanceof List) {
                    @SuppressWarnings("unchecked")
                    List<CharVector>synonyms = (List<CharVector>)obj;
                    OperatedClause extractedClause = null;
                    //유사어가 여러단어로 분석될경우
                    for(CharVector localToken : synonyms) {
                        localToken.setIgnoreCase();
                        SearchMethod localSearchMethod = searchIndexReader.createSearchMethod(new NormalSearchMethod());
                        PostingReader localPostingReader = localSearchMethod.search(indexId, localToken, queryPosition, weight);
                        OperatedClause localClause = new TermOperatedClause(indexId, localToken.toString(), localPostingReader, termSequence.getAndIncrement());

                        if(extractedClause == null) {
                            extractedClause = localClause;
                        } else {
                            //공백구분 유사어는 AND 관계가 맞다. 15.12.24 swsong
                            extractedClause = new AndOperatedClause(extractedClause, localClause);
                        }
                    }
                    if(synonymClause == null) {
                        synonymClause = extractedClause;
                    } else {
                        synonymClause = new OrOperatedClause(synonymClause, extractedClause);
                    }
                }
            }
            if(synonymClause != null) {
                int position = clause.getPosition();
                clause = new OrOperatedClause(clause, synonymClause);
                clause.setPosition(position);
            }
        }

        return clause;
    }
    @Override
    public OperatedClause[] children() {
        return new OperatedClause[] { operatedClause };
    }
}
