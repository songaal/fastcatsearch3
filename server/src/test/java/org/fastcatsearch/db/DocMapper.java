//package db;
//
//import java.util.ArrayList;
//import java.util.Collections;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//
//import models.DescriptionModel;
//import models.DocChapterModel;
//import models.DocModel;
//
//import org.apache.ibatis.annotations.Param;
//import org.apache.ibatis.session.SqlSession;
//import org.apache.ibatis.session.SqlSessionFactory;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//
//public class DocMapper implements IDocMapper {
//	Logger logger = LoggerFactory.getLogger(DocMapper.class);
//
//	public SqlSessionFactory sf = null;
//
//	public DocMapper(SqlSessionFactory sf) {
//		this.sf = sf;
//	}
//
//	@Override
//	public DocModel selectDoc(@Param int docNo) {
//		SqlSession session = sf.openSession();
//		try {
//			DocModel mp = new DocModel();
//			mp.docNo = docNo;
//			return session.selectOne("docMapper.selectDoc", mp);
//		} catch (Exception e) {
//			e.printStackTrace();
//			logger.error("selectOneDoc", e);
//		} finally {
//			session.clearCache();
//			session.close();
//		}
//		return null;
//	}
//
//	@Override
//	public List<DocModel> selectDoc(int desc_id, int pageNo, int limit) {
//		// TODO Auto-generated method stub
//		return null;
//	}
//
//	@Override
//	public List<DocModel> selectDocType(int desc_id) {
//		SqlSession session = sf.openSession();
//		List<DocModel> result = Collections.emptyList();
//		try {
//			DocModel dm = new DocModel();
//			dm.desc_id = desc_id;
//			result = session.selectList("docMapper.selectDoc", dm);
//		} catch (Exception e) {
//			e.printStackTrace();
//		} finally {
//			session.clearCache();
//			session.close();
//		}
//		return result;
//	}
//
//	@Override
//	public int createDoc(DocModel model) {
//		SqlSession session = sf.openSession();
//		try {
//			model.priorityNo = Integer.MAX_VALUE;
//			session.insert("docMapper.createDoc", model);
//			return model.docNo;
//		} catch (Exception e) {
//			logger.error(e.getMessage(), e);
//			session.rollback();
//		} finally {
//			session.clearCache();
//			session.commit(true);
//			session.close();
//		}
//		return 0;
//	}
//
//	@Override
//	public int createDoc(int desc_id, String title, String content, String version, String createUser) {
//		DocModel model = new DocModel();
//		model.ti = title;
//		model.desc_id = desc_id;
//		model.content = content;
//		model.createUser = createUser;
//		model.version = version;
//		return createDoc(model);
//	}
//
//	@Override
//	public void updateDoc(int docNo, int desc_id, String title, String content, String version, String updateUser) {
//		SqlSession session = sf.openSession();
//		int result = -1;
//		try {
//			DocModel mp = new DocModel();
//			mp.docNo = docNo;
//			mp.ti = title;
//			mp.desc_id = desc_id;
//			mp.content = content;
//			mp.lastModifyUser = updateUser;
//			mp.version = version;
//			result = session.update("docMapper.updateDoc", mp);
//		} catch (Exception e) {
//			logger.error(e.getMessage(), e);
//			session.rollback();
//		} finally {
//			session.clearCache();
//			session.commit(true);
//			session.close();
//		}
//	}
//
//	@Override
//	public void deleteDoc(int docNo) {
//		SqlSession session = sf.openSession();
//		try {
//			DocModel mp = new DocModel();
//			mp.docNo = docNo;
//			session.delete("docMapper.deleteDoc", mp);
//		} catch (Exception e) {
//			logger.error(e.getMessage(), e);
//			session.rollback();
//		} finally {
//			session.clearCache();
//			session.commit(true);
//			session.close();
//		}
//
//	}
//
//	public List<DescriptionModel> selectDescription() {
//		SqlSession session = sf.openSession();
//		List<DescriptionModel> result = new ArrayList<DescriptionModel>();
//		try {
//			result = session.selectList("docMapper.selectDesripction");
//		} catch (Exception e) {
//			logger.error(e.getMessage(), e);
//		} finally {
//			session.clearCache();
//			session.close();
//		}
//		return result;
//	}
//
//	@Override
//	public List<DocChapterModel> selectDocChapter(int docNo) {
//		SqlSession session = sf.openSession();
//		DocChapterModel mp = new DocChapterModel();
//		mp.seq = 0;
//		mp.docNo = docNo;
//		List<DocChapterModel> result = Collections.emptyList();
//		try {
//			result = session.selectList("docMapper.selectChapterDoc", mp);
//		} catch (Exception e) {
//			logger.error(e.getMessage(), e);
//		} finally {
//			session.clearCache();
//			session.close();
//		}
//		return result;
//	}
//
//	@Override
//	public DocChapterModel selectChapter(int seq) {
//		SqlSession session = sf.openSession();
//		try {
//			DocChapterModel mp = new DocChapterModel();
//			mp.seq = seq;
//			mp.docNo = 0;
//			return session.selectOne("docMapper.selectChapterDoc", mp);
//		} catch (Exception e) {
//			logger.error(e.getMessage(), e);
//		} finally {
//			session.clearCache();
//			session.close();
//		}
//		return null;
//	}
//
//	@Override
//	public int createChapter(int chapterNo, int docNo, String chapterName, String createUser) {
//		SqlSession session = sf.openSession();
//		int result = -1;
//		try {
//			DocChapterModel mp = new DocChapterModel();
//			mp.chapterNo = chapterNo;
//			mp.docNo = docNo;
//			mp.chapterName = chapterName;
//			mp.createUser = createUser;
//			mp.lastModifyUser = createUser;
//			// System.out.println("====createChapter=================");
//			// System.out.println("docNo " + docNo);
//			// System.out.println("chapterNo " + chapterNo);
//			// System.out.println("chapterName " + chapterName);
//			// System.out.println("before insert chapter seq number: " +
//			// mp.seq);
//			result = session.insert("docMapper.createChapterDoc", mp);
//			// System.out.println("after chapter seq number: " + mp.seq);
//			return mp.seq;
//		} catch (Exception e) {
//			e.printStackTrace();
//			logger.error(e.getMessage(), e);
//			session.rollback();
//		} finally {
//			session.clearCache();
//			session.commit();
//			session.close();
//		}
//		return result;
//	}
//
//	@Override
//	public int updateChapterList(int seq, int chapterNo, int docNo, String chapterName, String modifyUser) {
//		SqlSession session = sf.openSession();
//		int result = -1;
//		try {
//			DocChapterModel mp = new DocChapterModel();
//			mp.seq = seq;
//			mp.chapterNo = chapterNo;
//			mp.docNo = docNo;
//			mp.chapterName = chapterName;
//			mp.lastModifyUser = modifyUser;
//			result = session.update("docMapper.updateChapterList", mp);
//			session.commit();
//		} catch (Exception e) {
//			logger.error(e.getMessage(), e);
//			session.rollback();
//		} finally {
//			session.clearCache();
//			session.commit(true);
//			session.close();
//		}
//		return result;
//	}
//
//	@Override
//	public int deleteChapterDocNotInclude(int docNo, int[] seqs) {
//		SqlSession session = sf.openSession();
//		int result = -1;
//		try {
//			DocChapterModel mp = new DocChapterModel();
//			// mp.seq = seq;
//			mp.docNo = docNo;
//			mp.seqs = seqs;
//			result = session.delete("docMapper.deleteChapterDocNotInclude", mp);
//			session.commit();
//		} catch (Exception e) {
//			logger.error(e.getMessage(), e);
//			session.rollback();
//		} finally {
//			session.clearCache();
//			session.commit(true);
//			session.close();
//		}
//		return result;
//	}
//
//	@Override
//	public int updateChapterContent(int docNo, int seq, String content, String lastUser) {
//		SqlSession session = sf.openSession();
//		int result = -1;
//		try {
//			DocChapterModel mp = new DocChapterModel();
//			mp.docNo = docNo;
//			mp.seq = seq;
//			mp.content = content;
//			mp.lastModifyUser = lastUser;
//			result = session.delete("docMapper.updateChapter", mp);
//			session.commit();
//		} catch (Exception e) {
//			logger.error(e.getMessage(), e);
//			session.rollback();
//		} finally {
//			session.clearCache();
//			session.commit(true);
//			session.close();
//		}
//		return result;
//
//	}
//
//	@Override
//	public void deleteDocChapter(int docNo) {
//		SqlSession session = sf.openSession();
//		try {
//			DocChapterModel mp = new DocChapterModel();
//			mp.docNo = docNo;
//			System.out.println("deleteDocChapter docNo:" + docNo);
//			session.delete("docMapper.deleteChapterDoc", mp);
//		} catch (Exception e) {
//			e.printStackTrace();
//			logger.error(e.getMessage(), e);
//			session.rollback();
//		} finally {
//			session.clearCache();
//			session.commit(true);
//			session.close();
//		}
//	}
//
//	@Override
//	public int updateDocList(int priorityNo, int docNo) {
//		int result = 0;
//		SqlSession session = sf.openSession();
//		try {
//			DocModel mp = new DocModel();
//			mp.docNo = docNo;
//			mp.priorityNo = priorityNo;
//			session.update("docMapper.updateDocList", mp);
//		} catch (Exception e) {
//			logger.error(e.getMessage(), e);
//			session.rollback();
//		} finally {
//			session.clearCache();
//			session.commit(true);
//			session.close();
//		}
//		return result;
//	}
//
//	@Override
//	public List<DocModel> selectAllDocDescID(int desc_id) {
//		SqlSession session = sf.openSession();
//		try {
//			DocModel mp = new DocModel();
//			mp.desc_id = desc_id;
//			return session.selectList("docMapper.selectAllDocDESCID", mp);
//		} catch (Exception e) {
//			e.printStackTrace();
//			logger.error("selectOneDoc", e);
//		} finally {
//			session.clearCache();
//			session.close();
//		}
//		return new ArrayList<DocModel>();
//	}
//
//	@Override
//	public List<DocModel> selectAllDoc() {
//		SqlSession session = sf.openSession();
//		try {
//			return session.selectList("docMapper.selectAllDoc");
//		} catch (Exception e) {
//			e.printStackTrace();
//			logger.error("selectOneDoc", e);
//		} finally {
//			session.clearCache();
//			session.close();
//		}
//		return new ArrayList<DocModel>();
//	}
//	
//	public List<DocChapterModel> recentlyDocuments () {
//		
//		SqlSession session = sf.openSession();
//		Map<String,Object> param = new HashMap<String,Object>();
//		param.put("startRow", 1);
//		param.put("limit", 5);
//		try {
//			return session.selectList("docMapper.recentlyDocuments",param);
//		} finally {
//			session.clearCache();
//			session.close();
//		}
//	}
//}
