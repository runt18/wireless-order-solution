
var repaid_beginDate = new Ext.form.DateField({
	xtype : 'datefield',		
	format : 'Y-m-d',
	width : 100,
	maxValue : new Date(),
	readOnly : true,
	allowBlank : false
});
var repaid_endDate = new Ext.form.DateField({
	xtype : 'datefield',
	format : 'Y-m-d',
	width : 100,
	maxValue : new Date(),
	readOnly : true,
	allowBlank : false
});
var repaid_dateCombo = Ext.ux.createDateCombo({
	beginDate : repaid_beginDate,
	endDate : repaid_endDate,
	callback : function(){
		Ext.getCmp('btnSearchForRepaidStatistics').handler();
	}
});

var repaid_combo_staffs = new Ext.form.ComboBox({
	id : 'repaid_combo_staffs',
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
			Ext.getCmp('btnSearchForRepaidStatistics').handler();
		}
	}
});
function initGrid(){
	var cm = new Ext.grid.ColumnModel([
		new Ext.grid.RowNumberer(),
		{header : '反结账时间', dataIndex : 'orderDateFormat'},
		{header : '人员', dataIndex : 'operateStaff'},
		{header : '单据编号', dataIndex : 'orderId'},
		{header : '原应收', dataIndex : 'oldTotalPrice', align : 'right', renderer : Ext.ux.txtFormat.gridDou},
		{header : '原实收', dataIndex : 'oldActualPrice', align : 'right', renderer : Ext.ux.txtFormat.gridDou},
		{header : '反结账金额', dataIndex : 'repaidPrice', align : 'right', renderer : Ext.ux.txtFormat.gridDou},
		{header : '现应收', dataIndex : 'totalPrice', align : 'right', renderer : Ext.ux.txtFormat.gridDou},
		{header : '现实收', dataIndex : 'actualPrice', align : 'right', renderer : Ext.ux.txtFormat.gridDou},
		{header : '付款方式', dataIndex : 'payTypeText'}
	]);
	
	cm.defaultSortable = true;
	
	var ds = new Ext.data.Store({
		proxy : new Ext.data.HttpProxy({url : '../../QueryRepaidStatistics.do'}),
		reader : new Ext.data.JsonReader({totalProperty : 'totalProperty', root : 'root'}, [
		{name : 'orderDateFormat'},
		{name : 'operateStaff'},
		{name : 'orderId'},
		{name : 'oldTotalPrice'},
		{name : 'oldActualPrice'},
		{name : 'repaidPrice'},
		{name : 'totalPrice'},
		{name : 'actualPrice'},
		{name : 'payTypeValue'},
		{name : 'payTypeText'}
		])
		
	});
	
	var repaidStatisticsTbar = new Ext.Toolbar({
		items : [{
				xtype : 'tbtext',
				text : '日期:'
			}, repaid_dateCombo, {
				xtype : 'tbtext',
				text : '&nbsp;'
			}, repaid_beginDate , {
				xtype : 'tbtext',
				text : '&nbsp;至&nbsp;'
			}, repaid_endDate, {
				xtype : 'tbtext',
				text : '&nbsp;&nbsp;'
			},{
				xtype : 'tbtext',
				text : '操作人员:'
			}, repaid_combo_staffs, '->', {
				text : '搜索',
				id : 'btnSearchForRepaidStatistics',
				iconCls : 'btn_search',
				handler : function(e){
					if(!repaid_beginDate.isValid() || !repaid_endDate.isValid()){
						return;
					}
					var store = repaidStatisticsGrid.getStore();
					store.baseParams['beginDate'] = repaid_beginDate.getValue().format('Y-m-d 00:00:00');
					store.baseParams['endDate'] = repaid_endDate.getValue().format('Y-m-d 23:59:59');
					store.baseParams['staffId'] = repaid_combo_staffs.getValue();
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
	
	repaidStatisticsGrid = new Ext.grid.GridPanel({
		id : 'repaid_grid',
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
	    tbar : repaidStatisticsTbar,
	    bbar : pagingBar
	});
	repaidStatisticsGrid.region = 'center';
	repaidStatisticsGrid.on('render', function(){
		repaid_dateCombo.setValue(1);
		repaid_dateCombo.fireEvent('select', repaid_dateCombo, null, 1);
	});
	repaidStatisticsGrid.getStore().on('load', function(store, records, options){
		
		if(store.getCount() > 0){
			var sumRow = repaidStatisticsGrid.getView().getRow(store.getCount() - 1);	
			sumRow.style.backgroundColor = '#EEEEEE';			
			for(var i = 0; i < repaidStatisticsGrid.getColumnModel().getColumnCount(); i++){
				var sumCell = repaidStatisticsGrid.getView().getCell(store.getCount() - 1, i);
				sumCell.style.fontSize = '15px';
				sumCell.style.fontWeight = 'bold';	
				sumCell.style.color = 'green';
			}
			repaidStatisticsGrid.getView().getCell(store.getCount()-1, 1).innerHTML = '汇总';
			repaidStatisticsGrid.getView().getCell(store.getCount()-1, 2).innerHTML = '--';
			repaidStatisticsGrid.getView().getCell(store.getCount()-1, 3).innerHTML = '--';
			repaidStatisticsGrid.getView().getCell(store.getCount()-1, 4).innerHTML = '--';
			repaidStatisticsGrid.getView().getCell(store.getCount()-1, 5).innerHTML = '--';
			repaidStatisticsGrid.getView().getCell(store.getCount()-1, 7).innerHTML = '--';
			repaidStatisticsGrid.getView().getCell(store.getCount()-1, 8).innerHTML = '--';
			repaidStatisticsGrid.getView().getCell(store.getCount()-1, 9).innerHTML = '--';
		}
	});
}

Ext.onReady(function(){
	initGrid();
	new Ext.Panel({
		renderTo : 'divRepaidStatistics',//渲染到
		id : 'repaidStatisticsPanel',
		//solve不跟随窗口的变化而变化
		width : parseInt(Ext.getDom('divRepaidStatistics').parentElement.style.width.replace(/px/g,'')),
		height : parseInt(Ext.getDom('divRepaidStatistics').parentElement.style.height.replace(/px/g,'')),
		layout:'border',
		frame : false, //边框
		//子集
		items : [repaidStatisticsGrid]
	});
//	repaidStatisticsGrid.getStore().load();
});