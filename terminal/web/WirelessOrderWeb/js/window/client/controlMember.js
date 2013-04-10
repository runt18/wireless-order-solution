var cm_obj = {
	operation : {'insert':'INSERT', 'update':'UPDATE', 'delete':'DELETE', 'get':'GET', 'set':'SET' },
	data : {},
	ctSelect : {
		radioBJM : {id:'radioBJM', value:1},
		radioXJ : {id:'radioXJ', value : 2},
		radioBD : {id : 'radioBD',value : 3},
		idList : []
	},
	isRender : false
};
cm_obj.ctSelect.idList = [cm_obj.ctSelect.radioBJM.id, cm_obj.ctSelect.radioXJ.id, cm_obj.ctSelect.radioBD.id];
function cm_isRender(){
	// 返回页面渲染状态 , true:渲染完成 - false:正在渲染
	return cm_obj.isRender;
}
function cm_checkSelect(c){
	c = c == null || typeof c == 'undefined' ? {} : c;
	var eventType = typeof(c.event), event;
	if(c.event == null || eventType == 'undefined'){
		/**
		 * 获取选择
		 */
		for(var i = 0; i < cm_obj.ctSelect.idList.length; i++){
			var temp = Ext.getCmp(cm_obj.ctSelect.idList[i]);
			if(temp.getValue()){
				return temp;
			}
		}
	}else{
		if(eventType == 'string'){
			event = c.event;
		}else if(eventType == 'object'){
			event = c.event.getId();
		}else{
			return undefined;
		}
		/**
		 * 切换选择
		 */
		for(var i = 0; i < cm_obj.ctSelect.idList.length; i++){
			if(cm_obj.ctSelect.idList[i] != event)
				Ext.getCmp(cm_obj.ctSelect.idList[i]).setValue(false);
		}
		
		var btnBindClient = Ext.getCmp('cm_btnBindClient');
		var numberClientID = Ext.getCmp('cm_numberClientID');
		var memberBasicPanel = Ext.getCmp('panelControlMemberContent');
		var clientStatus = false;
		
		if(event == cm_obj.ctSelect.radioBJM.id){
			// 不记名
			btnBindClient.setVisible(false);
			clientStatus = true;
		}else if(event == cm_obj.ctSelect.radioXJ.id){
			// 新建
			btnBindClient.setVisible(false);
			clientStatus = false;
		}else if(event == cm_obj.ctSelect.radioBD.id){
			// 绑定
			btnBindClient.setVisible(true);
			clientStatus = true;
		}
		//
		for(var i = 0; i < memberBasicPanel.items.length; i++){
			var item = memberBasicPanel.items.get(i).items.get(0);
			if(typeof item != 'undefined' && item.clientMsg == true){
				item.setDisabled(false);
				item.setValue();
				item.clearInvalid();
				item.setDisabled(clientStatus);
			}
		}
		numberClientID.setDisabled(false);
		numberClientID.setValue();
		numberClientID.setDisabled(true);
		// 加载原数据
		if(typeof c.data != 'undefined'){
			var client = typeof c.data.client != 'undefined' ? c.data.client : {};
			if((event == cm_obj.ctSelect.radioBJM.id && eval(client.level == 1))
					|| (event == cm_obj.ctSelect.radioBD.id && eval(client.level == 0))){
				var level = client.level;
				client.level = null;
				cm_operationClientDataToMember({
					type : cm_obj.operation['set'],
					data : client
				});
				client.level = level;
			}
		}
	}
};	

