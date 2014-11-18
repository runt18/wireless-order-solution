Ext.onReady(function(){
	var memeberCardAliasID = {
		xtype : 'numberfield',
		id : 'cm_numberMemberCard',
		//inputType : 'password',
		fieldLabel : '会员卡',
		disabled : false
	};
	
	new Ext.Panel({
		id : 'panelControlMemberContent',
		renderTo : 'divControlMemberContent',
		frame : true,
		border : false,
		layout : 'column',
		width : 670,
		height : 185,
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
				id : 'cm_numberMemberId',
				fieldLabel : '会员编号'
			}]
		}, {
			items : [{
				id : 'cm_txtMemberName',
				fieldLabel : '名称' + Ext.ux.txtFormat.xh,
				allowBlank : false,
				blankText : '名称不能为空.'
			}]
		}, {
			items : [{
				id : 'cm_txtMemberMobile',
				style : 'font-weight: bold; color: #FF0000;',
				fieldLabel : '手机' + Ext.ux.txtFormat.xh,
				allowBlank : false,
				regex : Ext.ux.RegText.phone.reg,
				regexText : Ext.ux.RegText.phone.error
			}]
		}, {
			items : [memeberCardAliasID]
		}, {
			items : [{
				disabled : false,
				xtype : 'combo',
				id : 'cm_comboMemberType',
				fieldLabel : '会员类型' + Ext.ux.txtFormat.xh,
				readOnly : false,
				forceSelection : true,
				store : new Ext.data.JsonStore({
//					url: '../../QueryMemberType.do?dataSource=normal',
//					root : 'root',
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
						var firstCharge = Ext.getCmp('cm_numFirstCharge');
						var firstActualCharge = Ext.getCmp('cm_numFirstActualCharge');
						var rechargeType = Ext.getCmp('rd_comboFirstRechargeType');
						if(cm_obj.otype.toLowerCase() == Ext.ux.otype['insert'].toLowerCase() && record.get('attributeValue') == 0){
							firstCharge.show();
							firstCharge.getEl().up('.x-form-item').setDisplayed(true);	
							firstActualCharge.show();
							firstActualCharge.getEl().up('.x-form-item').setDisplayed(true);
							rechargeType.show();
							rechargeType.getEl().up('.x-form-item').setDisplayed(true);
							
							chargeRate = record.get('chargeRate');
							
							Ext.getCmp('chbPrintFirstRecharge').show();
							
							if(Ext.ux.smsModule)
								Ext.getCmp('chbSendFirstCharge').show();
							
						}else{
							firstCharge.hide();
							firstCharge.getEl().up('.x-form-item').setDisplayed(false);	
							firstActualCharge.hide();
							firstActualCharge.getEl().up('.x-form-item').setDisplayed(false);	
							rechargeType.hide();
							rechargeType.getEl().up('.x-form-item').setDisplayed(false);							
							
							Ext.getCmp('chbPrintFirstRecharge').hide();
							Ext.getCmp('chbSendFirstCharge').hide();
						}						
					}
				}
			}]
		}, {
			items : [{
				hidden : true,
				xtype : 'numberfield',
				id : 'cm_numFirstCharge',
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
								var pmm = Ext.getCmp('cm_numFirstActualCharge');
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
				id : 'cm_numFirstActualCharge',
				fieldLabel : '账户充额'
			}]
		}, 	{
			items : [{
				xtype : 'combo',
				id : 'rd_comboFirstRechargeType',
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
				id : 'cm_comboMemberSex',
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
				id : 'cm_dateMemberBirthday',
				fieldLabel : '生日',
				format : 'Y-m-d'
			}]
		}, 
/*		{
			items : [{
				id : 'cm_txtMemberTastePref',
				fieldLabel : '口味'
			}]
		}, {
			items : [{
				id : 'cm_txtMemberTaboo',
				fieldLabel : '忌讳'
			}]
		}, */
		{
			columnWidth : 1,
			items : [{
				id : 'cm_txtMemberContactAddress',
				fieldLabel : '联系地址',
				width : 535
			}]
		}
/*		, {
			columnWidth : 1,
			items : [{
				id : 'cm_txtMemberPublicComment',
				fieldLabel : '公有评论',
				width : 535
			}]
		}, {
			columnWidth : 1,
			items : [{
				id : 'cm_txtMemberPrivateComment',
				fieldLabel : '私有评论',
				width : 535
			}]
		}*/
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
			hidden : true,
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
		}]
	});
	
});

