// on page load function
function regionMgrOnLoad() {

	// update the operator name
	getOperatorName(pin, "../../");

	// loadDepartment();
	// departmentStore.reload();
	
	//测试
/*	Ext.onReady(function() {
		var root = new Ext.tree.TreeNode({
			id : "root",
			href : "http://www.easyjf.com",
			hrefTarget : "_blank",
			text : "树的根"
		});
		var c1 = new Ext.tree.TreeNode({
			id : "c1",
			href : "http://wlr.easyjf.com",
			hrefTarget : "_blank",
			text : "子节点"
		});
		root.appendChild(c1);
		var tree = new Ext.tree.TreePanel({
			renderTo : "hello",
			root : root,
			width : 100
		});
	});*/
};
