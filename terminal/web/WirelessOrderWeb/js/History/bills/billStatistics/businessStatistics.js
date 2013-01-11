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
		name : "cashIncome2"
	}, {
		name : "creditCardIncome2"
	}, {
		name : "hangIncome2"
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
//	layout : 'fit',
	items : [new Ext.form.FieldSet({
		xtype : 'fieldset',
		title : '汇总',
		layout : 'column',
		height : Ext.isIE ? 70 : 80 ,
		items : [{
			columnWidth : .06,
			items : [{
				xtype : 'label',
				text : '开始时间:'
			}]
		}, {
			columnWidth : .06,
			style : 'right',
			items : [{
				xtype : 'panel',
				html : '----'
			}]
		}, {
			columnWidth : .06,
			items : [{
				xtype : 'label',
				text : '实收总额:'
			}]
		}, {
			columnWidth : .06,
			style : 'right',
			items : [{
				xtype : 'panel',
				html : '----'
			}]
		}, {
			columnWidth : 1,
			height : 10
		}, {
			columnWidth : .06,
			items : [{
				xtype : 'label',
				text : '结束时间:'
			}]
		}, {
			columnWidth : .06,
			style : 'right',
			items : [{
				xtype : 'panel',
				html : '----'
			}]
		}, {
			columnWidth : .06,
			items : [{
				xtype : 'label',
				text : '账单总数:'
			}]
		}, {
			columnWidth : .06,
			style : 'right',
			items : [{
				xtype : 'panel',
				html : '----'
			}]
		}]
	})]
});

businessStatResultWin = new Ext.Window({
	title : '收款统计',
	width : 1200,
//	height : 500,
	height : 410,
	resizable : false,
	modal : true,
	closable : false,
	layout : 'border',
//	items : [businessStatResultSummaryPanel, businessStatResultGrid],
	items : [businessStatResultGrid],
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
