package org.fastcatsearch.alert;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import javax.management.Notification;

import org.fastcatsearch.cluster.Node;
import org.fastcatsearch.cluster.NodeService;
import org.fastcatsearch.control.JobService;
import org.fastcatsearch.db.DBService;
import org.fastcatsearch.db.InternalDBModule.MapperSession;
import org.fastcatsearch.db.mapper.ExceptionHistoryMapper;
import org.fastcatsearch.db.vo.ExceptionVO;
import org.fastcatsearch.env.Environment;
import org.fastcatsearch.exception.FastcatSearchException;
import org.fastcatsearch.notification.NotificationService;
import org.fastcatsearch.notification.message.CollectionLoadErrorNotification;
import org.fastcatsearch.notification.message.OutOfMemoryNotification;
import org.fastcatsearch.service.AbstractService;
import org.fastcatsearch.service.ServiceManager;
import org.fastcatsearch.settings.Settings;

/**
 * cluster 종합 alert 서비스. cluster노드에서 발생한 예외들이 모두 master에 전달된다.
 * 
 * */
public class ClusterAlertService extends AbstractService {

	private NodeService nodeService;
	private boolean isMasterNode;
	private static ClusterAlertService instance;

	private Map<NodeExceptionInfo, NodeExceptionInfo> exceptionMap;
	private List<NodeExceptionInfo> exceptionList;

	private Timer alertServiceTimer;

	private Object lock = new Object();
	
	public ClusterAlertService(Environment environment, Settings settings, ServiceManager serviceManager) {
		super(environment, settings, serviceManager);
	}

	public static ClusterAlertService getInstance() {
		return instance;
	}

	public void asSingleton() {
		instance = this;
	}

	@Override
	protected boolean doStart() throws FastcatSearchException {

		nodeService = serviceManager.getService(NodeService.class);
		isMasterNode = nodeService.isMaster();

		if(isMasterNode){
			exceptionMap = new ConcurrentHashMap<NodeExceptionInfo, NodeExceptionInfo>();
			exceptionList = new ArrayList<NodeExceptionInfo>();
			
			// 1초이내에 들어오는 중복에러는 모아서 처리한다.
			alertServiceTimer = new Timer("AlertServiceCountMapClear", true);
			alertServiceTimer.schedule(new CountMapClearTimerTask(), 1000, 1000);
		}
		return true;
	}

	@Override
	protected boolean doStop() throws FastcatSearchException {
		if(isMasterNode){
			alertServiceTimer.cancel();
			exceptionMap.clear();
		}
		return true;
	}

	@Override
	protected boolean doClose() throws FastcatSearchException {
		return true;
	}

	// 여러번의 동일한 exception이 들어올수도 있으므로 쌓아두면서 일괄 처리한다.
	protected void handleException(Node node, Throwable e) {
		//master가 아니면 무시.
		if(!isMasterNode){
			return;
		}
		
		NodeExceptionInfo key = new NodeExceptionInfo(node, e);
		
		int count = 0;
		synchronized (lock) {
			NodeExceptionInfo value = exceptionMap.get(key);
			if (value != null) {
				// 이미 발생한 에러면.
				count = value.incrementCount();
				logger.debug("handleException1 > {} : {}", count, e);
			} else {
				exceptionMap.put(key, key);
				key.setTime();
				count = key.getCount();
				//새로추가.
				exceptionList.add(key);
				
				logger.debug("handleException2 > {} : {}", count, e);
			}
		}
		
		if (count > 1) {
			// 두번째부터는 최종스택만 출력한다.
			if (e instanceof FastcatSearchException) {
				logger.error("[{}] [{}] {}", node, count, e);
			} else {
				StackTraceElement[] els = e.getStackTrace();
				if(els.length > 0){
					logger.error("[{}] [{}] {} {}", node, count, e, els[0]);
				}else{
					logger.error("[{}] [{}] {}", node, count, e);
				}
			}
		} else {
			if (e instanceof FastcatSearchException) {
				logger.error("[" + node + "] {}", e.toString());
			} else {
				logger.error("[" + node + "] exception", e);
			}
		}

		if (e instanceof OutOfMemoryError) {
			NotificationService notificationService = ServiceManager.getInstance().getService(NotificationService.class);
			notificationService.sendNotification(new OutOfMemoryNotification(e));
		}

	}

