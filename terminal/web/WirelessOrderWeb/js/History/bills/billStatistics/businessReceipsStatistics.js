receivablesStaticRecordCount = 22;

var receivablesStatResultStore = new Ext.data.Store({
	proxy : new Ext.data.HttpProxy({
		url : "../../BusinessReceiptsStatistics.do"
	}),
	baseParams : {
		dataSource : 'normal',
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
		name : "memberAmount"
	}, {
		name : "memberActual"
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
		name : "couponAmount"
	}, {
		name : "couponIncome"
	}, {
		name : "totalIncome"
	}, {
		name : "totalActual"
	}, {
		name : "totalActualCharge"
	}, {
		name : "totalActualRefund"
	}]),
	listeners : {
		load : function(thiz, rs, options){
			var sr = rs[rs.length-1];
			thiz.remove(sr);
			
			var onDuty = Ext.getDom('panelOfReceivablesStatOnDuty');
			var offDuty = Ext.getDom('panelOfReceivablesStatOffDuty');
			var totalPrice = Ext.getDom('panelOfReceivablesStatSumTotalPrice');
			var orderAmount = Ext.getDom('panelOfReceivablesStatSumOrderAmount');
			
			var cashIncome = Ext.getDom('panelOfReceivablesStatSumCashIncome');
			var cashAmount = Ext.getDom('panelOfReceivablesStatSumCashAmount');
			var creditCardIncome = Ext.getDom('panelOfReceivablesStatSumCreditCardIncome');
			var creditCardAmount = Ext.getDom('panelOfReceivablesStatSumCreditCardAmount');
			var hangIncome = Ext.getDom('panelOfReceivablesStatSumHangIncome');
			var hangAmount = Ext.getDom('panelOfReceivablesStatSumHangAmount');
			var signIncome = Ext.getDom('panelOfReceivablesStatSumSignIncome');
			var signAmount = Ext.getDom('panelOfReceivablesStatSumSignAmount');
			var memberActual = Ext.getDom('panelOfReceivablesStatSumMemberActual');
			var memberAmount = Ext.getDom('panelOfReceivablesStatSumMemberAmount');
			var couponIncome = Ext.getDom('panelOfReceivablesStatSumCouponIncome');
			var couponAmount = Ext.getDom('panelOfReceivablesStatSumCouponAmount');
			
			onDuty.innerHTML = Ext.getCmp('receivablesStaticBeginDate').getValue().format('Y-m-d');
			offDuty.innerHTML = Ext.getCmp('receivablesStaticEndDate').getValue().format('Y-m-d');
			
			totalPrice.innerHTML = sr.get('totalActual').toFixed(2);
			orderAmount.innerHTML = sr.get('orderAmount');
			
			cashIncome.innerHTML = sr.get('cashIncome2').toFixed(2);
			cashAmount.innerHTML = sr.get('cashAmount');
			creditCardIncome.innerHTML = sr.get('creditCardIncome2').toFixed(2);
			creditCardAmount.innerHTML = sr.get('creditCardAmount');
			hangIncome.innerHTML = sr.get('hangIncome2').toFixed(2);
			hangAmount.innerHTML = sr.get('hangAmount');
			signIncome.innerHTML = sr.get('signIncome2').toFixed(2);
			signAmount.innerHTML = sr.get('signAmount');
			memberActual.innerHTML = sr.get('memberActual').toFixed(2);
			memberAmount.innerHTML = sr.get('memberAmount');
			couponIncome.innerHTML = sr.get('couponIncome').toFixed(2);
			couponAmount.innerHTML = sr.get('couponAmount');
		}
	}
});

// 2，栏位模型
var receivablesStatResultColumnModel = new Ext.grid.ColumnModel([
	new Ext.grid.RowNumberer(), {
		header : '日期',
		dataIndex : 'offDutyToDate',
		width : 100
	}, {
		header : '应收',
		dataIndex : 'totalIncome',
		renderer : Ext.ux.txtFormat.gridDou,
		align : 'right',
		width : 100
	}, {
		header : '实收',
		dataIndex : 'totalActual',
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
		header : '会员',
		dataIndex : 'memberActual',
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
		header : '抹数',
		dataIndex : 'eraseIncome',
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
		header : '优惠劵',
		dataIndex : 'couponIncome',
		renderer : Ext.ux.txtFormat.gridDou,
		align : 'right',
		width : 70
	}, {
		header : '会员充值',
		dataIndex : 'totalActualCharge',
		renderer : Ext.ux.txtFormat.gridDou,
		align : 'right',
		width : 90
	}, {
		header : '会员退款',
		dataIndex : 'totalActualRefund',
		renderer : Ext.ux.txtFormat.gridDou,
		align : 'right',
		width : 90
	}
]);

