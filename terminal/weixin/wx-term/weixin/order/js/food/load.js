$(function(){
	
	$.ajax({
		url : '../../WxInterface.do',
		dataType : 'json',
		data : {
			dataSource : 'jsApiSign',
			url: location.href.split('#')[0],
			fid : Util.mp.fid
		},
		success : function(data, status, xhr){
			wx.config({
			    debug: false,  // 开启调试模式,调用的所有api的返回值会在客户端alert出来，若要查看传入的参数，可以在pc端打开，参数信息会通过log打出，仅在pc端时才会打印。
			    appId: data.other.appId,  // 必填，公众号的唯一标识
			    timestamp: data.other.timestamp,  // 必填，生成签名的时间戳
			    nonceStr: data.other.nonceStr,  // 必填，生成签名的随机串
			    signature: data.other.signature, // 必填，签名，见附录1
			    jsApiList: ["scanQRCode"]  // 必填，需要使用的JS接口列表，所有JS接口列表见附录2
			});		
		},
		error : function(xhr, errorType, error){
			alert('操作失误, 请重新进入');
		}
	});	
	
	 //自助点餐
	 var pickFoodComponent = new PickFoodComponent({
		 confirm : function(orderFoodData){
			 if(orderFoodData.length == 0){
				 Util.dialog.show({ msg : '您的购物车没有菜品, 请先选菜.', btn : 'yes'});
				 return;
			 }
			 
			wx.scanQRCode({
			    needResult: 1, // 默认为0，扫描结果由微信处理，1则直接返回扫描结果，
			    scanType: ["qrCode","barCode"], // 可以指定扫二维码还是一维码，默认二者都有
			    success: function (res) {
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
