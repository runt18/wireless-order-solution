var orderStatBut = new Ext.ux.ImageButton({
	imgPath : '../../images/menuDishStatis.png',
	imgWidth : 50,
	imgHeight : 50,
	tooltip : '点菜统计',
	handler : function(btn) {
		if (!isPrompt) {
			isPrompt = true;
			menuStatWin.show();
		}
	}
});

var kitchenStatBut = new Ext.ux.ImageButton({
	imgPath : '../../images/kitchenStatis.png',
	imgWidth : 50,
	imgHeight : 50,
	tooltip : '分厨统计',
	handler : function(btn) {
		if (!isPrompt) {
			isPrompt = true;
			kitchenStatWin.show();
		}
	}
});

var deptStatBut = new Ext.ux.ImageButton({
	imgPath : '../../images/deptStatis.png',
	imgWidth : 50,
	imgHeight : 50,
	tooltip : '部门统计',
	handler : function(btn) {
		if (!isPrompt) {
			isPrompt = true;
			deptStatWin.show();
		}
	}
});

var regionStatBut = new Ext.ux.ImageButton({
	imgPath : '../../images/regionStatis.png',
	imgWidth : 50,
	imgHeight : 50,
	tooltip : '区域统计',
	handler : function(btn) {
		if (!isPrompt) {
			isPrompt = true;
			regionStatWin.show();
		}
	}
});

var discountStatBut = new Ext.ux.ImageButton({
	imgPath : '../../images/discountStatis.png',
	imgWidth : 50,
	imgHeight : 50,
	tooltip : '折扣统计',
	handler : function(btn) {
		if (!isPrompt) {
			isPrompt = true;
			discountStatWin.show();
		}
	}
});

var shiftStatBut = new Ext.ux.ImageButton({
	imgPath : '../../images/shiftStatis.png',
	imgWidth : 50,
	imgHeight : 50,
	tooltip : '交班统计',
	handler : function(btn) {
		if (!isPrompt) {
			isPrompt = true;
			shiftStatWin.show();
		}
	}
});

var btnSalesSub = new Ext.ux.ImageButton({
	imgPath : '../../images/HistorySalesSub.png',
	imgWidth : 50,
	imgHeight : 50,
	tooltip : '销售统计',
	handler : function(btn) {
		salesSub();
	}
});


// --
var pushBackBut = new Ext.ux.ImageButton({
	imgPath : '../../images/UserLogout.png',
	imgWidth : 50,
	imgHeight : 50,
	tooltip : '返回',
	handler : function(btn) {
		location.href = 'FrontBusinessProtal.html?restaurantID=' + restaurantID
				+ '&pin=' + pin;
	}
});

var logOutBut = new Ext.ux.ImageButton({
	imgPath : '../../images/ResLogout.png',
	imgWidth : 50,
	imgHeight : 50,
	tooltip : '登出',
	handler : function(btn) {
	}
});

//修改link
function billOptModifyHandler(rowindex) {
	// "51","100","2011-07-26 23:23:41","一般","现金","100.44","150.0","0","3","0","0.0","","","","0.0","1","1"
	var data = Ext.ux.getSelData(billsGrid);
	
	var tableNbr = data['tableID']; // billsData[rowindex][1];
	var category = data['category']; // billsData[rowindex][3];
	var orderID = data['id']; // billsData[rowindex][0];
	var personCount = data['customNum']; // billsData[rowindex][8];
	var payType = 0;//0:一般 1:会员  //billsData[rowindex][15];
	var serviceRate = data['serviceRate']; // billsData[rowindex][10];
	var minCost = data['minCost']; //billsData[rowindex][9];	
	if (category == '一般') {
		category = 1;
	} else if (category == '外卖') {
		category = 2;
	} else if (category == '并台') {
		category = 3;
	} else if (category == '拼台') {
		category = 4;
	}
	location.href = 'BillModify.html?pin=' + pin + '&restaurantID='
			+ restaurantID + '&category=' + category + '&tableNbr=' + tableNbr
			+ '&personCount=' + personCount + '&minCost=' + minCost + '&orderID=' + orderID
			+ '&payType=' + payType + '&serviceRate=' + serviceRate;
};

// 查看link
var viewBillGenPanel = new Ext.Panel({
	region : 'north',
	height : 145,
	frame : true,
	items : [ {
		border : false,
		contentEl : 'billView'
	} ]
});

var viewBillAddPanel = new Ext.Panel({
	region : 'south',
	height : 60,
	frame : true,
	items : [ {
		border : false,
		contentEl : 'billViewAddInfo'
	} ]
});

// 1，表格的数据store
var viewBillStore = new Ext.data.Store({
	proxy : new Ext.data.MemoryProxy(viewBillData),
	reader : new Ext.data.JsonReader(Ext.ux.readConfig, 
	    [ 
	      { name : 'foodName' }, 
	      { name : 'tastePref'},
	      { name : 'count' }, 
	      { name : 'discount' }, 	       
	      { name : 'acturalPrice'} 
	    ]
	)
});

