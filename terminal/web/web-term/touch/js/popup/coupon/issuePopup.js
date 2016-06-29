define(function(require, exports, module){
	
	function IssueCouponPopup(param){
		
		param = param || {
			title : '',       		//头部信息
			issueMode : null,    	//发送类型
			issueTo : '',			//发送对象--memberId				
			issueComment : '',   	//备注
			member: '',
			confirm : function(){},  //确认的回调函数
			isPoint : false,         //是否积分兑换
			orderId : '', 			//账单ID，在issueMode是Order时需要
			issueRule : '',         
			postIssue : function(resultJSON){}//优惠券发放后的回调函数
		};
		
		var _issueCouponPopup = null;
		
		
		this.open = function(afterOpen){
			$.post('../OperatePromotion.do', {
					dataSource : 'getByCond', 
					status : 'progress', 
					issueTriggers : param.issueMode.triggers, 
					orderId : param.orderId,
					memberId : param.issueTo ? param.issueTo : ''
				}, 
				function(response, status, xhr){
					if(response.success){
						if(response.root.length > 0){
							_issueCouponPopup = new JqmPopup({
								loadUrl : './popup/coupon/issue.html',
								pageInit : function(self){
									var progressCoupon = "";
									for(var i = 0; i < response.root.length; i++){
										var eachProgressCoupon = '';
										if(param.isPoint){
											eachProgressCoupon = '<tr class="promotionClass_tr_issue" data-value="'+ response.root[i].issueTrigger.extra +'"> '
																 + '<td style="width:250px">'
																 + '<label style="height:50px"><input data-value="'+ response.root[i].issueTrigger.extra +'" type="checkbox" data-theme="e" class="promotionClass_checkInput_issue" promotion-id="' + response.root[i].id + '">' + response.root[i].coupon.name + '(' + response.root[i].issueTrigger.extra + '积分 )</label>'
																 + '</td>'
																 + '<td style="width:35px"><input data-value="'+ response.root[i].issueTrigger.extra +'" id="amount_input_issue_' + response.root[i].id + '" class="amountClass_input_issue" style="font-size:20px;font-weight: bold;width:35px;" maxlength="3" ></td>'
																 + '</tr>';
										}else{
											eachProgressCoupon = '<tr class="promotionClass_tr_issue" data-value="'+ response.root[i].issueTrigger.extra +'"> '
																 + '<td style="width:250px">'
																 + '<label style="height:50px"><input data-value="'+ response.root[i].issueTrigger.extra +'" type="checkbox" data-theme="e" class="promotionClass_checkInput_issue" promotion-id="' + response.root[i].id + '">' + response.root[i].coupon.name + '</label>'
																 + '</td>'
																 + '<td style="width:35px"><input data-value="'+ response.root[i].issueTrigger.extra +'" id="amount_input_issue_' + response.root[i].id + '" class="amountClass_input_issue" style="font-size:20px;font-weight: bold;width:35px;" maxlength="3" ></td>'
																 + '</tr>';
										}
										
											
										progressCoupon += eachProgressCoupon;
									}
									self.find('[id = issueTal_table_issue]').append(progressCoupon);
									self.find('[id = issueTal_table_issue]').trigger('create').trigger('refresh');
									
									
									//每个优惠活动CheckBox的click事件
									self.find('[id=issueTal_table_issue] .promotionClass_checkInput_issue').each(function(index, element){
										element.onclick = function(){
											var associatedInput = self.find('[id=amount_input_issue_' + $(element).attr('promotion-id') +']');
											if(element.checked){
												associatedInput.removeAttr('disabled');
												associatedInput.val(1);
												associatedInput.select();
											}else{
												associatedInput.attr('disabled', 'disabled');
												associatedInput.val('');
											}
										}
									});
									
									if(param.isPoint){
										self.find('[id="pointExchange_table_issue"]').show();
										self.find('[memberPoint_font_issue]').text('0');
										self.find('[memberPoint_font_issue]').text('0');
									}else{
										self.find('[id="pointExchange_table_issue"]').hide();
									}
									
									//更换标题
									if(param.title){
										if(param.member){
											if(param.isPoint){
												self.find('[id=couponIssueHeader_div_issue]').html('<h3>' + param.title + '--' + param.member.name + '(会员积分:' + param.member.point + ')</h3>');
											}else{
												self.find('[id=couponIssueHeader_div_issue]').html('<h3>' + param.title + '--' + param.member.name + '</h3>');
											}
											
										}else{
											self.find('[id=couponIssueHeader_div_issue]').html('<h3>' + param.title + '</h3>');
										}
									}
									
									//绑定确定按钮
									self.find('[id = couponIssueConfirm_a_issue]').click(function(){
										var promotions = [];
										var point = 0;
										self.find('[id=issueTal_table_issue] .promotionClass_tr_issue').each(function(index, element){
											var eachPromotion = $(element).find('.promotionClass_checkInput_issue')[0];
											if(eachPromotion.checked){
												var eachInput = $(element).find('.amountClass_input_issue');
												if(eachInput.val()){
													promotions.push([$(eachPromotion).attr('promotion-id'), parseInt(eachInput.val())].join(','));
													point += parseInt($(eachPromotion).attr('data-value')) * parseInt(eachInput.val())
												}else{
													promotions.push([$(eachPromotion).attr('promotion-id'), 1].join(','));
													point += parseInt($(eachPromotion).attr('data-value'));
												}
											}
										});
										
										if(param.isPoint){
											param.confirm(self, promotions, point);
										
										}else{
											var requestParam = {};
											if(param.orderId){
												requestParam['orderId'] = param.orderId;
											}
											
											Util.LM.show();
											if(promotions.length > 0){
												requestParam['promotions'] = promotions.join(';');
												requestParam['members'] = param.issueTo;
												requestParam['dataSource'] = 'issue';
												requestParam['issueMode'] = param.issueMode.mode;
												$.post('../OperateCoupon.do', requestParam, function(result){
													if(result.success){
														Util.LM.hide();
														Util.msg.tip('发放成功!');
														_issueCouponPopup.close();
													}else{
														Util.LM.hide();
														Util.msg.tip(result.msg); 
													}
													
													if(param.postIssue && typeof param.postIssue == 'function'){
														param.postIssue(result);
													}
												});
											}else{
												Util.msg.tip('请选择优惠券再发放'); 
											}
										
										}
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
						
					}else{
						Util.msg.tip(response.msg); 
					}
			});
			
		};
		
		this.close = function(afterOpen, timeout){
			_issueCouponPopup.close(afterOpen, timeout);
		};
	}

	var issueTrigger = {
		FREE : {val : 1, desc : "免费发券"},
		SINGLE_EXCEED : {val : 2, desc : "单次消费满"},
		WX_SUBSCRIBE : {val : 3, desc : "微信关注"},
		POINT : {val : 5, desc : '积分关注'}
	}
	
	IssueCouponPopup.IssueMode = {
		FAST : { mode : 1, desc : '快速', triggers : issueTrigger.FREE.val},
		ORDER : { mode : 2, desc : '账单', triggers : issueTrigger.FREE.val + "," + issueTrigger.SINGLE_EXCEED.val},
		POINT : { mode : 6, desc : '积分', triggers : issueTrigger.POINT.val}
	};
	
	exports.IssueMode = IssueCouponPopup.IssueMode;
	
	exports.newInstance = function(param){
		return new IssueCouponPopup(param);
	}
	
	

});