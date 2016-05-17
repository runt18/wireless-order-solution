$(function(){
	
	var fastFoodWaiterData = {
		_tableAlias : null,		//餐桌号
		_orderData : null,		//点菜资料
		_commentData : null,	//备注资料
		_orderId : null,		//账单Id
		_orderType : null		//下单方式
	};
	
	var orderType = {
		WX_PAY : 1,				//微信支付下单
		CONFIRM_BY_STAFF : 2,	//确认下单
		DIRECT_ORDER : 3		//直接下单
	}
	
	
	var payParam = null;		//微信支付的参数
	var checkPayTypeDialog;		//下单方式选择框
	var orderFoodPopup;			//点菜container
	var _hasFoods = false;      // 账单是否有菜品
	
	//加载店小二账单信息
	initWaiterOrder();
	
	var tableStatus = {
		IDLE : { val : 0, desc : '空闲'},
		BUSY : { val : 1, desc : '就餐'}
	};
	
	if(Util.mp.params.orderId){
		//店小二
		initTableMsg();
		if(!_hasFoods){
			//自助点餐点击
			$('#orderBySelf_a_waiter').click();
		}
	}else{
		//扫码
		//查看餐桌信息
		$.ajax({
			url : '../../WxOperateWaiter.do',
			data : {
				dataSource : 'getTableStatus',
				sessionId : Util.mp.params.sessionId ? Util.mp.params.sessionId : '',
				tableId : Util.mp.params.tableId ? Util.mp.params.tableId : ''
			},
			type : 'post',
			dataType : 'json',
			success : function(data, status, xhr){
				if(data.success){
					if(data.root[0].statusValue == tableStatus.BUSY.val){						
						initTableMsg();
						if(!_hasFoods){
							//自助点餐点击
							$('#orderBySelf_a_waiter').click();
						}
					}else{
						//自助点餐点击
						$('#orderBySelf_a_waiter').click();
					}	
				}
			}
		});
	}
	
	function initWaiterOrder(){
		
		//获取门店信息
		$.ajax({
			url : '../../WxOperateRestaurant.do',
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
			url : '../../WxOperateRestaurant.do',
			dataType : 'json',
			type : 'post',
			data : {
				dataSource : 'getByCond',
				branchId : Util.mp.params.branchId
			},
			success : function(data, status, xhr){
				fastFoodWaiterData._orderType = data.root[0].defaultOrderType;
			}
		});
		
		
		//加载店小二账单信息
		function initTableMsg(){
			//获取餐桌信息
			$.ajax({
				url : '../../WxOperateWaiter.do',
				data : {
					dataSource : 'getOrder',
					sessionId : Util.mp.params.sessionId,
					orderId : Util.mp.params.orderId
				},
				type : 'post',
				dataType : 'json',
				success : function(data, status, xhr){
					if(data.success){
						
						fastFoodWaiterData._orderId = data.root[0].id;
						
						//获取【待确认】的菜品信息
						$.ajax({
							url : '../../WxOperateOrder.do',
							type : 'post',
							datatype : 'json',
							data : {
								dataSource : 'getByCond',
								sessionId : Util.mp.params.sessionId, 
								status : '2',
								orderId : data.root[0].id
							},
							success : function(res, status, xhr){
								if(res.success && res.root.length > 0){
									initFoodList(res.root[0], true);
								}
							}
						});
						
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
								url : '../../WXOperateMember.do',
								type : 'post',
								dataType : 'json',
								data : {
									dataSource : 'getByCond',
									sessionId : Util.mp.params.sessionId,
									memberId : data.root[0].memberId
								},
								success : function(data, status, xhr){
									
									$('#memberName_span_waiter').text(data.root[0].name);
								}
							});
							
							//赋值会员价金额
							$('#actualPrice_span_waiter').text(data.root[0].actualPrice + '元');
							
							//赋值折扣金额
							$('#discountPrice_span_waiter').text(data.root[0].discountPrice + '元');
						
						}else{
							
							$('#memberName_span_waiter').text('——');
							
							//赋值会员价金额
							$('#actualPrice_span_waiter').text(data.root[0].actualPrice + '元');
							
							//赋值折扣金额
							$('#discountPrice_span_waiter').text(data.root[0].discountPrice + '元');
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
					}else{
						location.href = 'waiterTimeout.html';
					}
				}
			});	 
		}
		
		
		var waiterTabArr = [].slice.call($('#waiterTab_div_waiter').find('[data-type=waiterTab]'));
		waiterTabArr.forEach(function(el, index){
			el.onclick = function(){
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
		
		var orderListTemplete = '<div class="main-box" style="background-color: cornsilk;">'+
									'<ul class="m-b-list">'+
										'<li style="border-bottom:0px;line-height:10px;">&nbsp;</li>'+
										'<li  class="box-horizontal" style="border-bottom:0px;line-height:15px;">'+
											'<div style="width:85%;"><span data-type="foodIndex">{index}</span>、{foodName}<span style="font-size:20px;letter-spacing:4px;color:red;">x{count}</span><span style="color:red;">{discount}</span></div>'+
											'<div style="width:15%;"><font style="font-weight:bold;color:green">{foodPrice}元</font></div>'+
										'</li>'+
										'<li style="border-bottom:0px;line-height:10px;">&nbsp;</li>'+	
											'<div class="box-horizontal" style="line-height:15px;border-bottom:0px;">'+
											'<div style="width:98%;"><font style="font-family:Arial;font-size:12px;">{foodUnit}</font></div>'+
										'</div>'+
										'<li style="border-bottom:0px;line-height:10px;">&nbsp;</li>'+
									'</ul>'+
								'</div>';
		
		//判定是预定订单还是已下单的订单
		if(!isWxOrder){
			var html = [];
			data.orderFoods.forEach(function(temp, i){
				html.push(orderListTemplete.format({
					index : i+1,
					foodName : temp.name,
					count : temp.count,
					discount : temp.discount != 1 ? '(' + (temp.discount * 10) + '折)': '',
					foodPrice : temp.totalPrice,
					foodUnit : temp.tasteGroup.tastePref
				}));
			});
			$('#foodList_div_waiter').html(html.join(''));
			$('#foodAmountTips_span_waiter').html((data.orderFoods.length ? data.orderFoods.length : ''));
			$('#foodAmountTips_span_waiter').css('background', (data.orderFoods.length ? 'red' : ''));
		}else{
			var html = [];
			if(data.code){
				var orderCodeTips = '<div class="main-box" style="background-color: cornsilk;">'+
											'<span style="font-weight:bold; display:block;text-align:center;color:#666;">微订订单号：' + data.code + '</span>' + 
										'</div>';
				html.push(orderCodeTips);
			}
			data.foods.forEach(function(temp, i){
				html.push(orderListTemplete.format({
					index : i+1,
					foodName : temp.foodName,
					count : temp.count,
					discount : temp.discount != 1 ? '(' + (temp.discount * 10) + '折)': '',
					foodPrice : temp.totalPrice,
					foodUnit : temp.tasteGroup.tastePref + '<span style="color:red;float:right;">&nbsp;&nbsp;<strong>(待确认)</strong></span>'
				}));
			});
			$('#orderList_div_waiter').html(html.join(''));
			$('#orderAmountTips_span_waiter').html((data.foods.length ? data.foods.length : ''));
			$('#orderAmountTips_span_waiter').css('background', (data.foods.length ? 'red' : ''));
		}
		
		//标前缀
		$('#foodList_div_waiter').find('[data-type=foodIndex]').each(function(index, element){
			element.innerHTML = index + 1;
			hasFoods = true;
		});
		
		$('#orderList_div_waiter').find('[data-type=foodIndex]').each(function(index, element){
			element.innerHTML = index + 1;
			hasFoods = true;
		});
		
		if(!hasFoods){
			$('#tipsFoods_span_waiter').css({
				'margin' : '40% 0px',
				'display' : 'block'
			});
			$('#tipsOrder_span_waiter').css({
				'margin' : '40% 0px',
				'display' : 'block'
			});
		}else{
			//自助点餐点击
			$('#orderBySelf_a_waiter').click();
			
			$('#tipsFoods_span_waiter').css({
				'margin' : '0px 0px',
				'display' : 'none'
			});
			$('#tipsOrder_span_waiter').css({
				'margin' : '0px 0px',
				'display' : 'none'
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
		//建立popup
		orderFoodPopup = new PickFoodComponent({
			payType : fastFoodWaiterData._orderType,
			bottomId : 'fastFoodBottom_div_waiter',
			//下单键回调  能调用的三个参数_orderData, _commentData, _container
			confirm : function(_orderData, _commentData, _container, _calcOrderCost){
				if(!isProcessing){
					isProcessing = true;
					setTimeout(function(){
						isProcessing = false;
					},2000);
					fastFoodWaiterData._orderData = _orderData;
					fastFoodWaiterData._commentData = _commentData;
					//读取公众号会员数据
					$.ajax({
						url : '../../WXOperateMember.do',
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
										completeFinish : function(){ commit(_calcOrderCost); }
									});
									completeMemberMsgDialog.open();
									
								}else{
									commit(_calcOrderCost);
								}
							}else{
								Util.dialog.show({msg : data.msg});	
							}
						},
						
						error : function(xhr, status, err){
							Util.dialog.show({msg : err.msg});
						}
					});
					
				}
			},
			onCartChange : function(_orderData){
				var foodTypeCount = _orderData.length;
				if(foodTypeCount > 0){
					$('#shoppingCart_i_waiter').html('<span style="color:red;font-weight:bold;">' + foodTypeCount +'</span>');
			
				}else{
					$('#shoppingCart_i_waiter').html('');
				}
			}
		});
		
		
		//弹出所建立的popup
		orderFoodPopup.open(function(){
			$('#weixinWaiter_div_waiter').hide();
			$('#bottom_div_waiter').hide();
			$('#fastFoodBottom_div_waiter').show();
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
	
	//为我评价功能
	$('#reviewService_a_waiter').click(function(){
//		var completeMemberDialog = new CompleteMemberMsg();
//		completeMemberDialog.open();
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
					
					$.ajax({
						url : '../../WxOperateWaiter.do',
						data : {
							dataSource : 'callPay',
							sessionId : Util.mp.params.sessionId,
							orderId : Util.mp.params.orderId,
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
						}
					});
				}
				
			}
		});
		callPayDialog.open();
	});
	
	
	//下单功能数据传递
	function commit(calcOrderCost){
		var foods = '';
		var unitId = 0; 
		
		fastFoodWaiterData._orderData.forEach(function(element, index){
			if(index > 0){
				foods += '&';
			}
			
			if(element.unitPriceId){
				unitId = element.unitPriceId;
				
			}else{
				unitId = 0;
			}
				
			foods += element.id + ',' + element.count + ',' + unitId;
		});
		
		
		var payType = null;
		$('[name=payType_input_waiter]').each(function(index, el){
			if(el.checked == true){
				payType = el.getAttribute('data-type');
			}
		});
		//微信支付下单
		if(fastFoodWaiterData._orderType == orderType.WX_PAY){
			Util.lm.show();
			$.ajax({
				url : '../../WxOperateOrder.do',
				type : 'post',
				dataType : 'json',
				data : {
					dataSource : 'wxPayOrder',
					sessionId : Util.mp.params.sessionId,
					foods : foods,
					cost : calcOrderCost,
					tableAlias : fastFoodWaiterData._tableAlias,
					tableId : Util.mp.params.tableId ? Util.mp.params.tableId : ''
				},
				success : function(data, status, req){
					Util.lm.hide();
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
							}
						})
						dialog.open();
					}
				},
				error : function(req, status, error){
					alert(error);
				}
			});
		}else if(fastFoodWaiterData._orderType == orderType.CONFIRM_BY_STAFF){
			//确认下单
			$.ajax({
				url : '../../WxOperateOrder.do',
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
					orderId : fastFoodWaiterData._orderId,
					print : true
				},
				success : function(response, status, xhr){
					if(response.success){
						//提示框设置
						var finishOrderDialog = new WeDialogPopup({
							titleText : '温馨提示',
							content : '<span style="display:block;text-align:center;">下单成功,等待服务员确认订单</span>',
							leftText : '确认',
							left : function(){
								finishOrderDialog.close();								
							},
							afterClose : function(){
								//关闭后回调
								orderFoodPopup.closeShopping();
								$('#closeFastFood_a_waiter').click();
								$('#foodList_div_waiter').html('');
								initWaiterOrder();
							}
						});
						
						finishOrderDialog.open();
						
					}else{
						Util.dialog.show({msg : response.msg});	
					}
					
				},
				
				error : function(xhr, status, err){
					Util.dialog.show({msg : err.msg});
				}
			});
		}else if(fastFoodWaiterData._orderType == orderType.DIRECT_ORDER){
			//直接下单
			$.ajax({
				url : '../../WxOperateOrder.do',
				type : 'post',
				dataType : 'json',
				data : {
					dataSource : 'insert',
					foods : foods,
					sessionId : Util.mp.params.sessionId,
					tableAlias : fastFoodWaiterData._tableAlias,
					orderId : fastFoodWaiterData._orderId,
					force : true
				},
				success : function(data, status, req){
					if(data.success){
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
								orderFoodPopup.closeShopping();
								$('#closeFastFood_a_waiter').click();
								$('#foodList_div_waiter').html('');
								initWaiterOrder();
							}
						});
						
						finishOrderDialog.open();
					}
				},
				error : function(req, status, err){
					var errDialog;
					errDialog = new WeDialogPopup({
						titleText : '温磬提示',
						content : ('<span style="display:block;text-align:center;">' + err.msg + '</span>'),
						leftText : '确认',
						left : function(){
							errDialog.close();
						}
					});
					errDialog.open();
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
			
		
		
//		checkPayTypeDialog = new WeDialogPopup({
//			titleText : '请选择下单方式',
//			content : (		'<div class="weui_cells weui_cells_radio">'
//            			+		'<label class="weui_cell weui_check_label" for="payByWeiXin_input_waiter">'
//             		  	+				'<div class="weui_cell_bd weui_cell_primary">'
//                  		+					'<p>微信支付下单</p>'
//                		+				'</div>'
//                		+			'<div class="weui_cell_ft">'
//                		+   			'<input type="radio" class="weui_check" name="payType_input_waiter" id="payByWeiXin_input_waiter" data-type="payByWeiXin">'
//                    	+				'<span class="weui_icon_checked"></span>'
//                		+			'</div>'
//            			+		'</label>'
//           				+		'<label class="weui_cell weui_check_label" for="waiterCheck_input_waiter">'
//			            +    		'<div class="weui_cell_bd weui_cell_primary">'
//            		    +   			'<p>服务员下单</p>'
//           			    +   		'</div>'
//            			+  			'<div class="weui_cell_ft">'
//                   		+				'<input type="radio" name="payType_input_waiter" class="weui_check" id="waiterCheck_input_waiter" checked="checked" data-type="waiterCheck">'
//                  		+				'<span class="weui_icon_checked"></span>'
//            			+   		'</div>'
//          				+		'</label>'
//          				+		'<label class="weui_cell weui_check_label" for="fastOrder_input_waiter">'
//             		  	+			'<div class="weui_cell_bd weui_cell_primary">'
//                  		+				'<p>直接下单</p>'
//                		+			'</div>'
//                		+			'<div class="weui_cell_ft">'
//                		+   			'<input type="radio" class="weui_check" name="payType_input_waiter" id="fastOrder_input_waiter" data-type="fastOrder">'
//                    	+				'<span class="weui_icon_checked"></span>'
//                		+			'</div>'
//            			+		'</label>'		
//       			 		+	'</div>') ,
//			leftText : '取消',
//			left : function(){
//				checkPayTypeDialog.close();
//			},
//			rightText : '确认',
//			right : function(){},
//			dismissible : true
//		});
//		checkPayTypeDialog.open();
		
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
							errDialog.close();
						},
						afterClose : function(){
							orderFoodPopup.closeShopping();
							$('#closeFastFood_a_waiter').click();
							$('#foodList_div_waiter').html('');
							if(Util.mp.params.tableId){
								window.location.href = 'orderList.html?sessionId=' + Util.mp.params.sessionId;
							}else{
								initWaiterOrder();
							}
							
							setTimeout(function(){
								window.location.reload();
							}, 2000);
						}
					});
					
					errDialog.open();
				} 
			});
		}
	}
	
})