package org.fastcatsearch.ir.settings;

import javax.xml.bind.annotation.adapters.XmlAdapter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 기본 boolean값이 false이며, true일 경우만 출력함.
 * */
public class OptionalBooleanFalseAdapter extends XmlAdapter<Boolean, Boolean> {
	protected static Logger logger = LoggerFactory.getLogger(OptionalBooleanFalseAdapter.class);
	
	@Override
	public Boolean unmarshal(Boolean v) throws Exception {
		return v;
	}

	@Override
	public Boolean marshal(Boolean v) throws Exception {
		if(v != null && v.booleanValue()){
			return v;
		}
		return null;
	}

}
