package org.fastcatsearch.job.management;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.fastcatsearch.common.io.Streamable;
import org.fastcatsearch.exception.FastcatSearchException;
import org.fastcatsearch.ir.IRService;
import org.fastcatsearch.ir.config.CollectionConfig;
import org.fastcatsearch.ir.config.DataPlanConfig;
import org.fastcatsearch.ir.io.DataInput;
import org.fastcatsearch.ir.io.DataOutput;
import org.fastcatsearch.ir.search.CollectionHandler;
import org.fastcatsearch.job.Job;
import org.fastcatsearch.service.ServiceManager;
import org.fastcatsearch.util.CollectionContextUtil;
import org.fastcatsearch.util.FilePaths;

/*
 * 컬렉션 생성 job
 * */
public class CreateCollectionJob extends Job implements Streamable {

	private static final long serialVersionUID = 3159374972331262216L;

	private String collectionId;
	private String collectionName;
	private String indexNode;
	private String searchNodeListString;
	private String dataNodeListString;
	
	public CreateCollectionJob() {}
	
	
	public CreateCollectionJob(String collectionId, String collectionName, String indexNode, String searchNodeListString, String dataNodeListString) {
		this.collectionId = collectionId;
		this.collectionName = collectionName;
		this.indexNode = indexNode;
		this.searchNodeListString = searchNodeListString;
		this.dataNodeListString = dataNodeListString;
	}
	
	@Override
	public void readFrom(DataInput input) throws IOException {
		collectionId = input.readString();
		collectionName = input.readString();
		indexNode = input.readString();
		searchNodeListString = input.readString();
		dataNodeListString = input.readString();
	}

	@Override
	public void writeTo(DataOutput output) throws IOException {
		output.writeString(collectionId);
		output.writeString(collectionName);
		output.writeString(indexNode);
		output.writeString(searchNodeListString);
		output.writeString(dataNodeListString);
	}

	@Override
	public JobResult doRun() throws FastcatSearchException {
		List<String> searchNodeList = new ArrayList<String>();
		if (searchNodeListString != null) {
			for (String nodeStr : searchNodeListString.split(",")) {
				nodeStr = nodeStr.trim();
				if (nodeStr.length() > 0) {
					searchNodeList.add(nodeStr);
				}
			}
		}

		List<String> dataNodeList = new ArrayList<String>();
		if (dataNodeListString != null) {
			for (String nodeStr : dataNodeListString.split(",")) {
				nodeStr = nodeStr.trim();
				if (nodeStr.length() > 0) {
					dataNodeList.add(nodeStr);
				}
			}
		}

		try {
			boolean isSuccess = false;
			
			IRService irService = ServiceManager.getInstance().getService(IRService.class);
			
			CollectionHandler collectionHandler = irService.collectionHandler(collectionId);
			// 이미 컬렉션 데이터가 존재한다면, 그대로 로딩한다.
			if(collectionHandler == null) {
				CollectionConfig collectionConfig = new CollectionConfig(collectionName, indexNode, searchNodeList, dataNodeList, DataPlanConfig.DefaultDataPlanConfig);
	
				collectionHandler = irService.createCollection(collectionId, collectionConfig, true);
				
				isSuccess = (collectionHandler != null);
			}else{
				CollectionConfig collectionConfig = collectionHandler.collectionContext().collectionConfig();
				collectionConfig.setName(collectionName);
				collectionConfig.setIndexNode(indexNode);
				collectionConfig.setSearchNodeList(searchNodeList);
				collectionConfig.setDataNodeList(dataNodeList);
				FilePaths collectionFilePaths = collectionHandler.collectionContext().collectionFilePaths();
				isSuccess = CollectionContextUtil.writeConfigFile(collectionConfig, collectionFilePaths);
			}
			
			return new JobResult(isSuccess);
			
		} catch (Exception e) {
			throw new FastcatSearchException(e);
		}
	}

}
