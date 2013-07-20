var btnAddStockTake = new Ext.ux.ImageButton({
	imgPath : '../../images/btnAddStockTake.png',
	imgWidth : 50,
	imgHeight : 50,
	tooltip : '新建盘点任务',
	handler : function(btn){
		insertStockTakeHandler();
	}
});

var btnGetBack = new Ext.ux.ImageButton({
	imgPath : '../../images/UserLogout.png',
	imgWidth : 50,
	imgHeight : 50,
	tooltip : '返回',
	handler : function(btn){
		location.href = 'InventoryProtal.html?restaurantID=' + restaurantID + '&pin=' + pin;
	}
});

var btnLoginOut = new Ext.ux.ImageButton({
	imgPath : '../../images/ResLogout.png',
	imgWidth : 50,
	imgHeight : 50,
	tooltip : '登出',
	handler : function(btn){
		
	}
});

Ext.onReady(function(){
	//
	initGrid();
	
	var centerPanel = new Ext.Panel({
		title : '盘点任务管理',
		region : 'center',
		frame : true,
		layout : 'border',
		items : [stockTakeGrid],
		tbar : new Ext.Toolbar({
			height : 55,
			items : [btnAddStockTake, '->', btnGetBack, {
			    xtype : 'tbtext',
				text : '&nbsp;&nbsp;'
			}, btnLoginOut ]
		})
	});
	
	initMainView(null,centerPanel,null);
	getOperatorName(pin, "../../");
	//
	initWin();
	Ext.getCmp('comboStockTakeDept').store.load();
	Ext.getCmp('comboMaterialCateId').store.load();
	//
	initDetailActualAmountMenu();
});