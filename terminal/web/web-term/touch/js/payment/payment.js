//结账界面数据对象
var pm = {table : {}},
	//折扣,服务费方案, 付款方式
	discountData = [],  servicePlanData = [], payTypeData=[], restaurantData = [],
	//加载显示账单基础信息
	orderMsg = null,
	//赋值总额用于抹数计算
	checkOut_actualPrice,
	
	//普通付款或混合付款
	payType = 1, actualMemberID = -1,
	
	//付款状态
	isPaying = false,
	
	//筛选账单明细的条件
	lookupCondtion = "true",
	
	//当前折扣 
	calcDiscountID,
	
	//查询出来的菜品列表
	orderFoodDetails = [],
	
	//查找的会员
	member4Payment, member4Display,
	
	/**
	 * 元素模板
	 */
	//菜品列表
	payment_orderFoodListCmpTemplet = '<tr class="{isComboFoodTd}">'
		+ '<td>{dataIndex}</td>'
		+ '<td ><div class={foodNameStyle}>{name}</div></td>'
		+ '<td>{count}<img style="margin-top: 10px;margin-left: 5px;display:{isWeight}" src="images/weight.png"></td>'
		+ '<td><div style="height: 45px;overflow: hidden;">{tastePref}</div></td>'
		+ '<td>{tastePrice}</td>'
		+ '<td>{unitPrice}</td>'
		+ '<td>{discount}</td>'
		+ '<td>{totalPrice}</td>'
		+ '<td>{orderDateFormat}</td>'
		+ '<td>{waiter}</td>'
		+ '</tr>',	
	//账单详细
	payment_lookupOrderDetailTemplet = '<tr>'
		+ '<td>{dataIndex}</td>'
		+ '<td ><div style="height: 30px;overflow: hidden;">{name}</div></td>'
		+ '<td>{unitPrice}</td>'
		+ '<td>{count}<img style="margin-top: 10px;margin-left: 5px;display:{isWeight}" src="images/weight.png"></td>'
		+ '<td><div style="height: 30px;overflow: hidden;">{tastePref}</div></td>'
		+ '<td>{tastePrice}</td>'
		+ '<td>{isGift}</td>'
		+ '<td>{discount}</td>'
		+ '<td>{kitchenName}</td>'
		+ '<td>{operation}</td>'
		+ '<td>{orderDateFormat}</td>'
		+ '<td>{waiter}</td>'
		+ '<td>{cancelReason}</td>'
		+ '</tr>';



pm.entry = function(c){
	if(!c || !c.table){
		Util.msg.alert({msg : '账单不存在', topTip: true});
		return;
	}
	
	pm.table = c.table;
	
	location.href = "#paymentMgr";
	
 	loadSystemSettingData();
	
 	//加载账单信息
	refreshOrderData({calc : false});
	
	//加载折扣
	loadDiscountData();
	
	//加载服务费方案
	loadServicePlanData();
	
	//加载混合结账付款方式
	loadPayTypeData(); 
	
	//设置会员动态popup控件
	//会员信息来源
	if($('#orderFoodListMgr .payment_searchMemberType').length > 0){
		$('#orderFoodListMgr .payment_searchMemberType').remove();
	}    
	if($('#paymentMgr .payment_searchMemberType').length == 0){
		$('#paymentMgr').append(payment_searchMemberTypeTemplet);
	}	

	//会员折扣
	if($('#orderFoodListMgr .payment_popupDiscountCmp4Member').length > 0){
		$('#orderFoodListMgr .payment_popupDiscountCmp4Member').remove();
	} 
	if($('#paymentMgr .payment_popupDiscountCmp4Member').length == 0){
		$('#paymentMgr').append(payment_popupDiscountCmp4MemberTemplet);
	}
	
	//会员价格方案
	if($('#orderFoodListMgr .payment_popupPricePlanCmp4Member').length > 0){
		$('#orderFoodListMgr .payment_popupPricePlanCmp4Member').remove();
	}	 
	if($('#paymentMgr .payment_popupPricePlanCmp4Member').length == 0){
		$('#paymentMgr').append(payment_popupPricePlanCmp4MemberTemplet);
	}		
	
	//优惠劵
	if($('#orderFoodListMgr .payment_popupCouponCmp4Member').length > 0){
		$('#orderFoodListMgr .payment_popupCouponCmp4Member').remove();
	}	 
	if($('#paymentMgr .payment_popupCouponCmp4Member').length == 0){
		$('#paymentMgr').append(payment_popupCouponCmp4MemberTemplet);
	}		
	
};

//加载账单数据
function refreshOrderData(_c){
	Util.LM.show();
	_c = _c || {};
	$.ajax({
		url : "../QueryOrderByCalc.do",
		type : 'post',
		data : {
			tableID : pm.table.id,
			orderID : orderMsg?orderMsg.id:'',
			calc : typeof _c.calc == 'boolean' ? _c.calc : true,
			discountID : calcDiscountID,
			customNum : pm.table.customNum
		},
		success : function(jr, status, xhr){
			Util.LM.hide();
			if(jr.success){
				// 加载已点菜
				checkOutData = jr;
				checkOutData.root = jr.other.order.orderFoods;
				// 加载显示账单基础信息
				orderMsg = jr.other.order;
				//赋值总额用于抹数计算
				checkOut_actualPrice = jr.other.order.actualPrice;
				
				loadOrderBasicMsg();
			}else{
				Util.msg.alert({
					msg : jr.msg,
					renderTo : 'paymentMgr'
				});
				$('#lab_replaceBtn').show();
			}
		},
		error : function(request, status, err){
		}
	}); 	
	
}

/**
 * 显示账单信息
 */
