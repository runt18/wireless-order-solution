// --------------dishes order center panel-----------------
// 1，数据
// 格式：[菜名，口味，数量，单价，操作，实价]
//orderedData.push( [ "酸菜鱼", "只要酸菜不要鱼", 1, "￥56.2", "", "￥56.2" ]);
//orderedData.push( [ "酸菜鱼", "只要酸菜不要鱼", 1, "￥56.2", "", "￥56.2" ]);

// 2，表格的数据store
var orderedStore = new Ext.data.Store({
	proxy : new Ext.data.MemoryProxy(orderedData),
	reader : new Ext.data.ArrayReader({}, [ {
		name : "dishName"
	}, {
		name : "dishTaste"
	}, {
		name : "dishCount"
	}, {
		name : "dishPrice"
	}, {
		name : "dishOpt"
	}, {
		name : "dishTotalPrice"
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
function dishOptPressHandler(rowIndex) {
	if (dishOrderCurrRowIndex_ != -1) {
		Ext.Msg.alert("", "已催菜！");
		orderedStore.reload();
		dishOrderCurrRowIndex_ = -1;
	}

};

function dishOptDispley(value, cellmeta, record, rowIndex, columnIndex, store) {
	return "<center><a href=\"javascript:dishOptTasteHandler(" + rowIndex
			+ ")\">" + "<img src='../images/Modify.png'/>口味</a>"
			+ "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;"
			+ "<a href=\"javascript:dishOptDeleteHandler(" + rowIndex + ")\">"
			+ "<img src='../images/del.png'/>删除</a>"
			+ "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;"
			+ "<a href=\"javascript:dishOptPressHandler(" + rowIndex + ")\">"
			+ "<img src='../images/Modify.png'/>催菜</a>" + "</center>";
};

var orderedColumnModel = new Ext.grid.ColumnModel([ new Ext.grid.RowNumberer(),
		{
			header : "菜名",
			sortable : true,
			dataIndex : "dishName",
			width : 210
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
			dataIndex : "dishTotalPrice",
			width : 120
		}, {
			header : "<center>操作</center>",
			sortable : true,
			dataIndex : "dishOpt",
			width : 220,
			renderer : dishOptDispley
		} ]);

// 4，表格
var tasteChooseImgBut = new Ext.ux.ImageButton({
	imgPath : "../images/im48x48.png",
	imgWidth : 50,
	imgHeight : 50,
	tooltip : "口味",
	handler : function(btn) {
		dishOptTasteHandler(dishOrderCurrRowIndex_);
	}
});

var dishDeleteImgBut = new Ext.ux.ImageButton({
	imgPath : "../images/extlogo48.png",
	imgWidth : 50,
	imgHeight : 50,
	tooltip : "删除",
	handler : function(btn) {
		dishOptDeleteHandler(dishOrderCurrRowIndex_);
	}
});
var dishPressImgBut = new Ext.ux.ImageButton({
	imgPath : "../images/im48x48.png",
	imgWidth : 50,
	imgHeight : 50,
	tooltip : "催菜",
	handler : function(btn) {
		dishOptPressHandler(dishOrderCurrRowIndex_);
	}
});

var orderedGrid = new Ext.grid.GridPanel({
	title : "已点菜式",
	xtype : "grid",
	anchor : "99%",
	region : "center",
	border : false,
	ds : orderedStore,
	cm : orderedColumnModel,
	sm : new Ext.grid.RowSelectionModel({
		singleSelect : true
	}),
	tbar : new Ext.Toolbar({
		height : 55,
		items : [ {
			text : " ",
			disabled : true
		}, tasteChooseImgBut, {
			text : "&nbsp;&nbsp;&nbsp;",
			disabled : true
		}, dishDeleteImgBut, {
			text : "&nbsp;&nbsp;&nbsp;",
			disabled : true
		}, dishPressImgBut ]
	}),
	listeners : {
		rowclick : function(thiz, rowIndex, e) {
			dishOrderCurrRowIndex_ = rowIndex;
		},
		render : function(thiz) {
			orderedDishesOnLoad();
		}
	}
});

var orderedForm = new Ext.form.FormPanel({
	frame : true,
	border : false,
	region : "south",
	height : 60,
	items : [ {} ],
	buttons : [ {
		text : "提交",
		handler : function() {
		}
	}, {
		text : "清空",
		handler : function() {
			Ext.Msg.show({
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
	} ]
});

var dishesOrderCenterPanel = new Ext.Panel({
	region : "center",
	id : "dishesOrderCenterPanel",
	// title : "<div style='font-size:18px;padding-left:2px'>新下单<div>",
	layout : "border",
	items : [ orderedForm, orderedGrid ]
});

// --------------dishes taste pop window-----------------
// 1，数据
//dishTasteData = [];
//dishTasteData.push([ "只要酸菜不要鱼", "￥0" ]);
//dishTasteData.push([ "不要盐", "￥2" ]);
//dishTasteData.push([ "少盐", "￥3" ]);
//dishTasteData.push([ "中盐", "￥4" ]);
//dishTasteData.push([ "多盐", "￥5" ]);
//dishTasteData.push([ "超多盐", "￥6" ]);
//dishTasteData.push([ "使劲放盐", "￥7" ]);
//dishTasteData.push([ "咸死你", "￥8" ]);

// 2，表格的数据store
var dishTasteStore = new Ext.data.Store({
	proxy : new Ext.data.MemoryProxy(dishTasteData),
	reader : new Ext.data.ArrayReader({}, [ {
		name : "dishTaste"
	}, {
		name : "tastePrice"
	} ])
});

dishTasteStore.reload();

// 3，栏位模型
var dishTasteColumnModel = new Ext.grid.ColumnModel([
		new Ext.grid.RowNumberer(), {
			header : "口味",
			sortable : true,
			dataIndex : "dishTaste",
			width : 100
		}, {
			header : "价钱",
			sortable : true,
			dataIndex : "tastePrice",
			width : 100
		} ]);

// 4，表格
var dishTasteGrid = new Ext.grid.GridPanel(
		{
			title : "可选口味",
			anchor : "99%",
			ds : dishTasteStore,
			cm : dishTasteColumnModel,
			sm : new Ext.grid.RowSelectionModel({
				singleSelect : true
			}),
			listeners : {
				rowdblclick : function(thiz, rowIndex, e) {
					var selectedTaste = dishTasteData[rowIndex][0];
					var tastePrice = dishTasteData[rowIndex][1];
					var dishIndex = dishOrderCurrRowIndex_;
					orderedData[dishIndex][1] = selectedTaste;
					orderedData[dishIndex][5] = "￥"
							+ (parseFloat(orderedData[dishIndex][3]
									.substring(1)) + parseFloat(tastePrice
									.substring(1)));
					orderedStore.reload();
					dishTasteWindow.hide();
					dishOrderCurrRowIndex_ = -1;
				}
			}
		});

var dishTasteWindow = new Ext.Window({
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
	dishesOrderEastPanel.findById(relateItemId).fireEvent("blur",
			dishesOrderEastPanel.findById(relateItemId));
};

softKeyBoardDO = new Ext.Window({
	layout : "fit",
	width : 117,
	height : 118,
	resizable : false,
	closeAction : "hide",
	// x : 41,
	// y : 146,
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
								dishKeyboardSelect(softKBRelateItemId);
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
								dishKeyboardSelect(softKBRelateItemId);
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
								dishKeyboardSelect(softKBRelateItemId);
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
								dishKeyboardSelect(softKBRelateItemId);
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
								dishKeyboardSelect(softKBRelateItemId);
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
								dishKeyboardSelect(softKBRelateItemId);
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
								dishKeyboardSelect(softKBRelateItemId);
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
								dishKeyboardSelect(softKBRelateItemId);
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
								dishKeyboardSelect(softKBRelateItemId);
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
								dishKeyboardSelect(softKBRelateItemId);
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
								dishKeyboardSelect(softKBRelateItemId);
								dishesOrderEastPanel.findById(
										softKBRelateItemId).fireEvent(
										"blur",
										dishesOrderEastPanel
												.findById(softKBRelateItemId));
							}

						} ]
					} ]
		} ]
	} ],
	listeners : {
		show : function(thiz) {
			var f = Ext.get(softKBRelateItemId);
			f.focus.defer(100, f); // 万恶的EXT！为什么这样才可以！？！？
		},
		beforehide : function(thiz) {
			if ((softKBRelateItemId == "orderCountNum" && (Ext.getCmp(
					"orderCountNum").getValue() == 0 || Ext.getCmp(
					"orderCountNum").getValue() == ""))
					|| (softKBRelateItemId == "orderCountSpell" && (Ext.getCmp(
							"orderCountSpell").getValue() == 0 || Ext.getCmp(
							"orderCountSpell").getValue() == ""))) {
				return false;
			} else {
				return true;
			}
		}
	}
});

