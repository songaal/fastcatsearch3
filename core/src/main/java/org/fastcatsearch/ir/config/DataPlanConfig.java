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
@XmlType(propOrder = { "segmentRevisionBackupSize", "segmentDocumentLimit", "separateIncIndexing", "dataSequenceCycle" })
public class DataPlanConfig {
	private int dataSequenceCycle;
	private boolean isSeparateIncIndexing;
	private int documentLimit;
	private int segmentRevisionBackupSize;
	
	@XmlElement(name="data-sequence-cycle")
	public int getDataSequenceCycle() {
		return dataSequenceCycle;
	}
	public void setDataSequenceCycle(int dataSequenceCycle) {
		this.dataSequenceCycle = dataSequenceCycle;
	}
	@XmlElement(name="separate-inc-indexing")
	public boolean isSeparateIncIndexing() {
		return isSeparateIncIndexing;
	}
	public void setSeparateIncIndexing(boolean isSeparateIncIndexing) {
		this.isSeparateIncIndexing = isSeparateIncIndexing;
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
