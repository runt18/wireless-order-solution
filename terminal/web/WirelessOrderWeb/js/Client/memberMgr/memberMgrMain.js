
//-------------------------lib.js
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

function attentionHandler(){
	memberOperationHandler({
		type : 'attend'
	});
}

function cancelAttentionHandler(){
	memberOperationHandler({
		type : 'cancel'
	});
}

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

	}else if(c.type == 'attend'){
		var data = Ext.ux.getSelData(memberBasicGrid);
		Ext.Ajax.request({
			url : '../../OperateMember.do',
			params : {
				dataSource : 'interestedMember',
				attendtion : 1,
				memberId : data['id']
			},
			success : function(res, opt){
				var jr = Ext.decode(res.responseText);
				if(jr.success){
					Ext.example.msg(jr.title, String.format(Ext.ux.txtFormat.operateSuccess, data['name'], jr.msg));
					memberBasicGrid.getStore().reload();
				}else{
					Ext.ux.showMsg(jr);
				}
			},
			failure : function(res, opt){
				Ext.ux.showMsg(Ext.decode(res.responseText));
			}
		});
	
	}else if(c.type == 'cancel'){
		var data = Ext.ux.getSelData(memberBasicGrid);
		Ext.Ajax.request({
			url : '../../OperateMember.do',
			params : {
				dataSource : 'interestedMember',
				attendtion : 0,
				memberId : data['id']
			},
			success : function(res, opt){
				var jr = Ext.decode(res.responseText);
				if(jr.success){
					Ext.example.msg(jr.title, String.format(Ext.ux.txtFormat.operateSuccess, data['name'], jr.msg));
					memberBasicGrid.getStore().reload();
				}else{
					Ext.ux.showMsg(jr);
				}
			},
			failure : function(res, opt){
				Ext.ux.showMsg(Ext.decode(res.responseText));
			}
		});
	
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
					Ext.Ajax.request({
						url : '../../OperateMember.do',
						params : {
							dataSource : 'delete',
							
							id : data['id']
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
			width : 680,
			height : 350,
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
					var mobile = data != false && data['memberType']['attributeValue'] == 0 ? data['mobile'] : '';
					thiz.center();
					thiz.load({
						url : '../window/client/recharge.jsp',
						scripts : true,
						params : {
							memberMobile : mobile
						}
					});
				}
			},
			bbar : [{
				xtype : 'checkbox',
				id : 'chbPrintRecharge',
				checked : true,
				boxLabel : '打印充值信息'
			},{
				xtype : 'tbtext',
				text : '&nbsp;&nbsp;'
			},{
				xtype : 'checkbox',
				id : 'chbSendCharge',
				checked : true,
				boxLabel : '发送充值信息'+(Ext.ux.smsCount >= 20 ? '(<font style="color:green;font-weight:bolder">剩余'+Ext.ux.smsCount+'条</font>)' : '(<font style="color:red;font-weight:bolder">剩余'+Ext.ux.smsCount+'条, 请及时充值</font>)'),
				hidden : !Ext.ux.smsModule
			}, '->', {
				text : '充值',
				iconCls : 'icon_tb_recharge',
				handler : function(e){
					var sendSms = Ext.getCmp('chbSendCharge').getValue();
					if(sendSms){
						Ext.ux.setCookie(document.domain+'_chargeSms', true, 3650);
					}else{
						Ext.ux.setCookie(document.domain+'_chargeSms', false, 3650);
					}
					// 跨域调用充值方法
					rechargeControlCenter({
						isPrint : Ext.getCmp('chbPrintRecharge').getValue(),
						callback : function(_c){
							rechargeWin.hide();
							var st = Ext.getCmp('mr_comboMemberSearchType');
							st.setValue(2);
							st.fireEvent('select', st, null, null);
							var n = Ext.getCmp('numberSearchValueByNumber');
							n.setValue(_c.data.memberCard);
							Ext.getCmp('btnSearchMember').handler();
						}
					});
					
				}
			}, {
				text : '关闭',
				iconCls : 'btn_close',
				handler : function(e){
					rechargeWin.hide();
				}
			}]
		});
	}
	if(Ext.ux.getCookie(document.domain+'_chargeSms') == 'true'){
		Ext.getCmp('chbSendCharge').setValue(true);
	}else{
		Ext.getCmp('chbSendCharge').setValue(false);
	}
}

/**
 * 会员取款
 */
