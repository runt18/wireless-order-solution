var addSupplier = Ext.getCmp('supplier_addSupplier');
if(!addSupplier){
	addSupplier = new Ext.Window({
		id : 'supplier_addSupplier',
		title : '添加供应商',
		closable : false, //是否可关闭
		resizable : false, //大小调整
		modal : true,
		width : 260,
		bbar : [{
				xtype : 'tbtext',
				text : '  '
			},'->',{
				text : '保存',
				id : 'btnSaveSupplier',
				iconCls : 'btn_save',
				handler : function(e){
					var sId = Ext.getCmp('txtSId').getValue();
					var sName = Ext.getCmp('txtSName').getValue();
					var sTele = Ext.getCmp('txtSTele').getValue();
					var sAddr = Ext.getCmp('txtSAddr').getValue();
					var sContact = Ext.getCmp('txtSContact').getValue();
					var sComment = Ext.getCmp('txtSComment').getValue();
					
					var actionUrl = '';
					if(!Ext.getCmp('txtSName').isValid()){
						return;
					}
					if(addSupplier.operationType == 'insert'){
						
						actionUrl = '../../InsertSupplier.do';
					}
					else if(addSupplier.operationType == 'update'){
						actionUrl = '../../UpdateSupplier.do';
					}
					else return;
					
					Ext.Ajax.request({
						url : actionUrl,
						params : {
							supplierID : sId,
							supplierName : sName,
							tele : sTele,
							addr : sAddr,
							contact : sContact,
							comment : sComment
						},
						success : function(res, opt){
							Ext.getCmp('supplier_grid').store.reload();
							var jr = Ext.util.JSON.decode(res.responseText);
							if(jr.success){
								if(addSupplier.operationType == 'insert'){
									supplierOperactionHandler({type : 'insert'});									
								}else{
									addSupplier.hide();
								}
								Ext.example.msg(jr.title, jr.msg);
							}else{
								Ext.ux.showMsg(jr);
							}
							
						},
						failure : function(res, opt){
							Ext.ux.showMsg(Ext.util.JSON.decode(res.responseText));
						}
						
					});
				} 
			},{
				text : '关闭',
				id : 'btnCloseSupplier',
				iconCls : 'btn_close',
				handler : function(e){
					addSupplier.hide();
				}
		}],
		items : [{
			layout : 'form',
			frame : true,
			border : true,
			labelWidth : 68,
			height : Ext.isIE ? 150 : null,
			items : [{
				xtype : 'hidden',
				id : 'txtSId',
				hideLabel : true,
				hidden : true
			},{
				xtype : 'textfield',
				id : 'txtSName',
				width : 130,
				fieldLabel : '供应商名称',
				allowBlank : false
			},{
				xtype : 'textfield',
				id : 'txtSTele',
				width : 130,
				fieldLabel : '联系方式',
				regex : Ext.ux.RegText.phone.reg,
				regexText : Ext.ux.RegText.phone.error
				
			},{
				xtype : 'textfield',
				id : 'txtSContact',
				width : 130,
				fieldLabel : '联系人'
			},{
				xtype : 'textfield',
				id : 'txtSAddr',
				width : 130,
				fieldLabel : '地址'
			},{
				xtype : 'textfield',
				id : 'txtSComment',
				width : 130,
				fieldLabel : '备注'
/*				validator : function(v){
					if(Ext.util.Format.trim(v).length > 0 ){
						return true;
					}else{
						return '写上评价';
					}
				} */
			}]
	
		}],
		keys : [{
			key : Ext.EventObject.ENTER,
			scope : this,
			fn : function(){
				Ext.getCmp("btnSaveSupplier").handler();
			}
		}]
			
	});
}




var filterTypeDate = [[0,'全部'],[1,'供应商名称'],[2,'联系电话'],[3,'联系人']];
var filterComb = new Ext.form.ComboBox({
	fidldLabel : '过滤',
	forceSelection : true,
	width : 100,
	value : '全部',
	id : 'comboFilter',
	store : new Ext.data.SimpleStore({
		fields : [ 'value', 'text' ],
		data : filterTypeDate
	}),
	valueField : 'value',
	displayField : 'text',
	typeAhead : true,
	mode : 'local',
	triggerAction : 'all',
	selectOnFocus : true,
	allowBlank : false,
	readOnly : false,
	listeners : {
		select : function(combo, record, index){
			var fn = Ext.getCmp('findName');
			var ft = Ext.getCmp('findTele');
			var fc = Ext.getCmp('findContact');
			fn.setValue('');
			ft.setValue('');
			fc.setValue('');
			if(index == 0){
				fn.setVisible(false);
				ft.setVisible(false);
				fc.setVisible(false);
			}else if (index == 1){
				fn.setVisible(true);
				ft.setVisible(false);
				fc.setVisible(false);
				
			}else if(index == 2){
				fn.setVisible(false);
				ft.setVisible(true);
				fc.setVisible(false);
			}else{
				fn.setVisible(false);
				ft.setVisible(false);
				fc.setVisible(true);
			};
		}
	}
	
});


