package org.fastcatsearch.ir.search.posting;

import java.util.List;

import org.fastcatsearch.ir.search.TermDoc;
import org.fastcatsearch.ir.search.TermDocCollector;

public abstract class NodeReader {
	// 다음 문서번호.
	public abstract int next();

	// 채워준다.
//	public abstract void fill(List<TermDoc> totalTermDocList);
	public abstract void fill(TermDocCollector termDocCollector);
	
}
