var dishDeleteImgBut = new Ext.ux.ImageButton({
	imgPath : "../../images/DeleteDish.png",
	imgWidth : 50,
	imgHeight : 50,
	tooltip : "删除",
	handler : function(btn) {
		dishOptDeleteHandler(dishOrderCurrRowIndex_);
	}
});

var countAddImgBut = new Ext.ux.ImageButton({
	imgPath : "../../images/AddCount.png",
	imgWidth : 50,
	imgHeight : 50,
	tooltip : "数量加1",
	handler : function(btn) {
		if(orderedGrid.getSelectionModel().getSelections().length != 1){
			Ext.example.msg('提示', '请选中一条菜品再进行操作!');
			return;
		}
		if (dishOrderCurrRowIndex_ != -1) {
			var ds = orderedGrid.getStore().getAt(dishOrderCurrRowIndex_).data;					
			for(var i = 0; i < orderedData.root.length; i++){						
				if(eval(orderedData.root[i]['foodID'] == ds['foodID'])){
					if(compareNormalTasteContent(ds.tasteGroup.normalTasteContent,  orderedData.root[i].tasteGroup.normalTasteContent)
							&& ds.tasteGroup.tempTaste == orderedData.root[i].tasteGroup.tempTaste){
						orderedData.root[i].count += 1;
						break;
					}
				}

			}
			orderedStore.loadData(orderedData);
			orderedGrid.getSelectionModel().selectRow(dishOrderCurrRowIndex_);
			orderIsChanged = true;
		}
	}
});
var countMinusImgBut = new Ext.ux.ImageButton({
	imgPath : "../../images/MinusCount.png",
	imgWidth : 50,
	imgHeight : 50,
	tooltip : "数量减1",
	handler : function(btn) {
		if(orderedGrid.getSelectionModel().getSelections().length != 1){
			Ext.example.msg('提示', '请选中一条菜品再进行操作!');
			return;
		}
		if (dishOrderCurrRowIndex_ != -1) {
			var ds = orderedGrid.getStore().getAt(dishOrderCurrRowIndex_).data;
			if (ds.count > 1) {
				for(var i = 0; i < orderedData.root.length; i++){						
					if(eval(orderedData.root[i]['foodID'] == ds['foodID'])){
						if(compareNormalTasteContent(ds.tasteGroup.normalTasteContent,  orderedData.root[i].tasteGroup.normalTasteContent)
								&& ds.tasteGroup.tempTaste == orderedData.root[i].tasteGroup.tempTaste){
							orderedData.root[i].count -= 1;
							break;
						}
					}
				}
				orderedStore.loadData(orderedData);
				orderedGrid.getSelectionModel().selectRow(dishOrderCurrRowIndex_);
				orderIsChanged = true;
			}
		}
	}
});
var countEqualImgBut = new Ext.ux.ImageButton({
	imgPath : "../../images/EqualCount.png",
	imgWidth : 50,
	imgHeight : 50,
	tooltip : "数量等于",
	handler : function(btn) {
		if(orderedGrid.getSelectionModel().getSelections().length != 1){
			Ext.example.msg('提示', '请选中一条菜品再进行操作!');
			return;
		}
		if (dishOrderCurrRowIndex_ != -1) {
			dishCountInputWin.show();
		}
	}
});

//dish count input pop window
dishCountInputWin = new Ext.Window({
	layout : "fit",
	width : 200,
	height : 90,
	closeAction : "hide",
	resizable : false,
	closable : false,
	items : [ {
		layout : "form",
		labelWidth : 30,
		border : false,
		frame : true,
		items : [ {
			xtype : "numberfield",
			fieldLabel : "数量",
			id : "dishCountInput",
			maxValue : 65535,
			minValue : 1,
			width : 130
		} ]
	} ],
	buttonAlign : 'center',
	buttons : [
	    {
			text : "确定",
			id : 'btnSetFoodCount',
			handler : function(){
				var inputCount = dishCountInputWin.findById("dishCountInput").getValue();
				if (inputCount != "" && inputCount > 0 && inputCount < 65535) {
					dishCountInputWin.hide();
					var ds = orderedGrid.getStore().getAt(dishOrderCurrRowIndex_).data;
					for(var i = 0; i < orderedData.root.length; i++){						
						if(orderedData.root[i]['foodID'] == ds['foodID']){
							if(compareNormalTasteContent(ds.tasteGroup.normalTasteContent,  orderedData.root[i].tasteGroup.normalTasteContent)
									&& ds.tasteGroup.tempTaste == orderedData.root[i].tasteGroup.tempTaste){
								orderedData.root[i].count = inputCount;
								break;
							}
						}
					}
					orderedStore.loadData(orderedData);					
					orderedGrid.getSelectionModel().selectRow(dishOrderCurrRowIndex_);
					orderIsChanged = true;
				}
			}
		}, {
			text : "取消",
			handler : function() {
				dishCountInputWin.hide();
			}
		} ],
	listeners : {
		show : function(thiz) {
			var f = Ext.getCmp("dishCountInput");
			f.focus.defer(100, f); 
			f.setValue();
		}
	},
	keys : [{
		key : Ext.EventObject.ENTER,
		scope : this,
		fn : function(){
			Ext.getCmp('btnSetFoodCount').handler(); 
		}
	}]
});

