//-------------------------lib.js
/**********************************************************************/

Ext.onReady(function(){
	
	var memberTypeData = {root:[]};
	var discountData = [], pricePlanData = [];
	var mtObj = {
			operation : {
				'insert' : 'INSERT',
				'update' : 'UPDATE',
				'delete' : 'DELETE'
			}
	};	
		
	var memberTypeTree = null;
	var memberBasicGrid = null;
	var memberBasicWin = null;
	var adjustPointWin = null;
	var memberCouponWin = null;
	var m_memberTypeWin = null;
	var memberDisountWin = null;
	var m_searchAdditionFilter = 'create';
	var memberLevels = null;
	var memberLevelDetail = null;
	var memberTypeLevelChart = null;
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
		winInit();	
		
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

	var rechargeWin = null;
	function initRechargeWin(){
		if(!rechargeWin){
			rechargeWin = new Ext.Window({
				title : '会员充值',
				closable : false,
				modal : true,
				resizable : false,
				width : 680,
				height : 300,
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
						var id = data && data['memberType']['attributeValue'] == 0 ? data['id'] : '';
						var task = {
							run : function(){
								if(typeof rechargeNumberFocus == 'function'){
									if(!id){
										rechargeNumberFocus();
									}
									Ext.TaskMgr.stop(this);
								}
							},
							interval: 300
						};					
						thiz.center();
						thiz.load({
							url : '../window/client/recharge.jsp',
							scripts : true,
							params : {
								memberMobile : id
							}
						});
						Ext.TaskMgr.start(task);
					}
				},
				bbar : [{
					xtype : 'checkbox',
					id : 'printRecharge_checkbox_member',
					checked : true,
					boxLabel : '打印充值信息'
				},{
					xtype : 'tbtext',
					text : '&nbsp;&nbsp;'
				},{
					xtype : 'checkbox',
					id : 'chbSendCharge_checkbox_member',
					checked : true,
					boxLabel : '发送充值信息'+(Ext.ux.smsCount >= 20 ? '(<font style="color:green;font-weight:bolder">剩余'+Ext.ux.smsCount+'条</font>)' : '(<font style="color:red;font-weight:bolder">剩余'+Ext.ux.smsCount+'条, 请及时充值</font>)'),
					hidden : !Ext.ux.smsModule
				}, '->', {
					text : '充值',
					iconCls : 'icon_tb_recharge',
					handler : function(e){
						var sendSms = Ext.getCmp('chbSendCharge_checkbox_member').getValue();
						if(sendSms){
							Ext.ux.setCookie(document.domain+'_chargeSms', true, 3650);
						}else{
							Ext.ux.setCookie(document.domain+'_chargeSms', false, 3650);
						}
						// 跨域调用充值方法
						rechargeControlCenter({
							isPrint : Ext.getCmp('printRecharge_checkbox_member').getValue(),
							sendSms : sendSms,
							callback : function(_c){
								rechargeWin.hide();
								Ext.getCmp('searchMember_btn_member').handler();
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
			Ext.getCmp('chbSendCharge_checkbox_member').setValue(true);
		}else{
			Ext.getCmp('chbSendCharge_checkbox_member').setValue(false);
		}
	}

	/**
	 * 会员取款
	 */
	var takeMoneyWin = null;
	function initTakeMoneyWin(){
		if(!takeMoneyWin){
			takeMoneyWin = new Ext.Window({
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
						var id = data && data['memberType']['attributeValue'] == 0 ? data['id'] : '';
						var task = {
							run : function(){
								if(typeof takeMoneyNumberFocus == 'function'){
									if(!id){
										takeMoneyNumberFocus();
									}
									Ext.TaskMgr.stop(this);
								}
							},
							interval: 200
						};	
						thiz.center();
						thiz.load({
							url : '../window/client/takeMoney.jsp',
							scripts : true,
							params : {
								memberMobile : id
							}
						});
						Ext.TaskMgr.start(task);
					}
				},
				bbar : [{
					xtype : 'checkbox',
					id : 'printTakeMoney_checkbox_member',
					checked : true,
					boxLabel : '打印取款信息'
				}, '->', {
					text : '取款',
					iconCls : 'icon_tb_recharge',
					handler : function(){
						// 跨域调用取款方法
						takeMoneyControlCenter({
							isPrint : Ext.getCmp('printTakeMoney_checkbox_member').getValue(),
							callback : function(_c){
								takeMoneyWin.hide();
								Ext.getCmp('searchMember_btn_member').handler();
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
		rechargeWin.show();		
	}

	function takeMoneyHandler(){
		initTakeMoneyWin();
		takeMoneyWin.show();
	}
	
	var mr_queryMemberOperationWin = null;
	/**
	 * 会员操作明细
	 */
	function queryMemberOperationHandler(title, url, params){
		if(!mr_queryMemberOperationWin){
		mr_queryMemberOperationWin = new Ext.Window({
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
					id : 'sendCouponSms_checkbox_member',
					checked : true,
					boxLabel : '发送通知短信'+(Ext.ux.smsCount >= 20 ? '(<font style="color:green;font-weight:bolder">剩余'+Ext.ux.smsCount+'条</font>)' : '(<font style="color:red;font-weight:bolder">剩余'+Ext.ux.smsCount+'条, 请及时充值</font>)'),
					hidden : !Ext.ux.smsModule
				},'->',{
					text : '发放',
					id : 'memberCouponSend_btn_member',
					iconCls : 'btn_save',
					handler : function(){
						var sendSms = Ext.getCmp('sendCouponSms_checkbox_member').getValue();
						if(sendSms){
							Ext.ux.setCookie(document.domain+'_couponSms', true, 3650);
						}else{
							Ext.ux.setCookie(document.domain+'_couponSms', false, 3650);
						}		
						
						if(!Ext.getCmp('comboCouponType_combo_member').isValid()){
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
										var coupon = Ext.getCmp('comboCouponType_combo_member');
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
							var coupon = Ext.getCmp('comboCouponType_combo_member');
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
						Ext.getCmp('memberCouponSend_btn_member').handler();
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
						id : 'comboCouponType_combo_member',
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
			Ext.getCmp('sendCouponSms_checkbox_member').setValue(true);
		}else{
			Ext.getCmp('sendCouponSms_checkbox_member').setValue(false);
		}	
		memberCouponWin.show();
	}


	/**
	 * 
	 */
	function initAdjustPointWin(){
		if(!adjustPointWin){
			var numAdjustPoint = new Ext.form.NumberField({
				id : 'numAdjustPoint',
				xtype : 'numberfield',
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
						Ext.getCmp('radioAdjustPointIncrease_radio_member').setValue(true);
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
							id : 'radioAdjustPointIncrease_radio_member',
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
							id : 'numMemberPointForNow_textfield_member',
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
												Ext.getCmp('searchMember_btn_member').handler();
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
						Ext.getCmp('radioAdjustPointIncrease_radio_member').setValue(true);
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
		Ext.getCmp('numMemberPointForNow_textfield_member').setValue(data['point']);
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
		var recharge = '';
		if(record.data['memberType']['attributeValue'] == 0){
			recharge ='<a class="addMoney_a_member">充值</a>'
					+ '&nbsp;&nbsp;&nbsp;'
					+ '<a class="getMoney_a_member">取款</a>'
					+ '&nbsp;&nbsp;&nbsp;';
		}
		
		return ''
			+ '<a class="update_a_member">修改</a>'
			+ '&nbsp;&nbsp;&nbsp;&nbsp;'
			+ '<a class="delete_a_member">删除</a>'
			+ '&nbsp;&nbsp;&nbsp;&nbsp;'
			+ recharge
			+ '<a class="detail_a_member">操作明细</a>'
			+ '&nbsp;&nbsp;&nbsp;&nbsp;';
	};
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
				id : 'btnRefreshMemberType_btn_member',
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
//			region : 'west',
			region : 'center',
			width : 240,
			height : 220,
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
				memberTypeId : -1,
				listeners : {
					load : function(thiz){
						memberTypeData.root = [];
						for(var i = 0; i < thiz.childNodes.length; i++){
							memberTypeData.root.push({
								id : thiz.childNodes[i].attributes['memberTypeId'],
								name : thiz.childNodes[i].attributes['memberTypeName'],
								attributeValue : thiz.childNodes[i].attributes['attributeValue'],
								chargeRate : thiz.childNodes[i].attributes['chargeRate']
							});
						}
					}
				}
			}),
			tbar : memberTypeTreeTbar,
			listeners : {
		    	click : function(e){
		    		Ext.getCmp('searchMember_btn_member').handler();
		    		Ext.getDom('memberTypeShowType').innerHTML = e.text;
		    	}
		    }
		});
	};


	function winInit(){
		if(!memberBasicWin){
			var memeberCardAliasID = {
					xtype : 'numberfield',
					id : 'numberMemberCard_numberField_member',
					fieldLabel : '会员卡',
					disabled : false
			};
			var memberDetailCmp = new Ext.Panel({
				id : 'panelControlMemberContent_panel_member',
				frame : true,
				border : false,
				layout : 'column',
				width : 670,
				height : 200,
				defaults : {
					xtype : 'form',
					layout : 'form',
					labelWidth : 80,
					labelAlign : 'right',
					columnWidth : .33,
					defaults : {
						xtype : 'textfield',
						width : 110
					}
				},
				items : [{
					columnWidth : 1,
					items : [{
						xtype : 'hidden',
						id : 'numberMemberId_member',
						fieldLabel : '会员编号'
					}]
				}, {
					items : [{
						id : 'txtMemberName_text_member',
						fieldLabel : '名称' + Ext.ux.txtFormat.xh,
						allowBlank : false,
						blankText : '名称不能为空.'
					}]
				}, {
					items : [{
						id : 'txtMemberMobile_text_member',
						style : 'font-weight: bold; color: #FF0000;',
						fieldLabel : '手机',
						regex : Ext.ux.RegText.phone.reg,
						regexText : Ext.ux.RegText.phone.error
					}]
				}, {
					items : [memeberCardAliasID]
				}, {
					items : [{
						disabled : false,
						xtype : 'combo',
						id : 'comboMemberType_combo_member',
						fieldLabel : '会员类型' + Ext.ux.txtFormat.xh,
						readOnly : false,
						forceSelection : true,
						store : new Ext.data.JsonStore({
							fields : ['id', 'name', 'attributeValue', 'chargeRate']
						}),
						valueField : 'id',
						displayField : 'name',
						typeAhead : true,
						mode : 'local',
						triggerAction : 'all',
						selectOnFocus : true,
						allowBlank : false,
						blankText : '类型不允许为空.',
						listeners : {
							select : function(thiz, record, index){
								var firstCharge = Ext.getCmp('numFirstCharge_numberField_member');
								var firstActualCharge = Ext.getCmp('numFirstActualCharge_numberField_member');
								var rechargeType = Ext.getCmp('comboFirstRechargeType_combo_member');
								if(memberBasicWin.otype.toLowerCase() == Ext.ux.otype['insert'].toLowerCase() && record.get('attributeValue') == 0){
									firstCharge.show();
									firstCharge.getEl().up('.x-form-item').setDisplayed(true);	
									firstActualCharge.show();
									firstActualCharge.getEl().up('.x-form-item').setDisplayed(true);
									rechargeType.show();
									rechargeType.getEl().up('.x-form-item').setDisplayed(true);
									
									chargeRate = record.get('chargeRate');
									
									Ext.getCmp('chbPrintFirstRecharge_checkbox_member').show();
									
									if(Ext.ux.smsModule)
										Ext.getCmp('chbSendFirstCharge_checkbox_member').show();
									
								}else{
									firstCharge.hide();
									firstCharge.getEl().up('.x-form-item').setDisplayed(false);	
									firstActualCharge.hide();
									firstActualCharge.getEl().up('.x-form-item').setDisplayed(false);	
									rechargeType.hide();
									rechargeType.getEl().up('.x-form-item').setDisplayed(false);							
									
									Ext.getCmp('chbPrintFirstRecharge_checkbox_member').hide();
									Ext.getCmp('chbSendFirstCharge_checkbox_member').hide();
								}						
							}
						}
					}]
				}, {
					items : [{
							hidden : true,
						xtype : 'numberfield',
						id : 'numFirstCharge_numberField_member',
						fieldLabel : '首次充值',
						listeners : {
							render : function(thiz){
								Ext.getDom(thiz.getId()).onkeyup = function(){
									if(thiz.getRawValue() != ''){
										var iv = thiz.getValue();
										iv = parseInt(iv);
										if(iv < 1)
											iv = 0;
										if(iv > 100000)
											iv = 100000;
										thiz.setValue(parseInt(iv));
										
										var rm = thiz.getValue();
										var pmm = Ext.getCmp('numFirstActualCharge_numberField_member');
										pmm.setValue(Math.round(rm * chargeRate));
									}
								};
							}
						}
					}]
				}, {
					items : [{
							hidden : true,
						xtype : 'numberfield',
						id : 'numFirstActualCharge_numberField_member',
						fieldLabel : '账户充额'
					}]
				}, 	{
					items : [{
						xtype : 'combo',
						id : 'comboFirstRechargeType_combo_member',
						fieldLabel : '收款方式',
						readOnly : false,
						forceSelection : true,
						value : 1,
						store : new Ext.data.SimpleStore({
							fields : ['text', 'value'],
							data : [['现金', 1], ['刷卡', 2]]
						}),
						valueField : 'value',
						displayField : 'text',
						typeAhead : true,
						mode : 'local',
						triggerAction : 'all',
						selectOnFocus : true,
						allowBlank : false
					}]
					}, {
					items : [{
						xtype : 'combo',
						id : 'comboMemberSex_combo_member',
						fieldLabel : '性别' + Ext.ux.txtFormat.xh,
						readOnly : false,
						forceSelection : true,
						value : 0,
						store : new Ext.data.SimpleStore({
							fields : ['value', 'text'],
							data : [[0,'男'], [1, '女']]
						}),
						valueField : 'value',
						displayField : 'text',
						typeAhead : true,
						mode : 'local',
						triggerAction : 'all',
						selectOnFocus : true,
						allowBlank : false,
						blankText : '客户性别不能为空.'
					}]
				}, {
					items : [{
						xtype : 'datefield',
						id : 'dateMemberBirthday_dataField_member',
						fieldLabel : '生日',
						format : 'Y-m-d'
					}]
				}, 
				{
					columnWidth : 1,
					items : [{
						xtype : 'combo',
						id : 'comboMemberAge_combo_member',
						fieldLabel : '年龄段',
						readOnly : false,
						forceSelection : true,
						value : -1,
						store : new Ext.data.SimpleStore({
							fields : ['value', 'text'],
							data : [[-1, '无'],[5,'00后'], [4, '90后'], [3, '80后'], [2, '70后'], [1, '60后'], [6, '50后']]
						}),
						valueField : 'value',
						displayField : 'text',
						typeAhead : true,
						mode : 'local',
						triggerAction : 'all',
						selectOnFocus : true,
						allowBlank : true
					},{
						id : 'cm_txtMemberContactAddress',
						fieldLabel : '联系地址',
						width : 535
					}]
				}
				, {
					columnWidth : 1,
					xtype : 'panel',
					html : '<hr style="width: 100%; color: #DDD;">'
				}, {
					items : [{
						xtype : 'numberfield',
						id : 'cm_weixinMemberCard',
						cls : 'disableInput',
						fieldLabel : '微信会员卡',
						disabled : true
					}]
				}, {
					items : [{
						xtype : 'numberfield',
						id : 'cm_numberTotalBalance',
						cls : 'disableInput',
						fieldLabel : '账户余额',
						disabled : true,
						value : 0.00
					}]
				}, {
					items : [{
						xtype : 'numberfield',
						id : 'cm_numberBaseBalance',
						cls : 'disableInput',
						fieldLabel : '基本余额',
						disabled : true,
						value : 0.00
					}]
				}, {
					items : [{
						xtype : 'numberfield',
						id : 'cm_numberExtraBalance',
						cls : 'disableInput',
						fieldLabel : '赠送余额',
						disabled : true,
						value : 0.00
					}]
				}, {
					items : [{
						xtype : 'numberfield',
						id : 'cm_numberUsedBalance',
						cls : 'disableInput',
						fieldLabel : '累计消费',
						disabled : true,
						value : 0.00
					}]
				}, {
					items : [{
						xtype : 'numberfield',
						id : 'cm_numberUserPoint',
						cls : 'disableInput',
						fieldLabel : '累计积分',
						disabled : true,
						value : 0.00
					}]
				}, {
					items : [{
						xtype : 'numberfield',
						id : 'cm_numberMmeberPoint',
						cls : 'disableInput',
						fieldLabel : '当前积分',
						disabled : true,
						value : 0.00
					}]
				},{
					items : [{
						id : 'memberReferrer_numfield_mmm',
						fieldLabel : '推荐人',
						disabled : true,
						cls : 'disableInput'
					}]
				}]
			});		
			
			
			memberBasicWin = new Ext.Window({
				title : '&nbsp;',
				width : 660,
				height : Ext.isIE ? 280 : 235,
				modal : true,
				resizable : false,
				closable : false,
				items : [memberDetailCmp],
				listeners : {
					hide : function(thiz){
						thiz.body.update('');
					},
					show : function(thiz){
						var data = {};
						if(memberBasicWin.otype == Ext.ux.otype['update']){
							data = Ext.ux.getSelData(memberBasicGrid);
							data = !data ? {status:0} : data;
						}else{
							data = {status:0};
						}
						data.memberTypeData = memberTypeData.root;
						cm_operationMemberBasicMsg({
							type : 'SET',
							data : data
						});
						
						if(Ext.ux.getCookie(document.domain+'_chargeSms') == 'true'){
							Ext.getCmp('chbSendFirstCharge_checkbox_member').setValue(true);
						}else{
							Ext.getCmp('chbSendFirstCharge_checkbox_member').setValue(false);
						}	
						
						focusToAddMember();
						
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
				bbar : [{
						xtype : 'checkbox',
						id : 'chbPrintFirstRecharge_checkbox_member',
						checked : true,
						hidden : true,
						boxLabel : '打印充值信息'
					},{
						xtype : 'tbtext',
						text : '&nbsp;&nbsp;'
					},{
						xtype : 'checkbox',
						id : 'chbSendFirstCharge_checkbox_member',
						checked : true,
						boxLabel : '发送充值信息'+(Ext.ux.smsCount >= 20 ? '(<font style="color:green;font-weight:bolder">剩余'+Ext.ux.smsCount+'条</font>)' : '(<font style="color:red;font-weight:bolder">剩余'+Ext.ux.smsCount+'条, 请及时充值</font>)'),
						hidden : true
					},'->', {
						text : '应用',
						id : 'btnAppControlMemberBasicMsg',
						iconCls : 'btn_app',
						handler : function(e){
							if(typeof operateMemberHandler != 'function'){
								Ext.example.msg('提示', '操作失败, 请求异常, 请尝试刷新页面后重试.');
							}else{
								var sendSms = Ext.getCmp('chbSendFirstCharge_checkbox_member').getValue();
								if(sendSms){
									Ext.ux.setCookie(document.domain+'_chargeSms', true, 3650);
								}else{
									Ext.ux.setCookie(document.domain+'_chargeSms', false, 3650);
								}
								var btnClose = Ext.getCmp('btnCloseControlMemberBasicMsg');
								operateMemberHandler({
									type : memberBasicWin.otype,
									data : memberBasicWin.otype == Ext.ux.otype['update'] ? Ext.ux.getSelData(memberBasicGrid) : null,
									isPrint : Ext.getCmp('chbPrintFirstRecharge_checkbox_member').getValue(),
									sendSms : Ext.getCmp('chbSendFirstCharge_checkbox_member').getValue(),										
									setButtonStatus : function(s){
										e.setDisabled(s);
										btnClose.setDisabled(s);
									},
									callback : function(memberData, c, res){
										if(res.success){
											Ext.example.msg(res.title, res.msg);
											cm_operationMemberBasicMsg({
												type : 'SET',
												data : {memberTypeData: memberTypeData.root}
											});	
											focusToAddMember();
										}else{
											Ext.ux.showMsg(res);
											
										}
									}
								});							
							}
						}
				}, {
						text : '保存',
						id : 'btnSaveControlMemberBasicMsg',
						iconCls : 'btn_save',
						handler : function(e){
							if(typeof operateMemberHandler != 'function'){
								Ext.example.msg('提示', '操作失败, 请求异常, 请尝试刷新页面后重试.');
							}else{
								var sendSms = Ext.getCmp('chbSendFirstCharge_checkbox_member').getValue();
								if(sendSms){
									Ext.ux.setCookie(document.domain+'_chargeSms', true, 3650);
								}else{
									Ext.ux.setCookie(document.domain+'_chargeSms', false, 3650);
								}
								var btnClose = Ext.getCmp('btnCloseControlMemberBasicMsg');
								operateMemberHandler({
									type : memberBasicWin.otype,
									data : memberBasicWin.otype == Ext.ux.otype['update'] ? Ext.ux.getSelData(memberBasicGrid) : null,
									isPrint : Ext.getCmp('chbPrintFirstRecharge_checkbox_member').getValue(),
									sendSms : Ext.getCmp('chbSendFirstCharge_checkbox_member').getValue(),										
									setButtonStatus : function(s){
										e.setDisabled(s);
										btnClose.setDisabled(s);
									},
									callback : function(memberData, c, res){
										if(res.success){
											memberBasicWin.hide();
											Ext.example.msg(res.title, res.msg);
											Ext.getCmp('searchMember_btn_member').handler();
//											memberTypeTree.getRootNode().reload();
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
						Ext.getCmp('chbPrintFirstRecharge_checkbox_member').hide();
						Ext.getCmp('chbSendFirstCharge_checkbox_member').hide();
					}
				}]
			});	
		}

	}

	function cm_operationMemberData(c){
		if(c == null || c.type == null || typeof c.type == 'undefined')
			return;
			
		var data = {};
		var memberType = Ext.getCmp('comboMemberType_combo_member');
		var firstCharge = Ext.getCmp('numFirstCharge_numberField_member');	
		var firstActualCharge = Ext.getCmp('numFirstActualCharge_numberField_member');	
		var rechargeType = Ext.getCmp('comboFirstRechargeType_combo_member');
		var memberID = Ext.getCmp('numberMemberId_member');
		var name = Ext.getCmp('txtMemberName_text_member');
		var mobile = Ext.getCmp('txtMemberMobile_text_member');
		var memberCard = Ext.getCmp('numberMemberCard_numberField_member');
		var weixinCard = Ext.getCmp('cm_weixinMemberCard');

		
		var sex = Ext.getCmp('comboMemberSex_combo_member');
		var birthday = Ext.getCmp('dateMemberBirthday_dataField_member');
		var age = Ext.getCmp('comboMemberAge_combo_member');
		var addr = Ext.getCmp('cm_txtMemberContactAddress');
		
		var totalBalance = Ext.getCmp('cm_numberTotalBalance');
		var baseBalance = Ext.getCmp('cm_numberBaseBalance');
		var extraBalance = Ext.getCmp('cm_numberExtraBalance');
		var usedBalance = Ext.getCmp('cm_numberUsedBalance');
		var point = Ext.getCmp('cm_numberMmeberPoint');
		var usedPoint = Ext.getCmp('cm_numberUserPoint');
		var referrer = Ext.getCmp('memberReferrer_numfield_mmm');
		
		
		
		firstCharge.setValue();
		firstActualCharge.setValue();
		
		if(c.type.toUpperCase() == Ext.ux.otype['set'].toUpperCase()){
			
			firstCharge.getEl().up('.x-form-item').setDisplayed(false);
			firstActualCharge.getEl().up('.x-form-item').setDisplayed(false);
			rechargeType.getEl().up('.x-form-item').setDisplayed(false);
			
			memberType.store.loadData(c.data.memberTypeData);
			
			data = c.data == null || typeof c.data == 'undefined' ? {} : c.data;
			memberID.setValue(data['id']);
			name.setValue(data['name']);
			mobile.setValue(data['mobile']);
			referrer.setValue(data['referrer']);
			memberCard.setValue(data['memberCard']);
			weixinCard.setValue(data['weixinCard']);
			sex.setValue(typeof data['sexValue'] == 'undefined' ? 0 : data['sexValue']);
			birthday.setValue(data['birthdayFormat']);
			if(data['ageVal']){
				age.setValue(data['ageVal']);
			}else{
				age.setValue(-1);
			}
			addr.setValue(data['contactAddress']);
			
			totalBalance.setValue(parseFloat(data['totalBalance']).toFixed(2));
			baseBalance.setValue(data['baseBalance']);
			extraBalance.setValue(data['extraBalance']);
			usedBalance.setValue(data['usedBalance']);
			point.setValue(data['point']);
			usedPoint.setValue(data['usedPoint']);
			

			if(typeof data['memberType'] != 'undefined'){
				memberType.setValue(data['memberType']['id']);
			}else{
				if(typeof memberTypeTree != 'undefined'){
					var memberTypeId, attributeValue;
					if(Ext.ux.getSelNode(memberTypeTree) && Ext.ux.getSelNode(memberTypeTree).attributes.memberTypeId != '-1'){
						memberTypeId = Ext.ux.getSelNode(memberTypeTree).attributes.memberTypeId;
						attributeValue = Ext.ux.getSelNode(memberTypeTree).attributes.attributeValue;
						chargeRate = Ext.ux.getSelNode(memberTypeTree).attributes.chargeRate;
					}
					
					memberType.setValue(memberTypeId);
					
					if(memberBasicWin.otype.toLowerCase() == Ext.ux.otype['insert'].toLowerCase() && attributeValue == 0){
						firstCharge.show();
						firstCharge.getEl().up('.x-form-item').setDisplayed(true);	
						firstActualCharge.show();
						firstActualCharge.getEl().up('.x-form-item').setDisplayed(true);
						rechargeType.show();
						rechargeType.getEl().up('.x-form-item').setDisplayed(true);
					
						Ext.getCmp('chbPrintFirstRecharge_checkbox_member').show();
						
						if(Ext.ux.smsModule)
							Ext.getCmp('chbSendFirstCharge_checkbox_member').show();
					
					}else{
						firstCharge.hide();
						firstCharge.getEl().up('.x-form-item').setDisplayed(false);	
						firstActualCharge.hide();
						firstActualCharge.getEl().up('.x-form-item').setDisplayed(false);	
						rechargeType.hide();
						rechargeType.getEl().up('.x-form-item').setDisplayed(false);							
						
						Ext.getCmp('chbPrintFirstRecharge_checkbox_member').hide();
						Ext.getCmp('chbSendFirstCharge_checkbox_member').hide();					
					}				
				}else{
					memberType.setValue();
				}
				
			}
			Ext.getCmp('panelControlMemberContent_panel_member').doLayout();
			
		}else if(c.type.toUpperCase() == Ext.ux.otype['get'].toUpperCase()){
			data = {
				id : memberID.getValue(),
				memberType : {
					typeID : memberType.getValue()
				},
				memberCard : {
					aliasID : cardAliasID.getValue()
				}
			};
			c.data = data;
		}
		name.clearInvalid();
		mobile.clearInvalid();
		memberCard.clearInvalid();
		memberType.clearInvalid();
		sex.clearInvalid();
		
		return c;
	};

	function cm_operationMemberBasicMsg(c){
		if(c == null || c.type == null || typeof c.type == 'undefined')
			return;
		if(c.type.toUpperCase() == Ext.ux.otype['set'].toUpperCase()){
			if(c.data && !c.data.memberTypeData){
				$.ajax({
					url : '../../QueryMemberType.do',
					type : 'post',
					async:false,
					data : {dataSource : 'normal'},
					success : function(jr, status, xhr){
						c.data.memberTypeData = jr.root;
					},
					error : function(request, status, err){
						alert(request.msg);
					}
				}); 			
			}
			
			memberBasicWin.data = c.data;
			cm_operationMemberData({
				type : c.type,
				data : c.data
			});
		}else if(c.type.toUpperCase() == Ext.ux.otype['get'].toUpperCase()){
			c.data = cm_operationMemberData({
				type : c.type
			}).data;
		}
		return c;
	}
	/**
	 * 操作会员信息, 添加或修改
	 * @param c
	 */
	function operateMemberHandler(c){
		if(c == null || c.type == null || typeof c.type == 'undefined'){
			Ext.example.msg('提示', '操作失败, 获取请求类型失败, 请尝试刷新页面后重试.');
			return;
		}
		var membetType = Ext.getCmp('comboMemberType_combo_member');
		var memberName = Ext.getCmp('txtMemberName_text_member');
		var memberMobile = Ext.getCmp('txtMemberMobile_text_member');
		var memberSex = Ext.getCmp('comboMemberSex_combo_member');
		var birthday = Ext.getCmp('dateMemberBirthday_dataField_member');
		var age = Ext.getCmp('comboMemberAge_combo_member');
		var firstCharge = Ext.getCmp('numFirstCharge_numberField_member');
		var firstActualCharge = Ext.getCmp('numFirstActualCharge_numberField_member');
		var rechargeType = Ext.getCmp('comboFirstRechargeType_combo_member');
		
		if(!memberName.isValid() || !membetType.isValid() || !memberSex.isValid()){
			return;
		}
		
		if(typeof c.setButtonStatus == 'function'){
			c.setButtonStatus(true);
		}
		Ext.Ajax.request({
			url : '../../OperateMember.do',                                                                            
			params : {
				dataSource : memberBasicWin.otype.toLowerCase(),
				id : Ext.getCmp('numberMemberId_member').getValue(),
				name : memberName.getValue(),
				mobile : memberMobile.getValue(),
				memberTypeId : membetType.getValue(),
				sex : memberSex.getValue(),
				memberCard :Ext.getCmp('numberMemberCard_numberField_member').getValue(),
				birthday : birthday.getValue() ? birthday.getValue().format('Y-m-d') : '',
				firstCharge : firstCharge.getValue(),
				firstActualCharge : firstActualCharge.getValue(),
				rechargeType : rechargeType.getValue(),
				isPrint : c.isPrint,
				sendSms : c.sendSms,
				addr : Ext.getCmp('cm_txtMemberContactAddress').getValue(),
				age : age.getValue()
			},
			success : function(res, opt){
				c.setButtonStatus(false);
				var jr = Ext.decode(res.responseText);
				if(typeof c.callback == 'function'){
					c.callback({}, c, jr);
				}
			},
			failure : function(res, opt){
				c.setButtonStatus(false);
				if(typeof c.callback == 'function'){
					c.callback(memberData, c, Ext.decode(res.responseText));
				}
			}
		});
		
	}

	function focusToAddMember(){
		Ext.getCmp('txtMemberName_text_member').focus(true, 100);
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
			queryMemberOperationHandler('会员消费明细', '../Client_Module/MemberConsumeDetails.jsp', {
				otype : 1
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
//		imgPath : '../../images/sendCoupon.png',
//		imgWidth : 50,
//		imgHeight : 50,
//		tooltip : '发送优惠劵',
//		handler : function(e){
//			var data = memberBasicGrid.getSelectionModel().getSelections();
//			if(data.length < 1){
//				Ext.example.msg('提示', '请选中会员后进行操作.');
//				return;
//			}		
//			
//			initMemberCouponWin();
//		}
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
		
		loadPricePlanData();
	};

	function loadPricePlanData(){
		$.ajax({
			url : '../../OperatePricePlan.do',
			type : 'post',
			async:false,
			data : {
				dataSource : 'getByCond'
			},
			success : function(jr, status, xhr){
				pricePlanData = jr.root;
			},
			error : function(request, status, err){
				alert(request.msg);
			}
		}); 	
		
	}
	function checkLabel(t){
		if(t.length > 5){
			var after = t.substring(0, 5);
			return after;
		}else{
			return t;
		}
	}

	var defaultMemberTypeDiscount = {
		columnWidth : 1,
		items : [{
			xtype : 'combo',
			id : 'comboDiscount_combo_member',
			fieldLabel : '默认方案' + Ext.ux.txtFormat.xh,
			forceSelection : true,
			allowBlank : false,
			blankText : '折扣方案不能为空.',
			width : 135,
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
		
	};
	var selectRestaurant = new Ext.form.ComboBox({
		columnWidth : 0.3,
		id : 'selectRestaurant_combo_memeberMgrMain',
		xtype : 'combo',
		readOnly : false,
		forceSelection : true,
		store : new Ext.data.JsonStore({
			fields : ['id', 'name']
		}),
		valueField : 'id',
		displayField : 'name',
		typeAhead : true,
		mode : 'local',
		triggerAction : 'all',
		selectOnFocus : true,
		listeners : {
			render : function(thiz){
				Ext.Ajax.request({
					url : '../../OperateRestaurant.do',
					params : {
						dataSource : 'getByCond',
						id : restaurantID
					},
					success : function(res, opt){
						var jr = Ext.decode(res.responseText);
						//FIXME
						console.log(jr);
						var ss = [];
						ss.push({'id':jr.root[0].id, 'name' : jr.root[0].name + '(集团)'});
						for(var i = 0; i < jr.root[0].branches.length; i++){
							ss.push({'id':jr.root[0].branches[i].id, 'name':jr.root[0].branches[i].name});
						}
						selectRestaurant.store.loadData(ss);
						selectRestaurant.setValue(jr.root[0].id);
					},
					failure : function(res, opt){
						selectRestaurant.store.loadData({root:[{typeId:-1, name:'全部'}]});
						selectRestaurant.setValue(-1);
					}
				});
			}
		}
	});
	
	
	
	
	//默认会员价
	var defaultMemberTypePricePlan = {
		columnWidth : 1,
		items : [{
			xtype : 'combo',
			id : 'comboDefaultPricePlan_combo_member',
			fieldLabel : '默认方案' + Ext.ux.txtFormat.xh,
			forceSelection : true,
			allowBlank : false,
			blankText : '价格方案不能为空.',
			width : 135,
			store : new Ext.data.JsonStore({
				fields : [ 'id', 'name' ]
			}),
			valueField : 'id',
			displayField : 'name',
			typeAhead : true,
			mode : 'local',
			triggerAction : 'all',
			selectOnFocus : true,
			listeners : {
				focus  : function(thiz){
					var mPricePlanSelectedList = []; 
					var mPricePlanSelecteds = document.getElementsByName('memberPricePlan');
					for (var i = 0; i < mPricePlanSelecteds.length; i++) {
						if(mPricePlanSelecteds[i].checked){
							mPricePlanSelectedList.push({'id':mPricePlanSelecteds[i].value,'name':mPricePlanSelecteds[i].nextSibling.innerHTML});
						}
						
					}
					thiz.store.loadData(mPricePlanSelectedList);
				}
			}
		}]
		
	};

	//会员折扣和会员价设置
	function memberDiscountWinInit(){
		if(!memberDisountWin){
			memberDisountWin = new Ext.Window({
				title : '设置门店折扣',
				closable : false,
				resizable : false,
				width : 275,
				items : [{
					xtype : 'panel',
					layout : 'column',
					frame : true,
					width : 255,
					defaults : {
						columnWidth : .5,
						layout : 'form',
						labelWidth : 80
					},
					items : [{
						columnWidth : 0.4,
						xtype : 'label',
						text : '选择餐厅类型:'
					}, selectRestaurant]
				},{
					xtype : 'panel',
					layout : 'column',
					id : 'formMemberDiscount_panel_member',
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
						style : 'text-align:left;padding-bottom:3px;',
						text : '选择折扣方案:'
						
					}]
					
				},{
					xtype : 'panel',
					layout : 'column',
					id : 'formMemberPricePlan_panel_member',
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
						style : 'text-align:left;padding-bottom:3px;',
						text : '选择价格方案:'
					}]
					
				}, {
					xtype : 'label',
					html : '&nbsp;'
				}],
				bbar : ['->', {
					text : '保存',
					iconCls : 'btn_save',
					handler : function(e){
					}
				}, {
					text : '关闭',
					iconCls : 'btn_close',
					handler : function(e){
						memberDisountWin.close();
					}
				}],
				listeners : {
					beforeshow : function(e){
						//获得价格方案时, 重新渲染
						
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
											Ext.getCmp('comboDiscount_combo_member').setValue();
											Ext.getCmp('comboDiscount_combo_member').clearInvalid();
										},
										check : function(){
											Ext.getCmp('comboDiscount_combo_member').setValue();
											Ext.getCmp('comboDiscount_combo_member').clearInvalid();
										}
									}
								}]};
								Ext.getCmp('formMemberDiscount_panel_member').add(c);
								//solveIE自动换行时格式错乱
								if((i+1)%6 == 0){
									Ext.getCmp('formMemberDiscount_panel_member').add({columnWidth : 1});
								}
								Ext.getCmp('formMemberDiscount_panel_member').doLayout();
							}
							Ext.getCmp('formMemberDiscount_panel_member').add(defaultMemberTypeDiscount);
						}
						
						if(pricePlanData.length > 0 && document.getElementsByName('memberPricePlan').length == 0){
							for (var i = 0; i < pricePlanData.length; i++) {
								var c = {items : [{
									xtype : "checkbox", 
									name : "memberPricePlan",
									boxLabel : checkLabel(pricePlanData[i].name) , 
									hideLabel : true, 
									inputValue :  pricePlanData[i].id,
									listeners : {
										focus : function(){
											Ext.getCmp('comboDefaultPricePlan_combo_member').setValue();
											Ext.getCmp('comboDefaultPricePlan_combo_member').clearInvalid();
										},
										check : function(){
											Ext.getCmp('comboDefaultPricePlan_combo_member').setValue();
											Ext.getCmp('comboDefaultPricePlan_combo_member').clearInvalid();
										}
									}
								}]};
								Ext.getCmp('formMemberPricePlan_panel_member').add(c);
								//solveIE自动换行时格式错乱
								if((i+1)%6 == 0){
									Ext.getCmp('formMemberPricePlan_panel_member').add({columnWidth : 1});
								}
							}
							Ext.getCmp('formMemberPricePlan_panel_member').add(defaultMemberTypePricePlan);		
//							Ext.getCmp('formMemberPricePlan_panel_member').syncSize();
							Ext.getCmp('formMemberPricePlan_panel_member').doLayout();
						}else{
							Ext.getCmp('formMemberPricePlan_panel_member').hide();
						}						
						
					},
					hide : function(){
						var discounts = document.getElementsByName('memberDiscount');
						for (var i = 0; i < discounts.length; i++) {
							if(discounts[i].checked){
								discounts[i].checked = false;
							}
						}
						
						var pricePlans = document.getElementsByName('memberPricePlan');
						for (var i = 0; i < pricePlans.length; i++) {
							if(pricePlans[i].checked){
								pricePlans[i].checked = false;
							}
						}
						Ext.getCmp('formMemberPricePlan_panel_member').removeAll();
					}
				}
			})
		}
	}
	
	//会员类型设置
	function m_memberTypeWinInit(){
		if(!m_memberTypeWin){
			m_memberTypeWin = new Ext.Window({
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
						id : 'numTypeID_member',
						fieldLabel : '类型编号'
					}, {
						xtype : 'textfield',
						id : 'txtTypeName_textField_member',
						fieldLabel : '类型名称' + Ext.ux.txtFormat.xh,
						allowBlank : false,
						blankText : '类型名称不能为空.',
						value : "",
						selectOnFocus : true
					},{
						xtype : 'panel',
						layout : 'column',
						id : 'radioMemberTypes',
						frame : false,
						width : 255,
						defaults : {
							columnWidth : .5,
							layout : 'form',
							labelWidth : 80
						},
						items : [{
							columnWidth : .3,
					    	items : [{
								xtype : 'label',
								html :  '&nbsp;&nbsp;会员属性' + Ext.ux.txtFormat.xh
					    	}]
					    },{
							columnWidth : .25,
					    	labelWidth : 65,
					    	items : [{
								xtype : "radio",
								id : 'rdoMemberType4Charge_radio_member',
								name : "radioMemberType",
								boxLabel : '充值' , 
								hideLabel : true,
								checked : true,
								inputValue :  0,
								listeners : {
									render : function(e){
//										Ext.ux.checkPaddingTop(e);
										Ext.getDom('rdoMemberType4Charge_radio_member').onclick = function(){
											e.setValue(true);
											Ext.getCmp('numChargeRate_numberField_member').setValue(1.0);
											Ext.getCmp('numChargeRate_numberField_member').setDisabled(false);											
											m_memberTypeWin.s_memberType = e.inputValue;
										};
									}
								}
								
					    	}]
					    },{
							columnWidth : .25,
					    	labelWidth : 65,
					    	items : [{
								xtype : "radio", 
								id : 'rdoMemberType4Piont_radio_member',
								name : "radioMemberType",
								boxLabel : '积分' , 
								hideLabel : true, 
								inputValue :  1,
								listeners : {
									render : function(e){
//										Ext.ux.checkPaddingTop(e);
										Ext.getDom('rdoMemberType4Piont_radio_member').onclick = function(){
											e.setValue(true);
											Ext.getCmp('numChargeRate_numberField_member').setValue(0);
											Ext.getCmp('numChargeRate_numberField_member').setDisabled(true);										
											m_memberTypeWin.s_memberType = e.inputValue;
										};
									}
								}
								
					    	}]
					    }]
						
					}, {
						xtype : 'numberfield',
						id : 'numExchangeRate_numberField_member',
						fieldLabel : '积分比率' + Ext.ux.txtFormat.xh,
						value : 1.0,
						minValue : 0.00,
						allowBlank : false,
						blankText : '积分比率不能为空.',
						selectOnFocus : true
					}, {
						xtype : 'label',
						width : 200,
						style : 'color:green;fontSize:14px;',
						text : '输入 1.5, 表示消费 100 元兑换 150 积分'
					}, {
						xtype : 'numberfield',
						id : 'numInitialPoint_numberField_member',
						fieldLabel : '初始积分' + Ext.ux.txtFormat.xh,
						value : 1,
						minValue : 0.00,
						allowBlank : false,
						blankText : '初始积分不能为空.',
						selectOnFocus : true
					}, {
						xtype : 'label',
						style : 'color:green;fontSize:15px;',
						text : '说明:  新会员赠送积分.'
					}, {
						xtype : 'numberfield',
						id : 'numChargeRate_numberField_member',
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
						style : 'color:green;fontSize:15px;',
						text : '输入 1.5, 表示 100 元送 50 元'
					},{
						xtype : 'textarea',
						height : 40,
						id : 'txtCommentForMemberType_area_member',
						fieldLabel : '特权说明'
					}]
				}],
				bbar : ['->', {
					text : '保存',
					id : 'btnSaveMemberType_btn_member',
					iconCls : 'btn_save',
					handler : function(e){
						var typeID = Ext.getCmp('numTypeID_member');
						var typeName = Ext.getCmp('txtTypeName_textField_member');
						var chargeRate = Ext.getCmp('numChargeRate_numberField_member');
						var exchangeRate = Ext.getCmp('numExchangeRate_numberField_member');
						var initialPoint = Ext.getCmp('numInitialPoint_numberField_member');
						var discount = Ext.getCmp('comboDiscount_combo_member');
						var pricePlan = Ext.getCmp('comboDefaultPricePlan_combo_member');
						var attribute = m_memberTypeWin.s_memberType;
						var desc = Ext.getCmp('txtCommentForMemberType_area_member');
						
						if(!typeName.isValid() || !chargeRate.isValid() || !exchangeRate.isValid() 
								|| !initialPoint.isValid() || !discount.isValid()){
							return;
						}
						var memberDiscountCheckeds = "", memberPricePlanCheckeds = "";
						memberPricePlanCheckeds = getChecked(memberPricePlanCheckeds, document.getElementsByName('memberPricePlan'));
						
						if(memberPricePlanCheckeds){
							if(!pricePlan.isValid()){
								return;
							}
						}else{
							if(pricePlan){
								pricePlan.clearInvalid();
							}
							
						}
						
						var save = Ext.getCmp('btnSaveMemberType_btn_member');
						var close = Ext.getCmp('btnCloseMemberType_btn_member');
						
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
								pricePlanId : pricePlan?pricePlan.getValue() : '',
								exchangeRate : exchangeRate.getValue(),
								initialPoint : initialPoint.getValue(),
								chargeRate : chargeRate.getValue(),
								attr : attribute,
								desc : desc.getValue(),
								memberDiscountCheckeds : getChecked(memberDiscountCheckeds, document.getElementsByName('memberDiscount')),
								memberPricePlanCheckeds : memberPricePlanCheckeds
							},
							success : function(res, opt){
								var jr = Ext.decode(res.responseText);
								if(jr.success){
									Ext.example.msg(jr.title, jr.msg);
									m_memberTypeWin.hide();
									Ext.getCmp('btnRefreshMemberType_btn_member').handler();
									member_loadMemberTypeChart();
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
					id : 'btnCloseMemberType_btn_member',
					iconCls : 'btn_close',
					handler : function(e){
						m_memberTypeWin.hide();
						Ext.getCmp('txtTypeName_textField_member').enable();
					}
				}],
				keys : [{
					key : Ext.EventObject.ENTER,
					fn : function(){
						Ext.getCmp('btnSaveMemberType_btn_member').handler();
					},
					scope : this
				}, {
					key : Ext.EventObject.ESC,
					fn : function(){
						Ext.getCmp('btnCloseMemberType_btn_member').handler();
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
	
	function setMemberDiscount(){
		memberDiscountWinInit();
		memberDisountWin.show();
		memberDisountWin.center();
	}
	
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
		var typeID = Ext.getCmp('numTypeID_member');
		var typeName = Ext.getCmp('txtTypeName_textField_member');
		var chargeRate = Ext.getCmp('numChargeRate_numberField_member');
		var exchangeRate = Ext.getCmp('numExchangeRate_numberField_member');
		var initialPoint = Ext.getCmp('numInitialPoint_numberField_member');
		var discount = Ext.getCmp('comboDiscount_combo_member');
		var pricePlan = Ext.getCmp('comboDefaultPricePlan_combo_member');
		var desc = Ext.getCmp('txtCommentForMemberType_area_member');
		
		typeID.setValue(d['memberTypeId']);
		typeName.setValue(d['memberTypeName']);
		//是微信就不能该名字
		if(d['type'] == 2){
			typeName.disable();
		}
		exchangeRate.setValue(d['exchangeRate']);
		initialPoint.setValue(typeof d['initialPoint'] != 'undefined' ? d['initialPoint'] : 0);
		
		if(typeof d['attributeValue'] == 'undefined'){
			Ext.getDom('rdoMemberType4Charge_radio_member').onclick();
		}else{
			if(d['attributeValue'] == 0){
				Ext.getDom('rdoMemberType4Charge_radio_member').onclick();
			}else if(d['attributeValue'] == 1){
				Ext.getDom('rdoMemberType4Piont_radio_member').onclick();
			}
		}
		chargeRate.setValue(d['chargeRate']);
		discount.setValue(d['discount']);
		if(pricePlan){
			pricePlan.setValue(d['pricePlan'] > 0?d['pricePlan'] : '');
		}
		
		desc.setValue(d['desc']);
		
		typeID.clearInvalid();
		typeName.clearInvalid();
		chargeRate.clearInvalid();
		initialPoint.clearInvalid();
		discount.clearInvalid();
		if(pricePlan){
			pricePlan.clearInvalid();
		}
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

	//会员类型操作
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
			Ext.getCmp('txtTypeName_textField_member').focus(true, 100);
			
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
			loadPricePlanData();
			m_memberTypeWin.setTitle('修改会员类型');
			m_memberTypeWin.show();
			var discounts = document.getElementsByName('memberDiscount');
			for (var i = 0; i < sd.attributes['discounts'].length; i++) {
				for (var j = 0; j < discounts.length; j++) {
					if(sd.attributes['discounts'][i].discountID == discounts[j].value){
						
						discounts[j].checked = true;
					}
				}
			}
			Ext.getCmp('comboDiscount_combo_member').store.loadData(sd.attributes['discounts']);
			
			
			var pricePlans = document.getElementsByName('memberPricePlan');
			for (var i = 0; i < sd.attributes['pricePlans'].length; i++) {
				for (var j = 0; j < pricePlans.length; j++) {
					if(sd.attributes['pricePlans'][i].id == pricePlans[j].value){
						pricePlans[j].checked = true;
					}
				}
			}	
			
			if(Ext.getCmp('comboDefaultPricePlan_combo_member')){
				Ext.getCmp('comboDefaultPricePlan_combo_member').store.loadData(sd.attributes['pricePlans']);
			}
			
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
									Ext.getCmp('btnRefreshMemberType_btn_member').handler();
									member_loadMemberTypeChart();
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
			title : '添加等级',
			closable : false,
			modal : true,
			resizable : false,
			width : 250,
			bbar : [{
				text : '删除',
				id : 'btn_memberLevelDel',
				hidden : true,
				iconCls : 'btn_delete',
				handler : function(){
					deleteMemberLevel();
				}
			},'->',{
				text : '保存',
				id : 'btn_memberLevelAdd',
				iconCls : 'btn_save',
				handler : function(){
					var pointThreshold = Ext.getCmp('txtPointThreshold_numberfield_member');
					var combo_memberTypeId = Ext.getCmp('combo_memberLevel_mType');
					
					if(!pointThreshold.isValid() || !combo_memberTypeId.isValid()){
						return;
					}
					Ext.Ajax.request({
						url : '../../OperateMemberLevel.do',
						params : {
							id : Ext.getCmp('txtMemberLevelId_text_member').getValue(),
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
						if(!memberLevelAddWin.first || memberLevels.length == 1){
							Ext.getCmp('btn_memberLevelDel').show();
						}
					}else if(memberLevelAddWin.otype == 'insert'){
						data = {};
						Ext.getCmp('btn_memberLevelDel').hide();
					}
							
					operateMemberLevelData({
						type : 'SET',
						data : data
					});
				},
				hide : function(){
					memberLevelDetail="";
					Ext.getCmp('btn_memberLevelDel').hide();
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
					fieldLabel : '等级积分',
					id : 'txtPointThreshold_numberfield_member',
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
					id : 'txtMemberLevelId_text_member'
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
				
				thiz.setValue(combo_memberTypeData[0][0]);
			},
			fialure : function(res, opt){
				thiz.store.loadData(combo_memberTypeData);
			}
		});
	}

	function checkSetabled(v){
		for (var i = 0; i < yAxisData.length; i++) {
			if(yAxisData[i].x == v){
				return {status:yAxisData[i].status, first:yAxisData[i].first};
			}
		}
	}

	function member_operateMemberLevel(y, x){
		var p = checkSetabled(x);
		if(p.status == 3){
			Ext.example.msg('提示', '请按顺序添加等级.');
			return ;	
		};
		initMemberTypeData();
		var list = memberLevels;
		var otype = 'insert';
		for (var i = 0; i < list.length; i++) {
			if(list[i].pointThreshold == x){
				otype = 'update';
				memberLevelDetail = list[i];
				memberLevelDetail['xAxisText'] = list[i].memberTypeName;
				combo_memberTypeData.push([memberLevelDetail['memberTypeId'], memberLevelDetail['memberTypeName']]);
				Ext.getCmp('combo_memberLevel_mType').store.loadData(combo_memberTypeData);
			}
		}
		memberLevelAddWin.first = p.first;
		operateMemberLevel({otype:otype});
	}

	function operateMemberLevel(c){
		if(c == null || typeof c == 'undefined' || typeof c.otype == 'undefined'){
			return;
		}
		memberLevelAddWin.otype = c.otype;
		if(c.otype == 'insert'){
			memberLevelAddWin.setTitle('添加等级');
//			initMemberTypeData();
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
		var pointThreshold = Ext.getCmp('txtPointThreshold_numberfield_member');
		var combo_memberTypeId = Ext.getCmp('combo_memberLevel_mType');
		var memberLevelId = Ext.getCmp('txtMemberLevelId_text_member');
		if(c.type.toUpperCase() == Ext.ux.otype['set'].toUpperCase()){
			data = c.data;
			pointThreshold.setValue(data['pointThreshold']);
			
			if(data['memberTypeId']){
				combo_memberTypeId.setValue(data['memberTypeId']);
			}else{
				combo_memberTypeId.setValue();
			}
			memberLevelId.setValue(data['id']);

			if(memberLevelAddWin.first){
				pointThreshold.setValue(0);
				pointThreshold.setDisabled(true);
			}else{
				pointThreshold.setDisabled(false);
				pointThreshold.focus(true, 100);
			}
			
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
	    var val1 = obj1.x;
	    var val2 = obj2.x;
	    if (val1 < val2) {
	        return -1;
	    } else if (val1 > val2) {
	        return 1;
	    } else {
	        return 0;
	    }            
	};

	function getLevelChartInfo(x){
		for (var i = 0; i < yAxisData.length; i++) {
			if(yAxisData[i].x == x){
				if(yAxisData[i].status == 1){
					return '<span style="font-size : 13px;">' + yAxisData[i].level + (yAxisData[i].x >0 ? '-' + yAxisData[i].x +'分' :'')+ '</span>';
				}else if(yAxisData[i].status == 2){
					return '<span style="font-size : 13px;color:Maroon">点击设定-' + yAxisData[i].level + '</span>';
				}else if(yAxisData[i].status == 3){
					return '<span style="font-size : 13px;color:gray">' + yAxisData[i].level + '-未设定</span>';
				}
			}
		}		
	}
	//三角, 正方, 菱形
	var icons = ['triangle', 'square', 'diamond'];
	var yAxisData = [];

	function member_loadMemberTypeChart(){
		$.ajaxSetup({async : false});
		var chartMinX;
		$.post('../../QueryMemberLevel.do', {dataSource : 'chart', webMemberLevelChart : true}, function(res){
			memberLevels = res.root;
			chartDatas = eval('(' + res.other.chart + ')');

			yAxisData = chartDatas.data;
			
			//重新根据积分排序
//			yAxisData = chartDatas.data.sort(member_compare);
			
			chartMinX = -yAxisData[yAxisData.length-1].x * 0.15; 
		 	var chart = {
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
				            enabled: false,
				            text: '积分',
				            align : 'high'
				        },
				        labels: {
				            formatter: function() {
				                return this.value;
				            }
				        },
				        min: chartMinX,
				        showLastLabel: true
				    },
				    yAxis: {
				        title: {
				            text: '等级'
				        },
				        labels: {
				            formatter: function() {
				                return '' ;
				            }
				        },
				        lineWidth: 2
				    },
				    legend: {
				        enabled: false
				    },
				    tooltip: {
				    	enabled : false,
				        headerFormat: '<b>{series.name}</b><br/>',
				        pointFormat: '{point.x} km: {point.y}°C'
				    },
					plotOptions : {
						spline : {
							cursor : 'pointer',
							dataLabels : {
								align : 'left',
								enabled : true,
								style : {
									fontWeight: 'bold', 
									color: 'green'
								},
								formatter : function(){
					                return getLevelChartInfo(this.x);
								}
							},
							marker: {
								radius: 8,
			                    lineColor: '#666666',
			                    lineWidth: 1
				            },
							events : {
								click : function(e){
									member_operateMemberLevel(e.point.y, e.point.category);
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
				        data: yAxisData
//				        data : [{y:5, level : '微信会员', x:0, marker: {symbol:'diamond'}, status : 1}, {y : 5, level : '等级2',x:0.01.3, marker:{symbol:'triangle'}, status : 2, color : 'red'}, {y : 5, level : '等级3',x:1.94999992847442631.3, marker:{symbol:'square'}, status : 3, color : 'Gray'}]
				    }]			    
				};
				memberTypeLevelChart = new Highcharts.Chart(chart);
		});
	}
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
					id : 'memberTypeLevelChartsPanel',
					title : '会员等级路线图',
					region : 'south',
					contentEl : 'divMemberTypeLevelCharts'
				})
		]
	});
	
	new Ext.Panel({
		renderTo : 'divMember',
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
	 
//	memberBasicWin.render(document.body);
	Ext.ux.checkSmStat();
	
	memberTypeLevelChart.setSize(240, memberBasicGrid.getHeight() * 0.65-80);
	Ext.getCmp('memberTypeLevelChartsPanel').setHeight(memberBasicGrid.getHeight() * 0.65);
	Ext.getCmp('memberTypeLevelChartsPanel').getEl().setTop(memberBasicGrid.getHeight() * 0.35);
	memberTypeTree.setHeight(memberBasicGrid.getHeight() * 0.35);
	
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
	showFloatOption({treeId : 'tree_memberTypeMgr', option : [{name:'折扣设置', fn : setMemberDiscount}, {name:'修改', fn : updateMemberTypeHandler},{name:'删除', fn : deleteMemberTypeHandler}]});
	
	initAddLevelWin();
	
	//获取member数据, 在render事件后触发修改数据时不能更新UI
	Ext.getCmp('searchMember_btn_member').handler();
	
	function gridInit(){
		var beginBirthday = new Ext.form.DateField({
			id : 'beginBirthday_df_memebrMgrMain',
			xtype : 'datefield',
			format : 'Y-m-d',
			width : 100,
			maxValue : new Date(),
			readOnly : false
		});
		
		var endBirthday = new Ext.form.DateField({
			xtype : 'datafield',
			id : 'endBirthday_df_memebrMgrMain',
			format : 'Y-m-d',
			width : 100,
			maxValue : new Date(),
			readOnly : false,
			allowBlank : false
		});
		
		var birthdayDateCombo = Ext.ux.createDateCombo({
			width : 75,
			data : [[3, '近一个月'], [4, '近三个月'], [9, '近半年']],
			beginDate : beginBirthday,
			endDate : endBirthday,
			callback : function(){
				if(member_searchType){
					Ext.getCmp('searchMember_btn_member').handler();
				}
			}
		});
		
		
		var member_beginDate = new Ext.form.DateField({
			xtype : 'datefield',	
			id : 'dateSearchDateBegin_data_member',
			format : 'Y-m-d',
			width : 100,
			maxValue : new Date(),
			readOnly : false
//			allowBlank : false
		});
		var member_endDate = new Ext.form.DateField({
			xtype : 'datefield',
			id : 'dateSearchDateEnd',
			format : 'Y-m-d',
			width : 100,
			maxValue : new Date(),
			readOnly : false
//			allowBlank : false
		});
		var member_dateCombo = Ext.ux.createDateCombo({
			width : 75,
			data : [[3, '近一个月'], [4, '近三个月'], [9, '近半年']],
			beginDate : member_beginDate,
			endDate : member_endDate,
			callback : function(){
				if(member_searchType){
					Ext.getCmp('searchMember_btn_member').handler();
				}
			}
		});
		
		var referrerCombo = new Ext.form.ComboBox({
			id : 'referrerCombo',
			readOnly : false,
			forceSelection : true,
			width : 103,
			listWidth : 120,
			store : new Ext.data.SimpleStore({
				fields : ['staffID', 'staffName']
			}),
			valueField : 'staffID',
			displayField : 'staffName',
			typeAhead : true,
			mode : 'local',
			triggerAction : 'all',
			selectOnFocus : true,
			listeners : {
				render : function(thiz){
					var data = [[-1, '全部']];
					Ext.Ajax.request({
						url : '../../QueryStaff.do',
						success : function(res, opt){
							var jr = Ext.decode(res.responseText);
							for(var i = 0; i < jr.root.length; i++){
								data.push([jr.root[i]['staffID'], jr.root[i]['staffName']]);
							}
							thiz.store.loadData(data);
							thiz.setValue(-1);
							
						},
						fialure : function(res, opt){
							thiz.store.loadData(data);
							thiz.setValue(-1);
						}
					});
				},
				select : function(){
					Ext.getCmp('searchMember_btn_member').handler();
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
					text : ' 至 '
				}, 
				member_endDate,
				{xtype : 'tbtext', text : '&nbsp;&nbsp;'},			
				{xtype : 'tbtext', text : '消费金额:'},
				{
					xtype : 'numberfield',
					id : 'textTotalMinMemberCost_numberField_member',
					width : 60
				},
				{
					xtype : 'tbtext',
					text : '&nbsp;-&nbsp;'
				},			
				{
					xtype : 'numberfield',
					id : 'textTotalMaxMemberCost_numberField_member',
					width : 60
				},
				{xtype : 'tbtext', text : '&nbsp;&nbsp;'},	
				{xtype : 'tbtext', text : '消费次数:'},
				{
					xtype : 'numberfield',
					id : 'textTotalMinMemberCostCount_numberField_member',
					width : 50
				},
				{
					xtype : 'tbtext',
					text : '&nbsp;-&nbsp;'
				},			
				{
					xtype : 'numberfield',
					id : 'textTotalMaxMemberCostCount_numberField_member',
					width : 50
				},
				{xtype : 'tbtext', text : '&nbsp;&nbsp;'},	
				{xtype : 'tbtext', text : '余额:'},
				{
					xtype : 'numberfield',
					id : 'textMemberMinBalance_numberField_member',
					width : 50
				},
				{
					xtype : 'tbtext',
					text : '&nbsp;-&nbsp;'
				},				
				{
					xtype : 'numberfield',
					id : 'textMemberMaxBalance_numberField_member',
					width : 60
				},{
					xtype : 'tbtext',
					text : '推荐人:'
				}, referrerCombo]
			
		});
		
		var memberBasicGridSortTbar = new Ext.Toolbar({
			hidden : true,
			height : 28,		
			items : [
				{xtype : 'tbtext', text : '排序:&nbsp;&nbsp;'},
				{xtype : 'tbtext', text : '&nbsp;&nbsp;'},
				{
					xtype : 'radio',
					id : 'searchOrderBy_radio_member',
					checked : true,
					boxLabel : '创建时间',
					name : 'memberSort',
					inputValue : 'create',
					listeners : {
						check : function(e){
							if(e.getValue()){
								m_searchAdditionFilter = e.inputValue;
							}
						}
					}
				}, { xtype:'tbtext', text:'&nbsp;&nbsp;'}, {
					xtype : 'radio',
					name : 'memberSort',
					boxLabel : '消费额',
					inputValue : 'consumeMoney',
					listeners : {
						check : function(e){
							if(e.getValue()){
								m_searchAdditionFilter = e.inputValue;
							}
								
						}
					}
				}, { xtype:'tbtext', text:'&nbsp;&nbsp;'}, {
					xtype : 'radio',
					name : 'memberSort',
					boxLabel : '消费次数',
					inputValue : 'consumeAmount',
					listeners : {
						check : function(e){
							if(e.getValue())
								m_searchAdditionFilter = e.inputValue;
						}
					}
				}, { xtype:'tbtext', text:'&nbsp;&nbsp;'}, {
					xtype : 'radio',
					name : 'memberSort',
					boxLabel : '积分',
					inputValue : 'point',
					listeners : {
						check : function(e){
							if(e.getValue()){
								m_searchAdditionFilter = e.inputValue;
							}
						}
					}
				},{
					xtype : 'tbtext',
					text : '&nbsp;&nbsp;&nbsp;&nbsp;'
				},{
					xtype: 'tbtext',
					text : '会员生日:'
				},birthdayDateCombo,
				beginBirthday,{
					xtype : 'tbtext',
					text : '至'
				},endBirthday,{
					xtype : 'tbtext',
					text : '&nbsp;&nbsp;&nbsp;&nbsp;'
				}]
			
		});	
		
		var memberBasicGridTbar = new Ext.Toolbar({
			items : [{
				xtype : 'tbtext',
				text : String.format(Ext.ux.txtFormat.longerTypeName, '会员类型', 'memberTypeShowType', '----')
			}, {
				xtype : 'tbtext',
				text : '&nbsp;&nbsp;&nbsp;&nbsp;'
			},
			{
				xtype : 'tbtext',
				text : '会员手机/会员卡号/会员名:'
			}, 			
			{
				xtype : 'textfield',
				id : 'numberSearchByMemberPhoneOrCardOrName'
			}, '->', {
				text : '高级条件↓',
		    	id : 'member_btnHeightSearch',
		    	handler : function(){
		    		member_searchType = true;
					Ext.getCmp('member_btnCommonSearch').show();
		    		Ext.getCmp('member_btnHeightSearch').hide();
		    		
		    		memberBasicGrid.setHeight(memberBasicGrid.getHeight()-56);
		    		
		    		memberBasicGridExcavateMemberTbar.show();
		    		memberBasicGridSortTbar.show();
		    		
		    		
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
		    		memberBasicGridSortTbar.hide();
		    		
		    		memberBasicGrid.setHeight(memberBasicGrid.getHeight()+56);
		    		memberBasicGrid.syncSize(); //强制计算高度
		    		memberBasicGrid.doLayout();//重新布局 	
		    		
		    		//日期清空
		    		Ext.getCmp('dateSearchDateBegin_data_member').setValue('');
		    		Ext.getCmp('dateSearchDateEnd').setValue('');	    		
		    		
		    		//消费金额清空
		    		Ext.getCmp('textTotalMinMemberCost_numberField_member').setValue('');
		    		Ext.getCmp('textTotalMaxMemberCost_numberField_member').setValue('');
		    		
		    		//消费次数清空
		    		Ext.getCmp('textTotalMinMemberCostCount_numberField_member').setValue('');
		    		Ext.getCmp('textTotalMaxMemberCostCount_numberField_member').setValue('');
		    		
		    		//清空余额
		    		Ext.getCmp('textMemberMinBalance_numberField_member').setValue('=');
		    		Ext.getCmp('textMemberMaxBalance_numberField_member').setValue('');
		    		
		    		//设置按创建时间排序
		    		Ext.getCmp('searchOrderBy_radio_member').setValue(true);
		    		
		    		member_dateCombo.setValue(4);
		    		Ext.getCmp('searchMember_btn_member').handler();
				}
			},{xtype : 'tbtext', text : '&nbsp;&nbsp;'},	 
			{
				text : '搜索',
				id : 'searchMember_btn_member',
				iconCls : 'btn_search',
				handler : function(){
					
					var memberTypeNode = memberTypeTree.getSelectionModel().getSelectedNode();
					
					var gs = memberBasicGrid.getStore();
					
//					var tipWinShow = false;
					
					if(memberTypeNode){
						gs.baseParams['memberType'] = memberTypeNode.attributes.memberTypeId;
					}else{
						gs.baseParams['memberType'] = '';
					}
					
					gs.baseParams['memberCardOrMobileOrName'] = Ext.getCmp('numberSearchByMemberPhoneOrCardOrName').getValue();
					gs.baseParams['MinTotalMemberCost'] = Ext.getCmp('textTotalMinMemberCost_numberField_member').getValue();
					gs.baseParams['MaxTotalMemberCost'] = Ext.getCmp('textTotalMaxMemberCost_numberField_member').getValue();
					gs.baseParams['consumptionMinAmount'] = Ext.getCmp('textTotalMinMemberCostCount_numberField_member').getValue();
					gs.baseParams['consumptionMaxAmount'] = Ext.getCmp('textTotalMaxMemberCostCount_numberField_member').getValue();
					gs.baseParams['memberMinBalance'] = Ext.getCmp('textMemberMinBalance_numberField_member').getValue();
					gs.baseParams['memberMaxBalance'] = Ext.getCmp('textMemberMaxBalance_numberField_member').getValue();
					gs.baseParams['beginDate'] = Ext.getCmp('dateSearchDateBegin_data_member').getValue();
					gs.baseParams['endDate'] = Ext.getCmp('dateSearchDateEnd').getValue();
					gs.baseParams['needSum'] = true;
					gs.baseParams['orderBy'] = m_searchAdditionFilter;
					gs.baseParams['referrer'] = referrerCombo.getValue() != -1 ? referrerCombo.getValue() : null;
					gs.baseParams['beginBirthday'] = Ext.getCmp('beginBirthday_df_memebrMgrMain').getValue();
					gs.baseParams['endBirthday'] = Ext.getCmp('endBirthday_df_memebrMgrMain').getValue();
					gs.load({
						params : {
							start : 0,
							limit : 50
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
						if(store.getCount() > 0){
//							var memberSum = memberBasicGrid.getView().getRow(store.getCount() - 1);
//							memberSum.style.backgroundColor = '#EEEEEE';
							var memberSumView = memberBasicGrid.getView();
							
							for (var i = 0; i < memberBasicGrid.getColumnModel().getColumnCount(); i++) {
								var sumCell = memberSumView.getCell(store.getCount()-1, i);
								sumCell.style.fontSize = '15px';
								sumCell.style.fontWeight = 'bold';
								sumCell.style.color = 'green';
							}
							memberSumView.getCell(store.getCount()-1, 1).innerHTML = '汇总';
							memberSumView.getCell(store.getCount()-1, 2).innerHTML = '--';
							memberSumView.getCell(store.getCount()-1, 3).innerHTML = '--';
							memberSumView.getCell(store.getCount()-1, 4).innerHTML = '--';
							memberSumView.getCell(store.getCount()-1, 5).innerHTML = '--';
							memberSumView.getCell(store.getCount()-1, 6).innerHTML = '--';
							memberSumView.getCell(store.getCount()-1, 7).innerHTML = '--';
							memberSumView.getCell(store.getCount()-1, 8).innerHTML = '--';
							
//							memberSumView.getCell(store.getCount()-1, 9).innerHTML = '--';
							memberSumView.getCell(store.getCount()-1, 10).innerHTML = '--';
							memberSumView.getCell(store.getCount()-1, 11).innerHTML = '--';
							memberSumView.getCell(store.getCount()-1, 12).innerHTML = '--';
						}
						
						
						//绑定充值
						$('#divMember').find('.addMoney_a_member').each(function(index, element){
							element.onclick = function(){
								rechargeHandler();
							};
						});
						
						//绑定取款
						$('#divMember').find('.getMoney_a_member').each(function(index, element){
							element.onclick = function(){
								takeMoneyHandler();
							};
						});
						
						//绑定修改
						$('#divMember').find('.update_a_member').each(function(index, element){
							element.onclick = function(){
								updateMemberHandler();
							};
						});
						
						//绑定删除
						$('#divMember').find('.delete_a_member').each(function(index, element){
							element.onclick = function(){
								deleteMemberHandler();
							};
						});
						
						//绑定操作明细
						$('#divMember').find('.detail_a_member').each(function(index, element){
							element.onclick = function(){
								memberOperationSend();
							};
						});
						
						
					});
					
				}
			}, {
				hidden : true,
				text : '重置',
				iconCls : 'btn_refresh',
				handler : function(e){
					Ext.getCmp('btnRefreshMemberType_btn_member').handler();
					Ext.getCmp('searchMember_btn_member').handler();
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
//					var tipWinShow = false;
					var memberType = '';
					if(memberTypeNode){
						memberType = memberTypeNode.attributes.memberTypeId;
					}
					
					var url = '../../{0}?memberType={1}&memberCardOrMobileOrName={2}&MinTotalMemberCost={3}' +
							'&MaxTotalMemberCost={4}&consumptionMinAmount={5}&consumptionMaxAmount={6}&memberBalance={7}&memberBalanceEqual={8}&orderBy={9}&dataSource={10}';
					url = String.format(
						url, 
						'ExportHistoryStatisticsToExecl.do', 
						memberType,
						Ext.getCmp('numberSearchByMemberPhoneOrCardOrName').getValue(),
						Ext.getCmp('textTotalMinMemberCost_numberField_member').getValue(),
						Ext.getCmp('textTotalMaxMemberCost_numberField_member').getValue(),
						Ext.getCmp('textTotalMinMemberCostCount_numberField_member').getValue(),
						Ext.getCmp('textTotalMaxMemberCostCount_numberField_member').getValue(),
						Ext.getCmp('textMemberMinBalance_numberField_member').getValue(),
						Ext.getCmp('textMemberMaxBalance_numberField_member').getValue(),
						m_searchAdditionFilter,
						'memberList'
					);
					
					window.location = url;
				}
			}]
		});
		
		

		
		memberBasicGrid = createGridPanel(
			'',
			'会员信息',
			'',
			'',
			'../../QueryMember.do',
			[
				[true, false, false, true],
				['名称', 'name'],
				['类型', 'memberType.name'],
				['创建时间','createDateFormat'],
				['消费次数', 'consumptionAmount',,'right', 'Ext.ux.txtFormat.gridDou'],
				['消费总额', 'totalConsumption',,'right', 'Ext.ux.txtFormat.gridDou'],
				['累计积分', 'totalPoint',,'right', 'Ext.ux.txtFormat.gridDou'],
				['当前积分', 'point',,'right', 'Ext.ux.txtFormat.gridDou'],
				['总充值额', 'totalCharge',,'right'],
				['账户余额', 'totalBalance',,'right'],
				['手机号码', 'mobile', 125],
				['会员卡号', 'memberCard', 125],
				['操作', 'operation', 270, 'center', 'memberOperationRenderer']
			],
			MemberBasicRecord.getKeys(),
			[['isPaging', true], ['restaurantID', restaurantID],  ['dataSource', 'normal'],  ['needSum', true]],
			100,
			'',
			[memberBasicGridTbar, memberBasicGridExcavateMemberTbar, memberBasicGridSortTbar]
		);	
		memberBasicGrid.region = 'center';
		memberBasicGrid.loadMask = null;
		
		memberBasicGrid.on('rowdblclick', function(e){
			updateMemberHandler();
		});
		
		memberBasicGrid.keys = [{
			key : Ext.EventObject.ENTER,
			scope : this,
			fn : function(){ 
				Ext.getCmp('searchMember_btn_member').handler();
			}
		}];
	};
});
