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
	protected Map<String, ReaderSequencePair<T>> readerSequencePairMap;
	public IndexRef(){
		this(5);
	}
	public IndexRef(int size){ 
		readerList = new ArrayList<T>(size);
		dataRefList = new ArrayList<DataRef>(size);
		readerSequencePairMap = new HashMap<String, ReaderSequencePair<T>>(size);
	}
	
	//모든 색인 reader를 read한다.
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
	
	//indexSequence는 reader의 몇번째 색인필드를 사용하는지 가리킨다.  
	//FIXME read는 같지만 indexSequence가 다를경우, 나중에 읽는 것이 데이터위치가 변경되지 않을까? 
	public void add(String fieldId, T reader, int indexSequence) throws IOException {
		if(!readerList.contains(reader)){
			readerList.add(reader);
		}
		
		DataRef dataRef = reader.getRef(indexSequence);
		dataRefList.add(dataRef);
		readerSequencePairMap.put(fieldId, new ReaderSequencePair<T>(reader, indexSequence));
	}
	
	public int getSize(){
		return dataRefList.size();
	}
	
	
	public ReaderSequencePair<T> getReaderSequencePair(String fieldId){
		return readerSequencePairMap.get(fieldId);
	}
	
	public static class ReaderSequencePair<T> {
		private T reader;
		private int indexSequence;
		public ReaderSequencePair(T reader, int indexSequence) {
			this.reader = reader;
			this.indexSequence = indexSequence;
		}
		
		public T reader(){
			return reader;
		}
		
		public int sequence(){
			return indexSequence;
		}
	}
}
