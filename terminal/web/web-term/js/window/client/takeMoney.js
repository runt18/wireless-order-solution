var rd_rechargeSreachMemberCardWin;
var rechargeOperateData;
Ext.onReady(function(){

	var pe = Ext.query('#divMemberTakeMoney')[0].parentElement;
	var mw = parseInt(pe.style.width);
	var mh = parseInt(pe.style.height);
	var memeberMobile = new Ext.form.NumberField({
		xtype : 'numberfield',
		id : 'tm_numMemberMobileForTakeMoney',
		fieldLabel : '手机号码' + Ext.ux.txtFormat.xh,
		disabled : false,
		listeners : {
			render : function(thiz){
				new Ext.KeyMap(thiz.getId(), [{
					key : Ext.EventObject.ENTER,
					scope : this,
					fn : function(){
						takeMoneyLoadMemberData({read:1});
					}
				}]);
				if(tm_rechargeMemberMobile != null && tm_rechargeMemberMobile != '' && tm_rechargeMemberMobile != 'null'){
					thiz.setValue(tm_rechargeMemberMobile);
					takeMoneyLoadMemberData({read:1});
				}
			}
		}
	});
	
	var memberCard = {
		xtype : 'numberfield',
		id : 'tm_numMemberCardForTakeMoney',
		fieldLabel : '会员卡号',
		disabled : false,
		blankText : '会员卡不能为空, 请刷卡.',
		listeners : {
			render : function(thiz){
				new Ext.KeyMap(thiz.getId(), [{
					key : Ext.EventObject.ENTER,
					scope : this,
					fn : function(){
						takeMoneyLoadMemberData({read:2});
					}
				}]);
			}
		}
	};
	
	new Ext.Panel({
		renderTo : 'divMemberTakeMoney',
		width : mw,
		height : mh,
		frame : true,
		items : [{
			xtype : 'panel',
			layout : 'column',
			defaults : {
				//xtype : 'panel',
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
				items : [memberCard]
			}, 
			{
				xtype : 'panel',
				columnWidth : .3,
				html : ['<div class="x-form-item" >',
				    '<input type="button" value="读手机号码" onClick="takeMoneyLoadMemberData({read:1});" style="cursor:pointer; width:80px;" />',
				    '&nbsp;&nbsp;',
				    '<input type="button" value="读会员卡" onClick="takeMoneyLoadMemberData({read:2})" style="cursor:pointer; width:80px;" />',
				    '</div>'
				].join('')
			},{
				columnWidth : 1
			},
			{
				items : [{
					id : 'tm_numBaseBalance',
					fieldLabel : '收款总额',
					disabled : true
				}]
			}, {
				items : [{
					id : 'tm_numTotalBalance',
					fieldLabel : '账户余额',
					disabled : true
				}]
			}, {
				items : [{
					id : 'tm_numTotalPoint',
					fieldLabel : '当前积分',
					disabled : true
				}]
			}, {
				items : [{
					id : 'tm_numPayMannerMoney',
					fieldLabel : '实退金额' + Ext.ux.txtFormat.xh,
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
									var pmm = Ext.getCmp('tm_numTakeMoney');
									//var gm = Ext.getCmp('tm_numGiftMoney');
									//var gp = Ext.getCmp('tm_numGiftPoint');
									var tempMT = rechargeOperateData.memberType;
									
									pmm.setValue(Math.round(rm * tempMT.chargeRate));
									//gm.setValue(parseInt(rm * Math.abs((tempMT.chargeRate - 1))));
									//gp.setValue(Math.round(rm * tempMT.exchangeRate));
								}
							};
						}
					}
				}]
			}, {
				items : [{
					id : 'tm_numTakeMoney',
					fieldLabel : '账户扣额' + Ext.ux.txtFormat.xh,
					allowBlank : false
					//disabled : true
				}]
			}, {
				columnWidth : 1,
				items : [{
					xtype : 'textfield',
					id : 'tm_txtTakeMoneyComment',
					fieldLabel : '备注',
					width : 540
				}]
			}, {
				columnWidth : 1,
				html : '<hr style="color:#DDD"/>'
			}, {
				items : [{
					xtype : 'textfield',
					id : 'tm_txtMemberName',
					fieldLabel : '会员名称',
					disabled : true
				}]
			}, {
				items : [{
					xtype : 'textfield',
					id : 'tm_txtMmeberType',
					fieldLabel : '会员类别',
					disabled : true
				}]
			}, {
				items : [{
					xtype : 'textfield',
					id : 'tm_txtMemberSex',
					fieldLabel : '性别',
					disabled : true
				}]
			}, {
				items : [{
					xtype : 'textfield',
					id : 'tm_txtMemberBirthday',
					fieldLabel : '生日',
					disabled : true
				}]
			}, {
				items : [{
					xtype : 'textfield',
					id : 'tm_txtMemberIDCard',
					fieldLabel : '身份证',
					disabled : true
				}]
			}, {
				items : [{
					xtype : 'textfield',
					id : 'tm_txtMemberCompany',
					fieldLabel : '公司',
					disabled : true
				}]
			}, {
				columnWidth : 1,
				items : [{
					xtype : 'textfield',
					id : 'tm_txtMemberContactAddress',
					fieldLabel : '联系地址',
					width : 540,
					disabled : true
				}]
			}]
		}]
	});
	
});	
function takeMoneyLoadMemberData(c){
	
	c = c == null || typeof c == 'undefined' ? {} : c;
	
	var mobile = Ext.getCmp('tm_numMemberMobileForTakeMoney');
	var card = Ext.getCmp('tm_numMemberCardForTakeMoney');
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
	
	var tm_mask_load_recharge = new Ext.LoadMask(document.body, {
		msg : '正在读取, 请稍后......',
		remove : true
	});
	tm_mask_load_recharge.show();
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
						Ext.getCmp('tm_numPayMannerMoney').focus(100, true);
						Ext.getCmp('tm_numTakeMoney').clearInvalid();
					}else{
						Ext.example.msg('提示', '非充值属性会员不允许取款, 请重新刷卡.');
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
			tm_mask_load_recharge.hide();
		},
		failure : function(res, opt){
			tm_mask_load_recharge.hide();
			Ext.ux.showMsg(Ext.decode(res.responseText));
		}
	});
}	

