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
	private int defaultSize;

	public IndexRef(){
		this(5);
	}
	public IndexRef(int size){
        this.defaultSize = size;
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
	
	public void add(String fieldId, T... reader) throws IOException {
        if(reader.length > 1) {
            if(readersSequenceMap == null) {
                readersSequenceMap = new HashMap<Integer, T[]>(defaultSize);
            }
            readersSequenceMap.put(sequence++, reader);
        } else {
            readerSequenceMap.put(sequence++, reader[0]);
        }

		if(reader == null){
			dataRefList.add(DataRef.EMPTY_DATAREF);
			return;
		}

        T finalReader = null;
		
		//unique한 reader 리스트.
        if(reader.length > 1) {
            finalReader = (T) new CompoundReferenceableIndexReader(reader);
        } else {
            finalReader = reader[0];
        }

		if(!readerList.contains(finalReader)){
			readerList.add(finalReader);
		}
		
		DataRef dataRef = finalReader.getRef();
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
