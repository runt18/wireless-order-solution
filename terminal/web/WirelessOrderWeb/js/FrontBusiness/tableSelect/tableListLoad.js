function getData(_c) {
	_c = _c != null && typeof _c != 'undefined' ? _c : {};
	
	tableStatusListTS.length = 0;
	tableStatusListTSDisplay.length = 0;
	tableMergeList.length = 0;

	// 后台：["餐台1编号","餐台1人数","占用","餐台1名称","一般",0]，["餐台2编号","餐台2人数","空桌","餐台2名称","外卖",300.50]
	// 后台：[ID，別名編號，名稱，區域，人數，狀態，種類，最低消費]
	// 页面：tableStatusListTS和后台一致
	Ext.Ajax.request({
		url : "../../QueryTable.do",
		params : {
			"pin" : pin,
			"type" : 0,
			"isPaging" : false,
			"isCombo" : false
		},
		success : function(response, options) {
			var jr = Ext.decode(response.responseText);
			if(eval(jr.success)){
				tableStatusListTSDisplay = jr.root.slice(0);
				tableStatusListTS = jr.root.slice(0);
				tableListReflash(null);
				$("#tableNumber").bind("keyup", tableKeyboardSelect);
				if(typeof _c.callBack == 'function'){
					_c.callBack(jr);
				}
			}
		},
		failure : function(response, options) {
			
		}
	});

};

// on page load function
function tableSelectOnLoad() {
	
	getData();

	if(typeof(regionTree) != 'undefined'){
		regionTree.getRootNode().reload();
	}
	
	getOperatorName(pin, "../../");

	// 随机刷新, 利用时间戳为服务器减压
	var x = 1000 * 60 * 5, y = 1000 * 60 * 3;
	var rand = parseInt(Math.random() * (x - y + 1) + y);
	setInterval(function() {
		getData();
	}, rand);

};