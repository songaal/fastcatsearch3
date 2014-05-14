package org.fastcatsearch.ir.summary;

import java.util.HashMap;
import java.util.HashSet;

import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.CharsRefTermAttribute;
import org.apache.lucene.search.highlight.Scorer;
import org.apache.lucene.search.highlight.TextFragment;
import org.apache.lucene.search.highlight.WeightedTerm;
import org.apache.lucene.util.CharsRef;
import org.fastcatsearch.ir.io.CharVector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TokenizedTermScorer implements Scorer {
	
	Logger logger = LoggerFactory.getLogger(TokenizedTermScorer.class);

	TextFragment currentTextFragment = null;
	HashSet<String> uniqueTermsInFragment;

	float totalScore = 0;
	float maxTermWeight = 0;
	private HashMap<CharVector,WeightedTerm> termsToFind;

	private CharTermAttribute termAtt;
	private CharsRefTermAttribute refTermAtt;

	public TokenizedTermScorer(WeightedTerm[] weightedTerms) {
		termsToFind = new HashMap<CharVector,WeightedTerm>();
		for (int i = 0; i < weightedTerms.length; i++) {
			WeightedTerm existingTerm = termsToFind
					.get(weightedTerms[i].getTerm());
			if ((existingTerm == null)
					|| (existingTerm.getWeight() < weightedTerms[i].getWeight())) {
				// if a term is defined more than once, always use the highest scoring
				// weight
				CharVector cv =  new CharVector(weightedTerms[i].getTerm());
				cv.setIgnoreCase();
				termsToFind.put(cv, weightedTerms[i]);
				maxTermWeight = Math.max(maxTermWeight, weightedTerms[i].getWeight());
			}
		}
	}

	/* (non-Javadoc)
	 * @see org.apache.lucene.search.highlight.Scorer#init(org.apache.lucene.analysis.TokenStream)
	 */
	@Override
	public TokenStream init(TokenStream tokenStream) {
		
		termAtt = tokenStream.addAttribute(CharTermAttribute.class);
		if(tokenStream.hasAttribute(CharsRefTermAttribute.class)) {
			refTermAtt = tokenStream.addAttribute(CharsRefTermAttribute.class);
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.apache.lucene.search.highlight.FragmentScorer#startFragment(org.apache
	 * .lucene.search.highlight.TextFragment)
	 */
	@Override
	public void startFragment(TextFragment newFragment) {
		uniqueTermsInFragment = new HashSet<String>();
		currentTextFragment = newFragment;
		totalScore = 0;

	}


	/* (non-Javadoc)
	 * @see org.apache.lucene.search.highlight.Scorer#getTokenScore()
	 */
	@Override
	public float getTokenScore() {
		
		CharVector termText = null;
		WeightedTerm queryTerm = null;
		
		termText = new CharVector(termAtt.toString());
		termText.setIgnoreCase();
		
		//logger.trace("termAtt : {} in {}",termText, uniqueTermsInFragment);
		
		queryTerm = termsToFind.get(termText);
		if (queryTerm != null) {
			logger.trace("matched termText {}",termText);
			totalScore += queryTerm.getWeight();
			//uniqueTermsInFragment.add(termText);
			return queryTerm.getWeight();
		}
		
		if (refTermAtt != null) {
			CharsRef charRef = refTermAtt.charsRef();
			if(charRef!=null) {
				
				CharVector cv = new CharVector(charRef.toString());
				cv.setIgnoreCase();
				queryTerm = termsToFind.get(cv);
				if (queryTerm != null && !(charRef.offset > 0 && charRef.length == 1)) {
					logger.trace("matched refTermText {}",termText);
					totalScore += queryTerm.getWeight();
					//uniqueTermsInFragment.add(termText);
					return queryTerm.getWeight();
				}
			}
		}
		
		return 0;
	}


	/* (non-Javadoc)
	 * @see org.apache.lucene.search.highlight.Scorer#getFragmentScore()
	 */
	@Override
	public float getFragmentScore() {
		return totalScore;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.apache.lucene.search.highlight.FragmentScorer#allFragmentsProcessed()
	 */
	public void allFragmentsProcessed() {
		// this class has no special operations to perform at end of processing
	}

	/**
	 * 
	 * @return The highest weighted term (useful for passing to GradientFormatter
	 *         to set top end of coloring scale.
	 */
	public float getMaxTermWeight() {
		return maxTermWeight;
	}
}
