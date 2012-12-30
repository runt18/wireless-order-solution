function getData() {

	var Request = new URLParaQuery();
	pin = Request["pin"];

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
			resultJSON = Ext.util.JSON.decode(response.responseText);

			var rootData = resultJSON.root;
			// if (resultJSON.success == true) {
			if (rootData.length != 0) {
				if (rootData[0].message == "normal") {
					
					tableStatusListTSDisplay = rootData.slice(0);
					tableStatusListTS = rootData.slice(0);

					tableListReflash(null);

					// keyboard input table number
					$("#tableNumber").bind("keyup", tableKeyboardSelect);
				} else {
					Ext.MessageBox.show({
						msg : rootData[0].message,
						width : 300,
						buttons : Ext.MessageBox.OK
					});
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

	// 展開區域樹節點
	if(typeof(regionTree) != 'undefined'){
		regionTree.expandAll();
	}
	
	// update the operator name
	getOperatorName(pin, "../../");

	// 随机刷新
	var x = 300000;
	var y = 60000;
	var rand = parseInt(Math.random() * (x - y + 1) + y);
	setInterval(function() {
		getData();
	}, rand);

};