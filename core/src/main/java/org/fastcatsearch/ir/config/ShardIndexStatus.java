//package org.fastcatsearch.ir.config;
//
//import javax.xml.bind.annotation.XmlAttribute;
//import javax.xml.bind.annotation.XmlRootElement;
//
///**
// * <index-status sequence="2"> <index-full documents="50" updates ="1" deletes="20" start="2013-05-20 13:03:32"
// * end="2013-05-20 13:03:32" duration="365ms" /> <index-add documents="5" updates ="1" deletes="20" start="2013-05-20 13:03:32"
// * end="2013-05-20 13:03:32" duration="365ms" /> </collection-status>
// * */
//@XmlRootElement(name = "index-status")
//public class ShardIndexStatus extends CollectionIndexStatus {
//	private int sequence;
//	
//	public ShardIndexStatus copy() {
//		ShardIndexStatus shardIndexStatus = new ShardIndexStatus();
//		shardIndexStatus.sequence = sequence;
//		if (fullIndexStatus != null) {
//			shardIndexStatus.fullIndexStatus = fullIndexStatus.copy();
//		}
//		if (addIndexStatus != null) {
//			shardIndexStatus.addIndexStatus = addIndexStatus.copy();
//		}
//		return shardIndexStatus;
//	}
//
//	
//
//	@Override
//	public String toString() {
//		return "["+getClass().getSimpleName()+"] seq[" + sequence + "] last-full=[" + fullIndexStatus + "] last-add=[" + addIndexStatus + "]";
//	}
//
//	@XmlAttribute(name="sequence")
//	public int getSequence() {
//		return sequence;
//	}
//
//	public void setSequence(int sequence) {
//		this.sequence = sequence;
//	}
//
//
//}
