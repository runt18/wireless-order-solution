tasteCalcRenderer = function(val, metadata, record){
	var val = parseInt(record.get('tasteCalc'));
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
	var ctg = Ext.getCmp('commonTasteGrid');
	ctg.getStore().remove(ctg.getSelectionModel().getSelections()[0]);
	ctg.getView().refresh();
};

var commonTasteGridTbar = new Ext.Toolbar({
	height : 26,
	items : [
	    {xtype:'tbtext', text:'&nbsp;关联方式:&nbsp;'},
	    {
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
	    					Ext.getCmp('commonTasteGrid').getColumnModel().setColumnWidth(6, 80);
	    					Ext.getCmp('commonTasteGrid').getColumnModel().setHidden(6, true);
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
	    					Ext.getCmp('commonTasteGrid').getColumnModel().setHidden(6, false);
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
	    }
	]
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
	cm : new Ext.grid.ColumnModel(
		[
		 	new Ext.grid.RowNumberer(),
			{header:'口味名', dataIndex:'tasteName', width:100},
			{
				header : '等级', 
				dataIndex : 'rank', 
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
			{header:'价钱', dataIndex:'tastePrice', width:60, renderer:Ext.ux.txtFormat.gridDou},
			{header:'比例', dataIndex:'tasteRate', width:60, renderer:Ext.ux.txtFormat.gridDou},
			{header:'计算方式', dataIndex:'tasteCalc', renderer:tasteCalcRenderer},
			{header:'操作', align:'center', renderer:tasteOperationRenderer}
		]
	),
	ds : new Ext.data.JsonStore({
		url : '../../QueryFoodTaste.do',
		root : 'root',		
		fields : ['tasteID', 'tasteAliasID','tasteName','tastePrice','tasteRate', 'tasteCategory', 'tasteCalc', 'tasteCalcFormat', 'foodID', 'foodName', 'rank'],
		listeners : {
			beforeload : function(){
				var selData = Ext.ux.getSelData('menuMgrGrid');
				this.baseParams['foodID'] = selData.foodID;
				this.baseParams['pin'] = pin;
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
	items : [
	    {
	    	xtype:'tbtext', text:'&nbsp;口味名搜索:&nbsp;'
	    },
	    {
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
	    					if(store.getAt(i).data.tasteName.indexOf(txtTasteName) >= 0 ){
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
	    }
	]
});

var allTasteGrid = createGridPanel(
	'allTasteGrid',
	'<center>所有口味</center>',
	'',
	'',
	'../../QueryTaste.do',
	[
	    [true, false, false, false], 
	    ['口味名', 'tasteName', 100] , 
	    ['价钱', 'tastePrice', 60, 'right', 'Ext.ux.txtFormat.gridDou'], 
	    ['比例', 'tasteRate', 60, 'right', 'Ext.ux.txtFormat.gridDou'], 
	    ['计算方式', 'tasteCalc', '', '', 'tasteCalcRenderer']
	],
	['tasteID', 'tasteAlias','tasteName','tastePrice','tasteRate', 'tasteCategory', 'tasteCalc'],
	[['pin',pin], ['type',0], ['isCombo',false], ['isPaging',false]],
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
	var ctg = Ext.getCmp('commonTasteGrid');
	var sr = thiz.getSelectionModel().getSelections()[0];
	var cv = true;
	ctg.getStore().each(function(r){
		if(r.get('tasteID') == sr.get('tasteID')){
			cv = false;
		}
	});
	if(cv){
		sr.set('rank', 0);
		ctg.getStore().insert(0, sr);
		ctg.getView().refresh();
		ctg.getSelectionModel().selectFirstRow();
	}else{
		Ext.example.msg('提示', '该菜品已关联口味<'+sr.get('tasteName')+'>');
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
	    { xtype:'panel', columnWidth:.01, html:'&nbsp;'},	    allTasteGrid
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
	
	var foodID = c.data.foodID;
	var tasteContent = '';
	var ctg = Ext.getCmp('commonTasteGrid').getStore();
	
	if(mmObj.rdoTasteType == c.data.tasteRefType && mmObj.rdoTasteType == 1){
		Ext.example.msg('提示', '智能关联方式无需修改!');
		ctg.load();
		return;
	}
	
	for(var i = 0; i < ctg.getCount(); i++){
		tasteContent += (i > 0 ? '<split>' : '');
		tasteContent += (ctg.getAt(i).get('tasteID') + ',' + ctg.getAt(i).get('rank'));
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

