package org.fastcatsearch.datasource.reader;

import java.util.List;

import org.fastcatsearch.datasource.SourceModifier;
import org.fastcatsearch.env.Path;
import org.fastcatsearch.ir.common.IRException;
import org.fastcatsearch.ir.document.Document;
import org.fastcatsearch.ir.settings.Schema;

/**
 * 여러개의 reader를 모아서 multi source read가 가능하게하는 소스리더.
 * */
public class CompositeDataSourceReader extends DataSourceReader {

	private int pos;
	private DataSourceReader sourceReader;
	private List<DataSourceReader> sourceReaderList;
	
	public CompositeDataSourceReader(List<DataSourceReader> readerList) throws IRException{
		
	}
	
	@Override
	public boolean hasNext() throws IRException {
		if(sourceReader.hasNext()){
			return true;
		}else{
			if(pos < sourceReaderList.size()){
				//먼저 기존의 reader를 닫는다.
				sourceReader.close();
				//다른 reader를 생성한다.
//				if(!createSourceReader(schema, sourceReaderList.get(pos), isFull)){
//					return false;
//				}
				sourceReader = sourceReaderList.get(pos);
				pos++;
				if(sourceReader.hasNext()){
					return true;
				}else{
					while (!sourceReader.hasNext()) {
						if(pos < sourceReaderList.size()){
							//먼저 기존의 reader를 닫는다.
							sourceReader.close();
							//다른 reader를 생성한다.
//							if(!createSourceReader(schema, settingList.get(pos), isFull)){
//								return false;
//							}
							sourceReader = sourceReaderList.get(pos);
							pos++;
						}else{
							return false;
						}
					}
					return true;
				}
			}else{
				//더이상의 다른 reader가 없다.
				return false;
			}
		}
	}

	@Override
	public Document next() throws IRException {
		return sourceReader.next();
	}

	@Override
	public void close() throws IRException {
		sourceReader.close();
	}

}
