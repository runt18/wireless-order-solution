var deptData = [];
var PAGE_LIME = 18; 
var saleMaterial_materialTypeDate = [[1,'商品'],[2,'原料']];
var saleMaterial_materialTypeComb = new Ext.form.ComboBox({
	forceSelection : true,
	width : 90,
	id : 'saleMaterial_comboMaterialType',
	value : 2,
	store : new Ext.data.SimpleStore({
		fields : [ 'value', 'text' ],
		data : saleMaterial_materialTypeDate
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
        	
        	saleMaterial_materialCateComb.reset();
        	saleMaterial_materialComb.allowBlank = true;
        	saleMaterial_materialComb.reset();
        	saleMaterial_materialCateStore.load({  
	            params: {  
	            	type : combo.value,  
	            	dataSource : 'normal'
	            }  
            });     
            
        	saleMaterial_materialStore.load({
        		params: {
        			cateType : combo.value,
        			dataSource : 'normal'
        		}
        	});
		}  
	}
	
});


var saleMaterial_materialCateStore = new Ext.data.Store({
	//proxy : new Ext.data.MemoryProxy(data),
	proxy : new Ext.data.HttpProxy({url:'../../QueryMaterialCate.do?restaurantID=' + restaurantID}),
	reader : new Ext.data.JsonReader({totalProperty:'totalProperty', root : 'root'}, [
	         {name : 'id'},
	         {name : 'name'}
	])
});
saleMaterial_materialCateStore.load({  
    params: {  
    	type : saleMaterial_materialTypeComb.value,  
    	dataSource : 'normal'
    }
}); 
var saleMaterial_materialCateComb = new Ext.form.ComboBox({
	forceSelection : true,
	width : 90,
	id : 'saleMaterial_comboMaterialCate',
	store : saleMaterial_materialCateStore,
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
        	saleMaterial_materialComb.allowBlank = true;
        	saleMaterial_materialComb.reset();
        	saleMaterial_materialStore.load({  
	            params: {  
	            	cateType : saleMaterial_materialTypeComb.value,
	            	cateId : combo.value,  
	            	dataSource : 'normal'
	            }  
            });     
		}

	}
	
});



