
//------------------lib
function billQueryHandler() {
	var sType = searchType, sValue = '', sOperator = '', sAdditionFilter = 0;
	var onDuty = '', offDuty = '';
	if(sType == 0){
		sValue = '';
		searchOperator = '';
	}else if(sType == 4){
		var temp = searchValue.split(searchSubSplitSymbol);
		onDuty = Ext.getCmp(temp[0]).getValue().format('Y-m-d 00:00:00');
		offDuty = Ext.getCmp(temp[1]).getValue().format('Y-m-d 23:59:59');
		sValue = onDuty + '<split>' + offDuty;
	}else if(searchType == 9){
		sValue = '';
	}else{
		sValue = searchValue != '' ? Ext.getCmp(searchValue).getValue() : '';
		sOperator = searchOperator != '' ? Ext.getCmp(searchOperator).getValue() : '';
		if(typeof sValue == 'string' && sValue == ''){
			sType = 0;
			sValue = '';
		}
	}
	sAdditionFilter = Ext.getCmp(searchAdditionFilter).inputValue;	
	var gs = billsGrid.getStore();
	gs.baseParams['isPaging'] = true;
	gs.baseParams['restaurantID'] = restaurantID;
	
	gs.baseParams['type'] = sType;
	gs.baseParams['ope'] = sOperator;
	gs.baseParams['value'] = sValue;
	gs.baseParams['havingCond'] = sAdditionFilter;
	gs.load({
		params : {
			start : 0,
			limit : GRID_PADDING_LIMIT_20
		}
	});
};

//----------------------load
function loadAddKitchens() {
	kitchenMultSelectData = [];
	Ext.Ajax.request({
		url : "../../QueryKitchen.do",
		params : {
			"dataSource" : "normal",
			
			"isPaging" : false
		},
		success : function(response, options) {
			var resultJSON = Ext.util.JSON.decode(response.responseText);
			var rootData = resultJSON.root;
			for ( var i = 0; i < rootData.length; i++) {
				kitchenMultSelectData.push([
				    rootData[i].kitchenAlias,
					rootData[i].kitchenName, 
					rootData[i].kitchenID 
				]);
			}
		},
		failure : function(response, options) {
			Ext.MessageBox.show({
				msg : " Unknown page error ",
				width : 300,
				buttons : Ext.MessageBox.OK
			});
		}
	});
}

// on page load function
function billHistoryOnLoad() {
	// data init
	loadAddKitchens();
};
//-----------
/* ---------------------------------------------------------------- */
var btnCancelledFood = new Ext.ux.ImageButton({
	imgPath : '../../images/cancelledFoodStatis.png',
	imgWidth : 50,
	imgHeight : 50,
	tooltip : '退菜统计',
	handler : function(btn) {
		cancelledFood();
	}
});

var regionStatBut = new Ext.ux.ImageButton({
	imgPath : "../../images/regionStatis.png",
	imgWidth : 50,
	imgHeight : 50,
	tooltip : "区域统计",
	handler : function(btn) {
		regionStatWin.show();
	}
});

var discountStatBut = new Ext.ux.ImageButton({
	imgPath : "../../images/discountStatis.png",
	imgWidth : 50,
	imgHeight : 50,
	tooltip : "折扣统计",
	handler : function(btn) {
		discountStatWin.show();
	}
});

var shiftStatBut = new Ext.ux.ImageButton({
	imgPath : "../../images/shiftStatis.png",
	imgWidth : 50,
	imgHeight : 50,
	tooltip : "交班记录",
	handler : function(btn) {
		dutyRangeStat();
	}
});

var dailySettleStatBut = new Ext.ux.ImageButton({
	imgPath : "../../images/dailySettleStatis.png",
	imgWidth : 50,
	imgHeight : 50,
	tooltip : "日结记录",
	handler : function(btn) {
		dailySettleStat();
	}
});

