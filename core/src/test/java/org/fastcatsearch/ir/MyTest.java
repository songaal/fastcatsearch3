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

package org.fastcatsearch.ir;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import junit.framework.TestCase;

public class MyTest extends TestCase{
	public void _testShift(){
		System.out.println(1<<1);
		
	}
	public void __test2(){
		byte a = 127;
		byte b = -128;// in
		int diff = 0;
		int diff2 = 0;
		if((a < 0 && b >= 0) || (a >= 0 && b < 0)){
			diff = (255 - b & 0xff) - (255 - a & 0xff);
			diff2 = b -a  ;
		}
		else{
			diff = (b & 0xff) - (a & 0xff);
			diff2 = b - a ;
		}
		System.out.println(diff);
		System.out.println(diff2);
		int c = 255 - b & 0xff;
		int d = 256 ;
		
//		System.out.println("c="+c);
//		System.out.println("d="+(byte)d);
	}
	
	public void __test3(){
		char[] str = "abc123가나다라".toCharArray();
		for(int i=0;i<str.length;i++){
			byte a = (byte) ((str[i] >>> 8) & 0xff);
			byte b = (byte) ((str[i] >>> 0) & 0xff);
			System.out.println(str[i]+":"+a+","+b);
		}
	}
	
	public void _test4(){
		for(int i=-5;i<5;i++){
			int j = ~i;
			int k = j - 1;
			System.out.println(i +" ,"+j+" ,"+k);
			
		}
		
	}
	
	public void _testConvert(){
		int i = 11;
		char ch = (char)i;
		char ch2 = (char) (i+1);
		System.out.println(ch+""+(int)ch);
		
		System.out.println(ch<= ch2);
	}
	
	public void __testCompre(){
		char ch1 = '재';
		char ch2 = '있';
		
		if(ch1>ch2){
			System.out.println(1);
		}else if(ch1<ch2){
			System.out.println(-1);
		}else{
			System.out.println(ch1 - ch2);
		}
		
		
	}
	
	public void __testToByte(){
		char ch1 = 'a';
		char ch2 = '힣';
		
		for(int i=ch1; i<ch2;i++){
			char ch = (char) i;
			int a = (ch >> 8) & 0xff;
			int b = (ch >> 0) & 0xff;
			System.out.println(ch + " : "+a + ","+ b);
			assertTrue(a >= 0);
			assertTrue(b >= 0);
		}
		
	}
	
	public void testNot(){
		for(int i=-0xFF;i<0xFF;i++){
//			System.out.println(i+" : "+(byte)i);
			System.out.println(i+" : "+(~i)+" : "+(byte)i+" : "+(byte)(~(i & 0xFF))+" : "+(byte)(0xFF - i));
		}
		
	}
	
	public void byteTest(){
		byte b = 0;
		int i = -4;
		b = (byte)i;
		System.out.println("b = "+(b & 0xFF));
		b = (byte)(i & 0xFF);
		System.out.println("b2 = "+(b & 0xFF));
	}
	
	
	public void reflectTest() {
		List<String> types = new ArrayList<String>();
		String pkg = "org.fastcatsearch.ir.config.";
		ClassLoader clsldr = getClass().getClassLoader();
		String path = pkg.replace(".", File.separator);
		try {
			Enumeration<URL> em = clsldr.getResources(path);
			while(em.hasMoreElements()) {
				String urlstr = em.nextElement().toString();
				if(urlstr.startsWith("jar:file:")) {
					String jpath = urlstr.substring(9);
					int st = jpath.indexOf("!/");
					jpath = jpath.substring(0,st);
					JarFile jf = new JarFile(jpath);
					Enumeration<JarEntry>jee = jf.entries();
					while(jee.hasMoreElements()) {
						JarEntry je = jee.nextElement();
						System.out.println(je.getName());
					}
				} else  if(urlstr.startsWith("file:")) {
					
				}
				System.out.println(urlstr);
				//JarFile jf = new JarFile();
				//File dir = new File(em.nextElement().toExternalForm());
				//System.out.println(dir+":"+dir.exists());
				//File[] files = new File(em.nextElement().getFile()).listFiles();
				//for(int i=0;i<files.length;i++) {
					//System.out.println(files[i]+":"+files[i].exists());
				//}
			}
		} catch (IOException e) { }
	}
}
