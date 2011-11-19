// ----------------- 添加供应商  --------------------
supplierAddWin = new Ext.Window({
	layout : "fit",
	title : "添加供应商",
	width : 260,
	height : 210,
	closeAction : "hide",
	resizable : false,
	items : [ {
		layout : "form",
		id : "supplierAddForm",
		labelWidth : 40,
		border : false,
		frame : true,
		items : [ {
			xtype : "numberfield",
			fieldLabel : "编号",
			id : "supplierAddNumber",
			allowBlank : false,
			width : 180
		}, {
			xtype : "textfield",
			fieldLabel : "名称",
			id : "supplierAddName",
			allowBlank : false,
			width : 180
		}, {
			xtype : "textfield",
			fieldLabel : "电话",
			id : "supplierAddPhone",
			allowBlank : false,
			width : 180
		}, {
			xtype : "textfield",
			fieldLabel : "联系人",
			id : "supplierAddContact",
			allowBlank : false,
			width : 180
		}, {
			xtype : "textfield",
			fieldLabel : "地址",
			id : "supplierAddAddress",
			allowBlank : false,
			width : 180
		} ]
	} ],
	buttons : [
			{
				text : "确定",
				handler : function() {

					if (supplierAddWin.findById("supplierAddNumber").isValid()
							&& supplierAddWin.findById("supplierAddName")
									.isValid()
							&& supplierAddWin.findById("supplierAddPhone")
									.isValid()
							&& supplierAddWin.findById("supplierAddContact")
									.isValid()
							&& supplierAddWin.findById("supplierAddAddress")
									.isValid()) {

						var supplierAddNumber = supplierAddWin.findById(
								"supplierAddNumber").getValue();
						var supplierAddName = supplierAddWin.findById(
								"supplierAddName").getValue();
						var supplierAddPhone = supplierAddWin.findById(
								"supplierAddPhone").getValue();
						var supplierAddContact = supplierAddWin.findById(
								"supplierAddContact").getValue();
						var supplierAddAddress = supplierAddWin.findById(
								"supplierAddAddress").getValue();

						var isDuplicate = false;
						for ( var i = 0; i < supplierData.length; i++) {
							if (supplierAddNumber == supplierData[i][1]) {
								isDuplicate = true;
							}
						}

						if (!isDuplicate) {
							supplierAddWin.hide();

							Ext.Ajax.request({
								url : "../../InsertSupplier.do",
								params : {
									"pin" : pin,
									"supplierAlias" : supplierAddNumber,
									"supplierName" : supplierAddName,
									"supplierAddress" : supplierAddAddress,
									"supplierContact" : supplierAddContact,
									"supplierPhone" : supplierAddPhone
								},
								success : function(response, options) {
									var resultJSON = Ext.util.JSON
											.decode(response.responseText);
									if (resultJSON.success == true) {
										supplierStore.reload({
											params : {
												start : 0,
												limit : supplierPageRecordCount
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
								msg : "该供应商编号已存在！",
								width : 300,
								buttons : Ext.MessageBox.OK
							});
						}

					}

				}
			}, {
				text : "取消",
				handler : function() {
					supplierAddWin.hide();
				}
			} ],
	listeners : {
		"show" : function(thiz) {

			loadAllsupplier();

			supplierAddWin.findById("supplierAddNumber").setValue("");
			supplierAddWin.findById("supplierAddNumber").clearInvalid();

			supplierAddWin.findById("supplierAddName").setValue("");
			supplierAddWin.findById("supplierAddName").clearInvalid();

			supplierAddWin.findById("supplierAddPhone").setValue("");
			supplierAddWin.findById("supplierAddPhone").clearInvalid();

			supplierAddWin.findById("supplierAddContact").setValue("");
			supplierAddWin.findById("supplierAddContact").clearInvalid();

			supplierAddWin.findById("supplierAddAddress").setValue("");
			supplierAddWin.findById("supplierAddAddress").clearInvalid();

			var f = Ext.get("supplierAddNumber");
			f.focus.defer(100, f); // 为什么这样才可以！？！？

		}
	}
});

// --------------------------------------------------------------------------
var supplierAddBut = new Ext.ux.ImageButton({
	imgPath : "../../images/dishAdd.png",
	imgWidth : 50,
	imgHeight : 50,
	tooltip : "添加供应商",
	handler : function(btn) {
		supplierAddWin.show();
	}
});

// --
var pushBackBut = new Ext.ux.ImageButton({
	imgPath : "../../images/UserLogout.png",
	imgWidth : 50,
	imgHeight : 50,
	tooltip : "返回",
	handler : function(btn) {
		location.href = "InventoryProtal.html?restaurantID=" + restaurantID
				+ "&pin=" + pin;
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
var filterTypeData = [ [ "0", "全部" ], [ "1", "名称" ], [ "2", "电话" ],
		[ "3", "地址" ] ];
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

			// ------------------remove field-------------------
			if (!isAll) {
				searchForm.remove("conditionText");
			}

			// ------------------ add field -------------------
			if (index == 0) {
				// 全部
				isAll = true;
			} else if (index == 1) {
				// 名称
				searchForm.add(conditionText);
				isAll = false;
			} else if (index == 2) {
				// 电话
				searchForm.add(conditionText);
				isAll = false;
			} else if (index == 3) {
				// 地址
				searchForm.add(conditionText);
				isAll = false;
			}

			supplierQueryCondPanel.doLayout();
		}
	}
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
var supplierQueryCondPanel = new Ext.form.FormPanel({
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
						supplierStore.reload({
							params : {
								start : 0,
								limit : supplierPageRecordCount
							}
						});
					}
				}
			} ]
		} ]
	} ]
});

