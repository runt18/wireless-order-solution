// dish count input pop window
dishCountInputWin = new Ext.Window({
	layout : "fit",
	width : 200,
	height : 100,
	closeAction : "hide",
	resizable : false,
	closable : false,
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
	}, {
		name : "dishNbr"
	}, {
		name : "kitchNbr"
	}, {
		name : "tasteNbr"
	}, {
		name : "special"
	}, {
		name : "jian"
	}, {
		name : "soldOut"
	}, {
		name : "forFree"
	}, {
		name : "discountRate"
	}, {
		name : "currPrice"
	} ])
});

orderedStore.reload();

// 3，栏位模型
function dishOptTasteHandler(rowIndex) {
	if (dishOrderCurrRowIndex_ != -1) {
		if (orderedData[rowIndex][18] == "false") {
			dishOrderCurrRowIndex_ = rowIndex;
			dishTasteWindow.show();
		} else {
			Ext.MessageBox.show({
				msg : "临时菜不支持口味选择",
				width : 300,
				buttons : Ext.MessageBox.OK
			});
		}
	}
};

function dishOptDeleteHandler(rowIndex) {

	if (dishOrderCurrRowIndex_ != -1) {
		// if (Request["tableStat"] == "used") {
		// dishPushBackWin.show();
		// } else {
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
		// }
	}
};

// function dishOptPressHandler(rowIndex) {
//
// if (dishOrderCurrRowIndex_ != -1) {
// // Ext.Msg.alert("", "已催菜！");
// // orderedStore.reload();
// // dishOrderCurrRowIndex_ = -1;
// }
//
// };

function dishOptDispley(value, cellmeta, record, rowIndex, columnIndex, store) {
	return "<center><a href=\"javascript:dishOptTasteHandler(" + rowIndex
			+ ")\">" + "<img src='../../images/Modify.png'/>口味</a>"
			+ "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;"
			+ "<a href=\"javascript:dishOptDeleteHandler(" + rowIndex + ")\">"
			+ "<img src='../../images/del.png'/>删除</a>"
			// + "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;"
			// + "<a href=\"javascript:dishOptPressHandler(" + rowIndex + ")\">"
			// + "<img src='../images/Modify.png'/>催菜</a>"
			+ "</center>";
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
			header : "折扣率",
			sortable : true,
			dataIndex : "discountRate",
			width : 120
		// ,
		// editor : new Ext.form.NumberField({
		// allowBlank : false,
		// allowNegative : false,
		// maxValue : 100000
		// })
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
// var dishPressImgBut = new Ext.ux.ImageButton({
// imgPath : "../images/HurryFood.png",
// imgWidth : 50,
// imgHeight : 50,
// tooltip : "催菜",
// handler : function(btn) {
// dishOptPressHandler(dishOrderCurrRowIndex_);
// }
// });
var countAddImgBut = new Ext.ux.ImageButton(
		{
			imgPath : "../../images/AddCount.png",
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
			imgPath : "../../images/MinusCount.png",
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
	imgPath : "../../images/EqualCount.png",
	imgWidth : 50,
	imgHeight : 50,
	tooltip : "数量等于",
	handler : function(btn) {
		if (dishOrderCurrRowIndex_ != -1) {
			dishCountInputWin.show();
		}
	}
});

var orderedGrid = new Ext.grid.EditorGridPanel({
	title : "已点菜式",
	xtype : "grid",
	anchor : "99%",
	region : "center",
	border : false,
	clicksToEdit : 1,
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
		},
		// dishPressImgBut,
		dishDeleteImgBut, {
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
		// }, printBillImgBut
		// , {
		// text : "&nbsp;&nbsp;&nbsp;",
		// disabled : true
		// }, printDetailImgBut
		]
	}),
	listeners : {
		rowclick : function(thiz, rowIndex, e) {
			dishOrderCurrRowIndex_ = rowIndex;
		},
		afteredit : function(Obj) {
			var row = Obj.row;
			var editValue = Obj.value;
			orderedData[row][13] = editValue;
		}
	// ,
	// render : function(thiz) {
	// orderedDishesOnLoad();
	// }
	}
});

