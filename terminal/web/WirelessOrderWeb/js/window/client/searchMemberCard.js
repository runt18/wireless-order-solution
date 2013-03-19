Ext.onReady(function(){
	var pe = Ext.query('#divSearchMemberCardContent')[0].parentElement;
	var mw = parseInt(pe.style.width);
	var mh = parseInt(pe.style.height);
	
	var s_searchMemberCardGridPanelTbab = new Ext.Toolbar({
		height : 26,
		items : ['->', {
			text : '搜索',
			iconCls : 'btn_search',
			handler : function(e){
				s_searchMemberCardGridPanel.getStore().reload();
			}
		}, '-', {
			text : '添加',
			iconCls : 'btn_add',
			handler : function(){
				s_searchMemberCardGridAddHandler();
			}
		}]
	});
	s_searchMemberCardGridPanel = createGridPanel(
		's_searchMemberCardGridPanel',
		'',
		'',
		'',
		'../../QueryMember.do',
		[
			[true, false, true, true], 
			['卡号', 'memberCard.aliasID'],
			['会员名称', 'client.name'],
			['会员类型', 'memberType.name'],
			['余额', 'totalBalance',,'right', 'Ext.ux.txtFormat.gridDou'],
			['积分', 'point',,'right', 'Ext.ux.txtFormat.gridDou'],
			['手机号码', 'client.mobile'],
			['状态', 'status'],
			['操作', 'operation', 200, 'center', 's_searchMemberCardGridRenderer']
		],
		['memberCard.aliasID', 'client.name', 'memberType.name', 'totalBalance', 'point', 'client.mobile', 'status'],
		[['pin',pin], ['isPaging', true], ['restaurantID', restaurantID], ['dataSource', 'adv']],
		30,
		'',
		s_searchMemberCardGridPanelTbab
	);
	s_searchMemberCardGridPanel.region = 'center';
	s_searchMemberCardGridPanel.on('rowdblclick', function(){
		s_searchMemberCardGridAddHandler();
	});
	new Ext.Panel({
		renderTo : 'divSearchMemberCardContent',
		width : mw,
		height : mh,
//		frame : true,
		border : false,
		layout : 'border',
		items : [s_searchMemberCardGridPanel]
	});
	
});

function s_searchMemberCardGridRenderer(){
	return '<a href="javascript:s_searchMemberCardGridAddHandler()">添加</a>';
}

function s_searchMemberCardGridAddHandler(){
	if(sreachMemberCardCallback != ''){
		var sd = Ext.ux.getSelData(s_searchMemberCardGridPanel);
		if(!sd){
			Ext.example.msg('提示', '请选中一条记录再操作.');
			return;
		}
		eval(sreachMemberCardCallback+'(sd)');
	}
}