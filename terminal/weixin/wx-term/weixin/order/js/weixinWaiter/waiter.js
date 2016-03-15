$(function(){
	var fastFoodWaiterData = {};
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
				//FIXME
				console.log(data);
				if(data.success){
					initFoodList(data.root[0], true);
				}else{
//					location.href = 'linkTimeout.html';
				}
			}
		});
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
					fastFoodWaiterData.tableAlias = data.root[0].tableAlias;
					///赋值账单号
					$('#orderId_font_waiter').text(data.root[0].id);
					
					//赋值给餐台号
					$('#tableNum_font_waiter').text(data.root[0].table.name);
					
					//赋值给开台时间
					$('#openTableTime_font_waiter').text(data.root[0].birthDate);
					
					//赋值给开台人
					$('#openTablePeople_font_waiter').text(data.root[0].waiter);
					
					//加载菜品数据
					initFoodList(data.root[0], false);
				}else{
					//TODO 显示连接超时的界面
//					location.href = 'linkTimeout.html';
				}
			}
		});	
	}
	
	
			
			
	function initFoodList(data, isWxOrder){
		
		var orderListTemplete = '<div class="main-box" style="background-color: cornsilk;">'+
									'<ul class="m-b-list">'+
										'<li style="border-bottom:0px;line-height:10px;">&nbsp;</li>'+
										'<li  class="box-horizontal" style="border-bottom:0px;line-height:15px;">'+
											'<div style="width:85%;"><span data-type="foodIndex">{index}</span>、{foodName}</div>'+
											'<div style="width:15%;"><font style="font-weight:bold;color:green">{foodPrice}元</font></div>'+
										'</li>'+
										'<li style="border-bottom:0px;line-height:10px;">&nbsp;</li>'+	
											'<div class="box-horizontal" style="line-height:15px;border-bottom:0px;">'+
											'<div style="width:98%;"><font style="font-family:Arial;font-size:12px;">{foodUnit}</font></div>'+
										'</div>'+
										'<li style="border-bottom:0px;line-height:10px;">&nbsp;</li>'+
									'</ul>'+
								'</div>';
		
		if(!isWxOrder){
			var html = [];
			data.orderFoods.forEach(function(temp, i){
				html.push(orderListTemplete.format({
					index : i+1,
					foodName : temp.name,
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
					foodPrice : temp.totalPrice,
					foodUnit : temp.tasteGroup.tastePref + '<span style="color:red;float:right;">&nbsp;&nbsp;<strong>(待确认)</strong></span>'
				}));
			});
			$('#orderList_div_waiter').prepend(html.join(''));
		}
		
		$('#orderList_div_waiter').find('[data-type=foodIndex]').each(function(index, element){
			element.innerHTML = index + 1;
		});;
	}
	
	
	
	function setView(){
		var height = document.documentElement.clientHeight;
		$('#weixinWaiter_div_waiter').css('height', height-45);
	}
	
	window.onresize = setView;
	window.onresize();
		
	
	//店小二自助点餐功能弹出按钮
	var orderFoodPopup = null;
	$('#orderBySelf_a_waiter').click(function(){
		
		//建立popup
		orderFoodPopup = new PickFoodComponent({
			bottomId : 'fastFoodBottom_div_waiter',
			//下单键回调  能调用的三个参数_orderData, _commentData, _container
			confirm : function(_orderData, _commentData){
				var foods = '';
				var unitId = 0; 
				
				_orderData.forEach(function(element, index){
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
//						oid : Util.mp.oid,
//						fid : Util.mp.fid,
						sessionId : Util.mp.params.sessionId,
						foods : foods,
						comment : _commentData ? _commentData : '',
						branchId : '',
						tableAlias : fastFoodWaiterData.tableAlias
					},
					success : function(response, status, xhr){
						if(response.success){
							//提示框设置
							var finishOrderDialog = new DialogPopup({
								titleText : '提示tips',
								content : '下单成功,确认返回账单',
								leftText : '确认',
								left : function(){
									//确认后回调
									finishOrderDialog.close(function(){
										orderFoodPopup.closeShopping();
										$('#closeFastFood_a_waiter').click();
										$('#orderList_div_waiter').html('');
										initWaiterOrder();
									});
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
})