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

import java.io.IOException;

import org.fastcatsearch.common.io.Streamable;
import org.fastcatsearch.ir.io.DataInput;
import org.fastcatsearch.ir.io.DataOutput;
import org.fastcatsearch.transport.vo.StreamableString;


public class TestJob extends Job implements Streamable {

	public TestJob(){ }
	
	public TestJob(String str){
		args = new String[]{str};
	}
	
	@Override
	public JobResult doRun() {
		String[] args = getStringArrayArgs();
		StreamableString str = new StreamableString(args[0]);

		logger.debug("This is Test Job!! args="+str.value());
		
		return new JobResult(str);
	}

	@Override
	public void readFrom(DataInput input) throws IOException {
		String arg = input.readString();
		logger.debug("read arg >> {}", arg);
		args = new String[]{arg};
	}

	@Override
	public void writeTo(DataOutput output) throws IOException {
		output.writeString(getStringArrayArgs()[0]);
	}

}
