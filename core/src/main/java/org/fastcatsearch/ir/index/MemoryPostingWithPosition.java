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


/**
 */
public class MemoryPostingWithPosition extends MemoryPosting {

	public MemoryPostingWithPosition(int size) {
		super(size);
	}
	public MemoryPostingWithPosition(int size, boolean isIgnoreCase) {
		super(size, isIgnoreCase);
	}
	
	@Override
	protected PostingBufferWithPosition newPostingBuffer(){
		return new PostingBufferWithPosition();
	}
}

