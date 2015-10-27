function IssueCouponPopup(param){
	this.IssueMode = {
		FAST : { mode : 1, desc : '快速' },
		ORDER : { mode : 2, desc : '账单' },
	};
	
	param = param || {
		header : '',       //头部信息
		issueMode : null,    //发送类型
		issueTo : '',		//发送对象				
		issueCoupon : '',	//发送的优惠券
		issueComment : ''   //备注
	};
	
	var _issueCouponPopup = null;
	
	
	this.open = function(afterOpen){
		$.post('../OperatePromotion.do',  {dataSource : 'getByCond', status : 'progress'}, function(response, status, xhr){
			if(response.success){
				if(response.root.length > 0){
					_issueCouponPopup = new JqmPopup({
						loadUrl : './popup/coupon/issue.html',
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
									self.find('[id = issueTal_table_issue]').append(progressCoupon);
									self.find('[id = issueTal_table_issue]').trigger('create').trigger('refresh');
							
							
							//绑定确定按钮
							self.find('[id = couponIssueConfirm_a_issue]').click(function(){
								//TODO
							});
								
							
							//绑定取消按钮
							self.find('[id = couponIssueCancel_a_issue]').click(function(){
								_issueCouponPopup.close();
							});
						}
					});
					_issueCouponPopup.open(afterOpen);
				}else{
					Util.msg.tip('没有优惠券可以发放!'); 
				}
				
			}
		});
		
	};
	
	this.close = function(afterOpen, timeout){
		_issueCouponPopup.close(afterOpen, timeout);
	};
}