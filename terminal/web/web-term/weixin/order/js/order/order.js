var Templet = {
		
	mainBox: '<div class="box-order">' +
				'<div><div style="height: 20px;">' +
						'<div style="float: left;font-weight: bold">订单号:{code}</div>' +
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
				
				var totalPrice = 0, count = temp.orderFoods.length, foods = '';
				for (var j = 0; j < temp.orderFoods.length; j++) {
					totalPrice += temp.orderFoods[j].totalPrice;
					if(foods){
						foods += ',';
					}
					foods += temp.orderFoods[j].name;
				}
				
				html.push(Templet.mainBox.format({
					code : temp.code,
					status : temp.statusDesc,
					orderDate : temp.date,
					totalPrice : totalPrice,
					count : count,
					foods : foods,
					display : temp.statusVal == 1 ? 'hidden="hidden"' : ''
				}));
			}
			$('#divWeixinOrderList').before(html.join(''));
		},
		error : function(xhr, errorType, error){
//			alert('error');
		}
	});
});