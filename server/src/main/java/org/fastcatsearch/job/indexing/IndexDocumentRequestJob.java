package org.fastcatsearch.job.indexing;

import org.fastcatsearch.common.io.Streamable;
import org.fastcatsearch.exception.FastcatSearchException;
import org.fastcatsearch.http.action.service.indexing.IndexDocumentsAction;
import org.fastcatsearch.http.action.service.indexing.JSONRequestReader;
import org.fastcatsearch.ir.DynamicIndexModule;
import org.fastcatsearch.ir.IRService;
import org.fastcatsearch.ir.io.DataInput;
import org.fastcatsearch.ir.io.DataOutput;
import org.fastcatsearch.job.Job;
import org.fastcatsearch.service.ServiceManager;

import java.io.IOException;
import java.util.List;

/**
 * 색인노드의 dynamicIndexModule에 문서를 기록한다.
 * 차후 indexModule에서 파일로그를 각 데이터노드에 색인요청으로 전달하게 된다.
 * */
public class IndexDocumentRequestJob extends Job implements Streamable {
	private static final long serialVersionUID = -9030366773507675894L;

	@Override
	public JobResult doRun() throws FastcatSearchException {
		String collectionId = getStringArgs(0);
        String type = getStringArgs(1);
        String documents = getStringArgs(2);

		try {
			IRService irService = ServiceManager.getInstance().getService(IRService.class);
            DynamicIndexModule dynamicIndexModule = irService.getDynamicIndexModule(collectionId);
            if (dynamicIndexModule == null) {
                throw new FastcatSearchException("Collection [" + collectionId + "] is not exist.");
            }
            List<String> jsonList = new JSONRequestReader().readJsonList(documents);
            if(type.equalsIgnoreCase(IndexDocumentsAction.INSERT_TYPE)) {
                dynamicIndexModule.insertDocument(jsonList);
            } else if(type.equalsIgnoreCase(IndexDocumentsAction.UPDATE_TYPE)) {
                dynamicIndexModule.updateDocument(jsonList);
            } else if(type.equalsIgnoreCase(IndexDocumentsAction.DELETE_TYPE)) {
                dynamicIndexModule.deleteDocument(jsonList);
            }
		} catch (Throwable e) {
            throw new FastcatSearchException(e);
		}

		return new JobResult(true);
	}

    @Override
    public void readFrom(DataInput input) throws IOException {
        String[] strings = new String[3];
        strings[0] = input.readString();
        strings[1] = input.readString();
        strings[2] = input.readString();
        args = strings;
    }

    @Override
    public void writeTo(DataOutput output) throws IOException {
        String[] strings = (String[]) args;
        output.writeString(strings[0]);
        output.writeString(strings[1]);
        output.writeString(strings[2]);
    }
}
