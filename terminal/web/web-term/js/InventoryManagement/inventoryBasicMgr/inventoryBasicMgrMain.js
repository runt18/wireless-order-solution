//----------lib.js------
/**
 * 初始化原料类别操作窗口
 */
function initOperateMaterialCateWin(){
	if(!operateMaterialCateWin){
		var cateId = new Ext.form.Hidden({
			id : 'hideMaterialCateId'
		});
		var cateName = new Ext.form.TextField({
			id : 'txtMaterialCateName',
			fieldLabel : '类别名称',
			width : 130,
			allowBlank : false
		});
		operateMaterialCateWin = new Ext.Window({
			id : 'operateMaterialCateWin',
			title : '&nbsp;',
			modal : true,
			resizable : false,
			closable : false,
			width : 250,
			items : [{
				xtype : 'form',
				layout : 'form',
				width : 250,
				labelWidth : 65,
				frame : true,
				items : [cateId, cateName]
			}],
			keys : [{
				key : Ext.EventObject.ESC,
				scope : this,
				fn : function(){
					operateMaterialCateWin.hide();
				}
			}, {
				key : Ext.EventObject.ENTER,
				scope : this,
				fn : function(){
					Ext.getCmp('btnAddMaterialCate').handler();
				}
			}],
			bbar : [{
				text : '应用',
				id : 'btnAddMoreMaterialCate',
				iconCls : 'btn_app',
				handler : function(){
					var dataSource = "";
					if(operateMaterialCateWin.otype == Ext.ux.otype['insert']){
						dataSource = 'insert';
					}else if(operateMaterialCateWin.otype == Ext.ux.otype['update']){
						dataSource = 'update';
					}else{
						return;
					}
					if(!cateName.isValid()){
						return;
					}
					Ext.Ajax.request({
						url : '../../OperateMaterialCate.do',
						params : {
							dataSource : dataSource,
							restaurantID : restaurantID,
							cateId : cateId.getValue(),
							name : cateName.getValue()
						},
						success : function(res, opt){
							var jr = Ext.util.JSON.decode(res.responseText);
							if(jr.success){
								Ext.example.msg(jr.title, jr.msg);
								if(dataSource == 'update'){
									operateMaterialCateWin.hide();
									materialCateTree.getRootNode().reload();
								}else{
									Ext.getCmp('txtMaterialCateName').setValue();
									Ext.getCmp('txtMaterialCateName').focus(true, 100);
								}
							}else{
								Ext.ux.showMsg(jr);
							}
						},
						failure : function(res, opt) {
							Ext.ux.showMsg(Ext.decode(res.responseText));
						}
					});
				}
			},
			'->', {
				text : '保存',
				id : 'btnAddMaterialCate',
				iconCls : 'btn_save',
				handler : function(){
					var dataSource = "";
					if(operateMaterialCateWin.otype == Ext.ux.otype['insert']){
						dataSource = 'insert';
					}else if(operateMaterialCateWin.otype == Ext.ux.otype['update']){
						dataSource = 'update';
					}else{
						return;
					}
					if(!cateName.isValid()){
						return;
					}
					Ext.Ajax.request({
						url : '../../OperateMaterialCate.do',
						params : {
							dataSource : dataSource,
							restaurantID : restaurantID,
							cateId : cateId.getValue(),
							name : cateName.getValue()
						},
						success : function(res, opt){
							var jr = Ext.util.JSON.decode(res.responseText);
							if(jr.success){
								Ext.example.msg(jr.title, jr.msg);
								operateMaterialCateWin.hide();
								materialCateTree.getRootNode().reload();
							}else{
								Ext.ux.showMsg(jr);
							}
						},
						failure : function(res, opt) {
							Ext.ux.showMsg(Ext.decode(res.responseText));
						}
					});
				}
			}, {
				text : '关闭',
				iconCls : 'btn_close',
				handler : function(){
					//Ext.getCmp('btnAddMaterialCate').setText('保存');
					operateMaterialCateWin.hide();
					materialCateTree.getRootNode().reload();
				}
			}],
			listeners : {
				show : function(thiz){
					thiz.center();
				}
			}
		});
	}
}
/**
 * 操作原料类别
 * @param c
 */
