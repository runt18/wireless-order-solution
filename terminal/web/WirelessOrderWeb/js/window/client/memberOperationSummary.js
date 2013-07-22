function mcsc_moneyRenderer(v, md, record, ri, ci, store){
	if(v == 1){
		return record.get('chargeMoney');
	}else if(v == 2){
		return record.get('payMoney');
	}else{
		return '';
	}
}
var mcsc_center_grid, mcsc_search_comboOperateType, mcsc_search_onDuty, mcsc_search_offDuty, mcsc_search_memberType, mcsc_search_memerbMobile, mcsc_search_memerbCard;
Ext.onReady(function(){
	var pe = Ext.query('#divMemberConsumeSummaryContent')[0].parentElement;
	var mw = parseInt(pe.style.width);
	var mh = parseInt(pe.style.height);
	
	mcsc_search_comboOperateType = new Ext.form.ComboBox({
		xtype : 'combo',
		id : 'mcsc_search_comboOperateType',
		readOnly : true,
		forceSelection : true,
		width : 80,
		value : -1,
		store : new Ext.data.SimpleStore({
			fields : ['value', 'text'],
			data : [[-1, '全部'], [1, '充值'], [2, '消费'], [3, '积分消费'], [4, '积分调整'], [5, '金额调整']]
		}),
		valueField : 'value',
		displayField : 'text',
		typeAhead : true,
		mode : 'local',
		triggerAction : 'all',
		selectOnFocus : true,
		listeners : {
			select : function(){
				mcsc_searchMemberSummary();
			}
		}
	});
	mcsc_search_onDuty = new Ext.form.DateField({
		xtype : 'datefield',
		width : 100,
		format : 'Y-m-d',
		maxValue : new Date(new Date().getTime() - 24 * 3600 * 1000),
		readOnly : true,
		allowBlank : false
	});
	mcsc_search_offDuty = new Ext.form.DateField({
		xtype : 'datefield',
		width : 100,
		format : 'Y-m-d',
		maxValue : new Date(new Date().getTime() - 24 * 3600 * 1000),
		maxValue : new Date(),
		readOnly : true,
		allowBlank : false
	});
	mcsc_search_memberType = new Ext.form.ComboBox({
		id : 'mcsc_search_memberType',
		width : 90,
		forceSelection : true,
		readOnly : true,
		store : new Ext.data.JsonStore({
			root : 'root',
			fields : ['id', 'name']
		}),
		valueField : 'id',
		displayField : 'name',
		listClass : ' x-menu ',
		typeAhead : true,
		mode : 'local',
		triggerAction : 'all',
		selectOnFocus : true,
		listeners : {
			render : function(thiz){
				Ext.Ajax.request({
					url : '../../QueryMemberType.do?',
					params : {
						dataSource : 'normal',
						restaurantID : restaurantID
					},
					success : function(res, opt){
						var jr = Ext.decode(res.responseText);
						if(jr.success){
							jr.root.unshift({id:-1, name:'全部'});
						}else{
							Ext.example.msg('异常', '会员类型数据加载失败, 请联系客服人员.');
						}
						thiz.store.loadData(jr);
						thiz.setValue(-1);
					},
					failure : function(res, opt){
						thiz.store.loadData({root:[{typeId:-1, name:'全部'}]});
						thiz.setValue(-1);
					}
				});
			},
			select : function(){
				mcsc_searchMemberSummary();
			}
		}
	});
	mcsc_search_memerbCard = new Ext.form.NumberField({
		width : 100,
		style : 'text-align:left;'
	});
	mcsc_search_memerbMobile = new Ext.form.NumberField({
		width : 100,
		style : 'text-align:left;'
	});
	var mcsc_center_grid_tbar = new Ext.Toolbar({
		height : 26,
		items : [{
			xtype : 'tbtext',
			text : '数据:&nbsp;'
		}, {
			xtype : 'radio',
			name : 'mcsc_search_radioDataSource',
			checked : true,
			inputValue : 'today',
			boxLabel : '当日&nbsp;',
			listeners : {
				check : function(e){
					if(e.getValue()){
						mcsc_search_onDuty.setDisabled(true);
						mcsc_search_offDuty.setDisabled(true);
					}
				}
			}
		}, {
			xtype : 'radio',
			name : 'mcsc_search_radioDataSource',
			inputValue : 'history',
			boxLabel : '历史',
			listeners : {
				check : function(e){
					if(e.getValue()){
						mcsc_search_onDuty.setDisabled(false);
						mcsc_search_offDuty.setDisabled(false);
					}
				}
			}
		}, {
			xtype : 'tbtext',
			text : '&nbsp;&nbsp;日期:'
		}, {
			xtype : 'tbtext',
			text : '&nbsp;'
		}, mcsc_search_onDuty, {
			xtype : 'tbtext',
			text : '&nbsp;至&nbsp;'
		}, mcsc_search_offDuty, {
			xtype : 'tbtext',
			text : '&nbsp;&nbsp;操作类型:'
		}, mcsc_search_comboOperateType, {
			xtype : 'tbtext',
			text : '&nbsp;&nbsp;会员类型:'
		}, mcsc_search_memberType, {
			xtype : 'tbtext',
			text : '&nbsp;&nbsp;手机号码:'
		}, mcsc_search_memerbMobile, {
			xtype : 'tbtext',
			text : '&nbsp;&nbsp;会员卡:'
		}, mcsc_search_memerbCard, '->', {
			text : '搜索',
			iconCls : 'btn_search',
			handler : function(){
				mcsc_searchMemberSummary();
			}
		}, {
			text : '重置',
			iconCls : 'btn_refresh',
			handler : function(e){
				mcsc_search_comboOperateType.setValue(-1);
				mcsc_search_memberType.setValue(-1);
				mcsc_search_memerbMobile.setValue();
				mcsc_search_memerbCard.setValue();
				mcsc_searchMemberSummary();
			}
		}]
	});
	mcsc_center_grid = createGridPanel(
		'cds_center_grid',
		'',
		'',
		'',
		'../../QueryMemberOperationSummary.do',
		[
			[true, false, false, true], 
			['会员名称', 'member.name'],
			['会员类型', 'member.memberType.name'],
			['充值金额', 'chargeMoney',,'right', 'Ext.ux.txtFormat.gridDou'],
			['消费次数', 'consumeAmount',,'right', 'Ext.ux.txtFormat.gridDou'],
			['消费金额', 'payMoney',,'right', 'Ext.ux.txtFormat.gridDou'],
			['积分', 'consumePoint',,'right', 'Ext.ux.txtFormat.gridDou'],
			['积分消费', 'pointConsume',,'right', 'Ext.ux.txtFormat.gridDou'],
			['积分调整', 'pointAdjust',,'right', 'Ext.ux.txtFormat.gridDou'],
			['金额调整', 'moneyAdjust',,'right', 'Ext.ux.txtFormat.gridDou']
		],
		MOSummaryRecord.getKeys(),
		[['pin',pin], ['isPaging', true], ['restaurantID', restaurantID]],
		GRID_PADDING_LIMIT_20,
		'',
		mcsc_center_grid_tbar
	);
	mcsc_center_grid.frame = false;
	mcsc_center_grid.border = false;
	mcsc_center_grid.keys = [{
		key : Ext.EventObject.ENTER,
		scope : this,
		fn : function(){
			mcsc_searchMemberSummary();
		}
	}];
	mcsc_center_grid.on('render', function(){
		mcsc_searchMemberSummary();
	});
	new Ext.Panel({
		renderTo : 'divMemberConsumeSummaryContent',
		width : mw,
		height : mh,
		border : false,
		layout : 'fit',
		items : [mcsc_center_grid]
	});
});

