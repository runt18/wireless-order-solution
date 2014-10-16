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

var materialTypeDate = [[1,'商品'],[2,'原料']];
var materialTypeComb = new Ext.form.ComboBox({
	forceSelection : true,
	width : 90,
	id : 'materialType',
	value : 1,
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
var stockInDate = [[1, '采购'], [2, '入库调拨'], [3, '报溢'], [7, '盘盈']];
var stockOutDate = [[4, '退货'], [5, '出库调拨'], [6, '报损'], [8, '盘亏'], [9, '消耗']];
var stock = [[-1, '全部'], [1, '入库'], [2, '出库']];
Ext.onReady(function(){
	Ext.form.Field.prototype.msgTarget = 'side';
	
	//定义列模型
	var cm = new Ext.grid.ColumnModel([
	         new Ext.grid.RowNumberer(),
	         {header:'id', dataIndex:'id', hidden: true },
	         {header:'日期', dataIndex:'date'},
	         {header:'单号', dataIndex:'oriStockId'},
	         {header:'部门', dataIndex:'dept', width:160},
	         {header:'入库类型', dataIndex:'stockInSubType', width:100},
	         {header:'入库数量', dataIndex:'stockInAmount', align : 'right', renderer : renderFormat},
	         {header:'入库金额', dataIndex:'stockInMoney', align : 'right', renderer : renderFormat},
	         {header:'出库类型', dataIndex:'stockOutSubType', width:100},
	         {header:'出库数量', dataIndex:'stockOutAmount', align : 'right', renderer : renderFormat},
	         {header:'出库金额', dataIndex:'stockOutMoney', align : 'right', renderer : renderFormat},
	         {header:'结存数量', dataIndex:'remaining', align : 'right', renderer : renderFormat}]);
	
	cm.defaultSortable = true;
	//var data = {root: [{"id":426,"stockInSubType":"","remaining":3,"stockOutAmount":10,"stockInMoney":"1231231","oriStockId":"","stockOutMoney":15,"stockOutSubType":"盘亏","dept":"甜甜蜜蜜","date":"2013-07-02 12:03:45","stockInAmount":""}]};
	//数据加载器
	var ds = new Ext.data.Store({
		//proxy : new Ext.data.MemoryProxy(data),
		proxy : new Ext.data.HttpProxy({url:'../../QueryStockDetailReport.do'}),
		reader : new Ext.data.JsonReader({totalProperty:'totalProperty', root : 'root'}, [
	         {name : 'id'},                                                                         
	         {name : 'date'},
	         {name : 'oriStockId'},
	         {name : 'dept'},
	         {name : 'stockInSubType'},
	         {name : 'stockInAmount'},
	         {name : 'stockInMoney'},
	         {name : 'stockOutSubType'},
	         {name : 'stockOutAmount'},
	         {name : 'stockOutMoney'},
	         {name : 'remaining'}
		])
	});
	var date = new Date();
	date.setMonth(date.getMonth()-1);
	var detailReportBar = new Ext.Toolbar({
		items : [
 		{
			xtype : 'tbtext',
			text : String.format(
				Ext.ux.txtFormat.typeName,
				'部门','dept','全部部门'
			)
		},
		{ xtype:'tbtext', text:'日期:'},
		{
			xtype : 'datefield',
			id : 'sdr_beginDate',
			allowBlank : false,
			format : 'Y-m-d',
			value : date,
			maxValue : new Date(),
			width : 100
		}, {
			xtype : 'label',
			id : 'to',
			text : ' 至 '
		}, {
			xtype : 'datefield',
			id : 'sdr_endDate',
			allowBlank : false,
			format : 'Y-m-d',
			value : new Date(),
			maxValue : new Date(),
			width : 100
		},
		{xtype : 'tbtext', text : '&nbsp;'},
		{xtype : 'tbtext', text : '类型:'},
		materialTypeComb,
		{xtype : 'tbtext', text : '类别:'},
		materialCateComb,
		{xtype : 'tbtext', text : '货品:'},
		materialComb,
		{xtype:'tbtext', text:'&nbsp;&nbsp;'},
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
			selectOnFocus : true
		},

		{xtype:'tbtext', text:'&nbsp;&nbsp;'},
		'->', {
			text : '搜索',
			id : 'stockDetail_btnSearch',
			iconCls : 'btn_search',
			handler : function(){
				materialComb.allowBlank = false;
				if(!Ext.getCmp('materialId').isValid()){
					return;
				}
				var deptID = '-1';
				var sn = stockDetailReportTree.getSelectionModel().getSelectedNode();
				//Ext.MessageBox.alert(sn.attributes.deptID);
				var sgs = stockDetailReportGrid.getStore();
				sgs.baseParams['beginDate'] = Ext.getCmp('sdr_beginDate').getValue().format('Y-m-d');
				sgs.baseParams['endDate'] = Ext.getCmp('sdr_endDate').getValue().format('Y-m-d');
				sgs.baseParams['deptId'] = !sn ? deptID : sn.attributes.deptID;
				sgs.baseParams['materialId'] = Ext.getCmp('materialId').getValue();
				sgs.baseParams['stockType'] = Ext.getCmp('sdr_comboSearchForStockType').getValue();
				sgs.baseParams['subType'] = Ext.getCmp('sdr_comboSearchForSubType').getValue();
				//load两种加载方式,远程和本地
				sgs.load({
					params : {
						start : 0,
						limit : limitCount
					}
				});
			}
		},{xtype : 'tbtext', text : '&nbsp;&nbsp;'}
		]
	});
	
	var pagingBar = new Ext.PagingToolbar({
		pageSize : limitCount,
		store : ds,
		displayInfo : true,
		displayMsg : '显示第 {0} 条到 {1} 条记录，共 {2} 条',
		emptyMsg : '没有记录'
	});

	stockDetailReportTree = new Ext.tree.TreePanel({
		title : '部门信息',
		id : 'deptTree',
		region : 'west',
		width : 160,
		border : false,
		rootVisible : true,
		autoScroll : true,
		frame : true,
		bodyStyle : 'backgroundColor:#FFFFFF; border:1px solid #99BBE8;',
		loader : new Ext.tree.TreeLoader({
			dataUrl : '../../QueryDeptTree.do?time='+new Date(),
			baseParams : {
				warehouse : true
			}
		}),
		root : new Ext.tree.AsyncTreeNode({
			expanded : true,
			text : '全部部门',
	        leaf : false,
	        border : true,
	        deptID : '-1',
	        listeners : {
	        	load : function(){
	        		var treeRoot = stockDetailReportTree.getRootNode().childNodes;
	        		if(treeRoot.length > 0){
	        			deptData = [];
	        			for(var i = (treeRoot.length - 1); i >= 0; i--){
	    					if(treeRoot[i].attributes.deptID == 255 || treeRoot[i].attributes.deptID == 253){
	    						stockDetailReportTree.getRootNode().removeChild(treeRoot[i]);
	    					}
	    				}
	        			for(var i = 0; i < treeRoot.length; i++){
	        				var tp = {};
	        				tp.type = treeRoot[i].attributes.type;
	        				tp.deptID = treeRoot[i].attributes.deptID;
	        				tp.deptName = treeRoot[i].text;
	        				deptData.push(tp);
	        			}
	        			
	        		}else{
	        			stockDetailReportTree.getRootNode().getUI().hide();
	        			Ext.Msg.show({
	        				title : '提示',
	        				msg : '加载部门信息失败.',
	        				buttons : Ext.MessageBox.OK
	        			});
	        		}
	        	}
	        }
		}),
		listeners : {
			click : function(e){
				//var node = this.getSelectionModel().getSelectedNode();
				Ext.getDom('dept').innerHTML = e.text;
				Ext.getCmp('stockDetail_btnSearch').handler();
			}
		},
		tbar :	[
		     '->',
		     {
					text : '刷新',
					iconCls : 'btn_refresh',
					handler : function(){
						stockDetailReportTree.getRootNode().reload();
					}
			}
		      	 ]
			

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
		tbar : detailReportBar,
		bbar : pagingBar,
		listeners : {
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
	//ds.load({params:{start:0, limit:limitCount}});
   new Ext.Panel({
		renderTo : 'divStockDetail',
		width : parseInt(Ext.getDom('divStockDetail').parentElement.style.width.replace(/px/g,'')),
		height : parseInt(Ext.getDom('divStockDetail').parentElement.style.height.replace(/px/g,'')),
		layout : 'border',//布局
		//margins : '5 5 5 5',
		//子集
		items : [stockDetailReportTree,stockDetailReportGrid],
		keys : [{
			key : Ext.EventObject.ENTER,
			scope : this,
			fn : function(){
				Ext.getCmp('stockDetail_btnSearch').handler();
			}
		}]
	});
});