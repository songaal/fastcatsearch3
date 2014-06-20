package org.fastcatsearch.common;

import java.io.IOException;
import java.util.List;

import org.fastcatsearch.common.SMSSender;
import org.fastcatsearch.common.ShellExecutor;
import org.fastcatsearch.common.ShellExecutor.ShellResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 리눅스 sendmail를 wrapping해서 사용하는 클래스.
 * */
public class Sendmail {
	private static Logger logger = LoggerFactory.getLogger(SMSSender.class);
	private String sendmailExecPath;
	
	public Sendmail(String sendmailExecPath) {
		this.sendmailExecPath = sendmailExecPath;
	}

	public void sendText(String fromAddress, List<String> recipientToList, String subject, String text) throws IOException {
		ShellExecutor executor = new ShellExecutor();
		String[] cmdarray = new String[recipientToList.size() + 1];
		cmdarray[0] = sendmailExecPath;
		String recipientString = "";
		
		
		for (int i = 0; i < recipientToList.size(); i++) {
			cmdarray[i + 1] = recipientToList.get(i);
			recipientString += cmdarray[i + 1];
			if (i < recipientToList.size() - 1) {
				recipientString += ",";
			}
		}
		
		ShellResult shellResult = executor.exec(cmdarray);
		shellResult.println("To: " + recipientString, "utf-8");
		shellResult.println("From: " + fromAddress, "utf-8");
		shellResult.println("Subject: " + subject, "utf-8");
		shellResult.println(text, "utf-8");
		shellResult.println(".", "utf-8");
		shellResult.waitFor();
		logger.debug("{}", shellResult);
	}

}
