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

String.prototype.trim = function() {
	//return this.replace(/^\s+|\s+$/g,"");
	return this.replace(/(^\s*)|(\s*$)/g, "");
}

function checkSymbol(inputString){
	var patrn=/^[a-zA-Z0-9ㄱ-힣&\/$%_@-]+$/;  
	if (!patrn.exec(inputString)) 
	{ 
		alert('한글이나 영문 또는 숫자만 입력가능합니다.');
		return false;
	}else{
		return true;
	}
	
}
//function deleteSelectItem(oSelect) {
//	for ( var i = 0; i < oSelect.options.length; i++) {
//		if (i >= 0 && i <= oSelect.options.length - 1
//				&& oSelect.options[i].selected) {
//			oSelect.options[i] = null;
//			i--;
//		}
//	}
//}
//
//function addItem(oTargetSel) {
//	var willAdd = $("#editword")[0].innerHTML;
//	willAdd = willAdd.trim();
//	if (willAdd == "") {
//		alert("우선 대표단어를 써넣으세요!");
//		return;
//	}
//	
//	var oAdd = $("#selectadd").val();
//	oAdd = oAdd.trim();
//	if (oAdd == "") {
//		alert("추가할단어를 써넣으세요!");
//		return;
//	}
//	if(oAdd.length > 30){
//		alert("단어길이는 30자를 넘을 수 없습니다.\n현재글자수"+oAdd.length);
//		$("#selectadd").val("");
//		return;
//	}
//	if(!checkSymbol(oAdd))
//		return;
//	if(oAdd != ""){
//		$("#selectadd").val("");
//		var options = oTargetSel.options;
//		if(checkRedundancy(options, oAdd)){
//			alert(oAdd+" : 이미 리스트에 존재합니다.");
//			return;
//		}
//		
//		var oOption = new Option(oAdd,oAdd,true);
//		
//		if($.browser.msie && $.browser.version > 7.0) {
//			oTargetSel.add(oOption);
//		} else {
//			$(oTargetSel).append(oOption);
//		}
//	}
//}
//
//function checkRedundancy(options, word){
//	for ( var i = 0; i < options.length; i++) {
//		
//		if(word == options[i].value){
//			return true;
//		}
//	}
//	return false;
//}
//function addItemEdit(oTargetSel) {
//	var oAdd = $("#selectedit").val();
//	$("#selectedit").val("");
//	var oOption = document.createElement("option");
//
//	oOption.text = "" + oAdd;
//	oOption.value = "" + oAdd;
//	$(oTargetSel).append(oOption);
//}
//
//function deleteAll(oTargetSel) {
//	$(oTargetSel).children().remove();
//}
//
//function deleteWord() {
//	var selectedWord = $("#editword")[0].innerHTML;
//	var url = "dbservice.jsp";
//	if(selectedWord == '')
//		return;
//	
//	if(!confirm("Delete \""+selectedWord+"\"?")){
//		return;
//	}
//	$.ajax({
//			type:'POST',
//		 	url: url,
//		 	dataType:'text',
//			data: {
//					cmd: 12,
//					deleteword: selectedWord
//				  },
//			error: function(XMLHttpRequest, textStatus, errorThrown) {
//			},
//			success: function(data_obj) {
//				window.location.reload();  	
//			}
//		});	
//}
//function deleteWordForRecommend() {
//	var selectedWord = $("#editword")[0].innerHTML;
//	var url = "keywordService.jsp";
//	if(selectedWord == '')
//		return;
//	
//	if(!confirm("Delete \""+selectedWord+"\"?")){
//		return;
//	}
//	$.ajax({
//			type:'POST',
//		 	url: url,
//		 	dataType:'text',
//			data: {
//					cmd: 12,
//					deleteword: selectedWord
//				  },
//			error: function(XMLHttpRequest, textStatus, errorThrown) {
//			},
//			success: function(data_obj) {
//				window.location.reload();  	
//			}
//		});	
//}
function addSynonymWord(e){
	if(e){
		if(window.event){ // IE
			keynum = e.keyCode
		}else if(e.which){ // Netscape/Firefox/Opera
			keynum = e.which
		}
		
		if(keynum != 13)
			return;
	}
	
	var key = $.trim($("#synonymKey").val());
	var value = $.trim($("#synonymValue").val());

	if(value == ""){
		alert("추가할 단어를 입력하세요.");
		return;
	}
	
	var wordList = "";
	if(key != ""){
		wordList += "@" + key + ",";
	}
	wordList += value;
	
	$("#synonymWord").val(wordList);
	$("#addwordForm").submit();
	
}
function addRecommendWord(e){
	if(e){
		if(window.event){ // IE
			keynum = e.keyCode
		}else if(e.which){ // Netscape/Firefox/Opera
			keynum = e.which
		}
		
		if(keynum != 13)
			return;
	}
	
	var key = $.trim($("#recommendKey").val());
	var value = $.trim($("#recommendValue").val());

	if(value == ""){
		alert("추가할 단어를 입력하세요.");
		return;
	}
	
	var wordList = "";
	if(key != ""){
		wordList += "@" + key + ",";
	}
	wordList += value;
	
	$("#recommendWord").val(wordList);
	$("#addwordForm").submit();
	
}
//function addNewMajorWord(){
//	var majorWord = $("#newwordvalue").val();
//	majorWord = majorWord.trim();
//	if (majorWord == "") {
//		alert("추가할단어를 써넣으세요!");
//		return;
//	}
//	
//	if(majorWord.length > 30){
//		alert("단어길이는 30자를 넘을 수 없습니다.\n현재글자수"+majorWord.length);
//		$("#newwordvalue").val("");
//		return;
//	}
//	
//	if(!checkSymbol(majorWord))
//		return;
//	$("#newword").val(majorWord);
//	$("#editword")[0].innerHTML = majorWord;
//	document.addForm.cmd.value = "1"; //insert sql type
//	document.getElementById("dicselect").options.length = 0;
//	
//	$("#newwordvalue").val("");
//	$("#selectadd").focus();
//}
//function submitAddForm() {
//	$("#addForm").submit();
//	var addDiv = document.getElementById("addDiv");
//	addDiv.style.display = 'none';
//}

