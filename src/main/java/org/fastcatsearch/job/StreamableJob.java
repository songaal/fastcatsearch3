package org.fastcatsearch.job;

import org.fastcatsearch.common.io.Streamable;

import org.fastcatsearch.exception.FastcatSearchException;

public abstract class StreamableJob extends Job implements Streamable {

	private static final long serialVersionUID = -799321199758563930L;

	@Override
	public abstract JobResult doRun() throws FastcatSearchException;

}
