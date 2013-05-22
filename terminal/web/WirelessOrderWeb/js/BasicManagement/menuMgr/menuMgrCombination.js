
combinationOperationRenderer = function(){
	return '<a href="javascript:combinationDeleteHandler()">删除</a>';
};

combinationDeleteHandler = function(){
	var cmg = Ext.getCmp('combinationFoodGrid');
	cmg.getStore().remove(cmg.getSelectionModel().getSelections()[0]);
	cmg.getView().refresh();
};


combinationDisplaySumHandler = function(){
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
	cm : new Ext.grid.ColumnModel(
		[
		    new Ext.grid.RowNumberer(),
//		    {header:'编号', dataIndex:'aliasID', width:70},
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
		]
	),
	ds : new Ext.data.JsonStore({
		url : '../../QueryFoodCombination.do',
		root : 'root',
		fields : ComboFoodRecord.getKeys(),
		listeners : {
			beforeload : function(){
				var selData = Ext.ux.getSelData('menuMgrGrid');
				this.baseParams['foodID'] = selData.id;
				this.baseParams['pin'] = pin;
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
		items : [
			{xtype:'tbtext', text:String.format(Ext.ux.txtFormat.barTitle, '总计')}, 
			'-', '->',
			{xtype:'tbtext', text:String.format(Ext.ux.txtFormat.barMsg, '总成本', 'txtDisplayCombinationFoodPrice', '0.00')},
			{xtype:'tbtext', text:String.format(Ext.ux.txtFormat.barMsg, '总份数', 'txtDisplayCombinationFoodPriceAmount', '0.00')}
		]
	}),
	listeners : {
		resize : function(thiz, adjWidth, adjHeight, rawWidth, rawHeight){
			thiz.setHeight(tabItemsHeight);
		}
	}
});


var allFoodMiniGridTbar = new Ext.Toolbar({
	height : 26,
	items : [
		{ xtype:'tbtext', text:'菜名搜索:'},
		{
			xtype : 'textfield',
			id : 'txtMiniAllFoodNameSearch',
			width : 100
		},
		'->',
		{
			text : '搜索',
			id : 'btnSearchForAllFoodMiniGridTbar',
			iconCls : 'btn_search',
			handler : function(){
				var mafn = Ext.getCmp('txtMiniAllFoodNameSearch').getValue().trim();
				var afmgs = Ext.getCmp('allFoodMiniGrid').getStore();
				if(mafn == ''){
					afmgs.baseParams['type'] = 0;
				}else{
					afmgs.baseParams['type'] = 2;
					afmgs.baseParams['value'] = mafn;
				}
				afmgs.load({
					params : {
						limit : GRID_PADDING_LIMIT_20,
						start : 0
					}
				});
			}
		}
	]
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
	FoodMgrRecord.getKeys(),
    [['pin', pin], ['restaurantId', restaurantID], ['isPaging', true] ],
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
