package org.fastcatsearch.ir.dictionary;

import java.io.InputStream;

public interface DictionaryBuilder {
	public void loadSource(InputStream is);
}
