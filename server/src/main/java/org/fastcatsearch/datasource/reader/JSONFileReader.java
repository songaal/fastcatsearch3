package org.fastcatsearch.datasource.reader;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.fastcatsearch.datasource.SourceModifier;
import org.fastcatsearch.datasource.reader.annotation.SourceReader;
import org.fastcatsearch.ir.common.IRException;
import org.fastcatsearch.ir.config.SingleSourceConfig;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SourceReader(name="JSON_FILE")
public class JSONFileReader extends SingleSourceReader<Map<String,Object>> implements FileFilter {

	List<HashMap<String, Object>> items;

	public JSONFileReader() {
		super();
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public JSONFileReader(String collectionId, File filePath, SingleSourceConfig singleSourceConfig, SourceModifier sourceModifier, String lastIndexTime)
			throws IRException {
		super(collectionId, filePath, singleSourceConfig, sourceModifier, lastIndexTime);
	}
	
	String rootPath;
	String encoding;
	String[] fieldId;
	String delimiter;
	List<String> filePaths;
	int bufferSize;
	BufferedReader reader;
	
	private Map<String, Object> record;

	@Override
	public void init() throws IRException { 
		rootPath = filePath.makePath(getConfigString("filePath")).file().getAbsolutePath();
		encoding = getConfigString("encoding", null);

		if(bufferSize < 100) {
			bufferSize = 100;
		}
		
		filePaths = new ArrayList<String>();

		File rootFile = new File(rootPath);
		if(rootFile.isDirectory()) {
			rootFile.listFiles(this);
		} else {
			filePaths.add(rootFile.getAbsolutePath());
		}
	}
	
	@Override
	protected void initParameters() { 
		registerParameter(new SourceReaderParameter("filePath", "File or Dir Path", "Filepath for Indexing.(Relative for Server Home)"
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

	int pos;

	private Map<String, Object> fill() throws IRException {
		try {
			while (true) {
				if(items != null) {
					if(pos  < items.size()) {
						Map<String, Object> item = items.get(pos++);
						return item;
					}
					items = null;
				} else {
					if (reader != null) {
						try {
							JsonFactory jsonFactory = new JsonFactory();
							ObjectMapper mapper = new ObjectMapper(jsonFactory);
							TypeReference<List<HashMap<String, Object>>> typeRef = new TypeReference<List<HashMap<String, Object>>>(){};
							Object docs = mapper.readValue(reader, typeRef);
							items = (List<HashMap<String, Object>>) docs;
							pos = 0;

						} catch (IOException ex) {
							logger.error("", ex);
						}

//						HashMap<String, Object> item = null;
//						if(pos  < items.size()) {
//							item = items.get(pos++);
//						} else {
//							items = null;
//						}

						try {
							reader.close();
						} catch (IOException ignore) {
						}
						reader = null;

//						if (item != null) {
//							if (sourceModifier != null) {
//								sourceModifier.modify(item);
//							}
//							return item;
//						} else {
//							//get next reader..
//							try {
//								reader.close();
//							} catch (IOException ignore) {
//							}
//							reader = null;
//							continue;
//						}
					} else {
						if (filePaths.size() > 0) {
							logger.trace("fetch record..");
							String path = filePaths.remove(0);
							try {
								reader = new BufferedReader(new InputStreamReader(new FileInputStream(path), encoding));
							} catch (IOException ex) {
								logger.error("", ex);
							}
//							continue;
						} else {
							break;
						}
					}
				}
			}
		} finally {
			if(reader != null) {
				try {
					reader.close();
				} catch (IOException ignore) { }
			}
		}
		return null;
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
