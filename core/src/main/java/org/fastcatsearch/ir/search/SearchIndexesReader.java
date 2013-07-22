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

package org.fastcatsearch.ir.search;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.CharsRefTermAttribute;
import org.apache.lucene.analysis.tokenattributes.PositionIncrementAttribute;
import org.apache.lucene.analysis.tokenattributes.StopwordAttribute;
import org.apache.lucene.analysis.tokenattributes.SynonymAttribute;
import org.apache.lucene.util.CharsRef;
import org.fastcatsearch.ir.analysis.AnalyzerPool;
import org.fastcatsearch.ir.common.IRException;
import org.fastcatsearch.ir.common.IndexFileNames;
import org.fastcatsearch.ir.document.PrimaryKeyIndexReader;
import org.fastcatsearch.ir.field.Field;
import org.fastcatsearch.ir.field.FieldDataParseException;
import org.fastcatsearch.ir.index.IndexFieldOption;
import org.fastcatsearch.ir.io.BufferedFileInput;
import org.fastcatsearch.ir.io.BytesDataOutput;
import org.fastcatsearch.ir.io.CharVector;
import org.fastcatsearch.ir.io.CharVectorTokenizer;
import org.fastcatsearch.ir.io.FixedMinHeap;
import org.fastcatsearch.ir.io.IOUtil;
import org.fastcatsearch.ir.io.IndexInput;
import org.fastcatsearch.ir.query.MultiTermOperatedClause;
import org.fastcatsearch.ir.query.OperatedClause;
import org.fastcatsearch.ir.query.OrOperatedClause;
import org.fastcatsearch.ir.query.Term;
import org.fastcatsearch.ir.query.Term.Option;
import org.fastcatsearch.ir.query.TermOperatedClause;
import org.fastcatsearch.ir.search.posting.PostingDocsMerger;
import org.fastcatsearch.ir.search.posting.PostingDocsReader;
import org.fastcatsearch.ir.settings.FieldSetting;
import org.fastcatsearch.ir.settings.IndexSetting;
import org.fastcatsearch.ir.settings.PkRefSetting;
import org.fastcatsearch.ir.settings.PrimaryKeySetting;
import org.fastcatsearch.ir.settings.Schema;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 검색시 numeric field도 모두 string 형을 처리하기때문에, key는 123 1200 2 20000 31 과 같이 정렬되어 있다.
 * */
public class SearchIndexesReader implements Cloneable {
	private static Logger logger = LoggerFactory.getLogger(SearchIndexesReader.class);

	private IndexInput postingInput;
	private IndexInput lexiconInput;
	private IndexInput indexInput;
	private Schema schema;

	private MemoryLexicon[] memoryLexicon;
	private long[] fileLimit;
	private IndexFieldOption[] fieldIndexOptions;
	
	private PrimaryKeyIndexReader pkReader;

	private AnalyzerPool[] queryTokenizerPool;
	private List<IndexSetting> indexSettingList;
	private FieldSetting[] pkFieldSettingList;
	
	
	public SearchIndexesReader() {
	}

	public SearchIndexesReader(Schema schema, File dir) throws IOException, IRException {
		this(schema, dir, 0);
	}

	public SearchIndexesReader(Schema schema, IndexInput postingInput, IndexInput lexiconInput, IndexInput indexInput, PrimaryKeyIndexReader pkReader,
			MemoryLexicon[] memoryLexicon, long[] fileLimit,
			AnalyzerPool[] queryTokenizerPool, IndexFieldOption[] fieldIndexOptions) {
		this.schema = schema;
		this.postingInput = postingInput;
		this.lexiconInput = lexiconInput;
		this.indexInput = indexInput;
		this.pkReader = pkReader;
		this.memoryLexicon = memoryLexicon;
		this.fileLimit = fileLimit;
		this.queryTokenizerPool = queryTokenizerPool;
		this.fieldIndexOptions = fieldIndexOptions;
	}

