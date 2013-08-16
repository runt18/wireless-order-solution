var salesSubQueryType = 0;
var salesSubOrderType = 0;
var salesSubDeptId = -1;

function orderFoodStatPanelInit(){
	orderFoodStatPanelDeptTree = new Ext.tree.TreePanel({
		id : 'orderFoodStatPanelDeptTree',
		region : 'west',
		rootVisible : true,
		frame : true,
		width : 150,	
		animate : true,
		bodyStyle : 'backgroundColor:#FFFFFF; border:1px solid #99BBE8;',
		loader:new Ext.tree.TreeLoader({    
			dataUrl:'../../QueryDeptTree.do?time='+new Date(),
	        baseParams : {
	        	'restaurantID' : restaurantID
			}
	    }),
		root: new Ext.tree.AsyncTreeNode({
			expanded : true,
            text : '全部',
            leaf : false,
            deptID : '-1'
		}),
		tbar : new Ext.Toolbar({
			height : 26,
			items : []
		}),
        listeners : {
        	load : function(){
        		var treeRoot = orderFoodStatPanelDeptTree.getRootNode().childNodes;
        		for(var i = (treeRoot.length - 1); i >= 0; i--){
					if(treeRoot[i].attributes.deptID == 253){
						orderFoodStatPanelDeptTree.getRootNode().removeChild(treeRoot[i]);
					}
				}
        	},
        	click : function(e){
        		if(e.attributes.deptID == '' || e.attributes.deptID == '-1'){
        			salesSubDeptId = '';
        			if(e.hasChildNodes()){
        				for(var i = 0; i < e.childNodes.length; i++){
        					salesSubDeptId += (i > 0 ? ',' : '');
        					salesSubDeptId += e.childNodes[i].attributes.deptID;
        				}
        			}
        		}else{
        			salesSubDeptId = e.attributes.deptID;
        		}	        		
        	},
        	dblclick : function(e){
        		Ext.getCmp('salesSubBtnSearchByOrderFood').handler();
        	}
        }
	});
	var beginDate = new Ext.form.DateField({
		xtype : 'datefield',		
		format : 'Y-m-d',
		width : 100,
		readOnly : true,
		maxValue : new Date(),
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
			Ext.getCmp('salesSubBtnSearchByOrderFood').handler();
		}
	});
	var foodName = new Ext.form.TextField({
		width : 100
	});
	var orderFoodStatPanelGridTbar = new Ext.Toolbar({
		height : 26,
		items : [ {xtype:'tbtext',text:'日期:'}, dateCombo, 
		    {xtype:'tbtext',text:'&nbsp;'},  beginDate,
		    {xtype:'tbtext',text:'&nbsp;至&nbsp;'}, endDate, 
		    {xtype:'tbtext',text:'&nbsp;&nbsp;菜品:'}, foodName,
		    '->', {
			text : '搜索',
			iconCls : 'btn_search',
			id : 'salesSubBtnSearchByOrderFood',
			handler : function(){
				var bd = beginDate.getValue();
				var ed = endDate.getValue();
				if(bd == '' && ed == ''){
					dateCombo.setValue(0);
					dateCombo.fireEvent('select',dateCombo,null,0);
					return;
				}else if(bd != '' && ed == ''){
					Ext.ux.checkDuft(true, beginDate.getId(), endDate.getId());
				}else if(bd == '' && ed != ''){
					Ext.ux.checkDuft(false, beginDate.getId(), endDate.getId());
				}
				var gs = orderFoodStatPanelGrid.getStore();
				gs.baseParams['dateBeg'] = beginDate.getRawValue();
				gs.baseParams['dateEnd'] = endDate.getRawValue();
				gs.baseParams['deptID'] = salesSubDeptId;
				gs.baseParams['foodName'] = foodName.getValue();
				gs.load({
					params : {
						start : 0,
						limit : 15
					}
				});
			}
		}, '-', {
			text : '导出',
			iconCls : 'icon_tb_exoprt_excel',
			handler : function(){
				var bd = beginDate.getValue();
				var ed = endDate.getValue();
				if(bd == '' && ed == ''){
					dateCombo.setValue(0);
					dateCombo.fireEvent('select',dateCombo,null,0);
				}else if(bd != '' && ed == ''){
					Ext.ux.checkDuft(true, beginDate.getId(), endDate.getId());
				}else if(bd == '' && ed != ''){
					Ext.ux.checkDuft(false, beginDate.getId(), endDate.getId());
				}
				var url = '../../{0}?pin={1}&restaurantID={2}&dataSource={3}&onDuty={4}&offDuty={5}&deptID={6}&foodName={7}';
				url = String.format(
						url, 
						'ExportHistoryStatisticsToExecl.do', 
						-10, 
						restaurantID, 
						'salesFoodDetail',
						beginDate.getValue().format('Y-m-d 00:00:00'),
						endDate.getValue().format('Y-m-d 23:59:59'),
						salesSubDeptId,
						foodName.getValue()
					);
				window.location = url;
			}
		}]
	});
	
	orderFoodStatPanelGrid = createGridPanel(
		'',
		'',
		'',
		'',
		'../../SalesSubStatistics.do',
		[[true, false, false, true], 
         ['菜品','food.name', 150], 
         ['销量','salesAmount','','right','Ext.ux.txtFormat.gridDou'], 
         ['营业额','income',,'right','Ext.ux.txtFormat.gridDou'], 
         ['折扣额','discount',,'right','Ext.ux.txtFormat.gridDou'], 
         ['赠送额','gifted',,'right','Ext.ux.txtFormat.gridDou'],
         ['成本','cost','','right','Ext.ux.txtFormat.gridDou'], 
         ['成本率','costRate','','right','Ext.ux.txtFormat.gridDou'], 
         ['毛利','profit','','right','Ext.ux.txtFormat.gridDou'], 
         ['毛利率','profitRate','','right','Ext.ux.txtFormat.gridDou'],
         ['均价','avgPrice','','right','Ext.ux.txtFormat.gridDou'], 
         ['单位成本','avgCost','','right','Ext.ux.txtFormat.gridDou']
		],
		SalesSubStatRecord.getKeys().concat(['food', 'food.name']),
		[ ['isPaging', true], ['restaurantID', restaurantID], ['dataType', 1], ['queryType', 1]],
		15,
		'',
		orderFoodStatPanelGridTbar
	);
	orderFoodStatPanelGrid.keys = [{
		key : Ext.EventObject.ENTER,
		scope : this,
		fn : function(){
			Ext.getCmp('salesSubBtnSearchByOrderFood').handler();
		}
	}];
	orderFoodStatPanelGrid.region = 'center';
	orderFoodStatPanelGrid.getStore().on('load', function(store, records, options){
		if(store.getCount() > 0){
			var sumRow = orderFoodStatPanelGrid.getView().getRow(store.getCount()-1);	
			sumRow.style.backgroundColor = '#EEEEEE';			
			sumRow.style.color = 'green';
			for(var i = 0; i < orderFoodStatPanelGrid.getColumnModel().getColumnCount(); i++){
				var sumRow = orderFoodStatPanelGrid.getView().getCell(store.getCount()-1, i);
				sumRow.style.fontSize = '15px';
				sumRow.style.fontWeight = 'bold';					
			}
			orderFoodStatPanelGrid.getView().getCell(store.getCount()-1, 6).innerHTML = '--';
			orderFoodStatPanelGrid.getView().getCell(store.getCount()-1, 7).innerHTML = '--';
			orderFoodStatPanelGrid.getView().getCell(store.getCount()-1, 8).innerHTML = '--';
			orderFoodStatPanelGrid.getView().getCell(store.getCount()-1, 9).innerHTML = '--';
			orderFoodStatPanelGrid.getView().getCell(store.getCount()-1, 10).innerHTML = '--';
			orderFoodStatPanelGrid.getView().getCell(store.getCount()-1, 11).innerHTML = '--';
		}
	});
	orderFoodStatPanel = new Ext.Panel({
		title : '菜品统计',
		layout : 'border',
		items : [orderFoodStatPanelDeptTree, orderFoodStatPanelGrid]
	});	
}

