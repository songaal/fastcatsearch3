package org.fastcatsearch.ir.util;

import java.util.Set;

import org.fastcatsearch.ir.io.CharVector;
import org.junit.Test;

public class CharVectorHashSetTest {

	@Test
	public void testIgnoreCase() {
		
		Set<CharVector> set = new CharVectorHashSet(true);
		set.add(getCharVector("abc"));
		set.add(getCharVector("Abc"));
		set.add(getCharVector("aBc"));
		set.add(getCharVector("abC"));
		set.add(getCharVector("ABC"));		
		
		printSet(set);
		
		System.out.println(set.contains(getCharVector("abc")));
		System.out.println(set.contains(getCharVector("Abc")));
		System.out.println(set.contains(getCharVector("aBc")));
		System.out.println(set.contains(getCharVector("abC")));
		System.out.println(set.contains(getCharVector("ABC")));
		
	}
	
	private void printSet(Set<CharVector> set) {
		for(CharVector entry : set){
			System.out.println(entry);
		}
	}

	private CharVector getCharVector(String string) {
		return new CharVector(string);
	}

}
