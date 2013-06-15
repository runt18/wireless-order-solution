var btnInsertPricePlan = new Ext.ux.ImageButton({
	imgPath : '../../images/btnAddForBigBar.png',
	imgWidth : 50,
	imgHeight : 50,
	tooltip : '添加方案',
	handler : function(btn){
		insertPricePlanWinHandler();
	}
});

var pushBackBut = new Ext.ux.ImageButton({
	imgPath : '../../images/UserLogout.png',
	imgWidth : 50,
	imgHeight : 50,
	tooltip : '返回',
	handler : function(btn){
		location.href = 'BasicMgrProtal.html?restaurantID=' + restaurantID + '&pin=' + pin;
	}
});

var logOutBut = new Ext.ux.ImageButton({
	imgPath : '../../images/ResLogout.png',
	imgWidth : 50,
	imgHeight : 50,
	tooltip : '登出',
	handler : function(btn){
		
	}
});

/**********************************************************************/
insertPricePlanWinHandler = function(){
	pricePlanOperationHandler({
		type : pmObj.operation['insert']
	});
};

updatePricePlanWinHandler = function(){
	pricePlanOperationHandler({
		type : pmObj.operation['update']
	});
};

deletePricePlanWinHandler = function(){
	pricePlanOperationHandler({
		type : pmObj.operation['delete']
	});
};

