package org.fastcatsearch.module;

import org.fastcatsearch.common.Singletonable;
import org.fastcatsearch.env.Environment;
import org.fastcatsearch.settings.Settings;

public abstract class AbstractSingletoneModule extends AbstractModule implements Singletonable {

	public AbstractSingletoneModule(Environment environment, Settings settings) {
		super(environment, settings);
	}

}
