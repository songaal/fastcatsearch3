package org.fastcatsearch.statistics.util;

import org.fastcatsearch.statistics.log.AbstractLog;

/**
 * Raw 로그파일을 한줄씩 해석하여 읽어들이는 reader.
 * */
public abstract class LogParser<LogType extends AbstractLog> {

	public abstract LogType parseLine(String line);

}
