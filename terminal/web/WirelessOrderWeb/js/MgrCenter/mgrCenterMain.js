



function loginOnLoad() {
	
	protalFuncReg();
	$("#deviceMgr").each(function() {
		$(this).hover(function() {
			$(this).stop().css("background", "url(../images/deviceMgr_select.png) no-repeat 50%");
		},
		function() {
			$(this).stop().css("background", "url(../images/deviceMgr.png) no-repeat 50%");
		});
	});

	$("#restaurantMgr").each(function() {
		$(this).hover(function() {
			$(this).stop().css("background", "url(../images/resturantMgr_01.png) no-repeat 50%");
		},
		function() {
			$(this).stop().css("background", "url(../images/resturantMgr_02.png) no-repeat 50%");
		});
	});
	
}

Ext.onReady(function(){
	var centerPanel = new Ext.Panel({
		region : "center",
		frame : true,
		autoScroll : true,
		items : [ {
			border : false,
			contentEl : "protal"
		} ]
	});
	

	new Ext.Viewport({
		layout : "border",
		id : "viewport",
		items : [{
			region : "north",
			bodyStyle : "background-color:#DFE8F6;",
			html : "<h4 style='padding:10px;font-size:150%;float:left;'>Digi-e管理中心</h4><div id='optName' class='optName'></div>" +
			       "<div id='divLoginOut' class='loginOut' style='width: 40px;height: 41px;'><img id='btnLoginOut' src='../images/ResLogout.png' width='40' height='40' /> </div>",
			height : 50,
			border : false,
			margins : '0 0 0 0'
		},
		centerPanel,
		{
			region : "south",
			height : 30,
			layout : "form",
			frame : true,
			border : false,
			html : '<div style="font-size:11pt; text-align:center;"><b>版权所有(c) 2011 智易科技</b></div>'
		}]
	 });
	 
	 Ext.get('btnLoginOut').on('click', function(){
		Ext.Ajax.request({
			url : '../../LoginOut.do',
			success : function(){
				location.href = '../LoginAdmin.html';
			},
			failure : function(){
				
			}
		});
    });
	
});