package org.fastcatsearch.ir.search;

import java.io.File;
import java.util.Enumeration;

import org.fastcatsearch.ir.document.Document;

public class SegmentDocumentEnumeration implements Enumeration<Document> {
	
	public SegmentDocumentEnumeration(File segmentHome){
	}
	
	@Override
	public boolean hasMoreElements() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Document nextElement() {
		// TODO Auto-generated method stub
		return null;
	}

}