	public SearchIndexesReader(Schema schema, File dir, int revision) throws IOException, IRException {
		this.schema = schema;
		indexSettingList = schema.schemaSetting().getIndexSettingList();
		
		schema.schemaSetting().getFieldSettingList();
		
		
		int indexFieldSize = indexSettingList.size();
		queryTokenizerPool = new AnalyzerPool[indexFieldSize];
		memoryLexicon = new MemoryLexicon[indexFieldSize];
		fileLimit = new long[indexFieldSize];
		logger.debug("seg reader dir = {}", dir.getAbsolutePath());
		postingInput = new BufferedFileInput(IndexFileNames.getRevisionDir(dir, revision), IndexFileNames.postingFile);
		lexiconInput = new BufferedFileInput(IndexFileNames.getRevisionDir(dir, revision), IndexFileNames.lexiconFile);
		indexInput = new BufferedFileInput(IndexFileNames.getRevisionDir(dir, revision), IndexFileNames.indexFile);

		int fieldCount = postingInput.readInt();
		fieldIndexOptions = new IndexFieldOption[fieldCount];
		for (int i = 0; i < fieldCount; i++) {
			fieldIndexOptions[i] = new IndexFieldOption(postingInput.readInt());
		}
		
//		fieldSettingList = new FieldSetting[indexFieldSize];
		
		for (int i = 0; i < indexFieldSize; i++) {
			IndexSetting is = indexSettingList.get(i);
//			fieldSettingList[i] = schema.getFieldSetting(fieldId);
			String queryAnalyzerName = is.getQueryAnalyzer();
//			queryTokenizerPool[i] = is.queryAnalyzerPool;
			queryTokenizerPool[i] = schema.getAnalyzerPool(queryAnalyzerName);

			if(queryTokenizerPool[i] != null) {
				logger.debug("QueryTokenizer[{}] = {}", i, queryTokenizerPool[i].getClass().getSimpleName());
			}else{
				//분석기를 못찾았을 경우.
				throw new IRException("Query analyzer not found >> " + queryAnalyzerName);
			}

			int indexSize = indexInput.readInt();

			logger.debug("====memoryLexicon - {}==== indexSize = {}", i, indexSize);
			if (indexSize > 0) {
				memoryLexicon[i] = new MemoryLexicon(indexSize);
				// Load lexicon-index to memory

				for (int k = 0; k < indexSize; k++) {
					memoryLexicon[i].put(k, indexInput.readUString(), indexInput.readLong(), indexInput.readLong());
					// logger.debug(memoryLexicon[i].toString(k));
				}
				if (i > 0) {
					// 중요!!:첫부분에는 단어갯수가 int로 기록되어 있으므로 이 부분을 감안하여 limit설정.(-IOUtil.SIZE_OF_INT)
					fileLimit[i - 1] = memoryLexicon[i].getStartPointer() - IOUtil.SIZE_OF_INT;
					// logger.debug((i-1)+" - Lexicon limit = "+fileLimit[i-1]);
				}
			} else {
				memoryLexicon[i] = new MemoryLexicon(0);
				long l = indexInput.readLong();

				if (i > 0) {
					// 파일 리미트를 설정한다.
					fileLimit[i - 1] = l - IOUtil.SIZE_OF_INT;
					// logger.debug((i-1)+" - Lexicon limit = "+fileLimit[i-1]);
				}
			}

		}
		fileLimit[indexFieldSize - 1] = lexiconInput.length();

		indexInput.close();

		
		PrimaryKeySetting primaryKeySetting = schema.schemaSetting().getPrimaryKeySetting();
		List<PkRefSetting> pkRefSettingList = primaryKeySetting.getFieldList();
		pkFieldSettingList = new FieldSetting[pkRefSettingList.size()];
		
		for(int i = 0; i < pkRefSettingList.size(); i++){
			pkFieldSettingList[i] = schema.getFieldSetting(pkRefSettingList.get(i).getRef());
		}
		
		
		pkReader = new PrimaryKeyIndexReader(IndexFileNames.getRevisionDir(dir, revision), IndexFileNames.primaryKeyMap);
	}

	@Override
	public SearchIndexesReader clone() {
		return new SearchIndexesReader(schema, postingInput.clone(), lexiconInput.clone(), indexInput.clone(), pkReader.clone(), memoryLexicon, fileLimit,
			queryTokenizerPool, fieldIndexOptions);
	}

