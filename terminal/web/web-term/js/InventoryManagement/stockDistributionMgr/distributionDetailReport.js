Ext.onReady(function(){
	var stockOut = [[-1, '全部'], [11, '配送发货'], [13, '配送退货']];
	var stockIn = [[-1, '全部'], [12, '配送收货'], [14, '配送回收']];
	var stockType = {
		STOCKIN : {val : 1, desc : '入库'},
		STOCKOUT : {val : 2, desc : '出库'}
	}
	var limitCount = 20;

	function stockDetailHandler(orderID) {
		billDetailWin.show();
		billDetailWin.setTitle('库存单 ');
		billDetailWin.center();
	};

	var materialTypeDate = [[-1,'全部'],[1,'商品'],[2,'原料']];
	var materialTypeComb;
	materialTypeComb = new Ext.form.ComboBox({
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
	        	
	        	Ext.getCmp('btnSearch_distributionDetail').handler();
			}  
		}
		
	});
	
	var sDetail_materialCateStore = new Ext.data.Store({
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
	        	
	        	Ext.getCmp('btnSearch_distributionDetail').handler();	
			}

		}
	});
	
	var sDetail_materialStore = new Ext.data.Store({
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
	
	var materialComb;
	materialComb = new Ext.form.ComboBox({
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
				Ext.getCmp('btnSearch_distributionDetail').handler();
			}
			
		}
	});

	function renderFormat(v){
		if(typeof(v) != 'string'){
			v = Ext.ux.txtFormat.gridDou(v);
		}
		return v;
	}

	
	Ext.form.Field.prototype.msgTarget = 'side';
	
	function getStockOutMoney(data, colCls, json){
		return data%2 ? (json.json.totalPrice).toFixed(2) : 0;
	}
	
	function getStockOutAmount(data, colCls, json){
		return data%2 ? json.json.amount : 0;
	}
	
	function getStockInMoney(data, colCls, json){
		return data%2 ? 0 : (json.json.totalPrice).toFixed(2);
	}
	
	function getStockInAmount(data, colCls, json){
		return data%2 ? 0 : json.json.amount;
	}
	//定义列模型
	var cm = new Ext.grid.ColumnModel([
	         new Ext.grid.RowNumberer(),
	         {header:'日期', dataIndex:'oriStockDateFormat'},
	         {header:'库单号', dataIndex:'id', align : 'center'},
	         {header:'关联单号', dataIndex:'associateId'},
	         {header:'货品名称', dataIndex:'material', width:160, renderer : function(data){
	         	return data ? data.name : '----';
	         }},
	         {header:'出库门店', dataIndex:'stockOutRestaurant', renderer : function(data){
	         	return data ? data.name : '----';
	         }},
	         {header: '入库门店', dataIndex : 'stockInRestaurant', renderer : function(data){
	         	return data ? data.name : '----';
	         }},
	         {header:'库单类型', dataIndex:'subTypeText', width:100},
	         {header:'入库数量', dataIndex:'stockOutAmount', align : 'right', render : getStockOutAmount},
	         {header:'入库金额', dataIndex:'stockOutMoney', align : 'right', render : getStockOutMoney},
	         {header:'出库数量', dataIndex:'stockInAmount', align : 'right', render : getStockInAmount},
	         {header:'出库金额', dataIndex:'stockInMoney', align : 'right', render : getStockInMoney},
	         {header:'操作人', dataIndex:'operator', align : 'center'}
	]);
	cm.defaultSortable = true;
	//数据加载器
	var ds = new Ext.data.Store({
		proxy : new Ext.data.HttpProxy({url:'../../DistributionDetailReport.do'}),
		reader : new Ext.data.JsonReader({totalProperty:'totalProperty', root : 'root'}, [
	         {name : 'oriStockDateFormat'},
	         {name : 'id'},
	         {name : 'associateId'},
	         {name : 'material'},
	         {name : 'stockOutRestaurant'},
	         {name : 'stockInRestaurant'},
	         {name : 'subTypeText'},
	         {name : 'stockOutAmount'},
	         {name : 'stockOutMoney'},
	         {name : 'stockInAmount'},
	         {name : 'stockInMoney'},
	         {name : 'operator'}
		])
	});
	
	//日期默认为本月
	var date = new Date(new Date().getFullYear(), new Date().getMonth(), 1);
	
	
	var dateBegin = new Ext.form.DateField({
		id : 'beginDate_combo_distributionDetail',
		xtype : 'datefield',
		format : 'Y-m-d',
		maxValue : new Date(),
		readOnly : false,
		allowBlank : false
	});
	
	var dateEnd = new Ext.form.DateField({
		id : 'endDate_combo_distributionDetail',
		xtype : 'datefield',
		format : 'Y-m-d',
		maxValue : new Date(),
		readyOnly : false,
		allowBlank : false
	});
	
	var dateCombo;
	dateCombo = Ext.ux.createDateCombo({
		beginDate : dateBegin,
		endDate : dateEnd,
		callback : function(){
			Ext.getCmp('btnSearch_distributionDetail').handler();
		}
	});
	
	var stockInRestaurant;
	stockInRestaurant = new Ext.form.ComboBox({
		width : 110,
		readOnly : false,
		valueField : 'id',
		displayField : 'name',
		typeAhead : true,
		mode : 'local',
		triggerAction : 'all',
		selectOnFocus : true,
		allowBlank : false,
		blankText : '收货门店不能为空',
		store : new Ext.data.SimpleStore({
			fields : ['id', 'name']
		}),
		listeners : {
			render : function(){
				Ext.Ajax.request({
					url : '../../OperateRestaurant.do',
					params : {
						dataSource : 'getByCond',
						id : restaurantID
					},
					success : function(res, opt){
						var jr = Ext.decode(res.responseText);
						var data = [];
   						if(jr.success){
   							if(jr.root[0].typeVal == '3'){
	   							data.push([-1, '全部']);
								Ext.Ajax.request({
									url : '../../OperateRestaurant.do',
									params : {
										dataSource : 'getGroupRestaurant'
									},
									success : function(res, opt){
										var jr2 = Ext.decode(res.responseText);
										data.push([jr2.root[0]['id'], jr2.root[0]['name']]);
										data.push([jr.root[0]['id'], jr.root[0]['name']]);
										stockInRestaurant.store.loadData(data);
										stockInRestaurant.setValue(-1);
									},
									failure : function(res, opt){
										Ext.example.msg('错误提示', Ext.decode(res.responseText).msg);
									}
								});
							}else if(jr.root[0].typeVal == '2'){
								data.push([-1, '全部']);
								data.push([jr.root[0]['id'], jr.root[0]['name']]);
								for(var i = 0; i < jr.root[0].branches.length; i++){
									data.push([jr.root[0].branches[i]['id'], jr.root[0].branches[i]['name']]);
								}
								
								stockInRestaurant.store.loadData(data);
								stockInRestaurant.setValue(-1);
							}else{
								data.push([jr.root[0]['id'], jr.root[0]['name']]);
								stockInRestaurant.store.loadData(data);
								stockInRestaurant.setValue(jr.root[0]['id']);
							}
   						}
					},
					failure : function(res, opt){
						Ext.example.msg('错误提示', Ext.decode(res.responseText).msg);
					}
				});
			},
			select : function(){
				Ext.getCmp('btnSearch_distributionDetail').handler();
			}
		}
	});
	
	var stockOutRestaurant;
	stockOutRestaurant = new Ext.form.ComboBox({
		width : 120,
		readOnly : false,
		valueField : 'id',
		displayField : 'name',
		typeAhead : true,
		mode : 'local',
		triggerAction : 'all',
		selectOnFocus : true,
		allowBlank : false,
		blankText : '出货门店不能为空',
		store : new Ext.data.SimpleStore({
			fields : ['id', 'name']
		}),
		listeners : {
			render : function(){
				Ext.Ajax.request({
					url : '../../OperateRestaurant.do',
					params : {
						dataSource : 'getByCond',
						id : restaurantID
					},
					success : function(res, opt){
						var jr = Ext.decode(res.responseText);
						var data = [];
   						if(jr.success){
   							if(jr.root[0].typeVal == '3'){
	   							data.push([-1, '全部']);
								Ext.Ajax.request({
									url : '../../OperateRestaurant.do',
									params : {
										dataSource : 'getGroupRestaurant'
									},
									success : function(res, opt){
										var jr2 = Ext.decode(res.responseText);
										data.push([jr2.root[0]['id'], jr2.root[0]['name']]);
										data.push([jr.root[0]['id'], jr.root[0]['name']]);
										stockOutRestaurant.store.loadData(data);
										stockOutRestaurant.setValue(-1);
									},
									failure : function(res, opt){
										Ext.example.msg('错误提示', Ext.decode(res.responseText).msg);
									}
								});
							}else if(jr.root[0].typeVal == '2'){
								data.push([-1, '全部']);
								data.push([jr.root[0]['id'], jr.root[0]['name']]);
								for(var i = 0; i < jr.root[0].branches.length; i++){
									data.push([jr.root[0].branches[i]['id'], jr.root[0].branches[i]['name']]);
								}
								
								stockOutRestaurant.store.loadData(data);
								stockOutRestaurant.setValue(-1);
							}else{
								data.push([jr.root[0]['id'], jr.root[0]['name']]);
								stockOutRestaurant.store.loadData(data);
								stockOutRestaurant.setValue(jr.root[0]['id']);
							}
   						}
					},
					failure : function(res, opt){
						Ext.example.msg('错误提示', Ext.decode(res.responseText).msg);
					}
				});
			},
			select : function(){
				Ext.getCmp('btnSearch_distributionDetail').handler();
			}
		}
	});
	
	var detailReportBar = new Ext.Toolbar({
		items : [{
			xtype : 'tbtext',
			text : '&nbsp;&nbsp;&nbsp;&nbsp;日期：'
		},
		dateCombo,'　',
		dateBegin,{
			xtype : 'tbtext',
			text : '&nbsp;&nbsp;至&nbsp;&nbsp;'
		},
		dateEnd, {
			xtype : 'tbtext',
			text : '&nbsp;'
		},{
			xtype : 'tbtext',
			text : '类型:'
		},materialTypeComb,{
			xtype : 'tbtext',
			text : '类别:'
		},materialCateComb,{
			xtype : 'tbtext',
			text : '货品:'
		},materialComb,{
			xtype:'tbtext',
			text:'&nbsp;&nbsp;单据编号:'
		},{
			xtype : 'textfield',
			width : 100,
			id : 'fuzzyId_distributionDetail'
		},'->', {
			text : '刷新',
			id : 'btnSearch_distributionDetail',
			iconCls : 'btn_refresh',
			handler : function(){
				var gridPanelStore = stockDetailReportGrid.store;
				gridPanelStore.baseParams['beginDate'] = Ext.getCmp('beginDate_combo_distributionDetail').getValue();
				gridPanelStore.baseParams['endDate'] = Ext.getCmp('endDate_combo_distributionDetail').getValue();
				gridPanelStore.baseParams['materialCate'] = materialCateComb.getValue();
				gridPanelStore.baseParams['materialType'] = materialTypeComb.getValue();
				gridPanelStore.baseParams['materialId'] = materialComb.getValue();
				gridPanelStore.baseParams['fuzzyId'] = Ext.getCmp('fuzzyId_distributionDetail').getValue();
				gridPanelStore.baseParams['stockOutRestaurant'] = stockOutRestaurant.getValue();
				gridPanelStore.baseParams['stockInRestaurant'] = stockInRestaurant.getValue();
				gridPanelStore.baseParams['stockType'] = Ext.getCmp('stockType_distributionDetail').getValue();
				gridPanelStore.baseParams['subType'] = Ext.getCmp('subType_distritbuionDetail').getValue();
				gridPanelStore.baseParams['comment'] = Ext.getCmp('comment_distributionDetail').getValue();
				stockDetailReportGrid.store.load({
					baseParams : {
						start : 0,
						limit : limitCount
					}
				});
			}
		} ,{xtype : 'tbtext', text : '&nbsp;&nbsp;'}
		]
	});
	
	var detailReportSecondBar = new Ext.Toolbar({
		items : [{
			xtype : 'label',
			text : '出库门店:'
		}, stockOutRestaurant,{
			xtype : 'label',
			text : '入库门店:'
		}, stockInRestaurant,{
			xtype : 'tbtext',
			text : '&nbsp;'
		},{
			xtype:'tbtext',
			text:'&nbsp;&nbsp;'
		},{
			xtype : 'tbtext',
			text : '&nbsp;&nbsp;货单类型:'
		}, {
			xtype : 'combo',
			id : 'stockType_distributionDetail',
			readOnly : false,
			forceSelection : true,
			width : 60,
			value : -1,
			store : new Ext.data.SimpleStore({
				data : [[-1, '全部'], [stockType.STOCKIN.val, stockType.STOCKIN.desc], [stockType.STOCKOUT.val, stockType.STOCKOUT.desc]],
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
					var subType = Ext.getCmp('subType_distritbuionDetail');
					if(Ext.getCmp('stockType_distributionDetail').getValue() == 1){
						subType.store.loadData(stockIn);
						subType.setValue(-1);
					}else if(Ext.getCmp('stockType_distributionDetail').getValue() == 2){
						subType.store.loadData(stockOut);
						subType.setValue(-1);
					}else{
						subType.store.loadData([]);
						subType.setValue('')
					}
					Ext.getCmp('btnSearch_distributionDetail').handler();
				}
			}
		}, {
			xtype : 'tbtext',
			text : '&nbsp;&nbsp;业务类型:'
		}, {
			xtype : 'combo',
			id : 'subType_distritbuionDetail',
			readOnly : false,
			forceSelection : true,
			width : 90,
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
					Ext.getCmp('btnSearch_distributionDetail').handler();
				}
			}
		},{
			xtype:'tbtext',
			text:'&nbsp;&nbsp;'
		},{
			xtype:'tbtext', 
			text:'&nbsp;&nbsp;备注：'
		},{
			xtype : 'textfield', 
			width : 180, 
			id : 'comment_distributionDetail'
		}]
	});	
	
	
	var pagingBar = new Ext.PagingToolbar({
		id : 'pagingToolbar_stockDistribution',
		pageSize : limitCount,
		store : ds,
		displayInfo : true,
		displayMsg : '显示第 {0} 条到 {1} 条记录，共 {2} 条',
		emptyMsg : '没有记录'
	});

	var stockDetailReportGrid;
	stockDetailReportGrid = new Ext.grid.GridPanel({
		title : '配送明细',
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
		keys : {
			key : 13, //enter键
			scope : this,
			fn : function(){
				Ext.getCmp('btnSearch_distributionDetail').handler();
			}
		},
		tbar : detailReportBar,
		bbar : pagingBar,
		listeners : {
			render : function(){
				detailReportSecondBar.render(stockDetailReportGrid.tbar);
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
//			stockDetailReportGrid.getView().getCell(store.getCount()-1, 8).innerHTML = '--';
			stockDetailReportGrid.getView().getCell(store.getCount()-1, 12).innerHTML = '--';
//			stockDetailReportGrid.getView().getCell(store.getCount()-1, 15).innerHTML = '--';
		}
		
	});	
	
	
   new Ext.Panel({
		renderTo : 'mainContainer_div_distributionDetailReport',
		width : parseInt(Ext.getDom('mainContainer_div_distributionDetailReport').parentElement.style.width.replace(/px/g,'')),
		height : parseInt(Ext.getDom('mainContainer_div_distributionDetailReport').parentElement.style.height.replace(/px/g,'')),
		layout : 'border',//布局
		items : [stockDetailReportGrid],
		keys : [{
			key : Ext.EventObject.ENTER,
			scope : this,
			fn : function(){
//				Ext.getCmp('stockDetail_btnSearch').handler();
			}
		}],
		listeners : {
			render : function(){
//				Ext.getCmp('stockDetail_btnSearch').handler();
			}
		}
	});
   
	dateCombo.setValue(1);
	dateCombo.fireEvent('select');
//	Ext.getCmp('stockDetail_btnSearch').handler();
	
});