// 1，数据
// 格式：[菜名，菜名编号，菜名拼音，单价]
// dishesDisplayData = [];
// dishesDisplayData.push( [ "酸菜鱼", 1101, "SCY", "￥35.1" ]);
// dishesDisplayData.push( [ "京酱肉丝", 2201, "JJRS", "￥50" ]);
// dishesDisplayData.push( [ "酸菜鱼", 1112, "SCY", "￥35.1" ]);
// dishesDisplayData.push( [ "京酱肉丝", 2212, "JJRS", "￥50" ]);
// dishesDisplayData.push( [ "酸菜鱼", 1114, "SCY", "￥35.1" ]);
// dishesDisplayData.push( [ "京酱肉丝", 2312, "JJRS", "￥50" ]);
// dishesDisplayData.push( [ "酸菜鱼", 4234, "SCY", "￥35.1" ]);
// dishesDisplayData.push( [ "京酱肉丝", 2456, "JJRS", "￥50" ]);
// dishesDisplayData.push( [ "酸菜鱼", 1234, "SCY", "￥35.1" ]);
// dishesDisplayData.push( [ "京酱肉丝", 2765, "JJRS", "￥50" ]);
// dishesDisplayData.push( [ "酸菜鱼", 1678, "SCY", "￥35.1" ]);
// dishesDisplayData.push( [ "京酱肉丝", 2123, "JJRS", "￥50" ]);
// dishesDisplayData.push( [ "酸菜鱼", 1567, "SCY", "￥35.1" ]);
// dishesDisplayData.push( [ "京酱肉丝", 2567, "JJRS", "￥50" ]);
// dishesDisplayData.push( [ "酸菜鱼", 1355, "SCY", "￥35.1" ]);
// dishesDisplayData.push( [ "京酱肉丝", 2536, "JJRS", "￥50" ]);
// dishesDisplayData.push( [ "酸菜鱼", 1534, "SCY", "￥35.1" ]);
// dishesDisplayData.push( [ "京酱肉丝", 2345, "JJRS", "￥50" ]);
// dishesDisplayData.push( [ "酸菜鱼", 1456, "SCY", "￥35.1" ]);
// dishesDisplayData.push( [ "京酱肉丝", 2235, "JJRS", "￥50" ]);
// dishesDisplayData.push( [ "酸菜鱼", 1345, "SCY", "￥35.1" ]);
// dishesDisplayData.push( [ "京酱肉丝", 2756, "JJRS", "￥50" ]);
// dishesDisplayData.push( [ "酸菜鱼", 1345, "SCY", "￥35.1" ]);
// dishesDisplayData.push( [ "京酱肉丝", 2543, "JJRS", "￥50" ]);
// dishesDisplayData.push( [ "酸菜鱼", 1756, "SCY", "￥35.1" ]);
// dishesDisplayData.push( [ "京酱肉丝", 2786, "JJRS", "￥50" ]);
// dishesDisplayData.push( [ "酸菜鱼", 1456, "SCY", "￥35.1" ]);
// dishesDisplayData.push( [ "京酱肉丝", 2547, "JJRS", "￥50" ]);
// dishesDisplayData.push( [ "酸菜鱼", 1245, "SCY", "￥35.1" ]);
// dishesDisplayData.push( [ "京酱肉丝", 2765, "JJRS", "￥50" ]);
// dishesDisplayData.push( [ "酸菜鱼", 1768, "SCY", "￥35.1" ]);
// dishesDisplayData.push( [ "京酱肉丝", 2688, "JJRS", "￥50" ]);
// dishesDisplayData.push( [ "京酱肉丝", 2756, "JJRS", "￥50" ]);
// dishesDisplayData.push( [ "酸菜鱼", 1345, "SCY", "￥35.1" ]);
// dishesDisplayData.push( [ "京酱肉丝", 2543, "JJRS", "￥50" ]);
// dishesDisplayData.push( [ "酸菜鱼", 1756, "SCY", "￥35.1" ]);
// dishesDisplayData.push( [ "京酱肉丝", 2786, "JJRS", "￥50" ]);
// dishesDisplayData.push( [ "酸菜鱼", 1456, "SCY", "￥35.1" ]);
// dishesDisplayData.push( [ "京酱肉丝", 2547, "JJRS", "￥50" ]);
// dishesDisplayData.push( [ "酸菜鱼", 1245, "SCY", "￥35.1" ]);
// dishesDisplayData.push( [ "京酱肉丝", 2765, "JJRS", "￥50" ]);
// dishesDisplayData.push( [ "酸菜鱼", 1768, "SCY", "￥35.1" ]);
// dishesDisplayData.push( [ "京酱肉丝", 2688, "JJRS", "￥50" ]);