function cm_operationMemberData(c){
	if(c == null || c.type == null || typeof c.type == 'undefined')
		return;
		
	var data = {};
	var memberType = Ext.getCmp('cm_comboMemberType');
	var firstCharge = Ext.getCmp('cm_numFirstCharge');	
	var firstActualCharge = Ext.getCmp('cm_numFirstActualCharge');	
	var rechargeType = Ext.getCmp('rd_comboFirstRechargeType');
	var memberID = Ext.getCmp('cm_numberMemberId');
	var name = Ext.getCmp('cm_txtMemberName');
	var mobile = Ext.getCmp('cm_txtMemberMobile');
	var memberCard = Ext.getCmp('cm_numberMemberCard');
	var weixinCard = Ext.getCmp('cm_weixinMemberCard');

	
	var sex = Ext.getCmp('cm_comboMemberSex');
	var birthday = Ext.getCmp('cm_dateMemberBirthday');
	var addr = Ext.getCmp('cm_txtMemberContactAddress');
	
	var totalBalance = Ext.getCmp('cm_numberTotalBalance');
	var baseBalance = Ext.getCmp('cm_numberBaseBalance');
	var extraBalance = Ext.getCmp('cm_numberExtraBalance');
	var usedBalance = Ext.getCmp('cm_numberUsedBalance');
	var point = Ext.getCmp('cm_numberMmeberPoint');
	var usedPoint = Ext.getCmp('cm_numberUserPoint');
	
	
	
	firstCharge.setValue();
	firstActualCharge.setValue();
//	var publicComment = Ext.getCmp('cm_txtMemberPublicComment');
//	var privateComment = Ext.getCmp('cm_txtMemberPrivateComment');
	
	
	
	if(c.type.toUpperCase() == Ext.ux.otype['set'].toUpperCase()){
		firstCharge.getEl().up('.x-form-item').setDisplayed(false);
		firstActualCharge.getEl().up('.x-form-item').setDisplayed(false);
		rechargeType.getEl().up('.x-form-item').setDisplayed(false);
		
		memberType.store.loadData(c.data.memberTypeData);
		data = c.data == null || typeof c.data == 'undefined' ? {} : c.data;
		memberID.setValue(data['id']);
		name.setValue(data['name']);
		mobile.setValue(data['mobile']);
		memberCard.setValue(data['memberCard']);
		weixinCard.setValue(data['weixinCard']);
		sex.setValue(typeof data['sexValue'] == 'undefined' ? 0 : data['sexValue']);
		if(eval(data['birthday'] > 0)){
			birthday.setValue(new Date(eval(data['birthday'])));
		}else{
			birthday.setValue();
		}
		addr.setValue(data['contactAddress']);
		
		totalBalance.setValue(parseFloat(data['totalBalance']).toFixed(2));
		baseBalance.setValue(data['baseBalance']);
		extraBalance.setValue(data['extraBalance']);
		usedBalance.setValue(data['usedBalance']);
		point.setValue(data['point']);
		usedPoint.setValue(data['usedPoint']);
		
/*		if(!data['privateComment']){
			privateComment.setValue();
			privateComment.enable();
		}else{
			privateComment.setValue(data['privateComment'].comment);
			privateComment.disable();
		}
		
		if(!data['publicComment']){
			publicComment.setValue();
			publicComment.enable();
		}else{
			var comments = "";
			for (var i = 0; i < data['publicComment'].length; i++) {
				comments += data['publicComment'][i].comment + "  ";
			}
			publicComment.setValue(comments);
			publicComment.disable();
		}*/

		if(typeof data['memberType'] != 'undefined'){
			memberType.setValue(data['memberType']['id']);
		}else{
//			memberType.setValue();
			memberType.setValue(Ext.ux.getSelNode(memberTypeTree)?(Ext.ux.getSelNode(memberTypeTree).attributes.memberTypeId != '-1'?Ext.ux.getSelNode(memberTypeTree).attributes.memberTypeId:'') : '');
		}
		
/*		if(data['name']){
			publicComment.getEl().dom.readOnly = true;
			privateComment.getEl().dom.readOnly = true;
		}else{
			publicComment.getEl().dom.readOnly = false;
			privateComment.getEl().dom.readOnly = false;
		}*/
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
		if(!c.data.memberTypeData){
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
		
		cm_obj.data = c.data;
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
	var membetType = Ext.getCmp('cm_comboMemberType');
	var memberName = Ext.getCmp('cm_txtMemberName');
	var memberMobile = Ext.getCmp('cm_txtMemberMobile');
	var memberSex = Ext.getCmp('cm_comboMemberSex');
	var birthday = Ext.getCmp('cm_dateMemberBirthday');
	var firstCharge = Ext.getCmp('cm_numFirstCharge');
	var firstActualCharge = Ext.getCmp('cm_numFirstActualCharge');
	var rechargeType = Ext.getCmp('rd_comboFirstRechargeType');
	
	if(cm_obj.otype.toLowerCase() == Ext.ux.otype['insert'].toLowerCase()){
		if(!memberMobile.getValue() && !Ext.getCmp('cm_numberMemberCard').getValue()){
			Ext.example.msg('提示', '至少要输入手机或会员卡号');
			return;
		}
	}
	
	if(!memberName.isValid() || !membetType.isValid() || !memberSex.isValid()){
		return;
	}
	
	if(typeof c.setButtonStatus == 'function'){
		c.setButtonStatus(true);
	}
	Ext.Ajax.request({
		url : '../../OperateMember.do',                                                                            
		params : {
			dataSource : cm_obj.otype.toLowerCase(),
			id : Ext.getCmp('cm_numberMemberId').getValue(),
			name : memberName.getValue(),
			mobile : memberMobile.getValue(),
			memberTypeId : membetType.getValue(),
			sex : memberSex.getValue(),
			memberCard :Ext.getCmp('cm_numberMemberCard').getValue(),
			birthday : birthday.getValue() ? birthday.getValue().format('Y-m-d') : '',
			firstCharge : firstCharge.getValue(),
			firstActualCharge : firstActualCharge.getValue(),
			rechargeType : rechargeType.getValue(),
			isPrint : Ext.getCmp('chbPrintFirstRecharge').getValue(),
			addr : Ext.getCmp('cm_txtMemberContactAddress').getValue()
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
	Ext.getCmp('cm_txtMemberName').focus(true, 100);
}
