//反结账单头
function repaid_initNorthPanel(){

	dishesOrderNorthPanel = new Ext.Panel({
		hidden : !isRepaid,
		region : 'north',
		height : 62,
		frame : true,
		layout : 'column',
		defaults : {
			xtype : 'panel',
			layout : 'column',
//			columnWidth : 1,
			defaults : {
				xtype : 'form',
				layout : 'form',
				labelWidth : 65,
				width : 200,
				defaults : {
					width : 110
				}
			}
		},
		items : [{
			columnWidth : 1,
			items : [{
				items : [{
					xtype : 'textfield',
					id : 'txtSettleTypeFormat',
					fieldLabel : '结账方式',
					style : 'color:green;font-size:15px;font-weight:bold',
					value : '普通/会员',
					disabled : true
				}]
			}, {
				items : [{
					xtype : 'combo',
					id : 'comboDiscount',
					fieldLabel : '折扣方案',
					readOnly : false,
					forceSelection : true,
					store : new Ext.data.JsonStore({
						root : 'root',
						fields : [ 'id', 'name']
					}),
					valueField : 'id',
					displayField : 'name',
					typeAhead : true,
					mode : 'local',
					triggerAction : 'all',
					selectOnFocus : true,
					listeners : {
						select : function(thiz, record, index) {
							//选了别的折扣后取消会员注入
							if(orderType != 'common'){
								re_member = null;
								Ext.getCmp('txtSettleTypeFormat').setValue('普通');
								var payTypeCmo = Ext.getCmp('repaid_comboPayType');
								payTypeCmo.setValue(repaid_payType[0].id);
								payTypeCmo.fireEvent('select', payTypeCmo, null, null);
							}
							setRepaidOrderTitle();
							//FIXME
/*							Ext.Ajax.request({
								url : '../../OperateDiscount.do',
								params : {
									dataSource : 'setDiscount',
									orderId : orderID, 
									discountId : thiz.getValue() 
								},
								success : function(res){
									var jr = Ext.decode(res.responseText);
									if(jr.success){
										queryOrderDetail();
										setRepaidOrderTitle();
									}else{
										Ext.example.msg(jr.title, jr.msg);
									}
								},
								failure : function(res){}
							});*/
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
				width : 130,
				id : 'panelShowEraseQuota',
				style : 'font-size:18px;',
				html : '上限:￥<font id="fontShowEraseQuota" style="color:red;">0.00</font>'
			}, {
				width : 100,
				labelWidth : 40,
				items : [{
					xtype : 'label',
					width : 40,
					fieldLabel : '服务费',
					id : 'serviceRate',
					style : 'font-size:15px;text-align:right;'
				}]
			}, {
				items : [{
					xtype : 'combo',
					id : 'repaid_comboServicePlan',
					fieldLabel : '服务费方案',
					readOnly : false,
					forceSelection : true,
					store : new Ext.data.JsonStore({
						fields : [ 'planId', 'planName']
					}),
					valueField : 'planId',
					displayField : 'planName',
					typeAhead : true,
					mode : 'local',
					triggerAction : 'all',
					selectOnFocus : true				
				}]
			}]
		},{
			columnWidth : 0.15,
			items : [{
				xtype : 'label',
				width : 65,
				text : '收款方式:'
			},{
				xtype : 'combo',
				forceSelection : true,
				width : 80,
				id : 'repaid_comboPayType',
				store : new Ext.data.JsonStore({
					fields : [ 'id', 'name' ]
				}),
				valueField : 'id',
				displayField : 'name',
				typeAhead : true,
				mode : 'local',
				triggerAction : 'all',
				selectOnFocus : true,
				allowBlank : false,
				readOnly : false,
				listeners : {
					render : function(thiz){
						//如果是会员结账就不用添加混合
						repaid_payType.push({id:100, name:'混合结账'});
						thiz.getStore().loadData(repaid_payType);
						thiz.setValue(primaryOrderData.other.order.payTypeValue);
					},
					select : function(thiz){
						if(thiz.getValue() == 100){
							if(Ext.getCmp('repaid_mixedPayTypePanel').hasCheckbox){
								Ext.getCmp('repaid_mixedPayTypePanel').show();
							}else{
								initPaytypeCheckboxs();
							}
						}else{
							Ext.getCmp('repaid_mixedPayTypePanel').hide();
						}
					}
				}				
			},{
				xtype : 'label',
				width : 20,
				html : '&nbsp;'
			}]
		},  {
			columnWidth : 0.8,
			id : 'repaid_mixedPayTypePanel',
			items : []
		}, 
		{
			id : 'box4RepaidPricePlan',
			columnWidth : 0.15,
			items : [{
				xtype : 'label',
				width : 65,
				text : '价格方案:'
			},{
				xtype : 'combo',
				forceSelection : true,
				width : 80,
				id : 'repaid_txtPricePlanForPayOrder',
				store : new Ext.data.JsonStore({
					fields : [ 'id', 'name' ]
				}),
				valueField : 'id',
				displayField : 'name',
				typeAhead : true,
				mode : 'local',
				triggerAction : 'all',
				selectOnFocus : true,
				allowBlank : false,
				readOnly : false				
			},{
				xtype : 'label',
				width : 10,
				html : '&nbsp;'
			}]			
		}, {
			id : 'box4RepaidCoupon',
			columnWidth : 0.15,
			items : [{
				xtype : 'label',
				width : 65,
				html : '<font style="color:red;font-weight:bold">＊</font>优惠劵:'
			},{
				xtype : 'combo',
				forceSelection : true,
				width : 80,
				id : 'repaid_couponForPayOrder',
				store : new Ext.data.SimpleStore({
					fields : [ 'value', 'text' ]
				}),
				valueField : 'value',
				displayField : 'text',
				typeAhead : true,
				mode : 'local',
				triggerAction : 'all',
				selectOnFocus : true,
				allowBlank : false,
				readOnly : false				
			},{
				xtype : 'label',
				width : 10,
				html : '&nbsp;'
			}]			
		}]
	});
	
	//如果账单是混合结账就立即生成付款checkbox
	if(primaryOrderData.other.order.payTypeValue == 100){
		initPaytypeCheckboxs();
	}
}


function memberRepaid(){
	var bindMemberWin;
	if(!bindMemberWin){
		bindMemberWin = new Ext.Window({
			title : '会员反结账',
			width : 800,
			height : 200,
			modal : true,
			closable : false,
			resizable : false,
			keys: [{
				key : Ext.EventObject.ESC,
				scope : this,
				fn : function(){
					bindMemberWin.hide();
				}
			}],
			buttonAlign : 'center',
			buttons : [{
				text : '注入',
				handler : function(e){
					re_member = getOrderMember();
					Ext.getCmp('txtSettleTypeFormat').setValue('会员');
					bindMemberWin.hide();
					
					repaid_payType.push({id: 3, name: "会员余额", typeValue: 3});
					Ext.getCmp('repaid_comboPayType').getStore().loadData(repaid_payType);
					//是充值卡则选会员余额 
					if(re_member.memberType['attributeValue'] == 0){//充值
						var payTypeCmo = Ext.getCmp('repaid_comboPayType'); 
						payTypeCmo.setValue(3);
						payTypeCmo.fireEvent('select', payTypeCmo,null,null);
					}
					setRepaidOrderTitle({member:true});
					
				}
			}, {
				text : '关闭',
				handler : function(e){
					bindMemberWin.hide();
				}
			}],
			listeners : {
				hide : function(thiz){
//					calcDiscountID = tempCalcDiscountID;
					thiz.body.update('');
				},
				show : function(thiz){
					thiz.load({
						url : '../window/frontBusiness/memberRepaid.jsp',
						scripts : true
					});
				}
			}
		});
	}
	bindMemberWin.show();
}

function initPaytypeCheckboxs(){
	for (var i = 0; i < repaid_payType.length; i++) {
		if(repaid_payType[i].id == 100 || repaid_payType[i].id == 3){
			continue;
		}
		var checkBoxId = 'repaid_chbForPayType' + repaid_payType[i].id,  numberfieldId = 'repaid_numForPayType' + repaid_payType[i].id;
		if(i > 0){
			Ext.getCmp('repaid_mixedPayTypePanel').add({
				xtype : 'label',
				width : 15,
				html : '&nbsp;'
			});				
		}
		Ext.getCmp('repaid_mixedPayTypePanel').add({
			width : (repaid_payType[i].name.length * 25),
 	    	xtype : 'checkbox',
 	    	id : checkBoxId,
 	    	inputValue : repaid_payType[i].id,
 	    	relativePrice : numberfieldId,
 	    	boxLabel : repaid_payType[i].name + ':',
 	    	listeners : {
 	    		check : function(checkbox, checked){
 	    			var numForAlias = Ext.getCmp(checkbox.relativePrice);
					if(checked){
						payMoneyCalc[checkbox.relativePrice] = true;
						
	 	    			var mixedPayMoney = primaryOrderData.other.order.actualPrice;
	 	    			for(var pay in payMoneyCalc){
	 	    				if(typeof payMoneyCalc[pay] != 'boolean'){
	 	    					mixedPayMoney -= payMoneyCalc[pay];
	 	    				}
	 	    			}
	 	    			numForAlias.setValue(mixedPayMoney < 0? 0 : mixedPayMoney);							
						
						numForAlias.enable();
						numForAlias.focus(true, 100);
					}else{
						payMoneyCalc[checkbox.relativePrice] = false;
						
						numForAlias.disable();
						numForAlias.setValue();		
						numForAlias.clearInvalid();
					}
				},
				//解决第一次点击无效
				focus : function(thiz){
					var numForAlias = Ext.getCmp(thiz.relativePrice);
					if(document.getElementById(thiz.id).checked){
						numForAlias.disable();
					}else{
						numForAlias.enable();
						numForAlias.focus(true, 100);
					}
				}
 	    	}
	 	});				
	 	
		Ext.getCmp('repaid_mixedPayTypePanel').add({
			xtype : 'numberfield',
			id : numberfieldId,
			disabled : true,
			width : 70,
			minValue : 0,
 	    	listeners : {
 	    		blur : function(thiz){
 	    			if(thiz.getValue()){
 	    				payMoneyCalc[thiz.id] = thiz.getValue();
 	    			}
 	    		}
 	    	}
		});			 	
	}
	Ext.getCmp('repaid_mixedPayTypePanel').doLayout();
	Ext.getCmp('repaid_mixedPayTypePanel').hasCheckbox = true;
}

//如果是反结账则生成状态栏
if(isRepaid){
	repaid_initNorthPanel();
}

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
		rowclick : function(thiz, ri){
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
		rowclick : function(thiz, ri){
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
		rowclick : function(thiz, ri){
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
					if(tasteMenuData.root[j].taste.alias == gt.alias){
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
			var data = Ext.ux.getSelData(orderSingleGridPanel);
			commonTasteGridForTabPanel.getStore().load({
				params : {
					foodID : data?data.id:''
				}
			});
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
		readOnly : false,
		hidden : true,
		listeners : {
			render : function(thiz){
				Ext.Ajax.request({
					url : '../../QueryKitchen.do',
					params : {
						dataSource : 'normal'
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
	[ ['dataSource', 'foods'], ['isPaging', true]],
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
		Ext.ux.formatFoodName(records[i], 'displayFoodName', 'name', 0);
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
				readOnly : false,
				allowBlank : false,
				listeners : {
					render : function(thiz){
						Ext.Ajax.request({
							url : '../../QueryMenu.do',
							params : {
								dataSource : 'isAllowTempKitchen'
							},
							success : function(response, options){
								var jr = Ext.util.JSON.decode(response.responseText);
								thiz.store.loadData(jr.root);
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
    		kitchen.setValue();
    		
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
		text : '读取会员',
		hidden : !isRepaid,
		handler : function() {
			memberRepaid();
		}
	},{
		text : '提交',
		handler : function() {
			submitOrderHandler({
				href : (isRepaid ? '' : 'TableSelect.html')
			});
		}
	},
	{
		text : '提交并打印',
		hidden : true,
		handler : function() {
			submitOrderHandler({
				notPrint : false		
			});
		}
	}, 
	{
		text : '提交不打印',
		hidden : isRepaid,
		handler : function() {
			var href = '';
			if(isGroup){
				href = 'CheckOut.html?'+ 'orderID=' + orderID + '&category=' + tableCategory;
			}else{
				href = 'CheckOut.html?'+ 'tableID=' + tableDate.id+ '&personCount=1';
			}
			submitOrderHandler({
				notPrint : true,
				href : href			
			});
		}
	}, {
		text : '提交&结帐',
		hidden : isRepaid,
		handler : function() {
			var href = '';
			if(isGroup){
				href = 'CheckOut.html?'+ 'orderID=' + orderID + '&category=' + tableCategory;
			}else{
				href = 'CheckOut.html?'+ 'tableID=' + tableDate.id+ '&personCount=1';
			}
			submitOrderHandler({
				href : href			
			});
		}
	}, {
    	text : '刷新',
    	hidden : isFree || isRepaid,
    	handler : function(){
    		refreshOrderHandler();
    	}
    }, {
		text : '返回',
		handler : function() {
			if (orderIsChanged == false) {
				location.href = isRepaid ? 'Bills.html' : 'TableSelect.html';
			} else {
				Ext.MessageBox.show({
					msg : '下/改单还未提交，是否确认退出？',
					width : 300,
					buttons : Ext.MessageBox.YESNO,
					fn : function(btn) {
						if (btn == 'yes') {
							location.href = isRepaid ? 'Bills.html' : 'TableSelect.html';
						}
					}
				});
			}
		}
	}]
});

//快捷键面板
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
	//FIXME 键盘按键事件
	new Ext.KeyMap(document.body, [{
		key : 107,
		scope : this,
		fn : function(){
			quickActionWin.show();
		}
	}]);
}

	
function setRepaidOrderTitle(c){
	var orderFoodTitle = null;
	
	if(isRepaid){
		orderFoodTitle = '反结账 -- <span style="padding-left:2px; color:red;">'+orderId4Display+'</span>&nbsp;号帐单'
	}
	if(c && c.member){
		if(re_member && re_member.hasMember){
			orderFoodTitle += '&nbsp;&nbsp;&nbsp;会员名称: <span class="re_showMemberDetail">'+ re_member.name +'</span>';
			if(re_member.coupon){
				orderFoodTitle += '&nbsp;&nbsp;&nbsp;优惠券: <span class="re_showMemberDetail">'+ re_member.coupon.name +'</span>';
			}
		}
		orderFoodTitle += '&nbsp;&nbsp;&nbsp;当前折扣:<font color="green">'+ re_member.discount.name +'</font>';
	}else{
		if(re_member){
			orderFoodTitle += '&nbsp;&nbsp;&nbsp;会员名称: <span class="re_showMemberDetail">'+ re_member.name +'</span>';
		}
		
		if(primaryOrderData.other.order.coupon){
			orderFoodTitle += '&nbsp;&nbsp;&nbsp;优惠券:<font color="green">'+ primaryOrderData.other.order.coupon.name +'</font>';
		}
		
		if(primaryOrderData.other.order.discount){
			orderFoodTitle += '&nbsp;&nbsp;&nbsp;账单折扣:<font color="green">'+ primaryOrderData.other.order.discount.name +'</font>';
		}
		if(primaryOrderData.other.order.discounter){
			orderFoodTitle += '&nbsp;&nbsp;&nbsp;折扣人:<font color="green">'+ primaryOrderData.other.order.discounter +'</font>';
			orderFoodTitle += '&nbsp;&nbsp;&nbsp;折扣时间:<font color="green">'+ primaryOrderData.other.order.discountDate +'</font>';
		}
	}
	
	Ext.getCmp('billModCenterPanel').setTitle(orderFoodTitle);	
	
	$('.re_showMemberDetail').hover(function(){
		if(!re_memberDetailWin.loadMember){
			loadMemberDetail();
			re_memberDetailWin.loadMember = true;
		}
		re_memberDetailWin.setPosition($('.re_showMemberDetail').position().left, $('.re_showMemberDetail').position().top + 70);
		re_memberDetailWin.show();		
		
	}, function(){
		re_memberDetailWin.hide();
	});		
}

//加载会员显示窗口
function loadMemberDetailWin(){
	if(!re_memberDetailWin){
		re_memberDetailWin = new Ext.Window({
			id : 'repaid_memberDetailWin',
			closable : false, //是否可关闭
			resizable : false, //大小调整
			width : 280,	
			items : []	
		});
	}
}

//加载会员的基本信息
function loadMemberDetail(){
		
	var item = {
		layout : 'form',
		frame : true,
		border : true,
	 	defaults : {
	 		width : 130,
	 		disabled : true,
	 		labelStyle: 'font-weight:bold;font-size:15px;text-align:right;',
	 		style : 'font-size:15px;font-weight:bold;margin-bottom:5px;color:green;'
	 	},				
		items : [{
			id : 're_memberPhone',
			xtype : 'numberfield',
			fieldLabel : '手机号',
			value : re_member.mobile,
			listeners : {
				render : function(thiz){
					if(!re_member.mobile){
						thiz.getEl().up('.x-form-item').setDisplayed(false);
					}else{
						thiz.getEl().up('.x-form-item').setDisplayed(true);
					}
				}
			}
		},{
			id : 're_memberCard',
			xtype : 'numberfield',
			fieldLabel : '实体卡',
			value : re_member.memberCard,
			listeners : {
				render : function(thiz){
					if(!re_member.memberCard){
						thiz.getEl().up('.x-form-item').setDisplayed(false);
					}else{
						thiz.getEl().up('.x-form-item').setDisplayed(true);
					}
				}
			}
		},{
			id : 're_memberWeixinCard',
			xtype : 'numberfield',
			fieldLabel : '微信卡号',
			value : re_member.weixinCard,
			listeners : {
				render : function(thiz){
					if(!re_member.weixinCard){
						thiz.getEl().up('.x-form-item').setDisplayed(false);
					}else{
						thiz.getEl().up('.x-form-item').setDisplayed(true);
					}
				}
			}
		}]			
	}
	
	re_memberDetailWin.add(item);
	
	re_memberDetailWin.doLayout();
	
}

var dishesOrderEastPanel, centerPanel;
var commitOperate;

function showDetail(){
	if(!re_memberDetailWin.loadMember){
		loadMemberDetail();
		re_memberDetailWin.loadMember = true;
	}
	re_memberDetailWin.setPosition($('#re_showMemberDetail').position().left, $('#re_showMemberDetail').position().top + 70);
	re_memberDetailWin.show();		
}
	
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
		items : [ orderPanel, dishesOrderEastPanel,dishesOrderNorthPanel],
		listeners : {
			render : function(){
				//设置菜品列表title
				tableStuLoad();
			}
		}
	});
	
	var orderFoodTitle = null;
	if(isRepaid){
		orderFoodTitle = '反结账 -- <span style="padding-left:2px; color:red;">'+orderID+'</span>&nbsp;号帐单'
		if(re_member){
			orderFoodTitle += '&nbsp;&nbsp;&nbsp;会员名称: <span class="re_showMemberDetail">'+ re_member.name +'</span>';
		}
		
		if(primaryOrderData.other.order.coupon){
			orderFoodTitle += '&nbsp;&nbsp;&nbsp;优惠券:<font color="green">'+ primaryOrderData.other.order.coupon.name +'</font>';
		}
		if(primaryOrderData.other.order.discount){
			orderFoodTitle += '&nbsp;&nbsp;&nbsp;账单折扣:<font color="green">'+ primaryOrderData.other.order.discount.name +'</font>';
		}
		if(primaryOrderData.other.order.discounter){
			orderFoodTitle += '&nbsp;&nbsp;&nbsp;折扣人:<font color="green">'+ primaryOrderData.other.order.discounter +'</font>';
			orderFoodTitle += '&nbsp;&nbsp;&nbsp;折扣时间:<font color="green">'+ primaryOrderData.other.order.discountDate +'</font>';
		}	
		
	}
	

	
	var billModCenterPanel = new Ext.Panel({
		id : "billModCenterPanel",
		title : orderFoodTitle,
		region : "center",
		layout : "border",
		frame : false,
		items : [ centerPanel ]
	});
	
	initMainView(null, billModCenterPanel, null, function(){
			//隐藏价格方案 & 优惠券
			Ext.getCmp('box4RepaidPricePlan').hide();
			Ext.getCmp('box4RepaidCoupon').hide();
			// 初始化菜品数据
			loadOrderData();		
	});
	
	getOperatorName("../../");
	

	
	initKeyBoardEvent();
	
	//FIXME 重新刷新一次解决赠送bug
	if(!isFree && !isRepaid){
		refreshOrderHandler(true);
	}
	
	//加载会员信息窗口
	loadMemberDetailWin();
	
	//显示会员信息
	$('.re_showMemberDetail').hover(function(){
		if(!re_memberDetailWin.loadMember){
			loadMemberDetail();
			re_memberDetailWin.loadMember = true;
		}
		re_memberDetailWin.setPosition($('.re_showMemberDetail').position().left, $('.re_showMemberDetail').position().top + 70);
		re_memberDetailWin.show();		
		
	}, function(){
		re_memberDetailWin.hide();
	});	
	
	commitOperate = new Ext.LoadMask(document.body, {
	    msg  : '正在提交操作,请稍等......',
	    disabled : false,
	    removeMask : true
	});		
	
});