//--------------------------------------- 入庫統計 --------------------------------------------------------
// 条件框
var discountStatMSDS = new Ext.data.SimpleStore({
	fields : [ "retrunValue", "displayText" ],
	data : []
});

var discountStatFrom = new Ext.form.FormPanel({
	border : false,
	anchor : "right 10%",
	id : "discountStatForm",
	items : [ {
		layout : "column",
		border : false,
		frame : true,
		items : [ {
			layout : "form",
			border : false,
			width : 70,
			items : [ {
				xtype : 'radio',
				hideLabel : true,
				boxLabel : "明细",
				checked : true,
				name : 'discountStat',
				inputValue : 'detail'
			} ]
		}, {
			layout : "form",
			border : false,
			labelSeparator : '',
			width : 130,
			items : [ {
				xtype : 'radio',
				hideLabel : true,
				boxLabel : "按服务员汇总",
				// checked : true,
				name : 'discountStat',
				inputValue : 'sumByStaff'
			} ]
		}, {
			layout : "form",
			border : false,
			labelSeparator : '',
			width : 110,
			items : [ {
				xtype : 'radio',
				hideLabel : true,
				boxLabel : "按账单汇总",
				// checked : true,
				name : 'discountStat',
				inputValue : 'sumByBill'
			} ]
		} ]
	} ]
});

