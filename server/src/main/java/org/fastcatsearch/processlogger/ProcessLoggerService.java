package org.fastcatsearch.processlogger;

import java.util.HashMap;
import java.util.Map;

import org.fastcatsearch.cluster.NodeService;
import org.fastcatsearch.control.JobService;
import org.fastcatsearch.env.Environment;
import org.fastcatsearch.exception.FastcatSearchException;
import org.fastcatsearch.processlogger.log.ProcessLog;
import org.fastcatsearch.service.AbstractService;
import org.fastcatsearch.service.ServiceManager;
import org.fastcatsearch.settings.Settings;

/**
 * 작업의 시작과 끝에 반복적으로 히스토리기록관련 할일이 있는 경우 여기에 넣어서 기록할수 있도록 해준다.
 * 중복코드를 없애기 위한 서비스.. 차후 없애도 될것같음...  
 * */
public class ProcessLoggerService extends AbstractService {

	private NodeService nodeService;
	private boolean isMasterNode;
	
	private Map<Class<? extends ProcessLogger>, ProcessLogger> processLoggerMap;
	
	public ProcessLoggerService(Environment environment, Settings settings, ServiceManager serviceManager) {
		super(environment, settings, serviceManager);
	}

	@Override
	protected boolean doStart() throws FastcatSearchException {
		processLoggerMap = new HashMap<Class<? extends ProcessLogger>, ProcessLogger>();
		//db를 사용할지 말지 결정.
		registerProcessLogger(new SearchProcessLogger());
		registerProcessLogger(new IndexingProcessLogger());
		
		
		nodeService  = ServiceManager.getInstance().getService(NodeService.class);
		isMasterNode = nodeService.isMaster();
		return true;
	}

	@Override
	protected boolean doStop() throws FastcatSearchException {
		processLoggerMap.clear();
		return true;
	}

	@Override
	protected boolean doClose() throws FastcatSearchException {
		processLoggerMap = null;
		return true;
	}

	private void registerProcessLogger(ProcessLogger processLogger){
		processLoggerMap.put(processLogger.getClass(), processLogger);
	}
	
	/*
	 * ProcessLoggerJob 에서 실제 log를 남기고자할때 logger를 받아서 사용한다.
	 * */
	protected <T> T getProcessLogger(Class<T> processLoggerClass){
		return (T) processLoggerMap.get(processLoggerClass);
	}
	
	/**
	 * 프로세스 로그를 남길 내용을 전달하여 호출한다.
	 * */
	public void log(Class<? extends ProcessLogger> processLoggerClasss, ProcessLog processLog){
		ProcessLoggerJob processLoggerJob = new ProcessLoggerJob(processLoggerClasss, processLog);
		if(isMasterNode){
			//master면 바로 실행.
			ServiceManager.getInstance().getService(JobService.class).offerSequential(processLoggerJob);
		}else{
			//slave라면 master에게 보낸다.
			nodeService.sendRequestToMaster(processLoggerJob);
		}
	}
}
