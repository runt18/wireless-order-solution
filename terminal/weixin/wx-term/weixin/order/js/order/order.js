

function buildInvitation(bookId){
	Util.jump("invitation.html", bookId);
}


$(function(){
	
	var Template = {
		
		inside: '<div class="box-order {orderBorder}">' +
					'<div ><span style="font-weight: bold;font-size:20px;color:green;">{restaurantName}</div>' +
					'<div class="{orderClass}"><div style="height: 20px;">' +
							'<div style="float: left;font-size:16px;">微订编号:{code}</div>' +
							'<div style="float: right;color:#26a9d0;">{status}</div>' +
					'</div></div>' +
					'<div>账单号: <span>{orderId}</span></div>' +
					'<div>点单日期: <span style="font-weight: bold">{orderDate}</span></div>' +
					'<div>' +
						'<div style="float: left">金额: <span style="font-weight: bold;color: red;">{totalPrice}</span> 元</div>' +
						'<div style="float: right">菜品种类: <span style="font-weight: bold;color: red;">{count}</span> 项</div>' +
					'</div>' +
					'<div {display}>菜名: <span style="font-weight: bold;">{foods}</span></div>' +
				'</div>',
		book : '<div class="box-order orderBorder_commit">'	+
					'<div ><span style="font-weight: bold;font-size:20px;color:green;">{restaurantName}</div>' +
					'<div class="box-order-commit"><div style="height: 20px;">' +
						'<div style="float: left;font-size:16px;">预订单号:{code}</div>' +
						'<div style="float: right;color:#26a9d0;">{status}</div>' +
					'</div></div>' +
					'<div>预定日期: <span style="font-weight: bold">{orderDate}</div>' +
					'<div>预定区域: <span style="font-weight: bold">{orderRegion}</span></div>' +
					'<div>' +
					'<div style="float: left">预定菜品: <span style="font-weight: bold;color: red;">{count}</span> 项</div>' +
					'<div style="float: right">菜品价钱: <span style="font-weight: bold;color: red;">{totalPrice}</span> 元</div>' +
					'</div>' +
					'<div {display}>菜名: <span style="font-weight: bold;">{foods}</span></div>' +
					'<div style="margin-top: 5px;height: 33px;width: 99%;text-align: center;">' +
						'<button data-type="invitation_button_order" data-value={bookId} style="font-size:20px;width:40%;" class="orange">发送邀请函</button>&nbsp;&nbsp;' +
						'<button data-type="wxPay_button_order" data-value={bookId} style="font-size:20px;width:40%;{ishide}" class="orange">微信支付</button>' +
					'</div>	'+	
				'</div>'
		
	};
	
	Util.lbar('', function(html){ $(document.body).append(html);  });
	//获取订单列表
	$.ajax({
		url : '../../WxOperateOrder.do',
		dataType : 'json',
		data : {
			dataSource : 'getByCond',
			oid : Util.mp.oid,
			fid : Util.mp.fid,
			sessionId : Util.mp.params.sessionId,
			includeBranch : true
		},
		success : function(data, status, xhr){
			var html = [];
			//for(var i = 0; i < data.root.length; i++){
			data.root.forEach(function(temp, i){
				var count = temp.foods.length, foods = '';
				for (var j = 0; j < temp.foods.length; j++) {
					if(foods){
						foods += '，';
					}
					foods += temp.foods[j].foodName;
				}
				html.push(Template.inside.format({
					restaurantName : temp.restaurantName,
					code : temp.code,
					status : temp.statusDesc,
					orderId : temp.orderId ? temp.orderId : '----',
					orderDate : temp.date,
					totalPrice : temp.price,
					count : count,
					foods : foods,
					display : temp.statusVal == 1 ? 'hidden="hidden"' : '',
					orderClass : temp.statusVal == 2 ? 'box-order-commit' : '',
					orderBorder : temp.statusVal == 2 ? 'orderBorder_commit' : 'orderBorder_invalid'
				}));						
			});

			$('#divWeixinOrderList').html(html.join(''));
			
		},
		error : function(xhr, errorType, error){}
	});
	
	//获取预定列表
	$.ajax({
		url : '../../WxOperateBook.do',
		dataType : 'json',
		data : {
			dataSource : 'getByCond',
			oid : Util.mp.oid,
			fid : Util.mp.fid,
			sessionId : Util.mp.params.sessionId
		},
		success : function(data, status, xhr){
			var html = [];
			for(var i = 0; i < data.root.length; i++){
				var bookInfo = data.root[i];
				var count = 0, foods = '';
				
				if(bookInfo.order){
					count = bookInfo.order.orderFoods.length;
					for (var j = 0; j < bookInfo.order.orderFoods.length; j++) {
						if(foods){
							foods += '，';
						}
						foods += bookInfo.order.orderFoods[j].foodName;
					}				
				}

				var totalPrice;
				if(bookInfo.hasWxPay){
					totalPrice = '已微信支付' + bookInfo.wxPayMoney;
				}else if(bookInfo.price){
					totalPrice = bookInfo.price;
				}else{
					totalPrice = 0;
				}
				
				html.push(Template.book.format({
					restaurantName : bookInfo.restaurantName,
					code : bookInfo.id,
					status : bookInfo.statusDesc,
					orderDate : bookInfo.bookDate,
					orderRegion : bookInfo.region,
					totalPrice : totalPrice,
					count : count,
					display : !foods ? 'hidden="hidden"' : '',
					foods : foods,
					bookId : bookInfo.id,
					ishide : bookInfo.hasWxPay ? '' : 'display:none'
				}));						

			}
			$('#divWeixinBookList').html(html.join(''));
			
			//发送邀请函的点击事件
			$('#divWeixinBookList').find('[data-type="invitation_button_order"]').each(function(index, element){
				element.onclick = function(){
					buildInvitation($(element).attr('data-value'));
				}
			});
			
			//微信支付的点击事件
			$('#divWeixinBookList').find('[data-type="wxPay_button_order"]').each(function(index, element){
				element.onclick = function(){
					$.post('../../WxOperateBook.do', {
						bookId : bookId,
						fid : Util.mp.fid,
						oid : Util.mp.oid,
						dataSource : 'wxPay',
						branchId : typeof Util.mp.extra != 'undefined' ? Util.mp.extra : ''
					}, function(result){
						if(result.success){
							payParam = result.other;
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
							var dialog = new DialogPopup({
								content : '支付成功',
								titleText : '温馨提示',
								left : function(){
									dialog.close(function(){
										Util.jump('orderList.html?book=2', typeof Util.mp.extra != 'undefined' ? Util.mp.extra : '');
									}, 200);
								}
							})
							dialog.open();
						}else{
							payParam = null;
							var dialog = new DialogPopup({
								content : result.msg,
								titleText : '微信支付失败',
								left : function(){
									dialog.close();
								}
							})
							dialog.open();
						} 
					}, 'json');
				}
			});
			
		},
		error : function(xhr, errorType, error){}
	});
	
	//微信支付的参数
	var payParam = null;
	//微信支付回调函数
	function onBridgeReady(){
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
//					alert(res.err_msg);
//					alert(JSON.stringify(res));
					if (res.err_msg == "get_brand_wcpay_request:ok") {
						// 使用以上方式判断前端返回,微信团队郑重提示：res.err_msg将在用户支付成功后返回ok，但并不保证它绝对可靠。
					} 
				}
			);
		}
	}
	
	//如果是预订确定，则进入预订列表
	if(Util.mp.params.book){
		changWeixinOrderList('book');
	}
	
	//点单列表
	$('#tab4OrderList').click(function(){
		changWeixinOrderList('order');
	});
	
	//预订列表
	$('#tab4BookList').click(function(){
		changWeixinOrderList('book');
	});
	
	function changWeixinOrderList(otype){
		if(otype == 'order'){
			//设置显示订单列表内容
			$('#divWeixinOrderList').show();
			$('#divWeixinBookList').hide();
			//设置Tab的样式
			$('#divOrderList').addClass('tabPanelListCheck');
			$('#divBookList').removeClass('tabPanelListCheck');
		}else{
			//设置显示预订列表内容
			$('#divWeixinBookList').show();
			$('#divWeixinOrderList').hide();
			//设置Tab的样式
			$('#divOrderList').removeClass('tabPanelListCheck');
			$('#divBookList').addClass('tabPanelListCheck');
		}
	}
});