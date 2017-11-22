package org.fastcatsearch.ir.search;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.util.BytesRef;
import org.fastcatsearch.error.CoreErrorCode;
import org.fastcatsearch.error.SearchAbortError;
import org.fastcatsearch.error.SearchError;
import org.fastcatsearch.ir.analysis.AnalyzerPool;
import org.fastcatsearch.ir.common.IRException;
import org.fastcatsearch.ir.common.SettingException;
import org.fastcatsearch.ir.document.Document;
import org.fastcatsearch.ir.field.*;
import org.fastcatsearch.ir.filter.FilterException;
import org.fastcatsearch.ir.group.GroupDataMerger;
import org.fastcatsearch.ir.group.GroupHit;
import org.fastcatsearch.ir.group.GroupsData;
import org.fastcatsearch.ir.io.BitSet;
import org.fastcatsearch.ir.io.*;
import org.fastcatsearch.ir.query.*;
import org.fastcatsearch.ir.query.Term.Option;
import org.fastcatsearch.ir.search.clause.Clause;
import org.fastcatsearch.ir.search.clause.ClauseException;
import org.fastcatsearch.ir.settings.FieldSetting;
import org.fastcatsearch.ir.settings.FieldSetting.Type;
import org.fastcatsearch.ir.settings.Schema;
import org.fastcatsearch.ir.summary.BasicHighlightAndSummary;
import org.fastcatsearch.ir.util.Formatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;

public class CollectionSearcher implements Cloneable {
	private static Logger logger = LoggerFactory.getLogger(CollectionSearcher.class);

	private String collectionId;
	private CollectionHandler collectionHandler;

	private HighlightAndSummary has;

    private int bundleMemMaxCountLimit = 10 * 10000;
    private int bundleHashBucketSize = 100 * 10000;

    private boolean isAborted;

	public CollectionSearcher(CollectionHandler collectionHandler) {
		this.collectionId = collectionHandler.collectionId();
		this.collectionHandler = collectionHandler;
		has = new BasicHighlightAndSummary();

        //묶음검색 파일기반 해시셋 설정값.
        String bundleMemMaxCount = System.getProperty("bundleMemMaxCount");
        String bundleHashBucket = System.getProperty("bundleHashBucket");
        if(bundleMemMaxCount != null) {
            bundleMemMaxCountLimit = Integer.parseInt(bundleMemMaxCount);
        }
        if(bundleHashBucket != null) {
            bundleHashBucketSize = Integer.parseInt(bundleHashBucket);
        }
	}

    public void abort() {
        isAborted = true;
    }

    private void checkAborted() throws SearchAbortError {
        if(isAborted) {
            throw new SearchAbortError();
        }
    }
    private void checkAborted(String message) throws SearchAbortError {
        if(isAborted) {
            throw new SearchAbortError(message);
        }
    }
    public CollectionSearcher clone() {
        try {
            CollectionSearcher searcher = (CollectionSearcher) super.clone();
            searcher.isAborted = false;
            return searcher;
        } catch (CloneNotSupportedException e) {
            logger.error("", e);
        }
        return null;
    }
    /*
    * 결합 pk는 ;로 구분되어 있다.
    * */
    public Document getIndexableDocumentByPk(String pkValue) throws IOException, FieldDataParseException {
        int segmentSize = collectionHandler.segmentSize();
        if (segmentSize == 0) {
            return null;
        }

        BytesDataOutput tempOutput = new BytesDataOutput();
        //여러세그먼트에서 찾아본다.
        for(SegmentReader segmentReader : collectionHandler.segmentReaders()) {
            SearchIndexesReader searchIndexesReader = segmentReader.newSearchIndexesReader();
            try {
                int docNo = searchIndexesReader.getPrimaryKeyIndexesReader().getDocNo(pkValue, tempOutput);
                if (docNo != -1) {
                    //찾지못함.
                    //삭제문서가 아니면 리턴한다.
                    if (!segmentReader.deleteSet().isSet(docNo)) {
                        return segmentReader.segmentSearcher().getIndexableDocument(docNo);
                    }
                }
            } finally {
                searchIndexesReader.close();
            }
        }

        return null;
    }
	public GroupsData doGrouping(Query q) throws Exception {
		
		int segmentSize = collectionHandler.segmentSize();
		if (segmentSize == 0) {
			throw new SearchError(CoreErrorCode.COLLECTION_NOT_INDEXED, collectionId);
		}

		Groups groups = q.getGroups();

		if (groups == null) {
			return null;
		}

		if (segmentSize == 1) {
			// 머징필요없음.
            GroupHit groupHit = collectionHandler.getFirstSegmentSearcher().searchGroupHit(q);
            return groupHit.groupData();
		} else {

			GroupDataMerger dataMerger = null;
			if (groups != null) {
				dataMerger = new GroupDataMerger(groups, segmentSize);
			}

            for(SegmentReader r : collectionHandler.segmentReaders()) {
                GroupHit groupHit = r.segmentSearcher().searchGroupHit(q);
                if (dataMerger != null) {
                    dataMerger.put(groupHit.groupData());
                }
            }

			GroupsData groupData = null;
			if (dataMerger != null) {
				groupData = dataMerger.merge();
			}
			return groupData;
		}

	}

