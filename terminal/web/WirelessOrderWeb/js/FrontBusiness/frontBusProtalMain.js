// ******************************************************************************************************
// shiftWin
shiftPanel = new Ext.Panel({
	frame : true,
	items : [ {
		border : false,
		contentEl : "shiftDiv"
	} ]
});

// billVerifyWin
var billVerifyWin = new Ext.Window({
	layout : "fit",
	width : 200,
	height : 100,
	resizable : false,
	closable : false,
	draggable : false,
	modal : true,
	constrainHeade : true,	
	items : [ {
		layout : "form",
		labelWidth : 30,
		border : false,
		frame : true,
		items : [ {
			xtype : "textfield",
			inputType : "password",
			fieldLabel : "密码",
			id : "billVerifyPwd",
			width : 110
		} ]
	} ],
	buttonAlign : 'center',
	buttons : [{
		text : "确定",
		handler : function() {
			var billVerifyPwd = billVerifyWin.findById("billVerifyPwd").getValue();
			billVerifyWin.findById("billVerifyPwd").setValue("");

			var pwdTrans;
			if (billVerifyPwd != "") {
				pwdTrans = MD5(billVerifyPwd);
			} else {
				pwdTrans = billVerifyPwd;
			}
			
			billVerifyWin.hide();
			isPrompt = false;
			billVerifyWin.findById("billVerifyPwd").setValue("");

			Ext.Ajax.request({
				url : "../../VerifyPwd.do",
				params : {
					"pin" : currPin,
					"type" : 4,
					"pwd" : pwdTrans
				},
				success : function(response, options) {
					var resultJSON = Ext.util.JSON.decode(response.responseText);
					if (resultJSON.success == true) {
						location.href = "Bills.html?restaurantID=" + restaurantID + "&pin=" + currPin;
					} else {
						Ext.MessageBox.show({
							msg : resultJSON.data,
							width : 300,
							buttons : Ext.MessageBox.OK
						});
					}
				},
				failure : function(response, options) {
					
				}
			});
		}
	}, {
		text : "取消",
		handler : function() {
			billVerifyWin.hide();
			isPrompt = false;
			billVerifyWin.findById("billVerifyPwd").setValue("");
		}
	}],
	listeners : {
		show : function(thiz) {
			var f = Ext.get("billVerifyPwd");
			f.focus.defer(100, f); 
		}
	}
});

