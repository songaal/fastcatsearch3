package org.fastcatsearch.job.state;

import org.fastcatsearch.cluster.NodeService;
import org.fastcatsearch.env.Environment;
import org.fastcatsearch.exception.FastcatSearchException;
import org.fastcatsearch.job.cluster.ClearNodeTaskStateJob;
import org.fastcatsearch.job.cluster.UpdateNodeTaskStateJob;
import org.fastcatsearch.service.AbstractService;
import org.fastcatsearch.service.ServiceManager;
import org.fastcatsearch.settings.Settings;

import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

public class TaskStateService extends AbstractService {

	private NodeService nodeService;
	private boolean isMasterNode;
	private Map<String, Map<TaskKey, TaskState>> nodeTaskMap;

	private Timer stateTimer; // master노드로 현 task를 주기적으로 보낸다.

	public TaskStateService(Environment environment, Settings settings, ServiceManager serviceManager) {
		super(environment, settings, serviceManager);
	}

	@Override
	protected boolean doStart() throws FastcatSearchException {
		nodeTaskMap = new ConcurrentHashMap<String, Map<TaskKey, TaskState>>();
		nodeTaskMap.put(environment.myNodeId(), newTaskMap());
		
		nodeService = serviceManager.getService(NodeService.class);
		isMasterNode = nodeService.isMaster();

		// master가 아니면 현 task 상태를 주기적으로 master에 보낸다.
		stateTimer = new Timer("TaskStateTimer", true);
		if (!isMasterNode) {
			stateTimer.schedule(new TaskStateReportTask(), 5000, 5000);
		} else {
			stateTimer.schedule(new OldTaskStateClearTask(), 60*1000, (10 * 60000));
		}
		return true;
	}

	@Override
	protected boolean doStop() throws FastcatSearchException {
		if (!isMasterNode) {
			// master에 현 노드의 모든 task를 remove하도록한다.
			ClearNodeTaskStateJob clearJob = new ClearNodeTaskStateJob();
			nodeService.sendRequestToMaster(clearJob);
		}
		
		if (stateTimer != null) {
			stateTimer.cancel();
		}
		nodeTaskMap.clear();
		return true;
	}

	@Override
	protected boolean doClose() throws FastcatSearchException {
		nodeTaskMap = null;
		stateTimer = null;
		return true;
	}

	public void register(TaskKey key, TaskState taskState) {
		Map<TaskKey, TaskState> map = getMyNodeTaskMap();
		TaskState prevTaskState = map.get(key);
		if(prevTaskState != null) {
			//이전 상태가 남아있을때, 종료 상태가 아니면 동일한 작업을 시작하지 못한다.
            logger.debug("Task previous {} > {}", key, prevTaskState);
			if(!prevTaskState.isFinished()){
				return;
			}
		}
		logger.debug("Task registered {} > {}", key, taskState);
//		TaskState taskState = key.createState(isScheduled);
		map.put(key, taskState);
	}

	public void clearTaskMap(String nodeId) {
        if(nodeTaskMap != null) {
            nodeTaskMap.remove(nodeId);
        }
	}
	
	public TaskState getTaskState(TaskKey key) {
		Iterator<Map.Entry<String,Map<TaskKey,TaskState>>> iterator = nodeTaskMap.entrySet().iterator();
		while(iterator.hasNext()) {
			Entry<String,Map<TaskKey,TaskState>> entry = iterator.next();
			TaskState taskState =  entry.getValue().get(key);
			if(taskState != null) {
				return taskState;
			}
		}
		return null;
	}

	//각 노드에서 전달된 변경사항들이 master의 상태 map에 머징된다.
	public void putAllTasks(String nodeId, Map<TaskKey, TaskState> taskMap) {
		Map<TaskKey, TaskState> map = nodeTaskMap.get(nodeId);
		if(map == null) {
			map = newTaskMap();
			nodeTaskMap.put(nodeId, map);
		}
		map.putAll(taskMap);
	}
	
	private Map<TaskKey, TaskState> newTaskMap() {
		return new ConcurrentHashMap<TaskKey, TaskState>();
	}

	private Map<TaskKey, TaskState> getMyNodeTaskMap() {
		return nodeTaskMap.get(environment.myNodeId());
	}
	
	public Map<TaskKey, TaskState> getNodeTaskMap(String nodeId) {
		return nodeTaskMap.get(nodeId);
	}
	
	// clazz 에 해당하는 task 들을 넘겨준다.
	public List<Entry<TaskKey, TaskState>> getTaskEntryList(Class<? extends TaskKey> clazz) {
		List<Entry<TaskKey, TaskState>> list = null;
		
		Iterator<Map<TaskKey, TaskState>> iterator = nodeTaskMap.values().iterator();
		while(iterator.hasNext()){
			Map<TaskKey, TaskState> taskMap = iterator.next();
			for (Entry<TaskKey, TaskState> entry : taskMap.entrySet()) {
				if (clazz != null) {
					if (clazz.isInstance(entry.getKey())) {
						if (list == null) {
							list = new ArrayList<Entry<TaskKey, TaskState>>();
						}
						list.add(entry);
					}
				} else {
					if (list == null) {
						list = new ArrayList<Entry<TaskKey, TaskState>>();
					}
					list.add(entry);
				}
			}
		}
		return list;
	}

	class TaskStateReportTask extends TimerTask {

		@Override
		public void run() {
			//my node의 task를 master node 로 보낸다.
			Map<TaskKey, TaskState> taskMap = getMyNodeTaskMap();
			if (taskMap.size() > 0) {
				UpdateNodeTaskStateJob reportJob = new UpdateNodeTaskStateJob(environment.myNodeId());
				reportJob.setRunningTaskMap(taskMap);
				nodeService.sendRequestToMaster(reportJob);
				
				//finished 된 task는 목록에서 비운다.
				//다음부터는 running상태의 task만 전달된다.
				Iterator<Entry<TaskKey, TaskState>> iterator = taskMap.entrySet().iterator();
				while(iterator.hasNext()) {
					if(iterator.next().getValue().isFinished()){
						iterator.remove();
					}
				}
			}
			
			
		}

	}

	class OldTaskStateClearTask extends TimerTask {

		@Override
		public void run() {
			// 상태 finished, 1시간 지나면 삭제
			Iterator<Entry<String, Map<TaskKey, TaskState>>> iterator = nodeTaskMap.entrySet().iterator();
			while(iterator.hasNext()) {
				Iterator<Entry<TaskKey, TaskState>> taskState = iterator.next().getValue().entrySet().iterator();
				while (taskState.hasNext()) {
					if (taskState.next().getValue().isOldTask()) {
						taskState.remove();
					}
				}
			}
		}

	}

}
