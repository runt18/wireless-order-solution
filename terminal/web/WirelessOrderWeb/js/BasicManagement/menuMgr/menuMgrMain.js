﻿
//-------------lib.js---------
/**
 * 菜品搜索
 */
function searchMenuHandler(){
	var baseParams = {
		'isPaging' : true,
		
		'restaurantId' : restaurantID
	};
	// 分厨
	var sn = kitchenTreeForSreach.getSelectionModel().getSelectedNode();
	if(sn && sn.attributes.alias >= 0)
		baseParams['kitchen'] = sn.attributes.alias;
	// 编号, 名称, 拼音, 价钱, 库存管理
	var queryType = Ext.getCmp('filter').getValue();
	if(queryType != '全部' && queryType != 0){	
		if(queryType == 1){
			baseParams['alias'] = Ext.getCmp('numfieldForGridSearch').getValue();
			baseParams['operator'] = Ext.getCmp('comboOperatorForGridSearch').getValue();
		}else if(queryType == 2){
			baseParams['name'] = Ext.getCmp('textfieldForGridSearch').getValue();
		}else if(queryType == 3){
			//baseParams['pinyin'] = Ext.getCmp('textfieldForGridSearch').getValue();
		}else if(queryType == 4){
			baseParams['price'] = Ext.getCmp('numfieldForGridSearch').getValue();
			baseParams['operator'] = Ext.getCmp('comboOperatorForGridSearch').getValue();
		}else if(queryType == 6){
			var stock = Ext.getCmp('comboStockStatusForSearch');
			baseParams['stockStatus'] = stock.getRawValue() != '' && stock.getValue() > 0 ? stock.getValue() : '';
		}
	}
	// 状态
	var isSpecial = Ext.getCmp('specialCheckbox').getValue();
	var isRecommend = Ext.getCmp('recommendCheckbox').getValue();
	var isStop = Ext.getCmp('stopCheckbox').getValue();
	var isFree = Ext.getCmp('freeCheckbox').getValue();
	var isCurrPrice = Ext.getCmp('currPriceCheckbox').getValue();
	var isCombination = Ext.getCmp('combinationCheckbox').getValue();
	var isHot = Ext.getCmp('hotCheckbox').getValue();
	var isWeight = Ext.getCmp('weightCheckbox').getValue();
	if(isSpecial)
		baseParams['isSpecial'] = true;
	if(isRecommend)
		baseParams['isRecommend'] = true;
	if(isStop)
		baseParams['isStop'] = true;
	if(isFree)
		baseParams['isFree'] = true;
	if(isCurrPrice)
		baseParams['isCurrPrice'] = true;
	if(isCombination)
		baseParams['isCombination'] = true;
	if(isHot)
		baseParams['isHot'] = true;
	if(isWeight)
		baseParams['isWeight'] = true;
	
	var gs = menuGrid.getStore();
	gs.baseParams = baseParams;
	gs.load({
		params : {
			start : 0,
			limit : GRID_PADDING_LIMIT_20
		}
	});
//	menuMgrGrid.getSelectionModel().clearSelections();
//	menuMgrGrid.fireEvent('rowclick');
}

/**
 * 修改菜品相关信息,暂时包括:基础信息,关联口味,关联食材,关联套菜
 */
foodOperationHandler = function(c){
	c = c == null || typeof(c) == 'undefined' ? {type:''} : c;
	
	var foWinTab = Ext.getCmp('foodOperationWinTab');
	var activeTab = foWinTab.getActiveTab();
	var selData = Ext.ux.getSelData('menuMgrGrid');
	c.data = selData;
	
	if(typeof(activeTab) == 'undefined'){
		return;
	}
	
	if(activeTab.getId() == 'basicOperationTab'){
		if(c.type == mmObj.operation.insert){
//			resetbBasicOperation();
		}else if(c.type == mmObj.operation.update){
			updateBasicHandler(c);
		}else if(c.type == mmObj.operation.select){
			if(!selData){
				return;
			}
			resetbBasicOperation(selData);
		}
	}else if(activeTab.getId() == 'tasteOperationTab'){
		if(c.type == mmObj.operation.update){
			updateTasteHandler(c);
		}else if(c.type == mmObj.operation.select){
			if(!selData){
				return;
			}
			if(parseInt(selData.tasteRefType) == 1){
				Ext.getCmp('rdoTasteTypeSmart').setValue(true);
				Ext.getDom('rdoTasteTypeSmart').onclick();
			}else if(parseInt(selData.tasteRefType) == 2){
				Ext.getCmp('rdoTasteTypeManual').setValue(true);
				Ext.getDom('rdoTasteTypeManual').onclick();
			}
			var ctgd = Ext.getCmp('commonTasteGrid').getStore();
			ctgd.load();
		}
	}else if(activeTab.getId() == 'materialOperationTab'){
		if(c.type == mmObj.operation.update){
			updateMaterialHandler(c);
		}else if(c.type == mmObj.operation.select){
			if(!selData){
				return;
			}
			Ext.getCmp('txtMaterialNameSearch').setValue('');
			Ext.getCmp('btnSearchForAllMaterialGridTbar').handler();
			var hmgd = Ext.getCmp('haveMaterialGrid').getStore();
			hmgd.load();
		}
	}else if(activeTab.getId() == 'combinationOperationTab'){
		var cfgd = Ext.getCmp('combinationFoodGrid').getStore();
		if(c.type == mmObj.operation.insert){
			
		}else if(c.type == mmObj.operation.update){
			updateCombinationHandler(c);
		}else if(c.type == mmObj.operation.select){
			if(!selData){
				return;
			}
			Ext.getCmp('txtMiniAllFoodNameSearch').setValue('');
			Ext.getCmp('btnSearchForAllFoodMiniGridTbar').handler();
			cfgd.load();
		}
	}	
};

/**
 * 
 * @param active
 * @param type
 */
function foodOperation(active, type){
	var foWin = Ext.getCmp('foodOperationWin');
	var selRowData = Ext.ux.getSelData('menuMgrGrid');
	
	if(typeof(type) == 'string' && type == mmObj.operation.insert){
	
	}else if(typeof(type) == 'string' && type == mmObj.operation.update){	
		if(!selRowData){
			Ext.example.msg('提示','请选中一道菜品再操作!');
			return;
		}
	}else{
		return;
	}
	
	// 标志是否新添加菜品
	foWin.operation = type;
	foWin.show();
	foWin.center();
	
	var btnAddForOW = Ext.getCmp('btnAddForOW');
	var btnAppForOW = Ext.getCmp('btnAppForOW');
	var btnSaveForOW = Ext.getCmp('btnSaveForOW');
	var btnCloseForOW = Ext.getCmp('btnCloseForOW');
	var btnRefreshForOW = Ext.getCmp('btnRefreshForOW');
	var btnPreviousFood = Ext.getCmp('btnPreviousFood');
	var btnNextFood = Ext.getCmp('btnNextFood');
	var txtFoodPriceExplain = Ext.getCmp('txtFoodPriceExplain');
	
	if(typeof(type) == 'string' && type == mmObj.operation.insert){
		foWin.setTitle('添加菜品');
		btnAddForOW.setVisible(true);
		btnAppForOW.setVisible(false);
		btnSaveForOW.setVisible(false);
		btnPreviousFood.setVisible(false);
		btnNextFood.setVisible(false);		
		txtFoodPriceExplain.setVisible(false);
		
		btnAddForOW.setDisabled(false);
		btnAppForOW.setDisabled(true);
		btnSaveForOW.setDisabled(true);
		
		resetbBasicOperation();
		Ext.getCmp('combinationFoodGrid').getStore().removeAll();
		Ext.getDom('txtDisplayCombinationFoodPrice').innerHTML = '0.00';
		Ext.getDom('txtDisplayCombinationFoodPriceAmount').innerHTML = '0.00';
		Ext.getCmp('txtMiniAllFoodNameSearch').setValue('');
		Ext.getCmp('btnSearchForAllFoodMiniGridTbar').handler();
	}else if(typeof(type) == 'string' && type == mmObj.operation.update){
		foWin.setTitle(selRowData.name);
		btnAddForOW.setVisible(false);
		btnAppForOW.setVisible(true);
		btnSaveForOW.setVisible(true);
		btnPreviousFood.setVisible(true);
		btnNextFood.setVisible(true);
		txtFoodPriceExplain.setVisible(true);
		
		btnAddForOW.setDisabled(true);
		btnAppForOW.setDisabled(false);
		btnSaveForOW.setDisabled(false);
		btnPreviousFood.setDisabled(false);
		btnNextFood.setDisabled(false);
		
		if(!Ext.getCmp('menuMgrGrid').getSelectionModel().hasPrevious()){
			btnPreviousFood.setDisabled(true);
			btnNextFood.setDisabled(false);
		}else if(!Ext.getCmp('menuMgrGrid').getSelectionModel().hasNext()){
			btnPreviousFood.setDisabled(false);
			btnNextFood.setDisabled(true);
		}
	}
	
	btnCloseForOW.setVisible(true);
	btnCloseForOW.setDisabled(false);
	
	btnRefreshForOW.setVisible(true);
	btnRefreshForOW.setDisabled(false);
	
	if(typeof active == 'string'){
		if(Ext.getCmp(active)){
			var foWinTab = Ext.getCmp('foodOperationWinTab');
			if(!foWinTab.getActiveTab() || foWinTab.getActiveTab().getId() == active){
				foWinTab.fireEvent('tabchange');
			}else{
				foWinTab.setActiveTab(active);
			}
		}
	}
	
};
/**
 * 
 * @param p
 */
function refreshInfoGrid(p){
	if(p.getId() == 'materialGridTab'){
		if(!Ext.ux.getSelData('menuMgrGrid')){
			Ext.getCmp('materialGrid').getStore().removeAll();
			return;
		}
		Ext.getCmp('materialGrid').getStore().load();
	}else if(p.getId() == 'tasteGridTab'){
		if(!Ext.ux.getSelData('menuMgrGrid')){
			Ext.getCmp('tasteGrid').getStore().removeAll();
			return;
		}
		Ext.getCmp('tasteGrid').getStore().load();
	}else if(p.getId() == 'combinationGridTab'){
		if(!Ext.ux.getSelData('menuMgrGrid')){
			Ext.getCmp('combinationGrid').getStore().removeAll();
			return;
		}
		Ext.getCmp('combinationGrid').getStore().load();
	}
};
//-----------------


//------------------comb
function combinationOperationRenderer(){
	return '<a href="javascript:combinationDeleteHandler()">删除</a>';
};

function combinationDeleteHandler(){
	var cmg = Ext.getCmp('combinationFoodGrid');
	cmg.getStore().remove(cmg.getSelectionModel().getSelections()[0]);
	cmg.getView().refresh();
};

function combinationDisplaySumHandler(){
	var cfg = Ext.getCmp('combinationFoodGrid').getStore();
	var sumPrice = 0, sumAmount = 0, itemSumPrice = 0;
	for(var i = 0; i < cfg.getCount(); i++){
		itemSumPrice = cfg.getAt(i).get('unitPrice') * cfg.getAt(i).get('amount');
		cfg.getAt(i).set('sumPrice', itemSumPrice);
		sumPrice += itemSumPrice;
		sumAmount += cfg.getAt(i).get('amount');
	}
	Ext.getDom('txtDisplayCombinationFoodPrice').innerHTML = parseFloat(sumPrice).toFixed(2);
	Ext.getDom('txtDisplayCombinationFoodPriceAmount').innerHTML = parseFloat(sumAmount).toFixed(2);
};

