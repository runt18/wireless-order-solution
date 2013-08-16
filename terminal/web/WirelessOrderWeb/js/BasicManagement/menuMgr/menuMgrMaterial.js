
materialOperationRenderer = function(){
	return '<a href="javascript:materialDeleteHandler()">删除</a>';
};

materialDeleteHandler = function(){
	var hmg = Ext.getCmp('haveMaterialGrid');
	hmg.getStore().remove(hmg.getSelectionModel().getSelections()[0]);
	hmg.getView().refresh();
};

materialDisplaySumHandler = function(){
	var hmds = Ext.getCmp('haveMaterialGrid').getStore();
	var sumPrice = 0, sumConsumption = 0, itemSumPrice = 0;
	for(var i = 0; i < hmds.getCount(); i++){
		itemSumPrice = hmds.getAt(i).get('price') * hmds.getAt(i).get('consumption');
		hmds.getAt(i).set('sumPrice', itemSumPrice);
		sumPrice += itemSumPrice;
		sumConsumption += hmds.getAt(i).get('consumption');
	}
	Ext.getDom('txtDisplayHaveMaterialPrice').innerHTML = parseFloat(sumPrice).toFixed(2);
	Ext.getDom('txtDisplayHaveMaterialConsumption').innerHTML = parseFloat(sumConsumption).toFixed(2);
};

var haveMaterialGrid = new Ext.grid.EditorGridPanel({
	title : '<center>已关联食材</center>',
	id : 'haveMaterialGrid',
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
//		    {header:'编号', dataIndex:'materialAliasID', width:70},
		    {header:'食材名', dataIndex:'materialName', width:200},
		    {header:'价格', dataIndex:'price', width:80, align:'right', renderer:Ext.ux.txtFormat.gridDou},
		    {
		    	header:'消耗', 
		    	dataIndex:'consumption', 
		    	width:80, align:'right', 
		    	renderer:Ext.ux.txtFormat.gridDou,
		    	tooltip : '双击消耗数量修改',
		    	editor : new Ext.form.NumberField({
					maxLength : 8,
		    		maxLengthText : '长度不能超过8位',
		    		minValue : 0.01,
		    		maxValue : 65535,
		    		allowBlank : false,
		    		style : 'color:green; font-weight:bold;'
				})
		    },
		    {header:'成本', dataIndex:'sumPrice', width:90, align:'right', renderer:Ext.ux.txtFormat.gridDou},
		    {header:'操作', align:'center', renderer:materialOperationRenderer}
		]
	),
	ds : new Ext.data.Store({
		proxy : new Ext.data.HttpProxy({
			url : '../../QueryFoodMaterial.do'
		}),
		reader : new Ext.data.JsonReader(Ext.ux.readConfig, 
			[
			    {name:'materialID'},
			    {name:'materialAliasID'},
			    {name:'materialName'},
			    {name:'consumption'},
			    {name:'cateID'},
			    {name:'cateName'},
			    {name:'price'}
			]
		),
		listeners : {
			beforeload : function(){
				var selData = Ext.ux.getSelData('menuMgrGrid');
				this.baseParams['foodID'] = selData.foodID;
				this.baseParams['restaurantID'] = restaurantID;
			},
			load : function(thiz, rs){
				materialDisplaySumHandler();
			},
			add : function(thiz){
				materialDisplaySumHandler();
			},
			remove : function(thiz){
				materialDisplaySumHandler();
			},
			update : function(thiz){
				materialDisplaySumHandler();
			}
		}
	}),
	bbar : new Ext.Toolbar({
		height : 26,
		items : [
			{xtype:'tbtext', text:String.format(Ext.ux.txtFormat.barTitle, '总计')}, 
			'-', '->',
			{xtype:'tbtext', text:String.format(Ext.ux.txtFormat.barMsg, '总成本', 'txtDisplayHaveMaterialPrice', '0.00')},
			{xtype:'tbtext', text:String.format(Ext.ux.txtFormat.barMsg, '总消耗', 'txtDisplayHaveMaterialConsumption', '0.00')}
		]
	}),
	listeners : {
		resize : function(thiz, adjWidth, adjHeight, rawWidth, rawHeight){
			thiz.setHeight(tabItemsHeight);
		}
	}
});

