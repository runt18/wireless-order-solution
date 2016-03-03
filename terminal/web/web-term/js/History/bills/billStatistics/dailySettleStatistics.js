
function dailySettleStatDetalHandler(){
	var gs = Ext.ux.getSelData(dailySettleStatGrid);
	if(gs != false){
		var dailySettleStatWin = Ext.getCmp('dailySettleStatWin');
		if(!dailySettleStatWin){
			dailySettleStatWin = new Ext.Window({
				title : '营业统计 -- <font style="color:green;">历史</font>',
				id : 'dailySettleStatWin',
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
						dailySettleStatWin.hide();
					}
				}],
				keys : [{
					key : Ext.EventObject.ESC,
					scope : this,
					fn : function(){
						dailySettleStatWin.hide();
					}
				}],
				listeners : {
					hide : function(thiz){
						thiz.body.update('');
					},
					show : function(thiz){
						gs = Ext.ux.getSelData(dailySettleStatGrid);
						thiz.load({
							autoLoad : false,
							url : '../window/history/businessStatistics.jsp',
							scripts : true,
							nocache : true,
							text : '功能加载中, 请稍后......',
							params : {
								d : '_' + new Date().getTime(),
								queryPattern : 2,
								dataSource : 'history',
								onDuty : gs['onDuty'],
								offDuty : gs['offDuty']
							}
						});
					}
				}
			});
		}
		dailySettleStatWin.show();
		dailySettleStatWin.center();
	}
};

function dailySettleStatPrintHandler() {
	var gs = Ext.ux.getSelData(dailySettleStatGrid);
	if(gs != false){
		var tempMask = new Ext.LoadMask(document.body, {
			msg : '正在打印请稍候.......',
			remove : true
		});
		tempMask.show();
		Ext.Ajax.request({
			url : '../../PrintOrder.do',
			params : {
				
				'printType' : 8,
				onDuty : gs['onDutyFormat'],
				offDuty : gs['offDutyFormat']
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
	}
};


function dailySettleStatResultGridRenderer(value, cellmeta, record, rowIndex, columnIndex, store){
	return '<a href=\"javascript:dailySettleStatDetalHandler()">详细</a>'
	+ '&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;'
	+ '<a href=\"javascript:dailySettleStatPrintHandler()">打印</a>';
}


function dailySettleStatGridInit(){
	var onDuty = new Ext.form.DateField({
		xtype : 'datefield',		
		format : 'Y-m-d',
		width : 100,
		maxValue : new Date(),
		readOnly : false,
		allowBlank : false,
		listeners : {
			blur : function(thiz){									
//				Ext.ux.checkDuft(true, thiz.getId(), offDuty.getId());
			}
		}
	});
	var offDuty = new Ext.form.DateField({
		xtype : 'datefield',
		format : 'Y-m-d',
		width : 100,
		maxValue : new Date(),
		readOnly : false,
		allowBlank : false,
		listeners : {
			blur : function(thiz){									
//				Ext.ux.checkDuft(false, onDuty.getId(), thiz.getId());
			}
		}
	});
	var dateCombo = Ext.ux.createDateCombo({
		beginDate : onDuty,
		endDate : offDuty,
		callback : function(){
			Ext.getCmp('btnSearchBydDilySettleStatGrid').handler();
		}
	});
	
	var dailySettleStatGridTbar = new Ext.Toolbar({
		height : 26,
		items : [{
			xtype : 'tbtext',
			text : '日期:'
		}, dateCombo, {
			xtype : 'tbtext',
			text : '&nbsp;'
		},onDuty, {
			xtype : 'tbtext',
			text : '&nbsp;至&nbsp;'
		}, offDuty, '->', {
			text : '搜索',
			id : 'btnSearchBydDilySettleStatGrid',
			iconCls : 'btn_search',
			handler : function(){
				if(!onDuty.isValid() || !offDuty.isValid()){
					return;
				}
				var gs = dailySettleStatGrid.getStore();
				gs.baseParams['onDuty'] = Ext.util.Format.date(onDuty.getValue(), 'Y-m-d 00:00:00');
				gs.baseParams['offDuty'] = Ext.util.Format.date(offDuty.getValue(), 'Y-m-d 23:59:59');
				gs.load({
					params : {
						start : 0,
						limit : 10
					}
				});
			}
		}]
	});
	
	dailySettleStatGrid = createGridPanel(
		'',
		'',
		'',
		'',
		'../../dailySettleStat.do',
		[[true, false, false, true], 
		 ['操作人', 'staffName'],
		 ['开始时间', 'onDutyFormat'],
		 ['结束时间', 'offDutyFormat'],
		 ['操作', 'operator', 130, 'center', dailySettleStatResultGridRenderer]
		],
		['staffName', 'onDuty', 'onDutyFormat', 'offDuty', 'offDutyFormat'],
		[ ['isPaging', true], ['restaurantID', restaurantID]],
		15,
		null,
		dailySettleStatGridTbar
	);
	dailySettleStatGrid.frame = false;
	dailySettleStatGrid.border = false;	
}

function dailySettleStat(){
	if(!dailySettleStatWin){
		if(!dailySettleStatGrid){
			dailySettleStatGridInit();
		}
		dailySettleStatWin = new Ext.Window({
			title : '日结记录',
			width : 700,
			height : 410,
			resizable : false,
			modal : true,
			closable : false,
			layout : 'fit',
			items : [dailySettleStatGrid],
			bbar : ['->', {
				text : '关闭',
				iconCls : 'btn_close',
				handler : function(){
					dailySettleStatWin.hide();
				}
			}],
			keys : [{
				key : Ext.EventObject.ESC,
				scope : this,
				fn : function(){
					dailySettleStatWin.hide();
				}
			}]
		});
	}
	
	dailySettleStatWin.show();
	dailySettleStatWin.center();
};
