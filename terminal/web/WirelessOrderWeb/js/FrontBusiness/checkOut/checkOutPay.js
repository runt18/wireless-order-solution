/**
 * 
 */
var paySubmit = function(submitType) {
	if(isPaying == true){ return; }
	// 强制计算后再结账
//	refreshCheckOutData({
//		callback : function(){
			if(!checkOutListRefresh()){
				return;
			}
			setFormButtonStatus(true);
			var canSubmit = true;
			var forFree = document.getElementById("forFree").innerHTML;
			var change;
			if(inputReciptWin){
				change = Ext.getCmp('txtReciptReturn').getValue();
			}else{
				change = 0;
			}
			var cancelledFoodAmount = document.getElementById("spanCancelFoodAmount").innerHTML;
			var actualPrice = checkOut_actualPrice;
			var countPrice = document.getElementById("totalCount").innerHTML;
			var shouldPay = document.getElementById("shouldPay").innerHTML;
			var servicePlan = Ext.getCmp("comboServicePlan").getValue();
			var serviceRate = 0;
			var eraseQuota = document.getElementById("txtEraseQuota").value;
			var submitPrice = -1;
			
			var payManner = -1;
			var tempPay;

			// 现金
			if (submitType == 1) {
				submitPrice = actualPrice;
			} else {
				
			}
			// 暂结，调整参数
			if (submitType == 6) {
				tempPay = "true";
				payManner = 1;
			} else {
				tempPay = "";
				payManner = submitType;
			}
			// 服务费率
			if (serviceRate < 0 || serviceRate > 100) {
				setFormButtonStatus(false);
				Ext.Msg.alert("提示", "<b>服务费率范围是0%至100%！</b>");
				canSubmit = false;
			}
			// 抹数金额
			if(!isNaN(eraseQuota) && eraseQuota >=0 && eraseQuota > restaurantData.setting.eraseQuota){
				setFormButtonStatus(false);
				Ext.Msg.alert("提示", "<b>抹数金额大于设置上限，不能结帐！</b>");
				canSubmit = false;
			}
			// 会员卡结帐，检查余额；现金校验
			if (submitType == 3 && parseFloat(countPrice) > parseFloat(mBalance) && payType == 2) {
				setFormButtonStatus(false);
				Ext.Msg.alert("提示", "<b>会员卡余额小于合计金额，不能结帐！</b>");
				canSubmit = false;
			} else if (submitType == 1 && parseFloat(actualPrice + eraseQuota) < parseFloat(shouldPay)) {
				setFormButtonStatus(false);
				Ext.Msg.alert("提示", "<b>实缴金额小于应收金额，不能结帐！</b>");
				canSubmit = false;
			}
			
			if (!canSubmit) {
				return false;
			}
			isPaying = true;
			Ext.Ajax.request({
				url : "../../PayOrder.do",
				params : {
					"orderID" : orderMsg.id,
					"cashIncome" : submitPrice,
					"payType" : payType,
					'discountID' : calcDiscountID,
					"payManner" : payManner,
					"tempPay" : tempPay,
					"memberID" : actualMemberID,
					"comment" : Ext.getDom("remark").value,
					"servicePlan" : servicePlan,
					'eraseQuota' : eraseQuota == ''?0:eraseQuota
//					'pricePlanID' : calcPricePlanID,
//					'customNum' : Ext.getCmp('numCustomNum').getValue()
				},
				success : function(response, options) {
					isPaying = false;
					var resultJSON = Ext.decode(response.responseText);
					var dataInfo = resultJSON.data;
					
					if (resultJSON.success == true) {
						var interval = 5;
						var action = '';
						
						if (submitType == 6) {
							Ext.example.msg('提示', dataInfo);
							setFormButtonStatus(false);
						}else{
//							console.log('showwin');
							action = '&nbsp;<span id="returnInterval" style="color:red;"></span>&nbsp; 秒之后自动跳转.';
							new Ext.Window({
								title : '<center>结账信息</center>',
								width : 700,
								modal : true,
								closable : false,
								resizable : false,
								layout : 'column',
								defaults : {
									xtype : 'label',
									columnWidth : .25,
									style : 'font-size:15px;font-weight:bold;line-height:36px;padding-left:20px;'
								},
								items : [{
									html : '应收：￥<font color="red">'+countPrice+'</font>'
								}, {
									html : '实收：￥<font color="red">'+eval(countPrice-eraseQuota)+'</font>'
								}, {
									html : '收款：￥<font color="red">'+actualPrice+'</font>'
								}, {
									html : '找零：￥<font color="red">'+change+'</font>'
								}, {
									html : '抹数金额：￥<font color="red">'+eraseQuota+'</font>'
								}, {
									html : '退菜金额：￥<font color="red">'+cancelledFoodAmount+'</font>'
								}, {
									html : '赠送：￥<font color="red">'+forFree+'</font>'
								}, {
									html : '服务费：￥<font color="red">'+serviceRate+'</font>'
								}, {
									columnWidth : 1,
									style : 'font-size:22px;line-height:40px;text-align:center;',
									html : (dataInfo + '.' + action)
								}],
								buttonAlign : 'center',
								buttons : [{
									text : '确&nbsp;&nbsp;定',
									handler : function(e){
										if (submitType != 6) {
											location.href = "TableSelect.html";
										}
									},
									listeners : {
										render : function(e){
											e.getEl().setWidth(200, true);
										}
									}
								}],
								listeners : {
									show : function(){
										new Ext.util.TaskRunner().start({
											run: function(){
												if(interval < 1){
													location.href = "TableSelect.html";
												}
												Ext.getDom('returnInterval').innerHTML = interval;
												interval--;
										    },
										    interval : 1000
										});
									}
								}
							}).show(document.body);						
						}
					} else {
						var dataInfo = resultJSON.data;
						Ext.MessageBox.show({
							msg : dataInfo,
							width : 300,
							buttons : Ext.MessageBox.OK
						});
					}
//					checkOutForm.buttons[7].setDisabled(false);
					setFormButtonStatus(false);
				},
				failure : function(response, options) {
					isPaying = false;
					setFormButtonStatus(false);
					Ext.MessageBox.show({
						msg : "Unknow page error",
						width : 300,
						buttons : Ext.MessageBox.OK
					});
				}
			});	
//		}
//	});
};