var combinationFoodGrid = new Ext.grid.EditorGridPanel({
	title : '<center>已关联菜品<font color="red">(关联菜品即可设为套菜,否则留空)</font></center>',
	id : 'combinationFoodGrid',
	columnWidth : .55,
	loadMask : { msg: '数据请求中，请稍后...' },
	frame : true,
	trackMouseOver : true,
	viewConfig : {
		forceFit : true
	},
	sm : new Ext.grid.RowSelectionModel({singleSelect:true}),
	cm : new Ext.grid.ColumnModel([
	    new Ext.grid.RowNumberer(),
	    {header:'菜名', dataIndex:'name', width:200},
	    {header:'价格',  dataIndex:'unitPrice', align:'right', width:80, renderer:Ext.ux.txtFormat.gridDou},
	    {
	    	header : '份数',
	    	dataIndex : 'amount',
	    	width : 80,
	    	align : 'right',
	    	renderer : Ext.ux.txtFormat.gridDou,
	    	editor : new Ext.form.NumberField({
	    		maxLength : 8,
	    		maxLengthText : '长度不能超过8位',
	    		minValue : 1,
	    		maxValue : 65535,
	    		allowBlank : false,
	    		style : 'color:green; font-weight:bold;',
	    		validator : function(v){
	    	    		if(/^\d+$/.test(v)){
	    	    			return true;
	    	    		}else{
	    	    			return '输入有误,份数只能是正整数!';
	    	    		}
	    	    	}
	    	})
	    },
	    {header:'成本',  dataIndex:'sumPrice', align:'right', width:90, renderer:Ext.ux.txtFormat.gridDou},
	    {header:'操作', align:'center', renderer:combinationOperationRenderer}
	]),
	ds : new Ext.data.JsonStore({
		url : '../../QueryFoodCombination.do',
		root : 'root',
		fields : ComboFoodRecord.getKeys(),
		listeners : {
			beforeload : function(){
				var selData = Ext.ux.getSelData('menuMgrGrid');
				this.baseParams['foodID'] = selData.id;
				this.baseParams['restaurantID'] = restaurantID;
			},
			load : function(){
				combinationDisplaySumHandler();
			},
			add : function(){
				combinationDisplaySumHandler();
			},
			remove : function(){
				combinationDisplaySumHandler();
			},
			update : function(){
				combinationDisplaySumHandler();
			}
		}
	}),
	bbar : new Ext.Toolbar({
		height : 26,
		items : [{
			xtype:'tbtext', 
			text:String.format(Ext.ux.txtFormat.barTitle, '总计')
		}, '->', {
			xtype:'tbtext', 
			text:String.format(Ext.ux.txtFormat.barMsg, '总成本', 'txtDisplayCombinationFoodPrice', '0.00')
		}, {
			xtype:'tbtext', 
			text:String.format(Ext.ux.txtFormat.barMsg, '总份数', 'txtDisplayCombinationFoodPriceAmount', '0.00')
		}]
	}),
	listeners : {
		resize : function(thiz, adjWidth, adjHeight, rawWidth, rawHeight){
			thiz.setHeight(tabItemsHeight);
		}
	}
});

var allFoodMiniGridTbar = new Ext.Toolbar({
	height : 26,
	items : [{ xtype:'tbtext', text:'菜名搜索:'}, {
		xtype : 'textfield',
		id : 'txtMiniAllFoodNameSearch',
		width : 100
	}, '->', {
		text : '搜索',
		id : 'btnSearchForAllFoodMiniGridTbar',
		iconCls : 'btn_search',
		handler : function(){
			var mafn = Ext.getCmp('txtMiniAllFoodNameSearch').getValue().trim();
			var afmgs = allFoodMiniGrid.getStore();
			afmgs.baseParams['name'] = mafn;
			afmgs.load({
				params : {
					limit : GRID_PADDING_LIMIT_20,
					start : 0
				}
			});
		}
	}]
});

var allFoodMiniGrid = createGridPanel(
    'allFoodMiniGrid',
    '<center>所有菜品</center>',
    tabItemsHeight,
    '',
    '../../QueryMenuMgr.do',
    [
	    [true, false, false, true], 
	    ['编号', 'alias', 70] , 
	    ['菜名', 'name', 200] , 
	    ['价格', 'unitPrice', '', 'right', 'Ext.ux.txtFormat.gridDou']
	],
	FoodBasicRecord.getKeys(),
    [ ['restaurantId', restaurantID], ['isPaging', true]],
    GRID_PADDING_LIMIT_20,
    '',
    allFoodMiniGridTbar
);
allFoodMiniGrid.columnWidth = .44;
allFoodMiniGrid.getBottomToolbar().displayMsg = '共&nbsp;{2}&nbsp;条记录';
allFoodMiniGrid.on('resize', function(thiz){
	thiz.setHeight(tabItemsHeight);
});
allFoodMiniGrid.on('rowdblclick', function(thiz){
	var cfd = Ext.getCmp('combinationFoodGrid');
	var sr = thiz.getSelectionModel().getSelections()[0];
	var selData = Ext.ux.getSelData(menuGrid);
	var cv = true;
	if(sr.get('id') == selData.id){
		Ext.example.msg('提示','添加失败,套菜不能包含原菜!');
		return;
	}
	if(Ext.ux.cfs.isCombo(sr.get('status'))){
		Ext.example.msg('提示','添加失败,套菜不能关联套菜!');
		return;
	}
	cfd.getStore().each(function(r){
		if(r.get('id') == sr.get('id')){
			cv = false;
			r.set('amount', parseFloat(r.get('amount') + 1));
			return;
		}
	});
	if(cv){
		sr.set('amount', 1);
		cfd.getStore().insert(0, sr);
		cfd.getView().refresh();
	}
});

var combinationOperationPanel = new Ext.Panel({
	id : 'combinationOperationPanel',
	frame : true,
	border : false,
	layout : 'column',
	items : [
	    combinationFoodGrid,
	    {xtype:'panel', columnWidth:.01, html:'&nbsp;'},
	    allFoodMiniGrid
	]
});

/**
 * 修改菜品关联套菜
 */
updateCombinationHandler = function(c){
//	Ext.example.msg('提示','修改菜品关联套菜!');
	var foodID = c.data.id;
	var status = c.data.status;
	var comboContent = '';
	
	var cfg = Ext.getCmp('combinationFoodGrid').getStore();
	for(var i = 0; i < cfg.getCount(); i++){
		comboContent += (i > 0 ? '<split>' : '');
		comboContent += (cfg.getAt(i).get('id') + ',' + cfg.getAt(i).get('amount'));
	}
	
	setButtonStateOne(true);
	
	Ext.Ajax.request({
		url : '../../UpdateFoodCombination.do',
		params : {
			foodID : foodID,
			restaurantID : restaurantID,
			status : status,
			comboContent : comboContent
		},
		success : function(response, options){
			var jr = Ext.util.JSON.decode(response.responseText);
			if(eval(jr.success)){
				Ext.example.msg(jr.title, jr.msg);
				if(c.hide == true){
					Ext.getCmp('foodOperationWin').hide();
				}else{
					cfg.load();					
				}
				Ext.getCmp('menuMgrGrid').getStore().each(function(record){
					if(record.get('foodID') == c.data.foodID){
						if(cfg.getCount() > 0){
							record.set('combination', true);
						}else{
							record.set('combination', false);
						}
						Ext.ux.formatFoodName(record, 'displayFoodName', 'foodName');
						record.commit();
						return;
					}
				});
			}else{
				Ext.ux.showMsg(jr);
			}
			setButtonStateOne(false);
		},
		failure : function(response, options) {
			var jr = Ext.util.JSON.decode(response.responseText);
			Ext.ux.showMsg(jr);
			setButtonStateOne(false);
		}
	});
	
};
//----------------


//----------------taste----
tasteCalcRenderer = function(val, metadata, record){
	if(val == 0){
		return '按价格';
	}else if(val == 1){
		return '按比例';
	}
};

tasteOperationRenderer = function(value, cellmeta, record, rowIndex, columnIndex, store){
	return '<a href="javascript:tasteDeleteHandler()">删除</a>';
};

tasteDeleteHandler = function(){
	commonTasteGrid.getStore().remove(commonTasteGrid.getSelectionModel().getSelections()[0]);
	commonTasteGrid.getView().refresh();
};

var commonTasteGridTbar = new Ext.Toolbar({
	height : 26,
	items : [{
		xtype:'tbtext', 
		text:'&nbsp;关联方式:&nbsp;'
	}, {
		xtype : 'radio',
    	id : 'rdoTasteTypeSmart',
    	name : 'rdoTasteType',
    	boxLabel : '智能',
    	width : 60,
    	inputValue : 1,
    	listeners : {
    		render : function(e){
    			Ext.getDom(e.getId()).onclick = function(){
    				if(e.getValue()){
    					Ext.getCmp('allTasteGrid').setDisabled(true);
    					commonTasteGrid.getColumnModel().setColumnWidth(6, 80);
    					commonTasteGrid.getColumnModel().setHidden(6, true);
	    				mmObj.rdoTasteType = e.getRawValue();
	    			}
    			};
    		}
    	}
    }, {
    	xtype : 'radio',
    	id : 'rdoTasteTypeManual',
    	name : 'rdoTasteType',
    	boxLabel : '人工',
    	width : 60,
    	inputValue : 2,
    	listeners : {
    		render : function(e){
    			Ext.getDom(e.getId()).onclick = function(){
    				if(e.getValue()){
    					Ext.getCmp('allTasteGrid').setDisabled(false);
    					Ext.getCmp('allTasteGrid').getSelectionModel().selectFirstRow();
    					commonTasteGrid.getColumnModel().setHidden(6, false);
    					var sv = Ext.getDom('txtTasteNameSearch');
    					if(sv.value != ''){
    						sv.value = '';
    						sv.onkeyup();
    					}
	    				mmObj.rdoTasteType = e.getRawValue();
	    			}
    			};
    		}
    	}
    }]
});

var commonTasteGrid = new Ext.grid.EditorGridPanel({
	title : '<center>已关联口味</center>',
	id : 'commonTasteGrid',
	columnWidth : .55,
//	height : (Ext.isIE ? 405 : 400),
	loadMask : { msg: '数据请求中，请稍后...' },
	frame : true,
	trackMouseOver : true,
	viewConfig : {
		forceFit : true
	},
	sm : new Ext.grid.RowSelectionModel({singleSelect:true}),
	cm : new Ext.grid.ColumnModel([
	    new Ext.grid.RowNumberer(),
	    {header:'口味名', dataIndex:'taste.name', width:100},
		{
	    	header : '等级', 
			dataIndex : 'taste.rank', 
			width : 60, 
			align : 'center', 
			editor : new Ext.form.NumberField({
				maxLength : 8,
	    		maxLengthText : '长度不能超过8位',
	    		minValue : 0.01,
	    		maxValue : 65535,
	    		allowBlank : false,
	    		style : 'color:green; font-weight:bold;'
			})
		},
		{header:'价钱', dataIndex:'taste.price', width:60, renderer:Ext.ux.txtFormat.gridDou},
		{header:'比例', dataIndex:'taste.rate', width:60, renderer:Ext.ux.txtFormat.gridDou},
		{header:'计算方式', dataIndex:'taste.calcValue', renderer:tasteCalcRenderer},
		{header:'操作', align:'center', renderer:tasteOperationRenderer}
		]
	),
	ds : new Ext.data.JsonStore({
		url : '../../QueryFoodTaste.do',
		root : 'root',		
		fields : FoodTasteRecord.getKeys(),
		listeners : {
			beforeload : function(){
				var selData = Ext.ux.getSelData('menuMgrGrid');
				this.baseParams['foodID'] = selData.id;
				this.baseParams['restaurantID'] = restaurantID;
			}
		}
	}),
	tbar : commonTasteGridTbar,
	listeners : {
		resize : function(thiz, adjWidth, adjHeight, rawWidth, rawHeight){
			thiz.setHeight(tabItemsHeight);
		}
	}
});

