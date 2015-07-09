//-------------lib.js-----
/**
 * 
 */
function addNewFoodMaterial() {
	var materialData = Ext.ux.getSelData(materialBasicGrid);
	foodMaterialControlCenter({
		materialId : materialData['id'],
		materialName : materialData['name'],
		materialCateName : materialData['cateName'],
		count : 1
	});
	
}
/**
 * 
 * @param stroc
 * @param reocrd
 */
function deleteFoodMaterialHandler(stroc, reocrd){
	Ext.Msg.show({
		title : '提示',
		msg : '是否删除该原料',
		buttons : Ext.Msg.YESNO,
		icon : Ext.MessageBox.QUESTION,
		fn : function(e){
			if(e=='yes'){
				stroc.remove(reocrd);
			}
		}
	});
}
/**
 * 
 */
function operateFoodMaterialCount(c){
	var m = Ext.menu.MenuMgr.get('menuFoodMaterialCount');
	if(m){
		m.showAt([c.x, c.y]);
	}
}
/**
 * 
 * @param c
 */
function foodMaterialControlCenter(c) {
	if(c == null || typeof c == 'undefined' || (typeof c.count == 'undefined' && typeof c.otype == 'undefined')){
		Ext.example.msg('提示', '操作失败, 系统参数错误, 请联系客服人员.');
		return;
	}
	var foodData = Ext.ux.getSelData(foodBasicGrid);
	var fmStore = foodMaterialGrid.getStore();
	var data = foodMaterialGrid.getSelectionModel().getSelected();
	
	if (!foodData) {
		Ext.example.msg('提示', '请选中一道菜品再进行操作!');
		return;
	}
	// 删除操作, 优先处理
	if(c.otype == Ext.ux.otype['delete']){
		deleteFoodMaterialHandler(fmStore, data);
		return;
	}
	
	var mid = 0;
	if(typeof c.materialId == 'undefined'){
		mid = data.get('materialId');
	}else{
		mid = c.materialId;
	}
	var hasRecord = false;
	var sindex = 0;
	if (fmStore.getCount() > 0) {
		var temp = null;
		for ( var i = 0; i < fmStore.getCount(); i++) {
			temp = fmStore.getAt(i);
			if (temp.get('materialId') == mid) {
				hasRecord = true;
				if(c.otype == Ext.ux.otype['set']){
					// 直接设置
					temp.set('consumption', c.count);
					temp.commit();
					sindex = i;
				}else{
					if((temp.get('consumption') + c.count) <= 0){
						deleteFoodMaterialHandler(fmStore, temp);
					}else{
						temp.set('consumption', temp.get('consumption') + c.count);
						temp.commit();
						sindex = i;
					}
				}
				break;
			}
		}
	}
	if (!hasRecord) {
		fmStore.add(new FoodMaterialRecord({
			foodId : foodData['id'],
			materialId : mid,
			materialName : c.materialName,
			materialCateName : c.materialCateName,
			consumption : c.count
		}));
		sindex = fmStore.getCount() - 1;
	}
	foodMaterialGrid.getSelectionModel().selectRow(sindex);
}

//--------
//------------------load
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
		width : 180,
		border : true,
		rootVisible : true,
		autoScroll : true,
		frame : true,
		bodyStyle : 'backgroundColor:#FFFFFF; border:1px solid #99BBE8;',
		loader : new Ext.tree.TreeLoader({
			dataUrl : '../../QueryKitchen.do',
			baseParams : {
				dataSource : 'tree',
				
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
				Ext.getCmp('btnSearchFood').handler();
			},
			dblclick : function(node, e){
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
			width : 100,
			listeners : {
				focus : function(thiz){
					thiz.focus(true, 100);
				}
			}
		}, '->', {
			text : '搜索',
			id : 'btnSearchFood',
			iconCls : 'btn_search',
			handler : function(){
				var foodName = Ext.getCmp('txtFoodNameForSearch');
				var node = kitchenTreeForSreach.getSelectionModel().getSelectedNode();
				var gs = foodBasicGrid.getStore();
				gs.baseParams['kitchen'] = node && node.attributes.kid >= 0 ? node.attributes.kid : '';
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
		[['isPaging', true],  ['restaurantId', restaurantID], ['stockStatus', 1]],
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
		Ext.getCmp('btnRefreshFoodMaterial').handler();
		Ext.getCmp('btnSearchFoodMaterial').handler();
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
			id : 'btnRefreshFoodMaterial',
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
		[['restaurantId', restaurantID]],
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
	    	dataSource : 'normal'
	    }  
	}); 

	
	var materialBasicGridTbar = new Ext.Toolbar({
		height : 26,
		items : [{
				xtype : 'tbtext',
				text : '原料名称: '
			},{
				xtype : 'textfield',
				id : 'txtmaterialNameForSearch',
				width : 100,
				listeners : {
					focus : function(thiz){
						thiz.focus(true, 100);
					}
				}
			}, {
				xtype : 'tbtext',
				text : '&nbsp;&nbsp;'
			}, '->',
			{
				text : '搜索',
				id : 'btnSearchFoodMaterial',
				iconCls : 'btn_search',
				handler : function(){
					materialBasicGrid.getStore().load({
						params : {restaurantID : restaurantID, name : Ext.getCmp('txtmaterialNameForSearch').getValue()}
					});
				}
			},{
			text : '刷新',
			id : 'btnRefushMaterial',
			iconCls : 'btn_refresh',
			handler : function(){
				Ext.getCmp('txtmaterialNameForSearch').setValue();
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
		280,
		'../../QueryMaterial.do',
		[
			[true, false, false, false], 
			['原料名称', 'name', 100],
			['单位成本', 'price', 100],
			['操作', 'operate', 80 , 'center', 'materialBasicGridRenderer']
		],
		MaterialRecord.getKeys(),
		[['isPaging', true],  ['restaurantID', restaurantID], ['dataSource', 'normal'], ['cateType', 2]],
		GRID_PADDING_LIMIT_20,
		'',
		materialBasicGridTbar
	);
	materialBasicGrid.region = 'east';
	materialBasicGrid.on('render', function(thiz){
		Ext.getCmp('btnSearchFoodMaterial').handler();
	});
	materialBasicGrid.on('rowdblclick', function(thiz){
		addNewFoodMaterial();
	});
	materialBasicGrid.keys = [{
		key : Ext.EventObject.ENTER,
		fn : function(){
			Ext.getCmp('btnSearchFoodMaterial').handler();
		},
		scope : this
	}];
	
	menuFoodMaterialCount = new Ext.menu.Menu({
		id : 'menuFoodMaterialCount',
		hideOnClick : false,
		items : [new Ext.Panel({
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
		}), {hideOnClick : false}],
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
//--------------------

var btnLoginOut = new Ext.ux.ImageButton({
	imgPath : '../../images/ResLogout.png',
	imgWidth : 50,
	imgHeight : 50,
	tooltip : '登出',
	handler : function(btn){
		
	}
});

Ext.onReady(function(){
	initControl();
	
	new Ext.Panel({
		renderTo : 'divFoodMaterial',
		layout : 'border',
		frame : true,
		width : parseInt(Ext.getDom('divFoodMaterial').parentElement.style.width.replace(/px/g,'')),
		height : parseInt(Ext.getDom('divFoodMaterial').parentElement.style.height.replace(/px/g,'')),
		items : [kitchenTreeForSreach, {
			xtype:'panel',
			region:'center',
			layout : 'border',
			items : [foodBasicGrid, foodMaterialGrid]
		}, materialBasicGrid]
	});
	
});