function operateMaterialCateHandler(c){
	if(c == null || typeof c == 'undefined' || typeof c.otype == 'undefined'){
		return;
	}
	initOperateMaterialCateWin();
	
	operateMaterialCateWin.otype = c.otype;
	
	var cateId = Ext.getCmp('hideMaterialCateId');
	var cateName = Ext.getCmp('txtMaterialCateName');
	if(c.otype == Ext.ux.otype['insert']){
		operateMaterialCateWin.show();
		cateId.setValue();
		cateName.setValue();
		cateName.clearInvalid();
		cateName.focus(true, 100);
	}else if(c.otype == Ext.ux.otype['update']){
		var sn = materialCateTree.getSelectionModel().getSelectedNode();
		if(!sn || sn.attributes.cateID == -1){
			Ext.example.msg('提示', '请选中一个原料类别再进行操作.');
			return;
		}
		operateMaterialCateWin.show();
		cateId.setValue(sn.attributes.cateId);
		cateName.setValue(sn.attributes.name);
		cateName.focus(true, 100);
	}else if(c.otype == Ext.ux.otype['delete']){
		var sn = materialCateTree.getSelectionModel().getSelectedNode();
		if(!sn || sn.attributes.cateID == -1){
			Ext.example.msg('提示', '请选中一个原料类别再进行操作.');
			return;
		}
		Ext.Msg.show({
			title : '重要',
			msg : '是否删除原料类别?',
			icon: Ext.MessageBox.QUESTION,
			buttons : Ext.Msg.YESNO,
			fn : function(e){
				if(e == 'yes'){
					Ext.Ajax.request({
						url : '../../OperateMaterialCate.do',
						params : {
							dataSource : 'delete',
							restaurantID : restaurantID,
							cateId : sn.attributes.cateId
						},
						success : function(res, opt){
							var jr = Ext.decode(res.responseText);
							if(jr.success){
								materialCateTree.getRootNode().reload();
								Ext.example.msg(jr.title, jr.msg);
								operateMaterialCateWin.hide();
							}else{
								Ext.ux.showMsg(jr);
							}
						},
						failure : function(res, opt) {
							Ext.ux.showMsg(Ext.decode(res.responseText));
						}
					});
				}
			}
		});
	}
}

/**
 * 初始化原料信息操作窗口
 */
