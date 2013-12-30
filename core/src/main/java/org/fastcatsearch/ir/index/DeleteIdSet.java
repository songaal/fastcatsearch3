package org.fastcatsearch.ir.index;

import java.util.HashSet;

import org.fastcatsearch.ir.common.IRException;

public class DeleteIdSet extends HashSet<PrimaryKeys> {

	private static final long serialVersionUID = -518125101167212596L;

	private int keySize;
	
	public DeleteIdSet(int keySize){
		this.keySize = keySize;
	}
	
	public void add(String... keys) throws IRException{
		if(keySize != keys.length){
			throw new IRException("id field갯수가 일치하지 않습니다.");
		}
		
		add(new PrimaryKeys(keys));
	}
	
	public int keySize(){
		return keySize;
	}
}
