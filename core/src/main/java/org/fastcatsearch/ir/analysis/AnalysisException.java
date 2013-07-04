package org.fastcatsearch.ir.analysis;

import java.io.IOException;

public class AnalysisException extends Exception {

	public AnalysisException(IOException e) {
		super(e);
	}

	public AnalysisException(String message) {
		super(message);
	}

}
