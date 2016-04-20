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
	    +			'<a class="tranFood" onclick="uo.transFoodForTS({event:this})" data-index={dataIndex}  data-role="button" data-theme="b">转菜</a>'
	    +			'<a  data-index={dataIndex} data-role="button" data-theme="b"  data-rel="popup"  data-transition="pop" onclick="uo.openOrderFoodOtherOperate({event:this})">更多</a>'
	    +		'</div>'
	    +'</td>'
		+ '<td>{waiter}</td>'
		+ '</tr>';

	
/**
 * 显示已点菜界面函数, 入口
 * @param {object} c  
 */
uo.entry = function(c){
	location.href = '#orderFoodListMgr';
	//加载退菜原因
	if(uo.reasons.length <= 0){
		//加载退菜原因
		$.post('../OperateCancelReason.do', { dataSource : 'getByCond'}, function(result){
			if(result.success){
				uo.reasons = result.root;
				uo.loadCancelReasonData(result.root);
			}
		});			
	}

	//加载折扣方案
	uo.loadDiscountCmp();
	
	//异步刷新账单
	initOrderData({table : c.table});
	uo.table = c.table;
	
	
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
			orderID : c.order ? c.order.id : null,
			tableID : c.table ? c.table.id : null
		},
//		async : false,
		success : function(data, status, xhr){
			if(data.success){
				uo.order = data.other.order;
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
		var count = null;
		if((uo.order.orderFoods[i].status & 1 << 7) != 0){
			if(uo.order.orderFoods[i].count > 1){
				 count = uo.order.orderFoods[i].count;
			}else{
				 count = '<font color="red" style="font-size:25px;">' + uo.order.orderFoods[i].count + '</font>';
			}
		}else{
			 count = uo.order.orderFoods[i].count;
		}
		
		html += orderFoodListCmpTemplet.format({
			dataIndex : i + 1,
			id : uo.order.orderFoods[i].id,
			name : (uo.order.orderFoods[i].status & 1 << 7) != 0 ? uo.order.orderFoods[i].foodName + '<font color="red" style="font-size:25px;">[称重确认]</font>' : uo.order.orderFoods[i].foodName,
			count : count,
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
	//转菜
	$('#orderFoodListBody .tranFood').each(function(index, element){
		element.onclick= function(){
			var _selectedTable = null;
			var orderFood = uo.order.orderFoods[parseInt($(element).attr('data-index'))-1];		
			var askTablePopup = new AskTablePopup({
				tables : WirelessOrder.tables,
				title : '转菜',
				middle : function(){
					var prefectTable = askTablePopup.prefect();
					if(prefectTable){
						var foodCount = $('#foodAmountText_input_ask').val();
						Util.LM.show();
						$.post('../OperateOrderFood.do', {
							dataSource : 'transFood',
							orderId : uo.order.id,
							tableId : prefectTable.id,
							transFoods : orderFood.orderFoodId + ',' + foodCount
						}, function(data){
							Util.LM.hide();
							if(data.success){
								askTablePopup.close();
								Util.msg.tip(data.msg);
								//刷新账单
								initOrderData({order : uo.order});
							}else{
								Util.msg.tip(data.msg);
							}
						});
					}else{
						Util.msg.tip('没有此餐台, 请重新输入');
					}
				},
				tableSelect : function(selectedTable){
					_selectedTable = selectedTable;
					var foodCount = $('#foodAmountText_input_ask').val();
					Util.LM.show();
					$.post('../OperateOrderFood.do', {
						dataSource : 'transFood',
						orderId : uo.order.id,
						tableId : _selectedTable.id,
						transFoods : orderFood.orderFoodId + ',' + foodCount
					}, function(data){
						Util.LM.hide();
						if(data.success){
							askTablePopup.close();
							Util.msg.tip(data.msg);
							//刷新账单
							initOrderData({order : uo.order});
						}else{
							Util.msg.tip(data.msg);
						}
					});
					
				}
			});
			askTablePopup.open(function(){
				$('#left_a_askTable').hide();
				$('#foodAmount_td_ask').show();
				$('#foodAmountText_input_ask').val(orderFood.count);
				$('#middle_a_askTable').css('width', '48%');
				$('#right_a_askTable').css('width', '50%');
			});
		}
	});	
	uo.showNorthForUpdateOrder();
};

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
};

/**
 * 初始化页尾信息（菜品数量，消费总额）
 */
uo.showDescForUpdateOrder = function(){
	var html = "", memberSpan = "";
	if(uo.orderMember){
		if(uo.orderMember.isRaw){
			memberSpan = '<span style = "margin-left: 20px;">当前会员：<font style="text-decoration: underline;cursor: pointer;color:blue" id="memberBind_span_checkout" >' + uo.orderMember.name +"(点击绑定)</font></span>";
		}else{
			memberSpan = '<span style = "margin-left: 20px;">当前会员：<font style="color:green">' + uo.orderMember.name +"</font></span>";
		}
	}
	
	html = (uo.order.usedCoupons.length > 0 ?'<span style = "margin-left: 20px;">使用优惠劵：<font color="green">' + uo.order.usedCoupons.length + '张, 共¥' + uo.order.couponPrice + '</font></span>' : '') + 
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
	
	//绑定会元完善资料
	$('#memberBind_span_checkout').click(function(){
		seajs.use('./js/popup/member/perfect', function(perfectPopup){
			var perfectMemberPopup = perfectPopup.newInstance({
				selectedMember : uo.order.memberId,
				selectedOrder : uo.order.id,
				memberName : uo.orderMember.name,
				postBound : function(){
					initOrderData({order : uo.order});
				}
			});
			perfectMemberPopup.open();
			
		});
		
//		var perfectMemberPopup = new PerfectMemberPopup({
//			selectedMember : uo.order.memberId,
//			selectedOrder : uo.order.id,
//			memberName : uo.orderMember.name,
//			postBound : function(){
//				initOrderData({order : uo.order});
//			}
//		});
//		perfectMemberPopup.open();
	});
	
};
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
 * 返回
 */
uo.back = function(){
	ts.loadData();
};

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
};

/**
 * 选择退菜原因
 */
uo.selectCancelReason = function(c){
	var selectedReason = $(c.event);
	uo.selectingCancelReason = uo.reasons[parseInt($(c.event).attr('data-index'))];
	
	$('#cancelFoodReasonCmp a').attr('data-theme', 'c').removeClass('ui-btn-up-e').addClass('ui-btn-up-c');
	
	$(c.event).attr('data-theme', 'e').removeClass('ui-btn-up-c').addClass('ui-btn-up-e');
	
	$('#cancelFoodReasonCmp').trigger('refresh');
};

/**
 * 打开退菜操作
 */
uo.openCancelFoodCmp = function(c){

 	//检查是否为小数
	function checkDot(num){
		if(!isNaN(num)){
			num = num + ""; 
		}
		var dot = num.indexOf(".");
		if(dot != -1){
		    var dotCnt = num.substring(dot + 1, dot + 2);
		    var dotCnt2 = num.substring(dot + 2);
		    if(dotCnt >= 1 || dotCnt2 >= 1){
		        return true;
		    }else{
		    	return false;
		    }
		}else{
			return false;
		}
	}
	//刷新原因界面
	uo.loadCancelReasonData(uo.reasons);
	
	var orderFood = uo.order.orderFoods[parseInt($(c.event).attr('data-index'))-1];
	
	uo.operateFood = orderFood;
	
	$('#inputCancelFoodSet').val(checkDot(orderFood.count+'')?orderFood.count : parseInt(orderFood.count));
	NumKeyBoardAttacher.instance().attach($('#inputCancelFoodSet')[0]);
	$('#inputCancelFoodSet').focus();	
	$('#shadowForPopup').show();
	$('#cancelFoodSet').show();
	
	
	$('#inputCancelFoodSet').focus();
	$('#inputCancelFoodSet').select();
};

/**
 * 关闭退菜操作
 */
uo.closeCancelFoodCmp = function(){
	$('#inputCancelFoodSet').val('');
	
	$('#shadowForPopup').hide();
	$('#cancelFoodSet').hide();
	
};

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
	}else if(isNaN(num)){
		Util.msg.tip('数字不合规范.');
		$("#inputCancelFoodSet").val(uo.operateFood.count);
		$("#inputCancelFoodSet").select();
	}else if(num > uo.operateFood.count){
		Util.msg.tip('退菜数量不能大于原有数量');
		$("#inputCancelFoodSet").val(uo.operateFood.count);
		$("#inputCancelFoodSet").select();
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
};

