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

import org.fastcatsearch.control.JobException;
import org.fastcatsearch.ir.common.IRException;
import org.fastcatsearch.ir.dic.Dic;
import org.fastcatsearch.job.result.IndexingJobResult;
import org.fastcatsearch.service.ServiceException;


public class DictionaryReloadJob extends Job {

	@Override
	public JobResult doRun() throws JobException, ServiceException {
		String[] args = getStringArrayArgs();
		if("synonymDic".equals(args[0])){
			try {
				Dic.reload("synonym");
				return new JobResult(true);
			} catch (IRException e) {
				throw new JobException(e.getMessage(), e);
			}
		}else if("stopDic".equals(args[0])){
			try {
				Dic.reload("stopword");
				return new JobResult(true);
			} catch (IRException e) {
				throw new JobException(e.getMessage(), e);
			}
		}else if("koreanDic".equals(args[0])){
			try {
				Dic.reload("korean");
				return new JobResult(true);
			} catch (IRException e) {
				throw new JobException(e.getMessage(), e);
			}
		}
		
		return new JobResult(-1);
	}

}