function initOperateMaterialWin(){
	if(!operateMaterialWin){
		var materialId = new Ext.form.Hidden({
			id : 'hideMaterialId'
		});
		var materialName = new Ext.form.TextField({
			id : 'txtMaterialName',
			fieldLabel : '原料名称',
			allowBlank : false
		});
		var initMaterialCate = new Ext.form.ComboBox({
			id : 'txtMaterialCate',
			fieldLabel : '所属类别',
		    store : new Ext.data.JsonStore({
		    	root : 'root',
				fields : ['cateId', 'cateName']
			}),
			valueField : 'cateId',
			displayField : 'cateName',
			mode : 'local',
			triggerAction : 'all',
			typeAhead : true,
			selectOnFocus : true,
			forceSelection : true,
			readOnly : false,
			allowBlank : false
		});
		operateMaterialWin = new Ext.Window({
			id : 'operateMaterialWin',
			title : '&nbsp;',
			modal : true,
			resizable : false,
			closable : false,
			width : 250,
			items : [{
				xtype : 'form',
				layout : 'form',
				labelWidth : 65,
				width : 250,
				frame : true,
				defaults : {
					width : 130
				},
				items : [materialId, materialName, initMaterialCate]
			}],
			keys : [{
				key : Ext.EventObject.ESC,
				scope : this,
				fn : function(){
					operateMaterialWin.hide();
				}
			}, {
				key : Ext.EventObject.ENTER,
				scope : this,
				fn : function(){
					Ext.getCmp('btnAddMaterial').handler();
				}
			}],
			bbar : [{
				text : '应用',
				id : 'btnAddMoreMaterial',
				iconCls : 'btn_app',
				handler : function(){
					var dataSource = "";
					if(operateMaterialWin.otype == Ext.ux.otype['insert']){
						dataSource = 'insert';
					}else if(operateMaterialWin.otype == Ext.ux.otype['update']){
						dataSource = 'update';
					}else{
						return;
					}
					if(!materialName.isValid() || !initMaterialCate.isValid()){
						return;
					}
					Ext.Ajax.request({
						url : '../../OperateMaterial.do',
						params : {
							dataSource : dataSource,
							
							restaurantID : restaurantID,
							id : materialId.getValue(),
							name : materialName.getValue(),
							cateId : initMaterialCate.getValue()
						},
						success : function(res, opt){
							var jr = Ext.decode(res.responseText);
							if(jr.success){
								Ext.example.msg(jr.title, jr.msg);
								if(dataSource == 'update'){
									operateMaterialWin.hide();
									Ext.getCmp('btnSearchMaterial').handler();
								}else{
									Ext.getCmp('txtMaterialName').setValue();
									Ext.getCmp('txtMaterialName').focus(true, 100);
								}
								
							}else{
								Ext.ux.showMsg(jr);
							}
						},
						failure : function(res, opt) {
							Ext.ux.showMsg(Ext.decode(res.responseText));
						}
					});
				}
			},'->', {
				text : '保存',
				id : 'btnAddMaterial',
				iconCls : 'btn_save',
				handler : function(){
					var dataSource = "";
					if(operateMaterialWin.otype == Ext.ux.otype['insert']){
						dataSource = 'insert';
					}else if(operateMaterialWin.otype == Ext.ux.otype['update']){
						dataSource = 'update';
					}else{
						return;
					}
					if(!materialName.isValid() || !initMaterialCate.isValid()){
						return;
					}
					Ext.Ajax.request({
						url : '../../OperateMaterial.do',
						params : {
							dataSource : dataSource,
							
							restaurantID : restaurantID,
							id : materialId.getValue(),
							name : materialName.getValue(),
							cateId : initMaterialCate.getValue()
						},
						success : function(res, opt){
							var jr = Ext.decode(res.responseText);
							if(jr.success){
								Ext.example.msg(jr.title, jr.msg);
								operateMaterialWin.hide();
								Ext.getCmp('btnSearchMaterial').handler();
							}else{
								Ext.ux.showMsg(jr);
							}
						},
						failure : function(res, opt) {
							Ext.ux.showMsg(Ext.decode(res.responseText));
						}
					});
				}
			}, {
				text : '关闭',
				iconCls : 'btn_close',
				handler : function(){
					operateMaterialWin.hide();
					Ext.getCmp('btnSearchMaterial').handler();
				}
			}],
			listeners : {
				show : function(thiz){
					thiz.center();
					initMaterialCate.store.loadData(materialCateData);
					
				}
			}
		});
	}
}

/**
 * 操作原料
 * @param c
 */
function operateMaterialHandler(c){
	if(c == null || typeof c == 'undefined' || typeof c.otype == 'undefined'){
		return;
	}
	initOperateMaterialWin();
	
	operateMaterialWin.otype = c.otype;
	
	var materialId = Ext.getCmp('hideMaterialId');
	var materialName = Ext.getCmp('txtMaterialName');
	var materialCate = Ext.getCmp('txtMaterialCate');
	if(c.otype == Ext.ux.otype['insert']){
		operateMaterialWin.show();
		
		materialId.setValue();
		materialName.setValue();
		materialCate.setValue();
		materialName.clearInvalid();
		materialCate.clearInvalid();
		materialCate.setDisabled(false);
		
		var node = materialCateTree.getSelectionModel().getSelectedNode();
		if(node && typeof node.attributes.cateId != 'undefined' && node.attributes.cateId != -1) {
			materialCate.setValue(node.attributes.cateId);
		}
		
		materialName.focus(true, 100);
	}else if(c.otype == Ext.ux.otype['update']){
		var data = Ext.ux.getSelData(inventory_materialBasicGrid);
		if(!data){
			Ext.example.msg('提示', '请选中一条原料信息再进行操作.');
			return;
		}
		operateMaterialWin.show();
		
		materialId.setValue(data['id']);
		materialName.setValue(data['name']);
		materialCate.setValue(data['cateId']);
		
		materialName.focus(true, 100);
	}else if(c.otype == Ext.ux.otype['delete']){
		var data = Ext.ux.getSelData(inventory_materialBasicGrid);
		if(!data){
			Ext.example.msg('提示', '请选中一条原料信息再进行操作.');
			return;
		}
		Ext.Msg.show({
			title : '重要',
			msg : '是否删除原料信息?',
			icon: Ext.MessageBox.QUESTION,
			buttons : Ext.Msg.YESNO,
			fn : function(e){
				if(e == 'yes'){
					Ext.Ajax.request({
						url : '../../OperateMaterial.do',
						params : {
							dataSource : 'delete',
							restaurantID : restaurantID,
							id : data['id']
						},
						success : function(res, opt){
							var jr = Ext.decode(res.responseText);
							if(jr.success){
								Ext.example.msg(jr.title, jr.msg);
								Ext.getCmp('btnSearchMaterial').handler();
							}else{
								Ext.ux.showMsg(jr);
							}
						},
						failure : function(res, opt) {
							Ext.ux.showMsg(Ext.decode(res.responseText));
						}
					});
				}
			}
		});
	}
}
//---------

