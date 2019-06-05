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
	&sp=com.mydomain.sp.SampleStoredProdure  >> stored procedure 
	&qm=com.mydomain.modifier.SampleQueryModifier >> query modifier
	&rm=com.mydomain.modifier.SampleResultModifier >> result modifier
	&bd=bundlekey:5;price:desc,popular:desc
	
*/
public class Query {
	public static int SEARCH_OPT_NOCACHE = 1 << 0;
	public static int SEARCH_OPT_EXPLAIN = 1 << 1;
    public static int SEARCH_OPT_LOWERCASE = 1 << 2;
    public static int SEARCH_OPT_NOUNICODE = 1 << 3;
    public static int SEARCH_OPT_STOPONERROR = 1 << 4;

	public static enum EL {
		cn, sd, ht, sn, ln, so, ud, fl, se, ft, gr, gf, ra, sp, qm, rm, bd;
	};
	
	private Clause clause;
	private ViewContainer views;
	private Filters filters;
	private Groups groups;
	private Filters groupFilters;
	private Sorts sorts;
	private Metadata meta;
	private Query boostQuery;
	private Bundle bundle;
	
	public Query(){ 
		meta = new Metadata();
	}
	
	public String toString(){
		StringBuilder sb = new StringBuilder("\n[Metadata]").append(meta);
		if(clause != null) sb.append("\n[Clause]").append(clause);
		if(filters != null) sb.append("\n[Filters]").append(filters);
		if(groups != null) sb.append("\n[Groups]").append(groups);
		if(groupFilters != null) sb.append("\n[GroupFilter]").append(groupFilters);
		if(sorts != null) sb.append("\n[Sorts]").append(sorts);
		if(bundle != null) sb.append("\n[Bundle]").append(bundle);
		if(views != null){
			sb.append("\n[Views]");
			for(int i=0;i<views.size();i++){
				sb.append(views.get(i).toString()).append(",");
			}
		}		
		return sb.toString();
	}
	
	public void setClause(Clause clause){
		this.clause = clause;
	}
	
	public Clause getClause(){
		return clause;
	}

	public ViewContainer getViews() {
		return views;
	}

	public void setViews(ViewContainer view) {
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

	public Query getBoostQuery() {
		return boostQuery;
	}

	public void setBoostQuery(Query boostQuery) {
		this.boostQuery = boostQuery;
	}

	public Bundle getBundle() {
		return bundle;
	}

	public void setBundle(Bundle bundle) {
		this.bundle = bundle;
	}
	
}
