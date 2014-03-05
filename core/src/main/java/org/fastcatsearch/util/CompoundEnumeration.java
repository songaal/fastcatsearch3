package org.fastcatsearch.util;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.NoSuchElementException;

public class CompoundEnumeration<E> implements Enumeration<E> {

	private List<Enumeration<E>> enumerationList;
	private List<List<E>> listList;
	private int index;
	private int index2;

	public CompoundEnumeration() {
		enumerationList = new ArrayList<Enumeration<E>>();
		listList = new ArrayList<List<E>>();
	}

	public void add(Enumeration<E> e) {
		enumerationList.add(e);
	}
	
	public void add(List<E> e) {
		listList.add(e);
	}

	@Override
	public boolean hasMoreElements() {
		while (index < enumerationList.size()) {
			if (enumerationList.get(index) != null && enumerationList.get(index).hasMoreElements()) {
				return true;
			}
			index++;
		}
		
		while (index2 < listList.size()) {
			if (listList.get(index2) !=null && listList.get(index2).size() > 0) {
				return true;
			}
		}
		
		return false;
	}

	@Override
	public E nextElement() {
		
		if (!hasMoreElements()) {
			throw new NoSuchElementException();
		}
		
		if(index < enumerationList.size()) {
			return (E) enumerationList.get(index).nextElement();
		}
		
		if(index2 < listList.size()) {
			List<E> list = listList.get(index2);
			return list.remove(0);
		}
		return null;
	}
}