var orderedForm = new Ext.form.FormPanel(
		{
			frame : true,
			border : false,
			region : "south",
			height : 30,
			items : [ {} ],
			buttons : [
					{
						// tableID="100"&customNum="3"&foods="{[1100,2,1,0]}"
						// 各字段表示的意义：
						// tableID：餐台号
						// customNum：就餐人数
						// foods：菜品列表，格式为{[菜品1编号,菜品1数量,口味1编号,厨房1编号,菜品1折扣,2nd口味1编号,3rd口味1编号]}
						// 以点菜式格式：[菜名，口味，数量，￥单价，操作，￥实价，菜名编号，厨房编号，口味编号1,特,荐,停,送,折扣率,￥口味价钱,口味编号2,口味编号3]
						text : "提交",
						handler : function() {
							if (orderedData.length > 0) {

								var foodPara = "";
								for ( var i = 0; i < orderedData.length; i++) {
									if (orderedData[i][18] == "false") {
										// [是否临时菜(false),菜品1编号,菜品1数量,口味1编号,厨房1编号,菜品1折扣,2nd口味1编号,3rd口味1编号]
										foodPara = foodPara + "[false,"// 是否临时菜(false)
												+ orderedData[i][6] + "," // 菜品1编号
												+ orderedData[i][2] + "," // 菜品1数量
												+ orderedData[i][8] + "," // 口味1编号
												+ orderedData[i][7] + ","// 厨房1编号
												+ orderedData[i][13] + "," // 折扣率
												+ orderedData[i][15] + ","// 2nd口味1编号
												+ orderedData[i][16] // 3rd口味1编号
												+ "]，";
									} else {
										// 是否临时菜(true),临时菜1编号,临时菜1名称,临时菜1数量,临时菜1单价
										var price = orderedData[i][3].substr(1,
												orderedData[i][3].length - 1);
										foodPara = foodPara + "[true,"// 是否临时菜(true)
												+ orderedData[i][6] + "," // 临时菜1编号
												+ orderedData[i][19] + "," // 临时菜1名称
												+ orderedData[i][2] + "," // 临时菜1数量
												+ price + "" // 临时菜1单价(原材料單價)
												+ "]，";
									}

								}
								foodPara = "{"
										+ foodPara.substr(0,
												foodPara.length - 1) + "}";

								// alert(foodPara);

								var categoryOut;
								if (Request["category"] == "一般") {
									categoryOut = 1;
								} else if (Request["category"] == "外卖") {
									categoryOut = 2;
								} else if (Request["category"] == "并台") {
									categoryOut = 3;
								} else if (Request["category"] == "拼台") {
									categoryOut = 4;
								}

								var payMannerOut;
								var payMannerIn = billGenModForm.getForm()
										.findField("payManner").getGroupValue();
								if (payMannerIn == "cashPay") {
									payMannerOut = 1;
								} else if (payMannerIn == "cardPay") {
									payMannerOut = 2;
								} else if (payMannerIn == "memberPay") {
									payMannerOut = 3;
								} else if (payMannerIn == "handPay") {
									payMannerOut = 5;
								} else if (payMannerIn == "signPay") {
									payMannerOut = 4;
								}

								var serviceRateIn = billGenModForm.findById(
										"serviceRate").getValue();

								var commentOut = billGenModForm.findById(
										"remark").getValue();

								var memberIDOut = Request["memberID"] + "";

								orderedForm.buttons[0].setDisabled(true);
								Ext.Ajax
										.request({
											url : "../../UpdateOrder2.do",
											params : {
												"pin" : pin,
												"orderID" : Request["orderID"],
												"category" : categoryOut,
												"customNum" : Request["personCount"],
												"payType" : payType,
												"discountType" : discountType,
												"payManner" : payMannerOut,
												"serviceRate" : serviceRateIn,
												"memberID" : memberIDOut,
												"comment" : commentOut,
												// use
												"foods" : foodPara
											},
											success : function(response,
													options) {
												var resultJSON = Ext.util.JSON
														.decode(response.responseText);
												if (resultJSON.success == true) {

													// var
													// Request =
													// new
													// URLParaQuery();

													// 彈出成功提示語，打印提示語
													Ext.MessageBox
															.show({
																msg : resultJSON.data
																		+ "，是否打印账单？",
																width : 300,
																buttons : Ext.MessageBox.YESNO,
																fn : function(
																		btn) {
																	if (btn == "yes") {

																		Ext.Ajax
																				.request({
																					url : "../../PrintOrder.do",
																					params : {
																						"pin" : pin,
																						"orderID" : Request["orderID"],
																						"printReceipt" : 1
																					},
																					success : function(
																							response,
																							options) {
																						var resultJSON1 = Ext.util.JSON
																								.decode(response.responseText);
																						Ext.MessageBox
																								.show({
																									msg : resultJSON1.data,
																									width : 300,
																									buttons : Ext.MessageBox.OK,
																									fn : function() {
																										location.href = "Bills.html?pin="
																												+ pin
																												+ "&restaurantID="
																												+ restaurantID;
																									}
																								});

																					},
																					failure : function(
																							response,
																							options) {
																					}
																				});
																	} else {
																		location.href = "Bills.html?pin="
																				+ pin
																				+ "&restaurantID="
																				+ restaurantID;
																	}
																}
															});

												} else {
													orderedForm.buttons[0]
															.setDisabled(false);
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
												orderedForm.buttons[0]
														.setDisabled(false);
												Ext.MessageBox.show({
													msg : "Unknow page error",
													width : 300,
													buttons : Ext.MessageBox.OK
												});
											}
										});
							}
						}
					},
					{
						text : "返回",
						handler : function() {
							if (orderIsChanged == false) {
								location.href = "Bills.html?pin=" + pin
										+ "&restaurantID=" + restaurantID;
							} else {
								Ext.MessageBox.show({
									msg : "账单修改还未提交，是否确认返回？",
									width : 300,
									buttons : Ext.MessageBox.YESNO,
									fn : function(btn) {
										if (btn == "yes") {
											location.href = "Bills.html?pin="
													+ pin + "&restaurantID="
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
	listeners : {}
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

	}
});

// tab面版
var dishTasteTabPanel = new Ext.TabPanel({
	// layout : "fit",
	tabPosition : "top",
	width : 300,
	height : 300,
	activeTab : 0,
	items : [ dishTasteGridTas, dishTasteGridPar, dishTasteGridSiz ]
});

var dishTasteWindow = new Ext.Window(
		{
			layout : "fit",
			width : 550,
			height : 300,
			closeAction : "hide",
			// plain: true,
			items : dishTasteTabPanel,
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
						}
					},
					{
						text : "确定",
						handler : function() {
							// dishTasteWindow.hide();
							var choosenCount = 0;
							// 格式：[{編號,描述,價錢或比例,計算方式}]
							choosenTaset = [];
							dishTasteGridTas
									.getStore()
									.each(
											function(record) {
												if (record.get("tasteChoose")) {
													// 累計選擇數目，準備校驗超數
													choosenCount = choosenCount + 1;

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
													// 累計選擇數目，準備校驗超數
													choosenCount = choosenCount + 1;

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
													// 累計選擇數目，準備校驗超數
													choosenCount = choosenCount + 1;

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

							if (choosenCount > 3) {
								// 超過３個口味
								Ext.MessageBox.show({
									msg : "暂不允许选择超过３种口味",
									width : 300,
									buttons : Ext.MessageBox.OK
								});

							} else if (choosenCount == 0) {
								// 未有選擇口味
								orderedData[dishOrderCurrRowIndex_][8] = "0";
								orderedData[dishOrderCurrRowIndex_][15] = "0";
								orderedData[dishOrderCurrRowIndex_][16] = "0";
								orderedData[dishOrderCurrRowIndex_][1] = "无口味";

								orderedData[dishOrderCurrRowIndex_][5] = orderedData[dishOrderCurrRowIndex_][3];

								// refresh
								orderedStore.reload();
								// 底色处理，已点菜式原色底色
								// dishGridRefresh();
								orderIsChanged = true;

								// hide the window
								dishTasteWindow.hide();
							} else {
								// 校驗通過

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
									orderedData[dishOrderCurrRowIndex_][15] = choosenTaset[1][0];
									orderedData[dishOrderCurrRowIndex_][1] = orderedData[dishOrderCurrRowIndex_][1]
											+ choosenTaset[1][1];
									orderedData[dishOrderCurrRowIndex_][1] = orderedData[dishOrderCurrRowIndex_][1]
											+ "；";
								}
								// 第三口味
								if (choosenTaset[2] != undefined) {
									orderedData[dishOrderCurrRowIndex_][16] = choosenTaset[2][0];
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
								// dishGridRefresh();
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
					var tasteNbr2 = orderedData[dishOrderCurrRowIndex_][15];
					var tasteNbr3 = orderedData[dishOrderCurrRowIndex_][16];

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
					if (tasteNbr1 == 0 && tasteNbr2 == 0 && tasteNbr3 == 0) {

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
											}
										});
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
	// if ((softKBRelateItemId == "orderCountNum" && (Ext.getCmp(
	// "orderCountNum").getValue() == 0 || Ext.getCmp(
	// "orderCountNum").getValue() == ""))
	// || (softKBRelateItemId == "orderCountSpell" && (Ext.getCmp(
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
				// mune格式：[菜名，菜名编号，菜名拼音，单价，厨房编号,特,荐,停,送,時]
				// ordered格式：[菜名，口味，数量，￥单价，操作，￥实价，菜名编号，厨房编号，口味编号1,特,荐,停,送,折扣率,￥口味价钱,口味编号2,口味编号3,時]
				// var dishCurrCount = dishesOrderEastPanel.findById(
				// "orderCountNum").getValue();
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
					orderedData.push([ dishCurrName,// 菜名
					"无口味",// 口味
					1,// 数量
					dishCurrPrice, // 单价
					"",// 操作
					dishCurrPrice,// 实价
					dishNbr,// 菜名编号
					kitchenNbr,// 厨房编号
					0, // 口味编号
					dishesDisplayDataShow[rowIndex][5],// 特
					dishesDisplayDataShow[rowIndex][6],// 荐
					dishesDisplayDataShow[rowIndex][7],// 停
					dishesDisplayDataShow[rowIndex][8],// 送
					"1",// 折扣率
					"￥0",// ￥口味价钱
					0,// 口味编号2
					0, // 口味编号3
					dishesDisplayDataShow[rowIndex][9], // 時
					"false", // 是否临时菜
					"" // 菜名ORIG
					]);
				}
				orderedStore.reload();
				dishOrderCurrRowIndex_ = -1;
				orderIsChanged = true;

				// refresh the discount rate
				billListRefresh();
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
						// softKeyBoardDO.hide();
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
								// .getInnerWidth() + 77, 302);
								// softKBRelateItemId = "orderNbr";
								// softKeyBoardDO.show();

							},
							render : function(thiz) {
								// dishNbrOnLoad();
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
	title : "<div style='font-size:18px;padding-left:2px'>" + orderID
			+ "号帐单修改<div>",
	height : 75,
	border : false,
	layout : "form",
	frame : true,
	contentEl : "tableStatusDO",
	listeners : {
		render : function(thiz) {
			// tableStuLoad();
		}
	}
});

/*---------------- bill general modification ---------------- */
var discountKindData = [ [ "0", "一般" ], [ "1", "会员" ] ];

var discountKindComb = new Ext.form.ComboBox({
	fieldLabel : "结账方式",
	// labelStyle : "font-size:14px;font-weight:bold;",
	forceSelection : true,
	value : "一般",
	id : "payTpye",
	store : new Ext.data.SimpleStore({
		fields : [ "value", "text" ],
		data : discountKindData
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
			billListRefresh();
		}
	}
});

var billGenModForm = new Ext.form.FormPanel({
	frame : true,
	border : false,
	title : "总体信息",
	layout : "fit",
	items : [ {
		layout : "column",
		// height : 1,
		border : false,
		items : [ {
			layout : "form",
			border : false,
			labelSeparator : '：',
			labelWidth : 80,
			width : 300,
			// columnWidth : .30,
			items : [ discountKindComb ]
		}, {
			// columnWidth : .10,
			width : 170,
			layout : 'form',
			labelWidth : 80,
			labelSeparator : '',
			// hideLabels : true,
			border : false,
			items : [ {
				xtype : 'radio',
				fieldLabel : '折扣方式',
				boxLabel : '折扣1',
				checked : true,
				name : 'discountRadio',
				inputValue : 'discount1',
				anchor : '95%',
				listeners : {
					check : function(thiz, newValue, oldValue) {
						billListRefresh();
					}
				}
			} ]
		}, {
			// columnWidth : .10,
			width : 90,
			layout : 'form',
			labelWidth : 0,
			labelSeparator : '',
			hideLabels : true,
			border : false,
			items : [ {
				xtype : 'radio',
				// fieldLabel : '',
				boxLabel : '折扣2',
				name : 'discountRadio',
				inputValue : 'discount2',
				anchor : '95%',
				listeners : {
					check : function(thiz, newValue, oldValue) {
						billListRefresh();
					}
				}
			} ]
		}, {
			// columnWidth : .10,
			width : 90,
			layout : 'form',
			labelWidth : 0,
			labelSeparator : '',
			hideLabels : true,
			border : false,
			items : [ {
				xtype : 'radio',
				// fieldLabel : '',
				boxLabel : '折扣3',
				name : 'discountRadio',
				inputValue : 'discount3',
				anchor : '95%',
				listeners : {
					check : function(thiz, newValue, oldValue) {
						billListRefresh();
					}
				}
			} ]
		} ]
	}, {
		layout : "column",
		// height : 10,
		border : false,
		items : [ {
			layout : "form",
			border : false,
			labelSeparator : '：',
			labelWidth : 80,
			width : 253,
			items : [ {
				xtype : "numberfield",
				fieldLabel : "服务费",
				id : "serviceRate",
				allowBlank : false,
				anchor : "%99"
			} ]
		}, {
			// layout : "form",
			// border : false,
			// labelSeparator : '：',
			// labelWidth : 80,
			width : 47,
			html : "%"
		}, {
			// columnWidth : .10,
			width : 170,
			layout : 'form',
			labelWidth : 80,
			labelSeparator : '',
			// hideLabels : true,
			border : false,
			items : [ {
				xtype : 'radio',
				fieldLabel : '付款方式',
				boxLabel : '现金结帐',
				checked : true,
				name : 'payManner',
				inputValue : 'cashPay',
				anchor : '95%',
				listeners : {
					check : function(thiz, newValue, oldValue) {
						// alert("1");
						// checkOurListRefresh();
					}
				}
			} ]
		}, {
			// columnWidth : .10,
			width : 90,
			layout : 'form',
			labelWidth : 0,
			labelSeparator : '',
			hideLabels : true,
			border : false,
			items : [ {
				xtype : 'radio',
				// fieldLabel : '',
				boxLabel : '刷卡结帐',
				name : 'payManner',
				inputValue : 'cardPay',
				anchor : '95%',
				listeners : {
					check : function(thiz, newValue, oldValue) {
						// alert("2");
						// checkOurListRefresh();
					}
				}
			} ]
		}, {
			// columnWidth : .10,
			width : 60,
			layout : 'form',
			labelWidth : 0,
			labelSeparator : '',
			hideLabels : true,
			border : false,
			items : [ {
				xtype : 'radio',
				// fieldLabel : '',
				boxLabel : '挂帐',
				name : 'payManner',
				inputValue : 'handPay',
				anchor : '95%',
				listeners : {
					check : function(thiz, newValue, oldValue) {
						// alert("3");
						// checkOurListRefresh();
					}
				}
			} ]
		}, {
			// columnWidth : .10,
			width : 70,
			layout : 'form',
			labelWidth : 0,
			labelSeparator : '',
			hideLabels : true,
			border : false,
			items : [ {
				xtype : 'radio',
				// fieldLabel : '',
				boxLabel : '会员卡',
				name : 'payManner',
				inputValue : 'memberPay',
				anchor : '95%',
				listeners : {
					check : function(thiz, newValue, oldValue) {
						// alert("3");
						// checkOurListRefresh();
					}
				}
			} ]
		}, {
			// columnWidth : .10,
			width : 90,
			layout : 'form',
			labelWidth : 0,
			labelSeparator : '',
			hideLabels : true,
			border : false,
			items : [ {
				xtype : 'radio',
				// fieldLabel : '',
				boxLabel : '签单',
				name : 'payManner',
				inputValue : 'signPay',
				anchor : '95%',
				listeners : {
					check : function(thiz, newValue, oldValue) {
						// alert("3");
						// checkOurListRefresh();
					}
				}
			} ]
		} ]
	}, {
		layout : "column",
		// height:1,
		border : false,
		items : [ {
			layout : "form",
			border : false,
			labelSeparator : '：',
			labelWidth : 80,
			width : 1000,
			items : [ {
				xtype : "textfield",
				fieldLabel : "备注",
				id : "remark",
				anchor : "%99"
			} ]
		} ]
	} ]
});

Ext
		.onReady(function() {
			// 解决ext中文传入后台变问号问题
			Ext.lib.Ajax.defaultPostHeader += '; charset=utf-8';
			Ext.QuickTips.init();

			// *************整体布局************

			var northPanelDO = new Ext.Panel({
				id : "northPanelDO",
				region : "north",
				border : false,
				// margins : "0 5 0 0",
				height : 115,
				// height : 215,
				layout : "fit",
				items : billGenModForm
			});

			var centerPanelDO = new Ext.Panel({
				id : "centerPanelDO",
				region : "center",
				border : false,
				// margins : "0 5 0 0",
				layout : "border",
				items : [ dishesOrderCenterPanel, dishesOrderEastPanel,
						northPanelDO ]
			});

			var billModCenterPanel = new Ext.Panel({
				id : "billModCenterPanel",
				region : "center",
				border : false,
				margins : "0 5 0 0",
				layout : "border",
				items : [ centerPanelDO, dishesOrderNorthPanel ]
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
								billModCenterPanel,
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
