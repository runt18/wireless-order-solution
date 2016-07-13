Ext.onReady(function(){
	var materialTypeDate = [[-1,'全部'],[1,'商品'],[2,'原料']];
	var materialTypeComb = new Ext.form.ComboBox({
		fidldLabel : '类型:',
		forceSelection : true,
		width : 110,
		id : 'sdir_materialType',
		value : -1,
		store : new Ext.data.SimpleStore({
			fields : [ 'value', 'text' ],
			data : materialTypeDate
		}),
		valueField : 'value',
		displayField : 'text',
		typeAhead : true,
		mode : 'local',
		triggerAction : 'all',
		selectOnFocus : true,
		readOnly : false,
		listeners : {
	        select : function(combo, record, index){  
	        	materialCateComb.reset();
	        	materialComb.allowBlank = true;
	        	materialComb.reset();
	        	materialCateStore.load({  
		            params: {  
		            	type : combo.value,  
		            	dataSource : 'normal'
		            }  
	            });     
	        	materialStore.load({
	        		params: {
	        			cateType : combo.value,
	        			dataSource : 'normal'
	        		}
	        	});
	        	Ext.getCmp('btnStockDistributionSearch').handler();
			}  
		}
		
	});
	
	var alarmData = [[-1, '全部'],['underAlarm', '低于'],['overAlarm', '高于']];
	var materialAlarmCombo;
	materialAlarmCombo = new Ext.form.ComboBox({
		id : 'alarmCombo_combo_stockDistribution',
		fidldLabel : '库存预警:',
		forceSelection : true,
		width : 110,
		value : -1,
		store : new Ext.data.SimpleStore({
			fields : [ 'value', 'text' ],
			data : alarmData
		}),
		valueField : 'value',
		displayField : 'text',
		typeAhead : true,
		mode : 'local',
		triggerAction : 'all',
		selectOnFocus : true,
		readOnly : false,
		listeners : {
	        select : function(){  
	        	Ext.getCmp('btnStockDistributionSearch').handler();
			}  
		}
		
	});
	var materialCateStore = new Ext.data.Store({
		//proxy : new Ext.data.MemoryProxy(data),
		proxy : new Ext.data.HttpProxy({url:'../../QueryMaterialCate.do'}),
		reader : new Ext.data.JsonReader({totalProperty:'totalProperty', root : 'root'}, [
	         {name : 'id'},
	         {name : 'name'}
		])
	});
	materialCateStore.load({  
	    params: {  
	    	type : materialTypeComb.value,  
	    	dataSource : 'normal'
	    }
	}); 
	var materialCateComb = new Ext.form.ComboBox({
		fidldLabel : '类别:',
		forceSelection : true,
		width : 110,
		id : 'sdir_materialCate',
		store : materialCateStore,
		valueField : 'id',
		displayField : 'name',
		typeAhead : true,
		mode : 'local',
		triggerAction : 'all',
		selectOnFocus : true,
		//blankText: '不能为空', 
		readOnly : false,
		listeners : {
	        select : function(combo, record, index){ 
	        	materialComb.allowBlank = true;
	        	materialComb.reset();
	        	materialStore.load({  
		            params: {  
		            	cateType : materialTypeComb.value,
		            	cateId : combo.value,  
		            	dataSource : 'normal'
		            }  
	            });   
	        	Ext.getCmp('btnStockDistributionSearch').handler();
			}
	
		}
		
	});
	var materialStore = new Ext.data.Store({
		//proxy : new Ext.data.MemoryProxy(data),
		proxy : new Ext.data.HttpProxy({url:'../../QueryMaterial.do?contentAll=true'}),
		reader : new Ext.data.JsonReader({totalProperty:'totalProperty', root : 'root'}, [
			{name : 'id'},
			{name : 'name'},
			{name : 'pinyin'}
		])
	});
	materialStore.load({  
	    params: { 
	    	cateType : materialTypeComb.value,
	    	dataSource : 'normal'
	    }  
	}); 
	var materialComb = new Ext.form.ComboBox({
		fidldLabel : '货品:',
		forceSelection : true,
		width : 110,
		listWidth : 250,
		height : 200,
		maxHeight : 300,
		id : 'sdir_materialId',
		store : materialStore,
		valueField : 'id',
		displayField : 'name',
		typeAhead : true,
		mode : 'local',
		triggerAction : 'all',
		selectOnFocus : true,
		tpl:'<tpl for=".">' 
			+ '<div class="x-combo-list-item" style="height:18px;">'
			+ '{id} -- {name} -- {pinyin}'
			+ '</div>'
			+ '</tpl>',
		listeners : {
			beforequery : function(e){
				var combo = e.combo; 
				if(!e.forceAll){
					var value = e.query; 
					combo.store.filterBy(function(record,id){
						return record.get('name').indexOf(value) != -1 
								|| (record.get('id')+'').indexOf(value) != -1 
								|| record.get('pinyin').indexOf(value.toUpperCase()) != -1;
					});  
					combo.expand(); 
					combo.select(0, true);
					return false; 
				}
			},
			select : function(combo, record, index){
				Ext.getCmp('btnStockDistributionSearch').handler();
			}
		}
		
	});
	var stockDistributionDeptTree;
	var stockDistributionGrid;

	Ext.form.Field.prototype.msgTarget = 'side';
	
	var deptProperty = [];
	$.ajax({
		url : '../../OperateDept.do',
		type : 'post',
		dataType : 'json',
		async : false,
		data : {
			dataSource : 'getByCond',
			inventory : true
		},
		success : function(data){
			deptProperty = data.root;
		},
		error : function(xhr){
			Ext.ux.showMsg(JSON.parse(xhr.responseText));
		}
	});
	
	
	function renderAlarmAmount(data, cel, store){
		
		if(store.json.alarmAmount){
			return data >= store.json.alarmAmount ? '<span style="font-weight: bold; color :green;font-size :14px;">' + data + '<span>' : '<span style="font-weight: bold; color :red;font-size :14px;">' + data + '<span>';
		}else{
			return '<span style="font-weight: bold; color :green;font-size :14px;">' + data + '<span>';
		}
	}
	
	var deptColumnModel = [{header:'品项名称', dataIndex:'materialName', width:200}];
	var dataStore = [{name : 'materialName'}];
	for (var i = 0; i < deptProperty.length; i++) {
		deptColumnModel.push({header:deptProperty[i].name, dataIndex: 'dept'+deptProperty[i].id, align: 'right'});
		dataStore.push({name : 'dept'+deptProperty[i].id });
	}
	deptColumnModel.push({header:'成本单价', dataIndex:'price', align: 'right', renderer : Ext.ux.txtFormat.gridDou});
	deptColumnModel.push({header:'数量合计', dataIndex:'stock', align: 'right', renderer : renderAlarmAmount});
	deptColumnModel.push({header:'预警数量', dataIndex:'alarmAmount', align: 'right', renderer : function(data){return data ? data : ''}});
	deptColumnModel.push({header:'成本金额', dataIndex:'cost', align: 'right', renderer : Ext.ux.txtFormat.gridDou});
	
	dataStore.push({name : 'price'});
	dataStore.push({name : 'stock'});
	dataStore.push({name : 'alarmAmount'});
	dataStore.push({name : 'cost'});
	
/*	var stockDetail = new Ext.grid.ColumnModel([
		{header:'部门', dataIndex:'dept.name', width:220},
		{header:'品项名称', dataIndex:'materialName', width:220, hidden:true},
		{header:'数量', dataIndex:'stock', width:220, align: 'right', renderer : Ext.ux.txtFormat.gridDou},
		{header:'成本单价', dataIndex:'price', width:220, align: 'right', renderer : Ext.ux.txtFormat.gridDou},
		{header:'成本金额', dataIndex:'cost', width:220, align: 'right', renderer : Ext.ux.txtFormat.gridDou}
	                                            
	    ]);*/
	var stockDetail = new Ext.grid.ColumnModel(deptColumnModel);
	stockDetail.defaultSortable = true;
	
	var ds = new Ext.data.Store({
		proxy : new Ext.data.HttpProxy({url:'../../QueryMaterialDept.do'}),
		reader : new Ext.data.JsonReader({totalProperty:'totalProperty', root : 'root'}, dataStore)
/*		sortInfo:{field: 'materialName', direction: "ASC"},
		groupField:'materialName'*/
	});
	var date = new Date();
	date.setMonth(date.getMonth()-1);
	
	//部门combo
	var deptComb = new Ext.form.ComboBox({
		id : 'deptComb_comboBox_stockDistributionReport',
		forceSelection : true,
		width : 110,
		maxheight : 300,
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
					url : '../../OperateDept.do',
					params: { 
				    	dataSource : 'getByCond',
				    	inventory : true
				    }, 
					success : function(res, opt){
						var jr = Ext.decode(res.responseText);
						for(var i = 0; i < jr.root.length; i++){
							data.push([jr.root[i]['id'], jr.root[i]['name']]);
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
				Ext.getCmp('btnStockDistributionSearch').handler();	
			}
			
		}
	});
	
	var distributionReportBar = new Ext.Toolbar({
		items : [
 		{xtype : 'label', text : '部门:'},
 		deptComb,
		{xtype : 'tbtext', text : '&nbsp;'},
		{xtype : 'tbtext', text : '类型:'},
		materialTypeComb,
		{xtype : 'tbtext', text : '&nbsp;'},
		{xtype : 'tbtext', text : '类别:'},
		materialCateComb,
		{xtype : 'tbtext', text : '&nbsp;'},
		{xtype : 'tbtext', text : '货品:'},
		materialComb,
		{xtype : 'tbtext', text : '&nbsp;'},
		{xtype : 'tbtext', text : '库存预警:'},
		materialAlarmCombo,
		'->', 
/*			{
				text : '展开/收缩',
				iconCls : 'icon_tb_toggleAllGroups',
				handler : function(){
					stockDistributionGrid.getView().toggleAllGroups();
				} 
			},*/
			{
			text : '刷新',
			id : 'btnStockDistributionSearch',
			iconCls : 'btn_refresh',
			handler : function(){
				var deptID = '';
					
					var stockds = stockDistributionGrid.getStore();
					stockds.baseParams['deptId'] = Ext.getCmp('deptComb_comboBox_stockDistributionReport').getStore().getCount() > 0 ? Ext.getCmp('deptComb_comboBox_stockDistributionReport').getValue() : '-1';
					stockds.baseParams['cateType'] = Ext.getCmp('sdir_materialType').getValue();
					stockds.baseParams['cateId'] = Ext.getCmp('sdir_materialCate').getValue();
					stockds.baseParams['materialId'] = Ext.getCmp('sdir_materialId').getValue();
					stockds.baseParams['checkAlarm'] = Ext.getCmp('alarmCombo_combo_stockDistribution').getValue();
					stockds.load({
						params : {
							start : 0,
							limit : 13
						}
					});
			}
		},{xtype : 'tbtext', text : '&nbsp;&nbsp;'}
		]
	});
	var pagingBar = new Ext.PagingToolbar({
		   pageSize : 20,	//显示记录条数
		   store : ds,	//定义数据源
		   displayInfo : true,	//是否显示提示信息
		   displayMsg : "显示第 {0} 条到 {1} 条记录，共 {2} 条",
		   emptyMsg : "没有记录"
	});
	stockDistributionGrid = new Ext.grid.GridPanel({
		title : '库存分布明细',
		id : 'grid',
//		region : 'center',
		height : parseInt(Ext.getDom('divStockDistribution').parentElement.style.height.replace(/px/g,'')),
		border : true,
		frame : true,
		loadMask : {
			msg : '正在加载数据中...'
		},
		viewConfig : {
			forceFit : true
		},
        animCollapse: false,
        autoScroll : true,
		store : ds,
		cm : stockDetail,
/*		view : new Ext.grid.GroupingView({
            forceFit: true,
            groupTextTpl: '{text} ({[values.rs.length]}条记录)'
        }),*/
		tbar : distributionReportBar,
		bbar : pagingBar
	});
	
	
	//汇总
	stockDistributionGrid.getStore().on('load', function(store, records, options){
		if(store.getCount() > 0){

			var sumRow = stockDistributionGrid.getView().getRow(store.getCount() - 1);	
			sumRow.style.backgroundColor = '#EEEEEE';			
			var index;
			for (var i = 0; i < deptProperty.length; i++) {
				stockDistributionGrid.getView().getCell(store.getCount()-1, i+1).innerHTML = '--';
				index = i;
			}
			stockDistributionGrid.getView().getCell(store.getCount()-1, index+2).innerHTML = '--';
			stockDistributionGrid.getView().getCell(store.getCount()-1, index+4).innerHTML = '--';
			
			for(var i = 0; i < stockDistributionGrid.getColumnModel().getColumnCount(); i++){
				var sumCell = stockDistributionGrid.getView().getCell(store.getCount() - 1, i);
				sumCell.style.fontSize = '15px';
				sumCell.style.fontWeight = 'bold';
				sumCell.style.color = 'green';
			}
		}
		
	});
	
	
	new Ext.Panel({
		renderTo : 'divStockDistribution',
		width : parseInt(Ext.getDom('divStockDistribution').parentElement.style.width.replace(/px/g,'')),
		height : parseInt(Ext.getDom('divStockDistribution').parentElement.style.height.replace(/px/g,'')),
		items : [stockDistributionGrid]
	});
	
	stockDistributionGrid.getStore().load({params:{start:0,limit:20}});
});

