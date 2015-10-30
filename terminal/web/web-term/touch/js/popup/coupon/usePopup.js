function UseCouponPopup(param){
	
	param = param || {
		title : '',		//头部信息
		useMode : null,		//使用类型
		useTo : '',			//对象的使用
		memberName : '', 
		useComment : '',	//备注
		orderId : '',  		//账单id,在useMode是Order时使用
		useCuoponMethod : function(){}	//优惠券使用的回调函数
	};
	
	var _useCouponPopup = null;
	
	this.open = function(afterOpen){
		var availableCoupons = [];
		if(param.orderId){
			//搜索账单已用的
			$.post('../OperateCoupon.do',  {dataSource : 'getAvailableByOrder', orderId : param.orderId, memberId : param.useTo}, function(response, status, xhr){
				if(response.success){
					if(response.root.length > 0){
						availableCoupons = availableCoupons.concat(response.root);
					}
					
					createPopup(availableCoupons, afterOpen);
				}else{
					alert(response.msg);
				}

			}, 'json');
		}else{
			$.post('../OperateCoupon.do',  {dataSource : 'getByCond',status : 'issued', memberId : param.useTo}, function(response, status, xhr){
				if(response.success){
					if(response.root.length > 0){
						availableCoupons = availableCoupons.concat(response.root);
					}
					createPopup(availableCoupons, afterOpen);
				}else{
					alert(response.msg);
				}
			}, 'json');
		}
		
	};
	
	this.close = function(afterOpen, timeout){
		_useCouponPopup.close(afterOpen, timeout);
	};
	
	//创建popup
	function createPopup(availCoupons, afterOpen){
		if(availCoupons.length > 0){
			_useCouponPopup = new JqmPopup({
				loadUrl : './popup/coupon/use.html',
				pageInit : function(self){
					var progressCoupon = "";
					for(var i = 0; i < availCoupons.length; i++){
						var eachProgressCoupon = '<tr>'
											 + '<td style="width:250px">'
											 + '<label style="height:50px"><input type="checkbox" class="useCouponClass" data-theme="e" coupon_id="' + availCoupons[i].couponId + '">' + availCoupons[i].promotion.title + '<font style="float:right" color="red">word</font>' +'</label>'
											 + '</td>'
											 + '</tr>';
							
						if(availCoupons[i].useDate){
							eachProgressCoupon = eachProgressCoupon.replace('word', '已使用');
						}else{
							eachProgressCoupon = eachProgressCoupon.replace('word', ' ');
						}
						progressCoupon += eachProgressCoupon;
					}
					
					
					self.find('[id = useTal_table_use]').append(progressCoupon);
					self.find('[id = useTal_table_use]').trigger('create').trigger('refresh');
					
					//更改标题
					
					if(param.title){
						if(param.memberName){
							self.find('[id=couponUseHeader_div_use]').html('<h3>' + param.title + '--' + param.memberName + '</h3>');
						}else{
							self.find('[id=couponUseHeader_div_use]').html('<h3>' + param.title + '</h3>');
						}
						
					}
					
					
					
					//绑定确定按钮
					self.find('[id = couponUseConfirm_a_use]').click(function(){
						if(param.useCuoponMethod){
							
							var coupons = [];
							self.find('[id="useTal_table_use"] .useCouponClass').each(function(index, element){
								if(element.checked){
									coupons.push($(element).attr('coupon_id'));
								}
							});
							param.useCuoponMethod(coupons);
						}
					});
						
					
					//绑定取消按钮
					self.find('[id = couponUseCancel_a_use]').click(function(){
						_useCouponPopup.close();
					});
				}
			});
			_useCouponPopup.open(afterOpen);
		}else{
			Util.msg.tip('没有优惠券可以使用!'); 
		}
		
	}
}


UseCouponPopup.UseMode = {
	FAST : { mode : 1, desc : '快速' },
	ORDER : { mode : 2, desc : '账单' }
};