//package org.fastcatsearch.ir.config;
//
//import java.util.List;
//
//import javax.xml.bind.annotation.XmlElement;
//import javax.xml.bind.annotation.XmlRootElement;
//
//@XmlRootElement
//public class IndexingSourceConfig {
//	private List<SingleSourceConfig> sourceConfigList;
//
//	@XmlElement(name = "source")
//	public List<SingleSourceConfig> getSourceConfigList() {
//		return sourceConfigList;
//	}
//
//	public void setSourceConfigList(List<SingleSourceConfig> sourceConfigList) {
//		this.sourceConfigList = sourceConfigList;
//	}
//
//	@Override
//	public String toString() {
//		StringBuilder sb = new StringBuilder();
//		if (sourceConfigList != null) {
//			for (SingleSourceConfig sourceConfig : sourceConfigList) {
//				if (sb.length() > 0) {
//					sb.append(",");
//				}
//				sb.append(sourceConfig.getClass());
//			}
//		}
//		return sb.toString();
//	}
//}
