//修改link
function billOptModifyHandler(rowindex) {
	// "51","100","2011-07-26
	// 23:23:41","一般","现金","100.44","150.0","0","3","0","0.0","","","","0.0","1","1"
	var tableNbr = billsData[rowindex][1];
	var tableNbr2 = billsData[rowindex][7];
	var category = billsData[rowindex][3];
	var orderID = billsData[rowindex][0];
	var personCount = billsData[rowindex][8];
	var discountType = billsData[rowindex][16];
	var payType = billsData[rowindex][15];
	var give = billsData[rowindex][14];
	var payManner = billsData[rowindex][4];
	var serviceRate = billsData[rowindex][10];
	var memberID = billsData[rowindex][11];
	var comment = billsData[rowindex][13];
	var minCost = billsData[rowindex][9];
	location.href = "BillModify.html?pin=" + pin + "&restaurantID="
			+ restaurantID + "&category=" + category + "&tableNbr=" + tableNbr
			+ "&tableNbr2=" + tableNbr2 + "&personCount=" + personCount
			+ "&minCost=" + minCost + "&orderID=" + orderID + "&give=" + give
			+ "&payType=" + payType + "&discountType=" + discountType
			+ "&payManner=" + payManner + "&serviceRate=" + serviceRate
			+ "&memberID=" + memberID;
};

var modifyBillBut = new Ext.ux.ImageButton({
	imgPath : "../images/modifyBill.png",
	imgWidth : 50,
	imgHeight : 50,
	tooltip : "修改",
	handler : function(btn) {
		if (currRowIndex != -1) {
			billOptModifyHandler(currRowIndex);
		}
	}
});

// 查看link
var viewBillGenPanel = new Ext.Panel({
	region : "north",
	height : 140,
	frame : true,
	items : [ {
		border : false,
		contentEl : "billView"
	} ]
});

var viewBillAddPanel = new Ext.Panel({
	region : "south",
	height : 60,
	frame : true,
	items : [ {
		border : false,
		contentEl : "billViewAddInfo"
	} ]
});

// 1，表格的数据store
var viewBillData = [];

var viewBillStore = new Ext.data.Store({
	proxy : new Ext.data.MemoryProxy(viewBillData),
	reader : new Ext.data.ArrayReader({}, [ {
		name : "dishName"
	}, {
		name : "dishCount"
	}, {
		name : "discount"
	}, {
		name : "dishTaste"
	}, {
		name : "totalPrice"
	} ])
});

viewBillStore.reload();

// 2，栏位模型
var viewBillColumnModel = new Ext.grid.ColumnModel([
		new Ext.grid.RowNumberer(), {
			header : "菜名",
			sortable : true,
			dataIndex : "dishName",
			width : 100
		}, {
			header : "数量",
			sortable : true,
			dataIndex : "dishCount",
			width : 50
		}, {
			header : "折扣",
			sortable : true,
			dataIndex : "discount",
			width : 50
		}, {
			header : "口味",
			sortable : true,
			dataIndex : "dishTaste",
			width : 100
		}, {
			header : "金额（￥）",
			sortable : true,
			dataIndex : "totalPrice",
			width : 100
		} ]);

// 3,表格
var viewBillGrid = new Ext.grid.GridPanel({
	title : "已点菜",
	border : false,
	ds : viewBillStore,
	cm : viewBillColumnModel,
	// sm : new Ext.grid.RowSelectionModel({
	// singleSelect : true
	// }),
	listeners : {

	}
});

var viewBillDtlPanel = new Ext.Panel({
	region : "center",
	layout : "fit",
	// height : 260,
	frame : true,
	items : viewBillGrid
});

