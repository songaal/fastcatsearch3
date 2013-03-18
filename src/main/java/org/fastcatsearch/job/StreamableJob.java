package org.fastcatsearch.job;

import org.fastcatsearch.common.io.Streamable;
import org.fastcatsearch.control.JobException;
import org.fastcatsearch.service.ServiceException;

public abstract class StreamableJob extends Job implements Streamable{
	@Override
	public abstract Streamable run0() throws JobException, ServiceException;

}
