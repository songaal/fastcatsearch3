package org.fastcatsearch.common;

import org.fastcatsearch.env.Environment;
import org.junit.Before;

public class FastcatSearchTest {
	protected String homeDir;
	protected Environment environment;

	@Before
	public void setUp(){
		homeDir = "testHome/fastcatsearch";
		environment = new Environment(homeDir).init();
	}
}
