//声明：
var regionGrid;
var regionTree;
// 修改弹出框；
var updateRegionWin;
var tablePanel;
var tableUpdateWin;
var tableAddWin;

//过滤条件；
var filterTypeData = [ [ 0, '全部' ], [ 1, '餐台编号' ], [ 2, '餐台名称' ], [ 3, '最低消费' ], [ 4, '餐台状态' ], [ 5, '餐台类型' ] ];
//餐台类型过滤；
var typeAddData = [ [ 1, '一般' ], [ 2, '外卖' ], [ 3, '并台' ] ];
var typeAddStore = new Ext.data.SimpleStore({
	fields : [ 'value', 'text' ],
	data : typeAddData
});
//餐台状态过滤；
stateAddData = [ [ 0, '空闲'],[ 1, '就餐'],[ 2, '预定'] ];
var stateAddStore = new Ext.data.SimpleStore({
	fields : ['value','text'],
	data : stateAddData
});
//餐台编号、消费范围过滤
var operatorData = [ [ 1, '等于' ], [ 2, '大于等于' ], [ 3, '小于等于' ] ];

var tmp = 0;
//////////////////////搜索框中的数据（条件）---end///////////////////////////


var filterTypeCombox = new Ext.form.ComboBox({
	fieldLabel : '过滤: ',
	forceSelection : true,
	width : 100,
	value : '全部',
	id : 'filter',
	store : new Ext.data.SimpleStore({
		fields : ['value','text'],
		data : filterTypeData
	}),
	valueField : 'value',
	displayField : 'text',
	typeAhead : true,
	mode : 'local',
	triggerAction : 'all',
	selectOnFocus : true,
	allowBlank : false,
	listeners : {
		select : function(combo,record,index){
			var operatorFilterId = Ext.getCmp('operatorFilterId');
			var tableNameFilterId = Ext.getCmp('tableNameFilterId');
			var tableNumberAaialsFilterId = Ext.getCmp('tableNumberAaialsFilterId');
			var tableStateFilterId = Ext.getCmp('tableStateFilterId');
			var tableTypeFilterId = Ext.getCmp('tableTypeFilterId');
			
			tmp = index;//用来存储下拉框中的值（等于、大于等于、小于等于）
			if(index == 0){
				operatorFilterId.setVisible(false);
				tableNameFilterId.setVisible(false);
				tableNumberAaialsFilterId.setVisible(false);
				tableStateFilterId.setVisible(false);
				tableTypeFilterId.setVisible(false);
				conditionType = '';
				
			}else if(index == 1 || index == 3){
				operatorFilterId.setVisible(true);
				tableNumberAaialsFilterId.setVisible(true);
				
				tableNameFilterId.setVisible(false);
				tableStateFilterId.setVisible(false);
				tableTypeFilterId.setVisible(false);
				
				operatorFilterId.setValue(1);  //设置值为： ”等于“
				tableNumberAaialsFilterId.setValue();
				conditionType = operatorFilterId.getId()+','+tableNumberAaialsFilterId.getId();
			
			}else if(index == 2){
				tableNameFilterId.setVisible(true);
				tableNameFilterId.setValue();
				conditionType = tableNameFilterId.getId();
				
				operatorFilterId.setVisible(false);
				tableNumberAaialsFilterId.setVisible(false);
				tableStateFilterId.setVisible(false);
				tableTypeFilterId.setVisible(false);
			
			}else if(index == 4){
				tableStateFilterId.setVisible(true);
				tableStateFilterId.store.loadData(stateAddData);
				tableStateFilterId.setValue(stateAddData[0][0]);  //设置值为： ”空闲“
				conditionType = tableStateFilterId.getId();
				
				tableTypeFilterId.setVisible(false);
				tableNameFilterId.setVisible(false);
				operatorFilterId.setVisible(false);
				tableNumberAaialsFilterId.setVisible(false);
			
			}else if(index == 5){
				tableTypeFilterId.setVisible(true);
				tableTypeFilterId.store.loadData(typeAddData);
				tableTypeFilterId.setValue(typeAddData[0][0]);  //设置值为： ”一般“
				conditionType = tableTypeFilterId.getId();
				
				tableStateFilterId.setVisible(false);
				tableNameFilterId.setVisible(false);
				operatorFilterId.setVisible(false);
				tableNumberAaialsFilterId.setVisible(false);
			}
		}
	}
});