function dishOptDeleteHandler(rowIndex) {
	if(orderedGrid.getSelectionModel().getSelections().length != 1){
		Ext.example.msg('提示', '请选中一条菜品再进行操作!');
		return;
	}
	if (dishOrderCurrRowIndex_ != -1) {
		Ext.MessageBox.show({
			msg : "您确定要删除此菜品？",
			width : 300,
			buttons : Ext.MessageBox.YESNO,
			fn : function(btn) {
				if (btn == "yes") {
					var ds = orderedGrid.getStore().getAt(rowIndex).data;
					for(var i = 0; i < orderedData.root.length; i++){	
						if(eval(orderedData.root[i]['foodID'] == ds['foodID'])){
							if(compareNormalTasteContent(ds.tasteGroup.normalTasteContent,  orderedData.root[i].tasteGroup.normalTasteContent)
									&& ds.tasteGroup.tempTaste == orderedData.root[i].tasteGroup.tempTaste){
								orderedData.root.splice(i,1);
								break;
							}
						}
					}
					orderedStore.loadData(orderedData);
					orderIsChanged = true;
					dishOrderCurrRowIndex_ = -1;
				}
			}
		});
	}
};


function dishOptDispley(value, cellmeta, record, rowIndex, columnIndex, store) {
	return  '<a href=\"javascript:dishOptDeleteHandler(' + rowIndex + ')\">' + '删除</a>'
			+ '';
};

var orderedStore = new Ext.data.Store({
	proxy : new Ext.data.MemoryProxy(orderedData),
	reader : new Ext.data.JsonReader(Ext.ux.readConfig,
	[
	    {name : 'foodName'},
	    {name : 'displayFoodName'}, 
		{name : 'count'}, 
		{name : 'unitPrice'}, 
		{name : 'dishOpt'}, 
		{name : 'discount'}, 
		{name : 'orderDateFormat'}, 
		{name : 'waiter'}, 		    
		{name : 'acturalPrice'}, 
		{name : 'foodID'}, 
		{name : 'aliasID'},
		{name : 'currPrice'}, 
		{name : 'gift'}, 
		{name : 'recommend'}, 
		{name : 'soldout'}, 
		{name : 'special'}, 
		{name : 'seqID'}, 
		{name : 'status'},
		{name : 'count'},
		{name : 'temporary'},
		{name : 'kitchen'},
		{name : 'tastePref'},
		{name : 'tastePrice'},
		{name : 'tasteGroup'}
	]
	),
	listeners : {
		load : function(thiz, records){
			for(var i = 0; i < records.length; i++){
				Ext.ux.formatFoodName(records[i], 'displayFoodName', 'foodName');				
			}
		}
	}
});

var orderedColumnModel = new Ext.grid.ColumnModel([
	new Ext.grid.RowNumberer(),
	{
		header : "菜名",
		dataIndex : "displayFoodName",
		width : 200
	}, {
		header : "口味",
		sortable : true,
		dataIndex : "tastePref",
		width : 150
	}, {
		header : "数量",
		sortable : true,
		dataIndex : "count",
		width : 60,
		align : 'right',
		renderer : Ext.ux.txtFormat.gridDou
	}, {
		header : "单价",
		sortable : true,
		dataIndex : "unitPrice",
		width : 60,
		align : 'right',
		renderer : Ext.ux.txtFormat.gridDou
	}, {
		header : "折扣率",
		sortable : true,
		dataIndex : "discount",
		width : 60,
		align : 'right',
		renderer : Ext.ux.txtFormat.gridDou
	}, {
		header : "时间",
		sortable : true,
		dataIndex : "orderDateFormat",
		width : 150
	}, {
		header : "服务员",
		dataIndex : "waiter",
		width : 80
	} , {
		header : "操作",
		dataIndex : "dishOpt",
		align : 'center',
		width : 70,
		renderer : dishOptDispley
	}
]);

