//----------------------load--------
function priceBaiscGridRenderer(){
	return ''
		   + '<a href="javascript:updateFoodPricePlanWinHandler()">修改</a>';
};

function initData(){
	Ext.Ajax.request({
		url : '../../QueryMenu.do',
		params : {
			dataSource : 'kitchens',
			restaurantID : restaurantID,
			type : 3
		},
		success : function(res, opt) {
			var jr = Ext.decode(res.responseText);
			if(jr.success == true) {
				kitchenData = jr;
				kitchenData.root.unshift({
					id : -1,
					name : '全部'
				});
			}else{
				Ext.ux.showMsg(jr);
			}
		},
		failure : function(res, opt) {
			Ext.ux.showMsg(Ext.decode(res.responseText));
		}
	});
};

function initTree(){
	var tbar = new Ext.Toolbar({
		height : 26,
		items : ['->', {
			text : '添加',
			hidden : true,
			iconCls : 'btn_add',
			handler : function(){
				insertPricePlanWinHandler();
			}
		}, {
			text : '修改',
			iconCls : 'btn_edit',
			handler : function(){
				updatePricePlanWinHandler();
			}
		}, {
			text : '删除',
			iconCls : 'btn_delete',
			handler : function(){
				deletePricePlanWinHandler();
			}
		}, {
			text : '刷新',
			id : 'btnRefreshPricePlanTree',
			iconCls : 'btn_refresh',
			handler : function(){
				Ext.getDom('pricePlanShowName').innerHTML = '----';
				pricePlanTree.getRootNode().reload();
			}
		}]
	});
	
	pricePlanTree = new Ext.tree.TreePanel({
		title : '方案',
		region : 'west',
		width : 200,
		border : true,
		rootVisible : true,
		autoScroll : true,
		frame : true,
		tbar : tbar,
		bodyStyle : 'backgroundColor:#FFFFFF; border:1px solid #99BBE8;',
		loader : new Ext.tree.TreeLoader({
			dataUrl : '../../QueryPricePlanTree.do',
			baseParams : {
				
				restaurantID : restaurantID
			}
		}),
		root : new Ext.tree.AsyncTreeNode({
			expanded : true,
			text : '全部方案',
	        leaf : false,
	        border : true,
	        pricePlanID : '-1',
	        pricePlanName : '全部方案',
	        listeners : {
	        	expand : function(e){
	        		pricePlanData.root = [];
	        		for(var i = 0; i < e.childNodes.length; i++){
	        			var temp = e.childNodes[i];
	        			if(temp.attributes['statusValue'] == 1){
	        				temp.setText(temp.attributes['pricePlanName']+'<font color="red">(默认方案)</font>');
	        				temp.select();
	        				temp.fireEvent('click', temp);
	        				temp.fireEvent('dblclick', temp);	
	        			}
	        			pricePlanData.root.push({
        					pricePlanID : temp.attributes['pricePlanID'],
        					pricePlanName : temp.attributes['pricePlanName'],
        					statusValue : temp.attributes['statusValue']
        				});	
	        		}
	        	}
	        }
		}),
		listeners : {
			click : function(e){
				Ext.getDom('pricePlanShowName').innerHTML = e.attributes['pricePlanName'];
			},
			dblclick : function(){
				Ext.getCmp('btnSearchFoodPricePlan').handler();
			}
		}
	});	
};

