// ******************************************************************************************************
// shiftWin
var shiftPanel = new Ext.Panel({
	frame : true,
	items : [ {
		border : false,
		contentEl : "shiftDiv"
	} ]
});

var shiftWin = new Ext.Window({
	layout : "fit",
	width : 450,
	height : 450,
	closeAction : "hide",
	resizable : false,
	items : shiftPanel,
	buttons : [
			{
				text : "确定",
				handler : function() {
					// show the window
					shiftWin.hide();
				
					isPrompt = false;

					// do shift
					// alert(shiftStartTiem + " , " + shiftEndTiem);
					Ext.Ajax.request({
						url : "../DoShift.do",
						params : {
							"pin" : currPin,
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
				text : "打印",
				disabled : true,
				handler : function() {
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
				text : "交班",
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

Ext
		.onReady(function() {
			// 解决ext中文传入后台变问号问题
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

			var staffForm = new Ext.form.FormPanel(
					{
						layout : "form",
						id : "staffFrom",
						frame : true,
						labelSeparator : "：",
						style : "margin:0 auto",
						title : "<div style='font-size:18px;padding-left:2px'>员工登陆<div>",
						collapsible : false,
						buttonAlign : "center",
						labelWidth : 60,
						width : 280,
						height : 140,
						defaults : {
							width : 200
						},
						items : [
								{
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
								},
								{
									xtype : "textfield",
									inputType : "password",
									fieldLabel : "<img src='../images/password.png' style='float:left'/>&nbsp;密码",
									id : "empPassword"
								} ],
						buttons : [
								{
									text : '提交',
									handler : function() {
										if (staffForm.getForm().isValid()) {
											// check the password
											var pin = staffForm.findById(
													"empName").getValue();
											var password = "";
											for ( var i = 0; i < emplData.length; i++) {
												if (emplData[i][0] == pin) {
													password = emplData[i][2];
												}
											}
											var passwordInput = staffForm
													.findById("empPassword")
													.getValue();

											var pwdTrans;
											if (passwordInput != "") {
												pwdTrans = MD5(passwordInput);
											} else {
												pwdTrans = passwordInput;
											}
											if (password == pwdTrans) {

												// location.href =
												// "TableSelect.html?pin="
												// + pin
												// + "&restaurantID="
												// + restaurantID;
												currPin = pin;
												getOperatorName(currPin);
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
								} ]
					});

			// person login pop window
			personLoginWin = new Ext.Window({
				layout : "fit",
				width : 300,
				height : 160,
				closeAction : "hide",
				resizable : false,
				closable : false,
				items : staffForm
			});

			// ******************************************************************************************************

			var centerPanel = new Ext.Panel({
				region : "center",
				frame : true,
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
									// html : "<div style='padding:10px;
									// background-color:#A9D0F5'><h4
									// style='font-size:150%'>无线点餐网页终端<h4><div
									// style='float:right;background:
									// url(../images/UserLogout.png) no-repeat
									// 50%;'></div></div>",
									bodyStyle : "background-color:#A9D0F5",
									// html : "<h4
									// style='padding:10px;font-size:150%;float:left;'>无线点餐网页终端</h4><div
									// style='float:right;width:50px;height:50px;background:url(../images/UserLogout.png)
									// no-repeat 50%;'></div>",
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
