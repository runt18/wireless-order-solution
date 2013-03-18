function initRechargeWin(){
	var rechargeWin = Ext.getCmp('rechargeWin');
	if(!rechargeWin){
		rechargeWin = new Ext.Window({
			id : 'rechargeWin',
			title : '会员充值',
			closable : false,
			modal : true,
			resizable : false,
			width : 650,
			height : 430,
			layout : 'border',
			items : [{
				xtype : 'panel',
				border : false,
				region : 'center',
				autoLoad : {
					url : '../window/client/recharge.jsp',
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
					if(typeof rechargeBindMemberData != 'undefined')
						rechargeBindMemberData({});
				}
			},
			bbar : ['->', {
				text : '充值',
				iconCls : ' ',
				handler : function(e){
					rechargeControlCenter();
				}
			}, '-', {
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