function loadOrderBasicMsg(){
	//显示左边价钱
	document.getElementById("totalPrice").innerHTML = checkDot(orderMsg.totalPrice)?parseFloat(orderMsg.totalPrice).toFixed(2) : orderMsg.totalPrice;
	document.getElementById("actualPrice_td_payment").innerHTML = checkDot(orderMsg.actualPrice)?parseFloat(orderMsg.actualPrice).toFixed(2) : orderMsg.actualPrice;
	document.getElementById("forFree").innerHTML = checkDot(orderMsg.giftPrice)?parseFloat(orderMsg.giftPrice).toFixed(2) : orderMsg.giftPrice;
	document.getElementById("spanCancelFoodAmount").innerHTML = checkDot(orderMsg.cancelPrice)?parseFloat(orderMsg.cancelPrice).toFixed(2) : orderMsg.cancelPrice;
	document.getElementById("discountPrice").innerHTML = checkDot(orderMsg.discountPrice)?parseFloat(orderMsg.discountPrice).toFixed(2) : orderMsg.discountPrice;
	if(orderMsg.categoryValue != 4 && orderMsg.cancelPrice > 0){
		$('#spanSeeCancelFoodAmount').show();	
		$('#lab_replaceCancelBtn').hide();
	}else{
		$('#lab_replaceCancelBtn').show();
		$('#spanSeeCancelFoodAmount').hide();
	}	
	
	if(orderMsg.giftPrice > 0){
		$('#spanSeeGiftFoodAmount').show();	
		$('#lab_replaceGiftBtn').hide();
	}else{
		$('#lab_replaceGiftBtn').show();
		$('#spanSeeGiftFoodAmount').hide();
	}	
	
	if(orderMsg.discountPrice > 0){
		$('#spanSeeDiscountFoodAmount').show();	
		$('#lab_replaceDiscountBtn').hide();
	}else{
		$('#lab_replaceDiscountBtn').show();
		$('#spanSeeDiscountFoodAmount').hide();
	}		
	
	//清空抹数和备注
	$('#erasePrice_input_payment').val('');
	$('#remark').val('');
	
	//账单基础信息
	$('#orderIdInfo').html('结账 -- 账单号:<font color="#f7c942">' + orderMsg.id + '</font> ' + (orderMsg.isWeixinOrder?'(<span id="showWeixinOrder" style="font-size:15px;font-weight:bold;color:green;text-decoration:underline">微信账单</span>)' : ''));
	if(orderMsg.category != 4){
		$('#orderTableInfo').html('餐桌号:<font color="#f7c942">' + orderMsg.table.alias + '</font>&nbsp;' + (pm.table.name?'<font color="#f7c942" >(' + pm.table.name +')</font>' :''));
	}
	document.getElementById('spanDisplayCurrentServiceRate').innerHTML = (orderMsg.serviceRate*100)+'%';
	$('#orderCustomNum').html(orderMsg.customNum > 0 ? orderMsg.customNum : 1);
	$('#remark').val(orderMsg.comment && orderMsg.comment != '----' ? orderMsg.comment : '');
	
	//会员 & 折扣 & 优惠劵
	var discountDesc = '当前折扣:<font style="color:green;font-weight:bold;">'+ orderMsg.discount.name + '</font>';
	if(orderMsg.discounter){
		discountDesc += ', 折扣人:<font style="color:green;font-weight:bold;">'+ orderMsg.discounter + '</font>';
		discountDesc += ', 折扣时间:<font style="color:green;font-weight:bold;">'+ orderMsg.discountDate + '</font>';
	}
	$('#orderDiscountDesc').html(discountDesc);
	
	if(orderMsg.couponPrice > 0){
		$('#orderCouponInfo').html('当前优惠券:<font style="color:green;font-weight:bold;">'+ orderMsg.coupon.length + '张, 共¥' + orderMsg.couponPrice + '</font>');
	}else{
		$('#orderCouponInfo').html('');
	}
	
	if(orderMsg.memberId && orderMsg.memberId > 0){
		//设置会员结账按钮
		$('#btnPayByMember .ui-btn-text').html('会员余额');
		$('#btnPayByMember').buttonMarkup('refresh');
		
		$.post('../QueryMember.do', {dataSource : 'normal', id : orderMsg.memberId, forDetail : true}, function(result){
			if(result.success){
				member4Payment = result.root[0];
				//设置为已注入状态 
				member4Payment.hadSet = true;
				
				member4Display = Util.clone(member4Payment);
				
				var memberSpan = "";
				if(result.root[0].isRaw){
					memberSpan = '<span style = "margin-left: 20px;">当前会员：<font style="text-decoration: underline;cursor: pointer;color:blue" onclick="ts.member.memberInfoBind(\'loadMemberBind4Payment\', \''+ result.root[0].name +'\')">' + result.root[0].name +"(点击绑定)</font></span>";
				}else{
					memberSpan = '<span style = "margin-left: 20px;">当前会员：<font style="color:green">' + result.root[0].name +"</font></span>";
				}
				
				$('#orderMemberDesc').html(memberSpan);
			}
		});
	}else{
		member4Payment = null;
		member4Display = null;
		$('#orderMemberDesc').html('');
		//设置会员结账按钮
		$('#btnPayByMember .ui-btn-text').html('读取会员');
		$('#btnPayByMember').buttonMarkup('refresh');		
	}
	
	//微信账单
	$('#showWeixinOrder').hover(function(){
		if(!weixinOrderDetailWin.loadOrder){
			loadWeixinOrderDetail();
			weixinOrderDetailWin.loadOrder = true;
		}
		weixinOrderDetailWin.setPosition($('#showWeixinOrder').position().left + 475, $('#showWeixinOrder').position().top + 60);
		weixinOrderDetailWin.show();		
		
	}, function(){
		weixinOrderDetailWin.hide();
	});		
	
	//菜品列表
	var html = [];
	for(var i = 0; i < checkOutData.root.length; i++){
		html.push(payment_orderFoodListCmpTemplet.format({
			dataIndex : i + 1,
			id : checkOutData.root[i].id,
			name : checkOutData.root[i].foodName + ((checkOutData.root[i].status & 1 << 7) != 0 ? '<font color="red">[称重确认]</font>' : ''),
			count : checkOutData.root[i].count,
			isWeight : (checkOutData.root[i].status & 1 << 7) != 0 ? 'initial' : 'none',
			tastePref : checkOutData.root[i].tasteGroup.tastePref,
			tastePrice : checkOutData.root[i].tasteGroup.tastePrice,
			unitPrice : (checkOutData.root[i].unitPrice + checkOutData.root[i].tasteGroup.tastePrice).toFixed(2),
			discount : checkOutData.root[i].discount,
			totalPrice : checkOutData.root[i].totalPrice.toFixed(2),
			orderDateFormat : checkOutData.root[i].orderDateFormat.substring(11),
			waiter : checkOutData.root[i].waiter,
			isComboFoodTd : "",
			foodNameStyle : "commonFoodName"
		}));
		
		if((checkOutData.root[i].status & 1 << 5) != 0){
			var combo = checkOutData.root[i].combo;
			
			for (var j = 0; j < combo.length; j++) {
				html.push(payment_orderFoodListCmpTemplet.format({
					dataIndex : '',
					id : combo[j].comboFood.id,
					name : '┕' + combo[j].comboFoodDesc,
					count : combo[j].comboFood.amount,
					isWeight : (combo[j].comboFood.status & 1 << 7) != 0 ? 'initial' : 'none',
					tastePref : combo[j].tasteGroup ? combo[j].tasteGroup.tastePref : "无口味",
					tastePrice : combo[j].tasteGroup ? combo[j].tasteGroup.tastePrice : 0,
					unitPrice : "",
					discount : "",
					totalPrice : "",
					orderDateFormat : "",
					waiter : "",
					isComboFoodTd : "comboFoodTd",
					foodNameStyle : "comboFoodName"
				}));					
			}
		}
		
	}			
	
	$('#payment_orderFoodListBody').html(html.join("")).trigger('create');	
}

