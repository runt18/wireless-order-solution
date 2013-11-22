var materialTypeDate = [[1,'商品'],[2,'原料']];
var materialTypeComb = new Ext.form.ComboBox({
	forceSelection : true,
	width : 90,
	id : 'comboMaterialType',
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
	readOnly : true	,
	listeners : {
        select : function(combo, record, index){ 
        	
        	drm_materialCateComb.reset();
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
var drm_materialCateComb = new Ext.form.ComboBox({
	forceSelection : true,
	width : 90,
	id : 'drm_comboMaterialCate',
	store : materialCateStore,
	valueField : 'id',
	displayField : 'name',
	typeAhead : true,
	mode : 'local',
	triggerAction : 'all',
	selectOnFocus : true,
	//blankText: '不能为空', 
	readOnly : true,
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
	forceSelection : true,
	width : 100,
	listWidth : 250,
	maxheight : 300,
	id : 'drm_comboMaterial',
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
var deptTree;
Ext.onReady(function(){
	var cm = new Ext.grid.ColumnModel([
	       new Ext.grid.RowNumberer(),
	       {header: '品项名称 ', dataIndex: 'material.name'},
	       {header: '初期数量', dataIndex: 'primeAmount', align : 'right', renderer : Ext.ux.txtFormat.gridDou},
	       {header: '期末数量', dataIndex: 'endAmount', align : 'right', renderer : Ext.ux.txtFormat.gridDou},
	       {header: '理论消耗', dataIndex: 'expectAmount', align : 'right', renderer : Ext.ux.txtFormat.gridDou},
	       {header: '实际消耗', dataIndex: 'actualAmount', align : 'right', renderer : Ext.ux.txtFormat.gridDou},
	       {header: '差异数', dataIndex: 'deltaAmount', align : 'right', renderer : Ext.ux.txtFormat.gridDou}
	]);
	cm.defaultSortable = true;
	
	var ds = new Ext.data.Store({
		proxy : new Ext.data.HttpProxy({url: '../../QueryDeltaReport.do'}),
		reader : new Ext.data.JsonReader({totalProperty: 'totalProperty', root:'root'},[
				{name: 'material.name'},
				{name: 'primeAmount'},
				{name: 'expectAmount'},
				{name: 'actualAmount'},
				{name: 'deltaAmount'},
				{name: 'endAmount'},
				{name: 'primeAmount'}
		])
	});

	var pagingBar = new Ext.PagingToolbar({
		pageSize : 13,
		store : ds,
		displayInfo : true,
		displayMsg : '显示第 {0} 条到 {1} 条记录，共 {2} 条',
		emptyMsg : '没有记录'
	});
	
	var date = new Date();
	date.setMonth(date.getMonth()-1);
	
	var deltaReportBar = new Ext.Toolbar({
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
			id : 'dr_beginDate',
			allowBlank : false,
			format : 'Y-m',
			value : date,
			maxValue : new Date(),
			width : 100
		},
		{xtype : 'tbtext', text : '&nbsp;'},
		{xtype : 'tbtext', text : '类型:'},
		materialTypeComb,
		{xtype : 'tbtext', text : '类别:'},
		drm_materialCateComb,
		{xtype : 'tbtext', text : '货品:'},
		materialComb,
		'->', {
			text : '搜索',
			id : 'btnSearch',
			iconCls : 'btn_search',
			handler : function(){
				var deptID = '-1';
				var sn = deptTree.getSelectionModel().getSelectedNode();
				//Ext.MessageBox.alert(sn.attributes.deptID);
				var sgs = deltaReportGrid.getStore();
				sgs.baseParams['beginDate'] = Ext.getCmp('dr_beginDate').getValue().format('Y-m');
				sgs.baseParams['deptId'] = !sn ? deptID : sn.attributes.deptID;
				sgs.baseParams['cateType'] = Ext.getCmp('comboMaterialType').getValue();
				sgs.baseParams['cateId'] = Ext.getCmp('drm_comboMaterialCate').getValue();
				sgs.baseParams['materialId'] = Ext.getCmp('drm_comboMaterial').getValue();
				//load两种加载方式,远程和本地
				sgs.load({
					params : {
						start : 0,
						limit : 13
					}
				});
			}
		}
		]
	});
	
	
	var deltaReportGrid = new Ext.grid.GridPanel({
		title : '消耗差异表',
		//id : 'deltaReport',
		region : 'center',
		border : true,
		frame : true,
		store : ds,
		cm : cm,
		viewConfig : {
			forceFit : true		
		},
		tbar : deltaReportBar,
		bbar : pagingBar
	});
	
	
	ds.load({
		params:{start:0, limit:13}
	});
	
	deptTree = new Ext.tree.TreePanel({
		title : '部门信息',
		id : 'deptTree',   
		region : 'west',
		width : 170,
		border : false,
		rootVisible : true,
		autoScroll : true,
		frame : true,
		bodyStyle : 'backgroundColor:#FFFFFF; border:1px solid #99BBE8;',
		loader : new Ext.tree.TreeLoader({
			dataUrl : '../../QueryDeptTree.do?time=' + new Date(),
			baseParams : {
				'restaurantID' : restaurantID
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
				Ext.getCmp('btnSearch').handler();
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
	
	new Ext.Panel({
		renderTo : 'divDeltaReport',
		height : parseInt(Ext.getDom('divDeltaReport').parentElement.style.height.replace(/px/g,'')),
		layout : 'border',
		items : [deptTree, deltaReportGrid],
		keys : [{
			key : Ext.EventObject.ENTER,
			scope : this,
			fn : function(){
				Ext.getCmp('btnSearch').handler;
			}
		}]
	});
});