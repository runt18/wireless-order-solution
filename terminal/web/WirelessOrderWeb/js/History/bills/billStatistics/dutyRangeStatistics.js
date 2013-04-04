function dutyRangeStatPrintHandler(rowIndex) {
	var gs = Ext.ux.getSelData(dutyRangeStatPanel);
	Ext.Ajax.request({
		url : '../../PrintOrder.do',
		params : {
			'pin' : pin,
//			'printTmpShift' : 1,
			'printType' : 5,
			'onDuty' : gs['onDutyFormat'],
			'offDuty' : gs['offDutyFormat']
		},
		success : function(response, options) {
			Ext.ux.showMsg(Ext.decode(response.responseText));
		},
		failure : function(response, options) {
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
				title : '营业统计 -- <font style="color:green;">历史</font> -- 交班人:&nbsp;<font style="color:red;">' + gs['staff.name'] + '</font>',
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
						dutyRangeStatWin.hide();
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
						gs = Ext.ux.getSelData(dutyRangeStatPanel);
						thiz.load({
							autoLoad : false,
							url : '../window/history/businessStatistics.jsp',
							scripts : true,
							nocache : true,
							text : '功能加载中, 请稍后......',
							params : {
								d : '_' + new Date().getTime(),
								dataSource : 'history',
								queryPattern : 2,
								onDuty : gs['onDuty'],
								offDuty : gs['offDuty']
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

function dutyRangeStatPanelInit(){
	var beginDate = new Ext.form.DateField({
		xtype : 'datefield',		
		format : 'Y-m-d',
		width : 100,
		maxValue : new Date(),
		readOnly : true,
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
		readOnly : true,
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
				gs.baseParams['onDuty'] = beginDate.getValue().format('Y-m-d 00:00:00');
				gs.baseParams['offDuty'] = endDate.getValue().format('Y-m-d 23:59:59');
				gs.load({
					params : {
						start : 0,
						limit : 10
					}
				});
			}
		}]
	});
	
	dutyRangeStatPanel = createGridPanel(
		'',
		'',
		'',
		'',
		'../../DutyRangeStat.do',
		[[true, false, false, true], 
	     ['交班人', 'staff.name', 60],
	     ['开始时间', 'onDutyFormat'], 
	     ['结束时间', 'offDutyFormat'], 
	     ['操作','Operation', 100, 'center', 'dutyRangeStatPanelOperationRenderer']
		],
		['staff.name', 'onDuty', 'offDuty', 'onDutyFormat', 'offDutyFormat'],
		[['pin', pin], ['restaurantID', restaurantID], ['dataSource', 'history'], ['isPaging', true]],
		10,
		null,
		dutyRangeStatPanelTbar
	);
	dutyRangeStatPanel.frame = false;
	dutyRangeStatPanel.border = false; 
}

function dutyRangeStatWinInit(){
	if(!dutyRangeStatPanel){
		dutyRangeStatPanelInit();
	}
	dutyRangeStatWin = new Ext.Window({
		title : '交班记录',
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
				dutyRangeStatWin.hide();
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
			show : function(){
//				Ext.getCmp('btnRefreshDutyStatRange').handler();
			}
		}
	});
}

function dutyRangeStat(){
	if(!dutyRangeStatWin){
		dutyRangeStatWinInit();
	}
	dutyRangeStatWin.show();
	dutyRangeStatWin.center();
}