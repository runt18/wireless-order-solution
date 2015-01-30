
window.onload = function (){ 
} 

//存放退菜的数组
var uoCancelFoods = [];

var uo = {
	table : {},
	order : {},
	uoFood : [],
	reasons : [],
	discounts : [],
	selectedFood : {}
};


function initSearchTables(c){
	var html = '';
//	var click;
//	if(ts.commitTableOrTran == 'lookup'){
//		click = 'ts.toOrderFoodOrTransFood'; 
//	}else if(ts.commitTableOrTran == 'frontTransTable'){
//		click = 'ts.toOrderFoodOrTransFood';
//	}else{
//		click = 'ts.toOrderFoodOrTransFood';
//	}
	for (var i = 0; i < c.data.length; i++) {
		
		html += tableCmpTemplet.format({
			dataIndex : i,
			id : c.data[i].id,
			click : 'ts.toOrderFoodOrTransFood({alias:'+ c.data[i].alias +',id:'+ c.data[i].id +'})',
			alias : c.data[i].alias && c.data[i].alias != 0?c.data[i].alias:'<font color="green">搭台</font>',
			theme : c.data[i].statusValue == '1' ? "e" : "c",
			name : c.data[i].name == "" || typeof c.data[i].name != 'string' ? c.data[i].alias + "号桌" : c.data[i].name
		});	
	}
	$('#divSelectTablesForTs').html(html);
	$('#divSelectTablesForTs a').buttonMarkup( "refresh" );
}

var orderFoodListCmpTemplet = '<tr>'
	+ '<td>{dataIndex}</td>'
	+ '<td ><div style="height: 45px;overflow: hidden;">{name}</div></td>'
	+ '<td>{count}<img style="margin-top: 10px;margin-left: 5px;display:{isWeight}" src="images/weight.png"></td>'
	+ '<td><div style="height: 45px;overflow: hidden;">{tastePref}</div></td>'
	+ '<td>{actualPrice}</td>'
//	+ '<td>{totalPrice}</td>'
	+ '<td>{orderDateFormat}</td>'
	+ '<td>' 
	+ 		'<div data-role="controlgroup" data-type="horizontal" >'
    + 			'<a onclick="uo.openCancelFoodCmp({event:this})" data-index={dataIndex} data-role="button" data-theme="b">退菜</a>'
    +			'<a onclick="uo.transFoodForTS({event:this})" data-index={dataIndex} data-role="button" data-theme="b">转菜</a>'
    +			'<a onclick="uo.openOrderFoodOtherOperate({event:this})" data-index={dataIndex} data-role="button" data-theme="b"  data-rel="popup"  data-transition="pop" href="#{hasWeigh}">更多</a>'
    +		'</div>'
	+ '<td>{waiter}</td>'
	+ '</tr>';	

var tableCmpTemplet = '<a onclick="{click}" data-role="button" data-corners="false" data-inline="true" class="tableCmp" data-index={dataIndex} data-value={id} data-theme={theme}><div>{name}<br>{alias}</div></a>';


/**
 * 初始化菜单数据，存放在uoFood数组中
 * @param {object} data 餐桌对象
 */
function initOrderData(c){
	// 加载菜单数据
	$.ajax({
		url : '../QueryOrder.do',
		type : 'post',
		data : {
			tableID : c.table.id		
		},
//		async : false,
		success : function(data, status, xhr){
//			uo.uoFood = [];
			if(data.success){
				uo.order = data.other.order;
				uo.table = c.table;
/*				uo.order.orderFoods = data.other.order.orderFoods;
				for(x in  data.other.order.orderFoods){
					uo.uoFood.push( data.other.order.orderFoods[x]);
				}
				uoOther = data.other;*/
				if(uo.order.memberId > 0){
					$.ajax({
						url : '../QueryMember.do',
						type : 'post',
						data : {
							dataSource : 'normal',
							id : uo.order.memberId
						},
						async : false,
						success : function(data, status, xhr){
							if(data.success){
								uo.orderMember = data.root[0];
							}
						}
					});
				}else{
					uo.orderMember = null;
				}
				
				uo.showOrder();
				uo.showDescForUpdateOrder();
			    c.createrOrder == 'createrOrder' ? of.show({
					table : uo.table,
					order : uo.order,
					callback : function(){
						location.href = '#orderFoodListMgr';
						//异步刷新账单
						initOrderData({table : uo.table});
					}
				}) : null;
									
			}else{
				Util.msg.alert({
					title : data.title,
					msg : data.msg,
					renderTo : 'orderFoodListMgr',
					time : 3
				});
			}
		},
		error : function(request, status, err){
			Util.msg.alert({
				title : '温馨提示',
				msg : '初始化菜单数据失败.', 
				renderTo : 'orderFoodListMgr',
				time : 3
			});
		}
	});	
}


