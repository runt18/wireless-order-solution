Ext.BLANK_IMAGE_URL = '../../extjs/resources/images/default/s.gif';

var btnAddMaterialType = new Ext.ux.ImageButton({
	imgPath : '../../images/btnAddProgram.png',
	imgWidth : 50,
	imgHeight : 50,
	tooltip : '添加方案',
	handler : function(e){
		meterialTypeAddWin.show();
	}
});
var btnAddMaterial = new Ext.ux.ImageButton({
	imgPath : '../../images/btnAddDiscount.png',
	imgWidth : 50,
	imgHeight : 50,
	tooltip : '添加分厨折扣',
	handler : function(e){
		materialAddWin.show();
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
var store = new Ext.data.JsonStore({
    root: 'all',
    totalProperty: 'allCount',
    idProperty: 'materialId',
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
    proxy: new Ext.data.HttpProxy({
        url: '../../QueryMaterial.do'
    }),
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
	autoHeight:true,
	autoWidth:true,
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
	        width: 100,
	        sortable: true
	    },
		{
	        header: "编辑",
	        dataIndex: 'materialId',
			renderer:function(val){return "<a href='javascript:void()'>编辑</a>";},
	        width: 100,
	        sortable: true
	    },
		{
	        header: "删除",
	        dataIndex: 'materialId',
			renderer:function(val){return "<a href='javascript:void()'>删除</a>";},
	        width: 100,
	        sortable: true
	    },
	    {
	        header: "新建",
	        dataIndex: 'materialId',
			renderer:function(val){return "<a href='javascript:void()'>新建</a>";},
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
var materialAddWin = new Ext.Window({
	title:'添加原料',
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
						   xtype:'combo',
						   fieldLabel:'分类'
					},
					{
						   xtype:'textfield',
						   fieldLabel:'名称'
					},
					{
						   xtype:'textfield',
						   fieldLabel:'数量'
					},
					{
						   xtype:'textfield',
						   fieldLabel:'价格'
					},
					{
						   xtype:'textfield',
						   fieldLabel:'状态'
					}
	    	   ]
	       }
	],
	bbar:[
	      '->',
	      {
	    	  text:'保存'
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
	title:'添加材料分类',
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
	    	        	  blankText:'分类名称不能为空!',
	    	        	  id:'name'
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
	    	        	  id:'type'
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
								name:Ext.getCmp('name').getValue(),
								type:Ext.getCmp('type').getValue()
							},
							success:function(resp,opts){
								var json = Ext.decode(resp.responseText);
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
var tr = new Ext.tree.TreePanel({
	autoScroll:true,
	tbar:[
	      '->',
	      {
	    	  text:'修改'
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
tr.setRootNode(trRoot);
function refreshTree(){
	 Ext.Ajax.request({
			url:'../../QueryMaterialCate.do',
			params:{
				pin:pin,
				restaurantID:restaurantID
			},
			success:function(resp,opts){
				var json = Ext.decode(resp.responseText);
				for(var i = 0;i < json.msg.length;i ++){
					var trRootChild = new Ext.tree.TreeNode({
						text:''+json.msg[i].name
					});
					trRoot.appendChild(trRootChild);
				}
			},
			failure:function(resp,opts){
				alert('Ajax请求失败!');
			}
	  	});
}
var viewport;
Ext.onReady(function() {
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