/**
 * 加载折扣方案信息
 */
function loadDiscountData(_c){
	if(_c == null || typeof _c == 'undefined'){
		_c = {};
	}
	$.ajax({
		url : "../QueryDiscount.do",
		type : 'post',
		data : {
			dataSource : 'role'
		},
		success : function(jr, status, xhr){
			if(jr.success){
				discountData = jr.root;
				// 设置默认显示折扣方案
				for(var i = 0; i < discountData.length; i++){
					if(discountData[i].isDefault == 1){
						defaultsID = discountData[i].id;
						break;
					}else if(discountData[i].status == 2){
						defaultsID = discountData[i].id;
					}
				}	
				
				var html = '';
				for (var i = 0; i < jr.root.length; i++) {
					html += '<li data-icon="false" class="tempFoodKitchen" onclick="setDiscountPlan({id:'+ jr.root[i].id +'})"><a >'+ jr.root[i].name +'</a></li>';
				}
				$('#payment_discountCmp').html(html).trigger('create');
				$('#payment_discountCmp').listview('refresh');				
			}
		},
		error : function(request, status, err){
		}
	});	
}

/**
 * 加载服务费方案
 */

//记载服务费的function
function loadServicePlanData(){
	$.ajax({
		url : "../QueryServicePlan.do",
		type : 'post',
		data : {
			dataSource : 'planTree'
		},
		dataType : 'json',
		success : function(jr, status, xhr){
		},
		error : function(request, status, err){//FIXME 不是在success中获取
			var jr = eval("("+ request.responseText +")");
			var html = '';
			for (var i = 0; i < jr.length; i++) {
				html += '<li data-icon="false" class="tempFoodKitchen" onclick="setServicePlan({id:'+ jr[i].planId +'})"><a >'+ jr[i].planName +'</a></li>';
			}
			$('#payment_serviceCmp').html(html).trigger('create');
			$('#payment_serviceCmp').listview('refresh');				
		}
	}); 	
}


/**
 * 加载付款方式
 */
function loadPayTypeData(){
	$.ajax({
		url : "../QueryPayType.do",
		type : 'post',
		data : {
			dataSource : 'exceptMember'
		},
		success : function(jr, status, xhr){
			if(jr.success){
				payTypeData = jr.root;
			}
		},
		error : function(request, status, err){
		}
	}); 	
}

/**
 * 加载抹数
 */
function loadSystemSettingData(){
	$.ajax({
		url : "../QuerySystemSetting.do",
		type : 'post',
		success : function(jr, status, xhr){
			if(jr.success){
				restaurantData = jr.other.systemSetting;
				if(restaurantData.setting.eraseQuota > 0){
					
					$('#tr4EraseQuota').show();
					$('#font_showEraseQuota').html(restaurantData.setting.eraseQuota);
				}else{
					$('#tr4EraseQuota').hide();
					$('#font_showEraseQuota').html('');
					
				}
			} else {
			}
		},
		error : function(request, status, err){
		}
	}); 
	
}

/**
 * 设置折扣
 * @param c
 */
function setDiscountPlan(c){
	//关闭折扣选择
	$('#payment_popupDiscountCmp').popup('close');
	calcDiscountID = c.id;
	Util.LM.show();
	$.ajax({
		url : "../OperateDiscount.do",
		type : 'post',
		data : {
			dataSource : 'setDiscount',
			orderId : orderMsg.id, 
			discountId : calcDiscountID 
		},
		dataType : 'json',
		success : function(jr, status, xhr){
			Util.LM.hide();
			if(jr.success){
				refreshOrderData({calc:true});
			}else{
				Util.msg.alert({
					msg : jr.msg,
					renderTo : 'paymentMgr'
				});
			}
		},
		error : function(request, status, err){
		}
	}); 	
}

/**
 * 设置服务费方案
 * @param c
 */
function setServicePlan(c){
	//关闭折扣选择
	$('#payment_popupServiceCmp').popup('close');
	//设置服务费方案
	$.ajax({
		url : "../OperateOrderFood.do",
		type : 'post',
		data : {
			dataSource: 'service',
			orderId : orderMsg ? orderMsg.id : '',
			planId : c.id
		},
		success : function(jr, status, xhr){
			Util.LM.hide();
			if(jr.success){
				//刷新页面
				refreshOrderData({calc : true});
			}else{
				Util.msg.alert({
					msg : jr.msg,
					renderTo : 'paymentMgr'
				});
			}
		},
		error : function(request, status, err){
		}
	}); 	

}

function loadOrderDetail(){
	var tableId = 0;
	var orderId = 0;
	if($.mobile.activePage.attr( "id" ) == 'paymentMgr'){//结账界面中使用
		tableId = pm.table.id;
		orderId = orderMsg.id;
	}else if($.mobile.activePage.attr( "id" ) == 'orderFoodListMgr'){//已点菜界面使用
		tableId = uo.table.id;
		orderId = uo.order.id;
	}		
	Util.LM.show();
	$.ajax({
		url : "../QueryDetail.do",
		type : 'post',
		data : {
			queryType:'TodayByTbl',
			tableID: tableId,
			orderID : orderId
		},
		async : false,
		dataType : 'json',
		success : function(jr, status, xhr){
			Util.LM.hide();
			if(jr.success){
				orderFoodDetails = jr.root;
				pm.detailTotalPrice = jr.other.detailTotalPrice;
			}else{
				Util.msg.alert({
					msg : jr.msg,
					renderTo : 'paymentMgr'
				});
			}
		},
		error : function(request, status, err){
		}
	}); 	
}

