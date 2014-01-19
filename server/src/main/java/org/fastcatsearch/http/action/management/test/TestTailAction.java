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
 * 파일을 지속적으로 read line하여 전달해주는 action.
 * tail를 system.log파일에 걸 경우, 이 action에서 system.log에 기록을 하는 코드가 있으면 무한루프에 빠지기 때문에 여기서는 로거를 사용하지 않는다.
 * 
 * tail할 파일의 데이터가 작을 경우 http write시 버퍼크기에 미치지 못해 client쪽에서 데이터가 안보일수 있지만, 차후 로그파일에 몇라인이 기록되면 그때 보인다. 
 * 이 액션만 http response에 버퍼를 사용하지 않도록 하면 통일성이 깨지므로, 이 현상은 그대로 놔둔다.   
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
		System.out.println("TestTailAction "+this+" Started!");
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
					System.out.println("TestTailAction "+this+" "+line);
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
			System.err.println("TestTailAction 에러발생 "+this);
			return;
		} finally {
			if (input != null) {
				input.close();
			}
			if (streamWriter != null) {
				streamWriter.close();
			}
		}

		System.out.println("TestTailAction "+this+" Finished!");
	}

	private void printTailInfoHeader(StreamWriter streamWriter, File f) throws IOException {
		String header = "####################################################\n" 
					+ "# Tail file = " + f.getName() + " (" + Formatter.getFormatSize(f.length()) + ")\n"
				+ "####################################################\n";
		byte[] str = header.getBytes(encoding);
		streamWriter.write(str, 0, str.length);
	}

}
