
function onLoad(){
	Ext.getCmp('grid').getStore().load();
}

var suppllierGridTbar;
Ext.onReady(function(){
	Ext.BLANK_IMAGE_URL = '../../extjs/resources/images/default/s.gif';
	Ext.QuickTips.init();
	Ext.form.Field.prototype.msgTarget = 'side';
	
	var cm = new Ext.grid.ColumnModel([
       new Ext.grid.RowNumberer(),
       {header: '部门 ', dataIndex: 'deptName', width:120},
       {header: '期初余额', dataIndex: 'primeMoney'},
       {header: '领料金额', dataIndex: 'useMaterialMoney'},
       {header: '退料金额', dataIndex: 'stockOutMoney'},
       {header: '拨出金额', dataIndex: 'stockOutTransferMoney'},
       {header: '期末金额', dataIndex: 'endMoney'},
       {header: '成本金额', dataIndex: 'costMoney'},
       {header: '销售金额', dataIndex: 'salesMoney', width:130},
       {header: '毛利额', dataIndex: 'profit', width:130},
       {header: '毛利率', dataIndex: 'profitRate', id : 'rate', width:130}
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
	
	var date = new Date();
	date.setMonth(date.getMonth()-1);
	
	costAnalyzeGridTbar = new Ext.Toolbar({
		items : [
		{ xtype:'tbtext', text:'日期:'},
		{
			xtype : 'datefield',
			id : 'car_beginDate',
			allowBlank : false,
			format : 'Y-m',
			value : date,
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
		id : 'grid',
	    height : '500',
	    border : true,
	    frame : true,
	    store : ds,
	    loadMask : {
	    	msg : "数据加载中，请稍等..."
	    },
	    cm : cm,
	    viewConfig : {
	    	forceFit : true
	    },
	    tbar : costAnalyzeGridTbar,
	    bbar : pagingBar
	});
	

	costAnalyzeGrid.region = 'center';
	
	new Ext.Panel({
		renderTo : 'divCostAnalysis',
		width : parseInt(Ext.getDom('divCostAnalysis').parentElement.style.width.replace(/px/g,'')),
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