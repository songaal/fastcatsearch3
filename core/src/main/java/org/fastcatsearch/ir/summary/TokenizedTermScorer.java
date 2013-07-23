package org.fastcatsearch.ir.summary;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;

import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.CharsRefTermAttribute;
import org.apache.lucene.search.highlight.Scorer;
import org.apache.lucene.search.highlight.TextFragment;
import org.apache.lucene.search.highlight.WeightedTerm;

public class TokenizedTermScorer implements Scorer {

	TextFragment currentTextFragment = null;
	HashSet<String> uniqueTermsInFragment;

	float totalScore = 0;
	float maxTermWeight = 0;
	private HashMap<String,WeightedTerm> termsToFind;

	private CharTermAttribute termAtt;

	public TokenizedTermScorer(WeightedTerm[] weightedTerms) {
		termsToFind = new HashMap<String,WeightedTerm>();
		for (int i = 0; i < weightedTerms.length; i++) {
			WeightedTerm existingTerm = termsToFind
					.get(weightedTerms[i].getTerm());
			if ((existingTerm == null)
					|| (existingTerm.getWeight() < weightedTerms[i].getWeight())) {
				// if a term is defined more than once, always use the highest scoring
				// weight
				termsToFind.put(weightedTerms[i].getTerm(), weightedTerms[i]);
				maxTermWeight = Math.max(maxTermWeight, weightedTerms[i].getWeight());
			}
		}
	}

	/* (non-Javadoc)
	 * @see org.apache.lucene.search.highlight.Scorer#init(org.apache.lucene.analysis.TokenStream)
	 */
	@Override
	public TokenStream init(TokenStream tokenStream) {
		
		//
		//TODO CharsRefTermAttribute가 없는 tokenstream은 CharsTermAttribute을 사용하도록 해주어야한다.
		//
		//
		//
		termAtt = tokenStream.addAttribute(CharTermAttribute.class);
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
		String termText = termAtt.toString();
		WeightedTerm queryTerm = termsToFind.get(termText);
		if (queryTerm == null) {
			// not a query term - return
			return 0;
		}
		// found a query term - is it unique in this doc?
		if (!uniqueTermsInFragment.contains(termText)) {
			totalScore += queryTerm.getWeight();
			uniqueTermsInFragment.add(termText);
		}
		return queryTerm.getWeight();
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