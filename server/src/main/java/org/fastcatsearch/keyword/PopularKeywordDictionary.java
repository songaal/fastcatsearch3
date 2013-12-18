package org.fastcatsearch.keyword;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.lucene.store.InputStreamDataInput;
import org.apache.lucene.store.OutputStreamDataOutput;
import org.fastcatsearch.db.vo.PopularKeywordVO;
import org.fastcatsearch.ir.io.DataInput;
import org.fastcatsearch.ir.io.DataOutput;

public class PopularKeywordDictionary implements KeywordDictionary {
	
	public static final String realTimeFileName = "realTime.dict";
	public static final String lastDayFileName = "lastDay.dict";
	public static final String lastWeekFileName = "lastWeek.dict";
	
	private Date createTime;
	private List<PopularKeywordVO> keywordList;
	
	public PopularKeywordDictionary(){
		keywordList = new ArrayList<PopularKeywordVO>(0);
	}
	
	public PopularKeywordDictionary(List<PopularKeywordVO> keywordList){
		this.keywordList = keywordList;
		this.createTime = new Date();
	}
	
	public PopularKeywordDictionary(File dictionaryFile) {
		
		if (!dictionaryFile.exists()) {
			keywordList = new ArrayList<PopularKeywordVO>();
			logger.error("사전파일이 존재하지 않습니다. file={}", dictionaryFile.getAbsolutePath());
			return;
		}
		
		InputStream istream = null;
		
		try {
			istream = new FileInputStream(dictionaryFile);
			readFrom(istream);
		} catch (IOException e) {
			logger.error("",e);
		} finally {
			if (istream != null) try {
				istream.close();
			} catch (IOException e) { }
		}
	}

	public Date getCreateTime() {
		return createTime;
	}

	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}

	public List<PopularKeywordVO> getKeywordList() {
		return keywordList;
	}
	public void setKeywordList(List<PopularKeywordVO> keywordList) {
		this.keywordList = keywordList;
	}

	@Override
	public void readFrom(InputStream in) throws IOException {
		DataInput input = new InputStreamDataInput(in);

		keywordList = new ArrayList<PopularKeywordVO>();

		int size = input.readVInt();
		for (int i = 0; i < size; i++) {
			PopularKeywordVO vo = new PopularKeywordVO();
			vo.readFrom(input);
			keywordList.add(vo);
		}

	}

	@Override
	public void writeTo(OutputStream out) throws IOException {
		DataOutput output = new OutputStreamDataOutput(out);
		int size = keywordList.size();
		output.writeVInt(size);
		
		if(size > 0){
			for(PopularKeywordVO vo : keywordList){
				vo.writeTo(output);
			}
		}
	}
	
	
}
