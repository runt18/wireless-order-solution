var salesSubQueryType = 0;
var salesSubOrderType = 0;
var salesSubDeptId = -1;

function createStatGridTabDutyFn(_c){
	if(_c == null || typeof _c == 'undefined'){
		_c = {};
	}
	var comboDuty = new Ext.form.ComboBox({
		xtype : 'combo',
		id : typeof _c.id == 'undefined' ? null : _c.id,
    	width : 330,
    	store : new Ext.data.JsonStore({
    		root : 'root',
    		fields : [ 'duty', 'displayMsg' ],
    		data : _c.data != null && typeof _c.data != 'undefined' ? _c.data : {root:[]}
    	}),	
		valueField : 'duty',
		displayField : 'displayMsg',
		mode : 'local',
		triggerAction : 'all',
		typeAhead : true,
		selectOnFocus : true,
		forceSelection : true,
		allowBlank : false,
		readOnly : false,
		selectOnFocus : true,
		listeners : typeof _c.listeners != 'undefined' ? _c.listeners : null
	}); 
	return comboDuty;
}

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
        		Ext.getCmp('salesSubBtnSearch').handler();
        	}
        }
	});
	
	var duty = createStatGridTabDutyFn({
		data : shiftDutyOfToday,
		listeners : {
			'select' : function(){
				Ext.getCmp('salesSubBtnSearch').handler();
			}
		}
	});
	var foodName = new Ext.form.TextField({
		width : 100
	});
	var orderFoodStatPanelGridTbar = new Ext.Toolbar({
		height : 26,
		items : [ {xtype:'tbtext',text:'班次:'}, duty,
		{xtype:'tbtext',text:'&nbsp;&nbsp;菜品:'},foodName,
		'->', {
			text : '搜索',
			iconCls : 'btn_search',
			id : 'salesSubBtnSearch',
			handler : function(){
				if(!duty.isValid()){
					if(shiftDutyOfToday.root.lenght == 0){
						Ext.example.msg('提示', '没有班次可操作, 请先开始营业.');
						return
					}else{
						duty.setValue(shiftDutyOfToday.root[0]['duty']);
					}
				}
				var gs = Ext.getCmp('orderFoodStatPanelGrid').getStore();
				gs.baseParams['dateBeg'] = duty.getValue().split(salesSubSplitSymbol)[0];
				gs.baseParams['dateEnd'] = duty.getValue().split(salesSubSplitSymbol)[1];
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
				if(shiftDutyOfToday.root.lenght == 0){
					Ext.example.msg('提示', '没有班次可操作, 请先开始营业.');
					return
				}else{
					duty.setValue(shiftDutyOfToday.root[0]['duty']);
				}
				var url = '../../{0}?pin={1}&restaurantID={2}&dataSource={3}&onDuty={4}&offDuty={5}&deptID={6}&foodName={7}&isCookie=true';
				url = String.format(
						url, 
						'ExportTodayStatisticsToExecl.do', 
						-10, 
						restaurantID, 
						'salesFoodDetail',
						duty.getValue().split(salesSubSplitSymbol)[0],
						duty.getValue().split(salesSubSplitSymbol)[1],
						salesSubDeptId,
						foodName.getValue()
					);
				window.location = url;
			}
		}]
	});
	
	var orderFoodStatPanelGrid = createGridPanel(
		'orderFoodStatPanelGrid',
		'',
		'',
		'',
		'../../SalesSubStatistics.do',
		[[true, false, false, true], 
         ['菜品','food.name', 150], 
         ['销量','salesAmount',,'right','Ext.ux.txtFormat.gridDou'], 
         ['营业额','income',,'right','Ext.ux.txtFormat.gridDou'], 
         ['折扣额','discount',,'right','Ext.ux.txtFormat.gridDou'], 
         ['赠送额','gifted',,'right','Ext.ux.txtFormat.gridDou']
		],
		SalesSubStatRecord.getKeys().concat(['food', 'food.name']),
		[ ['isPaging', true], ['restaurantID', restaurantID], ['dataType', 0], ['queryType', 1]],
		15,
		'',
		orderFoodStatPanelGridTbar
	);
	orderFoodStatPanelGrid.keys = [{
		key : Ext.EventObject.ENTER,
		scope : this,
		fn : function(){
			Ext.getCmp('salesSubBtnSearch').handler();
		}
	}];
	orderFoodStatPanelGrid.region = 'center';
	
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
	//FIXME
	duty = createStatGridTabDutyFn({
		data : shiftDutyOfToday,
		listeners : {
			select : function(){
				Ext.getCmp('salesKitchenSubBtnSearch').handler();
			}
		}
	});
	var kitchenStatPanelGridTbar = new Ext.Toolbar({
		height : 26,
		items : [ {xtype:'tbtext',text:'班次:'}, duty, '->', {
			text : '展开/收缩',
			iconCls : 'icon_tb_toggleAllGroups',
			handler : function(){
				kitchenStatPanelGrid.getView().toggleAllGroups();
			}
		}, '-', {
			text : '搜索',
			id : 'salesKitchenSubBtnSearch',
			iconCls : 'btn_search',
			handler : function(){
				if(!duty.isValid()){
					if(shiftDutyOfToday.root.lenght == 0){
						Ext.example.msg('提示', '没有班次可操作, 请先开始营业.');
						return
					}else{
						duty.setValue(shiftDutyOfToday.root[0]['duty']);
					}
				}
				var gs = kitchenStatPanelGrid.getStore();
				gs.baseParams['dateBeg'] = duty.getValue().split(salesSubSplitSymbol)[0];
				gs.baseParams['dateEnd'] = duty.getValue().split(salesSubSplitSymbol)[1];
				gs.load();
				kitchenStatPanelGrid.getView().expandAllGroups();
			}
		}, '-', {
			text : '导出',
			iconCls : 'icon_tb_exoprt_excel',
			handler : function(){
				if(!duty.isValid()){
					if(shiftDutyOfToday.root.lenght == 0){
						Ext.example.msg('提示', '没有班次可操作, 请先开始营业.');
						return
					}else{
						duty.setValue(shiftDutyOfToday.root[0]['duty']);
					}
				}
				var url = '../../{0}?pin={1}&restaurantID={2}&dataSource={3}&onDuty={4}&offDuty={5}&deptID={6}&isCookie=true';
				url = String.format(
						url, 
						'ExportTodayStatisticsToExecl.do', 
						-10, 
						restaurantID, 
						'salesByKitchen',
						duty.getValue().split(salesSubSplitSymbol)[0],
						duty.getValue().split(salesSubSplitSymbol)[1]
					);
				window.location = url;
			}
		}]
	});
	
	var kitchenStatPanelGrid = createGridPanel(
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
	     ['dept.id','dept.id', 10]
		],
		SalesSubStatRecord.getKeys().concat(['kitchen', 'kitchen.name', 'dept', 'dept.id', 'dept.name']),
		[ ['restaurantID', restaurantID], ['dataType', 0], ['queryType', 2], ['isCookie', true]],
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
	var duty = createStatGridTabDutyFn({
		data : shiftDutyOfToday,
		listeners : {
			select : function(){
				Ext.getCmp('salesDeptSubBtnSearch').handler();
			}
		}
	});
	var deptStatPanelGridTbar = new Ext.Toolbar({
		height : 26,
		items : [ {xtype:'tbtext',text:'班次:'}, duty, '->', {
			text : '搜索',
			id : 'salesDeptSubBtnSearch',
			iconCls : 'btn_search',
			handler : function(){
				if(!duty.isValid()){
					if(shiftDutyOfToday.root.lenght == 0){
						Ext.example.msg('提示', '没有班次可操作, 请先开始营业.');
						return
					}else{
						duty.setValue(shiftDutyOfToday.root[0]['duty']);
					}
				}
				var gs = deptStatPanelGrid.getStore();
				gs.baseParams['dateBeg'] = duty.getValue().split(salesSubSplitSymbol)[0];
				gs.baseParams['dateEnd'] = duty.getValue().split(salesSubSplitSymbol)[1];
				gs.removeAll();
				gs.load();
			}
		}, '-', {
			text : '导出',
			iconCls : 'icon_tb_exoprt_excel',
			handler : function(){
				if(!duty.isValid()){
					if(shiftDutyOfToday.root.lenght == 0){
						Ext.example.msg('提示', '没有班次可操作, 请先开始营业.');
						return
					}else{
						duty.setValue(shiftDutyOfToday.root[0]['duty']);
					}
				}
				var url = '../../{0}?pin={1}&restaurantID={2}&dataSource={3}&onDuty={4}&offDuty={5}&deptID={6}&isCookie=true';
				url = String.format(
						url, 
						'ExportTodayStatisticsToExecl.do', 
						-10, 
						restaurantID, 
						'salesByDept',
						duty.getValue().split(salesSubSplitSymbol)[0],
						duty.getValue().split(salesSubSplitSymbol)[1]
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
	     ['赠送额','gifted',,'right','Ext.ux.txtFormat.gridDou']
		],
		SalesSubStatRecord.getKeys().concat(['dept', 'dept.id', 'dept.name']),
		[ ['restaurantID', restaurantID], ['dataType', 0], ['isCookie', true]],
		30,
		null,
		deptStatPanelGridTbar
	);
	
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
		items : [orderFoodStatPanel, kitchenStatPanel, deptStatPanel]
	});	
}

salesSubPanelnit = function(){
	if(!salesSubWinTabPanel){
		salesSubWinTabPanelInit();
	}
	
	salesSubWin = new Ext.Window({
		title : '销售统计',
		layout : 'fit',
		resizable : false,
		modal : true,
		closable : false,
		constrainHeader : true,
		width : 850,
		height : 500,
		items : [salesSubWinTabPanel],
		bbar : ['->', {
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