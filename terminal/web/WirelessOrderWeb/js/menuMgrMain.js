//----------------- 菜谱修改 --------------------
var kitchenTypeStoreMM = new Ext.data.Store({
	proxy : new Ext.data.HttpProxy({
		url : "../QueryMenu.do?pin=" + pin + "&type=4"
	}),
	reader : new Ext.data.JsonReader({
		root : 'root'
	}, [ {
		name : 'value',
		mapping : 'value'
	}, {
		name : 'text',
		mapping : 'text'
	} ])
});

var kitchenTypeCombMM = new Ext.form.ComboBox({
	fieldLabel : "厨房",
	forceSelection : true,
	width : 220,
	// value : kitchenTypeData[0][1],
	id : "kitchenTypeCombMM",
	store : kitchenTypeStoreMM,
	valueField : "value",
	displayField : "text",
	typeAhead : true,
	mode : "local",
	triggerAction : "all",
	selectOnFocus : true,
	allowBlank : false
});

menuModifyWin = new Ext.Window({
	layout : "fit",
	title : "修改菜谱",
	width : 315,
	height : 235,
	closeAction : "hide",
	resizable : false,
	items : [ {
		layout : "form",
		labelWidth : 30,
		border : false,
		frame : true,
		items : [ {
			xtype : "numberfield",
			fieldLabel : "编号",
			id : "menuModNumber",
			disabled : true,
			width : 220
		}, {
			xtype : "textfield",
			fieldLabel : "菜名",
			id : "menuModName",
			width : 220
		}, {
			xtype : "textfield",
			fieldLabel : "拼音",
			id : "menuModSpill",
			width : 220
		}, {
			xtype : "numberfield",
			fieldLabel : "价格",
			id : "menuModPrice",
			width : 220
		}, kitchenTypeCombMM, {
			layout : "column",
			border : false,
			anchor : "98%",
			items : [ {
				layout : "form",
				border : false,
				labelSeparator : '',
				width : 70,
				labelWidth : 30,
				items : [ {
					xtype : "checkbox",
					id : "specialCheckboxMM",
					fieldLabel : "特价"
				} ]
			}, {
				layout : "form",
				border : false,
				labelSeparator : '',
				width : 70,
				labelWidth : 30,
				items : [ {
					xtype : "checkbox",
					id : "recommendCheckboxMM",
					fieldLabel : "推荐"
				} ]
			}, {
				layout : "form",
				border : false,
				labelSeparator : '',
				width : 70,
				labelWidth : 30,
				items : [ {
					xtype : "checkbox",
					id : "freeCheckboxMM",
					fieldLabel : "赠送"
				} ]
			}, {
				layout : "form",
				border : false,
				labelSeparator : '',
				width : 70,
				labelWidth : 30,
				items : [ {
					xtype : "checkbox",
					id : "stopCheckboxMM",
					fieldLabel : "停售"
				} ]
			} ]
		} ]
	} ],
	buttons : [ {
		text : "确定",
		handler : function() {

		}
	}, {
		text : "取消",
		handler : function() {
			menuModifyWin.hide();
		}
	} ],
	listeners : {
		"show" : function(thiz) {
			kitchenTypeStoreMM.reload();
		}
	}
});

// --------------------------------------------------------------------------
var orderStatiBut = new Ext.ux.ImageButton({
	imgPath : "../images/orderStatic.png",
	imgWidth : 50,
	imgHeight : 50,
	tooltip : "点菜统计",
	handler : function(btn) {

	}
});

var dishAddBut = new Ext.ux.ImageButton({
	imgPath : "../images/dishAdd.png",
	imgWidth : 50,
	imgHeight : 50,
	tooltip : "添加新菜",
	handler : function(btn) {

	}
});

// --
var pushBackBut = new Ext.ux.ImageButton({
	imgPath : "../images/UserLogout.png",
	imgWidth : 50,
	imgHeight : 50,
	tooltip : "返回",
	handler : function(btn) {
		location.href = "PersonLogin.html?restaurantID=" + restaurantID
				+ "&isNewAccess=false&pin=" + pin;
	}
});

