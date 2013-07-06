/*#-------------------------------------------------------------------------------
# Copyright (C) 2011 - 2013 Websquared, Inc.
# All rights reserved.
#-------------------------------------------------------------------------------*/
$(function() { 
	$(".help").each(function() {
	    var obj = $(this);
	    var helpTopic = obj.attr("id");
	    var helpId = helpTopic + "_help";
	    helpId = helpId.replace(/\./,'_');
	    //<a href="javascript:help('fetchsize')"><img src="<%=FASTCAT_MANAGE_ROOT%>images/help.gif" /></a>
		//<br />
		//<div id="fetchsize_help" class="helpContents"></div>
		obj.after("<a href=\"javascript:help(\'"+helpTopic+"\', \'"+helpId+"\')\" class='helpIcon'>Help!</a><br><div id='"+helpId+"' class='helpContents'></div>"); 
	}); 
});


function help(helpTopic, helpId){
	
	var msg = "도움말이 존재하지 않습니다.";
	
	switch(helpTopic){
	
		
	case "server.port": 
		msg = "검색엔진에서 요청을 받아들이는 포트이다. 검색과 웹관리도구를 접근시 사용된다.<br>";
		msg += "기본값은 8080이며, 필요시 8000이상 12000이하의 정수를 설정하도록 한다.";
		break;
		
	case "synonym.two-way": 
		msg = "단방향 : 대표단어 => 유사단어 확장<br>";
		msg += "양방향 : 대표단어 => 유사단어 확장 AND 유사단어 => 대표단어 확장<br>";
		msg += "양방향으로 지정시 대표단어의 의미는 없으며, 풍부한 검색결과를 볼 수 있지만 정확도는 떨어질 수 있다.<br>";
		msg += "기본값은 단방향이다.";
		break;
		
	case "dynamic.classpath": 
		msg = "사용자가 생성한 외부클래스들을 검색엔진에서 인식할 수 있도록 설정한다.<br>";
		msg += "검색엔진 홈디렉토리 기준 상대경로로 인식하며, 여러 경로를 설정시 : 또는 ; 경로구분자로 구분한다.<br>";
		msg += "예) lib/project1.jar<br>";
		msg += "예) lib/myClass.jar:lib/myRanking.jar"
		break;
		
	case "license.key": 
		msg = "검색엔진을 사용하기 위해 라이센스 키를 등록한다.<br>";
		msg += "라이선스 키는 (주)웹스퀘어드 에서 발급한다."
		break;
			
	/////////////
	//데이터소스설정
	/////////////
		
	case "sourceTypeList": 
		msg = "파일 : 검색대상 원본데이터가 파일로 존재할 경우 선택한다.<br>";
		msg += "DB : 검색대상 원본데이터가 DB에 존재할 경우 선택한다.<br>";
		msg += "사용자지정 : 검색대상 원본데이터가 파일과 DB 이외의 곳에 존재할 경우 데이터소스 Reader를 직접구현하여 수집가능하다.";
		break;
	case "sourceFromList": 
		msg = "싱글소스 : 검색대상 원본데이터가 한곳에 존재할 경우 선택한다.<br>";
		msg += "멀티소스 : 검색대상 원본데이터가 서로 다른 머신의 DB 또는 파일들에 존재할 경우 이들을 혼합하여 색인하고자 할때 선택한다.";
		break;
		
	case "sourceModifier": 
		msg = "원본데이터를 Java를 이용해서 변환하려할때 구현한 클래스이름을 입력한다.<br>";
		msg += "데이터의 변환이 필요하지 않으면 공란으로 남겨둔다.<br>";
		msg += "소스모디파이어는 org.fastcatsearch.ir.source.SourceModifier를 구현한 클래스이다.<br>";
		msg += "<u>외부 클래스를 사용하려면 반드시 동적클래스패스에 해당 jar파일을 추가해야 한다.</u><br>";
		msg += "예) org.fastcatsearch.user.sourcemodifier.SuggestionModifier";
		break;
		
	case "fullFilePath": 
		msg = "이곳에 설정된 파일데이터는 검색엔진에서 전체색인에 사용된다.<br>";
		msg += "파일포맷은 자유로우며, 해당 파일을 해석할 수 있는 수집파일Parser를 아래에서 셋팅하도록 한다.<br>";
		msg += "상대경로 입력시 검색엔진 홈디렉토리를 기준으로 인식하며, /로 시작하는 절대경로로 설정가능하다.<br>";
		msg += "단일 파일을 설정가능할 뿐아니라, 디렉토리경로를 설정하면, 해당 디렉토리내의 모든 파일을 사용한다.<br>";
		msg += "예)collection/sample/testData/fullDir<br>";
		msg += "예)collection/sample/testData/full.txt";
		break;
	case "incFilePath": 
		msg = "이곳에 설정된 파일데이터는 검색엔진에서 전체색인에 사용된다.<br>";
		msg += "설정방법은 위의 전체색인 파일경로의 경우와 같다.<br>";
		msg += "예)collection/sample/testData/incDir<br>";
		break;
	case "fileDocParser": 
		msg = "위에 설정한 수집파일을 해석할 수 있는 Parser이다.<br>";
		msg += "기본파서는 패스트캣 수집파일을 해석할 수 있는 org.fastcatsearch.parser.FastcatSearchCollectFileParser이다.<br>";
		msg += "모든 파서는 org.fastcatsearch.ir.source.SourceReader를 상속받아 구현되며, 수집파일이 패스트캣 포맷이 아닐경우사용자가 직접 구현할 수도 있다.<br>";
		msg += "예) org.fastcatsearch.parser.FastcatSearchCollectFileParser이다";
		break;
	case "fileEncoding": 
		msg = "수집파일의 인코딩을 지정한다.<br>";
		msg += "텍스트 수집파일의 경우 인코딩을 잘못 지정하면, 파서가 문서를 제대로 해석할 수 없다.<br>";
		msg += "예) utf-8<br>";
		msg += "<b>>자주쓰이는 인코딩들</b>";
		msg += "<li>utf-8</li><li>euc-kr</li><li>cp949</li>";
		break;
		
	case "driver": 
		msg = "JDBC드라이버 클래스설정<br><u>해당 드라이버를 사용하려면 반드시 동적클래스패스에 드라이버 jar파일을 추가해야 한다.</u><br>";
		msg += "예) org.gjt.mm.mysql.Driver<br>";
		msg += "<b>>자주쓰이는 JDBC드라이버 리스트</b>";
		msg += "<li>MySql : org.gjt.mm.mysql.Driver</li>";
		msg += "<li>MSSQL(jtds) : net.sourceforge.jtds.jdbc.Driver</li>";
		msg += "<li>ORACLE : oracle.jdbc.driver.OracleDriver</li>";
		msg += "<li>PostgreSQL : org.postgresql.Driver</li>";
		msg += "<li>Cubrid : cubrid.jdbc.driver.CUBRIDDriver</li>";
		break;
		
	case "url": 
		msg = "JDBC URL설정<br>접속할 서버주소, 포트, DB명으로 jdbc연결설정.<br>";
		msg += "예) jdbc:mysql://192.168.0.100:3306/fastcat<br>";
		msg += "<b>>자주쓰이는 JDBC URL 리스트</b>";
		msg += "<li>MySql : jdbc:mysql://[host][:port]/[db]</li>";
		msg += "<li>MSSQL(jtds) : jdbc:jtds:sqlserver://[host][:port]/[db]</li>";
		msg += "<li>ORACLE : jdbc:oracle:thin//[host][:port]/SID</li>";
		msg += "<li>PostgreSQL : jdbc:postgresql://[host]:[port]/[db]</li>";
		msg += "<li>Cubrid : jdbc:cubrid:[host]:[port]:[db]:::</li>";
		break;
		
		
	case "fetchsize": 
		msg = "DB에서 데이터를 읽을때 사용하는 파라미터.<br>기본값은 1000이며, 100이상 10000이하의 정수를 설정한다.";
		break;
		
	case "bulksize":
		msg = "Reader가 데이터를 몇건씩 읽어서 처리하는지 정의하는 파라미터.<br>기본값은 100이며, 50이상 1000이하의 정수값을 설정한다.";
		break;
		
	case "beforeFullQuery": 
		msg = "전체색인전에 수행되는 Update 쿼리.<br>";
		msg += "선택사항이며 비지니스 용도에 맞게 설정하여 사용한다. 필요없을 경우 공란으로 남겨두어도 무방하다.<br>";
		msg += "예) Update SearchStatus Set flag = 1 Where type='full'<br>";
		break;
		
	case "beforeIncQuery": 
		msg = "증분색인전에 수행되는 Update 쿼리.<br>";
		msg += "선택사항이며 비지니스 용도에 맞게 설정하여 사용한다. 필요없을 경우 공란으로 남겨두어도 무방하다.<br>";
		msg += "예) Update SearchStatus Set flag = 1 Where type='inc'<br>";
		break;
		
	case "fullQuery": 
		msg = "전체색인시 수행되는 SELECT 쿼리.<br>";
		msg += "전체색인시 필수사항이며 데이터를 검색엔진으로 가져오는 역할을 한다.<br>";
		msg += "컬렉션 스키마 이름과 필드이름이 일치하도록 해야한다. <br>";
		msg += "예)Select subject As title, body, regdate, username From TestData<br>";
		break;
		
	case "deleteIdQuery": 
		msg = "증분색인시 수행되는 SELECT 쿼리.<br>";
		msg += "선택사항이며 삭제대상의 id리스트를 가져와, 검색엔진에서 해당 문서들을 삭제하는 역할을 한다.<br>";
		msg += "컬렉션 스키마에서 PK로 맵핑한 필드와 일치하도록 해야한다. <br>";
		msg += "예)Select idx From TestData<br>";
		break;
		
	case "incQuery": 
		msg = "증분색인시 수행되는 SELECT 쿼리.<br>";
		msg += "증분색인시 필수사항이며 추가된 데이터를 검색엔진으로 가져오는 역할을 한다.<br>";
		msg += "컬렉션 스키마 이름과 필드이름이 일치하도록 해야한다.<br>";
		msg += "이전 색인시간 이후로 추가된 데이터만 가져와야 하므로 Where 조건이 필요하다.<br>";
		msg += "시간조건은 regdate > <b>${indextime}.start_dt</b> 와 같이 색인내부변수를 이용한다.<br>";
		msg += "예)Select subject As title, body, regdate, username From TestData<br>";
		msg += "   Where regdate  > ${indextime}.start_dt";
		break;
		
	case "afterFullQuery": 
		msg = "전체색인후에 수행되는 Update 쿼리.<br>";
		msg += "선택사항이며 비지니스 용도에 맞게 설정하여 사용한다. 필요없을 경우 공란으로 남겨두어도 무방하다.<br>";
		msg += "예) Delete From DeleteIDList => 삭제문서리스트 테이블을 지운다.<br>";
		break;
		
	case "afterIncQuery": 
		msg = "증분색인후에 수행되는 Update 쿼리.<br>";
		msg += "선택사항이며 비지니스 용도에 맞게 설정하여 사용한다. 필요없을 경우 공란으로 남겨두어도 무방하다.<br>";
		msg += "예) Delete From DeleteIDList<br> => 증분색인시 삭제한 문서리스트를 지워준다. 다음 색인시에 다시 지우지 않도록 조치하기 위함.";
		break;
		
	case "fullBackupPath": 
		msg = "전체색인시 사용된 데이터를 수집문서로 만들때 사용되는 파일.<br>";
		msg += "디버그 또는 백업목적으로 사용할 수 있으며, 운영시 사용하지 않아도 무방하다.<br>";
		msg += "상대경로 입력시 검색엔진 홈디렉토리를 기준으로 인식하며, /로 시작하는 절대경로로 설정가능하다.<br>";
		msg += "디렉토리가 아닌 파일이름을 입력한다."
		break;
		
	case "incBackupPath": 
		msg = "증분색인시 사용된 데이터를 수집문서로 만들때 사용되는 파일.<br>";
		msg += "전체색인 백업파일경로와 동일한 방식으로 설정한다.";
		break;
		
	case "backupFileEncoding": 
		msg = "백업수집파일의 인코딩을 지정한다.<br>";
		msg += "예) utf-8<br>";
		msg += "<b>>자주쓰이는 인코딩들</b>";
		msg += "<li>utf-8</li><li>euc-kr</li><li>cp949</li>";
		break;
		
	case "customReaderClass": 
		msg = "사용자가 직접구현한 SourceReader 클래스명.<br>";
		msg += "사용하기 위해서는 org.fastcatsearch.ir.source.SourceReader를 구현하여 컴파일한 jar파일을 동적클래스패스에 추가해주어야 한다.<br>";
		msg += "예) org.fastcatsearch.user.reader.mail.MailReader => 이메일을 내용을 수집하는 Reader";
		break;
		
	case "customConfigFile": 
		msg = "사용자가 직접구현한 SourceReader의 셋팅을 담고 있는 설정파일이름<br>";
		msg += "파일포맷은 Java Property파일 형식이며, 파일은 컬렉션 홈디렉토리(패스트캣홈/collection/[컬렉션명])에 위치해야 한다.<br>";
		msg += "예) mail.conf";
		break;
		
		
		/////////////
		//검색테스트
		/////////////
	case "cn": 
		msg = "검색할 하나의 컬렉션명을 입력한다.<br>";
		msg += "cn=컬렉션명<br>";
		msg += "예) sample = >sample이라는 컬렉션을 검색한다.";
		break;
		
	case "ht": 
		msg = "검색결과에서 검색키워드를 부각시키기 위해 특정태그로 감싸준다.<br>";
		msg += "ht=시작태그:종료태그<br>";
		msg += "예) <b>:</b> => 볼드체로 감싸준다.";
		break;
		
	case "sn": 
		msg = "총 검색결과중에 부분범위를 설정하는 시작번호이다.<br>" +
				"시작번호는 1부터 시작하는 정수이다.<br>" +
				"예) 1 => 제일 처음 결과부터 가져온다.";
		break;
		
	case "ln": 
		msg = "총 검색결과중에 가져올 결과갯수를 설정하는 값.<br>" +
				"1이상의 정수를 입력한다. 너무 큰 값을 적으면 메모리부족에러가 발생할 수 있으므로, 필요한 만큼만 가져오도록 한다.<br>" +
				"예)50 => 50개의 결과를 가져온다.";
		break;
		
	case "so": 
		msg = "검색옵션.<br>" +
				"nocache : 검색결과를 캐시에서 가져오지 않고 새로 검색하도록 한다. 디버그용도.<br>" +
				"highlight : 검색결과 키워드 하이라이팅을 수행한다. 이 옵션을 사용하면 검색필드별로 셋팅을 할 필요가 없다.";
		break;
		
	case "ud": 
		msg = "쿼리통계에 검색키워드가 포함되기 위해서는 ud에 \"keyword:[검색키워드]\"와 같이 데이터를 넘겨야 한다.<br>" +
				"예) keyword:홍삼 음료";
		break;
		
	case "fl": 
		msg = "보여줄 필드를 나열한다.<br>" +
				"필드이름을 공백없이 콤마(,)로 연결해서 입력한다. 검색필드의 경우 요약길이를 지정할 수 있는데 필드명[:요약길이] 와 같이 설정한다.<br>" +
				"예) id,title,body:100,category";
		break;
		
	case "se": 
		msg = "검색할 필드를 설정하여 문법은 다음과 같다.<br>" +
				"{검색필드명,[검색필드명,..]:[AND|OR|EXT](검색키워드)[:가중치:옵션]}[AND|OR|NOT{또다른 조건}]<br>" +
				"가중치 : 검색키워드가 해당 필드에서 n번 출현하면 점수는 n * 가중치로 계산된다. 문서의 최종점수는 필드간의 점수를 모두 더한 값이다.<br>" +
				"옵션 : 옵션값은 아래 옵션들을 더한 값을 사용한다. 모두 설정시 값은 15가 된다.<br>" +
				"<li>유사어검색 = 1</li>" +
				"<li>금지어설정 = 2</li>" +
				"<li>하이라이트 태그사용 = 4</li>" +
				"<li>요약생성 = 8</li>" +
				"EXT 검색 : Prefix검색과 범위검색을 지원한다. Prefix검색은 별문자를사용하며, 범위검색에서 대괄호 []는 inclusive, 중괄호 ()는 exclusive를 나타낸다.<br>" +
				"검색조건은 필수항목이지만 값을 넘기지 않을 경우 모든 데이터를 반환한다. 하지만 검색엔진에 부담을 줄수 있으니 테스트용도로만 사용하도록 한다.<br>" +
				"예1) {body:핸드폰}<br>" +
				"예2) {title,body:OR(핸드폰):100}<br>" +
				"예3) {title:AND(핸드폰 필름):100:15}"; 
				//"예4) se={{title,body:OR(핸드폰)}AND{price:EXT[10000~20000]}}<br>" +
				//"예5) se={tags:EXT(핸드*)}";
		break;
		
	case "gr": 
		msg = "통계정보를 계산하며 문법은 다음과 같다.<br>" +
				"그룹필드명:그룹핑기능[:구간갯수][:정렬방식][:결과제한갯수]<br>" +
				"그룹핑기능에는 키별 개수를 셈하는 freq 기능을 제공한다.<br>" +
				"정렬방식에는 키이름 정렬인 key_asc, key_desc와 개수로 정렬하는 freq_asc, freq_desc가 있다.<br>" +
				"결과제한갯수는 그룹핑 결과가 많을때 상위 N개만 가져오고 싶을 때 사용되며, 정수값을 입력한다.<br>" +
				"사용자가 그룹핑기능을 개발하여 plugin하는 것도 가능하며 그룹핑기능에 지정한 기능이름을 입력한다.<br>" +
				"예1) category:freq<br>" +
				"예2) category:freq:freq_asc<br>" +
				"예3) category:freq:key_asc:10<br>"; +
				"예4) 사용자 PLUGIN >> category:my_grouping:key_asc<br>";
		break;
		
	case "gc":
		msg = "그룹핑 통계정보에 적용되지 않는 조건절이다.<br>" +
				"문법은 se 검색조건과 동일하다.<br>" +
				"검색시 그룹필터조건은 그룹핑 결과가 만들어지고 난 후에 적용되므로 이 조건이 se에 들어간 경우와 검색결과는 동일하다. 하지만 그룹핑결과는 전혀 달라지게 된다.<br>" +
				"이 조건은 그룹핑결과대상이 검색결과의 상위일 경우 사용한다.<br>" +
				"그룹필드에 적용시 해당필드가 검색필드로 셋팅되어 있어야 한다."; +
				"예) {category:OR(001 002)}"
		break;
				
	case "gf":
		msg = "그룹핑 통계정보에 적용되지 않는 필터조건절이다.<br>" +
				"문법은 ft 필터조건과 동일하다.<br>" +
				"gc와 비슷하게 그룹핑을 수행한 뒤에 결과셋을 제한하고자 할 경우 사용한다. 단지 검색조건을 이용하는지 필터조건을 이용하는지의 방식만 다르다.<br>" +
				"그룹필드에 적용시 해당 필드가 필터필드로 셋팅되어 있어야 한다."; +
				"예) {category:match:1111}"
		break;

	case "ra":
		msg = "검색결과 정렬방식을 설정하며 문법은 다음과 같다.<br>" +
				"정렬필드명[:정렬옵션],...<br>" +
				"컴마구분으로 다중정렬이 가능하며, 점수로 정렬을 하려면 필드명으로 _score_ 를 사용한다.<br>" +
				"정렬옵션에는 오름차순 asc와 내림차순 desc가 있다.<br>" +
				"예1) price:desc<br>" +
				"예2) _score_:desc,price:asc"
		break;
				
	case "ft":
		msg = "검색결과를 제한하는 필터기능이며, 문법은 다음과 같다.<br>" +
				"필터필드명:필터타입:필터키워드[;필터키워드2],...<br>" +
				"컴마구분으로 다중필터링(필터간 AND관계)이 가능하며, 필터키워드도 세미콜론(;) 구분으로 다중설정(키워드간 OR관계)이 가능하다.<br>" +
				"* 필터타입<br>" +
				"<li>match : 완전일치</li>" +
				"<li>prefix : 전방일치</li>" +
				"<li>suffix : 후방일치</li>" +
				"<li>section : 구간일치(~로 시작과 끝 연결)</li>" +
				"<li>match_boost : 완전일치 점수높임</li>" +
				"<li>prefix_boost : 전방일치 점수높임</li>" +
				"<li>suffix_boost : 후방일치 점수높임</li>" +
				"<li>section_boost : 구간일치 점수높임</li>" +
				"boost타입의 경우, 결과 갯수는 변하지 않고 결과문서의 점수에만 변화가 생긴다.<br>" +
				"예1) sellprice:section:500~2000;15000~20000<br>" +
				"예2) username:match:james,category:prefix:1030<br>";
				"예3) username:match_boost:james:500,category:prefix_boost:1030:500";
		break;
				
	}
	
	id = $("#"+helpId);
	if(id.length > 0 && msg.length > 0){
		id.html(msg);
		id.toggle();
	}
}
