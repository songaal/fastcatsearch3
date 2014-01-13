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
		SynonymDictionary dictionary = new SynonymDictionary(true);
		dictionary.addEntry("마우스", new String[] { "mouse, 로지텍" });
		dictionary.addEntry(null, new String[] { "엘지모니터, monitor, 광시야각, 마우스" });
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
		
		String word = "MONitor";
		CharVector[] r = dictionary.map().get(new CharVector(word));
		System.out.println(word + " >> "+ join(r));
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
		boolean ignoreCase = true;
		
		MapDictionary dictionary = new MapDictionary(ignoreCase);
		dictionary.addEntry("마우스", new String[] { "mouse", "로지텍" });
		dictionary.addEntry("모니터", new String[] { "엘지모니터", "monitor", "광시야각" });
		
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		dictionary.writeTo(out);
		out.close();
		CharVector[] result = dictionary.getUnmodifiableMap().get(new CharVector("모니터"));
		System.out.println(result[0]+","+result[1]);
		
		
		
		byte[] buffer = out.toByteArray();
		
		//다시 읽고.
		ByteArrayInputStream bais = new ByteArrayInputStream(buffer);
		MapDictionary dictionary2 = new MapDictionary(bais, ignoreCase);
		bais.close();
		CharVector[] result2 = dictionary2.getUnmodifiableMap().get(new CharVector("모니터"));
		System.out.println(result2[0]+","+result2[1]);
		//다시 쓰고.
		ByteArrayOutputStream out2 = new ByteArrayOutputStream();
		dictionary2.writeTo(out2);
		out2.close();
		
		//다시 읽고.
		ByteArrayInputStream bais2 = new ByteArrayInputStream(buffer);
		MapDictionary dictionary3 = new MapDictionary(bais2, ignoreCase);
		bais2.close();
		CharVector[] result3 = dictionary3.getUnmodifiableMap().get(new CharVector("모니터"));
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
	public void testCustomDictionary() throws IOException {
		boolean ignoreCase = true;
		
		CustomDictionary dictionary = new CustomDictionary(ignoreCase);
		dictionary.addEntry("마우스", new Object[] { "mouse", "로지텍", 1 });
		dictionary.addEntry("모니터", new Object[] { 0,"엘지모니터", "monitor", "광시야각", 2 });
		
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		dictionary.writeTo(out);
		out.close();
		//Object[] result = dictionary.getUnmodifiableMap().get(new CharVector("모니터"));
		Object[] result = dictionary.map().get(new CharVector("모니터"));
		System.out.println(result.length+":"+result[0]);
		System.out.println(result[0]+","+result[1]);
		
		
		
		byte[] buffer = out.toByteArray();
		
		//다시 읽고.
		ByteArrayInputStream bais = new ByteArrayInputStream(buffer);
		CustomDictionary dictionary2 = new CustomDictionary(bais, ignoreCase);
		bais.close();
		Object[] result2 = dictionary2.getUnmodifiableMap().get(new CharVector("모니터"));
		System.out.println(result2[0]+","+result2[1]);
		//다시 쓰고.
		ByteArrayOutputStream out2 = new ByteArrayOutputStream();
		dictionary2.writeTo(out2);
		out2.close();
		
		//다시 읽고.
		ByteArrayInputStream bais2 = new ByteArrayInputStream(buffer);
		CustomDictionary dictionary3 = new CustomDictionary(bais2, ignoreCase);
		bais2.close();
		Object[] result3 = dictionary3.getUnmodifiableMap().get(new CharVector("모니터"));
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
			dictionary.addEntry(term, null);
		}
		
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		dictionary.writeTo(out);
		out.close();
		
		for(String term : terms) {
			boolean contains = dictionary.getUnmodifiableSet().contains(new CharVector(term));
			System.out.println("is set has term "+term+" ? "+contains);
		}
		
		byte[] buffer = out.toByteArray();
		
		//다시 읽고.
		ByteArrayInputStream bais = new ByteArrayInputStream(buffer);
		SetDictionary dictionary2 = new SetDictionary(bais, true);
		bais.close();
		
		for(String term : terms) {
			boolean contains = dictionary2.getUnmodifiableSet().contains(new CharVector(term));
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
			boolean contains = dictionary3.getUnmodifiableSet().contains(new CharVector(term));
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
//			dictionary.addEntry(term);
		}
		
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		dictionary.writeTo(out);
		out.close();
		
		for(String term : terms) {
			boolean contains = dictionary.getUnmodifiableSet().contains(new CharVector(term));
			System.out.println("is set has term "+term+" ? "+contains);
		}
		
		byte[] buffer = out.toByteArray();
		
		//다시 읽고.
		ByteArrayInputStream bais = new ByteArrayInputStream(buffer);
		SetDictionary dictionary2 = new SetDictionary(bais, true);
		bais.close();
		
		for(String term : terms) {
			boolean contains = dictionary2.getUnmodifiableSet().contains(new CharVector(term));
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
			boolean contains = dictionary3.getUnmodifiableSet().contains(new CharVector(term));
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
	public void testSpaceDictionary(){
		SpaceDictionary spaceDictionary = new SpaceDictionary(true);
		spaceDictionary.addEntry("nike, air", null);
		spaceDictionary.addEntry("진, 청바지", null);
		spaceDictionary.addEntry("맥북, 프로, 레티나", null);
		for(Map.Entry<CharVector,CharVector[]> entry : spaceDictionary.map().entrySet()){
			System.out.println(entry.getKey() + " : " + join(entry.getValue()));
		}
		CharVector[] result = spaceDictionary.map().get(new CharVector("nIKEAIR"));
		Assert.assertEquals("nike", result[0].toString());
		Assert.assertEquals("air", result[1].toString());
		result = spaceDictionary.map().get(new CharVector("진청바지"));
		Assert.assertEquals("진", result[0].toString());
		Assert.assertEquals("청바지", result[1].toString());
		result = spaceDictionary.map().get(new CharVector("맥북프로레티나"));
		Assert.assertEquals("맥북", result[0].toString());
		Assert.assertEquals("프로", result[1].toString());
		Assert.assertEquals("레티나", result[2].toString());
	}
	
}
