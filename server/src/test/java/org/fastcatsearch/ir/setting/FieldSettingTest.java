package org.fastcatsearch.ir.setting;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.StringReader;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import org.fastcatsearch.ir.settings.FieldSetting;
import org.junit.Test;

public class FieldSettingTest {

	@Test
	public void testSettingMarshall() throws JAXBException {
		
		FieldSetting setting = new FieldSetting("title", "타이틀", FieldSetting.Type.STRING);
		setting.setRemoveTag(true);
		setting.setStore(false);
		JAXBContext context = JAXBContext.newInstance(setting.getClass());
		
		Marshaller marshaller = context.createMarshaller();
		marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);

		marshaller.marshal(setting, System.out);
	}
	
	@Test
	public void testSettingUnmarshall() throws JAXBException {
		String fieldSettingXML = 
				"<field id=\"title\" type=\"ACHAR\" name=\"문서제목\" size=\"100\" store=\"\" removeTag=\"true\" modify=\"\" multiValue=\"false\" multiValueDelimiter=\",\" />";
		
		JAXBContext context = JAXBContext.newInstance(FieldSetting.class);
		
		Unmarshaller unmarshaller = context.createUnmarshaller();

		FieldSetting setting = (FieldSetting) unmarshaller.unmarshal(new StringReader(fieldSettingXML));
		assertTrue(setting.isRemoveTag());
		assertEquals(100, setting.getSize().intValue());
		assertTrue(setting.isStore());
		assertFalse(setting.isModify());
	}

}
