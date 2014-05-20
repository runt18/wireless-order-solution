
//------------------lib
function billQueryHandler() {
	var gs = billsGrid.getStore();
	if(searchType){
		gs.baseParams['beginDate'] = Ext.getCmp('dateSearchDateBegin').getValue().format('Y-m-d 00:00:00');
		gs.baseParams['endDate'] = Ext.getCmp('dateSearchDateEnd').getValue().format('Y-m-d 23:59:59');
		gs.baseParams['comboPayType'] = Ext.getCmp('comboPayType').getValue();
		gs.baseParams['common'] = Ext.getCmp('textSearchValue').getValue();
		gs.baseParams['value'] = Ext.getCmp('numberSearchValue').getValue();
		if(isNaN(Ext.getCmp('textTableAliasOrName').getValue())){
			gs.baseParams['tableName'] = Ext.getCmp('textTableAliasOrName').getValue();
		}else{
			gs.baseParams['tableAlias'] = Ext.getCmp('textTableAliasOrName').getValue();
		}
		gs.baseParams['region'] = Ext.getCmp('history_comboRegion').getValue();
	}else{
		gs.baseParams['value'] = Ext.getCmp('numberSearchValue').getValue();
	}
	
	sAdditionFilter = Ext.getCmp(searchAdditionFilter).inputValue;	
	
	gs.baseParams['type'] = searchType;

	gs.baseParams['havingCond'] = sAdditionFilter;
	gs.load({
		params : {
			start : 0,
			limit : GRID_PADDING_LIMIT_20
		}
	});
};

