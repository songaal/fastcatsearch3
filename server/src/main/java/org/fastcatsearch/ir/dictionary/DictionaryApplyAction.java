package org.fastcatsearch.ir.dictionary;

import org.fastcatsearch.http.action.service.CallableAction;
import org.fastcatsearch.job.DictionaryCompileApplyJob;
import org.fastcatsearch.job.Job;
import org.fastcatsearch.util.ResultWriter;

public class DictionaryApplyAction extends CallableAction {

	public DictionaryApplyAction(String type) {
		super(type);
	}

	@Override
	protected Job createJob() {
		return new DictionaryCompileApplyJob();
	}

	@Override
	protected void writeResult(ResultWriter resultWriter, Object result) throws Exception {
		resultWriter.object();
		resultWriter.key("id").value("songaal").key("pass").value("1111");
		resultWriter.endObject();
		
	}

}
