package org.fastcatsearch.ir.analysis;

import org.fastcatsearch.ir.io.CharVector;


@TokenizerAttributes(name = "BiGram")
public class BiGramTokenizer extends NGramTokenizer {

	public BiGramTokenizer(){
		super(2);
	}

	@Override
        public void afterGeneratrNGram(CharVector token) {
	        // TODO Auto-generated method stub
	        // do Nothing 
        }
}