var businessStatBut = new Ext.ux.ImageButton({
	imgPath : "../../images/businessStatis.png",
	imgWidth : 50,
	imgHeight : 50,
	tooltip : "营业统计",
	handler : function(btn) {
		var businessStatWin = Ext.getCmp('businessStatWin');
		if(!businessStatWin){
			businessStatWin = new Ext.Window({
				title : '营业统计 -- <font style="color:green;">历史</font>',
				id : 'businessStatWin',
				width : 885,
				height : 555,
				closable : false,
				modal : true,
				resizable : false,	
				layout: 'fit',
				bbar : ['->', {
					text : '关闭',
					iconCls : 'btn_close',
					handler : function(){
						businessStatWin.hide();
					}
				}],
				keys : [{
					key : Ext.EventObject.ESC,
					scope : this,
					fn : function(){
						businessStatWin.hide();
					}
				}],
				listeners : {
					hide : function(thiz){
						thiz.body.update('');
					},
					show : function(thiz){
						thiz.load({
							autoLoad : false,
							url : '../window/history/businessStatistics.jsp',
							scripts : true,
							nocache : true,
							text : '功能加载中, 请稍后......',
							params : {
								d : '_' + new Date().getTime(),
								dataSource : 'history'
							}
						});
					}
				}
			});
		}
		businessStatWin.show();
		businessStatWin.center();
	}
});

var receivablesStatBut = new Ext.ux.ImageButton({
	imgPath : "../../images/businessReceips.png",
	imgWidth : 50,
	imgHeight : 50,
	tooltip : "收款统计",
	handler : function(btn) {
		receivablesStatResultWin.show();
		receivablesStatResultWin.center();
	}
});

var btnSalesSub = new Ext.ux.ImageButton({
	imgPath : '../../images/salesStat.png',
	imgWidth : 50,
	imgHeight : 50,
	tooltip : '销售统计',
	handler : function(btn) {
		salesSub();
		salesSubWinTabPanel.setActiveTab(orderFoodStatPanel);
	}
});

