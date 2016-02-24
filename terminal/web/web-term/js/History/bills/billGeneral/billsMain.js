var history_hours;
//------------------lib
function billQueryHandler(c) {
	c = c || {};
	var gs = billsGrid.getStore();
	var params = {
			start : 0,
			limit : GRID_PADDING_LIMIT_20
	};
	if(searchType){
		gs.baseParams['dateBeg'] = Ext.util.Format.date(Ext.getCmp('dateSearchDateBegin').getValue(), 'Y-m-d 00:00:00');
		gs.baseParams['dateEnd'] = Ext.util.Format.date(Ext.getCmp('dateSearchDateEnd').getValue(), 'Y-m-d 23:59:59');
		gs.baseParams['comboPayType'] = Ext.getCmp('comboPayType').getValue();
		gs.baseParams['common'] = Ext.getCmp('textSearchValue').getValue();
		if(c.isRange){
			gs.baseParams['isRange'] = true;
		}
		if(isNaN(Ext.getCmp('textTableAliasOrName').getValue())){
			gs.baseParams['tableName'] = Ext.getCmp('textTableAliasOrName').getValue();
			gs.baseParams['tableAlias'] = '';
		}else{
			gs.baseParams['tableAlias'] = Ext.getCmp('textTableAliasOrName').getValue();
			gs.baseParams['tableName'] = '';
		}
		gs.baseParams['region'] = Ext.getCmp('history_comboRegion').getValue();
		gs.baseParams['staffId'] = Ext.getCmp('historyBill_combo_staffs').getValue();
		
		var businessHour;
		
		if(history_hours){
			businessHour = history_hours;
		}else{
			businessHour = history_oBusinessHourData({type : 'get'}).data;
		}
		
		if(parseInt(businessHour.businessHourType) != -1){
			gs.baseParams['opening'] = businessHour.opening;
			gs.baseParams['ending'] = businessHour.ending;
		}else{
			gs.baseParams['opening'] = '';
		}
	}else{
		gs.baseParams = {};
	}
	gs.baseParams['orderId'] = Ext.getCmp('numberSearchValue').getValue();
	
	gs.baseParams['seqId'] = Ext.getCmp('numberSearchSeqIdValue').getValue();
	
	sAdditionFilter = Ext.getCmp(searchAdditionFilter).inputValue;	
	
	gs.baseParams['dataType'] = 1;

	gs.baseParams['havingCond'] = sAdditionFilter;
	gs.load({
		params : params
	});
};

function billQueryExportHandler() {
	var url;
	sAdditionFilter = Ext.getCmp(searchAdditionFilter).inputValue;
	if(searchType){
		var businessHour = history_oBusinessHourData({type : 'get'}).data;
		var opening, ending;
		if(parseInt(businessHour.businessHourType) != -1){
			opening = businessHour.opening;
			ending = businessHour.ending;
		}else{
			opening = '';
			ending = '';
		}
		url = '../../{0}?dateBeg={1}&dateEnd={2}&comboPayType={3}&common={4}&orderId={5}&tableName={6}&tableAlias={7}&region={8}&havingCond={9}&dataSource={10}&dataType={11}&seqId={12}&opening={13}&ending={14}';
		url = String.format(
			url, 
			'ExportHistoryStatisticsToExecl.do', 
			Ext.util.Format.date(Ext.getCmp('dateSearchDateBegin').getValue(), 'Y-m-d 00:00:00'), 
			Ext.util.Format.date(Ext.getCmp('dateSearchDateEnd').getValue(), 'Y-m-d 23:59:59'),
			Ext.getCmp('comboPayType').getValue(),
			Ext.getCmp('textSearchValue').getValue(),
			Ext.getCmp('numberSearchValue').getValue(),
			Ext.getCmp('textTableAliasOrName').getValue(),
			Ext.getCmp('textTableAliasOrName').getValue(),
			Ext.getCmp('history_comboRegion').getValue(),
			sAdditionFilter,
			'historyOrder',
			1,
			Ext.getCmp('numberSearchSeqIdValue').getValue(),
			opening,
			ending
		);
	}else{
		url = '../../{0}?orderId={1}&dataType={2}&havingCond={3}&dataSource={4}&seqId={5}';
		url = String.format(
				url, 
				'ExportHistoryStatisticsToExecl.do', 
				Ext.getCmp('numberSearchValue').getValue(), 
				1,
				sAdditionFilter,
				'historyOrder',
				Ext.getCmp('numberSearchSeqIdValue').getValue()
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
		dutyRangeStat({statType : 1});
	}
});