// operator function
function supplierDeleteHandler(rowIndex) {
	Ext.MessageBox.show({
		msg : "确定删除？",
		width : 300,
		buttons : Ext.MessageBox.YESNO,
		fn : function(btn) {
			if (btn == "yes") {
				var supplierID = supplierStore.getAt(rowIndex)
						.get("supplierID");

				Ext.Ajax.request({
					url : "../../DeleteSupplier.do",
					params : {
						"pin" : pin,
						"supplierID" : supplierID
					},
					success : function(response, options) {
						var resultJSON = Ext.util.JSON
								.decode(response.responseText);
						if (resultJSON.success == true) {
							supplierStore.reload({
								params : {
									start : 0,
									limit : supplierPageRecordCount
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

function supplierDishOpt(value, cellmeta, record, rowIndex, columnIndex, store) {
	return "<center><a href=\"javascript:supplierDeleteHandler(" + rowIndex
			+ ")\">" + "<img src='../../images/del.png'/>删除</a>" + "</center>";
};

// 1，表格的数据store
var supplierStore = new Ext.data.Store({
	proxy : new Ext.data.HttpProxy({
		url : "../../QuerySupplierMgr.do"
	}),
	reader : new Ext.data.JsonReader({
		totalProperty : "totalProperty",
		root : "root"
	}, [ {
		name : "supplierID"
	}, {
		name : "supplierAlias"
	}, {
		name : "supplierName"
	}, {
		name : "supplierPhone"
	}, {
		name : "supplierContact"
	}, {
		name : "supplierAddress"
	}, {
		name : "operator"
	}, {
		name : "message"
	} ])
});

// menuStore.reload();

// 2，栏位模型
var supplierColumnModel = new Ext.grid.ColumnModel([
		new Ext.grid.RowNumberer(), {
			header : "编号",
			sortable : true,
			dataIndex : "supplierAlias",
			width : 80
		// ,
		// editor : new Ext.form.TextField({
		// allowBlank : false,
		// allowNegative : false
		// ,
		// validator : function(v) {
		// var isDuplicate = false;
		// for ( var i = 0; i < supplierData.length; i++) {
		// if (v == supplierData[i][1]) {
		// isDuplicate = true;
		// }
		// }
		//
		// if (!isDuplicate) {
		// return true;
		// } else {
		// return "该供应商编号已存在！";
		// }
		// }
		// })
		}, {
			header : "名称",
			sortable : true,
			dataIndex : "supplierName",
			width : 100,
			editor : new Ext.form.TextField({
				allowBlank : false,
				allowNegative : false
			})
		}, {
			header : "电话",
			sortable : true,
			dataIndex : "supplierPhone",
			width : 100,
			editor : new Ext.form.TextField({
				allowBlank : false,
				allowNegative : false
			})
		}, {
			header : "联系人",
			sortable : true,
			dataIndex : "supplierContact",
			width : 90,
			editor : new Ext.form.TextField({
				allowBlank : false,
				allowNegative : false
			})
		}, {
			header : "地址",
			sortable : true,
			dataIndex : "supplierAddress",
			width : 180,
			editor : new Ext.form.TextField({
				allowBlank : false,
				allowNegative : false
			})
		}, {
			header : "<center>操作</center>",
			sortable : true,
			dataIndex : "operator",
			width : 180,
			renderer : supplierDishOpt
		} ]);

// -------------- layout ---------------
var supplierGrid;
Ext
		.onReady(function() {
			// 解决ext中文传入后台变问号问题
			Ext.lib.Ajax.defaultPostHeader += '; charset=utf-8';
			Ext.QuickTips.init();

			// ---------------------表格--------------------------
			supplierGrid = new Ext.grid.EditorGridPanel(
					{
						title : "供应商",
						xtype : "grid",
						anchor : "99%",
						region : "center",
						frame : true,
						margins : '0 5 0 0',
						ds : supplierStore,
						cm : supplierColumnModel,
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
								supplierGrid
										.getStore()
										.each(
												function(record) {
													if (record
															.isModified("supplierAlias") == true
															|| record
																	.isModified("supplierName") == true
															|| record
																	.isModified("supplierPhone") == true
															|| record
																	.isModified("supplierContact") == true
															|| record
																	.isModified("supplierAddress") == true) {
														modfiedArr
																.push(record
																		.get("supplierID")
																		+ " field_separator "
																		+ record
																				.get("supplierAlias")
																		+ " field_separator "
																		+ record
																				.get("supplierName")
																		+ " field_separator "
																		+ record
																				.get("supplierPhone")
																		+ " field_separator "
																		+ record
																				.get("supplierContact")
																		+ " field_separator "
																		+ record
																				.get("supplierAddress"));
													}
												});

								if (modfiedArr.length != 0) {
									// 獲取分頁表格的當前頁碼！神技！！！
									var toolbar = supplierGrid
											.getBottomToolbar();
									currPageIndex = toolbar.readPage(toolbar
											.getPageData());

									var modSuppliers = "";
									for ( var i = 0; i < modfiedArr.length; i++) {
										modSuppliers = modSuppliers
												+ modfiedArr[i]
												+ " record_separator ";
									}
									modSuppliers = modSuppliers.substring(0,
											modSuppliers.length - 18);

									Ext.Ajax
											.request({
												url : "../../UpdateSupplier.do",
												params : {
													"pin" : pin,
													"modSuppliers" : modSuppliers
												},
												success : function(response,
														options) {
													var resultJSON = Ext.util.JSON
															.decode(response.responseText);
													if (resultJSON.success == true) {
														supplierStore
																.reload({
																	params : {
																		start : (currPageIndex - 1)
																				* supplierPageRecordCount,
																		limit : supplierPageRecordCount
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
							pageSize : supplierPageRecordCount,
							store : supplierStore,
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
								supplierStore.reload({
									params : {
										start : 0,
										limit : supplierPageRecordCount
									}
								});
							}
						}
					});

			// 为store配置beforeload监听器
			supplierGrid.getStore()
					.on(
							'beforeload',
							function() {

								var queryTpye = filterTypeComb.getValue();
								if (queryTpye == "全部") {
									queryTpye = 0;
								}

								var queryValue = "";
								if (queryTpye != 0) {
									queryValue = searchForm.findById(
											"conditionText").getValue();
									if (!searchForm.findById("conditionText")
											.isValid()) {
										return false;
									}
								}

								// 输入查询条件参数
								this.baseParams = {
									"pin" : pin,
									"type" : queryTpye,
									"value" : queryValue,
									"isPaging" : true
								};

							});

			// 为store配置load监听器(即load完后动作)
			supplierGrid.getStore().on('load', function() {
				if (supplierGrid.getStore().getTotalCount() != 0) {
					var msg = this.getAt(0).get("message");
					if (msg != "normal") {
						Ext.MessageBox.show({
							msg : msg,
							width : 300,
							buttons : Ext.MessageBox.OK
						});
						this.removeAll();
					} else {
						// menuGrid
						// .getStore()
						// .each(
						// function(record) {
						// // 廚房顯示
						// for ( var i = 0; i < kitchenTypeData.length; i++) {
						// if (record
						// .get("kitchen") == kitchenTypeData[i][0]) {
						// record
						// .set(
						// "kitchenDisplay",
						// kitchenTypeData[i][1]);
						// }
						// }
						// // 菜品狀態顯示
						// record
						// .set(
						// "dishNameDisplay",
						// record
						// .get("dishName"));
						// if (record
						// .get("special") == true) {
						// record
						// .set(
						// "dishNameDisplay",
						// record
						// .get("dishNameDisplay")
						// + "<img src='../../images/icon_tip_te.gif'></img>");
						// }
						// if (record
						// .get("recommend") == true) {
						// record
						// .set(
						// "dishNameDisplay",
						// record
						// .get("dishNameDisplay")
						// + "<img
						// src='../../images/icon_tip_jian.gif'></img>");
						// }
						// if (record
						// .get("stop") == true) {
						// record
						// .set(
						// "dishNameDisplay",
						// record
						// .get("dishNameDisplay")
						// + "<img
						// src='../../images/icon_tip_ting.gif'></img>");
						// }
						// if (record
						// .get("free") == true) {
						// record
						// .set(
						// "dishNameDisplay",
						// record
						// .get("dishNameDisplay")
						// + "<img src='../../images/forFree.png'></img>");
						// }
						//
						// // 提交，去掉修改標記
						// record.commit();
						// });
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
					title : "<div style='font-size:20px;'>供应商管理<div>",
					items : [ supplierQueryCondPanel, supplierGrid ]
				} ],
				tbar : new Ext.Toolbar({
					height : 55,
					items : [ supplierAddBut, {
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
