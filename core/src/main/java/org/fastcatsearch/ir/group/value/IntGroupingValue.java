package org.fastcatsearch.ir.group.value;

import java.util.List;

import org.fastcatsearch.ir.group.GroupingValue;

public class IntGroupingValue extends GroupingValue<Integer> {

	public IntGroupingValue(Integer i) {
		value = i;
	}

	@Override
	public void add(Integer i) {
		if (value == null) {
			value = (Integer) i;
		} else {
			value = (Integer) value + (Integer) i;
		}
	}

	@Override
	public void setIfMax(Integer obj) {
		if (value == null) {
			value = obj;
		} else {
			int o = obj;
			int r = (Integer) value;
			// 입력값이 기존 값보다 크면 교체한다.
			if (o > r) {
				value = o;
			}
		}
	}

	@Override
	public void increment() {
		value++;
	}

	@Override
	public int compareTo(GroupingValue<Integer> o) {
		return value - (Integer) o.get();
	}

	public static IntGroupingValue[] createList(int groupKeySize) {
		return new IntGroupingValue[groupKeySize];
	}

}
