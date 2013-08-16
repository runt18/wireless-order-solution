function cancelFoodDetailsStatPanelInit(){
	var cfdsTree = new Ext.tree.TreePanel({
		region : 'west',
		frame : true,
		rootVisible : true,
		width : 150,
		bodyStyle : 'backgroundColor:#FFFFFF; border:1px solid #99BBE8;',
		loader : new Ext.tree.TreeLoader({
			dataUrl : '../../QueryDeptTree.do',
			baseParams : {
				restaurantID : restaurantID
			}
		}),
		root : new Ext.tree.AsyncTreeNode({
			expanded : true,
			text : '全部',
	        leaf : false,
	        deptID : -1,
	        listeners : {
	        	load : function(thiz){
	        		for(var i = thiz.childNodes.length - 1; i >= 0; i--){
        				if(thiz.childNodes[i].attributes.deptID == 253){
        					thiz.removeChild(thiz.childNodes[i]);
        					break;
        				}
        			}
	        	}
	        }
		}),
		tbar : new Ext.Toolbar({
			height : 26,
			items : ['->', {
				text : '刷新',
				iconCls : 'btn_refresh',
				handler : function(){
					cfdsTree.getRootNode().reload();
				}
			}]
		}),
		listeners : {
			dblclick : function(e){
				Ext.getCmp('btnSearchForCancelFoodDetailsStat').handler();
			}
		}
	});
	
	var beginDate = new Ext.form.DateField({
		xtype : 'datefield',		
		format : 'Y-m-d',
		width : 100,
		maxValue : new Date(),
		readOnly : true,
		listeners : {
			blur : function(thiz){									
				Ext.ux.checkDuft(true, thiz.getId(), endDate.getId());
			}
		}
	});
	var endDate = new Ext.form.DateField({
		xtype : 'datefield',
		format : 'Y-m-d',
		width : 100,
		maxValue : new Date(),
		readOnly : true,
		listeners : {
			blur : function(thiz){									
				Ext.ux.checkDuft(false, beginDate.getId(), thiz.getId());
			}
		}
	});
	var dateCombo = Ext.ux.createDateCombo({
		beginDate : beginDate,
		endDate : endDate,
		callback : function(){
			Ext.getCmp('btnSearchForCancelFoodDetailsStat').handler();
		}
	});
	var reasonCombo = new Ext.form.ComboBox({
		xtype : 'combo',
		forceSelection : true,
		width : 150,
		store : new Ext.data.Store({
			proxy : new Ext.data.HttpProxy({
				url : '../../QueryCancelReason.do'
			}),
			baseParams : { 
				restaurantID : restaurantID 
			},
			reader: new Ext.data.JsonReader({
				totalProperty : 'totalProperty',
				root : 'root',
				fields : ['id', 'reason', 'restaurantID']
			}),
			listeners : {
				load : function(thiz){
					thiz.insert(0, new Ext.data.Record({
						id : -1,
						reason : '全部',
						restaurantID : restaurantID
					}));
				}
			}
		}),
		valueField : 'id',
		displayField : 'reason',
		typeAhead : true,
//		mode : 'remote',
		readOnly : true,
		triggerAction : 'all',
		selectOnFocus : true
	});
	
	var cfdsGridTbar = new Ext.Toolbar({
		height : 26,
		items : [{
			xtype:'tbtext',
			text:'日期:'
		}, dateCombo, {
			xtype:'tbtext',
			text:'&nbsp;'
		}, beginDate, {
			xtype:'tbtext',
			text:'&nbsp;至&nbsp;'
		}, endDate, {
			xtype:'tbtext',
			text:'&nbsp;'
		}, '-', {
			xtype:'tbtext',
			text:'&nbsp;退菜原因:'
		}, reasonCombo, {
			xtype:'tbtext',
			text:'&nbsp;'
		}, '->', {
			text : '搜索',
			id : 'btnSearchForCancelFoodDetailsStat',
			iconCls : 'btn_search',
			handler : function(){
				var bd = beginDate.getValue();
				var ed = endDate.getValue();
				var sn = cfdsTree.getSelectionModel().getSelectedNode();
				if(bd == '' && ed == ''){
					dateCombo.setValue(0);
					dateCombo.fireEvent('select',dateCombo,null,0);
					return;
				}else if(bd != '' && ed == ''){
					Ext.ux.checkDuft(true, beginDate.getId(), endDate.getId());
				}else if(bd == '' && ed != ''){
					Ext.ux.checkDuft(false, beginDate.getId(), endDate.getId());
				}
				var gs = cfdsGrid.getStore();
				gs.baseParams['deptID'] = sn != null ? sn.attributes.deptID : -1;
				gs.baseParams['dateBeg'] = beginDate.getValue().format('Y-m-d 00:00:00');
				gs.baseParams['dateEnd'] = endDate.getValue().format('Y-m-d 23:59:59');
				gs.baseParams['reasonID'] = reasonCombo.getValue();
				gs.load({
					params : {
						start : 0,
						limit : 15
					}
				});
			}
		}]
	});
	var cfdsGrid = createGridPanel(
		'',
		'',
		'',
		'',
		'../../QueryCancelledFood.do',
		[[true, false, false, true], 
		 ['日期','orderDateFormat',150], 
		 ['菜名','foodName',180],
         ['部门','deptName'], 
         ['账单号', 'orderID'],
         ['单价','unitPrice',,'right','Ext.ux.txtFormat.gridDou'],
         ['退菜数量','count',,'right','Ext.ux.txtFormat.gridDou'], 
         ['退菜金额','totalPrice',,'right','Ext.ux.txtFormat.gridDou'],		              
         ['操作人','waiter'], 
         ['退菜原因','reason', 200]
		],
		['orderDateFormat', 'foodName', 'deptName', 'orderID', 'unitPrice', 'count', 'totalPrice', 'waiter', 'reason'],
		[ ['isPaging', true], ['qtype', 2], ['otype', 0], ['dtype', 1]],
		15,
		null,
		cfdsGridTbar
	);
	cfdsGrid.region = 'center';
	cfdsGrid.getStore().on('load', function(store, records, options){
		if(store.getCount() > 0){
			var sumRow = cfdsGrid.getView().getRow(store.getCount()-1);	
			sumRow.style.backgroundColor = '#EEEEEE';			
			sumRow.style.color = 'green';
			for(var i = 0; i < cfdsGrid.getColumnModel().getColumnCount(); i++){
				var sumRow = cfdsGrid.getView().getCell(store.getCount()-1, i);
				sumRow.style.fontSize = '15px';
				sumRow.style.fontWeight = 'bold';					
			}
			cfdsGrid.getView().getCell(store.getCount()-1, 1).innerHTML = '汇总';
			cfdsGrid.getView().getCell(store.getCount()-1, 2).innerHTML = '--';
			cfdsGrid.getView().getCell(store.getCount()-1, 3).innerHTML = '--';
			cfdsGrid.getView().getCell(store.getCount()-1, 4).innerHTML = '--';
			cfdsGrid.getView().getCell(store.getCount()-1, 5).innerHTML = '--';
			cfdsGrid.getView().getCell(store.getCount()-1, 8).innerHTML = '--';
			cfdsGrid.getView().getCell(store.getCount()-1, 9).innerHTML = '--';
		}
	});
	//
	cancelFoodDetailsStatPanel = new Ext.Panel({
		title : '明细汇总',
		layout : 'border',
		items : [cfdsTree, cfdsGrid]
	});
}

