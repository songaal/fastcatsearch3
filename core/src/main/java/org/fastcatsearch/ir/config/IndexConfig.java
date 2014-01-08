package org.fastcatsearch.ir.config;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
/**
<index-config>
	<pk-term-interval>64</pk-term-interval>
	<pk-bucket-size>64K</pk-bucket-size>
	<term-interval>64</term-interval>
	<bucket-size>64K</bucket-size>
	<work-memory-size>128M</work-memory-size>
	<work-bucket-size>256</work-bucket-size>
	<compression-type>fast</compression-type>
</index-config>
 * */
@XmlRootElement(name = "index-config")
public class IndexConfig {
	
	public static final IndexConfig defaultConfig;
	static{
		defaultConfig = new IndexConfig();
		defaultConfig.pkTermInterval = 128;
		defaultConfig.pkBucketSize = 65536;
		defaultConfig.indexTermInterval = 128;
		defaultConfig.indexWorkBucketSize = 65536;
		defaultConfig.indexWorkMemorySize = 134217728; //128mb
	}
	
	private int pkTermInterval;
	private int pkBucketSize;
	private int indexTermInterval;// inmemory lexicon ratio = 1/indexTermInterval
	private int indexWorkBucketSize;
	private int indexWorkMemorySize;// limit memory use. if exeed this value, flush.
	

	@XmlElement(name="pk-term-interval")
	public int getPkTermInterval() {
		return pkTermInterval;
	}

	@XmlElement(name="pk-bucket-size")
	public int getPkBucketSize() {
		return pkBucketSize;
	}

	@XmlElement(name="term-interval")
	public int getIndexTermInterval() {
		return indexTermInterval;
	}

	@XmlElement(name="work-bucket-size")
	public int getIndexWorkBucketSize() {
		return indexWorkBucketSize;
	}

	@XmlElement(name="work-memory-size")
	public int getIndexWorkMemorySize() {
		return indexWorkMemorySize;
	}

	public void setPkTermInterval(int pkTermInterval) {
		this.pkTermInterval = pkTermInterval;
	}

	public void setPkBucketSize(int pkBucketSize) {
		this.pkBucketSize = pkBucketSize;
	}

	public void setIndexTermInterval(int indexTermInterval) {
		this.indexTermInterval = indexTermInterval;
	}

	public void setIndexWorkBucketSize(int indexWorkBucketSize) {
		this.indexWorkBucketSize = indexWorkBucketSize;
	}

	public void setIndexWorkMemorySize(int indexWorkMemorySize) {
		this.indexWorkMemorySize = indexWorkMemorySize;
	}
	
}

