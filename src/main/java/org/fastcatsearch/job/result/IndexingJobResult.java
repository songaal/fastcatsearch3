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

import java.io.File;
import java.io.IOException;

import org.fastcatsearch.common.io.StreamInput;
import org.fastcatsearch.common.io.StreamOutput;
import org.fastcatsearch.common.io.Streamable;

public class IndexingJobResult implements Streamable {
	public String collection;
	public int docSize;
	public int updateSize;
	public int deleteSize;
	public int duration;
	public File segmentDir;
	
	public IndexingJobResult(){ }
	
	public IndexingJobResult(String collection, File segmentDir, int docSize, int updateSize, int deleteSize, int duration) {
		this.collection = collection;
		this.segmentDir = segmentDir;
		this.docSize = docSize;
		this.updateSize = updateSize;
		this.deleteSize = deleteSize;
		this.duration = duration;
		
	}

	public String toString(){
		return "[IndexingResult] collection = "+collection+", docSize = "+docSize+", updateSize = "+updateSize+", deleteSize = "+deleteSize+", duration = "+duration+", path="+segmentDir.getPath();
	}

	@Override
	public void readFrom(StreamInput input) throws IOException {
		collection = input.readString();
		docSize = input.readInt();
		updateSize = input.readInt();
		deleteSize = input.readInt();
		duration = input.readInt();
		segmentDir = new File(input.readString());
	}

	@Override
	public void writeTo(StreamOutput output) throws IOException {
		output.writeString(collection);
		output.writeInt(docSize);
		output.writeInt(updateSize);
		output.writeInt(deleteSize);
		output.writeInt(duration);
		output.writeString(segmentDir.getPath());
	}
}
