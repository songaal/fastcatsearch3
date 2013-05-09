package org.fastcatsearch.plugin;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlValue;

@XmlRootElement(name="plugin")
@XmlAccessorType(XmlAccessType.FIELD)
public class PluginSetting {
	
	@XmlAttribute
	String name;
	
	@XmlAttribute(name="class")
	String className;
	
	@XmlElementWrapper(name="params")
	List<Param> params;
	
	@XmlElement
	Web web;
	
	@XmlElement
	DB db;
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public String getClassName() {
		return className;
	}

	public void setClassName(String className) {
		this.className = className;
	}
	
	public Web getWeb() {
		return web;
	}

	public void setWeb(Web web) {
		this.web = web;
	}
	
	public List<Param> getParams() {
		return params;
	}

	public void setParams(List<Param> params) {
		this.params = params;
	}
	
	public DB getDB() {
		return db;
	}

	public void seDB(DB db) {
		this.db = db;
	}
	
	@XmlRootElement
	@XmlAccessorType(XmlAccessType.FIELD)
	public static class Param {
		@XmlAttribute
		String key;
		@XmlValue
		String value;
		
		public String getKey() {
			return key;
		}
		public String getValue() {
			return value;
		}
		public void setKey(String key) {
			this.key = key;
		}
		public void setValue(String value) {
			this.value = value;
		}
	}
	
	
	@XmlRootElement
	public static class Web {
		User user;
		Admin admin;
		
		@XmlElement
		public User getUser() {
			return user;
		}
		@XmlElement
		public Admin getAdmin() {
			return admin;
		}
		public void setUser(User user) {
			this.user = user;
		}
		public void setAdmin(Admin admin) {
			this.admin = admin;
		}
	}

	@XmlRootElement(name="db")
	public static class DB {
		List<DAO> daoList;
		
		@XmlElementWrapper(name="dao-list")
		@XmlElement(name="dao")
		public List<DAO> getDAOList() {
			return daoList;
		}
		public void setDAOList(List<DAO> daoList) {
			this.daoList = daoList;
		}
	}
	
	@XmlRootElement(name="dao")
	public static class DAO {
		String className;
		
		@XmlValue
		public String getClassName() {
			return className;
		}
		public void setClassName(String className) {
			this.className = className;
		}
	}
	
	
	@XmlRootElement
	@XmlAccessorType(XmlAccessType.FIELD)
	public static class User {
		@XmlElement
		Menu menu;
		@XmlElement
		Servlet servlet;
		public Menu getMenu() {
			return menu;
		}
		public Servlet getServlet() {
			return servlet;
		}
		public void setMenu(Menu menu) {
			this.menu = menu;
		}
		public void setServlet(Servlet servlet) {
			this.servlet = servlet;
		}
		
	}

	@XmlRootElement
	@XmlAccessorType(XmlAccessType.FIELD)
	public static class Admin {
		@XmlElement
		Menu menu;
		@XmlElement
		Servlet servlet;
		public Menu getMenu() {
			return menu;
		}
		public Servlet getServlet() {
			return servlet;
		}
		public void setMenu(Menu menu) {
			this.menu = menu;
		}
		public void setServlet(Servlet servlet) {
			this.servlet = servlet;
		}
		
	}
	
	@XmlRootElement
	@XmlAccessorType(XmlAccessType.FIELD)
	public static class Menu {
		@XmlAttribute
		String id;
		@XmlAttribute
		String label;
		@XmlElement
		Submenu submenu;
		public String getId() {
			return id;
		}
		public String getLabel() {
			return label;
		}
		public Submenu getSubmenu() {
			return submenu;
		}
		public void setId(String id) {
			this.id = id;
		}
		public void setLabel(String label) {
			this.label = label;
		}
		public void setSubmenu(Submenu submenu) {
			this.submenu = submenu;
		}
		
	}

	@XmlRootElement
	@XmlAccessorType(XmlAccessType.FIELD)
	public static class Submenu {
		@XmlAttribute
		String id;
		@XmlAttribute
		String label;
		@XmlElement
		List<Leaf> leafList;
		public String getId() {
			return id;
		}
		public String getLabel() {
			return label;
		}
		public List<Leaf> getLeaf() {
			return leafList;
		}
		public void setId(String id) {
			this.id = id;
		}
		public void setLabel(String label) {
			this.label = label;
		}
		public void setLeaf(List<Leaf> leafList) {
			this.leafList = leafList;
		}
		
	}

	@XmlRootElement
	@XmlAccessorType(XmlAccessType.FIELD)
	public static class Leaf {
		@XmlAttribute
		String id;
		@XmlAttribute
		String label;
		public String getId() {
			return id;
		}
		public String getLabel() {
			return label;
		}
		public void setId(String id) {
			this.id = id;
		}
		public void setLabel(String label) {
			this.label = label;
		}
		
	}

	@XmlRootElement
	@XmlAccessorType(XmlAccessType.FIELD)
	public static class Servlet {
		@XmlAttribute
		String path;
		@XmlValue
		String value;
		public String getPath() {
			return path;
		}
		public String getValue() {
			return value;
		}
		public void setPath(String path) {
			this.path = path;
		}
		public void setValue(String value) {
			this.value = value;
		}
		
	}
}








