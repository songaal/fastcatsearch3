//package org.fastcatsearch.ir.dic;
//
//import java.lang.reflect.Constructor;
//import java.lang.reflect.InvocationTargetException;
//
//import org.fastcatsearch.ir.config.IRConfig;
//import org.fastcatsearch.ir.config.IRSettings;
//
///**
// * [dic/korean/settings.conf]
//	pool.max = 10
//	pool.core = 3
//	system.dic.path = system.dic
//	synonym.dic.path = synonym.dic
//	
//	public KoreanDictionaryPool(settings){
//		settings.getInt("pool.max");
//		settings.getInt("pool.core");
////		settings.getString("system.dic.path");
//		settings.getString("user.dic.path");
//		settings.getString("stop.dic.path");
//		settings.getString("synonym.dic.path");
//		
//		//core갯수만큼 dic준비.
//	}
//	
//	
//	
//	
//	KoreanDictionaryPool pool;
//	
//	public KoreanTokenizer(){
//		pool = DictionaryLoader.load("korean", KoreanDictionaryPool.class); //동기화됨. 이미 다른데서 로드했다면 그냥 pool ref만 넘겨줌.
//		HashSetDictionary dic = pool.getSystem();
//		HashSetDictionary dic = pool.getUser();
//		HashSetDictionary dic = pool.getStop();
//		HashMapDictionary dic = pool.getSynonym();
//	}
//	
//	public boolean hasToken(String term){
//	
//		
//	
//	}
//	
//	
//	KoreanDictionaryPool pool = DictionaryLoader.reload("korean", KoreanDictionaryPool.class);
//	
//	
//	
//	boolean exist = dic.hasToken("");
//	
//
// * 
// * 
// * */
//public class DictionaryLoader {
//	public static <T extends DictionaryPool<? extends Dictionary>> T load(String dictionaryName, Class<T> clazz){
//		//TODO
//		//로드되었는지 먼저 확인후 로드한다.
//		
//		try {
//			String configPath = "dic/"+dictionaryName+"/settings.conf";
//			IRConfig config = IRSettings.getConfig(configPath);
//			Constructor<T> constructor = clazz.getConstructor(IRConfig.class);
//			T t = constructor.newInstance(config);
//			t.prepare();
//			return t;
//		} catch (InstantiationException e) {
//			e.printStackTrace();
//		} catch (IllegalAccessException e) {
//			e.printStackTrace();
//		} catch (SecurityException e) {
//			e.printStackTrace();
//		} catch (NoSuchMethodException e) {
//			e.printStackTrace();
//		} catch (IllegalArgumentException e) {
//			e.printStackTrace();
//		} catch (InvocationTargetException e) {
//			e.printStackTrace();
//		}
//		return null;
//	}
//	
//	public static <T extends DictionaryPool> T reload(String dictionaryName, Class<T> clazz){
//		return null;
//	}
//	
//}
