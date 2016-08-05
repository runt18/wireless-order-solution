$(function(){
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
					
					var coupon = '<div style="left:14px;top:8px;" class="stamp stamp03">'
				  	    +'<div class="par"><p>' + restaurantName + '</p><sub class="sign">￥</sub><span>{price}</span><sub>优惠券</sub><p>{comment}</p></div>' 
				  	     +'<i></i>' 
					    +'<div class="copy" style="z-index: 6000;">有效期<p>{begin}<br>{end}</p><a data-type="useCoupon_a_myCoupon" data-value={couponId}>扫码销券</a></div>' 
	   		            +'</div>'
	   		            + '<div style="height:7px;"></div>' ;
	   		            
	   		        var couponPanel = [];    
	   		            
   		            for(var i = 0; i < coupons.length; i++){
						couponPanel.push(coupon.format({
							price : coupons[i].couponType.price,
							comment : coupons[i].promotion.title,
							begin : coupons[i].couponType.beginExpired != '' ? coupons[i].couponType.beginExpired : '无',
							end : coupons[i].couponType.endExpired != '' ? coupons[i].couponType.endExpired : '无',
							couponId : coupons[i].couponType.id
						}));
					} 
					
					$('body').html(couponPanel.join(''));
					
					$('body').find('[data-type="useCoupon_a_myCoupon"]').each(function(index, element){
						element.onclick = function(){
							var memberMsgDialog;
							memberMsgDialog = new WeDialogPopup({
								titleText : '扫码销券',
								contentCallback : function(container){
									var img = '<img width="100%" height="90%" src="http://qr.topscan.com/api.php?text='+ $(element).attr('data-value') +'">';
									container.find('[id="dialogContent_div_dialogPopup"]').html(img);
								},
								leftText : '确认',
								left : function(){
									memberMsgDialog.close();
								}
							})
							memberMsgDialog.open();
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
});