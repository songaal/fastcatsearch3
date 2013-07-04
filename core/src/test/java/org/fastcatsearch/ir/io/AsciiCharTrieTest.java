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

import java.io.IOException;

import org.fastcatsearch.ir.io.AsciiCharTrie;



import junit.framework.TestCase;

public class AsciiCharTrieTest extends TestCase{
	public void test1() throws IOException{
		
		String[] keys = new String[]{"bool", "char","character","int","integer","in"};
//		String[] keys = new String[]{"ch","char"};
		int[] values = new int[]{4,12,12,5,14,5};
		
		AsciiCharTrie trie = new AsciiCharTrie();
		for(int i=0;i<keys.length;i++){
			trie.put(keys[i], values[i]);
		}
		
		for(int i=0;i<keys.length;i++){
			int actual = trie.get(keys[i]);
			System.out.println(values[i]+" : "+ actual);
			assertEquals(values[i], actual);
		}
	}
}
