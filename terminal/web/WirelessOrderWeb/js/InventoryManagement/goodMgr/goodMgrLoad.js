function initControl(){
	kitchenTreeForSreach = new Ext.tree.TreePanel({
		region : 'west',
		frame : true,
		width : 200,
		border : true,
		rootVisible : true,
		autoScroll : true,
		frame : true,
		bodyStyle : 'backgroundColor:#FFFFFF; border:1px solid #99BBE8;',
		loader : new Ext.tree.TreeLoader({
			dataUrl : '../../QueryKitchenMgr.do',
			baseParams : {
				dataSource : 'normal',
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
				var root = kitchenTreeForSreach.getRootNode();
				for(var i = root.childNodes.length - 1; i >= 0 ; i--){
					root.childNodes[i].remove();
				}
				for(var i = 0; i < kitchenData.length; i++){
					root.appendChild(new Ext.tree.TreeNode({
						text : kitchenData[i].name,
						aliasId : kitchenData[i].aliasId
					}));
				}
				root.expand();
			}
		}],
		listeners : {
			dblclick : function(node, e){
				
			}
		}
	});
	
}