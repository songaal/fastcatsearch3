package org.fastcatsearch.db.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
/**
 * DB select 결과 ResultSet을 객체 결과로 변환시킨다.
 * */
public interface ResultVOMapper<T> {
	public T map(ResultSet resultSet) throws SQLException;
}
