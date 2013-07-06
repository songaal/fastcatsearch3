/*#-------------------------------------------------------------------------------
# Copyright (C) 2011 - 2013 Websquared, Inc.
# All rights reserved.
#-------------------------------------------------------------------------------*/
/*
 * Copyright (C) 2011 WebSquared Inc. http://websqrd.com
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */

function markUsed (chkElement){
	try {
	$(function() {
		//alert(chkElement.checked+":"+chkElement.value);
		if($.ajax({
			url:"keywordService.jsp",
			data:{cmd:13,keyword:chkElement.value,used:(chkElement.checked?"n":"y")},
			async:false,
			cache:false,
			type:"post"
		}).responseText.trim().length > 0) {
		   alert('활성화 변경을 실패하였습니다.'); 
		}
	})
	} catch (ex) { }
}

function deleteKeyword(chkElement) {
	
	chkElement.checked = false;
	try {
		var kstr = chkElement.value;
		if(confirm("검색어 ["+kstr+"] 를 삭제하시겠습니까?")) {
			if(confirm("확실합니까?")) {
				$(function() {
					if($.ajax({
						url:"keywordService.jsp",
						data:{cmd:14,keyword:chkElement.value},
						async:false,
						cache:false,
						type:"post"
					}).responseText.trim().length > 0) {
						alert("키워드 삭제를 실패하였습니다.");
					}
					location.reload();
				})
			}
		}
	} catch (ex) { }
}

function editKeyword(element) {
	var telement = document.createElement("input");
	var strvalue = element.innerHTML;
	telement.value = strvalue;
	telement.style.width=element.offsetWidth+"px";
	telement.style.border="1px solid #CCCCCC";
	element.innerHTML="";
	element.appendChild(telement);
	telement.focus();
	
	element.onclick=function() { };
	
	telement.onblur=function() {
		modifyKeyword(strvalue,telement);
	};
	
	telement.onkeydown=function(e) {
		if(!e) { e=window.event; }
		var keycode=e.keyCode? e.keyCode : e.charCode;
		if(keycode==13) {
			modifyKeyword(strvalue,telement);
		};
	};
}

function modifyKeyword(keyword1,element) {
	var pelement = element.parentNode;
	var keyword2 = element.value;
	pelement.innerHTML=keyword2;
	if(keyword1!=keyword2) {
		try {
			$(function() {
				var ret = $.ajax({
					url:"keywordService.jsp",
					data:{cmd:15,keyword1:keyword1,keyword2:keyword2},
					async:false,
					cache:false,
					type:"post"
				}).responseText.trim();
				
				if(ret.length > 0) {
					pelement.innerHTML=keyword1;
					if(ret=="-1") {
						alert("["+keyword2+"] 는 이미 존재하는 키워드입니다.");
					} else {
						alert("변경을 실패하였습니다."); 
					}
				}
			})
		} catch (ex) { }
	}
	pelement.onclick=function() {
		editKeyword(pelement);
	};
}

function changeKeywordRank(element,keyword,num,type) {
	var element = $("#keywordHitPopular"+num);
	var value1 = 0;
	var value2 = 0;
	
	if(type == 1) {
		value1 = $("#keywordHitPopular"+(num-1)).attr("value")*1 + 1;
		value2 = $("#keywordHitPopular"+(num-2)).attr("value")*1;
		if(!isNaN(value1) && !isNaN(value2) && value1 >= value2) { value1 = value2; }
		if(!isNaN(value1)) {
			updateKeywordPopular(keyword,type,value1);
		} else {
			alert("이미 최상위 입니다.")
		}
	} else if(type == 2) {
		value1 = $("#keywordHitPopular"+(num+1)).attr("value")*1 - 1;
		value2 = $("#keywordHitPopular"+(num+2)).attr("value")*1;
		if(!isNaN(value1) && !isNaN(value2) && value1 <= value2) { value1 = value2; }
		if(!isNaN(value1)) {
			updateKeywordPopular(keyword,type,value1);
		} else {
			alert("이미 최하위 입니다.")
		}
	} else if(type == 3) {
		//TODO:인기순위고정에 대한코드를 기재한다.
	}
}

