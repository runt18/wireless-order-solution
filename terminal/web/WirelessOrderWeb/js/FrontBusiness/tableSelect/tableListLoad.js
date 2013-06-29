function getData(_c) {
	_c = _c != null && typeof _c != 'undefined' ? _c : {};
	
	tableStatusListTS.length = 0;
	tableStatusListTSDisplay.length = 0;
	tableMergeList.length = 0;

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