discountStatWin = new Ext.Window(
		{
			title : "折扣统计",
			width : 450,
			height : 430,
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
						labelWidth : 50,
						items : [ {
							xtype : "timefield",
							format : "H:i:s",
							id : "begDateInStatDiscount",
							width : 120,
							fieldLabel : "时间"
						} ]
					}, {
						layout : "form",
						border : false,
						labelSeparator : ' ',
						width : 200,
						labelWidth : 50,
						items : [ {
							xtype : "timefield",
							format : "H:i:s",
							id : "endDateInStatDiscount",
							width : 120,
							fieldLabel : "至"
						} ]
					} ]
				} ]
			}, {
				layout : "form",
				border : false,
				frame : true,
				hideLabels : true,
				anchor : "right 82%",
				items : [ {
					xtype : "itemselector",
					name : "discountStatMultSelect",
					id : "discountStatMultSelect",
					fromStore : discountStatMSDS,
					dataFields : [ "retrunValue", "displayText" ],
					toData : [ [ "", "" ] ],
					msHeight : 263,
					valueField : "retrunValue",
					displayField : "displayText",
					imagePath : "../../extjs/multiselect/images/",
					toLegend : "已选择员工",
					fromLegend : "可选择员工"
				} ]
			}, discountStatFrom ],
			buttons : [
					{
						text : "全选",
						handler : function() {
							var i = discountStatWin
									.findById("discountStatMultSelect").fromMultiselect.view;
							i.selectRange(0, i.store.length);
							discountStatWin.findById("discountStatMultSelect")
									.fromTo();
							discountStatWin.findById("discountStatMultSelect").toMultiselect.view
									.clearSelections();
						}
					},
					{
						text : "清空",
						handler : function() {
							discountStatWin.findById("discountStatMultSelect")
									.reset();
						}
					},
					{
						text : "确定",
						handler : function() {

							var selectCount = discountStatWin
									.findById("discountStatMultSelect").toMultiselect.store
									.getCount();

							if (selectCount != 0) {
								isPrompt = false;
								discountStatWin.hide();

								// -- 獲取選擇的員工 --
								var selectStaffs = "";
								for ( var i = 0; i < selectCount; i++) {
									var selectItem = discountStatWin
											.findById("discountStatMultSelect").toMultiselect.store
											.getAt(i).get("displayText"); // 員工特別處理！！！！！
									// if (selectItem != "") {
									selectStaffs = selectStaffs + "'"
											+ selectItem + "'" + ",";
									// }
								}
								// 去掉最后一个逗号
								selectStaffs = selectStaffs.substring(0,
										selectStaffs.length - 1);
								if (selectStaffs.substring(0, 1) == ",") {
									selectStaffs = selectStaffs.substring(1,
											selectStaffs.length);
								}

								// -- 獲取時間 --
								var dateFormated = new Date();
								var begDateInStatDiscount = discountStatWin
										.findById("begDateInStatDiscount")
										.getValue();
								if (begDateInStatDiscount != "") {
									begDateInStatDiscount = dateFormated
											.format('Y-m-d')
											+ " " + begDateInStatDiscount;
								}

								var endDateInStatDiscount = discountStatWin
										.findById("endDateInStatDiscount")
										.getValue();
								if (endDateInStatDiscount != "") {
									endDateInStatDiscount = dateFormated
											.format('Y-m-d')
											+ " " + endDateInStatDiscount;
								}

								// -- 獲取統計類型 --
								var staticType = discountStatFrom.getForm()
										.findField("discountStat")
										.getGroupValue();
								if (staticType == "detail") {
									isPrompt = true;
									discountStatDetailResultWin.show();

									// ＃＃＃＃＃＃＃＃＃＃＃＃明細統計＃＃＃＃＃＃＃＃＃＃＃＃
									Ext.Ajax
											.request({
												url : "../../DiscountStatDetail.do",
												params : {
													"pin" : pin,
													"beginDate" : begDateInStatDiscount,
													"endDate" : endDateInStatDiscount,
													"staffs" : selectStaffs,
													"StatisticsType" : "Today"
												},
												success : function(response,
														options) {
													var resultJSON = Ext.util.JSON
															.decode(response.responseText);
													// 格式：[帳單號，日期時間，菜品，單價，數量，折扣，口味，口味價錢，廚房id，服務員id，金額]
													var rootData = resultJSON.root;
													if (rootData[0].message == "normal") {
														discountStatDetailResultData.length = 0;
														for ( var i = 0; i < rootData.length; i++) {

															var kitchenName = "";
															for ( var j = 0; j < kitchenMultSelectData.length; j++) {
																if (kitchenMultSelectData[j][2] == rootData[i].kitchenID) {
																	kitchenName = kitchenMultSelectData[j][1];
																}
															}
															if (kitchenName == "") {
																kitchenName = "历史厨房";
															}
															if (rootData[i].kitchenID == "SUM") {
																kitchenName = "";
															}

															// var staffName =
															// "";
															// for ( var j = 0;
															// j <
															// staffData.length;
															// j++) {
															// if
															// (staffData[j][0]
															// ==
															// rootData[i].staffID)
															// {
															// staffName =
															// staffData[j][1];
															// }
															// }
															// if (staffName ==
															// "") {
															// staffName =
															// "历史员工";
															// }

															// 格式：[帳單號，日期時間，菜品，單價，數量，折扣，
															// 口味，口味價錢，廚房id，廚房，服務員id，服務員，金額]
															discountStatDetailResultData
																	.push([
																			rootData[i].orderID,
																			rootData[i].datetime,
																			rootData[i].foodName,
																			rootData[i].singlePrice,
																			rootData[i].count,
																			rootData[i].discount,
																			rootData[i].taste,
																			rootData[i].tastePrice,
																			rootData[i].kitchenID,
																			kitchenName,
																			// rootData[i].staffID,
																			// staffName,
																			rootData[i].staffName, 
																			rootData[i].amount,
																			rootData[i].message ]);
														}

														discountStatDetailResultStore
																.reload();

													} else {
														Ext.MessageBox
																.show({
																	msg : rootData[0].message,
																	width : 300,
																	buttons : Ext.MessageBox.OK
																});
													}
												},
												failure : function(response,
														options) {
													Ext.MessageBox
															.show({
																msg : " Unknown page error ",
																width : 300,
																buttons : Ext.MessageBox.OK
															});
												}
											});

								}
								// else if (staticType == "sumByMaterial") {
								// isPrompt = true;
								// inStatByMaterialResultWin.show();
								//
								// // ＃＃＃＃＃＃＃＃＃＃＃＃按食材統計＃＃＃＃＃＃＃＃＃＃＃＃
								// Ext.Ajax
								// .request({
								// url : "../../InventoryInStatByMaterial.do",
								// params : {
								// "pin" : pin,
								// "beginDate" : beginDate,
								// "endDate" : endDate,
								// "supplier" : supplier,
								// "departments" : departments,
								// "materials" : selectMaterials
								// },
								// success : function(response,
								// options) {
								// var resultJSON = Ext.util.JSON
								// .decode(response.responseText);
								//
								// var rootData = resultJSON.root;
								// if (rootData[0].message == "normal") {
								//
								// inStatByMaterialResultData = rootData
								// .slice(0);
								// inStatByMateriaGrid
								// .getStore()
								// .loadData(
								// inStatByMaterialResultData);
								// if (rootData[0].materialID == "NO_DATA") {
								// inStatByMateriaGrid
								// .getStore()
								// .removeAll();
								// }
								//
								// } else {
								// Ext.MessageBox
								// .show({
								// msg : rootData[0].message,
								// width : 300,
								// buttons : Ext.MessageBox.OK
								// });
								// }
								// },
								// failure : function(response,
								// options) {
								// Ext.MessageBox
								// .show({
								// msg : " Unknown page error ",
								// width : 300,
								// buttons : Ext.MessageBox.OK
								// });
								// }
								// });
								//
								// } else if (staticType == "sumByDept") {
								//
								// isPrompt = true;
								// inStatByDeptResultWin.show();
								//
								// // ＃＃＃＃＃＃＃＃＃＃＃＃按部門統計＃＃＃＃＃＃＃＃＃＃＃＃
								// Ext.Ajax
								// .request({
								// url : "../../InventoryInStatByDept.do",
								// params : {
								// "pin" : pin,
								// "beginDate" : beginDate,
								// "endDate" : endDate,
								// "supplier" : supplier,
								// "departments" : departments,
								// "materials" : selectMaterials
								// },
								// success : function(response,
								// options) {
								// var resultJSON = Ext.util.JSON
								// .decode(response.responseText);
								//
								// var rootData = resultJSON.root;
								// if (rootData[0].message == "normal") {
								//
								// inStatByDeptResultData = rootData
								// .slice(0);
								// inStatByDeptGrid
								// .getStore()
								// .loadData(
								// inStatByDeptResultData);
								// if (rootData[0].materialID == "NO_DATA") {
								// inStatByDeptGrid
								// .getStore()
								// .removeAll();
								// }
								//
								// } else {
								// Ext.MessageBox
								// .show({
								// msg : rootData[0].message,
								// width : 300,
								// buttons : Ext.MessageBox.OK
								// });
								// }
								// },
								// failure : function(response,
								// options) {
								// Ext.MessageBox
								// .show({
								// msg : " Unknown page error ",
								// width : 300,
								// buttons : Ext.MessageBox.OK
								// });
								// }
								// });
								//
								// } else if (staticType == "sumBySupplier") {
								//
								// isPrompt = true;
								// inStatBySupplierResultWin.show();
								//
								// // ＃＃＃＃＃＃＃＃＃＃＃＃按供應商統計＃＃＃＃＃＃＃＃＃＃＃＃
								// Ext.Ajax
								// .request({
								// url : "../../InventoryInStatBySupplier.do",
								// params : {
								// "pin" : pin,
								// "beginDate" : beginDate,
								// "endDate" : endDate,
								// "supplier" : supplier,
								// "departments" : departments,
								// "materials" : selectMaterials
								// },
								// success : function(response,
								// options) {
								// var resultJSON = Ext.util.JSON
								// .decode(response.responseText);
								//
								// var rootData = resultJSON.root;
								// if (rootData[0].message == "normal") {
								//
								// inStatBySupplierResultData = rootData
								// .slice(0);
								// inStatBySupplierGrid
								// .getStore()
								// .loadData(
								// inStatBySupplierResultData);
								// if (rootData[0].materialID == "NO_DATA") {
								// inStatBySupplierGrid
								// .getStore()
								// .removeAll();
								// }
								//
								// } else {
								// Ext.MessageBox
								// .show({
								// msg : rootData[0].message,
								// width : 300,
								// buttons : Ext.MessageBox.OK
								// });
								// }
								// },
								// failure : function(response,
								// options) {
								// Ext.MessageBox
								// .show({
								// msg : " Unknown page error ",
								// width : 300,
								// buttons : Ext.MessageBox.OK
								// });
								// }
								// });
								//
								// }

							} else {
								Ext.MessageBox.show({
									msg : "至少需要选择一个员工进行统计",
									width : 300,
									buttons : Ext.MessageBox.OK
								});
							}
						}
					}, {
						text : "取消",
						handler : function() {
							isPrompt = false;
							discountStatWin.hide();
						}
					} ],
			listeners : {
				"show" : function(thiz) {

					discountStatWin.findById("discountStatMultSelect").reset();
					discountStatWin.findById("begDateInStatDiscount").setValue(
							"");
					discountStatWin.findById("endDateInStatDiscount").setValue(
							"");

					discountStatFrom.getForm().findField("discountStat")
							.setValue("detail");

					discountStatMSDS.loadData(staffData);

				},
				"hide" : function(thiz) {
					isPrompt = false;
				}
			}
		});

