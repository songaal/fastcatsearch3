package org.fastcatsearch.ir.config;

import static org.junit.Assert.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import javax.xml.bind.JAXBException;

import org.fastcatsearch.common.BytesArray;
import org.fastcatsearch.common.io.BytesStreamInput;
import org.fastcatsearch.common.io.BytesStreamOutput;
import org.fastcatsearch.ir.io.BytesDataInput;
import org.fastcatsearch.ir.io.BytesDataOutput;
import org.fastcatsearch.util.JAXBConfigs;
import org.junit.Test;

public class ConfigFileTransferTest {

	@Test
	public void testMulti() throws IOException, JAXBException {
		
		BytesDataOutput output = new BytesDataOutput();
//		BytesStreamOutput output = new BytesStreamOutput();
		
		
		CollectionConfig collectionConfig =  new CollectionConfig();
		DataSourceConfig dataSourceConfig = new DataSourceConfig();
		
		JAXBConfigs.writeTo(output, collectionConfig, CollectionConfig.class);
		JAXBConfigs.writeTo(output, dataSourceConfig, DataSourceConfig.class);
		
//		printData(output.array(), 0, (int) output.position());
		BytesDataInput input = new BytesDataInput(output.array(), 0, (int) output.position());
//		BytesStreamInput input = new BytesStreamInput(output.bytesReference());
		
		CollectionConfig collectionConfig2 = JAXBConfigs.readFrom(input, CollectionConfig.class);
		DataSourceConfig dataSourceConfig2 = JAXBConfigs.readFrom(input, DataSourceConfig.class);
		
	}

	private void printData(byte[] array, int start, int size) {
		StringBuffer sb = new StringBuffer();
		for (int j = start; j < size; j++) {
			sb.append((char)array[j]);
		}
		System.out.println(sb.toString());
	}

}
