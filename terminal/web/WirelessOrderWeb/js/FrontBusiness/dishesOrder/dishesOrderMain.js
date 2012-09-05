
dishCountInputWin = new Ext.Window({
	layout : 'fit',
	width : 200,
	height : 100,
	closeAction : 'hide',
	resizable : false,
	items : [ {
		layout : 'form',
		labelWidth : 30,
		border : false,
		frame : true,
		items : [ {
			xtype : 'numberfield',
			fieldLabel : '数量',
			id : 'dishCountInput',
			width : 110
		} ]
	} ],
	buttons : [
			{
				text : '确定',
				handler : function() {
					var inputCount = dishCountInputWin.findById('dishCountInput');
					if (inputCount.getValue() != '' && inputCount.getValue() > 0 && inputCount.getValue() < 65535) {
						dishCountInputWin.hide();
						var ds = orderedGrid.getStore().getAt(dishOrderCurrRowIndex_).data;
						for(var i = 0; i < orderedData.root.length; i++){						
							if(ds.foodID == orderedData.root[i].foodID){							
								orderedData.root[i].count = inputCount.getValue();
								break;
							}
						}
						orderedStore.loadData(orderedData);	
						// 底色处理，已点菜式原色底色
						dishGridRefresh();
						orderedGrid.getSelectionModel().selectRow(dishOrderCurrRowIndex_);
						orderIsChanged = true;
					}

				}
			}, {
				text : '取消',
				handler : function() {
					dishCountInputWin.hide();
				}
			} ],
	listeners : {
		show : function(thiz) {
			// thiz.findById('personCountInput').focus();
			var f = Ext.get('dishCountInput');
			f.focus.defer(100, f); // 万恶的EXT！为什么这样才可以！？！？
		}
	}
});

// --------------dishes order center panel-----------------

// 已点菜式
// 2，表格的数据store
var orderedStore = new Ext.data.Store({
	proxy : new Ext.data.MemoryProxy(orderedData),
	reader : new Ext.data.JsonReader(Ext.ux.readConfig,
		    [ 
			    {name : 'foodName'}, 
			    {name : 'tastePref'}, 
			    {name : 'count'}, 
			    {name : 'unitPrice'}, 
			    {name : 'dishOpt'}, 
			    {name : 'discount'}, 
			    {name : 'orderDateFormat'}, 
			    {name : 'waiter'}, 		    
			    {name : 'acturalPrice'}, 
			    {name : 'foodID'}, 
			    {name : 'currPrice'}, 
			    {name : 'gift'}, 
			    {name : 'recommed'}, 
			    {name : 'soldout'}, 
			    {name : 'special'}, 
			    {name : 'seqID'}, 
			    {name : 'status'},
			    {name : 'count'},
			    {name : 'tasteID'},
			    {name : 'tasteIDThree'},
			    {name : 'tasteIDTwo'},
			    {name : 'tastePrice'},
			    {name : 'temporary'},
			    {name : 'aliasID'}
		    ]
		)
});

orderedStore.reload();
// 底色处理，已点菜式原色底色
dishGridRefresh();

var dishPushBackWin = new Ext.Window({
	layout : 'fit',
	width : 220,
	height : 120,
	closeAction : 'hide',
	resizable : false,
	items : [ {
		layout : 'form',
		labelWidth : 60,
		border : false,
		frame : true,
		items : [ 
		    {
		    	xtype : 'numberfield',
				fieldLabel : '退菜数量',
				id : 'dishPushBackCount',
				allowBlank : false,
				width : 110
			}, {
				xtype : 'textfield',
				inputType : 'password',
				fieldLabel : '密码',
				id : 'dishPushBackPwd',
				width : 110
			} ]
		} ],
		buttons : [
		    {
				text : '确定',
				handler : function() {
					if (dishPushBackWin.findById('dishPushBackCount') .isValid()) {
						var dishPushBackPwd = dishPushBackWin.findById('dishPushBackPwd').getValue();
						dishPushBackWin.findById('dishPushBackPwd').setValue('');

						var pwdTrans;
						if (dishPushBackPwd != '') {
							pwdTrans = MD5(dishPushBackPwd);
						} else {
							pwdTrans = dishPushBackPwd;
						}
						dishPushBackWin.hide();

						Ext.Ajax.request({
							url : '../../VerifyPwd.do',
							params : {
								'pin' : Request['pin'],
								'type' : '5',
								'pwd' : pwdTrans
							},
							success : function(response, options) {
								var resultJSON = Ext.util.JSON.decode(response.responseText);
								if (resultJSON.success == true) {
									var pushCount = dishPushBackWin.findById('dishPushBackCount').getValue();
									pushCount = parseFloat(pushCount);
									dishPushBackWin.findById('dishPushBackCount').setValue('');
									var ds = orderedGrid.getStore().getAt(dishOrderCurrRowIndex_).data;
									for(var i = 0; i < orderedData.root.length; i++){
										if(orderedData.root[i].foodID == ds.foodID){
											if((orderedData.root[i].count - pushCount) <= 0){
												orderedData.root.splice(i,1);
											}else if((orderedData.root[i].count - pushCount)> 0){
												orderedData.root[i].count -= pushCount;
											}
											break;
										}
									}
									
									orderedStore.loadData(orderedData);
									// 底色处理，已点菜式原色底色
									dishGridRefresh();
									orderIsChanged = true;
									dishOrderCurrRowIndex_ = -1;

									Ext.MessageBox.show({
										msg : resultJSON.data,
										width : 300,
										buttons : Ext.MessageBox.OK
									});
								} else {
									Ext.MessageBox.show({
										msg : resultJSON.data,
										width : 300,
										buttons : Ext.MessageBox.OK
									});
								}
							},
							failure : function(response, options) {
								
							}
						});
					}
				}
			},
			{
				text : '取消',
				handler : function() {
					dishPushBackWin.hide();
					dishPushBackWin.findById('dishPushBackPwd').setValue('');
				}
			} ],
	listeners : {
		show : function(thiz) {
			// thiz.findById('personCountInput').focus();
			var f = Ext.get('dishPushBackPwd');
			f.focus.defer(100, f); // 万恶的EXT！为什么这样才可以！？！？
			var ds = orderedGrid.getStore().getAt(dishOrderCurrRowIndex_).data;
			for(var i = 0; i < orderedData.root.length; i++){
				if(orderedData.root[i].foodID == ds.foodID){
					thiz.findById('dishPushBackCount').setValue(ds.count);
				}
			}
			
		}
	}
});

function dishOptDeleteHandler(rowIndex) {

	if (dishOrderCurrRowIndex_ != -1) {
		var ds = orderedGrid.getStore().getAt(rowIndex).data;		
		if (ds.status == 1) {
			dishPushBackWin.show();
		} else {
			Ext.MessageBox.show({
				msg : '您确定要删除此菜品？',
				width : 300,
				buttons : Ext.MessageBox.YESNO,
				fn : function(btn) {
					if (btn == 'yes') {						
						for(var i = 0; i < orderedData.root.length; i++){						
							if(ds.foodID == orderedData.root[i].foodID){							
								orderedData.root.splice(i,1);
								break;
							}
						}						
						orderedStore.loadData(orderedData);
						// 底色处理，已点菜式原色底色
						dishGridRefresh();
						orderIsChanged = true;
						dishOrderCurrRowIndex_ = -1;
					}
				}
			});
		}
	}
};
function dishOptPressHandler(rowIndex) {

	if (dishOrderCurrRowIndex_ != -1) {
		// Ext.Msg.alert('', '已催菜！');
		// orderedStore.reload();
		// dishOrderCurrRowIndex_ = -1;
	}

};
function dishOptDispley(value, cellmeta, record, rowIndex, columnIndex, store) {
	return ''
			+ '<a id="tasteLink' + rowIndex + '" href="javascript:dishOptTasteHandler(' + rowIndex + ')">'
			+ '<img src="../../images/Modify.png"/>口味</a>'
			+ '&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;'
			+ '<a href="javascript:dishOptDeleteHandler(' + rowIndex + ')">'
			+ '<img src="../../images/del.png"/>删除</a>'
			+ '&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;'
			+ '<a href="javascript:dishOptPressHandler(' + rowIndex + ')">'
			+ '<img src="../../images/Modify.png"/>催菜</a>'
			+'';
};

function dishWaiterDispley(value, cellmeta, record, rowIndex, columnIndex, store) {
	return Ext.getDom('optName').innerHTML;
}

var orderedColumnModel = new Ext.grid.ColumnModel([ new Ext.grid.RowNumberer(),
		{
			header : '菜名',
			dataIndex : 'foodName',
			width : 200
		}, {
			header : '口味',
			dataIndex : 'tastePref',
			width : 160
		}, {
			header : '数量',
			sortable : true,
			dataIndex : 'count',
			width : 80,
			align : 'right',
			renderer : Ext.ux.txtFormat.gridDou
		}, {
			header : '单价',
			sortable : true,
			dataIndex : 'unitPrice',
			width : 80,
			align : 'right',
			renderer : Ext.ux.txtFormat.gridDou
		}, {
			header : '时间',
			sortable : true,
			dataIndex : 'orderDateFormat',
			width : 160
		}, {
			header : '服务员',
			dataIndex : 'waiter',
			width : 80
		}, {
			header : '操作',
			dataIndex : 'dishOpt',
			align : 'center',
			width : 230,
			renderer : dishOptDispley
		} ]);

