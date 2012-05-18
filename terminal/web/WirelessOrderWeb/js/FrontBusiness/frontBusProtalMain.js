// ******************************************************************************************************
// shiftWin
var shiftPanel = new Ext.Panel({
	frame : true,
	items : [ {
		border : false,
		contentEl : "shiftDiv"
	} ]
});

var shiftWin = new Ext.Window(
		{
			layout : "fit",
			width : 450,
			height : 450,
			closeAction : "hide",
			resizable : false,
			closable : false,
			items : shiftPanel,
			buttons : [
					{
						text : "交班",
						handler : function() {
							// show the window
							shiftWin.hide();

							isPrompt = false;

							// do shift
							Ext.Ajax
									.request({
										url : "../../DoShift.do",
										params : {
											"pin" : currPin,
											"onDuty" : shiftStartTiem,
											"offDuty" : shiftEndTiem
										},
										success : function(response, options) {
											var resultJSON = Ext.util.JSON
													.decode(response.responseText);
											if (resultJSON.success == true) {
												// 彈出成功提示語，打印提示語
												Ext.MessageBox
														.show({
															msg : resultJSON.data
																	+ "，是否打印交班对账单？",
															width : 300,
															buttons : Ext.MessageBox.YESNO,
															fn : function(btn) {
																if (btn == "yes") {
																	// print
																	// shift
																	Ext.Ajax
																			.request({
																				url : "../../PrintOrder.do",
																				params : {
																					"pin" : currPin,
																					"printShift" : 1,
																					"onDuty" : shiftStartTiem,
																					"offDuty" : shiftEndTiem
																				},
																				success : function(
																						response,
																						options) {
																					var resultJSON1 = Ext.util.JSON
																							.decode(response.responseText);
																					Ext.MessageBox
																							.show({
																								msg : resultJSON1.data,
																								width : 300,
																								buttons : Ext.MessageBox.OK
																							});

																				},
																				failure : function(
																						response,
																						options) {
																				}
																			});
																}
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
					},
					{
						text : "打印",
						// disabled : true,
						handler : function() {
							Ext.Ajax.request({
								url : "../../PrintOrder.do",
								params : {
									"pin" : currPin,
									"printTmpShift" : 1,
									"onDuty" : shiftStartTiem,
									"offDuty" : shiftEndTiem
								},
								success : function(response, options) {
									var resultJSON = Ext.util.JSON
											.decode(response.responseText);
									Ext.MessageBox.show({
										msg : resultJSON.data,
										width : 300,
										buttons : Ext.MessageBox.OK
									});

								},
								failure : function(response, options) {
								}
							});
						}
					}, {
						text : "取消",
						handler : function() {
							shiftWin.hide();
							isPrompt = false;
						}
					} ]
		});

// billVerifyWin
var billVerifyWin = new Ext.Window({
	layout : "fit",
	width : 200,
	height : 100,
	closeAction : "hide",
	resizable : false,
	closable : false,
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
	buttons : [
			{
				text : "确定",
				handler : function() {
					var billVerifyPwd = billVerifyWin.findById("billVerifyPwd")
							.getValue();
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
							"type" : "4",
							"pwd" : pwdTrans
						},
						success : function(response, options) {
							var resultJSON = Ext.util.JSON
									.decode(response.responseText);
							if (resultJSON.success == true) {
								location.href = "Bills.html?restaurantID="
										+ restaurantID + "&pin=" + currPin;
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
			} ],
	listeners : {
		show : function(thiz) {
			// thiz.findById("personCountInput").focus();
			var f = Ext.get("billVerifyPwd");
			f.focus.defer(100, f); // 万恶的EXT！为什么这样才可以！？！？
		}
	}
});

// shiftVerifyWin
var shiftVerifyWin = new Ext.Window(
		{
			layout : "fit",
			width : 200,
			height : 100,
			closeAction : "hide",
			resizable : false,
			closable : false,
			items : [ {
				layout : "form",
				labelWidth : 30,
				border : false,
				frame : true,
				items : [ {
					xtype : "textfield",
					inputType : "password",
					fieldLabel : "密码",
					id : "shiftVerifyPwd",
					width : 110
				} ]
			} ],
			buttons : [
					{
						text : "确定",
						handler : function() {
							var shiftVerifyPwd = shiftVerifyWin.findById(
									"shiftVerifyPwd").getValue();
							shiftVerifyWin.findById("shiftVerifyPwd").setValue(
									"");

							var pwdTrans;
							if (shiftVerifyPwd != "") {
								pwdTrans = MD5(shiftVerifyPwd);
							} else {
								pwdTrans = shiftVerifyPwd;
							}

							shiftVerifyWin.hide();
							isPrompt = false;
							shiftVerifyWin.findById("shiftVerifyPwd").setValue(
									"");

							// 密碼校驗
							Ext.Ajax
									.request({
										url : "../../VerifyPwd.do",
										params : {
											"pin" : currPin,
											"type" : "4",
											"pwd" : pwdTrans
										},
										success : function(response, options) {
											var resultJSON = Ext.util.JSON
													.decode(response.responseText);
											if (resultJSON.success == true) {

												// 交班信息
												Ext.Ajax
														.request({
															url : "../../QueryShift.do",
															params : {
																"pin" : currPin
															},
															success : function(
																	response,
																	options) {
																var resultJSON = Ext.util.JSON
																		.decode(response.responseText);
																if (resultJSON.success == true) {
																	// show the
																	// window
																	shiftWin
																			.show();
																	isPrompt = true;

																	// update
																	// the shift
																	// data
																	// 后台：
																	// ["开始日期","结帐日期","账单数","现金金额","现金实收","刷卡金额",
																	// "刷卡实收","会员卡金额","会员卡实收","签单金额","签单实收",
																	// "挂账金额","挂账实收","实收金额","折扣金额","赠送金额"
																	var dataInfo = resultJSON.data;
																	var shiftList = dataInfo
																			.substr(
																					1,
																					dataInfo.length - 2)
																			.split(
																					",");

																	var oprName = "";
																	for ( var i = 0; i < emplData.length; i++) {
																		if (emplData[i][0] == currPin) {
																			oprName = emplData[i][1];
																		}
																	}
																	;

																	document
																			.getElementById("shiftOperator").innerHTML = oprName;
																	document
																			.getElementById("shiftBillCount").innerHTML = shiftList[2]
																			.substr(
																					1,
																					shiftList[2].length - 2);
																	document
																			.getElementById("shiftStartTime").innerHTML = shiftList[0]
																			.substr(
																					1,
																					shiftList[0].length - 2);
																	document
																			.getElementById("shiftEndTime").innerHTML = shiftList[1]
																			.substr(
																					1,
																					shiftList[1].length - 2);

																	document
																			.getElementById("amount1").innerHTML = shiftList[3]
																			.substr(
																					1,
																					shiftList[3].length - 2);
																	document
																			.getElementById("actual1").innerHTML = shiftList[4]
																			.substr(
																					1,
																					shiftList[4].length - 2);
																	document
																			.getElementById("amount2").innerHTML = shiftList[5]
																			.substr(
																					1,
																					shiftList[5].length - 2);
																	document
																			.getElementById("actual2").innerHTML = shiftList[6]
																			.substr(
																					1,
																					shiftList[6].length - 2);
																	document
																			.getElementById("amount3").innerHTML = shiftList[7]
																			.substr(
																					1,
																					shiftList[7].length - 2);
																	document
																			.getElementById("actual3").innerHTML = shiftList[8]
																			.substr(
																					1,
																					shiftList[8].length - 2);
																	document
																			.getElementById("amount4").innerHTML = shiftList[9]
																			.substr(
																					1,
																					shiftList[9].length - 2);
																	document
																			.getElementById("actual4").innerHTML = shiftList[10]
																			.substr(
																					1,
																					shiftList[10].length - 2);
																	document
																			.getElementById("amount5").innerHTML = shiftList[11]
																			.substr(
																					1,
																					shiftList[11].length - 2);
																	document
																			.getElementById("actual5").innerHTML = shiftList[12]
																			.substr(
																					1,
																					shiftList[12].length - 2);

																	document
																			.getElementById("discountAmt").innerHTML = shiftList[14]
																			.substr(
																					1,
																					shiftList[14].length - 2);
																	document
																			.getElementById("freeAmt").innerHTML = shiftList[15]
																			.substr(
																					1,
																					shiftList[15].length - 2);
																	document
																			.getElementById("payAmt").innerHTML = shiftList[13]
																			.substr(
																					1,
																					shiftList[13].length - 2);

																	shiftStartTiem = shiftList[0]
																			.substr(
																					1,
																					shiftList[0].length - 2);
																	shiftEndTiem = shiftList[1]
																			.substr(
																					1,
																					shiftList[1].length - 2);

																} else {
																	Ext.MessageBox
																			.show({
																				msg : resultJSON.data,
																				width : 300,
																				buttons : Ext.MessageBox.OK
																			});
																}
															},
															failure : function(
																	response,
																	options) {
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
					},
					{
						text : "取消",
						handler : function() {
							shiftVerifyWin.hide();
							isPrompt = false;
							shiftVerifyWin.findById("shiftVerifyPwd").setValue(
									"");
						}
					} ],
			listeners : {
				show : function(thiz) {
					// thiz.findById("personCountInput").focus();
					var f = Ext.get("shiftVerifyPwd");
					f.focus.defer(100, f); // 万恶的EXT！为什么这样才可以！？！？
				}
			}
		});

// ------------------------------ daily settle ---------------------------------
var doDailySettle = function() {
	Ext.Ajax.request({
		url : "../../DailySettleExec.do",
		params : {
			"pin" : currPin
		},
		success : function(response, options) {
			var resultJSON = Ext.util.JSON.decode(response.responseText);
			var rootData = resultJSON.root;
			if (rootData[0].message == "normal") {
				
				Ext.Ajax.request({
					url : "../../PrintOrder.do",
					params : {
						"pin" : currPin,
						"printDailySettle" : 1
					},
					success : function(response, options) {
						Ext.MessageBox.show({
							msg : "日结成功",
							width : 300,
							buttons : Ext.MessageBox.OK
						});
					},
					failure : function(response, options) {
					}
				});
				
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

// dailySettleVerifyWin
var dailySettleVerifyWin = new Ext.Window(
		{
			layout : "fit",
			width : 200,
			height : 100,
			closeAction : "hide",
			resizable : false,
			closable : false,
			items : [ {
				layout : "form",
				labelWidth : 30,
				border : false,
				frame : true,
				items : [ {
					xtype : "textfield",
					inputType : "password",
					fieldLabel : "密码",
					id : "dailySettleVerifyPwd",
					width : 110
				} ]
			} ],
			buttons : [
					{
						text : "确定",
						handler : function() {
							var dailySettleVerifyPwd = dailySettleVerifyWin
									.findById("dailySettleVerifyPwd")
									.getValue();
							dailySettleVerifyWin.findById(
									"dailySettleVerifyPwd").setValue("");

							var pwdTrans;
							if (dailySettleVerifyPwd != "") {
								pwdTrans = MD5(dailySettleVerifyPwd);
							} else {
								pwdTrans = dailySettleVerifyPwd;
							}

							dailySettleVerifyWin.hide();
							isPrompt = false;

							// 密碼校驗
							Ext.Ajax
									.request({
										url : "../../VerifyPwd.do",
										params : {
											"pin" : currPin,
											"type" : "4",
											"pwd" : pwdTrans
										},
										success : function(response, options) {
											var resultJSON = Ext.util.JSON
													.decode(response.responseText);
											if (resultJSON.success == true) {
												// 未交班帳單檢查
												Ext.Ajax
														.request({
															url : "../../DailySettleCheck.do",
															params : {
																"pin" : currPin
															},
															success : function(
																	response,
																	options) {
																var resultJSON = Ext.util.JSON
																		.decode(response.responseText);
																var rootData = resultJSON.root;
																if (rootData[0].message == "normal") {
																	if (rootData[0].text == "NoUnShift") {
																		// 沒剩帳單
																		dailySettleVerifyWin
																				.hide();
																		isPrompt = false;

																		doDailySettle();
																	} else {
																		// 有剩帳單
																		dailySettleVerifyWin
																				.hide();
																		isPrompt = false;

																		document
																				.getElementById("unShiftBillWarnMsg").innerHTML = rootData[0].text;

																		unShiftBillWarnWin
																				.show();
																		isPrompt = true;
																	}
																} else {
																	Ext.MessageBox
																			.show({
																				msg : rootData[0].message,
																				width : 300,
																				buttons : Ext.MessageBox.OK
																			});
																}
															},
															failure : function(
																	response,
																	options) {
																Ext.MessageBox
																		.show({
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
					},
					{
						text : "取消",
						handler : function() {
							dailySettleVerifyWin.hide();
							isPrompt = false;
							dailySettleVerifyWin.findById(
									"dailySettleVerifyPwd").setValue("");
						}
					} ],
			listeners : {
				show : function(thiz) {
					// thiz.findById("personCountInput").focus();
					var f = Ext.get("dailySettleVerifyPwd");
					f.focus.defer(100, f); // 万恶的EXT！为什么这样才可以！？！？
				}
			}
		});

// ---------------------------------------------------------------------------

Ext
		.onReady(function() {
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
					location.href = "../PersonLogin.html?restaurantID="
							+ restaurantID + "&isNewAccess=false&pin="
							+ currPin;
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
					items : [ "->", pushBackBut, {
						text : "&nbsp;&nbsp;&nbsp;",
						disabled : true
					}, logOutBut ]
				}),
				items : [ {
					border : false,
					contentEl : "protal"
				} ]
			});

			var viewport = new Ext.Viewport(
					{
						layout : "border",
						id : "viewport",
						items : [
								{
									region : "north",
									bodyStyle : "background-color:#A9D0F5",
									html : "<h4 style='padding:10px;font-size:150%;float:left;'>无线点餐网页终端</h4><div id='optName' class='optName'></div>",
									height : 50,
									margins : '0 0 5 0'
								},
								centerPanel,
								{
									region : "south",
									height : 30,
									layout : "form",
									frame : true,
									border : false,
									html : "<div style='font-size:11pt; text-align:center;'><b>版权所有(c) 2011 智易科技</b></div>"
								} ]
					});

			// -------------------- 浏览器大小改变 -------------------------------
			// Ext.EventManager.onWindowResize(function() {
			// // obj.style[attr]
			// document.getElementById("wrap").style["height"] =
			// (tableSelectCenterPanel
			// .getInnerHeight() - 100)
			// + "px";
			// });
		});
