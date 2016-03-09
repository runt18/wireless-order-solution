function cdd_deltaPointRenderer(v, md, r, ri, ci, store){
	var deltaPoint = parseInt(r.get('deltaPoint'));
	if(v == 2 || v == 3 || v == 4 || v == 7 || v == 8){
		if(deltaPoint < 0){
			return "<font color='red' size='4'>"+ deltaPoint + "</font>";
		}else{
			return "<font color='green' size='4'>"+ deltaPoint + "</font>";
		}
	}else{
		return '--';
	}
	
}
function cdd_remainingPointRenderer(v, md, r, ri, ci, store){
	if(v == 2 || v == 3 || v == 4 || v == 7 || v == 8){
		return parseInt(r.get('remainingPoint'));
	}else{
		return '--';
	}
}
function cdd_deltaTotalMoneyRenderer(v, md, r, ri, ci, store){
	var deltaTotalMoney = parseInt(r.get('deltaTotalMoney'));
	if(v == 1 || v == 2 || v == 5 || v == 6 || v == 7 || v == 8){
		
		if(v == 2 || v == 7 || v == 8){
			return '--';
		}else{
			if(deltaTotalMoney < 0){
				return "<font color='red' size='4'>"+ Ext.ux.txtFormat.gridDou(deltaTotalMoney) + "</font>";
			}else{
				return "<font color='green' size='4'>"+ Ext.ux.txtFormat.gridDou(deltaTotalMoney) + "</font>";
			}
		}
	}else{
		return '--';
	}
	
}
function cdd_remainingTotalMoneyRenderer(v, md, r, ri, ci, store){
	if(v == 1 || v == 2 || v == 6 || v == 7 || v == 8){
		if((v == 2 || v == 7 || v == 8) && r.get('member')['memberType']['attributeValue'] == 1){
			return '--';
		}else{
			return Ext.ux.txtFormat.gridDou(r.get('remainingTotalMoney'));			
		}
	}else{
		return '--';
	}
}
function cdd_payMoneyRenderer(v, md, r, ri, ci, store){
	if(v == 1 || v == 6){
		return Ext.ux.txtFormat.gridDou(r.get('chargeMoney'));
	}else if(v == 2 || v == 5 || v == 7 || v == 8){
		return Ext.ux.txtFormat.gridDou(r.get('payMoney'));
	}else{
		return '--';
	}
}
function cdd_payTypeRenderer(v, md, r, ri, ci, store){
	if(v == 1 || v == 6){
		return r.get('chargeTypeText');
	}else if(v == 2 || v == 5 || v == 7 || v == 8){
		return r.get('payTypeText');
	}else{
		return '--';
	}
}
var cdd_mo_grid, cdd_panelMemberOperationContent;
var cdd_search_comboOperateType, cdd_search_memberType, cdd_search_memerbMobile,cdd_search_onDuty, cdd_search_offDuty;
Ext.onReady(function(){
	var pe = Ext.query('#divMemberOperationContent')[0].parentElement;
	var mw = parseInt(pe.style.width);
	var mh = parseInt(pe.style.height);
	
	cdd_search_comboOperateType = new Ext.form.ComboBox({
		xtype : 'combo',
		id : 'cdd_search_comboOperateType',
		readOnly : false,
		forceSelection : true,
		width : 80,
		value : -1,
		store : new Ext.data.SimpleStore({
			fields : ['value', 'text'],
			data : [[-1, '全部'], [1, '消费'], [2, '充值'], [3, '积分']]
		}),
		valueField : 'value',
		displayField : 'text',
		typeAhead : true,
		mode : 'local',
		triggerAction : 'all',
		selectOnFocus : true,
		allowBlank : false,
		listeners : {
			select : function(){
				cdd_mo_grid.getColumnModel().setRenderer(4, cdd_deltaPointRenderer);
				cdd_mo_grid.getColumnModel().setRenderer(5, cdd_remainingPointRenderer);
				cdd_mo_grid.getColumnModel().setRenderer(6, cdd_payMoneyRenderer);
				cdd_mo_grid.getColumnModel().setRenderer(7, cdd_deltaTotalMoneyRenderer);
				cdd_mo_grid.getColumnModel().setRenderer(8, cdd_remainingTotalMoneyRenderer);
				
				cdd_searchMemberOperation();
			}
		}
	});
	cdd_search_memberType = new Ext.form.ComboBox({
		id : 'cdd_search_memberType',
		width : 90,
		forceSelection : true,
		readOnly : false,
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
						isCookie : true,
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
				cdd_searchMemberOperation();
			}
		}
	});
	cdd_search_memerbMobile = new Ext.form.NumberField({
		width : 100,
		style : 'text-align:left;',
		value : cdd_memberOperationOnMobile
	});
	
	//门店选择
	var branch_combo_memberOperation = new Ext.form.ComboBox({
		id : 'branch_combo_memberOperation',
		readOnly : false,
		forceSelection : true,
		width : 123,
		listWidth : 120,
		store : new Ext.data.SimpleStore({
			fields : ['id', 'name']
		}),
		valueField : 'id',
		displayField : 'name',
		typeAhead : true,
		mode : 'local',
		triggerAction : 'all',
		selectOnFocus : true,
		listeners : {
			render : function(thiz){
				var data = [];
				Ext.Ajax.request({
					url : '../../OperateRestaurant.do',
					params : {
						dataSource : 'getByCond',
						id : restaurantID
					},
					success : function(res, opt){
						var jr = Ext.decode(res.responseText);
						
						if(jr.root[0].typeVal != '2'){
							data.push([jr.root[0]['id'], jr.root[0]['name']]);
						}else{
							data.push([null, '全部'], [jr.root[0]['id'], jr.root[0]['name'] + '(集团)']);
							
							for(var i = 0; i < jr.root[0].branches.length; i++){
								data.push([jr.root[0].branches[i]['id'], jr.root[0].branches[i]['name']]);
							}
						}
						
						
						thiz.store.loadData(data);
						
						if(jr.root[0].typeVal != '2'){
							thiz.setValue(jr.root[0].id);
						}else{
							thiz.setValue(null);
						}
						
						thiz.fireEvent('select');
					}
				});
			},
			select : function(){
				cdd_searchMemberOperation();
			}
		}
	});
	
	cdd_search_onDuty = new Ext.form.DateField({
		xtype : 'datefield',
		width : 100,
		format : 'Y-m-d',
//		maxValue : new Date(new Date().getTime() - 24 * 3600 * 1000),
		maxValue : new Date(),
		hideParent : true,
		hidden : cdd_modal ? false : true,
		readOnly : false,
		allowBlank : false
	});
	cdd_search_offDuty = new Ext.form.DateField({
		xtype : 'datefield',
		width : 100,
		format : 'Y-m-d',
		maxValue : new Date(),
		hideParent : true,
		hidden : cdd_modal ? false : true,
		readOnly : false,
		allowBlank : false
	});
	
	var cdd_search_dateCombo = Ext.ux.createDateCombo({
		beginDate : cdd_search_onDuty,
		endDate : cdd_search_offDuty,
		callback : function(){
			cdd_searchMemberOperation();
		}
	});
	var cdd_mo_tbar = new Ext.Toolbar({
		height : 26,
		items : [{
			xtype : 'tbtext',
			text : '数据:&nbsp;'
		}, {
			xtype : 'radio',
			name : 'cdd_search_radioDataSource',
			inputValue : 'today',
			boxLabel : '当日&nbsp;',
			listeners : {
				check : function(e){
					if(e.getValue()){
						cdd_search_onDuty.setDisabled(true);
						cdd_search_offDuty.setDisabled(true);
						cdd_search_dateCombo.setDisabled(true);
					}
				}
			}
		}, {
			xtype : 'radio',
			name : 'cdd_search_radioDataSource',
			inputValue : 'history',
			boxLabel : '历史',
			checked : true,
			hideParent : true,
			hidden : cdd_modal ? false : true,
			listeners : {
				check : function(e){
					if(e.getValue()){
						cdd_search_onDuty.setDisabled(false);
						cdd_search_offDuty.setDisabled(false);
						cdd_search_dateCombo.setDisabled(false);
					}
				}
			}
		}, { 
			xtype : 'tbtext', 
			text : (cdd_modal ? '&nbsp;&nbsp;日期:&nbsp;' : ' ')
		}, cdd_search_dateCombo, {
			xtype : 'tbtext',
			text : '&nbsp;&nbsp;'
		},cdd_search_onDuty, { 
			xtype : 'tbtext',
			text : (cdd_modal ? '&nbsp;至&nbsp;' : ' ')
		}, cdd_search_offDuty, {
			xtype : 'tbtext',
			text : '&nbsp;&nbsp;操作类型:'
		}, cdd_search_comboOperateType, {
			xtype : 'tbtext',
			text : '&nbsp;&nbsp;手机号/卡号/会员名称:'
		}, cdd_search_memerbMobile, {
			xtype : 'tbtext',
			text : '&nbsp;&nbsp;门店选择 : '
		}, branch_combo_memberOperation, '->', {
			text : '搜索',
			iconCls : 'btn_search',
			handler : function(e){
				cdd_searchMemberOperation();
			}
		}, {
			text : '重置',
			iconCls : 'btn_refresh',
			handler : function(e){
				cdd_search_comboOperateType.setValue(-1);
				cdd_search_memberType.setValue(-1);
				cdd_search_memerbMobile.setValue();
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
			['流水号', 'seq', 110],
			['操作类型', 'operateTypeText', 60],
			['会员名称', 'member.name', 60],
			['所属门店', 'branchName', 60],
			['变动积分', 'operateTypeValue', 60, 'right', 'cdd_deltaPointRenderer'],
			['剩余积分', 'operateTypeValue', 60, 'right', 'cdd_remainingPointRenderer'],
			['收款金额', 'operateTypeValue', 60, 'right', 'cdd_payMoneyRenderer'],
			['变动金额', 'operateTypeValue', 60, 'right', 'cdd_deltaTotalMoneyRenderer'],
			['剩余金额', 'operateTypeValue', 60, 'right', 'cdd_remainingTotalMoneyRenderer'],
			['收款方式', 'operateTypeValue', 60, 'center', 'cdd_payTypeRenderer'],
			['操作时间', 'operateDateFormat'],
			['操作人', 'staffName', 60]
		],
		MemberOperationRecord.getKeys(),
		[ ['isPaging', true], ['restaurantID', restaurantID], ['isCookie', true]],
		GRID_PADDING_LIMIT_20,
		'',
		cdd_mo_tbar
	);
	cdd_mo_grid.frame = false;
	cdd_mo_grid.border = false;
	cdd_mo_grid.on('render', function(thiz){
		cdd_search_dateCombo.setValue(1);
		cdd_search_dateCombo.fireEvent('select', cdd_search_dateCombo, null, 1);
	});
	cdd_mo_grid.getStore().on('beforeload', function(){
		if(cdd_search_comboOperateType.getValue() == 1){
			cdd_mo_grid.getColumnModel().setRenderer(4, function(){
				return '--';
			});
			cdd_mo_grid.getColumnModel().setRenderer(5, function(){
				return '--';
			});
		}
		if(cdd_search_comboOperateType.getValue() == 3){
			cdd_mo_grid.getColumnModel().setRenderer(6, function(){
				return '--';
			});
			cdd_mo_grid.getColumnModel().setRenderer(7, function(){
				return '--';
			});
			cdd_mo_grid.getColumnModel().setRenderer(8, function(){
				return '--';
			});
		}

	});
	cdd_mo_grid.getStore().on('load', function(thiz){
		if(thiz.getCount() > 0 && cdd_search_memerbMobile.getValue() != ''){
			Ext.getCmp('mr_queryMemberOperationWin').setTitle('会员操作明细 --> ' + thiz.getAt(0).get('member').name);
		}
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
	var onDuty = '', offDuty = '';
	if(dataSource == 'history'){
		if(!cdd_search_onDuty.isValid() || !cdd_search_offDuty.isValid()){
			Ext.example.msg('提示', '操作失败, 请选择搜索时间段.');
			return;
		}
		onDuty = Ext.util.Format.date(cdd_search_onDuty.getValue(), 'Y-m-d 00:00:00');
		offDuty = Ext.util.Format.date(cdd_search_offDuty.getValue(), 'Y-m-d 23:59:59');
	}
	var memberType = cdd_search_memberType.getRawValue() != '' ? cdd_search_memberType.getValue() : '';
	var operateType = cdd_search_comboOperateType.getRawValue() != '' ? cdd_search_comboOperateType.getValue() : '';
	
	var gs = cdd_mo_grid.getStore();
	gs.baseParams['dataSource'] = dataSource;
	gs.baseParams['memberType'] = memberType > 0 ? memberType : '';
	gs.baseParams['fuzzy'] = cdd_search_memerbMobile.getValue();
	gs.baseParams['operateType'] = operateType > 0 ? operateType : '';
	gs.baseParams['onDuty'] = onDuty;
	gs.baseParams['offDuty'] = offDuty;
	gs.baseParams['branchId'] = Ext.getCmp('branch_combo_memberOperation').getValue();
	gs.load({
		params : {
			start : 0,
			limit : GRID_PADDING_LIMIT_20
		}
	});
}