//------------load
function materialBasicGridOperateRenderer(){
	return ''
		+ '<a href=\"javascript:operateMaterialHandler({otype:Ext.ux.otype[\'update\']})">修改</a>'
		+ '&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;'
		+ '<a href=\"javascript:operateMaterialHandler({otype:Ext.ux.otype[\'delete\']})">删除</a>';
}
function initControl(){
	var materialCateTreeTbae = new Ext.Toolbar({
		height : 26,
		items : ['->', {
			text : '添加',
			iconCls : 'btn_add',
			handler : function(){
				operateMaterialCateHandler({otype:Ext.ux.otype['insert']});
			}
		}, {
			text : '修改',
			iconCls : 'btn_edit',
			handler : function(){
				operateMaterialCateHandler({otype:Ext.ux.otype['update']});
			}
		}, {
			text : '删除',
			iconCls : 'btn_delete',
			handler : function(){
				operateMaterialCateHandler({otype:Ext.ux.otype['delete']});
			}
		}, {
			text : '刷新',
			iconCls : 'btn_refresh',
			handler : function(){
				materialCateTree.getRootNode().reload();
			}
		}]
	});
	materialCateTree = new Ext.tree.TreePanel({
		title : '原料类别信息',
		region : 'west',
		width : 200,
		border : true,
		rootVisible : true,
		frame : true,
		bodyStyle : 'backgroundColor:#FFFFFF; border:1px solid #99BBE8;',
		tbar : materialCateTreeTbae,
		loader : new Ext.tree.TreeLoader({
			dataUrl : '../../QueryMaterialCate.do?',
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
	        cateId : -1,
	        name : '全部',
	        listeners : {
	        	load : function(thiz){
	        		materialCateData.root = [];
	        		for(var i = 0; i < thiz.childNodes.length; i++){
	        			materialCateData.root.push({
	        				cateId : thiz.childNodes[i].attributes.cateId,
	        				cateName : thiz.childNodes[i].attributes.name
	        			});
	        		}
	        		Ext.getCmp('btnSearchMaterial').handler();
	        	}
	        	/*,beforecollapse : function(thiz){
					if(thiz.getSelectionModal().getSelectedNode().attributes.cateId == -1){
						Ext.getCmp('btnSearchMaterial').handler();
						return false;
					}
				}*/
	        }
		}),
		listeners : {
			click : function(e){
				Ext.getDom('displayQueryMaterialCate').innerHTML = e.attributes.name;
			},
			dblclick : function(e){
				Ext.getCmp('btnSearchMaterial').handler();
				if(e.attributes.cateId == -1){
					return false;
				}
			}
		}
	});
	
	var materialBasicGridTbar = new Ext.Toolbar({
		height : 26,
		items : [{
			xtype : 'tbtext',
			text : String.format(Ext.ux.txtFormat.typeName, '类别', 'displayQueryMaterialCate', '----')
		}, {
			xtype : 'tbtext',
			text : '原料名称:'
		}, {
			xtype : 'textfield',
			id : 'txtSearchForMaterialName',
			width : 100
		}, '->', {
			text : '搜索',
			id : 'btnSearchMaterial',
			iconCls : 'btn_search',
			handler : function(){
				var sn = materialCateTree.getSelectionModel().getSelectedNode();
				var name = Ext.getCmp('txtSearchForMaterialName');
				var gs = inventory_materialBasicGrid.getStore();
				gs.baseParams['cateId'] = (sn == null || !sn || sn.attributes.cateId == -1 ? '' : sn.attributes.cateId);
				gs.baseParams['name'] = name.getValue();
				gs.load({
					params : {
						start : 0,
						limit : GRID_PADDING_LIMIT_20
					}
				});
			}
		}, {
			text : '添加',
			iconCls : 'btn_add',
			handler : function(){
				operateMaterialHandler({otype:Ext.ux.otype['insert']});
			}
		}, {
			text : '修改',
			iconCls : 'btn_edit',
			handler : function(){
				operateMaterialHandler({otype:Ext.ux.otype['update']});
			}
		}, {
			text : '删除',
			iconCls : 'btn_delete',
			handler : function(){
				operateMaterialHandler({otype:Ext.ux.otype['delete']});
			}
		}]
	});
	inventory_materialBasicGrid = createGridPanel(
		'inventory_materialBasicGrid',
		'原料信息',
		'',
		'',
		'../../QueryMaterial.do',
		[
			[true, false, false, true], 
			['原料名称', 'name', 150],
			['所属类别', 'cateName'],
			['总数量', 'stock'],
			['单位成本', 'price'],
			['状态', 'statusText'],
			['最后修改人', 'lastModStaff'],
			['最后修改时间', 'lastModDateFormat', 150],
			['操作', 'operate', 150, 'center', 'materialBasicGridOperateRenderer']
		],
		['id', 'name', 'cateId', 'cateName', 'stock', 'price', 'statusValue', 'statusText',
		 'lastModStaff', 'lastModDate', 'lastModDateFormat'],
		[['isPaging', true],  ['restaurantID', restaurantID], ['dataSource', 'normal'], ['cateType', 2]],
		GRID_PADDING_LIMIT_20,
		'',
		materialBasicGridTbar
	);
	inventory_materialBasicGrid.region = 'center';
	inventory_materialBasicGrid.on('rowdblclick', function(){
		operateMaterialHandler({otype:Ext.ux.otype['update']});		
	});
	inventory_materialBasicGrid.keys = [{
		 key : Ext.EventObject.ENTER,
		 fn : function(){ 
			 Ext.getCmp('btnSearchMaterial').handler();
		 },
		 scope : this 
	}];
}
	

