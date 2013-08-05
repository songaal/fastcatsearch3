package org.fastcatsearch.ir.settings;

import javax.xml.bind.annotation.adapters.XmlAdapter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 값이 0 이상일 경우만 출력함.
 * */
public class OptionalIntPositiveAdapter extends XmlAdapter<Integer, Integer> {
	protected static Logger logger = LoggerFactory.getLogger(OptionalIntPositiveAdapter.class);
	
	@Override
	public Integer unmarshal(Integer v) throws Exception {
		return v;
	}

	@Override
	public Integer marshal(Integer v) throws Exception {
		if(v != null && v > 0){
			return v;
		}
		return null;
	}

}
