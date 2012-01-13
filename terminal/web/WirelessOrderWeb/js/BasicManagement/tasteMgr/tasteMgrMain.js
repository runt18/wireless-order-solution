// ----------------- 添加口味  --------------------
// 計算方式　－－　添加
var calAddData = [ [ 0, "按价格" ], [ 1, "按比例" ] ];

var calAddStore = new Ext.data.SimpleStore({
	fields : [ "value", "text" ],
	data : calAddData
});

var calAddComb = new Ext.form.ComboBox({
	fieldLabel : "计算方式",
	forceSelection : true,
	width : 160,
	value : calAddData[0][0],
	id : "calAddComb",
	store : calAddStore,
	valueField : "value",
	displayField : "text",
	typeAhead : true,
	mode : "local",
	triggerAction : "all",
	selectOnFocus : true,
	allowBlank : false
});

// 類別 －－ 增加
var typeAddData = [ [ 0, "口味" ], [ 1, "做法" ], [ 2, "规格" ] ];

var typeAddStore = new Ext.data.SimpleStore({
	fields : [ "value", "text" ],
	data : typeAddData
});

var typeAddComb = new Ext.form.ComboBox({
	fieldLabel : "类别",
	forceSelection : true,
	width : 160,
	value : typeAddData[0][0],
	id : "typeAddComb",
	store : typeAddStore,
	valueField : "value",
	displayField : "text",
	typeAhead : true,
	mode : "local",
	triggerAction : "all",
	selectOnFocus : true,
	allowBlank : false
});