// 4，表格
var tasteChooseImgBut = new Ext.ux.ImageButton({
	imgPath : '../../images/Taste.png',
	imgWidth : 50,
	imgHeight : 50,
	tooltip : '口味',
	handler : function(btn) {
		dishOptTasteHandler(dishOrderCurrRowIndex_);
	}
});

var dishDeleteImgBut = new Ext.ux.ImageButton({
	imgPath : '../../images/DeleteDish.png',
	imgWidth : 50,
	imgHeight : 50,
	tooltip : '删除',
	handler : function(btn) {
		dishOptDeleteHandler(dishOrderCurrRowIndex_);
	}
});
var dishPressImgBut = new Ext.ux.ImageButton({
	imgPath : '../../images/HurryFood.png',
	imgWidth : 50,
	imgHeight : 50,
	tooltip : '催菜',
	handler : function(btn) {
		dishOptPressHandler(dishOrderCurrRowIndex_);
	}
});
var countAddImgBut = new Ext.ux.ImageButton(
		{
			imgPath : '../../images/AddCount.png',
			imgWidth : 50,
			imgHeight : 50,
			tooltip : '数量加1',
			handler : function(btn) {
				if (dishOrderCurrRowIndex_ != -1) {
					var ds = orderedGrid.getStore().getAt(dishOrderCurrRowIndex_).data;		
					if (ds.status == 2) {									
						for(var i = 0; i < orderedData.root.length; i++){						
							if(ds.foodID == orderedData.root[i].foodID){							
								orderedData.root[i].count += 1;
								break;
							}
						}
						orderedStore.loadData(orderedData);
						dishGridRefresh();
						orderedGrid.getSelectionModel().selectRow(dishOrderCurrRowIndex_);
						orderIsChanged = true;
					}
				}
			}
		});
var countMinusImgBut = new Ext.ux.ImageButton(
		{
			imgPath : '../../images/MinusCount.png',
			imgWidth : 50,
			imgHeight : 50,
			tooltip : '数量减1',
			handler : function(btn) {
				if (dishOrderCurrRowIndex_ != -1) {
					var ds = orderedGrid.getStore().getAt(dishOrderCurrRowIndex_).data;		
					if (ds.status == 2) {
						if (ds.count > 1) {
							for(var i = 0; i < orderedData.root.length; i++){						
								if(ds.foodID == orderedData.root[i].foodID){							
									orderedData.root[i].count -= 1;
									break;
								}
							}
							orderedStore.loadData(orderedData);
							// 底色处理，已点菜式原色底色
							dishGridRefresh();
							orderedGrid.getSelectionModel().selectRow(dishOrderCurrRowIndex_);
							orderIsChanged = true;
						}
					}
				}

			}
		});
var countEqualImgBut = new Ext.ux.ImageButton({
	imgPath : '../../images/EqualCount.png',
	imgWidth : 50,
	imgHeight : 50,
	tooltip : '数量等于',
	handler : function(btn) {
		if (dishOrderCurrRowIndex_ != -1) {
			var ds = orderedGrid.getStore().getAt(dishOrderCurrRowIndex_).data;	
			if (ds.status == 2) {
				dishCountInputWin.show();
			}
		}
	}
});
var printTotalImgBut = new Ext.ux.ImageButton({
	imgPath : '../../images/PrintTotal.png',
	imgWidth : 50,
	imgHeight : 50,
	tooltip : '补打总单',
	handler : function(btn) {
		Ext.Ajax.request({
			url : '../../PrintOrder.do',
			params : {
				'pin' : Request['pin'],
				'tableID' : Request['tableNbr'],
				'printOrder' : 1
			},
			success : function(response, options) {
				var resultJSON = Ext.util.JSON.decode(response.responseText);
				Ext.MessageBox.show({
					msg : resultJSON.data,
					width : 300,
					buttons : Ext.MessageBox.OK
				});
			},
			failure : function(response, options) {
			}
		});
	}
});
var printDetailImgBut = new Ext.ux.ImageButton({
	imgPath : '../../images/PrintDetail.png',
	imgWidth : 50,
	imgHeight : 50,
	tooltip : '补打明细',
	handler : function(btn) {
		Ext.Ajax.request({
			url : '../../PrintOrder.do',
			params : {
				'pin' : Request['pin'],
				'tableID' : Request['tableNbr'],
				'printDetail' : 1
			},
			success : function(response, options) {
				var resultJSON = Ext.util.JSON.decode(response.responseText);
				Ext.MessageBox.show({
					msg : resultJSON.data,
					width : 300,
					buttons : Ext.MessageBox.OK
				});
			},
			failure : function(response, options) {
			}
		});
	}
});

var orderedGrid = new Ext.grid.GridPanel({
	title : '已点菜式',
	xtype : 'grid',
	anchor : '100%',
	region : 'center',
	border : false,
	ds : orderedStore,
	cm : orderedColumnModel,
	viewConfig : {
		forceFit : true
	},
	sm : new Ext.grid.RowSelectionModel({
		singleSelect : true
	}),
	tbar : new Ext.Toolbar({
		height : 55,
		items : [ 
			{xtype:'tbtext', text:'&nbsp;&nbsp;&nbsp;&nbsp;'}, 
			tasteChooseImgBut, 
			{xtype:'tbtext', text:'&nbsp;&nbsp;&nbsp;&nbsp;'}, 
			dishDeleteImgBut, 
			{xtype:'tbtext', text:'&nbsp;&nbsp;&nbsp;&nbsp;'}, 
			dishPressImgBut, 
			dishDeleteImgBut, 
			{xtype:'tbtext', text:'&nbsp;&nbsp;&nbsp;&nbsp;'}, 
			'-', 
			{xtype:'tbtext', text:'&nbsp;&nbsp;&nbsp;&nbsp;'}, 
			countAddImgBut, 
			{xtype:'tbtext', text:'&nbsp;&nbsp;&nbsp;&nbsp;'},  
			countMinusImgBut, 
			{xtype:'tbtext', text:'&nbsp;&nbsp;&nbsp;&nbsp;'}, 
			countEqualImgBut,
			{xtype:'tbtext', text:'&nbsp;&nbsp;&nbsp;&nbsp;'}
		]
	}),
	listeners : {
		rowclick : function(thiz, rowIndex, e) {
			dishOrderCurrRowIndex_ = rowIndex;
		},
		render : function(thiz) {
			orderedDishesOnLoad();
			tableStuLoad();
		}
	}
});

