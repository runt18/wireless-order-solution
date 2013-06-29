function priceBaiscGridRenderer(){
	return ''
		   + '<a href="javascript:updateFoodPricePlanWinHandler()">修改</a>';
};

function initData(){
	Ext.Ajax.request({
		url : '../../QueryMenu.do',
		params : {
			pin : pin,
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
				pin : pin,
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
		[['pin',pin], ['isPaging', true], ['restaurantID', restaurantID]],
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
						pin : pin,
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
					type : pmObj.operation['get'],
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

