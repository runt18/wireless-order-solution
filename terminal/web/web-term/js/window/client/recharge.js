var rd_rechargeSreachMemberCardWin;
var rechargeOperateData;


Ext.onReady(function(){
	var pe = Ext.query('#divMemberRechargeContent')[0].parentElement;
	var mw = parseInt(pe.style.width);
	var mh = parseInt(pe.style.height);
	var memeberMobile = new Ext.form.NumberField({
		xtype : 'numberfield',
		id : 'rd_memberInfoRecharge',
		fieldLabel : '手机号码/卡号' + Ext.ux.txtFormat.xh,
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
					rechargeLoadMemberData();
				}
			}
		}
	});
	var memeberCard = {
		xtype : 'numberfield',
		fieldLabel : '会员卡号',
		disabled : true,
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
				columnWidth : .3,
				html : '&nbsp;'
			},{
				labelWidth : 100,
				items : [memeberMobile]
			}, {
				xtype : 'panel',
				columnWidth : .3,
				html : ['<div class="x-form-item" >',
				    '&nbsp;&nbsp;&nbsp;<input type="button" value="读取会员" onClick="rechargeLoadMemberData()" style="cursor:pointer; width:80px;" />',
				    '</div>'
				].join('')
			},{
				columnWidth : 1
			}, {	
				items : [{
					id : 'rd_numTotalBalance',
					cls : 'disableInput',
					fieldLabel : '账户余额',
					disabled : true
				}]
			},{
				items : [{
					id : 'rd_numBaseBalance',
					cls : 'disableInput',
					fieldLabel : '基础余额',
					disabled : true
				}]
			},{
				items : [{
					id : 'rd_numberExtraBalance',
					cls : 'disableInput',
					fieldLabel : '赠送余额',
					disabled : true
				}]
			}, {
				hidden : true,
				items : [{
					id : 'rd_numTotalPoint',
					cls : 'disableInput',
					fieldLabel : '当前积分',
					disabled : true
				}]
			},{
				items : [{
					id : 'rd_numPayMannerMoney',
					cls : 'disableInput',
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
					cls : 'disableInput',
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
					fieldLabel : '充值说明',
					width : 540
				}]
			}, {
				columnWidth : 1,
				html : '<hr style="color:#DDD"/>'
			}, {
				items : [{
					xtype : 'textfield',
					id : 'rd_txtMemberName',
					cls : 'disableInput',
					fieldLabel : '会员名称',
					disabled : true
				}]
			}, {
				items : [{
					xtype : 'textfield',
					id : 'rd_txtMmeberType',
					cls : 'disableInput',
					fieldLabel : '会员类别',
					disabled : true
				}]
			}, {
				items : [{
					xtype : 'textfield',
					id : 'rd_txtMemberSex',
					cls : 'disableInput',
					fieldLabel : '性别',
					disabled : true
				}]
			}, {
				items : [{
					xtype : 'textfield',
					id : 'rd_numMemberMobileForRecharge',
					cls : 'disableInput',
					fieldLabel : '手机',
					disabled : true
				}]
			}, {
				items : [{
					xtype : 'textfield',
					id : 'rd_numMemberCardForRecharge',
					cls : 'disableInput',
					fieldLabel : '实体卡号',
					disabled : true
				}]
			}, {
				items : [{
					xtype : 'textfield',
					id : 'rd_numWeixinMemberCard',
					cls : 'disableInput',
					fieldLabel : '微信会员卡',
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
				columnWidth : 0.62,
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
	if(typeof Ext.ux.select_getMemberByCertainWin != 'undefined'){
		Ext.ux.select_getMemberByCertainWin.hide();
	}
	c = c == null || typeof c == 'undefined' ? {} : c;
	
	var mobile = Ext.getCmp('rd_memberInfoRecharge');
	if(!rd_rechargeMemberMobile && mobile.getValue() == ''){
		Ext.example.msg('提示', '操作失败, 请输入查找条件.');
		mobile.focus(true, 100);
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
			id : (rd_rechargeMemberMobile && rd_rechargeMemberMobile != 'null') ?rd_rechargeMemberMobile:'',
			sType : c.sType,
			memberCardOrMobileOrName : mobile.getValue()
		},
		success : function(res, opt){
			rd_mask_load_recharge.hide();
			var jr = Ext.decode(res.responseText);
			if(jr.success){
				rd_rechargeMemberMobile = null;
				if(jr.root.length == 1){
					rechargeOperateData = jr.root[0];
					if(rechargeOperateData.memberType.attributeValue == 0){
						Ext.example.msg('提示', '会员信息读取成功.');
						rechargeBindMemberData(rechargeOperateData);
						Ext.getCmp('rd_numPayMannerMoney').focus(true, 100);
						Ext.getCmp('rd_numRechargeMoney').clearInvalid();
					}else{
						Ext.example.msg('提示', '非充值属性会员不允许充值, 请重新刷卡.');
						rechargeOperateData = null;
						rechargeBindMemberData();
					}
				}else if(jr.root.length >= 1){
					c.callback = rechargeLoadMemberData;
					Ext.ux.select_getMemberByCertain(c);
				}else{
					Ext.example.msg('提示', '该会员信息不存在, 请重新输入条件后重试.');
					rechargeOperateData = null;
					rechargeBindMemberData();
				}
			}else{
				Ext.ux.showMsg(jr);
			}
			
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
	var weixinCard = Ext.getCmp('rd_numWeixinMemberCard');
	var totalBalance = Ext.getCmp('rd_numTotalBalance');
	var baseBalance = Ext.getCmp('rd_numBaseBalance');
	var extraBalance = Ext.getCmp('rd_numberExtraBalance');
	var totalPoint = Ext.getCmp('rd_numTotalPoint');
	var name = Ext.getCmp('rd_txtMemberName');
	var memberType = Ext.getCmp('rd_txtMmeberType');
	var sex = Ext.getCmp('rd_txtMemberSex');
	var birthday = Ext.getCmp('rd_txtMemberBirthday');
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
	weixinCard.setValue(data['weixinCard']);
	totalBalance.setValue(data['totalBalance']);
	baseBalance.setValue(data['baseBalance']);
	extraBalance.setValue(data['extraBalance']);
	totalPoint.setValue(data['point']);
	name.setValue(data['name']);
	memberType.setValue(memberTypeData['name']);
	sex.setValue(data['sexText']);
	birthday.setValue(data['birthdayFormat']);
	comment.setValue(data['comment']);
	
}

function rechargeNumberFocus(){
	Ext.getCmp('rd_memberInfoRecharge').focus(true, 100);
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
			memberID : rechargeOperateData.id,
			rechargeMoney : rechargeMoney.getValue(),
			rechargeType : rechargeType.getValue(),
			payMannerMoney : payMannerMoney.getValue(),
			comment : comment.getValue(),
			isPrint : typeof _c.isPrint == 'boolean' ? _c.isPrint : true,
			sendSms : typeof _c.sendSms == 'boolean' ? _c.sendSms : true,
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
						memberCard : Ext.getCmp('rd_memberInfoRecharge').getValue()	
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
