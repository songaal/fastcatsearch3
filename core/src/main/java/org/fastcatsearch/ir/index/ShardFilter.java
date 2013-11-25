//package org.fastcatsearch.ir.index;
//
//import java.util.HashSet;
//import java.util.Map;
//import java.util.Set;
//
//import org.fastcatsearch.ir.document.Document;
//import org.fastcatsearch.ir.field.Field;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//
///**
// * shard 설정의 filter에 맞는 문서인지를 판가름한다.
// * 향후 복잡한 filer식도 지원하도록 한다. >= 와 boolean연산. 그리고 다양한 필드셋팅에 대해서..
// * */
//public class ShardFilter {
//	private static Logger logger = LoggerFactory.getLogger(ShardFilter.class);
//	
//	public static String ROOT_FILTER = "ALL";
//	private Integer index;
//	protected Set<String> dataSet;
//	private boolean hasAll;
//	
//	public ShardFilter(Map<String, Integer> fieldSequenceMap, String filter) {
//		filter = filter.trim();
//		
//		if(filter.equals(ROOT_FILTER)){
//			hasAll = true;
//			return;
//		}
//		
//		dataSet = new HashSet<String>();
//		
//		try{
//			int pos = filter.indexOf('=');
//			
//			String fieldId = filter.substring(0, pos).trim().toUpperCase();
//			String value = filter.substring(pos + 1).trim();
//			String[] values = null;
//			if(value.startsWith("(")){
//				values = value.substring(1, value.length() - 1).split(",");
//			}
//			
//			
//			if(values != null){
//				//multi value
//				for (int i = 0; i < values.length; i++) {
//					dataSet.add(stripString(values[i].trim()));
//				}
//			}else{
//				//single value
//				dataSet.add(stripString(value));
//			}
//	
//			index = fieldSequenceMap.get(fieldId);
//			
//			logger.debug("shard filter id={} index={} dataSet={}", fieldId, index, dataSet);
//		}catch(Exception e){
//			logger.error("Filter condition has error.filter >> \"{}\"", filter);
//		}
//	}
//
//	private String stripString(String str){
//		if(str.startsWith("'") && str.endsWith("'")){
//			return str.substring(1, str.length() - 1);
//		}
//		return str;
//	}
//	
//	public boolean accept(Document document) {
//		if(hasAll){
//			return true;
//		}
//		if(index == null){
//			return false;
//		}
//		
//		Field field = document.get(index);
//		if(field == null){
//			return false;
//		}
//		
//		String fieldValue = field.getDataString();
//		return dataSet.contains(fieldValue);
//	}
//}