var orderedForm = new Ext.form.FormPanel({
	frame : true,
	border : false,
	region : 'south',
	height : 60,
	items : [ {} ],
	buttons : [
		{
			// tableID='100'&customNum='3'&foods='{[1100,2,1,0]}'
			// 各字段表示的意义：
			// tableID：餐台号
			// customNum：就餐人数
			// foods：菜品列表，格式为{[菜品1编号,菜品1数量,口味1编号,厨房1编号,菜品1折扣,2nd口味1编号,3rd口味1编号]}
			// 以点菜式格式：[菜名，口味，数量，￥单价，操作，￥实价，菜名编号，厨房编号，口味编号1,特,荐,停,送,￥口味价钱,口味编号2,口味编号3]
			text : '提交',
			handler : function() {
				if (orderedData.root.length > 0) {
					var inputPersCount = dishesOrderNorthPanel.findById('tablePersonCount').getValue();
					inputPersCount = inputPersCount == '' || inputPersCount == 0 ? 1 : inputPersCount;
					var Request = new URLParaQuery();
					var foodPara = '';
					for ( var i = 0; i < orderedData.root.length; i++) {
						foodPara += ( i > 0 ? '，' : '');
						if (orderedData.root[i].temporary == false) {
							// [是否临时菜(false),菜品1编号,菜品1数量,口味1编号,厨房1编号,菜品1折扣,2nd口味1编号,3rd口味1编号]，
							foodPara = foodPara + '[false,'// 是否临时菜(false)
												+ orderedData.root[i].aliasID + ',' // 菜品1编号
												+ orderedData.root[i].count + ',' // 菜品1数量
												+ orderedData.root[i].tasteID + ',' // 口味1编号
												+ orderedData.root[i].kitchenId + ','// 厨房1编号
												+ '0,'// 菜品1折扣
												+ orderedData.root[i].tasteIDTwo + ','// 2nd口味1编号
												+ orderedData.root[i].tasteIDThree + ',' // 3rd口味1编号
												+ orderedData.root[i].tmpTaste + ',' // 是否临时口味
												+ orderedData.root[i].tmpTastePref + ',' // 临时口味
												+ orderedData.root[i].tmpTastePrice + ','  // 临时口味价钱
												+ orderedData.root[i].tmpTasteAlias + ',' // 临时口味编号
												+ orderedData.root[i].hangStatus  // 菜品状态
												+ ']';
						} else {
							var foodname = orderedData.root[i].foodName;
							foodname = foodname.indexOf('<') > 0 ? foodname.substring(0,foodname.indexOf('<')) : foodname;
							// [是否临时菜(true),临时菜1编号,临时菜1名称,临时菜1数量,临时菜1单价]
							foodPara = foodPara + '[true,'// 是否临时菜(true)
												+ orderedData.root[i].foodID + ',' // 临时菜1编号
												+ foodname + ',' // 临时菜1名称
												+ orderedData.root[i].count + ',' // 临时菜1数量
												+ orderedData.root[i].unitPrice + ',' // 临时菜1单价(原料單價)
												+ orderedData.root[i].hangStatus  // 菜品状态
												+ ']';
						}									
					}
					foodPara = '{' + foodPara + '}';
					var type = 9;
					if (Request['tableStat'] == 'free') {
						type = 1;
					} else {
						type = 2;
					}
					
					orderedForm.buttons[0].setDisabled(true);
					orderedForm.buttons[1].setDisabled(true);
					orderedForm.buttons[2].setDisabled(true);
					// alert(foodPara);
					Ext.Ajax.request({
						url : '../../InsertOrder.do',
						params : {
							'pin' : Request['pin'],
							'tableID' : Request['tableNbr'],
							'tableID_2' : Request['tableNbr2'],
							'customNum' : inputPersCount,// get input count
							'type' : type,
							'originalTableID' : Request['tableNbr'],// no use
							'foods' : foodPara,
							'category' : category,
							'orderDate' : typeof(orderedData.other) == 'undefined' || typeof(orderedData.other.order) == 'undefined' ? '' : orderedData.other.order.orderDate
						},
						success : function(response, options) {
							
							var resultJSON = Ext.util.JSON.decode(response.responseText);
							var href = 'TableSelect.html?pin=' + Request['pin'] + '&restaurantID=' + restaurantID;
							
							if (resultJSON.success == true) {
								Ext.MessageBox.show({
									msg : resultJSON.msg,
									width : 300,
									buttons : Ext.MessageBox.OK,
									fn : function() {
										location.href = href;
									}
								});
							} else {
								if(eval(resultJSON.code == 14)){
									Ext.MessageBox.confirm('警告', '账单信息已更新,是否刷新已点菜并继续操作?否则返回.', function(btn){
										if(btn == 'yes'){
											
											var girdData = orderedData.root;
											var selData = new Array();
											
											var refresh = new Ext.LoadMask(document.body, {
											    msg  : '正在更新已点菜列表,请稍等......',
											    disabled : false,
											    removeMask : true
											});

											for(var i = (girdData.length - 1); i > 0; i--){
												if(girdData[i].status == 2){
													selData.push(girdData[i]);
												}
											}
											
											refresh.show();
											
											Ext.Ajax.request({
												url : '../../QueryOrder.do',
												params : {
													'pin' : Request['pin'],
													'tableID' : Request['tableNbr']
												},
												success : function(response, options) {
													var rj = Ext.util.JSON.decode(response.responseText);
													if (rj.success == true) {
														
														orderedData = rj;
														
														// 更新菜品状态为已点菜
														for(var i = 0; i < orderedData.root.length; i++){
															orderedData.root[i].status = 1;
														}
														
														for(var i = (selData.length - 1); i >= 0 ; i++){
															orderedData.root.push(selData[i]);
														}
														
														orderedStore.loadData(orderedData);
														
														dishGridRefresh();
														
														Ext.example.msg('提示', '已更新已点菜列表,请继续操作.');
														
													} else {
														Ext.ux.showMsg(rj);
													}
													refresh.hide();
												},
												failure : function(response, options) {
													var rj = Ext.util.JSON.decode(response.responseText);
													Ext.ux.showMsg(rj);
													refresh.hide();
												}
											});
											
										}else{
											location.href = href;
										}
									},this);
								}else if(eval(resultJSON.code == 3)){
									var interval = 3;
									var action = '<br/>点击确定返回或&nbsp;<span id="returnInterval" style="color:red;"></span>&nbsp;之后自动跳转.';
									new Ext.util.TaskRunner().start({
										run: function(){
											if(interval <= 1){
												location.href = href;
											}
											Ext.getDom('returnInterval').innerHTML = interval;
											interval--;
									    },
									    interval : 1000
									});
									Ext.MessageBox.show({
										title : resultJSON.title,
										msg : ('<center>' + resultJSON.msg + '.' + action + '</center>'),
										width : 300,
										buttons : Ext.MessageBox.OK,
										fn : function() {
											var Request = new URLParaQuery();
											if (submitType != 6) {
												location.href = "TableSelect.html?pin="
														+ Request["pin"] + "&restaurantID="
														+ restaurantID;
											}
										}
									});
								}else{
									Ext.ux.showMsg(resultJSON);
								}
								orderedForm.buttons[0].setDisabled(false);
								orderedForm.buttons[1].setDisabled(false);
								orderedForm.buttons[2].setDisabled(false);
							}
						},
						failure : function(response, options) {
							orderedForm.buttons[0].setDisabled(false);
							orderedForm.buttons[1].setDisabled(false);
							orderedForm.buttons[2].setDisabled(false);
							Ext.MessageBox.show({
								msg : 'Unknow page error',
								width : 300,
								buttons : Ext.MessageBox.OK
							});
						}
					});
				} else if (orderedData.root.length == 0) {
					Ext.MessageBox.show({
						msg : '还没有选择任何菜品，暂时不能提交',
						width : 300,
						buttons : Ext.MessageBox.OK
					});
				}
			}
		},
		{
			// tableID='100'&customNum='3'&foods='{[1100,2,1,0]}'
			// 各字段表示的意义：
			// tableID：餐台号
			// customNum：就餐人数
			// foods：菜品列表，格式为{[菜品1编号,菜品1数量,口味1编号,厨房1编号,菜品1折扣,2nd口味1编号,3rd口味1编号]}
			// 以点菜式格式：[菜名，口味，数量，￥单价，操作，￥实价，菜名编号，厨房编号，口味编号1,特,荐,停,送,￥口味价钱,口味编号2,口味编号3]
			text : '提交&结帐',
			handler : function() {
				if (orderedData.root.length > 0) {
					var inputPersCount = dishesOrderNorthPanel.findById('tablePersonCount').getValue();
					inputPersCount = inputPersCount == '' || inputPersCount == 0 ? 1 : inputPersCount;
					var Request = new URLParaQuery();
					var foodPara = '';
					for ( var i = 0; i < orderedData.root.length; i++) {
						foodPara += ( i > 0 ? '，' : '');
						if (orderedData.root[i].temporary == false) {							
							// [是否临时菜(false),菜品1编号,菜品1数量,口味1编号,厨房1编号,菜品1折扣,2nd口味1编号,3rd口味1编号]，
							foodPara = foodPara + '[false,'// 是否临时菜(false)
												+ orderedData.root[i].aliasID + ',' // 菜品1编号
												+ orderedData.root[i].count + ',' // 菜品1数量
												+ orderedData.root[i].tasteID + ',' // 口味1编号
												+ orderedData.root[i].kitchenId + ','// 厨房1编号
												+ '0,'// 菜品1折扣
												+ orderedData.root[i].tasteIDTwo + ','// 2nd口味1编号
												+ orderedData.root[i].tasteIDThree + ',' // 3rd口味1编号
												+ orderedData.root[i].tmpTaste + ',' // 是否临时口味
												+ orderedData.root[i].tmpTastePref + ',' // 临时口味
												+ orderedData.root[i].tmpTastePrice + ','  // 临时口味价钱
												+ orderedData.root[i].tmpTasteAlias +',' // 临时口味编号
												+ orderedData.root[i].hangStatus  // 菜品状态
												+ ']';
						} else {
							var foodname = orderedData.root[i].foodName;
							foodname = foodname.indexOf('<') > 0 ? foodname.substring(0,foodname.indexOf('<')) : foodname;
							// [是否临时菜(true),临时菜1编号,临时菜1名称,临时菜1数量,临时菜1单价]
							foodPara = foodPara + '[true,'// 是否临时菜(true)
												+ orderedData.root[i].foodID + ',' // 临时菜1编号
												+ foodname + ',' // 临时菜1名称
												+ orderedData.root[i].count + ',' // 临时菜1数量
												+ orderedData.root[i].unitPrice + ',' // 临时菜1单价(原料單價)
												+ orderedData.root[i].hangStatus  // 菜品状态
												+ ']';
						}									
					}
					foodPara = '{' + foodPara + '}';
					
					var type = 9;
					if (Request['tableStat'] == 'free') {
						type = 1;
					} else {
						type = 2;
					}
					orderedForm.buttons[0].setDisabled(true);
					orderedForm.buttons[1].setDisabled(true);
					orderedForm.buttons[2].setDisabled(true);
								
					Ext.Ajax.request({
						url : '../../InsertOrder.do',
						params : {
							'pin' : Request['pin'],
							'tableID' : Request['tableNbr'],
							'tableID_2' : Request['tableNbr2'],
							'customNum' : inputPersCount,// get input count
							'type' : type,
							'originalTableID' : Request['tableNbr'],// no use
							'foods' : foodPara,
							'category' : category,
							'orderDate' : typeof(orderedData.other) == 'undefined' || typeof(orderedData.other.order) == 'undefined' ? '' : orderedData.other.order.orderDate
						},
						success : function(response, options) {
							var resultJSON = Ext.util.JSON.decode(response.responseText);
							var href = 'CheckOut.html?tableNbr='
											+ Request['tableNbr']
											+ '&personCount='
											+ inputPersCount
											+ '&pin='
											+ Request['pin']
											+ '&restaurantID='
											+ restaurantID
											+ '&minCost='
											+ Request['minCost']
											+ '&serviceRate='
											+ Request['serviceRate'];
							
							if (resultJSON.success == true) {
								Ext.MessageBox.show({
									msg : resultJSON.msg,
									width : 300,
									buttons : Ext.MessageBox.OK,
									fn : function() {
										location.href = href;
									}
								});
							} else {
								if(eval(resultJSON.code == 14)){
									Ext.MessageBox.confirm('警告', '账单信息已更新,是否刷新已点菜并继续操作?否则返回.', function(btn){
										if(btn == 'yes'){
											
											var girdData = orderedData.root;
											var selData = new Array();
											
											var refresh = new Ext.LoadMask(document.body, {
											    msg  : '正在更新已点菜列表,请稍等......',
											    disabled : false,
											    removeMask : true
											});

											for(var i = (girdData.length - 1); i > 0; i--){
												if(girdData[i].status == 2){
													selData.push(girdData[i]);
												}
											}
											
											refresh.show();
											
											Ext.Ajax.request({
												url : '../../QueryOrder.do',
												params : {
													'pin' : Request['pin'],
													'tableID' : Request['tableNbr']
												},
												success : function(response, options) {
													var rj = Ext.util.JSON.decode(response.responseText);
													if (rj.success == true) {
														
														orderedData = rj;
														
														for(var i = 0; i < orderedData.root.length; i++){
															orderedData.root[i].status = 1;
														}
														
														for(var i = (selData.length - 1); i >= 0 ; i++){
															orderedData.root.push(selData[i]);
														}
														
														orderedStore.loadData(orderedData);
														
														dishGridRefresh();
														
														Ext.example.msg('提示', '已更新已点菜列表,请继续操作.');
														
													} else {
														Ext.ux.showMsg(rj);
													}
													refresh.hide();
												},
												failure : function(response, options) {
													var rj = Ext.util.JSON.decode(response.responseText);
													Ext.ux.showMsg(rj);
													refresh.hide();
												}
											});
											
										}else{
											location.href = href;
										}
									},this);
								}else if(eval(resultJSON.code == 3)){
									var interval = 3;
									var action = '<br/>点击确定返回或&nbsp;<span id="returnInterval" style="color:red;"></span>&nbsp;之后自动跳转.';
									new Ext.util.TaskRunner().start({
										run: function(){
											if(interval <= 1){
												var r = new URLParaQuery();
												var href = 'TableSelect.html?pin=' + r['pin'] + '&restaurantID=' + r['restaurantID'];
												location.href = href;
											}
											Ext.getDom('returnInterval').innerHTML = interval;
											interval--;
									    },
									    interval : 1000
									});
									Ext.MessageBox.show({
										title : resultJSON.title,
										msg : ('<center>' + resultJSON.msg + '.' + action + '</center>'),
										width : 300,
										buttons : Ext.MessageBox.OK,
										fn : function() {
											var Request = new URLParaQuery();
											if (submitType != 6) {
												location.href = "TableSelect.html?pin="
														+ Request["pin"] + "&restaurantID="
														+ restaurantID;
											}
										}
									});
								}else{
									Ext.ux.showMsg(resultJSON);
								}
								orderedForm.buttons[0].setDisabled(false);
								orderedForm.buttons[1].setDisabled(false);
								orderedForm.buttons[2].setDisabled(false);
							}
						},
						failure : function(response, options) {
							orderedForm.buttons[0].setDisabled(false);
							orderedForm.buttons[1].setDisabled(false);
							orderedForm.buttons[2].setDisabled(false);
							Ext.MessageBox.show({
								msg : 'Unknow page error',
								width : 300,
								buttons : Ext.MessageBox.OK
							});
						}
					});
				} else if (orderedData.root.length == 0) {
					Ext.MessageBox.show({
						msg : '还没有选择任何菜品，暂时不能提交',
						width : 300,
						buttons : Ext.MessageBox.OK
					});
				}
			}
		},
		{
			text : '返回',
			handler : function() {
				var Request = new URLParaQuery();
				if (orderIsChanged == false) {
					location.href = 'TableSelect.html?pin='
									+ Request['pin'] + '&restaurantID='
									+ restaurantID;
				} else {
					Ext.MessageBox.show({
						msg : '下/改单还未提交，是否确认退出？',
						width : 300,
						buttons : Ext.MessageBox.YESNO,
						fn : function(btn) {
							if (btn == 'yes') {
								location.href = 'TableSelect.html?pin='
												+ Request['pin']
												+ '&restaurantID='
												+ restaurantID;
							}
						}
					});
				}
			}
		}]
	});

