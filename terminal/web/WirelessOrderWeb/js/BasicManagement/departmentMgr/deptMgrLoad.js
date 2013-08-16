function loadDepartment() {
//	deptData = [];
//	Ext.Ajax.request({
//		url : '../../QueryDeptTree.do',
//		params : {
//			restaurantID : restaurantID
//		},
//		success : function(response, options) {
//			deptData = eval(response.responseText);
//		},
//		failure : function(response, options) {
//			Ext.MessageBox.show({
//				title : '提示',
//				msg : '加载部门信息失败.',
//				buttons : Ext.MessageBox.OK
//			});
//		}
//	});
}

function deptMgrOnLoad() {

	getOperatorName("../../");

//	loadDepartment();
	
};
