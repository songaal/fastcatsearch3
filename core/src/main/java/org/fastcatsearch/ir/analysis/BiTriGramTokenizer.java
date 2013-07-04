package org.fastcatsearch.ir.analysis;

import org.fastcatsearch.ir.io.CharVector;

@TokenizerAttributes(name = "BiTriGram")
public class BiTriGramTokenizer extends NGramTokenizer {
	private int additinalTermLen =3 ;

	public BiTriGramTokenizer() {
		super(2);
	}

	@Override
        public void afterGeneratrNGram(CharVector token) {
	        // TODO Auto-generated method stub
		if (token.length > additinalTermLen) {
//			System.out.println(token.toString());
			int tLen = token.length -2;
			for (int i = 0; i < tLen; i++) {
				CharVector ch = new CharVector();
				ch.start = token.start + i;
				ch.length = additinalTermLen;
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
        }
	
	
}
