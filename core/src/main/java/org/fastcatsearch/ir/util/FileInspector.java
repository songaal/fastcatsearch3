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

package org.fastcatsearch.ir.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

import org.fastcatsearch.ir.io.BufferedFileInput;
import org.fastcatsearch.ir.io.IndexInput;


public class FileInspector {
	private File f;
	private IndexInput in;
	
	public FileInspector(String path) throws IOException{
		f = new File(path);
		if(!f.exists()){
			throw new IOException("no such file = "+f.getAbsolutePath());
		}
		in = new BufferedFileInput(f);
	}
	
	public static void main(String[] args) throws IOException {
		if(args.length < 1){
			System.out.println("Usage : FileInspector filepath");
			System.exit(1);
		}
		FileInspector fi =new FileInspector(args[0]);
		
		BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
		System.out.println("FileInspector v0.1 started...");
		System.out.println("inspect file = "+args[0]);
		String line = null;
		System.out.println(">>");
		while((line = reader.readLine()) != null){
			try{
				if(line.equals("exit")){
					System.out.println("exit...");
					System.exit(0);
				}else if(line.equals("int")){
					System.out.println("int = "+fi.getInt());
				}else if(line.equals("vbint")){
					System.out.println("vbint = "+fi.getVInt());
				}else if(line.equals("long")){
					System.out.println("long = "+fi.getLong());
				}else if(line.equals("short")){
					System.out.println("short = "+fi.getInt());
				}else if(line.startsWith("achar")){
					String ii = line.substring(5);
					int i = Integer.parseInt(ii);
//					System.out.println("achar"+i+" = "+fi.getAChars(i));
				}else if(line.startsWith("uchar")){
					String ii = line.substring(5);
					int i = Integer.parseInt(ii);
//					System.out.println("uchar"+i+" = "+fi.getUChars(i));
				}else if(line.startsWith("byte")){
					String ii = line.substring(4);
					int i = Integer.parseInt(ii);
					System.out.println("byte"+i+" = "+fi.getBytes(i));
				}else if(line.startsWith("go")){
					String ii = line.substring(2);
					long l = Long.parseLong(ii);
					fi.pos(l);
					System.out.println("position = "+l);
				}else if(line.equals("size")){
					System.out.println("filesize = "+fi.size());
				}else if(line.equals("pos")){
					System.out.println("position = "+fi.pos());
				}else{
					System.out.println("cannot understand a command : "+line);
					System.out.println("retry..");
				}
				System.out.println(">>");
			}catch(Exception e){
				System.out.println("error.."+e.getStackTrace());
			}
			
		}
		
	}

	private int getVInt() throws IOException {
		return in.readVInt();
	}

//	private String getAChars(int i) throws IOException {
//		return new String(in.readAChars(i));
//	}
//
//	private String getUChars(int i) throws IOException {
//		return new String(in.readUChars(i));
//	}
	private long getLong() throws IOException {
		return in.readLong();
	}

	private int getInt() throws IOException {
		return in.readInt();
	}
	
	private int getBytes(int i) throws IOException {
		byte[] data = new byte[i];
		in.readBytes(data, 0, i);
		return i;
	}
	private long pos() throws IOException{
		return in.position();
	}
	private void pos(long l) throws IOException{
		if(l > in.length()){
			System.out.println("Position "+l +" exceed filesize = "+in.length());
		}else{
			in.seek(l);
		}
	}
	
	private long size() throws IOException{
		return in.length();
	}
}
