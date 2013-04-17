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
	renderText += '<a href="javascript:queryMemberOperationHandler()">操作明细</a>';
	
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
						limit : GRID_PADDING_LIMIT_20
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
		}, 
//		'-', {
//			text : '删除',
//			iconCls : 'btn_delete',
//			handler : function(e){
//				deleteMemberHandler();
//			}
//		}, 
		'-', {
			text : '充值',
			iconCls : 'icon_tb_recharge',
			handler : function(e){
				rechargeHandler();
			}
		}, '-', {
			text : '换卡',
			iconCls : 'btn_refresh',
			handler : function(e){
				changeMemberCardHandler();
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
//			['会员编号', 'id'],
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
		GRID_PADDING_LIMIT_20,
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

function winInit(){
	memberBasicWin = new Ext.Window({
		title : '&nbsp;',
		width : 650,
		height : 414,
		modal : true,
		resizable : false,
		closable : false,
		listeners : {
			hide : function(thiz){
				thiz.body.update('');
			},
			show : function(thiz){
				var task = {
					run : function(){
						if(typeof cm_operationMemberBasicMsg == 'function' && typeof cm_isRender == 'function' && cm_isRender()){
							var data = {};
							if(memberBasicWin.otype == mObj.operation['update']){
								data = Ext.ux.getSelData(memberBasicGrid);
								data = !data ? {status:0} : data;
							}else{
								data = {status:0};
							}
							cm_operationMemberBasicMsg({
								type : 'SET',
								data : data
							});
							Ext.TaskMgr.stop(this);
						}
					},
					interval: 500
				};
				
				thiz.center();
				thiz.load({
					url : '../window/client/controlMember.jsp',
					scripts : true,
					params : {
						
					},
					callback : function(){
						Ext.TaskMgr.start(task);
					}
				});
			}
		},
		keys : [{
			key : Ext.EventObject.ESC,
			scope : this,
			fn : function(){
				memberBasicWin.hide();
			}
		}],
		bbar : ['->', {
			text : '保存',
			id : 'btnSaveControlMemberBasicMsg',
			iconCls : 'btn_save',
			handler : function(e){
				if(typeof operateMemberHandler != 'function'){
					Ext.example.msg('提示', '操作失败, 请求异常, 请尝试刷新页面后重试.');
				}else{
					var btnClose = Ext.getCmp('btnCloseControlMemberBasicMsg');
					operateMemberHandler({
						type : memberBasicWin.otype,
						data : Ext.ux.getSelData(memberBasicGrid),
						setButtonStatus : function(s){
							e.setDisabled(s);
							btnClose.setDisabled(s);
						},
						callback : function(memberData, c, res){
							if(res.success){
								memberBasicWin.hide();
								Ext.example.msg(res.title, res.msg);
								Ext.getCmp('btnSearchMember').handler();
							}else{
								Ext.ux.showMsg(res);
							}
						}
					});							
				}
			}
		}, {
			text : '关闭',
			id : 'btnCloseControlMemberBasicMsg',
			iconCls : 'btn_close',
			handler : function(){
				memberBasicWin.hide();
			}
		}]
	});
}

/**************************************************/
function controlInit(){
	treeInit();
	gridInit();
	winInit();
};

/**************************************************/
function memberInit(){
//	dataInit();
	controlInit();
};

// to start the initialization
memberInit();