function kitchenGroupTextTpl(rs){
	return '部门:'+rs[0].get('dept.name');
}

function kitchenStatPanelInit(){
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
			Ext.getCmp('salesSubBtnSearchByKitchen').handler();
		}
	});
	var kitchenStatPanelGridTbar = new Ext.Toolbar({
		height : 26,
		items : [{xtype:'tbtext',text:'日期:'}, dateCombo, {xtype:'tbtext',text:'&nbsp;'},
		    beginDate, {xtype:'tbtext',text:'&nbsp;至&nbsp;'}, endDate, '->', {
			text : '展开/收缩',
			iconCls : 'icon_tb_toggleAllGroups',
			handler : function(){
				kitchenStatPanelGrid.getView().toggleAllGroups();
			}
		}, '-', {
			text : '搜索',
			id : 'salesSubBtnSearchByKitchen',
			iconCls : 'btn_search',
			handler : function(){
				var bd = beginDate.getValue();
				var ed = endDate.getValue();
				if(bd == '' && ed == ''){
					endDate.setValue(new Date());
					Ext.ux.checkDuft(false, beginDate.getId(), endDate.getId());
				}else if(bd != '' && ed == ''){
					Ext.ux.checkDuft(true, beginDate.getId(), endDate.getId());
				}else if(bd == '' && ed != ''){
					Ext.ux.checkDuft(false, beginDate.getId(), endDate.getId());
				}
				var gs = kitchenStatPanelGrid.getStore();
				gs.baseParams['dateBeg'] = beginDate.getRawValue();
				gs.baseParams['dateEnd'] = endDate.getRawValue();
				gs.load();
				kitchenStatPanelGrid.getView().expandAllGroups();
			}
		}, '-', {
			text : '导出',
//			hidden : true,
			iconCls : 'icon_tb_exoprt_excel',
			handler : function(){
				var bd = beginDate.getValue();
				var ed = endDate.getValue();
				if(bd == '' && ed == ''){
					dateCombo.setValue(0);
					dateCombo.fireEvent('select',dateCombo,null,0);
				}else if(bd != '' && ed == ''){
					Ext.ux.checkDuft(true, beginDate.getId(), endDate.getId());
				}else if(bd == '' && ed != ''){
					Ext.ux.checkDuft(false, beginDate.getId(), endDate.getId());
				}
				var url = '../../{0}?pin={1}&restaurantID={2}&dataSource={3}&onDuty={4}&offDuty={5}';
				url = String.format(
						url, 
						'ExportHistoryStatisticsToExecl.do', 
						-10, 
						restaurantID, 
						'salesByKitchen',
						beginDate.getValue().format('Y-m-d 00:00:00'),
						endDate.getValue().format('Y-m-d 23:59:59')
					);
				window.location = url;
			}
		}]
	});
	
	kitchenStatPanelGrid = createGridPanel(
		'',
		'',
		'',
		'',
		'../../SalesSubStatistics.do',
		[[true, false, false, false], 
	     ['分厨','kitchen.name'], 
	     ['营业额','income',,'right','Ext.ux.txtFormat.gridDou'], 
	     ['折扣额','discount',,'right','Ext.ux.txtFormat.gridDou'], 
	     ['赠送额','gifted',,'right','Ext.ux.txtFormat.gridDou'],
	     ['成本','cost','','right','Ext.ux.txtFormat.gridDou'], 
         ['成本率','costRate','','right','Ext.ux.txtFormat.gridDou'], 
         ['毛利','profit','','right','Ext.ux.txtFormat.gridDou'], 
         ['毛利率','profitRate','','right','Ext.ux.txtFormat.gridDou'],
	     ['dept.id','dept.id', 10]
		],
		SalesSubStatRecord.getKeys().concat(['dept', 'dept.id', 'dept.name', 'kitchen', 'kitchen.name']),
		[ ['restaurantID', restaurantID], ['dataType', 1], ['queryType', 2]],
		15,
		{
			name : 'dept.id',
			hide : true,
			sort : 'dept.id'
		},
		kitchenStatPanelGridTbar
	);
	kitchenStatPanelGrid.view = new Ext.grid.GroupingView({   
        forceFit:true,   
        groupTextTpl : '{[kitchenGroupTextTpl(values.rs)]}'
    });
	
	kitchenStatPanel = new Ext.Panel({
		title : '分厨统计',
		layout : 'fit',
		items : [kitchenStatPanelGrid]
	});	
}

