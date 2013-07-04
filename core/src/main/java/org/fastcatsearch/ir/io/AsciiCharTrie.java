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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AsciiCharTrie {
	private static Logger logger = LoggerFactory.getLogger(AsciiCharTrie.class);
	
	private final int ASCII_SIZE = 128;
	private Node root = new Node();
	
	public int get(String key) {
		key = key.toLowerCase();
		int len = key.length();
		Node node = root;
		
		for (int i = 0; i < len; i++) {
			char ch = key.charAt(i);
			if(ch >= 128){
				logger.error("This character is not an ascii code. char = {}", ch);
				return -1;
			}
			node = node.getChild(ch);
			
			//search fail!
			if(node == null)
				return -1;
		}
		
		if(node.getValue() != -1){
			return node.getValue();
		}
		
		return -1;
	}
	
	public void put(String key, int value) {
		key = key.toLowerCase();
		int len = key.length();

		Node node = root;
		
		for (int i = 0; i < len; i++) {
			char ch = key.charAt(i);
			if(ch >= ASCII_SIZE){
				logger.error("This character is not an ascii code. char = ", ch);
				return;
			}
			Node n = node.getChild(ch);
			if(n == null){
				n = new Node();
				node.setChild(ch, n);
			}
			
			node = n;
		}
		
		node.setValue(value);
		
	}
	
	class Node {
		
		private Node[] children;
		private int value = -1;
		
		public Node(){
			children = new Node[ASCII_SIZE];
//			logger.debug("generate!");
		}
		
		public Node getChild(int i){
			return children[i];
		}
		
		public void setChild(int i, Node node){
			children[i] = node;
		}
		
		public int getValue(){
			return value;
		}
		
		public void setValue(int value){
			this.value = value;
		}
	}
	
}
