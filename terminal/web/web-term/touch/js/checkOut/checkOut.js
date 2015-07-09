//已点菜界面数据对象
var uo = {
	table : {},
	order : {},
	uoFood : [],
	reasons : [],
	discounts : [],
	selectedFood : {}
},
	//存放退菜的数组
	uoCancelFoods = [],
	/**
	 * 元素模板
	 */
	//已点菜列表
	orderFoodListCmpTemplet = '<tr class="{isComboFoodTd}">'
		+ '<td>{dataIndex}</td>'
		+ '<td ><div class={foodNameStyle}>{name}</div></td>'
		+ '<td>{count}<img style="margin-top: 10px;margin-left: 5px;display:{isWeight}" src="images/weight.png"></td>'
		+ '<td><div style="height: 25px;overflow: hidden;">{tastePref}</div></td>'
		+ '<td>{unitPrice}</td>'
	//	+ '<td>{totalPrice}</td>'
		+ '<td>{orderDateFormat}</td>'
		+ '<td>' + '{comboFoodOpe}'
		+ 		'<div data-role="controlgroup" data-type="horizontal" class="{isHideOpe}">'
	    + 			'<a onclick="uo.openCancelFoodCmp({event:this})" data-index={dataIndex} data-role="button" data-theme="b">退菜</a>'
	    +			'<a onclick="uo.transFoodForTS({event:this})" data-index={dataIndex} data-role="button" data-theme="b">转菜</a>'
	    +			'<a  data-index={dataIndex} data-role="button" data-theme="b"  data-rel="popup"  data-transition="pop" onclick="uo.openOrderFoodOtherOperate({event:this})">更多</a>'
	    +		'</div>'
	    +'</td>'
		+ '<td>{waiter}</td>'
		+ '</tr>';

	function initSearchTables(c){
		var html = [];
		for (var i = 0; i < c.data.length; i++) {
			
			var aliasOrName;
			if(c.data[i].categoryValue == 1){
				aliasOrName = c.data[i].alias
			}else{
				aliasOrName = '<font color="green">'+ c.data[i].categoryText +'</font>'
			}
			html.push(tableCmpTemplet.format({
				dataIndex : i,
				id : c.data[i].id,
				click : 'ts.toOrderFoodOrTransFood({alias:'+ c.data[i].alias +',id:'+ c.data[i].id +'})',
				alias : aliasOrName,
				theme : c.data[i].statusValue == '1' ? "e" : "c",
				name : c.data[i].name == "" || typeof c.data[i].name != 'string' ? c.data[i].alias + "号桌" : c.data[i].name,
				tempPayStatus : c.data[i].isTempPaid? '暂结' : '&nbsp;&nbsp;',
				bookTableStatus : c.data[i].isBook? '订' : '',
				tempPayStatusClass : navigator.userAgent.indexOf("Firefox") >= 0?'tempPayStatus4Moz':'tempPayStatus'
			}));	
		}
		$('#divSelectTablesForTs').html(html.join(''));
		$('#divSelectTablesForTs a').buttonMarkup( "refresh" );
	}

	
$(document).on("pagebeforeshow","#orderFoodListMgr",function(){
	if(!jQuery.isEmptyObject(uo.table) && uo.fromBack){
		delete uo.fromBack;
		initOrderData({table : uo.table});
	}
});	
	

/**
 * 显示已点菜界面函数, 入口
 * @param {object} c  
 */
uo.entry = function(c){
	
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
	
	//是否为预订台
	if(c.table.isBook){
		$('#uo_btnCheckoutBook').show();
	}else{
		$('#uo_btnCheckoutBook').hide();
	}	
	
	//设置会员动态popup控件
	uo.setMemberReadCmp();
	
};
	
/**
 * 初始化菜单数据，存放在uoFood数组中
 * @param {object} data 餐桌对象
 */
