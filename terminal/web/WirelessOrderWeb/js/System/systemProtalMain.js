var passwordConfigForm = new Ext.form.FormPanel(
		{
			frame : true,
			border : false,
			layout : "fit",
			items : [
					{
						layout : "column",
						autoHeight : true, // important!!
						autoWidth : true,
						border : false,
						anchor : '98%',
						items : [
								{
									layout : "form",
									border : false,
									labelSeparator : '',
									columnWidth : .10,
									items : [ {
										xtype : "button",
										hideLabel : true,
										id : "adminLock",
										text : "",
										tooltip : '解锁',
										iconCls : 'lock',
										listeners : {
											"click" : function(thiz, e) {
												if (adminLock == "YES") {
													thiz.setIconClass("unlock");
													adminLock = "NO";

													passwordConfigWin.findById(
															"adminPwd")
															.setValue("");
													passwordConfigWin.findById(
															"adminPwdConfirm")
															.setValue("");

													passwordConfigWin.findById(
															"adminPwd")
															.enable();
													passwordConfigWin.findById(
															"adminPwdConfirm")
															.enable();
												} else {
													thiz.setIconClass("lock");
													adminLock = "YES";

													passwordConfigWin.findById(
															"adminPwd")
															.setValue("111111");
													passwordConfigWin.findById(
															"adminPwdConfirm")
															.setValue("111111");

													passwordConfigWin.findById(
															"adminPwd")
															.disable();
													passwordConfigWin.findById(
															"adminPwdConfirm")
															.disable();
												}
											}
										}
									} ]
								}, {
									layout : "form",
									border : false,
									labelSeparator : '：',
									labelWidth : 100,
									// width : 170,
									columnWidth : .45,
									items : [ {
										xtype : "textfield",
										inputType : "password",
										fieldLabel : "管理员密码",
										allowBlank : false,
										id : "adminPwd",
										width : 120
									} ]
								}, {
									layout : "form",
									border : false,
									labelSeparator : '：',
									labelWidth : 100,
									// width : 170,
									columnWidth : .45,
									items : [ {
										xtype : "textfield",
										inputType : "password",
										fieldLabel : "密码确认",
										allowBlank : false,
										id : "adminPwdConfirm",
										width : 120
									} ]
								} ]
					},
					{
						layout : "column",
						autoHeight : true, // important!!
						autoWidth : true,
						border : false,
						anchor : '98%',
						items : [
								{
									layout : "form",
									border : false,
									labelSeparator : '',
									columnWidth : .10,
									items : [ {
										xtype : "button",
										hideLabel : true,
										id : "financeLock",
										text : "",
										tooltip : '解锁',
										iconCls : 'lock',
										listeners : {
											"click" : function(thiz, e) {
												if (financeLock == "YES") {
													thiz.setIconClass("unlock");
													financeLock = "NO";

													passwordConfigWin.findById(
															"financePwd")
															.setValue("");

													passwordConfigWin.findById(
															"financePwd")
															.enable();

												} else {
													thiz.setIconClass("lock");
													financeLock = "YES";

													passwordConfigWin.findById(
															"financePwd")
															.setValue("******");

													passwordConfigWin.findById(
															"financePwd")
															.disable();

												}
											}
										}
									} ]
								}, {
									layout : "form",
									border : false,
									labelSeparator : '：',
									labelWidth : 100,
									columnWidth : .45,
									items : [ {
										xtype : "textfield",
										fieldLabel : "财务权限密码",
										id : "financePwd",
										width : 120
									} ]
								} ]
					},
					{
						layout : "column",
						autoHeight : true, // important!!
						autoWidth : true,
						border : false,
						anchor : '98%',
						items : [
								{
									layout : "form",
									border : false,
									labelSeparator : '',
									columnWidth : .10,
									items : [ {
										xtype : "button",
										hideLabel : true,
										id : "managerLock",
										text : "",
										tooltip : '解锁',
										iconCls : 'lock',
										listeners : {
											"click" : function(thiz, e) {
												if (managerLock == "YES") {
													thiz.setIconClass("unlock");
													managerLock = "NO";

													passwordConfigWin.findById(
															"managerPwd")
															.setValue("");

													passwordConfigWin.findById(
															"managerPwd")
															.enable();

												} else {
													thiz.setIconClass("lock");
													managerLock = "YES";

													passwordConfigWin.findById(
															"managerPwd")
															.setValue("******");

													passwordConfigWin.findById(
															"managerPwd")
															.disable();

												}
											}
										}
									} ]
								}, {
									layout : "form",
									border : false,
									labelSeparator : '：',
									labelWidth : 100,
									columnWidth : .45,
									items : [ {
										xtype : "textfield",
										fieldLabel : "店长权限密码",
										id : "managerPwd",
										width : 120
									} ]
								} ]
					},
					{
						layout : "column",
						autoHeight : true, // important!!
						autoWidth : true,
						border : false,
						anchor : '98%',
						items : [
								{
									layout : "form",
									border : false,
									labelSeparator : '',
									columnWidth : .10,
									items : [ {
										xtype : "button",
										hideLabel : true,
										id : "cashierLock",
										text : "",
										tooltip : '解锁',
										iconCls : 'lock',
										listeners : {
											"click" : function(thiz, e) {
												if (cashierLock == "YES") {
													thiz.setIconClass("unlock");
													cashierLock = "NO";

													passwordConfigWin.findById(
															"cashierPwd")
															.setValue("");

													passwordConfigWin.findById(
															"cashierPwd")
															.enable();

												} else {
													thiz.setIconClass("lock");
													cashierLock = "YES";

													passwordConfigWin.findById(
															"cashierPwd")
															.setValue("******");

													passwordConfigWin.findById(
															"cashierPwd")
															.disable();

												}
											}
										}
									} ]
								}, {
									layout : "form",
									border : false,
									labelSeparator : '：',
									labelWidth : 100,
									columnWidth : .45,
									items : [ {
										xtype : "textfield",
										fieldLabel : "收银员权限密码",
										id : "cashierPwd",
										width : 120
									} ]
								} ]
					},
					{
						layout : "column",
						autoHeight : true, // important!!
						autoWidth : true,
						border : false,
						anchor : '98%',
						items : [
								{
									layout : "form",
									border : false,
									labelSeparator : '',
									columnWidth : .10,
									items : [ {
										xtype : "button",
										hideLabel : true,
										id : "orderCancelLock",
										text : "",
										tooltip : '解锁',
										iconCls : 'lock',
										listeners : {
											"click" : function(thiz, e) {
												if (orderCancelLock == "YES") {
													thiz.setIconClass("unlock");
													orderCancelLock = "NO";

													passwordConfigWin.findById(
															"orderCancelPwd")
															.setValue("");

													passwordConfigWin.findById(
															"orderCancelPwd")
															.enable();

												} else {
													thiz.setIconClass("lock");
													orderCancelLock = "YES";

													passwordConfigWin.findById(
															"orderCancelPwd")
															.setValue("******");

													passwordConfigWin.findById(
															"orderCancelPwd")
															.disable();

												}
											}
										}
									} ]
								}, {
									layout : "form",
									border : false,
									labelSeparator : '：',
									labelWidth : 100,
									columnWidth : .45,
									items : [ {
										xtype : "textfield",
										fieldLabel : "退菜权限密码",
										id : "orderCancelPwd",
										width : 120
									} ]
								} ]
					},
					{
						html : '<div style="margin-top:4px"><font id="errorMsgChangePwd" style="color:red;"> </font></div>'
					} ]
		});