supplierOperactionHandler = function(c){
	if(c.type == 'undefined'){
		return;
	}
	
	var sId = Ext.getCmp('txtSId');
	var sName = Ext.getCmp('txtSName');
	var sTele = Ext.getCmp('txtSTele');
	var sAddr = Ext.getCmp('txtSAddr');
	var sContact = Ext.getCmp('txtSContact');
	var sComment = Ext.getCmp('txtSComment');
	
	if(c.type == 'insert'){
		addSupplier.setTitle('添加供应商');
		sId.setValue('');
		sName.setValue('');
		sTele.setValue('');
		sAddr.setValue('');
		sContact.setValue('');
		sComment.setValue('');
		
		sContact.clearInvalid();
		sName.clearInvalid();
	}
	else if(c.type == 'update'){
		var sn = Ext.getCmp('supplier_grid').getSelectionModel().getSelected(); 
		if(sn == null){
			Ext.MessageBox.alert('提示', '请选中一个供应商再进行操作.');
			return;
		}		
		addSupplier.setTitle('设置供应商信息');
		sId.setValue(sn.data.supplierID);
		sName.setValue(sn.data.name);
		sTele.setValue(sn.data.tele);
		sAddr.setValue(sn.data.addr);
		sContact.setValue(sn.data.contact);
		sComment.setValue(sn.data.comment);
	}
	addSupplier.operationType = c.type;
	//addSupplier.center();
	addSupplier.show();
	
	sName.focus(true, 100);
};

deleteSupplierOperationHandler = function(){
	var sd = Ext.getCmp('supplier_grid').getSelectionModel().getSelected();
/*	var sd = Ext.getCmp('grid').selModel.selections;
	Ext.MessageBox.alert('sd',sd.items[0].data.supplierID);*/
	if(sd != null){
		Ext.Msg.confirm(
				'提示',
				'是否删除方案 ?',
				function(e){
					if(e == 'yes'){
						Ext.Ajax.request({
							url : '../../DeleteSupplier.do',
							params : {
								supplierId : sd.data.supplierID
							},
							success : function(res, opt){
								Ext.getCmp('supplier_grid').store.reload();
								var jr = Ext.util.JSON.decode(res.responseText);
								if(jr.success){
									Ext.example.msg(jr.title, jr.msg);
									
								}else{
									Ext.ux.showMsg(jr);
								}
							},
							failure : function(res, opt){
								Ext.ux.showMsg(Ext.util.JSON.decode(res.responseText));
							}
						});
					}
				}
				
		);

	}else{
		Ext.MessageBox.alert('提示','请选中一个供应商再进行操作!');
		return;
	}
	
};

