package org.fastcatsearch.datasource.reader;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.*;
import org.fastcatsearch.datasource.SourceModifier;
import org.fastcatsearch.datasource.reader.annotation.SourceReader;
import org.fastcatsearch.ir.common.IRException;
import org.fastcatsearch.ir.config.SingleSourceConfig;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SourceReader(name="HDFS_JSON_FILE")
public class HDFSJSONFileReader extends SingleSourceReader<Map<String,Object>> {

	Configuration conf;
	List<HashMap<String, Object>> items;

	public HDFSJSONFileReader() {
		super();
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public HDFSJSONFileReader(String collectionId, File filePath, SingleSourceConfig singleSourceConfig, SourceModifier sourceModifier, String lastIndexTime)
			throws IRException {
		super(collectionId, filePath, singleSourceConfig, sourceModifier, lastIndexTime);
	}

	String encoding;
	String[] fieldId;
	String delimiter;
	List<Path> filePaths;
	int bufferSize;
	BufferedReader reader;
	String DEFAULTFS = "fs.defaultFS";

	private Map<String, Object> record;

	@Override
	public void init() throws IRException, IOException {

		String defaultfs = getConfigString("fs.defaultFS");
		String filePathStr = getConfigString("filePath");

		conf = new Configuration();
		conf.set(DEFAULTFS, defaultfs);

		encoding = getConfigString("encoding", null);

		if(bufferSize < 100) {
			bufferSize = 100;
		}
		
		filePaths = new ArrayList<Path>();
		String[] pathList = filePathStr.split(",");

		for(String path : pathList) {
			FileSystem fs = FileSystem.get(conf);

			String rootPath = filePath.makePath(path).file().getAbsolutePath();
			Path filePath = new Path(rootPath);
			FileStatus rootFile = fs.getFileStatus(filePath);
			if(rootFile.isDirectory()) {
				getFilePathList(rootFile);
			} else {
				filePaths.add(rootFile.getPath());
			}

			fs.close();
		}
	}
	
	@Override
	protected void initParameters() {
		registerParameter(new SourceReaderParameter("fs.defaultFS", "Default File System Name", "The name of the HDFS default file system."
				, SourceReaderParameter.TYPE_STRING_LONG, true, null));
		registerParameter(new SourceReaderParameter("filePath", "File or Dir Path", "Filepath for Indexing.(Relative for Server Home)"
				, SourceReaderParameter.TYPE_STRING_LONG, true, null));
		registerParameter(new SourceReaderParameter("encoding", "Encoding", "File encoding"
				, SourceReaderParameter.TYPE_STRING, false, null));
	}

	@Override
	public boolean hasNext() throws IRException, IOException {
		if(record == null) {
			record = fill();
		}
		return record !=null;
	}

	@Override
	protected Map<String, Object> next() throws IRException, IOException {
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

	private Map<String, Object> fill() throws IRException, IOException {
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
							Path filePath = filePaths.remove(0);
							FileSystem fs = FileSystem.get(conf);
							FileStatus fsStatus = fs.getFileStatus(filePath);
							try {
								reader = new BufferedReader(new InputStreamReader(fs.open(filePath), encoding));
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

	public boolean accept(FileStatus status) throws IOException {

		if(status.isDirectory()) {
			logger.trace("dir:{}", status.getPath());
			getFilePathList(status);
		} else if(status.isFile()) {
			logger.trace("file : {}", status.getPath());
			filePaths.add(status.getPath());
		}

		return true;
	}

	/*
	* HDFS 디렉토리 내부의 파일 리스트를 가져온다.
	* */
	private void getFilePathList(FileStatus rootFile) throws IOException {

		FileSystem fs = FileSystem.get(this.conf);
		String filePathStr = rootFile.getPath().toString();
		Path filePath = new Path(filePathStr);
		FileStatus[] fsStatus = fs.listStatus(filePath);

		for (int cnt = 0; cnt < fsStatus.length; cnt++) {
			FileStatus status = fsStatus[cnt];
			if (status.isDirectory()) {
				// 여기서 하위 디렉토리의 파일들의 리스트를 다시 가져와서 파일 내용을 읽어야 한다.
				getFilePathList(status);
			} else {
				Path path = status.getPath();
				filePaths.add(path);
			}
		}

		fs.close();
	}
}
