package org.fastcatsearch.plugin;

import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;

public class PluginSetting {

	protected String id;
	protected String namespace;
	protected String className;

	protected String name;
	protected String version;
	protected String description;
	protected boolean useDB;

	protected List<Action> actionList;
	protected List<PluginSchedule> scheduleList;

	public String getKey(String name) {
		return namespace + "/" + id + "/" + name;
	}

	@XmlAttribute
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	@XmlAttribute
	public String getNamespace() {
		return namespace;
	}

	public void setNamespace(String namespace) {
		this.namespace = namespace;
	}

	@XmlAttribute(name = "class")
	public String getClassName() {
		return className;
	}

	public void setClassName(String className) {
		this.className = className;
	}

	@XmlElement
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@XmlElement
	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	@XmlElement
	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	@XmlElement(name = "use-db")
	public boolean isUseDB() {
		return useDB;
	}

	public void setUseDB(boolean useDB) {
		this.useDB = useDB;
	}

	@XmlElementWrapper(name = "action-list")
	@XmlElement(name = "action")
	public List<Action> getActionList() {
		return actionList;
	}

	public void setActionList(List<Action> actionList) {
		this.actionList = actionList;
	}

	@XmlElementWrapper(name = "schedule-list")
	@XmlElement(name = "schedule")
	public List<PluginSchedule> getScheduleList() {
		return scheduleList;
	}

	public void setScheduleList(List<PluginSchedule> scheduleList) {
		this.scheduleList = scheduleList;
	}

	public static class Action {

		private String className;

		@XmlAttribute(name = "class")
		public String getClassName() {
			return className;
		}

		public void setClassName(String className) {
			this.className = className;
		}

	}

	public static class PluginSchedule {

		private String className;
		private String startTime;
		private int periodInMinute;
		private String args;

		@XmlAttribute(name = "class")
		public String getClassName() {
			return className;
		}

		public void setClassName(String className) {
			this.className = className;
		}

		@XmlAttribute
		public String getStartTime() {
			return startTime;
		}

		public void setStartTime(String startTime) {
			this.startTime = startTime;
		}

		@XmlAttribute
		public int getPeriodInMinute() {
			return periodInMinute;
		}

		public void setPeriodInMinute(int periodInMinute) {
			this.periodInMinute = periodInMinute;
		}

		@XmlAttribute
		public String getArgs() {
			return args;
		}

		public void setArgs(String args) {
			this.args = args;
		}

	}

}
