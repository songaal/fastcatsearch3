package org.fastcatsearch.common;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.nio.charset.Charset;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ShellExecutor {
	private static Logger logger = LoggerFactory.getLogger(SMSSender.class);
	
	public ShellResult exec(String[] cmdarray) {
		return exec(cmdarray, null, null);
	}
	
	public ShellResult exec(String[] cmdarray, String[] envp) {
		return exec(cmdarray, envp, null);
	}
	
	
	public ShellResult exec(String[] cmdarray, String[] envp, File dir) {
		
		try {
			if(dir == null) {
				dir = new File(".").getAbsoluteFile().getParentFile();
			}
			Process process = Runtime.getRuntime().exec(cmdarray, envp, dir);
			ShellResult shellReturn = new ShellResult(process, cmdarray, dir);
			return shellReturn;
		} catch (IOException e) {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			PrintWriter s = new PrintWriter(baos);
			e.printStackTrace(s);
			s.flush();
			return new ShellResult(new String(baos.toByteArray()), cmdarray, dir);
		}
	}
	
	
	
	public static class ShellResult {
		private Process process;
		private String[] cmdarray;
		private File workDir;
		private int exitValue = -1;
		private String outputString;
		private String errorString;
		private OutputStream os;
		
		public ShellResult(String errorString, String[] cmdarray, File workDir){
			this.errorString = errorString;
			this.cmdarray = cmdarray;
			this.workDir = workDir;
		}
		
		public ShellResult(Process process, String[] cmdarray, File workDir) {
			this.process = process;
			this.cmdarray = cmdarray;
			this.workDir = workDir;
		}
		
		public int getExitValue() {
			return exitValue;
		}
		public String getOutputString() {
			return outputString;
		}
		public String getErrorString() {
			return errorString;
		}
		
		public void println(String str, String encoding) throws IOException {
			byte[] data = str.getBytes(Charset.forName(encoding));
			write(data, 0, data.length);
			os.write('\n');
		}
		public void write(byte[] data, int offset, int length) throws IOException {
			if(data != null) {
				
				if(os == null) {
					os = process.getOutputStream();
				}
				
				os.write(data, offset, length);
			}
		}
		
		public int waitFor() {
			if (os != null) {
				try {
					os.close();
				} catch (IOException e) {
					logger.error("", e);
				}
			}

			if(process != null) {
				outputString = inputStreamToString(process.getInputStream());
				errorString = inputStreamToString(process.getErrorStream());
				try {
					exitValue = process.waitFor();
				} catch (InterruptedException e) {
					// ignore
				}
			}
			return exitValue;
		}
		
		private String inputStreamToString(InputStream is) {
			if(is == null) {
				return null;
			}
			StringBuffer sb = new StringBuffer();
			BufferedReader br = new BufferedReader(new InputStreamReader(is));
			try {
				for (String rline = null; (rline = br.readLine()) != null;) {
					sb.append(rline).append("\n");
				}
			} catch (Exception e) {
				logger.error("", e);
			}

			return sb.toString();
		}
		
		@Override
		public String toString() {
			String value = "Shell> ";
			
			for(String cmd : cmdarray) {
				value += (cmd + " ");
			}
			value += ("\n[WorkDir] " + workDir.getAbsolutePath());
			value += ("\n[ExitCode] " + exitValue);
			if(outputString != null && outputString.length() > 0) {
				value += ("\n[Output]\n" + outputString);
			}
			
			if(errorString != null && errorString.length() > 0) {
				value += ("\n[Error]\n" + errorString);
			}
			return value;
		}
		
	}
}