	public OperatedClause getOperatedClause(Term term) throws IOException, IRException {
		String[] fieldIdList = term.fieldname();
		int[] fieldSequence = null;
//		ArrayList<String>[] termList = null;
//		ArrayList<String>[] orgList = null;
//		if (summary != null) {
//			fieldSequence = new int[fieldname.length];
//			termList = new ArrayList[fieldname.length];
//			orgList = new ArrayList[fieldname.length];
//		}

		Term.Type type = term.type();
		int weight = term.weight();
		Option option = term.option();

		CharVector fullTerm = new CharVector(term.termString());

		// make all Alphabet to upperCase
//		fullTerm.toUpperCase();
//		CharVector token = new CharVector();
		OperatedClause totalClause = null;

		String primaryKeyId = schema.schemaSetting().getPrimaryKeySetting().getId();

		for (int i = 0; i < fieldIdList.length; i++) {
			String fieldId = fieldIdList[i];
			
			
//			if (summary != null) {
//				termList[i] = new ArrayList<String>();
//				orgList[i] = new ArrayList<String>(); // new ArrayList<String>(5);
//			}
			logger.debug("getOperatedClause {} at {}, type={}",term.termString(), fieldId, type);
			
			
			// if this is primary key field..
			 if(fieldId.equals(primaryKeyId)){
				
				OperatedClause idOperatedClause = getPrimaryKeyOperatedClause(term);
				
				if (idOperatedClause != null) {
					if (totalClause == null) {
						totalClause = idOperatedClause;
					} else {
						totalClause = new OrOperatedClause(totalClause, idOperatedClause);
					}
				} else {
					totalClause = new TermOperatedClause(new PostingDocs(-1, fullTerm,  new PostingDoc[0], 0), 0);
				}

				continue;
			}

//			int indexFieldSequence = schema.indexnames.get(fn);
			int indexFieldSequence = schema.getSearchIndexSequence(fieldId);
//			logger.debug("fieldname = {} => {}", fn, indexFieldSequence);
//			if (fieldSequence != null){
//				fieldSequence[i] = schema.getFieldSequence(fn);
//				
//			}
//				fieldSequence[i] = schemaSetting.fieldnames.get(fn);
			 
			 
			if (indexFieldSequence < 0) {
				throw new IRException("Unknown Search Fieldname = " + fieldId);
				// logger.error("Unknown Search Fieldname = "+fn);
				// continue;
			}

			OperatedClause oneFieldClause = null;
			Analyzer tokenizer = queryTokenizerPool[indexFieldSequence].getFromPool();

			try {
				CharVectorTokenizer charVectorTokenizer = new CharVectorTokenizer(fullTerm);
				
				CharsRefTermAttribute termAttribute = null;
				PositionIncrementAttribute positionAttribute = null;
				SynonymAttribute synonymAttribute = null;
				StopwordAttribute stopwordAttribute = null;
				
				MultiTermOperatedClause phraseOperatedClause = new MultiTermOperatedClause(fieldIndexOptions[indexFieldSequence].isStorePosition());
				
				int positionOffset = 0;
				CharVector token = null;
				
				while(charVectorTokenizer.hasNext()){
					CharVector eojeol = charVectorTokenizer.next();
					
					if(option.useWildcard()){
						if(isWildcardTerm(eojeol)){
							
						}
					}
					
					if(option.isBoolean()){
						if(isBooeanOperator(eojeol)){
							
						}
					}
					
					
					logger.debug("find {} at {} by {}", eojeol, fieldId, tokenizer);
					TokenStream tokenStream = tokenizer.tokenStream(fieldId, eojeol.getReader());
					tokenStream.reset();
					
					if(tokenStream.hasAttribute(CharsRefTermAttribute.class)){
						termAttribute = tokenStream.getAttribute(CharsRefTermAttribute.class);
					}
					if(tokenStream.hasAttribute(PositionIncrementAttribute.class)){
						positionAttribute = tokenStream.getAttribute(PositionIncrementAttribute.class);
					}
					CharTermAttribute charTermAttribute = tokenStream.getAttribute(CharTermAttribute.class);
					
					if(tokenStream.hasAttribute(SynonymAttribute.class)){
						synonymAttribute = tokenStream.getAttribute(SynonymAttribute.class);
					}
					if(tokenStream.hasAttribute(StopwordAttribute.class)){
						stopwordAttribute = tokenStream.getAttribute(StopwordAttribute.class);
					}
					
		//			PosTagAttribute tagAttribute = tokenStream.getAttribute(PosTagAttribute.class);
					
					while(tokenStream.incrementToken()){
		
						if(termAttribute != null){
							CharsRef charRef = termAttribute.charsRef();
							char[] buffer = new char[charRef.length()];
							System.arraycopy(charRef.chars, charRef.offset, buffer, 0, charRef.length);
							token = new CharVector(buffer, 0, buffer.length);
						}else{
							token = new CharVector(charTermAttribute.buffer(), 0, charTermAttribute.length());
						}
						
						logger.debug("token = {}", token);
						//token.toUpperCase();
						//
						// stopword
						//
						if(option.useStopword() && stopwordAttribute != null && stopwordAttribute.isStopword()){
							logger.debug("stopword : {}", token);
							continue;
						}
						
						int queryPosition = 0;
						if(positionAttribute != null){
							int position = positionAttribute.getPositionIncrement();
							queryPosition = positionOffset + position; //
							positionOffset = position + 2; //다음 position은 +2 부터 할당한다. 공백도 1만큼 차지.
						}
						PostingDocs termDocs = getPosting(indexFieldSequence, token);
//						if (termList != null) {
//							termList[i].add(token.toString());
//							orgList[i].add(eojeol.toString());
//						}
						
						//OperatedClause mainTermClause = new TermOperatedClause(termDocs, weight, ignoreTermFreq);
						
						//
						//유사어확장.
						//
						List<PostingDocs> synoymList = null;
						
						if(option.useSynonym() && synonymAttribute != null){
							CharVector[] synonymList = synonymAttribute.getSynonym();
							if(synonymList != null){
								synoymList = new ArrayList<PostingDocs>(synonymList.length);
								for (int j = 0; j < synonymList.length; j++) {
									CharVector synonym = synonymList[j];
									//여기서 synonym을 변경하면 사전의 entry가 변경되므로 변경하지 않도록한다.
									synonym = synonym.duplicate();
									//
									//유사어도 ignore case
									//
									synonym.toUpperCase();
									
									logger.debug("synonym = {}", synonym);
									PostingDocs synonymTermDocs = getPosting(indexFieldSequence, synonym);
									
									synoymList.add(synonymTermDocs);
									
									if (synonymTermDocs != null) {
										// add synonym terms.
//										if (termList != null) {
//											// logger.debug("add synonym = {}", synonym);
//											termList[i].add(synonym.toString());
//											orgList[i].add("");
//										}
		
//										// make synonym clauses
//										// give synonym a lesser score
//										OperatedClause clause = new TermOperatedClause(termDocs, weight - 1, ignoreTermFreq);
//										if (synonymClause == null)
//											synonymClause = clause;
//										else
//											synonymClause = new OrOperatedClause(synonymClause, clause);
									}
								}
							}
						}
						
						
						phraseOperatedClause.addTerm(termDocs, queryPosition, synoymList);
						
//						if (synonymClause != null) {
//							mainTermClause = new OrOperatedClause(mainTermClause, synonymClause);
//						}

//						if (oneFieldClause == null) {
//							oneFieldClause = mainTermClause;
//						} else {
//							// append tokens with OR, AND
//							if (type == Term.Type.AND) {
//								oneFieldClause = new AndOperatedClause(oneFieldClause, mainTermClause);
////								logger.debug("AND {}", oneFieldClause.toString());
//							} else if (type == Term.Type.OR) {
//								oneFieldClause = new OrOperatedClause(oneFieldClause, mainTermClause);
//							}
//						}
					}// while

				
					oneFieldClause = phraseOperatedClause;
				}
			}catch(IOException e){
				logger.error("", e);
			} finally {
				queryTokenizerPool[indexFieldSequence].releaseToPool(tokenizer);
			}

//			if (oneFieldClause == null) {
//				oneFieldClause = new TermOperatedClause(null, weight);
//			}

			if (totalClause == null) {
				totalClause = oneFieldClause;
			} else {
				totalClause = new OrOperatedClause(totalClause, oneFieldClause);
			}

		}// for

//		if (summary != null) {
//			logger.debug("fieldSequence.length = {}", fieldSequence.length);
//			for (int i = 0; i < fieldSequence.length; i++) {
//				if (termList[i].size() > 0) {
//					logger.debug("summary-{} term size = {}", i, termList[i].size());
//					HighlightInfo hi = new HighlightInfo(fieldSequence[i], termList[i], orgList[i], option.useHighlight(), option.useSummary());
//					logger.debug("Add summanry {} = {}", i, hi);
//					summary.add(hi);
//				}
//			}
//		}

		
		return totalClause;

	}

	
	private boolean isBooeanOperator(CharVector token1) {
		// TODO Auto-generated method stub
		return false;
	}

