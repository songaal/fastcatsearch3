package org.fastcatsearch.ir.config;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * <data-info documents="7800" deletes="300">
	<segmentList>
		<segment id="0" documents="6000">
			<revision id="0" documents="2000" deletes="100" time="2013-06-15 15:30:00"/>
			<revision id="1" documents="4500" deletes="200" time="2013-06-15 15:40:00"/>
			<revision id="2" documents="6000" deletes="250" time="2013-06-15 15:50:00"/>
		</segment>
		<segment id="1" documents="1800">
			<revision id="0" documents="500" deletes="10" time="2013-06-15 16:00:00"/>
			<revision id="1" documents="1800" deletes="50" time="2013-06-15 16:10:00"/>
		</segment>
	</segmentList>
</data-info>
 * */

@XmlRootElement(name = "data-info")
public class DataInfo {

}
