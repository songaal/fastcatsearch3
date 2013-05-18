/*
 * Copyright (c) 2013 Websquared, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 * 
 * Contributors:
 *     swsong - initial API and implementation
 */

package org.fastcatsearch.job;

import java.io.File;
import java.util.List;


import org.fastcatsearch.db.DBService;
import org.fastcatsearch.db.dao.SetDictionary;
import org.fastcatsearch.db.vo.SetDictionaryVO;
import org.fastcatsearch.ir.common.IRException;
import org.fastcatsearch.ir.config.IRConfig;
import org.fastcatsearch.ir.dic.HashSetDictionary;
import org.fastcatsearch.ir.io.CharVector;
import org.fastcatsearch.exception.FastcatSearchException;
import org.fastcatsearch.settings.IRSettings;


public class HashSetDictionaryCompileApplyJob extends Job {

	private static int BUCKET_SIZE = 16 * 1024;
	
	@Override
	public JobResult doRun() throws FastcatSearchException {
		String[] args = getStringArrayArgs();
		if("stopDic".equals(args[0])){
			try {
				compileStopDic();
//				return new JobResult(Dic.reload("stopword"));
			} catch (IRException e) {
				throw new FastcatSearchException(e.getMessage(), e);
			}
			
		}else if("koreanDic".equals(args[0])){
			try {
				compileKoreanDic();
//				return new JobResult(Dic.reload("korean"));
			} catch (IRException e) {
				throw new FastcatSearchException(e.getMessage(), e);
			}
			
		}else if("userDic".equals(args[0])){
			try {
				compileUserDic();
//				return new JobResult(Dic.reload("userword"));
			} catch (IRException e) {
				throw new FastcatSearchException(e.getMessage(), e);
			}
			
		}else{
			logger.error("Unknown dictionary = "+args[0]);
		}
		
		return new JobResult(-1);
	}
	
	public int compileStopDic() throws IRException{
		IRConfig irConfig = IRSettings.getConfig();
		String dicPath = IRSettings.path(irConfig.getString("stopword.dic.path"));
		
		HashSetDictionary dic = new HashSetDictionary(BUCKET_SIZE);
		
		DBService dbHandler = DBService.getInstance();
		int BULK_SIZE = 10000;
		
		/*
		 * bulk basic dic
		 * */
		SetDictionary bannedDictionary = dbHandler.getDAO("BannedDictionary");
		int count = bannedDictionary.selectCount();
		
		for(int startRow = 0; startRow < count; startRow += BULK_SIZE){
			List<SetDictionaryVO> list = bannedDictionary.selectPage(startRow, BULK_SIZE);
			
			//bulk insert
			for (int i = 0; i < list.size(); i++) {
				SetDictionaryVO banned = list.get(i);
				
				dic.put(new CharVector(banned.keyword).toUpperCase());
			}
		}
		
		dic.save(new File(dicPath));
		
		return count;
	}
	
	
	public int compileKoreanDic() throws IRException{
		IRConfig irConfig = IRSettings.getConfig();
		String dicPath = IRSettings.path(irConfig.getString("korean.dic.path"));
		
		HashSetDictionary dic = new HashSetDictionary(BUCKET_SIZE);
		
		DBService dbHandler = DBService.getInstance();
		int BULK_SIZE = 10000;
		
		/*
		 * bulk basic dic
		 * */
		SetDictionary basicDictionary = dbHandler.getDAO("BasicDictionary");
		int count = basicDictionary.selectCount();
		
		for(int startRow = 0; startRow < count; startRow += BULK_SIZE){
			List<SetDictionaryVO> list = basicDictionary.selectPage(startRow, BULK_SIZE);
			
			//bulk insert
			for (int i = 0; i < list.size(); i++) {
				SetDictionaryVO basic = list.get(i);				
				dic.put(new CharVector(basic.keyword));
			}
		}
		
		dic.save(new File(dicPath));
		
		return count;
	}
	
	public int compileUserDic() throws IRException{
		IRConfig irConfig = IRSettings.getConfig();
		String dicPath = irConfig.getString("user.dic.path");
		if(dicPath == null)
			throw new IRException("user.dic.path setting not exist!");
		
		HashSetDictionary dic = new HashSetDictionary(BUCKET_SIZE);
		
		DBService dbHandler = DBService.getInstance();
		int BULK_SIZE = 10000;
		
		/*
		 * bulk custom dic
		 * */
		SetDictionary userDictionary = dbHandler.getDAO("UserDictionary");
		int count = userDictionary.selectCount();
		
		for(int startRow = 0; startRow < count; startRow += BULK_SIZE){
			List<SetDictionaryVO> list = userDictionary.selectPage(startRow, BULK_SIZE);
			
			//bulk insert
			for (int i = 0; i < list.size(); i++) {
				SetDictionaryVO basic = list.get(i);
				
				dic.put(new CharVector(basic.keyword));
			}
		}
		
		dic.save(new File(IRSettings.path(dicPath)));
		
		return count;
	}

}
