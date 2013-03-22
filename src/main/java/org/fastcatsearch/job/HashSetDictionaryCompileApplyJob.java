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

import org.fastcatsearch.control.JobException;
import org.fastcatsearch.db.DBService;
import org.fastcatsearch.db.object.BannedDictionary;
import org.fastcatsearch.db.object.BasicDictionary;
import org.fastcatsearch.db.object.CustomDictionary;
import org.fastcatsearch.db.object.SetDictionaryDAO;
import org.fastcatsearch.ir.common.IRException;
import org.fastcatsearch.ir.config.IRConfig;
import org.fastcatsearch.ir.config.IRSettings;
import org.fastcatsearch.ir.dic.Dic;
import org.fastcatsearch.ir.dic.HashSetDictionary;
import org.fastcatsearch.ir.io.CharVector;
import org.fastcatsearch.service.ServiceException;


public class HashSetDictionaryCompileApplyJob extends Job {

	private static int BUCKET_SIZE = 16 * 1024;
	
	@Override
	public JobResult run0() throws JobException, ServiceException {
		String[] args = getStringArrayArgs();
		if("stopDic".equals(args[0])){
			try {
				compileStopDic();
				return new JobResult(Dic.reload("stopword"));
			} catch (IRException e) {
				throw new JobException(e.getMessage(), e);
			}
			
		}else if("koreanDic".equals(args[0])){
			try {
				compileKoreanDic();
				return new JobResult(Dic.reload("korean"));
			} catch (IRException e) {
				throw new JobException(e.getMessage(), e);
			}
			
		}else if("userDic".equals(args[0])){
			try {
				compileUserDic();
				return new JobResult(Dic.reload("userword"));
			} catch (IRException e) {
				throw new JobException(e.getMessage(), e);
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
		int count = dbHandler.BannedDictionary.selectCount();
		
		for(int startRow = 0; startRow < count; startRow += BULK_SIZE){
			List<SetDictionaryDAO> list = dbHandler.BannedDictionary.select(startRow, BULK_SIZE);
			
			//bulk insert
			for (int i = 0; i < list.size(); i++) {
				SetDictionaryDAO banned =(SetDictionaryDAO) list.get(i);
				
				dic.put(new CharVector(banned.term).toUpperCase());
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
		int count = dbHandler.BasicDictionary.selectCount();
		
		for(int startRow = 0; startRow < count; startRow += BULK_SIZE){
			List<SetDictionaryDAO> list = dbHandler.BasicDictionary.select(startRow, BULK_SIZE);
			
			//bulk insert
			for (int i = 0; i < list.size(); i++) {
				SetDictionaryDAO basic = list.get(i);				
				dic.put(new CharVector(basic.term));
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
		int count = dbHandler.CustomDictionary.selectCount();
		
		for(int startRow = 0; startRow < count; startRow += BULK_SIZE){
			List<SetDictionaryDAO> list = dbHandler.CustomDictionary.select(startRow, BULK_SIZE);
			
			//bulk insert
			for (int i = 0; i < list.size(); i++) {
				SetDictionaryDAO basic = (SetDictionaryDAO)list.get(i);
				
				dic.put(new CharVector(basic.term));
			}
		}
		
		dic.save(new File(IRSettings.path(dicPath)));
		
		return count;
	}

}
