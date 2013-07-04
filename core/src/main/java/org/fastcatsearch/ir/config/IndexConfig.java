package org.fastcatsearch.ir.config;
import javax.xml.bind.annotation.XmlElement;
/**
 <index>
	<pk-term-interval>64</pk-term-interval>
	<pk-bucket-size>64K</pk-bucket-size>
	<term-interval>64</term-interval>
	<bucket-size>64K</bucket-size>
	<work-memory-size>128M</work-memory-size>
	<work-bucket-size>256</work-bucket-size>
	<compression-type>fast</compression-type>
</index>
 * */
public class IndexConfig {
	private int pkTermInterval;
	private int pkBucketSize;
	private int indexTermInterval;
	private int indexWorkBucketSize;
	private int indexWorkMemorySize;
	

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