//function edit4Recommend(arg, oSelect) {
//	$(oSelect).children().remove();
//	var dickey = arg;
//	var dicvalue = "";
//	var url = "../dic/dbservice.jsp";
//	
//	$.ajax({
//			type:"post",
//		 	url: url,
//			data: {
//					cmd: 8,
//					keyword: arg
//				  },
//			error: function(XMLHttpRequest, textStatus, errorThrown) {
//			},
//			success: function(data_obj) {
//				dicvalue = data_obj;
//				
//				$("#newword").val(dickey);
//				
//				dicvalue = dicvalue.trim();
//				var words = dicvalue.split(",");
//				var l = words.length;
//				
//				for ( var i = 0; i < l; i++) {
//					if (words[i] == "") {
//						continue;
//					}
//					var oOption = document.createElement("option");
//					oOption.text = "" + words[i];
//					oOption.value = "" + words[i];
//
//					if($.browser.msie && $.browser.version >= 7.0) {
//						oSelect.add(oOption);
//					} else {
//						$(oSelect).append(oOption);
//					}
//
//				}
//				document.addForm.cmd.value = "2";
//				$("#editword")[0].innerHTML = dickey;
//				$("#newwordvalue").val("");
//			}
//		});	
//	
//}
//
//function edit(arg, oSelect) {
//	$(oSelect).children().remove();
//	var dickey = arg;
//	var dicvalue = "";
//	var url = "dbservice.jsp";
//	
//	$.ajax({
//			type:'POST',
//		 	url: url,
//			data: {
//					cmd: 5,
//					keyword: arg
//				  },
//			error: function(XMLHttpRequest, textStatus, errorThrown) {
//			},
//			success: function(data_obj) {
//				dicvalue = data_obj;
//				
//				$("#newword").val(dickey);
//				
//				dicvalue = dicvalue.trim();
//				var words = dicvalue.split(",");
//
//				var l = words.length;
//				
//				for ( var i = 0; i < l; i++) {
//					if (words[i] == "") {
//						continue;
//					}
//					var oOption = document.createElement("option");
//					oOption.text = "" + words[i];
//					oOption.value = "" + words[i];
//					if($.browser.msie && $.browser.version >= 7.0) {
//						oSelect.add(oOption);
//					} else {
//						$(oSelect).append(oOption);
//					}
//				}
//				document.addForm.cmd.value = "2";
//				$("#editword")[0].innerHTML = dickey;
//				$("#newwordvalue").val("");
//			}
//		});	
//	
//}
//function addNewWord(oSelect) {
//	var data = "";
//	if($("#editword")[0].innerHTML == ''){
//		return;
//	}
//	
//	if(oSelect.options.length == 0){
//		alert("No item!");
//		return;
//	}
//	
//	
//	
//	for ( var i = 0; i < oSelect.options.length; i++) {
//
//		var v = oSelect.options[i].value;
//		
//		if (i == (oSelect.options.length - 1)) {
//			data = data + "" + v;
//		} else {
//			data = data + "" + v + "\n";
//		}
//	}
//
//	$("#selectvalue").val(data);
//	$("#addForm").submit();
//	
//}
//
//function submitByEnterForNewwordvalue(e) {
//	e = e || window.event;
//	var key = e ? (e.charCode || e.keyCode) : 0;
//	if (key == 13) {
//		addNewMajorWord();
//	}
//}
//
//function submitByEnterForSelectadd(e) {
//	
//	e = e || window.event;
//	var key = e ? (e.charCode || e.keyCode) : 0;
//	if (key == 13) {
//		addItem(document.all.dicselect);
//	}
//}
//
//function listen() {
//	//add eventlistener to newwordvalue
//	var newwordvalue = document.getElementById('newwordvalue');
//	try {
//		newwordvalue.addEventListener('keydown', submitByEnterForNewwordvalue, false);
//	} catch (ex) {
//		newwordvalue.attachEvent('onkeydown', submitByEnterForNewwordvalue);
//	}
//	//add eventlistener to selectadd
//	var selectadd  = document.getElementById('selectadd');
//	
//	try {
//		selectadd.addEventListener('keydown', submitByEnterForSelectadd, false);
//		
//	} catch (ex) {
//		selectadd.attachEvent('onkeydown', submitByEnterForSelectadd);
//	}
//}
//
//function cancelAdd() {
//	document.getElementById('addDiv').style.display = 'none';
//	$("#newwordvalue").val("");
//}

