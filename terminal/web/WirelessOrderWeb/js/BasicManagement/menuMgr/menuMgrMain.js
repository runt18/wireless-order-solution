var btnAddFood = new Ext.ux.ImageButton({
	imgPath : '../../images/food_add.png',
	imgWidth : 50,
	imgHeight : 50,
	tooltip : '添加新菜',
	handler : function(btn) {
		foodOperation('basicOperationTab', mmObj.operation.insert);
	}
});

var btnDeleteFood = new Ext.ux.ImageButton({
	imgPath : '',
	imgWidth : 50,
	imgHeight : 50,
	tooltip : '删除菜品',
	handler : function(btn) {
		deleteFoodHandler();
	}
});

var btnFood = new Ext.ux.ImageButton({
	imgPath : '',
	imgWidth : 50,
	imgHeight : 50,
	tooltip : '修改菜品',
	handler : function(btn) {
		foodOperation('basicOperationTab', mmObj.operation.update);
	}
});

var btnTaste = new Ext.ux.ImageButton({
	imgPath : '../../images/taste_add.png',
	imgWidth : 50,
	imgHeight : 50,
	tooltip : '口味关联',
	handler : function(btn) {
		foodOperation('tasteOperationTab', mmObj.operation.update);
	}
});

var btnMaterial = new Ext.ux.ImageButton({
	imgPath : '../../images/material_add.png',
	imgWidth : 50,
	imgHeight : 50,
	tooltip : '食材关联',
	handler : function(btn) {
		foodOperation('materialOperationTab', mmObj.operation.update);
	}
});

var btnCombination = new Ext.ux.ImageButton({
	imgPath : '../../images/combination_add.png',
	imgWidth : 50,
	imgHeight : 50,
	tooltip : '关联套菜',
	handler : function(btn) {
		foodOperation('combinationOperationTab', mmObj.operation.update);
	}
});

var pushBackBut = new Ext.ux.ImageButton({
	imgPath : '../../images/UserLogout.png',
	imgWidth : 50,
	imgHeight : 50,
	tooltip : '返回',
	handler : function(btn) {
		location.href = "BasicMgrProtal.html?restaurantID=" + restaurantID
				+ "&isNewAccess=false&pin=" + pin;
	}
});

var logOutBut = new Ext.ux.ImageButton({
	imgPath : '../../images/ResLogout.png',
	imgWidth : 48,
	imgHeight : 48,
	tooltip : '登出',
	handler : function(btn) {
	}
});

var filterTypeComb = new Ext.form.ComboBox({
	forceSelection : true,
	readOnly : true,
	width : 100,
	value : '全部',
	id : 'filter',
	store : new Ext.data.SimpleStore({
		fields : [ 'value', 'text' ],
		data : [[0, '全部'], [1, '编号'], [2, '名称'], [3, '拼音'], [4, '价格']/*, [5, '厨房']*/]
	}),
	valueField : 'value',
	displayField : 'text',
	typeAhead : true,
	mode : 'local',
	triggerAction : 'all',
	selectOnFocus : true,
	allowBlank : false,
	listeners : {
		select : function(combo, record, index) {
			var ktCombo = Ext.getCmp('kitchenTypeComb');
			var oCombo = Ext.getCmp('comboOperatorForGridSearch');
			var ct = Ext.getCmp('textfieldForGridSearch');
			var cn = Ext.getCmp('numfieldForGridSearch');
			
			if (index == 0) {
				// 全部
				ktCombo.setVisible(false);
				oCombo.setVisible(false);
				ct.setVisible(false);
				cn.setVisible(false);
				conditionType = '';
			} else if (index == 1 || index == 4) {
				// 编号 或 价格
				ktCombo.setVisible(false);
				oCombo.setVisible(true);
				cn.setVisible(true);
				ct.setVisible(false);
				oCombo.setValue(1);
				cn.setValue('');
				conditionType = cn.getId();
			} else if (index == 2 || index == 3) {
				// 名称 或 拼音
				ktCombo.setVisible(false);
				oCombo.setVisible(false);
				cn.setVisible(false);
				ct.setVisible(true);
				ct.setValue('');
				conditionType = ct.getId();
			} else if (index == 5) {
				// 厨房
				ktCombo.setVisible(true);
				oCombo.setVisible(false);
				cn.setVisible(false);
				ct.setVisible(false);
				ktCombo.store.loadData(kitchenData);
				ktCombo.setValue(255);	
				conditionType = ktCombo.getId();
			}
		}
	}
});

