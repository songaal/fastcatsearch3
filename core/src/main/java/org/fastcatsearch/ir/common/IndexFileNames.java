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
	
	public static final String docStored = "doc.stored";
	public static final String docPosition = "doc.position";
	public static final String docDeleteSet = "delete.set"; //deleted docs in segment
	public static final String primaryKeyMap = "pk.map";
	public static final String primaryKeyMapIndex = "pk.map.index";
	
	public static final String postingFile = "posting";
	public static final String lexiconFile = "lexicon";
	public static final String indexFile = "index";
	public static final String tempFile = "temp";
	
	public static final String fieldIndexFile = "field.index";
	
	public static final String groupIndexFile = "group.index";
	public static final String groupKeyFile = "group.key"; //그룹순차번호별 key string저장.
	public static final String groupKeyMap = "group.pk"; //증분색인시 이전 리비전과의 머징을 위해 필요한 pk파일. group writer에서만 사용된다.
//	public static final String groupKeyMapIndex = "group.pk.index"; //그룹 pk의 index파일이나 단순 생성만 하고 사용은 안함. 
	public static final String groupInfoFile = "group.info";
	
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
	
//	public static String getIndexSuffixFileName(String name, String suffix){
//		return name + ".index." + suffix;
//	}
	public static String getIndexFileName(String name){
		return name + ".index";
	}
	
	public static File getRevisionDir(File parent, int revision){
		return new File(parent, Integer.toString(revision));
	}
	
//	public static File getBackupDir(File parent, int revision){
//		return new File(parent, ".bak."+revision);
//	}
}
