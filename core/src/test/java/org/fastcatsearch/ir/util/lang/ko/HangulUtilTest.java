package org.fastcatsearch.ir.util.lang.ko;

import static org.junit.Assert.*;

import org.fastcatsearch.util.lang.ko.HangulUtil;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Level;

public class HangulUtilTest {
	
	private static final Logger logger = LoggerFactory.getLogger("TEST_LOGGER");
	
	@Before
	public void init() {
		String LOG_LEVEL = System.getProperty("LOG_LEVEL");
		
		if(LOG_LEVEL==null || "".equals(LOG_LEVEL)) {
			LOG_LEVEL = "DEBUG";
		}
		
		((ch.qos.logback.classic.Logger)
			LoggerFactory.getLogger(ch.qos.logback.classic.Logger.ROOT_LOGGER_NAME)
			).setLevel(Level.toLevel("DEBUG"));
		
		((ch.qos.logback.classic.Logger)
			LoggerFactory.getLogger("TEST_LOGGER")
			).setLevel(Level.toLevel(LOG_LEVEL));
	}

	@Test
	public void testPrefix() {
		String s = HangulUtil.makeHangulPrefix("빙그레", '\t');
		
		
		String[] ss = s.split("\t");
		for (int i = 0; i < ss.length; i++) {
			logger.debug("word : {}",ss[i]);
		}
		assertEquals("ㅂ	비	빙	빙ㄱ	빙그	빙그ㄹ	빙그레", s);
	}

	@Test
	public void testSuffix() {
		String s = HangulUtil.makeHangulSuffix("빙그레", '\t');
		String[] ss = s.split("\t");
		for (int i = 0; i < ss.length; i++) {
			logger.debug("word : {}",ss[i]);
		}
	}
	
	@Test
	public void testChosung() {
		String s = HangulUtil.makeHangulChosung("빙그레", '\t');
		logger.debug(s);
		String[] ss = s.split("\t");
		for (int i = 0; i < ss.length; i++) {
			logger.debug("word : {}",ss[i]);
		}
		assertEquals("ㅂ	ㅂㄱ	ㅂㄱㄹ", s);
	}

    @Test
    public void testChosung2() {
        String s = HangulUtil.makeHangulChosung("abc", '\t');
        logger.debug("str= {}, size= {}", s, s.length());
        assertEquals("", s);
        assertEquals(0, s.length());
    }

}