var pushBackBut = new Ext.ux.ImageButton({
	imgPath : "../../images/UserLogout.png",
	imgWidth : 50,
	imgHeight : 50,
	tooltip : "返回",
	handler : function(btn) {
		location.href = "BasicMgrProtal.html?restaurantID="
			+ restaurantID + "&pin=" + pin;
	}
});

var logOutBut = new Ext.ux.ImageButton({
	imgPath : "../../images/ResLogout.png",
	imgWidth : 50,
	imgHeight : 50,
	tooltip : "登出",
	handler : function(btn) {}
});


//弹出添加餐桌窗口；
tableAddWin = new Ext.Window({
	layout : 'fit',
	title : '添加餐桌',
	width : 260,
	height : 206,
	closeAction : 'hide',
	resizable : false,
	modal : true,
	items : [{
		layout : 'form',
		id : 'tableAddForm',
		labelWidth : 60,
		border : false,
		frame : true,
		items : [{
			xtype  : 'numberfield',
			fieldLabel : '餐台编号',
			id : 'tableAddNumber',
			width : 160,
			validator : function(v){
				if(v < 32767 || v > 0){
					return '餐台编号范围是 1 至32767 ！';
				}else{
					return true;
				}
			}
		},{
			width : 160,
			xtype : 'textfield',
			fieldLabel : '餐桌名称',
			id : 'tableAddName'
		},{
			xtype : 'combo',
			id : 'tableAddAilas',
			fieldLabel : '餐台区域',
			width : 160,
			store : new Ext.data.JsonStore({
				fields : [ 'regionID', 'regionName'],
				root : 'root'
			}),
			valueField : 'regionID',//显示值；
			displayField : 'regionName',//显示文本；
			mode : 'local',//数据加载模式；
			triggerAction : 'all',//显示所有下列数据；
			typeAhead : true,
			selectOnFocus : true,
			forceSelection : true,
			allowBlank : false,//是否允许空值，默认为true，这里设置不能为空；
			blankText : '请选择正确的区域！！'
		},{
			width : 160,
			xtype : 'numberfield',
			fieldLabel : '最低消费',
			id : 'tableAddMincost'
		},{
			width : 160,
			xtype : 'numberfield',
			fieldLabel : '服务费率',
			id : 'tableAddSerRate',
			validator : function(v){
				if(v < 0 || v > 1){
					return '服务费率范围是0%至100%！！！';
				}else{
					return true;
				}
			}
		}]
	}],
	buttons : [{
		text : '确定',
		id : 'btSureAddTable',
		handler : function(){
			
			var tableAddNumber = tableAddWin.findById('tableAddNumber').getValue();
			var tableAddName = tableAddWin.findById('tableAddName').getValue();
			var tableAddAilas = tableAddWin.findById('tableAddAilas').getValue();
			var tableAddMincost = tableAddWin.findById('tableAddMincost').getValue();
			
			if(tableAddMincost == ''){
				tableAddMincost = 0;
			}
			
			var tableAddSerRate = tableAddWin.findById('tableAddSerRate').getValue();
			
			if (tableAddSerRate == '') {
				tableAddSerRate = 0;
			}
			
			var isDuplicate = false ;
			
			for ( var i = 0; i < regionTreeData.length; i++) {
				if (tableAddNumber == regionTreeData[i].tableAddAilas) {
					isDuplicate = true;
				}
			}
			
			if(!isDuplicate){
				
				tableAddWin.hide();
				isPrompt = false;
				
				Ext.Ajax.request({
					url : '../../InsertTable.do',
					params : {
						'restaurantID' : restaurantID,
						'tableAddNumber' : tableAddNumber,
						'tableAddName' : tableAddName,
						'tableAddAilas' : tableAddAilas,
						'tableAddMincost' : tableAddMincost,
						'tableAddSerRate' : tableAddSerRate
					},
					success : function(response , option){
						var resultJSON = Ext.util.JSON.decode(response.responseText);
						
						if(resultJSON.success){
							Ext.example.msg(resultJSON.title,resultJSON.msg);
							tableAddWin.hide();
						}else{
							Ext.ux.showMsg(resultJSON);
						}
					},
					failure : function(response , option){
						Ext.ux.showMsg(Ext.util.JSON.decode(response.responseText));
					}
				});
			}
			tablePanel.getStore().reload();//添加餐台成功时在加载一次数据；
		}
	},{
		text : '取消',
		handler : function(){
			tableAddWin.hide();
			isPrompt = false;
		}
	}],
	listeners : {//监听；
		show : function(thiz){
			
			tableAddWin.findById('tableAddNumber').setValue('');
			tableAddWin.findById('tableAddNumber').clearInvalid();
			
			tableAddWin.findById('tableAddName').setValue('');
			tableAddWin.findById('tableAddName').clearInvalid();
			
			var tableAddAilass = Ext.getCmp('tableAddAilas');
			var root = { root:[] };
			
			for ( var i = 0; i < regionTreeData.length; i++) {
				root.root.push(regionTreeData[i]);
			}
			
			tableAddAilass.store.loadData(root);
			tableAddWin.findById('tableAddAilas').setValue('');
			tableAddWin.findById('tableAddAilas').clearInvalid();
			
			tableAddWin.findById('tableAddMincost').setValue('');
			tableAddWin.findById('tableAddMincost').clearInvalid();
			
			tableAddWin.findById('tableAddSerRate').setValue('');
			tableAddWin.findById('tableAddSerRate').clearInvalid();
			
			var f = Ext.get('tableAddNumber');
			f.focus.defer(100,f);//获得鼠标的焦距；
		}
	},
	keys : [{//是增加键盘的确认动能；既是话相当于鼠标单击保存的功能是一样的；
		key : Ext.EventObject.ENTER,//此处是获得ENTER键
		fn : function(){
			Ext.getCmp('btSureAddTable').handler();
		},
		scope : this
	}]
});


