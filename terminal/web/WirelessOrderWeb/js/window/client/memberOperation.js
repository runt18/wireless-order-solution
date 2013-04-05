function cdd_memberCardRenderer(v){
	return '******' + v.substring(6,10);
}
function cdd_payMannerRenderer(v, md, record, ri, ci, store){
	if(record.get('operationTypeValue') == 1){
		return record.get('chargeTypeText');
	}else if(record.get('operationTypeValue') == 2){
		return record.get('payTypeText');
	}else{
		return '';
	}
}
var cdd_search_memerbCard;
var cdd_mo_grid;
var cdd_panelMemberOperationContent;
var cdd_search_onDuty, cdd_search_offDuty;
Ext.onReady(function(){
	var pe = Ext.query('#divMemberOperationContent')[0].parentElement;
	var mw = parseInt(pe.style.width);
	var mh = parseInt(pe.style.height);
	
	var cdd_menu_msearchMemberCard_content = new Ext.Panel({
		id : 'cdd_menu_msearchMemberCard',
		width : 700,
		height : 430,
		layout : 'border',
		items : [{
			xtype : 'panel',
			border : false,
			region : 'center'
		}],
		keys : [{
			key : Ext.EventObject.ESC,
			scope : this,
			fn : function(){
				rd_rechargeSreachMemberCardWin.hide();
			}
		}]
	});
	cdd_search_memerbCard = new Ext.form.TriggerField({
		inputType : 'password',
		width : 100,
		menu : new Ext.menu.Menu({
			listeners : {
				show : function(){
					cdd_menu_msearchMemberCard_content.load({
						url : '../window/client/searchMemberCard.jsp',
						scripts : true,
						params : {
							callback : 'cdd_search_memerbCard_callback'
						}
					});
				},
				hide : function(){
					cdd_menu_msearchMemberCard_content.body.update('');
				}
			},
			items : [new Ext.menu.Adapter(cdd_menu_msearchMemberCard_content)]
		}),
		onTriggerClick : function(){
			if(!this.disabled){
	    		this.menu.show(this.el, 'tl-bl?');
	    	}
		},
		listeners : {
			render : function(thiz){
				if(cdd_memberOperationOnMemberCard != '' && cdd_memberOperationOnMemberCard != 'null'){
					thiz.setValue(cdd_memberOperationOnMemberCard);
				}
			}
		}
	});
	cdd_search_onDuty = new Ext.form.DateField({
		xtype : 'datefield',
		width : 100,
		format : 'Y-m-d',
		maxValue : new Date(new Date().getTime() - 24 * 3600 * 1000),
		readOnly : true,
		allowBlank : false
	});
	cdd_search_offDuty = new Ext.form.DateField({
		xtype : 'datefield',
		width : 100,
		format : 'Y-m-d',
		maxValue : new Date(new Date().getTime() - 24 * 3600 * 1000),
		maxValue : new Date(),
		readOnly : true,
		allowBlank : false
	});
//	var cdd_search_common_date = Ext.ux.createDateCombo({
//		beginDate : cdd_search_onDuty,
//		endDate : cdd_search_offDuty,
//		callback : function(){
//			cdd_searchMemberOperation();
//		}
//	});
	var cdd_mo_tbar = new Ext.Toolbar({
		height : 26,
		items : [{
			xtype : 'tbtext',
			text : '数据:&nbsp;'
		}, {
			xtype : 'radio',
			name : 'cdd_search_radioDataSource',
			checked : true,
			inputValue : 'today',
			boxLabel : '当日&nbsp;',
			listeners : {
				check : function(e){
					if(e.getValue()){
						cdd_search_onDuty.setDisabled(true);
						cdd_search_offDuty.setDisabled(true);
					}
				}
			}
		}, {
			xtype : 'radio',
			name : 'cdd_search_radioDataSource',
			inputValue : 'history',
			boxLabel : '历史',
			listeners : {
				check : function(e){
					if(e.getValue()){
						cdd_search_onDuty.setDisabled(false);
						cdd_search_offDuty.setDisabled(false);
					}
				}
			}
		}, {
			xtype : 'tbtext',
			text : '&nbsp;&nbsp;操作类型:'
		}, {
			xtype : 'combo',
			id : 'cdd_search_comboOperateType',
			readOnly : true,
			forceSelection : true,
			width : 80,
			value : 0,
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
			text : '&nbsp;&nbsp;日期:'
		}, 
//		cdd_search_common_date, 
		{
			xtype : 'tbtext',
			text : '&nbsp;'
		}, cdd_search_onDuty, {
			xtype : 'tbtext',
			text : '&nbsp;至&nbsp;'
		}, cdd_search_offDuty, {
			xtype : 'tbtext',
			text : '&nbsp;&nbsp;会员卡:'
		}, cdd_search_memerbCard, '->', {
			text : '搜索',
			iconCls : 'btn_search',
			handler : function(e){
				cdd_searchMemberOperation();
			}
		}]
	});
	cdd_mo_grid = createGridPanel(
		'cdd_mo_grid',
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
			['变动金额', 'deltaTotalMoney', 60, 'right', 'Ext.ux.txtFormat.gridDou'],
			['余额', 'remainingTotalMoney', 60, 'right', 'Ext.ux.txtFormat.gridDou'],
			['会员卡号', 'memberCardAlias', '', '', 'cdd_memberCardRenderer'],
			['会员名称', 'member.client.name', 60],
			['收款方式' , 'operationTypeValue', 60, '', 'cdd_payMannerRenderer']
		],
		['operateSeq','operateDateFormat','staffName', 'operationTypeText', 'operationTypeValue',
		 'memberCardAlias','member.client.name','deltaTotalMoney', 'remainingTotalMoney', 'chargeTypeText', 'payTypeText'],
		[['pin',pin], ['isPaging', true], ['restaurantID', restaurantID]],
		GRID_PADDING_LIMIT_10,
		'',
		cdd_mo_tbar
	);
	cdd_mo_grid.frame = false;
	cdd_mo_grid.border = false;
	cdd_mo_grid.on('render', function(thiz){
		cdd_searchMemberOperation();
	});
	cdd_panelMemberOperationContent = new Ext.Panel({
		renderTo : 'divMemberOperationContent',
		width : mw,
		height : mh,
		border : false,
		layout : 'fit',
		items : [cdd_mo_grid],
		keys : [{
			key : Ext.EventObject.ENTER,
			scope : this,
			fn : function(){
				cdd_searchMemberOperation();
			}
		}]
	});
	
});
function cdd_searchMemberOperation(){
	var radio = document.getElementsByName('cdd_search_radioDataSource');
	var dataSource = 'today';
	for(var i = 0; i < radio.length; i++){
		if(radio[i].checked == true){
			dataSource = radio[i].value;
			break;
		}
	}
	var onDuty = cdd_search_onDuty.getValue();
	var offDuty = cdd_search_offDuty.getValue();
	onDuty = onDuty == '' ? '' : cdd_search_onDuty.getValue().format('Y-m-d 00:00:00');
	offDuty = offDuty == '' ? '' : cdd_search_offDuty.getValue().format('Y-m-d 23:59:59');
	var gs = cdd_mo_grid.getStore();
	gs.baseParams['dataSource'] = dataSource;
	gs.baseParams['memberCard'] = cdd_search_memerbCard.getValue();
	gs.baseParams['operateType'] = Ext.getCmp('cdd_search_comboOperateType').getValue();
	gs.baseParams['onDuty'] = onDuty;
	gs.baseParams['offDuty'] = offDuty;
	gs.load({
		params : {
			start : 0,
			limit : GRID_PADDING_LIMIT_10
		}
	});
}
function cdd_search_memerbCard_callback(data, _c){
	cdd_search_memerbCard.setValue(data['memberCard.aliasID']);
	cdd_search_memerbCard.menu.hide();
	cdd_searchMemberOperation();
}