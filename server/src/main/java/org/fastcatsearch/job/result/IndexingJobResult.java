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

import java.io.IOException;

import org.fastcatsearch.common.io.Streamable;
import org.fastcatsearch.ir.config.CollectionIndexStatus.IndexStatus;
import org.fastcatsearch.ir.io.DataInput;
import org.fastcatsearch.ir.io.DataOutput;

public class IndexingJobResult implements Streamable {
	public String collectionId;
	public IndexStatus indexStatus;
	public int duration;
	public boolean isSuccess;

	public IndexingJobResult() {
	}

	public IndexingJobResult(String collectionId, IndexStatus indexStatus, int duration) {
		this(collectionId, indexStatus, duration, true);
	}

	public IndexingJobResult(String collectionId, IndexStatus indexStatus, int duration, boolean isSuccess) {
		this.collectionId = collectionId;
		this.indexStatus = indexStatus;
		this.duration = duration;
		this.isSuccess = isSuccess;
	}

	public String toString() {
		return "[IndexingResult] success[" + isSuccess + "] collectionId = " + collectionId + ", indexStatus = " + indexStatus + " duration = "
				+ duration;
	}

	@Override
	public void readFrom(DataInput input) throws IOException {
		collectionId = input.readString();
		if(input.readBoolean()){
			indexStatus = new IndexStatus(input.readInt(), input.readInt(), "", input.readString(), "");
		}
		duration = input.readInt();
		isSuccess = input.readBoolean();
	}

	@Override
	public void writeTo(DataOutput output) throws IOException {
		output.writeString(collectionId);
		if(indexStatus != null){
			output.writeBoolean(true);
			output.writeInt(indexStatus.getDocumentCount());
			output.writeInt(indexStatus.getDeleteCount());
			output.writeString(indexStatus.getEndTime());
		}else{
			output.writeBoolean(false);
		}
		output.writeInt(duration);
		output.writeBoolean(isSuccess);
	}
}