var btnSubmitOrder = new Ext.Button({
	text : '提交',
	listeners : {
		render : function(thiz){
			thiz.getEl().setWidth(80, true);
		}
	},
	handler : function() {
		if (typeof(orderedData.root) != 'undefined' && orderedData.root.length > 0 && billGenModForm.findById('serviceRate').isValid()) {
			billListRefresh();
			var foodPara = '';
			for ( var i = 0; i < orderedData.root.length; i++) {
				foodPara += ( i > 0 ? '<<sh>>' : '');
				if (orderedData.root[i].temporary == false) {
					// [是否临时菜(false),菜品1编号,菜品1数量,口味1编号,厨房1编号,菜品1折扣,2nd口味1编号,3rd口味1编号]
					var normalTaste = '', tempTaste = '' , tasteGroup = orderedData.root[i].tasteGroup;
					for(var j = 0; j < tasteGroup.normalTasteContent.length; j++){
						var t = tasteGroup.normalTasteContent[j];
						normalTaste += ((j > 0 ? '<<stnt>>' : '') + (t.tasteID + '<<stb>>' + t.tasteAliasID + '<<stb>>' + t.tasteCategory));
					}
					if(tasteGroup.tempTaste != null && typeof tasteGroup.tempTaste != 'undefined'){
						if(tasteGroup.tempTaste.tasteName != '' && eval(tasteGroup.tempTaste.tasteID > 0))
							tempTaste = tasteGroup.tempTaste.tastePrice + '<<sttt>>' + tasteGroup.tempTaste.tasteName  + '<<sttt>>' + tasteGroup.tempTaste.tasteID+ '<<sttt>>' + tasteGroup.tempTaste.tasteAliasID; 
					}
					foodPara = foodPara 
							+ '['
							+ 'false' + '<<sb>>'// 是否临时菜(false)
							+ orderedData.root[i].aliasID + '<<sb>>' // 菜品1编号
							+ orderedData.root[i].count + '<<sb>>' // 菜品1数量
							+ (normalTaste + ' <<st>> ' + tempTaste) + '<<sb>>' // 口味1编号
							+ orderedData.root[i].kitchenID + '<<sb>>'// 厨房1编号
							+ orderedData.root[i].discount + '<<sb>>' // 折扣率
							+ orderedData.root[i].hangStatus + '<<sb>>' // 菜品叫起状态
							+ orderedData.root[i].status  // 菜品操作状态 1:已点菜 2:新点菜 3:反结账 
							+ ']';
				} else {
					var foodname = orderedData.root[i].foodName;
					foodname = foodname.indexOf('<') > 0 ? foodname.substring(0,foodname.indexOf('<')) : foodname;
					// 是否临时菜(true),临时菜1编号,临时菜1名称,临时菜1数量,临时菜1单价									
					foodPara = foodPara 
							+ '['
							+'true' + '<<sb>>'// 是否临时菜(true)
							+ orderedData.root[i].foodID + '<<sb>>' // 临时菜1编号
							+ foodname + '<<sb>>' // 临时菜1名称
							+ orderedData.root[i].count + '<<sb>>' // 临时菜1数量
							+ orderedData.root[i].unitPrice + '<<sb>>' // 临时菜1单价(原材料單價)
							+ orderedData.root[i].hangStatus + '<<sb>>' // 菜品叫起状态
							+ orderedData.root[i].status  // 菜品操作状态 1:已点菜 2:新点菜 3:反结账
							+ ']';
				}
			}
			foodPara = '{'+ foodPara + '}';
			
//			var payMannerOut = billGenModForm.getForm().findField("payManner").getGroupValue();
			var payMannerOut = null;
			var payManner = document.getElementsByName('radioPayType');
			for(var i = 0; i < payManner.length; i++){
				if(payManner[i].checked == true ){
					payMannerOut = payManner[i].value;
					break;
				}
			}
			
			var serviceRateIn = billGenModForm.findById("serviceRate").getValue();
			var commentOut = billGenModForm.findById("remark").getValue();
			var discountID = Ext.getCmp('comboDiscount');
			var erasePrice = Ext.getCmp('numErasePrice');
			
			if(typeof sysSetting.setting != 'undefined' && erasePrice.getValue() > sysSetting.setting.eraseQuota){
				Ext.example.msg('提示', '抹数金额不能大于系统设置,请重新输入.');
				return;
			}
			
			orderedGrid.buttons[0].setDisabled(true);
			orderedGrid.buttons[1].setDisabled(true);
			Ext.Ajax.request({
				url : "../../UpdateOrder2.do",
				params : {
					"pin" : pin,
					"orderID" : orderBasicMsg["id"],
					'tableAlias' : orderedData.other.order.tableAlias,
					"category" : orderBasicMsg["category"],
					"customNum" : orderBasicMsg['customNum'],
					"payType" : orderBasicMsg['settleTypeValue'],
					'discountID' : discountID.getValue(),
					"payManner" : payMannerOut,
					"serviceRate" : serviceRateIn,
					"memberID" : orderBasicMsg['memberID'],
					"comment" : commentOut,
					"foods" : foodPara,
					'erasePrice' : erasePrice.getValue()
				},
				success : function(response, options) {
					var resultJSON = Ext.util.JSON.decode(response.responseText);
					if (resultJSON.success == true) {
						// 彈出成功提示語，打印提示語
						Ext.MessageBox.show({
							msg : resultJSON.data + "，是否打印账单？",
							width : 300,
							buttons : Ext.MessageBox.YESNO,
							fn : function(btn) {
								if (btn == "yes") {
									var tempMask = new Ext.LoadMask(document.body, {
										msg : '正在打印请稍候.......',
										remove : true
									});
									tempMask.show();
									Ext.Ajax.request({
										url : "../../PrintOrder.do",
										params : {
											"pin" : pin,
											"orderID" : Request["orderID"],
//											"printReceipt" : 1
											'printType' : 3
										},
										success : function(response, options) {
											tempMask.hide();
											var jr = Ext.util.JSON.decode(response.responseText);
											Ext.MessageBox.show({
												msg : jr.msg,
												width : 300,
												buttons : Ext.MessageBox.OK,
												fn : function() {
													location.href = "Bills.html?pin="
																	+ pin
																	+ "&restaurantID="
																	+ restaurantID;
												}
											});
										},
										failure : function(response, options) {
											tempMask.hide();
											Ext.ux.showMsg(Ext.decode(response.responseText));
										}
									});
								} else {
									location.href = "Bills.html?pin="
													+ pin
													+ "&restaurantID="
													+ restaurantID;
								}
							}
						});
					} else {
						orderedGrid.buttons[0].setDisabled(false);
						orderedGrid.buttons[1].setDisabled(false);
						Ext.MessageBox.show({
							msg : resultJSON.data,
							width : 300,
							buttons : Ext.MessageBox.OK
						});
					}
				},
				failure : function(response, options) {
					orderedGrid.buttons[0].setDisabled(false);
					orderedGrid.buttons[1].setDisabled(false);
					Ext.MessageBox.show({
						msg : "Unknow page error",
						width : 300,
						buttons : Ext.MessageBox.OK
					});
				}
			});
		}
	}
});

