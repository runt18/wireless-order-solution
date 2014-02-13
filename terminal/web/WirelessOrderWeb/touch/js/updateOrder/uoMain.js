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
		
	}
};

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
	var num;
	num = parseInt($("#" + inputNumIdUO).val());
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
			time : 3,
		});
	}else{
		for(var x = 0; x < uoFood.length; x++){
			for(var y = 0; y < uoCancelFoods.length; y++){
				//alert(JSON.stringify(uoCancelFoods[y]));//return;
				if(uoFood[x].id == uoCancelFoods[y].id && uoFood[x].tasteGroup.tastePref == uoCancelFoods[y].dishes){
					uoFood[x].count = parseFloat(uoFood[x].count + uoCancelFoods[y].count).toFixed(2);
					uoFood[x].cancelReason = uoCancelFoods[y].reason.id;
				}
			}
		}
		uoCancelFoods = [];
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
	//设置输入框的显示值（原有值加上输入值）
	inputNumValUO += o.innerHTML;
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
		if(parseFloat($("#" + inputNumIdUO).val()) == 0){
			Util.msg.alert({
				title : '温馨提示',
				msg : '人数值不能为0.', 
				time : 3
			});
			inputNumValUO = "";
			$("#" + inputNumIdUO).val(1);
		}
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
		var foodPara = '';
		for ( var i = 0; i < orderFoods.length; i++) {
			foodPara += ( i > 0 ? '<<sh>>' : '');
			if (orderFoods[i].isTemporary) {
				// 临时菜
				var foodname = orderFoods[i].name;
				foodPara = foodPara 
						+ '[' 
						+ 'true' + '<<sb>>'// 是否临时菜(true)
						+ orderFoods[i].id + '<<sb>>' // 临时菜1编号
						+ foodname + '<<sb>>' // 临时菜1名称
						+ orderFoods[i].count + '<<sb>>' // 临时菜1数量
						+ orderFoods[i].unitPrice + '<<sb>>' // 临时菜1单价(原料單價)
						+ orderFoods[i].isHangup +'<<sb>>' // 菜品状态
						+ '1' + '<<sb>>' // 菜品操作状态 1:已点菜 2:新点菜 3:反结账
						+ orderFoods[i].kitchen.id + '<<sb>>'	// 临时菜出单厨房
						+ (typeof orderFoods[i].cancelReason != 'undefined' ?  orderFoods[i].cancelReason : 0)//退菜原因
						+ ']';
			}else{
				// 普通菜
				var normalTaste = '', tmpTaste = '' , tasteGroup = orderFoods[i].tasteGroup;
				for(var j = 0; j < tasteGroup.normalTasteContent.length; j++){
					var t = tasteGroup.normalTasteContent[j];
					normalTaste += ((j > 0 ? '<<stnt>>' : '') + (t.id + '<<stb>>' + t.cateValue + '<<stb>>' + t.cateStatusValue));
				}
				if(tasteGroup.tmpTaste != null && typeof tasteGroup.tmpTaste != 'undefined'){
					if(eval(tasteGroup.tmpTaste.id >= 0))
						tmpTaste = tasteGroup.tmpTaste.price + '<<sttt>>' + tasteGroup.tmpTaste.name  + '<<sttt>>' + tasteGroup.tmpTaste.id+ '<<sttt>>' + tasteGroup.tmpTaste.alias; 				
				}
				foodPara = foodPara 
						+ '['
						+ 'false' + '<<sb>>' // 是否临时菜(false)
						+ orderFoods[i].id + '<<sb>>' // 菜品1编号
						+ orderFoods[i].count + '<<sb>>' // 菜品1数量
						+ (normalTaste + ' <<st>> ' + tmpTaste) + '<<sb>>'
						+ orderFoods[i].kitchen.id + '<<sb>>'// 厨房1编号
						+ orderFoods[i].discount + '<<sb>>' // 菜品1折扣
						+ orderFoods[i].isHangup + '<<sb>>'//是否叫起
						+ (typeof orderFoods[i].cancelReason != 'undefined' ?  orderFoods[i].cancelReason : 0) //退菜原因
						+ ']';
			}
		}	
		foodPara = '{' + foodPara + '}';	
		var type = 2;
		Util.LM.show();
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
				Util.LM.hide();
				Util.msg.alert({
					title : data.title,
					msg : data.msg, 
					time : 3,
					fn : function(btn){
						Util.toggleContentDisplay({type:'hide', renderTo:'divUpdateOrder'});
						initTableData();
					}
				});
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
				comment : uo.order.comment,
				customNum : uo.order.customNum,
				discountID : typeof c.discountId != 'undefined' ? c.discountId : uo.order.discount.id,
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
		Util.msg.alert({
			title : '重要提示',
			msg : '账单已经修改，请先做“确认修改”操作。',
			time : 3
		});
	}
};

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
				dataSource : 'role',
				roleId : staffData.role.id
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
						time : 3,
					});
				}
			},
			error : function(xhr, status, err){
				this.isRequest = false;
				Util.LM.hide();
				Util.msg.alert({
					title : '错误',
					msg : '加载折扣信息失败.',
					time : 3,
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
		uo.tempPayForUO({
			discountId : select.getAttribute('data-value'),
			isPrint : false,
			callback : function(data){
				uo.cd.back();
			}
		});
	}
};

