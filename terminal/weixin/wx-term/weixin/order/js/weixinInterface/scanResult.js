var Request = new Util_urlParaQuery();
var couponId;

$(function(){
	$.post('../../WXOperateMember.do', {
		dataSource:'afterInpour',
		oid : Util.mp.oid,
		fid : Util.mp.fid,
		orderId : Request["orderId"]
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
	    			
	    			couponId = data.root[0].couponId;
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
	
});

function entryPromotion(){
	Util.jump('sales.html', couponId);
}
