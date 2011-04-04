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
	dishOrderCurrRowIndex_ = rowIndex;
	dishTasteWindow.show();
};
function dishOptModifyHandler() {
	alert("shit!shit!");
};
function dishOptDeleteHandler(rowIndex) {
	orderedData.splice(rowIndex, 1);
	orderedStore.reload();
};

function dishOptDispley(value, cellmeta, record, rowIndex, columnIndex, store) {
	return "<center><a href=\"javascript:dishOptTasteHandler(" + rowIndex
			+ ")\">" + "<img src='../images/Modify.png'/>口味</a>"
			+ "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;"
			+ "<a href=\"javascript:dishOptModifyHandler()\">"
			+ "<img src='../images/Modify.png'/>修改</a>"
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
var orderedGrid = new Ext.grid.GridPanel( {
	title : "已点菜式",
	// height : 400,
	xtype : "grid",
	anchor : "99%",
	// height : 500,
	ds : orderedStore,
	cm : orderedColumnModel
});

var orderedForm = new Ext.form.FormPanel(
		{
			frame : true,
			border : false,
			items : [
					{
						id : "orderedInfoMsg",
						html : "<br/><font size=3pt,style='padding-bottom:10pt;'>当前台号尚未下单。</font>"
					}, orderedGrid ],
			buttons : [
					{
						text : "提交",
						handler : function() {
							dishesOrderNorthPanel.findById("orderTime")
									.setValue(new Date().toLocaleString()); // according
																			// to
																			// the
																			// operation
																			// system
																			// language
																			// type!
							// dishesOrderNorthPanel.findById("orderTime").setValue(
							// DateTime.Now);
							dishesOrderNorthPanel.findById("orderOperator")
									.setValue("ZhaoTongFei");
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
						text : "删单"
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
	title : "新下单",
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
	// title : "菜单",
	// height : 400,
	anchor : "98%",
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

var dishesOrderEastPanel = new Ext.Panel( {
	region : "east",
	title : "菜谱",
	collapsible : true,
	width : 280,
	minSize : 180,
	maxSize : 380,
	margins : '0 5 0 5',
	split : true,
	id : "dishesOrderEastPanel",
	layout : "fit",
	items : [ {
		id : "dishesOrderForm",
		layout : "form",
		labelWidth : 60,
		frame : true,
		border : false,
		// height : 300,
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
			anchor : "90%",
			listeners : {
				change : function(thiz, newValue, oldValue) {
					alert(newValue);
					alert(oldValue);
				}
			}
		}, dishesDisplayGrid ]
	} ]
});

// --------------dishes order north panel-----------------
var dishesOrderNorthPanel = new Ext.form.FormPanel( {
	id : "dishesOrderNorthPanel",
	region : "north",
	height : 100,
	border : false,
	frame : true,
	// autoScroll : true,
	buttonAlign : "left",
	items : [ {
		layout : "column",
		border : false,
		anchor : '98%',
		labelSeparator : '：',
		items : [ {
			layout : "form",
			width : 230,
			labelWidth : 60,
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
			width : 150,
			labelWidth : 40,
			border : false,
			items : [ {
				xtype : "numberfield",
				fieldLabel : "人数",
				name : "personCount",
				id : "personCount",
				anchor : "90%"
			} ]
		} ]
	}, {
		layout : "column",
		border : false,
		anchor : '98%',
		labelSeparator : '：',
		items : [ {
			layout : "form",
			width : 230,
			labelWidth : 60,
			border : false,
			items : [ {
				xtype : "textfield",
				fieldLabel : "下单时间",
				name : "orderTime",
				id : "orderTime",
				disabled : true,
				anchor : "90%"
			} ]
		}, {
			layout : "form",
			width : 150,
			labelWidth : 40,
			border : false,
			items : [ {
				xtype : "textfield",
				fieldLabel : "操作人",
				name : "orderOperator",
				id : "orderOperator",
				disabled : true,
				anchor : "90%"
			} ]
		} ]
	} ],
	buttons : [ {
		text : "提交"
	}, {
		text : "清空",
		handler : function() {
			dishesOrderNorthPanel.findById("tableNumber").setValue("");
			dishesOrderNorthPanel.findById("personCount").setValue("");
		}
	} ]
});