	private boolean isWildcardTerm(CharVector token1) {
		// TODO Auto-generated method stub
		return false;
	}

	private OperatedClause getPrimaryKeyOperatedClause(Term term) throws IOException, FieldDataParseException {
		int weight = term.weight();
		String termString = term.termString();
		String[] list = termString.split(" ");
		int size = 1;
		if (list.length > 1) {
			size = list.length;
		}
		// 중복된 아이디리스트가 입력될수 있으므로 정렬후 중복제거 수행
		quickSort(list, 0, list.length - 1);
		size = removeRedundancy(list, list.length);
		logger.debug("search primary key! size={}", size);
		
		PostingDoc[] termDocList = new PostingDoc[size];
		int m = 0;
		BytesDataOutput pkOutput = new BytesDataOutput();
		
		String[] pkValues = null;
		for (int i = 0; i < size; i++) {
			
			pkOutput.reset();
			
			String pkValue = list[i];
			
			if(pkFieldSettingList.length > 1){
				//결합 pk일경우 값들은 ';'로 구분되어있다.
				pkValues = pkValue.split(";");
			}else{
				pkValues = new String[]{pkValue};
			}
			
			int docNo = -1;
			
			for(int j = 0; j< pkFieldSettingList.length; j++){
				FieldSetting fieldSetting = pkFieldSettingList[j];
				Field field = fieldSetting.createField(pkValues[j]);
				field.writeTo(pkOutput);
			}
			
			docNo = pkReader.get(pkOutput.array(), 0, (int) pkOutput.position());
			if (docNo != -1) {
				termDocList[m] = new PostingDoc(docNo, 1);
				m++;
			}
		}
		
		OperatedClause idOperatedClause = null;
		if (m > 0) {
			idOperatedClause = new TermOperatedClause(new PostingDocs(-1, new CharVector(termString), termDocList, m), weight);
		}

		return idOperatedClause;
		
	}

	protected PostingDocs getPosting(String indexFieldId, CharVector singleTerm) throws IOException {
		int indexSequence = schema.getSearchIndexSequence(indexFieldId);
		if (indexSequence < 0)
			throw new IOException("Unknown indexFieldId = " + indexFieldId);

		// logger.debug(indexFieldId+" = " + indexFieldSequence);
		return getPosting(indexSequence, singleTerm);
	}

