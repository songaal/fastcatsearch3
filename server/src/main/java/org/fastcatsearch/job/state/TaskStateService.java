package org.fastcatsearch.job.state;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.fastcatsearch.env.Environment;
import org.fastcatsearch.exception.FastcatSearchException;
import org.fastcatsearch.ir.common.IndexingType;
import org.fastcatsearch.service.AbstractService;
import org.fastcatsearch.service.ServiceManager;
import org.fastcatsearch.settings.Settings;

public class TaskStateService extends AbstractService {

	Map<TaskKey, TaskState> map;

	public TaskStateService(Environment environment, Settings settings, ServiceManager serviceManager) {
		super(environment, settings, serviceManager);
	}

	@Override
	protected boolean doStart() throws FastcatSearchException {
		map = new ConcurrentHashMap<TaskKey, TaskState>();
		return true;
	}

	@Override
	protected boolean doStop() throws FastcatSearchException {
		map.clear();
		return true;
	}

	@Override
	protected boolean doClose() throws FastcatSearchException {
		map.clear();
		return true;
	}

	public TaskState register(TaskKey key) {
		if (!map.containsKey(key)) {
			TaskState taskState = key.createState(this);
			map.put(key, taskState);
			return taskState;
		}else{
			return map.get(key);
		}
	}

	public void remove(TaskKey key) {
		map.remove(key);
	}

	public TaskState get(TaskKey key) {
		return map.get(key);
	}
	//clazz 에 해당하는 task 들을 넘겨준다.
	public List<TaskState> getTaskStateList(Class<? extends TaskState> clazz) {
		List<TaskState> list = null;
		Iterator<TaskState> iterator = map.values().iterator();
		while(iterator.hasNext()){
			TaskState taskState = iterator.next();
			if(clazz != null){
				if(clazz.isInstance(taskState)){
					if(list == null){
						list = new ArrayList<TaskState>();
					}
					list.add(taskState);
				}
			}else{
				if(list == null){
					list = new ArrayList<TaskState>();
				}
				list.add(taskState);
			}
		}
		return list;
	}

}
