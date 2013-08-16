updateCancelReasonHandler = function(){
	cancelReasonOperationHandler({
		type : bmObj.operation['update']
	});
};

deleteCancelReasonHandler = function(){
	cancelReasonOperationHandler({
		type : bmObj.operation['delete']
	});
};

cancelReasonOperationHandler = function(c){
	if(c == null || typeof c == 'undefined' || typeof c.type == 'undefined'){
		return;
	}
	oPanel.otype = c.type;
	
	if(c.type == bmObj.operation['insert']){
		oCancelReasonData({
			type : bmObj.operation['set']
		});
		oPanel.setTitle('添加退菜原因');
		oPanel.show();
		cancelReasonWin.doLayout();
	}else if(c.type == bmObj.operation['update']){
		var sd = Ext.ux.getSelData(crGrid);
		if(!sd){
			Ext.example.msg('提示', '请选中一个原因再进行操作.');
			oPanel.hide();
			cancelReasonWin.doLayout();
			return;
		}
		oCancelReasonData({
			type : bmObj.operation['set'],
			data : sd
		});
		oPanel.setTitle('修改退菜原因');
		oPanel.show();
		cancelReasonWin.doLayout();
	}else if(c.type == bmObj.operation['delete']){
		var sd = Ext.ux.getSelData(crGrid);
		if(!sd){
			Ext.example.msg('提示', '请选中一个原因再进行操作.');
			oPanel.hide();
			cancelReasonWin.doLayout();
			return;
		}
		Ext.Msg.show({
			title : '重要',
			msg : '是否删除退菜原因?',
			buttons :Ext.Msg.YESNO,
			icon: Ext.MessageBox.QUESTION,
			fn : function(btn){
				if(btn == 'yes'){
					Ext.Ajax.request({
						url : '../../DeleteCancelReason.do',
						params : {
							cancelReason : Ext.encode({
								id : sd['id'],
								restaurantID : restaurantID
							})
						},
						success : function(res, opt){
							var jr = Ext.util.JSON.decode(res.responseText);
							if(jr.success){
								Ext.example.msg(jr.title, jr.msg);
								Ext.getCmp('btnCloseCancelPanel').handler();
								Ext.getCmp('btnRefreshCRGrid').handler();
							}else{
								Ext.ux.showMsg(jr);
							}
						},
						failure : function(res, opt){
							Ext.ux.showMsg(Ext.decode(res.responseText));
						}
					});
				}
			}
		});
	}else{
		Ext.example.msg('错误', '未知操作类型, 请联系管理员');
	}
};

oCancelReasonData = function(c){
	if(c == null || c.type == null || typeof c.type == 'undefined')
		return;
	var data = {};
	var id = Ext.getCmp('numCancelReasonID');
	var reason = Ext.getCmp('txtCancelReason');
	if(c.type == bmObj.operation['set']){
		data = c.data == null || typeof c.data == 'undefined' ? {} : c.data;
		id.setValue(data['id']);
		reason.setValue(data['reason']);
	}else if(c.type == bmObj.operation['get']){
		data = {
			restaurantID : restaurantID,
			id : id.getValue(),
			reason : reason.getValue()
		};
		c.data = data;
	}
	reason.clearInvalid();
	return c;
};

// ******************************************************************************************************
Ext.onReady(function() {
	// 解决ext中文传入后台变问号问题
	Ext.lib.Ajax.defaultPostHeader += '; charset=utf-8';
	Ext.QuickTips.init();

	initWin();
	
	// ******************************************************************************************************
	var pushBackBut = new Ext.ux.ImageButton({
		imgPath : "../../images/UserLogout.png",
		imgWidth : 50,
		imgHeight : 50,
		tooltip : "返回",
		handler : function(btn) {
			location.href = "../PersonLogin.html?"+ strEncode('restaurantID=' + restaurantID + '&isNewAccess=false', 'mi');
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
			items : [  "->", pushBackBut, {
				text : "&nbsp;&nbsp;&nbsp;",
				disabled : true
				}, 
				logOutBut 
			]
		}),
		items : [ {
			border : false,
			contentEl : "protal"
		}]
	});

	new Ext.Viewport({
		layout : "border",
		id : "viewport",
		items : [{
			region : "north",
			bodyStyle : "background-color:#DFE8F6;",
			html : "<h4 style='padding:10px;font-size:150%;float:left;'>无线点餐网页终端</h4><div id='optName' class='optName'></div>",
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
			html : "<div style='font-size:11pt; text-align:center;'><b>版权所有(c) 2011 智易科技</b></div>"
		} ]
	});
});
