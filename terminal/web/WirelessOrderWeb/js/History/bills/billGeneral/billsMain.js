/* ---------------------------------------------------------------- */
var kitchenStatBut = new Ext.ux.ImageButton({
	imgPath : "../../images/kitchenStatis.png",
	imgWidth : 50,
	imgHeight : 50,
	tooltip : "分厨统计",
	handler : function(btn) {
//		if (!isPrompt) {
//			isPrompt = true;
//			kitchenStatWin.show(true);
//		}
		salesSub();
		salesSubWinTabPanel.setActiveTab(kitchenStatPanel);
	}
});

var deptStatBut = new Ext.ux.ImageButton({
	imgPath : "../../images/deptStatis.png",
	imgWidth : 50,
	imgHeight : 50,
	tooltip : "部门统计",
	handler : function(btn) {
//		if (!isPrompt) {
//			isPrompt = true;
//			deptStatWin.show();
//		}
		salesSub();
		salesSubWinTabPanel.setActiveTab(deptStatPanel);
	}
});

var regionStatBut = new Ext.ux.ImageButton({
	imgPath : "../../images/regionStatis.png",
	imgWidth : 50,
	imgHeight : 50,
	tooltip : "区域统计",
	handler : function(btn) {
		if (!isPrompt) {
			isPrompt = true;
			regionStatWin.show();
		}
	}
});

var discountStatBut = new Ext.ux.ImageButton({
	imgPath : "../../images/discountStatis.png",
	imgWidth : 50,
	imgHeight : 50,
	tooltip : "折扣统计",
	handler : function(btn) {
		if (!isPrompt) {
			isPrompt = true;
			discountStatWin.show();
		}
	}
});

var shiftStatBut = new Ext.ux.ImageButton({
	imgPath : "../../images/shiftStatis.png",
	imgWidth : 50,
	imgHeight : 50,
	tooltip : "交班记录",
	handler : function(btn) {
		if (!isPrompt) {
			isPrompt = true;
			shiftStatWin.show();
		}
	}
});

var dailySettleStatBut = new Ext.ux.ImageButton({
	imgPath : "../../images/dailySettleStatis.png",
	imgWidth : 50,
	imgHeight : 50,
	tooltip : "日结记录",
	handler : function(btn) {
		if (!isPrompt) {
			isPrompt = true;
			dailySettleStatWin.show();
		}
	}
});

var businessStatBut = new Ext.ux.ImageButton({
	imgPath : "../../images/businessStatis.png",
	imgWidth : 50,
	imgHeight : 50,
	tooltip : "营业统计",
	handler : function(btn) {
		businessStatResultWin.show();
		businessStatResultWin.center();
	}
});

var btnSalesSub = new Ext.ux.ImageButton({
	imgPath : '../../images/HistorySalesSub.png',
	imgWidth : 50,
	imgHeight : 50,
	tooltip : '销售统计',
	handler : function(btn) {
		salesSub();
		salesSubWinTabPanel.setActiveTab(orderFoodStatPanel);
	}
});

var btnCancelledFood = new Ext.ux.ImageButton({
	imgPath : '../../images/cancelledFoodStatis.png',
	imgWidth : 50,
	imgHeight : 50,
	tooltip : '退菜统计',
	handler : function(btn) {
		cancelledFood();
	}
});

