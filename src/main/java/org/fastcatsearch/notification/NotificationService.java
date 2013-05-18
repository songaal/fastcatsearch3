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
		
		
		if(isMasterNode){
			//master면 바로 사용.
			//sms, mail alert
			//db저장.
			//TODO 
			
			
			handleSystemNotification(notification);
			handleUserNotification(notification);
			
			
		}else{
			//slave면 master에 통보.
			nodeService.sendRequestToMaster(notificationJob);
		}
	}

	private void handleSystemNotification(Notification notification){
		
//		//엔진 DB에 적어준다.
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
	
	private void handleUserNotification(Notification notification){
		
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
