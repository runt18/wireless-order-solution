﻿//----------------- 點菜統計 --------------------
// 条件框
var dishMultSelectDS = new Ext.data.SimpleStore({
	fields : [ "retrunValue", "displayText" ],
	data : []
});

menuStatWin = new Ext.Window(
		{
			title : "点菜统计",
			width : 450,
			height : 380,
			closeAction : "hide",
			resizable : false,
			layout : "anchor",
			items : [ {
				border : false,
				anchor : "right 10%",
				items : [ {
					layout : "column",
					border : false,
					frame : true,
					anchor : "98%",
					items : [ {
						layout : "form",
						border : false,
						labelSeparator : ' ',
						width : 200,
						labelWidth : 30,
						items : [ {
							xtype : "datefield",
							id : "begDateMStat",
							width : 150,
							fieldLabel : "日期"
						} ]
					}, {
						layout : "form",
						border : false,
						labelSeparator : ' ',
						width : 200,
						labelWidth : 30,
						items : [ {
							xtype : "datefield",
							id : "endDateMStat",
							width : 150,
							fieldLabel : "至"
						} ]
					} ]
				} ]
			}, {
				layout : "form",
				border : false,
				frame : true,
				hideLabels : true,
				anchor : "right 91%",
				items : [ {
					xtype : "itemselector",
					name : "dishMultSelectMStat",
					id : "dishMultSelectMStat",
					fromStore : dishMultSelectDS,
					dataFields : [ "retrunValue", "displayText" ],
					toData : [ [ "", "" ] ],
					msHeight : 250,
					valueField : "retrunValue",
					displayField : "displayText",
					imagePath : "../../extjs/multiselect/images/",
					toLegend : "已选择菜品",
					fromLegend : "可选择菜品"
				} ]
			} ],
			buttons : [
					{
						text : "全选",
						handler : function() {
							var i = menuStatWin.findById("dishMultSelectMStat").fromMultiselect.view;
							i.selectRange(0, i.store.length);
							menuStatWin.findById("dishMultSelectMStat")
									.fromTo();
							menuStatWin.findById("dishMultSelectMStat").toMultiselect.view
									.clearSelections();
						}
					},
					{
						text : "清空",
						handler : function() {
							menuStatWin.findById("dishMultSelectMStat").reset();
						}
					},
					{
						text : "确定",
						handler : function() {

							var selectCount = menuStatWin
									.findById("dishMultSelectMStat").toMultiselect.store
									.getCount();

							if (selectCount != 0) {
								menuStatWin.hide();

								var selectDishes = "";
								for ( var i = 0; i < selectCount; i++) {
									var selectItem = menuStatWin
											.findById("dishMultSelectMStat").toMultiselect.store
											.getAt(i).get("retrunValue");
									if (selectItem != "") {
										selectDishes = selectDishes
												+ selectItem + ",";
									}
								}
								// 去掉最后一个逗号
								selectDishes = selectDishes.substring(0,
										selectDishes.length - 1);

								// 保存条件
								orderStaticBeginDate = menuStatWin.findById(
										"begDateMStat").getValue();
								if (orderStaticBeginDate != "") {
									var dateFormated = new Date();
									dateFormated = orderStaticBeginDate;
									orderStaticBeginDate = dateFormated
											.format('Y-m-d');
								}

								orderStaticEndDate = menuStatWin.findById(
										"endDateMStat").getValue();
								if (orderStaticEndDate != "") {
									var dateFormated = new Date();
									dateFormated = orderStaticEndDate;
									orderStaticEndDate = dateFormated
											.format('Y-m-d');
								}

								orderStaticDishesString = selectDishes;

								menuStatResultWin.show();

							} else {
								Ext.MessageBox.show({
									msg : "至少需要选择一个菜品进行统计",
									width : 300,
									buttons : Ext.MessageBox.OK
								});
							}
						}
					}, {
						text : "取消",
						handler : function() {
							menuStatWin.hide();
						}
					} ],
			listeners : {
				"show" : function(thiz) {

					dishMultSelectData = [];
					menuStatWin.findById("dishMultSelectMStat").reset();
					menuStatWin.findById("begDateMStat").setValue("");
					menuStatWin.findById("endDateMStat").setValue("");

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
						queryValue = searchForm.findById("conditionText")
								.getValue();
						if (!searchForm.findById("conditionText").isValid()) {
							return false;
						}
					} else if (conditionType == "number") {
						queryValue = searchForm.findById("conditionNumber")
								.getValue();
						if (!searchForm.findById("conditionNumber").isValid()) {
							return false;
						}
					} else if (conditionType == "kitchenTypeComb") {
						queryValue = searchForm.findById("kitchenTypeComb")
								.getValue();
						if (queryValue == kitchenTypeData[0][1]) {
							queryValue = 1;
						}
					}

					var in_isSpecial = menuQueryCondPanel.findById(
							"specialCheckbox").getValue();
					var in_isRecommend = menuQueryCondPanel.findById(
							"recommendCheckbox").getValue();
					var in_isFree = menuQueryCondPanel.findById("freeCheckbox")
							.getValue();
					var in_isStop = menuQueryCondPanel.findById("stopCheckbox")
							.getValue();

					// 输入查询条件参数
					Ext.Ajax.request({
						url : "../../QueryMenuMgr.do",
						params : {
							"pin" : pin,
							"type" : queryTpye,
							"ope" : queryOperator,
							"value" : queryValue,
							"isSpecial" : in_isSpecial,
							"isRecommend" : in_isRecommend,
							"isFree" : in_isFree,
							"isStop" : in_isStop,
							"isPaging" : false
						},
						success : function(response, options) {
							var resultJSON = Ext.util.JSON
									.decode(response.responseText);
							var root = resultJSON.root;
							if (root[0].message == "normal") {
								for ( var i = 0; i < root.length; i++) {
									dishMultSelectData.push([
											root[i].dishNumber,
											root[i].dishName ]);
								}
								dishMultSelectDS.loadData(dishMultSelectData);
							} else {
								Ext.MessageBox.show({
									msg : root[0].message,
									width : 300,
									buttons : Ext.MessageBox.OK
								});
							}

						},
						failure : function(response, options) {
						}
					});

				}
			}
		});

