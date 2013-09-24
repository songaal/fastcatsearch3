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

	protected List<Action> actionList;

	public String getKey(String name) {
		return namespace + "_" + id + "_" + name;
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

	@XmlElementWrapper(name = "action-list")
	@XmlElement(name = "action")
	public List<Action> getActionList() {
		return actionList;
	}

	public void setActionList(List<Action> actionList) {
		this.actionList = actionList;
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

}
