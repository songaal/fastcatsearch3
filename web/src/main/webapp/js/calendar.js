/*#-------------------------------------------------------------------------------
# Copyright (C) 2011 - 2013 Websquared, Inc.
# All rights reserved.
#-------------------------------------------------------------------------------*/
function formatDate(date) {      
    var myyear = date.getFullYear();     
    var mymonth = date.getMonth()+1;     
    var myweekday = date.getDate();      
         
    if(mymonth < 10){     
        mymonth = "0" + mymonth;     
    }      
    if(myweekday < 10){     
        myweekday = "0" + myweekday;     
    }     
    return (myyear+"-"+mymonth + "-" + myweekday);      
} 

function getMonthDays(year,mon){     
    var monthStartDate = new Date(year, mon, 1);      
    var monthEndDate = new Date(year, mon + 1, 1);      
    var   days   =   (monthEndDate   -   monthStartDate)/(1000   *   60   *   60   *   24);      
    return   days;      
} 

function getDay(d){
	var dt = new Date(d.replace(/-/g,   "/"));
	var year = dt.getFullYear();
	var mon = dt.getMonth() + 1;
	var day = dt.getDate();
	if(mon < 10){
		mon = "0" + mon;
	}
	if(day < 10){
		day = "0" + mon;
	}
	return [year+'-'+mon+'-'+day+' 00:00:00',year+'-'+mon+'-'+day+' 23:59:59'];
}

function week(d){
	var dt = new Date(d.replace(/-/g,   "/"));
	var year = dt.getFullYear();
	var mon = dt.getMonth();
	var day = dt.getDate();
	var wday = dt.getDay();
	var weekStartDate = new Date(year, mon, day - wday);      
    var re_0 = formatDate(weekStartDate);
    
    var weekEndDate = new Date(year, mon, day + (6 - wday));      
    var re_1 = formatDate(weekEndDate); 
//     
	 return [re_0+' 00:00:00',re_1+' 23:59:59'];
}

function whichWeekYear(d){
	var dt = new Date(d.replace(/-/g,   "/"));
	var year = dt.getFullYear();
	var beginDay = new Date(year,0,0); 
	var weekNo = parseInt((dt.getTime()-beginDay.getTime())/(7*60*60*24*1000)) + 1;
	return weekNo;
}

function whichWeekMonth(d){
	var dt = new Date(d.replace(/-/g,   "/"));
	var w = dt.getDay();
	var d = dt.getDate(); 
	return Math.ceil((d + 6 - w) / 7); 
}

function month(d){
	var dt = new Date(d.replace(/-/g,   "/"));
	var year = dt.getFullYear();
	var mon = dt.getMonth();
	
	var monthStartDate = new Date(year, mon, 1);      
    var re_0 = formatDate(monthStartDate);
    var monthEndDate = new Date(year, mon, getMonthDays(year,mon));      
    var re_1 = formatDate(monthEndDate);  
    
	 return [re_0+' 00:00:00',re_1+' 23:59:59'];
}

function year(d){
	var dt = new Date(d.replace(/-/g,   "/"));
	var year = dt.getFullYear();
	
	 return [year+'-01-01 00:00:00',year+'-12-31 23:59:59'];
}

