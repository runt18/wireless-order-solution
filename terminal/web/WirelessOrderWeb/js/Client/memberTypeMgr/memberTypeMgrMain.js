var btnInsertMemberType = new Ext.ux.ImageButton({
	imgPath : '../../images/btnAddMemberType.png',
	imgWidth : 50,
	imgHeight : 50,
	tooltip : '添加会员类型',
	handler : function(e){
		insertMemberTypeHandler();
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
function insertMemberTypeHandler(){
	memberTypeOperationHandler({
		type : mtObj.operation['insert']
	});
};
function updateMemberTypeHandler(){
	memberTypeOperationHandler({
		type : mtObj.operation['update']
	});
};
function deleteMemberTypeHandler(){
	memberTypeOperationHandler({
		type : mtObj.operation['delete']
	});
};

function memberTypeOperationHandler(c){
	if(c == null || typeof c == 'undefined' || typeof c.type == 'undefined'){
		return;
	}
	memberTypeWin.otype = c.type;
	
	if(c.type == mtObj.operation['insert']){
		
		memberTypeWin.setTitle('添加会员类型');
		memberTypeWin.show();
		memberTypeWin.center();
		
		bindMemberTypeData({
			discountType : 0,
			chargeRate : 1.00,
			exchangeRate : 1.00,
			initialPoint : 0
		});
	}else if(c.type == mtObj.operation['update']){
		var sd = Ext.ux.getSelData(memberTypeGrid);
		if(!sd){
			Ext.example.msg('提示', '请选中一个会员类型再进行操作.');
			return;
		}
		memberTypeWin.setTitle('修改会员类型');
		memberTypeWin.show();
		memberTypeWin.center();
		
		bindMemberTypeData(sd);
	}else if(c.type == mtObj.operation['delete']){
		var sd = Ext.ux.getSelData(memberTypeGrid.getId());
		if(!sd){
			Ext.example.msg('提示', '请选中一个会员类型再进行操作.');
			return;
		}
		Ext.Msg.show({
			title : '提示',
			msg : '是否删除会员类型?<br><font color="red">提示:如果该类型下已有会员则删除失败.</font>',
			buttons : Ext.Msg.YESNO,
			fn : function(e){
				if(e == 'yes'){
					Ext.Ajax.request({
						url : '../../OperateMemberType.do',
						params : {
							dataSource : 'delete',
							restaurantID : restaurantID,
							pin : pin,
							typeID : sd['id'],
							discountType : sd['discountTypeValue'],
							discountID : sd['discount']['id']
						},
						success : function(res, opt){
							var jr = Ext.decode(res.responseText);
							if(jr.success){
								Ext.example.msg(jr.title, jr.msg);
								memberTypeGrid.getStore().reload();
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

function bindMemberTypeData(d){
	var typeID = Ext.getCmp('numTypeID');
	var typeName = Ext.getCmp('txtTypeName');
	var chargeRate = Ext.getCmp('numChargeRate');
	var exchangeRate = Ext.getCmp('numExchangeRate');
	var initialPoint = Ext.getCmp('numInitialPoint');
	var discountType = Ext.getCmp('comboDiscountType');
	var discount = Ext.getCmp('comboDiscount');
	var discountRate = Ext.getCmp('numDiscountRate');
	var attribute = Ext.getCmp('comboAttribute');
	
	typeID.setValue(d['id']);
	typeName.setValue(d['name']);
	exchangeRate.setValue(d['exchangeRate']);
	initialPoint.setValue(typeof d['initialPoint'] != 'undefined' ? d['initialPoint'] : 0);
	discountType.setValue(d['discountTypeValue']);
	
	if(typeof d['attributeValue'] == 'undefined'){
		attribute.setValue(1);
	}else{
		attribute.setValue(d['attributeValue']);
	}
	attribute.fireEvent('select', attribute);
	chargeRate.setValue(d['chargeRate']);
	
	if(d['discountTypeValue'] == 0){
		discountRate.setValue();
		discountRate.setDisabled(true);
		discount.setValue(d['discount']['id']);
		discount.setDisabled(false);
	}else if(d['discountTypeValue'] == 1){
		discountRate.setValue(d['discountRate']);
		discountRate.setDisabled(false);
		discount.setValue();
		discount.setDisabled(true);
	}else{
		discountRate.setValue();
		discount.setValue();
		discountRate.setDisabled(true);
		discount.setDisabled(true);
	}
	
	typeID.clearInvalid();
	typeName.clearInvalid();
	chargeRate.clearInvalid();
	initialPoint.clearInvalid();
	attribute.clearInvalid();
	discountType.clearInvalid();
	discountRate.clearInvalid();
	discount.clearInvalid();
};


/**********************************************************************/
Ext.onReady(function(){
	//
	dataInit();
	//
	initMemberTypeGrid();
	
	var centerPanel = new Ext.Panel({
		title : '会员类型管理',
		region : 'center',
		layout : 'border',
		items : [memberTypeGrid],
		frame : true,
		autoScroll : true,
		tbar : new Ext.Toolbar({
			height : 55,
			items : [
			    {xtype : 'tbtext', text : '&nbsp;&nbsp;'}, 
			    btnInsertMemberType,
			    '->',
			    btnPushBack,
			    {xtype : 'tbtext', text : '&nbsp;&nbsp;'},
			    btnLogOut
			]
		})
	});
	
	initMainView(null, centerPanel, null);
	getOperatorName(pin, '../../');
	 
	memberTypeWinInit();
	/*
	var menu = new Ext.menu.Menu({
		items : [{
			text : '新建会员类型',
			handler : function(){
				insertMemberTypeHandler();
			}
		}]
	 });
	Ext.getDoc().on('contextmenu', function(e){
		e.stopEvent();
		menu.showAt(e.getXY());
	});
	*/
});

