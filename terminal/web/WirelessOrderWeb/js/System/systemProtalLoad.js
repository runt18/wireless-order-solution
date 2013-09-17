// on page load function
function loginOnLoad() {
	
	Ext.getDom('formatPrice').onclick = function(){		
		formatPrice.show();
		formatPrice.center();
	};
	
	Ext.getDom('resturantMgr').onclick = function(){		
		resturantMgr.show();
	};
	
	$("#staffMgr").each(function() {
		$(this).bind("click", function() {
				location.href = "StaffManagement.html?"+ strEncode('restaurantID=' + restaurantID, 'mi');
		});
	});

	
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
	
	$("#formatPrice").each(function() {
		$(this).hover(function() {
			$(this).stop().css("background", "url(../../images/formatPrice_select.png) no-repeat 50%");
		},
		function() {
			$(this).stop().css("background", "url(../../images/formatPrice.png) no-repeat 50%");
		});
	});
	
	$("#resturantMgr").each(function() {
		$(this).hover(function() {
			$(this).stop().css("background", "url(../../images/resturantMgr_01.png) no-repeat 50%");
		},
		function() {
			$(this).stop().css("background", "url(../../images/resturantMgr_02.png) no-repeat 50%");
		});
	});
	

	$("#staffMgr").each(function(){
		$(this).hover(function(){
			$(this).stop().css("background", "url(../../images/staffMgr_select.png) no-repeat 50%");
		},
		function(){
			$(this).stop().css("background", "url(../../images/staffMgr.png) no-repeat 50%");
		});
	});
	
	
};