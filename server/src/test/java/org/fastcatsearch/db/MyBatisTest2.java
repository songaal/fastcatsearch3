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
import org.fastcatsearch.db.TestVO.Type;
import org.junit.Test;
/**
 * table명까지 파라미터로 전달하여 mapper를 여러 table에서 범용적으로 사용할수 있는지 확인.
 * 결론 : 파라미터롤 #{tableName}을 사용하면 안되고, ${tableName} 을 사용해야 쿼리문을 단순 치환할수 있다. 
 * #{}는 preparedStatement의 파라미터 ? 로 변경되므로 테이블명에는 사용불가.
 * */
public class MyBatisTest2 {

	
	@Test
	public void testStartProgramatically() throws InterruptedException, IOException {
		
		String url = "jdbc:derby:/tmp/testdb;create=true";
		PooledDataSource dataSource = new PooledDataSource("org.apache.derby.jdbc.EmbeddedDriver", url, "", "");
		Environment environment = new Environment("ID", new JdbcTransactionFactory(), dataSource);
		Configuration configuration = new Configuration(environment);
		addSqlMappings(configuration, "org/fastcatsearch/db/TestMapper2.xml");
		
		SqlSessionFactory sqlSessionFactory = new SqlSessionFactoryBuilder().build(configuration);
		
		SqlSession session = sqlSessionFactory.openSession();
		TestMapper2 testMapper = session.getMapper(TestMapper2.class);
		
		try{
			testMapper.dropTable();
		}catch(Exception e){
			System.out.println(e.getMessage());
		}
		
		try{
			testMapper.createTable();
			testMapper.createIndex();
		}catch(Exception e){
//			System.out.println(e.getMessage());
			e.printStackTrace();
		}
		
		for(int i= 0;i < 10; i++){
			TestVO vo = new TestVO();
			vo.word = "word-"+i;
			vo.type = i % 2  == 0 ? Type.NOUN : Type.VERB;
			testMapper.insertWord(vo);
		}
		session.commit();
		
		for(int i= 1;i < 10; i++){
			TestVO vo = testMapper.selectWord(i);
			System.out.println(vo.id + ":"+vo.word + ":" + vo.type);
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
