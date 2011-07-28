//从url获取当前桌信息
function URLParaQuery() {
	var name, value, i;
	var str = location.href;
	var num = str.indexOf("?")
	str = str.substr(num + 1);
	var arrtmp = str.split("&");
	for (i = 0; i < arrtmp.length; i++) {
		num = arrtmp[i].indexOf("=");
		if (num > 0) {
			name = arrtmp[i].substring(0, num);
			value = arrtmp[i].substr(num + 1);
			this[name] = value;
		}
	}
}

// 获取操作人姓名
// 此函数要求页面上有operatorName,restaurantID全局变量；有id为optName的div
function getOperatorName(pin) {
	Ext.Ajax.request({
		url : "../QueryStaff.do",
		params : {
			"restaurantID" : restaurantID
		},
		success : function(response, options) {
			var resultJSON = Ext.util.JSON.decode(response.responseText);
			if (resultJSON.success == true) {
				var dataInfo = resultJSON.data;
				var staffList = dataInfo.split("，");
				for ( var i = 0; i < staffList.length; i++) {
					var staffInfo = staffList[i].substr(1,
							staffList[i].length - 2).split(',');
					// find the name
					if (staffInfo[0] == pin) {
						operatorName = staffInfo[1].substr(1,
								staffInfo[1].length - 2);
					}
				}
				// update the page
				document.getElementById("optName").innerHTML = operatorName;

			} else {
				var dataInfo = resultJSON.data;
				Ext.MessageBox.show({
					msg : dataInfo,
					width : 300,
					buttons : Ext.MessageBox.OK
				});
			}
		},
		failure : function(response, options) {
		}
	});
};

// 拼台时，获取主/副台号
// 此函数要求页面上有tableMergeList全局变量
function getMergeTable(tableNbr) {
	var mainTblNbr = 0;
	var mergeTblNbr = 0;
	var isFound = false;
	for ( var i = 0; i < tableMergeList.length; i++) {
		if (tableMergeList[i][0] == tableNbr) {
			mainTblNbr = tableNbr;
			mergeTblNbr = tableMergeList[i][1];
			isFound = true;
		}
	}
	if (!isFound) {
		for ( var i = 0; i < tableMergeList.length; i++) {
			if (tableMergeList[i][1] == tableNbr) {
				mainTblNbr = tableMergeList[i][0];
				mergeTblNbr = tableNbr;
			}
		}
	}
	var tblArr = [];
	tblArr.push(mainTblNbr);
	tblArr.push(mergeTblNbr);
	return tblArr;
};