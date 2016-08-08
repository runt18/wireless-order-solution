Ext.onReady(function(){
	var beginDate = new Ext.form.DateField({
		id: 'beginDate_combo_combo',
		xtype : 'datefield',
		format : 'Y-m-d',
		width : 100,
		maxValue : new Date(),
		readOnly : false,
		allowBlank : false
	});
	
	var endDate = new Ext.form.DateField({
		id : 'endDate_combo_combo',
		xtype : 'datafield',
		format : 'Y-m-d',
		width : 100,
		maxValue : new Date(),
		readOnly : false,
		allowBlank : false
	});
	
	//日期组件
	var dataCombo = Ext.ux.createDateCombo({
		beginDate : beginDate,
		endDate : endDate,
		callback : function(){
			Ext.getCmp('combo_btnSearch').handler();
		}
	});
	
	//套菜部门选择
	var comboDept_combo_combo = new Ext.form.ComboBox({
		id : 'comboDept_combo_combo',
		readOnly : false,
		forceSelection : true,
		width : 123,
		listWidth :120,
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
				var data = [[-1,'全部']];
				Ext.Ajax.request({
					url : '../../QueryDeptTree.do',
					success : function(res, opt){
						var jr = Ext.decode(res.responseText);
						for(var i = 0; i < jr.length; i++){
							data.push([jr[i]['deptID'], jr[i]['text']]);
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
				Ext.getCmp('combo_btnSearch').handler();
			}
		}
	});
	
	
	//子菜部门选择
	var subDept_combo_combo = new Ext.form.ComboBox({
		id : 'subDept_combo_combo',
		readOnly : false,
		forceSelection : true,
		width : 123,
		listWidth :120,
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
				var data = [[-1,'全部']];
				Ext.Ajax.request({
					url : '../../QueryDeptTree.do',
					success : function(res, opt){
						var jr = Ext.decode(res.responseText);
						for(var i = 0; i < jr.length; i++){
							data.push([jr[i]['deptID'], jr[i]['text']]);
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
				Ext.getCmp('combo_btnSearch').handler();
			}
		}
	});
	
	//门店选择
	var branch_combo_combo = new Ext.form.ComboBox({
		id : 'branch_combo_combo',
		readOnly : false,
		forceSelection : true,
		width : 123,
		listWidth :120,
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
				var data = [];
				Ext.Ajax.request({
					url : '../../OperateRestaurant.do',
					params : {
						dataSource : 'getByCond',
						id : restaurantID
					},
					success : function(res, opt){
						var jr = Ext.decode(res.responseText);
						
						if(jr.root[0].typeVal != '2'){
							var data = [];
							data.push([jr.root[0]['id'], jr.root[0]['name']]);
							thiz.store.loadData(data);
							thiz.setValue(jr.root[0].id);
							thiz.fireEvent('select');
						}else{
							var data = [];
							data.push([jr.root[0]['id'], jr.root[0]['name'] + '(集团)']);
							
							for(var i = 0; i < jr.root[0].branches.length; i++){
								data.push([jr.root[0].branches[i]['id'], jr.root[0].branches[i]['name']]);
							}
							
							thiz.store.loadData(data);
							thiz.setValue(jr.root[0].id);
							thiz.fireEvent('select');
						}
					}
				});
			},
			select : function(){
				//加载市别
				var hour = [[-1, '全部']];
				Ext.Ajax.request({
					url : '../../OperateBusinessHour.do',
					params : {
						dataSource : 'getByCond',
						branchId : branch_combo_combo.getValue()
					},
					success : function(res, opt){
						var jr = Ext.decode(res.responseText);
						
						for(var i = 0; i < jr.root.length; i++){
							hour.push([jr.root[i]['id'], jr.root[i]['name'], jr.root[i]['opening'], jr.root[i]['ending']]);
						}
						
						hour.push([-2, '自定义']);
						
						Ext.getCmp('combo_comboBusinessHour').setDisabled(false);
						Ext.getCmp('combo_comboBusinessHour').store.loadData(hour);
						Ext.getCmp('combo_comboBusinessHour').setValue(-1);
					}
				});
				
				//加载部门
				var dept = [[-1, '全部']];
				Ext.Ajax.request({
					url : '../../OperateDept.do',
					params : {
						dataSource : 'getByCond',
						branchId : branch_combo_combo.getValue()
					},
					success : function(res, opt){
						var jr = Ext.decode(res.responseText);
						
						for(var i = 0; i < jr.root.length; i++){
							dept.push([jr.root[i]['id'], jr.root[i]['name']]);
						}
						
						subDept_combo_combo.setDisabled(false);
						subDept_combo_combo.store.loadData(dept);
						subDept_combo_combo.setValue(-1);
						
						
						comboDept_combo_combo.setDisabled(false);
						comboDept_combo_combo.store.loadData(dept);
						comboDept_combo_combo.setValue(-1);
					}
				});
				Ext.getCmp('combo_btnSearch').handler();
			}
		}
	});
	
	//toorbar
	var businessHour;
	var comboStatisticsTbarItem = [{
		xtype : 'tbtext',
		width : 10
	}, {
		xtype : 'tbtext',
		text : '套餐名称'
	}, {
		xtype : 'textfield',
		id : 'foodName_textfield',
		width : 120
	},{
		xtype : 'tbtext',
		width : 10
	}, {
		xtype : 'tbtext',
		text : '套菜部门选择'
	}, comboDept_combo_combo, {
		xtype : 'tbtext',
		width : 10
	}, {
		xtype : 'tbtext',
		text : '子菜部门选择'
	}, subDept_combo_combo,{
		xtype : 'tbtext',
		width : 10
	}, {
		xtype : 'tbtext',
		text : '门店选择'
	}, branch_combo_combo, '->', {
		text : '搜索',
		id : 'combo_btnSearch',
		iconCls : 'btn_search',
		handler : function(e, aa){
			if(!beginDate.isValid() || !endDate.isValid){
				return;
			}
			
			businessHour =  Ext.ux.statistic_oBusinessHourData({type : 'get', statistic : 'combo_'}).data;
			
			var store = comboGrid.getStore();
			store.baseParams['dataSource'] = 'normal';
			store.baseParams['onDuty'] = Ext.util.Format.date(beginDate.getValue(), 'Y-m-d 00:00:00');
			store.baseParams['offDuty'] = Ext.util.Format.date(endDate.getValue(), 'Y-m-d 23:59:59');
			store.baseParams['opening'] = businessHour.opening != '00:00' ? businessHour.opening : '';
			store.baseParams['ending'] = businessHour.ending != '00:00' ? businessHour.ending : '';
			store.baseParams['branchId'] = Ext.getCmp('branch_combo_combo').getValue();
			store.baseParams['subDeptId'] = Ext.getCmp('subDept_combo_combo').getValue();
			store.baseParams['comboDeptId'] = Ext.getCmp('comboDept_combo_combo').getValue();
			store.baseParams['subFoodName'] = Ext.getCmp('foodName_textfield').getValue();
			
			store.load({
				params : {
					start : 0,
					limit : 20
				}
			});
		}
	}, {
		text : '导出',
		iconCls : 'icon_tb_exoprt_excel',
		handler : function(){
			var url = '../../{0}?dataSource={1}&onDuty={2}&offDuty={3}&opening={4}&ending={5}&branchId={6}&subDeptId={7}&subFoodName={8}&comboDeptId={9}';
			url = String.format(
				url,
				'ExportHistoryStatisticsToExecl.do',
				'comboDetail',
				Ext.util.Format.date(beginDate.getValue(), 'Y-m-d 00:00:00'),
				Ext.util.Format.date(endDate.getValue(), 'Y-m-d 23:59:59'),
				businessHour.opening != '00:00' ? businessHour.opening : '',
				businessHour.ending != '00:00' ? businessHour.ending : '',
				Ext.getCmp('branch_combo_combo').getValue(),
				Ext.getCmp('subDept_combo_combo').getValue(),
				Ext.getCmp('foodName_textfield').getValue(),
				Ext.getCmp('comboDept_combo_combo').getValue()
			);
			window.location = url;
		}
	}];
	
	var statisticsTbar = Ext.ux.initTimeBar({
		beginDate : beginDate, 
		endDate : endDate,
		dateCombo : dataCombo, 
		tbarType : 1, 
		statistic : 'combo_', 
		callback : function businessHourSelect(){
			}
	}).concat(comboStatisticsTbarItem);
	
	//grid的栏目
	var cm = new Ext.grid.ColumnModel([
		new Ext.grid.RowNumberer(),
		{header : '套餐名称', dataIndex : 'comboFoodName'},
		{header : '菜品名称', dataIndex : 'subFoodName'},
		{header : '数量', dataIndex : 'amount'},
		{header : '总额', dataIndex : 'totalPrice'}
	]);
	
	//默认排序
	cm.defaultSortable = true;
	
	var ds = new Ext.data.Store({
		proxy : new Ext.data.HttpProxy({url : '../../QueryComboStatistics.do'}),
		reader : new Ext.data.JsonReader({totalProperty : 'totalProperty', root : 'root'}, [
			{name : 'comboFoodName'},
			{name : 'subFoodName'},
			{name : 'amount'},
			{name : 'totalPrice'}
		]),
		listeners : {
			load : function(store, records, options){	
        		if(store.getCount() > 0){
        			var sumRow = comboGrid.getView().getRow(store.getCount() - 1);
        			sumRow.style.backgroundColor = '#EEEEEE';
        			for(var i = 0; i < comboGrid.getColumnModel().getColumnCount(); i++){
        				var sumCell = comboGrid.getView().getCell(store.getCount() -1, i);
        				sumCell.style.fontSize = '15px';
        				sumCell.style.fontWeight = 'bold';
        				sumCell.style.color= 'green';
        			}
        			comboGrid.getView().getCell(store.getCount()-1, 1).innerHTML = '汇总';
        			comboGrid.getView().getCell(store.getCount()-1, 2).innerHTML = '--';
        		}
        	}
		}
	});
	
	var pagingBar = new Ext.PagingToolbar({
		pageSize : 20,
		store : ds,
		displayInfo : true,
		displayMsg : "显示第{0} 条到 {1} 条记录, 共 {2}条",
		emptyMsg : " 没有记录"
	});
	
	var comboGrid = new Ext.grid.GridPanel({
		border : false,
		frame : false,
		store : ds,
		cm : cm,
		viewConfig : {
			forceFit : true
		},
		loadMask : {
			msg : '数据加载中,请稍后....'
		},
		bbar : pagingBar,	
		tbar : statisticsTbar
	});
	
	//定义couponGrid的位置
	comboGrid.region = 'center';
	
	comboGrid.keys = [{
		key : Ext.EventObject.ENTER,
		scope : this,
		fn : function(){
			Ext.getCmp('combo_btnSearch').handler();	
		}
	}];
	
	
	var comboDetailPanel = new Ext.Panel({
		title : '套餐明细',
		layout : 'border',
		region : 'center',
		frame : true,
		items : [comboGrid]
	});
	
	new Ext.Panel({
		renderTo : 'comboStatistics_div_cStatistics',
		id : 'comboStatisticsPanel',
		width : parseInt(Ext.getDom('comboStatistics_div_cStatistics').parentElement.style.width.replace(/px/g, '')),
		height : parseInt(Ext.getDom('comboStatistics_div_cStatistics').parentElement.style.height.replace(/px/g, '')),
		layout : 'border',
		frame : true,
		items : [comboDetailPanel]
	});
	
	dataCombo.setValue(1);
	dataCombo.fireEvent('select', dataCombo, null, 1);
	
	
});