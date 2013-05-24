function foodMaterialGridRenderer(v){
	return ''
		+ Ext.ux.txtFormat.gridDou(v)
		+ '<a href="javascript:" ><img src="../../images/btnAdd.gif" border="0" title="数量+1"/></a>&nbsp;'
		+ '<a href="javascript:" ><img src="../../images/btnDelete.png" border="0" title="数量-1"/></a>&nbsp;'
		+ '<a href="javascript:" ><img src="../../images/icon_tb_setting.png" border="0" title="数量设置"/></a>&nbsp;'
		+ '<a href="javascript:" ><img src="../../images/btnCancel.png" border="0" title="删除"/></a>'
		+ '';
}
function materialBasicGridRenderer(){
	return ''
		+ '<a href="javascript:" ><img src="../../images/icon_tb_add_to.png" border="0" title="添加菜品原料"/></a>'
		+ '';
}
function initControl(){
	kitchenTreeForSreach = new Ext.tree.TreePanel({
		title : '分厨',
		region : 'west',
		frame : true,
		width : 200,
		border : true,
		rootVisible : true,
		autoScroll : true,
		frame : true,
		bodyStyle : 'backgroundColor:#FFFFFF; border:1px solid #99BBE8;',
		loader : new Ext.tree.TreeLoader({
			dataUrl : '../../QueryKitchen.do',
			baseParams : {
				dataSource : 'tree',
				pin : pin,
				restaurantID : restaurantID
			}
		}),
		root : new Ext.tree.AsyncTreeNode({
			expanded : true,
			text : '全部',
	        leaf : false,
	        border : true,
	        alias : -1
		}),
		tbar : ['->', {
			text : '刷新',
			iconCls : 'btn_refresh',
			handler : function(){
				kitchenTreeForSreach.getRootNode().reload();
			}
		}],
		listeners : {
			dblclick : function(node, e){
				
			}
		}
	});
	
	var foodBasicGridTbar = new Ext.Toolbar({
		height : 26,
		items : ['->', {
			text : '刷新',
			id : 'btnSearchFood',
			iconCls : 'btn_refresh',
			handler : function(){
				var gs = foodBasicGrid.getStore();
				gs.load({
					params : {
						start : 0,
						limit : GRID_PADDING_LIMIT_20
					}
				});
			}
		}]
	});
	foodBasicGrid = createGridPanel(
		'foodBasicGrid',
		'菜品',
		'',
		350,
		'../../QueryMenuMgr.do',
		[
			[true, false, false, true], 
			['菜品编号', 'alias', 60],
			['菜品名称', 'name'],
			['厨房', 'kitchen.name', 60]
		],
		FoodBasicRecord.getKeys(),
		[['isPaging', true], ['pin',pin], ['restaurantId', restaurantID], ['stockStatus', 2]],
		GRID_PADDING_LIMIT_20,
		'',
		foodBasicGridTbar
	);
	foodBasicGrid.getBottomToolbar().displayMsg = '每页&nbsp;'+foodBasicGrid.getBottomToolbar().pageSize+'&nbsp;条';
	foodBasicGrid.region = 'west';
	foodBasicGrid.on('render', function(thiz){
		Ext.getCmp('btnSearchFood').handler();
	});
	foodBasicGrid.on('rowdblclick', function(thiz){
		Ext.getCmp('btnSearchFoodMaterial').handler();
	});
	
	var foodMaterialGridTbar = new Ext.Toolbar({
		height : 26,
		items : ['->', {
			text : '刷新',
			id : 'btnSearchFoodMaterial',
			iconCls : 'btn_refresh',
			handler : function(){
				var data = Ext.ux.getSelData(foodBasicGrid);
				if(!data){
					Ext.example.msg('提示', '请选中一道菜品才搜索');
					return;
				}
				var gs = foodMaterialGrid.getStore();
				gs.load({
					params : {
						start : 0,
						limit : GRID_PADDING_LIMIT_20,
						foodId : data['id']
					}
				});
			}
		}, {
			text : '保存',
			iconCls : 'btn_save',
			handler : function(){
				
			}
		}]
	});
	foodMaterialGrid = createGridPanel(
		'foodMaterialGrid',
		'已配置原料',
		'',
		'',
		'../../QueryFoodMaterial.do',
		[
			[true, false, false, false], 
			['原料名称', 'materialName'],
			['原料类别', 'materialCateName'],
			['数量', 'consumption', ,'right', 'foodMaterialGridRenderer']
		],
		FoodMaterialRecord.getKeys(),
		[['isPaging', true], ['pin',pin], ['restaurantId', restaurantID]],
		GRID_PADDING_LIMIT_20,
		'',
		foodMaterialGridTbar
	);
	foodMaterialGrid.region = 'center';
	
	var materialBasicGridTbar = new Ext.Toolbar({
		height : 26,
		items : ['->', {
			text : '刷新',
			id : 'btnSearchMaterial',
			iconCls : 'btn_refresh',
			handler : function(){
				materialBasicGrid.getStore().reload();
			}
		}]
	});
	materialBasicGrid = createGridPanel(
		'materialBasicGrid',
		'原料信息',
		'',
		350,
		'../../QueryMaterial.do',
		[
			[true, false, false, false], 
			['原料名称', 'name', 60],
			['单位成本', 'price', 60],
			['操作', 'operate', , 'center', 'materialBasicGridRenderer']
		],
		MaterialRecord.getKeys(),
		[['isPaging', true], ['pin',pin], ['restaurantID', restaurantID], ['dataSource', 'normal']],
		GRID_PADDING_LIMIT_20,
		'',
		materialBasicGridTbar
	);
	materialBasicGrid.region = 'east';
	materialBasicGrid.on('render', function(thiz){
		Ext.getCmp('btnSearchMaterial').handler();
	});
}