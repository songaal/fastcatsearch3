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
	// declaring variables
	var SEARCH_DATA_SIZE;
     //search query
    var hitChart;
    var hitData = [];
    //실패쿼리차트
    var failHitChart;
    var failHitData = [];
    
    var achitChart;
    var achitData = [];
    
    var acfailHitChart;
    var acfailHitData = [];
    
    //max response time
    var maxChart;
    var maxData = [];
    //average response time
    var aveChart;
    var aveData = [];
    
    
	var system_url = "/monitoring/search/detail";
    var search_url = "/monitoring/search/detail";
	
	var IS_TEST = "false"; //테스트데이터를 자동생성한다. 디버그용. true이면 테스트데이터 생성.
	
	var pollingType = '';
	var nowHour = 0;
	
	var hit2 = [];
	var fail2 = [];
	var achit2 = [];
	var acfail2 = [];
	var ave2 = [];
	var max2 = [];
	
	var hit3 = [];
	var fail3 = [];
	var achit3 = [];
	var acfail3 = [];
	var ave3 = [];
	var max3 = [];
	
	var times = [];
	
	var color_0 = "#b0de09";
	var color_1 = "#fcd202";
	var color_2 = "#ff6600";
	
	var tabTag = "hit";
	
	var csvHitData;
	var csvFailData;
	var csvAcHitData;
	var csvAcFailData;
	var csvMaxData;
	var csvAveData;
	
	var csvFileName = "";
	
	var hitHTML;
	var failHTML;
	var acHitHTML;
	var acFailHTML;
	var maxHTML;
	var aveHTML;
	
    window.onload = function() {
        $(document).ready(function(){
        	if ("hour" == typeTag) {
        		SEARCH_DATA_SIZE = 60;
        		$("#a_hour").attr("class","btn_s_on");
        	}else if ("day" == typeTag){
        		SEARCH_DATA_SIZE = 24;
        		$("#a_day").attr("class","btn_s_on");
        	}else if ("week" == typeTag){
        		SEARCH_DATA_SIZE = 7;
        		$("#a_week").attr("class","btn_s_on");
        	}else if ("month" == typeTag){
        		var year = $("#selectYear_0").val();
        		var mon = $("#selectMonth_0").val();
        		SEARCH_DATA_SIZE = getMonthDays(new Number(year),new Number(mon));
        		$("#a_month").attr("class","btn_s_on");
        	}else if ("year" == typeTag){
        		SEARCH_DATA_SIZE = 12;
        		$("#a_year").attr("class","btn_s_on");
        	}
        	showNow();
  	 		createHitChart();
  	 		createFailHitChart();
  	 		createacHitChart();
  	 		createacFailHitChart();
  	 		createMaxChart();
  	 		createAveChart();
			drawChart(typeTag);
		});                                    
    }
    
    function showNow(){
    	var dt = new Date();
		var year = dt.getFullYear();
		var mon = dt.getMonth() + 1;
		var day = dt.getDate() -1;
		var hour = dt.getHours();
		var min = dt.getMinutes();
		
		$('#selectHour option:eq('+hour+')').attr('selected', true);
		
		$('#selectMonth_0 option:eq('+(mon-1)+')').attr('selected', true);
		
		var start = year+'-'+makeTwoDigits(mon)+"-"+makeTwoDigits(day)+' 00:00:00';
		$("#selectWeek").html(week(start)[0].substring(0,10)+"~"+week(start)[1].substring(0,10));
    }
	function makeTwoDigits(i){
		if(i && i < 10) return "0"+i;
		else return i;
	}

