package org.fastcatsearch.ir.settings;

import javax.xml.bind.annotation.adapters.XmlAdapter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * value가 없는 attribute는 xml파일로 marshal시 출력하지 않도록 하여, xml을 깔끔하게 정리한다.  
 * */
public class OptionalStringAdapter extends XmlAdapter<String, String> {
	protected static Logger logger = LoggerFactory.getLogger(OptionalStringAdapter.class);
	
	@Override
	public String unmarshal(String v) throws Exception {
		return v;
	}

	@Override
	public String marshal(String v) throws Exception {
		if(v == null || v.length() == 0){
			return null;
		}
		return v;
	}

}
