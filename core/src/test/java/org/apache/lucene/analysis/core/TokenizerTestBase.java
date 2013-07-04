package org.apache.lucene.analysis.core;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.URL;

import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;
import org.apache.lucene.analysis.tokenattributes.TypeAttribute;
import org.apache.lucene.analysis.tokenattributes.TypeAttributeImpl;
import org.fastcatsearch.ir.common.IRException;

public class TokenizerTestBase {

	public void testTokenizerSpeed(Tokenizer tokenizer, boolean isDebug) throws IRException {
		URL url = getClass().getResource("/org/apache/lucene/analysis/korean_1000_text.txt");
		File file = new File(url.getFile());
		testTokenizer(tokenizer, file, isDebug);
	}

	public void testTokenizer(Tokenizer tokenizer, File file, boolean isDebug) throws IRException {

		int i = 0;
		long start = System.currentTimeMillis();
		long lap = start;

		int COUNT = 50;
		for (int k = 0; k < COUNT; k++) {
			InputStream is = null;
			try {
				is = new FileInputStream(file);
				BufferedReader dr = new BufferedReader(new InputStreamReader(is, "utf-8"));

				String line = null;

				CharTermAttribute termAttribute = tokenizer.getAttribute(CharTermAttribute.class);
				OffsetAttribute offsetAtt = tokenizer.getAttribute(OffsetAttribute.class);
				TypeAttribute typeAtt = null;
				try{
					typeAtt = tokenizer.getAttribute(TypeAttribute.class);
				}catch(Exception e){
					typeAtt = new TypeAttributeImpl();
				}

				while ((line = dr.readLine()) != null) {
					line = line.trim();
					if (line.length() == 0)
						continue;
					tokenizer.setReader(new StringReader(line));
					tokenizer.reset();
					if (isDebug) {
						System.out.println(">>>>" + line);
					}
					while (tokenizer.incrementToken()) {
						if (isDebug) {
							String str = termAttribute.toString();
							System.out.println(str + " " + typeAtt.type() + " [ " + offsetAtt.startOffset() + " ~ " + offsetAtt.endOffset() + " ]");
						}
					}
					i++;
					if ((i % 10000) == 0) {
						System.out.println(i + " th " + (System.currentTimeMillis() - lap) + "ms");
						lap = System.currentTimeMillis();
					}

				}

			} catch (Exception ignore) {
				ignore.printStackTrace();
				return;
			} finally {
				if (is != null) {
					try {
						is.close();
					} catch (IOException ignore) {
					}
				}
			}
		}

		long DTime = System.currentTimeMillis() - start;
		double lps = i / DTime * 1000;
		System.out.println("DONE " + i + " lines time = " + DTime + "ms, lps=" + lps);
		double mbps = file.length() * COUNT / (DTime / 1000.0) / 1024 / 1024;
		System.out.println("LineByLine index Extraction Speed : " + mbps + "MBp/s");
	}
}