var allTasteGridTbar = new Ext.Toolbar({
	height : 26,
	items : [{
		xtype:'tbtext', text:'&nbsp;口味名搜索:&nbsp;'
	}, {
		xtype : 'textfield',
    	id : 'txtTasteNameSearch',
    	width : 100,
    	listeners : {
    		render : function(e){
    			Ext.getDom('txtTasteNameSearch').onkeyup = function(){
    				var txtTasteName = Ext.getCmp('txtTasteNameSearch').getValue().trim();
    				var store = allTasteGrid.getStore();
    				var selModel = allTasteGrid.getSelectionModel();
    				var searchData = {root:[]}, orderByData = [], otherData = [], selIndex = [];
    				if(selModel.getSelections().length > 0){
    					selModel.clearSelections();
    				}
    				if(txtTasteName.length == 0){
    					for(var i = 0; i < store.getCount(); i++){
	    					var selRow = allTasteGrid.getView().getRow(i);
	    					selRow.style.backgroundColor = '#FFFFFF';
	    				}
    					return;
    				}
    				for(var i = 0; i < store.getCount(); i++){
    					if(store.getAt(i).data.name.indexOf(txtTasteName) >= 0 ){
    						orderByData.push(store.getAt(i).data);	    						
    					}else{
    						otherData.push(store.getAt(i).data);
    					}
    				}
    				for(var i = 0; i < orderByData.length; i++){
    					searchData.root.push(orderByData[i]);
    					selIndex.push(i);
    				}
    				for(var i = 0; i < otherData.length; i++){
    					searchData.root.push(otherData[i]);
    				}
    				store.loadData(searchData);
    				for(var i = 0; i < searchData.root.length; i++){
    					var selRow = allTasteGrid.getView().getRow(i);
    					if(i < orderByData.length){
    						selRow.style.backgroundColor = '#FFFF00';
    					}else{
    						selRow.style.backgroundColor = '#FFFFFF';
    					}
    				}
    			};
    		}
    	}
	}]
});

var allTasteGrid = createGridPanel(
	'allTasteGrid',
	'<center>所有口味</center>',
	'',
	'',
	'../../QueryTaste.do',
	[
	    [true, false, false, false], 
	    ['口味名', 'name', 100] , 
	    ['价钱', 'price', 60, 'right', 'Ext.ux.txtFormat.gridDou'], 
	    ['比例', 'rate', 60, 'right', 'Ext.ux.txtFormat.gridDou'], 
	    ['计算方式', 'calcValue', '', '', 'tasteCalcRenderer']
	],
	TasteRecord.getKeys(),
	[ ['type',0], ['isCombo',false], ['isPaging',false]],
	0,
	'',
	allTasteGridTbar
);
allTasteGrid.columnWidth = .44;
allTasteGrid.on('render', function(thiz){
	thiz.getStore().load({
		params : {
			limit : 30,
			start : 0
		}
	});
});
allTasteGrid.on('resize', function(thiz, adjWidth, adjHeight, rawWidth, rawHeight){
	thiz.setHeight(tabItemsHeight);
});

allTasteGrid.on('rowdblclick', function(thiz, ri, e){
	var ctg = commonTasteGrid;
	var sr = thiz.getSelectionModel().getSelections()[0];
	var cv = true;
	ctg.getStore().each(function(r){
		if(r.get('taste')['id'] == sr.get('id')){
			cv = false;
		}
	});
	if(cv){
		ctg.getStore().insert(0, new FoodTasteRecord({
			'taste.id' : sr.get('id'),
			'taste.name' : sr.get('name'),
			'taste.rank' : 0,
			'taste.price' : sr.get('price'),
			'taste.rate' : sr.get('rate'),
			'taste.calcValue' : sr.get('calcValue'),
			taste : {
				id : sr.get('id'),
				name : sr.get('name'),
				rank : 0,
				price : sr.get('price'),
				rate : sr.get('rate'),
				calcValue : sr.get('calcValue')
			}
		}));
		ctg.getView().refresh();
		ctg.getSelectionModel().selectFirstRow();
	}else{
		Ext.example.msg('提示', '该菜品已关联口味<'+sr.get('name')+'>');
	}
});
allTasteGrid.getStore().on('load', function(e){	
	mmObj.allTasteGridData = e.data;
});

var tasteOperationPanel = new Ext.Panel({
	id : 'tasteOperationPanel',
	frame : true,
	border : false,
	layout : 'column',
	items : [
	    commonTasteGrid,
	    { xtype:'panel', columnWidth:.01, html:'&nbsp;'},
	    allTasteGrid
	],
	listeners : {
		render : function(e){
			
		}
	}
});

/**
 * 修改菜品关联口味
 */
updateTasteHandler = function(c){
	var foodID = c.data.id;
	var tasteContent = '';
	var ctg = commonTasteGrid.getStore();
	
	if(mmObj.rdoTasteType == c.data.tasteRefType && mmObj.rdoTasteType == 1){
		Ext.example.msg('提示', '智能关联方式无需修改!');
		ctg.load();
		return;
	}
	
	for(var i = 0; i < ctg.getCount(); i++){
		tasteContent += (i > 0 ? '<split>' : '');
		tasteContent += (ctg.getAt(i).get('taste')['id'] + ',' + ctg.getAt(i).get('taste')['rank']);
	}
	
	setButtonStateOne(true);
	Ext.Ajax.request({
		url : '../../UpdateFoodTaste.do',
		params : {
			foodID : foodID,
			restaurantID : restaurantID,
			nValue : mmObj.rdoTasteType,
			oValue : c.data.tasteRefType,
			tasteContent : tasteContent
		},
		success : function(response, options){
			var jr = Ext.util.JSON.decode(response.responseText);
			if(eval(jr.success)){
				Ext.example.msg(jr.title, jr.msg);
				if(c.hide == true){
					Ext.getCmp('foodOperationWin').hide();
				}
				ctg.load();
				Ext.getCmp('menuMgrGrid').getSelectionModel().getSelections()[0].set('tasteRefType', mmObj.rdoTasteType);
			}else{
				Ext.ux.showMsg(jr);
			}
			setButtonStateOne(false);
		},
		failure : function(response, options) {
			var jr = Ext.util.JSON.decode(response.responseText);
			Ext.ux.showMsg(jr);
			setButtonStateOne(false);
		}
	});
};


