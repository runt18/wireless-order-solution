// --------------dishes order center panel-----------------
// 1，数据
var orderedData = [];
orderedData.push( [ "酸菜鱼", "只要酸菜不要鱼", 1, "￥56.2", "" ]);
orderedData.push( [ "酸菜鱼", "只要酸菜不要鱼", 1, "￥56.2", "" ]);

// 2，表格的数据store
var orderedStore = new Ext.data.Store( {
	proxy : new Ext.data.MemoryProxy(orderedData),
	reader : new Ext.data.ArrayReader( {}, [ {
		name : "dishName"
	}, {
		name : "dishTaste"
	}, {
		name : "dishCount"
	}, {
		name : "dishPrice"
	}, {
		name : "dishOpt"
	} ])
});

orderedStore.reload();

// 3，栏位模型
function dishOptTasteHandler(rowIndex) {
	if (dishOrderCurrRowIndex_ != -1) {
		dishOrderCurrRowIndex_ = rowIndex;
		dishTasteWindow.show();
	}
};
function dishOptDeleteHandler(rowIndex) {
	if (dishOrderCurrRowIndex_ != -1) {
		orderedData.splice(rowIndex, 1);
		orderedStore.reload();
		dishOrderCurrRowIndex_ = -1;
	}
};

function dishOptDispley(value, cellmeta, record, rowIndex, columnIndex, store) {
	return "<center><a href=\"javascript:dishOptTasteHandler(" + rowIndex
			+ ")\">" + "<img src='../images/Modify.png'/>口味</a>"
			+ "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;"
			+ "<a href=\"javascript:dishOptDeleteHandler(" + rowIndex + ")\">"
			+ "<img src='../images/del.png'/>删除</a></center>";
};

var orderedColumnModel = new Ext.grid.ColumnModel( [
		new Ext.grid.RowNumberer(), {
			header : "菜名",
			sortable : true,
			dataIndex : "dishName",
			width : 230
		}, {
			header : "口味",
			sortable : true,
			dataIndex : "dishTaste",
			width : 120
		}, {
			header : "数量",
			sortable : true,
			dataIndex : "dishCount",
			width : 120
		}, {
			header : "单价",
			sortable : true,
			dataIndex : "dishPrice",
			width : 120
		}, {
			header : "<center>操作</center>",
			sortable : true,
			dataIndex : "dishOpt",
			width : 220,
			renderer : dishOptDispley
		} ]);

// 4，表格
var tasteChooseImgBut = new Ext.ux.ImageButton( {
	imgPath : "../images/im48x48.png",
	imgWidth : 50,
	imgHeight : 50,
	tooltip : "口味",
	handler : function(btn) {
		dishOptTasteHandler(dishOrderCurrRowIndex_);
	}
});

var dishDeleteImgBut = new Ext.ux.ImageButton( {
	imgPath : "../images/extlogo48.png",
	imgWidth : 50,
	imgHeight : 50,
	tooltip : "删除",
	handler : function(btn) {
		dishOptDeleteHandler(dishOrderCurrRowIndex_);
	}
});

var orderedGrid = new Ext.grid.GridPanel( {
	title : "已点菜式",
	// height : 400,
	xtype : "grid",
	anchor : "99%",
	// height : 500,
	ds : orderedStore,
	cm : orderedColumnModel,
	sm : new Ext.grid.RowSelectionModel( {
		singleSelect : true
	}),
	tbar : new Ext.Toolbar( {
		height : 55,
		items : [ {
			text : " ",
			disabled : true
		}, tasteChooseImgBut, {
			text : "&nbsp;&nbsp;&nbsp;",
			disabled : true
		}, dishDeleteImgBut ]
	}),
	listeners : {
		rowclick : function(thiz, rowIndex, e) {
			dishOrderCurrRowIndex_ = rowIndex;
		}
	}
});

