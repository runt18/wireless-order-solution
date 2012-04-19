// ----------------- 添加餐桌  --------------------
var regionAddStore = new Ext.data.SimpleStore({
	fields : [ "value", "text" ],
	data : [],
	listeners : {
		load : function() {
			// 解決combo初始值問題
			// Ext.getCmp('regionAddComb').setValue(regionData[0][0]);
		}
	}
});

var regionAddComb = new Ext.form.ComboBox({
	fieldLabel : "区域",
	forceSelection : true,
	width : 160,
	// value : departmentData[0][1],
	id : "regionAddComb",
	store : regionAddStore,
	valueField : "value",
	displayField : "text",
	typeAhead : true,
	mode : "local",
	triggerAction : "all",
	selectOnFocus : true,
	allowBlank : false
});

tableAddWin = new Ext.Window({
	layout : "fit",
	title : "添加餐桌",
	width : 260,
	height : 197,
	closeAction : "hide",
	resizable : false,
	items : [ {
		layout : "form",
		id : "tableAddForm",
		labelWidth : 60,
		border : false,
		frame : true,
		items : [ {
			xtype : "numberfield",
			fieldLabel : "编号",
			id : "tableAddNumber",
			allowBlank : false,
			width : 160
		}, {
			xtype : "textfield",
			fieldLabel : "名称",
			id : "tableAddName",
			// allowBlank : false,
			width : 160
		}, regionAddComb, {
			xtype : "numberfield",
			fieldLabel : "最低消费",
			id : "tableAddMincost",
			// allowBlank : false,
			width : 160
		}, {
			xtype : "numberfield",
			fieldLabel : "服务费率",
			id : "tableAddSerRate",
			// allowBlank : false,
			width : 160,
			validator : function(v) {
				if (v < 0 || v > 1) {
					return "服务费率范围是0%至100%！";
				} else {
					return true;
				}
			}
		} ]
	} ],
	buttons : [
			{
				text : "确定",
				handler : function() {

					if (tableAddWin.findById("tableAddNumber").isValid()
							&& tableAddWin.findById("tableAddName").isValid()
							&& tableAddWin.findById("regionAddComb").isValid()
							&& tableAddWin.findById("tableAddMincost")
									.isValid()
							&& tableAddWin.findById("tableAddSerRate")
									.isValid()) {

						var tableAddNumber = tableAddWin.findById(
								"tableAddNumber").getValue();
						var tableAddName = tableAddWin.findById("tableAddName")
								.getValue();
						var region = regionAddComb.getValue();

						var tableAddMincost = tableAddWin.findById(
								"tableAddMincost").getValue();
						if (tableAddMincost == "") {
							tableAddMincost = 0;
						}

						var tableAddSerRate = tableAddWin.findById(
								"tableAddSerRate").getValue();
						if (tableAddSerRate == "") {
							tableAddSerRate = 0;
						}

						var isDuplicate = false;
						for ( var i = 0; i < tableData.length; i++) {
							if (tableAddNumber == tableData[i].tableAlias) {
								isDuplicate = true;
							}
						}

						if (!isDuplicate) {
							tableAddWin.hide();
							isPrompt = false;

							Ext.Ajax.request({
								url : "../../InsertTable.do",
								params : {
									"pin" : pin,
									"tableNumber" : tableAddNumber,
									"tableName" : tableAddName,
									"region" : region,
									"tableMincost" : tableAddMincost,
									"tableSerRate" : tableAddSerRate
								},
								success : function(response, options) {
									var resultJSON = Ext.util.JSON
											.decode(response.responseText);
									if (resultJSON.success == true) {
										loadAllTable();
										tableStore.reload({
											params : {
												start : 0,
												limit : pageRecordCount
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
								msg : "该餐桌编号已存在！",
								width : 300,
								buttons : Ext.MessageBox.OK
							});
						}

					}

				}
			}, {
				text : "取消",
				handler : function() {
					tableAddWin.hide();
					isPrompt = false;
				}
			} ],
	listeners : {
		"show" : function(thiz) {

			tableAddWin.findById("tableAddNumber").setValue("");
			tableAddWin.findById("tableAddNumber").clearInvalid();

			tableAddWin.findById("tableAddName").setValue("");
			tableAddWin.findById("tableAddName").clearInvalid();

			regionAddComb.setValue(regionData[0][0]);
			regionAddComb.clearInvalid();

			tableAddWin.findById("tableAddMincost").setValue("");
			tableAddWin.findById("tableAddMincost").clearInvalid();

			tableAddWin.findById("tableAddSerRate").setValue("");
			tableAddWin.findById("tableAddSerRate").clearInvalid();

			var f = Ext.get("tableAddNumber");
			f.focus.defer(100, f); // 为什么这样才可以！？！？

		}
	}
});

// --------------------------------------------------------------------------
var tableAddBut = new Ext.ux.ImageButton({
	imgPath : "../../images/material_add.png",
	imgWidth : 50,
	imgHeight : 50,
	tooltip : "添加餐桌",
	handler : function(btn) {
		if (!isPrompt) {
			tableAddWin.show();
			isPrompt = true;
		}
	}
});

// --
var pushBackBut = new Ext.ux.ImageButton({
	imgPath : "../../images/UserLogout.png",
	imgWidth : 50,
	imgHeight : 50,
	tooltip : "返回",
	handler : function(btn) {
		var isChange = false;
		tableGrid.getStore().each(
				function(record) {
					if (record.isModified("tableName") == true
							|| record.isModified("tableRegion") == true
							|| record.isModified("tableMinCost") == true
							|| record.isModified("tableServiceRate") == true) {
						isChange = true;
					}
				});

		if (isChange) {
			Ext.MessageBox.show({
				msg : "修改尚未保存，是否确认返回？",
				width : 300,
				buttons : Ext.MessageBox.YESNO,
				fn : function(btn) {
					if (btn == "yes") {
						location.href = "BasicMgrProtal.html?restaurantID="
								+ restaurantID + "&pin=" + pin;
					}
				}
			});
		} else {
			location.href = "BasicMgrProtal.html?restaurantID=" + restaurantID
					+ "&pin=" + pin;
		}
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
		[ "3", "区域" ], [ "4", "状态 " ] ];
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

			var regionComb = new Ext.form.ComboBox({
				hideLabel : true,
				forceSelection : true,
				width : 120,
				value : regionData[0][0],
				id : "regionComb",
				store : new Ext.data.SimpleStore({
					fields : [ "value", "text" ],
					data : regionData
				}),
				valueField : "value",
				displayField : "text",
				typeAhead : true,
				mode : "local",
				triggerAction : "all",
				selectOnFocus : true,
				allowBlank : false
			});

			var statusData = [ [ "0", "空闲" ], [ "1", "就餐" ] ];
			var statusComb = new Ext.form.ComboBox({
				hideLabel : true,
				forceSelection : true,
				width : 120,
				value : statusData[0][0],
				id : "statusComb",
				store : new Ext.data.SimpleStore({
					fields : [ "value", "text" ],
					data : statusData
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
			} else if (conditionType == "regionComb") {
				searchForm.remove("regionComb");
			} else if (conditionType == "statusComb") {
				searchForm.remove("statusComb");
			}

			// ------------------ add field -------------------
			operatorComb.setDisabled(false);
			// [ "0", "全部" ], [ "1", "编号" ], [ "2", "名称" ], [ "3", "区域" ], [
			// "4", "状态 " ]
			if (index == 0) {
				// 全部
				// searchForm.add(conditionText);
				operatorComb.setValue(1);
				operatorComb.setDisabled(true);
				conditionType = "text";
			} else if (index == 1) {
				// 编号
				searchForm.add(conditionNumber);
				conditionType = "number";
			} else if (index == 2) {
				// 名称
				searchForm.add(conditionText);
				operatorComb.setValue(1);
				operatorComb.setDisabled(true);
				conditionType = "text";
			} else if (index == 3) {
				// 區域
				searchForm.add(regionComb);
				operatorComb.setValue(1);
				operatorComb.setDisabled(true);
				conditionType = "regionComb";
			} else if (index == 4) {
				// 狀態
				searchForm.add(statusComb);
				operatorComb.setValue(1);
				operatorComb.setDisabled(true);
				conditionType = "statusComb";
			}

			tableQueryCondPanel.doLayout();
		}
	}
});

var operatorData = [ [ "1", "等于" ], [ "2", "大于等于" ], [ "3", "小于等于" ] ];
var operatorComb = new Ext.form.ComboBox({
	hideLabel : true,
	forceSelection : true,
	width : 100,
	value : operatorData[0][0],
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
var tableQueryCondPanel = new Ext.form.FormPanel({
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
						tableStore.reload({
							params : {
								start : 0,
								limit : pageRecordCount
							}
						});
					}
				}
			} ]
		} ]
	} ]
});