var viewBillWin = new Ext.Window(
		{
			layout : "fit",
			title : "查看账单",
			width : 450,
			height : 500,
			closeAction : "hide",
			resizable : false,
			// closable : false,
			items : [ {
				layout : "border",
				border : false,
				items : [ viewBillGenPanel, viewBillDtlPanel, viewBillAddPanel ]
			} ],
			buttons : [ {
				text : "确定",
				handler : function() {
					viewBillWin.hide();
				}
			}, {
				text : "打印",
				disabled : true,
				handler : function() {

					// Ext.Ajax.request({
					// url : "../PrintOrder.do",
					// params : {
					// "pin" : currPin,
					// "printShift" : 1
					// },
					// success : function(response, options) {
					// var resultJSON = Ext.util.JSON
					// .decode(response.responseText);
					// Ext.MessageBox.show({
					// msg : resultJSON.data,
					// width : 300,
					// buttons : Ext.MessageBox.OK
					// });
					//
					// },
					// failure : function(response, options) {
					// }
					// });
				}
			} ],
			listeners : {
				"show" : function(thiz) {
					var billID = billsData[currRowIndex][0];
					var tableType = billsData[currRowIndex][3];
					var tableNbr = billsData[currRowIndex][1];
					var personNbr = billsData[currRowIndex][8];
					var billDate = billsData[currRowIndex][2];
					var billPayType = billsData[currRowIndex][4];
					var billPayManna = billsData[currRowIndex][15];
					var PayMannaDescr;
					if (billPayManna == "1") {
						PayMannaDescr = "一般";
					} else {
						PayMannaDescr = "会员";
					}
					var billServiceRate = billsData[currRowIndex][10];
					var billWaiter = billsData[currRowIndex][17];
					var billForFree = billsData[currRowIndex][14];
					var billShouldPay = billsData[currRowIndex][5];
					var billAvtrualPay = billsData[currRowIndex][6];

					document.getElementById("billIDBV").innerHTML = billID;
					document.getElementById("billTypeBV").innerHTML = tableType;
					document.getElementById("tableNbrBV").innerHTML = tableNbr;
					document.getElementById("personNbrBV").innerHTML = personNbr;
					document.getElementById("billDateBV").innerHTML = billDate;
					document.getElementById("payTypeBV").innerHTML = PayMannaDescr;
					document.getElementById("payMannerBV").innerHTML = billPayType;
					document.getElementById("serviceRateBV").innerHTML = billServiceRate
							+ "％";
					document.getElementById("waiterBV").innerHTML = billWaiter;
					document.getElementById("forFreeBV").innerHTML = "￥"
							+ billForFree;
					document.getElementById("shouldPayBV").innerHTML = "￥"
							+ billShouldPay;
					document.getElementById("actrualPayBV").innerHTML = "￥"
							+ billAvtrualPay;

					// 后台：["菜名",菜名编号,厨房编号,"口味",口味编号,数量,￥单价,是否特价,是否推荐,是否停售,是否赠送,折扣率,口味编号2,口味编号3,￥口味价钱]
					Ext.Ajax
							.request({
								url : "../QueryOrder.do",
								params : {
									"pin" : pin,
									"orderID" : billID
								},
								success : function(response, options) {
									var resultJSON = Ext.util.JSON
											.decode(response.responseText);
									if (resultJSON.success == true) {
										if (resultJSON.data != "NULL") {
											var josnData = resultJSON.data;
											var orderList = josnData.split("，");
											for ( var i = 0; i < orderList.length; i++) {

												var orderInfo = orderList[i]
														.substr(
																1,
																orderList[i].length - 2)
														.split(",");
												// 实价 = 单价 * 數量 * 折扣 + 口味价钱
												var singlePrice = parseFloat(orderInfo[6]
														.substr(
																2,
																orderInfo[6].length - 3));
												var tastePrice = parseFloat(orderInfo[14]
														.substr(
																2,
																orderInfo[14].length - 3));
												var acturalPrice = 0.0;
												acturalPrice = singlePrice
														* parseFloat(orderInfo[5])
														* parseFloat(orderInfo[11])
														+ tastePrice;
												acturalPrice = "￥"
														+ acturalPrice
																.toFixed(1);
												viewBillData
														.push([
																orderInfo[0]
																		.substr(
																				1,
																				orderInfo[0].length - 2), // 菜名
																orderInfo[5],// 数量
																orderInfo[11], // 折扣率
																orderInfo[3]
																		.substr(
																				1,
																				orderInfo[3].length - 2),// 口味
																acturalPrice // 实价
														]);
											}

											// // 算應收
											// var shouldPayOth = 0.0;
											// for ( var i = 0; i <
											// viewBillData.length; i++) {
											// shouldPayOth = shouldPayOth
											// + parseFloat(viewBillData[i][4]
											// .substring(1));
											// }
											// document
											// .getElementById("shouldPayBV").innerHTML
											// = "￥"
											// + shouldPayOth;

											viewBillStore.reload();
										}
									} else {
										var dataInfo = resultJSON.data;
										// Ext.Msg.alert(tableData);
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

				},
				"hide" : function(thiz) {
					viewBillData.length = 0;
				}
			}
		});

function billViewHandler() {
	if (currRowIndex != -1) {
		viewBillWin.show();
	}
};

var viewBillBut = new Ext.ux.ImageButton({
	imgPath : "../images/viewBill.png",
	imgWidth : 50,
	imgHeight : 50,
	tooltip : "查看",
	handler : function(btn) {
		billViewHandler();
	}
});

// 明細link
// 2，表格的数据store
// 前台： [日期,名称,单价,数量,折扣,口味,口味价钱,厨房,服务员,备注]
// var billDetailData = [];

var billDetailStore = new Ext.data.Store({
	proxy : new Ext.data.HttpProxy({
		url : "../QueryDetail.do"
	}),
	reader : new Ext.data.JsonReader({
		totalProperty : "totalProperty",
		root : "root"
	}, [ {
		name : "order_date"
	}, {
		name : "food_name"
	}, {
		name : "unit_price"
	}, {
		name : "amount"
	}, {
		name : "discount"
	}, {
		name : "taste_pref"
	}, {
		name : "taste_price"
	}, {
		name : "kitchen"
	}, {
		name : "waiter"
	}, {
		name : "comment"
	}, {
		name : "message"
	} ])
});

// billDetailStore.reload();

// 3，栏位模型
var billDetailColumnModel = new Ext.grid.ColumnModel([
		new Ext.grid.RowNumberer(), {
			header : "日期",
			sortable : true,
			dataIndex : "order_date",
			width : 120
		}, {
			header : "名称",
			sortable : true,
			dataIndex : "food_name",
			width : 100
		}, {
			header : "单价",
			sortable : true,
			dataIndex : "unit_price",
			width : 100
		}, {
			header : "数量",
			sortable : true,
			dataIndex : "amount",
			width : 100
		}, {
			header : "折扣",
			sortable : true,
			dataIndex : "discount",
			width : 100
		}, {
			header : "口味",
			sortable : true,
			dataIndex : "taste_pref",
			width : 110
		}, {
			header : "口味价钱",
			sortable : true,
			dataIndex : "taste_price",
			width : 100
		}, {
			header : "厨房",
			sortable : true,
			dataIndex : "kitchen",
			width : 100
		}, {
			header : "服务员",
			sortable : true,
			dataIndex : "waiter",
			width : 100
		}, {
			header : "备注",
			sortable : true,
			dataIndex : "comment",
			width : 100
		} ]);

// 4，表格
var billDetailGrid = new Ext.grid.GridPanel({
	ds : billDetailStore,
	cm : billDetailColumnModel,
	sm : new Ext.grid.RowSelectionModel({
		singleSelect : true
	}),
	listeners : {

	},
	bbar : new Ext.PagingToolbar({
		pageSize : billDetailpageRecordCount,
		store : billDetailStore,
		displayInfo : true,
		displayMsg : '显示第 {0} 条到 {1} 条记录，共 {2} 条',
		emptyMsg : "没有记录"
	}),
	autoScroll : true,
	loadMask : {
		msg : "数据加载中，请稍等..."
	}
});

// 为store配置beforeload监听器
billDetailGrid.getStore().on('beforeload', function() {
	var billId = billsData[currRowIndex][0];
	// 输入查询条件参数
	this.baseParams = {
		"pin" : pin,
		"orderID" : billId
	};

});

// 为store配置load监听器(即load完后动作)
billDetailGrid.getStore().on('load', function() {
	var msg = this.getAt(0).get("message");
	if (msg != "normal") {
		Ext.MessageBox.show({
			msg : msg,
			width : 110,
			buttons : Ext.MessageBox.OK
		});
		this.removeAll();
	}
});

// 彈出框
billDetailWin = new Ext.Window({
	layout : "fit",
	width : 1100,
	height : 320,
	closeAction : "hide",
	resizable : true,
	items : billDetailGrid,
	buttons : [ {
		text : "关闭",
		handler : function() {
			billDetailWin.hide();
		}
	} ],
	listeners : {
		"show" : function(thiz) {
			billDetailStore.reload({
				params : {
					start : 0,
					limit : billDetailpageRecordCount
				}
			});
		}
	}
});

function billDetailHandler() {
	if (currRowIndex != -1) {
		billDetailWin.show();
	}
};

var detailBillBut = new Ext.ux.ImageButton({
	imgPath : "../images/detailBill.png",
	imgWidth : 50,
	imgHeight : 50,
	tooltip : "明细",
	handler : function(btn) {
		billDetailHandler();
	}
});

// 打印link
function printBillFunc(rowInd) {
	Ext.Ajax.request({
		url : "../PrintOrder.do",
		params : {
			"pin" : pin,
			"orderID" : billsData[rowInd][0],
			"printReceipt" : 1
		},
		success : function(response, options) {
			var resultJSON = Ext.util.JSON.decode(response.responseText);
			// currRowIndex = -1;
			Ext.MessageBox.show({
				msg : resultJSON.data,
				width : 300,
				buttons : Ext.MessageBox.OK
			});
		},
		failure : function(response, options) {
		}
	});
};

var printBillImgBut = new Ext.ux.ImageButton({
	imgPath : "../images/printBill.png",
	imgWidth : 50,
	imgHeight : 50,
	tooltip : "补打结账",
	handler : function(btn) {
		if (currRowIndex != -1) {
			printBillFunc(currRowIndex);
		}
	}
});

// --
var pushBackBut = new Ext.ux.ImageButton({
	imgPath : "../images/UserLogout.png",
	imgWidth : 50,
	imgHeight : 50,
	tooltip : "返回",
	handler : function(btn) {
		location.href = "PersonLogin.html?restaurantID=" + restaurantID
				+ "&isNewAccess=false&pin=" + pin;
	}
});

var logOutBut = new Ext.ux.ImageButton({
	imgPath : "../images/ResLogout.png",
	imgWidth : 50,
	imgHeight : 50,
	tooltip : "登出",
	handler : function(btn) {
	}
});

// ------------------ north ------------------------
// combom
var filterTypeData = [ [ "0", "全部" ], [ "1", "帐单号" ], [ "2", "台号" ],
		[ "8", "日期时间" ], [ "4", "类型" ], [ "5", "结帐方式" ], [ "6", "金额" ],
		[ "7", "实收" ] ];
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

			var conditionDate = new Ext.form.TimeField({
				hideLabel : true,
				id : "conditionDate",
				allowBlank : false,
				format : "H:i:s",
				width : 120
			});

			var tableTypeData = [ [ "1", "一般" ], [ "2", "外卖" ], [ "3", "并台" ],
					[ "4", "拼台" ] ];
			var tableTypeComb = new Ext.form.ComboBox({
				hideLabel : true,
				forceSelection : true,
				width : 120,
				// value : "等于",
				id : "tableTypeComb",
				store : new Ext.data.SimpleStore({
					fields : [ "value", "text" ],
					data : tableTypeData
				}),
				valueField : "value",
				displayField : "text",
				typeAhead : true,
				mode : "local",
				triggerAction : "all",
				selectOnFocus : true,
				allowBlank : false
			});

			var payTypeData = [ [ "1", "现金" ], [ "2", "刷卡" ], [ "3", "会员卡" ],
					[ "4", "签单" ], [ "5", "挂账" ] ];
			var payTypeComb = new Ext.form.ComboBox({
				hideLabel : true,
				forceSelection : true,
				width : 120,
				// value : "等于",
				id : "payTypeComb",
				store : new Ext.data.SimpleStore({
					fields : [ "value", "text" ],
					data : payTypeData
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
			} else if (conditionType == "date") {
				searchForm.remove("conditionDate");
			} else if (conditionType == "tableTypeComb") {
				searchForm.remove("tableTypeComb");
			} else if (conditionType == "payTypeComb") {
				searchForm.remove("payTypeComb");
			}

			// ------------------ add field -------------------
			operatorComb.setDisabled(false);
			if (index == 0) {
				// 全部
				// searchForm.add(conditionText);
				operatorComb.setValue("等于");
				operatorComb.setDisabled(true);
				conditionType = "text";
			} else if (index == 1) {
				// 帐单号
				searchForm.add(conditionNumber);
				// searchForm.items.add(conditionNumber);
				conditionType = "number";
			} else if (index == 2) {
				// 台号
				searchForm.add(conditionNumber);
				conditionType = "number";
			} else if (index == 3) {
				// 日期时间
				searchForm.add(conditionDate);
				conditionType = "date";
			} else if (index == 4) {
				// 类型
				searchForm.add(tableTypeComb);
				operatorComb.setValue("等于");
				operatorComb.setDisabled(true);
				tableTypeComb.setValue("一般");
				conditionType = "tableTypeComb";
			} else if (index == 5) {
				// 结帐方式
				searchForm.add(payTypeComb);
				operatorComb.setValue("等于");
				operatorComb.setDisabled(true);
				payTypeComb.setValue("现金");
				conditionType = "payTypeComb";
			} else if (index == 6) {
				// 金额
				searchForm.add(conditionNumber);
				conditionType = "number";
			} else if (index == 7) {
				// 实收
				searchForm.add(conditionNumber);
				conditionType = "number";
			}

			billsQueryCondPanel.doLayout();
		}
	}
});

var operatorData = [ [ "1", "等于" ], [ "2", "大于等于" ], [ "3", "小于等于" ] ];
var operatorComb = new Ext.form.ComboBox({
	hideLabel : true,
	forceSelection : true,
	width : 100,
	value : "等于",
	id : "operator",
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

// dymatic form
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

// 高級搜索彈出框
var tableTypeDataAdvSrch = [ [ "6", "全部" ], [ "1", "一般" ], [ "2", "外卖" ],
		[ "3", "并台" ], [ "4", "拼台" ] ];
var tableTypeCombAdvSrch = new Ext.form.ComboBox({
	// hideLabel : true,
	forceSelection : true,
	fieldLabel : "类型",
	width : 100,
	value : "全部",
	id : "tableTypeCombAdvSrch",
	store : new Ext.data.SimpleStore({
		fields : [ "value", "text" ],
		data : tableTypeDataAdvSrch
	}),
	valueField : "value",
	displayField : "text",
	typeAhead : true,
	mode : "local",
	triggerAction : "all",
	selectOnFocus : true,
	allowBlank : false
});

var payTypeDataAdvSrch = [ [ "6", "全部" ], [ "1", "现金" ], [ "2", "刷卡" ],
		[ "3", "会员卡" ], [ "4", "签单" ], [ "5", "挂账" ] ];
var payTypeCombAdvSrch = new Ext.form.ComboBox({
	// hideLabel : true,
	forceSelection : true,
	fieldLabel : "结帐方式",
	width : 100,
	value : "全部",
	id : "payTypeCombAdvSrch",
	store : new Ext.data.SimpleStore({
		fields : [ "value", "text" ],
		data : payTypeDataAdvSrch
	}),
	valueField : "value",
	displayField : "text",
	typeAhead : true,
	mode : "local",
	triggerAction : "all",
	selectOnFocus : true,
	allowBlank : false
});

advSrchForm = new Ext.form.FormPanel({
	frame : true,
	border : false,
	layout : "fit",
	items : [ {
		layout : "column",
		autoHeight : true, // important!!
		autoWidth : true,
		border : false,
		anchor : '98%',
		items : [ {
			layout : "form",
			border : false,
			labelSeparator : '：',
			labelWidth : 40,
			// width : 170,
			columnWidth : .50,
			items : [ {
				xtype : "timefield",
				fieldLabel : "时间",
				format : "H:i:s",
				width : 100,
				id : "advSrchStartTime"
			} ]
		}, {
			layout : 'form',
			border : false,
			labelWidth : 20,
			// width : 170,
			columnWidth : .50,
			labelSeparator : '',
			items : [ {
				xtype : "timefield",
				fieldLabel : "至",
				format : "H:i:s",
				width : 100,
				id : "advSrchEndTime"
			} ]
		}, {
			layout : "form",
			border : false,
			labelSeparator : '：',
			labelWidth : 40,
			// width : 170,
			columnWidth : .50,
			items : [ {
				xtype : "numberfield",
				fieldLabel : "金額",
				width : 100,
				id : "advSrchStartAmt"
			} ]
		}, {
			layout : 'form',
			border : false,
			labelWidth : 20,
			// width : 170,
			columnWidth : .50,
			labelSeparator : '',
			items : [ {
				xtype : "numberfield",
				fieldLabel : "至",
				width : 100,
				id : "advSrchEndAmt"
			} ]
		}, {
			layout : "form",
			border : false,
			labelSeparator : '：',
			labelWidth : 40,
			// width : 170,
			columnWidth : .50,
			items : [ {
				xtype : "textfield",
				fieldLabel : "台号",
				width : 100,
				id : "advSrchTableNbr"
			} ]
		}, {
			layout : "form",
			border : false,
			labelSeparator : '：',
			labelWidth : 60,
			// width : 170,
			columnWidth : .50,
			items : payTypeCombAdvSrch
		}, {
			layout : "form",
			border : false,
			labelSeparator : '：',
			labelWidth : 40,
			// width : 170,
			columnWidth : .50,
			items : tableTypeCombAdvSrch
		} ]
	} ]
});

advSrchWin = new Ext.Window(
		{
			layout : "fit",
			title : "高级搜索",
			width : 370,
			height : 190,
			// height : 500,
			closeAction : "hide",
			resizable : false,
			items : advSrchForm,
			buttons : [
					{
						text : "搜索",
						handler : function() {
							advSrchWin.hide();

							// bill adv srch
							// 1, get parameters
							var timeBegin = advSrchForm.findById(
									"advSrchStartTime").getValue();
							var endBegin = advSrchForm.findById(
									"advSrchEndTime").getValue();
							var amountBegin = advSrchForm.findById(
									"advSrchStartAmt").getValue();
							var amountEnd = advSrchForm.findById(
									"advSrchEndAmt").getValue();
							var tableNumber = advSrchForm.findById(
									"advSrchTableNbr").getValue();

							var payManner = payTypeCombAdvSrch.getValue();
							var in_payManner;
							if (payManner == "全部") {
								in_payManner = 6;
							} else {
								in_payManner = payManner;
							}
							;

							var tableType = tableTypeCombAdvSrch.getValue();
							var in_tableType;
							if (tableType == "全部") {
								in_tableType = 6;
							} else {
								in_tableType = tableType;
							}
							;

							// 2, do the search
							Ext.Ajax
									.request({
										url : "../QueryTodayAdv.do",
										params : {
											"pin" : pin,
											"timeBegin" : timeBegin,
											"timeEnd" : endBegin,
											"amountBegin" : amountBegin,
											"amountEnd" : amountEnd,
											"tableNumber" : tableNumber,
											"payManner" : in_payManner,
											"tableType" : in_tableType
										},
										success : function(response, options) {
											var resultJSON = Ext.util.JSON
													.decode(response.responseText);
											if (resultJSON.success == true) {
												var josnData = resultJSON.data;
												if (josnData != "") {
													var billList = josnData
															.split("，");
													billsData.length = 0;
													for ( var i = 0; i < billList.length; i++) {
														var billInfo = billList[i]
																.substr(
																		1,
																		billList[i].length - 2)
																.split(",");

														// 格式：["账单号","台号","日期","类型","结帐方式","金额","实收","台号2","就餐人数","最低消","服务费率","会员编号","会员姓名","账单备注","赠券金额","结帐类型","折扣类型","服务员"]
														// 后台格式：["账单号","台号","日期","类型","结帐方式","金额","实收","台号2","就餐人数","最低消","服务费率","会员编号","会员姓名","账单备注","赠券金额","结帐类型","折扣类型","服务员"]
														billsData
																.push([
																		billInfo[0]
																				.substr(
																						1,
																						billInfo[0].length - 2),// 账单号
																		billInfo[1]
																				.substr(
																						1,
																						billInfo[1].length - 2),// 台号
																		billInfo[2]
																				.substr(
																						1,
																						billInfo[2].length - 2),// 日期
																		billInfo[3]
																				.substr(
																						1,
																						billInfo[3].length - 2),// 类型
																		billInfo[4]
																				.substr(
																						1,
																						billInfo[4].length - 2), // 结帐方式
																		billInfo[5]
																				.substr(
																						1,
																						billInfo[5].length - 2), // 金额
																		billInfo[6]
																				.substr(
																						1,
																						billInfo[6].length - 2), // 实收
																		billInfo[7]
																				.substr(
																						1,
																						billInfo[7].length - 2), // 台号2
																		billInfo[8]
																				.substr(
																						1,
																						billInfo[8].length - 2), // 就餐人数
																		billInfo[9]
																				.substr(
																						1,
																						billInfo[9].length - 2), // 最低消
																		billInfo[10]
																				.substr(
																						1,
																						billInfo[10].length - 2), // 服务费率
																		billInfo[11]
																				.substr(
																						1,
																						billInfo[11].length - 2), // 会员编号
																		billInfo[12]
																				.substr(
																						1,
																						billInfo[12].length - 2), // 会员姓名
																		billInfo[13]
																				.substr(
																						1,
																						billInfo[13].length - 2), // 账单备注
																		billInfo[14]
																				.substr(
																						1,
																						billInfo[14].length - 2), // 赠券金额
																		billInfo[15]
																				.substr(
																						1,
																						billInfo[15].length - 2), // 结帐类型
																		billInfo[16]
																				.substr(
																						1,
																						billInfo[16].length - 2), // 折扣类型
																		billInfo[17]
																				.substr(
																						1,
																						billInfo[17].length - 2), // 服务员
																]);

													}

													// sum the prices
													var sumShouldPay = 0;
													var sumActualPay = 0;
													for ( var i = 0; i < billsData.length; i++) {
														sumShouldPay = sumShouldPay
																+ parseFloat(billsData[i][5]);
														sumActualPay = sumActualPay
																+ parseFloat(billsData[i][6]);
													}
													document
															.getElementById("shouldPaySum").innerHTML = sumShouldPay
															.toFixed(2);
													document
															.getElementById("actualPaySum").innerHTML = sumActualPay
															.toFixed(2);

												} else {
													billsData.length = 0;
												}
												billsStore.reload();
											}
										},
										failure : function(response, options) {
										}
									});
						}
					}, {
						text : "取消",
						handler : function() {
							advSrchWin.hide();
						}
					} ],
			listeners : {
				"show" : function() {
					tableTypeCombAdvSrch.setValue("全部");
					payTypeCombAdvSrch.setValue("全部");
				}
			}
		});

// panel
var billsQueryCondPanel = new Ext.form.FormPanel({
	region : "north",
	border : false,
	height : 23,
	// contentEl : "queryCondition"
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
					"click" : billQueryHandler
				}
			} ]
		}, {
			layout : 'form',
			border : false,
			width : 110,
			items : [ {
				xtype : "button",
				hideLabel : true,
				id : "advSrchBtn",
				text : "高级搜索",
				width : 100,
				// disabled : true,
				listeners : {
					"click" : function() {
						advSrchWin.show();
					}
				}
			} ]
		} ]
	} ]
});

