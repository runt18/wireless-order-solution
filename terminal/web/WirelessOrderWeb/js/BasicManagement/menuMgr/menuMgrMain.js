var operatorData = [ [ '1', '等于' ], [ '2', '大于等于' ], [ '3', '小于等于' ] ];
var filterTypeData = [ [ '0', '全部' ], [ '1', '编号' ], [ '2', '名称' ], [ '3', '拼音' ], [ '4', '价格' ], [ '5', '厨房' ] ];

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
	} ]),
	listeners : {
		load : function() {
			// 解決combo初始值問題
			Ext.getCmp('kitchenTypeCombMA').setValue(kitchenTypeData[0][0]);
		}
	}
});

var kitchenTypeCombMA = new Ext.form.ComboBox({
	fieldLabel : "厨房",
	forceSelection : true,
	width : 180,
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
	width : 245,
	height : 255,
	modal : true,
	closeAction : "hide",
	resizable : false,
	items : [
	    {
	    	layout : "form",
			id : "menuAddForm",
			labelWidth : 30,
			border : false,
			frame : true,
			items : [ 
			    {
					xtype : "numberfield",
					fieldLabel : "编号",
					id : "menuAddNumber",
					allowBlank : false,
					width : 180
				}, {
					xtype : "textfield",
					fieldLabel : "菜名",
					id : "menuAddName",
					allowBlank : false,
					width : 180
				}, {
					xtype : "textfield",
					fieldLabel : "拼音",
					id : "menuAddSpill",
					// allowBlank : false,
					width : 180
				}, {
					xtype : "numberfield",
					fieldLabel : "价格",
					id : "menuAddPrice",
					allowBlank : false,
					width : 180,
					validator : function(v) {
						if (v >= 0 && v <= 99999.99) {
							return true;
						} else {
							return "价格范围是0.00至99999.99！";
						}
					}
				}, kitchenTypeCombMA, 
				{
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
					}, {
						layout : "form",
						border : false,
						labelSeparator : '',
						width : 70,
						labelWidth : 30,
						items : [ {
							xtype : "checkbox",
							id : "currPriceCheckboxMA",
							fieldLabel : "时价"
						} ]
					} ]
				} ]
			} ],
	buttons : [
	    {
	    	text : "确定",
			listeners : {
				"click" : function() {
					if (menuAddWin.findById("menuAddNumber").isValid()
							&& menuAddWin.findById("menuAddName").isValid()
							&& menuAddWin.findById("menuAddSpill").isValid()
							&& menuAddWin.findById("menuAddPrice").isValid()) {

						var dishNumber = menuAddWin.findById("menuAddNumber").getValue();
						var dishName = menuAddWin.findById("menuAddName").getValue();
						var dishSpill = menuAddWin.findById("menuAddSpill").getValue();
						var dishPrice = menuAddWin.findById("menuAddPrice").getValue();
						var kitchenAlias = kitchenTypeCombMA.getValue();
						var kitchenId = 0;
						// 前台：kitchenTypeData：[厨房编号,厨房名称,厨房id]
						for ( var i = 0; i < kitchenTypeData.length; i++) {
							if (kitchenAlias == kitchenTypeData[i][0]) {
								kitchenId = kitchenTypeData[i][2];
							}
						}
						var isSpecial = menuAddWin.findById("specialCheckboxMA").getValue();
						var isRecommend = menuAddWin.findById("recommendCheckboxMA").getValue();
						var isFree = menuAddWin.findById("freeCheckboxMA").getValue();
						var isStop = menuAddWin.findById("stopCheckboxMA").getValue();
						var isCurrPrice = menuAddWin.findById("currPriceCheckboxMA").getValue();

						var isDuplicate = false;
						for ( var i = 0; i < dishMultSelectData.length; i++) {
							if (dishNumber == dishMultSelectData[i][0]) {
								isDuplicate = true;
							}
						}

						if (!isDuplicate) {
							isPrompt = false;
							menuAddWin.hide();

							Ext.Ajax.request({
								url : "../../InsertMenu.do",
								params : {
									"pin" : Request["pin"],
									"dishNumber" : dishNumber,
									"dishName" : dishName,
									"dishSpill" : dishSpill,
									"dishPrice" : dishPrice,
									"kitchenAlias" : kitchenAlias,
									"kitchenId" : kitchenId,
									"isSpecial" : isSpecial,
									"isRecommend" : isRecommend,
									"isFree" : isFree,
									"isStop" : isStop,
									"isCurrPrice" : isCurrPrice
								},
								success : function(response, options) {
									var resultJSON = Ext.util.JSON.decode(response.responseText);
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
	        }
	    }, {
			text : "取消",
			handler : function() {
				isPrompt = false;
				menuAddWin.hide();
			}
		}
    ],
	keys : [ 
	    {
	    	key : Ext.EventObject.ENTER,
			fn : function() {
				menuAddWin.buttons[0].fireEvent("click");
			},
			scope : this
		} 
	],
	listeners : {
		"show" : function(thiz) {
			loadAllDishes();
					
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
			menuAddWin.findById("currPriceCheckboxMA").setValue(false);

			var f = Ext.get("menuAddNumber");
			f.focus.defer(100, f); // 为什么这样才可以！？！？
		},
		"hide" : function(thiz) {
			isPrompt = false;
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
	} ]),
	listeners : {
		load : function() {
			// 解決combo初始值問題
			Ext.getCmp('kitchenTypeCombMM').setValue(menuStore.getAt(currRowIndex).get("kitchenAlias"));
		}
	}
});

var kitchenTypeCombMM = new Ext.form.ComboBox({
	fieldLabel : "厨房",
	forceSelection : true,
	width : 180,
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
	width : 245,
	height : 255,
	closeAction : "hide",
	resizable : false,
	items : [
	     {
			layout : "form",
			labelWidth : 30,
			border : false,
			frame : true,
			items : [ 
			    {
					xtype : "numberfield",
					fieldLabel : "编号",
					id : "menuModNumber",
					disabled : true,
					width : 180
				}, {
					xtype : "textfield",
					fieldLabel : "菜名",
					id : "menuModName",
					allowBlank : false,
					width : 180
				}, {
					xtype : "textfield",
					fieldLabel : "拼音",
					id : "menuModSpill",
					// allowBlank : false,
					width : 180
				}, {
					xtype : "numberfield",
					fieldLabel : "价格",
					id : "menuModPrice",
					allowBlank : false,
					width : 180
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
					}, {
						layout : "form",
						border : false,
						labelSeparator : '',
						width : 70,
						labelWidth : 30,
						items : [ {
							xtype : "checkbox",
							id : "currPriceCheckboxMM",
							fieldLabel : "时价"
						} ]
					} ]
				} ]
			} ],
	buttons : [
	    {
	    	text : "确定",
			handler : function() {
			if (menuModifyWin.findById("menuModNumber").isValid()
					&& menuModifyWin.findById("menuModName").isValid()
					&& menuModifyWin.findById("menuModSpill").isValid()
					&& menuModifyWin.findById("menuModPrice").isValid()) {
				isPrompt = false;
				menuModifyWin.hide();

				var foodID = menuStore.getAt(currRowIndex).get("foodID");
				var dishNumber = menuModifyWin.findById("menuModNumber").getValue();
				var dishName = menuModifyWin.findById("menuModName").getValue();
				var dishSpill = menuModifyWin.findById("menuModSpill").getValue();
				var dishPrice = menuModifyWin.findById("menuModPrice").getValue();
				var kitchenAlias = kitchenTypeCombMM.getValue();
				// 前台：kitchenTypeData：[厨房编号,厨房名称,厨房id]
				var kitchenId = 0;
				for ( var i = 0; i < kitchenTypeData.length; i++) {
					if (kitchenAlias == kitchenTypeData[i][0]) {
						kitchenId = kitchenTypeData[i][2];
					}
				}

				var isSpecial = menuModifyWin.findById("specialCheckboxMM").getValue();
				var isRecommend = menuModifyWin.findById("recommendCheckboxMM").getValue();
				var isFree = menuModifyWin.findById("freeCheckboxMM").getValue();
				var isStop = menuModifyWin.findById("stopCheckboxMM").getValue();
				var isCurrPrice = menuModifyWin.findById("currPriceCheckboxMM").getValue();

				Ext.Ajax.request({
					url : "../../UpdateMenu.do",
					params : {
						"pin" : Request["pin"],
						"foodID" : foodID,
						"dishNumber" : dishNumber,
						"dishName" : dishName,
						"dishSpill" : dishSpill,
						"dishPrice" : dishPrice,
						"kitchenId" : kitchenId,
						"kitchenAlias" : kitchenAlias,
						"isSpecial" : isSpecial,
						"isRecommend" : isRecommend,
						"isFree" : isFree,
						"isStop" : isStop,
						"isCurrPrice" : isCurrPrice
					},
					success : function(response, options) {
						var resultJSON = Ext.util.JSON.decode(response.responseText);
						if (resultJSON.success == true) {
							menuStore.reload({
								params : {
									start : (currPageIndex - 1) * dishesPageRecordCount,
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
		}, {
			text : "取消",
			handler : function() {
				isPrompt = false;
					menuModifyWin.hide();
				}
		} 
	],
	listeners : {
		"show" : function(thiz) {
			kitchenTypeStoreMM.reload();
		},
		"hide" : function(thiz) {
			isPrompt = false;
		}
	}
});

// --------------------------------------------------------------------------
var dishAddBut = new Ext.ux.ImageButton({
	imgPath : "../../images/material_add.png",
	imgWidth : 50,
	imgHeight : 50,
	tooltip : "添加新菜",
	handler : function(btn) {
		if (!isPrompt) {
			isPrompt = true;
			menuAddWin.show();
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
		location.href = "BasicMgrProtal.html?restaurantID=" + restaurantID
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
var filterTypeComb = new Ext.form.ComboBox({
	forceSelection : true,
	readOnly : true,
	width : 100,
	value : '全部',
	id : 'filter',
	store : new Ext.data.SimpleStore({
		fields : [ 'value', 'text' ],
		data : filterTypeData
	}),
	valueField : 'value',
	displayField : 'text',
	typeAhead : true,
	mode : 'local',
	triggerAction : 'all',
	selectOnFocus : true,
	allowBlank : false,
	listeners : {
		select : function(combo, record, index) {
			var ktCombo = Ext.getCmp('kitchenTypeComb');
			var oCombo = Ext.getCmp('operator');
			var ct = Ext.getCmp('conditionText');
			var cn = Ext.getCmp('conditionNumber');
			
			// ------------------ show or hide field -------------------
			if (index == 0) {
				// 全部
				ktCombo.setVisible(false);
				oCombo.setVisible(false);
				ct.setVisible(false);
				cn.setVisible(false);
				conditionType = '';
			} else if (index == 1 || index == 4) {
				// 编号 或 价格
				ktCombo.setVisible(false);
				oCombo.setVisible(true);
				cn.setVisible(true);
				ct.setVisible(false);
				oCombo.setValue(1);
				cn.setValue('');
				conditionType = cn.getId();
			} else if (index == 2 || index == 3) {
				// 名称 或 拼音
				ktCombo.setVisible(false);
				oCombo.setVisible(false);
				cn.setVisible(false);
				ct.setVisible(true);
				ct.setValue('');
				conditionType = ct.getId();
			} else if (index == 5) {
				// 厨房
				ktCombo.setVisible(true);
				oCombo.setVisible(false);
				cn.setVisible(false);
				ct.setVisible(false);
				ktCombo.store.loadData(kitchenTypeData);
				ktCombo.setValue(kitchenTypeData[0][0]);	
				conditionType = ktCombo.getId();
			}
		}
	}
});

// operator function
function dishModifyHandler(rowIndex) {
	// 獲取分頁表格的當前頁碼！神技！！！
	var toolbar = menuGrid.getBottomToolbar();
	currPageIndex = toolbar.readPage(toolbar.getPageData());

	var currRecord = menuStore.getAt(rowIndex);
	menuModifyWin.findById("menuModNumber").setValue(currRecord.get("dishNumber"));
	menuModifyWin.findById("menuModName").setValue(currRecord.get("dishName"));
	menuModifyWin.findById("menuModSpill").setValue(currRecord.get("dishSpill"));
	menuModifyWin.findById("menuModPrice").setValue(currRecord.get("dishPrice"));

	menuModifyWin.findById("specialCheckboxMM").setValue(currRecord.get("special"));
	menuModifyWin.findById("recommendCheckboxMM").setValue(currRecord.get("recommend"));
	menuModifyWin.findById("freeCheckboxMM").setValue(currRecord.get("free"));
	menuModifyWin.findById("stopCheckboxMM").setValue(currRecord.get("stop"));
	menuModifyWin.findById("currPriceCheckboxMM").setValue(currRecord.get("currPrice"));
	
	if (!isPrompt) {
		isPrompt = true;
		menuModifyWin.show();
	}
	
};

function dishDeleteHandler(rowIndex) {
	Ext.MessageBox.show({
		msg : "确定删除？",
		width : 300,
		buttons : Ext.MessageBox.YESNO,
		fn : function(btn) {
			if (btn == "yes") {
				var foodID = menuStore.getAt(rowIndex).get("foodID");

				Ext.Ajax.request({
					url : "../../DeleteMenu.do",
					params : {
						"pin" : pin,
						"foodID" : foodID
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
// 编号，名称，拼音，价格，厨房打印，操作，特,荐,停,送,時
var menuStore = new Ext.data.Store({
	proxy : new Ext.data.HttpProxy({
		url : "../../QueryMenuMgr.do"
	}),
	reader : new Ext.data.JsonReader({
		totalProperty : "totalProperty",
		root : "root"
	}, [ {
		name : "foodID"
	}, {
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
		name : "kitchenAlias"
	}, {
		name : "kitchenDisplay"
	}, {
		name : "kitchenID"
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
		name : "currPrice"
	}, {
		name : "message"
	}, {
		name : 'tasteRefType'
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
	width : 90,
	align : 'right',
	renderer : Ext.ux.txtFormat.gridDou
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


//2，栏位模型
function materialDeleteHandler(rowIndex) {
	Ext.MessageBox.show({
		msg : "确定删除？",
		width : 300,
		buttons : Ext.MessageBox.YESNO,
		fn : function(btn) {
			if (btn == "yes") {
				var foodID = menuStore.getAt(currRowIndex).get("foodID");
				var materialID = materialStore.getAt(rowIndex).get("materialId");

				Ext.Ajax.request({
					url : "../../DeleteFoodMaterial.do",
					params : {
						"pin" : pin,
						"foodID" : foodID,
						"materialID" : materialID
					},
					success : function(response, options) {
						var resultJSON = Ext.util.JSON
								.decode(response.responseText);
						if (resultJSON.success == true) {

							loadFoodMaterial();

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
						Ext.getCmp('addMaterialTabBtnRefresh').handler();
					},
					failure : function(response, options) {
						Ext.getCmp('addMaterialTabBtnRefresh').handler();
					}
				});
			}
		}
	});
};

// easten panel
var materialStore = new Ext.data.Store({
	proxy : new Ext.data.MemoryProxy(materialData),
	reader : new Ext.data.JsonReader(Ext.ux.readConfig, [ {
		name : "materialId"
	}, {
		name : "materialAlias"
	}, {
		name : "materialName"
	}, {
		name : "consumption"
	}, {
		name : "cateId"
	} , {
		name : "materialOpt"
	} ])
});

function materialOpt(value, cellmeta, record, rowIndex, columnIndex, store) {
	return "<center><a href=\"javascript:materialDeleteHandler(" + rowIndex
			+ ")\">" + "<img src='../../images/del.png'/>删除</a>" + "</center>";
};

var materialColumnModel = new Ext.grid.ColumnModel([
		new Ext.grid.RowNumberer(), {
			header : "编号",
			sortable : true,
			dataIndex : "materialAlias",
			width : 50
		}, {
			header : "食材",
			sortable : true,
			dataIndex : "materialName",
			width : 80
		}, {
			header : "消耗",
			sortable : true,
			dataIndex : "consumption",
			width : 80
		}, {
			header : '<center>操作</center>',
			sortable : true,
			dataIndex : 'materialOpt',
			width : 80,
			renderer : materialOpt
		} ]);

var materialGrid = new Ext.grid.GridPanel({
	xtype : 'grid',
	border : false,
	autoScroll : true,
	ds : materialStore,
	cm : materialColumnModel,
	sm : new Ext.grid.RowSelectionModel({
		singleSelect : true
	}),
	listeners : {
		rowclick : function(thiz, index, e){
			mmObj.mgIndex = index;
			var rowData = materialStore.getAt(index).data;
			var cateId = parseInt(rowData.cateId);
			Ext.getCmp('materialAddCost').setValue(rowData.consumption);
			
			Ext.getCmp('materialCateCombAdd').setValue(cateId);
			Ext.getCmp('materialAddComb').store.removeAll();
			materialComboDisplayData.length = 0;
			for ( var i = 0; i < materialComboData.length; i++) {
				if (materialComboData[i][2] == cateId) {
					materialComboDisplayData.push([
						materialComboData[i][0],
						materialComboData[i][1],
						materialComboData[i][2]
					]);
				}
			}
			Ext.getCmp('materialAddComb').store.loadData(materialComboDisplayData);
			Ext.getCmp('materialAddComb').setValue(rowData.materialId);
		}
	}
});

//-------------- 關聯食材 ---------------
var materialCateCombAdd = new Ext.form.ComboBox({
	fieldLabel : '种类',
	forceSelection : true,
	width : 100,
	id : 'materialCateCombAdd',
	store : new Ext.data.SimpleStore({
		fields : [ 'value', 'text' ],
		data : []
	}),
	valueField : 'value',
	displayField : 'text',
	typeAhead : true,
	mode : 'local',
	triggerAction : 'all',
	selectOnFocus : true,
	listeners : {
		select : function(combo, record, index) {
			var selectedCateID = record.get('value');
			materialAddStore.removeAll();
			materialComboDisplayData.length = 0;
			for ( var i = 0; i < materialComboData.length; i++) {
				if (materialComboData[i][2] == selectedCateID) {
					materialComboDisplayData.push([
						materialComboData[i][0],
						materialComboData[i][1],
						materialComboData[i][2] ]);
				}
			}
			materialAddStore.loadData(materialComboDisplayData);
			if (materialComboDisplayData.length > 0) {
				materialAddComb.setValue(materialComboDisplayData[0][0]);
			} else {
				materialAddComb.setValue();
			}
		},
		expand : function(){
		}
	}
});

//material add
var materialAddStore = new Ext.data.SimpleStore({
	fields : [ 'value', 'text', 'cateID' ],
	data : []
});

var materialAddComb = new Ext.form.ComboBox({
	fieldLabel : '食材',
	forceSelection : true,
	width : 100,
	id : 'materialAddComb',
	store : materialAddStore,
	valueField : 'value',
	displayField : 'text',
	typeAhead : true,
	mode : 'local',
	triggerAction : 'all',
	selectOnFocus : true,
	readOnly : true,
	listeners : {
		expand : function(){
//			this.list.dom.childNodes[0].style.width = this.width + 13;
//			this.list.setWidth(this.width + 13);
		}
	}
});

var addMaterialPanel = new Ext.Panel({
	id : 'materialAddForm',
	border : false,
	frame : true,
	height : 65,
	layout : 'column',
	defaults : {
		labelWidth : 35
	},
	items : [ 
	    {
	    	xtype : 'form',
	    	width : 160,
	    	items : [materialCateCombAdd]
	    },
	    {
	    	xtype : 'form',
	    	width : 160,
			items : [
			    {
			    	xtype : 'numberfield',
					fieldLabel : '消耗',
					id : 'materialAddCost',
					width : 115
			    }
			]
		},
		{
	    	xtype : 'form',
	    	width : 160,
	    	items : [materialAddComb]
	    }
	]
});

var addMaterialTab = new Ext.Panel({
	title : '食材关联',
	id : 'addMaterialTab',
	tbar : new Ext.Toolbar({
		height : 26,
		items : [
		    '->', 
		    {
		    	text:'添加', 
		    	iconCls : 'btn_add',
		    	handler:function(e){
		    		
		    		if(Ext.ux.getSelData('menuMgrGrid') == false){
		    			return false;
		    		}
		    		
		    		var checkst = Ext.ux.RegCheck([
		    		    {id:'materialCateCombAdd'},
		    		    {id:'materialAddCost'},
		    		    {id:'materialAddComb'}
		    		]);
		    		
		    		if(!checkst){
		    			return false;
		    		}
		    		var foodID = menuStore.getAt(currRowIndex).get('foodID');
					var cost = Ext.getCmp('materialAddCost').getValue();
					var materialID = materialAddComb.getValue();
						
					var isDuplicate = false;
					materialGrid.getStore().each(function(record) {
						if (record.get('materialId') == materialID) {
							isDuplicate = true;
						}
					});
					if (!isDuplicate) {
						Ext.Ajax.request({
							url : '../../InsertFoodMaterial.do',
							params : {
								'pin' : pin,
								'materialID' : materialID,
								'foodID' : foodID,
								'cost' : cost
							},
							success : function(response, options) {
								var resultJSON = Ext.util.JSON.decode(response.responseText);
								if (resultJSON.success == true) {
									loadFoodMaterial();
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
								Ext.getCmp('addMaterialTabBtnRefresh').handler();
							},
							failure : function(response, options) {
								Ext.getCmp('addMaterialTabBtnRefresh').handler();
							}
						});
					} else {
						Ext.MessageBox.show({
							msg : '改食材已存在，不能重复添加！',
							width : 300,
							buttons : Ext.MessageBox.OK
						});
					}
		    	}
		    }, {
		    	text : '修改',
		    	iconCls : 'btn_edit',
		    	handler : function(e){
		    		
		    		if(Ext.ux.getSelData('menuMgrGrid') == false){
		    			return false;
		    		}
		    		
		    		var checkst = Ext.ux.RegCheck([
		    		    {id:'materialAddComb'}
		    		]);
		    		
		    		if(!checkst){
		    			return false;
		    		}
		    		var foodID = menuStore.getAt(currRowIndex).get('foodID');
					var cost = Ext.getCmp('materialAddCost').getValue();
					var materialID = Ext.getCmp('materialAddComb').getValue();
		    		
		    		Ext.Ajax.request({
						url : '../../UpdateFoodMaterial.do',
						params : {
							restaurantId : restaurantID,
							foodId : foodID,
							materailId : materialID,
							consumption : cost
						},
						success : function(response, options) {
							var resultJSON = Ext.util.JSON.decode(response.responseText);
							Ext.MessageBox.show({
								title : '提示',
								msg : resultJSON.msg,
								width : 300,
								buttons : Ext.MessageBox.OK
							});
							Ext.getCmp('addMaterialTabBtnRefresh').handler();
							loadFoodMaterial();
						},
						failure : function(response, options) {
							var resultJSON = Ext.util.JSON.decode(response.responseText);
							Ext.MessageBox.show({
								title : '提示',
								msg : resultJSON.msg,
								width : 300,
								buttons : Ext.MessageBox.OK
							});
							Ext.getCmp('addMaterialTabBtnRefresh').handler();
						}
					});
		    	}
		    }, {
		    	text : '删除',
		    	iconCls : 'btn_delete',
		    	handler : function(){
		    		var selRow = materialGrid.getSelectionModel().getSelections();
		    		if(selRow.length == 1){
		    			materialDeleteHandler(mmObj.mgIndex);
		    		}
		    	}
		    }, {
		    	text : '重置',
		    	id : 'addMaterialTabBtnRefresh',
		    	iconCls : 'btn_refresh',
		    	handler : function(){
		    		resetMaterialPanel();
		    	}
		    }
		] 
	}),
	items : [addMaterialPanel, materialGrid],
	listeners : {
		resize : function(thiz, aw, ah, rw, rh){
			var gh = parseInt(ah - 26 - addMaterialPanel.height);
			materialGrid.setHeight(gh);
		}
	}
});

var commonTasteGrid = createGridPanel(
    'commonTasteGrid',
    '',
    '',
    '',
    '../../QueryFoodTaste.do',
    [
	    [true, false, false, false], 
	    ['口味','tasteName','100'] , 
	    ['价钱','tastePrice', '60','right','Ext.ux.txtFormat.gridDou'], 
	    ['比例','tasteRate','60','right','Ext.ux.txtFormat.gridDou'], 
	    ['计算方式', 'tasteCalcFormat']
	],
	['tasteID', 'tasteAlias','tasteName','tastePrice','tasteRate', 'tasteCategory', 'tasteCalc', 'tasteCalcFormat', 'foodID', 'foodName'],
    [['foodID',0], ['resturantID', restaurantID]],
    0
);
commonTasteGrid.border = false;
commonTasteGrid.frame = false;

tasteCalcRenderer = function(val){
	val = parseInt(val);
	if(val == 0){
		return '按价格';
	}else if(val == 1){
		return '按比例';
	}
};

var allTasteGridTbar = new Ext.Toolbar({
	height : 26,
	items : [
	    {
	    	xtype:'tbtext', text:'&nbsp;口味名搜索:&nbsp;'
	    },
	    {
	    	xtype : 'textfield',
	    	id : 'txtTasteNameSearch',
	    	width : 100,
	    	listeners : {
	    		render : function(e){
	    			Ext.getDom('txtTasteNameSearch').onkeyup = function(){
	    				var txtTasteName = Ext.getCmp('txtTasteNameSearch').getValue().trim();
	    				var store = allTasteGrid.getStore();
	    				var selModel = allTasteGrid.getSelectionModel();
	    				var searchData = {root:[]}, orderByData = [], otherData = [], selIndex = [];
	    				if(selModel.getSelections().length > 0){
	    					selModel.clearSelections();
	    				}
	    				if(txtTasteName.length == 0){
	    					for(var i = 0; i < store.getCount(); i++){
		    					var selRow = allTasteGrid.getView().getRow(i);
		    					selRow.style.backgroundColor = '#FFFFFF';
		    				}
	    					return;
	    				}
	    				for(var i = 0; i < store.getCount(); i++){
	    					if(store.getAt(i).data.tasteName.indexOf(txtTasteName) >= 0 ){
	    						orderByData.push(store.getAt(i).data);	    						
	    					}else{
	    						otherData.push(store.getAt(i).data);
	    					}
	    				}
	    				for(var i = 0; i < orderByData.length; i++){
	    					searchData.root.push(orderByData[i]);
	    					selIndex.push(i);
	    				}
	    				for(var i = 0; i < otherData.length; i++){
	    					searchData.root.push(otherData[i]);
	    				}
	    				store.loadData(searchData);
	    				for(var i = 0; i < searchData.root.length; i++){
	    					var selRow = allTasteGrid.getView().getRow(i);
	    					if(i < orderByData.length){
	    						selRow.style.backgroundColor = '#FFFF00';
	    					}else{
	    						selRow.style.backgroundColor = '#FFFFFF';
	    					}
	    				}
	    			};
	    		}
	    	}
	    }
	]
});
var allTasteGrid = createGridPanel(
	'allTasteGrid',
	'',
	'',
	'',
	'../../QueryTaste.do',
	[
	    [true, false, true, false], 
	    ['口味','tasteName','100'] , 
	    ['价钱','tastePrice', '60','right','Ext.ux.txtFormat.gridDou'], 
	    ['比例','tasteRate','60','right','Ext.ux.txtFormat.gridDou'], 
	    ['计算方式', 'tasteCalc','','','tasteCalcRenderer']
	],
	['tasteID', 'tasteAlias','tasteName','tastePrice','tasteRate', 'tasteCategory', 'tasteCalc'],
	[['pin',pin], ['type',0], ['isCombo',false], ['isPaging',false]],
	0,
	'',
	allTasteGridTbar
);
allTasteGrid.border = false;
allTasteGrid.frame = false;
allTasteGrid.on('rowclick', function(thiz, ri, e){
	
});
allTasteGrid.getStore().on('load', function(e){
	mmObj.allTasteGridData = e.data;
});

addTasteTabBtnAddFn = function(e){
	var mmds = Ext.ux.getSelData('menuMgrGrid');
	var atds = Ext.ux.getSelData('allTasteGrid');
	var ctStore = Ext.getCmp('commonTasteGrid').getStore();
	if(mmds == false || atds == false){
		return;
	}
	for(var i = 0; i < ctStore.getCount(); i++){
		if(ctStore.getAt(i).data.tasteID == atds.tasteID){
			Ext.MessageBox.show({
				title : '提示',
				msg : String.format('口味<{0}>已关联菜品<{1}>',atds.tasteName, ctStore.getAt(i).get('foodName')),
				width : 300,
				buttons : Ext.MessageBox.OK
			});
			return;
		}
	}
	e.setDisabled(true);
	Ext.Ajax.request({
		url : "../../InsertFoodTaste.do",
		params : {
			time : new Date(),
			foodID : mmds.foodID,
			restaurantID : restaurantID,
			tasteID : atds.tasteID
		},
		success : function(response, options) {
			e.setDisabled(false);
			var jsonResult = Ext.util.JSON.decode(response.responseText);
			Ext.ux.showMsg(jsonResult);
			if(eval(jsonResult.success)){
				refreshTasteRefTypeData(mmds.foodID);
			}
		},
		failure : function(response, options) {
			e.setDisabled(false);
			var jsonResult = Ext.util.JSON.decode(response.responseText);
			Ext.ux.showMsg(jsonResult);
		}
	});
};
addTasteTabBtnDeleteFn = function(e){
	var ctds = Ext.ux.getSelData('commonTasteGrid');
	if(ctds == false){
		return;
	}
	e.setDisabled(true);
	Ext.Ajax.request({
		url : "../../DeleteFoodTaste.do",
		params : {
			time : new Date(),
			foodID : ctds.foodID,
			restaurantID : restaurantID,
			tasteID : ctds.tasteID
		},
		success : function(response, options) {
			e.setDisabled(false);
			var jsonResult = Ext.util.JSON.decode(response.responseText);
			Ext.ux.showMsg(jsonResult);
			if(eval(jsonResult.success)){
				refreshTasteRefTypeData(ctds.foodID);
			}
		},
		failure : function(response, options) {
			e.setDisabled(false);
			var jsonResult = Ext.util.JSON.decode(response.responseText);
			Ext.ux.showMsg(jsonResult);
		}
	});
};

addTasteTabBtnEditFn = function(e){
	var mgds = Ext.ux.getSelData('menuMgrGrid');
	if(!mgds){
		return;
	}
	var tasteID = '';
	if(parseInt(mmObj.rdoTasteType) == 2){
		var tpds = Ext.getCmp('commonTasteGrid').getStore();
		if(tpds.getCount() == 0){
			return;
		}
		for(var i = 0; i < tpds.getCount(); i++){
			tasteID += (i > 0 ? ',' : '');
			tasteID += tpds.getAt(i).data.tasteID;
		}
	}
	e.setDisabled(true);
	Ext.Ajax.request({
		url : '../../UpdateFoodTaste.do',
		params : {
			foodID : mgds.foodID,
			restaurantID : restaurantID,
			nValue : mmObj.rdoTasteType,
			oValue : mgds.tasteRefType,
			tasteID : tasteID
		},
		success : function(response, options){
			e.setDisabled(false);
			var jsonResult = Ext.util.JSON.decode(response.responseText);
			Ext.ux.showMsg(jsonResult);
			if(eval(jsonResult.success)){
				refreshTasteRefTypeData(mgds.foodID);
			}
		},
		failure : function(response, options){
			e.setDisabled(false);
			var jsonResult = Ext.util.JSON.decode(response.responseText);
			Ext.ux.showMsg(jsonResult);
		}
	});
	
};

var addTasteTab = new Ext.Panel({
	title : '口味关联',
	id : 'addTasteTab',
	layout : 'fit',
	items : [
		{
			xtype : 'panel',
			id : 'addTasteTabContent',
			border : false,
			layout : 'accordion',
			layoutConfig : {
				animate : true
			},
			tbar : new Ext.Toolbar({
				height : 26,
				items : [
				    '->',
				    {
				    	xtype : 'radio',
				    	width : 130,
				    	id : 'rdoTasteTypeSmart',
				    	name : 'rdoTasteType',
				    	checked : true,
				    	boxLabel : '智能关联',
				    	inputValue : 1,
				    	listeners : {
				    		render : function(e){
				    			Ext.getDom(e.getId()).onclick = function(){
				    				if(e.getValue()){
				    					setFiledDisabled(true, ['addTasteTabBtnAdd','addTasteTabBtnDelete','allTasteGridPanel']);
					    				Ext.getCmp('commonTasteGridPanel').expand(true);
					    				mmObj.rdoTasteType = e.getRawValue();
					    			}
				    			};
				    			Ext.getCmp('rdoTasteTypeSmart').setValue(true);
				    			Ext.getDom('rdoTasteTypeSmart').onclick();
				    		}
				    	}
				    }, {
				    	xtype : 'radio',
				    	width : 130,
				    	id : 'rdoTasteTypeManual',
				    	name : 'rdoTasteType',
				    	boxLabel : '人工关联',
				    	inputValue : 2,
				    	listeners : {
				    		render : function(e){
				    			Ext.getDom(e.getId()).onclick = function(){
				    				if(e.getValue()){
				    					setFiledDisabled(false, ['addTasteTabBtnAdd','addTasteTabBtnDelete','allTasteGridPanel']);
					    				Ext.getCmp('commonTasteGridPanel').expand(true);
					    				Ext.getDom('txtTasteNameSearch').value = '';
					    				Ext.getDom('txtTasteNameSearch').onkeyup();
					    				mmObj.rdoTasteType = e.getRawValue();
					    			}
				    			};
				    		}
				    	}
				    }
				]
			}),
			items : [
				{
					xtype : 'panel',
					id : 'commonTasteGridPanel',
					title : '已关联口味',
					layout : 'fit',
					border : false,
					items : [commonTasteGrid]
				}, {
					xtype : 'panel',
					id : 'allTasteGridPanel',
					title : '所有口味',
					layout : 'fit',
					border : false,
					items : [allTasteGrid]
				}
			]
		}
	],
	tbar : new Ext.Toolbar({
		height : 26,
		items : [
		    '->',
		    {
		    	text : '添加', 
		    	id : 'addTasteTabBtnAdd',
		    	iconCls : 'btn_add',
		    	handler : function(e){
		    		addTasteTabBtnAddFn(e);
		    	}
		    }, {
		    	text : '修改',
		    	id : 'addTasteTabBtnEdit',
		    	iconCls : 'btn_edit',
		    	handler : function(e){
		    		addTasteTabBtnEditFn(e);
		    	}
		    }, {
		    	text : '删除',
		    	id : 'addTasteTabBtnDelete',
		    	iconCls : 'btn_delete',
		    	handler : function(e){
		    		addTasteTabBtnDeleteFn(e);
		    	}
		    }
		]
	}),
	listeners : {
		render : function(){
			
		}
	}
});

var materialPanel = new Ext.Panel({
	region : 'east',
	title : '&nbsp;',
	layout : 'fit',
	collapsible : true,
	collapsed : true,
	titleCollapse : true,
	frame : true,
	width : 350,
	items : [new Ext.TabPanel({
		id : 'materialPanelTab',
		activeTab: 0,
		tabPosition : 'bottom',
		items : [
		    addMaterialTab,
		    addTasteTab
		],
		listeners : {
			tabchange : function(e, p){
				p.doLayout();
			}
		}
	})],
	listeners : {
		expand : function(){
			Ext.getCmp('addMaterialTabBtnRefresh').handler();
		}
	}
});

resetMaterialPanel = function(){
	Ext.getCmp('materialCateCombAdd').setValue();
	Ext.getCmp('materialAddCost').setValue();
	Ext.getCmp('materialAddComb').setValue();
	Ext.getCmp('materialAddComb').store.removeAll();
};

setFiledDisabled = function(start, idList){
	var st = true;
	st = typeof(start) == 'boolean' ? start : st;
	for(var i = 0; i < idList.length; i++){
		var tp = Ext.getCmp(idList[i]);
		if(tp){
			tp.setDisabled(st);
		}
	}
};

/**
 * 强制同步更新页面菜品口味关联方式,确保数据准确
 */
refreshTasteRefTypeData = function(foodID){
	Ext.getCmp('commonTasteGridPanel').expand(true);
	Ext.getCmp('commonTasteGrid').getStore().load();
	// 强制同步更新页面菜品口味关联方式
	var mgd = Ext.getCmp('menuMgrGrid').getStore();
	for(var i = 0; i < mgd.getCount(); i++){
		if(mgd.getAt(i).get('foodID') == foodID){
			mgd.getAt(i).set('tasteRefType', 2);
		}
	}
};

// -------------- layout ---------------
var menuGrid;
Ext.onReady(function() {
	// 解决ext中文传入后台变问号问题
	Ext.lib.Ajax.defaultPostHeader += '; charset=utf-8';
	Ext.QuickTips.init();

	// ---------------------表格--------------------------
	menuGrid = new Ext.grid.GridPanel({
		xtype : 'grid',
		id : 'menuMgrGrid',
		region : 'center',
		frame : true,
		margins : '0 0 0 0',
		ds : menuStore,
		cm : menuColumnModel,
		sm : new Ext.grid.RowSelectionModel({
			singleSelect : true
		}),
		viewConfig : {
			forceFit : true
		},
		tbar : new Ext.Toolbar({
			height : 26,
			items : [
			    { xtype:'tbtext', text:'过滤:'},
				filterTypeComb,
				{ xtype:'tbtext', text:'&nbsp;&nbsp;'},
				{
					xtype : 'combo',
					hidden : true,
					hideLabel : true,
					forceSelection : true,
					width : 100,
					id : 'operator',
					value : '等于',
					rawValue : 1,
					store : new Ext.data.SimpleStore({
						fields : [ 'value', 'text' ],
						data : operatorData
					}),
					valueField : 'value',
					displayField : 'text',
					typeAhead : true,
					mode : 'local',
					triggerAction : 'all',
					selectOnFocus : true,
					readOnly : true,
					allowBlank : false
				}, 
				{ xtype:'tbtext', text:'&nbsp;&nbsp;'},
				{
					xtype : 'textfield',
					id : 'conditionText',
					hidden : true,
					width : 120
				}, 
				{
					xtype: 'numberfield',
					id : 'conditionNumber',
					style: 'text-align: left;',
					hidden : true,
					width : 120
				},
				{
					xtype : 'combo',
					forceSelection : true,
					hidden : true,
					width : 120,
					id : 'kitchenTypeComb',
					store : new Ext.data.SimpleStore({
						fields : [ 'value', 'text' ]
					}),
					valueField : 'value',
					displayField : 'text',
					typeAhead : true,
					mode : 'local',
					triggerAction : 'all',
					selectOnFocus : true
				},
				{ xtype:'tbtext', text:'&nbsp;&nbsp;&nbsp;&nbsp;特价:'},
				{
					xtype : 'checkbox',
					id : 'specialCheckbox'
				}, 
				{ xtype:'tbtext', text:'&nbsp;&nbsp;&nbsp;&nbsp;推荐:'},
				{
					xtype : 'checkbox',
					id : 'recommendCheckbox'
				}, 
				{ xtype:'tbtext', text:'&nbsp;&nbsp;&nbsp;&nbsp;赠送:'},
				{
					xtype : 'checkbox',
					id : 'freeCheckbox'
				},
				{ xtype:'tbtext', text:'&nbsp;&nbsp;&nbsp;&nbsp;停售:'},
				{
					xtype : 'checkbox',
					id : 'stopCheckbox'
				},
				{ xtype:'tbtext', text:'&nbsp;&nbsp;&nbsp;&nbsp;时价:'},
				{
					xtype : 'checkbox',
					id : 'currPriceCheckbox'
				}, '->', 
				{
					xtype : 'button',
					hideLabel : true,
					iconCls : 'btn_search',
					id : 'srchBtn',
					text : '搜索',
					width : 100,
					handler : function(thiz, e) {
						menuStore.reload({
							params : {
								start : 0,
								limit : dishesPageRecordCount
							}
						});
					},
					listeners : {
						
					}
				}
			]
		}),
		bbar : new Ext.PagingToolbar({
			pageSize : dishesPageRecordCount,
			store : menuStore,
			displayInfo : true,
			displayMsg : '显示第 {0} 条到 {1} 条记录，共 {2} 条',
			emptyMsg : '没有记录'
		}),
		autoScroll : true,
		loadMask : {
			msg : '数据加载中，请稍等...'
		},
		listeners : {
			'render' : function(thiz) {
				menuStore.reload({
					params : {
						start : 0,
						limit : dishesPageRecordCount
					}
				});
			},
			'rowclick' : function(thiz, rowIndex, e) {
				currRowIndex = rowIndex;
				var tp = Ext.getCmp('materialPanelTab').getActiveTab();
				var selData = menuStore.getAt(rowIndex);
				materialPanel.setTitle('&nbsp;' + selData.get('dishName'));
//				alert(selData.get('tasteRefType'));
				if(tp.getId() == addMaterialTab.getId()){
					Ext.getCmp('addMaterialTabBtnRefresh').handler();
					loadFoodMaterial();
				}else if(tp.getId() == addTasteTab.getId()){
					var ds = Ext.getCmp('commonTasteGrid').getStore();
					var tasteRefType = selData.get('tasteRefType');
					if(tasteRefType == 1){
						Ext.getCmp('rdoTasteTypeSmart').setValue(true);
						Ext.getDom('rdoTasteTypeSmart').onclick();
					}else if(tasteRefType == 2){
						Ext.getCmp('rdoTasteTypeManual').setValue(true);
						Ext.getDom('rdoTasteTypeManual').onclick();
					}
					ds.baseParams['foodID'] =  menuStore.getAt(rowIndex).get('foodID');
					ds.load();
				}				
			}
		}
	});

	// 为store配置beforeload监听器
	menuGrid.getStore().on('beforeload', function() {
		
		var queryType = Ext.getCmp('filter').getValue();
		var searchValue = Ext.getCmp(conditionType);
		var queryOperator = 1, queryValue = '';
		if(queryType == '全部' || queryType == 0 || !searchValue || searchValue.getValue().toString().trim() == '' ){	
			queryType = 0;
			queryValue = '';
		}else{
			queryOperator = Ext.getCmp('operator').getValue();
			if (queryOperator == '等于') {
				queryOperator = 1;
			}
			queryValue = searchValue.getValue();
		}
			
		var in_isSpecial = Ext.getCmp('specialCheckbox').getValue();
		var in_isRecommend = Ext.getCmp('recommendCheckbox').getValue();
		var in_isFree = Ext.getCmp('freeCheckbox').getValue();
		var in_isStop = Ext.getCmp('stopCheckbox').getValue();
		var in_isCurrPrice = Ext.getCmp('currPriceCheckbox').getValue();

		// 输入查询条件参数
		this.baseParams = {
			'pin' : pin,
			'type' : queryType,
			'ope' : queryOperator,
			'value' : queryValue,
			'isSpecial' : in_isSpecial,
			'isRecommend' : in_isRecommend,
			'isFree' : in_isFree,
			'isStop' : in_isStop,
			'isCurrPrice' : in_isCurrPrice,
			'isPaging' : true
		};
	});

	// 为store配置load监听器(即load完后动作)
	menuGrid.getStore().on('load', function() {
		if (menuGrid.getStore().getTotalCount() != 0) {
			var msg = this.getAt(0).get('message');
			if (msg != 'normal') {
				Ext.MessageBox.show({
					msg : msg,
					width : 300,
					buttons : Ext.MessageBox.OK
				});
				this.removeAll();
			} else {
				menuGrid.getStore().each(function(record) {
					// 廚房顯示
					for ( var i = 0; i < kitchenTypeData.length; i++) {
						if (record.get('kitchenAlias') == kitchenTypeData[i][0]) {
							record.set('kitchenDisplay', kitchenTypeData[i][1]);
						}
					}
					// 菜品狀態顯示
					record.set('dishNameDisplay', record.get('dishName'));
					if (record.get('special') == true) {
						record.set('dishNameDisplay', record.get('dishNameDisplay') + '<img src="../../images/icon_tip_te.gif"></img>');
					}
					if (record.get('recommend') == true) {
						record.set('dishNameDisplay', record.get('dishNameDisplay') + '<img src="../../images/icon_tip_jian.gif"></img>');
					}
					if (record.get('stop') == true) {
						record.set('dishNameDisplay', record.get('dishNameDisplay') + '<img src="../../images/icon_tip_ting.gif"></img>');
					}
					if (record.get('free') == true) {
						record.set('dishNameDisplay', record.get('dishNameDisplay') + '<img src="../../images/forFree.png"></img>');
					}
					if (record.get('currPrice') == true) {
						record.set('dishNameDisplay', record.get('dishNameDisplay') + '<img src="../../images/currPrice.png"></img>');
					}

					// 提交，去掉修改標記
					record.commit();
				});
			}
		}
	});
	
	// ---------------------end 表格--------------------------
	var centerPanel = new Ext.Panel({
		region : 'center',
		layout : 'fit',
		frame : true,
		title : '菜品管理',
		items : [ {
				layout : 'border',
				items : [  menuGrid, materialPanel ]
		} ],
		tbar : new Ext.Toolbar({
			height : 55,
			items : [ 
			    dishAddBut, 
			    { xtype:'tbtext', text : '&nbsp;&nbsp;&nbsp;' }, 
			    '->', 
				pushBackBut, 
				{ xtype:'tbtext', text : '&nbsp;&nbsp;&nbsp;' }, 
				logOutBut 
			]
		}),
		keys : [
		    {
			    key : Ext.EventObject.ENTER,
				scope : this,
				fn : function(){	
					Ext.getCmp('srchBtn').handler(); 
				}
			}
		]
	});

	var viewport = new Ext.Viewport({
		layout : 'border',
		id : 'viewport',
		items : [
		    {
		    	region : 'north',
		    	bodyStyle : 'background-color:#A9D0F5',
				html : '<h4 style="padding:10px;font-size:150%;float:left;">无线点餐网页终端</h4><div id="optName" class="optName"></div>',
//		    	frame : true,
//		    	html : '<div style=" font-size:20pt; float:left; padding-left:10px; padding-top:3px;"><b>无线点餐网页终端</b></div><div id="optName" class="optName" style="padding-top:8px;"></div>',
				height : 50,
				margins : '0 0 0 0'
			},
			centerPanel,
			{
				region : 'south',
				height : 30,
				layout : 'form',
				frame : true,
				border : false,
				html : '<div style="font-size:11pt; text-align:center;"><b>版权所有(c) 2011 智易科技</b></div>'
			} 
		]
	});
});
