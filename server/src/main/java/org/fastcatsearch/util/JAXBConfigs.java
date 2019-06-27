package org.fastcatsearch.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.io.Writer;

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
	private static final String DEFAULT_CHARSET = "UTF-8";
	public static <T> T readConfig(File file, Class<T> jaxbConfigClass) throws JAXBException {
		logger.debug("readConfig file >> {}, {}", file.getAbsolutePath(), file.exists());
		
		if(!file.exists()){
			return null;
		}
		
		InputStream is = null;
		try{
			is = new FileInputStream(file);
			logger.debug("read config file={}, {}", file.getName(), is);
			T config = readConfig(is, jaxbConfigClass);
//			logger.debug("read config {}, {}", config, file.getName());
			return config;
		}catch(Exception e){
			logger.error("JAXBConfig file error "+file.getAbsolutePath(), e);
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
	
	public static <T> T readConfig(Reader reader, Class<T> jaxbConfigClass) throws JAXBException {
		if(reader == null){
			return null;
		}
		JAXBContext context = JAXBContext.newInstance(jaxbConfigClass);
		
		Unmarshaller unmarshaller = context.createUnmarshaller();
		
		T config = (T) unmarshaller.unmarshal(reader);
		return config;
	}
	
	public static <T> T readConfig(InputStream is, Class<T> jaxbConfigClass) throws JAXBException {
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
	
	
	public synchronized static <T> void writeConfig(File file, Object jaxbConfig, Class<T> jaxbConfigClass) throws JAXBException {
		logger.debug("writeConfig >> {}, {}", file.getAbsolutePath(), jaxbConfigClass);
//		for(StackTraceElement e : Thread.currentThread().getStackTrace()){
//			logger.debug("> {}", e);
//		}
		Writer writer = null;
		try{
			if (!file.exists()) {
				logger.debug("create {}", file.getAbsolutePath());
				file.createNewFile();
			}
			
			writer = new OutputStreamWriter(new FileOutputStream(file), DEFAULT_CHARSET);
			writeRawConfig(writer, jaxbConfig, jaxbConfigClass);
		}catch(IOException e){
			throw new JAXBException(e);
		}finally{
			if(writer != null){
				try {
					writer.close();
				} catch (IOException ignore) {
				}
			}
		}
		
	}
	public static <T> void writeRawConfig(OutputStream os, Object jaxbConfig, Class<T> jaxbConfigClass) throws JAXBException {
		writeRawConfig(os, jaxbConfig, jaxbConfigClass, false);
	}
	public synchronized static <T> void writeRawConfig(OutputStream os, Object jaxbConfig, Class<T> jaxbConfigClass, boolean removeXmlDeclaration) throws JAXBException {
		JAXBContext context = JAXBContext.newInstance(jaxbConfigClass);
		Marshaller marshaller = context.createMarshaller();
		marshaller.setProperty(Marshaller.JAXB_ENCODING, DEFAULT_CHARSET);
		marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
//		logger.debug("removeXmlDeclaration!! {}", removeXmlDeclaration);
		if(removeXmlDeclaration){
			marshaller.setProperty(Marshaller.JAXB_FRAGMENT, Boolean.TRUE);
//			marshaller.setProperty("com.sun.xml.bind.xmlDeclaration", Boolean.FALSE);
		}
		
		Writer writer = null;
		try {
			writer = new OutputStreamWriter(os, DEFAULT_CHARSET);
			marshaller.marshal(jaxbConfig, writer);
		} catch (UnsupportedEncodingException e) {
			throw new JAXBException(e);
		}
	}
	
	public static <T> void writeRawConfig(Writer writer, Object jaxbConfig, Class<T> jaxbConfigClass) throws JAXBException {
		writeRawConfig(writer, jaxbConfig, jaxbConfigClass, false);
	}
	public synchronized static <T> void writeRawConfig(Writer writer, Object jaxbConfig, Class<T> jaxbConfigClass, boolean removeXmlDeclaration) throws JAXBException {
		JAXBContext context = JAXBContext.newInstance(jaxbConfigClass);
		Marshaller marshaller = context.createMarshaller();
		marshaller.setProperty(Marshaller.JAXB_ENCODING, DEFAULT_CHARSET);
		marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
//		logger.debug("removeXmlDeclaration!! {}", removeXmlDeclaration);
		if(removeXmlDeclaration){
			marshaller.setProperty(Marshaller.JAXB_FRAGMENT, Boolean.TRUE);
//			marshaller.setProperty("com.sun.xml.bind.xmlDeclaration", Boolean.FALSE);
		}
		marshaller.marshal(jaxbConfig, writer);
	}
	
	public static <T> void writeTo(DataOutput os, Object jaxbConfig, Class<T> jaxbConfigClass) throws JAXBException {
		try{
			BytesDataOutput bytesOutput = new BytesDataOutput();
			writeRawConfig(bytesOutput, jaxbConfig, jaxbConfigClass);
			int byteSize = (int) bytesOutput.position();
			os.writeVInt(byteSize);
			os.writeBytes(bytesOutput.array(), 0, byteSize);
		}catch(IOException e){
			throw new JAXBException(e);
		}
	}
}