// 结果框 -- 明細
// 格式：[帳單號，日期時間，菜品，單價，數量，折扣，口味，口味價錢，廚房id，廚房，服務員id，服務員，金額]
var discountStatDetailResultStore = new Ext.data.Store({
	proxy : new Ext.data.MemoryProxy(discountStatDetailResultData),
	reader : new Ext.data.ArrayReader({}, [ {
		name : "orderID"
	}, {
		name : "datetime"
	}, {
		name : "foodName"
	}, {
		name : "singlePrice"
	}, {
		name : "count"
	}, {
		name : "discount"
	}, {
		name : "taste"
	}, {
		name : "tastePrice"
	}, {
		name : "kitchenID"
	}, {
		name : "kitchenName"
	},
	// {
	// name : "staffID"
	// }, {
	// name : "staffName"
	// },
	{
		name : "staffName"
	}, {
		name : "amount"
	}, {
		name : "message"
	} ])
});

// 2，栏位模型
var discountStatDetailResultColumnModel = new Ext.grid.ColumnModel([
		new Ext.grid.RowNumberer(), {
			header : "账单号",
			sortable : true,
			dataIndex : "orderID",
			width : 80
		}, {
			header : "日期时间",
			sortable : true,
			dataIndex : "datetime",
			width : 80
		}, {
			header : "名称",
			sortable : true,
			dataIndex : "foodName",
			width : 80
		}, {
			header : "单价（￥）",
			sortable : true,
			dataIndex : "singlePrice",
			width : 80
		}, {
			header : "数量",
			sortable : true,
			dataIndex : "count",
			width : 80
		}, {
			header : "折扣",
			sortable : true,
			dataIndex : "discount",
			width : 80
		}, {
			header : "口味",
			sortable : true,
			dataIndex : "taste",
			width : 80
		}, {
			header : "口味价钱（￥）",
			sortable : true,
			dataIndex : "tastePrice",
			width : 80
		}, {
			header : "厨房",
			sortable : true,
			dataIndex : "kitchenName",
			width : 80
		}, {
			header : "服务员",
			sortable : true,
			dataIndex : "staffName",
			width : 80
		}, {
			header : "金额（￥）",
			sortable : true,
			dataIndex : "amount",
			width : 80
		} ]);

