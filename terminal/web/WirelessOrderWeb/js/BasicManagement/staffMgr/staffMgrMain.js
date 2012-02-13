// ----------------- 添加員工  --------------------
// 窗口 －－ 增加
staffAddWin = new Ext.Window(
		{
			layout : "fit",
			title : "添加员工",
			width : 260,
			height : 193,
			closeAction : "hide",
			resizable : false,
			items : [ {
				layout : "form",
				id : "staffAddWin",
				labelWidth : 60,
				border : false,
				frame : true,
				items : [
						{
							xtype : "numberfield",
							fieldLabel : "编号",
							id : "staffAddNumber",
							allowBlank : false,
							width : 160
						},
						{
							xtype : "textfield",
							fieldLabel : "姓名",
							id : "staffAddName",
							allowBlank : false,
							width : 160
						},
						{
							xtype : "textfield",
							inputType : "password",
							fieldLabel : "密码",
							id : "staffAddPwd",
							// allowBlank : false,
							width : 160
						},
						{
							xtype : "numberfield",
							fieldLabel : "赠送额度",
							id : "staffAddQuota",
							allowBlank : false,
							width : 160,
							validator : function(v) {
								if (v < 0.00 || v > 99999.99) {
									return "赠送额度范围是0.00至99999.99！";
								} else {
									return true;
								}
							}
						},
						{
							xtype : "checkbox",
							id : "noQuotaLimitAdd",
							fieldLabel : "无限制",
							listeners : {
								"check" : function(thiz, checked) {
									if (checked) {
										staffAddWin.findById("staffAddQuota")
												.disable();
									} else {
										staffAddWin.findById("staffAddQuota")
												.enable();
									}
								}
							}
						} ]
			} ],
			buttons : [
					{
						text : "确定",
						handler : function() {

							if (staffAddWin.findById("staffAddNumber")
									.isValid()
									&& staffAddWin.findById("staffAddName")
											.isValid()
									&& staffAddWin.findById("staffAddQuota")
											.isValid()) {

								var staffAddNumber = staffAddWin.findById(
										"staffAddNumber").getValue();
								var staffAddName = staffAddWin.findById(
										"staffAddName").getValue();
								var staffAddPwd = staffAddWin.findById(
										"staffAddPwd").getValue();
								var staffAddQuota = staffAddWin.findById(
										"staffAddQuota").getValue();
								if (staffAddQuota == null) {
									staffAddQuota = 0;
								}

								var isNoLimit = staffAddWin.findById(
										"noQuotaLimitAdd").getValue();
								if (isNoLimit == true) {
									staffAddQuota = -1;
								}

								var isDuplicate = false;
								for ( var i = 0; i < staffData.length; i++) {
									if (staffAddNumber == staffData[i].staffAlias) {
										isDuplicate = true;
									}
								}

								if (!isDuplicate) {
									staffAddWin.hide();
									isPrompt = false;

									Ext.Ajax
											.request({
												url : "../../InsertStaff.do",
												params : {
													"pin" : pin,
													"staffNumber" : staffAddNumber,
													"staffName" : staffAddName,
													"staffPwd" : staffAddPwd,
													"staffQuota" : staffAddQuota
												},
												success : function(response,
														options) {
													var resultJSON = Ext.util.JSON
															.decode(response.responseText);
													if (resultJSON.success == true) {
														loadAllStaff();
														staffStore
																.reload({
																	params : {
																		start : 0,
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
								} else {
									Ext.MessageBox.show({
										msg : "该员工编号已存在！",
										width : 300,
										buttons : Ext.MessageBox.OK
									});
								}

							}

						}
					}, {
						text : "取消",
						handler : function() {
							staffAddWin.hide();
							isPrompt = false;
						}
					} ],
			listeners : {
				"show" : function(thiz) {

					staffAddWin.findById("staffAddNumber").setValue("");
					staffAddWin.findById("staffAddNumber").clearInvalid();

					staffAddWin.findById("staffAddName").setValue("");
					staffAddWin.findById("staffAddName").clearInvalid();

					staffAddWin.findById("staffAddPwd").setValue("");
					staffAddWin.findById("staffAddPwd").clearInvalid();

					staffAddWin.findById("staffAddQuota").setValue(0);
					staffAddWin.findById("staffAddQuota").clearInvalid();

					staffAddWin.findById("noQuotaLimitAdd").setValue(false);

					var f = Ext.get("staffAddNumber");
					f.focus.defer(100, f); // 为什么这样才可以！？！？

				}
			}
		});

// --------------------------------------------------------------------------
var staffAddBut = new Ext.ux.ImageButton({
	imgPath : "../../images/material_add.png",
	imgWidth : 50,
	imgHeight : 50,
	tooltip : "添加员工",
	handler : function(btn) {
		if (!isPrompt) {
			staffAddWin.show();
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
		staffGrid.getStore().each(
				function(record) {
					if (record.isModified("staffName") == true
							|| record.isModified("staffQuota") == true) {
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
var filterTypeData = [ [ "0", "全部" ], [ "1", "编号" ], [ "2", "姓名" ] ];
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

			// ------------------remove field-------------------
			if (conditionType == "text") {
				searchForm.remove("conditionText");
			} else if (conditionType == "number") {
				searchForm.remove("conditionNumber");
			}

			// ------------------ add field -------------------
			operatorComb.setDisabled(false);
			// [ "0", "全部" ], [ "1", "编号" ], [ "2", "姓名" ]
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
				// 姓名
				searchForm.add(conditionText);
				operatorComb.setValue(1);
				operatorComb.setDisabled(true);
				conditionType = "text";
			}

			staffQueryCondPanel.doLayout();
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
var staffQueryCondPanel = new Ext.form.FormPanel({
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
						staffStore.reload({
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
function staffDeleteHandler(rowIndex) {
	Ext.MessageBox.show({
		msg : "确定删除？",
		width : 300,
		buttons : Ext.MessageBox.YESNO,
		fn : function(btn) {
			if (btn == "yes") {
				var staffID = staffStore.getAt(rowIndex).get("staffID");

				Ext.Ajax.request({
					url : "../../DeleteStaff.do",
					params : {
						"pin" : pin,
						"staffID" : staffID
					},
					success : function(response, options) {
						var resultJSON = Ext.util.JSON
								.decode(response.responseText);
						if (resultJSON.success == true) {
							loadAllStaff();
							staffStore.reload({
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

function staffOpt(value, cellmeta, record, rowIndex, columnIndex, store) {
	return "<center><a href=\"javascript:staffDeleteHandler(" + rowIndex
			+ ")\">" + "<img src='../../images/del.png'/>删除</a>" + "</center>";
};

// 1，表格的数据store
var staffStore = new Ext.data.Store({
	proxy : new Ext.data.HttpProxy({
		url : "../../QueryStaff.do"
	}),
	reader : new Ext.data.JsonReader({
		totalProperty : "totalProperty",
		root : "root"
	}, [ {
		name : "staffID"
	}, {
		name : "staffAlias"
	}, {
		name : "staffName"
	}, {
		name : "staffPassword"
	}, {
		name : "terminalID"
	}, {
		name : "staffGift"
	}, {
		name : "staffQuota"
	}, {
		name : "quotaOrig"
	}, {
		name : "noLimit"
	}, {
		name : "operator"
	}, {
		name : "message"
	} ])
});

var noLimitCheckColumn = new Ext.grid.CheckColumn({
	header : "无额度限制",
	dataIndex : "noLimit",
	width : 30
});

// 2，栏位模型
var staffColumnModel = new Ext.grid.ColumnModel([ new Ext.grid.RowNumberer(), {
	header : "编号",
	sortable : true,
	dataIndex : "staffAlias",
	width : 80
}, {
	header : "名称",
	sortable : true,
	dataIndex : "staffName",
	width : 100,
	editor : new Ext.form.TextField({
		allowBlank : false,
		allowNegative : false,
		selectOnFocus : true
	})
}, {
	header : "已赠送（￥）",
	sortable : true,
	dataIndex : "staffGift",
	width : 100
}, {
	header : "赠送额度（￥）",
	sortable : true,
	dataIndex : "staffQuota",
	width : 100,
	editor : new Ext.form.NumberField({
		allowBlank : false,
		selectOnFocus : true,
		validator : function(v) {
			if (v < 0.00 || v > 99999.99) {
				return "赠送额度范围是0.00至99999.99！";
			} else {
				return true;
			}
		}
	}),
	renderer : function(v, params, record) {
		if (v < 0) {
			return "无限制";
		} else {
			return v;
		}
	}
}, noLimitCheckColumn,
// {
// header : "计算方式",
// sortable : true,
// dataIndex : "tasteCalc",
// width : 100,
// editor : calModComb,
// renderer : function(value, cellmeta, record) {
// var calDesc = "";
// for ( var i = 0; i < calAddData.length; i++) {
// if (calAddData[i][0] == value) {
// calDesc = calAddData[i][1];
// }
// }
// return calDesc;
// }
// }, {
// header : "类型",
// sortable : true,
// dataIndex : "tasteCategory",
// width : 100,
// editor : typeModComb,
// renderer : function(value, cellmeta, record) {
// var typeDesc = "";
// for ( var i = 0; i < typeAddData.length; i++) {
// if (typeAddData[i][0] == value) {
// typeDesc = typeAddData[i][1];
// }
// }
// return typeDesc;
// }
// },
{
	header : "<center>操作</center>",
	sortable : true,
	dataIndex : "operator",
	width : 100,
	renderer : staffOpt
} ]);

// -------------- layout ---------------
var staffGrid;
Ext
		.onReady(function() {
			// 解决ext中文传入后台变问号问题
			Ext.lib.Ajax.defaultPostHeader += '; charset=utf-8';
			Ext.QuickTips.init();

			// ---------------------表格--------------------------
			staffGrid = new Ext.grid.EditorGridPanel(
					{
						// title : "员工",
						xtype : "grid",
						anchor : "99%",
						region : "center",
						frame : true,
						margins : '0 5 0 0',
						ds : staffStore,
						cm : staffColumnModel,
						plugins : noLimitCheckColumn,
						clicksToEdit : 2,
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
								staffGrid
										.getStore()
										.each(
												function(record) {
													if (record
															.isModified("staffName") == true
															|| record
																	.isModified("staffQuota") == true) {
														modfiedArr
																.push(record
																		.get("staffID")
																		+ " field_separator "
																		+ record
																				.get("terminalID")
																		+ " field_separator "
																		+ record
																				.get("staffName")
																		+ " field_separator "
																		+ record
																				.get("staffQuota"));
													}
												});

								if (modfiedArr.length != 0) {
									// 獲取分頁表格的當前頁碼！神技！！！
									var toolbar = staffGrid.getBottomToolbar();
									currPageIndex = toolbar.readPage(toolbar
											.getPageData());

									var modStaffs = "";
									for ( var i = 0; i < modfiedArr.length; i++) {
										modStaffs = modStaffs + modfiedArr[i]
												+ " record_separator ";
									}
									modStaffs = modStaffs.substring(0,
											modStaffs.length - 18);

									Ext.Ajax
											.request({
												url : "../../UpdateStaff.do",
												params : {
													"pin" : pin,
													"modStaffs" : modStaffs
												},
												success : function(response,
														options) {
													var resultJSON = Ext.util.JSON
															.decode(response.responseText);
													if (resultJSON.success == true) {
														// loadAllTaste();
														staffStore
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
							store : staffStore,
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
								staffStore.reload({
									params : {
										start : 0,
										limit : pageRecordCount
									}
								});
							}
						}
					});

			// 为store配置beforeload监听器
			staffGrid.getStore()
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

								// 输入查询条件参数
								this.baseParams = {
									"restaurantID" : restaurantID,
									"type" : queryTpye,
									"ope" : queryOperator,
									"value" : queryValue,
									"isPaging" : true,
									"isCombo" : false
								};

							});

			// 为store配置load监听器(即load完后动作)
			staffGrid.getStore().on('load', function() {
				if (staffGrid.getStore().getTotalCount() != 0) {
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

			staffGrid.on("beforeedit",
					function(e) {
						if (e.record.get("noLimit") == true
								&& e.field == "staffQuota") {
							e.cancel = true;
						}
					});

			staffGrid.on("afteredit", function(e) {
				if (e.field == "noLimit") {
					if (e.record.get("noLimit") == true) {
						e.record.set("staffQuota", -1);
					} else {
						if (e.record.get("quotaOrig") > 0) {
							e.record.set("staffQuota", e.record
									.get("quotaOrig"));
						} else {
							e.record.set("staffQuota", 0);
						}
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
					title : "<div style='font-size:20px;'>员工管理<div>",
					items : [ staffQueryCondPanel, staffGrid ]
				} ],
				tbar : new Ext.Toolbar({
					height : 55,
					items : [ staffAddBut, {
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