var dishesOrderCenterPanel = new Ext.Panel({
	region : 'center',
	id : 'dishesOrderCenterPanel',
	layout : 'border',
	items : [ orderedForm, orderedGrid ]
});

// --------------dishes taste pop window-----------------
// 前台：[口味编号,口味分类,口味名称,价钱,比例,计算方式]
var dishTasteStoreTas = new Ext.data.Store({
	proxy : new Ext.data.MemoryProxy(dishTasteDataTas),
	reader : new Ext.data.ArrayReader({}, [ {
		name : 'tasteNumber'
	}, {
		name : 'tasteType'
	}, {
		name : 'dishTaste'
	}, {
		name : 'tastePrice'
	}, {
		name : 'tasteRate'
	}, {
		name : 'tasteCountType'
	}, {
		name : 'CountTypeDescr'
	}, {
		name : 'tasteChoose'
	} ])
});

dishTasteStoreTas.reload();

// 3，栏位模型
var checkColumnTas = new Ext.grid.CheckColumn({
	header : ' ',
	dataIndex : 'tasteChoose',
	width : 100
});

var dishTasteColumnModelTas = new Ext.grid.ColumnModel([
		new Ext.grid.RowNumberer(), {
			header : '口味',
			sortable : true,
			dataIndex : 'dishTaste',
			width : 100
		}, {
			header : '价钱',
			sortable : true,
			dataIndex : 'tastePrice',
			width : 100
		}, {
			header : '比例',
			sortable : true,
			dataIndex : 'tasteRate',
			width : 100
		}, {
			header : '计算方式',
			sortable : true,
			dataIndex : 'CountTypeDescr',
			width : 100
		}, checkColumnTas ]);

// 4，表格
var dishTasteGridTas = new Ext.grid.EditorGridPanel({
	title : '口味',
	anchor : '99%',
	ds : dishTasteStoreTas,
	cm : dishTasteColumnModelTas,
	plugins : checkColumnTas,
	clicksToEdit : 1,
	sm : new Ext.grid.RowSelectionModel({
		singleSelect : true
	}),
	listeners : {
		'afteredit' : function(e) {
			var choTasteN = e.record.get('tasteNumber');
			var choTasteDescr = e.record.get('dishTaste');
			if (e.record.get('tasteChoose')) {

				// 校验是否超数
				tasteChoosenCount = tasteChoosenCount + 1;
				if (tasteChoosenCount > 3) {
					Ext.MessageBox.show({
						msg : '暂不允许选择超过３种口味',
						width : 300,
						buttons : Ext.MessageBox.OK
					});

					dishTasteGridTas.getStore().each(function(record) {
						if (record.get('tasteNumber') == choTasteN) {
							record.set('tasteChoose', false);
						}
					});
					tasteChoosenCount = tasteChoosenCount - 1;
				} else {
					// 记录选择的口味
					choosenTasteDisplay.push([ choTasteN, choTasteDescr ]);
				}

			} else {
				var thisIndex = -1;
				for ( var i = 0; i < choosenTasteDisplay.length - 1; i++) {
					if (choosenTasteDisplay[i][0] == choTasteN) {
						thisIndex = i;
					}
				}
				choosenTasteDisplay.splice(thisIndex, 1);
				tasteChoosenCount = tasteChoosenCount - 1;
			}

			choosenTasteRefresh();
		}
	}
});

// /////////////////////////////////

// 2，表格的数据store
// 前台：[口味编号,口味分类,口味名称,价钱,比例,计算方式]
var dishTasteStorePar = new Ext.data.Store({
	proxy : new Ext.data.MemoryProxy(dishTasteDataPar),
	reader : new Ext.data.ArrayReader({}, [ {
		name : 'tasteNumber'
	}, {
		name : 'tasteType'
	}, {
		name : 'dishTaste'
	}, {
		name : 'tastePrice'
	}, {
		name : 'tasteRate'
	}, {
		name : 'tasteCountType'
	}, {
		name : 'CountTypeDescr'
	}, {
		name : 'tasteChoose'
	} ])
});

dishTasteStorePar.reload();

// 3，栏位模型
var checkColumnPar = new Ext.grid.CheckColumn({
	header : ' ',
	dataIndex : 'tasteChoose',
	width : 100
});

var dishTasteColumnModelPar = new Ext.grid.ColumnModel([
		new Ext.grid.RowNumberer(), {
			header : '口味',
			sortable : true,
			dataIndex : 'dishTaste',
			width : 100
		}, {
			header : '价钱',
			sortable : true,
			dataIndex : 'tastePrice',
			width : 100
		}, {
			header : '比例',
			sortable : true,
			dataIndex : 'tasteRate',
			width : 100
		}, {
			header : '计算方式',
			sortable : true,
			dataIndex : 'CountTypeDescr',
			width : 100
		}, checkColumnPar ]);

