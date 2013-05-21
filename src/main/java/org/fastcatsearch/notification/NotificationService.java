package org.fastcatsearch.notification;

import org.fastcatsearch.cluster.NodeService;
import org.fastcatsearch.control.JobService;
import org.fastcatsearch.env.Environment;
import org.fastcatsearch.exception.FastcatSearchException;
import org.fastcatsearch.notification.message.Notification;
import org.fastcatsearch.service.AbstractService;
import org.fastcatsearch.service.ServiceManager;
import org.fastcatsearch.settings.Settings;

public class NotificationService extends AbstractService {

	private NodeService nodeService;
	private boolean isMasterNode;
	
	public NotificationService(Environment environment, Settings settings, ServiceManager serviceManager) {
		super(environment, settings, serviceManager);
		
	}

	@Override
	protected boolean doStart() throws FastcatSearchException {
		
		nodeService = serviceManager.getService(NodeService.class);
		isMasterNode = nodeService.isMaster();
		//
		//TODO 셋팅을 읽어들인후 db, sms, email 등 설정.
		if(settings.getBoolean("email.use")){
			
			
		}else if(settings.getBoolean("sms.use")){
			
			
		}
		
		return true;
	}

	@Override
	protected boolean doStop() throws FastcatSearchException {
		return true;
	}

	@Override
	protected boolean doClose() throws FastcatSearchException {
		return true;
	}
	
	
	/**
	 *  해당 notification의 통지설정에 따라서 관리자에게 통지서비스를 하고, db에 기록한다.
	 * */
	public void notify(Notification notification){
		
		NotificationJob notificationJob = new NotificationJob(notification);
		
		if(isMasterNode){
			//master면 바로 실행.
			ServiceManager.getInstance().getService(JobService.class).execute(notificationJob);
		}else{
			//slave라면 master에게 보낸다.
			nodeService.sendRequestToMaster(notificationJob);
		}
	}

	
	protected void handleNotification(Notification notification){
		
		logger.debug("Notification 통지 >> {}", notification.toMessageString());
//		if(notification instanceof IndexingStartNotification){
//			//
//			
//			
//		}else if(notification instanceof IndexingFinishNotification){
//			//
//			
//			
//			
//		}
	}
	


}
