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
		
	}
});
var pushBackBut = new Ext.ux.ImageButton({
	imgPath : '../../images/UserLogout.png',
	imgWidth : 50,
	imgHeight : 50,
	tooltip : '返回',
	handler : function(btn){
		location.href = 'BasicMgrProtal.html?restaurantID=' + restaurantID + '&pin=' + pin;
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
var materialAddWin = new Ext.Window({
	
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
						frame:true
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