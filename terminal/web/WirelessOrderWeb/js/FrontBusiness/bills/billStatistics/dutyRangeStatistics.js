function dutyRangeStatPrintHandler(rowIndex) {
	var tempMask = new Ext.LoadMask(document.body, {
		msg : '正在打印请稍候.......',
		remove : true
	});
	tempMask.show();
	var gs = Ext.ux.getSelData(dutyRangePanel);
	Ext.Ajax.request({
		url : '../../PrintOrder.do',
		params : {
			'printType' : statType == 1?5:12,
			'onDuty' : gs['onDutyFormat'],
			'offDuty' : gs['offDutyFormat']
		},
		success : function(response, options) {
			tempMask.hide();
			Ext.ux.showMsg(Ext.decode(response.responseText));
		},
		failure : function(response, options){
			tempMask.hide();
			Ext.ux.showMsg(Ext.decode(response.responseText));
		}
	});
};

function dutyRangeStatDetalHandler(){
	var gs = Ext.ux.getSelData(dutyRangePanel);
	if(gs != false){
		var dutyRangeStatWin = Ext.getCmp('dutyRangeStatWin');
		if(!dutyRangeStatWin){
			dutyRangeStatWin = new Ext.Window({
				title : '营业统计 -- <font style="color:green;">当日</font> -- '+(statType == 1?'交班人':'交款人')+':&nbsp;<font style="color:red;">' + gs['staffName'] + '</font>',
				id : 'dutyRangeStatWin',
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
						dutyRangeStatWin.destroy();
					}
				}],
				keys : [{
					key : Ext.EventObject.ESC,
					scope : this,
					fn : function(){
						dutyRangeStatWin.hide();
					}
				}],
				listeners : {
					hide : function(thiz){
						thiz.body.update('');
					},
					show : function(thiz){
						gs = Ext.ux.getSelData(dutyRangePanel);
						thiz.load({
							autoLoad : false,
							url : '../window/history/businessStatistics.jsp',
							scripts : true,
							nocache : true,
							text : '功能加载中, 请稍后......',
							params : {
								d : '_' + new Date().getTime(),
								dataSource : 'today',
								queryPattern : statType == 2? 5 : 2,
								onDuty : statType == 1?gs['onDuty']:gs['onDutyFormat'],
								offDuty : statType == 1?gs['offDuty']:gs['offDutyFormat'],
								businessStatic : statType
							}
						});
					}
				}
			});
		}
		dutyRangeStatWin.show();
		dutyRangeStatWin.center();
	}
};

function dutyRangePanelOperationRenderer(){
	return '<a href="javascript:dutyRangeStatDetalHandler()">详细</a>'
		   + '&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;'
		   + '<a href="javascript:dutyRangeStatPrintHandler()">补打</a>';
}

function dutyRangePanelInit(c){
	var url = '';
	//交班
	if(eval(c.statType == 1)){
		url = '../../DutyRangeStat.do';
	}else if(eval(c.statType == 2)){ //交款
		url = '../../PaymentStat.do';
	}
	dutyRangePanelTbar = new Ext.Toolbar({
		items : ['->', {
			text : '刷新',
			id : 'btnRefreshDutyRange',
			iconCls : 'btn_refresh',
			handler : function(){
				if(eval(c.statType == 1)){
					loadShiftDuty();
					// 清除后台返回的交班记录中全天和本班次记录段
					var tempDate = {root:[]};
					if(shiftDutyOfToday.root.length > 2){
						for(var i = 0; i < shiftDutyOfToday.root.length; i++){
							if(i != 0 && i != shiftDutyOfToday.root.length -1){
								tempDate.root.push(shiftDutyOfToday.root[i]);
							}
						}
					}
					
					dutyRangePanel.getStore().loadData(tempDate);
				}else if(eval(c.statType == 2)){
					loadPayment();
					dutyRangePanel.getStore().loadData(paymentOfToday);
				}

			}
		}]
	});
	
	dutyRangePanel = createGridPanel(
		'',
		'',
		'',
		'',
		url,
		[[true, false, false, false], 
	     [c.statType == 1?'交班人':'交款人', 'staffName', 60],
	     ['开始时间', 'onDutyFormat'], 
	     ['结束时间', 'offDutyFormat'], 
	     ['操作','Operation', 100, 'center', 'dutyRangePanelOperationRenderer']
		],
		['staffName','onDuty','offDuty','onDutyFormat', 'offDutyFormat'],
		[ ['dataSource', 'today']],
		0,
		null,
		dutyRangePanelTbar
	);
	dutyRangePanel.frame = false;
	dutyRangePanel.border = false; 
}

function dutyRangeWinInit(c){
	if(!dutyRangePanel || dutyRangePanel == null){
		dutyRangePanelInit(c);
	}
	dutyRangeWin = new Ext.Window({
		title : c.statType == 1?'交班记录':'交款记录',
		layout : 'fit',
		resizable : false,
		modal : true,
		closable : false,
		constrainHeader : true,
		width : 600,
		height : 410,
		items : [dutyRangePanel],
		bbar : ['->', {
			text : '关闭',
			iconCls : 'btn_close',
			handler : function(){
				dutyRangePanel.destroy();
				dutyRangeWin.destroy();
				dutyRangeWin = null;
				dutyRangePanel = null;
			}
		}],
		keys : [{
			key : Ext.EventObject.ESC,
			scope : this,
			fn : function(){
				dutyRangePanel.destroy();
				dutyRangeWin.destroy();
				dutyRangeWin = null;
				dutyRangePanel = null;
			}
		}],
		listeners : {
			show : function(){
				Ext.getCmp('btnRefreshDutyRange').handler();
			}
		}
	});
}

function dutyRangeSub(c){
	statType = c.statType;
	if(!dutyRangeWin || dutyRangeWin == null){
		dutyRangeWinInit(c);
	}
	dutyRangeWin.show();
	dutyRangeWin.center();
}