	protected PostingDocs getPosting(int indexFieldSequence, CharVector singleTerm) throws IOException {
		if (memoryLexicon[indexFieldSequence].size() == 0)
			return null;
		if (singleTerm.length == 0)
			return null;
		
		long[] posInfo = new long[2];
		boolean found = memoryLexicon[indexFieldSequence].binsearch(singleTerm, posInfo);

		long pos = -1;
		// cannot find in memory index, let's find it in file index
		// long tt = System.currentTimeMillis();
		if (found) {
			pos = posInfo[1];
		} else {
			lexiconInput.seek(posInfo[0]);
			while (lexiconInput.position() < fileLimit[indexFieldSequence]) {
				char[] term2 = lexiconInput.readUString();

				int cmp = compareKey(term2, singleTerm);

				if (cmp == 0) {
					pos = lexiconInput.readLong();
					if(logger.isDebugEnabled()){
						logger.debug("search success = {} at field-{}", new String(singleTerm.array, singleTerm.start, singleTerm.length),indexFieldSequence);
					}
					break;
				} else if (cmp > 0) {
					// if term value is greater than this term, there's no such
					// word.
					// search fail
					if(logger.isDebugEnabled()){
						logger.debug("search fail = {} at field-{}", new String(singleTerm.array, singleTerm.start, singleTerm.length), indexFieldSequence);
					}
					break;
				} else {
					// skip reading pos
					lexiconInput.seek(lexiconInput.position() + IOUtil.SIZE_OF_LONG);
				}
			}
		}

		if (pos >= 0) {
			
			return getTermDocs(indexFieldSequence, singleTerm, pos);
		}

		return null;
	}

	private PostingDocs getTermDocs(int indexFieldSequence, CharVector singleTerm, long pos) throws IOException{
		// tt = System.currentTimeMillis();
		postingInput.seek(pos);
		int len = postingInput.readVInt();
		int count = postingInput.readInt();
		int lastDocNo = postingInput.readInt();
	
		PostingDoc[] termDocList = new PostingDoc[count];
		
		int prevId = -1;
		int docId = -1;
		for (int i = 0; i < count; i++) {
			if (prevId >= 0) {
				docId = postingInput.readVInt() + prevId + 1;
			} else {
				docId = postingInput.readVInt();
			}
			
			int tf = postingInput.readVInt();
			int[] positions = null;
			if(tf > 0 && fieldIndexOptions[indexFieldSequence].isStorePosition()){
				int prevPosition = -1;
				positions = new int[tf];
				for (int j = 0; j < tf; j++) {
					if (prevPosition >= 0) {
						positions[j] = postingInput.readVInt() + prevPosition + 1;
					} else {
						positions[j] = postingInput.readVInt();
					}
					prevPosition = positions[j];
					
				}
				
			}
			
			termDocList[i] = new PostingDoc(docId, tf, positions);
			prevId = docId;
			
//			n++;
		}
		return new PostingDocs(indexFieldSequence, singleTerm, termDocList, count);
	}
	protected PostingDocs getExtendedPosting(int indexFieldSequence, CharVector singleTerm) throws IOException {

		// SUFFIX SEARCH
		if (singleTerm.array.length > 0 && singleTerm.array[0] == '*') {
			// 2012-02-09 swsong
			// 여러필드에 걸쳐서 prefix검색을 할때 두번째 필드부터는 singleTerm의 length가 변경된 상태여서 문제가
			// 생김.
			// 새로운 charVector를 만들어서 처리하는 것을 수정.
			CharVector cloneVector = singleTerm.clone();
			cloneVector.start++;
			cloneVector.length--;
			return getSuffixPosting(indexFieldSequence, cloneVector);
			// PREFIX SEARCH
		} else if (singleTerm.array[singleTerm.start + singleTerm.length - 1] == '*') {
			// remove last one
			CharVector cloneVector = singleTerm.clone();
			cloneVector.length--;
			return getPrefixPosting(indexFieldSequence, cloneVector);
		} else {
			// RANGE SEARCH
			int pos = 0;
			for (int i = singleTerm.start; i < singleTerm.length; i++, pos++) {
				char ch = singleTerm.array[i];
				if (ch == '~') {
					CharVector startTerm = (CharVector) singleTerm.clone();
					startTerm.length = pos;
					CharVector endTerm = (CharVector) singleTerm.clone();
					endTerm.length -= (pos + 1);
					endTerm.start = i + 1;
					// logger.debug("startCV = "+new String(startCV.array,
					// startCV.start, startCV.length));
					// logger.debug("endCV = "+new String(endCV.array,
					// endCV.start, endCV.length));

					// return getPosting(indexFieldSequence, startTerm);
					return getRangePosting(indexFieldSequence, startTerm, endTerm);
				}
			}

		}
		// logger.debug("IGNORE Extended Search!");
		// Treat as a whole-word non-extended search
		return getPosting(indexFieldSequence, singleTerm);
	}

