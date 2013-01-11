var businessStatResultStore = new Ext.data.Store({
	proxy : new Ext.data.HttpProxy({
		url : "../../businessStatistics.do"
	}),
	baseParams : {
		pin : pin,
		restaurantID : restaurantID,
		isPaging : true,
		StatisticsType : 'History'
	},
	reader : new Ext.data.JsonReader({
		totalProperty : "totalProperty",
		root : "root"
	}, [ {
		name : "offDuty"
	}, {
		name : 'offDutyToDate'
	}, {
		name : "orderAmount"
	}, {
		name : "cashAmount"
	}, {
		name : "cashIncome2"
	}, {
		name : "creditCardAmount"
	},{
		name : "creditCardIncome2"
	}, {
		name : "hangAmount"
	}, {
		name : "hangIncome2"
	}, {
		name : "signAmount"
	}, {
		name : "signIncome2"
	}, {
		name : "paidIncome"
	}, {
		name : "discountIncome"
	}, {
		name : "giftIncome"
	}, {
		name : "cancelIncome"
	}, {
		name : "eraseAmount"
	}, {
		name : "eraseIncome"
	}, {
		name : "totalPrice"
	}, {
		name : "totalPrice2"
	}]),
	listeners : {
		load : function(thiz, rs, options){
			var sr = rs[rs.length-1];
			thiz.remove(sr);
			
			var onDuty = Ext.getDom('panelOfBusinessStatOnDuty');
			var offDuty = Ext.getDom('panelOfBusinessStatOffDuty');
			var totalPrice = Ext.getDom('panelOfBusinessStatSumTotalPrice');
			var orderAmount = Ext.getDom('panelOfBusinessStatSumOrderAmount');
			
			var cashIncome = Ext.getDom('panelOfBusinessStatSumCashIncome');
			var cashAmount = Ext.getDom('panelOfBusinessStatSumCashAmount');
			var creditCardIncome = Ext.getDom('panelOfBusinessStatSumCreditCardIncome');
			var creditCardAmount = Ext.getDom('panelOfBusinessStatSumCreditCardAmount');
			var hangIncome = Ext.getDom('panelOfBusinessStatSumHangIncome');
			var hangAmount = Ext.getDom('panelOfBusinessStatSumHangAmount');
			var signIncome = Ext.getDom('panelOfBusinessStatSumSignIncome');
			var signAmount = Ext.getDom('panelOfBusinessStatSumSignAmount');
//			var eraseIncome = Ext.getDom('panelOfBusinessStatSumEraseIncome');
//			var erasAmount = Ext.getDom('panelOfBusinessStatSumEraseAmount');
			
			onDuty.innerHTML = Ext.getCmp('businessStaticBeginDate').getValue().format('Y-m-d');
			offDuty.innerHTML = Ext.getCmp('businessStaticEndDate').getValue().format('Y-m-d');
			
			totalPrice.innerHTML = sr.get('totalPrice2').toFixed(2);
			orderAmount.innerHTML = sr.get('orderAmount');
			
			cashIncome.innerHTML = sr.get('cashIncome2').toFixed(2);
			cashAmount.innerHTML = sr.get('cashAmount');
			creditCardIncome.innerHTML = sr.get('creditCardIncome2').toFixed(2);
			creditCardAmount.innerHTML = sr.get('creditCardAmount');
			hangIncome.innerHTML = sr.get('hangIncome2').toFixed(2);
			hangAmount.innerHTML = sr.get('hangAmount');
			signIncome.innerHTML = sr.get('signIncome2').toFixed(2);
			signAmount.innerHTML = sr.get('signAmount');
//			eraseIncome.innerHTML = sr.get('eraseIncome').toFixed(2);
//			erasAmount.innerHTML = sr.get('eraseAmount');
		}
	}
});

