var mcd_mo_grid, mcd_panelMemberOperationContent;
var mcd_search_comboOperateType, mcd_search_memberType, mcd_search_memerbCard
	,mcd_search_onDuty, mcd_search_offDuty, mcd_search_memberName;
var mcd_modal = true, queryType = 'History';

function member_showViewBillWin(){
	member_viewBillWin = new Ext.Window({
		layout : 'fit',
		title : '查看账单',
		width : 510,
		height : 550,
		resizable : false,
		closable : false,
		modal : true,
		bbar : ['->', {
			text : '关闭',
			iconCls : 'btn_close',
			handler : function() {
				member_viewBillWin.destroy();
			}
		}],
		keys : [{
			key : Ext.EventObject.ESC,
			scope : this,
			fn : function(){
				member_viewBillWin.destroy();
			}
		}],
		listeners : {
			show : function(thiz) {
				var sd = Ext.ux.getSelData(mcd_mo_grid);
				thiz.load({
					url : '../window/history/viewBillDetail.jsp', 
					scripts : true,
					method : 'post'
				});
				thiz.center();	
				thiz.orderId = sd.orderId;
				thiz.queryType = queryType;
				
			}
		}
	});
}

function member_billViewHandler() {
	member_showViewBillWin();
	member_viewBillWin.show();
	member_viewBillWin.center();
};

function linkOrderId(v){
	return '<a href=\"javascript:member_billViewHandler()\">'+ v +'</a>';
}