	public Document requestDocument(String segmentId, int docNo) throws IOException {
        SegmentReader reader = collectionHandler.segmentReader(segmentId);
		if(reader != null) {
			return reader.segmentSearcher().getDocument(docNo);
		}
		return null;
	}

	public InternalSearchResult searchInternal(Query q) throws IRException, IOException, SettingException {
		return searchInternal(q, false, null);
	}
	
	/**
	 * @param forMerging : 머징용도이면 start + length 만큼을 앞에서부터 모두 가져온다. 
	 * */
	public InternalSearchResult searchInternal(Query q, boolean forMerging) throws IRException, IOException, SettingException {
		return searchInternal(q, forMerging, null);
	}
	
	public InternalSearchResult searchInternal(Query q, boolean forMerging, PkScoreList boostList) throws IRException, IOException, SettingException {
		int segmentSize = collectionHandler.segmentSize();
		if (segmentSize == 0) {
			throw new SearchError(CoreErrorCode.COLLECTION_NOT_INDEXED, collectionId);
		}
        checkAborted();
		/*
		 * 중요!! 레퍼런스를 복사하여 세그먼트가 검색도중 동적으로 삭제되어도 문제없도록 한다.
		 * 2016-2-2 swsong
		 */
		TreeSet<SegmentReader> segmentReaders = new TreeSet<SegmentReader>(collectionHandler.segmentReaders());
		segmentSize = segmentReaders.size();

//		logger.debug("searchInternal incrementCount > {} ", q);
		collectionHandler.queryCounter().incrementCount();
		
		Schema schema = collectionHandler.schema();
		
		Metadata meta = q.getMeta();
		int start = meta.start();
		int rows = meta.rows();

		int sortMaxSize = start + rows - 1;
		int resultRows = rows;
		if (forMerging) {// 앞에서 부터 모두.
			resultRows = sortMaxSize;
		}

		Groups groups = q.getGroups();

		Sorts sorts = q.getSorts();
		FixedMaxPriorityQueue<HitElement> ranker = null;
		if (sorts == null) {
			ranker = new DefaultRanker(sortMaxSize);
		} else {
			// ranker에 정렬 로직이 담겨있다.
			// ranker 안에는 필드타입과 정렬옵션을 확인하여 적합한 byte[] 비교를 수행한다.
			ranker = sorts.createRanker(schema, sortMaxSize);
		}

		GroupDataMerger dataMerger = null;
		if (groups != null) {
			dataMerger = new GroupDataMerger(groups, segmentSize);
		}

		HighlightInfo highlightInfo = null;

		int totalSize = 0;
		// bundleKeySet 는 동일그룹갯수를 확인하는 용도이다.
        // 묶음검색에서는 전체 문서갯수가 아닌 묶음의 갯수가 총 결과갯수가 되므로, 그룹중복을 제거하여 계산해주어야 한다.
        // 32byte의 key를 HashSet에 넣었을때 100만개에 100MB, 1000만개에 1G 정도 메모리 소요.
        // 대부분 100만개 이하일 것이므로, 메모리에서 수행하도록 한다.
//		Set<BytesRef> bundleKeySet = new HashSet<BytesRef>();
        int keySize = 0;
        HybridHashSet bundleKeySet = new HybridHashSet(bundleMemMaxCountLimit, bundleHashBucketSize, keySize);
		List<Explanation> explanationList = null;
		BitSet[] segmentDocHitSetList = null;

		try {
            segmentDocHitSetList = new BitSet[segmentSize];
            Iterator<SegmentReader> iterator = segmentReaders.iterator();

			for(int i = 0; iterator.hasNext(); i++) {

                checkAborted();

				// segment 의 모든 결과를 보아야 중복체크가 가능하므로 reader를 받아오도록 한다.
				HitReader hitReader = iterator.next().segmentSearcher().searchHitReader(q, boostList);
				//
				//
				//FIXME highlightInfo 계속 덮어쓰나?
				//
				if (highlightInfo == null) {
					highlightInfo = hitReader.highlightInfo();
				}

				segmentDocHitSetList[i] = new BitSet();
				// posting data
				HitElement e = null;
				while ((e = hitReader.next()) != null) {

                    checkAborted();

					if (e.getBundleKey() != null) {
                        if(keySize == 0) {
                            keySize = e.getBundleKey().length();
                            bundleKeySet.setKeySize(keySize);
                        }
						segmentDocHitSetList[i].set(e.docNo());
						if(bundleKeySet.add(e.getBundleKey())) {
							totalSize++;
						}
					} else {
						totalSize++;
					}
					ranker.push(e);
//					logger.debug("heap insert hit > {}", e.docNo());
				}
				
				// Put GroupResult
				if (dataMerger != null) {
					dataMerger.put(hitReader.makeGroupData());
				}
				
				if(hitReader.explanation() != null){
					if(explanationList == null){
						explanationList = new ArrayList<Explanation>();
					}
					hitReader.explanation().setSegmentId(i);
					hitReader.explanation().setCollectionId(collectionId);
					explanationList.add(hitReader.explanation());
				}
			}
			
        } catch (FilterException e) {
            throw new IRException(e);
		} catch (ClauseException e) {
			throw new IRException(e);
		} finally {
            if(bundleKeySet != null) {
                bundleKeySet.clean();
            }
        }


		int rankerSize = ranker.size();
//		logger.debug("PAGE start={}, size={}", start, rankerSize);
		
		FixedHitStack hitStack = new FixedHitStack(rankerSize);
		for (int i = 1; i <= rankerSize; i++) {
			HitElement el = ranker.pop();
			hitStack.push(el);
		}
		
		int c = 1;
		FixedHitReader fixedHitReader = hitStack.getReader();
		FixedHitQueue totalHit = new FixedHitQueue(resultRows);
		while(fixedHitReader.next()) {
            checkAborted();
			HitElement el = fixedHitReader.read();
//			logger.debug("{} rank hit seg#{} {}", c, el.segmentSequence(), el.docNo(), el.score(), el.rowExplanations());
			
			if (forMerging) {
				//머징용도는 처음부터 모두 넣는다.
				//번들키가 없거나 totalHit에 존재하지 않을때만 추가하고 나머지는 버린다.
				totalHit.push(el);
			} else if (c >= start) {
				//차후 머징용도가 아니라면 start이후 부터만 가져온다. 
//				logger.debug("insert#{} > {}", c, el.docNo());
				totalHit.push(el);
			}
			c++;
		}

        checkAborted();

		GroupsData groupData = null;
		if (dataMerger != null) {
			groupData = dataMerger.merge();
		}
		
		HitElement[] hitElementList = totalHit.getHitElementList();
		int realSize = totalHit.size();
		/*
		 * 번들 요청이 있으면 하위 묶음문서를 찾아온다.
		 * */
		Bundle bundle = q.getBundle();
		if(bundle != null) {
			//검색결과의 hit내에서만 검색되도록 해야하므로, bitSet으로 filtering한다.
			fillBundleResult(schema, segmentReaders, hitElementList, realSize, bundle, segmentDocHitSetList);
		}
		return new InternalSearchResult(collectionId, hitElementList, realSize, totalSize, groupData, highlightInfo, explanationList);
	}
	
