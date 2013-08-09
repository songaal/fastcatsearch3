package org.fastcatsearch.ir;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.fastcatsearch.ir.common.IndexFileNames;
import org.fastcatsearch.ir.index.IndexWriteInfo;
import org.fastcatsearch.ir.io.BufferedFileInput;
import org.fastcatsearch.ir.io.BufferedFileOutput;
import org.fastcatsearch.ir.io.IOUtil;
import org.fastcatsearch.ir.io.IndexInput;
import org.fastcatsearch.ir.io.IndexOutput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MirrorSynchronizer {

	protected static final Logger logger = LoggerFactory.getLogger(MirrorSynchronizer.class);

	public File createMirrorSyncFile(List<IndexWriteInfo> indexWriteInfoList, File revisionDir) {
		File segmentDir = revisionDir.getParentFile();
		File file = new File(revisionDir, IndexFileNames.mirrorSync);
		IndexOutput output = null;
		try {
			byte[] buffer = new byte[4096];
			output = new BufferedFileOutput(file);
			output.writeInt(indexWriteInfoList.size());
			for (IndexWriteInfo indexWriteInfo : indexWriteInfoList) {
				String filename = indexWriteInfo.filename();
				File sourceFile = new File(segmentDir, indexWriteInfo.filename());
				long offset = indexWriteInfo.offset();
				long limit = indexWriteInfo.limit();
				long count = limit - offset;
				output.writeString(filename);
				output.writeVLong(count);
				IOUtil.transferFrom(output, sourceFile, offset, count, buffer);
			}

			logger.info("동기화 파일 {} 생성완료!", file.getAbsolutePath());
		} catch (IOException e) {
			logger.error("", e);
		} finally {
			try {
				if (output != null) {
					output.close();
				}
			} catch (IOException ignore) {
			}
		}

		return file;
	}

	public void applyMirrorSyncFile(File mirrorSyncFile, File revisionDir) throws IOException {

		File segmentDir = revisionDir.getParentFile();
		IndexInput input = null;
		try {
			input = new BufferedFileInput(mirrorSyncFile);
			int size = input.readInt();
			byte[] buffer = new byte[4096];
			for (int i = 0; i < size; i++) {
				String filename = input.readString();
				long length = input.readVLong();
				logger.debug("apply file[{}] [{}B]", filename, length);
				File targetFile = new File(segmentDir, filename);
				IndexOutput output = null;
				try {
					output = new BufferedFileOutput(targetFile, true);
					IOUtil.transferFrom(output, input, length, buffer);
				} finally {
					if (output != null) {
						try {
							output.close();
						} catch (IOException ignore) {
						}
					}
				}
			}
			input.close();
		} finally {
			if (input != null) {
				try {
					input.close();
				} catch (IOException ignore) {
				}
			}
		}

	}
}
