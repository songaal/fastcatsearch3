package org.fastcatsearch.common;

public class Strings {
	public static final String[] EMPTY_ARRAY = new String[0];
	
	public static String getHumanReadableTimeInterval(long timeInterval){
		
		int t = (int) timeInterval; 
		if(t < 1000){
			return t +"ms";
		}else{
			int s = t / 1000;
			if(s < 60){
				return String.format("%ds", s);
			}else{
				int m = s / 60;
				int s2 = s % 60;
				
				if(m < 60){
					return String.format("%dm %ds", m, s2);
				}else{
					int h = m / 60;
					int m2 = (m % 60);
					return String.format("%dh %dm %ds", h, m2, s2);
				}
			}
		}
		
	}
	
	public static String getHumanReadableByteSize(double byteSize){
		double unit = 1024;
		if(byteSize < unit){
			return String.format("%.0fb", byteSize);
		}else if(byteSize < unit * unit){
			return String.format("%.2fkb",(byteSize / unit));
		}else if(byteSize < unit * unit * unit){
			return String.format("%.2fmb",(byteSize / (unit * unit)));
		}else if(byteSize < unit * unit * unit * unit){
			return String.format("%.2fgb",(byteSize / (unit * unit * unit))); //기가
		}else if(byteSize < unit * unit * unit * unit * unit){
			return String.format("%.2ftb",(byteSize / (unit * unit * unit * unit))); //테라
		}else if(byteSize < unit * unit * unit * unit * unit * unit){
			return String.format("%.2fpb",(byteSize / (unit * unit * unit * unit * unit))); //페타
		}
			
		return byteSize+"b";
		
		
	}
}