Ext.onReady(function(){

	//设置列默认值
	supplierOpt = function(){
		return "<a href = \"javascript:supplierOperactionHandler({type:'update'})\">" + "<img src='../../images/Modify.png'/>修改</a>"
			 +"&nbsp;&nbsp;"
			 + "<a href=\"javascript:void(0);\" onclick=\"deleteSupplierOperationHandler()\">" + "<img src='../../images/del.png'/>删除</a>";
	};	
	
	//定义列模型
	var cm = new Ext.grid.ColumnModel([
	       new Ext.grid.RowNumberer(),
		   {header:'供应商名称',dataIndex:'name'},
		   {header:'联系方式',dataIndex:'tele'},
		   {header:'地址',dataIndex:'addr'},
		   {header:'联系人',dataIndex:'contact'},
		   {header:'备注',dataIndex:'comment'},
		   {header:'操作',align:'center',dataIndex:'supplierOpt',renderer : supplierOpt}
	       ]);
	  	cm.defaultSortable = true;
	                               	

	
	//数据加载器
	var ds = new Ext.data.Store({
		//高度封装
/*		url : 'json.js',
		data : {'rows' : {'supplierID' : 2, 'name' : 'aaa', 'tele' : '1235', 'addr' : 'dddddd', 'contact' : 'xili', 'comment' : 'good'}},
		root : 'rows',
		autoLoad : true,
		fields : ['supplierID', 'name', 'tele', 'addr', 'contact', 'comment']*/
		//原始写法
		//autoLoad : true,
		//代理,加载本地和远程
		//proxy : new Ext.data.MemoryProxy(data),
		//加载远程的
		proxy : new Ext.data.HttpProxy({url:'../../QuerySupplier.do'}),
		reader : new Ext.data.JsonReader({totalProperty:'totalProperty', root:'root'},[
			{name : 'supplierID'},
	        {name : 'name'},
	        {name : 'tele'},
	        {name : 'addr'},
	        {name : 'contact'},
	        {name : 'comment'},
	        {name : 'supplierOpt'}
		])
	});
	//ds.load({params:{start:0,limit:2}});

	

	
	

	var suppllierGridTbar = new Ext.Toolbar({
		items : [
		{ xtype:'tbtext', text:'过滤:'},
		{xtype:'tbtext', text:'&nbsp;&nbsp;'},
		filterComb,
		{xtype:'tbtext', text:'&nbsp;&nbsp;'},
		{
	    	xtype : 'textfield',
	    	id : 'findName',
	    	hideLabel : true,
	    	width : 120,
	    	hidden : true
	    },
		{
	    	xtype : 'textfield',
	    	id : 'findTele',
	    	hideLabel : true,
	    	width : 120,
	    	hidden : true
	    },
		{
	    	xtype : 'textfield',
	    	id : 'findContact',
	    	hideLabel : true,
	    	width : 120,
	    	hidden : true
	    },
	    '->', {
			text : '刷新',
			id : 'btnSearch',
			iconCls : 'btn_refresh',
			handler : function(){
				var sgs = supplierGrid.getStore();
				sgs.baseParams['name'] = Ext.getCmp('findName').getValue();
				sgs.baseParams['tele'] = Ext.getCmp('findTele').getValue();
				sgs.baseParams['contact'] = Ext.getCmp('findContact').getValue();
				sgs.baseParams['op'] = "e";
				//load两种加载方式,远程和本地
				sgs.load({
					params : {
						start : 0,
						limit : 10
					}
				});
			}
		},{
			text : '添加',
			iconCls : 'btn_add',
			handler : function(){
				supplierOperactionHandler({type : 'insert'});
			}
		}, {
			text : '修改',
			iconCls : 'btn_edit',
			handler : function(){
				var sd = Ext.getCmp('supplier_grid').getSelectionModel().getSelected;
				if(sd != null){
					supplierOperactionHandler({type:'update'});
					
				}else{
					Ext.MessageBox.alert('提示','请选择一个供应商再操作!');
				}
			}
		}, {
			text : '删除',
			iconCls : 'btn_delete',
			handler : function(){
				deleteSupplierOperationHandler();
			}
		}]
	});

	var pagingBar = new Ext.PagingToolbar({
	   pageSize : 10,	//显示记录条数
	   store : ds,	//定义数据源
	   displayInfo : true,	//是否显示提示信息
	   displayMsg : "显示第 {0} 条到 {1} 条记录，共 {2} 条",
	   emptyMsg : "没有记录"
	});

	var supplierGrid = new Ext.grid.GridPanel({
			title : '供应商列表',
			id : 'supplier_grid',
		    //height : '500',
		    border : true,
		    frame : true,
		    store : ds,
		    cm : cm,
		    viewConfig : {
		    	forceFit : true
		    },
		    tbar : suppllierGridTbar,
		    bbar : pagingBar
		});
	ds.load({params:{start:0,limit:10}});

	supplierGrid.region = 'center';
	
	new Ext.Panel({
		renderTo : 'divSupplier',//渲染到
		id : 'supplierPanel',
		//solve不跟随窗口的变化而变化
		width : parseInt(Ext.getDom('divSupplier').parentElement.style.width.replace(/px/g,'')),
		height : parseInt(Ext.getDom('divSupplier').parentElement.style.height.replace(/px/g,'')),
		layout:'border',
		frame : true, //边框
		//子集
		items : [supplierGrid],
		keys : [{
			key : Ext.EventObject.ENTER,
			scope : this,
			fn : function(){
				Ext.getCmp('btnSearch').handler();
			}
		}]
	});

});






