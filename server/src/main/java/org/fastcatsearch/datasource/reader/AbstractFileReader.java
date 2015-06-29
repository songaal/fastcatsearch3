package org.fastcatsearch.datasource.reader;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.fastcatsearch.datasource.SourceModifier;
import org.fastcatsearch.datasource.reader.annotation.SourceReader;
import org.fastcatsearch.ir.common.IRException;
import org.fastcatsearch.ir.config.SingleSourceConfig;

import java.io.*;
import java.util.*;


public abstract class AbstractFileReader extends SingleSourceReader<Map<String,Object>> implements FileFilter {

	private List<HashMap<String, Object>> items;

	public AbstractFileReader() {
		super();
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public AbstractFileReader(String collectionId, File filePath, SingleSourceConfig singleSourceConfig, SourceModifier sourceModifier, String lastIndexTime)
			throws IRException {
		super(collectionId, filePath, singleSourceConfig, sourceModifier, lastIndexTime);
	}
	
	protected String rootPath;
	protected String encoding;
	protected String[] fieldId;
	protected List<String> filePaths;
	protected BufferedReader reader;
	
	private Map<String, Object> record;

	@Override
	public void init() throws IRException { 
		rootPath = filePath.makePath(getConfigString("filePath")).file().getAbsolutePath();
		encoding = getConfigString("encoding", null);

		filePaths = new LinkedList<String>();

		File rootFile = new File(rootPath);
		if(rootFile.isDirectory()) {
			rootFile.listFiles(this);
		} else {
			filePaths.add(rootFile.getAbsolutePath());
		}
	}
	
	@Override
	protected void initParameters() {
		registerParameter(new SourceReaderParameter("filePath", "File or Dir Path", "Filepath for Indexing. Absolute or Relative path for server-home)"
				, SourceReaderParameter.TYPE_STRING_LONG, true, null));
		registerParameter(new SourceReaderParameter("encoding", "Encoding", "File encoding"
				, SourceReaderParameter.TYPE_STRING, false, null));
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
		while (true) {
			if(reader != null) {
				try {
					Map<String, Object> record = parse(reader);

					if (sourceModifier != null) {
						sourceModifier.modify(record);
					}
					return record;
				}catch(IOException e) {
					//get next reader..
					try {
						reader.close();
					} catch (IOException ignore) { }
					reader = null;
				}
			} else {
				if (filePaths.size() > 0) {
					String path = filePaths.remove(0);
					try {
						reader = new BufferedReader(new InputStreamReader(new FileInputStream(path), encoding));
						initReader(reader);
					} catch (IOException ex) {
						logger.error("", ex);
					}
				} else {
					//파일리스트가 0 이면 끝이다.
					return null;
				}
			}
		}
	}


	/*
	* 읽은 데이터를 리턴한다.
	* reader가 EOF등에 다다르면 IOException을 던져서 다음 reader를 준비하도록 한다.
	* */
	protected abstract Map<String, Object> parse(BufferedReader reader) throws IRException, IOException;

	protected abstract void initReader(BufferedReader reader) throws IRException, IOException;

	@Override
	public void close() throws IRException {
		super.close();
		if(reader != null) {
			try {
				reader.close();
			} catch (IOException ignore) {
			}
		}

	}

	@Override
	public boolean accept(File file) {

		if(file.isDirectory()) {
			logger.trace("dir:{}", file.getAbsolutePath());
			file.listFiles(this);
		} else if(file.isFile()) {
			logger.trace("file : {}", file.getAbsolutePath());
			if(! file.isHidden()) {
				filePaths.add(file.getAbsolutePath());
			}
		}

		return true;
	}
	

}
