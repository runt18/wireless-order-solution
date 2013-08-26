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
		co.show({
			table : getTableBytableId(tableNo)
		});
	}else{
		alert("没有该餐桌，请重新输入一个桌号！");
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


