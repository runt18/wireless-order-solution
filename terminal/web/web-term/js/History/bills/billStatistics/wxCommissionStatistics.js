Ext.onReady(function(){
	var orderShower;
	
	var dateBegin = new Ext.form.DateField({
		id : 'beginDate_datefiled_wxCommissionStatistics',
		xtype : 'datefield',
		format　: 'Y-m-d',
		maxValue : new Date(),
		readOnly : false,
		allowBlank : false
	});
	
	var dateEnd = new Ext.form.DateField({
		id : 'endDate_datefield_wxCommissionStatistics',
		xtype : 'datefield',
		format : 'Y-m-d',
		maxValue : new Date(),
		readOnly : false,
		allowBlank : false
	});
	
	var dateCombo = Ext.ux.createDateCombo({
		beginDate : dateBegin,
		endDate : dateEnd,
		callback : function(){
			Ext.getCmp('search_btnsearch_commissionStatistics').handler();
		}
	});
	
	
	
	//门店选择
	var branchSelect_combo_wxCommission = new Ext.form.ComboBox({
		id : 'branchSelect_combo_WxCommissionStatistics',
		readOnly : false,
		forceSelection : true,
		width : 103,
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
							data.push(['-1', '全部']);
							data.push([jr.root[0]['id'], jr.root[0]['name'] + '(集团)']);
							 
							for(var i = 0; i < jr.root[0].branches.length; i++){
								data.push([jr.root[0].branches[i]['id'], jr.root[0].branches[i]['name']]);
							}
						}
						
						thiz.store.loadData(data);
						thiz.setValue(jr.root[0].id);
					}
				});
			},
			select : function(){
				Ext.getCmp('search_btnsearch_commissionStatistics').handler();
			}
		}
		
	});
	
	var commissionToolbar = new Ext.Toolbar({
		items : [{
			xtype : 'label',
			text : '日期:'
		}, dateCombo, {
			xtype : 'label',
			html : '&nbsp;&nbsp;&nbsp;&nbsp;'
		}, dateBegin, {
			xtype : 'label',
			html : '&nbsp;-&nbsp;'
		}, dateEnd, {
			xtype : 'label',
			html : '&nbsp;&nbsp;&nbsp;&nbsp;'
		},{
			xtype : 'label',
			text : '佣金总量:'
		}, {
			xtype : 'numberfield',
			width : '100',
			id : 'minCommissionAmount_numberfield_wxCommissionStatistics'
		}, {
			xtype : 'label',
			html : '&nbsp;-&nbsp;'
		}, {
			xtype : 'numberfield',
			width : '100',
			id : 'maxCommissionAmount_numberfield_wxCommissionStatistics'
		}, {
			xtype : 'label',
			html : '&nbsp;&nbsp;&nbsp;&nbsp;'
		}, {
			xtype : 'label',
			text : '门店:'
		}, branchSelect_combo_wxCommission,'->'
			,{
				text : '搜索',
				id : 'search_btnsearch_commissionStatistics',
				iconCls : 'btn_search',
				handler : function(){
					var store = Ext.getCmp('commissionPanel_gridPanel_commissionStatistics').getStore();
					store.baseParams['dateBegin'] = Ext.getCmp('beginDate_datefiled_wxCommissionStatistics').getValue();
					store.baseParams['dateEnd'] = Ext.getCmp('endDate_datefield_wxCommissionStatistics').getValue();
					store.baseParams['branchId'] = Ext.getCmp('branchSelect_combo_WxCommissionStatistics').getValue() ? Ext.getCmp('branchSelect_combo_WxCommissionStatistics').getValue() : restaurantID;
					store.baseParams['minCommissionAmount'] = Ext.getCmp('minCommissionAmount_numberfield_wxCommissionStatistics').getValue();
					store.baseParams['maxCommissionAmount'] = Ext.getCmp('maxCommissionAmount_numberfield_wxCommissionStatistics').getValue();
					store.load();
				}
			}
		]
	});
	
	
	var cm = new Ext.grid.ColumnModel([
		new Ext.grid.RowNumberer(),
		{
			header : '日期',
			dataIndex : 'operateDateFormat'
		}, {
			header : '代言人',
			dataIndex : 'member.name'
		}, {
			header : '所属门店',
			dataIndex : 'branchName'
		}, {
			header : '佣金金额',
			dataIndex : 'deltaTotalMoney'
		},{
			header : '消费人',
			dataIndex : 'comment',
			renderer : function(data){
				if(data.split(',').length > 1){
					if(data.split(',')[1].split(':').length > 1){
						return data.split(',')[1].split(':')[1];
					}
				}else{
					return '';
				}
			}
		},{
			header : '账单号',
			dataIndex : 'comment',
			renderer : function(data){
				if(data.split(',').length > 1){
					if(data.split(',')[0].split(':').length > 1){
						return '<a style="cursor:pointer;" data-type="showOrderMsg_wxCommission">' + data.split(',')[0].split(':')[1] + '</a>';
					}
				}else{
					return '';
				}
			}
		}]);
	
	
	var store = new Ext.data.Store({
		proxy : new Ext.data.HttpProxy({url : '../../QueryWxCommisison.do'}),
		reader : new Ext.data.JsonReader({
			totalProperty : 'totalProperty',
			root : 'root'
		}, [{
			name : 'operateDateFormat'
		},{
			name : 'member.name'
		},{
			name : 'branchName'
		},{
			name : 'deltaTotalMoney'
		},{
			name : 'comment'
		},{
			name : 'comment'
		}])
	});
	
	
	var commissionPanel = new Ext.grid.GridPanel({
		id : 'commissionPanel_gridPanel_commissionStatistics',
		frame : true,
		autoScroll : true,
		height : parseInt(Ext.getDom('statisticsPanel_div_WxCommissionStatistcs').parentElement.style.height.replace(/px/g, '')),
		width : parseInt(Ext.getDom('statisticsPanel_div_WxCommissionStatistcs').parentElement.style.width.replace(/px/g, '')) - 20,
		viewConfig : {
			forceFit : true
		},
		cm : cm,
		loadMask : {
			msg : '数据加载中,请稍等....'
		},
		store : store,
		tbar : commissionToolbar,
		keys : {
			key : Ext.EventObject.ENTER,
			fn : function(){
			}
		}
	});
	
	
	new Ext.Panel({
		title : '佣金统计',
		frame : true,
		renderTo : 'statisticsPanel_div_WxCommissionStatistcs',
		items : [commissionPanel]
	});
	
	commissionPanel.getStore().on('load', function(grid, data){
		var orderIds = $('#statisticsPanel_div_WxCommissionStatistcs').find('[data-type=showOrderMsg_wxCommission]');
		var orderArr = [].slice.call(orderIds);
		orderArr.forEach(function(el, index, arr){
			el.onclick = function(){
				showOrderMsg(el.innerHTML);
			}
		});
	});
	
	function showOrderMsg(orderId){
		
		orderShower = new Ext.Window({
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
					orderShower.destroy();
				}
			}],
			keys : [{
				key : Ext.EventObject.ESC,
				scope : this,
				fn : function(){
					orderShower.destroy();
				}
			}],
			listeners : {
				show : function(thiz) {
					thiz.load({
						url : '../window/history/viewBillDetail.jsp', 
						scripts : true,
						method : 'post'
					});
					thiz.center();	
					
					thiz.orderId = orderId;
					thiz.branchId = Ext.getCmp('branchSelect_combo_WxCommissionStatistics').getValue();
				}
			}
		});
		
		orderShower.show();
		orderShower.center();
	}
	
	dateCombo.setValue(1);
	dateCombo.fireEvent('select');
});