function initGrid(){
	var tbar = new Ext.Toolbar({
		height : 26,
		items : [{
			xtype : 'tbtext',
			text : String.format(Ext.ux.txtFormat.typeName, '方案', 'pricePlanShowName', '----')
		}, {
			xtype : 'tbtext',
			text : '&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;'
		}, {
			xtype : 'tbtext',
			text : '过滤:'
		}, {
			xtype : 'combo',
			id : 'comboSearchType',
			readOnly : true,
			forceSelection : true,
			width : 100,
			value : 0,
			store : new Ext.data.SimpleStore({
				fields : [ 'value', 'text' ],
				data : [[0, '全部'], [1, '菜品编号'], [2, '菜品价格'], [3, '菜品名称'], [4, '厨房']]
			}),
			valueField : 'value',
			displayField : 'text',
			typeAhead : true,
			mode : 'local',
			triggerAction : 'all',
			selectOnFocus : true,
			listeners : {
				select : function(thiz, record, index){
					var textValue = Ext.getCmp('txtSearchTextValue');
					var operator = Ext.getCmp('comboOperator');
					var numberValue = Ext.getCmp('txtSearchNumberValue');
					var kitchen = Ext.getCmp('comboSearchKitchen');
					pmObj.searchType = index;
					if(index == 0){
						textValue.setVisible(false);
						operator.setVisible(false);
						numberValue.setVisible(false);
						kitchen.setVisible(false);
						pmObj.searchValue = '';
					}else if(index == 1){
						textValue.setVisible(false);
						operator.setVisible(true);
						numberValue.setVisible(true);
						kitchen.setVisible(false);
						operator.setValue(1);
						numberValue.setValue();
						pmObj.searchValue = operator.getId()+'<|>'+numberValue.getId();
					}else if(index == 2){
						textValue.setVisible(false);
						operator.setVisible(true);
						numberValue.setVisible(true);
						kitchen.setVisible(false);
						operator.setValue(1);
						numberValue.setValue();
						pmObj.searchValue = operator.getId()+'<|>'+numberValue.getId();
					}else if(index == 3){
						textValue.setVisible(true);
						operator.setVisible(false);
						numberValue.setVisible(false);
						kitchen.setVisible(false);
						textValue.setValue();
						pmObj.searchValue = textValue.getId();
					}else if(index == 4){
						textValue.setVisible(false);
						operator.setVisible(false);
						numberValue.setVisible(false);
						kitchen.setVisible(true);
						kitchen.store.loadData(kitchenData);
						kitchen.setValue(-1);
						pmObj.searchValue = kitchen.getId();
					}
				}
			}
		}, {
			xtype : 'tbtext',
			text : '&nbsp;&nbsp;'
		}, {
			xtype : 'textfield',
			id : 'txtSearchTextValue',
			hidden : true,
			width : 100
		}, {
			xtype : 'combo',
			hidden : true,
			hideLabel : true,
			forceSelection : true,
			width : 100,
			id : 'comboOperator',
			value : 0,
			store : new Ext.data.SimpleStore({
				fields : [ 'value', 'text' ],
				data : [[1,'等于'], [2,'大于等于'], [3,'小于等于']]
			}),
			valueField : 'value',
			displayField : 'text',
			typeAhead : true,
			mode : 'local',
			triggerAction : 'all',
			selectOnFocus : true,
			readOnly : true
		}, {
			xtype : 'numberfield',
			id : 'txtSearchNumberValue',
			style : 'text-align:left;',
			hidden : true,
			width : 100
		}, {
			xtype : 'combo',
			id : 'comboSearchKitchen',
			hidden : true,
			forceSelection : true,
			readOnly : true,
			width : 100,
			store : new Ext.data.JsonStore({
				root : 'root',
				fields : [ 'id', 'name' ]
			}),
			valueField : 'id',
			displayField : 'name',
			typeAhead : true,
			mode : 'local',
			triggerAction : 'all',
			selectOnFocus : true,
			listeners : {
				select : function(){
					Ext.getCmp('btnSearchFoodPricePlan').handler();
				}
			}
		}, '->', {
			text : '搜索',
			id : 'btnSearchFoodPricePlan',
			iconCls : 'btn_search',
			handler : function(){
				var pricePlanNode = pricePlanTree.getSelectionModel().getSelectedNode();
				var sOperator = '', sValue = '', sPrciePlan = '';
				if(pmObj.searchType == 0){
					sOperator = '';
					sValue = '';
				}else if(pmObj.searchType == 1 || pmObj.searchType == 2){
					var temp = pmObj.searchValue.split('<|>');
					sOperator = Ext.getCmp(temp[0]).getValue();
					sValue = Ext.getCmp(temp[1]).getValue();
				}else{
					sValue = Ext.getCmp(pmObj.searchValue).getValue();
				}
				if(pricePlanNode != null && typeof pricePlanNode.attributes['pricePlanID'] == 'number'){
					sPrciePlan = pricePlanNode.attributes['pricePlanID'];
				}else{
					sPrciePlan = '';
				}
				var gs = priceBaiscGrid.getStore();
				gs.baseParams['searchType'] = pmObj.searchType;
				gs.baseParams['searchOperator'] = sOperator;
				gs.baseParams['searchValue'] = sValue;
				gs.baseParams['searchPrciePlan'] = sPrciePlan;
				gs.load({
					params : {
						start : 0,
						limit : GRID_PADDING_LIMIT_20
					}
				});
			}
		}, {
			text : '重置',
			iconCls : 'btn_refresh',
			handler : function(){
				Ext.getCmp('comboSearchType').setValue(0);
				Ext.getCmp('comboSearchType').fireEvent('select', null, null, 0);
				priceBaiscGrid.getStore().baseParams['searchPrciePlan'] = null;
				Ext.getCmp('btnSearchFoodPricePlan').handler();
			}
		}, {
			text : '修改',
			iconCls : 'btn_edit',
			handler : function(){
				updateFoodPricePlanWinHandler();
			}
		}]
	});
	priceBaiscGrid = createGridPanel(
		'priceBaiscGrid',
		'菜品价格',
		'',
		'',
		'../../QueryFoodPricePlan.do',
		[
			[true, false, false, true], 
			//['方案编号', 'planId'] , 
			['方案名称', 'pricePlan.name'], 
			['菜品编号', 'foodAlias'],
			['菜品名称', 'foodName'], 
			['菜品价格', 'unitPrice',,'right', 'Ext.ux.txtFormat.gridDou'], 
			['厨房名称', 'kitchenName'],
			['操作', 'operation', '', 'center', 'priceBaiscGridRenderer']
		],
		FoodPricePlanRecord.getKeys(),
		[ ['isPaging', true], ['restaurantID', restaurantID]],
		GRID_PADDING_LIMIT_20,
		'',
		tbar
	);	
	priceBaiscGrid.region = 'center';
	priceBaiscGrid.on('rowdblclick', function(){
		updateFoodPricePlanWinHandler();
	});
	
	priceBaiscGrid.keys = [{
		key : Ext.EventObject.ENTER,
		scope : this,
		fn : function(){
			Ext.getCmp('btnSearchFoodPricePlan').handler();
		}
	}];
};