/**
 * 打开已点菜中单条菜更多操作
 */
uo.openOrderFoodOtherOperate = function(c){
	//获取选中行
	uo.selectedFood = uo.order.orderFoods[parseInt($(c.event).attr('data-index'))-1];
	//获取赠送权限
	var giftPrivileges = WirelessOrder.login.hasPrivilege(WirelessOrder.Staff.Privilege.GIFT);
	
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
};

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
	
	
};

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
};

/**
 * 撤台
 */
uo.cancelTable = function(){
	$('#updateFoodOtherOperateCmp').popup('close');
	
	setTimeout(function(){
		Util.msg.alert({
			msg : '是否撤台?',
			renderTo : 'orderFoodListMgr',
			buttons : 'yesback',
			certainCallback : function(){
				Util.LM.show();
				$.post('../OperateTable.do', {'tableId' : uo.order.table.id, 'dataSource' : 'cancelTable'}, function(result){
					Util.LM.hide();
					if(result.success){
						Util.msg.tip( result.msg);
						uo.cancelForUO();
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
					Util.msg.alert({
						msg : '操作失败, 请联系客服',
						renderTo : 'orderFoodListMgr'
					});		
				});							
			}
		});			
	}, 250);
	

};


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
	var weighPopup = null;
	weighPopup = new NumKeyBoardPopup({
		header : '输入称重--' + uo.selectedFood.foodName,
		left : function(){
			var count = $('#input_input_numKbPopup');
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
			uo.submitUpdateOrderHandler({orderFoods:uo.order.orderFoods, notPrint:!$('#chkPrintWeigh_input_checkOut').attr("checked")});	
			
			if($('#chkPrintWeigh_input_checkOut').attr('checked')){
				setcookie(document.domain + '_printWeightCheck', true);
			}else{
				setcookie(document.domain + '_printWeightCheck', false);
			}
			
			weighPopup.close();
		},
		right : function(){
			weighPopup.close();
		}
	});
	
	weighPopup.open(function(self){
		var print = '<label>'+
  		'<input type="checkbox" id="chkPrintWeigh_input_checkOut" data-theme="e" checked="checked">打印称重信息'+
	   	'</label>';	
		$('#content_div_numKbPopup').append(print);
		$('#content_div_numKbPopup').trigger('create');
		self.find('[id=middle_a_numKbPopup]').hide();

		if(getcookie(document.domain + '_printWeightCheck') == 'true'){
			$('#chkPrintWeigh_input_checkOut').attr("checked", true).checkboxradio("refresh");
		}else{
			$('#chkPrintWeigh_input_checkOut').attr("checked", false).checkboxradio("refresh");
		}
		
			setTimeout(function(){
			self.find('[id=input_input_numKbPopup]').focus();
		}, 200);
		
	});
	
	
};


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
	var giftPopup = null;
	giftPopup = new NumKeyBoardPopup({
		header : '输入赠送--' + uo.selectedFood.foodName,
		left : function(){
			var count = $('#input_input_numKbPopup');
			if(!count.val() || isNaN(count.val()) || count.val() < 0){
				Util.msg.tip('请输入正确的赠送数量');
				count.focus();
				return;
			}else if(count.val() > uo.selectedFood.count){
				Util.msg.tip('赠送数量不能大于原有数量');
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
					initOrderData({order : uo.order});
				}else{
					Util.msg.tip('赠送失败');
				}
			});
			giftPopup.close();
		},
		right : function(){
			giftPopup.close();
		}
	});
	
	giftPopup.open(function(self){
		self.find('[id=input_input_numKbPopup]').val(uo.selectedFood.count);
		self.find('[id=middle_a_numKbPopup]').hide();
		setTimeout(function(){
		//	self.find('[id=input_input_numKbPopup]').select();
			self.find('[id=input_input_numKbPopup]').focus();
			self.find('[id=input_input_numKbPopup]').select();
		}, 200);
		
	});
};

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
	
};

