// 条件框

var analysisCurrFrom = new Ext.form.FormPanel({
	border : false,
	anchor : "right 13%",
	id : "analysisCurrFrom",
	items : [ {
		layout : "column",
		border : false,
		frame : true,
		items : [ {
			layout : "form",
			border : false,
			width : 110,
			items : [ {
				xtype : 'radio',
				hideLabel : true,
				boxLabel : "当前库存",
				checked : true,
				name : 'analysisCurr',
				inputValue : 'current'
			} ]
		}, {
			layout : "form",
			border : false,
			labelSeparator : '',
			width : 110,
			items : [ {
				xtype : 'radio',
				hideLabel : true,
				boxLabel : "出入库汇总",
				// checked : true,
				name : 'analysisCurr',
				inputValue : 'inOutSum'
			} ]
		}, {
			layout : "form",
			border : false,
			labelSeparator : '',
			width : 125,
			items : [ {
				xtype : 'radio',
				hideLabel : true,
				boxLabel : "库存分析",
				// checked : true,
				name : 'analysisCurr',
				inputValue : 'analysis'
			} ]
		} ]
	} ]
});

inventoryAnalysisWin = new Ext.Window(
		{
			title : "库存分析",
			width : 450,
			height : 310,
			closeAction : "hide",
			resizable : false,
			layout : "anchor",
			items : [ {
				border : false,
				anchor : "right 15%",
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
							id : "begDateInvenAnalysis",
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
							id : "endDateInvenAnalysis",
							width : 120,
							fieldLabel : "至"
						} ]
					} ]
				} ]
			}, {
				layout : "column",
				title : "食材种类",
				border : false,
				anchor : "100% 37%",
				autoScroll : true,
				frame : true,
				items : [ {
					layout : "form",
					border : false,
					labelSeparator : '',
					width : 80,
					labelWidth : 45,
					items : [ {
						xtype : "checkbox",
						id : "cate1InvenAna"
					} ]
				}, {
					layout : "form",
					border : false,
					labelSeparator : '',
					width : 80,
					labelWidth : 45,
					items : [ {
						xtype : "checkbox",
						id : "cate2InvenAna"
					} ]
				}, {
					layout : "form",
					border : false,
					labelSeparator : '',
					width : 80,
					labelWidth : 45,
					items : [ {
						xtype : "checkbox",
						id : "cate3InvenAna"
					} ]
				}, {
					layout : "form",
					border : false,
					labelSeparator : '',
					width : 80,
					labelWidth : 45,
					items : [ {
						xtype : "checkbox",
						id : "cate4InvenAna"
					} ]
				}, {
					layout : "form",
					border : false,
					labelSeparator : '',
					width : 80,
					labelWidth : 45,
					items : [ {
						xtype : "checkbox",
						id : "cate5InvenAna"
					} ]
				}, {
					layout : "form",
					border : false,
					labelSeparator : '',
					width : 80,
					labelWidth : 45,
					items : [ {
						xtype : "checkbox",
						id : "cate6InvenAna"
					} ]
				}, {
					layout : "form",
					border : false,
					labelSeparator : '',
					width : 80,
					labelWidth : 45,
					items : [ {
						xtype : "checkbox",
						id : "cate7InvenAna"
					} ]
				}, {
					layout : "form",
					border : false,
					labelSeparator : '',
					width : 80,
					labelWidth : 45,
					items : [ {
						xtype : "checkbox",
						id : "cate8InvenAna"
					} ]
				}, {
					layout : "form",
					border : false,
					labelSeparator : '',
					width : 80,
					labelWidth : 45,
					items : [ {
						xtype : "checkbox",
						id : "cate9InvenAna"
					} ]
				}, {
					layout : "form",
					border : false,
					labelSeparator : '',
					width : 80,
					labelWidth : 45,
					items : [ {
						xtype : "checkbox",
						id : "cate10InvenAna"
					} ]
				} ]
			}, {
				layout : "column",
				title : "部门",
				border : false,
				anchor : "100% 37%",
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
						id : "dept1InvenAna"
					} ]
				}, {
					layout : "form",
					border : false,
					labelSeparator : '',
					width : 80,
					labelWidth : 40,
					items : [ {
						xtype : "checkbox",
						id : "dept2InvenAna"
					} ]
				}, {
					layout : "form",
					border : false,
					labelSeparator : '',
					width : 80,
					labelWidth : 40,
					items : [ {
						xtype : "checkbox",
						id : "dept3InvenAna"
					} ]
				}, {
					layout : "form",
					border : false,
					labelSeparator : '',
					width : 80,
					labelWidth : 40,
					items : [ {
						xtype : "checkbox",
						id : "dept4InvenAna"
					} ]
				}, {
					layout : "form",
					border : false,
					labelSeparator : '',
					width : 80,
					labelWidth : 40,
					items : [ {
						xtype : "checkbox",
						id : "dept5InvenAna"
					} ]
				}, {
					layout : "form",
					border : false,
					labelSeparator : '',
					width : 80,
					labelWidth : 40,
					items : [ {
						xtype : "checkbox",
						id : "dept6InvenAna"
					} ]
				}, {
					layout : "form",
					border : false,
					labelSeparator : '',
					width : 80,
					labelWidth : 40,
					items : [ {
						xtype : "checkbox",
						id : "dept7InvenAna"
					} ]
				}, {
					layout : "form",
					border : false,
					labelSeparator : '',
					width : 80,
					labelWidth : 40,
					items : [ {
						xtype : "checkbox",
						id : "dept8InvenAna"
					} ]
				}, {
					layout : "form",
					border : false,
					labelSeparator : '',
					width : 80,
					labelWidth : 40,
					items : [ {
						xtype : "checkbox",
						id : "dept9InvenAna"
					} ]
				}, {
					layout : "form",
					border : false,
					labelSeparator : '',
					width : 80,
					labelWidth : 40,
					items : [ {
						xtype : "checkbox",
						id : "dept10InvenAna"
					} ]
				} ]
			}, analysisCurrFrom ],
			buttons : [
					{
						text : "确定",
						handler : function() {

							// -- 獲取時間 --
							var beginDate = inventoryAnalysisWin.findById(
									"begDateInvenAnalysis").getValue();
							if (beginDate != "") {
								var dateFormated = new Date();
								dateFormated = beginDate;
								beginDate = dateFormated.format('Y-m-d');
							}

							var endDate = inventoryAnalysisWin.findById(
									"endDateInvenAnalysis").getValue();
							if (endDate != "") {
								var dateFormated = new Date();
								dateFormated = endDate;
								endDate = dateFormated.format('Y-m-d');
							}

							// -- 獲取食材種類--
							var materialCates = "";
							if (inventoryAnalysisWin.findById("cate1InvenAna")
									.getValue() == true) {
								materialCates = materialCates
										+ materialCateComboData[0][0] + ",";
							}
							if (inventoryAnalysisWin.findById("cate2InvenAna")
									.getValue() == true) {
								materialCates = materialCates
										+ materialCateComboData[1][0] + ",";
							}
							if (inventoryAnalysisWin.findById("cate3InvenAna")
									.getValue() == true) {
								materialCates = materialCates
										+ materialCateComboData[2][0] + ",";
							}
							if (inventoryAnalysisWin.findById("cate4InvenAna")
									.getValue() == true) {
								materialCates = materialCates
										+ materialCateComboData[3][0] + ",";
							}
							if (inventoryAnalysisWin.findById("cate5InvenAna")
									.getValue() == true) {
								materialCates = materialCates
										+ materialCateComboData[4][0] + ",";
							}
							if (inventoryAnalysisWin.findById("cate6InvenAna")
									.getValue() == true) {
								materialCates = materialCates
										+ materialCateComboData[5][0] + ",";
							}
							if (inventoryAnalysisWin.findById("cate7InvenAna")
									.getValue() == true) {
								materialCates = materialCates
										+ materialCateComboData[6][0] + ",";
							}
							if (inventoryAnalysisWin.findById("cate8InvenAna")
									.getValue() == true) {
								materialCates = materialCates
										+ materialCateComboData[7][0] + ",";
							}
							if (inventoryAnalysisWin.findById("cate9InvenAna")
									.getValue() == true) {
								materialCates = materialCates
										+ materialCateComboData[8][0] + ",";
							}
							if (inventoryAnalysisWin.findById("cate10InvenAna")
									.getValue() == true) {
								materialCates = materialCates
										+ materialCateComboData[9][0] + ",";
							}

							if (materialCates != "") {
								materialCates = materialCates.substring(0,
										materialCates.length - 1);
							}

							// -- 獲取部門 --
							var departments = "";
							if (inventoryAnalysisWin.findById("dept1InvenAna")
									.getValue() == true) {
								departments = departments + "0,";
							}
							if (inventoryAnalysisWin.findById("dept2InvenAna")
									.getValue() == true) {
								departments = departments + "1,";
							}
							if (inventoryAnalysisWin.findById("dept3InvenAna")
									.getValue() == true) {
								departments = departments + "2,";
							}
							if (inventoryAnalysisWin.findById("dept4InvenAna")
									.getValue() == true) {
								departments = departments + "3,";
							}
							if (inventoryAnalysisWin.findById("dept5InvenAna")
									.getValue() == true) {
								departments = departments + "4,";
							}
							if (inventoryAnalysisWin.findById("dept6InvenAna")
									.getValue() == true) {
								departments = departments + "5,";
							}
							if (inventoryAnalysisWin.findById("dept7InvenAna")
									.getValue() == true) {
								departments = departments + "6,";
							}
							if (inventoryAnalysisWin.findById("dept8InvenAna")
									.getValue() == true) {
								departments = departments + "7,";
							}
							if (inventoryAnalysisWin.findById("dept9InvenAna")
									.getValue() == true) {
								departments = departments + "8,";
							}
							if (inventoryAnalysisWin.findById("dept10InvenAna")
									.getValue() == true) {
								departments = departments + "9,";
							}

							if (departments != "") {
								departments = departments.substring(0,
										departments.length - 1);
							}

							// -- 獲取統計類型 --
							var staticType = analysisCurrFrom.getForm()
									.findField("analysisCurr").getGroupValue();
							if (staticType == "current") {
								isPrompt = true;
								inventAnaCurrResultWin.show();

								// ＃＃＃＃＃＃＃＃＃＃＃＃當前庫存＃＃＃＃＃＃＃＃＃＃＃＃
								Ext.Ajax
										.request({
											url : "../../InventoryAnalysisCurrent.do",
											params : {
												"pin" : pin,
												"beginDate" : beginDate,
												"endDate" : endDate,
												"materialCates" : materialCates,
												"departments" : departments
											},
											success : function(response,
													options) {
												var resultJSON = Ext.util.JSON
														.decode(response.responseText);

												var rootData = resultJSON.root;
												if (rootData[0].message == "normal") {

													currInventoryResultData = rootData
															.slice(0);
													currInvenAnaGrid
															.getStore()
															.loadData(
																	currInventoryResultData);
													if (rootData[0].materialID == "NO_DATA") {
														currInvenAnaGrid
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

							} else if (staticType == "inOutSum") {
								isPrompt = true;
								invenAnaInOutSumResultWin.show();

								// ＃＃＃＃＃＃＃＃＃＃＃＃出入庫統計＃＃＃＃＃＃＃＃＃＃＃＃
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

													changeStatByMaterialResultData = rootData
															.slice(0);
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

							} else if (staticType == "analysis") {

								isPrompt = true;
								inventAnalysisResultWin.show();

								// ＃＃＃＃＃＃＃＃＃＃＃＃庫存分析＃＃＃＃＃＃＃＃＃＃＃＃
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

													changeStatByOutDeptResultData = rootData
															.slice(0);
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

							}
						}
					}, {
						text : "取消",
						handler : function() {
							isPrompt = false;
							inventoryAnalysisWin.hide();
						}
					} ],
			listeners : {
				"show" : function(thiz) {

					inventoryAnalysisWin.findById("begDateInvenAnalysis")
							.setValue("");
					inventoryAnalysisWin.findById("endDateInvenAnalysis")
							.setValue("");

					// 神技！動態改變form中component的label！！！
					inventoryAnalysisWin.findById("cate1InvenAna").el.parent()
							.parent().parent().first().dom.innerHTML = materialCateComboData[0][1]
							+ ":";
					inventoryAnalysisWin.findById("cate2InvenAna").el.parent()
							.parent().parent().first().dom.innerHTML = materialCateComboData[1][1]
							+ ":";
					inventoryAnalysisWin.findById("cate3InvenAna").el.parent()
							.parent().parent().first().dom.innerHTML = materialCateComboData[2][1]
							+ ":";
					inventoryAnalysisWin.findById("cate4InvenAna").el.parent()
							.parent().parent().first().dom.innerHTML = materialCateComboData[3][1]
							+ ":";
					inventoryAnalysisWin.findById("cate5InvenAna").el.parent()
							.parent().parent().first().dom.innerHTML = materialCateComboData[4][1]
							+ ":";
					inventoryAnalysisWin.findById("cate6InvenAna").el.parent()
							.parent().parent().first().dom.innerHTML = materialCateComboData[5][1]
							+ ":";
					inventoryAnalysisWin.findById("cate7InvenAna").el.parent()
							.parent().parent().first().dom.innerHTML = materialCateComboData[6][1]
							+ ":";
					inventoryAnalysisWin.findById("cate8InvenAna").el.parent()
							.parent().parent().first().dom.innerHTML = materialCateComboData[7][1]
							+ ":";
					inventoryAnalysisWin.findById("cate9InvenAna").el.parent()
							.parent().parent().first().dom.innerHTML = materialCateComboData[8][1]
							+ ":";
					inventoryAnalysisWin.findById("cate10InvenAna").el.parent()
							.parent().parent().first().dom.innerHTML = materialCateComboData[9][1]
							+ ":";

					inventoryAnalysisWin.findById("cate1InvenAna").setValue(
							false);
					inventoryAnalysisWin.findById("cate2InvenAna").setValue(
							false);
					inventoryAnalysisWin.findById("cate3InvenAna").setValue(
							false);
					inventoryAnalysisWin.findById("cate4InvenAna").setValue(
							false);
					inventoryAnalysisWin.findById("cate5InvenAna").setValue(
							false);
					inventoryAnalysisWin.findById("cate6InvenAna").setValue(
							false);
					inventoryAnalysisWin.findById("cate7InvenAna").setValue(
							false);
					inventoryAnalysisWin.findById("cate8InvenAna").setValue(
							false);
					inventoryAnalysisWin.findById("cate9InvenAna").setValue(
							false);
					inventoryAnalysisWin.findById("cate10InvenAna").setValue(
							false);

					inventoryAnalysisWin.findById("dept1InvenAna").el.parent()
							.parent().parent().first().dom.innerHTML = departmentData[0][1]
							+ ":";
					inventoryAnalysisWin.findById("dept2InvenAna").el.parent()
							.parent().parent().first().dom.innerHTML = departmentData[1][1]
							+ ":";
					inventoryAnalysisWin.findById("dept3InvenAna").el.parent()
							.parent().parent().first().dom.innerHTML = departmentData[2][1]
							+ ":";
					inventoryAnalysisWin.findById("dept4InvenAna").el.parent()
							.parent().parent().first().dom.innerHTML = departmentData[3][1]
							+ ":";
					inventoryAnalysisWin.findById("dept5InvenAna").el.parent()
							.parent().parent().first().dom.innerHTML = departmentData[4][1]
							+ ":";
					inventoryAnalysisWin.findById("dept6InvenAna").el.parent()
							.parent().parent().first().dom.innerHTML = departmentData[5][1]
							+ ":";
					inventoryAnalysisWin.findById("dept7InvenAna").el.parent()
							.parent().parent().first().dom.innerHTML = departmentData[6][1]
							+ ":";
					inventoryAnalysisWin.findById("dept8InvenAna").el.parent()
							.parent().parent().first().dom.innerHTML = departmentData[7][1]
							+ ":";
					inventoryAnalysisWin.findById("dept9InvenAna").el.parent()
							.parent().parent().first().dom.innerHTML = departmentData[8][1]
							+ ":";
					inventoryAnalysisWin.findById("dept10InvenAna").el.parent()
							.parent().parent().first().dom.innerHTML = departmentData[9][1]
							+ ":";

					inventoryAnalysisWin.findById("dept1InvenAna").setValue(
							false);
					inventoryAnalysisWin.findById("dept2InvenAna").setValue(
							false);
					inventoryAnalysisWin.findById("dept3InvenAna").setValue(
							false);
					inventoryAnalysisWin.findById("dept4InvenAna").setValue(
							false);
					inventoryAnalysisWin.findById("dept5InvenAna").setValue(
							false);
					inventoryAnalysisWin.findById("dept6InvenAna").setValue(
							false);
					inventoryAnalysisWin.findById("dept7InvenAna").setValue(
							false);
					inventoryAnalysisWin.findById("dept8InvenAna").setValue(
							false);
					inventoryAnalysisWin.findById("dept9InvenAna").setValue(
							false);
					inventoryAnalysisWin.findById("dept10InvenAna").setValue(
							false);

					analysisCurrFrom.getForm().findField("analysisCurr")
							.setValue("current");

				},
				"hide" : function(thiz) {
					isPrompt = false;
				}
			}
		});

// 结果框
// 前台：[食材种类 食材 价格 库存量 小计]
// --------------------------------------------------------------------------------------------------------
var currInvenAnaReader = new Ext.data.JsonReader({
	idProperty : 'groupID',
	fields : [ {
		name : 'materialCateID',
		type : 'int'
	}, {
		name : 'materialCateName',
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

// define a custom summary function
Ext.grid.GroupSummary.Calculations['sumPrice'] = function(v, record, field) {
	return parseFloat(
			(parseFloat(v).toFixed(2) + (record.data.singlePrice * record.data.amount)
					.toFixed(2))).toFixed(2);
};

var currInvenAnaSummary = new Ext.grid.GroupSummary();

var currInvenAnaGrid = new Ext.grid.EditorGridPanel({
	ds : new Ext.data.GroupingStore({
		reader : currInvenAnaReader,
		data : [],
		sortInfo : {
			field : 'materialName',
			direction : "ASC"
		},
		groupField : 'materialCateName'
	}),

	columns : [ {
		id : 'groupDescr',
		header : "食材种类",
		width : 20,
		sortable : true,
		dataIndex : 'groupDescr',
		hideable : false,
		summaryRenderer : function(v, params, data) {
			return "合计";
		}
	}, {
		header : "NOT SHOW",
		width : 20,
		sortable : false,
		dataIndex : 'materialCateName'
	}, {
		header : "食材",
		width : 25,
		sortable : true,
		dataIndex : 'materialName'
	}, {
		id : 'singlePrice',
		header : "价格",
		width : 20,
		sortable : true,
		groupable : false,
		dataIndex : 'singlePrice',
		summaryType : 'sum'
	}, {
		header : "数量",
		width : 20,
		sortable : true,
		dataIndex : 'amount',
		summaryType : 'sum'
	}, {
		header : "小计",
		width : 20,
		sortable : false,
		dataIndex : 'sumPrice',
		summaryType : 'sumPrice',
		renderer : function(v, params, record) {
			return (record.data.singlePrice * record.data.amount).toFixed(2);
		}
	} ],

	view : new Ext.grid.GroupingView({
		forceFit : true,
		showGroupName : false,
		enableNoGroups : false, // REQUIRED!
		hideGroupedColumn : true
	}),

	plugins : currInvenAnaSummary,

	frame : true,
	width : 800,
	height : 450,
	clicksToEdit : 1,
	collapsible : true,
	animCollapse : false,
	trackMouseOver : false
});

// --------------------------------------------------------------------------------------------------------

var currInvenAnaResultWin = new Ext.Window({
	title : "当前库存",
	width : 400,
	height : 500,
	closeAction : "hide",
	resizable : false,
	layout : "fit",
	items : currInvenAnaGrid,
	buttons : [ {
		text : "打印",
		handler : function() {

		}
	}, {
		text : "关闭",
		handler : function() {
			isPrompt = false;
			currInvenAnaResultWin.hide();
		}
	} ],
	listeners : {
		"hide" : function(thiz) {
			isPrompt = false;
		}
	}
});

// // 结果框 -- 按調出部門
// //
// --------------------------------------------------------------------------------------------------------
// var changeStatByOutDeptReader = new Ext.data.JsonReader({
// idProperty : 'groupID',
// fields : [ {
// name : 'outDeptID',
// type : 'int'
// }, {
// name : 'outDeptName',
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
// var changeStatByOutDeptSummary = new Ext.grid.GroupSummary();
//
// var changeStatByOutDeptGrid = new Ext.grid.EditorGridPanel({
// ds : new Ext.data.GroupingStore({
// reader : changeStatByOutDeptReader,
// data : [],
// sortInfo : {
// field : 'materialName',
// direction : "ASC"
// },
// groupField : 'outDeptName'
// }),
//
// columns : [ {
// id : 'groupDescr',
// header : "调出部门",
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
// dataIndex : 'outDeptName'
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
// plugins : changeStatByOutDeptSummary,
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
// var changeStatByOutDeptResultWin = new Ext.Window({
// title : "调拨汇总-按调出部门",
// width : 400,
// height : 500,
// closeAction : "hide",
// resizable : false,
// layout : "fit",
// items : changeStatByOutDeptGrid,
// buttons : [ {
// text : "打印",
// handler : function() {
//
// }
// }, {
// text : "关闭",
// handler : function() {
// isPrompt = false;
// changeStatByOutDeptResultWin.hide();
// }
// } ],
// listeners : {
// "hide" : function(thiz) {
// isPrompt = false;
// }
// }
// });
//
// // 结果框 -- 按調入部門
// //
// --------------------------------------------------------------------------------------------------------
// var changeStatByInDeptReader = new Ext.data.JsonReader({
// idProperty : 'groupID',
// fields : [ {
// name : 'inDeptID',
// type : 'int'
// }, {
// name : 'inDeptName',
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
// var changeStatByInDeptSummary = new Ext.grid.GroupSummary();
//
// var changeStatByInDeptGrid = new Ext.grid.EditorGridPanel({
// ds : new Ext.data.GroupingStore({
// reader : changeStatByInDeptReader,
// data : [],
// sortInfo : {
// field : 'materialName',
// direction : "ASC"
// },
// groupField : 'inDeptName'
// }),
//
// columns : [ {
// id : 'groupDescr',
// header : "调入部门",
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
// dataIndex : 'inDeptName'
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
// plugins : changeStatByInDeptSummary,
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
// var changeStatByInDeptResultWin = new Ext.Window({
// title : "调拨汇总-按调入部门",
// width : 400,
// height : 500,
// closeAction : "hide",
// resizable : false,
// layout : "fit",
// items : changeStatByInDeptGrid,
// buttons : [ {
// text : "打印",
// handler : function() {
//
// }
// }, {
// text : "关闭",
// handler : function() {
// isPrompt = false;
// changeStatByInDeptResultWin.hide();
// }
// } ],
// listeners : {
// "hide" : function(thiz) {
// isPrompt = false;
// }
// }
// });
