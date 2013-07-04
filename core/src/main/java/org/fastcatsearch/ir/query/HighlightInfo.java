/*
 * Copyright 2013 Websquared, Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.fastcatsearch.ir.query;

import java.util.Iterator;
import java.util.List;

import org.fastcatsearch.ir.settings.FieldSetting;


/**
 * Contains information that used when making summary result. 
 * */
public class HighlightInfo {
	private int fieldnumber;
	private FieldSetting fieldSetting;
	private List<String> termList;
	private List<String> orgList;
	private int summarySize;
	private boolean useHighlight;
	private boolean useSummary;
	
	public HighlightInfo(int fieldnumber, List<String> termList, List<String> orgList, boolean useHighlight, boolean useSummary) {
		this.fieldnumber = fieldnumber;
		this.termList = termList;
		this.orgList = orgList;
		this.useHighlight = useHighlight;
		this.useSummary = useSummary;
	}
	public int fieldNumber(){
		return fieldnumber;
	}
	public FieldSetting getFieldSetting() {
		return fieldSetting;
	}
	public void setFieldSetting(FieldSetting fieldSetting) {
		this.fieldSetting = fieldSetting;
	}
	public List<String> termList(){
		return termList;
	}
	public List<String> orgList(){
		return orgList;
	}
	public int summarySize(){
		return summarySize;
	}
	public void setSummarySize(int s){
		summarySize = s;
	}
	public boolean useHighlight(){
		return useHighlight;
	}
	
	public boolean useSummary(){
		return useSummary;
	}
	
	public String toString(){
		StringBuffer sb = new StringBuffer();
		sb.append("field = ");
			sb.append(fieldnumber);
		sb.append(", terms = [");
		if(termList != null){
			Iterator<String> iter = termList.iterator();
			int i = 0;
			while (iter.hasNext()) {
				String term = iter.next();
				sb.append(term);
				if(i++ < termList.size() - 1)
					sb.append(",");
			}
		}else{
			sb.append("null");
		}
		sb.append("], orgTerms = [");
		if(orgList != null){
			Iterator<String> iter = orgList.iterator();
			int i = 0;
			while (iter.hasNext()) {
				String term = iter.next();
				sb.append(term);
				if(i++ < orgList.size() - 1)
					sb.append(",");
			}
		}else{
			sb.append("null");
		}
		sb.append("], summarySize = "+summarySize);
		sb.append(", highlight = ");
		sb.append(useHighlight);
		sb.append(", summary = ");
		sb.append(useSummary);
		
		return sb.toString();
	}
	
	@Override
	public int hashCode(){
		int b = 378551;
		int a = 63689;
		int h = 0;
		
		h = h * a + fieldnumber;
		a = a * b;
		
		Iterator<String> i = termList.iterator();
		while (i.hasNext()) {
			String term = i.next();
			for (int j = 0; j < term.length(); j++) {
				h = h * a + term.charAt(j);
				a = a * b;
			}
		}
		i = orgList.iterator();
		while (i.hasNext()) {
			String term = i.next();
			for (int j = 0; j < term.length(); j++) {
				h = h * a + term.charAt(j);
				a = a * b;
			}
		}
		
		h = h * a + summarySize;
		return h;
	}
	@Override
	public boolean equals(Object obj){
		HighlightInfo si = (HighlightInfo)obj;
		if(fieldnumber != si.fieldnumber){
			return false;
		}
		if(termList.size() != si.termList.size()){
			return false;
		}
		if(!termList.equals(obj)){
			return false;
		}
		if(orgList.size() != si.orgList.size()){
			return false;
		}
		if(!orgList.equals(obj)){
			return false;
		}
		if(summarySize != si.summarySize){
			return false;
		}
		return true;
	}
}
