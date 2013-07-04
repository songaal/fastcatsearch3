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

import java.io.File;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BitSetBulkReader {
	private static Logger logger = LoggerFactory.getLogger(BitSetBulkReader.class);

	private long[] bitdata;
	private long l;
	private int lp;
	private int bp;
	
	public BitSetBulkReader(BitSet set){
		bitdata = set.bitdata;
	}
	
	public int next(){
		while(lp < bitdata.length){
			l = bitdata[lp];
			while(bp < IOUtil.BITS_OF_LONG){
				long mask = 0x8000000000000000L >>> bp;
//			logger.debug("long  ="+Long.toBinaryString(l));
//			logger.debug("mask  ="+Long.toBinaryString(mask));
				int r = -1;
				if((l & mask) != 0){
					r = lp * IOUtil.BITS_OF_LONG + bp;
				}
				bp++;
				
				if(r != -1){
					return r;
				}
			}
			bp = 0;
			lp++;
		}
		return -1;
	}
	
	public static void main(String[] args) throws IOException {
		BitSet set = new BitSet(new File(args[0]));
		BitSetBulkReader reader = new BitSetBulkReader(set);
		while(true){
			int i = reader.next();
			if(i == -1)
				break;
				
			System.out.println(i);
			
		}
		
	}
}