function mcsc_searchMemberSummary(){
	var radio = document.getElementsByName('mcsc_search_radioDataSource');
	var dataSource = 'today';
	for(var i = 0; i < radio.length; i++){
		if(radio[i].checked == true){
			dataSource = radio[i].value;
			break;
		}
	}
	var onDuty = '', offDuty = '';
	if(dataSource == 'history'){
		if(!mcsc_search_onDuty.isValid() || !mcsc_search_offDuty.isValid()){
			Ext.example.msg('提示', '操作失败, 请选择搜索时间段.');
			return;
		}
		onDuty = mcsc_search_onDuty.getValue().format('Y-m-d 00:00:00');
		offDuty = mcsc_search_offDuty.getValue().format('Y-m-d 23:59:59');
	}
	var memberType = mcsc_search_memberType.getRawValue() != '' ? mcsc_search_memberType.getValue() : '';
	var operateType = mcsc_search_comboOperateType.getRawValue() != '' ? mcsc_search_comboOperateType.getValue() : '';
	
	var gs = mcsc_center_grid.getStore();
	gs.baseParams['dataSource'] = dataSource;
	gs.baseParams['memberType'] = memberType > 0 ? memberType : '';
	gs.baseParams['memberMobile'] = mcsc_search_memerbMobile.getValue();
	gs.baseParams['memberCard'] = mcsc_search_memerbCard.getValue();
	gs.baseParams['operateType'] = operateType > 0 ? operateType : '';
	gs.baseParams['onDuty'] = onDuty;
	gs.baseParams['offDuty'] = offDuty;
	gs.load({
		params : {
			start : 0,
			limit : GRID_PADDING_LIMIT_20
		}
	});
}