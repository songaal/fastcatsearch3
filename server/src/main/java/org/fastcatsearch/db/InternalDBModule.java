/*
 * Copyright (c) 2013 Websquared, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 * 
 * Contributors:
 *     swsong - initial API and implementation
 */

package org.fastcatsearch.db;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.List;
import java.util.Properties;

import org.apache.ibatis.builder.xml.XMLMapperBuilder;
import org.apache.ibatis.datasource.pooled.PooledDataSource;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.apache.ibatis.transaction.jdbc.JdbcTransactionFactory;
import org.fastcatsearch.env.Environment;
import org.fastcatsearch.module.AbstractModule;
import org.fastcatsearch.module.ModuleException;
import org.fastcatsearch.service.ServiceManager;
import org.fastcatsearch.settings.Settings;

public class InternalDBModule extends AbstractModule {

	private final static String derbyEmbeddedDriver = "org.apache.derby.jdbc.EmbeddedDriver";
	private final static String detbyUrlPrefix = "jdbc:derby:";
	private final static String detbyUrlSuffix = ";create=true"; //create if not exists
	private final String dbPath;

	protected static InternalDBModule instance;

	private List<URL> mapperFileList;
	
	private SqlSessionFactory sqlSessionFactory;

	public static InternalDBModule getInstance() {
		return instance;
	}

	public void asSingleton() {
		instance = this;
	}

	public InternalDBModule(String dbPath, List<URL> mapperFileList, Environment environment, Settings settings) {
		super(environment, settings);
		if(new File(dbPath).exists()){
			this.dbPath = detbyUrlPrefix + dbPath;	
		}else{
			//create
			this.dbPath = detbyUrlPrefix + dbPath + detbyUrlSuffix;
		}
		this.mapperFileList = mapperFileList;
	}

	@Override
	protected boolean doLoad() throws ModuleException {
		Properties driverProperties = new Properties();
		driverProperties.setProperty("driver.encoding", "UTF-8");
		//******* driverProperties *****
		//poolMaximumActiveConnections
		//poolMaximumIdleConnections
		//poolMaximumCheckoutTime
		//poolTimeToWait
		//poolPingQuery
		//poolPingEnabled
		//poolPingConnectionsNotUsedFor
		//////////////////////////////////
		
		PooledDataSource dataSource = new PooledDataSource(derbyEmbeddedDriver, dbPath, driverProperties);
		org.apache.ibatis.mapping.Environment environment = new org.apache.ibatis.mapping.Environment("ID", new JdbcTransactionFactory(), dataSource);
		Configuration configuration = new Configuration(environment);
		
		if(mapperFileList != null){
			for(URL mapperFile : mapperFileList){
				addSqlMappings(configuration, mapperFile);
			}
		}
		
		sqlSessionFactory = new SqlSessionFactoryBuilder().build(configuration);
		logger.info("DBModule[{}] Loaded! with {}", dbPath, mapperFileList);
		return true;
	}

	public SqlSession openSession(){
		return sqlSessionFactory.openSession();
	}
	public SqlSession openBatchSession(){
		return sqlSessionFactory.openSession(ExecutorType.BATCH);
	}
	
	@Override
	protected boolean doUnload() throws ModuleException {
//		try {
			logger.info(getClass().getSimpleName()+"[{}] Unloaded!, sqlSessionFactory = {}", dbPath, sqlSessionFactory);
//			DriverManager.getConnection(dbPath+";shutdown=true", "","");
//		} catch (SQLException e) {
			//shutdown되면 SQLException을 던지므로 OK.
//			logger.info("{}", e.getMessage());
//			return true;
//		}
		return true;
	}

	private void addSqlMappings(Configuration conf, URL mapperFilePath) {
		InputStream is = null;
		try {
			is = mapperFilePath.openStream();
			XMLMapperBuilder xmlParser = new XMLMapperBuilder(is, conf, mapperFilePath.toString(), conf.getSqlFragments());
			xmlParser.parse();
		} catch (IOException e) {
			logger.error("error loading mybatis mapping config file.", e);
		} finally {
			if(is != null){
				try {
					is.close();
				} catch (IOException e) {
				}
			}
		}
	}
	
	public static class MapperSession<T> {
		private SqlSession session;
		private T mapper;
		
		public SqlSession getSession() {
			return session;
		}
		
		public MapperSession(SqlSession session, T mapper){
			this.session = session;
			this.mapper = mapper;
		}
		
		public T getMapper(){
			return mapper;
		}
		
		public void commit(){
			if(session != null){
				session.commit();
			}
		}
		
		public void rollback(){
			if(session != null){
				session.rollback();
			}
		}
		public void closeSession(){
			if(session != null){
				session.commit();
				session.close();
			}
		}
	}

}
