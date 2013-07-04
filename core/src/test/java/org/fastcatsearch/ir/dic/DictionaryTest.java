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

package org.fastcatsearch.ir.dic;

import java.io.File;

import org.fastcatsearch.ir.common.IRException;
import org.fastcatsearch.ir.dic.HashMapDictionary;
import org.fastcatsearch.ir.dic.HashSetDictionary;
import org.fastcatsearch.ir.io.CharVector;

import junit.framework.TestCase;


public class DictionaryTest extends TestCase{
	public void testHashSetDic() throws IRException{
		File f = new File("dic/korean.dic");
		HashSetDictionary dic = new HashSetDictionary(f);
		String[] list = new String[]{"검색","엔진","그룹","동강","흐르는","잡화꿀","개발자", "영월"};
		long st = System.currentTimeMillis();
		for (int i = 0; i < list.length; i++) {
			CharVector term = new CharVector(list[i]);
			System.out.println(term+" = "+dic.contains(term));
		}
		System.out.println("search time = "+(System.currentTimeMillis() - st));
	}
	
	public void testHashMapDic() throws IRException{
		File f = new File("dic/synonym.dic");
		HashMapDictionary dic = new HashMapDictionary(f);
		String[] list = new String[]{"중국","나이키","빈문자","미국"};
		long st = System.currentTimeMillis();
		for (int i = 0; i < list.length; i++) {
			CharVector term = new CharVector(list[i]);
			CharVector[] termList = dic.get(term);
			if(termList!= null){
				for (int j = 0; j < termList.length; j++) {
					System.out.println(term+" => "+termList[j]);
				}
			}else{
				System.out.println(term+" => "+null);
			}
		}
		System.out.println("search time = "+(System.currentTimeMillis() - st));
	}
}
