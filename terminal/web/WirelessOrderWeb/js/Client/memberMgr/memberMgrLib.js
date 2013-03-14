function initRechargeWin(){
	var rechargeWin = Ext.getCmp('rechargeWin');
	if(!rechargeWin){
		rechargeWin = new Ext.Window({
			id : 'rechargeWin',
			title : '会员充值',
			closable : false,
			modal : true,
			resizable : false,
			width : 600,
			height : 300,
			layout : 'border',
			items : [{
				xtype : 'panel',
				border : false,
				region : 'center',
				autoLoad : {
					url : '../window/member/recharge.jsp',
					scripts : true
				}
			}],
			keys : [{
				key : Ext.EventObject.ESC,
				scope : this,
				fn : function(){
					rechargeWin.hide();
				}
			}],
			listeners : {
				show : function(thiz){
					thiz.center();
				}
			},
			bbar : ['->', {
				text : '关闭',
				iconCls : 'btn_close',
				handler : function(e){
					rechargeWin.hide();
				}
			}]
		});
	}
}

function rechargeHandler(){
	initRechargeWin();
	var rechargeWin = Ext.getCmp('rechargeWin');
	rechargeWin.show();
}