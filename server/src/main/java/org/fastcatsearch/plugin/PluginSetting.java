package org.fastcatsearch.plugin;

import org.fastcatsearch.http.ActionMapping;
import org.fastcatsearch.http.ActionMethod;
import org.fastcatsearch.http.action.management.analysis.GetBasicAnalizedResultAction;
import org.fastcatsearch.util.DynamicClassLoader;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlType;
import java.util.List;

@XmlType(name = "plugin")
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
		this.id = id.toUpperCase();
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
		private ActionMapping actionMap;

		@XmlAttribute
		public String getClassName() {
			return className;
		}

		public void setClassName(String className) {
			this.className = className;
		}
		
		//read only 속성.
		@XmlAttribute(required = false)
		public String getUri() {
			return findActionMapping("uri");
		}
		@XmlAttribute(required = false)
		public String getMethods() {
			return findActionMapping("method");
		}

		private String findActionMapping(String type) {
			if (actionMap == null) {
				try {
					Class<?> actionClass = DynamicClassLoader.loadClass(className);
					if (actionClass == null) {
						return null;
					}
					actionMap = actionClass.getAnnotation(ActionMapping.class);
				} catch (Exception e) {
					Class<?> actionClass = GetBasicAnalizedResultAction.class;
					actionMap = actionClass.getAnnotation(ActionMapping.class);
				}
			}
			if (actionMap != null) {
				if ("uri".equals(type)) {
					return actionMap.value();
				}
				if ("method".equals(type)) {
					String ret = "";
					ActionMethod[] methods = actionMap.method();
					for (ActionMethod method : methods) {
						if (!"".equals(ret)) {
							ret += ",";
						}
						ret += method.name();
					}
					return ret;
				}
			}
			return null;
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
