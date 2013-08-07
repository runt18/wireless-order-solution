$(function(){
	addRegions();
	addTables("allTable");
});
//从数据库中取出的餐桌信息
var tables;
//当前页
var pageNow = 0;
//设置一页显示的数目
var	limit = 24;
//当前区域下的总的餐桌数组
var temp = [];
//定义分页函数，start为开始下标，limit为一页最多显示的数目，temp为需要分页的数组对象
function getPagingData(start, limit, temp, isPaging){
    var pageRoot = [];
    if(temp.length != 0 && isPaging){ 
    	var dataIndex = start, dataSize = limit;		
    	dataSize = (dataIndex + dataSize) > temp.length ? dataSize - ((dataIndex + dataSize) - temp.length) : dataSize;			
    	pageRoot = temp.slice(dataIndex, dataIndex + dataSize);	
    }else{
    	pageRoot = temp;
    }
	
	return pageRoot;
}
function addTables(o){
	var xmlhttp;
	if(window.XMLHttpRequest){
		xmlhttp = new XMLHttpRequest();
	}else{
		xmlhttp = new ActiveXObject("Microsoft.XMLHTTP");
	}
	xmlhttp.onreadystatechange = function(){
		if(xmlhttp.readyState == 4 && xmlhttp.status == 200){
			var responseText = xmlhttp.responseText;
			 tables = eval("(" + responseText + ")");
			 var tableHtml = "";
	         //定义需要显示的当前页的餐桌数组
	         var pageRoot = [];
	         //把当前页设置为0
	         pageNow = 0;
	         //定义开始餐桌的下标
	         var start = pageNow*limit;
	         //把当前区域餐桌数组清空
	         temp = [];
	         //把对应区域的餐桌对象添加到temp数组中
	         for(x in tables.root){
	        	 if(tables.root[x].region.id == o.id){
	        		 temp.push(tables.root[x]);
	        	 }else if(o == "allTable"){
	        		 temp.push(tables.root[x]);
	        	 } 
	         }
			//显示当前页餐桌信息
	         pageRoot = getPagingData(start, limit, temp, true);
			for(x in pageRoot){
				tableHtml += "<div class='table-base' >"+pageRoot[x].alias+"</div>";
			}
			$("#divShowTable").html(tableHtml);
			var n;
			n = parseInt(temp.length/limit) ;
			$("#spanPageNow").html("第"+(pageNow+1)+"页");
			$("#spanAllPage").html("共"+(n+1)+"页");
		}
	};
	
	xmlhttp.open("POST", "/WirelessOrderWeb/QueryTable.do?pin=217", true);
	xmlhttp.setRequestHeader("Content-type", "application/x-www-form-urlencoded;charset=utf-8");
	xmlhttp.send("random="+Math.random());
}
//显示下一页信息
function nextPage(){
	var start;
	var n;
	n = parseInt(temp.length/limit) ;
	//判断是否为最后一页
	if(pageNow == n || temp.length == limit){
		alert("已经是最后一页了！");
	}else{
		pageNow++;
	}		
	start = pageNow*limit;
	var tableHtml = "";
    var pageRoot = [];
    pageRoot = getPagingData(start, limit, temp, true);
	for(x in pageRoot){
		tableHtml += "<div class='table-base' >"+pageRoot[x].alias+"</div>";
	}
	$("#divShowTable").html(tableHtml);
	$("#spanPageNow").html("第"+(pageNow+1)+"页");
	$("#spanAllPage").html("共"+(n+1)+"页");
}	
function frontPage(){	
	var start;
	var n;
	n = parseInt(temp.length/limit) ;
	if(pageNow==0){
		alert("已经是第一页了！");
	}else{
		pageNow--;
	}	
	start = pageNow*limit;
	var tableHtml = "";
	var pageRoot = [];
    pageRoot = getPagingData(start, limit, temp, true);
	for(x in pageRoot){
		tableHtml += "<div class='table-base' >"+pageRoot[x].alias+"</div>";
	}		
	$("#divShowTable").html(tableHtml);
	$("#spanPageNow").html("第"+(pageNow+1)+"页");
	$("#spanAllPage").html("共"+(n+1)+"页");
}	                               
function timer(obj,txt){
                obj.text(txt);
}        
function showTime(){                                
        var today = new Date();
        var weekday = ["星期日", "星期一", "星期二", "星期三", "星期四", "星期五", "星期六"];                          
        var y = today.getFullYear() + "年";
        var month = today.getMonth() + 1 + "月";
        var td = today.getDate() + "日";
        var d = "(" + weekday[today.getDay()] + ")";
        var tm = today.getHours() + ":" + today.getMinutes() + ":" + today.getSeconds();       
        timer($("#Y"),y);
        timer($("#MH"),month);        
        timer($("#TD"),td);        
        timer($("#D"),d);
        timer($("#TM"),tm);
}        
setInterval(showTime, 1000); 
