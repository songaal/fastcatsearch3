package org.fastcatsearch.ir.search;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.fastcatsearch.ir.io.DataRef;

public class IndexRef<T extends ReferencableIndexReader> {
	protected List<T> readerList;
	protected List<DataRef> dataRefList;
//	protected Map<String, ReaderSequencePair<T>> readerSequencePairMap;
	public IndexRef(){
		this(5);
	}
	public IndexRef(int size){
		readerList = new ArrayList<T>(size);
		dataRefList = new ArrayList<DataRef>(size);
//		readerSequencePairMap = new HashMap<String, ReaderSequencePair<T>>(size);
		readerList = new ArrayList<T>(size);
	}
	
	//모든 색인 reader를 read한다.
	//multi value field의 경우 데이터를 모두 메모리로 올릴수 없기때문에, single value field 도 하나씩 읽도록 하여 로직을 통일한다.  
	public void read(int docNo) throws IOException{
		for (T reader : readerList) {
			reader.read(docNo);
		}
	}
	public List<DataRef> getDataRefList(){
		return dataRefList;
	}
	public DataRef getDataRef(int sequence){
		return dataRefList.get(sequence);
	}
	
	public void add(String fieldId, T reader) throws IOException {
		if(!readerList.contains(reader)){
			readerList.add(reader);
		}
		
		DataRef dataRef = reader.getRef();
		dataRefList.add(dataRef);
//		readerSequencePairMap.put(fieldId, new ReaderSequencePair<T>(reader, indexSequence));
		readerList.add(reader);
	}
	
	public int getSize(){
		return dataRefList.size();
	}
	public T getReader(int sequence){
		return readerList.get(sequence);
	}
	
//	public ReaderSequencePair<T> getReaderSequencePair(String fieldId){
//		return readerSequencePairMap.get(fieldId);
//	}
	
//	public static class ReaderSequencePair<T> {
//		private T reader;
//		private int indexSequence;
//		public ReaderSequencePair(T reader, int indexSequence) {
//			this.reader = reader;
//			this.indexSequence = indexSequence;
//		}
//		
//		public T reader(){
//			return reader;
//		}
//		
//		public int sequence(){
//			return indexSequence;
//		}
//	}
}
