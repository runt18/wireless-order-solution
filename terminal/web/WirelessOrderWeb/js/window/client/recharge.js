var rd_rechargeSreachMemberCardWin;
var rechargeOperateData;
Ext.onReady(function(){
	var pe = Ext.query('#divMemberRechargeContent')[0].parentElement;
	var mw = parseInt(pe.style.width);
	var mh = parseInt(pe.style.height);
	
	var memeberCardAliasID = {
		xtype : 'numberfield',
		id : 'rd_numMemberCardAliasForRecharge',
		inputType : 'password',
		fieldLabel : '会员卡号' + Ext.ux.txtFormat.xh,
		disabled : false,
		style : 'font-weight: bold; color: #FF0000;',
		maxLength : 10,
		maxLengthText : '请输入10位会员卡号',
		minLength : 10,
		minLengthText : '请输入10位会员卡号',
		width : 315,
		allowBlank : false,
		blankText : '会员卡不能为空, 请刷卡.',
		listeners : {
			render : function(e){
				if(rd_rechargeMemberCardAlias != null && rd_rechargeMemberCardAlias != ''){
					e.setValue(rd_rechargeMemberCardAlias);
					rechargeLoadMemberData();
				}
			}
		}
	};
	
	new Ext.Panel({
		renderTo : 'divMemberRechargeContent',
		width : mw,
		height : mh,
		frame : true,
		items : [{
			xtype : 'panel',
			layout : 'column',
			defaults : {
				xtype : 'panel',
				layout : 'form',
				labelWidth : 80,
				labelAlign : 'right',
				columnWidth : .33,
				defaults : {
					xtype : 'numberfield',
					width : 110
				}
			},
			items : [ {
				columnWidth : .7,
				items : [memeberCardAliasID]
			}, {
				xtype : 'panel',
				columnWidth : .3,
				html : ['<div class="x-form-item" >',
				    '<input type="button" value="查找" onClick="rechargeSreachMemberCard(this)" style="cursor:pointer; width:80px;" />',
				    '&nbsp;&nbsp;',
				    '<input type="button" value="读卡" onClick="rechargeLoadMemberData(this)" style="cursor:pointer; width:80px;" />',
				    '</div>'
				].join('')
			},{
				items : [{
					id : 'rd_numTotalBalance',
					fieldLabel : '总余额',
					disabled : true
				}]
			}, {
				items : [{
					id : 'rd_numBaseBalance',
					fieldLabel : '基础余额',
					disabled : true
				}]
			}, {
				items : [{
					id : 'rd_numExtraBalance',
					fieldLabel : '赠送余额',
					disabled : true
				}]
			}, {
				items : [{
					id : 'rd_numPayMannerMoney',
					fieldLabel : '收款金额' + Ext.ux.txtFormat.xh,
					allowBlank : false,
					listeners : {
						render : function(thiz){
							Ext.getDom(thiz.getId()).onkeyup = function(){
								if(thiz.getRawValue() != '' && rechargeOperateData != null){
									var iv = thiz.getValue();
									iv = parseInt(iv);
									if(iv < 1)
										iv = 1;
									if(iv > 100000)
										iv = 100000;
									thiz.setValue(parseInt(iv));
									
									var rm = thiz.getValue();
									var pmm = Ext.getCmp('rd_numRechargeMoney');
									var gm = Ext.getCmp('rd_numGiftMoney');
									var gp = Ext.getCmp('rd_numGiftPoint');
									var tempMT = rechargeOperateData.root[0].memberType;
									
									pmm.setValue(rm);
									gm.setValue(parseInt(rm * Math.abs((tempMT.chargeRate - 1))));
									gp.setValue(Math.round(rm * tempMT.exchangeRate));
								}
							};
						}
					}
				}]
			}, {
				items : [{
					id : 'rd_numRechargeMoney',
					fieldLabel : '充值金额' + Ext.ux.txtFormat.xh,
					allowBlank : false,
					disabled : true
				}]
			}, {
				items : [{
					id : 'rd_numGiftMoney',
					fieldLabel : '赠送金额',
					disabled : true
				}]
			}, {
				items : [{
					xtype : 'combo',
					id : 'rd_comboRechargeType',
					fieldLabel : '收款方式' + Ext.ux.txtFormat.xh,
					readOnly : true,
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
					id : 'rd_numTotalPoint',
					fieldLabel : '当前积分',
					disabled : true
				}]
			}, {
				items : [{
					id : 'rd_numGiftPoint',
					fieldLabel : '赠送积分',
					disabled : true
				}]
			}, {
				columnWidth : 1,
				items : [{
					xtype : 'textfield',
					id : 'rd_txtRechargeComment',
					fieldLabel : '备注',
					width : 520
				}]
			}, {
				columnWidth : 1,
				bodyStyle : 'font-size:20px;font-weight:bold;color:#15428B;text-align:center;',
				html : '<hr style="color:#DDD"/>会员资料<hr style="color:#DDD"/>'
			}, {
				items : [{
					id : 'rd_numClientID',
					fieldLabel : '客户编号',
					disabled : true
				}]
			}, {
				items : [{
					xtype : 'textfield',
					id : 'rd_txtClientName',
					fieldLabel : '会员名称',
					disabled : true
				}]
			}, {
				items : [{
					xtype : 'textfield',
					id : 'rd_txtMmeberType',
					fieldLabel : '会员类别',
					disabled : true
				}]
			}, {
				items : [{
					xtype : 'textfield',
					id : 'rd_txtClientSex',
					fieldLabel : '性别',
					disabled : true
				}]
			}, {
				items : [{
					xtype : 'textfield',
					id : 'rd_txtClientMobile',
					fieldLabel : '手机',
					disabled : true
				}]
			}, {
				items : [{
					xtype : 'textfield',
					id : 'rd_txtClientTel',
					fieldLabel : '电话',
					disabled : true
				}]
			}, {
				items : [{
					xtype : 'textfield',
					id : 'rd_txtClientBirthday',
					fieldLabel : '生日',
					disabled : true
				}]
			}, {
				items : [{
					xtype : 'textfield',
					id : 'rd_txtClientIDCard',
					fieldLabel : '身份证',
					disabled : true
				}]
			}, {
				items : [{
					xtype : 'textfield',
					id : 'rd_txtClientCompany',
					fieldLabel : '公司',
					disabled : true
				}]
			}, {
				items : [{
					xtype : 'textfield',
					id : 'rd_txtClientTastePref',
					fieldLabel : '口味',
					disabled : true
				}]
			}, {
				items : [{
					xtype : 'textfield',
					id : 'rd_txtClientTaboo',
					fieldLabel : '忌讳',
					disabled : true
				}]
			}, {
				columnWidth : 1,
				items : [{
					xtype : 'textfield',
					id : 'rd_txtClientContactAddress',
					fieldLabel : '联系地址',
					width : 520,
					disabled : true
				}]
			}, {
				columnWidth : 1,
				items : [{
					xtype : 'textfield',
					id : 'rd_txtClientComment',
					fieldLabel : '备注',
					width : 520,
					disabled : true
				}]
			}]
		}],
		keys : [{
			key : Ext.EventObject.ENTER,
			scope : this,
			fn : function(){
				rechargeLoadMemberData();
			}
		}]
	});
});

