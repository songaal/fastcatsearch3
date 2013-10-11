package org.fastcatsearch.settings;

import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

import org.fastcatsearch.ir.common.IndexingType;

@XmlRootElement(name="scheduler")
public class SchedulerSetting {
	
	List<IndexingSchedule> indexingScheduleList;
	List<JobSchedule> jobScheduleList;
	
	
	@XmlElementWrapper(name="indexing-schedule-list")
	@XmlElement(name="indexing-schedule")
	public List<IndexingSchedule> getIndexingScheduleList() {
		return indexingScheduleList;
	}

	public void setIndexingScheduleList(List<IndexingSchedule> indexingScheduleList) {
		this.indexingScheduleList = indexingScheduleList;
	}

	@XmlElementWrapper(name="job-schedule-list")
	@XmlElement(name="job-schedule")
	public List<JobSchedule> getJobScheduleList() {
		return jobScheduleList;
	}

	public void setJobScheduleList(List<JobSchedule> jobScheduleList) {
		this.jobScheduleList = jobScheduleList;
	}

	

	public static class Param {
		private String key;
		private String value;
		
		@XmlAttribute
		public String getKey() {
			return key;
		}
		public void setKey(String key) {
			this.key = key;
		}
		
		@XmlElement
		public String getValue() {
			return value;
		}
		public void setValue(String value) {
			this.value = value;
		}
		
		
	}
	public static class JobSchedule {
		private String id;
		private String name;
		private String job;
		private String shell;
		private boolean active;
		private List<Param> paramList;
		private String start;
		private String end;
		private int periodInSecond;
		
		@XmlAttribute
		public String getId() {
			return id;
		}
		public void setId(String id) {
			this.id = id;
		}
		
		@XmlAttribute
		public String getName() {
			return name;
		}
		public void setName(String name) {
			this.name = name;
		}
		
		@XmlAttribute
		public String getJob() {
			return job;
		}
		public void setJob(String job) {
			this.job = job;
		}
		
		@XmlAttribute
		public String getShell() {
			return shell;
		}
		public void setShell(String shell) {
			this.shell = shell;
		}
		
		@XmlAttribute
		public boolean isActive() {
			return active;
		}
		public void setActive(boolean active) {
			this.active = active;
		}
		
		@XmlElement(name="param")
		public List<Param> getParamList() {
			return paramList;
		}
		public void setParamList(List<Param> paramList) {
			this.paramList = paramList;
		}
		
		@XmlAttribute
		public String getStart() {
			return start;
		}
		public void setStart(String start) {
			this.start = start;
		}
		
		@XmlAttribute
		public String getEnd() {
			return end;
		}
		public void setEnd(String end) {
			this.end = end;
		}
		
		@XmlAttribute
		public int getPeriodInSecond() {
			return periodInSecond;
		}
		public void setPeriodInSecond(int periodInSecond) {
			this.periodInSecond = periodInSecond;
		}
	}
	
	public static class IndexingSchedule {
		
		private String collectionId;
		private IndexingType indexingType;
		private boolean active;
		private String scheduleType;
		private String start;
		private String end;
		private int periodInSecond;
		
		@XmlAttribute
		public String getCollectionId() {
			return collectionId;
		}
		public void setCollectionId(String collectionId) {
			this.collectionId = collectionId;
		}
		
		@XmlAttribute
		public IndexingType getIndexingType() {
			return indexingType;
		}
		public void setIndexingType(IndexingType indexingType) {
			this.indexingType = indexingType;
		}
		
		@XmlAttribute
		public boolean isActive() {
			return active;
		}
		public void setActive(boolean active) {
			this.active = active;
		}
		
		@XmlAttribute
		public String getScheduleType() {
			return scheduleType;
		}
		public void setScheduleType(String scheduleType) {
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
		public String getEnd() {
			return end;
		}
		public void setEnd(String end) {
			this.end = end;
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