// --
var pushBackBut = new Ext.ux.ImageButton({
	imgPath : "../../images/UserLogout.png",
	imgWidth : 50,
	imgHeight : 50,
	tooltip : "返回",
	handler : function(btn) {
		location.href = '../PersonLogin.html?'+ strEncode('restaurantID=' + restaurantID, 'mi');
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

var history_viewBillGrid;
	if(!history_viewBillGrid){
		history_viewBillGrid = createGridPanel(
			'',
			'已点菜',
			'',
		    '',
		    '',
		    [
			    [true, false, false, false], 
			    ['菜名', 'name', 130] , 
			    ['口味', 'tasteGroup.tastePref', 100],
			    ['数量', 'count', 50, 'right', 'Ext.ux.txtFormat.gridDou'],
			    ['折扣', 'discount', 50, 'right', 'Ext.ux.txtFormat.gridDou'],
			    ['金额', 'totalPrice', 100, 'right', 'Ext.ux.txtFormat.gridDou']
			],
			OrderFoodRecord.getKeys(),
		    [],
		    0
		);
	}


history_viewBillGrid.region = 'center';

var viewBillGenPanel = new Ext.Panel({
	region : 'north',
	height : 120,
	frame : true,
	border : false,
	items : [new Ext.Panel({
		xtype : 'panel',
		layout : 'column',
		//height : Ext.isIE ? 90 : 110 ,
		height : 110,
		defaults : {
			columnWidth : .23,
			defaults : {
				xtype : 'panel',
				html : '----'
			}
		},
		items : [{
			items : [{
				cls : 'cLeft',
				html : '账单号:'
			}]
		}, {
			items : [{
				cls : 'left',
				id : 'billIDBV'
			}]
		},{
			items : [{
				cls : 'cLeft',
				html : '类型:'
			}]
		}, {
			items : [{
				cls : 'left',
				id : 'billTypeBV'
			}]
		}, {
			columnWidth : 1
		}, {
			items : [{
				cls : 'cLeft',
				html : '台号:'
			}]
		}, {
			items : [{
				cls : 'left',
				id : 'tableNbrBV'
			}]
		},{
			items : [{
				cls : 'cLeft',
				html : '人数:'
			}]
		}, {
			items : [{
				cls : 'left',
				id : 'personNbrBV'
			}]
		}, {
			columnWidth : 1
		}, {
			items : [{
				cls : 'cLeft',
				html : '日期:'
			}]
		}, {
			columnWidth : .7,
			items : [{
				cls : 'left',
				id : 'billDateBV'
			}]
		}, {
			columnWidth : 1
		},{
			items : [{
				cls : 'cLeft',
				html : '结账方式:'
			}]
		}, {
			items : [{
				cls : 'left',
				id : 'payTypeBV'
			}]
		},{
			items : [{
				cls : 'cLeft',
				html : '付款方式:'
			}]
		}, {
			items : [{
				cls : 'left',
				id : 'payMannerBV'
			}]
		}, {
			columnWidth : 1
		},{
			items : [{
				cls : 'cLeft',
				html : '服务费:'
			}]
		}, {
			items : [{
				cls : 'left',
				id : 'serviceRateBV'
			}]
		}]
	})]
});

var viewBillAddPanel = new Ext.Panel({
	region : 'south',
	height : 60,
	frame : true,
	border : false,
	items : [new Ext.Panel({
		xtype : 'panel',
		layout : 'column',
		//height : Ext.isIE ? 90 : 110 ,
		height : 70,
		defaults : {
			columnWidth : .16,
			defaults : {
				xtype : 'panel',
				html : '----'
			}
		},
		items : [{
			items : [{
				cls : 'cLeft',
				html : '抹数:'
			}]
		}, {
			items : [{
				cls : 'left',
				id : 'erasePuotaPriceBV'
			}]
		},{
			items : [{
				cls : 'cLeft',
				html : '赠送:'
			}]
		}, {
			items : [{
				cls : 'left',
				id : 'forFreeBV'
			}]
		}, {
			items : [{
				cls : 'cLeft',
				html : '应收:'
			}]
		}, {
			items : [{
				cls : 'left',
				id : 'shouldPayBV'
			}]
		},{
			columnWidth : 1
		},{
			items : [{
				cls : 'cLeft',
				html : '退菜:'
			}]
		}, {
			items : [{
				cls : 'left',
				id : 'cancelPriceBV'
			}]
		},  {
			items : [{
				cls : 'cLeft',
				html : '折扣:'
			}]
		}, {
			items : [{
				cls : 'left',
				id : 'discountBV'
			}]
		}, {
			items : [{
				cls : 'cLeft',
				html : '应收:'
			}]
		}, {
			items : [{
				cls : 'left',
				id : 'actrualPayBV'
			}]
		}]
	})]
});

var viewBillWin = Ext.getCmp('history_viewBillWin');
if(!viewBillWin){
	viewBillWin = new Ext.Window({
		id : 'history_viewBillWin',
		layout : 'fit',
		title : '查看账单',
		width : 500,
		height : 500,
		resizable : false,
		closable : false,
		modal : true,
		items : [ {
			layout : 'border',
			border : false,
			items : [ viewBillGenPanel, history_viewBillGrid, viewBillAddPanel ]
			//items : [viewBillGrid]
		} ],
		bbar : ['->', {
			text : '关闭',
			iconCls : 'btn_close',
			handler : function() {
				viewBillWin.hide();
			}
		}],
		keys : [{
			key : Ext.EventObject.ESC,
			scope : this,
			fn : function(){
				viewBillWin.hide();
			}
		}],
		listeners : {
			hide : function(thiz) {
				viewBillData = null;
				history_viewBillGrid.getStore().removeAll();
			},
			show : function(thiz) {
				var data = Ext.ux.getSelData(billsGrid);
				var orderID = data['id'];
				
				Ext.getDom('billIDBV').innerHTML = orderID;
				Ext.getDom('billTypeBV').innerHTML = data['categoryText'];
				Ext.getDom('tableNbrBV').innerHTML = data['table.alias'];
				Ext.getDom('personNbrBV').innerHTML = data['customNum'];
				Ext.getDom('billDateBV').innerHTML = data['orderDateFormat'];
				Ext.getDom('payTypeBV').innerHTML = data['settleTypeText'];
				Ext.getDom('payMannerBV').innerHTML = data['payTypeText'];
				Ext.getDom('serviceRateBV').innerHTML = data['serviceRate'] + '％';
				Ext.getDom('forFreeBV').innerHTML = '￥' + data['giftPrice'].toFixed(2);
				Ext.getDom('shouldPayBV').innerHTML = '￥' + data['totalPrice'].toFixed(2);
				Ext.getDom('actrualPayBV').innerHTML = '￥' + data['actualPrice'].toFixed(2);
				Ext.getDom('discountBV').innerHTML = '￥' + data['discountPrice'].toFixed(2);
				Ext.getDom('erasePuotaPriceBV').innerHTML = '￥' + data['erasePrice'].toFixed(2);
				Ext.getDom('cancelPriceBV').innerHTML = '￥' + data['cancelPrice'].toFixed(2);
	
				Ext.Ajax.request({
					url : '../../QueryOrder.do',
					params : {
						
						'orderID' : orderID,
						'queryType' : 'History'
					},
					success : function(response, options) {
						var jr = Ext.decode(response.responseText);
						if (jr.success == true) {
							viewBillData = jr;
							history_viewBillGrid.getStore().loadData(viewBillData);
						} else {
							Ext.ux.showMsg(jr);
						}
					},
					failure : function(response, options) {
						var jr = Ext.decode(response.responseText);
						Ext.ux.showMsg(jr);	
					}
				});
			}
		}
	});
}

function billViewHandler() {
	viewBillWin.show();
	viewBillWin.center();
};

function detailIsPaidRenderer(v){
	return eval(v) ? '是' : '否';
}

var billDetailGrid = createGridPanel(
	'',
	'',
	'',
	'',
	'../../QueryDetail.do',
	[
	    [true,false,false,false],
	    ['日期','orderDateFormat',100],
	    ['名称','name',130],
	    ['单价','unitPrice',60, 'right', 'Ext.ux.txtFormat.gridDou'],
	    ['数量','count', 60, 'right', 'Ext.ux.txtFormat.gridDou'], 
	    ['口味','tasteGroup.tastePref'],
	    ['口味价钱','tasteGroup.tastePrice', 60, 'right', 'Ext.ux.txtFormat.gridDou'],
	    ['厨房','kitchen.name', 60],
	    ['反结账','isPaid', 60, 'center', 'detailIsPaidRenderer'],
	    ['服务员','waiter', 60],
	    ['退菜原因', 'cancelReason.reason']
	],
	OrderFoodRecord.getKeys(),
	[ ['queryType', 'History']],
	'',
	''
);
billDetailGrid.frame = false;
billDetailGrid.border = false;
billDetailGrid.getStore().on('beforeload', function(thiz){
	thiz.baseParams['orderID'] = Ext.ux.getSelData(billsGrid)['id'];
});

var billgodtpStatus = false;
var billGroupOrderDetailTabPanel = new Ext.TabPanel({
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

billDetailWin = new Ext.Window({
	layout : 'fit',
	width : 1100,
	height : 370,
	closable : false,
	resizable : false,
	modal : true,
	items : [billDetailGrid, billGroupOrderDetailTabPanel],
	bbar : ['->', {
		text : '关闭',
		iconCls : 'btn_close',
		handler : function() {
			billDetailWin.hide();
		}
	} ],
	keys : [{
		key : Ext.EventObject.ESC,
		scope : this,
		fn : function(){
			billDetailWin.hide();
		}
	}],
	listeners : {
		show : function(thiz) {
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
					    [ ['orderID', sd.childOrder[i].id], ['queryType', 'History']],
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

function billDetailHandler(orderID) {
	billDetailWin.show();
	billDetailWin.setTitle('账单号: ' + orderID);
	billDetailWin.center();
};

// 打印link
function printBillFunc(rowInd) {
	var tempMask = new Ext.LoadMask(document.body, {
		msg : '正在打印请稍候.......',
		remove : true
	});
	tempMask.show();
	Ext.Ajax.request({
		url : "../../PrintOrder.do",
		params : {
			
			"orderID" : billsGrid.getStore().getAt(rowInd).get("id"),
			'printType' : 3
		},
		success : function(response, options) {
			tempMask.hide();
			Ext.ux.showMsg(Ext.decode(response.responseText));
		},
		failure : function(response, options) {
			tempMask.hide();
			Ext.ux.showMsg(Ext.decode(response.responseText));
		}
	});
};

var filterTypeComb = new Ext.form.ComboBox({
	fieldLabel : '过滤',
	forceSelection : true,
	width : 100,
	value : '全部',
	id : 'filter',
	store : new Ext.data.SimpleStore({
		fields : [ 'value', 'text' ],
		data :  [[0, '全部'], [1, '帐单号'], [2, '流水号'], [3, '台号'], [4, '日期'], [5, '类型'], 
		         [6, '结帐方式'], [7, '金额'], [8, '实收'], [9, '最近日结']]
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
			var dateSearchDateBegin = Ext.getCmp('dateSearchDateBegin');
			var dateSearchDateEnd = Ext.getCmp('dateSearchDateEnd');
			var numberSearchValue = Ext.getCmp('numberSearchValue');
			var tbtextDisplanZ = Ext.getCmp('tbtextDisplanZ');
			//
			comboOperator.setVisible(false);
			comboTableType.setVisible(false);
			comboPayType.setVisible(false);
			numberSearchValue.setVisible(false);
			dateSearchDateBegin.setVisible(false);
			dateSearchDateEnd.setVisible(false);
			tbtextDisplanZ.setVisible(false);
			
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
				// 日期
				comboOperator.setVisible(false);
				dateSearchDateBegin.setVisible(true);
				dateSearchDateEnd.setVisible(true);
				dateSearchDateBegin.setValue(new Date());
				dateSearchDateEnd.setValue(new Date());
				tbtextDisplanZ.setVisible(true);
				searchValue = dateSearchDateBegin.getId() + searchSubSplitSymbol + dateSearchDateEnd.getId();
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
			} else if (index == 9) {
				// 最近日结
				comboOperator.setVisible(false);
			}
			
		}
	}
});

function billOpt(value, cellmeta, record, rowIndex, columnIndex, store) {
	return ''
			+ '<a href=\"javascript:billViewHandler()\">查看</a>'
			+ '&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;'
			+ '<a href=\"javascript:billDetailHandler(' + record.get('id') + ')\">明细</a>'
			+ '';
};

var billsGrid;
Ext.onReady(function() {
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
			value : 1,
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
			xtype : 'datefield',
			id : 'dateSearchDateBegin',
			hidden : true,
			allowBlank : false,
			format : 'Y-m-d',
			value : new Date(),
			maxValue : new Date(),
			width : 100
		}, {
			xtype : 'label',
			hidden : true,
			id : 'tbtextDisplanZ',
			text : ' 至 '
		}, {
			xtype : 'datefield',
			id : 'dateSearchDateEnd',
			hidden : true,
			allowBlank : false,
			format : 'Y-m-d',
			value : new Date(),
			maxValue : new Date(),
			width : 100
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
			id : 'btnSreachForMainOrderGrid',
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
	billsGrid = createGridPanel(
		'billsGrid',
		'',
		'',
		'',
		'../../QueryHistory.do',
		[
			[true, false, false, true], 
			['帐单号', 'id'],
			['流水号', 'seqId'],
			['台号', 'table.alias'],
			['日期', 'orderDateFormat', 150],
			['账单类型', 'categoryText',,'center'],
			['结账方式', 'settleTypeText',,'center'],
			['收款方式', 'payTypeText',,'center'],
			['应收', 'totalPrice',,'right', 'Ext.ux.txtFormat.gridDou'],
			['实收', 'actualPrice',,'right', 'Ext.ux.txtFormat.gridDou'],
			['状态', 'statusText',,'center', 'function(v,m,r){if(r.get("statusValue")==2){return \'<font color=\"#FF0000\">反结账</font>\';}else{return v;}}'],
			['操作', 'operator', 180, 'center', 'billOpt']
		],
		OrderRecord.getKeys(),
		[['isPaging', true], ['restaurantID', restaurantID]],
		GRID_PADDING_LIMIT_20,
		'',
		billsGridTbar
	);
	billsGrid.region = 'center';
	billsGrid.on('render', function(){
		filterTypeComb.setValue(9);
		filterTypeComb.fireEvent('select', filterTypeComb, null, 9);
		billQueryHandler();
	});
	billsGrid.keys = [{
		key : Ext.EventObject.ENTER,
		scope : this,
		fn : function(){
			billQueryHandler();
		}
	}];
	
	// --------------------------------------------------------------------------
	new Ext.Panel({
		//title : '历史账单管理',
		renderTo : 'divHistoryStatistics',
		width : parseInt(Ext.getDom('divHistoryStatistics').parentElement.style.width.replace(/px/g,'')),
		height : parseInt(Ext.getDom('divHistoryStatistics').parentElement.style.height.replace(/px/g,'')),
		//region : 'center',
		layout : 'fit',
		items : [ {
			layout : 'border',
			items : [billsGrid]
		} ],
		tbar : new Ext.Toolbar({
			height : 55,
			items : [
			{xtype:'tbtext',text:'&nbsp;'},
			btnCancelledFood,
			{xtype:'tbtext',text:'&nbsp;&nbsp;&nbsp;'},
			shiftStatBut, 
			{xtype:'tbtext',text:'&nbsp;&nbsp;&nbsp;'},
			dailySettleStatBut,
			{xtype:'tbtext',text:'&nbsp;&nbsp;&nbsp;'},
			businessStatBut, 
			{xtype:'tbtext',text:'&nbsp;&nbsp;&nbsp;'},
			receivablesStatBut,
			{xtype:'tbtext',text:'&nbsp;&nbsp;&nbsp;'},
			btnSalesSub
			]
		})
	});
	billHistoryOnLoad();
/*	initMainView(null, centerPanel, null);
	getOperatorName("../../");*/
});