function initOrderData(c){
	// 加载菜单数据
	$.ajax({
		url : '../QueryOrderByCalc.do',
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
			    c.createrOrder == 'createrOrder' ? of.entry({
					table : uo.table,
					order : uo.order,
					orderFoodOperateType : 'normal',
					callback : function(){
						location.href = '#orderFoodListMgr';
						//异步刷新账单
						initOrderData({table : uo.table});
					}
				}) : null;
									
			}else{
				//清空账单信息
				$("#divNorthForUpdateOrder").html('');
				$('#orderFoodListBody').html('');
				$("#spanToTempPayStatus").html('');
				Util.msg.alert({
					title : data.title,
					msg : data.msg,
					renderTo : 'orderFoodListMgr',
					time : 3,
					fn : function(){
						window.history.back(-1);
					}
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
			name : uo.order.orderFoods[i].foodName,
			count : uo.order.orderFoods[i].count,
			isWeight : (uo.order.orderFoods[i].status & 1 << 7) != 0 ? 'initial' : 'none',
			hasWeigh : (uo.order.orderFoods[i].status & 1 << 7) != 0 ?'orderFoodMoreOperateCmp':'',
			tastePref : uo.order.orderFoods[i].tasteGroup.tastePref,
			unitPrice : uo.order.orderFoods[i].unitPrice.toFixed(2) + (uo.order.orderFoods[i].isGift?'&nbsp;[<font style="font-weight:bold;">已赠送</font>]':''),
//			totalPrice : uo.order.orderFoods[i].totalPrice.toFixed(2),
			orderDateFormat : uo.order.orderFoods[i].orderDateFormat.substring(11),
			waiter : uo.order.orderFoods[i].waiter ,
			comboFoodOpe : '',
			isHideOpe : "",
			isComboFoodTd : "",
			foodNameStyle : "commonFoodName"
		});
		
		if((uo.order.orderFoods[i].status & 1 << 5) != 0){
			var combo = uo.order.orderFoods[i].combo;
			
			for (var j = 0; j < combo.length; j++) {
				html += orderFoodListCmpTemplet.format({
					dataIndex : '',
					id : combo[j].comboFood.id,
					name : '┕' + combo[j].comboFoodDesc,
					count : combo[j].comboFood.amount,
					isWeight : (combo[j].comboFood.status & 1 << 7) != 0 ? 'initial' : 'none',
					hasWeigh : (combo[j].comboFood.status & 1 << 7) != 0 ?'orderFoodMoreOperateCmp':'',
					tastePref : combo[j].tasteGroup ? combo[j].tasteGroup.tastePref : "无口味",
					unitPrice : "",
//					totalPrice : combo.comboFood.totalPrice.toFixed(2),
					orderDateFormat : "",
					waiter : "",
					comboFoodOpe : '',
					isHideOpe : "none",
					isComboFoodTd : "comboFoodTd",
					foodNameStyle : "comboFoodName"
				});					
			}
			
		
		}
		
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
			"<span style = 'margin: 10px;'>餐台名： " + tableName + "</span>" +
			"<span style = 'margin: 10px;'>餐台号：" + uo.order.table.alias + " </span>" +
			"<span style = 'margin: 10px;'>账单号：" + uo.order.id + " </span>" +
			"<span style = 'margin: 10px;' id='customNumForUO'>用餐人数：" + uo.customNum + "</span>" +			
			(uo.order.comment ? ("<span style = 'margin: 10px;' >开台备注：" + uo.order.comment + "</span>") : "") +
		"</div>";
	$("#divNorthForUpdateOrder").html(html);
}

/**
 * 初始化页尾信息（菜品数量，消费总额）
 */
uo.showDescForUpdateOrder = function(){
	var html = "", memberSpan = "";
	if(uo.orderMember){
		if(uo.orderMember.isRaw){
			memberSpan = '<span style = "margin-left: 20px;">当前会员：<font style="text-decoration: underline;cursor: pointer;color:blue" onclick="ts.member.memberInfoBind(\'loadMemberBind4Checkout\', \''+ uo.orderMember.name +'\')">' + uo.orderMember.name +"(点击绑定)</font></span>";
		}else{
			memberSpan = '<span style = "margin-left: 20px;">当前会员：<font style="color:green">' + uo.orderMember.name +"</font></span>";
		}
	}
	
	html = (uo.order.coupon?"<span style = 'margin-left: 20px;'>当前优惠劵：<font color='green'>" + uo.order.coupon.couponType.name + (uo.order.coupon.couponType.price > 0? " (¥" + uo.order.coupon.couponType.price +")" : "") +"</font></span>" : "") + 
		memberSpan +
		(uo.order.discount?"<span style = 'margin-left: 20px;'>当前折扣：<font color='green'>" + uo.order.discount.name +"</font></span>" : "") +
		(uo.order.discounter ? "<span style = 'margin-left: 20px;'>折扣人：<font color='green'>" + uo.order.discounter + "</font></span><span style = 'margin-left: 20px;'>折扣时间：<font color='green'>" + uo.order.discountDate + "</font></span>" : "") ;
	$("#divDescForUpdateOrder").html(html);
	$("#spanTotalPriceUO").html('消费总额：<font color="green">¥'+ uo.order.actualPrice + "</font>");
	if(uo.order.tempPayStaff){
		$("#spanToTempPayStatus").html('暂结人：<font color="green">' + uo.order.tempPayStaff +'</font>，暂结时间：<font color="green">'+ uo.order.tempPayDate + "</font>");
	}else{
		$("#spanToTempPayStatus").html('');
	}
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

//设置会员动态popup控件
uo.setMemberReadCmp = function(){
	//会员信息来源
	if($('#paymentMgr .payment_searchMemberType').length > 0){
		$('#paymentMgr .payment_searchMemberType').remove();
	}	
	if($('#orderFoodListMgr .payment_searchMemberType').length == 0){
		$('#orderFoodListMgr').append(payment_searchMemberTypeTemplet);
	}	
	
	//折扣
	if($('#paymentMgr .payment_popupDiscountCmp4Member').length > 0){
		$('#paymentMgr .payment_popupDiscountCmp4Member').remove();
	}	
	if($('#orderFoodListMgr .payment_popupDiscountCmp4Member').length == 0){
		$('#orderFoodListMgr').append(payment_popupDiscountCmp4MemberTemplet);
	}
	
	//价格方案
	if($('#paymentMgr .payment_popupPricePlanCmp4Member').length > 0){
		$('#paymentMgr .payment_popupPricePlanCmp4Member').remove();
	}	
	if($('#orderFoodListMgr .payment_popupPricePlanCmp4Member').length == 0){
		$('#orderFoodListMgr').append(payment_popupPricePlanCmp4MemberTemplet);
	}	
	
	//优惠劵
	if($('#paymentMgr .payment_popupCouponCmp4Member').length > 0){
		$('#paymentMgr .payment_popupCouponCmp4Member').remove();
	}	
	if($('#orderFoodListMgr .payment_popupCouponCmp4Member').length == 0){
		$('#orderFoodListMgr').append(payment_popupCouponCmp4MemberTemplet);
	}		
}

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
		html += '<a data-role="button" data-index='+ i +' data-inline="true" data-theme="c" class="regionBtn" onclick="uo.selectCancelReason({event:this})">'+ data[i].reason +'</a>';
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
	
	$('#shadowForPopup').show();
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
	
	$('#shadowForPopup').hide();
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
		Util.msg.tip('退菜数目不正确');
		$("#inputCancelFoodSet").val(uo.operateFood.count);
		$("#inputCancelFoodSet").select();
		firstTimeInput = true;
	}else if(isNaN(num)){
		Util.msg.tip('数字不合规范.');
		$("#inputCancelFoodSet").val(uo.operateFood.count);
		$("#inputCancelFoodSet").select();
		firstTimeInput = true;
	}else if(num > uo.operateFood.count){
		Util.msg.tip('退菜数量不能大于原有数量');
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
		uoCancelFood.orderFoodId = uo.operateFood.orderFoodId;
		uoCancelFood.foodName = uo.operateFood.foodName;
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
 * 打开已点菜中单条菜更多操作
 */
uo.openOrderFoodOtherOperate = function(c){
	//获取选中行
	uo.selectedFood = uo.order.orderFoods[parseInt($(c.event).attr('data-index'))-1];
	
	var giftPrivileges = false;
	for (var i = 0; i < ln.staffData.role.privileges.length; i++) {
		if(ln.staffData.role.privileges[i].codeValue === 1003){
			giftPrivileges = true;
		}
	}
	if((uo.selectedFood.status & 1 << 3) != 0 && giftPrivileges && !uo.selectedFood.isGift){
		$('#btnGiftFood').show();
	}else{
		$('#btnGiftFood').hide();
	} 
	if((uo.selectedFood.status & 1 << 7) === 0){
		$('#btnWeighFood').hide();
	}else{
		$('#btnWeighFood').show();
	}
	
	$('#orderFood_moreOpe').listview('refresh');		
	
	$('#orderFoodMoreOperateCmp').popup('open');
	//动态使用popup时要动态设置popup控件位置
	$('#orderFoodMoreOperateCmp-popup').css({top:$(c.event).position().top, left:$(c.event).position().left});
}

/**
 * 催菜
 */
uo.hurriedFoodAction = function(){
	uo.selectedFood.isHurried = true;
	$('#orderFoodMoreOperateCmp').popup('close');
	uo.hurriedFood = true;
	setTimeout(function(){
		uo.submitUpdateOrderHandler({orderFoods:uo.order.orderFoods});
	}, 250);	
	
	
}

/**
 * 全单催菜
 */
uo.allFoodHurried = function(){
	var uoFood = uo.order.orderFoods;
	for(var x = 0; x < uoFood.length; x++){
		uoFood[x].isHurried = true;
	}	
	$('#updateFoodOtherOperateCmp').popup('close');
	uo.hurriedFood = true;
	setTimeout(function(){
		uo.submitUpdateOrderHandler({orderFoods:uoFood});
	}, 250);	
}


/**
 * 去称重
 */
uo.weighAction = function(){
	if((uo.selectedFood.status & 1 << 7) != 0 ){
		uo.weighOperate=true;
		//关闭更多控件,打开称重
		$('#orderFoodMoreOperateCmp').popup('close');
		setTimeout(function(){
			uo.openWeighOperate();
		}, 250);
	}else{
		Util.msg.tip('此菜品不可以称重');
	}
	
};

/**
 * 打开称重
 */
uo.openWeighOperate = function(){
	setTimeout(function(){
		$('#inputOrderFoodWeigh').focus();
		//显示菜名
		$('#weighFoodName').text(uo.selectedFood.foodName);		
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
	//删除动作
	delete uo.weighOperate;
}

/**
 * 称重操作
 */
uo.openWeighaction = function(){
	var count = $('#inputOrderFoodWeigh');
	if(!count.val()){
		Util.msg.tip('请输入称重数量');
		count.focus();
		return;
	}else if(isNaN(count.val())){
		Util.msg.tip('请输入正确的称重数量');
		count.focus();
		return;
	}else if(count.val() < uo.selectedFood.count){
		Util.msg.tip('称重数量不能少于原有数量');
		count.focus();
		return;
	}
	
	uo.selectedFood.count = count.val();
	//对更新的菜品和人数进行提交
	uo.submitUpdateOrderHandler({orderFoods:uo.order.orderFoods, notPrint:!$('#chkPrintWeigh').attr("checked")});	
}



/**
 * 去赠送
 */
uo.giftAction = function(){
	if((uo.selectedFood.status & 1 << 3) != 0 ){
		//关闭更多控件,打开赠送
		$('#orderFoodMoreOperateCmp').popup('close');
		setTimeout(function(){
			uo.openGiftOperate();
		}, 250);
	}else{
		Util.msg.tip('此菜品不可以赠送');
	}
	
};

/**
 * 打开赠送
 */
uo.openGiftOperate = function(){
	setTimeout(function(){
		firstTimeInput = true;
		$('#inputOrderFoodGift').val(uo.selectedFood.count);	
		$('#inputOrderFoodGift').select();
		//显示菜名
		$('#giftFoodName').text(uo.selectedFood.foodName);		
	}, 250);
	$('#orderFoodGiftCmp').parent().addClass('popup').addClass('in');
	$('#orderFoodGiftCmp').popup('open');
}

/**
 * 关闭赠送
 */
uo.closeGiftOperate = function(){
	$('#inputOrderFoodGift').val('');
	$('#orderFoodGiftCmp').popup('close');
}

/**
 * 赠送操作
 */
uo.openGiftaction = function(){
	var count = $('#inputOrderFoodGift');
	if(!count.val() || isNaN(count.val()) || count.val() < 0){
		Util.msg.tip('请输入正确的赠送数量');
		count.focus();
		return;
	}else if(count.val() > uo.selectedFood.count){
		Util.msg.tip('称重数量不能大于原有数量');
		count.focus();
		return;
	}
	
	uo.selectedFood.count = count.val();
	Util.LM.show();
	//提交赠送
	$.post('../OperateOrderFood.do', {
		dataSource : 'giftOrderFood',
		orderId : uo.order.id,
		giftFood : uo.selectedFood.id +','+ uo.selectedFood.count
	}, function(result){
		Util.LM.hide();
		if(result.success){
			Util.msg.tip(result.msg);
			initOrderData({table : uo.table});
			uo.closeGiftOperate();
		}else{
			Util.msg.tip('赠送失败');
		}
	});
}

/**
 * 打开备注
 */
uo.openCommentOperate = function(){
	//关闭更多
	$('#updateFoodOtherOperateCmp').popup('close');
	setTimeout(function(){
		$('#shadowForPopup').show();
		$('#orderFoodCommentCmp').show();
		$('#inputUpdateComment').val(uo.order.comment != "----" ? uo.order.comment : "");	
		$('#inputUpdateComment').select();
	}, 250);
	
}

/**
 * 关闭备注
 */
uo.closeComment = function(){
	$('#shadowForPopup').hide();
	$('#orderFoodCommentCmp').hide();
	if(YBZ_win){
		YBZ_win.close();	
	}
}

/**
 * 修改备注
 */
uo.saveComment = function(){
	Util.LM.show();
	$.post('../OperateOrderFood.do', {
		dataSource : 'updateComment',
		orderId : uo.order.id,
		comment : $('#inputUpdateComment').val()
	},function(data){
		Util.LM.hide();
		if(data.success){
			Util.msg.tip( '备注成功');	
			uo.closeComment();
			initOrderData({table : uo.table});
		}else{
			Util.msg.alert({
				title : '提示',
				msg : data.msg, 
				renderTo : 'orderFoodListMgr'
			});				
		}			
	});
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
	
	$('#transSomethingTitle').html(orderFood.foodName +" -- 请输入桌号，菜品数量确定转菜");
	
	ts.tf.id = orderFood.orderFoodId
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
		Util.msg.tip('请输入会员信息');			
		member.focus();
		return;
	}else if(member.val() <= 0 || isNaN(member.val())){
		Util.msg.tip('请输入正确的会员信息');			
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
					Util.msg.tip('会员注入成功');	
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
	$('#shadowForPopup').show();	
	
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
		//隐藏备注
		$('#tr4TxtTableComment').hide();
		//显示餐台选择
		$('#divSelectTablesForTs').show();
	}else if(ts.commitTableOrTran == 'member'){
		//显示为台号信息
		$("#txtTableNumForTS").attr("placeholder", "填写台号");
		//隐藏回删
		$('#td4CmpDeleteWord').hide();
	}else if(ts.commitTableOrTran == 'lookup'){
		//隐藏结账按钮
		$('#ts_toPaymentMgr').hide();
		$('#certain4searchTableCmps .ui-btn-text').html('确定');
		//去除3个按钮并排
		$('#searchTableCmpsFoot a').removeClass('tablePopbottomBtn');		
	}

	//不设置预订默认操作
	if(ts.commitTableOrTran != 'bookTableChoose' && ts.commitTableOrTran != 'addBookTableChoose'){
		//操作设置为默认
		ts.commitTableOrTran = 'table';
	}

	
	$('#transFoodCmp').hide();
	$('#shadowForPopup').hide();	
	
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
	Util.LM.show();
	$.post('../OperateOrderFood.do', {
		dataSource : 'transFood',
		orderId : uo.order.id,
		tableId : c.id,
		transFoods : (c.allTrans?c.allTrans:(ts.tf.id + ',' + ts.tf.count))		
	},function(data){
		Util.LM.hide();
		if(data.success){
			uo.closeTransOrderFood();
			Util.msg.tip(data.msg);			
			//刷新已点菜
			updateTable({id : uo.table.id});
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
					Util.msg.tip('操作成功');
					
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
						Util.msg.tip('打折成功');	
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
	//关闭后回调
	$('#updateFoodOtherOperateCmp').popup('close');
}

/**
 * 补打明细
 */
uo.printDetailPatch = function(){
	uo.printDetailPatchAction = true;
	//关闭后回调
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
			if(uo.updateCustom){//修改人数
				$('#orderCustomerCountSet').popup('open');
				firstTimeInput = true;
				focusInput = 'inputOrderCustomerCountSet';
				$('#inputOrderCustomerCountSet').focus();
				$('#inputOrderCustomerCountSet').select();
			}else if(uo.tempPayForPrintAllAction){//补打总单
				Util.LM.show();
				$.post('../PrintOrder.do', {'tableID' : uo.order.table.id, 'printType' : 14}, function(result){
					Util.LM.hide();
					delete uo.tempPayForPrintAllAction;
					if(result.success){
						Util.msg.tip( result.msg);
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
			}else if(uo.printDetailPatchAction){//补打明细
				Util.msg.alert({
					msg : '是否补打明细?',
					renderTo : 'orderFoodListMgr',
					buttons : 'yesback',
					certainCallback : function(){
						Util.LM.show();
						$.post('../PrintOrder.do', {'tableID' : uo.order.table.id, 'printType' : 15}, function(result){
							Util.LM.hide();
							delete uo.printDetailPatchAction;
							if(result.success){
								Util.msg.tip( result.msg);
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
							delete uo.printDetailPatchAction;
							Util.msg.alert({
								msg : '操作失败, 请联系客服',
								renderTo : 'orderFoodListMgr'
							});		
						});							
					},
					returnCallback : function(){
						delete uo.printDetailPatchAction;
					}
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
		Util.msg.tip('账单没有修改，不能提交');
	}else{
		var uoFood = uo.order.orderFoods;
		for(var x = 0; x < uoFood.length; x++){
			for(var y = 0; y < uoCancelFoods.length; y++){
				if(uoFood[x].orderFoodId == uoCancelFoods[y].orderFoodId && uoFood[x].tasteGroup.tastePref == uoCancelFoods[y].dishes){
					uoFood[x].count = parseFloat(uoFood[x].count + uoCancelFoods[y].count).toFixed(2);
					uoFood[x].cancelReason = uoCancelFoods[y].reason;
				}
			}
		}
		//对更新的菜品和人数进行提交
		uo.submitUpdateOrderHandler({orderFoods:uoFood});	
	}
};

/**
 * 已点菜改单提交操作
 */
uo.submitUpdateOrderHandler = function(c){
	var orderFoods = c.orderFoods;
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
				type : 7,
				notPrint : c.notPrint?c.notPrint:false
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
								Util.msg.tip('退菜成功');		
								updateTable({
									alias : uo.table.alias
								});											
								uo.canceling = false;
							}else if(uo.updateCustom){
								Util.msg.tip('账单修改成功');	
								initOrderData({table : uo.table});
								uo.closeOperatePeople();
							}else if(uo.weighOperate){
								Util.msg.tip( '账单修改成功');	
								initOrderData({table : uo.table});
								uo.closeWeighOperate();
							}else if(uo.hurriedFood){
								Util.msg.tip( '催菜成功');	
								initOrderData({table : uo.table});
								delete uo.hurriedFood;
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
							Util.msg.tip( data.msg);
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
		Util.msg.tip('没有任何菜品，不能提交');
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
		
		of.entry({
			table : uo.table,
			order : uo.order,
			orderFoodOperateType : 'normal',
			callback : function(){
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
	
	uoCancelFoods = [];
	uoFood = [];
	uo.back();
	
	//判断页面信息是否有改动
//	if(uoCancelFoods.length == 0 && uo.order.customNum == uo.customNum){
//		uoCancelFoods = [];
//		uoFood = [];
//		uo.back();
//	}else{
//		Util.msg.alert({
//			title : '重要',
//			msg : '账单信息已修改，“确定”将不保存这些改动，是否确定？',
//			buttons : 'YESBACK',
//			fn : function(btn){
//				if(btn == 'yes'){
//					uoCancelFoods = [];
//					uoFood = [];
//					uo.back();
//				}
//			}
//		});
//	}
};



/**
 * 打开微信会员绑定
 */
uo.memberInfoBind1 = function(){
	//渲染完善会员资料窗口
	$('#finishMemberInfo').trigger('create').trigger('refresh');	
	
	if(uo.memberInfoBind.typeList){
		$('#shadowForPopup').show();
		$('#finishMemberInfo').show();	
		
		setTimeout(function(){
			$('#fm_txtMemberName').val(uo.orderMember.name);
			$('#fm_txtMemberName').select();
		}, 250);		
	}else{
		Util.LM.show();
		$.ajax({
			url : '../QueryMemberType.do',
			type : 'post',
			async:false,
			data : {dataSource : 'normal'},
			success : function(jr, status, xhr){
				if(jr.success){
					Util.LM.hide();
					var html = [];
					var weixin;
					for (var i = 0; i < jr.root.length; i++) {
						if(jr.root[i].name == "微信会员"){
							weixin = jr.root[i];
							continue;
						}
						html.push('<option value={id} data-attrVal={attrVal} data-chargeRate={chargeRate}>{name}</option>'.format({
							id : jr.root[i].id,
							attrVal : jr.root[i].attributeValue,
							chargeRate : jr.root[i].chargeRate,
							name : jr.root[i].name
						}));
					}
					//加上微信会员选项
					html.unshift('<option value={id} data-attrVal={attrVal} data-chargeRate={chargeRate}>{name}</option>'.format({
						id : weixin.id,
						attrVal : weixin.attributeValue,
						chargeRate : weixin.chargeRate,
						name : weixin.name
					}));
					$('#fm_comboMemberType').html(html.join("")).trigger('create').selectmenu('refresh');
					
					uo.memberInfoBind.typeList = true;
					uo.memberInfoBind.firstOption = weixin;
					
					$('#shadowForPopup').show();
					$('#finishMemberInfo').show();	
					
					setTimeout(function(){
						$('#fm_txtMemberName').val(uo.orderMember.name);
						$('#fm_txtMemberName').select();
						
					}, 250);
				}else{
					Util.msg.alert({
						renderTo : 'orderFoodListMgr',
						msg : jr.msg
					});
				}
			},
			error : function(request, status, err){
				Util.msg.alert({
					renderTo : 'orderFoodListMgr',
					msg : request.msg
				});
			}
		}); 
	}
}

/**
 * 
 */
uo.memberInfoBind = function(id){
	uo.memberInfoBind.id = id;
	$('#'+id).load("memberBind.jsp",{memberName:uo.orderMember.name}, function(){
		$('#finishMemberInfo').trigger('create').trigger('refresh');	
	});
}

/**
 * 关闭微信绑定
 */
uo.closeMemberInfoBind = function(){
	$('#shadowForPopup').hide();
//	$('#finishMemberInfo').hide();
//	$('#divConfirmMember').hide();
	
	$('#finishMemberInfo').remove();
	
	$('#weixinMemberCertain .ui-btn-text').html('确定');
	$('#weixinMemberCertain')[0].onclick = uo.readMemberByDetail;
	$('#fm_txtMemberMobile').val('');
	$('#fm_numberMemberCard').val('');
	$('#fm_dateMemberBirthday').val('');
	
	//关闭易笔字
	YBZ_win = YBZ_win || '';
	if(YBZ_win){
		YBZ_win.close();
	}
	//关闭数字键盘
	$('#numberKeyboard').hide();
	
	$('#fm_txtMemberMobile').removeAttr("disabled").parent().removeClass('ui-disabled');
	$('#fm_numberMemberCard').removeAttr('disabled').parent().removeClass('ui-disabled');
	
	//默认微信会员
	$('#fm_comboMemberType').val(uo.memberInfoBind.firstOption.id);
	$('#fm_comboMemberType').selectmenu('refresh');
	

	
	
}

/**
 * 绑定前查找
 */
uo.readMemberByDetail = function(){
	var mobile = $('#fm_txtMemberMobile').val();
	var card = $('#fm_numberMemberCard').val();
	
	if(!mobile && !card){
		Util.msg.tip('请填写手机号或实体卡号');
		return;
	}
	
	Util.LM.show();
	$.post('../OperateMember.do', {
		dataSource : 'checkMember',
		name : $('#fm_txtMemberName').val(),
		mobile : mobile,
		card : card,
		sex : $('input[name="fm_comboMemberSex"]:checked').val(),
		birthday : $('#fm_dateMemberBirthday').val(),
		type : $('#fm_comboMemberType').val()
	}, function(result){
		Util.LM.hide();
		if(result.success){
			if(result.root.length > 0){
				$("#divConfirmMember").slideDown("slow");
				$('#weixinMemberCertain .ui-btn-text').html('确认并绑定');
				$('#weixinMemberCertain')[0].onclick = uo.bindWeixinMember;
				uo.showOldMemberDetail(result.root[0]);
			}else{
				uo.bindWeixinMember();
			}
		}else{
			Util.msg.alert({
				renderTo : 'orderFoodListMgr',
				msg : result.msg
			});
		}
	}, 'json');
}

/**
 * 设置对比数据
 */
uo.showOldMemberDetail = function(m){
	$('#confirmMemberName').html(m.name);
	$('#confirmMembeMobile').html(m.mobile?m.mobile:"----");
	$('#confirmMembeCard').html(m.memberCard?m.memberCard:"----");
	$('#confirmMembeSex').html(m.sexText);
	$('#confirmMembeBirthday').html(m.birthdayFormat?m.birthdayFormat:"----");
	$('#confirmMembeType').html(m.memberType.name);
	
	//设置数据到input
	$('#fm_txtMemberName').val(m.name);
	$('#fm_txtMemberMobile').val(m.mobile);
	$('#fm_numberMemberCard').val(m.memberCard);
	$('#fm_dateMemberBirthday').val(m.birthdayFormat);
	
	$('input[name="fm_comboMemberSex"]').each(function(){
		if(this.value == m.sexValue){
			$(this).attr("checked",true).checkboxradio("refresh");
		}else{
			$(this).attr("checked",false).checkboxradio("refresh");
		}
	});
	
	$('#fm_comboMemberType').val(m.memberType.id);
	$('#fm_comboMemberType').selectmenu("refresh");
	
	$('#fm_txtMemberMobile').attr("disabled","disabled").parent().addClass('ui-disabled');
	$('#fm_numberMemberCard').attr('disabled',"true").parent().addClass('ui-disabled');
}

/**
 * 绑定微信会员
 */
uo.bindWeixinMember = function(){
	var mobile = $('#fm_txtMemberMobile').val();
	var card = $('#fm_numberMemberCard').val();
	
	if(!mobile && !card){
		Util.msg.tip('请填写手机号或实体卡号');
		return;
	}
	
	Util.LM.show();
	$.post('../OperateMember.do', {
		dataSource : 'bindWxMember',
		id : uo.order.memberId,
		orderId : uo.order.id,
		name : $('#fm_txtMemberName').val(),
		mobile : mobile,
		card : card,
		sex : $('input[name="fm_comboMemberSex"]:checked').val(),
		birthday : $('#fm_dateMemberBirthday').val(),
		type : $('#fm_comboMemberType').val()
	}, function(result){
		Util.LM.hide();
		if(result.success){
			uo.closeMemberInfoBind();
			//异步刷新账单
			initOrderData({table : uo.table});
			Util.msg.tip(result.msg);
		}else{
			Util.msg.alert({
				renderTo : 'orderFoodListMgr',
				msg : result.msg
			});
		}
	}, 'json');	
}




