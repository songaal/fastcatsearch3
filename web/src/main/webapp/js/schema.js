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
	return this.replace(/(^\s*)|(\s*$)/g, "");
}

function editableTable(){
	$(function() { 
	var collectionName = $("#chooseCollection").text();  
	$(".editable").each(function() {   
	     
	        var objTD = $(this);   
	        var oldText = $.trim(objTD.text()); 
	          var inputLength = 0;
	          if(oldText.length == 0){
	          	inputLength = objTD.width()/10;
	          }else{
	          	inputLength = oldText.length;
	          }
	        var input = $("<input type='text' id= '"+objTD.attr("id")+"' size='4' class='inp02'  maxlength='3' value='" + oldText + "' />");   
	          
	        objTD.html(input);   
	          
	        input.click(function() {   
	           return false;   
	        });   
	          
	        input.css("font-size", objTD.css("font-size"));   
	        input.css("text-align", objTD.css("text-align"));   
	          
	       // input.select();   
	        
	    }); 
	    $(".editFieldName").each(function() {   
	     
	        var objTD = $(this);   
	        var oldText = $.trim(objTD.text()); 
	          var inputLength = 0;
	          if(oldText.length == 0){
	          	inputLength = objTD.width()/10;
	          }else{
	          	inputLength = oldText.length;
	          }
	        var input = $("<input type='text' id= '"+objTD.attr("id")+"' size='16' maxlength='26' class='inp02 alphanumeric' value='" + oldText + "' />");   
	          
	        objTD.html(input);   
	          
	        input.click(function() {   
	           return false;   
	        });   
	          
	        input.css("font-size", objTD.css("font-size"));   
	        input.css("text-align", objTD.css("text-align"));   
	          
	       // input.select();   
	        
	    }); 
	   
	    $(".inp02").blur(function() {
	        var objTD = $(this);
            var newText = $(this).val();
            var id = $(this).attr("id");
            var url = "collectionService.jsp?cmd=7&collection=" + collectionName + "&key=" + id + "&value=" + newText;    
            if(id.substring(0,2) == "04"){
		 		if(isNumber(newText)){
		 			if(newText > 256){
			 			alert("사이즈는 최대 256까지 설정가능합니다.");
			 			$(this).val("256");
			 			return;
		 			}
		 		}else{
		 			alert("숫자를 입력하세요.");
		 			$(this).val("");
		 			return;
		 		}      
            }
            $.post(url, function(data) {
	            if(data.trim() == 0) {
	                objTD.val(newText);
	                if(id.indexOf("01") == 0){
	                	location.reload();
	                }
	            } else if(data.trim() == 1){  
	            	alert('문법에 맞지 않습니다.');
	            	location.reload();    
	            } else if(data.trim() == 2){  
	            	//값이 변경되지 않음. 무시
	            } else if(data.trim() == 3){
	            	alert('파일관련 에러발생.');
	            	location.reload(); 
	            } else if(data.trim() == 9){  
	            	location.reload(); 
	            }
	           // ischange();  
            });   
            
	    });
	    
	   $(".chk").click(function() {
	        var objTD = $(this); 
	        var chkValue = "";
	        if(objTD.attr("checked") ){
	        	chkValue = "true";
	        }else{
	        	chkValue = "false";
	        }  
	           
	        var id = $(this).attr("id");//prev().text();   
            var url = "collectionService.jsp?cmd=7&collection=" + collectionName + "&key=" + id + "&value=" + chkValue;    
              
            $.post(url, function(data) {   
            	if(data.trim() == 0) {   
	            } else if(data.trim() == 1){  
	            	alert('문법에 맞지 않습니다.');
	            	location.reload();    
	            } else if(data.trim() == 2){  
	            	//값이 변경되지 않음. 무시
	            } else if(data.trim() == 3){  
	            	alert('파일관련 에러발생.');
	            	location.reload(); 
	            } else if(data.trim() == 9){  
	            	location.reload();    
	            }
	           // ischange();  
            });   
	    });
	    
	    $(".slt").change(function() {
	        var objTD = $(this); 
	        var sltValue = objTD.attr("value");
	            var id = $(this).attr("id");//prev().text(); 
	            var url = "collectionService.jsp?cmd=7&collection=" + collectionName + "&key=" + id + "&value=" + sltValue;    
	              
	            $.post(url, function(data) {
	            	if(data.trim() == 0) {   
	            		
		            } else if(data.trim() == 1){  
		            	alert('문법에 맞지 않습니다.');
		            	location.reload();    
		            } else if(data.trim() == 2){  
		            	//값이 변경되지 않음. 무시
		            } else if(data.trim() == 3){  
		            	alert('파일관련 에러발생.');
		            	location.reload(); 
		            } else if(data.trim() == 9){  
		            	location.reload();    
		            }
	            });   
	    });
	    
	    $(".sltindex").change(function() {
	        var objTD = $(this); 
	        var sltValue = objTD.attr("value");
	        if(sltValue.length > 1){
	        	//sltValue = "org.fastcatsearch.ir.analysis."+sltValue+"Tokenizer";
	        }else{
	        	sltValue = "";
	        }
	            var id = $(this).attr("id");  
	            var url = "collectionService.jsp?cmd=7&collection=" + collectionName + "&key=" + id + "&value=" + sltValue;    
	            $.post(url, function(data) {   
	            	if(data.trim() == 0) {   
		                 
		            } else if(data.trim() == 1){  
		            	alert('문법에 맞지 않습니다.');
		            	location.reload();    
		            } else if(data.trim() == 2){  
		            	//값이 변경되지 않음. 무시
		            } else if(data.trim() == 3){  
		            	alert('파일관련 에러발생.');
		            	location.reload(); 
		            } else if(data.trim() == 9){  
		            	location.reload();    
		            }
	            });   
	    });
	        
	}); 
}

