//package org.fastcatsearch.ir.io;
//
//public class NumberDataRef extends DataRef {
//	private Class<? extends Number> numberType;
//	
//	public NumberDataRef(Class<? extends Number> numberType){
//		this.numberType = numberType;
//	}
//	
//	
//	public Number getNumber(){
//		
//		if(numberType == Integer.class){
//			return bytesRef.toIntValue();
//		}else if(numberType == Long.class){
//			return bytesRef.toLongValue();
//		}else if(numberType == Float.class){
//			return Float.intBitsToFloat(bytesRef.toIntValue());
//		}else if(numberType == Double.class){
//			return Double.longBitsToDouble(bytesRef.toLongValue());
//		}
//		
//		return null;
//	}
//}
