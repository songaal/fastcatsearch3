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

public class IntList {
	private static Logger logger = LoggerFactory.getLogger(IntList.class);
			
	private final int DEFAULT_BIT_SIZE = 4096;
	private int[] intList;
	private int size;
	private File file;
	
	public IntList(){
		intList = new int[DEFAULT_BIT_SIZE];
	}
	public IntList(File dir, String filename) throws IOException{
		this(new File(dir, filename));
	}
	
	public IntList(File file) throws IOException{
		this.file = file;
		if(file.exists()){
			BufferedFileInput in = new BufferedFileInput(file);
			size = (int) (in.length() / IOUtil.SIZE_OF_INT);
			intList = new int[size];
			
			for (int i = 0; i < size; i++)
				intList[i] = in.readInt();
			
			in.close();
		}else{
			//파일이 없으면 빈 파일을 생성해준다.
			intList = new int[DEFAULT_BIT_SIZE];
			save();
		}
	}
	
	public int[] getList(){
		return intList;
	}
	
	public int getSize(){
		return size;
	}
	
	public void add(int number){
		
		if(size == intList.length){
			int newSize = (int) (intList.length * 1.2);
			int[] newIntList = new int[newSize];
			System.arraycopy(intList, 0, newIntList, 0, intList.length);
			intList = newIntList;
		}
		intList[size++] = number;
	}
	
	public void save() throws IOException{
		BufferedFileOutput out = new BufferedFileOutput(file);
		for (int i = 0; i < size; i++) {
			out.writeInt(intList[i]);
		}
		out.close();
	}
}
