var btnInsertMember = new Ext.ux.ImageButton({
	imgPath : '../../images/btnAddMember.png',
	imgWidth : 50,
	imgHeight : 50,
	tooltip : '添加会员',
	handler : function(e){
		insertMemberHandler();
	}
});

var btnRecharge = new Ext.ux.ImageButton({
	imgPath : '../../images/btnMemberRecharge.png',
	imgWidth : 50,
	imgHeight : 50,
	tooltip : '充值',
	handler : function(e){
		rechargeHandler();
	}
});

var btnConsumeDetail = new Ext.ux.ImageButton({
	imgPath : '../../images/btnConsumeDetail.png',
	imgWidth : 50,
	imgHeight : 50,
	tooltip : '消费明细',
	handler : function(e){
		queryMemberOperationHandler();
	}
});

var btnConsumeSummary = new Ext.ux.ImageButton({
	imgPath : '../../images/btnConsumeSummary.png',
	imgWidth : 50,
	imgHeight : 50,
	tooltip : '消费汇总',
	handler : function(e){
		queryMemberConsumeSummaryHandler();
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
	var data = Ext.ux.getSelData(memberBasicGrid);
	if(!data){
		Ext.example.msg('提示', '请选中一条会员信息.');
		return;
	}
	var changeMemberCardWin = Ext.getCmp('changeMemberCardWin');
	if(!changeMemberCardWin){
		var cmcw_hide_memberID = new Ext.form.Hidden({});	
		var cmcw_memberName = new Ext.form.TextField({
			fieldLabel : '会员名称',
			disabled : true
		});
		var cmcw_oldMemberCard = new Ext.form.TextField({
			fieldLabel : '原卡号',
			disabled : true
		});
		var cmcw_newMemberCard = new Ext.form.TextField({
			fieldLabel : '新卡号',
			allowBlank : false,
			validator : function(v){
				if(/^\d{10}$/.test(Ext.util.Format.trim(v)))
					return true;
				else
					return '请输入 10 纯数字卡号';
			}
		});
		changeMemberCardWin = new Ext.Window({
			title : '更换会员卡',
			width : 200,
			modal : true,
			closable : false,
			resizable : false,
			items : [{
				xtype : 'panel',
				frame : true,
				defaults : {
					xtype : 'form',
					layout : 'form',
					labelWidth : 60,
					border : false,
					defaults : {
						width : 100
					}
				},
				items : [{
					items : [cmcw_hide_memberID]
				}, {
					items : [cmcw_memberName]
				}, {
					items : [cmcw_oldMemberCard]
				}, {
					items : [cmcw_newMemberCard]
				}]
			}],
			keys : [{
				key : Ext.EventObject.ESC,
				scope : this,
				fn : function(){
					changeMemberCardWin.hide();
				}
			}],
			listeners : {
				show : function(){
					var data = Ext.ux.getSelData(memberBasicGrid);
					cmcw_hide_memberID.setValue(data['id']);
					cmcw_memberName.setValue(data['client.name']);
					cmcw_oldMemberCard.setValue('******'+data['memberCard.aliasID'].substring(6, 10));
					cmcw_newMemberCard.setValue();
					cmcw_newMemberCard.clearInvalid();
				}
			},
			bbar : ['->', {
				text : '保存',
				iconCls : 'btn_save',
				handler : function(){
					if(!cmcw_newMemberCard.isValid()){
						return;
					}
					if(cmcw_oldMemberCard.getValue() == Ext.util.Format.trim(cmcw_newMemberCard.getValue())){
//						Ext.example.msg('提示', '新旧卡一样, 请重新输入新卡.');
						cmcw_newMemberCard.markInvalid('提示', '新旧卡一样, 请重新输入新卡.');
						return;
					}
					
					Ext.Ajax.request({
						url : '../../MemberCard.do',
						params : {
							dataSource : 'change',
							pin : pin,
							restaurantID : restaurantID,
							memberID : cmcw_hide_memberID.getValue(),
							newCard : cmcw_newMemberCard.getValue()
						},
						success : function(res, opt){
							var jr = Ext.decode(res.responseText);
							if(jr.success){
								Ext.example.msg(jr.title, jr.msg);
								changeMemberCardWin.hide();
								var st = Ext.getCmp('comboMemberSearchType');
								st.setValue(2);
								st.fireEvent('select', st, null, null);
								var n = Ext.getCmp('numberSearchValueByNumber');
								n.setValue(Ext.util.Format.trim(cmcw_newMemberCard.getValue()));
								Ext.getCmp('btnSearchMember').handler();
							}else{
								Ext.ux.showMsg(jr);
							}
						},
						failure : function(res, opt){
							changeMemberCardWin.hide();
							Ext.ux.showMsg(Ext.decode(res.responseText));
						}
					});
				}
			}, {
				text : '关闭',
				iconCls : 'btn_close',
				handler : function(){
					changeMemberCardWin.hide();
				}
			}]
		});
	}
	changeMemberCardWin.show();
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
	}else if(c.type == mObj.operation['update']){
		var data = Ext.ux.getSelData(memberBasicGrid);
		if(!data){
			Ext.example.msg('提示', '请选中一条会员记录再进行操作.');
			return;
		}
		memberBasicWin.setTitle('修改会员资料');
		memberBasicWin.show();
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
			    {xtype : 'tbtext', text : '&nbsp;&nbsp;'},
			    btnConsumeDetail,
			    {xtype : 'tbtext', text : '&nbsp;&nbsp;'},
			    btnConsumeSummary,
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

