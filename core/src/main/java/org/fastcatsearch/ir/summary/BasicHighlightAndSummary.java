package org.fastcatsearch.ir.summary;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.CharsRefTermAttribute;
import org.apache.lucene.analysis.tokenattributes.FeatureAttribute;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;
import org.apache.lucene.search.highlight.Formatter;
import org.apache.lucene.search.highlight.Fragmenter;
import org.apache.lucene.search.highlight.Highlighter;
import org.apache.lucene.search.highlight.InvalidTokenOffsetsException;
import org.apache.lucene.search.highlight.Scorer;
import org.apache.lucene.search.highlight.SimpleFragmenter;
import org.apache.lucene.search.highlight.SimpleHTMLFormatter;
import org.apache.lucene.search.highlight.WeightedTerm;
import org.apache.lucene.util.CharsRef;
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
		
		FeatureAttribute featureAttribute = null;
		
		if(tokenStream.hasAttribute(FeatureAttribute.class)) {
			featureAttribute = tokenStream.getAttribute(FeatureAttribute.class);
		}
		
		CharTermAttribute charTermAttribute = tokenStream.getAttribute(CharTermAttribute.class);
		OffsetAttribute offsetAttribute = tokenStream.getAttribute(OffsetAttribute.class);
		
		String prevTermString = null;
		while(tokenStream.incrementToken()) {
			
			String termString = new String(charTermAttribute.buffer(), 0, charTermAttribute.length());
			
			float score = 0f;
			
			if(featureAttribute!=null) {
				if(featureAttribute.type()==FeatureAttribute.FeatureType.MAIN) {
					logger.trace("MAIN WORD {}", termString);
					score = 3.0f;
				} else if(featureAttribute.type()==FeatureAttribute.FeatureType.ADDITION) {
					logger.trace("ADDITION WORD {}", termString);
					score = 1.0f;
				} else if(featureAttribute.type()==FeatureAttribute.FeatureType.APPEND) {
					logger.trace("APPEND WORD {}", termString);
					score = 0f;
				} else if(featureAttribute.type()==FeatureAttribute.FeatureType.NULL) {
					logger.trace("NULL {}", termString);
					score = 0f;
				}
			}
			
			if(score > 0) {
				if(!termString.equals(prevTermString)){
					terms.add(new WeightedTerm(score, termString));
					logger.trace("++ charTermAttribute : {}", termString);
				}
				prevTermString = termString;
				
				//refterm 은 1글자 이하의 텀에서는 의미가 없음
				if(termString.length() > 1) {
					if(logger.isTraceEnabled()) {
						logger.trace("charTermAttribute : {} : [{}/{}]", termString, 
								offsetAttribute.startOffset(), termAttribute.charsRef().length());
					}
					
					if (termAttribute != null) {
						CharsRef charRef = termAttribute.charsRef();
						if (charRef!=null && !(charRef.offset > 0 && charRef.length() == 1)) {
							terms.add(new WeightedTerm(score, charRef.toString()));
							logger.trace("++ charRefTerm : {}", termString);
						}
					}
				}
			}
		}
		
		WeightedTerm[] weightedTerms = new WeightedTerm[terms.size()];
		weightedTerms = terms.toArray(weightedTerms);
		
		Scorer scorer = new TokenizedTermScorer(weightedTerms);
		Highlighter highlighter = new Highlighter(formatter, scorer);
		Fragmenter fragmenter = new SimpleFragmenter(len);
		highlighter.setTextFragmenter(fragmenter);
		
		tokenStream = //analyzer.tokenStream("", new StringReader(pText));
				new WrappedTokenStream(analyzer.tokenStream("", new StringReader(pText)), pText);
		
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
			if (len > pText.length()) {
				len = pText.length();
			}
			text = pText.substring(0, len);
		}
		return text;
	}
	
	class WrappedTokenStream extends TokenStream {
		
		private String pText;
		
		private TokenStream tokenStream;
		private OffsetAttribute offsetAttribute;
		private CharTermAttribute charTermAttribute;
		private CharsRefTermAttribute charsRefTermAttribute;
		
		private OffsetAttribute offsetAttributeLocal;
		private CharTermAttribute charTermAttributeLocal;
		private CharsRefTermAttribute charsRefTermAttributeLocal;

		public WrappedTokenStream(TokenStream tokenStream, String pText) {
			this.pText = pText;
			this.tokenStream = tokenStream;
			offsetAttribute = tokenStream.getAttribute(OffsetAttribute.class);
			charTermAttribute = tokenStream.getAttribute(CharTermAttribute.class);
			if(tokenStream.hasAttribute(CharsRefTermAttribute.class)) {
				charsRefTermAttribute = tokenStream.getAttribute(CharsRefTermAttribute.class);
			}
			
			offsetAttributeLocal = this.addAttribute(OffsetAttribute.class);
			charTermAttributeLocal = this.addAttribute(CharTermAttribute.class);
			charsRefTermAttributeLocal = this.addAttribute(CharsRefTermAttribute.class);
		}
		@Override
		public void end() throws IOException { tokenStream.end(); }
		@Override
		public void reset() throws IOException { tokenStream.reset(); }
		@Override
		public void close() throws IOException { tokenStream.close(); }
		@Override
		public boolean incrementToken() throws IOException {
			boolean ret = tokenStream.incrementToken();
	
			char[] buffer;
			int offset;
			int length;
			
			if(charTermAttributeLocal.buffer().length < charTermAttribute.buffer().length ) {
				charTermAttributeLocal.resizeBuffer(charTermAttribute.buffer().length);
			}
			buffer = charTermAttribute.buffer();
			length = charTermAttribute.length();
			charTermAttributeLocal.copyBuffer(buffer, 0, length);
			charTermAttributeLocal.setLength(length);
			
			if( charTermAttribute.length() > 0 
					&& charTermAttribute.buffer()[0] == 
						pText.charAt(offsetAttribute.startOffset())
					&& offsetAttribute.startOffset() + length <= pText.length()) {
				offsetAttributeLocal.setOffset(offsetAttribute.startOffset(),
						offsetAttribute.startOffset() + length);
			} else {
				offsetAttributeLocal.setOffset(offsetAttribute.startOffset(),
						offsetAttribute.endOffset());
			}
			
			if(charsRefTermAttribute!=null) {
				buffer = charsRefTermAttribute.charsRef().chars;
				offset = charsRefTermAttribute.charsRef().offset;
				length = charsRefTermAttribute.charsRef().length;
				charsRefTermAttributeLocal.setBuffer(buffer, offset, length);
			}
			return ret;
		}
		
	}
}
