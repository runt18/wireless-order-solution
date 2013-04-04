/**************************************************/
memberCardAliasRenderer = function(v){
	return ('******' + v.substring(6, 10));
};
memberStatusRenderer = function(v){
	for(var i = 0; i < memberStatus.length; i++){
		if(eval(memberStatus[i][0] == v)){
			return memberStatus[i][1];
		}
	}
};
memberOperationRenderer = function(val, m, record){
	var renderText = '';
	renderText += '<a href="javascript:updateMemberHandler()">修改</a>';
	renderText += '&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;';
	renderText += '<a href="javascript:queryMemberOperationHandler()">消费记录</a>';
	
//	if(eval(record.get('client.clientTypeID') > 0)){
//		renderText += '&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;';
//		renderText += '<a href="javascript:changeMemberCardHandler()">换卡</a>';
//	}
	
//	if(eval(record.get('memberType.attributeValue') == 0) && eval(record.get('client.clientTypeID') > 0)){
//		renderText += '&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;';
//		renderText += '<a href="javascript:rechargeHandler()">充值</a>';
//	}
	
//		   + '&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;'
//		   + '<a href="javascript:deleteMemberHandler()">删除</a>'
	return renderText;
};

/**************************************************/
treeInit = function(){
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
			dataUrl : '../../QueryMemberTypeTree.do',
			baseParams : {
				restaurantID : restaurantID
			}
		}),
		root : new Ext.tree.AsyncTreeNode({
			text : '全部类型',
			leaf : false,
			border : true,
			expanded : true,
			MemberTypeID : -1,
			listeners : {
				load : function(thiz){
					memberTypeData.root = [];
					for(var i = 0; i < thiz.childNodes.length; i++){
						memberTypeData.root.push({
							memberTypeID : thiz.childNodes[i].attributes['memberTypeID'],
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

gridInit = function(){
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
					var status = Ext.getCmp('comboSearchValueByStatus');
					if(value == 0){
						text.setVisible(false);
						comboOperation.setVisible(false);
						number.setVisible(false);
						status.setVisible(false);
						mObj.searchValue = '';
						Ext.getCmp('btnSearchMember').handler();
					}else if(value == 1){
						text.setVisible(true);
						comboOperation.setVisible(false);
						number.setVisible(false);
						status.setVisible(false);
						text.setValue();
						mObj.searchValue = text.getId();
					}else if(value == 2 || value == 3 || value ==4){
						text.setVisible(false);
						comboOperation.setVisible(true);
						number.setVisible(true);
						status.setVisible(false);
						comboOperation.setValue(0);
						number.setValue();
						mObj.searchValue = {
							o : comboOperation.getId(),
							v : number.getId()
						};
					}else if(value == 5){
						text.setVisible(false);
						comboOperation.setVisible(false);
						number.setVisible(false);
						status.setVisible(true);
						status.setValue(0);
						mObj.searchValue = status.getId();
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
			hidden : true
		}, {
			disabled : false,
			xtype : 'combo',
			id : 'comboSearchValueByStatus',
			readOnly : true,
			forceSelection : true,
			value : 0,
			width : 80,
			store : new Ext.data.SimpleStore({
				fields : ['value', 'text'],
				data : memberStatus
			}),
			valueField : 'value',
			displayField : 'text',
			typeAhead : true,
			mode : 'local',
			triggerAction : 'all',
			selectOnFocus : true,
			hidden : true
		}, '->', {
			text : '搜索',
			id : 'btnSearchMember',
			iconCls : 'btn_search',
			handler : function(){
				var memberTypeNode = memberTypeTree.getSelectionModel().getSelectedNode();
				var searchType = Ext.getCmp('comboMemberSearchType');
				var st='', sv='', so='';
				st = searchType.getValue();
				
				if(st == 2 || st == 3 || st == 4){
					so = Ext.getCmp(mObj.searchValue.o).getValue();
					sv = Ext.getCmp(mObj.searchValue.v).getValue();
				}else{
					so = 0;
					sv = Ext.getCmp(mObj.searchValue);
					sv = typeof sv != 'undefined' ? sv.getValue() : '';
				}
				var params = {
					searchType : st,
					searchOperation : so,
					searchValue : sv
				};
				if(memberTypeNode != null){
					params.searcheMemberType = memberTypeNode.attributes.memberTypeID;
				}else{
					params.searcheMemberType = '';
				}
				var gs = memberBasicGrid.getStore();
				gs.baseParams['params'] = Ext.encode(params);
				gs.load({
					params : {
						start : 0,
						limit : 30
					}
				});
			}
		}, '-', {
			text : '重置',
			iconCls : 'btn_refresh',
			handler : function(e){
				Ext.getCmp('btnRefreshMemberType').handler();
				Ext.getCmp('btnSearchMember').handler();
				var st = Ext.getCmp('comboMemberSearchType');
				st.setValue(0);
				st.fireEvent('select', st, null, null);
			}
		}, '-', {
			text : '添加',
			iconCls : 'btn_add',
			handler : function(e){
				insertMemberHandler();
			}
		}, '-', {
			text : '修改',
			iconCls : 'btn_edit',
			handler : function(e){
				updateMemberHandler();
			}
		}, '-', {
			text : '删除',
			iconCls : 'btn_delete',
			handler : function(e){
				deleteMemberHandler();
			}
		}, '-', {
			text : '充值',
			iconCls : 'icon_tb_recharge',
			handler : function(e){
				rechargeHandler();
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
			['会员编号', 'id'],
			['会员卡号', 'memberCard.aliasID',,,'memberCardAliasRenderer'],
			['会员类型', 'memberType.name'],
			['客户名称', 'client.name'],
			['余额', 'totalBalance',,'right', 'Ext.ux.txtFormat.gridDou'],
			['积分', 'point',,'right'],
			['使用状态', 'statusValue',,'center', 'memberStatusRenderer'],
			['最后操作时间', 'lastModDateFormat', 130],
			['最后操作人', 'staff.name'],
			['操作', 'operation', 250, 'center', 'memberOperationRenderer']
		],
		['id', 'memberCard', 'memberCard.aliasID', 'memberType', 'memberTypeID', 'memberType.name', 'memberType', 'memberType.attributeValue',
		 'client', 'client.name', 'client.clientTypeID',
		 'tele', 'lastModDateFormat', 'staff', 'staff.name', 'statusValue', 'comment',
		 'totalBalance', 'baseBalance', 'extraBalance', 'point'],
		[['isPaging', true], ['restaurantID', restaurantID], ['pin', pin], ['dataSource', 'normal']],
		30,
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
		fn : function(){ 
			Ext.getCmp('btnSearchMember').handler();
		},
		scope : this 
	}];
};

winInit = function(){
	var memeberCardAliasID = {
		xtype : 'numberfield',
		id : 'numberMemberCardAliasID',
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
		blankText : '会员卡不能为空.',
		setHideValue : function(val){
			var hv = null;
			if(val.length > 0){
				alert(/^[*?0-9]{1,10}$/.test(val));
//				if(/^\d{1,10}$/.test(val)){
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
		},
		listeners : {
			render : function(e){
//				var dom = Ext.getDom(e.getId());
//				dom.maxLength = 10;
//				if(Ext.isIE){
//					dom.onpropertychange = function(){
//						var tv = Ext.util.Format.trim(dom.value);
//						e.setHideValue(tv);
//					};
//				}else{
//					dom.oninput = function(){
//						var tv = Ext.util.Format.trim(dom.value);
//						e.setHideValue(tv);
//					};
//				}
			}
		}
	};
	
	var memberBasicPanel = new Ext.Panel({
		id : 'memberBasicPanel',
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
				id : 'comboMemberStatus',
				fieldLabel : '使用状态' + Ext.ux.txtFormat.xh,
				readOnly : true,
				forceSelection : true,
				value : 0,
				store : new Ext.data.SimpleStore({
					fields : ['value', 'text'],
					data : memberStatus
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
				xtype : 'numberfield',
				id : 'numberMemberID',
				fieldLabel : '会员编号'
			}]
		}, {
			items : [{
				disabled : false,
				xtype : 'combo',
				id : 'comboMemberType',
				fieldLabel : '会员类型' + Ext.ux.txtFormat.xh,
				readOnly : true,
				forceSelection : true,
				store : new Ext.data.JsonStore({
					root : 'root',
					fields : ['memberTypeID', 'memberTypeName']
				}),
				valueField : 'memberTypeID',
				displayField : 'memberTypeName',
				typeAhead : true,
				mode : 'local',
				triggerAction : 'all',
				selectOnFocus : true,
				allowBlank : false,
				blankText : '会员卡类型为空.'
			}]
		}, {
			items : [{
				xtype : 'numberfield',
				id : 'numberMmeberPoint',
				fieldLabel : '积分',
				value : 0.00
			}]
		}, {
			items : [{
				xtype : 'numberfield',
				id : 'numberTotalBalance',
				fieldLabel : '总余额',
				value : 0.00
			}]
		}, {
			items : [{
				xtype : 'numberfield',
				id : 'numberBaseBalance',
				fieldLabel : '基础余额',
				value : 0.00
			}]
		}, {
			items : [{
				xtype : 'numberfield',
				id : 'numberExtraBalance',
				fieldLabel : '赠送余额',
				value : 0.00
			}]
		}, {
			items : [{
				xtype : 'textfield',
				id : 'txtLastStaff',
				fieldLabel : '最后操作人'
			}]
		}, {
			items : [{
				xtype : 'textfield',
				id : 'txtLastModDate',
				fieldLabel : '最后操作时间'
			}]
		}, {
			columnWidth : 1,
			items : [{
				xtype : 'textfield',
				id : 'txtOperationComment',
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
				id : mObj.ctSelect.radioBJM.id,
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
						if(e.getValue())
							checkSelect(e);
					}
				}
			}]
		}, {
			columnWidth : .25,
			labelWidth : 80,
			items : [{
				xtype : 'radio',
				id : mObj.ctSelect.radioXJ.id,
				disabled : false,
				inputValue : 0,
				width : 30,
				fieldLabel : '新建客户资料',
				listeners : {
					resize : function(e){
						Ext.ux.checkPaddingTop(e);
					},
					check : function(e){
						if(e.getValue())
							checkSelect(e);
					}
				}
			}]
		}, {
			columnWidth : .20,
			labelWidth : 80,
			items : [{
				xtype : 'radio',
				id : mObj.ctSelect.radioBD.id,
				disabled : false,
				inputValue : 0,
				width : 30,
				fieldLabel : '绑定现有客户',
				listeners : {
					resize : function(e){
						Ext.ux.checkPaddingTop(e);
					},
					check : function(e){
						if(e.getValue())
							checkSelect(e);
					}
				}
			}]
		}, 
		{
			xtype : 'panel',
			columnWidth : .15,
			items : [{
				xtype : 'button',
				id : 'btnBindClient',
				text : '绑定',
				hidden : true,
				disabled : false,
				handler : function(){
					createClientHandler();
					clientWin.show();
					clientWin.center();
				}
			}]
		},
		{
			xtype : 'panel',
			columnWidth : 1,
			html : '<hr style="color:#DDD"/>'
		}, {
			items : [{
//				clientMsg : true,
				id : 'numberClientID',
				fieldLabel : '客户编号'
			}]
		}, {
			items : [{
				clientMsg : true,
				id : 'txtClientName',
				fieldLabel : '客户名称' + Ext.ux.txtFormat.xh,
				allowBlank : false,
				blankText : '客户名称不能为空.'
			}]
		}, {
			items : [getClientTypeTrigger({
				moption : {
					clientMsg : true,
				},
				id : 'tirggerClietnTypeByClient',
				fieldLabel : '客户类别' + Ext.ux.txtFormat.xh,
				allowBlank : false,
				blankText : '客户类别不能为空.'
			})]
		}, {
			items : [{
				clientMsg : true,
				xtype : 'combo',
				id : 'comboClientSex',
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
				selectOnFocus : true	
			}]
		}, {
			items : [{
				clientMsg : true,
				id : 'txtClientMobile',
				fieldLabel : '手机' + Ext.ux.txtFormat.xh,
				allowBlank : false,
				regex : Ext.ux.RegText.phone.reg,
				regexText : Ext.ux.RegText.phone.error
			}]
		}, {
			items : [{
				clientMsg : true,
				id : 'txtClientTele',
				fieldLabel : '电话'
			}]
		}, {
			items : [{
				clientMsg : true,
				xtype : 'datefield',
				id : 'dateClientBirthday',
				fieldLabel : '生日',
				format : 'Y-m-d'
			}]
		}, {
			items : [{
				clientMsg : true,
				id : 'txtClientIDCard',
				fieldLabel : '身份证'
			}]
		}, {
			items : [{
				clientMsg : true,
				id : 'txtClientCompany',
				fieldLabel : '公司'
			}]
		}, {
			items : [{
				clientMsg : true,
				id : 'txtClientTastePref',
				fieldLabel : '口味'
			}]
		}, {
			items : [{
				clientMsg : true,
				id : 'txtClientTaboo',
				fieldLabel : '忌讳'
			}]
		}, {
			columnWidth : 1,
			items : [{
				clientMsg : true,
				id : 'txtClientContactAddress',
				fieldLabel : '联系地址',
				width : 520
			}]
		}, {
			columnWidth : 1,
			items : [{
				clientMsg : true,
				id : 'txtClientComment',
				fieldLabel : '备注',
				width : 520
			}]
		}]
	});
	
	memberBasicWin = new Ext.Window({
		title : '&nbsp;',
		closable : false,
		resizable : false,
		modal : true,
		autoScroll : true,
		width : 650,
		items : [memberBasicPanel],
		bbar : ['->', {
			text : '保存',
			id : 'btnSaveOperationMember',
			iconCls : 'btn_save',
			handler : function(e){
				var actionURL;
				if(memberBasicWin.otype == mObj.operation['insert']){
					actionURL = '../../InsertMember.do';
				}else if(memberBasicWin.otype == mObj.operation['update']){
					actionURL = '../../UpdateMember.do';
				}else{
					return;
				}
				
				var memberStatus = Ext.getCmp('comboMemberStatus');
				var membetType = Ext.getCmp('comboMemberType');
				var memberCardAliasID = Ext.getCmp('numberMemberCardAliasID');
				var clientName = Ext.getCmp('txtClientName');
				var clientType = Ext.getCmp('tirggerClietnTypeByClient');
				var clientMobile = Ext.getCmp('txtClientMobile');
				var clientSex = Ext.getCmp('comboClientSex');
				
				if(!memberStatus.isValid() || !membetType.isValid() || !memberCardAliasID.isValid()
						|| !clientName.isValid() || !clientType.isValid()
						|| !clientMobile.isValid() || !clientSex.isValid()){
					return;
				}
				
				var memberData = operationMemberBasicMsg({type : mObj.operation['get']}).data;
				var params = '', clientData = {}, clientLevel = checkSelect();
				if(clientLevel.getId() == mObj.ctSelect.radioBJM.id){
					clientData = {};
				}else{
					clientData = memberData.client;
					if(clientLevel.getId() == mObj.ctSelect.radioBD.id){
						if(clientData.clientID == ''){
							Ext.example.msg('提示', '请绑定客户信息.');
							return;
						}
					}
				}
				clientData.level = clientLevel.inputValue;
				
				if(memberBasicWin.otype == mObj.operation['update']){
					if(clientData.level == 1){
						var selectData = Ext.ux.getSelData(memberBasicGrid);
						if(selectData.client.level == 0){
							if(selectData.totalBalance != 0 || selectData.point != 0){
								Ext.example.msg('提示', '该会员还有余额, 不允许绑定匿名用户.');
								return;
							}
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
				
				var save = Ext.getCmp('btnSaveOperationMember');
				var close = Ext.getCmp('btnCloseOperationMember');
				Ext.Ajax.request({
					url : actionURL,
					params : {
						pin : pin,
						restaurantID : restaurantID,
						status : memberStatus.getValue(),
						params : params
					},
					success : function(res, opt){
						var jr = Ext.decode(res.responseText);
						if(jr.success){
							Ext.example.msg(jr.title, jr.msg);
							memberBasicWin.hide();
							memberBasicGrid.getStore().reload();
						}else{
							Ext.ux.showMsg(jr);
						}
						save.setDisabled(false);
						close.setDisabled(false);
					},
					failure : function(res, opt){
						Ext.ux.showMsg(Ext.decode(res.responseText));
						save.setDisabled(false);
						close.setDisabled(false);
					}
				});
				
			}
		}, {
			text : '关闭',
			id : 'btnCloseOperationMember',
			iconCls : 'btn_close',
			handler : function(e){
				memberBasicWin.hide();
			}
		}],
		keys : [{
			key : Ext.EventObject.ENTER,
			scope : this,
			fn : function(arg1, e){ 
				if(e.getTarget() != null && e.getTarget().id == memeberCardAliasID.id){
//					alert('shua ka')
				}else{
					Ext.getCmp('btnSaveOperationMember').handler();					
				}
			}
		}, {
			key : Ext.EventObject.ESC,
			scope : this,
			fn : function(){ 
				memberBasicWin.hide();
			}
		}],
		listeners : {
			show : function(){
				Ext.getCmp('comboMemberType').store.loadData(memberTypeData);
			},
			hide : function(){
				Ext.getCmp('tirggerClietnTypeByClient').menu.hide();
			}
		}
	});
};

/**************************************************/
controlInit = function(){
	treeInit();
	gridInit();
	winInit();
};

/**************************************************/
memberInit = function(){
//	dataInit();
	controlInit();
};

// to start the initialization
memberInit();

