package org.fastcatsearch.job.management;

import java.io.File;
import java.io.IOException;

import org.fastcatsearch.common.io.Streamable;
import org.fastcatsearch.exception.FastcatSearchException;
import org.fastcatsearch.ir.IRService;
import org.fastcatsearch.ir.config.CollectionContext;
import org.fastcatsearch.ir.config.CollectionIndexStatus;
import org.fastcatsearch.ir.io.DataInput;
import org.fastcatsearch.ir.io.DataOutput;
import org.fastcatsearch.ir.search.CollectionHandler;
import org.fastcatsearch.job.Job;
import org.fastcatsearch.service.ServiceManager;
import org.fastcatsearch.settings.SettingFileNames;
import org.fastcatsearch.util.FilePaths;
import org.fastcatsearch.util.JAXBConfigs;

public class RestoreCollectionToPreviousJob extends Job implements Streamable {

	private static final long serialVersionUID = -255187322044455962L;

	private String collectionId;
	private String sequenceString;

	public RestoreCollectionToPreviousJob(){
	}
	
	public RestoreCollectionToPreviousJob(String collectionId, String sequenceString){
		this.collectionId = collectionId;
		this.sequenceString = sequenceString;
	}
	
	@Override
	public JobResult doRun() throws FastcatSearchException {

		boolean isSuccess = false;
		try {
			IRService irService = ServiceManager.getInstance().getService(IRService.class);

			CollectionContext collectionContext = irService.collectionContext(collectionId);
			int sequence = collectionContext.getPreviousDataSequence();
			if (sequenceString != null) {
				try{
					sequence = Integer.parseInt(sequenceString);
				}catch(Exception ignore){ 
				}
			}

			collectionContext.indexStatus().setSequence(sequence);
			FilePaths collectionFilePaths = collectionContext.collectionFilePaths();
			File collectionDir = collectionFilePaths.file();

			JAXBConfigs.writeConfig(new File(collectionDir, SettingFileNames.indexStatus), collectionContext.indexStatus(), CollectionIndexStatus.class);

			CollectionHandler collectionHandler = irService.loadCollectionHandler(collectionId);
			
			isSuccess = (collectionHandler != null && collectionHandler.isLoaded());
		} catch (Exception e) {
			logger.error("", e);
			return new JobResult(false);
		}
		return new JobResult(isSuccess);
	}

	@Override
	public void readFrom(DataInput input) throws IOException {
		collectionId = input.readString();
		sequenceString = input.readString();
	}

	@Override
	public void writeTo(DataOutput output) throws IOException {
		output.writeString(collectionId);
		output.writeString(sequenceString);
	}

}
