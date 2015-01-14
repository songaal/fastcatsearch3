package org.fastcatsearch.datasource.reader;

import java.io.File;
import java.util.Map;

import org.fastcatsearch.datasource.SourceModifier;
import org.fastcatsearch.ir.common.IRException;
import org.fastcatsearch.ir.config.DataSourceConfig;
import org.fastcatsearch.ir.config.SingleSourceConfig;
import org.fastcatsearch.ir.settings.SchemaSetting;

/**
 * nsf 와 ftp등을 이용할수 없는 원격파일등에 대해서 
 * 원격 저장소 데몬을 띄워 놓으면, RemoteResourceReader가 접속하여 리소스들을 가져올수 있도로 한다.
 * 내부적으로 RemoteResourceRepositoryConnector 가 파일(or Stream)을 가져오고
 * 이 클래스에서는 이들을 가공하여 Document를 생성한다.
 * RemoteResourceRepository는 내부적으로 transport 모듈을 사용하여 통신상의 통일성을 갖도록 한다.
 * */
public class RemoteResourceReader extends SingleSourceReader {

	
	public RemoteResourceReader(String collectionId, File filePath, SingleSourceConfig sourceConfig, SourceModifier sourceModifier, String lastIndexTime){
		super(collectionId, filePath, sourceConfig, sourceModifier, lastIndexTime);
		//TODO
		// 1. 외부 리소스 저장소에 연결.
		// 2. 디렉토리 또는 파일요청 (셋팅파일에 정의)
		//
		
	}
	@Override
	public boolean hasNext() throws IRException {
		// TODO Auto-generated method stub
		return false;
	}


	class RemoteResourceRepositoryConnector {
		
		public void connect(){ 
			
		}
		
		public boolean next(){ 
			return false;
		}
		
		public File readFile(){ 
			return null;
		}
		
	}


	@Override
	protected Map<String, Object> next() throws IRException {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public void close() throws IRException {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void init() throws IRException {
		// TODO Auto-generated method stub
		
	}
	@Override
	protected void initParameters() {
		// TODO Auto-generated method stub
		
	}
}
