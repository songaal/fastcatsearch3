package org.fastcatsearch.common;

import static org.junit.Assert.*;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Date;

import org.fastcatsearch.common.ShellExecutor.ShellResult;
import org.junit.Test;

public class ShellExecutorTest {

	@Test
	public void test() {
		ShellExecutor executor = new ShellExecutor();
		String[] cmdarray = new String[]{"ls", "-al"};
		ShellResult shellResult = executor.exec(cmdarray);
		shellResult.waitFor();
		System.out.println(shellResult);
	}
	
	@Test
	public void testSendmail() throws IOException {
		ShellExecutor executor = new ShellExecutor();
		String[] cmdarray = new String[]{"sendmail", "swsong@websqrd.com", "songaal@naver.com"};
		ShellResult shellResult = executor.exec(cmdarray);
		shellResult.println("To: swsong@websqrd.com, songaal@naver.com", "utf-8");
		shellResult.println("From: iPhone5s", "utf-8");
		shellResult.println("Subject: Test mail sendmail " + new Date(), "utf-8");
		shellResult.println("is it ok?\n한글도 오키?", "utf-8");
		shellResult.println(".", "utf-8");
		shellResult.waitFor();
		System.out.println(shellResult);
	}


}
