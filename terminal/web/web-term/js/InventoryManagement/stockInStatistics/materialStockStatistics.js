Ext.onReady(function(){
	var subTyps = {
		STOCK_IN : {val : 1 , desc : '采购'},
		STOCK_OUT : {val : 4 , desc : '退货'}
	}	
	
	var dateBegin = new Ext.form.DateField({
		id : 'beginDate_combo_MaterialStock',
		xtype : 'datefield',
		format : 'Y-m-d',
		maxValue : new Date(),
		readOnly : false,
		allowBlank : false
	});
	
	var dateEnd = new Ext.form.DateField({
		id : 'beginEnd_combo_MaterialStock',
		xtype : 'datefield',
		format : 'Y-m-d',
		maxValue : new Date(),
		readyOnly : false,
		allowBlank : false
	});
	
	var dateCombo = Ext.ux.createDateCombo({
		beginDate : dateBegin,
		endDate : dateEnd,
		callback : function(){
			Ext.getCmp('searchBtn_MaterialStock').handler();
		}
	});
	
	var suppliserCombo;
	suppliserCombo = new Ext.form.ComboBox({
				readOnly : false,
				forceSelection : true,
				width : 103,
				listWidth : 120,
				store : new Ext.data.SimpleStore({
					fields : ['supplierID', 'name']
				}),
				valueField : 'supplierID',
				displayField : 'name',
				typeAhead : true,
				mode : 'local',
				triggerAction : 'all',
				selectOnFocus : true,
				listeners : {
					render : function(thiz){
						var data = [[-1,'全部']];
						Ext.Ajax.request({
							url : '../../QuerySupplier.do',
							success : function(res, opt){
								var jr = Ext.decode(res.responseText);
								for(var i = 0; i < jr.root.length; i++){
									data.push([jr.root[i]['supplierID'], jr.root[i]['name']]);
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
						Ext.getCmp('searchBtn_MaterialStock').handler();
					}
				}
			});
			
	var cateType = [[-1, '全部'], [1, '商品'], [2, '原料']]
			
	
	var cateTypeCombo;
	cateTypeCombo = new Ext.form.ComboBox({
		width : 80,
		readOnly : false,
		forceSelection : true,
		store : new Ext.data.SimpleStore({
			data :	cateType,
			fields : ['value', 'text']
		}),
		valueField : 'value',
		displayField : 'text',
		typeAhead : true,
		mode : 'local',
		triggerAction : 'all',
		selectOnFocus : true,
		allowBlank : false,
		listeners : {
			render : function(thiz){
				cateTypeCombo.setValue(-1);
				cateTypeCombo.fireEvent('select');
			},
			select : function(thiz){
				var cateStore = cateIdCombo.getStore();
				cateStore.baseParams['type'] = cateTypeCombo.getValue();
				cateStore.load();
			}
		}
	})
	
	
	var cateIdCombo;
	cateIdCombo = new Ext.form.ComboBox({
		forceSelection : true,
		width : 100,
		valueField : 'id',
		displayField : 'name',
		typeAhead : true,
		mode : 'local',
		triggerAction : 'all',
		selectOnFocus : true,
		store : new Ext.data.JsonStore({
			url : '../../QueryMaterialCate.do',
			root : 'root',
			baseParams : {
				dataSource : 'normal',
				restaurantID : restaurantID
			},
			fields : MaterialCateRecord.getKeys(),
			listeners : {
				load : function(thiz, records, opts){
					if(records.length > 0){
						var PersonRecord = Ext.data.Record.create([
					         {name : 'id'},
					         {name : 'typeValue'},
					         {name : 'name'},
					         {name : 'rid'}				
						]);
						var newRecord= new PersonRecord({typeValue: -1,id: -1,name: "全部",rid: -1});   
						thiz.insert(0,newRecord); 
						cateIdCombo.setValue(-1);
						cateIdCombo.fireEvent('select');
					}
				}
				
			}
		}),
		listeners : {
			select : function(thiz){
				materialComb.allowBlank = true;
	        	materialComb.reset();
	        	sDetail_materialStore.load({  
		            params: {  
		            	cateType : cateTypeCombo.getValue(),
		            	cateId : cateIdCombo.getValue(),  
		            	dataSource : 'normal'
		            }  
	            });
				Ext.getCmp('searchBtn_MaterialStock').handler();
			}
		}
	});
	
	var sDetail_materialStore = new Ext.data.Store({
		//proxy : new Ext.data.MemoryProxy(data),
		proxy : new Ext.data.HttpProxy({url:'../../QueryMaterial.do?restaurantID=' + restaurantID}),
		reader : new Ext.data.JsonReader({totalProperty:'totalProperty', root : 'root'}, [
	         {name : 'id'},
	         {name : 'name'},
	         {name : 'pinyin'}
		])
	});

	sDetail_materialStore.load({  
	    params: { 
	    	cateType : cateTypeCombo.value,
	    	dataSource : 'normal'
	    }  
	});
	
	var materialComb;
	materialComb = new Ext.form.ComboBox({
		forceSelection : true,
		width : 100,
		listWidth : 250,
		maxheight : 300,
		id : 'materialId',
		store : sDetail_materialStore,
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
					combo.store.filterBy(function(record){
						return record.get('name').indexOf(value) != -1
								|| (record.get('id')+'').indexOf(value) != -1
								|| record.get('pinyin').indexOf(value.toUpperCase()) != -1;
					});
					combo.expand();
					combo.select(0, true);
					return false;
				
				}
			},
			select : function(){
				Ext.getCmp('searchBtn_MaterialStock').handler();		
			}
			
		}
	});
	
	
	var representToolbar;
	representToolbar = new Ext.Toolbar({
		items : [
		{
			xtype : 'tbtext',
			text : '&nbsp;&nbsp;&nbsp;&nbsp;日期：'
		},
		dateCombo,'　',
		dateBegin,{
			xtype : 'tbtext',
			text : '&nbsp;&nbsp;至&nbsp;&nbsp;'
		},
		dateEnd,{
			xtype : 'tbtext',
			text : '&nbsp;&nbsp;&nbsp;&nbsp;供应商：'
		}, suppliserCombo,{
			xtype : 'tbtext',
			text : '&nbsp;&nbsp;&nbsp;&nbsp;货品类型：'
		}, cateTypeCombo, {
			xtype : 'tbtext',
			text : '&nbsp;&nbsp;&nbsp;&nbsp;类别：'
		}, cateIdCombo, {
			xtype : 'tbtext',
			text : '&nbsp;&nbsp;&nbsp;&nbsp;货品：'
		},materialComb, '->',{
			text : '搜索',
			iconCls : 'btn_search',
			id : 'searchBtn_MaterialStock',
			handler : function(thiz){
				var dateBegin = Ext.util.Format.date(Ext.getCmp('beginDate_combo_MaterialStock').getValue(), 'Y-m-d');
				var dateEnd = Ext.util.Format.date(Ext.getCmp('beginEnd_combo_MaterialStock').getValue(), 'Y-m-d');
				var supplierId = suppliserCombo.getValue();
				var cateType = cateTypeCombo.getValue();
				var cateId = cateIdCombo.getValue();
				var materialId = materialComb.getValue();
				store.baseParams['dateBegin'] = dateBegin;
				store.baseParams['dateEnd'] = dateEnd;
				store.baseParams['supplierId'] = supplierId;
				store.baseParams['cateType'] = cateType;
				store.baseParams['cateId'] = cateId;
				store.baseParams['materialId'] = materialId;
				store.load();
				
			}
		},'-',{
			text : '导出',
			iconCls : 'icon_tb_exoprt_excel'
		}]
	});
	
	var cm = new Ext.grid.ColumnModel([
	                                   
				new Ext.grid.RowNumberer(),
				{
					header : '货品',
					dataIndex : 'materialName'
				},{
					header : '采购数量',
					dataIndex : 'stockIn'
				},{
					header : '采购金额',
					dataIndex : 'stockInMoney',
					renderer : function(data){
						return data.toFixed(2);
					}
				},{
					header : '退货数量',
					dataIndex : 'stockOut'
				},{
					header : '退货金额',
					dataIndex : 'stockOutMoney',
					renderer : function(data){
						return data.toFixed(2);
					}
				},{
					header : '合计金额',
					dataIndex : 'totalMoney',
					renderer : function(data, colCls, store){
						return (store.data.stockInMoney - store.data.stockOutMoney).toFixed(2);
					}					
				},{
					header : '操作',
					dataIndex : 'descration',
					renderer : function(){
						return '<a href="javascript:void(0);" data-type="descration_MaterialStock">详情</a>'
					}
				}]);
	
	var store = new Ext.data.Store({
		proxy : new Ext.data.HttpProxy({url : '../../MaterialStockStatistics.do'}),
		reader : new Ext.data.JsonReader({totalProperty : 'totalProperty', root : 'root'}, [{
			name : 'materialName'
		},{
			name : 'stockIn'
		},{
			name : 'stockInMoney'
		},{
			name : 'stockOut'
		},{
			name : 'stockOutMoney'
		},{
			name : 'totalMoney'
		},{
			name : 'descration'
		}]),
		listeners : {
			'load' : function(){
				$('[data-type=descration_MaterialStock]').click(function(){
					jumpToStockDetail();
				});
			}
		}
	});
	
	function jumpToStockDetail(){
		var selectCol = Ext.getCmp('MaterialStockPanel_gridPanel_MaterialStock').getSelectionModel().getSelected();
		var dateBegin = Ext.util.Format.date(Ext.getCmp('beginDate_combo_MaterialStock').getValue(), 'Y-m-d');
		var dateEnd = Ext.util.Format.date(Ext.getCmp('beginEnd_combo_MaterialStock').getValue(), 'Y-m-d');
		var cateType = cateTypeCombo.getValue();
		var cateId = cateIdCombo.getValue();
		var supplierId = suppliserCombo.getValue();
		var materialId = selectCol.json.materialId;
		Ext.ux.addTab('stockDetail', '进销存明细', 'InventoryManagement_Module/StockDetailReport.html', function(){
			var store = Ext.getCmp('stock_detail_grid').getStore();
//			Ext.getCmp('materialType').setValue(cateType);
//			Ext.getCmp('materialCate').setValue(cateId == -1 ? '' : cateId);
//			Ext.getCmp('materialId_stockDetail').setValue(materialId == -1 ? '' : materialId);
//			Ext.getCmp('comboSearchSupplierForDetail').setValue(supplierId);
			store.baseParams['beginDate'] = dateBegin;
			store.baseParams['endDate'] = dateEnd;
			store.baseParams['materialId'] = (materialId == -1 ? '' : materialId);
			store.baseParams['materialCateId'] = (cateId == -1 ? '' : cateId);
			store.baseParams['cateType'] = cateType;
			store.baseParams['supplier'] = supplierId;
			store.baseParams['subTypes'] = subTyps.STOCK_IN.val + '&' + subTyps.STOCK_OUT.val;
//			Ext.getCmp('stockDetail_btnSearch').handler();
			Ext.getCmp('stock_detail_grid').getStore().load();
			Ext.getCmp('stock_detail_grid').getStore().on('load', function(){
				store.baseParams['endDate'] = '';
				store.baseParams['subTypes'] = '';
			});
		});
	}
	
	var pagingbar = new Ext.PagingToolbar({
		pageSize : 20,
		store : store,
		displayInfo : true,
		displayMsg : '显示第{0} 条到{1} 条记录，共{2}条',
		emptyMsg : '没有记录'
	}); 
	
	
	var stockInPanel = new Ext.grid.GridPanel({
		id : 'MaterialStockPanel_gridPanel_MaterialStock',
		frame : false,
		autoScroll : true,
		height : parseInt(Ext.getDom('container_div_MaterialStockStatistics').parentElement.style.height.replace(/px/g, '')) - 40,
		width : parseInt(Ext.getDom('container_div_MaterialStockStatistics').parentElement.style.width.replace(/px/g, '')),
		viewConfig : {
			forceFit : true
		},
		cm : cm,
		loadMask : {
			msg : '数据加载中,请稍后....'
		},
		store : store,
		tbar : representToolbar,
		bbar : pagingbar,
		keys : {
			key : Ext.EventObject.ENTER,
			fn : function(){
			}
		}
	});
	
	new Ext.Panel({
		title : '货品采购统计',
		frame : true,
		renderTo : 'container_div_MaterialStockStatistics',
		items : [stockInPanel]
		
	});
	
	//汇总
	stockInPanel.getStore().on('load', function(store, records, options){
		var sumRow = null;
		if(store.getCount() > 0){

			sumRow = stockInPanel.getView().getRow(store.getCount() - 1);	
			sumRow.style.backgroundColor = '#EEEEEE';			
			for(var i = 0; i < stockInPanel.getColumnModel().getColumnCount(); i++){
				var sumCell = stockInPanel.getView().getCell(store.getCount() - 1, i);
				sumCell.style.fontSize = '15px';
				sumCell.style.fontWeight = 'bold';
				sumCell.style.color = 'green';
			}
			
			
			stockInPanel.getView().getCell(store.getCount()-1, 1).innerHTML = '汇总';
			stockInPanel.getView().getCell(store.getCount()-1, 7).innerHTML = '--';
		}
		
	});	
	
	dateCombo.setValue('1');
	dateCombo.fireEvent('select', dateCombo, null, 1);
});