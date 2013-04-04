var orderStatBut = new Ext.ux.ImageButton({
	imgPath : "../../images/menuDishStatis.png",
	imgWidth : 50,
	imgHeight : 50,
	tooltip : "点菜统计",
	handler : function(btn) {
		salesSub();
		salesSubWinTabPanel.setActiveTab(orderFoodStatPanel);
	}
});

var kitchenStatBut = new Ext.ux.ImageButton({
	imgPath : '../../images/kitchenStatis.png',
	imgWidth : 50,
	imgHeight : 50,
	tooltip : '分厨统计',
	handler : function(btn) {
		salesSub();
		salesSubWinTabPanel.setActiveTab(kitchenStatPanel);
	}
});

var deptStatBut = new Ext.ux.ImageButton({
	imgPath : '../../images/deptStatis.png',
	imgWidth : 50,
	imgHeight : 50,
	tooltip : '部门统计',
	handler : function(btn) {
		salesSub();
		salesSubWinTabPanel.setActiveTab(deptStatPanel);
	}
});

var regionStatBut = new Ext.ux.ImageButton({
	imgPath : '../../images/regionStatis.png',
	imgWidth : 50,
	imgHeight : 50,
	tooltip : '区域统计',
	handler : function(btn) {
//		if (!isPrompt) {
//			isPrompt = true;
//			regionStatWin.show();
//		}
	}
});

var discountStatBut = new Ext.ux.ImageButton({
	imgPath : '../../images/discountStatis.png',
	imgWidth : 50,
	imgHeight : 50,
	tooltip : '折扣统计',
	handler : function(btn) {
//		if (!isPrompt) {
//			isPrompt = true;
//			discountStatWin.show();
//		}
		
	}
});

var btnDutyRangeSub = new Ext.ux.ImageButton({
	imgPath : '../../images/shiftStatis.png',
	imgWidth : 50,
	imgHeight : 50,
	tooltip : '交班记录',
	handler : function(btn) {
//		if (!isPrompt) {
//			isPrompt = true;
//			shiftStatWin.show();
//		}
		dutyRangeSub();
	}
});

