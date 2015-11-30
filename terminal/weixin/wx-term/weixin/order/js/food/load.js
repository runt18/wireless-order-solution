$(function(){
	 //自助点餐
	 var pickFoodComponent = new PickFoodComponent({
		 confirm : function(orderFoodData){
			 if(orderFoodData.length == 0){
				 Util.dialog.show({ msg : '您的购物车没有菜品, 请先选菜.', btn : 'yes'});
				 return;
			 }
			 Util.lm.show();
			 
			 var foods = "";
			 var temp = null;
			 for(var i =0; i < orderFoodData.length; i++){
				 temp = orderFoodData[i];
				 if(i > 0){
					 foods += '&';
				 }
				 foods += (temp.id + ',' + temp.count);
			 }
			 $.ajax({
					url : '../../WxOperateOrder.do',
					dataType : 'json',
					type : 'post',
					data : {
						dataSource : 'insertOrder',
						oid : Util.mp.oid,
						fid : Util.mp.fid,
						foods : foods
					},
					success : function(data, status, xhr){
						Util.lm.hide();
						if(data.success){
							//刷新界面
							pickFoodComponent.refresh();
							pickFoodComponent.closeShopping();
							
							Util.dialog.show({title : '请呼叫服务员确认订单', msg : '<font style="font-weight:bold;font-size:25px;">订单号: ' + data.other.order.code + '</font>', btn : 'yes' });
						}else{
							Util.dialog.show({ msg : data.msg });
						}
					},
					error : function(xhr, errorType, error){
						Util.lm.hide();
						Util.dialog.show({ msg : '操作失败, 数据请求发生错误.' });
					}
				});
		 },
		 onCartChange : function(orderFoodData){
			 if(orderFoodData.length > 0){
				 document.getElementById('displayFoodCount_div_fastOrderFood').innerHTML = orderFoodData.length;
				 document.getElementById('displayFoodCount_div_fastOrderFood').style.visibility = 'visible';
			 }else{
				 document.getElementById('displayFoodCount_div_fastOrderFood').innerHTML ='';
				 document.getElementById('displayFoodCount_div_fastOrderFood').style.visibility = 'hidden';
			 }
		 }
	 });  
	 
	 pickFoodComponent.open();
	 
	  //打开购物车
	  $('#shoppingCar_li_member').click(function(){
		  pickFoodComponent.openShopping();
	  });
	  
	  
	  $('#foodOrderList').click(function(){
		  Util.jump('orderList.html', Util.mp.extra);
	  });
});
