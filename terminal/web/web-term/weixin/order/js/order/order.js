var Templet = {
		
	mainBox: '<div class="box-order {orderBorder}">' +
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
			'</div>'
};

$(function(){
	Util.lbar('', function(html){ $(document.body).append(html);  });
	$.ajax({
		url : '../../WXQueryOrder.do',
		dataType : 'json',
		data : {
			dataSource : 'getByMember',
			oid : Util.mp.oid,
			fid : Util.mp.fid
		},
		success : function(data, status, xhr){
			var html = [];
			for(var i = 0; i < data.root.length; i++){
				var temp = data.root[i];
				var totalPrice = 0, count = temp.foods.length, foods = '';
				for (var j = 0; j < temp.foods.length; j++) {
					totalPrice += temp.foods[j].totalPrice;
					if(foods){
						foods += ',';
					}
					foods += temp.foods[j].name;
				}
				
				html.push(Templet.mainBox.format({
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
			$('#divWeixinOrderList').before(html.join(''));
		},
		error : function(xhr, errorType, error){
//			alert('error');
		}
	});
});