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
			aliasId : -1,
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
						aliasId : kitchenData[i].aliasId
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
					fields : [ 'aliasId', 'name' ]
				}),
				valueField : 'aliasId',
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
			pin : pin,
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
						aliasId : kitchenData[i].aliasId,
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

