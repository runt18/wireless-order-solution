var dishOrderCurrRowIndex_ = 0;

Ext.onReady( function() {
	// 解决ext中文传入后台变问号问题
		Ext.lib.Ajax.defaultPostHeader += '; charset=utf-8';
		Ext.QuickTips.init();

		// ***************tableSelectNorthPanel******************
		var tableSelectNorthPanel = new Ext.form.FormPanel( {
			region : "north",
			//frame : true,
			//height : 35,
			height : 60,
			style:"padding-top:10px;padding-left:10px;",
			labelSeparator : '：',
			labelWidth : 60,
			border : false,
			items : [ {
				xtype : "textfield",
				border : false,
				fieldLabel : "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;桌号"
			} ]
		});

		var tableSelectCenterPanel = new Ext.Panel( {
			region : "center"
		});

		// *************整体布局*************
		var centerTabPanel = new Ext.Panel(
				{
					region : "center",
					tbar : [
							{
								text : "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;点菜&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;"
							},
							{
								text : "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;结账&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;"
							} ],
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