var logOutBut = new Ext.ux.ImageButton({
	imgPath : "../images/ResLogout.png",
	imgWidth : 50,
	imgHeight : 50,
	tooltip : "登出",
	handler : function(btn) {
	}
});

// ----------------- dymatic searchForm -----------------
// combom
var filterTypeData = [ [ "0", "全部" ], [ "1", "编号" ], [ "2", "名称" ],
		[ "3", "拼音" ], [ "4", "价格" ], [ "5", "厨房" ] ];
var filterTypeComb = new Ext.form.ComboBox({
	fieldLabel : "过滤",
	forceSelection : true,
	width : 100,
	value : "全部",
	id : "filter",
	store : new Ext.data.SimpleStore({
		fields : [ "value", "text" ],
		data : filterTypeData
	}),
	valueField : "value",
	displayField : "text",
	typeAhead : true,
	mode : "local",
	triggerAction : "all",
	selectOnFocus : true,
	allowBlank : false,
	listeners : {
		select : function(combo, record, index) {

			// ------------------dymatic field-------------------
			var conditionText = new Ext.form.TextField({
				hideLabel : true,
				id : "conditionText",
				allowBlank : false,
				width : 120
			});

			var conditionNumber = new Ext.form.NumberField({
				hideLabel : true,
				id : "conditionNumber",
				allowBlank : false,
				width : 120
			});

			var conditionDate = new Ext.form.TimeField({
				hideLabel : true,
				id : "conditionDate",
				allowBlank : false,
				format : "H:i:s",
				width : 120
			});

			// var kitchenTypeData = [ [ "1", "明档" ], [ "2", "烧味" ],
			// [ "3", "海鲜" ], [ "4", "厨房4" ], [ "5", "厨房5" ],
			// [ "6", "厨房6" ], [ "7", "厨房7" ], [ "8", "厨房8" ],
			// [ "9", "厨房9" ], [ "10", "厨房10" ], [ "11", "空" ] ];
			var kitchenTypeComb = new Ext.form.ComboBox({
				hideLabel : true,
				forceSelection : true,
				width : 120,
				value : kitchenTypeData[0][1],
				id : "kitchenTypeComb",
				store : new Ext.data.SimpleStore({
					fields : [ "value", "text" ],
					data : kitchenTypeData
				}),
				valueField : "value",
				displayField : "text",
				typeAhead : true,
				mode : "local",
				triggerAction : "all",
				selectOnFocus : true,
				allowBlank : false
			});

			// ------------------remove field-------------------
			if (conditionType == "text") {
				searchForm.remove("conditionText");
			} else if (conditionType == "number") {
				searchForm.remove("conditionNumber");
			} else if (conditionType == "date") {
				searchForm.remove("conditionDate");
			} else if (conditionType == "kitchenTypeComb") {
				searchForm.remove("kitchenTypeComb");
			}

			// ------------------ add field -------------------
			operatorComb.setDisabled(false);
			// [ "0", "全部" ], [ "1", "编号" ], [ "2", "名称" ],[ "3", "拼音" ], [ "4",
			// "价格" ], [ "5", "厨房" ]
			if (index == 0) {
				// 全部
				// searchForm.add(conditionText);
				operatorComb.setValue("等于");
				operatorComb.setDisabled(true);
				conditionType = "text";
			} else if (index == 1) {
				// 编号
				searchForm.add(conditionNumber);
				conditionType = "number";
			} else if (index == 2) {
				// 名称
				searchForm.add(conditionText);
				operatorComb.setValue("等于");
				operatorComb.setDisabled(true);
				conditionType = "text";
			} else if (index == 3) {
				// 拼音
				searchForm.add(conditionText);
				operatorComb.setValue("等于");
				operatorComb.setDisabled(true);
				conditionType = "text";
			} else if (index == 4) {
				// 价格
				searchForm.add(conditionNumber);
				conditionType = "number";
			} else if (index == 5) {
				// 厨房
				searchForm.add(kitchenTypeComb);
				operatorComb.setValue("等于");
				operatorComb.setDisabled(true);
				// payTypeComb.setValue("现金");
				conditionType = "kitchenTypeComb";
			}

			menuQueryCondPanel.doLayout();
		}
	}
});

