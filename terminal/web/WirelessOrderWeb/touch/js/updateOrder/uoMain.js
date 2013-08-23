$(function(){
	initOrderData();
	initCancelReason();
});
var uoFood = [];
var uoOther;
var selectigRow = "";
var typeForKeyNum;
var cancelReasonData = [];
var inputNumVal1 = "";
var inputNumId1;
var count;
var selectingReasonName = "";
var selectingReasonId = "";
var customNumForUo;
function initOrderData(){
	// 加载菜单数据
	$.ajax({
		url : '../QueryOrder.do',
		type : 'post',
		data : {
			pin : pin,
			restaurantID : restaurantID,
			tableID : 1,			
		},
		success : function(data, status, xhr){
			if(data.success){
				for(x in data.root){
					uoFood.push(data.root[x]);
				}
				uoOther = data.other;
				showNorthForUpdateOrder();
				showOrder();
				showDescForUpdateOrder();
			}else{
				alert('初始化菜品数据失败.');
			}
		},
		
		error : function(request, status, err){
			alert('初始化菜品数据失败.');
		}
	});
}
function initCancelReason(){
	$.ajax({
		url : '../QueryCancelReason.do',
		type : 'post',
		data : {
			pin : pin,
			restaurantID : restaurantID,
		},
		success : function(data, status, xhr){
			data = JSON.parse(data);
			for(x in data.root){
				cancelReasonData.push(data.root[x]);
			}
		},
		
		error : function(request, status, err){
			alert('初始化退菜数据失败.');
		}
	});
}
function showNorthForUpdateOrder(){
	var html = "";
	var tableName;
	if(uoOther.order.table.name == ""){
		tableName = uoOther.order.table.alias + "号桌";
	}else{
		tableName = uoOther.order.table.name;
	}
	customNum = uoOther.order.customNum;
	html = "<div>" +
			"<span style ='margin: 10px;'>账单号：" + uoOther.order.id + " </span>" +
			"<span style ='margin: 10px;'>餐台号：" + uoOther.order.table.alias + " </span>" +
			"<span style ='margin: 10px;'>餐台名： " + tableName + "</span>" +
			"<span style ='margin: 10px;' id = 'customNumForUO'>用餐人数：" + customNum + " </span>" +			
		"</div>";
	$("#divNorthForUpdateOrder").html(html);
}
function showOrder(){
	var html = "";
	var n = 1;
	for(x in uoFood){
		html += "<tr id = 'truoFood" + n + "' onclick = 'selectUOFood(this)'>" + 
				"<td>" + n + "</td>" +
				"<td>" + uoFood[x].name + "</td>" +
				"<td>" + uoFood[x].count.toFixed(1) + "</td>" +
				"<td>" + uoFood[x].tasteGroup.tastePref + "</td>" +
				"<td>" + uoFood[x].actualPrice.toFixed(2) + "</td>" +
				"<td>" + uoFood[x].totalPrice.toFixed(2) + "</td>" +
				"<td>" + uoFood[x].orderDateFormat + "</td>" +
				"<td><input type = 'button' value = '退菜' " + 
				"class = 'cancelFoodBtn' id = 'btnuo" + n + "' /></td>" +
				"<td>" + uoFood[x].waiter + "</td></tr>";
		n++;
	}
	$("#divCenterForUpdateOrder table").append(html);
	//设置鼠标移到退菜按钮上的移进移出效果
	$(".cancelFoodBtn").mouseover(function(){
		$(this).css("backgroundColor", "#FFD700");
	});
	$(".cancelFoodBtn").mouseout(function(){
		$(this).css("backgroundColor", "#75B2F4");
	});	
	
	$(".cancelFoodBtn").bind("click", function(){
		cancelFood(this);
	});
}