// 结果框
// 后台：[菜品ｉｄ，菜品名稱，菜品單價，是否臨時，總數量，總價格]
var menuStatResultStore = new Ext.data.Store({
	proxy : new Ext.data.HttpProxy({
		url : "../../MenuStatistics.do"
	}),
	reader : new Ext.data.JsonReader({
		totalProperty : "totalProperty",
		root : "root"
	}, [ {
		name : "dishNumber"
	}, {
		name : "dishName"
	}, {
		name : "dishPrice"
	}, {
		name : "isTemp"
	}, {
		name : "kitchen"
	}, {
		name : "kitchenDisplay"
	}, {
		name : "dishCount"
	}, {
		name : "dishTotalPrice"
	}, {
		name : "message"
	} ])
});

// 2，栏位模型
var menuStatResultColumnModel = new Ext.grid.ColumnModel([
		new Ext.grid.RowNumberer(), {
			header : "编号",
			sortable : true,
			dataIndex : "dishNumber",
			width : 80
		}, {
			header : "菜名",
			sortable : true,
			dataIndex : "dishName",
			width : 100
		}, {
			header : "厨房",
			sortable : true,
			dataIndex : "kitchenDisplay",
			width : 80
		}, {
			header : "数量",
			sortable : true,
			dataIndex : "dishCount",
			width : 60
		}, {
			header : "金额（￥）",
			sortable : true,
			dataIndex : "dishTotalPrice",
			width : 80
		} ]);

