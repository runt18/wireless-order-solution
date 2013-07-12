function smc_memberStatusRenderer(v){
	if(eval(v == 0))
		return '正常';
	else if(eval(v == 1))
		return '冻结';
	else
		return '--';
}

Ext.onReady(function(){
	var pe = Ext.query('#divSearchMemberCardContent')[0].parentElement;
	var mw = parseInt(pe.style.width);
	var mh = parseInt(pe.style.height);
	
	var s_sncgpt_name = new Ext.form.TextField({
		width : 90
	});
	var s_sncgpt_phone = new Ext.form.TextField({
		width : 90
	});
	var s_sncgpt_member_type = new Ext.form.ComboBox({
		id : 's_sncgpt_member_type',
		width : 90,
		forceSelection : true,
		store : new Ext.data.JsonStore({
			url: '../../QueryMemberType.do?restaurantID=' + restaurantID,
			root : 'root',
			fields : ['typeID', 'name']
		}),
		valueField : 'typeID',
		displayField : 'name',
		listClass : ' x-menu ',
		typeAhead : true,
		mode : 'local',
		triggerAction : 'all',
		selectOnFocus : true,
		listeners : {
			render : function(thiz){
				thiz.store.load();
			}
		}
	});
	var s_sncgpt_btnSearch = {
		text : '搜索',
		iconCls : 'btn_search',
		handler : function(e){
			var gs = s_searchMemberCardGridPanel.getStore();
			gs.baseParams['memberType'] = Ext.util.Format.trim(s_sncgpt_member_type.getRawValue()).length == 0 ? '' : s_sncgpt_member_type.getValue();
			gs.baseParams['memberName'] = s_sncgpt_name.getValue();
			gs.baseParams['mobile'] = s_sncgpt_phone.getValue();
			gs.load({
				params : {
					start : 0,
					limit : GRID_PADDING_LIMIT_10
				}
			});
		}
	};
	var s_searchMemberCardGridPanelTbab = new Ext.Toolbar({
		height : 26,
		items : [{
			xtype : 'tbtext',
			text : '会员名称:'
		}, s_sncgpt_name, {
			xtype : 'tbtext',
			text : '&nbsp;&nbsp;手机号码:'
		}, s_sncgpt_phone, {
			xtype : 'tbtext',
			text : '&nbsp;&nbsp;会员类型:'
		}, s_sncgpt_member_type,'->', 
		s_sncgpt_btnSearch, '-', {
			text : '选中',
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
//			['卡号', 'memberCard.aliasID'],
			['会员名称', 'client.name'],
			['会员类型', 'memberType.name'],
			['余额', 'totalBalance',,'right', 'Ext.ux.txtFormat.gridDou'],
			['积分', 'point',,'right', 'Ext.ux.txtFormat.gridDou'],
			['手机号码', 'client.mobile'],
			['状态', 'statusValue',,'center', 'smc_memberStatusRenderer'],
			['操作', 'operation', 100, 'center', 's_searchMemberCardGridRenderer']
		],
		['memberCard.aliasID', 'client.name', 'client.clientTypeID', 'memberType.name', 'totalBalance', 
		 'point', 'client.mobile', 'statusValue', 'memberType.attributeValue'],
		[['pin',pin], ['isPaging', true], ['restaurantID', restaurantID], ['dataSource', 'normal']],
		GRID_PADDING_LIMIT_10,
		'',
		s_searchMemberCardGridPanelTbab
	);
	s_searchMemberCardGridPanel.frame = false;
	s_searchMemberCardGridPanel.border = false;
	s_searchMemberCardGridPanel.region = 'center';
	s_searchMemberCardGridPanel.on('rowdblclick', function(){
		s_searchMemberCardGridAddHandler();
	});
	s_searchMemberCardGridPanel.keys = [{
		key : Ext.EventObject.ENTER,
		scope : this,
		fn : function(){
			s_sncgpt_btnSearch.handler();
		}
	}];
	
	new Ext.Panel({
		renderTo : 'divSearchMemberCardContent',
		width : mw,
		height : mh,
		border : false,
		layout : 'border',
		items : [s_searchMemberCardGridPanel]
	});
	
});

function s_searchMemberCardGridRenderer(){
	return '<a href="javascript:s_searchMemberCardGridAddHandler()">选中</a>';
}

function s_searchMemberCardGridAddHandler(){
	if(sreachMemberCardCallback != ''){
		var data = Ext.ux.getSelData(s_searchMemberCardGridPanel);
		if(!data){
			Ext.example.msg('提示', '请选中一条记录再操作.');
			return;
		}
		eval(sreachMemberCardCallback + '(data)');
	}
}