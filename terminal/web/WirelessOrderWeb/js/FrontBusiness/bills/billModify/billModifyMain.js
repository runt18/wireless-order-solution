var allFoodTabPanelGridTbar = new Ext.Toolbar({
	items : [ {
		xtype:'tbtext', 
		text:'过滤:'
	}, {
		xtype : 'combo',
		id : 'comSearchType',
	    	width : 70,
	    	listWidth : 70,
	    	store : new Ext.data.JsonStore({
			fields : ['type', 'name'],
			data : [{
				type : 0,
				name : '分厨'
			}, {
				type : 1,
				name : '菜名'
			}, {
				type : 2,
				name : '拼音'
			}, {
				type : 3,
				name : '编号'
			}]
		}),
		valueField : 'type',
		displayField : 'name',
		value : 0,
		mode : 'local',
		triggerAction : 'all',
		typeAhead : true,
		selectOnFocus : true,
		forceSelection : true,
		readOnly : true,
		listeners : {
			render : function(thiz){
				Ext.getCmp('comSearchType').fireEvent('select', thiz, null, 0);
			},
			select : function(thiz, r, index){
				var kitchen = Ext.getCmp('comSearchKitchen');
				var foodName = Ext.getCmp('txtSearchFoodName');
				var pinyin = Ext.getCmp('txtSearchPinyin');
				var foodAliasID = Ext.getCmp('txtSearchFoodAliasID');
				if(index == 0){
					kitchen.setVisible(true);
					foodName.setVisible(false);
					pinyin.setVisible(false);
					foodAliasID.setVisible(false);
					kitchen.setValue(254);
					bmObject.searchField = kitchen.getId();
				}else if(index == 1){
					kitchen.setVisible(false);
					foodName.setVisible(true);
					pinyin.setVisible(false);
					foodAliasID.setVisible(false);
					foodName.setValue();
					bmObject.searchField = foodName.getId();
				}else if(index == 2){
					kitchen.setVisible(false);
					foodName.setVisible(false);
					pinyin.setVisible(true);
					foodAliasID.setVisible(false);
					pinyin.setValue();
					bmObject.searchField = pinyin.getId();
				}else if(index == 3){
					kitchen.setVisible(false);
					foodName.setVisible(false);
					pinyin.setVisible(false);
					foodAliasID.setVisible(true);
					foodAliasID.setValue();
					bmObject.searchField = foodAliasID.getId();
				}
			}
		}
	}, {
		xtype:'tbtext', 
		text:'&nbsp;&nbsp;'
	}, new Ext.form.ComboBox({
		xtype : 'combo',
		id : 'comSearchKitchen',
	    	width : 100,
	    	listWidth : 100,
	    	store : new Ext.data.JsonStore({
			fields : [ 'kitchenAliasID', 'kitchenName' ],
			data : [{
				kitchenAliasID : 254,
		    	kitchenName : '全部'
			}, {
				kitchenAliasID : 255,
		    	kitchenName : '空'
			}]
		}),
		valueField : 'kitchenAliasID',
		displayField : 'kitchenName',
		value : 254,
		mode : 'local',
		triggerAction : 'all',
		typeAhead : true,
		selectOnFocus : true,
		forceSelection : true,
		readOnly : true,
		hidden : true,
		listeners : {
			render : function(thiz){
				Ext.Ajax.request({
					url : '../../QueryMenu.do',
					params : {
						restaurantID : restaurantID,
						type : 3
					},
					success : function(response, options){
						var jr = Ext.util.JSON.decode(response.responseText);
						var root = jr.root;
						thiz.store.loadData(root, true);
					}
				});
			},
			select : function(thiz, r, index){
				
			}
		}
	}), new Ext.form.TextField({
		xtype : 'textfield',
		id : 'txtSearchFoodName',
		hidden : true,
		width : 100
	}), new Ext.form.TextField({
		xtype : 'textfield',
		id : 'txtSearchPinyin',
		hidden : true,
		width : 100
	}), new Ext.form.TextField({
		xtype : 'textfield',
		id : 'txtSearchFoodAliasID',
		hidden : true,
		width : 100
	}),
	'->',
	{
		text : '重置',
		iconCls : 'btn_refresh',
		handler : function(){
			var st = Ext.getCmp('comSearchType');
			st.setValue(0);
			st.fireEvent('select', null, null, 0);
			Ext.getCmp('btnSearchMenu').handler();
		}
	}, {
		text : '搜索',
		id : 'btnSearchMenu',
		iconCls : 'btn_search',
		handler : function(){
			allFoodTabPanelGrid.getStore().load({
				params : {
					start : 0,
					limit : 30
				}
			});
		}
	}]
});