var discountStatDetailResultGrid = new Ext.grid.GridPanel({
	xtype : "grid",
	anchor : "99%",
	border : false,
	ds : discountStatDetailResultStore,
	cm : discountStatDetailResultColumnModel,
	sm : new Ext.grid.RowSelectionModel({
		singleSelect : true
	}),
	viewConfig : {
		forceFit : true
	},
	autoScroll : true,
	loadMask : {
		msg : "数据加载中，请稍等..."
	}
});

var discountStatDetailResultWin = new Ext.Window({
	title : "折扣明细",
	width : 800,
	height : 370,
	closeAction : "hide",
	resizable : false,
	layout : "fit",
	items : discountStatDetailResultGrid,
	buttons : [ {
		text : "打印",
		handler : function() {

		}
	}, {
		text : "关闭",
		handler : function() {
			isPrompt = false;
			discountStatDetailResultWin.hide();
		}
	} ],
	listeners : {
		"hide" : function(thiz) {
			isPrompt = false;
		}
	}
});

// // 结果框 -- 按食材
// //
// --------------------------------------------------------------------------------------------------------
// var inStatByMateriaReader = new Ext.data.JsonReader({
// idProperty : 'groupID',
// fields : [ {
// name : 'materialID',
// type : 'int'
// }, {
// name : 'materialName',
// type : 'string'
// }, {
// name : 'groupID',
// type : 'int'
// }, {
// name : 'groupDescr',
// type : 'string'
// }, {
// name : 'singlePrice',
// type : 'float'
// }, {
// name : 'amount',
// type : 'float'
// }, {
// name : 'sumPrice',
// type : 'float'
// }, {
// name : 'deptID'
// }, {
// name : 'deptName',
// type : 'string'
// } ]
//
// });
//
// // define a custom summary function
// Ext.grid.GroupSummary.Calculations['singlePrice'] = function(v, record,
// field) {
// if (record.data.amount != 0) {
// return parseFloat(
// (parseFloat(v).toFixed(2) + (record.data.sumPrice / record.data.amount)
// .toFixed(2))).toFixed(2);
// } else {
// return 0.00;
// }
// };
//
// var inStatByMateriaSummary = new Ext.grid.GroupSummary();
//
// var inStatByMateriaGrid = new Ext.grid.EditorGridPanel({
// ds : new Ext.data.GroupingStore({
// reader : inStatByMateriaReader,
// data : [],
// sortInfo : {
// field : 'deptName',
// direction : "ASC"
// },
// groupField : 'materialName'
// }),
//
// columns : [ {
// id : 'groupDescr',
// header : "食材",
// width : 20,
// sortable : true,
// dataIndex : 'groupDescr',
// // summaryType : 'count',
// hideable : false,
// // summaryRenderer : function(v, params, data) {
// // return ((v === 0 || v > 1) ? '(' + v + ' Tasks)' : '(1 Task)');
// // }
// summaryRenderer : function(v, params, data) {
// return "合计";
// }
// }, {
// header : "NOT SHOW",
// width : 20,
// sortable : false,
// dataIndex : 'materialName'
// }, {
// header : "部门",
// width : 25,
// sortable : true,
// dataIndex : 'deptName'
// }, {
// id : 'singlePrice',
// header : "价格",
// width : 20,
// sortable : false, // 計出來的列排序不起作用
// groupable : false,
// renderer : function(v, params, record) {
// // return Ext.util.Format.usMoney(record.data.sumPrice
// // / record.data.amount);
// if (record.data.amount != 0) {
// return (record.data.sumPrice / record.data.amount).toFixed(2);
// } else {
// return 0.00;
// }
// },
// dataIndex : 'singlePrice',
// summaryType : 'singlePrice'
// // summaryRenderer : Ext.util.Format.usMoney
// }, {
// header : "数量",
// width : 20,
// sortable : true,
// // renderer : Ext.util.Format.usMoney,
// dataIndex : 'amount',
// summaryType : 'sum'
// // editor: new Ext.form.NumberField({
// // allowBlank: false,
// // allowNegative: false,
// // style: 'text-align:left'
// // })
// }, {
// header : "小计",
// width : 20,
// sortable : true,
// // renderer : Ext.util.Format.chnMoney,
// dataIndex : 'sumPrice',
// summaryType : 'sum',
// renderer : function(v) {
// return parseFloat(v).toFixed(2);
// }
// // editor : new Ext.form.NumberField({
// // allowBlank : false,
// // allowNegative : false,
// // style : 'text-align:left'
// // })
// } ],
//
// view : new Ext.grid.GroupingView({
// forceFit : true,
// showGroupName : false,
// enableNoGroups : false, // REQUIRED!
// hideGroupedColumn : true
// }),
//
// plugins : inStatByMateriaSummary,
//
// frame : true,
// width : 800,
// height : 450,
// clicksToEdit : 1,
// collapsible : true,
// animCollapse : false,
// trackMouseOver : false
// // enableColumnMove: false,
// // title : 'Sponsored Projects'
// // iconCls: 'icon-grid',
// // renderTo: document.body
// });
//
// //
// --------------------------------------------------------------------------------------------------------
//
// var inStatByMaterialResultWin = new Ext.Window({
// title : "进货汇总-按食材",
// width : 400,
// height : 500,
// closeAction : "hide",
// resizable : false,
// layout : "fit",
// items : inStatByMateriaGrid,
// buttons : [ {
// text : "打印",
// handler : function() {
//
// }
// }, {
// text : "关闭",
// handler : function() {
// isPrompt = false;
// inStatByMaterialResultWin.hide();
// }
// } ],
// listeners : {
// "hide" : function(thiz) {
// isPrompt = false;
// }
// }
// });
//
// // 结果框 -- 按部門
// //
// --------------------------------------------------------------------------------------------------------
// var inStatByDeptReader = new Ext.data.JsonReader({
// idProperty : 'groupID',
// fields : [ {
// name : 'deptID',
// type : 'int'
// }, {
// name : 'deptName',
// type : 'string'
// }, {
// name : 'groupID',
// type : 'int'
// }, {
// name : 'groupDescr',
// type : 'string'
// }, {
// name : 'singlePrice',
// type : 'float'
// }, {
// name : 'amount',
// type : 'float'
// }, {
// name : 'sumPrice',
// type : 'float'
// }, {
// name : 'materialID'
// }, {
// name : 'materialName',
// type : 'string'
// } ]
//
// });
//
// var inStatByDeptSummary = new Ext.grid.GroupSummary();
//
// var inStatByDeptGrid = new Ext.grid.EditorGridPanel({
// ds : new Ext.data.GroupingStore({
// reader : inStatByDeptReader,
// data : [],
// sortInfo : {
// field : 'materialName',
// direction : "ASC"
// },
// groupField : 'deptName'
// }),
//
// columns : [ {
// id : 'groupDescr',
// header : "部门",
// width : 20,
// sortable : true,
// dataIndex : 'groupDescr',
// // summaryType : 'count',
// hideable : false,
// // summaryRenderer : function(v, params, data) {
// // return ((v === 0 || v > 1) ? '(' + v + ' Tasks)' : '(1 Task)');
// // }
// summaryRenderer : function(v, params, data) {
// return "合计";
// }
// }, {
// header : "NOT SHOW",
// width : 20,
// sortable : false,
// dataIndex : 'deptName'
// }, {
// header : "食材",
// width : 25,
// sortable : true,
// dataIndex : 'materialName'
// }, {
// id : 'singlePrice',
// header : "价格",
// width : 20,
// sortable : false, // 計出來的列排序不起作用
// groupable : false,
// renderer : function(v, params, record) {
// // return Ext.util.Format.usMoney(record.data.sumPrice
// // / record.data.amount);
// if (record.data.amount != 0) {
// return (record.data.sumPrice / record.data.amount).toFixed(2);
// } else {
// return 0.00;
// }
// },
// dataIndex : 'singlePrice',
// summaryType : 'singlePrice'
// // summaryRenderer : Ext.util.Format.usMoney
// }, {
// header : "数量",
// width : 20,
// sortable : true,
// // renderer : Ext.util.Format.usMoney,
// dataIndex : 'amount',
// summaryType : 'sum'
// // editor: new Ext.form.NumberField({
// // allowBlank: false,
// // allowNegative: false,
// // style: 'text-align:left'
// // })
// }, {
// header : "小计",
// width : 20,
// sortable : true,
// // renderer : Ext.util.Format.chnMoney,
// dataIndex : 'sumPrice',
// summaryType : 'sum',
// renderer : function(v) {
// return parseFloat(v).toFixed(2);
// }
// // editor : new Ext.form.NumberField({
// // allowBlank : false,
// // allowNegative : false,
// // style : 'text-align:left'
// // })
// } ],
//
// view : new Ext.grid.GroupingView({
// forceFit : true,
// showGroupName : false,
// enableNoGroups : false, // REQUIRED!
// hideGroupedColumn : true
// }),
//
// plugins : inStatByDeptSummary,
//
// frame : true,
// width : 800,
// height : 450,
// clicksToEdit : 1,
// collapsible : true,
// animCollapse : false,
// trackMouseOver : false
// // enableColumnMove: false,
// // title : 'Sponsored Projects'
// // iconCls: 'icon-grid',
// // renderTo: document.body
// });
//
// //
// --------------------------------------------------------------------------------------------------------
//
// var inStatByDeptResultWin = new Ext.Window({
// title : "进货汇总-按部门",
// width : 400,
// height : 500,
// closeAction : "hide",
// resizable : false,
// layout : "fit",
// items : inStatByDeptGrid,
// buttons : [ {
// text : "打印",
// handler : function() {
//
// }
// }, {
// text : "关闭",
// handler : function() {
// isPrompt = false;
// inStatByDeptResultWin.hide();
// }
// } ],
// listeners : {
// "hide" : function(thiz) {
// isPrompt = false;
// }
// }
// });
//
// // 结果框 -- 按供應商
// //
// --------------------------------------------------------------------------------------------------------
// var inStatBySupplierReader = new Ext.data.JsonReader({
// idProperty : 'groupID',
// fields : [ {
// name : 'supplierID',
// type : 'int'
// }, {
// name : 'supplierName',
// type : 'string'
// }, {
// name : 'groupID',
// type : 'int'
// }, {
// name : 'groupDescr',
// type : 'string'
// }, {
// name : 'singlePrice',
// type : 'float'
// }, {
// name : 'amount',
// type : 'float'
// }, {
// name : 'sumPrice',
// type : 'float'
// }, {
// name : 'materialID'
// }, {
// name : 'materialName',
// type : 'string'
// } ]
//
// });
//
// var inStatBySupplierSummary = new Ext.grid.GroupSummary();
//
// var inStatBySupplierGrid = new Ext.grid.EditorGridPanel({
// ds : new Ext.data.GroupingStore({
// reader : inStatBySupplierReader,
// data : [],
// sortInfo : {
// field : 'materialName',
// direction : "ASC"
// },
// groupField : 'supplierName'
// }),
//
// columns : [ {
// id : 'groupDescr',
// header : "供应商",
// width : 20,
// sortable : true,
// dataIndex : 'groupDescr',
// // summaryType : 'count',
// hideable : false,
// // summaryRenderer : function(v, params, data) {
// // return ((v === 0 || v > 1) ? '(' + v + ' Tasks)' : '(1 Task)');
// // }
// summaryRenderer : function(v, params, data) {
// return "合计";
// }
// }, {
// header : "NOT SHOW",
// width : 20,
// sortable : false,
// dataIndex : 'supplierName'
// }, {
// header : "食材",
// width : 25,
// sortable : true,
// dataIndex : 'materialName'
// }, {
// id : 'singlePrice',
// header : "价格",
// width : 20,
// sortable : false, // 計出來的列排序不起作用
// groupable : false,
// renderer : function(v, params, record) {
// // return Ext.util.Format.usMoney(record.data.sumPrice
// // / record.data.amount);
// if (record.data.amount != 0) {
// return (record.data.sumPrice / record.data.amount).toFixed(2);
// } else {
// return 0.00;
// }
// },
// dataIndex : 'singlePrice',
// summaryType : 'singlePrice'
// // summaryRenderer : Ext.util.Format.usMoney
// }, {
// header : "数量",
// width : 20,
// sortable : true,
// // renderer : Ext.util.Format.usMoney,
// dataIndex : 'amount',
// summaryType : 'sum'
// // editor: new Ext.form.NumberField({
// // allowBlank: false,
// // allowNegative: false,
// // style: 'text-align:left'
// // })
// }, {
// header : "小计",
// width : 20,
// sortable : true,
// // renderer : Ext.util.Format.chnMoney,
// dataIndex : 'sumPrice',
// summaryType : 'sum',
// renderer : function(v) {
// return parseFloat(v).toFixed(2);
// }
// // editor : new Ext.form.NumberField({
// // allowBlank : false,
// // allowNegative : false,
// // style : 'text-align:left'
// // })
// } ],
//
// view : new Ext.grid.GroupingView({
// forceFit : true,
// showGroupName : false,
// enableNoGroups : false, // REQUIRED!
// hideGroupedColumn : true
// }),
//
// plugins : inStatBySupplierSummary,
//
// frame : true,
// width : 800,
// height : 450,
// clicksToEdit : 1,
// collapsible : true,
// animCollapse : false,
// trackMouseOver : false
// // enableColumnMove: false,
// // title : 'Sponsored Projects'
// // iconCls: 'icon-grid',
// // renderTo: document.body
// });
//
// //
// --------------------------------------------------------------------------------------------------------
//
// var inStatBySupplierResultWin = new Ext.Window({
// title : "进货汇总-按供应商",
// width : 400,
// height : 500,
// closeAction : "hide",
// resizable : false,
// layout : "fit",
// items : inStatBySupplierGrid,
// buttons : [ {
// text : "打印",
// handler : function() {
//
// }
// }, {
// text : "关闭",
// handler : function() {
// isPrompt = false;
// inStatBySupplierResultWin.hide();
// }
// } ],
// listeners : {
// "hide" : function(thiz) {
// isPrompt = false;
// }
// }
// });
//
// //
// --------------------------------------end入庫統計----------------------------------------------
