function UseCouponPopup(param){
	
	param = param || {
		header : '',	//头部信息
		useMode : null,	//使用类型
		useTo : '',		//对象的使用
		useCoupon : '',	//使用的优惠券
		useComment : '',	//备注
		orderId : ''  //账单id
	};
	
	var _useCouponPopup = null;
	
	this.open = function(afterOpen){
		var availableCoupons = [];
		if(param.orderId){
			//搜索账单已用的
			$.post('../OperateCoupon.do',  {dataSource : 'getByCond', useMode : UseCoupon.UseMode.ORDER.mode, useAssociateId : param.orderId}, function(response, status, xhr){
				if(response.success && response.root.length > 0){
					availableCoupons = availableCoupons.concat(response.root);
				}
				//搜索会员可用的
				$.post('../OperateCoupon.do',  {dataSource : 'getByCond', status : 'issued', memberId : param.useTo}, function(result){
					if(result.success && result.root.length > 0){
						availableCoupons = availableCoupons.concat(result.root);
						createPopup(availableCoupons, afterOpen);
					}
				});
			});
		}else{
			$.post('../OperateCoupon.do',  {dataSource : 'getByCond', memberId : param.useTo}, function(response, status, xhr){
				if(response.success && response.root.length > 0){
					availableCoupons = availableCoupons.concat(response.root);
					createPopup(availableCoupons, afterOpen);
				}
			});
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
											 + '<label style="height:50px"><input type="checkbox" class="useCouponClass" data-theme="e" coupon_id="' + availCoupons[i].couponId + '">' + availCoupons[i].promotion.title + '</label>'
											 + '</td>'
											 + '</tr>';
							
						progressCoupon += eachProgressCoupon;
					}
					self.find('[id = useTal_table_use]').append(progressCoupon);
					self.find('[id = useTal_table_use]').trigger('create').trigger('refresh');
			
					
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
	ORDER : { mode : 2, desc : '账单' },
};