function showDescForUpdateOrder(){
	var html = "";
	html = "<div>" +
	"<span style = 'margin-left: 500px;'>菜品数量：" + uoFood.length + "</span>" +
	"<span style = 'margin-left: 50px;'>消费总额：</span>" +	
	"</div>";
	$("#divDescForUpdateOrder").html(html);
}
function selectUOFood(o){
//	$("td").css("backgroundColor", "#87CEEB");
	selectigRow = o.id;
//	$("#" + selectigRow + " td").css("backgroundColor", "#FFA07A");
}
function cancelFood(o){
	var rowId;
	rowId = "truoFood" + o.id.substring(5, o.id.length);
	count = $("#" + rowId).find("td").eq(2).text();
	showKeyboardNumForUO("foodCount");	
//	$("#" + o.id).unbind("click");
//	//绑定取消退菜事件
//	$("#" + o.id).bind("click", function(){
//		cancelForCancelFood(rowId);
//		$("#" + o.id).unbind("click");
//		$("#" + o.id).bind("click", function(){
//			cancelFood(this);
//		});
//	});
}
//取消退菜按钮
function cancelForCancelFood(rowId){
	var cancelIndex;
	cancelIndex = $("#" + rowId).prevAll().length + 1;
	$("#tabForUpdateOrder").find("tr").eq(cancelIndex).remove();
	$("#" + rowId).find("td").eq(7).find("input").val("退菜");
}
function showKeyboardNumForUO(type){
	$("#divHideForUO").show();
	$("#divKeyboardNumForUO").show(100);
	
	typeForKeyNum = type;
	var title = "";
	if(typeForKeyNum == "foodCount"){
		title = "";
	}else if(typeForKeyNum == "peopleCount"){
		title = "请输入人数";
	}
	$("#divTopForKeyboardNumForUO").html("<div style = 'font-size: 20px; " +
			"font-weight: bold; color: #fff; margin: 15px 15px 0 100px;'>" + title + "</div>");
	var htmlReason = '';
//  htmlReason = "<input type = 'text'  id = 'txtreasonForKeyboardNum' class = 'table-number' " +
//			"style = 'margin-left: 17px; background: #FFD700;' readonly='readonly'/>";
	for(x in cancelReasonData){
		htmlReason += "<input type = 'button' value = '" + cancelReasonData[x].reason + "' " +
				"class = 'keyboardbutton reason' onclick = 'getReason(this)'  " +
				"id = 'btnReason" + x + "' " +
				"style = 'margin: 0 0 5px 5px; width: 177px;'/>";
	}
	$("#divReasonForKeyboardNumForUO").html(htmlReason);
	
	//默认选中第一个退菜原因
	selectingReasonId = "btnReason0";
	selectingReasonName = $("#" + selectingReasonId).val();
	$("#" + selectingReasonId).css("backgroundColor", "#F0A00A");
	inputNumId1 = 'txtNumForUO';
	//设置鼠标移到数字键盘上的移进移出效果
	$(".keyboardbutton").mouseover(function(){
		$(this).css("backgroundColor", "#FFD700");
	});
	$(".keyboardbutton").mouseout(function(){
		$(this).css("backgroundColor", "#75B2F4");
	});
	$(".keyboardbutton.reason").unbind("mouseout mouseover");
	//关闭该界面
	$("#btnCloseForKeyboardNumUO").click(function(){
		$("#divKeyboardNumForUO").hide(100);
		$("#divHideForUO").hide();
		inputNumVal1 = "";
		$("#" + inputNumId1).val(inputNumVal1);
	});
	$("#" + inputNumId1).val(count);
	$("#" + inputNumId1).select();
}

