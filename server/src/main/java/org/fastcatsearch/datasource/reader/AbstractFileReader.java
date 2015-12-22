package org.fastcatsearch.datasource.reader;

import org.fastcatsearch.datasource.SourceModifier;
import org.fastcatsearch.ir.common.IRException;
import org.fastcatsearch.ir.config.SingleSourceConfig;

import java.io.*;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPInputStream;

/**
 * 파일기반의 소스데이터를 읽어들이는 Abstract Reader이다.
 * GZip으로 압축되어 있다면 자동으로 풀면서 읽어들이고, 압축되지 있지 않은 데이터는 그대로 읽어들인다.
 * 하위 클래스에서는 parse()를 구현하여 어떻게 문서를 읽어들이는지를 정의하도록 한다.
 * */
public abstract class AbstractFileReader extends SingleSourceReader<Map<String,Object>> implements FileFilter {

	private LinkedList<Map<String, Object>> items;
    protected String encoding;
    protected int bufferSize;
    protected int limitSize;
    protected List<String> filePaths;
    protected BufferedReader reader;
    private static final int DEFAULT_BUFFER_SIZE = 100;
    private int readCount;

	public AbstractFileReader() {
		super();
	}

	public AbstractFileReader(String collectionId, File filePath, SingleSourceConfig singleSourceConfig
            , SourceModifier sourceModifier, String lastIndexTime)
			throws IRException {
		super(collectionId, filePath, singleSourceConfig, sourceModifier, lastIndexTime);
	}

	@Override
	public void init() throws IRException {
        String filePathStr = getConfigString("filePath");
		encoding = getConfigString("encoding", "utf-8");
        bufferSize = getConfigInt("bufferSize", DEFAULT_BUFFER_SIZE);
        limitSize = getConfigInt("limitSize");
        if(bufferSize < DEFAULT_BUFFER_SIZE) {
            bufferSize = DEFAULT_BUFFER_SIZE;
        }
        items = new LinkedList<Map<String, Object>>();
		filePaths = new LinkedList<String>();
        String[] pathList = filePathStr.split(",");
        for(String path : pathList) {
            String rootPath = filePath.makePath(path).file().getAbsolutePath();
            File rootFile = new File(rootPath);
            if(rootFile.isDirectory()) {
                rootFile.listFiles(this);
            } else {
                filePaths.add(rootFile.getAbsolutePath());
            }
        }
        readCount = 0;
	}
	
	@Override
	protected void initParameters() {
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
	public boolean hasNext() throws IRException {
		if(items.size() == 0) {
			fill();
		}
		return items.size() > 0;
	}

	@Override
	protected Map<String, Object> next() throws IRException {
        if(items.size() == 0) {
            fill();
        }
        if(items.size() > 0) {
            return items.removeFirst();
        }
		return null;
	}

	private void fill() throws IRException {
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
					String path = filePaths.remove(0);
                    File f = new File(path);
                    if(!f.exists()) {
                        //파일이 없으면 continue
                        logger.error(String.format("File not exists : %s", f.getAbsolutePath()));
                        continue;
                    }
					try {
                        if(isGZipped(f)) {
                            reader = new BufferedReader((new InputStreamReader(new GZIPInputStream(new FileInputStream(f)), encoding)));
                        } else {
                            reader = new BufferedReader(new InputStreamReader(new FileInputStream(f), encoding));
                        }
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
	
    private boolean isGZipped(File file) {
        int magic = 0;
        RandomAccessFile raf = null;
        try {
            raf = new RandomAccessFile(file, "r");
            magic = raf.read() & 0xff | ((raf.read() << 8) & 0xff00);
        } catch (Throwable t) {
            logger.error("error while inspect file header.", t);
        } finally {
            if(raf != null) {
                try {
                    raf.close();
                } catch (IOException ignore) {
                }
            }
        }
        return magic == GZIPInputStream.GZIP_MAGIC;
    }
}
