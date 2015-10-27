function UseCoupon(param){
	this.UseMode = {
		FAST : {mode : 1, desc : '快速'},
		ORDER : {mode : 2, desc : '账单'}
	};
	
	param = param || {
		header : '',	//头部信息
		useMode : null,	//使用类型
		useTo : '',		//对象的使用
		useCoupon : '',	//使用的优惠券
		useComment : ''	//备注
	};
	
	var _useCouponPopup = null;
	
	this.open = function(afterOpen){
		$.post('../OperatePromotion.do',  {dataSource : 'getByCond', status : 'progress'}, function(response, status, xhr){
			if(response.success){
				if(response.root.length > 0){
					_useCouponPopup = new JqmPopup({
						loadUrl : './popup/coupon/use.html',
						pageInit : function(self){
							
									var progressCoupon = "";
									for(var i = 0; i < response.root.length; i++){
										var eachProgressCoupon = '<tr>'
															 + '<td style="width:250px">'
															 + '<label style="height:50px"><input type="checkbox" data-theme="e" id="' + response.root[i].id + '">' + response.root[i].title + '</label>'
															 + '</td>'
															 + '<td style="width:35px"><input style="font-size:20px;font-weight: bold;width:35px;" maxlength="3" disabled="disabled"></td>'
															 + '</tr>';
											
										progressCoupon += eachProgressCoupon;
									}
									self.find('[id = useTal_table_use]').append(progressCoupon);
									self.find('[id = useTal_table_use]').trigger('create').trigger('refresh');
							
							
							//绑定确定按钮
							self.find('[id = couponUseConfirm_a_use]').click(function(){
								//TODO
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
		});
	};
	
	this.close = function(afterOpen, timeout){
		_useCouponPopup.close(afterOpen, timeout);
	}
}
		