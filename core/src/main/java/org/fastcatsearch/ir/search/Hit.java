package org.fastcatsearch.ir.search;

import org.fastcatsearch.ir.group.GroupData;
import org.fastcatsearch.ir.group.GroupHit;
import org.fastcatsearch.ir.io.FixedHitStack;

public class Hit extends GroupHit {
	private FixedHitStack hitStack;
	
	public Hit(FixedHitStack hitStack, GroupData groupData, int totalCount){
		super(groupData, totalCount);
		this.hitStack = hitStack;
	}
	
	public FixedHitStack hitStack(){
		return hitStack;
	}
	
}