// center
function billOpt(value, cellmeta, record, rowIndex, columnIndex, store) {
	return "<center><a href=\"javascript:billOptModifyHandler(" + rowIndex
			+ ")\">" + "<img src='../images/Modify.png'/>修改</a>"
			+ "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;"
			+ "<a href=\"javascript:billViewHandler()\">"
			+ "<img src='../images/del.png'/>查看</a>"
			+ "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;"
			+ "<a href=\"javascript:billDetailHandler()\">"
			+ "<img src='../images/Modify.png'/>明细</a>"
			+ "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;"
			+ "<a href=\"javascript:printBillFunc(" + rowIndex + ")\">"
			+ "<img src='../images/Modify.png'/>补打</a>" + "</center>";
};

// 1，表格的数据store
var billsStore = new Ext.data.Store({
	proxy : new Ext.data.MemoryProxy(billsData),
	reader : new Ext.data.ArrayReader({}, [ {
		name : "billNumber"
	}, {
		name : "tableNumber"
	}, {
		name : "payDate"
	}, {
		name : "billType"
	}, {
		name : "payType"
	}, {
		name : "totalPrice"
	}, {
		name : "acturalPrice"
	}, {
		name : "billOpt"
	}, {
		name : "tableNbr2"
	}, {
		name : "personCount"
	}, {
		name : "minCost"
	}, {
		name : "serviceRate"
	}, {
		name : "memberID"
	}, {
		name : "memberName"
	}, {
		name : "comment"
	}, {
		name : "give"
	} ])
});

