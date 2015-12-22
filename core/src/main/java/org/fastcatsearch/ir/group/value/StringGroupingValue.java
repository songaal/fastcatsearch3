package org.fastcatsearch.ir.group.value;

import org.fastcatsearch.ir.group.GroupFunctionType;
import org.fastcatsearch.ir.group.GroupingValue;

public class StringGroupingValue extends GroupingValue<String> {

    public StringGroupingValue(GroupFunctionType type) {
        super(type);
    }

	public StringGroupingValue(String i, GroupFunctionType type) {
		super(i, type);
	}

	@Override
	public void add(String i) {
		if (value == null) {
			value = i;
		} else {
            //이미 셋팅되어 있다면 그대로 둔다
            //do nothing
		}
	}

	@Override
	public void increment() { }

	@Override
	public int compareTo(GroupingValue<String> o) {
		return value.compareTo(o.get());
	}

	public static StringGroupingValue[] createList(int groupKeySize, GroupFunctionType type) {
		StringGroupingValue[] list = new StringGroupingValue[groupKeySize];
		for (int i = 0; i < groupKeySize; i++) {
			list[i] = new StringGroupingValue(type);
		}
		return list;
	}
	
	@Override
	public boolean isEmpty() {
		return value == null || value.length() == 0;
	}


}
