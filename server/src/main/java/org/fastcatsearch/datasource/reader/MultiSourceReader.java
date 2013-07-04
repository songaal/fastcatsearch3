/*
 * Copyright (c) 2013 Websquared, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 * 
 * Contributors:
 *     swsong - initial API and implementation
 */

package org.fastcatsearch.datasource.reader;

import java.util.List;

import org.fastcatsearch.datasource.SourceModifier;
import org.fastcatsearch.ir.common.IRException;
import org.fastcatsearch.ir.document.Document;
import org.fastcatsearch.ir.settings.Schema;



public class MultiSourceReader extends SourceReader{
	
	private int pos;
	private SourceReader sourceReader;
	
	private List<SourceReader> sourceReaderList;
	private boolean isFull;
	
	public MultiSourceReader(Schema schema, List<SourceReader> sourceReaderList, SourceModifier sourceModifier, boolean isFull) throws IRException {
		super(schema, sourceModifier);
		this.sourceReaderList = sourceReaderList;
		this.isFull = isFull;
		//제일 첫 소스리더를 로딩한다.
		sourceReader = sourceReaderList.get(pos);
//		if(!createSourceReader(schema, settingList.get(pos), isFull)){
//			throw new IRException("소스리더 생성중 에러발생!");
//		}
		pos++;
	}

//	protected boolean createSourceReader(Schema schema, DataSourceSetting dsSetting, boolean isFull) throws IRException{
//		String sourceType = dsSetting.sourceType;
//		
//		if(sourceType.equalsIgnoreCase("FILE")){
//			sourceReader = DynamicClassLoader.loadObject(dsSetting.fileDocParser, SourceReader.class, new Class[]{Schema.class, DataSourceSetting.class, Boolean.class}, new Object[]{schema, dsSetting, isFull});
//			logger.debug("Loading sourceReader : {}, {}", dsSetting.fileDocParser, sourceReader);
//			if(sourceReader == null){
//				logger.error("소스리더를 로드하지 못했습니다. 해당 클래스가 클래스패스에 없거나 생성자 시그너처가 일치하는지 확인이 필요합니다. sourceType={}", sourceType);
//				return false;
//			}
//		}else if(sourceType.equalsIgnoreCase("DB")){
//			sourceReader = new DBReader(schema, dsSetting, isFull);
//			return true;
//		}else if(sourceType.equalsIgnoreCase("WEB")){
//			//웹페이지 리더
//			sourceReader = new WebPageSourceReader(schema, dsSetting, isFull);
//			return true;
//		}else if(sourceType.equalsIgnoreCase("CUSTOM")){
//			sourceReader = DynamicClassLoader.loadObject(dsSetting.customReaderClass, SourceReader.class, new Class[]{Schema.class, DataSourceSetting.class, Boolean.class, Properties.class}, new Object[]{schema, dsSetting, isFull});
//			logger.debug("Loading sourceReader : {}, {}", dsSetting.customReaderClass, sourceReader);
//			if(sourceReader == null){
//				logger.error("소스리더를 로드하지 못했습니다. 해당 클래스가 클래스패스에 없거나 생성자 시그너처가 일치하는지 확인이 필요합니다. sourceType={}", sourceType);
//				return false;
//			}
//		}else{
//			EventDBLogger.error(EventDBLogger.CATE_INDEX, "수집대상 소스타입을 알수 없습니다.sourceType={}", sourceType);
//		}
//		return false;
//	}
	
	@Override
	public boolean hasNext() throws IRException {
		if(sourceReader.hasNext()){
			return true;
		}else{
			if(pos < sourceReaderList.size()){
				//먼저 기존의 reader를 닫는다.
				sourceReader.close();
				//다른 reader를 생성한다.
//				if(!createSourceReader(schema, sourceReaderList.get(pos), isFull)){
//					return false;
//				}
				sourceReader = sourceReaderList.get(pos);
				pos++;
				if(sourceReader.hasNext()){
					return true;
				}else{
					while (!sourceReader.hasNext()) {
						if(pos < sourceReaderList.size()){
							//먼저 기존의 reader를 닫는다.
							sourceReader.close();
							//다른 reader를 생성한다.
//							if(!createSourceReader(schema, settingList.get(pos), isFull)){
//								return false;
//							}
							sourceReader = sourceReaderList.get(pos);
							pos++;
						}else{
							return false;
						}
					}
					return true;
				}
			}else{
				//더이상의 다른 reader가 없다.
				return false;
			}
		}
	}

	@Override
	public Document next() throws IRException {
		return sourceReader.next();
	}

	@Override
	public void close() throws IRException {
		sourceReader.close();
	}
	
	
}
