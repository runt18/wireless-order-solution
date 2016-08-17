$(function(){
	
	function refresh(){
		init();
	}
	
	function init(){
		var restaurantName = null; 
		var member = null;
		$.ajax({
			url : '../../../WxOperateRestaurant.do',
			type : 'post',
			data : {
				dataSource : 'detail',
				fid : Util.mp.fid,
				branchId : Util.mp.params.branchId
			},
			dataType : 'json',
			success : function(data){
				restaurantName = data.root[0].name;
			}
		});
		
		var coupons;
		wxLoadDialog.instance().show();
		//查找可以积分兑换的优惠券
		$.ajax({
			url : '../../../WxOperateCoupon.do',
			type : 'post',
			data : {
				dataSource : 'getPointChangeCoupon',
				status : 'progress',
				issueTriggers : '5',
				oid : Util.mp.oid,
				fid : Util.mp.fid
			},
			dataType : 'json',
			success : function(data, status, xhr){
				wxLoadDialog.instance().hide();
				if(data.success){
					if(data.root.length > 0){
						coupons = data.root;
						
						var memberPoint = null;
						$.ajax({
							url : '../../../WXOperateMember.do',
							type : 'post',
							data : {
								dataSource : 'getInfo',
								oid : Util.mp.oid,
								fid : Util.mp.fid
							},
							dataType : 'json',
							success : function(data, status, xhr){
								if(data.success){
									memberPoint = data.other.member.point;
									
									var coupon = '<div style="left:14px;top:8px;" class="stamp stamp03">'
								  	    +'<div class="par"><p>' + restaurantName + '</p><sub class="sign">￥</sub><span>{price}</span><sub>优惠券</sub><p>{comment}</p></div>' 
								  	     +'<i></i>' 
									    +'<div class="copy" style="z-index: 6000;"><p>{date}</p><a promotionId={promotionId} data-type="pointConsume_div_pointConsume" data-value={point}>{buttonName}</a></div>' 
					   		            +'</div>'
					   		            + '<div style="height:7px;"></div>' ;
					   		            
					   		        var couponPanel = [];    
					   		        var head = '<div style="height:30px;width:100%;line-height:30px;background-color:yellow;font-size:15px;" align="right" >我的积分:<font id="myPoint_div_pointConsume"></font>分</div>';
					   		        couponPanel.push(head);
					   		        
					   		        if(coupons.length > 0){
						   		        for(var i = 0; i < coupons.length; i++){
					   		            	var date = '';
							   		        if(coupons[i].coupon.expiredType == '1'){
							   		        	//开始结束时间
							   		        	if(coupons[i].coupon.beginExpired != '' && coupons[i].coupon.endExpired != ''){
							   		        		date = coupons[i].coupon.beginExpired + '开始<br>' + coupons[i].coupon.endExpired + '结束';
							   		        	}else if(coupons[i].coupon.beginExpired != '' && coupons[i].coupon.endExpired == ''){
							   		        		date = coupons[i].coupon.beginExpired + '开始<br>无结束时间';
							   		        	}else if(coupons[i].coupon.beginExpired == '' && coupons[i].coupon.endExpired != ''){
							   		        		date = coupons[i].coupon.endExpired + '结束';
							   		        	}else if(coupons[i].coupon.beginExpired == '' && coupons[i].coupon.endExpired == ''){
							   		        		date = '无使用期限'; 
							   		        	}
							   		        	
							   		        }else if(coupons[i].coupon.expiredType == '2'){
							   		        	//有效期
							   		        	date = '领取起' + coupons[i].coupon.expiredDuration + '日有效'
							   		        }
							   		        
							   		        var buttonName = null;
							   		        if(coupons[i].issueTrigger.extra > memberPoint){
							   		        	buttonName = '积分不足';
							   		        }else{
							   		        	buttonName = "兑换";
							   		        }
					   		            	
											couponPanel.push(coupon.format({
												price : coupons[i].coupon.price,
												comment : coupons[i].coupon.name + "(" + coupons[i].issueTrigger.extra + "积分)",
												date : date,
												point : coupons[i].issueTrigger.extra,
												promotionId : coupons[i].id,
												buttonName : buttonName
											}));
										} 
										
										$('body').html(couponPanel.join(''));
					   		        }else{
					   		      		$('body').html("暂无优惠券可以兑换");
					   		        }
				   		            
									$.ajax({
										url : '../../../WXOperateMember.do',
										type : 'post',
										data : {
											dataSource : 'getInfo',
											oid : Util.mp.oid,
											fid : Util.mp.fid
										},
										dataType : 'json',
										success : function(data, status, xhr){
											if(data.success){
												$('body').find('[id="myPoint_div_pointConsume"]').html(data.other.member.point);
											}
										}
									});
									
									$('body').find('[data-type="pointConsume_div_pointConsume"]').each(function(index, element){
										element.onclick = function(){
											var memberMsgDialog;
											memberMsgDialog = new WeDialogPopup({
												titleText : '积分兑换',
												contentCallback : function(container){
													container.find('[id="dialogContent_div_dialogPopup"]').html('你是否使用' + $(element).attr('data-value') + '兑换本张优惠券');
												},
												leftText : '确认',
												left : function(){
													wxLoadDialog.instance().show();
													$.ajax({
														url : '../../../WxOperateCoupon.do',
														type : 'post',
														dataType : 'json',
														data : {
															dataSource : 'pointConsume',
															promotionId : $(element).attr('promotionId'),
															oid : Util.mp.oid,
															fid : Util.mp.fid
														},
														success : function(data){
															wxLoadDialog.instance().hide();
															var msgDialog;
															msgDialog = new WeDialogPopup({
																titleText : '提示',
																contentCallback	 : function(containers){
																	containers.find('[id="dialogContent_div_dialogPopup"]').html(data.msg);
																},
																leftText : '确定',
																left : function(){
																	msgDialog.close();
																	refresh();
																}
															})
															
															memberMsgDialog.close(function(){
																msgDialog.open();
															}); 
															
														},
														error : function(){
					 										memberMsgDialog.close(function(){
																var errorDialog;
																errorDialog = new WeDialogPopup({
																	titleText : '失败',
																	containerCallback : function(container){
																		container.find('[id="dialogContent_div_dialogPopup"]').html('服务器请求失败,请稍后再试');
																	}
																})
																errorDialog.open();
															})
														}
													})
													
												},
												rightText : '取消',
												right : function(){
													memberMsgDialog.close();
												}
											})
											memberMsgDialog.open();
										}
									})
									
								}
							}
						});
						
					}else{
						$('body').html('您的积分不足,不能兑换优惠券');
					}
				}else{
					coupons = null
				}
			}
		});	
	}
	
	init();
});