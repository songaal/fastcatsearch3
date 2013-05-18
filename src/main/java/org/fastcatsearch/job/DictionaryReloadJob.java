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


import org.fastcatsearch.ir.common.IRException;
import org.fastcatsearch.exception.FastcatSearchException;


public class DictionaryReloadJob extends Job {

	@Override
	public JobResult doRun() throws FastcatSearchException {
//		String[] args = getStringArrayArgs();
//		if("synonymDic".equals(args[0])){
//			try {
//				Dic.reload("synonym");
//				return new JobResult(true);
//			} catch (IRException e) {
//				throw new FastcatSearchException(e.getMessage(), e);
//			}
//		}else if("stopDic".equals(args[0])){
//			try {
//				Dic.reload("stopword");
//				return new JobResult(true);
//			} catch (IRException e) {
//				throw new FastcatSearchException(e.getMessage(), e);
//			}
//		}else if("koreanDic".equals(args[0])){
//			try {
//				Dic.reload("korean");
//				return new JobResult(true);
//			} catch (IRException e) {
//				throw new FastcatSearchException(e.getMessage(), e);
//			}
//		}
		
		return new JobResult(-1);
	}

}