	/*
	 * 번들 문서를 찾아온다.
	 * */
	private void fillBundleResult(Schema schema, TreeSet<SegmentReader> segmentReaders, HitElement[] hitElementList, int size, Bundle bundle, BitSet[] segmentDocFilterList) throws IRException, IOException {

        checkAborted();

		/*
		 * el의 bundlekey를 보고 하위 묶음문서가 몇개가 있는지 확인한다.
		 * 2개 이상일 경우만 저장하고 나머지는 버린다.
		 */
		String fieldIndexId = bundle.getFieldIndexId();
		Sorts bundleSorts = bundle.getSorts();
		int bundleRows = bundle.getRows();
		int bundleStart = 1;
        int bundleOption = bundle.getOption();
        boolean isParentInclude = ( bundleOption == Bundle.OPT_PARENT_INCLUDE );

		FieldSetting bundleFieldSetting = schema.getFieldSetting(bundle.getFieldIndexId());
		Type bundleFieldType = bundleFieldSetting.getType();
		

        int segmentSize = segmentReaders.size();
        for (int k = 0; k < size; k++) {

            checkAborted();

            int totalSize = 0;
            //bundleKey로 clause생성한다.
            int mainDocNo = hitElementList[k].docNo();
            BytesRef bundleKey = hitElementList[k].getBundleKey();
            if(bundleKey == null) {
                continue;
            }
            String bundleStringKey = Formatter.getContentString(bundleKey, bundleFieldType);

            if(bundleStringKey == null) {
                continue;
            }

            Clause bundleClause = new Clause(new Term(fieldIndexId, bundleStringKey));
            Hit[] segmentHitList = new Hit[segmentSize];

            Iterator<SegmentReader> iterator = segmentReaders.iterator();
            for(int i = 0; iterator.hasNext(); i++) {

                checkAborted();

                //bundle key 별로 결과를 모은다.
                try {
                    segmentHitList[i] = iterator.next().segmentSearcher().searchBundleIndex(bundleClause, bundleSorts, bundleStart, bundleRows, segmentDocFilterList[i]);
                } catch (Throwable e) {
                    logger.error("bundle search error", e);
                    logger.error("---- [{}]", i);
                    for(SegmentReader r : segmentReaders) {
                        logger.error("> {}", r.segmentId());
                    }
                    logger.error("segmentReaders size = {} >> {}", segmentReaders.size(), segmentReaders);
                    logger.error("segmentSize = {}", segmentSize);
                    logger.error("segmentHitList.len = {}", segmentHitList.length);
                    logger.error("segmentDocFilterList.len = {}", segmentDocFilterList.length);
                }
                totalSize += segmentHitList[i].totalCount();
            }

            //2이상이어야만 번들이 유효하다.
            if(totalSize > 1) {

                FixedMinHeap<FixedHitReader> hitMerger = null;
                if (bundleSorts != null) {
                    hitMerger = bundleSorts.createMerger(schema, segmentSize);
                } else {
                    hitMerger = new FixedMinHeap<FixedHitReader>(segmentSize);
                }

                for (int i = 0; i < segmentSize; i++) {
                    FixedHitReader hitReader = segmentHitList[i].hitStack().getReader();
//					// posting data
                    if (hitReader.next()) {
                        hitMerger.push(hitReader);
                    }
                }

                int realSize = Math.min(bundleRows, totalSize);
                DocIdList bundleDocIdList = new DocIdList(realSize);
                int c = 1, n = 0;
                while (hitMerger.size() > 0) {

                    checkAborted();

                    FixedHitReader r = hitMerger.peek();
                    HitElement el = r.read();

                    //mainDocNo 와 동일한 문서는 제외한다.
                    //group 문서들에서 그룹대표 문서와 동일한 것은 보여주지 않는다.
                    //단, 대표문서를 포함옵션이 있다면 추가한다.
                    if(isParentInclude || el.docNo() != mainDocNo) {
                        if (c >= bundleStart) {
                            bundleDocIdList.add(el.segmentId(), el.docNo());
                            n++;
                            //logger.debug("[{}] {}", el.segmentSequence() ,el.docNo());
                        }
                        c++;

                    }

                    // 결과가 만들어졌으면 일찍 끝낸다.
                    if (n == bundleRows) {
                        break;
                    }

                    if (!r.next()) {
                        // 다 읽은 것은 버린다.
                        hitMerger.pop();
                    }
                    hitMerger.heapify();
                }

                //대표가 포함되지 않으면, 갯수를 줄인다.
                if(!isParentInclude) {
                    totalSize--;
                }
                hitElementList[k].setBundleDocIdList(bundleDocIdList);
                hitElementList[k].setTotalBundleSize(totalSize);
            }


        }

	}

