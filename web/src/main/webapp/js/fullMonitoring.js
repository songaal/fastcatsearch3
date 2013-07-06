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
    var avgTimeChart;
    var avgTimeData = [];
    //max response time
    var maxTimeChart;
    var maxTimeData = [];
    
    //전체색인정보 차트
    var fullIndexChart;
    var incIndexChart;
    var indexDocChart;
    
    var jvm_cpu_support;
	var system_cpu_support;
	var load_avg_support;
	var jvm_memory_support;
    
	var lastIndexInfoUpdateTime = 0;
	var system_url = "/monitoring/system";
    var search_url = "/monitoring/search";
	var indexing_info_url = "/monitoring/indexing";
	//각 컬렉션의 정보를 저장하는 배열.
	var cns = [];
	var hits = [];
	var tms = [];
	var txs = [];
	
	var totalHitName = "totalHit";
	
	var collectionNames = '{index:""';
    
	var IS_TEST = "true"; //테스트데이터를 자동생성한다. 디버그용. true이면 테스트데이터 생성.
		
    // this method called after all page contents are loaded
    window.onload = function() {
        checkAvailable();
        pollingData();
        $(document).ready(function(){
			setInterval(pollingData, 1000);
		});                                    
    }
    
    function checkAvailable(){
    	 $.ajax({
		    url: system_url,
		    data: {q: "", test: IS_TEST},
		    success: function (data)
		    {
				jvm_cpu_support = data.jvm_cpu_support;
				system_cpu_support =  data.system_cpu_support;
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
		 
	  	$.ajax({
		    url: search_url,
		    data: {q: "", test: IS_TEST},
		    async: false,
		    success: function (data)
		    {
		    	$.each(
					data,
					function(i, entity) {
						var cn = entity.name;
						cns[i] = cn;
						collectionNames = collectionNames + ","  + cn + ':' + 0;
					}
				 );
		     }
		 });
  	 	
  	 	createHitChart();
  	 	createTxChart();
  	 	createTmChart();
    }
    
    // method which loads external data
    function pollingData() {
    	pollingSystemData();
    	pollingSearchData();
    	pollingIndexingData();
    }
    
    function pollingSystemData(){
    	$.ajax({
		    url: system_url,
		    data: {test: IS_TEST},
		    success: updateSystemData
    	});
    }
    
    function pollingSearchData(){
    	$.ajax({
		    url: search_url,
		    data: {test: IS_TEST},
		    success: updateSearchData
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
		    		    success: updateIndexingInfo
		    		});
		    	}
		    }
    	});
   }
    
    function updateSystemData(data){
		//mem chart
		var mhm = data.mhm;
		var mhu = data.mhu;
		var mnm = data.mnm;
		var mnu = data.mnu;
		var useMem = mhu + mnu;
		var totalMem = mhm + mnm;
		
		//memory       	
       	memData.shift();
       	maxMemory = totalMem;
      	var memObject = {date:"", value01:useMem, value02:(totalMem-useMem)};
      	memData.push(memObject);
    	memChart.validateData();
		//cpu
		var cj = data.cj;
		var cs = data.cs;	
   		cpuData.shift();
      	var cpuObject = {index:"", java:cj, os:cs};
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
    	var hitJsonStr = '{index:""';
    	var txJsonStr = '{index:""';
    	var tmJsonStr = '{index:""';
    	var totalHitCount = 0;
    	$.each(
					data,
					function(i, entity) {
						var cn = entity.c;
						var hit = entity.h;
						var tm = entity.tm;
						var tx =  entity.tx;
						hits[i] = hit;
						tms[i] = tm;
						txs[i] = tx;
						totalHitCount = totalHitCount + hit;
						hitJsonStr = hitJsonStr + ', '+cn+':'+hit;
						txJsonStr = txJsonStr + ', '+cn+':'+tx;
						tmJsonStr = tmJsonStr + ', '+cn+':'+tm;
					}
				 );
		var hitObject = eval('(' + hitJsonStr+','+totalHitName+':'+totalHitCount+'}' + ')');
		hitData.shift();
		hitData.push(hitObject);
		hitChart.validateData();
		
		var txObject = eval('(' + txJsonStr+'}' + ')');
		avgTimeData.shift();
		avgTimeData.push(txObject);
		avgTimeChart.validateData();
		
		var tmObject = eval('(' + tmJsonStr+'}' + ')');
		maxTimeData.shift();
		maxTimeData.push(tmObject);
		maxTimeChart.validateData();
    }
    
    // create cpu chart
    function createCPUChart(){
    	// SERIAL CHART    
        cpuChart = new AmCharts.AmSerialChart();
        cpuChart.dataProvider = cpuData;
        cpuChart.pathToImages = "js/amcharts/images/";
        cpuChart.zoomOutButton = {
            backgroundColor: '#000000',
            backgroundAlpha: 0.15
        };
        cpuChart.categoryField = "index";
        cpuChart.addTitle("CPU(%)", 10);

        // category
        var categoryAxis = cpuChart.categoryAxis;
        categoryAxis.gridAlpha = 0.07;
        categoryAxis.axisColor = "#DADADA";
        categoryAxis.startOnAxis = true;
        
        // GRAPH
        var graph = new AmCharts.AmGraph();
        graph.title = "red line";
        graph.valueField = "java";
        graph.balloonText = "[[value]]%";
        cpuChart.addGraph(graph);
        
        // GRAPH
        if(system_cpu_support){
            var graph = new AmCharts.AmGraph();
            graph.title = "red line";
            graph.valueField = "os";
            graph.balloonText = "[[value]]%";
            cpuChart.addGraph(graph);
        }
         
    	 // Value
        var valueAxis = new AmCharts.ValueAxis();
        valueAxis.maximum = 100;
        cpuChart.addValueAxis(valueAxis);

        // CURSOR
        var chartCursor = new AmCharts.ChartCursor();
        chartCursor.zoomable = false; // as the chart displayes not too many values, we disabled zooming
    	chartCursor.cursorAlpha = 0; // set it to fals if you want the cursor to work in "select" mode
        cpuChart.addChartCursor(chartCursor);

        // WRITE
        cpuChart.write("chartJCPUDiv");
        for (var i=0; i < SYSTEM_DATA_SIZE; i++) {
      		var cpuObject = {index:"", java:0, os:0};
	  		cpuData.push(cpuObject);
    	};	
    } 
      
    // create memory chart
    function createMemChart(){
    	// SERIAL CHART
        memChart = new AmCharts.AmSerialChart();
        memChart.dataProvider = memData;
        memChart.pathToImages = "js/amcharts/images/";
        memChart.zoomOutButton = {
            backgroundColor: "#000000",
            backgroundAlpha: 0.15
        };
        memChart.categoryField = "date";
        memChart.addTitle("메모리(MB)", 10);
        // Category
        var categoryAxis = memChart.categoryAxis;
        categoryAxis.gridAlpha = 0.07;
        categoryAxis.axisColor = "#DADADA";
        categoryAxis.startOnAxis = true;

        // Value
        var valueAxis = new AmCharts.ValueAxis();
        valueAxis.gridAlpha = 0.07;
        memChart.addValueAxis(valueAxis);

        // use graph
        var graph = new AmCharts.AmGraph();
        graph.type = "line"; // it's simple line graph
        graph.valueField = "value01";
        graph.balloonText = "[[value]]MB ([[percents]]%)";
        graph.lineAlpha = 0;
        graph.fillAlphas = 0.6; // setting fillAlphas to > 0 value makes it area graph 
        memChart.addGraph(graph);

        // total graph
        var graph = new AmCharts.AmGraph();
        graph.type = "line";
        graph.valueField = "value02";
        graph.balloonText = "[[value]]MB ([[percents]]%)";
        graph.lineAlpha = 0;
        graph.fillAlphas = 0.6;
        memChart.addGraph(graph);

        // LEGEND
         var legend = new AmCharts.AmLegend();
         legend.align = "center";

        // CURSOR
        var chartCursor = new AmCharts.ChartCursor();
        chartCursor.zoomable = false; // as the chart displayes not too many values, we disabled zooming
        chartCursor.cursorAlpha = 0;
        memChart.addChartCursor(chartCursor);

        // WRITE
        memChart.write("chartMemDiv");
        
        for (var i=0; i < SYSTEM_DATA_SIZE; i++) {
          var memObject = {date:"", value01:0, value02:0};
		  memData.push(memObject);
        };
    }  
    
    // create load avg chart
    function createLoadChart(){
    	// SERIAL CHART    
        avgLoadChart = new AmCharts.AmSerialChart();
        avgLoadChart.dataProvider = avgLoadData;
        avgLoadChart.pathToImages = "js/amcharts/images/";
//        if(!load_avg_support)
//        	avgLoadChart.backgroundImage = "js/amcharts/images/disable.jpg";
//        avgLoadChart.zoomOutButton = {
//            backgroundColor: '#000000',
//            backgroundAlpha: 0.15
//        };
        avgLoadChart.categoryField = "index";
        if(load_avg_support){
        	avgLoadChart.addTitle("서버부하", 10);
        }else{
        	avgLoadChart.addTitle("[미지원]서버부하", 10);
        }

        // category
        var categoryAxis = avgLoadChart.categoryAxis;
        categoryAxis.gridAlpha = 0.07;
        categoryAxis.axisColor = "#DADADA";
        categoryAxis.startOnAxis = true;

        // GRAPH
        var graph = new AmCharts.AmGraph();
        graph.title = "red line";
        graph.valueField = "load";
        graph.balloonText = "[[value]]";
        avgLoadChart.addGraph(graph);
    	 // Value
        var valueAxis = new AmCharts.ValueAxis();
        valueAxis.minimum = 0;
        valueAxis.maximum = 5;
        avgLoadChart.addValueAxis(valueAxis);

        // CURSOR
//        var chartCursor = new AmCharts.ChartCursor();
//        chartCursor.zoomable = false; // as the chart displayes not too many values, we disabled zooming
//    	chartCursor.cursorAlpha = 0; // set it to fals if you want the cursor to work in "select" mode
//        avgLoadChart.addChartCursor(chartCursor);

        // WRITE
        avgLoadChart.write("chartLoadDiv");
    	for (var i=0; i < SYSTEM_DATA_SIZE; i++) {
  			var loadObject = {index:"", load:0};
  			avgLoadData.push(loadObject);
		}
    	
    	if(load_avg_support){
    		avgLoadChart.validateData();
    	}
    } 
    
    // create hit chart
    function createHitChart(){
    	// SERIAL CHART    
        hitChart = new AmCharts.AmSerialChart();
        hitChart.dataProvider = hitData;
        hitChart.pathToImages = "js/amcharts/images/";
        hitChart.zoomOutButton = {
            backgroundColor: '#000000',
            backgroundAlpha: 0.15
        };
        hitChart.categoryField = "index";
        hitChart.addTitle("초당 퀴리수(갯수)", 10);

        // category
        var categoryAxis = hitChart.categoryAxis;
        categoryAxis.gridAlpha = 0.07;
        categoryAxis.axisColor = "#DADADA";
        categoryAxis.startOnAxis = true;
        $.each(
					cns,
					function(i, entity) {
						 // GRAPH
				        var graph = new AmCharts.AmGraph();
				        graph.title = "red line";
				        graph.valueField = entity;
				        graph.balloonText = "[[value]]("+entity+")";
				        hitChart.addGraph(graph);
					}
				 );
		 // GRAPH
        var graph = new AmCharts.AmGraph();
        graph.title = "red line";
        graph.valueField = totalHitName;
        graph.balloonText = "[[value]](total)";
        hitChart.addGraph(graph);
    	 // Value
        var valueAxis = new AmCharts.ValueAxis();
        hitChart.addValueAxis(valueAxis);

        // CURSOR
        var chartCursor = new AmCharts.ChartCursor();
        chartCursor.zoomable = false; // as the chart displayes not too many values, we disabled zooming
    	chartCursor.cursorAlpha = 0; // set it to fals if you want the cursor to work in "select" mode
        hitChart.addChartCursor(chartCursor);

        // WRITE
        hitChart.write("chartSearchActDiv");
        for (var i=0; i < SYSTEM_DATA_SIZE; i++) {
      		var hitObject = eval('(' + collectionNames + ','+totalHitName+':0}' + ')');
	  		hitData.push(hitObject);
    	};	
    }  
    
    // create tx chart
    function createTxChart(){
    	// SERIAL CHART    
        avgTimeChart = new AmCharts.AmSerialChart();
        avgTimeChart.dataProvider = avgTimeData;
        avgTimeChart.pathToImages = "js/amcharts/images/";
        avgTimeChart.zoomOutButton = {
            backgroundColor: '#000000',
            backgroundAlpha: 0.15
        };
        avgTimeChart.categoryField = "index";
        avgTimeChart.addTitle("최대응답시간(ms)", 10);

        // category
        var categoryAxis = avgTimeChart.categoryAxis;
        categoryAxis.gridAlpha = 0.07;
        categoryAxis.axisColor = "#DADADA";
        categoryAxis.startOnAxis = true;
        $.each(
					cns,
					function(i, entity) {
						 // GRAPH
				        var graph = new AmCharts.AmGraph();
				        graph.title = "red line";
				        graph.valueField = entity;
				        graph.balloonText = "[[value]]("+entity+")";
				        avgTimeChart.addGraph(graph);
					}
				 );
    	 // Value
        var valueAxis = new AmCharts.ValueAxis();
        avgTimeChart.addValueAxis(valueAxis);

        // CURSOR
        var chartCursor = new AmCharts.ChartCursor();
        chartCursor.zoomable = false; // as the chart displayes not too many values, we disabled zooming
    	chartCursor.cursorAlpha = 0; // set it to fals if you want the cursor to work in "select" mode
        avgTimeChart.addChartCursor(chartCursor);

        // WRITE
        avgTimeChart.write("chartSearchMaxTimeDiv");
        for (var i=0; i < SYSTEM_DATA_SIZE; i++) {
      		var txObject = eval('(' + collectionNames + '}' + ')');
	  		avgTimeData.push(txObject);
    	};	
    }  
    
    // create tm chart
    function createTmChart(){
    	// SERIAL CHART    
        maxTimeChart = new AmCharts.AmSerialChart();
        maxTimeChart.dataProvider = maxTimeData;
        maxTimeChart.pathToImages = "js/amcharts/images/";
        maxTimeChart.zoomOutButton = {
            backgroundColor: '#000000',
            backgroundAlpha: 0.15
        };
        maxTimeChart.categoryField = "index";
        maxTimeChart.addTitle("평균응답시간(ms)", 10);

        // category
        var categoryAxis = maxTimeChart.categoryAxis;
        categoryAxis.gridAlpha = 0.07;
        categoryAxis.axisColor = "#DADADA";
        categoryAxis.startOnAxis = true;
        $.each(
					cns,
					function(i, entity) {
						 // GRAPH
				        var graph = new AmCharts.AmGraph();
				        graph.title = "red line";
				        graph.valueField = entity;
				        graph.balloonText = "[[value]]("+entity+")";
				        maxTimeChart.addGraph(graph);
					}
				 );
    	 // Value
        var valueAxis = new AmCharts.ValueAxis();
        maxTimeChart.addValueAxis(valueAxis);

        // CURSOR
        var chartCursor = new AmCharts.ChartCursor();
        chartCursor.zoomable = false; // as the chart displayes not too many values, we disabled zooming
    	chartCursor.cursorAlpha = 0; // set it to fals if you want the cursor to work in "select" mode
        maxTimeChart.addChartCursor(chartCursor);

        // WRITE
        maxTimeChart.write("chartSearchTimeDiv");
        for (var i=0; i < SYSTEM_DATA_SIZE; i++) {
      		var tmObject = eval('(' + collectionNames + '}' + ')');
	  		maxTimeData.push(tmObject);
    	};	
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
        fullIndexChart.pathToImages = "js/amcharts/images/";
        fullIndexChart.zoomOutButton = {
            backgroundColor: '#000000',
            backgroundAlpha: 0.15
        };
        fullIndexChart.categoryField = "name";
        fullIndexChart.addTitle("전체색인수", 10);

        // category
        var categoryAxis = fullIndexChart.categoryAxis;
        categoryAxis.gridAlpha = 0.07;
        categoryAxis.axisColor = "#DADADA";
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
        graph.balloonText = "[[value]]";
        graph.labelText = "[[value]]";
        graph.type = "column";
        graph.lineAlpha = 0;
        graph.fillAlphas = 1;
        graph.lineColor = "#C72C95";
        fullIndexChart.addGraph(graph);
        
        //2.
        graph = new AmCharts.AmGraph();
        graph.title = "업데이트";
        graph.valueField = "fupdate";
        graph.balloonText = "[[value]]";
        graph.labelText = "[[value]]";
        graph.type = "column";
        graph.lineAlpha = 0;
        graph.fillAlphas = 1;
        graph.lineColor = "#D8E0BD";
        fullIndexChart.addGraph(graph);
        
        //3.
        graph = new AmCharts.AmGraph();
        graph.title = "새로추가";
        graph.valueField = "finsert";
        graph.balloonText = "[[value]]";
        graph.labelText = "[[value]]";
        graph.type = "column";
        graph.lineAlpha = 0;
        graph.fillAlphas = 1;
        graph.lineColor = "#B3DBD4";
        fullIndexChart.addGraph(graph);
        
        // CURSOR
        var chartCursor = new AmCharts.ChartCursor();
        chartCursor.zoomable = false; // as the chart displayes not too many values, we disabled zooming
    	chartCursor.cursorAlpha = 0; // set it to fals if you want the cursor to work in "select" mode
        fullIndexChart.addChartCursor(chartCursor);

        // WRITE
        fullIndexChart.write("chartFullIndexDiv");
    }  
    
    function createIncIndexChart(){
    	// SERIAL CHART    
        incIndexChart = new AmCharts.AmSerialChart();
        incIndexChart.pathToImages = "js/amcharts/images/";
        incIndexChart.zoomOutButton = {
            backgroundColor: '#000000',
            backgroundAlpha: 0.15
        };
        incIndexChart.categoryField = "name";
        incIndexChart.addTitle("증분색인수", 10);

        // category
        var categoryAxis = incIndexChart.categoryAxis;
        categoryAxis.gridAlpha = 0.07;
        categoryAxis.axisColor = "#DADADA";
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
        graph.balloonText = "[[value]]";
        graph.labelText = "[[value]]";
        graph.type = "column";
        graph.lineAlpha = 0;
        graph.fillAlphas = 1;
        graph.lineColor = "#C72C95";
        incIndexChart.addGraph(graph);
        
        //2.
        graph = new AmCharts.AmGraph();
        graph.title = "업데이트";
        graph.valueField = "iupdate";
        graph.balloonText = "[[value]]";
        graph.labelText = "[[value]]";
        graph.type = "column";
        graph.lineAlpha = 0;
        graph.fillAlphas = 1;
        graph.lineColor = "#D8E0BD";
        incIndexChart.addGraph(graph);
        
        //3.
        graph = new AmCharts.AmGraph();
        graph.title = "새로추가";
        graph.valueField = "iinsert";
        graph.balloonText = "[[value]]";
        graph.labelText = "[[value]]";
        graph.type = "column";
        graph.lineAlpha = 0;
        graph.fillAlphas = 1;
        graph.lineColor = "#B3DBD4";
        incIndexChart.addGraph(graph);
        
        // CURSOR
        var chartCursor = new AmCharts.ChartCursor();
        chartCursor.zoomable = false; // as the chart displayes not too many values, we disabled zooming
    	chartCursor.cursorAlpha = 0; // set it to fals if you want the cursor to work in "select" mode
        incIndexChart.addChartCursor(chartCursor);

        // WRITE
        incIndexChart.write("chartIncIndexDiv");
    }  
    
    function createIndexDocChart(){
      	// SERIAL CHART    
        indexDocChart = new AmCharts.AmSerialChart();
        indexDocChart.pathToImages = "js/amcharts/images/";
        indexDocChart.zoomOutButton = {
            backgroundColor: '#000000',
            backgroundAlpha: 0.15
        };
        indexDocChart.categoryField = "name";
        indexDocChart.addTitle("증분색인수", 10);

        // category
        var categoryAxis = indexDocChart.categoryAxis;
        categoryAxis.gridAlpha = 0.07;
        categoryAxis.axisColor = "#DADADA";
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
        graph.balloonText = "[[value]]";
        graph.labelText = "[[value]]";
        graph.type = "column";
        graph.lineAlpha = 0;
        graph.fillAlphas = 1;
        graph.lineColor = "#B3DBD4";
        indexDocChart.addGraph(graph);
        
        // CURSOR
        var chartCursor = new AmCharts.ChartCursor();
        chartCursor.zoomable = false; // as the chart displayes not too many values, we disabled zooming
    	chartCursor.cursorAlpha = 0; // set it to fals if you want the cursor to work in "select" mode
        indexDocChart.addChartCursor(chartCursor);
        // WRITE
        indexDocChart.write("chartIndexDocDiv");
    }  
    
    
    
