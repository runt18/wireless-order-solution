var btnInsertMemberType = new Ext.ux.ImageButton({
	imgPath : ' ',
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

insertMemberTypeHandler = function(){
	memberTypeOperationHandler({
		type : mtObj.operation['insert']
	});
};

updateMemberTypeHandler = function(){
	memberTypeOperationHandler({
		type : mtObj.operation['update']
	});
};

deleteMemberTypeHandler = function(){
	memberTypeOperationHandler({
		type : mtObj.operation['delete']
	});
};

memberTypeOperationHandler = function(c){
	if(c == null || typeof c == 'undefined' || typeof c.type == 'undefined'){
		return;
	}
	memberTypeWin.otype = c.type;
	
	if(c.type == mtObj.operation['insert']){
		
		memberTypeWin.setTitle('添加会员类型');
		memberTypeWin.show();
		memberTypeWin.center();
		
		bindMemberTypeData({});
		
	}else if(c.type == mtObj.operation['update']){
		var sd = Ext.ux.getSelData(memberTypeGrid.getId());
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
						url : '../../DeleteMemberType.do',
						params : {
							restaurantID : restaurantID,
							pin : pin,
							typeID : sd['typeID'],
							discountType : sd['discountType'],
							discountID : sd['discountID']
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

bindMemberTypeData = function(d){
	var typeID = Ext.getCmp('numTypeID');
	var typeName = Ext.getCmp('txtTypeName');
	var chargeRate = Ext.getCmp('numChargeRate');
	var exchangeRate = Ext.getCmp('numExchangeRate');
	var discountType = Ext.getCmp('comboDiscountType');
	var discount = Ext.getCmp('comboDiscount');
	var discountRate = Ext.getCmp('numDiscountRate');
	var attribute = Ext.getCmp('comboAttribute');
	
	typeID.setValue(d['typeID']);
	typeName.setValue(d['name']);
	chargeRate.setValue(d['chargeRate']);
	exchangeRate.setValue(d['exchangeRate']);
	attribute.setValue(d['attribute']);
	discountType.setValue(d['discountType']);
	
	if(d['discountType'] == 0){
		discountRate.setValue();
		discountRate.setDisabled(true);
		discount.setValue(d['discountID']);
		discount.setDisabled(false);
	}else if(d['discountType'] == 1){
		discountRate.setValue(d['discountRate']);
		discountRate.setDisabled(false);
		discount.setValue();
		discount.setDisabled(true);
	}else{
		discountRate.setValue();
		discount.setValue();
	}
	
	typeID.clearInvalid();
	typeName.clearInvalid();
	chargeRate.clearInvalid();
	exchangeRate.clearInvalid();
	attribute.clearInvalid();
	discountType.clearInvalid();
	discountRate.clearInvalid();
	discount.clearInvalid();
};

/**********************************************************************/
discountTypeRenderer = function(val){
	for(var i = 0; i < discountTypeData.length; i++){
		if(eval(discountTypeData[i][0] == val)){
			return discountTypeData[i][1];
		}
	}
};

discountRateRenderer = function(val, m, r){
	if(r.get('discountType') == 0){
		return '--';
	}else{
		return Ext.ux.txtFormat.gridDou(val);
	}
};

discountRenderer = function(val, m, r){
	if(r.get('discountType') == 1){
		return '--';
	}else{
		for(var i = 0; i < discountData.length; i++){
			if(discountData[i].discountID == val){
				return discountData[i].discountName;
				break;
			}
		}
	}
};

memberAttributeRenderer = function(val){
	for(var i = 0; i < memberAttributData.length; i++){
		if(eval(memberAttributData[i][0] == val)){
			return memberAttributData[i][1];
		}
	}
};

memberTypeRenderer = function(){
	return ''
		   + '<a href="javascript:updateMemberTypeHandler()">修改</a>'
		   + '&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;'
		   + '<a href="javascript:deleteMemberTypeHandler()">删除</a>';
};

var memberTypeGridTbar = new Ext.Toolbar({
	items : [{
		xtype: 'tbtext',
		text : '过滤:'
	}, {
		xtype : 'combo',
		id : 'comboSearchType',
		readOnly : true,
		forceSelection : true,
		width : 80,
		value : 0,
		store : new Ext.data.SimpleStore({
			fields : [ 'value', 'text' ],
			data : [[0, '全部'], [1, '类型名称'], [2, '折扣方式']]
		}),
		valueField : 'value',
		displayField : 'text',
		typeAhead : true,
		mode : 'local',
		triggerAction : 'all',
		selectOnFocus : true,
		listeners : {
			select : function(thiz, record, index){
				
			}
		}
	}, '->', {
		text : '搜索',
		id : 'btnSearchMemberType',
		iconCls : 'btn_search',
		handler : function(e){
			var gs = memberTypeGrid.getStore();
			gs.load({
				params : {
					start : 0,
					limit : 30
				}
			});
		}
	}, {
		text : '添加',
		id : 'btnInsertMemberType',
		iconCls : 'btn_add',
		handler : function(e){
			insertMemberTypeHandler();
		}
	}, {
		text : '修改',
		id : 'btnUpdateMemberType',
		iconCls : 'btn_edit',
		handler : function(e){
			updateMemberTypeHandler();
		}
	}, {
		text : '删除',
		id : 'btnDeleteMemberType',
		iconCls : 'btn_delete',
		handler : function(e){
			deleteMemberTypeHandler();
		}
	}]
});

var memberTypeGrid = createGridPanel(
	'memberTypeGrid',
	'',
	'',
	'',
	'../../QueryMemberType.do',
	[
		[true, false, false, false], 
		['类型编号', 'typeID'],
		['类型名称', 'name'],
		['充值比率', 'chargeRate',,'right', 'Ext.ux.txtFormat.gridDou'],
		['积分比率', 'exchangeRate',,'right', 'Ext.ux.txtFormat.gridDou'],
		['折扣方式', 'discountType',,, 'discountTypeRenderer'],
		['折扣率', 'discountRate',,'right', 'discountRateRenderer'],
		['折扣方案', 'discountID',,, 'discountRenderer'],
		['会员属性', 'attribute',,, 'memberAttributeRenderer'],
		['操作', 'operation', 200, 'center', 'memberTypeRenderer']
	],
	['typeID','name','chargeRate','exchangeRate','discountID','discountType','discountRate','attribute'],
	[['pin',pin], ['isPaging', true], ['restaurantID', restaurantID]],
	30,
	'',
	memberTypeGridTbar
);	
memberTypeGrid.region = 'center';
memberTypeGrid.on('render', function(thiz){
	thiz.getStore().reload();
});

/**********************************************************************/
var memberTypeWin;
Ext.onReady(function(){
	dataInit();
	
	Ext.lib.Ajax.defaultPostHeader += '; charset=utf-8';
	Ext.QuickTips.init();

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
			    {xtype : 'tbtext', text : '&nbsp;&nbsp;'},
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
	 
	 memberTypeInit();
});

