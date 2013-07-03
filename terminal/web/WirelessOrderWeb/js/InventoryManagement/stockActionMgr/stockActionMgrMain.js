var btnAddStockOrder = new Ext.ux.ImageButton({
	imgPath : ' ',
	imgWidth : 50,
	imgHeight : 50,
	tooltip : '新建货单',
	handler : function(btn){
		insertStockActionHandler();
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
	loadData();
	//
	initControl();
	
	var centerPanel = new Ext.Panel({
		title : '库存任务管理',
		region : 'center',
		frame : true,
		layout : 'border',
		items : [stockBasicGrid],
		tbar : new Ext.Toolbar({
			height : 55,
			items : [btnAddStockOrder, '->', btnGetBack, {
			    xtype : 'tbtext',
				text : '&nbsp;&nbsp;'
			}, btnLoginOut ]
		})
	});
	
	initMainView(null,centerPanel,null);
	getOperatorName(pin, "../../");
	/*
	Ext.getDoc().on('contextmenu', function(e){
		e.stopEvent();
		var menu = new Ext.menu.Menu({
			items : [{
				text : '新建货单',
				handler : function(){
					btnAddStockOrder.handler();
				}
			}]
		});
		menu.showAt(e.getXY());
	});
	*/
	stockTaskNavWin.render(document.body);
	Ext.getCmp('comboDeptInForStockActionBasic').store.load();
	Ext.getCmp('comboSupplierForStockActionBasic').store.load();
	Ext.getCmp('comboDeptOutForStockActionBasic').store.load();
});