var receivablesStatResultGrid = new Ext.grid.GridPanel({
	xtype : "grid",
//	frame : true,
	border : false,
	region : 'center',
	ds : receivablesStatResultStore,
	autoScroll : true,
	loadMask : {
		msg : "数据加载中，请稍等..."
	},
	cm : receivablesStatResultColumnModel,
	sm : new Ext.grid.RowSelectionModel({
		singleSelect : true
	}),
/*	viewConfig : {
		forceFit : true
	},*/
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
		value : '前一天',
		listeners : {
			render : function(thiz){
				thiz.store.loadData([[0,'今天'], [1,'前一天'], [2,'最近7天'], [3, '最近一个月'], [4, '最近三个月']]);
			},
			select : function(thiz, record, index){
				var now = new Date();
				var dateBegin = Ext.getCmp('receivablesStaticBeginDate');
				var dateEnd = Ext.getCmp('receivablesStaticEndDate');
				dateEnd.setValue(now);
				if(index == 0){
					
				}else if(index == 1){
					now.setDate(now.getDate()-1);
					dateEnd.setValue(now);
				}else if(index == 2){
					now.setDate(now.getDate()-7);
				}else if(index == 3){
					now.setMonth(now.getMonth()-1);
				}else if(index == 4){
					now.setMonth(now.getMonth()-3);
				}
				dateBegin.setValue(now);
				Ext.getCmp('btnSearchReceivablesStat').handler();
			}
		}
	}, {
		xtype : 'tbtext',
		text : '&nbsp;'
	},{
		xtype : "datefield",
		format : "Y-m-d",
		id : "receivablesStaticBeginDate",
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
		id : "receivablesStaticEndDate",
		width : 100,
		maxValue : new Date(),
		allowBlank : false,
		readOnly : true
	}, '->', {
		text : '搜索',
		id : 'btnSearchReceivablesStat',
		iconCls : 'btn_search',
		handler : function(){
			var dateBegin = Ext.getCmp('receivablesStaticBeginDate');
			var dateEnd = Ext.getCmp('receivablesStaticEndDate');
			
			if(!dateBegin.isValid() || !dateEnd.isValid()){
				return;
			}
			
			var gs = receivablesStatResultGrid.getStore();
			gs.baseParams['dateBegin'] = dateBegin.getValue().format('Y-m-d 00:00:00');
			gs.baseParams['dateEnd'] = dateEnd.getValue().format('Y-m-d 23:59:59');
			gs.load({
				params : {
					start : 0,
					limit : receivablesStaticRecordCount
				}
			});
		}
	}, '-', {
		text : '导出',
//		hidden : true,
		iconCls : 'icon_tb_exoprt_excel',
		handler : function(){
			var onDuty = Ext.getCmp('receivablesStaticBeginDate');
			var offDuty = Ext.getCmp('receivablesStaticEndDate');
			
			var url = '../../{0}?pin={1}&restaurantID={2}&dataSource={3}&onDuty={4}&offDuty={5}';
			url = String.format(
					url, 
					'ExportHistoryStatisticsToExecl.do', 
					-10, 
					restaurantID, 
					'businessReceips',
					onDuty.getValue().format('Y-m-d 00:00:00'),
					offDuty.getValue().format('Y-m-d 23:59:59')
				);
			
			window.location = url;
		}
	}],
	bbar : new Ext.PagingToolbar({
		pageSize : receivablesStaticRecordCount,
		store : receivablesStatResultStore,
		displayInfo : true,
		displayMsg : '显示第 {0} 条到 {1} 条记录，共 {2} 条',
		emptyMsg : "没有记录"
	})
});