var btnSalesSub = new Ext.ux.ImageButton({
	imgPath : '../../images/salesStat.png',
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
	var data = Ext.ux.getSelData(billsGrid);
	if(eval(data['category'] == 4)){
		Ext.example.msg('提示', '团体餐桌暂不允许反结账.');
		return;
	}
	var tableNbr = data['tableID']; 
	var category = data['category']; 
	var orderID = data['id'];
	var personCount = data['customNum'];
	var payType = 0;
	var serviceRate = data['serviceRate']; 
	var minCost = data['minCost'];
	if (category == '一般') {
		category = 1;
	} else if (category == '外卖') {
		category = 2;
	} else if (category == '拆台') {
		category = 3;
	} else if (category == '并台') {
		category = 4;
	}
	location.href = 'BillModify.html?pin=' + pin + '&restaurantID='
			+ restaurantID + '&category=' + category + '&tableNbr=' + tableNbr
			+ '&personCount=' + personCount + '&minCost=' + minCost + '&orderID=' + orderID
			+ '&payType=' + payType + '&serviceRate=' + serviceRate;
};

// 1，表格的数据store
var viewBillStore = new Ext.data.Store({
	proxy : new Ext.data.MemoryProxy(viewBillData),
	reader : new Ext.data.JsonReader(Ext.ux.readConfig, 
	    [ 
	      { name : 'foodName' }, 
	      { name : 'tastePref'},
	      { name : 'count' }, 
	      { name : 'discount' }, 	       
	      { name : 'totalPrice'} 
	    ]
	)
});

// 2，栏位模型
var viewBillColumnModel = new Ext.grid.ColumnModel([
	new Ext.grid.RowNumberer(), {
		header : '菜名',
		dataIndex : 'foodName',
		width : 130
	}, {
		header : '口味',
		dataIndex : 'tastePref',
		width : 100
	}, {
		header : '数量',
		dataIndex : 'count',
		width : 50,
		align : 'right',
		renderer : Ext.ux.txtFormat.gridDou
	}, {
		header : '折扣',
		dataIndex : 'discount',
		width : 50,
		align : 'right',
		renderer : Ext.ux.txtFormat.gridDou
	}, {
		header : '金额',
		dataIndex : 'totalPrice',
		width : 100,
		align : 'right',
		renderer : Ext.ux.txtFormat.gridDou
	} 
]);

var viewBillGrid = new Ext.grid.GridPanel({
	title : '已点菜',
//	border : false,
	frame : true,
	ds : viewBillStore,
	cm : viewBillColumnModel
});


var viewBillGenPanel = new Ext.Panel({
	region : 'north',
	height : 145,
	frame : true,
	items : [ {
		border : false,
		contentEl : 'billView'
	} ]
});

var viewBillDtlPanel = new Ext.Panel({
	region : 'center',
	layout : 'fit',
	border : false,
	items : viewBillGrid
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
	bbar : ['->', {
		text : '关闭',
		iconCls : 'btn_close',
		handler : function(){
			viewBillWin.hide();
		}
	} ],
	keys : [{
		key : Ext.EventObject.ESC,
		scope : this,
		fn : function(){
			viewBillWin.hide();
		}
	}],
	listeners : {
		show : function(thiz) {
			var data = Ext.ux.getSelData(billsGrid);
			var billID = data['id'];
			var tableNbr = data['tableID'];
			var personNbr = data['customNum'];
			var billDate = data['orderDateFormat'];
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
			document.getElementById('billTypeBV').innerHTML = data.categoryFormat;
			document.getElementById('tableNbrBV').innerHTML = tableNbr;
			document.getElementById('personNbrBV').innerHTML = personNbr;
			document.getElementById('billDateBV').innerHTML = billDate;
			document.getElementById('payTypeBV').innerHTML = PayMannaDescr;
			document.getElementById('payMannerBV').innerHTML = data.payMannerFormat;
			document.getElementById('serviceRateBV').innerHTML = billServiceRate + '％';
			document.getElementById('waiterBV').innerHTML = billWaiter;
			document.getElementById('forFreeBV').innerHTML = '￥' + billForFree.toFixed(2);
			document.getElementById('shouldPayBV').innerHTML = '￥' + billShouldPay.toFixed(2);
			document.getElementById('actrualPayBV').innerHTML = '￥' + billAvtrualPay.toFixed(2);
			document.getElementById('discountBV').innerHTML = '￥' + parseFloat(billDiscount).toFixed(2);
			document.getElementById('erasePuotaPriceBV').innerHTML = '￥' + parseFloat(billErasePuota).toFixed(2);
			document.getElementById('cancelPriceBV').innerHTML = '￥' + parseFloat(billCancel).toFixed(2);
			
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

function detailIsPaidRenderer(v){
	return eval(v) ? '是' : '否';
}

function detailGridLoadListeners(_g){
	if(_g == null){
		return false;
	}
	var mg = null;
	if(typeof _g == 'object'){
		mg = _g;
	}else if(typeof _g == 'string'){
		mg = Ext.getCmp(_g);
	}
	if (mg.getStore().getCount() != 0) {
		var inputValue = Ext.getCmp(searchAdditionFilter).inputValue;
		var attribute = '';
		if(inputValue == 1){
			attribute = 'isPaid';
			detailExplain = '反结账';
		}else if(inputValue == 2){
			attribute = 'isDiscount';
			detailExplain = '打折';
		}else if(inputValue == 3){
			attribute = 'isGift';
			detailExplain = '赠送';
		}else if(inputValue == 4){
			attribute = 'isReturn';
			detailExplain = '退菜';
		}
		if(attribute != ''){
			for ( var i = 0; i < mg.getStore().getCount(); i++) {
				var record = mg.getStore().getAt(i);
				if (record.get(attribute) == true) {
					mg.getView().getRow(i).style.backgroundColor = '#DDDCCC';
				}
			}
		}
	}	
}

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
	}, {
		name : 'cancelReason'
	}]),
	listeners : {
		beforeload : function(thiz){
			thiz.baseParams = {
				'pin' : pin,
				'orderID' : Ext.ux.getSelData(billsGrid)['id'],
				'queryType' : 'Today'
			};
		},
		load : function(thiz, records, options){
			detailGridLoadListeners(billDetailGrid);
		}
	}
});