// 2，栏位模型
var businessStatResultColumnModel = new Ext.grid.ColumnModel([
	new Ext.grid.RowNumberer(), {
		header : '日期',
		dataIndex : 'offDutyToDate',
		width : 100
	}, {
		header : '金额',
//		sortable : true,
		dataIndex : 'totalPrice',
		renderer : Ext.ux.txtFormat.gridDou,
		align : 'right',
		width : 100
	}, {
		header : '实收',
		dataIndex : 'totalPrice2',
		renderer : Ext.ux.txtFormat.gridDou,
		align : 'right',
		width : 100
	}, {
		header : '账单数',
		dataIndex : 'orderAmount',
		align : 'right',
		width : 70
	}, {
		header : '现金',
		dataIndex : 'cashIncome2',
		renderer : Ext.ux.txtFormat.gridDou,
		align : 'right',
		width : 100
	}, {
		header : '刷卡',
		dataIndex : 'creditCardIncome2',
		renderer : Ext.ux.txtFormat.gridDou,
		align : 'right',
		width : 70
	}, {
		header : '挂账',
		dataIndex : 'hangIncome2',
		renderer : Ext.ux.txtFormat.gridDou,
		align : 'right',
		width : 70
	}, {
		header : '签单',
		dataIndex : 'signIncome2',
		renderer : Ext.ux.txtFormat.gridDou,
		align : 'right',
		width : 70
	}, {
		header : '折扣',
		dataIndex : 'discountIncome',
		renderer : Ext.ux.txtFormat.gridDou,
		align : 'right',
		width : 70
	}, {
		header : '赠送',
		dataIndex : 'giftIncome',
		renderer : Ext.ux.txtFormat.gridDou,
		align : 'right',
		width : 70
	}, {
		header : '退菜',
		dataIndex : 'cancelIncome',
		renderer : Ext.ux.txtFormat.gridDou,
		align : 'right',
		width : 70
	}, {
		header : '反结帐',
		dataIndex : 'paidIncome',
		renderer : Ext.ux.txtFormat.gridDou,
		align : 'right',
		width : 70
	}, {
		header : '抹数',
		dataIndex : 'eraseIncome',
		renderer : Ext.ux.txtFormat.gridDou,
		align : 'right',
		width : 70
	}
]);

var businessStatResultGrid = new Ext.grid.GridPanel({
	xtype : "grid",
//	frame : true,
	border : false,
	region : 'center',
	ds : businessStatResultStore,
	autoScroll : true,
	loadMask : {
		msg : "数据加载中，请稍等..."
	},
	cm : businessStatResultColumnModel,
	sm : new Ext.grid.RowSelectionModel({
		singleSelect : true
	}),
	viewConfig : {
		forceFit : true
	},
	tbar : [{
		xtype : 'tbtext',
		text : '日期:&nbsp;'
	}, {
		xtype : 'combo',
		id : 'comboBSSearchDate',
		forceSelection : true,
		width : 100,
		store : new Ext.data.SimpleStore({
			fields : ['value', 'text']
		}),
		valueField : 'value',
		displayField : 'text',
		typeAhead : true,
		mode : 'local',
		triggerAction : 'all',
		selectOnFocus : true,
		value : '今天',
		listeners : {
			render : function(thiz){
				thiz.store.loadData([[0,'今天'], [1,'前一天'], [2,'最近7天'], [3, '最近一个月'], [4, '最近三个月']]);
			},
			select : function(thiz, record, index){
				var now = new Date();
				var dateBegin = Ext.getCmp('businessStaticBeginDate');
				var dateEnd = Ext.getCmp('businessStaticEndDate');
				dateEnd.setValue(now);
				if(index == 0){
					
				}else if(index == 1){
					now.setDate(now.getDate()-1);
				}else if(index == 2){
					now.setDate(now.getDate()-7);
				}else if(index == 3){
					now.setMonth(now.getMonth()-1);
				}else if(index == 4){
					now.setMonth(now.getMonth()-3);
				}
				dateBegin.setValue(now);
				Ext.getCmp('btnSearchBusinessStat').handler();
			}
		}
	}, {
		xtype : 'tbtext',
		text : '&nbsp;'
	},{
		xtype : "datefield",
		format : "Y-m-d",
		id : "businessStaticBeginDate",
		width : 100,
		maxValue : new Date(),
		allowBlank : false,
		readOnly : true
	}, {
		xtype : 'tbtext',
		text : '&nbsp;至&nbsp;'
	}, {
		xtype : "datefield",
		format : "Y-m-d",
		id : "businessStaticEndDate",
		width : 100,
		maxValue : new Date(),
		allowBlank : false,
		readOnly : true
	}, '->', {
		text : '搜索',
		id : 'btnSearchBusinessStat',
		iconCls : 'btn_search',
		handler : function(){
			var dateBegin = Ext.getCmp('businessStaticBeginDate');
			var dateEnd = Ext.getCmp('businessStaticEndDate');
			
			if(!dateBegin.isValid() || !dateEnd.isValid()){
				return;
			}
			
			var gs = businessStatResultGrid.getStore();
			gs.baseParams['dateBegin'] = dateBegin.getValue().format('Y-m-d 00:00:00');
			gs.baseParams['dateEnd'] = dateEnd.getValue().format('Y-m-d 23:59:59');
			gs.load({
				params : {
					start : 0,
					limit : businessStaticRecordCount
				}
			});
		}
	}],
	bbar : new Ext.PagingToolbar({
		pageSize : businessStaticRecordCount,
		store : businessStatResultStore,
		displayInfo : true,
		displayMsg : '显示第 {0} 条到 <span id="spanBusinessStaticRecordCount">{1}</span> 条记录，共 {2} 条',
		emptyMsg : "没有记录"
	}),
	keys : [{
		key : Ext.EventObject.ESC,
		scope : this,
		fn : function(){
			businessStatResultWin.hide();
		}
	}]
});