// shiftVerifyWin
shiftVerifyWin = new Ext.Window({
	layout : "fit",
	width : 200,
	height : 100,
	closeAction : "hide",
	resizable : false,
	closable : false,
	items : [{
		layout : "form",
		labelWidth : 30,
		border : false,
		frame : true,
		items : [{
			xtype : "textfield",
			inputType : "password",
			fieldLabel : "密码",
			id : "shiftVerifyPwd",
			width : 110
		}]
	}],
	buttonAlign : 'center',
	buttons : [{
		text : "确定",
		handler : function(){
			var shiftVerifyPwd = shiftVerifyWin.findById("shiftVerifyPwd").getValue();
			shiftVerifyWin.findById("shiftVerifyPwd").setValue("");
			
			var pwdTrans;
			if (shiftVerifyPwd != ""){
				pwdTrans = MD5(shiftVerifyPwd);
			}else{
				pwdTrans = shiftVerifyPwd;
			}
			
			shiftVerifyWin.hide();
			isPrompt = false;
			shiftVerifyWin.findById("shiftVerifyPwd").setValue("");

			// 密碼校驗
			Ext.Ajax.request({
				url : "../../VerifyPwd.do",
				params : {
					"pin" : currPin,
					"type" : 4,
					"pwd" : pwdTrans
				},
				success : function(response, options){
					var resultJSON = Ext.util.JSON.decode(response.responseText);
					if (resultJSON.success == true){
						// 交班信息
						Ext.Ajax.request({
							url : "../../QueryShift.do",
							params : {
								"pin" : currPin
							},
							success : function(response, options){
								var resultJSON = Ext.util.JSON.decode(response.responseText);
								if (resultJSON.success == true){
									shiftWin.show();
									isPrompt = true;
									
									// update the shift data
									// 后台：["开始日期","结帐日期","账单数","现金金额","现金实收","刷卡金额",
									// "刷卡实收","会员卡金额","会员卡实收","签单金额","签单实收",
									// "挂账金额","挂账实收","实收金额","折扣金额","赠送金额"
									var dataInfo = resultJSON.data;
									var shiftList = dataInfo.substr(1, dataInfo.length - 2).split(",");
									
									var oprName = "";
									for(var i = 0; i < emplData.length; i++){
											if(emplData[i][0] == currPin){
												oprName = emplData[i][1];
											}
									};
									document.getElementById("shiftOperator").innerHTML = oprName;
									document.getElementById("shiftBillCount").innerHTML = shiftList[2].substr(1, shiftList[2].length - 2);
									document.getElementById("shiftStartTime").innerHTML = shiftList[0].substr(1, shiftList[0].length - 2);
									document.getElementById("shiftEndTime").innerHTML = shiftList[1].substr(1, shiftList[1].length - 2);									
									document.getElementById("amount1").innerHTML = shiftList[3].substr(1, shiftList[3].length - 2);
									document.getElementById("actual1").innerHTML = shiftList[4].substr(1, shiftList[4].length - 2);
									document.getElementById("amount2").innerHTML = shiftList[5].substr(1, shiftList[5].length - 2);
									document.getElementById("actual2").innerHTML = shiftList[6].substr(1, shiftList[6].length - 2);
									document.getElementById("amount3").innerHTML = shiftList[7].substr(1, shiftList[7].length - 2);
									document.getElementById("actual3").innerHTML = shiftList[8].substr(1, shiftList[8].length - 2);
									document.getElementById("amount4").innerHTML = shiftList[9].substr(1, shiftList[9].length - 2);
									document.getElementById("actual4").innerHTML = shiftList[10].substr(1, shiftList[10].length - 2);
									document.getElementById("amount5").innerHTML = shiftList[11].substr(1, shiftList[11].length - 2);
									document.getElementById("actual5").innerHTML = shiftList[12].substr(1, shiftList[12].length - 2);
									document.getElementById("discountAmt").innerHTML = shiftList[14].substr(1, shiftList[14].length - 2);
									document.getElementById("freeAmt").innerHTML = shiftList[15].substr(1, shiftList[15].length - 2);
									document.getElementById("payAmt").innerHTML = shiftList[13].substr(1, shiftList[13].length - 2);
									
									shiftStartTiem = shiftList[0].substr(1, shiftList[0].length - 2);
									shiftEndTiem = shiftList[1].substr(1, shiftList[1].length - 2);
								} else {
									Ext.MessageBox.show({
										msg : resultJSON.data,
										width : 300,
										buttons : Ext.MessageBox.OK
									});
								}
							},
							failure : function(response, options){
								
							}
						});
					} else {
						Ext.MessageBox.show({
							msg : resultJSON.data,
							width : 300,
							buttons : Ext.MessageBox.OK
						});
					}
				},
				failure : function(response, options){
					
				}
			});
		}
	}, {
		text : "取消",
		handler : function(){
			shiftVerifyWin.hide();
			isPrompt = false;
			shiftVerifyWin.findById("shiftVerifyPwd").setValue("");
		}
	}],
	listeners : {
		show : function(thiz) {
			var f = Ext.get("shiftVerifyPwd");
			f.focus.defer(100, f);
		}
	}
});

// ------------------------------ daily settle ---------------------------------
doDailySettle = function() {
	Ext.Ajax.request({
		url : "../../DailySettleExec.do",
		params : {
			"pin" : currPin
		},
		success : function(response, options) {
			var resultJSON = Ext.util.JSON.decode(response.responseText);
			if (eval(resultJSON.success == true)) {
				omsg = resultJSON.msg;
				Ext.getCmp('btnRiJieDaYin').handler();
			} else {
				Ext.ux.showMsg(resultJSON);
			}
		},
		failure : function(response, options) {
			Ext.MessageBox.show({
				msg : " Unknown page error ",
				width : 300,
				buttons : Ext.MessageBox.OK
			});
		}
	});
};