function deleteFoodHandler() {
	Ext.MessageBox.show({
		msg : '<center>是否确定删除？</center>',
		width : 200,
		buttons : Ext.MessageBox.YESNO,
		fn : function(btn) {
			if (btn == 'yes') {
				var selData = Ext.ux.getSelData('menuMgrGrid');
				Ext.Ajax.request({
					url : '../../DeleteMenu.do',
					params : {
						pin : pin,
						foodID : selData.id,
						restaurantID : restaurantID
					},
					success : function(response, options) {
						var jr = Ext.util.JSON.decode(response.responseText);
						if (eval(jr.success) == true) {
							Ext.example.msg('提示', '菜品(<font color="red">' + selData.name + '</font>)' + jr.msg);
							searchMenuHandler();
							Ext.getCmp('menuMgrGrid').getSelectionModel().clearSelections();
							Ext.getCmp('menuMgrGrid').fireEvent('rowclick');
						} else {
							Ext.ux.showMsg(jr);
						}
					},
					failure : function(response, options) {
						var jr = Ext.util.JSON.decode(response.responseText);
						Ext.ux.showMsg(jr);
					}
				});
			}
		}
	});
};

var tasteGrid = createGridPanel(
	'tasteGrid',
	'',
	'',
	'',
	'../../QueryFoodTaste.do',
	[
	    [true, false, false, false], 
	    ['口味', 'tasteName', 100] , 
	    ['价格', 'tastePrice', 60, 'right','Ext.ux.txtFormat.gridDou'], 
	    ['比例', 'tasteRate', 60, 'right','Ext.ux.txtFormat.gridDou'], 
	    ['计算方式', 'tasteCalcFormat']
	],
	['tasteID', 'tasteAlias','tasteName','tastePrice','tasteRate', 'tasteCategory', 'tasteCalc', 'tasteCalcFormat', 'foodID', 'foodName'],
	[['pin', pin], ['restaurantID', restaurantID], ['foodID', 0]],
	0
);
tasteGrid.frame = false;
tasteGrid.getStore().on('beforeload', function(){
	var selData = Ext.ux.getSelData('menuMgrGrid');
	this.baseParams['foodID'] = selData.id;
	this.baseParams['pin'] = pin;
	this.baseParams['restaurantID'] = restaurantID;
});

var materialGrid = createGridPanel(
	'materialGrid',
	'',
	'',
	'',
	'../../QueryFoodMaterial.do',
	[
	    [true, false, false, false], 
//		['编号', 'materialAliasID', 60], 
		['食材', 'materialName', 200], 
		['价格' , 'price', '', 'right', 'Ext.ux.txtFormat.gridDou'],
		['消耗', 'consumption', '', 'right', 'Ext.ux.txtFormat.gridDou']
	],
	['materialID', 'materialAliasID', 'materialName', 'consumption', 'cateID', 'cateName', 'price'],
	[['pin', pin], ['resturantID', restaurantID], ['foodID', 0]],
	0
);
materialGrid.frame = false;
materialGrid.getStore().on('beforeload', function(){
	var selData = Ext.ux.getSelData('menuMgrGrid');
	this.baseParams['foodID'] = selData.id;
});

