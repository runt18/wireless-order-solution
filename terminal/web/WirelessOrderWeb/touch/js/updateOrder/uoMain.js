$(function(){
	
});
//菜品数组
var uoFood = [];

var uoCancelFoods = [];
var uoOther;
var selectigRow = "";
var typeForKeyNum;
var cancelReasonData = [];
var inputNumVal1 = "";
var inputNumId1;
var count;
var selectingReasonName = "";
var selectingReasonId = "";
function initOrderData(data){
	// 加载菜单数据
	$.ajax({
		url : '../QueryOrder.do',
		type : 'post',
		data : {
			pin : pin,
			restaurantID : restaurantID,
			tableID : data.alias,			
		},
		success : function(data, status, xhr){
			uoFood = [];
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
//计算消费总额
function getTotalPriceUO(){
	var totalPriceUO = 0;
	for(x in uoFood){
		totalPriceUO += uoFood[x].count * uoFood[x].actualPrice;
	}
	return totalPriceUO;
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
			cancelReasonData = [];
			data = JSON.parse(data);
			for(x in data.root){
				cancelReasonData.push(data.root[x]);
			}
		},
		error : function(request, status, err){
			alert('初始化退菜原因失败.');
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
	var customNum = uoOther.order.customNum;
	html = "<div>" +
			"<span style ='margin: 10px;'>账单号：" + uoOther.order.id + " </span>" +
			"<span style ='margin: 10px;'>餐台号：" + uoOther.order.table.alias + " </span>" +
			"<span style ='margin: 10px;'>餐台名： " + tableName + "</span>" +
			"<span style ='margin: 10px;' id = 'customNumForUO'>用餐人数：" + customNum + " </span>" +			
		"</div>";
	$("#divNorthForUpdateOrder").html(html);
}
function showOrder(){
	var html = "<tr>" +
				"<th style = 'width: 4%'></th>" +
				"<th style = 'width: 24%'>菜名</th>" +
				"<th style = 'width: 6%'>数量</th>" +
				"<th style = 'width: 22%'>口味</th>" +
				"<th style = 'width: 6%'>单价</th>" +
				"<th style = 'width: 6%'>总价</th>" +
				"<th style = 'width: 10%'>时间</th>" +
				"<th>操作</th>" +
				"<th>服务员</th>" +
			"</tr>";
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
	$("#divCenterForUpdateOrder table").html(html);
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
	"<span style = 'margin-left: 50px;'>消费总额：</span>" + "<span id = 'spanTotalPriceUO'>" + "</span>" +	
	"</div>";
	$("#divDescForUpdateOrder").html(html);
	$("#spanTotalPriceUO").html(getTotalPriceUO() + "元");
}
function selectUOFood(o){
//	$("td").css("backgroundColor", "#87CEEB");
	selectigRow = o.id;
//	$("#" + selectigRow + " td").css("backgroundColor", "#FFA07A");
}
function cancelFood(o){
	var rowId, foodName, dishes;
	rowId = "truoFood" + o.id.substring(5, o.id.length);
	count = $("#" + rowId).find("td").eq(2).text();
	foodName = $("#" + rowId).find("td").eq(1).text();
	dishes = $("#" + rowId).find("td").eq(3).text();
	showKeyboardNumForUO("foodCount", foodName, dishes);	
}
//取消退菜按钮
function cancelForCancelFood(rowId){
	var cancelIndex, foodName, dishes;
	foodName = $("#" + rowId).find("td").eq(1).text();
	dishes = $("#" + rowId).find("td").eq(3).text();
	cancelIndex = $("#" + rowId).prevAll().length + 1;
	$("#tabForUpdateOrder").find("tr").eq(cancelIndex).remove();
	for(x in uoCancelFoods){
		if(uoCancelFoods[x].foodName == foodName && uoCancelFoods[x].dishes == dishes){
			uoCancelFoods.splice(x, 1);
			break;
		}
	}
	var totalPrice = getTotalPriceUO();
	for(x in uoCancelFoods){
		totalPrice += uoCancelFoods[x].totalPrice;
	}
	$("#spanTotalPriceUO").html(totalPrice + "元");
	$("#" + rowId).find("td").eq(7).find("input").val("退菜");
}
function showKeyboardNumForUO(type, foodName, dishes){
	$("#divHideForUO").show();
	$("#divKeyboardNumForUO").show(100);
	
	typeForKeyNum = type;
	var title = "";
	if(typeForKeyNum == "foodCount"){
		title = foodName + "(" + dishes + ")";
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
	selectingReasonName = o.value;
	$(".keyboardbutton.reason").css("backgroundColor", "#75B2F4");
    $("#" + o.id).css("backgroundColor", "#F0A00A");
}
function inputNum1(o){
	inputNumVal1 += o.value;
	$("#" + inputNumId1).val(inputNumVal1);
	//判断退菜数目是否合法
	if(inputNumId1 == "txtNumForUO"){
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
	if($("#" + inputNumId1).val() == "" || parseInt($("#" + inputNumId1).val()) <= 0){
		inputNumVal1 = 0;
	}else{
		inputNumVal1 = parseInt($("#" + inputNumId1).val()) - 1;
	}
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
		alert("退菜数目不能为0或太小");
	}else if(num == 'NaN'){
		alert("数字不合规范");
		inputNumVal1 = count;
		$("#" + inputNumId1).val(inputNumVal1);
		$("#" + inputNumId1).focus();
	}else{
		var uoCancelFood = {
				foodName : "" ,
				dishes : "" ,
				count : 0 ,
				reason : "" ,
				actualPrice : "",
				totalPrice : "",
			};
		var rowId, htmlcancel = "", foodName, totalPrice, actualPrice;
		rowId = selectigRow;
		foodName = $("#" + rowId).find("td").eq(1).text();
		actualPrice = $("#" + rowId).find("td").eq(4).text();
		totalPrice = actualPrice * (-num);
		htmlcancel = "<tr><td style = 'background: #FFA07A'>退</td>" +
				"<td style = 'background: #FFA07A'>" + foodName + "</td>" +
				"<td style = 'background: #FFA07A'>" + (-num) + "</td>" +
				"<td colspan = '7' style = 'background: #FFA07A'> " +
				"退菜原因：" + selectingReasonName + "</td>" + 
						"</tr>";
		
		$("#" + rowId).after(htmlcancel);
		//把相关数据加到退菜信息对象
		uoCancelFood.foodName = foodName;
		uoCancelFood.dishes = $("#" + rowId).find("td").eq(3).text();
		uoCancelFood.count = -num;
		uoCancelFood.reason = selectingReasonName;
		uoCancelFood.actualPrice = actualPrice;
		uoCancelFood.totalPrice = totalPrice;
		uoCancelFoods.push(uoCancelFood);
		//更改消费总额
		var totalPrice = getTotalPriceUO();
		for(x in uoCancelFoods){
			totalPrice += uoCancelFoods[x].totalPrice;
		}
		$("#spanTotalPriceUO").html(totalPrice + "元");
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
	};	
});

//确定按钮1
$("#btnSubmitForKeyboardNumUO1").click(function(){
	var num;
	num = parseInt($("#" + inputNumId1).val());
	if(num > 999){
		alert("人数不能超过999人");
		inputNumVal1 = "";
		$("#" + inputNumId1).val(inputNumVal1);
	}else{
		//关闭该界面
		$("#divKeyboardPeopleForUO").hide(100);
		$("#divHideForUO").hide();
		inputNumVal1 = "";
		$("#" + inputNumId1).val(inputNumVal1);		
		$("#customNumForUO").html("用餐人数：" + num);
	};
			
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
//点击工具栏的确定按钮
function sureForUO(){	
	for(x in uoFood){
		for(y in uoCancelFoods){
			if(uoFood[x].name == uoCancelFoods[y].foodName && uoFood[x].tasteGroup.tastePref){
				uoFood[x].count = (uoFood[x].count + uoCancelFoods[y].count).toFixed(2);
				uoFood[x].cancelReason = uoCancelFoods[y].reason;
			}
		}
	}
	uo.updateOrder = uoFood;
	for(x in updateOrder){
		
	}
//	alert(JSON.stringify(uo.updateOrder));
//	alert(uoFood[1].count);
	toggleContentDisplay({type:'hide', renderTo:'divUpdateOrder'});
}

//点击工具栏的取消按钮
function cancelForUO(){
//	showNorthForUpdateOrder();
//	showOrder();	
	toggleContentDisplay({type:'hide', renderTo:'divUpdateOrder'});
}

uo.show = function(c){
	toggleContentDisplay({
		type:'show', 
		renderTo:'divUpdateOrder'
	});
	initOrderData(c.table);
	initCancelReason();
	
//	alert(c.table.alias);
};

function nextRow(){
	var scrollTop;
	scrollTop = $("#divCenterForUpdateOrder").scrollTop() + 50;
	$("#divCenterForUpdateOrder").scrollTop(scrollTop);
}
function preRow(){
	var scrollTop;
	scrollTop = $("#divCenterForUpdateOrder").scrollTop() - 50;
	$("#divCenterForUpdateOrder").scrollTop(scrollTop);
};