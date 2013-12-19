package org.apache.lucene.analysis.tokenattributes;

import org.apache.lucene.util.AttributeImpl;

public class FeatureAttributeImpl extends AttributeImpl implements FeatureAttribute, Cloneable {

	private FeatureType type;
	
	@Override
	public FeatureType type() {
		return type;
	}

	@Override
	public void setType(FeatureType type) {
		this.type = type;
	}

	@Override
	public void clear() {
		type = FeatureAttribute.DEFAULT_TYPE;
	}

	@Override
	public void copyTo(AttributeImpl target) {
		FeatureAttributeImpl attribute = (FeatureAttributeImpl) target;
		attribute.setType(this.type);
	}

}
