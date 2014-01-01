package org.fastcatsearch.util;

import java.util.Calendar;

public class DateUtils {
	
	
	/**
	 * 현 시각을 기준으로 가장 가까운 이전 정각 시간을 구한다.
	 * */
	public static Calendar getLatestOnTimeSmallerThanNow(){
		Calendar calendar = Calendar.getInstance();
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.SECOND, 0);
		calendar.set(Calendar.MILLISECOND, 0);
		return calendar;
	}
	/**
	 * 현 시각을 기준으로 가장 가까운 다음 정각 시간을 구한다.
	 * */
	public static Calendar getLatestOnTimeLargerThanNow(){
		Calendar calendar = Calendar.getInstance();
		calendar.add(Calendar.HOUR_OF_DAY, 1);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.SECOND, 0);
		calendar.set(Calendar.MILLISECOND, 0);
		return calendar;
	}
	
	/**
	 * 현 시각을 기준으로 다음날 hourOfDay시를 구한다.
	 * */
	public static Calendar getNextDayHour(int hourOfDay){
		Calendar calendar = Calendar.getInstance();
		calendar.add(Calendar.DATE, 1);
		calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.SECOND, 0);
		calendar.set(Calendar.MILLISECOND, 0);
		return calendar;
	}
	
	/**
	 * 0분을 기준으로 minutes분 단위로 증가할때 현재보다 큰 다음 시각을 구한다. 
	 * */
	public static Calendar getLatestTimeLargerThanNow(int minutes){
		Calendar calendar = Calendar.getInstance();
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.SECOND, 0);
		calendar.set(Calendar.MILLISECOND, 0);
		
		Calendar now = Calendar.getInstance();
		while (calendar.before(now)){
			calendar.add(Calendar.MINUTE, minutes);
		}
		
		return calendar;
	}
	
	
	public static int getSecondsByMinutes(int minutes){
		return minutes * 60;
	}
	
	public static int getSecondsByHours(int hours){
		return hours * 60 * 60;
	}
	
	public static int getSecondsByDays(int days){
		return days * 24 * 60 * 60;
	}
	
}