function deptStatPanelInit(){
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
			Ext.getCmp('salesSubBtnSearchByDept').handler();
		}
	});
	deptStatPanelGridTbar = new Ext.Toolbar({
		height : 26,
		items : [{xtype:'tbtext',text:'日期:'}, dateCombo, {xtype:'tbtext',text:'&nbsp;'}, 
		beginDate, {xtype:'tbtext',text:'&nbsp;至&nbsp;'}, endDate, 
		'->', {
			text : '搜索',
			id : 'salesSubBtnSearchByDept',
			iconCls : 'btn_search',
			handler : function(){
				var bd = beginDate.getValue();
				var ed = endDate.getValue();
				if(bd == '' && ed == ''){
					endDate.setValue(new Date());
					Ext.ux.checkDuft(false, beginDate.getId(), endDate.getId());
				}else if(bd != '' && ed == ''){
					Ext.ux.checkDuft(true, beginDate.getId(), endDate.getId());
				}else if(bd == '' && ed != ''){
					Ext.ux.checkDuft(false, beginDate.getId(), endDate.getId());
				}
				var gs = deptStatPanelGrid.getStore();
				gs.baseParams['dateBeg'] = beginDate.getRawValue();
				gs.baseParams['dateEnd'] = endDate.getRawValue();
				gs.load();
			}
		}, '-', {
			text : '导出',
//			hidden : true,
			iconCls : 'icon_tb_exoprt_excel',
			handler : function(){
				var bd = beginDate.getValue();
				var ed = endDate.getValue();
				if(bd == '' && ed == ''){
					dateCombo.setValue(0);
					dateCombo.fireEvent('select',dateCombo,null,0);
				}else if(bd != '' && ed == ''){
					Ext.ux.checkDuft(true, beginDate.getId(), endDate.getId());
				}else if(bd == '' && ed != ''){
					Ext.ux.checkDuft(false, beginDate.getId(), endDate.getId());
				}
				var url = '../../{0}?pin={1}&restaurantID={2}&dataSource={3}&onDuty={4}&offDuty={5}';
				url = String.format(
						url, 
						'ExportHistoryStatisticsToExecl.do', 
						-10, 
						restaurantID, 
						'salesByDept',
						beginDate.getValue().format('Y-m-d 00:00:00'),
						endDate.getValue().format('Y-m-d 23:59:59')
					);
				window.location = url;
			}
		}]
	});
	
	var deptStatPanelGrid = createGridPanel(
		'',
		'',
		'',
		'',
		'../../SalesSubStatistics.do',
		[[true, false, false, false], 
	     ['部门','dept.name'],
	     ['营业额','income',,'right','Ext.ux.txtFormat.gridDou'], 
	     ['折扣额','discount',,'right','Ext.ux.txtFormat.gridDou'], 
	     ['赠送额','gifted',,'right','Ext.ux.txtFormat.gridDou'],
	     ['成本','cost','','right','Ext.ux.txtFormat.gridDou'], 
         ['成本率','costRate','','right','Ext.ux.txtFormat.gridDou'], 
         ['毛利','profit','','right','Ext.ux.txtFormat.gridDou'], 
         ['毛利率','profitRate','','right','Ext.ux.txtFormat.gridDou']
		],
		SalesSubStatRecord.getKeys().concat(['dept', 'dept.name']),
		[ ['restaurantID', restaurantID], ['dataType', 1], ['queryType', 0]],
		30,
		null,
		deptStatPanelGridTbar
	);
	deptStatPanelGrid.getStore().on('load', function(store, records, options){
		if(store.getCount() > 0){
			var sumRow = deptStatPanelGrid.getView().getRow(store.getCount()-1);	
			sumRow.style.backgroundColor = '#EEEEEE';			
			sumRow.style.color = 'green';
			for(var i = 0; i < deptStatPanelGrid.getColumnModel().getColumnCount(); i++){
				var sumRow = deptStatPanelGrid.getView().getCell(store.getCount()-1, i);
				sumRow.style.fontSize = '15px';
				sumRow.style.fontWeight = 'bold';					
			}
			deptStatPanelGrid.getView().getCell(store.getCount()-1, 6).innerHTML = '--';
			deptStatPanelGrid.getView().getCell(store.getCount()-1, 8).innerHTML = '--';
		}
	});
	deptStatPanel = new Ext.Panel({
		title : '部门统计',
		layout : 'fit',
		items : [deptStatPanelGrid]
	});
}

