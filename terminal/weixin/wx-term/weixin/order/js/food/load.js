$(function(){
	//调试模式
	var load_debug = false;
	
	var _orderType = null;
	
	var orderType = {
		WX_PAY : 1,				//微信支付下单
		CONFIRM_BY_STAFF : 2,	//确认下单
		DIRECT_ORDER : 3		//直接下单
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
				document.title = data.root[0].name; 
			}
		});
		
		//获取门店信息
		$.ajax({
			url : '../../WxOperateRestaurant.do',
			dataType : 'json',
			type : 'post',
			data : {
				dataSource : 'getByCond',
				branchId : Util.mp.params.branchId
			},
			success : function(data, status, xhr){
				_orderType = data.root[0].defaultOrderType;
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
				document.title = data.root[0].name; 
			}
		});
		
		//获取门店信息
		$.ajax({
			url : '../../WxOperateRestaurant.do',
			dataType : 'json',
			type : 'post',
			data : {
				dataSource : 'getByCond',
				branchId : Util.mp.params.branchId
			},
			success : function(data, status, xhr){
				_orderType = data.root[0].defaultOrderType;
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
			wx.config({
			    debug: false, 						// 开启调试模式,调用的所有api的返回值会在客户端alert出来，若要查看传入的参数，可以在pc端打开，参数信息会通过log打出，仅在pc端时才会打印。
			    appId: data.other.appId,  			// 必填，公众号的唯一标识
			    timestamp: data.other.timestamp,  	// 必填，生成签名的时间戳
			    nonceStr: data.other.nonceStr,  	// 必填，生成签名的随机串
			    signature: data.other.signature, 	// 必填，签名，见附录1
			    jsApiList: ["scanQRCode"]  			// 必填，需要使用的JS接口列表，所有JS接口列表见附录2
			});		
		},
		error : function(xhr, errorType, error){
			alert('操作失误, 请重新进入');
		}
	});	
	
	 //自助点餐
	 var pickFoodComponent = new PickFoodComponent({
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
								}
							});
							completeMemberMsgDialog.open();
						}else{
							commit(calcOrderCost);
						}
					}else{
						alert(data.msg);
					}
				},
				error : function(req, status, err){
					Util.lm.hide();
					console.log(err);
				}
			});
			
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
						success : function(response, status, xhr){
							if(response.success){
								//提示框设置
								var finishOrderDialog = new WeDialogPopup({
									titleText : '温馨提示',
									content : '<span style="display:block;text-align:center;">下单成功</span>',
									leftText : '确认',
									left : function(){
										finishOrderDialog.close();								
									},
									afterClose : function(){
										//关闭后回调
										window.location.href = 'orderList.html?sessionId=' + Util.mp.params.sessionId;
									}
								});
								
								finishOrderDialog.open();
									
							}else{
								Util.dialog.show({msg : response.msg});	
							}
								
						},
						
						error : function(xhr, status, err){
							Util.dialog.show({msg : err.msg});
						}
					});
				}else if(_orderType){
					if(tableId){
						checkPayTypeDialog.close();
						
						if(_orderType == orderType.WX_PAY){
							Util.lm.show();
							$.ajax({
								url : '../../WxOperateOrder.do',
								type : 'post',
								dataType : 'json',
								data : {
									dataSource : 'wxPayOrder',
									sessionId : Util.mp.params.sessionId,
		//							fid : Util.mp.fid,
		//							oid : Util.mp.oid,
									foods : foods,
									cost : calcOrderCost,
									tableId : tableId ? tableId : ''
								},
								success : function(data, status, req){
									Util.lm.hide();
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
											}
										})
										dialog.open();
									}
								},
								error : function(req, status, error){
									alert(error);
								}
							});
						}else if(_orderType == orderType.DIRECT_ORDER){
							$.ajax({
								url : '../../WxOperateOrder.do',
								type : 'post',
								dataType : 'json',
								data : {
									dataSource : 'insert',
									foods : foods,
									sessionId : Util.mp.params.sessionId,
									tableId : tableId,
									force : true
								},
								success : function(data, status, req){
									if(data.success){
										//提示框设置
										var finishOrderDialog = new WeDialogPopup({
											titleText : '温馨提示',
											content : '<span style="display:block;text-align:center;">下单成功</span>',
											leftText : '确认',
											left : function(){
												finishOrderDialog.close();								
											},
											afterClose : function(){
												//关闭后回调
												window.location.href = 'orderList.html?sessionId=' + Util.mp.params.sessionId;
											}
										});
										
										finishOrderDialog.open();
									}
								},
								error : function(req, status, err){
									var errDialog;
									errDialog = new WeDialogPopup({
										titleText : '温磬提示',
										content : ('<span style="display:block;text-align:center;">' + err.msg + '</span>'),
										leftText : '确认',
										left : function(){
											errDialog.close();
										}
									});
								}
							});
						}
						
					
					}else{
						var showQrCodeDialog
						showQrCodeDialog = new WeDialogPopup({
							titleText : '温磬提示',
							content : '<span style="display:block;text-align:center;">请先扫描桌上二维码</span>',
							leftText : '确认',
							left : function(){
								showQrCodeDialog.close();
								wx.scanQRCode({
								    needResult: 1, // 默认为0，扫描结果由微信处理，1则直接返回扫描结果，
								    scanType: ["qrCode","barCode"], // 可以指定扫二维码还是一维码，默认二者都有
								    success: function (res) {
										
										if(payType == 'payByWeiXin'){
											Util.lm.show();
											$.ajax({
												url : '../../WxOperateOrder.do',
												type : 'post',
												dataType : 'json',
												data : {
													dataSource : 'wxPayOrder',
													sessionId : Util.mp.params.sessionId,
						//							fid : Util.mp.fid,
						//							oid : Util.mp.oid,
													foods : foods,
													cost : calcOrderCost,
													tableId : tableId ? tableId : '',
													tableAlias : tableId ? '' : res.resultStr.split('?')[1]
												},
												success : function(data, status, req){
													Util.lm.hide();
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
														checkPayTypeDialog.close();
														payParam = null;
														var dialog = new WeDialogPopup({
															content : data.msg,
															titleText : '微信支付失败',
															leftText : '确认',
															left : function(){
																dialog.close();
															}
														})
														dialog.open();
													}
												},
												error : function(req, status, error){
													alert(error);
												}
											});
										}else if(payType == 'fastOrder'){
											$.ajax({
												url : '../../WxOperateOrder.do',
												type : 'post',
												dataType : 'json',
												data : {
													dataSource : 'insert',
													foods : foods,
													sessionId : Util.mp.params.sessionId,
							//						fid : Util.mp.fid,
							//						oid : Util.mp.oid,
													tableAlias : res.resultStr.split('?')[1],
													tableId : tableId,
													force : true
												},
												success : function(data, status, req){
													if(data.success){
														//提示框设置
														var finishOrderDialog = new WeDialogPopup({
															titleText : '温馨提示',
															content : '<span style="display:block;text-align:center;">下单成功</span>',
															leftText : '确认',
															left : function(){
																finishOrderDialog.close();								
															},
															afterClose : function(){
																//关闭后回调
																window.location.href = 'orderList.html?sessionId=' + Util.mp.params.sessionId;
															}
														});
														
														finishOrderDialog.open();
													}
												},
												error : function(req, status, err){
													var errDialog;
													errDialog = new WeDialogPopup({
														titleText : '温磬提示',
														content : ('<span style="display:block;text-align:center;">' + err.msg + '</span>'),
														leftText : '确认',
														left : function(){
															errDialog.close();
														}
													});
													errDialog.open();
												}
											});
										}
										
									
								    }
								});
							}
						});
						checkPayTypeDialog.close();
						showQrCodeDialog.open();
					}
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
					
				
//				checkPayTypeDialog = new WeDialogPopup({
//					titleText : '请选择下单方式',
//					content : (		'<div class="weui_cells weui_cells_radio">'
//		            			+		'<label class="weui_cell weui_check_label" for="payByWeiXin_input_waiter">'
//		             		  	+				'<div class="weui_cell_bd weui_cell_primary">'
//		                  		+					'<p>微信支付下单</p>'
//		                		+				'</div>'
//		                		+			'<div class="weui_cell_ft">'
//		                		+   			'<input type="radio" class="weui_check" name="payType_input_waiter" id="payByWeiXin_input_waiter" data-type="payByWeiXin">'
//		                    	+				'<span class="weui_icon_checked"></span>'
//		                		+			'</div>'
//		            			+		'</label>'
//		           				+		'<label class="weui_cell weui_check_label" for="waiterCheck_input_waiter">'
//					            +    		'<div class="weui_cell_bd weui_cell_primary">'
//		            		    +   			'<p>服务员下单</p>'
//		           			    +   		'</div>'
//		            			+  			'<div class="weui_cell_ft">'
//		                   		+				'<input type="radio" name="payType_input_waiter" class="weui_check" id="waiterCheck_input_waiter" checked="checked" data-type="waiterCheck">'
//		                  		+				'<span class="weui_icon_checked"></span>'
//		            			+   		'</div>'
//		          				+		'</label>'
//		          				+		'<label class="weui_cell weui_check_label" for="fastOrder_input_waiter">'
//		             		  	+			'<div class="weui_cell_bd weui_cell_primary">'
//		                  		+				'<p>直接下单</p>'
//		                		+			'</div>'
//		                		+			'<div class="weui_cell_ft">'
//		                		+   			'<input type="radio" class="weui_check" name="payType_input_waiter" id="fastOrder_input_waiter" data-type="fastOrder">'
//		                    	+				'<span class="weui_icon_checked"></span>'
//		                		+			'</div>'
//		            			+		'</label>'		
//		       			 		+	'</div>') ,
//					leftText : '取消',
//					left : function(){
//						checkPayTypeDialog.close();
//					},
//					rightText : '确认',
//					right : function(){},
//					dismissible : true
//				});
//				checkPayTypeDialog.open();
			}
			
			
