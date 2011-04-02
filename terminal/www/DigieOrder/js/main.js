var dishOrderCurrRowIndex_ = 0;

Ext.onReady(function() {
	// 解决ext中文传入后台变问号问题
		Ext.lib.Ajax.defaultPostHeader += '; charset=utf-8';
		Ext.QuickTips.init();

		// *************整体布局*************
		var centerTabPanel = new Ext.TabPanel(
				{
					id : "centerPanel",
					region : "center",
					border : false,
					layoutOnTabChange : true,
					activeTab : 0,
					tabPosition : "top",
					margins : '0 5 0 0',
					items : [
							{
								title : "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;点菜&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;",
								layout : "border",
								items : [ dishesOrderCenterPanel,
										dishesOrderEastPanel,
										dishesOrderNorthPanel ]
							},
							{
								title : "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;结账&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;"
							} ]
				});

		var viewport = new Ext.Viewport( {
			layout : "border",
			items : [ {
				region : "north",
				// html : "<img src=\"../images/img-6.jpg\"/>",
				height : 100,
				margins : '0 0 5 0'
			}, centerTabPanel ]
		});

		// -------------------- 浏览器大小改变 -------------------------------
		// 1,调整colDisplayFormUQ中表格的高度
		Ext.EventManager.onWindowResize(function() {
			dishesDisplayGrid.setHeight(dishesOrderEastPanel.findById(
					"dishesOrderForm").getInnerHeight() - 50);
			// dataSortGridUQ.setHeight(150);
			});
	});
