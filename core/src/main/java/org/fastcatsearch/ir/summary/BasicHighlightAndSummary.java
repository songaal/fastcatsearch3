package org.fastcatsearch.ir.summary;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.CharsRefTermAttribute;
import org.apache.lucene.search.highlight.Formatter;
import org.apache.lucene.search.highlight.Fragmenter;
import org.apache.lucene.search.highlight.Highlighter;
import org.apache.lucene.search.highlight.InvalidTokenOffsetsException;
import org.apache.lucene.search.highlight.Scorer;
import org.apache.lucene.search.highlight.SimpleFragmenter;
import org.apache.lucene.search.highlight.SimpleHTMLFormatter;
import org.apache.lucene.search.highlight.WeightedTerm;
import org.apache.lucene.util.Attribute;
import org.apache.lucene.util.CharsRef;
import org.fastcatsearch.ir.io.CharVector;
import org.fastcatsearch.ir.search.HighlightAndSummary;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BasicHighlightAndSummary implements HighlightAndSummary {

	private static final Logger logger = LoggerFactory.getLogger(BasicHighlightAndSummary.class);
	
	private static final String FRAGMENT_SEPARATOR = "...";

	@Override
	public String highlight(Analyzer analyzer, String pText, String query, 
			String[] tags, int len, int maxFragments) throws IOException {

		//
		// initialize tags ( if null or blank )
		//
		if(tags==null) {
			tags = new String[]{"",""};
		}
		
		for(int inx=0;inx<tags.length;inx++) {
			if(tags[inx]==null) { tags[inx]=""; }
		}

		//
		// minimum count of fragments
		//
    	if(maxFragments <= 0) {
    		maxFragments = 1;
    	}

    	//
    	// one full length of summary 
    	//
    	if(len<=0) {
    		len = pText.length() + 1;
    	}
    	
    	//
    	// lucene summary length is size of each fragment, so divide it by count of fragments
    	//
    	len = len / maxFragments;
		
    	TokenStream tokenStream = null;
    	
    	
    	
    	//TODO 스니펫만 만들고 하이라이팅을 하지 않는필드에 대해서는 DummyFormatter를 만들어서 넣어준다.
    	
    	
    	Formatter formatter = new SimpleHTMLFormatter(tags[0], tags[1]);
    	
		//
		// tokenize query and make weighted terms
		//
    	List<WeightedTerm> terms = new ArrayList<WeightedTerm>();
    	tokenStream = analyzer.tokenStream("", new StringReader(query));
    	
    	CharsRefTermAttribute termAttribute = null;
    	if(tokenStream.hasAttribute(CharsRefTermAttribute.class)){
    		termAttribute = tokenStream.getAttribute(CharsRefTermAttribute.class);
    	}
    	
    	CharTermAttribute charTermAttribute = tokenStream.getAttribute(CharTermAttribute.class);
		while(tokenStream.incrementToken()) {
			//
			// TODO:
			// 가중치 부여 : 현재 1.0f 로 기본가중치를 주고 있다.
			// 이 부분에서 가중치 를 많이 주면 해당 단어로 요약의 중심이 
			// 이동되므로 명사일 경우 가중치를 더 주는 방식을 고려해 본다.
			//
			String termString = null;
			if (termAttribute != null) {
				//FIXME:termString이 공백일 수 있는지 확인 필요
				CharsRef charRef = termAttribute.charsRef();
				if(charRef!=null) {
					termString = charRef.toString();
				} else {
					termString = "";
				}
			} else {
				termString = new String(charTermAttribute.buffer(), 0, charTermAttribute.length());
			}
//			logger.debug("HL >> {}", termString);
			terms.add(new WeightedTerm(1.0f, termString));
		}
		
		WeightedTerm[] weightedTerms = new WeightedTerm[terms.size()];
		weightedTerms = terms.toArray(weightedTerms);
		
		Scorer scorer = new TokenizedTermScorer(weightedTerms);
        Highlighter highlighter = new Highlighter(formatter,scorer);
        Fragmenter fragmenter = new SimpleFragmenter(len);
        highlighter.setTextFragmenter(fragmenter);
		
		tokenStream = analyzer.tokenStream("", new StringReader(pText));
		
        String text = pText;
        		
        try {
			text = highlighter.getBestFragments(tokenStream, pText, maxFragments, FRAGMENT_SEPARATOR);
		} catch (InvalidTokenOffsetsException e) {
			logger.error("",e);
		}

        //
        // return original text when if not summarized 
        //
        if (text == null || "".equals(text)) {
	        if(len > pText.length()) {
	        	len = pText.length();
	        }
        	text = pText.substring(0,len);
        }
        return text;
	}

}
