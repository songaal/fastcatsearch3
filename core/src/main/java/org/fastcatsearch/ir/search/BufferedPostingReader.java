package org.fastcatsearch.ir.search;

import java.io.IOException;

import org.fastcatsearch.ir.index.IndexFieldOption;
import org.fastcatsearch.ir.io.CharVector;
import org.fastcatsearch.ir.io.IndexInput;

public class BufferedPostingReader extends AbstractPostingReader {

	private static final int BUFFER_SIZE = 100;
	private int bufferSize;
	private int bufferPointer;
	private PostingDoc[] buffer;

	private IndexInput postingInput;
	private boolean isStorePosition;

	private int postingCount;
	private int postingRemain;
	private int prevId;

	public BufferedPostingReader(CharVector term, int termPosition, int weight, int documentCount, IndexFieldOption indexFieldOption, IndexInput postingInput, long inputOffset) {
		super(term, termPosition, weight, documentCount);
		this.postingInput = postingInput;
		this.isStorePosition = indexFieldOption.isStorePosition();
		
		try {
			postingInput.seek(inputOffset);
			int len = postingInput.readInt();
			this.postingCount = postingInput.readInt();
			int lastDocNo = postingInput.readInt();
		} catch (IOException e) {

		}
		postingRemain = postingCount;
		prevId = -1;
		
		//lazy creation
		if(postingRemain < BUFFER_SIZE){
			buffer = new PostingDoc[postingRemain];
		}else{
			buffer = new PostingDoc[BUFFER_SIZE];
		}
	}
	
	@Override
	public int size() {
		return postingCount;
	}

	@Override
	public boolean hasNext() throws IOException {
		if(!ensureFilled()){
			return false;
		}

		return bufferPointer < bufferSize;
	}

	private boolean ensureFilled() throws IOException {
		if (bufferPointer == bufferSize) {
			try {
				fill();
				return true;
			} catch (IOException e) {
				logger.error("error while fill posting buffer", e);
                throw e;
			}
		}
		return true;
	}
	
	private void fill() throws IOException {
		// read BUFFER_SIZE amount
		try {
			int docId = -1;
			bufferSize = 0;
			for (int i = 0; i < BUFFER_SIZE && postingRemain > 0; i++) {
				if (prevId >= 0) {
					docId = postingInput.readVInt() + prevId + 1;
				} else {
					docId = postingInput.readVInt();
				}
				int tf = postingInput.readVInt();
				int[] positions = null;
				if (tf > 0 && isStorePosition) {
					int prevPosition = -1;
					positions = new int[tf];
					for (int j = 0; j < tf; j++) {
						if (prevPosition >= 0) {
							positions[j] = postingInput.readVInt() + prevPosition + 1;
						} else {
							positions[j] = postingInput.readVInt();
						}
						prevPosition = positions[j];

					}

					
				}
				buffer[bufferSize++] = new PostingDoc(docId, tf, positions);
//				logger.debug("posting[{}] {} > {}", term, bufferSize-1, buffer[bufferSize-1]);
				postingRemain--;
				prevId = docId;
			}
		} finally {
			bufferPointer = 0;
		}

	}

	@Override
	public PostingDoc next() throws IOException {
		if(!ensureFilled()){
			return null;
		}

		if (bufferPointer < bufferSize) {
			return buffer[bufferPointer++];
		}

		return null;
	}

	@Override
	public void close() {
		if (postingInput != null) {
			try {
				postingInput.close();
			} catch (IOException e) {
				// ignore

			}
			buffer = null;
		}
	}
}