function compileAndApplyDic(category, dicType){
	if(!confirm("사전적용을 시작합니다.\n완료창이 나타날때 까지 기다려주십시오.")){
		return;
	}
	$.ajax({
		  type:'POST',
		  url: 'dbservice.jsp',
		  data: {
			cmd: 14,
			category: category,
			dic: dicType
		  },
		  dataType: 'text',
		  error: function(XMLHttpRequest, textStatus, errorThrown) {
			  doneFailApply(errorThrown);
		  },
		  success: function(data_obj) {
		  	doneSuccessApply();
		  }
	});
	
}

function addCustomWord(e){
	if(e){
		if(window.event){ // IE
			keynum = e.keyCode
		}else if(e.which){ // Netscape/Firefox/Opera
			keynum = e.which
		}
		
		if(keynum != 13)
			return;
	}
	
	var word = $("#customword").val();
	
	word = word.trim();
	if(word == '')
		{
		alert("추가할단어를 써넣으세요!");
		return;
		}

	if(word.length > 30){
		alert("단어길이는 30자를 넘을 수 없습니다.\n현재글자수"+word.length);
		$("#customword").val("");
		return;
	}
	if(!checkSymbol(word))
		return;

	$("#customwordReal").val(word);
	$("#addwordForm").submit();
}

function deleteCustomWord(){
	$("#delete").submit();
}
function deleteSynonymWord(){
	$("#delete").submit();
}
function deleteRecommendWord(){
	$("#delete").submit();
}
function addBannedWord(e){
	if(e){
		if(window.event){ // IE
			keynum = e.keyCode
		}else if(e.which){ // Netscape/Firefox/Opera
			keynum = e.which
		}
		
		if(keynum != 13)
			return;
	}
	
	var word = $("#bannedword").val();
	word = word.trim();
	if(word == '')
		{
		alert("추가할단어를 써넣으세요!");
		return;
		}
	
	if(word.length > 30){
		alert("단어길이는 30자를 넘을 수 없습니다.\n현재글자수"+word.length);
		$("#bannedword").val("");
		return;
	}	
	if(!checkSymbol(word))
		return;

	$("#bannedwordReal").val(word);
	$("#addBannedWordForm").submit();
}

function deleteBannedWord(){
	$("#delete").submit();
}

function addBasicWord(e){
	if(e){
		if(window.event){ // IE
			keynum = e.keyCode
		}else if(e.which){ // Netscape/Firefox/Opera
			keynum = e.which
		}
		
		if(keynum != 13)
			return;
	}
	
	var word = $("#basicword").val();
	word = word.trim();
	if(word == '')
		{
		alert("추가할단어를 써넣으세요!");
		return;
		}
	if(word.length > 30){
		alert("단어길이는 30자를 넘을 수 없습니다.\n현재글자수"+word.length);
		$("#basicword").val("");
		return;
	}	
	if(!checkSymbol(word))
		return;

	$("#basicwordReal").val(word);
	$("#addwordForm").submit();
}

function deleteBasicWord(){
	document.getElementById("delete").submit();
}

function gotoDict(url, category){
	submitPost(url, {category: category});
}
