package org.fastcatsearch.job.management;

import java.io.IOException;

import javax.xml.bind.JAXBException;

import org.fastcatsearch.cluster.NodeService;
import org.fastcatsearch.common.io.Streamable;
import org.fastcatsearch.exception.FastcatSearchException;
import org.fastcatsearch.ir.IRService;
import org.fastcatsearch.ir.common.IRException;
import org.fastcatsearch.ir.common.SettingException;
import org.fastcatsearch.ir.config.CollectionConfig;
import org.fastcatsearch.ir.config.CollectionContext;
import org.fastcatsearch.ir.io.DataInput;
import org.fastcatsearch.ir.io.DataOutput;
import org.fastcatsearch.job.Job;
import org.fastcatsearch.service.ServiceManager;
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
		IRService irService = ServiceManager.getInstance().getService(IRService.class);
		
		boolean collectionExists = false;
		boolean isSuccess = false;
		//객체도 함께 즉시 업데이트 해준다.
		CollectionContext collectionContext = irService.collectionContext(collectionId);
		CollectionConfig collectionConfig = null;
		if(collectionContext != null) {
			collectionExists = true;
			collectionConfig = collectionContext.collectionConfig();
		} else {
			try {
				irService.removeCollection(collectionId);
			} catch (SettingException ignore) {
			}
			collectionConfig = new CollectionConfig();
		}
		collectionConfig.setName(this.collectionConfig.getName());
		collectionConfig.setIndexNode(this.collectionConfig.getIndexNode());
		collectionConfig.setSearchNodeList(this.collectionConfig.getSearchNodeList());
		collectionConfig.setDataNodeList(this.collectionConfig.getDataNodeList());
		collectionConfig.setDataPlanConfig(this.collectionConfig.getDataPlanConfig());
		collectionConfig.setFullIndexingSegmentSize(this.collectionConfig.getFullIndexingSegmentSize());
	
		Exception ex = null;
		try {
			if(!collectionExists) {
				irService.createCollection(collectionId, collectionConfig, true);
			}
			isSuccess = CollectionContextUtil.writeConfigFile(collectionConfig, collectionFilePaths);
		} catch (IRException e) {
			ex = e;
		} catch (SettingException e) {
			ex = e;
		} finally {
			if(ex != null) {
				logger.error("", ex);
				isSuccess = false;
			}
		}
		
		NodeService nodeService = ServiceManager.getInstance().getService(NodeService.class);
		nodeService.updateLoadBalance(collectionId, collectionConfig.getDataNodeList());
		
		return new JobResult(isSuccess);
	}

}
