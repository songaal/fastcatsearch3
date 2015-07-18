///*
// * Copyright (c) 2013 Websquared, Inc.
// * All rights reserved. This program and the accompanying materials
// * are made available under the terms of the GNU Public License v2.0
// * which accompanies this distribution, and is available at
// * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
// * 
// * Contributors:
// *     swsong - initial API and implementation
// */
//
//package org.fastcatsearch.ir.summary;
//
//import java.io.IOException;
//import java.util.ArrayList;
//import java.util.Collections;
//import java.util.Iterator;
//import java.util.List;
//import java.util.regex.Matcher;
//import java.util.regex.Pattern;
//
//import org.apache.lucene.analysis.Analyzer;
//import org.fastcatsearch.ir.query.HighlightInfo;
//import org.fastcatsearch.ir.search.HighlightAndSummary;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//
//public class SimpleEnglishHighlightAndSummary implements HighlightAndSummary {
//	protected static Logger logger = LoggerFactory.getLogger(SimpleEnglishHighlightAndSummary.class);
//
//	public char[] modify(HighlightInfo summaryInfo, char[] target, String[] highlightTags) {
//		boolean useHighlight = summaryInfo.useHighlight();
//		boolean useSummary = summaryInfo.useSummary();
//
//		if (useSummary) {
//			if (useHighlight && highlightTags != null) {
//				if (target.length <= summaryInfo.summarySize() || summaryInfo.summarySize() <= 0) {
//					return highlight(target, summaryInfo.termList(), summaryInfo.orgList(), highlightTags);
//				} else {
//					return highlightAndSummarize(target, summaryInfo.termList(), summaryInfo.orgList(), summaryInfo.summarySize(), highlightTags,
//							true);
//				}
//			}
//			if (target.length <= summaryInfo.summarySize() || summaryInfo.summarySize() <= 0) {
//				return target;
//			} else {
//				return highlightAndSummarize(target, summaryInfo.termList(), summaryInfo.orgList(), summaryInfo.summarySize(), null, false);
//			}
//		} else {
//			if (useHighlight && highlightTags != null) {
//				return highlight(target, summaryInfo.termList(), summaryInfo.orgList(), highlightTags);
//			}
//
//			return target;
//		}
//	}
//
//	protected int getOccurrence(char[] target, String term, int[] occurrence, int maxSize) {
//		int p = 0;
//		int pos = -1;
//		int count = 0;
//		// logger.debug("target ="+new String(target));
//		while (p + term.length() <= target.length) {
//			boolean prevMatched = false;
//			for (int i = 0; i < term.length(); i++) {
//				// logger.debug("p= "+p+",term.length()= "+term.length()+" > "+target.length);
//				// if(p + term.length() > target.length){
//				if (p >= target.length) {
//					break;
//				}
//				// logger.debug("com "+target[p]+" == "+term.charAt(i));
//				if (target[p] != term.charAt(i)) {
//					i = -1;
//					if (prevMatched) {
//						// if prev character matched, then do not increase target string position. check from here p.
//						prevMatched = false;
//					} else {
//						p++;
//					}
//					continue;
//				} else {
//					prevMatched = true;
//					p++;
//					// logger.debug("p++ => "+p);
//				}
//
//				if (p > target.length) {
//					break;
//				}
//
//				// logger.debug("i = "+i+", term.length() - 1="+(term.length() - 1));
//
//				if (i == term.length() - 1) {
//					pos = p - term.length();
//					// logger.debug("count="+count+" < max="+maxSize);
//					if (count < maxSize) {
//						// logger.debug("@@@@ occu ==> "+pos);
//						occurrence[count++] = pos;
//						if (count == maxSize)
//							return count;
//					}
//				}
//			}
//		}
//
//		return count;
//	}
//
//	//
//	private char[] highlightAndSummarize(char[] target, List<String> terms, List<String> orgTerms, int summarySize, String[] highlightTags,
//			boolean useHighlight) {
//		// if(terms.size() == 0)
//		// return target;
//
//		int pos = -1;
//		int margin = 0;
//
//		if (terms == null || terms.size() == 0 || summarySize <= 10 || target.length <= 10) { // 타이틀같은 필드는 앞에서 부터 잘라준다.
//			// logger.debug("### summarySize="+summarySize+", target.length="+target.length);
//			pos = 0;
//		} else {
//			int OCCUR_SIZE_MAX = 1;
//			int[] occurrence = new int[OCCUR_SIZE_MAX];
//			Iterator<String> iterator = terms.iterator();
//
//			// find the first appeared term.
//			while (iterator.hasNext()) {
//				String term = iterator.next();
//				int count = getOccurrence(target, term, occurrence, OCCUR_SIZE_MAX);
//				// logger.debug("count = "+count);
//				for (int j = 0; j < count; j++) {
//					// logger.debug("occurrence["+j+"] = "+occurrence[j]);
//				}
//				if (pos < 0) {
//					pos = occurrence[0];
//				} else {
//					pos = pos < occurrence[0] ? pos : occurrence[0];
//				}
//				// logger.debug("pos = "+pos);
//			}
//			int M = 15;
//			margin = 5;// summarySize / 5;
//			M = summarySize / 3 < M ? summarySize / 3 : M;
//			// find . or ,
//			boolean foundPeriod = false;
//			for (; pos - margin > 0 && margin < M; margin++) {
//				// logger.debug("check period => "+target[pos - margin]+",pos="+pos+", margin="+margin);
//				if (target[pos - margin] == '.' || target[pos - margin] == ',') {
//					pos++;
//					foundPeriod = true;
//					break;
//				}
//			}
//
//			if (!foundPeriod) {
//				// find space character
//				for (; pos - margin > 0 && margin > M / 2; margin--) {
//					// logger.debug("check space => "+target[pos - margin]+",pos="+pos+", margin="+margin);
//					if (target[pos - margin] == ' ') {
//						pos++;
//						break;
//					}
//				}
//			}
//		}
//
//		String str = null;
//		// logger.debug("pos - margin ="+(pos - margin ));
//		if (pos - margin > 0) {
//			int st = pos - margin;
//			// logger.debug("target.length ="+(target.length)+", (pos - margin)="+(pos - margin));
//			if (target.length - st > summarySize) {
//				str = new String(target, st, summarySize);
//			} else {
//				str = new String(target, target.length - summarySize, summarySize);
//			}
//		} else {
//			// if(target.length - pos > summarySize){
//			// str = new String(target, pos, summarySize);
//			// }else{
//			// str = new String(target, target.length - summarySize, summarySize);
//			// }
//			str = new String(target, 0, summarySize);
//		}
//		str = str.trim() + "..";
//
//		// 긴 단어부터 하이라이팅을 수행한다.
//		if (useHighlight && terms != null) {
//			return highlight(str, terms, orgTerms, highlightTags);
//		}
//		return str.toCharArray();
//
//	}
//
//	private char[] highlight(char[] target, List<String> terms, List<String> orgTerms, String[] highlightTags) {
//		if (terms == null)
//			return target;
//
//		String str = new String(target);
//		return highlight(str, terms, orgTerms, highlightTags);
//	}
//
//	private char[] highlight(String target, List<String> terms, List<String> orgTerms, String[] highlightTags) {
//		String tmp = "";
//		List<MatchRegion> matchList = new ArrayList<MatchRegion>(terms.size());
//		for (int i = 0; i < orgTerms.size(); i++) {
//			String termStr = orgTerms.get(i);
//			if (termStr.length() > 3) {
//				if (!tmp.contains(termStr)) {
//					String patternStr = "^" + termStr;
//					Pattern param = Pattern.compile(patternStr, Pattern.CASE_INSENSITIVE);
//					Matcher matcher = param.matcher(target);
//					while (matcher.find()) {
//						matchList.add(new MatchRegion(matcher.start(), matcher.end(), termStr));
//					}
//				}
//				if (!tmp.contains(termStr)) {
//					String patternStr = termStr + "$";
//					Pattern param = Pattern.compile(patternStr, Pattern.CASE_INSENSITIVE);
//					Matcher matcher = param.matcher(target);
//					while (matcher.find()) {
//						matchList.add(new MatchRegion(matcher.start(), matcher.end(), termStr));
//					}
//				}
//				if (!tmp.contains(termStr)) {
//					String patternStr = "[\\s]" + termStr;
//					Pattern param = Pattern.compile(patternStr, Pattern.CASE_INSENSITIVE);
//					Matcher matcher = param.matcher(target);
//					while (matcher.find()) {
//						matchList.add(new MatchRegion(matcher.start() + 1, matcher.end(), termStr));
//						tmp += (termStr + " ");
//					}
//				}
//				if (!tmp.contains(termStr)) {
//					String patternStr = termStr + "[\\s]";
//					Pattern param = Pattern.compile(patternStr, Pattern.CASE_INSENSITIVE);
//					Matcher matcher = param.matcher(target);
//					while (matcher.find()) {
//						matchList.add(new MatchRegion(matcher.start(), matcher.end() - 1, termStr));
//						tmp += (termStr + " ");
//					}
//				}
//			} else {
//				// logger.debug("tmp="+tmp);
//				// logger.debug("termStr="+termStr);
//				if (!tmp.contains(termStr)) {
//					String patternStr = "^" + termStr + "[\\s]";
//					Pattern param = Pattern.compile(patternStr, Pattern.CASE_INSENSITIVE);
//					Matcher matcher = param.matcher(target);
//					while (matcher.find()) {
//						matchList.add(new MatchRegion(matcher.start(), matcher.end() - 1, termStr));
//					}
//				}
//				if (!tmp.contains(termStr)) {
//					String patternStr = "[\\s]" + termStr + "$";
//					Pattern param = Pattern.compile(patternStr, Pattern.CASE_INSENSITIVE);
//					Matcher matcher = param.matcher(target);
//					while (matcher.find()) {
//						matchList.add(new MatchRegion(matcher.start() + 1, matcher.end(), termStr));
//					}
//				}
//				if (!tmp.contains(termStr)) {
//					String patternStr = "[\\s]" + termStr + "[\\s]";
//					Pattern param = Pattern.compile(patternStr, Pattern.CASE_INSENSITIVE);
//					Matcher matcher = param.matcher(target);
//					while (matcher.find()) {
//						matchList.add(new MatchRegion(matcher.start() + 1, matcher.end() - 1, termStr));
//						tmp += (termStr + " ");
//					}
//				}
//			}
//		}
//		for (int i = 0; i < terms.size(); i++) {
//			String termStr = terms.get(i);
//			if (termStr.length() > 3) {
//				if (!tmp.contains(termStr)) {
//					String patternStr = "^" + termStr;
//					Pattern param = Pattern.compile(patternStr, Pattern.CASE_INSENSITIVE);
//					Matcher matcher = param.matcher(target);
//					while (matcher.find()) {
//						matchList.add(new MatchRegion(matcher.start(), matcher.end(), termStr));
//					}
//				}
//				if (!tmp.contains(termStr)) {
//					String patternStr = termStr + "$";
//					Pattern param = Pattern.compile(patternStr, Pattern.CASE_INSENSITIVE);
//					Matcher matcher = param.matcher(target);
//					while (matcher.find()) {
//						matchList.add(new MatchRegion(matcher.start(), matcher.end(), termStr));
//					}
//				}
//				if (!tmp.contains(termStr)) {
//					String patternStr = "[\\s]" + termStr;
//					Pattern param = Pattern.compile(patternStr, Pattern.CASE_INSENSITIVE);
//					Matcher matcher = param.matcher(target);
//					while (matcher.find()) {
//						matchList.add(new MatchRegion(matcher.start() + 1, matcher.end(), termStr));
//						tmp += (termStr + " ");
//					}
//				}
//				if (!tmp.contains(termStr)) {
//					String patternStr = termStr + "[\\s]";
//					Pattern param = Pattern.compile(patternStr, Pattern.CASE_INSENSITIVE);
//					Matcher matcher = param.matcher(target);
//					while (matcher.find()) {
//						matchList.add(new MatchRegion(matcher.start(), matcher.end() - 1, termStr));
//						tmp += (termStr + " ");
//					}
//				}
//			} else {
//				// logger.debug("tmp="+tmp);
//				// logger.debug("termStr="+termStr);
//				if (!tmp.contains(termStr)) {
//					String patternStr = "^" + termStr + "[\\s]";
//					Pattern param = Pattern.compile(patternStr, Pattern.CASE_INSENSITIVE);
//					Matcher matcher = param.matcher(target);
//					while (matcher.find()) {
//						matchList.add(new MatchRegion(matcher.start(), matcher.end() - 1, termStr));
//					}
//				}
//				if (!tmp.contains(termStr)) {
//					String patternStr = "[\\s]" + termStr + "$";
//					Pattern param = Pattern.compile(patternStr, Pattern.CASE_INSENSITIVE);
//					Matcher matcher = param.matcher(target);
//					while (matcher.find()) {
//						matchList.add(new MatchRegion(matcher.start() + 1, matcher.end(), termStr));
//					}
//				}
//				if (!tmp.contains(termStr)) {
//					String patternStr = "[\\s]" + termStr + "[\\s]";
//					Pattern param = Pattern.compile(patternStr, Pattern.CASE_INSENSITIVE);
//					Matcher matcher = param.matcher(target);
//					while (matcher.find()) {
//						matchList.add(new MatchRegion(matcher.start() + 1, matcher.end() - 1, termStr));
//						tmp += (termStr + " ");
//					}
//				}
//			}
//		}
//
//		Collections.sort(matchList);
//
//		StringBuffer sb = new StringBuffer();
//		int old = 0;
//		for (int i = 0; i < matchList.size(); i++) {
//			MatchRegion mr = matchList.get(i);
//			int start = mr.start;
//			// logger.debug(">> "+mr.start+" ~ "+mr.end);
//			// logger.debug(" => "+target);
//			// logger.debug("substring "+old+"~"+start);
//			if (old > start)
//				continue;
//
//			// logger.debug(" => "+target.substring(old, start));
//			sb.append(target.substring(old, start));
//			sb.append(highlightTags[0]);
//			sb.append(mr.term);
//			sb.append(highlightTags[1]);
//
//			old = mr.end;
//		}
//
//		sb.append(target.substring(old));
//		// logger.debug("substring "+old+"~ => "+target.substring(old));
//
//		return sb.toString().toCharArray();
//
//	}
//
//	private void addToList(List matchList, MatchRegion mr) {
//		if (matchList.size() == 0) {
//			matchList.add(mr);
//			return;
//		}
//
//		for (int i = 0; i < matchList.size(); i++) {
//			MatchRegion t = (MatchRegion) matchList.get(i);
//			if (t.compareTo(mr) != 0) {
//				matchList.add(mr);
//				break;
//			}
//		}
//	}
//
//	class MatchRegion implements Comparable<MatchRegion> {
//		int start;
//		int end;
//		String term;
//
//		public MatchRegion(int start, int end, String term) {
//			this.start = start;
//			this.end = end;
//			this.term = term;
//		}
//
//		public int compareTo(MatchRegion o) {
//			return start - o.start;
//		}
//
//		public String toString() {
//			return "MatchRegion " + start + "~" + end + ", " + term;
//		}
//
//	}
//
//	@Override
//	public String highlight(Analyzer analyzer, String pText, String query, String[] tags, int len) throws IOException {
//		// TODO Auto-generated method stub
//		return null;
//	}
//
//}