function initWin(){
	oPricePlanWin = new Ext.Window({
		modal : true,
		resizable : false,
		closable : false,
		width : 230,
		items : [{
			xtype : 'form',
			layout : 'form',
			frame : true,
			labelWidth : 70,
			defaults : {
				width : 120
			},
			items : [{
				xtype : 'hidden',
				id : 'txtPricePlanID'
			}, {
				xtype : 'textfield',
				id : 'txtPricePlanName',
				fieldLabel : '方案名称',
				allowBlank : false,
				blankText : '方案名称不能为空.',
				validator : function(v){
					if(Ext.util.Format.trim(v).length > 0){
						return true;
					}else{
						return '方案名称不能为空.';
					}
				}
			}, {
				xtype : 'combo',
				id : 'comboPricePlanStatus',
				fieldLabel : '状态',
				forceSelection : true,
				value : 0,
				store : new Ext.data.SimpleStore({
					fields : ['value', 'text'],
					data : pricePlanStatusData
				}),
				valueField : 'value',
				displayField : 'text',
				typeAhead : true,
				mode : 'local',
				triggerAction : 'all',
				selectOnFocus : true,
				readOnly : true
			}, {
				xtype : 'label',
				style : 'color:green;font-szie:12px;',
				text : '说明:  状态为必选项, 且唯一, 当状态为默认则结账时默认使用该价格方案.'
			}, {
				xtype : 'combo',
				id : 'comboCopyPricePlan',
				fieldLabel : '复制方案',
				forceSelection : true,
				store : new Ext.data.JsonStore({
					root : 'root',
					fields : ['pricePlanId', 'pricePlanName']
				}),
				valueField : 'pricePlanId',
				displayField : 'pricePlanName',
				typeAhead : true,
				mode : 'local',
				triggerAction : 'all',
				selectOnFocus : true
			}]
		}],
		bbar : ['->', {
			text : '保存',
			id : 'btnSavePricePlan',
			iconCls : 'btn_save',
			handler : function(){
				var name = Ext.getCmp('txtPricePlanName');
				var status = Ext.getCmp('comboPricePlanStatus');
				
				var pricePlan = operationPricePlanData({ 
					type : pmObj.operation['get'] 
				}).data;
				var action = '';
				if(oPricePlanWin.otype == pmObj.operation['insert']){
					if(!name.isValid()){
						return;
					}
					action = '../../InsertPricePlan.do';
					pricePlan.id = pricePlan.copyID == null ? '' : pricePlan.copyID;
				}else if(oPricePlanWin.otype == pmObj.operation['update']){
					if(!status.isValid() || !status.isValid()){
						return;
					}
					action = '../../UpdatePricePlan.do';
				}else{
					return;
				}
				// 删除多余字段
				(delete pricePlan['copyID']);
				Ext.Ajax.request({
					url : action,
					params : {
						
						restaurantID : restaurantID,
						name : pricePlan.name,
						id : pricePlan.id,
						status : pricePlan.statusValue
					},
					success : function(res, opt){
						var jr = Ext.decode(res.responseText);
						if(jr.success){
							oPricePlanWin.hide();
							Ext.example.msg(jr.title, jr.msg);
							Ext.getCmp('btnRefreshPricePlanTree').handler();
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
			text : '关闭',
			id : 'btnCloseOPricePlanWin',
			iconCls : 'btn_close',
			handler : function(){
				oPricePlanWin.hide();
			}
		}],
		keys : [{
			key : Ext.EventObject.ENTER,
			scope : this,
			fn : function(){
				Ext.getCmp('btnSavePricePlan').handler();
			}
		}, {
			key : Ext.EventObject.ESC,
			scope : this,
			fn : function(){
				Ext.getCmp('btnCloseOPricePlanWin').handler();
			}
		}],
		listeners : {
			show : function(){
				Ext.getCmp('comboCopyPricePlan').store.loadData(pricePlanData);
			}
		}
	});
	oPricePlanWin.render(document.body);
	
	//--------------------------------------------------
	oPriceBasicWin = new Ext.Window({
		modal : true,
		resizable : false,
		closable : false,
		width : 230,
		items : [{
			xtype : 'form',
			layout : 'form',
			frame : true,
			labelWidth : 70,
			labelAlign : 'right',
			defaults : {
				width : 110
			},
			items : [{
				xtype : 'hidden',
				id : 'hideFoodPricePlanID'
			}, {
				xtype : 'hidden',
				id : 'hidePricePlanID'
			}, {
				xtype : 'textfield',
				id : 'txtFoodPricePlanName',
				fieldLabel : '方案名称',
				disabled : true
			}, {
				xtype : 'textfield',
				id : 'txtFoodName',
				fieldLabel : '菜品名称',
				disabled : true
					
			}, {
				xtype : 'numberfield',
				id : 'numFoodUnitPrice',
				fieldLabel : '菜品价格'
			}]
		}],
		bbar : ['->', {
			text : '保存',
			id : 'btnSaveFoodPricePlan',
			iconCls : 'btn_save',
			handler : function(){
				var unitPrice = Ext.getCmp('numFoodUnitPrice');
				if(!unitPrice.isValid()){
					return;
				}
				var foodPricePlan = operationFoodPricePlanData({
					type : pmObj.operation['get']
				}).data;
				
				Ext.Ajax.request({
					url : '../../UpdateFoodPricePlan.do',
					params : {
						foodPricePlan : Ext.encode(foodPricePlan)
					},
					success : function(res, opt){
						var jr = Ext.util.JSON.decode(res.responseText);
						if(jr.success){
							Ext.getCmp('btnCloseFoodPricePlan').handler();
							Ext.example.msg(jr.title, jr.msg);
							Ext.getCmp('btnSearchFoodPricePlan').handler();
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
			text : '关闭',
			id : 'btnCloseFoodPricePlan',
			iconCls : 'btn_close',
			handler : function(){
				oPriceBasicWin.hide();
			}
		}],
		keys : [{
			key : Ext.EventObject.ENTER,
			scope : this,
			fn : function(){
				Ext.getCmp('btnSaveFoodPricePlan').handler();
			}
		}, {
			key : Ext.EventObject.ESC,
			scope : this,
			fn : function(){
				Ext.getCmp('btnCloseFoodPricePlan').handler();
			}
		}]
	});
	oPriceBasicWin.render(document.body);
	
};
//-----------------

//-------------------taste--
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
//---------------
var btnInsertPricePlan = new Ext.ux.ImageButton({
	imgPath : '../../images/btnAddForBigBar.png',
	imgWidth : 50,
	imgHeight : 50,
	tooltip : '添加方案',
	handler : function(btn){
		insertPricePlanWinHandler();
	}
});

/**********************************************************************/
insertPricePlanWinHandler = function(){
	pricePlanOperationHandler({
		type : pmObj.operation['insert']
	});
};

updatePricePlanWinHandler = function(){
	pricePlanOperationHandler({
		type : pmObj.operation['update']
	});
};

deletePricePlanWinHandler = function(){
	pricePlanOperationHandler({
		type : pmObj.operation['delete']
	});
};

function pricePlanOperationHandler(c){
	if(c == null || typeof c == 'undefined' || typeof c.type == 'undefined'){
		return;
	}
	oPricePlanWin.otype = c.type;
	
	var copyID = Ext.getCmp('comboCopyPricePlan');
	
	if(c.type == pmObj.operation['insert']){
		operationPricePlanData({ 
			type : pmObj.operation['set'] 
		});
		Ext.query('div label[for='+copyID.getId()+']')[0].parentElement.setAttribute('style', "display:block");
		
		oPricePlanWin.setTitle("添加方案");
		oPricePlanWin.show();
		oPricePlanWin.center();
	}else if(c.type == pmObj.operation['update']){
		var sn = pricePlanTree.getSelectionModel().getSelectedNode();
		if(!sn || sn.attributes.pricePlanID == -1){
			Ext.example.msg('提示', '请选中一个方案再进行操作.');
			return;
		}
		operationPricePlanData({ 
			type : pmObj.operation['set'],
			data : {
				id : sn.attributes['pricePlanID'],
				name : sn.attributes['pricePlanName'],
				statusValue : sn.attributes['statusValue']
			}
		});
		
		Ext.query('div label[for='+copyID.getId()+']')[0].parentElement.setAttribute('style', "display:none");
		
		oPricePlanWin.setTitle("修改方案");
		oPricePlanWin.show();
		oPricePlanWin.center();
	}else if(c.type == pmObj.operation['delete']){
		var sn = pricePlanTree.getSelectionModel().getSelectedNode();
		if(!sn || sn.attributes['pricePlanID'] == -1){
			Ext.example.msg('提示', '请选中一个方案再进行操作.');
			return;
		}
		if(sn.attributes['status'] == 1){
			Ext.example.msg('提示', '该价格方案为活动状态, 正在使用中的不允许删除.');
			return;
		}
		Ext.Msg.show({
			title : '提示',
			msg : '是否删除方案:<font color="#FF0000">'+sn.text+'</font>?<br/>重要:同时删除该方案下所有菜品价格信息.',
			icon : Ext.Msg.QUESTION,
			buttons : Ext.Msg.YESNO,
			fn : function(e){
				if(e == 'yes'){
					Ext.Ajax.request({
						url : '../../DeletePricePlan.do',
						params : {
							restaurantID : restaurantID,
							id : sn.attributes['pricePlanID']
						},
						success : function(res, opt){
							var jr = Ext.util.JSON.decode(res.responseText);
							if(jr.success){
								Ext.example.msg(jr.title, jr.msg);
								Ext.getCmp('btnRefreshPricePlanTree').handler();
							}else{
								Ext.ux.showMsg(jr);
							}
						},
						failure : function(res, opt){
							Ext.ux.showMsg(Ext.decode(res.responseText));
						}
					});
				}
			}
		});
	}else{
		Ext.example.msg('错误', '未知操作类型, 请联系管理员');
	}
};

function operationPricePlanData(c){
	if(c == null || c.type == null || typeof c.type == 'undefined')
		return;
	var data = {};
	var id = Ext.getCmp('txtPricePlanID');
	var name = Ext.getCmp('txtPricePlanName');
	var copyID = Ext.getCmp('comboCopyPricePlan');
	var status = Ext.getCmp('comboPricePlanStatus');
	if(c.type == pmObj.operation['set']){
		data = c.data == null || typeof c.data == 'undefined' ? {} : c.data;
		id.setValue(data['id']);
		name.setValue(data['name']);
		copyID.setValue();
		status.setValue(typeof data['statusValue'] == 'undefined' ? 0 : data['statusValue']);
	}else if(c.type == pmObj.operation['get']){
		data = {
			restaurantID : restaurantID,
			name : name.getValue(),
			id : id.getValue(),
			statusValue : status.getValue(),
			copyID : copyID.getRawValue() == '' ? null : copyID.getValue()
		};
		c.data = data;
	}
	name.clearInvalid();
	status.clearInvalid();
	return c;
};

/**********************************************************************/
function updateFoodPricePlanWinHandler(){
	foodPricePlanOperationHandler({
		type : pmObj.operation['update']
	});
};

function foodPricePlanOperationHandler(c){
	if(c == null || typeof c == 'undefined' || typeof c.type == 'undefined'){
		return;
	}
	oPriceBasicWin.otype = c.type;
	
	if(c.type == pmObj.operation['update']){
		var sd = Ext.ux.getSelData(priceBaiscGrid);
		if(!sd){
			Ext.example.msg('提示', '请选中一个菜品再进行操作.');
			return;
		}
		operationFoodPricePlanData({
			type : pmObj.operation['set'],
			data : sd
		});
		oPriceBasicWin.setTitle('修改菜品价格');
		oPriceBasicWin.show();
		oPriceBasicWin.center();
	}else{
		Ext.example.msg('错误', '未知操作类型, 请联系管理员');
	}
};

function operationFoodPricePlanData(c){
	if(c == null || c.type == null || typeof c.type == 'undefined')
		return;
	var data = {};
	var foodID = Ext.getCmp('hideFoodPricePlanID');
	var pricePlanID = Ext.getCmp('hidePricePlanID');
	var pricePlanName = Ext.getCmp('txtFoodPricePlanName');
	var foodName = Ext.getCmp('txtFoodName');
	var unitPrice = Ext.getCmp('numFoodUnitPrice');
	if(c.type == pmObj.operation['set']){
		data = c.data == null || typeof c.data == 'undefined' ? {} : c.data;
		foodID.setValue(data['foodId']);
		pricePlanID.setValue(data['planId']);
		pricePlanName.setValue(data['pricePlan.name']);
		foodName.setValue(data['foodName']);
		unitPrice.setValue(data['unitPrice']);
	}else if(c.type == pmObj.operation['get']){
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

/**********************************************************************/
Ext.onReady(function(){
	initData();
	initTree();
	initGrid();
	
	new Ext.Panel({
		title : '菜品价格方案管理',
		renderTo : 'divPrice',
		width : parseInt(Ext.getDom('divPrice').parentElement.style.width.replace(/px/g,'')),
		height : parseInt(Ext.getDom('divPrice').parentElement.style.height.replace(/px/g,'')),
		layout : 'border',
		frame : true,
		items : [pricePlanTree, priceBaiscGrid],
		tbar : new Ext.Toolbar({
			height : 55,
			items : [
			    {xtype:'tbtext',text:'&nbsp;&nbsp;'},
//			    btnAddProgram,
//			    {xtype:'tbtext',text:'&nbsp;&nbsp;'},
			    btnInsertPricePlan
			]
		})
	});
	
	initWin();
	
});