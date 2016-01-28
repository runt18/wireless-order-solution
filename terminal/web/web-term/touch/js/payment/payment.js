//结账界面数据对象
var pm = {
	table : {},
	entry : function(c){
		if(!c || !c.table){
			Util.msg.alert({msg : '账单不存在', topTip: true});
			return;
		}
		
		pm.table = c.table;
		
		location.href = "#paymentMgr";
	}
};
 
$(function(){
	
	//当离开结账页面时
	$('#paymentMgr').on("pagehide", function(){ 
		document.getElementById("totalPrice").innerHTML = 0.00;
		document.getElementById("actualPrice_td_payment").innerHTML = 0.00;
		document.getElementById("forFree").innerHTML = 0.00;
		document.getElementById("spanCancelFoodAmount").innerHTML = 0.00;
		document.getElementById("discountPrice").innerHTML = 0.00;	
		orderMsg.actualPrice = 0;
		orderMsg = null;
		
		//取消绑定抹数的弹出键盘
		NumKeyBoardAttacher.instance().detach($('#erasePrice_input_payment')[0]);
	});
	
	//进入界面界面
	$('#paymentMgr').on('pagebeforeshow', function(){
		//清除快捷键
		$(document).off('keydown');
		//设置快捷键
		$(document).on('keydown', function(event){
			if(event.which == 109){
				//'-'表示暂结
				$('#tempPay_a_payment').click();
			}else if(event.which == 107){
				//'+'表示现金结账
				$('#cash_a_payment').click();
			}
		});
		//绑定抹数的弹出键盘
		NumKeyBoardAttacher.instance().attach($('#erasePrice_input_payment')[0], function(inputVal){
			$('#erasePrice_input_payment').keyup();
		});
		
		//加载餐厅设置数据
		loadSystemSettingData();
	
	 	//加载账单信息
		refreshOrderData();
		
		//加载折扣
		loadDiscountData();
		
		//加载服务费方案
		loadServicePlanData();
		
		//加载混合结账付款方式
		loadPayTypeData(); 
	});

	//加载显示账单基础信息
	var orderMsg;
	orderMsg = {};
	//加载账单数据
	function refreshOrderData(){
		//console.log(getcookie(document.domain + '_printers').split(','));
		Util.LM.show();
		$.ajax({
			url : "../QueryOrderByCalc.do",
			type : 'post',
			data : {
				tableID : pm.table.id,
				orderID : orderMsg ? orderMsg.id : '',
				customNum : pm.table.customNum,
				orientedDisplay : getcookie(document.domain + '_printers')				//显示客显
			},
			success : function(jr, status, xhr){
				Util.LM.hide();
				if(jr.success){
					// 加载显示账单基础信息
					orderMsg = jr.other.order;
					//显示账单信息
					loadOrderBasicMsg();
				}else{
					Util.msg.alert({
						msg : jr.msg,
						renderTo : 'paymentMgr'
					});
				}
			},
			error : function(request, status, err){
			}
		}); 	
		
	}

	//检查是否为小数
	function checkDot(num){
		if(!isNaN(num)){
			num = num + ""; 
		}
		var dot = num.indexOf(".");
		if(dot != -1){
		    var dotCnt = num.substring(dot + 1, dot + 2);
		    var dotCnt2 = num.substring(dot + 2);
		    if(dotCnt >= 1 || dotCnt2 >= 1){
		        return true;
		    }else{
		    	return false;
		    }
		}else{
			return false;
		}
	}
	
	//显示账单信息
	function loadOrderBasicMsg(){
		//显示左边价钱
		document.getElementById("totalPrice").innerHTML = checkDot(orderMsg.totalPrice)?parseFloat(orderMsg.totalPrice).toFixed(2) : orderMsg.totalPrice;
		document.getElementById("actualPrice_td_payment").innerHTML = checkDot(orderMsg.actualPrice)?parseFloat(orderMsg.actualPrice).toFixed(2) : orderMsg.actualPrice;
		document.getElementById("forFree").innerHTML = checkDot(orderMsg.giftPrice)?parseFloat(orderMsg.giftPrice).toFixed(2) : orderMsg.giftPrice;
		document.getElementById("spanCancelFoodAmount").innerHTML = checkDot(orderMsg.cancelPrice)?parseFloat(orderMsg.cancelPrice).toFixed(2) : orderMsg.cancelPrice;
		document.getElementById("discountPrice").innerHTML = checkDot(orderMsg.discountPrice)?parseFloat(orderMsg.discountPrice).toFixed(2) : orderMsg.discountPrice;
		if(orderMsg.categoryValue != 4 && orderMsg.cancelPrice > 0){
			$('#spanSeeCancelFoodAmount_label_tableSelect').show();	
			$('#lab_replaceCancelBtn').hide();
		}else{
			$('#lab_replaceCancelBtn').show();
			$('#spanSeeCancelFoodAmount_label_tableSelect').hide();
		}	
		
		if(orderMsg.giftPrice > 0){
			$('#spanSeeGiftFoodAmount_label_tableSelect').show();	
			$('#lab_replaceGiftBtn').hide();
		}else{
			$('#lab_replaceGiftBtn').show();
			$('#spanSeeGiftFoodAmount_label_tableSelect').hide();
		}	
		
		if(orderMsg.discountPrice > 0){
			$('#spanSeeDiscountFoodAmount_label_tableSelect').show();	
			$('#lab_replaceDiscountBtn').hide();
		}else{
			$('#lab_replaceDiscountBtn').show();
			$('#spanSeeDiscountFoodAmount_label_tableSelect').hide();
		}		
		
		//清空抹数和备注
		$('#erasePrice_input_payment').val('');
		$('#remark').val('');
		
		//账单基础信息
		$('#orderIdInfo').html('结账 -- 账单号:<font color="#f7c942">' + orderMsg.id + '</font> ' + (orderMsg.isWeixinOrder?'(<span id="showWeixinOrder" style="font-size:15px;font-weight:bold;color:green;text-decoration:underline">微信账单</span>)' : ''));
		if(orderMsg.category != 4){
			$('#orderTableInfo').html('餐桌号:<font color="#f7c942">' + orderMsg.table.alias + '</font>&nbsp;' + (pm.table.name?'<font color="#f7c942" >(' + pm.table.name +')</font>' :''));
		}
		document.getElementById('spanDisplayCurrentServiceRate').innerHTML = (orderMsg.serviceRate*100)+'%';
		$('#orderCustomNum').html(orderMsg.customNum > 0 ? orderMsg.customNum : 1);
		$('#remark').val(orderMsg.comment && orderMsg.comment != '----' ? orderMsg.comment : '');
		
		//会员 & 折扣 & 优惠劵
		var discountDesc = '当前折扣:<font style="color:green;font-weight:bold;">'+ orderMsg.discount.name + '</font>';
		if(orderMsg.discounter){
			discountDesc += ', 折扣人:<font style="color:green;font-weight:bold;">'+ orderMsg.discounter + '</font>';
			discountDesc += ', 折扣时间:<font style="color:green;font-weight:bold;">'+ orderMsg.discountDate + '</font>';
		}
		$('#orderDiscountDesc').html(discountDesc);
		
		if(orderMsg.usedCoupons.length > 0){
			$('#orderCouponInfo').html('使用优惠券:<font style="color:green;font-weight:bold;">'+ orderMsg.usedCoupons.length + '张, 共¥' + orderMsg.couponPrice + '</font>');
		}else{
			$('#orderCouponInfo').html('');
		}
		
		orderMsg.member = null;
		if(orderMsg.memberId && orderMsg.memberId > 0){
			//设置会员结账按钮
			$('#memberBalance_a_payment .ui-btn-text').html('会员余额');
			$('#memberBalance_a_payment').buttonMarkup('refresh');
			
			//显示结账发券和用券按钮
			$('#issueCoupon_a_orderFood').show();
			$('#useCoupon_a_orderFood').show();
			
			$.post('../QueryMember.do', {dataSource : 'normal', id : orderMsg.memberId, forDetail : true}, function(result){
				if(result.success){
					
					orderMsg.member = result.root[0];
					
					var memberSpan = "";
					if(result.root[0].isRaw){
						memberSpan = '<span style = "margin-left: 20px;">当前会员：<font style="text-decoration: underline;cursor: pointer;color:blue" id="memberBind_span_payment">' + result.root[0].name +"(点击绑定)</font></span>";
					}else{
						memberSpan = '<span style = "margin-left: 20px;">当前会员：<font style="color:green">' + result.root[0].name +"</font></span>";
					}
					
					$('#memberInfo_span_payment').html(memberSpan);
					
					//会员绑定
					$('#memberBind_span_payment').click(function(){
						var perfectMemberPopup = new PerfectMemberPopup({
							selectedMember : orderMsg.memberId,
							selectedOrder : orderMsg.id,
							memberName : orderMsg.member.name,
							postBound : function(){
								refreshOrderData();
							}
						});
						perfectMemberPopup.open();
					});
				}
			}, 'json');
		}else{
			$('#memberInfo_span_payment').html('');
			//设置会员结账按钮
			$('#memberBalance_a_payment .ui-btn-text').html('读取会员');
			$('#memberBalance_a_payment').buttonMarkup('refresh');	
			
			//隐藏结账发券和用券按钮
			$('#issueCoupon_a_orderFood').hide();
			$('#useCoupon_a_orderFood').hide();
		}
		
		//微信账单
		$('#showWeixinOrder').hover(function(){
			if(!weixinOrderDetailWin.loadOrder){
				loadWeixinOrderDetail();
				weixinOrderDetailWin.loadOrder = true;
			}
			weixinOrderDetailWin.setPosition($('#showWeixinOrder').position().left + 475, $('#showWeixinOrder').position().top + 60);
			weixinOrderDetailWin.show();		
			
		}, function(){
			weixinOrderDetailWin.hide();
		});		
		
		//菜品列表
		var html = [];
		for(var i = 0; i < orderMsg.orderFoods.length; i++){
			//菜品列表
			var orderFoodTemplate = '<tr class="{isComboFoodTd}">'
				+ '<td>{dataIndex}</td>'
				+ '<td ><div class={foodNameStyle}>{name}</div></td>'
				+ '<td>{count}<img style="margin-top: 10px;margin-left: 5px;display:{isWeight}" src="images/weight.png"></td>'
				+ '<td><div style="height: 45px;overflow: hidden;">{tastePref}</div></td>'
				+ '<td>{tastePrice}</td>'
				+ '<td>{unitPrice}</td>'
				+ '<td>{discount}</td>'
				+ '<td>{totalPrice}</td>'
				+ '<td>{orderDateFormat}</td>'
				+ '<td>{waiter}</td>'
				+ '</tr>';
				
				var count = null;
				if((orderMsg.orderFoods[i].status & 1 << 7) != 0){
					if(orderMsg.orderFoods[i].count > 1){
						 count = orderMsg.orderFoods[i].count;
					}else{
						 count = '<font color="red" style="font-size:25px;">' + orderMsg.orderFoods[i].count + '</font>';
					}
				}else{
					 count = orderMsg.orderFoods[i].count;
				}
				
			html.push(orderFoodTemplate.format({
				dataIndex : i + 1,
				id : orderMsg.orderFoods[i].id,
				name :  (orderMsg.orderFoods[i].status & 1 << 7) != 0 ? orderMsg.orderFoods[i].foodName + '<font color="red" style="font-size:25px;">[称重确认]</font>' : orderMsg.orderFoods[i].foodName,
				count : count,
				isWeight : (orderMsg.orderFoods[i].status & 1 << 7) != 0 ? 'initial' : 'none',
				tastePref : orderMsg.orderFoods[i].tasteGroup.tastePref,
				tastePrice : orderMsg.orderFoods[i].tasteGroup.tastePrice,
				unitPrice : (orderMsg.orderFoods[i].unitPrice + orderMsg.orderFoods[i].tasteGroup.tastePrice).toFixed(2),
				discount : orderMsg.orderFoods[i].discount,
				totalPrice : orderMsg.orderFoods[i].totalPrice.toFixed(2),
				orderDateFormat : orderMsg.orderFoods[i].orderDateFormat.substring(11),
				waiter : orderMsg.orderFoods[i].waiter,
				isComboFoodTd : "",
				foodNameStyle : "commonFoodName"
			}));
			
			if((orderMsg.orderFoods[i].status & 1 << 5) != 0){
				var combo = orderMsg.orderFoods[i].combo;
				
				for (var j = 0; j < combo.length; j++) {
					html.push(orderFoodTemplate.format({
						dataIndex : '',
						id : combo[j].comboFood.id,
						name : '┕' + combo[j].comboFoodDesc,
						count : combo[j].comboFood.amount,
						isWeight : (combo[j].comboFood.status & 1 << 7) != 0 ? 'initial' : 'none',
						tastePref : combo[j].tasteGroup ? combo[j].tasteGroup.tastePref : "无口味",
						tastePrice : combo[j].tasteGroup ? combo[j].tasteGroup.tastePrice : 0,
						unitPrice : "",
						discount : "",
						totalPrice : "",
						orderDateFormat : "",
						waiter : "",
						isComboFoodTd : "comboFoodTd",
						foodNameStyle : "comboFoodName"
					}));					
				}
			}
			
		}			
		
		$('#payment_orderFoodListBody').html(html.join("")).trigger('create');	
	}
	
	//加载折扣方案信息
	function loadDiscountData(){
		$.ajax({
			url : "../QueryDiscount.do",
			type : 'post',
			data : {
				dataSource : 'role'
			},
			success : function(jr, status, xhr){
				if(jr.success){
					var html = '';
					for (var i = 0; i < jr.root.length; i++) {
						html += '<li data-icon="false" class="tempFoodKitchen" discount-id="' + jr.root[i].id + '"><a>'+ jr.root[i].name +'</a></li>';
					}
					$('#discount_ul_payment').html(html).trigger('create').listview('refresh');
					//每个折扣的处理事件
					$('#discount_ul_payment').find('.tempFoodKitchen').each(function(index, element){
						element.onclick = function(){
							//关闭折扣选择
							$('#discount_div_payment').popup('close');
							Util.LM.show();
							//设置折扣
							$.ajax({
								url : "../OperateDiscount.do",
								type : 'post',
								data : {
									dataSource : 'setDiscount',
									orderId : orderMsg.id, 
									discountId : $(element).attr('discount-id') 
								},
								dataType : 'json',
								success : function(jr, status, xhr){
									Util.LM.hide();
									if(jr.success){
										refreshOrderData();
										Util.msg.tip('设置折扣成功');
									}else{
										Util.msg.alert({
											msg : jr.msg,
											renderTo : 'paymentMgr'
										});
									}
								},
								error : function(request, status, err){
								}
							});
						};
					});
				}
			},
			error : function(request, status, err){
			}
		});	
	}
	
	//付款方式
	var payTypeData = null;
	//加载付款方式
	function loadPayTypeData(){
		$.ajax({
			url : "../QueryPayType.do",
			type : 'post',
			data : {
				dataSource : 'exceptMember'
			},
			success : function(jr, status, xhr){
				if(jr.success){
					payTypeData = jr.root;
				}
			},
			error : function(request, status, err){
			}
		}); 	
	}
	
	//加载服务费方案
	function loadServicePlanData(){
		$.ajax({
			url : "../QueryServicePlan.do",
			type : 'post',
			data : {
				dataSource : 'getByCond'
			},
			dataType : 'json',
			success : function(jr, status, xhr){
				var html = '';
				for (var i = 0; i < jr.root.length; i++) {
					html += '<li data-icon="false" class="tempFoodKitchen" plan-id="' + jr.root[i].id + '"><a>'+ jr.root[i].name +'</a></li>';
				}
				$('#servicePlan_ul_payment').html(html).trigger('create').listview('refresh');
				$('#servicePlan_ul_payment').find('.tempFoodKitchen').each(function(index, element){
					element.onclick = function(){
						//关闭服务费方案
						$('#servicePlan_div_payment').popup('close');
						//设置服务费方案
						$.ajax({
							url : "../OperateOrderFood.do",
							type : 'post',
							data : {
								dataSource: 'service',
								orderId : orderMsg ? orderMsg.id : '',
								planId : $(element).attr('plan-id')
							},
							success : function(jr, status, xhr){
								Util.LM.hide();
								if(jr.success){
									//刷新页面
									refreshOrderData();
									Util.msg.tip('服务费设置成功');
								}else{
									Util.msg.alert({
										msg : jr.msg,
										renderTo : 'paymentMgr'
									});
								}
							},
							error : function(request, status, err){
							}
						}); 
						console.log($(element).attr('plan-id'));
					};
				});
			},
			error : function(request, status, err){
				alert(err);
			}
		}); 	
	}
	
	//餐厅设置参数
	var restaurantData = null;

 	//加载餐厅设置参数
	function loadSystemSettingData(){
		$.ajax({
			url : "../QuerySystemSetting.do",
			type : 'post',
			success : function(jr, status, xhr){
				if(jr.success){
					restaurantData = jr.other.systemSetting;
					if(restaurantData.setting.eraseQuota > 0){
						$('#eraseQuota_tr_payment').show();
						$('#eraseQuota_font_payment').html(restaurantData.setting.eraseQuota);
					}else{
						$('#eraseQuota_tr_payment').hide();
						$('#eraseQuota_font_payment').html('');
					}
				}
			},
			error : function(request, status, err){
			}
		}); 
		
	}
	
	//页面初始化
	$('#paymentMgr').on("pageinit", function(){ 
		//微信支付Button
		$('#wx_li_payment').click(function(){
			$('#wxPayPopup_div_tableSelect').popup('close');
			 paySubmit({
			  	submitType : PayTypeEnum.WX,
			  	postPayment : function(resultJSON){
			  		if(resultJSON.success){
			  			Util.msg.alert({msg : '微信支付二维码打印成功', topTip : true});
			  		}else{
			  			Util.msg.alert({
							msg : '对不起，您还没开通微信店铺的支付功能' + '</br>错误信息：' + resultJSON.data,
							renderTo : 'paymentMgr'
						});
			  		}
			  	}
			 });
		});
		
		
		//扫描枪支付
		$('#authCode_li_payment').click(function(){
			$('#wxPayPopup_div_tableSelect').popup('close');
			setTimeout(function(){
				var wxPaymentPopup = new CreateWxPaymentPopup({
	  				pay : function(inputValue){
		  				paySubmit({
						  	submitType : PayTypeEnum.WX,
						  	authCode :　inputValue,
						  	postPayment : function(resultJSON){
					  			if(resultJSON.success){
					  				var elapsed = 0;   //逝去的时间
					  				
					  				//扫描的处理方法
					  				(function(){
					  					//指向自身
					  					var thiz = arguments.callee;
					  					Util.LM.show();
										$.ajax({
											url : "../QueryOrderByCalc.do",
											type : 'post',
											data : {
												orderID : orderMsg.id
											},
											success : function(jr, status, xhr){
												
												if(jr.success && jr.other.order.statusValue == '1'){
													//账单支付成功 && 账单已结账的情况下的处理
													Util.LM.hide();
									  				Util.msg.tip('扫描支付成功');
									  				wxPaymentPopup.close(function(){
									  					ts.loadData();
									  				}, 200);
												}else if(elapsed >= 10){//支付超时的处理
													//弹出继续等待的popup
													Util.LM.hide();
													wxPaymentPopup.close(function(){
														var wxPayTip = new CreateWxPaymentPopup({
						  									title : '温馨提示',
						  									leftText : '继续等待',
						  									content : '支付超时,请继续选择下面操作',
						  									rightText : '取消支付',
						  									pay : function(){
						  										elapsed = 0;
						  										wxPayTip.close(thiz);
						  									},
						  									right : function(){
						  										//取消支付
						  										$.ajax({
						  											url : '../BeeCloud.do',
						  											type : 'post',
						  											data : {
						  												dataSource : 'cancel',
						  												channel : 'wx_scan',
						  												billNo : resultJSON.billNo
						  											},
						  											success : function(jr){
						  												if(jr.success){
						  													Util.msg.tip(jr.msg);
						  												}else{
						  													Util.msg.tip(jr.msg);
						  												}
						  											}
						  										})
						  									}
														});
														wxPayTip.open();
													}, 200);
												}else{
													//延迟两秒执行自身
													setTimeout(thiz, 2000);
													elapsed += 2;
												}
											}
										});
					  				})();
					  			}else{
						  			Util.msg.tip('对不起，支付失败' + '</br>错误信息：' + resultJSON.data);
					  			}
					  		}
					    });
	  				}
	  			});
	  			wxPaymentPopup.open();
			}, 300);
		});
		
		function postPayment(resultJSON){
			if(resultJSON.success){
				Util.msg.alert({msg : '结账成功!', topTip : true});
				if(systemStatus == 4){
					//快餐模式下返回到点菜界面
					of.entry({orderFoodOperateType : 'fast', table : pm.table});
				}else{
					//返回餐台界面
					ts.loadData();
				}
			}else{
				Util.msg.alert({
					msg : resultJSON.data,
					renderTo : 'paymentMgr'
				});
			}
		}
		
		//现金结账
		$('#cash_a_payment').click(function(){
			paySubmit({
				submitType : PayTypeEnum.CASH,
				postPayment : postPayment
			});
		});	
		
		//刷卡结账
		$('#credit_a_payment').click(function(){
			paySubmit({
				submitType : PayTypeEnum.CREDIT_CARD,
				postPayment : postPayment
			});
		});
		
		//签单结账
		$('#sign_a_payment').click(function(){
			paySubmit({
				submitType : PayTypeEnum.SIGN,
				postPayment : postPayment
			});
		});
		
		//挂账结账
		$('#hang_a_payment').click(function(){
			paySubmit({
				submitType : PayTypeEnum.HANG,
				postPayment : postPayment
			});
		});
		
		//会员余额结账
		$('#memberPay_a_payment').click(function(){
			paySubmit({
				submitType : PayTypeEnum.MEMBER,
				postPayment : function(resultJSON){
					if(resultJSON.success){
						Util.msg.alert({msg : '结账成功!', topTip : true});
						$('#memberPayCancel_a_payment').click();
						setTimeout(function(){
							if(systemStatus == 4){
								//快餐模式下返回到点菜界面
								of.entry({orderFoodOperateType : 'fast', table : pm.table});
							}else{
								//返回餐台界面
								ts.loadData();
							}
						}, 250);
					}else{
						Util.msg.alert({
							msg : resultJSON.data,
							renderTo : 'paymentMgr'
						});
					}
				}
			});
		});
		
		//会员结账-取消
		$('#memberPayCancel_a_payment').click(function(){
			$('#showMemberInfoWin').popup('close');
		});
		
		//暂结
		$('#tempPay_a_payment').click(function(){
			paySubmit({
				submitType : PayTypeEnum.CASH,
				temp : true,
				postPayment : function(resultJSON){
					if(resultJSON.success){
						Util.msg.tip(resultJSON.data);
					}else{
						Util.msg.tip(resultJSON.data);
					}
				}
			});
		});
		
		//现金找零
		$('#cashReceive_a_payment').click(function(){
			$('#cashReceive_div_payment').popup('open');
		});
		
		//现金输入框在输入数字后实时显示找零
		$('#cashReceive_input_payment').on('keyup', function(){
			//计算抹零
			var eraseQuota = $('#erasePrice_input_payment').val();
			var actualPrice = orderMsg.actualPrice;
			if(!isNaN(eraseQuota)){
				actualPrice = orderMsg.actualPrice - eraseQuota;
			}
			
			if($('#cashReceive_input_payment').val() - actualPrice > 0){
				$('#cashBack_label_payment').text($('#cashReceive_input_payment').val() - actualPrice);
			}else{
				$('#cashBack_label_payment').text(0);
			}
		});
			
		//进入现金找零Popup的函数
		$('#cashReceive_div_payment').on('popupafteropen', function(event, ui){
			//计算抹零
			var eraseQuota = $('#erasePrice_input_payment').val();
			var actualPrice = orderMsg.actualPrice;
			if(!isNaN(eraseQuota)){
				actualPrice = orderMsg.actualPrice - eraseQuota;
			}
			$('#cashReceive_input_payment').val('');
			$('#cashBack_label_payment').text(0);
			$('#consume4CashReceive_a_payment').text(actualPrice);
			setTimeout(function(){
				$('#cashReceive_input_payment').focus();
			}, 200);
			//绑定现金收入的输入框
			NumKeyBoardAttacher.instance().attach($('#cashReceive_input_payment')[0], function(inputVal){
				$('#cashReceive_input_payment').keyup();
			});
		});
		
		//退出现金找零Popup的函数
		$('#cashReceive_div_payment').on('popupafterclose', function(event, ui){
			NumKeyBoardAttacher.instance().detach($('#cashReceive_input_payment')[0]);
		});
		
		//现金找零-取消
		$('#receivedCashCancel_a_payment').click(function(){
			$('#cashReceive_div_payment').popup('close');
		});
		
		//现金找零-确定
		$('#receivedCashConfirm_a_payment').click(function(){
			var input = $('#cashReceive_input_payment');
			if(input.val() && !isNaN(input.val())){
				paySubmit({
					submitType : PayTypeEnum.CASH,
					cashIncome : parseInt(input.val()),
					postPayment : function(resultJSON){
						if(resultJSON.success){
							Util.msg.alert({msg : '结账成功!', topTip : true});
							//关闭现金找零界面
							$('#receivedCashCancel_a_payment').click();
							//等完全关闭后再返回
							setTimeout(function(){
								if(systemStatus == 4){
									//快餐模式下返回到点菜界面
									of.entry({orderFoodOperateType : 'fast', table : pm.table});
								}else{
									//返回餐台界面
									ts.loadData();
								}
							}, 250);
	
						}else{
							Util.msg.alert({
								msg : resultJSON.data,
								renderTo : 'paymentMgr'
							});
						}
					}
				});
			}else{
				Util.msg.alert({msg:'请输入正确的结账金额', topTip : true});
				input.focus();
			}
		});
	
		//混合结账
		$('#mixed_a_payment').click(function(){
			var mixPayPopup = new createMixPayPopup({
				left : function(mixedIncome){
					paySubmit({
						submitType : PayTypeEnum.MIXED,
						temp : true,
						mixedIncome : mixedIncome,
						postPayment : function(resultJSON){
							
							if(resultJSON.success){
									Util.msg.tip(resultJSON.data);
							}else{
								Util.msg.tip(resultJSON.data);
							}
						}
					});
				},
				middle : function(mixedIncome){
					paySubmit({
						submitType : PayTypeEnum.MIXED,
						temp : false,
						mixedIncome : mixedIncome,
						postPayment : function(resultJSON){
							
							if(resultJSON.success){
									//关闭混合结账界面
									mixPayPopup.close();
									Util.msg.tip('结账成功!');
									//等完全关闭后再返回
									setTimeout(function(){
										if(systemStatus == 4){
											//快餐模式下返回到点菜界面
											of.entry({orderFoodOperateType : 'fast', table : pm.table});
										}else{
											//返回餐台界面
											ts.loadData();
										}
									}, 250);
							}else{
								Util.msg.tip(resultJSON.data);
							}
						}
					});
				},
				orderMessage : orderMsg
			});
			
			mixPayPopup.open();
		});
		
		//抹数联动
		$('#erasePrice_input_payment').on('keyup', function(){
			var eraseQuota = $('#erasePrice_input_payment').val();
			if(eraseQuota && isNaN(eraseQuota)){
				Util.msg.alert({msg:"请填写正确的抹数金额", topTip:true ,fn:function(){$("#erasePrice_input_payment").focus();$("#erasePrice_input_payment").select();}});
				return;
			}else if(!isNaN(eraseQuota) && eraseQuota > restaurantData.setting.eraseQuota){// 抹数金额
				Util.msg.alert({msg:"抹数金额大于设置上限，不能结帐!", topTip:true,fn:function(){$("#erasePrice_input_payment").focus();$("#erasePrice_input_payment").select();}});
				return;
			}			
			$('#actualPrice_td_payment').html((orderMsg.actualPrice * 10000 - eraseQuota * 10000)/10000);
			
		});
	
		//改单
		$('#updateOrder_a_payment').click(function(){
			uo.entry({
				table : pm.table
			});	
		});
		
		
		//打开发送优惠券
		$('#issueCoupon_a_orderFood').click(function(){
			//初始化发送优惠券
			var issueCoupon = new IssueCouponPopup({
				title : '发送优惠券',
				memberName : orderMsg.member.name, 
				issueMode : IssueCouponPopup.IssueMode.ORDER,
				orderId : orderMsg.id,
				issueTo : orderMsg.memberId
			});
			issueCoupon.open();
		});
		
		//打开用券
		$('#useCoupon_a_orderFood').click(function(){
			var useCouponPopup = new UseCouponPopup({
				title : '使用优惠券',
				useTo : orderMsg.memberId,
				orderId :  orderMsg.id,
				memberName : orderMsg.member.name,
				useCuoponMethod : function(coupons){
					$.post('../OperateOrderFood.do', {dataSource : 'coupon', orderId : orderMsg.id, coupons : coupons.join(',')}, function(response, status, xhr){
						if(response.success){
							Util.msg.tip('使用成功!');
							useCouponPopup.close();
							refreshOrderData();
						}else{
							Util.msg.tip(response.msg);
						}
					}, 'json');
					
				}
			});
			useCouponPopup.open();
		});
		
		//会员
		$('#memberRead_a_payment').click(function(){
			var memberReadPopup = null;
			memberReadPopup = new MemberReadPopup({
				confirm : function(member, discount, pricePlan){
					Util.LM.show();
					
					$.post('../OperateDiscount.do', {
						dataSource : 'setDiscount',
						orderId : orderMsg.id,
						memberId : member.id,
						discountId : discount.id,
						pricePlan : pricePlan.id
						
					}, function(data){
						Util.LM.hide();
						if(data.success){
							
							//刷新账单
							refreshOrderData();
							
							Util.msg.alert({topTip : true, msg : '会员注入成功'});	
							
							//关闭会员读取Popup
							memberReadPopup.close();
							
						}else{
							Util.msg.alert({
								msg : '使用会员失败</br>' + data.msg, 
								topTip : true
							});					
						}
					}, 'json');		
				}
			});
			//打开会员读取Popup
			memberReadPopup.open();
		});
		
		//读取会员&会员余额
		$('#memberBalance_a_payment').click(function(){
			
			if(orderMsg.memberId == 0){
				//会员未注入时打开读取会员
				$('#memberRead_a_payment').click();
				
			}else{
				//会员已注入时使用会员余额结账
				if(getcookie(document.domain+'_consumeSms') == "true"){
					$('#memberPaymentSendSMS').attr('checked', true);
				}else{
					$('#memberPaymentSendSMS').attr('checked', false);
				}
				
				$('#memberPaymentSendSMS').checkboxradio('refresh');
			
				
				$('#payment4MemberCertainName').text(orderMsg.member.name);
				$('#payment4MemberCertainType').text(orderMsg.member.memberType.name);
				$('#payment4MemberCertainBalance').text(orderMsg.member.totalBalance);
				$('#payment4MemberCertainPoint').text(orderMsg.member.point);	
				$('#payment4MemberCertainPhone').text(orderMsg.member.mobile ? orderMsg.member.mobile : '----');
				$('#payment4MemberCertainCard').text(orderMsg.member.memberCard ? orderMsg.member.memberCard : '----');	
				
				$('#showMemberInfoWin').popup('open');
			}
		});
		
		
		//退菜明细
		$('#spanSeeCancelFoodAmount_label_tableSelect').click(function(){
			var cancelDetail = null;
			cancelDetail = new DetailPopup({
				table : pm.table,
				order :  orderMsg,
				detailType : DetailPopup.DetailType.CANCEL.val
			});
			cancelDetail.open();
		});
		
		//折扣明细
		$('#spanSeeDiscountFoodAmount_label_tableSelect').click(function(){
			var discountDetail = null;
			discountDetail = new DetailPopup({
				table : pm.table,
				order :  orderMsg,
				detailType : DetailPopup.DetailType.DISCOUNT.val
			});
			discountDetail.open();
		});
		
		//赠送明细
		$('#spanSeeGiftFoodAmount_label_tableSelect').click(function(){
			var giftDetail = null;
			giftDetail = new DetailPopup({
				table : pm.table,
				order :  orderMsg,
				detailType : DetailPopup.DetailType.GIFT.val
			});
			giftDetail.open();
		});
		
		
	});
	
	var SettleTypeEnum = {
		NORMAL : { val : 1, desc : '普通结账' },
		MEMBER : { val : 2, desc : '会员结账' }
	};

	var PayTypeEnum = {
		CASH : { val : 1, desc : '现金' },
		CREDIT_CARD : { val : 2, desc : '刷卡' },
		MEMBER : { val : 3, desc : '会员' },
		SIGN : { val : 4, desc : '签单'},
		HANG : { val : 5, desc : '挂账'},
		WX : { val : 6, desc : '微信支付'},
		MIXED : { val : 100, desc : '混合'}
	};
	
	//付款状态
	var isPaying = false;
	//结账提交
	function paySubmit(c) {
	
		c = c || {
			submitType : PayTypeEnum.CASH,		//结账类型
			postPayment : null,					//结账处理函数
			authCode : null,                   //扫描二维码的值  
			temp : temp,						//是否暂结
			mixedIncome : mixedIncome			//混合结账数据
		};
		
		if(isPaying == true){ 
			return; 
		}
		
		if(c.temp == undefined){
			c.temp = false;
		}
		
		if(orderMsg == null){
			Util.msg.tip("读取账单有误, 不能结账");
			return;
		}
		
		var eraseQuota = $("#erasePrice_input_payment").val();
		if(eraseQuota && isNaN(eraseQuota)){
			Util.msg.alert({msg:"请填写正确的抹数金额", renderTo:'paymentMgr',fn:function(){$("#erasePrice_input_payment").focus();$("#erasePrice_input_payment").select();}});
			return;
		}else if(!isNaN(eraseQuota) && eraseQuota > restaurantData.setting.eraseQuota){// 抹数金额
	//		setFormButtonStatus(false);
			Util.msg.alert({msg:"抹数金额大于设置上限，不能结帐!", renderTo:'paymentMgr',fn:function(){$("#erasePrice_input_payment").focus();$("#erasePrice_input_payment").select();}});
			return;
		}	
	
		//普通或会员结账, 会员已注入则为会员结账, 否则为普通该结账
		var settleType;
		if(orderMsg.memberId > 0){
			settleType = SettleTypeEnum.MEMBER.val;
		}else{
			settleType = SettleTypeEnum.NORMAL.val;
		}
		
		//是否发送短信
		var sendSms = false;
		if(c.submitType == PayTypeEnum.MEMBER){
			//会员结账
			//FIXME 要加上抹数?
			if(orderMsg.member.totalBalance < orderMsg.actualPrice){
				Util.msg.tip('会员卡余额小于合计金额，不能结帐!');
				return;			
			}			
			
			//保存发送短信 
			if($('#memberPaymentSendSMS').attr('checked')){
				sendSms = true;
				setcookie(document.domain + '_consumeSms', true);
			}else{
				sendSms = false;
				setcookie(document.domain + '_consumeSms', false);
			}
			
		}
	
		Util.LM.show();
		
		isPaying = true;
		$.ajax({
			url : "../PayOrder.do",
			type : 'post',
			data : {
				orderID : orderMsg.id,
				cashIncome : c.cashIncome ? c.cashIncome : 0,
				payType : settleType,
				payManner : c.submitType.val,
				tempPay : c.temp,
				comment : $("#remark").val(),
				eraseQuota : eraseQuota == '' ? 0 : eraseQuota,
				customNum : orderMsg.customNum,
				payTypeCash : c.mixedIncome ? c.mixedIncome : '',
				sendSms : sendSms,
				orientedPrinter : getcookie(document.domain + '_printers'),			//特定打印机打印
				authCode : c.authCode ? c.authCode : null             //扫描二维码的值
			},
			dataType : 'json',
			success : function(resultJSON, status, xhr){
				Util.LM.hide();
				isPaying = false;
				
				if(c.postPayment){
					c.postPayment(resultJSON);
				}
			},
			error : function(request, status, err){
				Util.LM.hide();
				isPaying = false;
				Util.msg.tip("结账出错, 请刷新页面后重试");
			}
		}); 		
	};
});

