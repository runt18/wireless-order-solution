function foodAmountOperateRenderer(v, c, r){
	return Ext.ux.txtFormat.gridDou(v)
		+ '<a href="javascript:foodAmountOperateHandler({otype:0,count:1,grid:orderedGrid});"><img src="../../images/btnAdd.gif" border="0" title="菜品数量+1"/></a>&nbsp;'
		+ '<a href="javascript:foodAmountOperateHandler({otype:0,count:-1,grid:orderedGrid});"><img src="../../images/btnDelete.png" border="0" title="菜品数量-1"/></a>&nbsp;'
		+ '<a onClick="foodAmountSetHandler({x:event.clientX,y:event.clientY})"><img src="../../images/icon_tb_setting.png" border="0" title="菜品数量设置"/></a>&nbsp;'
		+ '<a href="javascript:foodAmountDeleteHandler()"><img src="../../images/btnCancel.png" border="0" title="删除菜品"/></a>';
}

var orderedGridTbar = new Ext.Toolbar({
	height : 26,
	items : [{
		text : '数量+1',
		iconCls : 'btn_add',
		handler : function(){
			foodAmountOperateHandler({
				otype : 0,
				count : 1
			});
		}
	}, '-', {
		text : '数量-1',
		iconCls : 'btn_delete',
		handler : function(){
			foodAmountOperateHandler({
				otype : 0,
				count : -1
			});
		}
	}, '-', {
		text : '数量设置',
		id : 'btnOperationFoodCount',
		iconCls : 'icon_tb_setting',
		handler : function(e){
			foodAmountSetHandler({
				x : e.getEl().getX(),
				y : (e.getEl().getY() + e.getEl().getHeight())
			});
		}
	}, '-', {
		text : '删除菜品',
		iconCls : 'btn_cancel',
		handler : function(){
			foodAmountDeleteHandler();
		}
	}]
});

var orderedGrid = createGridPanel(
	'orderSingleGridPanel',
	'已点菜列表',
	'',
	'',
	'',
	[
	    [true, false, false, false],
	    ['菜名', 'displayFoodName'], 
		['口味', 'tasteGroup.tastePref'] , 
		['数量', 'count', , 'right', 'foodAmountOperateRenderer'],
		['单价', 'unitPrice', , 'right', 'Ext.ux.txtFormat.gridDou'],
		['折扣率', 'discount', , 'right', 'Ext.ux.txtFormat.gridDou'],
		['下单时间', 'orderDateFormat'],
		['服务员', 'waiter']
	],
	OrderFoodRecord.getKeys(),
	[],
	0,
	'',
	orderedGridTbar
);
orderedGrid.buttonAlign = 'center';
orderedGrid.addButton({text : '提交', listeners : {
		render : function(thiz){
			thiz.getEl().setWidth(80, true);
		}
	}}, function() {
		submitOrderHandler({grid:orderedGrid});
	}, this);
	
orderedGrid.addButton({
	text : "返回",
	listeners : {
		render : function(thiz){
			thiz.getEl().setWidth(80, true);
		}
	}}, function() {
		if (orderIsChanged == false) {
			location.href = "Bills.html";
		} else {
			Ext.MessageBox.show({
				msg : "账单修改还未提交，是否确认返回？",
				width : 300,
				buttons : Ext.MessageBox.YESNO,
				fn : function(btn) {
					if (btn == "yes") {
						location.href = "Bills.html";
					}
				}
			});
		}
	}, this);
	
	
orderedGrid.region = 'center';


