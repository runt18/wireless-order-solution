$(function(){
	var fastFoodWaiterData = {
		_tableAlias : null,				//餐桌号
		_orderData : null,				//点菜资料
		_commentData : null,			//备注资料
		_orderId : null,				//账单Id
		_orderType : null,				//下单方式
		_tableId : null,        		//餐桌id
		_orderFoods : null,				//点菜信息
		_wxOrderFoods : null,
		_prefectMemberStauts : null 	//是否显示完善会员资料
	};
	var orderType = {
		WX_PAY : 1,				//微信支付下单
		CONFIRM_BY_STAFF : 2,	//确认下单
		DIRECT_ORDER : 3		//直接下单
	}
	
	var _orderType = {
		values : {
			WX_PAY : {val : 1, desc : '微信支付'},
			CONFIRM_BY_STAFF : {val : 2, desc : '确认下单'},
			DIRECT_ORDER : {val : 3, desc : '直接下单'}
		},
		valueOf : function(orderTypeValue){
			var type;
			for(var key in _orderType.values){
				if(orderTypeValue == _orderType.values[key]['val']){
					type = _orderType.values[key];
				}
			}
			if(type){
				return type;
			}else{
				console.log('输入的下单方式参数有误');
				return null;
			}
		}
	}
	
	var prefectMemberStauts = {
		SHOW_PREFECMEMBER : 0,         //显示完善会员资料
		HIDE_PREFECTMEMBER : 1         //不显示完善会员资料
	}
	
	
	var payParam = null;		//微信支付的参数
	var checkPayTypeDialog;		//下单方式选择框
	var orderFoodPopup;			//点菜container
	var _hasFoods = false;      // 账单是否有菜品
	
	
	//加载店小二账单信息
	initWaiterOrder();
	//加载桌面信息
	initTableMsg();
	
	
	var tableStatus = {
		IDLE : { val : 0, desc : '空闲'},
		BUSY : { val : 1, desc : '就餐'}
	};
	
	//加载店小二账单信息
	function initTableMsg(){
		var isComplete = 0;
		//获取餐桌信息
		$.ajax({
			url : '../../../WxOperateWaiter.do',
			data : {
				dataSource : 'getOrder',
				sessionId : Util.mp.params.sessionId,
				orderId : Util.mp.params.orderId,
				tableId : Util.mp.params.tableId
			},
			async : true,
			type : 'post',
			dataType : 'json',
			beforeSend : function(){
				wxLoadDialog.instance().show();
			},
			success : function(data, status, xhr){
				if(data.success){
					
					fastFoodWaiterData._orderId = data.root[0].id;
					
					fastFoodWaiterData._tableAlias = data.root[0].tableAlias;
					///赋值账单号
					$('#orderId_font_waiter').text(data.root[0].id);
					
					//赋值给餐台号
					$('#tableNum_font_waiter').text(data.root[0].table.name);
					
					//赋值给开台时间
					$('#openTableTime_font_waiter').text(data.root[0].birthDate);
					
					//赋值给开台人
					$('#openTablePeople_font_waiter').text(data.root[0].waiter);
					
					//赋值原价
					$('#actualPriceBeforeDiscount_span_waiter').text(data.root[0].actualPriceBeforeDiscount);
					
					
					//读取账单会员数据
					if(data.root[0].memberId != 0){
						$.ajax({
							url : '../../../WXOperateMember.do',
							type : 'post',
							dataType : 'json',
							data : {
								dataSource : 'getByCond',
								sessionId : Util.mp.params.sessionId,
								memberId : data.root[0].memberId
							},
							success : function(data, status, xhr){
								if(data.success){
									$('#memberName_span_waiter').text(data.root[0].name);
								}else if(data.code == '7546'){
									sessionTimeout();
								}else{
									Util.showErrorMsg(data.msg);
								}
							},
							error : function(req, status, err){
								if(err.code == '7546'){
									sessionTimeout();
								}else{
									Util.showErrorMsg(err.msg);					
								}
							}
						});
						
						//赋值会员价金额
						$('#actualPrice_span_waiter').text(data.root[0].actualPrice);
						
						//赋值折扣金额
						$('#discountPrice_span_waiter').text(data.root[0].discountPrice);
					
					}else{
						
						$('#memberName_span_waiter').text('——');
						
						//赋值会员价金额
						$('#actualPrice_span_waiter').text(data.root[0].actualPrice);
						
						//赋值折扣金额
						$('#discountPrice_span_waiter').text(data.root[0].discountPrice);
					}
					
					//加载菜品数据
					initFoodList(data.root[0], false);
					
					//已经结账的账单处理
					if(data.root[0].statusValue !== 0){
						$('#bottom_div_waiter').css({
							'background' : 'rgb(228, 240, 245)',
							'padding' : '4px 0'
						});
						$('#bottom_div_waiter').html('<span style="display:block;color:#156785;font-size:34px;text-align:center;">账单已结账</span>');
					}
				}else if(data.code == '7546'){
					sessionTimeout();	
				}
			},
			error : function(req, status, err){
				Util.showErrorMsg(err.msg);
			},
			complete : function(){
				isComplete ++;
			}
		});	 
		
		//获取【待确认】的菜品信息
		$.ajax({
			url : '../../../WxOperateOrder.do',
			type : 'post',
			datatype : 'json',
			async : true,
			data : {
				dataSource : 'getByCond',
				sessionId : Util.mp.params.sessionId, 
				status : '2',
				orderId : Util.mp.params.orderId,
				tableId : Util.mp.params.tableId
			},
			success : function(res, status, xhr){
				if(res.success){
					for(var i = (res.root.length - 1); i >= 0; i--){
						initFoodList(res.root[i], true);
					}
				}else if(res.code == '7546'){
					sessionTimeout();
				}else{
					Util.showErrorMsg(data.msg);
				}
			},
			error : function(req, status, err){
				if(err.code == '7546'){
					sessionTimeout();
				}else{
					Util.showErrorMsg(err.msg);					
				}
			},
			complete : function(){
				isComplete ++;
			}
		});
		(function(){
			if(isComplete == 2){
				wxLoadDialog.instance().hide();
				if(!fastFoodWaiterData._orderFoods && !fastFoodWaiterData._wxOrderFoods){
					$('#orderBySelf_a_waiter').click();
				}			
			}else{
				setTimeout(arguments.callee,200);
			}
		})();
	}
	
	function initWaiterOrder(){
		
		//获取门店信息
		$.ajax({
			url : '../../../WxOperateRestaurant.do',
			dataType : 'json',
			type : 'post',
			data : {
				dataSource : 'detail',
				branchId : Util.mp.params.branchId
			},
			success : function(data, status, xhr){
				document.title = data.root[0].name;
			}
		});
		
		//获取门店信息
		$.ajax({
			url : '../../../WxOperateRestaurant.do',
			dataType : 'json',
			type : 'post',
			data : {
				dataSource : 'getByCond',
				branchId : Util.mp.params.branchId
			},
			success : function(data, status, xhr){
				fastFoodWaiterData._orderType = data.root[0].defaultOrderType;
				fastFoodWaiterData._prefectMemberStauts = data.root[0].prefectMemberStatus;
			}
		});
		
		//tab点击事件
		var waiterTabArr = [].slice.call($('#waiterTab_div_waiter').find('[data-type=waiterTab]'));
		waiterTabArr.forEach(function(el, index){
			el.onclick = function(){
//				$($(el).children().get(0)).addClass('checkTab');
//				$($(el).siblings().children().get(0)).removeClass('checkTab');
				$(el).addClass('checkTab');
				$(el).siblings().removeClass('checkTab');
				if(el.getAttribute('data-name') == 'foodListTab_waiter'){
					$('#containerList_div_waiter').css('margin-left', '0');
				}else{
					$('#containerList_div_waiter').css('margin-left', '-100%');
				}
			}
		});
		
	}
	
	
			
			
	function initFoodList(data, isWxOrder){
		
		var orderListTemplete = '<div style="background-color: #fff;background-color: #fff;border: 1px solid #E4E3E3;border-radius: 5px;margin: 5px;padding: 4px;font-size: 1.1em;padding-bottom:0;">'+
									'<ul>'+
										'<li style="border-bottom:0px;line-height:10px;">&nbsp;</li>'+
										'<li  class="box-horizontal" style="border-bottom:0px;line-height:15px;">'+
											'<div style="width:85%;"><span data-type="foodIndex" style="display: -webkit-box;-webkit-line-clamp: 2;-webkit-box-orient: vertical;overflow: hidden;font-size: 12px;">{index}</span>、{foodName}<span style="font-size:20px;letter-spacing:4px;color:green;">x{count}</span><span style="color:green;">{discount}</span></div>'+
											'<div style="width:15%;"><font style="font-weight:bold;color:green">{foodPrice}元</font></div>'+
										'</li>'+
										'<li style="border-bottom:0px;line-height:10px;">&nbsp;</li>'+	
											'<div class="box-horizontal" style="line-height:15px;border-bottom:0px;">'+
											'<div style="width:98%;"><font style="font-family:Arial;font-size:12px;">{foodUnit}</font></div>'+
										'</div>'+
										'<li style="border-bottom:0px;line-height:10px;">&nbsp;</li>'+
									'</ul>'+
								'</div>';
								
								
		var bodyTemplate = ' <div style="position:relative;background-color: #fff;background-color: #fff;border-bottom: 1px solid #E4E3E3;margin:0 5px;padding: 4px;font-size: 1.1em;padding-bottom:0;"> ' +
							' <div style="display:inline-block;width:21;padding-left:0.2em;padding-right:0.2em;"> ' +
								' <img data-type="foodIndex" src="{imgUrl}" alt="" style="width:50px;height:50px;"> ' +
							' </div> ' +
							' <div style="max-height:5em;display: -webkit-box;-webkit-line-clamp: 2;-webkit-box-orient: vertical;overflow: hidden;position:absolute;top:2px;left: 21%;padding-left:0.5em;padding-right:0.5em;width:50%;">{foodName} ' +
							' </div> ' +
							' <div style="display:inline-block;float:right;max-width:20%;"> ' +
								' <span style="color:#f91313;">{foodPrice}元</span><br> ' +
								' <span style="font-size: 13px;text-align:right;display:inline-block;width:100%;">×{foodAmount}</span> ' +
							' </div> ' +
						' </div> ';
		
		//判定是预定订单还是已下单的订单
		if(!isWxOrder){
			var html = [];
			data.orderFoods.forEach(function(temp, i){
				html.push(bodyTemplate.format({
					foodName : temp.name,
					foodAmount : temp.count,
					foodPrice : temp.totalPrice,
					imgUrl : temp.img ? temp.img.thumbnail : 'images/noImage.jpg'
				}));
				
			});
			var orderMainBox = '<div style="background-color: #fff;border: 1px solid #E4E3E3;border-radius: 5px;margin: 5px;padding: 4px;font-size: 1.1em;padding-bottom:0;">';
			$('#foodList_div_waiter').html(orderMainBox + html.join('') + '</div>');
			$('#foodList_div_waiter').children().css('min-height', $('#foodList_div_waiter').height() - 10);
		}else{
			var html = [];
			if(data.code){
				var orderCodeTips = '<div style="background-color: #fff;background-color: #fff;border: 1px solid #E4E3E3;border-radius: 5px;margin: 5px;padding: 4px;font-size: 1.1em;padding-bottom:0;">'+
											'<span style="font-weight:bold; display:block;text-align:center;color:#666;">微订订单号：' + data.code + '</span>' + 
										'</div>';
				html.push(orderCodeTips);
			}
			data.foods.forEach(function(temp, i){
				html.push(bodyTemplate.format({
					foodName : temp.foodName + '<span style="color:#283892;float:right;">&nbsp;&nbsp;<strong>(待确认)</strong></span>',
					foodAmount : temp.count,
					foodPrice : temp.totalPrice,
					imgUrl : temp.img ? temp.img.thumbnail : '../../images/noImage.jpg'
				}));
			});
			$('#orderList_div_waiter').prepend(html.join(''));
		}
		
		//标前缀
		var foodListCount;
		$('#foodList_div_waiter').find('[data-type=foodIndex]').each(function(index, element){
			element.innerHTML = index + 1;
			foodListCount = index + 1;
		});
		
		//已确认小图标
		if(foodListCount){
			$('#foodAmountTips_span_waiter').html((foodListCount ? foodListCount : ''));
			$('#foodAmountTips_span_waiter').css('background', (foodListCount ? '#ff7d7c' : ''));
		}
		
		var orderListCount;
		$('#orderList_div_waiter').find('[data-type=foodIndex]').each(function(index, element){
			element.innerHTML = index + 1;
			orderListCount = index + 1;
		});
		
		//待确认小图标
		if(orderListCount){
			$('#orderAmountTips_span_waiter').html((orderListCount ? orderListCount : ''));
			$('#orderAmountTips_span_waiter').css('background', (orderListCount ? '#ff7d7c' : ''));
		}
		
		if(data.foods && data.foods.length > 0 || data.orderFoods && data.orderFoods.length > 0){
			if(data.orderFoods){
				fastFoodWaiterData._orderFoods = data.orderFoods;
			}
			
			if(data.foods){
				fastFoodWaiterData._wxOrderFoods = data.foods;
			}
			
			$('#tipsFoods_span_waiter').css({
				'margin' : '0px 0px',
				'display' : 'none'
			});
			$('#tipsOrder_span_waiter').css({
				'margin' : '0px 0px',
				'display' : 'none'
			});
			
			if(data.foods && !data.orderFoods && !fastFoodWaiterData._orderFoods){
				$('[data-name=orderListTab_waiter]').click();
			}
			
		}else{
			//自助点餐点击
			$('#tipsFoods_span_waiter').html('客官，你还没点菜..');
			$('#tipsFoods_span_waiter').css({
				'margin' : '40% 0px',
				'display' : 'block'
			});
			
			$('#tipsOrder_span_waiter').html('客官，你还没点菜..');
			$('#tipsOrder_span_waiter').css({
				'margin' : '40% 0px',
				'display' : 'block'
			});
		}
	}
	
	
	//设置菜品视图大小
	function setView(){
		var height = document.documentElement.clientHeight;
		var headerHeight = document.getElementById('orderHeader_div_waiter').offsetHeight;
		var bottomHeight = document.getElementById('bottom_div_waiter').offsetHeight;
		$('#foodViewList_div_waiter').css({
			'height' : (height - headerHeight - bottomHeight)
		});
		var viewHeight = $('#foodViewList_div_waiter').height();
		$('#foodList_div_waiter').css({
			'height' : viewHeight * 0.98
		});
		$('#orderList_div_waiter').css({
			'height' : viewHeight * 0.98
		});
	}
	
	window.onresize = setView;
	window.onresize();
	
	//店小二自助点餐功能弹出按钮
	$('#orderBySelf_a_waiter').click(function(){
		//防止连点下单
		var isProcessing = false;
		
		var createFastOrderFood;
		createFastOrderFood = new CreateFastOrderFood({
			confirmText : function(checkBtn){
				$(checkBtn).children().text(_orderType.valueOf(fastFoodWaiterData._orderType) ? _orderType.valueOf(fastFoodWaiterData._orderType).desc : '选好了');
			},
			confirm : function(_orderData){
				console.log(_orderData);
				if(!isProcessing){
					isProcessing = true;
					setTimeout(function(){
						isProcessing = false;
					},2000);
					fastFoodWaiterData._orderData = _orderData;
					//读取公众号会员数据
					if(fastFoodWaiterData._prefectMemberStauts == prefectMemberStauts.SHOW_PREFECMEMBER){//显示完善会员资料
						$.ajax({
							url : '../../../WXOperateMember.do',
							type : 'post',
							dataType : 'json',
							data : {
								dataSource : 'getByCond',
								sessionId : Util.mp.params.sessionId
							},
	
							success : function(data, status, xhr){
								if(data.success){
									//判断会员的信息是否补全
									if(data.root[0].isRaw){
										var completeMemberMsgDialog = new CompleteMemberMsg({
											sessionId : Util.mp.params.sessionId,
											completeFinish : function(){ commit(createFastOrderFood); }
										});
										completeMemberMsgDialog.open();
										
									}else{
										commit(createFastOrderFood);
									}
								}else if(data.code == '7546'){
									sessionTimeout();	
								}else{
									Util.showErrorMsg(data.msg);
								}
							},
						
							error : function(xhr, status, err){
								if(err.code == '7546'){
									sessionTimeout();
								}else{
									Util.showErrorMsg(err.msg);					
								}
							}
						});
					
					}else{
						commit(createFastOrderFood);
					}
				}
			}
		});
		createFastOrderFood.open(function(){
			$('#weixinWaiter_div_waiter').hide();
			$('#bottom_div_waiter').hide();
		});
	});
	
	//返回按钮
	$('#closeFastFood_a_waiter').click(function(){
		orderFoodPopup.close(function(){
			$('#weixinWaiter_div_waiter').show();
			$('#bottom_div_waiter').show();
			$('#fastFoodBottom_div_waiter').hide();
			$('#shoppingCart_i_waiter').html('');
		});
	});
	
	//购物车按钮设置
	$('#shoppingCarControler_a_waiter').click(function(){
		orderFoodPopup.openShopping();
	});
	
	//刷新
	$('#waiterRefresh_waiter').click(function(){
		$('#foodList_div_waiter').html('');
		$('#orderList_div_waiter').html('');
		updateMsg();
	});
	
	//为我评价功能
	$('#reviewService_a_waiter').click(function(){
		var remindDialog = new WeDialogPopup({
			leftText : '确定',
			left : function(){
				remindDialog.close();
			},
			content : '<span style="display: block;text-align: center;font-size: 16px;">尽请期待<span>',
			titleText : '温磬提示'
		});
		remindDialog.open();
	});
	
	//呼叫结账按钮
	$('#callPay_li_waiter').click(function(){
		var callPayDialog;
		callPayDialog = new WeDialogPopup({
			titleText : '呼叫结账',
			content : '<div>选择付款方式:</div>'+ 
					'<div style="margin-left:-10px;">' +
					'<ul class="m-b-list">' +
					'<li class="box-horizontal" style="line-height: 40px;font-size:18px;">' +
					'<div data-type="payType" data-value="现金" class="region_css_book weSelectedRegion_css_book" style="width:31%;border:1px solid #666;" href="#">'+
					'<ul class="m-b-list">现金</ul>' +
					'</div>' +
					'<div data-type="payType" data-value="刷卡" class="region_css_book" style="width:31%;border:1px solid #666;" href="#">' +
					'<ul class="m-b-list">刷卡</ul>'+
					'</div>' +
					'<div data-type="payType" data-value="其他" class="region_css_book" style="width:%;border:1px solid #666;" href>' +
					'<ul class="m-b-list">其他</ul>' +
					'</div>' +
					'</li>' +
					'</ul>' +
					'</div>',
			contentCallback : function(self){
				self.find('[data-type="payType"]').each(function(index, element){
					element.onclick = function(){
						if($(element).hasClass('weSelectedRegion_css_book')){
							$(element).addClass('weSelectedRegion_css_book');
						}else{
							self.find('[data-type="payType"]').removeClass('weSelectedRegion_css_book');
							$(element).addClass('weSelectedRegion_css_book');
						}
					}
				});
			},
			leftText : '取消',
			left : function(){
				callPayDialog.close();
			},
			rightText : '确认',
			right : function(self){
				
				//防止连点
				var isProcessing = false;
				if(!isProcessing){
					isProcessing = true;
					setTimeout(function(){
						isProcessing = false;
					}, 200);
					
					var payType = null;
					self.find('[data-type="payType"]').each(function(index, element){
						if($(element).hasClass('selectedRegion_css_book')){
							payType = $(element).attr('data-value');
						}
					});
					wxLoadDialog.instance().show();
					$.ajax({
						url : '../../../WxOperateWaiter.do',
						data : {
							dataSource : 'callPay',
							sessionId : Util.mp.params.sessionId,
							orderId : Util.mp.params.orderId,
							tableId : Util.mp.params.tableId,
							payType : payType
						},
						type : 'post',
						dataType : 'json',
						success : function(data){
							if(data.success){
								callPayDialog.close(function(){
									var callSuccess = new WeDialogPopup({
										titleText : '温馨提示',
										content : '<span style="display:block;text-align:center;">呼叫成功</span>',
										leftText : '确认',
										left : function(){
											callSuccess.close();
										}
									});
									callSuccess.open();
								}, 200);
							}else{
								callPayDialog.close(function(){
									var callFailure = new WeDialogPopup({
										titleText : '温馨提示',
										content : '呼叫失败',
										leftText : '确认',
										left : function(){
											callFailure.close();
										}
									});
									callFailure.open();
								}, 200);
							}
						},
						complete : function(){
							wxLoadDialog.instance().hide();
						}
					});
				}
				
			}
		});
		callPayDialog.open();
	});
	
	//下单功能数据传递
	function commit(container){
		var foods = '';
		var unitId = 0; 
		
		fastFoodWaiterData._orderData.forEach(function(element, index){
			if(index > 0){
				foods += '&';
			}
			
			if(element.unitPriceId){
				unitId = element.selectedUnitPrice.id;
				
			}else{
				unitId = 0;
			}
				
			foods += element.food.id + ',' + element.count + ',' + unitId;
		});
		
		var payType = null;
		$('[name=payType_input_waiter]').each(function(index, el){
			if(el.checked == true){
				payType = el.getAttribute('data-type');
			}
		});
		
		//微信支付下单
		if(fastFoodWaiterData._orderType == orderType.WX_PAY){
			$.ajax({
				url : '../../../WxOperateOrder.do',
				type : 'post',
				dataType : 'json',
				data : {
					dataSource : 'calcOrder',
					sessionId : Util.mp.params.sessionId,
					foods : foods,
					branchId : Util.mp.params.branchId
				},
				success : function(data, status, req){
					if(data.success){
						_calcOrderCost = data.root[0].actualPrice;
						
						wxLoadDialog.instance().show();
						$.ajax({
							url : '../../../WxOperateOrder.do',
							type : 'post',
							dataType : 'json',
							data : {
								dataSource : 'wxPayOrder',
								sessionId : Util.mp.params.sessionId,
								foods : foods,
								cost : _calcOrderCost,
								tableAlias : fastFoodWaiterData._tableAlias,
								tableId : Util.mp.params.tableId ? Util.mp.params.tableId : ''
							},
							success : function(data, status, req){
								wxLoadDialog.instance().hide();
								if(data.success){
									payParam = data.other;
									if(typeof WeixinJSBridge == 'undefined'){
										if (document.addEventListener) {
											document.addEventListener('WeixinJSBridgeReady', onBridgeReady,	false);
										} else if (document.attachEvent) {
											document.attachEvent('WeixinJSBridgeReady', onBridgeReady);
											document.attachEvent('onWeixinJSBridgeReady', onBridgeReady);
										}
									}else{
										onBridgeReady();
									} 
									
								}else{
									payParam = null;
									var dialog = new WeDialogPopup({
										content : data.msg,
										titleText : '微信支付失败',
										leftText : '确认',
										left : function(){
											dialog.close();
										},
										afterClose : function(){
//											window.location.reload();
											reload(container);
										}
									})
									dialog.open();
								}
							},
							error : function(req, status, error){
								wxLoadDialog.instance().hide();
								var dialog = new WeDialogPopup({
									content : data.msg,
									titleText : '微信支付失败',
									leftText : '确认',
									left : function(){
										dialog.close();
									},
									afterClose : function(){
//										window.location.reload();
										reload(container);
									}
								})
								dialog.open();
							}
						});
					}else if(data.code == '7546'){
						sessionTimeout();
					}else{
						Util.showErrorMsg(data.msg);
					}
				},
				error : function(req, status, err){
					$('#actualPrice_div_fastOrderFood').html('----');
					if(err.code == '7546'){
						sessionTimeout();
					}else{
						Util.showErrorMsg(err.msg);					
					}
				}
			});
		}else if(fastFoodWaiterData._orderType == orderType.CONFIRM_BY_STAFF){
			//确认下单
			wxLoadDialog.instance().show();
			$.ajax({
				url : '../../../WxOperateOrder.do',
				type : 'post',
				dataType : 'json',
				data : {
					dataSource : 'insertOrder',
	//				oid : Util.mp.oid,
	//				fid : Util.mp.fid,
					sessionId : Util.mp.params.sessionId,
					foods : foods,
					comment : fastFoodWaiterData._commentData ? fastFoodWaiterData._commentData : '',
					branchId : Util.mp.params.branchId,
					tableAlias : fastFoodWaiterData._tableAlias,
					tableId : Util.mp.params.tableId,
					orderId : fastFoodWaiterData._orderId,
					print : true
				},
				success : function(data, status, xhr){
					wxLoadDialog.instance().hide();
					if(data.success){
						//提示框设置
						var finishOrderDialog = new WeDialogPopup({
							titleText : '温馨提示',
							content : '<span style="display:block;text-align:center;">下单成功,等待服务员确认订单</span>',
							leftText : '确认',
							left : function(){
								finishOrderDialog.close();								
							},
							afterClose : function(){
								reload(container);
							}
						});
						
						finishOrderDialog.open();
						
					}else if(data.code == '7546'){
						sessionTimeout();
					}else{
						Util.showErrorMsg(data.msg);
					}
					
				},
				
				error : function(xhr, status, err){
					wxLoadDialog.instance().hide();
					if(err.code == '7546'){
						sessionTimeout();
					}else{
						Util.showErrorMsg(err.msg);					
					}
				}
			});
		}else if(fastFoodWaiterData._orderType == orderType.DIRECT_ORDER){
			wxLoadDialog.instance().show();
			//直接下单
			$.ajax({
				url : '../../../WxOperateOrder.do',
				type : 'post',
				dataType : 'json',
				data : {
					dataSource : 'insert',
					foods : foods,
					sessionId : Util.mp.params.sessionId,
					tableAlias : fastFoodWaiterData._tableAlias,
					tableId : Util.mp.params.tableId,
					orderId : fastFoodWaiterData._orderId,
					force : true
				},
				success : function(data, status, req){
					if(data.success){
						wxLoadDialog.instance().hide();
						//提示框设置
						var finishOrderDialog;
						finishOrderDialog = new WeDialogPopup({
							titleText : '温馨提示',
							content : '<span style="display:block;text-align:center;">下单成功,厨房正在安排,请稍等</span>',
							leftText : '确认',
							left : function(){
								finishOrderDialog.close();								
							},
							afterClose : function(){
								//关闭后回调
//								window.location.reload();
								reload(container);
							},
							dismissable : true
						});
						
						finishOrderDialog.open();
					}else if(data.code == '7546'){
						sessionTimeout();
					}else{
						Util.showErrorMsg(data.msg);
					}
				},
				error : function(req, status, err){
					wxLoadDialog.instance().hide();
					if(err.code == '7546'){
						sessionTimeout();
					}else{
						Util.showErrorMsg(err.msg);					
					}
				}
			});
		}else{
			var errDialog;
			errDialog = new WeDialogPopup({
				titleText : '温磬提示',
				content : ('<span style="display:block;text-align:center;">餐厅暂时不支持微信下单</span>'),
				leftText : '确认',
				left : function(){
					errDialog.close();
				}
			});
			
			errDialog.open();
		}
		
	}
	
	
	//微信支付回调函数
	function onBridgeReady(){
		if(payParam){
			WeixinJSBridge.invoke('getBrandWCPayRequest', {
				// 以下参数的值由BCPayByChannel方法返回来的数据填入即可
				"appId" : payParam.appId,
				"timeStamp" : payParam.timeStamp,
				"nonceStr" : payParam.nonceStr,
				"package" : payParam.package,
				"signType" : payParam.signType,
				"paySign" : payParam.paySign
			}, function(res) {
				if (res.err_msg == "get_brand_wcpay_request:ok") {
					// 使用以上方式判断前端返回,微信团队郑重提示：res.err_msg将在用户支付成功后返回ok，但并不保证它绝对可靠。
					
					var orderSuccessDialog;
					orderSuccessDialog = new WeDialogPopup({
						titleText : '温磬提示',
						content : ('<span style="display:block;text-align:center;">微信支付下单成功,厨房正在准备,请稍等</span>'),
						leftText : '确认',
						left : function(){
							orderSuccessDialog.close();
						},
						afterClose : function(){
							orderFoodPopup.closeShopping();
							$('#closeFastFood_a_waiter').click();
							$('#foodList_div_waiter').html('');
							if(Util.mp.params.tableId){
								window.location.href = 'orderList.html?sessionId=' + Util.mp.params.sessionId + "&m=" + Util.mp.oid + "&r=" + Util.mp.fid;
							}else{
								initWaiterOrder();
							}
							
							setTimeout(function(){
								reload(container);
							}, 2000);
						}
					});
					
					orderSuccessDialog.open();
				} 
			});
		}
	}
	
	//更新数据
	function updateMsg(){
		$('#foodList_div_waiter').html('');
		$('#orderList_div_waiter').html('');
		//加载店小二账单信息
		initWaiterOrder();
		//加载桌面信息
		initTableMsg();
	}
	
	function reload(container){
		container.close();
		$('#weixinWaiter_div_waiter').show();
		$('#bottom_div_waiter').show();
		wxLoadDialog.instance().show();
		setTimeout(function(){
			updateMsg();
			wxLoadDialog.instance().hide();
		});
//		var reloadHref = window.location.href;
//		window.location.href = reloadHref;
//		window.location.reload();
	}
	
	function sessionTimeout(){
		var sessionTimeoutPopup;
		sessionTimeoutPopup = new WeDialogPopup({
			titleText : '温磬提示',
			content : ('<span style="display:block;text-align:center;">链接已过期,请重新扫二维码</span>'),
			leftText : '确认',
			left : function(){
				sessionTimeoutPopup.close();
			},
			afterClose : function(){
				wx.closeWindow();
			}
		});
		sessionTimeoutPopup.open();
	}
})