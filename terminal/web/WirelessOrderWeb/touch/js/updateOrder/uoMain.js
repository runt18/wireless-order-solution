$(function(){
	
});
//存放账单菜品数组
var uoFood = [];
//存放退菜的数组
var uoCancelFoods = [];
//存放菜品相关信息
var uoOther;
//存放行号id
var selectigRow = "";
//存放退菜原因数据
var cancelReasonData = [];
//数字键盘输入框的显示值
var inputNumValUO = "";
//数字键盘输入框id
var inputNumIdUO;
//存放退菜数目
var count;
//选中的退菜原因
//var selectingReasonName = "";
//选中的退菜id
var selectingReasonId = "";
var selectingCancelReason;
/**
 * 显示已点菜界面函数
 * @param {object} c  
 */
uo.show = function(c){
	toggleContentDisplay({
		type: 'show', 
		renderTo: 'divUpdateOrder'
	});
	uo.table = c.table;
	initOrderData(c.table);
	initCancelReason();	
};

/**
 * 取得初始的消费总额
 * @returns {number} totalPriceUO
 */
function getTotalPriceUO(){
	var totalPriceUO = 0;
	for(x in uoFood){
		totalPriceUO += uoFood[x].count * uoFood[x].actualPrice;
	}
	return totalPriceUO;
}

/**
 * 设置所选择的行号id
 * @param {object} o 调用该函数的标签对象（tr标签）
 */
function selectUOFood(o){
	selectigRow = o.id;
}

/**
 * 点击退菜时，取得所在行的菜品信息(行号id，菜名，口味,菜数目）并弹出退菜信息框
 * @param {object} o 调用该方法的标签对象（input标签）
 */
function cancelFood(o){
	var rowId, foodName, dishes;
	rowId = "truoFood" + o.id.substring(5, o.id.length);
	foodName = $("#" + rowId).find("td").eq(1).text();
	dishes = $("#" + rowId).find("td").eq(3).text();
	count = $("#" + rowId).find("td").eq(2).text();
	//弹出退菜信息框
	showKeyboardNumForUO(foodName, dishes);	
}

/**
 * 退菜信息框
 * @param {string} foodName 菜名
 * @param {string} dishes 口味
 */
function showKeyboardNumForUO(foodName, dishes){
	$("#divHideForUO").show();
	$("#divKeyboardNumForUO").show(100);	
	var title = "";
	title = foodName + "(" + dishes + ")";
	//初始化标题信息
	$("#divTopForKeyboardNumForUO").html("<div style = 'font-size: 20px; " +
			"font-weight: bold; color: #fff; " +
			"margin: 15px 15px 0 100px;'>" + title + "</div>");
	//初始化退菜原因信息
	var htmlReason = '';
	for(x in cancelReasonData){
		htmlReason += "<input type = 'button' " +
				"value = '" + cancelReasonData[x].reason + "' " +
				"class = 'keyboardbutton reason' " +
				"onclick = 'setReason(this)' " +
				"id = 'btnReason" + cancelReasonData[x].id + "' " +
				"style = 'margin: 0 0 5px 5px; width: 177px;'/>";
	}
	$("#divReasonForKeyboardNumForUO").html(htmlReason);
	if(cancelReasonData.length > 0){
		//默认选中第一个退菜原因
		selectingReasonId = "btnReason" + cancelReasonData[0].id;
		selectingCancelReason = cancelReasonData[0];
//		selectingReasonName = $("#" + selectingReasonId).val();
		$("#" + selectingReasonId).css("backgroundColor", "#F0A00A");
	}
	
	//设定输入框id
	inputNumIdUO = 'txtNumForUO';
	
	//设置鼠标移到数字键盘上的移进移出效果
	$(".keyboardbutton").mouseover(function(){
		$(this).css("backgroundColor", "#FFD700");
	});
	$(".keyboardbutton").mouseout(function(){
		$(this).css("backgroundColor", "#75B2F4");
	});
	
	//取消退菜原因的鼠标效果
	$(".keyboardbutton.reason").unbind("mouseout mouseover");
	
	//设定输入框的初始值和选中状态
	$("#" + inputNumIdUO).val(count);
	$("#" + inputNumIdUO).select();
	
	//点击取消界面按钮
	$("#btnCloseForKeyboardNumUO").click(function(){
		$("#divKeyboardNumForUO").hide(100);
		$("#divHideForUO").hide();
		inputNumValUO = "";
		$("#" + inputNumIdUO).val(inputNumValUO);
	});	
}

/**
 * 选定退菜原因
 * @param {object} o 调用该函数的标签对象(input标签)
 */
