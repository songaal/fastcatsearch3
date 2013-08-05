package org.fastcatsearch.ir.settings;

import javax.xml.bind.annotation.adapters.XmlAdapter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 기본 boolean값이 true이며, false일 경우만 출력함.
 * */
public class OptionalBooleanTrueAdapter extends XmlAdapter<Boolean, Boolean> {
	protected static Logger logger = LoggerFactory.getLogger(OptionalBooleanTrueAdapter.class);
	
	@Override
	public Boolean unmarshal(Boolean v) throws Exception {
		return v;
	}

	@Override
	public Boolean marshal(Boolean v) throws Exception {
		if(v == null || !v.booleanValue()){
			return new Boolean(false);
		}
		return null;
	}

}