// 窗口 －－ 增加
tasteAddWin = new Ext.Window({
	layout : "fit",
	title : "添加口味",
	width : 260,
	height : 225,
	closeAction : "hide",
	resizable : false,
	items : [ {
		layout : "form",
		id : "tasteAddForm",
		labelWidth : 60,
		border : false,
		frame : true,
		items : [ {
			xtype : "numberfield",
			fieldLabel : "编号",
			id : "tasteAddNumber",
			allowBlank : false,
			width : 160
		}, {
			xtype : "textfield",
			fieldLabel : "名称",
			id : "tasteAddName",
			allowBlank : false,
			width : 160
		}, {
			xtype : "numberfield",
			fieldLabel : "价格",
			id : "tasteAddPrice",
			value : 0.00,
			// allowBlank : false,
			width : 160
		}, {
			xtype : "numberfield",
			fieldLabel : "比例",
			id : "tasteAddRate",
			value : 0.00,
			// allowBlank : false,
			width : 160,
			validator : function(v) {
				if (v < 0.00 || v > 9.99) {
					return "比例范围是0.00至9.99！";
				} else {
					return true;
				}
			}
		}, calAddComb, typeAddComb ]
	} ],
	buttons : [
			{
				text : "确定",
				handler : function() {

					if (tasteAddWin.findById("tasteAddNumber").isValid()
							&& tasteAddWin.findById("tasteAddName").isValid()) {

						var tasteAddNumber = tasteAddWin.findById(
								"tasteAddNumber").getValue();
						var tasteAddName = tasteAddWin.findById("tasteAddName")
								.getValue();
						var tasteAddPrice = tasteAddWin.findById(
								"tasteAddPrice").getValue();
						if (tasteAddPrice == "") {
							tasteAddPrice = 0;
						}
						var tasteAddRate = tasteAddWin.findById("tasteAddRate")
								.getValue();
						if (tasteAddRate == "") {
							tasteAddRate = 0;
						}

						var calAdd = calAddComb.getValue();
						var typeAdd = typeAddComb.getValue();

						var isDuplicate = false;
						for ( var i = 0; i < tasteData.length; i++) {
							if (tasteAddNumber == tasteData[i].tasteAlias) {
								isDuplicate = true;
							}
						}

						if (!isDuplicate) {
							tasteAddWin.hide();
							isPrompt = false;

							Ext.Ajax.request({
								url : "../../InsertTaste.do",
								params : {
									"pin" : pin,
									"tasteNumber" : tasteAddNumber,
									"tasteName" : tasteAddName,
									"tastePrice" : tasteAddPrice,
									"tasteRate" : tasteAddRate,
									"cal" : calAdd,
									"type" : typeAdd
								},
								success : function(response, options) {
									var resultJSON = Ext.util.JSON
											.decode(response.responseText);
									if (resultJSON.success == true) {
										loadAllTaste();
										tasteStore.reload({
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
								msg : "该口味编号已存在！",
								width : 300,
								buttons : Ext.MessageBox.OK
							});
						}

					}

				}
			}, {
				text : "取消",
				handler : function() {
					tasteAddWin.hide();
					isPrompt = false;
				}
			} ],
	listeners : {
		"show" : function(thiz) {

			tasteAddWin.findById("tasteAddNumber").setValue("");
			tasteAddWin.findById("tasteAddNumber").clearInvalid();

			tasteAddWin.findById("tasteAddName").setValue("");
			tasteAddWin.findById("tasteAddName").clearInvalid();

			tasteAddWin.findById("tasteAddPrice").setValue(0.00);
			tasteAddWin.findById("tasteAddRate").setValue(0.00);

			calAddComb.setValue(calAddData[0][0]);
			calAddComb.clearInvalid();

			typeAddComb.setValue(typeAddData[0][0]);
			typeAddComb.clearInvalid();

			var f = Ext.get("tasteAddNumber");
			f.focus.defer(100, f); // 为什么这样才可以！？！？

		}
	}
});

// --------------------------------------------------------------------------
var tasteAddBut = new Ext.ux.ImageButton({
	imgPath : "../../images/material_add.png",
	imgWidth : 50,
	imgHeight : 50,
	tooltip : "添加口味",
	handler : function(btn) {
		if (!isPrompt) {
			tasteAddWin.show();
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
		tasteGrid.getStore().each(
				function(record) {
					if (record.isModified("tasteName") == true
							|| record.isModified("tastePrice") == true
							|| record.isModified("tasteRate") == true
							|| record.isModified("tasteCalc") == true
							|| record.isModified("tasteCategory") == true) {
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
		[ "3", "价钱" ] ];
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

			// var statusData = [ [ "1", "空闲" ], [ "2", "就餐" ] ];
			// var statusComb = new Ext.form.ComboBox({
			// hideLabel : true,
			// forceSelection : true,
			// width : 120,
			// value : statusData[0][0],
			// id : "statusComb",
			// store : new Ext.data.SimpleStore({
			// fields : [ "value", "text" ],
			// data : statusData
			// }),
			// valueField : "value",
			// displayField : "text",
			// typeAhead : true,
			// mode : "local",
			// triggerAction : "all",
			// selectOnFocus : true,
			// allowBlank : false
			// });

			// ------------------remove field-------------------
			if (conditionType == "text") {
				searchForm.remove("conditionText");
			} else if (conditionType == "number") {
				searchForm.remove("conditionNumber");
			}
			// else if (conditionType == "regionComb") {
			// searchForm.remove("regionComb");
			// } else if (conditionType == "statusComb") {
			// searchForm.remove("statusComb");
			// }

			// ------------------ add field -------------------
			operatorComb.setDisabled(false);
			// [ "0", "全部" ], [ "1", "编号" ], [ "2", "名称" ], [ "3", "價錢" ]
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
				// 價錢
				searchForm.add(conditionNumber);
				conditionType = "number";
			}
			// else if (index == 4) {
			// // 狀態
			// searchForm.add(statusComb);
			// operatorComb.setValue(1);
			// operatorComb.setDisabled(true);
			// conditionType = "statusComb";
			// }

			tasteQueryCondPanel.doLayout();
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
var tasteQueryCondPanel = new Ext.form.FormPanel({
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
						tasteStore.reload({
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
function tasteDeleteHandler(rowIndex) {
	Ext.MessageBox.show({
		msg : "确定删除？",
		width : 300,
		buttons : Ext.MessageBox.YESNO,
		fn : function(btn) {
			if (btn == "yes") {
				var tasteID = tasteStore.getAt(rowIndex).get("tasteID");

				Ext.Ajax.request({
					url : "../../DeleteTaste.do",
					params : {
						"pin" : pin,
						"tasteID" : tasteID
					},
					success : function(response, options) {
						var resultJSON = Ext.util.JSON
								.decode(response.responseText);
						if (resultJSON.success == true) {
							loadAllTaste();
							tasteStore.reload({
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
};

// 計算方式 －－ 修改
var calModStore = new Ext.data.SimpleStore({
	fields : [ "value", "text" ],
	data : calAddData
});

var calModComb = new Ext.form.ComboBox({
	// fieldLabel : "计算方式",
	forceSelection : true,
	width : 160,
	value : calAddData[0][0],
	id : "calModComb",
	store : calModStore,
	valueField : "value",
	displayField : "text",
	typeAhead : true,
	mode : "local",
	triggerAction : "all",
	selectOnFocus : true,
	allowBlank : false
});

// 類別 －－ 修改
var typeModStore = new Ext.data.SimpleStore({
	fields : [ "value", "text" ],
	data : typeAddData
});

var typeModComb = new Ext.form.ComboBox({
	// fieldLabel : "类别",
	forceSelection : true,
	width : 160,
	value : typeAddData[0][0],
	id : "typeModComb",
	store : typeModStore,
	valueField : "value",
	displayField : "text",
	typeAhead : true,
	mode : "local",
	triggerAction : "all",
	selectOnFocus : true,
	allowBlank : false
});

function tasteOpt(value, cellmeta, record, rowIndex, columnIndex, store) {
	return "<center><a href=\"javascript:tasteDeleteHandler(" + rowIndex
			+ ")\">" + "<img src='../../images/del.png'/>删除</a>" + "</center>";
};

// 1，表格的数据store
var tasteStore = new Ext.data.Store({
	proxy : new Ext.data.HttpProxy({
		url : "../../QueryTaste.do"
	}),
	reader : new Ext.data.JsonReader({
		totalProperty : "totalProperty",
		root : "root"
	}, [ {
		name : "tasteID"
	}, {
		name : "tasteAlias"
	}, {
		name : "tasteName"
	}, {
		name : "tastePrice"
	}, {
		name : "tasteRate"
	}, {
		name : "tasteCategory"
	}, {
		name : "tasteCalc"
	}, {
		name : "operator"
	}, {
		name : "message"
	} ])
});

// menuStore.reload();

// 2，栏位模型
var tasteColumnModel = new Ext.grid.ColumnModel([ new Ext.grid.RowNumberer(), {
	header : "编号",
	sortable : true,
	dataIndex : "tasteAlias",
	width : 80
}, {
	header : "名称",
	sortable : true,
	dataIndex : "tasteName",
	width : 100,
	editor : new Ext.form.TextField({
		// allowBlank : false,
		allowNegative : false
	})
}, {
	header : "价格（￥）",
	sortable : true,
	dataIndex : "tastePrice",
	width : 80,
	editor : new Ext.form.NumberField({})
}, {
	header : "比例",
	sortable : true,
	dataIndex : "tasteRate",
	width : 80,
	editor : new Ext.form.NumberField({})
}, {
	header : "计算方式",
	sortable : true,
	dataIndex : "tasteCalc",
	width : 100,
	editor : calModComb,
	renderer : function(value, cellmeta, record) {
		var calDesc = "";
		for ( var i = 0; i < calAddData.length; i++) {
			if (calAddData[i][0] == value) {
				calDesc = calAddData[i][1];
			}
		}
		return calDesc;
	}
}, {
	header : "类型",
	sortable : true,
	dataIndex : "tasteCategory",
	width : 100,
	editor : typeModComb,
	renderer : function(value, cellmeta, record) {
		var typeDesc = "";
		for ( var i = 0; i < typeAddData.length; i++) {
			if (typeAddData[i][0] == value) {
				typeDesc = typeAddData[i][1];
			}
		}
		return typeDesc;
	}
}, {
	header : "<center>操作</center>",
	sortable : true,
	dataIndex : "operator",
	width : 180,
	renderer : tasteOpt
} ]);

// -------------- layout ---------------
var tasteGrid;
Ext
		.onReady(function() {
			// 解决ext中文传入后台变问号问题
			Ext.lib.Ajax.defaultPostHeader += '; charset=utf-8';
			Ext.QuickTips.init();

			// ---------------------表格--------------------------
			tasteGrid = new Ext.grid.EditorGridPanel(
					{
						title : "口味",
						xtype : "grid",
						anchor : "99%",
						region : "center",
						frame : true,
						margins : '0 5 0 0',
						ds : tasteStore,
						cm : tasteColumnModel,
						sm : new Ext.grid.RowSelectionModel({
							singleSelect : true
						}),
						viewConfig : {
							forceFit : true
						},
						listeners : {
							rowclick : function(thiz, rowIndex, e) {
								currRowIndex = rowIndex;
							}
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
								tasteGrid
										.getStore()
										.each(
												function(record) {
													if (record
															.isModified("tasteName") == true
															|| record
																	.isModified("tastePrice") == true
															|| record
																	.isModified("tasteRate") == true
															|| record
																	.isModified("tasteCalc") == true
															|| record
																	.isModified("tasteCategory") == true) {
														modfiedArr
																.push(record
																		.get("tasteID")
																		+ " field_separator "
																		+ record
																				.get("tasteName")
																		+ " field_separator "
																		+ record
																				.get("tastePrice")
																		+ " field_separator "
																		+ record
																				.get("tasteRate")
																		+ " field_separator "
																		+ record
																				.get("tasteCalc")
																		+ " field_separator "
																		+ record
																				.get("tasteCategory"));
													}
												});

								if (modfiedArr.length != 0) {
									// 獲取分頁表格的當前頁碼！神技！！！
									var toolbar = tasteGrid.getBottomToolbar();
									currPageIndex = toolbar.readPage(toolbar
											.getPageData());

									var modTastes = "";
									for ( var i = 0; i < modfiedArr.length; i++) {
										modTastes = modTastes + modfiedArr[i]
												+ " record_separator ";
									}
									modTastes = modTastes.substring(0,
											modTastes.length - 18);

									Ext.Ajax
											.request({
												url : "../../UpdateTaste.do",
												params : {
													"pin" : pin,
													"modTastes" : modTastes
												},
												success : function(response,
														options) {
													var resultJSON = Ext.util.JSON
															.decode(response.responseText);
													if (resultJSON.success == true) {
														// loadAllTaste();
														tasteStore
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
							store : tasteStore,
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
								// alert("here");
								tasteStore.reload({
									params : {
										start : 0,
										limit : pageRecordCount
									}
								});
							}
						}
					});

			// 为store配置beforeload监听器
			tasteGrid.getStore()
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
								}
								// else if (conditionType == "regionComb") {
								// queryValue = searchForm.findById(
								// "regionComb").getValue();
								// } else if (conditionType == "statusComb") {
								// queryValue = searchForm.findById(
								// "statusComb").getValue();
								// }

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
			tasteGrid.getStore().on('load', function() {
				if (tasteGrid.getStore().getTotalCount() != 0) {
					var msg = this.getAt(0).get("message");
					if (msg != "normal") {
						Ext.MessageBox.show({
							msg : msg,
							width : 300,
							buttons : Ext.MessageBox.OK
						});
						this.removeAll();
					} else {

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
					items : [ tasteQueryCondPanel, tasteGrid ]
				} ],
				tbar : new Ext.Toolbar({
					height : 55,
					items : [ tasteAddBut, {
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