/**
 * 初始化账单
 */
uo.showOrder = function(){
	var html = '';
	for(var i = 0; i < uo.order.orderFoods.length; i++){
		html += orderFoodListCmpTemplet.format({
			dataIndex : i + 1,
			id : uo.order.orderFoods[i].id,
			name : uo.order.orderFoods[i].name,
			count : uo.order.orderFoods[i].count.toFixed(2),
			isWeight : (uo.order.orderFoods[i].status & 1 << 7) != 0 ? 'initial' : 'none',
			hasWeigh : (uo.order.orderFoods[i].status & 1 << 7) != 0 ?'orderFoodMoreOperateCmp':'',
			tastePref : uo.order.orderFoods[i].tasteGroup.tastePref,
			actualPrice : (uo.order.orderFoods[i].actualPrice + uo.order.orderFoods[i].tasteGroup.tastePrice).toFixed(2) + (uo.order.orderFoods[i].isGift?'&nbsp;[<font style="font-weight:bold;">已赠送</font>]':''),
			totalPrice : uo.order.orderFoods[i].totalPrice.toFixed(2),
			orderDateFormat : uo.order.orderFoods[i].orderDateFormat.substring(11),
			waiter : uo.order.orderFoods[i].waiter 
		});
	}			
	
	$('#orderFoodListBody').html(html).trigger('create');
	
	uo.showNorthForUpdateOrder();	
}

/**
 * 初始化页头信息（账单号，餐台号，餐台名，用餐人数）
 */
uo.showNorthForUpdateOrder = function(){
	var html = "";
	var tableName;
	if(uo.order.table.name == ""){
		tableName = uo.order.table.alias + "号桌";
	}else{
		tableName = uo.order.table.name;
	}
	uo.customNum = uo.order.customNum;
	
	html = "<div><span style = 'margin : 10px 250px 10px 10px; font-size : 24px;font-weight : bold;'>已点菜页面</span>" +
			"<span style = 'margin: 10px;'>账单号：" + uo.order.id + " </span>" +
			"<span style = 'margin: 10px;'>餐台号：" + uo.order.table.alias + " </span>" +
			"<span style = 'margin: 10px;'>餐台名： " + tableName + "</span>" +
			"<span style = 'margin: 10px;' id='customNumForUO'>用餐人数：" + uo.customNum + "</span>" +			
		"</div>";
	$("#divNorthForUpdateOrder").html(html);
}

/**
 * 初始化页尾信息（菜品数量，消费总额）
 */
uo.showDescForUpdateOrder = function(){
	var html = "";
	html = (uo.orderMember?"<span style = 'margin-left: 20px;'>当前会员：" + uo.orderMember.name +"</span>" : "") +
		(uo.order.discount?"<span style = 'margin-left: 20px;'>当前折扣：" + uo.order.discount.name +"</span>" : "") +
		(uo.order.discounter ? "<span style = 'margin-left: 20px;'>折扣人：" + uo.order.discounter + "</span><span style = 'margin-left: 20px;'>折扣时间：" + uo.order.discountDate + "</span>" : "") ;
		$("#divDescForUpdateOrder").html(html);
		$("#spanTotalPriceUO").html('消费总额：'+ uo.getTotalPriceUO().toFixed(2) + "元");
}
/**
 * 取得初始的消费总额
 * @returns {number} totalPriceUO
 */
uo.getTotalPriceUO = function(){
	var totalPriceUO = 0;
	var uoFood = uo.order.orderFoods;
	for(x in uoFood){
		totalPriceUO += uoFood[x].count * (uoFood[x].actualPrice + uoFood[x].tasteGroup.tastePrice) * uoFood[x].discount;
	}
	return totalPriceUO;
};

/**
 * 显示已点菜界面函数
 * @param {object} c  
 */