function ischange(){
	$(function() {   
		    $(".editable").each(function() {   
		    
		     var collectionName = $("#chooseCollection").val();
		        var objTD = $(this);   
		            var id = objTD.attr("id");//prev().text();   
		            var url2 = "collectionService.jsp?cmd=9&collection=" + collectionName + "&key=" + id;
		             $.post(url2, function(data) {
		                
			            if(data.trim() == 1) {   
			                 objTD.css("background-color", "grey");
			                 $("#recover"+id.substr(2)).show();  
			            }
		            });
		    });   
		});
}

function goEditorPage(collection){
	if(confirm("작업본을 편집하겠습니까? 수정된 작업본은 다음 전체색인시 적용됩니다.")){
		window.location.href="schemaEditor.jsp?collection="+collection;
	}
}

function finish(collection) {
	var url = "collectionService.jsp?cmd=11&collection="+collection;
	
	$.post(url, function(data) {
	   if(data.trim() == 1) {
		   alert("주키(PK)필드는 반드시 설정해야 합니다.");
//	   }else if(data.trim() == 2) {   
//		   alert("검색용도의 색인필드가 적어도 하나 존재해야 합니다.");
	   }else{
		   window.location.href = "schema.jsp?collection="+collection;
	   }
	});
}

function startEdit(collection) {
	
	window.location.href = "schemaEditor.jsp?collection="+collection;
}

function addField(collection) {
	var url = "collectionService.jsp?cmd=4&collection="+collection;
     $.post(url, function(data) {
     
        if(data.trim() == 0) {
          startEdit(collection);
        }
        if(data.trim() == 1) {   
           alert('필드추가가 실패했습니다.'); 
           location.reload(); 
        }
    });
}

function deleteField(collection) {
	var x = document.getElementsByName("selectField");
	var deleteFieldName = "";
	for(i=0;i<x.length;i++){
		if(x[i].checked){
			deleteFieldName = x[i].value;
			break;
		}
	}
	
	if(deleteFieldName == ""){
		alert("삭제할 필드를 선택하세요.");
		return false;
	}
	
	 var bln = window.confirm(deleteFieldName+" 필드를 삭제하겠습니까?");
	 if(!bln){
	 	return false;
	 }   
	 
	var url = "collectionService.jsp?cmd=5&collection="+collection+"&fieldname="+deleteFieldName;
     $.post(url, function(data) {
     
        if(data.trim() == 0) {
          startEdit(collection);
        }
        if(data.trim() == 1) {   
           alert('필드삭제가 실패했습니다.'); 
           location.reload(); 
        }
    });
   
}

function deleteWorkSchema(collection) {
	
	 var bln = window.confirm("스키마 작업본을 삭제하겠습니까?");
	 if(!bln){
	 	return false;
	 }   
	var url = "collectionService.jsp?cmd=10&collection="+collection;
     $.post(url, function(data) {
     
        if(data.trim() == 0) {
          window.location.href = "schema.jsp?collection="+collection;
        }
        if(data.trim() == 1) {   
           alert('파일삭제가 실패했습니다.'); 
           location.reload(); 
        }
    });
}

function isNumber(n) {
  return !isNaN(parseFloat(n)) && isFinite(n);
}
