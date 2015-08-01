package org.fastcatsearch.datasource.reader;

import org.fastcatsearch.datasource.SourceModifier;
import org.fastcatsearch.ir.common.IRException;
import org.fastcatsearch.ir.config.SingleSourceConfig;

import java.io.*;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPInputStream;


public abstract class AbstractFileReader extends SingleSourceReader<Map<String,Object>> implements FileFilter {

	private LinkedList<Map<String, Object>> items;
    protected String rootPath;
    protected String encoding;
    protected boolean isCompressed;
    protected int bufferSize;
    protected List<String> filePaths;
    protected BufferedReader reader;
    private static final int DEFAULT_BUFFER_SIZE = 100;

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
		rootPath = filePath.makePath(getConfigString("filePath")).file().getAbsolutePath();
		encoding = getConfigString("encoding", null);
        isCompressed = getConfigBoolean("compressed", false);
        bufferSize = getConfigInt("bufferSize", DEFAULT_BUFFER_SIZE);
        if(bufferSize < DEFAULT_BUFFER_SIZE) {
            bufferSize = DEFAULT_BUFFER_SIZE;
        }
        items = new LinkedList<Map<String, Object>>();
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
        registerParameter(new SourceReaderParameter("compressed", "GZip Compressed", "GZip compressed"
                , SourceReaderParameter.TYPE_CHECK, true, "false"));
        registerParameter(new SourceReaderParameter("bufferSize", "Buffer Size", "Read Buffer Size"
                , SourceReaderParameter.TYPE_NUMBER, true, String.valueOf(DEFAULT_BUFFER_SIZE)));
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
					Map<String, Object> record = parse(reader);
					if (sourceModifier != null) {
						sourceModifier.modify(record);
					}
                    items.addLast(record);
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
                        if(isCompressed) {
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
