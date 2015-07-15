var Request = new common_urlParaQuery();
var rid = Request["rid"];
//rid = 40;
var basePath = "http://localhost:8080";

//悬浮操作框的treeNode id
var floatBarNodeId = "";
//悬浮操作框的绝对位置
var nodey=0,barX=500, barY=800;
//var menuTree_obj = {treeId : 'weixinMenuTree', option : [{name:'修改', fn:"floatBarUpdateHandler()"},{name:'删除', fn:"alert(2222)"}]};

var menuTree_obj = {treeId : 'weixinMenuTree',operateTree:Ext.ux.operateTree_weixinMenu, mult : [{m_type : 1, option :[{name:'添加子菜单', fn:"addChildMenu()"},{name:'修改', fn:"floatBarUpdateHandler()"},{name:'删除', fn:"deleteMenu()"}]}, 
                                          											{m_type : 2, option :[{name:'修改', fn:"floatBarUpdateHandler()"},{name:'删除', fn:"deleteMenu()"}]},
                                          											]};

var tree,tabs, updateDeptWin;

var weixinMenuLM = new Ext.LoadMask(document.body, {
	msg  : '正在加载菜单......'
});

var northPanel = new Ext.Panel({
	contentEl : 'div4MenuInit',
	region : 'north',
	border : true	
});

var northPanel2 = new Ext.Panel({  
    title: 'new panel',  
    region : 'north',
    html: "新panel"  
}); 


function deptWinInit(){
	if(!updateDeptWin){
		updateDeptWin = new Ext.Window({
			title : '添加菜单',
			closable : false,
			resizable : false,
			modal : true,
			width : 230,
			items : [{
				xtype : 'form',
				layout : 'form',
				frame : true,
				labelWidth : 65,
				items : [{
					xtype : 'hidden',
					id : 'txtDeptID'
				}, {
					xtype : 'textfield',
					id : 'txtDeptName',
					fieldLabel : '菜单名称',
					width : 130
				}]
			}],
			bbar : [
			'->',
			{
				text : '保存',
				id : 'btnSaveUpdateDept',
				iconCls : 'btn_save',
				handler : function(){
					var deptID = Ext.getCmp('txtDeptID');
					var deptName = Ext.getCmp('txtDeptName');
					
					if(updateDeptWin.otype == 'insert'){
						tree.root.appendChild(new Ext.tree.TreeNode({text:deptName.getValue(), expanded:true, expandable: true,m_type:1, cls:'floatBarStyle', children:[]}));
						Ext.example.msg('提示', '添加成功');
					}else if(updateDeptWin.otype == 'addChild'){
						tree.getNodeById(deptID.getValue()).appendChild(new Ext.tree.TreeNode({text:deptName.getValue(), m_type:2, cls:'floatBarStyle', leaf: true}));
						Ext.example.msg('提示', '添加成功');						
					}else if(updateDeptWin.otype == 'update'){
						tree.getNodeById(deptID.getValue()).setText(deptName.getValue());
						Ext.example.msg('提示', '修改成功');
					}
					
					
					updateDeptWin.hide();
				}
			}, {
				text : '关闭',
				iconCls : 'btn_close',
				handler : function(){
					updateDeptWin.hide();
				}
			}],
			keys : [{
				 key : Ext.EventObject.ENTER,
				 fn : function(){ 
					 Ext.getCmp('btnSaveUpdateDept').handler();
				 },
				 scope : this 
			 }],
			 listeners : {
				 hide : function(){
					 Ext.getCmp('txtDeptName').setValue('');
					 Ext.getCmp('txtDeptID').setValue('');
				 }
			 }
		});
	}

}

function floatBarUpdateHandler(){
	var tn = Ext.ux.getSelNode(tree);
	if(!tn){
		Ext.example.msg('提示', '操作失败, 请选中一个菜单再进行操作.');
		return;
	}
	updateDeptWin.otype = 'update';
	operateDeptHandler(tn);
}

function addChildMenu(){
	var tn = Ext.ux.getSelNode(tree);
	if(!tn){
		Ext.example.msg('提示', '操作失败, 请选中一个菜单再进行操作.');
		return;
	}	
	updateDeptWin.otype = 'addChild';
	operateDeptHandler(tn)
}

