function dutyRangeStatPrintHandler(rowIndex) {
	var gs = Ext.ux.getSelData(dutyRangeStatPanel);
	var tempMask = new Ext.LoadMask(document.body, {
		msg : '正在打印请稍候.......',
		remove : true
	});
	tempMask.show();
	Ext.Ajax.request({
		url : '../../PrintOrder.do',
		params : {
			
			'printType' : statType == 1 ? 7 : 13,
			'onDuty' : gs['onDutyFormat'],
			'offDuty' : gs['offDutyFormat']
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

function dutyRangeStatDetalHandler(){
	var gs = Ext.ux.getSelData(dutyRangeStatPanel);
	if(gs != false){
		var dutyRangeStatWin = Ext.getCmp('dutyRangeStatWin');
		if(!dutyRangeStatWin){
			dutyRangeStatWin = new Ext.Window({
				title : '营业统计 -- <font style="color:green;">历史</font> -- '+(statType == 1?'交班人':'交款人')+':&nbsp;<font style="color:red;">' + gs['staffName'] + '</font>',
				id : 'dutyRangeStatWin',
				width : 885,
				height : 600,
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
						dutyRangeStatWin.destroy();
					}
				}],
				listeners : {
					hide : function(thiz){
						thiz.body.update('');
					},
					show : function(thiz){
						gs = Ext.ux.getSelData(dutyRangeStatPanel);
						thiz.load({
							autoLoad : false,
							url : '../window/history/businessStatistics.jsp',
							scripts : true,
							nocache : true,
							text : '功能加载中, 请稍后......',
							params : {
								d : '_' + new Date().getTime(),
								dataSource : statType == 2?'paymentHistory':'history',
								queryPattern : statType == 2? 5 : 2,
								onDuty : statType == 1?gs['onDuty']:gs['onDutyFormat'],
								offDuty : statType == 1?gs['offDuty']:gs['offDutyFormat'],
								businessStatic : statType,
								staffId : statType == 2?gs['staffId']:''
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

function dutyRangeStatPanelOperationRenderer(){
	return '<a href="javascript:dutyRangeStatDetalHandler()">详细</a>'
		   + '&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;'
		   + '<a href="javascript:dutyRangeStatPrintHandler()">补打</a>';
}

function dutyRangeStatPanelInit(c){
	var beginDate = new Ext.form.DateField({
		xtype : 'datefield',		
		format : 'Y-m-d',
		width : 100,
		maxValue : new Date(),
		readOnly : false,
		allowBlank : false,
		listeners : {
			blur : function(thiz){									
				Ext.ux.checkDuft(true, thiz.getId(), endDate.getId());
			}
		}
	});
	var endDate = new Ext.form.DateField({
		xtype : 'datefield',
		format : 'Y-m-d',
		width : 100,
		maxValue : new Date(),
		readOnly : false,
		allowBlank : false,
		listeners : {
			blur : function(thiz){									
				Ext.ux.checkDuft(false, beginDate.getId(), thiz.getId());
			}
		}
	});
	var dateCombo = Ext.ux.createDateCombo({
		beginDate : beginDate,
		endDate : endDate,
		callback : function(){
			Ext.getCmp('btnRefreshDutyStatRange').handler();
		}
	});
	dutyRangeStatPanelTbar = new Ext.Toolbar({
		items : [{
			xtype:'tbtext',
			text:'日期:'
		}, dateCombo, {
			xtype:'tbtext',
			text:'&nbsp;'
		}, beginDate, {
			xtype:'tbtext',
			text:'&nbsp;至&nbsp;'
		}, endDate, {
			xtype:'tbtext',
			text:'&nbsp;'
		}, '->', {
			text : '搜索',
			id : 'btnRefreshDutyStatRange',
			iconCls : 'btn_search',
			handler : function(){
				if(!beginDate.isValid() || !endDate.isValid()){
					return;
				}
				var gs = dutyRangeStatPanel.getStore();
				gs.baseParams['onDuty'] = Ext.util.Format.date(beginDate.getValue(), 'Y-m-d 00:00:00');
				gs.baseParams['offDuty'] = Ext.util.Format.date(endDate.getValue(), 'Y-m-d 23:59:59');
				gs.load({
					params : {
						start : 0,
						limit : 10
					}
				});
			}
		}]
	});
	var url = '';
	//交班
	if(eval(c.statType == 1)){
		url = '../../DutyRangeStat.do';
	}else if(eval(c.statType == 2)){ //交款
		url = '../../PaymentStat.do';
	}
	
	dutyRangeStatPanel = createGridPanel(
		'',
		'',
		'',
		'',
		url,
		[[true, false, false, true], 
	     [c.statType == 1?'交班人':'交款人', 'staffName', 60],
	     ['开始时间', 'onDutyFormat'], 
	     ['结束时间', 'offDutyFormat'], 
	     ['操作','Operation', 100, 'center', dutyRangeStatPanelOperationRenderer]
		],
		['staffId','staffName', 'onDuty', 'offDuty', 'onDutyFormat', 'offDutyFormat'],
		[ ['dataSource', 'history'], ['isPaging', true]],
		10,
		null,
		dutyRangeStatPanelTbar
	);
	dutyRangeStatPanel.frame = false;
	dutyRangeStatPanel.border = false; 
}

function dutyRangeStatWinInit(c){
	if(!dutyRangeStatPanel || dutyRangeStatPanel == null){
		dutyRangeStatPanelInit(c);
	}
	dutyRangeStatWin = new Ext.Window({
		title : c.statType == 1?'交班记录':'交款记录',
		layout : 'fit',
		resizable : false,
		modal : true,
		closable : false,
		constrainHeader : true,
		width : 600,
		height : 410,
		items : [dutyRangeStatPanel],
		bbar : ['->', {
			text : '关闭',
			iconCls : 'btn_close',
			handler : function(){
				dutyRangeStatWin.destroy();
				dutyRangeStatPanel.destroy();
				dutyRangeStatWin = null;
				dutyRangeStatPanel = null;
			}
		}],
		keys : [{
			key : Ext.EventObject.ESC,
			scope : this,
			fn : function(){
				dutyRangeStatWin.destroy();
				dutyRangeStatPanel.destroy();
				dutyRangeStatWin = null;
				dutyRangeStatPanel = null;
			}
		}],
		listeners : {
			show : function(){
//				Ext.getCmp('btnRefreshDutyStatRange').handler();
			}
		}
	});
}

function dutyRangeStat(c){	
	statType = c.statType;
	if(!dutyRangeStatWin || dutyRangeStatWin == null){
		dutyRangeStatWinInit(c);
	}
	dutyRangeStatWin.show();
	dutyRangeStatWin.center();
}