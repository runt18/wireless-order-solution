var tasteChooseImgBut = new Ext.ux.ImageButton({
	imgPath : '../../images/Taste.png',
	imgWidth : 50,
	imgHeight : 50,
	tooltip : '口味',
	handler : function(btn) {
		orderTasteOperationHandler();
	}
});

var dishDeleteImgBut = new Ext.ux.ImageButton({
	imgPath : '../../images/DeleteDish.png',
	imgWidth : 50,
	imgHeight : 50,
	tooltip : '删除',
	handler : function(btn) {
		orderDeleteFoodOperationHandler();
	}
});
var dishPressImgBut = new Ext.ux.ImageButton({
	imgPath : '../../images/HurryFood.png',
	imgWidth : 50,
	imgHeight : 50,
	tooltip : '催菜',
	handler : function(btn) {
		Ext.example.msg('提示', '<font color="red">感谢您的使用!此功能正在开发中,请关注系统升级.</font>');
		return;
//		dishOptPressHandler(dishOrderCurrRowIndex_);
	}
});
var countAddImgBut = new Ext.ux.ImageButton({
	imgPath : '../../images/AddCount.png',
	imgWidth : 50,
	imgHeight : 50,
	tooltip : '数量加1',
	handler : function(btn) {
		orderFoodCountOperationHandler({
			otype : 0,
			count : 1
		});
	}
});

var countMinusImgBut = new Ext.ux.ImageButton({
	imgPath : '../../images/MinusCount.png',
	imgWidth : 50,
	imgHeight : 50,
	tooltip : '数量减1',
	handler : function(btn) {
		orderFoodCountOperationHandler({
			otype : 0,
			count : -1
		});
	}
});

var countEqualImgBut = new Ext.ux.ImageButton({
	imgPath : '../../images/EqualCount.png',
	imgWidth : 50,
	imgHeight : 50,
	tooltip : '数量等于',
	handler : function(e) {
		orderFoodCountRendererHandler({
			x : e.getEl().getX(),
			y : (e.getEl().getY() + 50)
		});
	}
});

var btnPushBack = new Ext.ux.ImageButton({
	imgPath : "../../images/UserLogout.png",
	imgWidth : 50,
	imgHeight : 50,
	tooltip : "返回",
	handler : function(btn) {
		if (orderIsChanged == false) {
			location.href = 'TableSelect.html?' + 'pin=' + pin + '&restaurantID=' + restaurantID;
		} else {
			Ext.MessageBox.show({
				msg : '下/改单还未提交，是否确认退出？',
				width : 300,
				buttons : Ext.MessageBox.YESNO,
				fn : function(btn) {
					if (btn == 'yes') {
						location.href = 'TableSelect.html?' + 'pin=' + pin + '&restaurantID=' + restaurantID;
					}
				}
			});
		}
	}
});

var btnLogOut = new Ext.ux.ImageButton({
	imgPath : "../../images/ResLogout.png",
	imgWidth : 50,
	imgHeight : 50,
	tooltip : "登出",
	handler : function(btn) {
		
	}
});	

addTasteHandler = function(thiz){
	var hgs = haveTasteGrid.getStore();
	var sr = thiz.getSelectionModel().getSelections()[0];
	var cs = true;
	
//	if(hgs.getCount() >= 3){
//		Ext.example.msg('提示', '该菜品已选择三种口味,最多只能选择三种.');
//		return;
//	}
	
	hgs.each(function(r){
		if(r.get('tasteAliasID') == sr.get('tasteAliasID')){
			Ext.example.msg('提示', '该菜品已选择该口味.');
			cs = false;
			return;
		}
	});
	
	if(cs){
		hgs.insert(hgs.getCount(), sr);
	}
};