/**
 * 关闭备注
 */
uo.closeComment = function(){
	$('#shadowForPopup').hide();
	$('#orderFoodCommentCmp').hide();
};

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
			initOrderData({order : uo.order});
		}else{
			Util.msg.alert({
				title : '提示',
				msg : data.msg, 
				renderTo : 'orderFoodListMgr'
			});				
		}			
	});
};

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
				customNum : uo.order.customNum,
				discountID : typeof c.discountId != 'undefined' ? c.discountId : '',
				tempPay : true,
				isPrint : typeof c.isPrint == 'boolean' ? c.isPrint : true,
				orientedPrinter : getcookie(document.domain + '_printers')
			},
			dataType : 'json',
			success : function(result, status, xhr){
				Util.LM.hide();
				if(result.success){
					Util.msg.tip('操作成功');
					
					if(typeof c.callback == 'function'){
						c.callback(result);
					}
					
					initOrderData({order : uo.order});
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
};

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
						initOrderData({order : uo.order});
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
};

/**
 * 补打总单
 */
uo.tempPayForPrintAll = function(){
	uo.tempPayForPrintAllAction = true;
	//关闭后回调
	$('#updateFoodOtherOperateCmp').popup('close');
};

/**
 * 补打明细
 */
uo.printDetailPatch = function(){
	uo.printDetailPatchAction = true;
	//关闭后回调
	$('#updateFoodOtherOperateCmp').popup('close');	
};