var orderedForm = new Ext.form.FormPanel( {
	frame : true,
	border : false,
	items : [ orderedGrid ],
	buttons : [
			{
				text : "提交",
				handler : function() {
					dishesOrderNorthPanel.findById("orderTime").setValue(
							new Date().toLocaleString()); // according
					// to
					// the
					// operation
					// system
					// language
					// type!
					// dishesOrderNorthPanel.findById("orderTime").setValue(
					// DateTime.Now);
					dishesOrderNorthPanel.findById("orderOperator").setValue(
							"ZhaoTongFei");
				}
			}, {
				text : "清空",
				handler : function() {
					Ext.Msg.show( {
						title : "提示",
						msg : "确定要删除所有已点菜式？",
						buttons : Ext.Msg.YESNO,
						fn : function(btn) {
							if (btn == "yes") {
								orderedData.length = 0;
								orderedStore.reload();
							}
							;
						},
						icon : Ext.MessageBox.QUESTION
					});
				}
			}, {
				text : "返回",
				handler : function() {
					location.href = "TableSelect.html";
				}
			} ],
	listeners : {
		afterlayout : function(thiz) {
			orderedGrid.setHeight(thiz.getInnerHeight() - 30);
		}
	}
});

var dishesOrderCenterPanel = new Ext.Panel( {
	region : "center",
	id : "dishesOrderCenterPanel",
	title : "<div style='font-size:18px;padding-left:2px'>新下单<div>",
	layout : "fit",
	items : [ orderedForm ]
});

// --------------dishes taste pop window-----------------
// 1，数据
var dishTasteData = [];
dishTasteData.push( [ "只要酸菜不要鱼" ]);
dishTasteData.push( [ "不要盐" ]);
dishTasteData.push( [ "少盐" ]);
dishTasteData.push( [ "中盐" ]);
dishTasteData.push( [ "多盐" ]);
dishTasteData.push( [ "超多盐" ]);
dishTasteData.push( [ "使劲放盐" ]);
dishTasteData.push( [ "咸死你" ]);

// 2，表格的数据store
var dishTasteStore = new Ext.data.Store( {
	proxy : new Ext.data.MemoryProxy(dishTasteData),
	reader : new Ext.data.ArrayReader( {}, [ {
		name : "dishTaste"
	} ])
});

dishTasteStore.reload();

// 3，栏位模型
var dishTasteColumnModel = new Ext.grid.ColumnModel( [
		new Ext.grid.RowNumberer(), {
			header : "口味",
			sortable : true,
			dataIndex : "dishTaste",
			width : 200
		} ]);

// 4，表格
var dishTasteGrid = new Ext.grid.GridPanel( {
	title : "可选口味",
	anchor : "99%",
	ds : dishTasteStore,
	cm : dishTasteColumnModel,
	sm : new Ext.grid.RowSelectionModel( {
		singleSelect : true
	}),
	listeners : {
		rowdblclick : function(thiz, rowIndex, e) {
			var selectedTaste = dishTasteData[rowIndex][0];
			var dishIndex = dishOrderCurrRowIndex_;
			orderedData[dishIndex][1] = selectedTaste;
			orderedStore.reload();
			dishTasteWindow.hide();
		}
	}
});

var dishTasteWindow = new Ext.Window( {
	layout : "fit",
	width : 250,
	height : 300,
	closeAction : "hide",
	// plain: true,
	items : dishTasteGrid
});

// --------------dishes order east panel-----------------
// soft key board
var softKBKeyHandler = function(relateItemId, number) {
	var currValue = dishesOrderEastPanel.findById(relateItemId).getValue();
	dishesOrderEastPanel.findById(relateItemId).setValue(
			currValue + "" + number);
};

softKeyBoardDO = new Ext.Window( {
	layout : "fit",
	width : 177,
	height : 100,
	closeAction : "hide",
	resizable : false,
	closable : false,
	x : 41,
	y : 146,
	items : [ {
		layout : "form",
		labelSeparator : '：',
		labelWidth : 40,
		frame : true,
		buttonAlign : "left",
		items : [ {
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
								softKBKeyHandler(softKBRelateItemId, "1");
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
								softKBKeyHandler(softKBRelateItemId, "2");
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
								softKBKeyHandler(softKBRelateItemId, "3");
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
								softKBKeyHandler(softKBRelateItemId, "4");
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
								softKBKeyHandler(softKBRelateItemId, "5");
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
								softKBKeyHandler(softKBRelateItemId, "6");
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
								softKBKeyHandler(softKBRelateItemId, "7");
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
								softKBKeyHandler(softKBRelateItemId, "8");
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
								softKBKeyHandler(softKBRelateItemId, "9");
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
								softKBKeyHandler(softKBRelateItemId, "0");
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
								softKeyBoardDO.hide();
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
								dishesOrderEastPanel.findById(
										softKBRelateItemId).setValue("");
							}

						} ]
					} ]
		} ]
	} ],
	listeners : {
		show : function(thiz) {
			var f = Ext.get(softKBRelateItemId);
			f.focus.defer(100, f); // 万恶的EXT！为什么这样才可以！？！？
}
}
});

