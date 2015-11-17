///*
// * Copyright 2013 Websquared, Inc.
// *
// * Licensed under the Apache License, Version 2.0 (the "License");
// * you may not use this file except in compliance with the License.
// * You may obtain a copy of the License at
// *
// *   http://www.apache.org/licenses/LICENSE-2.0
// *
// * Unless required by applicable law or agreed to in writing, software
// * distributed under the License is distributed on an "AS IS" BASIS,
// * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// * See the License for the specific language governing permissions and
// * limitations under the License.
// */
//
//package org.fastcatsearch.ir.document;
//
//import java.io.File;
//import java.io.IOException;
//import java.util.zip.DataFormatException;
//import java.util.zip.Deflater;
//import java.util.zip.Inflater;
//
//import org.fastcatsearch.ir.common.IRException;
//import org.fastcatsearch.ir.common.IndexFileNames;
//import org.fastcatsearch.ir.io.BufferedFileInput;
//import org.fastcatsearch.ir.io.BufferedFileOutput;
//import org.fastcatsearch.ir.io.BytesDataInput;
//import org.fastcatsearch.ir.io.IOUtil;
//import org.fastcatsearch.ir.io.IndexInput;
//import org.fastcatsearch.ir.io.IndexOutput;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//
//
//public class DocumentRestorer {
//	private static Logger logger = LoggerFactory.getLogger(DocumentRestorer.class);
//	private final int BUFFER_SIZE = 3 * 1024 * 1024;
//	private File dir;
//
//	public DocumentRestorer(File dir) throws IOException, IRException{
//		this.dir = dir;
//	}
//
//	public void setSize(int newSize) throws IRException, IOException{
//
//		IndexInput docInput = new BufferedFileInput(dir, IndexFileNames.docStored);
//		IndexInput positionInput = new BufferedFileInput(dir, IndexFileNames.docPosition);
//
//		//check document count and block size
//		int prevDocumentCount = docInput.readInt();
//		if(newSize >= prevDocumentCount){
//			//do nothing
//			return;
//		}
//		logger.info("DocumentRestorer size ={} => {}", prevDocumentCount, newSize);
//		int blockSize = docInput.readInt();
//		int docNo = newSize - 1;
//		int docIndex = docNo / blockSize;
//		int docOffset = docNo % blockSize;
//
//		positionInput.seek(docIndex * IOUtil.SIZE_OF_LONG);
//		long pos = positionInput.readLong();
//		docInput.seek(pos);
//		int len = docInput.readInt();
//		positionInput.close();
//
//		if(newSize % blockSize == 0){
//			docInput.close();
//			IndexOutput out = new BufferedFileOutput(dir, IndexFileNames.docStored, true);
//			out.seek(0);
//			out.writeInt(newSize);
//			out.setLength(pos + len);
//			logger.info("restored doc.stored file size() = {}", out.length());
//			out.close();
//		}else{
//			//if doc is in last block
//			byte[] infOutput = new byte[BUFFER_SIZE];
//			byte[] defOutput = new byte[BUFFER_SIZE];
//			byte[] data = new byte[len];
//			docInput.readBytes(data, 0, len);
//			docInput.close();
//
//			Inflater decompresser = new Inflater();
//			decompresser.setInput(data);
//			int resultLength = -1;
//			try {
//				resultLength = decompresser.inflate(infOutput);
//			} catch (DataFormatException e) {
//				throw new IOException("DataFormatException");
//			} finally {
//				decompresser.end();
//			}
////			logger.debug("BlockLength="+resultLength);
//			BytesDataInput bai = new BytesDataInput(infOutput, 0, resultLength);
//			for (int i = 0; i <= docOffset; i++) {
//				int docLen = bai.readInt();
////				logger.debug("bai.position() = "+bai.position()+", docLen = "+docLen);
//				bai.seek(bai.position() + docLen);
//			}
//			int end = (int) bai.position();
//			Deflater compresser = new Deflater(Deflater.BEST_SPEED);
//			compresser.setInput(infOutput, 0, end);
//			compresser.finish();
//
//			int compressedDataLength = compresser.deflate(defOutput);
//
//			IndexOutput out = new BufferedFileOutput(dir, IndexFileNames.docStored, true);
//			out.seek(0);
//			out.writeInt(newSize);
//			out.setLength(pos);
//			out.seek(out.length());//move to end of file
//			out.writeInt(compressedDataLength);
//			out.writeBytes(defOutput, 0, compressedDataLength);
//			logger.info("restored doc.stored file size() = {}", out.length());
//			out.close();
//		}
//
//	}
//
//
//
//
//
//}
