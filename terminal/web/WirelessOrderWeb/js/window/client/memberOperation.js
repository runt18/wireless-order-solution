var cdd_panelMemberOperationContent, cdd_gridMemberOperationContent;
Ext.onReady(function(){
	var pe = Ext.query('#divMemberOperationContent')[0].parentElement;
	var mw = parseInt(pe.style.width);
	var mh = parseInt(pe.style.height);
	
	var cdd_mo_tbar_adv = new Ext.Toolbar({
		height : 26,
		items : [{
			xtype : 'tbtext',
			text : '日期:'
		}, {
			xtype : 'datefield',
			format : 'Y-m-d',
			maxValue : new Date(new Date().getTime() - 24 * 3600 * 1000)
		}, {
			xtype : 'tbtext',
			text : '&nbsp;至&nbsp;'
		}, {
			xtype : 'datefield',
			format : 'Y-m-d',
			maxValue : new Date(new Date().getTime() - 24 * 3600 * 1000)
		}, '->', {
			text : '搜索',
			iconCls : 'btn_search',
			handler : function(e){
				cdd_searchMemberOperation();
			}
		}]
	});
	
	var cdd_mo_tbar = new Ext.Toolbar({
		height : 26,
		items : [{
			xtype : 'tbtext',
			text : '数据类型:&nbsp;'
		}, {
			xtype : 'radio',
			name : 'cdd_radioDataSource',
			checked : true,
			inputValue : 'today',
			boxLabel : '当日&nbsp;'
		}, {
			xtype : 'radio',
			name : 'cdd_radioDataSource',
			inputValue : 'history',
			boxLabel : '历史'
		}, {
			xtype : 'tbtext',
			text : '&nbsp;&nbsp;操作类型:'
		}, {
			xtype : 'combo',
			readOnly : true,
			forceSelection : true,
			width : 80,
			store : new Ext.data.SimpleStore({
				fields : ['value', 'text'],
				data : [[0, '全部'], [1, '充值'], [2, '消费'], [5, '换卡']]
			}),
			valueField : 'value',
			displayField : 'text',
			typeAhead : true,
			mode : 'local',
			triggerAction : 'all',
			selectOnFocus : true,
			allowBlank : false,
			blankText : ' '
		}, {
			xtype : 'tbtext',
			text : '&nbsp;&nbsp;会员名称:'
		}, {
			xtype : 'textfield',
			width : 80
		}, {
			xtype : 'tbtext',
			text : '&nbsp;&nbsp;会员卡:'
		}, {
			xtype : 'numberfield',
			width : 100,
			style : 'text-align:left;'
		}, {
			xtype : 'tbtext',
			text : '&nbsp;&nbsp;手机号码:'
		}, {
			xtype : 'numberfield',
			width : 100,
			style : 'text-align:left;'
		}]
	});
	
	cdd_mo_grid = createGridPanel(
		'cdd_gridMemberOperationContent',
		'',
		'',
		'',
		'../../QueryMemberOperation.do',
		[
			[true, false, false, true], 
			['流水号', 'operateSeq'],
			['操作时间', 'operateDateFormat'],
			['操作人', 'staffName', 60],
			['操作类型', 'operationTypeText', 60],
			['会员卡号', 'memberCardAlias'],
			['会员名称', 'member.client.name', 60],
			['变动金额', 'deltaTotalMoney', 60, 'right', 'Ext.ux.txtFormat.gridDou'],
			['余额', 'member.totalBalance', 60, 'right', 'Ext.ux.txtFormat.gridDou'],
			['收款方式' , 'chargeTypeText', 60, '', 'cdd_payMannerRender']
		],
		['operateSeq','operateDateFormat','staffName', 'operationTypeText', 'operationTypeValue',
		 'memberCardAlias','member.client.name','deltaTotalMoney','member.totalBalance', 'chargeTypeText', 'payTypeText'],
		[['pin',pin], ['isPaging', true], ['restaurantID', restaurantID]],
		3,
		'',
		[cdd_mo_tbar, cdd_mo_tbar_adv]
	);
	cdd_mo_grid.frame = false;
	cdd_mo_grid.border = false;
	cdd_panelMemberOperationContent = new Ext.Panel({
		renderTo : 'divMemberOperationContent',
		width : mw,
		height : mh,
		border : false,
		layout : 'fit',
		items : [cdd_mo_grid]
	});
	
});
/**
 * 
 * @param v
 * @param md
 * @param recode
 * @returns
 */
function cdd_payMannerRender(v, md, recode){
	if(recode.get('operationTypeValue') == 1){
		return recode.get('chargeTypeText');
	}else if(recode.get('operationTypeValue') == 2){
		return recode.get('payTypeText');
	}else{
		return '';
	}
}

/**
 * 
 */
function cdd_searchMemberOperation(){
	var radio = document.getElementsByName('cdd_radioDataSource');
	var dataSource = 'today';
	for(var i = 0; i < radio.length; i++){
		if(radio[i].checked == true){
			dataSource = radio[i].value;
			break;
		}
	}
	
	var gs = cdd_mo_grid.getStore();
	gs.baseParams['dataSource'] = dataSource;
	gs.load({
		params : {
			start : 0,
			limit : 3
		}
	});
}