function rechargeLoadMemberData(_c){
	_c = _c == null || typeof _c == 'undefined' ? {} : _c;
	
	var cardAlias = Ext.getCmp('rd_numMemberCardAliasForRecharge');
	if(typeof _c.memberCard != 'undefined'){
		cardAlias.setValue(_c.memberCard);
	}else{
		if(!cardAlias.isValid()){
			return;
		}
	}
	var rd_mask_load_recharge = new Ext.LoadMask(document.body, {
		msg : '正在读卡, 请稍后......',
		remove : true
	});
	rd_mask_load_recharge.show();
	Ext.Ajax.request({
		url : '../../QueryMember.do',
		params : {
			pin : pin,
			restaurantID : restaurantID,
			dataSource : 'normal',
			params : Ext.encode({
				searchType : 2,
				searchOperation : 0,
				searchValue : cardAlias.getValue()
			})
		},
		success : function(res, opt){
			var jr = Ext.decode(res.responseText);
			if(jr.success){
				rechargeOperateData = jr;
				rechargeBindMemberData(rechargeOperateData.root[0]);
			}else{
				Ext.ux.showMsg(jr);
			}
			rd_mask_load_recharge.hide();
		},
		failure : function(res, opt){
			rd_mask_load_recharge.hide();
			Ext.ux.showMsg(Ext.decode(res.responseText));
		}
	});
}

