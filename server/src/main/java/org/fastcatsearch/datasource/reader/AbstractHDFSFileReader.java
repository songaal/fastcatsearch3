package org.fastcatsearch.datasource.reader;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.*;
import org.fastcatsearch.datasource.SourceModifier;
import org.fastcatsearch.ir.common.IRException;
import org.fastcatsearch.ir.config.SingleSourceConfig;

import java.io.*;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 2017-06-15 지앤클라우드 전제현
 * HDFS에 저장된 파일기반의 소스데이터를 읽어들이는 Abstract Reader입니다.
 * */
public abstract class AbstractHDFSFileReader extends SingleSourceReader<Map<String,Object>> {

	private Configuration conf;
	private LinkedList<Map<String, Object>> items;
	protected String encoding;
	protected int bufferSize;
	protected int limitSize;
    protected List<Path> filePaths;
    protected BufferedReader reader;
    private static final int DEFAULT_BUFFER_SIZE = 100;
	private static final String DEFAULTFS = "fs.defaultFS";
    private int readCount;
	protected String reader_file_type;
	private String allowPattern;
	private String PATTERN_CSV = ".+\\.(csv)$";
	private String PATTERN_JSON = ".+\\.(json)$";

	public AbstractHDFSFileReader() {
		super();
	}

	public AbstractHDFSFileReader(String collectionId, File file, SingleSourceConfig singleSourceConfig
            , SourceModifier sourceModifier, String lastIndexTime)
			throws IRException, IOException {
		super(collectionId, file, singleSourceConfig, sourceModifier, lastIndexTime);
	}

	@Override
	public void init() throws IRException, IOException {

		String defaultfs = getConfigString("fs.defaultFS");
		String filePathStr = getConfigString("filePath");

		conf = new Configuration();
		conf.set(DEFAULTFS, defaultfs);

		bufferSize = getConfigInt("bufferSize", DEFAULT_BUFFER_SIZE);
		limitSize = getConfigInt("limitSize");
		if(bufferSize < DEFAULT_BUFFER_SIZE) {
			bufferSize = DEFAULT_BUFFER_SIZE;
		}

		encoding = getConfigString("encoding", "utf-8");
        items = new LinkedList<Map<String, Object>>();
		filePaths = new LinkedList<Path>();
        String[] pathList = filePathStr.split(",");
        for(String path : pathList) {
			FileSystem fs = FileSystem.get(conf);

			// 현재 바라보고 있는 경로가 소스 리더에서 요구하고 있는 확장자 파일인지를 확인한다.
            String rootPath = filePath.makePath(path).file().getAbsolutePath();
			if (reader_file_type.equalsIgnoreCase("CSV")) {
				allowPattern = PATTERN_CSV;
			} else {
				allowPattern = PATTERN_JSON;
			}
			boolean check = false;
			Pattern p = Pattern.compile(allowPattern);
			Matcher m = p.matcher(rootPath);
			check = m.matches();

			Path filePath = new Path(rootPath);
			FileStatus rootFile = fs.getFileStatus(filePath);
			if (rootFile.isDirectory()) {
				getFilePathList(rootFile);
			} else if (check) {
				// CSV 확장자가 아닌 파일은 제외한다.
				filePaths.add(rootFile.getPath());
			}

			fs.close();
        }
        readCount = 0;
	}
	
	@Override
	protected void initParameters() {
		registerParameter(new SourceReaderParameter("fs.defaultFS", "Default File System Name", "The name of the HDFS default file system."
				, SourceReaderParameter.TYPE_STRING_LONG, true, null));
		registerParameter(new SourceReaderParameter("filePath", "File or Dir Path", "File path for reading source file. Absolute path or relative path for collection home directory. Multiple paths are allowed with commas."
				, SourceReaderParameter.TYPE_STRING_LONG, true, null));
		registerParameter(new SourceReaderParameter("encoding", "Encoding", "File encoding"
				, SourceReaderParameter.TYPE_STRING, true, "utf-8"));
		registerParameter(new SourceReaderParameter("bufferSize", "Buffer Size", "Read Buffer Size"
				, SourceReaderParameter.TYPE_NUMBER, true, String.valueOf(DEFAULT_BUFFER_SIZE)));
		registerParameter(new SourceReaderParameter("limitSize", "Limit Size", "Read documents within limit size."
				, SourceReaderParameter.TYPE_NUMBER, false, ""));
	}

	@Override
	public boolean hasNext() throws IRException, IOException {
		if(items.size() == 0) {
			fill();
		}
		return items.size() > 0;
	}

	@Override
	protected Map<String, Object> next() throws IRException, IOException {
        if(items.size() == 0) {
            fill();
        }
        if(items.size() > 0) {
            return items.removeFirst();
        }
		return null;
	}

	private void fill() throws IRException, IOException {
		while (true) {
			if(reader != null) {
				try {
					if(items.size() >= bufferSize) {
						return;
					}
					if(limitSize > 0 && readCount >= limitSize) {
						return;
					}
					Map<String, Object> record = parse(reader);
					items.addLast(record);
					readCount++;
				} catch(IOException e) {
					//get next reader..
					try {
						reader.close();
					} catch (IOException ignore) { }
					reader = null;
				}
			} else {
				while (filePaths.size() > 0) {
					Path filePath = filePaths.remove(0);
					FileSystem fs = FileSystem.get(conf);
					FileStatus fsStatus = fs.getFileStatus(filePath);
					if(!fsStatus.isFile()) {
						//파일이 없으면 continue
						logger.error(String.format("File not exists : %s", filePath));
						continue;
					}
					try {
						/*
						압축한 파일은 우선 생각하지 않도록 한다.
						if(isGZipped(f)) {
							reader = new BufferedReader((new InputStreamReader(new GZIPInputStream(new FileInputStream(f)), encoding)));
						} else {
							reader = new BufferedReader(new InputStreamReader(new FileInputStream(f), encoding));
						}
						*/
						reader = new BufferedReader(new InputStreamReader(fs.open(filePath), encoding));
						initReader(reader);
						break;
					} catch (IOException ex) {
						logger.error("", ex);
						if(reader != null) {
							try {
								reader.close();
							} catch (IOException ignore) {
							}
							reader = null;
						}
					}
				}
				//파일이 더 이상 없으면 끝낸다.
				if(reader == null) {
					break;
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
			boolean check = false;
			Pattern p = Pattern.compile(allowPattern);
			Matcher m = p.matcher(status.getPath().toString());
			check = m.matches();

			if (status.isDirectory()) {
				// 여기서 하위 디렉토리의 파일들의 리스트를 다시 가져와서 파일 내용을 읽어야 한다.
				getFilePathList(status);
			} else if (check) {
				// CSV 확장자가 아닌 파일은 제외한다.
				Path path = status.getPath();
				filePaths.add(path);
			}
		}

		fs.close();
	}
}
