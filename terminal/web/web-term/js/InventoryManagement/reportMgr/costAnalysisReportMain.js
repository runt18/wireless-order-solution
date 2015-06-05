
function onLoad(){
//	Ext.getCmp('cost_analysis').getStore().load();
	Ext.getCmp('btnSearch').handler();
}

var suppllierGridTbar;
Ext.onReady(function(){
	var cm = new Ext.grid.ColumnModel([
       new Ext.grid.RowNumberer(),
       {header: '部门 ', dataIndex: 'deptName'},
       {header: '期初余额', dataIndex: 'primeMoney', align : 'right',  renderer : Ext.ux.txtFormat.gridDou},
       {header: '领料金额', dataIndex: 'useMaterialMoney', align : 'right', renderer : Ext.ux.txtFormat.gridDou},
       {header: '退料金额', dataIndex: 'stockOutMoney', align : 'right', renderer : Ext.ux.txtFormat.gridDou},
       {header: '拨出金额', dataIndex: 'stockOutTransferMoney', align : 'right', width : 120, renderer : Ext.ux.txtFormat.gridDou},
       {header: '期末金额', dataIndex: 'endMoney', align : 'right', width : 130, renderer : Ext.ux.txtFormat.gridDou},
       {header: '成本金额', dataIndex: 'costMoney', align : 'right', width : 130, renderer : Ext.ux.txtFormat.gridDou},
       {header: '销售金额', dataIndex: 'salesMoney', align : 'right', width : 130, renderer : Ext.ux.txtFormat.gridDou},
       {header: '毛利额', dataIndex: 'profit', align : 'right', width : 150, renderer : Ext.ux.txtFormat.gridDou},
       {header: '毛利率', id: 'profitRate', dataIndex: 'profitRate', align : 'right', renderer : Ext.ux.txtFormat.percent}
        
	]);
	cm.defaultSortable = true;
	
	var ds = new Ext.data.Store({
		proxy : new Ext.data.HttpProxy({url: '../../QueryCostAnalyzeReport.do' }),
		reader : new Ext.data.JsonReader({totalProperty: 'totalProperty', root:'root'},[
				{name: 'deptName'},
				{name: 'primeMoney'},
				{name: 'useMaterialMoney'},
				{name: 'stockOutMoney'},
				{name: 'stockOutTransferMoney'},
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
			xtype : 'datefield',
			id : 'car_beginDate',
			allowBlank : false,
			format : 'Y-m',
			value : new Date(),
			maxValue : new Date(),
			width : 100
		},'->', {
			text : '搜索',
			id : 'btnSearch',
			iconCls : 'btn_search',
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
//	    viewConfig : {
//	    	forceFit : true
//	    },
	    tbar : costAnalyzeGridTbar,
	    bbar : pagingBar
	});
	

	costAnalyzeGrid.region = 'center';
	
	new Ext.Panel({
		renderTo : 'divCostAnalysis',
		//width : parseInt(Ext.getDom('divCostAnalysis').parentElement.style.width.replace(/px/g,'')),
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
	
	onLoad();
});	