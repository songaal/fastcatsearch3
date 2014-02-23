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

package org.fastcatsearch.ir.io;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DirBufferedReader {
	private static Logger logger = LoggerFactory.getLogger(DirBufferedReader.class);
	private BufferedReader reader;

	private File[] fileList;
	private int fileCount;
	private int fileUsed;
	private String encoding;

	public DirBufferedReader(File[] fl, String encoding) throws IOException {
		this.encoding = encoding;
		// TODO fix bug
		fileList = new File[fl.length];

		for (int i = 0; i < fl.length; i++) {
			if (fl[i].isFile()) {
				fileList[fileCount] = fl[i];
				logger.debug("fl[" + i + "]; = " + fl[i].getName());
				fileCount++;
			}
		}

		if (fileList.length > 0) {
			reader = new BufferedReader(new InputStreamReader(new FileInputStream(fileList[0]), encoding));
		}

	}

	public DirBufferedReader(File f, String encoding) throws IOException {
		this.encoding = encoding;
		if (!f.exists()) {
			fileCount = 0;
			fileList = new File[fileCount];
		} else if (f.isDirectory()) {
			File[] fl = f.listFiles();
			for (int i = 0; i < fl.length; i++) {
				if (fl[i].isFile()) {
					if (fl[i].getName().startsWith(".")) {
						logger.debug("Not Normal File=>" + fl[i].getAbsolutePath());
						continue;
					}
					logger.debug("File=>" + fl[i].getAbsolutePath());
					fileCount++;
				} else {
					logger.debug("Not File=>" + fl[i].getAbsolutePath());
				}
			}
			// logger.debug("fileCount = "+fileCount);
			fileList = new File[fileCount];

			int m = 0;
			for (int i = 0; i < fl.length; i++) {
				if (fl[i].isFile()) {
					if (fl[i].getName().startsWith(".")) {
						continue;
					}
					fileList[m] = fl[i];
					// logger.debug("Ofl["+i+"]; = "+fl[i].getAbsolutePath());
					m++;
				} else {
					// logger.debug("Xfl["+i+"]; = "+fl[i].getAbsolutePath());
				}
			}

		} else {
			fileCount = 1;
			fileList = new File[fileCount];
			fileList[0] = f;
		}

		if (fileCount == 0) {
			logger.warn("There's no source file in directory " + f.getAbsolutePath());
			// throw new IOException("There's no source file in directory "+f.getAbsolutePath());
		}

		if (fileList.length > 0) {
			logger.debug("File " + fileList[0].getAbsolutePath() + " opened.");
			reader = new BufferedReader(new InputStreamReader(new FileInputStream(fileList[0]), encoding));
		}

	}

	public File currentFile() {

		if (fileUsed < fileList.length) {
			return fileList[fileUsed];
		}
		return null;
	}

	public String readLine() throws IOException {
		if (reader == null) {
			return null;
		}

		String line = reader.readLine();

		if (line == null) {
			try {
				reader.close();
				fileUsed++;
			} catch (IOException e) {
				// ignore
			}

			while (fileUsed < fileCount && !fileList[fileUsed].exists()) {
				fileUsed++;
			}

			if (fileUsed == fileCount) {
				return null;
			} else {
				reader = new BufferedReader(new InputStreamReader(new FileInputStream(fileList[fileUsed]), encoding));
				// logger.debug("File "+fileList[fileUsed].getAbsolutePath()+" opened.");
				line = reader.readLine();
				while (line == null && fileUsed < fileCount) {
					fileUsed++;
					reader.close();
					if (fileUsed == fileCount)
						break;

					reader = new BufferedReader(new InputStreamReader(new FileInputStream(fileList[fileUsed]), encoding));
					logger.debug("File " + fileList[fileUsed].getAbsolutePath() + " opened.");
					line = reader.readLine();
				}
			}

		}

		return line;
	}

	public void close() throws IOException {
		if (reader != null) {
			reader.close();
		}
	}
}