var passwordConfigWin = new Ext.Window(
		{
			layout : "fit",
			title : "密码设置",
			width : 550,
			height : 220,
			// height : 500,
			closeAction : "hide",
			resizable : false,
			items : passwordConfigForm,
			buttons : [
					{
						text : "保存",
						handler : function() {

							// 1, get parameters
							var adminPwd = passwordConfigWin.findById(
									"adminPwd").getValue();
							var adminPwdConfirm = passwordConfigWin.findById(
									"adminPwdConfirm").getValue();
							var financePwd = passwordConfigWin.findById(
									"financePwd").getValue();
							var managerPwd = passwordConfigWin.findById(
									"managerPwd").getValue();
							var cashierPwd = passwordConfigWin.findById(
									"cashierPwd").getValue();
							var orderCancelPwd = passwordConfigWin.findById(
									"orderCancelPwd").getValue();

							// 2, save the passwords
							if (passwordConfigWin.findById("adminPwd")
									.isValid()
									&& passwordConfigWin.findById(
											"adminPwdConfirm").isValid()) {
								if (adminPwd == adminPwdConfirm) {

									if (adminLock == "YES"
											&& financeLock == "YES"
											&& managerLock == "YES"
											&& cashierLock == "YES"
											&& orderCancelLock == "YES") {

										document
												.getElementById("errorMsgChangePwd").innerHTML = "密码并未修改";

									} else {

										passwordConfigWin.hide();
										isPrompt = false;

										if (adminLock == "YES") {
											adminPwd = "<special_message:not_change>";
										}
										if (financeLock == "YES") {
											financePwd = "<special_message:not_change>";
										}
										if (managerLock == "YES") {
											managerPwd = "<special_message:not_change>";
										}
										if (cashierLock == "YES") {
											cashierPwd = "<special_message:not_change>";
										}
										if (orderCancelLock == "YES") {
											orderCancelPwd = "<special_message:not_change>";
										}

										Ext.Ajax
												.request({
													url : "../../SetPassword.do",
													params : {
														"pin" : pin,
														"adminPwd" : adminPwd,
														"financePwd" : financePwd,
														"managerPwd" : managerPwd,
														"cashierPwd" : cashierPwd,
														"orderCancelPwd" : orderCancelPwd
													},
													success : function(
															response, options) {
														var resultJSON = Ext.util.JSON
																.decode(response.responseText);
														if (resultJSON.success == true) {

															var dataInfo = resultJSON.data;
															Ext.MessageBox
																	.show({
																		msg : dataInfo,
																		width : 300,
																		buttons : Ext.MessageBox.OK
																	});
														} else {
															var dataInfo = resultJSON.data;
															Ext.MessageBox
																	.show({
																		msg : dataInfo,
																		width : 300,
																		buttons : Ext.MessageBox.OK
																	});
														}
													},
													failure : function(
															response, options) {
														Ext.MessageBox
																.show({
																	msg : " Unknown page error ",
																	width : 300,
																	buttons : Ext.MessageBox.OK
																});
													}
												});

									}
								} else {
									document
											.getElementById("errorMsgChangePwd").innerHTML = "确认密码不一致";
								}
							}
						}
					}, {
						text : "取消",
						handler : function() {
							passwordConfigWin.hide();
							isPrompt = false;
						}
					} ],
			listeners : {
				"show" : function() {
					passwordConfigWin.findById("adminPwd").setValue("111111");
					passwordConfigWin.findById("adminPwdConfirm").setValue(
							"111111");
					passwordConfigWin.findById("adminPwd").clearInvalid();
					passwordConfigWin.findById("adminPwdConfirm")
							.clearInvalid();

					passwordConfigWin.findById("financePwd").setValue("******");
					passwordConfigWin.findById("managerPwd").setValue("******");
					passwordConfigWin.findById("cashierPwd").setValue("******");
					passwordConfigWin.findById("orderCancelPwd").setValue(
							"******");

					passwordConfigWin.findById("adminPwd").disable();
					passwordConfigWin.findById("adminPwdConfirm").disable();
					passwordConfigWin.findById("financePwd").disable();
					passwordConfigWin.findById("managerPwd").disable();
					passwordConfigWin.findById("cashierPwd").disable();
					passwordConfigWin.findById("orderCancelPwd").disable();

					passwordConfigWin.findById("adminLock")
							.setIconClass("lock");
					passwordConfigWin.findById("financeLock").setIconClass(
							"lock");
					passwordConfigWin.findById("managerLock").setIconClass(
							"lock");
					passwordConfigWin.findById("cashierLock").setIconClass(
							"lock");
					passwordConfigWin.findById("orderCancelLock").setIconClass(
							"lock");

					adminLock = "YES";
					financeLock = "YES";
					managerLock = "YES";
					cashierLock = "YES";
					orderCancelLock = "YES";

					document.getElementById("errorMsgChangePwd").innerHTML = "";
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
							+ restaurantID + "&isNewAccess=false&pin=" + pin;
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