var receivablesStatResultSummaryPanel = new Ext.Panel({
	region : 'south',
	frame : true,
	height : 100,
	items : [new Ext.form.FieldSet({
		xtype : 'fieldset',
		title : '汇总',
		layout : 'column',
		height : Ext.isIE ? 70 : 80 ,
		defaults : {
			columnWidth : .085,
			defaults : {
				xtype : 'panel',
				html : '----'
			}
		},
		items : [{
			columnWidth : .061,
			items : [{
				style : 'color:#15428B;margin-left:7px;',
				html : '开始时间:'
			}]
		}, {
			items : [{
				style : 'text-align:right;',
				id : 'panelOfReceivablesStatOnDuty',
				html : '----'
			}]
		},{
			items : [{
				style : 'color:#15428B;margin-left:7px;',
				html : '实收总额:'
			}]
		}, {
			columnWidth : .035,
			items : [{
				style : 'text-align:right;',
				id : 'panelOfReceivablesStatSumTotalPrice',
				html : '----'
			}]
		}, {
			items : [{
				style : 'color:#15428B;margin-left:7px;',
				html : '现金单总额:'
			}]
		}, {
			columnWidth : .035,
			items : [{
				style : 'text-align:right;',
				id : 'panelOfReceivablesStatSumCashIncome',
				html : '----'
			}]
		}, {
			items : [{
				style : 'color:#15428B;margin-left:7px;',
				html : '刷卡单总额:'
			}]
		}, {
			columnWidth : .035,
			items : [{
				style : 'text-align:right;',
				id : 'panelOfReceivablesStatSumCreditCardIncome',
				html : '----'
			}]
		}, {
			items : [{
				style : 'color:#15428B;margin-left:7px;',
				html : '会员单总额:'
			}]
		}, {
			columnWidth : .035,
			items : [{
				style : 'text-align:right;',
				id : 'panelOfReceivablesStatSumMemberActual',
				html : '----'
			}]
		}, {
			items : [{
				style : 'color:#15428B;margin-left:7px;',
				html : '挂账单总额:'
			}]
		}, {
			columnWidth : .035,
			items : [{
				style : 'text-align:right;',
				id : 'panelOfReceivablesStatSumHangIncome',
				html : '----'
			}]
		}, {
			items : [{
				style : 'color:#15428B;margin-left:7px;',
				html : '签单单总额:'
			}]
		}, {
			columnWidth : .035,
			items : [{
				style : 'text-align:right;',
				id : 'panelOfReceivablesStatSumSignIncome',
				html : '----'
			}]
		},
		{
			items : [{
				style : 'color:#15428B;margin-left:7px;',
				html : '优惠劵总额:'
			}]
		}, {
			columnWidth : .035,
			items : [{
				style : 'text-align:right;',
				id : 'panelOfReceivablesStatSumCouponIncome',
				html : '----'
			}]
		}, 
		//***********
		{
			columnWidth : 1,
			height : 10
		}, 
		//***********
		{
			columnWidth : .061,
			items : [{
				style : 'color:#15428B;margin-left:7px;',
				html : '结束时间:'
			}]
		}, {
			items : [{
				id : 'panelOfReceivablesStatOffDuty',
				style : 'text-align:right;',
				html : '----'
			}]
		},  {
			items : [{
				style : 'color:#15428B;margin-left:7px;',
				html : '账单总数:'
			}]
		}, {
			columnWidth : .035,
			items : [{
				id : 'panelOfReceivablesStatSumOrderAmount',
				style : 'text-align:right;',
				html : '----'
			}]
		}, {
			items : [{
				style : 'color:#15428B;margin-left:7px;',
				html : '现金单总数:'
			}]
		}, {
			columnWidth : .035,
			items : [{
				style : 'text-align:right;',
				id : 'panelOfReceivablesStatSumCashAmount',
				html : '----'
			}]
		}, {
			items : [{
				style : 'color:#15428B;margin-left:7px;',
				html : '刷卡单总数:'
			}]
		}, {
			columnWidth : .035,
			items : [{
				style : 'text-align:right;',
				id : 'panelOfReceivablesStatSumCreditCardAmount',
				html : '----'
			}]
		},{
			items : [{
				style : 'color:#15428B;margin-left:7px;',
				html : '会员单总数:'
			}]
		}, {
			columnWidth : .035,
			items : [{
				style : 'text-align:right;',
				id : 'panelOfReceivablesStatSumMemberAmount',
				html : '----'
			}]
		},  {
			items : [{
				style : 'color:#15428B;margin-left:7px;',
				html : '挂账单总数:'
			}]
		}, {
			columnWidth : .035,
			items : [{
				style : 'text-align:right;',
				id : 'panelOfReceivablesStatSumHangAmount',
				html : '----'
			}]
		}, {
			items : [{
				style : 'color:#15428B;margin-left:7px;',
				html : '签单单总数:'
			}]
		}, {
			columnWidth : .035,
			items : [{
				style : 'text-align:right;',
				id : 'panelOfReceivablesStatSumSignAmount',
				html : '----'
			}]
		}, {
			items : [{
				style : 'color:#15428B;margin-left:7px;',
				html : '优惠劵单数:'
			}]
		}, {
			columnWidth : .035,
			items : [{
				style : 'text-align:right;',
				id : 'panelOfReceivablesStatSumCouponAmount',
				html : '----'
			}]
		}]
	})]
});

Ext.onReady(function(){

	new Ext.Panel({
		renderTo : 'divBusinessReceiptStatistics',//渲染到
		id : 'businessReceiptStatisticsPanel',
		//solve不跟随窗口的变化而变化
		width : parseInt(Ext.getDom('divBusinessReceiptStatistics').parentElement.style.width.replace(/px/g,'')),
		height : parseInt(Ext.getDom('divBusinessReceiptStatistics').parentElement.style.height.replace(/px/g,'')),
		layout:'border',
		frame : true, //边框
		//子集
		items : [receivablesStatResultGrid, receivablesStatResultSummaryPanel]
	});
	Ext.getCmp('comboBSSearchDate').setValue(1);
	Ext.getCmp('comboBSSearchDate').fireEvent('select', null, null, 1);
//	repaidStatisticsGrid.getStore().load();
});