function cancelFoodByDeptStatPanelInit(){
	var cfbdsTree = new Ext.tree.TreePanel({
		region : 'west',
		frame : true,
		rootVisible : true,
		width : 150,
		bodyStyle : 'backgroundColor:#FFFFFF; border:1px solid #99BBE8;',
		loader : new Ext.tree.TreeLoader({
			dataUrl : '../../QueryDeptTree.do',
			baseParams : {
				restaurantID : restaurantID
			}
		}),
		root : new Ext.tree.AsyncTreeNode({
			expanded : true,
			text : '全部',
	        leaf : false,
	        deptID : -1,
	        listeners : {
	        	load : function(thiz){
	        		for(var i = thiz.childNodes.length - 1; i >= 0; i--){
        				if(thiz.childNodes[i].attributes.deptID == 253){
        					thiz.removeChild(thiz.childNodes[i]);
        					break;
        				}
        			}
	        	}
	        }
		}),
		tbar : new Ext.Toolbar({
			height : 26,
			items : ['->', {
				text : '刷新',
				iconCls : 'btn_refresh',
				handler : function(){
					cfbdsTree.getRootNode().reload();
				}
			}]
		}),
		listeners : {
			dblclick : function(e){
				Ext.getCmp('btnSearchForCancelFoodByDeptStat').handler();
			}
		}
	});
	
	var beginDate = new Ext.form.DateField({
		xtype : 'datefield',		
		format : 'Y-m-d',
		width : 100,
		maxValue : new Date(),
		readOnly : true,
		listeners : {
			blur : function(thiz){									
				Ext.ux.checkDuft(true, thiz.getId(), endDate.getId());
			}
		}
	});
	var endDate = new Ext.form.DateField({
		xtype : 'datefield',
		format : 'Y-m-d',
		width : 100,
		maxValue : new Date(),
		readOnly : true,
		listeners : {
			blur : function(thiz){									
				Ext.ux.checkDuft(false, beginDate.getId(), thiz.getId());
			}
		}
	});
	var dateCombo = Ext.ux.createDateCombo({
		beginDate : beginDate,
		endDate : endDate,
		callback : function(){
			Ext.getCmp('btnSearchForCancelFoodByDeptStat').handler();
		}
	});
	var cfbdsGridTbar = new Ext.Toolbar({
		height : 26,
		items : [{
			xtype:'tbtext',
			text:'日期:'
		}, dateCombo, {
			xtype:'tbtext',
			text:'&nbsp;'
		}, beginDate, {xtype:'tbtext',text:'&nbsp;至&nbsp;'}, endDate, '->', {
			text : '搜索',
			id : 'btnSearchForCancelFoodByDeptStat',
			iconCls : 'btn_search',
			handler : function(){
				var bd = beginDate.getValue();
				var ed = endDate.getValue();
				var sn = cfbdsTree.getSelectionModel().getSelectedNode();
				if(bd == '' && ed == ''){
					dateCombo.setValue(0);
					dateCombo.fireEvent('select',dateCombo,null,0);
					return;
				}else if(bd != '' && ed == ''){
					Ext.ux.checkDuft(true, beginDate.getId(), endDate.getId());
				}else if(bd == '' && ed != ''){
					Ext.ux.checkDuft(false, beginDate.getId(), endDate.getId());
				}
				var gs = cfbdsGrid.getStore();
				gs.baseParams['deptID'] = sn != null ? sn.attributes.deptID : -1;
				gs.baseParams['dateBeg'] = beginDate.getValue().format('Y-m-d 00:00:00');
				gs.baseParams['dateEnd'] = endDate.getValue().format('Y-m-d 23:59:59');
				gs.load();
			}
		}]
	});
	var cfbdsGrid = createGridPanel(
		'',
		'',
		'',
		'',
		'../../QueryCancelledFood.do',
		[[true, false, false, false], 
         ['原因','reason.reason'], 
         ['退菜数量','amount', ,'right','Ext.ux.txtFormat.gridDou'], 
         ['退菜金额','price', ,'right','Ext.ux.txtFormat.gridDou']		              
		],
		['reason.reason', 'amount','price'],
		[ ['qtype', 0], ['otype', 0], ['dtype', 1]],
		0,
		null,
		cfbdsGridTbar
	);
	cfbdsGrid.region = 'center';
	cfbdsGrid.getStore().on('load', function(store, records, options){
		if(store.getCount() > 0){
			var sumRow = cfbdsGrid.getView().getRow(store.getCount()-1);	
			sumRow.style.backgroundColor = '#EEEEEE';			
			sumRow.style.color = 'green';
			for(var i = 0; i < cfbdsGrid.getColumnModel().getColumnCount(); i++){
				var sumRow = cfbdsGrid.getView().getCell(store.getCount()-1, i);
				sumRow.style.fontSize = '15px';
				sumRow.style.fontWeight = 'bold';					
			}
		}
	});
	//
	cancelFoodByDeptStatPanel = new Ext.Panel({
		title : '部门汇总',
		layout : 'border',
		items : [cfbdsTree, cfbdsGrid]
	});
}