uo.show = function(c){
	
	//加载退出原因
	if(uo.reasons.length <= 0){
		//加载退菜原因
		$.post('../QueryCancelReason.do', function(result){
			if(result.success){
				uo.reasons = result.root;
				uo.loadCancelReasonData(result.root);
			}
		});			
	}
	
	if(true){
		uo.loadDiscountCmp();
	}
	
	//异步刷新账单
	initOrderData({table : c.table});
	uo.table = c.table;
};

/**
 * 返回
 */
uo.back = function(){
	ts.loadData();
}

/**
 * 加载退菜原因
 */
uo.loadCancelReasonData = function(data){
	uo.selectingCancelReason = null;
	var html = '';
	for (var i = 0; i < data.length; i++) {
		html += '<a data-role="button" data-index='+ i +' data-inline="true" class="regionBtn" onclick="uo.selectCancelReason({event:this})">'+ data[i].reason +'</a>';
	}
	$('#cancelFoodReasonCmp').html(html).trigger('create').trigger('refresh');
	
	html = null;
}

/**
 * 选择退菜原因
 */
uo.selectCancelReason = function(c){
	var selectedReason = $(c.event);
	uo.selectingCancelReason = uo.reasons[parseInt($(c.event).attr('data-index'))];
	
	$('#cancelFoodReasonCmp a').attr('data-theme', 'c').removeClass('ui-btn-up-e').addClass('ui-btn-up-c');
	
	$(c.event).attr('data-theme', 'e').removeClass('ui-btn-up-c').addClass('ui-btn-up-e');
	
	$('#cancelFoodReasonCmp').trigger('refresh');
}

/**
 * 打开退菜操作
 */
uo.openCancelFoodCmp = function(c){
	//刷新原因界面
	uo.loadCancelReasonData(uo.reasons);
	
	var orderFood = uo.order.orderFoods[parseInt($(c.event).attr('data-index'))-1];
	
	uo.operateFood = orderFood;
	
	$('#inputCancelFoodSet').val(checkDot(orderFood.count+'')?orderFood.count : parseInt(orderFood.count));
	
	$('#cancelFoodSetShadow').show();
	$('#cancelFoodSet').show();
	
	$('#numberKeyboard').show();
	
	firstTimeInput = true;
	$('#inputCancelFoodSet').focus();
	$('#inputCancelFoodSet').select();
}

/**
 * 关闭退菜操作
 */
uo.closeCancelFoodCmp = function(){
	$('#inputCancelFoodSet').val('');
	
	$('#cancelFoodSetShadow').hide();
	$('#cancelFoodSet').hide();
	
	$('#numberKeyboard').hide();
}

/**
 * 退菜操作
 */
uo.cancelFoodAction = function(){
	
	//取得退菜数目并进行判定
	var num = $("#inputCancelFoodSet").val();
	if(num <= 0){
		Util.msg.alert({
			msg : '退菜数目不正确', 
			topTip : true
		});
		$("#inputCancelFoodSet").val(uo.operateFood.count);
		$("#inputCancelFoodSet").select();
		firstTimeInput = true;
	}else if(isNaN(num)){
		Util.msg.alert({
			msg : '数字不合规范.', 
			topTip : true
		});
		$("#inputCancelFoodSet").val(uo.operateFood.count);
		$("#inputCancelFoodSet").select();
		firstTimeInput = true;
	}else if(num > uo.operateFood.count){
		Util.msg.alert({
			msg : '退菜数量不能大于原有数量', 
			topTip : true
		});
		$("#inputCancelFoodSet").val(uo.operateFood.count);
		$("#inputCancelFoodSet").select();
		firstTimeInput = true;
	}else{
		num = parseFloat(num).toFixed(2);
		//退菜信息
		var uoCancelFood = {
				alias : 0,
				foodName : "" ,
				dishes : "" ,
				count : 0 ,
				actualPrice : "",
				totalPrice : ""
			};
		var actualPrice,  totalPrice;
		
		actualPrice = uo.operateFood.actualPrice;
		totalPrice = actualPrice * (-num);
		
		//把相关数据加到退菜信息对象
		uoCancelFood.id = uo.operateFood.id;
		uoCancelFood.foodName = uo.operateFood.name;
		uoCancelFood.dishes = uo.operateFood.tasteGroup.tastePref;
		uoCancelFood.count = -num;
		if(uo.selectingCancelReason){
			uoCancelFood.reason = uo.selectingCancelReason;
		}
		
		uoCancelFood.actualPrice = actualPrice;
		uoCancelFood.totalPrice = totalPrice;
		//加到退菜数组
		uoCancelFoods.push(uoCancelFood);
		
		//更改消费总额
		var totalPrice = uo.getTotalPriceUO();
		for(x in uoCancelFoods){
			totalPrice += uoCancelFoods[x].totalPrice;
		}
		$("#spanTotalPriceUO").html('消费总额：'+totalPrice.toFixed(2) + "元");
		//关闭该界面
		uo.closeCancelFoodCmp();
		
		uo.canceling = true;
		uo.saveForUO();
	}
}
/**
 * 打开称重
 */