function salesSubWinTabPanelInit(){
	if(!orderFoodStatPanel){
		orderFoodStatPanelInit();		
	}
	if(!kitchenStatPanel){
		kitchenStatPanelInit();		
	}
	if(!deptStatPanel){
		deptStatPanelInit();		
	}
	
	salesSubWinTabPanel = new Ext.TabPanel({
		xtype : 'tabpanel',
		frame : true,
		activeTab : 0,
		border : false,
		items : [orderFoodStatPanel, kitchenStatPanel, deptStatPanel],
		listeners : {
			tabchange : function(thiz, tab){
				if(thiz.getActiveTab() == tab){
					salesSubWin.setTitle(String.format('销售统计 -- <font color="green">{0}</font>', tab.title));
				}
			}
		}
	});	
}

salesSubPanelnit = function(){
	if(!salesSubWinTabPanel){
		salesSubWinTabPanelInit();
	}
	
	salesSubWin = new Ext.Window({
		title : '&nbsp;',
		layout : 'fit',
		resizable : false,
		modal : true,
		closable : false,
		constrainHeader : true,
		width : 1200,
		height : 500,
		items : [salesSubWinTabPanel],
		bbar : ['->', {
			text : '导出',
			hidden : true,
			handler : function(){
				
			}
		}, {
			text : '关闭',
			iconCls : 'btn_close',
			handler : function(){
				salesSubWin.hide();
			}
		}],
		keys : [{
			key : Ext.EventObject.ESC,
			scope : this,
			fn : function(){
				salesSubWin.hide();
			}
		}]
	});
};

function salesSub(){
	if(!salesSubWin){
		salesSubPanelnit();
	}
	salesSubWin.show();
	salesSubWin.center();
}
