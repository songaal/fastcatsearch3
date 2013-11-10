package org.fastcatsearch.ir.misc.keywordSuggest;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.fastcatsearch.ir.query.RankInfo;
import org.fastcatsearch.util.lang.ko.HangulUtil;

/*
 * dataArray의 일부를 memoryIndex 에 올려서 binary search 후 가까운 위치부터  dataArray를 순차적으로 조사한다.
 * exact는 정확히 매칭하면 리턴하고, prefix는 startsWith로 검사후 결과에 add해준후 최종리턴.  
 * */
public class SearchMemoryIndex {
	
	List<TermPointer> memoryIndex = new ArrayList<TermPointer>();
	//List<String> dataArray = new ArrayList<String>();
	
	List<String> list = new ArrayList<String>();
	
	int memoryFactor = 1;
	
	public SearchMemoryIndex(){
		
	}
	
	public void add(String keyword){
		//tmp map에 저장, 계속확장.
		String decomposed = HangulUtil.decomposeHangul(keyword);
		System.out.println("# " + keyword + " > " + decomposed);
		list.add(decomposed);
		
		String[] eojeolList = keyword.split("\\s+");
		for (int i = 0; i < eojeolList.length; i++) {
			String decomposedEojeol = HangulUtil.decomposeHangul(eojeolList[i]);
			System.out.println("# " + eojeolList[i] + " > " + decomposedEojeol);
			list.add(decomposedEojeol);
		}
		//keyword 별 id부여.
		
	}
	
	public void makeIndex(){
		
		//1. dataArray 정렬하여 dataArray에 기록.
		Collections.sort(list);
		
		System.out.println("----------------------");
		
		//2. 1/n을 memoryIndex 로 올림.
		for (int i = 0; i < list.size(); i++) {
			System.out.println(list.get(i));
			if(i % memoryFactor == 0){
				memoryIndex.add(new TermPointer(list.get(i), i));
			}
		}
		
		
		
	}
	
	
	public boolean binsearch(String singleTerm, TermPointer termPointer){
		if(memoryIndex.size() == 0)
			return false;
		
    	int left = 0;
    	int right = memoryIndex.size() - 1;
    	int mid = -1;
    	
    	boolean found = false;
    	
    	while(left <= right){
    		mid = (left + right) / 2;

    		int cmp = memoryIndex.get(mid).term.compareTo(singleTerm);
    		
    		if(cmp == 0){
    			found = true;
    			break;
    		}else if(cmp < 0){
    			left = mid + 1;
    		}else{
    			right = mid - 1;
    		}
    	}
    	
    	if(found){
    		termPointer.term = singleTerm;
    		termPointer.pointer = mid;
    		return true;
    	}
    	
		//mid = Min(mid, right)
    	mid = right < mid ? right : mid;
    	
    	if(mid == -1)
    		mid = 0;
    	
    	if(mid > 0 && memoryIndex.get(mid).term.compareTo(singleTerm) > 0){
    		mid--;
    	}
    	if(mid < 0) mid = 0;
    	
    	termPointer.term = memoryIndex.get(mid).term;
		termPointer.pointer = memoryIndex.get(mid).pointer;
		return false;
	}
	

	public List<String> prefixSearch(String keyword){
		
		
		keyword = HangulUtil.decomposeHangul(keyword);
		
		
		List<String> result = null;
		TermPointer termPointer = new TermPointer();
		boolean found = binsearch(keyword, termPointer);
		//prefix search는 found와 상관없이 진행.
		int pointer = termPointer.pointer;
		System.out.println("check "+keyword+ ", found = "+found+", pointer="+pointer);
		if(pointer != -1){
			//최근 위치찾음. 순차검색시작.
			for(int i = pointer; i< list.size() ;i++){
				String target = list.get(i);
				int compares = comparePrefix(keyword, target);
				System.out.println("target ="+target + ", keyword = " +keyword+ ", compares="+compares);
				
				if(compares == 0){
					//일치.
					if(result == null){
						result = new ArrayList<String>();
					}
					result.add(target);
					continue;
				}
				
				if(target.startsWith(keyword)){
					if(result == null){
						result = new ArrayList<String>();
					}
					result.add(target);
				}
				
				
				if(compares < 0){
					break;
				}
				
				
				
			}
		}
		return result;
	}
	
	private int comparePrefix(String keyword, String target) {
		if(keyword.length() <= target.length() ){
			for(int i= 0; i < keyword.length() ; i++){
				int d = keyword.charAt(i) - target.charAt(i);
				if(d != 0){
					return d;
				}
			}
		}else{
			return 1;
		}
		
		return 0;
	}

	public String exactSearch(String keyword){
		
		
		return null;
	}
	
	public static class TermPointer {//implements Comparable<TermPointer> { 
		String term;
		int pointer;
		public TermPointer(){
		}
		
		public TermPointer(String term, int pointer){
			this.term = term;
			this.pointer = pointer;
		}
	}
	
}
