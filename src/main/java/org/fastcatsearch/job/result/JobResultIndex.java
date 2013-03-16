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

package org.fastcatsearch.job.result;

public class JobResultIndex {
	public String collection;
	public int docSize;
	public int updateSize;
	public int deleteSize;
	public int duration;
	
	public JobResultIndex(String collection, int docSize, int updateSize, int deleteSize, int duration) {
		this.collection = collection;
		this.docSize = docSize;
		this.updateSize = updateSize;
		this.deleteSize = deleteSize;
		this.duration = duration;
		
	}

	public String toString(){
		return "[IndexJobResult] collection = "+collection+", docSize = "+docSize+", updateSize = "+updateSize+", deleteSize = "+deleteSize+", duration = "+duration;
	}
}