var businessStatResultSummaryPanel = new Ext.Panel({
	region : 'south',
	frame : true,
	height : 100,
	items : [new Ext.form.FieldSet({
		xtype : 'fieldset',
		title : '汇总',
		layout : 'column',
		height : Ext.isIE ? 70 : 80 ,
		defaults : {
			columnWidth : .06,
			defaults : {
				xtype : 'panel',
				html : '----'
			}
		},
		items : [{
			items : [{
				style : 'color:#15428B;text-align:left;',
				html : '开始时间:'
			}]
		}, {
			items : [{
				style : 'text-align:right;',
				id : 'panelOfBusinessStatOnDuty',
				html : '----'
			}]
		}, {
			columnWidth : .01,
			html : '&nbsp;'
		}, {
			items : [{
				style : 'color:#15428B;text-align:left;',
				html : '实收总额:'
			}]
		}, {
			items : [{
				style : 'text-align:right;',
				id : 'panelOfBusinessStatSumTotalPrice',
				html : '----'
			}]
		}, {
			columnWidth : .01,
			html : '&nbsp;'
		}, {
			items : [{
				style : 'color:#15428B;text-align:left;',
				html : '现金单总额:'
			}]
		}, {
			items : [{
				style : 'text-align:right;',
				id : 'panelOfBusinessStatSumCashIncome',
				html : '----'
			}]
		}, {
			columnWidth : .01,
			html : '&nbsp;'
		}, {
			items : [{
				style : 'color:#15428B;text-align:left;',
				html : '刷卡单总额:'
			}]
		}, {
			items : [{
				style : 'text-align:right;',
				id : 'panelOfBusinessStatSumCreditCardIncome',
				html : '----'
			}]
		}, {
			columnWidth : .01,
			html : '&nbsp;'
		}, {
			items : [{
				style : 'color:#15428B;text-align:left;',
				html : '挂账单总额:'
			}]
		}, {
			items : [{
				style : 'text-align:right;',
				id : 'panelOfBusinessStatSumHangIncome',
				html : '----'
			}]
		}, {
			columnWidth : .01,
			html : '&nbsp;'
		}, {
			items : [{
				style : 'color:#15428B;text-align:left;',
				html : '签单单总额:'
			}]
		}, {
			items : [{
				style : 'text-align:right;',
				id : 'panelOfBusinessStatSumSignIncome',
				html : '----'
			}]
		}, {
			columnWidth : .01,
			html : '&nbsp;'
		}, 
//		{
//			items : [{
//				style : 'color:#15428B;text-align:left;',
//				html : '抹数总额:'
//			}]
//		}, {
//			items : [{
//				style : 'text-align:right;',
//				id : 'panelOfBusinessStatSumEraseIncome',
//				html : '----'
//			}]
//		}, {
//			columnWidth : .01,
//			html : '&nbsp;'
//		},
		//***********
		{
			columnWidth : 1,
			height : 10
		}, 
		//***********
		{
			items : [{
				style : 'color:#15428B;text-align:left;',
				html : '结束时间:'
			}]
		}, {
			items : [{
				id : 'panelOfBusinessStatOffDuty',
				style : 'text-align:right;',
				html : '----'
			}]
		}, {
			columnWidth : .01,
			html : '&nbsp;'
		}, {
			items : [{
				style : 'color:#15428B;text-align:left;',
				html : '账单总数:'
			}]
		}, {
			items : [{
				id : 'panelOfBusinessStatSumOrderAmount',
				style : 'text-align:right;',
				html : '----'
			}]
		}, {
			columnWidth : .01,
			html : '&nbsp;'
		}, {
			items : [{
				style : 'color:#15428B;text-align:left;',
				html : '现金单总数:'
			}]
		}, {
			items : [{
				style : 'text-align:right;',
				id : 'panelOfBusinessStatSumCashAmount',
				html : '----'
			}]
		}, {
			columnWidth : .01,
			html : '&nbsp;'
		}, {
			items : [{
				style : 'color:#15428B;text-align:left;',
				html : '刷卡单总数:'
			}]
		}, {
			items : [{
				style : 'text-align:right;',
				id : 'panelOfBusinessStatSumCreditCardAmount',
				html : '----'
			}]
		}, {
			columnWidth : .01,
			html : '&nbsp;'
		}, {
			items : [{
				style : 'color:#15428B;text-align:left;',
				html : '挂账单总数:'
			}]
		}, {
			items : [{
				style : 'text-align:right;',
				id : 'panelOfBusinessStatSumHangAmount',
				html : '----'
			}]
		}, {
			columnWidth : .01,
			html : '&nbsp;'
		}, {
			items : [{
				style : 'color:#15428B;text-align:left;',
				html : '签单单总数:'
			}]
		}, {
			items : [{
				style : 'text-align:right;',
				id : 'panelOfBusinessStatSumSignAmount',
				html : '----'
			}]
		}, {
			columnWidth : .01,
			html : '&nbsp;'
		}
//		,{
//			items : [{
//				style : 'color:#15428B;text-align:left;',
//				html : '抹数单总数:'
//			}]
//		}, {
//			items : [{
//				style : 'text-align:right;',
//				id : 'panelOfBusinessStatSumEraseAmount',
//				html : '----'
//			}]
//		}, {
//			columnWidth : .01,
//			html : '&nbsp;'
//		}  
		]
	})]
});

businessStatResultWin = new Ext.Window({
	title : '收款统计',
	width : 1200,
	height : 510,
//	height : 410,
	resizable : false,
	modal : true,
	closable : false,
	layout : 'border',
	items : [businessStatResultSummaryPanel, businessStatResultGrid],
//	items : [businessStatResultGrid],
	bbar : ['->', {
		text : '关闭',
		iconCls : 'btn_close',
		handler : function() {
			businessStatResultWin.hide();
		}
	}],
	keys : [{
		key : Ext.EventObject.ESC,
		scope : this,
		fn : function(){
			businessStatResultWin.hide();
		}
	}, {
		key : Ext.EventObject.ENTER,
		scope : this,
		fn : function(){
			Ext.getCmp('btnSearchBusinessStat').handler();
		}
	}],
	listeners : {
		show : function(thiz) {
			Ext.getCmp('comboBSSearchDate').setValue(0);
			Ext.getCmp('comboBSSearchDate').fireEvent('select', null, null, 0);
		},
		hide : function(){
			businessStatResultGrid.getStore().removeAll();
		}
	}
});