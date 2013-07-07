package org.fastcatsearch.ir.config;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "source")
public class FileSourceConfig extends DataSourceConfig {

	private String fullFilePath;
	private String incFilePath;
	private String fileEncoding;
	private String fileDocParser;
	
	@XmlElement
	public String getIncFilePath() {
		return incFilePath;
	}

	@XmlElement
	public String getFullFilePath() {
		return fullFilePath;
	}

	@XmlElement
	public String getFileEncoding() {
		return fileEncoding;
	}
	
	@XmlElement
	public String getFileDocParser() {
		return fileDocParser;
	}
	
	public void setIncFilePath(String incFilePath) {
		this.incFilePath = incFilePath;
	}

	public void setFullFilePath(String fullFilePath) {
		this.fullFilePath = fullFilePath;
	}

	public void setFileEncoding(String fileEncoding) {
		this.fileEncoding = fileEncoding;
	}

	public void setFileDocParser(String fileDocParser) {
		this.fileDocParser = fileDocParser;
		
	}
	
}