//--------------
var btnAddMaterialCate = new Ext.ux.ImageButton({
	imgPath : '../../images/btnAddMaterialCate.png',
	imgWidth : 50,
	imgHeight : 50,
	tooltip : '添加原料类别',
	handler : function(btn){
		operateMaterialCateHandler({otype:Ext.ux.otype['insert']});
	}
});

var btnAddMaterial = new Ext.ux.ImageButton({
	imgPath : '../../images/btnAddMaterial.png',
	imgWidth : 50,
	imgHeight : 50,
	tooltip : '添加原料',
	handler : function(btn){
		operateMaterialHandler({otype:Ext.ux.otype['insert']});
	}
});

Ext.onReady(function(){
	
	initControl();
	
	new Ext.Panel({
		renderTo : 'divMaterial',
		id : 'materialPanel',
		layout : 'border',
		//width : parseInt(Ext.getDom('divMaterial').parentElement.style.width.replace(/px/g,'')),
		height : parseInt(Ext.getDom('divMaterial').parentElement.style.height.replace(/px/g,'')),
		frame : true,
		items : [materialCateTree, inventory_materialBasicGrid]
/*		tbar : new Ext.Toolbar({
			height : 55,
			items : [btnAddMaterialCate, {
			    xtype : 'tbtext',
				text : '&nbsp;&nbsp;'
			}, btnAddMaterial]
		})*/
	});
});