function pricePlanOperationHandler(c){
	if(c == null || typeof c == 'undefined' || typeof c.type == 'undefined'){
		return;
	}
	oPricePlanWin.otype = c.type;
	
	var copyID = Ext.getCmp('comboCopyPricePlan');
	
	if(c.type == pmObj.operation['insert']){
		operationPricePlanData({ 
			type : pmObj.operation['set'] 
		});
		Ext.query('div label[for='+copyID.getId()+']')[0].parentElement.setAttribute('style', "display:block");
		
		oPricePlanWin.setTitle("添加方案");
		oPricePlanWin.show();
		oPricePlanWin.center();
	}else if(c.type == pmObj.operation['update']){
		var sn = pricePlanTree.getSelectionModel().getSelectedNode();
		if(!sn || sn.attributes.pricePlanID == -1){
			Ext.example.msg('提示', '请选中一个方案再进行操作.');
			return;
		}
		operationPricePlanData({ 
			type : pmObj.operation['set'],
			data : {
				id : sn.attributes['pricePlanID'],
				name : sn.attributes['pricePlanName'],
				statusValue : sn.attributes['statusValue']
			}
		});
		
		Ext.query('div label[for='+copyID.getId()+']')[0].parentElement.setAttribute('style', "display:none");
		
		oPricePlanWin.setTitle("修改方案");
		oPricePlanWin.show();
		oPricePlanWin.center();
	}else if(c.type == pmObj.operation['delete']){
		var sn = pricePlanTree.getSelectionModel().getSelectedNode();
		if(!sn || sn.attributes['pricePlanID'] == -1){
			Ext.example.msg('提示', '请选中一个方案再进行操作.');
			return;
		}
		if(sn.attributes['status'] == 1){
			Ext.example.msg('提示', '该价格方案为活动状态, 正在使用中的不允许删除.');
			return;
		}
		Ext.Msg.show({
			title : '提示',
			msg : '是否删除方案:<font color="#FF0000">'+sn.text+'</font>?<br/>重要:同时删除该方案下所有菜品价格信息.',
			icon : Ext.Msg.QUESTION,
			buttons : Ext.Msg.YESNO,
			fn : function(e){
				if(e == 'yes'){
					Ext.Ajax.request({
						url : '../../DeletePricePlan.do',
						params : {
							restaurantID : restaurantID,
							id : sn.attributes['pricePlanID']
						},
						success : function(res, opt){
							var jr = Ext.util.JSON.decode(res.responseText);
							if(jr.success){
								Ext.example.msg(jr.title, jr.msg);
								Ext.getCmp('btnRefreshPricePlanTree').handler();
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

function operationPricePlanData(c){
	if(c == null || c.type == null || typeof c.type == 'undefined')
		return;
	var data = {};
	var id = Ext.getCmp('txtPricePlanID');
	var name = Ext.getCmp('txtPricePlanName');
	var copyID = Ext.getCmp('comboCopyPricePlan');
	var status = Ext.getCmp('comboPricePlanStatus');
	if(c.type == pmObj.operation['set']){
		data = c.data == null || typeof c.data == 'undefined' ? {} : c.data;
		id.setValue(data['id']);
		name.setValue(data['name']);
		copyID.setValue();
		status.setValue(typeof data['statusValue'] == 'undefined' ? 0 : data['statusValue']);
	}else if(c.type == pmObj.operation['get']){
		data = {
			restaurantID : restaurantID,
			name : name.getValue(),
			id : id.getValue(),
			statusValue : status.getValue(),
			copyID : copyID.getRawValue() == '' ? null : copyID.getValue()
		};
		c.data = data;
	}
	name.clearInvalid();
	status.clearInvalid();
	return c;
};

/**********************************************************************/
function updateFoodPricePlanWinHandler(){
	foodPricePlanOperationHandler({
		type : pmObj.operation['update']
	});
};

function foodPricePlanOperationHandler(c){
	if(c == null || typeof c == 'undefined' || typeof c.type == 'undefined'){
		return;
	}
	oPriceBasicWin.otype = c.type;
	
	if(c.type == pmObj.operation['update']){
		var sd = Ext.ux.getSelData(priceBaiscGrid);
		if(!sd){
			Ext.example.msg('提示', '请选中一个菜品再进行操作.');
			return;
		}
		operationFoodPricePlanData({
			type : pmObj.operation['set'],
			data : sd
		});
		oPriceBasicWin.setTitle('修改菜品价格');
		oPriceBasicWin.show();
		oPriceBasicWin.center();
	}else{
		Ext.example.msg('错误', '未知操作类型, 请联系管理员');
	}
};

function operationFoodPricePlanData(c){
	if(c == null || c.type == null || typeof c.type == 'undefined')
		return;
	var data = {};
	var foodID = Ext.getCmp('hideFoodPricePlanID');
	var pricePlanID = Ext.getCmp('hidePricePlanID');
	var pricePlanName = Ext.getCmp('txtFoodPricePlanName');
	var foodName = Ext.getCmp('txtFoodName');
	var unitPrice = Ext.getCmp('numFoodUnitPrice');
	if(c.type == pmObj.operation['set']){
		data = c.data == null || typeof c.data == 'undefined' ? {} : c.data;
		foodID.setValue(data['foodId']);
		pricePlanID.setValue(data['planId']);
		pricePlanName.setValue(data['pricePlan.name']);
		foodName.setValue(data['foodName']);
		unitPrice.setValue(data['unitPrice']);
	}else if(c.type == pmObj.operation['get']){
		data = {
			restaurantID : restaurantID,
			planId : pricePlanID.getValue(),
			foodId : foodID.getValue(),
			unitPrice : unitPrice.getValue()
		};
		c.data = data;
	}
	unitPrice.clearInvalid();
	return c;
};

/**********************************************************************/
Ext.onReady(function(){
	initData();
	initTree();
	initGrid();
	
	var centerPanel = new Ext.Panel({
		title : '菜品价格方案管理',
		region : 'center',
		layout : 'border',
		frame : true,
		items : [pricePlanTree, priceBaiscGrid],
		tbar : new Ext.Toolbar({
			height : 55,
			items : [
			    {xtype:'tbtext',text:'&nbsp;&nbsp;'},
//			    btnAddProgram,
//			    {xtype:'tbtext',text:'&nbsp;&nbsp;'},
			    btnInsertPricePlan,
			    {xtype:'tbtext',text:'&nbsp;&nbsp;'},
			    '->', 
			    pushBackBut, 
			    {
					text : '&nbsp;&nbsp;&nbsp;',
					disabled : true
				}, 
				logOutBut 
			]
		})
	});
	
	initMainView(null, centerPanel, null);
	getOperatorName(pin, "../../");
	
	// 
	initWin();
	
});