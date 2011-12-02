//--------------------------------------- 入庫統計 --------------------------------------------------------
// 条件框
var inventoryInStatMSDS = new Ext.data.SimpleStore({
	fields : [ "retrunValue", "displayText" ],
	data : []
});

var inStatSupplierComboStore = new Ext.data.SimpleStore({
	fields : [ "value", "text" ],
	data : []
});

var inStatSupplierCombo = new Ext.form.ComboBox({
	fieldLabel : "供应商",
	forceSelection : true,
	width : 120,
	value : "全部",
	id : "inStatSupplierCombo",
	store : inStatSupplierComboStore,
	valueField : "value",
	displayField : "text",
	typeAhead : true,
	mode : "local",
	triggerAction : "all",
	selectOnFocus : true,
	allowBlank : false
});

var inventoryInStatFrom = new Ext.form.FormPanel({
	border : false,
	anchor : "right 8%",
	id : "InStatForm",
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
				name : 'InStat',
				inputValue : 'detail'
			} ]
		}, {
			layout : "form",
			border : false,
			labelSeparator : '',
			width : 110,
			items : [ {
				xtype : 'radio',
				hideLabel : true,
				boxLabel : "按食材汇总",
				// checked : true,
				name : 'InStat',
				inputValue : 'sumByMaterial'
			} ]
		}, {
			layout : "form",
			border : false,
			labelSeparator : '',
			width : 110,
			items : [ {
				xtype : 'radio',
				hideLabel : true,
				boxLabel : "按部门汇总",
				// checked : true,
				name : 'InStat',
				inputValue : 'sumByDept'
			} ]
		}, {
			layout : "form",
			border : false,
			labelSeparator : '',
			width : 110,
			items : [ {
				xtype : 'radio',
				hideLabel : true,
				boxLabel : "按供应商汇总",
				// checked : true,
				name : 'InStat',
				inputValue : 'sumBySupplier'
			} ]
		} ]
	} ]
});