	public DocumentResult searchDocument(DocIdList list, ViewContainer views, String[] tags, HighlightInfo highlightInfo) throws IOException {
		int realSize = list.size();
		Row[] row = new Row[realSize];
		Row[][] bundleRow = null;

		int fieldSize = collectionHandler.schema().getFieldSize();
		int viewSize = views.size();
		int[] fieldSequenceList = new int[viewSize];
		String[] fieldIdList = new String[viewSize];
		boolean[] fieldSelectOption = new boolean[fieldSize]; // true인 index의 필드값만 채워진다.
		for (int i = 0; i < views.size(); i++) {
			View v = views.get(i);
			String fieldId = v.fieldId();
			fieldIdList[i] = fieldId;
			int sequence = -1;
			if (fieldId.equalsIgnoreCase(ScoreField.fieldName)) {
				sequence = ScoreField.fieldNumber;
			} else if (fieldId.equalsIgnoreCase(DocNoField.fieldName)) {
				sequence = DocNoField.fieldNumber;
			} else {
				sequence = collectionHandler.schema().getFieldSequence(fieldId);
				if(sequence != -1){
					fieldSelectOption[sequence] = true;
				}
			}

			fieldSequenceList[i] = sequence;
		}

		Document[] eachDocList = new Document[realSize];
		Document[][] eachBundleDocList = null;

        Map<String, SegmentSearcher> segmentSearchMap = new HashMap<String, SegmentSearcher>();
		int idx = 0;
		for (int i = 0; i < list.size(); i++) {

            checkAborted();

			String segmentId = list.segmentId(i);
			int docNo = list.docNo(i);
			DocIdList bundleDocIdList = list.bundleDocIdList(i);

            SegmentSearcher segmentSearcher = segmentSearchMap.get(segmentId);
			if(segmentSearcher == null) {
				SegmentReader segmentReader = collectionHandler.segmentReader(segmentId);
				if(segmentReader != null) {
					segmentSearcher = segmentReader.segmentSearcher();
				} else {
					segmentReader = collectionHandler.getTmpSegmentReader(segmentId);
					if(segmentReader != null) {
						segmentSearcher = segmentReader.segmentSearcher();
					}
				}
				if(segmentSearcher != null) {
					segmentSearchMap.put(segmentId, segmentSearcher);
				} else {
					//찾지못함.
					throw new IOException("Cannot find segment = " + segmentId);
				}

			}
			Document doc = segmentSearcher.getDocument(docNo, fieldSelectOption);
			eachDocList[idx] = doc;
			
			if(bundleDocIdList != null) {
				//묶음문서 존재시에만 생성한다.
				if(eachBundleDocList == null) {
					eachBundleDocList = new Document[realSize][];
				}
				Document[] bundleDoclist = new Document[bundleDocIdList.size()];
				for (int j = 0; j < bundleDocIdList.size(); j++) {

                    checkAborted();

					String bundleSegmentId = bundleDocIdList.segmentId(j);
					int bundleDocNo = bundleDocIdList.docNo(j);

					segmentSearcher = segmentSearchMap.get(bundleSegmentId);
					if(segmentSearcher == null) {
						SegmentReader segmentReader = collectionHandler.segmentReader(bundleSegmentId);
						if(segmentReader != null) {
							segmentSearcher = segmentReader.segmentSearcher();
						} else {
							segmentReader = collectionHandler.getTmpSegmentReader(bundleSegmentId);
							if(segmentReader != null) {
								segmentSearcher = segmentReader.segmentSearcher();
							}
						}
						if(segmentSearcher != null) {
							segmentSearchMap.put(bundleSegmentId, segmentSearcher);
						} else {
							//찾지못함.
							throw new IOException("Cannot find bundled segment = " + bundleSegmentId);
						}
					}

					Document bundleDoc = segmentSearcher.getDocument(bundleDocNo, fieldSelectOption);
					bundleDoclist[j] = bundleDoc;
				}
				eachBundleDocList[idx] = bundleDoclist;
			}
			
			idx++;
		}
		
		
		for (int i = 0; i < realSize; i++) {
			row[i] = makeRowFromDocument(eachDocList[i], views, fieldSequenceList, tags, highlightInfo);
			
			//bundle document
			if(eachBundleDocList != null) {
				Document[] bundleDocList = eachBundleDocList[i];
				if(bundleDocList != null) {
					///묶음문서 존재시에만 bundleRow를 생성한다.
					if(bundleRow == null) {
						bundleRow = new Row[realSize][];
					}
					bundleRow[i] = new Row[bundleDocList.length];
					for (int j = 0; j < bundleDocList.length; j++) {
						bundleRow[i][j] = makeRowFromDocument(bundleDocList[j], views, fieldSequenceList, tags, highlightInfo);
					}
				}
			}
			
		}
		return new DocumentResult(row, bundleRow, fieldIdList);
	}

