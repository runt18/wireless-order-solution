Ext.onReady(function(){

	
	
 	var centerPanel;
 	centerPanel = new Ext.Panel({
 		title : '设置',
 	    region:'center',
        layout : 'border',
        items: [{
        	region:'center',
        	layout:'column',
       		 items : [{
					columnWidth: 0.33, 
					layout : 'form',
					width : 350,
					frame : true,
					labelWidth : 40,
					defaults : {
						width : 250
					},
					items : [{
						xtype : 'textfield',
						id : 'itemTitle_textfield_weixin',
						fieldLabel : '标题',
						allowBlank : false
					}, {
						layout : 'column',
						width : 320,
						frame : true,
						items : [{
							columnWidth: 1, 
							height: 5
						}]
					},{
						xtype : 'textfield',
						id : 'itemContent_textfield_weixin',
						fieldLabel : '内容',
						style : 'margin-top:5px'
					}, {
						xtype : 'textarea',
						id : 'itemUrl_textarea_weixin',
						fieldLabel : '链接'
					}]
				}, {
					columnWidth: 0.67,
					id : 'foodMultiPrice_column_weixin',
					layout : 'column',
					width : 400,
					frame : true,
					defaults : {
						layout : 'form'
					},
					items : [{
						columnWidth: 1,
						style : 'margin-bottom:10px;text-align:center',
						items :[{
							xtype : 'button',
							text : '添加子显示项',
							width : 200,
							height : 20,
							handler : function(){
								optMultiPriceHandler();
							}
						}]

					}]
				}]  
        }]
 	});
	
	
	
	
	new Ext.Panel({
		renderTo : 'deputySet_div_weixinDeputyMgr',
		width : parseInt(Ext.getDom('deputySet_div_weixinDeputyMgr').parentElement.style.width.replace(/px/g, '')),
		height : parseInt(Ext.getDom('deputySet_div_weixinDeputyMgr').parentElement.style.height.replace(/px/g, '')),
		layout : 'border',
        items : [centerPanel]
	});
	
	
	
	
	
	
	
});