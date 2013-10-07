package org.fastcatsearch.ir;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicInteger;

import org.fastcatsearch.env.Environment;
import org.fastcatsearch.module.AbstractModule;
import org.fastcatsearch.module.ModuleException;
import org.fastcatsearch.settings.Settings;

public class RealtimeQueryStatisticsModule extends AbstractModule {

	private static final long sinkPeriod = 1000L; //통계를 0으로 만드는 주기.
	private Timer timer;
	private Map<String, AtomicInteger[]> statisticsMap;
	
	//정해진 주기마다 통계를 0으로 만든다.
	//[0]에는 수치를 계속 더해가고, 주기가 되면 완료된 수치를 [1]로 옮기고 [0]을 0으로 만든다. 
	class StatisticsSinkTask extends TimerTask {
		@Override
		public void run() {
			for(AtomicInteger[] integerPair : statisticsMap.values()){
				//[1]에는 수치가 조금 늦게 들어가도 되지만, [0]은 0으로 즉시 초기화 해야 수치가 정확하다.
				integerPair[1].lazySet(integerPair[0].get());
				integerPair[0].set(0);
			}
		}
	}
	
	public RealtimeQueryStatisticsModule(Environment environment, Settings settings) {
		super(environment, settings);
	}

	
	
	@Override
	protected boolean doLoad() throws ModuleException {
		statisticsMap = new HashMap<String, AtomicInteger[]>();
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
		statisticsMap.put(collectionId, new AtomicInteger[]{new AtomicInteger(), new AtomicInteger()});
	}
	public void incrementQueryCount(String collectionId){
		AtomicInteger[] integerPair = statisticsMap.get(collectionId);
		if(integerPair != null){
			integerPair[0].incrementAndGet();
		}
	}
	
	public int getQueryCount(String collectionId){
		AtomicInteger[] integerPair = statisticsMap.get(collectionId);
		if(integerPair == null){
			return 0;
		}else{
			return integerPair[1].get();
		}
	}
	
	public void removeQueryCount(String collectionId){
		statisticsMap.remove(collectionId);
	}
	
	public Set<Map.Entry<String,AtomicInteger[]>> statisticsEntrySet(){
		return statisticsMap.entrySet();
	}
}
