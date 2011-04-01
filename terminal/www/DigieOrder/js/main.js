Ext.onReady( function() {
	// 解决ext中文传入后台变问号问题
		Ext.lib.Ajax.defaultPostHeader += '; charset=utf-8';
		Ext.QuickTips.init();

		// --------------dishes order center panel-----------------
		// 1，数据
		var orderedData = [];

		// 2，表格的数据store
		var orderedStore = new Ext.data.Store( {
			proxy : new Ext.data.MemoryProxy(orderedData),
			reader : new Ext.data.ArrayReader( {}, [ {
				name : "dishName"
			}, {
				name : "dishIndex"
			} ])
		});

		// 3，栏位模型
		var dishesDisplayColumnModel = new Ext.grid.ColumnModel( [
				new Ext.grid.RowNumberer(), {
					header : "菜名",
					sortable : true,
					dataIndex : "dish",
					width : 230
				} ]);

		// 4，表格
		var orderedGrid = new Ext.grid.GridPanel( {
			//title : "菜单",
			height : 400,
			ds : dishesDisplayStore,
			cm : dishesDisplayColumnModel
		});
		
		var dishesOrderCenterPanel = new Ext.Panel( {
			region : "center",
			title : "已点菜式",
			layout : "fit",
			items : [ orderedGrid ]
		});

		// --------------dishes order east panel-----------------
		// 1，数据
		var dishesDisplayData = [];
		dishesDisplayData.push( [ "酸菜鱼", 1 ]);
		dishesDisplayData.push( [ "京酱肉丝", 1 ]);
		dishesDisplayData.push( [ "酸菜鱼", 1 ]);
		dishesDisplayData.push( [ "京酱肉丝", 1 ]);
		dishesDisplayData.push( [ "酸菜鱼", 1 ]);
		dishesDisplayData.push( [ "京酱肉丝", 1 ]);
		dishesDisplayData.push( [ "酸菜鱼", 1 ]);
		dishesDisplayData.push( [ "京酱肉丝", 1 ]);
		dishesDisplayData.push( [ "酸菜鱼", 1 ]);
		dishesDisplayData.push( [ "京酱肉丝", 1 ]);
		dishesDisplayData.push( [ "酸菜鱼", 1 ]);
		dishesDisplayData.push( [ "京酱肉丝", 1 ]);
		dishesDisplayData.push( [ "酸菜鱼", 1 ]);
		dishesDisplayData.push( [ "京酱肉丝", 1 ]);
		dishesDisplayData.push( [ "酸菜鱼", 1 ]);
		dishesDisplayData.push( [ "京酱肉丝", 1 ]);
		dishesDisplayData.push( [ "酸菜鱼", 1 ]);
		dishesDisplayData.push( [ "京酱肉丝", 1 ]);
		dishesDisplayData.push( [ "酸菜鱼", 1 ]);
		dishesDisplayData.push( [ "京酱肉丝", 1 ]);
		dishesDisplayData.push( [ "酸菜鱼", 1 ]);
		dishesDisplayData.push( [ "京酱肉丝", 1 ]);
		dishesDisplayData.push( [ "酸菜鱼", 1 ]);
		dishesDisplayData.push( [ "京酱肉丝", 1 ]);
		dishesDisplayData.push( [ "酸菜鱼", 1 ]);
		dishesDisplayData.push( [ "京酱肉丝", 1 ]);
		dishesDisplayData.push( [ "酸菜鱼", 1 ]);
		dishesDisplayData.push( [ "京酱肉丝", 1 ]);
		dishesDisplayData.push( [ "酸菜鱼", 1 ]);
		dishesDisplayData.push( [ "京酱肉丝", 1 ]);

		// 2，表格的数据store
		var dishesDisplayStore = new Ext.data.Store( {
			proxy : new Ext.data.MemoryProxy(dishesDisplayData),
			reader : new Ext.data.ArrayReader( {}, [ {
				name : "dish"
			}, {
				name : "dishIndex"
			} ])
		});

		// 3，栏位模型
		var dishesDisplayColumnModel = new Ext.grid.ColumnModel( [
				new Ext.grid.RowNumberer(), {
					header : "菜名",
					sortable : true,
					dataIndex : "dish",
					width : 230
				} ]);

		// 4，表格
		var dishesDisplayGrid = new Ext.grid.GridPanel( {
			xtype : "grid",
			//title : "菜单",
			height : 400,
			ds : dishesDisplayStore,
			cm : dishesDisplayColumnModel
		});

		dishesDisplayStore.reload();

		var dishesOrderEastPanel = new Ext.Panel( {
			region : "east",
			title : "菜谱",
			collapsible : true, // 可折叠
			width : 200,
			minSize : 100, // split:true才有效
			maxSize : 300,
			margins : '0 5 0 5',
			split : true, // 可调节宽度
			id : "dishesOrderEastPanel",
			layout : "fit",
			items : [ {
				id : "dishesOrderForm",
				layout : "form",
				labelWidth : 60,
				frame : true,
				border : false,
				height : 300,
				items : [ {
					xtype : "numberfield",
					fieldLabel : "数量",
					name : "orderCount",
					id : "orderCount",
					value : 1,
					anchor : "90%"
				}, {
					xtype : "numberfield",
					fieldLabel : "菜名编号",
					name : "orderNbr",
					id : "orderNbr",
					anchor : "90%"
				}, dishesDisplayGrid ]
			} ]
		});

		var dishesOrderNorthPanel = new Ext.form.FormPanel( {
			id : "dishesOrderNorthPanel",
			region : "north",
			height : 80,
			border : false,
			frame : true,
			buttonAlign : "left",
			items : [ {
				layout : "column",
				border : false,
				anchor : '98%',
				labelSeparator : '：',
				items : [ {
					layout : "form",
					columnWidth : .20,
					labelWidth : 30,
					border : false,
					items : [ {
						xtype : "textfield",
						fieldLabel : "桌号",
						name : "tableNumber",
						id : "tableNumber",
						anchor : "90%"
					} ]
				}, {
					layout : "form",
					columnWidth : .10,
					labelWidth : 30,
					border : false,
					items : [ {
						xtype : "numberfield",
						fieldLabel : "人数",
						name : "person",
						id : "person",
						anchor : "90%"
					} ]
				} ]
			} ],
			buttons : [ {
				text : "提交"
			}, {
				text : "清空"
			} ]
		});

		// *************整体布局*************
		var centerTabPanel = new Ext.TabPanel( {
			id : "centerPanel",
			region : "center",
			border : false,
			layoutOnTabChange : true,
			activeTab : 0,
			tabPosition : "top",
			margins : '0 5 0 0',
			items : [
					{
						title : "点菜",
						layout : "border",
						items : [ dishesOrderCenterPanel, dishesOrderEastPanel,
								dishesOrderNorthPanel ]
					}, {
						title : "结账"
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
		Ext.EventManager.onWindowResize( function() {
			dishesDisplayGrid.setHeight(dishesOrderEastPanel.findById(
					"dishesOrderForm").getInnerHeight() - 50);
			// dataSortGridUQ.setHeight(150);
			});
	});
