package org.apache.lucene.analysis.tokenattributes;

import org.apache.lucene.util.AttributeImpl;

import java.util.List;

public class CompoundTermAttributeImpl extends AttributeImpl implements CompoundTermAttribute, Cloneable {
	private List compound;

	public CompoundTermAttributeImpl() {
	}

	public CompoundTermAttributeImpl(List compound) {
		this.compound = compound;
	}

	@Override
	public void clear() {
		compound = null;
	}

	@Override
	public boolean equals(Object other) {
		if (other == this) {
			return true;
		}

		if (other instanceof CompoundTermAttributeImpl) {
			final CompoundTermAttributeImpl o = (CompoundTermAttributeImpl) other;
			return (this.compound == null ? o.compound == null : this.compound.equals(o.compound));
		}

		return false;
	}

	@Override
	public int hashCode() {
		return (compound == null) ? 0 : compound.hashCode();
	}

	@Override
	public void copyTo(AttributeImpl target) {
		CompoundTermAttribute t = (CompoundTermAttribute) target;
		t.setCompounds(compound);
	}

	@Override
	public void setCompounds(List compound) {
		if(compound == null || compound.size() == 0) {
			this.compound = null;
		} else {
			this.compound = compound;
		}
	}

	@Override
	public List getCompounds() {
		if(compound==null || compound.size() == 0) {
			return null;
		}
		return compound;
	}
}
