/**
 * 显示已点菜界面函数
 * @param {object} c  
 */
uo.show = function(c){
	if(c.type == null || typeof c.type == 'undefined'){
		Util.toggleContentDisplay({
			type: 'show', 
			renderTo: 'divUpdateOrder'
		});
		initCancelReason();	
	}
	initOrderData({table : c.table});
	uo.table = c.table;
//	uo.updateTable = c.table;
};

uo.updateOrderHandler = function(){
	initTableData();
	uo.show({
		table : uo.table
	});
};

/**
 * 取得初始的消费总额
 * @returns {number} totalPriceUO
 */
uo.getTotalPriceUO = function(){
	var totalPriceUO = 0;
	for(x in uoFood){
		totalPriceUO += uoFood[x].count * (uoFood[x].actualPrice + uoFood[x].tasteGroup.tastePrice) * uoFood[x].discount;
	}
	return totalPriceUO;
};

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
	Util.dialongDisplay({
		type : 'show',
		renderTo : 'divKeyboardNumForUO'
	});
	var title = "";
	title = "<div>" + foodName + "(" + dishes + ")</div>" ;
	
	//初始化标题信息
	$("#divTopForKeyboardNumForUO").html("<div style = 'font-size: 20px; " +
			"font-weight: bold; color : white;" +
			"text-align : center; line-height : 40px;'>" + title + "</div>");
	
	if(cancelReasonData.length > 0){
		//初始化退菜原因信息
		var htmlReason = '';
		for(var i = 0; i < cancelReasonData.length; i++){
			htmlReason += "<div class = 'button-base reason' onclick = 'setReason(this)'" +
			"id = 'btnReason" + cancelReasonData[i].id + "' " +
			"style = 'margin: 0 0 5px 8px; height: 66px; width: 165px;'>" +
			cancelReasonData[i].reason + "</div>";
		}
		$("#divReasonForKeyboardNumForUO").html(htmlReason);
		
		//默认选中第一个退菜原因
		selectingReasonId = "btnReason" + cancelReasonData[0].id;
		selectingCancelReason = cancelReasonData[0];
		$("#" + selectingReasonId).css("backgroundColor", "#F0A00A");
	}
	
	//设定输入框id
	inputNumIdUO = 'txtNumForUO';
	
	//设定输入框的初始值和选中状态
	$("#" + inputNumIdUO).val(count);
	$("#" + inputNumIdUO).select();
	
	uo.selectAll = true;
	//点击取消界面按钮
	$("#btnCloseForKeyboardNumUO").click(function(){
		Util.dialongDisplay({
			type : 'hide',
			renderTo : 'divKeyboardNumForUO'
		});
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
	$(".button-base.reason").css("backgroundColor", "#4EEE99");
    $("#" + o.id).css("backgroundColor", "#F0A00A");
}

/**
 * 退菜信息框的确定按钮,确定退菜,
 * 并向退菜数组添加退菜对象
 */
uo.cf.save = function(){
	//取得退菜数目并进行判定
	var num = $("#" + inputNumIdUO).val();
	num = parseFloat(num).toFixed(2);
	if(num == 0){
		Util.msg.alert({
			title : '温馨提示',
			msg : '退菜数目不能为0或太小.', 
			time : 2,
			fn : function(btn){
				inputNumValUO = "";
				$("#" + inputNumIdUO).val(count);
				$("#" + inputNumIdUO).select();
			}
		});
	}else if(num == 'NaN'){
		Util.msg.alert({
			title : '温馨提示',
			msg : '数字不合规范.', 
			time : 2
		});
		inputNumValUO = "";
		$("#" + inputNumIdUO).val(count);
		$("#" + inputNumIdUO).select();
	}else{
		//退菜信息
		var uoCancelFood = {
				alias : 0,
				foodName : "" ,
				dishes : "" ,
				count : 0 ,
				reason : "" ,
				actualPrice : "",
				totalPrice : ""
			};
		var rowId, htmlcancel = "", foodName, actualPrice,  totalPrice;
		rowId = selectigRow;
		foodName = $("#" + rowId).find("td").eq(1).text();
		actualPrice = $("#" + rowId).find("td").eq(4).text();
		totalPrice = actualPrice * (-num);
		
/*		加上退菜信息
 * 		htmlcancel = "<tr><td style = 'background: #FFA07A'>退</td>" +
				"<td style = 'background: #FFA07A'>" + foodName + "</td>" +
				"<td style = 'background: #FFA07A'>" + (-num).toFixed(2) + "</td>" +
				"<td colspan = '7' style = 'background: #FFA07A'> " +
				"退菜原因：" + selectingCancelReason.reason + "</td>" + 
						"</tr>";
		
		$("#" + rowId).after(htmlcancel);*/
		//把相关数据加到退菜信息对象
		uoCancelFood.id = document.getElementById(rowId).getAttribute("data-value");
		uoCancelFood.foodName = foodName;
		uoCancelFood.dishes = $("#" + rowId).find("td").eq(3).text();
		uoCancelFood.count = -num;
		uoCancelFood.reason = selectingCancelReason;
		uoCancelFood.actualPrice = actualPrice;
		uoCancelFood.totalPrice = totalPrice;
		//加到退菜数组
		uoCancelFoods.push(uoCancelFood);
		//更改消费总额
		var totalPrice = uo.getTotalPriceUO();
		for(x in uoCancelFoods){
			totalPrice += uoCancelFoods[x].totalPrice;
		}
		$("#spanTotalPriceUO").html(totalPrice.toFixed(2) + "元");
		//关闭该界面
		Util.dialongDisplay({
			type : 'hide',
			renderTo : 'divKeyboardNumForUO'
		});
		inputNumValUO = "";
		$("#" + inputNumIdUO).val(inputNumValUO);
		uo.canceling = true;
		uo.saveForUO();
/*		
		//把按钮值由退菜改为取消退菜
		var btnReasonToggle;
		btnReasonToggle = $("#" + rowId).find("td").eq(7).find("div"); 
		btnReasonToggle.html("取消退菜");
		//移除退菜事件绑定
		btnReasonToggle.unbind("click");
		//绑定取消退菜事件
		btnReasonToggle.bind("click", function(){
			//调用取消退菜函数
			cancelForCancelFood(rowId);
			//移除取消退菜事件
			btnReasonToggle.unbind("click");
			//把按钮的值由取消退菜改为退菜
			btnReasonToggle.html("退菜");
			//绑定退菜事件
			btnReasonToggle.bind("click", function(){
				//调用退菜函数
				cancelFood(this);
			});
		});
		*/
	}
};

function transFoodForTS(o){
	$('#divTransFoodNumber').show();
	$('#divTransFoodTableAlias > span').removeClass('select-food-label');
	$('#divTransFoodTableAlias > span').addClass('trans-food-label');
	ts.commitTableOrTran = 'trans';
	$("#txtTableNumForTS").val("");
	$('#divSelectTablesForTs').html("");	
	var rowId, foodName;
	rowId = "truoFood" + o.id.substring(8, o.id.length);
	foodName = $("#" + rowId).find("td").eq(1).text();
	ts.tf.id = document.getElementById(rowId).getAttribute("data-value");
	ts.tf.count = $("#" + rowId).find("td").eq(2).text();	
	$('#txtFoodNumForTran').val(checkDot(ts.tf.count)?ts.tf.count : parseInt(ts.tf.count));
	showSelectTableNumTS();
	var title = "";
	title = foodName +" -- 请输入桌号，菜品数量确定转菜";
	$("#divTopForSelectTableNumTS").html("<div style = 'font-size: 15px; " +
			"font-weight: bold; color: #fff; margin: 15px;'>" + title + "</div>");	
	
	inputNumIdUO  = "txtTableNumForTS";
			
	$('#txtFoodNumForTran').click(function(){
		inputNumIdUO  = "txtFoodNumForTran";
	});		
	
	$('#txtTableNumForTS').click(function(){
		inputNumIdUO  = "txtTableNumForTS";
	});		
}

function allTransFoodForTS(){
	$('#divTransFoodNumber').hide();
	$('#divTransFoodTableAlias > span').removeClass('trans-food-label');
	$('#divTransFoodTableAlias > span').addClass('select-food-label');
	ts.commitTableOrTran = 'allTrans';
	$("#txtTableNumForTS").val("");
	$('#divSelectTablesForTs').html("");	
	showSelectTableNumTS();
	var title = "";
	title = "请输入桌号，确定全部转菜";
	$("#divTopForSelectTableNumTS").html("<div style = 'font-size: 20px; " +
			"font-weight: bold; color: #fff; margin: 15px;'>" + title + "</div>");		
}

function uo_transTableForTS(){
	$('#divTransFoodNumber').hide();
	$('#divTransFoodTableAlias > span').removeClass('trans-food-label');
	$('#divTransFoodTableAlias > span').addClass('select-food-label');
	ts.commitTableOrTran = 'transTable';
	$("#txtTableNumForTS").val("");
	$('#divSelectTablesForTs').html("");	
	showSelectTableNumTS();
	var title = "";
	title = "请输入桌号，确定转台";
	$("#divTopForSelectTableNumTS").html("<div style = 'font-size: 20px; " +
			"font-weight: bold; color: #fff; margin: 15px;'>" + title + "</div>");		
}

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
	var totalPrice = uo.getTotalPriceUO();
	for(x in uoCancelFoods){
		totalPrice += uoCancelFoods[x].totalPrice;
	}
	$("#spanTotalPriceUO").html(totalPrice.toFixed(2) + "元");
}