uo.openWeighOperate = function(){
	setTimeout(function(){
		$('#inputOrderFoodWeigh').focus();
		//显示菜名
		$('#weighFoodName').text(uo.selectedFood.name);		
	}, 250);
	$('#orderFoodWeighCmp').parent().addClass('popup').addClass('in');
	$('#orderFoodWeighCmp').popup('open');

}

/**
 * 关闭称重
 */
uo.closeWeighOperate = function(){
	$('#inputOrderFoodWeigh').val('');
	$('#orderFoodWeighCmp').popup('close');
	$('#orderFoodWeighCmp').popup('close');
	//删除动作
	delete uo.weighOperate;
}


/**
 * 打开更多操作
 */
uo.openOrderFoodOtherOperate = function(c){
	uo.selectedFood = uo.order.orderFoods[parseInt($(c.event).attr('data-index'))-1];
}

/**
 * 去称重
 */
uo.weighAction = function(){
	uo.weighOperate=true;
	
	setTimeout(function(){
		uo.openWeighOperate();
	}, 250);
	$('#orderFoodMoreOperateCmp').popup('close');
};

/**
 * 称重操作
 */
uo.openWeighaction = function(){
	var count = $('#inputOrderFoodWeigh');
	if(!count.val()){
		Util.msg.alert({
			msg : '请输入称重数量',
			topTip : true
		});
		count.focus();
		return;
	}else if(isNaN(count.val())){
		Util.msg.alert({
			msg : '请输入正确的称重数量',
			topTip : true
		});
		count.focus();
		return;
	}else if(count.val() < uo.selectedFood.count){
		Util.msg.alert({
			msg : '称重数量不能少于原有数量',
			topTip : true
		});
		count.focus();
		return;
	}
	
	uo.selectedFood.count = count.val();
	//对更新的菜品和人数进行提交
	uo.submitUpdateOrderHandler(uo.order.orderFoods);	
}

/**
 * 设置控件为转菜
 * @param o
 */
uo.transFoodForTS = function(c){
	//显示数量输入
	$('#td4TxtFoodNumForTran').show();
	//设置操作类型为转菜
	ts.commitTableOrTran = 'trans';
	
	var orderFood = uo.order.orderFoods[parseInt($(c.event).attr('data-index'))-1];
	
	$('#transSomethingTitle').html(orderFood.name +" -- 请输入桌号，菜品数量确定转菜");
	
	ts.tf.id = orderFood.id
	ts.tf.count = orderFood.count + '';	
	$('#txtFoodNumForTran').val(checkDot(ts.tf.count)?ts.tf.count : parseInt(ts.tf.count));	
	
	//打开控件
	uo.openTransOrderFood();
}

/**
 * 设置控件为全单转菜
 */
uo.allTransFoodForTS = function (){
	//隐藏数量输入
	$('#td4TxtFoodNumForTran').hide();
	
	ts.commitTableOrTran = 'allTrans';
	
	$('#transSomethingTitle').html("请输入桌号，确定全部转菜");

	//关闭更多操作
	$('#updateFoodOtherOperateCmp').popup('close');	
	
	//打开控件
	uo.openTransOrderFood();	

}

/**
 * 设置控件为转台
 */
uo.transTableForTS = function(){
	//隐藏数量输入
	$('#td4TxtFoodNumForTran').hide();
	ts.commitTableOrTran = 'transTable';
	
	$("#txtTableNumForTS").val("");
	
	$('#transSomethingTitle').html("请输入桌号，确定转台");
	
	//打开控件
	uo.openTransOrderFood();
}


/**
 * 设置控件为会员
 */
uo.useMemberForOrder = function(){
	//隐藏数量输入
	$('#td4TxtFoodNumForTran').hide();
	ts.commitTableOrTran = 'member';
	
	$("#txtTableNumForTS").val("");
	//设置为会员输入
	$("#txtTableNumForTS").attr("placeholder", "请输入会员卡号或手机号");
	//显示回删
	$('#td4CmpDeleteWord').show();
	
	$('#transSomethingTitle').html("请输入会员资料, 确定使用会员");
	
	//打开控件
	uo.openTransOrderFood();
}

