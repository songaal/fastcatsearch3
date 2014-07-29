package org.fastcatsearch.ir;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Calendar;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import org.fastcatsearch.cluster.NodeService;
import org.fastcatsearch.common.io.Streamable;
import org.fastcatsearch.env.Environment;
import org.fastcatsearch.exception.FastcatSearchException;
import org.fastcatsearch.ir.io.DataInput;
import org.fastcatsearch.ir.io.DataOutput;
import org.fastcatsearch.job.MasterNodeJob;
import org.fastcatsearch.service.AbstractService;
import org.fastcatsearch.service.ServiceManager;
import org.fastcatsearch.settings.Settings;

/**
 * 컬렉션별 검색갯수를 모은다. search노드는 1초에 한번 mater로 갯수를 보낸다. master노드는 1초에 한번 갯수를 취합하여 aggregateCountResult에 유지한다.
 * 
 * */
public class CollectionQueryCountService extends AbstractService {

	private RealtimeQueryCountModule aggregateModule;

	private Map<String, Integer> aggregateCountResult;

	private Timer aggregateTimer;
	private Timer reportTimer;

	public CollectionQueryCountService(Environment environment, Settings settings, ServiceManager serviceManager) {
		super(environment, settings, serviceManager);

		if (environment.isMasterNode()) {
			aggregateModule = new RealtimeQueryCountModule(environment, settings);
		}
	}

	/**
	 * 더해진 검색갯수는 1초에 한번씩 clear된다.
	 * */
	public void addQueryCount(String collectionId, int count) {
		if (aggregateModule != null) {
			if (!aggregateModule.incrementQueryCount(collectionId, count)) {
				aggregateModule.registerQueryCount(collectionId);
			}
		} else {
			throw new UnsupportedOperationException();
		}
	}

	public Map<String, Integer> aggregateCountResult() {
		return aggregateCountResult;
	}

	@Override
	protected boolean doStart() throws FastcatSearchException {

		IRService irService = serviceManager.getService(IRService.class);
		Set<String> collectionIdSet = irService.getDataNodeCollectionIdSet();
		boolean isDataNode = collectionIdSet.size() > 0;

		int period = 1000; // 1초.
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.MILLISECOND, 0);
		cal.add(Calendar.SECOND, 1);
		if (environment.isMasterNode()) {
			aggregateModule.load();

			aggregateTimer = new Timer("CollectionQueryCountAggregateTimer", true);
			// 1초단위로 취합하는 timertask를 시작한다.
			aggregateCountResult = new HashMap<String, Integer>();// 처음에 빈 객체.
			aggregateTimer.scheduleAtFixedRate(new AggregateCountTask(), cal.getTime(), period);
		}

		if (isDataNode) {
			logger.info("This is data node for {}", collectionIdSet);
			// 1초 단위로 report하는 timertask를 시작한다.
			reportTimer = new Timer("CollectionQueryCountReportTimer", true);
			reportTimer.scheduleAtFixedRate(new ReportCountTask(), cal.getTime(), period);
		}
		return true;
	}

	@Override
	protected boolean doStop() throws FastcatSearchException {

		if (aggregateTimer != null) {
			aggregateTimer.cancel();
		}

		if (reportTimer != null) {
			reportTimer.cancel();
		}

		if (aggregateModule != null) {
			aggregateModule.unload();
		}

		return true;
	}

	@Override
	protected boolean doClose() throws FastcatSearchException {
		aggregateModule = null;
		aggregateTimer = null;
		reportTimer = null;
		return true;
	}

	private class ReportCountTask extends TimerTask {

		@Override
		public void run() {
			NodeService nodeService = serviceManager.getService(NodeService.class);
			IRService irService = serviceManager.getService(IRService.class);

			RealtimeQueryCountModule module = irService.queryCountModule();
			Set<String> collectionIdSet = irService.getDataNodeCollectionIdSet();

			Map<String, Integer> result = new HashMap<String, Integer>(collectionIdSet.size());
			for (String collectionId : collectionIdSet) {
				int count = module.getQueryCount(collectionId);
				if(count > 0){
					result.put(collectionId, count);
//					logger.debug("##[REPORT] search count {} > {}", collectionId, count);
				}
			}
			//모두 0이면 보내지 않는다.
			if(result.size() > 0){
				ReportQueryCountJob job = new ReportQueryCountJob(result);
				job.setNoResult();
				nodeService.sendRequestToMaster(job);
			}
		}

	}

	private class AggregateCountTask extends TimerTask {

		@Override
		public void run() {
			// logger.debug("RUN AggregateCountTask");
			Set<String> set = aggregateModule.getRegisteredCollectionSet();
			Map<String, Integer> result = new HashMap<String, Integer>();
			for (String collectionId : set) {
				int count = aggregateModule.getQueryCount(collectionId);
				if (count >= 0) {
					result.put(collectionId, count);
					// logger.debug("##[AGGREGATE] search count {} > {}", collectionId, count);
				}
			}
			// set
			aggregateCountResult = result;
		}

	}

	/*
	 * master서버에 검색갯수를 전달한다. masternode에서 실행되야한다.
	 */
	public static class ReportQueryCountJob extends MasterNodeJob implements Streamable {

		private static final long serialVersionUID = -854194838400321409L;

		public ReportQueryCountJob() {
		}

		public ReportQueryCountJob(Map<String, Integer> result) {
			this.args = result;
		}

		@Override
		public JobResult doRun() throws FastcatSearchException {
//			logger.debug("ReportQueryCountJob > {}", args);
			Map<String, Integer> result = (Map<String, Integer>) args;
			CollectionQueryCountService collectionQueryCountService = ServiceManager.getInstance().getService(CollectionQueryCountService.class);
			for (Entry<String, Integer> entry : result.entrySet()) {
				if (entry.getValue() != null && entry.getValue() > 0) {
					if (collectionQueryCountService.isRunning()) {
						collectionQueryCountService.addQueryCount(entry.getKey(), entry.getValue());
					}
					// logger.debug("## add query count {} : {}", entry.getKey(), entry.getValue());
				}
			}

			return new JobResult(true);
		}

		@Override
		public void readFrom(DataInput input) throws IOException {
			int size = input.readVInt();

			Map<String, Integer> result = new HashMap<String, Integer>(size);
			for (int i = 0; i < size; i++) {
				result.put(input.readString(), input.readVInt());
			}
			this.args = result;
		}

		@Override
		public void writeTo(DataOutput output) throws IOException {
			Map<String, Integer> result = (Map<String, Integer>) args;

			output.writeVInt(result.size());

			for (Entry<String, Integer> entry : result.entrySet()) {
				if (entry.getValue() != null) {
					output.writeString(entry.getKey());
					output.writeVInt(entry.getValue().intValue());
				}
			}

		}
	}

}
