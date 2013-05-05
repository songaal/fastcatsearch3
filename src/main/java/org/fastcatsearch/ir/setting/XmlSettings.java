package org.fastcatsearch.ir.setting;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Map;

import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.fastcatsearch.ir.common.SettingException;
import org.fastcatsearch.ir.util.XmlUtil;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

@Deprecated
public class XmlSettings implements Settings {
	Document document;
	
	public XmlSettings(String filepath) {
		try {
			InputStream is = new FileInputStream(filepath);
			document = XmlUtil.parse(is);
			
			
		} catch (Exception e) {

		}
	}

	@Override
	public Settings getComponentSettings(Class<?> component) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Settings getByPrefix(String prefix) {
		try {
			NodeList nodeList = (NodeList) XPathFactory.newInstance().newXPath().evaluate(prefix, document, XPathConstants.NODESET);
			
		} catch (XPathExpressionException e) {
			e.printStackTrace();
		}
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Map<String, String> getAsMap() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String get(String setting) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String get(String setting, String defaultValue) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public float getAsFloat(String setting, float defaultValue) throws SettingException {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public double getAsDouble(String setting, double defaultValue) throws SettingException {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int getAsInt(String setting, int defaultValue) throws SettingException {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public long getAsLong(String setting, long defaultValue) throws SettingException {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public boolean getAsBoolean(String setting, boolean defaultValue) throws SettingException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public long getAsBytesSize(String setting, long defaultValue) throws SettingException {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public String[] getAsArray(String settingPrefix) throws SettingException {
		// TODO Auto-generated method stub
		return null;
	}

}
