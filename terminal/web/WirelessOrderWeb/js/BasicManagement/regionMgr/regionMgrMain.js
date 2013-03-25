//声明：
var regionGrid;
var regionTree;
// 修改弹出框；
var updateRegionWin;
var tablePanel;
var tableUpdateWin;
var tableAddWin;
var regionTreeData;

////////////////////aaaa//////////////////////////
//过滤条件；
var filterTypeData = [ [ 0, '全部' ], [ 1, '餐台编号' ], [ 2, '餐台名称' ], [ 3, '最低消费' ], [ 4, '餐台状态' ], [ 5, '餐台类型' ] ];
//餐台类型过滤；
var typeAddData = [ [ 1, '一般' ], [ 2, '外卖' ], [ 3, '并台' ] ];
var typeAddStore = new Ext.data.SimpleStore({
	fields : [ 'value', 'text' ],
	data : typeAddData
});
//餐台状态过滤；
var stateAddData = [ [ 0, '空闲'],[ 1, '就餐'],[ 2, '预定'] ];
var stateAddStore = new Ext.data.SimpleStore({
	fields : ['value','text'],
	data : stateAddData
});
//餐台编号、消费范围过滤
var operatorData = [ [ 1, '等于' ], [ 2, '大于等于' ], [ 3, '小于等于' ] ];

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
			//一系列的操作啦；
			var operatorFilterId = Ext.getCmp('operatorFilterId');
			var tableNameFilterId = Ext.getCmp('tableNameFilterId');
			var tableNumberAaialsFilterId = Ext.getCmp('tableNumberAaialsFilterId');
			var tableStateFilterId = Ext.getCmp('tableStateFilterId');
			var tableTypeFilterId = Ext.getCmp('tableTypeFilterId');
			
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
				conditionType = tableNumberAaialsFilterId.getId();
			}else if(index == 2){
				tableNameFilterId.setVisible(true);
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
		var isChange = false;
		regionGrid.getStore().each(function(record) {
			if (record.isModified("regionName") == true) {
				isChange = true;
			}
		});
		if (isChange) {
			Ext.MessageBox.show({
				msg : "修改尚未保存，是否确认返回？",
				width : 300,
				buttons : Ext.MessageBox.YESNO,
				fn : function(btn) {
					if (btn == "yes") {
						location.href = "BasicMgrProtal.html?restaurantID="
								+ restaurantID + "&pin=" + pin;
					}
				}
			});
		} else {
			location.href = "BasicMgrProtal.html?restaurantID=" + restaurantID
					+ "&pin=" + pin;
		}
	}
});

var logOutBut = new Ext.ux.ImageButton({
	imgPath : "../../images/ResLogout.png",
	imgWidth : 50,
	imgHeight : 50,
	tooltip : "登出",
	handler : function(btn) {
	}
});

//regionName
var regionStore = new Ext.data.Store({
	proxy : new Ext.data.HttpProxy({
		url : "../../QueryRegion.do"
	}),
	reader : new Ext.data.JsonReader({
		totalProperty : "totalProperty",
		root : "root"
	}, [ {
		name : "regionID"
	}, {
		name : "regionName"
	}, {
		name : "message"
	} ])
});
// menuStore.reload();

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
//			allowBlank : false,
			width : 160,
			validator : function(v){
				if(v < 65535 || v > 0){
					return '餐台编号范围是 1 至65535 ！';
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
			tablePanel.getStore().reload();//???????????????????????????????
		}
	},{
		text : '取消',
		handler : function(){
			//处理；
			tableAddWin.hide();
			isPrompt = false;
		}
	}],
	listeners : {
		//监听；
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
			f.focus.defer(100,f);
		}
	},
	keys : [{//是增加键盘的确认动能；既是话相当于鼠标单击保存的功能是一样的；
		key : Ext.EventObject.ENTER,
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
		if (!isPrompt) {
			tableAddWin.show();
			isPrompt = true;
		}
	}
});
// 1，表格的数据store
var searchForm = new Ext.Panel({
	border :  false,
	width : 130,
	id : 'searchForm',
	items : [{
		xtype : 'textfield',
		hidelLabel : true,
		id : '',
		allowBlank : false,
		width :  120
	}]
});

//combobox中的数据--begin
//var storeSel  = new Ext.data.Store({
//	baseParams : {//传入restaurantID参数；
//		'restaurantID' : restaurantID
//	},
//	proxy : new Ext.data.HttpProxy({//代理
//		url : '../../QueryRegionTree2Combobox.do'
//	}),
//	reader : new Ext.data.JsonReader({},//记录；
//		Ext.data.Record.create([
//            {name : 'cID',type : 'int',mapping : 'cID'},
//            {name : 'cNAME',type : 'string',mapping : 'cNAME'}
//        ])
//	),autoLoad : true//即时加载数据；
//});//combobox中的数据--end

