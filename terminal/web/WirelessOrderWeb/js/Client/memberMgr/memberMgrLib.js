/**********************************************************************/
function insertMemberHandler(){
	memberOperationHandler({
		type : Ext.ux.otype['insert']
	});
};

function updateMemberHandler(){
	memberOperationHandler({
		type : Ext.ux.otype['update']
	});
};

function deleteMemberHandler(){
	memberOperationHandler({
		type : Ext.ux.otype['delete']
	});
};

function memberOperationHandler(c){
	if(c == null || typeof c == 'undefined' || typeof c.type == 'undefined'){
		return;
	}
	memberBasicWin.otype = c.type;
	
	if(c.type == Ext.ux.otype['insert']){
		memberBasicWin.setTitle('添加会员资料');
		memberBasicWin.show();
	}else if(c.type == Ext.ux.otype['update']){
		var data = Ext.ux.getSelData(memberBasicGrid);
		if(!data){
			Ext.example.msg('提示', '请选中一条会员记录再进行操作.');
			return;
		}
		memberBasicWin.setTitle('修改会员资料');
		memberBasicWin.show();
	}else if(c.type == Ext.ux.otype['delete']){
		var data = Ext.ux.getSelData(memberBasicGrid);
		if(!data){
			Ext.example.msg('提示', '请选中一条会员记录再进行操作.');
			return;
		}
		Ext.Msg.show({
			title : '重要',
			msg : '是否删除会员资料?<br/>一旦成功将无法恢复.',
			buttons : Ext.Msg.YESNO,
			icon : Ext.Msg.QUESTION,
			fn : function(e){
				if(e == 'yes'){
					var params = Ext.encode({
						id : data['id'],
						restaurantID : restaurantID,
						client : {
							restaurantID : restaurantID,
							clientID : data['client']['clientID']
						},
						staff : {
							terminal : {
								restaurantID : restaurantID,
								pin : pin
							}
						}
					});
					Ext.Ajax.request({
						url : '../../DeleteMember.do',
						params : {
							params : params
						},
						success : function(res, opt){
							var jr = Ext.decode(res.responseText);
							if(jr.success){
								Ext.example.msg(jr.title, jr.msg);
								memberBasicGrid.getStore().reload();
							}else{
								Ext.ux.showMsg(jr);
							}
						},
						failure : function(res, opt){
							Ext.ux.showMsg(Ext.decode(res.responseText));
						}
					});
				}
			}
		});
	}
};


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
					var memberCard = !data ? '' : data['memberCard'] ;
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
/**
 * 
 */
function initAdjustPointWin(){
	if(!adjustPointWin){
		var numAdjustPoint = new Ext.form.NumberField({
			xtype : 'numberfield',
			id : 'numAdjustPoint',
			fieldLabel : '',
			width : 100,
			allowBlank : false,
			blankText : '调整积分不能为空, 0 则取消操作.',
			listeners : {
				render : function(){
					Ext.getCmp('radioAdjustPointIncrease').setValue(true);
				}
			}
		});
		adjustPointWin = new Ext.Window({
			title : '会员积分调整',
			modal : true,
			closable : false,
			resizable : false,
			width : 200,
			height : 146,
			layout : 'fit',
			frame : true,
			items : [{
				layout : 'column',
				frame : true,
				defaults : {
					columnWidth : .33,
					layout : 'form',
					labelWidth : 60
				},
				items : [{
					items : [{
						xtype : 'radio',
						id : 'radioAdjustPointIncrease',
						name : 'radioAdjustPoint',
						inputValue : 1,
						hideLabel : true,
						boxLabel : '增加',
						listeners : {
							check : function(e){
								if(e.getValue()){
									changeAdjustPointLabel('增加积分');
								}
							}
						}
					}]
				}, {
					items : [{
						xtype : 'radio',
						name : 'radioAdjustPoint',
						inputValue : 2,
						hideLabel : true,
						boxLabel : '减少',
						listeners : {
							check : function(e){
								if(e.getValue()){
									changeAdjustPointLabel('减少积分');
								}
							}
						}
					}]
				}, {
					items : [{
						xtype : 'radio',
						name : 'radioAdjustPoint',
						inputValue : 3,
						hideLabel : true,
						boxLabel : '设置',
						listeners : {
							check : function(e){
								if(e.getValue()){
									changeAdjustPointLabel('设置积分');
								}
							}
						}
					}]
				}, {
					columnWidth : 1,
					items : [{
						xtype : 'textfield',
						id : 'numMemberPointForNow',
						fieldLabel : '当前积分',
						width : 100,
						disabled : true
					}]
				}, {
					columnWidth : 1,
					items : [numAdjustPoint]
				}]
			}],
			bbar : ['->', {
				text : '保存',
				iconCls : 'btn_save',
				handler : function(){
					var data = Ext.ux.getSelData(memberBasicGrid);
					if(!numAdjustPoint.isValid()){
						return;
					}
					if(numAdjustPoint.getValue() == 0){
						adjustPointWin.hide();
						Ext.example.msg('提示', '你输入的积分为0, 无需调整');
						return;
					}
					Ext.Msg.show({
						title : '重要',
						msg : '是否'+Ext.query('label[for="numAdjustPoint"]')[0].innerHTML+numAdjustPoint.getValue(),
						buttons : Ext.Msg.YESNO,
						icon: Ext.MessageBox.QUESTION,
						fn : function(btn){
							if(btn=='yes'){
								var adjust = document.getElementsByName('radioAdjustPoint');
								for(var i=0; i< adjust.length; i++){
									if(adjust[i].checked){
										adjust = adjust[i].value;
										break;
									}
								}
								Ext.Ajax.request({
									url : '../../OperateMember.do',
									params : {
										dataSource : 'adjustPoint',
										pin : pin,
										memberId : data['id'],
										point : numAdjustPoint.getValue(),
										adjust : adjust
									},
									success : function(res, opt){
										var jr = Ext.decode(res.responseText);
										if(jr.success){
											adjustPointWin.hide();
											Ext.example.msg(jr.title, jr.msg);
											Ext.getCmp('btnSearchMember').handler();
										}else{
											Ext.ux.showMsg(jr);
										}
									},
									failure : function(res, opt){
										Ext.ux.showMsg(Ext.decode(res.responseText));
									}
								});
							}
						}
					});
				}
			}, {
				text : '关闭',
				iconCls : 'btn_close',
				handler : function(){
					adjustPointWin.hide();
				}
			}],
			keys : [{
				key : Ext.EventObject.ESC,
				scope : this,
				fn : function(){
					adjustPointWin.hide();
				}
			}],
			listeners : {
				hide : function(){
					numAdjustPoint.setValue();
					Ext.getCmp('radioAdjustPointIncrease').setValue(true);
				}
			}
		});
	}
}
function changeAdjustPointLabel(label){
	Ext.query('label[for="numAdjustPoint"]')[0].innerHTML = label+':';
}
/**
 * 积分调整
 */
function adjustPoint(){
	var data = Ext.ux.getSelData(memberBasicGrid);
	if(!data){
		Ext.example.msg('提示', '操作失败, 请选择一条记录后再进行操作.');
		return;
	}
	initAdjustPointWin();
	adjustPointWin.show();
	Ext.getCmp('numMemberPointForNow').setValue(data['point']);
}