inventoryInStatWin = new Ext.Window(
		{
			title : "入库统计",
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
							xtype : "datefield",
							id : "begDateInStat",
							width : 120,
							fieldLabel : "日期"
						} ]
					}, {
						layout : "form",
						border : false,
						labelSeparator : ' ',
						width : 200,
						labelWidth : 50,
						items : [ {
							xtype : "datefield",
							id : "endDateInStat",
							width : 120,
							fieldLabel : "至"
						} ]
					} ]
				} ]
			}, {
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
						labelSeparator : '：',
						width : 300,
						labelWidth : 50,
						items : inStatSupplierCombo
					} ]
				} ]
			}, {
				layout : "column",
				border : false,
				anchor : "99% 15%",
				autoScroll : true,
				frame : true,
				items : [ {
					layout : "form",
					border : false,
					labelSeparator : '',
					width : 80,
					labelWidth : 40,
					items : [ {
						xtype : "checkbox",
						id : "dept1InStat"
					// fieldLabel : departmentData[0][1]
					} ]
				}, {
					layout : "form",
					border : false,
					labelSeparator : '',
					width : 80,
					labelWidth : 40,
					items : [ {
						xtype : "checkbox",
						id : "dept2InStat"
					// fieldLabel : departmentData[1][1]
					} ]
				}, {
					layout : "form",
					border : false,
					labelSeparator : '',
					width : 80,
					labelWidth : 40,
					items : [ {
						xtype : "checkbox",
						id : "dept3InStat"
					// fieldLabel : departmentData[2][1]
					} ]
				}, {
					layout : "form",
					border : false,
					labelSeparator : '',
					width : 80,
					labelWidth : 40,
					items : [ {
						xtype : "checkbox",
						id : "dept4InStat"
					// fieldLabel : departmentData[3][1]
					} ]
				}, {
					layout : "form",
					border : false,
					labelSeparator : '',
					width : 80,
					labelWidth : 40,
					items : [ {
						xtype : "checkbox",
						id : "dept5InStat"
					// fieldLabel : departmentData[4][1]
					} ]
				}, {
					layout : "form",
					border : false,
					labelSeparator : '',
					width : 80,
					labelWidth : 40,
					items : [ {
						xtype : "checkbox",
						id : "dept6InStat"
					// fieldLabel : departmentData[5][1]
					} ]
				}, {
					layout : "form",
					border : false,
					labelSeparator : '',
					width : 80,
					labelWidth : 40,
					items : [ {
						xtype : "checkbox",
						id : "dept7InStat"
					// fieldLabel : departmentData[6][1]
					} ]
				}, {
					layout : "form",
					border : false,
					labelSeparator : '',
					width : 80,
					labelWidth : 40,
					items : [ {
						xtype : "checkbox",
						id : "dept8InStat"
					// fieldLabel : departmentData[7][1]
					} ]
				}, {
					layout : "form",
					border : false,
					labelSeparator : '',
					width : 80,
					labelWidth : 40,
					items : [ {
						xtype : "checkbox",
						id : "dept9InStat"
					// fieldLabel : departmentData[8][1]
					} ]
				}, {
					layout : "form",
					border : false,
					labelSeparator : '',
					width : 80,
					labelWidth : 40,
					items : [ {
						xtype : "checkbox",
						id : "dept10InStat"
					// fieldLabel : departmentData[9][1]
					} ]
				} ]
			}, {
				layout : "form",
				border : false,
				frame : true,
				hideLabels : true,
				anchor : "right 56%",
				items : [ {
					xtype : "itemselector",
					name : "materialInStatMultSelect",
					id : "materialInStatMultSelect",
					fromStore : inventoryInStatMSDS,
					dataFields : [ "retrunValue", "displayText" ],
					toData : [ [ "", "" ] ],
					msHeight : 173,
					valueField : "retrunValue",
					displayField : "displayText",
					imagePath : "../../extjs/multiselect/images/",
					toLegend : "已选择食材",
					fromLegend : "可选择食材"
				} ]
			}, inventoryInStatFrom ],
			buttons : [
					{
						text : "全选",
						handler : function() {
							var i = inventoryInStatWin
									.findById("materialInStatMultSelect").fromMultiselect.view;
							i.selectRange(0, i.store.length);
							inventoryInStatWin.findById(
									"materialInStatMultSelect").fromTo();
							inventoryInStatWin
									.findById("materialInStatMultSelect").toMultiselect.view
									.clearSelections();
						}
					},
					{
						text : "清空",
						handler : function() {
							inventoryInStatWin.findById(
									"materialInStatMultSelect").reset();
						}
					},
					{
						text : "确定",
						handler : function() {

							var selectCount = inventoryInStatWin
									.findById("materialInStatMultSelect").toMultiselect.store
									.getCount();

							if (selectCount != 0) {
								isPrompt = false;
								inventoryInStatWin.hide();

								// -- 獲取選擇的食材 --
								var selectMaterials = "";
								for ( var i = 0; i < selectCount; i++) {
									var selectItem = inventoryInStatWin
											.findById("materialInStatMultSelect").toMultiselect.store
											.getAt(i).get("retrunValue");
									// if (selectItem != "") {
									selectMaterials = selectMaterials
											+ selectItem + ",";
									// }
								}
								// 去掉最后一个逗号
								selectMaterials = selectMaterials.substring(0,
										selectMaterials.length - 1);
								if (selectMaterials.substring(0, 1) == ",") {
									selectMaterials = selectMaterials
											.substring(1,
													selectMaterials.length);
								}

								// -- 獲取時間 --
								var beginDate = inventoryInStatWin.findById(
										"begDateInStat").getValue();
								if (beginDate != "") {
									var dateFormated = new Date();
									dateFormated = beginDate;
									beginDate = dateFormated.format('Y-m-d');
								}

								var endDate = inventoryInStatWin.findById(
										"endDateInStat").getValue();
								if (endDate != "") {
									var dateFormated = new Date();
									dateFormated = endDate;
									endDate = dateFormated.format('Y-m-d');
								}

								// -- 獲取供應商 --
								var supplier = inStatSupplierCombo.getValue();
								if (supplier == "全部") {
									supplier = "-1";
								}

								// -- 獲取部門 --
								var departments = "";
								if (inventoryInStatWin.findById("dept1InStat")
										.getValue() == true) {
									departments = departments + "0,";
								}
								if (inventoryInStatWin.findById("dept2InStat")
										.getValue() == true) {
									departments = departments + "1,";
								}
								if (inventoryInStatWin.findById("dept3InStat")
										.getValue() == true) {
									departments = departments + "2,";
								}
								if (inventoryInStatWin.findById("dept4InStat")
										.getValue() == true) {
									departments = departments + "3,";
								}
								if (inventoryInStatWin.findById("dept5InStat")
										.getValue() == true) {
									departments = departments + "4,";
								}
								if (inventoryInStatWin.findById("dept6InStat")
										.getValue() == true) {
									departments = departments + "5,";
								}
								if (inventoryInStatWin.findById("dept7InStat")
										.getValue() == true) {
									departments = departments + "6,";
								}
								if (inventoryInStatWin.findById("dept8InStat")
										.getValue() == true) {
									departments = departments + "7,";
								}
								if (inventoryInStatWin.findById("dept9InStat")
										.getValue() == true) {
									departments = departments + "8,";
								}
								if (inventoryInStatWin.findById("dept10InStat")
										.getValue() == true) {
									departments = departments + "9,";
								}

								if (departments != "") {
									departments = departments.substring(0,
											departments.length - 1);
								}

								// -- 獲取統計類型 --
								var staticType = inventoryInStatFrom.getForm()
										.findField("InStat").getGroupValue();
								if (staticType == "detail") {
									isPrompt = true;
									inStatDetailResultWin.show();

									// ＃＃＃＃＃＃＃＃＃＃＃＃明細統計＃＃＃＃＃＃＃＃＃＃＃＃
									Ext.Ajax
											.request({
												url : "../../InventoryInStatDetail.do",
												params : {
													"pin" : pin,
													"beginDate" : beginDate,
													"endDate" : endDate,
													"supplier" : supplier,
													"departments" : departments,
													"materials" : selectMaterials
												},
												success : function(response,
														options) {
													var resultJSON = Ext.util.JSON
															.decode(response.responseText);
													// 格式：[食材id，日期，供應商id，經辦人，部門id，價格，數量，小計]
													var rootData = resultJSON.root;
													if (rootData[0].message == "normal") {
														inStatDetailResultData.length = 0;
														for ( var i = 0; i < rootData.length; i++) {
															var materialN = "";
															for ( var j = 0; j < materialData.length; j++) {
																if (materialData[j][0] == rootData[i].materialID) {
																	materialN = materialData[j][2];
																}
															}
															var supplierN = "";
															for ( var j = 0; j < supplierData.length; j++) {
																if (supplierData[j][0] == rootData[i].supplierID) {
																	supplierN = supplierData[j][2];
																}
															}
															var deptN = "";
															for ( var j = 0; j < departmentData.length; j++) {
																if (departmentData[j][0] == rootData[i].departmentID) {
																	deptN = departmentData[j][1];
																}
															}

															inStatDetailResultData
																	.push([
																			rootData[i].materialID,
																			materialN,
																			rootData[i].date,
																			rootData[i].supplierID,
																			supplierN,
																			rootData[i].operator,
																			rootData[i].departmentID,
																			deptN,
																			rootData[i].price,
																			rootData[i].amount,
																			rootData[i].total

																	]);
														}

														inStatDetailResultStore
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

								} else if (staticType == "sumByMaterial") {
									isPrompt = true;
									inStatByMaterialResultWin.show();

									// ＃＃＃＃＃＃＃＃＃＃＃＃按食材統計＃＃＃＃＃＃＃＃＃＃＃＃
									Ext.Ajax
											.request({
												url : "../../InventoryInStatByMaterial.do",
												params : {
													"pin" : pin,
													"beginDate" : beginDate,
													"endDate" : endDate,
													"supplier" : supplier,
													"departments" : departments,
													"materials" : selectMaterials
												},
												success : function(response,
														options) {
													var resultJSON = Ext.util.JSON
															.decode(response.responseText);

													var rootData = resultJSON.root;
													if (rootData[0].message == "normal") {

														inStatByMaterialResultData = rootData;
														inStatByMateriaGrid
																.getStore()
																.loadData(
																		inStatByMaterialResultData);
														if (rootData[0].materialID == "NO_DATA") {
															inStatByMateriaGrid
																	.getStore()
																	.removeAll();
														}

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

								} else if (staticType == "sumByDept") {

									isPrompt = true;
									inStatByDeptResultWin.show();

									// ＃＃＃＃＃＃＃＃＃＃＃＃按部門統計＃＃＃＃＃＃＃＃＃＃＃＃
									Ext.Ajax
											.request({
												url : "../../InventoryInStatByDept.do",
												params : {
													"pin" : pin,
													"beginDate" : beginDate,
													"endDate" : endDate,
													"supplier" : supplier,
													"departments" : departments,
													"materials" : selectMaterials
												},
												success : function(response,
														options) {
													var resultJSON = Ext.util.JSON
															.decode(response.responseText);

													var rootData = resultJSON.root;
													if (rootData[0].message == "normal") {

														inStatByDeptResultData = rootData;
														inStatByDeptGrid
																.getStore()
																.loadData(
																		inStatByDeptResultData);
														if (rootData[0].materialID == "NO_DATA") {
															inStatByDeptGrid
																	.getStore()
																	.removeAll();
														}

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

								} else if (staticType == "sumBySupplier") {

									isPrompt = true;
									inStatBySupplierResultWin.show();

									// ＃＃＃＃＃＃＃＃＃＃＃＃按供應商統計＃＃＃＃＃＃＃＃＃＃＃＃
									Ext.Ajax
											.request({
												url : "../../InventoryInStatBySupplier.do",
												params : {
													"pin" : pin,
													"beginDate" : beginDate,
													"endDate" : endDate,
													"supplier" : supplier,
													"departments" : departments,
													"materials" : selectMaterials
												},
												success : function(response,
														options) {
													var resultJSON = Ext.util.JSON
															.decode(response.responseText);

													var rootData = resultJSON.root;
													if (rootData[0].message == "normal") {

														inStatBySupplierResultData = rootData;
														inStatBySupplierGrid
																.getStore()
																.loadData(
																		inStatBySupplierResultData);
														if (rootData[0].materialID == "NO_DATA") {
															inStatBySupplierGrid
																	.getStore()
																	.removeAll();
														}

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

							} else {
								Ext.MessageBox.show({
									msg : "至少需要选择一个食材进行统计",
									width : 300,
									buttons : Ext.MessageBox.OK
								});
							}
						}
					}, {
						text : "取消",
						handler : function() {
							isPrompt = false;
							inventoryInStatWin.hide();
						}
					} ],
			listeners : {
				"show" : function(thiz) {

					inventoryInStatWin.findById("materialInStatMultSelect")
							.reset();
					inventoryInStatWin.findById("begDateInStat").setValue("");
					inventoryInStatWin.findById("endDateInStat").setValue("");

					inStatSupplierCombo.setValue("全部");

					// inventoryInStatMSDS.loadData(materialComboData);
					inStatSupplierComboStore.loadData(supplierComboData);

					// 神技！動態改變form中component的label！！！
					inventoryInStatWin.findById("dept1InStat").el.parent()
							.parent().parent().first().dom.innerHTML = departmentData[0][1]
							+ ":";
					inventoryInStatWin.findById("dept2InStat").el.parent()
							.parent().parent().first().dom.innerHTML = departmentData[1][1]
							+ ":";
					inventoryInStatWin.findById("dept3InStat").el.parent()
							.parent().parent().first().dom.innerHTML = departmentData[2][1]
							+ ":";
					inventoryInStatWin.findById("dept4InStat").el.parent()
							.parent().parent().first().dom.innerHTML = departmentData[3][1]
							+ ":";
					inventoryInStatWin.findById("dept5InStat").el.parent()
							.parent().parent().first().dom.innerHTML = departmentData[4][1]
							+ ":";
					inventoryInStatWin.findById("dept6InStat").el.parent()
							.parent().parent().first().dom.innerHTML = departmentData[5][1]
							+ ":";
					inventoryInStatWin.findById("dept7InStat").el.parent()
							.parent().parent().first().dom.innerHTML = departmentData[6][1]
							+ ":";
					inventoryInStatWin.findById("dept8InStat").el.parent()
							.parent().parent().first().dom.innerHTML = departmentData[7][1]
							+ ":";
					inventoryInStatWin.findById("dept9InStat").el.parent()
							.parent().parent().first().dom.innerHTML = departmentData[8][1]
							+ ":";
					inventoryInStatWin.findById("dept10InStat").el.parent()
							.parent().parent().first().dom.innerHTML = departmentData[9][1]
							+ ":";

					inventoryInStatWin.findById("dept1InStat").setValue(false);
					inventoryInStatWin.findById("dept2InStat").setValue(false);
					inventoryInStatWin.findById("dept3InStat").setValue(false);
					inventoryInStatWin.findById("dept4InStat").setValue(false);
					inventoryInStatWin.findById("dept5InStat").setValue(false);
					inventoryInStatWin.findById("dept6InStat").setValue(false);
					inventoryInStatWin.findById("dept7InStat").setValue(false);
					inventoryInStatWin.findById("dept8InStat").setValue(false);
					inventoryInStatWin.findById("dept9InStat").setValue(false);
					inventoryInStatWin.findById("dept10InStat").setValue(false);

					inventoryInStatFrom.getForm().findField("InStat").setValue(
							"detail");

				},
				"hide" : function(thiz) {
					isPrompt = false;
				}
			}
		});

// 结果框 -- 明細
// 前台：[食材 日期 供应商 经手人 部门 价格 数量 小计]
var inStatDetailResultStore = new Ext.data.Store({
	proxy : new Ext.data.MemoryProxy(inStatDetailResultData),
	reader : new Ext.data.ArrayReader({}, [ {
		name : "materialID"
	}, {
		name : "materialName"
	}, {
		name : "date"
	}, {
		name : "supplierID"
	}, {
		name : "supplierName"
	}, {
		name : "operator"
	}, {
		name : "departmentID"
	}, {
		name : "departmentName"
	}, {
		name : "price"
	}, {
		name : "amount"
	}, {
		name : "total"
	}, {
		name : "message"
	} ])
});

// 2，栏位模型
var inStatDetailResultColumnModel = new Ext.grid.ColumnModel([
		new Ext.grid.RowNumberer(), {
			header : "食材",
			sortable : true,
			dataIndex : "materialName",
			width : 80
		}, {
			header : "日期",
			sortable : true,
			dataIndex : "date",
			width : 80
		}, {
			header : "供应商",
			sortable : true,
			dataIndex : "supplierName",
			width : 80
		}, {
			header : "经手人",
			sortable : true,
			dataIndex : "operator",
			width : 80
		}, {
			header : "部门",
			sortable : true,
			dataIndex : "departmentName",
			width : 80
		}, {
			header : "价钱",
			sortable : true,
			dataIndex : "price",
			width : 80
		}, {
			header : "数量",
			sortable : true,
			dataIndex : "amount",
			width : 80
		}, {
			header : "小计（￥）",
			sortable : true,
			dataIndex : "total",
			width : 80
		} ]);

var inStatDetailResultGrid = new Ext.grid.GridPanel({
	xtype : "grid",
	anchor : "99%",
	border : false,
	ds : inStatDetailResultStore,
	cm : inStatDetailResultColumnModel,
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

var inStatDetailResultWin = new Ext.Window({
	title : "入库明细",
	width : 800,
	height : 370,
	closeAction : "hide",
	resizable : false,
	layout : "fit",
	items : inStatDetailResultGrid,
	buttons : [ {
		text : "打印",
		handler : function() {

		}
	}, {
		text : "关闭",
		handler : function() {
			isPrompt = false;
			inStatDetailResultWin.hide();
		}
	} ],
	listeners : {
		"hide" : function(thiz) {
			isPrompt = false;
		}
	}
});

// 结果框 -- 按食材
// --------------------------------------------------------------------------------------------------------
var inStatByMateriaReader = new Ext.data.JsonReader({
	idProperty : 'groupID',
	fields : [ {
		name : 'materialID',
		type : 'int'
	}, {
		name : 'materialName',
		type : 'string'
	}, {
		name : 'groupID',
		type : 'int'
	}, {
		name : 'groupDescr',
		type : 'string'
	}, {
		name : 'singlePrice',
		type : 'float'
	}, {
		name : 'amount',
		type : 'float'
	}, {
		name : 'sumPrice',
		type : 'float'
	}, {
		name : 'deptID'
	}, {
		name : 'deptName',
		type : 'string'
	} ]

});

// define a custom summary function
Ext.grid.GroupSummary.Calculations['singlePrice'] = function(v, record, field) {
	if (record.data.amount != 0) {
		return parseFloat(
				(parseFloat(v).toFixed(2) + (record.data.sumPrice / record.data.amount)
						.toFixed(2))).toFixed(2);
	} else {
		return 0.00;
	}
};

var inStatByMateriaSummary = new Ext.grid.GroupSummary();

var inStatByMateriaGrid = new Ext.grid.EditorGridPanel({
	ds : new Ext.data.GroupingStore({
		reader : inStatByMateriaReader,
		data : [],
		sortInfo : {
			field : 'deptName',
			direction : "ASC"
		},
		groupField : 'materialName'
	}),

	columns : [ {
		id : 'groupDescr',
		header : "食材",
		width : 20,
		sortable : true,
		dataIndex : 'groupDescr',
		// summaryType : 'count',
		hideable : false,
		// summaryRenderer : function(v, params, data) {
		// return ((v === 0 || v > 1) ? '(' + v + ' Tasks)' : '(1 Task)');
		// }
		summaryRenderer : function(v, params, data) {
			return "合计";
		}
	}, {
		header : "NOT SHOW",
		width : 20,
		sortable : false,
		dataIndex : 'materialName'
	}, {
		header : "部门",
		width : 25,
		sortable : true,
		dataIndex : 'deptName'
	}, {
		id : 'singlePrice',
		header : "价格",
		width : 20,
		sortable : false, // 計出來的列排序不起作用
		groupable : false,
		renderer : function(v, params, record) {
			// return Ext.util.Format.usMoney(record.data.sumPrice
			// / record.data.amount);
			if (record.data.amount != 0) {
				return (record.data.sumPrice / record.data.amount).toFixed(2);
			} else {
				return 0.00;
			}
		},
		dataIndex : 'singlePrice',
		summaryType : 'singlePrice'
	// summaryRenderer : Ext.util.Format.usMoney
	}, {
		header : "数量",
		width : 20,
		sortable : true,
		// renderer : Ext.util.Format.usMoney,
		dataIndex : 'amount',
		summaryType : 'sum'
	// editor: new Ext.form.NumberField({
	// allowBlank: false,
	// allowNegative: false,
	// style: 'text-align:left'
	// })
	}, {
		header : "小计",
		width : 20,
		sortable : true,
		// renderer : Ext.util.Format.chnMoney,
		dataIndex : 'sumPrice',
		summaryType : 'sum',
		renderer : function(v) {
			return parseFloat(v).toFixed(2);
		}
	// editor : new Ext.form.NumberField({
	// allowBlank : false,
	// allowNegative : false,
	// style : 'text-align:left'
	// })
	} ],

	view : new Ext.grid.GroupingView({
		forceFit : true,
		showGroupName : false,
		enableNoGroups : false, // REQUIRED!
		hideGroupedColumn : true
	}),

	plugins : inStatByMateriaSummary,

	frame : true,
	width : 800,
	height : 450,
	clicksToEdit : 1,
	collapsible : true,
	animCollapse : false,
	trackMouseOver : false
// enableColumnMove: false,
// title : 'Sponsored Projects'
// iconCls: 'icon-grid',
// renderTo: document.body
});

// --------------------------------------------------------------------------------------------------------

var inStatByMaterialResultWin = new Ext.Window({
	title : "入库汇总-按食材",
	width : 400,
	height : 500,
	closeAction : "hide",
	resizable : false,
	layout : "fit",
	items : inStatByMateriaGrid,
	buttons : [ {
		text : "打印",
		handler : function() {

		}
	}, {
		text : "关闭",
		handler : function() {
			isPrompt = false;
			inStatByMaterialResultWin.hide();
		}
	} ],
	listeners : {
		"hide" : function(thiz) {
			isPrompt = false;
		}
	}
});

// 结果框 -- 按部門
// --------------------------------------------------------------------------------------------------------
var inStatByDeptReader = new Ext.data.JsonReader({
	idProperty : 'groupID',
	fields : [ {
		name : 'deptID',
		type : 'int'
	}, {
		name : 'deptName',
		type : 'string'
	}, {
		name : 'groupID',
		type : 'int'
	}, {
		name : 'groupDescr',
		type : 'string'
	}, {
		name : 'singlePrice',
		type : 'float'
	}, {
		name : 'amount',
		type : 'float'
	}, {
		name : 'sumPrice',
		type : 'float'
	}, {
		name : 'materialID'
	}, {
		name : 'materialName',
		type : 'string'
	} ]

});

var inStatByDeptSummary = new Ext.grid.GroupSummary();

var inStatByDeptGrid = new Ext.grid.EditorGridPanel({
	ds : new Ext.data.GroupingStore({
		reader : inStatByDeptReader,
		data : [],
		sortInfo : {
			field : 'materialName',
			direction : "ASC"
		},
		groupField : 'deptName'
	}),

	columns : [ {
		id : 'groupDescr',
		header : "部门",
		width : 20,
		sortable : true,
		dataIndex : 'groupDescr',
		// summaryType : 'count',
		hideable : false,
		// summaryRenderer : function(v, params, data) {
		// return ((v === 0 || v > 1) ? '(' + v + ' Tasks)' : '(1 Task)');
		// }
		summaryRenderer : function(v, params, data) {
			return "合计";
		}
	}, {
		header : "NOT SHOW",
		width : 20,
		sortable : false,
		dataIndex : 'deptName'
	}, {
		header : "食材",
		width : 25,
		sortable : true,
		dataIndex : 'materialName'
	}, {
		id : 'singlePrice',
		header : "价格",
		width : 20,
		sortable : false, // 計出來的列排序不起作用
		groupable : false,
		renderer : function(v, params, record) {
			// return Ext.util.Format.usMoney(record.data.sumPrice
			// / record.data.amount);
			if (record.data.amount != 0) {
				return (record.data.sumPrice / record.data.amount).toFixed(2);
			} else {
				return 0.00;
			}
		},
		dataIndex : 'singlePrice',
		summaryType : 'singlePrice'
	// summaryRenderer : Ext.util.Format.usMoney
	}, {
		header : "数量",
		width : 20,
		sortable : true,
		// renderer : Ext.util.Format.usMoney,
		dataIndex : 'amount',
		summaryType : 'sum'
	// editor: new Ext.form.NumberField({
	// allowBlank: false,
	// allowNegative: false,
	// style: 'text-align:left'
	// })
	}, {
		header : "小计",
		width : 20,
		sortable : true,
		// renderer : Ext.util.Format.chnMoney,
		dataIndex : 'sumPrice',
		summaryType : 'sum',
		renderer : function(v) {
			return parseFloat(v).toFixed(2);
		}
	// editor : new Ext.form.NumberField({
	// allowBlank : false,
	// allowNegative : false,
	// style : 'text-align:left'
	// })
	} ],

	view : new Ext.grid.GroupingView({
		forceFit : true,
		showGroupName : false,
		enableNoGroups : false, // REQUIRED!
		hideGroupedColumn : true
	}),

	plugins : inStatByDeptSummary,

	frame : true,
	width : 800,
	height : 450,
	clicksToEdit : 1,
	collapsible : true,
	animCollapse : false,
	trackMouseOver : false
// enableColumnMove: false,
// title : 'Sponsored Projects'
// iconCls: 'icon-grid',
// renderTo: document.body
});

// --------------------------------------------------------------------------------------------------------

var inStatByDeptResultWin = new Ext.Window({
	title : "入库汇总-按部门",
	width : 400,
	height : 500,
	closeAction : "hide",
	resizable : false,
	layout : "fit",
	items : inStatByDeptGrid,
	buttons : [ {
		text : "打印",
		handler : function() {

		}
	}, {
		text : "关闭",
		handler : function() {
			isPrompt = false;
			inStatByDeptResultWin.hide();
		}
	} ],
	listeners : {
		"hide" : function(thiz) {
			isPrompt = false;
		}
	}
});

// 结果框 -- 按供應商
// --------------------------------------------------------------------------------------------------------
var inStatBySupplierReader = new Ext.data.JsonReader({
	idProperty : 'groupID',
	fields : [ {
		name : 'supplierID',
		type : 'int'
	}, {
		name : 'supplierName',
		type : 'string'
	}, {
		name : 'groupID',
		type : 'int'
	}, {
		name : 'groupDescr',
		type : 'string'
	}, {
		name : 'singlePrice',
		type : 'float'
	}, {
		name : 'amount',
		type : 'float'
	}, {
		name : 'sumPrice',
		type : 'float'
	}, {
		name : 'materialID'
	}, {
		name : 'materialName',
		type : 'string'
	} ]

});

var inStatBySupplierSummary = new Ext.grid.GroupSummary();

var inStatBySupplierGrid = new Ext.grid.EditorGridPanel({
	ds : new Ext.data.GroupingStore({
		reader : inStatBySupplierReader,
		data : [],
		sortInfo : {
			field : 'materialName',
			direction : "ASC"
		},
		groupField : 'supplierName'
	}),

	columns : [ {
		id : 'groupDescr',
		header : "供应商",
		width : 20,
		sortable : true,
		dataIndex : 'groupDescr',
		// summaryType : 'count',
		hideable : false,
		// summaryRenderer : function(v, params, data) {
		// return ((v === 0 || v > 1) ? '(' + v + ' Tasks)' : '(1 Task)');
		// }
		summaryRenderer : function(v, params, data) {
			return "合计";
		}
	}, {
		header : "NOT SHOW",
		width : 20,
		sortable : false,
		dataIndex : 'supplierName'
	}, {
		header : "食材",
		width : 25,
		sortable : true,
		dataIndex : 'materialName'
	}, {
		id : 'singlePrice',
		header : "价格",
		width : 20,
		sortable : false, // 計出來的列排序不起作用
		groupable : false,
		renderer : function(v, params, record) {
			// return Ext.util.Format.usMoney(record.data.sumPrice
			// / record.data.amount);
			if (record.data.amount != 0) {
				return (record.data.sumPrice / record.data.amount).toFixed(2);
			} else {
				return 0.00;
			}
		},
		dataIndex : 'singlePrice',
		summaryType : 'singlePrice'
	// summaryRenderer : Ext.util.Format.usMoney
	}, {
		header : "数量",
		width : 20,
		sortable : true,
		// renderer : Ext.util.Format.usMoney,
		dataIndex : 'amount',
		summaryType : 'sum'
	// editor: new Ext.form.NumberField({
	// allowBlank: false,
	// allowNegative: false,
	// style: 'text-align:left'
	// })
	}, {
		header : "小计",
		width : 20,
		sortable : true,
		// renderer : Ext.util.Format.chnMoney,
		dataIndex : 'sumPrice',
		summaryType : 'sum',
		renderer : function(v) {
			return parseFloat(v).toFixed(2);
		}
	// editor : new Ext.form.NumberField({
	// allowBlank : false,
	// allowNegative : false,
	// style : 'text-align:left'
	// })
	} ],

	view : new Ext.grid.GroupingView({
		forceFit : true,
		showGroupName : false,
		enableNoGroups : false, // REQUIRED!
		hideGroupedColumn : true
	}),

	plugins : inStatBySupplierSummary,

	frame : true,
	width : 800,
	height : 450,
	clicksToEdit : 1,
	collapsible : true,
	animCollapse : false,
	trackMouseOver : false
// enableColumnMove: false,
// title : 'Sponsored Projects'
// iconCls: 'icon-grid',
// renderTo: document.body
});

// --------------------------------------------------------------------------------------------------------

var inStatBySupplierResultWin = new Ext.Window({
	title : "入库汇总-按供应商",
	width : 400,
	height : 500,
	closeAction : "hide",
	resizable : false,
	layout : "fit",
	items : inStatBySupplierGrid,
	buttons : [ {
		text : "打印",
		handler : function() {

		}
	}, {
		text : "关闭",
		handler : function() {
			isPrompt = false;
			inStatBySupplierResultWin.hide();
		}
	} ],
	listeners : {
		"hide" : function(thiz) {
			isPrompt = false;
		}
	}
});

// --------------------------------------end入庫統計----------------------------------------------

// ---------------------------------------出庫統計--------------------------------------------------------
// 条件框
var inventoryOutStatMSDS = new Ext.data.SimpleStore({
	fields : [ "retrunValue", "displayText" ],
	data : []
});

var inventoryOutStatFrom = new Ext.form.FormPanel({
	border : false,
	anchor : "right 8%",
	id : "OutStatForm",
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
				name : 'OutStat',
				inputValue : 'detail'
			} ]
		}, {
			layout : "form",
			border : false,
			labelSeparator : '',
			width : 110,
			items : [ {
				xtype : 'radio',
				hideLabel : true,
				boxLabel : "按食材汇总",
				// checked : true,
				name : 'OutStat',
				inputValue : 'sumByMaterial'
			} ]
		}, {
			layout : "form",
			border : false,
			labelSeparator : '',
			width : 110,
			items : [ {
				xtype : 'radio',
				hideLabel : true,
				boxLabel : "按部门汇总",
				// checked : true,
				name : 'OutStat',
				inputValue : 'sumByDept'
			} ]
		}, {
			layout : "form",
			border : false,
			labelSeparator : '',
			width : 110,
			items : [ {
				xtype : 'radio',
				hideLabel : true,
				boxLabel : "按原因汇总",
				// checked : true,
				name : 'OutStat',
				inputValue : 'sumByReason'
			} ]
		} ]
	} ]
});

inventoryOutStatWin = new Ext.Window(
		{
			title : "出库统计",
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
							xtype : "datefield",
							id : "begDateOutStat",
							width : 120,
							fieldLabel : "日期"
						} ]
					}, {
						layout : "form",
						border : false,
						labelSeparator : ' ',
						width : 200,
						labelWidth : 50,
						items : [ {
							xtype : "datefield",
							id : "endDateOutStat",
							width : 120,
							fieldLabel : "至"
						} ]
					} ]
				} ]
			}, {
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
						labelSeparator : '：',
						width : 80,
						labelWidth : 40,
						items : [ {
							xtype : "checkbox",
							id : "destroyOutStat",
							fieldLabel : "报损"
						} ]
					}, {
						layout : "form",
						border : false,
						labelSeparator : '：',
						width : 80,
						labelWidth : 40,
						items : [ {
							xtype : "checkbox",
							id : "saleOutStat",
							fieldLabel : "销售"
						} ]
					}, {
						layout : "form",
						border : false,
						labelSeparator : '：',
						width : 80,
						labelWidth : 40,
						items : [ {
							xtype : "checkbox",
							id : "returnOutStat",
							fieldLabel : "退货"
						} ]
					}, {
						layout : "form",
						border : false,
						labelSeparator : '：',
						width : 80,
						labelWidth : 40,
						items : [ {
							xtype : "checkbox",
							id : "outInventOutStat",
							fieldLabel : "出仓"
						} ]
					} ]
				} ]
			}, {
				layout : "column",
				border : false,
				anchor : "99% 15%",
				autoScroll : true,
				frame : true,
				items : [ {
					layout : "form",
					border : false,
					labelSeparator : '',
					width : 80,
					labelWidth : 40,
					items : [ {
						xtype : "checkbox",
						id : "dept1OutStat"
					// fieldLabel : departmentData[0][1]
					} ]
				}, {
					layout : "form",
					border : false,
					labelSeparator : '',
					width : 80,
					labelWidth : 40,
					items : [ {
						xtype : "checkbox",
						id : "dept2OutStat"
					// fieldLabel : departmentData[1][1]
					} ]
				}, {
					layout : "form",
					border : false,
					labelSeparator : '',
					width : 80,
					labelWidth : 40,
					items : [ {
						xtype : "checkbox",
						id : "dept3OutStat"
					// fieldLabel : departmentData[2][1]
					} ]
				}, {
					layout : "form",
					border : false,
					labelSeparator : '',
					width : 80,
					labelWidth : 40,
					items : [ {
						xtype : "checkbox",
						id : "dept4OutStat"
					// fieldLabel : departmentData[3][1]
					} ]
				}, {
					layout : "form",
					border : false,
					labelSeparator : '',
					width : 80,
					labelWidth : 40,
					items : [ {
						xtype : "checkbox",
						id : "dept5OutStat"
					// fieldLabel : departmentData[4][1]
					} ]
				}, {
					layout : "form",
					border : false,
					labelSeparator : '',
					width : 80,
					labelWidth : 40,
					items : [ {
						xtype : "checkbox",
						id : "dept6OutStat"
					// fieldLabel : departmentData[5][1]
					} ]
				}, {
					layout : "form",
					border : false,
					labelSeparator : '',
					width : 80,
					labelWidth : 40,
					items : [ {
						xtype : "checkbox",
						id : "dept7OutStat"
					// fieldLabel : departmentData[6][1]
					} ]
				}, {
					layout : "form",
					border : false,
					labelSeparator : '',
					width : 80,
					labelWidth : 40,
					items : [ {
						xtype : "checkbox",
						id : "dept8OutStat"
					// fieldLabel : departmentData[7][1]
					} ]
				}, {
					layout : "form",
					border : false,
					labelSeparator : '',
					width : 80,
					labelWidth : 40,
					items : [ {
						xtype : "checkbox",
						id : "dept9OutStat"
					// fieldLabel : departmentData[8][1]
					} ]
				}, {
					layout : "form",
					border : false,
					labelSeparator : '',
					width : 80,
					labelWidth : 40,
					items : [ {
						xtype : "checkbox",
						id : "dept10OutStat"
					// fieldLabel : departmentData[9][1]
					} ]
				} ]
			}, {
				layout : "form",
				border : false,
				frame : true,
				hideLabels : true,
				anchor : "right 56%",
				items : [ {
					xtype : "itemselector",
					name : "materialOutStatMultSelect",
					id : "materialOutStatMultSelect",
					fromStore : inventoryOutStatMSDS,
					dataFields : [ "retrunValue", "displayText" ],
					toData : [ [ "", "" ] ],
					msHeight : 175,
					valueField : "retrunValue",
					displayField : "displayText",
					imagePath : "../../extjs/multiselect/images/",
					toLegend : "已选择食材",
					fromLegend : "可选择食材"
				} ]
			}, inventoryOutStatFrom ],
			buttons : [
					{
						text : "全选",
						handler : function() {
							var i = inventoryOutStatWin
									.findById("materialOutStatMultSelect").fromMultiselect.view;
							i.selectRange(0, i.store.length);
							inventoryOutStatWin.findById(
									"materialOutStatMultSelect").fromTo();
							inventoryOutStatWin
									.findById("materialOutStatMultSelect").toMultiselect.view
									.clearSelections();
						}
					},
					{
						text : "清空",
						handler : function() {
							inventoryOutStatWin.findById(
									"materialOutStatMultSelect").reset();
						}
					},
					{
						text : "确定",
						handler : function() {

							var selectCount = inventoryOutStatWin
									.findById("materialOutStatMultSelect").toMultiselect.store
									.getCount();

							if (selectCount != 0) {
								isPrompt = false;
								inventoryOutStatWin.hide();

								// -- 獲取選擇的食材 --
								var selectMaterials = "";
								for ( var i = 0; i < selectCount; i++) {
									var selectItem = inventoryOutStatWin
											.findById("materialOutStatMultSelect").toMultiselect.store
											.getAt(i).get("retrunValue");
									// if (selectItem != "") {
									selectMaterials = selectMaterials
											+ selectItem + ",";
									// }
								}
								// 去掉最后一个逗号
								selectMaterials = selectMaterials.substring(0,
										selectMaterials.length - 1);
								if (selectMaterials.substring(0, 1) == ",") {
									selectMaterials = selectMaterials
											.substring(1,
													selectMaterials.length);
								}

								// -- 獲取時間 --
								var beginDate = inventoryOutStatWin.findById(
										"begDateOutStat").getValue();
								if (beginDate != "") {
									var dateFormated = new Date();
									dateFormated = beginDate;
									beginDate = dateFormated.format('Y-m-d');
								}

								var endDate = inventoryOutStatWin.findById(
										"endDateOutStat").getValue();
								if (endDate != "") {
									var dateFormated = new Date();
									dateFormated = endDate;
									endDate = dateFormated.format('Y-m-d');
								}

								// -- 獲取原因 --
								// type: 0 : 消耗 1 : 报损 2 : 销售 3 : 退货 4 : 入库 5 :
								// 调出 6 : 调入 7 : 盘点
								var reasons = "";
								if (inventoryOutStatWin.findById(
										"destroyOutStat").getValue() == true) {
									reasons = reasons + TYPE_WEAR + ",";
								}
								if (inventoryOutStatWin.findById("saleOutStat")
										.getValue() == true) {
									reasons = reasons + TYPE_SELL + ",";
								}
								if (inventoryOutStatWin.findById(
										"returnOutStat").getValue() == true) {
									reasons = reasons + TYPE_RETURN + ",";
								}
								if (inventoryOutStatWin.findById(
										"outInventOutStat").getValue() == true) {
									reasons = reasons + TYPE_OUT_WARE + ",";
								}
								if (reasons != "") {
									reasons = reasons.substring(0,
											reasons.length - 1);
								}

								// -- 獲取部門 --
								var departments = "";
								if (inventoryOutStatWin
										.findById("dept1OutStat").getValue() == true) {
									departments = departments + "0,";
								}
								if (inventoryOutStatWin
										.findById("dept2OutStat").getValue() == true) {
									departments = departments + "1,";
								}
								if (inventoryOutStatWin
										.findById("dept3OutStat").getValue() == true) {
									departments = departments + "2,";
								}
								if (inventoryOutStatWin
										.findById("dept4OutStat").getValue() == true) {
									departments = departments + "3,";
								}
								if (inventoryOutStatWin
										.findById("dept5OutStat").getValue() == true) {
									departments = departments + "4,";
								}
								if (inventoryOutStatWin
										.findById("dept6OutStat").getValue() == true) {
									departments = departments + "5,";
								}
								if (inventoryOutStatWin
										.findById("dept7OutStat").getValue() == true) {
									departments = departments + "6,";
								}
								if (inventoryOutStatWin
										.findById("dept8OutStat").getValue() == true) {
									departments = departments + "7,";
								}
								if (inventoryOutStatWin
										.findById("dept9OutStat").getValue() == true) {
									departments = departments + "8,";
								}
								if (inventoryOutStatWin.findById(
										"dept10OutStat").getValue() == true) {
									departments = departments + "9,";
								}

								if (departments != "") {
									departments = departments.substring(0,
											departments.length - 1);
								}

								// -- 獲取統計類型 --
								var staticType = inventoryOutStatFrom.getForm()
										.findField("OutStat").getGroupValue();
								if (staticType == "detail") {
									isPrompt = true;
									outStatDetailResultWin.show();

									// ＃＃＃＃＃＃＃＃＃＃＃＃明細統計＃＃＃＃＃＃＃＃＃＃＃＃
									Ext.Ajax
											.request({
												url : "../../InventoryOutStatDetail.do",
												params : {
													"pin" : pin,
													"beginDate" : beginDate,
													"endDate" : endDate,
													"reasons" : reasons,
													"departments" : departments,
													"materials" : selectMaterials
												},
												success : function(response,
														options) {
													var resultJSON = Ext.util.JSON
															.decode(response.responseText);
													// 格式：[食材id，日期，原因code，原因，經辦人，部門id，價格，數量，小計]
													var rootData = resultJSON.root;
													if (rootData[0].message == "normal") {
														outStatDetailResultData.length = 0;
														for ( var i = 0; i < rootData.length; i++) {
															var materialN = "";
															for ( var j = 0; j < materialData.length; j++) {
																if (materialData[j][0] == rootData[i].materialID) {
																	materialN = materialData[j][2];
																}
															}
															// type: 0 : 消耗 1 :
															// 报损 2 : 销售 3 : 退货
															// 4 : 入库 5 :
															// 调出 6 : 调入 7 : 盘点
															var reasonN = inventoryTypeCode2Descr(rootData[i].reasonCode);

															var deptN = "";
															for ( var j = 0; j < departmentData.length; j++) {
																if (departmentData[j][0] == rootData[i].departmentID) {
																	deptN = departmentData[j][1];
																}
															}

															outStatDetailResultData
																	.push([
																			rootData[i].materialID,
																			materialN,
																			rootData[i].date,
																			rootData[i].reasonCode,
																			reasonN,
																			rootData[i].operator,
																			rootData[i].departmentID,
																			deptN,
																			rootData[i].price,
																			rootData[i].amount,
																			rootData[i].total

																	]);
														}

														outStatDetailResultStore
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

								} else if (staticType == "sumByMaterial") {
									isPrompt = true;
									outStatByMaterialResultWin.show();

									// ＃＃＃＃＃＃＃＃＃＃＃＃按食材統計＃＃＃＃＃＃＃＃＃＃＃＃
									Ext.Ajax
											.request({
												url : "../../InventoryOutStatByMaterial.do",
												params : {
													"pin" : pin,
													"beginDate" : beginDate,
													"endDate" : endDate,
													"reasons" : reasons,
													"departments" : departments,
													"materials" : selectMaterials
												},
												success : function(response,
														options) {
													var resultJSON = Ext.util.JSON
															.decode(response.responseText);

													var rootData = resultJSON.root;
													if (rootData[0].message == "normal") {

														outStatByMaterialResultData = rootData;
														outStatByMateriaGrid
																.getStore()
																.loadData(
																		outStatByMaterialResultData);
														if (rootData[0].materialID == "NO_DATA") {
															outStatByMateriaGrid
																	.getStore()
																	.removeAll();
														}

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

								} else if (staticType == "sumByDept") {

									isPrompt = true;
									outStatByDeptResultWin.show();

									// ＃＃＃＃＃＃＃＃＃＃＃＃按部門統計＃＃＃＃＃＃＃＃＃＃＃＃
									Ext.Ajax
											.request({
												url : "../../InventoryOutStatByDept.do",
												params : {
													"pin" : pin,
													"beginDate" : beginDate,
													"endDate" : endDate,
													"reasons" : reasons,
													"departments" : departments,
													"materials" : selectMaterials
												},
												success : function(response,
														options) {
													var resultJSON = Ext.util.JSON
															.decode(response.responseText);

													var rootData = resultJSON.root;
													if (rootData[0].message == "normal") {

														outStatByDeptResultData = rootData;
														outStatByDeptGrid
																.getStore()
																.loadData(
																		outStatByDeptResultData);
														if (rootData[0].materialID == "NO_DATA") {
															outStatByDeptGrid
																	.getStore()
																	.removeAll();
														}

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

								} else if (staticType == "sumByReason") {

									isPrompt = true;
									outStatByReasonResultWin.show();

									// ＃＃＃＃＃＃＃＃＃＃＃＃按原因統計＃＃＃＃＃＃＃＃＃＃＃＃
									Ext.Ajax
											.request({
												url : "../../InventoryOutStatByReason.do",
												params : {
													"pin" : pin,
													"beginDate" : beginDate,
													"endDate" : endDate,
													"reasons" : reasons,
													"departments" : departments,
													"materials" : selectMaterials
												},
												success : function(response,
														options) {
													var resultJSON = Ext.util.JSON
															.decode(response.responseText);

													var rootData = resultJSON.root;
													if (rootData[0].message == "normal") {

														outStatByReasonResultData = rootData;
														outStatByReasonGrid
																.getStore()
																.loadData(
																		outStatByReasonResultData);
														if (rootData[0].materialID == "NO_DATA") {
															outStatByReasonGrid
																	.getStore()
																	.removeAll();
														}

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

							} else {
								Ext.MessageBox.show({
									msg : "至少需要选择一个食材进行统计",
									width : 300,
									buttons : Ext.MessageBox.OK
								});
							}
						}
					}, {
						text : "取消",
						handler : function() {
							isPrompt = false;
							inventoryOutStatWin.hide();
						}
					} ],
			listeners : {
				"show" : function(thiz) {

					inventoryOutStatWin.findById("materialOutStatMultSelect")
							.reset();
					inventoryOutStatWin.findById("begDateOutStat").setValue("");
					inventoryOutStatWin.findById("endDateOutStat").setValue("");

					inventoryOutStatWin.findById("destroyOutStat").setValue(
							false);
					inventoryOutStatWin.findById("saleOutStat").setValue(false);
					inventoryOutStatWin.findById("returnOutStat").setValue(
							false);
					inventoryOutStatWin.findById("outInventOutStat").setValue(
							false);

					// inventoryOutStatMSDS.loadData(materialComboData);

					// 神技！動態改變form中component的label！！！
					inventoryOutStatWin.findById("dept1OutStat").el.parent()
							.parent().parent().first().dom.innerHTML = departmentData[0][1]
							+ ":";
					inventoryOutStatWin.findById("dept2OutStat").el.parent()
							.parent().parent().first().dom.innerHTML = departmentData[1][1]
							+ ":";
					inventoryOutStatWin.findById("dept3OutStat").el.parent()
							.parent().parent().first().dom.innerHTML = departmentData[2][1]
							+ ":";
					inventoryOutStatWin.findById("dept4OutStat").el.parent()
							.parent().parent().first().dom.innerHTML = departmentData[3][1]
							+ ":";
					inventoryOutStatWin.findById("dept5OutStat").el.parent()
							.parent().parent().first().dom.innerHTML = departmentData[4][1]
							+ ":";
					inventoryOutStatWin.findById("dept6OutStat").el.parent()
							.parent().parent().first().dom.innerHTML = departmentData[5][1]
							+ ":";
					inventoryOutStatWin.findById("dept7OutStat").el.parent()
							.parent().parent().first().dom.innerHTML = departmentData[6][1]
							+ ":";
					inventoryOutStatWin.findById("dept8OutStat").el.parent()
							.parent().parent().first().dom.innerHTML = departmentData[7][1]
							+ ":";
					inventoryOutStatWin.findById("dept9OutStat").el.parent()
							.parent().parent().first().dom.innerHTML = departmentData[8][1]
							+ ":";
					inventoryOutStatWin.findById("dept10OutStat").el.parent()
							.parent().parent().first().dom.innerHTML = departmentData[9][1]
							+ ":";

					inventoryOutStatWin.findById("dept1OutStat")
							.setValue(false);
					inventoryOutStatWin.findById("dept2OutStat")
							.setValue(false);
					inventoryOutStatWin.findById("dept3OutStat")
							.setValue(false);
					inventoryOutStatWin.findById("dept4OutStat")
							.setValue(false);
					inventoryOutStatWin.findById("dept5OutStat")
							.setValue(false);
					inventoryOutStatWin.findById("dept6OutStat")
							.setValue(false);
					inventoryOutStatWin.findById("dept7OutStat")
							.setValue(false);
					inventoryOutStatWin.findById("dept8OutStat")
							.setValue(false);
					inventoryOutStatWin.findById("dept9OutStat")
							.setValue(false);
					inventoryOutStatWin.findById("dept10OutStat").setValue(
							false);

					inventoryOutStatFrom.getForm().findField("OutStat")
							.setValue("detail");

				},
				"hide" : function(thiz) {
					isPrompt = false;
				}
			}
		});

// 结果框 -- 明細
// 前台：[食材 日期 出庫原因 经手人 部门 价格 数量 小计]
var outStatDetailResultStore = new Ext.data.Store({
	proxy : new Ext.data.MemoryProxy(outStatDetailResultData),
	reader : new Ext.data.ArrayReader({}, [ {
		name : "materialID"
	}, {
		name : "materialName"
	}, {
		name : "date"
	}, {
		name : "reasonCode"
	}, {
		name : "reasonName"
	}, {
		name : "operator"
	}, {
		name : "departmentID"
	}, {
		name : "departmentName"
	}, {
		name : "price"
	}, {
		name : "amount"
	}, {
		name : "total"
	}, {
		name : "message"
	} ])
});

// 2，栏位模型
var outStatDetailResultColumnModel = new Ext.grid.ColumnModel([
		new Ext.grid.RowNumberer(), {
			header : "食材",
			sortable : true,
			dataIndex : "materialName",
			width : 80
		}, {
			header : "日期",
			sortable : true,
			dataIndex : "date",
			width : 80
		}, {
			header : "出库原因",
			sortable : true,
			dataIndex : "reasonName",
			width : 80
		}, {
			header : "经手人",
			sortable : true,
			dataIndex : "operator",
			width : 80
		}, {
			header : "部门",
			sortable : true,
			dataIndex : "departmentName",
			width : 80
		}, {
			header : "价钱",
			sortable : true,
			dataIndex : "price",
			width : 80
		}, {
			header : "数量",
			sortable : true,
			dataIndex : "amount",
			width : 80
		}, {
			header : "小计（￥）",
			sortable : true,
			dataIndex : "total",
			width : 80
		} ]);

var outStatDetailResultGrid = new Ext.grid.GridPanel({
	xtype : "grid",
	anchor : "99%",
	border : false,
	ds : outStatDetailResultStore,
	cm : outStatDetailResultColumnModel,
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

var outStatDetailResultWin = new Ext.Window({
	title : "出库明细",
	width : 800,
	height : 370,
	closeAction : "hide",
	resizable : false,
	layout : "fit",
	items : outStatDetailResultGrid,
	buttons : [ {
		text : "打印",
		handler : function() {

		}
	}, {
		text : "关闭",
		handler : function() {
			isPrompt = false;
			outStatDetailResultWin.hide();
		}
	} ],
	listeners : {
		"hide" : function(thiz) {
			isPrompt = false;
		}
	}
});

// 结果框 -- 按食材
// --------------------------------------------------------------------------------------------------------
var outStatByMateriaReader = new Ext.data.JsonReader({
	idProperty : 'groupID',
	fields : [ {
		name : 'materialID',
		type : 'int'
	}, {
		name : 'materialName',
		type : 'string'
	}, {
		name : 'groupID',
		type : 'int'
	}, {
		name : 'groupDescr',
		type : 'string'
	}, {
		name : 'singlePrice',
		type : 'float'
	}, {
		name : 'amount',
		type : 'float'
	}, {
		name : 'sumPrice',
		type : 'float'
	}, {
		name : 'deptID'
	}, {
		name : 'deptName',
		type : 'string'
	} ]

});

var outStatByMateriaSummary = new Ext.grid.GroupSummary();

var outStatByMateriaGrid = new Ext.grid.EditorGridPanel({
	ds : new Ext.data.GroupingStore({
		reader : outStatByMateriaReader,
		data : [],
		sortInfo : {
			field : 'deptName',
			direction : "ASC"
		},
		groupField : 'materialName'
	}),

	columns : [ {
		id : 'groupDescr',
		header : "食材",
		width : 20,
		sortable : true,
		dataIndex : 'groupDescr',
		// summaryType : 'count',
		hideable : false,
		// summaryRenderer : function(v, params, data) {
		// return ((v === 0 || v > 1) ? '(' + v + ' Tasks)' : '(1 Task)');
		// }
		summaryRenderer : function(v, params, data) {
			return "合计";
		}
	}, {
		header : "NOT SHOW",
		width : 20,
		sortable : false,
		dataIndex : 'materialName'
	}, {
		header : "部门",
		width : 25,
		sortable : true,
		dataIndex : 'deptName'
	}, {
		id : 'singlePrice',
		header : "价格",
		width : 20,
		sortable : false, // 計出來的列排序不起作用
		groupable : false,
		renderer : function(v, params, record) {
			// return Ext.util.Format.usMoney(record.data.sumPrice
			// / record.data.amount);
			if (record.data.amount != 0) {
				return (record.data.sumPrice / record.data.amount).toFixed(2);
			} else {
				return 0.00;
			}
		},
		dataIndex : 'singlePrice',
		summaryType : 'singlePrice'
	// summaryRenderer : Ext.util.Format.usMoney
	}, {
		header : "数量",
		width : 20,
		sortable : true,
		// renderer : Ext.util.Format.usMoney,
		dataIndex : 'amount',
		summaryType : 'sum'
	// editor: new Ext.form.NumberField({
	// allowBlank: false,
	// allowNegative: false,
	// style: 'text-align:left'
	// })
	}, {
		header : "小计",
		width : 20,
		sortable : true,
		// renderer : Ext.util.Format.chnMoney,
		dataIndex : 'sumPrice',
		summaryType : 'sum',
		renderer : function(v) {
			return parseFloat(v).toFixed(2);
		}
	// editor : new Ext.form.NumberField({
	// allowBlank : false,
	// allowNegative : false,
	// style : 'text-align:left'
	// })
	} ],

	view : new Ext.grid.GroupingView({
		forceFit : true,
		showGroupName : false,
		enableNoGroups : false, // REQUIRED!
		hideGroupedColumn : true
	}),

	plugins : outStatByMateriaSummary,

	frame : true,
	width : 800,
	height : 450,
	clicksToEdit : 1,
	collapsible : true,
	animCollapse : false,
	trackMouseOver : false
// enableColumnMove: false,
// title : 'Sponsored Projects'
// iconCls: 'icon-grid',
// renderTo: document.body
});

// --------------------------------------------------------------------------------------------------------

var outStatByMaterialResultWin = new Ext.Window({
	title : "出库汇总-按食材",
	width : 400,
	height : 500,
	closeAction : "hide",
	resizable : false,
	layout : "fit",
	items : outStatByMateriaGrid,
	buttons : [ {
		text : "打印",
		handler : function() {

		}
	}, {
		text : "关闭",
		handler : function() {
			isPrompt = false;
			outStatByMaterialResultWin.hide();
		}
	} ],
	listeners : {
		"hide" : function(thiz) {
			isPrompt = false;
		}
	}
});

// 结果框 -- 按部門
// --------------------------------------------------------------------------------------------------------
var outStatByDeptReader = new Ext.data.JsonReader({
	idProperty : 'groupID',
	fields : [ {
		name : 'deptID',
		type : 'int'
	}, {
		name : 'deptName',
		type : 'string'
	}, {
		name : 'groupID',
		type : 'int'
	}, {
		name : 'groupDescr',
		type : 'string'
	}, {
		name : 'singlePrice',
		type : 'float'
	}, {
		name : 'amount',
		type : 'float'
	}, {
		name : 'sumPrice',
		type : 'float'
	}, {
		name : 'materialID'
	}, {
		name : 'materialName',
		type : 'string'
	} ]

});

var outStatByDeptSummary = new Ext.grid.GroupSummary();

var outStatByDeptGrid = new Ext.grid.EditorGridPanel({
	ds : new Ext.data.GroupingStore({
		reader : outStatByDeptReader,
		data : [],
		sortInfo : {
			field : 'materialName',
			direction : "ASC"
		},
		groupField : 'deptName'
	}),

	columns : [ {
		id : 'groupDescr',
		header : "部门",
		width : 20,
		sortable : true,
		dataIndex : 'groupDescr',
		// summaryType : 'count',
		hideable : false,
		// summaryRenderer : function(v, params, data) {
		// return ((v === 0 || v > 1) ? '(' + v + ' Tasks)' : '(1 Task)');
		// }
		summaryRenderer : function(v, params, data) {
			return "合计";
		}
	}, {
		header : "NOT SHOW",
		width : 20,
		sortable : false,
		dataIndex : 'deptName'
	}, {
		header : "食材",
		width : 25,
		sortable : true,
		dataIndex : 'materialName'
	}, {
		id : 'singlePrice',
		header : "价格",
		width : 20,
		sortable : false, // 計出來的列排序不起作用
		groupable : false,
		renderer : function(v, params, record) {
			// return Ext.util.Format.usMoney(record.data.sumPrice
			// / record.data.amount);
			if (record.data.amount != 0) {
				return (record.data.sumPrice / record.data.amount).toFixed(2);
			} else {
				return 0.00;
			}
		},
		dataIndex : 'singlePrice',
		summaryType : 'singlePrice'
	// summaryRenderer : Ext.util.Format.usMoney
	}, {
		header : "数量",
		width : 20,
		sortable : true,
		// renderer : Ext.util.Format.usMoney,
		dataIndex : 'amount',
		summaryType : 'sum'
	// editor: new Ext.form.NumberField({
	// allowBlank: false,
	// allowNegative: false,
	// style: 'text-align:left'
	// })
	}, {
		header : "小计",
		width : 20,
		sortable : true,
		// renderer : Ext.util.Format.chnMoney,
		dataIndex : 'sumPrice',
		summaryType : 'sum',
		renderer : function(v) {
			return parseFloat(v).toFixed(2);
		}
	// editor : new Ext.form.NumberField({
	// allowBlank : false,
	// allowNegative : false,
	// style : 'text-align:left'
	// })
	} ],

	view : new Ext.grid.GroupingView({
		forceFit : true,
		showGroupName : false,
		enableNoGroups : false, // REQUIRED!
		hideGroupedColumn : true
	}),

	plugins : outStatByDeptSummary,

	frame : true,
	width : 800,
	height : 450,
	clicksToEdit : 1,
	collapsible : true,
	animCollapse : false,
	trackMouseOver : false
// enableColumnMove: false,
// title : 'Sponsored Projects'
// iconCls: 'icon-grid',
// renderTo: document.body
});

// --------------------------------------------------------------------------------------------------------

var outStatByDeptResultWin = new Ext.Window({
	title : "出库汇总-按部门",
	width : 400,
	height : 500,
	closeAction : "hide",
	resizable : false,
	layout : "fit",
	items : outStatByDeptGrid,
	buttons : [ {
		text : "打印",
		handler : function() {

		}
	}, {
		text : "关闭",
		handler : function() {
			isPrompt = false;
			outStatByDeptResultWin.hide();
		}
	} ],
	listeners : {
		"hide" : function(thiz) {
			isPrompt = false;
		}
	}
});

// 结果框 -- 按原因
// --------------------------------------------------------------------------------------------------------
var outStatByReasonReader = new Ext.data.JsonReader({
	idProperty : 'groupID',
	fields : [ {
		name : 'reasonCode',
		type : 'int'
	}, {
		name : 'reasonName',
		type : 'string'
	}, {
		name : 'groupID',
		type : 'int'
	}, {
		name : 'groupDescr',
		type : 'string'
	}, {
		name : 'singlePrice',
		type : 'float'
	}, {
		name : 'amount',
		type : 'float'
	}, {
		name : 'sumPrice',
		type : 'float'
	}, {
		name : 'materialID'
	}, {
		name : 'materialName',
		type : 'string'
	} ]

});

var outStatByReasonSummary = new Ext.grid.GroupSummary();

var outStatByReasonGrid = new Ext.grid.EditorGridPanel({
	ds : new Ext.data.GroupingStore({
		reader : outStatByReasonReader,
		data : [],
		sortInfo : {
			field : 'materialName',
			direction : "ASC"
		},
		groupField : 'reasonName'
	}),

	columns : [ {
		id : 'groupDescr',
		header : "出库原因",
		width : 20,
		sortable : true,
		dataIndex : 'groupDescr',
		// summaryType : 'count',
		hideable : false,
		// summaryRenderer : function(v, params, data) {
		// return ((v === 0 || v > 1) ? '(' + v + ' Tasks)' : '(1 Task)');
		// }
		summaryRenderer : function(v, params, data) {
			return "合计";
		}
	}, {
		header : "NOT SHOW",
		width : 20,
		sortable : false,
		dataIndex : 'reasonName'
	}, {
		header : "食材",
		width : 25,
		sortable : true,
		dataIndex : 'materialName'
	}, {
		id : 'singlePrice',
		header : "价格",
		width : 20,
		sortable : false, // 計出來的列排序不起作用
		groupable : false,
		renderer : function(v, params, record) {
			// return Ext.util.Format.usMoney(record.data.sumPrice
			// / record.data.amount);
			if (record.data.amount != 0) {
				return (record.data.sumPrice / record.data.amount).toFixed(2);
			} else {
				return 0.00;
			}
		},
		dataIndex : 'singlePrice',
		summaryType : 'singlePrice'
	// summaryRenderer : Ext.util.Format.usMoney
	}, {
		header : "数量",
		width : 20,
		sortable : true,
		// renderer : Ext.util.Format.usMoney,
		dataIndex : 'amount',
		summaryType : 'sum'
	// editor: new Ext.form.NumberField({
	// allowBlank: false,
	// allowNegative: false,
	// style: 'text-align:left'
	// })
	}, {
		header : "小计",
		width : 20,
		sortable : true,
		// renderer : Ext.util.Format.chnMoney,
		dataIndex : 'sumPrice',
		summaryType : 'sum',
		renderer : function(v) {
			return parseFloat(v).toFixed(2);
		}
	// editor : new Ext.form.NumberField({
	// allowBlank : false,
	// allowNegative : false,
	// style : 'text-align:left'
	// })
	} ],

	view : new Ext.grid.GroupingView({
		forceFit : true,
		showGroupName : false,
		enableNoGroups : false, // REQUIRED!
		hideGroupedColumn : true
	}),

	plugins : outStatByReasonSummary,

	frame : true,
	width : 800,
	height : 450,
	clicksToEdit : 1,
	collapsible : true,
	animCollapse : false,
	trackMouseOver : false
// enableColumnMove: false,
// title : 'Sponsored Projects'
// iconCls: 'icon-grid',
// renderTo: document.body
});

// --------------------------------------------------------------------------------------------------------

var outStatByReasonResultWin = new Ext.Window({
	title : "出库汇总-按原因",
	width : 400,
	height : 500,
	closeAction : "hide",
	resizable : false,
	layout : "fit",
	items : outStatByReasonGrid,
	buttons : [ {
		text : "打印",
		handler : function() {

		}
	}, {
		text : "关闭",
		handler : function() {
			isPrompt = false;
			outStatByReasonResultWin.hide();
		}
	} ],
	listeners : {
		"hide" : function(thiz) {
			isPrompt = false;
		}
	}
});

// --------------------------------------end出庫統計----------------------------------------------

// ---------------------------------------调拨統計--------------------------------------------------------
// 条件框
var inventoryChangeStatMSDS = new Ext.data.SimpleStore({
	fields : [ "retrunValue", "displayText" ],
	data : []
});

var inventoryChangeStatFrom = new Ext.form.FormPanel({
	border : false,
	anchor : "right 8%",
	id : "ChangeStatForm",
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
				name : 'ChangeStat',
				inputValue : 'detail'
			} ]
		}, {
			layout : "form",
			border : false,
			labelSeparator : '',
			width : 110,
			items : [ {
				xtype : 'radio',
				hideLabel : true,
				boxLabel : "按食材汇总",
				// checked : true,
				name : 'ChangeStat',
				inputValue : 'sumByMaterial'
			} ]
		}, {
			layout : "form",
			border : false,
			labelSeparator : '',
			width : 125,
			items : [ {
				xtype : 'radio',
				hideLabel : true,
				boxLabel : "按调出部门汇总",
				// checked : true,
				name : 'ChangeStat',
				inputValue : 'sumByOutDept'
			} ]
		}, {
			layout : "form",
			border : false,
			labelSeparator : '',
			width : 110,
			items : [ {
				xtype : 'radio',
				hideLabel : true,
				boxLabel : "按调入部门汇总",
				// checked : true,
				name : 'ChangeStat',
				inputValue : 'sumByInDept'
			} ]
		} ]
	} ]
});

inventoryChangeStatWin = new Ext.Window(
		{
			title : "调拨统计",
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
							xtype : "datefield",
							id : "begDateChangeStat",
							width : 120,
							fieldLabel : "日期"
						} ]
					}, {
						layout : "form",
						border : false,
						labelSeparator : ' ',
						width : 200,
						labelWidth : 50,
						items : [ {
							xtype : "datefield",
							id : "endDateChangeStat",
							width : 120,
							fieldLabel : "至"
						} ]
					} ]
				} ]
			}, {
				layout : "column",
				border : false,
				anchor : "99% 15%",
				autoScroll : true,
				frame : true,
				items : [ {
					layout : "form",
					border : false,
					labelSeparator : '',
					width : 80,
					labelWidth : 40,
					items : [ {
						xtype : "checkbox",
						id : "dept1ChengeOutStat"
					// fieldLabel : departmentData[0][1]
					} ]
				}, {
					layout : "form",
					border : false,
					labelSeparator : '',
					width : 80,
					labelWidth : 40,
					items : [ {
						xtype : "checkbox",
						id : "dept2ChengeOutStat"
					// fieldLabel : departmentData[1][1]
					} ]
				}, {
					layout : "form",
					border : false,
					labelSeparator : '',
					width : 80,
					labelWidth : 40,
					items : [ {
						xtype : "checkbox",
						id : "dept3ChengeOutStat"
					// fieldLabel : departmentData[2][1]
					} ]
				}, {
					layout : "form",
					border : false,
					labelSeparator : '',
					width : 80,
					labelWidth : 40,
					items : [ {
						xtype : "checkbox",
						id : "dept4ChengeOutStat"
					// fieldLabel : departmentData[3][1]
					} ]
				}, {
					layout : "form",
					border : false,
					labelSeparator : '',
					width : 80,
					labelWidth : 40,
					items : [ {
						xtype : "checkbox",
						id : "dept5ChengeOutStat"
					// fieldLabel : departmentData[4][1]
					} ]
				}, {
					layout : "form",
					border : false,
					labelSeparator : '',
					width : 80,
					labelWidth : 40,
					items : [ {
						xtype : "checkbox",
						id : "dept6ChengeOutStat"
					// fieldLabel : departmentData[5][1]
					} ]
				}, {
					layout : "form",
					border : false,
					labelSeparator : '',
					width : 80,
					labelWidth : 40,
					items : [ {
						xtype : "checkbox",
						id : "dept7ChengeOutStat"
					// fieldLabel : departmentData[6][1]
					} ]
				}, {
					layout : "form",
					border : false,
					labelSeparator : '',
					width : 80,
					labelWidth : 40,
					items : [ {
						xtype : "checkbox",
						id : "dept8ChengeOutStat"
					// fieldLabel : departmentData[7][1]
					} ]
				}, {
					layout : "form",
					border : false,
					labelSeparator : '',
					width : 80,
					labelWidth : 40,
					items : [ {
						xtype : "checkbox",
						id : "dept9ChengeOutStat"
					// fieldLabel : departmentData[8][1]
					} ]
				}, {
					layout : "form",
					border : false,
					labelSeparator : '',
					width : 80,
					labelWidth : 40,
					items : [ {
						xtype : "checkbox",
						id : "dept10ChengeOutStat"
					// fieldLabel : departmentData[9][1]
					} ]
				} ]
			}, {
				layout : "column",
				border : false,
				anchor : "99% 15%",
				autoScroll : true,
				frame : true,
				items : [ {
					layout : "form",
					border : false,
					labelSeparator : '',
					width : 80,
					labelWidth : 40,
					items : [ {
						xtype : "checkbox",
						id : "dept1ChengeInStat"
					// fieldLabel : departmentData[0][1]
					} ]
				}, {
					layout : "form",
					border : false,
					labelSeparator : '',
					width : 80,
					labelWidth : 40,
					items : [ {
						xtype : "checkbox",
						id : "dept2ChengeInStat"
					// fieldLabel : departmentData[1][1]
					} ]
				}, {
					layout : "form",
					border : false,
					labelSeparator : '',
					width : 80,
					labelWidth : 40,
					items : [ {
						xtype : "checkbox",
						id : "dept3ChengeInStat"
					// fieldLabel : departmentData[2][1]
					} ]
				}, {
					layout : "form",
					border : false,
					labelSeparator : '',
					width : 80,
					labelWidth : 40,
					items : [ {
						xtype : "checkbox",
						id : "dept4ChengeInStat"
					// fieldLabel : departmentData[3][1]
					} ]
				}, {
					layout : "form",
					border : false,
					labelSeparator : '',
					width : 80,
					labelWidth : 40,
					items : [ {
						xtype : "checkbox",
						id : "dept5ChengeInStat"
					// fieldLabel : departmentData[4][1]
					} ]
				}, {
					layout : "form",
					border : false,
					labelSeparator : '',
					width : 80,
					labelWidth : 40,
					items : [ {
						xtype : "checkbox",
						id : "dept6ChengeInStat"
					// fieldLabel : departmentData[5][1]
					} ]
				}, {
					layout : "form",
					border : false,
					labelSeparator : '',
					width : 80,
					labelWidth : 40,
					items : [ {
						xtype : "checkbox",
						id : "dept7ChengeInStat"
					// fieldLabel : departmentData[6][1]
					} ]
				}, {
					layout : "form",
					border : false,
					labelSeparator : '',
					width : 80,
					labelWidth : 40,
					items : [ {
						xtype : "checkbox",
						id : "dept8ChengeInStat"
					// fieldLabel : departmentData[7][1]
					} ]
				}, {
					layout : "form",
					border : false,
					labelSeparator : '',
					width : 80,
					labelWidth : 40,
					items : [ {
						xtype : "checkbox",
						id : "dept9ChengeInStat"
					// fieldLabel : departmentData[8][1]
					} ]
				}, {
					layout : "form",
					border : false,
					labelSeparator : '',
					width : 80,
					labelWidth : 40,
					items : [ {
						xtype : "checkbox",
						id : "dept10ChengeInStat"
					// fieldLabel : departmentData[9][1]
					} ]
				} ]
			}, {
				layout : "form",
				border : false,
				frame : true,
				hideLabels : true,
				anchor : "right 51%",
				items : [ {
					xtype : "itemselector",
					name : "materialChangeStatMultSelect",
					id : "materialChangeStatMultSelect",
					fromStore : inventoryChangeStatMSDS,
					dataFields : [ "retrunValue", "displayText" ],
					toData : [ [ "", "" ] ],
					msHeight : 155,
					valueField : "retrunValue",
					displayField : "displayText",
					imagePath : "../../extjs/multiselect/images/",
					toLegend : "已选择食材",
					fromLegend : "可选择食材"
				} ]
			}, inventoryChangeStatFrom ],
			buttons : [
					{
						text : "全选",
						handler : function() {
							var i = inventoryChangeStatWin
									.findById("materialChangeStatMultSelect").fromMultiselect.view;
							i.selectRange(0, i.store.length);
							inventoryChangeStatWin.findById(
									"materialChangeStatMultSelect").fromTo();
							inventoryChangeStatWin
									.findById("materialChangeStatMultSelect").toMultiselect.view
									.clearSelections();
						}
					},
					{
						text : "清空",
						handler : function() {
							inventoryChangeStatWin.findById(
									"materialChangeStatMultSelect").reset();
						}
					},
					{
						text : "确定",
						handler : function() {

							var selectCount = inventoryChangeStatWin
									.findById("materialChangeStatMultSelect").toMultiselect.store
									.getCount();

							if (selectCount != 0) {
								isPrompt = false;
								inventoryChangeStatWin.hide();

								// -- 獲取選擇的食材 --
								var selectMaterials = "";
								for ( var i = 0; i < selectCount; i++) {
									var selectItem = inventoryChangeStatWin
											.findById("materialChangeStatMultSelect").toMultiselect.store
											.getAt(i).get("retrunValue");
									// if (selectItem != "") {
									selectMaterials = selectMaterials
											+ selectItem + ",";
									// }
								}
								// 去掉最后一个逗号
								selectMaterials = selectMaterials.substring(0,
										selectMaterials.length - 1);
								if (selectMaterials.substring(0, 1) == ",") {
									selectMaterials = selectMaterials
											.substring(1,
													selectMaterials.length);
								}

								// -- 獲取時間 --
								var beginDate = inventoryChangeStatWin
										.findById("begDateChangeStat")
										.getValue();
								if (beginDate != "") {
									var dateFormated = new Date();
									dateFormated = beginDate;
									beginDate = dateFormated.format('Y-m-d');
								}

								var endDate = inventoryChangeStatWin.findById(
										"endDateChangeStat").getValue();
								if (endDate != "") {
									var dateFormated = new Date();
									dateFormated = endDate;
									endDate = dateFormated.format('Y-m-d');
								}

								// -- 獲取調出部門 --
								var outDepartments = "";
								if (inventoryChangeStatWin.findById(
										"dept1ChengeOutStat").getValue() == true) {
									outDepartments = outDepartments + "0,";
								}
								if (inventoryChangeStatWin.findById(
										"dept2ChengeOutStat").getValue() == true) {
									outDepartments = outDepartments + "1,";
								}
								if (inventoryChangeStatWin.findById(
										"dept3ChengeOutStat").getValue() == true) {
									outDepartments = outDepartments + "2,";
								}
								if (inventoryChangeStatWin.findById(
										"dept4ChengeOutStat").getValue() == true) {
									outDepartments = outDepartments + "3,";
								}
								if (inventoryChangeStatWin.findById(
										"dept5ChengeOutStat").getValue() == true) {
									outDepartments = outDepartments + "4,";
								}
								if (inventoryChangeStatWin.findById(
										"dept6ChengeOutStat").getValue() == true) {
									outDepartments = outDepartments + "5,";
								}
								if (inventoryChangeStatWin.findById(
										"dept7ChengeOutStat").getValue() == true) {
									outDepartments = outDepartments + "6,";
								}
								if (inventoryChangeStatWin.findById(
										"dept8ChengeOutStat").getValue() == true) {
									outDepartments = outDepartments + "7,";
								}
								if (inventoryChangeStatWin.findById(
										"dept9ChengeOutStat").getValue() == true) {
									outDepartments = outDepartments + "8,";
								}
								if (inventoryChangeStatWin.findById(
										"dept10ChengeOutStat").getValue() == true) {
									outDepartments = outDepartments + "9,";
								}

								if (outDepartments != "") {
									outDepartments = outDepartments.substring(
											0, outDepartments.length - 1);
								}

								// -- 獲取部門 --
								var inDepartments = "";
								if (inventoryChangeStatWin.findById(
										"dept1ChengeInStat").getValue() == true) {
									inDepartments = inDepartments + "0,";
								}
								if (inventoryChangeStatWin.findById(
										"dept2ChengeInStat").getValue() == true) {
									inDepartments = inDepartments + "1,";
								}
								if (inventoryChangeStatWin.findById(
										"dept3ChengeInStat").getValue() == true) {
									inDepartments = inDepartments + "2,";
								}
								if (inventoryChangeStatWin.findById(
										"dept4ChengeInStat").getValue() == true) {
									inDepartments = inDepartments + "3,";
								}
								if (inventoryChangeStatWin.findById(
										"dept5ChengeInStat").getValue() == true) {
									inDepartments = inDepartments + "4,";
								}
								if (inventoryChangeStatWin.findById(
										"dept6ChengeInStat").getValue() == true) {
									inDepartments = inDepartments + "5,";
								}
								if (inventoryChangeStatWin.findById(
										"dept7ChengeInStat").getValue() == true) {
									inDepartments = inDepartments + "6,";
								}
								if (inventoryChangeStatWin.findById(
										"dept8ChengeInStat").getValue() == true) {
									inDepartments = inDepartments + "7,";
								}
								if (inventoryChangeStatWin.findById(
										"dept9ChengeInStat").getValue() == true) {
									inDepartments = inDepartments + "8,";
								}
								if (inventoryChangeStatWin.findById(
										"dept10ChengeInStat").getValue() == true) {
									inDepartments = inDepartments + "9,";
								}

								if (inDepartments != "") {
									inDepartments = inDepartments.substring(0,
											inDepartments.length - 1);
								}

								// -- 獲取統計類型 --
								var staticType = inventoryChangeStatFrom
										.getForm().findField("ChangeStat")
										.getGroupValue();
								if (staticType == "detail") {
									isPrompt = true;
									changeStatDetailResultWin.show();

									// ＃＃＃＃＃＃＃＃＃＃＃＃明細統計＃＃＃＃＃＃＃＃＃＃＃＃
									Ext.Ajax
											.request({
												url : "../../InventoryChangeStatDetail.do",
												params : {
													"pin" : pin,
													"beginDate" : beginDate,
													"endDate" : endDate,
													"outDepartments" : outDepartments,
													"inDepartments" : inDepartments,
													"materials" : selectMaterials
												},
												success : function(response,
														options) {
													var resultJSON = Ext.util.JSON
															.decode(response.responseText);

													// 格式：[食材id，日期，inDeptId，inDept，outDeptId，outDept，經辦人，價格，數量，小計]
													var rootData = resultJSON.root;
													if (rootData[0].message == "normal") {
														changeStatDetailResultData.length = 0;
														for ( var i = 0; i < rootData.length; i++) {
															var materialN = "";
															for ( var j = 0; j < materialData.length; j++) {
																if (materialData[j][0] == rootData[i].materialID) {
																	materialN = materialData[j][2];
																}
															}

															var outDeptN = "";
															for ( var j = 0; j < departmentData.length; j++) {
																if (departmentData[j][0] == rootData[i].outDeptID) {
																	outDeptN = departmentData[j][1];
																}
															}
															var inDeptN = "";
															for ( var j = 0; j < departmentData.length; j++) {
																if (departmentData[j][0] == rootData[i].inDeptID) {
																	inDeptN = departmentData[j][1];
																}
															}

															changeStatDetailResultData
																	.push([
																			rootData[i].materialID,
																			materialN,
																			rootData[i].date,
																			rootData[i].outDeptID,
																			outDeptN,
																			rootData[i].inDeptID,
																			inDeptN,
																			rootData[i].operator,
																			rootData[i].price,
																			rootData[i].amount,
																			rootData[i].total

																	]);
														}

														changeStatDetailResultStore
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

								} else if (staticType == "sumByMaterial") {
									isPrompt = true;
									changeStatByMaterialResultWin.show();

									// ＃＃＃＃＃＃＃＃＃＃＃＃按食材統計＃＃＃＃＃＃＃＃＃＃＃＃
									Ext.Ajax
											.request({
												url : "../../InventoryChangeStatByMaterial.do",
												params : {
													"pin" : pin,
													"beginDate" : beginDate,
													"endDate" : endDate,
													"outDepartments" : outDepartments,
													"inDepartments" : inDepartments,
													"materials" : selectMaterials
												},
												success : function(response,
														options) {
													var resultJSON = Ext.util.JSON
															.decode(response.responseText);

													var rootData = resultJSON.root;
													if (rootData[0].message == "normal") {

														changeStatByMaterialResultData = rootData;
														changeStatByMateriaGrid
																.getStore()
																.loadData(
																		changeStatByMaterialResultData);
														if (rootData[0].materialID == "NO_DATA") {
															changeStatByMateriaGrid
																	.getStore()
																	.removeAll();
														}

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

								} else if (staticType == "sumByOutDept") {

									isPrompt = true;
									changeStatByOutDeptResultWin.show();

									// ＃＃＃＃＃＃＃＃＃＃＃＃按調出部門統計＃＃＃＃＃＃＃＃＃＃＃＃
									Ext.Ajax
											.request({
												url : "../../InventoryChangeStatByOutDept.do",
												params : {
													"pin" : pin,
													"beginDate" : beginDate,
													"endDate" : endDate,
													"outDepartments" : outDepartments,
													"inDepartments" : inDepartments,
													"materials" : selectMaterials
												},
												success : function(response,
														options) {
													var resultJSON = Ext.util.JSON
															.decode(response.responseText);

													var rootData = resultJSON.root;
													if (rootData[0].message == "normal") {

														changeStatByOutDeptResultData = rootData;
														changeStatByOutDeptGrid
																.getStore()
																.loadData(
																		changeStatByOutDeptResultData);
														if (rootData[0].materialID == "NO_DATA") {
															changeStatByOutDeptGrid
																	.getStore()
																	.removeAll();
														}

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

								} else if (staticType == "sumByInDept") {

									isPrompt = true;
									changeStatByInDeptResultWin.show();

									// ＃＃＃＃＃＃＃＃＃＃＃＃按調入部門統計＃＃＃＃＃＃＃＃＃＃＃＃
									Ext.Ajax
											.request({
												url : "../../InventoryChangeStatByInDept.do",
												params : {
													"pin" : pin,
													"beginDate" : beginDate,
													"endDate" : endDate,
													"outDepartments" : outDepartments,
													"inDepartments" : inDepartments,
													"materials" : selectMaterials
												},
												success : function(response,
														options) {
													var resultJSON = Ext.util.JSON
															.decode(response.responseText);

													var rootData = resultJSON.root;
													if (rootData[0].message == "normal") {

														changeStatByInDeptResultData = rootData;
														changeStatByInDeptGrid
																.getStore()
																.loadData(
																		changeStatByInDeptResultData);
														if (rootData[0].materialID == "NO_DATA") {
															changeStatByInDeptGrid
																	.getStore()
																	.removeAll();
														}

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

							} else {
								Ext.MessageBox.show({
									msg : "至少需要选择一个食材进行统计",
									width : 300,
									buttons : Ext.MessageBox.OK
								});
							}
						}
					}, {
						text : "取消",
						handler : function() {
							isPrompt = false;
							inventoryChangeStatWin.hide();
						}
					} ],
			listeners : {
				"show" : function(thiz) {

					inventoryChangeStatWin.findById(
							"materialChangeStatMultSelect").reset();
					inventoryChangeStatWin.findById("begDateChangeStat")
							.setValue("");
					inventoryChangeStatWin.findById("endDateChangeStat")
							.setValue("");

					// inventoryChangeStatMSDS.loadData(materialComboData);

					// 神技！動態改變form中component的label！！！
					inventoryChangeStatWin.findById("dept1ChengeOutStat").el
							.parent().parent().parent().first().dom.innerHTML = departmentData[0][1]
							+ ":";
					inventoryChangeStatWin.findById("dept2ChengeOutStat").el
							.parent().parent().parent().first().dom.innerHTML = departmentData[1][1]
							+ ":";
					inventoryChangeStatWin.findById("dept3ChengeOutStat").el
							.parent().parent().parent().first().dom.innerHTML = departmentData[2][1]
							+ ":";
					inventoryChangeStatWin.findById("dept4ChengeOutStat").el
							.parent().parent().parent().first().dom.innerHTML = departmentData[3][1]
							+ ":";
					inventoryChangeStatWin.findById("dept5ChengeOutStat").el
							.parent().parent().parent().first().dom.innerHTML = departmentData[4][1]
							+ ":";
					inventoryChangeStatWin.findById("dept6ChengeOutStat").el
							.parent().parent().parent().first().dom.innerHTML = departmentData[5][1]
							+ ":";
					inventoryChangeStatWin.findById("dept7ChengeOutStat").el
							.parent().parent().parent().first().dom.innerHTML = departmentData[6][1]
							+ ":";
					inventoryChangeStatWin.findById("dept8ChengeOutStat").el
							.parent().parent().parent().first().dom.innerHTML = departmentData[7][1]
							+ ":";
					inventoryChangeStatWin.findById("dept9ChengeOutStat").el
							.parent().parent().parent().first().dom.innerHTML = departmentData[8][1]
							+ ":";
					inventoryChangeStatWin.findById("dept10ChengeOutStat").el
							.parent().parent().parent().first().dom.innerHTML = departmentData[9][1]
							+ ":";

					inventoryChangeStatWin.findById("dept1ChengeOutStat")
							.setValue(false);
					inventoryChangeStatWin.findById("dept2ChengeOutStat")
							.setValue(false);
					inventoryChangeStatWin.findById("dept3ChengeOutStat")
							.setValue(false);
					inventoryChangeStatWin.findById("dept4ChengeOutStat")
							.setValue(false);
					inventoryChangeStatWin.findById("dept5ChengeOutStat")
							.setValue(false);
					inventoryChangeStatWin.findById("dept6ChengeOutStat")
							.setValue(false);
					inventoryChangeStatWin.findById("dept7ChengeOutStat")
							.setValue(false);
					inventoryChangeStatWin.findById("dept8ChengeOutStat")
							.setValue(false);
					inventoryChangeStatWin.findById("dept9ChengeOutStat")
							.setValue(false);
					inventoryChangeStatWin.findById("dept10ChengeOutStat")
							.setValue(false);

					inventoryChangeStatWin.findById("dept1ChengeInStat").el
							.parent().parent().parent().first().dom.innerHTML = departmentData[0][1]
							+ ":";
					inventoryChangeStatWin.findById("dept2ChengeInStat").el
							.parent().parent().parent().first().dom.innerHTML = departmentData[1][1]
							+ ":";
					inventoryChangeStatWin.findById("dept3ChengeInStat").el
							.parent().parent().parent().first().dom.innerHTML = departmentData[2][1]
							+ ":";
					inventoryChangeStatWin.findById("dept4ChengeInStat").el
							.parent().parent().parent().first().dom.innerHTML = departmentData[3][1]
							+ ":";
					inventoryChangeStatWin.findById("dept5ChengeInStat").el
							.parent().parent().parent().first().dom.innerHTML = departmentData[4][1]
							+ ":";
					inventoryChangeStatWin.findById("dept6ChengeInStat").el
							.parent().parent().parent().first().dom.innerHTML = departmentData[5][1]
							+ ":";
					inventoryChangeStatWin.findById("dept7ChengeInStat").el
							.parent().parent().parent().first().dom.innerHTML = departmentData[6][1]
							+ ":";
					inventoryChangeStatWin.findById("dept8ChengeInStat").el
							.parent().parent().parent().first().dom.innerHTML = departmentData[7][1]
							+ ":";
					inventoryChangeStatWin.findById("dept9ChengeInStat").el
							.parent().parent().parent().first().dom.innerHTML = departmentData[8][1]
							+ ":";
					inventoryChangeStatWin.findById("dept10ChengeInStat").el
							.parent().parent().parent().first().dom.innerHTML = departmentData[9][1]
							+ ":";

					inventoryChangeStatWin.findById("dept1ChengeInStat")
							.setValue(false);
					inventoryChangeStatWin.findById("dept2ChengeInStat")
							.setValue(false);
					inventoryChangeStatWin.findById("dept3ChengeInStat")
							.setValue(false);
					inventoryChangeStatWin.findById("dept4ChengeInStat")
							.setValue(false);
					inventoryChangeStatWin.findById("dept5ChengeInStat")
							.setValue(false);
					inventoryChangeStatWin.findById("dept6ChengeInStat")
							.setValue(false);
					inventoryChangeStatWin.findById("dept7ChengeInStat")
							.setValue(false);
					inventoryChangeStatWin.findById("dept8ChengeInStat")
							.setValue(false);
					inventoryChangeStatWin.findById("dept9ChengeInStat")
							.setValue(false);
					inventoryChangeStatWin.findById("dept10ChengeInStat")
							.setValue(false);

					inventoryChangeStatFrom.getForm().findField("ChangeStat")
							.setValue("detail");

				},
				"hide" : function(thiz) {
					isPrompt = false;
				}
			}
		});

// 结果框 -- 明細
// 前台：[食材 日期 调出部门 调入部门 经手人 部门 价格 数量 小计]
var changeStatDetailResultStore = new Ext.data.Store({
	proxy : new Ext.data.MemoryProxy(changeStatDetailResultData),
	reader : new Ext.data.ArrayReader({}, [ {
		name : "materialID"
	}, {
		name : "materialName"
	}, {
		name : "date"
	}, {
		name : "outDeptID"
	}, {
		name : "outDeptName"
	}, {
		name : "inDeptID"
	}, {
		name : "inDeptName"
	}, {
		name : "operator"
	}, {
		name : "price"
	}, {
		name : "amount"
	}, {
		name : "total"
	}, {
		name : "message"
	} ])
});

// 2，栏位模型
var changeStatDetailResultColumnModel = new Ext.grid.ColumnModel([
		new Ext.grid.RowNumberer(), {
			header : "食材",
			sortable : true,
			dataIndex : "materialName",
			width : 80
		}, {
			header : "日期",
			sortable : true,
			dataIndex : "date",
			width : 80
		}, {
			header : "调出部门",
			sortable : true,
			dataIndex : "outDeptName",
			width : 80
		}, {
			header : "调入部门",
			sortable : true,
			dataIndex : "inDeptName",
			width : 80
		}, {
			header : "经手人",
			sortable : true,
			dataIndex : "operator",
			width : 80
		}, {
			header : "价钱",
			sortable : true,
			dataIndex : "price",
			width : 80
		}, {
			header : "数量",
			sortable : true,
			dataIndex : "amount",
			width : 80
		}, {
			header : "小计（￥）",
			sortable : true,
			dataIndex : "total",
			width : 80
		} ]);

var changeStatDetailResultGrid = new Ext.grid.GridPanel({
	xtype : "grid",
	anchor : "99%",
	border : false,
	ds : changeStatDetailResultStore,
	cm : changeStatDetailResultColumnModel,
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

var changeStatDetailResultWin = new Ext.Window({
	title : "调拨明细",
	width : 800,
	height : 370,
	closeAction : "hide",
	resizable : false,
	layout : "fit",
	items : changeStatDetailResultGrid,
	buttons : [ {
		text : "打印",
		handler : function() {

		}
	}, {
		text : "关闭",
		handler : function() {
			isPrompt = false;
			changeStatDetailResultWin.hide();
		}
	} ],
	listeners : {
		"hide" : function(thiz) {
			isPrompt = false;
		}
	}
});

// 结果框 -- 按食材
// --------------------------------------------------------------------------------------------------------
var changeStatByMateriaReader = new Ext.data.JsonReader({
	idProperty : 'groupID',
	fields : [ {
		name : 'materialID',
		type : 'int'
	}, {
		name : 'materialName',
		type : 'string'
	}, {
		name : 'groupID',
		type : 'int'
	}, {
		name : 'groupDescr',
		type : 'string'
	}, {
		name : 'singlePrice',
		type : 'float'
	}, {
		name : 'amount',
		type : 'float'
	}, {
		name : 'sumPrice',
		type : 'float'
	}, {
		name : 'outDeptID'
	}, {
		name : 'outDeptName',
		type : 'string'
	}, {
		name : 'inDeptID'
	}, {
		name : 'inDeptName',
		type : 'string'
	} ]

});

var changeStatByMateriaSummary = new Ext.grid.GroupSummary();

var changeStatByMateriaGrid = new Ext.grid.EditorGridPanel({
	ds : new Ext.data.GroupingStore({
		reader : changeStatByMateriaReader,
		data : [],
		sortInfo : {
			field : 'outDeptName',
			direction : "ASC"
		},
		groupField : 'materialName'
	}),

	columns : [ {
		id : 'groupDescr',
		header : "食材",
		width : 20,
		sortable : true,
		dataIndex : 'groupDescr',
		// summaryType : 'count',
		hideable : false,
		// summaryRenderer : function(v, params, data) {
		// return ((v === 0 || v > 1) ? '(' + v + ' Tasks)' : '(1 Task)');
		// }
		summaryRenderer : function(v, params, data) {
			return "合计";
		}
	}, {
		header : "NOT SHOW",
		width : 20,
		sortable : false,
		dataIndex : 'materialName'
	}, {
		header : "调出部门",
		width : 25,
		sortable : true,
		dataIndex : 'outDeptName'
	}, {
		header : "调入部门",
		width : 25,
		sortable : true,
		dataIndex : 'inDeptName'
	}, {
		id : 'singlePrice',
		header : "价格",
		width : 20,
		sortable : false, // 計出來的列排序不起作用
		groupable : false,
		renderer : function(v, params, record) {
			// return Ext.util.Format.usMoney(record.data.sumPrice
			// / record.data.amount);
			if (record.data.amount != 0) {
				return (record.data.sumPrice / record.data.amount).toFixed(2);
			} else {
				return 0.00;
			}
		},
		dataIndex : 'singlePrice',
		summaryType : 'singlePrice'
	// summaryRenderer : Ext.util.Format.usMoney
	}, {
		header : "数量",
		width : 20,
		sortable : true,
		// renderer : Ext.util.Format.usMoney,
		dataIndex : 'amount',
		summaryType : 'sum'
	// editor: new Ext.form.NumberField({
	// allowBlank: false,
	// allowNegative: false,
	// style: 'text-align:left'
	// })
	}, {
		header : "小计",
		width : 20,
		sortable : true,
		// renderer : Ext.util.Format.chnMoney,
		dataIndex : 'sumPrice',
		summaryType : 'sum',
		renderer : function(v) {
			return parseFloat(v).toFixed(2);
		}
	// editor : new Ext.form.NumberField({
	// allowBlank : false,
	// allowNegative : false,
	// style : 'text-align:left'
	// })
	} ],

	view : new Ext.grid.GroupingView({
		forceFit : true,
		showGroupName : false,
		enableNoGroups : false, // REQUIRED!
		hideGroupedColumn : true
	}),

	plugins : changeStatByMateriaSummary,

	frame : true,
	width : 800,
	height : 450,
	clicksToEdit : 1,
	collapsible : true,
	animCollapse : false,
	trackMouseOver : false
// enableColumnMove: false,
// title : 'Sponsored Projects'
// iconCls: 'icon-grid',
// renderTo: document.body
});

// --------------------------------------------------------------------------------------------------------

var changeStatByMaterialResultWin = new Ext.Window({
	title : "调拨汇总-按食材",
	width : 400,
	height : 500,
	closeAction : "hide",
	resizable : false,
	layout : "fit",
	items : changeStatByMateriaGrid,
	buttons : [ {
		text : "打印",
		handler : function() {

		}
	}, {
		text : "关闭",
		handler : function() {
			isPrompt = false;
			changeStatByMaterialResultWin.hide();
		}
	} ],
	listeners : {
		"hide" : function(thiz) {
			isPrompt = false;
		}
	}
});

// 结果框 -- 按調出部門
// --------------------------------------------------------------------------------------------------------
var changeStatByOutDeptReader = new Ext.data.JsonReader({
	idProperty : 'groupID',
	fields : [ {
		name : 'outDeptID',
		type : 'int'
	}, {
		name : 'outDeptName',
		type : 'string'
	}, {
		name : 'groupID',
		type : 'int'
	}, {
		name : 'groupDescr',
		type : 'string'
	}, {
		name : 'singlePrice',
		type : 'float'
	}, {
		name : 'amount',
		type : 'float'
	}, {
		name : 'sumPrice',
		type : 'float'
	}, {
		name : 'materialID'
	}, {
		name : 'materialName',
		type : 'string'
	} ]

});

var changeStatByOutDeptSummary = new Ext.grid.GroupSummary();

var changeStatByOutDeptGrid = new Ext.grid.EditorGridPanel({
	ds : new Ext.data.GroupingStore({
		reader : changeStatByOutDeptReader,
		data : [],
		sortInfo : {
			field : 'materialName',
			direction : "ASC"
		},
		groupField : 'outDeptName'
	}),

	columns : [ {
		id : 'groupDescr',
		header : "调出部门",
		width : 20,
		sortable : true,
		dataIndex : 'groupDescr',
		// summaryType : 'count',
		hideable : false,
		// summaryRenderer : function(v, params, data) {
		// return ((v === 0 || v > 1) ? '(' + v + ' Tasks)' : '(1 Task)');
		// }
		summaryRenderer : function(v, params, data) {
			return "合计";
		}
	}, {
		header : "NOT SHOW",
		width : 20,
		sortable : false,
		dataIndex : 'outDeptName'
	}, {
		header : "食材",
		width : 25,
		sortable : true,
		dataIndex : 'materialName'
	}, {
		id : 'singlePrice',
		header : "价格",
		width : 20,
		sortable : false, // 計出來的列排序不起作用
		groupable : false,
		renderer : function(v, params, record) {
			// return Ext.util.Format.usMoney(record.data.sumPrice
			// / record.data.amount);
			if (record.data.amount != 0) {
				return (record.data.sumPrice / record.data.amount).toFixed(2);
			} else {
				return 0.00;
			}
		},
		dataIndex : 'singlePrice',
		summaryType : 'singlePrice'
	// summaryRenderer : Ext.util.Format.usMoney
	}, {
		header : "数量",
		width : 20,
		sortable : true,
		// renderer : Ext.util.Format.usMoney,
		dataIndex : 'amount',
		summaryType : 'sum'
	// editor: new Ext.form.NumberField({
	// allowBlank: false,
	// allowNegative: false,
	// style: 'text-align:left'
	// })
	}, {
		header : "小计",
		width : 20,
		sortable : true,
		// renderer : Ext.util.Format.chnMoney,
		dataIndex : 'sumPrice',
		summaryType : 'sum',
		renderer : function(v) {
			return parseFloat(v).toFixed(2);
		}
	// editor : new Ext.form.NumberField({
	// allowBlank : false,
	// allowNegative : false,
	// style : 'text-align:left'
	// })
	} ],

	view : new Ext.grid.GroupingView({
		forceFit : true,
		showGroupName : false,
		enableNoGroups : false, // REQUIRED!
		hideGroupedColumn : true
	}),

	plugins : changeStatByOutDeptSummary,

	frame : true,
	width : 800,
	height : 450,
	clicksToEdit : 1,
	collapsible : true,
	animCollapse : false,
	trackMouseOver : false
// enableColumnMove: false,
// title : 'Sponsored Projects'
// iconCls: 'icon-grid',
// renderTo: document.body
});

// --------------------------------------------------------------------------------------------------------

var changeStatByOutDeptResultWin = new Ext.Window({
	title : "调拨汇总-按调出部门",
	width : 400,
	height : 500,
	closeAction : "hide",
	resizable : false,
	layout : "fit",
	items : changeStatByOutDeptGrid,
	buttons : [ {
		text : "打印",
		handler : function() {

		}
	}, {
		text : "关闭",
		handler : function() {
			isPrompt = false;
			changeStatByOutDeptResultWin.hide();
		}
	} ],
	listeners : {
		"hide" : function(thiz) {
			isPrompt = false;
		}
	}
});

// 结果框 -- 按調入部門
// --------------------------------------------------------------------------------------------------------
var changeStatByInDeptReader = new Ext.data.JsonReader({
	idProperty : 'groupID',
	fields : [ {
		name : 'inDeptID',
		type : 'int'
	}, {
		name : 'inDeptName',
		type : 'string'
	}, {
		name : 'groupID',
		type : 'int'
	}, {
		name : 'groupDescr',
		type : 'string'
	}, {
		name : 'singlePrice',
		type : 'float'
	}, {
		name : 'amount',
		type : 'float'
	}, {
		name : 'sumPrice',
		type : 'float'
	}, {
		name : 'materialID'
	}, {
		name : 'materialName',
		type : 'string'
	} ]

});

var changeStatByInDeptSummary = new Ext.grid.GroupSummary();

var changeStatByInDeptGrid = new Ext.grid.EditorGridPanel({
	ds : new Ext.data.GroupingStore({
		reader : changeStatByInDeptReader,
		data : [],
		sortInfo : {
			field : 'materialName',
			direction : "ASC"
		},
		groupField : 'inDeptName'
	}),

	columns : [ {
		id : 'groupDescr',
		header : "调入部门",
		width : 20,
		sortable : true,
		dataIndex : 'groupDescr',
		// summaryType : 'count',
		hideable : false,
		// summaryRenderer : function(v, params, data) {
		// return ((v === 0 || v > 1) ? '(' + v + ' Tasks)' : '(1 Task)');
		// }
		summaryRenderer : function(v, params, data) {
			return "合计";
		}
	}, {
		header : "NOT SHOW",
		width : 20,
		sortable : false,
		dataIndex : 'inDeptName'
	}, {
		header : "食材",
		width : 25,
		sortable : true,
		dataIndex : 'materialName'
	}, {
		id : 'singlePrice',
		header : "价格",
		width : 20,
		sortable : false, // 計出來的列排序不起作用
		groupable : false,
		renderer : function(v, params, record) {
			// return Ext.util.Format.usMoney(record.data.sumPrice
			// / record.data.amount);
			if (record.data.amount != 0) {
				return (record.data.sumPrice / record.data.amount).toFixed(2);
			} else {
				return 0.00;
			}
		},
		dataIndex : 'singlePrice',
		summaryType : 'singlePrice'
	// summaryRenderer : Ext.util.Format.usMoney
	}, {
		header : "数量",
		width : 20,
		sortable : true,
		// renderer : Ext.util.Format.usMoney,
		dataIndex : 'amount',
		summaryType : 'sum'
	// editor: new Ext.form.NumberField({
	// allowBlank: false,
	// allowNegative: false,
	// style: 'text-align:left'
	// })
	}, {
		header : "小计",
		width : 20,
		sortable : true,
		// renderer : Ext.util.Format.chnMoney,
		dataIndex : 'sumPrice',
		summaryType : 'sum',
		renderer : function(v) {
			return parseFloat(v).toFixed(2);
		}
	// editor : new Ext.form.NumberField({
	// allowBlank : false,
	// allowNegative : false,
	// style : 'text-align:left'
	// })
	} ],

	view : new Ext.grid.GroupingView({
		forceFit : true,
		showGroupName : false,
		enableNoGroups : false, // REQUIRED!
		hideGroupedColumn : true
	}),

	plugins : changeStatByInDeptSummary,

	frame : true,
	width : 800,
	height : 450,
	clicksToEdit : 1,
	collapsible : true,
	animCollapse : false,
	trackMouseOver : false
// enableColumnMove: false,
// title : 'Sponsored Projects'
// iconCls: 'icon-grid',
// renderTo: document.body
});

// --------------------------------------------------------------------------------------------------------

var changeStatByInDeptResultWin = new Ext.Window({
	title : "调拨汇总-按调入部门",
	width : 400,
	height : 500,
	closeAction : "hide",
	resizable : false,
	layout : "fit",
	items : changeStatByInDeptGrid,
	buttons : [ {
		text : "打印",
		handler : function() {

		}
	}, {
		text : "关闭",
		handler : function() {
			isPrompt = false;
			changeStatByInDeptResultWin.hide();
		}
	} ],
	listeners : {
		"hide" : function(thiz) {
			isPrompt = false;
		}
	}
});
// --------------------------------------end调拨統計----------------------------------------------
// ---------------------------------------消耗統計--------------------------------------------------------
// 条件框
var inventoryCostStatMSDS = new Ext.data.SimpleStore({
	fields : [ "retrunValue", "displayText" ],
	data : []
});

var inventoryCostStatFrom = new Ext.form.FormPanel({
	border : false,
	anchor : "right 8%",
	id : "CostStatForm",
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
				name : 'CostStat',
				inputValue : 'detail'
			} ]
		}, {
			layout : "form",
			border : false,
			labelSeparator : '',
			width : 110,
			items : [ {
				xtype : 'radio',
				hideLabel : true,
				boxLabel : "按食材汇总",
				// checked : true,
				name : 'CostStat',
				inputValue : 'sumByMaterial'
			} ]
		}, {
			layout : "form",
			border : false,
			labelSeparator : '',
			width : 110,
			items : [ {
				xtype : 'radio',
				hideLabel : true,
				boxLabel : "按部门汇总",
				// checked : true,
				name : 'CostStat',
				inputValue : 'sumByDept'
			} ]
		} ]
	} ]
});

inventoryCostStatWin = new Ext.Window(
		{
			title : "消耗统计",
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
							xtype : "datefield",
							id : "begDateCostStat",
							width : 120,
							fieldLabel : "日期"
						} ]
					}, {
						layout : "form",
						border : false,
						labelSeparator : ' ',
						width : 200,
						labelWidth : 50,
						items : [ {
							xtype : "datefield",
							id : "endDateCostStat",
							width : 120,
							fieldLabel : "至"
						} ]
					} ]
				} ]
			}, {
				layout : "column",
				border : false,
				anchor : "99% 15%",
				autoScroll : true,
				frame : true,
				items : [ {
					layout : "form",
					border : false,
					labelSeparator : '',
					width : 80,
					labelWidth : 40,
					items : [ {
						xtype : "checkbox",
						id : "dept1CostStat"
					// fieldLabel : departmentData[0][1]
					} ]
				}, {
					layout : "form",
					border : false,
					labelSeparator : '',
					width : 80,
					labelWidth : 40,
					items : [ {
						xtype : "checkbox",
						id : "dept2CostStat"
					// fieldLabel : departmentData[1][1]
					} ]
				}, {
					layout : "form",
					border : false,
					labelSeparator : '',
					width : 80,
					labelWidth : 40,
					items : [ {
						xtype : "checkbox",
						id : "dept3CostStat"
					// fieldLabel : departmentData[2][1]
					} ]
				}, {
					layout : "form",
					border : false,
					labelSeparator : '',
					width : 80,
					labelWidth : 40,
					items : [ {
						xtype : "checkbox",
						id : "dept4CostStat"
					// fieldLabel : departmentData[3][1]
					} ]
				}, {
					layout : "form",
					border : false,
					labelSeparator : '',
					width : 80,
					labelWidth : 40,
					items : [ {
						xtype : "checkbox",
						id : "dept5CostStat"
					// fieldLabel : departmentData[4][1]
					} ]
				}, {
					layout : "form",
					border : false,
					labelSeparator : '',
					width : 80,
					labelWidth : 40,
					items : [ {
						xtype : "checkbox",
						id : "dept6CostStat"
					// fieldLabel : departmentData[5][1]
					} ]
				}, {
					layout : "form",
					border : false,
					labelSeparator : '',
					width : 80,
					labelWidth : 40,
					items : [ {
						xtype : "checkbox",
						id : "dept7CostStat"
					// fieldLabel : departmentData[6][1]
					} ]
				}, {
					layout : "form",
					border : false,
					labelSeparator : '',
					width : 80,
					labelWidth : 40,
					items : [ {
						xtype : "checkbox",
						id : "dept8CostStat"
					// fieldLabel : departmentData[7][1]
					} ]
				}, {
					layout : "form",
					border : false,
					labelSeparator : '',
					width : 80,
					labelWidth : 40,
					items : [ {
						xtype : "checkbox",
						id : "dept9CostStat"
					// fieldLabel : departmentData[8][1]
					} ]
				}, {
					layout : "form",
					border : false,
					labelSeparator : '',
					width : 80,
					labelWidth : 40,
					items : [ {
						xtype : "checkbox",
						id : "dept10CostStat"
					// fieldLabel : departmentData[9][1]
					} ]
				} ]
			}, {
				layout : "form",
				border : false,
				frame : true,
				hideLabels : true,
				anchor : "right 67%",
				items : [ {
					xtype : "itemselector",
					name : "materialCostStatMultSelect",
					id : "materialCostStatMultSelect",
					fromStore : inventoryCostStatMSDS,
					dataFields : [ "retrunValue", "displayText" ],
					toData : [ [ "", "" ] ],
					msHeight : 215,
					valueField : "retrunValue",
					displayField : "displayText",
					imagePath : "../../extjs/multiselect/images/",
					toLegend : "已选择食材",
					fromLegend : "可选择食材"
				} ]
			}, inventoryCostStatFrom ],
			buttons : [
					{
						text : "全选",
						handler : function() {
							var i = inventoryCostStatWin
									.findById("materialCostStatMultSelect").fromMultiselect.view;
							i.selectRange(0, i.store.length);
							inventoryCostStatWin.findById(
									"materialCostStatMultSelect").fromTo();
							inventoryCostStatWin
									.findById("materialCostStatMultSelect").toMultiselect.view
									.clearSelections();
						}
					},
					{
						text : "清空",
						handler : function() {
							inventoryCostStatWin.findById(
									"materialCostStatMultSelect").reset();
						}
					},
					{
						text : "确定",
						handler : function() {

							var selectCount = inventoryCostStatWin
									.findById("materialCostStatMultSelect").toMultiselect.store
									.getCount();

							if (selectCount != 0) {
								isPrompt = false;
								inventoryCostStatWin.hide();

								// -- 獲取選擇的食材 --
								var selectMaterials = "";
								for ( var i = 0; i < selectCount; i++) {
									var selectItem = inventoryCostStatWin
											.findById("materialCostStatMultSelect").toMultiselect.store
											.getAt(i).get("retrunValue");
									// if (selectItem != "") {
									selectMaterials = selectMaterials
											+ selectItem + ",";
									// }
								}
								// 去掉最后一个逗号
								selectMaterials = selectMaterials.substring(0,
										selectMaterials.length - 1);
								if (selectMaterials.substring(0, 1) == ",") {
									selectMaterials = selectMaterials
											.substring(1,
													selectMaterials.length);
								}

								// -- 獲取時間 --
								var beginDate = inventoryCostStatWin.findById(
										"begDateCostStat").getValue();
								if (beginDate != "") {
									var dateFormated = new Date();
									dateFormated = beginDate;
									beginDate = dateFormated.format('Y-m-d');
								}

								var endDate = inventoryCostStatWin.findById(
										"endDateCostStat").getValue();
								if (endDate != "") {
									var dateFormated = new Date();
									dateFormated = endDate;
									endDate = dateFormated.format('Y-m-d');
								}

								// -- 獲取部門 --
								var departments = "";
								if (inventoryCostStatWin.findById(
										"dept1CostStat").getValue() == true) {
									departments = departments + "0,";
								}
								if (inventoryCostStatWin.findById(
										"dept2CostStat").getValue() == true) {
									departments = departments + "1,";
								}
								if (inventoryCostStatWin.findById(
										"dept3CostStat").getValue() == true) {
									departments = departments + "2,";
								}
								if (inventoryCostStatWin.findById(
										"dept4CostStat").getValue() == true) {
									departments = departments + "3,";
								}
								if (inventoryCostStatWin.findById(
										"dept5CostStat").getValue() == true) {
									departments = departments + "4,";
								}
								if (inventoryCostStatWin.findById(
										"dept6CostStat").getValue() == true) {
									departments = departments + "5,";
								}
								if (inventoryCostStatWin.findById(
										"dept7CostStat").getValue() == true) {
									departments = departments + "6,";
								}
								if (inventoryCostStatWin.findById(
										"dept8CostStat").getValue() == true) {
									departments = departments + "7,";
								}
								if (inventoryCostStatWin.findById(
										"dept9CostStat").getValue() == true) {
									departments = departments + "8,";
								}
								if (inventoryCostStatWin.findById(
										"dept10CostStat").getValue() == true) {
									departments = departments + "9,";
								}

								if (departments != "") {
									departments = departments.substring(0,
											departments.length - 1);
								}

								// -- 獲取統計類型 --
								var staticType = inventoryCostStatFrom
										.getForm().findField("CostStat")
										.getGroupValue();
								if (staticType == "detail") {
									isPrompt = true;
									costStatDetailResultWin.show();

									// ＃＃＃＃＃＃＃＃＃＃＃＃明細統計＃＃＃＃＃＃＃＃＃＃＃＃
									Ext.Ajax
											.request({
												url : "../../InventoryCostStatDetail.do",
												params : {
													"pin" : pin,
													"beginDate" : beginDate,
													"endDate" : endDate,
													"departments" : departments,
													"materials" : selectMaterials
												},
												success : function(response,
														options) {
													var resultJSON = Ext.util.JSON
															.decode(response.responseText);

													// 格式：[食材id，日期，DeptId，Dept，價格，數量，小計]
													var rootData = resultJSON.root;
													if (rootData[0].message == "normal") {
														costStatDetailResultData.length = 0;
														for ( var i = 0; i < rootData.length; i++) {
															var materialN = "";
															for ( var j = 0; j < materialData.length; j++) {
																if (materialData[j][0] == rootData[i].materialID) {
																	materialN = materialData[j][2];
																}
															}

															var deptN = "";
															for ( var j = 0; j < departmentData.length; j++) {
																if (departmentData[j][0] == rootData[i].deptID) {
																	deptN = departmentData[j][1];
																}
															}

															costStatDetailResultData
																	.push([
																			rootData[i].materialID,
																			materialN,
																			rootData[i].date,
																			rootData[i].deptID,
																			deptN,
																			rootData[i].price,
																			rootData[i].amount,
																			rootData[i].total

																	]);
														}

														costStatDetailResultStore
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

								} else if (staticType == "sumByMaterial") {
									isPrompt = true;
									costStatByMaterialResultWin.show();

									// ＃＃＃＃＃＃＃＃＃＃＃＃按食材統計＃＃＃＃＃＃＃＃＃＃＃＃
									Ext.Ajax
											.request({
												url : "../../InventoryCostStatByMaterial.do",
												params : {
													"pin" : pin,
													"beginDate" : beginDate,
													"endDate" : endDate,
													"departments" : departments,
													"materials" : selectMaterials
												},
												success : function(response,
														options) {
													var resultJSON = Ext.util.JSON
															.decode(response.responseText);

													var rootData = resultJSON.root;
													if (rootData[0].message == "normal") {

														costStatByMaterialResultData = rootData;
														costStatByMateriaGrid
																.getStore()
																.loadData(
																		costStatByMaterialResultData);
														if (rootData[0].materialID == "NO_DATA") {
															costStatByMateriaGrid
																	.getStore()
																	.removeAll();
														}

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

								} else if (staticType == "sumByDept") {

									isPrompt = true;
									costStatByDeptResultWin.show();

									// ＃＃＃＃＃＃＃＃＃＃＃＃按部門統計＃＃＃＃＃＃＃＃＃＃＃＃
									Ext.Ajax
											.request({
												url : "../../InventoryCostStatByDept.do",
												params : {
													"pin" : pin,
													"beginDate" : beginDate,
													"endDate" : endDate,
													"departments" : departments,
													"materials" : selectMaterials
												},
												success : function(response,
														options) {
													var resultJSON = Ext.util.JSON
															.decode(response.responseText);

													var rootData = resultJSON.root;
													if (rootData[0].message == "normal") {

														costStatByDeptResultData = rootData;
														costStatByDeptGrid
																.getStore()
																.loadData(
																		costStatByDeptResultData);
														if (rootData[0].materialID == "NO_DATA") {
															costStatByDeptGrid
																	.getStore()
																	.removeAll();
														}

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

							} else {
								Ext.MessageBox.show({
									msg : "至少需要选择一个食材进行统计",
									width : 300,
									buttons : Ext.MessageBox.OK
								});
							}
						}
					}, {
						text : "取消",
						handler : function() {
							isPrompt = false;
							inventoryCostStatWin.hide();
						}
					} ],
			listeners : {
				"show" : function(thiz) {

					inventoryCostStatWin.findById("materialCostStatMultSelect")
							.reset();
					inventoryCostStatWin.findById("begDateCostStat").setValue(
							"");
					inventoryCostStatWin.findById("endDateCostStat").setValue(
							"");

					// inventoryCostStatMSDS.loadData(materialComboData);

					// 神技！動態改變form中component的label！！！
					inventoryCostStatWin.findById("dept1CostStat").el.parent()
							.parent().parent().first().dom.innerHTML = departmentData[0][1]
							+ ":";
					inventoryCostStatWin.findById("dept2CostStat").el.parent()
							.parent().parent().first().dom.innerHTML = departmentData[1][1]
							+ ":";
					inventoryCostStatWin.findById("dept3CostStat").el.parent()
							.parent().parent().first().dom.innerHTML = departmentData[2][1]
							+ ":";
					inventoryCostStatWin.findById("dept4CostStat").el.parent()
							.parent().parent().first().dom.innerHTML = departmentData[3][1]
							+ ":";
					inventoryCostStatWin.findById("dept5CostStat").el.parent()
							.parent().parent().first().dom.innerHTML = departmentData[4][1]
							+ ":";
					inventoryCostStatWin.findById("dept6CostStat").el.parent()
							.parent().parent().first().dom.innerHTML = departmentData[5][1]
							+ ":";
					inventoryCostStatWin.findById("dept7CostStat").el.parent()
							.parent().parent().first().dom.innerHTML = departmentData[6][1]
							+ ":";
					inventoryCostStatWin.findById("dept8CostStat").el.parent()
							.parent().parent().first().dom.innerHTML = departmentData[7][1]
							+ ":";
					inventoryCostStatWin.findById("dept9CostStat").el.parent()
							.parent().parent().first().dom.innerHTML = departmentData[8][1]
							+ ":";
					inventoryCostStatWin.findById("dept10CostStat").el.parent()
							.parent().parent().first().dom.innerHTML = departmentData[9][1]
							+ ":";

					inventoryCostStatWin.findById("dept1CostStat").setValue(
							false);
					inventoryCostStatWin.findById("dept2CostStat").setValue(
							false);
					inventoryCostStatWin.findById("dept3CostStat").setValue(
							false);
					inventoryCostStatWin.findById("dept4CostStat").setValue(
							false);
					inventoryCostStatWin.findById("dept5CostStat").setValue(
							false);
					inventoryCostStatWin.findById("dept6CostStat").setValue(
							false);
					inventoryCostStatWin.findById("dept7CostStat").setValue(
							false);
					inventoryCostStatWin.findById("dept8CostStat").setValue(
							false);
					inventoryCostStatWin.findById("dept9CostStat").setValue(
							false);
					inventoryCostStatWin.findById("dept10CostStat").setValue(
							false);

					inventoryCostStatFrom.getForm().findField("CostStat")
							.setValue("detail");

				},
				"hide" : function(thiz) {
					isPrompt = false;
				}
			}
		});

// 结果框 -- 明細
// 前台：[食材 日期 部门 价格 数量 小计]
var costStatDetailResultStore = new Ext.data.Store({
	proxy : new Ext.data.MemoryProxy(costStatDetailResultData),
	reader : new Ext.data.ArrayReader({}, [ {
		name : "materialID"
	}, {
		name : "materialName"
	}, {
		name : "date"
	}, {
		name : "deptID"
	}, {
		name : "deptName"
	}, {
		name : "price"
	}, {
		name : "amount"
	}, {
		name : "total"
	}, {
		name : "message"
	} ])
});

// 2，栏位模型
var costStatDetailResultColumnModel = new Ext.grid.ColumnModel([
		new Ext.grid.RowNumberer(), {
			header : "食材",
			sortable : true,
			dataIndex : "materialName",
			width : 80
		}, {
			header : "日期",
			sortable : true,
			dataIndex : "date",
			width : 80
		}, {
			header : "部门",
			sortable : true,
			dataIndex : "deptName",
			width : 80
		}, {
			header : "价钱",
			sortable : true,
			dataIndex : "price",
			width : 80
		}, {
			header : "数量",
			sortable : true,
			dataIndex : "amount",
			width : 80
		}, {
			header : "小计（￥）",
			sortable : true,
			dataIndex : "total",
			width : 80
		} ]);

var costStatDetailResultGrid = new Ext.grid.GridPanel({
	xtype : "grid",
	anchor : "99%",
	border : false,
	ds : costStatDetailResultStore,
	cm : costStatDetailResultColumnModel,
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

var costStatDetailResultWin = new Ext.Window({
	title : "消耗明细",
	width : 800,
	height : 370,
	closeAction : "hide",
	resizable : false,
	layout : "fit",
	items : costStatDetailResultGrid,
	buttons : [ {
		text : "打印",
		handler : function() {

		}
	}, {
		text : "关闭",
		handler : function() {
			isPrompt = false;
			costStatDetailResultWin.hide();
		}
	} ],
	listeners : {
		"hide" : function(thiz) {
			isPrompt = false;
		}
	}
});

// 结果框 -- 按食材
// --------------------------------------------------------------------------------------------------------
var costStatByMateriaReader = new Ext.data.JsonReader({
	idProperty : 'groupID',
	fields : [ {
		name : 'materialID',
		type : 'int'
	}, {
		name : 'materialName',
		type : 'string'
	}, {
		name : 'groupID',
		type : 'int'
	}, {
		name : 'groupDescr',
		type : 'string'
	}, {
		name : 'singlePrice',
		type : 'float'
	}, {
		name : 'amount',
		type : 'float'
	}, {
		name : 'sumPrice',
		type : 'float'
	}, {
		name : 'deptID'
	}, {
		name : 'deptName',
		type : 'string'
	} ]

});

var costStatByMateriaSummary = new Ext.grid.GroupSummary();

var costStatByMateriaGrid = new Ext.grid.EditorGridPanel({
	ds : new Ext.data.GroupingStore({
		reader : costStatByMateriaReader,
		data : [],
		sortInfo : {
			field : 'deptName',
			direction : "ASC"
		},
		groupField : 'materialName'
	}),

	columns : [ {
		id : 'groupDescr',
		header : "食材",
		width : 20,
		sortable : true,
		dataIndex : 'groupDescr',
		// summaryType : 'count',
		hideable : false,
		// summaryRenderer : function(v, params, data) {
		// return ((v === 0 || v > 1) ? '(' + v + ' Tasks)' : '(1 Task)');
		// }
		summaryRenderer : function(v, params, data) {
			return "合计";
		}
	}, {
		header : "NOT SHOW",
		width : 20,
		sortable : false,
		dataIndex : 'materialName'
	}, {
		header : "部门",
		width : 25,
		sortable : true,
		dataIndex : 'deptName'
	}, {
		id : 'singlePrice',
		header : "价格",
		width : 20,
		sortable : false, // 計出來的列排序不起作用
		groupable : false,
		renderer : function(v, params, record) {
			// return Ext.util.Format.usMoney(record.data.sumPrice
			// / record.data.amount);
			if (record.data.amount != 0) {
				return (record.data.sumPrice / record.data.amount).toFixed(2);
			} else {
				return 0.00;
			}
		},
		dataIndex : 'singlePrice',
		summaryType : 'singlePrice'
	// summaryRenderer : Ext.util.Format.usMoney
	}, {
		header : "数量",
		width : 20,
		sortable : true,
		// renderer : Ext.util.Format.usMoney,
		dataIndex : 'amount',
		summaryType : 'sum'
	// editor: new Ext.form.NumberField({
	// allowBlank: false,
	// allowNegative: false,
	// style: 'text-align:left'
	// })
	}, {
		header : "小计",
		width : 20,
		sortable : true,
		// renderer : Ext.util.Format.chnMoney,
		dataIndex : 'sumPrice',
		summaryType : 'sum',
		renderer : function(v) {
			return parseFloat(v).toFixed(2);
		}
	// editor : new Ext.form.NumberField({
	// allowBlank : false,
	// allowNegative : false,
	// style : 'text-align:left'
	// })
	} ],

	view : new Ext.grid.GroupingView({
		forceFit : true,
		showGroupName : false,
		enableNoGroups : false, // REQUIRED!
		hideGroupedColumn : true
	}),

	plugins : costStatByMateriaSummary,

	frame : true,
	width : 800,
	height : 450,
	clicksToEdit : 1,
	collapsible : true,
	animCollapse : false,
	trackMouseOver : false
// enableColumnMove: false,
// title : 'Sponsored Projects'
// iconCls: 'icon-grid',
// renderTo: document.body
});

// --------------------------------------------------------------------------------------------------------

var costStatByMaterialResultWin = new Ext.Window({
	title : "消耗汇总-按食材",
	width : 400,
	height : 500,
	closeAction : "hide",
	resizable : false,
	layout : "fit",
	items : costStatByMateriaGrid,
	buttons : [ {
		text : "打印",
		handler : function() {

		}
	}, {
		text : "关闭",
		handler : function() {
			isPrompt = false;
			costStatByMaterialResultWin.hide();
		}
	} ],
	listeners : {
		"hide" : function(thiz) {
			isPrompt = false;
		}
	}
});

// 结果框 -- 按部門
// --------------------------------------------------------------------------------------------------------
var costStatByDeptReader = new Ext.data.JsonReader({
	idProperty : 'groupID',
	fields : [ {
		name : 'deptID',
		type : 'int'
	}, {
		name : 'deptName',
		type : 'string'
	}, {
		name : 'groupID',
		type : 'int'
	}, {
		name : 'groupDescr',
		type : 'string'
	}, {
		name : 'singlePrice',
		type : 'float'
	}, {
		name : 'amount',
		type : 'float'
	}, {
		name : 'sumPrice',
		type : 'float'
	}, {
		name : 'materialID'
	}, {
		name : 'materialName',
		type : 'string'
	} ]

});

var costStatByDeptSummary = new Ext.grid.GroupSummary();

var costStatByDeptGrid = new Ext.grid.EditorGridPanel({
	ds : new Ext.data.GroupingStore({
		reader : costStatByDeptReader,
		data : [],
		sortInfo : {
			field : 'materialName',
			direction : "ASC"
		},
		groupField : 'deptName'
	}),

	columns : [ {
		id : 'groupDescr',
		header : "部门",
		width : 20,
		sortable : true,
		dataIndex : 'groupDescr',
		// summaryType : 'count',
		hideable : false,
		// summaryRenderer : function(v, params, data) {
		// return ((v === 0 || v > 1) ? '(' + v + ' Tasks)' : '(1 Task)');
		// }
		summaryRenderer : function(v, params, data) {
			return "合计";
		}
	}, {
		header : "NOT SHOW",
		width : 20,
		sortable : false,
		dataIndex : 'deptName'
	}, {
		header : "食材",
		width : 25,
		sortable : true,
		dataIndex : 'materialName'
	}, {
		id : 'singlePrice',
		header : "价格",
		width : 20,
		sortable : false, // 計出來的列排序不起作用
		groupable : false,
		renderer : function(v, params, record) {
			// return Ext.util.Format.usMoney(record.data.sumPrice
			// / record.data.amount);
			if (record.data.amount != 0) {
				return (record.data.sumPrice / record.data.amount).toFixed(2);
			} else {
				return 0.00;
			}
		},
		dataIndex : 'singlePrice',
		summaryType : 'singlePrice'
	// summaryRenderer : Ext.util.Format.usMoney
	}, {
		header : "数量",
		width : 20,
		sortable : true,
		// renderer : Ext.util.Format.usMoney,
		dataIndex : 'amount',
		summaryType : 'sum'
	// editor: new Ext.form.NumberField({
	// allowBlank: false,
	// allowNegative: false,
	// style: 'text-align:left'
	// })
	}, {
		header : "小计",
		width : 20,
		sortable : true,
		// renderer : Ext.util.Format.chnMoney,
		dataIndex : 'sumPrice',
		summaryType : 'sum',
		renderer : function(v) {
			return parseFloat(v).toFixed(2);
		}
	// editor : new Ext.form.NumberField({
	// allowBlank : false,
	// allowNegative : false,
	// style : 'text-align:left'
	// })
	} ],

	view : new Ext.grid.GroupingView({
		forceFit : true,
		showGroupName : false,
		enableNoGroups : false, // REQUIRED!
		hideGroupedColumn : true
	}),

	plugins : costStatByDeptSummary,

	frame : true,
	width : 800,
	height : 450,
	clicksToEdit : 1,
	collapsible : true,
	animCollapse : false,
	trackMouseOver : false
// enableColumnMove: false,
// title : 'Sponsored Projects'
// iconCls: 'icon-grid',
// renderTo: document.body
});

// --------------------------------------------------------------------------------------------------------

var costStatByDeptResultWin = new Ext.Window({
	title : "消耗汇总-按部门",
	width : 400,
	height : 500,
	closeAction : "hide",
	resizable : false,
	layout : "fit",
	items : costStatByDeptGrid,
	buttons : [ {
		text : "打印",
		handler : function() {

		}
	}, {
		text : "关闭",
		handler : function() {
			isPrompt = false;
			costStatByDeptResultWin.hide();
		}
	} ],
	listeners : {
		"hide" : function(thiz) {
			isPrompt = false;
		}
	}
});
// --------------------------------------end消耗統計----------------------------------------------
// ---------------------------------------全部統計--------------------------------------------------------
// 条件框
var inventoryAllStatMSDS = new Ext.data.SimpleStore({
	fields : [ "retrunValue", "displayText" ],
	data : []
});

var inventoryAllStatFrom = new Ext.form.FormPanel({
	border : false,
	anchor : "right 8%",
	id : "AllStatForm",
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
				name : 'AllStat',
				inputValue : 'detail'
			} ]
		}, {
			layout : "form",
			border : false,
			labelSeparator : '',
			width : 110,
			items : [ {
				xtype : 'radio',
				hideLabel : true,
				boxLabel : "按食材汇总",
				// checked : true,
				name : 'AllStat',
				inputValue : 'sumByMaterial'
			} ]
		}, {
			layout : "form",
			border : false,
			labelSeparator : '',
			width : 110,
			items : [ {
				xtype : 'radio',
				hideLabel : true,
				boxLabel : "按部门汇总",
				// checked : true,
				name : 'AllStat',
				inputValue : 'sumByDept'
			} ]
		} ]
	} ]
});

inventoryAllStatWin = new Ext.Window(
		{
			title : "库存统计",
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
							xtype : "datefield",
							id : "begDateAllStat",
							width : 120,
							fieldLabel : "日期"
						} ]
					}, {
						layout : "form",
						border : false,
						labelSeparator : ' ',
						width : 200,
						labelWidth : 50,
						items : [ {
							xtype : "datefield",
							id : "endDateAllStat",
							width : 120,
							fieldLabel : "至"
						} ]
					} ]
				} ]
			}, {
				layout : "column",
				border : false,
				anchor : "99% 15%",
				autoScroll : true,
				frame : true,
				items : [ {
					layout : "form",
					border : false,
					labelSeparator : '',
					width : 80,
					labelWidth : 40,
					items : [ {
						xtype : "checkbox",
						id : "dept1AllStat"
					// fieldLabel : departmentData[0][1]
					} ]
				}, {
					layout : "form",
					border : false,
					labelSeparator : '',
					width : 80,
					labelWidth : 40,
					items : [ {
						xtype : "checkbox",
						id : "dept2AllStat"
					// fieldLabel : departmentData[1][1]
					} ]
				}, {
					layout : "form",
					border : false,
					labelSeparator : '',
					width : 80,
					labelWidth : 40,
					items : [ {
						xtype : "checkbox",
						id : "dept3AllStat"
					// fieldLabel : departmentData[2][1]
					} ]
				}, {
					layout : "form",
					border : false,
					labelSeparator : '',
					width : 80,
					labelWidth : 40,
					items : [ {
						xtype : "checkbox",
						id : "dept4AllStat"
					// fieldLabel : departmentData[3][1]
					} ]
				}, {
					layout : "form",
					border : false,
					labelSeparator : '',
					width : 80,
					labelWidth : 40,
					items : [ {
						xtype : "checkbox",
						id : "dept5AllStat"
					// fieldLabel : departmentData[4][1]
					} ]
				}, {
					layout : "form",
					border : false,
					labelSeparator : '',
					width : 80,
					labelWidth : 40,
					items : [ {
						xtype : "checkbox",
						id : "dept6AllStat"
					// fieldLabel : departmentData[5][1]
					} ]
				}, {
					layout : "form",
					border : false,
					labelSeparator : '',
					width : 80,
					labelWidth : 40,
					items : [ {
						xtype : "checkbox",
						id : "dept7AllStat"
					// fieldLabel : departmentData[6][1]
					} ]
				}, {
					layout : "form",
					border : false,
					labelSeparator : '',
					width : 80,
					labelWidth : 40,
					items : [ {
						xtype : "checkbox",
						id : "dept8AllStat"
					// fieldLabel : departmentData[7][1]
					} ]
				}, {
					layout : "form",
					border : false,
					labelSeparator : '',
					width : 80,
					labelWidth : 40,
					items : [ {
						xtype : "checkbox",
						id : "dept9AllStat"
					// fieldLabel : departmentData[8][1]
					} ]
				}, {
					layout : "form",
					border : false,
					labelSeparator : '',
					width : 80,
					labelWidth : 40,
					items : [ {
						xtype : "checkbox",
						id : "dept10AllStat"
					// fieldLabel : departmentData[9][1]
					} ]
				} ]
			}, {
				layout : "form",
				border : false,
				frame : true,
				hideLabels : true,
				anchor : "right 67%",
				items : [ {
					xtype : "itemselector",
					name : "materialAllStatMultSelect",
					id : "materialAllStatMultSelect",
					fromStore : inventoryAllStatMSDS,
					dataFields : [ "retrunValue", "displayText" ],
					toData : [ [ "", "" ] ],
					msHeight : 215,
					valueField : "retrunValue",
					displayField : "displayText",
					imagePath : "../../extjs/multiselect/images/",
					toLegend : "已选择食材",
					fromLegend : "可选择食材"
				} ]
			}, inventoryAllStatFrom ],
			buttons : [
					{
						text : "全选",
						handler : function() {
							var i = inventoryAllStatWin
									.findById("materialAllStatMultSelect").fromMultiselect.view;
							i.selectRange(0, i.store.length);
							inventoryAllStatWin.findById(
									"materialAllStatMultSelect").fromTo();
							inventoryAllStatWin
									.findById("materialAllStatMultSelect").toMultiselect.view
									.clearSelections();
						}
					},
					{
						text : "清空",
						handler : function() {
							inventoryAllStatWin.findById(
									"materialAllStatMultSelect").reset();
						}
					},
					{
						text : "确定",
						handler : function() {

							var selectCount = inventoryAllStatWin
									.findById("materialAllStatMultSelect").toMultiselect.store
									.getCount();

							if (selectCount != 0) {
								isPrompt = false;
								inventoryAllStatWin.hide();

								// -- 獲取選擇的食材 --
								var selectMaterials = "";
								for ( var i = 0; i < selectCount; i++) {
									var selectItem = inventoryAllStatWin
											.findById("materialAllStatMultSelect").toMultiselect.store
											.getAt(i).get("retrunValue");
									// if (selectItem != "") {
									selectMaterials = selectMaterials
											+ selectItem + ",";
									// }
								}
								// 去掉最后一个逗号
								selectMaterials = selectMaterials.substring(0,
										selectMaterials.length - 1);
								if (selectMaterials.substring(0, 1) == ",") {
									selectMaterials = selectMaterials
											.substring(1,
													selectMaterials.length);
								}

								// -- 獲取時間 --
								var beginDate = inventoryAllStatWin.findById(
										"begDateAllStat").getValue();
								if (beginDate != "") {
									var dateFormated = new Date();
									dateFormated = beginDate;
									beginDate = dateFormated.format('Y-m-d');
								}

								var endDate = inventoryAllStatWin.findById(
										"endDateAllStat").getValue();
								if (endDate != "") {
									var dateFormated = new Date();
									dateFormated = endDate;
									endDate = dateFormated.format('Y-m-d');
								}

								// -- 獲取部門 --
								var departments = "";
								if (inventoryAllStatWin
										.findById("dept1AllStat").getValue() == true) {
									departments = departments + "0,";
								}
								if (inventoryAllStatWin
										.findById("dept2AllStat").getValue() == true) {
									departments = departments + "1,";
								}
								if (inventoryAllStatWin
										.findById("dept3AllStat").getValue() == true) {
									departments = departments + "2,";
								}
								if (inventoryAllStatWin
										.findById("dept4AllStat").getValue() == true) {
									departments = departments + "3,";
								}
								if (inventoryAllStatWin
										.findById("dept5AllStat").getValue() == true) {
									departments = departments + "4,";
								}
								if (inventoryAllStatWin
										.findById("dept6AllStat").getValue() == true) {
									departments = departments + "5,";
								}
								if (inventoryAllStatWin
										.findById("dept7AllStat").getValue() == true) {
									departments = departments + "6,";
								}
								if (inventoryAllStatWin
										.findById("dept8AllStat").getValue() == true) {
									departments = departments + "7,";
								}
								if (inventoryAllStatWin
										.findById("dept9AllStat").getValue() == true) {
									departments = departments + "8,";
								}
								if (inventoryAllStatWin.findById(
										"dept10AllStat").getValue() == true) {
									departments = departments + "9,";
								}

								if (departments != "") {
									departments = departments.substring(0,
											departments.length - 1);
								}

								// -- 獲取統計類型 --
								var staticType = inventoryAllStatFrom.getForm()
										.findField("AllStat").getGroupValue();
								if (staticType == "detail") {
									isPrompt = true;
									allStatDetailResultWin.show();

									// ＃＃＃＃＃＃＃＃＃＃＃＃明細統計＃＃＃＃＃＃＃＃＃＃＃＃
									Ext.Ajax
											.request({
												url : "../../InventoryAllStatDetail.do",
												params : {
													"pin" : pin,
													"beginDate" : beginDate,
													"endDate" : endDate,
													"departments" : departments,
													"materials" : selectMaterials
												},
												success : function(response,
														options) {
													var resultJSON = Ext.util.JSON
															.decode(response.responseText);

													// 格式：[食材 日期 操作 经手人 部门 价格 数量
													// 小计]
													var rootData = resultJSON.root;
													if (rootData[0].message == "normal") {
														allStatDetailResultData.length = 0;
														for ( var i = 0; i < rootData.length; i++) {
															var materialN = "";
															for ( var j = 0; j < materialData.length; j++) {
																if (materialData[j][0] == rootData[i].materialID) {
																	materialN = materialData[j][2];
																}
															}

															var deptN = "";
															for ( var j = 0; j < departmentData.length; j++) {
																if (departmentData[j][0] == rootData[i].deptID) {
																	deptN = departmentData[j][1];
																}
															}

															var typeN = inventoryTypeCode2Descr(rootData[i].typeCode);

															allStatDetailResultData
																	.push([
																			rootData[i].materialID,
																			materialN,
																			rootData[i].date,
																			rootData[i].typeCode,
																			typeN,
																			rootData[i].operator,
																			rootData[i].deptID,
																			deptN,
																			rootData[i].price,
																			rootData[i].amount,
																			rootData[i].total

																	]);
														}

														allStatDetailResultStore
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

								} else if (staticType == "sumByMaterial") {
									isPrompt = true;
									allStatByMaterialResultWin.show();

									// ＃＃＃＃＃＃＃＃＃＃＃＃按食材統計＃＃＃＃＃＃＃＃＃＃＃＃
									Ext.Ajax
											.request({
												url : "../../InventoryAllStatByMaterial.do",
												params : {
													"pin" : pin,
													"beginDate" : beginDate,
													"endDate" : endDate,
													"departments" : departments,
													"materials" : selectMaterials
												},
												success : function(response,
														options) {
													var resultJSON = Ext.util.JSON
															.decode(response.responseText);

													var rootData = resultJSON.root;
													if (rootData[0].message == "normal") {

														allStatByMaterialResultData = rootData;
														allStatByMateriaGrid
																.getStore()
																.loadData(
																		allStatByMaterialResultData);
														if (rootData[0].materialID == "NO_DATA") {
															allStatByMateriaGrid
																	.getStore()
																	.removeAll();
														}

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

								} else if (staticType == "sumByDept") {

									isPrompt = true;
									allStatByDeptResultWin.show();

									// ＃＃＃＃＃＃＃＃＃＃＃＃按部門統計＃＃＃＃＃＃＃＃＃＃＃＃
									Ext.Ajax
											.request({
												url : "../../InventoryAllStatByDept.do",
												params : {
													"pin" : pin,
													"beginDate" : beginDate,
													"endDate" : endDate,
													"departments" : departments,
													"materials" : selectMaterials
												},
												success : function(response,
														options) {
													var resultJSON = Ext.util.JSON
															.decode(response.responseText);

													var rootData = resultJSON.root;
													if (rootData[0].message == "normal") {

														allStatByDeptResultData = rootData;
														allStatByDeptGrid
																.getStore()
																.loadData(
																		allStatByDeptResultData);
														if (rootData[0].materialID == "NO_DATA") {
															allStatByDeptGrid
																	.getStore()
																	.removeAll();
														}

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

							} else {
								Ext.MessageBox.show({
									msg : "至少需要选择一个食材进行统计",
									width : 300,
									buttons : Ext.MessageBox.OK
								});
							}
						}
					}, {
						text : "取消",
						handler : function() {
							isPrompt = false;
							inventoryAllStatWin.hide();
						}
					} ],
			listeners : {
				"show" : function(thiz) {

					inventoryAllStatWin.findById("materialAllStatMultSelect")
							.reset();
					inventoryAllStatWin.findById("begDateAllStat").setValue("");
					inventoryAllStatWin.findById("endDateAllStat").setValue("");

					// inventoryAllStatMSDS.loadData(materialComboData);

					// 神技！動態改變form中component的label！！！
					inventoryAllStatWin.findById("dept1AllStat").el.parent()
							.parent().parent().first().dom.innerHTML = departmentData[0][1]
							+ ":";
					inventoryAllStatWin.findById("dept2AllStat").el.parent()
							.parent().parent().first().dom.innerHTML = departmentData[1][1]
							+ ":";
					inventoryAllStatWin.findById("dept3AllStat").el.parent()
							.parent().parent().first().dom.innerHTML = departmentData[2][1]
							+ ":";
					inventoryAllStatWin.findById("dept4AllStat").el.parent()
							.parent().parent().first().dom.innerHTML = departmentData[3][1]
							+ ":";
					inventoryAllStatWin.findById("dept5AllStat").el.parent()
							.parent().parent().first().dom.innerHTML = departmentData[4][1]
							+ ":";
					inventoryAllStatWin.findById("dept6AllStat").el.parent()
							.parent().parent().first().dom.innerHTML = departmentData[5][1]
							+ ":";
					inventoryAllStatWin.findById("dept7AllStat").el.parent()
							.parent().parent().first().dom.innerHTML = departmentData[6][1]
							+ ":";
					inventoryAllStatWin.findById("dept8AllStat").el.parent()
							.parent().parent().first().dom.innerHTML = departmentData[7][1]
							+ ":";
					inventoryAllStatWin.findById("dept9AllStat").el.parent()
							.parent().parent().first().dom.innerHTML = departmentData[8][1]
							+ ":";
					inventoryAllStatWin.findById("dept10AllStat").el.parent()
							.parent().parent().first().dom.innerHTML = departmentData[9][1]
							+ ":";

					inventoryAllStatWin.findById("dept1AllStat")
							.setValue(false);
					inventoryAllStatWin.findById("dept2AllStat")
							.setValue(false);
					inventoryAllStatWin.findById("dept3AllStat")
							.setValue(false);
					inventoryAllStatWin.findById("dept4AllStat")
							.setValue(false);
					inventoryAllStatWin.findById("dept5AllStat")
							.setValue(false);
					inventoryAllStatWin.findById("dept6AllStat")
							.setValue(false);
					inventoryAllStatWin.findById("dept7AllStat")
							.setValue(false);
					inventoryAllStatWin.findById("dept8AllStat")
							.setValue(false);
					inventoryAllStatWin.findById("dept9AllStat")
							.setValue(false);
					inventoryAllStatWin.findById("dept10AllStat").setValue(
							false);

					inventoryAllStatFrom.getForm().findField("AllStat")
							.setValue("detail");
				},
				"hide" : function(thiz) {
					isPrompt = false;
				}
			}
		});

// 结果框 -- 明細
// 前台：[食材 日期 操作 经手人 部门 价格 数量 小计]
var allStatDetailResultStore = new Ext.data.Store({
	proxy : new Ext.data.MemoryProxy(allStatDetailResultData),
	reader : new Ext.data.ArrayReader({}, [ {
		name : "materialID"
	}, {
		name : "materialName"
	}, {
		name : "date"
	}, {
		name : "typeCode"
	}, {
		name : "typeName"
	}, {
		name : "operator"
	}, {
		name : "deptID"
	}, {
		name : "deptName"
	}, {
		name : "price"
	}, {
		name : "amount"
	}, {
		name : "total"
	}, {
		name : "message"
	} ])
});

// 2，栏位模型
var allStatDetailResultColumnModel = new Ext.grid.ColumnModel([
		new Ext.grid.RowNumberer(), {
			header : "食材",
			sortable : true,
			dataIndex : "materialName",
			width : 80
		}, {
			header : "日期",
			sortable : true,
			dataIndex : "date",
			width : 80
		}, {
			header : "操作",
			sortable : true,
			dataIndex : "typeName",
			width : 80
		}, {
			header : "经手人",
			sortable : true,
			dataIndex : "operator",
			width : 80
		}, {
			header : "部门",
			sortable : true,
			dataIndex : "deptName",
			width : 80
		}, {
			header : "价钱",
			sortable : true,
			dataIndex : "price",
			width : 80
		}, {
			header : "数量",
			sortable : true,
			dataIndex : "amount",
			width : 80
		}, {
			header : "小计（￥）",
			sortable : true,
			dataIndex : "total",
			width : 80
		} ]);

var allStatDetailResultGrid = new Ext.grid.GridPanel({
	xtype : "grid",
	anchor : "99%",
	border : false,
	ds : allStatDetailResultStore,
	cm : allStatDetailResultColumnModel,
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

var allStatDetailResultWin = new Ext.Window({
	title : "库存明细",
	width : 800,
	height : 370,
	closeAction : "hide",
	resizable : false,
	layout : "fit",
	items : allStatDetailResultGrid,
	buttons : [ {
		text : "打印",
		handler : function() {

		}
	}, {
		text : "关闭",
		handler : function() {
			isPrompt = false;
			allStatDetailResultWin.hide();
		}
	} ],
	listeners : {
		"hide" : function(thiz) {
			isPrompt = false;
		}
	}
});

// 结果框 -- 按食材
// --------------------------------------------------------------------------------------------------------
var allStatByMateriaReader = new Ext.data.JsonReader({
	idProperty : 'groupID',
	fields : [ {
		name : 'materialID',
		type : 'int'
	}, {
		name : 'materialName',
		type : 'string'
	}, {
		name : 'groupID',
		type : 'int'
	}, {
		name : 'groupDescr',
		type : 'string'
	}, {
		name : 'singlePrice',
		type : 'float'
	}, {
		name : 'amount',
		type : 'float'
	}, {
		name : 'sumPrice',
		type : 'float'
	}, {
		name : 'deptID'
	}, {
		name : 'deptName',
		type : 'string'
	} ]

});

var allStatByMateriaSummary = new Ext.grid.GroupSummary();

var allStatByMateriaGrid = new Ext.grid.EditorGridPanel({
	ds : new Ext.data.GroupingStore({
		reader : allStatByMateriaReader,
		data : [],
		sortInfo : {
			field : 'deptName',
			direction : "ASC"
		},
		groupField : 'materialName'
	}),

	columns : [ {
		id : 'groupDescr',
		header : "食材",
		width : 20,
		sortable : true,
		dataIndex : 'groupDescr',
		// summaryType : 'count',
		hideable : false,
		// summaryRenderer : function(v, params, data) {
		// return ((v === 0 || v > 1) ? '(' + v + ' Tasks)' : '(1 Task)');
		// }
		summaryRenderer : function(v, params, data) {
			return "合计";
		}
	}, {
		header : "NOT SHOW",
		width : 20,
		sortable : false,
		dataIndex : 'materialName'
	}, {
		header : "部门",
		width : 25,
		sortable : true,
		dataIndex : 'deptName'
	}, {
		id : 'singlePrice',
		header : "价格",
		width : 20,
		sortable : false, // 計出來的列排序不起作用
		groupable : false,
		renderer : function(v, params, record) {
			// return Ext.util.Format.usMoney(record.data.sumPrice
			// / record.data.amount);
			if (record.data.amount != 0) {
				return (record.data.sumPrice / record.data.amount).toFixed(2);
			} else {
				return 0.00;
			}
		},
		dataIndex : 'singlePrice',
		summaryType : 'singlePrice'
	// summaryRenderer : Ext.util.Format.usMoney
	}, {
		header : "数量",
		width : 20,
		sortable : true,
		// renderer : Ext.util.Format.usMoney,
		dataIndex : 'amount',
		summaryType : 'sum'
	// editor: new Ext.form.NumberField({
	// allowBlank: false,
	// allowNegative: false,
	// style: 'text-align:left'
	// })
	}, {
		header : "小计",
		width : 20,
		sortable : true,
		// renderer : Ext.util.Format.chnMoney,
		dataIndex : 'sumPrice',
		summaryType : 'sum',
		renderer : function(v) {
			return parseFloat(v).toFixed(2);
		}
	// editor : new Ext.form.NumberField({
	// allowBlank : false,
	// allowNegative : false,
	// style : 'text-align:left'
	// })
	} ],

	view : new Ext.grid.GroupingView({
		forceFit : true,
		showGroupName : false,
		enableNoGroups : false, // REQUIRED!
		hideGroupedColumn : true
	}),

	plugins : allStatByMateriaSummary,

	frame : true,
	width : 800,
	height : 450,
	clicksToEdit : 1,
	collapsible : true,
	animCollapse : false,
	trackMouseOver : false
// enableColumnMove: false,
// title : 'Sponsored Projects'
// iconCls: 'icon-grid',
// renderTo: document.body
});

// --------------------------------------------------------------------------------------------------------

var allStatByMaterialResultWin = new Ext.Window({
	title : "库存汇总-按食材",
	width : 400,
	height : 500,
	closeAction : "hide",
	resizable : false,
	layout : "fit",
	items : allStatByMateriaGrid,
	buttons : [ {
		text : "打印",
		handler : function() {

		}
	}, {
		text : "关闭",
		handler : function() {
			isPrompt = false;
			allStatByMaterialResultWin.hide();
		}
	} ],
	listeners : {
		"hide" : function(thiz) {
			isPrompt = false;
		}
	}
});

// 结果框 -- 按部門
// --------------------------------------------------------------------------------------------------------
var allStatByDeptReader = new Ext.data.JsonReader({
	idProperty : 'groupID',
	fields : [ {
		name : 'deptID',
		type : 'int'
	}, {
		name : 'deptName',
		type : 'string'
	}, {
		name : 'groupID',
		type : 'int'
	}, {
		name : 'groupDescr',
		type : 'string'
	}, {
		name : 'singlePrice',
		type : 'float'
	}, {
		name : 'amount',
		type : 'float'
	}, {
		name : 'sumPrice',
		type : 'float'
	}, {
		name : 'materialID'
	}, {
		name : 'materialName',
		type : 'string'
	} ]

});

var allStatByDeptSummary = new Ext.grid.GroupSummary();

var allStatByDeptGrid = new Ext.grid.EditorGridPanel({
	ds : new Ext.data.GroupingStore({
		reader : allStatByDeptReader,
		data : [],
		sortInfo : {
			field : 'materialName',
			direction : "ASC"
		},
		groupField : 'deptName'
	}),

	columns : [ {
		id : 'groupDescr',
		header : "部门",
		width : 20,
		sortable : true,
		dataIndex : 'groupDescr',
		// summaryType : 'count',
		hideable : false,
		// summaryRenderer : function(v, params, data) {
		// return ((v === 0 || v > 1) ? '(' + v + ' Tasks)' : '(1 Task)');
		// }
		summaryRenderer : function(v, params, data) {
			return "合计";
		}
	}, {
		header : "NOT SHOW",
		width : 20,
		sortable : false,
		dataIndex : 'deptName'
	}, {
		header : "食材",
		width : 25,
		sortable : true,
		dataIndex : 'materialName'
	}, {
		id : 'singlePrice',
		header : "价格",
		width : 20,
		sortable : false, // 計出來的列排序不起作用
		groupable : false,
		renderer : function(v, params, record) {
			// return Ext.util.Format.usMoney(record.data.sumPrice
			// / record.data.amount);
			if (record.data.amount != 0) {
				return (record.data.sumPrice / record.data.amount).toFixed(2);
			} else {
				return 0.00;
			}
		},
		dataIndex : 'singlePrice',
		summaryType : 'singlePrice'
	// summaryRenderer : Ext.util.Format.usMoney
	}, {
		header : "数量",
		width : 20,
		sortable : true,
		// renderer : Ext.util.Format.usMoney,
		dataIndex : 'amount',
		summaryType : 'sum'
	// editor: new Ext.form.NumberField({
	// allowBlank: false,
	// allowNegative: false,
	// style: 'text-align:left'
	// })
	}, {
		header : "小计",
		width : 20,
		sortable : true,
		// renderer : Ext.util.Format.chnMoney,
		dataIndex : 'sumPrice',
		summaryType : 'sum',
		renderer : function(v) {
			return parseFloat(v).toFixed(2);
		}
	// editor : new Ext.form.NumberField({
	// allowBlank : false,
	// allowNegative : false,
	// style : 'text-align:left'
	// })
	} ],

	view : new Ext.grid.GroupingView({
		forceFit : true,
		showGroupName : false,
		enableNoGroups : false, // REQUIRED!
		hideGroupedColumn : true
	}),

	plugins : allStatByDeptSummary,

	frame : true,
	width : 800,
	height : 450,
	clicksToEdit : 1,
	collapsible : true,
	animCollapse : false,
	trackMouseOver : false
// enableColumnMove: false,
// title : 'Sponsored Projects'
// iconCls: 'icon-grid',
// renderTo: document.body
});

// --------------------------------------------------------------------------------------------------------

var allStatByDeptResultWin = new Ext.Window({
	title : "库存汇总-按部门",
	width : 400,
	height : 500,
	closeAction : "hide",
	resizable : false,
	layout : "fit",
	items : allStatByDeptGrid,
	buttons : [ {
		text : "打印",
		handler : function() {

		}
	}, {
		text : "关闭",
		handler : function() {
			isPrompt = false;
			allStatByDeptResultWin.hide();
		}
	} ],
	listeners : {
		"hide" : function(thiz) {
			isPrompt = false;
		}
	}
});
// --------------------------------------end全部統計----------------------------------------------
// ---------------------------------------盤點統計--------------------------------------------------------
// 条件框
var inventoryCheckStatMSDS = new Ext.data.SimpleStore({
	fields : [ "retrunValue", "displayText" ],
	data : []
});

var inventoryCheckStatFrom = new Ext.form.FormPanel({
	border : false,
	anchor : "right 10%",
	id : "CheckStatForm",
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
				name : 'CheckStat',
				inputValue : 'detail'
			} ]
		}, {
			layout : "form",
			border : false,
			labelSeparator : '',
			width : 120,
			items : [ {
				xtype : 'radio',
				hideLabel : true,
				boxLabel : "汇总",
				// checked : true,
				name : 'CheckStat',
				inputValue : 'sum'
			} ]
		}, {
			layout : "form",
			border : false,
			labelSeparator : '：',
			width : 100,
			labelWidth : 60,
			items : [ {
				xtype : "checkbox",
				id : "isDifferentCheck",
				fieldLabel : "有差异"
			} ]
		} ]
	} ]
});

inventoryCheckStatWin = new Ext.Window(
		{
			title : "盘点统计",
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
							xtype : "datefield",
							id : "begDateCheckStat",
							width : 120,
							fieldLabel : "日期"
						} ]
					}, {
						layout : "form",
						border : false,
						labelSeparator : ' ',
						width : 200,
						labelWidth : 50,
						items : [ {
							xtype : "datefield",
							id : "endDateCheckStat",
							width : 120,
							fieldLabel : "至"
						} ]
					} ]
				} ]
			}, {
				layout : "column",
				border : false,
				anchor : "99% 15%",
				autoScroll : true,
				frame : true,
				items : [ {
					layout : "form",
					border : false,
					labelSeparator : '',
					width : 80,
					labelWidth : 40,
					items : [ {
						xtype : "checkbox",
						id : "dept1CheckStat"
					// fieldLabel : departmentData[0][1]
					} ]
				}, {
					layout : "form",
					border : false,
					labelSeparator : '',
					width : 80,
					labelWidth : 40,
					items : [ {
						xtype : "checkbox",
						id : "dept2CheckStat"
					// fieldLabel : departmentData[1][1]
					} ]
				}, {
					layout : "form",
					border : false,
					labelSeparator : '',
					width : 80,
					labelWidth : 40,
					items : [ {
						xtype : "checkbox",
						id : "dept3CheckStat"
					// fieldLabel : departmentData[2][1]
					} ]
				}, {
					layout : "form",
					border : false,
					labelSeparator : '',
					width : 80,
					labelWidth : 40,
					items : [ {
						xtype : "checkbox",
						id : "dept4CheckStat"
					// fieldLabel : departmentData[3][1]
					} ]
				}, {
					layout : "form",
					border : false,
					labelSeparator : '',
					width : 80,
					labelWidth : 40,
					items : [ {
						xtype : "checkbox",
						id : "dept5CheckStat"
					// fieldLabel : departmentData[4][1]
					} ]
				}, {
					layout : "form",
					border : false,
					labelSeparator : '',
					width : 80,
					labelWidth : 40,
					items : [ {
						xtype : "checkbox",
						id : "dept6CheckStat"
					// fieldLabel : departmentData[5][1]
					} ]
				}, {
					layout : "form",
					border : false,
					labelSeparator : '',
					width : 80,
					labelWidth : 40,
					items : [ {
						xtype : "checkbox",
						id : "dept7CheckStat"
					// fieldLabel : departmentData[6][1]
					} ]
				}, {
					layout : "form",
					border : false,
					labelSeparator : '',
					width : 80,
					labelWidth : 40,
					items : [ {
						xtype : "checkbox",
						id : "dept8CheckStat"
					// fieldLabel : departmentData[7][1]
					} ]
				}, {
					layout : "form",
					border : false,
					labelSeparator : '',
					width : 80,
					labelWidth : 40,
					items : [ {
						xtype : "checkbox",
						id : "dept9CheckStat"
					// fieldLabel : departmentData[8][1]
					} ]
				}, {
					layout : "form",
					border : false,
					labelSeparator : '',
					width : 80,
					labelWidth : 40,
					items : [ {
						xtype : "checkbox",
						id : "dept10CheckStat"
					// fieldLabel : departmentData[9][1]
					} ]
				} ]
			}, {
				layout : "form",
				border : false,
				frame : true,
				hideLabels : true,
				anchor : "right 67%",
				items : [ {
					xtype : "itemselector",
					name : "materialCheckStatMultSelect",
					id : "materialCheckStatMultSelect",
					fromStore : inventoryCheckStatMSDS,
					dataFields : [ "retrunValue", "displayText" ],
					toData : [ [ "", "" ] ],
					msHeight : 215,
					valueField : "retrunValue",
					displayField : "displayText",
					imagePath : "../../extjs/multiselect/images/",
					toLegend : "已选择食材",
					fromLegend : "可选择食材"
				} ]
			}, inventoryCheckStatFrom ],
			buttons : [
					{
						text : "全选",
						handler : function() {
							var i = inventoryCheckStatWin
									.findById("materialCheckStatMultSelect").fromMultiselect.view;
							i.selectRange(0, i.store.length);
							inventoryCheckStatWin.findById(
									"materialCheckStatMultSelect").fromTo();
							inventoryCheckStatWin
									.findById("materialCheckStatMultSelect").toMultiselect.view
									.clearSelections();
						}
					},
					{
						text : "清空",
						handler : function() {
							inventoryCheckStatWin.findById(
									"materialCheckStatMultSelect").reset();
						}
					},
					{
						text : "确定",
						handler : function() {

							var selectCount = inventoryCheckStatWin
									.findById("materialCheckStatMultSelect").toMultiselect.store
									.getCount();

							if (selectCount != 0) {
								isPrompt = false;
								inventoryCheckStatWin.hide();

								// -- 獲取選擇的食材 --
								var selectMaterials = "";
								for ( var i = 0; i < selectCount; i++) {
									var selectItem = inventoryCheckStatWin
											.findById("materialCheckStatMultSelect").toMultiselect.store
											.getAt(i).get("retrunValue");
									// if (selectItem != "") {
									selectMaterials = selectMaterials
											+ selectItem + ",";
									// }
								}
								// 去掉最后一个逗号
								selectMaterials = selectMaterials.substring(0,
										selectMaterials.length - 1);
								if (selectMaterials.substring(0, 1) == ",") {
									selectMaterials = selectMaterials
											.substring(1,
													selectMaterials.length);
								}

								// -- 獲取時間 --
								var beginDate = inventoryCheckStatWin.findById(
										"begDateCheckStat").getValue();
								if (beginDate != "") {
									var dateFormated = new Date();
									dateFormated = beginDate;
									beginDate = dateFormated.format('Y-m-d');
								}

								var endDate = inventoryCheckStatWin.findById(
										"endDateCheckStat").getValue();
								if (endDate != "") {
									var dateFormated = new Date();
									dateFormated = endDate;
									endDate = dateFormated.format('Y-m-d');
								}

								// -- 獲取部門 --
								var departments = "";
								if (inventoryCheckStatWin.findById(
										"dept1CheckStat").getValue() == true) {
									departments = departments + "0,";
								}
								if (inventoryCheckStatWin.findById(
										"dept2CheckStat").getValue() == true) {
									departments = departments + "1,";
								}
								if (inventoryCheckStatWin.findById(
										"dept3CheckStat").getValue() == true) {
									departments = departments + "2,";
								}
								if (inventoryCheckStatWin.findById(
										"dept4CheckStat").getValue() == true) {
									departments = departments + "3,";
								}
								if (inventoryCheckStatWin.findById(
										"dept5CheckStat").getValue() == true) {
									departments = departments + "4,";
								}
								if (inventoryCheckStatWin.findById(
										"dept6CheckStat").getValue() == true) {
									departments = departments + "5,";
								}
								if (inventoryCheckStatWin.findById(
										"dept7CheckStat").getValue() == true) {
									departments = departments + "6,";
								}
								if (inventoryCheckStatWin.findById(
										"dept8CheckStat").getValue() == true) {
									departments = departments + "7,";
								}
								if (inventoryCheckStatWin.findById(
										"dept9CheckStat").getValue() == true) {
									departments = departments + "8,";
								}
								if (inventoryCheckStatWin.findById(
										"dept10CheckStat").getValue() == true) {
									departments = departments + "9,";
								}

								if (departments != "") {
									departments = departments.substring(0,
											departments.length - 1);
								}

								// -- 是否有差異 --
								var isDiff = inventoryCheckStatWin.findById(
										"isDifferentCheck").getValue();

								// -- 獲取統計類型 --
								var staticType = inventoryCheckStatFrom
										.getForm().findField("CheckStat")
										.getGroupValue();
								if (staticType == "detail") {
									isPrompt = true;
									checkStatDetailResultWin.show();

									// ＃＃＃＃＃＃＃＃＃＃＃＃明細統計＃＃＃＃＃＃＃＃＃＃＃＃
									Ext.Ajax
											.request({
												url : "../../InventoryCheckStatDetail.do",
												params : {
													"pin" : pin,
													"beginDate" : beginDate,
													"endDate" : endDate,
													"departments" : departments,
													"materials" : selectMaterials,
													"isDiff" : isDiff
												},
												success : function(response,
														options) {
													var resultJSON = Ext.util.JSON
															.decode(response.responseText);

													// 格式：[食材 日期 部门 经手人 盘点前价格
													// 盘点前数量 盘点后价格 盘点后数量]
													var rootData = resultJSON.root;
													if (rootData[0].message == "normal") {
														checkStatDetailResultData.length = 0;
														for ( var i = 0; i < rootData.length; i++) {
															var materialN = "";
															for ( var j = 0; j < materialData.length; j++) {
																if (materialData[j][0] == rootData[i].materialID) {
																	materialN = materialData[j][2];
																}
															}

															var deptN = "";
															for ( var j = 0; j < departmentData.length; j++) {
																if (departmentData[j][0] == rootData[i].deptID) {
																	deptN = departmentData[j][1];
																}
															}

															checkStatDetailResultData
																	.push([
																			rootData[i].materialID,
																			materialN,
																			rootData[i].date,
																			rootData[i].deptID,
																			deptN,
																			rootData[i].operator,
																			rootData[i].pricePrevious,
																			rootData[i].amountPrevious,
																			rootData[i].price,
																			rootData[i].amount

																	]);
														}

														checkStatDetailResultStore
																.reload();

														if (rootData[0].materialID == "NO_DATA") {
															checkStatDetailResultStore
																	.removeAll();
														}

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

								} else if (staticType == "sum") {
									isPrompt = true;
									checkStatSumResultWin.show();

									// ＃＃＃＃＃＃＃＃＃＃＃＃匯總統計＃＃＃＃＃＃＃＃＃＃＃＃
									Ext.Ajax
											.request({
												url : "../../InventoryCheckStatSum.do",
												params : {
													"pin" : pin,
													"beginDate" : beginDate,
													"endDate" : endDate,
													"departments" : departments,
													"materials" : selectMaterials,
													"isDiff" : isDiff
												},
												success : function(response,
														options) {
													var resultJSON = Ext.util.JSON
															.decode(response.responseText);

													// 格式：[食材 日期 盘点前价格 盘点前数量
													// 盘点前合计 盘点后价格 盘点后数量 盘点后合计
													// 损益 盈亏]
													var rootData = resultJSON.root;
													if (rootData[0].message == "normal") {
														checkStatSumResultData.length = 0;
														for ( var i = 0; i < rootData.length; i++) {
															var materialN = "";
															for ( var j = 0; j < materialData.length; j++) {
																if (materialData[j][0] == rootData[i].materialID) {
																	materialN = materialData[j][2];
																}
															}

															checkStatSumResultData
																	.push([
																			rootData[i].materialID,
																			materialN,
																			rootData[i].date,
																			rootData[i].pricePrevious,
																			rootData[i].amountPrevious,
																			"",
																			rootData[i].price,
																			rootData[i].amount,
																			"",
																			"",
																			""

																	]);
														}

														checkStatSumResultStore
																.reload();

														if (rootData[0].materialID == "NO_DATA") {
															checkStatSumResultStore
																	.removeAll();
														}

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

							} else {
								Ext.MessageBox.show({
									msg : "至少需要选择一个食材进行统计",
									width : 300,
									buttons : Ext.MessageBox.OK
								});
							}
						}
					}, {
						text : "取消",
						handler : function() {
							isPrompt = false;
							inventoryCheckStatWin.hide();
						}
					} ],
			listeners : {
				"show" : function(thiz) {

					inventoryCheckStatWin.findById(
							"materialCheckStatMultSelect").reset();
					inventoryCheckStatWin.findById("begDateCheckStat")
							.setValue("");
					inventoryCheckStatWin.findById("endDateCheckStat")
							.setValue("");

					// inventoryCheckStatMSDS.loadData(materialComboData);

					// 神技！動態改變form中component的label！！！
					inventoryCheckStatWin.findById("dept1CheckStat").el
							.parent().parent().parent().first().dom.innerHTML = departmentData[0][1]
							+ ":";
					inventoryCheckStatWin.findById("dept2CheckStat").el
							.parent().parent().parent().first().dom.innerHTML = departmentData[1][1]
							+ ":";
					inventoryCheckStatWin.findById("dept3CheckStat").el
							.parent().parent().parent().first().dom.innerHTML = departmentData[2][1]
							+ ":";
					inventoryCheckStatWin.findById("dept4CheckStat").el
							.parent().parent().parent().first().dom.innerHTML = departmentData[3][1]
							+ ":";
					inventoryCheckStatWin.findById("dept5CheckStat").el
							.parent().parent().parent().first().dom.innerHTML = departmentData[4][1]
							+ ":";
					inventoryCheckStatWin.findById("dept6CheckStat").el
							.parent().parent().parent().first().dom.innerHTML = departmentData[5][1]
							+ ":";
					inventoryCheckStatWin.findById("dept7CheckStat").el
							.parent().parent().parent().first().dom.innerHTML = departmentData[6][1]
							+ ":";
					inventoryCheckStatWin.findById("dept8CheckStat").el
							.parent().parent().parent().first().dom.innerHTML = departmentData[7][1]
							+ ":";
					inventoryCheckStatWin.findById("dept9CheckStat").el
							.parent().parent().parent().first().dom.innerHTML = departmentData[8][1]
							+ ":";
					inventoryCheckStatWin.findById("dept10CheckStat").el
							.parent().parent().parent().first().dom.innerHTML = departmentData[9][1]
							+ ":";

					inventoryCheckStatWin.findById("dept1CheckStat").setValue(
							false);
					inventoryCheckStatWin.findById("dept2CheckStat").setValue(
							false);
					inventoryCheckStatWin.findById("dept3CheckStat").setValue(
							false);
					inventoryCheckStatWin.findById("dept4CheckStat").setValue(
							false);
					inventoryCheckStatWin.findById("dept5CheckStat").setValue(
							false);
					inventoryCheckStatWin.findById("dept6CheckStat").setValue(
							false);
					inventoryCheckStatWin.findById("dept7CheckStat").setValue(
							false);
					inventoryCheckStatWin.findById("dept8CheckStat").setValue(
							false);
					inventoryCheckStatWin.findById("dept9CheckStat").setValue(
							false);
					inventoryCheckStatWin.findById("dept10CheckStat").setValue(
							false);

					inventoryCheckStatFrom.getForm().findField("CheckStat")
							.setValue("detail");

					inventoryCheckStatWin.findById("isDifferentCheck")
							.setValue(false);

				},
				"hide" : function(thiz) {
					isPrompt = false;
				}
			}
		});

// 结果框 -- 明細
// 前台：[食材 日期 操作 经手人 部门 价格 数量 小计]
var checkStatDetailResultStore = new Ext.data.Store({
	proxy : new Ext.data.MemoryProxy(checkStatDetailResultData),
	reader : new Ext.data.ArrayReader({}, [ {
		name : "materialID"
	}, {
		name : "materialName"
	}, {
		name : "date"
	}, {
		name : "deptID"
	}, {
		name : "deptName"
	}, {
		name : "operator"
	}, {
		name : "pricePrevious"
	}, {
		name : "amountPrevious"
	}, {
		name : "price"
	}, {
		name : "amount"
	}, {
		name : "message"
	} ]),
	listeners : {
		"load" : function(thiz, records, options) {
			thiz.each(function(record) {
				var beforePrice = parseFloat(record.get("pricePrevious"));
				var afterPrice = parseFloat(record.get("price"));
				var beforeAmount = parseFloat(record.get("amountPrevious"));
				var afterAmount = parseFloat(record.get("amount"));

				var priceDiffDisplay = "";
				if (beforePrice > afterPrice) {

					priceDiffDisplay = afterPrice - beforePrice;
					priceDiffDisplay = priceDiffDisplay.toFixed(2);
					record.set("price", "<font color='red'>" + afterPrice
							+ " ( " + priceDiffDisplay + " ) </font>");
					// 提交，去掉修改標記
					record.commit();

				} else if (afterPrice > beforePrice) {

					priceDiffDisplay = "+"
							+ (afterPrice - beforePrice).toFixed(2);
					record.set("price", "<font color='green'>" + afterPrice
							+ " ( " + priceDiffDisplay + " ) </font>");
					// 提交，去掉修改標記
					record.commit();

				}

				var amountDiffDisplay = "";
				if (beforeAmount > afterAmount) {

					amountDiffDisplay = afterAmount - beforeAmount;
					amountDiffDisplay = amountDiffDisplay.toFixed(2);
					record.set("amount", "<font color='red'>" + afterAmount
							+ " ( " + amountDiffDisplay + " ) </font>");
					// 提交，去掉修改標記
					record.commit();

				} else if (afterAmount > beforeAmount) {

					amountDiffDisplay = "+"
							+ (afterAmount - beforeAmount).toFixed(2);
					record.set("amount", "<font color='green'>" + afterAmount
							+ " ( " + amountDiffDisplay + " ) </font>");
					// 提交，去掉修改標記
					record.commit();

				}

			});
		}
	}
});

// 2，栏位模型
var checkStatDetailResultColumnModel = new Ext.grid.ColumnModel([
		new Ext.grid.RowNumberer(), {
			header : "食材",
			sortable : true,
			dataIndex : "materialName",
			width : 80
		}, {
			header : "日期",
			sortable : true,
			dataIndex : "date",
			width : 120
		}, {
			header : "部门",
			sortable : true,
			dataIndex : "deptName",
			width : 80
		}, {
			header : "经手人",
			sortable : true,
			dataIndex : "operator",
			width : 80
		}, {
			header : "盘点前价格",
			sortable : true,
			dataIndex : "pricePrevious",
			width : 80
		}, {
			header : "盘点前数量",
			sortable : true,
			dataIndex : "amountPrevious",
			width : 80
		}, {
			header : "盘点后价格",
			sortable : true,
			dataIndex : "price",
			width : 80
		}, {
			header : "盘点后数量",
			sortable : true,
			dataIndex : "amount",
			width : 80
		} ]);

var checkStatDetailResultGrid = new Ext.grid.GridPanel({
	xtype : "grid",
	anchor : "99%",
	border : false,
	ds : checkStatDetailResultStore,
	cm : checkStatDetailResultColumnModel,
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

var checkStatDetailResultWin = new Ext.Window({
	title : "盘点明细",
	width : 900,
	height : 370,
	closeAction : "hide",
	resizable : false,
	layout : "fit",
	items : checkStatDetailResultGrid,
	buttons : [ {
		text : "打印",
		handler : function() {

		}
	}, {
		text : "关闭",
		handler : function() {
			isPrompt = false;
			checkStatDetailResultWin.hide();
		}
	} ],
	listeners : {
		"hide" : function(thiz) {
			isPrompt = false;
		}
	}
});

// 结果框 -- 匯總
// 前台：[食材 日期 盘点前价格 盘点前数量 盘点前合计 盘点后价格 盘点后数量 盘点后合计 损益 盈亏]
var checkStatSumResultStore = new Ext.data.Store({
	proxy : new Ext.data.MemoryProxy(checkStatSumResultData),
	reader : new Ext.data.ArrayReader({}, [ {
		name : "materialID"
	}, {
		name : "materialName"
	}, {
		name : "date"
	}, {
		name : "pricePrevious"
	}, {
		name : "amountPrevious"
	}, {
		name : "totalPrevious"
	}, {
		name : "price"
	}, {
		name : "amount"
	}, {
		name : "total"
	}, {
		name : "amountDiff"
	}, {
		name : "totalDiff"
	}, {
		name : "message"
	} ]),
	listeners : {
		"load" : function(thiz, records, options) {
			thiz.each(function(record) {
				var beforePrice = parseFloat(record.get("pricePrevious"));
				var afterPrice = parseFloat(record.get("price"));
				var beforeAmount = parseFloat(record.get("amountPrevious"));
				var afterAmount = parseFloat(record.get("amount"));

				record.set("totalPrevious", (beforePrice * beforeAmount)
						.toFixed(2));
				record.set("total", (afterPrice * afterAmount).toFixed(2));

				var amountDiff = afterAmount - beforeAmount;
				amountDiff = amountDiff.toFixed(2);
				var totalDiff = (afterPrice * afterAmount).toFixed(2)
						- (beforePrice * beforeAmount).toFixed(2);
				totalDiff = totalDiff.toFixed(2);

				if (totalDiff < 0) {
					record.set("totalDiff", "<font color='red'>￥" + totalDiff
							+ "</font>");

				} else if (totalDiff > 0) {
					record.set("totalDiff", "<font color='green'>￥" + totalDiff
							+ "</font>");
				} else {
					record.set("totalDiff", "￥" + totalDiff);
				}

				if (amountDiff < 0) {
					record.set("amountDiff", "<font color='red'>￥" + amountDiff
							+ "</font>");

				} else if (amountDiff > 0) {
					record.set("amountDiff", "<font color='green'>￥"
							+ amountDiff + "</font>");
				} else {
					record.set("amountDiff", amountDiff);
				}

				// 提交，去掉修改標記
				record.commit();

			});
		}
	}
});

// 2，栏位模型
var checkStatSumResultColumnModel = new Ext.grid.ColumnModel([
		new Ext.grid.RowNumberer(), {
			header : "食材",
			sortable : true,
			dataIndex : "materialName",
			width : 80
		}, {
			header : "日期",
			sortable : true,
			dataIndex : "date",
			width : 120
		}, {
			header : "盘点前价格",
			sortable : true,
			dataIndex : "pricePrevious",
			width : 80
		}, {
			header : "盘点前数量",
			sortable : true,
			dataIndex : "amountPrevious",
			width : 80
		}, {
			header : "盘点前合计",
			sortable : true,
			dataIndex : "totalPrevious",
			width : 80
		}, {
			header : "盘点后价格",
			sortable : true,
			dataIndex : "price",
			width : 80
		}, {
			header : "盘点后数量",
			sortable : true,
			dataIndex : "amount",
			width : 80
		}, {
			header : "盘点后合计",
			sortable : true,
			dataIndex : "total",
			width : 80
		}, {
			header : "损益",
			sortable : true,
			dataIndex : "amountDiff",
			width : 80
		}, {
			header : "盈亏",
			sortable : true,
			dataIndex : "totalDiff",
			width : 80
		} ]);

var checkStatSumResultGrid = new Ext.grid.GridPanel({
	xtype : "grid",
	anchor : "99%",
	border : false,
	ds : checkStatSumResultStore,
	cm : checkStatSumResultColumnModel,
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

var checkStatSumResultWin = new Ext.Window({
	title : "盘点汇总",
	width : 1000,
	height : 370,
	closeAction : "hide",
	resizable : false,
	layout : "fit",
	items : checkStatSumResultGrid,
	buttons : [ {
		text : "打印",
		handler : function() {

		}
	}, {
		text : "关闭",
		handler : function() {
			isPrompt = false;
			checkStatSumResultWin.hide();
		}
	} ],
	listeners : {
		"hide" : function(thiz) {
			isPrompt = false;
		}
	}
});
// --------------------------------------end盤點統計----------------------------------------------
//--------------------------------------- 出貨統計 --------------------------------------------------------
//条件框
var inventoryReturnStatMSDS = new Ext.data.SimpleStore({
	fields : [ "retrunValue", "displayText" ],
	data : []
});

var returnStatSupplierComboStore = new Ext.data.SimpleStore({
	fields : [ "value", "text" ],
	data : []
});

var returnStatSupplierCombo = new Ext.form.ComboBox({
	fieldLabel : "供应商",
	forceSelection : true,
	width : 120,
	value : "全部",
	id : "returnStatSupplierCombo",
	store : returnStatSupplierComboStore,
	valueField : "value",
	displayField : "text",
	typeAhead : true,
	mode : "local",
	triggerAction : "all",
	selectOnFocus : true,
	allowBlank : false
});

var inventoryReturnStatFrom = new Ext.form.FormPanel({
	border : false,
	anchor : "right 8%",
	id : "ReturnStatForm",
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
				name : 'ReturnStat',
				inputValue : 'detail'
			} ]
		}, {
			layout : "form",
			border : false,
			labelSeparator : '',
			width : 110,
			items : [ {
				xtype : 'radio',
				hideLabel : true,
				boxLabel : "按食材汇总",
				// checked : true,
				name : 'ReturnStat',
				inputValue : 'sumByMaterial'
			} ]
		}, {
			layout : "form",
			border : false,
			labelSeparator : '',
			width : 110,
			items : [ {
				xtype : 'radio',
				hideLabel : true,
				boxLabel : "按部门汇总",
				// checked : true,
				name : 'ReturnStat',
				inputValue : 'sumByDept'
			} ]
		}, {
			layout : "form",
			border : false,
			labelSeparator : '',
			width : 110,
			items : [ {
				xtype : 'radio',
				hideLabel : true,
				boxLabel : "按供应商汇总",
				// checked : true,
				name : 'ReturnStat',
				inputValue : 'sumBySupplier'
			} ]
		} ]
	} ]
});

inventoryReturnStatWin = new Ext.Window(
		{
			title : "出货统计",
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
							xtype : "datefield",
							id : "begDateReturnStat",
							width : 120,
							fieldLabel : "日期"
						} ]
					}, {
						layout : "form",
						border : false,
						labelSeparator : ' ',
						width : 200,
						labelWidth : 50,
						items : [ {
							xtype : "datefield",
							id : "endDateReturnStat",
							width : 120,
							fieldLabel : "至"
						} ]
					} ]
				} ]
			}, {
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
						labelSeparator : '：',
						width : 300,
						labelWidth : 50,
						items : returnStatSupplierCombo
					} ]
				} ]
			}, {
				layout : "column",
				border : false,
				anchor : "99% 15%",
				autoScroll : true,
				frame : true,
				items : [ {
					layout : "form",
					border : false,
					labelSeparator : '',
					width : 80,
					labelWidth : 40,
					items : [ {
						xtype : "checkbox",
						id : "dept1ReturnStat"
					// fieldLabel : departmentData[0][1]
					} ]
				}, {
					layout : "form",
					border : false,
					labelSeparator : '',
					width : 80,
					labelWidth : 40,
					items : [ {
						xtype : "checkbox",
						id : "dept2ReturnStat"
					// fieldLabel : departmentData[1][1]
					} ]
				}, {
					layout : "form",
					border : false,
					labelSeparator : '',
					width : 80,
					labelWidth : 40,
					items : [ {
						xtype : "checkbox",
						id : "dept3ReturnStat"
					// fieldLabel : departmentData[2][1]
					} ]
				}, {
					layout : "form",
					border : false,
					labelSeparator : '',
					width : 80,
					labelWidth : 40,
					items : [ {
						xtype : "checkbox",
						id : "dept4ReturnStat"
					// fieldLabel : departmentData[3][1]
					} ]
				}, {
					layout : "form",
					border : false,
					labelSeparator : '',
					width : 80,
					labelWidth : 40,
					items : [ {
						xtype : "checkbox",
						id : "dept5ReturnStat"
					// fieldLabel : departmentData[4][1]
					} ]
				}, {
					layout : "form",
					border : false,
					labelSeparator : '',
					width : 80,
					labelWidth : 40,
					items : [ {
						xtype : "checkbox",
						id : "dept6ReturnStat"
					// fieldLabel : departmentData[5][1]
					} ]
				}, {
					layout : "form",
					border : false,
					labelSeparator : '',
					width : 80,
					labelWidth : 40,
					items : [ {
						xtype : "checkbox",
						id : "dept7ReturnStat"
					// fieldLabel : departmentData[6][1]
					} ]
				}, {
					layout : "form",
					border : false,
					labelSeparator : '',
					width : 80,
					labelWidth : 40,
					items : [ {
						xtype : "checkbox",
						id : "dept8ReturnStat"
					// fieldLabel : departmentData[7][1]
					} ]
				}, {
					layout : "form",
					border : false,
					labelSeparator : '',
					width : 80,
					labelWidth : 40,
					items : [ {
						xtype : "checkbox",
						id : "dept9ReturnStat"
					// fieldLabel : departmentData[8][1]
					} ]
				}, {
					layout : "form",
					border : false,
					labelSeparator : '',
					width : 80,
					labelWidth : 40,
					items : [ {
						xtype : "checkbox",
						id : "dept10ReturnStat"
					// fieldLabel : departmentData[9][1]
					} ]
				} ]
			}, {
				layout : "form",
				border : false,
				frame : true,
				hideLabels : true,
				anchor : "right 56%",
				items : [ {
					xtype : "itemselector",
					name : "materialReturnStatMultSelect",
					id : "materialReturnStatMultSelect",
					fromStore : inventoryReturnStatMSDS,
					dataFields : [ "retrunValue", "displayText" ],
					toData : [ [ "", "" ] ],
					msHeight : 173,
					valueField : "retrunValue",
					displayField : "displayText",
					imagePath : "../../extjs/multiselect/images/",
					toLegend : "已选择食材",
					fromLegend : "可选择食材"
				} ]
			}, inventoryReturnStatFrom ],
			buttons : [
					{
						text : "全选",
						handler : function() {
							var i = inventoryReturnStatWin
									.findById("materialReturnStatMultSelect").fromMultiselect.view;
							i.selectRange(0, i.store.length);
							inventoryReturnStatWin.findById(
									"materialReturnStatMultSelect").fromTo();
							inventoryReturnStatWin
									.findById("materialReturnStatMultSelect").toMultiselect.view
									.clearSelections();
						}
					},
					{
						text : "清空",
						handler : function() {
							inventoryReturnStatWin.findById(
									"materialReturnStatMultSelect").reset();
						}
					},
					{
						text : "确定",
						handler : function() {

							var selectCount = inventoryReturnStatWin
									.findById("materialReturnStatMultSelect").toMultiselect.store
									.getCount();

							if (selectCount != 0) {
								isPrompt = false;
								inventoryReturnStatWin.hide();

								// -- 獲取選擇的食材 --
								var selectMaterials = "";
								for ( var i = 0; i < selectCount; i++) {
									var selectItem = inventoryReturnStatWin
											.findById("materialReturnStatMultSelect").toMultiselect.store
											.getAt(i).get("retrunValue");
									// if (selectItem != "") {
									selectMaterials = selectMaterials
											+ selectItem + ",";
									// }
								}
								// 去掉最后一个逗号
								selectMaterials = selectMaterials.substring(0,
										selectMaterials.length - 1);
								if (selectMaterials.substring(0, 1) == ",") {
									selectMaterials = selectMaterials
											.substring(1,
													selectMaterials.length);
								}

								// -- 獲取時間 --
								var beginDate = inventoryReturnStatWin.findById(
										"begDateReturnStat").getValue();
								if (beginDate != "") {
									var dateFormated = new Date();
									dateFormated = beginDate;
									beginDate = dateFormated.format('Y-m-d');
								}

								var endDate = inventoryReturnStatWin.findById(
										"endDateReturnStat").getValue();
								if (endDate != "") {
									var dateFormated = new Date();
									dateFormated = endDate;
									endDate = dateFormated.format('Y-m-d');
								}

								// -- 獲取供應商 --
								var supplier = returnStatSupplierCombo.getValue();
								if (supplier == "全部") {
									supplier = "-1";
								}

								// -- 獲取部門 --
								var departments = "";
								if (inventoryReturnStatWin.findById("dept1ReturnStat")
										.getValue() == true) {
									departments = departments + "0,";
								}
								if (inventoryReturnStatWin.findById("dept2ReturnStat")
										.getValue() == true) {
									departments = departments + "1,";
								}
								if (inventoryReturnStatWin.findById("dept3ReturnStat")
										.getValue() == true) {
									departments = departments + "2,";
								}
								if (inventoryReturnStatWin.findById("dept4ReturnStat")
										.getValue() == true) {
									departments = departments + "3,";
								}
								if (inventoryReturnStatWin.findById("dept5ReturnStat")
										.getValue() == true) {
									departments = departments + "4,";
								}
								if (inventoryReturnStatWin.findById("dept6ReturnStat")
										.getValue() == true) {
									departments = departments + "5,";
								}
								if (inventoryReturnStatWin.findById("dept7ReturnStat")
										.getValue() == true) {
									departments = departments + "6,";
								}
								if (inventoryReturnStatWin.findById("dept8ReturnStat")
										.getValue() == true) {
									departments = departments + "7,";
								}
								if (inventoryReturnStatWin.findById("dept9ReturnStat")
										.getValue() == true) {
									departments = departments + "8,";
								}
								if (inventoryReturnStatWin.findById("dept10ReturnStat")
										.getValue() == true) {
									departments = departments + "9,";
								}

								if (departments != "") {
									departments = departments.substring(0,
											departments.length - 1);
								}

								// -- 獲取統計類型 --
								var staticType = inventoryReturnStatFrom.getForm()
										.findField("ReturnStat").getGroupValue();
								if (staticType == "detail") {
									isPrompt = true;
									returnStatDetailResultWin.show();

									// ＃＃＃＃＃＃＃＃＃＃＃＃明細統計＃＃＃＃＃＃＃＃＃＃＃＃
									Ext.Ajax
											.request({
												url : "../../InventoryReturnStatDetail.do",
												params : {
													"pin" : pin,
													"beginDate" : beginDate,
													"endDate" : endDate,
													"supplier" : supplier,
													"departments" : departments,
													"materials" : selectMaterials
												},
												success : function(response,
														options) {
													var resultJSON = Ext.util.JSON
															.decode(response.responseText);
													// 格式：[食材id，日期，供應商id，經辦人，部門id，價格，數量，小計]
													var rootData = resultJSON.root;
													if (rootData[0].message == "normal") {
														returnStatDetailResultData.length = 0;
														for ( var i = 0; i < rootData.length; i++) {
															var materialN = "";
															for ( var j = 0; j < materialData.length; j++) {
																if (materialData[j][0] == rootData[i].materialID) {
																	materialN = materialData[j][2];
																}
															}
															var supplierN = "";
															for ( var j = 0; j < supplierData.length; j++) {
																if (supplierData[j][0] == rootData[i].supplierID) {
																	supplierN = supplierData[j][2];
																}
															}
															var deptN = "";
															for ( var j = 0; j < departmentData.length; j++) {
																if (departmentData[j][0] == rootData[i].departmentID) {
																	deptN = departmentData[j][1];
																}
															}

															returnStatDetailResultData
																	.push([
																			rootData[i].materialID,
																			materialN,
																			rootData[i].date,
																			rootData[i].supplierID,
																			supplierN,
																			rootData[i].operator,
																			rootData[i].departmentID,
																			deptN,
																			rootData[i].price,
																			rootData[i].amount,
																			rootData[i].total

																	]);
														}

														returnStatDetailResultStore
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

								} else if (staticType == "sumByMaterial") {
									isPrompt = true;
									returnStatByMaterialResultWin.show();

									// ＃＃＃＃＃＃＃＃＃＃＃＃按食材統計＃＃＃＃＃＃＃＃＃＃＃＃
									Ext.Ajax
											.request({
												url : "../../InventoryReturnStatByMaterial.do",
												params : {
													"pin" : pin,
													"beginDate" : beginDate,
													"endDate" : endDate,
													"supplier" : supplier,
													"departments" : departments,
													"materials" : selectMaterials
												},
												success : function(response,
														options) {
													var resultJSON = Ext.util.JSON
															.decode(response.responseText);

													var rootData = resultJSON.root;
													if (rootData[0].message == "normal") {

														returnStatByMaterialResultData = rootData;
														returnStatByMateriaGrid
																.getStore()
																.loadData(
																		returnStatByMaterialResultData);
														if (rootData[0].materialID == "NO_DATA") {
															returnStatByMateriaGrid
																	.getStore()
																	.removeAll();
														}

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

								} else if (staticType == "sumByDept") {

									isPrompt = true;
									returnStatByDeptResultWin.show();

									// ＃＃＃＃＃＃＃＃＃＃＃＃按部門統計＃＃＃＃＃＃＃＃＃＃＃＃
									Ext.Ajax
											.request({
												url : "../../InventoryReturnStatByDept.do",
												params : {
													"pin" : pin,
													"beginDate" : beginDate,
													"endDate" : endDate,
													"supplier" : supplier,
													"departments" : departments,
													"materials" : selectMaterials
												},
												success : function(response,
														options) {
													var resultJSON = Ext.util.JSON
															.decode(response.responseText);

													var rootData = resultJSON.root;
													if (rootData[0].message == "normal") {

														returnStatByDeptResultData = rootData;
														returnStatByDeptGrid
																.getStore()
																.loadData(
																		returnStatByDeptResultData);
														if (rootData[0].materialID == "NO_DATA") {
															returnStatByDeptGrid
																	.getStore()
																	.removeAll();
														}

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

								} else if (staticType == "sumBySupplier") {

									isPrompt = true;
									returnStatBySupplierResultWin.show();

									// ＃＃＃＃＃＃＃＃＃＃＃＃按供應商統計＃＃＃＃＃＃＃＃＃＃＃＃
									Ext.Ajax
											.request({
												url : "../../InventoryReturnStatBySupplier.do",
												params : {
													"pin" : pin,
													"beginDate" : beginDate,
													"endDate" : endDate,
													"supplier" : supplier,
													"departments" : departments,
													"materials" : selectMaterials
												},
												success : function(response,
														options) {
													var resultJSON = Ext.util.JSON
															.decode(response.responseText);

													var rootData = resultJSON.root;
													if (rootData[0].message == "normal") {

														returnStatBySupplierResultData = rootData;
														returnStatBySupplierGrid
																.getStore()
																.loadData(
																		returnStatBySupplierResultData);
														if (rootData[0].materialID == "NO_DATA") {
															returnStatBySupplierGrid
																	.getStore()
																	.removeAll();
														}

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

							} else {
								Ext.MessageBox.show({
									msg : "至少需要选择一个食材进行统计",
									width : 300,
									buttons : Ext.MessageBox.OK
								});
							}
						}
					}, {
						text : "取消",
						handler : function() {
							isPrompt = false;
							inventoryReturnStatWin.hide();
						}
					} ],
			listeners : {
				"show" : function(thiz) {

					inventoryReturnStatWin.findById("materialReturnStatMultSelect")
							.reset();
					inventoryReturnStatWin.findById("begDateReturnStat").setValue("");
					inventoryReturnStatWin.findById("endDateReturnStat").setValue("");

					returnStatSupplierCombo.setValue("全部");

					// inventoryInStatMSDS.loadData(materialComboData);
					returnStatSupplierComboStore.loadData(supplierComboData);

					// 神技！動態改變form中component的label！！！
					inventoryReturnStatWin.findById("dept1ReturnStat").el.parent()
							.parent().parent().first().dom.innerHTML = departmentData[0][1]
							+ ":";
					inventoryReturnStatWin.findById("dept2ReturnStat").el.parent()
							.parent().parent().first().dom.innerHTML = departmentData[1][1]
							+ ":";
					inventoryReturnStatWin.findById("dept3ReturnStat").el.parent()
							.parent().parent().first().dom.innerHTML = departmentData[2][1]
							+ ":";
					inventoryReturnStatWin.findById("dept4ReturnStat").el.parent()
							.parent().parent().first().dom.innerHTML = departmentData[3][1]
							+ ":";
					inventoryReturnStatWin.findById("dept5ReturnStat").el.parent()
							.parent().parent().first().dom.innerHTML = departmentData[4][1]
							+ ":";
					inventoryReturnStatWin.findById("dept6ReturnStat").el.parent()
							.parent().parent().first().dom.innerHTML = departmentData[5][1]
							+ ":";
					inventoryReturnStatWin.findById("dept7ReturnStat").el.parent()
							.parent().parent().first().dom.innerHTML = departmentData[6][1]
							+ ":";
					inventoryReturnStatWin.findById("dept8ReturnStat").el.parent()
							.parent().parent().first().dom.innerHTML = departmentData[7][1]
							+ ":";
					inventoryReturnStatWin.findById("dept9ReturnStat").el.parent()
							.parent().parent().first().dom.innerHTML = departmentData[8][1]
							+ ":";
					inventoryReturnStatWin.findById("dept10ReturnStat").el.parent()
							.parent().parent().first().dom.innerHTML = departmentData[9][1]
							+ ":";

					inventoryReturnStatWin.findById("dept1ReturnStat").setValue(false);
					inventoryReturnStatWin.findById("dept2ReturnStat").setValue(false);
					inventoryReturnStatWin.findById("dept3ReturnStat").setValue(false);
					inventoryReturnStatWin.findById("dept4ReturnStat").setValue(false);
					inventoryReturnStatWin.findById("dept5ReturnStat").setValue(false);
					inventoryReturnStatWin.findById("dept6ReturnStat").setValue(false);
					inventoryReturnStatWin.findById("dept7ReturnStat").setValue(false);
					inventoryReturnStatWin.findById("dept8ReturnStat").setValue(false);
					inventoryReturnStatWin.findById("dept9ReturnStat").setValue(false);
					inventoryReturnStatWin.findById("dept10ReturnStat").setValue(false);

					inventoryReturnStatFrom.getForm().findField("ReturnStat").setValue(
							"detail");

				},
				"hide" : function(thiz) {
					isPrompt = false;
				}
			}
		});

//结果框 -- 明細
//前台：[食材 日期 供应商 经手人 部门 价格 数量 小计]
var returnStatDetailResultStore = new Ext.data.Store({
	proxy : new Ext.data.MemoryProxy(returnStatDetailResultData),
	reader : new Ext.data.ArrayReader({}, [ {
		name : "materialID"
	}, {
		name : "materialName"
	}, {
		name : "date"
	}, {
		name : "supplierID"
	}, {
		name : "supplierName"
	}, {
		name : "operator"
	}, {
		name : "departmentID"
	}, {
		name : "departmentName"
	}, {
		name : "price"
	}, {
		name : "amount"
	}, {
		name : "total"
	}, {
		name : "message"
	} ])
});

//2，栏位模型
var returnStatDetailResultColumnModel = new Ext.grid.ColumnModel([
		new Ext.grid.RowNumberer(), {
			header : "食材",
			sortable : true,
			dataIndex : "materialName",
			width : 80
		}, {
			header : "日期",
			sortable : true,
			dataIndex : "date",
			width : 80
		}, {
			header : "供应商",
			sortable : true,
			dataIndex : "supplierName",
			width : 80
		}, {
			header : "经手人",
			sortable : true,
			dataIndex : "operator",
			width : 80
		}, {
			header : "部门",
			sortable : true,
			dataIndex : "departmentName",
			width : 80
		}, {
			header : "价钱",
			sortable : true,
			dataIndex : "price",
			width : 80
		}, {
			header : "数量",
			sortable : true,
			dataIndex : "amount",
			width : 80
		}, {
			header : "小计（￥）",
			sortable : true,
			dataIndex : "total",
			width : 80
		} ]);

var returnStatDetailResultGrid = new Ext.grid.GridPanel({
	xtype : "grid",
	anchor : "99%",
	border : false,
	ds : returnStatDetailResultStore,
	cm : returnStatDetailResultColumnModel,
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

var returnStatDetailResultWin = new Ext.Window({
	title : "出货明细",
	width : 800,
	height : 370,
	closeAction : "hide",
	resizable : false,
	layout : "fit",
	items : returnStatDetailResultGrid,
	buttons : [ {
		text : "打印",
		handler : function() {

		}
	}, {
		text : "关闭",
		handler : function() {
			isPrompt = false;
			returnStatDetailResultWin.hide();
		}
	} ],
	listeners : {
		"hide" : function(thiz) {
			isPrompt = false;
		}
	}
});

//结果框 -- 按食材
//--------------------------------------------------------------------------------------------------------
var returnStatByMateriaReader = new Ext.data.JsonReader({
	idProperty : 'groupID',
	fields : [ {
		name : 'materialID',
		type : 'int'
	}, {
		name : 'materialName',
		type : 'string'
	}, {
		name : 'groupID',
		type : 'int'
	}, {
		name : 'groupDescr',
		type : 'string'
	}, {
		name : 'singlePrice',
		type : 'float'
	}, {
		name : 'amount',
		type : 'float'
	}, {
		name : 'sumPrice',
		type : 'float'
	}, {
		name : 'deptID'
	}, {
		name : 'deptName',
		type : 'string'
	} ]

});


var returnStatByMateriaSummary = new Ext.grid.GroupSummary();

var returnStatByMateriaGrid = new Ext.grid.EditorGridPanel({
	ds : new Ext.data.GroupingStore({
		reader : returnStatByMateriaReader,
		data : [],
		sortInfo : {
			field : 'deptName',
			direction : "ASC"
		},
		groupField : 'materialName'
	}),

	columns : [ {
		id : 'groupDescr',
		header : "食材",
		width : 20,
		sortable : true,
		dataIndex : 'groupDescr',
		// summaryType : 'count',
		hideable : false,
		// summaryRenderer : function(v, params, data) {
		// return ((v === 0 || v > 1) ? '(' + v + ' Tasks)' : '(1 Task)');
		// }
		summaryRenderer : function(v, params, data) {
			return "合计";
		}
	}, {
		header : "NOT SHOW",
		width : 20,
		sortable : false,
		dataIndex : 'materialName'
	}, {
		header : "部门",
		width : 25,
		sortable : true,
		dataIndex : 'deptName'
	}, {
		id : 'singlePrice',
		header : "价格",
		width : 20,
		sortable : false, // 計出來的列排序不起作用
		groupable : false,
		renderer : function(v, params, record) {
			// return Ext.util.Format.usMoney(record.data.sumPrice
			// / record.data.amount);
			if (record.data.amount != 0) {
				return (record.data.sumPrice / record.data.amount).toFixed(2);
			} else {
				return 0.00;
			}
		},
		dataIndex : 'singlePrice',
		summaryType : 'singlePrice'
	// summaryRenderer : Ext.util.Format.usMoney
	}, {
		header : "数量",
		width : 20,
		sortable : true,
		// renderer : Ext.util.Format.usMoney,
		dataIndex : 'amount',
		summaryType : 'sum'
	// editor: new Ext.form.NumberField({
	// allowBlank: false,
	// allowNegative: false,
	// style: 'text-align:left'
	// })
	}, {
		header : "小计",
		width : 20,
		sortable : true,
		// renderer : Ext.util.Format.chnMoney,
		dataIndex : 'sumPrice',
		summaryType : 'sum',
		renderer : function(v) {
			return parseFloat(v).toFixed(2);
		}
	// editor : new Ext.form.NumberField({
	// allowBlank : false,
	// allowNegative : false,
	// style : 'text-align:left'
	// })
	} ],

	view : new Ext.grid.GroupingView({
		forceFit : true,
		showGroupName : false,
		enableNoGroups : false, // REQUIRED!
		hideGroupedColumn : true
	}),

	plugins : returnStatByMateriaSummary,

	frame : true,
	width : 800,
	height : 450,
	clicksToEdit : 1,
	collapsible : true,
	animCollapse : false,
	trackMouseOver : false
//enableColumnMove: false,
//title : 'Sponsored Projects'
//iconCls: 'icon-grid',
//renderTo: document.body
});

//--------------------------------------------------------------------------------------------------------

var returnStatByMaterialResultWin = new Ext.Window({
	title : "出货汇总-按食材",
	width : 400,
	height : 500,
	closeAction : "hide",
	resizable : false,
	layout : "fit",
	items : returnStatByMateriaGrid,
	buttons : [ {
		text : "打印",
		handler : function() {

		}
	}, {
		text : "关闭",
		handler : function() {
			isPrompt = false;
			returnStatByMaterialResultWin.hide();
		}
	} ],
	listeners : {
		"hide" : function(thiz) {
			isPrompt = false;
		}
	}
});

//结果框 -- 按部門
//--------------------------------------------------------------------------------------------------------
var returnStatByDeptReader = new Ext.data.JsonReader({
	idProperty : 'groupID',
	fields : [ {
		name : 'deptID',
		type : 'int'
	}, {
		name : 'deptName',
		type : 'string'
	}, {
		name : 'groupID',
		type : 'int'
	}, {
		name : 'groupDescr',
		type : 'string'
	}, {
		name : 'singlePrice',
		type : 'float'
	}, {
		name : 'amount',
		type : 'float'
	}, {
		name : 'sumPrice',
		type : 'float'
	}, {
		name : 'materialID'
	}, {
		name : 'materialName',
		type : 'string'
	} ]

});

var returnStatByDeptSummary = new Ext.grid.GroupSummary();

var returnStatByDeptGrid = new Ext.grid.EditorGridPanel({
	ds : new Ext.data.GroupingStore({
		reader : returnStatByDeptReader,
		data : [],
		sortInfo : {
			field : 'materialName',
			direction : "ASC"
		},
		groupField : 'deptName'
	}),

	columns : [ {
		id : 'groupDescr',
		header : "部门",
		width : 20,
		sortable : true,
		dataIndex : 'groupDescr',
		// summaryType : 'count',
		hideable : false,
		// summaryRenderer : function(v, params, data) {
		// return ((v === 0 || v > 1) ? '(' + v + ' Tasks)' : '(1 Task)');
		// }
		summaryRenderer : function(v, params, data) {
			return "合计";
		}
	}, {
		header : "NOT SHOW",
		width : 20,
		sortable : false,
		dataIndex : 'deptName'
	}, {
		header : "食材",
		width : 25,
		sortable : true,
		dataIndex : 'materialName'
	}, {
		id : 'singlePrice',
		header : "价格",
		width : 20,
		sortable : false, // 計出來的列排序不起作用
		groupable : false,
		renderer : function(v, params, record) {
			// return Ext.util.Format.usMoney(record.data.sumPrice
			// / record.data.amount);
			if (record.data.amount != 0) {
				return (record.data.sumPrice / record.data.amount).toFixed(2);
			} else {
				return 0.00;
			}
		},
		dataIndex : 'singlePrice',
		summaryType : 'singlePrice'
	// summaryRenderer : Ext.util.Format.usMoney
	}, {
		header : "数量",
		width : 20,
		sortable : true,
		// renderer : Ext.util.Format.usMoney,
		dataIndex : 'amount',
		summaryType : 'sum'
	// editor: new Ext.form.NumberField({
	// allowBlank: false,
	// allowNegative: false,
	// style: 'text-align:left'
	// })
	}, {
		header : "小计",
		width : 20,
		sortable : true,
		// renderer : Ext.util.Format.chnMoney,
		dataIndex : 'sumPrice',
		summaryType : 'sum',
		renderer : function(v) {
			return parseFloat(v).toFixed(2);
		}
	// editor : new Ext.form.NumberField({
	// allowBlank : false,
	// allowNegative : false,
	// style : 'text-align:left'
	// })
	} ],

	view : new Ext.grid.GroupingView({
		forceFit : true,
		showGroupName : false,
		enableNoGroups : false, // REQUIRED!
		hideGroupedColumn : true
	}),

	plugins : returnStatByDeptSummary,

	frame : true,
	width : 800,
	height : 450,
	clicksToEdit : 1,
	collapsible : true,
	animCollapse : false,
	trackMouseOver : false
//enableColumnMove: false,
//title : 'Sponsored Projects'
//iconCls: 'icon-grid',
//renderTo: document.body
});

//--------------------------------------------------------------------------------------------------------

var returnStatByDeptResultWin = new Ext.Window({
	title : "出货汇总-按部门",
	width : 400,
	height : 500,
	closeAction : "hide",
	resizable : false,
	layout : "fit",
	items : returnStatByDeptGrid,
	buttons : [ {
		text : "打印",
		handler : function() {

		}
	}, {
		text : "关闭",
		handler : function() {
			isPrompt = false;
			returnStatByDeptResultWin.hide();
		}
	} ],
	listeners : {
		"hide" : function(thiz) {
			isPrompt = false;
		}
	}
});

//结果框 -- 按供應商
//--------------------------------------------------------------------------------------------------------
var returnStatBySupplierReader = new Ext.data.JsonReader({
	idProperty : 'groupID',
	fields : [ {
		name : 'supplierID',
		type : 'int'
	}, {
		name : 'supplierName',
		type : 'string'
	}, {
		name : 'groupID',
		type : 'int'
	}, {
		name : 'groupDescr',
		type : 'string'
	}, {
		name : 'singlePrice',
		type : 'float'
	}, {
		name : 'amount',
		type : 'float'
	}, {
		name : 'sumPrice',
		type : 'float'
	}, {
		name : 'materialID'
	}, {
		name : 'materialName',
		type : 'string'
	} ]

});

var returnStatBySupplierSummary = new Ext.grid.GroupSummary();

var returnStatBySupplierGrid = new Ext.grid.EditorGridPanel({
	ds : new Ext.data.GroupingStore({
		reader : returnStatBySupplierReader,
		data : [],
		sortInfo : {
			field : 'materialName',
			direction : "ASC"
		},
		groupField : 'supplierName'
	}),

	columns : [ {
		id : 'groupDescr',
		header : "供应商",
		width : 20,
		sortable : true,
		dataIndex : 'groupDescr',
		// summaryType : 'count',
		hideable : false,
		// summaryRenderer : function(v, params, data) {
		// return ((v === 0 || v > 1) ? '(' + v + ' Tasks)' : '(1 Task)');
		// }
		summaryRenderer : function(v, params, data) {
			return "合计";
		}
	}, {
		header : "NOT SHOW",
		width : 20,
		sortable : false,
		dataIndex : 'supplierName'
	}, {
		header : "食材",
		width : 25,
		sortable : true,
		dataIndex : 'materialName'
	}, {
		id : 'singlePrice',
		header : "价格",
		width : 20,
		sortable : false, // 計出來的列排序不起作用
		groupable : false,
		renderer : function(v, params, record) {
			// return Ext.util.Format.usMoney(record.data.sumPrice
			// / record.data.amount);
			if (record.data.amount != 0) {
				return (record.data.sumPrice / record.data.amount).toFixed(2);
			} else {
				return 0.00;
			}
		},
		dataIndex : 'singlePrice',
		summaryType : 'singlePrice'
	// summaryRenderer : Ext.util.Format.usMoney
	}, {
		header : "数量",
		width : 20,
		sortable : true,
		// renderer : Ext.util.Format.usMoney,
		dataIndex : 'amount',
		summaryType : 'sum'
	// editor: new Ext.form.NumberField({
	// allowBlank: false,
	// allowNegative: false,
	// style: 'text-align:left'
	// })
	}, {
		header : "小计",
		width : 20,
		sortable : true,
		// renderer : Ext.util.Format.chnMoney,
		dataIndex : 'sumPrice',
		summaryType : 'sum',
		renderer : function(v) {
			return parseFloat(v).toFixed(2);
		}
	// editor : new Ext.form.NumberField({
	// allowBlank : false,
	// allowNegative : false,
	// style : 'text-align:left'
	// })
	} ],

	view : new Ext.grid.GroupingView({
		forceFit : true,
		showGroupName : false,
		enableNoGroups : false, // REQUIRED!
		hideGroupedColumn : true
	}),

	plugins : returnStatBySupplierSummary,

	frame : true,
	width : 800,
	height : 450,
	clicksToEdit : 1,
	collapsible : true,
	animCollapse : false,
	trackMouseOver : false
//enableColumnMove: false,
//title : 'Sponsored Projects'
//iconCls: 'icon-grid',
//renderTo: document.body
});

//--------------------------------------------------------------------------------------------------------

var returnStatBySupplierResultWin = new Ext.Window({
	title : "出货汇总-按供应商",
	width : 400,
	height : 500,
	closeAction : "hide",
	resizable : false,
	layout : "fit",
	items : returnStatBySupplierGrid,
	buttons : [ {
		text : "打印",
		handler : function() {

		}
	}, {
		text : "关闭",
		handler : function() {
			isPrompt = false;
			returnStatBySupplierResultWin.hide();
		}
	} ],
	listeners : {
		"hide" : function(thiz) {
			isPrompt = false;
		}
	}
});

//--------------------------------------end出貨統計----------------------------------------------
