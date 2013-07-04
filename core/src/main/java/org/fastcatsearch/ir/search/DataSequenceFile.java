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

package org.fastcatsearch.ir.search;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.Date;

import org.fastcatsearch.ir.common.IRException;
import org.fastcatsearch.ir.common.IRFileName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class DataSequenceFile {
	private static Logger logger = LoggerFactory.getLogger(DataSequenceFile.class);
	private int sequence = -1;
	private String dateStr;
	private File f;
	public DataSequenceFile(File collectionHomeDir) throws IRException{
		this(collectionHomeDir, -1);
	}
	
	public DataSequenceFile(File collectionHomeDir, int seq) throws IRException{
		
		f = new File(collectionHomeDir, IRFileName.dataSequenceFile);
		
		//if 'seq' is 0 or a positive integer.. 
		if(seq >= 0){
			//write
			this.sequence = seq;
			logger.debug("DataSequenceFile Set seq[{}] >> {}", sequence, f.getAbsolutePath());
			//do not save
		}else{
			try{
				if(f.exists()){
					BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(f)));
					try{
						this.sequence = Integer.parseInt(reader.readLine());
					}catch(NumberFormatException e){
						this.sequence = 0;
						logger.warn("[WARN] DataSequenceFile sequence parsing error. file = "+f.getAbsolutePath());
					}
					this.dateStr = reader.readLine();
					reader.close();
					logger.debug("DataSequenceFile Load seq[{}], {} >> {}", sequence, dateStr, f.getAbsolutePath());
				}else{
					//if file is not found, sequence is 0. 
					this.sequence = 0;
					save();
					logger.debug("DataSequenceFile을 Create seq[{}] >> {}", sequence, f.getAbsolutePath());
				}
			}catch(IOException e){
				throw new IRException(e);
			}
		}
	}
	
	public int getSequence(){
		return sequence;
	}
	
	public File getDataDirFile(){
		return new File(f.getParent(), sequence == 0 ? "data" : "data"+sequence);
	}
	
	
	public String getDateString(){
		return dateStr;
	}
	
	public void save() throws IRException{
		try{
			PrintWriter writer = new PrintWriter(new OutputStreamWriter(new FileOutputStream(f)));
			writer.println(sequence);
			dateStr = new Date().toString();
			writer.println(dateStr);
			writer.close();
		}catch(IOException e){
			throw new IRException(e);
		}
	}
}	
