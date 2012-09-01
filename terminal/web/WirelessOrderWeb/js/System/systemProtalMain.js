﻿var passwordConfigForm = new Ext.form.FormPanel({
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
			    	items : [
			    	    {
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
										Ext.getCmp("adminPwd").setValue("");
										Ext.getCmp("adminPwdConfirm").setValue("");

										Ext.getCmp("adminPwd").enable();
										Ext.getCmp("adminPwdConfirm").enable();
									} else {
										thiz.setIconClass("lock");
										adminLock = "YES";

										Ext.getCmp("adminPwd").setValue("111111");
										Ext.getCmp("adminPwdConfirm").setValue("111111");

										Ext.getCmp("adminPwd").disable();
										Ext.getCmp("adminPwdConfirm").disable();
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
						items : [
						    {
						    	xtype : "textfield",
								inputType : "password",
								fieldLabel : "管理员密码",
								allowBlank : false,
								id : "adminPwd",
								width : 120
							}
						 ]
					}, {
						layout : "form",
						border : false,
						labelSeparator : '：',
						labelWidth : 100,
						columnWidth : .45,
						items : [
						    {
						    	xtype : "textfield",
								inputType : "password",
								fieldLabel : "密码确认",
								allowBlank : false,
								id : "adminPwdConfirm",
								width : 120
							}
						]
					}
				]
	    }, {
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
					items : [
					    {
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

										Ext.getCmp("financePwd").setValue("");
										Ext.getCmp("financePwd").enable();
									} else {
										thiz.setIconClass("lock");
										financeLock = "YES";

										Ext.getCmp("financePwd").setValue("******");
										Ext.getCmp("financePwd").disable();
									}
								}
							}
					    }
					]
			    }, {
			    	layout : "form",
			    	border : false,
			    	labelSeparator : '：',
			    	labelWidth : 100,
			    	columnWidth : .45,
			    	items : [
			    	    {
			    	    	xtype : "textfield",
							fieldLabel : "财务权限密码",
							id : "financePwd",
							width : 120
						}
			    	]
			    }
			]
	    }, {
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
			    	items : [
			    	     {
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

										Ext.getCmp("managerPwd").setValue("");
										Ext.getCmp("managerPwd").enable();
									} else {
										thiz.setIconClass("lock");
										managerLock = "YES";

										Ext.getCmp("managerPwd").setValue("******");
										Ext.getCmp("managerPwd").disable();
									}
								}
							}
						}
			    	]
			    }, {
			    	layout : "form",
					border : false,
					labelSeparator : '：',
					labelWidth : 100,
					columnWidth : .45,
						items : [
						    {
						    	xtype : "textfield",
								fieldLabel : "店长权限密码",
								id : "managerPwd",
								width : 120
							}
						 ]
			    	}
			    ]
			}, {
				layout : "column",
				autoHeight : true, 
				autoWidth : true,
				border : false,
				anchor : '98%',
				items : [
				    {
				    	layout : "form",
						border : false,
						labelSeparator : '',
						columnWidth : .10,
						items : [
						    {
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
											Ext.getCmp("cashierPwd").setValue("");
											Ext.getCmp("cashierPwd").enable();
										} else {
											thiz.setIconClass("lock");
											cashierLock = "YES";
											Ext.getCmp("cashierPwd").setValue("******");
											Ext.getCmp("cashierPwd").disable();
										}
									}
								}
						    }
						]
				    }, {
				    	layout : "form",
						border : false,
						labelSeparator : '：',
						labelWidth : 100,
						columnWidth : .45,
						items : [
						    {
						    	xtype : "textfield",
								fieldLabel : "收银员权限密码",
								id : "cashierPwd",
								width : 120
							}
						]
				    }
				]
			}, {
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
						items : [
						    {
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
											Ext.getCmp("orderCancelPwd").setValue("");
											Ext.getCmp("orderCancelPwd").enable();
										} else {
											thiz.setIconClass("lock");
											orderCancelLock = "YES";
											Ext.getCmp("orderCancelPwd").setValue("******");
											Ext.getCmp("orderCancelPwd").disable();
										}
									}
								}
						    }
						]
				    }, {
				    	layout : "form",
						border : false,
						labelSeparator : '：',
						labelWidth : 100,
						columnWidth : .45,
						items : [
						    {
						    	xtype : "textfield",
								fieldLabel : "退菜权限密码",
								id : "orderCancelPwd",
								width : 120
							}
						]
				    }
				]
			}, {
				html : '<div style="margin-top:4px"><font id="errorMsgChangePwd" style="color:red;"> </font></div>'
			}
		]
	}
);

