package org.fastcatsearch.db.vo;

import java.io.IOException;

import org.fastcatsearch.common.io.Streamable;
import org.fastcatsearch.ir.io.DataInput;
import org.fastcatsearch.ir.io.DataOutput;


public class PopularKeywordVO implements Streamable {
	public enum RankDiffType {UP, DN, NEW, EQ }

	private int id;
	private String category;
	private String time;
	private int rank;
	
	private String word;
	private int count;
	private RankDiffType rankDiffType;
	private int rankDiff;
	
	public PopularKeywordVO(){
	}
	
	public PopularKeywordVO(String category, String time, String word, int count, int rank, RankDiffType rankDiffType, int rankDiff) {
		this.category = category;
		this.time = time;
		this.word = word;
		this.count = count;
		this.rank = rank;
		this.rankDiffType = rankDiffType;
		this.rankDiff = rankDiff;
	}
	
	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}
	
	public String getCategory() {
		return category;
	}
	public void setCategory(String category) {
		this.category = category;
	}
	public String getTime() {
		return time;
	}
	public void setTime(String time) {
		this.time = time;
	}
	public String getWord() {
		return word;
	}
	public void setWord(String word) {
		this.word = word;
	}
	public int getCount() {
		return count;
	}
	public void setCount(int count) {
		this.count = count;
	}
	public int getRank() {
		return rank;
	}
	public void setRank(int rank) {
		this.rank = rank;
	}
	public RankDiffType getRankDiffType() {
		return rankDiffType;
	}
	public void setRankDiffType(RankDiffType rankDiffType) {
		this.rankDiffType = rankDiffType;
	}
	public int getRankDiff() {
		return rankDiff;
	}
	public void setRankDiff(int rankDiff) {
		this.rankDiff = rankDiff;
	}

	@Override
	public void readFrom(DataInput input) throws IOException {
		this.category = input.readString();
		this.time = input.readString();
		this.word = input.readString();
		this.count = input.readInt();
		this.rank = input.readInt();
		this.rankDiffType = RankDiffType.valueOf(input.readString());
		this.rankDiff =  input.readInt();
	}

	@Override
	public void writeTo(DataOutput output) throws IOException {
		output.writeString(category);
		output.writeString(time);
		output.writeString(word);
		output.writeInt(count);
		output.writeInt(rank);
		output.writeString(rankDiffType.name());
		output.writeInt(rankDiff);
	}
	
}
