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

var materialTypeDate = [['','全部'],[1,'商品'],[2,'原料']];
var materialTypeComb = new Ext.form.ComboBox({
	forceSelection : true,
	width : 90,
	id : 'materialType',
	value : '',
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
        	materialCateStore.load({  
	            params: {  
	            	type : combo.value,  
	            	dataSource : 'normal'
	            }  
            }); 
            
/*            materialComb.allowBlank = true;
        	materialComb.reset();
        	materialStore.load({
        		params: {
        			cateType : combo.value,
        			dataSource : 'normal'
        		}
        	});*/
		}  
	}
	
});

	
var stockIn_deptCombo = new Ext.form.ComboBox({
	id : 'stockIn_deptCombo',
	forceSelection : true,
	width : 90,
	value : -1,
	store : new Ext.data.SimpleStore({
		fields : ['id', 'name']
	}),
	valueField : 'id',
	displayField : 'name',
	typeAhead : true,
	mode : 'local',
	triggerAction : 'all',
	selectOnFocus : true,
	allowBlank : false,
	readOnly : false,
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
			Ext.getCmp('stockIn_btnSearch').handler();
		}
	}
});	
	
var stockIn_combo_staffs = new Ext.form.ComboBox({
	id : 'stockIn_combo_staffs',
	readOnly : false,
	forceSelection : true,
	width : 80,
	listWidth : 120,
	store : new Ext.data.SimpleStore({
		fields : ['staffID', 'staffName']
	}),
	valueField : 'staffID',
	displayField : 'staffName',
	typeAhead : true,
	mode : 'local',
	triggerAction : 'all',
	selectOnFocus : true,
	listeners : {
		render : function(thiz){
			var data = [[-1,'全部']];
			Ext.Ajax.request({
				url : '../../QueryStaff.do',
				params : {privileges : '1003'},
				success : function(res, opt){
					var jr = Ext.decode(res.responseText);
					for(var i = 0; i < jr.root.length; i++){
						data.push([jr.root[i]['staffID'], jr.root[i]['staffName']]);
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
			Ext.getCmp('giftStatistic_btnSearch').handler();
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
	forceSelection : true,
	width : 90,
	id : 'materialCate',
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
/*        	materialComb.allowBlank = true;
        	materialComb.reset();
        	materialStore.load({  
	            params: {  
	            	cateType : materialTypeComb.value,
	            	cateId : combo.value,  
	            	dataSource : 'normal'
	            }  
            });   */  
		}

	}
	
});
var materialStore = new Ext.data.Store({
	//proxy : new Ext.data.MemoryProxy(data),
	proxy : new Ext.data.HttpProxy({url:'../../QueryMaterial.do?restaurantID=' + restaurantID}),
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
	forceSelection : true,
	width : 100,
	listWidth : 250,
	maxheight : 300,
	id : 'materialId',
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
			Ext.getCmp('btnSearch').handler();		
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
var stockOutDate = [[4, '退货'], [6, '报损'], [8, '盘亏'], [9, '消耗']];//, [5, '出库调拨']
var stock = [[-1, '全部'], [1, '入库'], [2, '出库']];
var stockIn_dateCombo;
Ext.onReady(function(){
	var beginDate = new Ext.form.DateField({
		id : 'stockIn_beginDate',
		xtype : 'datefield',		
		format : 'Y-m-d',
		width : 100,
		maxValue : new Date(),
		readOnly : false,
		allowBlank : false,
		listeners : {
			blur : function(thiz){									
				Ext.ux.checkDuft(true, thiz.getId(), endDate.getId());
			}
		}
	});
	var endDate = new Ext.form.DateField({
		id : 'stockIn_endDate',
		xtype : 'datefield',
		format : 'Y-m-d',
		width : 100,
		maxValue : new Date(),
		readOnly : false,
		allowBlank : false,
		listeners : {
			blur : function(thiz){									
				Ext.ux.checkDuft(false, beginDate.getId(), thiz.getId());
			}
		}
	});	
	
	stockIn_dateCombo = Ext.ux.createDateCombo({
		beginDate : beginDate,
		endDate : endDate,
		data : [[7, '本月'], [8, '上月'], [4,'最近三个月']],
		callback : function(){
//			Ext.getCmp('giftStatistic_btnSearch').handler();
		}
	});
	
	//定义列模型
	var cm = new Ext.grid.ColumnModel([
	         new Ext.grid.RowNumberer(),
	         {header:'id', dataIndex:'id', hidden: true },
	         {header:'名称', dataIndex:'name'},
	         {header:'均价', dataIndex:'avgPrice', align : 'right', renderer : renderFormat},
	         {header:'数量', dataIndex:'count', align : 'right', renderer : renderFormat},
	         {header:'金额', dataIndex:'totalMoney', align : 'right', renderer : renderFormat},
	         {header:'参考成本', dataIndex:'referencePrice', align : 'right', renderer : renderFormat},
	         {header:'最高价', dataIndex:'maxPrice', align : 'right', renderer : renderFormat},
	         {header:'最低价', dataIndex:'minPrice',align : 'right', renderer : renderFormat}]);
	
	cm.defaultSortable = true;
	//var data = {root: [{"id":426,"stockInSubType":"","remaining":3,"stockOutAmount":10,"stockInMoney":"1231231","oriStockId":"","stockOutMoney":15,"stockOutSubType":"盘亏","dept":"甜甜蜜蜜","date":"2013-07-02 12:03:45","stockInAmount":""}]};
	//数据加载器
	var ds = new Ext.data.Store({
		//proxy : new Ext.data.MemoryProxy(data),
		proxy : new Ext.data.HttpProxy({url:'../../QueryStockInGeneral.do'}),
		reader : new Ext.data.JsonReader({totalProperty:'totalProperty', root : 'root'}, [
	         {name : 'id'},                                                                         
	         {name : 'name'},
	         {name : 'avgPrice'},
	         {name : 'count'},
	         {name : 'totalMoney'},
	         {name : 'referencePrice'},
	         {name : 'minPrice'},
	         {name : 'maxPrice'}
		])
	});
	
	var detailReportBar = new Ext.Toolbar({
		items : [
		{ xtype:'tbtext', text:'&nbsp;&nbsp;日期:'},
		stockIn_dateCombo, 
		{xtype:'tbtext',text:'&nbsp;'}, 
		beginDate,{
			xtype : 'label',
			id : 'to',
			text : '至'
		}, endDate,{
			xtype : 'tbtext',
			text : '&nbsp;&nbsp;&nbsp;&nbsp;经手人:'
		},stockIn_combo_staffs, {
			xtype : 'tbtext',
			text : '&nbsp;&nbsp;&nbsp;&nbsp;供应商:'
		}, {
			xtype : 'combo',
			id : 'stockIn_comboSearchForSupplier',
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
							
							stockIn_dateCombo.setValue(7);
							stockIn_dateCombo.fireEvent('select', stockIn_dateCombo, {data : {value : 7}}, 7);							
						},
						fialure : function(res, opt){
							thiz.store.loadData(data);
							thiz.setValue(-1);
						}
					});
				},
				select : function(){
					Ext.getCmp('btnSearchForStockBasicMsg').handler();
				}
			}
			
		},
		{xtype:'tbtext', text:'&nbsp;&nbsp;'},
		'->', {
			text : '搜索',
			id : 'btnSearch',
			iconCls : 'btn_search',
			handler : function(){
				//Ext.MessageBox.alert(sn.attributes.deptID);
				var sgs = stockDetailReportGrid.getStore();
				sgs.baseParams['beginDate'] = Ext.getCmp('stockIn_beginDate').getValue().format('Y-m-d');
				sgs.baseParams['endDate'] = Ext.getCmp('stockIn_endDate').getValue().format('Y-m-d');
				sgs.baseParams['name'] = Ext.getCmp('stockIn_Name').getValue();
				sgs.baseParams['operateStaff'] = stockIn_combo_staffs.getValue();
				sgs.baseParams['deptId'] = stockIn_deptCombo.getValue();
				sgs.baseParams['cateType'] = materialTypeComb.getValue();
				sgs.baseParams['cateId'] = materialCateComb.getValue();
				sgs.baseParams['suppler'] = Ext.getCmp('stockIn_comboSearchForSupplier').getValue();
				//load两种加载方式,远程和本地
				sgs.load({
					params : {
						start : 0,
						limit : GRID_PADDING_LIMIT_20
					}
				});
			}
		},{xtype : 'tbtext', text : '&nbsp;&nbsp;'}
		]
	});
	
	var detailReportBar2 = new Ext.Toolbar({
		items : [{
				xtype : 'tbtext',
				text : '&nbsp;&nbsp;部门: '
			}, stockIn_deptCombo ,
			{xtype : 'tbtext', text : '&nbsp;&nbsp;&nbsp;&nbsp;'},
			{xtype : 'tbtext', text : '类型:'},
			materialTypeComb,
			{xtype : 'tbtext', text : '&nbsp;&nbsp;&nbsp;&nbsp;类别:'},
			materialCateComb,{
				xtype : 'tbtext',
				text : '&nbsp;&nbsp;&nbsp;&nbsp;名称:'
			},{
				xtype : 'textfield',
				id : 'stockIn_Name',
				width : 100
			}
		]
	});
	
	var pagingBar = new Ext.PagingToolbar({
		pageSize : GRID_PADDING_LIMIT_20,
		store : ds,
		displayInfo : true,
		displayMsg : '显示第 {0} 条到 {1} 条记录，共 {2} 条',
		emptyMsg : '没有记录'
	});

	stockDetailReportTree = new Ext.tree.TreePanel({
		title : '部门信息',
		id : 'stockDetailDeptTree',
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
				'restaurantID' : restaurantID,
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
			},
			dblclick : function(){
				Ext.getCmp('btnSearch').handler();
				
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
		title : '',
		id : 'stock_in_grid',
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
			render : function(){
				detailReportBar2.render(stockDetailReportGrid.tbar);
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
	//ds.load({params:{start:0, limit:limitCount}});
   new Ext.Panel({
		renderTo : 'divStockInGeneral',
		width : parseInt(Ext.getDom('divStockInGeneral').parentElement.style.width.replace(/px/g,'')),
		height : parseInt(Ext.getDom('divStockInGeneral').parentElement.style.height.replace(/px/g,'')),
		layout : 'border',//布局
		//margins : '5 5 5 5',
		//子集
		items : [stockDetailReportGrid],
		keys : [{
			key : Ext.EventObject.ENTER,
			scope : this,
			fn : function(){
				Ext.getCmp('btnSearch').handler();
			}
		}]
	});
	
		
});