var unShiftBillWarnWin = new Ext.Window({
	layout : "fit",
	width : 300,
	height : 200,
	closeAction : "hide",
	resizable : false,
	closable : false,
	items : [ {
		border : false,
		frame : true,
		contentEl : "unShiftBillWarnDiv"
	} ],
	buttonAlign : 'center',
	buttons : [ {
		text : "确定",
		handler : function() {
			unShiftBillWarnWin.hide();
			isPrompt = false;
			
			doDailySettle();
		}
	}, {
		text : "取消",
		handler : function() {
			unShiftBillWarnWin.hide();
			isPrompt = false;
		}
	} ]
});

// dailyConfirmWin
dailyConfirmWin = new Ext.Window({
	layout : "fit",
	width : 200,
	height : 100,
	closeAction : "hide",
	resizable : false,
	closable : false,
	items : [{
		layout : "form",
		labelWidth : 30,
		border : false,
		frame : true,
		items : [{
			xtype : "textfield",
			inputType : "password",
			fieldLabel : "密码",
			id : "dailySettleVerifyPwd",
			width : 110
		}]
	}],
	buttonAlign : 'center',
	buttons : [{
		text : "确定",
		handler : function(){
			var dailySettleVerifyPwd = dailyConfirmWin.findById("dailySettleVerifyPwd").getValue();
			dailyConfirmWin.findById("dailySettleVerifyPwd").setValue("");
			
			var pwdTrans;
			if (dailySettleVerifyPwd != "") {
				pwdTrans = MD5(dailySettleVerifyPwd);
			} else {
				pwdTrans = dailySettleVerifyPwd;
			}
			
			dailyConfirmWin.hide();
			isPrompt = false;
			
			Ext.Ajax.request({
				url : "../../VerifyPwd.do",
				params : {
					"pin" : currPin,
					"type" : 4,
					"pwd" : pwdTrans
				},
				success : function(response, options) {
					var resultJSON = Ext.util.JSON.decode(response.responseText);
					if (resultJSON.success == true) {
						Ext.Ajax.request({
							url : "../../DailySettleCheck.do",
							params : {
								"pin" : currPin
							},
							success : function(response, options) {
								var resultJSON = Ext.util.JSON.decode(response.responseText);
								var rootData = resultJSON.root;
								if (rootData[0].message == "normal") {
									if (rootData[0].text == "NoUnShift") {
										// 沒剩帳單
										dailyConfirmWin.hide();
										isPrompt = false;
										doDailySettle();
									} else {
										// 有剩帳單
										dailyConfirmWin.hide();
										isPrompt = false;
										document.getElementById("unShiftBillWarnMsg").innerHTML = rootData[0].text;
										unShiftBillWarnWin.show();
										isPrompt = true;
									}
								} else {
									Ext.MessageBox.show({
										msg : rootData[0].message,
										width : 300,
										buttons : Ext.MessageBox.OK
									});
								}
							},
							failure : function(response, options) {
								Ext.MessageBox.show({
									msg : " Unknown page error ",
									width : 300,
									buttons : Ext.MessageBox.OK
								});
							}
						});
					} else {
						Ext.MessageBox.show({
							msg : resultJSON.data,
							width : 300,
							buttons : Ext.MessageBox.OK
						});
					}
				},
				failure : function(response, options) {
					
				}
			});
		}
	}, {
		text : "取消",
		handler : function() {
			dailyConfirmWin.hide();
			isPrompt = false;
			dailyConfirmWin.findById("dailySettleVerifyPwd").setValue("");
		}
	}],
	listeners : {
		show : function(thiz) {
			var f = Ext.get("dailySettleVerifyPwd");
			f.focus.defer(100, f); 
		}
	}
});

var dailySettleCheckDetpGrid = new Ext.grid.GridPanel({
//	border : false,
	layout : "fit",
	ds : new Ext.data.JsonStore({
		fields : ['deptName', 'deptDiscount', 'deptGift', 'deptAmount']
	}),
	cm : new Ext.grid.ColumnModel([
	    new Ext.grid.RowNumberer(), 
	    {
	    	header : "部门",
	        dataIndex : "deptName",
	        width : 100
	    }, {
	    	header : "折扣",
	    	dataIndex : "deptDiscount",
	    	width : 100,
	    	align : 'right',
	    	renderer : function(val){
	    		return val.toFixed(2);
	        }
	    }, {
	    	header : "赠送",
	    	dataIndex : "deptGift",
	        width : 100,
	        align : 'right',
	        renderer : function(val){
	            return val.toFixed(2);
	        }
	    }, {
	    	header : "金额",
	    	dataIndex : "deptAmount",
	    	width : 100,
	    	align : 'right',
	    	renderer : function(val){
	    		return val.toFixed(2);
	        }
	    } 
	]),
	viewConfig : {
		forceFit : true
	}
});

