﻿
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
	var attendtion;
	if(record.data['acctendtioned']){
		attendtion = '<a href="javascript:cancelAttentionHandler()">取消关注</a>';
	}else{
		attendtion = '<a href="javascript:attentionHandler()">添加关注</a>';
	}
	return ''
		+ '<a href="javascript:updateMemberHandler()">修改</a>'
		+ '&nbsp;&nbsp;&nbsp;&nbsp;'
		+ '<a href="javascript:deleteMemberHandler()">删除</a>'
		+ '&nbsp;&nbsp;&nbsp;&nbsp;'
		+ '<a href="javascript:memberOperationSend() ">操作明细</a>'
		+ '&nbsp;&nbsp;&nbsp;&nbsp;'
		+ attendtion;
//		+ '&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;'
//		+ '<a href="javascript:adjustPoint()">积分调整</a>';
};
/**************************************************/
function treeInit(){
	var memberTypeTreeTbar = new Ext.Toolbar({
		items : ['->', {
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
		id : 'tree_memberMgr',
		title : '会员类型',
		region : 'west',
		width : 200,
		border : true,
		rootVisible : false,
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
				
				var tipWinShow = false;
				
				if(memberTypeNode){
					if(memberTypeNode.childNodes.length > 0 && memberTypeNode.attributes.memberTypeId != -1){
						gs.baseParams['memberType'] = '';
						gs.baseParams['memberTypeAttr'] = memberTypeNode.attributes.attr;
					}else{
						if(memberTypeNode.attributes.attr >= 0){
							gs.baseParams['memberType'] = '';
							gs.baseParams['memberTypeAttr'] = memberTypeNode.attributes.attr;
						}else{
							gs.baseParams['memberType'] = memberTypeNode.attributes.memberTypeId;
							gs.baseParams['memberTypeAttr'] = '';
						}
					}
				}else{
					gs.baseParams['memberType'] = '';
					gs.baseParams['memberTypeAttr'] = '';
				}
				
				gs.baseParams['memberCardOrMobileOrName'] = Ext.getCmp('numberSearchByMemberPhoneOrCardOrName').getValue();
//				gs.baseParams['totalBalance'] = searchType == 4 ? searchValue : '';
				gs.baseParams['MinTotalMemberCost'] = Ext.getCmp('textTotalMinMemberCost').getValue();
				gs.baseParams['MaxTotalMemberCost'] = Ext.getCmp('textTotalMaxMemberCost').getValue();
				gs.baseParams['consumptionMinAmount'] = Ext.getCmp('textTotalMinMemberCostCount').getValue();
				gs.baseParams['consumptionMaxAmount'] = Ext.getCmp('textTotalMaxMemberCostCount').getValue();
//				gs.baseParams['point'] = Ext.getCmp('numberSearchByMemberPhoneOrCard').getValue();
//				gs.baseParams['usedPoint'] = Ext.getCmp('numberSearchByMemberPhoneOrCard').getValue();
				gs.baseParams['memberBalance'] = Ext.getCmp('textMemberBalance').getValue();
				gs.baseParams['memberBalanceEqual'] = Ext.getCmp('memberBalanceEqual').getValue();
//				gs.baseParams['so'] = Ext.getCmp('comboSearchValueByOperation').getValue();
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
					if(gs.getTotalCount() > 0 && tipWinShow){
						memberBasicGrid.getSelectionModel().selectAll();
//						if(Ext.ux.getCookie(document.domain+'_memberTip') != 'true'){
							memberTipWin.show();
//						}					
					}
					
					tipWinShow = true;
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
				var searchType = Ext.getCmp('mr_comboMemberSearchType').getValue();
				var searchValue = Ext.getCmp(mObj.searchValue) ? Ext.getCmp(mObj.searchValue).getValue() : '';
				
				var memberType, memberTypeAttr;
				
				if(memberTypeNode){
					if(memberTypeNode.childNodes.length > 0 && memberTypeNode.attributes.memberTypeId != -1){
						memberType = '';
						memberTypeAttr = memberTypeNode.attributes.attr;
					}else{
						if(memberTypeNode.attributes.attr >= 0){
							memberType = '';
							memberTypeAttr = memberTypeNode.attributes.attr;
						}else{
							memberType = memberTypeNode.attributes.memberTypeId;
							memberTypeAttr = '';
						}
					}
				}else{
					memberType = '';
					memberTypeAttr = '';
				}
				var url = '../../{0}?memberType={1}&memberTypeAttr={2}&name={3}&memberCard={4}&mobile={5}&totalBalance={6}' +
						'&usedBalance={7}&consumptionAmount={8}&point={9}&usedPoint={10}&so={11}&dataSource={12}';
				url = String.format(
					url, 
					'ExportHistoryStatisticsToExecl.do', 
					memberType,
					memberTypeAttr,
					searchType == 1 ? searchValue : '',
					searchType == 2 ? searchValue : '',
					searchType == 3 ? searchValue : '',
					searchType == 4 ? searchValue : '',
					searchType == 5 ? searchValue : '',
					searchType == 6 ? searchValue : '',
					searchType == 7 ? searchValue : '',
					searchType == 8 ? searchValue : '',
					Ext.getCmp('comboSearchValueByOperation').getValue(),
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
			[true, true, false, true],
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
					interval: 500
				};
				
				thiz.center();
				thiz.load({
					url : '../window/client/controlMember.jsp', 
					scripts : true,
					params : {
						otype : memberBasicWin.otype
					},
					callback : function(){
						Ext.TaskMgr.start(task);
					}
				});
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

var btnMemberCouponWin = new Ext.ux.ImageButton({
	imgPath : '../../images/sendCoupon.png',
	imgWidth : 50,
	imgHeight : 50,
	tooltip : '发送优惠劵',
	handler : function(e){
		var data = memberBasicGrid.getSelectionModel().getSelections();
		if(data.length < 1){
			Ext.example.msg('提示', '请选中会员后进行操作.');
			return;
		}		
		
		initMemberCouponWin();
	}
});

function fnRemberTip(){
	if(Ext.getDom('chxMemberTip').checked){
		Ext.ux.setCookie(document.domain+'_memberTip', true, 3650);
	}else{
		Ext.ux.setCookie(document.domain+'_memberTip', false, 3650);
	}
}

/**********************************************************************/
Ext.onReady(function(){
	treeInit();
	gridInit();
	
	new Ext.Panel({
		renderTo : 'divMember',
		id : 'memberMgrPanel',
		//width : parseInt(Ext.getDom('divMember').parentElement.style.width.replace(/px/g,'')),
		height : parseInt(Ext.getDom('divMember').parentElement.style.height.replace(/px/g,'')),
		layout : 'border',
		items : [memberTypeTree, memberBasicGrid],
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
			    btnRechargeDetail,
			    {xtype : 'tbtext', text : '&nbsp;&nbsp;'},
			    btnMemberCouponWin
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
	
});