function operateDeptHandler(node){
	var deptId = Ext.getCmp('txtDeptID');
	var deptName = Ext.getCmp("txtDeptName");
	if(updateDeptWin.otype == 'update'){
		updateDeptWin.setTitle("修改菜单信息");
		deptId.setValue(node.id);
		deptName.setValue(node.text);
	}else if(updateDeptWin.otype == 'insert'){
		updateDeptWin.setTitle("添加父菜单");
		deptId.setValue();
		deptName.setValue();
		deptName.clearInvalid();
	}else if(updateDeptWin.otype == 'addChild'){
		updateDeptWin.setTitle("添加子菜单");
		deptId.setValue(node.id);
		deptName.setValue();
		deptName.clearInvalid();
	}
	updateDeptWin.show();
	updateDeptWin.center();
	deptName.focus(true, 100);
}

function deleteMenu(){
//	var s = tree.getSelectionModel().getSelectedNode();
//	tree.root.removeChild(s);
	var node = Ext.ux.getSelNode(tree);
	Ext.Msg.confirm(
		'提示',
		'是否删除: ' + node.text,
		function(e){
			if(e == 'yes'){
				node.remove();
			}
		}
	);	
	
	
}

function operateMenuContent(){
	var tn = Ext.ux.getSelNode(tree);
	if(!tn){
		Ext.example.msg('提示', '操作失败, 请选中一个菜单再进行操作.');
		return;
	}		
	if(!$('#menuTxtReply').val()){
		Ext.example.msg('提示', '请输入内容');
		return ;
	}
	
	var dataSource = "insertMenu";
	if(tn.attributes.key && !isNaN(tn.attributes.key)){
		dataSource = "updateMenu";
	}	
	
	$.ajax({ 
	    type : "post", 
	    async:false, 
	    url : basePath+"/wx-term/WXOperateMenu.do",
	    data : {
	    	dataSource : dataSource,
	    	rid : rid,
	    	key : tn.attributes.key,
	    	text : $('#menuTxtReply').val()
	    },
	    dataType : "jsonp",//jsonp数据类型 
	    jsonp: "jsonpCallback",//服务端用于接收callback调用的function名的参数 
	    success : function(data){ 
			if(data.success){
				tn.attributes.type = "click";
				tn.attributes.key = data.other.key;
			}
	    }, 
	    error:function(xhr){ 
	        var rt = JSON.parse(xhr.responseText);
	        if(rt.success){
	        	Ext.example.msg('提示', rt.msg);
	        	if(dataSource == "insertMenu"){
	        		tn.attributes.type = "click";
	        		tn.attributes.key = rt.other.key;
	        	}
			}
	    } 
	}); 
	
} 

function operateMenuUrl(){
	var tn = Ext.ux.getSelNode(tree);
	if(!tn){
		Ext.example.msg('提示', '操作失败, 请选中一个菜单再进行操作.');
		return;
	}		
	if(!$('#url4Menu').val()){
		Ext.example.msg('提示', '请输入内容');
		return ;
	}	
	
	tn.attributes.type = "view";
	tn.attributes.url = $('#url4Menu').val();
	
	Ext.example.msg('提示', '操作成功');
}

function getWeixinMenu(){
	weixinMenuLM.show();
	$.ajax({ 
	    type : "get", 
//	    async:false, 
	    url : basePath+"/wx-term/WXOperateMenu.do?dataSource=weixinMenu&rid="+rid,
	    dataType : "jsonp",//jsonp数据类型 
	    jsonp: "jsonCallback",//服务端用于接收callback调用的function名的参数 
	    success : function(data){
	    	weixinMenuLM.hide();
			if(data.success){
				console.log(data.root);
			}
	    }, 
	    error:function(xhr){
	    	weixinMenuLM.hide();
	    	var rt = JSON.parse(xhr.responseText);
	    	var root ={
	 			text : 'root',
	 			children : []
	    	}
	    	
	    	var menu = rt.root[0];
	    	
	    	console.log("result:")
	    	console.log(menu)
	    	
	    	for (var i = 0; i < menu.button.length; i++) {
				var btn = {
	 				text : menu.button[i].name,
	 				type : menu.button[i].type,
	 				m_type : 1,
	 				key : menu.button[i].key,
	 				cls:'floatBarStyle',
	 				expanded:true,
	 				expandable: true,
	 				children : []
	 			}
				if(menu.button[i].sub_button && menu.button[i].sub_button.length > 0){
					var childs = menu.button[i].sub_button;
					for (var j = 0; j < childs.length; j++) {
						var son = {
							text : childs[j].name,
							key : childs[j].key,
							type : childs[j].type,
							m_type : 2,
							url : childs[j].url,
							leaf : true
						}
						btn.children.push(son);
					}
				}
				
				root.children.push(btn);
			}
	    	
	    	console.log(root)
	    	tree.setRootNode(new Ext.tree.AsyncTreeNode(root));
	    	
	    } 
	}); 	
}

