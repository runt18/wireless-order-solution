//----------------- 入庫統計 --------------------
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

inventoryInStatWin = new Ext.Window(
		{
			title : "入库统计",
			width : 450,
			height : 430,
			closeAction : "hide",
			resizable : false,
			layout : "anchor",
			items : [
					{
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
					},
					{
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
					},
					{
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
					},
					{
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
					},
					{
						layout : "column",
						border : false,
						anchor : "right 10%",
						frame : true,
						items : [
								{
									layout : "form",
									border : false,
									labelSeparator : '',
									width : 70,
									labelWidth : 30,
									items : [ {
										xtype : "checkbox",
										id : "detailInStat",
										fieldLabel : "明细",
										listeners : {
											"check" : function(thiz, checked) {
												if (checked) {
													inventoryInStatWin
															.findById(
																	"sumByMaterialInStat")
															.setValue(false);
													inventoryInStatWin
															.findById(
																	"sumByDeptInStat")
															.setValue(false);
													inventoryInStatWin
															.findById(
																	"sumBySuplierInStat")
															.setValue(false);
												}
											}
										}
									} ]
								},
								{
									layout : "form",
									border : false,
									labelSeparator : '',
									width : 110,
									labelWidth : 70,
									items : [ {
										xtype : "checkbox",
										id : "sumByMaterialInStat",
										fieldLabel : "按食材汇总",
										listeners : {
											"check" : function(thiz, checked) {
												if (checked) {
													inventoryInStatWin
															.findById(
																	"detailInStat")
															.setValue(false);
													inventoryInStatWin
															.findById(
																	"sumByDeptInStat")
															.setValue(false);
													inventoryInStatWin
															.findById(
																	"sumBySuplierInStat")
															.setValue(false);
												}
											}
										}
									} ]
								},
								{
									layout : "form",
									border : false,
									labelSeparator : '',
									width : 110,
									labelWidth : 70,
									items : [ {
										xtype : "checkbox",
										id : "sumByDeptInStat",
										fieldLabel : "按部门汇总",
										listeners : {
											"check" : function(thiz, checked) {
												if (checked) {
													inventoryInStatWin
															.findById(
																	"detailInStat")
															.setValue(false);
													inventoryInStatWin
															.findById(
																	"sumByMaterialInStat")
															.setValue(false);
													inventoryInStatWin
															.findById(
																	"sumBySuplierInStat")
															.setValue(false);
												}
											}
										}
									} ]
								},
								{
									layout : "form",
									border : false,
									labelSeparator : '',
									width : 110,
									labelWidth : 80,
									items : [ {
										xtype : "checkbox",
										id : "sumBySuplierInStat",
										fieldLabel : "按供应商汇总",
										listeners : {
											"check" : function(thiz, checked) {
												if (checked) {
													inventoryInStatWin
															.findById(
																	"detailInStat")
															.setValue(false);
													inventoryInStatWin
															.findById(
																	"sumByMaterialInStat")
															.setValue(false);
													inventoryInStatWin
															.findById(
																	"sumByDeptInStat")
															.setValue(false);
												}
											}
										}
									} ]
								} ]
					} ],
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
								if (inventoryInStatWin.findById("detailInStat")
										.getValue() == true) {
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

								} else if (inventoryInStatWin.findById(
										"sumByMaterialInStat").getValue() == true) {
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

								} else if (inventoryInStatWin.findById(
										"sumByDeptInStat").getValue() == true) {

								} else if (inventoryInStatWin.findById(
										"sumBySuplierInStat").getValue() == true) {

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

					inventoryInStatMSDS.loadData(materialComboData);
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

					inventoryInStatWin.findById("detailInStat").setValue(true);
					inventoryInStatWin.findById("sumByMaterialInStat")
							.setValue(false);
					inventoryInStatWin.findById("sumByDeptInStat").setValue(
							false);
					inventoryInStatWin.findById("sumBySuplierInStat").setValue(
							false);

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
		text : "退出",
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
//inStatByMaterialResultData = [ {
//	materialID : 100,
//	materialName : '雞肉',
//	groupID : 112,
//	groupDescr : '',
//	// price : 6,
//	amount : 150,
//	deptName : 'department',
//	sumPrice : 1000
//}, {
//	materialID : 100,
//	materialName : '雞肉',
//	groupID : 113,
//	groupDescr : '',
//	// price : 4,
//	amount : 150,
//	deptName : 'department',
//	sumPrice : 1000
//}, {
//	materialID : 100,
//	materialName : '雞肉',
//	groupID : 114,
//	groupDescr : '',
//	// price : 4,
//	amount : 150,
//	deptName : 'department',
//	sumPrice : 1000
//}, {
//	materialID : 100,
//	materialName : '雞肉',
//	groupID : 115,
//	groupDescr : '',
//	// price : 8,
//	amount : 100,
//	deptName : 'department',
//	sumPrice : 1000
//}, {
//	materialID : 101,
//	materialName : '豬肉',
//	groupID : 101,
//	groupDescr : '',
//	// price : 6,
//	amount : 100,
//	deptName : 'department',
//	sumPrice : 1000
//}, {
//	materialID : 101,
//	materialName : '豬肉',
//	groupID : 102,
//	groupDescr : '',
//	// price : 6,
//	amount : 100,
//	deptName : 'department',
//	sumPrice : 1000
//}, {
//	materialID : 101,
//	materialName : '豬肉',
//	groupID : 103,
//	groupDescr : '',
//	// price : 4,
//	amount : 100,
//	deptName : 'department',
//	sumPrice : 1000
//}, {
//	materialID : 101,
//	materialName : '豬肉',
//	groupID : 121,
//	groupDescr : '',
//	// price : 2,
//	amount : 100,
//	deptName : 'department',
//	sumPrice : 1000
//}, {
//	materialID : 101,
//	materialName : '豬肉',
//	groupID : 104,
//	groupDescr : '',
//	// price : 6,
//	amount : 100,
//	deptName : 'department',
//	sumPrice : 1000
//}, {
//	materialID : 102,
//	materialName : '醬油',
//	groupID : 105,
//	groupDescr : '',
//	// price : 4,
//	amount : 125,
//	deptName : 'department',
//	sumPrice : 1000
//}, {
//	materialID : 102,
//	materialName : '醬油',
//	groupID : 106,
//	groupDescr : '',
//	// price : 4,
//	amount : 125,
//	deptName : 'department',
//	sumPrice : 1000
//}, {
//	materialID : 102,
//	materialName : '醬油',
//	groupID : 107,
//	groupDescr : '',
//	// price : 6,
//	amount : 125,
//	deptName : 'department',
//	sumPrice : 1000
//}, {
//	materialID : 102,
//	materialName : '醬油',
//	groupID : 108,
//	groupDescr : '',
//	// price : 4,
//	amount : 125,
//	deptName : 'department',
//	sumPrice : 1000
//}, {
//	materialID : 102,
//	materialName : '醬油',
//	groupID : 109,
//	groupDescr : '',
//	// price : 4,
//	amount : 125,
//	deptName : 'department',
//	sumPrice : 1000
//}, {
//	materialID : 102,
//	materialName : '醬油',
//	groupID : 110,
//	groupDescr : '',
//	// price : 10,
//	amount : 125,
//	deptName : 'department',
//	sumPrice : 1000
//}, {
//	materialID : 102,
//	materialName : '醬油',
//	groupID : 111,
//	groupDescr : '',
//	// price : 8,
//	amount : 125,
//	deptName : 'department',
//	sumPrice : 1000
//} ];

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
	return v + (record.data.sumPrice / record.data.amount);
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
		sortable : true,
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
			return record.data.sumPrice / record.data.amount;
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
		summaryType : 'sum'
	// renderer : function(v) {
	// return v + ' hours';
	// },
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
		text : "退出",
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

// ----------------- end 入庫統計 --------------------
