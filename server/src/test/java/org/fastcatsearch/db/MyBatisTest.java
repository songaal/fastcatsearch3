package org.fastcatsearch.db;

import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import org.apache.ibatis.builder.xml.XMLMapperBuilder;
import org.apache.ibatis.datasource.pooled.PooledDataSource;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.mapping.Environment;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.apache.ibatis.transaction.jdbc.JdbcTransactionFactory;
import org.junit.Test;

public class MyBatisTest {

	
	@Test
	public void testStartProgramatically() throws InterruptedException, IOException {
		
		String url = "jdbc:derby:/Users/swsong/TEST_HOME/fastcatsearch2_shard/node1/db/plugin";
		PooledDataSource dataSource = new PooledDataSource("org.apache.derby.jdbc.EmbeddedDriver", url, "", "");
		Environment environment = new Environment("ID", new JdbcTransactionFactory(), dataSource);
		Configuration configuration = new Configuration(environment);
		addSqlMappings(configuration, "org/fastcatsearch/db/TestMapper.xml");
		
		SqlSessionFactory sqlSessionFactory = new SqlSessionFactoryBuilder().build(configuration);
		
		System.out.println("Open session....");
		SqlSession session = sqlSessionFactory.openSession();
		System.out.println("Open session....Done");
		TestMapper setDictionaryMapper = session.getMapper(TestMapper.class);
		System.out.println("Got mapper!");
//		for(int i= 0;i < 20; i++){
//			
//			//SetDictionaryVO vo = (SetDictionaryVO) session.selectOne("org.fastcatsearch.db.SetDictionaryMapper.selectWord", i);
//			Map<String, Object> vo = setDictionaryMapper.selectWord(i);
//			if(vo != null){
//				
//				for(Map.Entry<String, Object> entry : vo.entrySet()){
//				System.out.println(entry.getKey() + "= " + entry.getValue());
//				}
//			}
//		}
		
		
			
		//SetDictionaryVO vo = (SetDictionaryVO) session.selectOne("org.fastcatsearch.db.SetDictionaryMapper.selectWord", i);
		List<Map<String, Object>> list = setDictionaryMapper.selectList(0, 15, "%a%");
		if(list != null){
			for(int i=0;i<list.size(); i++){
				Map<String, Object> vo = list.get(i);
				System.out.println("------------");
				for(Map.Entry<String, Object> entry : vo.entrySet()){
					System.out.println(entry.getKey() + "= " + entry.getValue());
				}
			}
		}
		
		session.close();
	}

	
	/*
	 * 결론 : session open은 처음에만 jdbc가 접속때문에 시간이 걸리고, 다음부터는 open에 시간이 거의 걸리지 않음.
	 * 즉, 사용후 close하는것이 좋음. 
	 * */
	@Test
	public void testSessionCloseAndOpen() throws InterruptedException, IOException {
		
		String url = "jdbc:derby:/Users/swsong/TEST_HOME/fastcatsearch2_shard/node1/db/plugin";
		PooledDataSource dataSource = new PooledDataSource("org.apache.derby.jdbc.EmbeddedDriver", url, "", "");
		Environment environment = new Environment("ID", new JdbcTransactionFactory(), dataSource);
		Configuration configuration = new Configuration(environment);
		addSqlMappings(configuration, "org/fastcatsearch/db/TestMapper.xml");
		
		SqlSessionFactory sqlSessionFactory = new SqlSessionFactoryBuilder().build(configuration);
		
		long lap = System.nanoTime();
		for(int k =0;k <10; k++){
			System.out.println("Open session....");
			SqlSession session = sqlSessionFactory.openSession();
			System.out.println("Open session....Done");
			TestMapper setDictionaryMapper = session.getMapper(TestMapper.class);
			System.out.println("Got mapper!");
			
			//SetDictionaryVO vo = (SetDictionaryVO) session.selectOne("org.fastcatsearch.db.SetDictionaryMapper.selectWord", i);
			List<Map<String, Object>> list = setDictionaryMapper.selectList(0, 15, null);
			if(list != null){
				for(int i=0;i<list.size(); i++){
					Map<String, Object> vo = list.get(i);
	//				System.out.println("------------");
					for(Map.Entry<String, Object> entry : vo.entrySet()){
	//					System.out.println(entry.getKey() + "= " + entry.getValue());
					}
				}
			}
			session.close();
			
			System.out.println("lap time = "+(System.nanoTime() - lap)/1000000);
			lap = System.nanoTime();
		}
			
	}
	
	@Test
	public void testOpenTwice() throws InterruptedException, IOException, SQLException {
		
		String url = "jdbc:derby:/Users/swsong/TEST_HOME/fastcatsearch2_shard/node1/db/plugin";
		PooledDataSource dataSource = new PooledDataSource("org.apache.derby.jdbc.EmbeddedDriver", url, "", "");
		Environment environment = new Environment("ID", new JdbcTransactionFactory(), dataSource);
		Configuration configuration = new Configuration(environment);
		addSqlMappings(configuration, "org/fastcatsearch/db/TestMapper.xml");
		
		System.out.println("----1----");
		SqlSessionFactory sqlSessionFactory = new SqlSessionFactoryBuilder().build(configuration);
		SqlSession session = sqlSessionFactory.openSession();
		TestMapper setDictionaryMapper = session.getMapper(TestMapper.class);
		Map<String, Object> vo = setDictionaryMapper.selectWord(1);
		System.out.println(vo);
		session.close();
		System.out.println(sqlSessionFactory.getConfiguration().getEnvironment().getDataSource().getConnection());
		
		
		System.out.println("----2----");
		sqlSessionFactory = new SqlSessionFactoryBuilder().build(configuration);
		session = sqlSessionFactory.openSession();
		setDictionaryMapper = session.getMapper(TestMapper.class);
		vo = setDictionaryMapper.selectWord(1);
		System.out.println(vo);
		session.close();
			
	}
	
	private static void addSqlMappings(Configuration conf, String mapperFilePath) {
		InputStream is = null;
		try {
			//is = new FileInputStream(mapperFilePath);
			is = Resources.getResourceAsStream(mapperFilePath);
			XMLMapperBuilder xmlParser = new XMLMapperBuilder(is, conf, "mapper.xml", conf.getSqlFragments());
			xmlParser.parse();
		} catch (IOException ex) {
			ex.printStackTrace();
		} finally {
			if(is != null){
				try {
					is.close();
				} catch (IOException e) {
				}
			}
		}
	}
}
