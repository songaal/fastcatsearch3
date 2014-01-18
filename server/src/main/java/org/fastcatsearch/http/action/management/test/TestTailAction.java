package org.fastcatsearch.http.action.management.test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;

import org.fastcatsearch.http.ActionMapping;
import org.fastcatsearch.http.action.ActionRequest;
import org.fastcatsearch.http.action.ActionResponse;
import org.fastcatsearch.http.action.AuthAction;
import org.fastcatsearch.http.action.StreamWriter;
import org.fastcatsearch.ir.util.Formatter;

/*
 * */
@ActionMapping("/management/test/tail")
public class TestTailAction extends AuthAction {

	static String encoding = "utf-8";

	@Override
	public void doAuthAction(ActionRequest request, ActionResponse response) throws Exception {
		String filepath = request.getParameter("filepath");
		int defaultSize = request.getIntParameter("defaultSize", 1024);
		String type = request.getParameter("type", "text");
		String linefeed = null;
		if (type.equalsIgnoreCase("text")) {
			linefeed = "\n";
		} else if (type.equalsIgnoreCase("html")) {
			linefeed = "<br>";

		}
		File f = new File(filepath);
		logger.debug("Tail file = {}", f.getAbsolutePath());

		if (!f.exists()) {
			return;
		}
		long skipSize = f.length();
		Reader fileReader = new FileReader(f);
		if(skipSize > defaultSize){
			skipSize -= defaultSize;
		}
		fileReader.skip(skipSize);

		writeHeader(response);
		StreamWriter streamWriter = response.getStreamWriter();
		BufferedReader input = new BufferedReader(fileReader, 1024);
		try {
			streamWriter.writeHeader(response);

			printTailInfoHeader(streamWriter, f);
			String line = null;
			while (true) {
				if ((line = input.readLine()) != null) {
					byte[] data = (line + linefeed).getBytes(encoding);
					streamWriter.write(data, 0, data.length);
				} else {
					try {
						Thread.sleep(500L);
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
			if (input != null) {
				input.close();
			}
			if (streamWriter != null) {
				streamWriter.close();
			}
		}

		logger.debug("TestTailAction Finished!");
	}

	private void printTailInfoHeader(StreamWriter streamWriter, File f) throws IOException {
		String header = "####################################################\n" 
					+ "# Tail file = " + f.getName() + " (" + Formatter.getFormatSize(f.length()) + ")\n"
				+ "####################################################\n";
		byte[] str = header.getBytes(encoding);
		streamWriter.write(str, 0, str.length);
	}

}
