Ext.onReady(function(){
	var hours;
	
	var dateBegin = new Ext.form.DateField({
		id : 'beginDate_combo_SupplierStockIn',
		xtype : 'datefield',
		format : 'Y-m-d',
		maxValue : new Date(),
		readOnly : false,
		allowBlank : false
	});
	
	var dateEnd = new Ext.form.DateField({
		id : 'beginEnd_combo_SupplierStockIn',
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
			Ext.getCmp('searchBtn_supplierStock').handler();
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
						Ext.getCmp('searchBtn_supplierStock').handler();
					}
				}
			});
			
	var cateType = [[-1, '全部'], [1, '商品'], [2, '原料']]
			
	
	var cateTypeCombo;
	cateTypeCombo = new Ext.form.ComboBox({
		width : 80,
		fieldLabel : '货品类型',
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
		width : 100,
		forceSelection : true,
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
		valueField : 'id',
		displayField : 'name',
		typeAhead : true,
		mode : 'local',
		triggerAction : 'all',
		selectOnFocus : true,
		listeners : {
			select : function(thiz){
				Ext.getCmp('searchBtn_supplierStock').handler();
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
		}, cateIdCombo, '->',{
			text : '搜索',
			iconCls : 'btn_search',
			id : 'searchBtn_supplierStock',
			handler : function(thiz){
				var dateBegin = Ext.util.Format.date(Ext.getCmp('beginDate_combo_SupplierStockIn').getValue(), 'Y-m-d');
				var dateEnd = Ext.util.Format.date(Ext.getCmp('beginEnd_combo_SupplierStockIn').getValue(), 'Y-m-d');
				var supplierId = suppliserCombo.getValue();
				var cateType = cateTypeCombo.getValue();
				var cateId = cateIdCombo.getValue();
				store.baseParams['dateBegin'] = dateBegin;
				store.baseParams['dateEnd'] = dateEnd;
				store.baseParams['supplierId'] = supplierId;
				store.baseParams['cateType'] = cateType;
				store.baseParams['cateId'] = cateId;
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
					header : '供应商',
					dataIndex : 'supplier.name'
				},{
					header : '采购次数',
					dataIndex : 'stockInAmount'
				},{
					header : '采购金额',
					dataIndex : 'stockInMoney',
					renderer : function(data){
						return data.toFixed(2);
					}
				},{
					header : '退货次数',
					dataIndex : 'stockOutAmount'
				},{
					header : '退货金额',
					dataIndex : 'stockOutMoney',
					renderer : function(data){
						return data.toFixed(2);
					}
				},{
					header : '合计金额',
					dataIndex : 'totalMoney',
					renderer : function(data){
						return data.toFixed(2);
					}
				},{
					header : '操作',
					dataIndex : 'descration',
					renderer : function(){
						return '<a href="javascript:void(0);" data-type="descration_supplierStock">详情</a>'
					}
				}]);
	
	var store = new Ext.data.Store({
		proxy : new Ext.data.HttpProxy({url : '../../SupplierStockStatistics.do'}),
		reader : new Ext.data.JsonReader({totalProperty : 'totalProperty', root : 'root'}, [{
			name : 'supplier.name'
		},{
			name : 'stockInAmount'
		},{
			name : 'stockInMoney'
		},{
			name : 'stockOutAmount'
		},{
			name : 'stockOutMoney'
		},{
			name : 'totalMoney'
		},{
			name : 'descration'
		}]),
		listeners : {
			'load' : function(){
				$('[data-type=descration_supplierStock]').click(function(){
					jumpToStockAction();
				});
			}
		}
	});
	
	
	var pagingbar = new Ext.PagingToolbar({
		pageSize : 20,
		store : store,
		displayInfo : true,
		displayMsg : '显示第{0} 条到{1} 条记录，共{2}条',
		emptyMsg : '没有记录'
	}); 
	
	
	function jumpToStockAction(){
		
		var selectCol = Ext.getCmp('supplierStockPanel_gridPanel_SupplierStock').getSelectionModel().getSelected();
		var dateBegin = Ext.util.Format.date(Ext.getCmp('beginDate_combo_SupplierStockIn').getValue(), 'Y-m-d');
		var dateEnd = Ext.util.Format.date(Ext.getCmp('beginEnd_combo_SupplierStockIn').getValue(), 'Y-m-d');
		var cateType = cateTypeCombo.getValue();
		var cateId = cateIdCombo.getValue();
		var supplierId = selectCol.json.supplier.supplierID;
		Ext.ux.addTab('stockDetail', '进销存明细', 'InventoryManagement_Module/StockDetailReport.html', function(){
			var store = Ext.getCmp('stock_detail_grid').getStore();
			Ext.getCmp('materialType').setValue(cateType);
			Ext.getCmp('materialCate').setValue(cateId >= 0 ? cateId : '');
//			Ext.getCmp('sdr_beginDate').setValue('');
			Ext.getCmp('comboSearchSupplierForDetail').setValue(supplierId);
			
			store.baseParams['cateType'] = cateType;
			store.baseParams['materialCateId'] = (cateId >= 0 ? cateId : '');
			store.baseParams['supplier'] = supplierId;
			store.baseParams['beginDate'] = dateBegin;
			store.baseParams['endDate'] = dateEnd;
			store.load();
			store.on('load', function(){
				store.baseParams['cateType'] = '';
				store.baseParams['materialCateId'] = '';
				store.baseParams['supplier'] = '';
				store.baseParams['beginDate'] = '';
				store.baseParams['endDate'] = '';
			});
		});
	}
	
	
	var stockInPanel = new Ext.grid.GridPanel({
		id : 'supplierStockPanel_gridPanel_SupplierStock',
		frame : false,
		autoScroll : true,
		height : parseInt(Ext.getDom('container_div_supplierStockInStatistics').parentElement.style.height.replace(/px/g, '')) - 40,
		width : parseInt(Ext.getDom('container_div_supplierStockInStatistics').parentElement.style.width.replace(/px/g, '')),
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
//				Ext.getCmp('represent_btnSearch').handler();
			}
		}
	});
	
	
	store.on('load', function(){
		if(store.getCount() > 0){
			var sumRow = stockInPanel.getView().getRow(store.getCount() - 1);
			sumRow.style.backgroundColor = '#EEEEEE';
			for(var i = 0; i < stockInPanel.getColumnModel().getColumnCount(); i++){
				var sumCell = stockInPanel.getView().getCell(store.getCount() - 1, i);
				sumCell.style.fontSize = '15px';
				sumCell.style.fontWeight = 'bold';
				sumCell.style.color = 'green';
			}
			
			stockInPanel.getView().getCell(store.getCount() - 1 , 1).innerHTML = '汇总';
			stockInPanel.getView().getCell(store.getCount() - 1, 7).innerHTML = '--';
		}
	});
	
	new Ext.Panel({
		title : '供应商采购统计',
		frame : true,
		renderTo : 'container_div_supplierStockInStatistics',
		items : [stockInPanel]
		
	});
	
	dateCombo.setValue('1');
	dateCombo.fireEvent('select', dateCombo, null, 1);
});