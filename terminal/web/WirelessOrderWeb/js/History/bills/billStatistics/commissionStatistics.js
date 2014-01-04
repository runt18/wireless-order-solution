
function commissionDetailInit(){
	var commission_beginDate = new Ext.form.DateField({
		xtype : 'datefield',		
		format : 'Y-m-d',
		width : 100,
		maxValue : new Date(),
		readOnly : true,
		allowBlank : false
	});
	var commission_endDate = new Ext.form.DateField({
		xtype : 'datefield',
		format : 'Y-m-d',
		width : 100,
		maxValue : new Date(),
		readOnly : true,
		allowBlank : false
	});
	var commission_dateCombo = Ext.ux.createDateCombo({
		beginDate : commission_beginDate,
		endDate : commission_endDate,
		callback : function(){
			Ext.getCmp('btnSearchForCommissionStatistics').handler();
		}
	});
	
	var commission_combo_staffs = new Ext.form.ComboBox({
		id : 'commission_combo_staffs',
		readOnly : true,
		forceSelection : true,
		width : 103,
		listWidth : 120,
		store : new Ext.data.SimpleStore({
			fields : ['staffID', 'staffName']
		}),
		valueField : 'staffID',
		displayField : 'staffName',
		typeAhead : true,
		mode : 'local',
		triggerAction : 'all',
		selectOnFocus : true,
		listeners : {
			render : function(thiz){
				var data = [[-1,'全部']];
				Ext.Ajax.request({
					url : '../../QueryStaff.do',
					success : function(res, opt){
						var jr = Ext.decode(res.responseText);
						for(var i = 0; i < jr.root.length; i++){
							data.push([jr.root[i]['staffID'], jr.root[i]['staffName']]);
						}
						thiz.store.loadData(data);
						thiz.setValue(-1);
					},
					fialure : function(res, opt){
						thiz.store.loadData(data);
						thiz.setValue(-1);
					}
				});
			},
			select : function(){
				Ext.getCmp('btnSearchForCommissionStatistics').handler();
			}
		}
	});
	//------------------------tree
	commissionDeptTree = new Ext.tree.TreePanel({
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
					commissionDeptTree.getRootNode().reload();
				}
			}]
		}),
		listeners : {
			click : function(e){
				Ext.getCmp('btnSearchForCommissionStatistics').handler(e.attributes.deptID);
				Ext.getDom('lblCommissionDept').innerHTML = e.text;
			}
		}
	});
	//---------------------grid
	var cm = new Ext.grid.ColumnModel([
		new Ext.grid.RowNumberer(),
		{header : '日期', dataIndex : 'orderDateFormat'},
		{header : '菜名', dataIndex : 'foodName'},
		{header : '部门', dataIndex : 'dept'},
		{header : '账单号', dataIndex : 'orderId'},
		{header : '单价', dataIndex : 'unitPrice', align : 'right', renderer : Ext.ux.txtFormat.gridDou},
		{header : '数量', dataIndex : 'amount', align : 'right', renderer : Ext.ux.txtFormat.gridDou},
		{header : '总额', dataIndex : 'totalPrice', align : 'right', renderer : Ext.ux.txtFormat.gridDou},
		{header : '提成', dataIndex : 'commission', align : 'right', renderer : Ext.ux.txtFormat.gridDou},
		{header : '人员', dataIndex : 'staffName'}
	]);
	
	cm.defaultSortable = true;
	
	var ds = new Ext.data.Store({
		proxy : new Ext.data.HttpProxy({url : '../../QueryCommissionStatistics.do'}),
		reader : new Ext.data.JsonReader({totalProperty : 'totalProperty', root : 'root'}, [
		{name : 'orderDateFormat'},
		{name : 'foodName'},
		{name : 'dept'},
		{name : 'orderId'},
		{name : 'unitPrice'},
		{name : 'amount'},
		{name : 'totalPrice'},
		{name : 'commission'},
		{name : 'staffName'}
		])
		
	});
	
	var commissionStatisticsTbar = new Ext.Toolbar({
		items : [{
				xtype : 'tbtext',
				text : String.format(
					Ext.ux.txtFormat.typeName,
					'部门','lblCommissionDept','----'
				)
			},{
				xtype : 'tbtext',
				text : '日期:'
			}, commission_dateCombo, {
				xtype : 'tbtext',
				text : '&nbsp;'
			}, commission_beginDate , {
				xtype : 'tbtext',
				text : '&nbsp;至&nbsp;'
			}, commission_endDate, {
				xtype : 'tbtext',
				text : '&nbsp;&nbsp;'
			},{
				xtype : 'tbtext',
				text : '操作人员:'
			}, commission_combo_staffs, '->', {
				text : '搜索',
				id : 'btnSearchForCommissionStatistics',
				iconCls : 'btn_search',
				handler : function(e){
					if(!commission_beginDate.isValid() || !commission_endDate.isValid()){
						return;
					}
					var store = commissionStatisticsGrid.getStore();
					store.baseParams['beginDate'] = commission_beginDate.getValue().format('Y-m-d 00:00:00');
					store.baseParams['endDate'] = commission_endDate.getValue().format('Y-m-d 23:59:59');
					store.baseParams['staffId'] = commission_combo_staffs.getValue();
					store.baseParams['deptId'] = e?e : "-1";
					store.load({
						params : {
							start : 0,
							limit : limitCount
						}
					});
				}
			}]
	});
	var pagingBar = new Ext.PagingToolbar({
	   pageSize : limitCount,	//显示记录条数
	   store : ds,	//定义数据源
	   displayInfo : true,	//是否显示提示信息
	   displayMsg : "显示第 {0} 条到 {1} 条记录，共 {2} 条",
	   emptyMsg : "没有记录"
	});
	
	commissionStatisticsGrid = new Ext.grid.GridPanel({
		id : 'commission_grid',
	    //height : '500',
	    border : true,
	    frame : true,
	    store : ds,
	    cm : cm,
	    viewConfig : {
	    	forceFit : true
	    },
	    loadMask : {
	    	msg : "数据加载中，请稍等..."
	    },
	    tbar : commissionStatisticsTbar,
	    bbar : pagingBar
	});
	commissionStatisticsGrid.region = 'center';
	
	commissionDetailPanel = new Ext.Panel({
		title : '提成明细',
		layout:'border',
		frame : false, //边框
		//子集
		items : [commissionDeptTree, commissionStatisticsGrid]
	});
}



