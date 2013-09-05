//当前页
var pageNow = 1;
//设置一页显示的数目
var	limit;
//全部餐桌
var tables = [];
//设置就餐餐桌数组
var busyTables = [];
//设置空闲餐桌数组
var freeTables = [];
//当前状态下的被选中区域的餐桌数组
var tempForRegion = [];
//被选中区域的所有状态餐桌数组
var tempForAllStatus = [];
//临时餐桌数组
var temp = [];
//总页数
var n;
//定义存在餐桌的区域id数组
var regionId = [];
var region = [];
//定义输入框id
var inputNumId;
//定义输入框显示的值
var inputNumVal = "";
//选中区域的id
var selectingRegionId;
//设置输入桌号界面的类型
var typeForInputTableNum;
//设置当前状态类型（busy， free, allStatus）
var statusType = "";

/**
 * onload
 */
$(function(){
	initTables();
});

/**
 * 定时器，定时刷新餐桌选择页面数据
 */
window.setInterval("initTables()", 240000);

/**
 * 初始化餐桌信息，保存到tables数组中
 * freeTables存放空闲餐桌，busyTables存放就餐餐桌
 */
function initTables(){
		// 加载菜单数据
		$.ajax({
			url : '../QueryTable.do',
			type : 'post',
			data : {
				pin : pin,
				random : Math.random(),
			},
			success : function(data, status, xhr){
				tables = [];
				busyTables = [];
				freeTables = [];
				region = [];
				regionId = [];
				data = eval("(" + data + ")");
				if(data.success){
					//把所有餐桌对象都放到本地数组tables中,freeTables存放空闲餐桌，busyTables存放就餐餐桌
					for(x in data.root){	
						if(data.root[x].statusValue == 0){
							freeTables.push(data.root[x]);
						}else if(data.root[x].statusValue == 1){
							busyTables.push(data.root[x]);
						}
						tables.push(data.root[x]);
					}
					//从tables数组中，遍历得到含有餐桌的区域数组region
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
					//添加区域信息
					var regionHtml = "";
					for(x in region){
						regionHtml += "<div class='button-base regionSelect' id='region"+region[x].id+
							"' style='margin-bottom: 2px;' onclick='addTables(this)'>"+region[x].name+"</div>";		
					}
					$("#divShowRegion").html(regionHtml);
					//设置区域未选中状态的背景色（#D4F640）
					$(".button-base.regionSelect").css("backgroundColor", "#D4F640");
					//默认选中全部状态区域（#FFA07A）
					$("#divAllArea").css("backgroundColor", "#FFA07A");
					//默认显示全部状态下的全部区域
					statusType = "allStatus";
					tempForAllStatus = tables;
					temp = tables;
					//根据实际窗口的大小设置limit
					var width = $("#divTableShowForSelect").width();
					var height = $("#divTableShowForSelect").height() - 1;
					limit = Math.floor(width/102) * Math.floor(height/82);
					n = Math.ceil(temp.length/limit) ; 
					showTable(temp, pageNow);
				}else{
					Util.msg.alert({
						title : data.title,
						msg : data.msg, 
					});
				}
			},
			error : function(request, status, err){
				Util.msg.alert({
					title : '温馨提示',
					msg : err, 
				});
			}
		});	
}

/**
 * 显示餐桌
 * @param {object} temp 需要显示的餐桌数组
 * @param {int} pageNow 当前页数
 */
function showTable(temp, pageNow){	
	if(temp.length != 0){
		var tableHtml = "";
		var pageRoot = [];
		var start = (pageNow-1) * limit;
		var tableName;
		pageRoot = getPagingData(start, limit, temp, true);
		for(x in pageRoot){
			if(pageRoot[x].name ==""){
				tableName = pageRoot[x].alias + "号桌";
			}else{
				tableName = pageRoot[x].name;
			}
			tableHtml += "<div class = 'table-base' tableObject = " + JSON.stringify(pageRoot[x]) + " id = 'divtable" + pageRoot[x].alias +
			"' onclick = 'selectTable(this)'> " + 
			"<div style = 'margin-top: 25px; font-weight: bold;'>" + 
			tableName + "</div>" + 
			"<div style = 'color: #462B77; font-size: 10px;'>" + pageRoot[x].alias + "</div>" + 
			"</div>";
		}
		$("#divTableShowForSelect").html(tableHtml);
		//把占用的餐桌背景色改为占用色（#FFFF00）
		for(x in busyTables){
			$("#divtable" + busyTables[x].alias).css("backgroundColor", "#FF0");
		}
		$("#spanPageNow").html("第" + pageNow + "页");
		$("#spanAllPage").html("共" + n + "页");
	}else{
		$("#divTableShowForSelect").html("");
	}	
}

//判断是否有该餐桌alias
function hasTable(tableObject, tableNo){
	for(x in tableObject){
		if(tableNo == tableObject[x].alias){
			return true;
		}
	}
	return false;
}

/**
 * 根据餐桌id，返回餐桌对象
 * @param {int} tableId
 * @returns {object} 
 */
function getTableBytableId(tableId){
	for(x in tables){
		if(tables[x].alias == tableId){
			return tables[x];		
		}
	}
}

/**
 * 定义分页函数
 * @param {int} start 开始下标
 * @param {int} limit 一页最多显示的数目
 * @param {object} tempObject 需要分页的数组对象
 * @param {boolean} isPaging 是否需要分页
 * @returns {object} pageRoot 已经完成分页的数组对象
 */
function getPagingData(start, limit, tempObject, isPaging){
    var pageRoot = [];
    if(tempObject.length != 0 && isPaging){ 
    	var dataIndex = start, dataSize = limit;		
    	dataSize = (dataIndex + dataSize) > tempObject.length ? dataSize - ((dataIndex + dataSize) - tempObject.length) : dataSize;			
    	pageRoot = tempObject.slice(dataIndex, dataIndex + dataSize);	
    }else{
    	pageRoot = tempObject;
    }	
	return pageRoot;
}

//显示第一页
function firstPage(){
	if(temp.length != 0){
		pageNow = 1;
		showTable(temp, pageNow);
	}
}

//显示最后一页
function lastPage(){
	if(temp.length != 0){
		pageNow = n;
		showTable(temp, pageNow);
	}
}

//显示上一页
function frontPage(){	
	if(temp.length != 0){
		if(pageNow == 1){
			return;
		}else{
			pageNow --;
		}
		showTable(temp, pageNow);
	}	
}

//显示下一页信息
function nextPage(){
	//判断是否为最后一页
	if(temp.length != 0){
		if(pageNow == n){
			return;
		}else{
			pageNow ++;
		}		
		showTable(temp, pageNow);
	}	
}	