//餐厅管理中的  ：：修改 --begin
tableUpdate = function(){
	if(!Ext.ux.getSelData(tablePanel.getId())){//先判断某行是否有被点击了；
		Ext.example.msg('tips','请选择一行在点击修改！！');
		return ;
	}
	//弹出修改窗口；
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
//					allowBlank : false,
					width : 160
				},{
					xtype : 'combo',
					id : 'regionUpdateComb',
					fieldLabel : '餐台区域',
					width : 160,
					//总结：如果此处数据是从某个action请求回来的话，displayField和valueField一定要和其中的Key相同，否则不会显示，而要是从一个JsonStrore中拿取数据其Key值也要和其中的Key相对应
//					store : storeSel,
//					displayField : 'cNAME',//显示文本；
//					valueField : 'cID',//显示值；
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
//						alert(regionTreeData[i].regionID + "," + regionTreeData[i].regionName);
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
	//要在这里在重新加载数据；???
	tableUpdateWin.show();
//	tableUpdateWin.center();
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
			height : 120,
			closeAction : "hide",
//			floating : true,
//			shadow : true,//要floating设置为true时才有效
			items : [{
				xtype : 'form',
				layout : 'form',
				labelWidth : 65,
				labelheigh : 20,
				frame : true,
				items : [ {
					xtype : 'textfield',
					id : 'regionID',// 区域编号Id;
					fieldLabel : '区域编号',
					readOnly : true,
					disabled : true // 默认可编辑（false）；
				}, {// 区域名称；
					xtype : 'textfield',
					id : 'regionName',
					fieldLabel : '区域名称'
				} ]
			} ],
			bbar : [ '->', {
				text : '保存',
				id : 'btSaveUpdateReion',
				iconCls : 'btn_save',
				handler : function() {
					// 事件；
					var regionID  = Ext.getCmp('regionID');
					var regionName = Ext.getCmp('regionName');
					Ext.Ajax.request({
						url : '../../UpdateRegion.do',//请求URL
						params : {//请求时的参数；
							restaurantID : restaurantID,//餐馆ID
							regionID : regionID.getValue(),//区域ID
							regionName : regionName.getValue()//区域名称
						},
						success : function(res, opt){//请求成功时要执行的操作；
							var jr = Ext.util.JSON.decode(res.responseText);
							Ext.example.msg(jr.title,jr.msg);
							updateRegionWin.hide();//关闭窗口；
							regionTree.getRootNode().reload();//成功之后重新加载；?????
//							Ext.getCmp('btUpdateTable').sotre.reload();
						},
						failure : function(res, opt){//请求失败时要执行的操作；
							Ext.ux.showMsg(Ext.util.JSON.decode(res.responseText));
						}
					});
				}
			}, {
				text : '关闭',
				iconCls : 'btn_close',
				handler : function() {
					updateRegionWin.hide();//关闭窗口；
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
tableDeleteHandler = function(){
	Ext.MessageBox.show({
		msg : '确定删除?',
		width :  300,
		buttons : Ext.MessageBox.YESNO,
		fn : function(btn){
			if(btn=='yes'){
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
};

//餐台修改/删除操作；
tableOpt = function(value, cellmeta, record, rowIndex, columnIndex, store){//需要传参数：rowNumber；
	return "<a href=\"javascript:tableUpdate(" + rowIndex + ")\">" + "<img src='../../images/Modify.png'/>修改</a>"
		 +"&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;"
		 + "<a href=\"javascript:tableDeleteHandler(" + rowIndex + ")\">" + "<img src='../../images/del.png'/>删除</a>";
};
/////////////////////aaaa/////////////////////////
Ext.onReady(function() {
	// 解决ext中文传入后台变问号问题
	Ext.lib.Ajax.defaultPostHeader += '; charset=utf-8';
	Ext.QuickTips.init();
	/////////////////////////////////
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
		// 加入根节点；
		loader : new Ext.tree.TreeLoader({
			dataUrl : '../../QueryRegionTree.do?time='+new Date(),
			baseParams : {
				'restaurantID' : restaurantID
			}
		}),
		root : new Ext.tree.AsyncTreeNode({
			expanded : true,
			text : '区域名称',
			iconCls : 'me-iconCls',
			leaf : false,
			border : true,
			regionID : '-1',
			listeners : {
				load : function(){//加载数据，放到regionTreeData[],以至Gridpanel中的数字相对应；
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
//						for(var i = 0 ; i < regionTreeData.length ; i++){
//							alert(regionTreeData[i].regionName);
//						}
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
		tbar : [ '->', {// 在tbar中加入“修改”
			text : '修改',
			iconCls : 'btn_edit',
			// 事件处理；handler（）；
			handler : function(e) {
				var node = regionTree.getSelectionModel().getSelectedNode();
				if (!node || node.attributes.regionID == -1) {//判断是否有选择节点；
					Ext.example.msg('提示','请选择一个区域在进行修改！');
					return;
				}
				updateRegion();//弹出更新区域窗口；
			}
		}, {// 在tbar中加入“刷新”
			text : '刷新',
			iconCls : 'btn_refresh',
			// 事件处理；handler（）；
			handler : function() {
				regionTree.getRootNode().reload();
			}
		} ],
		listeners : {
			click : function(e){//单击，
				Ext.getDom('regionNameShowType').innerHTML = e.text;
			},
			dblclick : function(e){//还有双击
				
				Ext.getCmp('btSearchRegion').handler();
			}
		}
	});
	////////////////////GridPanel--begin//////////////////////////////////
	statusData=[['0','空闲'],['1','就餐'],['2','预定']];
	categoryData=[['0','一般'],['1','外卖'],['2','并台']];
	//列模型；
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
			renderer : function(value, cellmeta, record){//????
				var regionName = '--';
				for(var i = 0; i < regionTreeData.length; i++){
					if(regionTreeData[i].regionID == value){
						regionName = regionTreeData[i].regionName;
						break;
					}
				}
				return regionName;
			}
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
			renderer : function(v){//对当前的信息进行在加工的回调函数；
				if(v==0){
					return "空闲";
				}else if(v==1){
					return "就餐";
				}else if(v==2){
					return "预定";
				}
			}
		},{
			header : "餐台类型",
			sortable : true,
			dataIndex : "tableCategoryDisplay",
			align : 'center',
			fixed : true,
			width : 130,
			renderer : function(v){
				if (v==1) {
					return "一般";
				}else if(v==2){
					return "外卖";
				}else if(v==3){
					return "并台";
				}
			}
		},{
			header : "操作",
			align : 'center',
			dataIndex : "tableOpt",
			align : 'center',
			sortable : true,
			renderer : tableOpt
		}
	]);
	
	//store_tableGrid;
	var store_tableGrid = new Ext.data.Store({
		baseParams : {
			'restaurantID' : restaurantID
		},
		proxy : new Ext.data.HttpProxy({//远程加载数据；
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
    	    {name : 'tableServiceRate',type : 'float',mapping : 'tableServiceRate'},//一定要跟数据库的类型相同，否则会出现数据转换，以致将返回的数据转换成其他类型；
    	    {name : 'tableStatusDisplay',type : 'int',mapping : 'tableStatusDisplay'},
    	    {name : 'tableCategoryDisplay',type : 'int',mapping : 'tableCategoryDisplay'},
    	    {name : 'tableOpt',type : 'string',mapping : 'tableOpt'}
    	]))
	});
	store_tableGrid.load({//传入分页参数；
		params:{start:0,limit:14}
	});
	
//	store_tableGrid.on('beforeload',function(){
//        Ext.apply(
//            this.baseParams,
//            {
//                username:Ext.get('username').dom.value,
//                real_name:Ext.get('real_name').dom.value,
//                email:Ext.get('email').dom.value
//            	var regionID = '';
//				var regionName = Ext.getCmp('txtSearchRegionName').getValue();
//				var selNode = regionTree.getSelectionModel().getSelectedNode();
//				regionID = !selNode ? regionID : selNode.attributes.regionID;
//				regionName = regionName.replace(/(^\s*)|(\s*$)/g, '');//等同于java中的trim；
//            	start : 0,
//				limit : 14,
//				regionID : 5,
//				regionName : Ext.getCmp('txtSearchRegionName').getValue()
//            });
//	});
////////////////////GridPanel--center//////////////////////////////////
	//中间_右边的Grid布局；
	tablePanel = new Ext.grid.GridPanel({
		title : '餐台管理',
		height : 480,
		autoScroll : true,
		cm : cm_tableGrid,//列模型；
		sm : new Ext.grid.RowSelectionModel({
			singleSelect : true
		}),//行选择模型；
		store : store_tableGrid,//数据；
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
				text : '&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;'
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
				value : '文本框',
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
					fields : ['value','text']/*,
					data : stateAddData*/
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
					fields : ['value','text']/*,
					data : typeAddData*/
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
					var regionID = '';
					var regionName = Ext.getCmp('txtSearchRegionName').getValue();
					var selNode = regionTree.getSelectionModel().getSelectedNode();
					regionID = !selNode ? regionID : selNode.attributes.regionID;
					regionName = regionName.replace(/(^\s*)|(\s*$)/g, '');//等同于java中的trim；
					tablePanel.getStore().load({
						params : {
							start : 0,
							limit : 14,
							regionID : regionID,
							regionName : regionName
						}
					});
				}
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
				}
			}]
//		})
	,
		bbar : new Ext.PagingToolbar({//下面的工具栏；
			store : store_tableGrid,//数据
			pageSize : 14,//一页所显示的数据；
			displayInfo : true,
			displayMsg : '显示第 {0} 条到 {1} 条记录，共 {2} 条',
			emtpyMsg : '没有记录'
			
		}),
		listeners : {
			'rowclick' : function(thiz,rowIndex,e){
				currRowIndex = rowIndex;
			}
		}
	});////////////////////GridPanel--end//////////////////////////////////
	
	regionGrid = new Ext.grid.EditorGridPanel({
				// title : "部门",
				xtype : "grid",
				anchor : "99%",
				region : "center",
				frame : true,
				margins : '0 5 0 0',
				ds : regionStore
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
				},
			new Ext.Panel({
				title : '区域餐台管理',
				region : "center",
				layout : "border",
				frame : true,
				margins : '5 5 5 5',
				items : [
			         regionTree,
			         new Ext.Panel({
			        	 region : 'center',
			        	 margins : '0 0 0 5',
			        	 items : tablePanel
				 })],
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