function commissionTotalInit(){
	var commissionTotal_beginDate = new Ext.form.DateField({
		xtype : 'datefield',		
		format : 'Y-m-d',
		width : 100,
		maxValue : new Date(),
		readOnly : true,
		allowBlank : false
	});
	var commissionTotal_endDate = new Ext.form.DateField({
		xtype : 'datefield',
		format : 'Y-m-d',
		width : 100,
		maxValue : new Date(),
		readOnly : true,
		allowBlank : false
	});
	var commissionTotal_dateCombo = Ext.ux.createDateCombo({
		beginDate : commissionTotal_beginDate,
		endDate : commissionTotal_endDate,
		callback : function(){
			Ext.getCmp('btnSearchForCommissionTotal').handler();
		}
	});
	//--------tree
	commissionTotalDeptTree = new Ext.tree.TreePanel({
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
					commissionTotalDeptTree.getRootNode().reload();
				}
			}]
		}),
		listeners : {
			click : function(e){
				Ext.getCmp('btnSearchForCommissionTotal').handler(e.attributes.deptID);
				Ext.getDom('lblCommissionTotalDept').innerHTML = e.text;
			}
		}
	});
	//--------grid
	var cm = new Ext.grid.ColumnModel([
		new Ext.grid.RowNumberer(),
		{header : '销售总额', dataIndex : 'totalPrice', width : 200, align : 'right', renderer : Ext.ux.txtFormat.gridDou},
		{header : '提成总额', dataIndex : 'commission', width : 200, align : 'right', renderer : Ext.ux.txtFormat.gridDou},
		{header : '人员', width : 200, dataIndex : 'staffName'}
	]);
	
	cm.defaultSortable = true;
	
	var ds = new Ext.data.Store({
		proxy : new Ext.data.HttpProxy({url : '../../QueryCommissionTotal.do'}),
		reader : new Ext.data.JsonReader({totalProperty : 'totalProperty', root : 'root'}, [
		{name : 'totalPrice'},
		{name : 'commission'},
		{name : 'staffName'}
		])
		
	});
	
	var commissionTotalStatisticsTbar = new Ext.Toolbar({
		items : [{
				xtype : 'tbtext',
				text : String.format(
					Ext.ux.txtFormat.typeName,
					'部门','lblCommissionTotalDept','----'
				)
			},{
				xtype : 'tbtext',
				text : '日期:'
			}, commissionTotal_dateCombo, {
				xtype : 'tbtext',
				text : '&nbsp;'
			}, commissionTotal_beginDate , {
				xtype : 'tbtext',
				text : '&nbsp;至&nbsp;'
			}, commissionTotal_endDate, {
				xtype : 'tbtext',
				text : '&nbsp;&nbsp;'
			},'->', {
				text : '搜索',
				id : 'btnSearchForCommissionTotal',
				iconCls : 'btn_search',
				handler : function(e){
					if(!commissionTotal_beginDate.isValid() || !commissionTotal_endDate.isValid()){
						return;
					}
					var store = commissionTotalStatisticsGrid.getStore();
					store.baseParams['beginDate'] = commissionTotal_beginDate.getValue().format('Y-m-d 00:00:00');
					store.baseParams['endDate'] = commissionTotal_endDate.getValue().format('Y-m-d 23:59:59');
					store.baseParams['deptId'] = e?e : "-1";
					store.load({
						params : {
							start : 0,
							limit : limitCount
						}
					});
				}
			}]
	});
	var pagingBar = new Ext.PagingToolbar({
	   pageSize : limitCount,	//显示记录条数
	   store : ds,	//定义数据源
	   displayInfo : true,	//是否显示提示信息
	   displayMsg : "显示第 {0} 条到 {1} 条记录，共 {2} 条",
	   emptyMsg : "没有记录"
	});
	
	commissionTotalStatisticsGrid = new Ext.grid.GridPanel({
		id : 'commissionTotal_grid',
	    //height : '500',
	    border : true,
	    frame : true,
	    store : ds,
	    cm : cm,
//	    viewConfig : {
//	    	forceFit : true
//	    },
	    loadMask : {
	    	msg : "数据加载中，请稍等..."
	    },
	    tbar : commissionTotalStatisticsTbar,
	    bbar : pagingBar
	});
	commissionTotalStatisticsGrid.region = 'center';
	
	commissionTotalPanel = new Ext.Panel({
		id : 'commissionTotalStatisticsPanel',
		title : '提成汇总',
		layout:'border',
		frame : false, //边框
		//子集
		items : [commissionTotalDeptTree, commissionTotalStatisticsGrid]
	});
	
}

Ext.onReady(function(){
	commissionDetailInit();
	commissionTotalInit();
	new Ext.TabPanel({
		renderTo : 'divCommissionStatistics',//渲染到
		id : 'commissionStatisticsPanel',
		//solve不跟随窗口的变化而变化
		width : parseInt(Ext.getDom('divCommissionStatistics').parentElement.style.width.replace(/px/g,'')),
		height : parseInt(Ext.getDom('divCommissionStatistics').parentElement.style.height.replace(/px/g,'')),
		border : false,
		//子集
		items : [commissionDetailPanel, commissionTotalPanel],
		listeners : {
			render : function(thiz){
				thiz.setActiveTab(commissionDetailPanel);
			}
		}
	});
//	repaidStatisticsGrid.getStore().load();
});