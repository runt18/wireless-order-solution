var btnAddTable = new Ext.ux.ImageButton({
	imgPath : '../../images/btnAddForBigBar.png',
	imgWidth : 50,
	imgHeight : 50,
	tooltip : '添加新餐台',
	handler : function(btn){
		insertTableBasicHandler();
	}
});

var btnGetBack = new Ext.ux.ImageButton({
	imgPath : '../../images/UserLogout.png',
	imgWidth : 50,
	imgHeight : 50,
	tooltip : '返回',
	handler : function(btn){
		location.href = 'BasicMgrProtal.html?restaurantID=' + restaurantID + '&pin=' + pin;
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
	initTree();
	//
	initGrid();
	
	var centerPanel = new Ext.Panel({
		title : '区域管理',
		region : 'center',
		frame : true,
		layout : 'border',
		items : [regionTree, tableBasicGrid],
		tbar : new Ext.Toolbar({
			height : 55,
			items : [btnAddTable, '->', btnGetBack, {
			    xtype : 'tbtext',
				text : '&nbsp;&nbsp;'
			}, btnLoginOut ]
		})
	});
	
	initMainView(null,centerPanel,null);
	getOperatorName(pin, "../../");
	//
	initWin();
});