Ext.onReady(function(){
	var pe = Ext.query('#divMemberConsumeDetails')[0].parentElement;
	var mw = parseInt(pe.style.width);
	var mh = parseInt(pe.style.height);
	
/*	mcd_search_comboOperateType = new Ext.form.ComboBox({
		xtype : 'combo',
		id : 'mcd_search_comboOperateType',
		readOnly : false,
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
		allowBlank : false,
		listeners : {
			select : function(){
				mcd_searchMemberOperation();
			}
		}
	});*/
	mcd_search_memberType = new Ext.form.ComboBox({
		id : 'mcd_search_memberType',
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
				mcd_searchMemberOperation();
			}
		}
	});
	mcd_search_memerbCard = new Ext.form.NumberField({
		width : 100,
		style : 'text-align:left;'
	});
	mcd_search_onDuty = new Ext.form.DateField({
		xtype : 'datefield',
		width : 100,
		format : 'Y-m-d',
//		maxValue : new Date(new Date().getTime() - 24 * 3600 * 1000),
		maxValue : new Date(),
		hideParent : true,
		hidden : mcd_modal ? false : true,
		readOnly : false,
		allowBlank : false
	});
	mcd_search_offDuty = new Ext.form.DateField({
		xtype : 'datefield',
		width : 100,
		format : 'Y-m-d',
		maxValue : new Date(),
		hideParent : true,
		hidden : mcd_modal ? false : true,
		readOnly : false,
		allowBlank : false
	});
	var mcd_search_dateCombo = Ext.ux.createDateCombo({
		beginDate : mcd_search_onDuty,
		endDate : mcd_search_offDuty,
		callback : function(){
			mcd_searchMemberOperation();
		}
	});
	
	
	
	mcd_search_memberName = new Ext.form.TextField({
		xtype : 'textfield',
		width : 100
		
	});
	var mcd_mo_tbar = new Ext.Toolbar({
		height : 26,
		items : [{
			xtype : 'tbtext',
			text : '数据:&nbsp;'
		}, {
			xtype : 'radio',
			name : 'mcd_search_radioDataSource',
			id : 'memberConsume_today',
			inputValue : 'today',
			boxLabel : '当日&nbsp;',
			listeners : {
				check : function(e){
					if(e.getValue()){
						mcd_search_onDuty.setDisabled(true);
						mcd_search_offDuty.setDisabled(true);
						mcd_search_dateCombo.setDisabled(true);
						queryType = 'Today'
					}
				}
			}
		}, {
			xtype : 'radio',
			name : 'mcd_search_radioDataSource',
			inputValue : 'history',
			boxLabel : '历史',
			hideParent : true,
			checked : true,
			hidden : mcd_modal ? false : true,
			listeners : {
				check : function(e){
					if(e.getValue()){
						mcd_search_onDuty.setDisabled(false);
						mcd_search_offDuty.setDisabled(false);
						mcd_search_dateCombo.setDisabled(false);
						queryType = 'History';
					}
				}
			}
		}, { 
			xtype : 'tbtext', 
			text : (mcd_modal ? '&nbsp;&nbsp;日期:&nbsp;' : ' ')
		}, mcd_search_dateCombo, {
			xtype : 'tbtext',
			text : '&nbsp;&nbsp;'
		}, mcd_search_onDuty, { 
			xtype : 'tbtext',
			text : (mcd_modal ? '&nbsp;至&nbsp;' : ' ')
		}, mcd_search_offDuty, 
/*		{
			xtype : 'tbtext',
			text : '&nbsp;&nbsp;操作类型:'
		}, mcd_search_comboOperateType,*/ 
		{
			xtype : 'tbtext',
			text : '&nbsp;&nbsp;会员类型:'
		}, mcd_search_memberType, {
			xtype : 'tbtext',
			text : '&nbsp;&nbsp;收款方式:'			
		},{
			xtype : 'combo',
			forceSelection : true,
			width : 80,
			id : 'consume_comboPayType',
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
						url : '../../OperatePayType.do?',
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
					mcd_searchMemberOperation();
				}
			}				
		},{
			xtype : 'tbtext',
			text : '&nbsp;&nbsp;手机号/卡号/会员名称:'
		}, mcd_search_memberName, '->', {
			text : '搜索',
			iconCls : 'btn_search',
			handler : function(e){
				mcd_searchMemberOperation();
			}
		}, {
			text : '重置',
			iconCls : 'btn_refresh',
			handler : function(e){
				mcd_search_memberType.setValue(-1);
				mcd_search_memberName.setValue();
				mcd_searchMemberOperation();
			}
			
		}, '-', {
				text : '导出',
//				hidden : true,
				iconCls : 'icon_tb_exoprt_excel',
				handler : function(){
					var radio = document.getElementsByName('mcd_search_radioDataSource');
					var dataSource = 'today';
					for(var i = 0; i < radio.length; i++){
						if(radio[i].checked == true){
							dataSource = radio[i].value;
							break;
						}
					}
					var onDuty = '', offDuty = '';
					if(dataSource == 'history'){
						if(!mcd_search_onDuty.isValid() || !mcd_search_offDuty.isValid()){
							Ext.example.msg('提示', '操作失败, 请选择搜索时间段.');
							return;
						}
						onDuty = Ext.util.Format.date(mcd_search_onDuty.getValue(), 'Y-m-d 00:00:00');
						offDuty = Ext.util.Format.date(mcd_search_offDuty.getValue(), 'Y-m-d 23:59:59');
					}
					var memberType = mcd_search_memberType.getRawValue() != '' ? mcd_search_memberType.getValue() : '';
					var url = '../../{0}?memberType={1}&dataSource={2}&onDuty={3}&offDuty={4}&fuzzy={5}&dataSources={6}&operateType=1&payType={7}';
					url = String.format(
							url, 
							'ExportHistoryStatisticsToExecl.do', 
							memberType > 0 ? memberType : '', 
							'consumeDetail',
							onDuty,
							offDuty,
							mcd_search_memberName.getValue(),
							dataSource,
							Ext.getCmp('consume_comboPayType').getValue()
						);
					window.location = url;
				}
			}]
	});
	mcd_mo_grid = createGridPanel(
		'mcd_mo_grid',
		'',
		'',
		'',
		'../../QueryMemberOperation.do',
		[
			[true, false, false, true], 
			['账单号', 'orderId', 110, 'center', 'linkOrderId'],
			['消费时间', 'operateDateFormat'],
			['会员名称', 'member.name', 60],
			['手机', 'member.mobile'],
			['会员类型', 'member.memberType.name'],
			['收款方式', 'payTypeText'],
			['消费金额', 'payMoney', 60, 'right', 'Ext.ux.txtFormat.gridDou'],
			['所得积分', 'deltaPoint', 60, 'right', 'Ext.ux.txtFormat.gridDou'],
			['操作人', 'staffName', 90, 'center'],
			['备注', 'comment', 200, 'center']
		],
		MemberOperationRecord.getKeys(),
		[ ['isPaging', true], ['restaurantID', restaurantID]],
		GRID_PADDING_LIMIT_20,
		'',
		mcd_mo_tbar
	);
	mcd_mo_grid.frame = false;
	mcd_mo_grid.border = false;

	
	mcd_mo_grid.getStore().on('load', function(store, records, options){
		if(store.getCount() > 0){
			var memberSum = mcd_mo_grid.getView().getRow(store.getCount() - 1);
			var memberSumView = mcd_mo_grid.getView();
			
			memberSum.style.backgroundColor = '#EEEEEE';
			
			for (var i = 0; i < mcd_mo_grid.getColumnModel().getColumnCount(); i++) {
				var sumCell = memberSumView.getCell(store.getCount()-1, i);
				sumCell.style.fontSize = '15px';
				sumCell.style.fontWeight = 'bold';
				sumCell.style.color = 'green';
			}
			memberSumView.getCell(store.getCount()-1, 1).innerHTML = '汇总';
			memberSumView.getCell(store.getCount()-1, 2).innerHTML = '--';
			memberSumView.getCell(store.getCount()-1, 3).innerHTML = '--';
			memberSumView.getCell(store.getCount()-1, 4).innerHTML = '--';
			memberSumView.getCell(store.getCount()-1, 5).innerHTML = '--';
			memberSumView.getCell(store.getCount()-1, 6).innerHTML = '--';
			
			memberSumView.getCell(store.getCount()-1, 9).innerHTML = '--';
			memberSumView.getCell(store.getCount()-1, 10).innerHTML = '--';
		}
	});
	
	mcd_panelMemberOperationContent = new Ext.Panel({
		renderTo : 'divMemberConsumeDetails',
		width : mw,
		height : mh,
		border : false,
		layout : 'fit',
		items : [mcd_mo_grid],
		keys : [{
			key : Ext.EventObject.ENTER,
			scope : this,
			fn : function(){
				mcd_searchMemberOperation();
			}
		}]
	});

	if(otype && otype == 0){
		Ext.getDom('memberConsume_today').checked = true;
		Ext.getCmp('memberConsume_today').fireEvent('check', Ext.getCmp('memberConsume_today'), true);
	}	
	mcd_search_dateCombo.setValue(1);
	mcd_search_dateCombo.fireEvent('select', mcd_search_dateCombo, null, 1);	
	
});
function mcd_searchMemberOperation(){
	var radio = document.getElementsByName('mcd_search_radioDataSource');
	var dataSource = 'today';
	for(var i = 0; i < radio.length; i++){
		if(radio[i].checked == true){
			dataSource = radio[i].value;
			break;
		}
	}
	var onDuty = '', offDuty = '';
	if(dataSource == 'history'){
		if(!mcd_search_onDuty.isValid() || !mcd_search_offDuty.isValid()){
			Ext.example.msg('提示', '操作失败, 请选择搜索时间段.');
			return;
		}
		onDuty = Ext.util.Format.date(mcd_search_onDuty.getValue(), 'Y-m-d 00:00:00');
		offDuty = Ext.util.Format.date(mcd_search_offDuty.getValue(), 'Y-m-d 23:59:59');
	}
	var memberType = mcd_search_memberType.getRawValue() != '' ? mcd_search_memberType.getValue() : '';
	//var operateType = mcd_search_comboOperateType.getRawValue() != '' ? mcd_search_comboOperateType.getValue() : '';
	
	var gs = mcd_mo_grid.getStore();
	gs.baseParams['dataSource'] = dataSource;
	gs.baseParams['memberType'] = memberType > 0 ? memberType : '';
	gs.baseParams['fuzzy'] = mcd_search_memberName.getValue();
	gs.baseParams['payType'] = Ext.getCmp('consume_comboPayType').getValue();
	gs.baseParams['operateType'] = 1;
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