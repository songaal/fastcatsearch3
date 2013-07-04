package org.fastcatsearch.ir.summary;

import java.io.IOException;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.search.highlight.Highlighter;
import org.apache.lucene.search.highlight.SimpleFragmenter;
import org.apache.lucene.search.highlight.SimpleHTMLFormatter;
import org.apache.lucene.search.highlight.WeightedTerm;
import org.fastcatsearch.ir.search.HighlightAndSummary;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BasicHighlightAndSummary implements HighlightAndSummary {

	private static final Logger logger = LoggerFactory.getLogger(BasicHighlightAndSummary.class);

	public String highlight(Analyzer analyzer, String pText, String pQuery, String[] tags, int len) throws IOException {

		try{
			if (len == 0) {
				len = pText.length() + 1;
			}
	
			String[] queryList = pQuery.split(" ");
			
			WeightedTerm[] weightedTerms = new WeightedTerm[queryList.length];
	
			for (int inx = 0; inx < queryList.length; inx++) {
				weightedTerms[inx] = new WeightedTerm(1.0f, queryList[inx]);
			}
	
			Highlighter highlighter = new Highlighter(new SimpleHTMLFormatter(tags[0], tags[1]), new TokenizedTermScorer(weightedTerms));
			highlighter.setTextFragmenter(new SimpleFragmenter(len));
			String text = highlighter.getBestFragment(analyzer, "", pText);
			if (text == null) {
				if (len > pText.length()) {
					len = pText.length();
				}
				text = pText.substring(0, len);
			}
			return text;
		}catch(Exception e){
			throw new IOException(e);
		}
	}

	
}
