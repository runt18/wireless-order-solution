
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

var materialCategoryTree;
var materialBasicGrid;
Ext.onReady(function(){
	materialCategoryTree = new Ext.Panel({
		title : '原料类别信息',
		region : 'west',
		width : 200,
		frame : true,
		tbar : ['->', {
			text : 'asdasd'
		}]
	});
	
	materialBasicGrid = new Ext.Panel({
		title : '原料基础信息',
		region : 'center',
		frame : true,
		tbar : ['->', {
			text : 'asdasd'
		}]
	});
	var centerPanel = new Ext.Panel({
		title : '原料资料管理',
		region : 'center',
		layout : 'border',
		frame : true,
		items : [materialCategoryTree, materialBasicGrid],
		tbar : new Ext.Toolbar({
			height : 55,
			items : ['->', btnGetBack, {
			    xtype : 'tbtext',
				text : '&nbsp;&nbsp;'
			}, btnLoginOut ]
		})
	});
	
	new Ext.Viewport({
		layout : 'border',
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
			frame : true,
			html : '<div style="font-size:11pt; text-align:center;"><b>版权所有(c) 2011 智易科技</b></div>'
		} ]
	});
	
});