// 2，栏位模型
var viewBillColumnModel = new Ext.grid.ColumnModel([
	new Ext.grid.RowNumberer(), {
		header : '菜名',
		sortable : true,
		dataIndex : 'foodName',
		width : 130
	}, {
		header : '口味',
		sortable : true,
		dataIndex : 'tastePref',
		width : 100
	}, {
		header : '数量',
		sortable : true,
		dataIndex : 'count',
		width : 50,
		align : 'right',
		renderer : Ext.ux.txtFormat.gridDou
	}, {
		header : '折扣',
		sortable : true,
		dataIndex : 'discount',
		width : 50,
		align : 'right',
		renderer : Ext.ux.txtFormat.gridDou
	}, {
		header : '金额（￥）',
		sortable : true,
		dataIndex : 'acturalPrice',
		width : 100,
		align : 'right',
		renderer : Ext.ux.txtFormat.gridDou
	} 
]);

// 3,表格
var viewBillGrid = new Ext.grid.GridPanel({
	title : '已点菜',
	border : false,
//	frame : true,
	ds : viewBillStore,
	cm : viewBillColumnModel
});

var viewBillDtlPanel = new Ext.Panel({
	region : 'center',
	layout : 'fit',
//	border : false,
	items : viewBillGrid
});

var viewBillWin = new Ext.Window({
	layout : 'fit',
	title : '查看账单',
	width : 500,
	height : 500,
	closeAction : 'hide',
	resizable : false,
	closable : false,
	modal : true,
	items : [ {
		layout : 'border',
		border : false,
		items : [ viewBillGenPanel, viewBillDtlPanel, viewBillAddPanel ]
	} ],
	buttons : [ {
		text : '打印',
		disabled : true,
		handler : function() {
			
		}
	}, {
		text : '确定',
		handler : function(){
			viewBillWin.hide();
		}
	} ],
	listeners : {
		show : function(thiz) {
			var data = Ext.ux.getSelData(billsGrid);
			var billID = data['id'];
			var tableType = data['category'];
			var tableNbr = data['tableID'];
			var personNbr = data['customNum'];
			var billDate = data['orderDateFormat'];
			var billPayType = data['payManner'];
			var PayMannaDescr = '一般';
			var billServiceRate = data['serviceRate'];
			var billWaiter = data['waiter'];
			var billForFree = data['giftPrice'];
			var billShouldPay = data['totalPrice'];
			var billAvtrualPay = data['acturalPrice'];
			var billDiscount = data['discountPrice'];
			var billErasePuota = data['erasePuotaPrice'];
			var billCancel = data['cancelPrice'];

			document.getElementById('billIDBV').innerHTML = billID;
			document.getElementById('billTypeBV').innerHTML = tableType;
			document.getElementById('tableNbrBV').innerHTML = tableNbr;
			document.getElementById('personNbrBV').innerHTML = personNbr;
			document.getElementById('billDateBV').innerHTML = billDate;
			document.getElementById('payTypeBV').innerHTML = PayMannaDescr;
			document.getElementById('payMannerBV').innerHTML = billPayType;
			document.getElementById('serviceRateBV').innerHTML = billServiceRate + '％';
			document.getElementById('waiterBV').innerHTML = billWaiter;
			document.getElementById('forFreeBV').innerHTML = '￥' + billForFree.toFixed(2);
			document.getElementById('shouldPayBV').innerHTML = '￥' + billShouldPay.toFixed(2);
			document.getElementById('actrualPayBV').innerHTML = '￥' + billAvtrualPay.toFixed(2);
			Ext.getDom('discountBV').innerHTML = '￥' + parseFloat(billDiscount).toFixed(2);
			Ext.getDom('erasePuotaPriceBV').innerHTML = '￥' + parseFloat(billErasePuota).toFixed(2);
			Ext.getDom('cancelPriceBV').innerHTML = '￥' + parseFloat(billCancel).toFixed(2);
			
			// 后台：["菜名",菜名编号,厨房编号,"口味",口味编号,数量,￥单价,是否特价,是否推荐,是否停售,是否赠送,折扣率,口味编号2,口味编号3,￥口味价钱,是否時價]
			Ext.Ajax.request({
				url : '../../QueryOrder.do',
				params : {
					'pin' : pin,
					'orderID' : billID,
					'queryType' : 'Today'
				},
				success : function(response, options) {
					var resultJSON = Ext.util.JSON.decode(response.responseText);
					if (resultJSON.success == true) {						
						viewBillData = resultJSON;
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
		hide : function(thiz) {
			viewBillData = null;
			viewBillStore.removeAll();
		}
	}
});

function billViewHandler() {
	viewBillWin.show();
	viewBillWin.center();
};

// 明細link
// 2，表格的数据store
// 前台： [日期,名称,单价,数量,折扣,口味,口味价钱,厨房,服务员,备注]
var billDetailStore = new Ext.data.Store({
	proxy : new Ext.data.HttpProxy({
		url : '../../QueryDetail.do'
	}),
	reader : new Ext.data.JsonReader({
		totalProperty : 'totalProperty',
		root : 'root'
	}, [ {
		name : 'order_date'
	}, {
		name : 'food_name'
	}, {
		name : 'unit_price'
	}, {
		name : 'amount'
	}, {
		name : 'discount'
	}, {
		name : 'taste_pref'
	}, {
		name : 'taste_price'
	}, {
		name : 'kitchen'
	}, {
		name : 'waiter'
	}, {
		name : 'comment'
	}, {
		name : 'isPaid'
	}, {
		name : 'isDiscount'
	}, {
		name : 'isGift'
	}, {
		name : 'isReturn'
	}, {
		name : 'message'
	} ])
});

// 3，栏位模型
var billDetailColumnModel = new Ext.grid.ColumnModel([
	new Ext.grid.RowNumberer(), {
		header : '日期',
		sortable : true,
		dataIndex : 'order_date',
		width : 130
	}, {
		header : '名称',
		sortable : true,
		dataIndex : 'food_name',
		width : 160
	}, {
		header : '单价',
		sortable : true,
		dataIndex : 'unit_price',
		align : 'right',
		width : 60,
		renderer : Ext.ux.txtFormat.gridDou
	}, {
		header : '数量',
		sortable : true,
		dataIndex : 'amount',
		align : 'right',
		width : 60
	}, {
		header : '折扣',
		sortable : true,
		dataIndex : 'discount',
		align : 'right',
		width : 60,
		renderer : Ext.ux.txtFormat.gridDou
	}, {
		header : '口味',
		sortable : true,
		dataIndex : 'taste_pref',
		width : 120
	}, {
		header : '口味价钱',
		sortable : true,
		dataIndex : 'taste_price',
		align : 'right',
		width : 60,
		renderer : Ext.ux.txtFormat.gridDou
	}, {
		header : '厨房',
		sortable : true,
		dataIndex : 'kitchen',
		width : 60
	}, {
		header : '已结账',
		sortable : true,
		dataIndex : '',
		width : 60,
		renderer : function(v){
			return eval(v == 0) ? '否' : '是';
		}
	}, {
		header : '服务员',
		sortable : true,
		dataIndex : 'waiter',
		width : 80
	}, {
		header : '备注',
		sortable : true,
		dataIndex : 'comment',
		width : 100
	} 
]);

// 4，表格
var billDetailGrid = new Ext.grid.GridPanel({
	ds : billDetailStore,
	cm : billDetailColumnModel,
	border : false,
	sm : new Ext.grid.RowSelectionModel({
		singleSelect : true
	}),
	viewConfig : {
		forceFit : true
	},
	bbar : createPagingToolbar(billDetailpageRecordCount, billDetailStore),
	autoScroll : true,
	loadMask : {
		msg : '数据加载中，请稍等...'
	}
});

// 为store配置beforeload监听器
billDetailGrid.getStore().on('beforeload', function() {
	this.baseParams = {
		'pin' : pin,
		'orderID' : Ext.ux.getSelData(billsGrid)['id'],
		'queryType' : 'Today'
	};

});

// 为store配置load监听器(即load完后动作)
billDetailGrid.getStore().on('load', function() {
	var msg = this.getAt(0).get('message');
	if (msg != 'normal') {
		Ext.MessageBox.show({
			msg : msg,
			width : 110,
			buttons : Ext.MessageBox.OK
		});
		this.removeAll();
	} else {
		if (billDetailGrid.getStore().getTotalCount() != 0) {
			var inputValue = Ext.getCmp(searchAdditionFilter).inputValue;
			var attribute = '';
			if(inputValue == 1){
				attribute = 'isPaid';
				detailExplain = '反结账';
			}else if(inputValue == 2){
				attribute = 'idDiscount';
				detailExplain = '打折';
			}else if(inputValue == 3){
				attribute = 'isGift';
				detailExplain = '赠送';
			}else if(inputValue == 4){
				attribute = 'isReturn';
				detailExplain = '退菜';
			}
			if(attribute != ''){
				for ( var i = 0; i < billDetailGrid.getStore().getCount(); i++) {
					var record = billDetailGrid.getStore().getAt(i);
					if (record.get(attribute) == true) {
						billDetailGrid.getView().getRow(i).style.backgroundColor = '#DDDCCC';
					}
				}
			}
		}
	}
});

// 彈出框
var billDetailWin = new Ext.Window({
	layout : 'fit',
	width : 1100,
	height : 370,
	closeAction : 'hide',
	closable : false,
	resizable : true,
	modal : true,
	items : billDetailGrid,
	bbar : ['->', {
		text : '关闭',
		iconCls : 'btn_close',
		handler : function(){
			billDetailWin.hide();
		}
	}],
	listeners : {
		'show' : function(thiz) {
			billDetailStore.reload({
				params : {
					start : 0,
					limit : billDetailpageRecordCount
				}
			});
		}
	}
});

// 查看明细
function billDetailHandler(orderID) {
	billDetailWin.show();
	billDetailWin.setTitle('账单号: ' + orderID);
	billDetailWin.center();
};

// 打印link
function printBillFunc(orderID) {
	Ext.Ajax.request({
		url : '../../PrintOrder.do',
		params : {
			'pin' : pin,
			'orderID' : orderID,
			'printReceipt' : 1
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
};



// ------------------ north ------------------------
var filterTypeComb = new Ext.form.ComboBox({
	fieldLabel : '过滤',
	forceSelection : true,
	width : 100,
	value : '全部',
	id : 'filter',
	store : new Ext.data.SimpleStore({
		fields : [ 'value', 'text' ],
		data :  [[ '0', '全部' ], [ '1', '帐单号' ], [ '2', '流水号' ], [ '3', '台号' ], [ '4', '时间' ], [ '5', '类型' ], [ '6', '结帐方式' ], [ '7', '金额' ], [ '8', '实收' ]]
	}),
	valueField : 'value',
	displayField : 'text',
	typeAhead : true,
	mode : 'local',
	triggerAction : 'all',
	selectOnFocus : true,
	readOnly : true,
	allowBlank : false,
	listeners : {
		select : function(combo, record, index) {
			searchType = combo.getValue();
			searchValue = '';
			
			var comboOperator = Ext.getCmp('comboOperator');
			var comboTableType = Ext.getCmp('comboTableType');
			var comboPayType = Ext.getCmp('comboPayType');
			var timeCondition = Ext.getCmp('timeCondition');
			var numberSearchValue = Ext.getCmp('numberSearchValue');
			
			//
			comboOperator.setVisible(false);
			comboTableType.setVisible(false);
			comboPayType.setVisible(false);
			timeCondition.setVisible(false);
			numberSearchValue.setVisible(false);
			
			comboOperator.setVisible(true);
			comboOperator.setValue(1);
			
			if (index == 0) {
				// 全部
				comboOperator.setVisible(false);
				searchValue = '';
			} else if (index == 1) {
				// 帐单号
				numberSearchValue.setVisible(true);
				numberSearchValue.setValue();
				searchValue = numberSearchValue.getId();
			} else if (index == 2) {
				// 流水号
				numberSearchValue.setVisible(true);
				numberSearchValue.setValue();
				searchValue = numberSearchValue.getId();
			} else if (index == 3) {
				// 台号
				numberSearchValue.setVisible(true);
				numberSearchValue.setValue();
				searchValue = numberSearchValue.getId();
			} else if (index == 4) {
				// 时间
				timeCondition.setVisible(true);
				timeCondition.setValue(new Date().format('H:i:s'));
				searchValue = timeCondition.getId();
			} else if (index == 5) {
				// 类型
				comboOperator.setVisible(false);
				comboTableType.setVisible(true);
				comboTableType.setValue(1);
				searchValue = comboTableType.getId();
			} else if (index == 6) {
				// 结帐方式
				comboOperator.setVisible(false);
				comboPayType.setVisible(true);
				comboPayType.setValue(1);
				searchValue = comboPayType.getId();
			} else if (index == 7) {
				// 金额
				numberSearchValue.setVisible(true);
				numberSearchValue.setValue();
				searchValue = numberSearchValue.getId();
			} else if (index == 8) {
				// 实收
				numberSearchValue.setVisible(true);
				numberSearchValue.setValue();
				searchValue = numberSearchValue.getId();
			}
			
		}
	}
});

var operatorComb = new Ext.form.ComboBox({
	hideLabel : true,
	forceSelection : true,
	width : 100,
	value : '等于',
	id : 'operatorComb',
	store : new Ext.data.SimpleStore({
		fields : [ 'value', 'text' ],
		data : [[ '1', '等于' ], [ '2', '大于等于' ], [ '3', '小于等于' ]]
	}),
	valueField : 'value',
	displayField : 'text',
	typeAhead : true,
	mode : 'local',
	triggerAction : 'all',
	selectOnFocus : true,
	allowBlank : false
});

// 高級搜索彈出框
//var tableTypeCombAdvSrch = new Ext.form.ComboBox({
//	// hideLabel : true,
//	forceSelection : true,
//	fieldLabel : '类型',
//	width : 100,
//	value : '全部',
//	id : 'tableTypeCombAdvSrch',
//	store : new Ext.data.SimpleStore({
//		fields : [ 'value', 'text' ],
//		data : [[6, '全部'], [1, '一般'], [2, '外卖'], [3, '并台'], [4, '拼台']]
//	}),
//	valueField : 'value',
//	displayField : 'text',
//	typeAhead : true,
//	mode : 'local',
//	triggerAction : 'all',
//	selectOnFocus : true,
//	allowBlank : false
//});

//var payTypeCombAdvSrch = new Ext.form.ComboBox({
//	forceSelection : true,
//	width : 100,
//	value : '全部',
//	id : 'payTypeCombAdvSrch',
//	store : new Ext.data.SimpleStore({
//		fields : [ 'value', 'text' ],
//		data : [ [ '6', '全部' ], [ '1', '现金' ], [ '2', '刷卡' ], [ '3', '会员卡' ], [ '4', '签单' ], [ '5', '挂账' ] ]
//	}),
//	valueField : 'value',
//	displayField : 'text',
//	typeAhead : true,
//	mode : 'local',
//	triggerAction : 'all',
//	selectOnFocus : true,
//	allowBlank : false
//});

//advSrchForm = new Ext.form.FormPanel({
//	frame : true,
//	border : false,
//	layout : 'fit',
//	items : [ {
//		layout : 'column',
//		autoHeight : true, // important!!
//		autoWidth : true,
//		border : false,
//		items : [ {
//			layout : 'form',
//			border : false,
//			labelSeparator : '：',
//			labelWidth : 40,
//			columnWidth : .50,
//			items : [ {
//				xtype : 'timefield',
//				fieldLabel : '时间',
//				format : 'H:i:s',
//				width : 100,
//				id : 'advSrchStartTime'
//			} ]
//		}, {
//			layout : 'form',
//			border : false,
//			labelWidth : 20,
//			columnWidth : .50,
//			labelSeparator : '',
//			items : [ {
//				xtype : 'timefield',
//				fieldLabel : '至',
//				format : 'H:i:s',
//				width : 100,
//				id : 'advSrchEndTime'
//			} ]
//		}, {
//			layout : 'form',
//			border : false,
//			labelSeparator : '：',
//			labelWidth : 40,
//			columnWidth : .50,
//			items : [ {
//				xtype : 'numberfield',
//				fieldLabel : '金額',
//				width : 100,
//				id : 'advSrchStartAmt'
//			} ]
//		}, {
//			layout : 'form',
//			border : false,
//			labelWidth : 20,
//			columnWidth : .50,
//			labelSeparator : '',
//			items : [ {
//				xtype : 'numberfield',
//				fieldLabel : '至',
//				width : 100,
//				id : 'advSrchEndAmt'
//			} ]
//		}, {
//			layout : 'form',
//			border : false,
//			labelSeparator : '：',
//			labelWidth : 40,
//			columnWidth : .50,
//			items : [ {
//				xtype : 'textfield',
//				fieldLabel : '台号',
//				width : 100,
//				id : 'advSrchTableNbr'
//			} ]
//		}, {
//			layout : 'form',
//			border : false,
//			labelSeparator : '：',
//			labelWidth : 60,
//			columnWidth : .50,
//			items : payTypeCombAdvSrch
//		}, {
//			layout : 'form',
//			border : false,
//			labelSeparator : '：',
//			labelWidth : 40,
//			columnWidth : .50,
//			items : tableTypeCombAdvSrch
//		} ]
//	} ]
//});

//advSrchWin = new Ext.Window({
//	layout : 'fit',
//	title : '高级搜索',
//	width : 370,
//	height : 190,
//	closeAction : 'hide',
//	resizable : false,
//	items : advSrchForm,
//	buttons : [{
//		text : '搜索',
//		handler : function(){
//			advSrchWin.hide();
//			
//			// bill adv srch 1, get parameters
//			var timeBegin = advSrchForm.findById('advSrchStartTime').getValue();
//			var endBegin = advSrchForm.findById('advSrchEndTime').getValue();
//			var amountBegin = advSrchForm.findById('advSrchStartAmt').getValue();
//			var amountEnd = advSrchForm.findById('advSrchEndAmt').getValue();
//			var tableNumber = advSrchForm.findById('advSrchTableNbr').getValue();
//
//			var payManner = payTypeCombAdvSrch.getValue();
//			var in_payManner;
//			if (payManner == '全部') {
//				in_payManner = 6;
//			} else {
//				in_payManner = payManner;
//			}
//			
//			var tableType = tableTypeCombAdvSrch.getValue();
//			var in_tableType;
//			if (tableType == '全部') {
//				in_tableType = 6;
//			} else {
//				in_tableType = tableType;
//			}
//			
//			// 2, do the search
//			Ext.Ajax.request({
//				url : '../../QueryTodayAdv.do',
//				params : {
//					'pin' : pin,
//					'timeBegin' : timeBegin,
//					'timeEnd' : endBegin,
//					'amountBegin' : amountBegin,
//					'amountEnd' : amountEnd,
//					'tableNumber' : tableNumber,
//					'payManner' : in_payManner,
//					'tableType' : in_tableType
//				},
//				success : function(response, options) {
//					var resultJSON = Ext.decode(response.responseText);
//					if (resultJSON.success == true) {
//						var josnData = resultJSON.data;
//						if (josnData != '') {
//							var billList = josnData.split('，');
//							billsData.length = 0;
//							for ( var i = 0; i < billList.length; i++) {
//								var billInfo = billList[i].substr(1, billList[i].length - 2).split(',');								
//								// 格式：['账单号','台号','日期','类型','结帐方式','金额','实收','台号2','就餐人数','最低消','服务费率','会员编号','会员姓名','账单备注','赠券金额','结帐类型','折扣类型','服务员']
//								// 后台格式：['账单号','台号','日期','类型','结帐方式','金额','实收','台号2','就餐人数','最低消','服务费率','会员编号','会员姓名','账单备注','赠券金额','结帐类型','折扣类型','服务员']
//								billsData.push([
//								    billInfo[0].substr(1, billInfo[0].length - 2),// 账单号
//									billInfo[1].substr(1, billInfo[1].length - 2),// 台号
//									billInfo[2].substr(1, billInfo[2].length - 2),// 日期
//									billInfo[3].substr(1, billInfo[3].length - 2),// 类型
//									billInfo[4].substr(1, billInfo[4].length - 2), // 结帐方式
//									billInfo[5].substr(1, billInfo[5].length - 2), // 金额
//									billInfo[6].substr(1, billInfo[6].length - 2), // 实收
//									billInfo[7].substr(1, billInfo[7].length - 2), // 台号2
//									billInfo[8].substr(1, billInfo[8].length - 2), // 就餐人数
//									billInfo[9].substr(1, billInfo[9].length - 2), // 最低消
//									billInfo[10].substr(1, billInfo[10].length - 2), // 服务费率
//									billInfo[11].substr(1, billInfo[11].length - 2), // 会员编号
//									billInfo[12].substr(1, billInfo[12].length - 2), // 会员姓名
//									billInfo[13].substr(1, billInfo[13].length - 2), // 账单备注
//									billInfo[14].substr(1, billInfo[14].length - 2), // 赠券金额
//									billInfo[15].substr(1, billInfo[15].length - 2), // 结帐类型
//									billInfo[16].substr(1, billInfo[16].length - 2), // 折扣类型
//									billInfo[17].substr(1, billInfo[17].length - 2), // 服务员
//									billInfo[18], // 是否反結帳
//									billInfo[19], // 是否折扣
//									billInfo[20], // 是否赠送
//									billInfo[21], // 是否退菜
//									billInfo[22].substr(1, billInfo[22].length - 2) // 流水号
//								]);
//							}
//							
//							// sum the prices
//							var sumShouldPay = 0;
//							var sumActualPay = 0;
//							for ( var i = 0; i < billsData.length; i++) {
//								sumShouldPay = sumShouldPay + parseFloat(billsData[i][5]);
//								sumActualPay = sumActualPay + parseFloat(billsData[i][6]);
//							}
//							document.getElementById('shouldPaySum').innerHTML = sumShouldPay.toFixed(2);
//							document.getElementById('actualPaySum').innerHTML = sumActualPay.toFixed(2);
//						} else {
//							billsData.length = 0;
//						}
//						billsStore.reload();
//					}
//				},
//				failure : function(response, options) {
//					
//				}
//			});
//		}
//	}, {
//		text : '取消',
//		handler : function() {
//			advSrchWin.hide();
//		}
//	} ],
//	listeners : {
//		'show' : function() {
//			tableTypeCombAdvSrch.setValue('全部');
//			payTypeCombAdvSrch.setValue('全部');
//		}
//	}
//});


// center
function billOpt(value, cellmeta, record, rowIndex, columnIndex, store) {
	return '<a href=\'javascript:billOptModifyHandler(' + rowIndex + ')\'>修改</a>'
			+ '&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;'
			+ '<a href=\'javascript:billViewHandler()\'>查看</a>'
			+ '&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;'
			+ '<a href=\'javascript:billDetailHandler(' + record.get('id') + ')\'>明细</a>'
			+ '&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;'
			+ '<a href=\'javascript:printBillFunc(' + record.get('id') + ')\'>补打</a>';
};

var billsStore = new Ext.data.Store({
	proxy : new Ext.data.MemoryProxy(billsData),
	reader : new Ext.data.JsonReader(Ext.ux.readConfig, [{
		name : 'id'
	}, {
		name : 'seqID'
	}, {
		name : 'tableID'
	}, {
		name : 'orderDate'
	}, {
		name : 'orderDateFormat'
	}, {
		name : 'payMannerFormat'
	}, {
		name : 'payManner'
	}, {
		name : 'categoryFormat'
	}, {
		name : 'category'
	}, {
		name : 'totalPrice'
	}, {
		name : 'acturalPrice'
	}, {
		name : 'status'
	}, {
		name : 'serviceRate'
	}, {
		name : 'customNum'
	}, {
		name : 'waiter'
	}, {
		name : 'minCost'
	}, {
		name : 'giftPrice'
	}, {
		name : 'discountPrice'
	}, {
		name : 'cancelPrice'
	}, {
		name : 'erasePuotaPrice'
	}])
});

// 2，栏位模型
var billsColumnModel = new Ext.grid.ColumnModel([ new Ext.grid.RowNumberer(), {
	header : '帐单号',
	sortable : true,
	dataIndex : 'id',
	width : 100
}, {
	header : '流水号',
	sortable : true,
	dataIndex : 'seqID',
	width : 100
}, {
	header : '台号',
	sortable : true,
	dataIndex : 'tableID',
	width : 100
}, {
	header : '日期',
	sortable : true,
	dataIndex : 'orderDateFormat',
	width : 150
}, {
	header : '类型',
	sortable : true,
	dataIndex : 'categoryFormat',
	width : 100
}, {
	header : '结帐方式',
	sortable : true,
	dataIndex : 'payMannerFormat',
	width : 100
}, {
	header : '金额（￥）',
	sortable : true,
	dataIndex : 'totalPrice',
	width : 120,
	align : 'right',
	renderer : Ext.ux.txtFormat.gridDou
}, {
	header : '实收（￥）',
	sortable : true,
	dataIndex : 'acturalPrice',
	width : 120,
	align : 'right',
	renderer : Ext.ux.txtFormat.gridDou
}, {
	header : '状态',
	sortable : true,
	dataIndex : 'status',
	width : 80,
	align : 'center',
	renderer : function(v){
		if(eval(v == 1)){
			return '已结账';
		}else if(eval(v == 2)){
			return '<font color="#FF0000">反结账</font>';
		}else{
			return '未结账';
		}
	}
}, {
	header : '操作',
	dataIndex : 'billOpt',
	align : 'center',
	width : 270,
	renderer : billOpt
} ]);

var billsGrid;
Ext.onReady(function(){
	
	Ext.lib.Ajax.defaultPostHeader += '; charset=utf-8';
	Ext.QuickTips.init();
	
	var billsGridTbar = new Ext.Toolbar({
		height : 26,
		items : [{
			xtype : 'tbtext',
			text : '过滤:'
		}, 
		{ xtype:'tbtext', text:'&nbsp;&nbsp;'},
		filterTypeComb,
		{ xtype:'tbtext', text:'&nbsp;&nbsp;'},
		{
			xtype : 'combo',
			forceSelection : true,
			width : 100,
			value : '1',
			id : 'comboOperator',
			hidden : true,
			store : new Ext.data.SimpleStore({
				fields : [ 'value', 'text' ],
				data : [[1, '等于'], [2, '大于等于' ], [3, '小于等于']]
			}),
			valueField : 'value',
			displayField : 'text',
			typeAhead : true,
			mode : 'local',
			triggerAction : 'all',
			selectOnFocus : true,
			allowBlank : false,
			readOnly : true,
			listeners : {
				select : function(combo, record, index){
					searchOperator = combo.getId();
				}
			}
		}, {
			xtype : 'combo',
			forceSelection : true,
			width : 120,
			value : 1,
			id : 'comboTableType',
			hidden : true,
			store : new Ext.data.SimpleStore({
				fields : [ 'value', 'text' ],
				data : [[ 1, '一般' ], [2, '外卖' ], [3, '并台' ], [4, '拼台' ]]
			}),
			valueField : 'value',
			displayField : 'text',
			typeAhead : true,
			mode : 'local',
			triggerAction : 'all',
			selectOnFocus : true,
			allowBlank : false,
			readOnly : true
		}, {
			xtype : 'combo',
			forceSelection : true,
			width : 120,
			value : 1,
			id : 'comboPayType',
			hidden : true,
			store : new Ext.data.SimpleStore({
				fields : [ 'value', 'text' ],
				data : [[1, '现金' ], [2, '刷卡' ], [3, '会员卡' ], [4, '签单' ], [5, '挂账' ]]
			}),
			valueField : 'value',
			displayField : 'text',
			typeAhead : true,
			mode : 'local',
			triggerAction : 'all',
			selectOnFocus : true,
			allowBlank : false,
			readOnly : true
		}, {
			xtype : 'timefield',
			id : 'timeCondition',
			hidden : true,
			allowBlank : false,
			format : 'H:i:s',
			value : new Date().format('H:i:s'),
			width : 120
		}, {
			xtype : 'numberfield',
			id : 'numberSearchValue',
			hidden : true,
			style : 'text-align: left;',
			width : 130
		}, 
		{ xtype:'tbtext', text:'&nbsp;&nbsp;'}, {
			xtype : 'radio',
			checked : true,
			boxLabel : '全部',
			name : 'conditionRadio',
			inputValue : 0,
			listeners : {
				check : function(e){
					if(e.getValue())
						searchAdditionFilter = e.getId();
				}
			}
		}, { xtype:'tbtext', text:'&nbsp;&nbsp;'}, {
			xtype : 'radio',
			name : 'conditionRadio',
			boxLabel : '反结帐',
			inputValue : 1,
			listeners : {
				check : function(e){
					if(e.getValue())
						searchAdditionFilter = e.getId();
				}
			}
		}, { xtype:'tbtext', text:'&nbsp;&nbsp;'}, {
			xtype : 'radio',
			name : 'conditionRadio',
			boxLabel : '折扣',
			inputValue : 2,
			listeners : {
				check : function(e){
					if(e.getValue())
						searchAdditionFilter = e.getId();
				}
			}
		}, { xtype:'tbtext', text:'&nbsp;&nbsp;'}, {
			xtype : 'radio',
			name : 'conditionRadio',
			boxLabel : '赠送',
			inputValue : 3,
			listeners : {
				check : function(e){
					if(e.getValue())
						searchAdditionFilter = e.getId();
				}
			}
		}, { xtype:'tbtext', text:'&nbsp;&nbsp;'}, {
			xtype : 'radio',
			name : 'conditionRadio',
			boxLabel : '退菜',
			inputValue : 4,
			listeners : {
				check : function(e){
					if(e.getValue())
						searchAdditionFilter = e.getId();
				}
			}
		}, { xtype:'tbtext', text:'&nbsp;&nbsp;'}, {
			xtype : 'radio',
			name : 'conditionRadio',
			boxLabel : '抹数',
			inputValue : 5,
			listeners : {
				check : function(e){
					if(e.getValue())
						searchAdditionFilter = e.getId();
				}
			}
		},
		'->',
		{
			text : '搜索',
			iconCls : 'btn_search',
			handler : function(e){
				billQueryHandler();
			}
		}, {
			text : '高级搜索',
			hidden : true,
			iconCls : 'btn_search',
			handler : function(e){
				advSrchWin.show();
			}
		}]
	});
	
	// 3,表格
	billsGrid = new Ext.grid.GridPanel({
		region : 'center',
		frame : true,
		ds : billsStore,
		cm : billsColumnModel,
		loadMask : {msg:'数据加载中, 请稍等......'},
		viewConfig : {
			forceFit : true
		},
		sm : new Ext.grid.RowSelectionModel({
			singleSelect : true
		}),
		tbar : billsGridTbar,
		listeners : {
			rowclick : function(thiz, rowIndex, e) {
				currRowIndex = rowIndex;
			}
		}
	});
	
	var billSum = new Ext.Panel({
		region : 'south',
		frame : true,
		border : false,
		height : 40,
		contentEl : 'billSum'
	});
	
	var centerPanel = new Ext.Panel({
		title : '当日账单管理',
		region : 'center',
		layout : 'fit',
		frame : true,
		items : [{
			layout : 'border',
			items : [billsGrid, billSum ]
		}],
		tbar : new Ext.Toolbar({
			height : 55,
			items : [
			    orderStatBut, 
			    {xtype:'tbtext',text:'&nbsp;&nbsp;&nbsp;'},
			    kitchenStatBut, 
				{xtype:'tbtext',text:'&nbsp;&nbsp;&nbsp;'},
				deptStatBut,
				{xtype:'tbtext',text:'&nbsp;&nbsp;&nbsp;'},
				regionStatBut,
				{xtype:'tbtext',text:'&nbsp;&nbsp;&nbsp;'},
				shiftStatBut,
				{xtype:'tbtext',text:'&nbsp;&nbsp;&nbsp;'},
				btnSalesSub,
				'->', 
				pushBackBut, 
				{xtype:'tbtext',text:'&nbsp;&nbsp;&nbsp;'}, 
				logOutBut 
			]
		})
	});

	new Ext.Viewport({
		layout : 'border',
		id : 'viewport',
		items : [{
			region : 'north',
			bodyStyle : 'background-color:#DFE8F6;',
			html : '<h4 style="padding:10px;font-size:150%;float:left;">无线点餐网页终端</h4><div id="optName" class="optName"></div>',
			height : 50,
			border : false,
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
		} ]
	});
});
