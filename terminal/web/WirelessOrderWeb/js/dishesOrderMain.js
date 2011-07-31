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

// 3，栏位模型
function dishOptTasteHandler(rowIndex) {
	if (dishOrderCurrRowIndex_ != -1) {
		dishOrderCurrRowIndex_ = rowIndex;
		dishTasteWindow.show();
	}
};

var dishPushBackWin = new Ext.Window({
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
					var dishPushBackPwd = dishPushBackWin.findById(
							"dishPushBackPwd").getValue();
					dishPushBackWin.findById("dishPushBackPwd").setValue("");

					var pwdTrans;
					if (dishPushBackPwd != "") {
						pwdTrans = MD5(dishPushBackPwd);
					} else {
						pwdTrans = dishPushBackPwd;
					}

					dishPushBackWin.hide();

					Ext.Ajax.request({
						url : "../VerifyPwd.do",
						params : {
							"pin" : Request["pin"],
							"type" : "3",
							"pwd" : pwdTrans
						},
						success : function(response, options) {
							var resultJSON = Ext.util.JSON
									.decode(response.responseText);
							if (resultJSON.success == true) {
								orderedData.splice(dishOrderCurrRowIndex_, 1);
								orderedStore.reload();
								orderIsChanged = true;
								dishOrderCurrRowIndex_ = -1;

								Ext.MessageBox.show({
									msg : resultJSON.data,
									width : 300,
									buttons : Ext.MessageBox.OK
								});
							} else {
								Ext.MessageBox.show({
									msg : resultJSON.data,
									width : 300,
									buttons : Ext.MessageBox.OK
								});
							}
						},
						failure : function(response, options) {
						}
					});
				}
			}, {
				text : "取消",
				handler : function() {
					dishPushBackWin.hide();
					dishPushBackWin.findById("dishPushBackPwd").setValue("");
				}
			} ],
	listeners : {
		show : function(thiz) {
			// thiz.findById("personCountInput").focus();
			var f = Ext.get("dishPushBackPwd");
			f.focus.defer(100, f); // 万恶的EXT！为什么这样才可以！？！？
		}
	}
});
function dishOptDeleteHandler(rowIndex) {

	if (dishOrderCurrRowIndex_ != -1) {
		if (Request["tableStat"] == "used") {
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
	return "<center><a href=\"javascript:dishOptTasteHandler(" + rowIndex
			+ ")\">" + "<img src='../images/Modify.png'/>口味</a>"
			+ "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;"
			+ "<a href=\"javascript:dishOptDeleteHandler(" + rowIndex + ")\">"
			+ "<img src='../images/del.png'/>删除</a>"
			+ "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;"
			+ "<a href=\"javascript:dishOptPressHandler(" + rowIndex + ")\">"
			+ "<img src='../images/Modify.png'/>催菜</a>" + "</center>";
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
	imgPath : "../images/Taste.png",
	imgWidth : 50,
	imgHeight : 50,
	tooltip : "口味",
	handler : function(btn) {
		dishOptTasteHandler(dishOrderCurrRowIndex_);
	}
});

var dishDeleteImgBut = new Ext.ux.ImageButton({
	imgPath : "../images/DeleteDish.png",
	imgWidth : 50,
	imgHeight : 50,
	tooltip : "删除",
	handler : function(btn) {
		dishOptDeleteHandler(dishOrderCurrRowIndex_);
	}
});
var dishPressImgBut = new Ext.ux.ImageButton({
	imgPath : "../images/HurryFood.png",
	imgWidth : 50,
	imgHeight : 50,
	tooltip : "催菜",
	handler : function(btn) {
		dishOptPressHandler(dishOrderCurrRowIndex_);
	}
});
var countAddImgBut = new Ext.ux.ImageButton(
		{
			imgPath : "../images/AddCount.png",
			imgWidth : 50,
			imgHeight : 50,
			tooltip : "数量加1",
			handler : function(btn) {
				if (dishOrderCurrRowIndex_ != -1) {
					orderedData[dishOrderCurrRowIndex_][2] = parseFloat(orderedData[dishOrderCurrRowIndex_][2]) + 1;
					orderedStore.reload();
					orderedGrid.getSelectionModel().selectRow(
							dishOrderCurrRowIndex_);
					orderIsChanged = true;
				}
			}
		});
var countMinusImgBut = new Ext.ux.ImageButton(
		{
			imgPath : "../images/MinusCount.png",
			imgWidth : 50,
			imgHeight : 50,
			tooltip : "数量减1",
			handler : function(btn) {
				if (dishOrderCurrRowIndex_ != -1) {
					if (orderedData[dishOrderCurrRowIndex_][2] != "1") {
						orderedData[dishOrderCurrRowIndex_][2] = parseFloat(orderedData[dishOrderCurrRowIndex_][2]) - 1;
						orderedStore.reload();
						orderedGrid.getSelectionModel().selectRow(
								dishOrderCurrRowIndex_);
						orderIsChanged = true;
					}
				}

			}
		});
var countEqualImgBut = new Ext.ux.ImageButton({
	imgPath : "../images/EqualCount.png",
	imgWidth : 50,
	imgHeight : 50,
	tooltip : "数量等于",
	handler : function(btn) {
		if (dishOrderCurrRowIndex_ != -1) {
			dishCountInputWin.show();
		}
	}
});
var printTotalImgBut = new Ext.ux.ImageButton({
	imgPath : "../images/PrintTotal.png",
	imgWidth : 50,
	imgHeight : 50,
	tooltip : "补打总单",
	handler : function(btn) {
		Ext.Ajax.request({
			url : "../PrintOrder.do",
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
	imgPath : "../images/PrintDetail.png",
	imgWidth : 50,
	imgHeight : 50,
	tooltip : "补打明细",
	handler : function(btn) {
		Ext.Ajax.request({
			url : "../PrintOrder.do",
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
		}, '-', dishDeleteImgBut, {
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
						// foods：菜品列表，格式为{[菜品1编号,菜品1数量,口味1编号,厨房1编号]，[菜品2编号,菜品2数量,口味2编号,厨房2编号]}
						// 以点菜式格式：[菜名，口味，数量，单价，操作，实价，菜名编号，厨房编号，口味编号]
						text : "提交",
						handler : function() {
							if (orderedData.length > 0) {

								var Request = new URLParaQuery();

								var foodPara = "";
								for ( var i = 0; i < orderedData.length; i++) {
									foodPara = foodPara + "["
											+ orderedData[i][6] + "," // 菜品1编号
											+ orderedData[i][2] + "," // 菜品1数量
											+ orderedData[i][8] + "," // 口味1编号
											+ orderedData[i][7] // 厨房1编号
											+ "]，";
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
								Ext.Ajax
										.request({
											url : "../InsertOrder.do",
											params : {
												"pin" : Request["pin"],
												"tableID" : Request["tableNbr"],
												"tableID_2" : Request["tableNbr2"],
												"customNum" : Request["personCount"],
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
var dishTasteStore = new Ext.data.Store({
	proxy : new Ext.data.MemoryProxy(dishTasteData),
	reader : new Ext.data.ArrayReader({}, [ {
		name : "dishTaste"
	}, {
		name : "tastePrice"
	} ])
});

dishTasteStore.reload();

// 3，栏位模型
var dishTasteColumnModel = new Ext.grid.ColumnModel([
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
		} ]);

// 4，表格
var dishTasteGrid = new Ext.grid.GridPanel({
	title : "可选口味",
	anchor : "99%",
	ds : dishTasteStore,
	cm : dishTasteColumnModel,
	sm : new Ext.grid.RowSelectionModel({
		singleSelect : true
	}),
	listeners : {
		rowdblclick : function(thiz, rowIndex, e) {
			// 口味格式：[口味，价钱，口味编号]
			// 以点菜式格式：[菜名，口味，数量，单价，操作，实价，菜名编号，厨房编号，口味编号,特,荐,停]
			var selectedTaste = dishTasteData[rowIndex][0];
			var tastePrice = dishTasteData[rowIndex][1];
			var tasteNbr = dishTasteData[rowIndex][2];
			var dishIndex = dishOrderCurrRowIndex_;
			// update taste
			orderedData[dishIndex][1] = selectedTaste;
			// update price
			var currPrice = parseFloat(orderedData[dishIndex][3].substring(1))
					+ parseFloat(tastePrice.substring(1)) + "";
			if (currPrice.indexOf(".") < 0) {
				currPrice = currPrice + ".00";
			} else if (currPrice.length - currPrice.indexOf(".") == 3) {
				currPrice = currPrice;
			} else if (currPrice.length - currPrice.indexOf(".") == 2) {
				currPrice = currPrice + "0";
			}
			orderedData[dishIndex][5] = "￥" + currPrice;
			// update taste num
			orderedData[dishIndex][8] = tasteNbr;

			orderedStore.reload();
			orderIsChanged = true;
			dishTasteWindow.hide();
			dishOrderCurrRowIndex_ = -1;
		}
	}
});

var dishTasteWindow = new Ext.Window({
	layout : "fit",
	width : 250,
	height : 300,
	closeAction : "hide",
	// plain: true,
	items : dishTasteGrid
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
		},
		beforehide : function(thiz) {
			if ((softKBRelateItemId == "orderCountNum" && (Ext.getCmp(
					"orderCountNum").getValue() == 0 || Ext.getCmp(
					"orderCountNum").getValue() == ""))
					|| (softKBRelateItemId == "orderCountSpell" && (Ext.getCmp(
							"orderCountSpell").getValue() == 0 || Ext.getCmp(
							"orderCountSpell").getValue() == ""))) {
				return false;
			} else {
				return true;
			}
		}
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
var dishesDisplayGrid = new Ext.grid.GridPanel({
	xtype : "grid",
	// height : 400,
	anchor : "98%",
	autoScroll : true,
	region : "center",
	ds : dishesDisplayStore,
	cm : dishesDisplayColumnModel,
	sm : new Ext.grid.RowSelectionModel({
		singleSelect : true
	}),
	listeners : {
		rowdblclick : function(thiz, rowIndex, e) {

			if (dishesDisplayDataShow[rowIndex][7] == "false") {
				// mune格式：[菜名，菜名编号，菜名拼音，单价，厨房编号]
				// ordered格式：[菜名，口味，数量，单价，操作，实价，菜名编号，厨房编号，口味编号,特,荐,停]
				var dishCurrCount = dishesOrderEastPanel.findById(
						"orderCountNum").getValue();
				var dishCurrName = dishesDisplayDataShow[rowIndex][0];
				var dishCurrPrice = dishesDisplayDataShow[rowIndex][3];
				var dishNbr = dishesDisplayDataShow[rowIndex][1];
				var kitchenNbr = dishesDisplayDataShow[rowIndex][4];

				var isAlreadyOrderd = false;

				for ( var i = 0; i < orderedData.length; i++) {
					if (orderedData[i][6] == dishNbr) {
						orderedData[i][2] = parseFloat(orderedData[i][2]) + 1;
						isAlreadyOrderd = true;
					}
				}

				if (isAlreadyOrderd == false) {
					orderedData.push([ dishCurrName, "无口味", dishCurrCount,
							dishCurrPrice, "", dishCurrPrice, dishNbr,
							kitchenNbr, 0, dishesDisplayDataShow[rowIndex][5],
							dishesDisplayDataShow[rowIndex][6],
							dishesDisplayDataShow[rowIndex][7] ]);
				}
				orderedStore.reload();
				dishOrderCurrRowIndex_ = -1;
				orderIsChanged = true;
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
		items : [
				{
					layout : "form",
					labelWidth : 60,
					border : false,
					labelSeparator : '：',
					columnWidth : .50,
					items : [ {
						xtype : "textfield",
						fieldLabel : "数量",
						name : "orderCountSpell",
						id : "orderCountSpell",
						value : 1,
						anchor : "90%",
						listeners : {
							focus : function(thiz) {
								softKeyBoardDO
										.setPosition(dishesOrderCenterPanel
												.getInnerWidth() + 77, 187);
								softKBRelateItemId = "orderCountSpell";
								softKeyBoardDO.show();

							},
							blur : function(thiz) {
								var thisValue = thiz.getValue();
								dishesOrderEastPanel.findById("orderCountNum")
										.setValue(thisValue);
							}
						}
					} ],
					listeners : {
						render : function(thiz) {
							thiz.hide();
						}
					}
				}, {
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
						xtype : "textfield",
						fieldLabel : "数量",
						name : "orderCountNum",
						id : "orderCountNum",
						value : 1,
						anchor : "90%",
						listeners : {
							focus : function(thiz) {
								softKeyBoardDO
										.setPosition(dishesOrderCenterPanel
												.getInnerWidth() + 77, 187);
								softKBRelateItemId = "orderCountNum";
								softKeyBoardDO.show();

							},
							blur : function(thiz) {
								var thisValue = thiz.getValue();
								dishesOrderEastPanel
										.findById("orderCountSpell").setValue(
												thisValue);
							}
						}
					} ],
					listeners : {
						render : function(thiz) {
							thiz.hide();
						}
					}
				},
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
								softKeyBoardDO
										.setPosition(dishesOrderCenterPanel
												.getInnerWidth() + 77, 187);
								softKBRelateItemId = "orderNbr";
								softKeyBoardDO.show();

							},
							render : function(thiz) {
								dishNbrOnLoad();
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
			dishesOrderEastPanel.findById("orderCountNum").fireEvent("blur",
					dishesOrderEastPanel.findById("orderCountNum"));
			dishesOrderEastPanel.findById("orderCountSpell").fireEvent("blur",
					dishesOrderEastPanel.findById("orderCountSpell"));

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
						dishesDisplayData[i][7] ]);
			}
			dishesDisplayStore.reload();
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
	contentEl : "tableStatusDO",
	listeners : {
		render : function(thiz) {
			tableStuLoad();
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
