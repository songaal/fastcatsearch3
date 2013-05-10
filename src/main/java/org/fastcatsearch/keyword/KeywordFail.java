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
//package org.fastcatsearch.keyword;
//
//import java.sql.Connection;
//import java.sql.PreparedStatement;
//import java.sql.ResultSet;
//import java.sql.SQLException;
//import java.sql.Statement;
//import java.sql.Timestamp;
//import java.util.ArrayList;
//import java.util.Date;
//import java.util.List;
//
//import org.fastcatsearch.db.dao.DAOBase;
//
//
///**
// * 기존의 KeywordFail 클래스에서 몇가지 변경하여 업그레이드 함. <br/>
// * 기존의 키워드누적합계와 시간별통계 이외에 일자별/주별/월별/년별통계 를 합산할 수 있으며 <br/>
// * 누적/시간별은 매 5분마다, 나머지는 매 한시간 마다 한 번씩 합산을 수행한다. <br/>
// * 
// * @author lupfeliz
// *
// */
//public class KeywordFail extends DAOBase {
//	
//	public static final int POPULAR_HOUR = 0;
//	public static final int POPULAR_ACCUM = 1;
//	public static final int STATISTICS_DATE = 2;
//	public static final int STATISTICS_WEEK = 3;
//	public static final int STATISTICS_MONTH = 4;
//	public static final int STATISTICS_YEAR = 10;
//	
//	public KeywordFail(){}
//	
//
//	public KeywordFail(int id, String keyword, int hit, int popular, int prevRank, boolean isUsed, Date dateRegister, Date dateUpdate) {
//		this.id = id;
//		this.keyword = keyword;
//		this.hit = hit;
//		this.popular = popular;
//		this.prevRank = prevRank;
//		this.isUsed = isUsed;
//		this.dateRegister = dateRegister;
//		this.dateUpdate = dateUpdate;
//	}
//	
//	public String toString(){
//		return id+ " : "+keyword+ " : "+hit+ " : "+popular+" : "+prevRank+" : "+isUsed;
//	}
//	
//	public int create() throws SQLException{
//		/**
//		 * type 에 따른 구분 분류
//		 * type=0:실시간, 누적 //5분마다
//		 * type=1:시간별 //5분마다
//		 * type=2:일별 //1일마다 계산. 1개월분 데이터 보관
//		 * type=3:주별 //1일마다 계산. 6개월분 데이터 보관
//		 * type=4:월별 //1일마다 계산. 2년분 데이터 보관
//		 * type=5:년별 //1일마다 계산. 10년분 데이터 보관
//		 * 총 100 + (24*100) + (31*100) + (22*100) + (24*100) + (10*100) 
//		 * = 100 + 2,400 + 3,100 + 2,200 + 2,400 + 1,000 = 11,200 개(MAX)
//		 * 5분마다 부하량 = 100 회
//		 **/
//		Connection conn = null;
//		String createSQL = null;
//		String prodName = conn.getMetaData().getDatabaseProductName();
//		createSQL = "create table " + tableName + "(id int primary key,type int,num int, keyword varchar(50),hit int, popular int, prevRank int, isUsed smallint, dateRegister timestamp, dateUpdate timestamp)";
//		if("Apache Derby".equals(prodName)) {
//		} else if("MySQL".equals(prodName)) { //MYSQL
//			createSQL+=" character set = utf8";
//		} else if("".equals(prodName)) { //ORACLE
//		} else if("".equals(prodName)) { //MSSQL
//		}
//		
//		Statement stmt =null;
//		try {
//			conn = conn();
//			stmt = conn.createStatement();
//			return stmt.executeUpdate(createSQL);
//		} finally {
//			releaseResource(stmt);
//			releaseConnection(conn);
//		}
//		
//	}
//	
//	public int clearHourOfDay(int hourOfDay) {
//		return clearFail(POPULAR_HOUR,hourOfDay);
//	}
//	
//	public int clearFail(int type, int num) {
//		Connection conn = null;
//		PreparedStatement pstmt = null;
//		String deleteSQL = "delete from " + tableName + " where type=? and num = ?";
//		
//		try {
//			conn = conn();
//			pstmt = conn.prepareStatement(deleteSQL);
//			int parameterIndex = 1;
//			pstmt.setInt(parameterIndex++, type);
//			pstmt.setInt(parameterIndex++, num);
//			return pstmt.executeUpdate();
//		}catch(SQLException e){
//			logger.error(e.getMessage(),e);
//			return -1;
//		}finally{
//			releaseResource(pstmt);
//			releaseConnection(conn);
//		}
//	}
//	
//	public int clearFail(int type, int num, Date date) {
//		Connection conn = null;
//		PreparedStatement pstmt = null;
//		String deleteSQL = "delete from " + tableName + " where type=? and num = ? and dateRegister = ?";
//		
//		try {
//			conn = conn();
//			pstmt = conn.prepareStatement(deleteSQL);
//			int parameterIndex = 1;
//			pstmt.setInt(parameterIndex++, type);
//			pstmt.setInt(parameterIndex++, num);
//			pstmt.setTimestamp(parameterIndex++, new Timestamp(date.getTime()));
//			return pstmt.executeUpdate();
//		}catch(SQLException e){
//			logger.error(e.getMessage(),e);
//			return -1;
//		} finally {
//			releaseResource(pstmt);
//			releaseConnection(conn);
//		}
//	}
//	
//	public int clearFailBefore(int type, int num, Date date) {
//		PreparedStatement pstmt = null;
//		Connection conn = null;
//		String deleteSQL = "delete from " + tableName + " where type=? and num = ? and dateRegister < ?";
//		
//		try {
//			conn = conn();
//			pstmt = conn.prepareStatement(deleteSQL);
//			int parameterIndex = 1;
//			pstmt.setInt(parameterIndex++, type);
//			pstmt.setInt(parameterIndex++, num);
//			pstmt.setTimestamp(parameterIndex++, new Timestamp(date.getTime()));
//			return pstmt.executeUpdate();
//		}catch(SQLException e){
//			logger.error(e.getMessage(),e);
//			return -1;
//		}finally{
//			releaseResource(pstmt);
//			releaseConnection(conn);
//		}
//	}
//	
//	public int insert(int type, int num, String keyword, int hit, int popular, int prevRank, boolean isUsed, Date dateRegister, Date dateUpdate) {
//		Connection conn = null;
//		PreparedStatement pstmt = null;
//		String insertSQL = "insert into " + tableName + "(id, type, num, keyword, hit, popular, prevRank, isUsed, dateRegister, dateUpdate) values (?,?,?,?,?,?,?,?,?,?)";
//				
//		try{
//			conn = conn();
//			pstmt = conn.prepareStatement(insertSQL);
//			int parameterIndex = 1;
//			pstmt.setInt(parameterIndex++, ID);
//			pstmt.setInt(parameterIndex++, type);
//			pstmt.setInt(parameterIndex++, num);
//			pstmt.setString(parameterIndex++, keyword);
//			pstmt.setInt(parameterIndex++, hit);
//			pstmt.setInt(parameterIndex++, popular);
//			pstmt.setInt(parameterIndex++, prevRank);
//			pstmt.setBoolean(parameterIndex++, isUsed);
//			pstmt.setTimestamp(parameterIndex++, new Timestamp(dateRegister.getTime()));
//			pstmt.setTimestamp(parameterIndex++, new Timestamp(dateUpdate.getTime()));
//			int c =  pstmt.executeUpdate();
//			if(c > 0){
//				ID++;
//			}
//			return c;
//		}catch(SQLException e){
//			logger.error(e.getMessage(),e);
//			return -1;
//		}finally{
//			releaseResource(pstmt);
//			releaseConnection(conn);
//		}
//	}
//	
//	public List<KeywordFail> selectKeywordFail(int type, int num) {
//		List<KeywordFail> result = new ArrayList<KeywordFail>();
//		String selectSQL = "SELECT id, keyword, hit, popular, prevRank, isUsed, dateRegister, dateUpdate from " + tableName + " where type =? and num = ? order by hit desc";
//		Connection conn = null;
//		PreparedStatement pstmt = null;
//		ResultSet rs = null;
//		
//		try{
//			conn = conn();
//			pstmt = conn.prepareStatement(selectSQL);
//			int parameterIndex = 1;
//			pstmt.setInt(parameterIndex++, type);
//			pstmt.setInt(parameterIndex++, num);
//			rs = pstmt.executeQuery();
//			
//			while(rs.next()){
//				KeywordFail h = new KeywordFail();
//				parameterIndex = 1;
//				h.id = rs.getInt(parameterIndex++);
//				h.keyword = rs.getString(parameterIndex++);
//				h.hit = rs.getInt(parameterIndex++);
//				h.popular = rs.getInt(parameterIndex++);
//				h.prevRank = rs.getInt(parameterIndex++);
//				h.isUsed = rs.getBoolean(parameterIndex++);
//				h.dateRegister = new Date(rs.getTimestamp(parameterIndex++).getTime());
//				h.dateUpdate = new Date(rs.getTimestamp(parameterIndex++).getTime());
//				result.add(h);
//			}
//			
//			pstmt.close();
//			rs.close();
//		} catch(SQLException e){
//			logger.error(e.getMessage(),e);
//		} finally {
//			releaseResource(pstmt, rs);
//			releaseConnection(conn);
//		}
//		
//		return result;
//	}
//	
//	public List<KeywordFail> selectKeywordFail(int type, int num, Date date) {
//		List<KeywordFail> result = new ArrayList<KeywordFail>();
//		String selectSQL = "SELECT id, keyword, hit, popular, prevRank, isUsed, dateRegister,dateUpdate from " + tableName + " where type =? and num = ? and dateRegister = ? order by hit desc";
//		
//		Connection conn = null;
//		PreparedStatement pstmt = null;
//		ResultSet rs = null;
//		
//		try{
//			conn = conn();
//			pstmt = conn.prepareStatement(selectSQL);
//			int parameterIndex = 1;
//			pstmt.setInt(parameterIndex++, type);
//			pstmt.setInt(parameterIndex++, num);
//			pstmt.setTimestamp(parameterIndex++, new Timestamp(date.getTime()));
//			rs = pstmt.executeQuery();
//			
//			while(rs.next()){
//				KeywordFail h = new KeywordFail();
//				
//				parameterIndex = 1;
//				h.id = rs.getInt(parameterIndex++);
//				h.keyword = rs.getString(parameterIndex++);
//				h.hit = rs.getInt(parameterIndex++);
//				h.popular = rs.getInt(parameterIndex++);
//				h.prevRank = rs.getInt(parameterIndex++);
//				h.isUsed = rs.getBoolean(parameterIndex++);
//				h.dateRegister = new Date(rs.getTimestamp(parameterIndex++).getTime());
//				h.dateUpdate = new Date(rs.getTimestamp(parameterIndex++).getTime());
//				result.add(h);
//			}
//			
//			pstmt.close();
//			rs.close();
//		} catch(SQLException e){
//			logger.error(e.getMessage(),e);
//		} finally {
//			releaseResource(pstmt, rs);
//			releaseConnection(conn);
//		}
//		
//		return result;
//	}
//	
//	public List<KeywordFail> selectKeywordFailLimit(int type, int num, int limitCount) {
//		List<KeywordFail> result = new ArrayList<KeywordFail>();
//		Connection conn = null;
//		PreparedStatement pstmt = null;
//		ResultSet rs = null;
//		
//		try{
//			String selectSQL = null;
//			String prodName = conn.getMetaData().getDatabaseProductName();
//			if("Apache Derby".equals(prodName)) {
//				selectSQL = "SELECT * from (SELECT " + tableName + ".id, " + tableName + ".keyword, " + tableName + ".hit, " + tableName + ".popular, " + tableName + ".prevRank, " + tableName + ".isUsed, dateRegister, dateUpdate, ROW_NUMBER() OVER() AS rownum from " + tableName + " where type=? and num = ? order by popular desc, hit desc) AS tmp WHERE rownum <= ?";
//			} else if("MySQL".equals(prodName)) { //MYSQL
//				selectSQL = "SELECT * from (SELECT " + tableName + ".id, " + tableName + ".keyword, " + tableName + ".hit, " + tableName + ".popular, " + tableName + ".prevRank, " + tableName + ".isUsed, dateRegister, dateUpdate, @ROWNUM:=@ROWNUM+1 AS rownum from " + tableName + ",(select @ROWNUM:=0)r where type=? and num = ? order by popular desc, hit desc) AS tmp WHERE rownum <= ?";
//			} else if("".equals(prodName)) { //ORACLE
//			} else if("".equals(prodName)) { //MSSQL
//			}
//			conn = conn();
//			pstmt = conn.prepareStatement(selectSQL);
//			int parameterIndex = 1;
//			pstmt.setInt(parameterIndex++, type);
//			pstmt.setInt(parameterIndex++, num);
//			pstmt.setInt(parameterIndex++, limitCount);
//			rs = pstmt.executeQuery();
//			
//			while(rs.next()){
//				KeywordFail h = new KeywordFail();
//				
//				parameterIndex = 1;
//				h.id = rs.getInt(parameterIndex++);
//				h.keyword = rs.getString(parameterIndex++);
//				h.hit = rs.getInt(parameterIndex++);
//				h.popular = rs.getInt(parameterIndex++);
//				h.prevRank = rs.getInt(parameterIndex++);
//				h.isUsed = rs.getBoolean(parameterIndex++);
//				h.dateRegister = new Date(rs.getTimestamp(parameterIndex++).getTime());
//				h.dateUpdate = new Date(rs.getTimestamp(parameterIndex++).getTime());
//				result.add(h);
//			}
//			
//			pstmt.close();
//			rs.close();
//		} catch(SQLException e){
//			logger.error(e.getMessage(),e);
//		} finally {
//			releaseResource(pstmt, rs);
//			releaseConnection(conn);
//		}
//		
//		return result;
//	}
//	
//	public List<KeywordFail> selectPopularKeywordFail() {
//		
//		return selectKeywordFail(KeywordFail.POPULAR_ACCUM,0);
//	}
//	
//	//인기검색어 검색
//	public List<KeywordFail> prefixSearchPopular(String keyword) {
//		List<KeywordFail> result = new ArrayList<KeywordFail>();
//		String selectSQL = "SELECT id, keyword, hit, popular, prevRank, isUsed, dateRegister, dateUpdate from " + tableName + " where type = ? and num = ? and keyword like ? order by popular desc, hit desc";
//		
//		Connection conn = null;
//		PreparedStatement pstmt = null;
//		ResultSet rs = null;
//		
//		try{
//			conn = conn();
//			pstmt = conn.prepareStatement(selectSQL);
//			int parameterIndex = 1;
//			pstmt.setInt(parameterIndex++, KeywordFail.POPULAR_ACCUM);
//			pstmt.setInt(parameterIndex++, 0);
//			pstmt.setString(parameterIndex++, keyword + "%");
//			rs = pstmt.executeQuery();
//			
//			while(rs.next()){
//				KeywordFail h = new KeywordFail();
//				
//				parameterIndex = 1;
//				h.id = rs.getInt(parameterIndex++);
//				h.keyword = rs.getString(parameterIndex++);
//				h.hit = rs.getInt(parameterIndex++);
//				h.popular = rs.getInt(parameterIndex++);
//				h.prevRank = rs.getInt(parameterIndex++);
//				h.isUsed = rs.getBoolean(parameterIndex++);
//				h.dateRegister = new Date(rs.getTimestamp(parameterIndex++).getTime());
//				h.dateUpdate = new Date(rs.getTimestamp(parameterIndex++).getTime());
//				result.add(h);
//			}
//			
//			pstmt.close();
//			rs.close();
//		} catch(SQLException e){
//			logger.error(e.getMessage(),e);
//		} finally {
//			releaseResource(rs, pstmt);
//			releaseConnection(conn);
//		}
//		return result;
//	}
//
//	//사용하지 않는 인기검색어를 마킹한다.
//	public int setNotUsing(String[] keywords) {
//		String insertSQL = "update " + tableName + " set isUsed = 0 where keyword = ?";
//		
//		Connection conn = null;
//		PreparedStatement pstmt = null;
//		try{
//			conn = conn();
//			pstmt = conn.prepareStatement(insertSQL);
//			for (int i = 0; i < keywords.length; i++) {
//				pstmt.setString(1, keywords[i]);
//				pstmt.addBatch();
//			}
//			int[] result = pstmt.executeBatch();
//			return result.length;
//			
//		}catch(SQLException e){
//			logger.error(e.getMessage(),e);
//			return -1;
//		}finally{
//			releaseResource(pstmt);
//			releaseConnection(conn);
//		}
//	}
//
//	//사용하지 않는 인기검색어 를 마킹.
//	public int setNotUsing (String keyword, int using) {
//		String insertSQL = "update " + tableName + " set isUsed = ? where keyword = ?";
//		
//		Connection conn = null;
//		PreparedStatement pstmt = null;
//		try {
//			conn = conn();
//			int parameterIndex = 1;
//			pstmt = conn.prepareStatement(insertSQL);
//			pstmt.setInt(parameterIndex++, using);
//			pstmt.setString(parameterIndex++, keyword);
//			return  pstmt.executeUpdate();
//		} catch (SQLException e) {
//			logger.error(e.getMessage(),e);
//			return -1;
//		} finally {
//			releaseResource(pstmt);
//			releaseConnection(conn);
//		}
//	}
//	
//	public int countKeywordFailItem(String keyword) {
//		String sql = "select count(*) from " + tableName + " where type=? and num=0 and keyword=?";
//		
//		Connection conn = null;
//		PreparedStatement pstmt = null;
//		ResultSet rs = null;
//		try {
//			conn = conn();
//			pstmt = conn.prepareStatement(sql);
//			pstmt.setObject(1, POPULAR_ACCUM );
//			pstmt.setObject(2, keyword);
//			rs = pstmt.executeQuery();
//			if(rs.next()) { return rs.getInt(1); }
//		} catch (SQLException e) {
//			logger.error(e.getMessage(),e);
//			return -1;
//		} finally {
//			releaseResource(pstmt, rs);
//			releaseConnection(conn);
//		}
//		return 0;
//	}
//	
//	/**
//	 * FIXME:해당 내용은 한글에만 적용되는 내용이므로 차후 패키지 밖으로 빼는것을 권장 함.
//	 */
//    private final int[][] chartbl = {
//        {0xAC00,0xAE4A,0xAE4B}, {0xAE4C,0xB08F,0xB097}, {0xB098,0xB2E2,0xB2E3},
//        {0xB2E4,0xB525,0xB52F}, {0xB530,0xB775,0xB77B}, {0xB77C,0xB9C1,0xB9C7},
//        {0xB9C8,0xBC11,0xBC13}, {0xBC14,0xBE5B,0xBE5F}, {0xBE60,0xC0A5,0xC0AB},
//        {0xC0AC,0xC2F6,0xC2F7}, {0xC2F8,0xC53D,0xC543}, {0xC544,0xC78E,0xC78F},
//        {0xC790,0xC9D9,0xC9DB}, {0xC9DC,0xCC27,0xCC27}, {0xCC28,0xCE6D,0xCE73},
//        {0xCE74,0xD0B9,0xD0BF}, {0xD0C0,0xD305,0xD308}, {0xD30C,0xD551,0xD557},
//        {0xD558,0xD79D,0xD7A3},
//    };
//    
//	/**
//	 * FIXME:해당 내용은 한글에만 적용되는 내용이므로 차후 패키지 밖으로 빼는것을 권장 함.
//	 */
//    public String[] getBoundaryOfCharacter(String str) {
//        String[] ret=new String[3];
//        int chr=0x0,stchr=0x0,mdchr=0x0,edchr=0x0;
//        if(str==null) str="";
//        str=str.trim();
//        if(str.length()>0) {
//            chr=(int)str.substring(str.length()-1).toCharArray()[0];
//            if((chr & 0xFF00) == 0x3100) {
//                switch (chr) {
//                    case 0x3131: stchr=chartbl[0][0] ; mdchr=chartbl[0][1] ;edchr=chartbl[0][2] ; break;
//                    case 0x3132: stchr=chartbl[1][0] ; mdchr=chartbl[1][1] ;edchr=chartbl[1][2] ; break;
//                    case 0x3134: stchr=chartbl[2][0] ; mdchr=chartbl[2][1] ;edchr=chartbl[2][2] ; break;
//                    case 0x3137: stchr=chartbl[3][0] ; mdchr=chartbl[3][1] ;edchr=chartbl[3][2] ; break;
//                    case 0x3138: stchr=chartbl[4][0] ; mdchr=chartbl[4][1] ;edchr=chartbl[4][2] ; break;
//                    case 0x3139: stchr=chartbl[5][0] ; mdchr=chartbl[5][1] ;edchr=chartbl[5][2] ; break;
//                    case 0x3141: stchr=chartbl[6][0] ; mdchr=chartbl[6][1] ;edchr=chartbl[6][2] ; break;
//                    case 0x3142: stchr=chartbl[7][0] ; mdchr=chartbl[7][1] ;edchr=chartbl[7][2] ; break;
//                    case 0x3143: stchr=chartbl[8][0] ; mdchr=chartbl[8][1] ;edchr=chartbl[8][2] ; break;
//                    case 0x3145: stchr=chartbl[9][0] ; mdchr=chartbl[9][1] ;edchr=chartbl[9][2] ; break;
//                    case 0x3146: stchr=chartbl[10][0]; mdchr=chartbl[10][1];edchr=chartbl[10][2]; break;
//                    case 0x3147: stchr=chartbl[11][0]; mdchr=chartbl[11][1];edchr=chartbl[11][2]; break;
//                    case 0x3148: stchr=chartbl[12][0]; mdchr=chartbl[12][1];edchr=chartbl[12][2]; break;
//                    case 0x3149: stchr=chartbl[13][0]; mdchr=chartbl[13][1];edchr=chartbl[13][2]; break;
//                    case 0x314A: stchr=chartbl[14][0]; mdchr=chartbl[14][1];edchr=chartbl[14][2]; break;
//                    case 0x314B: stchr=chartbl[15][0]; mdchr=chartbl[15][1];edchr=chartbl[15][2]; break;
//                    case 0x314C: stchr=chartbl[16][0]; mdchr=chartbl[16][1];edchr=chartbl[16][2]; break;
//                    case 0x314D: stchr=chartbl[17][0]; mdchr=chartbl[17][1];edchr=chartbl[17][2]; break;
//                    case 0x314E: stchr=chartbl[18][0]; mdchr=chartbl[18][1];edchr=chartbl[18][2]; break;
//                };
//            } else if(chr >= chartbl[0][0]  && chr <= chartbl[0][1] ) { stchr=chr; mdchr=chartbl[0][1] ;edchr=chartbl[0][2] ;
//            } else if(chr >= chartbl[1][0]  && chr <= chartbl[1][1] ) { stchr=chr; mdchr=chartbl[1][1] ;edchr=chartbl[1][2] ;
//            } else if(chr >= chartbl[2][0]  && chr <= chartbl[2][1] ) { stchr=chr; mdchr=chartbl[2][1] ;edchr=chartbl[2][2] ;
//            } else if(chr >= chartbl[3][0]  && chr <= chartbl[3][1] ) { stchr=chr; mdchr=chartbl[3][1] ;edchr=chartbl[3][2] ;
//            } else if(chr >= chartbl[4][0]  && chr <= chartbl[4][1] ) { stchr=chr; mdchr=chartbl[4][1] ;edchr=chartbl[4][2] ;
//            } else if(chr >= chartbl[5][0]  && chr <= chartbl[5][1] ) { stchr=chr; mdchr=chartbl[5][1] ;edchr=chartbl[5][2] ;
//            } else if(chr >= chartbl[6][0]  && chr <= chartbl[6][1] ) { stchr=chr; mdchr=chartbl[6][1] ;edchr=chartbl[6][2] ;
//            } else if(chr >= chartbl[7][0]  && chr <= chartbl[7][1] ) { stchr=chr; mdchr=chartbl[7][1] ;edchr=chartbl[7][2] ;
//            } else if(chr >= chartbl[8][0]  && chr <= chartbl[8][1] ) { stchr=chr; mdchr=chartbl[8][1] ;edchr=chartbl[8][2] ;
//            } else if(chr >= chartbl[9][0]  && chr <= chartbl[9][1] ) { stchr=chr; mdchr=chartbl[9][1] ;edchr=chartbl[9][2] ;
//            } else if(chr >= chartbl[10][0] && chr <= chartbl[10][1]) { stchr=chr; mdchr=chartbl[10][1];edchr=chartbl[10][2];
//            } else if(chr >= chartbl[11][0] && chr <= chartbl[11][1]) { stchr=chr; mdchr=chartbl[11][1];edchr=chartbl[11][2];
//            } else if(chr >= chartbl[12][0] && chr <= chartbl[12][1]) { stchr=chr; mdchr=chartbl[12][1];edchr=chartbl[12][2];
//            } else if(chr >= chartbl[13][0] && chr <= chartbl[13][1]) { stchr=chr; mdchr=chartbl[13][1];edchr=chartbl[13][2];
//            } else if(chr >= chartbl[14][0] && chr <= chartbl[14][1]) { stchr=chr; mdchr=chartbl[14][1];edchr=chartbl[14][2];
//            } else if(chr >= chartbl[15][0] && chr <= chartbl[15][1]) { stchr=chr; mdchr=chartbl[15][1];edchr=chartbl[15][2];
//            } else if(chr >= chartbl[16][0] && chr <= chartbl[16][1]) { stchr=chr; mdchr=chartbl[16][1];edchr=chartbl[16][2];
//            } else if(chr >= chartbl[17][0] && chr <= chartbl[17][1]) { stchr=chr; mdchr=chartbl[17][1];edchr=chartbl[17][2]; 
//            } else if(chr >= chartbl[18][0] && chr <= chartbl[18][1]) { stchr=chr; mdchr=chartbl[18][1];edchr=chartbl[18][2]; }
//            if(stchr!=0x0 && edchr!=0x0) {
//                str=str.substring(0,str.length()-1);
//                ret[0]=str+(char)stchr;
//                ret[1]=str+(char)mdchr;
//                ret[2]=str+(char)edchr;
//            } else {
//            	ret[0]=ret[1]=ret[2]=str;
//            }
//        }
//        return ret;
//    }
//	
//	public List<KeywordFail> selectUsingPopularFail(String keyword, int type, int time, Date date) {
//		List<KeywordFail> result = new ArrayList<KeywordFail>();
//		PreparedStatement pstmt = null;
//		Connection conn = null;
//		ResultSet rs = null;
//		try{
//			String[] bstr = getBoundaryOfCharacter(keyword);
//			String selectSQL = null;
//			conn = conn();
//			int parameterIndex = 1;
//			if(keyword==null || "".equals(keyword)) {
//				if(date != null) {
//					selectSQL = "SELECT id, keyword, hit, popular, prevRank, isUsed, dateRegister from " + tableName + " where type=? and num = ? and dateRegister = ? and isUsed = 1 order by popular desc, hit desc";
//					pstmt = conn.prepareStatement(selectSQL);
//					pstmt.setInt(parameterIndex++, type);
//					pstmt.setInt(parameterIndex++, time);
//					pstmt.setTimestamp(parameterIndex++,new Timestamp(date.getTime()));
//				} else {
//					selectSQL = "SELECT id, keyword, hit, popular, prevRank, isUsed, dateRegister from " + tableName + " where type=? and num = ? and isUsed = 1 order by popular desc, hit desc";
//					pstmt = conn.prepareStatement(selectSQL);
//					pstmt.setInt(parameterIndex++, type);
//					pstmt.setInt(parameterIndex++, time);
//				}
//			} else {
//				selectSQL = "SELECT id, keyword, hit, popular, prevRank, isUsed, dateRegister from " + tableName + " where type=? and num = ? and isUsed = 1 and keyword >= ? and ( keyword <= ?  or keyword <= ? ) order by popular desc, hit desc";
//				pstmt = conn.prepareStatement(selectSQL);
//				pstmt.setInt(parameterIndex++, KeywordFail.POPULAR_ACCUM);
//				pstmt.setInt(parameterIndex++, 0);
//				pstmt.setString(parameterIndex++,bstr[0]);
//				pstmt.setString(parameterIndex++,bstr[1]);
//				pstmt.setString(parameterIndex++,bstr[2]);
//			}
//			
//			rs = pstmt.executeQuery();
//			while(rs.next()){
//				KeywordFail h = new KeywordFail();
//				parameterIndex = 1;
//				h.id = rs.getInt(parameterIndex++);
//				h.keyword = rs.getString(parameterIndex++);
//				h.hit = rs.getInt(parameterIndex++);
//				h.popular = rs.getInt(parameterIndex++);
//				h.prevRank = rs.getInt(parameterIndex++);
//				h.isUsed = rs.getBoolean(parameterIndex++);
//				result.add(h);
//			}
//			pstmt.close();
//			rs.close();
//		} catch(SQLException e){
//			logger.error(e.getMessage(),e);
//		} finally {
//			releaseResource(pstmt,rs);
//			releaseConnection(conn);
//		}
//		
//		return result;
//	}
//	
//	public Timestamp isPoluarFailKeywordUpdated(Timestamp lastTime){
//		String selectSQL = "SELECT dateUpdate from " + tableName + " where type=1 and num = 0 and isUsed = 1 and  dateUpdate > ? order by dateUpdate desc";
//		Connection conn = null;
//		PreparedStatement pstmt = null;
//		ResultSet rs = null;
//		
//		try{
//			conn = conn();
//			pstmt = conn.prepareStatement(selectSQL);
//			pstmt.setTimestamp(1, lastTime);
//			rs = pstmt.executeQuery();
//			if(rs.next()){
//				return rs.getTimestamp(1);
//			}
//		} catch(SQLException e){
//			logger.error(e.getMessage(),e);
//		} finally {
//			releaseResource(pstmt, rs);
//			releaseConnection(conn);
//		}
//		return null;
//	}
//	public List<KeywordFail> selectPopularFail() {
//		String selectSQL = "SELECT id, keyword, hit, popular, prevRank, isUsed, dateRegister from " + tableName + " where type = ? and num = ? order by popular desc, hit desc";
//		
//		List<KeywordFail> result = new ArrayList<KeywordFail>();
//		PreparedStatement pstmt = null;
//		Connection conn = null;
//		ResultSet rs = null;
//		try{
//			conn = conn();
//			pstmt = conn.prepareStatement(selectSQL);
//			int parameterIndex = 1;
//			pstmt.setInt(parameterIndex++, KeywordFail.POPULAR_ACCUM);
//			pstmt.setInt(parameterIndex++, 0);
//			rs = pstmt.executeQuery();
//			
//			while(rs.next()){
//				KeywordFail h = new KeywordFail();
//				parameterIndex = 1;
//				h.id = rs.getInt(parameterIndex++);
//				h.keyword = rs.getString(parameterIndex++);
//				h.hit = rs.getInt(parameterIndex++);
//				h.popular = rs.getInt(parameterIndex++);
//				h.prevRank = rs.getInt(parameterIndex++);
//				h.isUsed = rs.getBoolean(parameterIndex++);
//				
//				result.add(h);
//			}
//			pstmt.close();
//			rs.close();
//		} catch(SQLException e){
//			logger.error(e.getMessage(),e);
//		} finally {
//			releaseResource(pstmt, rs);
//			releaseConnection(conn);
//		}
//		
//		return result;
//	}
//	
//	public int testAndCreate() throws SQLException {
//		Connection conn = null;
//		PreparedStatement pstmt = null;
//		ResultSet rs = null;
//		
//		try {
//			conn = conn();
//			pstmt = conn.prepareStatement("select count(*) from " + tableName);
//			rs = pstmt.executeQuery();
//			rs.next();
//			return 0;
//		} catch (SQLException e) {
//			create();
//			return 1;
//		} finally {
//			releaseResource(pstmt, rs);
//			releaseConnection(conn);
//		}
//	}
//
//	public int deleteKeywordFail(String keyword) {
//		String selectSQL = "DELETE from " + tableName + " where keyword=? ";
//		
//		int ret = 0;
//		Connection conn = null;
//		PreparedStatement pstmt = null;
//		
//		try{
//			conn = conn();
//			pstmt = conn.prepareStatement(selectSQL);
//			pstmt.setString(1, keyword);
//			ret = pstmt.executeUpdate();
//			
//		}catch(SQLException e){
//			logger.error(e.getMessage(),e);
//		} finally {
//			releaseResource(pstmt);
//			releaseConnection(conn);
//		}
//		
//		return ret;
//	}
//	
//	public int modifyKeywordFail(String keyword1, String keyword2) {
//		int ret = 0;
//		String selectSQL = "SELECT COUNT(*) FROM " + tableName + " WHERE type=1 AND keyword=?";
//		String updateSQL = "UPDATE " + tableName + " SET keyword=? WHERE keyword=?";
//		
//		Connection conn = null;
//		PreparedStatement pst = null;
//		ResultSet res = null;
//		
//		try {
//			conn = conn();
//			pst = conn.prepareStatement(selectSQL);
//			pst.setObject(1, keyword2);
//			res = pst.executeQuery();
//			int keywordCnt = 0;
//			if(res.next()) { keywordCnt = res.getInt(1); }
//			
//			if(keywordCnt == 0) {
//				res.close();
//				pst.close();
//				pst = conn.prepareStatement(updateSQL);
//				pst.setObject(1, keyword2);
//				pst.setObject(2, keyword1);
//				ret = pst.executeUpdate();
//			} else {
//				return -1;
//			}
//			
//		} catch (SQLException e) {
//			logger.error(e.getMessage(),e);
//		} finally {
//			releaseResource(pst, res);
//			releaseConnection(conn);
//		}
//		
//		return ret;
//	}
//	
//	public int updateKeywordPopularFail(String keyword, int type, int popular) {
//		int ret = 0;
//		PreparedStatement pst = null;
//		ResultSet res = null;
//		Connection conn= null;
//		try {
//			conn = conn();
//			if(type==1 || type==2) {
//    			String updateSQL = "UPDATE " + tableName + " SET popular=?, dateUpdate=? WHERE type=1 and keyword=?";
//    			pst = conn.prepareStatement(updateSQL);
//    			pst.setObject(1, popular);
//    			pst.setObject(2, new Timestamp(System.currentTimeMillis()));
//    			pst.setObject(3, keyword);
//    			ret = pst.executeUpdate();
//			} else if(type==3) {
//				//FIXME:순위고정에 대한 코드를 기술한다.
//			}
//		} catch (SQLException e) {
//			logger.error(e.getMessage(),e);
//		} finally {
//			if ( pst != null )	releaseResource(pst);
//			if ( res != null )	releaseResource(res);
//			releaseConnection(conn);
//		}
//		return ret;
//	}
//}
//