	protected PostingDocs getPrefixPosting(int indexFieldSequence, CharVector singleTerm) throws IOException {
		if (memoryLexicon[indexFieldSequence].size() == 0)
			return null;

		long[] posInfo = new long[2];
		boolean found = memoryLexicon[indexFieldSequence].binsearch(singleTerm, posInfo);

		int foundCount = 0;
		long startPos = -1;

		long lexiconPos = posInfo[0];
		// logger.debug("lexiconPos = {}", lexiconPos);
		lexiconInput.seek(lexiconPos);

		while (lexiconInput.position() < fileLimit[indexFieldSequence]) {
			// lexiconInput
			char[] term2 = lexiconInput.readUString();
			int cmp = comparePrefixKey(term2, indexFieldSequence, singleTerm);
			// logger.debug("compare key "+new String(term2)+" = "+cmp);
			
			//작거나 같으면 prefix에 부합한다.2013-05-28 swsong.
    		if(cmp <= 0){
				long pos = lexiconInput.readLong();
				if (foundCount == 0){
					startPos = pos;
				}
				foundCount++;
				// logger.debug("prefix term = {}", new String(term2));

			} else if (cmp > 0) {
				// if term value is greater than this term, there's no suchword.
				// search fail
				// logger.debug("search finish! = "+new String(singleTerm.array,
				// singleTerm.start, singleTerm.length));
				break;
			} 
		}

		// logger.debug("startPos = {}, foundCount = {}", startPos, foundCount);
		return makeTermDocs(indexFieldSequence, singleTerm, startPos, foundCount);
	}

	protected PostingDocs getSuffixPosting(int indexFieldSequence, CharVector singleTerm) throws IOException {
		if (memoryLexicon[indexFieldSequence].size() == 0)
			return null;

		long[] posInfo = new long[2];
		boolean found = memoryLexicon[indexFieldSequence].binsearch(singleTerm, posInfo);

		int foundCount = 0;
		long startPos = -1;

		long lexiconPos = posInfo[0];
		// logger.debug("lexiconPos = {}", lexiconPos);
		lexiconInput.seek(lexiconPos);

		boolean isIncludSearch = false;

		if (singleTerm.array[singleTerm.start + singleTerm.length - 1] == '*') {
			singleTerm.length--;
			isIncludSearch = true;
		}

		while (lexiconInput.position() < fileLimit[indexFieldSequence]) {
			// lexiconInput
			char[] term2 = lexiconInput.readUString();
			int cmp = isIncludSearch ? compareIncludingKey(term2, indexFieldSequence, singleTerm) : compareSuffixKey(term2, indexFieldSequence, singleTerm);
			// logger.debug("compare key "+new String(term2)+" = "+cmp);
			if (cmp == 0) {
				long pos = lexiconInput.readLong();
				if (foundCount == 0)
					startPos = pos;
				foundCount++;
				// logger.debug("prefix term = {}", new String(term2));

			} else if (cmp > 0) {
				// if term value is greater than this term, there's no such
				// word.
				// search fail
				break;
			} else {
				// skip reading pos
				lexiconInput.seek(lexiconInput.position() + IOUtil.SIZE_OF_LONG);
				// retry!
			}
		}

		logger.debug("startPos = {}, foundCount = {}", startPos, foundCount);
		return makeTermDocs(indexFieldSequence, singleTerm, startPos, foundCount);
	}

