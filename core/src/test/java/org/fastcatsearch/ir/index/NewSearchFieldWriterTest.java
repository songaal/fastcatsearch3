//package org.fastcatsearch.ir.index;
//
//import org.apache.lucene.analysis.Analyzer;
//import org.fastcatsearch.ir.config.Schema;
//import org.fastcatsearch.ir.document.Document;
//import org.fastcatsearch.ir.setting.SchemaSetting;
//import org.junit.Test;
//
//public class NewSearchFieldWriterTest {
//	
//	@Test
//	public void create(){
//		
//		
//		//만약 schema를 xml에서 로딩할 경우 schemaSetting에서 analyzer class Name등을 class로딩하여 schema객체로 넣어준다.
//		//즉 schemaSetting => schema변환기가 필요하다. ir에서는 수행하지 않고 fastcatsearch에서 class로딩을 수행한다.
//		//반대로 schema를 setting으로 변환할 경우는? 없다고 본다. 
//		//TODO 모든 jaxb 셋팅들은 fastcatsearch로 이동한다.
//		// 모든 설정파일 관련은 fastcatsearch로 이동하고 ir에서는 파라미터와 객체만 이용한다.
//		//필요에 따라 설정객체 indexConfig 같은 객체를 만들어 사용한다.
//		//모든 설정파일 => 객체는 fastcatsearch에서 수행한다.classloader와 확장성때문..
//		SchemaSetting schemaSetting = new SchemaSetting();
//		
//		
//		
//		Schema schema = new Schema(schemaSetting);
//		
//		
////		IndexSetings indexSetings = schema.indexSetings();
//		DefaultAnalyzer textAnalyzerPool = new DefaultAnalyzer();
//		DefaultAnalyzer keywordAnalyzer = new DefaultAnalyzer();
//		
//		schema.addField(new TextField("title"));
//		schema.addField(new IntField("price"));
//		
//		schema.addIndexField("title", textAnalyzerPool);//pool 또는 analyzer모두 사용가능.
//		schema.addIndexField("price", keywordAnalyzer);
//		
//		
//		SearchFieldWriter writer = new SearchFieldWriter(schema, indexConfig);
//		
//		Reader reader1 = new String("가나다라마바사아자차.");
//		Reader reader2 = new String("1234");
//		
//		FieldValue f1 = new TextFieldValue("title", reader1);//analyzer 강제 셋팅가능?
//		FieldValue f2 = new IntFieldValue("price", reader2);
//		Document document = new Document();
//		document.add(f1).add(f2);
//		
//		writer.write(document);
//	}
//	
//	class DocumentIndexWriter{
//		FieldIndexWriter[] fieldIndexWriterList;
//		Schema schema;
//		public DocumentIndexWriter(schema, indexConfig){
//			
//			indexInterval = indexConfig.getInt("index.term.interval");
//			indexBucketSize = indexConfig.getByteSize("index.work.bucket.size");
//			workMemoryLimit = indexConfig.getByteSize("index.work.memory");
//			workMemoryCheck = indexConfig.getInt("index.work.check");
//			tokenizers = new Analyzer[indexSettingList.size()];
//			memoryPosting = new MemoryPosting[indexSettingList.size()];
//			isNumericField = new boolean[indexSettingList.size()];
//			
//			
//			IndexField[] indexFieldList = schema.getIndexFieldList();
//			
//			for(IndexField indexField : indexFieldList){
//				FieldIndexWriter w = new FieldIndexWriter(indexField, indexWriteConfig);
//				fieldIndexWriterList[i] = w;
//			}
//			
//			
//			
//		}
//		
//		void writeDocument(Document document){
//
//			for(indexField indexField : indexFieldList){
//				FieldValue fieldValue = document.get(indexField.id());
//				
//				w[i].writeField(fieldValue);
//			}
//			
//		}
//
//	}
//	
//	class FieldIndexWriter{
//		
//		public FieldIndexWriter(indexField, indexWriteConfig){
//			fieldName = indexField.getFieldName();
//			analyzer = indexField.getAnalyzer();
//			
//		}
//		void writeField(Field field){
//			
//			
//		}
//	}
//}