var operatorData = [ [ "1", "等于" ], [ "2", "大于等于" ], [ "3", "小于等于" ] ];
var operatorComb = new Ext.form.ComboBox({
	hideLabel : true,
	forceSelection : true,
	width : 100,
	value : "等于",
	id : "operator",
	disabled : true,
	store : new Ext.data.SimpleStore({
		fields : [ "value", "text" ],
		data : operatorData
	}),
	valueField : "value",
	displayField : "text",
	typeAhead : true,
	mode : "local",
	triggerAction : "all",
	selectOnFocus : true,
	allowBlank : false
});

var searchForm = new Ext.Panel({
	border : false,
	width : 130,
	id : "searchForm",
	items : [ {
		xtype : "textfield",
		hideLabel : true,
		id : "conditionText",
		allowBlank : false,
		width : 120
	} ]
});

// ----------------- dymatic checkbox Form -----------------

// panel
var menuQueryCondPanel = new Ext.form.FormPanel({
	region : "north",
	border : false,
	height : 23,
	bodyStyle : "margin-top:5px;",
	items : [ {
		layout : "column",
		border : false,
		anchor : "98%",
		items : [ {
			layout : "form",
			labelWidth : 40,
			border : false,
			labelSeparator : '：',
			width : 150,
			items : filterTypeComb
		}, {
			layout : "form",
			border : false,
			width : 110,
			items : operatorComb
		}, searchForm, {
			layout : "form",
			border : false,
			labelSeparator : '',
			width : 70,
			labelWidth : 30,
			items : [ {
				xtype : "checkbox",
				id : "specialCheckbox",
				fieldLabel : "特价"
			} ]
		}, {
			layout : "form",
			border : false,
			labelSeparator : '',
			width : 70,
			labelWidth : 30,
			items : [ {
				xtype : "checkbox",
				id : "recommendCheckbox",
				fieldLabel : "推荐"
			} ]
		}, {
			layout : "form",
			border : false,
			labelSeparator : '',
			width : 70,
			labelWidth : 30,
			items : [ {
				xtype : "checkbox",
				id : "freeCheckbox",
				fieldLabel : "赠送"
			} ]
		}, {
			layout : "form",
			border : false,
			labelSeparator : '',
			width : 70,
			labelWidth : 30,
			items : [ {
				xtype : "checkbox",
				id : "stopCheckbox",
				fieldLabel : "停售"
			} ]
		}, {
			layout : 'form',
			border : false,
			width : 70,
			items : [ {
				xtype : "button",
				hideLabel : true,
				id : "srchBtn",
				text : "搜索",
				width : 100,
				listeners : {
					"click" : function(thiz, e) {
						menuStore.reload({
							params : {
								start : 0,
								limit : dishesPageRecordCount
							}
						});
					}
				}
			} ]
		} ]
	} ]
});

// operator function
function dishModifyHandler(rowIndex) {

	var currRecord = menuStore.getAt(rowIndex);
	menuModifyWin.findById("menuModNumber").setValue(
			currRecord.get("dishNumber"));
	menuModifyWin.findById("menuModName").setValue(currRecord.get("dishName"));
	menuModifyWin.findById("menuModSpill")
			.setValue(currRecord.get("dishSpill"));
	menuModifyWin.findById("menuModPrice")
			.setValue(currRecord.get("dishPrice"));
	menuModifyWin.findById("kitchenTypeCombMM").setValue(
			currRecord.get("kitchenDisplay"));

	menuModifyWin.findById("specialCheckboxMM").setValue(
			currRecord.get("special"));
	menuModifyWin.findById("recommendCheckboxMM").setValue(
			currRecord.get("recommend"));
	menuModifyWin.findById("freeCheckboxMM").setValue(currRecord.get("free"));
	menuModifyWin.findById("stopCheckboxMM").setValue(currRecord.get("stop"));

	menuModifyWin.show();

};