function rechargeBindMemberData(data){
	var mobile = Ext.getCmp('tm_numMemberMobileForTakeMoney');
	var memberCard = Ext.getCmp('tm_numMemberCardForTakeMoney');
	var totalBalance = Ext.getCmp('tm_numTotalBalance');
	var baseBalance = Ext.getCmp('tm_numBaseBalance');
	//var extraBalance = Ext.getCmp('tm_numExtraBalance');
	var totalPoint = Ext.getCmp('tm_numTotalPoint');
	var name = Ext.getCmp('tm_txtMemberName');
	var memberType = Ext.getCmp('tm_txtMmeberType');
	var sex = Ext.getCmp('tm_txtMemberSex');
	var birthday = Ext.getCmp('tm_txtMemberBirthday');
	var IDCard = Ext.getCmp('tm_txtMemberIDCard');
	var company = Ext.getCmp('tm_txtMemberCompany');
	var contactAddress = Ext.getCmp('tm_txtMemberContactAddress');
	//var comment = Ext.getCmp('tm_txtMemberComment');
	
	var rechargeMoney = Ext.getCmp('tm_numTakeMoney');
	//var rechargeType = Ext.getCmp('tm_comboTakeMoneyType');
	var rechargePayMannerMoney = Ext.getCmp('tm_numPayMannerMoney');
	var rechargeComment = Ext.getCmp('tm_txtTakeMoneyComment');
	//var rechargeGiftMoney = Ext.getCmp('tm_numGiftMoney');
	//var rechargeGiftPoint = Ext.getCmp('tm_numGiftPoint');
	
	rechargeMoney.setValue();
	//rechargeType.setValue(1);
	rechargePayMannerMoney.setValue();
	rechargeComment.setValue();
	//rechargeGiftMoney.setValue();
	//rechargeGiftPoint.setValue();
	
	data = data == null || typeof data == 'undefined' ? {} : data;
	var memberTypeData = typeof data['memberType'] == 'undefined' ? {} : data['memberType'];
	
	mobile.setValue(data['mobile']);
	memberCard.setValue(data['memberCard']);
	totalBalance.setValue(data['totalBalance']);
	baseBalance.setValue(data['baseBalance']);
	//extraBalance.setValue(data['extraBalance']);
	totalPoint.setValue(data['point']);
	name.setValue(data['name']);
	memberType.setValue(memberTypeData['name']);
	sex.setValue(data['sexText']);
	birthday.setValue(data['birthdayFormat']);
	IDCard.setValue(data['idCard']);
	company.setValue(data['company']);
	contactAddress.setValue(data['contactAddress']);
	//comment.setValue(data['comment']);
	
}
/**
 * 取款
 * @param _c
 */
function takeMoneyControlCenter(_c){
	_c = _c == null || typeof _c == 'undefined' ? {} : _c;
	
	if(rechargeOperateData == null || typeof rechargeOperateData == 'undefined'){
		Ext.example.msg('提示', '未读取会员信息, 请先刷卡.');
		return;
	}
	
	if(rechargeOperateData.memberType.attributeValue != 0){
		Ext.example.msg('提示', '优惠属性会员不允许充值, 请重新刷卡.');
		return;
	}
	
	var memberMobile = Ext.getCmp('tm_numMemberMobileForTakeMoney');
	var rechargeMoney = Ext.getCmp('tm_numTakeMoney');
	//var rechargeType = Ext.getCmp('tm_comboTakeMoneyType');
	var payMannerMoney = Ext.getCmp('tm_numPayMannerMoney');
	var comment = Ext.getCmp('tm_txtTakeMoneyComment');
	
	if(!memberMobile.isValid()){
		return;
	}
	if(memberMobile.getValue() != rechargeOperateData.mobile){
		Ext.example.msg('提示', '会员信息已改变, 请重新读取信息.');
		return;
	}
	if(!rechargeMoney.isValid() || !payMannerMoney.isValid()){
		return;
	}
	
	var mask = new Ext.LoadMask(document.body, {
		msg : '正在取款, 请稍候......',
		remove : true
	});
	mask.show();
	
	Ext.Ajax.request({
		url : '../../OperateMember.do',
		params : {
			dataSource : 'takeMoney',
			isCookie : true,
			memberID : rechargeOperateData.id,
			takeMoney : rechargeMoney.getValue(),
			//rechargeType : rechargeType.getValue(),
			payMannerMoney : payMannerMoney.getValue(),
			comment : comment.getValue(),
			isPrint : typeof _c.isPrint == 'boolean' ? _c.isPrint : true
		},
		success : function(res, opt){
			mask.hide();
			var jr = Ext.decode(res.responseText);
			if(jr.success){
				if(typeof _c.reload == 'boolean' && _c.reload){
//					takeMoneyLoadMemberData({});
				}
				Ext.example.msg(jr.title, jr.msg);
				if(typeof _c.callback == 'function'){
					jr.data = {
						memberCard : Ext.getCmp('tm_numMemberCardForTakeMoney').getValue()	
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
	
