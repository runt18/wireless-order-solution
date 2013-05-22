var addSupplier;

addSupplier = new Ext.Window({
	title : '添加供应商',
	closable : true, //是否可关闭
	resizable : true, //大小调整
	modal : true,
	width : 260,
	items : [{
		xtype : 'form',
		layout : 'form',
		frame : true,
		labelWidth : 65,
		items : [{
			xtype : 'hidden',
			id : 'sId'
		},{
			xtype : 'textfield',
			id : 'sName',
			width : 130,
			fieldLabel : '供应商名称',
			allowBlank : false,
			blankText : '供应商不允许为空'
/*			validator : function(v){
				if(Ext.util.Format.trim(v).length > 0){
					return true;
				}else{
					return '供应商不允许为空';
				}
			}*/
		},{
			xtype : 'textfield',
			id : 'sTele',
			width : 130,
			fieldLabel : '联系方式',
			allowBlank : false,
			validator : function(v){
				if(Ext.util.Format.trim(v).length > 0){
					return true;
				}else{
					return '联系方式不允许为空';
				}
			}
			
		},{
			xtype : 'textfield',
			id : 'sAddr',
			width : 130,
			fieldLabel : '联系地址',
			allowBlank : false,
			validator : function(v){
				if(Ext.util.Format.trim(v).length > 0){
					return true;
				}else{
					return '地址不能为空';
				}
			}
		},{
			xtype : 'textfield',
			id : 'sContact',
			width : 130,
			fieldLabel : '联系人',
			validator : function(v){
				if(Ext.util.Format.trim(v).length > 0){
					return true;
				}else{
					return '联系人不能为空';
				}
			}
		},{
			xtype : 'textfield',
			id : 'sComment',
			width : 130,
			fieldLabel : '备注',
			validator : function(v){
				if(Ext.util.Format.trim(v).length > 0 ){
					return true;
				}else{
					return '写上评价';
				}
			} 
		}],
		bbar : [{
			xtype : 'tbtext',
			text : '  '
		},'->',{
			text : '保存',
			id : 'btnSaveSupplier',
			iconCls : 'btn_save',
			handler : function(e){
				var sId = Ext.getCmp('sId').getValue();
				var sName = Ext.getCmp('sName').getValue();
				var sTele = Ext.getCmp('sTele').getValue();
				var sAddr = Ext.getCmp('sAddr').getValue();
				var sContact = Ext.getCmp('sContact').getValue();
				var sComment = Ext.getCmp('sComment').getValue();
				
				var actionUrl = '';
				if(!Ext.getCmp('sName').isValid() || !Ext.getCmp('sTele').isValid() || !Ext.getCmp('sAddr').isValid()){
					return;
				}
				if(addSupplier.operationType == 'insert'){
					
					actionUrl = '../../InsertSupplier.do';
				}
				else if(addSupplier.operationType == 'update'){
					actionUrl = '../../UpdateSupplier.do';
				}
				else return;
				
/*				var btnSave = Ext.getCmp('btnSaveSupplier');
				var btnCancel = Ext.getCmp('btnCloseSupplier');
				btnSave.setDisabled(false);
				btnCancel.setDisabled(false);*/
				
				Ext.Ajax.request({
					url : actionUrl,
					params : {
						pin : pin,
						supplierID : sId,
						supplierName : sName,
						tele : sTele,
						addr : sAddr,
						contact : sContact,
						comment : sComment
					},
					success : function(res, opt){
						Ext.getCmp('grid').store.reload();
						var jr = Ext.util.JSON.decode(res.responseText);
						if(jr.success){
							addSupplier.hide();
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
		}]
		
	}]
	
});


supplierOperactionHandler = function(c){
	if(c.type == 'undefined'){
		return;
	}
	
	var sId = Ext.getCmp('sId');
	var sName = Ext.getCmp('sName');
	var sTele = Ext.getCmp('sTele');
	var sAddr = Ext.getCmp('sAddr');
	var sContact = Ext.getCmp('sContact');
	var sComment = Ext.getCmp('sComment');
	
	if(c.type == 'insert'){
		addSupplier.setTitle('添加供应商');
	}
	else if(c.type == 'update'){
		var sn = Ext.getCmp('grid').getSelectionModel().getSelected(); 
		if(sn == null){
			Ext.MessageBox.alert('提示', '请选中一个供应商再进行操作.');
			return;
		}		
		addSupplier.setTitle('设置供应商信息');
		//Ext.MessageBox.alert('haode',sn.data.supplierID);
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
};

deleteSupplierOperationHandler = function(){
	var sd = Ext.getCmp('grid').getSelectionModel().getSelected();
/*	var sd = Ext.getCmp('grid').selModel.selections;
	Ext.MessageBox.alert('sd',sd.items[0].data.supplierID);*/
	if(sd != null){
		Ext.Msg.confirm(
				'提示',
				'是否删除方案 ?',
				function(e){
					if(e == 'yes'){
						
						Ext.Ajax.request({
							url : '../../DeleteSupplier.do?pin='+pin,
							params : {
								supplierId : sd.data.supplierID
							},
							success : function(res, opt){
								Ext.getCmp('grid').store.reload();
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
		Ext.MessageBox.alert('提示','请选中一个供应商再进行zheng!');
		return;
	}

	
	
	
};

Ext.onReady(function(){
	Ext.BLANK_IMAGE_URL = '../../extjs/resources/images/default/s.gif';
	Ext.QuickTips.init();
	Ext.form.Field.prototype.msgTarget = 'side';
	var btnAddSupplier = new Ext.ux.ImageButton({
		imgPath : '../../images/btnAddProgram.png',
		imgWidth : 50,
		imgHeight : 50,
		tooltip : '添加供应商',
		handler : function(e){
			supplierOperactionHandler({type:'insert'});
	
		}
	});
	//设置列默认值
	supplierOpt = function(){
		return "<a href = \"javascript:void(0);\" onclick=\"supplierOperactionHandler('update')\">" + "<img src='../../images/Modify.png'/>修改</a>"
			 +"&nbsp;&nbsp;"
			 + "<a href=\"javascript:void(0);\" onclick=\"deleteSupplierOperationHandler()\">" + "<img src='../../images/del.png'/>删除</a>";
	};	
	
	//定义列模型
	var cm = new Ext.grid.ColumnModel([
	       new Ext.grid.RowNumberer(),
		   {header:'供应商编号',dataIndex:'supplierID'},
		   {header:'供应商名称',dataIndex:'name'},
		   {header:'联系方式',dataIndex:'tele'},
		   {header:'地址',dataIndex:'addr'},
		   {header:'联系人',dataIndex:'contact'},
		   {header:'备注',dataIndex:'comment'},
		   {header:'操作',align:'center',dataIndex:'supplierOpt',renderer : supplierOpt}
	       ]);
	  	cm.defaultSortable = true;
	                               	
/*	var data = {results:1,rows:[{'supplierID' : 2, 'name' : 'aaa', 'tele' : '1235', 'addr' : 'dddddd', 'contact' : 'xili', 'comment' : 'good', 'caozuo':'<a href = "javascript:void(0);" onclick="deleteSupplierOperationHandler()">删除</a>&nbsp;&nbsp;&nbsp;&nbsp;<a href = "javascript:void(0);" onclick="supplierOperactionHandler(dmObj.operation.insert)">增加</a>'},
	                            {'supplierID':1, 'name':'bbb', 'tele':'234556', 'addr':'中环路', 'contact':'mingren', 'comment':'very good', 'caozuo':'<a href = "#" >删除</a>&nbsp;&nbsp;&nbsp;&nbsp;<a href = "#">增加</a>'},
								{'supplierID':3, 'name':'ccc', 'tele':'234556', 'addr':'中环路', 'contact':'mingren', 'comment':'very good', 'caozuo':'<a href = "#" >删除</a>&nbsp;&nbsp;&nbsp;&nbsp;<a href = "#">增加</a>'}
	                            ]};*/
	
/*	var ds = new Ext.data.Store({
		//代理,加载本地和远程
		//加载本地的
	    proxy: new Ext.data.MemoryProxy(data),
	    reader: new Ext.data.ArrayReader({}, [
	        {name : 'supplierID', mapping : 0},
	        {name : 'name', mapping : 1},
	        {name : 'tele', mapping : 2},
	        {name : 'addr', mapping : 3},
	        {name : 'contact', mapping : 4},
	        {name : 'comment', mapping : 5},
	        {name : 'caozuo', mapping : 6}
	    ])
	});*/
	//ds.load();
	
	//数据加载器
	var ds = new Ext.data.Store({
		//高度封装
/*		url : 'json.js',
		data : {'rows' : {'supplierID' : 2, 'name' : 'aaa', 'tele' : '1235', 'addr' : 'dddddd', 'contact' : 'xili', 'comment' : 'good'}},
		root : 'rows',
		autoLoad : true,
		fields : ['supplierID', 'name', 'tele', 'addr', 'contact', 'comment']*/
		//原始写法
		autoLoad : true,
		//代理,加载本地和远程
		//proxy : new Ext.data.MemoryProxy(data),
		//加载远程的
		proxy : new Ext.data.HttpProxy({url:'../../QuerySupplier.do?pin=' + pin}),
		reader : new Ext.data.JsonReader({totalProperty:'totalProperty', root:'rows'},[
			{name : 'supplierID'},
	        {name : 'name'},
	        {name : 'tele'},
	        {name : 'addr'},
	        {name : 'contact'},
	        {name : 'comment'},
	        {name : 'supplierOpt'}
		])
	});

	

	
	
	
	var suppllierGridTbar = new Ext.Toolbar({
		items : [{
			xtype : 'tbtext', 
			text : '供应商:'
		}, {
	    	xtype : 'textfield',
	    	id : 'txtSearchSupplierName'
	    }, '->', {
			text : '搜索',
			id : 'btnSearch',
			iconCls : 'btn_search',
			handler : function(){
				var supplierName = Ext.getCmp('txtSearchSupplierName');
				
				var sgs = supplierGrid.getStore();
				sgs.baseParams['name'] = supplierName.getValue();
				Ext.MessageBox.alert('名字', supplierName.getValue());
				//load两种加载方式,远程和本地
				sgs.load({
					params : {
						start : 0,
						limit : 20
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
				var sd = Ext.getCmp('grid').getSelectionModel().getSelected;
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
	   pageSize : 5,	//显示记录条数
	   store : ds,	//定义数据源
	   displayInfo : true,	//是否显示提示信息
	   displayMsg : "显示第{0}-{1}条,共有{2}条记录",
	   emptyMsg : "没有记录"
	});

	var supplierGrid = new Ext.grid.GridPanel({
			title : '供应商列表',
			id : 'grid',
		    height : '500',
		    border : true,
		    frame : true,
		    store : ds,
		    cm: cm,
		    tbar : suppllierGridTbar,
		    bbar : pagingBar
		});

	supplierGrid.region = 'center';
	
	var supplierPanel = new Ext.Panel({
		title : '供应商管理',
		region : 'center',//渲染到
		layout : 'border',//布局
		frame : true, //透明
		//子集
		items : [supplierGrid],
		tbar : new Ext.Toolbar({
			height : 55,
			items : [
			    {xtype:'tbtext',text:'&nbsp;&nbsp;'},
			    btnAddSupplier
			]
		})
	});

	new Ext.Viewport({
		layout : 'border',
		id : 'viewport',
		items : 
		[{
			region : 'north',
			bodyStyle : 'background-color:#DFE8F6;',
			html : '<h4 style="padding:10px;font-size:150%;float:left;">无线点餐网页终端</h4>',
			height : 50,
			border : false,
			margins : '0 0 0 0'
		},supplierPanel,
		{
			region : 'south',
			height : 30,
			frame : true,
			html : '<div style="font-size:11pt; text-align:center;"><b>版权所有(c) 2011 智易科技</b></div>'
		}]
	});


});