var menuStatResultGrid = new Ext.grid.GridPanel({
	// title : "菜品",
	xtype : "grid",
	anchor : "99%",
	border : false,
	ds : menuStatResultStore,
	cm : menuStatResultColumnModel,
	sm : new Ext.grid.RowSelectionModel({
		singleSelect : true
	}),
	bbar : new Ext.PagingToolbar({
		pageSize : dishesStaticRecordCount,
		store : menuStatResultStore,
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
menuStatResultGrid.getStore().on('beforeload', function() {

	// 输入查询条件参数
	this.baseParams = {
		"pin" : pin,
		"foodIDs" : orderStaticDishesString,
		"dateBegin" : orderStaticBeginDate,
		"dateEnd" : orderStaticEndDate
	};

});

// 为store配置load监听器(即load完后动作)
menuStatResultGrid.getStore().on('load', function() {
	if (menuStatResultGrid.getStore().getTotalCount() != 0) {
		var msg = this.getAt(0).get("message");
		if (msg != "normal") {
			Ext.MessageBox.show({
				msg : msg,
				width : 300,
				buttons : Ext.MessageBox.OK
			});
			this.removeAll();
		} else {
			menuStatResultGrid.getStore().each(function(record) {
				// 廚房顯示
				for ( var i = 0; i < kitchenTypeData.length; i++) {
					if (record.get("kitchen") == kitchenTypeData[i][0]) {
						record.set("kitchenDisplay", kitchenTypeData[i][1]);
					}
				}

				// 提交，去掉修改標記
				record.commit();
			});
		}
	}
});

menuStatResultWin = new Ext.Window({
	title : "统计结果",
	width : 450,
	height : 380,
	closeAction : "hide",
	resizable : false,
	layout : "fit",
	items : menuStatResultGrid,
	buttons : [ {
		text : "退出",
		handler : function() {
			menuStatResultWin.hide();
		}
	} ],
	listeners : {
		"show" : function(thiz) {
			menuStatResultGrid.getStore().reload({
				params : {
					start : 0,
					limit : dishesStaticRecordCount
				}
			});
		}
	}
});

// ----------------- 增加新菜 --------------------
var kitchenTypeStoreMA = new Ext.data.Store({
	proxy : new Ext.data.HttpProxy({
		url : "../../QueryMenu.do?pin=" + pin + "&type=4"
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

var kitchenTypeCombMA = new Ext.form.ComboBox({
	fieldLabel : "厨房",
	forceSelection : true,
	width : 220,
	// value : kitchenTypeData[0][1],
	id : "kitchenTypeCombMA",
	store : kitchenTypeStoreMA,
	valueField : "value",
	displayField : "text",
	typeAhead : true,
	mode : "local",
	triggerAction : "all",
	selectOnFocus : true,
	allowBlank : false
});

menuAddWin = new Ext.Window({
	layout : "fit",
	title : "添加新菜",
	width : 315,
	height : 235,
	closeAction : "hide",
	resizable : false,
	items : [ {
		layout : "form",
		id : "menuAddForm",
		labelWidth : 30,
		border : false,
		frame : true,
		items : [ {
			xtype : "numberfield",
			fieldLabel : "编号",
			id : "menuAddNumber",
			allowBlank : false,
			width : 220
		}, {
			xtype : "textfield",
			fieldLabel : "菜名",
			id : "menuAddName",
			allowBlank : false,
			width : 220
		}, {
			xtype : "textfield",
			fieldLabel : "拼音",
			id : "menuAddSpill",
			allowBlank : false,
			width : 220
		}, {
			xtype : "numberfield",
			fieldLabel : "价格",
			id : "menuAddPrice",
			allowBlank : false,
			width : 220
		}, kitchenTypeCombMA, {
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
					id : "specialCheckboxMA",
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
					id : "recommendCheckboxMA",
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
					id : "freeCheckboxMA",
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
					id : "stopCheckboxMA",
					fieldLabel : "停售"
				} ]
			} ]
		} ]
	} ],
	buttons : [
			{
				text : "确定",
				handler : function() {
					if (menuAddWin.findById("menuAddNumber").isValid()
							&& menuAddWin.findById("menuAddName").isValid()
							&& menuAddWin.findById("menuAddSpill").isValid()
							&& menuAddWin.findById("menuAddPrice").isValid()) {

						var dishNumber = menuAddWin.findById("menuAddNumber")
								.getValue();
						var dishName = menuAddWin.findById("menuAddName")
								.getValue();
						var dishSpill = menuAddWin.findById("menuAddSpill")
								.getValue();
						var dishPrice = menuAddWin.findById("menuAddPrice")
								.getValue();
						var kitchen = kitchenTypeCombMA.getValue();
						// 前台：kitchenTypeData：[厨房编号,厨房名称]
						for ( var i = 0; i < kitchenTypeData.length; i++) {
							if (kitchen == kitchenTypeData[i][1]) {
								kitchen = kitchenTypeData[i][0];
							}
						}
						var isSpecial = menuAddWin
								.findById("specialCheckboxMA").getValue();
						var isRecommend = menuAddWin.findById(
								"recommendCheckboxMA").getValue();
						var isFree = menuAddWin.findById("freeCheckboxMA")
								.getValue();
						var isStop = menuAddWin.findById("stopCheckboxMA")
								.getValue();

						var isDuplicate = false;
						for ( var i = 0; i < dishMultSelectData.length; i++) {
							if (dishNumber == dishMultSelectData[i][0]) {
								isDuplicate = true;
							}
						}

						if (!isDuplicate) {
							menuAddWin.hide();

							Ext.Ajax.request({
								url : "../../InsertMenu.do",
								params : {
									"pin" : Request["pin"],
									"dishNumber" : dishNumber,
									"dishName" : dishName,
									"dishSpill" : dishSpill,
									"dishPrice" : dishPrice,
									"kitchen" : kitchen,
									"isSpecial" : isSpecial,
									"isRecommend" : isRecommend,
									"isFree" : isFree,
									"isStop" : isStop
								},
								success : function(response, options) {
									var resultJSON = Ext.util.JSON
											.decode(response.responseText);
									if (resultJSON.success == true) {
										menuStore.reload({
											params : {
												start : 0,
												limit : dishesPageRecordCount
											}
										});

										var dataInfo = resultJSON.data;
										Ext.MessageBox.show({
											msg : dataInfo,
											width : 300,
											buttons : Ext.MessageBox.OK
										});
									} else {
										var dataInfo = resultJSON.data;
										Ext.MessageBox.show({
											msg : dataInfo,
											width : 300,
											buttons : Ext.MessageBox.OK
										});
									}
								},
								failure : function(response, options) {
								}
							});
						} else {
							Ext.MessageBox.show({
								msg : "该菜品编号已存在！",
								width : 300,
								buttons : Ext.MessageBox.OK
							});
						}

					}

				}
			}, {
				text : "取消",
				handler : function() {
					menuAddWin.hide();
				}
			} ],
	listeners : {
		"show" : function(thiz) {
			loadAllDishes();

			kitchenTypeCombMA.setValue(kitchenTypeData[0][1]);
			kitchenTypeStoreMA.reload();

			menuAddWin.findById("menuAddNumber").setValue("");
			menuAddWin.findById("menuAddNumber").clearInvalid();

			menuAddWin.findById("menuAddName").setValue("");
			menuAddWin.findById("menuAddName").clearInvalid();

			menuAddWin.findById("menuAddSpill").setValue("");
			menuAddWin.findById("menuAddSpill").clearInvalid();

			menuAddWin.findById("menuAddPrice").setValue("");
			menuAddWin.findById("menuAddPrice").clearInvalid();

			menuAddWin.findById("specialCheckboxMA").setValue(false);
			menuAddWin.findById("recommendCheckboxMA").setValue(false);
			menuAddWin.findById("freeCheckboxMA").setValue(false);
			menuAddWin.findById("stopCheckboxMA").setValue(false);

			var f = Ext.get("menuAddNumber");
			f.focus.defer(100, f); // 为什么这样才可以！？！？
		}
	}
});

// ----------------- 菜谱修改 --------------------
var kitchenTypeStoreMM = new Ext.data.Store({
	proxy : new Ext.data.HttpProxy({
		url : "../../QueryMenu.do?pin=" + pin + "&type=4"
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

menuModifyWin = new Ext.Window(
		{
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
					allowBlank : false,
					width : 220
				}, {
					xtype : "textfield",
					fieldLabel : "拼音",
					id : "menuModSpill",
					allowBlank : false,
					width : 220
				}, {
					xtype : "numberfield",
					fieldLabel : "价格",
					id : "menuModPrice",
					allowBlank : false,
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
			buttons : [
					{
						text : "确定",
						handler : function() {
							if (menuModifyWin.findById("menuModNumber")
									.isValid()
									&& menuModifyWin.findById("menuModName")
											.isValid()
									&& menuModifyWin.findById("menuModSpill")
											.isValid()
									&& menuModifyWin.findById("menuModPrice")
											.isValid()) {
								menuModifyWin.hide();

								var dishNumber = menuModifyWin.findById(
										"menuModNumber").getValue();
								var dishName = menuModifyWin.findById(
										"menuModName").getValue();
								var dishSpill = menuModifyWin.findById(
										"menuModSpill").getValue();
								var dishPrice = menuModifyWin.findById(
										"menuModPrice").getValue();
								var kitchen = kitchenTypeCombMM.getValue();
								// 前台：kitchenTypeData：[厨房编号,厨房名称]
								for ( var i = 0; i < kitchenTypeData.length; i++) {
									if (kitchen == kitchenTypeData[i][1]) {
										kitchen = kitchenTypeData[i][0];
									}
								}
								var isSpecial = menuModifyWin.findById(
										"specialCheckboxMM").getValue();
								var isRecommend = menuModifyWin.findById(
										"recommendCheckboxMM").getValue();
								var isFree = menuModifyWin.findById(
										"freeCheckboxMM").getValue();
								var isStop = menuModifyWin.findById(
										"stopCheckboxMM").getValue();

								Ext.Ajax
										.request({
											url : "../../UpdateMenu.do",
											params : {
												"pin" : Request["pin"],
												"dishNumber" : dishNumber,
												"dishName" : dishName,
												"dishSpill" : dishSpill,
												"dishPrice" : dishPrice,
												"kitchen" : kitchen,
												"isSpecial" : isSpecial,
												"isRecommend" : isRecommend,
												"isFree" : isFree,
												"isStop" : isStop
											},
											success : function(response,
													options) {
												var resultJSON = Ext.util.JSON
														.decode(response.responseText);
												if (resultJSON.success == true) {
													menuStore
															.reload({
																params : {
																	start : (currPageIndex - 1)
																			* dishesPageRecordCount,
																	limit : dishesPageRecordCount
																}
															});

													var dataInfo = resultJSON.data;
													Ext.MessageBox
															.show({
																msg : dataInfo,
																width : 300,
																buttons : Ext.MessageBox.OK
															});
												} else {
													var dataInfo = resultJSON.data;
													Ext.MessageBox
															.show({
																msg : dataInfo,
																width : 300,
																buttons : Ext.MessageBox.OK
															});
												}
											},
											failure : function(response,
													options) {
											}
										});
							}
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
	imgPath : "../../images/orderStatic.png",
	imgWidth : 50,
	imgHeight : 50,
	tooltip : "点菜统计",
	handler : function(btn) {
		menuStatWin.show();
	}
});

var dishAddBut = new Ext.ux.ImageButton({
	imgPath : "../../images/dishAdd.png",
	imgWidth : 50,
	imgHeight : 50,
	tooltip : "添加新菜",
	handler : function(btn) {
		menuAddWin.show();
	}
});

// --
var pushBackBut = new Ext.ux.ImageButton({
	imgPath : "../../images/UserLogout.png",
	imgWidth : 50,
	imgHeight : 50,
	tooltip : "返回",
	handler : function(btn) {
		location.href = "MenuProtal.html?restaurantID=" + restaurantID
				+ "&isNewAccess=false&pin=" + pin;
	}
});

var logOutBut = new Ext.ux.ImageButton({
	imgPath : "../../images/ResLogout.png",
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
	// border : false,
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
	// 獲取分頁表格的當前頁碼！神技！！！
	var toolbar = menuGrid.getBottomToolbar();
	currPageIndex = toolbar.readPage(toolbar.getPageData());

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
	Ext.MessageBox.show({
		msg : "确定删除？",
		width : 300,
		buttons : Ext.MessageBox.YESNO,
		fn : function(btn) {
			if (btn == "yes") {
				var dishNumber = menuStore.getAt(rowIndex).get("dishNumber");

				Ext.Ajax.request({
					url : "../../DeleteMenu.do",
					params : {
						"pin" : pin,
						"dishNumber" : dishNumber
					},
					success : function(response, options) {
						var resultJSON = Ext.util.JSON
								.decode(response.responseText);
						if (resultJSON.success == true) {
							menuStore.reload({
								params : {
									start : 0,
									limit : dishesPageRecordCount
								}
							});

							var dataInfo = resultJSON.data;
							Ext.MessageBox.show({
								msg : dataInfo,
								width : 300,
								buttons : Ext.MessageBox.OK
							});
						} else {
							var dataInfo = resultJSON.data;
							Ext.MessageBox.show({
								msg : dataInfo,
								width : 300,
								buttons : Ext.MessageBox.OK
							});
						}
					},
					failure : function(response, options) {
					}
				});
			}
		}
	});
};

// function dishRelateHandler(rowIndex) {
//
// };

function menuDishOpt(value, cellmeta, record, rowIndex, columnIndex, store) {
	return "<center><a href=\"javascript:dishModifyHandler(" + rowIndex
			+ ")\">" + "<img src='../../images/Modify.png'/>修改</a>"
			+ "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;"
			+ "<a href=\"javascript:dishDeleteHandler(" + rowIndex + ")\">"
			+ "<img src='../../images/del.png'/>删除</a>"
			// + "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;"
			// + "<a href=\"javascript:dishRelateHandler(" + rowIndex + ")\">"
			// + "<img src='../images/Modify.png'/>关联</a>"
			+ "</center>";
};

// 1，表格的数据store
// 编号，名称，拼音，价格，厨房打印，操作，特,荐,停,送
var menuStore = new Ext.data.Store({
	proxy : new Ext.data.HttpProxy({
		url : "../../QueryMenuMgr.do"
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
	width : 80
}, {
	header : "名称",
	sortable : true,
	dataIndex : "dishNameDisplay",
	width : 180
}, {
	header : "拼音",
	sortable : true,
	dataIndex : "dishSpill",
	width : 80
}, {
	header : "价格（￥）",
	sortable : true,
	dataIndex : "dishPrice",
	width : 90
}, {
	header : "厨房打印",
	sortable : true,
	dataIndex : "kitchenDisplay",
	width : 80
}, {
	header : "<center>操作</center>",
	sortable : true,
	dataIndex : "operator",
	width : 230,
	renderer : menuDishOpt
} ]);

// -------------- 關聯食材 ---------------
var materialStore = new Ext.data.Store({
	proxy : new Ext.data.MemoryProxy(materialData),
	reader : new Ext.data.ArrayReader({}, [ {
		name : "materialNumber"
	}, {
		name : "materialName"
	}, {
		name : "materialCost"
	}, {
		name : "materialOpt"
	} ])
});

// 2，栏位模型
var materialColumnModel = new Ext.grid.ColumnModel([
		new Ext.grid.RowNumberer(), {
			header : "编号",
			sortable : true,
			dataIndex : "materialNumber",
			width : 50
		}, {
			header : "食材",
			sortable : true,
			dataIndex : "materialName",
			width : 80
		}, {
			header : "消耗",
			sortable : true,
			dataIndex : "materialCost",
			width : 50
		}, {
			header : "操作",
			sortable : true,
			dataIndex : "materialOpt",
			width : 100
		} ]);

var materialGrid = new Ext.grid.GridPanel({
	xtype : "grid",
	anchor : "99%",
	border : true,
	ds : materialStore,
	cm : materialColumnModel,
	sm : new Ext.grid.RowSelectionModel({
		singleSelect : true
	}),
	listeners : {
	// rowclick : function(thiz, rowIndex, e) {
	// dishOrderCurrRowIndex_ = rowIndex;
	// },
	// render : function(thiz) {
	// orderedDishesOnLoad();
	// tableStuLoad();
	// }
	}
});

var materialPanel = new Ext.Panel({
	region : "east",
	title : "食材关联",
	layout : "fit",
	margins : '0 5 0 0',
	collapsible : true,
	collapsed : true,
	titleCollapse : true,
	frame : true,
	width : 300,
	items : materialGrid,
	listeners : {
		"collapse" : function(panel) {
			menuQueryCondPanel.setHeight(27);
		},
		"expand" : function(panel) {
			menuQueryCondPanel.setHeight(27);
		}
	}
});

// -------------- layout ---------------
var menuGrid;
Ext
		.onReady(function() {
			// 解决ext中文传入后台变问号问题
			Ext.lib.Ajax.defaultPostHeader += '; charset=utf-8';
			Ext.QuickTips.init();

			// ---------------------表格--------------------------
			menuGrid = new Ext.grid.GridPanel({
				title : "菜品",
				xtype : "grid",
				anchor : "99%",
				region : "center",
				frame : true,
				margins : '0 5 0 0',
				ds : menuStore,
				cm : menuColumnModel,
				sm : new Ext.grid.RowSelectionModel({
					singleSelect : true
				}),
				viewConfig : {
					forceFit : true
				},
				listeners : {
					rowclick : function(thiz, rowIndex, e) {
						currRowIndex = rowIndex;

						// 關聯食材

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
				},
				listeners : {
					"render" : function(thiz) {
						menuStore.reload({
							params : {
								start : 0,
								limit : dishesPageRecordCount
							}
						});
					}
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
									"isStop" : in_isStop,
									"isPaging" : true
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
																						+ "<img src='../../images/icon_tip_te.gif'></img>");
															}
															if (record
																	.get("recommend") == true) {
																record
																		.set(
																				"dishNameDisplay",
																				record
																						.get("dishNameDisplay")
																						+ "<img src='../../images/icon_tip_jian.gif'></img>");
															}
															if (record
																	.get("stop") == true) {
																record
																		.set(
																				"dishNameDisplay",
																				record
																						.get("dishNameDisplay")
																						+ "<img src='../../images/icon_tip_ting.gif'></img>");
															}
															if (record
																	.get("free") == true) {
																record
																		.set(
																				"dishNameDisplay",
																				record
																						.get("dishNameDisplay")
																						+ "<img src='../../images/forFree.png'></img>");
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
					items : [ menuQueryCondPanel, menuGrid, materialPanel ]
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
