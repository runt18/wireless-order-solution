$(function(){
//	addRegions();
	getTables();	
});
//当前页
var pageNow = 1;
//设置一页显示的数目
var	limit;
//全部餐桌
var tables = [];
//当前区域下的总的餐桌数组
var temp = [];
//总页数
var n;
//定义存在餐桌的区域id数组
var regionId = [];
var region = [];
/*定义分页函数
 * start: 开始下标
 * limit: 一页最多显示的数目
 * temp: 需要分页的数组对象
 * isPaging: 是否需要分页（true，false）
 */
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
//显示餐桌状态信息，即有多少餐桌为空闲或就餐状态
function showStatus(){
	$("#divStatus").slideToggle();
}
//从后台取出餐桌信息，保存到tables数组中
function getTables(){	
	$.get("/WirelessOrderWeb/QueryTable.do", {random : Math.random(), pin : 15}, function(result){
		var tablesTemp; 
		tablesTemp = eval("(" + result + ")");
		for(x in tablesTemp.root){
			tables.push(tablesTemp.root[x]);
		}
		region.push(tables[0].region);
		regionId.push(tables[0].region.id);
		for(x in tables){
			var flag = false;
			for(y in regionId){
				if(regionId[y] == tables[x].region.id){		
					flag = true;
					break;
				}			
			}
			if(!flag){
				region.push(tables[x].region);
				regionId.push(tables[x].region.id);
			}
		}
		var regionHtml = "";
		for(x in region){
			regionHtml += "<div class='button-base regionSelect' id='region"+region[x].id+
				"' style='margin-bottom: 2px;' onclick='addTables(this)'>"+region[x].name+"</div>";		
		}
		$("#divShowRegion").html(regionHtml);
		$(".button-base.regionSelect").css("backgroundColor", "#F1C40D");
		
		$("#divAllArea").css("backgroundColor", "#DAA520");
		temp = tables;	
		var width = $("#divTableShow").width();
		var height = $("#divTableShow").height() - 1;
		limit = Math.floor(width/102) * Math.floor(height/82);
		n = Math.ceil(temp.length/limit) ; 
		showTable(temp, pageNow);
	});	
}
//function addRegions(){
//		
//
//}

function addTables(o){
	pageNow = 1;
	//把当前区域餐桌数组清空
    temp = [];
    //把对应区域的餐桌对象添加到temp数组中
    pageNow = 1;
    //把当前区域餐桌数组清空
    temp = [];
    //把对应区域的餐桌对象添加到temp数组中
    for(x in tables){
    	if(o == "allTable"){
    		temp.push(tables[x]);
    		$(".button-base.regionSelect").css("backgroundColor", "#F1C40D");
    		$("#divAllArea").css("backgroundColor", "#DAA520");
    	}else if(tables[x].region.id == o.id.substr(6)){
    		temp.push(tables[x]);
    		$(".button-base.regionSelect").css("backgroundColor", "#F1C40D");
    	    $("#divAllArea").css("backgroundColor", "#4EEE99");
    	    $("#" + o.id).css("backgroundColor", "#DAA520");
    	} 
     }
    n = Math.ceil(temp.length/limit) ;      
    showTable(temp, pageNow);
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
//显示第一页
function firstPage(){
	if(temp.length != 0){
		pageNow = 1;
		showTable(temp, pageNow);
	}else{
		alert("该区域没有餐桌！");
	}
}
//显示最后一页
function lastPage(){
	if(temp.length != 0){
		pageNow = n;
		showTable(temp, pageNow);
	}else{
		alert("该区域没有餐桌！");
	}
}
//显示上一页
function frontPage(){	
	if(temp.length != 0){
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
