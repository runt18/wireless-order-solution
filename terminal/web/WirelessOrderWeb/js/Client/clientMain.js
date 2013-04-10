Ext.onReady(function() {
	
	Ext.lib.Ajax.defaultPostHeader += '; charset=utf-8';
	Ext.BLANK_IMAGE_URL = '../extjs/resources/images/default/s.gif';
	Ext.QuickTips.init();

	var centerPanel = new Ext.Panel({
		region : 'center',
		frame : true,
		autoScroll : true,
		tbar : new Ext.Toolbar({
			height : 55,
			items : ['->', new Ext.ux.ImageButton({
				imgPath : '../../images/UserLogout.png',
				imgWidth : 50,
				imgHeight : 50,
				tooltip : '返回',
				handler : function(e){
					location.href = '../PersonLogin.html?restaurantID=' + restaurantID + '&isNewAccess=false' + '&pin=' + pin;
				}
			}), {
				xtype : 'tbtext',
				text : '&nbsp;&nbsp;&nbsp;&nbsp;'
			}, new Ext.ux.ImageButton({
				imgPath : '../../images/ResLogout.png',
				imgWidth : 50,
				imgHeight : 50,
				tooltip : '登出',
				handler : function(e){
					
				}
			}), {
				xtype : 'tbtext',
				text : '&nbsp;&nbsp;&nbsp;&nbsp;'
			}]
		}),
		items : [{
			contentEl : 'protal'
		}]
	});

	 new Ext.Viewport({
		layout : 'border',
		id : 'viewport',
		items : [{
			region : 'north',
			bodyStyle : 'background-color:#DFE8F6;',
			html : '<h4 style="padding:10px;font-size:150%;float:left;">无线点餐网页终端</h4><div id="optName" class="optName"></div>',
			height : 50,
			border : false,
			margins : '0 0 0 0'
		},
		centerPanel,
		{
			region : 'south',
			height : 30,
			layout : 'form',
			frame : true,
			border : false,
			html : '<div style="font-size:11pt; text-align:center;"><b>版权所有(c) 2011 智易科技</b></div>'
		}]
	 });
	 
	 getOperatorName(pin, '../../');
	 
	 bingTrigger();
});

bingTrigger = function(){
	$("#clientMgr").each(function() {
		$(this).bind("click", function() {
			window.location.href = 'ClientManagement.html?'
									+ 'pin=' + pin
									+ '&restaurantID=' + restaurantID;
		});
	});
	
	$("#memberTypeMgr").each(function() {
		$(this).bind("click", function() {
			window.location.href = 'MemberTypeManagement.html?'
									+ 'pin=' + pin
									+ '&restaurantID=' + restaurantID;
		});
	});
	
	$("#memberMgr").each(function() {
		$(this).bind("click", function() {
			window.location.href = 'MemberManagement.html?'
									+ 'pin=' + pin
									+ '&restaurantID=' + restaurantID;
		});
	});
	
	$("#clientMgr").each(function(){
		$(this).hover(function(){
			$(this).stop().css("background", "url(../../images/clientMgr_select.png) no-repeat 50%");
		},
		function(){
			$(this).stop().css("background", "url(../../images/clientMgr.png) no-repeat 50%");
		});
	});
	
	$("#memberTypeMgr").each(function(){
		$(this).hover(function(){
			$(this).stop().css("background", "url(../../images/memberTypeMgr_select.png) no-repeat 50%");
		},
		function(){
			$(this).stop().css("background", "url(../../images/memberTypeMgr.png) no-repeat 50%");
		});
	});
	
	$("#memberMgr").each(function(){
		$(this).hover(function(){
			$(this).stop().css("background", "url(../../images/memberMgr_select.png) no-repeat 50%");
		},
		function(){
			$(this).stop().css("background", "url(../../images/memberMgr.png) no-repeat 50%");
		});
	});
	
};

