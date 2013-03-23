/*
 * Copyright (c) 2013 Websquared, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 * 
 * Contributors:
 *     swsong - initial API and implementation
 */

package org.fastcatsearch.job;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.fastcatsearch.cluster.Node;
import org.fastcatsearch.cluster.NodeService;
import org.fastcatsearch.common.io.StreamInput;
import org.fastcatsearch.common.io.StreamOutput;
import org.fastcatsearch.control.JobException;
import org.fastcatsearch.control.ResultFuture;
import org.fastcatsearch.data.DataService;
import org.fastcatsearch.data.DataStrategy;
import org.fastcatsearch.ir.config.IRSettings;
import org.fastcatsearch.job.result.IndexingJobResult;
import org.fastcatsearch.log.EventDBLogger;
import org.fastcatsearch.service.ServiceException;
import org.fastcatsearch.transport.common.SendFileResultFuture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/*
 * 전체색인을 수행하여 색인파일을 생성하고,
 * 해당하는 data node에 색인파일을 복사한다.
 * 
 * */
public class NodeFullIndexJob extends StreamableJob {
	private static Logger indexingLogger = LoggerFactory.getLogger("INDEXING_LOG");
	
	String collectionId;
	
	public NodeFullIndexJob() { }
	
	public NodeFullIndexJob(String collectionId) {
		this.collectionId = collectionId;
	}


	@Override
	public JobResult doRun() throws JobException, ServiceException {
		
		IndexingJobResult indexingJobResult = null;
		try {
			FullIndexJob fullIndexJob = new FullIndexJob();
			fullIndexJob.setArgs(new String[]{collectionId});
			ResultFuture resultFuture = getJobExecutor().offer(fullIndexJob);
			indexingJobResult = (IndexingJobResult) resultFuture.take();
			if(!resultFuture.isSuccess()){
				throw new JobException("색인파일 생성에 실패하여 작업을 중단합니다.");
			}
			
			/*
			 * 색인파일 원격복사.
			 * */
			DataStrategy dataStrategy = DataService.getInstance().getCollectionDataStrategy(collectionId);
			List<Node> nodeList = dataStrategy.dataNodes();
			if(nodeList == null || nodeList.size() == 0){
				throw new JobException("색인파일을 복사할 노드가 정의되어있지 않습니다.");
			}
			
			File segmentDir = indexingJobResult.segmentDir;
			
			Collection<File> files = FileUtils.listFiles(segmentDir, null, true);
			int fileCount = files.size();
			
			
			for (int i = 0; i < nodeList.size(); i++) {
				Node node = nodeList.get(i);
				Iterator<File> fileIterator = files.iterator();
				int count = 1;
				while(fileIterator.hasNext()){
					File sourceFile = fileIterator.next();
					File targetFile = environment.filePaths().getRelativePathFile(sourceFile);
					logger.debug("sourceFile >> {}", sourceFile.getPath());
					logger.debug("targetFile >> {}", targetFile.getPath());
					logger.info("[{} / {}]파일 {} 전송시작! ", new Object[]{count, fileCount, sourceFile.getPath()});
					SendFileResultFuture sendFileResultFuture = NodeService.getInstance().sendFile(node, sourceFile, targetFile);
					Object result = sendFileResultFuture.take();
					if(sendFileResultFuture.isSuccess()){
						logger.info("[{} / {}]파일 {} 전송완료!", new Object[]{count, fileCount, sourceFile.getPath()});
					}else{
						throw new JobException("파일전송에 실패했습니다.");
					}
					count++;
				}
				
			}
			
			
		} catch (Exception e) {
			EventDBLogger.error(EventDBLogger.CATE_INDEX, "전체색인에러", EventDBLogger.getStackTrace(e));
			indexingLogger.error("["+collectionId+"] Indexing error = "+e.getMessage(),e);
			throw new JobException(e);
		}
		
		return new JobResult(indexingJobResult);
	}


	@Override
	public void readFrom(StreamInput input) throws IOException {
		collectionId = input.readString();
	}


	@Override
	public void writeTo(StreamOutput output) throws IOException {
		output.writeString(collectionId);
	}
	


}