/**
 * 餐桌人数修改框
 */
uo.showdivKeyboardPeopleForUO = function(c){
	if(c.type == "setFood"){
		//弹出菜品数量输入框
		Util.dialongDisplay({
			type : 'show',
			renderTo : 'divKeyboardPeopleForUO'
		});
		$("#divRightForKeyboardPeopleForUO > div[class*=isDot]").html(".");
		var title = "请输入菜品数量";
		$("#divTopForKeyboardPeopleForUO").html("<div style = 'font-size: 20px; " +
				"font-weight: bold; color: #fff; " +
				"margin: 15px 15px 0 100px;'>" + title + "</div>");
		//设定输入框的id和初始显示信息
		inputNumIdUO = 'txtPeopleNumForUO';
		inputNumValUO = 1;
		
		$("#" + inputNumIdUO).val(inputNumValUO);
		$("#" + inputNumIdUO).select();
		
		uo.selectAll = true;
		inputNumValUO = "";
		//取消按钮
		$("#btnCloseForPeopleKeyboardUO").click(function(){
			Util.dialongDisplay({
				type : 'hide',
				renderTo : 'divKeyboardPeopleForUO'
			});
			$("#divLeftForKeyboardPeopleForUO > div[class*=isSave]").unbind("click");
			inputNumValUO = "";
			$("#" + inputNumIdUO).val(inputNumValUO);
		});	
		$("#divLeftForKeyboardPeopleForUO > div[class*=isSave]").bind("click", function(){
			co.saveForSetFood({data : c.data});
		});
	}else if(c.type == "setCountForPeople"){
		//弹出人数输入框
		Util.dialongDisplay({
			type : 'show',
			renderTo : 'divKeyboardPeopleForUO'
		});
		var title = "请输入餐桌人数";
		$("#divTopForKeyboardPeopleForUO").html("<div style = 'font-size: 20px; " +
				"font-weight: bold; color: #fff; " +
				"margin: 15px 15px 0 100px;'>" + title + "</div>");
		//设定输入框的id和初始显示信息
		inputNumIdUO = 'txtPeopleNumForUO';
		inputNumValUO = $("#customNumForUO").html().substring(5);
		uo.selectAll = true;
		$("#" + inputNumIdUO).val(inputNumValUO);
		$("#" + inputNumIdUO).select();
		inputNumValUO = "";
		//取消按钮
		$("#btnCloseForPeopleKeyboardUO").click(function(){
			Util.dialongDisplay({
				type : 'hide',
				renderTo : 'divKeyboardPeopleForUO'
			});
			inputNumValUO = "";
			$("#" + inputNumIdUO).val(inputNumValUO);
		});	
		$("#divLeftForKeyboardPeopleForUO > div[class*=isSave]").bind("click", function(){
			uo.saveForChangePeople();
		});
	}
};

