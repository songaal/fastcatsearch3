package org.fastcatsearch.ir.util;

import java.util.Map;
import java.util.Map.Entry;

import org.fastcatsearch.ir.io.CharVector;
import org.junit.Test;

public class CharVectorHashMapTest {

	@Test
	public void testIgnoreCase() {
		Map<CharVector, CharVector> map = new CharVectorHashMap<CharVector>(true);
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
		
		System.out.println(map.containsKey(getCharVector("aBc")));
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

}