function setReason(o){
	//为全局变量（退菜原因）赋值
	var reasonId = o.id.substring(9);
	for(x in cancelReasonData){
		if(cancelReasonData[x].id == reasonId){
			selectingCancelReason = cancelReasonData[x];
			break;
		}
	}
	//设置选中状态的背景色
	$(".keyboardbutton.reason").css("backgroundColor", "#75B2F4");
    $("#" + o.id).css("backgroundColor", "#F0A00A");
}

/**
 * 退菜信息框的确定按钮,确定退菜,
 * 并向退菜数组添加退菜对象
 */
$("#btnSubmitForKeyboardNumUO").click(function(){
	//取得退菜数目并进行判定
	var num = $("#" + inputNumIdUO).val();
	num = parseFloat(num).toFixed(2);
	if(num == 0){
		alert("退菜数目不能为0或太小");
		inputNumValUO = "";
		$("#" + inputNumIdUO).val(count);
		$("#" + inputNumIdUO).select();
	}else if(num == 'NaN'){
		alert("数字不合规范");
		inputNumValUO = "";
		$("#" + inputNumIdUO).val(count);
		$("#" + inputNumIdUO).select();
	}else{
		//退菜信息
		var uoCancelFood = {
				id : 0,
				foodName : "" ,
				dishes : "" ,
				count : 0 ,
				reason : "" ,
				actualPrice : "",
				totalPrice : "",
			};
		var rowId, htmlcancel = "", foodName, actualPrice,  totalPrice;
		rowId = selectigRow;
		foodName = $("#" + rowId).find("td").eq(1).text();
		actualPrice = $("#" + rowId).find("td").eq(4).text();
		totalPrice = actualPrice * (-num);
		htmlcancel = "<tr><td style = 'background: #FFA07A'>退</td>" +
				"<td style = 'background: #FFA07A'>" + foodName + "</td>" +
				"<td style = 'background: #FFA07A'>" + (-num).toFixed(2) + "</td>" +
				"<td colspan = '7' style = 'background: #FFA07A'> " +
				"退菜原因：" + selectingCancelReason.reason + "</td>" + 
						"</tr>";
		
		$("#" + rowId).after(htmlcancel);
		//把相关数据加到退菜信息对象
		uoCancelFood.id = document.getElementById(rowId).getAttribute("data-value");
		uoCancelFood.foodName = foodName;
		uoCancelFood.dishes = $("#" + rowId).find("td").eq(3).text();
		uoCancelFood.count = -num;
		uoCancelFood.reason = selectingCancelReason;
		uoCancelFood.actualPrice = actualPrice;
		uoCancelFood.totalPrice = totalPrice;
//		alert(uoCancelFood.reason.id);

		//加到退菜数组
		uoCancelFoods.push(uoCancelFood);
		//更改消费总额
		var totalPrice = getTotalPriceUO();
		for(x in uoCancelFoods){
			totalPrice += uoCancelFoods[x].totalPrice;
		}
		$("#spanTotalPriceUO").html(totalPrice.toFixed(2) + "元");
		//关闭该界面
		$("#divKeyboardNumForUO").hide(100);
		$("#divHideForUO").hide();
		inputNumValUO = "";
		$("#" + inputNumIdUO).val(inputNumValUO);
		
		//把按钮值由退菜改为取消退菜
		var btnReasonToggle;
		btnReasonToggle = $("#" + rowId).find("td").eq(7).find("input"); 
		btnReasonToggle.val("取消退菜");
		
		//移除退菜事件绑定
		btnReasonToggle.unbind("click");
		
		//绑定取消退菜事件
		btnReasonToggle.bind("click", function(){
			//调用取消退菜函数
			cancelForCancelFood(rowId);
			
			//移除取消退菜事件
			btnReasonToggle.unbind("click");
			//把按钮的值由取消退菜改为退菜
			btnReasonToggle.val("退菜");
			//绑定退菜事件
			btnReasonToggle.bind("click", function(){
				//调用退菜函数
				cancelFood(this);
			});
		});
		
	};	
});

/**
 * 取消退菜
 * @param {string} rowId 所在行号id
 */
function cancelForCancelFood(rowId){
	var cancelIndex, dishes;
	var foodId = document.getElementById(rowId).getAttribute("data-value");
	dishes = $("#" + rowId).find("td").eq(3).text();
	//退菜行号，移除表格的退菜行
	cancelIndex = $("#" + rowId).prevAll().length + 1;
	$("#tabForUpdateOrder").find("tr").eq(cancelIndex).remove();
	//从退菜数组中删掉被取消的退菜对象
	for(x in uoCancelFoods){
		if(uoCancelFoods[x].id == foodId && uoCancelFoods[x].dishes == dishes){
			uoCancelFoods.splice(x, 1);
			break;
		}
	}
	//修改退菜总额
	var totalPrice = getTotalPriceUO();
	for(x in uoCancelFoods){
		totalPrice += uoCancelFoods[x].totalPrice;
	}
	$("#spanTotalPriceUO").html(totalPrice.toFixed(2) + "元");
}

