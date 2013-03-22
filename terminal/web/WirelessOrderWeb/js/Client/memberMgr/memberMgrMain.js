var btnInsertMember = new Ext.ux.ImageButton({
	imgPath : ' ',
	imgWidth : 50,
	imgHeight : 50,
	tooltip : '添加会员',
	handler : function(e){
		insertMemberHandler();
	}
});

var btnRecharge = new Ext.ux.ImageButton({
	imgPath : ' ',
	imgWidth : 50,
	imgHeight : 50,
	tooltip : '充值',
	handler : function(e){
		rechargeHandler();
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

changeMemberCardHandler = function(){
	alert('changeMemberCardHandler')
};


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
	memberBasicWin.otype = c.type;
	
	if(c.type == mObj.operation['insert']){
		memberBasicWin.setTitle('添加会员资料');
		memberBasicWin.show();
		memberBasicWin.center();
		Ext.getCmp(mObj.ctSelect.radioBJM.id).setValue(true);
		Ext.getCmp('numberMemberCardAliasID').setDisabled(false);
		operationMembetBasicMsg({
			type : mObj.operation['set'],
			data : {
				status : 0
			}
		});
	}else if(c.type == mObj.operation['update']){
		var data = Ext.ux.getSelData(memberBasicGrid);
		if(!data){
			Ext.example.msg('提示', '请选中一条会员记录再进行操作.');
			return;
		}
		memberBasicWin.setTitle('修改会员资料');
		memberBasicWin.show();
		memberBasicWin.center();
		Ext.getCmp('numberMemberCardAliasID').setDisabled(true);
		operationMembetBasicMsg({
			type : mObj.operation['set'],
			data : data
		});
	}else if(c.type == mObj.operation['delete']){
		var data = Ext.ux.getSelData(memberBasicGrid);
		if(!data){
			Ext.example.msg('提示', '请选中一条会员记录再进行操作.');
			return;
		}
		Ext.Msg.show({
			title : '重要',
			msg : '是否删除会员资料?<br/>一旦成功将无法恢复.',
			buttons : Ext.Msg.YESNO,
			icon : Ext.Msg.QUESTION,
			fn : function(e){
				if(e == 'yes'){
					var params = Ext.encode({
						id : data['id'],
						restaurantID : restaurantID,
						client : {
							restaurantID : restaurantID,
							clientID : data['client']['clientID']
						},
						staff : {
							terminal : {
								restaurantID : restaurantID,
								pin : pin
							}
						}
					});
					Ext.Ajax.request({
						url : '../../DeleteMember.do',
						params : {
							params : params
						},
						success : function(res, opt){
							var jr = Ext.decode(res.responseText);
							if(jr.success){
								Ext.example.msg(jr.title, jr.msg);
								memberBasicGrid.getStore().reload();
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
	}
};

/**********************************************************************/
Ext.onReady(function(){
	
	Ext.BLANK_IMAGE_URL = '../../extjs/resources/images/default/s.gif';
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
			    {xtype : 'tbtext', text : '&nbsp;&nbsp;'},
			    btnRecharge,
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
	 
	getOperatorName(pin, '../../');
	 
	memberBasicWin.render(document.body);
});