//得到选中的退菜原因
function getReason(o){
//	$("#txtreasonForKeyboardNum").val(o.value);
	selectingReasonName = o.value;
//	$(".keyboardbutton.reason").unbind("mouseout mouseover");
	$(".keyboardbutton.reason").css("backgroundColor", "#75B2F4");
    $("#" + o.id).css("backgroundColor", "#F0A00A");
}
function inputNum1(o){
	inputNumVal1 += o.value;
	$("#" + inputNumId1).val(inputNumVal1);
	//判断退菜数目是否合法
	if(typeForKeyNum == "foodCount"){
		if(parseFloat($("#" + inputNumId1).val()) > count){
			alert("退菜数不能超过点菜数！");
			inputNumVal1 = "";
			$("#" + inputNumId1).val(count);
		}
		if(parseFloat($("#" + inputNumId1).val()) < 0){
			alert("退菜数不能小于0！");
			inputNumVal1 = "";
			$("#" + inputNumId1).val(count);
		}
	}
	$("#" + inputNumId1).focus();	
}
//重置数字
$("#btnBackAllForKeyboardNumUO").click(function(){
	inputNumVal1 = "";
	$("#" + inputNumId1).val(inputNumVal1);
	$("#" + inputNumId1).focus();
});
//加一按钮
$("#addOneForKeyboardNumUO").click(function(){
	var inputAddOne = (parseFloat($("#" + inputNumId1).val()) + 1);
	if(inputAddOne >= count){
//		$("#" + inputNumId1).val(count);
		inputNumVal1 = count;
	}else{
//		$("#" + inputNumId1).val(inputAddOne);
		inputNumVal1 = inputAddOne;
	}
	$("#" + inputNumId1).val(inputNumVal1);
	$("#" + inputNumId1).focus();
});
//减一按钮
$("#deleteOneForKeyboardNumUO").click(function(){
	var inputDelOne = (parseFloat($("#" + inputNumId1).val()) - 1);
	if(inputDelOne <= 0){
		inputNumVal1 = 0;
	}else{
		inputNumVal1 = inputDelOne + "";
	}
	$("#" + inputNumId1).val(inputNumVal1);
	$("#" + inputNumId1).focus();
});
//加一按钮1
$("#addOneForKeyboardNumUO1").click(function(){
	if($("#" + inputNumId1).val() == ""){
		inputNumVal1 = 1;
	}else{
		inputNumVal1 = parseInt($("#" + inputNumId1).val()) + 1;
	}
	$("#" + inputNumId1).val(inputNumVal1);
	$("#" + inputNumId1).focus();
});
//减一按钮1
$("#deleteOneForKeyboardNumUO1").click(function(){
	inputNumVal1 = parseInt(inputNumVal1) - 1;
	$("#" + inputNumId1).val(inputNumVal1);
	$("#" + inputNumId1).focus();
});
//重置数字1
$("#btnBackAllForKeyboardNumUO1").click(function(){
	inputNumVal1 = "";
	$("#" + inputNumId1).val(inputNumVal1);
	$("#" + inputNumId1).focus();
});
//确定按钮
$("#btnSubmitForKeyboardNumUO").click(function(){
	var num = $("#" + inputNumId1).val();
	num = parseFloat(num).toFixed(1);
	if(num == 0){
		alert("退菜数目不能为0");
	}else if(num == 'NaN'){
		alert("数字不合规范");
		inputNumVal1 = count;
		$("#" + inputNumId1).val(inputNumVal1);
		$("#" + inputNumId1).focus();
	}else{
		var rowId, htmlcancel = "";
		rowId = selectigRow;
		htmlcancel = "<tr><td style = 'background: #FFA07A'>退</td>" +
				"<td style = 'background: #FFA07A'>" + $("#" + rowId).find("td").eq(1).text() + "</td>" +
				"<td style = 'background: #FFA07A'>-" + num + "</td>" +
				"<td colspan = '7' style = 'background: #FFA07A'> " +
				"退菜原因：" + selectingReasonName + "</td>" + 
						"</tr>";
		
		$("#" + rowId).after(htmlcancel);
//		$("#" + rowId + " td").css("backgroundColor", "#FFA07A");	
		//关闭该界面
		$("#divKeyboardNumForUO").hide(100);
		$("#divHideForUO").hide();
		inputNumVal1 = "";
		$("#" + inputNumId1).val(inputNumVal1);		
		//把退菜按钮改为取消退菜
		var btnReasonToggle;
		btnReasonToggle = $("#" + rowId).find("td").eq(7).find("input"); 
		btnReasonToggle.val("取消退菜");
		btnReasonToggle.unbind("click");
		//绑定取消退菜事件
		btnReasonToggle.bind("click", function(){
			cancelForCancelFood(rowId);
			btnReasonToggle.unbind("click");
			btnReasonToggle.bind("click", function(){
				cancelFood(this);
			});
		});
	}	
});

//确定按钮1
$("#btnSubmitForKeyboardNumUO1").click(function(){
	var num;
	num = $("#" + inputNumId1).val();
	
	//关闭该界面
	$("#divKeyboardPeopleForUO").hide(100);
	$("#divHideForUO").hide();
	inputNumVal1 = "";
	$("#" + inputNumId1).val(inputNumVal1);		
	$("#customNumForUO").html("用餐人数：" + num);		
});



//点击工具栏的人数按钮
function showdivKeyboardPeopleForUO(){
	$("#divHideForUO").show();
	$("#divKeyboardPeopleForUO").show(100);
	var title = "请输入餐桌人数";
	$("#divTopForKeyboardPeopleForUO").html("<div style = 'font-size: 20px; " +
			"font-weight: bold; color: #fff; margin: 15px 15px 0 100px;'>" + title + "</div>");
	inputNumId1 = 'txtPeopleNumForUO';
	inputNumVal1 = $("#customNumForUO").html().substring(5);
	$("#" + inputNumId1).val(inputNumVal1);
	$("#" + inputNumId1).select();
	inputNumVal1 = "";
	//关闭该界面
	$("#btnCloseForKeyboardNumUO1").click(function(){
		$("#divKeyboardPeopleForUO").hide(100);
		$("#divHideForUO").hide();
		inputNumVal1 = "";
		$("#" + inputNumId1).val(inputNumVal1);
	});	
}







function nextRow(){
	var rowId;
	rowId = parseInt(selectigRow.substring(8, selectigRow.length)) + 1;
	if(rowId > uoFood.length){
		rowId = 1;
	}
	selectigRow = "truoFood" + rowId;
//	$("td").css("backgroundColor", "#87CEEB");
//	$("#" + selectigRow + " td").css("backgroundColor", "#FFA07A");
}
function preRow(){
	var rowId;
	rowId = parseInt(selectigRow.substring(8, selectigRow.length)) - 1;
	if(rowId == 0){
		rowId = uoFood.length;
	}
	selectigRow = "truoFood" + rowId;
//	$("td").css("backgroundColor", "#87CEEB");
//	$("#" + selectigRow + " td").css("backgroundColor", "#FFA07A");
}