Ext.onReady(function(){
	var limitCount = 20;
	var stockForm = new Ext.form.FormPanel({  
		height : 200,
	    region : 'north',
	    id : 'stockForm',
	    labelAlign : 'center',
	    bodyStyle :'padding:5px',
	    frame : true,
	    layout : 'column',   //定义该元素为布局为列布局方式
	    border : false,
		reader : new Ext.data.JsonReader({totalProperty:'totalProperty', root : 'root'}, [
	         {name : 'oriStockId'},
	         {name : 'oriStockDateFormat'},
	         {name : 'operatorName'},
	         {name : 'birthDateFormat'},
	         {name : 'typeText'},
	         {name : 'amount'},
	         {name : 'stockInName'},
	         {name : 'supplierName'},
	         {name : 'approverName'},
	         {name : 'subTypeText'},
	         {name : 'price'},
	         {name : 'stockOutName'},
	         {name : 'approverDateFormat'}
		]),
	    items: [{  
	        columnWidth :.4,  //该列占用的宽度，标识为50％
	        layout : 'form',
	        border :false,
	        defaults : {anchor: '95%'},  
	        // 第一列中的表项
	        items :[  
	                {xtype: 'textfield',fieldLabel: '原始货单',name: 'oriStockId',disabled:true},  
	                {xtype: 'textfield',fieldLabel: '操作人',name: 'operatorName',disabled:true},  
	                {xtype: 'textfield',fieldLabel: '货单大类',name: 'typeText',disabled:true},
	                {xtype: 'textfield',fieldLabel: '总数量',name: 'amount',disabled:true},
	                {xtype: 'textfield',fieldLabel: '入货部门',name: 'stockInName',disabled:true},
	                {xtype: 'textfield',fieldLabel: '供应商',name: 'supplierName',disabled:true},
	                {xtype: 'textfield',fieldLabel: '审核人',name: 'approverName',disabled:true}
	         ]},
	    {
	         columnWidth : .4,  
	         layout : 'form',  
	         border :false,
	         defaults : {anchor: '95%'},  
	         // 第二列中的表项
	         items :[  
	             {xtype:'textfield', fieldLabel:'原始日期', name:'oriStockDateFormat', disabled:true},  
	             {xtype:'textfield', fieldLabel:'操作时间', name:'birthDateFormat', disabled:true},  
	             {xtype:'textfield', fieldLabel:'货单小类', name:'subTypeText', disabled:true},
	             {xtype:'textfield', fieldLabel:'总金额', name:'price', disabled:true},
	             {xtype:'textfield', fieldLabel:'出货部门', name:'stockOutName', disabled:true},
	             {xtype:'textfield', fieldLabel:'审核时间', name:'approverDateFormat', disabled:true}
	         ]
	    }]	
	});





	var stockDetail = new Ext.grid.ColumnModel([
		 new Ext.grid.RowNumberer(),
		 {header:'品项名称', dataIndex:'materialName', width:160},
		 {header:'数量', dataIndex:'amount', width:140, align:'right'},
		 {header:'单价', dataIndex:'price', width:120, align:'right', renderer : renderFormat},
		 {header:'结存数量', dataIndex:'remaining', width:140, align:'right'}
	]);

	stockDetail.defaultSortable = true;

	var stockDetailStore = new Ext.data.Store({
		//proxy : new Ext.data.MemoryProxy(data),
		proxy : new Ext.data.HttpProxy({url:'../../QueryStockActionDetail.do'}),
		reader : new Ext.data.JsonReader({totalProperty:'totalProperty', root : 'root'}, [
	         {name : 'materialName'},
	         {name : 'amount'},
	         {name : 'price'},
	         {name : 'remaining'}
		])
	});

	var stockDetailGrid = new Ext.grid.GridPanel({
		title : '库存明细',
		id : 'detailGrid',
		region : 'center', 
		border : true,
		frame : true,
		store : stockDetailStore,
		//ds : ds,
		cm : stockDetail

	});
	//stockDetailStore.load();
	var billDetailWin = new Ext.Window({
		layout : 'border',
		width : 650,
		//autoHeight: true,
		height : 500,
		closable : false,
		resizable : false,
		modal : true,
		items : [stockForm, stockDetailGrid],
		bbar : ['->', {
			text : '关闭',
			iconCls : 'btn_close',
			handler : function() {
				billDetailWin.hide();
			}
		}]
	});

	function stockDetailHandler(orderID) {
		billDetailWin.show();
		billDetailWin.setTitle('库存单 ');
		billDetailWin.center();
	};

	var materialTypeDate = [[-1,'全部'],[1,'商品'],[2,'原料']];
	var materialTypeComb = new Ext.form.ComboBox({
		forceSelection : true,
		width : 90,
		id : 'materialType',
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
		readOnly : false	,
		listeners : {
	        select : function(combo, record, index){  
	        	materialCateComb.reset();
	        	materialComb.allowBlank = true;
	        	materialComb.reset();
	        	sDetail_materialCateStore.load({  
		            params: {  
		            	type : combo.value,  
		            	dataSource : 'normal'
		            }  
	            });     
	        	sDetail_materialStore.load({
	        		params: {
	        			cateType : combo.value,
	        			dataSource : 'normal'
	        		}
	        	});
	        	
	        	Ext.getCmp('stockDetail_btnSearch').handler();		
			}  
		}
		
	});
	var sDetail_materialCateStore = new Ext.data.Store({
		//proxy : new Ext.data.MemoryProxy(data),
		proxy : new Ext.data.HttpProxy({url:'../../QueryMaterialCate.do?restaurantID=' + restaurantID}),
		reader : new Ext.data.JsonReader({totalProperty:'totalProperty', root : 'root'}, [
		         {name : 'id'},
		         {name : 'name'}
		])
	});
	sDetail_materialCateStore.load({  
	    params: {  
	    	type : materialTypeComb.value,  
	    	dataSource : 'normal'
	    }
	}); 
	var materialCateComb = new Ext.form.ComboBox({
		forceSelection : true,
		width : 90,
		id : 'materialCate',
		store : sDetail_materialCateStore,
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
	        	sDetail_materialStore.load({  
		            params: {  
		            	cateType : materialTypeComb.value,
		            	cateId : combo.value,  
		            	dataSource : 'normal'
		            }  
	            });     
	        	
	        	Ext.getCmp('stockDetail_btnSearch').handler();		
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
	    	cateType : materialTypeComb.value,
	    	dataSource : 'normal'
	    }  
	}); 
	var materialComb = new Ext.form.ComboBox({
		forceSelection : true,
		width : 100,
		listWidth : 250,
		maxheight : 300,
		id : 'materialId_stockDetail',
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
				Ext.getCmp('stockDetail_btnSearch').handler();		
			}
			
		}
	});

	function renderFormat(v){
		if(typeof(v) != 'string'){
			v = Ext.ux.txtFormat.gridDou(v);
		}
		return v;
	}

	var stockDetailReportTree;	
	var stockInDate = [[1, '采购'], [2, '领料'], [3, '其他入库'], [7, '盘盈']];
	var stockOutDate = [[4, '退货'], [5, '退料'], [6, '其他出库'], [8, '盘亏'], [9, '消耗']];//, [5, '退料']
	var stock = [[-1, '全部'], [1, '入库'], [2, '出库']];
	
	
	Ext.form.Field.prototype.msgTarget = 'side';
	
	//定义列模型
	var cm = new Ext.grid.ColumnModel([
	         new Ext.grid.RowNumberer(),
//	         {header:'id', dataIndex:'id', hidden: true },
	         {header:'日期', dataIndex:'date'},
	         {header:'库单号', dataIndex:'stockActionId', align : 'center'},
	         {header:'原始单号', dataIndex:'oriStockId'},
	         {header:'货品名称', dataIndex:'materialName', width:160},
	         {header:'供应商', dataIndex:'supplier'},
	         {header:'出库部门', dataIndex:'deptOut'},
	         {header: '入库部门', dataIndex : 'deptIn'},
	         {header:'入库类型', dataIndex:'stockInSubType', width:100},
	         {header:'入库数量', dataIndex:'stockInAmount', align : 'right', renderer : renderFormat},
	         {header:'入库金额', dataIndex:'stockInMoney', align : 'right', renderer : renderFormat},
	         {header:'出库类型', dataIndex:'stockOutSubType', width:100},
	         {header:'出库数量', dataIndex:'stockOutAmount', align : 'right', renderer : renderFormat},
	         {header:'出库金额', dataIndex:'stockOutMoney', align : 'right', renderer : renderFormat},
	         {header:'结存数量', dataIndex:'remaining', align : 'right', renderer : renderFormat},
	         {header:'操作人', dataIndex:'operater'}
	]);
	cm.defaultSortable = true;
	//数据加载器
	var ds = new Ext.data.Store({
		//proxy : new Ext.data.MemoryProxy(data),
		proxy : new Ext.data.HttpProxy({url:'../../QueryStockDetailReport.do'}),
		reader : new Ext.data.JsonReader({totalProperty:'totalProperty', root : 'root'}, [
	         {name : 'id'},                                                                         
	         {name : 'date'},
	         {name : 'stockActionId'},
	         {name : 'oriStockId'},
	         {name : 'supplier'},
	         {name : 'materialName'},
	         {name : 'deptOut'},
	         {name : 'deptIn'},
	         {name : 'stockInSubType'},
	         {name : 'stockInAmount'},
	         {name : 'stockInMoney'},
	         {name : 'stockOutSubType'},
	         {name : 'stockOutAmount'},
	         {name : 'stockOutMoney'},
	         {name : 'remaining'},
	         {name : 'operater'}
		])
	});
	
	//日期默认为本月
	var date = new Date(new Date().getFullYear(), new Date().getMonth(), 1);
	
	
	//出库部门combo
	var stockOutDeptComb = new Ext.form.ComboBox({
		id : 'stockOutDeptComb_comboBox_stockDetailPeport',
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
				Ext.getCmp('stockDetail_btnSearch').handler();	
			}
			
		}
	});
	
	//入库部门combo
	var stockInDeptComb = new Ext.form.ComboBox({
		id : 'stockInDeptComb_comboBox_stockDetailPeport',
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
				Ext.getCmp('stockDetail_btnSearch').handler();	
			}
			
		}
	});
	
	var detailReportBar = new Ext.Toolbar({
		items : [
		{ xtype:'tbtext', text:'日期:'},
		{
			xtype : 'datefield',
			id : 'sdr_beginDate',
			allowBlank : false,
			maxValue : new Date(),
			value : new Date(),
            width:100,  
            plugins: 'monthPickerPlugin',  
            format: 'Y-m'			
		},
		{xtype : 'tbtext', text : '&nbsp;'},
		{xtype : 'tbtext', text : '类型:'},
		materialTypeComb,
		{xtype : 'tbtext', text : '类别:'},
		materialCateComb,
		{xtype : 'tbtext', text : '货品:'},
		materialComb,
		{xtype:'tbtext', text:'&nbsp;&nbsp;'},
		'->', {
			text : '刷新',
			id : 'stockDetail_btnSearch',
			iconCls : 'btn_refresh',
			handler : function(){
				var store = stockDetailReportGrid.getStore();
				store.baseParams['beginDate'] = Ext.getCmp('sdr_beginDate').getValue().format('Y-m');
				store.baseParams['deptOut'] = Ext.getCmp('stockOutDeptComb_comboBox_stockDetailPeport').getStore().getCount() > 0 ? Ext.getCmp('stockOutDeptComb_comboBox_stockDetailPeport').getValue() : '-1';
				store.baseParams['deptIn'] = Ext.getCmp('stockInDeptComb_comboBox_stockDetailPeport').getValue();
				store.baseParams['materialId'] = Ext.getCmp('materialId_stockDetail').getValue();
				store.baseParams['materialCateId'] = Ext.getCmp('materialCate').getValue();
				store.baseParams['cateType'] = Ext.getCmp('materialType').getValue();
				store.baseParams['stockType'] = Ext.getCmp('sdr_comboSearchForStockType').getValue();
				store.baseParams['subType'] = Ext.getCmp('sdr_comboSearchForSubType').getValue();
				store.baseParams['supplier'] = Ext.getCmp('comboSearchSupplierForDetail').getValue();
				//TODO
				
				stockDetailReportGrid.getStore().load({
					params : {
						start : 0,
						limit : limitCount
					}
				});
			}
		},'-', {
			text : '导出',
			iconCls : 'icon_tb_exoprt_excel',
			handler : function(){
//				var sn = stockDetailReportTree.getSelectionModel().getSelectedNode();
				var url = "../../{0}?dataSource={1}&beginDate={2}&deptIn={3}&deptOut={4}&materialId={5}&materialCateId={6}&cateType={7}&stockType={8}&subType={9}&supplier={10}";
				url = String.format(
					url,
					'ExportHistoryStatisticsToExecl.do',
					'stockActionDetail',
					Ext.getCmp('sdr_beginDate').getValue().format('Y-m'),
					Ext.getCmp('stockOutDeptComb_comboBox_stockDetailPeport').getValue(),
					Ext.getCmp('stockInDeptComb_comboBox_stockDetailPeport').getValue(),
					Ext.getCmp('materialId_stockDetail').getValue(),
					Ext.getCmp('materialCate').getValue(),
					Ext.getCmp('materialType').getValue(),
					Ext.getCmp('sdr_comboSearchForStockType').getValue(),
					Ext.getCmp('sdr_comboSearchForSubType').getValue(),
					Ext.getCmp('comboSearchSupplierForDetail').getValue()
				);
				window.location = url;
			}
		},{xtype : 'tbtext', text : '&nbsp;&nbsp;'}
		]
	});
	
	var detailReportSecondBar = new Ext.Toolbar({
		items : [{
			xtype : 'label',
			text : '出库部门:'
		}, stockOutDeptComb,{
			xtype : 'label',
			text : '入库部门:'
		}, stockInDeptComb,{
			xtype : 'tbtext',
			text : '&nbsp;'
		},
		{
			xtype : 'tbtext',
			text : '货单类型:'
		}, {
			xtype : 'combo',
			id : 'sdr_comboSearchForStockType',
			readOnly : false,
			forceSelection : true,
			width : 100,
			value : -1,
			store : new Ext.data.SimpleStore({
				data : stock,
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
				select : function(thiz){
					var subType = Ext.getCmp('sdr_comboSearchForSubType');
					if(thiz.getValue() == 1){
						subType.store.loadData(stockInDate);
						subType.setValue(1);
					}else if(thiz.getValue() == 2){
						subType.store.loadData(stockOutDate);
						subType.setValue(4);
					}else{
						subType.store.loadData('');
						subType.setValue('');
					}
					Ext.getCmp('stockDetail_btnSearch').handler();
				}
			}
		}, {
			xtype : 'tbtext',
			text : '业务类型:'
		}, {
			xtype : 'combo',
			id : 'sdr_comboSearchForSubType',
			readOnly : false,
			forceSelection : true,
			width : 100,
			store : new Ext.data.SimpleStore({
				fields : ['value', 'text']
			}),
			valueField : 'value',
			displayField : 'text',
			typeAhead : true,
			mode : 'local',
			triggerAction : 'all',
			selectOnFocus : true,
			listeners : {
				select : function(){
					Ext.getCmp('stockDetail_btnSearch').handler();
				}
			}
		},

		{xtype:'tbtext', text:'&nbsp;&nbsp;'},
		{
			xtype : 'tbtext',
			text : '&nbsp;&nbsp;供应商:'
		}, {
			xtype : 'combo',
			id : 'comboSearchSupplierForDetail',
			readOnly : false,
			forceSelection : true,
			width : 100,
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
						params : {
							
						},
						success : function(res, opt){
							var jr = Ext.decode(res.responseText);
							for(var i = 0; i < jr.root.length; i++){
								data.push([jr.root[i]['supplierID'], jr.root[i]['name']]);
							}
							thiz.store.loadData(data);
							thiz.setValue(-1);
						},
						failure : function(res, opt){
							thiz.store.loadData(data);
							thiz.setValue(-1);
						}
					});
				},
				select : function(){
					Ext.getCmp('stockDetail_btnSearch').handler();
				}
			}
			
		}
		]
	});	
	
	
	var pagingBar = new Ext.PagingToolbar({
		pageSize : limitCount,
		store : ds,
		displayInfo : true,
		displayMsg : '显示第 {0} 条到 {1} 条记录，共 {2} 条',
		emptyMsg : '没有记录'
	});


	var stockDetailReportGrid = new Ext.grid.GridPanel({
		title : '进销存明细',
		id : 'stock_detail_grid',
		region : 'center',
		height : '500',
		border : true,
		frame : true,
		store : ds,
		cm : cm,
		viewConfig : {
			forceFit : true
		},
		loadMask : {
			msg : '正在读取数据中...'
		},
		tbar : detailReportBar,
		bbar : pagingBar,
		listeners : {
			render : function(){
				detailReportSecondBar.render(stockDetailReportGrid.tbar);
			},
			rowdblclick : function(grid, rowindex, e){ 
				var id = -1;
			    grid.getSelectionModel().each(function(rec){   
			         	//alert(rec.get('oriStockId'));//记录中的字段名
			         	id = rec.get('id');
			    });   
				stockForm.form.load({
					url:'../../QueryStockAction.do?'+ strEncode('restaurantID=' + restaurantID + '&id'+ id, KEYS)
				});
				var detailds = stockDetailGrid.getStore();
				detailds.load({
					params : {
						id : id
					}
				});
				stockDetailHandler();
			}
		}

	});
	
	
	//汇总
	stockDetailReportGrid.getStore().on('load', function(store, records, options){
		var sumRow = null;
		if(store.getCount() > 0){

			sumRow = stockDetailReportGrid.getView().getRow(store.getCount() - 1);	
			sumRow.style.backgroundColor = '#EEEEEE';			
			for(var i = 0; i < stockDetailReportGrid.getColumnModel().getColumnCount(); i++){
				var sumCell = stockDetailReportGrid.getView().getCell(store.getCount() - 1, i);
				sumCell.style.fontSize = '15px';
				sumCell.style.fontWeight = 'bold';
				sumCell.style.color = 'green';
			}
			
			
			stockDetailReportGrid.getView().getCell(store.getCount()-1, 1).innerHTML = '汇总';
			stockDetailReportGrid.getView().getCell(store.getCount()-1, 2).innerHTML = '--';
			stockDetailReportGrid.getView().getCell(store.getCount()-1, 3).innerHTML = '--';
			stockDetailReportGrid.getView().getCell(store.getCount()-1, 4).innerHTML = '--';
			stockDetailReportGrid.getView().getCell(store.getCount()-1, 5).innerHTML = '--';
			stockDetailReportGrid.getView().getCell(store.getCount()-1, 6).innerHTML = '--';
			stockDetailReportGrid.getView().getCell(store.getCount()-1, 7).innerHTML = '--';
			stockDetailReportGrid.getView().getCell(store.getCount()-1, 8).innerHTML = '--';
			stockDetailReportGrid.getView().getCell(store.getCount()-1, 11).innerHTML = '--';
			stockDetailReportGrid.getView().getCell(store.getCount()-1, 15).innerHTML = '--';
		}
		
	});	
	
	
   new Ext.Panel({
		renderTo : 'divStockDetail',
		width : parseInt(Ext.getDom('divStockDetail').parentElement.style.width.replace(/px/g,'')),
		height : parseInt(Ext.getDom('divStockDetail').parentElement.style.height.replace(/px/g,'')),
		layout : 'border',//布局
		//margins : '5 5 5 5',
		//子集
		items : [stockDetailReportGrid],
		keys : [{
			key : Ext.EventObject.ENTER,
			scope : this,
			fn : function(){
				Ext.getCmp('stockDetail_btnSearch').handler();
			}
		}],
		listeners : {
			render : function(){
				//TODO
//				Ext.getCmp('stockDetail_btnSearch').handler();
			}
		}
	});
   
   Ext.getCmp('stockDetail_btnSearch').handler();
});