// operator function
function tableDeleteHandler(rowIndex) {

	if (tableGrid.getStore().getAt(rowIndex).get("tableStatus") == 1) {
		Ext.MessageBox.show({
			msg : "就餐状态餐台不能删除",
			width : 300,
			buttons : Ext.MessageBox.OK
		});
	} else {

		Ext.MessageBox.show({
			msg : "确定删除？",
			width : 300,
			buttons : Ext.MessageBox.YESNO,
			fn : function(btn) {
				if (btn == "yes") {
					var tableID = tableStore.getAt(rowIndex).get("tableID");

					Ext.Ajax.request({
						url : "../../DeleteTable.do",
						params : {
							"pin" : pin,
							"tableID" : tableID
						},
						success : function(response, options) {
							var resultJSON = Ext.util.JSON
									.decode(response.responseText);
							if (resultJSON.success == true) {
								loadAllTable();
								tableStore.reload({
									params : {
										start : 0,
										limit : pageRecordCount
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
	}
};

var regionCombStore = new Ext.data.Store({
	proxy : new Ext.data.HttpProxy({
		url : "../../QueryRegion.do?pin=" + pin
				+ "&isPaging=false&isCombo=true&isTree=false"
	}),
	reader : new Ext.data.JsonReader({
		root : 'root'
	}, [ {
		name : 'regionID',
		mapping : 'regionID'
	}, {
		name : 'regionName',
		mapping : 'regionName'
	} ])
});

var regionModComb = new Ext.form.ComboBox({
	forceSelection : true,
	id : "regionModComb",
	store : regionCombStore,
	valueField : "regionID",
	displayField : "regionName",
	typeAhead : true,
	mode : "local",
	triggerAction : "all",
	selectOnFocus : true,
	allowBlank : false,
	validator : function(v) {
		if (tableGrid.getStore().getAt(currRowIndex).get("tableStatus") == 1) {
			return "就餐状态餐台不能修改";
		} else {
			return true;
		}
	}
});

function tableOpt(value, cellmeta, record, rowIndex, columnIndex, store) {
	return "<center><a href=\"javascript:tableDeleteHandler(" + rowIndex
			+ ")\">" + "<img src='../../images/del.png'/>删除</a>" + "</center>";
};

// 1，表格的数据store
//* ["账单号","台号","日期","类型","结帐方式","金额","实收","台号2",
//	 * "就餐人数","最低消","服务费率","会员编号","会员姓名","账单备注",
//	 * "赠券金额","结帐类型","折扣类型","服务员",是否反結帳]
var tableStore = new Ext.data.Store({
	proxy : new Ext.data.HttpProxy({
		url : "../../QueryHistory.do"
	}),
	reader : new Ext.data.JsonReader({
		totalProperty : "totalProperty",
		root : "root"
	}, [ {
		name : "tableID"
	}, {
		name : "tableAlias"
	}, {
		name : "tableName"
	}, {
		name : "tableRegion"
	}, {
		name : "tableCustNbr"
	}, {
		name : "tableStatus"
	}, {
		name : "tableStatusDisplay"
	}, {
		name : "tableCategory"
	}, {
		name : "tableCategoryDisplay"
	}, {
		name : "tableMinCost"
	}, {
		name : "tableServiceRate"
	}, {
		name : "operator"
	}, {
		name : "message"
	} ])
});

// menuStore.reload();

// 2，栏位模型
var tableColumnModel = new Ext.grid.ColumnModel([
		new Ext.grid.RowNumberer(),
		{
			header : "编号",
			sortable : true,
			dataIndex : "tableAlias",
			width : 80
		},
		{
			header : "名称",
			sortable : true,
			dataIndex : "tableName",
			width : 100,
			editor : new Ext.form.TextField({
				// allowBlank : false,
				allowNegative : false,
				selectOnFocus : true,
				validator : function(v) {
					if (tableGrid.getStore().getAt(currRowIndex).get(
							"tableStatus") == 1) {
						return "就餐状态餐台不能修改";
					} else {
						return true;
					}
				}
			})
		},
		{
			header : "区域",
			sortable : true,
			dataIndex : "tableRegion",
			width : 100,
			editor : regionModComb,
			renderer : function(value, cellmeta, record) {
				var regionDesc = "";
				for ( var i = 0; i < regionData.length; i++) {
					if (regionData[i][0] == value) {
						regionDesc = regionData[i][1];
					}
				}
				return regionDesc;
			}
		},
		{
			header : "最低消（￥）",
			sortable : true,
			dataIndex : "tableMinCost",
			width : 100,
			editor : new Ext.form.NumberField({
				allowBlank : false,
				allowNegative : false,
				selectOnFocus : true,
				validator : function(v) {
					if (tableGrid.getStore().getAt(currRowIndex).get(
							"tableStatus") == 1) {
						return "就餐状态餐台不能修改";
					} else {
						return true;
					}
				}
			})
		},
		{
			header : "服务费率",
			sortable : true,
			dataIndex : "tableServiceRate",
			width : 100,
			editor : new Ext.form.TextField({
				allowBlank : false,
				allowNegative : false,
				selectOnFocus : true,
				validator : function(v) {
					if (tableGrid.getStore().getAt(currRowIndex).get(
							"tableStatus") == 1) {
						return "就餐状态餐台不能修改";
					} else {
						if (v < 0 || v > 1) {
							return "服务费率范围是0%至100%！";
						} else {
							return true;
						}
					}
				}
			}),
			renderer : function(v, params, record) {
				return (record.data.tableServiceRate * 100) + "%";
			}
		}, {
			header : "状态",
			sortable : true,
			dataIndex : "tableStatusDisplay",
			width : 90
		}, {
			header : "类型",
			sortable : true,
			dataIndex : "tableCategoryDisplay",
			width : 90
		}, {
			header : "<center>操作</center>",
			sortable : true,
			dataIndex : "operator",
			width : 180,
			renderer : tableOpt
		} ]);

// -------------- layout ---------------
var tableGrid;
Ext
		.onReady(function() {
			// 解决ext中文传入后台变问号问题
			Ext.lib.Ajax.defaultPostHeader += '; charset=utf-8';
			Ext.QuickTips.init();

			// ---------------------表格--------------------------
			tableGrid = new Ext.grid.EditorGridPanel(
					{
						title : "餐台",
						xtype : "grid",
						anchor : "99%",
						region : "center",
						frame : true,
						margins : '0 5 0 0',
						ds : tableStore,
						cm : tableColumnModel,
						sm : new Ext.grid.RowSelectionModel({
							singleSelect : true
						}),
						viewConfig : {
							forceFit : true
						},
						tbar : [ {
							text : '保存修改',
							tooltip : '保存修改',
							iconCls : 'save',
							handler : function() {
								// 修改記錄格式:id field_separator name
								// field_separator phone field_separator contact
								// field_separator address record_separator id
								// field_separator name field_separator phone
								// field_separator contact field_separator
								// address
								var modfiedArr = [];
								tableGrid
										.getStore()
										.each(
												function(record) {
													if (record
															.isModified("tableName") == true
															|| record
																	.isModified("tableRegion") == true
															|| record
																	.isModified("tableMinCost") == true
															|| record
																	.isModified("tableServiceRate") == true) {
														modfiedArr
																.push(record
																		.get("tableID")
																		+ " field_separator "
																		+ record
																				.get("tableName")
																		+ " field_separator "
																		+ record
																				.get("tableRegion")
																		+ " field_separator "
																		+ record
																				.get("tableMinCost")
																		+ " field_separator "
																		+ record
																				.get("tableServiceRate"));
													}
												});

								if (modfiedArr.length != 0) {
									// 獲取分頁表格的當前頁碼！神技！！！
									var toolbar = tableGrid.getBottomToolbar();
									currPageIndex = toolbar.readPage(toolbar
											.getPageData());

									var modTables = "";
									for ( var i = 0; i < modfiedArr.length; i++) {
										modTables = modTables + modfiedArr[i]
												+ " record_separator ";
									}
									modTables = modTables.substring(0,
											modTables.length - 18);

									Ext.Ajax
											.request({
												url : "../../UpdateTable.do",
												params : {
													"pin" : pin,
													"modTables" : modTables
												},
												success : function(response,
														options) {
													var resultJSON = Ext.util.JSON
															.decode(response.responseText);
													if (resultJSON.success == true) {
														tableStore
																.reload({
																	params : {
																		start : (currPageIndex - 1)
																				* pageRecordCount,
																		limit : pageRecordCount
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
						} ],
						bbar : new Ext.PagingToolbar({
							pageSize : pageRecordCount,
							store : tableStore,
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
								tableStore.reload({
									params : {
										start : 0,
										limit : pageRecordCount
									}
								});
							},
							"rowclick" : function(thiz, rowIndex, e) {
								currRowIndex = rowIndex;
							}
						}
					});

			// 为store配置beforeload监听器
			tableGrid.getStore()
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
								} else if (conditionType == "regionComb") {
									queryValue = searchForm.findById(
											"regionComb").getValue();
								} else if (conditionType == "statusComb") {
									queryValue = searchForm.findById(
											"statusComb").getValue();
								}

								// 输入查询条件参数
								this.baseParams = {
									"pin" : pin,
									"type" : queryTpye,
									"ope" : queryOperator,
									"value" : queryValue,
									"isPaging" : true,
									"isCombo" : false
								};

							});

			// 为store配置load监听器(即load完后动作)
			tableGrid.getStore().on('load', function() {
				if (tableGrid.getStore().getTotalCount() != 0) {
					var msg = this.getAt(0).get("message");
					if (msg != "normal") {
						Ext.MessageBox.show({
							msg : msg,
							width : 300,
							buttons : Ext.MessageBox.OK
						});
						this.removeAll();
					} else {
						tableGrid.getStore().each(function(record) {
							// 狀態顯示
							if (record.get("tableStatus") == 0) {
								record.set("tableStatusDisplay", "空闲");
							} else {
								record.set("tableStatusDisplay", "就餐");
							}

							// 類型顯示: 一般 : 1\n外卖 : 2\n并台 : 3\n拼台 : 4
							if (record.get("tableCategory") == 1) {
								record.set("tableCategoryDisplay", "一般");
							} else if (record.get("tableCategory") == 2) {
								record.set("tableCategoryDisplay", "外卖");
							} else if (record.get("tableCategory") == 3) {
								record.set("tableCategoryDisplay", "并台");
							} else if (record.get("tableCategory") == 4) {
								record.set("tableCategoryDisplay", "拼台");
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
					title : "<div style='font-size:20px;'>餐台管理<div>",
					items : [ tableQueryCondPanel, tableGrid ]
				} ],
				tbar : new Ext.Toolbar({
					height : 55,
					items : [ tableAddBut, {
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
