/*
 * Copyright 2013 Websquared, Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.fastcatsearch.ir.common;

import java.io.File;

public class IndexFileNames {
//	public static String segmentInfoFile = "segment.info";
//	public static String collectionInfoFile = "collection.info";
	
	public static String docStored = "doc.stored";
	public static String docPosition = "doc.position";
	public static String docDeleteSet = "delete.set"; //deleted docs in segment
	public static String primaryKeyMap = "pk.map";
	public static String primaryKeyMapIndex = "pk.map.index";
	
	public static String postingFile = "posting";
	public static String lexiconFile = "lexicon";
	public static String indexFile = "index";
	public static String tempFile = "temp";
	
	public static String fieldIndexFile = "field.index";
	
	public static String groupDataFile = "group.data";
	public static String groupKeyFile = "group.key";
	public static String groupKeyMap = "group.map";
	public static String groupKeyMapIndex = "group.map.index";
	public static String groupInfoFile = "group.info";
	
//	public static String dataSequenceFile = "data.sequence";
	
	public static String getTempFileName(String name){
		return name + "." + tempFile;
	}
	
	public static String getSuffixFileName(String name, String suffix){
		return name + "." + suffix;
	}
	public static String getSuffixFileName(String name, String... suffixList){
		for(String suffix : suffixList){
			name += ("."+suffix);
		}
		return name;
	}
	public static String getMultiValueSuffixFileName(String name, String suffix){
		return name + ".mv." + suffix;
	}
	
	public static File getRevisionDir(File parent, int revision){
		return new File(parent, revision+"/");
	}
	
	public static File getBackupDir(File parent, int revision){
		return new File(parent, ".bak."+revision);
	}
}
