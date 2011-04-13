﻿var dishOrderCurrRowIndex_ = 0;

Ext.onReady(function() {
	// 解决ext中文传入后台变问号问题
		Ext.lib.Ajax.defaultPostHeader += '; charset=utf-8';
		Ext.QuickTips.init();

		// ***************tableSelectNorthPanel******************
		var softKeyBoard = new Ext.Window( {
			layout : "fit",
			width : 177,
			height : 100,
			closeAction : "hide",
			resizable : false,
			x : 41,
			y : 146,
			items : [ {
				layout : "form",
				labelSeparator : '：',
				labelWidth : 40,
				frame : true,
				buttonAlign : "left",
				items : [
				// {
				// xtype : "numberfield",
				// fieldLabel : "桌号",
				// width : 85
				// },
				{
					layout : "column",
					border : false,
					items : [
							{
								layout : "form",
								width : 30,
								border : false,
								items : [ {
									text : "1",
									xtype : "button",
									handler : function() {
										var currValue = tableSelectNorthPanel
												.findById("tableNumber")
												.getValue();
										tableSelectNorthPanel.findById(
												"tableNumber").setValue(
												currValue + "1");
									}
								} ]
							},
							{
								layout : "form",
								width : 30,
								border : false,
								items : [ {
									text : "2",
									xtype : "button",
									handler : function() {
										var currValue = tableSelectNorthPanel
												.findById("tableNumber")
												.getValue();
										tableSelectNorthPanel.findById(
												"tableNumber").setValue(
												currValue + "2");
									}
								} ]
							},
							{
								layout : "form",
								width : 30,
								border : false,
								items : [ {
									text : "3",
									xtype : "button",
									handler : function() {
										var currValue = tableSelectNorthPanel
												.findById("tableNumber")
												.getValue();
										tableSelectNorthPanel.findById(
												"tableNumber").setValue(
												currValue + "3");
									}
								} ]
							},
							{
								layout : "form",
								width : 30,
								border : false,
								items : [ {
									text : "4",
									xtype : "button",
									handler : function() {
										var currValue = tableSelectNorthPanel
												.findById("tableNumber")
												.getValue();
										tableSelectNorthPanel.findById(
												"tableNumber").setValue(
												currValue + "4");
									}
								} ]
							},
							{
								layout : "form",
								width : 30,
								border : false,
								items : [ {
									text : "5",
									xtype : "button",
									handler : function() {
										var currValue = tableSelectNorthPanel
												.findById("tableNumber")
												.getValue();
										tableSelectNorthPanel.findById(
												"tableNumber").setValue(
												currValue + "5");
									}
								} ]
							},
							{
								layout : "form",
								width : 30,
								border : false,
								items : [ {
									text : "6",
									xtype : "button",
									handler : function() {
										var currValue = tableSelectNorthPanel
												.findById("tableNumber")
												.getValue();
										tableSelectNorthPanel.findById(
												"tableNumber").setValue(
												currValue + "6");
									}
								} ]
							},
							{
								layout : "form",
								width : 30,
								border : false,
								items : [ {
									text : "7",
									xtype : "button",
									handler : function() {
										var currValue = tableSelectNorthPanel
												.findById("tableNumber")
												.getValue();
										tableSelectNorthPanel.findById(
												"tableNumber").setValue(
												currValue + "7");
									}
								} ]
							},
							{
								layout : "form",
								width : 30,
								border : false,
								items : [ {
									text : "8",
									xtype : "button",
									handler : function() {
										var currValue = tableSelectNorthPanel
												.findById("tableNumber")
												.getValue();
										tableSelectNorthPanel.findById(
												"tableNumber").setValue(
												currValue + "8");
									}
								} ]
							},
							{
								layout : "form",
								width : 30,
								border : false,
								items : [ {
									text : "9",
									xtype : "button",
									handler : function() {
										var currValue = tableSelectNorthPanel
												.findById("tableNumber")
												.getValue();
										tableSelectNorthPanel.findById(
												"tableNumber").setValue(
												currValue + "9");
									}
								} ]
							},
							{
								layout : "form",
								width : 30,
								border : false,
								items : [ {
									text : "0",
									xtype : "button",
									handler : function() {
										var currValue = tableSelectNorthPanel
												.findById("tableNumber")
												.getValue();
										tableSelectNorthPanel.findById(
												"tableNumber").setValue(
												currValue + "0");
									}
								} ]
							},
							{
								layout : "form",
								width : 60,
								border : false,
								items : [ {
									text : "&nbsp;确 认&nbsp;",
									xtype : "button",
									handler : function() {
										softKeyBoard.hide();
									}
								} ]
							},
							{
								layout : "form",
								width : 60,
								border : false,
								items : [ {
									text : "&nbsp;清 空&nbsp;",
									xtype : "button",
									handler : function() {
										tableSelectNorthPanel.findById(
												"tableNumber").setValue("");
									}
								} ]
							} ]
				} ]
			// ,
			// buttons : [ {
			// text : "确定"
			// }, {
			// text : "取消"
			// } ]
			} ],
			listeners : {
				show : function(thiz) {
					// tableSelectNorthPanel.findById("tableNumber").focus(true);
			// document.getElementById("tableNumber").focus();
		}
	}
		});

		var tableSelectNorthPanel = new Ext.form.FormPanel( {
			region : "north",
			frame : true,
			height : 45,
			labelSeparator : '：',
			labelWidth : 30,
			border : false,
			items : [ {
				border : false,
				layout : "form",
				// style : "padding-top:10px;padding-left:20px;",
				items : [ {
					layout : "column",
					border : false,
					anchor : '98%',
					labelSeparator : '：',
					items : [ {
						layout : "form",
						width : 237,
						labelWidth : 30,
						style : "padding-top:7px;",
						border : false,
						items : [ {
							xtype : "numberfield",
							fieldLabel : "<b>桌号</b>",
							name : "tableNumber",
							id : "tableNumber",
							anchor : "90%",
							listeners : {
								focus : function(thiz) {
									softKeyBoard.show();
								}
							}
						} ]
					},
					// {
							// width : 80,
							// border : false,
							// // layout : "fit",
							// style : "padding-top:7px;",
							// items : [ {
							// xtype : "button",
							// // style : "margin-left:7px;",
							// tooltip : "软键盘",
							// width : 30,
							// text : "K",
							// listeners : {
							// "click" : function() {
							// softKeyBoard.show();
							// }
							// }
							// } ]
							// },
							{
								width : 800,
								contentEl : "tableStatusTS"
							} ]
				} ]
			} ]
		});

		// ***************tableSelectCenterPanel******************
		var tableSelectCenterPanel = new Ext.Panel( {
			region : "center",
			layout : "fit",
			bodyStyle : "background-color:#d8ebef;padding-top:4%",
			contentEl : "tableDisplay",
			autoScroll : true
		});

		// *************整体布局*************
		var dishesOrderImgBut = new Ext.ux.ImageButton( {
			imgPath : "../images/im48x48.png",
			imgWidth : 50,
			imgHeight : 50,
			tooltip : "点菜",
			handler : function(btn) {
				Ext.MessageBox.alert("test", "点菜");
			}
		});

		var checkOutImgBut = new Ext.ux.ImageButton( {
			imgPath : "../images/extlogo48.png",
			imgWidth : 50,
			imgHeight : 50,
			tooltip : "结账",
			handler : function(btn) {
				Ext.MessageBox.alert("test", "结账");
			}
		});

		var orderDeleteImgBut = new Ext.ux.ImageButton( {
			imgPath : "../images/im48x48.png",
			imgWidth : 50,
			imgHeight : 50,
			tooltip : "删单",
			handler : function(btn) {
				Ext.MessageBox.alert("test", "删单");
			}
		});

		var centerTabPanel = new Ext.Panel( {
			region : "center",
			tbar : new Ext.Toolbar( {
				height : 55,
				items : [ {
					text : " ",
					disabled : true
				}, dishesOrderImgBut, {
					text : "&nbsp;&nbsp;&nbsp;",
					disabled : true
				}, checkOutImgBut, dishesOrderImgBut, {
					text : "&nbsp;&nbsp;&nbsp;",
					disabled : true
				}, orderDeleteImgBut ]
			}),
			layout : "border",
			border : false,
			items : [ tableSelectNorthPanel, tableSelectCenterPanel ]
		});

		var viewport = new Ext.Viewport(
				{
					layout : "border",
					items : [
							{
								region : "north",
								html : "<div style='padding:10px; background-color:#A9D0F5'><h4 style='font-size:150%'>无线点餐网页终端<h4></div>",
								height : 50,
								margins : '0 0 5 0'
							},
							centerTabPanel,
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
		// 1,调整colDisplayFormUQ中表格的高度
		// Ext.EventManager.onWindowResize( function() {
		// if (centerTabPanel.getActiveTab().getId() == "dishesOrderTab") {
		// dishesDisplayGrid.setHeight(dishesOrderEastPanel.findById(
		// "dishesOrderForm").getInnerHeight() - 50);
		// }
		// // dataSortGridUQ.setHeight(150);
		// });
	});