function updateKeywordPopular(keyword,type,popular) {
	try {
		popular = parseInt(popular);
		$(function() {
			var ret = $.ajax({
				url:"keywordService.jsp",
				data:{cmd:16,keyword:keyword,type:type,popular:popular},
				async:false,
				cache:false,
				type:"post"
			}).responseText.trim();
			
			if(ret.length > 0) {
				alert("변경을 실패하였습니다."); 
			} else {
				location.reload();
			}
		})
	} catch (ex) { }
}


function addNewKeyWord() {
	try {
		var err = false;
		var newkeyword = $("#newkeyword").attr("value");
		var newkeywordpop = $("#newkeywordpop").attr("value");
		var newkeywordhit = $("#newkeywordhit").attr("value");
		newkeywordpop = parseInt(newkeywordpop) * 100;
		newkeywordhit = parseInt(newkeywordhit) * 1;
		if(!err && (newkeyword.length < 2 || newkeyword.length > 10)) {
			err = "키워드는 2글자 이상, 10글자 이하로 입력하셔야 합니다.";
		}
		if(!err && isNaN(newkeywordpop)) {
			err = "인기율은 숫자만 입력해 주세요";
			$("#newkeywordpop").focus();
		}
		if(!err && isNaN(newkeywordhit)) {
			err = "검색수는 숫자만 입력해 주세요";
			$("#newkeywordhit").focus();
		}
		
		if(err) {
			alert(err);
		} else {
			$(function() {
				var ret = $.ajax({
					url:"keywordService.jsp",
					data:{cmd:17,newkeyword:newkeyword,newkeywordpop:newkeywordpop,newkeywordhit:newkeywordhit},
					async:false,
					cache:false,
					type:"post"
				}).responseText.trim();
				
				if(ret.length > 0) {
					if(ret == "1") {
						alert("이미 \""+newkeyword+"\" 키워드가 존재합니다."); 
					} else {
						alert("검색어 추가를 실패하였습니다."); 
					}
				} else {
					location.reload();
				}
			})
		}
		
	} catch (ex) { }
}

function addNewKeyWordFail() {
	try {
		var err = false;
		var newkeyword = $("#newkeyword").attr("value");
		var newkeywordpop = $("#newkeywordpop").attr("value");
		var newkeywordhit = $("#newkeywordhit").attr("value");
		newkeywordpop = parseInt(newkeywordpop) * 100;
		newkeywordhit = parseInt(newkeywordhit) * 1;
		if(!err && (newkeyword.length < 2 || newkeyword.length > 10)) {
			err = "키워드는 2글자 이상, 10글자 이하로 입력하셔야 합니다.";
		}
		if(!err && isNaN(newkeywordpop)) {
			err = "인기율은 숫자만 입력해 주세요";
			$("#newkeywordpop").focus();
		}
		if(!err && isNaN(newkeywordhit)) {
			err = "검색수는 숫자만 입력해 주세요";
			$("#newkeywordhit").focus();
		}
		
		if(err) {
			alert(err);
		} else {
			$(function() {
				var ret = $.ajax({
					url:"keywordService.jsp",
					data:{cmd:18,newkeyword:newkeyword,newkeywordpop:newkeywordpop,newkeywordhit:newkeywordhit},
					async:false,
					cache:false,
					type:"post"
				}).responseText.trim();
				
				if(ret.length > 0) {
					if(ret == "1") {
						alert("이미 \""+newkeyword+"\" 키워드가 존재합니다."); 
					} else {
						alert("검색어 추가를 실패하였습니다."); 
					}
				} else {
					location.reload();
				}
			})
		}
		
	} catch (ex) { }
}