// 4，表格
var dishTasteGridPar = new Ext.grid.EditorGridPanel({
	title : '做法',
	anchor : '99%',
	ds : dishTasteStorePar,
	cm : dishTasteColumnModelPar,
	plugins : checkColumnPar,
	clicksToEdit : 1,
	sm : new Ext.grid.RowSelectionModel({
		singleSelect : true
	}),
	listeners : {
		'afteredit' : function(e) {
			var choTasteN = e.record.get('tasteNumber');
			var choTasteDescr = e.record.get('dishTaste');
			if (e.record.get('tasteChoose')) {

				// 校验是否超数
				tasteChoosenCount = tasteChoosenCount + 1;
				if (tasteChoosenCount > 3) {
					Ext.MessageBox.show({
						msg : '暂不允许选择超过３种口味',
						width : 300,
						buttons : Ext.MessageBox.OK
					});

					dishTasteGridPar.getStore().each(function(record) {
						if (record.get('tasteNumber') == choTasteN) {
							record.set('tasteChoose', false);
						}
					});
					tasteChoosenCount = tasteChoosenCount - 1;
				} else {
					// 记录选择的口味
					choosenTasteDisplay.push([ choTasteN, choTasteDescr ]);
				}

			} else {
				var thisIndex = -1;
				for ( var i = 0; i < choosenTasteDisplay.length - 1; i++) {
					if (choosenTasteDisplay[i][0] == choTasteN) {
						thisIndex = i;
					}
				}
				choosenTasteDisplay.splice(thisIndex, 1);
				tasteChoosenCount = tasteChoosenCount - 1;
			}

			choosenTasteRefresh();
		}
	}
});

// /////////////////////////////////

// 2，表格的数据store
// 前台：[口味编号,口味分类,口味名称,价钱,比例,计算方式]
var dishTasteStoreSiz = new Ext.data.Store({
	proxy : new Ext.data.MemoryProxy(dishTasteDataSiz),
	reader : new Ext.data.ArrayReader({}, [ {
		name : 'tasteNumber'
	}, {
		name : 'tasteType'
	}, {
		name : 'dishTaste'
	}, {
		name : 'tastePrice'
	}, {
		name : 'tasteRate'
	}, {
		name : 'tasteCountType'
	}, {
		name : 'CountTypeDescr'
	}, {
		name : 'tasteChoose'
	} ])
});

dishTasteStoreSiz.reload();

// 3，栏位模型
var checkColumnSiz = new Ext.grid.CheckColumn({
	header : ' ',
	dataIndex : 'tasteChoose',
	width : 100
});

var dishTasteColumnModelSiz = new Ext.grid.ColumnModel([
		new Ext.grid.RowNumberer(), {
			header : '口味',
			sortable : true,
			dataIndex : 'dishTaste',
			width : 100
		}, {
			header : '价钱',
			sortable : true,
			dataIndex : 'tastePrice',
			width : 100
		}, {
			header : '比例',
			sortable : true,
			dataIndex : 'tasteRate',
			width : 100
		}, {
			header : '计算方式',
			sortable : true,
			dataIndex : 'CountTypeDescr',
			width : 100
		}, checkColumnSiz ]);

// 4，表格
var dishTasteGridSiz = new Ext.grid.EditorGridPanel({
	title : '规格',
	anchor : '99%',
	ds : dishTasteStoreSiz,
	cm : dishTasteColumnModelSiz,
	plugins : checkColumnSiz,
	clicksToEdit : 1,
	sm : new Ext.grid.RowSelectionModel({
		singleSelect : true
	}),
	listeners : {
		'afteredit' : function(e) {
			var choTasteN = e.record.get('tasteNumber');
			var choTasteDescr = e.record.get('dishTaste');
			if (e.record.get('tasteChoose')) {

				// 校验是否超数
				tasteChoosenCount = tasteChoosenCount + 1;
				if (tasteChoosenCount > 3) {
					Ext.MessageBox.show({
						msg : '暂不允许选择超过３种口味',
						width : 300,
						buttons : Ext.MessageBox.OK
					});

					dishTasteGridSiz.getStore().each(function(record) {
						if (record.get('tasteNumber') == choTasteN) {
							record.set('tasteChoose', false);
						}
					});
					tasteChoosenCount = tasteChoosenCount - 1;
				} else {
					// 记录选择的口味
					choosenTasteDisplay.push([ choTasteN, choTasteDescr ]);
				}

			} else {
				var thisIndex = -1;
				for ( var i = 0; i < choosenTasteDisplay.length - 1; i++) {
					if (choosenTasteDisplay[i][0] == choTasteN) {
						thisIndex = i;
					}
				}
				choosenTasteDisplay.splice(thisIndex, 1);
				tasteChoosenCount = tasteChoosenCount - 1;
			}

			choosenTasteRefresh();
		}
	}
});

// tab面版
var dishTasteTabPanel = new Ext.TabPanel({
	// layout : 'fit',
	region : 'center',
	tabPosition : 'top',
	width : 300,
	height : 280,
	activeTab : 0,
	items : [ dishTasteGridTas, dishTasteGridPar, dishTasteGridSiz ]
});

