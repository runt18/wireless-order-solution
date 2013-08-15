$(function(){
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
//设置占用餐桌的数组
var busyTables = [];
//总页数
var n;
//定义存在餐桌的区域id数组
var regionId = [];
var region = [];
//定义输入框id
var inputNumId;
var inputNumVal = "";
//被选中餐桌的id
var selectTableId;
//被选中餐桌原来的背景色
var primeBgColor;

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
//function showEating(){
//	pageNow = 0;
//	for(x in temp){
//		if(temp[x].statusText == "就餐"){
//			eating.push(temp[x]);
//		}
//	}
//	limit = Math.floor(width/102) * Math.floor(height/82);
//	n = Math.ceil(eating.length/limit) ; 
//	showTable(eating, pageNow);
//	
//}
//从后台取出餐桌信息，保存到tables数组中
function getTables(){
	$.get("/WirelessOrderWeb/QueryTable.do", {random : Math.random(), pin : 15}, function(result){
		var tablesTemp; 
		tablesTemp = eval("(" + result + ")");
		for(x in tablesTemp.root){
			if(tablesTemp.root[x].statusValue == 1){
				busyTables.push(tablesTemp.root[x]);
			}
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
		//添加区域信息
		var regionHtml = "";
		for(x in region){
			regionHtml += "<div class='button-base regionSelect' id='region"+region[x].id+
				"' style='margin-bottom: 2px;' onclick='addTables(this)'>"+region[x].name+"</div>";		
		}
		$("#divShowRegion").html(regionHtml);
		$(".button-base.regionSelect").css("backgroundColor", "#F1C40D");
		$("#divAllArea").css("backgroundColor", "#DAA520");
		temp = tables;	
		var width = $("#divTableShowForSelect").width();
		var height = $("#divTableShowForSelect").height() - 1;
		limit = Math.floor(width/102) * Math.floor(height/82);
		n = Math.ceil(temp.length/limit) ; 
		showTable(temp, pageNow);
		//默认选中第一桌
		selectTableId = "divtable" + tables[0].alias;
		//保存背景色
		primeBgColor = $("#" + selectTableId).css("backgroundColor");
		//设置被选中餐桌的背景色
		$("#" + selectTableId).css("backgroundColor", "#FFA07A");
	});	
}
//如果餐桌被选中，则把该餐桌底色改为选中色(#FFA07A)
function changeColorForSelect(selectingId){
	//把上次被选的餐桌背景色还原
	$("#" + selectTableId).css("backgroundColor", primeBgColor);
	//得到本次被选的餐桌
	selectTableId = selectingId;
	//保存本次被选餐桌的原有色
	primeBgColor = $("#" + selectTableId).css("backgroundColor");
	//设置被选中餐桌的背景色
	$("#" + selectTableId).css("backgroundColor", "#FFA07A");	
}
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
		var tableName;
		pageRoot = getPagingData(start, limit, temp, true);
		for(x in pageRoot){
			if(pageRoot[x].name ==""){
				tableName = pageRoot[x].alias + "号桌";
			}else{
				tableName = pageRoot[x].name;
			}
			tableHtml += "<div class = 'table-base' id = 'divtable" + pageRoot[x].alias +
			"' onclick = 'selectTable(this)'> " + tableName + 
			"<input type = 'text' value = " + JSON.stringify(pageRoot[x]) + " style = 'display : none' />" +
			"</div>";
		}
		
		$("#divShowTableForSelect").html(tableHtml);
		//设置被选中餐桌的背景色
		$("#" + selectTableId).css("backgroundColor", "#FFA07A");
		//把占用的餐桌背景色改为占用色（#FFFF00）
		for(x in busyTables){
			$("#divtable" + busyTables[x].alias).css("backgroundColor", "#FF0");
		}
		$("#spanPageNow").html("第" + pageNow + "页");
		$("#spanAllPage").html("共" + n + "页");
	}else{
		alert("该区域没有设置餐桌！");
	}	
}

$(".keyboardbutton").mouseover(function(){
	$(this).css("backgroundColor", "#FFD700");
});
$(".keyboardbutton").mouseout(function(){
	$(this).css("backgroundColor", "#75B2F4");
});
function selectTable(o){
	changeColorForSelect(o.id);
	$("#divHideForTableSelect").show();
	$("#divShowMessageForTableSelect").show(500);
	var tableMessage, tabMessage;
	tabMessage =  $("#" + o.id + " input").attr("value");
	tableMessage = JSON.parse(tabMessage);
	//关闭该界面
	$("#btnCancelForShowMessageTS").click(function (){
		$("#divShowMessageForTableSelect").hide(500);
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
	
}
function inputNum(o){
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
		inputNumVal = tempNum.substring(0,tempNum.length-1);
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
//获得桌号，判断是否跳到点菜页面
$("#btnSubmitForSelectTableNumTS").click(function(){
	//获得餐桌号
	var tableNo;
	tableNo = parseInt($("#txtTableNumForTS").val());	
	if(hasTable(tableNo)){
		$("#divSelectTableNumForTs").hide(500);
		$("#divHideForTableSelect").hide();
		inputNumVal = "";
		$("#txtTableNumForTS").val("");
		//设置该餐桌为选中状态
		changeColorForSelect("divtable" + tableNo);
		toggleContentDisplay({type:'show', renderTo:'divCreateOrder'});
	}else{
		alert("没有该餐桌，请重新输入一个桌号！");
	}	
});
//模拟开台函数
function setEatingTable(){
	var tableNum;
	var peopleNum;
	tableNum = parseInt($("#txtTableNumForSM").val());
	peopleNum = parseInt($("#txtPeopleNumForSM").val());
	alert("开台桌号：" + tableNum + "  " + "人数：" + peopleNum);
	
}
//判断是否有该餐桌alias
function hasTable(tableNo){
	for(x in tables){
		if(tableNo == tables[x].alias){
			return true;
		}
	}
	return false;
}
//弹出桌号选择框，能够转到点菜页面
function createOrderForTS(){
	//把上次被选的餐桌背景色还原
	$("#" + selectTableId).css("backgroundColor", primeBgColor);
	//selectTableId = o.id;
	//设置被选中餐桌的背景色
	$("#" + selectTableId).css("backgroundColor", "#FFA07A");
	$("#divHideForTableSelect").show();
	$("#divSelectTableNumForTs").show(500);
	
	$("#divTopForSelectTableNumTS").html("<div style = 'font-size: 20px; " +
			"font-weight: bold; color: #fff; margin: 15px;'>" +
			"请输入桌号，确定进入点菜界面</div>");
	
	//关闭该界面
	$("#btnCloseForSelectTableNumTS").click(function(){
		$("#divSelectTableNumForTs").hide(500);
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
