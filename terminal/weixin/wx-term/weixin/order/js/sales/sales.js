
$(function(){
	Util.lbar('', function(html){ $(document.body).append(html);  });
	
	var params = null;
	if(Util.mp.params.pid){
		params = {
			dataSource : 'getByCond',
			pid : Util.mp.params.pid, 
			fid : Util.mp.params.r,
			oid : Util.mp.params.m
		};
		//如果url parameter中包含‘pid’
	}
	
	if(params){
		$.ajax({
			url : '../../WxOperatePromotion.do',
			dataType : 'json',
			type : 'post',
			data : params,
			success : function(data, statuc, xhr){
				if(data.success && data.root.length > 0){
					$('#divInfoContent').html(data.root[0].entire);
				}else{
					$('#divInfoContent').html('暂无活动信息');
				}
			},
			error : function(xhr, errorType, error){
				//微信帐号还未与餐厅会员绑定
				$('#divInfoContent').html('微信帐号还未激活');
			}
		});
	}
	
	//渲染优惠活动的HTML
//	function render(coupon){
//		//优惠活动规则类型
//		var PromotionRule = {
//			DISPLAY_ONLY : { id : 1, desc : '纯显示' },
//			FREE : { id : 2, desc : '免费领取优惠券' }
//		};
//		
//		var promotion = coupon.promotion;
//		var promotionBody = promotion.entire;
//		if(promotion.rule == PromotionRule.FREE.id){
//			//promotionBody += '<hr><div style="margin: 10px 10px 10px 10px; font-size: 14px;font-weight: bold;">'
//			//+ (data.root[0].drawProgress ? (data.root[0].drawProgress.point>=promotion.point?'':customerPoint(promotion.pType,data.root[0].drawProgress.point,promotion.point)) : '' + '</div>')
//			//+ day_closeToPromotion(promotion.promotionBeginDate)
//			promotionBody += '<div style="margin: 10px 10px 10px 10px;">';
//			//优惠券类型的活动显示对应的优惠券
//			var couponTemplate = '<div class="box">' +
//								 '<div class="box_in" ><img src="{couponImg}"><div id="divCouponDrawn" style="display:{couponDrawn}"><img src="images/hasCoupon.png" class="hasCoupon"></div></div>' + 
//								 '<br><span style="margin-top: 15px;">{name}</span><br><span >面额 : {cPrice} 元</span><br><span >到期 : {expiredTime}</span>' +
//								 '</div>';
//			promotionBody += couponTemplate.format({
//									couponImg : couponType.ossImage ? couponType.ossImage.image : 'http://digie-image-real.oss.aliyuncs.com/nophoto.jpg',
//									couponDrawn : coupon.drawProgress.isOk ? 'block' : 'none',
//									name : couponType.name,
//									cPrice : couponType.price,
//									expiredTime : couponType.expiredFormat
//									});
//			promotionBody += '<div id="div_toMyCoupon" style="text-align: right;font-size:15px;display: block;" ><br><a href="javascript:void(0)" onclick="Util.jump(\'member.html\', \'coupon\')" style="color:blue;text-decoration: underline;">→查看我的优惠劵</a></div>';
//			
//			promotionBody += '</div>';
//			
//		}
//		
//		return promotionBody;
//	}
});