var dishTasteWindow = new Ext.Window({
	layout : 'border',
	width : 550,
	height : 300,
	closeAction : 'hide',
	// plain: true,
	items : [ dishTasteTabPanel, {
				region : 'south',
				height : 20,
				bodyStyle : 'background-color:#A9D0F5',
				contentEl : 'choosenTaste'
	} ],
	buttons : [
		{
			text : '清空',
			handler : function() {
				dishTasteGridTas.getStore().each(function(record) {
					record.set('tasteChoose', false);
				});
				dishTasteGridPar.getStore().each(function(record) {
					record.set('tasteChoose', false);
				});
				dishTasteGridSiz.getStore().each(function(record) {
					record.set('tasteChoose', false);
				});
				choosenTasteDisplay.length = 0;
				choosenTasteRefresh();
				tasteChoosenCount = 0;
			}
		},
		{
			text : '确定',
			handler : function() {
				// dishTasteWindow.hide();
				// 格式：[{編號,描述,價錢或比例,計算方式}]
				choosenTaset.length = 0;
				var ds = orderedStore.getAt(dishOrderCurrRowIndex_).data;
				
				dishTasteGridTas.getStore().each(function(record) {
					if (record.get('tasteChoose')) {
						if (record.get('tasteCountType') == '0') {
							choosenTaset.push([
								record.get('tasteNumber'),// 編號
								record.get('dishTaste'),// 描述
								record.get('tastePrice'),// 價錢或比例
								record.get('tasteCountType') // 計算方式
							]);
						} else {
							choosenTaset.push([
								record.get('tasteNumber'),// 編號
								record.get('dishTaste'),// 描述
								record.get('tasteRate'),// 價錢或比例
								record.get('tasteCountType') // 計算方式
							]);
						}
					}
				});
				dishTasteGridPar.getStore().each(function(record) {
					if (record.get('tasteChoose')) {
						if (record.get('tasteCountType') == '0') {
							choosenTaset.push([
							    record.get('tasteNumber'),// 編號
							    record.get('dishTaste'),// 描述
							    record.get('tastePrice'),// 價錢或比例
							    record.get('tasteCountType') // 計算方式
							]);
						} else {
							choosenTaset.push([
							    record.get('tasteNumber'),// 編號
							    record.get('dishTaste'),// 描述
							    record.get('tasteRate'),// 價錢或比例
							    record.get('tasteCountType') // 計算方式
							]);
						}
					}
				});
				dishTasteGridSiz.getStore().each(function(record) {
					if (record.get('tasteChoose')) {
						if (record.get('tasteCountType') == '0') {
							choosenTaset.push([
							    record.get('tasteNumber'),// 編號
							    record.get('dishTaste'),// 描述
							    record.get('tastePrice'),// 價錢或比例
							    record.get('tasteCountType') // 計算方式
							]);
						} else {
							choosenTaset.push([
							    record.get('tasteNumber'),// 編號
							    record.get('dishTaste'),// 描述
							    record.get('tasteRate'),// 價錢或比例
							    record.get('tasteCountType') // 計算方式
							]);
						}
					}
				});

				if (tasteChoosenCount == 0) {
					// 格式：[菜名，口味，数量，￥单价，操作，￥实价，菜名编号，厨房编号，口味编号1,特,荐,停,送,￥口味价钱,口味编号2,口味编号3,菜品状态]
					// 未有選擇口味					
					for(var i = 0; i < orderedData.root.length; i++){						
						if(orderedData.root[i].foodID == ds.foodID){
							orderedData.root[i].tasteID = 0;
							orderedData.root[i].tasteIDTwo = 0;
							orderedData.root[i].tasteIDThree = 0;
							orderedData.root[i].tastePref = '无口味';
							orderedData.root[i].acturalPrice = orderedData.root[i].unitOrice;
							break;
						}
					}
					
					// refresh
					orderedStore.loadData(orderedData);
					// 底色处理，已点菜式原色底色
					dishGridRefresh();
					orderIsChanged = true;

					// hide the window
					dishTasteWindow.hide();
				} else {
					// 校驗通過
					// 格式：[菜名，口味，数量，￥单价，操作，￥实价，菜名编号，厨房编号，口味编号1,特,荐,停,送,￥口味价钱,口味编号2,口味编号3,菜品状态]
					// 更新單價,// 格式：[{編號,描述,價錢或比例,計算方式}]
					var origPrice = ds.unitPrice;
					var currPrice = parseFloat(origPrice);
					var tpTastePref = '', tpTasteID = 0, tpTasteIDTwo = 0, tpTasteIDThree = 0;
					for ( var i = 0; i < choosenTaset.length; i++) {
						if (choosenTaset[i][3] == 1)
							currPrice = currPrice * (1 + parseFloat(choosenTaset[i][2]));
					}
					for ( var i = 0; i < choosenTaset.length; i++) {
						if (choosenTaset[i][3] == 0)
							currPrice = currPrice + parseFloat(choosenTaset[i][2]);
					}

					// mark the choosen taset
					// 第一口味
					if (choosenTaset[0] != undefined) {
						tpTasteID = choosenTaset[0][0];
						tpTastePref += (choosenTaset[0][1] + '；');					
					}
					// 第二口味
					if (choosenTaset[1] != undefined) {
						tpTasteIDTwo = choosenTaset[1][0];
						tpTastePref += (choosenTaset[1][1] + '；');					
					}
					// 第三口味
					if (choosenTaset[2] != undefined) {
						tpTasteIDThree = choosenTaset[2][0];
						tpTastePref += (choosenTaset[2][1] + '；');						
					}
					tpTastePref = tpTastePref.substring(0, tpTastePref.length - 1);
					
					// refresh
					for(var i = 0; i < orderedData.root.length; i++){						
						if(orderedData.root[i].foodID == ds.foodID){
							orderedData.root[i].tasteID = tpTasteID;
							orderedData.root[i].tasteIDTwo = tpTasteIDTwo;
							orderedData.root[i].tasteIDThree = tpTasteIDThree;
							orderedData.root[i].tastePref = tpTastePref;
							orderedData.root[i].acturalPrice = currPrice;
							break;
						}
					}
					orderedStore.loadData(orderedData);
						// 底色处理，已点菜式原色底色
					dishGridRefresh();
					orderIsChanged = true;

						// hide the window
					dishTasteWindow.hide();
					}
				}
		}, {
			text : '取消',
			handler : function() {
				dishTasteWindow.hide();
			}
		} ],
			listeners : {
				'show' : function(thiz) {
					var ds = orderedStore.getAt(dishOrderCurrRowIndex_).data;
					// show的時候從一點菜式數組中取出當前菜品的口味情況
					var tasteNbr1 = ds.tasteID;
					var tasteNbr2 = ds.tasteIDTwo;
					var tasteNbr3 = ds.tasteIDThree;

//					 alert('tasteNbr1: '+tasteNbr1+' tasteNbr2: '+tasteNbr2+'tasteNbr3: '+tasteNbr3);

					// 清空选择
					dishTasteGridTas.getStore().each(function(record) {
						record.set('tasteChoose', false);
					});
					dishTasteGridPar.getStore().each(function(record) {
						record.set('tasteChoose', false);
					});
					dishTasteGridSiz.getStore().each(function(record) {
						record.set('tasteChoose', false);
					});

					choosenTasteDisplay.length = 0;
					tasteChoosenCount = 0;

					if (tasteNbr1 == 0 && tasteNbr2 == 0 && tasteNbr3 == 0) {

						choosenTasteRefresh();

					} else {
						dishTasteGridTas.getStore().each(function(record) {
							if (record.get('tasteNumber') == tasteNbr1
													|| record.get('tasteNumber') == tasteNbr2
													|| record.get('tasteNumber') == tasteNbr3) {
												record.set('tasteChoose', true);
												choosenTasteDisplay.push([
																record.get('tasteNumber'),
																record.get('dishTaste') ]);
												tasteChoosenCount = tasteChoosenCount + 1;
											}
										});
						dishTasteGridPar.getStore().each(
										function(record) {
											if (record.get('tasteNumber') == tasteNbr1
													|| record.get('tasteNumber') == tasteNbr2
													|| record.get('tasteNumber') == tasteNbr3) {
												record.set('tasteChoose', true);
												choosenTasteDisplay.push([
																record.get('tasteNumber'),
																record.get('dishTaste') ]);
												tasteChoosenCount = tasteChoosenCount + 1;
											}
										});
						dishTasteGridSiz
								.getStore()
								.each(
										function(record) {
											if (record.get('tasteNumber') == tasteNbr1
													|| record
															.get('tasteNumber') == tasteNbr2
													|| record
															.get('tasteNumber') == tasteNbr3) {
												record.set('tasteChoose', true);
												choosenTasteDisplay.push([
																record.get('tasteNumber'),
																record.get('dishTaste') ])
												tasteChoosenCount = tasteChoosenCount + 1;
											}
										});

						choosenTasteRefresh();
					}
				}
			}
		});

// --------------dishes order east panel-----------------
// soft key board
var softKBKeyHandler = function(relateItemId, number) {

	var currValue = dishesOrderEastPanel.findById(relateItemId).getValue();
	dishesOrderEastPanel.findById(relateItemId).setValue(
			currValue + ('' + number));

	dishesOrderEastPanel.findById(relateItemId).fireEvent('blur', dishesOrderEastPanel.findById(relateItemId));
};

softKeyBoardDO = new Ext.Window({
	layout : 'fit',
	width : 117,
	height : 142,
	resizable : false,
	closeAction : 'hide',
	// x : 41,
	// y : 146,
	items : [ {
		layout : 'form',
		labelSeparator : '：',
		labelWidth : 40,
		frame : true,
		buttonAlign : 'left',
		items : [ {
			layout : 'column',
			border : false,
			items : [
					{
						layout : 'form',
						width : 30,
						border : false,
						items : [ {
							text : '1',
							xtype : 'button',
							handler : function() {
								softKBKeyHandler(softKBRelateItemId, '1');
								dishKeyboardSelect(softKBRelateItemId);
							}
						} ]
					},
					{
						layout : 'form',
						width : 30,
						border : false,
						items : [ {
							text : '2',
							xtype : 'button',
							handler : function() {
								softKBKeyHandler(softKBRelateItemId, '2');
								dishKeyboardSelect(softKBRelateItemId);
							}
						} ]
					},
					{
						layout : 'form',
						width : 30,
						border : false,
						items : [ {
							text : '3',
							xtype : 'button',
							handler : function() {
								softKBKeyHandler(softKBRelateItemId, '3');
								dishKeyboardSelect(softKBRelateItemId);
							}
						} ]
					},
					{
						layout : 'form',
						width : 30,
						border : false,
						items : [ {
							text : '4',
							xtype : 'button',
							handler : function() {
								softKBKeyHandler(softKBRelateItemId, '4');
								dishKeyboardSelect(softKBRelateItemId);
							}
						} ]
					},
					{
						layout : 'form',
						width : 30,
						border : false,
						items : [ {
							text : '5',
							xtype : 'button',
							handler : function() {
								softKBKeyHandler(softKBRelateItemId, '5');
								dishKeyboardSelect(softKBRelateItemId);
							}
						} ]
					},
					{
						layout : 'form',
						width : 30,
						border : false,
						items : [ {
							text : '6',
							xtype : 'button',
							handler : function() {
								softKBKeyHandler(softKBRelateItemId, '6');
								dishKeyboardSelect(softKBRelateItemId);
							}
						} ]
					},
					{
						layout : 'form',
						width : 30,
						border : false,
						items : [ {
							text : '7',
							xtype : 'button',
							handler : function() {
								softKBKeyHandler(softKBRelateItemId, '7');
								dishKeyboardSelect(softKBRelateItemId);
							}
						} ]
					},
					{
						layout : 'form',
						width : 30,
						border : false,
						items : [ {
							text : '8',
							xtype : 'button',
							handler : function() {
								softKBKeyHandler(softKBRelateItemId, '8');
								dishKeyboardSelect(softKBRelateItemId);
							}
						} ]
					},
					{
						layout : 'form',
						width : 30,
						border : false,
						items : [ {
							text : '9',
							xtype : 'button',
							handler : function() {
								softKBKeyHandler(softKBRelateItemId, '9');
								dishKeyboardSelect(softKBRelateItemId);
							}
						} ]
					},
					{
						layout : 'form',
						width : 30,
						border : false,
						items : [ {
							text : '&nbsp;.',
							xtype : 'button',
							handler : function() {
								softKBKeyHandler(softKBRelateItemId, '.');
								dishKeyboardSelect(softKBRelateItemId);
							}
						} ]
					},
					{
						layout : 'form',
						width : 60,
						border : false,
						items : [ {
							text : '&nbsp;删 除&nbsp;',
							xtype : 'button',
							handler : function() {
								var origValue = dishesOrderEastPanel.findById(softKBRelateItemId).getValue() + '';
								var newValue = origValue.substring(0, origValue.length - 1);
								dishesOrderEastPanel.findById(softKBRelateItemId).setValue(newValue);
								dishKeyboardSelect(softKBRelateItemId);
								dishesOrderEastPanel.findById(softKBRelateItemId).fireEvent('blur',dishesOrderEastPanel.findById(softKBRelateItemId));
							}

						} ]
					},
					{
						layout : 'form',
						width : 30,
						border : false,
						items : [ {
							text : '0',
							xtype : 'button',
							handler : function() {
								softKBKeyHandler(softKBRelateItemId, '0');
								dishKeyboardSelect(softKBRelateItemId);
							}
						} ]
					},
					{
						layout : 'form',
						width : 60,
						border : false,
						items : [ {
							text : '&nbsp;清 空&nbsp;',
							xtype : 'button',
							handler : function() {
								dishesOrderEastPanel.findById(
										softKBRelateItemId).setValue('');
								dishKeyboardSelect(softKBRelateItemId);
								dishesOrderEastPanel.findById(
										softKBRelateItemId).fireEvent(
										'blur',
										dishesOrderEastPanel.findById(softKBRelateItemId));
							}

						} ]
					} ]
		} ]
	} ],
	listeners : {
		show : function(thiz) {
			var f = Ext.get(softKBRelateItemId);
			f.focus.defer(100, f); // 万恶的EXT！为什么这样才可以！？！？
		}
	// ,
	// beforehide : function(thiz) {
	// if ((softKBRelateItemId == 'orderCountSpell' && (Ext.getCmp(
	// 'orderCountSpell').getValue() == 0 || Ext.getCmp(
	// 'orderCountSpell').getValue() == ''))) {
	// return false;
	// } else {
	// return true;
	// }
	// }
	}
});

