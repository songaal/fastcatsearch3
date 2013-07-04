package org.apache.lucene.analysis.tokenattributes;

import org.apache.lucene.util.AttributeImpl;
import org.fastcatsearch.ir.io.CharVector;

public class SynonymAttributeImpl extends AttributeImpl implements SynonymAttribute, Cloneable {
	private CharVector[] synonym;
	
	public SynonymAttributeImpl() {
	}

	public SynonymAttributeImpl(CharVector[] synonym) {
		this.synonym = synonym;
	}

	@Override
	public void clear() {
		synonym = null;
	}

	@Override
	public boolean equals(Object other) {
		if (other == this) {
			return true;
		}

		if (other instanceof SynonymAttributeImpl) {
			final SynonymAttributeImpl o = (SynonymAttributeImpl) other;
			return (this.synonym == null ? o.synonym == null : this.synonym.equals(o.synonym));
		}

		return false;
	}

	@Override
	public int hashCode() {
		return (synonym == null) ? 0 : synonym.hashCode();
	}

	@Override
	public void copyTo(AttributeImpl target) {
		SynonymAttribute t = (SynonymAttribute) target;
		t.setSynonym(synonym);
	}

	@Override
	public void setSynonym(CharVector[] synonym) {
		this.synonym = synonym;
	}

	@Override
	public CharVector[] getSynonym() {
		return synonym;
	}
}