Ext.onReady(function(){
	var memeberCardAliasID = {
		xtype : 'numberfield',
		id : 'cm_numberMemberCardAliasID',
		inputType : 'password',
		fieldLabel : '请刷卡' + Ext.ux.txtFormat.xh,
		disabled : false,
		style : 'font-weight: bold; color: #FF0000;',
		maxLength : 10,
		maxLengthText : '请输入10位会员卡号',
		minLength : 10,
		minLengthText : '请输入10位会员卡号',
		width : 315,
		allowBlank : false,
		blankText : '会员卡不能为空.',
		setHideValue : function(val){
			var hv = null;
			if(val.length > 0){
				alert(/^[*?0-9]{1,10}$/.test(val));
	//			if(/^\d{1,10}$/.test(val)){
				if(/^[*?0-9]{1,10}$/.test(val)){
					hv = val;
					var display = '', di = val.length < 6 ? val.length : 6;
					for(var i = 0; i < di; i++){
						display += '*';
					}
					if(val.length > 6){
						display += (val.substring(5, val.length));
					}
					this.setValue(display);
				}else{
					hv = val.substring(0, val.length - 1);
					this.setValue(hv);
				}
			}
			this.hideValue = hv;
		},
		getHideValue : function(val){
			return this.hideValue;
		}
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
				disabled : true,
				width : 110
			}
		},
		items : [{
			columnWidth : .66,
			items : [memeberCardAliasID]
		}, {
			items : [{
				disabled : false,
				xtype : 'combo',
				id : 'cm_comboMemberStatus',
				fieldLabel : '使用状态' + Ext.ux.txtFormat.xh,
				readOnly : true,
				forceSelection : true,
				value : 0,
				store : new Ext.data.SimpleStore({
					fields : ['value', 'text'],
					data : [[0, '正常'], [1, '冻结']]
				}),
				valueField : 'value',
				displayField : 'text',
				typeAhead : true,
				mode : 'local',
				triggerAction : 'all',
				selectOnFocus : true,
				allowBlank : false,
				blankText : '会员卡状态为空.'
			}]
		}, {
			xtype : 'panel',
			columnWidth : 1
		},{
			items : [{
				xtype : 'hidden',
				id : 'cm_numberMemberID',
				fieldLabel : '会员编号'
			}]
		}, {
			items : [{
				disabled : false,
				xtype : 'combo',
				id : 'cm_comboMemberType',
				fieldLabel : '会员类型' + Ext.ux.txtFormat.xh,
				readOnly : true,
				forceSelection : true,
				store : new Ext.data.JsonStore({
					url: '../../QueryMemberType.do?restaurantID=' + restaurantID,
					root : 'root',
					fields : ['typeID', 'name']
				}),
				valueField : 'typeID',
				displayField : 'name',
				typeAhead : true,
				mode : 'local',
				triggerAction : 'all',
				selectOnFocus : true,
				allowBlank : false,
				blankText : '会员类型不允许为空.',
				listeners : {
					render : function(thiz){
						thiz.store.load();
					}
				}
			}]
		}, {
			items : [{
				xtype : 'numberfield',
				id : 'cm_numberMmeberPoint',
				fieldLabel : '积分',
				value : 0.00
			}]
		}, {
			items : [{
				xtype : 'numberfield',
				id : 'cm_numberTotalBalance',
				fieldLabel : '总余额',
				value : 0.00
			}]
		}, {
			items : [{
				xtype : 'numberfield',
				id : 'cm_numberBaseBalance',
				fieldLabel : '基础余额',
				value : 0.00
			}]
		}, {
			items : [{
				xtype : 'numberfield',
				id : 'cm_numberExtraBalance',
				fieldLabel : '赠送余额',
				value : 0.00
			}]
		}, {
			items : [{
				xtype : 'textfield',
				id : 'cm_txtLastStaff',
				fieldLabel : '最后操作人'
			}]
		}, {
			items : [{
				xtype : 'textfield',
				id : 'cm_txtLastModDate',
				fieldLabel : '最后操作时间'
			}]
		}, {
			columnWidth : 1,
			items : [{
				xtype : 'textfield',
				id : 'cm_txtOperationComment',
				fieldLabel : '最后操作备注',
				width : 520
			}]
		}, {
			xtype : 'panel',
			columnWidth : 1,
			html : '<hr style="color:#DDD"/>'
		}, {
			xtype : 'panel',
			columnWidth : .15,
			html : '&nbsp;'
		}, {
			columnWidth : .25,
			labelWidth : 65,
			items : [{
				xtype : 'radio',
				id : cm_obj.ctSelect.radioBJM.id,
				disabled : false,
				inputValue : 1,
				width : 20,
				fieldLabel : '不记名客户',
				checked : true,
				listeners : {
					resize : function(e){
						Ext.ux.checkPaddingTop(e);
					},
					check : function(e){
						if(e.getValue()){
							cm_checkSelect({
								event : e,
								data : cm_obj.data
							});
						}
					}
				}
			}]
		}, {
			columnWidth : .25,
			labelWidth : 80,
			items : [{
				xtype : 'radio',
				id : cm_obj.ctSelect.radioXJ.id,
				disabled : false,
				inputValue : 0,
				width : 30,
				fieldLabel : '新建客户资料',
				listeners : {
					resize : function(e){
						Ext.ux.checkPaddingTop(e);
					},
					check : function(e){
						if(e.getValue()){
							cm_checkSelect({
								event : e,
								data : cm_obj.data
							});
						}
					}
				}
			}]
		}, {
			columnWidth : .20,
			labelWidth : 80,
			items : [{
				xtype : 'radio',
				id : cm_obj.ctSelect.radioBD.id,
				disabled : false,
				inputValue : 0,
				width : 30,
				fieldLabel : '绑定现有客户',
				listeners : {
					resize : function(e){
						Ext.ux.checkPaddingTop(e);
					},
					check : function(e){
						if(e.getValue()){
							cm_checkSelect({
								event : e,
								data : cm_obj.data
							});
						}
					}
				}
			}]
		}, 
		{
			xtype : 'panel',
			columnWidth : .15,
			items : [{
				xtype : 'button',
				id : 'cm_btnBindClient',
				text : '绑定',
				hidden : true,
				disabled : false,
				handler : function(){
					cm_searchClientAndBindMsg();
				}
			}]
		},
		{
			xtype : 'panel',
			columnWidth : 1,
			html : '<hr style="color:#DDD"/>'
		}, {
			items : [{
				xtype : 'hidden',
				id : 'cm_numberClientID',
				fieldLabel : '客户编号'
			}]
		}, {
			items : [{
				clientMsg : true,
				id : 'cm_txtClientName',
				fieldLabel : '客户名称' + Ext.ux.txtFormat.xh,
				allowBlank : false,
				blankText : '客户名称不能为空.'
			}]
		}, {
			items : [getClientTypeTrigger({
				moption : {
					clientMsg : true,
				},
				id : 'cm_tirggerClietnTypeByClient',
				fieldLabel : '客户类别' + Ext.ux.txtFormat.xh,
				allowBlank : false,
				blankText : '客户类别不能为空.',
				rootVisible : false
			})]
		}, {
			items : [{
				clientMsg : true,
				xtype : 'combo',
				id : 'cm_comboClientSex',
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
				clientMsg : true,
				id : 'cm_txtClientMobile',
				fieldLabel : '手机' + Ext.ux.txtFormat.xh,
				allowBlank : false,
				regex : Ext.ux.RegText.phone.reg,
				regexText : Ext.ux.RegText.phone.error
			}]
		}, {
			items : [{
				clientMsg : true,
				id : 'cm_txtClientTele',
				fieldLabel : '电话'
			}]
		}, {
			items : [{
				clientMsg : true,
				xtype : 'datefield',
				id : 'cm_dateClientBirthday',
				fieldLabel : '生日',
				format : 'Y-m-d'
			}]
		}, {
			items : [{
				clientMsg : true,
				id : 'cm_txtClientIDCard',
				fieldLabel : '身份证'
			}]
		}, {
			items : [{
				clientMsg : true,
				id : 'cm_txtClientCompany',
				fieldLabel : '公司'
			}]
		}, {
			items : [{
				clientMsg : true,
				id : 'cm_txtClientTastePref',
				fieldLabel : '口味'
			}]
		}, {
			items : [{
				clientMsg : true,
				id : 'cm_txtClientTaboo',
				fieldLabel : '忌讳'
			}]
		}, {
			columnWidth : 1,
			items : [{
				clientMsg : true,
				id : 'cm_txtClientContactAddress',
				fieldLabel : '联系地址',
				width : 520
			}]
		}, {
			columnWidth : 1,
			items : [{
				clientMsg : true,
				id : 'cm_txtClientComment',
				fieldLabel : '备注',
				width : 520,
				listeners : {
					resize : function(){
						cm_obj.isRender = true;
					}
				}
			}]
		}]
	});
	
});

