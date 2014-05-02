package org.fastcatsearch.job.management;

import java.io.IOException;

import javax.xml.bind.JAXBException;

import org.fastcatsearch.common.io.Streamable;
import org.fastcatsearch.exception.FastcatSearchException;
import org.fastcatsearch.ir.IRService;
import org.fastcatsearch.ir.config.JDBCSourceConfig;
import org.fastcatsearch.ir.io.DataInput;
import org.fastcatsearch.ir.io.DataOutput;
import org.fastcatsearch.job.Job;
import org.fastcatsearch.service.ServiceManager;
import org.fastcatsearch.util.JAXBConfigs;

/**
 * 현 노드의 셋팅파일을 타 노드에 복사할때 사용한다. 
 * */
public class SyncJDBCSettingFileObjectJob extends Job implements Streamable {

	protected static final long serialVersionUID = -6707105484687769581L;
	protected Object jaxbConfig;
	
	public SyncJDBCSettingFileObjectJob() {}
	
	public SyncJDBCSettingFileObjectJob(Object jaxbConfig) {
		this.jaxbConfig = jaxbConfig;
	}

	@Override
	public void readFrom(DataInput input) throws IOException {
		try {
			jaxbConfig = JAXBConfigs.readFrom(input, JDBCSourceConfig.class);
		} catch (JAXBException e) {
			throw new IOException(e);
		}
	}

	@Override
	public void writeTo(DataOutput output) throws IOException {
		try {
			JAXBConfigs.writeTo(output, jaxbConfig, JDBCSourceConfig.class);
		} catch (JAXBException e) {
			throw new IOException(e);
		}
	}
	
	@Override
	public JobResult doRun() throws FastcatSearchException {
		
		IRService irService = ServiceManager.getInstance().getService(IRService.class);
		if(jaxbConfig instanceof JDBCSourceConfig) {
			try {
				//셋팅 업데이트 및 저장.
				irService.updateJDBCSourceConfig((JDBCSourceConfig) jaxbConfig);
				return new JobResult(true);
			} catch (JAXBException e) {
				logger.error("", e);
			}
		}
		return new JobResult(false);
	}


	
	
}