//-------------------------------
var basicOperationPanel = new Ext.Panel({
	frame : true,
	border : false,
	layout : 'fit',
	items : [{
		layout : 'column',
		height : 700,
		items : [{
			columnWidth : .35,
		 	layout : 'column',
		 	defaults : {
		 		xtype : 'panel',
		 		layout : 'form',
		 		labelWidth : 35
		 	},
		 	items : [{
		 		columnWidth : 1,
		 		items : [{
		 			xtype : 'textfield',
		 			id : 'txtBasicForFoodName',
		 			fieldLabel : '菜名',
		 			allowBlank : false,
		 			width : 255
		 		}]
		 	}, {
		 		columnWidth : .5,
		 		items : [{
		 			xtype : 'numberfield',
		 	    	id : 'numBasicForFoodAliasID',
		 	    	fieldLabel : '编号',
		 	    	maxValue : 65535,
		 	    	minValue : 1,
		 	    	allowBlank : false,
		 	    	width : 100,
		 	    	validator : function(v){
		 	    		if(v > 0 && v <= 65535 && v.indexOf('.') == -1){
		 	    	    	return true;
		 	    	    }else{
		 	    	    	return '编号需在 1  至 65535 之间,且为整数!';
		 	    	    }
		 	    	}
		 		}]
		 	}, {
		 		columnWidth : .5,
		 		items : [{
		 			xtype : 'textfield',
		 			id : 'txtBasicForPinyin',
		 			fieldLabel : '拼音',
		 			width : 103,
		 			validator : function(v){
		 				if(/^[a-zA-Z]+$/.test(v)){
		 					return true;
		 	    	    }else{
		 	    	    	return '只能输入拼音,不区分大小写!';
		 	    	    }
		 	    	}
		 		}]
		 	}, {
		 		columnWidth : .5,
		 	    items : [{
		 	    	xtype : 'numberfield',
		 	    	id : 'numBasicForPrice',
		 	    	style : 'text-align:right;',
		 	    	fieldLabel : '价格',
		 	    	decimalPrecision : 2,
		 	    	allowBlank : false,
		 	    	maxValue : 99999.99,
		 	    	minValue : 0.00,
		 	    	width : 100,
		 	    	validator : function(v){
		 	    		if(v >= 0.00 && v <= 99999.99){
		 	    	    	return true;
		 	    	    }else{
		 	    	    	return '价格需在 0.00  至 99999.99 之间!';
		 	    	    }
		 	    	}
		 	    }]
		 	}, {
		 		columnWidth : .5,
		 	    items : [{
		 	    	xtype : 'combo',
		 	       	id : 'cmbBasicForKitchenAlias',
		 	    	fieldLabel : '厨房',
		 	    	width : 86,
		 	    	listWidth : 99,
		 	    	store : new Ext.data.JsonStore({
						fields : [ 'alias', 'name' ]
					}),
					valueField : 'alias',
					displayField : 'name',
					mode : 'local',
					triggerAction : 'all',
					typeAhead : true,
					selectOnFocus : true,
					forceSelection : true,
					allowBlank : false,
					readOnly : true
		 	    }]
		 	}, {
		 		columnWidth : 1,
		 		xtype : 'panel',
		 		id : 'txtFoodPriceExplain',
		 		height : 20,
		 		html : '<a href="javascript:fppOperation()">说明:此价格对应当前活动的价格方案,点击查看其他方案</a>'
		 	}, {
		 		columnWidth : 1,
		 	    items : [{
		 	    	xtype : 'textarea',
		 	    	id : 'txtBasicForDesc',
		 	    	fieldLabel : '简介',
		 	    	width : 253,
		 	    	height : 150,
		 	    	maxLength : 500,
		 	    	maxLengthText : '简介最多输入500字,可以为空.'
		 	    }]
		 	}, {
		 		columnWidth : .13,
		 	    items : [{html:'状态:'}]
		 	}, {
		 		columnWidth : .2,
		 	    items : [{
		 	    	xtype : 'checkbox',
		 	    	id : 'chbForBasicSpecial',
		 	    	hideLabel : true,
		 	    	boxLabel : '<img title="特价" src="../../images/icon_tip_te.png"></img>'
		 	    }]
		 	}, {
		 		columnWidth : .2,
		 	    items : [{
		 	    	xtype : 'checkbox',
		 	    	id : 'chbForBasicRecommend',
		 	    	hideLabel : true,
		 	    	boxLabel : '<img title="推荐" src="../../images/icon_tip_jian.png"></img>'
		 	    }]
		 	}, {
		 		columnWidth : .2,
		 	    items : [{
		 	    	xtype : 'checkbox',
		 	    	id : 'chbForBasicFree',
		 	    	hideLabel : true,
		 	    	boxLabel : '<img title="赠送" src="../../images/forFree.png"></img>'
		 	    }]
		 	}, {
		 		columnWidth : .2,
		 	    items : [{
		 	    	xtype : 'checkbox',
		 	    	id : 'chbForBasicStop',
		 	    	hideLabel : true,
		 	    	boxLabel : '<img title="停售" src="../../images/icon_tip_ting.png"></img>'
		 	   	}]
		 	}, {
		 		columnWidth : .13,
		 	    items : [{html:'&nbsp;&nbsp;&nbsp;&nbsp;'}]
		 	}, {
		 		columnWidth : .2,
		 	    items : [{
		 	    	xtype : 'checkbox',
		 	    	id : 'chbForBasicCurrPrice',
		 	    	hideLabel : true,
		 	    	boxLabel : '<img title="时价" src="../../images/currPrice.png"></img>'
		 	    }]
		 	}, {
		 		columnWidth : .2,
		 	    items : [{
		 	    	xtype : 'checkbox',
		 	    	id : 'chbForBasicHot',
		 	    	hideLabel : true,
		 	    	boxLabel : '<img title="热销" src="../../images/hot.png"></img>'
		 	    }]
		 	}, {
		 		columnWidth : .2,
		 	    items : [{
		 	    	xtype : 'checkbox',
		 	    	id : 'chbForBasicWeight',
		 	    	hideLabel : true,
		 	    	boxLabel : '<img title="热销" src="../../images/weight.png"></img>'
		 	    }]
		 	}, {
		 		columnWidth : .5,
		 		labelWidth : 55,
		 		items : [{
		 	    	xtype : 'combo',
		 	    	id : 'comboBasicForStockStatus',
		 	    	fieldLabel : '库存管理',
		 	    	width : 66,
		 	    	listWidth : Ext.isIE ? 79 : 83,
		 	    	store : new Ext.data.SimpleStore({
						fields : ['value', 'text'],
						data : stockStatusData
					}),
					valueField : 'value',
					displayField : 'text',
					mode : 'local',
					triggerAction : 'all',
					typeAhead : true,
					selectOnFocus : true,
					forceSelection : true,
					allowBlank : false,
					readOnly : true
		 	    }]
		 	}]
		}, {
			columnWidth : .65,
			labelWidth : 60,
			items : [ new Ext.BoxComponent({
				xtype : 'box',
		 	    id : 'foodBasicImg',
		 	    name : 'foodBasicImg',
		 	    width : 555,
		 	    height : 420,
		 	    autoEl : {
		 	    	tag : 'img',
		 	    	title : '菜品图预览.',
		 	    	style : 'filter:progid:DXImageTransform.Microsoft.AlphaImageLoader(sizingMethod=scale); width:555; height:420; cursor:hand;'
		 	    }
			}), {
				tag : 'div',
		 	    height : 10
		 	}, {
		 		xtype : 'panel',
		 	    layout : 'column',
		 	    items : [{
		 	    	xtype : 'form',
		 	    	layout : 'form',
		 	    	labelWidth : 60,
		 	    	columnWidth : .7,
		 	    	url : '../../ImageFileUpload.do',
		 	    	id : 'imgFileUploadForm',
		 	    	fileUpload : true,
		 	    	items : [{}],
		 	    	listeners : {
		 	    		render : function(e){
		 	    			Ext.getDom(e.getId()).setAttribute('enctype', 'multipart/form-data');
			 	  		}
		 	    	}
		 	    }, {
		 	    	xtype : 'panel',
		 	    	columnWidth : .15,
		 	    	items : [{
		 	    		xtype : 'button',
		 	    		id : 'btnUploadFoodImage',
		 	        	text : '上传图片',
		 	        	handler : function(e){
		 	        		var check = true;
		 	        		var img = '';
		 	        		if(Ext.isIE){
		 	        			var file = Ext.getDom('txtImgFile');
		 	        			file.select();
		 	        			img = document.selection.createRange().text;
		 	        		}else{
		 	        			var file = Ext.getDom('txtImgFile'); 
				 	        	img = file.value;
		 	        		}
		 	        		if(typeof(img) != 'undefined' && img.length > 0){
		 	        			var index = img.lastIndexOf('.');
				 	        	var type = img.substring(index+1, img.length);
				 	        	check = false;
				 	        	for(var i = 0; i < imgTypeTmp.length; i++){
				 	        		if(type.toLowerCase() == imgTypeTmp[i].toLowerCase()){
				 	        			check = true;
					 	        	   	break;
					 	        	}
				 	        	}
		 	        		}else{
		 	        			check = false;
		 	        		}
		 	        		 
		 	        		if(check){
		 	        			var selData = Ext.ux.getSelData('menuMgrGrid');
				 	        	selData.arrt = {
				 	        	    type : mmObj.operation.img.upload
				 	        	};
				 	        	uploadFoodImage(selData);
		 	        		}else{
		 	        			Ext.example.msg('提示', '上传图片失败,未选择图片或图片类型正确.');
		 	        		}
		 	        	 }
		 	    	 }]
		 	    }, {
		 	    	xtype : 'panel',
		 	    	columnWidth : .15,
		 	    	items : [{
		 	    		xtype : 'button',
		 	        	id : 'btnDeleteFoodImage',
		 	        	text : '删除图片',
		 	        	handler : function(e){
		 	        		var selData = Ext.ux.getSelData('menuMgrGrid');
		 	        		if(!selData)
		 	        			return;
		 	        	    
		 	        		if(selData.img.indexOf('nophoto.jpg') != -1){
		 	        			Ext.example.msg('提示', '该菜品没有图片,无需删除.');
		 	        			return;
		 	        		}
		 	        		 
		 	        		Ext.Msg.confirm('提示', '是否确定删除菜品图片?', function(e){
		 	        			if(e == 'yes'){
		 	        				selData.arrt = {
		 	        				    type : mmObj.operation.img.del
			 	       				};
		 	        				uploadFoodImage(selData);
		 	        			 }
		 	        		}, this);
		 	        	}
		 	    	}]
		 		}]
			}]
		}]
	}]
});
//----------------basic
var basicOperationPanel = new Ext.Panel({
	frame : true,
	border : false,
	layout : 'fit',
	items : [{
		layout : 'column',
		items : [{
			columnWidth : .35,
		 	layout : 'column',
		 	defaults : {
		 		xtype : 'panel',
		 		layout : 'form',
		 		labelWidth : 30
		 	},
		 	items : [{
		 		columnWidth : 1,
		 		items : [{
		 			xtype : 'textfield',
		 			id : 'txtBasicForFoodName',
		 			fieldLabel : '菜名',
		 			allowBlank : false,
		 			width : 255
		 		}]
		 	}, {
		 		columnWidth : .5,
		 		items : [{
		 			xtype : 'numberfield',
		 	    	id : 'numBasicForFoodAliasID',
		 	    	fieldLabel : '编号',
		 	    	maxValue : 65535,
		 	    	minValue : 1,
		 	    	allowBlank : false,
		 	    	width : 100,
		 	    	validator : function(v){
		 	    		if(v > 0 && v <= 65535 && v.indexOf('.') == -1){
		 	    	    	return true;
		 	    	    }else{
		 	    	    	return '编号需在 1  至 65535 之间,且为整数!';
		 	    	    }
		 	    	}
		 		}]
		 	}, {
		 		columnWidth : .5,
		 		items : [{
		 			xtype : 'textfield',
		 			id : 'txtBasicForPinyin',
		 			fieldLabel : '拼音',
		 			width : 103,
		 			validator : function(v){
		 				if(/^[a-zA-Z]+$/.test(v)){
		 					return true;
		 	    	    }else{
		 	    	    	return '只能输入拼音,不区分大小写!';
		 	    	    }
		 	    	}
		 		}]
		 	}, {
		 		columnWidth : .5,
		 	    items : [{
		 	    	xtype : 'numberfield',
		 	    	id : 'numBasicForPrice',
		 	    	style : 'text-align:right;',
		 	    	fieldLabel : '价格',
		 	    	decimalPrecision : 2,
		 	    	allowBlank : false,
		 	    	maxValue : 99999.99,
		 	    	minValue : 0.00,
		 	    	width : 100,
		 	    	validator : function(v){
		 	    		if(v >= 0.00 && v <= 99999.99){
		 	    	    	return true;
		 	    	    }else{
		 	    	    	return '价格需在 0.00  至 99999.99 之间!';
		 	    	    }
		 	    	}
		 	    }]
		 	}, {
		 		columnWidth : .5,
		 	    items : [{
		 	    	xtype : 'combo',
		 	       	id : 'cmbBasicForKitchenAlias',
		 	    	fieldLabel : '厨房',
		 	    	width : 86,
		 	    	listWidth : 99,
		 	    	store : new Ext.data.JsonStore({
						fields : [ 'alias', 'name' ]
					}),
					valueField : 'alias',
					displayField : 'name',
					mode : 'local',
					triggerAction : 'all',
					typeAhead : true,
					selectOnFocus : true,
					forceSelection : true,
					allowBlank : false,
					readOnly : true
		 	    }]
		 	}, {
		 		columnWidth : 1,
		 		xtype : 'panel',
		 		id : 'txtFoodPriceExplain',
		 		height : 20,
		 		html : '<a href="javascript:fppOperation()">说明:此价格对应当前活动的价格方案,点击查看其他方案</a>'
		 	}, {
		 		columnWidth : 1,
		 	    items : [{
		 	    	xtype : 'textarea',
		 	    	id : 'txtBasicForDesc',
		 	    	fieldLabel : '简介',
		 	    	width : 253,
		 	    	height : 150,
		 	    	maxLength : 500,
		 	    	maxLengthText : '简介最多输入500字,可以为空.'
		 	    }]
		 	}, {
		 		columnWidth : .13,
		 	    items : [{html:'状态:'}]
		 	}, {
		 		columnWidth : .2,
		 	    items : [{
		 	    	xtype : 'checkbox',
		 	    	id : 'chbForBasicSpecial',
		 	    	hideLabel : true,
		 	    	boxLabel : '<img title="特价" src="../../images/icon_tip_te.png"></img>'
		 	    }]
		 	}, {
		 		columnWidth : .2,
		 	    items : [{
		 	    	xtype : 'checkbox',
		 	    	id : 'chbForBasicRecommend',
		 	    	hideLabel : true,
		 	    	boxLabel : '<img title="推荐" src="../../images/icon_tip_jian.png"></img>'
		 	    }]
		 	}, {
		 		columnWidth : .2,
		 	    items : [{
		 	    	xtype : 'checkbox',
		 	    	id : 'chbForBasicFree',
		 	    	hideLabel : true,
		 	    	boxLabel : '<img title="赠送" src="../../images/forFree.png"></img>'
		 	    }]
		 	}, {
		 		columnWidth : .2,
		 	    items : [{
		 	    	xtype : 'checkbox',
		 	    	id : 'chbForBasicStop',
		 	    	hideLabel : true,
		 	    	boxLabel : '<img title="停售" src="../../images/icon_tip_ting.png"></img>'
		 	   	}]
		 	}, {
		 		columnWidth : .13,
		 	    items : [{html:'&nbsp;&nbsp;&nbsp;&nbsp;'}]
		 	}, {
		 		columnWidth : .2,
		 	    items : [{
		 	    	xtype : 'checkbox',
		 	    	id : 'chbForBasicCurrPrice',
		 	    	hideLabel : true,
		 	    	boxLabel : '<img title="时价" src="../../images/currPrice.png"></img>'
		 	    }]
		 	}, {
		 		columnWidth : .2,
		 	    items : [{
		 	    	xtype : 'checkbox',
		 	    	id : 'chbForBasicHot',
		 	    	hideLabel : true,
		 	    	boxLabel : '<img title="热销" src="../../images/hot.png"></img>'
		 	    }]
		 	}, {
		 		columnWidth : .2,
		 	    items : [{
		 	    	xtype : 'checkbox',
		 	    	id : 'chbForBasicWeight',
		 	    	hideLabel : true,
		 	    	boxLabel : '<img title="热销" src="../../images/weight.png"></img>'
		 	    }]
		 	}, {
		 		columnWidth : .5,
		 		labelWidth : 55,
		 		items : [{
		 	    	xtype : 'combo',
		 	    	id : 'comboBasicForStockStatus',
		 	    	fieldLabel : '库存管理',
		 	    	width : 66,
		 	    	listWidth : Ext.isIE ? 79 : 83,
		 	    	store : new Ext.data.SimpleStore({
						fields : ['value', 'text'],
						data : stockStatusData
					}),
					valueField : 'value',
					displayField : 'text',
					mode : 'local',
					triggerAction : 'all',
					typeAhead : true,
					selectOnFocus : true,
					forceSelection : true,
					allowBlank : false,
					readOnly : true
		 	    }]
		 	}]
		}, {
			columnWidth : .65,
			labelWidth : 60,
			items : [ new Ext.BoxComponent({
				xtype : 'box',
		 	    id : 'foodBasicImg',
		 	    name : 'foodBasicImg',
		 	    width : 555,
		 	    height : 420,
		 	    autoEl : {
		 	    	tag : 'img',
		 	    	title : '菜品图预览.',
		 	    	style : 'filter:progid:DXImageTransform.Microsoft.AlphaImageLoader(sizingMethod=scale); width:555; height:420; cursor:hand;'
		 	    }
			}), {
				tag : 'div',
		 	    height : 10
		 	}, {
		 		xtype : 'panel',
		 	    layout : 'column',
		 	    items : [{
		 	    	xtype : 'form',
		 	    	layout : 'form',
		 	    	labelWidth : 60,
		 	    	columnWidth : .7,
		 	    	url : '../../ImageFileUpload.do',
		 	    	id : 'imgFileUploadForm',
		 	    	fileUpload : true,
		 	    	items : [{}],
		 	    	listeners : {
		 	    		render : function(e){
		 	    			Ext.getDom(e.getId()).setAttribute('enctype', 'multipart/form-data');
			 	  		}
		 	    	}
		 	    }, {
		 	    	xtype : 'panel',
		 	    	columnWidth : .15,
		 	    	items : [{
		 	    		xtype : 'button',
		 	    		id : 'btnUploadFoodImage',
		 	        	text : '上传图片',
		 	        	handler : function(e){
		 	        		var check = true;
		 	        		var img = '';
		 	        		if(Ext.isIE){
		 	        			var file = Ext.getDom('txtImgFile');
		 	        			file.select();
		 	        			img = document.selection.createRange().text;
		 	        		}else{
		 	        			var file = Ext.getDom('txtImgFile'); 
				 	        	img = file.value;
		 	        		}
		 	        		if(typeof(img) != 'undefined' && img.length > 0){
		 	        			var index = img.lastIndexOf('.');
				 	        	var type = img.substring(index+1, img.length);
				 	        	check = false;
				 	        	for(var i = 0; i < imgTypeTmp.length; i++){
				 	        		if(type.toLowerCase() == imgTypeTmp[i].toLowerCase()){
				 	        			check = true;
					 	        	   	break;
					 	        	}
				 	        	}
		 	        		}else{
		 	        			check = false;
		 	        		}
		 	        		 
		 	        		if(check){
		 	        			var selData = Ext.ux.getSelData('menuMgrGrid');
				 	        	selData.arrt = {
				 	        	    type : mmObj.operation.img.upload
				 	        	};
				 	        	uploadFoodImage(selData);
		 	        		}else{
		 	        			Ext.example.msg('提示', '上传图片失败,未选择图片或图片类型正确.');
		 	        		}
		 	        	 }
		 	    	 }]
		 	    }, {
		 	    	xtype : 'panel',
		 	    	columnWidth : .15,
		 	    	items : [{
		 	    		xtype : 'button',
		 	        	id : 'btnDeleteFoodImage',
		 	        	text : '删除图片',
		 	        	handler : function(e){
		 	        		var selData = Ext.ux.getSelData('menuMgrGrid');
		 	        		if(!selData)
		 	        			return;
		 	        	    
		 	        		if(selData.img.indexOf('nophoto.jpg') != -1){
		 	        			Ext.example.msg('提示', '该菜品没有图片,无需删除.');
		 	        			return;
		 	        		}
		 	        		 
		 	        		Ext.Msg.confirm('提示', '是否确定删除菜品图片?', function(e){
		 	        			if(e == 'yes'){
		 	        				selData.arrt = {
		 	        				    type : mmObj.operation.img.del
			 	       				};
		 	        				uploadFoodImage(selData);
		 	        			 }
		 	        		}, this);
		 	        	}
		 	    	}]
		 		}]
			}]
		}]
	}]
});

