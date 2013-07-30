package org.fastcatsearch.ir.search;

import org.fastcatsearch.ir.group.GroupsData;
import org.fastcatsearch.ir.group.GroupHit;
import org.fastcatsearch.ir.io.FixedHitStack;
import org.fastcatsearch.ir.query.HighlightInfo;

public class Hit extends GroupHit {
	private FixedHitStack hitStack;
	private HighlightInfo highlightInfo;
	
	public Hit(FixedHitStack hitStack, GroupsData groupData, int totalCount, HighlightInfo highlightInfo){
		super(groupData, totalCount);
		this.hitStack = hitStack;
		this.highlightInfo = highlightInfo;
	}
	
	public FixedHitStack hitStack(){
		return hitStack;
	}
	
	public HighlightInfo highlightInfo(){
		return highlightInfo;
	}
}
