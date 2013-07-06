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
	var SYSTEM_DATA_SIZE = 30;
	var KEYWORD_LOG_SIZE = 100;
	var FAIL_COUNT_LIMIT = 10;
	var FAIL_COMMENT = "검색엔진 접속에 실패했습니다.";
	var SE_UPDATE_FAIL_COMM = "이벤트내역 업데이트에 실패했습니다.";
	//메모리 차트
    var memChart;
    var memData = [];
    var maxMemory;
    //cpu 차트
    var cpuChart;
    var cpuData = [];
    //load average
    var avgLoadChart;
    var avgLoadData = [];
    //search query
    var hitChart;
    var hitData = [];
    //average response time
    var responseTimeChart;
    var responseTimeData = [];
    //실패쿼리차트
    var failHitChart;
    var failHitData = [];
    
    //전체색인정보 차트
    var fullIndexChart;
    var incIndexChart;
    var indexDocChart;
    
    var jvm_cpu_support;
	var load_avg_support;
	var jvm_memory_support;
    
	var lastIndexInfoUpdateTime = 0;
	var lastPopularListUpdateTime = 0;
	var lastEventUpdateTime  = 0;
	var system_url = "/monitoring/system";
    var search_url = "/monitoring/search";
	var indexing_info_url = "/monitoring/indexing";
	var keyword_url = "/monitoring/keywordList";
	var popular_url = "/keyword/popular";
	var search_event_url = "/monitoring/eventList";
	
	var IS_TEST = ""; //테스트데이터를 자동생성한다. 디버그용. true이면 테스트데이터 생성.
	
	var cpuColor = "#339900";
	var memoryColor = "#330066";
	var loadColor = "#330066";
	var hitColor = "#3366cc";
	var timeColor = "#666600";
	var failHitColor = "#ff9900";
	var deleteColor = "#C72C95";
	var updateColor = "#666600";
	var insertColor = "#95ACCB";
	var docColor = "#999999";
	var logColorA = "#B6C4D4";
	var logColorB = "#DCE4F0";
	
	
	
	var logkeywordIntervalId;
	var pollingDataIntervalId;
	var popularIntervalId;
	var indexIntervalId;
	var searchEventIntervalId;
	
	var failCount = 0;
	
    // this method called after all page contents are loaded
    window.onload = function() {
        checkAvailable();
        //처음엔 무조건 색인상태와 인기검색어를 한번 출력한다.
        pollingIndexingData();
        pollingSearchEvent();
        
        $(document).ready(function(){
			$("#log_switch").click(
				function() {	
					if ($(this).attr("checked")) {
						logkeywordIntervalId = setInterval(logKeyword, 1000);
					}else{
						clearInterval(logkeywordIntervalId);
					}		
				});
			$("#monitering_switch").click(
				function() {	
					if ($(this).attr("checked")) {
						pollingData();
						pollingDataIntervalId = setInterval(pollingData, 1000);
					}else{
						clearInterval(pollingDataIntervalId);
					}		
				});
			$("#indexing_switch").click(
				function() {	
					if ($(this).attr("checked")) {
						pollingIndexingData();
						indexIntervalId = setInterval(pollingIndexingData, 1000);
					}else{
						clearInterval(indexIntervalId);
					}		
				});
			$("#event_switch").click(
				function() {	
					if ($(this).attr("checked")) {
						pollingSearchEvent();
        				searchEventIntervalId = setInterval(pollingSearchEvent, 1000);
					}else{
						clearInterval(searchEventIntervalId);
					}		
				});
			$("#popular_switch").click(
				function() {	
					if ($(this).attr("checked")) {
						pollingPopularKeyword();
        				popularIntervalId = setInterval(pollingPopularKeyword, 1000);
					}else{
						clearInterval(popularIntervalId);
					}		
				});
				
				
			$("#main_switch").click(
				function() {	
					if ($(this).attr("checked")) {
						clearInterval(indexIntervalId);
						clearInterval(pollingDataIntervalId);
						clearInterval(logkeywordIntervalId);
						clearInterval(popularIntervalId);
						clearInterval(searchEventIntervalId);
						
						pollingIndexingData();
						pollingData();
						pollingDataIntervalId = setInterval(pollingData, 1000);
						indexIntervalId = setInterval(pollingIndexingData, 1000);
						logkeywordIntervalId = setInterval(logKeyword, 1000);
						pollingPopularKeyword();
        				popularIntervalId = setInterval(pollingPopularKeyword, 1000);
        				pollingSearchEvent();
        				searchEventIntervalId = setInterval(pollingSearchEvent, 1000);
        				
						$("#indexing_switch").attr("checked",true);
						$("#monitering_switch").attr("checked",true);
						$("#log_switch").attr("checked",true);
						$("#popular_switch").attr("checked",true);
						$("#event_switch").attr("checked",true);
					}else{
						clearInterval(indexIntervalId);
						clearInterval(pollingDataIntervalId);
						clearInterval(logkeywordIntervalId);
						clearInterval(popularIntervalId);
						clearInterval(searchEventIntervalId);
						$("#indexing_switch").attr("checked",false);
						$("#monitering_switch").attr("checked",false);
						$("#log_switch").attr("checked",false);
						$("#popular_switch").attr("checked",false);
						$("#event_switch").attr("checked",false);
					}		
				});
		});                                    
    }
    
    function logKeyword() {
		  $.ajax({
		    url: keyword_url,
		    data: {test: IS_TEST},
		    success: function (data){
		    	if(data.length > 0){
		    		var color = getColor();
		    		
					$.each(
						data,
						function(i, entity) {
							var key = entity.key;
							$("#keyLogDiv").append("<p style='background:"+color+";'>"+key+"</p>");
						}
					 );
				
					$("#keyLogDiv").append("<p style='background:"+color+";'><i>"+nowFormat()+"</i></p>");
					if($("#keyLogDiv p").length > KEYWORD_LOG_SIZE){
						var toRemove = $("#keyLogDiv p").length - KEYWORD_LOG_SIZE;
						$("#keyLogDiv p:lt("+toRemove+")").remove();
					}
					$("#keyLogDiv").scrollTop($("#keyLogDiv")[0].scrollHeight);
				}
		    },
		    error: handleFail
		 });
	}
	
    colorSeq = 0;
	function getColor(){
  		if(colorSeq++ % 2 == 0){
  			return logColorA;
  		}else{
  			return logColorB;
  		}
	}
	
	function handleFail(){
		failCount++;
		if(failCount > FAIL_COUNT_LIMIT){
			clearInterval(logkeywordIntervalId);
			clearInterval(pollingDataIntervalId);
			clearInterval(indexIntervalId);
        	clearInterval(popularIntervalId); 
        	clearInterval(searchEventIntervalId); 
			$("#log_switch").attr("checked", false);
			$("#monitering_switch").attr("checked", false);
			$("#indexing_switch").attr("checked", false);
			failCount = 0;
			alert(FAIL_COMMENT);
		}
	}
	
	function nowFormat(){
		var d = new Date();
  		var curr_date = d.getDate();
  		var curr_month = makeTwoDigits(d.getMonth() + 1); 
  		var curr_year = makeTwoDigits(d.getFullYear());
  		var curr_hour = makeTwoDigits(d.getHours());
  		var curr_minute = makeTwoDigits(d.getMinutes());
  		var curr_second = makeTwoDigits(d.getSeconds());
  		return curr_year + "-" + curr_month + "-" + curr_date + " " + curr_hour + ":" + curr_minute + ":" + curr_second;
	}
	function makeTwoDigits(i){
		if(i < 10) return "0"+i;
		else return i;
	}

    function checkAvailable(){
    	 $.ajax({
		    url: system_url,
		    data: {q: "", test: IS_TEST},
		    success: function (data)
		    {
				jvm_cpu_support = data.jvm_cpu_support;
				load_avg_support = data.load_avg_support;
				jvm_memory_support =  data.jvm_memory_support;
			  	
			  	createCPUChart();
			  	createMemChart();    
		  	 	createLoadChart();
		  	 	
		  	 	createFullIndexChart();
  	 			createIncIndexChart();
  	 			createIndexDocChart();
		     }
		 });
  	 	
  	 	createHitChart();
  	 	createFailHitChart();
  	 	createResponseTimeChart();
    }
    
    // method which loads external data
    function pollingData() {
    	pollingSystemData();
    	pollingSearchData();
    }
    
    function pollingSystemData(){
    	$.ajax({
		    url: system_url,
		    data: {test: IS_TEST, simple:""},
		    success: updateSystemData,
		    error:handleFail
    	});
    }
    
    function pollingSearchData(){
    	$.ajax({
		    url: search_url,
		    data: {test: IS_TEST, simple:""},
		    success: updateSearchData,
		    error:handleFail
		 });
    }
    
    function pollingIndexingData(){
		$.ajax({
		    url: indexing_info_url,
		    data: {q: lastIndexInfoUpdateTime},
		    success: function (data){
		    	if(data.update == true){
		    		lastIndexInfoUpdateTime = new Date().getTime();
		    		$.ajax({
		    		    url: indexing_info_url,
		    		    success: updateIndexingInfo,
		    		    error:handleFail
		    		});
		    	}
		    },
		    error:handleFail
		});
   }
   
   function pollingPopularKeyword(){
	   $.ajax({
		    url: popular_url,
		    data: {q: lastPopularListUpdateTime},
		    success: function (data){
		    	if(data.update == true){
		    		lastPopularListUpdateTime = new Date().getTime();
		    		$.ajax({
		    		    url: popular_url,
		    		    data: {new_version: "true"},
		    		    success: updatePopularKeyword,
		    		    error:handleFail
		    		});
		    	}
		    },
		    error:handleFail
		});
	   
    }
    
    function pollingSearchEvent(){
    	 $.ajax({
 		    url: search_event_url,
 		    data: {q: lastEventUpdateTime},
 		    success: function (data){
 		    	if(data.update == true){
 		    		lastEventUpdateTime = new Date().getTime();
 		    		$.ajax({
 		    		    url: search_event_url,
 		    		    success: updateSearchEvent,
 		    		    error:handleFail
 		    		});
 		    	}
 		    },
 		    error:handleFail
 		});
    }
    
    function updatePopularKeyword(data){
    	var popularList = "";
    	$.each(
			data.list,
			function(i, entity) {
				if(i > 9) return false;
				var term = entity.term;
				var preRank = entity.prevRank;
				var change = preRank - i - 1;
				if(change == 0)
					change = "-";
				if(preRank == 999)
					change = "new"
				
				popularList += '<tr><td class="first">'+(i+1)+'</td><td>'+term+'</td><td>'+change+'</td></tr>';
			}
		 );
		$("#popular_keywords").html(popularList);
    }
    
    function updateSearchEvent(data){
    	var eventList = "";
    	if(data){
	    	$.each(
				data,
				function(i, entity) {
					
					var id = entity.id;
					var type = entity.type;
					var when = entity.when;
					var category = entity.category;
					var summary = entity.summary;
					var status = entity.status;
					var stacktrace = entity.stacktrace;
					var statusHTML = "";
					if("T" == status){
						status = "처리됨";
						statusHTML = '<td id="td_'+id+'">'+status+'</td>';
					}else{
						status = "미처리";
						statusHTML = '<td id="td_'+id+'"><a onclick="handleEvent('+id+');" class="btn_s">'+status+'</a></td>';
					}
						
				eventList += '<tr>' + 
						  '<td>'+when+'</td>' +
						  '<td><strong class="small tb">'+type+'</strong></td>' +
						  '<td>'+category+'</td>' +
						  '<td onmouseover="tip.start(this)" tips="'+stacktrace+'">'+summary+'</td>' +
						  statusHTML +
						  '</tr>';
				}
	    	);
		}
		$("#searchEventDiv").html(eventList);
    }
    
    function updateSystemData(data){
//      	alert(data.mx+", "+data.mhm+", "+data.cj);
		//mem chart
		var totalMem = data.mx;
		var useMem = data.mu;
		
		//memory
       	memData.shift();
      	var memObject = {index:"", used: useMem, total: (totalMem)};
      	memData.push(memObject);
    	memChart.validateData();
    	
		//cpu
   		cpuData.shift();
      	var cpuObject = {index:"", java: data.cj};
      	cpuData.push(cpuObject);
		cpuChart.validateData();
		 
		if(load_avg_support){
			var loadAvg = data.la;
	       	avgLoadData.shift();
	      	var loadObject = {index:"", load:loadAvg};
	      	avgLoadData.push(loadObject);
			avgLoadChart.validateData();
		}
    }
    
    function updateSearchData(data){
		var hitObject = {index:"", hit: data.h, fail: data.fh};
		hitData.shift();
		hitData.push(hitObject);
		hitChart.validateData();
		failHitChart.validateData();
		
		var timeObject = {index:"", mean: data.ta, max: data.tx};
		responseTimeData.shift();
		responseTimeData.push(timeObject);
		responseTimeChart.validateData();
    }
    
    // create cpu chart
    function createCPUChart(){
    	// SERIAL CHART    
        cpuChart = new AmCharts.AmSerialChart();
        cpuChart.dataProvider = cpuData;
        cpuChart.categoryField = "index";
        cpuChart.addTitle("CPU(%)", 10);

        // category
        var categoryAxis = cpuChart.categoryAxis;
        categoryAxis.gridAlpha = 0.1;
        
        categoryAxis.startOnAxis = true;
        // GRAPH
        var graph = new AmCharts.AmGraph();
        graph.type = "line";
        graph.valueField = "java";
        graph.balloonText = "[[value]]%";
        graph.lineColor = cpuColor;
        graph.fillAlphas = 0.3;
        cpuChart.addGraph(graph);
    	 // Value
        var valueAxis = new AmCharts.ValueAxis();
        valueAxis.maximum = 100;
        cpuChart.addValueAxis(valueAxis);

        // CURSOR
        var chartCursor = new AmCharts.ChartCursor();
        chartCursor.zoomable = false; // as the chart displayes not too many values, we disabled zooming
    	chartCursor.cursorAlpha = 0.3;
    	chartCursor.cursorPosition = "mouse";
        cpuChart.addChartCursor(chartCursor);

        // WRITE
        cpuChart.write("chartJCPUDiv");
        for (var i=0; i < SYSTEM_DATA_SIZE; i++) {
      		var cpuObject = {index: "", java: 0};
	  		cpuData.push(cpuObject);
    	};
    	cpuChart.validateData();
    } 
      
    // create memory chart
    function createMemChart(){
    	// SERIAL CHART
        memChart = new AmCharts.AmSerialChart();
        memChart.dataProvider = memData;
        memChart.categoryField = "index";
        memChart.addTitle("메모리(MB)", 10);
        // Category
        var categoryAxis = memChart.categoryAxis;
        
        
        categoryAxis.startOnAxis = true;

        // Value
        var valueAxis = new AmCharts.ValueAxis();
        valueAxis.gridAlpha = 0.07;
        memChart.addValueAxis(valueAxis);

        // use graph
        var graph = new AmCharts.AmGraph();
        graph.type = "line"; // it's simple line graph
        graph.valueField = "used";
        graph.balloonText = "사용 [[value]]MB ([[percents]]%)";
        graph.lineAlpha = 0;
        graph.fillAlphas = 0.6; // setting fillAlphas to > 0 value makes it area graph 
        memChart.addGraph(graph);

        // total graph
        var graph = new AmCharts.AmGraph();
        graph.type = "line";
        graph.valueField = "total";
        graph.balloonText = "총 [[value]]MB ([[percents]]%)";
        graph.lineAlpha = 0;
        graph.fillAlphas = 0.6;
        memChart.addGraph(graph);

        // CURSOR
        var chartCursor = new AmCharts.ChartCursor();
        chartCursor.zoomable = false; // as the chart displayes not too many values, we disabled zooming
        chartCursor.cursorAlpha = 0.3;
        memChart.addChartCursor(chartCursor);

        // WRITE
        memChart.write("chartMemDiv");
        
        for (var i=0; i < SYSTEM_DATA_SIZE; i++) {
          var memObject = {index: "", used: 0, total: 0};
		  memData.push(memObject);
        };
        
        memChart.validateData();
    }  
    
    // create load avg chart
    function createLoadChart(){
    	// SERIAL CHART    
        avgLoadChart = new AmCharts.AmSerialChart();
        avgLoadChart.dataProvider = avgLoadData;
        avgLoadChart.categoryField = "index";
        if(load_avg_support){
        	avgLoadChart.addTitle("서버부하", 10);
        }else{
        	avgLoadChart.addTitle("[미지원]서버부하", 10);
        }

        // category
        var categoryAxis = avgLoadChart.categoryAxis;
        categoryAxis.startOnAxis = true;

        // GRAPH
        var graph = new AmCharts.AmGraph();
        graph.valueField = "load";
        graph.balloonText = "[[value]]";
        graph.lineColor = loadColor;
        graph.fillAlphas = 0.3;
        avgLoadChart.addGraph(graph);
        
    	 // Value

        if(load_avg_support){
//        	var valueAxis = new AmCharts.ValueAxis();
//        	valueAxis.minimum = 0;
//        	valueAxis.maximum = 5;
//        	avgLoadChart.addValueAxis(valueAxis);
        	// CURSOR
			var chartCursor = new AmCharts.ChartCursor();
			chartCursor.zoomable = false; // as the chart displayes not too many values, we disabled zooming
			chartCursor.cursorAlpha = 0.3;
			avgLoadChart.addChartCursor(chartCursor);
        }

        // WRITE
        avgLoadChart.write("chartLoadDiv");
    	for (var i=0; i < SYSTEM_DATA_SIZE; i++) {
  			var loadObject = {index: "", load: 0};
  			avgLoadData.push(loadObject);
		}
	
		avgLoadChart.validateData();
    } 
    
    // create hit chart
    function createHitChart(){
    	// SERIAL CHART    
        hitChart = new AmCharts.AmSerialChart();
        hitChart.dataProvider = hitData;

        hitChart.categoryField = "index";
        hitChart.addTitle("검색처리수(개)", 10);

        // category
        var categoryAxis = hitChart.categoryAxis;
        categoryAxis.startOnAxis = true;

		 // GRAPH
        var graph = new AmCharts.AmGraph();
        graph.type = "step";
        graph.valueField = "hit";
        graph.balloonText = "[[value]]개";
        graph.lineColor = hitColor;
        graph.fillAlphas = 0.3;
        hitChart.addGraph(graph);
        
    	 // Value
        var valueAxis = new AmCharts.ValueAxis();
        valueAxis.integersOnly = true;
        hitChart.addValueAxis(valueAxis);

        // CURSOR
        var chartCursor = new AmCharts.ChartCursor();
        chartCursor.zoomable = false; // as the chart displayes not too many values, we disabled zooming
    	chartCursor.cursorAlpha = 0.3;
        hitChart.addChartCursor(chartCursor);

        // WRITE
        hitChart.write("chartSearchActDiv");
        for (var i=0; i < SYSTEM_DATA_SIZE; i++) {
      		var hitObject = {index: "", hit: 0, fail: 0};
	  		hitData.push(hitObject);
    	};
    	
    	hitChart.validateData();
    }  
    
    // 평균 응답시간
    function createResponseTimeChart(){
    	// SERIAL CHART    
        responseTimeChart = new AmCharts.AmSerialChart();
        responseTimeChart.dataProvider = responseTimeData;
        responseTimeChart.categoryField = "index";
        responseTimeChart.addTitle("검색응답시간(ms)", 10);

        // category
        var categoryAxis = responseTimeChart.categoryAxis;
        categoryAxis.startOnAxis = true;
        
        // 최대응답시간 GRAPH
        var graph = new AmCharts.AmGraph();
        graph.type = "step";
        graph.valueField = "max";
        graph.balloonText = "최대 [[value]]ms";
        graph.lineColor = timeColor;
        graph.lineAlpha = 0;
        graph.fillAlphas = 0.4;
        responseTimeChart.addGraph(graph);
        
        // 평균응답시간 GRAPH
        var graph = new AmCharts.AmGraph();
        graph.type = "step";
        graph.valueField = "mean";
        graph.balloonText = "평균 [[value]]ms";
        graph.lineColor = timeColor;
        graph.lineAlpha = 0;
        graph.fillAlphas = 0.5;
        responseTimeChart.addGraph(graph);
        
    	// Value
        var valueAxis = new AmCharts.ValueAxis();
        valueAxis.integersOnly = true;
        responseTimeChart.addValueAxis(valueAxis);

        // CURSOR
        var chartCursor = new AmCharts.ChartCursor();
        chartCursor.zoomable = false; // as the chart displayes not too many values, we disabled zooming
    	chartCursor.cursorAlpha = 0.3; // set it to fals if you want the cursor to work in "select" mode
        responseTimeChart.addChartCursor(chartCursor);

        // WRITE
        responseTimeChart.write("chartSearchTimeDiv");
        for (var i=0; i < SYSTEM_DATA_SIZE; i++) {
      		var timeObject = {index: "", mean: 0};
	  		responseTimeData.push(timeObject);
    	};
    	
    	responseTimeChart.validateData();
    }  
    
    // 실패쿼리
    function createFailHitChart(){
    	// SERIAL CHART    
        failHitChart = new AmCharts.AmSerialChart();
        failHitChart.dataProvider = hitData;
        failHitChart.categoryField = "index";
        failHitChart.addTitle("검색실패수(개)", 10);

        // category
        var categoryAxis = failHitChart.categoryAxis;
        
        
        categoryAxis.startOnAxis = true;
 
        // GRAPH
        var graph = new AmCharts.AmGraph();
        graph.type = "step";
        graph.valueField = "fail";
        graph.balloonText = "[[value]]개";
        graph.lineColor = failHitColor;
        graph.fillAlphas = 0.4;
        failHitChart.addGraph(graph);
        
    	 // Value
        var valueAxis = new AmCharts.ValueAxis();
        valueAxis.integersOnly = true;
        failHitChart.addValueAxis(valueAxis);

        // CURSOR
        var chartCursor = new AmCharts.ChartCursor();
        chartCursor.zoomable = false; // as the chart displayes not too many values, we disabled zooming
    	chartCursor.cursorAlpha = 0.3;
        failHitChart.addChartCursor(chartCursor);

        // WRITE
        failHitChart.write("chartSearchFailDiv");
    	//data입력생략함.
        //createHitChart에서 해주었음.
    	failHitChart.validateData();
    } 
    
    //색인정보를 업데이트 해준다.
    function updateIndexingInfo(data){
    	fullIndexChart.dataProvider = data;
    	fullIndexChart.validateData();
    	
    	incIndexChart.dataProvider = data;
    	incIndexChart.validateData();
    	
    	indexDocChart.dataProvider = data;
    	indexDocChart.validateData();
    }
    
    function createFullIndexChart(){
    	// SERIAL CHART    
        fullIndexChart = new AmCharts.AmSerialChart();
        fullIndexChart.categoryField = "name";
        fullIndexChart.addTitle("전체색인수", 10);

        // category
        var categoryAxis = fullIndexChart.categoryAxis;
        
        
        categoryAxis.gridPosition = "start";
        categoryAxis.labelRotation = 45;
        categoryAxis.autoGridCount = false;
    	 // Value
        var valueAxis = new AmCharts.ValueAxis();
        valueAxis.stackType = "regular";
        valueAxis.gridAlpha = 0.1;
        valueAxis.axisAlpha = 0;
        valueAxis.labelsEnabled = false;
        fullIndexChart.addValueAxis(valueAxis);
        
        // GRAPH
        //1.
        var graph = new AmCharts.AmGraph();
        graph.title = "삭제";
        graph.valueField = "fdelete";
        graph.balloonText = "삭제 [[value]]개";
        graph.labelText = "[[value]]";
        graph.type = "column";
        graph.lineAlpha = 0;
        graph.fillAlphas = 1;
        graph.lineColor = deleteColor;
        fullIndexChart.addGraph(graph);
        
        //2.
        graph = new AmCharts.AmGraph();
        graph.title = "업데이트";
        graph.valueField = "fupdate";
        graph.balloonText = "업데이트 [[value]]개";
        graph.labelText = "[[value]]";
        graph.type = "column";
        graph.lineAlpha = 0;
        graph.fillAlphas = 1;
        graph.lineColor = updateColor;
        fullIndexChart.addGraph(graph);
        
        //3.
        graph = new AmCharts.AmGraph();
        graph.title = "새로추가";
        graph.valueField = "finsert";
        graph.balloonText = "새문서 [[value]]개";
        graph.labelText = "[[value]]";
        graph.type = "column";
        graph.lineAlpha = 0;
        graph.fillAlphas = 1;
        graph.lineColor = insertColor;
        fullIndexChart.addGraph(graph);
        
        // CURSOR
        var chartCursor = new AmCharts.ChartCursor();
        chartCursor.zoomable = false; // as the chart displayes not too many values, we disabled zooming
    	chartCursor.cursorAlpha = 0.3;
        fullIndexChart.addChartCursor(chartCursor);

        // WRITE
        fullIndexChart.write("chartFullIndexDiv");
    }  
    
    function createIncIndexChart(){
    	// SERIAL CHART    
        incIndexChart = new AmCharts.AmSerialChart();
        incIndexChart.categoryField = "name";
        incIndexChart.addTitle("증분색인수", 10);

        // category
        var categoryAxis = incIndexChart.categoryAxis;
        
        
        categoryAxis.gridPosition = "start";
        categoryAxis.labelRotation = 45;
        categoryAxis.autoGridCount = false;
        

    	 // Value
        var valueAxis = new AmCharts.ValueAxis();
        valueAxis.stackType = "regular";
        valueAxis.gridAlpha = 0.1;
        valueAxis.axisAlpha = 0;
        valueAxis.labelsEnabled = false;
        incIndexChart.addValueAxis(valueAxis);

        // GRAPH
        //1.
        var graph = new AmCharts.AmGraph();
        graph.title = "삭제";
        graph.valueField = "idelete";
        graph.balloonText = "삭제 [[value]]개";
        graph.labelText = "[[value]]";
        graph.type = "column";
        graph.lineAlpha = 0;
        graph.fillAlphas = 1;
        graph.lineColor = deleteColor;
        incIndexChart.addGraph(graph);
        
        //2.
        graph = new AmCharts.AmGraph();
        graph.title = "업데이트";
        graph.valueField = "iupdate";
        graph.balloonText = "업데이트 [[value]]개";
        graph.labelText = "[[value]]";
        graph.type = "column";
        graph.lineAlpha = 0;
        graph.fillAlphas = 1;
        graph.lineColor = updateColor;
        incIndexChart.addGraph(graph);
        
        //3.
        graph = new AmCharts.AmGraph();
        graph.title = "새로추가";
        graph.valueField = "iinsert";
        graph.balloonText = "새문서 [[value]]개";
        graph.labelText = "[[value]]";
        graph.type = "column";
        graph.lineAlpha = 0;
        graph.fillAlphas = 1;
        graph.lineColor = insertColor;
        incIndexChart.addGraph(graph);
        
        // CURSOR
        var chartCursor = new AmCharts.ChartCursor();
        chartCursor.zoomable = false; // as the chart displayes not too many values, we disabled zooming
    	chartCursor.cursorAlpha = 0.3; // set it to fals if you want the cursor to work in "select" mode
        incIndexChart.addChartCursor(chartCursor);

        // WRITE
        incIndexChart.write("chartIncIndexDiv");
    }  
    
    function createIndexDocChart(){
      	// SERIAL CHART    
        indexDocChart = new AmCharts.AmSerialChart();
        indexDocChart.categoryField = "name";
        indexDocChart.addTitle("누적문서수", 10);

        // category
        var categoryAxis = indexDocChart.categoryAxis;
        categoryAxis.gridPosition = "start";
        categoryAxis.labelRotation = 45;
        categoryAxis.autoGridCount = false;

    	 // Value
        var valueAxis = new AmCharts.ValueAxis();
        valueAxis.gridAlpha = 0.1;
        valueAxis.axisAlpha = 0;
        valueAxis.labelsEnabled = false;
        indexDocChart.addValueAxis(valueAxis);

        // GRAPH
        //1.
        var graph = new AmCharts.AmGraph();
        graph.title = "누적문서수";
        graph.valueField = "tdoc";
        graph.balloonText = "총 [[value]]개";
        graph.labelText = "[[value]]";
        graph.type = "column";
        graph.lineAlpha = 0;
        graph.fillAlphas = 1;
        graph.lineColor = docColor;
        indexDocChart.addGraph(graph);
        
        // CURSOR
        var chartCursor = new AmCharts.ChartCursor();
        chartCursor.zoomable = false; // as the chart displayes not too many values, we disabled zooming
    	chartCursor.cursorAlpha = 0.3; // set it to fals if you want the cursor to work in "select" mode
        indexDocChart.addChartCursor(chartCursor);
        // WRITE
        indexDocChart.write("chartIndexDocDiv");
    }  

	function selectSize(){
		var selectSize = $("#sizeSelector").find("option:selected").val();
		switch (selectSize){
		   case "1240":
		     $("#container_monitor").css("width",1240);
		     break;
		   case "1024":
		     $("#container_monitor").css("width",1024);
		     break;
		   case "100":
		     $("#container_monitor").css("width",document.body.clientWidth);
		     break;
		   case "90":
		     $("#container_monitor").css("width",(document.body.clientWidth * 0.9));
		     break;
		   default:
		}
		
	}
