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
	
	public static final String realTimeFileName = "realTimePopularKeyword";
	public static final String dailyFileName = "dailyPopularKeyword";
	public static final String weeklyFileName = "weeklyPopularKeyword";
	public static final String monthlyFileName = "monthlyPopularKeyword";
	public static final String yearlyFileName = "yearlyPopularKeyword";
	
	private Date createTime;
	private List<PopularKeywordVO> keywordList;
	
	public PopularKeywordDictionary(){
		keywordList = new ArrayList<PopularKeywordVO>(0);
		this.createTime = new Date();
	}
	
	public PopularKeywordDictionary(List<PopularKeywordVO> keywordList){
		this.keywordList = keywordList;
		this.createTime = new Date();
	}
	
	public PopularKeywordDictionary(File dictionaryFile) throws IOException {
		if (!dictionaryFile.exists()) {
			keywordList = new ArrayList<PopularKeywordVO>();
			throw new IOException("dictionary file not found: " + dictionaryFile.getAbsolutePath());
		}
		InputStream istream = null;
		try {
			istream = new FileInputStream(dictionaryFile);
			readFrom(istream);
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
		this.createTime = new Date(input.readLong());
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
		output.writeLong(createTime.getTime());
		int size = keywordList.size();
		output.writeVInt(size);
		
		if(size > 0){
			for(PopularKeywordVO vo : keywordList){
				vo.writeTo(output);
			}
		}
	}
	
	
}
