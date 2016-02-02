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

package org.fastcatsearch.ir.document;

import org.fastcatsearch.ir.field.Field;
import org.json.JSONException;
import org.json.JSONStringer;
import org.json.JSONWriter;

import java.util.ArrayList;
import java.util.List;


public class Document {
	
	private List<Field> fields;
	private int docId; //검색결과로 채워진다. 색인시에는 아무연관이 없다.
	private int score; //after search, this has cetain value.
	
	public Document(int size){
		fields = new ArrayList<Field>(size);
	}

	public int size(){
		return fields.size();
	}
	
	public void add(Field f){
		fields.add(f);
	}
	
	public void set(int index, Field f){
		fields.set(index, f);
	}
	
	public Field get(int index){
		return fields.get(index);
	}
	
	public void setDocId(int docId){
		this.docId = docId;
	}
	
	public int getDocId(){
		return docId;
	}
	
	public void setScore(int score){
		this.score = score;
	}
	
	public int getScore(){
		return score;
	}
	
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("[Document]");
		for (int index = 0; index < fields.size(); index++) {
			sb.append("\nF_").append(index).append("[").append(fields.get(index).getClass().getSimpleName()).append("]=").append(fields.get(index).toString());
		}
		return sb.toString();
	}

    public String toJsonString() throws JSONException {

        JSONStringer json =  new JSONStringer();

        JSONWriter w = json.object();
        for(Field f : fields) {
            w.key(f.getId()).value(f.toString());
        }
        w.endObject();
        return json.toString();
    }
}
