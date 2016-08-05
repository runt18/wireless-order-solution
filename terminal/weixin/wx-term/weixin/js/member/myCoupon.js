$(function(){
	var tabs = [];
	
	var tab1 = {
		tab : '未使用',
		onActive : function(container){
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
							
							var coupon = '<div style="left:16px;top:12px;" class="stamp stamp02">'
						  	    +'<div class="par"><p>{name}</p><sub class="sign">￥</sub><span>{price}</span><sub>优惠券</sub><p>{comment}</p></div>' 
							    +'<div class="copy">有效期<p>{begin}<br>{end}</p></div>' 
					            +'<i></i>' 
			   		            +'</div>';
			   		            
			   		        var couponPanel = [];    
			   		            
		   		            for(var i = 0; i < coupons.length; i++){
								couponPanel.push(coupon.format({
									name : coupons[i].couponType.name,
									price : coupons[i].couponType.price,
									comment : coupons[i].promotion.title,
									begin : coupons[i].couponType.beginExpired != '' ? coupons[i].couponType.beginExpired : '无',
									end : coupons[i].couponType.endExpired != '' ? coupons[i].couponType.endExpired : '无'
								}));
							} 
							
							for(var i = 0; i < couponPanel.length; i++){
								$('#xxxxx').append(couponPanel[i])
							}
			   		            
							
						}else{
							coupons = null;
						}
					}else{
						coupons = null
					}
				}
			});	
			
		},
		onDeactive : function(container){
			$('#xxxxx').html('');
		}
	}
	
	var tab2 = {
		tab : '已使用',
		onActive : function(container){
			var coupons;
			wxLoadDialog.instance().show();
			//查找自己拥有未使用的优惠券
			$.ajax({
				url : '../../../WxOperateCoupon.do',
				type : 'post',
				data : {
					dataSource : 'getByCond',
					status : 'used',
					expired : 'true',
					oid : Util.mp.oid,
					fid : Util.mp.fid
				},
				dataType : 'json',
				success : function(data, status, xhr){
					wxLoadDialog.instance().hide();
					if(data.success){
						if(data.root.length > 0){
							coupons = data.root;
							
							var coupon = '<div style="left:16px;top:12px;" class="stamp stamp02">'
						  	    +'<div class="par"><p>{name}</p><sub class="sign">￥</sub><span>{price}</span><sub>优惠券</sub><p>{comment}</p></div>' 
							    +'<div class="copy">有效期<p>{begin}<br>{end}</p></div>' 
					            +'<i></i>' 
			   		            +'</div>';
			   		            
			   		        var couponPanel = [];    
			   		            
		   		            for(var i = 0; i < coupons.length; i++){
								couponPanel.push(coupon.format({
									name : coupons[i].couponType.name,
									price : coupons[i].couponType.price,
									comment : coupons[i].promotion.title,
									begin : coupons[i].couponType.beginExpired != '' ? coupons[i].couponType.beginExpired : '无',
									end : coupons[i].couponType.endExpired != '' ? coupons[i].couponType.endExpired : '无'
								}));
							} 
							
							for(var i = 0; i < couponPanel.length; i++){
								$('#xxxxx').append(couponPanel[i]);
							}
			   		            
							
						}else{
							coupons = null;
						}
					}else{
						coupons = null
					}
				}
			});	
			
		},
		onDeactive : function(container){
			$('#xxxxx').html('');
		}
	}
	
	tabs.push(tab1);
	tabs.push(tab2);
	
	
	var tabPanel = new CreateTabPanel(tabs);
	tabPanel.open();
	
});