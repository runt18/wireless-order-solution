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
		   + '<a href="#">充值</a>'
		   + '&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;'
		   + '<a href="#">消费详细</a>'
		   + '&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;'
		   + '<a href="#">修改</a>'
		   + '&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;'
		   + '<a href="#">删除</a>';
};

/**************************************************/
controlInit = function(){
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
//			Ext.getCmp('btnSearchClient').handler();
		},
		scope : this 
	}];
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

