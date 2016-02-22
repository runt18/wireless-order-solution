var init_materialBasicGrid, init_materialCateTree, editData=new Map(),  isInit = false;

var init_uploadMask = new Ext.LoadMask(document.body, {
	msg : '正在保存...'
});

function initStock(){
	Ext.Msg.show({
		title : '是否初始化库存?',
		msg : '初始化后将清空所有库存库单和盘点单等信息',
		icon: Ext.MessageBox.QUESTION,
		buttons : Ext.Msg.YESNO,
		fn : function(e){
			if(e == 'yes'){
				Ext.Ajax.request({
					url : '../../OperateMaterialInit.do',
					params : {
						dataSource : 'init'
					},
					success : function(res, opt){
						var jr = Ext.decode(res.responseText);
						if(jr.success){
							isInit = true;
							Ext.example.msg(jr.title, jr.msg);
							Ext.getCmp('init_txtSearchForMaterialName').setValue();
							Ext.getCmp('btnSearchInitMaterial').handler();
						}else{
							Ext.ux.showMsg({success:false, msg:'初始化失败,请联系客服'});
						}
						
					},
					failure : function(res, opt) {
						Ext.ux.showMsg({success:false, msg:'操作失败, 请刷新页面后再试'});
					}
				});
			}
		}
	});		
}