var orderedGrid = new Ext.grid.EditorGridPanel({
	title : '已点菜',
	xtype : "grid",
	region : 'center',
	margins : '5 0 0 0',
	frame : true,
	clicksToEdit : 1,
	loadMask : { msg: '数据请求中，请稍后...' }, 
	ds : orderedStore,
	cm : orderedColumnModel,
	sm : new Ext.grid.RowSelectionModel({
		singleSelect : true
	}),
	tbar : new Ext.Toolbar({
		height : 55,
		items : [ 
		    { xtype : 'tbtext', text : "&nbsp;&nbsp;&nbsp;"	},
			dishDeleteImgBut, 
			{ xtype : 'tbtext', text : "&nbsp;&nbsp;&nbsp;"	},
			'-', dishDeleteImgBut, 
			{ xtype : 'tbtext', text : "&nbsp;&nbsp;&nbsp;"	},
			countAddImgBut,
			{ xtype : 'tbtext', text : "&nbsp;&nbsp;&nbsp;"	},
			countMinusImgBut, 
			{ xtype : 'tbtext', text : "&nbsp;&nbsp;&nbsp;"	},
			countEqualImgBut, 
			{ xtype : 'tbtext', text : "&nbsp;&nbsp;&nbsp;"	}
		]
	}),
	buttonAlign : 'center',
	buttons : [
		btnSubmitOrder,
	    {
			xtype : 'button',
			text : "返回",
			handler : function() {
				if (orderIsChanged == false) {
					location.href = "Bills.html?pin=" + pin + "&restaurantID=" + restaurantID;
				} else {
					Ext.MessageBox.show({
						msg : "账单修改还未提交，是否确认返回？",
						width : 300,
						buttons : Ext.MessageBox.YESNO,
						fn : function(btn) {
							if (btn == "yes") {
								location.href = "Bills.html?pin=" + pin + "&restaurantID=" + restaurantID;
							}
						}
					});
				}
			}
		}
	],
	listeners : {
		rowclick : function(thiz, rowIndex, e) {
			dishOrderCurrRowIndex_ = rowIndex;
		},
		afteredit : function(Obj) {
			var row = Obj.row;
			var editValue = Obj.value;
			orderedData[row][13] = editValue;
		}
	}
});