function dishDeleteHandler(rowIndex) {

};

function dishRelateHandler(rowIndex) {

};

function menuDishOpt(value, cellmeta, record, rowIndex, columnIndex, store) {
	return "<center><a href=\"javascript:dishModifyHandler(" + rowIndex
			+ ")\">" + "<img src='../images/Modify.png'/>修改</a>"
			+ "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;"
			+ "<a href=\"javascript:dishDeleteHandler()\">"
			+ "<img src='../images/del.png'/>删除</a>"
			+ "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;"
			+ "<a href=\"javascript:dishRelateHandler()\">"
			+ "<img src='../images/Modify.png'/>关联</a>" + "</center>";
};

// 1，表格的数据store
// 编号，名称，拼音，价格，厨房打印，操作，特,荐,停,送
var menuStore = new Ext.data.Store({
	proxy : new Ext.data.HttpProxy({
		url : "../QueryMenuMgr.do"
	}),
	reader : new Ext.data.JsonReader({
		totalProperty : "totalProperty",
		root : "root"
	}, [ {
		name : "dishNumber"
	}, {
		name : "dishName"
	}, {
		name : "dishNameDisplay"
	}, {
		name : "dishSpill"
	}, {
		name : "dishPrice"
	}, {
		name : "kitchen"
	}, {
		name : "kitchenDisplay"
	}, {
		name : "operator"
	}, {
		name : "special"
	}, {
		name : "recommend"
	}, {
		name : "stop"
	}, {
		name : "free"
	}, {
		name : "message"
	} ])
});

// menuStore.reload();

// 2，栏位模型
var menuColumnModel = new Ext.grid.ColumnModel([ new Ext.grid.RowNumberer(), {
	header : "编号",
	sortable : true,
	dataIndex : "dishNumber",
	width : 180
}, {
	header : "名称",
	sortable : true,
	dataIndex : "dishNameDisplay",
	width : 250
}, {
	header : "拼音",
	sortable : true,
	dataIndex : "dishSpill",
	width : 180
}, {
	header : "价格（￥）",
	sortable : true,
	dataIndex : "dishPrice",
	width : 180
}, {
	header : "厨房打印",
	sortable : true,
	dataIndex : "kitchenDisplay",
	width : 180
}, {
	header : "<center>操作</center>",
	sortable : true,
	dataIndex : "operator",
	width : 330,
	renderer : menuDishOpt
} ]);

