

function buildInvitation(bookId){
	Util.jump("invitation.html", bookId);
}


$(function(){
	
	var Template = {
		
		inside: '<div class="box-order {orderBorder}">' +
					'<div class="{orderClass}"><div style="height: 20px;">' +
							'<div style="float: left;">订单号:{code}</div>' +
							'<div style="float: right;color:#26a9d0;">{status}</div>' +
					'</div></div>' +
					'<div>点单日期: <span style="font-weight: bold">{orderDate}</span></div>' +
					'<div>' +
						'<div style="float: left">金额: <span style="font-weight: bold;color: red;">{totalPrice}</span> 元</div>' +
						'<div style="float: right">菜品种类: <span style="font-weight: bold;color: red;">{count}</span> 项</div>' +
					'</div>' +
					'<div {display}>菜名: <span style="font-weight: bold;">{foods}</span></div>' +
				'</div>',
		book : '<div class="box-order orderBorder_commit">'	+
					'<div class="box-order-commit"><div style="height: 20px;">' +
						'<div style="float: left;">预订单号:{code}</div>' +
						'<div style="float: right;color:#26a9d0;">{status}</div>' +
					'</div></div>' +
					'<div>预定日期: <span style="font-weight: bold">{orderDate}</div>' +
					'<div>预定区域: <span style="font-weight: bold">{orderRegion}</span></div>' +
					'<div>' +
					'<div style="float: left">预定菜品: <span style="font-weight: bold;color: red;">{count}</span> 项</div>' +
					'<div style="float: right">菜品价钱: <span style="font-weight: bold;color: red;">{totalPrice}</span> 元</div>' +
					'</div>' +
					'<div {display}>菜名: <span style="font-weight: bold;">{foods}</span></div>' +
					'<div style="margin-top: 5px;height: 33px;width: 99%;text-align: center;" >' +
						'<a class="a_demo_two" style="font-size: 20px;font-weight: bold;" onclick="buildInvitation({bookId})">发送邀请函</a>' +		
					'</div>	'+	
				'</div>'
		
	};
	
	Util.lbar('', function(html){ $(document.body).append(html);  });
	//获取订单列表
	$.ajax({
		url : '../../WxQueryOrder.do',
		dataType : 'json',
		data : {
			dataSource : 'getByMember',
			oid : Util.mp.oid,
			fid : Util.mp.fid,
			type : Util.mp.extra 
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
					code : temp.code,
					status : temp.statusDesc,
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
			fid : Util.mp.fid
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
					code : bookInfo.id,
					status : bookInfo.statusDesc,
					orderDate : bookInfo.bookDate,
					orderRegion : bookInfo.region,
					totalPrice : totalPrice,
					count : count,
					display : !foods ? 'hidden="hidden"' : '',
					foods : foods,
					bookId : bookInfo.id
				}));						

			}
			$('#divWeixinBookList').html(html.join(''));
		},
		error : function(xhr, errorType, error){}
	});
	
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