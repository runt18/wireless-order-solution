var mrd_mo_grid, mrd_panelMemberOperationContent;
var mrd_search_comboOperateType, mrd_search_memberType, mrd_search_comboOperateType, mrd_search_memerbCard
	,mrd_search_onDuty, mrd_search_offDuty, mrd_search_memberName;
var mrd_modal = true;
Ext.onReady(function(){
	var pe = Ext.query('#divMemberRechargeDetails')[0].parentElement;
	var mw = parseInt(pe.style.width);
	var mh = parseInt(pe.style.height);
	
	mrd_search_comboOperateType = new Ext.form.ComboBox({
		xtype : 'combo',
		id : 'mrd_search_comboOperateType',
		readOnly : false,
		forceSelection : true,
		width : 80,
		value : -1,
		store : new Ext.data.SimpleStore({
			fields : ['value', 'text'],
			data : [[-1, '全部'], [1, '充值'], [6, '取款']]
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
				mrd_searchMemberOperation();
			}
		}
	});
	mrd_search_memberType = new Ext.form.ComboBox({
		id : 'mrd_search_memberType',
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
				mrd_searchMemberOperation();
			}
		}
	});
	mrd_search_memerbCard = new Ext.form.NumberField({
		width : 100,
		style : 'text-align:left;'
	});
	mrd_search_onDuty = new Ext.form.DateField({
		xtype : 'datefield',
		width : 100,
		format : 'Y-m-d',
		maxValue : new Date(),
		hideParent : true,
		hidden : mrd_modal ? false : true,
		readOnly : false,
		allowBlank : false
	});
	mrd_search_offDuty = new Ext.form.DateField({
		xtype : 'datefield',
		width : 100,
		format : 'Y-m-d',
		maxValue : new Date(),
		hideParent : true,
		hidden : mrd_modal ? false : true,
		readOnly : false,
		allowBlank : false
	});
	var mrd_search_dateCombo = Ext.ux.createDateCombo({
		beginDate : mrd_search_onDuty,
		endDate : mrd_search_offDuty,
		callback : function(){
			mrd_searchMemberOperation();
		}
	});
	mrd_search_memberName = new Ext.form.TextField({
		xtype : 'textfield',
		width : 100
		
	});
	var mrd_mo_tbar = new Ext.Toolbar({
		height : 26,
		items : [{
			xtype : 'tbtext',
			text : '数据:&nbsp;'
		}, {
			xtype : 'radio',
			name : 'mrd_search_radioDataSource',
			inputValue : 'today',
			boxLabel : '当日&nbsp;',
			listeners : {
				check : function(e){
					if(e.getValue()){
						mrd_search_onDuty.setDisabled(true);
						mrd_search_offDuty.setDisabled(true);
						mrd_search_dateCombo.setDisabled(true);
					}
				}
			}
		}, {
			xtype : 'radio',
			name : 'mrd_search_radioDataSource',
			inputValue : 'history',
			boxLabel : '历史',
			hideParent : true,
			checked : true,
			hidden : mrd_modal ? false : true,
			listeners : {
				check : function(e){
					if(e.getValue()){
						mrd_search_onDuty.setDisabled(false);
						mrd_search_offDuty.setDisabled(false);
						mrd_search_dateCombo.setDisabled(false);
					}
				}
			}
		}, { 
			xtype : 'tbtext', 
			text : (mrd_modal ? '&nbsp;&nbsp;日期:&nbsp;' : ' ')
		}, mrd_search_dateCombo, {
			xtype : 'tbtext',
			text : '&nbsp;&nbsp;'
		}, mrd_search_onDuty, { 
			xtype : 'tbtext',
			text : (mrd_modal ? '&nbsp;至&nbsp;' : ' ')
		}, mrd_search_offDuty, 
		{
			xtype : 'tbtext',
			text : '&nbsp;&nbsp;会员类型:'
		}, mrd_search_memberType,
		{
			xtype : 'tbtext',
			text : '&nbsp;&nbsp;操作类型:'
		}, mrd_search_comboOperateType, {
			xtype : 'tbtext',
			text : '&nbsp;&nbsp;收款方式:'			
		},{
			xtype : 'combo',
			forceSelection : true,
			width : 80,
			id : 'recharge_comboPayType',
			store : new Ext.data.JsonStore({
				fields : [ 'id', 'name' ]
			}),
			valueField : 'id',
			displayField : 'name',
			typeAhead : true,
			mode : 'local',
			triggerAction : 'all',
			selectOnFocus : true,
			allowBlank : false,
			readOnly : false,
			listeners : {
				render : function(thiz){
					Ext.Ajax.request({
						url : '../../OperatePayType.do',
						params : {
							dataSource : 'getByCond'
						},
						success : function(res, opt){
							var jr = Ext.decode(res.responseText);
							if(jr.success){
								jr.root.unshift({id:-1, name:'全部'});
								thiz.store.loadData(jr.root);
								thiz.setValue(-1);
							}
						},
						failure : function(res, opt){
							thiz.store.loadData({root:[{typeId:-1, name:'全部'}]});
							thiz.setValue(-1);
						}
					});
				},
				select : function(){
					mrd_searchMemberOperation();
				}
			}				
		}, 
		{
			xtype : 'tbtext',
			text : '&nbsp;&nbsp;手机号/卡号/会员名称:'
		}, mrd_search_memberName, '->', {
			text : '搜索',
			iconCls : 'btn_search',
			handler : function(e){
				mrd_searchMemberOperation();
			}
		}, {
			text : '重置',
			iconCls : 'btn_refresh',
			handler : function(e){
				mrd_search_comboOperateType.setValue(-1);
				mrd_search_memberType.setValue(-1);
				mrd_search_memberName.setValue();
				mrd_searchMemberOperation();
			}
			
		}, '-', {
				text : '导出',
//				hidden : true,
				iconCls : 'icon_tb_exoprt_excel',
				handler : function(){
					var radio = document.getElementsByName('mrd_search_radioDataSource');
					var dataSource = 'today';
					for(var i = 0; i < radio.length; i++){
						if(radio[i].checked == true){
							dataSource = radio[i].value;
							break;
						}
					}
					var onDuty = '', offDuty = '';
					if(dataSource == 'history'){
						if(!mrd_search_onDuty.isValid() || !mrd_search_offDuty.isValid()){
							Ext.example.msg('提示', '操作失败, 请选择搜索时间段.');
							return;
						}
						onDuty = Ext.util.Format.date(mrd_search_onDuty.getValue(), 'Y-m-d 00:00:00');
						offDuty = Ext.util.Format.date(mrd_search_offDuty.getValue(), 'Y-m-d 23:59:59');
					}
					var memberType = mrd_search_memberType.getRawValue() != '' ? mrd_search_memberType.getValue() : '';
					var url = '../../{0}?memberType={1}&dataSource={2}&onDuty={3}&offDuty={4}&fuzzy={5}&dataSources={6}&detailOperate={7}&operateType=2&payType={8}';
					url = String.format(
							url, 
							'ExportHistoryStatisticsToExecl.do', 
							memberType > 0 ? memberType : '', 
							'rechargeDetail',
							onDuty,
							offDuty,
							mrd_search_memberName.getValue(),
							dataSource,
							mrd_search_comboOperateType.getRawValue() != '' ? mrd_search_comboOperateType.getValue() : '',
							Ext.getCmp('recharge_comboPayType').getValue()
						);
					window.location = url;
				}
			}]
	});
	mrd_mo_grid = createGridPanel(
		'mrd_mo_grid',
		'',
		'',
		'',
		'../../QueryMemberOperation.do',
		[
			[true, false, false, true], 
			['会员名称', 'member.name', 60],
			['会员类型', 'member.memberType.name'],
			['手机号码', 'member.mobile', 60],
			['实收/实退', 'chargeMoney', 60, 'right', 'Ext.ux.txtFormat.gridDou'],
			['充值/退款', 'deltaTotalMoney', 60, 'right', 'Ext.ux.txtFormat.gridDou'],
			['收款方式', 'chargeTypeText'],
			['操作人', 'staffName', 90, 'center'],
			['操作时间', 'operateDateFormat'],
			['操作类型', 'operateTypeText', 90, 'center']
		],
		MemberOperationRecord.getKeys(),
		[ ['isPaging', true], ['restaurantID', restaurantID]],
		GRID_PADDING_LIMIT_20,
		'',
		mrd_mo_tbar
	);
	mrd_mo_grid.frame = false;
	mrd_mo_grid.border = false;
	mrd_mo_grid.on('render', function(thiz){
		mrd_search_dateCombo.setValue(1);
		mrd_search_dateCombo.fireEvent('select', mrd_search_dateCombo, null, 1);
	});
	mrd_mo_grid.getStore().on('load', function(){
//		mrd_search_memerbCard.setValue();
	});
	mrd_panelMemberOperationContent = new Ext.Panel({
		renderTo : 'divMemberRechargeDetails',
		width : mw,
		height : mh,
		border : false,
		layout : 'fit',
		items : [mrd_mo_grid],
		keys : [{
			key : Ext.EventObject.ENTER,
			scope : this,
			fn : function(){
				mrd_searchMemberOperation();
			}
		}]
	});
	
	mrd_mo_grid.getStore().on('load', function(store, records, options){
		if(store.getCount() > 0){
			var memberSum = mrd_mo_grid.getView().getRow(store.getCount() - 1);
			var memberSumView = mrd_mo_grid.getView();
			
			memberSum.style.backgroundColor = '#EEEEEE';
			
			for (var i = 0; i < mrd_mo_grid.getColumnModel().getColumnCount(); i++) {
				var sumCell = memberSumView.getCell(store.getCount()-1, i);
				sumCell.style.fontSize = '15px';
				sumCell.style.fontWeight = 'bold';
				sumCell.style.color = 'green';
			}
			
			memberSumView.getCell(store.getCount()-1, 1).innerHTML = '汇总';
			memberSumView.getCell(store.getCount()-1, 2).innerHTML = '--';
			memberSumView.getCell(store.getCount()-1, 3).innerHTML = '--';
			memberSumView.getCell(store.getCount()-1, 6).innerHTML = '--';
			memberSumView.getCell(store.getCount()-1, 7).innerHTML = '--';
			memberSumView.getCell(store.getCount()-1, 8).innerHTML = '--';
			memberSumView.getCell(store.getCount()-1, 9).innerHTML = '--';
		}
	});
	
	
	
	
});
function mrd_searchMemberOperation(){
	var radio = document.getElementsByName('mrd_search_radioDataSource');
	var dataSource = 'today';
	for(var i = 0; i < radio.length; i++){
		if(radio[i].checked == true){
			dataSource = radio[i].value;
			break;
		}
	}
	var onDuty = '', offDuty = '';
	if(dataSource == 'history'){
		if(!mrd_search_onDuty.isValid() || !mrd_search_offDuty.isValid()){
			Ext.example.msg('提示', '操作失败, 请选择搜索时间段.');
			return;
		}
		onDuty = Ext.util.Format.date(mrd_search_onDuty.getValue(), 'Y-m-d 00:00:00');
		offDuty = Ext.util.Format.date(mrd_search_offDuty.getValue(), 'Y-m-d 23:59:59');
	}
	var memberType = mrd_search_memberType.getRawValue() != '' ? mrd_search_memberType.getValue() : '';
	
	var gs = mrd_mo_grid.getStore();
	gs.baseParams['dataSource'] = dataSource;
	gs.baseParams['memberType'] = memberType > 0 ? memberType : '';
	gs.baseParams['fuzzy'] = mrd_search_memberName.getValue();
	gs.baseParams['operateType'] = 2;
	gs.baseParams['chargeType'] = Ext.getCmp('recharge_comboPayType').getValue();
	gs.baseParams['detailOperate'] = mrd_search_comboOperateType.getRawValue() != '' ? mrd_search_comboOperateType.getValue() : '';
	gs.baseParams['onDuty'] = onDuty;
	gs.baseParams['offDuty'] = offDuty;
	gs.baseParams['total'] = true;
	gs.load({
		params : {
			start : 0,
			limit : GRID_PADDING_LIMIT_20
		}
	});
}