// 1，数据
var dishesDisplayData = [];
dishesDisplayData.push( [ "酸菜鱼", 1101, 35.1 ]);
dishesDisplayData.push( [ "京酱肉丝", 2201, 50 ]);
dishesDisplayData.push( [ "酸菜鱼", 1112, 35.1 ]);
dishesDisplayData.push( [ "京酱肉丝", 2212, 50 ]);
dishesDisplayData.push( [ "酸菜鱼", 1114, 35.1 ]);
dishesDisplayData.push( [ "京酱肉丝", 2312, 50 ]);
dishesDisplayData.push( [ "酸菜鱼", 4234, 35.1 ]);
dishesDisplayData.push( [ "京酱肉丝", 2456, 50 ]);
dishesDisplayData.push( [ "酸菜鱼", 1234, 35.1 ]);
dishesDisplayData.push( [ "京酱肉丝", 2765, 50 ]);
dishesDisplayData.push( [ "酸菜鱼", 1678, 35.1 ]);
dishesDisplayData.push( [ "京酱肉丝", 2123, 50 ]);
dishesDisplayData.push( [ "酸菜鱼", 1567, 35.1 ]);
dishesDisplayData.push( [ "京酱肉丝", 2567, 50 ]);
dishesDisplayData.push( [ "酸菜鱼", 1355, 35.1 ]);
dishesDisplayData.push( [ "京酱肉丝", 2536, 50 ]);
dishesDisplayData.push( [ "酸菜鱼", 1534, 35.1 ]);
dishesDisplayData.push( [ "京酱肉丝", 2345, 50 ]);
dishesDisplayData.push( [ "酸菜鱼", 1456, 35.1 ]);
dishesDisplayData.push( [ "京酱肉丝", 2235, 50 ]);
dishesDisplayData.push( [ "酸菜鱼", 1345, 35.1 ]);
dishesDisplayData.push( [ "京酱肉丝", 2756, 50 ]);
dishesDisplayData.push( [ "酸菜鱼", 1345, 35.1 ]);
dishesDisplayData.push( [ "京酱肉丝", 2543, 50 ]);
dishesDisplayData.push( [ "酸菜鱼", 1756, 35.1 ]);
dishesDisplayData.push( [ "京酱肉丝", 2786, 50 ]);
dishesDisplayData.push( [ "酸菜鱼", 1456, 35.1 ]);
dishesDisplayData.push( [ "京酱肉丝", 2547, 50 ]);
dishesDisplayData.push( [ "酸菜鱼", 1245, 35.1 ]);
dishesDisplayData.push( [ "京酱肉丝", 2765, 50 ]);
dishesDisplayData.push( [ "酸菜鱼", 1768, 35.1 ]);
dishesDisplayData.push( [ "京酱肉丝", 2688, 50 ]);
dishesDisplayData.push( [ "京酱肉丝", 2756, 50 ]);
dishesDisplayData.push( [ "酸菜鱼", 1345, 35.1 ]);
dishesDisplayData.push( [ "京酱肉丝", 2543, 50 ]);
dishesDisplayData.push( [ "酸菜鱼", 1756, 35.1 ]);
dishesDisplayData.push( [ "京酱肉丝", 2786, 50 ]);
dishesDisplayData.push( [ "酸菜鱼", 1456, 35.1 ]);
dishesDisplayData.push( [ "京酱肉丝", 2547, 50 ]);
dishesDisplayData.push( [ "酸菜鱼", 1245, 35.1 ]);
dishesDisplayData.push( [ "京酱肉丝", 2765, 50 ]);
dishesDisplayData.push( [ "酸菜鱼", 1768, 35.1 ]);
dishesDisplayData.push( [ "京酱肉丝", 2688, 50 ]);

// 2，表格的数据store
var dishesDisplayStore = new Ext.data.Store( {
	proxy : new Ext.data.MemoryProxy(dishesDisplayData),
	reader : new Ext.data.ArrayReader( {}, [ {
		name : "dish"
	}, {
		name : "dishIndex"
	}, {
		name : "dishPrice"
	} ])
});

