// on page load function
function loginOnLoad() {
	
	restaurantID = Request["restaurantID"];

	// for local test
	if (restaurantID == undefined) {
		restaurantID = "18";
	}

	// protal function register
	protalFuncReg();

	// update the operator name
	getOperatorName("../../");
	// emplData: [pin，姓名，密码]
	// 后台格式：{success:true,
	// data:'[0x1,"张宁远","d7a7b87838c6e3853f3f6d3bdc836a7c"]，[0x2,"李颖宜","6718853969f567306e3c753c32d3b88d"]'}
	Ext.Ajax.request({
		url : "../../QueryStaff.do",
		params : {
			"restaurantID" : restaurantID,
			"type" : 0,
			"isPaging" : false,
			"isCombo" : false
		},
		success : function(response, options) {
			var resultJSON = Ext.util.JSON.decode(response.responseText);
			var rootData = resultJSON.root;
			var staffData = [];
			if (rootData.length != 0) {
				if (resultJSON.msg == "normal") {
					staffData = rootData;
					emplData = [];
					for ( var i = 0; i < staffData.length; i++) {
						emplData.push([ staffData[i].staffID, // pin
						staffData[i].staffName, // 姓名
						staffData[i].staffPassword // 密码
						]);
					}
				} else {
					Ext.MessageBox.show({
						msg : resultJSON.msg,
						width : 300,
						buttons : Ext.MessageBox.OK
					});
				}
			}
		},
		failure : function(response, options) {
		}
	});

	// mouse over & mouse off -- heightlight the icon
	$("#order").each(function(){
		$(this).hover(function(){
			$(this).stop().css("background", "url(../../images/order_select.png) no-repeat 50%");
		},
		function(){
			$(this).stop().css("background", "url(../../images/order.png) no-repeat 50%");
		});
	});

	$("#bill").each(function(){
		$(this).hover(function(){
			$(this).stop().css("background", "url(../../images/bill_select.png) no-repeat 50%");
		},
		function(){
			$(this).stop().css("background", "url(../../images/bill.png) no-repeat 50%");
		});
	});

	$("#shift").each(function(){
		$(this).hover(function(){
			$(this).stop().css("background", "url(../../images/shift_selected.png) no-repeat 50%");
		},
		function(){
			$(this).stop().css("background", "url(../../images/shift.png) no-repeat 50%");
		});
	});
	
	$("#dailySettle").each(function(){
		$(this).hover(function(){
			$(this).stop().css("background", "url(../../images/dailySettle_select.png) no-repeat 50%");
		},
		function(){
			$(this).stop().css("background", "url(../../images/dailySettle.png) no-repeat 50%");
		});
	});
	
};