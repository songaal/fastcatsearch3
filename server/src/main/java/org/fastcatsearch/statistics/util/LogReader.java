package org.fastcatsearch.statistics.util;

import org.fastcatsearch.statistics.AbstractLog;

public abstract class LogReader<LogType extends AbstractLog> {

	public abstract LogType readLine(String line);

}
