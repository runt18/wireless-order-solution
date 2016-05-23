$(function(){
	//调试模式
	var load_debug = false;
	
	var _orderType = null;      //下单方式
	var _prefectMemberStauts = null   //是否显示完善会员资料
	
	var orderType = {
		WX_PAY : 1,				//微信支付下单
		CONFIRM_BY_STAFF : 2,	//确认下单
		DIRECT_ORDER : 3		//直接下单
	}
	
	var prefectMemberStauts = {
		SHOW_PREFECMEMBER : 0,   //显示完善会员资料
		HIDE_PREFECTMEMBER : 1  //不显示完善会员资料
	}
	
	
 	if(Util.mp.extra){
		$.ajax({
			url : '../../WxOperateRestaurant.do',
			dataType : 'json',
			type : 'post',
			data : {
				dataSource : 'detail',
				branchId : Util.mp.extra,
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
			url : '../../WxOperateRestaurant.do',
			dataType : 'json',
			type : 'post',
			data : {
				dataSource : 'getByCond',
				branchId : Util.mp.params.branchId,
				sessionId : Util.mp.params.sessionId
			},
			success : function(data, status, xhr){
				if(data.success){
					_orderType = data.root[0].defaultOrderType;
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
	}else{
		$.ajax({
			url : '../../WxOperateRestaurant.do',
			dataType : 'json',
			type : 'post',
			data : {
				dataSource : 'detail',
				fid : Util.mp.fid,
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
			url : '../../WxOperateRestaurant.do',
			dataType : 'json',
			type : 'post',
			data : {
				dataSource : 'getByCond',
				branchId : Util.mp.params.branchId,
				sessionId : Util.mp.params.sessionId
			},
			success : function(data, status, xhr){
				if(data.success){
					_orderType = data.root[0].defaultOrderType; 
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
	}
	 
	$.ajax({
		url : '../../WxInterface.do',
		dataType : 'json',
		type : 'post',
		data : {
			dataSource : 'jsApiSign',
			url: location.href.split('#')[0],
			fid : Util.mp.fid,
			sessionId : Util.mp.params.sessionId
		},
		success : function(data, status, xhr){
			if(data.success){
				wx.config({
				    debug: false, 						// 开启调试模式,调用的所有api的返回值会在客户端alert出来，若要查看传入的参数，可以在pc端打开，参数信息会通过log打出，仅在pc端时才会打印。
				    appId: data.other.appId,  			// 必填，公众号的唯一标识
				    timestamp: data.other.timestamp,  	// 必填，生成签名的时间戳
				    nonceStr: data.other.nonceStr,  	// 必填，生成签名的随机串
				    signature: data.other.signature, 	// 必填，签名，见附录1
				    jsApiList: ["scanQRCode"]  			// 必填，需要使用的JS接口列表，所有JS接口列表见附录2
				});		 
			}else if(data.code == '7546'){
				sessionTimeout();
			}else{
				Util.showErrorMsg(data.msg);
			}
		},
		error : function(xhr, errorType, error){
			if(err.code == '7546'){
				sessionTimeout();
			}else{
				Util.showErrorMsg(error.msg);					
			}
		}
	});	
	
	 //自助点餐
	 var pickFoodComponent = new PickFoodComponent({
	 	 payType : _orderType,
		 confirm : function(orderFoodData, comment, container, calcOrderCost){
			if(orderFoodData.length == 0){
				var dialog = new WeDialogPopup({
					content : '你的购物车没有菜品,请先选菜',
					leftText : '确定',
					left : function(){
						dialog.close();
					}
				})
				dialog.open();
				return;
			}
			Util.lm.show();
			
			
			if(__prefectMemberStauts = prefectMemberStauts.SHOW_PREFECMEMBER){//显示
				$.ajax({
					url : '../../WXOperateMember.do',
					type : 'post',
					data : {
						dataSource : 'getByCond',
						oid : Util.mp.oid,
						fid : Util.mp.fid,
						sessionId : Util.mp.params.sessionId
					},
					dataType : 'json',
					success : function(data){
						Util.lm.hide();
						if(data.success){
							if(data.root[0].isRaw){
								var completeMemberMsgDialog = new CompleteMemberMsg({
									completeFinish : function(){
										commit(calcOrderCost);
									},
									sessionId : Util.mp.params.sessionId
								});
								completeMemberMsgDialog.open();
							}else{
								commit(calcOrderCost);
							}
						}else if(data.code == '7546'){
							sessionTimeout();
						}else{
							Util.showErrorMsg(data.msg);
						}
					},
					error : function(req, status, err){
						Util.lm.hide();
						if(err.code == '7546'){
							sessionTimeout();
						}else{
							Util.showErrorMsg(err.msg);					
						}
					}
				});
			}else{
				commit(calcOrderCost);
			}
			
			
			//下单功能数据传递
			function commit(calcOrderCost){
				var tableId = Util.mp.params.tableId;
				var foods = '';
				var unitId = 0; 
				
				orderFoodData.forEach(function(element, index){
					if(index > 0){
						foods += '&';
					}
					
					if(element.unitPriceId){
						unitId = element.unitPriceId;
						
					}else{
						unitId = 0;
					}
						
					foods += element.id + ',' + element.count + ',' + unitId;
				});
				
				
				
				var payType = null;
				$('[name=payType_input_waiter]').each(function(index, el){
					if(el.checked == true){
						payType = el.getAttribute('data-type');
					}
				});
				
				if(_orderType == orderType.CONFIRM_BY_STAFF){
					$.ajax({
						url : '../../WxOperateOrder.do',
						type : 'post',
						dataType : 'json',
						data : {
							dataSource : 'insertOrder',
//									oid : Util.mp.oid,
//									fid : Util.mp.fid,
							sessionId : Util.mp.params.sessionId,
							foods : foods,
							branchId : Util.mp.params.branchId,
							comment : comment,
							print : true,
							tableId : tableId
						},
						success : function(data, status, xhr){
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
										//关闭后回调
										window.location.href = 'orderList.html?sessionId=' + Util.mp.params.sessionId + "&m=" + Util.mp.oid + "&r=" + Util.mp.fid;
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
							if(err.code == '7546'){
								sessionTimeout();
							}else{
								Util.showErrorMsg(err.msg);					
							}
						}
					});
				}else if(_orderType == orderType.WX_PAY || _orderType == orderType.DIRECT_ORDER){
//					if(_orderType == orderType.WX_PAY){
//						Util.lm.show();
//						$.ajax({
//							url : '../../WxOperateOrder.do',
//							type : 'post',
//							dataType : 'json',
//							data : {
//								dataSource : 'wxPayOrder',
//								sessionId : Util.mp.params.sessionId,
//	//							fid : Util.mp.fid,
//	//							oid : Util.mp.oid,
//								foods : foods,
//								cost : calcOrderCost,
//								tableId : tableId ? tableId : ''
//							},
//							success : function(data, status, req){
//								Util.lm.hide();
//								if(data.success){
//									payParam = data.other;
//									if(typeof WeixinJSBridge == 'undefined'){
//										if (document.addEventListener) {
//											document.addEventListener('WeixinJSBridgeReady', onBridgeReady,	false);
//										} else if (document.attachEvent) {
//											document.attachEvent('WeixinJSBridgeReady', onBridgeReady);
//											document.attachEvent('onWeixinJSBridgeReady', onBridgeReady);
//										}
//									}else{
//										onBridgeReady();
//									} 
//									
//								}else{
//									payParam = null;
//									var dialog = new WeDialogPopup({
//										content : data.msg,
//										titleText : '微信支付失败',
//										leftText : '确认',
//										left : function(){
//											dialog.close();
//										}
//									})
//									dialog.open();
//								}
//							},
//							error : function(req, status, error){
//								alert(error);
//							}
//						});
//					}else if(_orderType == orderType.DIRECT_ORDER){
//						$.ajax({
//							url : '../../WxOperateOrder.do',
//							type : 'post',
//							dataType : 'json',
//							data : {
//								dataSource : 'insert',
//								foods : foods,
//								sessionId : Util.mp.params.sessionId,
//								tableId : tableId,
//								force : true
//							},
//							success : function(data, status, req){
//								if(data.success){
//									//提示框设置
//									var finishOrderDialog = new WeDialogPopup({
//										titleText : '温馨提示',
//										content : '<span style="display:block;text-align:center;">下单成功,厨房正在安排,请稍等</span>',
//										leftText : '确认',
//										left : function(){
//											finishOrderDialog.close();								
//										},
//										afterClose : function(){
//											//关闭后回调
//											window.location.href = 'orderList.html?sessionId=' + Util.mp.params.sessionId;
//										}
//									});
//									
//									finishOrderDialog.open();
//								}
//							},
//							error : function(req, status, err){
//								var errDialog;
//								errDialog = new WeDialogPopup({
//									titleText : '温磬提示',
//									content : ('<span style="display:block;text-align:center;">' + err.msg + '</span>'),
//									leftText : '确认',
//									left : function(){
//										errDialog.close();
//									}
//								});
//							}
//						});
//					}
//					
//				
//					var errDialog;
//					errDialog = new WeDialogPopup({
//						titleText : '温磬提示',
//						content : ('<span style="display:block;text-align:center;">餐厅不支持该下单方式</span>'),
//						leftText : '确认',
//						left : function(){
//							errDialog.close();
//						},
//						afterClose : function(){
//							window.location.href = 'orderList.html?sessionId=' + Util.mp.params.sessionId;
//						}
//					});
//					errDialog.open();
					var errDialog;
					errDialog = new WeDialogPopup({
						titleText : '温磬提示',
						content : ('<span style="display:block;text-align:center;">餐厅暂时不支持微信下单</span>'),
						leftText : '确认',
						left : function(){
							errDialog.close();
						},
						afterClose : function(){
							window.location.href = 'orderList.html?sessionId=' + Util.mp.params.sessionId + "&m=" + Util.mp.oid + "&r=" + Util.mp.fid;
						}
					});
					errDialog.open();
					
				}else{
					var errDialog;
					errDialog = new WeDialogPopup({
						titleText : '温磬提示',
						content : ('<span style="display:block;text-align:center;">餐厅暂时不支持微信下单</span>'),
						leftText : '确认',
						left : function(){
							errDialog.close();
						},
						afterClose : function(){
							window.location.href = 'orderList.html?sessionId=' + Util.mp.params.sessionId + "&m=" + Util.mp.oid + "&r=" + Util.mp.fid;
						}
					});
					errDialog.open();
				}
			}
		},
		bottomId : 'bottom',
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
	
	
	 
	  //打开购物车
	$('#shoppingCar_li_member').click(function(){
		pickFoodComponent.openShopping();
	});
	  
	  
	$('#foodOrderList').click(function(){
		window.location.href = 'orderList.html?sessionId=' + Util.mp.params.sessionId + "&m=" + Util.mp.oid + "&r=" + Util.mp.fid;
//		Util.jump('orderList.html', typeof Util.mp.extra != 'undefined' ? Util.mp.extra : '');
	});
	
	//首页
	$('#jumpIndex_a_member').click(function(){
		window.location.href = 'index.html?sessionId=' + Util.mp.params.sessionId + "&m=" + Util.mp.oid + "&r=" + Util.mp.fid;
	});
});
