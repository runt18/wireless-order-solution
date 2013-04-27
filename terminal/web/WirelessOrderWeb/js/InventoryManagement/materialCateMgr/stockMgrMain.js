Ext.BLANK_IMAGE_URL = '../../extjs/resources/images/default/s.gif';

var btnAddMaterialType = new Ext.ux.ImageButton({
	imgPath : '../../images/btnAddMaterialCate.png',
	imgWidth : 50,
	imgHeight : 50,
	tooltip : '添加方案',
	handler : function(e){
		meterialTypeAddWin.show();
		Ext.getCmp('materialAddSave').show();
		Ext.getCmp('materialUpdateSave').hide();
	}
});
var btnAddMaterial = new Ext.ux.ImageButton({
	imgPath : '../../images/btnAddMaterial.png',
	imgWidth : 50,
	imgHeight : 50,
	tooltip : '添加分厨折扣',
	handler : function(e){
		materialAddWin.show();
		Ext.getCmp('price').setValue('');
		Ext.getCmp('amount').setValue('');
		Ext.getCmp('status').setValue('');
		Ext.getCmp('name').setValue('');
		Ext.getCmp('materialId').setValue('');
		Ext.getCmp('materialAddSave').show();
		Ext.getCmp('materialUpdateSave').hide();
	}
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
var materialCateStore = new Ext.data.JsonStore({
    root: 'all',
    idProperty: 'cateId',
    fields: [
        'cateId', 
		'name'
    ],
    url: '../../QueryMaterialCate.do',
    baseParams:{
    	pin:pin,
    	restaurantID:restaurantID
    }
});
var materialAddWin = new Ext.Window({
	closable:false,
	layout:'form',
	closeAction:'hide',
	frame:true,
	width:400,
	height:200,
	items:[
	       {
	    	   xtype:'form',
	    	   frame:true,
	    	   items:[
	    	        {
	    	        	xtype:'hidden',
	    	        	id:'materialId'
	    	        },
					{
						   xtype:'combo',
						   fieldLabel:'分类',
						   store:materialCateStore,
						   valueField:'cateId',
					       displayField:'name',
					       readOnly:true,
					       id:'cateId',
					       forceSelection:true,
	    	        	   typeAhead : true,
	    	        	   mode : 'remote',
	    	        	   triggerAction : 'all',
	    	        	   selectOnFocus : true,
	    	        	   allowBlank : false
					},
					{
						   xtype:'textfield',
						   fieldLabel:'名称',
						   id:'name'
					},
					{
						   xtype:'textfield',
						   fieldLabel:'数量',
						   id:'amount'
					},
					{
						   xtype:'textfield',
						   fieldLabel:'价格',
						   id:'price'
					},
					{
						   xtype:'combo',
						   store:new Ext.data.SimpleStore({
							   fields:['value','text'],
							   data:[[1,'正常'],[2,'停用'],[3,'预警'],[4,'删除']]
						   }),
						   fieldLabel:'状态',
						   valueField:'value',
						   displayField:'text',
						   id:'status',
						   readOnly:true,
						   forceSelection:true,
	    	        	   typeAhead : true,
	    	        	   mode : 'local',
	    	        	   triggerAction : 'all',
	    	        	   selectOnFocus : true,
	    	        	   allowBlank : false
					}
	    	   ]
	       }
	],
	bbar:[
	      '->',
	      {
	    	  text:'保存',
	    	  id:'materialUpdateSave',
	    	  listeners:{
	    		  click:function(){
	    			  Ext.Ajax.request({
							url:'../../UpdateMaterial.do',
							params:{
								pin:pin,
								restaurantID:restaurantID,
								materialID:Ext.getCmp('materialId').getValue(),
								cateId:Ext.getCmp('cateId').getValue(),
								name:Ext.getCmp('name').getValue(),
								price:Ext.getCmp('price').getValue(),
								amount:Ext.getCmp('amount').getValue(),
								status:Ext.getCmp('status').getValue()
							},
							success:function(resp,opts){
								var json = Ext.decode(resp.responseText);
								Ext.example.msg('提示',json.msg);
								materialAddWin.hide();
								grid.getStore().reload({params:{start:0,limit:25}});
							},
							failure:function(resp,opts){
								alert('Ajax请求失败!');
							}
	    			  	});
	    		  }
	    	  }
	      },
	      {
	    	  text:'保存',
	    	  id:'materialAddSave',
	    	  listeners:{
	    		  click:function(){
    				  Ext.Ajax.request({
							url:'../../AddMaterial.do',
							params:{
								pin:pin,
								restaurantID:restaurantID,
								cateId:Ext.getCmp('cateId').getValue(),
								name:Ext.getCmp('name').getValue(),
								price:Ext.getCmp('price').getValue(),
								amount:Ext.getCmp('amount').getValue(),
								status:Ext.getCmp('status').getValue()
							},
							success:function(resp,opts){
								var json = Ext.decode(resp.responseText);
								Ext.example.msg('提示',json.msg);
								materialAddWin.hide();
								grid.getStore().reload({params:{start:0,limit:25}});
							},
							failure:function(resp,opts){
								alert('Ajax请求失败!');
							}
	    			  	});
	    		  }
	    	  }
	      },
	      {
	    	  text:'关闭',
	    	  listeners:{
	    		  click:function(){
	    			  materialAddWin.hide();
	    		  }
	    	  }
	      }
	]
});
var meterialTypeAddWin = new Ext.Window({
	closable:false,
	width:320,
	height:160,
	closeAction:'hide',
	items:[
	       {
	    	   xtype:'form',
	    	   frame:true,
	    	   items:[
	    	          {
	    	        	  xtype:'textfield',
	    	        	  fieldLabel:'分类名称',
	    	        	  allowBlank:false,
	    	        	  id:'cateName'
	    	          },
	    	          {
	    	        	  xtype:'combo',
	    	        	  fieldLabel:'类型',
	    	        	  store:new Ext.data.SimpleStore({
	    	        		  fields:['value','text'],
	    	        		  data:[[1,'商品'],[2,'原料']]
	    	        	  }),
	    	        	  forceSelection:true,
	    	        	  valueField:'value',
	    	        	  displayField:'text',
	    	        	  typeAhead : true,
	    	        	  mode : 'local',
	    	        	  triggerAction : 'all',
	    	        	  selectOnFocus : true,
	    	        	  allowBlank : false,
	    	        	  id:'cateType'
	    	          }
	    	   ]
	       }
	],
	bbar:[
	      '->',
	      {
	    	  text:'保存',
	    	  listeners:{
	    		  click:function(){
	    			  Ext.Ajax.request({
							url:'../../AddMaterialCate.do',
							params:{
								pin:pin,
								restaurantID:restaurantID,
								name:Ext.getCmp('cateName').getValue(),
								type:Ext.getCmp('cateType').getValue()
							},
							success:function(resp,opts){
								var json = Ext.decode(resp.responseText);
								Ext.example.msg('提示',json.msg);
								meterialTypeAddWin.hide();
							},
							failure:function(resp,opts){
								alert('Ajax请求失败!');
							}
	    			  	});
	    			  }
	    		  }  
	      },
	      {
	    	  text:'关闭',
	    	  listeners:{
	    		  click:function(){
	    			  meterialTypeAddWin.hide();
	    		  }
	    	  }
	      }
	]
});
var treeRoot = new Ext.tree.AsyncTreeNode({//定义了root为异步加载，不然出现的枝节点会无限加载的  
    id : 'root'  
});
var treeLoader = new Ext.tree.TreeLoader({//定义了loader，向后台传送请求，然后返回json形式的数据  
    dataUrl : '../../QueryMaterialCate.do'  
});

var tr = new Ext.tree.TreePanel({
	autoScroll:true,
	frame:true,
	animate : true,//动态加载  
    border : false,//不让树的底端出现边框  
    rootVisible : false,//让root消失，有时候为了美观和功能需要这样做  
    root : treeRoot,  
    loader : treeLoader,  
	tbar:[
	      '->',
	      {
	    	  text:'修改'
	      },
	      {
	    	  text:'删除',
	    	  listeners:{
	    		  click:function(){
	    			  Ext.Ajax.request({
	    					url:'../../DeleMaterialCate.do',
	    					params:{
	    						pin:pin,
	    						restaurantID:restaurantID,
	    						cateID:'1'
	    					},
	    					success:function(resp,opts){
	    						var json = Ext.decode(resp.responseText);
	    					},
	    					failure:function(resp,opts){
	    						alert('Ajax请求失败!');
	    					}
	    			  	});
	    		  }
	    	  }
	      },
	      {
	    	  text:'刷新',
	    	  listeners:{
	    		  click:function(){
	    			  refreshTree();
	    		  }
	    	  }
	      }
	]
});
var trRoot = new Ext.tree.TreeNode({
	text:'所有原材料'
});
tr.setRootNode(treeRoot);
function refreshTree(){
	 Ext.Ajax.request({
			url:'../../QueryMaterialCate.do',
			params:{
				pin:pin,
				restaurantID:restaurantID
			},
			success:function(resp,opts){
				var json = Ext.decode(resp.responseText);
				for(var i = 0;i < json.all.length;i ++){
					var trRootChild = new Ext.tree.TreeNode({
						text:''+json.all[i].name,
						id:''+json.all[i].cateId
					});
					trRoot.appendChild(trRootChild);
				}
			},
			failure:function(resp,opts){
				alert('Ajax请求失败!');
			}
	  	});
}
var store = new Ext.data.JsonStore({
    root: 'all',
    totalProperty: 'allCount',
    idProperty: 'materialId',
    url:'../../QueryMaterial.do',
    remoteSort: false,
    fields: [
        'materialId', 
		'amount', 
		'price',
		'name',
		'cateName', 
		'status',
		'lastModStaff',
		'lastModDate'
    ],
    listeners:{
    	beforeload:function(){
    		this.baseParams = {
    			pin:pin,
    			restaurntID:restaurantID
    		};
    	}
    }
});
var pagingBar = new Ext.PagingToolbar({ 
    pageSize: 25,
    store: store,
    displayInfo: true,
    displayMsg: '显示  {0} - {1} 共 {2}',
    emptyMsg: "没有要显示的记录",
    items:[
        '-', {
        pressed: true,
        enableToggle:true,
        text: '显示预览',
        cls: 'x-btn-text-icon details',
        toggleHandler: function(btn, pressed){
            var view = grid.getView();
            view.showPreview = pressed;
            view.refresh();
        }
    }]
});
var grid = new Ext.grid.GridPanel({
	autoWidth:true,
	autoScroll:true,
	height:300,
    title:'原料管理',
    store: store,
    trackMouseOver:false,
    disableSelection:true,
    loadMask: true,
    columns:[
		{
	        header: "原料ID",
	        dataIndex: 'materialId',
	        width: 100,
	        sortable: true
	    },
		{
	        header: "分类名称",
	        dataIndex: 'cateName',
	        width: 100,
	        align: 'right',
	        sortable: true
	    },
		{
	        header: "数量",
	        dataIndex: 'amount',
	        width: 100,
	        sortable: true
	    },
		{
	        header: "价格",
	        dataIndex: 'price',
	        width: 100,
	        sortable: true
	    },
		{
	        header: "名称",
	        dataIndex: 'name',
	        width: 100,
	        sortable: true
	    },
		{
	        header: "状态",
	        dataIndex: 'status',
	        renderer:function(val){
	        	if(val == '1'){
	        		return "正常";
	        	}
	        	if(val == '2'){
	        		return "<span color='red'>停用</span>";
	        	}
	        	if(val == '3'){
	        		return "预警";
	        	}
	        	else if(val == '4'){
	        		return "删除";
	        	}
	        },
	        width: 100,
	        sortable: true
	    },
	    {
	        header: "最后更新人",
	        dataIndex: 'lastModStaff',
	        width: 100,
	        sortable: true
	    },
	    {
	        header: "更新日期",
	        dataIndex: 'lastModDate',
	        renderer:function(val){
	        	return (new Date(val.time).format('20y-m-d H:i:s'));
	        },
	        width: 100,
	        sortable: true
	    },
		{
	        header: "编辑",
	        dataIndex: 'materialId',
			renderer:function(val){return "<a href='javascript:editMaterial("+val+");'>编辑</a>";},
	        width: 100,
	        sortable: true
	    },
		{
	        header: "删除",
	        dataIndex: 'materialId',
			renderer:function(val){return "<a href='javascript:deleMaterial("+val+");'>删除</a>";},
	        width: 100,
	        sortable: true
	    }
	],
    viewConfig: {
        forceFit:true,
        enableRowBody:true,
        showPreview:true,
        getRowClass : function(record, rowIndex, p, store){
            if(this.showPreview){
                return 'x-grid3-row-expanded';
            }
            return 'x-grid3-row-collapsed';
        }
    },
    bbar: pagingBar
});
function editMaterial(id){
	materialAddWin.show();
	Ext.getCmp('materialAddSave').hide();
	Ext.getCmp('materialUpdateSave').show();
	Ext.Ajax.request({
		url:'../../QueryMaterialByID.do',
		params:{
			pin:pin,
			restaurantID:restaurantID,
			materialID:id
		},
		success:function(resp,opts){
			var json = Ext.decode(resp.responseText);
			//Ext.getCmp('cateId').setValue(json.msg.cateId);
			Ext.getCmp('price').setValue(json.msg.price);
			Ext.getCmp('amount').setValue(json.msg.amount);
			Ext.getCmp('status').setValue(json.msg.status);
			Ext.getCmp('name').setValue(json.msg.name);
			Ext.getCmp('materialId').setValue(json.msg.materialId);
		},
		failure:function(resp,opts){
			alert('Ajax请求失败!');
		}
  	});
}
function deleMaterial(id){
	Ext.MessageBox.confirm('提示','数据删除后将无法恢复，确认删除?',function(btn){
		if(btn == 'yes'){
			Ext.Ajax.request({
				url:'../../DeleMaterialByID.do',
				params:{
					pin:pin,
					restaurantID:restaurantID,
					materialID:id
				},
				success:function(resp,opts){
					var json = Ext.decode(resp.responseText);
					Ext.example.msg('提示',json.msg);
					grid.getStore().reload({params:{start:0,limit:25}});
				},
				failure:function(resp,opts){
					alert('Ajax请求失败!');
				}
		  	});
		}
	});
}
var viewport;
Ext.onReady(function() {
    store.load({params:{start:0,limit:25}});
	refreshTree();
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
				title : '原材料管理',
				region : "center",
				layout:'border',
				tbar : new Ext.Toolbar({
					height : 55,
					items : [
					    {xtype:'tbtext',text:'&nbsp;&nbsp;'},
					    btnAddMaterialType,
					    {xtype:'tbtext',text:'&nbsp;&nbsp;'},
					    btnAddMaterial,
					    {xtype:'tbtext',text:'&nbsp;&nbsp;'},
					    '->', 
					    pushBackBut, 
					    {
							text : '&nbsp;&nbsp;&nbsp;',
							disabled : true
						}, 
						logOutBut 
					]
				}),
				items:[
					new Ext.Panel({
						title : '原材料分类',
						region : "west",
						width:300,
						frame : true,
						items:[tr]
					}),
					new Ext.Panel({
						title:'列表',
						region:'center',
						layout:'border',
						frame:true,
						items:[
						    {region:'center',items:[grid]}
						]
					})
				],
				frame : true,
				margins : '5 5 5 5'
		}),
		{
			region : "south",
			height : 30,
			layout : "form",
			frame : true,
			border : false,
			html : "<div style='font-size:11pt; text-align:center;'><b>版权所有(c) 2011 智易科技</b></div>"
		},
		]
	});
});