var dailySettleCheckDetpPanel = new Ext.Panel({
	region : "center",
	layout : "fit",
	frame : true,
	items : dailySettleCheckDetpGrid
});

var dailySettleCheckTablePanel = new Ext.Panel({
	frame : true,
	region : "north",
	height : 440,
	items : [ {
		border : false,
		contentEl : "shiftCheckTableDiv"
	} ]
});


var dailySettleCheckTableWin = new Ext.Window({
	layout : "border",
	width : 450,
	height : 600,
	closeAction : "hide",
	resizable : false,
	closable : false,
	modal : true,
	items : [ dailySettleCheckTablePanel, dailySettleCheckDetpPanel ],
	buttonAlign : 'center',
	buttons : [{
		text : "交班",
		id : 'btnJiaoBan',
		handler : function() {
			Ext.MessageBox.show({
				msg : "确认进行交班？",
				width : 300,
				buttons : Ext.MessageBox.YESNO,
				fn : function(btn){
					if(btn == "yes"){
						Ext.Ajax.request({
							url : "../../DoShift.do",
							params : {
								pin : currPin,
								onDuty : shiftCheckDate.onDuty,
								offDuty : shiftCheckDate.offDuty
							},
							success : function(response, options) {
								var resultJSON = Ext.util.JSON.decode(response.responseText);
								if (resultJSON.success == true) {
									omsg = resultJSON.data;
									Ext.getCmp('btnJiaoBanDaYin').handler(null);
								} else {
									Ext.MessageBox.show({
										msg : resultJSON.data,
										width : 300,
										buttons : Ext.MessageBox.OK
									});
								}
							},
							failure : function(response, options) {
								var resultJSON = Ext.util.JSON.decode(response.responseText);
								Ext.MessageBox.show({
									msg : resultJSON.data,
									width : 300,
									buttons : Ext.MessageBox.OK
								});
							}
						});
					}
				}
			});
			
		}
	}, {
		text : '打印',
		id : 'btnJiaoBanDaYin',
		handler : function(e){
			Ext.Ajax.request({
				url : "../../PrintOrder.do",
				params : {
					pin : currPin,
					onDuty : shiftCheckDate.onDuty,
					offDuty : shiftCheckDate.offDuty,
//					'printTmpShift' : e != null ? 1 : 0,
//					'printShift' : e != null ? 0 : 1
					'printType' : e != null ? 4 : 5
				},
				success : function(response, options) {
					var resultJSON = Ext.util.JSON.decode(response.responseText);
					Ext.example.msg('提示', (resultJSON.msg + (omsg.length > 0 ? ('<br/>'+omsg) : '')));
					if(omsg.length > 0)
						dailySettleCheckTableWin.hide();
					omsg = '';
				},
				failure : function(response, options) {
//					var resultJSON = Ext.util.JSON.decode(response.responseText);
//					Ext.MessageBox.show({
//						msg : resultJSON.data,
//						width : 300,
//						buttons : Ext.MessageBox.OK
//					});
					Ext.ux.showMsg(Ext.decode(response.responseText));
				}
			});
		}
	}, {
		text : "日结",
		id : 'btnRiJie',
		handler : function() {
			Ext.MessageBox.show({
				msg : "确认进行日结？",
				width : 300,
				buttons : Ext.MessageBox.YESNO,
				fn : function(btn) {
					if (btn == "yes") {
						dailySettleCheckTableWin.hide();
						
						// 未交班帳單檢查
						Ext.Ajax.request({
							url : "../../DailySettleCheck.do",
							params : {
								"pin" : currPin
							},
							success : function(response, options) {
								var resultJSON = Ext.util.JSON.decode(response.responseText);
								if (eval(resultJSON.success == true)) {
									doDailySettle();
								} else {
									document.getElementById("unShiftBillWarnMsg").innerHTML = resultJSON.msg;
									unShiftBillWarnWin.show();
								}
							},
							failure : function(response, options) {
								Ext.MessageBox.show({
									msg : " Unknown page error ",
									width : 300,
									buttons : Ext.MessageBox.OK
								});
							}
						});
					}
				}
			});
		}
	}, {
		text : '打印',
		id : 'btnRiJieDaYin',
		handler : function(e){
			Ext.Ajax.request({
				url : "../../PrintOrder.do",
				params : {
					pin : currPin,
					onDuty : shiftCheckDate.onDuty,
					offDuty : shiftCheckDate.offDuty,
//					'printTmpShift' : e != null ? 1 : 0,
//					'printDailySettle' : e != null ? 0 : 1
					'printType' : e != null ? 6 : 5
				},
				success : function(response, options) {
					var jr = Ext.util.JSON.decode(response.responseText);
					Ext.example.msg('提示', (jr.msg + (omsg.length > 0 ? ('<br/>'+omsg) : '')));
					if(omsg.length > 0)
						dailySettleCheckTableWin.hide();
					omsg = '';
				},
				failure : function(response, options) {
					Ext.ux.showMsg(Ext.decode(response.responseText));
				}
			});
		}
	}, {
		text : "关闭",
		handler : function(){
			dailySettleCheckTableWin.hide();
		}
	}],
	listeners : {
		show : function(thiz){
			
			var btnJiaoBan = Ext.getCmp('btnJiaoBan');
			var btnJiaoBanDaYin = Ext.getCmp('btnJiaoBanDaYin');
			var btnRiJie = Ext.getCmp('btnRiJie');
			var btnRiJieDaYin = Ext.getCmp('btnRiJieDaYin');
			if(shiftCheckDate.otype == 0){
				Ext.getDom('shiftTitle').innerHTML = '交班表';
				btnJiaoBan.setVisible(true);
				btnJiaoBanDaYin.setVisible(true);
				btnRiJie.setVisible(false);
				btnRiJieDaYin.setVisible(false);
			}else if(shiftCheckDate.otype == 1){
				Ext.getDom('shiftTitle').innerHTML = '日结表';
				btnJiaoBan.setVisible(false);
				btnJiaoBanDaYin.setVisible(false);
				btnRiJie.setVisible(true);
				btnRiJieDaYin.setVisible(true);
			}else{
				thiz.hide();
				return;
			}
			
			Ext.getDom('shiftOperatorCheck').innerHTML = Ext.getDom('optName').innerHTML;
			Ext.getDom('shiftBillCountCheck').innerHTML = shiftCheckDate.allBillCount;
			Ext.getDom('shiftStartTimeCheck').innerHTML = shiftCheckDate.onDuty;
			Ext.getDom('shiftEndTimeCheck').innerHTML = shiftCheckDate.offDuty;
			
			Ext.getDom('billCount1Check').innerHTML = shiftCheckDate.cashBillCount;
			Ext.getDom('amount1Check').innerHTML = shiftCheckDate.cashAmount.toFixed(2);
			Ext.getDom('actual1Check').innerHTML = shiftCheckDate.cashActual.toFixed(2);
			
			Ext.getDom('billCount2Check').innerHTML = shiftCheckDate.creditBillCount;
			Ext.getDom('amount2Check').innerHTML = shiftCheckDate.creditAmount.toFixed(2);
			Ext.getDom('actual2Check').innerHTML = shiftCheckDate.creditActual.toFixed(2);
			
			Ext.getDom('billCount3Check').innerHTML = shiftCheckDate.memberBillCount;
			Ext.getDom('amount3Check').innerHTML = shiftCheckDate.memberAmount.toFixed(2);
			Ext.getDom('actual3Check').innerHTML = shiftCheckDate.memberActual.toFixed(2);
			
			Ext.getDom('billCount4Check').innerHTML = shiftCheckDate.signBillCount;
			Ext.getDom('amount4Check').innerHTML = shiftCheckDate.signAmount.toFixed(2);
			Ext.getDom('actual4Check').innerHTML = shiftCheckDate.signActual.toFixed(2);
			
			Ext.getDom('billCount5Check').innerHTML = shiftCheckDate.hangBillCount;
			Ext.getDom('amount5Check').innerHTML = shiftCheckDate.hangAmount.toFixed(2);
			Ext.getDom('actual5Check').innerHTML = shiftCheckDate.hangActual.toFixed(2);
			
			Ext.getDom('billCountSumCheck').innerHTML = shiftCheckDate.allBillCount;
			Ext.getDom('amountSumCheck').innerHTML = (shiftCheckDate.cashAmount 
														+ shiftCheckDate.creditAmount
														+ shiftCheckDate.memberAmount
														+ shiftCheckDate.signAmount+
														+ shiftCheckDate.hangAmount).toFixed(2);
			Ext.getDom('actualSumCheck').innerHTML = (shiftCheckDate.cashActual
														+ shiftCheckDate.creditActual
														+ shiftCheckDate.memberActual
														+ shiftCheckDate.signActual
														+ shiftCheckDate.hangActual).toFixed(2);
			
			Ext.getDom('eraseAmountCheck').innerHTML = shiftCheckDate.eraseAmount.toFixed(2);
			Ext.getDom('eraseIncomeCheck').innerHTML = shiftCheckDate.eraseBillCount;
			
			Ext.getDom('discountAmountCheck').innerHTML = shiftCheckDate.discountAmount.toFixed(2);
			Ext.getDom('discountBillCountCheck').innerHTML = shiftCheckDate.discountBillCount;
			
			Ext.getDom('giftAmountCheck').innerHTML = shiftCheckDate.discountAmount.toFixed(2);
			Ext.getDom('giftBillCountCheck').innerHTML = shiftCheckDate.discountBillCount;
			
			Ext.getDom('giftAmountCheck').innerHTML = shiftCheckDate.giftAmount.toFixed(2);
			Ext.getDom('giftBillCountCheck').innerHTML = shiftCheckDate.giftBillCount;
			
			Ext.getDom('returnAmountCheck').innerHTML = shiftCheckDate.returnAmount.toFixed(2);
			Ext.getDom('returnBillCountCheck').innerHTML = shiftCheckDate.returnBillCount;
			
			Ext.getDom('repayAmountCheck').innerHTML = shiftCheckDate.repayAmount.toFixed(2);
			Ext.getDom('repayBillCountCheck').innerHTML = shiftCheckDate.repayBillCount;
			
			Ext.getDom('serviceAmountCheck').innerHTML = shiftCheckDate.serviceAmount.toFixed(2);
			
			dailySettleCheckDetpGrid.getStore().loadData(shiftCheckDate.deptInfos);
		}
	}
});

