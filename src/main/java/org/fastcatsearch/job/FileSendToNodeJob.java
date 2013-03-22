package org.fastcatsearch.job;

import java.io.File;

import org.fastcatsearch.cluster.Node;
import org.fastcatsearch.cluster.NodeService;
import org.fastcatsearch.control.JobException;
import org.fastcatsearch.service.ServiceException;
import org.fastcatsearch.transport.common.SendFileResultFuture;

public class FileSendToNodeJob extends Job {
	private static final long serialVersionUID = 2700311768581340839L;

	@Override
	public Object run0() throws JobException, ServiceException {
		String[] args = getStringArrayArgs();
		if(args.length < 2){
			return false;
		}
		
		String filepath = args[0];
		String nodeId = args[1];
		NodeService nodeService = NodeService.getInstance();
		Node node = nodeService.getNodeById(nodeId);
		File sourceFile = environment.filePaths().getFile(filepath);
		SendFileResultFuture resultFuture = nodeService.sendFile(node, sourceFile, new File(filepath));
		if(resultFuture == null){
			return false;
		}
		
		Object result = resultFuture.take();
		logger.debug("파일전송 결과받음 >> {}", result);
		return new JobResult(resultFuture);
	}

}
