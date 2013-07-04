package org.fastcatsearch.ir.analysis;

import java.util.Vector;

import org.fastcatsearch.ir.io.CharVector;

public abstract class NGramTokenizer extends Tokenizer {

	private TypeTokenizer typeTokenizer;
	private boolean hasQue;
	protected Vector<CharVector> que;
	private final int TERM_LEN;
	protected int seq;

	public NGramTokenizer(int n){
		TERM_LEN = n;
	}
	
	public abstract void afterGeneratrNGram(CharVector token);
	
	protected void generateNGram(CharVector token) {
		int i = 0;

		que.clear();
		if (token.length > TERM_LEN) {
			int tLen = token.length -1;
			for (i = 0; i < tLen; i++) {
				CharVector ch = new CharVector();
				ch.start = token.start + i;
				ch.length = TERM_LEN;
				ch.array = token.array;
				que.add(ch);
			}
		}
		else
		{
			CharVector ch = new CharVector();
			ch.start = token.start;
			ch.length = token.length;
			ch.array = token.array;
			que.add(ch);
		}
		
		afterGeneratrNGram(token);
		
		if ( que.size() > 0 )		
			hasQue = true;
	}

	@Override
	protected void init() {
		typeTokenizer = new TypeTokenizer();
		que = new Vector<CharVector>();
		hasQue = false;
		typeTokenizer.setInput(input);
	}

	@Override
	public boolean nextToken(CharVector token) {
		boolean result = false;

		if (hasQue == true) {
			if (que.size() == 0) {
				result = typeTokenizer.nextToken(token);
				seq = typeTokenizer.seq;
				if (result == false)
					return false;
				else
					{
					generateNGram(token);
					CharVector cv = que.get(0);
					token.array = cv.array;
					token.start = cv.start;
					token.length = cv.length;
					que.remove(0);
					
					if ( que.size() == 0 )
						hasQue = false;
					
					return true;
					}
			} else {
				CharVector cv = que.get(0);
				token.array = cv.array;
				token.start = cv.start;
				token.length = cv.length;
				que.remove(0);
				
				if ( que.size() == 0 )
					hasQue = false;
				
				return true;
			}
		} else {
			result = typeTokenizer.nextToken(token);
			seq = typeTokenizer.seq;
			if (result == false)
				return false;

			if (token.length <= TERM_LEN)
				return true;

			generateNGram(token);
			CharVector cv = que.get(0);
			token.array = cv.array;
			token.start = cv.start;
			token.length = cv.length;
			que.remove(0);
			
			if ( que.size() == 0 )
				hasQue = false;
			
			return true;
		}
	}

	@Override
	public boolean nextToken(CharVector token, int[] seq) {
		boolean bResult = nextToken(token);
		seq[0] = typeTokenizer.seq;
		return bResult;
	}


}
