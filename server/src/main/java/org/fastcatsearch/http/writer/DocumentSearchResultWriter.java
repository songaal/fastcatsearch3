package org.fastcatsearch.http.writer;

import java.io.IOException;
import java.io.Writer;

import org.fastcatsearch.ir.query.Result;
import org.fastcatsearch.ir.query.Row;
import org.fastcatsearch.ir.util.Formatter;
import org.fastcatsearch.util.ResponseWriter;
import org.fastcatsearch.util.ResultWriterException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 검색결과를 response stream에 기록하는 클래스.
 * */
public  class DocumentSearchResultWriter extends AbstractDocumentSearchResultWriter {
	protected static Logger logger = LoggerFactory.getLogger(DocumentSearchResultWriter.class);

	private boolean isAdmin;
	private String[] fieldNames = null;

	public DocumentSearchResultWriter(Writer writer, boolean isAdmin) {
		super(writer);
		this.isAdmin = isAdmin;
	}

	@Override
	public void writeResult(Object obj, ResponseWriter rStringer, long searchTime, boolean isSuccess) throws ResultWriterException, IOException {
		Result result = null;
		if (!isSuccess) {
			String errorMsg = null;
			if (obj == null)
				errorMsg = "null";
			else
				errorMsg = obj.toString();

			rStringer.object().key("status").value(1).key("time").value(Formatter.getFormatTime(searchTime)).key("total_count").value(0).key("error_msg").value(errorMsg).endObject();
		} else {
			result = (Result) obj;
			fieldNames = result.getFieldNameList();
			rStringer.object().key("status").value(0).key("time").value(Formatter.getFormatTime(searchTime)).key("total_count").value(result.getTotalCount()).key("count")
			                .value(result.getCount()).key("field_count").value(result.getFieldCount()).key("seg_count").value(result.getSegmentCount()).key("doc_count")
			                .value(result.getDocCount()).key("del_count").value(result.getDeletedDocCount()).key("fieldname_list").array("name");

			if (result.getCount() == 0  ) {
				rStringer.value("_no_");
			} else {
				rStringer.value("_no_");
				for (int i = 0; i < fieldNames.length; i++) {
					rStringer.object();					
					rStringer.key("name").value(fieldNames[i]);
					rStringer.endObject();
				}
			}
			rStringer.endArray();
			writeBody(result, rStringer, searchTime);
			rStringer.endObject();			
		}
		writer.write(rStringer.toString());
	}

	public void writeBody(Result result, ResponseWriter rStringer, long searchTime) throws ResultWriterException {
		
		rStringer.key("result");
		// data
		Row[] rows = result.getData();
		int start = result.getStart();

		if (rows.length == 0) {
			rStringer.array("item").object().key("_no_").value("No result found!").endObject().endArray();
		} else {
			rStringer.array("item");
			for (int i = 0; i < rows.length; i++) {
				Row row = rows[i];

				rStringer.object().key("_no_").value(start + i);

				for (int k = 0; k < fieldNames.length; k++) {
					char[] f = row.get(k);
					String fdata = new String(f).trim();
					rStringer.key(fieldNames[k]).value(fdata);				
				}
				if ( row.isDeleted() )
					rStringer.key("_delete_").value("true");
				else
					rStringer.key("_delete_").value("false");
				rStringer.endObject();
			}
			rStringer.endArray();
		}
	}
}