/**
 * 确定修改餐桌人数
 */
uo.saveForChangePeople = function(){
	
	var num = $("#" + inputNumIdUO).val();
	
	//关闭该界面
	Util.dialongDisplay({
		type : 'hide',
		renderTo : 'divKeyboardPeopleForUO'
	});
	//清空输入框的显示信息
	inputNumValUO = "";
	$("#" + inputNumIdUO).val(inputNumValUO);
	//更改页面端的的人数
	$("#customNumForUO").html("用餐人数：" + num);	
	$("#divLeftForKeyboardPeopleForUO > div[class*=isSave]").unbind("click");
	uo.updateCustom = true;
	uo.saveForUO();
};

/**
 * 工具栏的确定按钮,对整个页面信息提交
 */
uo.saveForUO = function(){
	uo.customNum = $("#customNumForUO").html().substring(5);
	//判断页面信息是否有改动
	if(uoCancelFoods.length == 0 && uo.order.customNum == uo.customNum){
		Util.msg.alert({
			title : '温馨提示',
			msg : '账单没有修改，不能提交', 
			time : 3
		});
	}else{
		for(var x = 0; x < uoFood.length; x++){
			for(var y = 0; y < uoCancelFoods.length; y++){
				if(uoFood[x].id == uoCancelFoods[y].id && uoFood[x].tasteGroup.tastePref == uoCancelFoods[y].dishes){
					uoFood[x].count = parseFloat(uoFood[x].count + uoCancelFoods[y].count).toFixed(2);
					uoFood[x].cancelReason = uoCancelFoods[y].reason;
				}
			}
		}
		uo.updateOrder = uoFood;
		//对更新的菜品和人数进行提交
		uo.submitUpdateOrderHandler(uoFood);	
	}
};