	private Row makeRowFromDocument(Document document, ViewContainer views, int[] fieldSequenceList, String[] tags, HighlightInfo highlightInfo) throws IOException {
		Row rows = new Row(views.size());
		for (int j = 0; j < views.size(); j++) {
			View view = views.get(j);

			int fieldSequence = fieldSequenceList[j];
			if (fieldSequence == ScoreField.fieldNumber) {
				//여기서는 score를 알수가 없으므로 공백처리.
				//float score = document.getScore();
				rows.put(j, null);
			} else if (fieldSequence == DocNoField.fieldNumber) {
				rows.put(j, Integer.toString(document.getDocId()).toCharArray());
			} else if (fieldSequence == UnknownField.fieldNumber) {
				rows.put(j, UnknownField.value().toCharArray());
			} else {
				Field field = document.get(fieldSequence);
//				logger.debug("field#{} >> {}", j, field);
				String text = null;
				if (field != null) {
					text = field.toString();
				}

				boolean isHighlightSummary = false;
				if (has != null && text != null && highlightInfo != null) {
					//하이라이팅만 수행하거나, 또는 view.snippetSize 가 존재하면 summary까지 수행될수 있다.
					String fieldId = view.fieldId();
					Option searchOption = highlightInfo.getOption(fieldId);
					if(searchOption.useHighlight()) {
						String indexAnalyzerId = highlightInfo.getIndexAnalyzerId(fieldId);
						String queryAnalyzerId = highlightInfo.getQueryAnalyzerId(fieldId);
						String queryTerm = highlightInfo.getQueryTerm(fieldId);
						if (indexAnalyzerId != null && queryAnalyzerId != null && queryTerm != null) {
//							a = System.nanoTime();
							text = getHighlightedSnippet(fieldId, text, indexAnalyzerId, queryAnalyzerId, queryTerm, tags, view, searchOption);
//							b += (System.nanoTime() - a);
							isHighlightSummary = true;
						}
					}
				}
				
				if(!isHighlightSummary && view.isSummarize()){
					//검색필드가 아니라서 하이라이팅이 불가능한경우는 앞에서부터 잘라 summary 해준다.
					if(text != null){
						if(text.length() > view.snippetSize()){
							text = text.substring(0, view.snippetSize());
						}
					}
				}

				if (text != null) {
					rows.put(j, text.toCharArray());
				} else {
					rows.put(j, null);
				}

			}
		}
		
		return rows;
	}
	private String getHighlightedSnippet(String fieldId, String text, String indexAnalyzerId, String queryAnalyzerId, String queryString, String[] tags, View view, Option searchOption) throws IOException {
		AnalyzerPool queryAnalyzerPool = collectionHandler.analyzerPoolManager().getPool(queryAnalyzerId);
		//analyzer id 가 같으면 하나만 공통으로 사용한다.
		boolean isSamePool = queryAnalyzerId.equals(indexAnalyzerId);
		AnalyzerPool indexAnalyzerPool = null;
		
		if(isSamePool){
			indexAnalyzerPool = queryAnalyzerPool;
		}else{
			indexAnalyzerPool = collectionHandler.analyzerPoolManager().getPool(indexAnalyzerId);
		}
		
		if (queryAnalyzerPool != null) {
			Analyzer queryAnalyzer = queryAnalyzerPool.getFromPool();
			Analyzer indexAnalyzer = null;
			if(isSamePool){
				indexAnalyzer = queryAnalyzer;
			}else{
				indexAnalyzer = indexAnalyzerPool.getFromPool();
			}
			
			if (indexAnalyzer != null && queryAnalyzer != null) {
				try {
					if (searchOption.useForceHighlight()) {
						String[] queryStringArr = queryString.split(" ");
						HashSet<String> queryStringList = new HashSet<String>();
						for (String temp : queryStringArr) {
							if (temp.trim().length() != 0) {
								queryStringList.add(temp);
							}
						}

						//text = text.replaceAll(tags[0], "");
						//text = text.replaceAll(tags[1], "");
						for (String temp : queryStringList) {
							text = text.replaceAll(temp, tags[0] + temp + tags[1]);
							text = text.replaceAll(tags[0] + tags[0] + temp + tags[1] + tags[1], tags[0] + temp + tags[1]);
						}
					} else {
						text = has.highlight(fieldId, indexAnalyzer, queryAnalyzer, text, queryString, tags, view.snippetSize(), view.fragmentSize(), searchOption);
					}
				} finally {
					if(!isSamePool){
						indexAnalyzerPool.releaseToPool(indexAnalyzer);
					}
					queryAnalyzerPool.releaseToPool(queryAnalyzer);
				}
			}
		}

		return text;
	}
}
