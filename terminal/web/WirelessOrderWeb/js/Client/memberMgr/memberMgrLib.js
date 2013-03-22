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
			keys : [{
				key : Ext.EventObject.ESC,
				scope : this,
				fn : function(){
					rechargeWin.hide();
				}
			}],
			listeners : {
				hide : function(thiz){
					thiz.body.update('');
				},
				show : function(thiz){
					var sd = Ext.ux.getSelData(memberBasicGrid);
					thiz.center();
					thiz.load({
						url : '../window/client/recharge.jsp',
						scripts : true,
						params : {
							memberCard : !sd ? '' : sd['memberCard.aliasID']
						}
					});
				}
			},
			bbar : ['->', {
				text : '充值',
				iconCls : 'icon_tb_recharge',
				handler : function(e){
					// 跨域调用充值方法
					rechargeControlCenter({
						callback : function(_c){
							rechargeWin.hide();
							var st = Ext.getCmp('comboMemberSearchType');
							st.setValue(2);
							st.fireEvent('select', st, null, null);
							var n = Ext.getCmp('numberSearchValueByNumber');
							n.setValue(_c.data.memberCardAlias);
							Ext.getCmp('btnSearchMember').handler();
						}
					});
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