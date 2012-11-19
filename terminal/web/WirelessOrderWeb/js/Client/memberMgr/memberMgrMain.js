﻿var btnInsertMember = new Ext.ux.ImageButton({
	imgPath : ' ',
	imgWidth : 50,
	imgHeight : 50,
	tooltip : '添加会员',
	handler : function(e){
		insertMemberHandler();
	}
});

var btnPushBack = new Ext.ux.ImageButton({
	imgPath : '../../images/UserLogout.png',
	imgWidth : 50,
	imgHeight : 50,
	tooltip : '返回',
	handler : function(e){
		location.href = './ClientMain.html?restaurantID=' + restaurantID + '&isNewAccess=false&' + '&pin=' + pin;
	}
});

var btnLogOut =  new Ext.ux.ImageButton({
	imgPath : '../../images/ResLogout.png',
	imgWidth : 50,
	imgHeight : 50,
	tooltip : '登出',
	handler : function(e){
		
	}
});

/**********************************************************************/
insertMemberHandler = function(){
	memberOperationHandler({
		type : mObj.operation['insert']
	});
};

updateMemberHandler = function(){
	memberOperationHandler({
		type : mObj.operation['update']
	});
};

deleteMemberHandler = function(){
	memberOperationHandler({
		type : mObj.operation['delete']
	});
};

memberOperationHandler = function(c){
	if(c == null || typeof c == 'undefined' || typeof c.type == 'undefined'){
		return;
	}
	alert(c.type);
};

/**********************************************************************/
Ext.onReady(function(){
	
	Ext.lib.Ajax.defaultPostHeader += '; charset=utf-8';
	Ext.QuickTips.init();

	var centerPanel = new Ext.Panel({
		title : '会员管理',
		region : 'center',
		layout : 'border',
		items : [memberTypeTree, memberBasicGrid],
		frame : true,
		autoScroll : true,
		tbar : new Ext.Toolbar({
			height : 55,
			items : [
			    {xtype : 'tbtext', text : '&nbsp;&nbsp;'},
			    btnInsertMember,
			    '->',
			    btnPushBack,
			    {xtype : 'tbtext', text : '&nbsp;&nbsp;'},
			    btnLogOut
			]
		})
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
	 
});