/**
 * 打开操作人数
 */
uo.showOperatePeople = function(){
	uo.updateCustom = true;
	
	$('#updateFoodOtherOperateCmp').popup('close');
};

/**
 * 关闭操作人数
 */
uo.closeOperatePeople = function(c){
	uo.updateCustom = false;
	if(c && c.callback){
		c.callback();
	}
};

/**
 * 打开更多操作
 */
uo.openMoreOperate = function(){
	$('#updateFoodOtherOperateCmp').popup({
		afterclose: function (event, ui) { 
			if(uo.updateCustom){//修改人数
				var updateCustomPopup = null;
				updateCustomPopup = new NumKeyBoardPopup({
					header : '修改人数',
					left : function(){
						var num = $("#input_input_numKbPopup").val();
						//更改页面端的的人数
						$("#customNumForUO").html("用餐人数：" + num);	
						uo.customNum = num;
						uo.updateCustom = true;
						uo.saveForUO();
						updateCustomPopup.close();
					},
					right : function(){
						uo.closeOperatePeople();
						updateCustomPopup.close();
					}
						
				});

				updateCustomPopup.open(function(self){
					self.find('[id=input_input_numKbPopup]').val(uo.order.customNum);
					self.find('[id=middle_a_numKbPopup]').hide();
					setTimeout(function(){
					//	self.find('[id=input_input_numKbPopup]').select();
						self.find('[id=input_input_numKbPopup]').focus();
						self.find('[id=input_input_numKbPopup]').select();
					}, 200);
				});
				
			}else if(uo.tempPayForPrintAllAction){//补打总单
				Util.LM.show();
				$.post('../PrintOrder.do', {
					orderID : uo.order.id, 
					printType : 14,
					orientedPrinter : getcookie(document.domain + '_printers')			//特定打印机打印
				}, function(result){
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
						$.post('../PrintOrder.do', {
							orderID : uo.order.id, 
							printType : 15,
							orientedPrinter : getcookie(document.domain + '_printers')			//特定打印机打印
						}, function(result){
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
};


/**
 * 工具栏的确定按钮,对整个页面信息提交
 */
uo.saveForUO = function(){
	//判断页面信息是否有改动
	if(uoCancelFoods.length == 0 && uo.order.customNum == uo.customNum){
		Util.msg.tip('账单没有修改，不能提交');
	}else{
		uo.order.customNum = uo.customNum;
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
				notPrint : c.notPrint ? c.notPrint : false,
				orientedPrinter : getcookie(document.domain + '_printers')			//特定打印机打印
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
								//刷新账单
								initOrderData({order : uo.order});
								uo.canceling = false;
							}else if(uo.updateCustom){
								Util.msg.tip('账单修改成功');	
								initOrderData({order : uo.order});
								uo.closeOperatePeople();
							}else if(uo.weighOperate){
								Util.msg.tip( '账单修改成功');	
								initOrderData({order : uo.order});
							}else if(uo.hurriedFood){
								Util.msg.tip( '催菜成功');	
								initOrderData({order : uo.order});
								delete uo.hurriedFood;
							}else{
								Util.msg.alert({
									title : data.title,
									msg : data.msg, 
									renderTo : 'orderFoodListMgr',
									time : 3,
									fn : function(btn){
										ts.loadData();
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
										initOrderData({order : uo.order});
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
	of.entry({
		table : uo.table,
		orderFoodOperateType : 'normal'
	});
};

/**
 * 工具栏的取消按钮,取消对该页面的修改操作
 */
uo.cancelForUO = function(){	
	
	uoCancelFoods = [];
	uoFood = [];
	uo.back();
	
};





$(function(){
	//已点菜界面的结账按钮事件
	$('#payOrder_a_checkOut').click(function(){
		pm.entry({table:uo.table});
	});
	
	
	//退菜原因的add
	$('#cancelAdd_span_checkOut').click(function(){
		var cancelInput = $('#inputCancelFoodSet').val();
		if(cancelInput == ""){
			$('#inputCancelFoodSet').val(cancelInput + 1);
		}else{
			$('#inputCancelFoodSet').val(parseInt(cancelInput)  + 1);
		}
	});
	
	//退菜原因的reduce
	$('#cancelReduce_span_checkout').click(function(){
		var cancelInput = $('#inputCancelFoodSet').val();
		if(cancelInput == ""){
			$('#inputCancelFoodSet').val(cancelInput - 1);
		}else{
				$('#inputCancelFoodSet').val(parseInt(cancelInput) - 1);
		}
	});
	
	$('#orderFoodListMgr').on('pageinit', function(){
		//会员
		$('#memberRead_a_orderFood').click(function(){
			seajs.use('./js/popup/member/read', function(readPopup){
				var memberReadPopup = null;
				memberReadPopup = readPopup.newInstance({
					confirm : function(member, discount, pricePlan){
						Util.LM.show();
						
						$.post('../OperateDiscount.do', {
							dataSource : 'setDiscount',
							orderId : uo.order.id,
							memberId : member.id,
							discountId : discount.id,
							pricePlan : pricePlan.id
							
						}, function(data){
							Util.LM.hide();
							if(data.success){
								
								//异步刷新账单
								initOrderData({order : uo.order});
								
								Util.msg.alert({topTip : true, msg : '会员注入成功'});	
								
								//关闭会员读取Popup
								memberReadPopup.close();
								
							}else{
								Util.msg.tip('使用会员失败</br>' + data.msg);					
							}
						}, 'json');		
					}
				});
				//打开会员读取Popup
				memberReadPopup.open();
			});
		});
		
		//转台
		$('#checkOutTranTable_a_tableSelect').click(function(){
			var _selectedTable = null;
			var askTablePopup = new AskTablePopup({
				tables : WirelessOrder.tables,
				title : '转台',
				middle : function(){
					var sourceTable = uo.table;
					var destAlias = $('#left_input_askTable').val();
					
					var destTable = WirelessOrder.tables.getByAlias(destAlias);
					
					if(!sourceTable || !destTable){
						Util.msg.tip('查找餐台出错,请检查台号是否正确');
						return;
					}
					
					Util.LM.show();
					
					$.post('../OperateTable.do', {
						dataSource : 'transTable',
						oldTableId : sourceTable.id,
						newTableId : destTable.id,
						orientedPrinter : getcookie(document.domain + '_printers')
					},function(data){
						Util.LM.hide();
						if(data.success){
							askTablePopup.close(function(){
								Util.msg.tip(data.msg);
								//返回餐台选择界面
								ts.loadData();
							}, 200);
						}else{
							Util.msg.tip(data.msg);				
						}			
					}).error(function(){
						Util.LM.hide();
						Util.msg.tip('操作失败, 请刷新页面重试');		
					});	
					
				},
				tableSelect : function(selectedTable){
					_selectedTable = selectedTable;
					var sourceTable = uo.table;
					
					var destTable = _selectedTable;
					
					Util.LM.show();
					
					$.post('../OperateTable.do', {
						dataSource : 'transTable',
						oldTableId : sourceTable.id,
						newTableId : destTable.id
					},function(data){
						Util.LM.hide();
						if(data.success){
							askTablePopup.close(function(){
								Util.msg.tip(data.msg);
								//返回餐台选择界面
								ts.loadData();
							}, 200);
						}else{
							Util.msg.tip(data.msg);				
						}			
					}).error(function(){
						Util.LM.hide();
						Util.msg.tip('操作失败, 请刷新页面重试');		
					});	
					
				}
			});
			askTablePopup.open(function(){
				$('#left_a_askTable').hide();
				$('#middle_a_askTable').css('width', '48%');
				$('#right_a_askTable').css('width', '50%');
			});
		});
		
		//全单转菜
		$('#allTrantable_li_tableSelect').click(function(){
			$('#updateFoodOtherOperateCmp').popup('close');
			setTimeout(function(){
				var _selectedTable = null;
				var askTablePopup = new AskTablePopup({
					tables : WirelessOrder.tables,
					title : '全单转菜',
					middle :function(){
						var perfectMatched = askTablePopup.prefect();
						if(perfectMatched){
							Util.LM.show();
							$.post('../OperateOrderFood.do', {
								dataSource : 'transFood',
								orderId : uo.order.id,
								tableId : perfectMatched.id,
								transFoods : -1		
							},function(data){
								Util.LM.hide();
								if(data.success){
									Util.msg.tip(data.msg);
									askTablePopup.close(function(){
										//刷新账单
										initOrderData({order : uo.order});
									}, 200);
								}else{
									Util.msg.tip(data.msg);				
								}			
							});
						}else{
							Util.msg.tip('没有此餐台,请重新输入');
						}
					},
					tableSelect : function(selectedTable){
						_selectedTable = selectedTable;
						Util.LM.show();
						$.post('../OperateOrderFood.do', {
							dataSource : 'transFood',
							orderId : uo.order.id,
							tableId : _selectedTable.id,
							transFoods : -1	
						},function(data){
							Util.LM.hide();
							if(data.success){
								Util.msg.tip(data.msg);
								askTablePopup.close(function(){
									//刷新账单
									initOrderData({order : uo.order});
								}, 200);
							}else{
								Util.msg.tip(data.msg);				
							}			
						});
					}
				});
				askTablePopup.open(function(){
					$('#left_a_askTable').hide();
					$('#middle_a_askTable').css('width', '48%');
					$('#right_a_askTable').css('width', '50%');
				});
			}, 500);
		});
		
		// 绑定明细
		$('#detail_a_tableSelect').click(function(){
			var detailPopup = null;
			detailPopup = new DetailPopup({
				table : uo.table,
				order :  uo.order
			});
			detailPopup.open();
		});
		
		
		//微信店小二补打
		$('#weixinWaiter_li_tableSelect').click(function(){
			var url = null;
			if(window.location.hostname === 'e-tones.net'){
				url = 'http://wx.' + hostname + ':' + window.location.port + '/wx-term/WxOperateWaiter.do';
			}else{
				url = window.location.origin + '/wx-term/WxOperateWaiter.do';
			}
			
			Util.LM.show();
			$.ajax({
				url : '../OperateRestaurant.do',
				data : {
					dataSource : 'getByCond',
					byId : true
				},
				type : 'post',
				dataType : 'json',
				success : function(data){
					$.ajax({
						type : 'post',
						url : url,
						data : {
							dataSource : 'print',
							restaurantId : data.root[0].id,
							orderId : uo.order.id
						},
						dataType : 'jsonp',
						jsonp : 'callback',
						success : function(data){
							Util.LM.hide();
							if(data.success){
								Util.msg.tip(data.msg);
							}else{
								Util.msg.tip(data.msg);
							}
						},
						error : function(xhr, e){
							Util.LM.hide();
							Util.msg.tip(data.msg);
						}
					});
				}
			});
		});
		
	});
	
});