/*orderedGrid.buttons = [new Ext.Button({
	text : '提交',
	listeners : {
		render : function(thiz){
			thiz.getEl().setWidth(80, true);
		}
	},
	handler : function() {
		submitOrderHandler({grid:orderedGrid});
	}
}), new Ext.Button({
	text : "返回",
	listeners : {
		render : function(thiz){
			thiz.getEl().setWidth(80, true);
		}
	},
	handler : function() {
		if (orderIsChanged == false) {
			location.href = "Bills.html";
		} else {
			Ext.MessageBox.show({
				msg : "账单修改还未提交，是否确认返回？",
				width : 300,
				buttons : Ext.MessageBox.YESNO,
				fn : function(btn) {
					if (btn == "yes") {
						location.href = "Bills.html";
					}
				}
			});
		}
	}
})];*/
orderedGrid.getStore().on('load', function(thiz, rs){
	for(var i = 0; i < rs.length; i++){
		Ext.ux.formatFoodName(rs[i], 'displayFoodName', 'name');				
	}
});

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
		readOnly : false,
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
					kitchen.setValue(-1);
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
	    value : -1,
	    store : new Ext.data.JsonStore({
			fields : [ 'id', 'name' ],
			data : [{
				id : -1,
				name : '全部'
			}]
		}),
		valueField : 'id',
		displayField : 'name',
		mode : 'local',
		triggerAction : 'all',
		typeAhead : true,
		selectOnFocus : true,
		forceSelection : true,
		readOnly : false,
		hidden : true,
		listeners : {
			render : function(thiz){
				Ext.Ajax.request({
					url : '../../QueryMenu.do',
					params : {
						isCookie : true,
						dataSource : 'kitchens',
						restaurantID : restaurantID
					},
					success : function(response, options){
						var jr = Ext.util.JSON.decode(response.responseText);
						var root = jr.root;
						thiz.store.loadData(root, true);
					}
				});
			},
			select : function(thiz){
				Ext.getCmp('btnSearchMenu').handler();
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
			var searchType = Ext.getCmp('comSearchType').getValue();
			var searchValue = Ext.getCmp(bmObject.searchField).getValue();
			var gs = allFoodTabPanelGrid.getStore();
			gs.baseParams['kitchenAlias'] = searchType == 0 ? searchValue : '';
			gs.baseParams['foodName'] = searchType == 1 ? searchValue : '';
			gs.baseParams['pinyin'] = searchType == 2 ? searchValue : '';
			gs.baseParams['foodAlias'] = searchType == 3 ? searchValue : '';
			
			gs.load({
				params : {
					start : 0,
					limit : GRID_PADDING_LIMIT_30
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
	[ ['dataSource', 'foods'], ['restaurantID', restaurantID], ['isPaging', true]],
	GRID_PADDING_LIMIT_30,
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
/**
 * 
 */
function billModifyOnLoad() {
	Ext.Ajax.request({
		url : '../../QuerySystemSetting.do',
		params : {
			isCookie : true,
			"restaurantID" : restaurantID
		},
		success : function(res, opt){
			var jr = Ext.decode(res.responseText);
			sysSetting = jr.other.systemSetting;
			var eraseQuota = parseInt(sysSetting.setting.eraseQuota);
			if(eraseQuota > 0){
				Ext.getDom('fontShowEraseQuota').innerHTML = eraseQuota.toFixed(2);
				Ext.getCmp('numErasePrice').setDisabled(false);
			}else{
				Ext.getDom('fontShowEraseQuota').innerHTML = 0.00;
				Ext.getCmp('numErasePrice').setDisabled(true);
			}
		},
		failure : function(res, opt) { 
			Ext.ux.showMsg(Ext.decode(res.responseText));
		}
	});
	
	Ext.Ajax.request({
		url : "../../QueryOrder.do",
		params : {
			isCookie : true,
			restaurantID : restaurantID,
			orderID : orderID,
			queryType: 'Today'
		},
		success : function(response, options) {
			var resultJSON = Ext.util.JSON.decode(response.responseText);
			if (resultJSON.success == true) {
				orderedGrid.order = resultJSON.other.order;
				orderedGrid.order.orderFoods = resultJSON.root;
				orderedGrid.getStore().loadData(resultJSON);
				
				// 加载账单基础信息
				Ext.getCmp('txtSettleTypeFormat').setValue(orderedGrid.order.settleTypeText);
				Ext.getCmp('serviceRate').setValue(orderedGrid.order.serviceRate * 100);
				Ext.getCmp('numErasePrice').setValue(orderedGrid.order.erasePrice);
				var payManner = document.getElementsByName('radioPayType');
				for(var i = 0; i < payManner.length; i++){
					if(payManner[i].value == orderedGrid.order.payTypeValue){
						payManner[i].checked = true;
						break;
					}
				}
				
				Ext.Ajax.request({
					url : '../../QueryDiscountTree.do',
					params : {
						isCookie : true,
						restaurantID : restaurantID
					},
					success : function(res, opt) {
						discountData = eval(res.responseText);
						
						Ext.Ajax.request({
							url : '../../QueryDiscountPlan.do',
							params : {
								isCookie : true,
								restaurantID : restaurantID
							},
							success : function(res, opt){
								var jr = Ext.util.JSON.decode(res.responseText);
								discountPlanData = {root:[]};
								for(var i = 0; i < jr.root.length; i++){
									if(jr.root[i].rate > 0 && jr.root[i].rate < 1){
										discountPlanData.root.push(jr.root[i]);
									}
								}
								var discount = Ext.getCmp('comboDiscount');
								discount.store.loadData({root:discountData});
								discount.setValue(orderedGrid.order.discount.id);
							},
							failure : function(res, opt) {
								Ext.MessageBox.show({
									title : '警告',
									msg : '加载折扣方案信息失败.',
									width : 300
								});
							}
						});
					},
					failure : function(res, opt) {
						Ext.MessageBox.show({
							title : '警告',
							msg : '加载折扣方案信息失败.',
							width : 300
						});
					}
				});
			} else {
				Ext.ux.showMsg(resultJSON);
			}
		},
		failure : function(response, options) {
			Ext.ux.showMsg(Ext.decode(response.responseText));
		}
	});
};