var paymentBut = new Ext.ux.ImageButton({
	imgPath : "../../images/paymentRecord.png",
	imgWidth : 50,
	imgHeight : 50,
	tooltip : "交款记录",
	handler : function(btn) {
		dutyRangeStat({statType : 2});
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


function history_oBusinessHourData(c){
	if(c == null || c.type == null || typeof c.type == 'undefined')
		return;
	var data = {};
	var apmBegin = Ext.getCmp('history_comboBusinessBeginApm');
	var openingHour = Ext.getCmp('history_comboBegionHour');
	var openingMin = Ext.getCmp('history_comboBegionMin');
	var txtBusinessHourBegin = Ext.getCmp('txtBusinessHourBegin');
	
	var apmEnd = Ext.getCmp('history_comboBusinessEndApm');
	var endingHour = Ext.getCmp('history_comboEndHour');
	var endingMin = Ext.getCmp('history_comboEndMin');
	var txtBusinessHourEnd = Ext.getCmp('txtBusinessHourEnd');
	
	
	if(c.type == 'set'){
		data = c.data == null || typeof c.data == 'undefined' ? {} : c.data;
		
		txtBusinessHourBegin.show();
		txtBusinessHourEnd.show();
		
		openingHour.hide();
		endingHour.hide();
		openingMin.hide();
		endingMin.hide();
		apmBegin.hide();
		apmEnd.hide();
		Ext.getCmp('txtBusinessHourBeginText').hide();
		Ext.getCmp('txtBusinessHourEndText').hide();
		Ext.getCmp('txtBusinessMinBeginText').hide();
		Ext.getCmp('txtBusinessMinEndText').hide();		
		
		if(typeof data[2] != 'undefined'){
			
			txtBusinessHourBegin.setText('<font style="color:green; font-size:20px">'+data[2]+'</font>');
			txtBusinessHourEnd.setText('<font style="color:green; font-size:20px">'+data[3]+'</font>');
			
			beginTimes = data[2].split(':');
			endTimes = data[3].split(':');
			
			if(parseInt(beginTimes[0]) > 12){
				apmBegin.setValue(1);
				var openingHourValue = parseInt(beginTimes[0]) - 12;
				openingHourValue = openingHourValue > 9 ? openingHourValue+'' : '0'+openingHourValue;
				openingHour.setValue(openingHourValue);			
			}else{
				apmBegin.setValue(0);
				openingHour.setValue(beginTimes[0]);
			}
			
			if(parseInt(endTimes[0]) > 12){
				apmEnd.setValue(1);
				var endingHourValue = parseInt(endTimes[0]) - 12;
				endingHourValue = endingHourValue > 9 ? endingHourValue+'' : '0'+endingHourValue;
				endingHour.setValue(endingHourValue);		
			}else{
				apmEnd.setValue(0);
				endingHour.setValue(endTimes[0]);
			}
			
			openingMin.setValue(beginTimes[1]);
			
			endingMin.setValue(endTimes[1]);
			
		}else{
			txtBusinessHourBegin.setText('<font style="color:green; font-size:20px">00:00</font>');
			txtBusinessHourEnd.setText('<font style="color:green; font-size:20px">00:00</font>');
			
			if(data[0] == -2){
				txtBusinessHourBegin.hide();
				txtBusinessHourEnd.hide();
				
				openingHour.setValue('00');
				endingHour.setValue('00');
				openingMin.setValue('00');
				endingMin.setValue('00');
				apmBegin.setValue(0);
				apmEnd.setValue(0);
				
				openingHour.show();
				endingHour.show();
				openingMin.show();
				endingMin.show();
				apmBegin.show();
				apmEnd.show();
				Ext.getCmp('txtBusinessHourBeginText').show();
				Ext.getCmp('txtBusinessHourEndText').show();
				Ext.getCmp('txtBusinessMinBeginText').show();
				Ext.getCmp('txtBusinessMinEndText').show();
			}
			
		}
	}else if(c.type == 'get'){
		openingHour = openingHour.getValue();
		endingHour = endingHour.getValue();
		
		if(apmBegin.getValue() == 1){
			openingHour = parseInt(openingHour) + 12;
		}
		
		if(apmEnd.getValue() == 1){
			endingHour = parseInt(endingHour) + 12;
		}
		
		data.opening = openingHour + ':' + openingMin.getValue();
		
		data.ending = endingHour + ':' + endingMin.getValue();
		
		data.businessHourType = Ext.getCmp('history_comboBusinessHour').getValue();
		
		c.data = data;
	}
	return c;
};

var historyBill_combo_staffs = new Ext.form.ComboBox({
	id : 'historyBill_combo_staffs',
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
				Ext.getCmp('btnSreachForMainOrderGrid').handler();
			}
		}
	}
});

