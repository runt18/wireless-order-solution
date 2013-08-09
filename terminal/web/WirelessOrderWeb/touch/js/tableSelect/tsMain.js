$(function(){
	addRegions();
	addTables("allTable");
});
//当前页
var pageNow = 1;
//设置一页显示的数目
var	limit = 50;
//当前区域下的总的餐桌数组
var temp = [];
//总页数
var n;
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
function showTable(temp, pageNow){	
	if(temp.length != 0){
		var tableHtml = "";
		var pageRoot = [];
		var start = (pageNow-1) * limit;
		pageRoot = getPagingData(start, limit, temp, true);
		for(x in pageRoot){
			tableHtml += "<div class = 'table-base' id = 'divtable" + pageRoot[x].alias +
			"' onclick = 'selectTable(this)'> " + pageRoot[x].alias + 
			"<input type = 'text' value = " + JSON.stringify(pageRoot[x]) + " style = 'display : none' />" +
			"</div>";
		}
		$("#divShowTable").html(tableHtml);
		$("#spanPageNow").html("第" + pageNow + "页");
		$("#spanAllPage").html("共" + n + "页");
	}else{
		alert("该区域没有设置餐桌！");
	}	
}
function selectTable(o){
	$("#" + o.id).css("backgroundColor", "#4CB848");
	$("#divHide").show();
	$("#divShowMessage").show(500);
	var tableMessage, tabMessage;
	tabMessage =  $("#" + o.id + " input").attr("value");
	tableMessage = JSON.parse(tabMessage);
	var htmlMessage = '';
	htmlMessage = "<a href = '#' style = 'position: absolute;  top: 0%;  left: 90%; text-decoration: none; font-weight:bold;'>" + 
				   "关闭" + "</a></ br>";
	htmlMessage += "<p>餐桌id号: " + tableMessage.alias + "</p>" +
				   "<p>类型: " + tableMessage.categoryText + "</p>" +
				   "<p>就餐状态: " + tableMessage.statusText + "</p>" +
				   "<p>区域号: " + tableMessage.region.id + "</p>" +
				   "<p>所在区域: " + tableMessage.region.name + "</p>" +
				   "<p>餐厅id号: " + tableMessage.rid + "</p>";
	$("#divShowMessage").html(htmlMessage);	
	$("#divShowMessage a").click(function (){
		$("#" +o.id).css("backgroundColor", "#87CEEA");
		$("#divShowMessage").hide(500);
		$("#divHide").hide();
	});
}
//用jquery Ajax实现异步请求数据
function addTables(o){
	$.post("/WirelessOrderWeb/QueryTable.do", {random : Math.random, pin : 217}, function(result){
		var tables;
		tables = eval("(" + result + ")");
		pageNow = 1;
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
        n = Math.ceil(temp.length/limit) ;
        showTable(temp, pageNow);
	});
}
//显示下一页信息
function nextPage(){
	//判断是否为最后一页
	if(temp.length != 0){
		if(pageNow == n){
			alert("已经是最后一页了！");
		}else{
			pageNow ++;
		}		
		showTable(temp, pageNow);
	}else{
		alert("请先选择区域！");
	}	
}	
function frontPage(){	
	if(temp.length !=0){
		if(pageNow == 1){
			alert("已经是第一页了！");
		}else{
			pageNow --;
		}
		showTable(temp, pageNow);
	}else{
		alert("请先选择区域！");
	}	
}	  
function showTime(){
	$("#spanTime").text(myDate());
}

setInterval(showTime, 1000); 
/*
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
			//从数据库中取出的餐桌信息
			//alert(responseText);
			var tables;
			tables = eval("(" + responseText + ")");
	        pageNow = 1;
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
	        n = Math.ceil(temp.length/limit) ;
	        showTable(temp, pageNow);
		}
	};	
	xmlhttp.open("POST", "/WirelessOrderWeb/QueryTable.do?pin=217", true);
	xmlhttp.setRequestHeader("Content-type", "application/x-www-form-urlencoded;charset=utf-8");
	xmlhttp.send("random=" + Math.random());	
}
*/