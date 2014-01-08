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

package org.fastcatsearch.ir.index;

import java.util.Random;

import org.fastcatsearch.ir.common.IRException;
import org.fastcatsearch.ir.index.MemoryPosting;
import org.fastcatsearch.ir.io.CharVector;
import org.fastcatsearch.ir.io.IOUtil;
import org.fastcatsearch.ir.util.Formatter;
import org.junit.Test;

public class MemoryPostingTest {
	public static void main(String[] args) throws IRException {
		MemoryPostingTest t = new MemoryPostingTest();
		int size = Integer.parseInt(args[0]);
		int count = Integer.parseInt(args[1]);

		t.test1(size, count);
	}

	public void test1(int size, int count) throws IRException {
		// try {
		// Thread.sleep(20 * 1000);
		// } catch (InterruptedException e) {
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// }
		MemoryPosting mp = new MemoryPosting(64 * 1024);

		byte[] buf = new byte[size * 2];
		char[] cbuf = new char[size];

		Random r = new Random(System.currentTimeMillis());
		CharVector term = new CharVector();

		for (int i = 0; i < count; i++) {
			r.nextBytes(buf);
			for (int pos = 0; pos < size; pos++) {
				cbuf[pos] = (char) IOUtil.readShort(buf, pos * 2);
			}
			term.init(cbuf, 0, size);

			mp.add(term, r.nextInt(10000000));
			if ((i + 1) % 10000 == 0)
				System.out.println("mem = " + Formatter.getFormatSize(Runtime.getRuntime().totalMemory()) + " / " + Formatter.getFormatSize(mp.staticMemorySize()) + " / "
						+ Formatter.getFormatSize(mp.workingMemorySize()));
		}

	}

	@Test
	public void testLargeAndFlush() throws IRException {

		int count = 1000000000;
		MemoryPosting mp = new MemoryPosting(64 * 1024);
		System.out.println("---------START------------");
		Random r = new Random(System.currentTimeMillis());
		CharVector term = new CharVector("abc");
		byte[] buf = new byte[8];
//		for (int f = 0; f < flush; f++) {

			int docNo = 0;

			for (int i = 0; i < count; i++) {
				docNo += (r.nextInt(10) + 1);
				r.nextBytes(buf);
				term = new CharVector(new String(buf));
				mp.add(term, docNo);
				if ((i + 1) % 100000 == 0){
					System.out.println((i+1)+"] mem = max:" + Formatter.getFormatSize(Runtime.getRuntime().maxMemory()) + " / tot:" + Formatter.getFormatSize(Runtime.getRuntime().totalMemory()) + " / free:" +
							Formatter.getFormatSize(Runtime.getRuntime().freeMemory()) + " / static:" + Formatter.getFormatSize(mp.staticMemorySize()) + " / work:"
							+ Formatter.getFormatSize(mp.workingMemorySize()));
					
					//mp.saveTo();
					
					if(mp.staticMemorySize() > 128 * 1024 * 1024){
						mp.clear();
						System.out.println("---------------------");
						System.out.println("CLEAR mem = max:" + Formatter.getFormatSize(Runtime.getRuntime().maxMemory()) + " / tot:" + Formatter.getFormatSize(Runtime.getRuntime().totalMemory()) + " / free:" +
								Formatter.getFormatSize(Runtime.getRuntime().freeMemory()) + " / static:" + Formatter.getFormatSize(mp.staticMemorySize()) + " / work:"
								+ Formatter.getFormatSize(mp.workingMemorySize()));
						
						System.out.println("---------------------");
					}
				}
			}
			
			
			
//		}

	}
}
