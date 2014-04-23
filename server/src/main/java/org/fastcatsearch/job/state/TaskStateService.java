package org.fastcatsearch.job.state;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Queue;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.fastcatsearch.cluster.NodeService;
import org.fastcatsearch.env.Environment;
import org.fastcatsearch.exception.FastcatSearchException;
import org.fastcatsearch.job.cluster.UpdateNodeTaskStateJob;
import org.fastcatsearch.service.AbstractService;
import org.fastcatsearch.service.ServiceManager;
import org.fastcatsearch.settings.Settings;

public class TaskStateService extends AbstractService {

	private NodeService nodeService;
	private boolean isMasterNode;
	private Map<TaskKey, TaskState> map;
	private Queue<TaskKey> removeTaskQueue; // 종료된 task를 master에 알릴때 사용된다.

	private Timer reportTimer; // master노드로 현 task를 주기적으로 보낸다.

	public TaskStateService(Environment environment, Settings settings, ServiceManager serviceManager) {
		super(environment, settings, serviceManager);
	}

	@Override
	protected boolean doStart() throws FastcatSearchException {
		map = new ConcurrentHashMap<TaskKey, TaskState>();

		nodeService = serviceManager.getService(NodeService.class);
		isMasterNode = nodeService.isMaster();

		// master가 아니면 현 task 상태를 주기적으로 master에 보낸다.
		if (!isMasterNode) {
			removeTaskQueue = new ConcurrentLinkedQueue<TaskKey>();
			reportTimer = new Timer("TaskStateReportTimer", true);
			reportTimer.schedule(new TaskStateReportTask(), 1000, 1000);
		}
		return true;
	}

	@Override
	protected boolean doStop() throws FastcatSearchException {
		if (!isMasterNode && map.size() > 0) {
			// master에 현 노드의 모든 task를 remove하도록한다.
			UpdateNodeTaskStateJob reportJob = new UpdateNodeTaskStateJob();
			reportJob.setRemoveTaskKeyList(map.keySet().toArray(new TaskKey[0]));
			nodeService.sendRequestToMaster(reportJob);
		}
		if (reportTimer != null) {
			reportTimer.cancel();
		}
		map.clear();
		return true;
	}

	@Override
	protected boolean doClose() throws FastcatSearchException {
		if (map != null) {
			map.clear();
		}
		reportTimer = null;
		return true;
	}

	public TaskState register(TaskKey key, boolean isScheduled) {
		TaskState prevTaskState = map.get(key);
		if(prevTaskState != null) {
			//이전 상태가 남아있을때, 종료 상태가 아니면 동일한 작업을 시작하지 못한다.
			if(!prevTaskState.isFinished()){
				return null;
			}
		}
		
		TaskState taskState = key.createState(isScheduled);
		map.put(key, taskState);
		return taskState;
	}

	public void remove(TaskKey key) {
		map.remove(key);
		if (removeTaskQueue != null) {
			removeTaskQueue.add(key);
		}
	}

	public TaskState get(TaskKey key) {
		return map.get(key);
	}

	public void putAllTasks(Map<TaskKey, TaskState> taskMap) {
		map.putAll(taskMap);
	}

	// clazz 에 해당하는 task 들을 넘겨준다.
	public List<Entry<TaskKey, TaskState>> getTaskEntryList(Class<? extends TaskState> clazz) {
		List<Entry<TaskKey, TaskState>> list = null;
		for (Entry<TaskKey, TaskState> entry : map.entrySet()) {
			if (clazz != null) {
				if (clazz.isInstance(entry.getValue())) {
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
		return list;
	}

	class TaskStateReportTask extends TimerTask {

		@Override
		public void run() {

			if (map.size() > 0 || removeTaskQueue.size() > 0) {
				UpdateNodeTaskStateJob reportJob = new UpdateNodeTaskStateJob();
				if (map.size() > 0) {
					reportJob.setRunningTaskMap(map);
				}

				if (removeTaskQueue.size() > 0) {
					TaskKey[] removeTaskKeyList = removeTaskQueue.toArray(new TaskKey[0]);
					removeTaskQueue.clear();
					reportJob.setRemoveTaskKeyList(removeTaskKeyList);
				}

				nodeService.sendRequestToMaster(reportJob);
			}
		}

	}

}