	class CountMapClearTimerTask extends TimerTask {

		@Override
		public void run() {
			if (exceptionList.size() > 0) {
				
				List<NodeExceptionInfo> oldList = null;
				synchronized (lock) {
					oldList = exceptionList;
					exceptionList = new ArrayList<NodeExceptionInfo>();
					exceptionMap = new HashMap<NodeExceptionInfo, NodeExceptionInfo>();
				}
				
				// 지워준다.
				MapperSession<ExceptionHistoryMapper> mapperSession = DBService.getInstance().getMapperSession(ExceptionHistoryMapper.class);
				try {
					ExceptionHistoryMapper mapper = mapperSession.getMapper();
					for (NodeExceptionInfo nodeExceptionInfo : oldList) {
						Node node = nodeExceptionInfo.getNode();
						long time = nodeExceptionInfo.getTime();
						Throwable e = nodeExceptionInfo.getThrowable();
						StringWriter sw = new StringWriter();
						PrintWriter writer = new PrintWriter(sw);
						e.printStackTrace(writer);
						int count = nodeExceptionInfo.getCount();
						
						ExceptionVO vo = new ExceptionVO();
						vo.setNode(node.toString());
						vo.setRegtime(new Timestamp(time));
						if (count > 1) {
							vo.setMessage("[" + count + "] " + e.getMessage());
						} else {
							vo.setMessage(e.getMessage());
						}
						vo.setTrace(sw.toString());
						try {
							mapper.putEntry(vo);
						} catch (Exception e1) {
							logger.error("", e1);
						}
					}
				} finally {
					if (mapperSession != null) {
						mapperSession.closeSession();
					}
				}
				
			}

		}

	}

	class NodeExceptionInfo {
		private int hash;
		private Node node;
		private Throwable e;
//		private StackTraceElement el;
		private long time;
		private AtomicInteger count;
		
		public NodeExceptionInfo(Node node, Throwable e) {
			this.node = node;
			this.e = e;
//			StackTraceElement[] els = e.getStackTrace();
//			if(els != null && els.length > 0){
//				this.el = els[0];
//			}
			count = new AtomicInteger(1);
		}

		public Node getNode() {
			return node;
		}

		public Throwable getThrowable() {
			return e;
		}

		public void setTime() {
			time = System.currentTimeMillis();
		}

		public long getTime() {
			return time;
		}

		public int incrementCount(){
			return count.incrementAndGet();
		}
		public int getCount(){
			return count.intValue();
		}
		
		@Override
		public int hashCode(){
			int h = hash;
			if(h == 0){
				h = node.toString().hashCode() * 31 + e.hashCode();
				hash = h;
			}
			return hash;
		}
		@Override
		public boolean equals(Object obj) {
			if (obj instanceof NodeExceptionInfo) {
				NodeExceptionInfo key = (NodeExceptionInfo) obj;
				String message = this.e.getMessage();
				return this.node.equals(key.node) && (message != null ? message.equals(key.e.getMessage()) : key.e.getMessage() == null);
			}
			return false;
		}

	}

	/**
	 * 해당 노드에서 발생한 에러를 마스터 노드에 알린다.
	 * */
	public void alert(Throwable e) {

		FastcatSearchAlertJob alertJob = new FastcatSearchAlertJob(nodeService.getMyNode(), e);
		// isMaster
		if (isMasterNode) {
			// master면 바로 실행.
			ServiceManager.getInstance().getService(JobService.class).offerSequential(alertJob);
		} else {
			// slave라면 master에게 보낸다.
			nodeService.sendRequestToMaster(alertJob);
		}

	}
}
