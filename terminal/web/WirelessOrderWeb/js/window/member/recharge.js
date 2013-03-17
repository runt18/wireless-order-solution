var rechargeOperateData;
Ext.onReady(function(){
	var pe = Ext.query('#divMemberRechargeContent')[0].parentElement;
	var mw = parseInt(pe.style.width);
	var mh = parseInt(pe.style.height);
	
	var memeberCardAliasID = {
		xtype : 'numberfield',
		id : 'numMemberCardAliasForRecharge',
		fieldLabel : '会员卡号' + Ext.ux.txtFormat.xh,
		disabled : false,
		style : 'font-weight: bold; color: #FF0000;',
		maxLength : 10,
		maxLengthText : '请输入10位会员卡号',
		minLength : 10,
		minLengthText : '请输入10位会员卡号',
		width : 350,
		allowBlank : false,
		blankText : '会员卡不能为空, 请刷卡.'
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
				columnWidth : .8,
				items : [memeberCardAliasID]
			}, {
				xtype : 'panel',
				columnWidth : .2,
				html : '<div class="x-form-item" ><input type="button" value="读卡" onClick="rechargeLoadMemberData(this)" style="cursor:pointer; width:80px;" /></div>'
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
					fieldLabel : '充值金额' + Ext.ux.txtFormat.xh,
					validator : function(v){
						if(v > 0 && v <= 99999){
							return true;
						}else{
							return '单次充值金额在 0 - 9999 之间.';
						}
					},
					listeners : {
						render : function(thiz){
							Ext.getDom(thiz.getId()).onkeyup = function(){
								if(thiz.getRawValue() != '' && rechargeOperateData != null){
									if(!thiz.isValid()){
										thiz.setValue(thiz.getRawValue().substring(0, 5));
										return;
									}
									var rm = thiz.getValue();
									var pmm = Ext.getCmp('rd_numPayMannerMoney');
									var gm = Ext.getCmp('rd_numGiftMoney');
									var gp = Ext.getCmp('rd_numGiftPoint');
									var tempMT = rechargeOperateData.root[0].memberType;
									if(pmm.getRawValue() == '' || pmm.getValue() == 0 || pmm.getValue() < rm){
										pmm.setValue(rm);
									}
									gm.setValue(parseInt(rm * Math.abs((tempMT.chargeRate - 1))));
									gp.setValue(parseInt(rm * tempMT.exchangeRate));
								}
							};
						}
					}
				}]
			}, {
				items : [{
					id : 'rd_numPayMannerMoney',
					fieldLabel : '收款金额' + Ext.ux.txtFormat.xh
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
					id : 'rd_comboPayManner',
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
					id : 'rd_txtMemberCardComment',
					fieldLabel : '备注',
					width : 520
				}]
			}, {
				columnWidth : 1,
				bodyStyle : 'font-size:20px;font-weight:bold;color:#15428B;text-align:center;',
				html : '<hr style="color:#DDD"/>会员资料<hr style="color:#DDD"/>'
			}, {
				items : [{
					id : 'rd_numMmeberID',
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

function rechargeLoadMemberData(){
	var cardAlias = Ext.getCmp('numMemberCardAliasForRecharge');
	if(!cardAlias.isValid()){
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
			pin : pin,
			restaurantID : restaurantID,
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
	var memberCardAliasForRecharge = Ext.getCmp('numMemberCardAliasForRecharge');
	var totalBalance = Ext.getCmp('rd_numTotalBalance');
	var baseBalance = Ext.getCmp('rd_numBaseBalance');
	var extraBalance = Ext.getCmp('rd_numExtraBalance');
	var totalPoint = Ext.getCmp('rd_numTotalPoint');
	var memberID = Ext.getCmp('rd_numMmeberID');
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
	
	data = data == null || typeof data == 'undefined' ? {} : data;
	var memberCard = typeof data['memberCard'] == 'undefined' ? {} : data['memberCard'];
	var client = typeof data['client'] == 'undefined' ? {} : data['client'];
	var clientType = typeof data['clientType'] == 'undefined' ? {} : data['clientType'];
	
	memberCardAliasForRecharge.setValue(memberCard['aliasID']);
	totalBalance.setValue(data['totalBalance']);
	baseBalance.setValue(data['baseBalance']);
	extraBalance.setValue(data['extraBalance']);
	totalPoint.setValue(data['point']);
	memberID.setValue(client['clientID']);
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
}

function rechargeControlCenter(_c){
	alert('夸页面调用充值方法')
}