// 2，表格的数据store
var dishesDisplayStore = new Ext.data.Store({
	proxy : new Ext.data.MemoryProxy(dishesDisplayDataShow),
	reader : new Ext.data.ArrayReader({}, [ {
		name : "dish"
	}, {
		name : "dishIndex"
	}, {
		name : "dishSpell"
	}, {
		name : "dishPrice"
	} ])
});

// 3，栏位模型
var dishesDisplayColumnModel = new Ext.grid.ColumnModel([
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
		}, {
			header : "菜名拼音",
			sortable : true,
			dataIndex : "dishSpell",
			width : 80
		}, {
			header : "单价",
			sortable : true,
			dataIndex : "dishPrice",
			width : 80
		} ]);

// 4，表格
var dishesDisplayGrid = new Ext.grid.GridPanel({
	xtype : "grid",
	// height : 400,
	anchor : "98%",
	autoScroll : true,
	region : "center",
	ds : dishesDisplayStore,
	cm : dishesDisplayColumnModel,
	sm : new Ext.grid.RowSelectionModel({
		singleSelect : true
	}),
	listeners : {
		rowdblclick : function(thiz, rowIndex, e) {
			var dishCurrCount = dishesOrderEastPanel.findById("orderCountNum")
					.getValue();
			var dishCurrName = dishesDisplayDataShow[rowIndex][0];
			var dishCurrPrice = dishesDisplayDataShow[rowIndex][3];

			orderedData.push([ dishCurrName, "无口味", dishCurrCount,
					dishCurrPrice, "", dishCurrPrice ]);
			orderedStore.reload();
		},
		render : function(thiz) {
			orderedMenuOnLoad();
			tasteOnLoad();
		}
	}
});

