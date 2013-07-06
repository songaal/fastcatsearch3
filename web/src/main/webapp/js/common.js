/*
 * 
#-------------------------------------------------------------------------------
# Copyright (C) 2011 - 2013 Websquared, Inc.
# All rights reserved.
#-------------------------------------------------------------------------------
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

var update_event_url = "/admin/management/moniteringService.jsp";
var SE_UPDATE_FAIL_COMM = "이벤트내역 업데이트에 실패했습니다.";

// login form class change
function chgClass(cv){
	var cid = document.getElementById("id");
	var cpw = document.getElementById("pw");
	if (cv == "id") cid.className = "inp_log";
	else{
		if (cid.value != "") cid.className = "inp_log";
		else cid.className = "inp_log_id";
	}
	if (cv == "pw") cpw.className = "inp_log";
	else{
		if (cpw.value != "") cpw.className = "inp_log";
		else cpw.className = "inp_log_pw";
	}
}

//popup    
var tip={
	$:function(ele){ 
	    if(typeof(ele)=="object") 
	        return ele; 
	    else if(typeof(ele)=="string"||typeof(ele)=="number") 
	        return document.getElementById(ele.toString()); 
	        return null; 
    }, 
    mousePos:function(e){ 
        var x,y; 
        var e = e||window.event; 
        return{x:e.clientX+document.body.scrollLeft+document.documentElement.scrollLeft,y:e.clientY+document.body.scrollTop+document.documentElement.scrollTop}; 
    }, 
    start:function(obj){ 
        var self = this; 
        var t = self.$("mjs:tip"); 
        obj.onmousemove=function(e){ 
            var mouse = self.mousePos(e);
            var wx;
            var selectSize = $("#sizeSelector").find("option:selected").val();
			switch (selectSize){
			   case "1240":
			     wx = 320;
			     break;
			   case "1024":
			     wx = 420;
			     break;
			   case "100":
			     wx = -10;
			     break;
			   case "90":
			     wx = 80;
			     break;
			   default:
			}
            t.style.left = mouse.x - wx + 'px'; 
            t.style.top = mouse.y - 120 + 'px'; 
            var tips = obj.getAttribute("tips");
			if("" == tips){
				return;
			}
            t.innerHTML = tips;
            t.style.display = ''; 
        } 
        obj.onmouseout=function(){ 
            t.style.display = 'none'; 
        } 
    } 
}
   
function handleEvent(id){
		$.ajax({
		    url: update_event_url,
		    data: {id: id, status:"T", cmd:0},
		    success: function (data){
		    	if($.trim(data) == "success"){
		    		$("#td_"+id).html("처리됨");
		    	}else{
		    		alert(SE_UPDATE_FAIL_COMM);
		    	}
		    }
    	});
		return;
} 

function expandEvent(id){
		var stacktrace = $("#tips_"+id).attr("tips");
		var tb = $("#td_"+id).parent();
		var nextTb = tb.next();
		if(nextTb.children().length > 1 || nextTb.children().length == 0){
			tb.after("<tr style='background:#e3e3e3;'><td colspan=5 style='text-align:left;'>"+stacktrace+"</td></tr>");
		}else{
			nextTb.remove();
		}
}

//쿠키 생성
function setCookie(cName, cValue){
	setCookie(cName, cValue, 7);
}
function setCookie(cName, cValue, cDay){
     var expire = new Date();
     expire.setDate(expire.getDate() + cDay);
     cookies = cName + '=' + escape(cValue) + '; path=/ '; // 한글 깨짐을 막기위해 escape(cValue)를 합니다.
     if(typeof cDay != 'undefined') cookies += ';expires=' + expire.toGMTString() + ';';
     document.cookie = cookies;
}

// 쿠키 가져오기
function getCookie(cName) {
	return getCookie(cName, '');
}
function getCookie(cName, defaultValue) {
     cName = cName + '=';
     var cookieData = document.cookie;
     var start = cookieData.indexOf(cName);
     var cValue = '';
     if(start != -1){
          start += cName.length;
          var end = cookieData.indexOf(';', start);
          if(end == -1)end = cookieData.length;
          cValue = cookieData.substring(start, end);
     }else{
    	 return defaultValue;
     }
     return unescape(cValue);
}


function submitGet(url, data){
	submitForm(url, data, "GET");
}
function submitPost(url, data){
	submitForm(url, data, "POST");
}
//가상의 폼을 만들어서 sumit한다.
function submitForm(url, data, method){
	
    $('body').append($('<form/>', {
		id : 'jQueryPostItForm',
		method : method,
		action : url
	}));

	for ( var i in data) {
		$('#jQueryPostItForm').append($('<input/>', {
			type : 'hidden',
			name : i,
			value : data[i]
		}));
	}

	$('#jQueryPostItForm').submit();
}