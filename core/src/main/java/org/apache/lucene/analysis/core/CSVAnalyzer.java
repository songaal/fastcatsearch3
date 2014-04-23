package org.apache.lucene.analysis.core;

/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import java.io.IOException;
import java.io.Reader;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.util.CharTokenizer;
import org.apache.lucene.util.AttributeSource;

public final class CSVAnalyzer extends Analyzer {
  
  public CSVAnalyzer() {
  }
  
  @Override
  protected TokenStreamComponents createComponents(final String fieldName,
      final Reader reader) {
    return new TokenStreamComponents(new CSVTokenizer(reader));
  }
}

/*
 * 사용시 주의 : 컴마 뒤의 공백은 없어지나, 컴마 앞의 공백 즉, 단어 뒤에 공백이 있을시 사라지지 않음.    
 * */
final class CSVTokenizer extends CharTokenizer {
	  
	  public CSVTokenizer(Reader in) {
	    super(in);
	  }

	  public CSVTokenizer(AttributeSource source, Reader in) {
	    super(source, in);
	  }

	  public CSVTokenizer(AttributeFactory factory, Reader in) {
	    super(factory, in);
	  }
	  
	  boolean trailingWhitespace = false;
	  @Override
	  protected boolean isTokenChar(int c) {
		  if(c == ',' ){
			  trailingWhitespace = true;
			  return false;
		  }else if(c == ' '){
			  if(trailingWhitespace){
				  return false;
			  }else{
				  return true;
			  }
		  }else{
			  trailingWhitespace = false;
			  return true;
		  }
	  }
	  
	  @Override
	  public void reset() throws IOException {
	    super.reset();
	    trailingWhitespace = false;
	  }
	}