/**
 * 工具栏的取消按钮,取消对该页面的修改操作
 */
uo.cancelForUO = function(){	
	uo.customNum = $("#customNumForUO").html().substring(5);
	//判断页面信息是否有改动
	if(uoCancelFoods.length == 0 && uo.order.customNum == uo.customNum){
		uoCancelFoods = [];
		uoFood = [];
		Util.toggleContentDisplay({type:'hide', renderTo:'divUpdateOrder'});
	}else{
		Util.msg.alert({
			title : '重要',
			msg : '账单信息已修改，“确定”将不保存这些改动，是否确定？',
			buttons : 'YESBACK',
			fn : function(btn){
				if(btn == 'yes'){
					uoCancelFoods = [];
					uoFood = [];
					Util.toggleContentDisplay({type:'hide', renderTo:'divUpdateOrder'});
				}
			}
		});
	}
};

/**
 * 数字键点击事件
 * @param {object} o 触发该函数的按钮对象 
 */
function inputNumUO(o){
	if(uo.selectAll){
		inputNumValUO = "";
	}else{
		inputNumValUO = $("#" + inputNumIdUO).val() != 0 ? $("#" + inputNumIdUO).val() : "";
	}
	
	//设置输入框的显示值（原有值加上输入值）
	inputNumValUO += o.innerHTML;
	uo.selectAll = false;
	$("#" + inputNumIdUO).val(inputNumValUO);
	$("#" + inputNumIdUO).focus();
	//判断退菜数目是否合法
	if(inputNumIdUO == "txtNumForUO"){
		if(parseFloat($("#" + inputNumIdUO).val()) > count){
			Util.msg.alert({
				title : '温馨提示',
				msg : '退菜数不能超过点菜数.', 
				time : 3,
				fn : function(btn){
					inputNumValUO = "";
					$("#" + inputNumIdUO).val(count);
				}
			});
		}
		if(parseFloat($("#" + inputNumIdUO).val()) < 0){
			Util.msg.alert({
				title : '温馨提示',
				msg : '退菜数不能小于0.', 
				time : 3
			});
			inputNumValUO = "";
			$("#" + inputNumIdUO).val(count);
		}
	}
	//判断输入的人数是否合法
	if(inputNumIdUO == "txtPeopleNumForUO"){
		if(parseInt($("#" + inputNumIdUO).val()) > 255){
			Util.msg.alert({
				title : '温馨提示',
				msg : '数目不能超过255.', 
				time : 3
			});
			inputNumValUO = "";
			$("#" + inputNumIdUO).val(1);
		}
		if(parseFloat($("#" + inputNumIdUO).val()) < 0){
			Util.msg.alert({
				title : '温馨提示',
				msg : '数量不能小于0.', 
				time : 3
			});
			inputNumValUO = "";
			$("#" + inputNumIdUO).val(1);
		}
	}
	
	if(getDom(inputNumIdUO).oninput){
		getDom(inputNumIdUO).oninput();
	}	
	
}

