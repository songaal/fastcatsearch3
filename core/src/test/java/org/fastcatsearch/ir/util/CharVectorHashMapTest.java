package org.fastcatsearch.ir.util;

import java.util.Map;
import java.util.Map.Entry;

import org.fastcatsearch.ir.io.CharVector;
import org.junit.Test;

public class CharVectorHashMapTest {

	@Test
	public void testIgnoreCase() {
		Map<CharVector, CharVector> map = new CharVectorHashMap<CharVector>(false);
		CharVector charKey = getCharVector("abc");
		map.put(charKey, charKey);
		charKey = getCharVector("ABC");
		map.put(charKey, charKey);
		charKey = getCharVector("Abc");
		map.put(charKey, charKey);
		charKey = getCharVector("abC");
		map.put(charKey, charKey);
		charKey = getCharVector("가나다");
		map.put(charKey, charKey);
		
		printMap(map);
		
		System.out.println(map.containsKey(getICCharVector("abc")));
		System.out.println(map.containsKey(getICCharVector("aBc")));
		System.out.println(map.containsKey(getCharVector("Abc")));
		System.out.println(map.containsKey(getCharVector("가나다")));
	}

	private void printMap(Map<CharVector, CharVector> map) {
		for(Entry<CharVector, CharVector> entry : map.entrySet()){
			System.out.println(entry);
		}
	}

	private CharVector getCharVector(String string) {
		return new CharVector(string);
	}
	
	private CharVector getICCharVector(String string) {
		CharVector cv = new CharVector(string);
		cv.setIgnoreCase();
		return cv;
	}

	@Test
	public void testCharVectorSpeed(){
		CharVector term1 = new CharVector("abcdefghijklmnop");
		CharVector term2 = new CharVector("abcdefghijklmnop");
		int COUNT = 1000 * 100000;
		long st = System.nanoTime();
		for(int i=0;i<COUNT;i++){
			boolean b = term1.equals(term2);
		}
		System.out.println("time1 = "+(System.nanoTime() - st)/1000000 + "ms");
		st = System.nanoTime();
		term1.setIgnoreCase();
		for(int i=0;i<COUNT;i++){
			boolean b = term1.equals(term2);
		}
		System.out.println("time2 = "+(System.nanoTime() - st)/1000000 + "ms");
	}
}
