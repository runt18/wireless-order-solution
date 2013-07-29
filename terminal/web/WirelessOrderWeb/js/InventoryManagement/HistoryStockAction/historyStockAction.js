
var stockActionHead = {

	region : 'north',
	height : 120,
	frame : true,
	items : [{
		id : 'displayPanelForStockTitle',
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
				id : 'hideStockActionId'
			}]
		}, 
		{
			id : 'displayPanelForDeptIn',
			items : [{
				id : 'txtDeptInForStockActionBasic',
				xtype : 'textfield',
				fieldLabel : '收货仓',
				disabled : true
			}]
		}, 
		{
			id : 'displayPanelForSupplier',
			items : [{
				id : 'txtSupplierForStockActionBasic',
				xtype : 'textfield',
				fieldLabel : '供应商',
				disabled : true
			}]
		}, {
			id : 'displayPanelForDeptOut',
			items : [{
				id : 'txtDeptOutForStockActionBasic',
				xtype : 'textfield',
				fieldLabel : '出货仓',
				disabled : true
			}]
		}, {
			items : [{
				id : 'txtOriStockIdForStockActionBasic',
				xtype : 'textfield',
				fieldLabel : '原始单号',
				disabled : true
			}]
		}, {
			items : [{
				id : 'txtOriStockDateForStockActionBasic',
				xtype : 'textfield',
				fieldLabel : '货单日期',
				disabled : true
			}]
		}, {
			columnWidth : 1,
			style : 'width:100%;',
			items : [{
				id : 'txtCommentForStockActionBasic',
				xtype : 'textfield',
				width : 774,
				fieldLabel : '备注',
				disabled : true
			}]
		}, {
			items : [{
				id : 'txtApproverNameForStockActionBasic',
				xtype : 'textfield',
				fieldLabel : '审核人',
				disabled : true
			}]
		}, {
			items : [{
				id : 'dateApproverDateForStockActionBasic',
				xtype : 'textfield',
				fieldLabel : '审核日期',
				disabled : true
			}]
		}, {
			items : [{
				id : 'txtOperatorNameForStockActionBasic',
				xtype : 'textfield',
				fieldLabel : '制单人',
				disabled : true
			}]
		}, {
			items : [{
				id : 'dateOperatorDateForStockActionBasic',
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
		html : '总数量小计:<input id="txtTotalAmount" type="text" disabled="disabled" style="height: 20px;width:90px;font-size :18px;font-weight: bolder;" />' +
			'&nbsp;&nbsp;&nbsp; 总金额:<input id="txtTotalPrice" type="text" disabled="disabled" style="height: 20px;width:90px;font-size :18px;font-weight: bolder;" />' +
			'&nbsp;&nbsp;&nbsp;<label id="labActualPrice" >实际金额:</label><input id="txtActualPrice" disabled="disabled" type="text" style=" height: 20px;width:90px;font-size :18px;font-weight: bolder; color:red"/>'
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
	id : 'panelStockAction',
	layout : 'border',
	width : '100%',
	items : [stockActionHead, stockDetailGrid, stockActionPanelSouth]
	
});

var stockActionWin = new Ext.Window({
	title : '货单基础信息',
	id : 'winStockAction',
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



var pushBackBut = new Ext.ux.ImageButton({
	imgPath : '../../images/UserLogout.png',
	imgWidth : 50,
	imgHeight : 50,
	tooltip : '返回',
	handler : function(btn){
		location.href = 'InventoryProtal.html?restaurantID=' + restaurantID + '&pin=' + pin;
	}
});

var logOutBut = new Ext.ux.ImageButton({
	imgPath : '../../images/ResLogout.png',
	imgWidth : 50,
	imgHeight : 50,
	tooltip : '登出',
	handler : function(btn){
		
	}
});

function showDetail(){
	var txtStockIn = Ext.getCmp('displayPanelForDeptIn');
	var txtStockOut = Ext.getCmp('displayPanelForDeptOut');
	var txtSupplier = Ext.getCmp('displayPanelForSupplier');
	var titleDom = Ext.getCmp('displayPanelForStockTitle');
	var sn = Ext.getCmp('historyStockActionGrid').getSelectionModel().getSelected();
	
	Ext.getCmp('txtDeptInForStockActionBasic').setValue(sn.data.deptIn.name);
	Ext.getCmp('txtSupplierForStockActionBasic').setValue(sn.data.supplier.name);
	Ext.getCmp('txtDeptOutForStockActionBasic').setValue(sn.data.deptOut.name) ;
	Ext.getCmp('txtOriStockIdForStockActionBasic').setValue(sn.data.oriStockId);
	Ext.getCmp('txtOriStockDateForStockActionBasic').setValue(sn.data.oriStockDateFormat);
	Ext.getCmp('txtCommentForStockActionBasic').setValue(sn.data.comment);
	Ext.getCmp('txtApproverNameForStockActionBasic').setValue(sn.data.approverName);
	if(sn.data.statusValue !=1){
		Ext.getCmp('dateApproverDateForStockActionBasic').setValue(sn.data.approverDateFormat);
	}else{
		Ext.getCmp('dateApproverDateForStockActionBasic').setValue();
	}
	Ext.getCmp('txtOperatorNameForStockActionBasic').setValue(sn.data.operatorName);
	Ext.getCmp('dateOperatorDateForStockActionBasic').setValue(sn.data.birthDateFormat);

	if(typeof sn.data.stockDetails != 'undefined' && sn.data.stockDetails.length > 0){
		for ( var i = 0; i < sn.data.stockDetails.length; i++) {
			var item = sn.data.stockDetails[i];
			dsStockDetail.add(new StockDetailRecord({
				'material.name' : item['materialName'],
				amount : item['amount'],
				price : item['price'],
				totalPrice : item['amount'] * item['price']
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
	}else if(sn.data.subTypeValue == 3 || sn.data.subTypeValue == 7){
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
	Ext.getDom('txtTotalAmount').value = sn.data.amount;
	Ext.getDom('txtTotalPrice').value = sn.data.price;
	Ext.getDom('txtActualPrice').value = sn.data.actualPrice;
}


var stockActionGrid;
var sBar;
var hideTopTBar;
Ext.onReady(function(){
	
	Ext.BLANK_IMAGE_URL = '../../extjs/resources/images/default/s.gif';
	Ext.QuickTips.init();
	Ext.form.Field.prototype.msgTarget = 'side';
	
	function stockOperateRenderer(){
		return '<a href="javascript:showDetail()">查看</a>';
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
	    {header: '货单编号', dataIndex: 'id', width: 50},
	    {header: '货单类型', width:110, renderer: stockTypeRenderer},
	    {header: '货品类型', dataIndex: 'cateTypeText', width:75},
	    {header: '原始单号', dataIndex: 'oriStockId', width:90},
	    {header: '时间', dataIndex: 'oriStockDateFormat', width:100},
	    {header: '出货仓/供应商', renderer: stockOutRenderer, width:100},
	    {header: '收货仓/供应商', dataIndex: 'stockInRenderer', renderer: stockInRenderer, width:100},
	    {header: '数量', dataIndex: 'amount', width:90, align: 'center', renderer: Ext.ux.txtFormat.gridDou},
	    {header: '应收金额', dataIndex: 'price', width:100, align: 'center', renderer: Ext.ux.txtFormat.gridDou},
	    {header: '实际金额', dataIndex: 'actualPrice', width:100, align: 'center', renderer: Ext.ux.txtFormat.gridDou},
	    {header: '审核人', dataIndex: 'approverName', width:80, align: 'center'},
	    {header: '审核状态', dataIndex: 'statusText', align: 'center', width:70},
	    {header: '制单人', dataIndex: 'operatorName', width:80, align: 'center'},
	    {header: '操作', align: 'center', width:140, dataIndex: 'stockOperateRenderer', renderer: stockOperateRenderer}
	]);
	
	var ds = new Ext.data.Store({
		
		proxy : new Ext.data.HttpProxy({url: '../../QueryStockAction.do?pin=' + pin}),
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
					sumRow.style.color = 'green';
					for(var i = 0; i < stockActionGrid.getColumnModel().getColumnCount(); i++){
						var sumRow = stockActionGrid.getView().getCell(store.getCount() - 1, i);
						sumRow.style.fontSize = '15px';
						sumRow.style.fontWeight = 'bold';					
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
	   pageSize : 10,
	   store : ds,	
	   displayInfo : true,	
	   displayMsg : "显示第 {0} 条到 {1} 条记录，共 {2} 条",
	   emptyMsg : "没有记录"
	});
	
/*	var date = new Date();
	date.setMonth(date.getMonth()-1);*/
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
				id : 'comboSearchForStockType',
				readOnly : true,
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
						if(thiz.getValue() == 1){
							subType.store.loadData(stockInDate);
							subType.setValue(-1);
						}else{
							subType.store.loadData(stockOutDate);
							subType.setValue(-1);
						}
						Ext.getCmp('btnSearch').handler();
					}
				}
			}, {
				xtype : 'tbtext',
				text : '&nbsp;&nbsp;业务类型:'
			}, {
				xtype : 'combo',
				id : 'comboSearchForSubType',
				readOnly : true,
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
						Ext.getCmp('btnSearch').handler();
					}
				}
			}, {
				xtype : 'tbtext',
				text : '&nbsp;&nbsp;货品类型:'
			}, {
				xtype : 'combo',
				id : 'comboSearchForCateType',
				readOnly : true,
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
						Ext.getCmp('btnSearch').handler();
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
				readOnly : true,
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
						Ext.getCmp('btnSearch').handler();
					},
					render : function(thiz){
						var data = [[-1,'全部']];
						Ext.Ajax.request({
							url : '../../QueryDept.do',
							params : {
								dataSource : 'normal',
								pin : pin
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
				readOnly : true,
				forceSelection : true,
				width : 100,
				value : -1,
				store : new Ext.data.SimpleStore({
					data : [[-1, '全部'], [1, '未审核'], [2, '审核通过'], [3, ' 冲红']],
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
						Ext.getCmp('btnSearch').handler();
					}
				}
			}, {
				xtype : 'tbtext',
				text : '&nbsp;&nbsp;供应商:'
			}, {
				xtype : 'combo',
				id : 'comboSearchForSupplier',
				readOnly : true,
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
							params : {
								pin : pin
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
						Ext.getCmp('btnSearch').handler();
					}
				}
				
			}
		]
	});
	stockActionGrid = new Ext.grid.GridPanel({
		id : 'historyStockActionGrid',
		height : 200,
		border : true,
		frame : true,
		cm : cm,
		store : ds,
		tbar : new Ext.Toolbar({
			items : [
			         {xtype : 'tbtext', text : '&nbsp;&nbsp;&nbsp;货单编号/原始单号: '},
				     {xtype : 'textfield', id : 'oriStockId', width: 120},
				     '->',
				     {
				    	 text : '搜索',
				    	 id : 'btnSearch',
				    	 iconCls : 'btn_search',
				    	 handler : function(e){
				    		 var beginDate = Ext.getCmp('beginDate');
				    		 var endDate = Ext.getCmp('endDate');
				    		 var st = Ext.getCmp('comboSearchForStockType');
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
								 gs.baseParams['beginDate'] = beginDate.getValue().format('Y-m-d');
								 gs.baseParams['endDate'] = endDate.getValue().format('Y-m-d');
				    		 }
				    		 
							 gs.load({
								params : {
									start : 0,
									limit : 10,
									isHistory : 'true'
								}
							 });
				    		 
				    	 }
				     },'-',
				     {
				    	 text : '高级条件↓',
				    	 id : 'btnHeightSearch',
				    	 handler : function(){
				    		Ext.Ajax.request({
				    			url : '../../QuerySystemSetting.do',
				    			params : {
				    				restaurantID : restaurantID
				    			},
				    			success : function(res, opt){
				    				var jr = Ext.decode(res.responseText);
				    				if(jr.success){
					    				var maxDate = new Date(jr.other.systemSetting.setting.stringCurrentMonth);
					    				var date = new Date(jr.other.systemSetting.setting.stringCurrentMonth);
					    				
					    				Ext.getCmp('endDate').setValue(maxDate);
					    				Ext.getCmp('endDate').maxValue = maxDate;
					    				Ext.getCmp('beginDate').maxValue = maxDate;
					    				
					    				date.setMonth(date.getMonth() - 1);

					    				Ext.getCmp('beginDate').setValue(date);
					    				
				    				}else{
				    					Ext.ux.showMsg(jr);
				    				}

				    			},
				    			failure : function(res, opt){
				    				Ext.ux.showMsg(Ext.decode(res.responseText));
				    			}
				    		});
				    		 
				    		Ext.getCmp('btnHeightSearch').hide();
				    		Ext.getCmp('btnCommonSearch').show();
				    		
				    		Ext.getCmp('oriStockId').setValue();
				    		Ext.getCmp('oriStockId').disable();
				    		Ext.getCmp('tbarSecond').show();
				    		stockActionGrid.syncSize();//重新计算高度
				    		stockActionPanel.doLayout();//重新布局

				    	 }
				     },{
				    	 text : '高级条件↑',
				    	 id : 'btnCommonSearch',
				    	 hidden : true,
				    	 handler : function(thiz){
				    		Ext.getCmp('oriStockId').enable();
				    		Ext.getCmp('btnHeightSearch').show();
				    		Ext.getCmp('btnCommonSearch').hide();
				    		Ext.getCmp('tbarSecond').hide();
				    		stockActionGrid.syncSize();
				    		stockActionPanel.doLayout();
				    		
				    		Ext.getCmp('comboSearchForStockType').setValue(-1);
				    		Ext.getCmp('comboSearchForSubType').store.loadData('');
				    		Ext.getCmp('comboSearchForSubType').setValue();
				    		Ext.getCmp('comboSearchForCateType').setValue(-1);
				    		Ext.getCmp('comboSearchForDept').setValue(-1);
				    		Ext.getCmp('comboSearchForStockStatus').setValue(-1);
				    		Ext.getCmp('comboSearchForSupplier').setValue(-1);
				    		
				    		Ext.getCmp('btnSearch').handler();
				    		Ext.getCmp('tbarSecond').hide();
				    		stockActionGrid.syncSize();
				    		stockActionPanel.doLayout();

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
	
	ds.load({
		params:{
			start:0, 
			limit:10,
			isPaging: 'true',
			isHistory : 'true'
		}
	});
	
	
	stockActionGrid.region = 'center';
	var stockActionPanel = new Ext.Panel({
		title : '历史库单',
		region : 'center',
		layout : 'border',
		frame : true,
		items : [stockActionGrid],
		tbar : new Ext.Toolbar({
			height : 55,
			items : [
			      '->',
			      pushBackBut,
			      {xtype : 'tbtext', text : '&nbsp;&nbsp;'},
			      logOutBut
			         ]
		}),
		keys : [{
			key : Ext.EventObject.ENTER,
			scope : this,
			fn : function(){
				Ext.getCmp('btnSearch').handler();
			}
		}]
		
	});

	new Ext.Viewport({
		id : 'viewport',
		layout : 'border',
		items : [{
			region : 'north',
			bodyStyle : 'background-color:#DFE8F6;',
			html : '<h4 style="padding:10px;font-size:150%;float:left;">无线点餐网页终端</h4>',
			height : 50,
			border : false,
			margins : '0 0 0 0'
		},stockActionPanel,
		{
			region : 'south',
			height : 30,
			frame : true,
			html : '<div style="font-size:11pt; text-align:center;"><b>版权所有(c) 2011 智易科技</b></div>'
			
		}]
	});
	
	
	
	
	
	
});