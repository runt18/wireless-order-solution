// 从url获取当前桌信息
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

// on page load function
function loginOnLoad() {

	var Request = new URLParaQuery();
	restaurantID = Request["restaurantID"];

//	// for local test
//	if (restaurantID == undefined) {
//		restaurantID = "11";
//	}

	// emplData: [pin，姓名，密码]
	// 后台格式：{success:true,
	// data:'[0x1,"张宁远","d7a7b87838c6e3853f3f6d3bdc836a7c"]，[0x2,"李颖宜","6718853969f567306e3c753c32d3b88d"]'}
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
				emplData = [];
				for ( var i = 0; i < staffList.length; i++) {
					var staffInfo = staffList[i].substr(1,
							staffList[i].length - 2).split(',');
					emplData.push([ staffInfo[0], // pin
					staffInfo[1].substr(1, staffInfo[1].length - 2), // 姓名
					staffInfo[2].substr(1, staffInfo[2].length - 2) // 密码
					]);
				}

				emplComboData.length = 0;
				for ( var i = 0; i < emplData.length; i++) {
					emplComboData.push([ emplData[i][0],// pin
					emplData[i][1] // 姓名
					]);
				}

				emplStore.reload();
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