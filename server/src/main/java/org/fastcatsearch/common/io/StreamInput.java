/*
 * Licensed to ElasticSearch and Shay Banon under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. ElasticSearch licenses this
 * file to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.fastcatsearch.common.io;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.fastcatsearch.common.BytesArray;
import org.fastcatsearch.common.BytesReference;
import org.fastcatsearch.common.Strings;
import org.fastcatsearch.ir.io.BytesBuffer;
import org.fastcatsearch.ir.io.DataInput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 */
public abstract class StreamInput extends DataInput {
	

	/**
	 * Reads a bytes reference from this stream, might hold an actual reference to the underlying bytes of the stream.
	 */
	public BytesReference readBytesReference() throws IOException {
		int length = readVInt();
		return readBytesReference(length);
	}

	/**
	 * Reads a bytes reference from this stream, might hold an actual reference to the underlying bytes of the stream.
	 */
	public BytesReference readBytesReference(int length) throws IOException {
		if (length == 0) {
			return BytesArray.EMPTY;
		}
		byte[] bytes = new byte[length];
		readBytes(bytes, 0, length);
		return new BytesArray(bytes, 0, length);
	}

}
