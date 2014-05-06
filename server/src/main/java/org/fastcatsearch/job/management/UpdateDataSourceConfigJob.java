package org.fastcatsearch.job.management;

import java.io.IOException;

import javax.xml.bind.JAXBException;

import org.fastcatsearch.common.io.Streamable;
import org.fastcatsearch.exception.FastcatSearchException;
import org.fastcatsearch.ir.IRService;
import org.fastcatsearch.ir.config.CollectionContext;
import org.fastcatsearch.ir.config.DataSourceConfig;
import org.fastcatsearch.ir.io.DataInput;
import org.fastcatsearch.ir.io.DataOutput;
import org.fastcatsearch.job.Job;
import org.fastcatsearch.service.ServiceManager;
import org.fastcatsearch.util.CollectionContextUtil;
import org.fastcatsearch.util.FilePaths;
import org.fastcatsearch.util.JAXBConfigs;

/**
 * 각 노드에 데이터소스설정을 전송 및 반영한다.
 * */
public class UpdateDataSourceConfigJob extends Job implements Streamable {

	private static final long serialVersionUID = -7927522045710504365L;
	
	private String collectionId;
	private DataSourceConfig dataSourceConfig;
	
	public UpdateDataSourceConfigJob(){
	}
	
	public UpdateDataSourceConfigJob(String collectionId, DataSourceConfig dataSourceConfig){
		this.collectionId = collectionId;
		this.dataSourceConfig = dataSourceConfig;
	}
	
	@Override
	public void readFrom(DataInput input) throws IOException {
		try {
			this.collectionId = input.readString();
			this.dataSourceConfig = JAXBConfigs.readFrom(input, DataSourceConfig.class);
		} catch (JAXBException e) {
			throw new IOException(e);
		}
	}

	@Override
	public void writeTo(DataOutput output) throws IOException {
		try {
			output.writeString(collectionId);
			JAXBConfigs.writeTo(output, this.dataSourceConfig, DataSourceConfig.class);
		} catch (JAXBException e) {
			throw new IOException(e);
		}
	}

	@Override
	public JobResult doRun() throws FastcatSearchException {
		FilePaths collectionFilePaths = environment.filePaths().collectionFilePaths(this.collectionId);		
		
		boolean isSuccess = CollectionContextUtil.writeConfigFile(this.dataSourceConfig, collectionFilePaths);
		if(isSuccess) {
			IRService irService = ServiceManager.getInstance().getService(IRService.class);
			CollectionContext collectionContext = irService.collectionContext(collectionId);
			if(collectionContext != null) {
				collectionContext.setDataSourceConfig(dataSourceConfig);
			}else{
				isSuccess = false;
			}
		}
		return new JobResult(isSuccess);
	}

}
