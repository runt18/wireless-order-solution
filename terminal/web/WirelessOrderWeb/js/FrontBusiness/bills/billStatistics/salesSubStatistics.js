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
		readOnly : true,
		selectOnFocus : true
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
		data:shiftDutyOfToday
	});
	var orderFoodStatPanelGridTbar = new Ext.Toolbar({
		height : 26,
		items : [ {xtype:'tbtext',text:'班次:'}, duty,
		{xtype:'tbtext',text:'&nbsp;&nbsp;&nbsp;&nbsp;'},
		'->', {
			text : '搜索',
			iconCls : 'btn_search',
			id : 'salesSubBtnSearch',
			handler : function(){
				if(!duty.isValid()){
					Ext.example.msg('提示', '请选择一个班次再操作.');
					return;
				}
				var gs = Ext.getCmp('orderFoodStatPanelGrid').getStore();
				gs.baseParams['dateBeg'] = duty.getValue().split(salesSubSplitSymbol)[0];
				gs.baseParams['dateEnd'] = duty.getValue().split(salesSubSplitSymbol)[1];
				gs.baseParams['deptID'] = salesSubDeptId;
				gs.load({
					params : {
						start : 0,
						limit : 15
					}
				});
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
         ['菜品','food.foodName', 150], 
         ['销量','salesAmount',,'right','Ext.ux.txtFormat.gridDou'], 
         ['营业额','income',,'right','Ext.ux.txtFormat.gridDou'], 
         ['折扣额','discount',,'right','Ext.ux.txtFormat.gridDou'], 
         ['赠送额','gifted',,'right','Ext.ux.txtFormat.gridDou']
		],
		['food', 'food.foodName', 'salesAmount', 'income', 'discount', 'gifted'],
		[['pin', pin], ['isPaging', true], ['restaurantID', restaurantID], ['dataType', 0], ['queryType', 1]],
		15,
		'',
		orderFoodStatPanelGridTbar
	);
	orderFoodStatPanelGrid.region = 'center';
	
	orderFoodStatPanel = new Ext.Panel({
		title : '菜品统计',
		layout : 'border',
		items : [orderFoodStatPanelDeptTree, orderFoodStatPanelGrid]
	});	
}

function kitchenGroupTextTpl(rs){
	return '部门:'+rs[0].get('dept.deptName');
}

function kitchenStatPanelInit(){
	var duty = createStatGridTabDutyFn({
		data:shiftDutyOfToday
	});
	var kitchenStatPanelGridTbar = new Ext.Toolbar({
		height : 26,
		items : [ {xtype:'tbtext',text:'班次:'}, duty, '->', {
			text : '展开/收缩',
			iconCls : 'icon_tb_toggleAllGroups',
			handler : function(){
				kitchenStatPanelGrid.getView().toggleAllGroups();
			}
		}, {
			text : '搜索',
			iconCls : 'btn_search',
			handler : function(){
				if(!duty.isValid()){
					Ext.example.msg('提示', '请选择一个班次再操作.');
					return;
				}
				var gs = kitchenStatPanelGrid.getStore();
				gs.baseParams['dateBeg'] = duty.getValue().split(salesSubSplitSymbol)[0];
				gs.baseParams['dateEnd'] = duty.getValue().split(salesSubSplitSymbol)[1];
				gs.removeAll();
				gs.load();
				kitchenStatPanelGrid.getView().expandAllGroups();
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
	     ['分厨','kitchen.kitchenName'], 
	     ['营业额','income',,'right','Ext.ux.txtFormat.gridDou'], 
	     ['折扣额','discount',,'right','Ext.ux.txtFormat.gridDou'], 
	     ['赠送额','gifted',,'right','Ext.ux.txtFormat.gridDou'],
	     ['dept.deptID','dept.deptID', 10]
		],
		['income','discount','gifted', 'kitchen', 'kitchen.kitchenName', 'dept', 'dept.deptID', 'dept.deptName'],
		[['pin', pin], ['restaurantID', restaurantID], ['dataType', 0], ['queryType', 2]],
		15,
		{
			name : 'dept.deptID',
			hide : true,
			sort : 'dept.deptID'
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
		data:shiftDutyOfToday
	});
	var deptStatPanelGridTbar = new Ext.Toolbar({
		height : 26,
		items : [ {xtype:'tbtext',text:'班次:'}, duty, '->', {
			text : '搜索',
			iconCls : 'btn_search',
			handler : function(){
				if(!duty.isValid()){
					Ext.example.msg('提示', '请选择一个班次再操作.');
					return;
				}
				var gs = deptStatPanelGrid.getStore();
				gs.baseParams['dateBeg'] = duty.getValue().split(salesSubSplitSymbol)[0];
				gs.baseParams['dateEnd'] = duty.getValue().split(salesSubSplitSymbol)[1];
				gs.removeAll();
				gs.load();
			}
		}]
	});
	
	var deptStatPanelGrid = createGridPanel(
		'',
		'',
		'',
		'',
		'../../SalesSubStatistics.do',
		[[true, false, false, true], 
	     ['部门','dept.deptName'],
	     ['营业额','income',,'right','Ext.ux.txtFormat.gridDou'], 
	     ['折扣额','discount',,'right','Ext.ux.txtFormat.gridDou'], 
	     ['赠送额','gifted',,'right','Ext.ux.txtFormat.gridDou']
		],
		['income','discount','gifted', 'dept', 'dept.deptName'],
		[['pin', pin], ['restaurantID', restaurantID], ['dataType', 0]],
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
		width : 800,
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

/*salesSub = function(){	
	
	var cmData = [[true, false, false, true], 
	              ['部门','item'], ['销量','salesAmount','','right','Ext.ux.txtFormat.gridDou'], ['均价','avgPrice','','right','Ext.ux.txtFormat.gridDou'], ['单位成本','avgCost','','right','Ext.ux.txtFormat.gridDou'],
	              ['营业额','income','','right','Ext.ux.txtFormat.gridDou'], ['折扣额','discount','','right','Ext.ux.txtFormat.gridDou'], ['赠送额','gifted','','right','Ext.ux.txtFormat.gridDou'],
	              ['成本','cost','','right','Ext.ux.txtFormat.gridDou'], ['成本率','costRate','','right','Ext.ux.txtFormat.gridDou'], ['毛利','profit','','right','Ext.ux.txtFormat.gridDou'], 
	              ['毛利率','profitRate','','right','Ext.ux.txtFormat.gridDou']];
	var url = '../../SalesSubStatistics.do?tiem='+new Date();
	var readerData = ['item','income','discount','gifted','cost','costRate','profit','profitRate','salesAmount','avgPrice','avgCost'];
	var baseParams = [['pin', pin], ['restaurantID', restaurantID], ['dataType', 0]];
	var pageSize = 15;
	var id = 'salesSub_grid';
	var title = '';
	var height = '';
	var width = '';
	var groupName = '';
	
	var salesSubGrid_tbar = new Ext.Toolbar({
		buttonAlign : 'left',
		height : 26,
		items : [
		{xtype:'tbtext', text:String.format(Ext.ux.txtFormat.typeName, '类别', 'salesSubShowType', '') },
		{xtype:'tbtext',text:'&nbsp;&nbsp;'},
		{xtype:'tbtext',text:'班次:'},
		{
 	    	xtype : 'combo',
 	    	id : 'comboDuty',
 	    	width : 330,
 	    	store : new Ext.data.JsonStore({
 	    		root : 'root',
				fields : [ 'duty', 'displayMsg' ]
			}),
			valueField : 'duty',
			displayField : 'displayMsg',
			mode : 'local',
			triggerAction : 'all',
			typeAhead : true,
			selectOnFocus : true,
			forceSelection : true,
			allowBlank : false,
			readOnly : true,
			selectOnFocus : true
 	    },
		{xtype:'tbtext',text:'&nbsp;&nbsp;&nbsp;&nbsp;'},
		{
			xtype : 'radio',
			hideLabel : true,
			width : 100,
			boxLabel : "按毛利排序",
			name : 'salesSubGridOrderByRadio',
			id : 'salesSubGridOrderByRadioProsit',
			checked : true,
			inputValue : '0',
			listeners : {
				check : function(e){
					if(e.getValue() == true){
						salesSubOrderType = e.getRawValue();
					}
				}
			}
		},
		{
			xtype : 'radio',
			hideLabel : true,
			width : 100,
			boxLabel : '按销量排序',
			id : 'salesSubGridOrderByRadioSales',
			name : 'salesSubGridOrderByRadio',				
			inputValue : '1',
			listeners : {
				check : function(e){
					if(e.getValue() == true){
						salesSubOrderType = e.getRawValue();
					}
				},
				blur : function(e){
					
				}
			}
		},						
		'->',
		{
			text : '搜索',
			iconCls : 'btn_search',
			id : 'salesSubBtnSearch',
			width : 150,
			handler : function(){
				var duty = Ext.getCmp('comboDuty');
				if(!duty.isVisible()){
					Ext.example.msg('提示', '请选择一个班次再操作.');
					return;
				}
				var gs = salesSubGrid.getStore();
				gs.baseParams['dateBeg'] = duty.getValue().split(salesSubSplitSymbol)[0];
				gs.baseParams['dateEnd'] = duty.getValue().split(salesSubSplitSymbol)[1];
				gs.baseParams['queryType'] = salesSubQueryType;
				gs.baseParams['orderType'] = salesSubOrderType;
				gs.baseParams['deptID'] = salesSubDeptId;
				gs.removeAll();
				gs.load({params:{start:0,limit:15}});
				
				salesSubSetColumn();
			}
		}
		]
	});
	
	var salesSubGrid = createGridPanel(id,title,height,width,url,cmData,readerData,baseParams,pageSize,groupName,salesSubGrid_tbar);
	salesSubGrid.region = 'center';
	salesSubGrid.getStore().on('load', function(store, records, options){
		if(store.getCount() > 0){
			var sumRow = salesSubGrid.getView().getRow(store.getCount()-1);	
			sumRow.style.backgroundColor = '#EEEEEE';			
			sumRow.style.color = 'green';
			for(var i = 0; i < salesSubGrid.getColumnModel().getColumnCount(); i++){
				var sumRow = salesSubGrid.getView().getCell(store.getCount()-1, i);
				sumRow.style.fontSize = '15px';
				sumRow.style.fontWeight = 'bold';					
			}
			salesSubGrid.getView().getCell(store.getCount()-1, 3).innerHTML = '--';
			salesSubGrid.getView().getCell(store.getCount()-1, 4).innerHTML = '--';
		}
	});
	
	var salesSubMuneTree_tbar = new Ext.Toolbar({
		buttonAlign : 'left',
		height : 23,
		items : [
		'->',
		{
			xtype : 'radio',
			hideLabel : true,
			width : 80,
			boxLabel : '部门汇总',
			id : 'salesSubMuneTreeTypeRadioDept',
			name : 'salesSubMuneTreeTypeRadio',
			inputValue : '0',
			checked : true,
			listeners : {
				check : function(e){						
					if(e.getValue() == true){						
						salesSubSetDisplay(true, e.boxLabel,  e.getRawValue(), true, '部门' );
						Ext.getCmp('salesSubGridOrderByRadioProsit').setValue(true);							
						Ext.getCmp('salesSubMuneTree').root.select();							
						salesSubDeptId = '';
					}
				}
			}
		},
		{
			xtype : 'radio',
			hideLabel : true,
			width : 80,
			boxLabel : '菜品明细',
			name : 'salesSubMuneTreeTypeRadio',
			inputValue : '1',
			listeners : {
				check : function(e){						
					if(e.getValue() == true){							
						salesSubSetDisplay(false, '全部菜品', e.getRawValue(), false,'菜品' );
						Ext.getCmp('salesSubGridOrderByRadioProsit').setValue(true);	
						salesSubDeptId = '';
						var root = Ext.getCmp('salesSubMuneTree').getRootNode();							
						if(root.hasChildNodes()){
							for(var i = 0; i <  root.childNodes.length; i++){
								salesSubDeptId += (i > 0 ? ',' : '');
	        					salesSubDeptId += root.childNodes[i].attributes.deptID;
							}
						}
					}
				}
			}
		}
		]
	});
	
	var salesSubMuneTree = new Ext.tree.TreePanel({
		id : 'salesSubMuneTree',
		border : false,
		rootVisible : true,
		height : 410,	
		animate : true,
		loader:new Ext.tree.TreeLoader({    
	          dataUrl:'../../QueryDeptTree.do?time='+new Date(),
	          baseParams : {
					'restaurantID' : restaurantID
				}
	       }),
		root: new Ext.tree.AsyncTreeNode({
			expanded : true,
            text : '全部菜品',
            leaf : false,
            deptID : '-1'
		}),
        listeners : {
        	load : function(){
        		var treeRoot = salesSubMuneTree.getRootNode().childNodes;
        		for(var i = (treeRoot.length - 1); i >= 0; i--){
					if(treeRoot[i].attributes.deptID == 253){
						salesSubMuneTree.getRootNode().removeChild(treeRoot[i]);
					}
				}
        	},
        	click : function(e){
        		if(e.attributes.deptID == '' || e.attributes.deptID == '-1'){
        			salesSubSetDisplay(false, e.text, 1, false, '菜品');
        			Ext.getCmp('salesSubGridOrderByRadioProsit').setValue(true);
        			salesSubDeptId = '';
        			if(e.hasChildNodes()){
        				for(var i = 0; i < e.childNodes.length; i++){
        					salesSubDeptId += (i > 0 ? ',' : '');
        					salesSubDeptId += e.childNodes[i].attributes.deptID;
        				}
        			}	        				        		
        		}else{
        			salesSubSetDisplay(false, e.text, 1, false, '菜品');
        			salesSubDeptId = e.attributes.deptID;
        		}	        		
        	},
        	dblclick : function(e){
        		Ext.getCmp('salesSubBtnSearch').handler();
        	}
        }
	});
	var salesSubMune = new Ext.Panel({
		region : 'west',
		width : 160,			
		border : false,
		items : [{xtype:'panel', tbar:salesSubMuneTree_tbar, border:false}, salesSubMuneTree]
	});
	
	
	
	var salesSubWin = new Ext.Window({
		title : '销售统计',
		layout : 'border',
		resizable : false,
		modal : true,
		closable : false,
		constrainHeader : true,
		draggable : false,
		width : 1200,
		height : 500,
		items : [salesSubMune,salesSubGrid],
		buttons : [
		{
			text : '打印',
			disabled : true,
			handler : function(){
				
			}
		},
		{
			text : '退出',
			handler : function(){
				salesSubWin.close();
			}
		}],
		listeners : {
			show : function(){
				Ext.Ajax.request({
					url : '../../QueryDutyRangeByNow.do',
					params : {
						pin : pin
					},
					success : function(res, opt){
						var jr = Ext.util.JSON.decode(res.responseText);
						var bd = {root:[]};
						for(var i = 0; i < jr.root.length; i++){
							bd.root.push({
								duty : jr.root[i].onDuty + salesSubSplitSymbol + jr.root[i].offDuty,
								displayMsg : (jr.root[i].onDuty + ' -- ' + jr.root[i].offDuty + ' (' + jr.root[i].name + ')')
							});
						}
						Ext.getCmp('comboDuty').store.loadData(bd);
						Ext.getCmp('comboDuty').setValue(bd.root[0].duty);
						Ext.getCmp('salesSubBtnSearch').handler();
					},
					failure : function(res, opt){
						Ext.ux.showMsg(Ext.util.JSON.decode(res.responseText));
					}
				});
			}
		}
	});
		
	salesSubWin.show();	
	Ext.getCmp('salesSubMuneTreeTypeRadioDept').setValue(true);
	Ext.getCmp('salesSub_grid').getStore().removeAll();
	salesSubSetColumn();
};

salesSubSearchCheckDate = function(_s, _bid, _eid, _num){
	var beginDate = Ext.getCmp(_bid);
	var endDate = Ext.getCmp(_eid);
	var bdv = null, edv = null;
	var day = typeof(_num) != 'undefined' ? _num : 40;
	
	if(typeof(beginDate) == 'undefined' || typeof(endDate) == 'undefined'){
		return false;
	}
	
	bdv = beginDate.getValue();
	edv = endDate.getValue();
	
	if(bdv == '' && edv == ''){
		return false;
	}else{		
		if(_s){
			if(edv == '' || bdv > edv ){
				endDate.setRawValue(beginDate.getRawValue());
			}else if((bdv.add(Date.DAY, day) < edv )){
				endDate.setRawValue(beginDate.getValue().add(Date.DAY, day).format('Y-m-d'));
			}
		}else if(!_s){
			if(bdv == '' || edv < bdv){
				beginDate.setRawValue(endDate.getRawValue());
			}else if((edv.add(Date.DAY, (day * -1)) > bdv )){
				beginDate.setRawValue(endDate.getValue().add(Date.DAY, (day * -1)).format('Y-m-d'));
			}
		}
	}
};

salesSubSetColumn = function(){
	var grid = Ext.getCmp('salesSub_grid');
	var colHide = true, colWidth = 0;				
	if(salesSubQueryType == 0){
		colHide = true;
		colWidth = 0;
	}else{
		colHide = false;
		colWidth = 80;
	}					
	grid.getColumnModel().setHidden(2, colHide);
	grid.getColumnModel().setHidden(3, colHide);
	grid.getColumnModel().setHidden(4, colHide);
	grid.getColumnModel().setColumnWidth(2, colWidth);
	grid.getColumnModel().setColumnWidth(3, colWidth);
	grid.getColumnModel().setColumnWidth(4, colWidth);
	
};

salesSubSetDisplay = function(_tree, _queryTypeName, _queryType, _orderType, _colName){
	Ext.getCmp('salesSubMuneTree').setDisabled(_tree);
	Ext.getDom('salesSubShowType').innerHTML = _queryTypeName;
	salesSubQueryType = _queryType;
	Ext.getCmp('salesSubGridOrderByRadioSales').setDisabled(_orderType);
	Ext.getCmp('salesSub_grid').getColumnModel().setColumnHeader(1, _colName);
};*/

