$(function(){
	initWaiterOrder();
	function initWaiterOrder(orderId){
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
					fastFoodWaiterData.tableAlias = data.root[0].table.Alias;
					///赋值账单号
					$('#orderId_font_waiter').text(data.root[0].id);
					
					//赋值给餐台号
					$('#tableNum_font_waiter').text(data.root[0].tableAlias);
					
					//赋值给开台时间
					$('#openTableTime_font_waiter').text(data.root[0].birthDate);
					
					//赋值给开台人
					$('#openTablePeople_font_waiter').text(data.root[0].waiter);
					
					//加载菜品数据
					initFoodList(data.root[0]);
				}else{
					//TODO 显示连接超时的界面
					location.href = 'linkTimeout.html';
				}
			}
		});		
	}
	
	
			
			
	function initFoodList(data){
		
		var orderListTemplete = '<div class="main-box" style="background-color: cornsilk;">'+
									'<ul class="m-b-list">'+
										'<li style="border-bottom:0px;line-height:10px;">&nbsp;</li>'+
										'<li  class="box-horizontal" style="border-bottom:0px;line-height:15px;">'+
											'<div style="width:90%;">{index}、{foodName}</div>'+
											'<div style="width:10%;"><font style="font-weight:bold;color:green">{foodPrice}元</font></div>'+
										'</li>'+
										'<li style="border-bottom:0px;line-height:10px;">&nbsp;</li>'+	
											'<div class="box-horizontal" style="line-height:15px;border-bottom:0px;">'+
											'<div style="width:98%;"><font style="font-family:Arial;font-size:12px;">{foodUnit}</font></div>'+
										'</div>'+
										'<li style="border-bottom:0px;line-height:10px;">&nbsp;</li>'+
									'</ul>'+
								'</div>';
		
		var html = [];
		
		data.orderFoods.forEach(function(temp, i){
			html.push(orderListTemplete.format({
				index : i+1,
				foodName : temp.foodName,
				foodPrice : temp.totalPrice,
				foodUnit : temp.tasteGroup.tastePref
			}));
		});
		
		
		$('#orderList_div_waiter').html(html.join(''));
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
						oid : Util.mp.oid,
						fid : Util.mp.fid,
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
})