var rd_rechargeSreachMemberCardWin;
var rechargeOperateData;
Ext.onReady(function(){
	var pe = Ext.query('#divMemberRechargeContent')[0].parentElement;
	var mw = parseInt(pe.style.width);
	var mh = parseInt(pe.style.height);
	var memeberMobile = new Ext.form.NumberField({
		xtype : 'numberfield',
		id : 'rd_numMemberMobileForRecharge',
		fieldLabel : '手机号码' + Ext.ux.txtFormat.xh,
		disabled : false,
		listeners : {
			render : function(thiz){
				new Ext.KeyMap(thiz.getId(), [{
					key : Ext.EventObject.ENTER,
					scope : this,
					fn : function(){
						rechargeLoadMemberData({read:1});
					}
				}]);
				if(rd_rechargeMemberMobile != null && rd_rechargeMemberMobile != '' && rd_rechargeMemberMobile != 'null'){
					thiz.setValue(rd_rechargeMemberMobile);
					rechargeLoadMemberData({read:1});
				}
			}
		}
	});
	var memeberCard = {
		xtype : 'numberfield',
		id : 'rd_numMemberCardForRecharge',
		fieldLabel : '会员卡号',
		disabled : false,
		blankText : '会员卡不能为空, 请刷卡.',
		listeners : {
			render : function(thiz){
				new Ext.KeyMap(thiz.getId(), [{
					key : Ext.EventObject.ENTER,
					scope : this,
					fn : function(){
						rechargeLoadMemberData({read:2});
					}
				}]);
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
				layout : 'form',
				labelWidth : 80,
				labelAlign : 'right',
				columnWidth : .33,
				defaults : {
					xtype : 'numberfield',
					width : 110
				}
			},
			items : [{
				items : [memeberMobile]
			}, {
				items : [memeberCard]
			}, {
				xtype : 'panel',
				columnWidth : .3,
				html : ['<div class="x-form-item" >',
				    '<input type="button" value="读手机号码" onClick="rechargeLoadMemberData({read:1})" style="cursor:pointer; width:80px;" />',
				    '&nbsp;&nbsp;',
				    '<input type="button" value="读会员卡" onClick="rechargeLoadMemberData({read:2})" style="cursor:pointer; width:80px;" />',
				    '</div>'
				].join('')
			},{
				columnWidth : 1
			},{
				items : [{
					id : 'rd_numBaseBalance',
					fieldLabel : '收款总额',
					disabled : true
				}]
			}, {	
				items : [{
					id : 'rd_numTotalBalance',
					fieldLabel : '账户余额',
					disabled : true
				}]
			}, {
				items : [{
					id : 'rd_numTotalPoint',
					fieldLabel : '当前积分',
					disabled : true
				}]
			},{
				items : [{
					id : 'rd_numPayMannerMoney',
					fieldLabel : '实收金额' + Ext.ux.txtFormat.xh,
					allowBlank : false,
					listeners : {
						render : function(thiz){
							Ext.getDom(thiz.getId()).onkeyup = function(){
								if(thiz.getRawValue() != '' && rechargeOperateData != null){
									var iv = thiz.getValue();
									iv = parseInt(iv);
									if(iv < 1)
										iv = 0;
									if(iv > 100000)
										iv = 100000;
									thiz.setValue(parseInt(iv));
									
									var rm = thiz.getValue();
									var pmm = Ext.getCmp('rd_numRechargeMoney');
									var tempMT = rechargeOperateData.memberType;
									pmm.setValue(Math.round(rm * tempMT.chargeRate));
								}
							};
						}
					}
				}]
			}, {
				items : [{
					id : 'rd_numRechargeMoney',
					fieldLabel : '账户充额' + Ext.ux.txtFormat.xh,
					allowBlank : false
				}]
			},{
				items : [{
					xtype : 'combo',
					id : 'rd_comboRechargeType',
					fieldLabel : '收款方式' + Ext.ux.txtFormat.xh,
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
			},{
				columnWidth : 1,
				items : [{
					xtype : 'textfield',
					id : 'rd_txtRechargeComment',
					fieldLabel : '备注',
					width : 540
				}]
			}, {
				columnWidth : 1,
				html : '<hr style="color:#DDD"/>'
			}, {
				items : [{
					xtype : 'textfield',
					id : 'rd_txtMemberName',
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
					id : 'rd_txtMemberSex',
					fieldLabel : '性别',
					disabled : true
				}]
			}, {
				items : [{
					xtype : 'textfield',
					id : 'rd_txtMemberBirthday',
					fieldLabel : '生日',
					disabled : true
				}]
			}, {
				items : [{
					xtype : 'textfield',
					id : 'rd_txtMemberIDCard',
					fieldLabel : '身份证',
					disabled : true
				}]
			}, {
				items : [{
					xtype : 'textfield',
					id : 'rd_txtMemberCompany',
					fieldLabel : '公司',
					disabled : true
				}]
			}, {
				columnWidth : 1,
				items : [{
					xtype : 'textfield',
					id : 'rd_txtMemberContactAddress',
					fieldLabel : '联系地址',
					width : 540,
					disabled : true
				}]
			}, {
				columnWidth : 1,
				items : [{
					xtype : 'textfield',
					id : 'rd_txtMemberComment',
					fieldLabel : '备注',
					width : 540,
					disabled : true
				}]
			}]
		}]
	});
});
/**
 * 
 * @param c
 */