//添加餐台图标；
var tableAddBut = new Ext.ux.ImageButton({
	imgPath : '../../images/btnAddForBigBar.png',
	imgWidth : 50,
	imgHeight : 50,
	tooltip : '添加餐桌',
	handler : function(btn){
		tableAddWin.show();
	}
});

//餐厅管理中的  ：：修改 --begin
tableUpdate = function(){
	
	if(!Ext.ux.getSelData(tablePanel.getId())){//先判断某行是否有被点击了；
		Ext.example.msg('tips','请选择一行在点击修改！！');
		return ;
	}
	
	if(tablePanel.getStore().getAt(currRowIndex).get('tableStatusDisplay') == 1){
		Ext.MessageBox.alert('ERROR','此餐台正在就餐，不能修改！');
	}else{//弹出修改窗口；
		if(!tableUpdateWin){
			tableUpdateWin = new Ext.Window({
				layout : 'fit',
				title : '修改餐台信息',
				modal : true,
				width : 255,
				height : 175,
				closeAction : 'hide',
				resizable : false,//默认为true；
				items : [{
					layout : 'form',
					id : 'tableUpdateFrom',
					labelWidth : 60,
					border : true,
					frame : true,
					items : [{
						xtype : 'hidden',
						id : 'tableID',
						fieldLabel : '餐台编号',
						width : 160
					},{
						xtype : 'textfield',
						id : 'tableUpdateName',
						fieldLabel : '餐台名称',
						width : 160
					},{
						xtype : 'combo',
						id : 'regionUpdateComb',
						fieldLabel : '餐台区域',
						width : 160,
						store : new Ext.data.JsonStore({
							fields : ['regionID','regionName'],
							root : 'root'
						}),
						displayField : 'regionName',//显示文本；
						valueField : 'regionID',//显示值；
						mode : 'local',//数据加载模式；
						triggerAction : 'all',//显示所有下列数据；
						typeAhead : true,
						selectOnFocus : true,
						forceSelection : true,
						allowBlank : false,//是否允许空值，默认为true，这里设置不能为空；
						blankText : '请选择正确的区域！！'
					},{
						xtype : 'textfield',
						id : 'tableUpdateMincost',
						fieldLabel : '最低消费',
						width : 160
					},{
						xtype : 'textfield',
						id : 'tableUpdateServiceRate',
						fieldLabel : '服务费率',
						width : 160
					}]
				}],
				bbar : ['->',
				    {text : '保存',
				    id : 'btnSaveUpdateTable',
				    iconCls : 'btn_save',
				    
				    handler : function(){
				    	var tableID = Ext.getCmp('tableID');
				    	var tableName = Ext.getCmp('tableUpdateName');
				    	var tableRegion = Ext.getCmp('regionUpdateComb');
				    	var tableMincost = Ext.getCmp('tableUpdateMincost');
				    	var tableServiceRate = Ext.getCmp('tableUpdateServiceRate');
				    	
				    	if (!tableID.isValid() || !tableName.isValid() || !tableRegion.isValid() || !tableMincost || !tableServiceRate.isValid()) {
							return ;
						}
				    	
				    	var save = Ext.getCmp('btnSaveUpdateTable');
				    	var cancel = Ext.getCmp('btnCancelUpdateTable');
				    	
				    	save.setDisabled(true);
				    	cancel.setDisabled(true);
				    	Ext.Ajax.request({
				    		
				    		url : '../../UpdateTable.do',
				    		params : {
				    			restaurantID : restaurantID,
				    			tableID : tableID.getValue(),
				    			tableName : tableName.getValue(),
				    			tableRegion : tableRegion.getValue(),
				    			tableMincost : tableMincost.getValue(),
				    			tableServiceRate : tableServiceRate.getValue()
				    		},
				    		success : function(res, opt){
								var jr = Ext.util.JSON.decode(res.responseText);
								
								if(jr.success){
									Ext.example.msg(jr.title, jr.msg);
									tableUpdateWin.hide();
									Ext.getCmp('btSearchRegion').handler();
								
								}else{
									Ext.ux.showMsg(jr);
								}
								
								save.setDisabled(false);
								cancel.setDisabled(false);
				    		},
				    		failure : function(res, opt) {
								Ext.ux.showMsg(Ext.util.JSON.decode(res.responseText));
								save.setDisabled(false);
								cancel.setDisabled(false);
							}
				    	});
				    }},{
				    	text : '关闭',
				    	id : 'btnCancelUpdateTable',
				    	iconCls : 'btn_close',
				    	handler : function(){
				    		tableUpdateWin.hide();
				    	}
				    }
				],
				listeners : {
					show : function(thiz){
						
						var seldata = Ext.ux.getSelData(tablePanel.getId());
						var tableID = Ext.getCmp('tableID');
						var tableAddName = Ext.getCmp('tableUpdateName');
						var regionAddComb = Ext.getCmp('regionUpdateComb');
						var tableAddMincost = Ext.getCmp('tableUpdateMincost');
						var tableServiceRate = Ext.getCmp('tableUpdateServiceRate');
						
						var root = { root:[] };
						for ( var i = 0; i < regionTreeData.length; i++) {
							root.root.push(regionTreeData[i]);
						}
						
						regionAddComb.store.loadData(root);
						
						tableID.setValue(seldata.tableID);
						tableAddName.setValue(seldata.tableName);
						regionAddComb.setValue(seldata.tableRegion);
						tableAddMincost.setValue(seldata.tableMinCost);
						tableServiceRate.setValue(seldata.tableServiceRate);
					}
				},
				keys : [{//是增加键盘的确认动能；既是话相当于鼠标单击保存的功能是一样的；
					key : Ext.EventObject.ENTER,
					fn : function(){
						Ext.getCmp('btnSaveUpdateTable').handler();
					},
					scope : this
				}]
			});
		}
		tableUpdateWin.show();
	}
};//餐厅管理中的  ：：修改 --end;

