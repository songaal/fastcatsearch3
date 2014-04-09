package org.fastcatsearch.ir.search;

import org.fastcatsearch.ir.group.GroupsData;
import org.fastcatsearch.ir.group.GroupHit;
import org.fastcatsearch.ir.io.FixedHitStack;
import org.fastcatsearch.ir.query.HighlightInfo;

public class Hit extends GroupHit {
	private FixedHitStack hitStack;
	private HighlightInfo highlightInfo;
	private Explanation explanation;
	
	public Hit(FixedHitStack hitStack, GroupsData groupData, int totalCount, HighlightInfo highlightInfo){
		this(hitStack, groupData, totalCount, highlightInfo, null);
	}
	public Hit(FixedHitStack hitStack, GroupsData groupData, int totalCount, HighlightInfo highlightInfo, Explanation explanation){
		super(groupData, totalCount);
		this.hitStack = hitStack;
		this.highlightInfo = highlightInfo;
		this.explanation = explanation;
	}
	
	public FixedHitStack hitStack(){
		return hitStack;
	}
	
	public HighlightInfo highlightInfo(){
		return highlightInfo;
	}
	
	public Explanation explanation(){
		return explanation;
	}
}
