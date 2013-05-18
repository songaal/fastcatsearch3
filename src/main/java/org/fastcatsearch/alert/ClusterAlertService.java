package org.fastcatsearch.alert;

import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicInteger;

import org.fastcatsearch.cluster.Node;
import org.fastcatsearch.cluster.NodeService;
import org.fastcatsearch.control.JobService;
import org.fastcatsearch.env.Environment;
import org.fastcatsearch.exception.FastcatSearchException;
import org.fastcatsearch.notification.NotificationService;
import org.fastcatsearch.service.AbstractService;
import org.fastcatsearch.service.ServiceManager;
import org.fastcatsearch.settings.Settings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * master노드에서 실행되는 알러트서비스. cluster노드에서 발생한 예외들이 모두 전달된다.
 * */
public class ClusterAlertService extends AbstractService {
	protected static final Logger alertLogger = LoggerFactory.getLogger("ALERT_LOG");

	private NodeService nodeService;
	private boolean isMasterNode;
	private static ClusterAlertService instance;
	
	private Map<NodeErrorKey, AtomicInteger> exceptionCountMap;

	private Timer alertServiceTimer;

	public ClusterAlertService(Environment environment, Settings settings, ServiceManager serviceManager) {
		super(environment, settings, serviceManager);
	}

	public static ClusterAlertService getInstance(){
		return instance;
	}
	
	public void asSingleton(){
		instance = this;
	}
	
	@Override
	protected boolean doStart() throws FastcatSearchException {
		
		nodeService = serviceManager.getService(NodeService.class);
		isMasterNode = nodeService.isMaster();
		
		exceptionCountMap = new HashMap<NodeErrorKey, AtomicInteger>();

		// 1초이내에 들어오는 중복에러는 모아서 처리한다.
		alertServiceTimer = new Timer("AlertServiceCountMapClear", true);
		alertServiceTimer.schedule(new CountMapClearTimerTask(), 1000, 1000);
		return true;
	}

	@Override
	protected boolean doStop() throws FastcatSearchException {
		alertServiceTimer.cancel();
		exceptionCountMap.clear();
		return true;
	}

	@Override
	protected boolean doClose() throws FastcatSearchException {
		return true;
	}
	
	// 여러번의 동일한 exception이 들어올수도 있으므로 쌓아두면서 일괄 처리한다.
	// 만약 관리자가 mail-alert, sms-alert등을 원할경우 해당 서비스를 호출해준다.
	protected void handleException(Node node, Throwable e) {
		int count = getThrowableCount(node, e);

		if (count > 1) {
			//두번째부터는 최종스택만 출력한다.
			logger.error("[{}] [{}] {}", node, count, e.getStackTrace()[0]);
		} else {
			logger.error("[{}] exception", e);
		}
		
		if(e instanceof OutOfMemoryError){
			NotificationService notificationService = ServiceManager.getInstance().getService(NotificationService.class);
			notificationService.notify();
		}
		
	}

	private int getThrowableCount(Node node, Throwable e) {
		// 발생 원인은 0에
		StackTraceElement errorSpot = e.getStackTrace()[0];
		NodeErrorKey key = new NodeErrorKey(node, errorSpot);
		AtomicInteger count = exceptionCountMap.get(key);
		if (count != null) {
			// 이미 발생한 에러면.
			return count.incrementAndGet();
		} else {
			count = new AtomicInteger(1);
			exceptionCountMap.put(key, count);
			return count.intValue();
		}
	}

	class CountMapClearTimerTask extends TimerTask {

		@Override
		public void run() {
			if (exceptionCountMap.size() > 0) {
				// 지워준다.
				exceptionCountMap = new HashMap<NodeErrorKey, AtomicInteger>();
			}

		}

	}
	
	class NodeErrorKey {
		private Node node;
		private StackTraceElement el;
		NodeErrorKey(Node node, StackTraceElement el){
			this.node = node;
			this.el = el;
		}
		
		@Override
		public boolean equals(Object obj){
			if(obj instanceof NodeErrorKey){
				NodeErrorKey key = (NodeErrorKey) obj;
				return this.node.equals(key.node) && this.el.equals(key.el);
			}
			return false;
		}
		
	}

	/**
	 * 해당 노드에서 발생한 에러를 마스터 노드에 알린다.
	 * */
	public void alert(Throwable e) {
		
		FastcatSearchAlertJob alertJob = new FastcatSearchAlertJob(nodeService.getMyNode(), e);
		//isMaster
		if(isMasterNode){
			//master면 바로 실행.
			ServiceManager.getInstance().getService(JobService.class).execute(alertJob);
		}else{
			//slave라면 master에게 보낸다.
			nodeService.sendRequestToMaster(alertJob);
		}
		
		
	}
}
