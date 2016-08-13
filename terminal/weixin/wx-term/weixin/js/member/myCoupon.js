 $(function(){
	var refreshTimeoutId = null;	
	
	function init(){
		var restaurantName = null; 
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
		//查找自己拥有未使用的优惠券
		$.ajax({
			url : '../../../WxOperateCoupon.do',
			type : 'post',
			data : {
				dataSource : 'getByCond',
				status : 'issued',
				filter : '1',
				oid : Util.mp.oid,
				fid : Util.mp.fid
			},
			dataType : 'json',
			success : function(data, status, xhr){
				wxLoadDialog.instance().hide();
				if(data.success){
					if(data.root.length > 0){
						coupons = data.root;
						
						var coupon = '<div style="left:14px;top:8px;" class="stamp stamp03" >'
					  	    +'<div data-type="promotion" data-value={promotionid} class="par"><p>' + restaurantName + '</p><sub class="sign">￥</sub><span>{price}</span><sub>优惠券</sub><p>{comment}</p></div>' 
						    +'<div class="copy" style="z-index: 6000;"><p>{date}</p><a data-type="useCoupon_a_myCoupon" birthDay={birthday}  couponPrice={price} couponName={comment} memebrId={memberId} data-value={couponId}>扫码销券</a></div>' 
		   		            +'</div>'
		   		            + '<div style="height:7px;"></div>' ;
		   		            
		   		        var couponPanel = [];    
		   		            
	   		            for(var i = 0; i < coupons.length; i++){
							
	   		            	var date = '';
			   		        if(coupons[i].couponType.expiredType == '1'){
			   		        	//开始结束时间
			   		        	if(coupons[i].couponType.beginExpired != '' && coupons[i].couponType.endExpired != ''){
			   		        		date = cuopons[i].couponType.beginExpired + '开始<br>' + coupons[i].couponType.endExpired + '结束';
			   		        	}else if(coupons[i].couponType.beginExpired != '' && coupons[i].couponType.endExpired == ''){
			   		        		date = coupons[i].couponType.beginExpired + '开始<br>无结束时间';
			   		        	}else if(coupons[i].couponType.beginExpired == '' && coupons[i].couponType.endExpired != ''){
			   		        		date = coupons[i].couponType.endExpired + '结束';
			   		        	}else if(coupons[i].couponType.beginExpired == '' && coupons[i].couponType.endExpired == ''){
			   		        		date = '无使用期限';
			   		        	}
			   		        	
			   		        }else if(coupons[i].couponType.expiredType == '2'){
			   		        	//有效期
			   		        	date = coupons[i].birthDate + '领取<br>领取起' + coupons[i].couponType.expiredDuration + '日有效'
			   		        }
	   		            	
							
							couponPanel.push(coupon.format({
								price : coupons[i].couponType.price,
								comment : coupons[i].promotion.title,
								date :　date,
								couponId : coupons[i].couponId,
								memberId : coupons[i].member.id,
								birthday :　coupons[i].birthDate,
								promotionId : coupons[i].promotion.id
							}));
						} 
						
						$('body').html(couponPanel.join(''));
						
						
						$('body').find('[data-type="promotion"]').each(function(index, element){
							element.onclick = function(){
								Util.jump('../../order/sales.html?pid=' + $(element).attr('data-value'));
							}
						})
						
						$('body').find('[data-type="useCoupon_a_myCoupon"]').each(function(index, element){
							element.onclick = function(){
								var memberMsgDialog;
								memberMsgDialog = new WeDialogPopup({
									titleText : '扫码销券',
									contentCallback : function(container){
										
										var table = '<table style="color:black">'
											+'<tr align="left">' 
												+'<td>券名 : '+  $(element).attr('couponName') + '</td>'
											+'</tr>'
											+'<tr align="left">' 
												+'<td  colspan="2">面额 : '+ $(element).attr('couponPrice') + '</td>'
											+'</tr>'
											+'<tr align="left">' 
												+'<td  colspan="2">领取时间 : '+ $(element).attr('birthday') + '</td>'
											+'</tr>'
											+'<tr align="left">' 
												+'<td  colspan="2">优惠券号 : '+ $(element).attr('data-value') + '</td>'
											+'</tr>'
											+'<hr/>'
											+'<tr height="5px"></tr>'
											+'<tr>' 
												+'<td colspan="2"><img width="100%" height="90%" src="http://qr.topscan.com/api.php?text='+ $(element).attr('data-value') +'"></td>'
											+'</tr>'
										+'</table>';
										
										
										container.find('[id="dialogContent_div_dialogPopup"]').html(table);
									},
									leftText : '确认',
									left : function(){
										memberMsgDialog.close();
									},
									afterClose : function(){
										if(refreshTimeoutId){
											clearTimeout(refreshTimeoutId);
										}
									}
								})
								memberMsgDialog.open(function(){
									(function refreshCoupon(){
										$.ajax({
											url : '../../../WxOperateCoupon.do',
											type : 'post',
											dataType : 'json',
											data : {
												dataSource : 'getById',
												oid : Util.mp.oid,
												fid : Util.mp.fid,
												cid : $(element).attr('data-value')
											},
											success : function(data){
												if(data.success){
													if(data.root[0].statusValue == '4'){
														//已使用
														var msgDialog;
														msgDialog = new WeDialogPopup({
															titleText : '提示',
															contentCallback : function(container){
																container.find('[id="dialogContent_div_dialogPopup"]').html('使用【' + data.root[0].couponType.name + '】优惠券成功');
															},
															leftText : '确认',
															left : function(){
																msgDialog.close();
															},
															afterClose : function(){
																refresh();
															}
														});
														memberMsgDialog.close(function(){
															msgDialog.open();
														}, 100);																	
														
													}else{
														refreshTimeoutId = setTimeout(refreshCoupon, 1000);
													}
												}else{
													refreshTimeoutId = setTimeout(refreshCoupon, 1000);
												}
											},
											error : function(){
												refreshTimeoutId = setTimeout(refreshCoupon, 1000);
											}
										})
										
									})();
								
								});
							}
						})
		   		            
					}else{
						coupons = null;
					}
				}else{
					coupons = null
				}
			}
		});	
	}
	
	init();
	
	function refresh(){
		init();
		$('html,body').animate({scrollTop: '0px'}, 1000);
	}

	
});