/**
 * 餐桌人数修改框
 */
function showdivKeyboardPeopleForUO(){
	//弹出人数输入框
	$("#divHideForUO").show();
	$("#divKeyboardPeopleForUO").show(100);
	var title = "请输入餐桌人数";
	$("#divTopForKeyboardPeopleForUO").html("<div style = 'font-size: 20px; " +
			"font-weight: bold; color: #fff; " +
			"margin: 15px 15px 0 100px;'>" + title + "</div>");
	//设定输入框的id和初始显示信息
	inputNumIdUO = 'txtPeopleNumForUO';
	inputNumValUO = $("#customNumForUO").html().substring(5);
	$("#" + inputNumIdUO).val(inputNumValUO);
	$("#" + inputNumIdUO).select();
	inputNumValUO = "";
	
	//取消按钮
	$("#btnCloseForPeopleKeyboardUO").click(function(){
		$("#divKeyboardPeopleForUO").hide(100);
		$("#divHideForUO").hide();
		inputNumValUO = "";
		$("#" + inputNumIdUO).val(inputNumValUO);
	});	
}

//确定修改餐桌人数
$("#btnSubmitForPeopleKeyboardUO").click(function(){
	var num;
	num = parseInt($("#" + inputNumIdUO).val());
	//关闭该界面
	$("#divKeyboardPeopleForUO").hide(100);
	$("#divHideForUO").hide();
	//清空输入框的显示信息
	inputNumValUO = "";
	$("#" + inputNumIdUO).val(inputNumValUO);
	//更改页面端的的人数
	$("#customNumForUO").html("用餐人数：" + num);			
});

/**
 * 工具栏的确定按钮,对整个页面信息提交
 */
function sureForUO(){	
	for(x in uoFood){
		for(y in uoCancelFoods){
			if(uoFood[x].id == uoCancelFoods[y].id && uoFood[x].tasteGroup.tastePref == uoCancelFoods[y].dishes){
				uoFood[x].count = (uoFood[x].count + uoCancelFoods[y].count).toFixed(2);
				uoFood[x].cancelReason = uoCancelFoods[y].reason.id;
			}
		}
	}
	uoCancelFoods = [];
	uo.updateOrder = uoFood;
	uo.customNum = $("#customNumForUO").html().substring(5);	
	//对更新的菜品和人数进行提交
	submitUpdateOrderHandler(uoFood);
	
	
}

/**
 * 工具栏的取消按钮,取消对该页面的修改操作
 */
function cancelForUO(){	
	uoCancelFoods = [];
	uoFood = [];
	toggleContentDisplay({type:'hide', renderTo:'divUpdateOrder'});
}

/**
 * 下翻按钮
 */
function nextRow(){
	var scrollTop;
	scrollTop = $("#divCenterForUpdateOrder").scrollTop() + 50;
	$("#divCenterForUpdateOrder").scrollTop(scrollTop);
}

/**
 * 上翻按钮
 */
function preRow(){
	var scrollTop;
	scrollTop = $("#divCenterForUpdateOrder").scrollTop() - 50;
	$("#divCenterForUpdateOrder").scrollTop(scrollTop);
};

/**
 * 数字键点击事件
 * @param {object} o 触发该函数的按钮对象 
 */
function inputNumUO(o){
	//设置输入框的显示值（原有值加上输入值）
	inputNumValUO += o.value;
	$("#" + inputNumIdUO).val(inputNumValUO);
	$("#" + inputNumIdUO).focus();
	
	//判断退菜数目是否合法
	if(inputNumIdUO == "txtNumForUO"){
		if(parseFloat($("#" + inputNumIdUO).val()) > count){
			alert("退菜数不能超过点菜数！");
			inputNumValUO = "";
			$("#" + inputNumIdUO).val(count);
		}
		if(parseFloat($("#" + inputNumIdUO).val()) < 0){
			alert("退菜数不能小于0！");
			inputNumValUO = "";
			$("#" + inputNumIdUO).val(count);
		}
	}
	
	//判断输入的人数是否合法
	if(inputNumIdUO == "txtPeopleNumForUO"){
		if(parseInt($("#" + inputNumIdUO).val()) > 255){
			alert("输入的人数不得超过255！");
			inputNumValUO = "";
			$("#" + inputNumIdUO).val(uoOther.order.customNum);
		}
		if(parseFloat($("#" + inputNumIdUO).val()) == 0){
			alert("人数值不能为0！");
			inputNumValUO = "";
			$("#" + inputNumIdUO).val(uoOther.order.customNum);
		}
	}
}