function lookupOrderDetailByType(type){
	if(type){
		$('input[name=lookupType]').attr('checked', false);
		$('input[data-type='+type+']').attr('checked', true);
		$('input[name=lookupType]').checkboxradio('refresh');
	}

	$('#lab4CancelReasonOrComment').html('备注');
	if(type == 'detail_all'){
		lookupCondtion = "true"; 
	}else if(type == 'detail_cancel'){
		lookupCondtion = "tempData.operationValue == 2";
		//退菜时, 显示字符为退菜
		$('#lab4CancelReasonOrComment').html('退菜原因');
	}else if(type == 'detail_discount'){
		lookupCondtion = "tempData.discount < 1";
	}else if(type == 'detail_gift'){
		lookupCondtion = "tempData.isGift == true";
	}else if(type == 'detail_trans'){
		lookupCondtion = "tempData.operationValue == 3";
	}
	
	//账单查看
	var html = '';
	for(var i = 0, index = 1; i < orderFoodDetails.length; i++){
		if(eval(lookupCondtion)){
			html += payment_lookupOrderDetailTemplet.format({
				dataIndex : index,
				id : orderFoodDetails[i].id,
				name : orderFoodDetails[i].foodName,
				count : orderFoodDetails[i].count,
				isWeight : (orderFoodDetails[i].status & 1 << 7) != 0 ? 'initial' : 'none',
				isGift : orderFoodDetails[i].isGift?'是':'否',	
				discount : orderFoodDetails[i].discount,
				tastePref : orderFoodDetails[i].tasteGroup.tastePref,
				tastePrice : orderFoodDetails[i].tasteGroup.tastePrice,
				unitPrice : (orderFoodDetails[i].unitPrice + orderFoodDetails[i].tasteGroup.tastePrice).toFixed(2),
				cancelReason : orderFoodDetails[i].cancelReason.reason?orderFoodDetails[i].cancelReason.reason:'',
				totalPrice : orderFoodDetails[i].totalPrice.toFixed(2),
				orderDateFormat : orderFoodDetails[i].orderDateFormat.substring(11),
				kitchenName : orderFoodDetails[i].kitchen.name,
				operation : orderFoodDetails[i].operation,
				waiter : orderFoodDetails[i].waiter 
			});	
			index ++;
		}

	}		
	
	//设置总价
	$('#orderDetailTotalPrice').text(pm.detailTotalPrice);
	
	$('#payment_lookupOrderDetailBody').html(html).trigger('create');
	
	//账单基础信息
	if($.mobile.activePage.attr( "id" ) == 'paymentMgr'){//结账界面中使用
		$('#lookupOrderDetailHead_orderId').html('查看账单信息 -- 账单号:<font color="#f7c942">' + orderMsg.id + '</font> ');
		$('#lookupOrderDetailHead_table').html('餐桌号:<font color="#f7c942">' + orderMsg.table.alias + '</font>&nbsp;' + (pm.table.name?'<font color="#f7c942" >(' + pm.table.name +')</font>' :''));    		
	}else if($.mobile.activePage.attr( "id" ) == 'orderFoodListMgr'){//已点菜界面使用
		$('#lookupOrderDetailHead_orderId').html('查看账单信息 -- 账单号:<font color="#f7c942">' + uo.order.id + '</font> ');
		$('#lookupOrderDetailHead_table').html('餐桌号:<font color="#f7c942">' + uo.table.alias + '</font>&nbsp;' + (uo.table.name?'<font color="#f7c942" >(' + uo.table.name +')</font>' :''));		
	}	

	$('#shadowForPopup').show();
	$('#lookupOrderDetail').show();
	
}

function closeLookupOrderDetailWin(){
	$('#shadowForPopup').hide();
	$('#lookupOrderDetail').hide();	
}


/**
 * 
 * @param stype: 精确输入的条件, 是手机号或卡号
 */
function readMemberByCondtion(stype){
	var memberInfo = $('#txtMemberInfo4Read');
	
	if(!memberInfo.val()){
		Util.msg.alert({msg:'请填写会员相关信息', topTip:true});
		memberInfo.focus();
		return;
	}
	
	if(stype){
		$('#payment_searchMemberType').popup('close');
	}else{
		stype = '';
	}
	Util.LM.show();
	$.ajax({
		url : "../QueryMember.do",
		type : 'post',
		data : {
			dataSource:'normal',
			sType: stype,
			forDetail : true,
			memberCardOrMobileOrName:memberInfo.val()
		},
//		async : false,
		dataType : 'json',
		success : function(jr, status, xhr){
			Util.LM.hide();
			if(jr.success){
				if(jr.root.length == 1){
					if(member4Payment){
						member4Display = Util.clone(member4Payment);
					}
					member4Payment = jr.root[0];
					if(jr.other){
						member4Payment.coupons = jr.other.coupons;
					}
					//设置为最新读取的会员
					member4Payment.isFresh = true;
					Util.msg.alert({msg:'会员信息读取成功.', topTip:true});
					loadMemberInfo(member4Payment);
				}else if(jr.root.length > 1){
					$('#payment_searchMemberType').popup().popup('open');
					$('#payment_searchMemberType').css({top:$('#btnReadMember').position().top - 270, left:$('#btnReadMember').position().left-300});
					$('#payment_searchMemberTypeCmp').listview().listview('refresh');
				}else{
					Util.msg.alert({msg:'该会员信息不存在, 请重新输入条件后重试.', renderTo : 'paymentMgr', fn : function(){
						memberInfo.focus();
					}});
				}
			}else{
				Util.msg.alert({
					msg : jr.msg,
					renderTo : 'paymentMgr'
				});
			}
		},
		error : function(request, status, err){
		}
	}); 	
}

function loadMemberInfo(member){
	$('#payment4MemberName').text(member.name);
	$('#payment4MemberType').text(member.memberType.name);
	$('#payment4MemberBalance').text(member.totalBalance);
	$('#payment4MemberPoint').text(member.point);
	$('#payment4MemberPhone').text(member.mobile?member.mobile:'----');
	$('#payment4MemberCard').text(member.memberCard?member.memberCard:'----');	
	
	$('#payment4MemberDiscount').text(member.memberType.discount.name);
	$('#payment4MemberDiscount').attr('data-value', member.memberType.discount.id);
	
	var discounts = member.memberType.discounts;
	var pricePlans = member.memberType.pricePlans;
	
	var discountHtml = '', pricePlanHtml = '';
	for (var i = 0; i < discounts.length; i++) {
		discountHtml += '<li data-icon="false" class="popupButtonList" onclick="chooseMemberDiscount({id:'+ discounts[i].id +',name:\''+ discounts[i].name +'\'})"><a >'+ discounts[i].name +'</a></li>';
	}
	$('#payment_discountList4Member').html(discountHtml).trigger('create');
	
	if(pricePlans.length > 0){
		$('#payment4MemberPricePlan').text(member.memberType.pricePlan.name);
		$('#payment4MemberPricePlan').attr('data-value', member.memberType.pricePlan.id);
		for (var i = 0; i < pricePlans.length; i++) {
			pricePlanHtml += '<li data-icon="false" class="popupButtonList" onclick="chooseMemberPricePlan({id:'+ pricePlans[i].id +',name:\''+ pricePlans[i].name +'\'})"><a >'+ pricePlans[i].name +'</a></li>';
		}
		$('#payment_pricePlanList4Member').html(pricePlanHtml).trigger('create');
	}
	
	if(member.coupons && member.coupons.length > 0){
		var couponHtml = '';
		$('#payment4MemberCoupon').text('不使用');
		for (var i = 0; i < member.coupons.length; i++) {
			couponHtml += '<li data-icon="false" class="popupButtonList" onclick="chooseMemberCoupon({id:'+ member.coupons[i].couponId +',name:\''+ member.coupons[i].couponType.name +'\'})"><a >'+ member.coupons[i].couponType.name +'</a></li>';
		}
		$('#payment_couponList4Member').html(couponHtml).trigger('create');		
	}else{
		$('#payment4MemberCoupon').hide();
		$('#payment4MemberCoupon').attr('data-value', '');
		$('#link_payment_popupCouponCmp4Member').hide();
	}
}

