Ext.onReady(function(){
	var hours;
	var beginDate = new Ext.form.DateField({
		id : 'beginDate_combo_passengerFlow',
		xtype : 'datefield',
		format : 'Y-m-d',
		width : 100,
		maxValue : new Date(),
		readOnly : false,
		allowBlank : false
	});
	
	var endDate = new Ext.form.DateField({
		id : 'endDate_combo_passengerFlow',
		xtype : 'datefield',
		format : 'Y-m-d',
		width : 100,
		maxValue : new Date(),
		readOnly : false,
		allowBlank : false
	});
	
	var dataCombo = Ext.ux.createDateCombo({
		beginDate : beginDate,
		endDate : endDate,
		callback : function(){
//			Ext.getCmp('search_btn_passengerFlow').handler();
		}
	});
	
	//区域选择
	var regionSelect_combo_passengerFlow = new Ext.form.ComboBox({
		id : 'regionSelect_combo_passengerFlow',
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
				var data = [[-1, '全部']];
				Ext.Ajax.request({
					url : '../../OperateRegion.do',
					params : {
						dataSource : 'getByCond'
					},
					success : function(res, opt){
						var jr = Ext.decode(res.responseText);
						
						for(var i = 0; i < jr.root.length; i++){
							data.push([jr.root[i]['id'], jr.root[i]['name']]);
						}
						
						thiz.store.loadData(data);
					},
					failure : function(res, opt){
						thiz.store.loadData({root : [{id : -1, name : '全部'}]});
						thiz.setValue(-1);
					}
				});
			},
			select : function(){
//				Ext.getCmp('search_btn_passengerFlow').handler();
			}
		}
	});
	
	//门店选择
	var branchSelect_combo_passengerFlow = new Ext.form.ComboBox({
		id : 'branchSelect_combo_passengerFlow',
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
				var data = [[-1, '全部']];
				
				Ext.Ajax.request({
					url : '../../OperateRegion.do',
					params : {
						dataSource : 'getByCond'
					},
					success : function(res, opt){
						
						var jr = Ext.decode(res.responseText);
						
						for(var i = 0; i < jr.root.length; i++){
							data.push([jr.root[i]['id'], jr.root[i]['name']]);
						}
						
						thiz.store.loadData(data);
					},
					failure : function(res, opt){
						thiz.store.loadData({root : [{id : -1, name : '全部'}]});
						thiz.setValue(-1);
					}
					
				});
			},
			select : function(){
//				Ext.getCmp('search_btn_passengerFlow').handler();
				//TODO select的时候做的处理
			}
		}
		
		
	});
	
	var passengerFlowTbarFor2 = [{
		xtype : 'tbtext',
		width : 10
	}, {
		xtype : 'tbtext',
		text : '区域选择'},
	{
		xtype : 'tbtext',
		text : '&nbsp;&nbsp;'
	}, regionSelect_combo_passengerFlow, {
		xtype : 'tbtext',
		text : '&nbsp;&nbsp;'
	}, {
		xtype : 'tbtext',
		text : '门店选择'
	}, branchSelect_combo_passengerFlow, '->', {
		text : '搜索',
		id : 'search_btn_passengerFlow',
		iconCls : 'btn_search',
		handler : function(){
			//TODO 搜索的事件
		}
	}];
	
	
	
	//couponGrid的栏目
	var cm = new Ext.grid.ColumnModel([
	    new Ext.grid.RowNumberer(),
	    {header : '操作日期', dataIndex : 'offDutyToDate'},
	    {header : '实收', dataIndex : 'totalActual'},
	    {header : '人数', dataIndex : 'customerAmount'},
	    {header : '人均消费', dataIndex : 'averageCost'}
   ]);	
	
	//默认排序
	cm.defaultSortable = true;
	
	var ds = new Ext.data.Store({
		proxy : new Ext.data.HttpProxy({url : '../../BusinessReceiptsStatistics.do'}),
		reader : new Ext.data.JsonReader({totalProperty : ' totalProperty', root : 'root',}, [
		      {name : 'offDutyToDate'},
		      {name : 'totalActual'},
		      {name : 'customerAmount'},
         ])
	})
	
	
	ds.baseParams['dataSource'] = 'normal';
	ds.load();
	
	var passengerFlowTbar = Ext.ux.initTimeBar({
		beginDate : beginDate,
		endDate : endDate,
		dataCombo : dataCombo,
		tbarType : 1,
		statistics : 'passengerFlow_',
		callback : function businessHourSelect(){
			hours = null;
		}
	}).concat(passengerFlowTbarFor2);
	
	var pagingBar = new Ext.PagingToolbar({
		pageSize : 20,
		store : ds,
		displayInfo : true,
		displayMsg : "显示第{0} 条到 {1} 条记录, 共 {2}条",
		emptyMsg : " 没有记录"
	});
	
	var passengerFlowStatistics = new Ext.grid.GridPanel({
		border : false,
		frame : false,
		cm : cm,
		store : ds,
		viewConfig : {
			forceFit : true
		},
		loadMask : {
			msg : '数据加载中,请稍后....'
		},
		//TODO 头部栏
//		tbar : ''
		bbar : pagingBar
	});
	
	passengerFlowStatistics.region = 'center';
	
	var couponDetailPanel = new Ext.Panel({
		title : '客流统计',
		layout : 'border',
		region : 'center',
		frame : true,
		items : [passengerFlowStatistics]
	});
	
	
	new Ext.Panel({
		renderTo : 'passengerFlow_div_passenferFlowStatistics',
		id : 'couponStatisticsPanel',
		width : parseInt(Ext.getDom('passengerFlow_div_passenferFlowStatistics').parentElement.style.width.replace(/px/g, '')),
		height : parseInt(Ext.getDom('passengerFlow_div_passenferFlowStatistics').parentElement.style.height.replace(/px/g, '')),
		layout : 'border',
		frame : true,
		items : [couponDetailPanel]
	});
	
	//TODO 图表
	
});