var rd_rechargeSreachMemberCardWin;
var rechargeOperateData;
Ext.onReady(function(){

	var pe = Ext.query('#divMemberTakeMoney')[0].parentElement;
	var mw = parseInt(pe.style.width);
	var mh = parseInt(pe.style.height);
	var memeberMobile = new Ext.form.NumberField({
		xtype : 'numberfield',
		id : 'tm_memberInfoRecharge',
		fieldLabel : '手机号码/卡号' + Ext.ux.txtFormat.xh,
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
					takeMoneyLoadMemberData();
				}
			}
		}
	});
	
	var memberCard = {
		xtype : 'numberfield',
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
				columnWidth : .3,
				html : '&nbsp;'
			},{
				labelWidth : 100,
				items : [memeberMobile]
			}, 
			{
				xtype : 'panel',
				columnWidth : .3,
				html : ['<div class="x-form-item" >',
				    '&nbsp;&nbsp;&nbsp;<input type="button" value="读取会员" onClick="takeMoneyLoadMemberData();" style="cursor:pointer; width:80px;" />',
				    '</div>'
				].join('')
			},{
				columnWidth : 1
			}, {
				items : [{
					id : 'tm_numTotalBalance',
					cls : 'disableInput',
					fieldLabel : '账户余额',
					disabled : true
				}]
			},
			{
				items : [{
					id : 'tm_numBaseBalance',
					cls : 'disableInput',
					fieldLabel : '基础余额',
					disabled : true
				}]
			}, {
				items : [{
					xtype : 'numberfield',
					id : 'tm_numberExtraBalance',
					cls : 'disableInput',
					fieldLabel : '赠送余额',
					disabled : true,
					value : 0.00
				}]
			}, {
				items : [{
					id : 'tm_numTotalPoint',
					cls : 'disableInput',
					fieldLabel : '当前积分',
					disabled : true
				}]
			}, {
				items : [{
					id : 'tm_numPayMannerMoney',
					cls : 'disableInput',
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
									
									pmm.setValue(Math.round(rm));
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
					cls : 'disableInput',
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
					cls : 'disableInput',
					fieldLabel : '会员名称',
					disabled : true
				}]
			}, {
				items : [{
					xtype : 'textfield',
					id : 'tm_txtMmeberType',
					cls : 'disableInput',
					fieldLabel : '会员类别',
					disabled : true
				}]
			}, {
				items : [{
					xtype : 'textfield',
					id : 'tm_txtMemberSex',
					cls : 'disableInput',
					fieldLabel : '性别',
					disabled : true
				}]
			}, {
				items : [{
					xtype : 'textfield',
					id : 'tm_numMemberMobileForTakeMoney',
					cls : 'disableInput',
					fieldLabel : '手机',
					disabled : true
				}]
			}, {
				items : [{
					xtype : 'textfield',
					id : 'tm_numMemberCardForTakeMoney',
					cls : 'disableInput',
					fieldLabel : '实体卡号',
					disabled : true
				}]
			}, {
				items : [{
					xtype : 'textfield',
					id : 'tm_numWeixinMemberCard',
					cls : 'disableInput',
					fieldLabel : '微信会员卡',
					disabled : true
				}]
			}, {
				items : [{
					xtype : 'textfield',
					id : 'tm_txtMemberBirthday',
					cls : 'disableInput',
					fieldLabel : '生日',
					disabled : true
				}]
			}, {
				columnWidth : 0.62,
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
	if(typeof Ext.ux.select_getMemberByCertainWin != 'undefined'){
		Ext.ux.select_getMemberByCertainWin.hide();
	}	
	c = c == null || typeof c == 'undefined' ? {} : c;
	
	var mobile = Ext.getCmp('tm_memberInfoRecharge');
	if(tm_rechargeMemberMobile == 'null' && mobile.getValue() == ''){
		Ext.example.msg('提示', '操作失败, 请输入查找条件.');
		mobile.focus(true, 100);
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
			id : (tm_rechargeMemberMobile != 'null' && tm_rechargeMemberMobile != null)?tm_rechargeMemberMobile : '',
			sType : c.sType,
			memberCardOrMobileOrName : mobile.getValue()
		},
		success : function(res, opt){
			var jr = Ext.decode(res.responseText);
			tm_rechargeMemberMobile = null;
			if(jr.success){
				tm_mask_load_recharge.hide();
				if(jr.root.length == 1){
					rechargeOperateData = jr.root[0];
					if(rechargeOperateData.memberType.attributeValue == 0){
						Ext.example.msg('提示', '会员信息读取成功.');
						rechargeBindMemberData(rechargeOperateData);
						Ext.getCmp('tm_numPayMannerMoney').focus(true, 200);
						Ext.getCmp('tm_numTakeMoney').clearInvalid();
					}else{
						Ext.example.msg('提示', '非充值属性会员不允许取款, 请重新刷卡.');
						rechargeOperateData = null;
						rechargeBindMemberData();
					}
				}else if(jr.root.length > 1){
					c.callback = takeMoneyLoadMemberData;
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
			tm_mask_load_recharge.hide();
			Ext.ux.showMsg(Ext.decode(res.responseText));
		}
	});
}	


function rechargeBindMemberData(data){
	var mobile = Ext.getCmp('tm_numMemberMobileForTakeMoney');
	var memberCard = Ext.getCmp('tm_numMemberCardForTakeMoney');
	var weixinCard = Ext.getCmp('tm_numWeixinMemberCard');
	var totalBalance = Ext.getCmp('tm_numTotalBalance');
	var baseBalance = Ext.getCmp('tm_numBaseBalance');
	var extraBalance = Ext.getCmp('tm_numberExtraBalance');
	var totalPoint = Ext.getCmp('tm_numTotalPoint');
	var name = Ext.getCmp('tm_txtMemberName');
	var memberType = Ext.getCmp('tm_txtMmeberType');
	var sex = Ext.getCmp('tm_txtMemberSex');
	var birthday = Ext.getCmp('tm_txtMemberBirthday');
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
	extraBalance.setValue(data['extraBalance']);
	weixinCard.setValue(data['weixinCard']);
	totalPoint.setValue(data['point']);
	name.setValue(data['name']);
	memberType.setValue(memberTypeData['name']);
	sex.setValue(data['sexText']);
	birthday.setValue(data['birthdayFormat']);
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
						memberCard : Ext.getCmp('tm_memberInfoRecharge').getValue()	
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

//输入手机号
function takeMoneyNumberFocus(){
	Ext.getCmp('tm_memberInfoRecharge').focus(true, 100);
}
	
