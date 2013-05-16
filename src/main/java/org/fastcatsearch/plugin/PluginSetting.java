package org.fastcatsearch.plugin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlValue;
import javax.xml.bind.annotation.adapters.XmlAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

@XmlRootElement(name = "plugin")
@XmlAccessorType(XmlAccessType.FIELD)
public class PluginSetting {

	@XmlAttribute
	String id;

	@XmlAttribute
	String namespace;

	@XmlAttribute(name = "class")
	String className;

	@XmlJavaTypeAdapter(MapAdapter.class)
	@XmlElement
	Map<String, String> properties = new HashMap<String, String>();

	@XmlElement
	Web web;

	@XmlElementWrapper(name = "analyzer-list")
	@XmlElement
	List<Analyzer> analyzerList;
	
	@XmlElement
	DB db;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getNamespace() {
		return namespace;
	}

	public void setNamespace(String namespace) {
		this.namespace = namespace;
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

	public Map<String, String> getProperties() {
		return properties;
	}

	public void setProperties(Map<String, String> properties) {
		this.properties = properties;
	}

	public DB getDB() {
		return db;
	}

	public void seDB(DB db) {
		this.db = db;
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

	@XmlRootElement
	public static class Analyzer {
		String id;
		String name;
		String value;
		
		@XmlAttribute
		public String getId() {
			return id;
		}
		@XmlAttribute
		public String getName() {
			return name;
		}
		@XmlValue
		public String getValue() {
			return value;
		}
		public void setId(String id) {
			this.id = id;
		}
		public void setName(String name) {
			this.name = name;
		}
		public void setValue(String value) {
			this.value = value;
		}
	}
	@XmlRootElement(name = "db")
	public static class DB {
		List<DAO> daoList;

		@XmlElementWrapper(name = "dao-list")
		@XmlElement(name = "dao")
		public List<DAO> getDAOList() {
			return daoList;
		}

		public void setDAOList(List<DAO> daoList) {
			this.daoList = daoList;
		}
	}

	@XmlRootElement(name = "dao")
	public static class DAO {
		String name;
		String className;

		@XmlAttribute
		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

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
		String ref;
		@XmlAttribute
		String categoryLabel;

		public String getRef() {
			return ref;
		}

		public void setRef(String ref) {
			this.ref = ref;
		}

		public String getCategoryLabel() {
			return categoryLabel;
		}

		public void setCategoryLabel(String categoryLabel) {
			this.categoryLabel = categoryLabel;
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

	static class AdaptedMap {
		@XmlElement(name="property")
		public List<Property> properties = new ArrayList<Property>();
	}
	
	static class Property {
		@XmlAttribute
        public String key;
		@XmlValue
        public String value;
    }
	
	static class MapAdapter extends XmlAdapter<AdaptedMap, Map<String, String>> {
		@Override
		public Map<String, String> unmarshal(AdaptedMap adaptedMap) throws Exception {
			Map<String, String> map = new HashMap<String, String>();
			for (Property property : adaptedMap.properties) {
				map.put(property.key, property.value);
			}
			return map;
		}

		@Override
		public AdaptedMap marshal(Map<String, String> map) throws Exception {
			AdaptedMap adaptedMap = new AdaptedMap();
			for (Map.Entry<String, String> mapEntry : map.entrySet()) {
				Property property = new Property();
				property.key = mapEntry.getKey();
				property.value = mapEntry.getValue();
				adaptedMap.properties.add(property);
			}
			return adaptedMap;
		}
	}
}