dishesDisplayStore.reload();

var dishesChooseBySpellForm = new Ext.form.FormPanel({
	title : "菜名拼音选菜",
	id : "dishesChooseBySpellForm",
	border : false,
	frame : true,
	items : [ {
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
						name : "orderCountSpell",
						id : "orderCountSpell",
						value : 1,
						anchor : "90%",
						listeners : {
							focus : function(thiz) {
								softKeyBoardDO
										.setPosition(dishesOrderCenterPanel
												.getInnerWidth() + 77, 187);
								softKBRelateItemId = "orderCountSpell";
								softKeyBoardDO.show();

							},
							blur : function(thiz) {
								var thisValue = thiz.getValue();
								dishesOrderEastPanel.findById("orderCountNum")
										.setValue(thisValue);
							}
						}
					} ]
				}, {
					layout : "form",
					labelWidth : 60,
					border : false,
					labelSeparator : '：',
					columnWidth : .50,
					items : [ {
						xtype : "textfield",
						fieldLabel : "菜名拼音",
						name : "orderSpell",
						id : "orderSpell",
						anchor : "90%",
						listeners : {
							focus : function(thiz) {
								softKeyBoardDO.hide();
							},
							render : function(thiz) {
								dishSpellOnLoad();
							}
						}
					} ]
				} ]
	} ]
});

