package org.fastcatsearch.db.vo;

import java.util.Date;

public class KeywordHitVO {
	public int id;
	public String keyword;
	public int hit;
	public int popular;
	public int prevRank;
	public boolean isUsed;
	public Date dateRegister;
	public Date dateUpdate;
}