function chooseMemberDiscount(c){
	$('#payment_popupDiscountCmp4Member').popup('close');
	var discount = $('#payment4MemberDiscount');
	discount.text(c.name);
	discount.attr('data-value', c.id);
}

function chooseMemberPricePlan(c){
	$('#payment_popupPricePlanCmp4Member').popup('close');
	var pricePlan = $('#payment4MemberPricePlan');
	pricePlan.text(c.name);
	pricePlan.attr('data-value', c.id);
}

function chooseMemberCoupon(c){
	$('#payment_popupCouponCmp4Member').popup('close');
	var coupon = $('#payment4MemberCoupon');
	coupon.text(c.name);
	coupon.attr('data-value', c.id);
}

/**
 * 为账单注入会员
 */
function setMemberToOrder(){
	if(!member4Payment || !member4Payment.isFresh){
		Util.msg.alert({
			topTip : true,
			msg : '请先读取会员'
		});
		return;
	}
	
	Util.LM.show();
	var discount = $('#payment4MemberDiscount');
	var pricePlan = $('#payment4MemberPricePlan');
	var coupon = $('#payment4MemberCoupon');
	var orderId = 0;
	
	if($.mobile.activePage.attr( "id" ) == 'paymentMgr'){//结账界面的orderId
		orderId = orderMsg.id;
	}else if($.mobile.activePage.attr( "id" ) == 'orderFoodListMgr'){//已点菜界面的orderId
		orderId = uo.order.id;
	}
	
	$.post('../OperateDiscount.do', {
		dataSource : 'setDiscount',
		orderId : orderId,
		memberId : member4Payment.id,
		discountId : discount.attr('data-value')?discount.attr('data-value'):'',
		pricePlan : pricePlan.attr('data-value')?pricePlan.attr('data-value'):'',
		coupon : coupon.attr('data-value')?coupon.attr('data-value'):''
	}, function(data){
		Util.LM.hide();
		if(data.success){
			
			if($.mobile.activePage.attr( "id" ) == 'paymentMgr'){//结账界面的orderId
				//异步刷新账单
				refreshOrderData();
			}else if($.mobile.activePage.attr( "id" ) == 'orderFoodListMgr'){//已点菜界面的orderId
				//异步刷新账单
				initOrderData({table : uo.table});
			}			
			closeReadMemberByCondtionWin();
			
			//设置当前查出会员为注入成功状态
			member4Payment.hadSet = true;
			member4Display = Util.clone(member4Payment);
			Util.msg.alert({
				topTip : true,
				msg : '会员注入成功'
			});	
			
		}else{
			Util.msg.alert({
				msg : '使用会员失败, 请刷新页面重试', 
				topTip : true
			});					
		}
	});		
}

function openReadMemberByCondtionWin(){
	if($.mobile.activePage.attr( "id" ) == 'paymentMgr'){//结账界面
		//异步刷新账单
//		refreshOrderData();
	}else if($.mobile.activePage.attr( "id" ) == 'orderFoodListMgr'){//已点菜界面
//		uo.setMemberReadCmp();
	}	
	
	$('#shadowForPopup').show();
	$('#readMemberWin').show();
	
	
	//设置button样式
	$('#btnReadMember').attr("data-theme", "b");
	$('#btnReadMember').buttonMarkup("refresh");
	$('#link_payment_popupDiscountCmp4Member').attr("data-theme", "e");
	$('#link_payment_popupDiscountCmp4Member').buttonMarkup("refresh");
	$('#link_payment_popupPricePlanCmp4Member').attr("data-theme", "e");
	$('#link_payment_popupPricePlanCmp4Member').buttonMarkup("refresh");
	$('#link_payment_popupCouponCmp4Member').attr("data-theme", "e");
	$('#link_payment_popupCouponCmp4Member').buttonMarkup("refresh");	
	
	NumKeyBoardAttacher.instance().attach($('#txtMemberInfo4Read')[0]);
	$('#txtMemberInfo4Read').focus();
}

function closeReadMemberByCondtionWin(){
	
	$('#shadowForPopup').hide();
	$('#readMemberWin').hide();
	
	$('#txtMemberInfo4Read').val('');
	$('#payment4MemberName').text('----');
	$('#payment4MemberType').text('----');
	$('#payment4MemberBalance').text('----');
	$('#payment4MemberPoint').text('----');
	$('#payment4MemberPhone').text('----');
	$('#payment4MemberCard').text('----');
	
	$('#payment4MemberDiscount').text('----');	
	$('#payment4MemberPricePlan').text('----');
	$('#payment4MemberCoupon').text('----');
	$('#payment_discountList4Member').html('');
	$('#payment_pricePlanList4Member').html('');
	$('#payment_couponList4Member').html('');
	
	//如果不是已注入会员则去除最新会员标记
	if(member4Payment && !member4Payment.hadSet){
		member4Payment.isFresh = false;
	}
}


function readMemberWinToSelectDiscount(){
	$('#payment_popupDiscountCmp4Member').popup().popup('open');
	$('#payment_popupDiscountCmp4Member').css({top:$('#link_payment_popupDiscountCmp4Member').position().top - 270, left:$('#link_payment_popupDiscountCmp4Member').position().left-300});
	$('#payment_discountList4Member').listview().listview('refresh');	
}

function readMemberWinToSelectPricePlan(){
	$('#payment_popupPricePlanCmp4Member').popup().popup('open');
	$('#payment_popupPricePlanCmp4Member').css({top:$('#link_payment_popupPricePlanCmp4Member').position().top - 270, left:$('#link_payment_popupPricePlanCmp4Member').position().left-300});
	$('#payment_pricePlanList4Member').listview().listview('refresh');	
}

function readMemberWinToSelectCoupon(){
	$('#payment_popupCouponCmp4Member').popup().popup('open');
	$('#payment_popupCouponCmp4Member').css({top:$('#link_payment_popupCouponCmp4Member').position().top - 270, left:$('#link_payment_popupCouponCmp4Member').position().left-300});
	$('#payment_couponList4Member').listview().listview('refresh');	
}

function showMemberInfoWin(){
	if(!member4Display || !member4Display.hadSet){
		/*Util.msg.alert({msg : '账单还未注入会员, 不能使用会员结账', topTip:true});*/
		openReadMemberByCondtionWin();
		return;
	}
	
	if(getcookie(document.domain+'_consumeSms') == "true"){
		$('#memberPaymentSendSMS').attr('checked', true);
	}else{
		$('#memberPaymentSendSMS').attr('checked', false);
	}
	
	$('#memberPaymentSendSMS').checkboxradio('refresh');

	
	$('#payment4MemberCertainName').text(member4Display.name);
	$('#payment4MemberCertainType').text(member4Display.memberType.name);
	$('#payment4MemberCertainBalance').text(member4Display.totalBalance);
	$('#payment4MemberCertainPoint').text(member4Display.point);	
	$('#payment4MemberCertainPhone').text(member4Display.mobile?member4Display.mobile:'----');
	$('#payment4MemberCertainCard').text(member4Display.memberCard?member4Display.memberCard:'----');	
	
	$('#showMemberInfoWin').popup('open');
}



