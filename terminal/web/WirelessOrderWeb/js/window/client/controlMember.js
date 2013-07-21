Ext.onReady(function(){
	var memeberCardAliasID = {
		xtype : 'numberfield',
		id : 'cm_numberMemberCard',
		//inputType : 'password',
		fieldLabel : '会员卡',
		disabled : false,
		maxLength : 10,
		maxLengthText : '请输入10位会员卡号',
		minLength : 10,
		minLengthText : '请输入10位会员卡号'
	};
	
	new Ext.Panel({
		id : 'panelControlMemberContent',
		renderTo : 'divControlMemberContent',
		frame : true,
		border : false,
		layout : 'column',
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
				readOnly : true,
				forceSelection : true,
				store : new Ext.data.JsonStore({
					url: '../../QueryMemberType.do?dataSource=normal&restaurantID=' + restaurantID,
					root : 'root',
					fields : ['id', 'name', 'attributeValue']
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
					render : function(thiz){
						thiz.store.load();
					}
				}
			}]
		}, {
			items : [{
				xtype : 'combo',
				id : 'cm_comboMemberSex',
				fieldLabel : '性别' + Ext.ux.txtFormat.xh,
				readOnly : true,
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
		}, {
			items : [{
				id : 'cm_txtMemberTele',
				fieldLabel : '电话'
			}]
		}, {
			items : [{
				id : 'cm_txtMemberIDCard',
				fieldLabel : '身份证'
			}]
		}, {
			items : [{
				id : 'cm_txtMemberCompany',
				fieldLabel : '公司'
			}]
		}, {
			items : [{
				id : 'cm_txtMemberTastePref',
				fieldLabel : '口味'
			}]
		}, {
			items : [{
				id : 'cm_txtMemberTaboo',
				fieldLabel : '忌讳'
			}]
		}, {
			columnWidth : 1,
			items : [{
				id : 'cm_txtMemberContactAddress',
				fieldLabel : '联系地址',
				width : 520
			}]
		}, {
			columnWidth : 1,
			items : [{
				id : 'cm_txtMemberComment',
				fieldLabel : '备注',
				width : 520
			}]
		}, {
			columnWidth : 1,
			xtype : 'panel',
			html : '<hr style="width: 100%; color: #DDD;">'
		}, {
			items : [{
				xtype : 'numberfield',
				id : 'cm_numberTotalBalance',
				fieldLabel : '总余额',
				disabled : true,
				value : 0.00
			}]
		}, {
			items : [{
				xtype : 'numberfield',
				id : 'cm_numberBaseBalance',
				fieldLabel : '基础余额',
				disabled : true,
				value : 0.00
			}]
		}, {
			items : [{
				xtype : 'numberfield',
				id : 'cm_numberExtraBalance',
				fieldLabel : '赠送余额',
				disabled : true,
				value : 0.00
			}]
		}, {
			items : [{
				xtype : 'numberfield',
				id : 'cm_numberUsedBalance',
				fieldLabel : '累计消费',
				disabled : true,
				value : 0.00
			}]
		}, {
			items : [{
				xtype : 'numberfield',
				id : 'cm_numberUserPoint',
				fieldLabel : '累计积分',
				disabled : true,
				value : 0.00
			}]
		}, {
			items : [{
				xtype : 'numberfield',
				id : 'cm_numberMmeberPoint',
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
	var memberID = Ext.getCmp('cm_numberMemberId');
	var name = Ext.getCmp('cm_txtMemberName');
	var mobile = Ext.getCmp('cm_txtMemberMobile');
	var memberCard = Ext.getCmp('cm_numberMemberCard');
	var memberType = Ext.getCmp('cm_comboMemberType');
	var sex = Ext.getCmp('cm_comboMemberSex');
	var birthday = Ext.getCmp('cm_dateMemberBirthday');
	var tele = Ext.getCmp('cm_txtMemberTele');
	var idCard = Ext.getCmp('cm_txtMemberIDCard');
	var company = Ext.getCmp('cm_txtMemberCompany');
	var tastePref = Ext.getCmp('cm_txtMemberTastePref');
	var taboo = Ext.getCmp('cm_txtMemberTaboo');
	var addr = Ext.getCmp('cm_txtMemberContactAddress');
	var comment = Ext.getCmp('cm_txtMemberComment');
	
	var totalBalance = Ext.getCmp('cm_numberTotalBalance');
	var baseBalance = Ext.getCmp('cm_numberBaseBalance');
	var extraBalance = Ext.getCmp('cm_numberExtraBalance');
	var usedBalance = Ext.getCmp('cm_numberUsedBalance');
	var point = Ext.getCmp('cm_numberMmeberPoint');
	var usedPoint = Ext.getCmp('cm_numberUserPoint');
	
	if(c.type.toUpperCase() == Ext.ux.otype['set'].toUpperCase()){
		data = c.data == null || typeof c.data == 'undefined' ? {} : c.data;
		memberID.setValue(data['id']);
		name.setValue(data['name']);
		mobile.setValue(data['mobile']);
		memberCard.setValue(data['memberCard']);
		sex.setValue(typeof data['sexValue'] == 'undefined' ? 0 : data['sexValue']);
		if(eval(data['birthday'] > 0)){
			birthday.setValue(new Date(eval(data['birthday'])));
		}else{
			birthday.setValue();
		}
		tele.setValue(data['tele']);
		idCard.setValue(data['idCard']);
		company.setValue(data['company']);
		tastePref.setValue(data['tastePref']);
		taboo.setValue(data['taboo']);
		addr.setValue(data['contactAddress']);
		comment.setValue(data['comment']);
		
		totalBalance.setValue(data['totalBalance']);
		baseBalance.setValue(data['baseBalance']);
		extraBalance.setValue(data['extraBalance']);
		usedBalance.setValue(data['usedBalance']);
		point.setValue(data['point']);
		usedPoint.setValue(data['usedPoint']);
		
		if(typeof data['memberType'] != 'undefined'){
			var task = {
				run: function(){
					if(memberType.store.getCount() > 0){
						memberType.setValue(data['memberType']['id']);
						Ext.TaskMgr.stop(this);
					}
				},
				interval: 250 
			};
			Ext.TaskMgr.start(task);
		}else{
			memberType.setValue();
		}
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
	
	if(cm_obj.otype.toLowerCase() == Ext.ux.otype['update'].toLowerCase()){
		// 验证旧类型为充值属性
		if(c.data['memberType']['attributeValue'] == 0){
			for(var i = 0; i < membetType.store.getCount(); i++){
				if(membetType.store.getAt(i).get('id') == membetType.getValue()){
					if(membetType.store.getAt(i).get('attributeValue') != c.data['memberType']['attributeValue']){
						if(c.data['totalBalance'] > 0){
							Ext.example.msg('提示', '该会员还有余额, 不允许设置为优惠属性的类型会员');
							return;
						}
					}
					break;
				}
			}
		}
	}
	
	if(!memberName.isValid() || !memberMobile.isValid() 
		|| !membetType.isValid() || !memberSex.isValid()){
		return;
	}
	
	if(typeof c.setButtonStatus == 'function'){
		c.setButtonStatus(true);
	}
	Ext.Ajax.request({
		url : '../../OperateMember.do',
		params : {
			dataSource : cm_obj.otype.toLowerCase(),
			pin : pin,
			id : Ext.getCmp('cm_numberMemberId').getValue(),
			name : memberName.getValue(),
			mobile : memberMobile.getValue(),
			memberTypeId : membetType.getValue(),
			sex : memberSex.getValue(),
			memberCard :Ext.getCmp('cm_numberMemberCard').getValue(),
			birthday : birthday.getValue() ? birthday.getValue().format('Y-m-d') : '',
			telt : Ext.getCmp('cm_txtMemberTele').getValue(),
			idCard : Ext.getCmp('cm_txtMemberIDCard').getValue(),
			company : Ext.getCmp('cm_txtMemberCompany').getValue(),
			tastePref : Ext.getCmp('cm_txtMemberTastePref').getValue(),
			taboo : Ext.getCmp('cm_txtMemberTaboo').getValue(),
			addr : Ext.getCmp('cm_txtMemberContactAddress').getValue(),
			comment : Ext.getCmp('cm_txtMemberComment').getValue()
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
