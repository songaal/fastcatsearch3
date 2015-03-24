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

import org.apache.lucene.util.BytesRef;
import org.fastcatsearch.ir.query.Bundle;
import org.fastcatsearch.ir.query.RankInfo;
import org.fastcatsearch.ir.query.Sort;
import org.fastcatsearch.ir.settings.Schema;

import java.io.IOException;
import java.util.List;



/**
 * @author sangwook.song
 *
 */
public class BundleSortGenerator extends SortGenerator {
	
	private String fieldId;
	private IndexRef<FieldIndexReader> bundleIndexRef;
	private BytesRef data;
	
	public BundleSortGenerator(Bundle bundle, List<Sort> querySortList, Schema schema, FieldIndexesReader fieldIndexesReader) throws IOException{
		super(querySortList, schema, fieldIndexesReader);
		
		fieldId = bundle.getFieldIndexId();
		bundleIndexRef = fieldIndexesReader.selectIndexRef(new String[] { fieldId });
		data = bundleIndexRef.getDataRef(0).bytesRef();
	}
	
	@Override
	public HitElement[] getHitElement(RankInfo[] rankInfoList, int n) throws IOException{
		HitElement[] result = super.getHitElement(rankInfoList, n);
		///번들 데이터를 채워준다.
		for (int i = 0; i < n; i++) {
			RankInfo ri = rankInfoList[i];
			bundleIndexRef.read(ri.docNo());
			if(!isBundleKeyEmpty(data)) {
				BytesRef bundleKey = data.duplicate();
				result[i].setBundleKey(bundleKey);
			}
		}
		return result;
	}
	
	@Override
	public void getHitElement(RankInfo[] rankInfoList, HitElement[] result, int n) throws IOException{
		super.getHitElement(rankInfoList, result, n);
		for (int i = 0; i < n; i++) {
			RankInfo ri = rankInfoList[i];
			bundleIndexRef.read(ri.docNo());
			if(!isBundleKeyEmpty(data)) {
				BytesRef bundleKey = data.duplicate();
				result[i].setBundleKey(bundleKey);
			}
		}
	}

	private boolean isBundleKeyEmpty(BytesRef bundleKey) {
		for(int i = bundleKey.offset; i < bundleKey.length; i++) {
			if(bundleKey.bytes[i] != 0) {
				return false;
			}
		}
		return true;
	}
}























