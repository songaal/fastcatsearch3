package org.fastcatsearch.ir.settings;

import javax.xml.bind.annotation.XmlAttribute;

public class IndexRefSetting extends RefSetting {
	private String indexAnalyzer;
	
	public IndexRefSetting() {}
	
	public IndexRefSetting(String ref, String indexAnalyzer) {
		super(ref);
		this.indexAnalyzer = indexAnalyzer;
	}
	
	@Override
	public String toString(){
		return "[IndexRefSetting]"+getRef() + ":" +indexAnalyzer;
	}
	
	@XmlAttribute
	public String getIndexAnalyzer() {
		return indexAnalyzer;
	}

	public void setIndexAnalyzer(String indexAnalyzer) {
		this.indexAnalyzer = indexAnalyzer;
	}
	
}
