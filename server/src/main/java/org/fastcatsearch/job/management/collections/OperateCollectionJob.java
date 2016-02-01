package org.fastcatsearch.job.management.collections;

import org.apache.commons.io.FileUtils;
import org.fastcatsearch.common.io.Streamable;
import org.fastcatsearch.exception.FastcatSearchException;
import org.fastcatsearch.ir.DynamicIndexModule;
import org.fastcatsearch.ir.IRService;
import org.fastcatsearch.ir.io.DataInput;
import org.fastcatsearch.ir.io.DataOutput;
import org.fastcatsearch.ir.search.CollectionHandler;
import org.fastcatsearch.job.Job;
import org.fastcatsearch.service.ServiceManager;
import org.fastcatsearch.util.CollectionContextUtil;

import java.io.IOException;

public class OperateCollectionJob extends Job implements Streamable {

	private static final long serialVersionUID = -3982783541369359503L;

	private String collectionId;
	private String command;
	
	public OperateCollectionJob() {}
	
	public OperateCollectionJob(String collectionId, String command) {
		this.collectionId = collectionId;
		this.command = command;
	}

	@Override
	public void readFrom(DataInput input) throws IOException {
		collectionId = input.readString();
		command = input.readString();
	}

	@Override
	public void writeTo(DataOutput output) throws IOException {
		output.writeString(collectionId);
		output.writeString(command);
	}

	@Override
	public JobResult doRun() throws FastcatSearchException {
		
		String errorMessage = null;
		
		try {
			IRService irService = ServiceManager.getInstance().getService(IRService.class);

			CollectionHandler collectionHandler = irService.collectionHandler(collectionId);
			

			if ("START".equalsIgnoreCase(command)) {
				if(collectionHandler == null) {
					irService.loadCollectionHandler(collectionId);
					return new JobResult(true);
				}else{
					if(collectionHandler.isLoaded()){
						errorMessage = "Collection [" + collectionId + "] is already started.";
						return new JobResult(errorMessage);
					}else{
						irService.loadCollectionHandler(collectionId);
						return new JobResult(true);
					}
				}
			}
			
			if (collectionHandler == null) {
				errorMessage = "Collection [" + collectionId + "] is not exist.";
				return new JobResult(errorMessage);
			}

			if ("STOP".equalsIgnoreCase(command)) {
				if(!collectionHandler.isLoaded()){
					errorMessage = "Collection [" + collectionId + "] is already stoped.";
					return new JobResult(errorMessage);
				}
                DynamicIndexModule dynamicIndexModule = irService.getDynamicIndexModule(collectionId);
                if(dynamicIndexModule != null) {
                    dynamicIndexModule.unload();
                }
				collectionHandler.close();
			} else if ("REMOVE".equalsIgnoreCase(command)) {
				boolean isSuccess = irService.removeCollection(collectionId);
				return new JobResult(isSuccess);
            } else if ("TRUNCATE".equalsIgnoreCase(command)) {
                DynamicIndexModule dynamicIndexModule = irService.getDynamicIndexModule(collectionId);
                if(dynamicIndexModule != null) {
                    dynamicIndexModule.unload();
                }
                if(collectionHandler != null) {
                    collectionHandler.close();
                }
                FileUtils.deleteQuietly(collectionHandler.collectionContext().dataFilePaths().file());
                FileUtils.deleteQuietly(collectionHandler.collectionContext().indexLogPaths().file());

                collectionHandler.collectionContext().clearDataInfoAndStatus();
                CollectionContextUtil.saveCollectionAfterIndexing(collectionHandler.collectionContext());

                irService.loadCollectionHandler(collectionId);
                return new JobResult(true);
			} else {
				errorMessage = "Cannot understand command > " + command;
				return new JobResult(errorMessage);
			}

			return new JobResult(true);
		} catch (Exception e) {
			throw new FastcatSearchException(e);
		}
	}

}