/* updateRegionWin---begin */
updateRegion = function(){
	if(!updateRegionWin){
		updateRegionWin = new Ext.Window({
			title : '修改区域信息',
			closable : false,
			resizable : false,
			modal : true,
			width : 230,
//			height : 130,
			closeAction : "hide",
			items : [{
				xtype : 'form',
				layout : 'form',
				labelWidth : 60,
				frame : true,
				items : [ {
					xtype : 'textfield',
					id : 'regionID',
					fieldLabel : '区域编号',
					readOnly : true,
					disabled : true,
					width : 120
				}, {
					xtype : 'textfield',
					id : 'regionName',
					fieldLabel : '区域名称',
					width : 120
				} ]
			} ],
			bbar : [ '->', {
				text : '保存',
				id : 'btSaveUpdateReion',
				iconCls : 'btn_save',
				handler : function() {
					var regionID  = Ext.getCmp('regionID');
					var regionName = Ext.getCmp('regionName');
					Ext.Ajax.request({
						url : '../../UpdateRegion.do',
						params : {
							restaurantID : restaurantID,
							regionID : regionID.getValue(),
							regionName : regionName.getValue()
						},
						success : function(res, opt){
							var jr = Ext.util.JSON.decode(res.responseText);
							Ext.example.msg(jr.title,jr.msg);
							updateRegionWin.hide();
							regionTree.getRootNode().reload();
						},
						failure : function(res, opt){
							Ext.ux.showMsg(Ext.util.JSON.decode(res.responseText));
						}
					});
				}
			}, {
				text : '关闭',
				iconCls : 'btn_close',
				handler : function() {
					updateRegionWin.hide();
				}
			} ],
			listeners : {
				show : function(){
					
					var node = regionTree.getSelectionModel().getSelectedNode();
					Ext.getCmp('regionID').setValue(node.attributes.regionID);
					Ext.getCmp('regionName').setValue(node.text);
					var regionName = Ext.get('regionName');
					regionName.focus.defer(100,regionName);
				}
			},
			keys : [{//是增加键盘的确认动能；既是话相当于鼠标单击保存的功能是一样的；
				key : Ext.EventObject.ENTER,
				fn : function(){
					Ext.getCmp('btSaveUpdateReion').handler();
				},
				scope : this
			}]
		});
	}
	updateRegionWin.show();
	updateRegionWin.center();
};
/* updateRegionWin---end */

