var addSupplier;

addSupplier = new Ext.Window({
	title : '添加供应商',
	closable : false, //是否可关闭
	resizable : true, //大小调整
	modal : true,
	width : 260,
	items : [{
		xtype : 'form',
		layout : 'form',
		frame : true,
		labelWidth : 65,
		items : [{
			xtype : 'textfield',
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
			allowBlank : false,
			regex : Ext.ux.RegText.phone.reg,
			regexText : Ext.ux.RegText.phone.error,
			validator : function(v){
				if(Ext.util.Format.trim(v).length > 0){
					return true;
				}else{
					return '联系方式不允许为空';
				}
			}
			
		},{
			xtype : 'textfield',
			id : 'txtSContact',
			width : 130,
			fieldLabel : '联系人',
			allowBlank : false,
			validator : function(v){
				if(Ext.util.Format.trim(v).length > 0){
					return true;
				}else{
					return '联系人不能为空';
				}
			}
		},{
			xtype : 'textfield',
			id : 'txtSAddr',
			width : 130,
			fieldLabel : '地址',
			validator : function(v){
				if(Ext.util.Format.trim(v).length > 0){
					return true;
				}else{
					return '地址不能为空';
				}
			}
		},{
			xtype : 'textfield',
			id : 'txtSComment',
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
				var sId = Ext.getCmp('txtSId').getValue();
				var sName = Ext.getCmp('txtSName').getValue();
				var sTele = Ext.getCmp('txtSTele').getValue();
				var sAddr = Ext.getCmp('txtSAddr').getValue();
				var sContact = Ext.getCmp('txtSContact').getValue();
				var sComment = Ext.getCmp('txtSComment').getValue();
				
				var actionUrl = '';
				if(!Ext.getCmp('txtSName').isValid() || !Ext.getCmp('txtSTele').isValid() || !Ext.getCmp('txtSContact').isValid()){
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
	}],
	listeners : {
		'show' : function(thiz){
			if(addSupplier.operationType == 'insert'){
				Ext.getCmp('txtSName').setValue('');
				Ext.getCmp('txtSName').clearInvalid();
				
				Ext.getCmp('txtSTele').setValue('');
				Ext.getCmp('txtSTele').clearInvalid();
				
				Ext.getCmp('txtSAddr').setValue('');

				Ext.getCmp('txtSContact').setValue('');
				Ext.getCmp('txtSContact').clearInvalid();
				
				Ext.getCmp('txtSComment').setValue('');
				
				var fn = Ext.getCmp('txtSName');
				fn.focus.defer(100, fn);
			}
			
			
		}
		
	},
	keys : [{
		key : Ext.EventObject.ENTER,
		scope : this,
		fn : function(){
			Ext.getCmp("btnSaveSupplier").handler();
		}
	}]
		
});

var pushBackBut = new Ext.ux.ImageButton({
	imgPath : '../../images/UserLogout.png',
	imgWidth : 50,
	imgHeight : 50,
	tooltip : '返回',
	handler : function(btn){
		location.href = 'InventoryProtal.html?restaurantID=' + restaurantID + '&pin=' + pin;
	}
});

var logOutBut = new Ext.ux.ImageButton({
	imgPath : '../../images/ResLogout.png',
	imgWidth : 50,
	imgHeight : 50,
	tooltip : '登出',
	handler : function(btn){
		
	}
});


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
	readOnly : true,
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
	}
	else if(c.type == 'update'){
		var sn = Ext.getCmp('grid').getSelectionModel().getSelected(); 
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
		Ext.MessageBox.alert('提示','请选中一个供应商再进行操作!');
		return;
	}
	
};

Ext.onReady(function(){
	Ext.BLANK_IMAGE_URL = '../../extjs/resources/images/default/s.gif';
	Ext.QuickTips.init();
	Ext.form.Field.prototype.msgTarget = 'side';
	var btnAddSupplier = new Ext.ux.ImageButton({
		imgPath : '../../images/btnAddSupplier.png',
		imgWidth : 50,
		imgHeight : 50,
		tooltip : '添加供应商',
		handler : function(e){
			supplierOperactionHandler({type:'insert'});
	
		}
	});
	//设置列默认值
	supplierOpt = function(){
		return "<a href = \"javascript:supplierOperactionHandler({type:'update'})\">" + "<img src='../../images/Modify.png'/>修改</a>"
			 +"&nbsp;&nbsp;"
			 + "<a href=\"javascript:void(0);\" onclick=\"deleteSupplierOperationHandler()\">" + "<img src='../../images/del.png'/>删除</a>";
	};	
	
	//定义列模型
	var cm = new Ext.grid.ColumnModel([
	       new Ext.grid.RowNumberer(),
		   {header:'供应商名称',dataIndex:'name',width:210},
		   {header:'联系方式',dataIndex:'tele',width:210},
		   {header:'地址',dataIndex:'addr',width:230},
		   {header:'联系人',dataIndex:'contact',width:210},
		   {header:'备注',dataIndex:'comment',width:200},
		   {header:'操作',align:'center',dataIndex:'supplierOpt',renderer : supplierOpt,width:253}
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
		proxy : new Ext.data.HttpProxy({url:'../../QuerySupplier.do?pin=' + pin}),
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
			text : '搜索',
			id : 'btnSearch',
			iconCls : 'btn_search',
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
	   pageSize : 10,	//显示记录条数
	   store : ds,	//定义数据源
	   displayInfo : true,	//是否显示提示信息
	   displayMsg : "显示第 {0} 条到 {1} 条记录，共 {2} 条",
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
	ds.load({params:{start:0,limit:10}});

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
			    btnAddSupplier,
			    '->',
			    pushBackBut, 
			    {xtype:'tbtext',text:'&nbsp;&nbsp;'},
				logOutBut 
			]
		}),
		keys : [{
			key : Ext.EventObject.ENTER,
			scope : this,
			fn : function(){
				Ext.getCmp('btnSearch').handler();
			}
		}]
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