var allMaterialGridTbar = new Ext.Toolbar({
	height : 26,
	items : [
		{ xtype:'tbtext', text:'食材名搜索:' },
		{
			xtype : 'textfield',
			id : 'txtMaterialNameSearch',
			width : 100
		},
		'->',
		{
			text : '搜索',
			id : 'btnSearchForAllMaterialGridTbar',
			iconCls : 'btn_search',
			handler : function(){
				var mn = Ext.getCmp('txtMaterialNameSearch').getValue().trim();
				var amgd = Ext.getCmp('allMaterialGrid').getStore();
				if(mn == ''){
					amgd.baseParams['type'] = 0;
				}else{
					amgd.baseParams['type'] = 2;
					amgd.baseParams['value'] = mn;
				}
				amgd.load({
					params : {
						limit : 30,
						start : 0
					}
				});
			}
		}
	]
});

var allMaterialGrid = createGridPanel(
	'allMaterialGrid',
	'<center>所有食材</center>',
	tabItemsHeight,
	'',
	'../../QueryMaterialMgr.do',
	[
	    [true, false, false, true], 
		['编号', 'materialAlias', 70] , 
		['食材名', 'materialName', 200], 
	    ['价格', 'price', '', 'right', 'Ext.ux.txtFormat.gridDou']
//		['种类', 'cateName']
	],
	['materialName', 'price','materialAlias','materialID', 'cateID', 'cateName', 'warningNbr', 'dangerNbr', 'storage'],
	[ ['type',0], ['isDanger',false], ['isWarning', false], ['isPaging',true], ['value', '']],
	30,
	'',
	allMaterialGridTbar
);
allMaterialGrid.columnWidth = .44;
allMaterialGrid.getBottomToolbar().displayMsg = '共&nbsp;{2}&nbsp;条记录';
allMaterialGrid.on('render', function(thiz){
	
});
allMaterialGrid.on('resize', function(thiz){
	thiz.setHeight(tabItemsHeight);
});
allMaterialGrid.on('rowdblclick', function(thiz, ri, e){
	var hmg = Ext.getCmp('haveMaterialGrid');
	var sr = thiz.getSelectionModel().getSelections()[0];
	var cv = true;
	hmg.getStore().each(function(r){
		if(r.get('materialID') == sr.get('materialID')){
			cv = false;
			r.set('consumption', parseFloat(r.get('consumption') + 1));
			return;
		}
	});
	if(cv){
		sr.set('materialAliasID', sr.get('materialAlias'));
		sr.set('consumption', 1);
		hmg.getStore().insert(0, sr);
		hmg.getView().refresh();
//		hmg.getSelectionModel().selectFirstRow();
	}
});

var materialOperationPanel = new Ext.Panel({
	id : 'materialOperationPanel',
	frame : true,
	border : false,
	layout : 'column',
	items : [
	    haveMaterialGrid,
	    {xtype:'panel', columnWidth:.01, html:'&nbsp;'},
	    allMaterialGrid
	]
});

/**
 * 修改菜品关联食材
 */
updateMaterialHandler = function(c){
	
	var foodID = c.data.foodID;
	var materailContent = '';
	
	var hmg = Ext.getCmp('haveMaterialGrid').getStore();
	for(var i = 0; i < hmg.getCount(); i++){
		materailContent += (i > 0 ? '<split>' : '');
		materailContent += (hmg.getAt(i).get('materialID') + ',' + hmg.getAt(i).get('consumption'));
	}
	
	setButtonStateOne(true);
	
	Ext.Ajax.request({
		url : '../../UpdateFoodMaterial.do',
		params : {
			foodID : foodID,
			restaurantID : restaurantID,
			materailContent : materailContent
		},
		success : function(response, options){
			var jr = Ext.util.JSON.decode(response.responseText);
			if(eval(jr.success)){
				Ext.example.msg(jr.title, jr.msg);
				if(c.hide == true){
					Ext.getCmp('foodOperationWin').hide();
				}
				hmg.load();
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