/* tableDeleteHandler---begin */
tableDeleteHandler = function(){
	if(tablePanel.getStore().getAt(currRowIndex).get('tableStatusDisplay') == 1){
		Ext.MessageBox.alert('ERROR','此餐台正在就餐，不能删除！！');
	}else{
		Ext.MessageBox.show({
			msg : '确定删除?',
			width :  300,
			buttons : Ext.MessageBox.YESNO,
			fn : function(btn){
				if(btn == 'yes'){
					Ext.Ajax.request({
						
						url : '../../DeleteTable.do',
						
						params : {
							'restaurantID' : restaurantID,
							'tableID' : Ext.ux.getSelData(tablePanel.getId()).tableID
						},
						success : function(res, opt){
							var jr = Ext.util.JSON.decode(res.responseText);
							if(jr.success){
								Ext.example.msg(jr.title, jr.msg);
								tablePanel.store.reload();//如果返回true就重新加载；
							}else{
								Ext.ux.showMsg(jr);
							}
			    		},
			    		failure : function(res, opt) {
							Ext.ux.showMsg(Ext.util.JSON.decode(res.responseText));
						}
					});
				}
			}
		});
	}
};

tableOpt = function(value, cellmeta, record, rowIndex, columnIndex, store){
	return "<a href=\"javascript:tableUpdate(" + rowIndex + ")\">" + "<img src='../../images/Modify.png'/>修改</a>"
		 +"&nbsp;"
		 + "<a href=\"javascript:tableDeleteHandler(" + rowIndex + ")\">" + "<img src='../../images/del.png'/>删除</a>";
};

