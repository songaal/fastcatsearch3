package org.fastcatsearch.util;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Enumeration;

public class DirectoryFileEnumeration implements Enumeration<File> {

	//최종결과 파일들.
	private Deque<File> resultQueue;
	//중간 디렉토리들.
	private Deque<File> directoryQueue;
	//최종결과 파일 선별시 사용되는 filter
	private FileFilter resultFileFilter;

	public DirectoryFileEnumeration(File root, FileFilter resultFileFilter) {
		this.resultFileFilter = resultFileFilter;
		resultQueue = new ArrayDeque<File>();
		directoryQueue = new ArrayDeque<File>();
		directoryQueue.addLast(root);
	}

	public int getDirectoryQueueSize(){
		return directoryQueue.size();
	}
	
	@Override
	public boolean hasMoreElements() {
		while (resultQueue.size() == 0) {

			if (directoryQueue.size() == 0) {
				// 파일도 없고 디렉토리도 없으면 탐색 끝.
				return false;
			} else {
				File dir = directoryQueue.pollFirst();
				if(resultFileFilter != null && resultFileFilter.accept(dir)){
					resultQueue.addLast(dir);
				}
				//listFile호출시 resultFileFilter를 적용하지 않는다.
				//만약 filter에 directory를 제외하는 로직이 있다면 하위 디렉토리 탐색이 불가능해진다.
				for (File f : dir.listFiles()) {
					if (f.isDirectory()) {
						directoryQueue.addLast(f);
					} else {
						//결과file선별시에만 resultFileFilter를 적용한다.
						if(resultFileFilter != null && resultFileFilter.accept(f)){
							resultQueue.addLast(f);
						}
					}
				}
			}
		}

		return resultQueue.size() > 0;
	}

	@Override
	public File nextElement() {
		return resultQueue.pollFirst();
	}

}
