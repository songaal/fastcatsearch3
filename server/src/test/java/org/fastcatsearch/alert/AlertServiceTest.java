package org.fastcatsearch.alert;

import java.io.IOException;

import org.fastcatsearch.cluster.Node;
import org.fastcatsearch.exception.FastcatSearchException;
import org.fastcatsearch.exception.FastcatSearchException;
import org.fastcatsearch.settings.Settings;
import org.junit.Test;

public class AlertServiceTest {

	@Test
	public void test() throws FastcatSearchException {
		Settings settings = new Settings();
		ClusterAlertService alertService = new ClusterAlertService(null, settings, null);
		alertService.start();
		
		int COUNT = 1000;
		Node node = new Node("maser", "", "192.168.0.30", 8080);
		FastcatSearchException e = new FastcatSearchException("에러발생.", new IOException("io에러"));
		for (int i = 0; i < COUNT; i++) {
			alertService.handleException(node, e);
			try {
				Thread.sleep(100);
			} catch (InterruptedException e1) {
				e1.printStackTrace();
			}
		}
	}

}
