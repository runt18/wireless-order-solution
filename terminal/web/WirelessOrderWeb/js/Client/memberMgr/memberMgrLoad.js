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

winInit = function(){
	
	var memeberCardID = new Ext.form.NumberField({
		id : 'numberMemberCardID',
		fieldLabel : '会员卡号',
		width : 370
	});
	
	var importPanel = new Ext.Panel({
		frame : true,
		border : false,
		layout : 'column',
		defaults : {
			xtype : 'form',
			layout : 'form',
			labelWidth : 65
		},
		items : [{
			columnWidth : .78,
			items : [memeberCardID]
		}, {
			columnWidth : .2,
			items : [{
				xtype : 'button',
				text : '查&nbsp;&nbsp;找',
				listeners : {
					render : function(e){
						e.getEl().setWidth(110, true);
					}
				}
			}]
		}, {
			columnWidth : .33,
			items : [{
				xtype : 'textfield',
				fieldLabel : '会员编号',
				width : 110
			}]
		}, {
			columnWidth : .33,
			items : [{
				xtype : 'textfield',
				fieldLabel : '会员类型',
				width : 110
			}]
		}, {
			columnWidth : .33,
			items : [{
				xtype : 'numberfield',
				fieldLabel : '积分',
				width : 110
			}]
		}, {
			columnWidth : .33,
			items : [{
				xtype : 'numberfield',
				fieldLabel : '总余额',
				width : 110
			}]
		}, {
			columnWidth : .33,
			items : [{
				xtype : 'numberfield',
				fieldLabel : '基础余额',
				width : 110
			}]
		}, {
			columnWidth : .33,
			items : [{
				xtype : 'numberfield',
				fieldLabel : '赠送余额',
				width : 110
			}]
		}]
	});
	
	var commonForm = new Ext.form.FormPanel({
		title : 'commonForm',
		items : [{}]
	});
	
	memberBasicWin = new Ext.Window({
		title : '&nbsp;',
		closable : false,
		resizable : false,
		modal : true,
		autoScroll : true,
		width : 600,
		height : 300,
		items : [importPanel, commonForm],
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
				if(e.getTarget() != null && e.getTarget().id == memeberCardID.getId()){
					alert(e.getTarget().id+'   s你');
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
	
	getOperatorName(pin, '../../');
	
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

