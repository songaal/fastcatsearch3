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

package org.fastcatsearch.transport;

/**
 */
public class TransportOption {

	private static final byte TYPE_MESSAGE_FILE = 1 << 0;
	
    private static final byte STATUS_REQRES = 1 << 0;
    private static final byte STATUS_ERROR = 1 << 1; 
    private static final byte STATUS_COMPRESS = 1 << 2;
    private static final byte STATUS_OBJECT = 1 << 3; //결과객체가 streamable이 아닌 Object인지 여부.

    public static boolean isRequest(byte value) {
        return (value & STATUS_REQRES) == 0;
    }

    public static byte setRequest(byte value) {
        value &= ~STATUS_REQRES;
        return value;
    }

    public static boolean isResponse(byte value) {
        return (value & STATUS_REQRES) != 0;
    }
    
    public static byte setResponse(byte value) {
        value |= STATUS_REQRES;
        return value;
    }

    public static boolean isError(byte value) {
        return (value & STATUS_ERROR) != 0;
    }

    public static byte setErrorResponse(byte value) {
    	value = setResponse(value);
        value = setError(value);
        return value;
    }
    
    public static byte setError(byte value) {
        value |= STATUS_ERROR;
        return value;
    }

    public static boolean isCompress(byte value) {
        return (value & STATUS_COMPRESS) != 0;
    }

    public static byte setCompress(byte value) {
        value |= STATUS_COMPRESS;
        return value;
    }
    
    public static boolean isResponseObject(byte value) {
        return (value & STATUS_OBJECT) != 0;
    }

    public static byte setResponseObject(byte value) {
        value |= STATUS_OBJECT;
        return value;
    }
    
    
    public static boolean isTypeMessage(byte value) {
        return (value & TYPE_MESSAGE_FILE) == 0;
    }
    
    public static byte setTypeMessage(byte value) {
        value &= ~TYPE_MESSAGE_FILE;
        return value;
    }
    
    public static boolean isTypeFile(byte value) {
        return (value & TYPE_MESSAGE_FILE) != 0;
    }
    
    public static byte setTypeFile(byte value) {
        value |= TYPE_MESSAGE_FILE;
        return value;
    }
    
}