// ------------------------------------------------
	function showHourSearchData_0(start_0, end_0, collection){
    	$.ajax({
		    url: system_url,
		    data: {test: IS_TEST, simple:"", start_0:start_0, end_0:end_0,start_1:"", end_1:"",start_2:"", end_2:"", type:"hour", collection:collection},
		    success: updateSearchData_0
    	});
    }
	function showHourSearchData_1(start_0, end_0, start_1, end_1, collection){
    	$.ajax({
		    url: system_url,
		    data: {test: IS_TEST, simple:"", start_0:start_0, end_0:end_0,start_1:start_1, end_1:end_1,start_2:"", end_2:"", type:"hour", collection:collection},
		    success: updateSearchData_1
    	});
    }
    function showHourSearchData_2(start_0, end_0, start_1, end_1, start_2, end_2, collection){
    	$.ajax({
		    url: system_url,
		    data: {test: IS_TEST, simple:"", start_0:start_0, end_0:end_0,start_1:start_1, end_1:end_1,start_2:start_2, end_2:end_2, type:"hour", collection:collection},
		    success: updateSearchData_2
    	});
    }
    //----------
    function showDaySearchData_0(start_0, end_0, collection){
    	$.ajax({
		    url: system_url,
		    data: {test: IS_TEST, simple:"", start_0:start_0, end_0:end_0,start_1:"", end_1:"",start_2:"", end_2:"", type:"day", collection:collection},
		    success: updateSearchData_0
    	});
    }
	function showDaySearchData_1(start_0, end_0, start_1, end_1,collection){
    	$.ajax({
		    url: system_url,
		    data: {test: IS_TEST, simple:"", start_0:start_0, end_0:end_0,start_1:start_1, end_1:end_1,start_2:"", end_2:"", type:"day", collection:collection},
		    success: updateSearchData_1
    	});
    }
    function showDaySearchData_2(start_0, end_0, start_1, end_1, start_2, end_2, collection){
    	$.ajax({
		    url: system_url,
		    data: {test: IS_TEST, simple:"", start_0:start_0, end_0:end_0,start_1:start_1, end_1:end_1,start_2:start_2, end_2:end_2, type:"day", collection:collection},
		    success: updateSearchData_2
    	});
    }
    //-------
    function showWeekSearchData_0(start_0, end_0, collection){
    	$.ajax({
		    url: system_url,
		    data: {test: IS_TEST, simple:"", start_0:start_0, end_0:end_0,start_1:"", end_1:"",start_2:"", end_2:"", type:"week", collection:collection},
		    success: updateSearchData_0
    	});
    }
	function showWeekSearchData_1(start_0, end_0, start_1, end_1, collection){
    	$.ajax({
		    url: system_url,
		    data: {test: IS_TEST, simple:"", start_0:start_0, end_0:end_0,start_1:start_1, end_1:end_1,start_2:"", end_2:"", type:"week", collection:collection},
		    success: updateSearchData_1
    	});
    }
    function showWeekSearchData_2(start_0, end_0, start_1, end_1, start_2, end_2, collection){
    	$.ajax({
		    url: system_url,
		    data: {test: IS_TEST, simple:"", start_0:start_0, end_0:end_0,start_1:start_1, end_1:end_1,start_2:start_2, end_2:end_2, type:"week", collection:collection},
		    success: updateSearchData_2
    	});
    }
    //--------
    function showMonthSearchData_0(start_0, end_0, collection){
    	$.ajax({
		    url: system_url,
		    data: {test: IS_TEST, simple:"", start_0:start_0, end_0:end_0,start_1:"", end_1:"",start_2:"", end_2:"", type:"month", collection:collection},
		    success: updateSearchData_0
    	});
    }
	function showMonthSearchData_1(start_0, end_0, start_1, end_1, collection){
    	$.ajax({
		    url: system_url,
		    data: {test: IS_TEST, simple:"", start_0:start_0, end_0:end_0,start_1:start_1, end_1:end_1,start_2:"", end_2:"", type:"month", collection:collection},
		    success: updateSearchData_1
    	});
    }
    function showMonthSearchData_2(start_0, end_0, start_1, end_1, start_2, end_2, collection){
    	$.ajax({
		    url: system_url,
		    data: {test: IS_TEST, simple:"", start_0:start_0, end_0:end_0,start_1:start_1, end_1:end_1,start_2:start_2, end_2:end_2, type:"month", collection:collection},
		    success: updateSearchData_2
    	});
    }
    //--------
    function showYearSearchData_0(start_0, end_0, collection){
    	$.ajax({
		    url: system_url,
		    data: {test: IS_TEST, simple:"", start_0:start_0, end_0:end_0,start_1:"", end_1:"",start_2:"", end_2:"", type:"year", collection:collection},
		    success: updateSearchData_0
    	});
    }
	function showYearSearchData_1(start_0, end_0, start_1, end_1, collection){
    	$.ajax({
		    url: system_url,
		    data: {test: IS_TEST, simple:"", start_0:start_0, end_0:end_0,start_1:start_1, end_1:end_1,start_2:"", end_2:"", type:"year", collection:collection},
		    success: updateSearchData_1
    	});
    }
    function showYearSearchData_2(start_0, end_0, start_1, end_1, start_2, end_2, collection){
    	$.ajax({
		    url: system_url,
		    data: {test: IS_TEST, simple:"", start_0:start_0, end_0:end_0,start_1:start_1, end_1:end_1,start_2:start_2, end_2:end_2, type:"year", collection:collection},
		    success: updateSearchData_2
    	});
    }
    //-------------------------------------------------------
    function updateSearchData_0(data){
		if(data.re_0){
			$("#hitDiv").css("display","block");
		   	$("#failDiv").css("display","block");
		   	$("#achitDiv").css("display","block");
		   	$("#acfailDiv").css("display","block");
		   	$("#maxDiv").css("display","block");
		   	$("#aveDiv").css("display","block");
		   	
			hitData = new Array();
			failHitData = new Array();
			acfailHitData = new Array();
			achitData = new Array();
			maxData = new Array();
			aveData = new Array();
			
			csvHitData = "";
			csvFailData = "";
			csvAcHitData = "";
			csvAcFailData = "";
			csvMaxData = "";
			csvAveData = "";
			
			hitHTML = "";
			failHTML = "";
			acHitHTML = "";
			acFailHTML = "";
			maxHTML = "";
			aveHTML = "";
			
	    	$.each(
				eval('('+data.re_0+')'),
				function(i, entity) {
					var id = entity.id;
					var hit = entity.hit;
					var fail = entity.fail;
					var achit = entity.achit;
					var acfail = entity.acfail;
					var ave = entity.ave;
					var max = entity.max;
					var time = entity.time.substring(11,16);
					if ("week" == typeTag){
		        		switch (i){
							case 0:
							time = "일요일";
						     break;
						   case 1:
						   time = "월요일";
						     break;
						   case 2:
						   time = "화요일";
						     break;
						   case 3:
						   time = "수요일";
						     break;
						   case 4:
						   time = "목요일";
						     break;
						   case 5:
						   time = "금요일";
						     break;
						   case 6:
						   time = "토요일";
						     break;
						   default:
							}
		        	}else if ("month" == typeTag){
		        		time = "D" + entity.time.substring(8,10);
		        	}else if ("year" == typeTag){
		        		time = (i+1) +"월";
		        	}else if ("hour" == typeTag){
		        		time = "M" + entity.time.substring(14,16);
		        	}else if ("day" == typeTag){
		        		time = "H" + entity.time.substring(11,13);
		        	}
		        	times[i] = time;
					//hit
			      	var hitObject = {index:time, hit: hit};
		      		hitData.push(hitObject);
			    	//fail
			      	var failObject = {index:time, fail: fail};
			      	failHitData.push(failObject);
			      	
			      	//achit
			      	var achitObject = {index:time, achit: achit};
		      		achitData.push(achitObject);
			    	//fail
			      	var acfailObject = {index:time, acfail: acfail};
			      	acfailHitData.push(acfailObject);
					 
			      	var maxObject = {index:time, max:max};
			      	maxData.push(maxObject);
			      	
			      	var aveObject = {index:time, ave:ave};
			      	aveData.push(aveObject);
			      	
			      	var collection = $("#collection").val();
			      	if(collection == "__global__"){
			      		collection = "전체";
			      	}
			      	hitHTML += "<tr><td>"+collection+"</td><td>"+time+"</td><td>"+hit+"</td><td>-</td><td>-</td><td>-</td><td>-</td></tr>";
			      	failHTML += "<tr><td>"+collection+"</td><td>"+time+"</td><td>"+fail+"</td><td>-</td><td>-</td><td>-</td><td>-</td></tr>";
			      	acHitHTML += "<tr><td>"+collection+"</td><td>"+time+"</td><td>"+achit+"</td><td>-</td><td>-</td><td>-</td><td>-</td></tr>";
			      	acFailHTML += "<tr><td>"+collection+"</td><td>"+time+"</td><td>"+acfail+"</td><td>-</td><td>-</td><td>-</td><td>-</td></tr>";
			      	maxHTML += "<tr><td>"+collection+"</td><td>"+time+"</td><td>"+max+"</td><td>-</td><td>-</td><td>-</td><td>-</td></tr>";
			      	aveHTML += "<tr><td>"+collection+"</td><td>"+time+"</td><td>"+ave+"</td><td>-</td><td>-</td><td>-</td><td>-</td></tr>";
			      	
			      	csvHitData += collection+","+time+","+hit+"\n";
			      	csvFailData += collection+","+time+","+fail+"\n";
			      	csvAcHitData += collection+","+time+","+achit+"\n";
			      	csvAcFailData += collection+","+time+","+acfail+"\n";
			      	csvMaxData += collection+","+time+","+max+"\n";
			      	csvAveData += collection+","+time+","+ave+"\n";
				}
	    	);
    		hitChart.dataProvider = hitData;
			failHitChart.dataProvider = failHitData;
			acfailHitChart.dataProvider = acfailHitData;
			achitChart.dataProvider = achitData;
			maxChart.dataProvider = maxData;
			aveChart.dataProvider = aveData;
			
	    	hitChart.validateData();
	    	failHitChart.validateData();
	    	achitChart.validateData();
	    	acfailHitChart.validateData();
	    	maxChart.validateData();
	    	aveChart.validateData();
	    	
	    	changeTab(tabTag);
		}
    }
    
      function updateSearchData_1(data){
      	var hits = [];
      	var fails = [];
      	var achits = [];
      	var acfails = [];
      	var aves = [];
      	var maxs = [];
		if(data.re_0){
	    	$.each(
				eval('('+data.re_0+')'),
				function(i, entity) {
					hits[i] = entity.hit;
					fails[i] = entity.fail;
					achits[i] = entity.achit;
					acfails[i] = entity.acfail;
					aves[i] = entity.ave;
					maxs[i] = entity.max;
				}
	    	);
		}
		if(data.re_1){
			$("#hitDiv").css("display","block");
		   	$("#failDiv").css("display","block");
		   	$("#achitDiv").css("display","block");
		   	$("#acfailDiv").css("display","block");
		   	$("#maxDiv").css("display","block");
		   	$("#aveDiv").css("display","block");
		   	
		   	csvHitData = "";
			csvFailData = "";
			csvAcHitData = "";
			csvAcFailData = "";
			csvMaxData = "";
			csvAveData = "";
			
			hitHTML = "";
			failHTML = "";
			acHitHTML = "";
			acFailHTML = "";
			maxHTML = "";
			aveHTML = "";
			
	    	$.each(
				eval('('+data.re_1+')'),
				function(i, entity) {
					var id = entity.id;
					var hit = entity.hit;
					var fail = entity.fail;
					var achit = entity.achit;
					var acfail = entity.acfail;
					var ave = entity.ave;
					var max = entity.max;
					//hit
			      	var hitObject = {index:times[i], hit: hits[i], hit2:hit};
		      		hitData[i] = hitObject;
			    	//fail
			      	var failObject = {index:times[i], fail: fails[i], fail2:fail};
			      	failHitData[i] = failObject;
			      	
			      	//achit
			      	var achitObject = {index:times[i], achit: achits[i], achit2:achit};
		      		achitData[i] = achitObject;
			    	//fail
			      	var acfailObject = {index:times[i], acfail: acfails[i], acfail2:acfail};
			      	acfailHitData[i] = acfailObject;
					 
			      	var maxObject = {index:times[i], max:maxs[i], max2:max};
			      	maxData[i] = maxObject;
			      	
			      	var aveObject = {index:times[i], ave:aves[i], ave2:ave};
			      	aveData[i] = aveObject;
			      	
			      	var collection = $("#collection").val();
			      	if(collection == "__global__"){
			      		collection = "전체";
			      	}
			      	hitHTML += "<tr><td>"+collection+"</td><td>"+times[i]+"</td><td>"+hits[i]+"</td><td>"+hit+"</td><td>"+computeRate(hits[i],hit)+"</td><td>-</td><td>-</td></tr>";
			      	failHTML += "<tr><td>"+collection+"</td><td>"+times[i]+"</td><td>"+fails[i]+"</td><td>"+fail+"</td><td>"+computeRate(fails[i],fail)+"</td><td>-</td><td>-</td></tr>";
			      	acHitHTML += "<tr><td>"+collection+"</td><td>"+times[i]+"</td><td>"+achits[i]+"</td><td>"+achit+"</td><td>"+computeRate(achits[i],achit)+"</td><td>-</td><td>-</td></tr>";
			      	acFailHTML += "<tr><td>"+collection+"</td><td>"+times[i]+"</td><td>"+acfails[i]+"</td><td>"+acfail+"</td><td>"+computeRate(acfails[i],acfail)+"</td><td>-</td><td>-</td></tr>";
			      	maxHTML += "<tr><td>"+collection+"</td><td>"+times[i]+"</td><td>"+maxs[i]+"</td><td>"+max+"</td><td>"+computeRate(maxs[i],max)+"</td><td>-</td><td>-</td></tr>";
			      	aveHTML += "<tr><td>"+collection+"</td><td>"+times[i]+"</td><td>"+aves[i]+"</td><td>"+ave+"</td><td>"+computeRate(aves[i],ave)+"</td><td>-</td><td>-</td></tr>";
			      	
			      	csvHitData += collection+","+times[i]+","+hits[i]+","+hit+","+computeRate(hits[i],hit)+"\n";
			      	csvFailData += collection+","+times[i]+","+fails[i]+","+fail+","+computeRate(fails[i],fail)+"\n";
			      	csvAcHitData += collection+","+times[i]+","+achits[i]+","+achit+","+computeRate(achits[i],achit)+"\n";
			      	csvAcFailData += collection+","+times[i]+","+acfails[i]+","+acfail+","+computeRate(acfails[i],acfail)+"\n";
			      	csvMaxData += collection+","+times[i]+","+maxs[i]+","+max+","+computeRate(maxs[i],max)+"\n";
			      	csvAveData += collection+","+times[i]+","+aves[i]+","+ave+","+computeRate(aves[i],ave)+"\n";
				}
	    	);
	    	hitChart.validateData();
	    	failHitChart.validateData();
	    	achitChart.validateData();
	    	acfailHitChart.validateData();
	    	maxChart.validateData();
	    	aveChart.validateData();
	    	changeTab(tabTag);
		}
    }
    
    function updateSearchData_2(data){
      	var hits = [];
      	var fails = [];
      	var achits = [];
      	var acfails = [];
      	var aves = [];
      	var maxs = [];
      	
      	var hits_2 = [];
      	var fails_2 = [];
      	var achits_2 = [];
      	var acfails_2 = [];
      	var aves_2 = [];
      	var maxs_2 = [];
      	
      	
		if(data.re_0){
	    	$.each(
				eval('('+data.re_0+')'),
				function(i, entity) {
					hits[i] = entity.hit;
					fails[i] = entity.fail;
					achits[i] = entity.achit;
					acfails[i] = entity.acfail;
					aves[i] = entity.ave;
					maxs[i] = entity.max;
				}
	    	);
		}
		if(data.re_1){
	    	$.each(
				eval('('+data.re_1+')'),
				function(i, entity) {
					hits_2[i] = entity.hit;
					fails_2[i] = entity.fail;
					achits_2[i] = entity.achit;
					acfails_2[i] = entity.acfail;
					aves_2[i] = entity.ave;
					maxs_2[i] = entity.max;
				}
	    	);
		}
		if(data.re_2){
			$("#hitDiv").css("display","block");
		   	$("#failDiv").css("display","block");
		   	$("#achitDiv").css("display","block");
		   	$("#acfailDiv").css("display","block");
		   	$("#maxDiv").css("display","block");
		   	$("#aveDiv").css("display","block");
		   	
		   	csvHitData = "";
			csvFailData = "";
			csvAcHitData = "";
			csvAcFailData = "";
			csvMaxData = "";
			csvAveData = "";
			
			hitHTML = "";
			failHTML = "";
			acHitHTML = "";
			acFailHTML = "";
			maxHTML = "";
			aveHTML = "";
			
	    	$.each(
				eval('('+data.re_2+')'),
				function(i, entity) {
					var id = entity.id;
					var hit = entity.hit;
					var fail = entity.fail;
					var achit = entity.achit;
					var acfail = entity.acfail;
					var ave = entity.ave;
					var max = entity.max;
					//hit {index:time, mem: mems[i], mem2:mems_2[i], mem3:mem};
			      	var hitObject = {index:times[i], hit: hits[i], hit2:hits_2[i], hit3:hit};
		      		hitData[i] = hitObject;
			    	//fail
			      	var failObject = {index:times[i], fail: fails[i], fail2:fails_2[i], fail3:fail};
			      	failHitData[i] = failObject;
			      	
			      	//achit
			      	var achitObject = {index:times[i], achit: achits[i], achit2:achits_2[i], achit3:achit};
		      		achitData[i] = achitObject;
			    	//fail
			      	var acfailObject = {index:times[i], acfail: acfails[i], acfail2:acfails_2[i], acfail3:acfail};
			      	acfailHitData[i] = acfailObject;
					 
			      	var maxObject = {index:times[i], max:maxs[i], max2:maxs_2[i], max3:max};
			      	maxData[i] = maxObject;
			      	
			      	var aveObject = {index:times[i], ave:aves[i], ave2:aves_2[i], ave3:ave};
			      	aveData[i] = aveObject;
			      	
			      	var collection = $("#collection").val();
			      	if(collection == "__global__"){
			      		collection = "전체";
			      	}
			      	hitHTML += "<tr><td>"+collection+"</td><td>"+times[i]+"</td><td>"+hits[i]+"</td><td>"+hits_2[i]+"</td><td>"+computeRate(hits[i],hits_2[i])+"</td><td>"+hit+"</td><td>"+computeRate(hits[i],hit)+"</td></tr>";
			      	failHTML += "<tr><td>"+collection+"</td><td>"+times[i]+"</td><td>"+fails[i]+"</td><td>"+fails_2[i]+"</td><td>"+computeRate(fails[i],fails_2[i])+"</td><td>"+fail+"</td><td>"+computeRate(fails[i],fail)+"</td></tr>";
			      	acHitHTML += "<tr><td>"+collection+"</td><td>"+times[i]+"</td><td>"+achits[i]+"</td><td>"+achits_2[i]+"</td><td>"+computeRate(achits[i],achits_2[i])+"</td><td>"+achit+"</td><td>"+computeRate(achits[i],achit)+"</td></tr>";
			      	acFailHTML += "<tr><td>"+collection+"</td><td>"+times[i]+"</td><td>"+acfails[i]+"</td><td>"+acfails_2[i]+"</td><td>"+computeRate(acfails[i],acfails_2[i])+"</td><td>"+acfail+"</td><td>"+computeRate(acfails[i],acfail)+"</td></tr>";
			      	maxHTML += "<tr><td>"+collection+"</td><td>"+times[i]+"</td><td>"+maxs[i]+"</td><td>"+maxs_2[i]+"</td><td>"+computeRate(maxs[i],maxs_2[i])+"</td><td>"+max+"</td><td>"+computeRate(maxs[i],max)+"</td></tr>";
			      	aveHTML += "<tr><td>"+collection+"</td><td>"+times[i]+"</td><td>"+aves[i]+"</td><td>"+aves_2[i]+"</td><td>"+computeRate(aves[i],aves_2[i])+"</td><td>"+ave+"</td><td>"+computeRate(aves[i],ave)+"</td></tr>";
			      	
			      	csvHitData += collection+","+times[i]+","+hits[i]+","+hits_2[i]+","+computeRate(hits[i],hits_2[i])+","+hit+","+computeRate(hits[i],hit)+"\n";
			      	csvFailData += collection+","+times[i]+","+fails[i]+","+fails_2[i]+","+computeRate(fails[i],fails_2[i])+","+fail+","+computeRate(fails[i],fail)+"\n";
			      	csvAcHitData += collection+","+times[i]+","+achits[i]+","+achits_2[i]+","+computeRate(achits[i],achits_2[i])+","+achit+","+computeRate(achits[i],achit)+"\n";
			      	csvAcFailData += collection+","+times[i]+","+acfails[i]+","+acfails_2[i]+","+computeRate(acfails[i],acfails_2[i])+","+acfail+","+computeRate(acfails[i],acfail)+"\n";
			      	csvMaxData += collection+","+times[i]+","+maxs[i]+","+maxs_2[i]+","+computeRate(maxs[i],maxs_2[i])+","+max+","+computeRate(maxs[i],max)+"\n";
			      	csvAveData += collection+","+times[i]+","+aves[i]+","+aves_2[i]+","+computeRate(aves[i],aves_2[i])+","+ave+","+computeRate(aves[i],ave)+"\n";
				}
	    	);
	    	hitChart.validateData();
	    	failHitChart.validateData();
	    	achitChart.validateData();
	    	acfailHitChart.validateData();
	    	maxChart.validateData();
	    	aveChart.validateData();
	    	changeTab(tabTag);
		}
    }
    
    
