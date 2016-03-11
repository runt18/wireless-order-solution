$(function(){
	var height = window.innerHeight || document.body.clientHeight || document.documentElement.clientHeight;
//	var height = $('window').height() || $('document.body').height();
	$('#bigbox').css({height : height - 50});
	
	
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
						branchId : ''
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
										orderFoodPopup.close(function(_orderData){
											$('#bigbox').show();
											$('#waiterBottom_div_waiter').show();
											$('#fastFoodBottom_div_waiter').hide();
										});
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
			$('#bigbox').hide();
			$('#waiterBottom_div_waiter').hide();
			$('#fastFoodBottom_div_waiter').show();
		});
	});
	
	
	//返回按钮
	$('#closeFastFood_a_waiter').click(function(){
		orderFoodPopup.close(function(){
			$('#bigbox').show();
			$('#waiterBottom_div_waiter').show();
			$('#fastFoodBottom_div_waiter').hide();
		});
	});
	
	//购物车按钮设置
	$('#shoppingCarControler_a_waiter').click(function(){
		orderFoodPopup.openShopping();
	});
})