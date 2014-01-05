package org.fastcatsearch.job.management;

import java.io.IOException;

import javax.xml.bind.JAXBException;

import org.fastcatsearch.common.io.Streamable;
import org.fastcatsearch.exception.FastcatSearchException;
import org.fastcatsearch.ir.config.CollectionConfig;
import org.fastcatsearch.ir.io.DataInput;
import org.fastcatsearch.ir.io.DataOutput;
import org.fastcatsearch.job.Job;
import org.fastcatsearch.util.CollectionContextUtil;
import org.fastcatsearch.util.FilePaths;
import org.fastcatsearch.util.JAXBConfigs;

/**
 * 각 노드에 컬렉션 셋팅을 전송 및 반영한다.
 * */
public class UpdateCollectionConfigJob extends Job implements Streamable {

	private static final long serialVersionUID = 526201580792091177L;

	private String collectionId;
	private CollectionConfig collectionConfig;
	
	public UpdateCollectionConfigJob(){
	}
	
	public UpdateCollectionConfigJob(String collectionId, CollectionConfig collectionConfig){
		this.collectionId = collectionId;
		this.collectionConfig = collectionConfig;
	}
	
	@Override
	public void readFrom(DataInput input) throws IOException {
		try {
			this.collectionId = input.readString();
			this.collectionConfig = JAXBConfigs.readFrom(input, CollectionConfig.class);
		} catch (JAXBException e) {
			throw new IOException(e);
		}
	}

	@Override
	public void writeTo(DataOutput output) throws IOException {
		try {
			output.writeString(collectionId);
			JAXBConfigs.writeTo(output, this.collectionConfig, CollectionConfig.class);
		} catch (JAXBException e) {
			throw new IOException(e);
		}
	}

	@Override
	public JobResult doRun() throws FastcatSearchException {
		FilePaths collectionFilePaths = environment.filePaths().collectionFilePaths(this.collectionId);		
		
		boolean isSuccess = CollectionContextUtil.updateConfig(this.collectionConfig, collectionFilePaths);
		
		return new JobResult(isSuccess);
	}

}