// 3，栏位模型
var dishesDisplayColumnModel = new Ext.grid.ColumnModel( [
		new Ext.grid.RowNumberer(), {
			header : "菜名",
			sortable : true,
			dataIndex : "dish",
			width : 130
		}, {
			header : "菜名编号",
			sortable : true,
			dataIndex : "dishIndex",
			width : 80
		} ]);

// 4，表格
var dishesDisplayGrid = new Ext.grid.GridPanel( {
	xtype : "grid",
	// height : 400,
	anchor : "98%",
	autoScroll : true,
	ds : dishesDisplayStore,
	cm : dishesDisplayColumnModel,
	sm : new Ext.grid.RowSelectionModel( {
		singleSelect : true
	}),
	listeners : {
		rowdblclick : function(thiz, rowIndex, e) {
			var dishCurrCount = dishesOrderEastPanel.findById("orderCount")
					.getValue();
			var dishCurrName = dishesDisplayData[rowIndex][0];
			var dishCurrPrice = "￥" + dishesDisplayData[rowIndex][2];

			orderedData.push( [ dishCurrName, "无特别要求", dishCurrCount,
					dishCurrPrice ]);
			orderedStore.reload();
		}
	}
});

dishesDisplayStore.reload();

var dishesOrderEastPanel = new Ext.form.FormPanel( {
	region : "east",
	title : "菜谱",
	collapsible : true,
	width : 400,
	minSize : 300,
	maxSize : 500,
	margins : '0 5 0 5',
	split : true,
	id : "dishesOrderEastPanel",
	border : false,
	frame : true,
	items : [
			{
				layout : "column",
				border : false,
				anchor : "98%",
				items : [
						{
							layout : "form",
							labelWidth : 60,
							border : false,
							labelSeparator : '：',
							columnWidth : .50,
							items : [ {
								xtype : "numberfield",
								fieldLabel : "数量",
								name : "orderCount",
								id : "orderCount",
								value : 1,
								anchor : "90%",
								listeners : {
									focus : function(thiz) {
										softKeyBoardDO.setPosition(
												dishesOrderCenterPanel
														.getInnerWidth() + 45,
												148);
										softKBRelateItemId = "orderCount";
										softKeyBoardDO.show();

									}
								}
							} ]
						},
						{
							layout : "form",
							labelWidth : 60,
							border : false,
							labelSeparator : '：',
							columnWidth : .50,
							items : [ {
								xtype : "numberfield",
								fieldLabel : "菜名编号",
								name : "orderNbr",
								id : "orderNbr",
								anchor : "90%",
								listeners : {
									focus : function(thiz) {
										softKeyBoardDO.setPosition(
												dishesOrderCenterPanel
														.getInnerWidth() + 232,
												148);
										softKBRelateItemId = "orderNbr";
										softKeyBoardDO.show();

									}
								}
							} ]
						} ]
			}, dishesDisplayGrid ]
});

// --------------dishes order north panel-----------------
var dishesOrderNorthPanel = new Ext.Panel( {
	id : "dishesOrderNorthPanel",
	region : "north",
	height : 40,
	border : false,
	layout : "form",
	frame : true,
	contentEl : "tableStatusDO"
});

Ext.onReady( function() {
	// 解决ext中文传入后台变问号问题
		Ext.lib.Ajax.defaultPostHeader += '; charset=utf-8';
		Ext.QuickTips.init();

		// *************整体布局*************
		var centerPanelDO = new Ext.Panel( {
			id : "centerPanelDO",
			region : "center",
			border : false,
			margins : "0 5 0 0",
			layout : "border",
			items : [ dishesOrderCenterPanel, dishesOrderEastPanel,
					dishesOrderNorthPanel ]
		});

		var viewport = new Ext.Viewport(
				{
					layout : "border",
					id : "viewport",
					items : [
							{
								region : "north",
								html : "<div style='padding:10px; background-color:#A9D0F5'><h4 style='font-size:150%'>无线点餐网页终端<h4></div>",
								height : 50,
								margins : "0 0 5 0"
							},
							centerPanelDO,
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
			dishesDisplayGrid
					.setHeight(dishesOrderEastPanel.getInnerHeight() - 50);
			// dataSortGridUQ.setHeight(150);
			});
	});