var billDetailColumnModel = new Ext.grid.ColumnModel([
	new Ext.grid.RowNumberer(), {
		header : '日期',
		dataIndex : 'order_date',
		width : 110
	}, {
		header : '名称',
		dataIndex : 'food_name',
		width : 130
	}, {
		header : '单价',
		dataIndex : 'unit_price',
		align : 'right',
		width : 60,
		renderer : Ext.ux.txtFormat.gridDou
	}, {
		header : '数量',
		dataIndex : 'amount',
		align : 'right',
		width : 60
	}, {
		header : '折扣',
		dataIndex : 'discount',
		align : 'right',
		width : 60,
		renderer : Ext.ux.txtFormat.gridDou
	}, {
		header : '口味',
		dataIndex : 'taste_pref'
	}, {
		header : '口味价钱',
		dataIndex : 'taste_price',
		align : 'right',
		width : 60,
		renderer : Ext.ux.txtFormat.gridDou
	}, {
		header : '厨房',
		dataIndex : 'kitchen',
		width : 60
	}, {
		header : '反结账',
		dataIndex : 'isPaid',
		width : 60,
		align : 'center',
		renderer : detailIsPaidRenderer
	}, {
		header : '服务员',
		dataIndex : 'waiter',
		width : 60
	}, {
		header : '退菜原因',
		dataIndex : 'cancelReason'
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
	bbar : createPagingBar(billDetailpageRecordCount, billDetailStore),
	autoScroll : true,
	loadMask : {
		msg : '数据加载中，请稍等...'
	}
});

var billgodtpStatus = false;
var billGroupOrderDetailTabPanel = new Ext.TabPanel({
	activeTab : 0,
	border : false,
	enableTabScroll : true,
	listeners : {
		tabchange : function(thiz, stab){
			if(billgodtpStatus && thiz.getActiveTab().getId() == stab.getId()){
				stab.getStore().load({
					params : {
						start : 0,
						limit : billDetailpageRecordCount
					}
				});				
			}
		}
	}
});

var billDetailWin = new Ext.Window({
	layout : 'fit',
	width : 1100,
	height : 370,
	closeAction : 'hide',
	closable : false,
	resizable : true,
	modal : true,
	items : [billDetailGrid, billGroupOrderDetailTabPanel],
	bbar : ['->', {
		text : '关闭',
		iconCls : 'btn_close',
		handler : function(){
			billDetailWin.hide();
		}
	}],
	keys : [{
		key : Ext.EventObject.ESC,
		scope : this,
		fn : function(){
			billDetailWin.hide();
		}
	}],
	listeners : {
		beforeshow : function(thiz){
			var sd = Ext.ux.getSelData(billsGrid);
			billgodtpStatus = false;
			if(sd.category == 4){
				billDetailGrid.hide();
				billGroupOrderDetailTabPanel.show();
				for(var i = billGroupOrderDetailTabPanel.items.length - 1; i >= 0 ; i--){
					billGroupOrderDetailTabPanel.items.get(i).destroy();
					billGroupOrderDetailTabPanel.remove(billGroupOrderDetailTabPanel.items.get(i));
				}
				var active = null;
				for(var i = 0; i < sd.childOrder.length; i++){
					var tempDetailGridID = 'detailGridForTabPanel' + sd.childOrder[i].id;
					var gp = createGridPanel(
						tempDetailGridID,
						('子账单编号:' + sd.childOrder[i].id),
						'',
					    '',
					    '../../QueryDetail.do',
					    [
						    [true, false, false, true], 
						    ['日期', 'order_date', 110] , 
						    ['名称', 'food_name', 130] , 
						    ['单价', 'unit_price', 60, 'right', 'Ext.ux.txtFormat.gridDou'],
						    ['数量', 'amount', 60, 'right', 'Ext.ux.txtFormat.gridDou'],
						    ['折扣', 'discount', 60, 'right', 'Ext.ux.txtFormat.gridDou'],
						    ['口味', 'taste_pref'],
						    ['口味价钱', 'taste_price', 60, 'right', 'Ext.ux.txtFormat.gridDou'],
						    ['厨房', 'kitchen', 60],
						    ['反结账', 'aaa', 60, 'center', 'detailIsPaidRenderer'],
						    ['服务员', 'waiter', 60],
						    ['退菜原因', 'cancelReason']
						],
						['order_date', 'food_name', 'unit_price', 'amount', 'discount',
						 'taste_pref', 'taste_price', 'kitchen', 'waiter', 'cancelReason'],
					    [['pin', pin], ['orderID', sd.childOrder[i].id], ['queryType', 'Today']],
					    billDetailpageRecordCount,
					    ''
					);
					gp.border = false;
					gp.frame = false;
					gp.on('load', function(thiz, rs, opt){
						detailGridLoadListeners(gp);
					});
					billGroupOrderDetailTabPanel.add(gp);
					if(i == 0)
						active = gp;
				}
				billgodtpStatus = true;
				billGroupOrderDetailTabPanel.setActiveTab(active);
				billGroupOrderDetailTabPanel.setHeight(thiz.getInnerHeight());
			}else{
				billGroupOrderDetailTabPanel.hide();
				billDetailGrid.show();
				billDetailGrid.getStore().reload({
					params : {
						start : 0,
						limit : billDetailpageRecordCount
					}
				});
				billDetailGrid.setHeight(thiz.getInnerHeight());
			}
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
//			'printReceipt' : 1
			'printType' : 3
		},
		success : function(response, options) {
//			var resultJSON = Ext.util.JSON.decode(response.responseText);
//			Ext.MessageBox.show({
//				msg : resultJSON.data,
//				width : 300,
//				buttons : Ext.MessageBox.OK
//			});
			Ext.ux.showMsg(Ext.decode(response.responseText));
		},
		failure : function(response, options) {
			Ext.ux.showMsg(Ext.decode(response.responseText));
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
		data :  [[0, '全部'], [1, '帐单号'], [2, '流水号'], [3, '台号'], [4, '时间'], [5, '类型'],
		         [6, '结帐方式'], [7, '金额'], [8, '实收']]
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

function billOpt(value, cellmeta, record, rowIndex, columnIndex, store) {
	return '<a href=\'javascript:billOptModifyHandler(' + rowIndex + ')\'>修改</a>'
			+ '&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;'
			+ '<a href=\'javascript:billViewHandler()\'>查看</a>'
			+ '&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;'
			+ '<a href=\'javascript:billDetailHandler(' + record.get('id') + ')\'>详细</a>'
			+ '&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;'
			+ '<a href=\'javascript:printBillFunc(' + record.get('id') + ')\'>补打</a>';
};

var billsStore = new Ext.data.Store({
	proxy : new Ext.data.HttpProxy({
		url : '../../QueryToday.do'
	}),
	reader : new Ext.data.JsonReader(Ext.ux.readConfig, [{
		name : 'id'
	}, {
		name : 'seqID'
	}, {
		name : 'tableID'
	}, {
		name : 'tableAlias'
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
		name : 'settleTypeFormat'
	}, {
		name : 'settleTypeValue'
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
	}, {
		name : 'childOrder'
	}]),
	listeners : {
		load : function(thiz, rs, options){
			for(var i = 0; i < rs.length; i++){
				if(eval(rs[i].get('id') == 0)){
					document.getElementById("shouldPaySum").innerHTML = rs[i].get('totalPrice').toFixed(2);
					document.getElementById("actualPaySum").innerHTML = rs[i].get('acturalPrice').toFixed(2);
					thiz.remove(rs[i]);
					return;
				}
			}
		}
	}
});

// 2，栏位模型
var billsColumnModel = new Ext.grid.ColumnModel([ new Ext.grid.RowNumberer(), {
	header : '帐单号',
	dataIndex : 'id',
	width : 100
}, {
	header : '流水号',
	dataIndex : 'seqID',
	width : 100
}, {
	header : '台号',
	dataIndex : 'tableAlias',
	width : 60,
	renderer : function(v){
		if(eval(v == 0)){
			return '--';
		}else{
			return v;
		}
	}
}, {
	header : '日期',
	dataIndex : 'orderDateFormat',
	width : 150
}, {
	header : '账单类型',
	dataIndex : 'categoryFormat',
	width : 100
}, {
	header : '结账方式',
	dataIndex : 'settleTypeFormat',
	width : 100
}, {
	header : '收款方式',
	dataIndex : 'payMannerFormat',
	width : 100
}, {
	header : '金额',
	dataIndex : 'totalPrice',
	width : 120,
	align : 'right',
	renderer : Ext.ux.txtFormat.gridDou
}, {
	header : '实收',
	dataIndex : 'acturalPrice',
	width : 120,
	align : 'right',
	renderer : Ext.ux.txtFormat.gridDou
}, {
	header : '状态',
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
				data : [[ 1, '一般' ], [2, '外卖' ], [3, '拆台' ], [4, '并台' ]]
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
		bbar : createPagingBar(billsGridDataSize, billsStore),
		listeners : {
			render : function(){
				billQueryHandler();
			},
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
//			    orderStatBut, 
//			    {xtype:'tbtext',text:'&nbsp;&nbsp;&nbsp;'},
//			    kitchenStatBut, 
//				{xtype:'tbtext',text:'&nbsp;&nbsp;&nbsp;'},
//				deptStatBut,
//				{xtype:'tbtext',text:'&nbsp;&nbsp;&nbsp;'},
//				regionStatBut,
				{xtype:'tbtext',text:'&nbsp;'},
				btnDutyRangeSub,
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