var history_setStatisticsDate = function(){
	if(sendToPageOperation){
		if(searchType){
			Ext.getCmp('btnBillCommonSearch').handler();
		}
		
		Ext.getCmp('btnBillHeightSearch').handler();
		
		Ext.getCmp('dateSearchDateBegin').setValue(sendToStatisticsPageBeginDate);
		Ext.getCmp('dateSearchDateEnd').setValue(sendToStatisticsPageEndDate);	
		
		Ext.getCmp('history_comboRegion').setValue(sendToStatisticsRegion);
		
		history_hours = sendToStatisticsPageHours;
		
		
		if(sendToStatisticsPayType == -1){
			Ext.getCmp('comboPayType').setValue(-1);
		}else{
			for (var i = 0; i < historyPayTypes.length; i++) {
				if(historyPayTypes[i].name == sendToStatisticsPayType){
					Ext.getCmp('comboPayType').setValue(historyPayTypes[i].id);
					break;
				}
			}
		}
		
		$('input[name="conditionRadio"]').each(function(){
			if(this.value == sendToStatisticsOperateType){
				Ext.getCmp(this.id).setValue(true);
				Ext.getCmp(this.id).fireEvent('check', Ext.getCmp(this.id), true);
			}
		});
		
		billQueryHandler({isRange:true});
		
		Ext.getCmp('txtBusinessHourBegin').setText('<font style="color:green; font-size:20px">'+history_hours.openingText+'</font>');
		Ext.getCmp('txtBusinessHourEnd').setText('<font style="color:green; font-size:20px">'+history_hours.endingText+'</font>');
		Ext.getCmp('history_comboBusinessHour').setValue(history_hours.hourComboValue);		
		
		sendToPageOperation = false;		
	}

};