function cancelFoodByReasonStatPanelInit(){
	var cfbrsTree = new Ext.tree.TreePanel({
		region : 'west',
		frame : true,
		rootVisible : true,
		width : 150,
		bodyStyle : 'backgroundColor:#FFFFFF; border:1px solid #99BBE8;',
		loader : new Ext.tree.TreeLoader({
			dataUrl : '../../QueryCancelReasonTree.do',
			baseParams : {
				restaurantID : restaurantID
			}
		}),
		root : new Ext.tree.AsyncTreeNode({
			expanded : true,
			text : '全部',
	        leaf : false,
	        reasonID : -1
		}),
		tbar : new Ext.Toolbar({
			height : 26,
			items : ['->', {
				text : '刷新',
				iconCls : 'btn_refresh',
				handler : function(){
					cfbrsTree.getRootNode().reload();
				}
			}]
		}),
		listeners : {
			dblclick : function(e){
				Ext.getCmp('btnSearchForCancelFoodByReasonStat').handler();
			}
		}
	});
	
	var beginDate = new Ext.form.DateField({
		xtype : 'datefield',		
		format : 'Y-m-d',
		width : 100,
		maxValue : new Date(),
		readOnly : true,
		listeners : {
			blur : function(thiz){									
				Ext.ux.checkDuft(true, thiz.getId(), endDate.getId());
			}
		}
	});
	var endDate = new Ext.form.DateField({
		xtype : 'datefield',
		format : 'Y-m-d',
		width : 100,
		maxValue : new Date(),
		readOnly : true,
		listeners : {
			blur : function(thiz){									
				Ext.ux.checkDuft(false, beginDate.getId(), thiz.getId());
			}
		}
	});
	var dateCombo = Ext.ux.createDateCombo({
		beginDate : beginDate,
		endDate : endDate,
		callback : function(){
			Ext.getCmp('btnSearchForCancelFoodByReasonStat').handler();
		}
	});
	var cfbrsGridTbar = new Ext.Toolbar({
		height : 26,
		items : [{
			xtype:'tbtext',
			text:'日期:'
		}, dateCombo, {
			xtype:'tbtext',
			text:'&nbsp;'
		}, beginDate, {xtype:'tbtext',text:'&nbsp;至&nbsp;'}, endDate, '->', {
			text : '搜索',
			id : 'btnSearchForCancelFoodByReasonStat',
			iconCls : 'btn_search',
			handler : function(){
				var bd = beginDate.getValue();
				var ed = endDate.getValue();
				var sn = cfbrsTree.getSelectionModel().getSelectedNode();
				if(bd == '' && ed == ''){
					dateCombo.setValue(0);
					dateCombo.fireEvent('select',dateCombo,null,0);
					return;
				}else if(bd != '' && ed == ''){
					Ext.ux.checkDuft(true, beginDate.getId(), endDate.getId());
				}else if(bd == '' && ed != ''){
					Ext.ux.checkDuft(false, beginDate.getId(), endDate.getId());
				}
				var gs = cfbrsGrid.getStore();
				gs.baseParams['reasonID'] = sn != null ? sn.attributes.reasonID : -1;
				gs.baseParams['dateBeg'] = beginDate.getValue().format('Y-m-d 00:00:00');
				gs.baseParams['dateEnd'] = endDate.getValue().format('Y-m-d 23:59:59');
				gs.load();
			}
		}]
	});
	var cfbrsGrid = createGridPanel(
		'',
		'',
		'',
		'',
		'../../QueryCancelledFood.do',
		[[true, false, false, false], 
         ['部门','dept.name'], 
         ['退菜数量','amount', ,'right','Ext.ux.txtFormat.gridDou'], 
         ['退菜金额','price', ,'right','Ext.ux.txtFormat.gridDou']		              
		],
		['dept.name','amount','price'],
		[ ['qtype', 1], ['otype', 0], ['dtype', 1]],
		15,
		null,
		cfbrsGridTbar
	);
	cfbrsGrid.region = 'center';
	cfbrsGrid.getStore().on('load', function(store, records, options){
		if(store.getCount() > 0){
			var sumRow = cfbrsGrid.getView().getRow(store.getCount()-1);	
			sumRow.style.backgroundColor = '#EEEEEE';			
			sumRow.style.color = 'green';
			for(var i = 0; i < cfbrsGrid.getColumnModel().getColumnCount(); i++){
				var sumRow = cfbrsGrid.getView().getCell(store.getCount()-1, i);
				sumRow.style.fontSize = '15px';
				sumRow.style.fontWeight = 'bold';					
			}
		}
	});
	//
	cancelFoodByReasonStatPanel = new Ext.Panel({
		title : '原因汇总',
		layout : 'border',
		items : [cfbrsTree, cfbrsGrid]
	});
}

