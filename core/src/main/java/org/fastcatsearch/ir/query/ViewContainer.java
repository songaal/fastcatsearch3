package org.fastcatsearch.ir.query;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ViewContainer {
	
	public List<View> container;
	
	public Map<String,List<View>> map;

	public ViewContainer(int length) {
		container = new ArrayList<View>(length);
		map = new HashMap<String,List<View>>();
	}
	public ViewContainer() {
		container = new ArrayList<View>();
		map = new HashMap<String,List<View>>();
	}
	
	List<View> viewElements = null;
	public void add(View view) {
		if(view!=null) {
			if(!map.containsKey(view.fieldId())) {
				viewElements = new ArrayList<View>();
				viewElements.add(view);
				map.put(view.fieldId(), viewElements);
			} else {
				viewElements = map.get(view.fieldId());
				if(!viewElements.contains(view)) {
					viewElements.add(view);
				}
			}
		}
		if(!container.contains(view)) {
			container.add(view);
		}
	}
	
	public View get(int inx) {
		return container.get(inx);
	}
	
	public void setSummarized(String fieldId,
			boolean summarized) {
		if(map.containsKey(fieldId)) {
			for(View view : map.get(fieldId)) {
				//positive only setting
				if(summarized) {
					view.setHighlighted(summarized);
				}
			}
		}
	}
	
	public void setHighlighted(String fieldId,
			boolean highlighted) {
		for(View view : map.get(fieldId)) {
			//positive only setting
			if(view.isHighlighted()) {
				view.setHighlighted(highlighted);
			}
		}
	}
	
	public int size() {
		if(container!=null) {
			return container.size();
		}
		return 0;
	}
}