var allFoodTabPanelGrid = createGridPanel(
	'allFoodTabPanelGrid',
	'',
	'',
	'',
	'../../QueryMenu.do',
	[
	    [true, false, true, true], 
	    ['菜名', 'displayFoodName', 200], 
	    ['编号', 'alias', 70] , 
		['拼音', 'pinyin', 70], 
		['价格', 'unitPrice', 70, 'right', 'Ext.ux.txtFormat.gridDou']
	],
	FoodBasicRecord.getKeys(),
	[['pin',pin], ['type', 1], ['restaurantID', restaurantID], ['isPaging', true]],
	30,
	'',
	allFoodTabPanelGridTbar
);
allFoodTabPanelGrid.getBottomToolbar().displayMsg = '每页&nbsp;30&nbsp;条,共&nbsp;{2}&nbsp;条记录';
allFoodTabPanelGrid.getStore().on('beforeload', function(thiz, records){
	var searchType = Ext.getCmp('comSearchType');
	var searchValue = Ext.getCmp(bmObject.searchField);
	this.baseParams['searchType'] = typeof(searchType) != 'undefined' ? searchType.getValue() : '';
	this.baseParams['searchValue'] = typeof(searchValue) != 'undefined' ? searchValue.getValue() : '';
});
allFoodTabPanelGrid.frame = false;
allFoodTabPanelGrid.border = false;
allFoodTabPanelGrid.getStore().on('load', function(thiz, records){
	for(var i = 0; i < records.length; i++){
		Ext.ux.formatFoodName(records[i], 'displayFoodName', 'name');
	}
});
allFoodTabPanelGrid.on('rowdblclick', function(thiz, ri, e){
	bindGridData({
		grid : orderedGrid,
		record : thiz.getStore().getAt(ri)
	});
//	billListRefresh();
});

var dishesOrderEastPanel = new Ext.Panel({
	title : '菜单',
	region : 'east',
	width : 450,
	frame : true,
	layout : 'fit',
	id : 'dishesOrderEastPanel',
	items : [
		new Ext.TabPanel({
			activeTab : 0,
			items : [{
				title : '&nbsp;所有菜&nbsp;',
				xtype : 'panel',
				layout : 'fit',
				items : [allFoodTabPanelGrid]
			}]
		})
	]
});

var billGenModForm = new Ext.Panel({
	region : 'north',
	height : 62,
	frame : true,
	layout : 'column',
	defaults : {
		xtype : 'panel',
		layout : 'column',
		columnWidth : 1,
		defaults : {
			xtype : 'form',
			layout : 'form',
			labelWidth : 60,
			width : 220,
			defaults : {
				width : 130
			}
		}
	},
	items : [{
		items : [{
			items : [{
				xtype : 'textfield',
				id : 'txtSettleTypeFormat',
				fieldLabel : '结账方式',
				value : '一般/会员',
				disabled : true
			}]
		}, {
			items : [{
				xtype : 'combo',
				id : 'comboDiscount',
//				width : 100,
				fieldLabel : '折扣方案',
				readOnly : true,
				forceSelection : true,
				store : new Ext.data.JsonStore({
					root : 'root',
					fields : [ 'discountID', 'discountName']
				}),
				valueField : 'discountID',
				displayField : 'discountName',
				typeAhead : true,
				mode : 'local',
				triggerAction : 'all',
				selectOnFocus : true,
				listeners : {
					select : function(combo, record, index) {
						billListRefresh();
					}
				}
			}]
		}, {
			width : 135,
			items : [{
				xtype : 'numberfield',
				id : 'numErasePrice',
				fieldLabel : '抹数金额',
				width : 60,
				minValue : 0,
				value : 0
			}]
		}, {
			xtype : 'panel',
			width : 150,
			id : 'panelShowEraseQuota',
			style : 'font-size:18px;',
			html : '上限:￥<font id="fontShowEraseQuota" style="color:red;">0.00</font>'
		}, {
			items : [{
				xtype : 'numberfield',
				width : 60,
				fieldLabel : '服务费',
				id : 'serviceRate',
				allowBlank : false,
				validator : function(v) {
					if (v < 0 || v > 100 || v.indexOf('.') != -1) {
						return '服务费率范围是0%至100%,且为整数.';
					} else {
						return true;
					}
				}
			}]
		}]
	}, {
		defaults : {
			labelWidth : 1,
			labelSeparator : ' ',
		},
		items : [{
			xtype : 'label',
			width : 65,
			text : '收款方式:'
		}, {
			width : 80,
			items : [{
				xtype : 'radio',
				name : 'radioPayType',
				boxLabel : '现金结账',
				inputValue : '1'
			}]
		}, {
			width : 80,
			items : [{
				xtype : 'radio',
				name : 'radioPayType',
				boxLabel : '刷卡结账',
				inputValue : '2'
			}]
		}, {
			width : 80,
			items : [{
				xtype : 'radio',
				name : 'radioPayType',
				boxLabel : '会员消费',
				inputValue : '3',
				disabled : true
			}]
		}, {
			width : 60,
			items : [{
				xtype : 'radio',
				name : 'radioPayType',
				boxLabel : '签单',
				inputValue : '4'
			}]
		}, {
			width : 75,
			items : [{
				xtype : 'radio',
				name : 'radioPayType',
				boxLabel : '挂账',
				inputValue : '5'
			}]
		}, {
			xtype : 'form',
			labelWidth : 60,
			labelSeparator : ':',
			items : [{
				xtype : 'textfield',
				id : 'remark',
				fieldLabel : '备注',
				width : 420
			}]
		}]
	}]
});

Ext.onReady(function(){
	billModifyOnLoad();
	
	var centerPanelDO = new Ext.Panel({
		id : "centerPanelDO",
		region : "center",
		border : false,
		layout : "border",
		items : [ orderedGrid, dishesOrderEastPanel, billGenModForm ]
	});
	
	var billModCenterPanel = new Ext.Panel({
		id : "billModCenterPanel",
		region : "center",
		layout : "border",
		frame : true,
		title : '&nbsp;<span style="padding-left:2px; color:red;">' + orderID + '</span>&nbsp;号帐单修改',
		items : [ centerPanelDO ]
	});
	
	initMainView(null, billModCenterPanel, null);
	getOperatorName(pin, "../../");
});
