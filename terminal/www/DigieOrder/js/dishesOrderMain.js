var dishOrderCurrRowIndex_ = 0;

Ext.onReady( function() {
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
								id : "dishesOrderTab",
								layout : "border",
								items : [ dishesOrderCenterPanel,
										dishesOrderEastPanel,
										dishesOrderNorthPanel ]
							},
							{
								title : "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;结账&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;",
								id : "checkOutTab",
								layout : "border",
								items : [ checkOutCenterPanel,
										checkOutNorthPanel ]
							} ],
					listeners : {
						tabchange : function(thiz, tab) {
							if (tab.getId() == "dishesOrderTab") {
								dishesOrderEastPanel.setWidth(280);
								dishesDisplayGrid
										.setHeight(dishesOrderEastPanel
												.findById("dishesOrderForm")
												.getInnerHeight() - 50);
								dishesOrderEastPanel.doLayout();

								dishesOrderNorthPanel.setHeight(100);
								dishesOrderNorthPanel.doLayout();
								// dishesOrderCenterPanel
								// .setHeight(dishesOrderCenterPanel
								// .getHeight() - 100);
								// dishesOrderCenterPanel.doLayout();

							} else {
								checkOutNorthPanel.setHeight(100);
								checkOutNorthPanel.doLayout();
								// checkOutCenterPanel
								// .setHeight(checkOutCenterPanel
								// .getHeight() - 100);
								// checkOutCenterPanel.doLayout();
							}
						}
					}
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
		Ext.EventManager.onWindowResize( function() {
			if (centerTabPanel.getActiveTab().getId() == "dishesOrderTab") {
				dishesDisplayGrid.setHeight(dishesOrderEastPanel.findById(
						"dishesOrderForm").getInnerHeight() - 50);
			}
			// dataSortGridUQ.setHeight(150);
			});
	});
