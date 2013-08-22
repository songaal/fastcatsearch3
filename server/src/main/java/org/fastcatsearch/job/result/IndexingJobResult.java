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
import org.fastcatsearch.ir.config.DataInfo.RevisionInfo;
import org.fastcatsearch.ir.io.DataInput;
import org.fastcatsearch.ir.io.DataOutput;

public class IndexingJobResult implements Streamable {
	public String collectionId;
	public String shardId;
	public RevisionInfo revisionInfo;
	public int duration;
	public boolean isSuccess;

	public IndexingJobResult() {
	}

	public IndexingJobResult(String collectionId, String shardId, RevisionInfo revisionInfo, int duration) {
		this(collectionId, shardId, revisionInfo, duration, true);
	}

	public IndexingJobResult(String collectionId, String shardId, RevisionInfo revisionInfo, int duration, boolean isSuccess) {
		this.collectionId = collectionId;
		this.shardId = shardId;
		this.revisionInfo = revisionInfo;
		this.duration = duration;
		this.isSuccess = isSuccess;
	}

	public String toString() {
		return "[IndexingResult] success[" + isSuccess + "] collectionId = " + collectionId + ", shardId = " + shardId + ", revisionInfo = " + revisionInfo + " duration = "
				+ duration;
	}

	@Override
	public void readFrom(DataInput input) throws IOException {
		collectionId = input.readString();
		shardId = input.readString();
		revisionInfo = new RevisionInfo(input.readInt(), input.readInt(), input.readInt(), input.readInt(), input.readInt(), input.readString());
		duration = input.readInt();
		isSuccess = input.readBoolean();
	}

	@Override
	public void writeTo(DataOutput output) throws IOException {
		output.writeString(collectionId);
		output.writeString(shardId);
		output.writeInt(revisionInfo.getDocumentCount());
		output.writeInt(revisionInfo.getInsertCount());
		output.writeInt(revisionInfo.getUpdateCount());
		output.writeInt(revisionInfo.getDeleteCount());
		output.writeString(revisionInfo.getCreateTime());
		output.writeInt(duration);
		output.writeBoolean(isSuccess);
	}
}
