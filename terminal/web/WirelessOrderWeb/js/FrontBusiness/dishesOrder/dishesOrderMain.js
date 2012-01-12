// dish count input pop window
dishCountInputWin = new Ext.Window({
	layout : "fit",
	width : 200,
	height : 100,
	closeAction : "hide",
	resizable : false,
	items : [ {
		layout : "form",
		labelWidth : 30,
		border : false,
		frame : true,
		items : [ {
			xtype : "numberfield",
			fieldLabel : "数量",
			id : "dishCountInput",
			width : 110
		} ]
	} ],
	buttons : [
			{
				text : "确定",
				handler : function() {
					var inputCount = dishCountInputWin.findById(
							"dishCountInput").getValue();
					if (inputCount != 0 && inputCount != "") {
						dishCountInputWin.hide();
						orderedData[dishOrderCurrRowIndex_][2] = inputCount;
						orderedStore.reload();
						// 底色处理，已点菜式原色底色
						dishGridRefresh();
						orderedGrid.getSelectionModel().selectRow(
								dishOrderCurrRowIndex_);
						orderIsChanged = true;
					}

				}
			}, {
				text : "取消",
				handler : function() {
					dishCountInputWin.hide();
				}
			} ],
	listeners : {
		show : function(thiz) {
			// thiz.findById("personCountInput").focus();
			var f = Ext.get("dishCountInput");
			f.focus.defer(100, f); // 万恶的EXT！为什么这样才可以！？！？
		}
	}
});

// --------------dishes order center panel-----------------

// 已点菜式
// 2，表格的数据store
var orderedStore = new Ext.data.Store({
	proxy : new Ext.data.MemoryProxy(orderedData),
	reader : new Ext.data.ArrayReader({}, [ {
		name : "dishName"
	}, {
		name : "dishTaste"
	}, {
		name : "dishCount"
	}, {
		name : "dishPrice"
	}, {
		name : "dishOpt"
	}, {
		name : "dishTotalPrice"
	} ])
});

orderedStore.reload();
// 底色处理，已点菜式原色底色
dishGridRefresh();

// 3，栏位模型
// 移去dishesOrderLaod.js
// function dishOptTasteHandler(rowIndex) {
// if (dishOrderCurrRowIndex_ != -1) {
// dishOrderCurrRowIndex_ = rowIndex;
// dishTasteWindow.show();
// }
// };

