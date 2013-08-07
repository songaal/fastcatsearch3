<%--
# Copyright (C) 2011 - 2013 Websquared, Inc.
# All rights reserved.
--%>

<%@ page contentType="text/html; charset=UTF-8"%> 

<%
boolean isAuthorized = true;
Object authObj = "익명사용자";
String[] accessLog = new String[]{"",""};

%>

<% { %>
<% String __FASTCAT_MANAGE_ROOT__ = (String)application.getAttribute("FASTCAT_MANAGE_ROOT")+"/"; %>
<% String corporationName = "--";//IRSettings.getConfig().getString("LICENSE_CORPNAME"); %>
<div id="header">
	<div><img src="<%=__FASTCAT_MANAGE_ROOT__%>images/fastcatsearch-logo.gif" /></div>
	<div id="loginbar">
		<% if(isAuthorized) { %>
		<!-- 로그온시 정보 -->
		<div id="login_box">
			<p class="logname"><%=corporationName %> (<%=(String)authObj %>님) <input type="button" class="btn" value="로그아웃" onclick="javascript:logout()"/></p>
		</div>
		<% } %>
	</div>
	<!-- GNB -->
	<div class="menucontainer">
		<div class="menu">
		<ul>
			<li><a href="<%=__FASTCAT_MANAGE_ROOT__%>main.jsp">홈</a></li>
			<!----> <li><a href="<%=__FASTCAT_MANAGE_ROOT__%>monitoring/resourceStat.jsp">통계</a>
				<table><tr><td>
					<ul>
						<li><a href="<%=__FASTCAT_MANAGE_ROOT__%>monitoring/resourceStat.jsp">리소스통계</a></li>
						<li><a href="<%=__FASTCAT_MANAGE_ROOT__%>monitoring/searchStat.jsp">검색통계</a></li>
					</ul>
				</td></tr></table>
			</li>
			<li><a href="<%=__FASTCAT_MANAGE_ROOT__%>collection/main.jsp">컬렉션</a>
				<table><tr><td>
					<ul>
						<li><a href="<%=__FASTCAT_MANAGE_ROOT__%>collection/main.jsp">컬렉션정보</a></li>
						<li><a href="<%=__FASTCAT_MANAGE_ROOT__%>collection/schema.jsp">스키마설정</a></li>
						<li><a href="<%=__FASTCAT_MANAGE_ROOT__%>collection/datasource.jsp">데이터소스설정</a></li>
					</ul>
				</td></tr></table>
			</li>
			<li><a class="drop" href="<%=__FASTCAT_MANAGE_ROOT__%>indexing/main.jsp">색인</a>
				<table><tr><td>
					<ul>
						<li><a href="<%=__FASTCAT_MANAGE_ROOT__%>indexing/main.jsp">색인정보</a></li>
						<li><a href="<%=__FASTCAT_MANAGE_ROOT__%>indexing/result.jsp">색인결과</a></li>
						<li><a href="<%=__FASTCAT_MANAGE_ROOT__%>indexing/schedule.jsp">색인주기설정</a></li>
						<li><a href="<%=__FASTCAT_MANAGE_ROOT__%>indexing/history.jsp">색인히스토리</a></li>
					</ul>
				</td></tr></table>
			</li>
			<li><a href="<%=__FASTCAT_MANAGE_ROOT__%>dic/main.jsp">사전</a>
				<table><tr><td>
					<ul>
						<li><a href="<%=__FASTCAT_MANAGE_ROOT__%>dic/main.jsp">사전정보</a></li>
						<li><a href="<%=__FASTCAT_MANAGE_ROOT__%>dic/synonymDic.jsp">유사어사전</a></li>
						<li><a href="<%=__FASTCAT_MANAGE_ROOT__%>dic/userDic.jsp">사용자사전</a></li>
						<li><a href="<%=__FASTCAT_MANAGE_ROOT__%>dic/stopDic.jsp">금지어사전</a></li>
						<li><a href="<%=__FASTCAT_MANAGE_ROOT__%>dic/koreanDic.jsp">한국어사전</a></li>
					</ul>
				</td></tr></table>
			</li>
			<li><a href="<%=__FASTCAT_MANAGE_ROOT__%>management/main.jsp">관리</a>
				<table><tr><td>
					<ul>
						<li><a href="<%=__FASTCAT_MANAGE_ROOT__%>management/monitorPopup.jsp" target="fc_monitor">실시간모니터링(팝업)</a></li>
						<li><a href="<%=__FASTCAT_MANAGE_ROOT__%>management/serverCluster.jsp">서버클러스터상태</a></li>
						<li><a href="<%=__FASTCAT_MANAGE_ROOT__%>management/main.jsp">시스템상태</a></li>
						<li><a href="<%=__FASTCAT_MANAGE_ROOT__%>management/searchEvent.jsp">이벤트내역</a></li>
						<li><a href="<%=__FASTCAT_MANAGE_ROOT__%>management/jobHistory.jsp">작업히스토리</a></li>
						<li><a href="<%=__FASTCAT_MANAGE_ROOT__%>management/account.jsp">계정관리</a></li>
						<li><a href="<%=__FASTCAT_MANAGE_ROOT__%>management/config.jsp">사용자설정</a></li>
						<li><a href="<%=__FASTCAT_MANAGE_ROOT__%>management/advConfig.jsp">고급설정보기</a></li>
						<li><a href="<%=__FASTCAT_MANAGE_ROOT__%>management/restore.jsp">복원</a></li>
						<li><a href="<%=__FASTCAT_MANAGE_ROOT__%>management/license.jsp">라이선스</a></li>
					</ul>
				</td></tr></table>
			</li>
			<li><a href="<%=__FASTCAT_MANAGE_ROOT__%>keyword/recommend.jsp">검색어관리</a>
				<table><tr><td>
					<ul>
						<li><a href="<%=__FASTCAT_MANAGE_ROOT__%>keyword/recommend.jsp">추천어</a></li>
						<li><a href="<%=__FASTCAT_MANAGE_ROOT__%>keyword/popularKeyword.jsp">인기검색어</a></li>
						<li><a href="<%=__FASTCAT_MANAGE_ROOT__%>keyword/popularFailKeyword.jsp">실패검색어</a></li>
						<li><a href="<%=__FASTCAT_MANAGE_ROOT__%>keyword/hourlyKeyword.jsp">시간대별검색어</a></li>
					</ul>
				</td></tr></table>
			</li>
			<li><a href="<%=__FASTCAT_MANAGE_ROOT__%>tester/search.jsp">테스트</a>
				<table><tr><td>
					<ul>
						<li><a href="<%=__FASTCAT_MANAGE_ROOT__%>tester/search.jsp">검색테스트</a></li>
						<li><a href="<%=__FASTCAT_MANAGE_ROOT__%>tester/analyzer.jsp">분석기테스트</a></li>
						<li><a href="<%=__FASTCAT_MANAGE_ROOT__%>tester/dbTester.jsp">DB테스트</a></li>
						<li><a href="<%=__FASTCAT_MANAGE_ROOT__%>tester/searchDoc.jsp">문서원문조회</a></li>
					</ul>
				</td></tr></table>
			</li>
		</ul>
		</div><!-- // E : menu -->
	</div><!-- // E : menucontainer -->
	<script type="text/javascript">
		function logout(){
			location.href="<%=__FASTCAT_MANAGE_ROOT__%>index.jsp?cmd=logout";
		}
	</script>
</div><!-- // E : #header -->
<% } %>
