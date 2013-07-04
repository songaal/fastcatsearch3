/*
 * Copyright (c) 2013 Websquared, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 * 
 * Contributors:
 *     swsong - initial API and implementation
 */

package org.fastcatsearch.statistics;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 두개의 슬롯을 번갈아 가면서 키워드를 채워넣는다.
 * 한 주기 동안 키워드를 채우고 다음주기에는 반대편 슬롯에 키워드를 채운다.
 * StatisticsInfoService에서는 선택된 슬롯의 키워드를 가져와서 웹페이지로 리스트를 리턴해준다. 
 * @author swsong
 *
 */
public class SearchKeywordCache {
	private static Logger logger = LoggerFactory.getLogger(SearchKeywordCache.class);
	private static int MAX_SIZE = 200; //초당 200개이상의 검색이 들어올수 없다.
	private String[][] keywordList;
	private int[] count; //슬롯별 키워드 갯수
	private int slot;
	private int prevSlot = 1;
	
	public SearchKeywordCache(){
		count = new int[2];
		keywordList = new String[2][];
		//두개의 슬롯을 초기화한다.
		keywordList[0] = new String[MAX_SIZE];
		keywordList[1] = new String[MAX_SIZE];
	}
	
	//반대편 슬롯으로 스위치하고, 번호를 리턴한다.
	public void switchSlot(){
		prevSlot = slot;
		if(slot == 0){
			slot = 1;
		}else{
			slot = 0;
		}
		count[slot] = 0;
	}
	
	//이전 주기에 쌓여있는 리스트를 반환 
	public String[] getKeywordList(){
		return keywordList[prevSlot];
	}
	//이전 주기의 리스트갯수를 반환.
	public int getCount(){
		return count[prevSlot];
	}
	
	public void addKeyword(String keyword){
		//MAX_SIZE를 넘어서면 무시한다. 
		if(count[slot] >= MAX_SIZE){
			logger.warn("SearchKeywordCache의 SIZE가 부족합니다. Ignored keyword=>"+keyword);
			return;
		}
		//이전 키워드와 같으면 무시한다.
		if(count[slot] > 0){
			if(keywordList[slot][count[slot] - 1].equals(keyword)){
				return;
			}
		}
		logger.debug("Insert "+keyword+" to Slot-"+slot);
		keywordList[slot][count[slot]++] = keyword;
	}
	
	
}
