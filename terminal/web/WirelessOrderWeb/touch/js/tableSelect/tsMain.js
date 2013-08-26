$(function(){
	getTables();	
});
//当前页
var pageNow = 1;
//设置一页显示的数目
var	limit;
//全部餐桌
var tables = [];
//设置就餐状态餐桌的数组
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
//定义输入框的值
var inputNumVal = "";
//选中区域的id
var selectingRegionId;
//被选中餐桌的id
//var selectTableId;
//被选中餐桌原来的背景色
//var primeBgColor;
//设置输入桌号界面的类型
var typeForInputTableNum;
//设置当前状态类型（busy， free, allStatus）
var statusType = "";

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
 * 取得当前区域下不同状态的餐桌数组 
 * @param {string} type 状态类型，分为空闲（free），就餐（busy），全部状态（allStatus）
 * @param {object} tempTables 区域数组对象
 * @returns {object} statusTables 餐桌数组对象 
 */
function getStatusTables(type, tempTables){
	var statusTables = [];
	if(type == "free"){
		for(x in tempTables){
			if(tempTables[x].statusValue == 0){
				statusTables.push(tempTables[x]);
			}
		}
	}else if(type == "busy"){
		for(x in tempTables){
			if(tempTables[x].statusValue == 1){
				statusTables.push(tempTables[x]);
			}
		}		
	}else if(type == "allStatus"){
		for(x in tempTables){
			statusTables.push(tempTables[x]);
		}	
	}
	return statusTables;
}
///**
// * 把被选中的餐桌背景色改为选中色(#FFA07A)
// * @param {string} selectingId 被选中餐桌的id
// */
//function changeColorForSelect(selectingId){
//	//把上次被选的餐桌背景色还原
//	$("#" + selectTableId).css("backgroundColor", primeBgColor);
//	//得到本次被选的餐桌
//	selectTableId = selectingId;
//	//保存本次被选餐桌的原有色
//	primeBgColor = $("#" + selectTableId).css("backgroundColor");
//	//设置被选中餐桌的背景色
//	$("#" + selectTableId).css("backgroundColor", "#FFA07A");	
//}
/**
 * 点击区域，显示不同状态的餐桌数组
 * @param {object} o 
 */