/**
 * 界面赋值
 */
function resetbBasicOperation(_d){
	var foodName = Ext.getCmp('txtBasicForFoodName');
	var foodAliasID = Ext.getCmp('numBasicForFoodAliasID');
	var foodPinyin = Ext.getCmp('txtBasicForPinyin');
	var foodPrice = Ext.getCmp('numBasicForPrice');
	var foodKitchenAlias = Ext.getCmp('cmbBasicForKitchenAlias');
	var foodDesc = Ext.getCmp('txtBasicForDesc');
	var isSpecial = Ext.getCmp('chbForBasicSpecial');
	var isRecommend = Ext.getCmp('chbForBasicRecommend');
	var isFree = Ext.getCmp('chbForBasicFree');
	var isStop = Ext.getCmp('chbForBasicStop');
	var isCurrPrice = Ext.getCmp('chbForBasicCurrPrice');
	var isHot = Ext.getCmp('chbForBasicHot');
	var isWeight = Ext.getCmp('chbForBasicWeight');
	var img = Ext.getDom('foodBasicImg');
	var btnUploadFoodImage = Ext.getCmp('btnUploadFoodImage');
	var btnDeleteFoodImage = Ext.getCmp('btnDeleteFoodImage');
	var stockStatus = Ext.getCmp('comboBasicForStockStatus');
	
	var data = {};
	
	// 清空图片信息
	refreshFoodImageMsg();
	
	var imgFile = Ext.getCmp('txtImgFile');
	
	if(_d != null && typeof(_d) != 'undefined'){
		data = _d;
		foodAliasID.setDisabled(true);
		btnUploadFoodImage.setDisabled(false);
		btnDeleteFoodImage.setDisabled(false);
		imgFile.setDisabled(false);
	}else{
		data = {};
		foodAliasID.setDisabled(false);
		btnUploadFoodImage.setDisabled(true);
		btnDeleteFoodImage.setDisabled(true);
		imgFile.setDisabled(true);
	}
	
	if(foodKitchenAlias.store.getCount() == 0){
		foodKitchenAlias.store.loadData(kitchenData);
	}
	
	var status = typeof(data.status) == 'undefined' ? 0 : parseInt(data.status);
	
	foodName.setValue(data.name);
	foodAliasID.setValue(data.alias);
	foodPinyin.setValue(data.pinyin);
	foodPrice.setValue(data.unitPrice);
	foodKitchenAlias.setValue(data['kitchen.alias']);
	foodDesc.setValue(data.desc);
	isSpecial.setValue(Ext.ux.cfs.isSpecial(status));
	isRecommend.setValue(Ext.ux.cfs.isRecommend(status));
	isStop.setValue(Ext.ux.cfs.isStop(status));
	isFree.setValue(Ext.ux.cfs.isGift(status));
	isCurrPrice.setValue(Ext.ux.cfs.isCurrPrice(status));
	isHot.setValue(Ext.ux.cfs.isHot(status));
	isWeight.setValue(Ext.ux.cfs.isWeigh(status));
	img.src = typeof(data.img) == 'undefined' || data.img == '' ? '../../images/nophoto.jpg' : data.img;
	stockStatus.setValue(typeof(data.stockStatusValue) == 'undefined' ? 1 : data.stockStatusValue);
	
	foodName.clearInvalid();
	foodAliasID.clearInvalid();
	foodPinyin.clearInvalid();
	foodPrice.clearInvalid();
	stockStatus.clearInvalid();
	foodKitchenAlias.clearInvalid();
};

/**
 * 添加菜品基础信息
 */
function addBasicHandler(){
	basicOperationBasicHandler({
		type : mmObj.operation.insert
	});
};

/**
 * 修改菜品基础信息
 */
function updateBasicHandler(c){
	c.type = mmObj.operation.update;
	basicOperationBasicHandler(c);
};

/**
 * 操作菜品基础信息,目前支持添加和修改操作
 */
