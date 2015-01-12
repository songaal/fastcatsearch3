package org.fastcatsearch.datasource.reader;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.fastcatsearch.datasource.SourceModifier;
import org.fastcatsearch.datasource.reader.annotation.SourceReader;
import org.fastcatsearch.ir.common.IRException;
import org.fastcatsearch.ir.config.SingleSourceConfig;

@SourceReader(name="SIMPLE_FILE")
public class SimpleFileReader extends SingleSourceReader<Map<String,Object>> implements FileFilter, Runnable {
	
	public SimpleFileReader() {
		super();
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public SimpleFileReader(File filePath, SingleSourceConfig singleSourceConfig, SourceModifier sourceModifier, String lastIndexTime)
			throws IRException {
		super(filePath, singleSourceConfig, sourceModifier, lastIndexTime);
	}
	
	String rootPath;
	String encoding;
	String fieldId;
	List<String> filePaths;
	boolean finished;
	int bufferSize;
	int currentDepth;
	BufferedReader reader;
	
	private Map<String, Object> record;

	@Override
	public void init() throws IRException { 
		rootPath = getConfigString("rootPath","/");
		encoding = getConfigString("encoding",null);
		fieldId = getConfigString("fieldId").trim().toUpperCase();
		bufferSize = getConfigInt("bufferSize");
		
		if(bufferSize < 100) {
			bufferSize = 100;
		}
		
		finished = false;
		filePaths = new ArrayList<String>();
		currentDepth = 0;
		Thread t = new Thread(this);
		t.start();
	}
	
	@Override
	protected void initParameters() { 
		registerParameter(new SourceReaderParameter("rootPath", "Data Root Path", "Root Filepath for Indexing. (Absolute Path)"
				, SourceReaderParameter.TYPE_STRING_LONG, true, null));
		registerParameter(new SourceReaderParameter("fieldId", "Mapping Field Id", "Mapping Field-Id In Collection Schema"
				, SourceReaderParameter.TYPE_STRING, true, "DATA"));
		registerParameter(new SourceReaderParameter("encoding", "Encoding", "File encoding"
				, SourceReaderParameter.TYPE_STRING, false, null));
		registerParameter(new SourceReaderParameter("bufferSize", "Buffer Size", "Reading Buffer Size"
				, SourceReaderParameter.TYPE_NUMBER, false, "100"));
	}

	@Override
	public boolean hasNext() throws IRException {
		if(record == null) {
			record = fill();
		}
		return record !=null;
	}

	@Override
	protected Map<String, Object> next() throws IRException {
		Map<String,Object> ret = record;
		record = null;
		if(ret != null) {
			return ret;
		} else {
			ret = fill();
		}
		return ret;
	}
	
	private Map<String, Object> fill() throws IRException {
		try {
			while (true) {
				if(reader != null) {
					String rline = null;
					try {
						rline = reader.readLine();
					} catch (IOException ex) {
						logger.error("", ex);
					}
					if(rline != null) {
						Map<String, Object> record = new HashMap<String, Object>();
						record.put(fieldId, rline);
						if (sourceModifier != null) {
							sourceModifier.modify(record);
						}
						this.record = record;
						return record;
					} else {
						//get next reader..
						try {
							reader.close();
						} catch (IOException ignore) { }
						reader = null;
						continue;
					}
				} else {
					if (filePaths.size() > 0) {
						logger.trace("fetch record..");
						String path = filePaths.remove(0);
						try {
							reader = new BufferedReader(new InputStreamReader(new FileInputStream(path),encoding));
						} catch (IOException ex) {
							logger.error("", ex);
						}
						continue;
					} else {
						try {
							logger.trace("waiting..");
							Thread.sleep(100);
						} catch (InterruptedException ex) { 
							logger.debug("CATCH INTERRUPT! {}", ex.getMessage());
							break;
						}
						if(finished && filePaths.size() == 0) {
							break;
						}
						continue;
					}
				}
			}
		} finally {
			if(record == null && reader != null) {
				try {
					reader.close();
				} catch (IOException ignore) { }
			}
		}
		return null;
	}
	
	@Override
	public boolean accept(File file) {
		logger.trace("finished:{} / file:{}", finished, file);
		int currentDepth = this.currentDepth;
		while(!finished) {
			if(filePaths.size() > bufferSize) {
				try {
					Thread.sleep(100);
				} catch (InterruptedException ignore) { }
				continue;
			}
			
			if(file.isDirectory()) {
				logger.trace("dir current:{} ", currentDepth);
					file.listFiles(this);
			} else if(file.isFile()) {
				logger.trace("file : {}", file);
				filePaths.add(file.getAbsolutePath());
			}
			break;
		}
		return false;
	}
	
	@Override
	public void run() {
		File rootFile = new File(rootPath);
		if(rootFile.isDirectory()) {
			rootFile.listFiles(this);
		} else {
			filePaths.add(rootFile.getAbsolutePath());
		}
		finished = true;
	}
}
