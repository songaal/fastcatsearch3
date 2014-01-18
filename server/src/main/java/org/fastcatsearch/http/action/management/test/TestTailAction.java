package org.fastcatsearch.http.action.management.test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.Date;

import org.fastcatsearch.http.ActionMapping;
import org.fastcatsearch.http.action.ActionRequest;
import org.fastcatsearch.http.action.ActionResponse;
import org.fastcatsearch.http.action.AuthAction;
import org.fastcatsearch.http.action.StreamWriter;

/*
 * */
@ActionMapping("/management/test/tail")
public class TestTailAction extends AuthAction {

	@Override
	public void doAuthAction(ActionRequest request, ActionResponse response) throws Exception {
		String filepath = request.getParameter("filepath");
		File f = new File(filepath);
		logger.debug("Tail file = {}", f.getAbsolutePath());
		
		if (!f.exists()) {
			return;
		}
		long fileSize = f.length();
		Reader fileReader = new FileReader(f);
		fileReader.skip(fileSize);
		
		writeHeader(response);
		StreamWriter streamWriter = response.getStreamWriter();
		BufferedReader input = new BufferedReader(fileReader);
		try {
			streamWriter.writeHeader(response);

			String line = null;
			while (true) {
				if ((line = input.readLine()) != null) {
					byte[] data = (line + "\n").getBytes("utf-8");
					streamWriter.write(data, 0, data.length);
				}else{
					try {
						Thread.sleep(1000L);
					} catch (InterruptedException x) {
						Thread.currentThread().interrupt();
						break;
					}
				}
			}
		} catch (IOException e) {
			logger.error("TestTailAction 에러발생. ", e.getMessage());
			return;
		} finally {
			if(input != null){
				input.close();
			}
			if (streamWriter != null) {
				streamWriter.close();
			}
		}

		logger.debug("TestTailAction Finished!");
	}

}