function rechargeBindMemberData(data){
	var memberCardAliasForRecharge = Ext.getCmp('rd_numMemberCardAliasForRecharge');
	var totalBalance = Ext.getCmp('rd_numTotalBalance');
	var baseBalance = Ext.getCmp('rd_numBaseBalance');
	var extraBalance = Ext.getCmp('rd_numExtraBalance');
	var totalPoint = Ext.getCmp('rd_numTotalPoint');
	var clientID = Ext.getCmp('rd_numClientID');
	var clientName = Ext.getCmp('rd_txtClientName');
	var memberType = Ext.getCmp('rd_txtMmeberType');
	var clientSex = Ext.getCmp('rd_txtClientSex');
	var clientMobile = Ext.getCmp('rd_txtClientMobile');
	var clientTel = Ext.getCmp('rd_txtClientTel');
	var clientBirthday = Ext.getCmp('rd_txtClientBirthday');
	var clientIDCard = Ext.getCmp('rd_txtClientIDCard');
	var clientCompany = Ext.getCmp('rd_txtClientCompany');
	var clientTastePref = Ext.getCmp('rd_txtClientTastePref');
	var clientTaboo = Ext.getCmp('rd_txtClientTaboo');
	var clientContactAddress = Ext.getCmp('rd_txtClientContactAddress');
	var clientComment = Ext.getCmp('rd_txtClientComment');
	
	var rechargeMoney = Ext.getCmp('rd_numRechargeMoney');
	var rechargeType = Ext.getCmp('rd_comboRechargeType');
	var rechargePayMannerMoney = Ext.getCmp('rd_numPayMannerMoney');
	var rechargeComment = Ext.getCmp('rd_txtRechargeComment');
	var rechargeGiftMoney = Ext.getCmp('rd_numGiftMoney');
	var rechargeGiftPoint = Ext.getCmp('rd_numGiftPoint');
	
	rechargeMoney.setValue();
	rechargeType.setValue(1);
	rechargePayMannerMoney.setValue();
	rechargeComment.setValue();
	rechargeGiftMoney.setValue();
	rechargeGiftPoint.setValue();
	
	data = data == null || typeof data == 'undefined' ? {} : data;
	var memberCard = typeof data['memberCard'] == 'undefined' ? {} : data['memberCard'];
	var client = typeof data['client'] == 'undefined' ? {} : data['client'];
	var clientType = typeof client['clientType'] == 'undefined' ? {} : client['clientType'];
	
	memberCardAliasForRecharge.setValue(memberCard['aliasID']);
	totalBalance.setValue(data['totalBalance']);
	baseBalance.setValue(data['baseBalance']);
	extraBalance.setValue(data['extraBalance']);
	totalPoint.setValue(data['point']);
	clientID.setValue(client['clientID']);
	clientName.setValue(client['name']);
	memberType.setValue(clientType['name']);
	clientSex.setValue(client['sexDisplay']);
	clientMobile.setValue(client['mobile']);
	clientTel.setValue(client['tele']);
	clientBirthday.setValue(client['birthdayFormat']);
	clientIDCard.setValue(client['IDCard']);
	clientCompany.setValue(client['company']);
	clientTastePref.setValue(client['tastePref']);
	clientTaboo.setValue(client['taboo']);
	clientContactAddress.setValue(client['contactAddress']);
	clientComment.setValue(client['comment']);
	
	memberCardAliasForRecharge.clearInvalid();
	rechargeMoney.clearInvalid();
	rechargePayMannerMoney.clearInvalid();
	rechargeComment.clearInvalid();
	
}

/**
 * 充值
 * @param _c
 */