/**
 * 登陆会员
 */
uo.useMemberForOrderAction = function(){
	var member = $('#txtTableNumForTS');
	if(!member.val()){
		Util.msg.alert({
			msg : '请输入会员信息', 
			topTip : true
		});			
		member.focus();
		return;
	}else if(member.val() <= 0 || isNaN(member.val())){
		Util.msg.alert({
			msg : '请输入正确的会员信息', 
			topTip : true
		});			
		firstTimeInput = true;
		member.focus();
		member.select();	
		return;
	}
	Util.LM.show();
	$.post('../QueryMember.do', {
		dataSource : 'normal',
		memberCardOrMobileOrName : member.val()
	}, function(result){
		if(result.success && result.root.length > 0){
			$.post('../OperateDiscount.do', {
				dataSource : 'setDiscount',
				orderId : uo.order.id,
				memberId : result.root[0].id
			}, function(data){
				Util.LM.hide();
				if(data.success){
					//异步刷新账单
					initOrderData({table : uo.table});
					uo.closeTransOrderFood();
					Util.msg.alert({
						topTip : true,
						msg : '会员注入成功'
					});	
				}else{
					Util.msg.alert({
						title : '提示',
						msg : '使用会员失败, 请刷新页面重试', 
						renderTo : 'orderFoodListMgr',
						time : 2
					});					
				}
			});				
		}else{
			Util.LM.hide();
			Util.msg.alert({
				title : '提示',
				msg : '未找到对应会员, 请输入正确信息', 
				renderTo : 'orderFoodListMgr',
				time : 2,
				fn : function(){
					$('#txtTableNumForTS').focus();
				}
			});
			
		}	
	});
	
	

	
}


/**
 * 转菜操作
 */
uo.openTransOrderFood = function (){
	//设置数字键盘触发
	numKeyBoardFireEvent = ts.s.fireEvent;
	
	$('#transFoodCmp').trigger('create').trigger('refresh');
	$('#transFoodCmp').show();
	$('#transFoodCmpShadow').show();	
	
	$('#numberKeyboard').show();
	
	$('#txtTableNumForTS').focus();
}

/**
 * 关闭转菜
 */
uo.closeTransOrderFood = function(){
	//取消数字键盘触发
	numKeyBoardFireEvent = null;	
	
	if(ts.commitTableOrTran == 'openTable'){
		//隐藏人数输入
		$('#td4OpenTablePeople').hide();
		$('#openTablePeople').val(1);
	}else if(ts.commitTableOrTran == 'tableTransTable'){
		//隐藏转去台
		$('#td4ToOtherTable').hide();
		$('#numToOtherTable').val('');		
	}else if(ts.commitTableOrTran == 'apartTable'){
		//隐藏拆台
		$('#divSelectTablesSuffixForTs').hide();
		//显示餐台选择
		$('#divSelectTablesForTs').show();
	}else if(ts.commitTableOrTran == 'member'){
		//显示为台号信息
		$("#txtTableNumForTS").attr("placeholder", "填写台号");
		//隐藏回删
		$('#td4CmpDeleteWord').hide();
	}
	
	//操作设置为默认
	ts.commitTableOrTran = 'table'
	
	$('#transFoodCmp').hide();
	$('#transFoodCmpShadow').hide();	
	
	$('#numberKeyboard').hide();
	
	$('#txtTableNumForTS').val('');
	$('#txtFoodNumForTran').val('');
	
	$('#divSelectTablesForTs').html('')
}

/**
 * 执行转菜
 */
uo.transFood = function(c){
	ts.tf.count = $('#txtFoodNumForTran').val();
	$.post('../TransFood.do', {
		orderId : uo.order.id,
		aliasId : c.alias,
		transFoods : (c.allTrans?c.allTrans:(ts.tf.id + ',' + ts.tf.count))		
	},function(data){
		if(data.success){
			uo.closeTransOrderFood();
			Util.msg.alert({
				msg : data.msg, 
				topTip : true
			});			
			//刷新已点菜
			updateTable({alias : uo.table.alias});
			ts.tf={};
		}else{
			Util.msg.alert({
				title : '温馨提示',
				msg : data.msg, 
				renderTo : 'orderFoodListMgr',
				time : 2
			});				
		}			
	});
}

