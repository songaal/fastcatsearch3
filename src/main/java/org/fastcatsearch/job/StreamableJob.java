package org.fastcatsearch.job;

import org.fastcatsearch.common.io.Streamable;
import org.fastcatsearch.control.JobException;
import org.fastcatsearch.service.ServiceException;

public abstract class StreamableJob extends Job implements Streamable{

	private static final long serialVersionUID = -799321199758563930L;

	@Override
	public abstract JobResult run0() throws JobException, ServiceException;
	
}