var dishPushBackWin = new Ext.Window(
		{
			layout : "fit",
			width : 220,
			height : 120,
			closeAction : "hide",
			resizable : false,
			items : [ {
				layout : "form",
				labelWidth : 60,
				border : false,
				frame : true,
				items : [ {
					xtype : "numberfield",
					fieldLabel : "退菜数量",
					id : "dishPushBackCount",
					allowBlank : false,
					width : 110
				}, {
					xtype : "textfield",
					inputType : "password",
					fieldLabel : "密码",
					id : "dishPushBackPwd",
					width : 110
				} ]
			} ],
			buttons : [
					{
						text : "确定",
						handler : function() {
							if (dishPushBackWin.findById("dishPushBackCount")
									.isValid()) {
								var dishPushBackPwd = dishPushBackWin.findById(
										"dishPushBackPwd").getValue();
								dishPushBackWin.findById("dishPushBackPwd")
										.setValue("");

								var pwdTrans;
								if (dishPushBackPwd != "") {
									pwdTrans = MD5(dishPushBackPwd);
								} else {
									pwdTrans = dishPushBackPwd;
								}

								dishPushBackWin.hide();

								Ext.Ajax
										.request({
											url : "../../VerifyPwd.do",
											params : {
												"pin" : Request["pin"],
												"type" : "3",
												"pwd" : pwdTrans
											},
											success : function(response,
													options) {
												var resultJSON = Ext.util.JSON
														.decode(response.responseText);
												if (resultJSON.success == true) {
													var pushCount = dishPushBackWin
															.findById(
																	"dishPushBackCount")
															.getValue();
													dishPushBackWin
															.findById(
																	"dishPushBackCount")
															.setValue("");
													if (parseFloat(orderedData[dishOrderCurrRowIndex_][2])
															- parseFloat(pushCount) <= 0) {
														// 退光了
														orderedData
																.splice(
																		dishOrderCurrRowIndex_,
																		1);
													} else if (parseFloat(orderedData[dishOrderCurrRowIndex_][2])
															- parseFloat(pushCount) > 0) {
														// 退部份
														orderedData[dishOrderCurrRowIndex_][2] = parseFloat(orderedData[dishOrderCurrRowIndex_][2])
																- parseFloat(pushCount);
													}

													orderedStore.reload();
													// 底色处理，已点菜式原色底色
													dishGridRefresh();
													orderIsChanged = true;
													dishOrderCurrRowIndex_ = -1;

													Ext.MessageBox
															.show({
																msg : resultJSON.data,
																width : 300,
																buttons : Ext.MessageBox.OK
															});
												} else {
													Ext.MessageBox
															.show({
																msg : resultJSON.data,
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
					},
					{
						text : "取消",
						handler : function() {
							dishPushBackWin.hide();
							dishPushBackWin.findById("dishPushBackPwd")
									.setValue("");
						}
					} ],
			listeners : {
				show : function(thiz) {
					// thiz.findById("personCountInput").focus();
					var f = Ext.get("dishPushBackPwd");
					f.focus.defer(100, f); // 万恶的EXT！为什么这样才可以！？！？

					thiz.findById("dishPushBackCount").setValue(
							orderedData[dishOrderCurrRowIndex_][2]);
				}
			}
		});

function dishOptDeleteHandler(rowIndex) {

	if (dishOrderCurrRowIndex_ != -1) {
		// if (Request["tableStat"] == "used")
		if (orderedData[dishOrderCurrRowIndex_][16] == "1") {
			dishPushBackWin.show();
		} else {
			Ext.MessageBox.show({
				msg : "您确定要删除此菜品？",
				width : 300,
				buttons : Ext.MessageBox.YESNO,
				fn : function(btn) {
					if (btn == "yes") {
						orderedData.splice(rowIndex, 1);
						orderedStore.reload();
						// 底色处理，已点菜式原色底色
						dishGridRefresh();
						orderIsChanged = true;
						dishOrderCurrRowIndex_ = -1;
					}
				}
			});
		}
	}
};
function dishOptPressHandler(rowIndex) {

	if (dishOrderCurrRowIndex_ != -1) {
		// Ext.Msg.alert("", "已催菜！");
		// orderedStore.reload();
		// dishOrderCurrRowIndex_ = -1;
	}

};

function dishOptDispley(value, cellmeta, record, rowIndex, columnIndex, store) {
	return "<center><a id='tasteLink" + rowIndex
			+ "' href=\"javascript:dishOptTasteHandler(" + rowIndex + ")\">"
			+ "<img src='../../images/Modify.png'/>口味</a>"
			+ "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;"
			+ "<a href=\"javascript:dishOptDeleteHandler(" + rowIndex + ")\">"
			+ "<img src='../../images/del.png'/>删除</a>"
			+ "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;"
			+ "<a href=\"javascript:dishOptPressHandler(" + rowIndex + ")\">"
			+ "<img src='../../images/Modify.png'/>催菜</a>" + "</center>";
};

var orderedColumnModel = new Ext.grid.ColumnModel([ new Ext.grid.RowNumberer(),
		{
			header : "菜名",
			sortable : true,
			dataIndex : "dishName",
			width : 210
		}, {
			header : "口味",
			sortable : true,
			dataIndex : "dishTaste",
			width : 120
		}, {
			header : "数量",
			sortable : true,
			dataIndex : "dishCount",
			width : 120
		}, {
			header : "单价",
			sortable : true,
			dataIndex : "dishTotalPrice",
			width : 120
		}, {
			header : "<center>操作</center>",
			sortable : true,
			dataIndex : "dishOpt",
			width : 220,
			renderer : dishOptDispley
		} ]);

// 4，表格
var tasteChooseImgBut = new Ext.ux.ImageButton({
	imgPath : "../../images/Taste.png",
	imgWidth : 50,
	imgHeight : 50,
	tooltip : "口味",
	handler : function(btn) {
		dishOptTasteHandler(dishOrderCurrRowIndex_);
	}
});

var dishDeleteImgBut = new Ext.ux.ImageButton({
	imgPath : "../../images/DeleteDish.png",
	imgWidth : 50,
	imgHeight : 50,
	tooltip : "删除",
	handler : function(btn) {
		dishOptDeleteHandler(dishOrderCurrRowIndex_);
	}
});
var dishPressImgBut = new Ext.ux.ImageButton({
	imgPath : "../../images/HurryFood.png",
	imgWidth : 50,
	imgHeight : 50,
	tooltip : "催菜",
	handler : function(btn) {
		dishOptPressHandler(dishOrderCurrRowIndex_);
	}
});
var countAddImgBut = new Ext.ux.ImageButton(
		{
			imgPath : "../../images/AddCount.png",
			imgWidth : 50,
			imgHeight : 50,
			tooltip : "数量加1",
			handler : function(btn) {
				if (dishOrderCurrRowIndex_ != -1) {
					if (orderedData[dishOrderCurrRowIndex_][16] == "2") {
						orderedData[dishOrderCurrRowIndex_][2] = parseFloat(orderedData[dishOrderCurrRowIndex_][2]) + 1;
						orderedStore.reload();
						// 底色处理，已点菜式原色底色
						dishGridRefresh();
						orderedGrid.getSelectionModel().selectRow(
								dishOrderCurrRowIndex_);
						orderIsChanged = true;
					}
				}
			}
		});
var countMinusImgBut = new Ext.ux.ImageButton(
		{
			imgPath : "../../images/MinusCount.png",
			imgWidth : 50,
			imgHeight : 50,
			tooltip : "数量减1",
			handler : function(btn) {
				if (dishOrderCurrRowIndex_ != -1) {
					if (orderedData[dishOrderCurrRowIndex_][16] == "2") {
						if (orderedData[dishOrderCurrRowIndex_][2] != "1") {
							orderedData[dishOrderCurrRowIndex_][2] = parseFloat(orderedData[dishOrderCurrRowIndex_][2]) - 1;
							orderedStore.reload();
							// 底色处理，已点菜式原色底色
							dishGridRefresh();
							orderedGrid.getSelectionModel().selectRow(
									dishOrderCurrRowIndex_);
							orderIsChanged = true;
						}
					}
				}

			}
		});
var countEqualImgBut = new Ext.ux.ImageButton({
	imgPath : "../../images/EqualCount.png",
	imgWidth : 50,
	imgHeight : 50,
	tooltip : "数量等于",
	handler : function(btn) {
		if (dishOrderCurrRowIndex_ != -1) {
			if (orderedData[dishOrderCurrRowIndex_][16] == "2") {
				dishCountInputWin.show();
			}
		}
	}
});
var printTotalImgBut = new Ext.ux.ImageButton({
	imgPath : "../../images/PrintTotal.png",
	imgWidth : 50,
	imgHeight : 50,
	tooltip : "补打总单",
	handler : function(btn) {
		Ext.Ajax.request({
			url : "../../PrintOrder.do",
			params : {
				"pin" : Request["pin"],
				"tableID" : Request["tableNbr"],
				"printOrder" : 1
			},
			success : function(response, options) {
				var resultJSON = Ext.util.JSON.decode(response.responseText);
				Ext.MessageBox.show({
					msg : resultJSON.data,
					width : 300,
					buttons : Ext.MessageBox.OK
				});
			},
			failure : function(response, options) {
			}
		});
	}
});
var printDetailImgBut = new Ext.ux.ImageButton({
	imgPath : "../../images/PrintDetail.png",
	imgWidth : 50,
	imgHeight : 50,
	tooltip : "补打明细",
	handler : function(btn) {
		Ext.Ajax.request({
			url : "../../PrintOrder.do",
			params : {
				"pin" : Request["pin"],
				"tableID" : Request["tableNbr"],
				"printDetail" : 1
			},
			success : function(response, options) {
				var resultJSON = Ext.util.JSON.decode(response.responseText);
				Ext.MessageBox.show({
					msg : resultJSON.data,
					width : 300,
					buttons : Ext.MessageBox.OK
				});
			},
			failure : function(response, options) {
			}
		});
	}
});

var orderedGrid = new Ext.grid.GridPanel({
	title : "已点菜式",
	xtype : "grid",
	anchor : "99%",
	region : "center",
	border : false,
	ds : orderedStore,
	cm : orderedColumnModel,
	viewConfig : {
		forceFit : true
	},
	sm : new Ext.grid.RowSelectionModel({
		singleSelect : true
	}),
	tbar : new Ext.Toolbar({
		height : 55,
		items : [ {
			text : " ",
			disabled : true
		}, tasteChooseImgBut, {
			text : "&nbsp;&nbsp;&nbsp;",
			disabled : true
		}, dishDeleteImgBut, {
			text : "&nbsp;&nbsp;&nbsp;",
			disabled : true
		}, dishPressImgBut, dishDeleteImgBut, {
			text : "&nbsp;&nbsp;&nbsp;",
			disabled : true
		}, '-', {
			text : "&nbsp;&nbsp;&nbsp;",
			disabled : true
		}, countAddImgBut, {
			text : "&nbsp;&nbsp;&nbsp;",
			disabled : true
		}, countMinusImgBut, {
			text : "&nbsp;&nbsp;&nbsp;",
			disabled : true
		}, countEqualImgBut, {
			text : "&nbsp;&nbsp;&nbsp;",
			disabled : true
		}
		// , '-', {
		// text : "&nbsp;&nbsp;&nbsp;",
		// disabled : true
		// }, printTotalImgBut, {
		// text : "&nbsp;&nbsp;&nbsp;",
		// disabled : true
		// }, printDetailImgBut
		]
	}),
	listeners : {
		rowclick : function(thiz, rowIndex, e) {
			dishOrderCurrRowIndex_ = rowIndex;
		},
		render : function(thiz) {
			orderedDishesOnLoad();
			tableStuLoad();
		}
	}
});

var orderedForm = new Ext.form.FormPanel(
		{
			frame : true,
			border : false,
			region : "south",
			height : 60,
			items : [ {} ],
			buttons : [
					{
						// tableID="100"&customNum="3"&foods="{[1100,2,1,0]}"
						// 各字段表示的意义：
						// tableID：餐台号
						// customNum：就餐人数
						// foods：菜品列表，格式为{[菜品1编号,菜品1数量,口味1编号,厨房1编号,菜品1折扣,2nd口味1编号,3rd口味1编号]}
						// 以点菜式格式：[菜名，口味，数量，￥单价，操作，￥实价，菜名编号，厨房编号，口味编号1,特,荐,停,送,￥口味价钱,口味编号2,口味编号3]
						text : "提交",
						handler : function() {
							if (orderedData.length > 0
									&& dishesOrderNorthPanel.findById(
											"tablePersonCount").isValid()) {

								var Request = new URLParaQuery();

								var foodPara = "";
								for ( var i = 0; i < orderedData.length; i++) {
									if (orderedData[i][18] == "false") {
										// [是否临时菜(false),菜品1编号,菜品1数量,口味1编号,厨房1编号,菜品1折扣,2nd口味1编号,3rd口味1编号]，
										foodPara = foodPara + "[false,"// 是否临时菜(false)
												+ orderedData[i][6] + "," // 菜品1编号
												+ orderedData[i][2] + "," // 菜品1数量
												+ orderedData[i][8] + "," // 口味1编号
												+ orderedData[i][7] + ","// 厨房1编号
												+ "0,"// 菜品1折扣
												+ orderedData[i][14] + ","// 2nd口味1编号
												+ orderedData[i][15] // 3rd口味1编号
												+ "]，";
									} else {
										// [是否临时菜(true),临时菜1编号,临时菜1名称,临时菜1数量,临时菜1单价]，
										var price = orderedData[i][3].substr(1,
												orderedData[i][3].length - 1);
										foodPara = foodPara + "[true,"// 是否临时菜(true)
												+ orderedData[i][6] + "," // 临时菜1编号
												+ orderedData[i][19] + "," // 临时菜1名称
												+ orderedData[i][2] + "," // 临时菜1数量
												+ price + "" // 临时菜1单价(原料單價)
												+ "]，";
									}

								}
								foodPara = "{"
										+ foodPara.substr(0,
												foodPara.length - 1) + "}";
								

								var type = 9;
								if (Request["tableStat"] == "free") {
									type = 1;
								} else {
									type = 2;
								}

								var inputPersCount = dishesOrderNorthPanel
										.findById("tablePersonCount")
										.getValue();

								// alert("pin:" + Request["pin"] + " tableID:"
								// + Request["tableNbr"]
								// + " tableID_2:"
								// + Request["tableNbr2"]
								// + " customNum:"
								// + Request["personCount"] + " type:"
								// + type + " originalTableID:"
								// + Request["tableNbr"]
								// + " category:" + category
								// + " foods" + foodPara);
								orderedForm.buttons[0].setDisabled(true);
								Ext.Ajax
										.request({
											url : "../../InsertOrder.do",
											params : {
												"pin" : Request["pin"],
												"tableID" : Request["tableNbr"],
												"tableID_2" : Request["tableNbr2"],
												"customNum" : inputPersCount,// get
												// input
												// count
												"type" : type,
												"originalTableID" : Request["tableNbr"],// no
												// use
												"foods" : foodPara,
												"category" : category
											},
											success : function(response,
													options) {
												var resultJSON = Ext.util.JSON
														.decode(response.responseText);
												if (resultJSON.success == true) {
													Ext.MessageBox
															.show({
																msg : resultJSON.data,
																width : 300,
																buttons : Ext.MessageBox.OK,
																fn : function() {
																	var Request = new URLParaQuery();
																	location.href = "TableSelect.html?pin="
																			+ Request["pin"]
																			+ "&restaurantID="
																			+ restaurantID;
																}
															});
												} else {
													orderedForm.buttons[0].setDisabled(false);
													Ext.MessageBox
															.show({
																msg : resultJSON.data,
																width : 300,
																buttons : Ext.MessageBox.OK
															});
												}
											},
											failure : function(response,
													options) {
												orderedForm.buttons[0].setDisabled(false);
												Ext.MessageBox
														.show({
															msg : "Unknow page error",
															width : 300,
															buttons : Ext.MessageBox.OK
														});
											}
										});
							} else if (orderedData.length == 0) {
								Ext.MessageBox.show({
									msg : "还没有选择任何菜品，暂时不能提交",
									width : 300,
									buttons : Ext.MessageBox.OK
								});
							}
						}
					}
					// , {
					// text : "清空",
					// handler : function() {
					// Ext.Msg.show({
					// title : "提示",
					// msg : "确定要删除所有已点菜式？",
					// buttons : Ext.Msg.YESNO,
					// fn : function(btn) {
					// if (btn == "yes") {
					// orderedData.length = 0;
					// orderedStore.reload();
					// }
					// ;
					// },
					// icon : Ext.MessageBox.QUESTION
					// });
					// }
					// }
					,
					{
						text : "返回",
						handler : function() {
							var Request = new URLParaQuery();
							if (orderIsChanged == false) {
								location.href = "TableSelect.html?pin="
										+ Request["pin"] + "&restaurantID="
										+ restaurantID;
							} else {
								Ext.MessageBox
										.show({
											msg : "下/改单还未提交，是否确认退出？",
											width : 300,
											buttons : Ext.MessageBox.YESNO,
											fn : function(btn) {
												if (btn == "yes") {
													location.href = "TableSelect.html?pin="
															+ Request["pin"]
															+ "&restaurantID="
															+ restaurantID;
												}
											}
										});
							}
						}
					} ]
		});

var dishesOrderCenterPanel = new Ext.Panel({
	region : "center",
	id : "dishesOrderCenterPanel",
	// title : "<div style='font-size:18px;padding-left:2px'>新下单<div>",
	layout : "border",
	items : [ orderedForm, orderedGrid ]
});

// --------------dishes taste pop window-----------------
// 口味

// 2，表格的数据store
// 前台：[口味编号,口味分类,口味名称,价钱,比例,计算方式]
var dishTasteStoreTas = new Ext.data.Store({
	proxy : new Ext.data.MemoryProxy(dishTasteDataTas),
	reader : new Ext.data.ArrayReader({}, [ {
		name : "tasteNumber"
	}, {
		name : "tasteType"
	}, {
		name : "dishTaste"
	}, {
		name : "tastePrice"
	}, {
		name : "tasteRate"
	}, {
		name : "tasteCountType"
	}, {
		name : "CountTypeDescr"
	}, {
		name : "tasteChoose"
	} ])
});

dishTasteStoreTas.reload();

// 3，栏位模型
var checkColumnTas = new Ext.grid.CheckColumn({
	header : " ",
	dataIndex : "tasteChoose",
	width : 100
});

var dishTasteColumnModelTas = new Ext.grid.ColumnModel([
		new Ext.grid.RowNumberer(), {
			header : "口味",
			sortable : true,
			dataIndex : "dishTaste",
			width : 100
		}, {
			header : "价钱",
			sortable : true,
			dataIndex : "tastePrice",
			width : 100
		}, {
			header : "比例",
			sortable : true,
			dataIndex : "tasteRate",
			width : 100
		}, {
			header : "计算方式",
			sortable : true,
			dataIndex : "CountTypeDescr",
			width : 100
		}, checkColumnTas ]);

// 4，表格
var dishTasteGridTas = new Ext.grid.EditorGridPanel({
	title : '口味',
	anchor : "99%",
	ds : dishTasteStoreTas,
	cm : dishTasteColumnModelTas,
	plugins : checkColumnTas,
	clicksToEdit : 1,
	sm : new Ext.grid.RowSelectionModel({
		singleSelect : true
	}),
	listeners : {
		"afteredit" : function(e) {
			var choTasteN = e.record.get("tasteNumber");
			var choTasteDescr = e.record.get("dishTaste");
			if (e.record.get("tasteChoose")) {

				// 校验是否超数
				tasteChoosenCount = tasteChoosenCount + 1;
				if (tasteChoosenCount > 3) {
					Ext.MessageBox.show({
						msg : "暂不允许选择超过３种口味",
						width : 300,
						buttons : Ext.MessageBox.OK
					});

					dishTasteGridTas.getStore().each(function(record) {
						if (record.get("tasteNumber") == choTasteN) {
							record.set("tasteChoose", false);
						}
					});
					tasteChoosenCount = tasteChoosenCount - 1;
				} else {
					// 记录选择的口味
					choosenTasteDisplay.push([ choTasteN, choTasteDescr ]);
				}

			} else {
				var thisIndex = -1;
				for ( var i = 0; i < choosenTasteDisplay.length - 1; i++) {
					if (choosenTasteDisplay[i][0] == choTasteN) {
						thisIndex = i;
					}
				}
				choosenTasteDisplay.splice(thisIndex, 1);
				tasteChoosenCount = tasteChoosenCount - 1;
			}

			choosenTasteRefresh();
		}
	}
});

// /////////////////////////////////

// 2，表格的数据store
// 前台：[口味编号,口味分类,口味名称,价钱,比例,计算方式]
var dishTasteStorePar = new Ext.data.Store({
	proxy : new Ext.data.MemoryProxy(dishTasteDataPar),
	reader : new Ext.data.ArrayReader({}, [ {
		name : "tasteNumber"
	}, {
		name : "tasteType"
	}, {
		name : "dishTaste"
	}, {
		name : "tastePrice"
	}, {
		name : "tasteRate"
	}, {
		name : "tasteCountType"
	}, {
		name : "CountTypeDescr"
	}, {
		name : "tasteChoose"
	} ])
});

dishTasteStorePar.reload();

// 3，栏位模型
var checkColumnPar = new Ext.grid.CheckColumn({
	header : " ",
	dataIndex : "tasteChoose",
	width : 100
});

var dishTasteColumnModelPar = new Ext.grid.ColumnModel([
		new Ext.grid.RowNumberer(), {
			header : "口味",
			sortable : true,
			dataIndex : "dishTaste",
			width : 100
		}, {
			header : "价钱",
			sortable : true,
			dataIndex : "tastePrice",
			width : 100
		}, {
			header : "比例",
			sortable : true,
			dataIndex : "tasteRate",
			width : 100
		}, {
			header : "计算方式",
			sortable : true,
			dataIndex : "CountTypeDescr",
			width : 100
		}, checkColumnPar ]);

// 4，表格
var dishTasteGridPar = new Ext.grid.EditorGridPanel({
	title : '做法',
	anchor : "99%",
	ds : dishTasteStorePar,
	cm : dishTasteColumnModelPar,
	plugins : checkColumnPar,
	clicksToEdit : 1,
	sm : new Ext.grid.RowSelectionModel({
		singleSelect : true
	}),
	listeners : {
		"afteredit" : function(e) {
			var choTasteN = e.record.get("tasteNumber");
			var choTasteDescr = e.record.get("dishTaste");
			if (e.record.get("tasteChoose")) {

				// 校验是否超数
				tasteChoosenCount = tasteChoosenCount + 1;
				if (tasteChoosenCount > 3) {
					Ext.MessageBox.show({
						msg : "暂不允许选择超过３种口味",
						width : 300,
						buttons : Ext.MessageBox.OK
					});

					dishTasteGridPar.getStore().each(function(record) {
						if (record.get("tasteNumber") == choTasteN) {
							record.set("tasteChoose", false);
						}
					});
					tasteChoosenCount = tasteChoosenCount - 1;
				} else {
					// 记录选择的口味
					choosenTasteDisplay.push([ choTasteN, choTasteDescr ]);
				}

			} else {
				var thisIndex = -1;
				for ( var i = 0; i < choosenTasteDisplay.length - 1; i++) {
					if (choosenTasteDisplay[i][0] == choTasteN) {
						thisIndex = i;
					}
				}
				choosenTasteDisplay.splice(thisIndex, 1);
				tasteChoosenCount = tasteChoosenCount - 1;
			}

			choosenTasteRefresh();
		}
	}
});

// /////////////////////////////////

// 2，表格的数据store
// 前台：[口味编号,口味分类,口味名称,价钱,比例,计算方式]
var dishTasteStoreSiz = new Ext.data.Store({
	proxy : new Ext.data.MemoryProxy(dishTasteDataSiz),
	reader : new Ext.data.ArrayReader({}, [ {
		name : "tasteNumber"
	}, {
		name : "tasteType"
	}, {
		name : "dishTaste"
	}, {
		name : "tastePrice"
	}, {
		name : "tasteRate"
	}, {
		name : "tasteCountType"
	}, {
		name : "CountTypeDescr"
	}, {
		name : "tasteChoose"
	} ])
});

dishTasteStoreSiz.reload();

// 3，栏位模型
var checkColumnSiz = new Ext.grid.CheckColumn({
	header : " ",
	dataIndex : "tasteChoose",
	width : 100
});

var dishTasteColumnModelSiz = new Ext.grid.ColumnModel([
		new Ext.grid.RowNumberer(), {
			header : "口味",
			sortable : true,
			dataIndex : "dishTaste",
			width : 100
		}, {
			header : "价钱",
			sortable : true,
			dataIndex : "tastePrice",
			width : 100
		}, {
			header : "比例",
			sortable : true,
			dataIndex : "tasteRate",
			width : 100
		}, {
			header : "计算方式",
			sortable : true,
			dataIndex : "CountTypeDescr",
			width : 100
		}, checkColumnSiz ]);

// 4，表格
var dishTasteGridSiz = new Ext.grid.EditorGridPanel({
	title : '规格',
	anchor : "99%",
	ds : dishTasteStoreSiz,
	cm : dishTasteColumnModelSiz,
	plugins : checkColumnSiz,
	clicksToEdit : 1,
	sm : new Ext.grid.RowSelectionModel({
		singleSelect : true
	}),
	listeners : {
		"afteredit" : function(e) {
			var choTasteN = e.record.get("tasteNumber");
			var choTasteDescr = e.record.get("dishTaste");
			if (e.record.get("tasteChoose")) {

				// 校验是否超数
				tasteChoosenCount = tasteChoosenCount + 1;
				if (tasteChoosenCount > 3) {
					Ext.MessageBox.show({
						msg : "暂不允许选择超过３种口味",
						width : 300,
						buttons : Ext.MessageBox.OK
					});

					dishTasteGridSiz.getStore().each(function(record) {
						if (record.get("tasteNumber") == choTasteN) {
							record.set("tasteChoose", false);
						}
					});
					tasteChoosenCount = tasteChoosenCount - 1;
				} else {
					// 记录选择的口味
					choosenTasteDisplay.push([ choTasteN, choTasteDescr ]);
				}

			} else {
				var thisIndex = -1;
				for ( var i = 0; i < choosenTasteDisplay.length - 1; i++) {
					if (choosenTasteDisplay[i][0] == choTasteN) {
						thisIndex = i;
					}
				}
				choosenTasteDisplay.splice(thisIndex, 1);
				tasteChoosenCount = tasteChoosenCount - 1;
			}

			choosenTasteRefresh();
		}
	}
});

// tab面版
var dishTasteTabPanel = new Ext.TabPanel({
	// layout : "fit",
	region : "center",
	tabPosition : "top",
	width : 300,
	height : 280,
	activeTab : 0,
	items : [ dishTasteGridTas, dishTasteGridPar, dishTasteGridSiz ]
});

var dishTasteWindow = new Ext.Window(
		{
			layout : "border",
			width : 550,
			height : 300,
			closeAction : "hide",
			// plain: true,
			items : [ dishTasteTabPanel, {
				region : "south",
				height : 20,
				bodyStyle : "background-color:#A9D0F5",
				contentEl : "choosenTaste"
			} ],
			buttons : [
					{
						text : "清空",
						handler : function() {
							dishTasteGridTas.getStore().each(function(record) {
								record.set("tasteChoose", false);
							});
							dishTasteGridPar.getStore().each(function(record) {
								record.set("tasteChoose", false);
							});
							dishTasteGridSiz.getStore().each(function(record) {
								record.set("tasteChoose", false);
							});
							choosenTasteDisplay.length = 0;
							choosenTasteRefresh();
							tasteChoosenCount = 0;
						}
					},
					{
						text : "确定",
						handler : function() {
							// dishTasteWindow.hide();
							// 格式：[{編號,描述,價錢或比例,計算方式}]
							choosenTaset.length = 0;
							orderedData[dishOrderCurrRowIndex_][8] = "0";
							orderedData[dishOrderCurrRowIndex_][14] = "0";
							orderedData[dishOrderCurrRowIndex_][15] = "0";

							dishTasteGridTas
									.getStore()
									.each(
											function(record) {
												if (record.get("tasteChoose")) {
													if (record
															.get("tasteCountType") == "0") {
														choosenTaset
																.push([
																		record
																				.get("tasteNumber"),// 編號
																		record
																				.get("dishTaste"),// 描述
																		record
																				.get("tastePrice"),// 價錢或比例
																		record
																				.get("tasteCountType") // 計算方式
																]);
													} else {
														choosenTaset
																.push([
																		record
																				.get("tasteNumber"),// 編號
																		record
																				.get("dishTaste"),// 描述
																		record
																				.get("tasteRate"),// 價錢或比例
																		record
																				.get("tasteCountType") // 計算方式
																]);
													}
												}
											});
							dishTasteGridPar
									.getStore()
									.each(
											function(record) {
												if (record.get("tasteChoose")) {
													if (record
															.get("tasteCountType") == "0") {
														choosenTaset
																.push([
																		record
																				.get("tasteNumber"),// 編號
																		record
																				.get("dishTaste"),// 描述
																		record
																				.get("tastePrice"),// 價錢或比例
																		record
																				.get("tasteCountType") // 計算方式
																]);
													} else {
														choosenTaset
																.push([
																		record
																				.get("tasteNumber"),// 編號
																		record
																				.get("dishTaste"),// 描述
																		record
																				.get("tasteRate"),// 價錢或比例
																		record
																				.get("tasteCountType") // 計算方式
																]);
													}
												}
											});
							dishTasteGridSiz
									.getStore()
									.each(
											function(record) {
												if (record.get("tasteChoose")) {
													if (record
															.get("tasteCountType") == "0") {
														choosenTaset
																.push([
																		record
																				.get("tasteNumber"),// 編號
																		record
																				.get("dishTaste"),// 描述
																		record
																				.get("tastePrice"),// 價錢或比例
																		record
																				.get("tasteCountType") // 計算方式
																]);
													} else {
														choosenTaset
																.push([
																		record
																				.get("tasteNumber"),// 編號
																		record
																				.get("dishTaste"),// 描述
																		record
																				.get("tasteRate"),// 價錢或比例
																		record
																				.get("tasteCountType") // 計算方式
																]);
													}
												}
											});

							if (tasteChoosenCount == 0) {
								// 格式：[菜名，口味，数量，￥单价，操作，￥实价，菜名编号，厨房编号，口味编号1,特,荐,停,送,￥口味价钱,口味编号2,口味编号3,菜品状态]
								// 未有選擇口味
								orderedData[dishOrderCurrRowIndex_][1] = "无口味";
								orderedData[dishOrderCurrRowIndex_][5] = orderedData[dishOrderCurrRowIndex_][3];

								// refresh
								orderedStore.reload();
								// 底色处理，已点菜式原色底色
								dishGridRefresh();
								orderIsChanged = true;

								// hide the window
								dishTasteWindow.hide();
							} else {
								// 校驗通過
								// 格式：[菜名，口味，数量，￥单价，操作，￥实价，菜名编号，厨房编号，口味编号1,特,荐,停,送,￥口味价钱,口味编号2,口味编号3,菜品状态]
								// 更新單價,// 格式：[{編號,描述,價錢或比例,計算方式}]
								var origPrice = orderedData[dishOrderCurrRowIndex_][3]
										.substring(1);
								var currPrice = parseFloat(origPrice);
								for ( var i = 0; i < choosenTaset.length; i++) {
									if (choosenTaset[i][3] == 1)
										currPrice = currPrice
												* (1 + parseFloat(choosenTaset[i][2]));
								}
								for ( var i = 0; i < choosenTaset.length; i++) {
									if (choosenTaset[i][3] == 0)
										currPrice = currPrice
												+ parseFloat(choosenTaset[i][2]);
								}
								// update the single price
								orderedData[dishOrderCurrRowIndex_][5] = "￥"
										+ currPrice;

								// mark the choosen taset
								// 第一口味
								if (choosenTaset[0] != undefined) {
									orderedData[dishOrderCurrRowIndex_][8] = choosenTaset[0][0];
									orderedData[dishOrderCurrRowIndex_][1] = choosenTaset[0][1];
									orderedData[dishOrderCurrRowIndex_][1] = orderedData[dishOrderCurrRowIndex_][1]
											+ "；";
								}
								// 第二口味
								if (choosenTaset[1] != undefined) {
									orderedData[dishOrderCurrRowIndex_][14] = choosenTaset[1][0];
									orderedData[dishOrderCurrRowIndex_][1] = orderedData[dishOrderCurrRowIndex_][1]
											+ choosenTaset[1][1];
									orderedData[dishOrderCurrRowIndex_][1] = orderedData[dishOrderCurrRowIndex_][1]
											+ "；";
								}
								// 第三口味
								if (choosenTaset[2] != undefined) {
									orderedData[dishOrderCurrRowIndex_][15] = choosenTaset[2][0];
									orderedData[dishOrderCurrRowIndex_][1] = orderedData[dishOrderCurrRowIndex_][1]
											+ choosenTaset[2][1];
									orderedData[dishOrderCurrRowIndex_][1] = orderedData[dishOrderCurrRowIndex_][1]
											+ "；";
								}
								orderedData[dishOrderCurrRowIndex_][1] = orderedData[dishOrderCurrRowIndex_][1]
										.substring(
												0,
												orderedData[dishOrderCurrRowIndex_][1].length - 1);

								// refresh
								orderedStore.reload();
								// 底色处理，已点菜式原色底色
								dishGridRefresh();
								orderIsChanged = true;

								// hide the window
								dishTasteWindow.hide();
							}

						}
					}, {
						text : "取消",
						handler : function() {
							dishTasteWindow.hide();
						}
					} ],
			listeners : {
				"show" : function(thiz) {

					// show的時候從一點菜式數組中取出當前菜品的口味情況
					var tasteNbr1 = orderedData[dishOrderCurrRowIndex_][8];
					var tasteNbr2 = orderedData[dishOrderCurrRowIndex_][14];
					var tasteNbr3 = orderedData[dishOrderCurrRowIndex_][15];

					// alert("tasteNbr1:"+tasteNbr1+" tasteNbr2:"+tasteNbr2+"
					// tasteNbr3:"+tasteNbr3);

					// 清空选择
					dishTasteGridTas.getStore().each(function(record) {
						record.set("tasteChoose", false);
					});
					dishTasteGridPar.getStore().each(function(record) {
						record.set("tasteChoose", false);
					});
					dishTasteGridSiz.getStore().each(function(record) {
						record.set("tasteChoose", false);
					});

					choosenTasteDisplay.length = 0;
					tasteChoosenCount = 0;

					if (tasteNbr1 == 0 && tasteNbr2 == 0 && tasteNbr3 == 0) {

						choosenTasteRefresh();

					} else {
						dishTasteGridTas
								.getStore()
								.each(
										function(record) {
											if (record.get("tasteNumber") == tasteNbr1
													|| record
															.get("tasteNumber") == tasteNbr2
													|| record
															.get("tasteNumber") == tasteNbr3) {
												record.set("tasteChoose", true);
												choosenTasteDisplay
														.push([
																record
																		.get("tasteNumber"),
																record
																		.get("dishTaste") ]);
												tasteChoosenCount = tasteChoosenCount + 1;
											}
										});
						dishTasteGridPar
								.getStore()
								.each(
										function(record) {
											if (record.get("tasteNumber") == tasteNbr1
													|| record
															.get("tasteNumber") == tasteNbr2
													|| record
															.get("tasteNumber") == tasteNbr3) {
												record.set("tasteChoose", true);
												choosenTasteDisplay
														.push([
																record
																		.get("tasteNumber"),
																record
																		.get("dishTaste") ])
												tasteChoosenCount = tasteChoosenCount + 1;
											}
										});
						dishTasteGridSiz
								.getStore()
								.each(
										function(record) {
											if (record.get("tasteNumber") == tasteNbr1
													|| record
															.get("tasteNumber") == tasteNbr2
													|| record
															.get("tasteNumber") == tasteNbr3) {
												record.set("tasteChoose", true);
												choosenTasteDisplay
														.push([
																record
																		.get("tasteNumber"),
																record
																		.get("dishTaste") ])
												tasteChoosenCount = tasteChoosenCount + 1;
											}
										});

						choosenTasteRefresh();
					}
				}
			}
		});

// --------------dishes order east panel-----------------
// soft key board
var softKBKeyHandler = function(relateItemId, number) {

	var currValue = dishesOrderEastPanel.findById(relateItemId).getValue();
	dishesOrderEastPanel.findById(relateItemId).setValue(
			currValue + ("" + number));

	dishesOrderEastPanel.findById(relateItemId).fireEvent("blur",
			dishesOrderEastPanel.findById(relateItemId));
};

softKeyBoardDO = new Ext.Window({
	layout : "fit",
	width : 117,
	height : 142,
	resizable : false,
	closeAction : "hide",
	// x : 41,
	// y : 146,
	items : [ {
		layout : "form",
		labelSeparator : '：',
		labelWidth : 40,
		frame : true,
		buttonAlign : "left",
		items : [ {
			layout : "column",
			border : false,
			items : [
					{
						layout : "form",
						width : 30,
						border : false,
						items : [ {
							text : "1",
							xtype : "button",
							handler : function() {
								softKBKeyHandler(softKBRelateItemId, "1");
								dishKeyboardSelect(softKBRelateItemId);
							}
						} ]
					},
					{
						layout : "form",
						width : 30,
						border : false,
						items : [ {
							text : "2",
							xtype : "button",
							handler : function() {
								softKBKeyHandler(softKBRelateItemId, "2");
								dishKeyboardSelect(softKBRelateItemId);
							}
						} ]
					},
					{
						layout : "form",
						width : 30,
						border : false,
						items : [ {
							text : "3",
							xtype : "button",
							handler : function() {
								softKBKeyHandler(softKBRelateItemId, "3");
								dishKeyboardSelect(softKBRelateItemId);
							}
						} ]
					},
					{
						layout : "form",
						width : 30,
						border : false,
						items : [ {
							text : "4",
							xtype : "button",
							handler : function() {
								softKBKeyHandler(softKBRelateItemId, "4");
								dishKeyboardSelect(softKBRelateItemId);
							}
						} ]
					},
					{
						layout : "form",
						width : 30,
						border : false,
						items : [ {
							text : "5",
							xtype : "button",
							handler : function() {
								softKBKeyHandler(softKBRelateItemId, "5");
								dishKeyboardSelect(softKBRelateItemId);
							}
						} ]
					},
					{
						layout : "form",
						width : 30,
						border : false,
						items : [ {
							text : "6",
							xtype : "button",
							handler : function() {
								softKBKeyHandler(softKBRelateItemId, "6");
								dishKeyboardSelect(softKBRelateItemId);
							}
						} ]
					},
					{
						layout : "form",
						width : 30,
						border : false,
						items : [ {
							text : "7",
							xtype : "button",
							handler : function() {
								softKBKeyHandler(softKBRelateItemId, "7");
								dishKeyboardSelect(softKBRelateItemId);
							}
						} ]
					},
					{
						layout : "form",
						width : 30,
						border : false,
						items : [ {
							text : "8",
							xtype : "button",
							handler : function() {
								softKBKeyHandler(softKBRelateItemId, "8");
								dishKeyboardSelect(softKBRelateItemId);
							}
						} ]
					},
					{
						layout : "form",
						width : 30,
						border : false,
						items : [ {
							text : "9",
							xtype : "button",
							handler : function() {
								softKBKeyHandler(softKBRelateItemId, "9");
								dishKeyboardSelect(softKBRelateItemId);
							}
						} ]
					},
					{
						layout : "form",
						width : 30,
						border : false,
						items : [ {
							text : "&nbsp;.",
							xtype : "button",
							handler : function() {
								softKBKeyHandler(softKBRelateItemId, ".");
								dishKeyboardSelect(softKBRelateItemId);
							}
						} ]
					},
					{
						layout : "form",
						width : 60,
						border : false,
						items : [ {
							text : "&nbsp;删 除&nbsp;",
							xtype : "button",
							handler : function() {
								var origValue = dishesOrderEastPanel.findById(
										softKBRelateItemId).getValue()
										+ "";
								var newValue = origValue.substring(0,
										origValue.length - 1);
								dishesOrderEastPanel.findById(
										softKBRelateItemId).setValue(newValue);
								dishKeyboardSelect(softKBRelateItemId);
								dishesOrderEastPanel.findById(
										softKBRelateItemId).fireEvent(
										"blur",
										dishesOrderEastPanel
												.findById(softKBRelateItemId));
							}

						} ]
					},
					{
						layout : "form",
						width : 30,
						border : false,
						items : [ {
							text : "0",
							xtype : "button",
							handler : function() {
								softKBKeyHandler(softKBRelateItemId, "0");
								dishKeyboardSelect(softKBRelateItemId);
							}
						} ]
					},
					{
						layout : "form",
						width : 60,
						border : false,
						items : [ {
							text : "&nbsp;清 空&nbsp;",
							xtype : "button",
							handler : function() {
								dishesOrderEastPanel.findById(
										softKBRelateItemId).setValue("");
								dishKeyboardSelect(softKBRelateItemId);
								dishesOrderEastPanel.findById(
										softKBRelateItemId).fireEvent(
										"blur",
										dishesOrderEastPanel
												.findById(softKBRelateItemId));
							}

						} ]
					} ]
		} ]
	} ],
	listeners : {
		show : function(thiz) {
			var f = Ext.get(softKBRelateItemId);
			f.focus.defer(100, f); // 万恶的EXT！为什么这样才可以！？！？
		}
	// ,
	// beforehide : function(thiz) {
	// if ((softKBRelateItemId == "orderCountSpell" && (Ext.getCmp(
	// "orderCountSpell").getValue() == 0 || Ext.getCmp(
	// "orderCountSpell").getValue() == ""))) {
	// return false;
	// } else {
	// return true;
	// }
	// }
	}
});

// ------------------------------------- 菜谱 -----------------------------------
// 2，表格的数据store
var dishesDisplayStore = new Ext.data.Store({
	proxy : new Ext.data.MemoryProxy(dishesDisplayDataShow),
	reader : new Ext.data.ArrayReader({}, [ {
		name : "dish"
	}, {
		name : "dishIndex"
	}, {
		name : "dishSpell"
	}, {
		name : "dishPrice"
	} ])
});

// 3，栏位模型
var dishesDisplayColumnModel = new Ext.grid.ColumnModel([
		new Ext.grid.RowNumberer(), {
			header : "菜名",
			sortable : true,
			dataIndex : "dish",
			width : 130
		}, {
			header : "菜名编号",
			sortable : true,
			dataIndex : "dishIndex",
			width : 80
		}, {
			header : "菜名拼音",
			sortable : true,
			dataIndex : "dishSpell",
			width : 80
		}, {
			header : "单价",
			sortable : true,
			dataIndex : "dishPrice",
			width : 80
		} ]);

// 4，表格
var dishesDisplayGrid = new Ext.grid.GridPanel(
		{
			xtype : "grid",
			// height : 400,
			anchor : "98%",
			autoScroll : true,
			region : "center",
			ds : dishesDisplayStore,
			cm : dishesDisplayColumnModel,
			viewConfig : {
				forceFit : true
			},
			sm : new Ext.grid.RowSelectionModel({
				singleSelect : true
			}),
			listeners : {
				rowdblclick : function(thiz, rowIndex, e) {

					if (dishesDisplayDataShow[rowIndex][7] == "false") {
						// mune格式：[菜名，菜名编号，菜名拼音，单价，厨房编号,特,荐,停,送,菜品状态]
						// ordered格式：[菜名，口味，数量，￥单价，操作，￥实价，菜名编号，厨房编号，口味编号1,特,荐,停,送,￥口味价钱,口味编号2,口味编号3]
						// var dishCurrCount = dishesOrderEastPanel.findById(
						// "orderCountNum").getValue();
						var dishCurrName = dishesDisplayDataShow[rowIndex][0];
						var dishCurrPrice = dishesDisplayDataShow[rowIndex][3];
						var dishNbr = dishesDisplayDataShow[rowIndex][1];
						var kitchenNbr = dishesDisplayDataShow[rowIndex][4];

						var isAlreadyOrderd = false;

						for ( var i = 0; i < orderedData.length; i++) {
							if (orderedData[i][6] == dishNbr
									&& orderedData[i][16] == "2") {
								orderedData[i][2] = parseFloat(orderedData[i][2]) + 1;
								isAlreadyOrderd = true;
							}
						}

						if (isAlreadyOrderd == false) {
							orderedData.push([ dishCurrName, "无口味", 1,
											dishCurrPrice, "", dishCurrPrice,
											dishNbr, kitchenNbr, 0,
											dishesDisplayDataShow[rowIndex][5],
											dishesDisplayDataShow[rowIndex][6],
											dishesDisplayDataShow[rowIndex][7],
											dishesDisplayDataShow[rowIndex][8],
											"￥0", 0, 0, "2",
											dishesDisplayDataShow[rowIndex][9],
											"false" ]);
						}
						orderedStore.reload();
						// 底色处理，已点菜式原色底色
						dishGridRefresh();
						orderIsChanged = true;
						dishOrderCurrRowIndex_ = -1;
					} else {
						Ext.MessageBox.show({
							msg : "该菜品已售完！",
							width : 300,
							buttons : Ext.MessageBox.OK
						});
					}
				},
				render : function(thiz) {
					orderedMenuOnLoad();
					tasteOnLoad();
				}
			}
		});

dishesDisplayStore.reload();

var dishesChooseBySpellForm = new Ext.form.FormPanel({
	title : "菜名拼音选菜",
	id : "dishesChooseBySpellForm",
	border : false,
	frame : true,
	items : [ {
		layout : "column",
		border : false,
		anchor : "98%",
		items : [ {
			layout : "form",
			labelWidth : 60,
			border : false,
			labelSeparator : '：',
			columnWidth : .50,
			items : [ {
				xtype : "textfield",
				fieldLabel : "菜名拼音",
				name : "orderSpell",
				id : "orderSpell",
				anchor : "90%",
				listeners : {
					focus : function(thiz) {
						softKeyBoardDO.hide();
					},
					render : function(thiz) {
						dishSpellOnLoad();
					}
				}
			} ]
		} ]
	} ]
});

var dishesChooseByNumForm = new Ext.form.FormPanel({
	title : "菜名编号选菜",
	id : "dishesChooseByNumForm",
	border : false,
	frame : true,
	items : [ {
		layout : "column",
		border : false,
		anchor : "98%",
		items : [

				{
					layout : "form",
					labelWidth : 60,
					border : false,
					labelSeparator : '：',
					columnWidth : .50,
					items : [ {
						xtype : "numberfield",
						fieldLabel : "菜名编号",
						name : "orderNbr",
						id : "orderNbr",
						anchor : "90%",
						listeners : {
							focus : function(thiz) {
								// softKeyBoardDO
								// .setPosition(dishesOrderCenterPanel
								// .getInnerWidth() + 77, 187);
								// softKBRelateItemId = "orderNbr";
								// softKeyBoardDO.show();

							},
							render : function(thiz) {
								dishNbrOnLoad();
							}
						}
					} ]
				},
				{
					layout : "form",
					border : false,
					columnWidth : .50,
					items : [ {
						xtype : "button",
						text : "小键盘",
						name : "softKeyBoardNbrBtn",
						id : "softKeyBoardNbrBtn",
						listeners : {
							"click" : function(thiz, e) {
								softKeyBoardDO
										.setPosition(dishesOrderCenterPanel
												.getInnerWidth() + 77, 187);
								softKBRelateItemId = "orderNbr";
								softKeyBoardDO.show();
							}
						}
					} ]
				} ]
	} ]
});

var dishesChooseByKitchenForm = new Ext.form.FormPanel(
		{
			title : "分厨选菜",
			id : "dishesChooseByKitchenForm",
			border : false,
			frame : true,
			items : [ {
				contentEl : "kitchenSelectDO"
			} ],
			listeners : {
				render : function(thiz) {
					document.getElementById("kitchenSelectDO").style["visibility"] = "visible";
					// bind the kitchen select image click function
					kitchenSelectLoad();
				}
			}
		});

var dishesDisplayTabPanel = new Ext.TabPanel({
	activeTab : 0,
	// height : 65,
	height : 135,
	region : "north",
	border : false,
	items : [ dishesChooseByNumForm, dishesChooseBySpellForm,
			dishesChooseByKitchenForm ],
	listeners : {
		// for FF only!!! FF when clicking the tab, the focus of the number
		// field
		// does not lost!!!
		tabchange : function(thiz, panel) {
			// dishesOrderEastPanel.findById("orderCountNum").fireEvent("blur",
			// dishesOrderEastPanel.findById("orderCountNum"));
			// dishesOrderEastPanel.findById("orderCountSpell").fireEvent("blur",
			// dishesOrderEastPanel.findById("orderCountSpell"));

			// hide the soft keyboard
			if (softKeyBoardDO.isVisible()) {
				softKeyBoardDO.hide();
			}

			// change the height of the form panel
			if (panel.getId() == "dishesChooseByKitchenForm") {
				dishesDisplayTabPanel.setHeight(135);
				dishesOrderEastPanel.doLayout();
			} else {
				dishesDisplayTabPanel.setHeight(65);
				dishesOrderEastPanel.doLayout();
			}

			// show all the dishes
			dishesDisplayDataShow.length = 0;
			for ( var i = 0; i < dishesDisplayData.length; i++) {
				dishesDisplayDataShow.push([ dishesDisplayData[i][0],
						dishesDisplayData[i][1], dishesDisplayData[i][2],
						dishesDisplayData[i][3], dishesDisplayData[i][4],
						dishesDisplayData[i][5], dishesDisplayData[i][6],
						dishesDisplayData[i][7], dishesDisplayData[i][8],
						dishesDisplayData[i][9] ]);
			}
			dishesDisplayStore.reload();

			// clear the number or spell input
			dishesOrderEastPanel.findById("orderSpell").setValue("");
			dishesOrderEastPanel.findById("orderNbr").setValue("");
		}
	}
});

var dishesOrderEastPanel = new Ext.Panel({
	region : "east",
	collapsible : true,
	width : 432,
	minSize : 432,
	maxSize : 432,
	split : true,
	id : "dishesOrderEastPanel",
	layout : "border",
	items : [ dishesDisplayTabPanel, dishesDisplayGrid ]
});

// --------------dishes order north panel-----------------
var dishesOrderNorthPanel = new Ext.Panel({
	id : "dishesOrderNorthPanel",
	region : "north",
	title : "<div style='font-size:18px;padding-left:2px'>新下单<div>",
	height : 75,
	border : false,
	layout : "form",
	frame : true,
	// contentEl : "tableStatusDO",
	items : [ {
		layout : "column",
		border : false,
		anchor : "98%",
		items : [ {
			layout : "form",
			border : false,
			id : "tableNbrFrom",
			width : 120,
			contentEl : "tableStatusTableNbr"
		}, {
			layout : "form",
			border : false,
			width : 50,
			contentEl : "tableStatusPerCount"
		}, {
			layout : "form",
			border : false,
			width : 50,
			items : [ {
				xtype : "numberfield",
				id : "tablePersonCount",
				width : 45,
				hideLabel : true,
				value : 1,
				validator : function(v) {
					if (v >= 0 && v <= 99) {
						return true;
					} else {
						return "人数输入范围是０～９９";
					}
				}

			} ]
		}, {
			layout : "form",
			border : false,
			width : 100,
			contentEl : "tableStatusMinCost"
		} ]
	} ],
	listeners : {
		render : function(thiz) {
			// tableStuLoad();
		}
	}
});

Ext
		.onReady(function() {
			// 解决ext中文传入后台变问号问题
			Ext.lib.Ajax.defaultPostHeader += '; charset=utf-8';
			Ext.QuickTips.init();

			// *************整体布局*************
			var centerPanelDO = new Ext.Panel({
				id : "centerPanelDO",
				region : "center",
				border : false,
				margins : "0 5 0 0",
				layout : "border",
				items : [ dishesOrderCenterPanel, dishesOrderEastPanel,
						dishesOrderNorthPanel ]
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
									margins : "0 0 5 0"
								},
								centerPanelDO,
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
			// 1,调整colDisplayFormUQ中表格的高度
			// Ext.EventManager.onWindowResize(function() {
			// dishesDisplayGrid
			// .setHeight(dishesOrderEastPanel.getInnerHeight() - 100);
			// // dataSortGridUQ.setHeight(150);
			// });
		});