function initMaterialControl(){
	var init_deptCombo = new Ext.form.ComboBox({
		id : 'init_deptCombo',
		forceSelection : true,
		width : 90,
		value : 252,
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
				var data = [];
				Ext.Ajax.request({
					url : '../../OperateDept.do',
					params : {
						dataSource : 'getByCond',
						inventory : true
					},
					success : function(res, opt){
						var jr = Ext.decode(res.responseText);
						for(var i = 0; i < jr.root.length; i++){
							data.push([jr.root[i]['id'], jr.root[i]['name']]);
						}
						thiz.store.loadData(data);
						thiz.setValue(252);
					},
					fialure : function(res, opt){
						thiz.store.loadData(data);
					}
				});				
			},
			select : function(){
				Ext.getCmp('btnSearchInitMaterial').handler();
			}
		}
	});	
	
	
	var init_materialCateTreeTbae = new Ext.Toolbar({
		height : 26,
		items : ['->',{
			text : '刷新',
			iconCls : 'btn_refresh',
			handler : function(){
				init_materialCateTree.getRootNode().reload();
			}
		}]
	});
	init_materialCateTree = new Ext.tree.TreePanel({
		id : 'init_materialCateTree',
		title : '类别信息',
		region : 'west',
		width : 200,
		border : true,
		rootVisible : false,
		frame : true,
		bodyStyle : 'backgroundColor:#FFFFFF; border:1px solid #99BBE8;',
		tbar : init_materialCateTreeTbae,
		loader : new Ext.tree.TreeLoader({
			dataUrl : '../../QueryMaterialCate.do',
			baseParams : {
				dataSource : 'tree'
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
	        	load : function(thiz, records){
	        		materialCateData = [];
	        		materialGoodCateData = [];
	        		Ext.Ajax.request({
	        			url : '../../QueryMaterialCate.do',
	        			params : {dataSource : 'normal'},
						success : function(res, opt){
							var jr = Ext.decode(res.responseText);
							for (var i = 0; i < jr.root.length; i++) {
								if(jr.root[i].typeValue == 2){
									materialCateData.push([jr.root[i].id, jr.root[i].name]);
								}else{
									materialGoodCateData.push([jr.root[i].id, jr.root[i].name]);
								}
								
							}
						},
						failure : function(res, opt){
							Ext.ux.showMsg(Ext.decode(res.responseText));
						}
	        		});
	        		//Ext.getCmp('btnSearchInitMaterial').handler();
	        	}
	        }
		}),
		listeners : {
			click : function(e){
				Ext.getDom('displayQueryMaterialCate').innerHTML = e.text;
				
				Ext.getCmp('btnSearchInitMaterial').handler();
			}
		}
	});
	
	var materialBasicGridTbar = new Ext.Toolbar({
		height : 26,
		items : [{
			xtype : 'tbtext',
			text : String.format(Ext.ux.txtFormat.typeName, '类别', 'displayQueryMaterialCate', '----')
		},{
			xtype : 'tbtext',
			text : '&nbsp;&nbsp;部门: '
		},init_deptCombo, {
			xtype : 'tbtext',
			text : '&nbsp;&nbsp;&nbsp;&nbsp;物品名称:'
		}, {
			id : 'init_txtSearchForMaterialName',
			xtype : 'textfield',
			width : 100
		}, '->', {
			text : '搜索',
			id : 'btnSearchInitMaterial',
			iconCls : 'btn_search',
			handler : function(){
				if(isInit){
					var sn = init_materialCateTree.getSelectionModel().getSelectedNode();
					var name = Ext.getCmp('init_txtSearchForMaterialName');
					var deptId = Ext.getCmp('init_deptCombo');
					var gs = init_materialBasicGrid.getStore();
					gs.baseParams['cateType'] = (sn == null || !sn ? '' : sn.attributes.type);
					gs.baseParams['cateId'] = (sn == null || !sn ? '' : sn.attributes.cateId);
					gs.baseParams['deptId'] = deptId.getValue();
					gs.baseParams['name'] = name.getValue();
					gs.load();				
				}

			}
		}, {
			text : '保存设置',
			id : 'btnSaveInitMaterial',
			iconCls : 'btn_save',
			handler : function(){
				if(editData.size() == 0){
					return;
				}
				$.post('../../OperateMaterialInit.do', {dataSource:'isInit'}, function(jr){
					isInit = jr.success;
					if(jr.success){
						init_uploadMask.show();
						var deptId = Ext.getCmp('init_deptCombo');
						
						Ext.Ajax.request({
							url : '../../OperateMaterialInit.do',
							params : {
								dataSource : 'updateDeptStock',
								deptId : deptId.getValue(),
								editData : editData.values().join("<li>"),
								cateType : init_materialBasicGrid.cateType ? (init_materialBasicGrid.cateType == true ? 1 : 2) : 2
							},
							success : function(res, opt){
								init_uploadMask.hide();
								var jr = Ext.decode(res.responseText);
								if(jr.success){
									Ext.example.msg(jr.title, jr.msg);
									init_materialBasicGrid.getStore().commitChanges();
									editData.clear() ;
								}else{
									Ext.ux.showMsg(jr);
								}						
							},
							failure : function(res, opt){
								Ext.ux.showMsg(Ext.decode(res.responseText));
							}
						});
					}else{
						Ext.example.msg('提示', '未初始化库存, 不能修改库存数量');
					}
				});	
			}
		},{
			xtype : 'tbtext',
			text : '&nbsp;&nbsp;&nbsp;&nbsp;'
		},'-',{
			xtype : 'tbtext',
			text : '&nbsp;&nbsp;&nbsp;&nbsp;'
		}, {
			text : '初始化库存',
			id : 'btnInitMaterial',
			iconCls : 'btn_delete',
			handler : function(){
				initStock();			
			}
		},{
			xtype : 'tbtext',
			text : '&nbsp;&nbsp;'
		}]
	});
	
	var cm = new Ext.grid.ColumnModel([
	       new Ext.grid.RowNumberer(),
	       {header: '物品名称 ', dataIndex: 'name'},
	       {header: '库存', dataIndex: 'stock', align: 'right', editor: new Ext.form.NumberField({
	    	   allowBlank: false,
	    	   listeners : {
	    		   focus : function(thiz){
	    			   thiz.focus(true, 100);
	    		   }
	    	   }
	       }), renderer: Ext.ux.txtFormat.gridDou},
	       {header: '单位成本', dataIndex: 'price', align: 'right', renderer: Ext.ux.txtFormat.gridDou}
	]);
	cm.defaultSortable = true;
	
	var ds = new Ext.data.Store({
		proxy : new Ext.data.HttpProxy({url: '../../OperateMaterialInit.do'}),
		reader : new Ext.data.JsonReader({totalProperty: 'totalProperty', root:'root'},[
				{name: 'id'},
				{name: 'name'},
				{name: 'stock'},
				{name: 'price'}
		]),
		baseParams : {
			dataSource : 'getInitMaterial'
		}
	});
	
	init_materialBasicGrid = new Ext.grid.EditorGridPanel({
		title : '货品列表 -- <font color="green" size=4>单击库存列即可修改库存数量</font>',
		id : 'init_materialBasicGrid',
		region : 'center',
		store : ds,
		cm : cm,
		clicksToEdit: 1,
		autoSizeColumns: true,
		viewConfig : {
			forceFit : true		
		},
//		selModel : new Ext.grid.RowSelectionModel(),
		tbar : materialBasicGridTbar,
		listeners : {
			beforeedit : function(e){
				if(!isInit){
					return false;	
				}
			},
			afteredit : function(e){
				if(editData.containsKey(e.record.data['id'])){
					editData.remove(e.record.data['id']);
				} 
				editData.put(e.record.data['id'], e.record.data['id'] + ',' + e.record.data['stock'] + ',' +  e.record.data['price']);
				init_materialBasicGrid.cateType = e.record.data['isGood']; 
			},
			cellclick : function(grid, rowIndex, columnIndex, e) {
		        var record = grid.getStore().getAt(rowIndex);  // Get the Record
				$.post('../../OperateMaterialInit.do', {dataSource:'isInit'}, function(jr){
					isInit = jr.success;
					if(!isInit){
						Ext.example.msg('提示', '开始录入库单后, 不能修改库存数量');
						record.commit();
					}
				});			        
		        
		    }
		}
	});
	
	init_materialBasicGrid.keys = [{
		 key : Ext.EventObject.ENTER,
		 fn : function(){ 
			 Ext.getCmp('btnSearchInitMaterial').handler();
		 },
		 scope : this 
	}];
	
}

function fnCheckIsInit(){
	$.post('../../OperateMaterialInit.do', {dataSource:'isInit'}, function(jr){
		isInit = jr.success;
	});
}

Ext.onReady(function(){
	
	initMaterialControl();
	
	new Ext.Panel({
		renderTo : 'divInitMaterial',
		id : 'initMaterialPanel',
		layout : 'border',
		height : parseInt(Ext.getDom('divInitMaterial').parentElement.style.height.replace(/px/g,'')),
		frame : true,
		items : [init_materialCateTree, init_materialBasicGrid]
	});
	
	$.post('../../OperateMaterialInit.do', {dataSource:'isInit'}, function(jr){
		isInit = jr.success;
		if(!jr.success){
			initStock();
		}else{
			Ext.getCmp('btnSearchInitMaterial').handler();
		}		
	});

});