//			function commit(){
//			  var foods = "";
//			  var unitId = 0;
//			  orderFoodData.forEach(function(e, index){
//				 if(index > 0){
//					 foods += '&';
//				 }
//				 if(e.unitPriceId){
//					 unitId = e.unitPriceId;
//				 }else{
//					 unitId = 0;
//				 }
//				 foods += (e.id + ',' + e.count + ',' + unitId);
//			  });
//			   Util.lm.show();
//			  $.ajax({
//				url : '../../WxOperateOrder.do',
//				dataType : 'json',
//				type : 'post',
//				data : {
//					dataSource : 'insertOrder',
//					oid : Util.mp.oid,
//					fid : Util.mp.fid,
//					foods : foods,
//					comment : comment,
//					branchId : typeof Util.mp.extra != 'undefined' ? Util.mp.extra : ''
//				},
//				success : function(data, status, xhr){
//					if(data.success){
//						 Util.lm.hide();
//						var dialogOrder = new DialogPopup({
//							titleText : '温馨提示',
//							leftText : '自助扫码',
//							content : '<font style="font-weight:bold;font-size:25px;color:blue;">订单号: ' + data.other.code + '</font><br><font style="color:green;">1.您可呼叫服务员来确认订单</font><br><font style="color:green;">2.您可选择扫描桌上二维码下单</font>',
//							left : function(){	
//								wx.scanQRCode({ 
//								    needResult: 1, // 默认为0，扫描结果由微信处理，1则直接返回扫描结果，
//								    scanType: ["qrCode","barCode"], // 可以指定扫二维码还是一维码，默认二者都有
//								    success: function (res) {
//								    	if(res.resultStr.split('?').length == 1){
//								    		//url不带餐桌号就要输入台号
//								    		dialogOrder.close(function(){
//												var dialog = new DialogPopup({
//									    			titleText : '请输入台号',
//									    			leftText : '确认提交',
//								 	    			content : '<h3>请输入台号:<a data-type="numberInput_a_load" style="color:red;"></a></h3><br/>'
//										    					+'<div data-type="numberKyes_div_load" style="margin-top:-40px;"></div>',
//										    		contentCallback : function(dialogDiv){
//										    			dialogDiv.find('[data-type="numberKyes_div_load"]').width(dialogDiv.width());
//									    				var numKeys = new Array("7", "8", "9", "0", "4", "5", "6", "删除", "1", "2", "3", "清空");
//										    			var keys = "";
//										    			for(var i = 0; i < numKeys.length; i++){
//										    				var eachButton = '<input type="button" class="a_demo_two" style="width:60px;height:55px;font-size:18px;" value="' + numKeys[i] + '">';
//										    				if(i % 4 == 0){
//										    					keys += '<br/>';
//										    				}
//										    				keys += eachButton;
//										    			}	
//										    			dialogDiv.find('[data-type="numberKyes_div_load"]').append(keys);
//										    			dialogDiv.find('[data-type="numberKyes_div_load"] input').each(function(index, element){
//										    				element.onclick = function(){
//										    					if($(element).val() == '删除'){
//										    						var s = dialogDiv.find('[data-type="numberInput_a_load"]').text();
//										    						dialogDiv.find('[data-type="numberInput_a_load"]').text(s.substring(0, s.length - 1));
//										    					}else if($(element).val() == '清空'){
//										    						dialogDiv.find('[data-type="numberInput_a_load"]').text('');
//										    					}else{
//										    						dialogDiv.find('[data-type="numberInput_a_load"]').text(dialogDiv.find('[data-type="numberInput_a_load"]').text() + $(element).val());
//										    					}
//										    				}
//										    			});
//										    		},
//										    		left : function(dialogDiv){
//										    			if(dialogDiv.find('[data-type="numberInput_a_load"]').text() == ""){
//										    				Util.dialog.show({ msg : '餐桌号不能为空', btn :'yes'});
//										    			}else{
//										    				 Util.lm.show();
//															 $.ajax({
//																url : '../../WxOperateOrder.do',
//																dataType : 'json',
//																type : 'post',
//																data : {
//																	dataSource : 'self',
//																	oid : Util.mp.oid,
//																	fid : Util.mp.fid,
//																	wid : data.other.id,
//																	tableAlias : dialogDiv.find('[data-type="numberInput_a_load"]').text(),
//																	qrCode :  res.resultStr.split('?')[0],
//																	branchId : typeof Util.mp.extra != 'undefined' ? Util.mp.extra : ''
//																},
//																success : function(data, status, xhr){
//																	Util.lm.hide();
//																	if(data.success){
//																		dialog.close(function(){
//																				//刷新界面
//																			pickFoodComponent.refresh();
//																			pickFoodComponent.closeShopping();
//																			
//																			var dialogClose = new DialogPopup({
//																				content : '下单成功',
//																				titleText : '温馨提示',
//																				left : function(){
//																					dialogClose.close();
//																					window.location.reload();
//																					 $('#foodOrderList').click();
//																				}
//																			})
//																			dialogClose.open();
//																		}, 200);
//																	}else{
//																		Util.dialog.show({ msg : data.msg });
//																	}
//																},
//																error : function(xhr, errorType, error){
//																	Util.lm.hide();
//																	Util.dialog.show({ msg : '操作失败, 数据请求发生错误.' });
//																}
//															});
//										    			}
//										    		}
//									    		});
//									    		dialog.open(); 
//											}, 200);
//								    	}else{
//										 	 dialogOrder.close(function(){
//										 	 	 Util.lm.show();
//										 	 	 $.ajax({
//													url : '../../WxOperateOrder.do',
//													dataType : 'json',
//													type : 'post',
//													data : {
//														dataSource : 'self',
//														oid : Util.mp.oid,
//														fid : Util.mp.fid,
//														wid : data.other.id,
//														tableAlias : res.resultStr.split('?')[1],
//														qrCode :  res.resultStr.split('?')[0],
//														branchId : typeof Util.mp.extra != 'undefined' ? Util.mp.extra : ''
//													},
//													success : function(data, status, xhr){
//														Util.lm.hide();
//														if(data.success){
//															//刷新界面
//															pickFoodComponent.refresh();
//															pickFoodComponent.closeShopping();
//															
//															var dialogClose = new DialogPopup({
//																content : '下单成功',
//																titleText : '温馨提示',
//																left : function(){
//																	dialogClose.close();
//																	window.location.reload();
//																	 $('#foodOrderList').click();
//																}
//															})
//															dialogClose.open();
//														}else{
//															Util.dialog.show({ msg : data.msg });
//														}
//													},
//													error : function(xhr, errorType, error){
//														Util.lm.hide();
//														Util.dialog.show({ msg : '操作失败, 数据请求发生错误.' });
//													}
//												});
//										 	 }, 200);
//								    	}
//								    }
//								});
//							},
//							afterClose : function(){
//								dialogOrder.close();
//								window.location.reload();
//							 	$('#foodOrderList').click();
//							}
//						});
//						dialogOrder.open();	
//				 	}else{
//				 		Util.dialog.show({ msg : data.msg });
//				 	}
//				},
//				error : function(xhr, errorType, error){
//					Util.lm.hide();
//					Util.dialog.show({ msg : '操作失败, 数据请求发生错误.' });
//				}
//			  });
//			}
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
						content : '<span style="display:block;text-align:center;">下单成功</span>',
						leftText : '确认',
						left : function(){
							finishOrderDialog.close();								
						},
						afterClose : function(){
							
							//关闭后回调
							window.location.href = 'orderList.html?sessionId=' + Util.mp.params.sessionId;
						}
					});
					
					finishOrderDialog.open();
				} 
			});
		}
	}
	 
	  //打开购物车
	$('#shoppingCar_li_member').click(function(){
		pickFoodComponent.openShopping();
	});
	  
	  
	$('#foodOrderList').click(function(){
		Util.jump('orderList.html', typeof Util.mp.extra != 'undefined' ? Util.mp.extra : '');
	});
});