// ------------------------------------- 普通菜谱
// -----------------------------------
// 2，表格的数据store
var dishesDisplayStore = new Ext.data.Store({
	proxy : new Ext.data.MemoryProxy(dishesDisplayDataShow),
	reader : new Ext.data.ArrayReader({}, [ {
		name : 'dish'
	}, {
		name : 'dishIndex'
	}, {
		name : 'dishSpell'
	}, {
		name : 'dishPrice'
	} ])
});

// 3，栏位模型
var dishesDisplayColumnModel = new Ext.grid.ColumnModel([
		new Ext.grid.RowNumberer(), {
			header : '菜名',
			sortable : true,
			dataIndex : 'dish',
			width : 130
		}, {
			header : '菜名编号',
			sortable : true,
			dataIndex : 'dishIndex',
			width : 80
		}, {
			header : '菜名拼音',
			sortable : true,
			dataIndex : 'dishSpell',
			width : 80
		}, {
			header : '单价',
			sortable : true,
			dataIndex : 'dishPrice',
			width : 80
		} ]);

// 4，表格
var dishesDisplayGrid = new Ext.grid.GridPanel({
	xtype : 'grid',
	// height : 400,
	id : 'dishesDisplayGrid',
	anchor : '98%',
	autoScroll : true,
	// region : 'center',
	ds : dishesDisplayStore,
	cm : dishesDisplayColumnModel,
	viewConfig : {
		forceFit : true
	},
	sm : new Ext.grid.RowSelectionModel({
		singleSelect : true
	}),
	listeners : {
		rowdblclick : function(thiz, rowIndex, e) {
			if (dishesDisplayDataShow[rowIndex][7] == 'false') {
						
						// mune格式：[菜名，菜名编号，菜名拼音，单价，厨房编号,特,荐,停,送,菜品状态]
						// ordered格式：[菜名，口味，数量，￥单价，操作，￥实价，菜名编号，厨房编号，口味编号1,特,荐,停,送,￥口味价钱,口味编号2,口味编号3]
						// var dishCurrCount = dishesOrderEastPanel.findById(
						// 'orderCountNum').getValue();
				orderedStore.removeAll();
				
				var dishCurrName = dishesDisplayDataShow[rowIndex][0];
				var dishCurrPrice = dishesDisplayDataShow[rowIndex][3];
				var dishNbr = dishesDisplayDataShow[rowIndex][1];
				var kitchenNbr = dishesDisplayDataShow[rowIndex][4];

				var isAlreadyOrderd = false;
				
				if(typeof(orderedData) != 'undefined' && typeof(orderedData.root) != 'undefined' ){
					for ( var i = 0; i < orderedData.root.length; i++) {
						if (orderedData.root[i].foodID == dishNbr && orderedData.root[i].status == 2) {
							orderedData.root[i].count += 1;
							isAlreadyOrderd = true;
							break;
						}
					}
				}
				if (isAlreadyOrderd == false) {
					orderedData.root.push({
						aliasID : dishNbr,
						foodName : dishCurrName,
						tastePref : '无口味',
						count : 1,
						unitPrice : dishCurrPrice.substring(1),
						acturalPrice : dishCurrPrice.substring(1),
						orderDateFormat : new Date().format('Y-m-d  H:i:s'),
						waiter : Ext.getDom('optName').innerHTML,
						foodID : dishNbr,
						kitchenId : kitchenNbr,
						tasteID : 0,
						special : dishesDisplayDataShow[rowIndex][5],
						recommed : dishesDisplayDataShow[rowIndex][6],
						soldout : dishesDisplayDataShow[rowIndex][7],
						gift : dishesDisplayDataShow[rowIndex][8],
						tastePrice : 0,
						tasteIDTwo : 0,
						tasteIDThree : 0,
						status : 2,
						currPrice : dishesDisplayDataShow[rowIndex][9],
						temporary : false,
						tmpTaste : false,						
						tmpTastePref : '',
						tmpTastePrice : 0,
						tmpTasteAlias : 0,
						hangStatus : 0
					});
				}
				
				orderedStore.loadData(orderedData);				
				// 底色处理，已点菜式原色底色
				dishGridRefresh();
				orderIsChanged = true;
				dishOrderCurrRowIndex_ = -1;
						
				// show all the dishes
				dishesDisplayDataShow.length = 0;
				for ( var i = 0; i < dishesDisplayData.length; i++) {
					dishesDisplayDataShow.push([
						dishesDisplayData[i][0],
						dishesDisplayData[i][1],
						dishesDisplayData[i][2],
						dishesDisplayData[i][3],
						dishesDisplayData[i][4],
						dishesDisplayData[i][5],
						dishesDisplayData[i][6],
						dishesDisplayData[i][7],
						dishesDisplayData[i][8],
						dishesDisplayData[i][9] ]);
				}
				dishesDisplayStore.reload();
						
				// clear the number or spell input
				dishesOrderEastPanel.findById('orderSpell').setValue('');
				dishesOrderEastPanel.findById('orderNbr').setValue('');
						
				var f;
				if (dishesDisplayTabPanel.getActiveTab().getId() == 'dishesChooseByNumForm') {
					f = Ext.get('orderNbr');
					f.focus.defer(100, f);
				} else {
//					f = Ext.get('orderSpell');						
//					f.focus.defer(100, f);
				}

			} else {
				Ext.MessageBox.show({
					msg : '该菜品已售完！',
					width : 300,
					buttons : Ext.MessageBox.OK
				});
			}
		},
		render : function(thiz) {
			orderedMenuOnLoad();
			tasteOnLoad();
		}
	}
});

dishesDisplayStore.reload();

// -----------------------------------------------------------------------------------
var dishesChooseBySpellForm = new Ext.form.FormPanel({
	title : '菜名拼音选菜',
	id : 'dishesChooseBySpellForm',
	border : false,
	frame : true,
	items : [ {
		layout : 'column',
		border : false,
		anchor : '98%',
		items : [ {
			layout : 'form',
			labelWidth : 60,
			border : false,
			labelSeparator : '：',
			columnWidth : .50,
			items : [ {
				xtype : 'textfield',
				fieldLabel : '菜名拼音',
				name : 'orderSpell',
				id : 'orderSpell',
				anchor : '90%',
				listeners : {
					focus : function(thiz) {
						if(softKeyBoardDO.renderer && softKeyBoardDO.show){
							softKeyBoardDO.hide();
						}						
					},
					render : function(thiz) {
						dishSpellOnLoad();
					}
				}
			} ]
		} ]
	} ]
});

var dishesChooseByNumForm = new Ext.form.FormPanel({
	title : '菜名编号选菜',
	id : 'dishesChooseByNumForm',
	border : false,
	frame : true,
	items : [ {
		layout : 'column',
		border : false,
		anchor : '98%',
		items : [

				{
					layout : 'form',
					labelWidth : 60,
					border : false,
					labelSeparator : '：',
					columnWidth : .50,
					items : [ {
						xtype : 'numberfield',
						fieldLabel : '菜名编号',
						name : 'orderNbr',
						id : 'orderNbr',
						anchor : '90%',
						listeners : {
							focus : function(thiz) {
								// softKeyBoardDO
								// .setPosition(dishesOrderCenterPanel
								// .getInnerWidth() + 77, 187);
								// softKBRelateItemId = 'orderNbr';
								// softKeyBoardDO.show();

							},
							render : function(thiz) {
								dishNbrOnLoad();
							}
						}
					} ]
				},
				{
					layout : 'form',
					border : false,
					columnWidth : .50,
					items : [ {
						xtype : 'button',
						text : '小键盘',
						name : 'softKeyBoardNbrBtn',
						id : 'softKeyBoardNbrBtn',
						listeners : {
							'click' : function(thiz, e) {
								softKeyBoardDO.setPosition(dishesOrderCenterPanel.getInnerWidth() + 77, 187);
								softKBRelateItemId = 'orderNbr';
								softKeyBoardDO.show();
							}
						}
					} ]
				} ]
	} ]
});

var dishesChooseByKitchenForm = new Ext.form.FormPanel(
		{
			title : '分厨选菜',
			id : 'dishesChooseByKitchenForm',
			border : false,
			frame : true,
			items : [ {
				contentEl : 'kitchenSelectDO'
					//,
				//autoScroll : true
			} ],
			autoScroll : true,
			listeners : {
				render : function(thiz) {
					document.getElementById('kitchenSelectDO').style['visibility'] = 'visible';
					// bind the kitchen select image click function
					kitchenSelectLoad();
				}
			}
		});