function cancelledFood(){
	cancelFoodStatWin = Ext.getCmp('cancelFoodStatWin');
	if(!cancelFoodStatWin){
		// 
		cancelFoodDetailsStatPanelInit();
		//
		cancelFoodByDeptStatPanelInit();
		// 
		cancelFoodByReasonStatPanelInit();
		//
		cancelFoodStatWinTabPanel = new Ext.TabPanel({
			border : false,
			items : [cancelFoodDetailsStatPanel, cancelFoodByDeptStatPanel, cancelFoodByReasonStatPanel],
			listeners : {
				render : function(thiz){
					thiz.setActiveTab(cancelFoodDetailsStatPanel);
				},
				tabchange : function(thiz, tab){
					if(thiz.getActiveTab() == tab){
						cancelFoodStatWin.setTitle(String.format('退菜汇总 -- <font color="green">{0}</font>', tab.title));
					}
				}
			}
		});
		// 
		cancelFoodStatWin = new Ext.Window({
			title : '&nbsp;',
			id : 'cancelFoodStatWin',
			resizable : false,
			modal : true,
			closable : false,
			width : 1200,
			height : 500,
			layout : 'fit',
			items : [cancelFoodStatWinTabPanel],
			bbar : ['->', {
				text : '关闭',
				iconCls : 'btn_close',
				handler : function(){
					cancelFoodStatWin.hide();
				}
			}],
			keys : [{
				key : Ext.EventObject.ESC,
				scope : this,
				fn : function(){
					cancelFoodStatWin.hide();
				}
			}],
			listeners : {
				show : function(){
					
				}
			}
		});
	}
	
	cancelFoodStatWin.show();
	cancelFoodStatWin.center();
};