	protected PostingDocs getRangePosting(int indexFieldSequence, CharVector startTerm, CharVector endTerm)
			throws IOException {
		if (memoryLexicon[indexFieldSequence].size() == 0)
			return null;
		logger.debug("Range : {} ~ {}", startTerm,  endTerm);

		int cmpValid = 0;
		char[] startTermChars = new char[startTerm.length];
		System.arraycopy(startTerm.array, startTerm.start, startTermChars, 0, startTerm.length);
		cmpValid = compareKey(startTermChars, endTerm);
		// ensure startTerm <= endTerm
		if (cmpValid > 0) {
			return new PostingDocs(indexFieldSequence, startTerm, 0);
		}

		/*
		 * 1. find startTerm
		 */
		long[] posInfo = new long[2];
		boolean found = memoryLexicon[indexFieldSequence].binsearch(startTerm, posInfo);

		// if(isNumericField[indexFieldSequence])
		// found = memoryLexicon[indexFieldSequence].binsearchNumeric(startTerm,
		// posInfo);
		// else
		// found = memoryLexicon[indexFieldSequence].binsearch(startTerm, posInfo);

		int foundCount = 0;
		long startPos = -1;

		long lexiconPos = posInfo[0];
		logger.debug("lexiconPos = {} / limit = {}", lexiconPos, fileLimit[indexFieldSequence]);
		lexiconInput.seek(lexiconPos);

		/*
		 * startTerm
		 */
		char[] term2 = null;
		int cmp = 0;

		// char[] prevTerm = null;
		// long prevPos = -1;
		boolean isStarted = false;

		while (lexiconInput.position() < fileLimit[indexFieldSequence]) {
			// lexiconInput
			term2 = lexiconInput.readUString();
			startPos = lexiconInput.readLong();
			// logger.debug("Test term = "+new String(term2));
			cmp = compareKey(term2, startTerm);

			if (cmp == 0) {
				// logger.debug("range start1 term = "+new String(term2));
				isStarted = true;
				// isMatched = true;
				foundCount++;
				break;
			} else if (cmp > 0) {
				// use prev term, because prev term is closest among the
				// smallers than this term.
				// term2 = prevTerm;
				// startPos = prevPos;
//				logger.debug("range start2 term = {}", new String(term2));
				isStarted = true;

				if (compareKey(term2, endTerm) == 0) {
					// 종료텀이 처음에 발견되었을때.
					foundCount++;
				}
				break;
			}
		}

		if (!isStarted)
			return null;

		// if(isMatched && startTerm.equals(endTerm)){
		// //시작과 끝이 같고, 일치하는 텀이 있다면. 하나발견된것임.
		// foundCount = 1;
		// }
		/*
		 * endTerm
		 */
		// logger.debug("while {} < {}",lexiconInput.position(),
		// fileLimit[indexFieldSequence] );
		while (lexiconInput.position() < fileLimit[indexFieldSequence]) {

			cmp = compareKey(term2, endTerm);
			// logger.debug("compareKey == {}, {}:{}", new Object[]{cmp, term2,
			// endTerm});
			if (cmp == 0) {
				logger.debug("range end term(inclusive) = {}", new String(term2));
				foundCount++;
				break;
			} else if (cmp > 0) {
				break;
			} else {
				// logger.debug("range term = "+new String(term2));
				foundCount++;
				// continue
			}
			// logger.debug("lexiconInput.position()="+lexiconInput.position());
			term2 = lexiconInput.readUString();
			lexiconInput.readLong();
			// lexiconInput.position(lexiconInput.position() +
			// IOUtil.SIZEOFLONG);
			// logger.debug("Next term = "+new String(term2)+", "+term2.length);
		}

		// logger.debug("startPos = "+startPos+", foundCount = "+foundCount);
		return makeTermDocs(indexFieldSequence, startTerm, startPos, foundCount);

	}

	// thread-unsafe! 호출하는 메서드에서 thread-safe하게 호출해야한다.
	int mpseq = 0;

	private PostingDocs makeTermDocs(int indexFieldSequence, CharVector term, long startPos, int foundCount) throws IOException {
		
		if (foundCount > 0) {
			FixedMinHeap<PostingDocsReader> heap = new FixedMinHeap<PostingDocsReader>(foundCount);
			mpseq++;

			long pos = startPos;
			postingInput.seek(pos);
			
			List<PostingDocs> termDocsList = new ArrayList<PostingDocs>(foundCount);
			
			for (int c = 0; c < foundCount; c++) {
				int prevId = -1;
				// 위치정보를 가지고 포스팅을 읽는다.
				int len = postingInput.readVInt();
				int count = postingInput.readInt();
				int lastDocNo = postingInput.readInt();

				// logger.debug("prefix posting {} / {}", c, foundCount);
//	    		logger.debug("prefix posting len = {}", len);
//	    		logger.debug("prefix posting count = {}", count);
//	    		logger.debug("prefix posting lastDocNo = {}", lastDocNo);

				PostingDoc[] termDocList = new PostingDoc[count];
				
				int docId = -1;
				
				for (int i = 0; i < count; i++) {
					if (prevId >= 0) {
						docId = postingInput.readVInt() + prevId + 1;
					} else {
						docId = postingInput.readVInt();
					}
					
					int tf = postingInput.readVInt();
					
					int[] positions = null;
					if(fieldIndexOptions[indexFieldSequence].isStorePosition()){
						int prevPosition = -1;
						positions = new int[tf];
						for (int j = 0; j < tf; j++) {
							if (prevPosition >= 0) {
								positions[j] = postingInput.readVInt() + prevPosition + 1;
							} else {
								positions[j] = postingInput.readVInt();
							}
							prevPosition = positions[j];
						}
						
					}
					
					termDocList[i] = new PostingDoc(docId, tf, positions);
					
					prevId = docId;

				}

//				TermDocsReader r = new TermDocsReader(new TermDocs(indexFieldSequence, term, termDocList, count));
				
				termDocsList.add(new PostingDocs(indexFieldSequence, term, termDocList, count));
//				if (r.next()) {
//					heap.push(r);
//				}

			}// for

			return new PostingDocsMerger(termDocsList).merge(indexFieldSequence, term, 1024);
		}
		return null;
	}

//	private void addTermDocs(TermDocs termDocs, int prevDocNo, int prevTfSum) {
//
//		if (termDocs.docs().length == termDocs.count()) {
//			int newLength = (int) (termDocs.docs().length * 1.2);
//			try {
//				int[] newDocs = new int[newLength];
//				int[] newTfs = new int[newLength];
//				System.arraycopy(termDocs.docs(), 0, newDocs, 0, termDocs.docs().length);
//				System.arraycopy(termDocs.tfs(), 0, newTfs, 0, termDocs.tfs().length);
//
//				termDocs.setDocs(newDocs);
//				termDocs.setTfs(newTfs);
//			} catch (OutOfMemoryError e) {
//				logger.error("OOM! while allocating memory size = " + newLength, e);
//				throw e;
//			}
//		}
//
//		int count = termDocs.count();
//		termDocs.docs()[count] = prevDocNo;
//		termDocs.tfs()[count] = prevTfSum;
//		count++;
//		termDocs.setCount(count);
//		// logger.info("termDoc count = "+count+" : "+prevDocNo+", "+prevTfSum);
//	}

