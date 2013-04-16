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
					var data = Ext.ux.getSelData(memberBasicGrid);
					var cardAlias = data != false && eval(data['memberType.attributeValue'] == 0) && eval(data['client.clientTypeID'] != 0) ? data['memberCard.aliasID'] : '';
					thiz.center();
					thiz.load({
						url : '../window/client/recharge.jsp',
						scripts : true,
						params : {
							memberCard : cardAlias
						}
					});
				}
			},
			bbar : [{
				xtype : 'checkbox',
				id : 'chbPrintRecharge',
				checked : true,
				boxLabel : '打印充值信息'
			}, '->', {
				text : '充值',
				iconCls : 'icon_tb_recharge',
				handler : function(e){
					// 跨域调用充值方法
					rechargeControlCenter({
						isPrint : Ext.getCmp('chbPrintRecharge').getValue(),
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
/**
 * 充值
 * @returns
 */
function rechargeHandler(){
	initRechargeWin();
	var rechargeWin = Ext.getCmp('rechargeWin');
	rechargeWin.show();
}
/**
 * 会员操作明细
 */
function queryMemberOperationHandler(){
	var mr_queryMemberOperationWin = Ext.getCmp('mr_queryMemberOperationWin');
	if(!mr_queryMemberOperationWin){
		mr_queryMemberOperationWin = new Ext.Window({
			id : 'mr_queryMemberOperationWin',
			title : '会员操作明细',
			modal : true,
			closable : false,
			resizable : false,
			width : 1000,
			height : 500,
			keys : [{
				key : Ext.EventObject.ESC,
				scope : this,
				fn : function(){
					mr_queryMemberOperationWin.hide();
				}
			}],
			bbar : ['->', {
				text : '关闭',
				iconCls : 'btn_close',
				handler : function(e){
					mr_queryMemberOperationWin.hide();
				}
			}],
			listeners : {
				hide : function(thiz){
					thiz.body.update('');
				},
				show : function(thiz){
					var data = Ext.ux.getSelData(memberBasicGrid);
					var memberCard = !data ? '' : data['memberCard.aliasID'] ;
					thiz.center();
					thiz.load({
						url : '../window/client/memberOperation.jsp',
						scripts : true,
						params : {
							memberCard : memberCard,
							modal : true
						}
					});
				}
			}
		});
	}
	mr_queryMemberOperationWin.show();
}
/**
 * 会员操作汇总
 */
function queryMemberConsumeSummaryHandler(){
	var mr_queryMemberConsumeSummaryWin = Ext.getCmp('mr_queryMemberConsumeSummaryWin');
	if(!mr_queryMemberConsumeSummaryWin){
		mr_queryMemberConsumeSummaryWin = new Ext.Window({
			id : 'mr_queryMemberConsumeSummaryWin',
			title : '会员操作汇总',
			modal : true,
			closable : false,
			resizable : false,
			width : 800,
			height : 500,
			keys : [{
				key : Ext.EventObject.ESC,
				scope : this,
				fn : function(){
					mr_queryMemberConsumeSummaryWin.hide();
				}
			}],
			bbar : ['->', {
				text : '关闭',
				iconCls : 'btn_close',
				handler : function(e){
					mr_queryMemberConsumeSummaryWin.hide();
				}
			}],
			listeners : {
				hide : function(thiz){
					thiz.body.update('');
				},
				show : function(thiz){
					var data = Ext.ux.getSelData(memberBasicGrid);
					var memberCard = !data ? '' : data['memberCard.aliasID'] ;
					thiz.center();
					thiz.load({
						url : '../window/client/memberConsumeSummary.jsp',
						scripts : true,
						params : {
							memberCard : memberCard
						}
					});
				}
			}
		});
	}
	mr_queryMemberConsumeSummaryWin.show();
};