/**
 * 重置数字
 */
uo.backAll = function(){
	inputNumValUO = "";
	$("#" + inputNumIdUO).val(inputNumValUO);
	$("#" + inputNumIdUO).focus();
};

uo.backOne = function(){
	var tempNum;
	tempNum = $("#" + inputNumIdUO).val();
	if(tempNum.length > 0){
		inputNumValUO = tempNum.substring(0, tempNum.length-1);
		$("#" + inputNumIdUO).val(inputNumValUO);
	}
	$("#" + inputNumIdUO).focus();
	if(getDom(inputNumIdUO).oninput){
		getDom(inputNumIdUO).oninput();
	}	
	
};


/**
 * 加一按钮
 */
uo.addOneNum = function(){
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
};

/**
 * 减一按钮
 */
uo.cutOne = function(){
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
};

/**
 * 工具栏点菜按钮
 */
uo.goToCreateOrder = function(){
	uo.customNum = $("#customNumForUO").html().substring(5);
	//判断页面信息是否有改动
	if(uoCancelFoods.length == 0 && uo.order.customNum == uo.customNum){
		//FIXME 每点一次餐台都去更新菜品
		initFoodData();
		
		co.show({
			table : uo.table,
			order : uo.order,
			callback : function(){
				initTableData();
				uo.cancelForUO();
			}
		});
	}else{
		Util.msg.alert({
			title : '重要提示',
			msg : '账单已经修改，请先做“确认修改”操作。',
			time : 3
		});
	}
};

/**
 * 已点菜改单提交操作
 */
uo.submitUpdateOrderHandler = function(c){
	var orderFoods = c;
	if(orderFoods.length > 0){
		orderDataModel.tableAlias = uo.table.alias;
		orderDataModel.customNum = uo.customNum;
		orderDataModel.orderFoods = orderFoods;
		orderDataModel.categoryValue = uoOther.order.categoryValue;
		orderDataModel.id = uoOther.order.id,
		orderDataModel.orderDate = uoOther.order.orderDate;

		Util.LM.show();
		$.ajax({
			url : '../InsertOrder.do',
			type : 'post',
			data : {
				commitOrderData : JSON.stringify(Wireless.ux.commitOrderData(orderDataModel)),
				type : 7
			},
			success : function(data, status, xhr){
				Util.LM.hide();
					//下单成功时才出现倒数, 其他问题则等待确认
					if(data.success){
						//清空退菜列表
						uoCancelFoods = [];
						if(uo.printTemp == true){
							uo.printTemp = false;
							uo.tempPayForUO();
						}else{
							if(uo.canceling){
								updateTable({
									alias : uo.table.alias
								});								
								Util.msg.alert({
									title : data.title,
									msg : '退菜成功', 
									time : 2
								});		
								uo.canceling = false;
							}else if(uo.updateCustom){
								updateTable({
									alias : uo.table.alias
								});
								Util.msg.alert({
									title : data.title,
									msg : '修改成功', 
									time : 2
								});	
								uo.updateCustom = false;
							}else{
								Util.msg.alert({
									title : data.title,
									msg : data.msg, 
									time : 3,
									fn : function(btn){
										Util.toggleContentDisplay({type:'hide', renderTo:'divUpdateOrder'});
										initTableData();
									}
								});							
							}

						}

					}else{
						//账单过期就刷新, 否则另外处理
						if(data.code == '9195'){
							Util.msg.alert({
								title : data.title,
								msg : data.msg, 
								buttons : 'YESBACK',
								btnEnter : '刷新账单',
								fn : function(btn){
									if(btn == 'yes'){
										uoCancelFoods = [];
										uo.updateOrderHandler();
									}
								}
							});
						}else{
							Util.msg.alert({
								title : data.title,
								msg : data.msg, 
								fn : function(btn){
									return;
								}
							});
						}

					}
			},
			error : function(request, status, err){
				Util.LM.hide();
				Util.msg.alert({
					title : '温馨提示',
					msg : err, 
					time : 3
				});
			}
		});
	}else if(orderFoods.length == 0){
		Util.msg.alert({
			title : '温馨提示',
			msg : '没有任何菜品，不能提交', 
			time : 3
		});
	}
};

