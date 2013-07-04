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
	initControl();
	
	var centerPanel = new Ext.Panel({
		title : '菜品原料配料',
		region : 'center',
		layout : 'border',
		frame : true,
		items : [kitchenTreeForSreach, {
			xtype:'panel',
			region:'center',
			layout : 'border',
			items : [foodBasicGrid, foodMaterialGrid]
		}, materialBasicGrid],
		tbar : new Ext.Toolbar({
			height : 55,
			items : ['->', btnGetBack, {
			    xtype : 'tbtext',
				text : '&nbsp;&nbsp;'
			}, btnLoginOut ]
		})
	});
	
	initMainView(null,centerPanel,null);
	getOperatorName(pin, "../../");
});