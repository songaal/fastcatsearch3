package org.fastcatsearch.db;

import java.io.IOException;
import java.io.InputStream;
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
/**
 * table명까지 파라미터로 전달하여 mapper를 여러 table에서 범용적으로 사용할수 있는지 확인.
 * 결론 : 파라미터롤 #{tableName}을 사용하면 안되고, ${tableName} 을 사용해야 쿼리문을 단순 치환할수 있다. 
 * #{}는 preparedStatement의 파라미터 ? 로 변경되므로 테이블명에는 사용불가.
 * */
public class MyBatisTest2 {

	
	@Test
	public void testStartProgramatically() throws InterruptedException, IOException {
		String tableName = "Analysis_Korean_user";
		
		
		String url = "jdbc:derby:/Users/swsong/TEST_HOME/fastcatsearch2_shard/node1/db/plugin";
		PooledDataSource dataSource = new PooledDataSource("org.apache.derby.jdbc.EmbeddedDriver", url, "", "");
		Environment environment = new Environment("ID", new JdbcTransactionFactory(), dataSource);
		Configuration configuration = new Configuration(environment);
		addSqlMappings(configuration, "org/fastcatsearch/db/TestMapper2.xml");
		
		SqlSessionFactory sqlSessionFactory = new SqlSessionFactoryBuilder().build(configuration);
		
		SqlSession session = sqlSessionFactory.openSession();
		TestMapper2 setDictionaryMapper = session.getMapper(TestMapper2.class);
		
		for(int i= 0;i < 10; i++){
			
			Map<String, Object> vo = setDictionaryMapper.selectWord(tableName, i);
			if(vo != null){
				
				for(Map.Entry<String, Object> entry : vo.entrySet()){
				System.out.println(entry.getKey() + "= " + entry.getValue());
				}
			}
		}
		
		
			
		
		session.close();
	}

	
	private static void addSqlMappings(Configuration conf, String mapperFilePath) {
		InputStream is = null;
		try {
			//is = new FileInputStream(mapperFilePath);
			is = Resources.getResourceAsStream(mapperFilePath);
			XMLMapperBuilder xmlParser = new XMLMapperBuilder(is, conf, mapperFilePath, conf.getSqlFragments());
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