uo.tempPayForPrintAll = function(c){
	Util.LM.show();
	$.post('../PrintOrder.do', {'tableID' : uo.order.table.alias, 'printType' : 1}, function(result){
		Util.LM.hide();
		if(result.success){
			Util.msg.alert({
				msg : '操作成功',
				time : 3,
				callback : null
			});
		}else{
			Util.msg.alert({
				title : '错误',
				msg : result.msg,
				time : 3
			});
		}		
	});
}

/**
 * 暂结
 */
uo.tempPayForUO = function(c){
	c = c == null ? {} : c;
	uo.customNum = $("#customNumForUO").html().substring(5);
	//判断页面信息是否有改动
	if(uoCancelFoods.length == 0 && uo.order.customNum == uo.customNum){
		Util.LM.show();
		$.ajax({
			url : '../PayOrder.do',
			type : 'post',
			data : {
				pin : pin,
				eraseQuota : uo.order.erasePrice,
				orderID : uo.order.id,
				payType : uo.order.settleTypeValue,
				memberID : uo.order.member,
				payManner : uo.order.payTypeValue,
				serviceRate : uo.order.serviceRate,
				cashIncome : '-1',
//				comment : uo.order.comment,
				customNum : uo.order.customNum,
				discountID : typeof c.discountId != 'undefined' ? c.discountId : '',
				tempPay : true,
				isPrint : typeof c.isPrint == 'boolean' ? c.isPrint : true
			},
			dataType : 'text',
			success : function(result, status, xhr){
				Util.LM.hide();
				result = eval("(" + result + ")");
				if(result.success){
					Util.msg.alert({
						msg : '操作成功',
						time : 3,
						callback : typeof c.callback == 'function' ? c.callback(result) : null
					});
					initOrderData({table : uo.table});
				}else{
					Util.msg.alert({
						title : '错误',
						msg : result.data,
						time : 3
					});
				}
			},
			error : function(xhr, status, err){
				Util.LM.hide();
				Util.msg.alert({
					title : '错误',
					msg : err,
					time : 3
				});
			}
		});
	}else{
/*		Util.msg.alert({
			title : '重要提示',
			msg : '账单已经修改，请先做“确认修改”操作。',
			time : 3
		});*/
		uo.printTemp = true;
		uo.saveForUO();
	}
};

uo.inputForPay = function(){
	Util.dialongDisplay({
		type : 'show',
		renderTo : 'divInputForPay'
	});
	$("#txtNewTableForTS").focus();	
};

uo.inputPay = function (c){
	var inputPay=getDom('numInputReceipt');
	if(c.type === 1){
		inputPay.value=inputPay.value.substring(0, inputPay.value.length - 1);
	}else if(c.type === 2){
		inputPay.value='';
	}else{
		inputPay.value=inputPay.value + '' + c.value;
	}
	inputPay.focus();
};

uo.ip.back = function(){
	Util.dialongDisplay({
		type : 'hide',
		renderTo : 'divInputForPay'
	});
	$("#txtShouldToReceipt").val();
	$("#numInputReceipt").val();
	$('#txtReciptReturn').val();
};

//弹出会员暂结
uo.mPay.show = function(){
	inputNumIdUO = "txtMemberPhoneOrCard";
	Util.dialongDisplay({
		renderTo : 'divMemberTempPay',
		type : 'show'
	});
	$("#txtMemberPhoneOrCard").focus();
};

//关闭会员暂结
uo.mPay.back = function(){
	Util.dialongDisplay({
		renderTo : 'divMemberTempPay',
		type : 'hide'
	});
	getDom('txtMemberPhoneOrCard').value = '';
	inputNumValUO = '';
};

