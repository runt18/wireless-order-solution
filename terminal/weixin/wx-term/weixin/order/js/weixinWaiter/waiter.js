$(function(){
	initWaiterOrder();
	
				
	function initWaiterOrder(orderId){
		$.ajax({
			url : '../../WxOperateWaiter.do',
			data : {
				dataSource : 'getOrder',
				fid : Util.mp.fid,
				orderId : '6534897'
			},
			type : 'post',
			dataType : 'json',
			success : function(data, status, xhr){
				console.log(data);
				
				//赋值账单号
				$('#orderId_font_waiter').text(data.root[0].id);
				
				//赋值给餐台号
				$('#tableNum_font_waiter').text(data.root[0].tableAlias);
				
				//赋值给开台时间
				$('#openTableTime_font_waiter').text(data.root[0].birthDate);
				
				//赋值给开台人
				$('#openTablePeople_font_waiter').text(data.root[0].waiter);
				
				//加载菜品数据
				initFoodList(data.root[0]);
			}
		});		
	}
	
	
			
			
	function initFoodList(data){
		var orderListTemplete = '<div class="main-box" style="background-color: cornsilk;">'+
									'<ul class="m-b-list">'+
										'<li style="border-bottom:0px;line-height:10px;">&nbsp;</li>'+
										'<li  class="box-horizontal" style="border-bottom:0px;line-height:15px;">'+
											'<div style="width:90%;">{index}、{foodName}</div>'+
											'<div style="width:10%;"><font style="font-weight:bold;color:green">{foodPrice}元</font></div>'+
										'</li>'+
										'<li style="border-bottom:0px;line-height:10px;">&nbsp;</li>'+	
											'<div class="box-horizontal" style="line-height:15px;border-bottom:0px;">'+
											'<div style="width:98%;"><font style="font-family:Arial;font-size:12px;">{foodUnit}</font></div>'+
										'</div>'+
										'<li style="border-bottom:0px;line-height:10px;">&nbsp;</li>'+
									'</ul>'+
								'</div>';
		
		var html = [];
		
		data.orderFoods.forEach(function(temp, i){
			html.push(orderListTemplete.format({
				index : i+1,
				foodName : temp.foodName,
				foodPrice : temp.totalPrice,
				foodUnit : temp.tasteGroup.tastePref
			}));
		});
		
		
		$('#orderList_div_waiter').html(html.join(''));
	}
	
	
	
	function setView(){
		var height = document.documentElement.clientHeight;
		$('#weixinWaiter_div_waiter').css('height', height-45);
	}
	
	window.onresize = setView;
	window.onresize();
})