function addTables(o){
    pageNow = 1;
    //把当前区域餐桌数组清空
    temp = [];
    tempForAllStatus = [];
    tempForRegion = [];
    var statusTable = [];
    //初始化当前区域的所有状态餐桌数组
    for(x in tables){
    	if(o == "allTable"){
    		tempForAllStatus.push(tables[x]);	
    	}else if(tables[x].region.id == o.id.substr(6)){
    		tempForAllStatus.push(tables[x]); 		
    	} 
     }
   //判断当前处于哪个状态
    if(statusType == "allStatus"){
    	statusTable = tables;
    }else if(statusType == "free"){
    	statusTable = freeTables;
    }else if(statusType == "busy"){
    	statusTable = busyTables;
    }
    //获得当前状态的对应区域餐桌数组
    for(x in statusTable){
    	if(o == "allTable"){
    		tempForRegion.push(statusTable[x]);
    		$(".button-base.regionSelect").css("backgroundColor", "#D4F640");
    		$("#divAllArea").css("backgroundColor", "#FFA07A");
    	}else if(statusTable[x].region.id == o.id.substr(6)){
    		tempForRegion.push(statusTable[x]);
    		$(".button-base.regionSelect").css("backgroundColor", "#D4F640");
    	    $("#divAllArea").css("backgroundColor", "#4EEE99");
    	    $("#" + o.id).css("backgroundColor", "#FFA07A");
    	} 
     }  
    temp = tempForRegion;
    n = Math.ceil(temp.length/limit) ;      
    showTable(temp, pageNow);
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
/**
 * 点击全部状态按钮
 */
function showStatus(){
	$("#divStatus").slideToggle();
	$("#divAllStatus").css("backgroundColor", "#FFA07A");
	$("#busyForTableSelect").css("backgroundColor", "#D4F640");
	$("#freeForTableSelect").css("backgroundColor", "#D4F640");
	statusType = "allStatus";
	pageNow = 1;
	temp = tempForAllStatus;
	n = Math.ceil(temp.length/limit) ;      
    showTable(temp, pageNow);
}
//点击空闲状态按钮
$("#freeForTableSelect").click(function(){
	$("#freeForTableSelect").css("backgroundColor", "#FFA07A");
	$("#busyForTableSelect").css("backgroundColor", "#D4F640");
	$("#divAllStatus").css("backgroundColor", "#4EEE99");
	statusType = "free";
	pageNow = 1;
	temp = getStatusTables("free", tempForAllStatus);
	n = Math.ceil(temp.length/limit) ;      
    showTable(temp, pageNow);
});
//点击就餐状态按钮
$("#busyForTableSelect").click(function(){
	$("#busyForTableSelect").css("backgroundColor", "#FFA07A");
	$("#freeForTableSelect").css("backgroundColor", "#D4F640");
	$("#divAllStatus").css("backgroundColor", "#4EEE99");
	statusType = "busy";
	pageNow = 1;
	temp = getStatusTables("busy", tempForAllStatus);
	n = Math.ceil(temp.length/limit) ;      
    showTable(temp, pageNow);
});
//设置鼠标移到数字键盘上的移进移出效果
$(".keyboardbutton").mouseover(function(){
	$(this).css("backgroundColor", "#FFD700");
});
$(".keyboardbutton").mouseout(function(){
	$(this).css("backgroundColor", "#75B2F4");
});
/**
 * 选中一张餐桌
 * @param {object} o 被选中的餐桌节点
 */
function selectTable(o){
	var tableMessage, tabMessage;
	tabMessage = document.getElementById(o.id).getAttribute("tableObject");
	tableMessage = JSON.parse(tabMessage);
	//判断是否为已点菜餐桌
	if(tableMessage.statusText == "就餐"){
//		toggleContentDisplay({type:'show', renderTo:'divUpdateOrder'});
		
		uo.show({
			table : getTableBytableId(o.id.substring(8, o.id.length))
		});
	}else{
		$("#divHideForTableSelect").show();
		$("#divShowMessageForTableSelect").show(100);
		//关闭该界面
		$("#btnCancelForShowMessageTS").click(function (){
			$("#divShowMessageForTableSelect").hide(100);
			$("#divHideForTableSelect").hide();
			inputNumVal = "";
			$("#txtPeopleNumForSM").val("");
		});
		$("#txtTableNumForSM").val(tableMessage.alias);
		$("#txtPeopleNumForSM").select();
		inputNumId  = "txtPeopleNumForSM";
		//点击选中按钮，选择桌号输入框
		$("#selectTableNum").click(function(){
			$("#txtTableNumForSM").select();
			inputNumId  = "txtTableNumForSM";
			inputNumVal = "";
		});
		//点击选中按钮，选择人数输入框
		$("#selectPeopleNum").click(function(){
			$("#txtPeopleNumForSM").select();
			inputNumId  = "txtPeopleNumForSM";
			inputNumVal = "";
		});
		//直接点击桌号 输入框
		$("#txtTableNumForSM").click(function(){
			inputNumId  = "txtTableNumForSM";
			inputNumVal = "";
			inputNumVal += $("#" + inputNumId).val();
		});
		//直接点击人数 输入框
		$("#txtPeopleNumForSM").click(function(){
			inputNumId  = "txtPeopleNumForSM";
			inputNumVal = "";
			inputNumVal += $("#" + inputNumId).val();
		});
	}	
}
/**
 * 点击数字键盘上的数字，对输入框进行输入
 * @param {object} o
 */
function inputNum(o){
	inputNumVal += o.value;
	$("#" + inputNumId).val(inputNumVal);
	//判断人数是否超过限定
	if(inputNumId == "txtPeopleNumForSM"){
		if(parseInt(inputNumVal) > 999){
			alert("人数超过限定，请重新输入！");
			inputNumVal = "";
			$("#" + inputNumId).val(inputNumVal);
		}
	}
	//判断桌号是否超过限定
	if(inputNumId == "txtTableNumForSM" || inputNumId =="txtTableNumForTS"){
		if(parseInt(inputNumVal) > 65536){
			alert("桌号超过限定，请重新输入！");
			inputNumVal = "";
			$("#" + inputNumId).val(inputNumVal);
		}
	}
	$("#" + inputNumId).focus();	
}
//清除一位数字
$("#btnBackOneForSTNum").click(function(){
	var tempNum;
	tempNum = $("#txtTableNumForTS").val();
	if(tempNum.length > 0){
		inputNumVal = tempNum.substring(0, tempNum.length-1);
		$("#txtTableNumForTS").val(inputNumVal);
	}
	$("#" + inputNumId).focus();
});
//重置数字
$("#btnBackAllForSTNum").click(function(){
	inputNumVal = "";
	$("#txtTableNumForTS").val(inputNumVal);
	$("#" + inputNumId).focus();
});
//跳转到点菜界面
function renderToCreateOrder(tableNo, peopleNo){
	if(hasTable(tables, tableNo)){
		$("#divSelectTableNumForTs").hide(100);
		$("#divShowMessageForTableSelect").hide(100);
		$("#divHideForTableSelect").hide();
		inputNumVal = "";
		$("#txtTableNumForTS").val(inputNumVal);
		$("#txtPeopleNumForSM").val(inputNumVal);
		//设置该餐桌为选中状态
//		changeColorForSelect("divtable" + tableNo);
		//toggleContentDisplay({type:'show', renderTo:'divCreateOrder'});
		co.show({
			table : getTableBytableId(tableNo)
		});
	}else{
		alert("没有该餐桌，请重新输入一个桌号！");
	}	
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
//点击空台 8弹出点菜页面上的点菜按钮
$("#btnRenderToCreateOrder").click(function(){
	var tableNo;
	var peopleNo;
	tableNo = parseInt($("#txtTableNumForSM").val());
	if($("#txtPeopleNumForSM").val() == ""){
		peopleNo = 0;
	}else{
		peopleNo = parseInt($("#txtPeopleNumForSM").val());
	}	
	renderToCreateOrder(tableNo, peopleNo);
});
//点击桌号选择页面的确定（点菜）按钮
$("#btnSubmitForSelectTableNumTS").click(function(){
	//获得餐桌号
	var tableNo;
	var peopleNo = 0;
	if($("#txtTableNumForTS") == ""){
		tableNo = -1;
	}else{
		tableNo = parseInt($("#txtTableNumForTS").val());
	}
	if(typeForInputTableNum == "createOrder"){
		renderToCreateOrder(tableNo, peopleNo);	
	}else if(typeForInputTableNum == "check"){
		//判断该餐桌是否已点菜（下单）
		if(hasTable(tables, tableNo)){
			if(hasTable(busyTables, tableNo)){
				alert(tableNo + "号餐桌已点菜，可以结账");
			}else{
				alert("该餐桌未点菜，不能结账");
			}
		}else{
			alert("没有该餐桌，请重新输入一个桌号！");
		}
	}	
});
//判断是否有该餐桌alias
function hasTable(tableObject, tableNo){
	for(x in tableObject){
		if(tableNo == tableObject[x].alias){
			return true;
		}
	}
	return false;
}
//点击工具栏上的点菜按钮，弹出桌号选择框，能够转到点菜页面
function createOrderForTS(){
	showSelectTableNumTS("createOrder");
}
//点击工具栏上的结账按钮
function checkOnTS(){
	showSelectTableNumTS("check");
}
//弹出和关闭桌号选择界面
function showSelectTableNumTS(type){
	$("#divHideForTableSelect").show();
	$("#divSelectTableNumForTs").show(100);
	typeForInputTableNum = type;
	var title = "";
	if(typeForInputTableNum == "createOrder"){
		title = "请输入桌号，确定进入点菜界面";
	}else if(typeForInputTableNum == "check"){
		title = "请输入结账桌号";
	}
	$("#divTopForSelectTableNumTS").html("<div style = 'font-size: 20px; " +
			"font-weight: bold; color: #fff; margin: 15px;'>" + title + "</div>");
	//关闭该界面
	$("#btnCloseForSelectTableNumTS").click(function(){
		$("#divSelectTableNumForTs").hide(100);
		$("#divHideForTableSelect").hide();
		inputNumVal = "";
		$("#txtTableNumForTS").val("");
	});
	$("#txtTableNumForTS").select();
	inputNumId  = "txtTableNumForTS";
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
