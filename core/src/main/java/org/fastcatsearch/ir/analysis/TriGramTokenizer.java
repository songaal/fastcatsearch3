package org.fastcatsearch.ir.analysis;

import org.fastcatsearch.ir.io.CharVector;

@TokenizerAttributes(name = "TriGram")
public class TriGramTokenizer extends NGramTokenizer {
	public TriGramTokenizer()
	{
		super(3);
	}

	@Override
        public void afterGeneratrNGram(CharVector token) {
	        // TODO Auto-generated method stub
		 // do Nothing  
        }
}
