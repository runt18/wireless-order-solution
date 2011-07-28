﻿// on page load function
function loginOnLoad() {

	var Request = new URLParaQuery();
	restaurantID = Request["restaurantID"];

	// for local test
	if (restaurantID == undefined) {
		restaurantID = "11";
	}
	
	// protal function register
	protalFuncReg();

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
				personLoginWin.show();
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
	
	
	// mouse over & mouse off -- heightlight the icon
	$("#order").each(function() {
		$(this).hover(function() {
			$(this).stop().css("background","url(../images/order_select.png) no-repeat 50%");
		}, function() {
			$(this).stop().css("background","url(../images/order.png) no-repeat 50%");
		});
	});
	
	$("#bill").each(function() {
		$(this).hover(function() {
			$(this).stop().css("background","url(../images/bill_select.png) no-repeat 50%");
		}, function() {
			$(this).stop().css("background","url(../images/bill.png) no-repeat 50%");
		});
	});
	
	$("#member").each(function() {
		$(this).hover(function() {
			$(this).stop().css("background","url(../images/member_select.png) no-repeat 50%");
		}, function() {
			$(this).stop().css("background","url(../images/member.png) no-repeat 50%");
		});
	});
	
	$("#book").each(function() {
		$(this).hover(function() {
			$(this).stop().css("background","url(../images/book_select.png) no-repeat 50%");
		}, function() {
			$(this).stop().css("background","url(../images/book.png) no-repeat 50%");
		});
	});
	
	$("#system").each(function() {
		$(this).hover(function() {
			$(this).stop().css("background","url(../images/system_select.png) no-repeat 50%");
		}, function() {
			$(this).stop().css("background","url(../images/system.png) no-repeat 50%");
		});
	});
	
	$("#logout").each(function() {
		$(this).hover(function() {
			$(this).stop().css("background","url(../images/logout_select.png) no-repeat 50%");
		}, function() {
			$(this).stop().css("background","url(../images/logout.png) no-repeat 50%");
		});
	});

};