//重置数字
$(".btnBackAllForKeyboardNumUO").click(function(){
	inputNumValUO = "";
	$("#" + inputNumIdUO).val(inputNumValUO);
	$("#" + inputNumIdUO).focus();
});

//加一按钮
$(".addOneForKeyboardNumUO").click(function(){
	if($("#" + inputNumIdUO).val() == ""){
		$("#" + inputNumIdUO).val(0);
	}
	var inputAddOne = parseFloat($("#" + inputNumIdUO).val()) + 1;
	//退菜输入框
	if(inputNumIdUO == "txtNumForUO"){
		if(inputAddOne > count){
			inputNumValUO = count;
		}else{
			inputNumValUO = inputAddOne;
		}
	}
	//人数输入框
	if(inputNumIdUO == "txtPeopleNumForUO"){
		if(inputAddOne > 255){
			inputNumValUO = 255;
		}else{
			inputNumValUO = inputAddOne;
		}
	}
	
	$("#" + inputNumIdUO).val(inputNumValUO);
	$("#" + inputNumIdUO).focus();
});

//减一按钮
$(".deleteOneForKeyboardNumUO").click(function(){
	if($("#" + inputNumIdUO).val() == ""){
		$("#" + inputNumIdUO).val(0);
	}
	var inputDelOne = parseFloat($("#" + inputNumIdUO).val()) - 1;
	if(inputDelOne <= 0){
		inputNumValUO = 0;
	}else{
		inputNumValUO = inputDelOne + "";
	}
	$("#" + inputNumIdUO).val(inputNumValUO);
	$("#" + inputNumIdUO).focus();
});

/**
 * 已点菜账单提交操作
 */
function submitUpdateOrderHandler(c){
	var orderFoods = c;
	if(orderFoods.length > 0){
		var foodPara = '';
		for ( var i = 0; i < orderFoods.length; i++) {
			foodPara += ( i > 0 ? '<<sh>>' : '');
			if (orderFoods[i].isTemporary) {
				// 临时菜
				alert("临时菜");
			}else{
				// 普通菜
				var normalTaste = '', tmpTaste = '' , tasteGroup = orderFoods[i].tasteGroup;
				for(var j = 0; j < tasteGroup.normalTasteContent.length; j++){
					var t = tasteGroup.normalTasteContent[j];
					normalTaste += ((j > 0 ? '<<stnt>>' : '') + (t.id + '<<stb>>' + t.alias + '<<stb>>' + t.cateValue));
				}
				if(tasteGroup.tmpTaste != null && typeof tasteGroup.tmpTaste != 'undefined'){
					if(eval(tasteGroup.tmpTaste.id >= 0))
						tmpTaste = tasteGroup.tmpTaste.price + '<<sttt>>' + tasteGroup.tmpTaste.name  + '<<sttt>>' + tasteGroup.tmpTaste.id+ '<<sttt>>' + tasteGroup.tmpTaste.alias; 				
				}
//				var reason = "";
//				reason = orderFoods[i].cancelReason == null ? null : orderFoods[i].cancelReason;
				foodPara = foodPara 
						+ '['
						+ 'false' + '<<sb>>' // 是否临时菜(false)
						+ orderFoods[i].alias + '<<sb>>' // 菜品1编号
						+ orderFoods[i].count + '<<sb>>' // 菜品1数量
						+ (normalTaste + ' <<st>> ' + tmpTaste) + '<<sb>>'
						+ orderFoods[i].kitchen.alias + '<<sb>>'// 厨房1编号
						+ orderFoods[i].discount + '<<sb>>' // 菜品1折扣
						+ orderFoods[i].isHangup + '<<sb>>'//是否叫起
						+ orderFoods[i].cancelReason //退菜原因
						+ ']';
				
			}
		}	
		
		foodPara = '{' + foodPara + '}';	
		var type = 2;
		$.ajax({
			url : '../InsertOrder.do',
			type : 'post',
			data : {
				pin : pin,
				tableID : uo.table.alias,
				orderID : uoOther.order.id,
				customNum : uo.customNum,
				type : type,
				foods : foodPara,
				category : uoOther.order.categoryValue,
				orderDate : uoOther.order.orderDate
			},
			success : function(data, status, xhr){
				toggleContentDisplay({type:'hide', renderTo:'divUpdateOrder'});
			},
			error : function(request, status, err){
				alert('ERR.');
			}
		});
	}else if(orderFoods.length == 0){
		alert("没有任何菜品，不能提交！");
	}
}
