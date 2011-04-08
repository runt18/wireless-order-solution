﻿var dishOrderCurrRowIndex_ = 0;

Ext.onReady(function() {
	// 解决ext中文传入后台变问号问题
		Ext.lib.Ajax.defaultPostHeader += '; charset=utf-8';
		Ext.QuickTips.init();

		// ***************tableSelectNorthPanel******************
		var tableSelectNorthPanel = new Ext.form.FormPanel( {
			region : "north",
			frame : true,
			height : 50,
			labelSeparator : '：',
			labelWidth : 30,
			border : false,
			items : [ {
				border : false,
				layout : "form",
				style : "padding-top:10px;padding-left:20px;",
				items : [ {
					xtype : "textfield",
					border : false,
					fieldLabel : "桌号"
				} ]
			} ]
		});

		// ***************tableSelectCenterPanel******************
		var tableSelectCenterPanel = new Ext.Panel( {
			region : "center",
			contentEl : "tableDisplay"
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
								html : "<div style='padding:40px; background-color:#A9D0F5'><h1 style='font-size:200%'>无线点餐网页终端<h1></div>",
								height : 100,
								margins : '0 0 5 0'
							}, centerTabPanel ]
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
