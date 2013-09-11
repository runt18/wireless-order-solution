// on page load function
function loginOnLoad() {

	var Request = new URLParaQuery();
	restaurantID = Request["restaurantID"];
	// for local test
	if (restaurantID == undefined) {
		restaurantID = "18";
	}

	// protal function register
	protalFuncReg();

	// emplData: [pin，姓名，密码]
	// 后台格式：{success:true,
	// data:'[0x1,"张宁远","d7a7b87838c6e3853f3f6d3bdc836a7c"]，[0x2,"李颖宜","6718853969f567306e3c753c32d3b88d"]'}
	Ext.Ajax.request({
		url : "../VerifyLogin.do",
		success : function(res, opt) {
			var jr = Ext.decode(res.responseText);
			if(jr.success){
				document.getElementById("optName").innerHTML = jr.other.staff.staffName;
			}else{
				Ext.Ajax.request({
					url : "../QueryStaff.do",
					params : {
						"restaurantID" : restaurantID,
						"type" : 0,
						"isPaging" : false,
						"isCombo" : false,
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

								emplComboData.length = 0;
								for ( var i = 0; i < staffData.length; i++) {
									emplComboData.push([ staffData[i].staffID,// pin
									staffData[i].staffName // 姓名
									]);
								}

								emplStore.reload();
								personLoginWin.show();
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
			}
		},
		failure : function(){
			
		}
	});
	


	// update the operator name
/*	if (currPin != "") {
		getOperatorName(currPin, "../");
	}*/

	// shiftWin.show();
	// shiftWin.hide();

	// mouse over & mouse off -- heightlight the icon
	$("#frontBusiness").each(function() {
		$(this).hover(function() {
			$(this).stop().css("background", "url(../images/front_select.png) no-repeat 50%");
		},
		function() {
			$(this).stop().css("background","url(../images/front.png) no-repeat 50%");
		});
	});

	$("#member").each(function(){
		$(this).hover(function() {
			$(this).stop().css("background", "url(../images/member_select.png) no-repeat 50%");
		},
		function() {
			$(this).stop().css("background", "url(../images/member.png) no-repeat 50%");
		});
	});
	
	$("#history").each(function() {
		$(this).hover(function() {
			$(this).stop().css("background", "url(../images/history_select.png) no-repeat 50%");
		},
		function(){
			$(this).stop().css("background", "url(../images/history.png) no-repeat 50%");
		});
	});

	$("#basicManagement").each(function() {
		$(this).hover(function() {
			$(this).stop().css("background", "url(../images/basicManagement_select.png) no-repeat 50%");
		},
		function() {
			$(this).stop().css("background", "url(../images/basicManagement.png) no-repeat 50%");
		});
	});

	$("#system").each(function() {
		$(this).hover(function() {
			$(this).stop().css("background", "url(../images/system_select.png) no-repeat 50%");
		},
		function() {
			$(this).stop().css("background", "url(../images/system.png) no-repeat 50%");
		});
	});

	$("#logout").each(function() {
		$(this).hover(function() {
			$(this).stop().css("background", "url(../images/logout_select.png) no-repeat 50%");
		},
		function() {
			$(this).stop().css("background", "url(../images/logout.png) no-repeat 50%");
		});
	});

	$("#inventoryManagement").each(function() {
		$(this).hover(function() {
			$(this).stop().css("background", "url(../images/inventory_select.png) no-repeat 50%");
		},
		function() {
			$(this).stop().css("background", "url(../images/inventory.png) no-repeat 50%");
		});
	});

};