var dishesChooseByNumForm = new Ext.form.FormPanel({
	title : "菜名编号选菜",
	id : "dishesChooseByNumForm",
	border : false,
	frame : true,
	items : [ {
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
						name : "orderCountNum",
						id : "orderCountNum",
						value : 1,
						anchor : "90%",
						listeners : {
							focus : function(thiz) {
								softKeyBoardDO
										.setPosition(dishesOrderCenterPanel
												.getInnerWidth() + 77, 187);
								softKBRelateItemId = "orderCountNum";
								softKeyBoardDO.show();

							},
							blur : function(thiz) {
								var thisValue = thiz.getValue();
								dishesOrderEastPanel
										.findById("orderCountSpell").setValue(
												thisValue);
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
								softKeyBoardDO
										.setPosition(dishesOrderCenterPanel
												.getInnerWidth() + 281, 187);
								softKBRelateItemId = "orderNbr";
								softKeyBoardDO.show();

							},
							render : function(thiz) {
								dishNbrOnLoad();
							}
						}
					} ]
				} ]
	} ]
});

var dishesDisplayTabPanel = new Ext.TabPanel({
	activeTab : 0,
	height : 65,
	region : "north",
	border : false,
	items : [ dishesChooseByNumForm, dishesChooseBySpellForm ],
	listeners : {
		// for FF only!!! FF when clicking the tab, the focus of the number
		// field
		// does not lost!!!
		tabchange : function(thiz, panel) {
			dishesOrderEastPanel.findById("orderCountNum").fireEvent("blur",
					dishesOrderEastPanel.findById("orderCountNum"));
			dishesOrderEastPanel.findById("orderCountSpell").fireEvent("blur",
					dishesOrderEastPanel.findById("orderCountSpell"));

			// hide the soft keyboard
			if (softKeyBoardDO.isVisible()) {
				softKeyBoardDO.hide();
			}
		}
	}
});

var dishesOrderEastPanel = new Ext.Panel({
	region : "east",
	collapsible : true,
	width : 432,
	minSize : 332,
	maxSize : 532,
	split : true,
	id : "dishesOrderEastPanel",
	layout : "border",
	items : [ dishesDisplayTabPanel, dishesDisplayGrid ]
});

// --------------dishes order north panel-----------------
var dishesOrderNorthPanel = new Ext.Panel({
	id : "dishesOrderNorthPanel",
	region : "north",
	title : "<div style='font-size:18px;padding-left:2px'>新下单<div>",
	height : 75,
	border : false,
	layout : "form",
	frame : true,
	contentEl : "tableStatusDO",
	listeners : {
		render : function(thiz) {
			tableStuLoad();
		}
	}
});

Ext
		.onReady(function() {
			// 解决ext中文传入后台变问号问题
			Ext.lib.Ajax.defaultPostHeader += '; charset=utf-8';
			Ext.QuickTips.init();

			// *************整体布局*************
			var centerPanelDO = new Ext.Panel({
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
			// Ext.EventManager.onWindowResize(function() {
			// dishesDisplayGrid
			// .setHeight(dishesOrderEastPanel.getInnerHeight() - 100);
			// // dataSortGridUQ.setHeight(150);
			// });
		});
