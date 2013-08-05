//// ******************************************************************************************************

// menuVerifyWin
var menuVerifyWin = new Ext.Window({
	layout : "fit",
	width : 200,
	height : 100,
//	closeAction : "hide",
	resizable : false,
	closable : false,
	draggable : false,
	modal : true,
	constrainHeade : true,			
	items : [{
		layout : "form",
		labelWidth : 30,
		border : false,
		frame : true,
		items : [{
			xtype : "textfield",
			inputType : "password",
			fieldLabel : "密码",
			id : "menuVerifyPwd",
			width : 110
		}]
	}],
	buttons : [{
		text : "确定",
		id : 'btnMenuVerifyWinSubmit',
		handler : function() {
			var menuVerifyPwd = menuVerifyWin.findById("menuVerifyPwd").getValue();
			menuVerifyWin.findById("menuVerifyPwd").setValue("");
			
			var pwdTrans;
			if (menuVerifyPwd != "") {
				pwdTrans = MD5(menuVerifyPwd);
			} else {
				pwdTrans = menuVerifyPwd;
			}
			
			menuVerifyWin.hide();
			isPrompt = false;
			menuVerifyWin.findById("menuVerifyPwd").setValue("");

			Ext.Ajax.request({
				url : "../VerifyPwd.do",
				params : {
					"pin" : currPin,
					"type" : "3",
					"pwd" : pwdTrans
				},
				success : function(response, options) {
					var resultJSON = Ext.util.JSON.decode(response.responseText);
					if (resultJSON.success == true) {
						location.href = "BasicManagement_Module/BasicMgrProtal.html?pin=" + currPin
										+ "&restaurantID=" + restaurantID;
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
			menuVerifyWin.hide();
			isPrompt = false;
			menuVerifyWin.findById("menuVerifyPwd").setValue("");
		}
	}],
	listeners : {
		show : function(thiz) {
			var f = Ext.get("menuVerifyPwd");
			f.focus.defer(100, f); // 万恶的EXT！为什么这样才可以！？！？
		}
	},
	keys : [{
		key : Ext.EventObject.ENTER,
		fn : function(){
			Ext.getCmp('btnMenuVerifyWinSubmit').handler(); 
		},
		scope : this 
	}]
});

// historyVerifyWin
var historyVerifyWin = new Ext.Window({
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
			id : "historyVerifyPwd",
			width : 110
		} ]
	} ],
	buttons : [{
		text : "确定",
		id : 'btnHistoryVerifyWinSubmit',
		handler : function() {				
			var historyVerifyPwd = historyVerifyWin.findById("historyVerifyPwd").getValue();
			historyVerifyWin.findById("historyVerifyPwd").setValue("");				
							
			var pwdTrans;
			if (historyVerifyPwd != "") {
				pwdTrans = MD5(historyVerifyPwd);
			} else {
				pwdTrans = historyVerifyPwd;
			}
			
			historyVerifyWin.hide();
			isPrompt = false;
			Ext.Ajax.request({
				url : "../VerifyPwd.do",
				params : {
					"pin" : currPin,
					"type" : "2",
					"pwd" : pwdTrans
				},
				success : function(response, options) {
					var resultJSON = Ext.util.JSON
							.decode(response.responseText);
					if (resultJSON.success == true) {
						location.href = 'History_Module/HistoryStatistics.html?pin='
							+ currPin 
							+ '&restaurantID='
							+ restaurantID;
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
			historyVerifyWin.hide();
			isPrompt = false;
			historyVerifyWin.findById("historyVerifyPwd")
					.setValue("");
		}
	} ],
	listeners : {
		show : function(thiz) {
			var f = Ext.get("historyVerifyPwd");
			f.focus.defer(100, f); 
		}
	},
	keys : [{
		key : Ext.EventObject.ENTER,
		fn : function(){Ext.getCmp('btnHistoryVerifyWinSubmit').handler(); },
		scope : this 
	}]
});

// inventoryVerifyWin
var inventoryVerifyWin = new Ext.Window({
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
			id : "inventoryVerifyPwd",
			width : 110
		} ]
	} ],
	buttons : [{
		text : "确定",
		id : 'btnInventoryVerifyWinSubmit',
		handler : function() {
			var inventoryVerifyPwd = inventoryVerifyWin.findById("inventoryVerifyPwd").getValue();
			inventoryVerifyWin.findById("inventoryVerifyPwd").setValue("");
			var pwdTrans;
			if (inventoryVerifyPwd != "") {
				pwdTrans = MD5(inventoryVerifyPwd);
			} else {
				pwdTrans = inventoryVerifyPwd;
			}
			inventoryVerifyWin.hide();
			isPrompt = false;
			inventoryVerifyWin.findById("inventoryVerifyPwd").setValue("");			
			
			Ext.Ajax.request({
				url : "../VerifyPwd.do",
				params : {
					"pin" : currPin,
					"type" : "3",
					"pwd" : pwdTrans
				},
				success : function(response, options) {
					var resultJSON = Ext.util.JSON
							.decode(response.responseText);
					if (resultJSON.success == true) {
						location.href = "InventoryManagement_Module/InventoryProtal.html?pin="
								+ currPin
								+ "&restaurantID="
								+ restaurantID;
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
			inventoryVerifyWin.hide();
			isPrompt = false;
			inventoryVerifyWin.findById("inventoryVerifyPwd")
					.setValue("");
		}
	} ],
	listeners : {
		show : function(thiz) {
			var f = Ext.get("inventoryVerifyPwd");
			f.focus.defer(100, f); 
		}
	},
	keys : [{
		key : Ext.EventObject.ENTER,
		fn : function(){Ext.getCmp('btnInventoryVerifyWinSubmit').handler(); },
		scope : this 
	}]
});

//systemVerifyWin
var systemVerifyWin = new Ext.Window({
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
			id : "systemVerifyPwd",
			width : 110
		} ]
	} ],
	buttons : [{
		text : "确定",
		id : 'btnSystemVerifyWinSubmit',
		handler : function() {
			var systemVerifyPwd = systemVerifyWin.findById("systemVerifyPwd").getValue();
			systemVerifyWin.findById("systemVerifyPwd").setValue("");
			var pwdTrans;
			if (systemVerifyPwd != "") {
				pwdTrans = MD5(systemVerifyPwd);
			} else {
				pwdTrans = systemVerifyPwd;
			}
			systemVerifyWin.hide();
			isPrompt = false;
			systemVerifyWin.findById("systemVerifyPwd").setValue("");				
			
			Ext.Ajax.request({
				url : "../VerifyPwd.do",
				params : {
					"pin" : currPin,
					"type" : "1",
					"pwd" : pwdTrans
				},
				success : function(response, options) {
					var resultJSON = Ext.util.JSON
							.decode(response.responseText);
					if (resultJSON.success == true) {
						location.href = "System_Module/SystemProtal.html?pin="
							+ currPin + "&restaurantID="
							+ restaurantID;
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
			systemVerifyWin.hide();
			isPrompt = false;
			systemVerifyWin.findById("systemVerifyPwd")
					.setValue("");
		}
	}],
	listeners : {
		show : function(thiz) {
			var f = Ext.get("systemVerifyPwd");
			f.focus.defer(100, f); 
		}
	},
	keys : [{
		key : Ext.EventObject.ENTER,
		fn : function(){Ext.getCmp('btnSystemVerifyWinSubmit').handler(); },
		scope : this 
	}]
});
// memberVerifyWin
var memberVerifyWin = new Ext.Window({
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
			id : "memberVerifyPwd",
			width : 110
		} ]
	} ],
	buttons : [{
		text : "确定",
		id : 'btnMemberVerifyWinSubmit',
		handler : function() {
			var memberVerifyPwd = Ext.getCmp("memberVerifyPwd").getValue();
			var pwdTrans;
			if (memberVerifyPwd != "") {
				pwdTrans = MD5(memberVerifyPwd);
			} else {
				pwdTrans = memberVerifyPwd;
			}
			memberVerifyWin.hide();
			isPrompt = false;
			Ext.getCmp("memberVerifyPwd").setValue();				
			
			Ext.Ajax.request({
				url : "../VerifyPwd.do",
				params : {
					"pin" : currPin,
					"type" : 3,
					"pwd" : pwdTrans
				},
				success : function(response, options) {
					var resultJSON = Ext.util.JSON.decode(response.responseText);
					if (resultJSON.success == true) {
						window.location.href = 'Client_Module/ClientMain.html?'
							+ 'pin=' + currPin
							+ '&restaurantID=' + restaurantID;
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
			memberVerifyWin.hide();
			isPrompt = false;
			Ext.getCmp("memberVerifyPwd").setValue("");
		}
	}],
	listeners : {
		show : function(thiz) {
			var f = Ext.get("memberVerifyPwd");
			f.focus.defer(100, f); 
		}
	},
	keys : [{
		key : Ext.EventObject.ENTER,
		fn : function(){Ext.getCmp('btnMemberVerifyWinSubmit').handler(); },
		scope : this 
	}]
});
var staffForm;
Ext.onReady(function() {
	Ext.lib.Ajax.defaultPostHeader += '; charset=utf-8';
	Ext.QuickTips.init();
	
	emplComboData = [ [ "XXX", "XXX" ] ];
	emplStore = new Ext.data.Store({
		proxy : new Ext.data.MemoryProxy(emplComboData),
		reader : new Ext.data.ArrayReader({}, [ {
			name : "value"
		}, {
			name : "text"
		} ])
	});
	emplStore.load();

	staffForm = new Ext.form.FormPanel({
		layout : "form",
		id : "staffFrom",
		frame : true,
		labelSeparator : "：",
		style : "margin:0 auto",
		collapsible : false,
		buttonAlign : "center",
		labelWidth : 60,
//		width : 280,
//		height : 140,
		defaults : {
			width : 200
		},
		items : [{
			xtype : "combo",
			fieldLabel : "<img src='../images/user.png'/ style='float:left'>&nbsp;姓名",
			id : "empName",
			forceSelection : true,
			store : emplStore,
			valueField : "value",
			displayField : "text",
			typeAhead : true,
			mode : "local",
			triggerAction : "all",
			selectOnFocus : true,
			blankText : '请选择一位员工',
			emptyText : '请选择',
			allowBlank : false
		}, {
			xtype : "textfield",
			inputType : "password",
			fieldLabel : "<img src='../images/password.png' style='float:left'/>&nbsp;密码",
			id : "empPassword"
		}],
		buttons : [ {
			text : '提交',
			id : 'btnStaffFormSubmit',
			handler : function() {
				if (staffForm.getForm().isValid()) {
					var pin = staffForm.findById("empName").getValue();
					var password = "";
					for ( var i = 0; i < emplData.length; i++) {
						if (emplData[i][0] == pin) {
							password = emplData[i][2];
						}
					}
					var passwordInput = staffForm.findById("empPassword").getValue();
					var pwdTrans;
					pwdTrans = MD5(passwordInput);
					if (password == pwdTrans) {
						currPin = pin;
						getOperatorName(currPin, "../");
						isVerified = true;
						personLoginWin.hide();
						personLoginWin.findById(
								"empPassword")
								.setValue("");
					} else {
						isVerified = false;
						Ext.MessageBox.show({
							msg : "姓名或密码错误！",
							width : 300,
							buttons : Ext.MessageBox.OK
						});
					}
				}
			}
		}, {
			text : '重置',
			handler : function() {
				staffForm.getForm().reset();
			}
		}]
	});

	// person login pop window
	personLoginWin = new Ext.Window({
		title : "<div style='font-size:18px;text-align:center;width:288px;'>员工登陆<div>",
		layout : "fit",
		width : 300,
		height : 150,
		resizable : false,
		closable : false,
		modal : true,
		constrainHeade : true,
		draggable : false,
		items : staffForm,
		keys : [
		 {
			 key : Ext.EventObject.ENTER,
			 fn : function(){Ext.getCmp('btnStaffFormSubmit').handler(); },
			 scope : this 
		 },{
			 key : Ext.EventObject.ESC,
			 fn : function(){ staffForm.getForm().reset(); },
			 scope : this 
		 }]
	});

	// ******************************************************************************************************
	var centerPanel = new Ext.Panel({
		region : "center",
		frame : true,
		autoScroll : true,
		items : [ {
			border : false,
			contentEl : "protal"
		} ]
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
			border : false,
			html : '<div style="font-size:11pt; text-align:center;"><b>版权所有(c) 2011 智易科技</b>&nbsp;&nbsp;<a href="javascript:downloadUtil()" style="font-size:12px;">工具下载</a></div>'
		}]
	 });
});

function downloadUtil(){
	var downloadUrilWin = Ext.getCmp('downloadUrilWin');
	if(!downloadUrilWin){
		var urls = [
		    '<table border="1" class="tb_base">',
		    '<tr>',
			    '<th class="table_title text_center" style="width:150px;">工具名</th>',
			    '<th class="table_title text_center">操作</th>',
		    '</tr>',
		    '<tr>',
			    '<td>打印服务</td>',
			    '<td class="text_center"><a href="http://e-tones.net:10080/pserver/pserver.exe">下载</a></td>',
		    '</tr>',
		    '<tr>',
			    '<td>打印机驱动</td>',
			    '<td class="text_center"><a href="http://www.gainscha.com.cn/pic.asp?id=191840056">下载</a></td>',
		    '</tr>',
		    '</table>'
		];
		downloadUrilWin = new Ext.Window({
			title : '工具下载',
			modal : true,
			closable : false,
			resize : false,
			width : 277,
			height : Ext.isIE ? 132 : 135,
			html : urls.join(''),
			keys : [{
				key : Ext.EventObject.ESC,
				sopce : this,
				fn : function(){
					downloadUrilWin.hide();
				}
			}],
			bbar : ['->', {
				text : '关闭',
				iconCls : 'btn_close',
				handler : function(){
					downloadUrilWin.hide();
				}
			}]
		});
	}
	downloadUrilWin.show();
}