var commonTasteGridForTabPanel = new Ext.grid.GridPanel({
	title : '常用口味',
	id : 'commonTasteGridForTabPanel',
	trackMouseOver : true,
//	frame : true,
	loadMask : { msg: '数据请求中,请稍等......' },
	viewConfig : {
		forceFit : true
	},
	cm : new Ext.grid.ColumnModel([
		new Ext.grid.RowNumberer(),
		{header:'口味名', dataIndex:'tasteName', width:120},
		{header:'价钱', dataIndex:'tastePrice', width:80, align:'right', renderer:Ext.ux.txtFormat.gridDou},
		{header:'', dataIndex:'tasteRate', hidden:true},
		{header:'', dataIndex:'tasteCalcFormat', hidden:true},
		{header:'', dataIndex:'operation', hidden:true}
	]),
	ds : new Ext.data.JsonStore({
		url : '../../QueryFoodTaste.do',
		root : 'root',		
		fields : ['tasteID', 'tasteAliasID', 'tasteName', 'tastePrice', 'tasteRate', 'tasteCalcFormat', 'tasteCategory'],
		listeners : {
			beforeload : function(){
				var data = null;
				if (isGroup) {
					data = Ext.ux.getSelData(orderGroupGridTabPanel.getActiveTab());
				}else{
					data = Ext.ux.getSelData(orderSingleGridPanel);
				}
				this.baseParams['foodID'] = data.foodID;
				this.baseParams['pin'] = pin;
				this.baseParams['restaurantID'] = restaurantID;
			},
			load : function(thiz){
				if(thiz.getCount() > 0){
					choosenTasteTabPanel.setActiveTab(commonTasteGridForTabPanel);
				}else{
					choosenTasteTabPanel.setActiveTab(allTasteGridForTabPanel);
				}
			}
		}
	}),
	listeners : {
		rowdblclick : function(thiz, ri){
			addTasteHandler(thiz);
		}
	}
}); 

var allTasteGridForTabPanel = new Ext.grid.GridPanel({
	title : '所有口味',
	id : 'allTasteGridForTabPanel',
	trackMouseOver : true,
//	frame : true,
	loadMask : { msg: '数据请求中,请稍等......' },
	viewConfig : {
		forceFit : true
	},
	cm : new Ext.grid.ColumnModel([
		new Ext.grid.RowNumberer(),
		{header:'口味名', dataIndex:'tasteName', width:120},
		{header:'价钱', dataIndex:'tastePrice', width:80, align:'right', renderer:Ext.ux.txtFormat.gridDou},
		{header:'', dataIndex:'tasteRate', hidden:true},
		{header:'', dataIndex:'tasteCalcFormat', hidden:true},
		{header:'', dataIndex:'operation', hidden:true}
	]),
	ds : new Ext.data.JsonStore({
		root : 'root',
		fields : ['tasteID', 'tasteAliasID', 'tasteName', 'tastePrice', 'tasteRate', 'tasteCalcFormat', 'tasteCategory']
	}),
	listeners : {
		rowdblclick : function(thiz, ri){
			addTasteHandler(thiz);
		}
	}
}); 

var ggForTabPanel = new Ext.grid.GridPanel({
	title : '规格',
	id : 'ggForTabPanel',
	trackMouseOver : true,
//	frame : true,
	loadMask : { msg: '数据请求中,请稍等......' },
	viewConfig : {
		forceFit : true
	},
	cm : new Ext.grid.ColumnModel([
		new Ext.grid.RowNumberer(),
		{header:'规格名', dataIndex:'tasteName', width:120},
		{header:'', dataIndex:'tastePrice', hidden:true},
		{header:'比例', dataIndex:'tasteRate', width:80, align:'right', renderer:Ext.ux.txtFormat.gridDou},
		{header:'', dataIndex:'tasteCalcFormat', hidden:true},
		{header:'', dataIndex:'operation', hidden:true}
	]),
	ds : new Ext.data.JsonStore({
		root : 'root',
		fields : ['tasteID', 'tasteAliasID', 'tasteName', 'tastePrice', 'tasteRate', 'tasteCalcFormat', 'tasteCategory']
	}),
	listeners : {
		rowdblclick : function(thiz, ri){
			addTasteHandler(thiz);
		}
	}
}); 

var choosenTasteTabPanel = new Ext.TabPanel({
	activeTab: 0,
	region : 'center',
	items : [commonTasteGridForTabPanel, allTasteGridForTabPanel, ggForTabPanel]
});

