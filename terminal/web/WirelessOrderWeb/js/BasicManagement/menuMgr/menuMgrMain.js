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
		location.href = "BasicMgrProtal.html?"+ strEncode('restaurantID=' + restaurantID, 'mi');
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
		data : [[0, '全部'], [1, '编号'], [2, '名称'],/* [3, '拼音'],*/ [4, '价格']/*, [5, '厨房']*/, [6, '库存管理']]
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
			var stock = Ext.getCmp('comboStockStatusForSearch');
			
			var v = combo.getValue();
			if (v == 0) {
				// 全部
				ktCombo.setVisible(false);
				oCombo.setVisible(false);
				ct.setVisible(false);
				cn.setVisible(false);
				stock.setVisible(false);
				conditionType = '';
			} else if (v == 1 || v == 4) {
				// 编号 或 价格
				ktCombo.setVisible(false);
				oCombo.setVisible(true);
				cn.setVisible(true);
				ct.setVisible(false);
				stock.setVisible(false);
				oCombo.setValue(1);
				cn.setValue('');
				conditionType = cn.getId();
			} else if (v == 2 || v == 3) {
				// 名称 或 拼音
				ktCombo.setVisible(false);
				oCombo.setVisible(false);
				cn.setVisible(false);
				ct.setVisible(true);
				stock.setVisible(false);
				ct.setValue('');
				conditionType = ct.getId();
			} else if (v == 5) {
				// 厨房
				ktCombo.setVisible(true);
				oCombo.setVisible(false);
				cn.setVisible(false);
				ct.setVisible(false);
				stock.setVisible(false);
				ktCombo.store.loadData(kitchenData);
//				ktCombo.setValue(255);	
				conditionType = ktCombo.getId();
			} else if(v == 6){
				ktCombo.setVisible(false);
				oCombo.setVisible(false);
				cn.setVisible(false);
				ct.setVisible(false);
				stock.setVisible(true);
				stock = ktCombo.getId();
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
//			items : [kitchenTreeForSreach, menuGrid, displayInfoPanel]
			items : [kitchenTreeForSreach, menuGrid]
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
	
	initMainView(null, centerPanel, null);
	getOperatorName("../../");
	
	initFoodOperationWin();
	
	foodOperationWin.operation == mmObj.operation.select;
	foodOperationWin.render(document.body);
	var foWinTab = Ext.getCmp('foodOperationWinTab');
	foWinTab.setActiveTab('tasteOperationTab');
	foWinTab.setActiveTab('materialOperationTab');
	foWinTab.setActiveTab('combinationOperationTab');
	
});
