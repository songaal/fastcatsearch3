package org.fastcatsearch.statistics.util;

import org.fastcatsearch.statistics.AbstractLog;

/**
 * Raw 로그파일을 한줄씩 해석하여 읽어들이는 reader.
 * */
public abstract class LogReader<LogType extends AbstractLog> {

	public abstract LogType readLine(String line);

}
