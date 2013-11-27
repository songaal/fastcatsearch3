package org.fastcatsearch.processlogger;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import org.fastcatsearch.common.io.Streamable;
import org.fastcatsearch.exception.FastcatSearchException;
import org.fastcatsearch.ir.io.DataInput;
import org.fastcatsearch.ir.io.DataOutput;
import org.fastcatsearch.job.Job;
import org.fastcatsearch.processlogger.log.ProcessLog;
import org.fastcatsearch.service.ServiceManager;

public class ProcessLoggerJob extends Job implements Streamable {

	private static final long serialVersionUID = -1455315501463956117L;
	private Class<? extends ProcessLogger> processLoggerClasss;
	private ProcessLog processLog;
	
	public ProcessLoggerJob(){
	}
	
	public ProcessLoggerJob(Class<? extends ProcessLogger> processLoggerClasss, ProcessLog processLog) {
		this.processLoggerClasss = processLoggerClasss;
		this.processLog = processLog;
	}

	public ProcessLog getProcessLog(){
		return processLog;
	}
	@Override
	public JobResult doRun() throws FastcatSearchException {
		logger.debug(">>WRITE ProcessLog >> {}", processLog);
		ProcessLogger processLogger = ServiceManager.getInstance().getService(ProcessLoggerService.class).getProcessLogger(processLoggerClasss);
		processLogger.log(processLog);
		return new JobResult();
	}

	@Override
	public void readFrom(DataInput input) throws IOException {
		Class<? extends ProcessLog> processLogClass = null;
		ObjectInputStream ois = new ObjectInputStream(input);
		try {
			processLoggerClasss = (Class<? extends ProcessLogger>) ois.readObject();
			processLogClass  = (Class<? extends ProcessLog>) ois.readObject();
			processLog = processLogClass.newInstance();
		} catch (Exception e) {
			throw new IOException(e);
		}
		processLog.readFrom(input);
	}

	@Override
	public void writeTo(DataOutput output) throws IOException {
		ObjectOutputStream oos = new ObjectOutputStream(output);
		oos.writeObject(processLoggerClasss);
		oos.writeObject(processLog.getClass());
		processLog.writeTo(output);
		
		
		
	}

}
