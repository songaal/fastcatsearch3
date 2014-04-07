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

/**
 * RankInfo는 포스팅을 검색한 결과로 OperatedClause의 next메소드를 통해 만들어 진다. RankInfo는 docNo와
 * score정보만 가지고 있으며 SortGenerator를 통해 소트필드정보를 가지는 HitElement를 만들게 된다.
 * 
 * @author sangwook.song
 * 
 */
public class RankInfo {
	private int docNo;
	private int score;
	private int hit; // 매칭횟수.

	public RankInfo() {
	}

	public void init(int docNo, int score) {
		init(docNo, score, 1);
	}

	public void init(int docNo, int score, int hit) {
		this.docNo = docNo;
		this.score = score;
		this.hit = hit;
	}

	public int docNo() {
		return docNo;
	}

	public int score() {
		return score;
	}

	public int hit() {
		return hit;
	}

	public void addScore(float add) {
		score += add;
	}

	public void addHit(int add) {
		hit += add;
	}

	public void multiplyScore(float mul) {
		score *= mul;
	}

	public void score(int score) {
		this.score = score;
	}

	public void hit(int hit) {
		this.hit = hit;
	}

	public String toString() {
		return "docNo=" + docNo + ",score=" + score + ",hit=" + hit;
	}
}
