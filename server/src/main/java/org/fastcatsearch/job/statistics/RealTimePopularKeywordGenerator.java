package org.fastcatsearch.job.statistics;

import java.io.File;
import java.io.FilenameFilter;

import org.fastcatsearch.settings.StaticticsSettings;

public class RealTimePopularKeywordGenerator {
	private File tmpDir;
	private File targetDir;
	private StaticticsSettings staticticsSettings;
	
	public RealTimePopularKeywordGenerator(File tmpDir, File targetDir, StaticticsSettings staticticsSettings) {
		this.tmpDir = tmpDir;
		this.targetDir = targetDir;
		this.staticticsSettings = staticticsSettings;
	}

	public void generate() {
		
		final int MAX_FILE_COUNT = 6; //30분.
		
		
		//2. 기존 파일 롤링. rt/0.log => 1.log.
		File[] fileList = targetDir.listFiles(new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				return false;
			}
		});
		
		//3. 메모리에 담아 키워드순으로 정렬하여 파일기록(rt/0.log)후 tmp디렉토리 삭제.
		
		
		//4. targetDir내 파일 갯수한정삭제. (예 6개:30분)
		
		
		//5. targetDir내 파일 머장 합산(최근갯수에 가중치를 곱한다.*6 *5 *4 ...)하여 tmp.log로 기록 (제외어 파일 사용)
		
		
		//6. 기존 last.log를 last.log.bak으로 이동하고,
		// 갯수로 재정렬(머징정렬이용) 하여 파일 last.log로 기록한다. (상위 300개만 사용) 
		
		
		//7. last.log의 상위 10개를 읽어들여, last.log.bak에서의 순위변동을 계산한다. (last.log.bak을 Map<key,순위>에 띄워서 사용)
		//없으면 new, 있으면 +/- 순위변동값.
		
		
		//8. 순위변동이 계산된 메모리의 키워드리스트를 db에 insert한다. 
	}

}
