package org.fastcatsearch.ir.group.value;

import org.fastcatsearch.ir.group.GroupingValue;

public class LongGroupingValue extends GroupingValue<Long> {

	public LongGroupingValue(Long i) {
		value = i;
	}

	@Override
	public void add(Long i) {
		if (value == null) {
			value = (Long) i;
		} else {
			value = (Long) value + (Long) i;
		}
	}

	@Override
	public void setIfMaxValue(Long obj) {
		if (value == null) {
			value = obj;
		} else {
			long o = obj;
			long r = (Long) value;
			// 입력값이 기존 값보다 크면 교체한다.
			if (o > r) {
				value = o;
			}
		}
	}

	@Override
	public void setIfMinValue(Long obj) {
		if (value == null) {
			value = obj;
		} else {
			long o = obj;
			long r = (Long) value;
			// 입력값이 기존 값보다 작으면 교체한다.
			if (o < r) {
				value = o;
			}
		}
	}

	@Override
	public void increment() {
		value++;
	}

	@Override
	public int compareTo(GroupingValue<Long> o) {
		long cmp = value - (Long) o.get();
		return cmp == 0L ? 0 : (cmp < 0L ? -1 : 1);
	}

	public static LongGroupingValue[] createList(int groupKeySize) {
		LongGroupingValue[] list = new LongGroupingValue[groupKeySize];
		for (int i = 0; i < groupKeySize; i++) {
			list[i] = new LongGroupingValue(0L);
		}
		return list;
	}

	@Override
	public boolean isEmpty() {
		return value == null;
	}
	
}