/**
 * 暂结
 */
uo.tempPayForUO = function(c){
	c = c == null ? {} : c;
	//判断页面信息是否有改动
	if(uoCancelFoods.length == 0 && uo.order.customNum == uo.customNum){
		Util.LM.show();
		$.ajax({
			url : '../PayOrder.do',
			type : 'post',
			data : {
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
				if(typeof result == 'string'){
					result = eval("(" + result + ")");
				}
				if(result.success){
					Util.msg.alert({
						msg : '操作成功',
						topTip : true
					});
					
					if(typeof c.callback == 'function'){
						c.callback(result);
					}
					
					initOrderData({table : uo.table});
				}else{
					Util.msg.alert({
						title : '错误',
						msg : result.data,
						renderTo : 'orderFoodListMgr',
						time : 3
					});
				}
			},
			error : function(xhr, status, err){
				Util.LM.hide();
				Util.msg.alert({
					title : '错误',
					msg : err,
					renderTo : 'orderFoodListMgr',
					time : 3
				});
			}
		});
	}else{
		uo.printTemp = true;
		uo.saveForUO();
	}
};

/**
 * 动态加载折扣数据
 */
uo.loadDiscountCmp = function(){
	Util.LM.show();
	$.post('../QueryDiscount.do', {dataSource:'role'}, function(result){
		Util.LM.hide();
		var html = '';
		for (var i = 0; i < result.root.length; i++) {
			html += '<li class="tempFoodKitchen" onclick="uo.chooseDiscount({id:'+ result.root[i].id +'})"><a >'+ result.root[i].name +'</a></li>';
		}
		$('#discountCmp').html(html).trigger('create');
		$('#discountCmp').listview('refresh');
	});
}

/**
 * 折扣选择
 */
uo.chooseDiscount = function(c){
	Util.LM.show();
	uo.discounting = true;
	$.post('../OperateDiscount.do', {
		dataSource : 'setDiscount',
		orderId : uo.order.id,
		discountId : c.id
	}, function(data){
		Util.LM.hide();
		if(data.success){
			$('#popupDiscountCmp').popup({
				afterclose: function (event, ui) { 
					if(uo.discounting){
						Util.msg.alert({
							topTip : true,
							msg : '打折成功'
						});	
						//异步刷新账单
						initOrderData({table : uo.table});
						uo.discounting = false;
					}

				}
			});			
			//关闭折扣选择
			$('#popupDiscountCmp').popup('close');
		}else{
			Util.msg.alert({
				title : '提示',
				msg : '打折失败, 请刷新页面重试', 
				renderTo : 'orderFoodListMgr',
				time : 2
			});					
		}
	});	
}

/**
 * 补打总单
 */
uo.tempPayForPrintAll = function(){
	
	uo.tempPayForPrintAllAction = true;
	$('#updateFoodOtherOperateCmp').popup('close');
}

/**
 * 打开操作人数
 */
uo.showOperatePeople = function(){
	uo.updateCustom = true;
	
	$('#inputOrderCustomerCountSet').val(uo.order.customNum);
	$('#updateFoodOtherOperateCmp').popup('close');
}

/**
 * 关闭操作人数
 */
uo.closeOperatePeople = function(c){
	uo.updateCustom = false;
	$('#orderCustomerCountSet').popup({
		afterclose: function (event, ui) { 
			if(c && c.callback){
				c.callback();
			}
		}
	});	
	$('#orderCustomerCountSet').popup('close');
}

/**
 * 打开更多操作
 */
uo.openMoreOperate = function(){
	$('#updateFoodOtherOperateCmp').popup({
		afterclose: function (event, ui) { 
			if(uo.updateCustom){
				$('#orderCustomerCountSet').popup('open');
				firstTimeInput = true;
				focusInput = 'inputOrderCustomerCountSet';
				$('#inputOrderCustomerCountSet').focus();
				$('#inputOrderCustomerCountSet').select();
			}else if(uo.tempPayForPrintAllAction){
				Util.LM.show();
				$.post('../PrintOrder.do', {'tableID' : uo.order.table.id, 'printType' : 14}, function(result){
					Util.LM.hide();
					delete uo.tempPayForPrintAllAction;
					if(result.success){
						Util.msg.alert({
							msg : '操作成功',
							topTip : true
						});
					}else{
						Util.msg.alert({
							title : '错误',
							msg : result.msg,
							renderTo : 'orderFoodListMgr',
							time : 3
						});
					}		
				}).error(function() {
					Util.LM.hide();
					delete uo.tempPayForPrintAllAction;
					Util.msg.alert({
						msg : '操作失败, 请联系客服',
						renderTo : 'orderFoodListMgr'
					});		
				});				
			}
		}
	});	
}