var allFoodTabPanelGridTbar = new Ext.Toolbar({
	items : [
		{xtype:'tbtext', text:'过滤:'},
		{
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
						kitchen.setValue(254);
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
		}, 
		{xtype:'tbtext', text:'&nbsp;&nbsp;'},
		new Ext.form.ComboBox({
			xtype : 'combo',
			id : 'comSearchKitchen',
 	    	width : 100,
 	    	listWidth : 100,
 	    	store : new Ext.data.JsonStore({
				fields : [ 'kitchenAliasID', 'kitchenName' ],
				data : [{
					kitchenAliasID : 254,
			    	kitchenName : '全部'
				}, {
					kitchenAliasID : 255,
			    	kitchenName : '空'
				}]
			}),
			valueField : 'kitchenAliasID',
			displayField : 'kitchenName',
			value : 254,
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
							var jr = Ext.util.JSON.decode(response.responseText);
							var root = jr.root;
							thiz.store.loadData(root, true);
						}
					});
				},
				select : function(thiz, r, index){
					
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
				allFoodTabPanelGrid.getStore().load({
					params : {
						start : 0,
						limit : 30
					}
				});
			}
		}
	]
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
	    ['编号', 'foodAliasID', 70] , 
		['拼音', 'pinyin', 70], 
		['价格', 'unitPrice', 70, 'right', 'Ext.ux.txtFormat.gridDou']
	],
	['displayFoodName', 'foodName', 'aliasID', 'foodID', 'pinyin', 'unitPrice', 'stop', 'special', 'recommend', 'gift', 'currPrice', 'combination', 'kitchen.kitchenID', 'kitchen', 'discount'],
	[['pin',pin], ['type', 1], ['restaurantID', restaurantID], ['isPaging', true]],
	30,
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
		Ext.ux.formatFoodName(records[i], 'displayFoodName', 'foodName');
	}
});
allFoodTabPanelGrid.on('rowdblclick', function(thiz, ri, e){
	var r = thiz.getStore().getAt(ri);
	if(r.get('stop') == true){
		Ext.example.msg('提示', '该菜品已停售,请重新选择.');
	}else{
		var isAlreadyOrderd = true;
		if(typeof(orderedData) != 'undefined' && typeof(orderedData.root) != 'undefined' ){
			for ( var i = 0; i < orderedData.root.length; i++) {
				if (orderedData.root[i].foodID == r.get('foodID')) {
					if(orderedData.root[i].tasteGroup.normalTasteContent.length == 0){
						orderedData.root[i].count += 1;
						isAlreadyOrderd = false;
						break;
					}
				}
			}
		}
		if (isAlreadyOrderd) {
			orderedData.root.push({
				foodID : r.get('foodID'),
				aliasID : r.get('aliasID'),
				foodName : r.get('foodName'),
				count : 1,
				unitPrice : r.get('unitPrice'),
				acturalPrice : r.get('unitPrice'),
				orderDateFormat : new Date().format('Y-m-d H:i:s'),
				waiter : Ext.getDom('optName').innerHTML,
				kitchenID : r.get('kitchen.kitchenID'),
				kitchen : r.get('kitchen'),
				tasteID : 0,
				special : r.get('special'),
				recommend : r.get('recommend'),
				soldout : r.get('stop'),
				gift : r.get('gift'),
				currPrice : r.get('currPrice'),
				temporary : false,
				hangStatus : 0,
				tastePref : '无口味',
				tastePrice : 0,
				tasteGroup : {
					normalTaste : null,
					normalTasteContent : [],
					tempTaste : null
				}
			});
		}
		orderedStore.loadData(orderedData);	
		billListRefresh();
	}
});

