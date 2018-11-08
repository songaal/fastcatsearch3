package org.fastcatsearch.job.indexing;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.fastcatsearch.cluster.ClusterUtils;
import org.fastcatsearch.cluster.Node;
import org.fastcatsearch.cluster.NodeJobResult;
import org.fastcatsearch.cluster.NodeService;
import org.fastcatsearch.common.io.Streamable;
import org.fastcatsearch.control.JobService;
import org.fastcatsearch.control.ResultFuture;
import org.fastcatsearch.db.mapper.IndexingResultMapper.ResultStatus;
import org.fastcatsearch.exception.FastcatSearchException;
import org.fastcatsearch.ir.IRService;
import org.fastcatsearch.ir.common.IndexingType;
import org.fastcatsearch.ir.config.CollectionContext;
import org.fastcatsearch.ir.config.CollectionIndexStatus.IndexStatus;
import org.fastcatsearch.ir.config.DataInfo.SegmentInfo;
import org.fastcatsearch.job.cluster.NodeDirectoryCleanJob;
import org.fastcatsearch.job.result.IndexingJobResult;
import org.fastcatsearch.job.state.IndexingTaskState;
import org.fastcatsearch.node.ReloadNode;
import org.fastcatsearch.service.ServiceManager;
import org.fastcatsearch.transport.vo.StreamableThrowable;
import org.fastcatsearch.util.FilePaths;

public class CollectionFullIndexingStepCopyJob extends IndexingJob{

	private static final long serialVersionUID = 7898036370433248984L;
	
	public CollectionFullIndexingStepCopyJob() {
	}
	
	@Override
	public JobResult doRun() throws FastcatSearchException {
		// TODO Auto-generated method stub
				prepare(IndexingType.FULL, "COPY-INDEX");
				
				Throwable throwable = null;
				ResultStatus resultStatus = ResultStatus.RUNNING;
				Object result = null;
				
				long startTime = System.currentTimeMillis();
				try{
					IRService irService = ServiceManager.getInstance().getService(IRService.class);
					
					CollectionContext collectionContext = irService.collectionContext(collectionId);
					
					if(collectionContext == null){
						throw new FastcatSearchException("Collection [" + collectionId + "] is not exist.");
					}
					
					String indexNodeId = collectionContext.collectionConfig().getIndexNode();
					NodeService nodeService = ServiceManager.getInstance().getService(NodeService.class);
					
					Node indexNode = nodeService.getNodeById(indexNodeId);
					
					if(!nodeService.isMyNode(indexNode)){
						throw new RuntimeException("Invalid index node collection[" + collectionId + "] node[" + indexNodeId + "]");
					}
					
					if(!updateIndexingStatusStart()){
						logger.error("Cannot start indexing job. {} : {}", collectionId, indexNodeId);
						resultStatus = ResultStatus.CANCEL;
						
						return new JobResult();
					}
					
					indexingTaskState.setStep(IndexingTaskState.STEP_FILECOPY);
					
					SegmentInfo segmentInfo = collectionContext.dataInfo().getSegmentInfoList().get(0);
					
					if(segmentInfo != null){
						FilePaths indexFilePaths = collectionContext.indexFilePaths();
						File indexDir = indexFilePaths.file();
						
						List<Node> nodeList = new ArrayList<Node>(nodeService.getNodeById(collectionContext.collectionConfig().getDataNodeList()));
						
						nodeList.remove(nodeService.getMyNode());
						
						File relativeDataDir = environment.filePaths().relativise(indexDir);
						NodeDirectoryCleanJob cleanJob = new NodeDirectoryCleanJob(relativeDataDir);
						
						NodeJobResult[] nodeResultList = null;
						
						nodeResultList = ClusterUtils.sendJobToNodeList(cleanJob, nodeService, nodeList, false);
						
						//성공한 node만 전송.
						nodeList = new ArrayList<Node>();
						for(int i = 0; i < nodeResultList.length; i++){
							NodeJobResult r = nodeResultList[i];
							
							if(r.isSuccess()){
								nodeList.add(r.node());
							}
							else{
								logger.warn("Do not send index file to {}", r.node());
							}
						}
						
						TransferIndexFileMultiNodeJob transferJob = new TransferIndexFileMultiNodeJob(indexDir, nodeList);
						ResultFuture resultFuture = JobService.getInstance().offer(transferJob);
						Object obj = resultFuture.take();
						
						if(resultFuture.isSuccess() && obj != null){
							nodeResultList = (NodeJobResult[]) obj;
						}
						
						nodeList = new ArrayList<Node>();
						for(int i = 0; i < nodeResultList.length; i++){
							NodeJobResult r = nodeResultList[i];
							logger.debug("node#{} >> {}", i, r);
							
							if(r.isSuccess()){
								nodeList.add(r.node());
							}
						}
						
						if(stopRequested){
							throw new IndexingStopException();
						}
						
						Set<Node> reloadNodeSet = new HashSet<Node>();
						
						reloadNodeSet.addAll(nodeList);	// 데이터노드
						reloadNodeSet.add(indexNode);	// 인덱스노드
						reloadNodeSet.add(nodeService.getMasterNode());	// 마스터노드(관리기에 보여지지 위함) 추가
						
						for(String nodeId : collectionContext.collectionConfig().getSearchNodeList()){
							reloadNodeSet.add(nodeService.getNodeById(nodeId));
						}
						
						ReloadNode reloadNode = ReloadNode.getInstance();
						
						reloadNode.putCollectionCopyNode(collectionId, reloadNodeSet);
						reloadNode.putCollectionContext(collectionId, collectionContext);
					}
					
					if(indexingTaskState == null){
						logger.info("copyJob indexingTaskState null");
					}
					else{
						logger.info("copyJob indexingTaskState not null");
					}

					indexingTaskState.setStep(IndexingTaskState.STEP_FINALIZE);
					int duration = (int) (System.currentTimeMillis() - startTime);
					
					IndexStatus indexStatus = collectionContext.indexStatus().getFullIndexStatus();
					indexingLogger.info("[{}] ! time = {}", collectionId, duration);
					
					result = new IndexingJobResult(collectionId, indexStatus, duration);
					resultStatus = ResultStatus.SUCCESS;
					indexingTaskState.setStep(IndexingTaskState.STEP_END);
					return new JobResult(result);
				}
				catch (Exception e) {
					if(stopRequested){
						resultStatus = ResultStatus.STOP;
					}else{
						resultStatus = ResultStatus.CANCEL;
					}
					result = new IndexingJobResult(collectionId, null, (int) (System.currentTimeMillis() - startTime), false);
					return new JobResult(result);
				}
				finally {
					Streamable streamableResult = null;
					if (throwable != null) {
						streamableResult = new StreamableThrowable(throwable);
					} else if (result instanceof Streamable) {
						streamableResult = (Streamable) result;
					}

					updateIndexingStatusFinish(resultStatus, streamableResult);
				}
			}
}
