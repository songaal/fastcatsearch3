package org.fastcatsearch.ir.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import org.fastcatsearch.ir.io.BytesDataInput;
import org.fastcatsearch.ir.io.BytesDataOutput;
import org.fastcatsearch.ir.io.DataInput;
import org.fastcatsearch.ir.io.DataOutput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JAXBConfigs {
	private static final Logger logger = LoggerFactory.getLogger(JAXBConfigs.class);
	
	public static <T> T readConfig(File file, Class<T> jaxbConfigClass) throws JAXBException {
		if(!file.exists()){
			return null;
		}
		
		InputStream is = null;
		try{
			is = new FileInputStream(file);
			T config = readConfig(is, jaxbConfigClass);
			logger.debug("read config {}, {}", config, file.getName());
			return config;
		}catch(IOException e){
			logger.error("JAXBConfig file io error "+file.getAbsolutePath(), e);
			throw new JAXBException(e);
		}finally{
			if(is != null){
				try {
					is.close();
				} catch (IOException ignore) {
				}
			}
		}
	}
	
	protected static <T> T readConfig(InputStream is, Class<T> jaxbConfigClass) throws JAXBException {
		if(is == null){
			return null;
		}
		JAXBContext context = JAXBContext.newInstance(jaxbConfigClass);
		
		Unmarshaller unmarshaller = context.createUnmarshaller();

		T config = (T) unmarshaller.unmarshal(is);
		return config;
	}
	
	public static <T> T readFrom(DataInput is, Class<T> jaxbConfigClass) throws JAXBException {
		if(is == null){
			return null;
		}
		int size = 0;
		try {
			size = is.readVInt();
		} catch (IOException e) {
			throw new JAXBException(e);
		}
		byte[] array = new byte[size];
		try {
			is.readBytes(array, 0, size);
		} catch (IOException e) {
			throw new JAXBException(e);
		}
		
		BytesDataInput bytesInput = new BytesDataInput(array, 0, size);
		
		return readConfig(bytesInput, jaxbConfigClass);
	}
	
	
	public static <T> void writeConfig(File file, Object jaxbConfig, Class<T> jaxbConfigClass) throws JAXBException {
		OutputStream os = null;
		try{
			os = new FileOutputStream(file);
			writeConfig(os, jaxbConfig, jaxbConfigClass);
		}catch(IOException e){
			throw new JAXBException(e);
		}finally{
			if(os != null){
				try {
					os.close();
				} catch (IOException ignore) {
				}
			}
		}
		
	}
	protected static <T> void writeConfig(OutputStream os, Object jaxbConfig, Class<T> jaxbConfigClass) throws JAXBException {
		JAXBContext context = JAXBContext.newInstance(jaxbConfigClass);
		Marshaller marshaller = context.createMarshaller();
		marshaller.setProperty(Marshaller.JAXB_ENCODING, "UTF-8");
		marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
		marshaller.marshal(jaxbConfig, os);
	}
	
	public static <T> void writeTo(DataOutput os, Object jaxbConfig, Class<T> jaxbConfigClass) throws JAXBException {
		try{
			BytesDataOutput bytesOutput = new BytesDataOutput();
			writeConfig(bytesOutput, jaxbConfig, jaxbConfigClass);
			int byteSize = (int) bytesOutput.position();
			os.writeVInt(byteSize);
			os.writeBytes(bytesOutput.array(), 0, byteSize);
		}catch(IOException e){
			throw new JAXBException(e);
		}
	}
}
