package org.fastcatsearch.ir.summary;

import java.io.StringReader;
import java.util.List;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.search.highlight.Formatter;
import org.apache.lucene.search.highlight.Fragmenter;
import org.apache.lucene.search.highlight.Highlighter;
import org.apache.lucene.search.highlight.Scorer;
import org.apache.lucene.search.highlight.SimpleFragmenter;
import org.apache.lucene.search.highlight.SimpleHTMLFormatter;
import org.apache.lucene.search.highlight.WeightedTerm;
import org.fastcatsearch.ir.config.FieldSetting;
import org.fastcatsearch.ir.query.HighlightInfo;
import org.fastcatsearch.ir.search.HighlightAndSummary;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BasicHighlightAndSummary implements HighlightAndSummary {
	
	public static final int DEFAULT_MAX_FRAGMENTS = 2;
	
	private static final Logger logger = LoggerFactory.getLogger(BasicHighlightAndSummary.class);
	
	private static final String fragmentSeparator = "...";
	
    public static String highlight(Analyzer analyzer, String pText, List<String>pQuery, String preTag, 
    		String postTag, int len, int maxFragments) throws Exception {
    	
    	Formatter formatter = new SimpleHTMLFormatter(preTag, postTag);
    	
    	if(maxFragments <= 0) {
    		maxFragments = 1;
    	}
    	
    	if(len<=0) {
    		len = pText.length() + 1;
    	}
    	
    	//len 을 maxFragments 로 나누어 주지 않으면 각 fragments 마다 len 의 길이를 갖게 된다.
    	len = len / maxFragments;
        
        WeightedTerm[] weightedTerms = new WeightedTerm[pQuery.size()];
        
        for(int inx=0;inx<pQuery.size(); inx++) {
        	weightedTerms[inx] = new WeightedTerm(1.0f, pQuery.get(inx));
        }
    	
    	Scorer scorer = null;
    	
    	TokenStream tokenStream = analyzer.tokenStream("", new StringReader(pText));
    	
    	scorer = new TokenizedTermScorer(weightedTerms);
        
        Highlighter highlighter = new Highlighter(formatter,scorer);
        
        Fragmenter fragmenter =new SimpleFragmenter(len);
        
        highlighter.setTextFragmenter(fragmenter);
        
        String text = highlighter.getBestFragments(tokenStream, pText, maxFragments, fragmentSeparator);
        if (text == null || "".equals(text)) {
	        if(len > pText.length()) {
	        	len = pText.length();
	        }
        	text = pText.substring(0,len);
        }
        return text;
    }

	@Override
	public char[] modify(HighlightInfo summaryInfo, char[] target, String[] highlightTags) {
		boolean useHighlight = summaryInfo.useHighlight();
		boolean useSummary = summaryInfo.useSummary();
		int summarySize = summaryInfo.summarySize();
		
		String summaryStr = "";
		
		if(useHighlight && highlightTags != null){
			try {
				FieldSetting fs = summaryInfo.getFieldSetting();
				if(fs!=null && fs.indexSetting !=null) {
					//Analyzer analyzer = fs.indexSetting.queryAnalyzerPool.getFromPool();
					Analyzer analyzer = fs.indexSetting.indexAnalyzerPool.getFromPool();
					if(analyzer!=null) {
						return highlight(analyzer,new String(target), summaryInfo.termList(), 
							highlightTags[0], highlightTags[1],summarySize, DEFAULT_MAX_FRAGMENTS).toCharArray();
					}
					fs.indexSetting.indexAnalyzerPool.releaseToPool(analyzer);
				}

				return summaryStr.toCharArray();
			} catch (Exception e) {
				logger.error("",e);
			}
		}
		
		if(useSummary) {
			return new String(target,0,summarySize).toCharArray();
		}
		return target;
	}
}