var billsGrid;
var foodStatus;
var historyExtraBar;
var historyPayTypes;
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
			if(searchType){
				Ext.getCmp('btnSreachForMainOrderGrid').handler();
			}
		}
	});
	
	
	historyExtraBar = new Ext.Toolbar({
		id : 'historyExtraBar',
		hidden : true,
		height : 28,
		items : [{xtype : 'tbtext', text : '&nbsp;&nbsp;&nbsp;&nbsp;'},
			{xtype : 'tbtext', text : '台名/台号:'},
			{
				xtype : 'textfield',
				id : 'textTableAliasOrName',
				hidden : false,
				width : 100
			},
			{xtype : 'tbtext', text : '&nbsp;&nbsp;'},
			{xtype : 'tbtext', text : '收款方式:'},
			{xtype : 'tbtext', text : '&nbsp;&nbsp;'},
			{
				xtype : 'combo',
				forceSelection : true,
				width : 70,
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
						$.ajax({
							url : '../../OperatePayType.do',
							type : 'post',
							dataType : 'json',
							data : {
								dataSource : 'getByCond'
							},
							async : false,
							success : function(jr){
								historyPayTypes = jr.root;
								jr.root.unshift({id:-1, name:'全部'});
								thiz.getStore().loadData(jr.root);
								thiz.setValue(-1);								
							}
						});
					},
					select : function(){
						if(searchType){
							Ext.getCmp('btnSreachForMainOrderGrid').handler();
						}
					}
				}
			},
			{xtype : 'tbtext', text : '&nbsp;&nbsp;'},
			{xtype : 'tbtext', text : '操作人员:'},
			historyBill_combo_staffs,
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
						
						$.ajax({
							url : '../../OperateRegion.do',
							type : 'post',
							dataType : 'json',
							async : false,
							data : {
								dataSource : 'getByCond'
							},
							success : function(jr){
								for(var i = 0; i < jr.root.length; i++){
									data.push([jr.root[i]['id'], jr.root[i]['name']]);
								}
								thiz.store.loadData(data);
								thiz.setValue(-1);								
							},
							error : function(){
								thiz.store.loadData(data);
								thiz.setValue(-1);								
							}
						});
						
					},
					select : function(){
						if(searchType){
							Ext.getCmp('btnSreachForMainOrderGrid').handler();
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
	
	var historyHighTimeBar = new Ext.Toolbar({
		id : 'historyHighTimeBar',
		hidden : true,
		height : 28,
		items : [
			{xtype : 'tbtext', text : '&nbsp;&nbsp;&nbsp;&nbsp;'},
			{xtype : 'tbtext', text : '日期:&nbsp;&nbsp;'},
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
			{xtype : 'tbtext', text : '市别:'},
			{
				xtype : 'combo',
				forceSelection : true,
				width : 90,
				value : -1,
				id : 'history_comboBusinessHour',
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
						var data = [[-1,'全天']];
						$.ajax({
							url : '../../OperateBusinessHour.do',
							data : {
								dataSource : 'getByCond'
							},
							type : 'post',
							dataType : 'json',
							async : false,
							success : function(jr){
								for(var i = 0; i < jr.root.length; i++){
									data.push([jr.root[i]['id'], jr.root[i]['name'], jr.root[i]['opening'], jr.root[i]['ending']]);
								}
								data.push([-2,'自定义']);
								thiz.store.loadData(data);
								thiz.setValue(-1);
							},
							error : function(){
								thiz.store.loadData(data);
								thiz.setValue(-1);								
							}
						});
						
					},
					select : function(thiz, record, index){
						history_oBusinessHourData({data : record.json, type : 'set'});
						if(searchType){
							Ext.getCmp('btnSreachForMainOrderGrid').handler();
						}
						
					}
				}
			},
			{xtype : 'tbtext', text : '&nbsp;&nbsp;'},
			{xtype : 'tbtext', id : 'txtBusinessHourBegin', text : '<font style="color:green; font-size:20px">00:00</font>'},
			{
		    		xtype : 'combo',
		    		width : 50,
		    		value : 0,
		    		id : 'history_comboBusinessBeginApm',
		    		forceSelection : true,
					hideLabel : true,
					hidden : true,
		    		store : new Ext.data.SimpleStore({
						fields : [ 'value', 'text' ],
						data : [[0, '上午'], [1, '下午' ]]
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
			{
				xtype : 'combo',
				forceSelection : true,
				width : 40,
				value : '00',
				id : 'history_comboBegionHour',
				hidden : true,
				store : new Ext.data.SimpleStore({
					fields : [ 'value', 'text' ],
					data : [['00', '00' ], ['01', '01' ], ['02', '02' ], ['03', '03' ], ['04', '04' ], ['05', '05'], ['06', '06'], ['07', '07'], ['08', '08'], ['09', '09'], ['10', '10'], ['11', '11'], ['12', '12']]
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
			{xtype : 'tbtext', id:'txtBusinessHourBeginText',hidden : true, text : '时'},
			{
				xtype : 'combo',
				forceSelection : true,
				width : 40,
				value : '00',
				id : 'history_comboBegionMin',
				hidden : true,
				store : new Ext.data.SimpleStore({
					fields : [ 'value', 'text' ],
					data : [['00', '00'], ['30', '30']]
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
			{xtype : 'tbtext', id:'txtBusinessMinBeginText',hidden : true,text : '分'},
			{
				xtype : 'tbtext',
				hidden : false,
				text : '&nbsp;至&nbsp;'
			}, 
			{xtype : 'tbtext', id : 'txtBusinessHourEnd', text : '<font style="color:green; font-size:20px">00:00</font>'},
			{
		    		xtype : 'combo',
		    		width : 50,
		    		value : 0,
		    		id : 'history_comboBusinessEndApm',
		    		forceSelection : true,
					hideLabel : true,
					hidden : true,
		    		store : new Ext.data.SimpleStore({
						fields : [ 'value', 'text' ],
						data : [[0, '上午'], [1, '下午' ]]
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
			{
				xtype : 'combo',
				forceSelection : true,
				width : 40,
				value : '00',
				id : 'history_comboEndHour',
				hidden : true,
				store : new Ext.data.SimpleStore({
					fields : [ 'value', 'text' ],
					data : [['00', '00' ], ['01', '01' ], ['02', '02' ], ['03', '03' ], ['04', '04' ], ['05', '05'], ['06', '06'], ['07', '07'], ['08', '08'], ['09', '09'], ['10', '10'], ['11', '11'], ['12', '12']]
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
			{xtype : 'tbtext', id:'txtBusinessHourEndText',hidden : true, text : '时'},
			{
				xtype : 'combo',
				forceSelection : true,
				width : 40,
				value : '00',
				id : 'history_comboEndMin',
				hidden : true,
				store : new Ext.data.SimpleStore({
					fields : [ 'value', 'text' ],
					data : [['00', '00'], ['30', '30']]
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
			{xtype : 'tbtext', id:'txtBusinessMinEndText', hidden : true,text : '分'},
			{xtype : 'tbtext', text : '&nbsp;&nbsp;'}
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
			width : 100
		},
		{ xtype:'tbtext', text:'&nbsp;&nbsp;'},
		{
			xtype : 'tbtext',
			text : '&nbsp;&nbsp;&nbsp;&nbsp;流水号:'
		}, 
		{ xtype:'tbtext', text:'&nbsp;&nbsp;'},
		{
			xtype : 'numberfield',
			id : 'numberSearchSeqIdValue',
			width : 100
		},
		{ xtype:'tbtext', text:'&nbsp;&nbsp;'},
		{
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
		},{ xtype:'tbtext', text:'&nbsp;&nbsp;'},{
			xtype : 'radio',
			name : 'conditionRadio',
			boxLabel : '会员价',
			inputValue : 8,
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
	    		
	    		Ext.getCmp('historyExtraBar').show();
	    		Ext.getCmp('historyHighTimeBar').show();
	    		
	    		billsGrid.setHeight(billsGrid.getHeight()-56);
	    		billsGrid.syncSize(); //强制计算高度
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
	    		
	    		Ext.getCmp('historyExtraBar').hide();
	    		Ext.getCmp('historyHighTimeBar').hide();
	    		
	    		Ext.getCmp('textSearchValue').setValue();
	    		Ext.getCmp('textTableAliasOrName').setValue();
	    		
	    		history_dateCombo.setValue(1);
	    		history_dateCombo.fireEvent('select', history_dateCombo,null,1);
	    		
	    		
	    		Ext.getCmp('history_comboBusinessHour').setValue(-1);
	    		Ext.getCmp('history_comboBusinessHour').fireEvent('select', Ext.getCmp('history_comboBusinessHour'),-1,-1);
	    		
	    		Ext.getCmp('comboPayType').setValue(-1);
	    		Ext.getCmp('history_comboRegion').setValue(-1);
	    		Ext.getCmp('historyBill_combo_staffs').setValue(-1);
	    		

	    		
	    		billsGrid.setHeight(billsGrid.getHeight()+56);
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
			['流水号', 'seqId',70],
			['台号', 'table.alias', 120, null, function(v,m,r){
				if(r.get('table.name') != ''){
					return v + '(' + r.get("table.name") + ')';
				}else{
					return v;
				}
			}],
			['区域', 'table.region.name'],
			['日期', 'orderDateFormat', 150],
//			['账单类型', 'categoryText',,'center'],
			['结账方式', 'settleTypeText', null, 'center'],
			['收款方式', 'payTypeText', null, 'center'],
			['优惠劵金额', 'couponPrice', null, 'right', function(v){
				if(!isNaN(v)){
					return Ext.ux.txtFormat.gridDou(v);
				}else{
					return v; 
				}
			}],
			['应收', 'totalPrice', null, 'right', Ext.ux.txtFormat.gridDou],
			['实收', 'actualPrice', null, 'right', Ext.ux.txtFormat.gridDou],
			['状态', 'statusText', null, 'center', function(v, m, r){
				if(r.get("statusValue") == 2){
					return '<font color=\"#FF0000\">反结账</font>';
				}else{
					return v;
				}
			}],
			['备注', 'comment', null, 'center', 'commentTip'],
			['操作', 'operator', 140, 'center', billOpt]
		],
		OrderRecord.getKeys(),
		[['dataType', 1]],
		GRID_PADDING_LIMIT_20,
		'',
		[billsGridTbar,historyHighTimeBar, historyExtraBar]
	);
	billsGrid.region = 'center';
	billsGrid.on('render', function(){
		if(!sendToPageOperation){
			history_dateCombo.setValue(1);
			history_dateCombo.fireEvent('select', history_dateCombo,null,1);
		}
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
			//FIXME
			paymentBut,
			{xtype : 'tbtext', text : '&nbsp;&nbsp;'},
			shiftStatBut, 
			{xtype:'tbtext',text:'&nbsp;&nbsp;&nbsp;'},
			dailySettleStatBut
			]
		})
	});
	billHistoryOnLoad();
//	billQueryHandler();
	
	history_setStatisticsDate();
	
	Ext.getCmp('history').updateStatisticsDate = history_setStatisticsDate;
	
});

