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



/**
 * 从后台取出餐桌信息，保存到tables数组中
 * freeTables存放空闲餐桌，busyTables存放就餐餐桌
 */
function getTables(){
	$.get("/WirelessOrderWeb/QueryTable.do", {pin : 15}, function(result){
		var tablesTemp; 
		tablesTemp = eval("(" + result + ")");
	    //把所有餐桌对象都放到本地数组tables中,freeTables存放空闲餐桌，busyTables存放就餐餐桌
		for(x in tablesTemp.root){	
			if(tablesTemp.root[x].statusValue == 0){
				freeTables.push(tablesTemp.root[x]);
			}else if(tablesTemp.root[x].statusValue == 1){
				busyTables.push(tablesTemp.root[x]);
			}
			tables.push(tablesTemp.root[x]);
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
//		selectingRegionId = "divAllArea";
		$("#divAllArea").css("backgroundColor", "#FFA07A");
		//默认显示全部状态下的全部区域
		statusType = "allStatus";
		tempForAllStatus = tables;
		temp = tables;
		//得到就餐状态的所有餐桌
//		busyTables = getStatusTables("busy", temp);
		//根据实际窗口的大小设置limit
		var width = $("#divTableShowForSelect").width();
		var height = $("#divTableShowForSelect").height() - 1;
		limit = Math.floor(width/102) * Math.floor(height/82);
		n = Math.ceil(temp.length/limit) ; 
		showTable(temp, pageNow);
		//默认选中第一桌
//		selectTableId = "divtable" + tables[0].alias;
		//保存背景色
//		primeBgColor = $("#" + selectTableId).css("backgroundColor");
		//设置被选中餐桌的背景色
//		$("#" + selectTableId).css("backgroundColor", "#FFA07A");
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
		$("#divShowTableForSelect").html(tableHtml);
		//把占用的餐桌背景色改为占用色（#FFFF00）
		for(x in busyTables){
			$("#divtable" + busyTables[x].alias).css("backgroundColor", "#FF0");
		}
		//设置被选中餐桌的背景色
//		$("#" + selectTableId).css("backgroundColor", "#FFA07A");
		$("#spanPageNow").html("第" + pageNow + "页");
		$("#spanAllPage").html("共" + n + "页");
	}else{
		alert("该区域没有设置餐桌！");
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
 * 根据餐桌id，返回餐桌对象字符串
 * @param {int} tableId
 * @returns {string} 
 */
function getTableBytableId(tableId){
	for(x in tables){
		if(tables[x].alias == tableId){
			return tables[x];		
		}
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