function basicOperationBasicHandler(c){
	
	if(c == null || typeof(c) == 'undefined' || typeof(c.type) == 'undefined'){
		return;
	}
	
	var actionURL = '';
	if(c.type == mmObj.operation.insert){
		actionURL = '../../InsertMenu.do';
	}else if(c.type == mmObj.operation.update){
		actionURL = '../../UpdateMenu.do';
	}else{
		return;
	}
	
	var foodName = Ext.getCmp('txtBasicForFoodName');
	var foodAliasID = Ext.getCmp('numBasicForFoodAliasID');
	var foodPinyin = Ext.getCmp('txtBasicForPinyin');
	var foodPrice = Ext.getCmp('numBasicForPrice');
	var foodKitchenAlias = Ext.getCmp('cmbBasicForKitchenAlias');
	var foodDesc = Ext.getCmp('txtBasicForDesc');
	var isSpecial = Ext.getCmp('chbForBasicSpecial');
	var isRecommend = Ext.getCmp('chbForBasicRecommend');
	var isFree = Ext.getCmp('chbForBasicFree');
	var isStop = Ext.getCmp('chbForBasicStop');
	var isCurrPrice = Ext.getCmp('chbForBasicCurrPrice');
	var isHot = Ext.getCmp('chbForBasicHot');
	var isWeight = Ext.getCmp('chbForBasicWeight');
	var isCombination = false;
	var comboContent = '';
	var kitchenID = '';
	var cfg = Ext.getCmp('combinationFoodGrid');
	var stockStatus = Ext.getCmp('comboBasicForStockStatus');
		
	if(!foodName.isValid() || !foodPinyin.isValid() || !foodAliasID.isValid() || !foodPrice.isValid() || !foodKitchenAlias.isValid()){
		Ext.getCmp('foodOperationWinTab').setActiveTab('basicOperationTab');
		return;
	}
	
	if(c.type == mmObj.operation.insert){
		if(cfg.getStore().getCount() > 0){
			isCombination = true;
			for(var i = 0; i < cfg.getStore().getCount(); i++){
				comboContent += (i > 0 ? '<split>' : '');
				comboContent += (cfg.getStore().getAt(i).get('foodID') + ',' + cfg.getStore().getAt(i).get('amount'));
			}
		}
	}else if(c.type == mmObj.operation.update){
		isCombination = (typeof(c.data) != 'undefined' && typeof(c.data.combination) != 'undefined' ? c.data.combination : false);
	}
	
	for(var i = 0; i < kitchenData.length; i++){
		if(kitchenData[i].alias == foodKitchenAlias.getValue()){
			kitchenID = kitchenData[i].id;
			break;
		}
	}
	
	if(c.type == mmObj.operation.insert){
		Ext.getCmp('btnAddForOW').setDisabled(true);
		Ext.getCmp('btnCloseForOW').setDisabled(true);
		Ext.getCmp('btnRefreshForOW').setDisabled(true);
	}else if(c.type == mmObj.operation.update){
		setButtonStateOne(true);
	}
	
	Ext.Ajax.request({
		url : actionURL,
		params : {
			
			restaurantID : restaurantID,
			foodID : (typeof(c.data) != 'undefined' && typeof(c.data.id) != 'undefined' ? c.data.id : 0),
			foodName : foodName.getValue().trim(),
			foodAliasID : foodAliasID.getValue(),
			foodPinyin : foodPinyin.getValue(),
			foodPrice : foodPrice.getValue(),
			kitchenAliasID : foodKitchenAlias.getValue(),
			kitchenID : kitchenID,
			foodDesc : foodDesc.getValue().trim(),
			isSpecial : isSpecial.getValue(),
			isRecommend : isRecommend.getValue(),
			isFree : isFree.getValue(),
			isStop : isStop.getValue(),
			isCurrPrice : isCurrPrice.getValue(),
			isHot : isHot.getValue(),
			isCombination : isCombination,
			isWeight : isWeight.getValue(),
			comboContent : comboContent,
			stockStatus : stockStatus.getValue()
		},
		success : function(res, opt){
			var jr = Ext.util.JSON.decode(res.responseText);
			
			if(jr.success == true){
				if(c.type == mmObj.operation.insert){
					Ext.Msg.confirm(jr.titile, jr.msg + '\n是否继续添加?', function(e){
						if(e == 'yes'){
							var faid = foodAliasID.getValue();
							var kaid = foodKitchenAlias.getValue();	
							
							resetbBasicOperation();
							Ext.getCmp('combinationFoodGrid').getStore().removeAll();
							Ext.getCmp('txtMiniAllFoodNameSearch').setValue('');
							Ext.getCmp('btnSearchForAllFoodMiniGridTbar').handler();
							
							foodAliasID.setValue(parseInt(faid+1));
							foodKitchenAlias.setValue(kaid);
						}else{
							Ext.getCmp('foodOperationWin').hide();
						}
					}, this);
				}else if(c.type == mmObj.operation.update){
					if(c.hide == true){
						Ext.getCmp('foodOperationWin').hide();
					}
					Ext.example.msg(jr.title, jr.msg);
					foodName.setValue(foodName.getValue().trim());
					foodDesc.setValue(foodDesc.getValue().trim());
					Ext.getCmp('menuMgrGrid').getStore().each(function(record){
						if(record.get('id') == c.data.foodID){
							record.set('name', foodName.getValue().trim());
							record.set('pinyin', foodPinyin.getValue());
							record.set('unitPrice', foodPrice.getValue());
							record.set('kitchen.id', kitchenID);
							record.set('kitchen.name', foodKitchenAlias.getRawValue());
							record.set('kitchen.alias', foodKitchenAlias.getValue());
							record.set('desc', foodDesc.getValue().trim());
							record.set('special', isSpecial.getValue());
							record.set('recommend', isRecommend.getValue());
							record.set('gift', isFree.getValue());
							record.set('stop', isStop.getValue());
							record.set('currPrice', isCurrPrice.getValue());
							record.set('hot', isHot.getValue());
							record.set('weight', isWeight.getValue());
							Ext.ux.formatFoodName(record, 'displayFoodName', 'name');
							record.commit();
							return;
						}
					});
				}
			}else{
				Ext.ux.showMsg(jr);
			}
			
			if(c.type == mmObj.operation.insert){
				Ext.getCmp('btnAddForOW').setDisabled(false);
				Ext.getCmp('btnCloseForOW').setDisabled(false);
				Ext.getCmp('btnRefreshForOW').setDisabled(false);
			}else if(c.type == mmObj.operation.update){
				setButtonStateOne(false);
			}
			
		},
		failure : function(res, opt) {
			Ext.ux.showMsg(Ext.util.JSON.decode(res.responseText));
			if(c.type == mmObj.operation.insert){
				Ext.getCmp('btnAddForOW').setDisabled(false);
				Ext.getCmp('btnCloseForOW').setDisabled(false);
				Ext.getCmp('btnRefreshForOW').setDisabled(false);
			}else if(c.type == mmObj.operation.update){
				setButtonStateOne(false);
			}
		}
	});
	
};

/**
 * 图片操作
 */
function uploadFoodImage(c){
	var otype = null;
	if(typeof(c.arrt) == 'undefined' || typeof(c.arrt.type) == 'undefined'){
		Ext.example.msg('提示', '操作失败, 获取图片操作类型失败, 请联系客服人员.');
		return;
	}
	if(c.arrt.type == mmObj.operation.img.upload){
		otype = 0;
	}else if(c.arrt.type == mmObj.operation.img.del){
		otype = 1;
	}else{
		Ext.example.msg('提示', '操作失败, 获取图片操作类型失败, 请联系客服人员.');
		return;
	}
	
	var btnUploadFoodImage = Ext.getCmp('btnUploadFoodImage');
	var btnDeleteFoodImage = Ext.getCmp('btnDeleteFoodImage');
	
	btnUploadFoodImage.setDisabled(true);
	btnDeleteFoodImage.setDisabled(true);
	setButtonStateOne(true);
	
	if(!foodImageUpdateLoaddingMask){
		foodImageUpdateLoaddingMask = new Ext.LoadMask(document.body, {
			msg  : '图片操作中，请稍候......'
		});
	}
	foodImageUpdateLoaddingMask.show();		
	
	Ext.getCmp('imgFileUploadForm').getForm().submit({
		url : '../../ImageFileUpload.do?restaurantID=' + restaurantID + '&foodID=' + c.id + '&otype=' + otype + '&time=' + new Date(), 
		success : function(thiz, result){
			foodImageUpdateLoaddingMask.hide();
			
			var jr = Ext.decode(result.response.responseText);
			if(eval(jr.success)){
				Ext.example.msg(jr.title, jr.msg);
				Ext.getCmp('menuMgrGrid').getStore().each(function(record){
					if(record.get('id') == c.id){
						record.set('img', jr.root[0].img);
						record.commit();
						return;
					}
				});
				
				if(typeof(c.arrt) != 'undefined' && typeof(c.arrt.type) != 'undefined' && c.arrt.type == mmObj.operation.img.del)
					refreshFoodImageMsg();
				
			}else{
				Ext.ux.showMsg(jr);
			}
			btnUploadFoodImage.setDisabled(false);
			btnDeleteFoodImage.setDisabled(false);
			setButtonStateOne(false);
		},
		failure : function(thiz, result){
			foodImageUpdateLoaddingMask.hide();
			btnUploadFoodImage.setDisabled(false);
			btnDeleteFoodImage.setDisabled(false);
			setButtonStateOne(false);
			Ext.ux.showMsg(Ext.decode(result.response.responseText));
		}
	});
};

/**
 * 
 */
function refreshFoodImageMsg(){
	var img = Ext.getDom('foodBasicImg');
	var imgFile = Ext.getCmp('txtImgFile');
	var imgForm = Ext.getCmp('imgFileUploadForm');
	img.src = '../../images/nophoto.jpg';
	if(imgFile){
		imgForm.remove('txtImgFile');
	}
	imgFile = getImageFile();
	imgForm.add(imgFile);
	imgForm.doLayout(true);
	//Ext.getCmp('testimg').doLayout();
};

/**
 * 
 */
function getImageFile(){
	var img = new Ext.form.TextField({
		xtype : 'textfield',
		id : 'txtImgFile',
		name : 'txtImgFile',
		fieldLabel : '选择图片',
		height : 22,
		inputType : 'file',
		listeners : {
			render : function(e){
				if(Ext.isIE){
					e.el.dom.size = 45;
				}else{
					e.el.dom.size = 40;
				}
				Ext.get('txtImgFile').on('change', function(){
					try{
						if(Ext.isIE){
	 						var img = Ext.getDom('foodBasicImg');
	 						var file = Ext.getDom('txtImgFile');
	 						file.select();
	 						var imgURL = document.selection.createRange().text;
	 						if(imgURL && imgURL.length > 0){
	 							var index = imgURL.lastIndexOf('.');
 	        	    			var type = imgURL.substring(index+1, img.length);
 	        	    			var check = false;
 	        	    			for(var i = 0; i < imgTypeTmp.length; i++){
 	        	    				if(type.toLowerCase() == imgTypeTmp[i].toLowerCase()){
 	        	    					check = true;
 	        	    					break;
 	        	    				}
 	        	    			}
 	        	    			if(check){
 	        	    				img.src = Ext.BLANK_IMAGE_URL;
 	        	    				img.filters.item("DXImageTransform.Microsoft.AlphaImageLoader").src = imgURL;								 	        	    				
 	        	    			}else{
 	        	    				file.select();
 	        	    				document.execCommand('Delete');
 	        	    				Ext.example.msg('提示', '操作失败,选择的图片类型不正确,请重新选择!');
 	        	    			}
	 						}
	 					}else{
	 						var img = Ext.getDom('foodBasicImg');
	 						var file = Ext.getDom('txtImgFile'); 
	 						if(file.files && file.files[0]){
	 							var index = file.value.lastIndexOf('.');
 	        	    			var type = file.value.substring(index+1, img.length);
 	        	    			var check = false;
 	        	    			for(var i = 0; i < imgTypeTmp.length; i++){
 	        	    				if(type.toLowerCase() == imgTypeTmp[i].toLowerCase()){
 	        	    					check = true;
 	        	    					break;
 	        	    				}
 	        	    			}
 	        	    			if(check){
 	        	    				var reader = new FileReader();
 	        	    				reader.onload = function(evt){img.src = evt.target.result;};
 	        	    				reader.readAsDataURL(file.files[0]);
 	        	    			}else{
 	        	    				file.value = '';
 	        	    				Ext.example.msg('提示', '操作失败,选择的图片类型不正确,请重新选择!');
 	        	    			}
	 						}
	 					}
					} catch(e){
						Ext.example.msg('提示', '操作失败,无法获取图片信息.请换浏览器后重试.');
					}
				}, this);
			}
		}
	});
	return img;
};

//------------------------------------------------------------------
function operationFoodPricePlanData(c){
	if(c == null || c.type == null || typeof c.type == 'undefined')
		return;
	var data = {};
	var foodID = Ext.getCmp('hideFoodPricePlanID');
	var pricePlanID = Ext.getCmp('hidePricePlanID');
	var pricePlanName = Ext.getCmp('txtFoodPricePlanName');
	var unitPrice = Ext.getCmp('numFoodPricePlanUnitPrice');
	if(c.type == mmObj.operation['set']){
		data = c.data == null || typeof c.data == 'undefined' ? {} : c.data;
		foodID.setValue(data['foodId']);
		pricePlanID.setValue(data['planId']);
		pricePlanName.setValue(data['pricePlan.name']);
		unitPrice.setValue(data['unitPrice']);
	}else if(c.type == mmObj.operation['get']){
		data = {
			restaurantID : restaurantID,
			planId : pricePlanID.getValue(),
			foodId : foodID.getValue(),
			unitPrice : unitPrice.getValue()
		};
		c.data = data;
	}
	unitPrice.clearInvalid();
	return c;
};

function fppGridUpdateHandler(){
	var sd = Ext.ux.getSelData('fppGrid');
	if(!sd){
		Ext.example.msg('提示', '请选择一个方案价格再进行操作.');
		return;
	}
	Ext.getCmp('fppOperationPanel').show();
	foodPricePlanWin.doLayout();
	operationFoodPricePlanData({
		type : mmObj.operation['set'],
		data : sd
	});
};

