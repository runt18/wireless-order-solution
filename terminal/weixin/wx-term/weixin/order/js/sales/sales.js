/**
 * 
 */

//分析url
function parseURL(url) {
    var a = document.createElement('a');
    a.href = url;
    return {
        source: url,
        protocol: a.protocol.replace(':', ''),
        host: a.hostname,
        port: a.port,
        query: a.search,
        params: (function () {
            var ret = {},
            seg = a.search.replace(/^\?/, '').split('&'),
            len = seg.length, i = 0, s;
            for (; i < len; i++) {
                if (!seg[i]) { 
                	continue; 
                }
                s = seg[i].split('=');
                ret[s[0]] = s[1];
            }
            return ret;
 
        })(),
        file: (a.pathname.match(/\/([^\/?#]+)$/i) || [, ''])[1],
        hash: a.hash.replace('#', ''),
        path: a.pathname.replace(/^([^\/])/, '/$1'),
        relative: (a.href.match(/tps?:\/\/[^\/]+(.+)/) || [, ''])[1],
        segments: a.pathname.replace(/^\\/, '').split('/')
    };
}

$(function(){
	Util.lbar('', function(html){ $(document.body).append(html);  });
	
	var requestUrl = parseURL(location.href);
	if(requestUrl.params.pid){
		//如果url parameter中包含‘pid’
		$.ajax({
			url : '../../WxOperatePromotion.do',
			dataType : 'json',
			type : 'post',
			data : { dataSource : 'getByCond', pid : requestUrl.params.pid, fid : requestUrl.params.r, oid : requestUrl.params.m },
			success : function(data, statuc, xhr){
				if(data.success && data.root.length > 0){
					Util.getDom('divInfoContent').innerHTML = data.root[0].entire;
				}else{
					Util.getDom('divInfoContent').innerHTML = '暂无活动信息';
				}
			},
			error : function(xhr, errorType, error){
				//微信帐号还未与餐厅会员绑定
				Util.getDom('divInfoContent').innerHTML = '微信帐号还未激活';
			}
		});
		
	}else if(requestUrl.params.cid || requestUrl.params.e == 'default'){
		var params ;
		if(requestUrl.params.e == 'default'){
			params = {
					dataSource : 'defaultCoupon',
					cid : requestUrl.params.cid, 
					fid : requestUrl.params.r, 
					oid : requestUrl.params.m
				};
		}else{
			params = {
				dataSource : 'getById',
				cid : requestUrl.params.cid, 
				fid : requestUrl.params.r, 
				oid : requestUrl.params.m
			};
		}
		
		//优惠活动规则类型
		var PromotionRule = {
			DISPLAY_ONLY : { id : 1, desc : '纯显示' },
			FREE : { id : 2, desc : '免费领取优惠券' }
		};
		
		$.ajax({
			url : '../../WxOperateCoupon.do',
			dataType : 'json',
			type : 'post',
			data : params,
			success : function(data, statuc, xhr){
				if(data.success && data.root.length > 0){
					
					var promotion = data.root[0].promotion;
					var couponType = data.root[0].couponType;
					var promotionBody = promotion.entire;
					if(promotion.rule == PromotionRule.FREE.id){
						//优惠券类型的活动显示对应的优惠券
						var couponTemplate = '<div class="box">' +
											 '<div class="box_in" ><img src="{couponImg}"><div id="divHasCoupon" style="display:none"><img src="images/hasCoupon.png" class="hasCoupon"></div></div>' + 
											 '<br><span style="margin-top: 15px;">{name}</span><br><span >面额 : {cPrice} 元</span><br><span >到期 : {expiredTime}</span>' +
											 '</div>';
						promotionBody += couponTemplate.format({
												couponImg : couponType.ossImage ? couponType.ossImage.image : 'http://digie-image-real.oss.aliyuncs.com/nophoto.jpg',
												name : couponType.name,
												cPrice : couponType.price,
												expiredTime : data.root[0].couponType.expiredFormat
						});
					}
					
					Util.getDom('divInfoContent').innerHTML = promotionBody;
					
	/* 				Util.getDom('divInfoContent').innerHTML = promotion.entire
						+ '<hr><div style="margin: 10px 10px 10px 10px; font-size: 14px;font-weight: bold;">'
						//+ (data.root[0].drawProgress ? (data.root[0].drawProgress.point>=promotion.point?'':customerPoint(promotion.pType,data.root[0].drawProgress.point,promotion.point)) : '' + '</div>')
						//+ day_closeToPromotion(promotion.promotionBeginDate)
						+ '<div style="margin: 10px 10px 10px 10px;">'
						+ (promotion.pType != 1 ? couponTemplate.format({
							couponImg : data.root[0].couponType.ossImage?data.root[0].couponType.ossImage.image:'http://digie-image-real.oss.aliyuncs.com/nophoto.jpg',
							name : data.root[0].couponType.name,
							cPrice : data.root[0].couponType.price,
							expiredTime : data.root[0].couponType.expiredFormat
						}) : '')
						+ '<div id="div_getCouponBtn" style="margin-top: 10px;text-align: center;display: none" ><a class="a_demo_two" style="font-size: 20px;font-weight: bold;" onclick="getCouponByPid('+data.root[0].couponId+')">领取优惠劵</a></div>'
						+ '<div id="div_toMyCoupon" style="text-align: right;font-size:15px;display: none;" ><br><a href="javascript:void(0)" onclick="Util.skip(\'member.html\', \'coupon\')" style="color:blue;text-decoration: underline;">→查看我的优惠劵</a></div>'
						+ '</div>';
						
					$('img').parent().css('width', '100%');	
					
					
					if(data.root[0].statusValue < 3){
						if(data.root[0].drawProgress && data.root[0].drawProgress.isOk){
							$('#div_getCouponBtn').show();
						}else{
							$('#div_getCouponBtn').hide();							
						}
						$('#divHasCoupon').hide();
						$('#div_toMyCoupon').hide();
					}else{
						$('#div_getCouponBtn').hide();		
						$('#div_toMyCoupon').show();
						$('#divHasCoupon').show();
									
					} */
				}else{
					Util.getDom('divInfoContent').innerHTML = '暂无促销信息';
				}
			},
			error : function(xhr, errorType, error){
				//微信帐号还未与餐厅会员绑定
				Util.getDom('divInfoContent').innerHTML = '微信帐号还未激活';
			}
		});
	}
	
});