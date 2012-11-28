/**************************************************/
dataInit = function(){
//	Ext.Ajax.request({
//		url : '../../QueryDiscountTree.do',
//		params : {
//			restaurantID : restaurantID,
//			pin : pin
//		},
//		success : function(res, opt){
//			discountData = eval(res.responseText);
//		},
//		failure : function(res, opt){
//			Ext.ux.showMsg(Ext.decode(res.responseText));
//		}
//	});
};

/**************************************************/
memberOperationRenderer = function(){
	return ''
		   + '<a href="">充值</a>'
		   + '&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;'
		   + '<a href="">消费详细</a>'
		   + '&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;'
		   + '<a href="javascript:updateMemberHandler()">修改</a>'
		   + '&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;'
		   + '<a href="javascript:deleteMemberHandler()">删除</a>';
};

/**************************************************/
treeInit = function(){
	var memberTypeTreeTbar = new Ext.Toolbar({
		items : [ '->', {
			text : '刷新',
			iconCls : 'btn_refresh',
			handler : function(){
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
			MemberTypeID : -1
		}),
		tbar : memberTypeTreeTbar
	});
};

gridInit = function(){
	var memberBasicGridTbar = new Ext.Toolbar({
		items : [{
			xtype : 'tbtext',
			text : String.format(Ext.ux.txtFormat.typeName, '会员类型', 'memberTypeShowType', '----')
		}, {
			xtype : 'tbtext',
			text : '&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;'
		}, 
		'->',
		{
			text : '搜索',
			id : 'btnSearchMember',
			iconCls : 'btn_search',
			handler : function(){
				memberBasicGrid.getStore().reload();
			}
		}, {
			text : '重置',
			iconCls : 'btn_refresh',
			handler : function(e){
				
			}
		}, {
			text : '添加',
			iconCls : 'btn_add',
			handler : function(e){
				insertMemberHandler();
			}
		}, {
			text : '修改',
			iconCls : 'btn_edit',
			handler : function(e){
				updateMemberHandler();
			}
		}, {
			text : '删除',
			iconCls : 'btn_delete',
			handler : function(e){
				deleteMemberHandler();
			}
		}]
	});
	
	memberBasicGrid = createGridPanel(
		'memberBasicGrid',
		'会员信息',
		'',
		'',
		'../../QueryClient.do',
		[
			[true, false, true, true], 
			['会员账号', 'name'],
			['会员卡号', 'clientID'],
			['会员类型', 'clientType.name'],
			['会员名称', 'clientType.name'],
			['余额', 'mobile',,'right'],
			['积分', 'tele',,'right'],
			['是否正常', 'tele',,'center'],
			['最后操作时间', 'company'],
			['最后操作人', 'company'],
			['操作', 'operation', 250, 'center', 'memberOperationRenderer']
		],
		['clientID'],
		[['pin',pin], ['isPaging', true], ['restaurantID', restaurantID]],
		30,
		'',
		memberBasicGridTbar
	);	
	memberBasicGrid.region = 'center';
	memberBasicGrid.keys = [{
		key : Ext.EventObject.ENTER,
		fn : function(){ 
			Ext.getCmp('btnSearchMember').handler();
		},
		scope : this 
	}];
};

testFN = function(a1, a2){
	alert(a1 + '   :   ' + a2);
};

winInit = function(){
	
	var memeberCardID = {
		xtype : 'numberfield',
		id : 'numberMemberCardID',
		fieldLabel : '会员卡号',
		disabled : false,
		style : 'font-weight: bold; color: #FF0000;',
		maxLength : 10,
		maxLengthText : '请输入10位会员卡号',
		width : 400
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
			columnWidth : .8,
			items : [memeberCardID]
		}, {
			columnWidth : .2,
			items : [{
				xtype : 'button',
				text : '读&nbsp;&nbsp;卡',
				disabled : false,
				listeners : {
					render : function(e){
						e.getEl().setWidth(110, true);
					}
				},
				handler : function(){
					
				}
			}]
		}, {
			xtype : 'panel',
			columnWidth : 1
		},{
			items : [{
				xtype : 'textfield',
				fieldLabel : '会员编号'
			}]
		}, {
			items : [{
				xtype : 'textfield',
				fieldLabel : '会员类型'
			}]
		}, {
			items : [{
				xtype : 'numberfield',
				fieldLabel : '积分'
			}]
		}, {
			items : [{
				xtype : 'numberfield',
				fieldLabel : '总余额'
			}]
		}, {
			items : [{
				xtype : 'numberfield',
				fieldLabel : '基础余额'
			}]
		}, {
			items : [{
				xtype : 'numberfield',
				fieldLabel : '赠送余额'
			}]
		}, {
			items : [{
				xtype : 'textfield',
				fieldLabel : '最后操作人'
			}]
		}, {
			items : [{
				xtype : 'textfield',
				fieldLabel : '最后操作时间'
			}]
		}, {
			columnWidth : 1,
			items : [{
				xtype : 'textfield',
				fieldLabel : '最后操作备注',
				width : 520
			}]
		}, {
			xtype : 'panel',
			columnWidth : 1,
			html : '<hr/>'
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
				width : 20,
				fieldLabel : '不记名客户',
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
					alert('绑定现有客户信息.');
				}
			}]
		},
		{
			xtype : 'panel',
			columnWidth : 1,
			html : '<hr/>'
		}, {
			items : [{
				id : 'munClientID',
				fieldLabel : '客户编号'
			}]
		}, {
			items : [{
				id : 'txtClientName',
				fieldLabel : '客户名称'
			}]
		}, {
			items : [{
				id : 'txtClientType',
				fieldLabel : '客户类别 ',
			}]
		}, {
			items : [{
				id : 'comboClientSex',
				fieldLabel : '性别'		
			}]
		}, {
			items : [{
				id : 'txtClientMobile',
				fieldLabel : '手机'
			}]
		}, {
			items : [{
				id : 'txtClientTele',
				fieldLabel : '电话'
			}]
		}, {
			items : [{
				id : 'dateClientBirthday',
				fieldLabel : '生日'
			}]
		}, {
			items : [{
				id : 'txtClientIDCard',
				fieldLabel : '身份证'
			}]
		}, {
			items : [{
				id : 'txtClientCompany',
				fieldLabel : '公司'
			}]
		}, {
			items : [{
				id : 'txtClientTastePref',
				fieldLabel : '口味'
			}]
		}, {
			items : [{
				id : 'txtClientTaboo',
				fieldLabel : '忌讳'
			}]
		}, {
			columnWidth : 1,
			items : [{
				id : 'txtClientContactAddress',
				fieldLabel : '联系地址',
				width : 520
			}]
		}, {
			columnWidth : 1,
			items : [{
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
				
				alert('save');
				return;
//				var clientID = Ext.getCmp('munClientID');
				
				var actionURL = '';
				
//				if(!clientName.isValid() || !clientType.isValid()){
//					return;
//				}
				
				if(memberBasicWin.otype == mObj.operation['insert']){
					actionURL = '';
				}else if(memberBasicWin.otype == mObj.operation['update']){
					actionURL = '';
				}else{
					return;
				}
				
				var save = Ext.getCmp('btnSaveOperationMember');
				var close = Ext.getCmp('btnCloseOperationMember');
				Ext.Ajax.request({
					url : actionURL,
					params : {
						restaurantID : restaurantID
					},
					success : function(res, opt){
						var jr = Ext.decode(res.responseText);
						if(jr.success){
							Ext.example.msg(jr.title, jr.msg);
							clientWin.hide();
							clientBasicGrid.getStore().reload();
						}else{
							Ext.ux.showMsg(jr);
						}
						save.setDisabled(false);
						close.setDisabled(false);
					},
					failure : function(res, opt){
						Ext.ux.showMsg(Ext.util.JSON.decode(res.responseText));
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
			fn : function(arg1, e){ 
				if(e.getTarget() != null && e.getTarget().id == memeberCardID.id){
					alert(e.getTarget().id+'   你');
				}else{
					Ext.getCmp('btnSaveOperationMember').handler();					
				}
			},
			scope : this 
		}, {
			key : Ext.EventObject.ESC,
			fn : function(){ 
				memberBasicWin.hide();
			},
			scope : this 
		}]
	});
	
//	clientWin.setPosition(clientWin.width * -1 -100, 0);
//	clientWin.show();
//	clientWin.hide();

};

/**************************************************/
controlInit = function(){
	
	treeInit();
	
	gridInit();
	
	winInit();
	
};

/**************************************************/
memberInit = function(){
	
	dataInit();
	
	controlInit();
	
//	Ext.Ajax.request({
//	url : '../../QueryDiscountTree.do',
//	params : {
//		restaurantID : restaurantID,
//		pin : pin
//	},
//	success : function(res, opt){
//		alert(res.responseText);
//	},
//	failure : function(res, opt){
//		Ext.ux.showMsg(Ext.decode(res.responseText));
//	}
//});
	
};

// to start the initialization
memberInit();

