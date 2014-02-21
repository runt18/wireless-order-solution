var btnPushBack = new Ext.ux.ImageButton({
	imgPath : "../../images/UserLogout.png",
	imgWidth : 50,
	imgHeight : 50,
	tooltip : "返回",
	handler : function(btn) {
		if (orderIsChanged == false) {
			location.href = 'TableSelect.html';
		} else {
			Ext.MessageBox.show({
				msg : '下/改单还未提交，是否确认退出？',
				width : 300,
				buttons : Ext.MessageBox.YESNO,
				fn : function(btn) {
					if (btn == 'yes') {
						location.href = 'TableSelect.html';
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
		if(r.get('taste.alias') == sr.get('taste.alias')){
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
		{header:'口味名', dataIndex:'taste.name', width:120},
		{header:'价钱', dataIndex:'taste.price', width:80, align:'right', renderer:Ext.ux.txtFormat.gridDou},
		{header:'', dataIndex:'taste.rate', hidden:true},
		{header:'', dataIndex:'taste.calcText', hidden:true},
		{header:'', dataIndex:'operation', hidden:true}
	]),
	ds : new Ext.data.JsonStore({
		url : '../../QueryFoodTaste.do',
		root : 'root',		
		fields : FoodTasteRecord.getKeys(),
		listeners : {
			beforeload : function(){
				var data = null;
				if (isGroup) {
					data = Ext.ux.getSelData(orderGroupGridTabPanel.getActiveTab());
				}else{
					data = Ext.ux.getSelData(orderSingleGridPanel);
				}
				this.baseParams['foodID'] = data.id;
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
	loadMask : { msg: '数据请求中,请稍等......' },
	viewConfig : {
		forceFit : true
	},
	cm : new Ext.grid.ColumnModel([
		new Ext.grid.RowNumberer(),
		{header:'口味名', dataIndex:'taste.name', width:120},
		{header:'价钱', dataIndex:'taste.price', width:80, align:'right', renderer:Ext.ux.txtFormat.gridDou},
		{header:'', dataIndex:'taste.rate', hidden:true},
		{header:'', dataIndex:'taste.calcText', hidden:true},
		{header:'', dataIndex:'operation', hidden:true}
	]),
	ds : new Ext.data.JsonStore({
		root : 'root',
		fields : FoodTasteRecord.getKeys()
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
	loadMask : { msg: '数据请求中,请稍等......' },
	viewConfig : {
		forceFit : true
	},
	cm : new Ext.grid.ColumnModel([
		new Ext.grid.RowNumberer(),
		{header:'规格名', dataIndex:'taste.name', width:120},
		{header:'', dataIndex:'taste.price', hidden:true},
		{header:'比例', dataIndex:'taste.rate', width:80, align:'right', renderer:Ext.ux.txtFormat.gridDou},
		{header:'', dataIndex:'taste.calcText', hidden:true},
		{header:'', dataIndex:'operation', hidden:true}
	]),
	ds : new Ext.data.JsonStore({
		root : 'root',
		fields : FoodTasteRecord.getKeys()
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
		{header:'口味名', dataIndex:'taste.name', width:130},
		{header:'价钱', dataIndex:'taste.price', width:70, align:'right', renderer:Ext.ux.txtFormat.gridDou},
		{header:'比例', dataIndex:'taste.rate', width:70, align:'right', renderer:Ext.ux.txtFormat.gridDou},
		{header:'计算方式', dataIndex:'taste.calcText', width:80},
		{header:'操作', align:'center', dataIndex:'operation', width:80, renderer:tasteOperationRenderer}
	]),
	ds : new Ext.data.JsonStore({
		root : 'root',
		fields : FoodTasteRecord.getKeys()
	})
});

/**
 * 重置菜品口味
 */
function refreshHaveTasteHandler(){
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
					if(eval(tasteMenuData.root[j].taste.alias) == gt.taste.alias){
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
				name : '助记码'
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
		maxHeight : 500,
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
		readOnly : true,
		hidden : true,
		listeners : {
			render : function(thiz){
				Ext.Ajax.request({
					url : '../../QueryMenu.do',
					params : {
						dataSource : 'kitchens',
						restaurantID : restaurantID
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
			var searchValue = '';
			
			if(orderMainObject.searchField != ''){
				searchValue = Ext.getCmp(orderMainObject.searchField).getValue();
			}
			
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
	    [true, false, false, true], 
	    ['菜名', 'displayFoodName', 200], 
	    ['助记码', 'alias', 70] , 
		['拼音', 'pinyin', 70], 
		['价格', 'unitPrice', 70, 'right', 'Ext.ux.txtFormat.gridDou']
	],
	FoodBasicRecord.getKeys(),
	[ ['dataSource', 'foods'], ['restaurantID', restaurantID], ['isPaging', true], ['isCookie', true]],
	GRID_PADDING_LIMIT_30,
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
allFoodTabPanelGrid.on('rowclick', function(thiz, ri, e){
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
			    width : 130,
			    store : new Ext.data.JsonStore({
					fields : KitchenRecord.getKeys()
				}),
				valueField : 'id',
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
								isCookie : true,
								dataSource : 'kitchens',
								restaurantID : restaurantID,
								isAllowTemp : true
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
    			id : new Date().format('His'),
    			alias : new Date().format('His'),
    			name : name.getValue().replace(/,/g,';').replace(/，/g,';'),
    			unitPrice : price.getValue(),
    			acturalPrice : price.getValue(),
    			kitchen : {
    				id : kitchen.getValue()
    			},
    			status : 0,
    			count : count.getValue(),
    			orderDateFormat : new Date().format('Y-m-d H:i:s'),
    			waiter : Ext.getDom('optName').innerHTML,
    			dataType : 2,
    			isTemporary : true,
    			hangup : false,
    			tasteGroup : {
    				groupId : 0,
    				tastePref : '无口味',
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
				href : 'TableSelect.html'
			});
		}
	}, {
		text : '提交不打印',
		handler : function() {
			var href = '';
			if(isGroup){
				href = 'CheckOut.html?'+ 'restaurantID=' + restaurantID + '&orderID=' + orderID + '&category=' + tableCategory;
			}else{
				href = 'CheckOut.html?'+ 'restaurantID=' + restaurantID + '&tableID=' + tableAliasID+ '&personCount=1';
			}
			submitOrderHandler({
				notPrint : true,
				href : href			
			});
		}
	}, {
		text : '提交&结帐',
		handler : function() {
			var href = '';
			if(isGroup){
				href = 'CheckOut.html?'+ 'restaurantID=' + restaurantID + '&orderID=' + orderID + '&category=' + tableCategory;
			}else{
				href = 'CheckOut.html?'+ 'restaurantID=' + restaurantID + '&tableID=' + tableAliasID+ '&personCount=1';
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
				location.href = 'TableSelect.html';
			} else {
				Ext.MessageBox.show({
					msg : '下/改单还未提交，是否确认退出？',
					width : 300,
					buttons : Ext.MessageBox.YESNO,
					fn : function(btn) {
						if (btn == 'yes') {
							location.href = 'TableSelect.html';
						}
					}
				});
			}
		}
	}]
});

function initKeyBoardEvent(){
	
	var foodAlias = new Ext.form.NumberField({
		xtype : 'numberfield',
		columnWidth : .49,
		height : 110,
		style : 'line-height: 100px;font-size: 100px;font-weight: bold;text-align: left;color: red;',
		allowBlank : false,
		listeners : {
			render : function(thiz){
				thiz.getEl().dom.setAttribute('maxLength', 5);
			}
		}
	});
	var foodCount = new Ext.form.NumberField({
		xtype : 'numberfield',
		columnWidth : .49,
		height : 110,
		style : 'line-height: 100px;font-size: 100px;font-weight: bold;text-align: left;color: red;',
		allowBlank : false,
		listeners : {
			render : function(thiz){
				thiz.getEl().dom.setAttribute('maxLength', 3);
			}
		}
	});
	
	var btnSaveForQAWin = new Ext.Button({
		text : '保存再录(+)',
		handler : function(thiz){
			if(!foodAlias.isValid() || !foodCount.isValid()){
				return;
			}
			
			var ri = null, gs = allFoodTabPanelGrid.getStore();
			for(var i = 0; i < gs.getCount(); i++){
				if(gs.getAt(i).get('alias') == foodAlias.getValue()){
					ri = i;
					break;
				}
			}
			
			if(ri && ri >= 0){
				addOrderFoodHandler({
					grid : allFoodTabPanelGrid,
					rowIndex : ri,
					count : foodCount.getValue(),
					callback : function(){
						Ext.example.msg('提示', '添加成功.');
						foodAlias.setValue();
						foodCount.setValue();
						foodAlias.clearInvalid();
						foodCount.clearInvalid();
						
						foodAlias.focus(foodAlias, 100);
					}
				});
			}else{
				Ext.example.msg('提示', '该编号菜品信息不在当前展示列表, 请重新输入.');
				foodAlias.focus(foodAlias, 100);
				foodAlias.selectText();
			}
		},
		listeners : {
			render : function(thiz){
				thiz.getEl().setWidth(100, true);
			}
		}
	});
	var btnCloseForQAWin = new Ext.Button({
		text : '关闭(ESC)',
		handler : function(){
			quickActionWin.hide();
		},
		listeners : {
			render : function(thiz){
				thiz.getEl().setWidth(100, true);
			}
		}
	});
	
	var quickActionWin = new Ext.Window({
		title : '&nbsp;',
		modal : true,
		closable : false,
		resizeble : false,
		width : 600,
		keys : [{
			key : 27,
			scope : this,
			fn : function(){
				btnCloseForQAWin.handler(btnCloseForQAWin);
			}
		}, {
			key : 107,
			scope : this,
			fn : function(){
				btnSaveForQAWin.handler(btnSaveForQAWin);
			}
		}, {
			key : 111,
			scope : this,
			fn : function(){
				foodAlias.focus(foodAlias, 100);
				foodAlias.selectText();
			}
		}, {
			key : 106,
			scope : this,
			fn : function(){
				foodCount.focus(foodCount, 100);
				foodCount.selectText();
			}
		}],
		listeners : {
			show : function(){
				foodAlias.setValue();
				foodCount.setValue();
				foodAlias.clearInvalid();
				foodCount.clearInvalid();
				
				foodAlias.focus(foodAlias, 100);
			}
		},
		items : [{
			layout : 'column',
			height : 183,
			frame : true,
			items : [{
				columnWidth : .5,
				html : '菜品编号(/)',
				height : 30,
				style : 'font-size:26px;'
			}, {
				columnWidth : .5,
				html : '数量(*)',
				height : 30,
				style : 'font-size:26px;'
			}, foodAlias, {
				columnWidth : .01,
				html : '&nbsp;'
			}, foodCount, {
				columnWidth : 1,
				buttonAlign : 'center',
				buttons : [btnSaveForQAWin, btnCloseForQAWin]
			}]
		}]
	});
	
	new Ext.KeyMap(document.body, [{
		key : 107,
		scope : this,
		fn : function(){
			quickActionWin.show();
		}
	}]);
}


var dishesOrderNorthPanel = new Ext.Panel({
	id : 'dishesOrderNorthPanel',
	region : 'north',
	height : 40,
	border : false,
	frame : true
});

var dishesOrderEastPanel, centerPanel;
Ext.onReady(function() {
	var menuTabPanel = new Ext.TabPanel({
		id : 'menuTabPanel',
		activeItem : 0,
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
		title : ' 菜单 &nbsp;<font style="color:red;">编号点菜快捷键(+)</font>',
		layout : 'fit',
//		margins : '0 0 0 5',
		items : [menuTabPanel]
	});
	
	centerPanel = new Ext.Panel({
		id : 'centerPanel',
		region : 'center',
		layout : 'border',
		items : [ orderPanel, dishesOrderEastPanel ],
		listeners : {
			render : function(){
				// 初始化数据
				loadOrderData();
				tableStuLoad();
			}
		}
	});

	initMainView(null, centerPanel, null);
	getOperatorName("../../");
	
	initKeyBoardEvent();
});