/**************************************************/
memberCardAliasRenderer = function(v){
	return v.length > 0 ? ('******' + v.substring(6, 10)) : '';
};
memberStatusRenderer = function(v){
	for(var i = 0; i < memberStatus.length; i++){
		if(eval(memberStatus[i][0] == v)){
			return memberStatus[i][1];
		}
	}
};
memberOperationRenderer = function(val, m, record){
	return ''
		+ '<a href="javascript:updateMemberHandler()">修改</a>'
		+ '&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;'
		+ '<a href="javascript:deleteMemberHandler()">删除</a>'
		+ '&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;'
		+ '<a href="javascript:queryMemberOperationHandler()">操作明细</a>';
//		+ '&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;'
//		+ '<a href="javascript:adjustPoint()">积分调整</a>';
};

/**************************************************/
function treeInit(){
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
			dataUrl : '../../QueryMemberType.do',
			baseParams : {
				dataSource : 'tree',
				restaurantId : restaurantID
			}
		}),
		root : new Ext.tree.AsyncTreeNode({
			text : '全部类型',
			leaf : false,
			border : true,
			expanded : true,
			MemberTypeId : -1,
			listeners : {
				load : function(thiz){
					memberTypeData.root = [];
					for(var i = 0; i < thiz.childNodes.length; i++){
						memberTypeData.root.push({
							memberTypeID : thiz.childNodes[i].attributes['memberTypeId'],
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

function gridInit(){
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
					if(value == 0){
						text.setVisible(false);
						comboOperation.setVisible(false);
						number.setVisible(false);
						mObj.searchValue = '';
						Ext.getCmp('btnSearchMember').handler();
					}else if(value == 1){
						text.setVisible(true);
						number.setVisible(false);
						text.setValue();
						mObj.searchValue = text.getId();
					}else{
						text.setVisible(false);
						number.setVisible(true);
						number.setValue();
						mObj.searchValue = number.getId();
						comboOperation.setValue(0);
						comboOperation.setVisible(!(value == 2 || value == 3));
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
			style : 'text-align: left;',
			hidden : true
		}, '->', {
			text : '搜索',
			id : 'btnSearchMember',
			iconCls : 'btn_search',
			handler : function(){
				var memberTypeNode = memberTypeTree.getSelectionModel().getSelectedNode();
				var searchType = Ext.getCmp('comboMemberSearchType').getValue();
				var searchValue = Ext.getCmp(mObj.searchValue) ? Ext.getCmp(mObj.searchValue).getValue() : '';
				
				var gs = memberBasicGrid.getStore();
				
				if(memberTypeNode){
					if(memberTypeNode.childNodes.length > 0 && memberTypeNode.attributes.memberTypeId != -1){
						gs.baseParams['memberType'] = '';
						gs.baseParams['memberTypeAttr'] = memberTypeNode.attributes.attr;
					}else{
						gs.baseParams['memberType'] = memberTypeNode.attributes.memberTypeId;
						gs.baseParams['memberTypeAttr'] = '';
					}
				}else{
					gs.baseParams['memberType'] = '';
					gs.baseParams['memberTypeAttr'] = '';
				}
				
				gs.baseParams['name'] = searchType == 1 ? searchValue : '';
				gs.baseParams['memberCard'] = searchType == 2 ? searchValue : '';
				gs.baseParams['mobile'] = searchType == 3 ? searchValue : '';
				gs.baseParams['totalBalance'] = searchType == 4 ? searchValue : '';
				gs.baseParams['usedBalance'] = searchType == 5 ? searchValue : '';
				gs.baseParams['consumptionAmount'] = searchType == 6 ? searchValue : '';
				gs.baseParams['point'] = searchType == 7 ? searchValue : '';
				gs.baseParams['usedPoint'] = searchType == 8 ? searchValue : '';
				
				gs.baseParams['so'] = Ext.getCmp('comboSearchValueByOperation').getValue();
				gs.load({
					params : {
						start : 0,
						limit : GRID_PADDING_LIMIT_20
					}
				});
			}
		}, {
			hidden : true,
			text : '重置',
			iconCls : 'btn_refresh',
			handler : function(e){
				Ext.getCmp('btnRefreshMemberType').handler();
				Ext.getCmp('btnSearchMember').handler();
				var st = Ext.getCmp('comboMemberSearchType');
				st.setValue(0);
				st.fireEvent('select', st, null, null);
			}
		}, {
			text : '添加',
			iconCls : 'btn_add',
			handler : function(e){
				insertMemberHandler();
			}
		}, {
			hidden : true,
			text : '修改',
			iconCls : 'btn_edit',
			handler : function(e){
				updateMemberHandler();
			}
		}, {
			hidden : true,
			text : '删除',
			iconCls : 'btn_delete',
			handler : function(e){
				deleteMemberHandler();
			}
		}, {
			text : '充值',
			iconCls : 'icon_tb_recharge',
			handler : function(e){
				rechargeHandler();
			}
		}, {
			text : '积分调整',
			iconCls : 'icon_tb_setting',
			handler : function(e){
				adjustPoint();
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
			['名称', 'name'],
			['类型', 'memberType.name'],
			['消费次数', 'consumptionAmount',,'right', 'Ext.ux.txtFormat.gridDou'],
			['消费总额', 'totalConsumption',,'right', 'Ext.ux.txtFormat.gridDou'],
			['累计积分', 'totalPoint',,'right', 'Ext.ux.txtFormat.gridDou'],
			['当前积分', 'point',,'right', 'Ext.ux.txtFormat.gridDou'],
			['充值额', 'totalCharge',,'right', 'Ext.ux.txtFormat.gridDou'],
			['余额', 'totalBalance',,'right', 'Ext.ux.txtFormat.gridDou'],
			['手机号码', 'mobile'],
			['会员卡号', 'memberCard'],
			['操作', 'operation', 230, 'center', 'memberOperationRenderer']
		],
		MemberBasicRecord.getKeys(),
		[['isPaging', true], ['restaurantID', restaurantID],  ['dataSource', 'normal']],
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
		scope : this,
		fn : function(){ 
			Ext.getCmp('btnSearchMember').handler();
		}
	}];
};

function winInit(){
	memberBasicWin = new Ext.Window({
		title : '&nbsp;',
		width : 650,
		height : 296,
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
						if(typeof cm_operationMemberBasicMsg == 'function'){
							var data = {};
							if(memberBasicWin.otype == Ext.ux.otype['update']){
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
						otype : memberBasicWin.otype
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
						data : memberBasicWin.otype == Ext.ux.otype['update'] ? Ext.ux.getSelData(memberBasicGrid) : null,
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
