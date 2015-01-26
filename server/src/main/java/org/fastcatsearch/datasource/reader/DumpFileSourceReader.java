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

package org.fastcatsearch.datasource.reader;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.fastcatsearch.datasource.SourceModifier;
import org.fastcatsearch.datasource.reader.annotation.SourceReader;
import org.fastcatsearch.env.Environment;
import org.fastcatsearch.ir.common.IRException;
import org.fastcatsearch.ir.config.DataSourceConfig;
import org.fastcatsearch.ir.config.SingleSourceConfig;
import org.fastcatsearch.ir.index.PrimaryKeys;
import org.fastcatsearch.ir.io.DirBufferedReader;
import org.fastcatsearch.ir.settings.SchemaSetting;

@SourceReader(name="DUMP_FILE")
public class DumpFileSourceReader extends SingleSourceReader<Map<String, Object>> {

	protected DirBufferedReader br;
	protected Map<String, Object> dataMap;

	protected static String DOC_START = "<doc>";
	protected static String DOC_END = "</doc>";

	protected static String OPEN_PATTERN = "^<([\\w]+[^>]*)>$";
	protected static String CLOSE_PATTERN = "^<\\/([\\w]+[^>]*)>$";
	protected Pattern OPAT;
	protected Pattern CPAT;

	public DumpFileSourceReader() {
		super();
	}
	
	public DumpFileSourceReader(String collectionId, File filePath, SingleSourceConfig singleSourceConfig, SourceModifier<Map<String, Object>> sourceModifier, String lastIndexTime) throws IRException {
		super(collectionId, filePath, singleSourceConfig, sourceModifier, lastIndexTime);
	}

	@Override
	public void init() throws IRException {
		String fileEncoding = getConfigString("encoding");
		if (fileEncoding == null) {
			fileEncoding = Charset.defaultCharset().toString();
		}
		try {
			File file = filePath.makePath(getConfigString("filepath")).file();
			br = new DirBufferedReader(file, fileEncoding);
			logger.info("Collect file = {}, {}", file.getAbsolutePath(), fileEncoding);
		} catch (UnsupportedEncodingException e) {
			logger.error(e.getMessage(), e);
			throw new IRException(e);
		} catch (FileNotFoundException e) {
			logger.error(e.getMessage(), e);
			throw new IRException(e);
		} catch (IOException e) {
			logger.error(e.getMessage(), e);
			throw new IRException(e);
		}
		dataMap = null;

		OPAT = Pattern.compile(OPEN_PATTERN);
		CPAT = Pattern.compile(CLOSE_PATTERN);
	}

	@Override
	public boolean hasNext() throws IRException {
		String line = null;
		dataMap = new HashMap<String, Object>();

		String oneDoc = readOneDoc();
		if (oneDoc == null) {
			return false;
		}

		BufferedReader reader = new BufferedReader(new StringReader(oneDoc));

		StringBuffer sb = new StringBuffer();

		String openTag = "";
		boolean isOpened = false;

		while (true) {
			try {
				line = reader.readLine();

				if (line == null) {

					break;
				}

				if (line.length() == 0) {
					continue;
				}

				line = line.trim();

				if (line.length() > 1 && line.charAt(0) == '<' && line.charAt(1) != '/') {
					Matcher m = OPAT.matcher(line);
					if (m.matches()) {
						if(!isOpened){
							String tag = m.group(1);
							openTag = tag;
							isOpened = true;
	//						if (logger.isTraceEnabled()) {
	//							logger.trace("OpenTag [{}]", tag);
	//						}
							continue;
						}
					}
				}

				if (isOpened && line.startsWith("</")) {
					Matcher m = CPAT.matcher(line);
					if (m.matches()) {
						String closeTag = m.group(1);
						if (openTag.equals(closeTag)) {
							isOpened = false;
							String targetStr = sb.toString();
//							if (logger.isTraceEnabled()) {
//								logger.trace("CloseTag [{}]", closeTag);
//								logger.trace("Data [{}]", targetStr);
//							}
							dataMap.put(openTag.toUpperCase(), targetStr);
							sb = new StringBuffer();
							continue;
						}
					}
				}

				if (sb.length() > 0) {
					sb.append(Environment.LINE_SEPARATOR);
				}

				sb.append(line);

				// logger.debug(sb.toString());

			} catch (IOException e) {
				logger.error(e.getMessage(), e);
				throw new IRException(e);
			}
		}
		// logger.debug("doc = "+document);
		if (dataMap == null)
			return false;

		return true;

	}

	private String checkDeleteDocs(String line) throws IOException {
		while ("<delete_doc>".equals(line)) {
			line = nextLine();
			if (line == null) {
				return null;
			}
			int keySize = deleteIdList.keySize();
			PrimaryKeys pk = new PrimaryKeys(keySize);
			int i = 0;
			while(!line.equals("</delete_doc>")) {
				pk.set(i++, line);
				line = nextLine();
			}
			logger.debug("Delete request>> {}", pk);
			deleteIdList.add(pk);
			line = nextLine();
		}
		
		return line;
	}
	

	protected String nextLine() throws IOException {
		String line = br.readLine();

		if (line == null) {
			return null;
		}

		line = line.trim();

		while (line.length() == 0) {
			line = nextLine();
			if (line == null) {
				return null;
			}
			line = line.trim();
		}
		return line;
	}

	private String readOneDoc() throws IRException {
		try {
			StringBuffer sb = new StringBuffer();

			String line = nextLine();

			if (line == null) {
				return null;
			}

			line = checkDeleteDocs(line);
			if (line == null) {
				return null;
			}
			
			int lineNumber = 0;

			while (!line.equals(DOC_START)) {
				// doc opened
				line = nextLine();
				if (line == null) {
					return null;
				}
			}

			line = nextLine();

			// doc started
			while (!line.equals(DOC_END)) {
				// doc ended
				if (lineNumber >= 1) {
					sb.append(Environment.LINE_SEPARATOR);
				}
				sb.append(line);
				line = nextLine();
				lineNumber++;
			}
			return sb.toString();
		} catch (IOException e) {
			throw new IRException(e);
		}
	}

	@Override
	public Map<String, Object> next() throws IRException {
		return dataMap;
	}

	@Override
	public void close() throws IRException {
		try {
			if (br != null) {
				br.close();
			}
		} catch (IOException e) {
			throw new IRException(e);
		}
	}

	@Override
	protected void initParameters() {
		registerParameter(new SourceReaderParameter("filepath", "File Path", "Filepath for indexing."
				, SourceReaderParameter.TYPE_STRING_LONG, true, null));
		registerParameter(new SourceReaderParameter("encoding", "Encoding", "File encoding"
				, SourceReaderParameter.TYPE_STRING, true, null));
	}


}
