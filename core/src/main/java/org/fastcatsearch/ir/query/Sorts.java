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

import org.fastcatsearch.ir.search.BundleSortGenerator;
import org.fastcatsearch.ir.search.FieldIndexesReader;
import org.fastcatsearch.ir.search.HitMerger;
import org.fastcatsearch.ir.search.HitRanker;
import org.fastcatsearch.ir.search.SortGenerator;
import org.fastcatsearch.ir.settings.Schema;



public class Sorts {
	public static final Sorts DEFAULT_SORTS = new Sorts(); 
	private List<Sort> sortList;
	
	public Sorts(){
		sortList = new ArrayList<Sort>();
	}
	
	public String toString(){
		String str = "";
		for(Sort g : sortList){
			str += g.toString()+",";
		}
		return str;
	}
	
	public void add(Sort Sort){
		sortList.add(Sort);
	}
	
	public int size(){
		return sortList.size();
	}
	
	public List<Sort> getSortList(){
		return sortList;
	}
	
	public SortGenerator getSortGenerator(Schema schema, FieldIndexesReader fieldIndexesReader, Bundle bundle) throws IOException {
		if(bundle == null) {
			return new SortGenerator(sortList, schema, fieldIndexesReader);
		} else {
			return new BundleSortGenerator(bundle, sortList, schema, fieldIndexesReader);
		}
	}

	public HitRanker createRanker(Schema schema, int sortMaxSize) throws IOException {
		return new HitRanker(sortList, schema, sortMaxSize);
	}
	
	public HitMerger createMerger(Schema schema, int segmentSize) throws IOException {
		return new HitMerger(sortList, schema, segmentSize);
	}
}