function rechargeControlCenter(_c){
	_c = _c == null || typeof _c == 'undefined' ? {} : _c;
	
	if(rechargeOperateData == null || typeof rechargeOperateData == 'undefined'){
		Ext.example.msg('提示', '未读取会员信息, 请先刷卡.');
		return;
	}
	
	var member = rechargeOperateData.root[0];
	if(member.memberType.attributeValue != 0 || member.client.clientTypeID <=0 ){
		Ext.example.msg('提示', '不记名用户和优惠属性会员不允许, 请重新刷卡.');
		return;
	}
	
	var memberCardAliasForRecharge = Ext.getCmp('rd_numMemberCardAliasForRecharge');
	var memberCardAlias = Ext.getCmp('rd_numMemberCardAliasForRecharge');
	var rechargeMoney = Ext.getCmp('rd_numRechargeMoney');
	var rechargeType = Ext.getCmp('rd_comboRechargeType');
	var payMannerMoney = Ext.getCmp('rd_numPayMannerMoney');
	var comment = Ext.getCmp('rd_txtRechargeComment');
	
	if(!memberCardAliasForRecharge.isValid()){
		return;
	}
	if(memberCardAliasForRecharge.getValue() != member.memberCard.aliasID){
		Ext.example.msg('提示', '会员信息已改变, 请重新读卡.');
		return;
	}
	
	if(!rechargeMoney.isValid() || !payMannerMoney.isValid() || !rechargeType.isValid()){
		return;
	}
	
	var mask = new Ext.LoadMask(document.body, {
		msg : '正在充值, 请稍候......',
		remove : true
	});
	mask.show();
	
	Ext.Ajax.request({
		url : '../../MemberRecharge.do',
		params : {
			pin : pin,
			restaurantID : restaurantID,
			memberID : rechargeOperateData.root[0].id,
			memberCardAlias : memberCardAlias.getValue(),
			rechargeMoney : rechargeMoney.getValue(),
			rechargeType : rechargeType.getValue(),
			payMannerMoney : payMannerMoney.getValue(),
			comment : comment.getValue(),
			isPrint : typeof _c.isPrint == 'boolean' ? _c.isPrint : true
		},
		success : function(res, opt){
			mask.hide();
			var jr = Ext.decode(res.responseText);
			if(jr.success){
				if(typeof _c.reload == 'boolean' && _c.reload){
					rechargeLoadMemberData();
				}
				Ext.example.msg(jr.title, jr.msg);
				if(typeof _c.callback == 'function'){
					jr.data = {
						memberCardAlias : memberCardAlias.getValue()
					};
					_c.callback(jr);
				}
			}else{
				Ext.ux.showMsg(jr);
			}
		},
		failure : function(res, opt){
			mask.hide();
			Ext.ux.showMsg(Ext.decode(res.responseText));
		}
	});
}

function initRechargeSreachMemberCard(){
	if(!rd_rechargeSreachMemberCardWin){
		rd_rechargeSreachMemberCardWin = new Ext.Window({
			id : 'rd_rechargeSreachMemberCardWin',
			title : '查找会员',
			closable : false,
			modal : true,
			resizable : false,
			width : 700,
			height : 430,
			layout : 'border',
			items : [{
				xtype : 'panel',
				border : false,
				region : 'center'
			}],
			keys : [{
				key : Ext.EventObject.ESC,
				scope : this,
				fn : function(){
					rd_rechargeSreachMemberCardWin.hide();
				}
			}],
			listeners : {
				hide : function(thiz){
					thiz.body.update('');
				},
				show : function(thiz){
					thiz.center();
					thiz.load({
						url : '../window/client/searchMemberCard.jsp',
						scripts : true,
						params : {
							callback : 'rechargeSearchMemberCardCallback'
						}
					});
				}
			},
			bbar : ['->', {
				text : '关闭',
				iconCls : 'btn_close',
				handler : function(e){
					rd_rechargeSreachMemberCardWin.hide();
				}
			}]
		});
	}
}

function rechargeSreachMemberCard(){
	initRechargeSreachMemberCard();
	rd_rechargeSreachMemberCardWin.show();
}

function rechargeSearchMemberCardCallback(data, _c){
	if(data['memberType.attributeValue'] == 0 && data['client.clientTypeID'] > 0){
		rd_rechargeSreachMemberCardWin.hide();
		var cardAlias = Ext.getCmp('rd_numMemberCardAliasForRecharge');
		cardAlias.setValue(data['memberCard.aliasID']);
		rechargeLoadMemberData();
	}else{
		Ext.example.msg("提示", "匿名用户或会员类型优惠属性不能充值, 请重新选择.");
		return;
	}
}