//会员暂结
uo.mPay.tempPay = function(){
	var memberPhoneOrCard = $('#txtMemberPhoneOrCard').val(); 
	if(!memberPhoneOrCard || isNaN(memberPhoneOrCard)){
		Util.msg.alert({
			title : '提示',
			msg : '请输入有效数字', 
			time : 2,
			fn : function(){
				$('#txtMemberPhoneOrCard').focus();
			}
		});			
		return;
	}
	Util.LM.show();
	$.post('../QueryOrderFromMemberPay.do', {
		orderID:uo.order.id,
		st:0,
		sv:memberPhoneOrCard	
	}, function(data){
		if(typeof data == 'string'){
			data = eval("(" + data + ")");	
		}
		if(data.success){
			var result_member;
			if(data.other.members){
				result_member = data.other.members[0];
			}else if(data.other.member){
				result_member = data.other.member;
			}else{
				Util.msg.alert({
					title : '提示',
					msg : '无对应的会员, 请重新输入条件', 
					time : 2
				});	
				return;
			}
			$.post('../PayOrder.do', {
				orderID : uo.order.id,
				payType : 2,
				tempPay : true,
				cashIncome : data.other.newOrder.totalPrice,
				memberID : result_member.id
			}, function(result){
				result = eval("(" + result + ")");
				Util.LM.hide();
				if(result.success){
					uo.mPay.back();
				}
				Util.msg.alert({
					title : '提示',
					msg : result.data, 
					time : 2
				});	
			});			
		}else{
			Util.LM.hide();
			Util.msg.alert({
				title : data.title,
				msg : data.msg, 
				time : 2,
				fn : function(){
					$('#txtMemberPhoneOrCard').focus();
				}
			});				
		}
	});
	
}

/**
 * 设置折扣
 */
uo.cd = {
	el : 'divChangeDiscountForUpdateOrder',
	data : [],
	isRequest : false,
	selectClass : 'uo-div-select-kitchen',
	initContent : function(c){
		var temp = null, html = [];
		for(var i = 0; i < this.data.length; i++){
			temp = this.data[i];
			html.push(Templet.uo.changeDiscount.format({
				id : temp.id,
				name : temp.name
			}));
		}
		$('#{0} > div[addr=centent]'.format(this.el)).html(html.join(''));
		temp = null, html = null;
	},
	show : function(){
		if(this.isRequest === true) return;
		
		Util.LM.show();
		this.isRequest = true;
		$.ajax({
			url : '../QueryDiscount.do',
			data : {
				dataSource : 'role'
			},
			dataType : 'json',
			success : function(data, status, xhr){
				this.isRequest = false;
				Util.LM.hide();
				if(data.success && data.root.length > 0){
					Util.dialongDisplay({
						renderTo : uo.cd.el,
						type : 'show'
					});
					uo.cd.data = data.root;
					uo.cd.initContent();
				}else{
					Util.msg.alert({
						title : '提示',
						msg : '没有折扣信息.',
						time : 3
					});
				}
			},
			error : function(xhr, status, err){
				this.isRequest = false;
				Util.LM.hide();
				Util.msg.alert({
					title : '错误',
					msg : '加载折扣信息失败.',
					time : 3
				});
			}
		});
	},
	back : function(){
		uo.cd.data = [];
		uo.cd.initContent();
		this.isRequest = false;
		Util.dialongDisplay({
			renderTo : this.el,
			type : 'hide'
		});
	},
	select : function(c){
		var temp = null, list = $('#{0} > div[addr=centent] > div'.format(this.el));
		for(var i = 0; i < list.length; i++){
			temp = $(list[i]);
			if(parseInt(temp.attr('data-value')) === c.id){
				temp.addClass(this.selectClass);
			}else{
				temp.removeClass(this.selectClass);
			}
		}
	},
	save : function(c){
		var select = $('#{0} > div[addr=centent] > div[class*={1}]'.format(this.el, this.selectClass));
		if(select.length != 1) return;
		select = select[0];
		$.post('../OperateDiscount.do', {
			dataSource : 'setDiscount',
			orderId : uo.order.id,
			discountId : select.getAttribute('data-value')
		}, function(data){
			if(data.success){
				Util.msg.alert({
					title : '提示',
					msg : '打折成功', 
					time : 2,
					fn : function(){
						updateTable({
							alias : uo.table.alias
						});						
						uo.cd.back();		
					}
				});			
			}else{
				Util.msg.alert({
					title : '提示',
					msg : '打折失败, 请刷新页面重试', 
					time : 2
				});					
			}
			
		});
/*		uo.tempPayForUO({
			discountId : select.getAttribute('data-value'),
			isPrint : false,
			callback : function(data){
				uo.cd.back();
			}
		});*/
	}
};