var emptyPanel = new Ext.Panel({
	title : '临时菜点菜',
	id : 'emptyPanel',
	items : [ {} ]
});

var dishesDisplayTabPanel = new Ext.TabPanel({
	activeTab : 0,
	// height : 65,
	height : 135,
	region : 'north',
	border : false,
	items : [ dishesChooseByNumForm, dishesChooseBySpellForm,
			dishesChooseByKitchenForm, emptyPanel ],
	listeners : {
		// for FF only!!! FF when clicking the tab, the focus of the number
		// field
		// does not lost!!!
		tabchange : function(thiz, panel) {
			// dishesOrderEastPanel.findById('orderCountNum').fireEvent('blur',
			// dishesOrderEastPanel.findById('orderCountNum'));
			// dishesOrderEastPanel.findById('orderCountSpell').fireEvent('blur',
			// dishesOrderEastPanel.findById('orderCountSpell'));

			// hide the soft keyboard
			if (softKeyBoardDO.isVisible()) {
				softKeyBoardDO.hide();
			}

			// change the height of the form panel
			if (panel.getId() == 'dishesChooseByKitchenForm') {
				dishesDisplayTabPanel.setHeight(135);
				dishesOrderEastPanel.doLayout();
				Ext.getCmp('menuCard').layout
						.setActiveItem('dishesDisplayGrid');
				ketchenDeselect(ketchenSelectIndex);
			} else if (panel.getId() == 'emptyPanel') {
				Ext.getCmp('menuCard').layout.setActiveItem('tempDishPanel');
				dishesDisplayTabPanel.setHeight(0);
				dishesOrderEastPanel.doLayout();
			} else {
				dishesDisplayTabPanel.setHeight(65);
				dishesOrderEastPanel.doLayout();
				Ext.getCmp('menuCard').layout
						.setActiveItem('dishesDisplayGrid');
			}

			// show all the dishes
			dishesDisplayDataShow.length = 0;
			for ( var i = 0; i < dishesDisplayData.length; i++) {
				dishesDisplayDataShow.push([ dishesDisplayData[i][0],
						dishesDisplayData[i][1], dishesDisplayData[i][2],
						dishesDisplayData[i][3], dishesDisplayData[i][4],
						dishesDisplayData[i][5], dishesDisplayData[i][6],
						dishesDisplayData[i][7], dishesDisplayData[i][8],
						dishesDisplayData[i][9] ]);
			}
			dishesDisplayStore.reload();

			// clear the number or spell input
			dishesOrderEastPanel.findById('orderSpell').setValue('');
			dishesOrderEastPanel.findById('orderNbr').setValue('');
		}
	}
});

// var dishesOrderEastPanel = new Ext.Panel({
// region : 'east',
// collapsible : true,
// width : 432,
// minSize : 432,
// maxSize : 432,
// split : true,
// id : 'dishesOrderEastPanel',
// layout : 'border',
// items : [ dishesDisplayTabPanel, menuCard ]
// });

// --------------dishes order north panel-----------------
var dishesOrderNorthPanel = new Ext.Panel({
	id : 'dishesOrderNorthPanel',
	region : 'north',
	title : '<div style="font-size:18px;padding-left:2px">新下单<div>',
	height : 75,
	border : false,
	layout : 'form',
	frame : true,
	// contentEl : 'tableStatusDO',
	items : [ {
		layout : 'column',
		border : false,
		anchor : '100%',
		items : [ {
			layout : 'form',
			border : false,
			id : 'tableNbrFrom',
			width : 100,
			contentEl : 'tableStatusTableNbr'
		}, {
			layout : 'form',
			border : false,
			width : 50,
			contentEl : 'tableStatusPerCount'
		}, {
			layout : 'form',
			border : false,
			width : 50,
			items : [ {
				xtype : 'numberfield',
				id : 'tablePersonCount',
				width : 35,
				hideLabel : true,
				maxValue : 99,
				minValue : 1,
				value : 1,
				validator : function(v) {
					if (v >= 0 && v <= 99) {
						return true;
					} else {
						return '人数输入范围是０～９９';
					}
				}
			} ]
		}, {
			layout : 'form',
			border : false,
			width : 300,
			contentEl : 'tableStatusDynamic'
		} ]
	} ],
	listeners : {
		render : function(thiz) {
			// tableStuLoad();
		}
	}
});

var menuCard;
var dishesOrderEastPanel;
var centerPanelDO;
var tempDishGrid;
Ext.onReady(function() {
	// 解决ext中文传入后台变问号问题
	Ext.lib.Ajax.defaultPostHeader += '; charset=utf-8';
	Ext.QuickTips.init();

	// *************整体布局*************
	// ------------------------------- 臨時菜點菜
	// -----------------------------
	tempDishGrid = new Ext.grid.PropertyGrid({
		// title : 'Property Grid',
		id : 'tempDishGrid',
		region : 'center',
		nameText : 'new query',
		hideHeaders : true,
		source : {
			'临时菜名称' : '',
			'单价' : 0,
			'数量' : 1
		},
		customEditors : {
			'临时菜名称' : new Ext.grid.GridEditor(new Ext.form.TextField({
				selectOnFocus : true,
					// allowBlank : false
			})),
			'单价' : new Ext.grid.GridEditor(new Ext.form.NumberField({
				selectOnFocus : true,
				allowBlank : false,
				allowNegative : false,
				maxValue : 9999.99,
				minValue : 0.01,
				validator : function(v) {
					if (v >= 0 && v <= 99999.99) {
						return true;
					} else {
						return '价格范围是0.00至99999.99！';
					}
				}
			})),
			'数量' : new Ext.grid.GridEditor(new Ext.form.NumberField({
				selectOnFocus : true,
				allowBlank : false,
				allowNegative : false,
				maxValue : 99.99,
				minValue : 0.01,
				validator : function(v) {
					if (v >= 0.01 && v <= 99.99) {
						return true;
					} else {
						return '数量范围是0.01至99.99！';
					}
				}
			}))
		}
	});

	var tempDishCtl = new Ext.form.FormPanel({
		id : 'tempDishCtl',
		region : 'south',
		height : 1,
		frame : true,
		items : [{}],
		buttons : [
		    {
				text : '添加',
				handler : function() {
					var name = tempDishGrid.getSource().临时菜名称;
					var price = tempDishGrid.getSource().单价;
					var count = tempDishGrid.getSource().数量;

					if (name == '') {
						Ext.MessageBox.show({
							msg : '临时菜名称不能为空！',
							width : 300,
							buttons : Ext.MessageBox.OK
						});
					} else {
						orderedData.root.push({
							foodName : (name + '<img src="../../images/tempDish.png" />'),
							tastePref : '无口味',
							count : count,
							unitPrice : price,
							acturalPrice : price,
							orderDateFormat : (new Date().format('Y-m-d H:m:s')),
							waiter : Ext.getDom('optName').innerHTML,
							foodID : -1,
							kitchenID : 0,
							kitchenId : 0,
							special : false,
							recommed : false,
							soldout : false,
							gift : false,
							tastePrice : 0,
							tasteIDTwo : 0,
							tasteIDThree : 0,
							status : 2,
							currPrice : false,
							temporary : true,
							tmpFoodName : name,
							tmpTasteAlias : 0,
							hangStatus : 0
						});					
						
						orderedStore.loadData(orderedData);
						// 底色处理，已点菜式原色底色
						dishGridRefresh();
						orderIsChanged = true;
						dishOrderCurrRowIndex_ = -1;

						tempDishGrid.setSource({
							'临时菜名称' : '',
							'单价' : 0,
							'数量' : 1
						});

					}
				}
			} ]
		});

	var tempDishPanel = new Ext.Panel({
		layout : 'border',
		id : 'tempDishPanel',
		items : [ tempDishGrid, tempDishCtl ]
	});

	// ------------------------------- 菜譜card
	menuCard = new Ext.Panel({
		id : 'menuCard',
		layout : 'card',
		region : 'center',
		activeItem : 0,
		items : [ dishesDisplayGrid, tempDishPanel ]
	});

	dishesOrderEastPanel = new Ext.Panel({
		region : 'east',
		collapsible : true,
		width : 432,
		minSize : 432,
		maxSize : 432,
		split : true,
		id : 'dishesOrderEastPanel',
		layout : 'border',
		items : [ dishesDisplayTabPanel, menuCard ]
	});

	centerPanelDO = new Ext.Panel({
		id : 'centerPanelDO',
		region : 'center',
		border : false,
		margins : '0 0 0 0',
		layout : 'border',
		items : [ dishesOrderCenterPanel, dishesOrderEastPanel, dishesOrderNorthPanel ]
	});

	new Ext.Viewport({
		layout : 'border',
		id : 'viewport',
		items : [
		    {
				region : 'north',
				bodyStyle : 'background-color:#DFE8F6;',
				html : '<h4 style="padding:10px;font-size:150%;float:left;">无线点餐网页终端</h4><div id="optName" class="optName"></div>',
				height : 50,
				border : false,
				margins : '0 0 0 0'
			},
			centerPanelDO,
			{
				region : 'south',
				height : 30,
				layout : 'form',
				frame : true,
				border : false,
				html : '<div style="font-size:11pt; text-align:center;""><b>版权所有(c) 2011 智易科技</b></div>'
			} ]
	});
	
});


