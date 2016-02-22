
var btnDutyRangeSub = new Ext.ux.ImageButton({
	imgPath : '../../images/shiftStatis.png',
	imgWidth : 50,
	imgHeight : 50,
	tooltip : '交班记录',
	handler : function(btn) {
		dutyRangeSub({statType : 1});
	}
});

var btnPaymentSub = new Ext.ux.ImageButton({
	imgPath : '../../images/paymentRecord.png',
	imgWidth : 50,
	imgHeight : 50,
	tooltip : '交款记录',
	handler : function(btn) {
		dutyRangeSub({statType : 2});
	}
});

var btnSalesSub = new Ext.ux.ImageButton({
	imgPath : '../../images/salesStatForDaily.png',
	imgWidth : 50,
	imgHeight : 50,
	tooltip : '销售统计',
	handler : function(btn) {
		salesSub();
	}
});

var pushBackBut = new Ext.ux.ImageButton({
	imgPath : '../../images/UserLogout.png',
	imgWidth : 50,
	imgHeight : 50,
	tooltip : '返回',
	handler : function(btn) {
		location.href = 'TableSelect.html';
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

function billOptModifyHandler(rowindex) {
	var data = Ext.ux.getSelData(billsGrid);
	var orderType = 'common';
	if(data['category'] == 4){
		Ext.example.msg('提示', '团体餐桌暂不允许反结账.');
		return;
	}
	if(data['settleTypeValue'] == 2){
/*		Ext.example.msg('提示', '会员结账单暂不允许反结账.');
		return;*/
		orderType = 'member';
	}
	
	setDynamicKey('OrderMain.html', 'orderID=' + data['id'] +'&orderType=' + orderType);
};

function showViewBillWin(){
	viewBillWin = new Ext.Window({
		layout : 'fit',
		title : '查看账单',
		width : 510,
		height : 555,
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
						queryType : 'Today'
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

function showBillDetailWin(){
	billDetailWin = new Ext.Window({
		layout : 'fit',
		width : 1100,
		height : 440,
		closable : false,
		resizable : false,
		modal : true,
	//	items : [billDetailGrid, billGroupOrderDetailTabPanel],
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
						queryType : 'Today',
						foodStatus : foodStatus
					},
					method : 'post'
				});
				thiz.center();	
			}
		}
	});
}
// 查看明细
function billDetailHandler(orderID) {
	showBillDetailWin();
	billDetailWin.show();
	billDetailWin.setTitle('账单号: ' + orderID);
	billDetailWin.center();
};

// 打印link
function printBillFunc(orderID) {
	var tempMask = new Ext.LoadMask(document.body, {
		msg : '正在打印请稍候.......',
		remove : true
	});
	tempMask.show();
	Ext.Ajax.request({
		url : '../../PrintOrder.do',
		params : {
			orderID : orderID,
			printType : 3,
			orientedPrinter : Ext.util.Cookies.get(document.domain + '_printers')			//特定打印机打印
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
var frontBill_combo_staffs = new Ext.form.ComboBox({
	id : 'frontBill_combo_staffs',
	readOnly : false,
	forceSelection : true,
	width : 100,
	listWidth : 120,
	store : new Ext.data.SimpleStore({
		fields : ['staffID', 'staffName']
	}),
	valueField : 'staffID',
	displayField : 'staffName',
	typeAhead : true,
	mode : 'local',
	triggerAction : 'all',
	selectOnFocus : true,
	listeners : {
		render : function(thiz){
			var staffData = [[-1,'全部']];
			Ext.Ajax.request({
				url : '../../QueryStaff.do',
				params : {privileges : 1005},
				success : function(res, opt){
					var jr = Ext.decode(res.responseText);
					for(var i = 0; i < jr.root.length; i++){
						staffData.push([jr.root[i]['staffID'], jr.root[i]['staffName']]);
					}
					thiz.store.loadData(staffData);
					thiz.setValue(-1);
				},
				fialure : function(res, opt){
					thiz.store.loadData(staffData);
					thiz.setValue(-1);
				}
			});
		},
		select : function(){
			if(searchType){
				Ext.getCmp('fontBill_search').handler();
			}
		}
	}
});

// ------------------ north ------------------------
var f_bills_filterTypeComb = new Ext.form.ComboBox({
	fieldLabel : '过滤',
	forceSelection : true,
	width : 100,
	value : '全部',
	id : 'front_bill_filter',
	store : new Ext.data.SimpleStore({
		fields : [ 'value', 'text' ],
		data :  [[0, '全部'], [1, '帐单号'], [2, '流水号'], [3, '台号'], [4, '时间'], [5, '金额'], [6, '实收'], [7, '类型'], [8, '结帐方式'], [9, '操作员工']]
	}),
	valueField : 'value',
	displayField : 'text',
	typeAhead : true,
	mode : 'local',
	triggerAction : 'all',
	selectOnFocus : true,
	readOnly : false,
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
			var staffs = Ext.getCmp('frontBill_combo_staffs');
			
			//
			comboOperator.setVisible(false);
			comboTableType.setVisible(false);
			comboPayType.setVisible(false);
			timeCondition.setVisible(false);
			numberSearchValue.setVisible(false);
			staffs.setValue(false);
			
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
				// 金额
				numberSearchValue.setVisible(true);
				numberSearchValue.setValue();
				searchValue = numberSearchValue.getId();
			} else if (index == 6) {
				// 实收
				numberSearchValue.setVisible(true);
				numberSearchValue.setValue();
				searchValue = numberSearchValue.getId();
			} else if (index == 7) {
				// 类型
				comboOperator.setVisible(false);
				comboTableType.setVisible(true);
				comboTableType.setValue(1);
				searchValue = comboTableType.getId();
			} else if (index == 8) {
				// 结帐方式
				comboOperator.setVisible(false);
				comboPayType.setVisible(true);
				comboPayType.setValue(1);
				searchValue = comboPayType.getId();
			}else if (index == 9) {
				//操作人员
				comboOperator.setVisible(false);
				staffs.setVisible(true);
				staffs.setValue('全部');
				searchValue = staffs.getId();
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
}

function couponPriceHandler(v){
	if(!isNaN(v)){
		return Ext.ux.txtFormat.gridDou(v);
	}else{
		return v; 
	}
}
function commentTip(value, meta, rec, rowIdx, colIdx, ds){
	var subValue = value.length >6 ? value.substring(0,6) + '...' : value ;
    return '<div ext:qtitle="" ext:qtip="' + value + '">'+ subValue +'</div>';
}

var billsGrid;
var foodStatus;
Ext.onReady(function(){
	duty = createStatGridTabDutyFn({
		data : shiftDutyOfToday,
		listeners : {
			select : function(){
				if(searchType){
					Ext.getCmp('fontBill_search').handler();
				}
			}
		}		
	});
	var historySBar = new Ext.Toolbar({
		id : 'todayHighSBar',
		hidden : true,
		height : 28,
		items : [
			{xtype : 'tbtext', text : '班次:'},
			duty,
			{xtype : 'tbtext', text : '&nbsp;&nbsp;'},
			{xtype : 'tbtext', text : '台名/台号:'},
			{
				xtype : 'textfield',
				id : 'textTableAliasOrName',
				hidden : false,
				width : 100
			},
			{xtype : 'tbtext', text : '&nbsp;&nbsp;'},
			
			{xtype : 'tbtext', text : '收款方式:'},
			{
				xtype : 'combo',
				forceSelection : true,
				width : 80,
				id : 'comboPayType',
				store : new Ext.data.JsonStore({
					fields : [ 'id', 'name' ]
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
						Ext.Ajax.request({
							url : '../../QueryPayType.do',
							params : {dataSource : 'allPayType'},
							success : function(res){
								var jr = Ext.decode(res.responseText);
								jr.root.unshift({id:-1, name:'全部'});
								thiz.getStore().loadData(jr.root);
								thiz.setValue(-1);
							},
							failure : function(){
							
							}
						});
					},
					select : function(){
						if(searchType){
							Ext.getCmp('fontBill_search').handler();
						}
					}
				}				
			},
			{xtype : 'tbtext', text : '&nbsp;&nbsp;'},
			{xtype : 'tbtext', text : '操作员工:'},
			frontBill_combo_staffs,
			{xtype : 'tbtext', text : '&nbsp;&nbsp;'},
			{xtype : 'tbtext', text : '区域:'},
			{
				xtype : 'combo',
				forceSelection : true,
				width : 100,
				value : -1,
				id : 'today_comboRegion',
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
							url : '../../OperateRegion.do',
							params : {
								dataSource : 'getByCond'
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
					},
					select : function(){
						if(searchType){
							Ext.getCmp('fontBill_search').handler();
						}
					}
				}
			},
			{xtype : 'tbtext', text : '&nbsp;&nbsp;'},
			{xtype : 'tbtext', text : '备注搜索:'},
			{
				xtype : 'textfield',
				id : 'textSearchValue',
				hidden : false,
				width : 100
			}
		]
	});
	
	
	var billsGridTbar = new Ext.Toolbar({
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
		{ xtype:'tbtext', text:'&nbsp;&nbsp;'}, 
		{
			xtype : 'tbtext',
			text : '&nbsp;&nbsp;&nbsp;&nbsp;流水号:'
		}, 
		{ xtype:'tbtext', text:'&nbsp;'},
		{
			xtype : 'numberfield',
			id : 'tbSeqId',
			width : 130
		},
		{ xtype:'tbtext', text:'&nbsp;&nbsp;'},{
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
			boxLabel : '转菜',
			inputValue : 7,
			listeners : {
				check : function(e){
					if(e.getValue()){
						foodStatus = 'isTransfer';
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
			id : 'fontBill_search',
			iconCls : 'btn_search',
			handler : function(e){
				billQueryHandler();
			}
		}, {
			text : '高级条件↓',
	    	id : 'btnBillHeightSearch',
	    	handler : function(){
	    		searchType = true;
				Ext.getCmp('btnBillHeightSearch').hide();
	    		Ext.getCmp('btnBillCommonSearch').show();
	    		
	    		Ext.getCmp('todayHighSBar').show();
	    		
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
	    		
	    		Ext.getCmp('textSearchValue').setValue();
	    		Ext.getCmp('today_comboRegion').setValue(-1);
	    		
	    		Ext.getCmp('comboPayType').setValue(-1);
	    		Ext.getCmp('frontBill_combo_staffs').setValue(-1);
	    		
	    		Ext.getCmp('todayHighSBar').hide();
	    		
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
		'../../QueryOrderStatistics.do',
		[
			[true, false, false, true], 
			['帐单号', 'id'],
			['流水号', 'seqId'],
			['台号', 'table.alias',120,,'function(v,m,r){if(v != 0){return v+\"(\"+r.get("table.name")+\")\";}else{return r.get("table.name");}}'],
			['区域', 'table.region.name'],
			['日期', 'orderDateFormat', 150],
			['账单类型', 'categoryText',,'center'],
			['结账方式', 'settleTypeText',,'center'],
			['收款方式', 'payTypeText',,'center'],
			['优惠劵金额', 'couponPrice',,'right','couponPriceHandler'],
			['应收', 'totalPrice',,'right', 'Ext.ux.txtFormat.gridDou'],
			['实收', 'actualPrice',,'right', 'Ext.ux.txtFormat.gridDou'],
			['状态', 'statusText',,'center', 'function(v,m,r){if(r.get("statusValue")==2){return \'<font color=\"#FF0000\">反结账</font>\';}else{return v;}}'],
			['备注', 'comment',,'center', 'commentTip'],
			['操作', 'operator', 270, 'center', 'billOpt']
		],
		OrderRecord.getKeys(),
		[['dataType', 0]],
		GRID_PADDING_LIMIT_20,
		'',
		[billsGridTbar, historySBar]
	);
	billsGrid.region = 'center';
	billsGrid.on('render', function(){
		billQueryHandler();
	});
	billsGrid.on('rowclick', function(thiz, rowIndex, e){
		currRowIndex = rowIndex;
	});
	billsGrid.getStore().on('load', function(thiz, rs, options){
		for(var i = 0; i < rs.length; i++){
			if(eval(rs[i].get('id') == 0)){
				document.getElementById("shouldPaySum").innerHTML = rs[i].get('totalPrice').toFixed(2);
				document.getElementById("actualPaySum").innerHTML = rs[i].get('actualPrice').toFixed(2);
				thiz.remove(rs[i]);
				return;
			}
		}
	});
	billsGrid.keys = [{
		key : Ext.EventObject.ENTER,
		scope : this,
		fn : function(){
			billQueryHandler();
		}
	}];
	
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
			items : [{
				xtype:'tbtext',
				text:'&nbsp;'
			},
			//FIXME 暂时保留
/*			btnPaymentSub, 
			{
				xtype:'tbtext',
				text:'&nbsp;&nbsp;&nbsp;'
			},	*/		
			btnDutyRangeSub, 
			{
				xtype:'tbtext',
				text:'&nbsp;&nbsp;&nbsp;'
			},
			btnSalesSub
/*			,
			'->', 
			pushBackBut, 
			{
				xtype:'tbtext',
				text:'&nbsp;&nbsp;&nbsp;'
			}*/
			]
		})
	});
	
	initMainView(null, centerPanel, null);
	getOperatorName("../../");
	
});