var combinationGrid = createGridPanel(
	'combinationGrid',
	'',
	'',
	'',
	'../../QueryFoodCombination.do',
	[
	    [true, false, false, false], 
		['菜名', 'foodName', 200], 
		['价格', 'unitPrice', '', 'right', 'Ext.ux.txtFormat.gridDou'], 
		['份数', 'amount', '', 'right', 'Ext.ux.txtFormat.gridDou']
	],
	['foodID', 'aliasID', 'foodName', 'unitPrice', 'amount', 'kitchenID', 'kitchenName'],
	[['pin', pin], ['resturantID', restaurantID], ['foodID', 0]],
	0
);
combinationGrid.frame = false;
combinationGrid.getStore().on('beforeload', function(){
	var selData = Ext.ux.getSelData('menuMgrGrid');
	this.baseParams['foodID'] = selData.foodID;
	this.baseParams['pin'] = pin;
	this.baseParams['restaurantID'] = restaurantID;
});

var displayInfoPanel = new Ext.Panel({
	region : 'east',
	title : '&nbsp;',
	layout : 'fit',
	collapsed : true,
	collapsible : true,
	titleCollapse : true,
	frame : true,
	width : 350,
	items : [new Ext.TabPanel({
		id : 'displayInfoPanelTab',
		activeTab: 0,
		tabPosition : 'bottom',
		defaults : {
			xtype : 'panel',
			border : false,
	    	layout : 'fit'
		},
		items : [{
			id : 'tasteGridTab',
	    	title : '已关联口味',
	    	items : [tasteGrid]
	    },/*{
		    	id : 'materialGridTab',
		    	title : '已关联食材',
		    	items : [materialGrid]
		 }, */ {
	    	id : 'combinationGridTab',
		    title : '已关联套菜',
		    items : [combinationGrid]
	    }],
		listeners : {
			tabchange : function(e, p){
				refreshInfoGrid(p);				
			}
		}
	})]
});

function setButtonStateOne(s){
	if(typeof s != 'boolean'){
		return;
	}
	Ext.getCmp('btnPreviousFood').setDisabled(s);
	Ext.getCmp('btnNextFood').setDisabled(s);
	Ext.getCmp('btnAppForOW').setDisabled(s);
	Ext.getCmp('btnSaveForOW').setDisabled(s);
	Ext.getCmp('btnCloseForOW').setDisabled(s);
	Ext.getCmp('btnRefreshForOW').setDisabled(s);
};

Ext.onReady(function() {
	initKitchenTreeForSreach();
	
	initMenuGrid();
	
	var centerPanel = new Ext.Panel({
		region : 'center',
		layout : 'fit',
		frame : true,
		title : '菜品管理',
		items : [ {
			layout : 'border',
			items : [kitchenTreeForSreach, menuGrid, displayInfoPanel]
		} ],
		tbar : new Ext.Toolbar({
			height : 55,
			items : [btnAddFood, { 
				xtype:'tbtext', 
				text : '&nbsp;&nbsp;&nbsp;&nbsp;' 
			}, btnTaste, { 
				xtype:'tbtext',
				text : '&nbsp;&nbsp;&nbsp;&nbsp;'
			}, btnCombination, '->', pushBackBut, { 
				xtype:'tbtext', 
				text : '&nbsp;&nbsp;&nbsp;&nbsp;'
			}, logOutBut ]
		}),
		keys : [{
			key : Ext.EventObject.ENTER,
			scope : this,
			fn : function(){	
				Ext.getCmp('srchBtn').handler(); 
			}
		}]
	});
	
	// 初始化页面主框架
	initMainView(null, centerPanel, null, pin);
	// update the operator name
	getOperatorName(pin, "../../");
	
	initFoodOperationWin();
	
	foodOperationWin.operation == mmObj.operation.select;
	foodOperationWin.render(document.body);
	var foWinTab = Ext.getCmp('foodOperationWinTab');
	foWinTab.setActiveTab('tasteOperationTab');
	foWinTab.setActiveTab('materialOperationTab');
	foWinTab.setActiveTab('combinationOperationTab');
	
});
