package org.fastcatsearch.ir.dictionary;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Map;

import org.junit.Assert;

import org.fastcatsearch.ir.io.CharVector;
import org.junit.Test;

public class DictionaryTest {

	@Test
	public void testSynonym() throws IOException {
		ListMapDictionary dictionary = new ListMapDictionary();
		dictionary.addEntry("마우스,mouse,로지텍");
		dictionary.addEntry("모니터,엘지모니터,monitor,광시야각");
		
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		dictionary.writeTo(out);
		out.close();
		CharVector[] result = dictionary.getMap().get(new CharVector("엘지모니터"));
		System.out.println(result[0]+","+result[1]);
		
		byte[] buffer = out.toByteArray();
		
		//다시 읽고.
		ByteArrayInputStream bais = new ByteArrayInputStream(buffer);
		ListMapDictionary dictionary2 = new ListMapDictionary(bais);
		bais.close();
		CharVector[] result2 = dictionary2.getMap().get(new CharVector("엘지모니터"));
		System.out.println(result2[0]+","+result2[1]);
		//다시 쓰고.
		ByteArrayOutputStream out2 = new ByteArrayOutputStream();
		dictionary2.writeTo(out2);
		out2.close();
		
		//다시 읽고.
		ByteArrayInputStream bais2 = new ByteArrayInputStream(buffer);
		ListMapDictionary dictionary3 = new ListMapDictionary(bais2);
		bais2.close();
		CharVector[] result3 = dictionary3.getMap().get(new CharVector("엘지모니터"));
		System.out.println(result3[0]+","+result3[1]);
		
		
		byte[] buffer2 = out2.toByteArray();
		
		Assert.assertEquals(buffer.length, buffer2.length);
		for (int i = 0; i < buffer2.length; i++) {
			System.out.println(buffer[i]+":"+buffer2[i]);
			if(buffer[i] != buffer2[i]){
				System.out.println(">>>>>>>>>>>>>>>>");
			}
		}
//		Assert.assertArrayEquals(buffer, buffer2);
	}

	@Test
	public void testHashSet() throws IOException {
		
		HashSetDictionary dictionary = new HashSetDictionary();
		
		String[] terms = new String[] { "삼성", "LG", "애플" };
		
		for(String term : terms) {
			dictionary.addEntry(term);
		}
		
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		dictionary.writeTo(out);
		out.close();
		
		for(String term : terms) {
			boolean contains = dictionary.getSet().contains(new CharVector(term));
			System.out.println("is set has term "+term+" ? "+contains);
		}
		
		byte[] buffer = out.toByteArray();
		
		//다시 읽고.
		ByteArrayInputStream bais = new ByteArrayInputStream(buffer);
		HashSetDictionary dictionary2 = new HashSetDictionary(bais);
		bais.close();
		
		for(String term : terms) {
			boolean contains = dictionary2.getSet().contains(new CharVector(term));
			System.out.println("is set2 has term "+term+" ? "+contains);
		}
		
		//다시 쓰고.
		ByteArrayOutputStream out2 = new ByteArrayOutputStream();
		dictionary2.writeTo(out2);
		out2.close();
		
		//다시 읽고.
		ByteArrayInputStream bais2 = new ByteArrayInputStream(buffer);
		HashSetDictionary dictionary3 = new HashSetDictionary(bais2);
		bais2.close();
		
		for(String term : terms) {
			boolean contains = dictionary3.getSet().contains(new CharVector(term));
			System.out.println("is set3 has term "+term+" ? "+contains);
		}
		
		
		byte[] buffer2 = out2.toByteArray();
		
		Assert.assertEquals(buffer.length, buffer2.length);
		for (int i = 0; i < buffer2.length; i++) {
			System.out.println(buffer[i]+":"+buffer2[i]);
			if(buffer[i] != buffer2[i]){
				System.out.println(">>>>>>>>>>>>>>>>");
			}
		}
//		Assert.assertArrayEquals(buffer, buffer2);
	}

	@Test
	public void testTagProbDictionary() throws IOException {
		
		HashSetDictionary dictionary = new HashSetDictionary();
		
		String[] terms = new String[] { "삼성", "LG", "애플" };
		
		for(String term : terms) {
			dictionary.addEntry(term);
		}
		
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		dictionary.writeTo(out);
		out.close();
		
		for(String term : terms) {
			boolean contains = dictionary.getSet().contains(new CharVector(term));
			System.out.println("is set has term "+term+" ? "+contains);
		}
		
		byte[] buffer = out.toByteArray();
		
		//다시 읽고.
		ByteArrayInputStream bais = new ByteArrayInputStream(buffer);
		HashSetDictionary dictionary2 = new HashSetDictionary(bais);
		bais.close();
		
		for(String term : terms) {
			boolean contains = dictionary2.getSet().contains(new CharVector(term));
			System.out.println("is set2 has term "+term+" ? "+contains);
		}
		
		//다시 쓰고.
		ByteArrayOutputStream out2 = new ByteArrayOutputStream();
		dictionary2.writeTo(out2);
		out2.close();
		
		//다시 읽고.
		ByteArrayInputStream bais2 = new ByteArrayInputStream(buffer);
		HashSetDictionary dictionary3 = new HashSetDictionary(bais2);
		bais2.close();
		
		for(String term : terms) {
			boolean contains = dictionary3.getSet().contains(new CharVector(term));
			System.out.println("is set3 has term "+term+" ? "+contains);
		}
		
		
		byte[] buffer2 = out2.toByteArray();
		
		Assert.assertEquals(buffer.length, buffer2.length);
		for (int i = 0; i < buffer2.length; i++) {
			System.out.println(buffer[i]+":"+buffer2[i]);
			if(buffer[i] != buffer2[i]){
				System.out.println(">>>>>>>>>>>>>>>>");
			}
		}
//		Assert.assertArrayEquals(buffer, buffer2);
	}
}
