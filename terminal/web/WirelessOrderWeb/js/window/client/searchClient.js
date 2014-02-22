var sc_searchClientGrid;
var sc_obj = {searchType:0, searchValue:''};
Ext.onReady(function(){
	var pe = Ext.query('#divSearchClientContent')[0].parentElement;
	var mw = parseInt(pe.style.width);
	var mh = parseInt(pe.style.height);
	
	var sc_searchClientGridTbar = new Ext.Toolbar({
		items : [{
			xtype : 'tbtext',
			text : '过滤:'
		}, {
			xtype : 'combo',
			id : 'sc_comboClientSearchType',
			readOnly : false,
			forceSelection : true,
			width : 90,
			value : 0,
			store : new Ext.data.SimpleStore({
				fields : [ 'value', 'text' ],
				data : [[0, '全部'], [1, '客户名称'], [2, '公司'], [3, '手机号码'], [4, '性别']]
			}),
			valueField : 'value',
			displayField : 'text',
			typeAhead : true,
			mode : 'local',
			triggerAction : 'all',
			selectOnFocus : true,
			listeners : {
				select : function(thiz, record, index){
					var textValue = Ext.getCmp('sc_txtSearchTextValue');
					var numberValue = Ext.getCmp('sc_txtSearchNumberValue');
					var sexValue = Ext.getCmp('sc_comboSearchClientSex');
					
					sc_obj.searchType = thiz.getValue();
					if(index == 0){
						textValue.setVisible(false);
						numberValue.setVisible(false);
						sexValue.setVisible(false);
						sc_obj.searchValue = '';
					}else if(index == 1 || index == 2){
						textValue.setVisible(true);
						numberValue.setVisible(false);
						sexValue.setVisible(false);
						sc_obj.searchValue = textValue;
					}else if(index == 3){
						textValue.setVisible(false);
						numberValue.setVisible(true);
						sexValue.setVisible(false);
						numberValue.setValue();
						sc_obj.searchValue = numberValue;
					}else if(index == 4){
						textValue.setVisible(false);
						numberValue.setVisible(false);
						sexValue.setVisible(true);
						sexValue.setValue(0);
						sc_obj.searchValue = sexValue;
					}
				}
			}
		}, {
			xtype : 'tbtext',
			text : '&nbsp;&nbsp;'
		}, {
			xtype : 'textfield',
			id : 'sc_txtSearchTextValue',
			hidden : true,
			width : 150
		}, {
			xtype : 'numberfield',
			id : 'sc_txtSearchNumberValue',
			style : 'text-align:left;',
			hidden : true,
			width : 150
		}, {
			xtype : 'combo',
			id : 'sc_comboSearchClientSex',
			hidden : true,
			readOnly : false,
			forceSelection : true,
			width : 90,
			value : 0,
			store : new Ext.data.SimpleStore({
				fields : [ 'value', 'text' ],
				data : [[0,'男'], [1, '女']]
			}),
			valueField : 'value',
			displayField : 'text',
			typeAhead : true,
			mode : 'local',
			triggerAction : 'all',
			selectOnFocus : true			
		}, '->', {
			text : '搜索',
			id : 'sc_btnSearchClientByBindWin',
			iconCls : 'btn_search',
			handler : function(e){
				if(sc_obj.searchType == 0){
					sc_obj.searchValue = '';
				}
				
				var gs = sc_searchClientGrid.getStore();
				gs.baseParams['searchType'] = sc_obj.searchType;
				gs.baseParams['searchValue'] = sc_obj.searchValue == '' ? '' : sc_obj.searchValue.getValue();
				gs.load({
					params : {
						start : 0,
						limit : GRID_PADDING_LIMIT_10
					}
				});
			}
		}]
	});
	
	sc_searchClientGrid = createGridPanel(
		'sc_searchClientGrid',
		'',
		'',
		'',
		'../../QueryClient.do',
		[
			[true, false, false, true], 
			['客户编号', 'clientID', 100],
			['客户名称', 'name', 100],
			['客户类别', 'clientType.name'],
			['性别', 'sexDisplay', 70],
			['手机', 'mobile'],
			['电话', 'tele'],
			['公司', 'company', 150],
			['操作', 'operate', 100, 'center', 'sc_searchClientGridOperateRenderer']
		],
		['clientID', 'name', 'clientType', 'clientTypeID', 'clientType.name', 'birthdayFormat', 'birthday',
		 'memberAccount', 'sexDisplay', 'sex', 'mobile', 'tele', 'company', 
		 'tastePref', 'taboo', 'comment', 'contactAddress', 'IDCard'],
		[ ['isPaging', true], ['restaurantID', restaurantID]],
		GRID_PADDING_LIMIT_10,
		'',
		sc_searchClientGridTbar
	);	
	sc_searchClientGrid.border = false;
	sc_searchClientGrid.frame = false;
	sc_searchClientGrid.on('rowdblclick', function(){
		sc_searchClientGridOperateRendererHandler();
	});
	sc_searchClientGrid.on('render', function(){
		Ext.getCmp('sc_btnSearchClientByBindWin').handler();
	});
	new Ext.Panel({
		renderTo : 'divSearchClientContent',
		width : mw,
		height : mh,
		border : false,
		layout : 'fit',
		items : [sc_searchClientGrid]
	});
});

function sc_searchClientGridOperateRenderer(){
	return '<a href="javascript:sc_searchClientGridOperateRendererHandler()">选中</a>';
};

function sc_searchClientGridOperateRendererHandler(){
	var data = Ext.ux.getSelData(sc_searchClientGrid);
	if(!data){
		Ext.example.msg('提示', '请选中一条客户信息.');
	}else{
		if(sc_sreachClientCallback != 'null' && Ext.util.Format.trim(sc_sreachClientCallback).length > 0){
			eval(sc_sreachClientCallback + '(data)');
		}
	}
};