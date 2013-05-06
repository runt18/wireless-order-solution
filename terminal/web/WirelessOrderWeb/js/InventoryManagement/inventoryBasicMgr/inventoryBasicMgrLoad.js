function initControl(){
	var materialCateTreeTbae = new Ext.Toolbar({
		height : 26,
		items : ['->', {
			text : '添加',
			iconCls : 'btn_add',
			handler : function(){
				if(!operateMaterialCateWin){
					operateMaterialCateWin = new Ext.Window({
						id : 'operateMaterialCateWin',
						title : '&nbsp;',
						modal : true,
						resizable : false,
						closable : false,
						width : 230,
						items : [{
							xtype : 'form',
							layout : 'form',
							labelWidth : 65,
							frame : true,
							items : [{
								xtype : 'textfield',
								fieldLabel : '类别名称',
								width : 130
							}]
						}],
						bbar : ['->', {
							text : '保存',
							iconCls : 'btn_save',
							handler : function(){
								
							}
						}, {
							text : '关闭',
							iconCls : 'btn_close',
							handler : function(){
								operateMaterialCateWin.hide();
							}
						}]
					});
				}
				operateMaterialCateWin.show();
				operateMaterialCateWin.center();
			}
		}, {
			text : '修改',
			iconCls : 'btn_edit',
			handler : function(){
				
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
			text : '全部类别',
	        leaf : false,
	        border : true,
	        cateId : -1,
	        listeners : {
	        	load : function(){
	        		
	        	}
	        }
		})
	});
	
	materialBasicGrid = new Ext.Panel({
		title : '原料基础信息',
		region : 'center',
		frame : true,
		tbar : ['->', {
			text : 'asdasd'
		}]
	});
}
	