function billQueryExportHandler() {
	var url;
	if(searchType){
		url = '../../{0}?beginDate={1}&endDate={2}&comboPayType={3}&common={4}&value={5}&type={6}&havingCond={7}&dataSource={8}';
		url = String.format(
			url, 
			'ExportHistoryStatisticsToExecl.do', 
			Ext.getCmp('dateSearchDateBegin').getValue().format('Y-m-d 00:00:00'), 
			Ext.getCmp('dateSearchDateEnd').getValue().format('Y-m-d 23:59:59'),
			Ext.getCmp('comboPayType').getValue(),
			Ext.getCmp('textSearchValue').getValue(),
			Ext.getCmp('numberSearchValue').getValue(),
			searchType,
			sAdditionFilter,
			'historyOrder'
		);
	}else{
		url = '../../{0}?value={1}&type={2}&havingCond={3}&dataSource={4}';
		url = String.format(
				url, 
				'ExportHistoryStatisticsToExecl.do', 
				Ext.getCmp('numberSearchValue').getValue(), 
				searchType,
				sAdditionFilter,
				'historyOrder'
		);
	}
	window.location = url;
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


var logOutBut = new Ext.ux.ImageButton({
	imgPath : "../../images/ResLogout.png",
	imgWidth : 50,
	imgHeight : 50,
	tooltip : "登出",
	handler : function(btn) {
		
	}
});

function showViewBillWin(){
	viewBillWin = new Ext.Window({
		id : 'history_viewBillWin',
		layout : 'fit',
		title : '查看账单',
		width : 510,
		height : 550,
		resizable : false,
		closable : false,
		modal : true,
		bbar : ['->', {
			text : '关闭',
			iconCls : 'btn_close',
			handler : function() {
				viewBillWin.destroy();
			}
		}],
		keys : [{
			key : Ext.EventObject.ESC,
			scope : this,
			fn : function(){
				viewBillWin.destroy();
			}
		}],
		listeners : {
			show : function(thiz) {
				var sd = Ext.ux.getSelData(billsGrid);
				thiz.load({
					url : '../window/history/viewBillDetail.jsp', 
					scripts : true,
					params : {
						orderId : sd.id,
						queryType : 'History'
					},
					method : 'post'
				});
				thiz.center();	
			}
		}
	});
}

function billViewHandler() {
	showViewBillWin();
	viewBillWin.show();
	viewBillWin.center();
};

function detailIsPaidRenderer(v){
	return eval(v) ? '是' : '否';
}

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

function showBillDetailWin(){
	billDetailWin = new Ext.Window({
		layout : 'fit',
		width : 1100,
		height : 440,
		closable : false,
		resizable : false,
		modal : true,
		bbar : ['->', {
			text : '关闭',
			iconCls : 'btn_close',
			handler : function() {
				billDetailWin.destroy();
			}
		} ],
		keys : [{
			key : Ext.EventObject.ESC,
			scope : this,
			fn : function(){
				billDetailWin.destroy();
			}
		}],
		listeners : {
			show : function(thiz) {
				var sd = Ext.ux.getSelData(billsGrid);
				thiz.load({
					url : '../window/history/orderDetail.jsp', 
					scripts : true,
					params : {
						orderId : sd.id,
						foodStatus : foodStatus
					},
					method : 'post'
				});
				thiz.center();	
			}
		}
	});
}



function billDetailHandler(orderID) {
	showBillDetailWin();
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

function billOpt(value, cellmeta, record, rowIndex, columnIndex, store) {
	return ''
			+ '<a href=\"javascript:billViewHandler()\">查看</a>'
			+ '&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;'
			+ '<a href=\"javascript:billDetailHandler(' + record.get('id') + ')\">明细</a>'
			+ '';
};
	
function commentTip(value, meta, rec, rowIdx, colIdx, ds){
	var subValue = value.length >6 ? value.substring(0,6) + '...' : value ;
    return '<div ext:qtitle="" ext:qtip="' + value + '">'+ subValue +'</div>';
}
function couponPriceHandler(v){
	if(!isNaN(v)){
		return Ext.ux.txtFormat.gridDou(v);
	}else{
		return v; 
	}
}

var billsGrid;
var foodStatus;
var historySBar;
Ext.onReady(function() {
	var history_beginDate = new Ext.form.DateField({
		xtype : 'datefield',	
		id : 'dateSearchDateBegin',
		format : 'Y-m-d',
		width : 100,
		maxValue : new Date(),
		readOnly : false,
		allowBlank : false
	});
	var history_endDate = new Ext.form.DateField({
		xtype : 'datefield',
		id : 'dateSearchDateEnd',
		format : 'Y-m-d',
		width : 100,
		maxValue : new Date(),
		readOnly : false,
		allowBlank : false
	});
	var history_dateCombo = Ext.ux.createDateCombo({
		width : 90,
		beginDate : history_beginDate,
		endDate : history_endDate,
		callback : function(){
//			Ext.getCmp('btnSreachForMainOrderGrid').handler();
		}
	});
	
	
	historySBar = new Ext.Toolbar({
		id : 'historyHighSBar',
		hidden : true,
		height : 28,
		items : [
			{xtype : 'tbtext', text : '查看日期:'},
			{xtype : 'tbtext', text : '&nbsp;&nbsp;'},
			history_dateCombo,
			{xtype : 'tbtext', text : '&nbsp;'},
			history_beginDate,
			{
				xtype : 'label',
				hidden : false,
				id : 'tbtextDisplanZ',
				text : ' 至 '
			}, 
			history_endDate,
			{xtype : 'tbtext', text : '&nbsp;&nbsp;'},
			{xtype : 'tbtext', text : '收款方式:'},
			{
				xtype : 'combo',
				forceSelection : true,
				width : 70,
				value : -1,
				id : 'comboPayType',
				store : new Ext.data.SimpleStore({
					fields : [ 'value', 'text' ],
					data : [[-1, '全部'], [1, '现金' ], [2, '刷卡' ], [3, '会员卡' ], [4, '签单' ], [5, '挂账' ]]
				}),
				valueField : 'value',
				displayField : 'text',
				typeAhead : true,
				mode : 'local',
				triggerAction : 'all',
				selectOnFocus : true,
				allowBlank : false,
				readOnly : false
			},
			{xtype : 'tbtext', text : '&nbsp;&nbsp;'},
			{xtype : 'tbtext', text : '台名/台号:'},
			{
				xtype : 'textfield',
				id : 'textTableAliasOrName',
				hidden : false,
				width : 100
			},
			{xtype : 'tbtext', text : '&nbsp;&nbsp;'},
			{xtype : 'tbtext', text : '备注搜索:'},
			{
				xtype : 'textfield',
				id : 'textSearchValue',
				hidden : false,
				width : 100
			},
			{xtype : 'tbtext', text : '&nbsp;&nbsp;'},
			{xtype : 'tbtext', text : '区域:'},
			{
				xtype : 'combo',
				forceSelection : true,
				width : 90,
				value : -1,
				id : 'history_comboRegion',
				store : new Ext.data.SimpleStore({
					fields : ['id', 'name']
				}),
				valueField : 'id',
				displayField : 'name',
				typeAhead : true,
				mode : 'local',
				triggerAction : 'all',
				selectOnFocus : true,
				allowBlank : false,
				readOnly : false,
				listeners : {
					render : function(thiz){
						var data = [[-1,'全部']];
						Ext.Ajax.request({
							url : '../../QueryRegion.do',
							params : {
								dataSource : 'normal'
							},
							success : function(res, opt){
								var jr = Ext.decode(res.responseText);
								for(var i = 0; i < jr.root.length; i++){
									data.push([jr.root[i]['id'], jr.root[i]['name']]);
								}
								thiz.store.loadData(data);
								thiz.setValue(-1);
							},
							fialure : function(res, opt){
								thiz.store.loadData(data);
								thiz.setValue(-1);
							}
						});
					}
				}
			}
		]
	});
	
	
	
	
	var billsGridTbar = new Ext.Toolbar({
		id : 'historyCommonSBar',
		height : 26,
		items : [{
			xtype : 'tbtext',
			text : '&nbsp;&nbsp;&nbsp;&nbsp;账单号:'
		}, 
		{ xtype:'tbtext', text:'&nbsp;&nbsp;'},
		{
			xtype : 'numberfield',
			id : 'numberSearchValue',
			width : 130
		},
		{ xtype:'tbtext', text:'&nbsp;&nbsp;'}, {
			xtype : 'radio',
			id : 'searchAdditionFilterAll',
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
					if(e.getValue()){
						foodStatus = 'isRepaid';
						searchAdditionFilter = e.getId();
					}
						
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
					if(e.getValue()){
						foodStatus = 'isGift';
						searchAdditionFilter = e.getId();
					}
				}
			}
		}, { xtype:'tbtext', text:'&nbsp;&nbsp;'}, {
			xtype : 'radio',
			name : 'conditionRadio',
			boxLabel : '退菜',
			inputValue : 4,
			listeners : {
				check : function(e){
					if(e.getValue()){
						foodStatus = 'isReturn';
						searchAdditionFilter = e.getId();
					}
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
		}, { xtype:'tbtext', text:'&nbsp;&nbsp;'}, {
			xtype : 'radio',
			name : 'conditionRadio',
			boxLabel : '优惠劵',
			inputValue : 6,
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
		},{
			text : '导出',
			iconCls : 'icon_tb_exoprt_excel',
			handler : function(){
				billQueryExportHandler();
			}
		}, {
			text : '高级条件↓',
	    	id : 'btnBillHeightSearch',
	    	handler : function(){
	    		searchType = true;
				Ext.getCmp('btnBillHeightSearch').hide();
	    		Ext.getCmp('btnBillCommonSearch').show();
	    		
	    		Ext.getCmp('historyHighSBar').show();
	    		
	    		billsGrid.setHeight(billsGrid.getHeight()-28);
	    		billsGrid.syncSize();
	    		billsGrid.doLayout();//重新布局 	
			}
		}, {
			 text : '高级条件↑',
	    	 id : 'btnBillCommonSearch',
	    	 hidden : true,
	    	 handler : function(thiz){
	    	 	searchType = false;
	    		Ext.getCmp('btnBillHeightSearch').show();
	    		Ext.getCmp('btnBillCommonSearch').hide();
	    		
	    		history_dateCombo.setValue(2);
	    		Ext.getCmp('textSearchValue').setValue();
	    		Ext.getCmp('comboPayType').setValue(-1);
	    		
//	    		Ext.getCmp('btnSreachForMainOrderGrid').handler();
	    		Ext.getCmp('historyHighSBar').hide();
	    		
	    		billsGrid.setHeight(billsGrid.getHeight()+28);
	    		billsGrid.syncSize();
	    		billsGrid.doLayout();
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
//			['流水号', 'seqId'],
			['台号', 'table.alias'],
			['日期', 'orderDateFormat', 150],
//			['账单类型', 'categoryText',,'center'],
			['结账方式', 'settleTypeText',,'center'],
			['收款方式', 'payTypeText',,'center'],
			['优惠劵金额', 'couponPrice',,'right','couponPriceHandler'],
			['应收', 'totalPrice',,'right', 'Ext.ux.txtFormat.gridDou'],
			['实收', 'actualPrice',,'right', 'Ext.ux.txtFormat.gridDou'],
			['状态', 'statusText',,'center', 'function(v,m,r){if(r.get("statusValue")==2){return \'<font color=\"#FF0000\">反结账</font>\';}else{return v;}}'],
			['备注', 'comment',,'center', 'commentTip'],
			['操作', 'operator', 140, 'center', 'billOpt']
		],
		OrderRecord.getKeys(),
		[['isPaging', true], ['restaurantID', restaurantID]],
		GRID_PADDING_LIMIT_20,
		'',
		[billsGridTbar,historySBar]
	);
	billsGrid.region = 'center';
	billsGrid.on('render', function(){
		history_dateCombo.setValue(2);
		history_dateCombo.fireEvent('select', history_dateCombo,null,2);
	});
	billsGrid.on('bodyresize', function(e, w, h){
		
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
			shiftStatBut, 
			{xtype:'tbtext',text:'&nbsp;&nbsp;&nbsp;'},
			dailySettleStatBut
			]
		})
	});
	billHistoryOnLoad();
	billQueryHandler();
});
