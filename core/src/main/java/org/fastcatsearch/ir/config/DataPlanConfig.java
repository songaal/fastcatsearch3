package org.fastcatsearch.ir.config;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

/**
<data-plan>
	<data-sequence-cycle>2</data-sequence-cycle>
	<separate-inc-indexing>true</separate-inc-indexing>
	<document-limit>2000000</document-limit>
	<segment-revision-backup-size>2</segment-revision-backup-size>
</data-plan>
 * */

@XmlRootElement(name = "data-plan")
@XmlType(propOrder = { "segmentRevisionBackupSize", "segmentDocumentLimit", "dataSequenceCycle" })
public class DataPlanConfig {
	private int dataSequenceCycle;
	private int documentLimit;
	private int segmentRevisionBackupSize;
	
	
	public static final DataPlanConfig DefaultDataPlanConfig = new DataPlanConfig(2, 2000000, 0);
	
	public DataPlanConfig(){
	}
	
	public DataPlanConfig(int dataSequenceCycle, int documentLimit, int segmentRevisionBackupSize){
		this.dataSequenceCycle = dataSequenceCycle;
		this.documentLimit = documentLimit;
		this.segmentRevisionBackupSize = segmentRevisionBackupSize;
	}
	
	@XmlElement(name="data-sequence-cycle")
	public int getDataSequenceCycle() {
		return dataSequenceCycle;
	}
	public void setDataSequenceCycle(int dataSequenceCycle) {
		this.dataSequenceCycle = dataSequenceCycle;
	}
	
	@XmlElement(name="segment-document-limit")
	public int getSegmentDocumentLimit() {
		return documentLimit;
	}
	public void setSegmentDocumentLimit(int documentLimit) {
		this.documentLimit = documentLimit;
	}
	@XmlElement(name="segment-revision-backup-size")
	public int getSegmentRevisionBackupSize() {
		return segmentRevisionBackupSize;
	}
	public void setSegmentRevisionBackupSize(int segmentRevisionBackupSize) {
		this.segmentRevisionBackupSize = segmentRevisionBackupSize;
	}
}
