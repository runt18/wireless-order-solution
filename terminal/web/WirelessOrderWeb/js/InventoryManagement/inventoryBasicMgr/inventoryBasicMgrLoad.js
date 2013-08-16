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
			hidden : true,
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
				var gs = materialBasicGrid.getStore();
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
	materialBasicGrid = createGridPanel(
		'materialBasicGrid',
		'原料信息',
		'',
		'',
		'../../QueryMaterial.do',
		[
			[true, false, false, true], 
			['原料名称', 'name'],
			['所属类别', 'cateName'],
			['总数量', 'stock'],
			['单位成本', 'price'],
			['状态', 'statusText'],
			['最后修改人', 'lastModStaff'],
			['最后修改时间', 'lastModDateFormat', 150],
			['操作', 'operate', 200, 'center', 'materialBasicGridOperateRenderer']
		],
		['id', 'name', 'cateId', 'cateName', 'stock', 'price', 'statusValue', 'statusText',
		 'lastModStaff', 'lastModDate', 'lastModDateFormat'],
		[['isPaging', true],  ['restaurantID', restaurantID], ['dataSource', 'normal'], ['cateType', 2]],
		GRID_PADDING_LIMIT_20,
		'',
		materialBasicGridTbar
	);
	materialBasicGrid.region = 'center';
	materialBasicGrid.on('rowdblclick', function(){
		operateMaterialHandler({otype:Ext.ux.otype['update']});		
	});
}
	