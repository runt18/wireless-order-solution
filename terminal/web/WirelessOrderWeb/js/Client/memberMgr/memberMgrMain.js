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
			width : 650,
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
}
/**
 * 充值
 * @returns
 */
function rechargeHandler(){
	initRechargeWin();
	Ext.getCmp('rechargeWin').show();
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
			width : 1200,
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
					thiz.center();
					thiz.load({
						url : '../window/client/memberOperation.jsp',
						scripts : true,
						params : {
							memberMobile : !data ? '' : data['mobile'],
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
function queryMemberOperationSummaryHandler(){
	var mr_queryMemberConsumeSummaryWin = Ext.getCmp('mr_queryMemberConsumeSummaryWin');
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
memberOperationRenderer = function(val, m, record){
	return ''
		+ '<a href="javascript:updateMemberHandler()">修改</a>'
		+ '&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;'
		+ '<a href="javascript:deleteMemberHandler()">删除</a>'
		+ '&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;'
		+ '<a href="javascript:queryMemberOperationHandler()">操作明细</a>';
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
		title : '会员类型',
		region : 'west',
		width : 200,
		border : true,
		rootVisible : true,
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
	    		Ext.getDom('memberTypeShowType').innerHTML = e.text;
	    	},
	    	dblclick : function(e){
	    		Ext.getCmp('btnSearchMember').handler();
	    	}
	    }
	});
};

function gridInit(){
	var memberBasicGridTbar = new Ext.Toolbar({
		items : [{
			xtype : 'tbtext',
			text : String.format(Ext.ux.txtFormat.typeName, '会员类型', 'memberTypeShowType', '----')
		}, {
			xtype : 'tbtext',
			text : '&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;'
		}, {
			xtype : 'tbtext',
			text : '过滤:'
		}, {
			disabled : false,
			xtype : 'combo',
			id : 'comboMemberSearchType',
			fieldLabel : '过滤',
			readOnly : true,
			forceSelection : true,
			value : 0,
			width : 80,
			store : new Ext.data.SimpleStore({
				fields : ['value', 'text'],
				data : searchTypeData
			}),
			valueField : 'value',
			displayField : 'text',
			typeAhead : true,
			mode : 'local',
			triggerAction : 'all',
			selectOnFocus : true,
			listeners : {
				select : function(thiz, record, index){
					var value = thiz.getValue();
					var text = Ext.getCmp('txtSearchValueByText');
					var comboOperation = Ext.getCmp('comboSearchValueByOperation');
					var number = Ext.getCmp('numberSearchValueByNumber');
					if(value == 0){
						text.setVisible(false);
						comboOperation.setVisible(false);
						number.setVisible(false);
						mObj.searchValue = '';
						Ext.getCmp('btnSearchMember').handler();
					}else if(value == 1){
						text.setVisible(true);
						number.setVisible(false);
						text.setValue();
						mObj.searchValue = text.getId();
					}else{
						text.setVisible(false);
						number.setVisible(true);
						number.setValue();
						mObj.searchValue = number.getId();
						comboOperation.setValue(0);
						comboOperation.setVisible(!(value == 2 || value == 3));
					}
				}
			}
		}, {
			xtype : 'tbtext',
			text : '&nbsp;&nbsp;'
		}, {
			xtype : 'textfield',
			id : 'txtSearchValueByText',
			hidden : true
		}, {
			disabled : false,
			xtype : 'combo',
			id : 'comboSearchValueByOperation',
			readOnly : true,
			forceSelection : true,
			value : 0,
			width : 80,
			store : new Ext.data.SimpleStore({
				fields : ['value', 'text'],
				data : [[0, '等于'], [1, '大于等于'], [2, '小于等于']]
			}),
			valueField : 'value',
			displayField : 'text',
			typeAhead : true,
			mode : 'local',
			triggerAction : 'all',
			selectOnFocus : true,
			hidden : true
		}, {
			xtype : 'numberfield',
			id : 'numberSearchValueByNumber',
			style : 'text-align: left;',
			hidden : true
		}, '->', {
			text : '搜索',
			id : 'btnSearchMember',
			iconCls : 'btn_search',
			handler : function(){
				var memberTypeNode = memberTypeTree.getSelectionModel().getSelectedNode();
				var searchType = Ext.getCmp('comboMemberSearchType').getValue();
				var searchValue = Ext.getCmp(mObj.searchValue) ? Ext.getCmp(mObj.searchValue).getValue() : '';
				
				var gs = memberBasicGrid.getStore();
				
				if(memberTypeNode){
					if(memberTypeNode.childNodes.length > 0 && memberTypeNode.attributes.memberTypeId != -1){
						gs.baseParams['memberType'] = '';
						gs.baseParams['memberTypeAttr'] = memberTypeNode.attributes.attr;
					}else{
						gs.baseParams['memberType'] = memberTypeNode.attributes.memberTypeId;
						gs.baseParams['memberTypeAttr'] = '';
					}
				}else{
					gs.baseParams['memberType'] = '';
					gs.baseParams['memberTypeAttr'] = '';
				}
				
				gs.baseParams['name'] = searchType == 1 ? searchValue : '';
				gs.baseParams['memberCard'] = searchType == 2 ? searchValue : '';
				gs.baseParams['mobile'] = searchType == 3 ? searchValue : '';
				gs.baseParams['totalBalance'] = searchType == 4 ? searchValue : '';
				gs.baseParams['usedBalance'] = searchType == 5 ? searchValue : '';
				gs.baseParams['consumptionAmount'] = searchType == 6 ? searchValue : '';
				gs.baseParams['point'] = searchType == 7 ? searchValue : '';
				gs.baseParams['usedPoint'] = searchType == 8 ? searchValue : '';
				
				gs.baseParams['so'] = Ext.getCmp('comboSearchValueByOperation').getValue();
				gs.load({
					params : {
						start : 0,
						limit : GRID_PADDING_LIMIT_20
					}
				});
			}
		}, {
			hidden : true,
			text : '重置',
			iconCls : 'btn_refresh',
			handler : function(e){
				Ext.getCmp('btnRefreshMemberType').handler();
				Ext.getCmp('btnSearchMember').handler();
				var st = Ext.getCmp('comboMemberSearchType');
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
			text : '积分调整',
			iconCls : 'icon_tb_setting',
			handler : function(e){
				adjustPoint();
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
			['充值额', 'totalCharge',,'right', 'Ext.ux.txtFormat.gridDou'],
			['余额', 'totalBalance',,'right', 'Ext.ux.txtFormat.gridDou'],
			['手机号码', 'mobile'],
			['会员卡号', 'memberCard'],
			['操作', 'operation', 230, 'center', 'memberOperationRenderer']
		],
		MemberBasicRecord.getKeys(),
		[['isPaging', true], ['restaurantID', restaurantID],  ['dataSource', 'normal']],
		GRID_PADDING_LIMIT_20,
		'',
		memberBasicGridTbar
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
		width : 650,
		height : 296,
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

var btnConsumeDetail = new Ext.ux.ImageButton({
	imgPath : '../../images/btnConsumeDetail.png',
	imgWidth : 50,
	imgHeight : 50,
	tooltip : '消费明细',
	handler : function(e){
		queryMemberOperationHandler();
	}
});

var btnConsumeSummary = new Ext.ux.ImageButton({
	imgPath : '../../images/btnConsumeSummary.png',
	imgWidth : 50,
	imgHeight : 50,
	tooltip : '消费汇总',
	handler : function(e){
		queryMemberOperationSummaryHandler();
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


/**********************************************************************/
Ext.onReady(function(){
	treeInit();
	gridInit();
	
	new Ext.Panel({
		renderTo : 'divMember',
		width : parseInt(Ext.getDom('divMember').parentElement.style.width.replace(/px/g,'')),
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
			    btnConsumeDetail,
			    {xtype : 'tbtext', text : '&nbsp;&nbsp;'},
			    btnConsumeSummary,
			    {xtype : 'tbtext', text : '&nbsp;&nbsp;'},
			    btnAdjustPoint
			]
		})
	});
	 
	winInit();
	memberBasicWin.render(document.body);
});

