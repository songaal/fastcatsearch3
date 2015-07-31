/*
 * Copyright 2013 Websquared, Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.fastcatsearch.ir.io;

import java.util.HashMap;

import org.fastcatsearch.ir.io.CharVector;



import junit.framework.TestCase;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class CharVectorTest {

    @Test
	public void test1(){
		String str ="금나와라와라뚝딱은나와라와라뚝딱";
		char[] ca = str.toCharArray();
		CharVector[] c = new CharVector[6];
		c[0] = new CharVector(ca,0,1);
		c[1] = new CharVector(ca,1,5);
		c[2] = new CharVector(ca,6,2);
		c[3] = new CharVector(ca,8,1);
		c[4] = new CharVector(ca,9,5);
		c[5] = new CharVector(ca,14,2);
		
		for(int i =0;i<c.length;i++)
			System.out.println(new String(c[i].array(),c[i].start(),c[i].length()) +" : "+c[i].hashCode());
		
		HashMap map = new HashMap();
		int i=0;
		map.put(c[i++], 0);
		map.put(c[i++], 3);
		map.put(c[i++], 9);
		map.put(c[i++], 1);
		map.put(c[i++], 3);
		map.put(c[i++], 9);
		
		i=0;
		assertEquals(0, map.get(c[i++]));
		assertEquals(3, map.get(c[i++]));
		assertEquals(9, map.get(c[i++]));
		assertEquals(1, map.get(c[i++]));
		assertEquals(3, map.get(c[i++]));
		assertEquals(9, map.get(c[i++]));
	}

    @Test
	public void trimTest(){
		CharVector cv = new CharVector("   1 2 4  ");
		System.out.println("Before ="+cv);
		System.out.println("After ="+cv.trim());
		
		cv = new CharVector("");
		System.out.println("After ="+cv.trim());
	}

    @Test
    public void removeWhitespaces() {
        CharVector cv = new CharVector("   1 2 4  ");
        CharVector cv2 = new CharVector("124");
        assertEquals(cv2, cv.removeWhitespaces());

        cv = new CharVector("").removeWhitespaces();
        assertEquals(0, cv.length());
    }
}