var passwordConfigWin = new Ext.Window({
	layout : "fit",
	title : "权限密码设置",
	width : 550,
	height : 220,
	closeAction : "hide",
	closable : false,
	modal : true,
	resizable : false,
	items : passwordConfigForm,
	bbar : [
	    '->',
	    {
	    	text : "保存",
	    	iconCls : 'btn_save',
	    	handler : function() {
	    		
	    		var adminPwd = Ext.getCmp("adminPwd").getValue();
				var adminPwdConfirm = Ext.getCmp("adminPwdConfirm").getValue();
				var financePwd = Ext.getCmp("financePwd").getValue();
				var managerPwd = Ext.getCmp("managerPwd").getValue();
				var cashierPwd = Ext.getCmp("cashierPwd").getValue();
				var orderCancelPwd = Ext.getCmp("orderCancelPwd").getValue();

				// 2, save the passwords
				if (Ext.getCmp("adminPwd").isValid() && Ext.getCmp("adminPwdConfirm").isValid()) {
					if (adminPwd == adminPwdConfirm) {
						if (adminLock == "YES" && financeLock == "YES"
								&& managerLock == "YES" && cashierLock == "YES"
								&& orderCancelLock == "YES") {
							
							Ext.getDom("errorMsgChangePwd").innerHTML = "密码并未修改";
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
							
							Ext.Ajax.request({
								url : "../../SetPassword.do",
								params : {
									"pin" : pin,
									"adminPwd" : adminPwd,
									"financePwd" : financePwd,
									"managerPwd" : managerPwd,
									"cashierPwd" : cashierPwd,
									"orderCancelPwd" : orderCancelPwd
								},
								success : function(response, options) {
									var resultJSON = Ext.util.JSON.decode(response.responseText);
									if (resultJSON.success == true) {
										var dataInfo = resultJSON.data;
										Ext.MessageBox.show({
											msg : dataInfo,
											width : 300,
											buttons : Ext.MessageBox.OK
										});
									} else {
										var dataInfo = resultJSON.data;
										Ext.MessageBox.show({
											msg : dataInfo,
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
						}
					} else {
						Ext.getDom("errorMsgChangePwd").innerHTML = "确认密码不一致";
					}
				}
			}
	    }, {
	    	text : "关闭",
	    	iconCls : 'btn_close',
			handler : function() {
				passwordConfigWin.hide();
			}
	    }
	],
	listeners : {
		"show" : function() {
			Ext.getCmp("adminPwd").setValue("111111");
			Ext.getCmp("adminPwdConfirm").setValue("111111");
			Ext.getCmp("adminPwd").clearInvalid();
			Ext.getCmp("adminPwdConfirm").clearInvalid();

			Ext.getCmp("financePwd").setValue("******");
			Ext.getCmp("managerPwd").setValue("******");
			Ext.getCmp("cashierPwd").setValue("******");
			Ext.getCmp("orderCancelPwd").setValue("******");

			Ext.getCmp("adminPwd").disable();
			Ext.getCmp("adminPwdConfirm").disable();
			Ext.getCmp("financePwd").disable();
			Ext.getCmp("managerPwd").disable();
			Ext.getCmp("cashierPwd").disable();
			Ext.getCmp("orderCancelPwd").disable();

			Ext.getCmp("adminLock").setIconClass("lock");
			Ext.getCmp("financeLock").setIconClass("lock");
			Ext.getCmp("managerLock").setIconClass("lock");
			Ext.getCmp("cashierLock").setIconClass("lock");
			Ext.getCmp("orderCancelLock").setIconClass("lock");
			
			adminLock = "YES";
			financeLock = "YES";
			managerLock = "YES";
			cashierLock = "YES";
			orderCancelLock = "YES";
			
			Ext.getDom("errorMsgChangePwd").innerHTML = "";
		}
	}
});

var formatPrice = new Ext.Window({
	title : '收款金额尾数处理方式设置',
	modal : true,
	resizable : false,
	closable : false,
	width : 260,
	items : [
	    {
	    	xtype : 'panel',
	    	layout : 'column',
	    	frame : true,
	    	defaults : {
	    		xtype : 'panel',
	    		layout : 'form',
	    	},
	    	items : [
	    		{
	    			columnWidth : .38,
	    			items : [{
						xtype : 'radio',
						id : 'rdoFormatTypeNOACTION',
						hideLabel : true,
						name : 'rdoFormatType',
						inputValue : 0,
						boxLabel : '不处理',
						listeners : {
							render : function(e){
								Ext.getDom('rdoFormatTypeNOACTION').onclick = function(){
									e.setValue(true);
									formatPrice.priceTail = e.inputValue;
								};
							}
						}
					}]
	    		}, {
	    			columnWidth : .30,
	    			items : [{
						xtype : 'radio',
						id : 'rdoFormatTypeDECIMALCUT',
						hideLabel : true,
						name : 'rdoFormatType',
						inputValue : 1,
						boxLabel : '抹零',
						listeners : {
							render : function(e){
								Ext.getDom('rdoFormatTypeDECIMALCUT').onclick = function(){
									e.setValue(true);
									formatPrice.priceTail = e.inputValue;
								};
							}
						}
					}]
	    		}, {
	    			columnWidth : .32,
	    			items : [{
						xtype : 'radio',
						id : 'rdoFormatTypeDECIMALROUND',
						hideLabel : true,
						name : 'rdoFormatType',
						inputValue : 2,
						boxLabel : '四舍五入',
						checked : true,
						listeners : {
							render : function(e){
								Ext.getDom('rdoFormatTypeDECIMALROUND').onclick = function(){
									e.setValue(true);
									formatPrice.priceTail = e.inputValue;
								};
							}
						}
					}]
	    		}
	    	]
	    }	      
	],
	bbar : [
	    '->',
		{
			text : '保存',
			id : 'btnSavePriceTail',
			iconCls : 'btn_save',
			handler : function(e){
//				alert(formatPrice.priceTail);
				Ext.getCmp('btnSavePriceTail').setDisabled(true);
				Ext.getCmp('btnClosePriceTail').setDisabled(true);
				Ext.Ajax.request({
					url : '../../UpdatePriceTail.do',
					params : {
						restaurantID : restaurantID,
						priceTail : formatPrice.priceTail
					},
					success : function(response, options){
						var jr = Ext.util.JSON.decode(response.responseText);
						if(jr.success){
							Ext.example.msg(jr.title, jr.msg);
							formatPrice.hide();
						}else{
							Ext.ux.showMsg(jr);
						}
						Ext.getCmp('btnSavePriceTail').setDisabled(false);
						Ext.getCmp('btnClosePriceTail').setDisabled(false);
					},
					failure : function(response, options) {
						Ext.getCmp('btnSavePriceTail').setDisabled(false);
						Ext.getCmp('btnClosePriceTail').setDisabled(false);
						var jr = Ext.util.JSON.decode(response.responseText);
						Ext.ux.showMsg(jr);
					}
				});
			}
		}, {
			text : '关闭',
			id : 'btnClosePriceTail',
			iconCls : 'btn_close',
			handler : function(e){
				formatPrice.hide();
			}
		}
	],
	listeners : {
		show : function(){
			Ext.Ajax.request({
				url : '../../QuerySystemSetting.do',
				params : {
					restaurantID : restaurantID
				},
				success : function(response, options){
					var jr = Ext.util.JSON.decode(response.responseText);
					
					if(jr.success){
						var setting = jr.root[0];
						if(setting.priceTail == 0){
							Ext.getDom('rdoFormatTypeNOACTION').onclick();
						}else if(setting.priceTail == 1){
							Ext.getDom('rdoFormatTypeDECIMALCUT').onclick();
						}else if(setting.priceTail == 2){
							Ext.getDom('rdoFormatTypeDECIMALROUND').onclick();
						}
					}else{
						Ext.ux.showMsg(jr);
					}
				},
				failure : function(response, options) {
					var jr = Ext.util.JSON.decode(response.responseText);
					Ext.ux.showMsg(jr);
				}
			});
		}
	}
});

// ---------------------------------------------------------------------------
Ext.onReady(function() {
	Ext.lib.Ajax.defaultPostHeader += '; charset=utf-8';
	Ext.QuickTips.init();

	// ******************************************************************************************************
	var pushBackBut = new Ext.ux.ImageButton({
		imgPath : "../../images/UserLogout.png",
		imgWidth : 50,
		imgHeight : 50,
		tooltip : "返回",
		handler : function(btn) {
			location.href = "../PersonLogin.html?restaurantID=" + restaurantID + "&isNewAccess=false&pin=" + pin;
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
			items : [
			    "->", 
			    pushBackBut, 
			    {
			    	text : "&nbsp;&nbsp;&nbsp;",
					disabled : true
				},
				logOutBut
			]
		}),
		items : [
		    {
		    	border : false,
				contentEl : "protal"
			}
		]
	});
	
	new Ext.Viewport({
		layout : "border",
		id : "viewport",
		items : [
		    {
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
				border : false,
				html : "<div style='font-size:11pt; text-align:center;'><b>版权所有(c) 2011 智易科技</b></div>"
			}
		]
	});
});