billsStore.reload();

// 2，栏位模型
var billsColumnModel = new Ext.grid.ColumnModel([ new Ext.grid.RowNumberer(), {
	header : "帐单号",
	sortable : true,
	dataIndex : "billNumber",
	width : 120
}, {
	header : "台号",
	sortable : true,
	dataIndex : "tableNumber",
	width : 120
}, {
	header : "日期",
	sortable : true,
	dataIndex : "payDate",
	width : 120
}, {
	header : "类型",
	sortable : true,
	dataIndex : "billType",
	width : 120
}, {
	header : "结帐方式",
	sortable : true,
	dataIndex : "payType",
	width : 120
}, {
	header : "金额（￥）",
	sortable : true,
	dataIndex : "totalPrice",
	width : 120
}, {
	header : "实收（￥）",
	sortable : true,
	dataIndex : "acturalPrice",
	width : 120
}, {
	header : "<center>操作</center>",
	sortable : true,
	dataIndex : "billOpt",
	width : 270,
	renderer : billOpt
} ]);

Ext
		.onReady(function() {
			// 解决ext中文传入后台变问号问题
			Ext.lib.Ajax.defaultPostHeader += '; charset=utf-8';
			Ext.QuickTips.init();

			// 3,表格
			var billsGrid = new Ext.grid.GridPanel({
				title : "帐单",
				xtype : "grid",
				anchor : "99%",
				region : "center",
				border : false,
				ds : billsStore,
				cm : billsColumnModel,
				sm : new Ext.grid.RowSelectionModel({
					singleSelect : true
				}),
				listeners : {
					rowclick : function(thiz, rowIndex, e) {
						currRowIndex = rowIndex;
					}
				}
			});

			var billSum = new Ext.Panel({
				region : "south",
				frame : true,
				border : false,
				height : 40,
				contentEl : "billSum"
			});

			var centerPanel = new Ext.Panel({
				region : "center",
				layout : "fit",
				frame : true,
				items : [ {
					layout : "border",
					title : "<div style='font-size:20px;'>帐单信息<div>",
					items : [ billsQueryCondPanel, billsGrid, billSum ]
				} ],
				tbar : new Ext.Toolbar({
					height : 55,
					items : [ modifyBillBut, {
						text : "&nbsp;&nbsp;&nbsp;",
						disabled : true
					}, viewBillBut, {
						text : "&nbsp;&nbsp;&nbsp;",
						disabled : true
					}, detailBillBut, {
						text : "&nbsp;&nbsp;&nbsp;",
						disabled : true
					}, printBillImgBut, "->", pushBackBut, {
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
