function foodMaterialGridRenderer(v){
	return ''
		+ Ext.ux.txtFormat.gridDou(v)
		+ '<a href="javascript:foodMaterialControlCenter({count:1});" ><img src="../../images/btnAdd.gif" border="0" title="数量+1"/></a>&nbsp;'
		+ '<a href="javascript:foodMaterialControlCenter({count:-1});" ><img src="../../images/btnDelete.png" border="0" title="数量-1"/></a>&nbsp;'
		+ '<a onClick="operateFoodMaterialCount({x:event.clientX,y:event.clientY});" href="javascript:"><img src="../../images/icon_tb_setting.png" border="0" title="数量设置"/></a>&nbsp;'
		+ '<a href="javascript:foodMaterialControlCenter({otype:Ext.ux.otype[\'delete\']});" ><img src="../../images/btnCancel.png" border="0" title="删除"/></a>'
		+ '';
}
function materialBasicGridRenderer(){
	return ''
		+ '<a href="javascript:addNewFoodMaterial()" ><img src="../../images/icon_tb_add_to.png" border="0" title="添加菜品原料"/></a>'
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
				Ext.getDom('showTypeForSearchKitchen').innerHTML = '----';
				kitchenTreeForSreach.getRootNode().reload();
//				Ext.getCmp('txtFoodNameForSearch').setValue();
//				Ext.getCmp('btnSearchFood').handler();
			}
		}],
		listeners : {
			click : function(e){
				Ext.getDom('showTypeForSearchKitchen').innerHTML = e.text;
			},
			dblclick : function(node, e){
				Ext.getCmp('btnSearchFood').handler();
			}
		}
	});
	
	var foodBasicGridTbar = new Ext.Toolbar({
		height : 26,
		items : [{
			xtype : 'tbtext',
			text : String.format(Ext.ux.txtFormat.typeName, '分厨', 'showTypeForSearchKitchen', '----')
		}, {
			xtype : 'tbtext',
			text : '菜名:'
		}, {
			xtype : 'textfield',
			id : 'txtFoodNameForSearch',
			width : 100
		}, '->', {
			text : '搜索',
			id : 'btnSearchFood',
			iconCls : 'btn_search',
			handler : function(){
				var foodName = Ext.getCmp('txtFoodNameForSearch');
				var node = kitchenTreeForSreach.getSelectionModel().getSelectedNode();
				var gs = foodBasicGrid.getStore();
				gs.baseParams['kitchen'] = node && node.attributes.alias >= 0 ? node.attributes.alias : '';
				gs.baseParams['name'] = foodName.getValue();
				gs.load({
					params : {
						start : 0,
						limit : GRID_PADDING_LIMIT_20
					}
				});
				foodMaterialGrid.getStore().removeAll();
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
		[['isPaging', true], ['pin',pin], ['restaurantId', restaurantID], ['stockStatus', 3]],
		GRID_PADDING_LIMIT_20,
		'',
		foodBasicGridTbar
	);
	foodBasicGrid.getBottomToolbar().displayMsg = '每页&nbsp;'+foodBasicGrid.getBottomToolbar().pageSize+'&nbsp;条';
	foodBasicGrid.region = 'west';
	foodBasicGrid.on('render', function(thiz){
		Ext.getCmp('btnSearchFood').handler();
	});
	foodBasicGrid.on('rowclick', function(thiz){
		Ext.getCmp('btnSearchFoodMaterial').handler();
		Ext.getCmp('btnSearchMaterial').handler();
	});
	foodBasicGrid.keys = [{
		key : Ext.EventObject.ENTER,
		scope : this,
		fn : function(){
			Ext.getCmp('btnSearchFood').handler();
		}
	}];
	
	var foodMaterialGridTbar = new Ext.Toolbar({
		height : 26,
		items : ['->', {
			text : '刷新',
			id : 'btnSearchFoodMaterial',
			iconCls : 'btn_refresh',
			handler : function(){
				var data = Ext.ux.getSelData(foodBasicGrid);
				if(!data){
					Ext.example.msg('提示', '请选中一道菜品再搜索');
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
			id : 'btnSaveSettingForFoodMaterial',
			iconCls : 'btn_save',
			handler : function(){
				var food = Ext.ux.getSelData(foodBasicGrid);
				var content = '';
				
				var gs = foodMaterialGrid.getStore();
				var temp = null;
				for(var i = 0; i < gs.getCount(); i++){
					temp = gs.getAt(i);
					if(i > 0)
						content += '<sp>';
					content += (temp.get('materialId') + ',' + temp.get('consumption'));
					temp = null;
				}
				Ext.Ajax.request({
					url : '../../OperateFoodMaterial.do',
					params : {
						dataSource : 'update',
						pin : pin,
						restaurantId : restaurantID,
						foodId : food['id'],
						content : content
					},
					success : function(res, opt){
						var jr = Ext.decode(res.responseText);
						if(jr.success){
							Ext.example.msg(jr.title, jr.msg);
							Ext.getCmp('btnSearchFoodMaterial').handler();
						}else{
							Ext.ux.showMsg(jr);
						}
					},
					failure : function(res, opt){
						Ext.ux.showMsg(Ext.decode(res.responseText));
					}
				});
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
	    	cateType : '2',
	    	dataSource : 'onlyMaterial'
	    }  
	}); 
	var materialComb = new Ext.form.ComboBox({
		forceSelection : true,
		width : 110,
		listWidth : 250,
		maxheight : 250,
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
				materialBasicGrid.getStore().load({
					params : {restaurantID : restaurantID, materialId : materialComb.getValue()}
				});
			}
			
		}
	});
	
	var materialBasicGridTbar = new Ext.Toolbar({
		height : 26,
		items : ['->', {
				text : '原料名称: '
			}, materialComb, 
			{
				text : '&nbsp;&nbsp;'
			}, {
			text : '刷新',
			id : 'btnSearchMaterial',
			iconCls : 'btn_refresh',
			handler : function(){
				materialComb.setValue();
				materialBasicGrid.getStore().load({
					params : {restaurantID : restaurantID}
				});
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
	materialBasicGrid.on('rowdblclick', function(thiz){
		addNewFoodMaterial();
	});
	
	menuFoodMaterialCount = new Ext.menu.Menu({
		id : 'menuFoodMaterialCount',
		hideOnClick : false,
		items : [new Ext.menu.Adapter(new Ext.Panel({
			frame : true,
			width : 150,
			items : [{
				xtype : 'form',
				layout : 'form',
				frame : true,
				labelWidth : 30,
				items : [{
					xtype : 'numberfield',
					id : 'numbFoodMaterialCount',
					fieldLabel : '数量',
					width : 80,
					validator : function(v){
						if(v >= 0.01 && v <= 255){
							return true;
						}else{
							return '菜品数量在 1 ~ 255 之间.';
						}
					} 
				}]
			}],
			bbar : ['->', {
				text : '确定',
				id : 'btnSaveFoodMaterialCount',
				iconCls : 'btn_save',
				handler : function(e){
					var count = Ext.getCmp('numbFoodMaterialCount');
					if(!count.isValid()){
						return;
					}
					Ext.getCmp('btnCancelFoodMaterialCount').handler();
					foodMaterialControlCenter({
						otype : Ext.ux.otype['set'],
						count : count.getValue()
					});
				}
			}, {
				text : '关闭',
				id : 'btnCancelFoodMaterialCount',
				iconCls : 'btn_close',
				handler : function(e){
					Ext.menu.MenuMgr.get('menuFoodMaterialCount').hide();
				}
			}],
			keys : [{
				key : Ext.EventObject.ENTER,
				scope : this,
				fn : function(){
					Ext.getCmp('btnSaveFoodMaterialCount').handler();
				}
			}]
		}), {hideOnClick : false})],
		listeners : {
			show : function(){
				var data = Ext.ux.getSelData(foodMaterialGrid);
				var count = Ext.getCmp('numbFoodMaterialCount');
				count.setValue(data['consumption']);
				count.clearInvalid();
				count.focus.defer(100, count);
			}
		}
	});
	menuFoodMaterialCount.render(document.body);
}