
$(function(){
	$.post('../../WXOperateMember.do', {
		dataSource:'afterInpour',
		oid : Util.mp.oid,
		fid : Util.mp.fid,
		orderId : Util.mp.params.orderId
	}, function(result){
		if(result.success){
			
			$('#div4OrderInfo').show();

			$('#spanOrderId').text(result.other.order.id);
			$('#spanMemberName').text(result.other.member.name);
			$('#spanBillShouldPay').text(result.other.order.pureTotalPrice);
			$('#spanBillAfterDiscount').text(result.other.order.actualPrice);
			$('#memberPoint').text(result.other.member.point);
			
			$('#restName').text(result.other.restName);
			
	    	$.post('../../WxOperatePromotion.do', {dataSource : 'promotions', fid : Util.mp.fid, oid : Util.mp.oid}, function(data){
	    		if(data.success){
	    			var promotion = data.root[0].promotion;
	    			
	    			$('#div4Active').show();
	    			$('#promotionTitle').html(promotion.title);
	    			$('#promotionImage').attr("src", promotion.image);
	    			
	    			couponId = promotion.couponId;
	    		}else{
	    			$('#div4Welcome').show();
	    		}
	    		
	    	});
		}else{
			alert(result.msg);
		}   	    	
	}, "json").error(function(result){
		alert("注入会员出错, 请稍后再试");
	});
	
	var payParam = null;
	
	function onBridgeReady() {
		if(payParam){
			WeixinJSBridge.invoke('getBrandWCPayRequest', {
				// 以下参数的值由BCPayByChannel方法返回来的数据填入即可
				"appId" : payParam.appId,
				"timeStamp" : payParam.timeStamp,
				"nonceStr" : payParam.nonceStr,
				"package" : payParam.package,
				"signType" : payParam.signType,
				"paySign" : payParam.paySign
				}, function(res) {
					if (res.err_msg == "get_brand_wcpay_request:ok") {
						// 使用以上方式判断前端返回,微信团队郑重提示：res.err_msg将在用户支付成功后返回ok，但并不保证它绝对可靠。
					} 
				});
		}
	}
	
	function callpay() {
		$.post('../../WxBeeCloud.do', {
			dataSource : 'start',
			oid : Util.mp.oid,
			fid : Util.mp.fid,
			orderId : Util.mp.params.orderId
		}, function(result){
			if(result.success){
				payParam = result.root[0];
				if (typeof WeixinJSBridge == "undefined") {
					if (document.addEventListener) {
						document.addEventListener('WeixinJSBridgeReady', onBridgeReady,	false);
					} else if (document.attachEvent) {
						document.attachEvent('WeixinJSBridgeReady', onBridgeReady);
						document.attachEvent('onWeixinJSBridgeReady', onBridgeReady);
					}
				} else {
					onBridgeReady();
				}
			}else{
				payParam = null;
				alert(result.msg);
			}   	    	
		}, "json").error(function(result){
			alert("微信支付失败");
		});

	}
	//微信支付
	//$('#iknowButton').click(callpay);
	$('#iknowButton').click(function(){
		Util.jump('member.html');
	});
	
	var couponId = 0;
	//查看优惠活动
	$('#checkPromtionButton').on('click', function(){
		Util.jump('sales.html?cid=' + couponId);
	});
});