function fppGridOperationRenderer(){
	return '<a href="javascript:fppGridUpdateHandler()">修改</>';
};

function fppOperation(){
	if(!foodPricePlanWin){
		var fppGridTbar = new Ext.Toolbar({
			height : 26,
			items : ['->', {
				text : '刷新',
				id : 'btnRefreshFPPGrid',
				iconCls : 'btn_refresh',
				handler : function(){
					var gs = fppGrid.getStore();
					gs.removeAll();
					
					oPanel.hide();
					foodPricePlanWin.doLayout();
					
					gs.baseParams['searchValue'] = foodPricePlanWin.foodData['alias'];
					gs.load();
				}
			}, {
				text : '修改',
				iconCls : 'btn_edit',
				handler : function(){
					fppGridUpdateHandler();
				}
			}]
		});
		fppGrid = createGridPanel(
			'fppGrid',
			'',
			'',
			'',
			'../../QueryFoodPricePlan.do',
			[
				[true, false, false, false], 
				['方案', 'pricePlan.name'],
				['价格', 'unitPrice', 60, 'right', 'Ext.ux.txtFormat.gridDou'],
				['操作', 'operation', 80, 'center', 'fppGridOperationRenderer']
			],
			FoodPricePlan.getKeys(),
			[['restaurantID', restaurantID], ['searchType', 1], ['searchValue', 0]],
			0,
			'',
			fppGridTbar
		);
		fppGrid.region = 'center';
		
		oPanel = new Ext.Panel({
			id : 'fppOperationPanel',
			frame : true,
			region : 'south',
			layout : 'column',
			autoHeight : true,
			defaults : {
				xtype : 'form',
				layout : 'form',
				labelWidth : 35
			},
			items : [{
				xtype : 'hidden',
				id : 'hidePricePlanID'
			}, {
				xtype : 'hidden',
				id : 'hideFoodPricePlanID'
			}, {
				columnWidth : .5,
				items : [{
					xtype : 'textfield',
					id : 'txtFoodPricePlanName',
					fieldLabel : '方案',
					width : 100,
					disabled : true
				}]
			}, {
				columnWidth : .5,
				items : [{
					xtype : 'numberfield',
					id : 'numFoodPricePlanUnitPrice',
					fieldLabel : '价格',
					width : 100,
					allowBlank : false,
					blankText : '价格不能为空.',
					validator : function(v){
						if(v >= 0 && v < 65535){
							return true;
						}else{
							return '价格不能为空.';
						}
					}
				}]
			}],
			buttonAlign : 'center',
			buttons : [{
				text : '保存',
				handler : function(){
					var unitPrice = Ext.getCmp('numFoodPricePlanUnitPrice');
					if(!unitPrice.isValid()){
						return;
					}
					var foodPricePlan = operationFoodPricePlanData({
						type : mmObj.operation['get']
					}).data;
					
					Ext.Ajax.request({
						url : '../../UpdateFoodPricePlan.do',
						params : {
							foodPricePlan : Ext.encode(foodPricePlan)
						},
						success : function(res, opt){
							var jr = Ext.util.JSON.decode(res.responseText);
							if(jr.success){
								Ext.example.msg(jr.title, jr.msg);
								Ext.getCmp('btnRefreshFPPGrid').handler();
							}else{
								Ext.ux.showMsg(jr);
							}
						},
						failure : function(res, opt){
							Ext.ux.showMsg(Ext.decode(res.responseText));
						}
					});
				}
			}, {
				text : '取消',
				id : 'btnCloseFoodPricePlanPanel',
				handler : function(){
					oPanel.hide();
					foodPricePlanWin.doLayout();
				}
			}]
		});
		
		foodPricePlanWin = new Ext.Window({
			title : '&nbsp;',
			modal : true,
			resizable : false,
			closable : false,
			draggable : false,
			width : 350,
			height : 390,
			layout : 'border',
			items : [fppGrid, oPanel],
			bbar : ['->', {
				text : '关闭',
				iconCls : 'btn_close',
				handler : function(){
					foodPricePlanWin.hide();
				}
			}],
			keys : [{
				key : Ext.EventObject.ESC,
				scope : this,
				fn : function(){
					foodPricePlanWin.hide();
				}
			}],
			listeners : {
				beforeshow : function(){
					oPanel.hide();
				},
				show : function(){
					Ext.getCmp('btnRefreshFPPGrid').handler();
				},
				hide : function(){
					fppGrid.getStore().removeAll();
				}
			}
		});
	}
	var sd = Ext.ux.getSelData(menuGrid);
	foodPricePlanWin.foodData = sd;
	foodPricePlanWin.setTitle(sd['name']+' -- 所有价格方案信息');
	foodPricePlanWin.show();
	
};
//------------------
//-----------load----------------
function initKitchenTreeForSreach(){
	kitchenTreeForSreach = new Ext.tree.TreePanel({
		region : 'west',
		frame : true,
		width : 200,
		border : true,
		rootVisible : true,
		autoScroll : true,
		frame : true,
		bodyStyle : 'backgroundColor:#FFFFFF; border:1px solid #99BBE8;',
		root : new Ext.tree.TreeNode({
			text : '全部',
			alias : -1,
			expanded : true
		}),
		tbar : ['->', {
			text : '刷新',
			iconCls : 'btn_refresh',
			handler : function(){
				Ext.getDom('showTypeForSearchKitchen').innerHTML = '----';
				var root = kitchenTreeForSreach.getRootNode();
				for(var i = root.childNodes.length - 1; i >= 0 ; i--){
					root.childNodes[i].remove();
				}
				for(var i = 0; i < kitchenData.length; i++){
					root.appendChild(new Ext.tree.TreeNode({
						text : kitchenData[i].name,
						alias : kitchenData[i].alias
					}));
				}
				root.expand();
			}
		}],
		listeners : {
			click : function(e){
				Ext.getDom('showTypeForSearchKitchen').innerHTML = e.text;
			},
			dblclick : function(node, e){
				searchMenuHandler();
				Ext.getCmp('menuMgrGrid').getSelectionModel().clearSelections();
				Ext.getCmp('menuMgrGrid').fireEvent('rowclick');
			}
		}
	});
}
function menuIsHaveImage(value, cellmeta, record, rowIndex, columnIndex, store){
	var style = '', content = '';
	if(record.get('img').indexOf('nophoto.jpg') == -1){
		style = 'style="color:green;"';
		content = '已上传';
	}else{
		content = '未设置';
	}
	return '<a href=\"javascript:btnFood.handler()" ' + style + ' >' + content + '</a>';
};

function menuDishOpt(value, cellmeta, record, rowIndex, columnIndex, store) {
	return '' 
		 + '<a href=\"javascript:btnTaste.handler()">口味</a>'
		 + '&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;'
		 + '<a href=\"javascript:btnFood.handler()">修改</a>'
		 + '&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;'
		 + '<a href=\"javascript:btnDeleteFood.handler()">删除</a>'
//		 + '&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;'
//		 + '<a href=\"javascript:testFn()">test</a>'
		 + '';
};

function initMenuGrid(){
	var menuColumnModel = new Ext.grid.ColumnModel([ 
	    new Ext.grid.RowNumberer(), 
	    {
    		header : '编号',
    		dataIndex : 'alias',
    		width : 65
    	}, {
    		header : '名称',
    		dataIndex : 'displayFoodName',
    		width : 180
    	}, {
    		header : '拼音',
    		dataIndex : 'pinyin',
    		width : 65
    	}, {
    		header : '价格',
    		dataIndex : 'unitPrice',
    		width : 65,
    		align : 'right',
    		renderer : Ext.ux.txtFormat.gridDou
    	}, {
    		header : '打印厨房',
    		dataIndex : 'kitchen.name',
    		width : 65
    	}, {
    		header : '图片状态',
    		width : 65,
    		align : 'center',
    		renderer : menuIsHaveImage
    	}, {
    		header : '操作',
    		dataIndex : 'operator',
    		width : 200,
    		align : 'center',
    		renderer : menuDishOpt
    	}
    ]);                                 
	var menuStore = new Ext.data.Store({
		proxy : new Ext.data.HttpProxy({ url : "../../QueryMenuMgr.do" }),
		reader : new Ext.data.JsonReader(Ext.ux.readConfig, FoodBasicRecord.getKeys()),
		listeners : {
			load : function(thiz, records){
				for(var i = 0; i < records.length; i++){
					Ext.ux.formatFoodName(records[i], 'displayFoodName', 'name');
				}
			}
		}
	});
	
	menuGrid = new Ext.grid.GridPanel({
		id : 'menuMgrGrid',
		region : 'center',
		frame : true,
//		margins : '0 5 0 0',
		ds : menuStore,
		cm : menuColumnModel,
		sm : new Ext.grid.RowSelectionModel({
			singleSelect : true
		}),
		viewConfig : {
			forceFit : true
		},
		bbar : createPagingBar(GRID_PADDING_LIMIT_20, menuStore),
		autoScroll : true,
		loadMask : { msg : '数据加载中，请稍等...' },
		tbar : new Ext.Toolbar({
			height : 26,
			items : [{
				xtype : 'tbtext',
				text : String.format(Ext.ux.txtFormat.typeName, '分厨', 'showTypeForSearchKitchen', '----')
			}, { 
				xtype:'tbtext', 
				text:'过滤:'
			}, filterTypeComb, { 
				xtype:'tbtext', 
				text:'&nbsp;&nbsp;'
			}, {
				xtype : 'combo',
				hidden : true,
				hideLabel : true,
				forceSelection : true,
				width : 100,
				id : 'comboOperatorForGridSearch',
				value : '等于',
				rawValue : 1,
				store : new Ext.data.SimpleStore({
					fields : [ 'value', 'text' ],
					data : [[1, '等于'], [2, '大于等于'], [3, '小于等于']]
				}),
				valueField : 'value',
				displayField : 'text',
				typeAhead : true,
				mode : 'local',
				triggerAction : 'all',
				selectOnFocus : true,
				readOnly : true,
				allowBlank : false
			}, {
				xtype : 'textfield',
				id : 'textfieldForGridSearch',
				hidden : true,
				width : 120
			}, {
				xtype: 'numberfield',
				id : 'numfieldForGridSearch',
				style: 'text-align: left;',
				hidden : true,
				width : 120
			}, {
				xtype : 'combo',
				forceSelection : true,
				hidden : true,
				width : 120,
				id : 'kitchenTypeComb',
				store : new Ext.data.JsonStore({
					fields : [ 'alias', 'name' ]
				}),
				valueField : 'alias',
				displayField : 'name',
				typeAhead : true,
				mode : 'local',
				triggerAction : 'all',
				selectOnFocus : true
			}, {
				xtype : 'combo',
				forceSelection : true,
				hidden : true,
				width : 120,
				id : 'comboStockStatusForSearch',
				store : new Ext.data.SimpleStore({
					fields : ['value', 'text'],
					data : stockStatusData
				}),
				valueField : 'value',
				displayField : 'text',
				typeAhead : true,
				mode : 'local',
				triggerAction : 'all',
				selectOnFocus : true
			}, { 
				xtype:'tbtext', 
				text:'&nbsp;&nbsp;&nbsp;&nbsp;特价:'
			}, {
				xtype : 'checkbox',
				id : 'specialCheckbox'
			}, { 
				xtype:'tbtext', 
				text:'&nbsp;&nbsp;&nbsp;&nbsp;推荐:'
			}, {
				xtype : 'checkbox',
				id : 'recommendCheckbox'
			}, { 
				xtype:'tbtext', 
				text:'&nbsp;&nbsp;&nbsp;&nbsp;赠送:'
			}, {
				xtype : 'checkbox',
				id : 'freeCheckbox'
			}, { 
				xtype:'tbtext', 
				text:'&nbsp;&nbsp;&nbsp;&nbsp;停售:'
			}, {
				xtype : 'checkbox',
				id : 'stopCheckbox'
			}, { 
				xtype:'tbtext',
				text:'&nbsp;&nbsp;&nbsp;&nbsp;时价:'
			}, {
				xtype : 'checkbox',
				id : 'currPriceCheckbox'
			}, { 
				xtype:'tbtext', 
				text:'&nbsp;&nbsp;&nbsp;&nbsp; 套菜:'
			}, {
				xtype : 'checkbox',
				id : 'combinationCheckbox'
			}, { 
				xtype:'tbtext', 
				text:'&nbsp;&nbsp;&nbsp;&nbsp; 热销:'
			}, {
				xtype : 'checkbox',
				id : 'hotCheckbox'
			}, { 
				xtype:'tbtext',
				text:'&nbsp;&nbsp;&nbsp;&nbsp; 称重:'
			}, {
				xtype : 'checkbox',
				id : 'weightCheckbox'
			}, '->', {
				xtype : 'button',
				hideLabel : true,
				iconCls : 'btn_search',
				id : 'srchBtn',
				text : '搜索',
				width : 100,
				handler : function(thiz, e) {
					searchMenuHandler();
					Ext.getCmp('menuMgrGrid').getSelectionModel().clearSelections();
					Ext.getCmp('menuMgrGrid').fireEvent('rowclick');
				}
			}]
		}),
		listeners : {
			render : function(thiz) {
				searchMenuHandler();
			},
			rowclick : function(thiz, rowIndex, e) {
				/*
				if(!displayInfoPanel.collapsed){
					var selData = Ext.ux.getSelData('menuMgrGrid');
					var selTab = Ext.getCmp('displayInfoPanelTab').getActiveTab();
					if(!selData){
						displayInfoPanel.setTitle('');
					}else{
						displayInfoPanel.setTitle(selData.foodName);
					}
					refreshInfoGrid(selTab);
				}
				*/
			},
			rowdblclick : function(){
				foodOperation('basicOperationTab', mmObj.operation.update);
			}
		}
	});
}