var cm_searchClientAndBindMsgWin;
function cm_searchClientAndBindMsg(){
	if(!cm_searchClientAndBindMsgWin){
		cm_searchClientAndBindMsgWin = new Ext.Window({
			title : '选择客户资料',
			id : 'cm_searchClientAndBindMsgWin',
			closable : false,
			modal : true,
			resizable : false,
			layout : 'fit',
			height : 430,
			width : 800,
			bbar : ['->', {
				text : '绑定',
				id : 'btnFindClientAndBind',
				iconCls : 'btn_save',
				handler : function(e){
					
				}
			}, {
				text : '关闭',
				iconCls : 'btn_close',
				handler : function(e){
					cm_searchClientAndBindMsgWin.hide();
				}
			}],
			listeners : {
				hide : function(thiz){
					thiz.body.update('');
				},
				show : function(thiz){
					thiz.center();
					thiz.load({
						url : '../window/client/searchClient.jsp',
						scripts : true,
						params : {
							callback : 'cm_searchClientCallback'
						}
					});
				}
			},
			keys : [{
				key : Ext.EventObject.ESC,
				scope : this,
				fn : function(){
					cm_searchClientAndBindMsgWin.hide();
				}
			}]
		});
	}
	cm_searchClientAndBindMsgWin.show();
}
/**
 * 
 * @param data
 */
