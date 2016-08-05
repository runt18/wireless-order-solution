$(function(){
	var template = {
		orderHead : ' <div class="myolist"> ' +
					' <div class="myorderid clearfix plr15 borderbottom" data-type="controlExpand_orderList"> ' +
						' <h4 class="pull-left font09em"> ' +
							' <span style="line-height:2em;">订单号: {orderId}</span> ' +
						' </h4> ' +
						' <h4 class="pull-right font09em">{orderDate} &nbsp;&nbsp;<i style="background:url(../../images/expand.png);display:inline-block;width:16px;height:16px;"></i></h4> ' +
					' </div> ' +
					' <div class="myddetailbox"> ',
					
		orderBody : ' <div data-type="expandOrderFoods_orderList" class="myodetail clearfix"> ' +
							' <div class="col-xs-2 clearPadding proimg"> ' +
								' <img src="{imgUrl}" alt="" style="width:50px;height:50px;"> ' +
							' </div> ' +
							' <div class="col-xs-8 clearPadding proname plr10 ">{foodName} ' +
							' </div> ' +
							' <div class="col-xs-2 clearPadding text-right"> ' +
								' <span class="colorred2">￥{foodPrice}</span><br> ' +
								' <span class="font09em text-muted">× {foodAmount}</span> ' +
							' </div> ' +
						' </div> ',	
						
		orderFoot :			' <div class="ptb10 plr15 text-muted font09em clearfix"> ' +
							' <div class="pull-left font09em"> ' +
								' <span class="badge bg-dining" style="font-weight:normal;">{status}</span> ' +
							' </div> ' +
							' <div class="pull-right"> ' +
								' <span>共{totalAmount}件商品</span>&nbsp;&nbsp;合计：<span class="colorred2">{price}</span>' +
								//'&nbsp;&nbsp;实收：<span class="colorred2">￥{actualPrice}</span> ' +
							' </div> ' +
						' </div> ' +
					' </div> ' +
				' </div> '
	};
	
	new CreateTabPanel([{
		tab : '订单列表',
		initTab : 0,
		onActive : function(container){
			var loadingDialog;
			loadingDialog = wxLoadDialog.instance();
			loadingDialog.show();
			$.ajax({
				url : '../../../WxOperateOrder.do',
				dataType : 'json',
				data : {
					dataSource : 'getByCond',
					oid : Util.mp.oid,
					fid : Util.mp.fid,
					sessionId : Util.mp.params.sessionId,
					includeBranch : true
				},
				success : function(data, status, xhr){
					$('#' + container.attr('id')).html('');
					var html = [];
					data.root.forEach(function(temp, i){
						var count = temp.foods.length, foods = '';
						var body = [];
						for (var j = 0; j < temp.foods.length; j++) {
							if(foods){
								foods += '，';
							}
							foods += temp.foods[j].foodName;
							
							body.push(template.orderBody.format({
								foodName : temp.foods[j].name,
								foodPrice : temp.foods[j].actualPrice,
								foodAmount : temp.foods[j].count,
								imgUrl : temp.foods[j].img ? temp.foods[j].img.thumbnail : '../../images/noImage.jpg'
							}));
						}
						var head = template.orderHead.format({
							orderId : temp.orderId,
							orderDate : temp.date
						})
						
						var foot = template.orderFoot.format({
							totalAmount : count,
							status : temp.statusDesc,
							price : temp.price.toFixed(2)
						});
						
						$('#' + container.attr('id')).append(head + body.join('') + foot);
						
						$('#' + container.attr('id')).find('[data-type=controlExpand_orderList]').each(function(index, el){
							el.onclick = function(){
								$(el).siblings().toggle();
							}
						});
					});
		
				},
				error : function(xhr, errorType, error){},
				complete : function(){
					loadingDialog.hide();
				}
			});
		},
		onDeactive : function(){
		}
	}, {
		tab : '预订列表',
		initTab : 1,
		onActive : function(container){
			var loadingDialog;
			loadingDialog = wxLoadDialog.instance();
			loadingDialog.show();
			$.ajax({
				url : '../../../WxOperateBook.do',
				dataType : 'json',
				data : {
					dataSource : 'getByCond',
					oid : Util.mp.oid,
					fid : Util.mp.fid,
					sessionId : Util.mp.params.sessionId
				},
				success : function(data, status, xhr){
					var html = [];
					$('#' + container.attr('id')).html('');
					for(var i = 0; i < data.root.length; i++){
						var bookInfo = data.root[i];
						var count = 0, foods = '';
						var body = [];
						if(bookInfo.order){
							count = bookInfo.order.orderFoods.length;
							for (var j = 0; j < bookInfo.order.orderFoods.length; j++) {
								if(foods){
									foods += '，';
								}
								foods += bookInfo.order.orderFoods[j].foodName;
								body.push(template.orderBody.format({
									foodName : bookInfo.order.orderFoods[j].foodName,
									foodPrice : bookInfo.order.orderFoods[j].actualPrice,
									foodAmount : bookInfo.order.orderFoods[j].count,
									imgUrl : bookInfo.order.orderFoods[j].img ? bookInfo.order.orderFoods[j].img.thumbnail : 'images/noImage.jpg'
								}));
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
						
						
						var head = template.orderHead.format({
							orderDate : data.root[i].bookDate,
							orderId : data.root[i].id
						});
						
						var foot = template.orderFoot.format({
							status : data.root[i].statusDesc,
							totalAmount : count,
							price : totalPrice
						});
		
						$('#' + container.attr('id')).append(head + body.join('') + foot);
						$('#' + container.attr('id')).find('[data-type=controlExpand_orderList]').each(function(index, el){
							el.onclick = function(){
								$(el).siblings().toggle();
							}
						});
					}
					
				},
				error : function(xhr, errorType, error){},
				complete : function(){
					loadingDialog.hide();
				}
			});	
		},
		onDeactive : function(){
		}
	}]).open();
});