$(function(){
	
	//当离开结账页面时
	$('#paymentMgr').on("pagehide", function(){ 
		document.getElementById("totalPrice").innerHTML = 0.00;
		document.getElementById("actualPrice_td_payment").innerHTML = 0.00;
		document.getElementById("forFree").innerHTML = 0.00;
		document.getElementById("spanCancelFoodAmount").innerHTML = 0.00;
		document.getElementById("discountPrice").innerHTML = 0.00;	
		checkOut_actualPrice = 0;
		orderMsg = null;
		
		//取消绑定抹数的弹出键盘
		NumKeyBoardAttacher.instance().detach($('#erasePrice_input_payment')[0]);
	});
	
	//进入界面界面
	$('#paymentMgr').on('pageshow', function(){
		//清除快捷键
		$(document).off('keydown');
		//设置快捷键
		$(document).on('keydown', function(event){
			if(event.which == 109){
				//'-'表示暂结
				$('#tempPay_a_payment').click();
			}else if(event.which == 107){
				//'+'表示现金结账
				$('#cash_a_payment').click();
			}
		});
		//绑定抹数的弹出键盘
		NumKeyBoardAttacher.instance().attach($('#erasePrice_input_payment')[0], function(inputVal){
			$('#erasePrice_input_payment').keyup();
		});
	});
	
	//页面初始化
	$('#paymentMgr').on("pageinit", function(){ 
		//console.log('paymentMgr --- pageinit');
		//微信支付Button
		$('#wx_a_payment').click(function(){
			 paySubmit({
			  	submitType : PayTypeEnum.WX,
			  	postPayment : function(resultJSON){
			  		if(resultJSON.success){
			  			Util.msg.alert({msg : '微信支付二维码打印成功', topTip : true});
			  		}else{
			  			Util.msg.alert({
							msg : '对不起，您还没开通微信店铺的支付功能' + '</br>错误信息：' + resultJSON.data,
							renderTo : 'paymentMgr'
						});
			  		}
			  	}
			 });
		});
		
		function postPayment(resultJSON){
			if(resultJSON.success){
				Util.msg.alert({msg : '结账成功!', topTip : true});
				if(systemStatus == 4){
					//快餐模式下返回到点菜界面
					of.entry({orderFoodOperateType : 'fast'});
				}else{
					//返回餐台界面
					ts.loadData();
				}
			}else{
				Util.msg.alert({
					msg : resultJSON.data,
					renderTo : 'paymentMgr'
				});
			}
		}
		
		//现金结账
		$('#cash_a_payment').click(function(){
			paySubmit({
				submitType : PayTypeEnum.CASH,
				postPayment : postPayment
			});
		});	
		
		//刷卡结账
		$('#credit_a_payment').click(function(){
			paySubmit({
				submitType : PayTypeEnum.CREDIT_CARD,
				postPayment : postPayment
			});
		});
		
		//签单结账
		$('#sign_a_payment').click(function(){
			paySubmit({
				submitType : PayTypeEnum.SIGN,
				postPayment : postPayment
			});
		});
		
		//挂账结账
		$('#hang_a_payment').click(function(){
			paySubmit({
				submitType : PayTypeEnum.HANG,
				postPayment : postPayment
			});
		});
		
		//会员余额结账
		$('#memberPay_a_payment').click(function(){
			paySubmit({
				submitType : PayTypeEnum.MEMBER,
				postPayment : function(resultJSON){
					if(resultJSON.success){
						Util.msg.alert({msg : '结账成功!', topTip : true});
						$('#memberPayCancel_a_payment').click();
						setTimeout(function(){
							if(systemStatus == 4){
								//快餐模式下返回到点菜界面
								of.entry({orderFoodOperateType : 'fast'});
							}else{
								//返回餐台界面
								ts.loadData();
							}
						}, 250);
					}else{
						Util.msg.alert({
							msg : resultJSON.data,
							renderTo : 'paymentMgr'
						});
					}
				}
			});
		});
		
		//会员结账-取消
		$('#memberPayCancel_a_payment').click(function(){
			$('#showMemberInfoWin').popup('close');
		});
		
		//暂结
		$('#tempPay_a_payment').click(function(){
			paySubmit({
				submitType : PayTypeEnum.CASH,
				temp : true,
				postPayment : function(resultJSON){
					if(resultJSON.success){
						Util.msg.alert({msg : resultJSON.data, topTip : true});
					}else{
						Util.msg.alert({
							msg : resultJSON.data,
							renderTo : 'paymentMgr'
						});
					}
				}
			});
		});
		
		//现金找零
		$('#cashReceive_a_payment').click(function(){
			$('#cashReceive_div_payment').popup('open');
		});
		
		//现金输入框在输入数字后实时显示找零
		$('#cashReceive_input_payment').on('keyup', function(){
			//计算抹零
			var eraseQuota = $('#erasePrice_input_payment').val();
			var actualPrice = checkOut_actualPrice;
			if(!isNaN(eraseQuota)){
				actualPrice = checkOut_actualPrice - eraseQuota;
			}
			
			if($('#cashReceive_input_payment').val() - actualPrice > 0){
				$('#cashBack_label_payment').text($('#cashReceive_input_payment').val() - actualPrice);
			}else{
				$('#cashBack_label_payment').text(0);
			}
		});
			
		//进入现金找零Popup的函数
		$('#cashReceive_div_payment').on('popupafteropen', function(event, ui){
			//计算抹零
			var eraseQuota = $('#erasePrice_input_payment').val();
			var actualPrice = checkOut_actualPrice;
			if(!isNaN(eraseQuota)){
				actualPrice = checkOut_actualPrice - eraseQuota;
			}
			$('#cashReceive_input_payment').val('');
			$('#cashBack_label_payment').text(0);
			$('#consume4CashReceive_a_payment').text(actualPrice);
			setTimeout(function(){
				$('#cashReceive_input_payment').focus();
			}, 200);
			//绑定现金收入的输入框
			NumKeyBoardAttacher.instance().attach($('#cashReceive_input_payment')[0], function(inputVal){
				$('#cashReceive_input_payment').keyup();
			});
		});
		
		//退出现金找零Popup的函数
		$('#cashReceive_div_payment').on('popupafterclose', function(event, ui){
			NumKeyBoardAttacher.instance().detach($('#cashReceive_input_payment')[0]);
		});
		
		//现金找零-取消
		$('#receivedCashCancel_a_payment').click(function(){
			$('#cashReceive_div_payment').popup('close');
		});
		
		//现金找零-确定
		$('#receivedCashConfirm_a_payment').click(function(){
			var input = $('#cashReceive_input_payment');
			if(input.val() && !isNaN(input.val())){
				paySubmit({
					submitType : PayTypeEnum.CASH,
					cashIncome : parseInt(input.val()),
					postPayment : function(resultJSON){
						if(resultJSON.success){
							Util.msg.alert({msg : '结账成功!', topTip : true});
							//关闭现金找零界面
							$('#receivedCashCancel_a_payment').click();
							//等完全关闭后再返回
							setTimeout(function(){
								if(systemStatus == 4){
									//快餐模式下返回到点菜界面
									of.entry({orderFoodOperateType : 'fast'});
								}else{
									//返回餐台界面
									ts.loadData();
								}
							}, 250);
	
						}else{
							Util.msg.alert({
								msg : resultJSON.data,
								renderTo : 'paymentMgr'
							});
						}
					}
				});
			}else{
				Util.msg.alert({msg:'请输入正确的结账金额', topTip : true});
				input.focus();
			}
		});
	
		//混合结账
		$('#mixed_a_payment').click(function(){
			if(member4Display && member4Display.hadSet){
				Util.msg.alert({msg:'会员不支持混合结账', topTip:true});
				return;
			}
			
			//混合结账选项
			var maxTr = '<tr>' +
					'<td><label><input data-theme="e" id={checkboxId} data-for={numberfieldId} type="checkbox" name="mixPayCheckbox">{name}</label></td>'+
					'<td style="padding-right: 10px;"><input data-theme="c" id={numberfieldId} class="mixPayInputFont numberInputStyle" disabled="disabled" ></td>'+
					'</tr>';
			var html = [];
			var checkBoxes = [];
			for (var i = 0; i < payTypeData.length; i++) {
				var checkBoxId = "chbForPayType" + payTypeData[i].id;
				var numberfieldId = "numForPayType" + payTypeData[i].id;
				checkBoxes.push(checkBoxId);
				html.push(maxTr.format({
					name : payTypeData[i].name,
					checkboxId : checkBoxId,
					numberfieldId : numberfieldId
				}));
			}
			
			$('#mixedPay_tbl_payment').html(html.join('')).trigger('create');
			
			//混合结账中每个CheckBox按钮的事件
			for(var i = 0; i < checkBoxes.length; i++){
				$('#' + checkBoxes[i]).click(function(){
					
					var curCheckbox = $(this);
					var numForAlias = $("#" + curCheckbox.attr('data-for'));
					
					if(curCheckbox.attr('checked')){
						
						var mixedPayMoney = checkOut_actualPrice;
						for (var i = 0; i < payTypeData.length; i++) {
							var checked = $('#chbForPayType' + payTypeData[i].id).attr('checked');
							var money = $('#numForPayType' + payTypeData[i].id).val();
							if(checked && money){
								mixedPayMoney = (mixedPayMoney * 10000 - parseInt(money) * 10000) / 10000; 
							}
						}
						
						numForAlias.val(mixedPayMoney < 0 ? 0 : mixedPayMoney);							
						
						numForAlias.removeAttr("disabled"); 
						numForAlias.parent().removeClass('ui-disabled');
				
						numForAlias.focus();
						numForAlias.select();
						
					}else{
						
						numForAlias.attr("disabled", true); 
						numForAlias.parent().addClass('ui-disabled');
				
						numForAlias.val('');		
					}	
				});
			}
			
			$('#mixedPayWin').popup('open');
			
		});
		
		//混合结账-暂结
		$('#mixedTempPay_a_payment').click(function(){
			mixPay(true);
		});
		
		//混合结账-结账
		$('#mixedPay_a_payment').click(function(){
			mixPay(false);
		});
		
		function mixPay(temp){
			
			var mixedIncome = '';
			for (var i = 0; i < payTypeData.length; i++) {
				var checked = $('#chbForPayType' + payTypeData[i].id).attr('checked');
				if(checked && $('#numForPayType' + payTypeData[i].id).val()){
					if(mixedIncome.length != 0){
						mixedIncome += '&';
					}
					mixedIncome += (payTypeData[i].id + ',' + $('#numForPayType' + payTypeData[i].id).val());  
				}
			}	
			
			paySubmit({
				submitType : PayTypeEnum.MIXED,
				temp : temp,
				mixedIncome : mixedIncome,
				postPayment : function(resultJSON){
					
					if(resultJSON.success){
						if(temp){
							Util.msg.alert({msg : resultJSON.data, topTip : true});
						}else{
							//关闭混合结账界面
							$('#mixedPayCancel_a_payment').click();
							Util.msg.alert({msg : '结账成功!', topTip : true});
							//等完全关闭后再返回
							setTimeout(function(){
								if(systemStatus == 4){
									//快餐模式下返回到点菜界面
									of.entry({orderFoodOperateType : 'fast'});
								}else{
									//返回餐台界面
									ts.loadData();
								}
							}, 250);
						}
					}else{
						Util.msg.alert({
							msg : resultJSON.data,
							renderTo : 'paymentMgr'
						});
					}
				}
			});
			
		}
		
		//混合结账-取消
		$('#mixedPayCancel_a_payment').click(function(){
			$('#mixedPayWin').popup('close');
		});
		
		//FIXME 抹数框输入时
//		$('#erasePrice_input_payment').focus(function(){
//			focusInput = this.id;
//			usedEraseQuota = false;
//			mouseOutNumKeyboard = true;
//			$('#numberKeyboard').show();	
//			//设置数字键盘触发
//			numKeyBoardFireEvent = function (){
//				$('#erasePrice_input_payment').keyup();
//			};
//			
//			$('#calculator4NumberKeyboard').on("mouseover", function(){
//				usedEraseQuota = false;
//				mouseOutNumKeyboard = false;
//			});
//			
//			$('#calculator4NumberKeyboard').on("mouseout", function(){
//				usedEraseQuota = true;
//				mouseOutNumKeyboard = true;
//			});			
//		});	
		
		//抹数联动
		$('#erasePrice_input_payment').on('keyup', function(){
			var eraseQuota = $('#erasePrice_input_payment').val();
			if(eraseQuota && isNaN(eraseQuota)){
				Util.msg.alert({msg:"请填写正确的抹数金额", topTip:true ,fn:function(){$("#erasePrice_input_payment").focus();$("#erasePrice_input_payment").select();}});
				return;
			}else if(!isNaN(eraseQuota) && eraseQuota > restaurantData.setting.eraseQuota){// 抹数金额
				Util.msg.alert({msg:"抹数金额大于设置上限，不能结帐!", topTip:true,fn:function(){$("#erasePrice_input_payment").focus();$("#erasePrice_input_payment").select();}});
				return;
			}			
			$('#actualPrice_td_payment').html((checkOut_actualPrice * 10000 - eraseQuota * 10000)/10000);
			
		});
	
		//改单
		$('#updateOrder_a_payment').click(function(){
			uo.entry({
				table : pm.table
			});	
		});
		
		//初始化发券界面
		var cuoponIssuePopup = null;
		cuoponIssuePopup = new JqmPopup({
			loadUrl : './popup/cuopon/issue.html',
			pageInit : function(self){
				self.find('[id=cuoponIssueCancel_a_issue]').click(function(){
					cuoponIssuePopup.close();
				});
				
				$.post('../OperatePromotion.do',  {dataSource : 'getByCond', status : 'progress'}, function(response, status, xhr){
					if(response.success){
						var progressCuopon = "";
						for(var i = 0; i < response.root.length; i++){
							var eachProgressCuopon = '<tr>'
												 + '<td style="width:250px">'
												 + '<label style="height:50px"><input type="checkbox" data-theme="e" id="' + response.root[i].id + '">' + response.root[i].title + '</label>'
												 + '</td>'
												 + '<td style="width:35px"><input style="font-size:20px;font-weight: bold;width:35px;" maxlength="3"></td>'
												 + '</tr>';
								
							progressCuopon += eachProgressCuopon;
						}
						self.find('[id = issueTal_table_issue]').append(progressCuopon);
						self.find('[id = issueTal_table_issue]').trigger('create').trigger('refresh');
					}
				});
				
				
//				
//				$.post('../QueryMember.do', {dataSource : 'normal', id : orderMsg.memberId, forDetail : true}, function(result){
//					if(result.success){
//						member4Payment = result.root[0];
//						//设置为已注入状态 
//						member4Payment.hadSet = true;
//						
//						member4Display = Util.clone(member4Payment);
//						
//						var memberSpan = "";
//						if(result.root[0].isRaw){
//							memberSpan = '<span style = "margin-left: 20px;">当前会员：<font style="text-decoration: underline;cursor: pointer;color:blue" onclick="ts.member.memberInfoBind(\'loadMemberBind4Payment\', \''+ result.root[0].name +'\')">' + result.root[0].name +"(点击绑定)</font></span>";
//						}else{
//							memberSpan = '<span style = "margin-left: 20px;">当前会员：<font style="color:green">' + result.root[0].name +"</font></span>";
//						}
//						
//						$('#orderMemberDesc').html(memberSpan);
//					}
//				});
				
				
				
			}
		});
		
		//打开发送优惠券
		$('#issueCuopon_a_orderFood').click(function(){
			console.log('opening');
			cuoponIssuePopup.open(function(self){
		
			});
		});
		
		
	});
	
	var SettleTypeEnum = {
		NORMAL : { val : 1, desc : '普通结账' },
		MEMBER : { val : 2, desc : '会员结账' }
	};

	var PayTypeEnum = {
		CASH : { val : 1, desc : '现金' },
		CREDIT_CARD : { val : 2, desc : '刷卡' },
		MEMBER : { val : 3, desc : '会员' },
		SIGN : { val : 4, desc : '签单'},
		HANG : { val : 5, desc : '挂账'},
		WX : { val : 6, desc : '微信支付'},
		MIXED : { val : 100, desc : '混合'}
	};
	
	//结账提交
	function paySubmit(c) {
	
		c = c || {
			submitType : PayTypeEnum.CASH,		//结账类型
			postPayment : null,					//结账处理函数
			temp : temp,						//是否暂结
			mixedIncome : mixedIncome			//混合结账数据
		};
		
		if(isPaying == true){ 
			return; 
		}
		
		if(c.temp == undefined){
			c.temp = false;
		}
		
		//是否发送短信
		var sendSms = false;
		
		if(orderMsg == null){
			Util.msg.alert({msg:"读取账单有误, 不能结账", renderTo:'paymentMgr'});
			return;
		}
		
		var eraseQuota = $("#erasePrice_input_payment").val();
		if(eraseQuota && isNaN(eraseQuota)){
			Util.msg.alert({msg:"请填写正确的抹数金额", renderTo:'paymentMgr',fn:function(){$("#erasePrice_input_payment").focus();$("#erasePrice_input_payment").select();}});
			return;
		}else if(!isNaN(eraseQuota) && eraseQuota > restaurantData.setting.eraseQuota){// 抹数金额
	//		setFormButtonStatus(false);
			Util.msg.alert({msg:"抹数金额大于设置上限，不能结帐!", renderTo:'paymentMgr',fn:function(){$("#erasePrice_input_payment").focus();$("#erasePrice_input_payment").select();}});
			return;
		}	
	
		//普通或会员结账, 会员已注入则为会员结账, 否则为普通该结账
		var settleType;
		if(member4Display && member4Display.hadSet){
			settleType = SettleTypeEnum.MEMBER.val;
			actualMemberID = member4Display.id;
		}else{
			settleType = SettleTypeEnum.NORMAL.val;
		}
		
		if(c.submitType == PayTypeEnum.MEMBER){
			//会员结账
			//FIXME 要加上抹数?
			if(member4Display.totalBalance < checkOut_actualPrice){
				Util.msg.alert({msg : '会员卡余额小于合计金额，不能结帐!', topTip:true});
				return;			
			}			
			
			//保存发送短信 
			if($('#memberPaymentSendSMS').attr('checked')){
				sendSms = true;
				setcookie(document.domain + '_consumeSms', true);
			}else{
				sendSms = false;
				setcookie(document.domain + '_consumeSms', false);
			}
			
		}
	
		Util.LM.show();
		
		isPaying = true;
		$.ajax({
			url : "../PayOrder.do",
			type : 'post',
			data : {
				"orderID" : orderMsg.id,
				"cashIncome" : c.cashIncome ? c.cashIncome : 0,
				"payType" : settleType,
				"payManner" : c.submitType.val,
				"tempPay" : c.temp,
				"memberID" : actualMemberID,
				"comment" : $("#remark").val(),
				'eraseQuota' : eraseQuota == '' ? 0 : eraseQuota,
				'customNum' : orderMsg.customNum,
				'payTypeCash' : c.mixedIncome ? c.mixedIncome : '',
				'sendSms' : sendSms
			},
			dataType : 'json',
			success : function(resultJSON, status, xhr){
				Util.LM.hide();
				isPaying = false;
				
				if(c.postPayment){
					c.postPayment(resultJSON);
				}
			},
			error : function(request, status, err){
				Util.LM.hide();
				isPaying = false;
				Util.msg.alert({
					msg : "结账出错, 请刷新页面后重试",
					renderTo : 'paymentMgr'
				});
			}
		}); 		
	};
});

