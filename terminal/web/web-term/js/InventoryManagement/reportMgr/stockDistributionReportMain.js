
var materialTypeDate = [[1,'商品'],[2,'原料']];
var materialTypeComb = new Ext.form.ComboBox({
	fidldLabel : '类型:',
	forceSelection : true,
	width : 110,
	id : 'sdir_materialType',
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
		}  
	}
	
});
var materialCateStore = new Ext.data.Store({
	//proxy : new Ext.data.MemoryProxy(data),
	proxy : new Ext.data.HttpProxy({url:'../../QueryMaterialCate.do?restaurantID=' + restaurantID}),
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
var deptTree;
var stockDistributionGrid;
Ext.onReady(function(){
	Ext.form.Field.prototype.msgTarget = 'side';
	
	deptTree = new Ext.tree.TreePanel({
		title : '部门信息',
		id : 'deptTree',   
		region : 'west',
		width : 200,
		border : false,
		rootVisible : true,
		autoScroll : true,
		frame : true,
		bodyStyle : 'backgroundColor:#FFFFFF; border:1px solid #99BBE8;',
		loader : new Ext.tree.TreeLoader({
			dataUrl : '../../QueryDeptTree.do?time=' + new Date(),
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
	        deptID : ' ',
	        listeners : {
	        	load : function(){
	        		var treeRoot = deptTree.getRootNode().childNodes;
	        		if(treeRoot.length > 0){
	        			deptData = [];
	        			for(var i = (treeRoot.length - 1); i >= 0; i--){
	    					if(treeRoot[i].attributes.deptID == 255 || treeRoot[i].attributes.deptID == 253){
	    						deptTree.getRootNode().removeChild(treeRoot[i]);
	    					}
	    				}
	        		}else{
	        			deptTree.getRootNode().getUI().hide();
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
				Ext.getDom('dept').innerHTML = e.text;
			},
			dblclick : function(e){
				Ext.getCmp('btnStockDistributionSearch').handler();
			}
		},
		tbar :	[
		     '->',
		     {
				text : '刷新',
				iconCls : 'btn_refresh',
				handler : function(){
					deptTree.getRootNode().reload();
				}
			}
		 ]
			

	});
	
	
	var stockDetail = new Ext.grid.ColumnModel([
		{header:'部门', dataIndex:'dept.name', width:220},
		{header:'品项名称', dataIndex:'materialName', width:220, hidden:true},
		{header:'数量', dataIndex:'stock', width:220, align: 'right', renderer : Ext.ux.txtFormat.gridDou},
		{header:'成本单价', dataIndex:'price', width:220, align: 'right', renderer : Ext.ux.txtFormat.gridDou},
		{header:'成本金额', dataIndex:'cost', width:220, align: 'right', renderer : Ext.ux.txtFormat.gridDou}
	                                            
	    ]);
	    stockDetail.defaultSortable = true;
	var ds = new Ext.data.GroupingStore({
		//proxy : new Ext.data.MemoryProxy(data),
		proxy : new Ext.data.HttpProxy({url:'../../QueryMaterialDept.do'}),
		reader : new Ext.data.JsonReader({totalProperty:'totalProperty', root : 'root'}, [
			 {name : 'dept.name'},
	         {name : 'materialName'},
	         {name : 'stock'},
	         {name : 'price'},
	         {name : 'cost'}

		]),
		sortInfo:{field: 'materialName', direction: "ASC"},
		groupField:'materialName'
	});
	var date = new Date();
	date.setMonth(date.getMonth()-1);
	var distributionReportBar = new Ext.Toolbar({
		items : [
 		{
			xtype : 'tbtext',
			text : String.format(
				Ext.ux.txtFormat.typeName,
				'部门','dept','全部部门'
			)
		},
		{xtype : 'tbtext', text : '&nbsp;'},
		{xtype : 'tbtext', text : '类型:'},
		materialTypeComb,
		{xtype : 'tbtext', text : '&nbsp;'},
		{xtype : 'tbtext', text : '类别:'},
		materialCateComb,
		{xtype : 'tbtext', text : '&nbsp;'},
		{xtype : 'tbtext', text : '货品:'},
		materialComb,

		'->', {
				text : '展开/收缩',
				iconCls : 'icon_tb_toggleAllGroups',
				handler : function(){
					stockDistributionGrid.getView().toggleAllGroups();
				} 
			}, {
			text : '搜索',
			id : 'btnStockDistributionSearch',
			iconCls : 'btn_search',
			handler : function(){
				var deptID = '';
					var sn = deptTree.getSelectionModel().getSelectedNode();
					
					var stockds = stockDistributionGrid.getStore();
					stockds.baseParams['deptId'] = !sn ? deptID : sn.attributes.deptID;
					stockds.baseParams['cateType'] = Ext.getCmp('sdir_materialType').getValue();
					stockds.baseParams['cateId'] = Ext.getCmp('sdir_materialCate').getValue();
					stockds.baseParams['materialId'] = Ext.getCmp('sdir_materialId').getValue();
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
		   pageSize : 13,	//显示记录条数
		   store : ds,	//定义数据源
		   displayInfo : true,	//是否显示提示信息
		   displayMsg : "显示第 {0} 条到 {1} 条记录，共 {2} 条",
		   emptyMsg : "没有记录"
	});
	stockDistributionGrid = new Ext.grid.GridPanel({
		title : '库存分布明细',
		id : 'grid',
		region : 'center',
		height : '500',
		border : true,
		frame : true,
        animCollapse: false,
		store : ds,
		cm : stockDetail,
		view : new Ext.grid.GroupingView({
            forceFit: true,
            groupTextTpl: '{text} ({[values.rs.length]}条记录)'
        }),
		tbar : distributionReportBar,
		bbar : pagingBar
	});
	ds.load({params:{start:0,limit:13}});
	
	new Ext.Panel({
		renderTo : 'divStockDistribution',
		width : parseInt(Ext.getDom('divStockDistribution').parentElement.style.width.replace(/px/g,'')),
		height : parseInt(Ext.getDom('divStockDistribution').parentElement.style.height.replace(/px/g,'')),
		layout : 'border',//布局
		//margins : '5 5 5 5',
		//子集
		items : [deptTree, stockDistributionGrid]
	});
});
