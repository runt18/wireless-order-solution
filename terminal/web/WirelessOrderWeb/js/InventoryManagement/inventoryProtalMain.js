Ext.onReady(function() {
	var pushBackBut = new Ext.ux.ImageButton({
		imgPath : "../../images/UserLogout.png",
		imgWidth : 50,
		imgHeight : 50,
		tooltip : "返回",
		handler : function(btn) {
			location.href = "../PersonLogin.html?restaurantID=" + restaurantID + "&isNewAccess=false&pin=" + pin;
		}
	});

	var logOutBut = new Ext.ux.ImageButton({
		imgPath : "../../images/ResLogout.png",
		imgWidth : 50,
		imgHeight : 50,
		tooltip : "登出",
		handler : function(btn) {
		}
	});

	var centerPanel = new Ext.Panel({
		region : "center",
		frame : true,
		autoScroll : true,
		tbar : new Ext.Toolbar({
			height : 55,
			items : [ "->", pushBackBut, {
				text : "&nbsp;&nbsp;&nbsp;",
				disabled : true
			}, logOutBut ]
		}),
		items : [ {
			border : false,
			contentEl : "protal"
		} ]
	});
	
	initMainView(null,centerPanel,null);
	getOperatorName(pin, "../../");
	
});
