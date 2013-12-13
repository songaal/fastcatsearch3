package org.fastcatsearch.ir.dictionary;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.fastcatsearch.ir.io.CharVector;
import org.junit.Assert;
import org.junit.Test;

public class DictionaryTest {
	@Test
	public void testSynonymDictionary() throws IOException {
		SynonymDictionary dictionary = new SynonymDictionary();
		dictionary.addEntry("마우스", new String[] { "mouse, 로지텍" }, true, new boolean[] { true });
		dictionary.addEntry(null, new String[] { "엘지모니터, monitor, 광시야각, 마우스" }, true, new boolean[] { true });
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		dictionary.writeTo(baos);
		
		byte[] data = baos.toByteArray();
	
		ByteArrayInputStream bais = new ByteArrayInputStream(data);
		SynonymDictionary dictionary2 = new SynonymDictionary();
		dictionary2.readFrom(bais);
		System.out.println("---- synonym map---");
		Map<CharVector, CharVector[]> map = dictionary2.map();
		for(Entry<CharVector, CharVector[]> entry : map.entrySet()){
			System.out.println(entry.getKey() + ": " +join(entry.getValue()));
		}
		
		System.out.println("---- word set---");
		for(CharVector cv : dictionary2.getWordSet()){
			System.out.println(cv);
		}
	}
	
	
	private String join(CharVector[] list){
		String result = "";
		for(int i=0;i<list.length; i++){
			result += list[i].toString();
			if(i < list.length - 1){
				result += ", ";
			}
		}
		return result;
	}
	@Test
	public void testMapDictionary() throws IOException {
		MapDictionary dictionary = new MapDictionary();
		dictionary.addEntry("마우스", new String[] { "mouse, 로지텍" }, true, new boolean[] { true });
		dictionary.addEntry("모니터", new String[] { "엘지모니터, monitor, 광시야각" }, true, new boolean[] { true });
		
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		dictionary.writeTo(out);
		out.close();
		CharVector[] result = dictionary.getMap().get(new CharVector("엘지모니터"));
		System.out.println(result[0]+","+result[1]);
		
		byte[] buffer = out.toByteArray();
		
		//다시 읽고.
		ByteArrayInputStream bais = new ByteArrayInputStream(buffer);
		MapDictionary dictionary2 = new MapDictionary(bais);
		bais.close();
		CharVector[] result2 = dictionary2.getMap().get(new CharVector("엘지모니터"));
		System.out.println(result2[0]+","+result2[1]);
		//다시 쓰고.
		ByteArrayOutputStream out2 = new ByteArrayOutputStream();
		dictionary2.writeTo(out2);
		out2.close();
		
		//다시 읽고.
		ByteArrayInputStream bais2 = new ByteArrayInputStream(buffer);
		MapDictionary dictionary3 = new MapDictionary(bais2);
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
		
		SetDictionary dictionary = new SetDictionary();
		
		String[] terms = new String[] { "삼성", "LG", "애플" };
		
		for(String term : terms) {
			dictionary.addEntry(term, null, true, null);
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
		SetDictionary dictionary2 = new SetDictionary(bais, true);
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
		SetDictionary dictionary3 = new SetDictionary(bais2, true);
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
		
		SetDictionary dictionary = new SetDictionary();
		
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
		SetDictionary dictionary2 = new SetDictionary(bais, true);
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
		SetDictionary dictionary3 = new SetDictionary(bais2, true);
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
