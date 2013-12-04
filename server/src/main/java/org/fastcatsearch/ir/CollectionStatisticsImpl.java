package org.fastcatsearch.ir;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import org.fastcatsearch.ir.query.Metadata;
import org.fastcatsearch.ir.query.Query;
import org.fastcatsearch.ir.search.CollectionStatistics;

/**
 * 1. 검색수치는 메모리에 이전 1분 통계 한개를 유지하며, 1분마다 master 서버에서 가져간다. 
 * 
 * 2. 검색키워드기록은 buffered file writer.알아서 스위치.
 * 2. 검색키워드는 서버마다 하루 단위로 파일로 가지고 있는다. 파일은 [컬렉션명]/statistics/log/2013/12/08.log 와 같이 월단위 디렉토리로 유지한다.
 * 	  mater에서 자정에 요청시 하루단위의 파일을 master에서 가져간다.
 *    주,월,년 별 통계는 master에서 해당날짜에 일단위 DB통계를 이용하여 계산하도록 한다. (해당날짜에 서버다운시 차후 계산하도록.. 계산여부상태를 DB에 기록하여 다음 통계시 확인.바로 이전 상태여부만 확인하면 됨.).
 *    DB는 컬렉션별, 날짜타입별로 table을 따로 생성. 예를 들어 주별통계는 stat_[collectionId]_week 과 같이 된다.
 * 3. 실시간 인기검색어를 위해 5분단위로 파일에 쌓아두며, .success.1 .success.0 .fail.1 .fail.0 으로 번갈아가며 유지한다. 5분마다 master에서 가져간다.
 * */
public class CollectionStatisticsImpl implements CollectionStatistics {

	private Timer timer;
	private List<String> keywordList;
	
	@Override
	public void start() {
		if(timer != null){
			timer.cancel();
		}
		
		keywordList = new ArrayList<String>();
		
		timer = new Timer();
		timer.schedule(new StatisticsManageScheduler(), 60 * 1000L); //1분.
	}

	@Override
	public void stop() {
		timer.cancel();
	}

	@Override
	public void close() {
		timer.cancel();
		timer = null;
		
	}

	@Override
	public void add(Query q) {
	
		if(q != null){
			
			//1. 검색수치 1증가.
			
			
			//2. searchKeyword 추가.
			String searchKeyword = q.getMeta().getUserData(Metadata.UD_KEYWORD);
			keywordList.add(searchKeyword);
			
		}
	}
	
	
	/*
	 * 1분마다 돌면서 검색데이터를 통계내서 가지고 있도록 한다.
	 * */
	private class StatisticsManageScheduler extends TimerTask {
		
		@Override
		public void run() {
			
			//이전 통계 데이터 삭제.
			if(keywordList.size() > 0){
				
				//1. switch slot.
				List<String> oldList = keywordList;
				keywordList = new ArrayList<String>();
				
				
				//2. 정렬.
				
				//3. 
			}
			
		}
		
	}

}