// ---------------------------------------------------------------------------

Ext.onReady(function() {
	// 解决ext中文传入后台变问号问题
	Ext.lib.Ajax.defaultPostHeader += '; charset=utf-8';
	Ext.QuickTips.init();

	// ******************************************************************************************************
	var pushBackBut = new Ext.ux.ImageButton({
		imgPath : "../../images/UserLogout.png",
		imgWidth : 50,
		imgHeight : 50,
		tooltip : "返回",
		handler : function(btn) {
			location.href = "../PersonLogin.html?restaurantID=" + restaurantID + "&isNewAccess=false&pin=" + currPin;
		}
	});

	var logOutBut = new Ext.ux.ImageButton({
		imgPath : "../../images/ResLogout.png",
		imgWidth : 50,
		imgHeight : 50,
		tooltip : "登出",
		handler : function(btn) {
			
		}
	});

	var centerPanel = new Ext.Panel({
		region : "center",
		frame : true,
		autoScroll : true,
		tbar : new Ext.Toolbar({
			height : 55,
			items : [ "->", pushBackBut, 
			{
				text : "&nbsp;&nbsp;&nbsp;",
				disabled : true
			}, 
			logOutBut]
		}),
		items : [{
			border : false,
			contentEl : "protal"
		}]
	});
	
	new Ext.Viewport({
		layout : "border",
		id : "viewport",
		items : [{
			region : "north",
			bodyStyle : "background-color:#DFE8F6;",
			html : "<h4 style='padding:10px;font-size:150%;float:left;'>无线点餐网页终端</h4><div id='optName' class='optName'></div>",
			height : 50,
			border : false,
			margins : '0 0 0 0'
		},
		centerPanel,
		{
			region : "south",
			height : 30,
			layout : "form",
			frame : true,
			html : "<div style='font-size:11pt; text-align:center;'><b>版权所有(c) 2011 智易科技</b></div>"
		} ]
	});
});
