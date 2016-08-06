$(function(){
	var _orderType = null;      //下单方式
	var _prefectMemberStauts = null   //是否显示完善会员资料
	var _confirmText = '';
	
	var orderType = {
		WX_PAY : 1,				//微信支付下单
		CONFIRM_BY_STAFF : 2,	//确认下单
		DIRECT_ORDER : 3		//直接下单
	}
	
	var prefectMemberStauts = {
		SHOW_PREFECMEMBER : 0,   //显示完善会员资料
		HIDE_PREFECTMEMBER : 1  //不显示完善会员资料
	}
	
	$.ajax({
		url : '../../../WxOperateRestaurant.do',
		dataType : 'json',
		type : 'post',
		data : {
			dataSource : 'detail',
			fid : Util.mp.fid,
			branchId : Util.mp.params.branchId,
			sessionId : Util.mp.params.sessionId
		},
		success : function(data, status, xhr){
			if(data.success){
				document.title = data.root[0].name; 
			}else if(data.code == '7546'){
				sessionTimeout();
			}else{
				Util.showErrorMsg(data.msg);
			}
		},
		error : function(data, status, err){
			if(err.code == '7546'){
				sessionTimeout();
			}else{
				Util.showErrorMsg(err.msg);					
			}
		}
	});
	
	//获取门店信息
	$.ajax({
		url : '../../../WxOperateRestaurant.do',
		dataType : 'json',
		type : 'post',
		data : {
			dataSource : 'getByCond',
			fid : Util.mp.fid,
			branchId : Util.mp.params.branchId,
			sessionId : Util.mp.params.sessionId
		},
		success : function(data, status, xhr){
			if(data.success){
				_orderType = data.root[0].defaultOrderType;
				
				//选好了文字
				if(_orderType == orderType.WX_PAY){//微信支付
					_confirmText = '微信支付';
				}else if(_orderType == orderType.CONFIRM_BY_STAFF){//确认下单
					_confirmText = '确认下单';
				}else if(_orderType == orderType.DIRECT_ORDER){//直接下单
					_confirmText = '直接下单';
				}
			}else if(data.code == '7546'){
				sessionTimeout();
			}else{
				Util.showErrorMsg(data.msg);
			}
		},
		error : function(data, status, err){
			if(err.code == '7546'){
				sessionTimeout();
			}else{
				Util.showErrorMsg(err.msg);					
			}
			_prefectMemberStauts = data.root[0].prefectMemberStatus;
		}
	});
	
	
	var createFastOrderFood = new CreateFastOrderFood({
		confirmText : _confirmText,
		confirm : function(orderData){
			//TODO 未绑定会员出现绑定会员			
			var foods = '';
			var unitId = 0; 
			
			orderData.forEach(function(element, index){
				if(index > 0){
					foods += '&';
				}
				
				if(element.selectedUnitPrice){
					unitId = element.selectedUnitPrice.id;
				}else{
					unitId = 0;
				}
					
				foods += element.food.id + ',' + element.count + ',' + unitId;
			});
			
			
			//微信支付下单
			if(_orderType == orderType.WX_PAY){
				$.ajax({
					url : '../../../WxOperateOrder.do',
					type : 'post',
					dataType : 'json',
					data : {
						dataSource : 'calcOrder',
						sessionId : Util.mp.params.sessionId,
						foods : foods,
						branchId : Util.mp.params.branchId
					},
					success : function(data, status, req){
						if(data.success){
							var calcOrderCost = data.root[0].actualPrice;
							
							wxLoadDialog.instance().show();
							$.ajax({
								url : '../../../WxOperateOrder.do',
								type : 'post',
								dataType : 'json',
								data : {
									dataSource : 'wxPayOrder',
									sessionId : Util.mp.params.sessionId,
									foods : foods,
									cost : calcOrderCost
								},
								success : function(data, status, req){
									wxLoadDialog.instance().hide();
									if(data.success){
										payParam = data.other;
										if(typeof WeixinJSBridge == 'undefined'){
											if (document.addEventListener) {
												document.addEventListener('WeixinJSBridgeReady', onBridgeReady,	false);
											} else if (document.attachEvent) {
												document.attachEvent('WeixinJSBridgeReady', onBridgeReady);
												document.attachEvent('onWeixinJSBridgeReady', onBridgeReady);
											}
										}else{
											onBridgeReady();
										} 
										
									}else{
										payParam = null;
										var dialog = new WeDialogPopup({
											content : data.msg,
											titleText : '微信支付失败',
											leftText : '确认',
											left : function(){
												dialog.close();
											},
											afterClose : function(){
												window.location.reload();
											}
										})
										dialog.open();
									}
								},
								error : function(req, status, error){
									wxLoadDialog.instance().hide();
									var dialog = new WeDialogPopup({
										content : data.msg,
										titleText : '微信支付失败',
										leftText : '确认',
										left : function(){
											dialog.close();
										},
										afterClose : function(){
											window.location.reload();
										}
									})
									dialog.open();
								}
							});
						}else if(data.code == '7546'){
							sessionTimeout();
						}else{
							Util.showErrorMsg(data.msg);
						}
					},
					error : function(req, status, err){
						if(err.code == '7546'){
							sessionTimeout();
						}else{
							Util.showErrorMsg(err.msg);					
						}
					}
				});
			}else if(_orderType == orderType.CONFIRM_BY_STAFF){
				//确认下单
				wxLoadDialog.instance().show();
				$.ajax({
					url : '../../../WxOperateOrder.do',
					type : 'post',
					dataType : 'json',
					data : {
						dataSource : 'insertOrder',
						sessionId : Util.mp.params.sessionId,
						foods : foods,
						branchId : Util.mp.params.branchId,
						print : true
					},
					success : function(data, status, xhr){
						wxLoadDialog.instance().hide();
						if(data.success){
							//提示框设置
							var finishOrderDialog = new WeDialogPopup({
								titleText : '温馨提示',
								content : '<span style="display:block;text-align:center;">下单成功,等待服务员确认订单</span>',
								leftText : '确认',
								left : function(){
									finishOrderDialog.close();								
								},
								afterClose : function(){
									window.location.reload();
								}
							});
							
							finishOrderDialog.open();
							
						}else if(data.code == '7546'){
							sessionTimeout();
						}else{
							Util.showErrorMsg(data.msg);
						}
						
					},
					error : function(xhr, status, err){
						wxLoadDialog.instance().hide();
						if(err.code == '7546'){
							sessionTimeout();
						}else{
							Util.showErrorMsg(err.msg);					
						}
					}
				});
				 
			}else if(_orderType == orderType.DIRECT_ORDER){
				wxLoadDialog.instance().show();
				//直接下单
				$.ajax({
					url : '../../../WxOperateOrder.do',
					type : 'post',
					dataType : 'json',
					data : {
						dataSource : 'insert',
						foods : foods,
						sessionId : Util.mp.params.sessionId,
						force : true
					},
					success : function(data, status, req){
						if(data.success){
							wxLoadDialog.instance().hide();
							//提示框设置
							var finishOrderDialog;
							finishOrderDialog = new WeDialogPopup({
								titleText : '温馨提示',
								content : '<span style="display:block;text-align:center;">下单成功,厨房正在安排,请稍等</span>',
								leftText : '确认',
								left : function(){
									finishOrderDialog.close();								
								},
								afterClose : function(){
									//关闭后回调
									window.location.reload();
								},
								dismissable : true
							});
							
							finishOrderDialog.open();
						}else if(data.code == '7546'){
							sessionTimeout();
						}else{
							Util.showErrorMsg(data.msg);
						}
					},
					error : function(req, status, err){
						wxLoadDialog.instance().hide();
						if(err.code == '7546'){
							sessionTimeout();
						}else{
							Util.showErrorMsg(err.msg);					
						}
					}
				});
			}else{
				var errDialog;
				errDialog = new WeDialogPopup({
					titleText : '温磬提示',
					content : ('<span style="display:block;text-align:center;">餐厅暂时不支持微信下单</span>'),
					leftText : '确认',
					left : function(){
						errDialog.close();
					}
				});
				
				errDialog.open();
			}
		}
	});
	createFastOrderFood.open();
	
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
				if (res.err_msg == "get_brand_wcpay_request:ok") {
					// 使用以上方式判断前端返回,微信团队郑重提示：res.err_msg将在用户支付成功后返回ok，但并不保证它绝对可靠。
					var finishOrderDialog = new WeDialogPopup({
						titleText : '温馨提示',
						content : '<span style="display:block;text-align:center;">微信支付下单成功,厨房正在准备,请稍等</span>',
						leftText : '确认',
						left : function(){
							finishOrderDialog.close();								
						},
						afterClose : function(){
							
							//关闭后回调
							window.location.href = 'orderList.html?sessionId=' + Util.mp.params.sessionId + "&m=" + Util.mp.oid + "&r=" + Util.mp.fid;
						}
					});
					
					finishOrderDialog.open();
				} 
			});
		}
	}
	
	//session 过期处理
	function sessionTimeout(){
		var sessionTimeoutPopup;
		sessionTimeoutPopup = new WeDialogPopup({
			titleText : '温磬提示',
			content : ('<span style="display:block;text-align:center;">链接已过期,请重新进入</span>'),
			leftText : '确认',
			left : function(){
				sessionTimeoutPopup.close();
			},
			afterClose : function(){
				wx.closeWindow();
			}
		});
		sessionTimeoutPopup.open();
	}
	
});