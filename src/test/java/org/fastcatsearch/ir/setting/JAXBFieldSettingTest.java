package org.fastcatsearch.ir.setting;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.StringReader;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import org.fastcatsearch.ir.settings.FieldSetting;
import org.junit.Test;

public class JAXBFieldSettingTest {

	@Test
	public void testSettingMarshall() throws JAXBException {
		
		FieldSetting setting = new FieldSetting("title", "타이틀", FieldSetting.Type.UCHAR);
		JAXBContext context = JAXBContext.newInstance(setting.getClass());
		
		Marshaller marshaller = context.createMarshaller();
		marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);

		marshaller.marshal(setting, System.out);
	}
	
	@Test
	public void testSettingUnmarshall() throws JAXBException {
		String fieldSettingXML = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n" + 
				"<field id=\"title\" type=\"ACHAR\" name=\"문서제목\" size=\"100\" primary=\"false\" store=\"false\" remove-tag=\"true\" virtual=\"false\" modify=\"false\" multi-value=\"false\" multi-value-delimiter=\"0\" multi-value-max-count=\"0\"/>";
		
		JAXBContext context = JAXBContext.newInstance(FieldSetting.class);
		
		Unmarshaller unmarshaller = context.createUnmarshaller();
//		unmarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);

		FieldSetting setting = (FieldSetting) unmarshaller.unmarshal(new StringReader(fieldSettingXML));
		assertTrue(setting.isRemoveTag());
		assertEquals(100, setting.getSize());
	}

}
