Ext.onReady(function(){
	

	var stockActionHead = {

		region : 'north',
		height : 120,
		frame : true,
		items : [{
			id : 'displayPanelForHistoryStockTitle',
			height : 30,
			bodyStyle : 'font-size:18px;text-align:center;',
			html : '-----'
		}, {
			xtype : 'panel',
			layout : 'column',
			defaults : {
				xtype : 'form',
				layout : 'form',
				style : 'width:218px;',
				labelWidth : 60,
				columnWidth : .25,
				defaults : { width : 120 }
			},
			items : [{
				items : [{
					xtype : 'hidden',
					id : 'hideHistoryStockActionId'
				}]
			}, 
			{
				id : 'displayPanelForHistoryDeptIn',
				items : [{
					id : 'txtDeptInForHistoryStockActionBasic',
					xtype : 'textfield',
					fieldLabel : '收货仓',
					disabled : true
				}]
			}, 
			{
				id : 'displayPanelForHistorySupplier',
				items : [{
					id : 'txtSupplierForHistoryStockActionBasic',
					xtype : 'textfield',
					fieldLabel : '供应商',
					disabled : true
				}]
			}, {
				id : 'displayPanelForHistoryDeptOut',
				items : [{
					id : 'txtDeptOutForHistoryStockActionBasic',
					xtype : 'textfield',
					fieldLabel : '出货仓',
					disabled : true
				}]
			}, {
				items : [{
					id : 'txtOriStockIdForHistoryStockActionBasic',
					xtype : 'textfield',
					fieldLabel : '原始单号',
					disabled : true
				}]
			}, {
				items : [{
					id : 'txtOriStockDateForHistoryStockActionBasic',
					xtype : 'textfield',
					fieldLabel : '货单日期',
					disabled : true
				}]
			}, {
				columnWidth : 1,
				style : 'width:100%;',
				items : [{
					id : 'txtCommentForHistoryStockActionBasic',
					xtype : 'textfield',
					width : 774,
					fieldLabel : '备注',
					disabled : true
				}]
			}, {
				items : [{
					id : 'txtApproverNameForHistoryStockActionBasic',
					xtype : 'textfield',
					fieldLabel : '审核人',
					disabled : true
				}]
			}, {
				items : [{
					id : 'dateApproverDateForHistoryStockActionBasic',
					xtype : 'textfield',
					fieldLabel : '审核日期',
					disabled : true
				}]
			}, {
				items : [{
					id : 'txtOperatorNameForHistoryStockActionBasic',
					xtype : 'textfield',
					fieldLabel : '制单人',
					disabled : true
				}]
			}, {
				items : [{
					id : 'dateOperatorDateForHistoryStockActionBasic',
					xtype : 'textfield',
					fieldLabel : '制单日期',
					disabled : true
				}]
			}]
		}]
	};

	var stockActionPanelSouth = {
			id : 'stockActionPanelSouth',
			region : 'south',
			frame : true,
			height : 37,
			bodyStyle : 'font-size:18px;text-align:center;',
			html : '总数量小计:<input id="txtHistoryTotalAmount" type="text" disabled="disabled" style="height: 20px;width:90px;font-size :18px;font-weight: bolder;" />' +
				'&nbsp;&nbsp;&nbsp; 总金额:<input id="txtHistoryTotalPrice" type="text" disabled="disabled" style="height: 20px;width:90px;font-size :18px;font-weight: bolder;" />' +
				'&nbsp;&nbsp;&nbsp;<label id="labActualPrice" >实际金额:</label><input id="txtHistoryActualPrice" disabled="disabled" type="text" style=" height: 20px;width:90px;font-size :18px;font-weight: bolder; color:red"/>'
	};

	var cmStockDetail = new Ext.grid.ColumnModel([
		new Ext.grid.RowNumberer(),
		{header: '货品名称', dataIndex: 'material.name', width: 200},
		{header: '数量', dataIndex: 'amount', width: 200},
		{header: '单价', dataIndex: 'price', width: 200},
		{header: '总价', dataIndex: 'totalPrice', width: 200}
	]);

	var dsStockDetail = new Ext.data.Store({
		reader : new Ext.data.JsonReader({totalProperty : 'totalProperty', root : 'root'}, [
		     {name : 'material.name'} ,
		     {name : 'amount'},
		     {name : 'price'},
		     {name : 'totalPrice'}
		])
	});

	var stockDetailGrid = new Ext.grid.GridPanel({
		id : 'stockDetailGrid',
		border : true,
		region : 'center',
		frame : true,
		cm : cmStockDetail,
		store : dsStockDetail
	});

	var stockActionPanel = new Ext.Panel({
		id : 'panelHistoryStockAction',
		layout : 'border',
		width : '100%',
		items : [stockActionHead, stockDetailGrid, stockActionPanelSouth]
		
	});

	var stockActionWin = new Ext.Window({
		title : '货单基础信息',
		id : 'winHistoryStockAction',
		width : 900,
		height : 550,
		modal : true,
		closable : false,
		resizable : false,
	    layout : 'card',
	    activeItem : 0,
	    defaults : {
	        border:false
	    },
	    items : [stockActionPanel],
	    bbar : ['->', {
	    	text : '取消',
	    	iconCls : 'btn_cancel',
	    	handler : function(){
	    		stockActionWin.hide();
	    		dsStockDetail.removeAll();
	    	}
	    }]
	});

	function showDetail(){
		var txtStockIn = Ext.getCmp('displayPanelForHistoryDeptIn');
		var txtStockOut = Ext.getCmp('displayPanelForHistoryDeptOut');
		var txtSupplier = Ext.getCmp('displayPanelForHistorySupplier');
		var titleDom = Ext.getCmp('displayPanelForHistoryStockTitle');
		var sn = Ext.getCmp('historyHistoryStockActionGrid').getSelectionModel().getSelected();
		
		Ext.getCmp('txtDeptInForHistoryStockActionBasic').setValue(sn.data.deptIn.name);
		Ext.getCmp('txtSupplierForHistoryStockActionBasic').setValue(sn.data.supplier.name);
		Ext.getCmp('txtDeptOutForHistoryStockActionBasic').setValue(sn.data.deptOut.name) ;
		Ext.getCmp('txtOriStockIdForHistoryStockActionBasic').setValue(sn.data.oriStockId);
		Ext.getCmp('txtOriStockDateForHistoryStockActionBasic').setValue(sn.data.oriStockDateFormat);
		Ext.getCmp('txtCommentForHistoryStockActionBasic').setValue(sn.data.comment);
		Ext.getCmp('txtApproverNameForHistoryStockActionBasic').setValue(sn.data.approverName);
		if(sn.data.statusValue != 1){
			Ext.getCmp('dateApproverDateForHistoryStockActionBasic').setValue(sn.data.approverDateFormat);
		}else{
			Ext.getCmp('dateApproverDateForHistoryStockActionBasic').setValue();
		}
		Ext.getCmp('txtOperatorNameForHistoryStockActionBasic').setValue(sn.data.operatorName);
		Ext.getCmp('dateOperatorDateForHistoryStockActionBasic').setValue(sn.data.birthDateFormat);

		if(typeof sn.data.stockDetails != 'undefined' && sn.data.stockDetails.length > 0){
			for ( var i = 0; i < sn.data.stockDetails.length; i++) {
				var item = sn.data.stockDetails[i];
				dsStockDetail.add(new StockDetailRecord({
					'material.name' : item['materialName'],
					amount : item['amount'],
					price : item['price'],
					totalPrice : (item['amount'] * item['price']).toFixed(2)
				}));
			}
		}
		
		if(sn.data.subTypeValue == 1){
			txtStockIn.show();
			txtStockOut.hide();
			txtSupplier.show();
		}else if(sn.data.subTypeValue == 2 || sn.data.subTypeValue ==5){
			txtStockIn.show();
			txtStockOut.show();
			txtSupplier.hide();
		}else if(sn.data.subTypeValue == 4){
			txtStockIn.hide();
			txtStockOut.show();
			txtSupplier.show();
		}else if(sn.data.subTypeValue == 3 || sn.data.subTypeValue == 7 || sn.data.subTypeValue == 10){
			txtStockIn.show();
			txtStockOut.hide();
			txtSupplier.hide();
		}else{
			txtStockIn.hide();
			txtStockOut.show();
			txtSupplier.hide();
		}
		stockActionWin.show();
		titleDom.body.update(sn.data.typeText + ' -- ' + sn.data.cateTypeText + sn.data.subTypeText + '单' + '<label style="margin-left:50px">库单编号: ' + sn.data.id + '</label>');
		Ext.getDom('txtHistoryTotalAmount').value = sn.data.amount;
		Ext.getDom('txtHistoryTotalPrice').value = sn.data.price;
		Ext.getDom('txtHistoryActualPrice').value = sn.data.actualPrice;
	}


	var stockActionGrid;
	var sBar;
	var hideTopTBar;
	//var date , maxDate;

	
	Ext.form.Field.prototype.msgTarget = 'side';
	
	function stockOperateRenderer(){
		return '<a href="javascript:void(0);" data-type="historyStockShower">查看</a>&nbsp;&nbsp;&nbsp;';
	}
	
	function stockTypeRenderer(v, m, r, ri, ci, s){
		return r.get('typeText') + ' -- ' + r.get('subTypeText');
	}
	
	function stockInRenderer(v, m, r, ri, ci, s){
		var display = '', t = r.get('typeValue'), st = r.get('subTypeValue');
		if(t == 1){
			if(st == 1 || st == 2 || st == 3 || st == 7){
				display = r.get('deptIn').name;
			}
		}else if(t == 2){
			if(st == 4){
				display = r.get('supplier').name;
			}else if(st == 5){
				display = r.get('deptOut').name;
			}
		}
		return display;
	}
	function displayPrice(v){
		if(v == 0){
			return "----";
		}else{
			return Ext.ux.txtFormat.gridDou(v);
		}
	}
	function stockOutRenderer(v, m, r, ri, ci, s){
		var display = '', t = r.get('typeValue'), st = r.get('subTypeValue');
		if(t == 1){
			if(st == 1){
				display = r.get('supplier').name;
			}else if(st == 2){
				display = r.get('deptOut').name;
			}
		}else if(t == 2){
			display = r.get('deptOut').name;
		}
		return display;
	}
	var cm = new Ext.grid.ColumnModel([
	                                   
	    new Ext.grid.RowNumberer(),
	    {header: '货单编号', dataIndex: 'id'},
	    {header: '货单类型', width:110, renderer: stockTypeRenderer},
	    {header: '货品类型', dataIndex: 'cateTypeText', width:60},
	    {header: '原始单号', dataIndex: 'oriStockId'},
	    {header: '时间', dataIndex: 'oriStockDateFormat', width:100},
	    {header: '出货仓/供应商', renderer: stockOutRenderer, width:100},
	    {header: '收货仓/供应商', dataIndex: 'stockInRenderer', renderer: stockInRenderer, width:100},
	    {header: '数量', dataIndex: 'amount', align: 'right', width:80, renderer: Ext.ux.txtFormat.gridDou},
	    {header: '应收金额', dataIndex: 'price', align: 'right', width:80, renderer: displayPrice},
	    {header: '实际金额', dataIndex: 'actualPrice', width:100, align: 'right', renderer: displayPrice},
	    {header: '审核人', dataIndex: 'approverName', width:80, align: 'center'},
	    {header: '审核状态', dataIndex: 'statusText', align: 'center', width:70},
	    {header: '制单人', dataIndex: 'operatorName', width:80, align: 'center'},
	    {header: '操作', id:'operation', dataIndex: 'stockOperateRenderer' , width : 100, renderer: stockOperateRenderer}
	]);
	
	var ds = new Ext.data.Store({
		
		proxy : new Ext.data.HttpProxy({url: '../../QueryStockAction.do'}),
		reader : new Ext.data.JsonReader({totalProperty : 'totalProperty', root : 'root'}, [
			{name : 'id'},
			{name : 'typeText'},
			{name : 'typeValue'},
			{name : 'subTypeText'},
			{name : 'subTypeValue'},
			{name : 'cateTypeText'},
			{name : 'cateTypeValue'},
			{name : 'oriStockId'},
			{name : 'oriStockDateFormat'},
			{name : 'supplier'},
			{name : 'deptIn'},
			{name : 'deptOut'},
			{name : 'amount'},
			{name : 'price'},
			{name : 'actualPrice'},
			{name : 'approverName'},
			{name : 'approverDateFormat'},
			{name : 'statusValue'},
			{name : 'statusText'},
			{name : 'operatorName'},
			{name : 'birthDateFormat'},
			{name : 'stockDetails'}
		]),
		listeners : {
			'load' : function(store, records, options){
				//this.remove(this.getAt(this.getTotalCount()));
				this.baseParams['isPaging'] = 'true';
				this.baseParams['isHistory'] = 'true';
				
				var sumRow;
				for(var i = 0; i < records.length; i++){
					if(eval(records[i].get('statusValue') != 1)){
						sumRow = stockActionGrid.getView().getRow(i);
						sumRow.style.backgroundColor = '#DDD';
						sumRow = null;
					}
				}
				sumRow = null;
				if(store.getCount() > 0){
					sumRow = stockActionGrid.getView().getRow(store.getCount() - 1);	
					sumRow.style.backgroundColor = '#EEEEEE';			
					for(var i = 0; i < stockActionGrid.getColumnModel().getColumnCount(); i++){
						var sumCell = stockActionGrid.getView().getCell(store.getCount() - 1, i);
						sumCell.style.fontSize = '13px';
						sumCell.style.fontWeight = 'bold';
						sumCell.style.color = 'green';
					}
					stockActionGrid.getView().getCell(store.getCount()-1, 1).innerHTML = '汇总';
					stockActionGrid.getView().getCell(store.getCount()-1, 2).innerHTML = '--';
					stockActionGrid.getView().getCell(store.getCount()-1, 3).innerHTML = '--';
					stockActionGrid.getView().getCell(store.getCount()-1, 4).innerHTML = '--';
					stockActionGrid.getView().getCell(store.getCount()-1, 5).innerHTML = '--';
					stockActionGrid.getView().getCell(store.getCount()-1, 6).innerHTML = '--';
					stockActionGrid.getView().getCell(store.getCount()-1, 7).innerHTML = '--';
					stockActionGrid.getView().getCell(store.getCount()-1, 8).innerHTML = '--';
					//stockActionGrid.getView().getCell(store.getCount()-1, 10).innerHTML = '--';
					stockActionGrid.getView().getCell(store.getCount()-1, 11).innerHTML = '--';
					stockActionGrid.getView().getCell(store.getCount()-1, 12).innerHTML = '--';
					stockActionGrid.getView().getCell(store.getCount()-1, 13).innerHTML = '--';
					stockActionGrid.getView().getCell(store.getCount()-1, 14).innerHTML = '--';
				}
				
			}
		}
		
	});
	
	

	var pagingBar = new Ext.PagingToolbar({
	   id : 'paging',
	   pageSize : PAGE_LIME,
	   store : ds,	
	   displayInfo : true,	
	   displayMsg : "显示第 {0} 条到 {1} 条记录，共 {2} 条",
	   emptyMsg : "没有记录"
	});
	


	
	sBar = new Ext.Toolbar({
		id : 'tbarSecond',
		height : 28,
		hidden : true,
		items : [
		    {xtype : 'tbtext', text : '&nbsp;&nbsp;'},
			{xtype : 'tbtext', text : '查看日期:'},
			{xtype : 'tbtext', text : '&nbsp;&nbsp;'},
			{
				xtype : 'datefield',
				id : 'beginDate',
				allowBlank : false,
				format : 'Y-m-d',
				width : 100
			}, {
				xtype : 'label',
				id : 'to',
				text : ' 至 '
			}, {
				xtype : 'datefield',
				id : 'endDate',
				allowBlank : false,
				format : 'Y-m-d',
				width : 100
			},{
				xtype : 'tbtext',
				text : '&nbsp;&nbsp;'
			},
			{
				xtype : 'tbtext',
				text : '货单类型:'
			}, {
				xtype : 'combo',
				id : 'hsa_comboSearchForStockType',
				readOnly : false,
				forceSelection : true,
				width : 100,
				value : -1,
				store : new Ext.data.SimpleStore({
					data : winParams.st,
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
						var subType = Ext.getCmp('comboSearchForSubType');
						var stockInDate = [[1, '采购'], [2, '入库调拨'], [3, '报溢'], [7, '盘盈']];
						var stockOutDate = [[-1, '全部'], [4, '退货'], [6, '报损'], [8, '盘亏'], [9, '消耗']];//, [5, '出库调拨']
						if(thiz.getValue() == 1){
							subType.store.loadData(stockInDate);
							subType.setValue(-1);
						}else if(thiz.getValue() == 2){
							subType.store.loadData(stockOutDate);
							subType.setValue(-1);
						}else{
							subType.store.loadData('');
							subType.setValue();
						}
						Ext.getCmp('btnHistoryStockSearch').handler();
					}
				}
			}, {
				xtype : 'tbtext',
				text : '&nbsp;&nbsp;业务类型:'
			}, {
				xtype : 'combo',
				id : 'comboSearchForSubType',
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
						Ext.getCmp('btnHistoryStockSearch').handler();
					}
				}
			}, {
				xtype : 'tbtext',
				text : '&nbsp;&nbsp;货品类型:'
			}, {
				xtype : 'combo',
				id : 'comboSearchForCateType',
				readOnly : false,
				forceSelection : true,
				width : 100,
				value : -1,
				store : new Ext.data.SimpleStore({
					data : winParams.cate,
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
					select : function(){
						Ext.getCmp('btnHistoryStockSearch').handler();
					}
				}
			}, {
				xtype : 'tbtext',
				text : '&nbsp;&nbsp;仓库:'
			}, {
				xtype : 'combo',
				id : 'comboSearchForDept',
				fieldLabel : '仓库',
				width : 100,
				readOnly : false,
				forceSelection : true,
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
						Ext.getCmp('btnHistoryStockSearch').handler();
					},
					render : function(thiz){
						var data = [[-1,'全部']];
						Ext.Ajax.request({
							url : '../../OperateDept.do',
							params : {
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
							failure : function(res, opt){
								thiz.store.loadData(data);
								thiz.setValue(-1);
							}
						});
					}
				}
			}, {
				xtype : 'tbtext',
				text : '&nbsp;&nbsp;审核状态:'
			}, {
				xtype : 'combo',
				id : 'comboSearchForStockStatus',
				readOnly : false,
				forceSelection : true,
				width : 100,
				value : -1,
				store : new Ext.data.SimpleStore({
					data : [[-1, '全部'], [2, '审核通过'], [3, ' 冲红']],
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
					select : function(){
						Ext.getCmp('btnHistoryStockSearch').handler();
					}
				}
			}, {
				xtype : 'tbtext',
				text : '&nbsp;&nbsp;供应商:'
			}, {
				xtype : 'combo',
				id : 'comboSearchForSupplier',
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
						Ext.getCmp('btnHistoryStockSearch').handler();
					}
				}
				
			}
		]
	});
	stockActionGrid = new Ext.grid.GridPanel({
		id : 'historyHistoryStockActionGrid',
		height : 200,
		border : true,
		frame : true,
		cm : cm,
		store : ds,
		loadMask : {
			msg : '正在加载数据中...'
		},
		//autoExpandColumn : 'operation',
		viewConfig : {
			forceFit : true
		},
		tbar : new Ext.Toolbar({
			items : [
			         {xtype : 'tbtext', text : '&nbsp;&nbsp;&nbsp;货单编号/原始单号: '},
				     {xtype : 'textfield', id : 'oriStockId', width: 120},
				     '->',
				     {
				    	 text : '搜索',
				    	 id : 'btnHistoryStockSearch',
				    	 iconCls : 'btn_search',
				    	 handler : function(e){
				    		 var beginDate = Ext.getCmp('beginDate');
				    		 var endDate = Ext.getCmp('endDate');
				    		 var st = Ext.getCmp('hsa_comboSearchForStockType');
				    		 var subType = Ext.getCmp('comboSearchForSubType');
				    		 var cate = Ext.getCmp('comboSearchForCateType');
				    		 var dept = Ext.getCmp('comboSearchForDept');
				    		 var status = Ext.getCmp('comboSearchForStockStatus');
				    		 var supplier = Ext.getCmp('comboSearchForSupplier');
				    		 var stockActionId = Ext.getCmp('oriStockId');
				    		 
							 var gs = stockActionGrid.getStore();
							 
				    		 if(Ext.getCmp('tbarSecond').hidden){
				    			 gs.baseParams['oriStockId'] = stockActionId.getValue();
								 gs.baseParams['beginDate'] = '';
								 gs.baseParams['endDate'] = '';
								 gs.baseParams['stockType'] = '';
								 gs.baseParams['subType'] = '';
								 gs.baseParams['cateType'] = '';
								 gs.baseParams['dept'] = '';
								 gs.baseParams['status'] = '';
								 gs.baseParams['supplier'] = '';
				    		 }else{

								 gs.baseParams['stockType'] = st.getValue();
								 gs.baseParams['subType'] = subType.getValue();
								 gs.baseParams['cateType'] = cate.getValue();
								 gs.baseParams['dept'] = dept.getValue();
								 gs.baseParams['status'] = status.getValue() != -1 ? status.getValue() : '';
								 gs.baseParams['supplier'] = supplier.getValue();
								 gs.baseParams['oriStockId'] = stockActionId.getValue();
								 gs.baseParams['beginDate'] = beginDate.getValue() != ''? beginDate.getValue().format('Y-m-d 00:00:00') : '';
								 gs.baseParams['endDate'] = endDate.getValue() != ''?endDate.getValue().format('Y-m-d 23:59:59') : '';
				    		 }
				    		 
							 gs.load({
								params : {
									start : 0,
									limit : PAGE_LIME,
									isHistory : 'true'
								}
							 });
				    		 
				    	 }
				     },'-',
				     {
				    	 text : '高级条件↓',
				    	 id : 'btnHistoryStockHeightSearch',
				    	 handler : function(){

					    		Ext.getCmp('btnHistoryStockHeightSearch').hide();
					    		Ext.getCmp('btnHistoryStockCommonSearch').show();
					    		
					    		Ext.getCmp('oriStockId').setValue();
					    		Ext.getCmp('oriStockId').disable();
					    		Ext.getCmp('tbarSecond').show();

					    		stockActionGrid.setHeight(stockActionGrid.getHeight()-28);
					    		
					    		stockActionGrid.syncSize();//重新计算高度
					    		stockActionPanel.doLayout();//重新布局

				    	 }
				     },{
				    	 text : '高级条件↑',
				    	 id : 'btnHistoryStockCommonSearch',
				    	 hidden : true,
				    	 handler : function(thiz){
				    		Ext.getCmp('oriStockId').enable();
				    		Ext.getCmp('btnHistoryStockHeightSearch').show();
				    		Ext.getCmp('btnHistoryStockCommonSearch').hide();
				    		
				    		Ext.getCmp('hsa_comboSearchForStockType').setValue(-1);
				    		Ext.getCmp('comboSearchForSubType').store.loadData('');
				    		Ext.getCmp('comboSearchForSubType').setValue();
				    		Ext.getCmp('comboSearchForCateType').setValue(-1);
				    		Ext.getCmp('comboSearchForDept').setValue(-1);
				    		Ext.getCmp('comboSearchForStockStatus').setValue(-1);
				    		Ext.getCmp('comboSearchForSupplier').setValue(-1);
				    		
				    		
				    		Ext.getCmp('tbarSecond').hide();
				    		Ext.getCmp('btnHistoryStockSearch').handler();
				    		
				    		stockActionGrid.setHeight(stockActionGrid.getHeight()+28);
				    		stockActionGrid.syncSize();
				    		stockActionGrid.doLayout();

				    	 }
				     }
			]
		}),
		bbar : pagingBar,
		listeners : {
			'render' : function(){
				sBar.render(stockActionGrid.tbar);
			},
			'rowdblclick' : function(){
				showDetail();
			}
		}
		
	});
	
	
	stockActionGrid.region = 'center';
	new Ext.Panel({
		renderTo : 'divHistoryStock',
		width : parseInt(Ext.getDom('divHistoryStock').parentElement.style.width.replace(/px/g,'')),
		height : parseInt(Ext.getDom('divHistoryStock').parentElement.style.height.replace(/px/g,'')),
		layout : 'border',
		items : [stockActionGrid],
		keys : [{
			key : Ext.EventObject.ENTER,
			scope : this,
			fn : function(){
				Ext.getCmp('btnHistoryStockSearch').handler();
			}
		}]
		
	});
	
	//关闭再打开页面时, id重复
	if($("div[id=stockActionPanelSouth]").length > 1){
		
		$("div[id=stockActionPanelSouth]").eq(0).remove();
		
	}
	
	ds.load({
		params:{
			start:0, 
			limit:PAGE_LIME,
			isPaging: 'true',
			isHistory : 'true'
		}
	});
	
	
	//数据读取后操作中的查看
	ds.on('load', function(){
		Ext.query('[data-type=historyStockShower]').forEach(function(item, index){
			item.onclick = function(){
				showDetail();
			}
		});
	});
	
});