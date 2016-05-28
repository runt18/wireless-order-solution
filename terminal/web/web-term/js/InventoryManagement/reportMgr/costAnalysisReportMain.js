Ext.onReady(function(){
	var suppllierGridTbar;
	var cm = new Ext.grid.ColumnModel([
       new Ext.grid.RowNumberer(),
       {header: '部门 ', dataIndex: 'deptName'},
       {header: '期初余额', dataIndex: 'primeMoney', align : 'right',  renderer : Ext.ux.txtFormat.gridDou},
       {header: '采购金额', dataIndex: 'useMaterialMoney', align : 'right', renderer : Ext.ux.txtFormat.gridDou},
       {header: '领料金额', dataIndex: 'stockInTransferMoney', align : 'right', renderer : Ext.ux.txtFormat.gridDou},
       {header: '其他入库额', dataIndex: 'stockSpillMoney', align : 'right', renderer : Ext.ux.txtFormat.gridDou},
       {header: '盘盈金额', dataIndex: 'stockTakeMoreMoney', align : 'right', renderer : Ext.ux.txtFormat.gridDou},
       {header: '退货金额', dataIndex: 'stockOutMoney', align : 'right', renderer : Ext.ux.txtFormat.gridDou},
       {header: '退料金额', dataIndex: 'stockOutTransferMoney', align : 'right', renderer : Ext.ux.txtFormat.gridDou},
       {header: '其他出库额', dataIndex: 'stockDamageMoney', align : 'right', renderer : Ext.ux.txtFormat.gridDou},
       {header: '盘亏金额', dataIndex: 'stockTakeLessMoney', align : 'right', renderer : Ext.ux.txtFormat.gridDou},
       {header: '期末金额', dataIndex: 'endMoney', align : 'right', width : 130, renderer : Ext.ux.txtFormat.gridDou},
       {header: '成本金额', dataIndex: 'costMoney', align : 'right', renderer : Ext.ux.txtFormat.gridDou},
       {header: '销售金额', dataIndex: 'salesMoney', align : 'right', renderer : Ext.ux.txtFormat.gridDou},
       {header: '毛利额', dataIndex: 'profit', align : 'right', renderer : Ext.ux.txtFormat.gridDou},
       {header: '毛利率', dataIndex: 'profitRate', align : 'right', renderer : Ext.ux.txtFormat.gridDou}
       
	]);
	cm.defaultSortable = true;
	
	var ds = new Ext.data.Store({
		proxy : new Ext.data.HttpProxy({url: '../../QueryCostAnalyzeReport.do' }),
		reader : new Ext.data.JsonReader({totalProperty: 'totalProperty', root:'root'},[
				{name: 'deptName'},
				{name: 'primeMoney'},
				{name: 'useMaterialMoney'},
				{name: 'stockInTransferMoney'},
				{name: 'stockSpillMoney'},
				{name: 'stockTakeMoreMoney'},
				{name: 'stockOutMoney'},
				{name: 'stockOutTransferMoney'},
				{name: 'stockDamageMoney'},
				{name: 'stockTakeLessMoney'},
				{name: 'endMoney'},
				{name: 'costMoney'},
				{name: 'salesMoney'},
				{name: 'profit'},
				{name: 'profitRate'}
		])
	});
	
//	var date = new Date();
//	date.setMonth(date.getMonth()-1);
	
	costAnalyzeGridTbar = new Ext.Toolbar({
		items : [
		{ xtype:'tbtext', text:'日期:'},
		{
			id : 'car_beginDate',
			xtype: 'datefield',  
			allowBlank : false,
			maxValue : new Date(),
			value : new Date(),
            width:100,  
            plugins: 'monthPickerPlugin',  
            format: 'Y-m'
            //editable: false
		},'->', {
			text : '刷新',
			id : 'btnSearch',
			iconCls : 'btn_refresh',
			handler : function(){
				var gs = costAnalyzeGrid.getStore();
				gs.baseParams['beginDate'] = Ext.getCmp('car_beginDate').getValue().format('Y-m');
				gs.load();
			}
		}]
	});
	
	var pagingBar = new Ext.PagingToolbar({
	   pageSize : 13,	//显示记录条数
	   store : ds,	//定义数据源
	   displayInfo : true,	//是否显示提示信息
	   displayMsg : "显示第 {0} 条到 {1} 条记录，共 {2} 条",
	   emptyMsg : "没有记录"
	});
	
	var costAnalyzeGrid = new Ext.grid.GridPanel({
		title : '成本分析表',
		id : 'cost_analysis',
	    height : '500',
	    border : true,
	    frame : true,
	    store : ds,
	    loadMask : {
	    	msg : "数据加载中，请稍等..."
	    },
	    cm : cm,
	    autoExpandColumn : 'profitRate',
	    autoExpandMax : 200,
	    viewConfig : {
	    	forceFit : true
	    },
	    tbar : costAnalyzeGridTbar,
	    bbar : pagingBar
	});
	

	costAnalyzeGrid.region = 'center';
	
	costAnalyzeGrid.getStore().on('load', function(store, records, options){
		
		if(store.getCount() > 0){
			var sumRow = costAnalyzeGrid.getView().getRow(store.getCount() - 1);	
			sumRow.style.backgroundColor = '#EEEEEE';			
			sumRow.style.color = 'green';
			for(var i = 0; i < costAnalyzeGrid.getColumnModel().getColumnCount(); i++){
				var sumRow = costAnalyzeGrid.getView().getCell(store.getCount() - 1, i);
				sumRow.style.fontSize = '15px';
				sumRow.style.fontWeight = 'bold';					
			}
			costAnalyzeGrid.getView().getCell(store.getCount()-1, 1).innerHTML = '汇总';
//			costAnalyzeGrid.getView().getCell(store.getCount()-1, 10).innerHTML = '--';
//			costAnalyzeGrid.getView().getCell(store.getCount()-1, 11).innerHTML = '--';
		}
	});	
	
	
	new Ext.Panel({
		renderTo : 'divCostAnalysis',
		height : parseInt(Ext.getDom('divCostAnalysis').parentElement.style.height.replace(/px/g,'')),
		layout : 'border',
		//子集
		items : [costAnalyzeGrid],
		keys : [{
			key : Ext.EventObject.ENTER,
			scope : this,
			fn : function(){
				Ext.getCmp('btnSearch').handler();
			}
		}]
	});
	
	//页面打开即加载数据
//	Ext.getCmp('btnSearch').handler();
});	