deleteTasteHandler = function(){
	haveTasteGrid.getStore().remove(haveTasteGrid.getSelectionModel().getSelections()[0]);
	haveTasteGrid.getView().refresh();
};

tasteOperationRenderer = function(){
	return '<a href="javascript:deleteTasteHandler()">删除</a>';
};

var haveTasteGrid = new Ext.grid.GridPanel({
	title : '已选口味',
	id : 'haveTasteGrid',
	width : 420,
	trackMouseOver : true,
	frame : true,
	region : 'west',
	loadMask : { msg: '数据请求中,请稍等......' },
	viewConfig : {
		forceFit : true
	},
	cm : new Ext.grid.ColumnModel([
		new Ext.grid.RowNumberer(),
		{header:'口味名', dataIndex:'tasteName', width:130},
		{header:'价钱', dataIndex:'tastePrice', width:70, align:'right', renderer:Ext.ux.txtFormat.gridDou},
		{header:'比例', dataIndex:'tasteRate', width:70, align:'right', renderer:Ext.ux.txtFormat.gridDou},
		{header:'计算方式', dataIndex:'tasteCalcFormat', width:80},
		{header:'操作', align:'center', dataIndex:'operation', width:80, renderer:tasteOperationRenderer}
	]),
	ds : new Ext.data.JsonStore({
		root : 'root',
		fields : ['tasteID', 'tasteAliasID', 'tasteName', 'tastePrice', 'tasteRate', 'tasteCalcFormat', 'tasteCategory']
	})
});

/**
 * 重置菜品口味
 */
refreshHaveTasteHandler = function(){
	haveTasteGrid.getStore().removeAll();
	
	var data = null;
	if (isGroup) {
		data = Ext.ux.getSelData(orderGroupGridTabPanel.getActiveTab());
	}else{
		data = Ext.ux.getSelData(orderSingleGridPanel);
	}
	var tasteGroup = data.tasteGroup;
	var hd = {root:[]};
	
	if(tasteGroup != null && typeof tasteGroup.normalTasteContent != 'undefined'){
		for(var i = 0; i < tasteGroup.normalTasteContent.length; i++){
			var gt = tasteGroup.normalTasteContent[i];
			if(gt != null && typeof gt != 'undefined'){
				for(var j = 0; j < tasteMenuData.root.length; j++){
					if(eval(tasteMenuData.root[j].tasteAliasID) == gt.tasteAliasID){
						hd.root.push(tasteMenuData.root[j]);
						break;
					}
				}
			}
		}
	}
	haveTasteGrid.getStore().loadData(hd);
};

