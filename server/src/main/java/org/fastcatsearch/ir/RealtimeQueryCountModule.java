package org.fastcatsearch.ir;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import org.fastcatsearch.env.Environment;
import org.fastcatsearch.module.AbstractModule;
import org.fastcatsearch.module.ModuleException;
import org.fastcatsearch.settings.Settings;
import org.fastcatsearch.util.AsynchronousCounter;

public class RealtimeQueryCountModule extends AbstractModule {

	private static final long sinkPeriod = 1000L; //통계를 0으로 만드는 주기.
	private Timer timer;
	private Map<String, AsynchronousCounter> statisticsMap;
	
	//정해진 주기마다 통계를 0으로 만든다.
	//[0]에는 수치를 계속 더해가고, 주기가 되면 완료된 수치를 [1]로 옮기고 [0]을 0으로 만든다. 
	class StatisticsSinkTask extends TimerTask {
		@Override
		public void run() {
			for(AsynchronousCounter counter : statisticsMap.values()){
				//[1]에는 수치가 조금 늦게 들어가도 되지만, [0]은 0으로 즉시 초기화 해야 수치가 정확하다.
				int c = counter.resetCount();
//				logger.debug("@StatisticsSinkTask sink {}", c);
			}
		}
	}
	
	public RealtimeQueryCountModule(Environment environment, Settings settings) {
		super(environment, settings);
	}

	
	
	@Override
	protected boolean doLoad() throws ModuleException {
		statisticsMap = new HashMap<String, AsynchronousCounter>();
		timer = new Timer();
		timer.schedule(new StatisticsSinkTask(), sinkPeriod, sinkPeriod);
		return true;
	}

	@Override
	protected boolean doUnload() throws ModuleException {
		timer.cancel();
		timer = null;
		return true;
	}

	public void registerQueryCount(String collectionId){
		synchronized (statisticsMap) {
			statisticsMap.put(collectionId, new AsynchronousCounter());
		}
	}
	public boolean incrementQueryCount(String collectionId){
		AsynchronousCounter counter = statisticsMap.get(collectionId);
		if(counter != null){
			counter.incrementCount();
			return true;
		}else{
			return false;
		}
	}
	
	public boolean incrementQueryCount(String collectionId, int count){
		AsynchronousCounter counter = statisticsMap.get(collectionId);
		if(counter != null){
			counter.addCount(count);
			return true;
		}else{
			return false;
		}
	}
	
	public AsynchronousCounter getQueryCounter(String collectionId){
		return statisticsMap.get(collectionId);
	}
	
	public int getQueryCount(String collectionId){
		AsynchronousCounter counter = statisticsMap.get(collectionId);
		if(counter == null){
			return -1;
		}else{
			return counter.getLastCount();
		}
	}
	
	public void removeQueryCount(String collectionId){
		synchronized (statisticsMap) {
			statisticsMap.remove(collectionId);
		}
	}
	
	public Set<Map.Entry<String, AsynchronousCounter>> statisticsEntrySet(){
		return statisticsMap.entrySet();
	}
	
	public Set<String> getRegisteredCollectionSet(){
		return statisticsMap.keySet();
	}
}
