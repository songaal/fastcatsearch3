package org.apache.lucene.search.highlight;

import java.io.IOException;
import java.io.Reader;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.CharsRefTermAttribute;
import org.fastcatsearch.ir.summary.TokenizedTermScorer;
import org.junit.Test;


public class FragmentHighlighterTest {

	@Test
	public void testHighlighter() throws IOException, InvalidTokenOffsetsException {
		
		int idx = 0;
		WeightedTerm[] weightedTerms = new WeightedTerm[4];
		weightedTerms[idx++] = new WeightedTerm(3.0f, "아버지");
		weightedTerms[idx++] = new WeightedTerm(3.0f, "가방");
		weightedTerms[idx++] = new WeightedTerm(2.0f, "들어");
		weightedTerms[idx++] = new WeightedTerm(2.0f, "가신다");
		
		String[] preAnalyzedTermList = new String[]{"아버지", "가방", "에", "빨리", "들어", "가신다"};
		MockAnalyzer analyzer = new MockAnalyzer(preAnalyzedTermList);
		
		
		int len = 300;
		Highlighter highlighter = new Highlighter(new TokenizedTermScorer(weightedTerms));
        highlighter.setTextFragmenter(new SimpleFragmenter(len));
        String text = highlighter.getBestFragment(analyzer, "fieldName", "text");
        if (text == null) {
//        	text = pText.substring(0,len);
        }
        
        System.out.println(text);
	}

	@Test
	public void testMockAnalyzer() throws IOException{
		
		String[] preAnalyzedTermList = new String[]{"가나", "다라", "마바", "사아"};
		MockAnalyzer mockAnalyzer = new MockAnalyzer(preAnalyzedTermList);
		TokenStream tokenStream = mockAnalyzer.tokenStream(null, null);
		CharTermAttribute termAttribute =  tokenStream.getAttribute(CharTermAttribute.class);
		while(tokenStream.incrementToken()){
			System.out.println(termAttribute.toString());
		}
	}
	
	class MockAnalyzer extends Analyzer {
		String[] preAnalyzedTermList;
		
		public MockAnalyzer(String... preAnalyzedTermList){
			this.preAnalyzedTermList = preAnalyzedTermList;
			
		}
		
		@Override
		protected TokenStreamComponents createComponents(String fieldName, Reader reader) {
			//공백으로 분리하여 입력된다.
			final MockTokenizer tokenizer = new MockTokenizer(preAnalyzedTermList);
			try {
				tokenizer.reset();
			} catch (IOException e) {
			}
			
			return new TokenStreamComponents(tokenizer);
		}
		
	}
	
	class MockTokenizer extends Tokenizer {
		String[] preAnalyzedTermList;
		CharTermAttribute termAttribute;
		CharsRefTermAttribute refTermAttribute;
		int pos;
		
		public MockTokenizer(String[] preAnalyzedTermList) {
			super(null);
			this.preAnalyzedTermList = preAnalyzedTermList;
			termAttribute = addAttribute(CharTermAttribute.class);
			refTermAttribute = addAttribute(CharsRefTermAttribute.class);
		}

		@Override
		public boolean incrementToken() throws IOException {
			if(pos < preAnalyzedTermList.length){
				termAttribute.setEmpty();
				termAttribute.append(preAnalyzedTermList[pos]);
				refTermAttribute.setBuffer(preAnalyzedTermList[pos].toCharArray(), 0, preAnalyzedTermList[pos].length());
				pos++;
				return true;
			}
			return false;
		}
	}
	
	
}
