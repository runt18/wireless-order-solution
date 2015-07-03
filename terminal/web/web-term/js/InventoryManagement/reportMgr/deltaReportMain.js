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
	readOnly : false	,
	listeners : {
        select : function(combo, record, index){ 
        	
        	drm_materialCateComb.reset();
        	sDelta_materialComb.allowBlank = true;
        	sDelta_materialComb.reset();
        	sDelta_materialCateStore.load({  
	            params: {  
	            	type : combo.value,  
	            	dataSource : 'normal'
	            }  
            });     
            
        	sDelta_materialStore.load({
        		params: {
        			cateType : combo.value,
        			dataSource : 'normal'
        		}
        	});
		}  
	}
	
});


var sDelta_materialCateStore = new Ext.data.Store({
	//proxy : new Ext.data.MemoryProxy(data),
	proxy : new Ext.data.HttpProxy({url:'../../QueryMaterialCate.do?restaurantID=' + restaurantID}),
	reader : new Ext.data.JsonReader({totalProperty:'totalProperty', root : 'root'}, [
	         {name : 'id'},
	         {name : 'name'}
	])
});
sDelta_materialCateStore.load({  
    params: {  
    	type : materialTypeComb.value,  
    	dataSource : 'normal'
    }
}); 
var drm_materialCateComb = new Ext.form.ComboBox({
	forceSelection : true,
	width : 90,
	id : 'drm_comboMaterialCate',
	store : sDelta_materialCateStore,
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
        	sDelta_materialComb.allowBlank = true;
        	sDelta_materialComb.reset();
        	sDelta_materialStore.load({  
	            params: {  
	            	cateType : materialTypeComb.value,
	            	cateId : combo.value,  
	            	dataSource : 'normal'
	            }  
            });     
		}

	}
	
});



var sDelta_materialStore = new Ext.data.Store({
	//proxy : new Ext.data.MemoryProxy(data),
	proxy : new Ext.data.HttpProxy({url:'../../QueryMaterial.do?restaurantID=' + restaurantID}),
	reader : new Ext.data.JsonReader({totalProperty:'totalProperty', root : 'root'}, [
         {name : 'id'},
         {name : 'name'},
         {name : 'pinyin'}
	])
});
sDelta_materialStore.load({  
    params: { 
    	cateType : materialTypeComb.value,
    	dataSource : 'normal'
    }  
}); 
var sDelta_materialComb = new Ext.form.ComboBox({
	forceSelection : true,
	width : 100,
	listWidth : 250,
	maxheight : 300,
	id : 'drm_comboMaterial',
	store : sDelta_materialStore,
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
var deltaReportDeptTree;
Ext.onReady(function(){
	
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
		drm_materialCateComb,
		{xtype : 'tbtext', text : '货品:'},
		sDelta_materialComb,
		'->', {
			text : '搜索',
			id : 'btnSearch',
			iconCls : 'btn_search',
			handler : function(){
				var sn = deltaReportDeptTree.getSelectionModel().getSelectedNode();
				var sgs = deltaReportGrid.getStore();
				sgs.baseParams['beginDate'] = Ext.getCmp('dr_beginDate').getValue().format('Y-m');
				sgs.baseParams['deptId'] = !sn ? '-1' : sn.attributes.deptID;
				sgs.baseParams['cateType'] = Ext.getCmp('comboMaterialType').getValue();
				sgs.baseParams['cateId'] = Ext.getCmp('drm_comboMaterialCate').getValue();
				sgs.baseParams['materialId'] = Ext.getCmp('drm_comboMaterial').getValue();
				//load两种加载方式,远程和本地
				sgs.load({
					params : {
						start : 0,
						limit : PAGE_LIME
					}
				});
			}
		}, {
			text : '导出',
			iconCls : 'icon_tb_exoprt_excel',
			handler : function(){
				var sn = deltaReportDeptTree.getSelectionModel().getSelectedNode();
				
				var url = "../../{0}?dataSource={1}&beginDate={2}&deptId={3}&materialId={4}&cateId={5}&cateType={6}";
				url = String.format(
					url,
					'ExportHistoryStatisticsToExecl.do',
					'detailReport',
					Ext.getCmp('dr_beginDate').getValue().format('Y-m'),
					!sn ? "-1" : sn.attributes.deptID,
					Ext.getCmp('drm_comboMaterial').getValue(),
					Ext.getCmp('drm_comboMaterialCate').getValue(),
					Ext.getCmp('comboMaterialType').getValue()
				);
				window.location = url;
			}
		}
		]
	});
	
	
	var deltaReportGrid = createGridPanel(
			'',
			'货品列表',
			'',
			'',
			'../../QueryDeltaReport.do',
			[
				[true, false, false, false], 
				['品项名称', 'material.name', 130],
				['初期数量', 'primeAmount',,'right', 'Ext.ux.txtFormat.gridDou'],
				['入库总数', 'stockInTotal',80,'right', 'Ext.ux.txtFormat.gridDou'],
				['出库总数', 'stockOutTotal',80,'right', 'Ext.ux.txtFormat.gridDou'],
				['期末数量', 'endAmount',80,'right', 'Ext.ux.txtFormat.gridDou'],
				['理论消耗', 'expectAmount',80,'right', 'Ext.ux.txtFormat.gridDou'],
				['实际消耗', 'actualAmount',80,'right', 'Ext.ux.txtFormat.gridDou'],
				['差异数', 'deltaAmount',80,'right', 'Ext.ux.txtFormat.gridDou']
			],
			deltaReportRecord.getKeys(),
			[['isPaging', true]],
			GRID_PADDING_LIMIT_20,
			'',
			deltaReportBar
		);		
	deltaReportGrid.region = 'center';
	deltaReportGrid.on('render', function(){
		Ext.getCmp('btnSearch').handler();
	});	
	
	deltaReportDeptTree = new Ext.tree.TreePanel({
		title : '部门信息',
		id : 'deltaReportDeptTree',   
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
	        		var treeRoot = deltaReportDeptTree.getRootNode().childNodes;
	        		if(treeRoot.length > 0){
	        			deptData = [];
	        			for(var i = (treeRoot.length - 1); i >= 0; i--){
	    					if(treeRoot[i].attributes.deptID == 255 || treeRoot[i].attributes.deptID == 253){
	    						deltaReportDeptTree.getRootNode().removeChild(treeRoot[i]);
	    					}
	    				}
	        		}else{
	        			deltaReportDeptTree.getRootNode().getUI().hide();
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
				Ext.getCmp('btnSearch').handler();
			}
		},
		tbar :	[
		     '->',
		     {
				text : '刷新',
				iconCls : 'btn_refresh',
				handler : function(){
					deltaReportDeptTree.getRootNode().reload();
				}
			}
		 ]
	});
	
	new Ext.Panel({
		renderTo : 'divDeltaReport',
		height : parseInt(Ext.getDom('divDeltaReport').parentElement.style.height.replace(/px/g,'')),
		layout : 'border',
		items : [deltaReportDeptTree, deltaReportGrid],
		keys : [{
			key : Ext.EventObject.ENTER,
			scope : this,
			fn : function(){
				Ext.getCmp('btnSearch').handler;
			}
		}]
	});
	
	
});