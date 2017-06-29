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


public class Row {
	private static char[] EMPTY_ROW = new char[0];
	private char[][] row;
	private int fieldCount;
	private int score;
    private int hit;
	private float distance;
	private int filterMatchOrder;

	//원문조회기능에서필요.
	private boolean isDeleted;
	private String rowTag;

	public Row(){ }
	
	public Row(int fieldCount){ 
		this.fieldCount = fieldCount;
		row = new char[fieldCount][];
		isDeleted = false;
	}
	
	public Row(char[][] row){
		this.row = row;
		this.fieldCount = row.length;
	}
	
	public String toString(){
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < fieldCount; i++) {
			sb.append(new String(row[i])+":"+score+" ,");
		}
		return sb.toString();
	}
	
	public void put(int i, char[] fieldData){
		if(fieldData == null){
			row[i] = EMPTY_ROW;
		}else{
			row[i] = fieldData;
		}
	}
	
	public char[] get(int i){
		return row[i];
	}
	
	public int getFieldCount(){
		return fieldCount;
	}

	public boolean isDeleted() {
		return isDeleted;
	}

	public void setDeleted(boolean isDeleted) {
		this.isDeleted = isDeleted;
	}
	
	public String getRowTag() {
		return rowTag;
	}

	public void setRowTag(String rowTag) {
		this.rowTag = rowTag;
	}

	public int getScore(){
		return score;
	}
	
	public void setScore(int score) {
		this.score = score;
	}

    public int getHit() {
        return hit;
    }

    public void setHit(int hit) {
        this.hit = hit;
    }

	public float getDistance(){
		return distance;
	}

	public void setDistance(float distance) {
		this.distance = distance;
	}

	public int getFilterMatchOrder(){
		return filterMatchOrder;
	}

	public void setFilterMatchOrder(int filterMatchOrder) {
		this.filterMatchOrder = filterMatchOrder;
	}
}

