package org.fastcatsearch.ir.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JAXBConfigs {
	private static final Logger logger = LoggerFactory.getLogger(JAXBConfigs.class);
	
	public static <T> T readConfig(File file, Class<T> jaxbConfigClass){
		InputStream is = null;
		try{
			is = new FileInputStream(file);
			return readConfig(is, jaxbConfigClass);
		}catch(Exception e){
			logger.error("JAXBConfig file read error", e);
		}finally{
			if(is != null){
				try {
					is.close();
				} catch (IOException ignore) {
				}
			}
		}
		return null;
	}
	
	public static <T> T readConfig(InputStream is, Class<T> jaxbConfigClass) {
		try{
			JAXBContext context = JAXBContext.newInstance(jaxbConfigClass);
			
			Unmarshaller unmarshaller = context.createUnmarshaller();
	
			T config = (T) unmarshaller.unmarshal(is);
			return config;
		}catch(Exception e){
			logger.error("JAXBConfig file read error", e);
		}
		return null;
	}
	
	
	public static <T> void writeConfig(File file, Object jaxbConfig, Class<T> jaxbConfigClass){
		OutputStream os = null;
		try{
			os = new FileOutputStream(file);
			writeConfig(os, jaxbConfig, jaxbConfigClass);
		}catch(Exception e){
			logger.error("JAXBConfig file read error", e);
		}finally{
			if(os != null){
				try {
					os.close();
				} catch (IOException ignore) {
				}
			}
		}
		
	}
	public static <T> void writeConfig(OutputStream os, Object jaxbConfig, Class<T> jaxbConfigClass){
		try{
			JAXBContext context = JAXBContext.newInstance(jaxbConfigClass);
			Marshaller marshaller = context.createMarshaller();
			marshaller.setProperty(Marshaller.JAXB_ENCODING, "UTF-8");
			marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
			marshaller.marshal(jaxbConfig, os);
		}catch(Exception e){
			logger.error("JAXBConfig file read error", e);
		}
	}
}