Ext.onReady(function(){
	
	var programTreeTbar = new Ext.Toolbar({
		items : ['->'
/*		,{
			text : '',
			iconCls : 'btn_app',
			handler : function(){
				getWeixinMenu()
			}
		}*/
		,{
			text : '添加父菜单',
			iconCls : 'btn_add',
			handler : function(){
				updateDeptWin.otype = 'insert';
				operateDeptHandler();
			}
		},{
			text : '发布菜单',
			iconCls : 'btn_save',
			handler : function(){
				if(!tree.getRootNode().hasChildNodes()){
					Ext.example.msg('提示', '请至少添加一个菜单');
					return;
				}
				var menu = {"selfmenu_info":{"button":[]}};
				var btns = [];
				tree.getRootNode().eachChild(function(child){
					var btn ;

					if(child.hasChildNodes()){
						btn = {"name":child.text,"sub_button":{"list":[]}};
						child.eachChild(function(childson){
							var sonBtn;
							if(childson.attributes.type == "view"){
								sonBtn = {
									"type" : childson.attributes.type,
									"name" : childson.text,
									"url" : childson.attributes.url
								}						
							}else{
								sonBtn = {
									"type" : childson.attributes.type || "click",
									"name" : childson.text,
									"key" : childson.attributes.key || -1
								}
							}
							
							btn.sub_button.list.push(sonBtn);
						})
					}else{
						if(child.attributes.type == "view"){
							btn = {
								"type" : child.attributes.type,
								"name" : child.text,
								"url" : child.attributes.url
							}						
						}else{
							btn = {
								"type" : child.attributes.type || "click",
								"name" : child.text,
								"key" : child.attributes.key || -1
							}
						}					
					}
					
					btns.push(btn);
				});
				
				menu.selfmenu_info.button = btns;
				
				console.log("commit======")
				
				console.log(menu)
				$.ajax({ 
				    type : "post", 
				    async:false, 
				    url : basePath+"/wx-term/WXOperateMenu.do",
				    data : {
				    	dataSource : 'commitMenu',
				    	rid :rid,
				    	menu : JSON.stringify(menu)
				    },
				    dataType : "jsonp",//jsonp数据类型 
				    jsonp: "jsonpCallback",//服务端用于接收callback调用的function名的参数 
				    success : function(data){ 
						if(data.success){
							Ext.example.msg('提示', '发布成功');
						}
				    }, 
				    error:function(xhr){ 
				        var rt = JSON.parse(xhr.responseText);
				        if(rt.success){
				        	Ext.example.msg('提示', '发布成功');
						}
				    } 
				}); 
				
			}
		}]
	});	
	tree = new Ext.tree.TreePanel({
		title : '菜单管理',
		region : 'west',
		id : 'weixinMenuTree',
		width : 200,
		border : false,
		rootVisible : false,
		frame : true,
		autoScroll : true,
		lines : false,
		enableDD : true,
		bodyStyle : 'backgroundColor:#FFFFFF; border:1px solid #99BBE8;padding-left: 10px;',
		tbar : programTreeTbar,
 		root : new Ext.tree.AsyncTreeNode({
 			text : 'root',
 			children : [
/* 			{
 				text : '扫一扫',
 				m_type : 1,
 				cls:'floatBarStyle',
 				expanded:true,
 				expandable: true,
 				children : []
 			},{
 				text : '我的',
 				m_type : 1,
 				cls:'floatBarStyle',
 				expanded:true, 
 				expandable: true,
 				children : [{
 					text : '优惠活动', 
 					type : "view",
 					m_type : 2,
 					url : "www.baidu.com",
 					leaf:true
 				},{
 					text : '会员卡', 
 					type : "click",
 					m_type : 2,
 					key : "Member",
 					leaf:true
 				}]
 			}*/
 			]
		}) ,
		loader : new Ext.tree.TreeLoader(),
		listeners : {
			render : function(){
				getWeixinMenu();
			},
			click : function(e){
				$('#menuTxtReply').val("");			
				$('#url4Menu').val("");			
				var tn = Ext.ux.getSelNode(tree);
				if(tn.attributes.type == "view"){
					var tab = tabs.getComponent("tab_view");
					tabs.setActiveTab(tab);
					$('#url4Menu').val(tn.attributes.url);
				}else if(tn.attributes.type == "click"){
					var p = Ext.getCmp('contentPanel');
//					p.layout.north = northPanel2;
//					p.doLayout();
//					p.add(northPanel2).doLayout();
					if(isNaN(parseInt(tn.attributes.key))){
						return;
					}
					
					var tab = tabs.getComponent("tab_click");
					tabs.setActiveTab(tab);
					
					$.ajax({ 
					    type : "post", 
					    async:false, 
					    url : basePath+"/wx-term/WXOperateMenu.do",
					    data : {
					    	dataSource : 'menuReply',
					    	rid : rid,
					    	key : tn.attributes.key
					    },
					    dataType : "jsonp",//jsonp数据类型 
					    jsonp: "jsonpCallback",//服务端用于接收callback调用的function名的参数 
					    success : function(data){ 
							if(data.success){
							}
					    }, 
					    error:function(xhr){ 
					        var rt = JSON.parse(xhr.responseText);
					        if(rt.success){
								$('#menuTxtReply').val(rt.other.text);					
							}
					    } 
					}); 
				}else if(typeof tn.attributes.type == "undefined"){
//					Ext.getCmp('contentPanel').removeAll();
					Ext.getCmp('contentPanel').add(northPanel);
					Ext.getCmp('contentPanel').doLayout();
				}else{
					var tab = tabs.getComponent("tab_click");
					tabs.setActiveTab(tab);
				}
				
			},
			startdrag : function(t, n, e){
				if(!n.hasChildNodes()){
					startDeptNode = n.parentNode;
				}
			}
		}
	});
	
	//tree.expandAll();
	
/*  	tree.loader = new Ext.tree.TreeLoader({
		dataUrl : '../../QueryPrivilege.do',
		baseParams : {
			dataSource : 'pageTree'
		}
	});  */
/* 	var root = new Ext.tree.AsyncTreeNode({
		expanded: true,
		text : 'gen',
		children : [{
			text : 'node1', leaf:true
		},{
			text : 'node2', leaf:true
		}]
	}); 
	tree.setRootNode(root); */
	
	
 	tabs = new Ext.TabPanel({
 	    region:'center',
 	    deferredRender:false,
 	    activeTab:0,
 	    border : false,
 	    defaults: {autoScroll:true},
 	    enableTabScroll:true,
 	    minTabWidth: 115,
 	    //autoDestroy : false,
 	    items:[{
 	    	id : "tab_click",
 	        contentEl:'textReplyBox',
 	        title: '文字', 
 	        //iconCls : 'tab_home'
 	    },{
 	    	id : 'tab_view',
 	    	contentEl : 'urlReplyBox',
 	    	title:'连接'
 	    },{
 	        contentEl:'textAndPicReplyBox',
 	        title: '图文', 
 	        //iconCls : 'tab_home'
 	    }],
 	    listeners : {
 	    	tabchange : function(){
 	    		nodey = 0;
// 	    		page_tipHide();
 	    	}
 	    },
 	    plugins: new Ext.ux.TabCloseMenu()
 	    
 	});
 	
 	var centerPanel = new Ext.Panel({
 		id : 'contentPanel',
 		title : '设置',
 	    region:'center',
        margins:'5 0 5 5',
        split:true,
        layout : 'border',
        items: [
/*        {
        	contentEl : 'div4MenuInit',
        	region : 'north',
        	border : false
        },*/
        //northPanel2,
        tabs]
 	});
	
    new Ext.Viewport({
    	id : 'weixinViewport',
        layout : 'border',
        items : [{
                region : 'north',
				bodyStyle : 'background-color:#DFE8F6;',
				html : "<h4 id='restaurantName' style='padding:10px;font-size:150%;float:left;'>无线点餐网页终端</h4>" +
					"<div id='divLoginOut' class='loginOut' style='width: 40px;height: 41px;'><img id='btnLoginOut' src='../../images/UserLogout.png' width='40' height='40' /> </div>",
				height : 50,
				margins : '0 0 0 0',
				collapsible : false
            },tree,centerPanel
            ,{
    			region : 'south',
    			height : 30,
    			frame : true,
    			border : false,
    			html : '<div style="font-size:11pt; text-align:center;"><b>版权所有(c) 2011 智易科技</b></div>'
			}  
         ]
    });
    deptWinInit();
    
	showFloatOption(menuTree_obj);	
	
	
});	