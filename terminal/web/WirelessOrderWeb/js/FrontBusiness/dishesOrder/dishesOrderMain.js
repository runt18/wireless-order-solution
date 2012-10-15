
addTasteHandler = function(thiz){
	var hgs = haveTasteGrid.getStore();
	var sr = thiz.getSelectionModel().getSelections()[0];
	var cs = true;
	
	if(hgs.getCount() >= 3){
		Ext.example.msg('提示', '该菜品已选择三种口味,最多只能选择三种.');
		return;
	}
	
	hgs.each(function(r){
		if(r.get('tasteAliasID') == sr.get('tasteAliasID')){
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
	frame : true,
	loadMask : { msg: '数据请求中,请稍等......' },
	viewConfig : {
		forceFit : true
	},
	cm : new Ext.grid.ColumnModel([
		new Ext.grid.RowNumberer(),
		{header:'口味名', dataIndex:'tasteName', width:120},
		{header:'价钱', dataIndex:'tastePrice', width:80, align:'right', renderer:Ext.ux.txtFormat.gridDou},
		{header:'', dataIndex:'tasteRate', hidden:true},
		{header:'', dataIndex:'tasteCalcFormat', hidden:true},
		{header:'', dataIndex:'operation', hidden:true}
	]),
	ds : new Ext.data.JsonStore({
		url : '../../QueryFoodTaste.do',
		root : 'root',		
		fields : ['tasteID', 'tasteAliasID', 'tasteName', 'tastePrice', 'tasteRate', 'tasteCalcFormat'],
		listeners : {
			beforeload : function(){
				var selData = Ext.ux.getSelData('orderedGrid');
				this.baseParams['foodID'] = selData.foodID;
				this.baseParams['pin'] = pin;
				this.baseParams['restaurantID'] = restaurantID;
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
		rowdblclick : function(thiz, ri){
			addTasteHandler(thiz);
		}
	}
}); 

var allTasteGridForTabPanel = new Ext.grid.GridPanel({
	title : '所有口味',
	id : 'allTasteGridForTabPanel',
	trackMouseOver : true,
	frame : true,
	loadMask : { msg: '数据请求中,请稍等......' },
	viewConfig : {
		forceFit : true
	},
	cm : new Ext.grid.ColumnModel([
		new Ext.grid.RowNumberer(),
		{header:'口味名', dataIndex:'tasteName', width:120},
		{header:'价钱', dataIndex:'tastePrice', width:80, align:'right', renderer:Ext.ux.txtFormat.gridDou},
		{header:'', dataIndex:'tasteRate', hidden:true},
		{header:'', dataIndex:'tasteCalcFormat', hidden:true},
		{header:'', dataIndex:'operation', hidden:true}
	]),
	ds : new Ext.data.JsonStore({
		root : 'root',
		fields : ['tasteID', 'tasteAliasID', 'tasteName', 'tastePrice', 'tasteRate', 'tasteCalcFormat']
	}),
	listeners : {
		rowdblclick : function(thiz, ri){
			addTasteHandler(thiz);
		}
	}
}); 

var ggForTabPanel = new Ext.grid.GridPanel({
	title : '规格',
	id : 'ggForTabPanel',
	trackMouseOver : true,
	frame : true,
	loadMask : { msg: '数据请求中,请稍等......' },
	viewConfig : {
		forceFit : true
	},
	cm : new Ext.grid.ColumnModel([
		new Ext.grid.RowNumberer(),
		{header:'规格名', dataIndex:'tasteName', width:120},
		{header:'', dataIndex:'tastePrice', hidden:true},
		{header:'比例', dataIndex:'tasteRate', width:80, align:'right', renderer:Ext.ux.txtFormat.gridDou},
		{header:'', dataIndex:'tasteCalcFormat', hidden:true},
		{header:'', dataIndex:'operation', hidden:true}
	]),
	ds : new Ext.data.JsonStore({
		root : 'root',
		fields : ['tasteID', 'tasteAliasID', 'tasteName', 'tastePrice', 'tasteRate', 'tasteCalcFormat']
	}),
	listeners : {
		rowdblclick : function(thiz, ri){
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
		{header:'口味名', dataIndex:'tasteName', width:130},
		{header:'价钱', dataIndex:'tastePrice', width:70, align:'right', renderer:Ext.ux.txtFormat.gridDou},
		{header:'比例', dataIndex:'tasteRate', width:70, align:'right', renderer:Ext.ux.txtFormat.gridDou},
		{header:'计算方式', dataIndex:'tasteCalcFormat', width:80},
		{header:'操作', align:'center', dataIndex:'operation', width:80, renderer:tasteOperationRenderer}
	]),
	ds : new Ext.data.JsonStore({
		root : 'root',
		fields : ['tasteID', 'tasteAliasID', 'tasteName', 'tastePrice', 'tasteRate', 'tasteCalcFormat']
	})
});

/**
 * 
 */
refreshHaveTasteHandler = function(){
	haveTasteGrid.getStore().removeAll();
	
	var or = Ext.getCmp('orderedGrid').getSelectionModel().getSelections()[0];
	var ht = [], hd = {root:[]};
	
	ht.push(or.get('tasteID'));
	ht.push(or.get('tasteIDTwo'));
	ht.push(or.get('tasteIDThree'));
	
	for(var i = 0; i < ht.length; i++){
		if(ht[i] != null && typeof(ht[i]) != 'undefined'){
			for(var j = 0; j < tasteMenuData.root.length; j++){
				if(eval(tasteMenuData.root[j].tasteAliasID) == ht[i]){
					hd.root.push(tasteMenuData.root[j]);
					break;
				}
			}
		}
	}
	haveTasteGrid.getStore().loadData(hd);
};

var choosenTasteWin = new Ext.Window({
	closable : false,
	modal : true,
	resizable : false,
	layout : 'border',
	width : 650,
	height : 380,
	items : [haveTasteGrid, choosenTasteTabPanel],
	bbar : [
		'->',
		{
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
				var or = Ext.getCmp('orderedGrid').getSelectionModel().getSelections()[0];
				var htgs = haveTasteGrid.getStore();
				var tastePref = '';
				
				for(var i = 0; i < orderedData.root.length; i++){
					if(eval(orderedData.root[i].foodID == or.get('foodID') && orderedData.root[i].status == 2 && orderedData.root[i].status == or.get('status'))){
						if(orderedData.root[i].tasteID == or.get('tasteID') && orderedData.root[i].tasteIDTwo == or.get('tasteIDTwo') && orderedData.root[i].tasteIDThree == or.get('tasteIDThree')){
							if(typeof(htgs.getAt(0)) != 'undefined'){
								orderedData.root[i].tasteID  = htgs.getAt(0).get('tasteAliasID');
								tastePref += (tastePref.length > 0 ? ';' : '');
								tastePref += htgs.getAt(0).get('tasteName');
							}else{
								orderedData.root[i].tasteID = 0;
							}
							if(typeof(htgs.getAt(1)) != 'undefined'){
								orderedData.root[i].tasteIDTwo  = htgs.getAt(1).get('tasteAliasID');
								tastePref += (tastePref.length > 0 ? ';' : '');
								tastePref += htgs.getAt(1).get('tasteName');
							}else{
								orderedData.root[i].tasteIDTwo = 0;
							}
							if(typeof(htgs.getAt(2)) != 'undefined'){
								orderedData.root[i].tasteIDThree = htgs.getAt(2).get('tasteAliasID');
								tastePref += (tastePref.length > 0 ? ';' : '');
								tastePref += htgs.getAt(2).get('tasteName');
							}else{
								orderedData.root[i].tasteIDThree = 0;
							}
							orderedData.root[i].tastePref = tastePref.length > 0 ? tastePref : '无口味 ';
						}
					}
				}
				
				// 合并重复数据
				var tempData = {root:[]};
				for(var i = 0; i < orderedData.root.length; i++){
					if(orderedData.root[i].status == 1){
						tempData.root.push(orderedData.root[i]);
					}else{
						var cs = true;
						for(var j = 0; j < tempData.root.length; j++){
							if(tempData.root[j].status == 2 && tempData.root[j].foodID == orderedData.root[i].foodID && tempData.root[j].status == orderedData.root[i].status){
								if(eval(tempData.root[j].tasteID == orderedData.root[i].tasteID)
										&& eval(tempData.root[j].tasteIDTwo == orderedData.root[i].tasteIDTwo)
										&& eval(tempData.root[j].tasteIDThree == orderedData.root[i].tasteIDThree)){
									cs = false;
									tempData.root[j].count += orderedData.root[i].count;
								}
							}
						}
						
						if(cs){
							tempData.root.push(orderedData.root[i]);
						}
					}
				}
				
				choosenTasteWin.hide();
				
				orderedData.root = tempData.root;
				
				orderedStore.loadData(orderedData);
				
				dishGridRefresh();
				
			}
		}, {
			text : '关闭',
			iconCls : 'btn_close',
			handler : function(){
				choosenTasteWin.hide();
			}
		}
	],
	listeners : {
		render : function(){
			
		},
		show : function(thiz){
			commonTasteGridForTabPanel.getStore().load();
			
			var or = Ext.getCmp('orderedGrid').getSelectionModel().getSelections()[0];
			thiz.setTitle(or.get('foodName'));
			
			refreshHaveTasteHandler();
			
		}
	}
});


dishCountInputWin = new Ext.Window({
	layout : 'fit',
	width : 200,
	height : 90,
	closeAction : 'hide',
	closable : false,
	resizable : false,
	modal : true,
	buttonAlign : 'center',
	items : [ {
		layout : 'form',
		labelWidth : 30,
		border : false,
		frame : true,
		items : [ {
			xtype : 'numberfield',
			fieldLabel : '数量',
			id : 'dishCountInput',
			width : 130
		} ]
	} ],
	buttons : [{
		text : '确定',
		id : 'btnSetFoodCount',
		handler : function() {
			var inputCount = dishCountInputWin.findById('dishCountInput');
			if (inputCount.getValue() != '' && inputCount.getValue() > 0 && inputCount.getValue() < 65535) {
				dishCountInputWin.hide();
				var ds = orderedGrid.getStore().getAt(dishOrderCurrRowIndex_).data;
				for(var i = 0; i < orderedData.root.length; i++){						
					if(ds.foodID == orderedData.root[i].foodID && ds.status == orderedData.root[i].status){							
						if(eval(ds.tasteID == orderedData.root[i].tasteID)
								&& eval(ds.tasteIDTwo == orderedData.root[i].tasteIDTwo)
								&& eval(ds.tasteIDThree == orderedData.root[i].tasteIDThree)){
							orderedData.root[i].count = inputCount.getValue();
							break;
						}
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
	}],
	listeners : {
		show : function(thiz) {
			var f = Ext.get('dishCountInput');
			f.focus.defer(100, f);
			Ext.getCmp('dishCountInput').setValue();
			thiz.center();
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

// 已点菜式
// 2，表格的数据store
var orderedStore = new Ext.data.Store({
	proxy : new Ext.data.MemoryProxy(orderedData),
	reader : new Ext.data.JsonReader(Ext.ux.readConfig,
	    [ 
			{name : 'foodName'},
			{name : 'displayFoodName'}, 
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
			{name : 'recommend'}, 
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
	),
	listeners : {
		load : function(thiz, records){
			for(var i = 0; i < records.length; i++){
				Ext.ux.formatFoodName(records[i], 'displayFoodName', 'foodName');
			}
		}
	}
});

//orderedStore.reload();
// 底色处理，已点菜式原色底色
//dishGridRefresh();

var dishPushBackWin = new Ext.Window({
	layout : 'fit',
	width : 220,
	height : 120,
	closeAction : 'hide',
	resizable : false,
	items : [{
		layout : 'form',
		labelWidth : 60,
		border : false,
		frame : true,
		items : [{
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
		}]
	}],
	buttons : [{
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
								if(ds.aliasID == orderedData.root[i].aliasID){
									if(eval(ds.tasteID == orderedData.root[i].tasteID)
											&& eval(ds.tasteIDTwo == orderedData.root[i].tasteIDTwo)
											&& eval(ds.tasteIDThree == orderedData.root[i].tasteIDThree)){
										
										if((orderedData.root[i].count - pushCount) <= 0){
											orderedData.root.splice(i,1);
										}else if((orderedData.root[i].count - pushCount)> 0){
											orderedData.root[i].count -= pushCount;
										}
										break;
									}
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
	}, {
		text : '取消',
		handler : function() {
			dishPushBackWin.hide();
			dishPushBackWin.findById('dishPushBackPwd').setValue('');
		}
	}],
	listeners : {
		show : function(thiz) {
			var f = Ext.get('dishPushBackPwd');
			f.focus.defer(100, f); 
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
								if(eval(ds.tasteID == orderedData.root[i].tasteID)
										&& eval(ds.tasteIDTwo == orderedData.root[i].tasteIDTwo)
										&& eval(ds.tasteIDThree == orderedData.root[i].tasteIDThree)){
									orderedData.root.splice(i,1);
									break;
								}
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
		
	}
};

function dishOptDispley(value, cellmeta, record, rowIndex, columnIndex, store) {
	return ''
			+ '<a id="tasteLink' + rowIndex + '" href="javascript:dishOptTasteHandler(' + rowIndex + ')">'
			+ '口味</a>'
			+ '&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;'
			+ '<a href="javascript:dishOptDeleteHandler(' + rowIndex + ')">'
			+ (record.get('status') == 1 ? '退菜' : '删除') + '</a>'
			+ '&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;'
			+ '<a href="javascript:dishOptPressHandler(' + rowIndex + ')">'
			+ '催菜</a>'
			+ '';
};

function dishWaiterDispley(value, cellmeta, record, rowIndex, columnIndex, store) {
	return Ext.getDom('optName').innerHTML;
}

var orderedColumnModel = new Ext.grid.ColumnModel([
    new Ext.grid.RowNumberer(),
    {
    	header : '菜名',
		dataIndex : 'displayFoodName',
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
		width : 150
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
	}
]);

// 4，表格
var tasteChooseImgBut = new Ext.ux.ImageButton({
	imgPath : '../../images/Taste.png',
	imgWidth : 50,
	imgHeight : 50,
	tooltip : '口味',
	handler : function(btn) {
		if(orderedGrid.getSelectionModel().getSelections().length != 1){
			Ext.example.msg('提示', '请选中一条数据再进行操作.');
			return;
		}
		
		dishOptTasteHandler(dishOrderCurrRowIndex_);
	}
});

var dishDeleteImgBut = new Ext.ux.ImageButton({
	imgPath : '../../images/DeleteDish.png',
	imgWidth : 50,
	imgHeight : 50,
	tooltip : '删除',
	handler : function(btn) {
		if(orderedGrid.getSelectionModel().getSelections().length != 1){
			Ext.example.msg('提示', '请选中一条数据再进行操作.');
			return;
		}
		
		dishOptDeleteHandler(dishOrderCurrRowIndex_);
	}
});
var dishPressImgBut = new Ext.ux.ImageButton({
	imgPath : '../../images/HurryFood.png',
	imgWidth : 50,
	imgHeight : 50,
	tooltip : '催菜',
	handler : function(btn) {
		Ext.example.msg('提示', '<font color="red">感谢您的使用!此功能正在开发中,请关注系统升级.</font>');
		return;
		
		if(orderedGrid.getSelectionModel().getSelections().length != 1){
			Ext.example.msg('提示', '请选中一条数据再进行操作.');
			return;
		}
		
		dishOptPressHandler(dishOrderCurrRowIndex_);
	}
});
var countAddImgBut = new Ext.ux.ImageButton({
	imgPath : '../../images/AddCount.png',
	imgWidth : 50,
	imgHeight : 50,
	tooltip : '数量加1',
	handler : function(btn) {
		if(orderedGrid.getSelectionModel().getSelections().length != 1){
			Ext.example.msg('提示', '请选中一条数据再进行操作.');
			return;
		}
		
		if (dishOrderCurrRowIndex_ != -1) {
			var ds = orderedGrid.getStore().getAt(dishOrderCurrRowIndex_).data;		
			if (ds.status == 2) {									
				for(var i = 0; i < orderedData.root.length; i++){						
					if(ds.foodID == orderedData.root[i].foodID && orderedData.root[i].status == 2){
						if(eval(ds.tasteID == orderedData.root[i].tasteID)
								&& eval(ds.tasteIDTwo == orderedData.root[i].tasteIDTwo)
								&& eval(ds.tasteIDThree == orderedData.root[i].tasteIDThree)){
							orderedData.root[i].count += 1;
							break;
						}
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

var countMinusImgBut = new Ext.ux.ImageButton({
	imgPath : '../../images/MinusCount.png',
	imgWidth : 50,
	imgHeight : 50,
	tooltip : '数量减1',
	handler : function(btn) {
		if(orderedGrid.getSelectionModel().getSelections().length != 1){
			Ext.example.msg('提示', '请选中一条数据再进行操作.');
			return;
		}
		
		if (dishOrderCurrRowIndex_ != -1) {
			var ds = orderedGrid.getStore().getAt(dishOrderCurrRowIndex_).data;		
			if (ds.status == 2) {
				if (ds.count > 1) {
					for(var i = 0; i < orderedData.root.length; i++){						
						if(ds.foodID == orderedData.root[i].foodID && orderedData.root[i].status == 2){							
							if(eval(ds.tasteID == orderedData.root[i].tasteID)
									&& eval(ds.tasteIDTwo == orderedData.root[i].tasteIDTwo)
									&& eval(ds.tasteIDThree == orderedData.root[i].tasteIDThree)){
								orderedData.root[i].count -= 1;
								break;
							}
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
		if(orderedGrid.getSelectionModel().getSelections().length != 1){
			Ext.example.msg('提示', '请选中一条数据再进行操作.');
			return;
		}
		
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
	handler : function(btn){
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

var btnSubmitOrder = new Ext.Button({
	text : '提交',
	listeners : {
		render : function(thiz){
			thiz.getEl().setWidth(80, true);
		}
	},
	handler : function() {
		submitOrderHandler({
			href : 'TableSelect.html?pin=' + Request['pin'] + '&restaurantID=' + restaurantID
		});
	}
});

var btnSubmitAndCheckoutOrder = new Ext.Button({
	text : '提交&结帐',
	handler : function() {		
		submitOrderHandler({
			href : 'CheckOut.html?tableNbr=' + Request['tableNbr'] + '&personCount=' + 1 
					+ '&pin=' + Request['pin'] + '&restaurantID=' + restaurantID + '&minCost=' 
					+ Request['minCost'] + '&serviceRate=' + Request['serviceRate']
		});
	}
});

var orderedGrid = new Ext.grid.GridPanel({
	title : '已点菜',
	id : 'orderedGrid',
	xtype : 'grid',
	region : 'center',
	margins : '5 5 0 0',
	frame : true,
	buttonAlign : 'center',
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
	buttons : [
	    btnSubmitOrder, 
	    btnSubmitAndCheckoutOrder,
	    {
	    	text : '刷新',
	    	handler : function(){
	    		refreshOrderHandler();
	    	}
	    }, {
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
								location.href = 'TableSelect.html?pin=' + Request['pin'] + '&restaurantID=' + restaurantID;
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
		render : function(thiz) {
			orderedDishesOnLoad();
			tableStuLoad();
		}
	}
});

var dishesOrderNorthPanel = new Ext.Panel({
	id : 'dishesOrderNorthPanel',
	region : 'north',
	height : 40,
	border : false,
	layout : 'form',
	frame : true,
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
	} ]
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
	['displayFoodName', 'foodName', 'foodAliasID', 'foodID', 'pinyin', 'unitPrice', 'stop', 'special', 'recommend', 'gift', 'currPrice', 'combination', 'kitchen.kitchenID'],
	[['pin',pin], ['type', 1], ['restaurantID', restaurantID], ['isPaging', true]],
	30,
	'',
	allFoodTabPanelGridTbar
);
allFoodTabPanelGrid.getBottomToolbar().displayMsg = '每页&nbsp;30&nbsp;条,共&nbsp;{2}&nbsp;条记录';
allFoodTabPanelGrid.getStore().on('beforeload', function(thiz, records){
	var searchType = Ext.getCmp('comSearchType');
	var searchValue = Ext.getCmp(orderMainObject.searchField);
	this.baseParams['searchType'] = typeof(searchType) != 'undefined' ? searchType.getValue() : '';
	this.baseParams['searchValue'] = typeof(searchValue) != 'undefined' ? searchValue.getValue() : '';
});
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
				if (orderedData.root[i].foodID == r.get('foodID') && orderedData.root[i].status == 2) {
					if(orderedData.root[i].tasteID == 0 && orderedData.root[i].tasteIDTwo == 0 && orderedData.root[i].tasteIDThree == 0){
						orderedData.root[i].count += 1;
						isAlreadyOrderd = false;
						break;
					}
				}
			}
		}
//		alert('isAlreadyOrderd: '+isAlreadyOrderd)
		if (isAlreadyOrderd) {
			orderedData.root.push({
				aliasID : r.get('foodAliasID'),
				foodName : r.get('foodName'),
				tastePref : '无口味',
				count : 1,
				unitPrice : r.get('unitPrice'),
				acturalPrice : r.get('unitPrice'),
				orderDateFormat : new Date().format('Y-m-d H:i:s'),
				waiter : Ext.getDom('optName').innerHTML,
				foodID : r.get('foodID'),
				aliasID : r.get('foodAliasID'),
				kitchenId : r.get('kitchen.kitchenID'),
				special : r.get('special'),
				recommend : r.get('recommend'),
				soldout : r.get('stop'),
				gift : r.get('gift'),
				tastePrice : 0,
				tasteID : 0,
				tasteIDTwo : 0,
				tasteIDThree : 0,
				status : 2,
				currPrice : r.get('currPrice'),
				temporary : false,
				tmpTaste : false,						
				tmpTastePref : '',
				tmpTastePrice : 0,
				tmpTasteAlias : 0,
				hangStatus : 0
			});
		}
		
		orderedStore.loadData(orderedData);				
		
		dishGridRefresh();
	}
	
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
	layout : 'column',
	defaults : {
		xtype : 'panel',
		layout : 'form',
		frame : true,
		labelWidth : 45,
		style : 'padding:10 10 0 10;'
	},
	items : [{
		columnWidth : .5,
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
		columnWidth : .5,
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
		columnWidth : .5,
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
	}],
	tbar : [
	    '->',
	    {
	    	text : '添加',
	    	iconCls : 'sss',
	    	handler : function(e){
	    		var name = Ext.getCmp('txtTempFoodName');
	    		var count = Ext.getCmp('numTempFoodCount');
	    		var price = Ext.getCmp('numTempFoodPrice');
	    		
	    		if(!name.isValid() || !count.isValid() || !price.isValid()){
	    			return;
	    		}
	    		
	    		orderedData.root.push({
					foodName : name.getValue().replace(/,/g,';').replace(/，/g,';'),
					tastePref : '无口味',
					count : count.getValue(),
					unitPrice : price.getValue(),
					acturalPrice : price.getValue(),
					orderDateFormat : (new Date().format('Y-m-d H:i:s')),
					waiter : Ext.getDom('optName').innerHTML,
					foodID : (new Date().format('His')),
					aliasID  : (new Date().format('His')),
					kitchenID : 0,
					special : false,
					recommend : false,
					soldout : false,
					gift : false,
					tastePrice : 0,
					tasteID : 0,
					tasteIDTwo : 0,
					tasteIDThree : 0,
					status : 2,
					currPrice : false,
					temporary : true,
					tmpFoodName : name.getValue(),
					tmpTasteAlias : 0,
					hangStatus : 0
				});		
	    		
	    		orderedStore.loadData(orderedData);
	    		
	    		dishGridRefresh();
	    		
	    		name.setValue();
	    		count.setValue(1);
	    		price.setValue(0);
	    		
	    		name.clearInvalid();
	    		count.clearInvalid();
	    		price.clearInvalid();
	    	}
	    }
	]
});

var dishesOrderEastPanel;
var centerPanel;

Ext.onReady(function() {
	// 解决ext中文传入后台变问号问题
	Ext.lib.Ajax.defaultPostHeader += '; charset=utf-8';
	Ext.QuickTips.init();

	// ------------------------------- 菜譜card
	var menuTabPanel = new Ext.TabPanel({
		id : 'menuTabPanel',
		activeItem : 0,
//		items : [allFoodTabPanel, tempFoodTabPanel],
		items : [allFoodTabPanel],
		listeners : {
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
		title : ' 菜单 ',
		layout : 'fit',
		margins : '5 0 0 0',
		items : [menuTabPanel]
	});

	centerPanel = new Ext.Panel({
		title : '&nbsp;',
		id : 'centerPanel',
		region : 'center',
		border : false,
		frame : true,
		margins : '0 0 0 0',
		layout : 'border',
		items : [ orderedGrid, dishesOrderEastPanel, dishesOrderNorthPanel ],
		listeners : {
			render : function(){
				tasteOnLoad();
			}
		}
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
			centerPanel,
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

/**
 * 刷新账单信息 
 */
refreshOrderHandler = function(){
	var girdData = orderedData.root;
	var selData = new Array();
	
	var refresh = new Ext.LoadMask(document.body, {
	    msg  : '正在更新已点菜列表,请稍等......',
	    disabled : false,
	    removeMask : true
	});

	for(var i = (girdData.length - 1); i >= 0; i--){
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
				
				for(var i = (selData.length - 1); i >= 0 ; i--){
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
};


/**
 * 根据返回做错误码作相关操作
 */
refreshOrder = function(res){
	
	var href = 'TableSelect.html?pin=' + Request['pin'] + '&restaurantID=' + restaurantID;
	
	if(eval(res.code == 14)){
		Ext.MessageBox.confirm('警告', '账单信息已更新,是否刷新已点菜并继续操作?否则返回.', function(btn){
			if(btn == 'yes'){
				refreshOrderHandler();
			}else{
				location.href = href;
			}
		},this);
	}else if(eval(res.code == 3)){
		var interval = 3;
		var action = '<br/>点击确定返回或&nbsp;<span id="returnInterval" style="color:red;"></span>&nbsp;之后自动跳转.';
		new Ext.util.TaskRunner().start({
			run: function(){
				if(interval < 1){
					location.href = href;
				}
				Ext.getDom('returnInterval').innerHTML = interval;
				interval--;
		    },
		    interval : 1000
		});
		Ext.MessageBox.show({
			title : res.title,
			msg : ('<center>' + res.msg + '.' + action + '</center>'),
			width : 300,
			buttons : Ext.MessageBox.OK,
			fn : function(){
				if(submitType != 6){
					location.href = "TableSelect.html?pin=" + Request["pin"] + "&restaurantID=" + restaurantID;
				}
			}
		});
	}else{
		Ext.ux.showMsg(res);
	}
};

/**
 * 提交账单信息
 */
submitOrderHandler = function(c){
	if(orderedData.root.length > 0){
		var inputPersCount = Ext.getCmp('tablePersonCount').getValue();
		inputPersCount = inputPersCount == '' || inputPersCount == 0 ? 1 : inputPersCount;
		var foodPara = '';
		for ( var i = 0; i < orderedData.root.length; i++) {
			foodPara += ( i > 0 ? '，' : '');
			if (orderedData.root[i].temporary == false) {
				// [是否临时菜(false),菜品1编号,菜品1数量,口味1编号,厨房1编号,菜品1折扣,2nd口味1编号,3rd口味1编号]，
				foodPara = foodPara 
						+ '[false,'// 是否临时菜(false)
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
						+ orderedData.root[i].hangStatus + ','  // 菜品状态
						+ orderedData.root[i].status  // 菜品操作状态 1:已点菜 2:新点菜 3:反结账
						+ ']';
			} else {
				var foodname = orderedData.root[i].foodName;
				foodname = foodname.indexOf('<') > 0 ? foodname.substring(0,foodname.indexOf('<')) : foodname;
				// [是否临时菜(true),临时菜1编号,临时菜1名称,临时菜1数量,临时菜1单价]
				foodPara = foodPara 
						+ '[true,'// 是否临时菜(true)
						+ orderedData.root[i].aliasID + ',' // 临时菜1编号
						+ foodname + ',' // 临时菜1名称
						+ orderedData.root[i].count + ',' // 临时菜1数量
						+ orderedData.root[i].unitPrice + ',' // 临时菜1单价(原料單價)
						+ orderedData.root[i].hangStatus + ','  // 菜品状态
						+ orderedData.root[i].status  // 菜品操作状态 1:已点菜 2:新点菜 3:反结账
						+ ']';
			}									
		}	
		
		foodPara = '{' + foodPara + '}';
		
		var type = 9;
		if(Request['tableStat'] == 'free'){
			type = 1;
		}else{
			type = 2;
		}
		
		orderedGrid.buttons[0].setDisabled(true);
		orderedGrid.buttons[1].setDisabled(true);
		orderedGrid.buttons[2].setDisabled(true);
		orderedGrid.buttons[3].setDisabled(true);
		
		Ext.Ajax.request({
			url : '../../InsertOrder.do',
			params : {
				'pin' : Request['pin'],
				'tableID' : Request['tableNbr'],
				'tableID_2' : Request['tableNbr2'],
				'customNum' : inputPersCount,
				'type' : type,
				'originalTableID' : Request['tableNbr'],
				'foods' : foodPara,
				'category' : category,
				'orderDate' : typeof(orderedData.other) == 'undefined' || typeof(orderedData.other.order) == 'undefined' ? '' : orderedData.other.order.orderDate
			},
			success : function(response, options) {
				var rj = Ext.util.JSON.decode(response.responseText);
				if (rj.success == true) {
					var interval = 3;
					var action = '&nbsp;<span id="returnInterval" style="color:red;"></span>&nbsp;之后自动跳转.';
					new Ext.util.TaskRunner().start({
						run: function(){
							if(interval < 1){
								if(typeof(c.href) != 'undefined'){
									location.href = c.href;								
								}								
							}
							Ext.getDom('returnInterval').innerHTML = interval;
							interval--;
					    },
					    interval : 1000
					});
					
					Ext.MessageBox.show({
						msg : (rj.msg + action),
						width : 300,
						buttons : Ext.MessageBox.OK,
						fn : function() {
							if(typeof(c.href) != 'undefined'){
								location.href = c.href;								
							}
						}
					});
				} else {
					refreshOrder(rj);
					
					orderedGrid.buttons[0].setDisabled(false);
					orderedGrid.buttons[1].setDisabled(false);
					orderedGrid.buttons[2].setDisabled(false);
					orderedGrid.buttons[3].setDisabled(false);
				}
			},
			failure : function(response, options) {
				orderedGrid.buttons[0].setDisabled(false);
				orderedGrid.buttons[1].setDisabled(false);
				orderedGrid.buttons[2].setDisabled(false);
				orderedGrid.buttons[3].setDisabled(false);
				Ext.ux.showMsg(Ext.util.JSON.decode(response.responseText));
			}
		});
	}else if(orderedData.root.length == 0){
		Ext.MessageBox.show({
			msg : '还没有选择任何菜品，暂时不能提交',
			width : 300,
			buttons : Ext.MessageBox.OK
		});
	}
};