Ext
		.onReady(function() {
			// 解决ext中文传入后台变问号问题
			Ext.lib.Ajax.defaultPostHeader += '; charset=utf-8';
			Ext.QuickTips.init();

			// ---------------------表格--------------------------
			var menuGrid = new Ext.grid.GridPanel({
				title : "菜品",
				xtype : "grid",
				anchor : "99%",
				region : "center",
				border : false,
				ds : menuStore,
				cm : menuColumnModel,
				sm : new Ext.grid.RowSelectionModel({
					singleSelect : true
				}),
				listeners : {
					rowclick : function(thiz, rowIndex, e) {
						currRowIndex = rowIndex;
					}
				},
				bbar : new Ext.PagingToolbar({
					pageSize : dishesPageRecordCount,
					store : menuStore,
					displayInfo : true,
					displayMsg : '显示第 {0} 条到 {1} 条记录，共 {2} 条',
					emptyMsg : "没有记录"
				}),
				autoScroll : true,
				loadMask : {
					msg : "数据加载中，请稍等..."
				}
			});

			// 为store配置beforeload监听器
			menuGrid.getStore()
					.on(
							'beforeload',
							function() {

								var queryTpye = filterTypeComb.getValue();
								if (queryTpye == "全部") {
									queryTpye = 0;
								}

								var queryOperator = operatorComb.getValue();
								if (queryOperator == "等于") {
									queryOperator = 1;
								}

								var queryValue = "";
								if (conditionType == "text" && queryTpye != 0) {
									queryValue = searchForm.findById(
											"conditionText").getValue();
									if (!searchForm.findById("conditionText")
											.isValid()) {
										return false;
									}
								} else if (conditionType == "number") {
									queryValue = searchForm.findById(
											"conditionNumber").getValue();
									if (!searchForm.findById("conditionNumber")
											.isValid()) {
										return false;
									}
								} else if (conditionType == "kitchenTypeComb") {
									queryValue = searchForm.findById(
											"kitchenTypeComb").getValue();
									if (queryValue == kitchenTypeData[0][1]) {
										queryValue = 1;
									}
								}

								var in_isSpecial = menuQueryCondPanel.findById(
										"specialCheckbox").getValue();
								var in_isRecommend = menuQueryCondPanel
										.findById("recommendCheckbox")
										.getValue();
								var in_isFree = menuQueryCondPanel.findById(
										"freeCheckbox").getValue();
								var in_isStop = menuQueryCondPanel.findById(
										"stopCheckbox").getValue();

								// 输入查询条件参数
								this.baseParams = {
									"pin" : pin,
									"type" : queryTpye,
									"ope" : queryOperator,
									"value" : queryValue,
									"isSpecial" : in_isSpecial,
									"isRecommend" : in_isRecommend,
									"isFree" : in_isFree,
									"isStop" : in_isStop
								};

							});

			// 为store配置load监听器(即load完后动作)
			menuGrid
					.getStore()
					.on(
							'load',
							function() {
								if (menuGrid.getStore().getTotalCount() != 0) {
									var msg = this.getAt(0).get("message");
									if (msg != "normal") {
										Ext.MessageBox.show({
											msg : msg,
											width : 300,
											buttons : Ext.MessageBox.OK
										});
										this.removeAll();
									} else {
										menuGrid
												.getStore()
												.each(
														function(record) {
															// 廚房顯示
															for ( var i = 0; i < kitchenTypeData.length; i++) {
																if (record
																		.get("kitchen") == kitchenTypeData[i][0]) {
																	record
																			.set(
																					"kitchenDisplay",
																					kitchenTypeData[i][1]);
																}
															}
															// 菜品狀態顯示
															record
																	.set(
																			"dishNameDisplay",
																			record
																					.get("dishName"));
															if (record
																	.get("special") == true) {
																record
																		.set(
																				"dishNameDisplay",
																				record
																						.get("dishNameDisplay")
																						+ "<img src='../images/icon_tip_te.gif'></img>");
															}
															if (record
																	.get("recommend") == true) {
																record
																		.set(
																				"dishNameDisplay",
																				record
																						.get("dishNameDisplay")
																						+ "<img src='../images/icon_tip_jian.gif'></img>");
															}
															if (record
																	.get("stop") == true) {
																record
																		.set(
																				"dishNameDisplay",
																				record
																						.get("dishNameDisplay")
																						+ "<img src='../images/icon_tip_ting.gif'></img>");
															}
															if (record
																	.get("free") == true) {
																record
																		.set(
																				"dishNameDisplay",
																				record
																						.get("dishNameDisplay")
																						+ "<img src='../images/forFree.png'></img>");
															}

															// 提交，去掉修改標記
															record.commit();
														});
									}
								}
							});
			// ---------------------end 表格--------------------------

			var centerPanel = new Ext.Panel({
				region : "center",
				layout : "fit",
				frame : true,
				items : [ {
					layout : "border",
					title : "<div style='font-size:20px;'>菜品管理<div>",
					items : [ menuQueryCondPanel, menuGrid ]
				} ],
				tbar : new Ext.Toolbar({
					height : 55,
					items : [ orderStatiBut, {
						text : "&nbsp;&nbsp;&nbsp;",
						disabled : true
					}, dishAddBut, {
						text : "&nbsp;&nbsp;&nbsp;",
						disabled : true
					}, "->", pushBackBut, {
						text : "&nbsp;&nbsp;&nbsp;",
						disabled : true
					}, logOutBut ]
				})
			});

			var viewport = new Ext.Viewport(
					{
						layout : "border",
						id : "viewport",
						items : [
								{
									region : "north",
									bodyStyle : "background-color:#A9D0F5",
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