Ext.onReady(function() {
	Ext.lib.Ajax.defaultPostHeader += '; charset=utf-8';
	Ext.QuickTips.init();
	
	regionTree = new Ext.tree.TreePanel({
		title : '区域管理',
		region : 'west',
		enableDD : true,//是否有拖拽
		width : 200,
		id : 'regionMsg',// id
		border : true,
		frame : true,
		lines : true,
		rootVisible : true,//是否隐藏根节点；
		autoScroll : true,
		bodyStyle : 'backgroundColor:#FFFFFF; border:1px solid #99BBE8;',
		loader : new Ext.tree.TreeLoader({
			dataUrl : '../../QueryRegionTree.do?',
			baseParams : {
				'pin' : pin
			}
		}),
		root : new Ext.tree.AsyncTreeNode({
			expanded : true,
			text : '区域名称',
			leaf : false,
			border : true,
			regionID : '-1',
			listeners : {
				load : function(){
					var treeRoot = regionTree.getRootNode().childNodes;
					if(treeRoot.length > 0){
						regionTreeData = [];
						for(var i = (treeRoot.length - 1); i >= 0; i--){
	    					if(treeRoot[i].attributes.regionID == 255 || treeRoot[i].attributes.regionID == 253){
	    						regionTree.getRootNode().removeChild(treeRoot[i]);
	    					}
	    				}
						for ( var i = 0; i < treeRoot.length; i++){
	        				var tp = {};
	        				tp.regionID = treeRoot[i].attributes.regionID;
	        				tp.regionName = treeRoot[i].text;
	        				regionTreeData.push(tp);
	        			}
						Ext.getCmp('btnRefreshSearchRegion').handler();
					}else{
						regionTree.getRootNode().getUI().hide();
						Ext.Msg.show({
							title : '提示',
							msg : '加载区域信息失败！',
							buttons : Ext.MessageBox.OK
						});
					}
				}
			}
		}),
		tbar : [ '->', {
			text : '修改',
			iconCls : 'btn_edit',
			handler : function(e) {
				var node = regionTree.getSelectionModel().getSelectedNode();
				if (!node || node.attributes.regionID == -1) {
					Ext.example.msg('提示','请选择一个区域在进行修改.');
					return;
				}
				updateRegion();
			}
		}, {
			text : '刷新',
			iconCls : 'btn_refresh',
			handler : function() {
				regionTree.getRootNode().reload();
			}
		} ],
		listeners : {
			click : function(e){
				Ext.getDom('regionNameShowType').innerHTML = e.text;
			},
			dblclick : function(e){
				Ext.getCmp('btSearchRegion').handler();
			}
		}
	});
	cm_tableGrid = new Ext.grid.ColumnModel([
		new Ext.grid.RowNumberer(),{
			header : "编号",
			sortable : true,
			dataIndex : "tableID",
			align : 'center',
			width : 100,
			fixed : true
		},{
			header : "餐台编号",
			sortable : true,
			dataIndex : "tableAlias",
			align : 'center',
			width : 100,
			fixed : true
		},{
			header : "餐台名称",
			sortable : true,
			dataIndex : "tableName",
			align : 'center',
			width : 100,
			fixed : true
		},{
			header : "餐台区域",
			sortable : true,
			dataIndex : "tableRegion",
			align : 'center',
			fixed : true,
			width : 120,
			hidden : true
			/*,renderer : function(value, cellmeta, record){
				var regionName = '--';
				for(var i = 0; i < regionTreeData.length; i++){
					if(regionTreeData[i].regionID == value){
						regionName = regionTreeData[i].regionName;
						break;
					}
				}
				return regionName;
			}*/
		},{
			header : "最低消费（￥）",
			sortable : true,
			dataIndex : "tableMinCost",
			align : 'center',
			width : 130,
			fixed : true
		},{
			header : "服务费率",
			sortable : true,
			dataIndex : "tableServiceRate",
			align : 'center',
			width : 140,
			fixed : true
		},{
			header : "餐台状态",
			sortable : true,
			fixed : true,
			dataIndex : "tableStatusDisplay",
			align : 'center',
			width : 130,
			renderer : function(v){
				if(v == stateAddData[0][0]){
					return stateAddData[0][1];
				}else if(v == stateAddData[1][0]){
					return stateAddData[1][1];
				}else if(v == stateAddData[2][0]){
					return stateAddData[2][1];
				}
			}
		},{
			header : "餐台类型",
			sortable : true,
			dataIndex : "tableCategoryDisplay",
			align : 'center',
			fixed : true,
			hidden : true,
			width : 130,
			renderer : function(v){
				if (v == typeAddData[0][0]) {
					return typeAddData[0][1];
				}else if(v == typeAddData[1][0]){
					return typeAddData[1][1];
				}else if(v == typeAddData[2][0]){
					return typeAddData[2][1];
				}
			}
		},{
			header : "操作",
			align : 'center',
			dataIndex : "tableOpt",
			align : 'center',
			sortable : true,
//			width : 130,
			renderer : tableOpt
		}
	]);
	
	var store_tableGrid = new Ext.data.Store({
		baseParams : {
			'restaurantID' : restaurantID
		},
		proxy : new Ext.data.HttpProxy({
			url : '../../QueryRegionTable.do',
		}),
		reader : new Ext.data.JsonReader({
			totalProperty : "totalProperty",
			root : "root"
		}, new Ext.data.Record.create([
       	    {name : 'tableID',type : 'int',mapping : 'tableID'},
    	    {name : 'tableAlias',type : 'int',mapping : 'tableAlias'},
    	    {name : 'tableName',type : 'string',mapping : 'tableName'},
    	    {name : 'tableRegion',type : 'int',mapping : 'tableRegion'},
    	    {name : 'tableMinCost',type : 'int',mapping : 'tableMinCost'},
    	    {name : 'tableServiceRate',type : 'float',mapping : 'tableServiceRate'},
    	    {name : 'tableStatusDisplay',type : 'int',mapping : 'tableStatusDisplay'},
    	    {name : 'tableCategoryDisplay',type : 'int',mapping : 'tableCategoryDisplay'},
    	    {name : 'tableOpt',type : 'string',mapping : 'tableOpt'}
    	]))
	});
	
	store_tableGrid.load({
		params:{start:0,limit:pageRecordCount}
	});
	
	tablePanel = new Ext.grid.GridPanel({
		title : '餐台管理',
		frame : true,
		region : 'center',
		autoScroll : true,
		cm : cm_tableGrid,
		sm : new Ext.grid.RowSelectionModel({
			singleSelect : true
		}),
		store : store_tableGrid,
		tbar :[{
				xtype : 'tbtext',
				text : String.format(
					Ext.ux.txtFormat.typeName,
					'区域','regionNameShowType','----'
				)
			},{
				xtype:'tbtext', 
	    		text:'&nbsp;&nbsp;'
			},{
				xtype : 'hidden',
				id : 'txtSearchRegionName',
				text : '&nbsp;&nbsp;&nbsp;&nbsp;'
			},{
				xtype : 'tbtext',
				text : '过滤:'  //按什么条件来过滤；
			},
			  filterTypeCombox //过滤条件的下拉框；
			,{
				xtype : 'tbtext',
				text : '&nbsp;&nbsp;&nbsp;'
			},{                //餐台编号、消费范围过滤的下拉框；等于、大于等于、小于等于的下拉框；
				xtype : 'combo',
				hidden : true,
				forceSelection : true,
				width : 100,
				value : operatorData[0][0],
				id : 'operatorFilterId',
				store : new Ext.data.SimpleStore({
					fields : ['value','text'],
					data : operatorData
				}),
				valueField :'value',
				displayField : 'text',
				typeAhead : true,
				mode : 'local',
				triggerAction : 'all',
				selectOnFocus : true,
				allowBlank : false
			},{
				xtype : 'tbtext',
				text : '&nbsp;&nbsp;&nbsp;'
			},{                 //当点下按餐台名称来过滤时候才让其显示出来；
				xtype : 'textfield',
				id : 'tableNameFilterId',
				hidden : true,
				width : 120
			},{                 //当点下按餐台编号、最低消费来过滤时候才让其显示出来；
				xtype : 'numberfield',
				id : 'tableNumberAaialsFilterId',
				style : 'text-align: left;',
				hidden : true,
				width : 120
			},{                 //当点下按餐台状态来过滤时候才让其显示出来；
				xtype : 'combo',
				hidden : true,
				forceSelection : true,
				width : 120,
				value : typeAddData[0][0],
				id : 'tableStateFilterId',
				store : new Ext.data.SimpleStore({
					fields : ['value','text']
				}),
				valueField : 'value',
				displayField : 'text',
				typeAhead : true,
				mode : 'local',
				selectOnFocue : true,
				triggerAction : 'all',
				allowBlank : false
			},{                 //当点下按餐台类型来过滤时候才让其显示出来；
				xtype : 'combo',
				hidden : true,
				forceSelection : true,
				width : 120,
				value : typeAddData[0][0],
				id : 'tableTypeFilterId',
				store : new Ext.data.SimpleStore({
					fields : ['value','text']
				}),
				valueField : 'value',
				displayField : 'text',
				typeAhead : true,
				mode : 'local',
				selectOnFocue : true,
				triggerAction : 'all',
				allowBlank : false
			},'->',{
				text : '搜索',
				id : 'btSearchRegion',
				iconCls : 'btn_search',
				handler : function(){
					var regionID = '';//区域编号
					var regionName = Ext.getCmp('txtSearchRegionName').getValue();
					var selNode = regionTree.getSelectionModel().getSelectedNode();
					regionID = !selNode ? regionID : selNode.attributes.regionID;
					regionName = regionName.replace(/(^\s*)|(\s*$)/g, '');
					
					var operatorNumbersO = '';//声明操作符号；等于、大于等于、小于等于
					var operatorNumbersN = '';//声明操作餐台编号
					var operatorNumbersA = '';//声明操作最低消费
					var operatorName = '';//声明操作名字
					var operatorStates = '';//声明操作的餐台状态
					var operatorTypes = '';//声明操作的餐台类型
					
					if(tmp == 0){//表示全部
						operatorNumbersO = '';
						operatorNumbersN = '';
						operatorStates = '';
						operatorTypes = '';
						operatorNumberA = '';
					}else if(tmp == 1 || tmp == 3){//餐台编号\最低消费
						var operatorNumbers = conditionType.split(',');//将其拆分
						operatorNumbersO = Ext.getCmp(operatorNumbers[0]).getValue();//操作符
						if(tmp == 1){//拆分之后拿到餐台编号
							operatorNumbersN = Ext.getCmp(operatorNumbers[1]).getValue();//操作值（餐台编号）
						}
						if(tmp == 3){//拆分之后拿到最低消费
							operatorNumbersA = Ext.getCmp(operatorNumbers[1]).getValue();//操作值（最低消费）
						}
					}else if(tmp == 2){//餐台名称
						operatorName = Ext.getCmp(conditionType).getValue();
					}else if(tmp == 4){//餐台状态
						operatorStates = Ext.getCmp(conditionType).getValue();
					}else{//餐台类型
						operatorTypes = Ext.getCmp(conditionType).getValue();
					}
					
					var gs = tablePanel.getStore();
					gs.baseParams['regionName'] = regionName;
					gs.baseParams['regionID'] = regionID;
					gs.baseParams['operatorNumbersO'] = operatorNumbersO;
					gs.baseParams['operatorNumbersN'] = operatorNumbersN;
					gs.baseParams['operatorNumbersA'] = operatorNumbersA;
					gs.baseParams['operatorName'] = operatorName;
					gs.baseParams['operatorStates'] = operatorStates;
					gs.baseParams['operatorTypes'] = operatorTypes;
					
					gs.load({
						params : {
							start : 0,
							limit : pageRecordCount,
						}
					});
				},
				keys : [{//是增加键盘的确认动能；既是话相当于鼠标单击保存的功能是一样的；
					key : Ext.EventObject.ENTER,//此处是按下ENTER键
					fn : function(){
						Ext.getCmp('btSearchRegion').handler();
					},
					scope : this
				}]
			},{
				text : '修改',
				iconCls : 'btn_edit',
				handler : function(){
					tableUpdate();//GridPanel的右上角的修改操作；function;
				}
			},{
				text : '重置',
				id : 'btnRefreshSearchRegion',
				iconCls : 'btn_refresh',
				handler : function(){
					
					regionTree.getSelectionModel().clearSelections();
					Ext.getDom('regionNameShowType').innerHTML = '----';
					Ext.getCmp('txtSearchRegionName').setValue();
					Ext.getCmp('btSearchRegion').handler();
					
					Ext.getCmp('filter').setValue(0);
					Ext.getCmp('filter').fireEvent('select',null,null,0);
					
					Ext.getCmp('btSearchRegion').handler();
				}
			}]
		,bbar : new Ext.PagingToolbar({
			store : store_tableGrid,
			pageSize : pageRecordCount,
			displayInfo : true,
			displayMsg : '显示第 {0} 条到 {1} 条记录，共 {2} 条',
			emtpyMsg : '没有记录'
		}),
		listeners : {
			'rowclick' : function(thiz,rowIndex,e){
				currRowIndex = rowIndex;
			}
		}
	});

	viewport = new Ext.Viewport({
		layout : "border",
		id : "viewport",
		items : [{
			region : "north",
			bodyStyle : "background-color:#DFE8F6;",
			html : "<h4 style='padding:10px;font-size:150%;float:left;'>无线点餐网页终端</h4><div id='optName' class='optName'></div>",
			height : 50,
			border : false,
			margins : '0 0 0 0'
		},new Ext.Panel({
				title : '区域餐台管理',
				region : "center",
				layout : "border",
				frame : true,
				margins : '5 5 5 5',
				items : [regionTree,tablePanel],
				tbar : new Ext.Toolbar({
					height : 55,
					items : [tableAddBut,{
						text : "&nbsp;&nbsp;&nbsp;",
						disabled : true
						},"->", pushBackBut,{
						text : "&nbsp;&nbsp;&nbsp;",
						disabled : true
					}, logOutBut ]
				})
		}),{
			region : "south",
			height : 30,
			layout : "form",
			frame : true,
			border : false,
			html : "<div style='font-size:11pt; text-align:center;'><b>版权所有(c) 2011 智易科技</b></div>"
		} ]
	});
});