package org.fastcatsearch.ir.search;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.fastcatsearch.ir.io.DataRef;

public class IndexRef<T extends ReferenceableReader> {
	protected List<T> readerList;
	protected List<DataRef> dataRefList;
	protected Map<Integer, T> readerSequenceMap;
	protected int sequence;
    protected Map<Integer, T[]> readersSequenceMap;

	public IndexRef(){
		this(5);
	}
	public IndexRef(int size){
		readerList = new ArrayList<T>(size);
		dataRefList = new ArrayList<DataRef>(size);
		readerSequenceMap = new HashMap<Integer, T>(size);
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
        readerSequenceMap.put(sequence++, reader);

        if(reader == null){
            dataRefList.add(DataRef.EMPTY_DATAREF);
            return;
        }


        //unique한 reader 리스트.
        if(!readerList.contains(reader)){
            readerList.add(reader);
        }

        DataRef dataRef = reader.getRef();
        dataRefList.add(dataRef);
	}
	
	public int getSize(){
		return sequence;
	}
	public T getReader(int sequence){
		return readerSequenceMap.get(sequence);
	}

    public T[] getReaders(int sequence){
        return readersSequenceMap.get(sequence);
    }
	
}