	private int compareKey(char[] t, CharVector term) {

		int len1 = t.length;
		int len2 = term.length;

		int len = len1 < len2 ? len1 : len2;

		for (int i = 0; i < len; i++) {
			char ch = term.array[term.start + i];

			if (t[i] != ch) {
				return t[i] - ch;
			}
		}

		return len1 - len2;
	}

	private int comparePrefixKey(char[] t, int fieldSequence, CharVector term) {
		int len1 = t.length;
		int len2 = term.length;

		int len = len1 < len2 ? len1 : len2;

		for (int i = 0; i < len; i++) {
			char ch = term.array[term.start + i];

			if (t[i] != ch) {
				return t[i] - ch;
			}
		}

		// if come here, they're partially matched.
		if (len1 >= len2) {
			// term is prefix
			return 0;
		}
		// char[] t is prefix. we don't want this case.
		return len1 - len2;
	}

	private int compareSuffixKey(char[] t, int fieldSequence, CharVector term) {

		// enable to include term
		if (t.length >= term.length) {
			for (int i = 0, j = 0; i < t.length; i++) {
				if ((t.length - i) < j) {
					break;
				}
				if (t[t.length - 1 - i] == term.array[term.start + term.length - 1 - j]) {
					if (++j == term.length) {
						return 0;
					}
				} else {
					j = 0;
					continue;
				}
			}
		}
		return -1;
	}

	private int compareIncludingKey(char[] t, int fieldSequence, CharVector term) {

		// enable to include term
		if (t.length >= term.length) {
			for (int i = 0, j = 0; i < t.length; i++) {
				if ((t.length - i) < j) {
					break;
				}
				if (t[i] == term.array[term.start + j]) {
					if (++j == term.length) {
						return 0;
					}
				} else {
					j = 0;
					continue;
				}
			}
		}
		return -1;
	}

	private void quickSort(String[] e, int first, int last) {

		if (last <= 0)
			return;

		int stackMaxSize = (int) ((Math.log(last - first + 1) + 3) * 2);
		int[][] stack = new int[stackMaxSize][2];

		String pivotId = null;
		int sp = 0;
		int left = 0, right = 0;

		while (true) {
			while (first < last) {
				left = first;
				right = last;
				int median = (left + right) / 2;

				// move pivot to left most.
				String tmp = e[left];
				e[left] = e[median];
				e[median] = tmp;
				pivotId = e[left];

				while (left < right) {
					while (e[right].compareTo(pivotId) >= 0 && (left < right))
						right--;

					if (left != right) {
						e[left] = e[right];
						left++;
					}

					while (e[left].compareTo(pivotId) <= 0 && (left < right))
						left++;

					if (left != right) {
						e[right] = e[left];
						right--;
					}
				}

				e[left] = pivotId;

				if (left - first < last - left) {
					if (left + 1 < last) {
						sp++;
						stack[sp][0] = left + 1;
						stack[sp][1] = last;
					}
					last = left - 1;
				} else {
					if (first < left - 1) {
						sp++;
						stack[sp][0] = first;
						stack[sp][1] = left - 1;
					}
					first = left + 1;
				}

			}

			if (sp == 0) {
				return;
			} else {
				first = stack[sp][0];
				last = stack[sp][1];
				sp--;
			}

		}
	}

	private int removeRedundancy(String[] list, int length) {
		String prev = "";
		int c = 0;
		for (int i = 0; i < length; i++) {
			list[i] = list[i].trim();
			String str = list[i];
			if (!str.equalsIgnoreCase(prev) && prev.length() > 0) {
				list[c++] = prev;
			}
			prev = str;
		}
		if (prev.length() > 0)
			list[c++] = prev;

		return c;
	}

	public void close() throws IOException {
		lexiconInput.close();
		postingInput.close();
		pkReader.close();
		for (int i = 0; i < queryTokenizerPool.length; i++) {
			queryTokenizerPool[i] = null;
		}
	}
}