/**
 * 
 * @returns {Boolean}
 */
function checkOutListRefresh(){
	if(typeof restaurantData.setting != 'undefined' && typeof restaurantData.setting.eraseQuota == 'undefined'){
		var eraseQuota = parseFloat(document.getElementById("txtEraseQuota").value);
		if(eval(eraseQuota < 0 || eraseQuota > restaurantData.setting.eraseQuota)){
			Ext.example.msg('提示', '抹数金额在 0 至 ' + restaurantData.setting.eraseQuota +' 之间.');
			document.getElementById("txtEraseQuota").value = 0;
			return false;
		}else{
			return true;
		}		
	}else{
		return true;
	}
}
/**
 * 
 */
function memberPay(){
	Ext.ux.checkSmStat();
	var bindMemberWin = Ext.getCmp('co_bindMemberWin');
	if(!bindMemberWin){
		bindMemberWin = new Ext.Window({
			title : '会员结账',
			width : 800,
			height : 500,
			modal : true,
			closable : false,
			resizable : false,
			keys: [{
				key : Ext.EventObject.ESC,
				scope : this,
				fn : function(){
					bindMemberWin.hide();
				}
			}],
			bbar : [{
				xtype : 'checkbox',
				id : 'chbFrontSendConsume',
				checked : true,
				boxLabel : '发送消费信息'+(Ext.ux.smsCount >= 20 ? '(<font style="color:green;font-weight:bolder">剩余'+Ext.ux.smsCount+'条</font>)' : '(<font style="color:red;font-weight:bolder">剩余'+Ext.ux.smsCount+'条, 请及时充值</font>)'),
				hidden : !Ext.ux.smsModule
			}],
			buttonAlign : 'center',
			buttons : [{
				text : '暂结',
				handler : function(e){
					if(memberPayOrderHandler){
//						Ext.getCmp('mpo_txtDiscountForPayOrder').fireEvent('select', Ext.getCmp('mpo_txtDiscountForPayOrder'));
						memberPayOrderHandler({
							tempPay : true,
							
							disabledButton : function(){
								bindMemberWin.buttons[0].setDisabled(true);
								bindMemberWin.buttons[1].setDisabled(true);
								bindMemberWin.buttons[2].setDisabled(true);
							},
							enbledButton : function(){
								bindMemberWin.buttons[0].setDisabled(false);
								bindMemberWin.buttons[1].setDisabled(false);
								bindMemberWin.buttons[2].setDisabled(false);
							},
							callback : function(res, data, c){
								if(res.success){
									Ext.example.msg('提示', '操作成功, <font style="color:red;">'+data.newOrder.id+'&nbsp;</font>账单会员暂结成功.' );
								}
							}
						});
					}else{
						Ext.example.msg('提示', '操作成功, 会员暂结成功.');
					}
				}
			}, {
				text : '结账',
				handler : function(e){
					var sendSms = Ext.getCmp('chbFrontSendConsume').getValue();
					if(sendSms){
						Ext.ux.setCookie(document.domain+'_consumeSms', true, 3650);
					}else{
						Ext.ux.setCookie(document.domain+'_consumeSms', false, 3650);
					}
					if(memberPayOrderHandler){
//						Ext.getCmp('mpo_txtDiscountForPayOrder').fireEvent('select', Ext.getCmp('mpo_txtDiscountForPayOrder'));
						memberPayOrderHandler({
							tempPay : false,
							
							disabledButton : function(){
								bindMemberWin.buttons[0].setDisabled(true);
								bindMemberWin.buttons[1].setDisabled(true);
								bindMemberWin.buttons[2].setDisabled(true);
							},
							enbledButton : function(){
								bindMemberWin.buttons[0].setDisabled(false);
								bindMemberWin.buttons[1].setDisabled(false);
								bindMemberWin.buttons[2].setDisabled(false);
							},
							callback : function(res, data, c){
								if(res.success){
									var member = data.member;
									var newOrder = data.newOrder;
									var interval = 5;
									
									var action = '&nbsp;<span id="returnInterval" style="color:red;"></span>&nbsp; 秒之后自动跳转.';
									new Ext.Window({
										title : '<center>结账信息</center>',
										width : 600,
										modal : true,
										closable : false,
										resizable : false,
										layout : 'column',
										defaults : {
											xtype : 'label',
											columnWidth : .33,
											style : 'vertical-align:middle;line-height:36px;padding-left:20px;font-size:15px;font-weight:bold;'
										},
										items : [{
											html : '会员名称：<font color="red">' + member.name + '</font>'
										}, {
											html : '本次消费：￥<font color="red">' + newOrder.actualPrice.toFixed(2) + '</font>'
										}, {
											html : '余额：￥<font color="red">' + (eval(member.totalBalance - newOrder.actualPrice > 0) ? parseFloat(member.totalBalance - newOrder.actualPrice).toFixed(2) : '---') + '</font>'
										}, {
											html : '账单金额：￥<font color="red">' + newOrder.actualPrice.toFixed(2) + '</font>'
										}, {
											html : '收款金额：￥<font color="red">' + newOrder.actualPrice.toFixed(2) + '</font>'
										}, {
											html : '收款方式：<font color="red">' + newOrder.payMannerDisplay + '</font>'
										}, {
											columnWidth : 1,
											style : 'font-size:22px;line-height:40px;text-align:center;',
											html : (res.data + '.' + action)
										}],
										buttonAlign : 'center',
										buttons : [{
											text : '确&nbsp;&nbsp;定',
											width : 200,
											handler : function(e){
												location.href = "TableSelect.html";
											}
										}],
										listeners : {
											show : function(){
												new Ext.util.TaskRunner().start({
													run: function(){
														if(interval < 1){
															location.href = "TableSelect.html";
														}
														Ext.getDom('returnInterval').innerHTML = interval;
														interval--;
												    },
												    interval : 1000
												});
											}
										}
									}).show(document.body);
								}else{
									Ext.MessageBox.show({
										msg : res.data,
										buttons : Ext.MessageBox.OK
									});
								}
							}
						});
					}else{
						Ext.example.msg('提示', '操作请求失败, 请联系管理员 .');
					}
				}
			}, {
				text : '关闭',
				handler : function(e){
					bindMemberWin.hide();
				}
			}],
			listeners : {
				hide : function(thiz){
					calcDiscountID = tempCalcDiscountID;
					thiz.body.update('');
				},
				show : function(thiz){
					tempCalcDiscountID = calcDiscountID;
					thiz.load({
						url : '../window/frontBusiness/memberPayOrder.jsp',
						scripts : true,
						params : {
							orderID : checkOutData.other.order.id
						}
					});
				}
			}
		});
	}
	if(Ext.ux.getCookie(document.domain+'_consumeSms') == 'true'){
		Ext.getCmp('chbFrontSendConsume').setValue(true);
	}else{
		Ext.getCmp('chbFrontSendConsume').setValue(false);
	}
	bindMemberWin.show();
}