function initTakeMoneyWin(){
	var takeMoneyWin = Ext.getCmp('takeMoneyWin');
	if(!takeMoneyWin){
		takeMoneyWin = new Ext.Window({
			id : 'takeMoneyWin',
			title : '会员取款',
			closable : false,
			modal : true,
			resizable : false,
			width : 680,
			height : 275,
			keys : [{
				key : Ext.EventObject.ESC,
				scope : this,
				fn : function(){
					takeMoneyWin.hide();
				}
			}],
			listeners : {
				hide : function(thiz){
					thiz.body.update('');
				},
				show : function(thiz){
					var data = Ext.ux.getSelData(memberBasicGrid);
					var mobile = data != false && data['memberType']['attributeValue'] == 0 ? data['mobile'] : '';
					thiz.center();
					thiz.load({
						url : '../window/client/takeMoney.jsp',
						scripts : true,
						params : {
							memberMobile : mobile
						}
					});
				}
			},
			bbar : [{
				xtype : 'checkbox',
				id : 'chbPrintTakeMoney',
				checked : true,
				boxLabel : '打印取款信息'
			}, '->', {
				text : '取款',
				iconCls : 'icon_tb_recharge',
				handler : function(){
					// 跨域调用取款方法
					takeMoneyControlCenter({
						isPrint : Ext.getCmp('chbPrintTakeMoney').getValue(),
						callback : function(_c){
							takeMoneyWin.hide();
//							var st = Ext.getCmp('mr_comboMemberSearchType');
//							st.setValue(2);
//							st.fireEvent('select', st, null, null);
//							var n = Ext.getCmp('numberSearchValueByNumber');
//							n.setValue(_c.data.memberCard);
//							Ext.getCmp('btnSearchMember').handler();
						}
					});
				}
			}, {
				text : '关闭',
				iconCls : 'btn_close',
				handler : function(){
					takeMoneyWin.hide();
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
	Ext.getCmp('rechargeWin').show();
}

function takeMoneyHandler(){
	initTakeMoneyWin();
	Ext.getCmp('takeMoneyWin').show();
}
var	mr_queryMemberOperationWin;
/**
 * 会员操作明细
 */
function queryMemberOperationHandler(title, url, params){
	var mr_queryMemberOperationWin = Ext.getCmp('mr_queryMemberOperationWin');
	if(!mr_queryMemberOperationWin){
	mr_queryMemberOperationWin = new Ext.Window({
			id : 'mr_queryMemberOperationWin',
			modal : true,
			closable : false,
			resizable : false,
			width : 1320,
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
				}
			}
		});
	}

	mr_queryMemberOperationWin.show();
	mr_queryMemberOperationWin.setTitle(title);
	mr_queryMemberOperationWin.load({
		url : url,
		scripts : true,
		params : params
	});
}

/**
 * 会员操作汇总
 */
function queryMemberOperationSummaryHandler(){
	var title = '会员操作明细';
	queryMemberOperationHandler(title, '../window/client/memberOperation.jsp', {modal : true});
	/*var mr_queryMemberConsumeSummaryWin = Ext.getCmp('mr_queryMemberConsumeSummaryWin');
	if(!mr_queryMemberConsumeSummaryWin){
		mr_queryMemberConsumeSummaryWin = new Ext.Window({
			id : 'mr_queryMemberConsumeSummaryWin',
			title : '会员操作汇总',
			modal : true,
			closable : false,
			resizable : false,
			width : 1200,
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
						url : '../window/client/memberOperationSummary.jsp',
						scripts : true,
						params : {
							memberCard : memberCard
						}
					});
				}
			}
		});
	}
	mr_queryMemberConsumeSummaryWin.show();*/
};


function initMemberCouponWin(){
	if(!memberCouponWin){
		memberCouponWin = new Ext.Window({
			id : 'memberCouponWin',
			title : '选择优惠劵',
			closable : false,
			modal : true,
			resizable : false,
			width : 300,
			keys : [{
				key : Ext.EventObject.ESC,
				scope : this,
				fn : function(){
					memberCouponWin.hide();
				}
			}],
			bbar : [{
				xtype : 'checkbox',
				id : 'chbSendCouponSms',
				checked : true,
				boxLabel : '发送通知短信'+(Ext.ux.smsCount >= 20 ? '(<font style="color:green;font-weight:bolder">剩余'+Ext.ux.smsCount+'条</font>)' : '(<font style="color:red;font-weight:bolder">剩余'+Ext.ux.smsCount+'条, 请及时充值</font>)'),
				hidden : !Ext.ux.smsModule
			},'->',{
				text : '发放',
				id : 'btn_memberCouponSend',
				iconCls : 'btn_save',
				handler : function(){
					var sendSms = Ext.getCmp('chbSendCouponSms').getValue();
					if(sendSms){
						Ext.ux.setCookie(document.domain+'_couponSms', true, 3650);
					}else{
						Ext.ux.setCookie(document.domain+'_couponSms', false, 3650);
					}		
					
					if(!Ext.getCmp('member_comboCoupon').isValid()){
						return;
					}
					
					var members = memberBasicGrid.getSelectionModel().getSelections();
					if(members.length < 1){
						Ext.example.msg('提示', '请选中会员后进行操作.');
						return;
					}
					
					if(sendSms){
						Ext.Msg.confirm(
							'提示',
							'总共 '+ members.length +' 条短信, 是否发送' ,
							function(e){
								if(e == 'yes'){
									var coupon = Ext.getCmp('member_comboCoupon');
									var membersData = '';
									for (var i = 0; i < members.length; i++) {
										if(i > 0){
											membersData += ',';
										}
										membersData += members[i].get('id');
									}
									
									Ext.Ajax.request({
										url : '../../OperateCoupon.do',
										params : {
											membersData : membersData,
											coupon : coupon.getValue(),
											sendSms : sendSms?true : '',
											dataSource : 'sendCoupon'
										},
										success : function(res, opt){
											var jr = Ext.decode(res.responseText);
											if(jr.success){
												Ext.ux.showMsg(jr);
												memberCouponWin.hide();
											}else{
												Ext.ux.showMsg(jr);
											}
											
										},
										failure : function(res, opt){
											Ext.ux.showMsg(Ext.util.JSON.decode(res.responseText));
										}
									});
								}
							}
						);						
					}else{
						var coupon = Ext.getCmp('member_comboCoupon');
						var membersData = '';
						for (var i = 0; i < members.length; i++) {
							if(i > 0){
								membersData += ',';
							}
							membersData += members[i].get('id');
						}
						
						Ext.Ajax.request({
							url : '../../OperateCoupon.do',
							params : {
								membersData : membersData,
								coupon : coupon.getValue(),
								dataSource : 'sendCoupon'
							},
							success : function(res, opt){
								var jr = Ext.decode(res.responseText);
								if(jr.success){
									Ext.ux.showMsg(jr);
									memberCouponWin.hide();
								}else{
									Ext.ux.showMsg(jr);
								}
								
							},
							failure : function(res, opt){
								Ext.ux.showMsg(Ext.util.JSON.decode(res.responseText));
							}
						});
					}
					
			
				}
			}, {
				text : '取消',
				id : 'btn_cancelMmemberLevelWin',
				iconCls : 'btn_close',
				handler : function(){
					memberCouponWin.hide();
				}
			}],
			keys: [{
				key : Ext.EventObject.ENTER,
				scope : this,
				fn : function(){
					Ext.getCmp('btn_memberCouponSend').handler();
				}
			},{
				key : Ext.EventObject.ESC,
				scope : this,
				fn : function(){
					memberCouponWin.hide();
				}
			}],
			items : [{
				layout : 'form',
				labelWidth : 80,
				width : 300,
				border : false,
				frame : true,
				items : [{
					xtype : 'combo',
					id : 'member_comboCoupon',
					fieldLabel : '优惠劵类型',
					readOnly : false,
					forceSelection : true,
					width : 130,
					listWidth : 120,
					store : new Ext.data.SimpleStore({
						fields : ['id', 'couponName']
					}),
					valueField : 'id',
					displayField : 'couponName',
					typeAhead : true,
					mode : 'local',
					triggerAction : 'all',
					selectOnFocus : true,
					allowBlank : false,					
					listeners : {
						render : function(thiz){
							var data = [];
							Ext.Ajax.request({
								url : '../../QueryCouponType.do',
								params : {
									dataSource : 'unExpired'
								},								
								success : function(res, opt){
									var jr = Ext.decode(res.responseText);
									for(var i = 0; i < jr.length; i++){
										data.push([jr[i]['couponTypeId'], jr[i]['typeName']]);
									}
									thiz.store.loadData(data);
								},
								fialure : function(res, opt){
									thiz.store.loadData(data);
								}
							});
						}
					}
				}]
			}]
		});
	}
	if(Ext.ux.getCookie(document.domain+'_couponSms') == 'true'){
		Ext.getCmp('chbSendCouponSms').setValue(true);
	}else{
		Ext.getCmp('chbSendCouponSms').setValue(false);
	}	
	memberCouponWin.show();
}


/**
 * 
 */
function initAdjustPointWin(){
	if(!adjustPointWin){
		var numAdjustPoint = new Ext.form.NumberField({
			xtype : 'numberfield',
			id : 'numAdjustPoint',
			fieldLabel : '',
			style : 'color:red;',
			width : 100,
			allowBlank : false,
			blankText : '调整积分不能为空, 0 则取消操作.',
			validator : function(value){
				var adjust = document.getElementsByName('radioAdjustPoint');
				for(var i=0; i< adjust.length; i++){
					if(adjust[i].checked){
						adjust = adjust[i].value;
						break;
					}
				}
				if(adjust == 2){
					var data = Ext.ux.getSelData(memberBasicGrid);
					if(Math.abs(value) > data['point']){
						Ext.getCmp('numAdjustPoint').setValue(data['point']);
					}
					return true;
				}else{
					return true;
				}
			},
			listeners : {
				render : function(){
					Ext.getCmp('radioAdjustPointIncrease').setValue(true);
				}
			}
		});
		adjustPointWin = new Ext.Window({
			title : '&nbsp;',
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
						style : 'color:green;',
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
	adjustPointWin.setTitle('调整积分, 会员:'+data['name']);
	Ext.getCmp('numMemberPointForNow').setValue(data['point']);
}

//---------------------

//--------------------load------
/**************************************************/
memberCardAliasRenderer = function(v){
	return v.length > 0 ? ('******' + v.substring(6, 10)) : '';
};
memberStatusRenderer = function(v){
	for(var i = 0; i < memberStatus.length; i++){
		if(eval(memberStatus[i][0] == v)){
			return memberStatus[i][1];
		}
	}
};
function memberOperationSend(){
	var data = Ext.ux.getSelData(memberBasicGrid);
	var mobile = data != false ? data['mobile'] : '';
	var title = '会员操作明细 --> ' + data.name;
	queryMemberOperationHandler(title, '../window/client/memberOperation.jsp', { memberMobile : mobile, modal : true});
} 

memberOperationRenderer = function(val, m, record){
//	var attendtion;
//	if(record.data['acctendtioned']){
//		attendtion = '<a href="javascript:cancelAttentionHandler()">取消关注</a>';
//	}else{
//		attendtion = '<a href="javascript:attentionHandler()">添加关注</a>';
//	}
	return ''
		+ '<a href="javascript:updateMemberHandler()">修改</a>'
		+ '&nbsp;&nbsp;&nbsp;&nbsp;'
		+ '<a href="javascript:deleteMemberHandler()">删除</a>'
		+ '&nbsp;&nbsp;&nbsp;&nbsp;'
		+ '<a href="javascript:rechargeHandler()">充值</a>'
		+ '&nbsp;&nbsp;&nbsp;'
		+ '<a href="javascript:takeMoneyHandler()">取款</a>'
		+ '&nbsp;&nbsp;&nbsp;'
		+ '<a href="javascript:memberOperationSend() ">操作明细</a>'
		+ '&nbsp;&nbsp;&nbsp;&nbsp;';
//		+ '&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;'
//		+ '<a href="javascript:adjustPoint()">积分调整</a>';
};
/**************************************************/
function treeInit(){
	var memberTypeTreeTbar = new Ext.Toolbar({
		items : ['->',{
			text : '添加',
			iconCls : 'btn_add',
			handler : function(){
				insertMemberTypeHandler();
			}			
			
		},{
			text : '刷新',
			id : 'btnRefreshMemberType',
			iconCls : 'btn_refresh',
			handler : function(){
				Ext.getDom('memberTypeShowType').innerHTML = '----';
				memberTypeTree.getRootNode().reload();
			}
		}]
	});
	
	memberTypeTree = new Ext.tree.TreePanel({
		id : 'tree_memberTypeMgr',
		title : '会员类型',
//		region : 'west',
		region : 'center',
		width : 240,
		border : true,
		rootVisible : true,
		singleExpand : true,
		autoScroll : true,
		frame : true,
		bodyStyle : 'backgroundColor:#FFFFFF; border:1px solid #99BBE8;',
		loader : new Ext.tree.TreeLoader({
			dataUrl : '../../QueryMemberType.do',
			baseParams : {
				dataSource : 'tree',
				restaurantId : restaurantID
			}
		}),
		root : new Ext.tree.AsyncTreeNode({
			text : '全部类型',
			leaf : false,
			border : true,
			expanded : true,
			MemberTypeId : -1,
			listeners : {
				load : function(thiz){
					memberTypeData.root = [];
					for(var i = 0; i < thiz.childNodes.length; i++){
						memberTypeData.root.push({
							memberTypeID : thiz.childNodes[i].attributes['memberTypeId'],
							memberTypeName : thiz.childNodes[i].attributes['memberTypeName']
						});
					}
				}
			}
		}),
		tbar : memberTypeTreeTbar,
		listeners : {
	    	click : function(e){
	    		Ext.getCmp('btnSearchMember').handler();
	    		Ext.getDom('memberTypeShowType').innerHTML = e.text;
	    	}
	    }
	});
};

function gridInit(){
	var member_beginDate = new Ext.form.DateField({
		xtype : 'datefield',	
		id : 'dateSearchDateBegin',
		format : 'Y-m-d',
		width : 100,
		maxValue : new Date(),
		readOnly : false,
		allowBlank : false
	});
	var member_endDate = new Ext.form.DateField({
		xtype : 'datefield',
		id : 'dateSearchDateEnd',
		format : 'Y-m-d',
		width : 100,
		maxValue : new Date(),
		readOnly : false,
		allowBlank : false
	});
	var member_dateCombo = Ext.ux.createDateCombo({
		width : 75,
		data : [[3, '近一个月'], [4, '近三个月'], [9, '近半年']],
		beginDate : member_beginDate,
		endDate : member_endDate,
		callback : function(){
			if(member_searchType){
				Ext.getCmp('btnSearchMember').handler();
			}
		}
	});
	
	var memberBasicGridExcavateMemberTbar = new Ext.Toolbar({
		hidden : true,
		height : 28,		
		items : [
			{xtype : 'tbtext', text : '日期:&nbsp;&nbsp;'},
			{xtype : 'tbtext', text : '&nbsp;&nbsp;'},
			member_dateCombo,
			{xtype : 'tbtext', text : '&nbsp;'},
			member_beginDate,
			{
				xtype : 'label',
				hidden : false,
				id : 'tbtextDisplanZ',
				text : ' 至 '
			}, 
			member_endDate,
			{xtype : 'tbtext', text : '&nbsp;&nbsp;'},			
			{xtype : 'tbtext', text : '消费金额:'},
			{
				xtype : 'numberfield',
				id : 'textTotalMinMemberCost',
				width : 60
			},
			{
				xtype : 'tbtext',
				text : '&nbsp;-&nbsp;'
			},			
			{
				xtype : 'numberfield',
				id : 'textTotalMaxMemberCost',
				width : 60
			},
			{xtype : 'tbtext', text : '&nbsp;&nbsp;'},	
			{xtype : 'tbtext', text : '消费次数:'},
			{
				xtype : 'numberfield',
				id : 'textTotalMinMemberCostCount',
				width : 50
			},
			{
				xtype : 'tbtext',
				text : '&nbsp;-&nbsp;'
			},			
			{
				xtype : 'numberfield',
				id : 'textTotalMaxMemberCostCount',
				width : 50
			},
/*			{xtype : 'tbtext', text : '&nbsp;&nbsp;'},	
			{xtype : 'tbtext', text : '当前积分:'},
			{
				xtype : 'numberfield',
				id : 'textMemberPoint',
				width : 60
			},*/
			{xtype : 'tbtext', text : '&nbsp;&nbsp;'},	
			{xtype : 'tbtext', text : '余额:'},
			{
				id : 'memberBalanceEqual',
				xtype : 'combo',
				readOnly : false,
				forceSelection : true,
				value : '=',
				width : 70,
				store : new Ext.data.SimpleStore({
					fields : ['value', 'text'],
					data : [['=', '等于'], ['>=', '大于等于'], ['<=', '小于等于']]
				}),
				valueField : 'value',
				displayField : 'text',
				typeAhead : true,
				mode : 'local',
				triggerAction : 'all',
				selectOnFocus : true
			},				
			{
				xtype : 'numberfield',
				id : 'textMemberBalance',
				width : 60
			}]
		
	});
	
	var memberBasicGridOtherMemberTbar = new Ext.Toolbar({
		hidden : true,
		height : 28,		
		items : [
			{xtype : 'tbtext', text : '当前积分:'},
			{
				id : 'memberCurrentPointEqual',
				xtype : 'combo',
				readOnly : false,
				forceSelection : true,
				value : '=',
				width : 70,
				store : new Ext.data.SimpleStore({
					fields : ['value', 'text'],
					data : [['=', '等于'], ['>=', '大于等于'], ['<=', '小于等于']]
				}),
				valueField : 'value',
				displayField : 'text',
				typeAhead : true,
				mode : 'local',
				triggerAction : 'all',
				selectOnFocus : true
			},			
			{
				xtype : 'numberfield',
				id : 'textMemberCurrentPoint',
				width : 60
			},
			{xtype : 'tbtext', text : '&nbsp;&nbsp;'},	
			{xtype : 'tbtext', text : '客户名称:'},
			{
				xtype : 'textfield',
				id : 'textMemberName',
				width : 50
			}]
		
	});	
	var memberBasicGridTbar = new Ext.Toolbar({
		items : [{
			xtype : 'tbtext',
			text : String.format(Ext.ux.txtFormat.longerTypeName, '会员类型', 'memberTypeShowType', '----')
		}, {
			xtype : 'tbtext',
			text : '&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;'
		}, 
		{
			xtype : 'tbtext',
			text : '会员手机/会员卡号/会员名:'
		}, 			
		{
			xtype : 'numberfield',
			id : 'numberSearchByMemberPhoneOrCardOrName'
		}, '->', {
			text : '高级条件↓',
	    	id : 'member_btnHeightSearch',
	    	handler : function(){
	    		member_searchType = true;
				Ext.getCmp('member_btnCommonSearch').show();
	    		Ext.getCmp('member_btnHeightSearch').hide();
	    		
	    		memberBasicGrid.setHeight(memberBasicGrid.getHeight()-28);
	    		
	    		memberBasicGridExcavateMemberTbar.show();
	    		
	    		
	    		memberBasicGrid.syncSize(); //强制计算高度
	    		memberBasicGrid.doLayout();//重新布局 	
			}
		}, {
			text : '高级条件↑',
	    	id : 'member_btnCommonSearch',
			hidden : true,
	    	handler : function(){
	    		member_searchType = true;
				Ext.getCmp('member_btnHeightSearch').show();
	    		Ext.getCmp('member_btnCommonSearch').hide();
	    		
	    		memberBasicGridExcavateMemberTbar.hide();
	    		
	    		memberBasicGrid.setHeight(memberBasicGrid.getHeight()+28);
	    		memberBasicGrid.syncSize(); //强制计算高度
	    		memberBasicGrid.doLayout();//重新布局 	
	    		
	    		member_dateCombo.setValue(4);
	    		member_dateCombo.fireEvent('select', member_dateCombo,null,4);
	    		
	    		Ext.getCmp('textTotalMemberCost').setValue();
	    		Ext.getCmp('usedBalanceEqual').setValue('=');
	    		Ext.getCmp('textTotalMemberCostCount').setValue();
	    		Ext.getCmp('consumptionAmountEqual').setValue('=');
	    		Ext.getCmp('numberSearchByMemberPhoneOrCard').setValue();
	    		Ext.getCmp('memberBalanceEqual').setValue('=');
			}
		},{xtype : 'tbtext', text : '&nbsp;&nbsp;'},	 
		{
			text : '搜索',
			id : 'btnSearchMember',
			iconCls : 'btn_search',
			handler : function(){
				
				var memberTypeNode = memberTypeTree.getSelectionModel().getSelectedNode();
				
				var gs = memberBasicGrid.getStore();
				
//				var tipWinShow = false;
				
				if(memberTypeNode){
					gs.baseParams['memberType'] = memberTypeNode.attributes.memberTypeId;
				}else{
					gs.baseParams['memberType'] = '';
				}
				
				gs.baseParams['memberCardOrMobileOrName'] = Ext.getCmp('numberSearchByMemberPhoneOrCardOrName').getValue();
				gs.baseParams['MinTotalMemberCost'] = Ext.getCmp('textTotalMinMemberCost').getValue();
				gs.baseParams['MaxTotalMemberCost'] = Ext.getCmp('textTotalMaxMemberCost').getValue();
				gs.baseParams['consumptionMinAmount'] = Ext.getCmp('textTotalMinMemberCostCount').getValue();
				gs.baseParams['consumptionMaxAmount'] = Ext.getCmp('textTotalMaxMemberCostCount').getValue();
				gs.baseParams['memberBalance'] = Ext.getCmp('textMemberBalance').getValue();
				gs.baseParams['memberBalanceEqual'] = Ext.getCmp('memberBalanceEqual').getValue();
				gs.load({
					params : {
						start : 0,
						limit : 200
					}
				});
				gs.on('load', function(store, records, options){
					if(memberTypeNode){
						if(typeof memberTypeNode.attributes.attr != 'undefined' && memberTypeNode.attributes.attr == 2){
							for (var i = 0; i < records.length; i++) {
								records[i].set('acctendtioned', true);
							}
						}
					}
//					if(gs.getTotalCount() > 0 && tipWinShow){
//						memberBasicGrid.getSelectionModel().selectAll();
////						if(Ext.ux.getCookie(document.domain+'_memberTip') != 'true'){
//							memberTipWin.show();
////						}					
//					}
//					
//					tipWinShow = true;
				});
				
				
			}
		}, {
			hidden : true,
			text : '重置',
			iconCls : 'btn_refresh',
			handler : function(e){
				Ext.getCmp('btnRefreshMemberType').handler();
				Ext.getCmp('btnSearchMember').handler();
				var st = Ext.getCmp('mr_comboMemberSearchType');
				st.setValue(0);
				st.fireEvent('select', st, null, null);
			}
		}, {
			text : '添加',
			iconCls : 'btn_add',
			handler : function(e){
				insertMemberHandler();
			}
		}, {
			hidden : true,
			text : '修改',
			iconCls : 'btn_edit',
			handler : function(e){
				updateMemberHandler();
			}
		}, {
			hidden : true,
			text : '删除',
			iconCls : 'btn_delete',
			handler : function(e){
				deleteMemberHandler();
			}
		}, {
			text : '充值',
			iconCls : 'icon_tb_recharge',
			handler : function(e){
				rechargeHandler();
			}
		}, {
			text : '取款',
			iconCls : 'btn_edit',
			handler : function(){
				takeMoneyHandler();
			}
		}, {
			text : '积分调整',
			iconCls : 'icon_tb_setting',
			handler : function(e){
				adjustPoint();
			}
		}, '-', {
			text : '导出',
			iconCls : 'icon_tb_exoprt_excel',
			handler : function(e){

				var memberTypeNode = memberTypeTree.getSelectionModel().getSelectedNode();
//				var tipWinShow = false;
				var memberType = '';
				if(memberTypeNode){
					memberType = memberTypeNode.attributes.memberTypeId;
				}
				
				var url = '../../{0}?memberType={1}&memberCardOrMobileOrName={2}&MinTotalMemberCost={3}' +
						'&MaxTotalMemberCost={4}&consumptionMinAmount={5}&consumptionMaxAmount={6}&memberBalance={7}&memberBalanceEqual={8}&dataSource={9}';
				url = String.format(
					url, 
					'ExportHistoryStatisticsToExecl.do', 
					memberType,
					Ext.getCmp('numberSearchByMemberPhoneOrCardOrName').getValue(),
					Ext.getCmp('textTotalMinMemberCost').getValue(),
					Ext.getCmp('textTotalMaxMemberCost').getValue(),
					Ext.getCmp('textTotalMinMemberCostCount').getValue(),
					Ext.getCmp('textTotalMaxMemberCostCount').getValue(),
					Ext.getCmp('textMemberBalance').getValue(),
					Ext.getCmp('memberBalanceEqual').getValue(),
					'memberList'
				);
				
				window.location = url;
			}
		}]
	});
	
	memberBasicGrid = createGridPanel(
		'memberBasicGrid',
		'会员信息',
		'',
		'',
		'../../QueryMember.do',
		[
			[true, false, false, true],
			['名称', 'name'],
			['类型', 'memberType.name'],
			['消费次数', 'consumptionAmount',,'right', 'Ext.ux.txtFormat.gridDou'],
			['消费总额', 'totalConsumption',,'right', 'Ext.ux.txtFormat.gridDou'],
			['累计积分', 'totalPoint',,'right', 'Ext.ux.txtFormat.gridDou'],
			['当前积分', 'point',,'right', 'Ext.ux.txtFormat.gridDou'],
			['总充值额', 'baseBalance',,'right', 'Ext.ux.txtFormat.gridDou'],
			['账户余额', 'totalBalance',,'right', 'Ext.ux.txtFormat.gridDou'],
			['手机号码', 'mobile', 125],
			['会员卡号', 'memberCard', 125],
			['操作', 'operation', 270, 'center', 'memberOperationRenderer']
		],
		MemberBasicRecord.getKeys(),
		[['isPaging', true], ['restaurantID', restaurantID],  ['dataSource', 'normal']],
		200,
		'',
		[memberBasicGridTbar, memberBasicGridExcavateMemberTbar]
	);	
	memberBasicGrid.region = 'center';
	memberBasicGrid.loadMask = null;
	
	
	memberBasicGrid.on('render', function(e){
		Ext.getCmp('btnSearchMember').handler();
	});
	memberBasicGrid.on('rowdblclick', function(e){
		updateMemberHandler();
	});
	memberBasicGrid.keys = [{
		key : Ext.EventObject.ENTER,
		scope : this,
		fn : function(){ 
			Ext.getCmp('btnSearchMember').handler();
		}
	}];
};

function winInit(){
	memberBasicWin = new Ext.Window({
		title : '&nbsp;',
		width : 660,
		height : Ext.isIE ? 330 : 296,
		modal : true,
		resizable : false,
		closable : false,
		listeners : {
			hide : function(thiz){
				thiz.body.update('');
			},
			show : function(thiz){
				var task = {
					run : function(){
						if(typeof cm_operationMemberBasicMsg == 'function'){
							var data = {};
							if(memberBasicWin.otype == Ext.ux.otype['update']){
								data = Ext.ux.getSelData(memberBasicGrid);
								data = !data ? {status:0} : data;
							}else{
								data = {status:0};
							}
							cm_operationMemberBasicMsg({
								type : 'SET',
								data : data
							});
							Ext.TaskMgr.stop(this);
						}
					},
					interval: 1000
				};
				
				
				thiz.load({
					url : '../window/client/controlMember.jsp', 
					scripts : true,
					params : {
						otype : memberBasicWin.otype
					},
					callback : function(){
						Ext.TaskMgr.start(task);
//							var data = {};
//							if(memberBasicWin.otype == Ext.ux.otype['update']){
//								data = Ext.ux.getSelData(memberBasicGrid);
//								data = !data ? {status:0} : data;
//							}else{
//								data = {status:0};
//							}
//							cm_operationMemberBasicMsg({
//								type : 'SET',
//								data : data
//							});						
					}
				});
				thiz.center();
			}
		},
		keys : [{
			key : Ext.EventObject.ESC,
			scope : this,
			fn : function(){
				memberBasicWin.hide();
			}
		}],
		bbar : ['->', {
			text : '保存',
			id : 'btnSaveControlMemberBasicMsg',
			iconCls : 'btn_save',
			handler : function(e){
				if(typeof operateMemberHandler != 'function'){
					Ext.example.msg('提示', '操作失败, 请求异常, 请尝试刷新页面后重试.');
				}else{
					var btnClose = Ext.getCmp('btnCloseControlMemberBasicMsg');
					operateMemberHandler({
						type : memberBasicWin.otype,
						data : memberBasicWin.otype == Ext.ux.otype['update'] ? Ext.ux.getSelData(memberBasicGrid) : null,
						setButtonStatus : function(s){
							e.setDisabled(s);
							btnClose.setDisabled(s);
						},
						callback : function(memberData, c, res){
							if(res.success){
								memberBasicWin.hide();
								Ext.example.msg(res.title, res.msg);
								Ext.getCmp('btnSearchMember').handler();
								memberTypeTree.getRootNode().reload();
							}else{
								Ext.ux.showMsg(res);
							}
						}
					});							
				}
			}
		}, {
			text : '关闭',
			id : 'btnCloseControlMemberBasicMsg',
			iconCls : 'btn_close',
			handler : function(){
				memberBasicWin.hide();
			}
		}]
	});
}

//----------------

var btnInsertMember = new Ext.ux.ImageButton({
	imgPath : '../../images/btnAddMember.png',
	imgWidth : 50,
	imgHeight : 50,
	tooltip : '添加会员',
	handler : function(e){
		insertMemberHandler();
	}
});

var btnRecharge = new Ext.ux.ImageButton({
	imgPath : '../../images/btnMemberRecharge.png',
	imgWidth : 50,
	imgHeight : 50,
	tooltip : '充值',
	handler : function(e){
		rechargeHandler();
	}
});

var btnTakeMoney = new Ext.ux.ImageButton({
	imgPath : '../../images/btnTakeMoney.png',
	imgWidth : 50,
	imgHeight : 50,
	tooltip : '取款',
	handler : function(e){
		takeMoneyHandler();
	}
});

var btnAdjustPoint = new Ext.ux.ImageButton({
	imgPath : '../../images/btnAdjustPoint.png',
	imgWidth : 50,
	imgHeight : 50,
	tooltip : '积分调整',
	handler : function(e){
		adjustPoint();
	}
});

var btnConsumeSummary = new Ext.ux.ImageButton({
	imgPath : '../../images/btnConsumeSummary.png',
	imgWidth : 50,
	imgHeight : 50,
	tooltip : '操作明细',
	handler : function(e){
		queryMemberOperationSummaryHandler();
	}
});

var btnConsumeDetail = new Ext.ux.ImageButton({
	imgPath : '../../images/btnConsumeDetail.png',
	imgWidth : 50,
	imgHeight : 50,
	tooltip : '消费明细',
	handler : function(e){
		queryMemberOperationHandler('会员消费明细', '../Client_Module/MemberConsumeDetails.html', {
			modal : true
		});
	}
});

var btnRechargeDetail = new Ext.ux.ImageButton({
	imgPath : '../../images/btnRechargeDetails.png',
	imgWidth : 50,
	imgHeight : 50,
	tooltip : '充值明细',
	handler : function(e){
		queryMemberOperationHandler('会员充值明细', '../Client_Module/MemberRechargeDetails.html', {
			modal : true
		});
	}
});

//var btnMemberCouponWin = new Ext.ux.ImageButton({
//	imgPath : '../../images/sendCoupon.png',
//	imgWidth : 50,
//	imgHeight : 50,
//	tooltip : '发送优惠劵',
//	handler : function(e){
//		var data = memberBasicGrid.getSelectionModel().getSelections();
//		if(data.length < 1){
//			Ext.example.msg('提示', '请选中会员后进行操作.');
//			return;
//		}		
//		
//		initMemberCouponWin();
//	}
//});

function fnRemberTip(){
	if(Ext.getDom('chxMemberTip').checked){
		Ext.ux.setCookie(document.domain+'_memberTip', true, 3650);
	}else{
		Ext.ux.setCookie(document.domain+'_memberTip', false, 3650);
	}
}

/*会员类型********************************************************/
function member_dataInit(){
	Ext.Ajax.request({
		url : '../../QueryDiscountTree.do',
		params : {
			restaurantID : restaurantID
			
		},
		success : function(res, opt){
			discountData = eval(res.responseText);

		},
		failure : function(res, opt){
			Ext.ux.showMsg(Ext.decode(res.responseText));
		}
	});
};
function checkLabel(t){
	if(t.length > 5){
		var after = t.substring(0, 5);
		return after;
	}else{
		return t;
	}
}
function m_memberTypeWinInit(){
	if(!m_memberTypeWin){
		m_memberTypeWin = new Ext.Window({
			id : 'm_memberTypeWin',
			title : '会员类型',
			closable : false,
			modal : true,
			resizable : false,
			width : 275,
			items : [{
				xtype : 'form',
				layout : 'form',
				frame : true,
				width : 275,
				labelWidth : 70,
				labelAlign : 'right',
				defaults : {
					width : 160
				},
				items : [{
					xtype : 'hidden',
					id : 'numTypeID',
					fieldLabel : '类型编号'
				}, {
					xtype : 'textfield',
					id : 'txtTypeName',
					fieldLabel : '类型名称' + Ext.ux.txtFormat.xh,
					allowBlank : false,
					blankText : '类型名称不能为空.',
					value : "",
					selectOnFocus : true
				}, {
					xtype : 'numberfield',
					id : 'numExchangeRate',
					fieldLabel : '积分比率' + Ext.ux.txtFormat.xh,
					value : 1,
					minValue : 0.00,
					allowBlank : false,
					blankText : '积分比率不能为空.',
					selectOnFocus : true
				}, {
					xtype : 'label',
					width : 200,
					style : 'color:green;font-szie:12px;',
					text : '说明:  使用会员结账时, 消费金额兑换积分的利率, 如金额 100 元兑换 150 积分, 则输入 1.5, 默认 1.'
				}, {
					xtype : 'numberfield',
					id : 'numInitialPoint',
					fieldLabel : '初始积分' + Ext.ux.txtFormat.xh,
					value : 1,
					minValue : 0.00,
					allowBlank : false,
					blankText : '初始积分不能为空.',
					selectOnFocus : true
				}, {
					xtype : 'label',
					style : 'color:green;font-szie:12px;',
					text : '说明:  新会员赠送积分.'
				}, {
					xtype : 'combo',
					id : 'comboAttribute',
					fieldLabel : '会员属性' + Ext.ux.txtFormat.xh,
					forceSelection : true,
					width : 130,
					value : 0,
					store : new Ext.data.SimpleStore({
						fields : [ 'value', 'text' ],
						data : memberAttributeData
					}),
					valueField : 'value',
					displayField : 'text',
					typeAhead : true,
					mode : 'local',
					triggerAction : 'all',
					selectOnFocus : true,
					allowBlank : false,
					blankText : '会员属性不能为空.',
					listeners : {
						render : function(e){
							e.setValue(0);
							e.fireEvent('select', e);
						},
						select : function(e, rocord, index){
							var cr = Ext.getCmp('numChargeRate');
							if(e.getValue() == 0){
								cr.setValue(1);
								cr.setDisabled(false);
							}else if(e.getValue() == 1){
								cr.setValue(0);
								cr.setDisabled(true);
							}
							cr.clearInvalid();
						}
					}
				}, {
					xtype : 'label',
					autoWidth : true,
					style : 'color:green;font-szie:12px;width : 250px',
					text : '说明:  所有属性都可使用积分功能, 积分类型只使用该会员类型的折扣信息, 充值则可使用会员资料中基本金额、赠送金额等更多信息'
				}, {
					xtype : 'numberfield',
					id : 'numChargeRate',
					fieldLabel : '充值比率' + Ext.ux.txtFormat.xh,
					value : 1.00,
					minValue : 0.00,
					minText : '充值比率最小为 0.00.',
					decimalPrecision : 2,
					allowBlank : false,
					blankText : '充值比率不能为空.',
					selectOnFocus : true,
					disabled : true
				}, {
					xtype : 'label',
					autoWidth : true,
					style : 'color:green;font-szie:12px;',
					text : '说明:充100.00元送50.00元, 充值比率输入  1.5, 默认 1 '
				}, {
					xtype : 'textarea',
					height : 40,
					id : 'txtCommentForMemberType',
					fieldLabel : '特权说明'
				},{
					xtype : 'panel',
					layout : 'column',
					id : 'formMemberDiscount',
					frame : true,
					width : 255,
					defaults : {
						columnWidth : .333,
						layout : 'form',
						labelWidth : 80
					},
					items : [{
						columnWidth : 1,
						xtype : 'label',
						id : 'txtTest',
						style : 'text-align:left;padding-bottom:3px;',
						text : '折扣方案:'
						
					}]
					
				},{
					xtype : 'combo',
					id : 'comboDiscount',
					fieldLabel : '默认方案' + Ext.ux.txtFormat.xh,
					forceSelection : true,
					allowBlank : false,
					blankText : '折扣方案不能为空.',
					store : new Ext.data.JsonStore({
						fields : [ 'discountID', 'text' ]
					}),
					valueField : 'discountID',
					displayField : 'text',
					typeAhead : true,
					mode : 'local',
					triggerAction : 'all',
					selectOnFocus : true,
					listeners : {
						focus  : function(thiz){
							var mDiscountSelectedList = []; 
							var mDiscountSelecteds = document.getElementsByName('memberDiscount');
							for (var i = 0; i < mDiscountSelecteds.length; i++) {
								if(mDiscountSelecteds[i].checked){
									mDiscountSelectedList.push({'discountID':mDiscountSelecteds[i].value,'text':mDiscountSelecteds[i].nextSibling.innerHTML});
								}
								
							}
							thiz.store.loadData(mDiscountSelectedList);
						}
					}
				}]
			}],
			bbar : ['->', {
				text : '保存',
				id : 'btnSaveMemberType',
				iconCls : 'btn_save',
				handler : function(e){
					var typeID = Ext.getCmp('numTypeID');
					var typeName = Ext.getCmp('txtTypeName');
					var chargeRate = Ext.getCmp('numChargeRate');
					var exchangeRate = Ext.getCmp('numExchangeRate');
					var initialPoint = Ext.getCmp('numInitialPoint');
					var discount = Ext.getCmp('comboDiscount');
					var attribute = Ext.getCmp('comboAttribute');
					var desc = Ext.getCmp('txtCommentForMemberType');
					
					if(!typeName.isValid() || !chargeRate.isValid() || !exchangeRate.isValid() 
							|| !initialPoint.isValid() || !attribute.isValid() || !discount.isValid()){
						return;
					}
					var memberDiscountCheckeds = "";
					
					var save = Ext.getCmp('btnSaveMemberType');
					var close = Ext.getCmp('btnCloseMemberType');
					
					save.setDisabled(true);
					close.setDisabled(true);
					Ext.Ajax.request({
						url : '../../OperateMemberType.do',
						params : {
							dataSource : m_memberTypeWin.otype.toLowerCase(),
							restaurantID : restaurantID,
							typeID : typeID.getValue(),
							typeName : typeName.getValue(),
							discountID : discount.getValue(),
							exchangeRate : exchangeRate.getValue(),
							initialPoint : initialPoint.getValue(),
							chargeRate : chargeRate.getValue(),
							attr : attribute.getValue(),
							desc : desc.getValue(),
							memberDiscountCheckeds : getChecked(memberDiscountCheckeds, document.getElementsByName('memberDiscount'))
						},
						success : function(res, opt){
							var jr = Ext.decode(res.responseText);
							if(jr.success){
								Ext.example.msg(jr.title, jr.msg);
								m_memberTypeWin.hide();
								Ext.getCmp('btnRefreshMemberType').handler();
							}else{
								Ext.ux.showMsg(jr);								
							}
							save.setDisabled(false);
							close.setDisabled(false);
						},
						failure : function(res, opt){
							Ext.ux.showMsg(Ext.decode(res.responseText));
							save.setDisabled(false);
							close.setDisabled(false);
						}
					});
				}
			}, {
				text : '关闭',
				id : 'btnCloseMemberType',
				iconCls : 'btn_close',
				handler : function(e){
					m_memberTypeWin.hide();
					Ext.getCmp('txtTypeName').enable();
				}
			}],
			listeners : {
				beforeshow : function(e){
					if(document.getElementsByName('memberDiscount').length == 0){
						for (var i = 0; i < discountData.length; i++) {
							var c = {items : [{
								xtype : "checkbox", 
								name : "memberDiscount",
								boxLabel : checkLabel(discountData[i].text) , 
								hideLabel : true, 
								inputValue :  discountData[i].discountID,
								listeners : {
									focus : function(){
										Ext.getCmp('comboDiscount').setValue();
										Ext.getCmp('comboDiscount').clearInvalid();
									},
									check : function(){
										Ext.getCmp('comboDiscount').setValue();
										Ext.getCmp('comboDiscount').clearInvalid();
									}
								}
							}]};
							Ext.getCmp('formMemberDiscount').add(c);
							//solveIE自动换行时格式错乱
							if((i+1)%6 == 0){
								Ext.getCmp('formMemberDiscount').add({columnWidth : 1});
							}
							Ext.getCmp('formMemberDiscount').doLayout();
						}
					}
				},
				hide : function(){
					var discounts = document.getElementsByName('memberDiscount');
					for (var i = 0; i < discounts.length; i++) {
						if(discounts[i].checked){
							discounts[i].checked = false;
						}
					}
				}
			},
			keys : [{
				key : Ext.EventObject.ENTER,
				fn : function(){
					Ext.getCmp('btnSaveMemberType').handler();
				},
				scope : this
			}, {
				key : Ext.EventObject.ESC,
				fn : function(){
					Ext.getCmp('btnCloseMemberType').handler();
				},
				scope : this
			}]
		});
	}

};


function insertMemberTypeHandler(){
	memberTypeOperationHandler({
		type : mtObj.operation['insert']
	});
};
function updateMemberTypeHandler(){
	memberTypeOperationHandler({
		type : mtObj.operation['update']
	});
};
function deleteMemberTypeHandler(){
	memberTypeOperationHandler({
		type : mtObj.operation['delete']
	});
};

function bindMemberTypeData(d){
	var typeID = Ext.getCmp('numTypeID');
	var typeName = Ext.getCmp('txtTypeName');
	var chargeRate = Ext.getCmp('numChargeRate');
	var exchangeRate = Ext.getCmp('numExchangeRate');
	var initialPoint = Ext.getCmp('numInitialPoint');
	var discount = Ext.getCmp('comboDiscount');
	var attribute = Ext.getCmp('comboAttribute');
	var desc = Ext.getCmp('txtCommentForMemberType');
	
	typeID.setValue(d['memberTypeId']);
	typeName.setValue(d['memberTypeName']);
	//是微信就不能该名字
	if(d['type'] == 2){
		typeName.disable();
	}
	exchangeRate.setValue(d['exchangeRate']);
	initialPoint.setValue(typeof d['initialPoint'] != 'undefined' ? d['initialPoint'] : 0);
	
	if(typeof d['attributeValue'] == 'undefined'){
		attribute.setValue(1);
	}else{
		attribute.setValue(d['attributeValue']);
	}
	attribute.fireEvent('select', attribute);
	chargeRate.setValue(d['chargeRate']);
	discount.setValue(d['discount']);
	desc.setValue(d['desc']);
	
	typeID.clearInvalid();
	typeName.clearInvalid();
	chargeRate.clearInvalid();
	initialPoint.clearInvalid();
	attribute.clearInvalid();
	discount.clearInvalid();
};

function getChecked(checkeds, checkBoxs){
	for (var i = 0; i < checkBoxs.length; i++) {
		if(checkBoxs[i].checked){
			if(checkeds == ""){
				checkeds += checkBoxs[i].value;
			}else{
				checkeds += "," + checkBoxs[i].value;
			}
		}
	}
	return checkeds;
}

function memberTypeOperationHandler(c){
	if(c == null || typeof c == 'undefined' || typeof c.type == 'undefined'){
		return;
	}
	m_memberTypeWinInit();
	m_memberTypeWin.otype = c.type;
	if(c.type == mtObj.operation['insert']){
		m_memberTypeWin.setTitle('添加会员类型');
		m_memberTypeWin.show();
		m_memberTypeWin.center();
		Ext.getCmp('txtTypeName').focus(true, 100);
		
		bindMemberTypeData({
			discountType : 0,
			chargeRate : 1.00,
			exchangeRate : 1.00,
			initialPoint : 0
		});
		
	}else if(c.type == mtObj.operation['update']){
		var sd = Ext.ux.getSelNode(memberTypeTree);
		if(!sd){
			Ext.example.msg('提示', '请选中一个会员类型再进行操作.');
			return;
		}
		m_memberTypeWin.setTitle('修改会员类型');
		m_memberTypeWin.show();
		
		var discounts = document.getElementsByName('memberDiscount');
		for (var i = 0; i < sd.attributes['discounts'].length; i++) {
			for (var j = 0; j < discounts.length; j++) {
				if(sd.attributes['discounts'][i].discountId == discounts[j].value){
					
					discounts[j].checked = true;
				}
			}
		}
		var mDiscountSelectedList = []; 
		var mDiscountSelecteds = document.getElementsByName('memberDiscount');
		for (var i = 0; i < mDiscountSelecteds.length; i++) {
			if(mDiscountSelecteds[i].checked){
				mDiscountSelectedList.push({'discountID':mDiscountSelecteds[i].value,'text':mDiscountSelecteds[i].nextSibling.innerHTML});
			}
			
		}
		Ext.getCmp('comboDiscount').store.loadData(mDiscountSelectedList);
		
		bindMemberTypeData(sd.attributes);
		
		m_memberTypeWin.center();
	}else if(c.type == mtObj.operation['delete']){
		var sd = Ext.ux.getSelNode(memberTypeTree);
		if(!sd){
			Ext.example.msg('提示', '请选中一个会员类型再进行操作.');
			return;
		}
		Ext.Msg.show({
			title : '提示',
			msg : '是否删除会员类型?<br><font color="red">提示:如果该类型下已有会员则删除失败.</font>',
			buttons : Ext.Msg.YESNO,
			fn : function(e){
				if(e == 'yes'){
					Ext.Ajax.request({
						url : '../../OperateMemberType.do',
						params : {
							dataSource : 'delete',
							typeID : sd.attributes.memberTypeId
						},
						success : function(res, opt){
							var jr = Ext.decode(res.responseText);
							if(jr.success){
								Ext.example.msg(jr.title, jr.msg);
								Ext.getCmp('btnRefreshMemberType').handler();
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


//************************会员等级
function deleteMemberLevel(){
	if(!memberLevelDetail){
		Ext.example.msg('提示', '操作失败, 请选中一条数据再进行操作.');
		return;
	}
	Ext.Msg.confirm(
		'提示',
		'是否删除: ' + memberLevelDetail['xAxisText'],
		function(e){
			if(e == 'yes'){
				Ext.Ajax.request({
					url : '../../OperateMemberLevel.do',
					params : {
						id : memberLevelDetail['id'],
						dataSource : 'delete'
					},
					success : function(res, opt){
						var jr = Ext.util.JSON.decode(res.responseText);
						if(jr.success){
							Ext.example.msg(jr.title, String.format(Ext.ux.txtFormat.deleteSuccess, memberLevelDetail['xAxisText']));
							memberLevelAddWin.hide();
							member_loadMemberTypeChart();
							
						}else{
							Ext.ux.showMsg(jr);
						}
					},
					failure : function(res, opt){
						Ext.ux.showMsg(Ext.util.JSON.decode(res.responseText));
					}
				});
			}
		}
	);

}
function initAddLevelWin(){
	memberLevelAddWin = new Ext.Window({
		id : 'memberLevelAddWin',
		title : '添加等级',
		closable : false,
		modal : true,
		resizable : false,
		width : 250,
		bbar : [{
			text : '删除',
			id : 'btn_memberLevelDel',
			iconCls : 'btn_delete',
			handler : function(){
				deleteMemberLevel();
			}
		},'->',{
			text : '保存',
			id : 'btn_memberLevelAdd',
			iconCls : 'btn_save',
			handler : function(){
				var pointThreshold = Ext.getCmp('txtPointThreshold');
				var combo_memberTypeId = Ext.getCmp('combo_memberLevel_mType');
				
				if(!pointThreshold.isValid() || !combo_memberTypeId.isValid()){
					return;
				}
				Ext.Ajax.request({
					url : '../../OperateMemberLevel.do',
					params : {
						id : Ext.getCmp('txtMemberLevelId').getValue(),
						pointThreshold : pointThreshold.getValue(),
						memberTypeId : combo_memberTypeId.getValue(),
						dataSource : memberLevelAddWin.otype
					},
					success : function(res, opt){
						var jr = Ext.decode(res.responseText);
						if(jr.success){
							Ext.ux.showMsg(jr);
							memberLevelAddWin.hide();
							member_loadMemberTypeChart();
						}else{
							Ext.ux.showMsg(jr);
						}
						
					},
					failure : function(res, opt){
						Ext.ux.showMsg(Ext.util.JSON.decode(res.responseText));
					}
				});
			}
		}, {
			text : '取消',
			id : 'btn_cancelMmemberLevelWin',
			iconCls : 'btn_close',
			handler : function(){
				memberLevelAddWin.hide();
			}
		}],
		keys: [{
			key : Ext.EventObject.ENTER,
			scope : this,
			fn : function(){
				Ext.getCmp('btn_memberLevelAdd').handler();
			}
		},{
			key : Ext.EventObject.ESC,
			scope : this,
			fn : function(){
				memberLevelAddWin.hide();
			}
		}],
		listeners : {
			show : function(){
				var data = {};
				if(memberLevelAddWin.otype == 'update'){
					data = memberLevelDetail;
					Ext.getCmp('btn_memberLevelDel').show();
				}else if(memberLevelAddWin.otype == 'insert'){
					data = {};
					Ext.getCmp('btn_memberLevelDel').hide();
				}
//				Ext.getCmp('combo_memberLevel_mType').store.loadData(memberTypeData.root);
				operateMemberLevelData({
					type : 'SET',
					data : data
				});
			},
			hide : function(){
				memberLevelDetail="";
			}
		},
		items : [{
			layout : 'form',
			labelWidth : 60,
			width : 250,
			border : false,
			frame : true,
			items : [{
				xtype : 'numberfield',
				fieldLabel : '积分',
				id : 'txtPointThreshold',
				allowBlank : false,
				width : 130,
				validator : function(v){
					if(Ext.util.Format.trim(v).length > 0){
						return true;
					}else{
						return '积分不能为空.';
					}
				}
			},{
				xtype : 'combo',
				id : 'combo_memberLevel_mType',
				fieldLabel : '会员类型',
				readOnly : false,
				forceSelection : true,
				width : 130,
				listWidth : 120,
				store : new Ext.data.SimpleStore({
					fields : ['id', 'name']
				}),
				valueField : 'id',
				displayField : 'name',
				typeAhead : true,
				mode : 'local',
				triggerAction : 'all',
				selectOnFocus : true,
				allowBlank : false
			}, {
				xtype : 'hidden',
				id : 'txtMemberLevelId'
			}]
		}]
	});
}

function initMemberTypeData(){
	combo_memberTypeData = [];
	var thiz = Ext.getCmp('combo_memberLevel_mType');
	Ext.Ajax.request({
		url : '../../QueryMemberType.do',
		params : {
			dataSource : 'notBelongType'
		},
		success : function(res, opt){
			var jr = Ext.decode(res.responseText);
			for(var i = 0; i < jr.root.length; i++){
				combo_memberTypeData.push([jr.root[i]['id'], jr.root[i]['name']]);
			}
			thiz.store.loadData(combo_memberTypeData);
		},
		fialure : function(res, opt){
			thiz.store.loadData(combo_memberTypeData);
		}
	});
}

function member_operateMemberLevel(y, x){
	initMemberTypeData();
	var list = memberLevels;
	var otype = 'insert';
	for (var i = 0; i < list.length; i++) {
		if(list[i].pointThreshold == y){
			otype = 'update';
			memberLevelDetail = list[i];
			memberLevelDetail['xAxisText'] = x;
			combo_memberTypeData.push([memberLevelDetail['memberTypeId'], memberLevelDetail['memberTypeName']]);
			Ext.getCmp('combo_memberLevel_mType').store.loadData(combo_memberTypeData);
		}
	}
	
	operateMemberLevel({otype:otype});
}

function operateMemberLevel(c){
	if(c == null || typeof c == 'undefined' || typeof c.otype == 'undefined'){
		return;
	}
	memberLevelAddWin.otype = c.otype;
	if(c.otype == 'insert'){
		memberLevelAddWin.setTitle('添加等级');
//		initMemberTypeData();
	}else if(c.otype == 'update'){
		memberLevelAddWin.setTitle('修改等级');
	}
	memberLevelAddWin.show();
	memberLevelAddWin.center();
}

function operateMemberLevelData(c){
	if(c == null || typeof c == 'undefined' || typeof c.type == 'undefined'){
		return;
	}
	var data = {};
	var pointThreshold = Ext.getCmp('txtPointThreshold');
	var combo_memberTypeId = Ext.getCmp('combo_memberLevel_mType');
	var memberLevelId = Ext.getCmp('txtMemberLevelId');
	if(c.type.toUpperCase() == Ext.ux.otype['set'].toUpperCase()){
		data = c.data;
		pointThreshold.setValue(data['pointThreshold']);
		
		if(data['memberTypeId']){
			combo_memberTypeId.setValue(data['memberTypeId']);
		}else{
			combo_memberTypeId.setValue();
		}
		memberLevelId.setValue(data['id']);
		pointThreshold.focus(true, 100);
		
	}else if(c.type.toUpperCase() == Ext.ux.otype['get'].toUpperCase()){
		data = {
			memberLevelId : memberLevelId.getValue(),
			memberTypeId : combo_memberTypeId.getValue(),
			pointThreshold : pointThreshold.getValue()
		};
		c.data = data;
	}
	
	pointThreshold.clearInvalid();
	combo_memberTypeId.clearInvalid();
	return c;
}
var colors = ['SpringGreen', 'GreenYellow', 'cyan'];
var member_compare = function (obj1, obj2) {
    var val1 = obj1.y;
    var val2 = obj2.y;
    if (val1 < val2) {
        return -1;
    } else if (val1 > val2) {
        return 1;
    } else {
        return 0;
    }            
}; 
function member_loadMemberTypeChart(){
	$.ajaxSetup({async : false});
	var xAxisData = [], yAxisData = [];
	$.post('../../QueryMemberLevel.do', {dataSource : 'chart'}, function(res){
		memberLevels = res.root;
		chartDatas = eval('(' + res.other.chart + ')');

		//重新根据积分排序
		yAxisData = chartDatas.data.sort(member_compare);
		
		for (var i = 0; i < yAxisData.length; i++) {
			xAxisData.push(yAxisData[i].x);
			delete yAxisData[i].x;
			if(yAxisData[i].color){
				yAxisData[i].dataLabels = {
		            enabled: true,
		            color: 'black',
		            align: 'right',
		            style: {
		                fontSize: '15px',
		                fontFamily: 'Verdana, sans-serif'
		            }
		        };
			}
		}		
	 	var chart = {
/*			    chart: {
			        type: 'bar',
			        renderTo : 'divMemberTypeLevelChart'
			    },
			    title: {
			        text : '<font style="font-size:15px;color:red;font-weight:bold;textShadow: \'0 0 3px black\'">★点击等级条可设置积分</font>'
			    },
				plotOptions : {
					bar : {
						allowPointSelect :true,
						cursor : 'pointer',
						events : {
							click : function(e){
								member_operateMemberLevel(e.point.y, e.point.category);
							}
						}
					}
				},        
			    xAxis: {                                                           
			        categories: xAxisData,
			        labels : {
			        	style : {
							color : 'green',
							fontWeight : 'bold',
							fontSize : '15px'
			        	}
			        },
			        title: {                                                       
			            text: null                                                 
			        }                                                              
			    },          
			    yAxis: {
			        min: 0,
			        align: 'right',
			        title: {
			            text: '积分'
			        }
			    },
			    tooltip: {
			    	enabled : false,
			        pointFormat: '<table><tbody><tr><td style="color:red;padding:0">{series.name}: </td><td style="padding:0"><b>{point.y} </b></td></tr></tbody></table>',
			        shared: true,
			        useHTML: true
			    },
			   legend: {                                                          
			        enabled : false
			    },
			    credits : {
			    	enabled : false
			    },         
			    exporting : {
			    	enabled : false
			    },
			    series: [{                                                         
			        name: '积分',                                             
			        data: yAxisData
           
			    }]     
			    
			    */
			    
			    chart: {
			        type: 'spline',
			        inverted: true,
			        renderTo : 'divMemberTypeLevelChart'
			    },
			    title: {
			        text: ''
			    },
			    xAxis: {
			    	reversed : false,
			        title: {
			            enabled: true,
			            text: '积分'
			        },
			        labels: {
			            formatter: function() {
			                return this.value +'分';
			            }
			        },
			        min: -10,
			        showLastLabel: true
			    },
			    yAxis: {
			        title: {
			            text: '等级'
			        },
			        labels: {
			            formatter: function() {
			                return this.value ;
			            }
			        },
			        lineWidth: 2
			    },
			    legend: {
			        enabled: false
			    },
			    tooltip: {
			        headerFormat: '<b>{series.name}</b><br/>',
			        pointFormat: '{point.x} km: {point.y}°C'
			    },
			    plotOptions: {
			        spline: {
			            marker: {
			                enable: false
			            }
			        }
			    },
				plotOptions : {
					line : {
						cursor : 'pointer',
						dataLabels : {
							enabled : true,
							style : {
								fontWeight: 'bold', 
								color: 'green' 
							}
						},
						events : {
							click : function(e){
								each(e.point.category);
							}
						}
					}
				},			    
			    credits : {
			    	enabled : false
			    },         
			    exporting : {
			    	enabled : false
			    },			    
			    series: [{
			        name: 'Temperature',
			        data: [[0, 15], [10, 15], [100, 15]]
			    }]			    
			};
			new Highcharts.Chart(chart);
	});
}
var memberMgr_obj = {treeId : 'tree_memberTypeMgr', option : [{name:'修改', fn:"updateMemberTypeHandler()"},{name:'删除', fn:"deleteMemberTypeHandler()"}]};
var memberLevels, memberLevelDetail;
Ext.onReady(function(){
	member_dataInit();
	
	treeInit();
	gridInit();
	
	member_dataInit();
	
	member_loadMemberTypeChart();
	
	var memberTypePanel = new Ext.Panel({
		layout : 'border',
		width : 240,
		frame : false,
		region : 'west',
		items : [memberTypeTree
				, new Ext.Panel({
					title : '会员等级路线图',
					region : 'south',
					contentEl : 'divMemberTypeLevelChart'
				})
		]
	});
	
	new Ext.Panel({
		renderTo : 'divMember',
		id : 'memberMgrPanel',
		//width : parseInt(Ext.getDom('divMember').parentElement.style.width.replace(/px/g,'')),
		height : parseInt(Ext.getDom('divMember').parentElement.style.height.replace(/px/g,'')),
		layout : 'border',
		items : [memberTypePanel, memberBasicGrid],
		autoScroll : true,
		tbar : new Ext.Toolbar({
			height : 55,
			items : [
			    {xtype : 'tbtext', text : '&nbsp;&nbsp;'},
			    btnInsertMember,
			    {xtype : 'tbtext', text : '&nbsp;&nbsp;'},
			    btnRecharge,
			    {xtype : 'tbtext', text : '&nbsp;&nbsp;'},
			    btnTakeMoney,
			    {xtype : 'tbtext', text : '&nbsp;&nbsp;'},
			    btnAdjustPoint,
			    {xtype : 'tbtext', text : '&nbsp;&nbsp;'},
			    btnConsumeSummary,
			    {xtype : 'tbtext', text : '&nbsp;&nbsp;'},
			    btnConsumeDetail,
			    {xtype : 'tbtext', text : '&nbsp;&nbsp;'},
			    btnRechargeDetail
			]
		})
	});
	 
	winInit();
	memberBasicWin.render(document.body);
	Ext.ux.checkSmStat();
	
	memberTipWin = Ext.ux.ToastWindow({
		width : 260,
		height : 152,
		html : '<div style="position:relative;background-color: whitesmoke;height :120px;" align="center">' +
				'<br><p style="color: #f00;text-shadow: 1px 1px 0px #212121;font-size: 19px;">您可以对会员进行如下操作 : </p><br>' +
				'<input type="button" value="发送优惠劵" class="operationBtn" style="margin-right:10px;" onclick="javascript:initMemberCouponWin();memberTipWin.hide();"/>' +
				'<input  class="operationBtn" type="button" value="发送问候短信"/><br>' +
				'<div style="position:absolute;left : 0; bottom: 3px;"><input id="chxMemberTip" type="checkbox" onclick="javascript:fnRemberTip()" />不再显示</div>'+
			'</div>'
	});
	showFloatOption(memberMgr_obj);
	
	initAddLevelWin();
});