var choosenTasteWin = new Ext.Window({
	title : '&nbsp;',
	closable : false,
	modal : true,
	resizable : false,
	layout : 'border',
	width : 650,
	height : 390,
	items : [haveTasteGrid, choosenTasteTabPanel],
	bbar : ['->', {
		text : '重置已选',
    	iconCls : 'btn_refresh',
		handler : function(){
			refreshHaveTasteHandler();
		}
	}, {
		text : '删除已选',
		iconCls : 'btn_delete',
		handler : function(){
			haveTasteGrid.getStore().removeAll();
		}
	}, {
		text : '保存',
    	iconCls : 'btn_save',
		handler : function(){
			orderTasteOperationHandler();
		}
	}, {
		text : '关闭',
		iconCls : 'btn_close',
		handler : function(){
			choosenTasteWin.hide();
		}
	}],
	keys : [{
		key : Ext.EventObject.ESC,
		scope : this,
		fn : function(){
			choosenTasteWin.hide();
		}
	}],
	listeners : {
		show : function(thiz){
			thiz.center();
			commonTasteGridForTabPanel.getStore().load();
			refreshHaveTasteHandler();
			if(ggForTabPanel.getStore().getCount() == 0){
				tasteOnLoad();				
			}
		}
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
					kitchen.setValue(-1);
					orderMainObject.searchField = kitchen.getId();
				}else if(index == 1){
					kitchen.setVisible(false);
					foodName.setVisible(true);
					pinyin.setVisible(false);
					foodAliasID.setVisible(false);
					foodName.setValue();
					orderMainObject.searchField = foodName.getId();
				}else if(index == 2){
					kitchen.setVisible(false);
					foodName.setVisible(false);
					pinyin.setVisible(true);
					foodAliasID.setVisible(false);
					pinyin.setValue();
					orderMainObject.searchField = pinyin.getId();
				}else if(index == 3){
					kitchen.setVisible(false);
					foodName.setVisible(false);
					pinyin.setVisible(false);
					foodAliasID.setVisible(true);
					foodAliasID.setValue();
					orderMainObject.searchField = foodAliasID.getId();
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
			fields : [ 'alias', 'name' ],
			data : [{
				alias : -1,
				name : '全部'
			}]
		}),
		valueField : 'alias',
		displayField : 'name',
		value : -1,
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
						var jr = Ext.decode(response.responseText);
						thiz.store.loadData(jr.root, true);
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
	}, '-', {
		text : '搜索',
		id : 'btnSearchMenu',
		iconCls : 'btn_search',
		handler : function(){
			var searchType = Ext.getCmp('comSearchType').getValue();
			var searchValue = Ext.getCmp(orderMainObject.searchField).getValue();
//			this.baseParams['searchType'] = typeof(searchType) != 'undefined' ? searchType.getValue() : '';
//			this.baseParams['searchValue'] = typeof(searchValue) != 'undefined' ? searchValue.getValue() : '';
			var gs = allFoodTabPanelGrid.getStore();
			gs.baseParams['kitchenAlias'] = searchType == 0 ? searchValue : '';
			gs.baseParams['foodName'] = searchType == 1 ? searchValue : '';
			gs.baseParams['pinyin'] = searchType == 2 ? searchValue : '';
			gs.baseParams['foodAlias'] = searchType == 3 ? searchValue : '';
			
			gs.load({
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
	    [true, false, false, true], 
	    ['菜名', 'displayFoodName', 200], 
	    ['编号', 'alias', 70] , 
		['拼音', 'pinyin', 70], 
		['价格', 'unitPrice', 70, 'right', 'Ext.ux.txtFormat.gridDou']
	],
//	['displayFoodName', 'foodName', 'aliasID', 'foodID', 'pinyin', 'hot', 'weight', 'isHangup', 'kitchenID',
//	 'unitPrice', 'stop', 'special', 'recommend', 'gift', 'currPrice', 'combination', 'kitchen.id'
//	],
	FoodBasicRecord.getKeys(),
	[['pin',pin], ['type', 1], ['restaurantID', restaurantID], ['isPaging', true]],
	30,
	'',
	allFoodTabPanelGridTbar
);
allFoodTabPanelGrid.frame = false;
allFoodTabPanelGrid.border = false;
allFoodTabPanelGrid.getBottomToolbar().displayMsg = '每页&nbsp;30&nbsp;条,共&nbsp;{2}&nbsp;条记录';
allFoodTabPanelGrid.on('render', function(thiz){
	Ext.getCmp('btnSearchMenu').handler();
});
allFoodTabPanelGrid.getStore().on('load', function(thiz, records){
	for(var i = 0; i < records.length; i++){
		Ext.ux.formatFoodName(records[i], 'displayFoodName', 'name');
	}
});
allFoodTabPanelGrid.on('rowdblclick', function(thiz, ri, e){
	addOrderFoodHandler({
		grid : thiz,
		rowIndex : ri
	});
});

var allFoodTabPanel = new Ext.Panel({
	title : '&nbsp;所有菜&nbsp;',
	id : 'allFoodTabPanel',
	layout : 'fit',
	items : [allFoodTabPanelGrid]
});

var tempFoodTabPanel = new Ext.Panel({
	title : '&nbsp;临时菜&nbsp;',
	id : 'tempFoodTabPanel',
	layout : 'fit',
	border : false,
	items : [{
		xtype : 'panel',
		frame : true,
		border : false,
		layout : 'column',
		defaults : {
			xtype : 'panel',
			layout : 'form',
			border : false,
			columnWidth : .5,
			labelWidth : 45
		},
		items : [{
			items : [{
				xtype : 'textfield',
				id : 'txtTempFoodName',
				fieldLabel : '菜名',
				allowBlank : false,
				validator : function(v){
					if(v.trim().length == 0){
						return '菜名不允许为空.';
					}else{
						return true;
					}
				}
			}]
		}, {
			items : [{
				xtype : 'numberfield',
				id : 'numTempFoodCount',
				fieldLabel : '数量',
				value : 1,
				allowBlank : false,
				style : 'text-align:right;',
				validator : function(v){
					if(v < 0.01 || v > 65535){
						return '数量需在 0.01 至 65535 之间.';
					}else{
						return true;
					}
				}
			}]
		}, {
			items : [{
				xtype : 'numberfield',
				id : 'numTempFoodPrice',
				fieldLabel : '单价',
				value : 0,
				allowBlank : false,
				style : 'text-align:right;',
				validator : function(v){
					if(v < 0.00 || v > 65535){
						return '单价需在 0  至 65535 之间.';
					}else{
						return true;
					}
				}
			}]
		}, {
			items : [new Ext.form.ComboBox({
				xtype : 'combo',
				id : 'comboTempFoodKitchen',
				fieldLabel : '分厨',
			    width : 144,
			    store : new Ext.data.JsonStore({
					fields : [ 'aliasId', 'name' ]
				}),
				valueField : 'aliasId',
				displayField : 'name',
				mode : 'local',
				triggerAction : 'all',
				typeAhead : true,
				selectOnFocus : true,
				forceSelection : true,
				readOnly : true,
				allowBlank : false,
				listeners : {
					render : function(thiz){
						Ext.Ajax.request({
							url : '../../QueryMenu.do',
							params : {
								restaurantID : restaurantID,
								isAllowTemp : true,
								type : 3
							},
							success : function(response, options){
								var jr = Ext.util.JSON.decode(response.responseText);
								var root = jr.root;
								for(var i = root.length - 1; i >= 0; i--){
									if(root[i].aliasId == 253 || root[i].aliasId == 255){
										root.splice(i,1);
									}
								}
								thiz.store.loadData(root);
							}
						});
					}
				}
			})]
		}]
	}],
	tbar : [ '->', {
		text : '添加',
    	iconCls : 'btn_add',
    	handler : function(e){
    		var name = Ext.getCmp('txtTempFoodName');
    		var count = Ext.getCmp('numTempFoodCount');
    		var price = Ext.getCmp('numTempFoodPrice');
    		var kitchen = Ext.getCmp('comboTempFoodKitchen');
    		
    		if(!name.isValid() || !count.isValid() || !price.isValid() || !kitchen.isValid()){
    			return;
    		}
    		
    		orderSingleGridPanel.order.orderFoods.push({
				foodName : name.getValue().replace(/,/g,';').replace(/，/g,';'),
				tastePref : '无口味',
				count : count.getValue(),
				unitPrice : price.getValue(),
				acturalPrice : price.getValue(),
				orderDateFormat : (new Date().format('Y-m-d H:i:s')),
				waiter : Ext.getDom('optName').innerHTML,
				foodID : (new Date().format('His')),
				aliasID  : (new Date().format('His')),
				discount : 0,
				kitchenID : kitchen.getValue(),
				special : false,
				recommend : false,
				soldout : false,
				gift : false,
				hot : false,
				weight : false,
				dataType : 2,
				currPrice : false,
				temporary : true,
				tmpFoodName : name.getValue(),
				tmpTasteAlias : 0,
				hangup : false,
				tasteGroup : {
					normalTaste : null,
					normalTasteContent : [],
					tempTaste : null
				}
			});		
    		orderSingleGridPanel.getStore().loadData({root:orderSingleGridPanel.order.orderFoods});
    		orderGroupDisplayRefresh({
				control : orderSingleGridPanel
			});
    		
    		name.setValue();
    		count.setValue(1);
    		price.setValue(0);
    		kitchen.setValue(0);
    		
    		name.clearInvalid();
    		count.clearInvalid();
    		price.clearInvalid();
    		kitchen.clearInvalid();
    	}
    }]
});

var orderPanel = new Ext.Panel({
	xtype : 'panel',
	title : '已点菜列表',
	frame : true,
	region : 'center',
	layout : 'fit',
	buttonAlign : 'center',
	buttons : [{
		text : '提交',
		handler : function() {
			submitOrderHandler({
				href : 'TableSelect.html?pin=' + pin + '&restaurantID=' + restaurantID
			});
		}
	}, {
		text : '提交&结帐',
		handler : function() {
			var href = '';
			if(isGroup){
				href = 'CheckOut.html?'
					+ 'pin=' + pin
					+ '&restaurantID=' + restaurantID
					+ '&orderID=' + orderID
					+ '&category=' + tableCategory;
			}else{
				href = 'CheckOut.html?'
					+ 'pin=' + pin
					+ '&restaurantID=' + restaurantID
					+ '&tableID=' + tableAliasID 
					+ '&personCount=1';
			}
			submitOrderHandler({
				href : href			
			});
		}
	}, {
    	text : '刷新',
    	hidden : isGroup || isFree,
    	handler : function(){
    		refreshOrderHandler();
    	}
    }, {
		text : '返回',
		handler : function() {
			if (orderIsChanged == false) {
				location.href = 'TableSelect.html?' + 'pin=' +pin  + '&restaurantID=' + restaurantID;
			} else {
				Ext.MessageBox.show({
					msg : '下/改单还未提交，是否确认退出？',
					width : 300,
					buttons : Ext.MessageBox.YESNO,
					fn : function(btn) {
						if (btn == 'yes') {
							location.href = 'TableSelect.html?' + 'pin=' +pin + '&restaurantID=' + restaurantID;
						}
					}
				});
			}
		}
	}]
});

var dishesOrderNorthPanel = new Ext.Panel({
	id : 'dishesOrderNorthPanel',
	region : 'north',
	height : 40,
	border : false,
//	layout : 'form',
	frame : true
});

var dishesOrderEastPanel;
var centerPanel;
Ext.onReady(function() {
	Ext.lib.Ajax.defaultPostHeader += '; charset=utf-8';
	Ext.QuickTips.init();
	
	var menuTabPanel = new Ext.TabPanel({
		id : 'menuTabPanel',
		activeItem : 0,
//		items : [allFoodTabPanel, tempFoodTabPanel],
		items : [allFoodTabPanel],
		listeners : {
			beforerender : function(thiz){
				if(!isGroup){
					thiz.add(tempFoodTabPanel);
				}
			},
			tabchange : function(thiz, active){
				if(active.getId() == tempFoodTabPanel.getId()){
					var name = Ext.getCmp('txtTempFoodName');
		    		var count = Ext.getCmp('numTempFoodCount');
		    		var price = Ext.getCmp('numTempFoodPrice');
		    		
		    		name.setValue();
		    		count.setValue(1);
		    		price.setValue(0);
		    		name.clearInvalid();
		    		count.clearInvalid();
		    		price.clearInvalid();
				}
			}
		}
	});

	dishesOrderEastPanel = new Ext.Panel({
		region : 'east',
		width : 450,
		id : 'dishesOrderEastPanel',
		frame : true,
		title : ' 菜单 ',
		layout : 'fit',
		margins : '0 0 0 5',
		items : [menuTabPanel]
	});
	
	centerPanel = new Ext.Panel({
		title : '&nbsp;',
		id : 'centerPanel',
		region : 'center',
		layout : 'border',
		items : [ orderPanel, dishesOrderEastPanel ],
		listeners : {
			render : function(){
				// 初始化数据
				loadOrderData();
				tableStuLoad();
//				tasteOnLoad();
			}
		}
		,tbar : new Ext.Toolbar({
			height : 55,
			items : ['->',
			btnPushBack,
			{xtype:'tbtext', text:'&nbsp;&nbsp;&nbsp;&nbsp;'},
			btnLogOut,
			{xtype:'tbtext', text:'&nbsp;&nbsp;&nbsp;&nbsp;'}
			]
		})
	});

	new Ext.Viewport({
		layout : 'border',
		id : 'viewport',
		items : [ {
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
			layout : 'form',
			frame : true,
			border : false,
			html : '<div style="font-size:11pt; text-align:center;""><b>版权所有(c) 2011 智易科技</b></div>'
		}]
	});
	
});