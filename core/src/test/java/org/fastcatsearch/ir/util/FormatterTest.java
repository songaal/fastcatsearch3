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

import java.util.Random;

import org.fastcatsearch.ir.util.Formatter;

import junit.framework.TestCase;

public class FormatterTest extends TestCase{
	
	public void testTime(){
		Random r = new Random();
		
		long s = 0;
		for (int i = 1; i < 5; i++) {
			s  = r.nextInt((int) Math.pow(60, i));
			System.out.println(s+" = "+Formatter.getFormatTime(s));
		}
		
		
	}
	
	public void testSize(){
		Random r = new Random();
		
		long s = 0;
		for (int i = 1; i < 5; i++) {
			s  = r.nextInt((int) Math.pow(1024, i));
			System.out.println(s+" = "+Formatter.getFormatSize(s));
		}
		
		
	}
	
	
}