/**
 * 确定修改餐桌人数
 */
uo.saveForChangePeople = function(){
	
	var num = $("#inputOrderCustomerCountSet").val();
	
	//更改页面端的的人数
	$("#customNumForUO").html("用餐人数：" + num);	
	
	uo.customNum = num;
	
	uo.updateCustom = true;
	uo.saveForUO();
};

/**
 * 工具栏的确定按钮,对整个页面信息提交
 */
uo.saveForUO = function(){
	//判断页面信息是否有改动
	if(uoCancelFoods.length == 0 && uo.order.customNum == uo.customNum){
		Util.msg.alert({
			topTip : true,
			msg : '账单没有修改，不能提交'
		});
	}else{
		var uoFood = uo.order.orderFoods;
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
 * 已点菜改单提交操作
 */
uo.submitUpdateOrderHandler = function(c){
	var orderFoods = c;
	if(orderFoods.length > 0){
		orderDataModel.tableID = uo.table.id;
		orderDataModel.customNum = uo.customNum;
		orderDataModel.orderFoods = orderFoods;
		orderDataModel.categoryValue = uo.order.categoryValue;
		orderDataModel.id = uo.order.id,
		orderDataModel.orderDate = uo.order.orderDate;

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
						if(uo.printTemp){
							uo.printTemp = false;
							uo.tempPayForUO();
						}else{
							if(uo.canceling){
								Util.msg.alert({
									topTip : true,
									msg : '退菜成功'
								});		
								updateTable({
									alias : uo.table.alias
								});											
								uo.canceling = false;
							}else if(uo.updateCustom){
								Util.msg.alert({
									topTip : true,
									msg : '账单修改成功'
								});	
								initOrderData({table : uo.table});
								uo.closeOperatePeople();
							}else if(uo.weighOperate){
								Util.msg.alert({
									topTip : true,
									msg : '账单修改成功'
								});	
								initOrderData({table : uo.table});
								uo.closeWeighOperate();
							}else{
								Util.msg.alert({
									title : data.title,
									msg : data.msg, 
									renderTo : 'orderFoodListMgr',
									time : 3,
									fn : function(btn){
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
								renderTo : 'orderFoodListMgr',
								fn : function(btn){
									if(btn == 'yes'){
										uoCancelFoods = [];
										initOrderData({table : uo.table});
									}
								}
							});
						}else{
							Util.msg.alert({
								msg : data.msg, 
								topTip : true
							});
						}

					}
			},
			error : function(request, status, err){
				Util.LM.hide();
				Util.msg.alert({
					title : '温馨提示',
					msg : err, 
					renderTo : 'orderFoodListMgr',
					time : 3
				});
			}
		});
	}else if(orderFoods.length == 0){
		Util.msg.alert({
			topTip : true,
			msg : '没有任何菜品，不能提交'
		});
	}
};

/**
 * 去点菜界面
 */
uo.goToCreateOrder = function(){
	//判断页面信息是否有改动
	if(uoCancelFoods.length == 0 && uo.order.customNum == uo.customNum){
		//FIXME 每点一次餐台都去更新菜品
//		initFoodData();
		
		of.show({
			table : uo.table,
			order : uo.order,
			callback : function(){
				/*initTableData();
				uo.cancelForUO();*/
				location.href = '#orderFoodListMgr';
				//异步刷新账单
				initOrderData({table : uo.table});
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
 * 工具栏的取消按钮,取消对该页面的修改操作
 */
uo.cancelForUO = function(){	
	//判断页面信息是否有改动
	if(uoCancelFoods.length == 0 && uo.order.customNum == uo.customNum){
		uoCancelFoods = [];
		uoFood = [];
		uo.back();
	}else{
		Util.msg.alert({
			title : '重要',
			msg : '账单信息已修改，“确定”将不保存这些改动，是否确定？',
			buttons : 'YESBACK',
			fn : function(btn){
				if(btn == 'yes'){
					uoCancelFoods = [];
					uoFood = [];
					uo.back();
				}
			}
		});
	}
};