var saleMaterial_materialStore = new Ext.data.Store({
	//proxy : new Ext.data.MemoryProxy(data),
	proxy : new Ext.data.HttpProxy({url:'../../QueryMaterial.do?restaurantID=' + restaurantID}),
	reader : new Ext.data.JsonReader({totalProperty:'totalProperty', root : 'root'}, [
         {name : 'id'},
         {name : 'name'},
         {name : 'pinyin'}
	])
});
saleMaterial_materialStore.load({  
    params: { 
    	cateType : saleMaterial_materialTypeComb.value,
    	dataSource : 'normal'
    }  
}); 
var saleMaterial_materialComb = new Ext.form.ComboBox({
	forceSelection : true,
	width : 100,
	listWidth : 250,
	maxheight : 300,
	id : 'saleMaterial_comboMaterial',
	store : saleMaterial_materialStore,
	valueField : 'id',
	displayField : 'name',
	typeAhead : true,
	mode : 'local',
	triggerAction : 'all',
	selectOnFocus : true,
	allowBlank : false,
	blankText: '不能为空', 
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
			Ext.getCmp('saleMaterial_btnSearch').handler();		
		}
		
	}
});
var saleMaterial_DeptTree;
Ext.onReady(function(){
	
	var date = new Date();
	date.setMonth(date.getMonth()-1);
	
	var saleMaterial_Bar = new Ext.Toolbar({
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
			maxValue : new Date(),
			value : new Date(),
            width:100,  
            plugins: 'monthPickerPlugin',  
            format: 'Y-m'
		},
		{xtype : 'tbtext', text : '&nbsp;'},
		{xtype : 'tbtext', text : '类型:'},
		saleMaterial_materialTypeComb,
		{xtype : 'tbtext', text : '类别:'},
		saleMaterial_materialCateComb,
		{xtype : 'tbtext', text : '货品:'},
		saleMaterial_materialComb,
		'->', {
			text : '搜索',
			id : 'saleMaterial_btnSearch',
			iconCls : 'btn_search',
			handler : function(){
				
				if(!saleMaterial_materialComb.isValid()){
					return;
				}
				
				var sn = saleMaterial_DeptTree.getSelectionModel().getSelectedNode();
				var sgs = saleMaterial_Grid.getStore();
				sgs.baseParams['beginDate'] = Ext.getCmp('dr_beginDate').getValue().format('Y-m');
				sgs.baseParams['deptId'] = !sn ? '-1' : sn.attributes.deptID;
				sgs.baseParams['cateType'] = Ext.getCmp('saleMaterial_comboMaterialType').getValue();
				sgs.baseParams['cateId'] = Ext.getCmp('saleMaterial_comboMaterialCate').getValue();
				sgs.baseParams['materialId'] = Ext.getCmp('saleMaterial_comboMaterial').getValue();
				//load两种加载方式,远程和本地
				sgs.load({
					params : {
						start : 0,
						limit : PAGE_LIME
					}
				});
			}
		}
/*		, {
			text : '导出',
			iconCls : 'icon_tb_exoprt_excel',
			handler : function(){
				var sn = saleMaterial_DeptTree.getSelectionModel().getSelectedNode();
				
				var url = "../../{0}?dataSource={1}&beginDate={2}&deptId={3}&materialId={4}&cateId={5}&cateType={6}";
				url = String.format(
					url,
					'ExportHistoryStatisticsToExecl.do',
					'detailReport',
					Ext.getCmp('dr_beginDate').getValue().format('Y-m'),
					!sn ? "-1" : sn.attributes.deptID,
					Ext.getCmp('saleMaterial_comboMaterial').getValue(),
					Ext.getCmp('saleMaterial_comboMaterialCate').getValue(),
					Ext.getCmp('saleMaterial_comboMaterialType').getValue()
				);
				window.location = url;
			}
		}*/
		]
	});
	
	
	var saleMaterial_Grid = createGridPanel(
			'',
			'销售对账列表',
			'',
			'',
			'../../QuerySaleOfMaterial.do',
			[
				[true, false, false, false], 
				['菜品名称', 'foodName', 130],
				['销售数量', 'amount',,'right'],
				['对应比例', 'rate',80,'right', 'Ext.ux.txtFormat.gridDou'],
				['消耗数量', 'consume',80,'right', 'Ext.ux.txtFormat.gridDou']
			],
			saleMaterial_Record.getKeys(),
			[['isPaging', true]],
			GRID_PADDING_LIMIT_20,
			'',
			saleMaterial_Bar
		);		
	saleMaterial_Grid.region = 'center';
	saleMaterial_Grid.on('render', function(){
		//Ext.getCmp('saleMaterial_btnSearch').handler();
	});	
	saleMaterial_Grid.getStore().on('load', function(store, records, options){
		
		if(store.getCount() > 0){
			var sumRow = saleMaterial_Grid.getView().getRow(store.getCount() - 1);	
			sumRow.style.backgroundColor = '#EEEEEE';			
			sumRow.style.color = 'green';
			for(var i = 0; i < saleMaterial_Grid.getColumnModel().getColumnCount(); i++){
				var sumRow = saleMaterial_Grid.getView().getCell(store.getCount() - 1, i);
				sumRow.style.fontSize = '15px';
				sumRow.style.fontWeight = 'bold';					
			}
			saleMaterial_Grid.getView().getCell(store.getCount()-1, 1).innerHTML = '汇总';
			saleMaterial_Grid.getView().getCell(store.getCount()-1, 2).innerHTML = '--';
			saleMaterial_Grid.getView().getCell(store.getCount()-1, 3).innerHTML = '--';
		}
	});	
	
	
	
	saleMaterial_DeptTree = new Ext.tree.TreePanel({
		title : '部门信息',
		id : 'saleMaterial_DeptTree',   
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
				restaurantID : restaurantID,
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
	        		var treeRoot = saleMaterial_DeptTree.getRootNode().childNodes;
	        		if(treeRoot.length > 0){
	        			deptData = [];
	        			for(var i = (treeRoot.length - 1); i >= 0; i--){
	    					if(treeRoot[i].attributes.deptID == 255 || treeRoot[i].attributes.deptID == 253){
	    						saleMaterial_DeptTree.getRootNode().removeChild(treeRoot[i]);
	    					}
	    				}
	        		}else{
	        			saleMaterial_DeptTree.getRootNode().getUI().hide();
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
				Ext.getCmp('saleMaterial_btnSearch').handler();
			}
		},
		tbar :	[
		     '->',
		     {
				text : '刷新',
				iconCls : 'btn_refresh',
				handler : function(){
					saleMaterial_DeptTree.getRootNode().reload();
				}
			}
		 ]
	});
	
	new Ext.Panel({
		renderTo : 'divSaleOfMaterial',
		height : parseInt(Ext.getDom('divSaleOfMaterial').parentElement.style.height.replace(/px/g,'')),
		layout : 'border',
		items : [saleMaterial_DeptTree, saleMaterial_Grid]
/*		keys : [{
			key : Ext.EventObject.ENTER,
			scope : this,
			fn : function(){
				Ext.getCmp('saleMaterial_btnSearch').handler;
			}
		}]*/
	});
	
	
});