package org.fastcatsearch.ir.analysis;

import java.io.IOException;
import java.io.Reader;

import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.tokenattributes.CharsRefTermAttribute;

public class NGramWordTokenizer extends Tokenizer {
	private int minGram;
	private int maxGram;

	private char[] charBuffer;
	private char[] readBuffer;
	private int length;

	private int nGram;
	private int pos;
	private final CharsRefTermAttribute termAttribute = addAttribute(CharsRefTermAttribute.class);

	public NGramWordTokenizer(Reader input) {
		this(input, 1, 3);
	}

	public NGramWordTokenizer(Reader input, int minGram, int maxGram) {
		super(input);
		this.minGram = minGram;
		this.maxGram = maxGram;
		readBuffer = new char[1024];
		nGram = minGram;
		
		try {
			fill();
		} catch (IOException e) {
			logger.error("", e);
		}
	}
	
	private void fill() throws IOException {
		charBuffer = null;
		int n = 0;

		while ((n = input.read(readBuffer)) != -1) {
			if (n > 0) {
				if (charBuffer == null) {
					charBuffer = new char[n];
					System.arraycopy(readBuffer, 0, charBuffer, 0, n);
				} else {
					char[] newCharBuffer = new char[charBuffer.length + n];
					System.arraycopy(charBuffer, 0, newCharBuffer, 0, charBuffer.length);
					System.arraycopy(readBuffer, 0, newCharBuffer, charBuffer.length, n);
					charBuffer = newCharBuffer;
				}
			}
		}

		
		length = 0;
		if(charBuffer != null) {
			for (int i = 0; i < charBuffer.length; i++) {
				if (charBuffer[i] <= 32) {
					continue;
				}
	
				charBuffer[length++] = (char) charBuffer[i];
			}
		}
		
		pos = 0;
		nGram = minGram;
	}
	
	@Override
	public void setReader(Reader input) throws IOException {
		super.setReader(input);
		
		charBuffer = null;
		int n = 0;

		while ((n = input.read(readBuffer)) != -1) {
			if (n > 0) {
				if (charBuffer == null) {
					charBuffer = new char[n];
					System.arraycopy(readBuffer, 0, charBuffer, 0, n);
				} else {
					char[] newCharBuffer = new char[charBuffer.length + n];
					System.arraycopy(charBuffer, 0, newCharBuffer, 0, charBuffer.length);
					System.arraycopy(readBuffer, 0, newCharBuffer, charBuffer.length, n);
					charBuffer = newCharBuffer;
				}
			}
		}

		
		length = 0;
		if(charBuffer != null) {
			for (int i = 0; i < charBuffer.length; i++) {
				if (charBuffer[i] <= 32) {
					continue;
				}
	
				charBuffer[length++] = (char) charBuffer[i];
			}
		}
		
		pos = 0;
		nGram = minGram;
		
//		System.out.println("@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@");
//		for(StackTraceElement e : Thread.currentThread().getStackTrace()){
//			System.out.println(e.toString());
//		}
//		System.out.println("@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@");
	}
	
	@Override
	public boolean incrementToken() throws IOException {
		while (pos < length) {

			while (nGram <= maxGram) {

				if (pos + nGram <= length) {
					// System.out.println(new String(charBuffer, pos, nGram));
					termAttribute.setBuffer(charBuffer, pos, nGram);
					if (nGram == maxGram) {
						nGram = minGram;
						pos++;
					} else {
						nGram++;
					}
					return true;
				}

				if (nGram == maxGram) {
					nGram = minGram;
					pos++;
					break;
					// 다음 pos로 옮겨서 minGram부터 분석시작.
				} else {
					nGram++;
				}

			}
		}

		return false;
	}

}
