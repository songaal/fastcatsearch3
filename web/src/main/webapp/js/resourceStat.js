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
	var SYSTEM_DATA_SIZE;
	//메모리 차트
    var memChart;
    var memData = [];
    var maxMemory;
    //cpu 차트
    var cpuChart;
    var cpuData = [];
    //load 차트
    var loadChart;
    var loadData = [];
    
	var system_url = "/monitoring/system/detail";
    var search_url = "/monitoring/search/detail";
	
	var IS_TEST = "false"; //테스트데이터를 자동생성한다. 디버그용. true이면 테스트데이터 생성.
	
	var tabTag = "cpu";
	var nowHour = 0;
	
	var cpu2 = [];
	var mem2 = [];
	var load2 = [];
	var time2 = [];
	var cpu3 = [];
	var mem3 = [];
	var load3 = [];
	var time3 = [];
	
	var times = [];
	
	var color_0 = "#b0de09";
	var color_1 = "#fcd202";
	var color_2 = "#ff6600";
	
	var csvCPUData;
	var csvMEMData;
	var csvLOADData;
	
	var csvFileName = "";
	
	var cpuHTML;
	var memHTML;
	var loadHTML;
	
    window.onload = function() {
        $(document).ready(function(){
        	if ("hour" == typeTag) {
        		SYSTEM_DATA_SIZE = 60;
        		$("#a_hour").attr("class","btn_s_on");
        	}else if ("day" == typeTag){
        		SYSTEM_DATA_SIZE = 24;
        		$("#a_day").attr("class","btn_s_on");
        	}else if ("week" == typeTag){
        		SYSTEM_DATA_SIZE = 7;
        		$("#a_week").attr("class","btn_s_on");
        	}else if ("month" == typeTag){
        		SYSTEM_DATA_SIZE = 31;
        		$("#a_month").attr("class","btn_s_on");
        	}else if ("year" == typeTag){
        		SYSTEM_DATA_SIZE = 12;
        		$("#a_year").attr("class","btn_s_on");
        	}
        	showNow();
  	 		createCPUChart();
	  		createMemChart();    
  	 		createLoadChart();
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
		var dateVar = makeTwoDigits($("#selectDate").val());
		var hourVar = makeTwoDigits($("#selectHour").val());
		
		var start = year+'-'+makeTwoDigits(mon)+"-"+makeTwoDigits(day)+' 00:00:00';
		$("#selectWeek").html(week(start)[0].substring(0,10)+"~"+week(start)[1].substring(0,10));
    }
	function makeTwoDigits(i){
		if(i && i < 10) return "0"+i;
		else return i;
	}

// ------------------------------------------------
	function showHourSystemData_0(start_0, end_0){
    	$.ajax({
		    url: system_url,
		    data: {test: IS_TEST, simple:"", start_0:start_0, end_0:end_0,start_1:"", end_1:"",start_2:"", end_2:"", type:"hour"},
		    success: updateSystemData_0
    	});
    }
	function showHourSystemData_1(start_0, end_0, start_1, end_1){
    	$.ajax({
		    url: system_url,
		    data: {test: IS_TEST, simple:"", start_0:start_0, end_0:end_0,start_1:start_1, end_1:end_1,start_2:"", end_2:"", type:"hour"},
		    success: updateSystemData_1
    	});
    }
    function showHourSystemData_2(start_0, end_0, start_1, end_1, start_2, end_2){
    	$.ajax({
		    url: system_url,
		    data: {test: IS_TEST, simple:"", start_0:start_0, end_0:end_0,start_1:start_1, end_1:end_1,start_2:start_2, end_2:end_2, type:"hour"},
		    success: updateSystemData_2
    	});
    }
    //----------
    function showDaySystemData_0(start_0, end_0){
    	$.ajax({
		    url: system_url,
		    data: {test: IS_TEST, simple:"", start_0:start_0, end_0:end_0,start_1:"", end_1:"",start_2:"", end_2:"", type:"day"},
		    success: updateSystemData_0
    	});
    }
	function showDaySystemData_1(start_0, end_0, start_1, end_1){
    	$.ajax({
		    url: system_url,
		    data: {test: IS_TEST, simple:"", start_0:start_0, end_0:end_0,start_1:start_1, end_1:end_1,start_2:"", end_2:"", type:"day"},
		    success: updateSystemData_1
    	});
    }
    function showDaySystemData_2(start_0, end_0, start_1, end_1, start_2, end_2){
    	$.ajax({
		    url: system_url,
		    data: {test: IS_TEST, simple:"", start_0:start_0, end_0:end_0,start_1:start_1, end_1:end_1,start_2:start_2, end_2:end_2, type:"day"},
		    success: updateSystemData_2
    	});
    }
    //-------
    function showWeekSystemData_0(start_0, end_0){
    	$.ajax({
		    url: system_url,
		    data: {test: IS_TEST, simple:"", start_0:start_0, end_0:end_0,start_1:"", end_1:"",start_2:"", end_2:"", type:"week"},
		    success: updateSystemData_0
    	});
    }
	function showWeekSystemData_1(start_0, end_0, start_1, end_1){
    	$.ajax({
		    url: system_url,
		    data: {test: IS_TEST, simple:"", start_0:start_0, end_0:end_0,start_1:start_1, end_1:end_1,start_2:"", end_2:"", type:"week"},
		    success: updateSystemData_1
    	});
    }
    function showWeekSystemData_2(start_0, end_0, start_1, end_1, start_2, end_2){
    	$.ajax({
		    url: system_url,
		    data: {test: IS_TEST, simple:"", start_0:start_0, end_0:end_0,start_1:start_1, end_1:end_1,start_2:start_2, end_2:end_2, type:"week"},
		    success: updateSystemData_2
    	});
    }
    //--------
    function showMonthSystemData_0(start_0, end_0){
    	$.ajax({
		    url: system_url,
		    data: {test: IS_TEST, simple:"", start_0:start_0, end_0:end_0,start_1:"", end_1:"",start_2:"", end_2:"", type:"month"},
		    success: updateSystemData_0
    	});
    }
	function showMonthSystemData_1(start_0, end_0, start_1, end_1){
    	$.ajax({
		    url: system_url,
		    data: {test: IS_TEST, simple:"", start_0:start_0, end_0:end_0,start_1:start_1, end_1:end_1,start_2:"", end_2:"", type:"month"},
		    success: updateSystemData_1
    	});
    }
    function showMonthSystemData_2(start_0, end_0, start_1, end_1, start_2, end_2){
    	$.ajax({
		    url: system_url,
		    data: {test: IS_TEST, simple:"", start_0:start_0, end_0:end_0,start_1:start_1, end_1:end_1,start_2:start_2, end_2:end_2, type:"month"},
		    success: updateSystemData_2
    	});
    }
    //--------
    function showYearSystemData_0(start_0, end_0){
    	$.ajax({
		    url: system_url,
		    data: {test: IS_TEST, simple:"", start_0:start_0, end_0:end_0,start_1:"", end_1:"",start_2:"", end_2:"", type:"year"},
		    success: updateSystemData_0
    	});
    }
	function showYearSystemData_1(start_0, end_0, start_1, end_1){
    	$.ajax({
		    url: system_url,
		    data: {test: IS_TEST, simple:"", start_0:start_0, end_0:end_0,start_1:start_1, end_1:end_1,start_2:"", end_2:"", type:"year"},
		    success: updateSystemData_1
    	});
    }
    function showYearSystemData_2(start_0, end_0, start_1, end_1, start_2, end_2){
    	$.ajax({
		    url: system_url,
		    data: {test: IS_TEST, simple:"", start_0:start_0, end_0:end_0,start_1:start_1, end_1:end_1,start_2:start_2, end_2:end_2, type:"year"},
		    success: updateSystemData_2
    	});
    }
    //-------------------------------------------------------
    function updateSystemData_0(data){
		if(data.re_0){
			$("#cpuDiv").css("display","block");
		   	$("#memDiv").css("display","block");
		   	$("#loadDiv").css("display","block");
		   	
			cpuData = new Array();
			memData = new Array();
			loadData = new Array();
			
			csvCPUData = "";
			csvMEMData = "";
			csvLOADData = "";
			cpuHTML = "";
			memHTML = "";
			loadHTML = "";
			
	    	$.each(
				eval('('+data.re_0+')'),
				function(i, entity) {
					var id = entity.id;
					var cpu = entity.cpu;
					var mem = entity.mem;
					var load = entity.load.toFixed(1);
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
					//memory
			      	var memObject = {index:time, mem: mem};
		      		memData.push(memObject);
			    	//cpu
			      	var cpuObject = {index:time, cpu: cpu};
			      	cpuData.push(cpuObject);
					 
			      	var loadObject = {index:time, load:load};
			      	loadData.push(loadObject);
			      	
			      	cpuHTML += "<tr><td>"+time+"</td><td>"+cpu+"</td><td>-</td><td>-</td><td>-</td><td>-</td></tr>";
			      	memHTML += "<tr><td>"+time+"</td><td>"+mem+"</td><td>-</td><td>-</td><td>-</td><td>-</td></tr>";
			        loadHTML += "<tr><td>"+time+"</td><td>"+load+"</td><td>-</td><td>-</td><td>-</td><td>-</td></tr>";
			      	csvCPUData += time+","+cpu+"\n";
			      	csvMEMData += time+","+mem+"\n";
			      	csvLOADData += time+","+load+"\n";
				}
	    	);
	    	memChart.dataProvider = memData;
			cpuChart.dataProvider = cpuData;
			loadChart.dataProvider = loadData;
			
	    	memChart.validateData();
	    	cpuChart.validateData();
	    	loadChart.validateData();
	    	
	    	changeTab(tabTag);
		}
    }
    
      function updateSystemData_1(data){
      	var cpus = [];
      	var mems = [];
      	var loads = [];
		if(data.re_0){
	    	$.each(
				eval('('+data.re_0+')'),
				function(i, entity) {
					cpus[i] = entity.cpu;
					mems[i] = entity.mem;
					loads[i] = entity.load.toFixed(1);
				}
	    	);
		}
		if(data.re_1){
			$("#cpuDiv").css("display","block");
		   	$("#memDiv").css("display","block");
		   	$("#loadDiv").css("display","block");
		   	
			csvCPUData = "";
			csvMEMData = "";
			csvLOADData = "";
			cpuHTML = "";
			memHTML = "";
			loadHTML = "";
			
	    	$.each(
				eval('('+data.re_1+')'),
				function(i, entity) {
					var id = entity.id;
					var cpu = entity.cpu;
					var mem = entity.mem;
					var load = entity.load.toFixed(1);
					//memory
			      	var memObject = {index:times[i], mem: mems[i], mem2:mem};
		      		memData[i] = memObject;
			    	//cpu
			      	var cpuObject = {index:times[i], cpu: cpus[i], cpu2:cpu};
			      	cpuData[i] = cpuObject;
					 
			      	var loadObject = {index:times[i], load:loads[i], load2:load};
			      	loadData[i] = loadObject;
			      	
			      	cpuHTML += "<tr><td>"+times[i]+"</td><td>"+cpus[i]+"</td><td>"+cpu+"</td><td>"+computeRate(cpus[i],cpu)+"</td><td>-</td><td>-</td></tr>";
			      	memHTML += "<tr><td>"+times[i]+"</td><td>"+mems[i]+"</td><td>"+mem+"</td><td>"+computeRate(mems[i],mem)+"</td><td>-</td><td>-</td></tr>";
			        loadHTML += "<tr><td>"+times[i]+"</td><td>"+loads[i]+"</td><td>"+load+"</td><td>"+computeRate(loads[i],load)+"</td><td>-</td><td>-</td></tr>";
			      	csvCPUData += times[i]+","+cpus[i]+","+cpu+","+computeRate(cpus[i],cpu)+"\n";
			      	csvMEMData += times[i]+","+mems[i]+","+mem+","+computeRate(mems[i],mem)+"\n";
			      	csvLOADData += times[i]+","+loads[i]+","+load+","+computeRate(loads[i],load)+"\n";
				}
	    	);
	    	memChart.validateData();
	    	cpuChart.validateData();
	    	loadChart.validateData();
	    	
	    	changeTab(tabTag);
		}
    }
    
    function updateSystemData_2(data){
      	var cpus = [];
      	var mems = [];
      	var loads = [];
      	var cpus_2 = [];
      	var mems_2 = [];
      	var loads_2 = [];
      	
		if(data.re_0){
	    	$.each(
				eval('('+data.re_0+')'),
				function(i, entity) {
					cpus[i] = entity.cpu;
					mems[i] = entity.mem;
					loads[i] = entity.load.toFixed(1);
				}
	    	);
		}
		if(data.re_1){
	    	$.each(
				eval('('+data.re_1+')'),
				function(i, entity) {
					cpus_2[i] = entity.cpu;
					mems_2[i] = entity.mem;
					loads_2[i] = entity.load.toFixed(1);
				}
	    	);
		}
		if(data.re_2){
			$("#cpuDiv").css("display","block");
		   	$("#memDiv").css("display","block");
		   	$("#loadDiv").css("display","block");
		   	
			csvCPUData = "";
			csvMEMData = "";
			csvLOADData = "";
			cpuHTML = "";
			memHTML = "";
			loadHTML = "";
			
	    	$.each(
				eval('('+data.re_2+')'),
				function(i, entity) {
					var id = entity.id;
					var cpu = entity.cpu;
					var mem = entity.mem;
					var load = entity.load.toFixed(1);
					//memory
			      	var memObject = {index:times[i], mem: mems[i], mem2:mems_2[i], mem3:mem};
		      		memData[i] = memObject;
			    	//cpu
			      	var cpuObject = {index:times[i], cpu: cpus[i], cpu2:cpus_2[i], cpu3:cpu};
			      	cpuData[i] = cpuObject;
					 
			      	var loadObject = {index:times[i], load:loads[i], load2:loads_2[i], load3:load};
			      	loadData[i] = loadObject;
			      	
			      	cpuHTML += "<tr><td>"+times[i]+"</td><td>"+cpus[i]+"</td><td>"+cpus_2[i]+"</td><td>"+computeRate(cpus[i],cpus_2[i])+"</td><td>"+cpu+"</td><td>"+computeRate(cpus[i],cpu)+"</td></tr>";
			      	memHTML += "<tr><td>"+times[i]+"</td><td>"+mems[i]+"</td><td>"+mems_2[i]+"</td><td>"+computeRate(mems[i],mems_2[i])+"</td><td>"+mem+"</td><td>"+computeRate(mems[i],mem)+"</td></tr>";
			        loadHTML += "<tr><td>"+times[i]+"</td><td>"+loads[i]+"</td><td>"+loads_2[i]+"</td><td>"+computeRate(loads[i],loads_2[i])+"</td><td>"+load+"</td><td>"+computeRate(loads[i],load)+"</td></tr>";
			      	csvCPUData += times[i]+","+cpus[i]+","+cpus_2[i]+","+computeRate(cpus[i],cpus_2[i])+","+cpu+","+computeRate(cpus[i],cpu)+"\n";
			      	csvMEMData += times[i]+","+mems[i]+","+mems_2[i]+","+computeRate(mems[i],mems_2[i])+","+mem+","+computeRate(mems[i],mem)+"\n";
			      	csvLOADData += times[i]+","+loads[i]+","+loads_2[i]+","+computeRate(loads[i],loads_2[i])+","+load+","+computeRate(loads[i],load)+"\n";
				}
	    	);
	    	memChart.validateData();
	    	cpuChart.validateData();
	    	loadChart.validateData();
	    	
	    	changeTab(tabTag);
		}
    }
    
    
// ------------------------------------------------   
    
 
    // create cpu chart
    function createCPUChart(){
    	// SERIAL CHART    
        cpuChart = new AmCharts.AmSerialChart();
        cpuChart.dataProvider = cpuData;
        cpuChart.categoryField = "index";
        cpuChart.addTitle("CPU(%)", 10);

        // category
        var categoryAxis = cpuChart.categoryAxis;
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
        graph.valueField = "cpu";
        graph.title = "0";
        graph.balloonText = "[[value]]%";
        graph.lineThickness  = 2;
        graph.fillAlphas = -1; 
        graph.lineColor = color_0;
        cpuChart.addGraph(graph);
    	 // Value
        var valueAxis = new AmCharts.ValueAxis();
        valueAxis.maximum = 100;
        cpuChart.addValueAxis(valueAxis);

        // if(count == 1){
        	// var graph = new AmCharts.AmGraph();
	        // graph.valueField = "cpu2";
	        // graph.title = "1";
	        // graph.balloonText = "[[value]]%";
	        // graph.lineThickness  = 2;
	        // graph.fillAlphas = -1; 
	        // cpuChart.addGraph(graph);
        // }else if(count == 2){
        	var graph = new AmCharts.AmGraph();
	        graph.valueField = "cpu2";
	        graph.title = "1";
	        graph.balloonText = "[[value]]%";
	        graph.lineThickness  = 2;
	        graph.fillAlphas = -1;
	        graph.lineColor = color_1; 
	        cpuChart.addGraph(graph);
	        var graph = new AmCharts.AmGraph();
	        graph.valueField = "cpu3";
	        graph.title = "2";
	        graph.balloonText = "[[value]]%";
	        graph.lineThickness  = 2;
	        graph.fillAlphas = -1;
	        graph.lineColor = color_2; 
	        cpuChart.addGraph(graph);
        // }

        // CURSOR
        var chartCursor = new AmCharts.ChartCursor();
        chartCursor.cursorAlpha = 0.3;
        cpuChart.addChartCursor(chartCursor);
		cpuChart.pathToImages = "/admin/js/amcharts/images/";
		
        // WRITE
        cpuChart.write("chartJCPUDiv");
        for (var i=0; i < SYSTEM_DATA_SIZE; i++) {
      		var cpuObject = {index: "", java: 0};
	  		cpuData.push(cpuObject);
    	};
    	cpuChart.validateData();
    }
    
    function zoomCpuChart() {
                // different zoom methods can be used - zoomToIndexes, zoomToDates, zoomToCategoryValues
                cpuChart.zoomToIndexes(cpuData.length*nowHour/24, cpuData.length*(nowHour+1)/24);
    }
    function zoomMemChart() {
                // different zoom methods can be used - zoomToIndexes, zoomToDates, zoomToCategoryValues
                memChart.zoomToIndexes(memData.length*nowHour/24, memData.length*(nowHour+1)/24);
    } 
    function zoomLoadChart() {
                // different zoom methods can be used - zoomToIndexes, zoomToDates, zoomToCategoryValues
                avgLoadChart.zoomToIndexes(avgLoadData.length*nowHour/24, avgLoadData.length*(nowHour+1)/24);
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

        // Value
        var valueAxis = new AmCharts.ValueAxis();
        valueAxis.gridAlpha = 0.07;
        memChart.addValueAxis(valueAxis);

        // use graph
        var graph = new AmCharts.AmGraph();
        graph.valueField = "mem";
        graph.title = "0";
        graph.balloonText = "사용 [[value]]MB ([[percents]]%)";
        graph.lineThickness  = 2;
        graph.fillAlphas = -1; // setting fillAlphas to > 0 value makes it area graph 
        graph.lineColor = color_0;
        memChart.addGraph(graph);

        // if(count == 1){
        	// var graph = new AmCharts.AmGraph();
	        // graph.valueField = "used2";
	        // graph.title = "1";
	        // graph.balloonText = "사용 [[value]]MB ([[percents]]%)";
	        // graph.lineThickness  = 2;
	        // graph.fillAlphas = -1; 
	        // memChart.addGraph(graph);
        // }else if(count == 2){
        	var graph = new AmCharts.AmGraph();
	        graph.valueField = "mem2";
	        graph.title = "1";
	        graph.balloonText = "사용 [[value]]MB ([[percents]]%)";
	        graph.lineThickness  = 2;
	        graph.fillAlphas = -1; 
	        graph.lineColor = color_1; 
	        memChart.addGraph(graph);
	        var graph = new AmCharts.AmGraph();
	        graph.valueField = "mem3";
	        graph.title = "2";
	        graph.balloonText = "사용 [[value]]MB ([[percents]]%)";
	        graph.lineThickness  = 2;
	        graph.fillAlphas = -1; 
	        graph.lineColor = color_2;  
	        memChart.addGraph(graph);
        // }

        // CURSOR
        var chartCursor = new AmCharts.ChartCursor();
        chartCursor.cursorAlpha = 0.3;
        memChart.addChartCursor(chartCursor);
		memChart.pathToImages = "/admin/js/amcharts/images/";
        
        
        // WRITE
        memChart.write("chartMemDiv");
        
        for (var i=0; i < SYSTEM_DATA_SIZE; i++) {
          var memObject = {index: "", used: 0};
		  memData.push(memObject);
        }
        
        memChart.validateData();
    }  
    
    // create load avg chart
    function createLoadChart(count){
    	// SERIAL CHART    
        loadChart = new AmCharts.AmSerialChart();
        loadChart.dataProvider = loadData;
        loadChart.categoryField = "index";
        //if(load_avg_support){
        	loadChart.addTitle("서버부하", 10);
        //}else{
        //	avgLoadChart.addTitle("[미지원]서버부하", 10);
       // }

        // category
        var categoryAxis = loadChart.categoryAxis;
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
        graph.valueField = "load";
        graph.title = "0";
        graph.balloonText = "[[value]]";
        graph.lineThickness  = 2;
        graph.fillAlphas = -1; 
        graph.lineColor = color_0;
        loadChart.addGraph(graph);
        
    	 // Value
        var valueAxis = new AmCharts.ValueAxis();
        valueAxis.minimum = 0;
        valueAxis.maximum = 5;
        loadChart.addValueAxis(valueAxis);

        // if(count == 1){
        	// var graph = new AmCharts.AmGraph();
	        // graph.valueField = "load2";
	        // graph.title = "1";
	        // graph.balloonText = "[[value]]";
	        // graph.lineThickness  = 2;
	        // graph.fillAlphas = -1; 
	        // avgLoadChart.addGraph(graph);
        // }else if(count == 2){
        	var graph = new AmCharts.AmGraph();
	        graph.valueField = "load2";
	        graph.title = "1";
	        graph.balloonText = "[[value]]";
	        graph.lineThickness  = 2;
	        graph.fillAlphas = -1; 
	        graph.lineColor = color_1; 
	        loadChart.addGraph(graph);
	        var graph = new AmCharts.AmGraph();
	        graph.valueField = "load3";
	        graph.title = "2";
	        graph.balloonText = "[[value]]";
	        graph.lineThickness  = 2;
	        graph.fillAlphas = -1;
	        graph.lineColor = color_2;  
	        loadChart.addGraph(graph);
        // }

        // CURSOR
        var chartCursor = new AmCharts.ChartCursor();
        chartCursor.cursorAlpha = 0.3;
        loadChart.addChartCursor(chartCursor);
		loadChart.pathToImages = "/admin/js/amcharts/images/";

        // WRITE
        loadChart.write("chartLoadDiv");
    	for (var i=0; i < SYSTEM_DATA_SIZE; i++) {
  			var loadObject = {index: "", load: 0};
  			loadData.push(loadObject);
		}
	
		loadChart.validateData();
    } 
    
	function drawChart2(type){
		var start_0;
		var start_1;
		var start_2;
		var end_0;
		var end_1;
		var end_2;
		var dateVar = makeTwoDigits($("#selectDate").val());
		var hourVar = makeTwoDigits($("#selectHour").val());
		
		if("hour" == type){
			start_0 = dateVar+' '+hourVar+':00:00';
			end_0 = dateVar+' '+hourVar+':59:59';
			
			var dateVar2 = makeTwoDigits($("#selectDate_2").val());
			var hourVar2 = makeTwoDigits($("#selectHour_2").val());
			var dateVar3 = makeTwoDigits($("#selectDate_3").val());
			var hourVar3 = makeTwoDigits($("#selectHour_3").val());
			var flag2;
			var flag3;
			if(dateVar2 != "0000-00" && hourVar2 != "시"){
				flag2 = true;
				start_1 = dateVar2+' '+hourVar2+':00:00';
				end_1 = dateVar2+' '+hourVar2+':59:59';
			}
			if(dateVar3 != "0000-00" && hourVar3 != "시"){
				flag3 = true;
				start_2 = dateVar3+' '+hourVar3+':00:00';
				end_2 = dateVar3+' '+hourVar3+':59:59';
			}
			if(flag2 && flag3){
				showHourSystemData_2(start_0, end_0, start_1, end_1, start_2, end_2);
			}else if(flag2){
				showHourSystemData_1(start_0, end_0, start_1, end_1);
			}else if(flag3){
				showHourSystemData_1(start_0, end_0, start_2, end_2);
			}else{
				showHourSystemData_0(start_0, end_0);
			}
		}else if("day" == type){
			showDaySystemData_2(start_0, end_0, start_1, end_1, start_2, end_2);
		}else if("week" == type){
			showWeekSystemData_2(week(start_0)[0], week(start_0)[1], week(start_1)[0], week(start_1)[1], week(start_2)[0], week(start_2)[1]);
		}else if("month" == type){
			var tp_0 = $("#selectYear_0").val()+"-"+makeTwoDigits($("#selectMonth_0").val())+"-01";
			var tp_1 = $("#selectYear_1").val()+"-"+makeTwoDigits($("#selectMonth_1").val())+"-01";
			var tp_2 = $("#selectYear_2").val()+"-"+makeTwoDigits($("#selectMonth_2").val())+"-01";
			showMonthSystemData_2(month(tp_0)[0], month(tp_0)[1], month(tp_1)[0], month(tp_1)[1], month(tp_2)[0], month(tp_2)[1]);
		}else if("year" == type){
			var tp_0 = $("#selectYear_0").val()+"-01-01";
			var tp_1 = $("#selectYear_1").val()+"-01-01";
			var tp_2 = $("#selectYear_2").val()+"-01-01";
			showYearSystemData_2(year(tp_0)[0], year(tp_0)[1], year(tp_1)[0], year(tp_1)[1], year(tp_2)[0], year(tp_2)[1]);
		}
	}
	
	
	function drawChart(type){
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
				showHourSystemData_2(start_0, end_0, start_1, end_1, start_2, end_2);
			}else if("day" == type){
				showDaySystemData_2(start_0, end_0, start_1, end_1, start_2, end_2);
			}else if("week" == type){
				showWeekSystemData_2(week(start_0)[0], week(start_0)[1], week(start_1)[0], week(start_1)[1], week(start_2)[0], week(start_2)[1]);
			}else if("month" == type){
				var tp_0 = $("#selectYear_0").val()+"-"+makeTwoDigits($("#selectMonth_0").val())+"-01";
				var tp_1 = $("#selectYear_1").val()+"-"+makeTwoDigits($("#selectMonth_1").val())+"-01";
				var tp_2 = $("#selectYear_2").val()+"-"+makeTwoDigits($("#selectMonth_2").val())+"-01";
				showMonthSystemData_2(month(tp_0)[0], month(tp_0)[1], month(tp_1)[0], month(tp_1)[1], month(tp_2)[0], month(tp_2)[1]);
			}else if("year" == type){
				var tp_0 = $("#selectYear_0").val()+"-01-01";
				var tp_1 = $("#selectYear_1").val()+"-01-01";
				var tp_2 = $("#selectYear_2").val()+"-01-01";
				showYearSystemData_2(year(tp_0)[0], year(tp_0)[1], year(tp_1)[0], year(tp_1)[1], year(tp_2)[0], year(tp_2)[1]);
			}
		}else if (start_0 != "" && end_0 != "" && start_1 != "" && end_1 != "") {
			if("hour" == type){
				showHourSystemData_1(start_0, end_0, start_1, end_1);
			}else if("day" == type){
				showDaySystemData_1(start_0, end_0, start_1, end_1);
			}else if("week" == type){
				showWeekSystemData_1(week(start_0)[0], week(start_0)[1], week(start_1)[0], week(start_1)[1]);
			}else if("month" == type){
				var tp_0 = $("#selectYear_0").val()+"-"+makeTwoDigits($("#selectMonth_0").val())+"-01";
				var tp_1 = $("#selectYear_1").val()+"-"+makeTwoDigits($("#selectMonth_1").val())+"-01";
				var tp_2 = $("#selectYear_2").val()+"-"+makeTwoDigits($("#selectMonth_2").val())+"-01";
				showMonthSystemData_1(month(tp_0)[0], month(tp_0)[1], month(tp_1)[0], month(tp_1)[1]);
			}else if("year" == type){
				var tp_0 = $("#selectYear_0").val()+"-01-01";
				var tp_1 = $("#selectYear_1").val()+"-01-01";
				var tp_2 = $("#selectYear_2").val()+"-01-01";
				showYearSystemData_1(year(tp_0)[0], year(tp_0)[1], year(tp_1)[0], year(tp_1)[1]);
			}
		}else if (start_0 != "" && end_0 != "") {
			if("hour" == type){
				showHourSystemData_0(start_0, end_0);
			}else if("day" == type){
				showDaySystemData_0(start_0, end_0);
			}else if("week" == type){
				showWeekSystemData_0(week(start_0)[0], week(start_0)[1]);
			}else if("month" == type){
				var tp_0 = $("#selectYear_0").val()+"-"+makeTwoDigits($("#selectMonth_0").val())+"-01";
				var tp_1 = $("#selectYear_1").val()+"-"+makeTwoDigits($("#selectMonth_1").val())+"-01";
				var tp_2 = $("#selectYear_2").val()+"-"+makeTwoDigits($("#selectMonth_2").val())+"-01";
				showMonthSystemData_0(month(tp_0)[0], month(tp_0)[1]);
			}else if("year" == type){
				var tp_0 = $("#selectYear_0").val()+"-01-01";
				var tp_1 = $("#selectYear_1").val()+"-01-01";
				var tp_2 = $("#selectYear_2").val()+"-01-01";
				showYearSystemData_0(year(tp_0)[0], year(tp_0)[1]);
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
				showHourSystemData_0(start_0, end_0);
			}else if("day" == typeTag){
				$("#selectDate_2").val("0000-00");
				$("#selectDate_3").val("0000-00");
				showDaySystemData_0(start_0, end_0);
			}else if("week" == typeTag){
				$("#selectDate_2").val("0000-00");
				$("#selectDate_3").val("0000-00");
				$("#hiddenCompare_3").html("0000 00-00~0000 00-00");
				$("#hiddenCompare_7").html("0000 00-00~0000 00-00");
				$("#selectWeek").html(week(start_0)[0].substring(0,10)+"~"+week(start_0)[1].substring(0,10));
				showWeekSystemData_0(week(start_0)[0], week(start_0)[1]);
			}else if("month" == typeTag){
				var tp_0 = $("#selectYear_0").val()+"-"+makeTwoDigits($("#selectMonth_0").val())+"-01";
				$("#selectYear_1").val("");
				$("#selectMonth_1").val("");
				$("#selectYear_2").val("");
				$("#selectMonth_2").val("");
				showMonthSystemData_0(month(tp_0)[0], month(tp_0)[1]);
			}else if("year" == typeTag){
				$("#selectYear_1").val("");
				$("#selectYear_2").val("");
				var tp_0 = $("#selectYear_0").val()+"-01-01";
				showYearSystemData_0(year(tp_0)[0], year(tp_0)[1]);
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
		// var showType = $("#showType").find("option:selected").val();
		switch (showType){
			case "hour":
		   	window.location.href="resourceStat.jsp?type=hour"; 
		     break;
		   case "day":
		   	window.location.href="resourceStat.jsp?type=day"; 
		     break;
		   case "week":
		   	window.location.href="resourceStat.jsp?type=week"; 
		     break;
		   case "month":
		   	window.location.href="resourceStat.jsp?type=month"; 
		     break;
		   case "year":
		   	window.location.href="resourceStat.jsp?type=year"; 
		     break;
		   default:
		}
	}

	function changeTab(tabType){
		tabTag = tabType;
		switch (tabTag){
			case "cpu":
		   	$("#cpuDiv").css("z-index","0");
		   	$("#memDiv").css("z-index","-1");
		   	$("#loadDiv").css("z-index","-1");
		   	$("#dataTable").html(cpuHTML);
		   	$("#a_load").attr("class","btn_s");
		   	$("#a_cpu").attr("class","btn_s_on");
		   	$("#a_mem").attr("class","btn_s");
		     break;
		   case "mem":
		   	$("#cpuDiv").css("z-index","-1");
		   	$("#memDiv").css("z-index","0");
		   	$("#loadDiv").css("z-index","-1");
		   	$("#dataTable").html(memHTML);
		   	$("#a_load").attr("class","btn_s");
		   	$("#a_cpu").attr("class","btn_s");
		   	$("#a_mem").attr("class","btn_s_on");
		     break;
		   case "load":
		   	$("#cpuDiv").css("z-index","-1");
		   	$("#memDiv").css("z-index","-1");
		   	$("#loadDiv").css("z-index","0");
		   	$("#dataTable").html(loadHTML);
		   	$("#a_load").attr("class","btn_s_on");
		   	$("#a_cpu").attr("class","btn_s");
		   	$("#a_mem").attr("class","btn_s");
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
		if("hour" == typeTag){
			csvFileName = typeTag+"-"+tabTag + "-" + $("#selectDate").val()+"-H"+$("#selectHour").val() + ".csv";
		}else if("day" == typeTag){
			csvFileName = tabTag + "-D" + $("#selectDate").val()+".csv";
		}else if("week" == typeTag){
			csvFileName = tabTag +"-"+$("#selectDate").val().substring(0,7)+ "-w" + whichWeekMonth($("#selectDate").val())+".csv";
		}else if("month" == typeTag){
			csvFileName = tabTag + "-" + $("#selectYear_0").val()+"-M"+$("#selectMonth_0").val() + ".csv";
		}else if("year" == typeTag){
			$("#stime").html("기준시간<br>"+$("#selectYear_0").val());
			$("#ctime_1").html("비교시간1<br>"+$("#selectYear_1").val());
			$("#ctime_2").html("비교시간2<br>"+$("#selectYear_2").val());
			csvFileName = tabTag + "-Y" + $("#selectYear_0").val() + ".csv";
		}
		document.csvForm.filename.value = csvFileName;
		switch (tabTag){
			case "cpu":
				document.csvForm.data.value = "시간,"+$("#stime").html().replace("<br>"," ").replace("<br>"," ")+","+$("#ctime_1").html().replace("<br>"," ").replace("<br>"," ")+",비교시간1증감율,"+$("#ctime_2").html().replace("<br>"," ").replace("<br>"," ")+",비교시간2증감율"+"\n"  + csvCPUData;
		     break;
		   case "mem":
			   document.csvForm.data.value = "시간,"+$("#stime").html().replace("<br>"," ").replace("<br>"," ")+","+$("#ctime_1").html().replace("<br>"," ").replace("<br>"," ")+",비교시간1증감율,"+$("#ctime_2").html().replace("<br>"," ").replace("<br>"," ")+",비교시간2증감율"+"\n" + csvMEMData;
		     break;
		   case "load":
			   document.csvForm.data.value = "시간,"+$("#stime").html().replace("<br>"," ").replace("<br>"," ")+","+$("#ctime_1").html().replace("<br>"," ").replace("<br>"," ")+",비교시간1증감율,"+$("#ctime_2").html().replace("<br>"," ").replace("<br>"," ")+",비교시간2증감율"+"\n" + csvLOADData;
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