function rechargeLoadMemberData(c){
	c = c == null || typeof c == 'undefined' ? {} : c;
	
	var mobile = Ext.getCmp('rd_numMemberMobileForRecharge');
	var card = Ext.getCmp('rd_numMemberCardForRecharge');
	if(typeof c.read != 'number'){
		Ext.example.msg('提示', '操作失败, 程序异常, 请联系客服人员.');
		return;			
	}else if(c.read != 1 && c.read != 2){
		Ext.example.msg('提示', '操作失败, 程序异常, 请联系客服人员.');
		return;		
	}else if(c.read == 1 && (mobile.getRawValue() == '' || !mobile.isValid())){
		// memberMobile
		Ext.example.msg('提示', '请输入11位手机号码.');
		mobile.focus(mobile, true);
		return;
	}else if(c.read == 2 && card.getRawValue() == ''){
		// memberCard
		Ext.example.msg('提示', '请输入会员卡号.');
		card.focus(mobile, true);
		return;
	}
	
	var rd_mask_load_recharge = new Ext.LoadMask(document.body, {
		msg : '正在读卡, 请稍后......',
		remove : true
	});
	rd_mask_load_recharge.show();
	Ext.Ajax.request({
		url : '../../QueryMember.do',
		params : {
			dataSource : 'normal',
			memberCardOrMobile : c.read == 1 ? mobile.getValue() : (c.read == 2 ? card.getValue() : '')
		},
		success : function(res, opt){
			var jr = Ext.decode(res.responseText);
			if(jr.success){
				if(jr.root.length == 1){
					rechargeOperateData = jr.root[0];
					if(rechargeOperateData.memberType.attributeValue == 0){
						Ext.example.msg('提示', '会员信息读取成功.');
						rechargeBindMemberData(rechargeOperateData);
						Ext.getCmp('rd_numPayMannerMoney').focus(100, true);
						Ext.getCmp('rd_numRechargeMoney').clearInvalid();
					}else{
						Ext.example.msg('提示', '非充值属性会员不允许充值, 请重新刷卡.');
						rechargeOperateData = null;
						rechargeBindMemberData();
					}
				}else{
					Ext.example.msg('提示', '该会员信息不存在, 请重新输入条件后重试.');
					rechargeOperateData = null;
					rechargeBindMemberData();
				}
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
	var mobile = Ext.getCmp('rd_numMemberMobileForRecharge');
	var memberCard = Ext.getCmp('rd_numMemberCardForRecharge');
	var totalBalance = Ext.getCmp('rd_numTotalBalance');
	var baseBalance = Ext.getCmp('rd_numBaseBalance');
	var totalPoint = Ext.getCmp('rd_numTotalPoint');
	var name = Ext.getCmp('rd_txtMemberName');
	var memberType = Ext.getCmp('rd_txtMmeberType');
	var sex = Ext.getCmp('rd_txtMemberSex');
	var birthday = Ext.getCmp('rd_txtMemberBirthday');
	var IDCard = Ext.getCmp('rd_txtMemberIDCard');
	var company = Ext.getCmp('rd_txtMemberCompany');
	var contactAddress = Ext.getCmp('rd_txtMemberContactAddress');
	var comment = Ext.getCmp('rd_txtMemberComment');
	
	var rechargeMoney = Ext.getCmp('rd_numRechargeMoney');
	var rechargeType = Ext.getCmp('rd_comboRechargeType');
	var rechargePayMannerMoney = Ext.getCmp('rd_numPayMannerMoney');
	var rechargeComment = Ext.getCmp('rd_txtRechargeComment');
	
	rechargeMoney.setValue();
	rechargeType.setValue(1);
	rechargePayMannerMoney.setValue();
	rechargeComment.setValue();
	
	data = data == null || typeof data == 'undefined' ? {} : data;
	var memberTypeData = typeof data['memberType'] == 'undefined' ? {} : data['memberType'];
	
	mobile.setValue(data['mobile']);
	memberCard.setValue(data['memberCard']);
	totalBalance.setValue(data['totalBalance']);
	baseBalance.setValue(data['baseBalance']);
	totalPoint.setValue(data['point']);
	name.setValue(data['name']);
	memberType.setValue(memberTypeData['name']);
	sex.setValue(data['sexText']);
	birthday.setValue(data['birthdayFormat']);
	IDCard.setValue(data['idCard']);
	company.setValue(data['company']);
	contactAddress.setValue(data['contactAddress']);
	comment.setValue(data['comment']);
	
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
	
	if(rechargeOperateData.memberType.attributeValue != 0){
		Ext.example.msg('提示', '优惠属性会员不允许充值, 请重新刷卡.');
		return;
	}
	
	var memberMobile = Ext.getCmp('rd_numMemberMobileForRecharge');
	var rechargeMoney = Ext.getCmp('rd_numRechargeMoney');
	var rechargeType = Ext.getCmp('rd_comboRechargeType');
	var payMannerMoney = Ext.getCmp('rd_numPayMannerMoney');
	var comment = Ext.getCmp('rd_txtRechargeComment');
	
	if(!memberMobile.isValid()){
		return;
	}
	if(memberMobile.getValue() != rechargeOperateData.mobile){
		Ext.example.msg('提示', '会员信息已改变, 请重新读取信息.');
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
		url : '../../OperateMember.do',
		params : {
			dataSource : 'charge',
			isCookie : true,
			memberID : rechargeOperateData.id,
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
//					rechargeLoadMemberData({});
				}
				Ext.example.msg(jr.title, jr.msg);
				if(typeof _c.callback == 'function'){
					jr.data = {
						memberCard : Ext.getCmp('rd_numMemberCardForRecharge').getValue()	
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
