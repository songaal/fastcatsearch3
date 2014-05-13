package org.apache.lucene.analysis.tokenattributes;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.util.AttributeImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AdditionalTermAttributeImpl extends AttributeImpl implements
		AdditionalTermAttribute {
	
	private static final Logger logger = LoggerFactory.getLogger(AdditionalTermAttributeImpl.class);
	
	private List<String[]> additionalTerms;
	private List<int[]> offsets;
	private OffsetAttribute offsetAttribute;
	
	@Override
	public void init(TokenStream tokenStream) {
		if(tokenStream!=null && tokenStream.hasAttribute(OffsetAttribute.class)) {
			this.offsetAttribute = tokenStream.getAttribute(OffsetAttribute.class);
		}
		this.additionalTerms = new ArrayList<String[]>();
		this.offsets = new ArrayList<int[]>();
	}
	
	@Override
	public void clear() {
		//additionalTerms.clear();
	}

	@Override
	public boolean equals(Object other) {
		if (other == this) {
			return true;
		}

		if (other instanceof AdditionalTermAttributeImpl) {
			final AdditionalTermAttributeImpl o = (AdditionalTermAttributeImpl) other;
			return (this.additionalTerms == null ? o.additionalTerms == null : this.additionalTerms.equals(o.additionalTerms));
		}

		return false;
	}

	@Override
	public int hashCode() {
		return (additionalTerms == null) ? 0 : additionalTerms.hashCode();
	}

	@Override
	public void copyTo(AttributeImpl target) {
		AdditionalTermAttribute t = (AdditionalTermAttribute) target;
		for(int inx=0;inx < additionalTerms.size(); inx++) {
			String[] term = additionalTerms.get(inx);
			int[] offset = offsets.get(inx);
			t.addAdditionalTerm(term[0], term[1], offset[0], offset[1]);
		}
	}
	
	@Override
	public void addAdditionalTerm(String additionalTerm, String type, int start, int end) {
		logger.trace("add additional {}", additionalTerm);
		this.additionalTerms.add(new String[] { additionalTerm, type });
		this.offsets.add(new int[] { start, end } );
	}

	@Override
	public Iterator<String[]> iterateAdditionalTerms() {
		return new Iterator<String[]>() {

			@Override
			public boolean hasNext() {
				return additionalTerms.size() > 0;
			}

			@Override
			public String[] next() {
				String[] term = additionalTerms.remove(0);
				int[] offset = offsets.remove(0);
				if(offsetAttribute!=null) {
					offsetAttribute.setOffset(offset[0], offset[1]);
				}
				return term;
			}

			@Override
			public void remove() { 
				additionalTerms.remove(0);
			}
		};
	}
}