// ------------------------------------------------   
    
    // create hit chart
    function createHitChart(){
    	// SERIAL CHART    
        hitChart = new AmCharts.AmSerialChart();
        hitChart.dataProvider = hitData;

        hitChart.categoryField = "index";
        hitChart.addTitle("검색처리수(개)", 10);

        // category
        var categoryAxis = hitChart.categoryAxis;
        categoryAxis.gridAlpha = 0.07;
        categoryAxis.axisColor = "#DADADA";
        categoryAxis.autoGridCount = false;
        if ("hour" == typeTag) {
    		categoryAxis.gridCount = 12;
    	}else if ("day" == typeTag){
    		categoryAxis.gridCount = 12;
    	}else if ("week" == typeTag){
    		categoryAxis.gridCount = 7;
    	}else if ("month" == typeTag){
    		categoryAxis.gridCount = 6;
    	}else if ("year" == typeTag){
    		categoryAxis.gridCount = 12;
    	}

		 // GRAPH
        var graph = new AmCharts.AmGraph();
        graph.valueField = "hit";
        graph.title = "현재";
        graph.balloonText = "[[value]]개";
        graph.lineThickness  = 2;
        graph.fillAlphas = -1; 
        graph.lineColor = color_0;
        hitChart.addGraph(graph);
        
    	 // Value
        var valueAxis = new AmCharts.ValueAxis();
        valueAxis.integersOnly = true;
        hitChart.addValueAxis(valueAxis);

    	var graph = new AmCharts.AmGraph();
        graph.valueField = "hit2";
        graph.title = "비교1";
        graph.balloonText = "[[value]]개";
        graph.lineThickness  = 2;
        graph.fillAlphas = -1;
        graph.lineColor = color_1; 
        hitChart.addGraph(graph);
        
        var graph = new AmCharts.AmGraph();
        graph.valueField = "hit3";
        graph.title = "비교2";
        graph.balloonText = "[[value]]개";
        graph.lineThickness  = 2;
        graph.fillAlphas = -1; 
        graph.lineColor = color_2;
        hitChart.addGraph(graph);

        // CURSOR
        var chartCursor = new AmCharts.ChartCursor();
        chartCursor.cursorAlpha = 0.3;
        hitChart.addChartCursor(chartCursor);
		hitChart.pathToImages = "/admin/js/amcharts/images/";

        // WRITE
        hitChart.write("chartSearchActDiv");
        for (var i=0; i < SEARCH_DATA_SIZE; i++) {
      		var hitObject = {index: "", hit: 0};
	  		hitData.push(hitObject);
    	};
    	
    	hitChart.validateData();
    }  
    
    
    // 실패쿼리
    function createFailHitChart(){
    	// SERIAL CHART    
        failHitChart = new AmCharts.AmSerialChart();
        failHitChart.dataProvider = failHitData;
        failHitChart.categoryField = "index";
        failHitChart.addTitle("검색실패수(개)", 10);

        // category
        var categoryAxis = failHitChart.categoryAxis;
        categoryAxis.gridAlpha = 0.07;
        categoryAxis.axisColor = "#DADADA";
        categoryAxis.autoGridCount = false;
        if ("hour" == typeTag) {
    		categoryAxis.gridCount = 12;
    	}else if ("day" == typeTag){
    		categoryAxis.gridCount = 12;
    	}else if ("week" == typeTag){
    		categoryAxis.gridCount = 7;
    	}else if ("month" == typeTag){
    		categoryAxis.gridCount = 6;
    	}else if ("year" == typeTag){
    		categoryAxis.gridCount = 12;
    	}
 
        // GRAPH
        var graph = new AmCharts.AmGraph();
        graph.valueField = "fail";
        graph.title = "현재";
        graph.balloonText = "[[value]]개";
        graph.lineThickness  = 2;
        graph.fillAlphas = -1; 
        graph.lineColor = color_0;
        failHitChart.addGraph(graph);
        
    	 // Value
        var valueAxis = new AmCharts.ValueAxis();
        valueAxis.integersOnly = true;
        failHitChart.addValueAxis(valueAxis);

    	var graph = new AmCharts.AmGraph();
        graph.valueField = "fail2";
        graph.title = "비교1";
        graph.balloonText = "[[value]]개";
        graph.lineThickness  = 2;
        graph.fillAlphas = -1; 
        graph.lineColor = color_1;
        failHitChart.addGraph(graph);
        var graph = new AmCharts.AmGraph();
        graph.valueField = "fail3";
        graph.title = "비교2";
        graph.balloonText = "[[value]]개";
        graph.lineThickness  = 2;
        graph.fillAlphas = -1; 
        graph.lineColor = color_2;
        failHitChart.addGraph(graph);

        // CURSOR
        var chartCursor = new AmCharts.ChartCursor();
        chartCursor.cursorAlpha = 0.3;
        failHitChart.addChartCursor(chartCursor);
		failHitChart.pathToImages = "/admin/js/amcharts/images/";

        // WRITE
        failHitChart.write("chartSearchFailDiv");
    	for (var i=0; i < SEARCH_DATA_SIZE; i++) {
      		var failObject = {index: "", fail: 0};
	  		failHitData.push(failObject);
    	};
    	failHitChart.validateData();
    } 
    
     // create hit chart
    function createacHitChart(){
    	// SERIAL CHART    
        achitChart = new AmCharts.AmSerialChart();
        achitChart.dataProvider = achitData;

        achitChart.categoryField = "index";
        achitChart.addTitle("누적 검색처리수(개)", 10);

        // category
        var categoryAxis = achitChart.categoryAxis;
        categoryAxis.gridAlpha = 0.07;
        categoryAxis.axisColor = "#DADADA";
        categoryAxis.autoGridCount = false;
        if ("hour" == typeTag) {
    		categoryAxis.gridCount = 12;
    	}else if ("day" == typeTag){
    		categoryAxis.gridCount = 12;
    	}else if ("week" == typeTag){
    		categoryAxis.gridCount = 7;
    	}else if ("month" == typeTag){
    		categoryAxis.gridCount = 6;
    	}else if ("year" == typeTag){
    		categoryAxis.gridCount = 12;
    	}

		 // GRAPH
        var graph = new AmCharts.AmGraph();
        graph.valueField = "achit";
        graph.title = "현재";
        graph.balloonText = "[[value]]개";
        graph.lineThickness  = 2;
        graph.fillAlphas = -1; 
        graph.lineColor = color_0;
        achitChart.addGraph(graph);
        
    	 // Value
        var valueAxis = new AmCharts.ValueAxis();
        valueAxis.integersOnly = true;
        achitChart.addValueAxis(valueAxis);

    	var graph = new AmCharts.AmGraph();
        graph.valueField = "achit2";
        graph.title = "비교1";
        graph.balloonText = "[[value]]개";
        graph.lineThickness  = 2;
        graph.fillAlphas = -1;
        graph.lineColor = color_1; 
        achitChart.addGraph(graph);
        
        var graph = new AmCharts.AmGraph();
        graph.valueField = "achit3";
        graph.title = "비교2";
        graph.balloonText = "[[value]]개";
        graph.lineThickness  = 2;
        graph.fillAlphas = -1; 
        graph.lineColor = color_2;
        achitChart.addGraph(graph);

        // CURSOR
        var chartCursor = new AmCharts.ChartCursor();
        chartCursor.cursorAlpha = 0.3;
        achitChart.addChartCursor(chartCursor);
		achitChart.pathToImages = "/admin/js/amcharts/images/";

        // WRITE
        achitChart.write("acchartSearchActDiv");
        for (var i=0; i < SEARCH_DATA_SIZE; i++) {
      		var achitObject = {index: "", achit: 0};
	  		achitData.push(achitObject);
    	};
    	
    	achitChart.validateData();
    }  
    
    
    // 실패쿼리
    function createacFailHitChart(){
    	// SERIAL CHART    
        acfailHitChart = new AmCharts.AmSerialChart();
        acfailHitChart.dataProvider = acfailHitData;
        acfailHitChart.categoryField = "index";
        acfailHitChart.addTitle("누적 검색실패수(개)", 10);

        // category
        var categoryAxis = acfailHitChart.categoryAxis;
        categoryAxis.gridAlpha = 0.07;
        categoryAxis.axisColor = "#DADADA";
        categoryAxis.autoGridCount = false;
        if ("hour" == typeTag) {
    		categoryAxis.gridCount = 12;
    	}else if ("day" == typeTag){
    		categoryAxis.gridCount = 12;
    	}else if ("week" == typeTag){
    		categoryAxis.gridCount = 7;
    	}else if ("month" == typeTag){
    		categoryAxis.gridCount = 6;
    	}else if ("year" == typeTag){
    		categoryAxis.gridCount = 12;
    	}
 
        // GRAPH
        var graph = new AmCharts.AmGraph();
        graph.valueField = "acfail";
        graph.title = "현재";
        graph.balloonText = "[[value]]개";
        graph.lineThickness  = 2;
        graph.fillAlphas = -1; 
        graph.lineColor = color_0;
        acfailHitChart.addGraph(graph);
        
    	 // Value
        var valueAxis = new AmCharts.ValueAxis();
        valueAxis.integersOnly = true;
        acfailHitChart.addValueAxis(valueAxis);

    	var graph = new AmCharts.AmGraph();
        graph.valueField = "acfail2";
        graph.title = "비교1";
        graph.balloonText = "[[value]]개";
        graph.lineThickness  = 2;
        graph.fillAlphas = -1; 
        graph.lineColor = color_1;
        acfailHitChart.addGraph(graph);
        var graph = new AmCharts.AmGraph();
        graph.valueField = "acfail3";
        graph.title = "비교2";
        graph.balloonText = "[[value]]개";
        graph.lineThickness  = 2;
        graph.fillAlphas = -1; 
        graph.lineColor = color_2;
        acfailHitChart.addGraph(graph);

        // CURSOR
        var chartCursor = new AmCharts.ChartCursor();
        chartCursor.cursorAlpha = 0.3;
        acfailHitChart.addChartCursor(chartCursor);
		acfailHitChart.pathToImages = "/admin/js/amcharts/images/";

        // WRITE
        acfailHitChart.write("acchartSearchFailDiv");
    	for (var i=0; i < SEARCH_DATA_SIZE; i++) {
      		var acfailObject = {index: "", acfail: 0};
	  		acfailHitData.push(acfailObject);
    	};
    	acfailHitChart.validateData();
    } 
    
    // 최대 응답시간
    function createMaxChart(){
    	// SERIAL CHART    
        maxChart = new AmCharts.AmSerialChart();
        maxChart.dataProvider = maxData;
        maxChart.categoryField = "index";
        maxChart.addTitle("최대 검색응답시간(ms)", 10);

        // category
        var categoryAxis = maxChart.categoryAxis;
        categoryAxis.gridAlpha = 0.07;
        categoryAxis.axisColor = "#DADADA";
        categoryAxis.autoGridCount = false;
        if ("hour" == typeTag) {
    		categoryAxis.gridCount = 12;
    	}else if ("day" == typeTag){
    		categoryAxis.gridCount = 12;
    	}else if ("week" == typeTag){
    		categoryAxis.gridCount = 7;
    	}else if ("month" == typeTag){
    		categoryAxis.gridCount = 6;
    	}else if ("year" == typeTag){
    		categoryAxis.gridCount = 12;
    	}
        
        // 최대응답시간 GRAPH
        var graph = new AmCharts.AmGraph();
        graph.valueField = "max";
        graph.title = "현재1";
        graph.balloonText = "최대 [[value]]ms";
        graph.lineThickness  = 2;
        graph.fillAlphas = -1; 
        graph.lineColor = color_0;
        maxChart.addGraph(graph);
        
        
    	// Value
        var valueAxis = new AmCharts.ValueAxis();
        valueAxis.integersOnly = true;
        maxChart.addValueAxis(valueAxis);

    	var graph = new AmCharts.AmGraph();
        graph.valueField = "max2";
        graph.title = "비교11";
        graph.balloonText = "최대 [[value]]ms";
        graph.lineThickness  = 2;
        graph.fillAlphas = -1; 
        graph.lineColor = color_1;
        maxChart.addGraph(graph);
         
        var graph = new AmCharts.AmGraph();
        graph.valueField = "max3";
        graph.title = "비교21";
        graph.balloonText = "최대 [[value]]ms";
        graph.lineThickness  = 2;
        graph.fillAlphas = -1; 
        graph.lineColor = color_2;
        maxChart.addGraph(graph);

        // CURSOR
        var chartCursor = new AmCharts.ChartCursor();
        chartCursor.cursorAlpha = 0.3;
        maxChart.addChartCursor(chartCursor);
		maxChart.pathToImages = "/admin/js/amcharts/images/";

        // WRITE
        maxChart.write("chartMaxDiv");
        for (var i=0; i < SEARCH_DATA_SIZE; i++) {
      		var timeObject = {index: "", max:0};
	  		maxData.push(timeObject);
    	};
    	
    	maxChart.validateData();
    } 
    
    //평균 응답시간
    function createAveChart(){
    	// SERIAL CHART    
        aveChart = new AmCharts.AmSerialChart();
        aveChart.dataProvider = aveData;
        aveChart.categoryField = "index";
        aveChart.addTitle("평균 검색응답시간(ms)", 10);

        // category
        var categoryAxis = aveChart.categoryAxis;
        categoryAxis.gridAlpha = 0.07;
        categoryAxis.axisColor = "#DADADA";
        categoryAxis.autoGridCount = false;
        if ("hour" == typeTag) {
    		categoryAxis.gridCount = 12;
    	}else if ("day" == typeTag){
    		categoryAxis.gridCount = 12;
    	}else if ("week" == typeTag){
    		categoryAxis.gridCount = 7;
    	}else if ("month" == typeTag){
    		categoryAxis.gridCount = 6;
    	}else if ("year" == typeTag){
    		categoryAxis.gridCount = 12;
    	}
        
        // 평균응답시간 GRAPH
        var graph = new AmCharts.AmGraph();
        graph.valueField = "ave";
        graph.title = "현재1";
        graph.balloonText = "평균 [[value]]ms";
        graph.lineThickness  = 2;
        graph.fillAlphas = -1; 
        graph.lineColor = color_0;
        aveChart.addGraph(graph);
        
    	// Value
        var valueAxis = new AmCharts.ValueAxis();
        valueAxis.integersOnly = true;
        aveChart.addValueAxis(valueAxis);

        var graph = new AmCharts.AmGraph();
        graph.valueField = "ave2";
        graph.title = "비교12";
        graph.balloonText = "평균 [[value]]ms";
        graph.lineThickness  = 2;
        graph.fillAlphas = -1; 
        graph.lineColor = color_1;
        aveChart.addGraph(graph);
         
        var graph = new AmCharts.AmGraph();
        graph.valueField = "ave3";
        graph.title = "비교22";
        graph.balloonText = "평균 [[value]]ms";
        graph.lineThickness  = 2;
        graph.fillAlphas = -1; 
        graph.lineColor = color_2;
        aveChart.addGraph(graph);

        // CURSOR
        var chartCursor = new AmCharts.ChartCursor();
        chartCursor.cursorAlpha = 0.3;
        aveChart.addChartCursor(chartCursor);
		aveChart.pathToImages = "/admin/js/amcharts/images/";

        // WRITE
        aveChart.write("chartAveDiv");
        for (var i=0; i < SEARCH_DATA_SIZE; i++) {
      		var timeObject = {index: "", ave: 0};
	  		aveData.push(timeObject);
    	};
    	
    	aveChart.validateData();
    } 
    
    
    //---------------------------------------------------------------------------------

	function drawChart(type){
		
		var collection = $("#collection").val();
		var start_0;
		var start_1;
		var start_2;
		var end_0;
		var end_1;
		var end_2;
		var dateVar = makeTwoDigits($("#selectDate").val());
		var hourVar = makeTwoDigits($("#selectHour").val());
		if(dateVar){
			if (hourVar && hourVar != "시") {
				start_0 = dateVar+' '+hourVar+':00:00';
				end_0 = dateVar+' '+hourVar+':59:59';
			}else{
				start_0 = dateVar+' 00:00:00';
				end_0 = dateVar+' 23:59:59';
			}
		}else{
			start_0 = "";
			end_0 = "";
		}
		dateVar = makeTwoDigits($("#selectDate_2").val());
		hourVar = makeTwoDigits($("#selectHour_2").val());
		if(dateVar != "0000-00"){
			if (hourVar && hourVar != "시") {
				start_1 = dateVar+' '+hourVar+':00:00';
				end_1 = dateVar+' '+hourVar+':59:59';
			}else{
				start_1 = dateVar+' 00:00:00';
				end_1 = dateVar+' 23:59:59';
			}
		}else{
			start_1 = "";
			end_1 = "";
		}
		
		dateVar = makeTwoDigits($("#selectDate_3").val());
		hourVar = makeTwoDigits($("#selectHour_3").val());
		if(dateVar != "0000-00"){
			if (hourVar && hourVar != "시") {
				start_2 = dateVar+' '+hourVar+':00:00';
				end_2 = dateVar+' '+hourVar+':59:59';
			}else{
				start_2 = dateVar+' 00:00:00';
				end_2 = dateVar+' 23:59:59';
			}
		}else{
			start_2 = "";
			end_2 = "";
		}
		
		if($("#selectYear_0").val() && $("#selectYear_0").val() != ""){
			start_0 = $("#selectYear_0").val();
			end_0 = $("#selectMonth_0").val();
			if($("#selectYear_1").val() != "년" && $("#selectMonth_1").val() != "월"){
				start_1 = $("#selectYear_1").val();
				end_1 = $("#selectMonth_1").val();
			}else{
				start_1 = "";
				end_1 = "";
			}
			
			if($("#selectYear_2").val() != "년" && $("#selectMonth_2").val() != "월"){
				start_2 = $("#selectYear_2").val();
				end_2 = $("#selectMonth_2").val();
			}else{
				start_2 = "";
				end_2 = "";
			}
		}
		if (start_0 != "" && end_0 != "" && start_1 != "" && end_1 != "" && start_2 != "" && end_2 != "") {
			if("hour" == type){
				showHourSearchData_2(start_0, end_0, start_1, end_1, start_2, end_2, collection);
			}else if("day" == type){
				showDaySearchData_2(start_0, end_0, start_1, end_1, start_2, end_2, collection);
			}else if("week" == type){
				showWeekSearchData_2(week(start_0)[0], week(start_0)[1], week(start_1)[0], week(start_1)[1], week(start_2)[0], week(start_2)[1], collection);
			}else if("month" == type){
				var tp_0 = $("#selectYear_0").val()+"-"+makeTwoDigits($("#selectMonth_0").val())+"-01";
				var tp_1 = $("#selectYear_1").val()+"-"+makeTwoDigits($("#selectMonth_1").val())+"-01";
				var tp_2 = $("#selectYear_2").val()+"-"+makeTwoDigits($("#selectMonth_2").val())+"-01";
				showMonthSearchData_2(month(tp_0)[0], month(tp_0)[1], month(tp_1)[0], month(tp_1)[1], month(tp_2)[0], month(tp_2)[1], collection);
			}else if("year" == type){
				var tp_0 = $("#selectYear_0").val()+"-01-01";
				var tp_1 = $("#selectYear_1").val()+"-01-01";
				var tp_2 = $("#selectYear_2").val()+"-01-01";
				showYearSearchData_2(year(tp_0)[0], year(tp_0)[1], year(tp_1)[0], year(tp_1)[1], year(tp_2)[0], year(tp_2)[1], collection);
			}
		}else if (start_0 != "" && end_0 != "" && start_1 != "" && end_1 != "") {
			if("hour" == type){
				showHourSearchData_1(start_0, end_0, start_1, end_1, collection);
			}else if("day" == type){
				showDaySearchData_1(start_0, end_0, start_1, end_1, collection);
			}else if("week" == type){
				showWeekSearchData_1(week(start_0)[0], week(start_0)[1], week(start_1)[0], week(start_1)[1], collection);
			}else if("month" == type){
				var tp_0 = $("#selectYear_0").val()+"-"+makeTwoDigits($("#selectMonth_0").val())+"-01";
				var tp_1 = $("#selectYear_1").val()+"-"+makeTwoDigits($("#selectMonth_1").val())+"-01";
				var tp_2 = $("#selectYear_2").val()+"-"+makeTwoDigits($("#selectMonth_2").val())+"-01";
				showMonthSearchData_1(month(tp_0)[0], month(tp_0)[1], month(tp_1)[0], month(tp_1)[1], collection);
			}else if("year" == type){
				var tp_0 = $("#selectYear_0").val()+"-01-01";
				var tp_1 = $("#selectYear_1").val()+"-01-01";
				var tp_2 = $("#selectYear_2").val()+"-01-01";
				showYearSearchData_1(year(tp_0)[0], year(tp_0)[1], year(tp_1)[0], year(tp_1)[1], collection);
			}
		}else if (start_0 != "" && end_0 != "") {
			if("hour" == type){
				showHourSearchData_0(start_0, end_0, collection);
			}else if("day" == type){
				showDaySearchData_0(start_0, end_0, collection);
			}else if("week" == type){
				showWeekSearchData_0(week(start_0)[0], week(start_0)[1], collection);
			}else if("month" == type){
				var tp_0 = $("#selectYear_0").val()+"-"+makeTwoDigits($("#selectMonth_0").val())+"-01";
				var tp_1 = $("#selectYear_1").val()+"-"+makeTwoDigits($("#selectMonth_1").val())+"-01";
				var tp_2 = $("#selectYear_2").val()+"-"+makeTwoDigits($("#selectMonth_2").val())+"-01";
				showMonthSearchData_0(month(tp_0)[0], month(tp_0)[1], collection);
			}else if("year" == type){
				var tp_0 = $("#selectYear_0").val()+"-01-01";
				var tp_1 = $("#selectYear_1").val()+"-01-01";
				var tp_2 = $("#selectYear_2").val()+"-01-01";
				showYearSearchData_0(year(tp_0)[0], year(tp_0)[1], collection);
			}
		}
		
	}
	
	function init(){
		var start_0;
		var end_0;
		var dateVar = makeTwoDigits($("#selectDate").val());
		var hourVar = makeTwoDigits($("#selectHour").val());
		if(dateVar){
			if (hourVar && hourVar != "시") {
				start_0 = dateVar+' '+hourVar+':00:00';
				end_0 = dateVar+' '+hourVar+':59:59';
			}else{
				start_0 = dateVar+' 00:00:00';
				end_0 = dateVar+' 23:59:59';
			}
		}else{
			start_0 = "";
			end_0 = "";
		}
		
//		$("#hiddenCompare_0").css("display","block");
//		$("#hiddenCompare_1").css("display","none");
//		$("#hiddenCompare_2").css("display","none");
//		$("#hiddenCompare_3").css("display","none");
//		$("#hiddenCompare_4").css("display","none");
//		$("#hiddenCompare_5").css("display","none");
//		$("#hiddenCompare_6").css("display","none");
//		$("#hiddenCompare_7").css("display","none");
//		$("#hiddenCompare_8").css("display","none");
		
		if("hour" == typeTag){
				$("#selectDate_2").val("0000-00");
				$("#selectHour_2").val("시");
				$("#selectDate_3").val("0000-00");
				$("#selectHour_3").val("시");
				showHourSearchData_0(start_0, end_0);
			}else if("day" == typeTag){
				$("#selectDate_2").val("0000-00");
				$("#selectDate_3").val("0000-00");
				showDaySearchData_0(start_0, end_0);
			}else if("week" == typeTag){
				$("#selectDate_2").val("0000-00");
				$("#selectDate_3").val("0000-00");
				$("#hiddenCompare_3").html("0000 00-00~0000 00-00");
				$("#hiddenCompare_7").html("0000 00-00~0000 00-00");
				$("#selectWeek").html(week(start_0)[0].substring(0,10)+"~"+week(start_0)[1].substring(0,10));
				showWeekSearchData_0(week(start_0)[0], week(start_0)[1]);
			}else if("month" == typeTag){
				var tp_0 = $("#selectYear_0").val()+"-"+makeTwoDigits($("#selectMonth_0").val())+"-01";
				$("#selectYear_1").val("");
				$("#selectMonth_1").val("");
				$("#selectYear_2").val("");
				$("#selectMonth_2").val("");
				showMonthSearchData_0(month(tp_0)[0], month(tp_0)[1]);
			}else if("year" == typeTag){
				$("#selectYear_1").val("");
				$("#selectYear_2").val("");
				var tp_0 = $("#selectYear_0").val()+"-01-01";
				showYearSearchData_0(year(tp_0)[0], year(tp_0)[1]);
			}
	}
	
	function changeSelect(){
		if(typeTag == "hour"){
			if($("#selectDate_2").val() == "0000-00" || $("#selectHour_2").val() == "시"){
					if($("#selectDate_3").val() != "0000-00" || $("#selectHour_3").val() != "시"){
						$("#selectDate_3").val("0000-00");
						$("#selectHour_3").val("시");
						alert("위의 선택사항을 먼저 선택하세요.");
						return false;
					}else{
						return false;
					}
			}else{
				if($("#selectDate_3").val() == "0000-00" && $("#selectHour_3").val() == "시"){
				}else{
					if($("#selectDate_3").val() != "0000-00" && $("#selectHour_3").val() != "시"){
					}else{
						return false;
					}
				}
			}
		}else if(typeTag == "month"){
			if($("#selectYear_1").val() == "년" || $("#selectMonth_1").val() == "월"){
					if($("#selectYear_2").val() != "년" || $("#selectMonth_2").val() != "월"){
						$("#selectYear_2").val("년");
						$("#selectMonth_2").val("시");
						alert("위의 선택사항을 먼저 선택하세요.");
						return false;
					}else{
						return false;
					}
			}else{
				if($("#selectYear_2").val() == "년" && $("#selectMonth_2").val() == "월"){
				}else{
					if($("#selectYear_2").val() != "년" && $("#selectMonth_2").val() != "월"){
					}else{
						return false;
					}
				}
			}
		}else if(typeTag == "day" || typeTag == "week"){
			if($("#selectDate_2").val() == "0000-00" ){
					if($("#selectDate_3").val() != "0000-00"){
						$("#selectDate_3").val("0000-00");
						$("#hiddenCompare_7").html("0000 00-00~0000 00-00");
						alert("위의 선택사항을 먼저 선택하세요.");
						return false;
					}else{
						return false;
					}
			}
		}else if(typeTag == "week"){
			if($("#selectDate_2").val() == "0000-00" ){
					if($("#selectDate_3").val() != "0000-00"){
						$("#selectDate_3").val("0000-00");
						$("#hiddenCompare_7").html("0000 00-00~0000 00-00");
						alert("위의 선택사항을 먼저 선택하세요.");
						return false;
					}else{
						return false;
					}
			}
		}else if(typeTag == "year"){
			if($("#selectYear_1").val() == "년" ){
					if($("#selectYear_2").val() != "년"){
						$("#selectYear_2").val("년");
						alert("위의 선택사항을 먼저 선택하세요.");
						return false;
					}else{
						return false;
					}
			}
		}
		
		drawChart(typeTag);
	}
	
	function showHiddenHour(){
		
		if ($("#hiddenCompare_0").css("display") == "block") {
			$("#hiddenCompare_0").css("display","none");
			$("#hiddenCompare_1").css("display","block");
			$("#hiddenCompare_2").css("display","block");
			$("#hiddenCompare_3").css("display","block");
			$("#hiddenCompare_4").css("display","block");
			$("#hiddenCompare_5").css("display","block");
		} else{
			$("#hiddenCompare_5").css("display","none");
			$("#hiddenCompare_6").css("display","block");
			$("#hiddenCompare_7").css("display","block");
			$("#hiddenCompare_8").css("display","block");
		}
		
	}

	function changeShowType(showType){
		switch (showType){
			case "hour":
		   	window.location.href="searchStat.jsp?type=hour"; 
		     break;
		   case "day":
		   	window.location.href="searchStat.jsp?type=day"; 
		     break;
		   case "week":
		   	window.location.href="searchStat.jsp?type=week"; 
		     break;
		   case "month":
		   	window.location.href="searchStat.jsp?type=month"; 
		     break;
		   case "year":
		   	window.location.href="searchStat.jsp?type=year"; 
		     break;
		   default:
		}
	}
	
	function changeTab(tabType){
		tabTag = tabType;
		switch (tabType){
		   case "hit":
			$("#hitDiv").css("z-index","0");
			$("#failDiv").css("z-index","-1");
		   	$("#achitDiv").css("z-index","-1");
		   	$("#acfailDiv").css("z-index","-1");
		   	$("#maxDiv").css("z-index","-1");
		   	$("#aveDiv").css("z-index","-1");
		   	$("#dataTable").html(hitHTML);
		   	
		   	$("#a_hit").attr("class","btn_s_on");
		   	$("#a_fail").attr("class","btn_s");
		   	$("#a_achit").attr("class","btn_s");
		   	$("#a_acfail").attr("class","btn_s");
		   	$("#a_max").attr("class","btn_s");
		   	$("#a_ave").attr("class","btn_s");
		     break;
		   case "fail":
			$("#hitDiv").css("z-index","-1");
			$("#failDiv").css("z-index","0");
		   	$("#achitDiv").css("z-index","-1");
		   	$("#acfailDiv").css("z-index","-1");
		   	$("#maxDiv").css("z-index","-1");
		   	$("#aveDiv").css("z-index","-1");
		   	$("#dataTable").html(failHTML);
		   	
		   	$("#a_hit").attr("class","btn_s");
		   	$("#a_fail").attr("class","btn_s_on");
		   	$("#a_achit").attr("class","btn_s");
		   	$("#a_acfail").attr("class","btn_s");
		   	$("#a_max").attr("class","btn_s");
		   	$("#a_ave").attr("class","btn_s");
		     break;
		   case "achit":
		   	$("#hitDiv").css("z-index","-1");
		   	$("#failDiv").css("z-index","-1");
		   	$("#achitDiv").css("z-index","0");
		   	$("#acfailDiv").css("z-index","-1");
		   	$("#maxDiv").css("z-index","-1");
		   	$("#aveDiv").css("z-index","-1");
		   	$("#dataTable").html(acHitHTML);
		   	
		   	$("#a_hit").attr("class","btn_s");
		   	$("#a_fail").attr("class","btn_s");
		   	$("#a_achit").attr("class","btn_s_on");
		   	$("#a_acfail").attr("class","btn_s");
		   	$("#a_max").attr("class","btn_s");
		   	$("#a_ave").attr("class","btn_s");
		     break;
		   case "acfail":
		   	$("#hitDiv").css("z-index","-1");
		   	$("#failDiv").css("z-index","-1");
		   	$("#achitDiv").css("z-index","-1");
		   	$("#acfailDiv").css("z-index","0");
		   	$("#maxDiv").css("z-index","-1");
		   	$("#aveDiv").css("z-index","-1");
		   	$("#dataTable").html(acFailHTML);
		   	
		   	$("#a_hit").attr("class","btn_s");
		   	$("#a_fail").attr("class","btn_s");
		   	$("#a_achit").attr("class","btn_s");
		   	$("#a_acfail").attr("class","btn_s_on");
		   	$("#a_max").attr("class","btn_s");
		   	$("#a_ave").attr("class","btn_s");
		     break;
		   case "max":
		   	$("#hitDiv").css("z-index","-1");
		   	$("#failDiv").css("z-index","-1");
		   	$("#achitDiv").css("z-index","-1");
		   	$("#acfailDiv").css("z-index","-1");
		   	$("#maxDiv").css("z-index","0");
		   	$("#aveDiv").css("z-index","-1");
		   	$("#dataTable").html(maxHTML);
		   	
		   	$("#a_hit").attr("class","btn_s");
		   	$("#a_fail").attr("class","btn_s");
		   	$("#a_achit").attr("class","btn_s");
		   	$("#a_acfail").attr("class","btn_s");
		   	$("#a_max").attr("class","btn_s_on");
		   	$("#a_ave").attr("class","btn_s");
		     break;
		   case "ave":
		   	$("#hitDiv").css("z-index","-1");
		   	$("#failDiv").css("z-index","-1");
		   	$("#achitDiv").css("z-index","-1");
		   	$("#acfailDiv").css("z-index","-1");
		   	$("#maxDiv").css("z-index","-1");
		   	$("#aveDiv").css("z-index","0");
		   	$("#dataTable").html(aveHTML);
		   	
		   	$("#a_hit").attr("class","btn_s");
		   	$("#a_fail").attr("class","btn_s");
		   	$("#a_achit").attr("class","btn_s");
		   	$("#a_acfail").attr("class","btn_s");
		   	$("#a_max").attr("class","btn_s");
		   	$("#a_ave").attr("class","btn_s_on");
		     break;
		   default:
		}
		
		if(parseInt($("#dataTable").css("height")) < 300){
			$("#tbDiv").css("height",$("#dataTable").css("height"));
		}else{
			$("#tbDiv").css("height","300px");
		}
			
		if("hour" == typeTag){
			var h1;
			if(makeTwoDigits($("#selectHour").val()) == "시"){
				h1 = "00";
			}else{
				h1 = "H"+makeTwoDigits($("#selectHour").val());
			}
			var h2;
			if(makeTwoDigits($("#selectHour_2").val()) == "시"){
				h2 = "00";
			}else{
				h2 = "H"+makeTwoDigits($("#selectHour_2").val());
			}
			var h3;
			if(makeTwoDigits($("#selectHour_3").val()) == "시"){
				h3 = "00";
			}else{
				h3 = "H"+makeTwoDigits($("#selectHour_3").val());
			}
			$("#stime").html("기준시간<br>"+$("#selectDate").val()+"-"+h1);
			$("#ctime_1").html("비교시간1<br>"+$("#selectDate_2").val()+"-"+h2);
			$("#ctime_2").html("비교시간2<br>"+$("#selectDate_3").val()+"-"+h3);
		}else if("day" == typeTag){
			$("#stime").html("기준시간<br>"+$("#selectDate").val());
			$("#ctime_1").html("비교시간1<br>"+$("#selectDate_2").val());
			$("#ctime_2").html("비교시간2<br>"+$("#selectDate_3").val());
		}else if("week" == typeTag){
			$("#stime").html("기준시간<br>"+$("#selectWeek").html().replace("~","<br>~"));
			$("#ctime_1").html("비교시간1<br>"+$("#hiddenCompare_3").html().replace("~","<br>~"));
			$("#ctime_2").html("비교시간2<br>"+$("#hiddenCompare_7").html().replace("~","<br>~"));
		}else if("month" == typeTag){
			if($("#selectYear_0").val() == "년" || makeTwoDigits($("#selectMonth_0").val()) == "월"){
				$("#stime").html("기준시간<br>0000-00");
			}else{
				$("#stime").html("기준시간<br>"+$("#selectYear_0").val()+"-"+makeTwoDigits($("#selectMonth_0").val()));
			}
			
			if($("#selectYear_1").val() == "년" || makeTwoDigits($("#selectMonth_1").val()) == "월"){
				$("#ctime_1").html("비교시간1<br>0000-00");
			}else{
				$("#ctime_1").html("비교시간1<br>"+$("#selectYear_1").val()+"-"+makeTwoDigits($("#selectMonth_1").val()));
			}
			
			if($("#selectYear_2").val() == "년" || makeTwoDigits($("#selectMonth_2").val()) == "월"){
				$("#ctime_2").html("비교시간2<br>0000-00");
			}else{
				$("#ctime_2").html("비교시간2<br>"+$("#selectYear_2").val()+"-"+makeTwoDigits($("#selectMonth_2").val()));
			}
		}else if("year" == typeTag){
			if($("#selectYear_0").val() == "년"){
				$("#stime").html("기준시간<br>0000");
			}else{
				$("#stime").html("기준시간<br>"+$("#selectYear_0").val());
			}
			if($("#selectYear_1").val() == "년"){
				$("#ctime_1").html("비교시간1<br>0000");
			}else{
				$("#ctime_1").html("비교시간1<br>"+$("#selectYear_1").val());
			}
			
			if($("#selectYear_2").val() == "년"){
				$("#ctime_2").html("비교시간1<br>0000");
			}else{
				$("#ctime_2").html("비교시간1<br>"+$("#selectYear_2").val());
			}
		}
	}
	
	function downloadCSV(){
		var collection = $("#collection").val();
		if(collection == "__global__"){
			collection = "전체";
		}
		if("hour" == typeTag){
			csvFileName = collection+"-"+tabTag + "-" + $("#selectDate").val()+"-H"+$("#selectHour").val() + ".csv";
		}else if("day" == typeTag){
			csvFileName = collection+"-"+tabTag + "-D" + $("#selectDate").val()+".csv";
		}else if("week" == typeTag){
			csvFileName = collection+"-"+tabTag +"-"+$("#selectDate").val().substring(0,7)+ "-w" + whichWeekMonth($("#selectDate").val())+".csv";
		}else if("month" == typeTag){
			csvFileName = collection+"-"+tabTag + "-M" + $("#selectYear_0").val()+"-"+$("#selectMonth_0").val() + ".csv";
		}else if("year" == typeTag){
			$("#stime").html("기준시간<br>"+$("#selectYear_0").val());
			$("#ctime_1").html("비교시간1<br>"+$("#selectYear_1").val());
			$("#ctime_2").html("비교시간2<br>"+$("#selectYear_2").val());
			csvFileName = collection+"-"+tabTag + "-Y" + $("#selectYear_0").val() + ".csv";
		}
		document.csvForm.filename.value = csvFileName;
		switch (tabTag){
			case "hit":
				document.csvForm.data.value = "컬렉션,시간,"+$("#stime").html().replace("<br>"," ").replace("<br>"," ")+","+$("#ctime_1").html().replace("<br>"," ").replace("<br>"," ")+",비교시간1증감율,"+$("#ctime_2").html().replace("<br>"," ").replace("<br>"," ")+",비교시간2증감율"+"\n" + csvHitData;
		     break;
		   case "fail":
			   document.csvForm.data.value = "컬렉션,시간,"+$("#stime").html().replace("<br>"," ").replace("<br>"," ")+","+$("#ctime_1").html().replace("<br>"," ").replace("<br>"," ")+",비교시간1증감율,"+$("#ctime_2").html().replace("<br>"," ").replace("<br>"," ")+",비교시간2증감율"+"\n" + csvFailData;
		     break;
		   case "achit":
			   document.csvForm.data.value = "컬렉션,시간,"+$("#stime").html().replace("<br>"," ").replace("<br>"," ")+","+$("#ctime_1").html().replace("<br>"," ").replace("<br>"," ")+",비교시간1증감율,"+$("#ctime_2").html().replace("<br>"," ").replace("<br>"," ")+",비교시간2증감율"+"\n"  + csvAcHitData;
		   	break;
		   case "max":
			   document.csvForm.data.value = "컬렉션,시간,"+$("#stime").html().replace("<br>"," ").replace("<br>"," ")+","+$("#ctime_1").html().replace("<br>"," ").replace("<br>"," ")+",비교시간1증감율,"+$("#ctime_2").html().replace("<br>"," ").replace("<br>"," ")+",비교시간2증감율"+"\n" + csvMaxData;
		   	break;
		   case "ave":
			   document.csvForm.data.value = "컬렉션,시간,"+$("#stime").html().replace("<br>"," ").replace("<br>"," ")+","+$("#ctime_1").html().replace("<br>"," ").replace("<br>"," ")+",비교시간1증감율,"+$("#ctime_2").html().replace("<br>"," ").replace("<br>"," ")+",비교시간2증감율"+"\n" + csvAveData;
		     break;
		   default:
		}
		document.csvForm.submit();
	}

	function computeRate(main, comp){
		var result = "";
		
		if (comp == 0) {
			result = "∞";
		} else{
			var rate = (main - comp)/comp * 100;
			if(rate == 100)
				rate = 0;
			result = rate.toFixed(1) + "%";
		}
		
		return result;
	}
