/*
 * Copyright 2013 Websquared, Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.fastcatsearch.ir.util;

import org.apache.lucene.util.BytesRef;
import org.fastcatsearch.ir.settings.FieldSetting.Type;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Pattern;

/**
 * 객체 / 문자열의 상호변환 등을 위한 유틸
 *
 */
public class Formatter {
	
	private static final Logger logger = LoggerFactory.getLogger(Formatter.class);

//	public static final SimpleDateFormat DATEFORMAT_DEFAULT_PARSE = new SimpleDateFormat("yyyyMMddHHmmssS");
//	public static final SimpleDateFormat DATEFORMAT_DEFAULT_FORMAT = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss");
//	public static final SimpleDateFormat DATEFORMAT_OUTPUT_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.S");
//	public static final SimpleDateFormat DATEFORMAT_DEFAULT_FORMAT_MIN = new SimpleDateFormat("yyyy.MM.dd HH:mm");

	/** 파싱을 위한 날자포맷.**/
	private static final ThreadLocal<SimpleDateFormat> DATEFORMAT_DEFAULT_PARSE = new ThreadLocal<SimpleDateFormat>(){
		@Override
		protected SimpleDateFormat initialValue() {
			return new SimpleDateFormat("yyyyMMddHHmmssS");
		}
	};

	/** 포맷팅을 위한 날자포맷. **/
	private static final ThreadLocal<SimpleDateFormat> DATEFORMAT_DEFAULT_FORMAT = new ThreadLocal<SimpleDateFormat>(){
		@Override
		protected SimpleDateFormat initialValue() {
			return new SimpleDateFormat("yyyy.MM.dd HH:mm:ss");
		}
	};
	private static final ThreadLocal<SimpleDateFormat> DATEFORMAT_OUTPUT_FORMAT = new ThreadLocal<SimpleDateFormat>(){
		@Override
		protected SimpleDateFormat initialValue() {
			return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.S");
		}
	};
	private static final ThreadLocal<SimpleDateFormat> DATEFORMAT_DEFAULT_FORMAT_MIN = new ThreadLocal<SimpleDateFormat>(){
		@Override
		protected SimpleDateFormat initialValue() {
			return new SimpleDateFormat("yyyy.MM.dd HH:mm");
		}
	};

	/** 날자 중의 특수기호들을 삭제하기 위한 패턴 **/
	public static final Pattern PTN_STRIP_DATE = Pattern.compile("[-\t\0\r\n :.,/]");
//	public static final Pattern PTN_PARSE_DATE = Pattern.compile("[0-9]{4}+[-\t :.,/][0-9]{1-2}+[-\t :.,/][0-9]{1-2}+[-\t :.,/][0-9]{1-2}");

	/**
	 * 문자를 파싱하여 Date 객체로 반환한다.
	 * 반드시 년도(4자리) 월(2자리) 일(2자리) 시간(2자리) 분(2자리) 초(자리) 밀리초(3자리) 순으로 들어와야 하며
	 * 앞에서부터 순서대로라면 일부만 들어와도 파싱을 시도한다. (예를들면 20121010 등)
	 * @param data
	 * @return
	 * @throws ParseException
	 */
	public static Date parseDate(String data, Date defaultValue) {
		try {
			return parseDate(data);
		} catch (Exception e) {
			logger.error("ERROR PARSING DATE STRING \"{}\" / {}", data, e.getMessage());
		}
		return defaultValue;
	}
	public static Date parseDate(String data) throws ParseException {
		if (data == null) {
			return null;
		}
		data=PTN_STRIP_DATE.matcher(data).replaceAll("");//.trim();
		//일부만 들어와도 되도록 자리맞춤을 위한 0 채움
		for(int strlen=data.length(); strlen < 17; strlen++) { data+="0"; }
		Date ret = null;
		try {
			ret = DATEFORMAT_DEFAULT_PARSE.get().parse(data);
		} catch (Exception e) {
			logger.error("ERROR PARSING DATE STRING \"{}\" / {}", data, e.getMessage()); 
		}
		return ret;
	}
	
	/**
	 * 날자를 문자로 변환하여 반환한다.
	 * 형식은 yyyy-MM-dd HH:mm:ss.S
	 * @return
	 */
	public static String formatDate() {
		return formatDate(new Date());
	}
	public static String formatDate(Date date) {
		return DATEFORMAT_DEFAULT_FORMAT.get().format(date);
	}
	public static String formatDateEndsMinute(Date date) {
		return DATEFORMAT_DEFAULT_FORMAT_MIN.get().format(date);
	}
	
	private static String CONTROL_CHAR_REGEXP = "["+(char)0+" "+(char)1+" "+(char)2+" "+(char)3+" "+(char)4+" "+(char)5+" "
	+(char)6+" "+(char)7+" "+(char)8+" "+(char)11+" "+(char)12+" "+(char)14+" "+(char)15+" "+(char)16+" "+(char)17+" "
	+(char)18+" "+(char)19+" "+(char)20+" "+(char)21+" "+(char)22+" "+(char)23+" "+(char)24+" "+(char)25+" "+(char)26+" "
	+(char)27+" "+(char)28+" "+(char)29+" "+(char)30+" "+(char)31+"]";
	
	public static String getFormatTime(long t){
		if(t > 1000){
			float a = (float) (t / 1000);
			if(a > 60){
				float b = a / 60;
				
				if(b > 60){
					float c = b / 60;
					return String.format("%.1f h", c);
				}else{
					return String.format("%.1f m", b);
				}
				
			}else{
				return String.format("%.1f s", a);
			}
		}else{
			return t +" ms";
		}
	}
	
	public static String getFormatSize(long s){
		 
		if(s > 1024){
			float a = (float) (s / 1024);
			
			if(a > 1024){
				float b = a / 1024;
				
				if(b > 1024){
					float c = b / 1024;
					return String.format("%.1f GB", c);
				}else{
					return String.format("%.1f MB", b);
				}
				
			}else{
				return String.format("%.1f KB", a);
			}
		}else{
			return s +" B";
		}
	}
	
	public static String removeControlChars(String value){
		return value.replaceAll(CONTROL_CHAR_REGEXP, " ");
	}
	
	public static String getContentString(BytesRef ref, Type type) {
		String contentString = null;	
		if(type == Type.ASTRING) {
			contentString = ref.toAlphaString().trim();
		} else if(type == Type.STRING) {
			contentString = new String(ref.toUCharArray()).trim();
		} else if(type == Type.DATETIME) {
			contentString = String.valueOf(DATEFORMAT_OUTPUT_FORMAT.get().format(new Date(ref.toLongValue())));
		} else if(type == Type.DOUBLE) {
			contentString = String.valueOf(Double.longBitsToDouble(ref.toLongValue())).trim();
		} else if(type == Type.FLOAT) {
			contentString = String.valueOf(Float.intBitsToFloat(ref.toIntValue())).trim();
		} else if(type == Type.LONG) {
			contentString = String.valueOf(ref.toLongValue()).trim();
		} else if(type == Type.INT) {
			contentString = String.valueOf(ref.toIntValue()).trim();
		}
		
		if(contentString!=null) {
			contentString = contentString.trim();
		}
		return contentString;
	}
	
	public static void main(String[] arg) throws Exception {
		
		System.out.println(""+Formatter.parseDate("20150420"));
		
	}
}
