package org.fastcatsearch.job.cluster;

import java.io.File;
import java.io.IOException;

import org.fastcatsearch.common.io.Streamable;
import org.fastcatsearch.exception.FastcatSearchException;
import org.fastcatsearch.ir.io.DataInput;
import org.fastcatsearch.ir.io.DataOutput;
import org.fastcatsearch.job.Job;
import org.fastcatsearch.util.CoreFileUtils;

/**
 * 노드의 특정디렉토리를 삭제한다.
 * */
public class NodeDirectoryCleanJob extends Job implements Streamable {
	
	private static final long serialVersionUID = 5938061955319252477L;

	private File directory;

	public NodeDirectoryCleanJob() {
	}

	public NodeDirectoryCleanJob(File directory) {
		this.directory = directory;
	}

	@Override
	public JobResult doRun() throws FastcatSearchException {

		try {
			
			File directoryFile = environment.filePaths().makePath(directory.getPath()).file();
			
//			FileUtils.deleteDirectory(directoryFile);
			//연이은 디렉토리도 함께 제거. 1이면 2,3,4,도 만약존재시 제거.
			logger.debug("Delete Directory >> {}", directoryFile.getAbsolutePath());
			CoreFileUtils.removeDirectoryCascade(directoryFile);
			
			return new JobResult(true);

		} catch (Exception e) {
			logger.error("", e);
			throw new FastcatSearchException("ERR-00000", e);
		}

	}

	@Override
	public void readFrom(DataInput input) throws IOException {
		directory = new File(input.readString());
	}

	@Override
	public void writeTo(DataOutput output) throws IOException {
		output.writeString(directory.getPath());
	}

}