function cm_searchClientCallback(data){
	cm_searchClientAndBindMsgWin.hide();
	cm_operationClientDataToMember({
		type : cm_obj.operation['set'],
		data : data
	});
}

function cm_operationMemberData(c){
	if(c == null || c.type == null || typeof c.type == 'undefined')
		return;
	var data = {};
	var memberID = Ext.getCmp('cm_numberMemberID');
	var memberCardAliasID = Ext.getCmp('cm_numberMemberCardAliasID');
	var memberType = Ext.getCmp('cm_comboMemberType');
	var point = Ext.getCmp('cm_numberMmeberPoint');
	var totalBalance = Ext.getCmp('cm_numberTotalBalance');
	var baseBalance = Ext.getCmp('cm_numberBaseBalance');
	var extraBalance = Ext.getCmp('cm_numberExtraBalance');
	var lastStaff = Ext.getCmp('cm_txtLastStaff');
	var lastModDate = Ext.getCmp('cm_txtLastModDate');
	var comment = Ext.getCmp('cm_txtOperationComment');
	var status = Ext.getCmp('cm_comboMemberStatus');
	if(c.type.toUpperCase() == cm_obj.operation['set'].toUpperCase()){
		data = c.data == null || typeof c.data == 'undefined' ? {} : c.data;
		memberID.setValue(data['id']);
		memberCardAliasID.setValue(data['memberCard.aliasID']);
		point.setValue(data['point']);
		totalBalance.setValue(data['totalBalance']);
		baseBalance.setValue(data['baseBalance']);
		extraBalance.setValue(data['extraBalance']);
		lastStaff.setValue(data['staff.name']);
		lastModDate.setValue(data['lastModDateFormat']);
		comment.setValue(data['comment']);
		status.setValue(typeof data['statusValue'] == 'undefined' ? 0 : data['statusValue']);
		
		if(typeof data['memberTypeID'] != 'undefined'){
			var task = {
				run: function(){
					if(memberType.store.getCount() > 0){
						memberType.setValue(data['memberTypeID']);
						Ext.TaskMgr.stop(this);
					}
				},
				interval: 250 
			};
			Ext.TaskMgr.start(task);
		}else{
			memberType.setValue(data['memberTypeID']);
		}
	}else if(c.type.toUpperCase() == cm_obj.operation['get'].toUpperCase()){
		data = {
			id : memberID.getValue(),
			memberType : {
				typeID : memberType.getValue()
			},
			memberCard : {
				aliasID : memberCardAliasID.getValue()
			},
			status : status.getValue()
		};
		c.data = data;
	}
	memberCardAliasID.clearInvalid();
	memberType.clearInvalid();
	status.clearInvalid();
	return c;
};
function cm_operationClientDataToMember(c){
	if(c == null || c.type == null || typeof c.type == 'undefined')
		return;
	var data = {};
	var clientID = Ext.getCmp('cm_numberClientID');
	var clientName = Ext.getCmp('cm_txtClientName');
	var clientType = Ext.getCmp('cm_tirggerClietnTypeByClient');
	var clientSex = Ext.getCmp('cm_comboClientSex');
	var clientMobile = Ext.getCmp('cm_txtClientMobile');
	var clientTele = Ext.getCmp('cm_txtClientTele');
	var clientBirthday = Ext.getCmp('cm_dateClientBirthday');
	var clietnIDCard = Ext.getCmp('cm_txtClientIDCard');
	var clientCompany = Ext.getCmp('cm_txtClientCompany');
	var clientTastePref = Ext.getCmp('cm_txtClientTastePref');
	var clietTaboo = Ext.getCmp('cm_txtClientTaboo');
	var clientContactAddress = Ext.getCmp('cm_txtClientContactAddress');
	var clientComment = Ext.getCmp('cm_txtClientComment');
	if(c.type.toUpperCase() == cm_obj.operation['set'].toUpperCase()){
		data = c.data == null || typeof c.data == 'undefined' ? {} : c.data;
		if(eval(data['level'] == 0)){
			Ext.getCmp(mObj.ctSelect.radioBD.id).setValue(true);
		}else if(eval(data['level'] == 1)){
			Ext.getCmp(mObj.ctSelect.radioBJM.id).setValue(true);
		}
		clientID.setValue(data['clientID']);
		clientName.setValue(data['name']);
		clientSex.setValue(data['sex']);
		clientMobile.setValue(data['mobile']);
		clientTele.setValue(data['tele']);
		clietnIDCard.setValue(data['IDCard']);
		clientCompany.setValue(data['company']);
		clientTastePref.setValue(data['tastePref']);
		clietTaboo.setValue(data['taboo']);
		clientContactAddress.setValue(data['contactAddress']);
		clientComment.setValue(data['comment']);
		clientBirthday.setValue(typeof data['birthday'] == 'undefined' ? undefined : new Date(data['birthday']));
//		clientType.setValue(data['clientTypeID']);
		if(typeof data['clientTypeID'] != 'undefined'){
			var task = {
				run: function(){
					if(clientType.isLoad && clientType.hasNode()){
						clientType.setValue(data['clientTypeID']);
						Ext.TaskMgr.stop(this);
					}
				},
				interval: 250 
			};
			Ext.TaskMgr.start(task);
		}else{
			clientType.setValue(data['clientTypeID']);
		}
	}else if(c.type.toUpperCase() == cm_obj.operation['get'].toUpperCase()){
		data = {
			clientID : clientID.getValue(),
			name : clientName.getValue(),
			clientType : {
				typeID : clientType.getValue()
			},
			sex : clientSex.getValue(),
			mobile : clientMobile.getValue(),
			tele : clientTele.getValue(),
			birthday : clientBirthday.getValue() != '' ? clientBirthday.getValue().getTime() : '',
			IDCard : clietnIDCard.getValue(),
			company : clientCompany.getValue(),
			tastePref : clientTastePref.getValue(),
			taboo : clietTaboo.getValue(),
			contactAddress : clientContactAddress.getValue(),
			comment : clientComment.getValue()
		};
		c.data = data;
	}
	clientName.clearInvalid();
	clientType.clearInvalid();
	return c;
};
function cm_operationMemberBasicMsg(c){
	if(c == null || c.type == null || typeof c.type == 'undefined')
		return;
	var data = {};
	if(c.type.toUpperCase() == cm_obj.operation['set'].toUpperCase()){
		cm_obj.data = c.data;
		data = c.data;
		cm_operationMemberData({
			type : c.type,
			data : data
		});
		cm_operationClientDataToMember({
			type : c.type,
			data : data['client']
		});
	}else if(c.type.toUpperCase() == cm_obj.operation['get'].toUpperCase()){
		data = cm_operationMemberData({
			type : c.type
		}).data;
		data.client = cm_operationClientDataToMember({
			type : c.type
		}).data;
		c.data = data;
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
	var actionURL;
	if(c.type.toUpperCase() == cm_obj.operation['insert'].toUpperCase()){
		actionURL = '../../InsertMember.do';
	}else if(c.type.toUpperCase() == cm_obj.operation['update'].toUpperCase()){
		actionURL = '../../UpdateMember.do';
	}else{
		return;
	}
	
	var memberStatus = Ext.getCmp('cm_comboMemberStatus');
	var membetType = Ext.getCmp('cm_comboMemberType');
	var memberCardAliasID = Ext.getCmp('cm_numberMemberCardAliasID');
	var clientName = Ext.getCmp('cm_txtClientName');
	var clientType = Ext.getCmp('cm_tirggerClietnTypeByClient');
	var clientMobile = Ext.getCmp('cm_txtClientMobile');
	var clientSex = Ext.getCmp('cm_comboClientSex');
	
	if(!memberStatus.isValid() || !membetType.isValid() || !memberCardAliasID.isValid()
			|| !clientName.isValid() || !clientType.isValid()
			|| !clientMobile.isValid() || !clientSex.isValid()){
		return;
	}
	var memberData = cm_operationMemberBasicMsg({type : cm_obj.operation['get']}).data;
	var params = '', clientData = {}, clientLevel = cm_checkSelect();
	if(clientLevel.getId() == cm_obj.ctSelect.radioBJM.id){
		clientData = {};
	}else{
		clientData = memberData.client;
		if(clientLevel.getId() == cm_obj.ctSelect.radioBD.id){
			if(clientData.clientID == ''){
				Ext.example.msg('提示', '请绑定客户信息.');
				return;
			}
		}
	}
	clientData.level = clientLevel.inputValue;
	
	if(c.type.toUpperCase() == cm_obj.operation['update'].toUpperCase()){
		if(clientData.level == 1){
			if(typeof c.data != 'undefined'){
				if(c.data.client.level == 0){
					if(c.data.totalBalance != 0 || c.data.point != 0){
						Ext.example.msg('提示', '该会员还有余额, 不允许绑定匿名用户.');
						return;
					}
				}
			}else{
				Ext.example.msg('提示', '读取会员原信息失败, 请尝试刷新页面后重新操作.');
				return;
			}
		}
	}
	memberData.restaurantID = restaurantID;
	memberData.client = clientData;
	memberData.staff = {
		terminal : {
			pin : pin
		}
	};
	delete memberData['status'];
	params = Ext.encode(memberData);
	if(typeof c.setButtonStatus == 'function'){
		c.setButtonStatus(true);
	}
	Ext.Ajax.request({
		url : actionURL,
		params : {
			pin : pin,
			restaurantID : restaurantID,
			status : memberStatus.getValue(),
			params : params
		},
		success : function(res, opt){
			c.setButtonStatus(false);
			var jr = Ext.decode(res.responseText);
			if(typeof c.callback == 'function'){
				c.callback(memberData, c, jr);
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
