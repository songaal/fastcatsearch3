package org.fastcatsearch.ir.config;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "jdbc-source")
public class JDBCSourceInfo {

	private String id;
	private String name;
	private String driver;
	private String url;
	private String user;
	private String password;
	
	@XmlAttribute
	public String getId() {
		return id;
	}
	
	@XmlAttribute
	public String getName() {
		return name;
	}
	
	@XmlAttribute
	public String getDriver() {
		return driver;
	}
	
	@XmlAttribute
	public String getUrl() {
		return url;
	}

	@XmlAttribute
	public String getPassword() {
		return password;
	}

	@XmlAttribute
	public String getUser() {
		return user;
	}

	public void setId(String id) {
		this.id = id;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public void setDriver(String driver) {
		this.driver = driver;
	}
	
	public void setUrl(String url) {
		this.url = url;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public void setUser(String user) {
		this.user = user;
	}

	@Override
    public String toString() {
        return new StringBuffer().append(this.getClass().getSimpleName())
                .append(":").append(id)
                .append(":").append(url)
                .append(":").append(user)
                .toString();
    }

}