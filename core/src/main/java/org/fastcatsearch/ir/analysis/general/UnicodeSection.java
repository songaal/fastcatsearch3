package org.fastcatsearch.ir.analysis.general;

public class UnicodeSection {
	public UnicodeSection(int tSym, int iStart, int iEnd) {
		set(tSym, iStart, iEnd);
	}
	
	public void set(int sym, int iStart, int iEnd)
	{
		code = sym;
		start = iStart;
		end = iEnd;
	}
	
	public int code;
	public int start;
	public int end;
}
