define(function(require, exports, module){
	
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
				$.post('../OperateCoupon.do',  {dataSource : 'getAvailableByOrder',filter : '1', orderId : param.orderId, memberId : param.useTo, orderId : param.orderId}, function(response, status, xhr){
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
				$.post('../OperateCoupon.do',  {dataSource : 'getAvailableByManual',filter : '1', status : 'issued', expired : false, memberId : param.useTo}, function(response, status, xhr){
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
						var progressCouponOne = "";
						var progressCouponTwo = "";
						var progressCouponThree = "";
						for(var i = 0; i < availCoupons.length; i++){
							if(availCoupons[i].promotion.isUseTimeMatched){
								var eachProgressCoupon = '<tr align="center">'
													 + '<td style="width:250px">'
													 + '<label style="height:50px"><input $(input_checked) type="checkbox" class="useCouponClass" data-theme="e" coupon_id="' + availCoupons[i].couponId + '">' + availCoupons[i].couponType.name + '<font style="float:right" color="red">$(word)</font>' +'</label>'
													 + '</td>'
													 + '</tr>';
								
								if(availCoupons[i].statusText == '已使用'){
									eachProgressCoupon = eachProgressCoupon.replace('$(word)', '已使用').replace('$(input_checked)', 'checked');
								}else{
									eachProgressCoupon = eachProgressCoupon.replace('$(word)', ' ');
								}
								
								
								
								if(i % 3 == 0){
									progressCouponOne += eachProgressCoupon;
								}else if(i % 3 == 1){
									progressCouponTwo += eachProgressCoupon;
								}else{
									progressCouponThree += eachProgressCoupon;
								}
							}
						}
						
						
						self.find('[id = useTal1_table_use]').append(progressCouponOne);
						self.find('[id = useTal1_table_use]').trigger('create').trigger('refresh');
						
						self.find('[id = useTal2_table_use]').append(progressCouponTwo);
						self.find('[id = useTal2_table_use]').trigger('create').trigger('refresh');
						
						self.find('[id = useTal3_table_use]').append(progressCouponThree);
						self.find('[id = useTal3_table_use]').trigger('create').trigger('refresh');
						
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
								self.find('[id=use_div_use] .useCouponClass').each(function(index, element){
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
		FAST : { mode : 20, desc : '快速' },
		ORDER : { mode : 21, desc : '账单' }
	};
	
	exports.UseMode = UseCouponPopup.UseMode;
	
	exports.newInstance = function(param){
		return new UseCouponPopup(param);
	}
});