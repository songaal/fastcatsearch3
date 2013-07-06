package org.fastcatsearch.ir.config;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
<collection-status>
	<data-status sequence="2" time="Mon May 20 13:03:32 KST 2013" />
	<index-full documents="50" start="2013-05-20 13:03:32" end="2013-05-20 13:03:32" duration="365ms" />
	<index-increment documents="5" start="2013-05-20 13:03:32" end="2013-05-20 13:03:32" duration="365ms" />	
</collection-status>
 * */
@XmlRootElement(name = "collection-status")
public class CollectionStatus {
	private DataStatus data;
	
	@XmlElement(name="data-status")
	public DataStatus getData(){
		return data;
	}
	
	public void setData(DataStatus data){
		this.data = data;
	}
	
	public static class DataStatus {
		private int sequence;
		
		@XmlAttribute(name="data-status")
		public int getSequence(){
			return sequence;
		}
		
		public void setSequence(int sequence){
			this.sequence = sequence;
		}
		
		public String getPathName(){
			return "data"+(sequence == 0 ? "" : Integer.toString(sequence)); 
		}
		
		public String getPathName(int seq){
			if(seq != -1){
				return "data"+(sequence == 0 ? "" : Integer.toString(seq));
			}else{
				return getPathName();
			}
		}
	}
}
