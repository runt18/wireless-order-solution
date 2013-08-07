$(function(){
	addRegions();
	addTables("allTable");
});
//从数据库中取出的餐桌信息
var tables1;
//当前页
var pageNow = 0;
//设置一页显示的数目
var	limit = 50;
//当前区域下的总的餐桌数组
var temp = [];
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
			 tables1 = eval("(" + responseText + ")");
			 var obj = document.getElementById("divShowTable");
	         obj.innerHTML = "";
	         //定义需要显示的当前页的餐桌数组
	         var pageRoot = [];
	         //把当前页设置为0
	         pageNow = 0;
	         //定义开始餐桌的下标
	         var start = pageNow*limit;
	         //把当前区域餐桌数组清空
	         temp = [];
	         //把对应区域的餐桌对象添加到temp数组中
	         for(x in tables1.root){
	        	 if(tables1.root[x].region.id == o.id){
	        		 temp.push(tables1.root[x]);
	        	 }else if(o == "allTable"){
	        		 temp.push(tables1.root[x]);
	        	 } 
	         }
			var dataIndex = start, dataSize = limit;
			//dataSize为当前页所显示的实际数目
			dataSize = (dataIndex + limit) > temp.length ? dataSize - ((dataIndex + dataSize) - temp.length) : dataSize;
			pageRoot = temp.slice(dataIndex, dataIndex + dataSize);
			//显示当前页餐桌信息
			for(x in pageRoot){
				obj.innerHTML += "<div class='table-base' >"+pageRoot[x].alias+"</div>";
			}
		}
	};
	
	xmlhttp.open("POST", "/WirelessOrderWeb/QueryTable.do?pin=217", true);
	xmlhttp.setRequestHeader("Content-type", "application/x-www-form-urlencoded;charset=utf-8");
	xmlhttp.send(null);
}
//显示下一页信息
function nextPage(){
	var start;
	var n;
	n = parseInt(temp.length/limit) ;
	//判断是否为最后一页
	if(pageNow == n){
		alert("已经是最后一页了！");
	}else{
		pageNow++;
	}		
	start = pageNow*limit;
	var obj = document.getElementById("right");
	obj.innerHTML = "";	
    var pageRoot = [];
	var dataIndex = start, dataSize = limit;		
	dataSize = (dataIndex + dataSize) > temp.length ? dataSize - ((dataIndex + dataSize) - temp.length) : dataSize;			
	pageRoot = temp.slice(dataIndex, dataIndex + dataSize);				
	for(x in pageRoot){
		obj.innerHTML += "<div class='table-base' >"+pageRoot[x].alias+"</div>";
	}
}	
function frontPage(){	
	var start;
	if(pageNow==0){
		alert("已经是第一页了！");
	}else{
		pageNow--;
	}	
	start = pageNow*limit;
	var obj = document.getElementById("right");
	obj.innerHTML = "";	
	var pageRoot = [];
	var dataIndex = start, dataSize = limit;				
	dataSize = (dataIndex + dataSize) > temp.length ? dataSize - ((dataIndex + dataSize) - temp.length) : dataSize;				
	pageRoot = temp.slice(dataIndex, dataIndex + dataSize);	
	for(x in pageRoot){
		obj.innerHTML += "<div class='table-base' >"+pageRoot[x].alias+"</div>";
	}		
}	