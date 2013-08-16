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
			
			'printType' : 5,
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
				title : '营业统计 -- <font style="color:green;">当日</font> -- 交班人:&nbsp;<font style="color:red;">' + gs['staff.name'] + '</font>',
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

function dutyRangePanelOperationRenderer(){
	return '<a href="javascript:dutyRangeStatDetalHandler()">详细</a>'
		   + '&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;'
		   + '<a href="javascript:dutyRangeStatPrintHandler()">补打</a>';
}

function dutyRangePanelInit(){
	dutyRangePanelTbar = new Ext.Toolbar({
		items : ['->', {
			text : '刷新',
			id : 'btnRefreshDutyRange',
			iconCls : 'btn_refresh',
			handler : function(){
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
			}
		}]
	});
	
	dutyRangePanel = createGridPanel(
		'',
		'',
		'',
		'',
		'../../DutyRangeStat.do',
		[[true, false, false, false], 
	     ['交班人', 'staff.name', 60],
	     ['开始时间', 'onDutyFormat'], 
	     ['结束时间', 'offDutyFormat'], 
	     ['操作','Operation', 100, 'center', 'dutyRangePanelOperationRenderer']
		],
		['staff.name', 'onDuty', 'offDuty', 'onDutyFormat', 'offDutyFormat'],
		[ ['dataSource', 'today']],
		0,
		null,
		dutyRangePanelTbar
	);
	dutyRangePanel.frame = false;
	dutyRangePanel.border = false; 
}

function dutyRangeWinInit(){
	if(!dutyRangePanel){
		dutyRangePanelInit();
	}
	dutyRangeWin = new Ext.Window({
		title : '交班记录',
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
				dutyRangeWin.hide();
			}
		}],
		keys : [{
			key : Ext.EventObject.ESC,
			scope : this,
			fn : function(){
				dutyRangeWin.hide();
			}
		}],
		listeners : {
			show : function(){
				Ext.getCmp('btnRefreshDutyRange').handler();
			}
		}
	});
}

function dutyRangeSub(){
	if(!dutyRangeWin){
		dutyRangeWinInit();
	}
	dutyRangeWin.show();
	dutyRangeWin.center();
}
