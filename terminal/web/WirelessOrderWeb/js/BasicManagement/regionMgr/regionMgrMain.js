//声明：
var regionGrid;
var regionTree;
// 修改弹出框；
var updateRegionWin;
var tablePanel;
var tableUpdateWin;
var tableAddWin;

////////////////////aaaa//////////////////////////

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

//弹出添加餐桌窗口；
tableAdd = function(){
	if(!tableAddWin){
		tableAddWin = new Ext.Window({
			layout : 'fit',
			title : '添加餐桌',
			width : 260,
			height : 206,
			closeAction : 'hide',
			resizable : false,
			items : [{
				layout : 'form',
				id : 'tableAddForm',
				labelWidth : 60,
				border : false,
				frame : true,
				items : [{
					width : 160,
					xtype  : 'numberfield',
					fieldLabel : '餐桌编号',
					id : 'tableAddNumber',
					allowBlank : false
				},{
					width : 160,
					xtype : 'textfield',
					fieldLabel : '餐桌名称',
					id : 'tableAddName'
				},{
					width : 160,
					xtype : 'textfield',
					fieldLabel : '餐桌区域',
					id : 'tableAddAilas',
					validator : function(v){
						if(v < 10 || v >= 0){
							return '餐桌区域必须为 0~9 ！！';
						}else{
							return true;
						}
					}
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
					tablePanel.getStore().reload();//
				}
			},{
				text : '取消',
				handler : function(){
					//处理；
					tableAddWin.hide();
				}
			}],
			listeners : {
				//监听；
				'show' : function(thiz){
					tableAddWin.findById('tableAddNumber').setValue('');
					tableAddWin.findById('tableAddNumber').clearInvalid();
					
					tableAddWin.findById('tableAddName').setValue('');
					tableAddWin.findById('tableAddName').clearInvalid();
					
					tableAddWin.findById('tableAddAilas').setValue('');
					tableAddWin.findById('tableAddAilas').clearInvalid();
					
					tableAddWin.findById('tableAddMincost').setValue('');
					tableAddWin.findById('tableAddMincost').clearInvalid();
					
					tableAddWin.findById('tableAddSerRate').setValue('');
					tableAddWin.findById('tableAddSerRate').clearInvalid();
					
					var f = Ext.get('tableAddNumber');
					f.focus.defer(100,f);
				}
			}
		});
	}
	tableAddWin.show();
	tableAddWin.center();
};

//添加餐台图标；
var tableAddBut = new Ext.ux.ImageButton({
	imgPath : '../../images/btnAddForBigBar.png',
	imgWidth : 50,
	imgHeight : 50,
	tooltip : '添加餐桌',
	handler : function(btn){
		tableAdd();
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

// 2，栏位模型
var regionColumnModel = new Ext.grid.ColumnModel([ 
    new Ext.grid.RowNumberer(),
    {
		header : "名称",
		sortable : true,
		dataIndex : "regionName",
		width : 100,
		editor : new Ext.form.TextField({
			allowBlank : false,
			selectOnFocus : true
		})
    } 
]);

//combobox中的数据--begin
var storeSel  = new Ext.data.Store({
	baseParams : {//传入restaurantID参数；
		'restaurantID' : restaurantID
	},
	proxy : new Ext.data.HttpProxy({//代理
		url : '../../QueryRegionTree2Combobox.do'
	}),
	reader : new Ext.data.JsonReader({},//记录；
		Ext.data.Record.create([
            {name : 'cID',type : 'int',mapping : 'cID'},
            {name : 'cNAME',type : 'string',mapping : 'cNAME'}
        ])
	),autoLoad : true//即时加载数据；
});//combobox中的数据--end

//餐厅管理中的  ：：修改 --begin
tableUpdate = function(){
	if(!Ext.ux.getSelData(tablePanel.getId())){//先判断某行是否有被点击了；
		Ext.example.msg('tips','请选择一行在点击修改！！');
		return ;
	}
	//弹出修改窗口；
	if(!tableUpdateWin){
		tableUpdateWin = new Ext.Window({
			title : '修改餐台信息',
			id : 'btUpdateTable',
			closable : false,
			modal : true,
			width : 263,
			items : [{
				xtype : 'form',
				layout : 'form',
				frame : true,
				items : [{
					xtype : 'hidden',
					id : 'tableID',
					fieldLabel : '餐台编号'
				},{
					xtype : 'textfield',
					id : 'tableAddName',
					fieldLabel : '餐台名称',
					allowBlank : false
				},{
					xtype : 'combo',
					id : 'regionAddComb',
					fieldLabel : '餐台区域',
					width : 130,
					store : storeSel,
					displayField : 'cNAME',//显示文本；
					valueField : 'cID',//显示值；
					mode : 'local',//数据加载模式；
					triggerAction : 'all',//显示所有下列数据；
					typeAhead : true,
					selectOnFocus : true,
					forceSelection : true,
					allowBlank : false,//是否允许空值，默认为true，这里设置不能为空；
					blankText : '请选择正确的区域！！'
				},{
					xtype : 'textfield',
					id : 'tableAddMincost',
					fieldLabel : '最低消费'
				},{
					xtype : 'textfield',
					id : 'tableServiceRate',
					fieldLabel : '服务费率'
				}]
			}],
			bbar : ['->',
			    {text : '保存',
			    id : 'btnSaveUpdateTable',
			    iconCls : 'btn_save',
			    handler : function(){
			    	var tableID = Ext.getCmp('tableID');
			    	var tableName = Ext.getCmp('tableAddName');
			    	var tableRegion = Ext.getCmp('regionAddComb');
			    	var tableMincost = Ext.getCmp('tableAddMincost');
			    	var tableServiceRate = Ext.getCmp('tableServiceRate');
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
				show : function(){
					var seldata = Ext.ux.getSelData(tablePanel.getId());
					var tableID = Ext.getCmp('tableID');
					var tableAddName = Ext.getCmp('tableAddName');
					var regionAddComb = Ext.getCmp('regionAddComb');
					var tableAddMincost = Ext.getCmp('tableAddMincost');
					var tableServiceRate = Ext.getCmp('tableServiceRate');
//					alert(tableAddName);
//					var root = {
//						root:[]
//					};
//					for ( var int = 0; int < array.length; int++) {
//						
//					}
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
					Ext.getCmp('btUpdateTable').handler();
				},
				scope : this
			}]
		});
	}
	//要在这里在重新加载数据；???
	/*function(){
//		Ext.getCmp('regionAddComb').store.reload();
//		Ext.getCmp('regionAddComb').reset();
	});*/
	tableUpdateWin.show();
	tableUpdateWin.center();
};//餐厅管理中的  ：：修改 --end;

/* updateRegionWin---begin */
updateRegionWin = new Ext.Window({
	title : '修改区域信息',
	closable : false,
	resizable : false,
	modal : true,
	width : 230,
	height : 120,
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
//					Ext.getCmp('btUpdateTable').sotre.reload();
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
				updateRegionWin.show();
				updateRegionWin.center();
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
			width : 100,
			fixed : true
		},{
			header : "餐台编号",
			sortable : true,
			dataIndex : "tableAlias",
			width : 100,
			fixed : true
		},{
			header : "餐台名称",
			sortable : true,
			dataIndex : "tableName",
			width : 100,
			fixed : true
		},{
			header : "餐台区域",
			sortable : true,
			dataIndex : "tableRegion",
			fixed : true,
			width : 120/*,
			renderer : function(value){
				alert(value.length);
				alert(regionTreeData.length);
				var regionName = '--';
				for(var i = 0; i < regionTreeData.length; i++){
					if(record.get('tableRegion') == regionTreeData[i].regionID){
						alert(regionTreeData[i].regionID);
						regionNamen = regionTreeData[i].regionName;
						break;
					}
				}
				return regionName;
				alert(value);
			}*/
		},{
			header : "最低消费（￥）",
			sortable : true,
			dataIndex : "tableMinCost",
			width : 130,
			fixed : true
		},{
			header : "服务费率",
			sortable : true,
			dataIndex : "tableServiceRate",
			width : 140,
			fixed : true
		},{
			header : "餐台状态",
			sortable : true,
			fixed : true,
			dataIndex : "tableStatusDisplay",
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
			sortable : true,
			renderer : tableOpt
		}
	]);
	
	table_proxy = new Ext.data.HttpProxy({//远程加载数据；
		url : '../../QueryRegionTable.do',
	});
	table_GridRecord = new Ext.data.Record.create([
	    {name : 'tableID',type : 'int',mapping : 'tableID'},
	    {name : 'tableAlias',type : 'int',mapping : 'tableAlias'},
	    {name : 'tableName',type : 'string',mapping : 'tableName'},
	    {name : 'tableRegion',type : 'int',mapping : 'tableRegion'},
	    {name : 'tableMinCost',type : 'int',mapping : 'tableMinCost'},
	    {name : 'tableServiceRate',type : 'float',mapping : 'tableServiceRate'},//一定要跟数据库的类型相同，否则会出现数据转换，以致将返回的数据转换成其他类型；
	    {name : 'tableStatusDisplay',type : 'int',mapping : 'tableStatusDisplay'},
	    {name : 'tableCategoryDisplay',type : 'int',mapping : 'tableCategoryDisplay'},
	    {name : 'tableOpt',type : 'string',mapping : 'tableOpt'}
	]);
	
	//reader;
	var reader = new Ext.data.JsonReader({
		totalProperty : "totalProperty",
		root : "root"
	}, table_GridRecord);
	
	//store_tableGrid;
	var store_tableGrid = new Ext.data.Store({
		baseParams : {
			'restaurantID' : restaurantID
		},
		proxy : table_proxy,
		reader : reader
	});
	store_tableGrid.load({//传入分页参数；
		params:{start:0,limit:14}
	});
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
		tbar : new Ext.Toolbar({
			height : 26,
			items : [{
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
				id : 'txtSearchRegionName'
			},'->',
			{
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
//					tablePanel.store.reload();//重新加载；
					
//					Ext.getCmp('regionAddComb').store.reload();
					
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
		}),
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
				ds : regionStore,
				cm : regionColumnModel
			});

	// 为store配置beforeload监听器
	regionGrid.getStore().on('beforeload', function() {
		// 输入查询条件参数
		this.baseParams = {
			"pin" : pin,
			"isPaging" : true,
			"isCombo" : false,
			"isTree" : false
		};
	});

	// 为store配置load监听器(即load完后动作)
	regionGrid.getStore().on('load', function() {
		if (regionGrid.getStore().getTotalCount() != 0) {
			var msg = this.getAt(0).get("message");
			if (msg != "normal") {
				Ext.MessageBox.show({
					msg : msg,
					width : 300,
					buttons : Ext.MessageBox.OK
				});
				this.removeAll();
			} 
		}
	});
	
	var centerPanel = new Ext.Panel({
		title : '区域餐台管理',
		region : "center",
		layout : "border",
		frame : true,
		margins : '5 5 5 5',
		items : [regionTree,
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
			centerPanel,
				{
			region : "south",
			height : 30,
			layout : "form",
			frame : true,
			border : false,
			html : "<div style='font-size:11pt; text-align:center;'><b>版权所有(c) 2011 智易科技</b></div>"
		} ]
	});
});