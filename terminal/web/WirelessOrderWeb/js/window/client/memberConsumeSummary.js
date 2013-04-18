function mcsc_moneyRenderer(v, md, record, ri, ci, store){
	if(v == 1){
		return record.get('chargeMoney');
	}else if(v == 2){
		return record.get('payMoney');
	}else{
		return '';
	}
}
function mcsc_memberCardRenderer(v){
	return '******' + v.substring(6,10);
}
var mcsc_center_grid;
var mcsc_search_memerbCard;
var mcsc_search_onDuty, mcsc_search_offDuty;
Ext.onReady(function(){
	var pe = Ext.query('#divMemberConsumeSummaryContent')[0].parentElement;
	var mw = parseInt(pe.style.width);
	var mh = parseInt(pe.style.height);
	
	var mcsc_menu_msearchMemberCard_content = new Ext.Panel({
		id : 'mcsc_menu_msearchMemberCard_content',
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
	mcsc_search_memerbCard = new Ext.form.TriggerField({
		inputType : 'password',
		width : 100,
		hideOnClick : false,
		menu : new Ext.menu.Menu({
			listeners : {
				show : function(){
					mcsc_menu_msearchMemberCard_content.load({
						url : '../window/client/searchMemberCard.jsp',
						scripts : true,
						params : {
							callback : 'mcsc_search_memerbCard_callback'
						}
					});
				},
				hide : function(){
					mcsc_menu_msearchMemberCard_content.body.update('');
				}
			},
			items : [new Ext.menu.Adapter(mcsc_menu_msearchMemberCard_content)]
		}),
		onTriggerClick : function(){
			if(!this.disabled){
	    		this.menu.show(this.el, 'tl-bl?');
	    	}
		},
		listeners : {
			render : function(thiz){
				if(mcsc_memberCard != '' && mcsc_memberCard != 'null'){
					thiz.setValue(mcsc_memberCard);
				}
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
			text : '&nbsp;&nbsp;操作类型:'
		}, {
			xtype : 'combo',
			id : 'mcsc_search_comboOperateType',
			readOnly : true,
			forceSelection : true,
			width : 80,
			value : 0,
			store : new Ext.data.SimpleStore({
				fields : ['value', 'text'],
				data : [[0, '全部'], [1, '充值'], [2, '消费']]
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
		}, {
			xtype : 'tbtext',
			text : '&nbsp;'
		}, mcsc_search_onDuty, {
			xtype : 'tbtext',
			text : '&nbsp;至&nbsp;'
		}, mcsc_search_offDuty, {
			xtype : 'tbtext',
			text : '&nbsp;&nbsp;会员卡:'
		}, mcsc_search_memerbCard, {
			iconCls : 'btn_delete',
			tooltip : '清除会员卡信息',
			handler : function(){
				mcsc_search_memerbCard.setValue();
			}
		}, '->', {
			text : '搜索',
			iconCls : 'btn_search',
			handler : function(){
				mcsc_searchMemberConsumeSummary();
			}
		}]
	});
	mcsc_center_grid = createGridPanel(
		'cds_center_grid',
		'',
		'',
		'',
		'../../QueryMemberConsumeSummary.do',
		[
			[true, false, false, true], 
			['日期', 'operateDate','','','function(v){return Ext.util.Format.date(new Date(v), "Y-m-d");}'],
			['汇总类型', '', , 'center','function(){return Ext.getCmp("mcsc_search_comboOperateType").getRawValue();}'],
			['金额', 'operationTypeValue', , 'right', 'mcsc_moneyRenderer'],
			['积分', 'deltaPoint', , 'right', 'Ext.ux.txtFormat.gridDou'],
			['会员类型', 'member.memberType.name'],
			['会员卡号', 'memberCardAlias',, '', 'mcsc_memberCardRenderer'],
			['会员名称', 'member.client.name']
		],
		['operateDate', 'payMoney', 'chargeMoney', 'operationTypeText', 'operationTypeValue',
		 'member.memberType.name', 'memberCardAlias', 'deltaPoint', 'member.client.name'],
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
			mcsc_searchMemberConsumeSummary();
		}
	}];
	mcsc_center_grid.on('render', function(){
		mcsc_searchMemberConsumeSummary();
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

function mcsc_search_memerbCard_callback(data, _c){
	mcsc_search_memerbCard.setValue(data['memberCard.aliasID']);
	mcsc_search_memerbCard.menu.hide();
	mcsc_searchMemberConsumeSummary();
}

function mcsc_searchMemberConsumeSummary(){
	var radio = document.getElementsByName('mcsc_search_radioDataSource');
	var dataSource = 'today';
	for(var i = 0; i < radio.length; i++){
		if(radio[i].checked == true){
			dataSource = radio[i].value;
			break;
		}
	}
	var onDuty = mcsc_search_onDuty.getValue();
	var offDuty = mcsc_search_offDuty.getValue();
	onDuty = onDuty == '' ? '' : mcsc_search_onDuty.getValue().format('Y-m-d 00:00:00');
	offDuty = offDuty == '' ? '' : mcsc_search_offDuty.getValue().format('Y-m-d 23:59:59');
	var gs = mcsc_center_grid.getStore();
	gs.baseParams['dataSource'] = dataSource;
	gs.baseParams['memberCard'] = mcsc_search_memerbCard.getValue();
	gs.baseParams['operateType'] = Ext.getCmp('mcsc_search_comboOperateType').getValue();
	gs.baseParams['onDuty'] = onDuty;
	gs.baseParams['offDuty'] = offDuty;
	gs.load({
		params : {
			start : 0,
			limit : GRID_PADDING_LIMIT_20
		}
	});
}