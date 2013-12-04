package org.fastcatsearch.ir.config;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name="schedule")
public class IndexingScheduleConfig {
	public static enum ScheduleType { REGULAR_PERIOD };
	
	private IndexingSchedule fullIndexingSchedule;
	private IndexingSchedule addIndexingSchedule;
	
	@XmlElement(name="full-indexing-schedule")
	public IndexingSchedule getFullIndexingSchedule() {
		return fullIndexingSchedule;
	}

	public void setFullIndexingSchedule(IndexingSchedule fullIndexingSchedule) {
		this.fullIndexingSchedule = fullIndexingSchedule;
	}

	@XmlElement(name="add-indexing-schedule")
	public IndexingSchedule getAddIndexingSchedule() {
		return addIndexingSchedule;
	}

	public void setAddIndexingSchedule(IndexingSchedule addIndexingSchedule) {
		this.addIndexingSchedule = addIndexingSchedule;
	}

	public static class IndexingSchedule {
		
		public static final IndexingSchedule DefaultIndexingSchedule = new IndexingSchedule(ScheduleType.REGULAR_PERIOD, "2000-01-01 00:00:00", 0, false);
		
		private ScheduleType scheduleType;
		private String start;
		private int periodInSecond;
		private boolean active;
		
		public IndexingSchedule(){
		}
		
		public IndexingSchedule(ScheduleType scheduleType, String start, int periodInSecond, boolean active) {
			this.scheduleType = scheduleType;
			this.start = start;
			this.periodInSecond = periodInSecond;
			this.active = active;
		}

		@XmlAttribute
		public boolean isActive() {
			return active;
		}
		public void setActive(boolean active) {
			this.active = active;
		}
		
		@XmlAttribute
		public ScheduleType getScheduleType() {
			return scheduleType;
		}
		public void setScheduleType(ScheduleType scheduleType) {
			this.scheduleType = scheduleType;
		}
		
		@XmlAttribute
		public String getStart() {
			return start;
		}
		public void setStart(String start) {
			this.start = start;
		}
		
		@XmlAttribute
		public int getPeriodInSecond() {
			return periodInSecond;
		}
		public void setPeriodInSecond(int periodInSecond) {
			this.periodInSecond = periodInSecond;
		}
	}
}