var dishesOrderEastPanel = new Ext.Panel({
	title : '菜单',
	region : 'east',
	width : 450,
	frame : true,
	layout : 'fit',
	margins : '5 0 0 5',
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

var billGenModForm = new Ext.Panel({
	region : 'north',
	height : 62,
	frame : true,
	layout : 'column',
	defaults : {
		xtype : 'panel',
		layout : 'column',
		columnWidth : 1,
		defaults : {
			xtype : 'form',
			layout : 'form',
			labelWidth : 60,
			width : 220,
			defaults : {
				width : 130
			}
		}
	},
	items : [{
		items : [{
			items : [{
				xtype : 'textfield',
				id : 'txtSettleTypeFormat',
				fieldLabel : '结账方式',
				value : '一般/会员',
				disabled : true
			}]
		}, {
			items : [{
				xtype : 'combo',
				id : 'comboDiscount',
//				width : 100,
				fieldLabel : '折扣方案',
				readOnly : true,
				forceSelection : true,
				store : new Ext.data.JsonStore({
					root : 'root',
					fields : [ 'discountID', 'discountName']
				}),
				valueField : 'discountID',
				displayField : 'discountName',
				typeAhead : true,
				mode : 'local',
				triggerAction : 'all',
				selectOnFocus : true,
				listeners : {
					select : function(combo, record, index) {
						billListRefresh();
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
			width : 150,
			id : 'panelShowEraseQuota',
			style : 'font-size:18px;',
			html : '上限:￥<font id="fontShowEraseQuota" style="color:red;">0.00</font>'
		}, {
			items : [{
				xtype : 'numberfield',
				width : 60,
				fieldLabel : '服务费',
				id : 'serviceRate',
				allowBlank : false,
				validator : function(v) {
					if (v < 0 || v > 100 || v.indexOf('.') != -1) {
						return '服务费率范围是0%至100%,且为整数.';
					} else {
						return true;
					}
				}
			}]
		}]
	}, {
		defaults : {
			labelWidth : 1,
			labelSeparator : ' ',
		},
		items : [{
			xtype : 'label',
			width : 65,
			text : '收款方式:'
		}, {
			width : 80,
			items : [{
				xtype : 'radio',
				name : 'radioPayType',
				boxLabel : '现金结账',
				inputValue : '1'
			}]
		}, {
			width : 80,
			items : [{
				xtype : 'radio',
				name : 'radioPayType',
				boxLabel : '刷卡结账',
				inputValue : '2'
			}]
		}, {
			width : 80,
			items : [{
				xtype : 'radio',
				name : 'radioPayType',
				boxLabel : '会员消费',
				inputValue : '3',
				disabled : true
			}]
		}, {
			width : 60,
			items : [{
				xtype : 'radio',
				name : 'radioPayType',
				boxLabel : '签单',
				inputValue : '4'
			}]
		}, {
			width : 75,
			items : [{
				xtype : 'radio',
				name : 'radioPayType',
				boxLabel : '挂账',
				inputValue : '5'
			}]
		}, {
			xtype : 'form',
			labelWidth : 60,
			labelSeparator : ':',
			items : [{
				xtype : 'textfield',
				id : 'remark',
				fieldLabel : '备注',
				width : 420
			}]
		}]
	}]
});

Ext.onReady(function(){
	Ext.lib.Ajax.defaultPostHeader += '; charset=utf-8';
	Ext.QuickTips.init();
	
	var centerPanelDO = new Ext.Panel({
		id : "centerPanelDO",
		region : "center",
		border : false,
		layout : "border",
		items : [ orderedGrid, dishesOrderEastPanel, billGenModForm ]
	});
	
	var billModCenterPanel = new Ext.Panel({
		id : "billModCenterPanel",
		region : "center",
		layout : "border",
		frame : true,
		title : '&nbsp;<span style="padding-left:2px; color:red;">' + orderID + '</span>&nbsp;号帐单修改',
		items : [ centerPanelDO ]
	});
	
	new Ext.Viewport({
		layout : "border",
		id : "viewport",
		items : [{
			region : "north",
			bodyStyle : "background-color:#DFE8F6;",
			html : "<h4 style='padding:10px;font-size:150%;float:left;'>无线点餐网页终端</h4><div id='optName' class='optName'></div>",
			height : 50,
			border : false,
			margins : "0 0 0 0"
		},
		billModCenterPanel,
		{
			region : "south",
			height : 30,
			layout : "form",
			frame : true,
			border : false,
			html : "<div style='font-size:11pt; text-align:center;'><b>版权所有(c) 2011 智易科技</b></div>"
		}]
	});
});
