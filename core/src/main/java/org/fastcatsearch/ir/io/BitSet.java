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

public class BitSet {
	private static Logger logger = LoggerFactory.getLogger(BitSet.class);
			
	private final int DEFAULT_BIT_SIZE = 8;
	protected long[] bitdata;
	private File file;
	
	private transient int onCount = -1;

	public BitSet(){
		bitdata = new long[DEFAULT_BIT_SIZE];
	}
	
	public BitSet(int size){
		bitdata = new long[size];
	}
	public BitSet(File dir, String filename) throws IOException{
		this(dir, filename, false);
	}
	public BitSet(File dir, String filename, boolean create) throws IOException{
		this(new File(dir, filename), create);
	}
	
	public BitSet(File file) throws IOException{
		this(file, false);
	}
	public BitSet(File file, boolean create) throws IOException{
		this.file = file;
		logger.debug("Load deleteSet >> {}", file.getAbsolutePath());
		if(!create && file.exists()){
			BufferedFileInput in = new BufferedFileInput(file);
			int size = (int) (in.length() / IOUtil.SIZE_OF_LONG);
			bitdata = new long[size];
			
			for(int i=0;i<size;i++){
				bitdata[i] = in.readLong();
//				logger.debug("delete set {}", bitdata[i]);
			}
			in.close();
			
		}else{
			//파일이 없으면 빈 파일을 생성해준다.
			bitdata = new long[DEFAULT_BIT_SIZE];
			save();
		}
	}

    public void setFile(File f) {
        this.file = f;
    }
    public void init(BitSet another) {
        this.bitdata = another.bitdata;
    }
	
	public String toString(){
		return "[BitSet] "+file.getAbsolutePath();
	}
	
	public String getEntry(){
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < bitdata.length; i++) {
			long l = bitdata[i];
			for (int off = 0; off < IOUtil.BITS_OF_LONG; off++) {
				long mask = 0x8000000000000000L >>> off;
				if((l & mask) != 0){
					sb.append(i * 64 + off);
					sb.append(", ");
				}
			}
		}
		return sb.toString();
	}
	public boolean isSet(int number){
		int pos = number / IOUtil.BITS_OF_LONG;
		int off = number % IOUtil.BITS_OF_LONG;
		
		long mask = 0x8000000000000000L >>> off;
		int size = bitdata.length;
		if(pos >= size){
			return false;
		}
		
		return (bitdata[pos] & mask) != 0;
	}
	
	public void set(int number){
		int pos = number / IOUtil.BITS_OF_LONG;
		int off = number % IOUtil.BITS_OF_LONG;
		long mask = 0x8000000000000000L >>> off;
		
		int size = bitdata.length;
		if(pos >= size){
			while(pos >= size){
				size += DEFAULT_BIT_SIZE;
			}
			long[] newWords = new long[size];
			System.arraycopy(bitdata, 0, newWords, 0, bitdata.length);
			bitdata = newWords;
		}
		
		bitdata[pos] |= mask;

        //무효처리.
        onCount = -1;
	}
	
	public void save() throws IOException{
		BufferedFileOutput out = new BufferedFileOutput(file);
		int size = bitdata.length;
		for (int i = 0; i < size; i++) {
			out.writeLong(bitdata[i]);
		}
		out.close();
	}

    public int getOnCount() {
        if(onCount == -1) {
            int count = 0;
            for (int i = 0; i < bitdata.length; i++) {
                long bits = bitdata[i];
                for(int p = 0; p < Long.SIZE; p++) {
                    if((bits & 1) > 0) {
                        count++;
                    }
                    bits >>>= 1;
                }
            }
            onCount = count;
        }
        return onCount;
    }
}
