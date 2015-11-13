package org.fastcatsearch.ir.group.value;

import org.fastcatsearch.ir.group.GroupingValue;

public class StringGroupingValue extends GroupingValue<String> {

	public StringGroupingValue(String i) {
		value = i;
	}

	@Override
	public void add(String i) {
		if (value == null) {
			value = (String) i;
		} else {
			value = (String) value + (String) i;
		}
	}

	@Override
	public void setIfMaxValue(String obj) {
		if (value == null) {
			value = obj;
		} else {
			// 입력값이 기존 값보다 크면 교체한다.
			if(obj.compareTo(value) > 0) {
				value = obj;
			}
		}
	}

	@Override
	public void setIfMinValue(String obj) {
		if (value == null) {
			value = obj;
		} else {
			// 입력값이 기존 값보다 작으면 교체한다.
			if(obj.compareTo(value) < 0) {
				value = obj;
			}
		}
	}
	
	@Override
	public void increment() { }

	@Override
	public int compareTo(GroupingValue<String> o) {
		return value.compareTo(o.get());
	}

	public static StringGroupingValue[] createList(int groupKeySize) {
		StringGroupingValue[] list = new StringGroupingValue[groupKeySize];
		for (int i = 0; i < groupKeySize; i++) {
			list[i] = new StringGroupingValue("");
		}
		return list;
	}
	
	@Override
	public boolean isEmpty() {
		return value == null || value.length() == 0;
	}


}