function initFoodOperationWin(){
	if(!foodOperationWin){
		foodOperationWin = new Ext.Window({
			id : 'foodOperationWin',
			closeAction : 'hide',
			closable : false,
			collapsed : true,
			modal : true,
			resizable : false,
			width : 900,
			height : 545,
			layout : 'fit',
			keys : [{
				key : Ext.EventObject.ESC,
				scope : this,
				fn : function(){
					foodOperationWin.hide();
				}
			}],
			bbar : [ {
				text : '上一道菜品',
		    	id : 'btnPreviousFood',
		    	iconCls : 'btn_previous',
		    	tooltip : '加载上一道菜品相关信息',
		    	handler : function(){
		    		Ext.getCmp('menuMgrGrid').getSelectionModel().selectPrevious();
		    		Ext.getCmp('foodOperationWin').setTitle(Ext.ux.getSelData('menuMgrGrid').name);
		    		Ext.getCmp('foodOperationWinTab').fireEvent('tabchange');
		    		
		    		Ext.getCmp('btnPreviousFood').setDisabled(!Ext.getCmp('menuMgrGrid').getSelectionModel().hasPrevious());
		    		Ext.getCmp('btnNextFood').setDisabled(false);
		    	}
		    }, {
		    	text : '下一道菜品',
		    	id : 'btnNextFood',
		    	iconCls : 'btn_next',
		    	tooltip : '加载下一道菜品相关信息',
		    	handler : function(){
		    		Ext.getCmp('menuMgrGrid').getSelectionModel().selectNext();
		    		Ext.getCmp('foodOperationWin').setTitle(Ext.ux.getSelData('menuMgrGrid').name);
		    		Ext.getCmp('foodOperationWinTab').fireEvent('tabchange');
		    		
		    		Ext.getCmp('btnPreviousFood').setDisabled(false);
		    		Ext.getCmp('btnNextFood').setDisabled(!Ext.getCmp('menuMgrGrid').getSelectionModel().hasNext());
		    	}
		    }, '->', {
		    	xtype : 'button',
		    	text : '添加',
		    	id : 'btnAddForOW',
		    	iconCls : 'btn_add',
		    	tooltip : '添加新菜品',
		    	handler : function(){
		    		addBasicHandler();
		    	}
		    }, {
		    	text : '应用',
		    	id : 'btnAppForOW',
		    	iconCls : 'btn_app',
		    	tooltip : '保存修改',
		    	handler : function(){
		    		foodOperationHandler({
	    				type : mmObj.operation.update,
	    				hide : false
	    			});
		    	}
		    }, {
		    	text : '关闭',
		    	id : 'btnCloseForOW',
		    	iconCls : 'btn_close',
		    	tooltip : '关闭窗体',
		    	handler : function(){
		    		foodOperationWin.hide();
		    	}
		    }, {
		    	text : '保存',
		    	id : 'btnSaveForOW',
		    	iconCls : 'btn_save',
		    	tooltip : '保存修改并关闭窗体',
		    	handler : function(){
		    		foodOperationHandler({
	    				type : mmObj.operation.update,
	    				hide : true
	    			});
		    	}
		    }, {
		    	text : '重置',
		    	id : 'btnRefreshForOW',
		    	iconCls : 'btn_refresh',
		    	tooltip : '重新加载菜品相关信息',
		    	handler : function(){
		    		var foWinTab = Ext.getCmp('foodOperationWinTab');
		    		if(foodOperationWin.operation == mmObj.operation.insert){
		    			if(foWinTab.getActiveTab().getId() == 'basicOperationTab'){
		    				resetbBasicOperation();
		    			}else if(foWinTab.getActiveTab().getId() == 'combinationOperationTab'){
		    				Ext.getCmp('combinationFoodGrid').getStore().removeAll();
		    				Ext.getCmp('txtMiniAllFoodNameSearch').setValue('');
		    				Ext.getCmp('btnSearchForAllFoodMiniGridTbar').handler();
		    			}
		    		}else{
		    			foWinTab.fireEvent('tabchange');
		    		}
		    	}
		    }],
			listeners : {
				hide : function(){
	    			var tabID = Ext.getCmp('foodOperationWinTab').getActiveTab().getId();
	    			if(tabID == 'basicOperationTab' || tabID == 'combinationOperationTab'){
	    				Ext.getCmp('menuMgrGrid').getSelectionModel().clearSelections();
	    				Ext.getCmp('menuMgrGrid').getStore().reload();
	    			}
	    			Ext.getCmp('menuMgrGrid').fireEvent('rowclick');
	    		}
			},
			items : [{
				xtype : 'tabpanel',
		    	id : 'foodOperationWinTab',
		    	border : false,
		    	activeTab : 0,
		    	defaults : {
		    		xtype : 'panel',
		    		layout : 'fit'
		    	},
		    	items : [{
		    		id : 'basicOperationTab',
	    	    	title : '菜品信息',
	    	    	items : [basicOperationPanel]
		    	}, {
		    		id : 'tasteOperationTab',
	    	    	title : '口味关联',
	    	    	items : [tasteOperationPanel]
	    	    }, 
	    	    /*{
	    	    	id : 'materialOperationTab',
	    	    	title : '食材关联',
	    	    	items : [materialOperationPanel]
	    	    },*/ 
	    	    {
	    	    	id : 'combinationOperationTab',
	    	    	title : '套菜关联',
	    	    	items : [combinationOperationPanel]
	    	    }],
	    	    listeners : {
		    		beforetabchange : function(thiz, newTab, currentTab ){
		    			foodOperationWin.newTab = !newTab ? '' : newTab.getId();
		    			foodOperationWin.currentTab = !currentTab ? '' : currentTab.getId();
		    		},
		    		tabchange : function(e, p){
		    			var foWinTab = Ext.getCmp('foodOperationWinTab');
		    			if(typeof(foWinTab.getActiveTab()) == 'undefined'){
		    				return;
		    			}
		    			if(foodOperationWin.operation == mmObj.operation.insert){
		    				if(foWinTab.getActiveTab().getId() == 'basicOperationTab' || foWinTab.getActiveTab().getId() == 'combinationOperationTab'){
		    					foodOperationHandler({
				    				type : mmObj.operation.insert
				    			});
		    				}else{
		    					foWinTab.setActiveTab(foodOperationWin.currentTab);
		    				}
		    			}else{
		    				foodOperationHandler({
			    				type : mmObj.operation.select
			    			});
		    			}
		    		}
		    	}
			}]
		});
	}
}

// on page load function
function menuMgrOnLoad() {

	Ext.Ajax.request({
		url : "../../QueryMenu.do",
		params : {
			dataSource : 'kitchens',
			restaurantID : restaurantID,
			type : 3
		},
		success : function(response, options) {
			var resultJSON = Ext.decode(response.responseText);
			if (resultJSON.success == true) {
				kitchenData = resultJSON.root;
				for(var i = 0; i < kitchenData.length; i++){
					kitchenTreeForSreach.getRootNode().appendChild(new Ext.tree.TreeNode({
						text : kitchenData[i].name,
						alias : kitchenData[i].alias,
						leaf : true
					}));
				}
			} else {
				Ext.MessageBox.show({
					msg : resultJSON.msg,
					width : 300,
					buttons : Ext.MessageBox.OK
				});
			}
		},
		failure : function(response, options) {
			
		}
	});
};










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
	//menuMgrOnLoad();
	Ext.Ajax.request({
		url : "../../QueryMenu.do",
		params : {
			dataSource : 'kitchens',
			restaurantID : restaurantID,
			type : 3
		},
		success : function(response, options) {
			var resultJSON = Ext.decode(response.responseText);
			if (resultJSON.success == true) {
				kitchenData = resultJSON.root;
				for(var i = 0; i < kitchenData.length; i++){
					kitchenTreeForSreach.getRootNode().appendChild(new Ext.tree.TreeNode({
						text : kitchenData[i].name,
						alias : kitchenData[i].alias,
						leaf : true
					}));
				}
			} else {
				Ext.MessageBox.show({
					msg : resultJSON.msg,
					width : 300,
					buttons : Ext.MessageBox.OK
				});
			}
		},
		failure : function(response, options) {
			
		}
	});
	initMenuGrid();
	
	new Ext.Panel({
		renderTo : 'divMenu',
		layout : 'fit',
		//width : parseInt(Ext.getDom('divMenu').parentElement.style.width.replace(/px/g,'')),
		height : parseInt(Ext.getDom('divMenu').parentElement.style.height.replace(/px/g,'')),
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
			}, btnCombination]
		}),
		keys : [{
			key : Ext.EventObject.ENTER,
			scope : this,
			fn : function(){	
				Ext.getCmp('srchBtn').handler(); 
			}
		}]
	});
	
	initFoodOperationWin();
	
	foodOperationWin.operation == mmObj.operation.select;
	foodOperationWin.render(document.body);
	var foWinTab = Ext.getCmp('foodOperationWinTab');
	foWinTab.setActiveTab('tasteOperationTab');
	foWinTab.setActiveTab('materialOperationTab');
	foWinTab.setActiveTab('combinationOperationTab');
	
});
