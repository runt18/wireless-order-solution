var Templet = {
		
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
	takeout: '<div class="box-order {orderBorder}">' +
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
			'<div>电话: <span style="font-weight: bold;">{phone}</span></div>' +
			'<div>地址: <span style="font-weight: bold;">{address}</span></div>' +			
		'</div>',
	book : '<div class="box-order orderBorder_commit">'	+
				'<div class="box-order-commit"><div style="height: 20px;">' +
					'<div style="float: left;">订单号:{code}</div>' +
					'<div style="float: right;color:#26a9d0;">{status}</div>' +
				'</div></div>' +
				'<div>预定日期: <span style="font-weight: bold">{orderDate}</span></div>' +
				'<div>' +
				'<div style="float: left">定金: <span style="font-weight: bold;color: red;">{totalPrice}</span> 元</div>' +
				'<div style="float: right">预定菜品: <span style="font-weight: bold;color: red;">{count}</span> 项</div>' +
				'</div>' +
				'<div {display}>菜名: <span style="font-weight: bold;">{foods}</span></div>' +
				'<div style="margin-top: 5px;height: 33px;width: 99%;text-align: center;" >' +
					'<a class="a_demo_two" style="font-size: 20px;font-weight: bold;" onclick="buildInvitation({bookId})">发送邀请函</a>' +		
				'</div>	'+	
			'</div>'
	
};

function changWeixinOrderList(otype){
	if(otype == 'order'){
		$('#divWeixinOrderList').show();
		$('#divWeixinBookList').hide();
	}else{
		$('#divWeixinBookList').show();
		$('#divWeixinOrderList').hide();
	}
}

function buildInvitation(bookId){
	Util.skip("invitation.html", bookId);
}


$(function(){
	Util.lbar('', function(html){ $(document.body).append(html);  });
	//获取订单列表
	$.ajax({
		url : '../../WXQueryOrder.do',
		dataType : 'json',
		data : {
			dataSource : 'getByMember',
			oid : Util.mp.oid,
			fid : Util.mp.fid,
			type : Util.mp.extra 
		},
		success : function(data, status, xhr){
			var html = [];
			for(var i = 0; i < data.root.length; i++){
				var temp = data.root[i];
				var totalPrice = 0, count = temp.foods.length, foods = '';
				for (var j = 0; j < temp.foods.length; j++) {
					totalPrice += temp.foods[j].totalPrice;
					if(foods){
						foods += '，';
					}
					foods += temp.foods[j].name;
				}
				if(Util.mp.extra == 3){//外卖
					html.push(Templet.takeout.format({
						code : temp.code,
						status : temp.statusDesc,
						orderDate : temp.date,
						totalPrice : totalPrice,
						count : count,
						foods : foods,
						display : temp.statusVal == 1 ? 'hidden="hidden"' : '',
						orderClass : temp.statusVal == 2?'box-order-commit' : '',
						orderBorder : temp.statusVal == 2?'orderBorder_commit' : 'orderBorder_invalid',
						phone : temp.contect.phone,
						address : temp.contect.address
					}));					
				}else{//默认店内
					html.push(Templet.inside.format({
						code : temp.code,
						status : temp.statusDesc,
						orderDate : temp.date,
						totalPrice : totalPrice,
						count : count,
						foods : foods,
						display : temp.statusVal == 1 ? 'hidden="hidden"' : '',
						orderClass : temp.statusVal == 2?'box-order-commit' : '',
						orderBorder : temp.statusVal == 2?'orderBorder_commit' : 'orderBorder_invalid'
					}));						
				}

			}
			$('#divWeixinOrderList').html(html.join(''));
		},
		error : function(xhr, errorType, error){}
	});
	
	//获取预定列表
	$.ajax({
		url : '../../WXQueryBook.do',
		dataType : 'json',
		data : {
			dataSource : 'bookList',
			oid : Util.mp.oid,
			fid : Util.mp.fid
		},
		success : function(data, status, xhr){
			var html = [];
			for(var i = 0; i < data.root.length; i++){
				var temp = data.root[i];
				var totalPrice = 0, count = 0, foods = '';
				
				if(temp.order){
					count = temp.order.orderFoods.length;
					for (var j = 0; j < temp.order.orderFoods.length; j++) {
						totalPrice += temp.order.orderFoods[j].totalPrice;
						if(foods){
							foods += '，';
						}
						foods += temp.order.orderFoods[j].foodName;
					}				
				}

				
				html.push(Templet.book.format({
					code : temp.id,
					status : temp.statusDesc,
					orderDate : temp.bookDate,
					totalPrice : temp.money,
					count : count,
					display : !foods ? 'hidden="hidden"' : '',
					foods : foods,
					bookId : temp.id
				}));						

			}
			$('#divWeixinBookList').html(html.join(''));
		},
		error : function(xhr, errorType, error){}
	});
	
	//切换tab
	$('.tabPanelList').on('click', function(){
		$('.tabPanelList').removeClass('tabPanelListCheck');
		$(this).addClass('tabPanelListCheck');
	});
});