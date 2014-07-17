package org.fastcatsearch.notification;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.fastcatsearch.alert.ClusterAlertService;
import org.fastcatsearch.cluster.NodeService;
import org.fastcatsearch.control.JobService;
import org.fastcatsearch.db.DBService;
import org.fastcatsearch.db.InternalDBModule.MapperSession;
import org.fastcatsearch.db.mapper.NotificationConfigMapper;
import org.fastcatsearch.db.vo.NotificationConfigVO;
import org.fastcatsearch.env.Environment;
import org.fastcatsearch.exception.FastcatSearchException;
import org.fastcatsearch.notification.message.Notification;
import org.fastcatsearch.service.AbstractService;
import org.fastcatsearch.service.ServiceManager;
import org.fastcatsearch.settings.Settings;

public class NotificationService extends AbstractService {

	private NodeService nodeService;
	private boolean isMasterNode;
	private Map<String, NotificationConfigVO> notificationMap;

	public NotificationService(Environment environment, Settings settings, ServiceManager serviceManager) {
		super(environment, settings, serviceManager);

	}

	@Override
	protected boolean doStart() throws FastcatSearchException {
		notificationMap = new HashMap<String, NotificationConfigVO>();
		nodeService = serviceManager.getService(NodeService.class);
		isMasterNode = nodeService.isMaster();

		if (isMasterNode) {
			// notification config를 메모리에 유지한다.
			MapperSession<NotificationConfigMapper> mapperSession = DBService.getInstance().getMapperSession(NotificationConfigMapper.class);
			try {
				NotificationConfigMapper mapper = mapperSession.getMapper();
				List<NotificationConfigVO> configList = mapper.getEntryList();
				
				logger.debug("@@configList >> {}", configList);
				for (NotificationConfigVO config : configList) {
					notificationMap.put(config.getCode(), config);
					logger.debug("### NotificationConfig > {} : {}", config.getCode(), config.toString());
				}
			} catch (Exception e) {
				logger.error("", e);
				ClusterAlertService.getInstance().alert(e);
			} finally {
				if (mapperSession != null) {
					mapperSession.closeSession();
				}
			}
		}

		return true;
	}

	@Override
	protected boolean doStop() throws FastcatSearchException {
		notificationMap.clear();
		return true;
	}

	@Override
	protected boolean doClose() throws FastcatSearchException {
		notificationMap = null;
		return true;
	}

	/**
	 * 해당 notification의 통지설정에 따라서 관리자에게 통지서비스를 하고, db에 기록한다.
	 * */
	public void sendNotification(Notification notification) {

		NotificationJob notificationJob = new NotificationJob(notification);

		if (isMasterNode) {
			// master면 바로 실행.
			ServiceManager.getInstance().getService(JobService.class).offerSequential(notificationJob);
		} else {
			// slave라면 master에게 보낸다.
			nodeService.sendRequestToMaster(notificationJob);
		}
	}

	public void updateNotificationConfig(NotificationConfigVO vo) {
		notificationMap.put(vo.getCode(), vo);
		MapperSession<NotificationConfigMapper> mapperSession = DBService.getInstance().getMapperSession(NotificationConfigMapper.class);
		try {
			NotificationConfigMapper mapper = mapperSession.getMapper();
			mapper.updateEntry(vo);
		} catch (Exception e) {
			logger.error("", e);
		} finally {
			if (mapperSession != null) {
				mapperSession.closeSession();
			}
		}
	}

	public void putNotificationConfig(NotificationConfigVO vo) {
		notificationMap.put(vo.getCode(), vo);
		MapperSession<NotificationConfigMapper> mapperSession = DBService.getInstance().getMapperSession(NotificationConfigMapper.class);
		try {
			NotificationConfigMapper mapper = mapperSession.getMapper();
			mapper.putEntry(vo);
		} catch (Exception e) {
			logger.error("", e);
		} finally {
			if (mapperSession != null) {
				mapperSession.closeSession();
			}
		}
	}

	public void deleteNotificationConfig(String code) {
		notificationMap.remove(code);
		MapperSession<NotificationConfigMapper> mapperSession = DBService.getInstance().getMapperSession(NotificationConfigMapper.class);
		try {
			NotificationConfigMapper mapper = mapperSession.getMapper();
			mapper.deleteEntry(code);
		} catch (Exception e) {
			logger.error("", e);
		} finally {
			if (mapperSession != null) {
				mapperSession.closeSession();
			}
		}

	}

	public Map<String, NotificationConfigVO> getNotificationConfigMap() {
		return notificationMap;
	}

}
