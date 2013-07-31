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

package org.fastcatsearch.ir.query;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.fastcatsearch.ir.common.IRException;
import org.fastcatsearch.ir.search.FieldIndexesReader;
import org.fastcatsearch.ir.settings.FieldSetting;
import org.fastcatsearch.ir.settings.Schema;



public class Filters {
	
	private List<Filter> filterList;
	
	public Filters(){
		filterList = new ArrayList<Filter>();
	}
	
	public String toString(){
		String str = "";
		for(Filter g : filterList){
			str += g.toString()+",";
		}
		return str;
	}
	
	public void add(Filter Filter){
		filterList.add(Filter);
	}
	
	public int size(){
		return filterList.size();
	}
	
	public List<Filter> getFilterList(){
		return filterList;
	}
	
	public HitFilter getHitFilter(Schema schema, FieldIndexesReader fieldIndexesReader, int bulkSize) throws IOException, IRException {
		return new HitFilter(filterList, schema, fieldIndexesReader, bulkSize);
	}
}