// --
var pushBackBut = new Ext.ux.ImageButton({
	imgPath : "../../images/UserLogout.png",
	imgWidth : 50,
	imgHeight : 50,
	tooltip : "返回",
	handler : function(btn) {
		location.href = '../PersonLogin.html?restaurantID=' 
						+ restaurantID 
						+ '&isNewAccess=false'
						+ '&pin='
						+ pin;
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

// 查看link
var viewBillGenPanel = new Ext.Panel({
	region : "north",
	height : 140,
	frame : true,
	border : false,
	items : [ {
		border : false,
		contentEl : "billView"
	} ]
});

var viewBillAddPanel = new Ext.Panel({
	region : "south",
	height : 60,
	frame : true,
	border : false,
	items : [ {
		border : false,
		contentEl : "billViewAddInfo"
	} ]
});

// 1，表格的数据store
var viewBillData = {totalProperty:0, root:[]};

var viewBillStore = new Ext.data.Store({
	proxy : new Ext.data.MemoryProxy(viewBillData),
	reader : new Ext.data.JsonReader(Ext.ux.readConfig, 
	    [ 
	      { name : "foodName" }, 
	      { name : "tastePref"},
	      { name : "count" }, 
	      { name : "discount" }, 	       
	      { name : "acturalPrice"} 
	    ]
	)
});

// 2，栏位模型
var viewBillColumnModel = new Ext.grid.ColumnModel([
	new Ext.grid.RowNumberer(), {
		header : "菜名",
		sortable : true,
		dataIndex : "foodName",
		width : 130
	}, {
		header : "口味",
		sortable : true,
		dataIndex : "tastePref",
		width : 100
	}, {
		header : "数量",
		sortable : true,
		dataIndex : "count",
		width : 50,
		align : 'right',
		renderer : Ext.ux.txtFormat.gridDou
	}, {
		header : "折扣",
		sortable : true,
		dataIndex : "discount",
		width : 50,
		align : 'right',
		renderer : Ext.ux.txtFormat.gridDou
	}, {
		header : "金额（￥）",
		sortable : true,
		dataIndex : "acturalPrice",
		width : 100,
		align : 'right',
		renderer : Ext.ux.txtFormat.gridDou
	} 
]);

// 3,表格
var viewBillGrid = new Ext.grid.GridPanel({
	title : "已点菜",
	border : false,
	ds : viewBillStore,
	cm : viewBillColumnModel,
	loadMask : { msg: '数据请求中，请稍后...' },
	animate : false,
	margins : { top : 0, bottom : 0, right : 0, left : 0 },
	cmargins : { top : 0, bottom : 0, right : 0, left : 0 },
	listeners : {
		
	}
});

var viewBillDtlPanel = new Ext.Panel({
	region : "center",
	layout : "fit",
	// height : 260,
	frame : true,
	border : false,
	items : viewBillGrid
});

var viewBillWin = new Ext.Window({
	layout : "fit",
	title : "查看账单",
	width : 500,
	height : 500,
	closeAction : "hide",
	resizable : false,
	// closable : false,
	items : [ {
		layout : "border",
		border : false,
		items : [ viewBillGenPanel, viewBillDtlPanel, viewBillAddPanel ]
	} ],
	buttons : [{
		text : "确定",
		handler : function() {
			viewBillWin.hide();
		}
	}, {
		text : "打印",
		disabled : true,
		handler : function() {
			
		}
	}],
	listeners : {
		"show" : function(thiz) {
			var selData = billsGrid.getStore().getAt(currRowIndex);
			var billID = selData.get("orderID");
			var tableType = selData.get("orderCategory");
			var tableNbr = selData.get("tableAlias");
			var personNbr = selData.get("customerNum");
			var billDate = selData.get("orderDate");
			var billPayType = selData.get("payManner");
			var payType = selData.get("payType");
			var payTypeDescr;
			if (payType == "1") {
				payTypeDescr = "一般";
			} else {
				payTypeDescr = "会员";
			}
			var billServiceRate = selData.get("serviceRate");
			var billWaiter = selData.get("staff");
			var billForFree = selData.get("giftPrice");
			var billShouldPay = selData.get("totalPrice");
			var billAvtrualPay = selData.get("actualIncome");

			document.getElementById("billIDBV").innerHTML = billID;
			document.getElementById("billTypeBV").innerHTML = tableType;
			document.getElementById("tableNbrBV").innerHTML = tableNbr;
			document.getElementById("personNbrBV").innerHTML = personNbr;
			document.getElementById("billDateBV").innerHTML = billDate;
			document.getElementById("payTypeBV").innerHTML = payTypeDescr;
			document.getElementById("payMannerBV").innerHTML = billPayType;
			document.getElementById("serviceRateBV").innerHTML = billServiceRate + "％";
			document.getElementById("waiterBV").innerHTML = billWaiter;
			document.getElementById("forFreeBV").innerHTML = "￥" + billForFree;
			document.getElementById("shouldPayBV").innerHTML = "￥" + billShouldPay;
			document.getElementById("actrualPayBV").innerHTML = "￥" + billAvtrualPay;

			// 后台：["菜名",菜名编号,厨房编号,"口味",口味编号,数量,￥单价,是否特价,是否推荐,是否停售,是否赠送,折扣率,口味编号2,口味编号3,￥口味价钱,是否時價]
			Ext.Ajax.request({
				url : "../../QueryOrder.do",
				params : {
					"pin" : pin,
					"orderID" : billID,
					"queryType" : "History"
				},
				success : function(response, options) {
					var resultJSON = Ext.util.JSON.decode(response.responseText);					
					if (resultJSON.success == true) {
						viewBillData = resultJSON;
						var tpItem = null;
						var acturalPrice = null;
						for(var i = 0; i < viewBillData.root.length; i++){
							tpItem = viewBillData.root[i];
							acturalPrice = parseFloat( tpItem.unitPrice * tpItem.count *  tpItem.discount + tpItem.tastePrice );
							viewBillData.root[i].acturalPrice = acturalPrice;
							tpItem = null;
							acturalPrice = null;							
						}
						Ext.getDom('discountBV').innerHTML = "￥" + resultJSON.other.order.discountPrice.toFixed(2);
						viewBillStore.loadData(viewBillData);
					} else {
						Ext.MessageBox.show({
							msg : resultJSON.msg,
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
			viewBillData = null;
			viewBillStore.removeAll();
		}
	}
});

function billViewHandler() {
	if (currRowIndex != -1) {
		viewBillWin.show();
	}
};

// 明細link
// 2，表格的数据store
// 前台： [日期,名称,单价,数量,折扣,口味,口味价钱,厨房,服务员,备注]
var billDetailStore = new Ext.data.Store({
	proxy : new Ext.data.HttpProxy({
		url : "../../QueryDetail.do"
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
		name : "isPaid"
	}, {
		name : "isDiscount"
	}, {
		name : "isGift"
	}, {
		name : "isReturn"
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
			header : "反结帐",
			sortable : true,
			dataIndex : "isPaid",
			width : 80,
			renderer : function(v){
				if(v){
					return '是';
				}else{
					return '否';
				}
			}
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
	viewConfig : {
		forceFit : true
	},
	listeners : {},
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
	var orderID = billsGrid.getStore().getAt(currRowIndex).get("orderID");
	// 输入查询条件参数
	this.baseParams = {
		"pin" : pin,
		"orderID" : orderID,
		"queryType" : "History"
	};

});

// 为store配置load监听器(即load完后动作)
billDetailGrid.getStore().on('load', function() {
//	var msg = this.getAt(0).get("message");
//	if (msg != "normal") {
//		Ext.MessageBox.show({
//			msg : msg,
//			width : 110,
//			buttons : Ext.MessageBox.OK
//		});
//		this.removeAll();
//	} else {
//		if (billDetailGrid.getStore().getTotalCount() != 0) {
//			billDetailGrid.getStore().each(function(record) {
//				// 反結帳顯示
//				record.set("isPaid", norCounPayCode2Descr(record.get("isPaid")));
//				// 提交，去掉修改標記
//				record.commit();
//			});
//
//			// 底色处理
//			var conditionRadio = billsQueryCondPanel.getForm().findField("conditionRadio").getGroupValue();
//			if (conditionRadio == "isPaid") {
//				for ( var i = 0; i < billDetailGrid.getStore().getCount(); i++) {
//					var record = billDetailGrid.getStore().getAt(i);
//					if (record.get("isPaid") == norCounPayCode2Descr(COUNTER_PAY)) {
//						billDetailGrid.getView().getRow(i).style.backgroundColor = "#FFFF93";
//					}
//				}
//			} else if (conditionRadio == "discount") {
//				for ( var i = 0; i < billDetailGrid.getStore().getCount(); i++) {
//					var record = billDetailGrid.getStore().getAt(i);
//					if (record.get("isDiscount") == true) {
//						billDetailGrid.getView().getRow(i).style.backgroundColor = "#FFFF93";
//					}
//				}
//			} else if (conditionRadio == "gift") {
//				for ( var i = 0; i < billDetailGrid.getStore().getCount(); i++) {
//					var record = billDetailGrid.getStore().getAt(i);
//					if (record.get("isGift") == true) {
//						billDetailGrid.getView().getRow(i).style.backgroundColor = "#FFFF93";
//					}
//				}
//			} else if (conditionRadio == "return") {
//				for ( var i = 0; i < billDetailGrid.getStore().getCount(); i++) {
//					var record = billDetailGrid.getStore().getAt(i);
//					if (record.get("isReturn") == true) {
//						billDetailGrid.getView().getRow(i).style.backgroundColor = "#FFFF93";
//					}
//				}
//			}
//		}
//	}
});

// 彈出框
billDetailWin = new Ext.Window({
	layout : "fit",
	width : 1100,
	height : 320,
//	closeAction : "hide",
	closable : false,
	resizable : false,
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

// 打印link
function printBillFunc(rowInd) {
	Ext.Ajax.request({
		url : "../../PrintOrder.do",
		params : {
			"pin" : pin,
			"orderID" : billsGrid.getStore().getAt(rowInd).get("orderID"),
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


// ------------------ north ------------------------
var filterTypeComb = new Ext.form.ComboBox({
	fieldLabel : "过滤",
	forceSelection : true,
	width : 100,
	value : 9,
	id : "filter",
	store : new Ext.data.SimpleStore({
		fields : [ "value", "text" ],
		data : [[ "0", "全部" ], [ "1", "帐单号" ], [ "2", "流水号" ],
				[ "3", "台号" ], [ "4", "日期" ], [ "5", "类型" ], [ "6", "结帐方式" ],
				[ "7", "金额" ], [ "8", "实收" ], [ "9", "最近日结" ]]
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
//			var conditionText = new Ext.form.TextField({
//				hideLabel : true,
//				id : "conditionText",
//				allowBlank : false,
//				width : 120
//			});

			var conditionNumber = new Ext.form.NumberField({
				hideLabel : true,
				id : "conditionNumber",
				allowBlank : false,
				width : 120
			});

			var conditionDate = new Ext.form.DateField({
				hideLabel : true,
				id : "conditionDate",
				allowBlank : false,
				format : "Y-m-d",
				width : 120
			});

			var tableTypeComb = new Ext.form.ComboBox({
				hideLabel : true,
				forceSelection : true,
				width : 120,
				id : "tableTypeComb",
				store : new Ext.data.SimpleStore({
					fields : [ "value", "text" ],
					data : [[ "1", "一般" ], [ "2", "外卖" ], [ "3", "并台" ], [ "4", "拼台" ]]
				}),
				valueField : "value",
				displayField : "text",
				typeAhead : true,
				mode : "local",
				triggerAction : "all",
				selectOnFocus : true,
				allowBlank : false
			});

			var payTypeComb = new Ext.form.ComboBox({
				hideLabel : true,
				forceSelection : true,
				width : 120,
				// value : "等于",
				id : "payTypeComb",
				store : new Ext.data.SimpleStore({
					fields : [ "value", "text" ],
					data : [[ "1", "现金" ], [ "2", "刷卡" ], [ "3", "会员卡" ], [ "4", "签单" ], [ "5", "挂账" ]]
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
				operatorComb.setValue("等于");
				operatorComb.setDisabled(true);
				conditionType = "text";
			} else if (index == 1) {
				// 帐单号
				searchForm.add(conditionNumber);
				conditionType = "number";
			} else if (index == 2) {
				// 流水号
				searchForm.add(conditionNumber);
				conditionType = "number";
			} else if (index == 3) {
				// 台号
				searchForm.add(conditionNumber);
				conditionType = "number";
			} else if (index == 4) {
				// 日期时间
				searchForm.add(conditionDate);
				conditionType = "date";
			} else if (index == 5) {
				// 类型
				searchForm.add(tableTypeComb);
				operatorComb.setValue("等于");
				operatorComb.setDisabled(true);
				tableTypeComb.setValue("一般");
				conditionType = "tableTypeComb";
			} else if (index == 6) {
				// 结帐方式
				searchForm.add(payTypeComb);
				operatorComb.setValue("等于");
				operatorComb.setDisabled(true);
				payTypeComb.setValue("现金");
				conditionType = "payTypeComb";
			} else if (index == 7) {
				// 金额
				searchForm.add(conditionNumber);
				conditionType = "number";
			} else if (index == 8) {
				// 实收
				searchForm.add(conditionNumber);
				conditionType = "number";
			} else if (index == 9) {
				// 最近日结
				operatorComb.setValue("等于");
				operatorComb.setDisabled(true);
				conditionType = "text";
			}

			billsQueryCondPanel.doLayout();
		}
	}
});

var operatorComb = new Ext.form.ComboBox({
	hideLabel : true,
	forceSelection : true,
	width : 100,
	value : "等于",
	id : "operator",
	store : new Ext.data.SimpleStore({
		fields : [ "value", "text" ],
		data : [[ "1", "等于" ], [ "2", "大于等于" ], [ "3", "小于等于" ]]
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
var tableTypeCombAdvSrch = new Ext.form.ComboBox({
	// hideLabel : true,
	forceSelection : true,
	fieldLabel : "类型",
	width : 100,
	value : "全部",
	id : "tableTypeCombAdvSrch",
	store : new Ext.data.SimpleStore({
		fields : [ "value", "text" ],
		data : [[ "6", "全部" ], [ "1", "一般" ], [ "2", "外卖" ], [ "3", "并台" ], [ "4", "拼台" ]]
	}),
	valueField : "value",
	displayField : "text",
	typeAhead : true,
	mode : "local",
	triggerAction : "all",
	selectOnFocus : true,
	allowBlank : false
});

var payTypeCombAdvSrch = new Ext.form.ComboBox({
	// hideLabel : true,
	forceSelection : true,
	fieldLabel : "结帐方式",
	width : 100,
	value : "全部",
	id : "payTypeCombAdvSrch",
	store : new Ext.data.SimpleStore({
		fields : [ "value", "text" ],
		data : [[ "6", "全部" ], [ "1", "现金" ], [ "2", "刷卡" ], [ "3", "会员卡" ], [ "4", "签单" ], [ "5", "挂账" ]]
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
		anchor : '100%',
		items : [ {
			layout : "form",
			border : false,
			labelSeparator : '：',
			labelWidth : 60,
			// width : 170,
			columnWidth : .50,
			items : [ {
				xtype : "datefield",
				fieldLabel : "日期",
				format : "Y-m-d",
				width : 100,
				id : "advSrchStartDate"
			} ]
		}, {
			layout : 'form',
			border : false,
			labelWidth : 20,
			// width : 170,
			columnWidth : .50,
			labelSeparator : '',
			items : [ {
				xtype : "datefield",
				fieldLabel : "至",
				format : "Y-m-d",
				width : 100,
				id : "advSrchEndDate"
			} ]
		} ]
	}, {
		layout : "column",
		autoHeight : true, // important!!
		autoWidth : true,
		border : false,
		anchor : '98%',
		items : [ {
			layout : "form",
			border : false,
			labelSeparator : '：',
			labelWidth : 60,
			// width : 170,
			columnWidth : .50,
			items : [ {
				xtype : "numberfield",
				fieldLabel : "金额",
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
		} ]
	}, {
		layout : "column",
		autoHeight : true, // important!!
		autoWidth : true,
		border : false,
		anchor : '98%',
		items : [ {
			layout : "form",
			border : false,
			labelSeparator : '：',
			labelWidth : 60,
			// width : 170,
			columnWidth : .50,
			items : [ {
				xtype : "numberfield",
				fieldLabel : "流水号",
				width : 100,
				id : "advSrchStartSeqNum"
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
				id : "advSrchEndSeqNum"
			} ]
		} ]
	}, {
		layout : "column",
		autoHeight : true, // important!!
		autoWidth : true,
		border : false,
		anchor : '98%',
		items : [ {
			layout : "form",
			border : false,
			labelSeparator : '：',
			labelWidth : 60,
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
		} ]
	}, {
		layout : "column",
		autoHeight : true, // important!!
		autoWidth : true,
		border : false,
		anchor : '98%',
		items : [ {
			layout : "form",
			border : false,
			labelSeparator : '：',
			labelWidth : 60,
			// width : 170,
			columnWidth : .50,
			items : tableTypeCombAdvSrch
		} ]
	} ]
});

advSrchWin = new Ext.Window({
	layout : "fit",
	title : "高级搜索",
	width : 370,
	height : 200,
	// height : 500,
	closeAction : "hide",
	resizable : false,
	items : advSrchForm,
	buttons : [
			{
				text : "搜索",
				handler : function() {
					advSrchWin.hide();

					queryType = "advance";
					billsStore.reload({
						params : {
							"start" : 0,
							"limit" : billRecordCount
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
			advSrchForm.findById("advSrchStartDate").setValue("");
			advSrchForm.findById("advSrchEndDate").setValue("");
			advSrchForm.findById("advSrchStartAmt").setValue("");
			advSrchForm.findById("advSrchEndAmt").setValue("");
			advSrchForm.findById("advSrchStartSeqNum").setValue("");
			advSrchForm.findById("advSrchEndSeqNum").setValue("");
			advSrchForm.findById("advSrchTableNbr").setValue("");
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
			layout : "form",
			border : false,
			width : 60,
			items : [ {
				xtype : 'radio',
				hideLabel : true,
				boxLabel : "全部",
				checked : true,
				name : 'conditionRadio',
				inputValue : 'all'
			} ]
		}, {
			layout : "form",
			border : false,
			labelSeparator : '',
			width : 70,
			items : [ {
				xtype : 'radio',
				hideLabel : true,
				boxLabel : "反结帐",
				name : 'conditionRadio',
				inputValue : 'isPaid'
			} ]
		}, {
			layout : "form",
			border : false,
			labelSeparator : '',
			width : 60,
			items : [ {
				xtype : 'radio',
				hideLabel : true,
				boxLabel : "折扣",
				name : 'conditionRadio',
				inputValue : 'discount'
			} ]
		}, {
			layout : "form",
			border : false,
			labelSeparator : '',
			width : 60,
			items : [ {
				xtype : 'radio',
				hideLabel : true,
				boxLabel : "赠送",
				name : 'conditionRadio',
				inputValue : 'gift'
			} ]
		}, {
			layout : "form",
			border : false,
			labelSeparator : '',
			width : 60,
			items : [ {
				xtype : 'radio',
				hideLabel : true,
				boxLabel : "退菜",
				name : 'conditionRadio',
				inputValue : 'return'
			} ]
		}, {
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
	return ""
			+ "<a href=\"javascript:billViewHandler()\">"
			+ "<img src='../../images/del.png'/>查看</a>"
			+ "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;"
			+ "<a href=\"javascript:billDetailHandler()\">"
			+ "<img src='../../images/Modify.png'/>明细</a>"
			// + "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;"
			// + "<a href=\"javascript:printBillFunc(" + rowIndex + ")\">"
			// + "<img src='../../images/Modify.png'/>补打</a>"
			+ "";
};

// 1，表格的数据store
// ["账单号","台号","日期","类型","结帐方式","金额","实收","台号2",
// "就餐人数","最低消","服务费率","会员编号","会员姓名","账单备注",
// "赠券金额","结帐类型","折扣类型","服务员",是否反結帳, 是否折扣, 是否赠送, 是否退菜, "流水号"]
var billsStore = new Ext.data.Store({
	proxy : new Ext.data.HttpProxy({
		url : "../../QueryHistory.do"
	}),
	autoLoad : false,
	reader : new Ext.data.JsonReader({
		totalProperty : "totalProperty",
		root : "root"
	}, [ {
		name : "orderID"
	}, {
		name : "tableAlias"
	}, {
		name : "orderDate"
	}, {
		name : "orderCategory"
	}, {
		name : "payManner"
	}, {
		name : "totalPrice"
	}, {
		name : "actualIncome"
	}, {
		name : "table2Alias"
	}, {
		name : "customerNum"
	}, {
		name : "minCost"
	}, {
		name : "serviceRate"
	}, {
		name : "giftPrice"
	}, {
		name : "member"
	}, {
		name : "comment"
	}, {
		name : "giftPrice"
	}, {
		name : "payType"
	}, {
		name : "discountType"
	}, {
		name : "staff"
	}, {
		name : "isPaid"
	}, {
		name : "isDiscount"
	}, {
		name : "isGift"
	}, {
		name : "isCancel"
	}, {
		name : "seqID"
	}, {
		name : "billOpt"
	} ])
});

// 2，栏位模型
var billsColumnModel = new Ext.grid.ColumnModel([ new Ext.grid.RowNumberer(), {
	header : "帐单号",
	sortable : true,
	dataIndex : "orderID",
	width : 120
}, {
	header : "流水号",
	sortable : true,
	dataIndex : "seqID",
	width : 120
}, {
	header : "台号",
	sortable : true,
	dataIndex : "tableAlias",
	width : 120
}, {
	header : "日期",
	sortable : true,
	dataIndex : "orderDate",
	width : 120
}, {
	header : "类型",
	sortable : true,
	dataIndex : "orderCategory",
	width : 120
}, {
	header : "结帐方式",
	sortable : true,
	dataIndex : "payManner",
	width : 120
}, {
	header : "金额（￥）",
	sortable : true,
	dataIndex : "totalPrice",
	width : 120
}, {
	header : "实收（￥）",
	sortable : true,
	dataIndex : "actualIncome",
	width : 120
}, {
	header : "反结帐",
	sortable : true,
	dataIndex : "isPaid",
	width : 80
}, {
	header : "<center>操作</center>",
	sortable : true,
	dataIndex : "billOpt",
	align : 'center',
	width : 170,
	renderer : billOpt
} ]);

var billsGrid;
Ext.onReady(function() {
	// 解决ext中文传入后台变问号问题
	Ext.lib.Ajax.defaultPostHeader += '; charset=utf-8';
	Ext.QuickTips.init();

	// 3,表格
	billsGrid = new Ext.grid.GridPanel({
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
		bbar : new Ext.PagingToolbar({
			pageSize : billRecordCount,
			store : billsStore,
			displayInfo : true,
			displayMsg : '显示第 {0} 条到 {1} 条记录，共 {2} 条',
			emptyMsg : "没有记录"
		}),
		autoScroll : true,
		loadMask : {
			msg : "数据加载中，请稍等..."
		},
		listeners : {
			"rowclick" : function(thiz, rowIndex, e) {
				currRowIndex = rowIndex;
			},

			"render" : function(thiz) {
				billQueryHandler();
			}
		}
	});		

	// 为store配置load监听器(即load完后动作)
	billsGrid.getStore().on('load', function() {
		currRowIndex = -1;
		if (billsGrid.getStore().getTotalCount() != 0) {
			billsGrid.getStore().each(function(record) {
				// 反結帳顯示
				record.set("isPaid", norCounPayCode2Descr(record.get("isPaid")));
				// 提交，去掉修改標記
				record.commit();
			});
		}
	});		

	billsGrid.getStore().on('beforeload', function(){
		if (queryType == "normal") {
			var queryTpye = filterTypeComb.getValue();
			if (queryTpye == "全部") {
				queryTpye = 0;
			}

			var queryOperator = operatorComb.getValue();
			if (queryOperator == "等于") {
				queryOperator = 1;
			}

			var queryValue = "";
			if (conditionType == "text" && queryTpye != 0
					&& queryTpye != 9) {
				queryValue = searchForm.findById(
						"conditionText").getValue();
			} else if (conditionType == "number") {
				queryValue = searchForm.findById(
						"conditionNumber").getValue();
			} else if (conditionType == "date") {
				var dateFormated = new Date();
				queryValue = searchForm.findById(
						"conditionDate").getValue();
				dateFormated = queryValue;
				queryValue = dateFormated.format('Y-m-d');
			} else if (conditionType == "tableTypeComb") {
				queryValue = searchForm.findById(
						"tableTypeComb").getValue();
				if (queryValue == "一般") {
					queryValue = 1;
				}
			} else if (conditionType == "payTypeComb") {
				queryValue = searchForm.findById("payTypeComb")
						.getValue();
				if (queryValue == "现金") {
					queryValue = 1;
				}
			}
			
			// -- 獲取額外過濾條件--
			var additionFilter = 0;
			if (billsQueryCondPanel.getForm().findField(
					"conditionRadio") != null) {
				var conditionRadio = billsQueryCondPanel
						.getForm().findField("conditionRadio")
						.getGroupValue();
				if (conditionRadio == "all") {
					additionFilter = 0;
				} else if (conditionRadio == "isPaid") {
					additionFilter = 1;
				} else if (conditionRadio == "discount") {
					additionFilter = 2;
				} else if (conditionRadio == "gift") {
					additionFilter = 3;
				} else if (conditionRadio == "return") {
					additionFilter = 4;
				}
			}
			
			// 输入查询条件参数
			this.baseParams = {
				"pin" : pin,
				"type" : queryTpye,
				"ope" : queryOperator,
				"value" : queryValue,
				"havingCond" : additionFilter,
				"isPaging" : true,
				"queryType" : "normal"
			};
		} else if (queryType == "advance") {
			// bill adv srch
			// 1, get parameters
			var dateFormated = new Date();
			var dateBegin = advSrchForm.findById(
					"advSrchStartDate").getValue();
			var dateEnd = advSrchForm
					.findById("advSrchEndDate").getValue();
			if (dateBegin != "") {
				dateFormated = dateBegin;
				dateBegin = dateFormated.format('Y-m-d');
			}
			if (dateEnd != "") {
				dateFormated = dateEnd;
				dateEnd = dateFormated.format('Y-m-d');
			}

			var amountBegin = advSrchForm.findById("advSrchStartAmt").getValue();
			var amountEnd = advSrchForm.findById("advSrchEndAmt").getValue();
			var seqNumBegin = advSrchForm.findById("advSrchStartSeqNum").getValue();
			var seqNumEnd = advSrchForm.findById("advSrchEndSeqNum").getValue();
			var tableNumber = advSrchForm.findById("advSrchTableNbr").getValue();

			var payManner = payTypeCombAdvSrch.getValue();
			var in_payManner;
			if (payManner == "全部") {
				in_payManner = 6;
			} else {
				in_payManner = payManner;
			}

			var tableType = tableTypeCombAdvSrch.getValue();
			var in_tableType;
			if (tableType == "全部") {
				in_tableType = 6;
			} else {
				in_tableType = tableType;
			}

			this.baseParams = {
				"pin" : pin,
				"dateBegin" : dateBegin,
				"dateEnd" : dateEnd,
				"amountBegin" : amountBegin,
				"amountEnd" : amountEnd,
				"seqNumBegin" : seqNumBegin,
				"seqNumEnd" : seqNumEnd,
				"tableNumber" : tableNumber,
				"payManner" : in_payManner,
				"tableType" : in_tableType,
				"isPaging" : true,
				"queryType" : "Advance"
			};
		}
	});				

	// --------------------------------------------------------------------------
	var centerPanel = new Ext.Panel({
		region : "center",
		layout : "fit",
		frame : true,
		items : [ {
			layout : "border",
			title : "<div style='font-size:20px;'>帐单信息<div>",
			items : [ billsQueryCondPanel, billsGrid ]
		} ],
		tbar : new Ext.Toolbar({
			height : 55,
			items : [
			kitchenStatBut, 
			{xtype:'tbtext',text:'&nbsp;&nbsp;&nbsp;'},
			deptStatBut,
			{xtype:'tbtext',text:'&nbsp;&nbsp;&nbsp;'},
			btnCancelledFood,
			{xtype:'tbtext',text:'&nbsp;&nbsp;&nbsp;'},
			shiftStatBut, 
			{xtype:'tbtext',text:'&nbsp;&nbsp;&nbsp;'},
			dailySettleStatBut,
			{xtype:'tbtext',text:'&nbsp;&nbsp;&nbsp;'},
			businessStatBut, 
			{xtype:'tbtext',text:'&nbsp;&nbsp;&nbsp;'},
			btnSalesSub,
			"->", 
			pushBackBut,
			{xtype:'tbtext',text:'&nbsp;&nbsp;&nbsp;'},
			logOutBut 
			]
		})
	});

	new Ext.Viewport({
		layout : "border",
		id : "viewport",
		items : [{
			region : "north",
			bodyStyle : "background-color:#DFE8F6;",
			html : "<h4 style='padding:10px;font-size:150%;float:left;'>无线点餐网页终端</h4><div id='optName' class='optName'></div>",
			height : 50,
			border : false,
			margins : '0 0 0 0'
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
});
