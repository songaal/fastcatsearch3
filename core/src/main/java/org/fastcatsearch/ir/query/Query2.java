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

import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

import org.fastcatsearch.ir.search.clause.Clause;

/**
	cn=allgoods
	&ht=<a>:</a>
	&sn=0
	&ln=10
	&so=cache
	&ud=debug:true,logger:file
	&fl=askldjf,askjdf:30,askljfl,salkjfl
	&se={gd_nm:마우스 피스:100}or{brand,seller,kindnm:베트남 신혼 여행:1000}
	&gr=sellprice:freq:key_desc,goodkind:section_freq:5:freq_asc
	&gc={sellprice:12345:0}
	&ra=_score_,price:asc,point:desc
*/
public class Query2 {
	public static int SEARCH_OPT_NOCACHE = 1 << 0;
	public static int SEARCH_OPT_HIGHLIGHT = 1 << 1;
	
	private Clause clause;
	private List<View> views;
	private Filters filters;
	private Groups groups;
	private Clause groupClause;
	private Filters groupFilters;
	private Sorts sorts;
	private Metadata meta;
	
	public Query2(){ }
	
	public String toString(){
		StringBuilder sb = new StringBuilder("\n[Metadata]").append(meta);
		if(clause != null) sb.append("\n[Clause]").append(clause);
		if(filters != null) sb.append("\n[Filters]").append(filters);
		if(groups != null) sb.append("\n[Groups]").append(groups);
		if(groupClause != null) sb.append("\n[GroupCluase]").append(groupClause);
		if(groupFilters != null) sb.append("\n[GroupFilter]").append(groupFilters);
		if(sorts != null) sb.append("\n[Sorts]").append(sorts);
		
		if(views != null){
			sb.append("\n[Views]");
			for(int i=0;i<views.size();i++){
				sb.append(views.get(i).toString()).append(",");
			}
		}		
		return sb.toString();
	}
	public void writeTo(OutputStream out){
		
	}
	
	public void readFrom(InputStream in){
		
	}
	
	public void setClause(Clause clause){
		this.clause = clause;
	}
	
	public Clause getClause(){
		return clause;
	}

	public List<View> getViews() {
		return views;
	}

	public void setViews(List<View> view) {
		this.views = view;
	}

	public Filters getFilters() {
		return filters;
	}

	public void setFilters(Filters filters) {
		this.filters = filters;
	}
	
	public Groups getGroups() {
		return groups;
	}

	public void setGroups(Groups groups) {
		this.groups = groups;
	}
	
	public void setGroupClause(Clause groupClause){
		this.groupClause = groupClause;
	}
	
	public Clause getGroupClause(){
		return groupClause;
	}

	public Sorts getSorts() {
		return sorts;
	}

	public void setSorts(Sorts sorts) {
		this.sorts = sorts;
	}

	public Metadata getMeta() {
		return meta;
	}

	public void setMeta(Metadata meta) {
		this.meta = meta;
	}

	public Filters getGroupFilters() {
		return groupFilters;
	}
	
	public void setGroupFilters(Filters groupFilters) {
		this.groupFilters = groupFilters;
	}
}
