$(function(){
	var fastFoodWaiterData = {
		_tableAlias : null,		//餐桌号
		_orderData : null,		//点菜资料
		_commentData : null		//备注资料
	};
	initWaiterOrder();
	function initWaiterOrder(){
		$.ajax({
			url : '../../WxOperateOrder.do',
			type : 'post',
			datatype : 'json',
			data : {
				dataSource : 'getByCond',
				sessionId : Util.mp.params.sessionId, 
				status : '2'
			},
			success : function(data, status, xhr){
				if(data.success){
					if(data.root.length > 0){
						initFoodList(data.root[0], true);
					}
					
				}else{
					location.href = 'waiterTimeout.html';
				}
			}
		});
		
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
					fastFoodWaiterData._tableAlias = data.root[0].tableAlias;
					///赋值账单号
					$('#orderId_font_waiter').text(data.root[0].id);
					
					//赋值给餐台号
					$('#tableNum_font_waiter').text(data.root[0].table.name);
					
					//赋值给开台时间
					$('#openTableTime_font_waiter').text(data.root[0].birthDate);
					
					//赋值给开台人
					$('#openTablePeople_font_waiter').text(data.root[0].waiter);
					
					//赋值会员价金额
					$('#actualPrice_span_waiter').text(data.root[0].actualPrice);
					
					//赋值折扣金额
					$('#discountPrice_span_waiter').text(data.root[0].discountPrice);
					
					//赋值原价
					$('#actualPriceBeforeDiscount_span_waiter').text(data.root[0].actualPriceBeforeDiscount);
					
					
					//读取账单会员数据
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
					
					$('memberName_span_waiter').text();
					
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
			$('#orderList_div_waiter').append(html.join(''));
		}else{
			var html = [];
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
			$('#orderList_div_waiter').prepend(html.join(''));
		}
		
		//标前缀
		$('#orderList_div_waiter').find('[data-type=foodIndex]').each(function(index, element){
			element.innerHTML = index + 1;
			if(index >= 0){
				$('#tips_span_waiter').html('');
			}
		});
	}
	
	
	//设置菜品视图大小
	function setView(){
		var height = document.documentElement.clientHeight;
		var bottomHeight = document.getElementById('bottom_div_waiter').offsetHeight;
		$('#weixinWaiter_div_waiter').css('height', height - bottomHeight);
	}
	
	window.onresize = setView;
	window.onresize();
		
	
	//店小二自助点餐功能弹出按钮
	var orderFoodPopup = null;
	$('#orderBySelf_a_waiter').click(function(){
		
		//防止连点下单
		var isProcessing = false;
		//建立popup
		orderFoodPopup = new PickFoodComponent({
			bottomId : 'fastFoodBottom_div_waiter',
			//下单键回调  能调用的三个参数_orderData, _commentData, _container
			confirm : function(_orderData, _commentData){
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
							sessionId : Util.mp.params.sessionId,
						},

						success : function(data, status, xhr){
							if(data.success){
								//判断会员的信息是否补全
								if(data.root[0].isRaw){
									var completeMemberMsgDialog = new CompleteMemberMsg({
										sessionId : Util.mp.params.sessionId,
										completeFinish : commit
									});
									completeMemberMsgDialog.open();
									
								}else{
									commit();
								}
							}else{
								Util.dialog.show({msg : response.msg});	
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
//	$('#reviewService_a_waiter').click(function(){
//		var completeMemberDialog = new CompleteMemberMsg();
//		completeMemberDialog.open();
//	});
	
	//呼叫结账按钮
	$('#callPay_li_waiter').click(function(){
		var callPayDialog = new DialogPopup({
			titleText : '呼叫结账',
			content : '<div>选择付款方式:</div>'+ 
					'<div style="margin-left:-10px;">' +
					'<ul class="m-b-list">' +
					'<li class="box-horizontal" style="line-height: 40px;font-size:18px;">' +
					'<div data-type="payType" data-value="现金" class="region_css_book selectedRegion_css_book" style="width:31%;" href="#">'+
					'<ul class="m-b-list">现金</ul>' +
					'</div>' +
					'<div data-type="payType" data-value="刷卡" class="region_css_book" style="width:31%;" href="#">' +
					'<ul class="m-b-list">刷卡</ul>'+
					'</div>' +
					'<div data-type="payType" data-value="其他" class="region_css_book" style="width:%;" href>' +
					'<ul class="m-b-list">其他</ul>' +
					'</div>' +
					'</li>' +
					'</ul>' +
					'</div>',
			contentCallback : function(self){
				self.find('[data-type="payType"]').each(function(index, element){
					element.onclick = function(){
						if($(element).hasClass('selectedRegion_css_book')){
							$(element).addClass('selectedRegion_css_book');
						}else{
							self.find('[data-type="payType"]').removeClass('selectedRegion_css_book');
							$(element).addClass('selectedRegion_css_book');
						}
					}
				});
			},
			leftText : '确认',
			left : function(self){
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
								var callSuccess = new DialogPopup({
									titleText : '温馨提示',
									content : '呼叫成功',
									leftText : '确认',
									left : function(){
										callSuccess.close();
									}
								});
								callSuccess.open();
							}, 200);
						}else{
							callPayDialog.close(function(){
								var callFailure = new DialogPopup({
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
		});
		callPayDialog.open();
	});
	
	
	//下单功能数据传递
	function commit(){
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
				branchId : '',
				tableAlias : fastFoodWaiterData._tableAlias,
				print : true
			},
			success : function(response, status, xhr){
				if(response.success){
					//提示框设置
					var finishOrderDialog = new DialogPopup({
						titleText : '温馨提示',
						content : '下单成功,确认返回账单',
						leftText : '确认',
						left : function(){
							finishOrderDialog.close();								
						},
						afterClose : function(){
							//关闭后回调
							orderFoodPopup.closeShopping();
							$('#closeFastFood_a_waiter').click();
							$('#orderList_div_waiter').html('');
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
	
	}
})