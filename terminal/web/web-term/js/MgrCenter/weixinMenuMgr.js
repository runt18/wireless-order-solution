

var Request = new common_urlParaQuery();
var rid = Request["rid"];
//rid = 40;
//var basePath = "http://localhost:8080";
var basePath = "http://wx.e-tones.net";

/**
 * 拓展string方法
 * @param args
 * @returns {String}
 */
String.prototype.format = function(args){
    var result = this;
    if (arguments.length > 0){    
        if (arguments.length == 1 && typeof args == "object"){
            for(var key in args) {
                if(args[key] != undefined){
                    var reg = new RegExp("({" + key + "})", "g");
                    result = result.replace(reg, args[key]);
                }
            }
        }else{
        	for(var i = 0; i < arguments.length; i++){
        		if (arguments[i] != undefined) {
        			var reg= new RegExp("({)" + i + "(})", "g");
        			result = result.replace(reg, arguments[i]);
                }
            }
        }
    }
    return result;
};



//悬浮操作框的treeNode id
var floatBarNodeId = "";
//悬浮操作框的绝对位置
var nodey=0,barX=500, barY=800;
//var menuTree_obj = {treeId : 'weixinMenuTree', option : [{name:'修改', fn:"floatBarUpdateHandler()"},{name:'删除', fn:"alert(2222)"}]};
//悬浮操作内容
var menuTree_obj = {treeId : 'weixinMenuTree',operateTree:Ext.ux.operateTree_weixinMenu, mult : [{m_type : 1, option :[{name:'添加子菜单', fn:"addChildMenu()"},{name:'修改', fn:"floatBarUpdateHandler()"},{name:'删除', fn:"deleteMenu()"}]}, 
                                          											{m_type : 2, option :[{name:'修改', fn:"floatBarUpdateHandler()"},{name:'删除', fn:"deleteMenu()"}]},
                                          											]};

var tree,tabs, updateDeptWin, p_box, imgFile,centerPanel,
	multiFoodPriceCount = 0, subscribe = false, subscribeKey = -1;

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
					id : 'menuID'
				}, {
					xtype : 'textfield',
					id : 'txtMenuName',
					fieldLabel : '菜单名称',
					width : 130
				}]
			}],
			bbar : [
			'->',
			{
				text : '保存',
				id : 'btnSaveUpdateMenu',
				iconCls : 'btn_save',
				handler : function(){
					var deptID = Ext.getCmp('menuID');
					var deptName = Ext.getCmp('txtMenuName');
					
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
					subscribe = false;
					
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
					 Ext.getCmp('btnSaveUpdateMenu').handler();
				 },
				 scope : this 
			 }],
			 listeners : {
				 hide : function(){
					 Ext.getCmp('txtMenuName').setValue('');
					 Ext.getCmp('menuID').setValue('');
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
	operateDeptHandler(tn);
}

function operateDeptHandler(node){
	var deptId = Ext.getCmp('menuID');
	var deptName = Ext.getCmp("txtMenuName");
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
	var tn = Ext.ux.getSelNode(tree);
	Ext.Msg.confirm(
		'提示',
		'是否删除: ' + tn.text,
		function(e){
			if(e == 'yes'){
				if(tn.attributes.key && !isNaN(tn.attributes.key)){
					$.ajax({ 
					    type : "post", 
					    async : false,
					    url : basePath + "/wx-term/WXOperateMenu.do",
					    data : {
					    	dataSource : 'deleteMenu',
						    rid : rid,
						    key : tn.attributes.key
					    },
					    dataType : "jsonp",		//jsonp数据类型 
					    jsonp: "jsonpCallback",	//服务端用于接收callback调用的function名的参数 
					    success : function(data){ 
							 Ext.example.msg('提示', data.msg);
					    }, 
					    error : function(xhr){ 
					        var rt = JSON.parse(xhr.responseText);
					        Ext.example.msg('提示', rt.msg);
					    }
					}); 	
				}
				tn.remove();
				clearTabContent();
				
			}
		}
	);	
	
	
}

/**
 * 保存文本回复
 */
function operateMenuContent(){
	var tn = Ext.ux.getSelNode(tree);
	if(!tn && !subscribe){
		Ext.example.msg('提示', '操作失败, 请选中一个菜单再进行操作.');
		return;
	}		
	if(!$('#menuTxtReply').val()){
		Ext.example.msg('提示', '请输入内容');
		return ;
	}
	
	var key = null;
	var dataSource = "insertText";
	if(subscribeKey != -1){
		dataSource = "updateText";
		key = subscribeKey;
	}else if(tn && tn.attributes.key && !isNaN(tn.attributes.key)){
		dataSource = "updateText";
		key = tn.attributes.key;
	}	
	
	$.ajax({ 
	    type : "post", 
	    async:false, 
	    url : basePath + "/wx-term/WXOperateMenu.do",
	    dataType : "jsonp",		//jsonp数据类型 
	    jsonp : "jsonpCallback",	//服务端用于接收callback调用的function名的参数 	    
	    data : {
	    	dataSource : dataSource,
	    	rid : rid,
	    	key : key != null ? key : "",
	    	text : $('#menuTxtReply').val(),
	    	subscribe : subscribe ? subscribe : ""
	    },
	    success : function(rt){ 
	        if(rt.success){
	        	if(dataSource == "insertText"){
	        		if(tn){
	        			tn.attributes.type = "click";
	        			tn.attributes.key = rt.other.key;
	        		}else{
	        			subscribeKey = rt.other.key;
	        		}
	        	}
			}
	        Ext.example.msg('提示', rt.msg);
	    }, 
	    error:function(xhr){ 
	        var rt = JSON.parse(xhr.responseText);
	        if(rt.success){
	        	if(dataSource == "insertText"){
	        		if(tn){
	        			tn.attributes.type = "click";
	        			tn.attributes.key = rt.other.key;
	        		}else{
	        			subscribeKey = rt.other.key;
	        		}
	        	}
			}
	        Ext.example.msg('提示', rt.msg);
	    } 
	}); 
	
} 

/**
 * 设置系统保留菜单
 */
function setSystemMenu(){
	var tn = Ext.ux.getSelNode(tree);
	if(!tn){
		Ext.example.msg('提示', '操作失败, 请选中一个菜单再进行操作.');
		return;
	}	
	
	var key = $('input[name="systemSet"]:checked').val();
	
	tn.attributes.type = "click";
	if(key == "scan_event_key"){
		tn.attributes.type = "scancode_waitmsg";
	}
	tn.attributes.key = key;
	
	Ext.example.msg('提示', '设置成功');
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
	    url :  basePath + "/wx-term/WXOperateMenu.do?dataSource=weixinMenu&rid="+rid,
	    jsonp: "jsonpCallback",	//服务端用于接收callback调用的function名的参数 
	    dataType : "json",		//jsonp数据类型
	    success : function(rt){
	    	weixinMenuLM.hide();
	    	var root = {
	 			text : 'root',
	 			children : []
	    	};
	    	
	    	var menu = rt.root[0];
	    	
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
	 			};
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
						};
						btn.children.push(son);
					}
				}
				
				root.children.push(btn);
			}
	    	
	    	tree.setRootNode(new Ext.tree.AsyncTreeNode(root));
	    }, 
	    error : function(xhr){
	    	weixinMenuLM.hide();
	    	var rt = JSON.parse(xhr.responseText);
	    	var root ={
	 			text : 'root',
	 			children : []
	    	};
	    	
	    	var menu = rt.root[0];
	    	
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
	 			};
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
						};
						btn.children.push(son);
					}
				}
				
				root.children.push(btn);
			}
	    	
	    	tree.setRootNode(new Ext.tree.AsyncTreeNode(root));
	    } 
	}); 	
}

function clearTabContent(){
	$('#menuTxtReply').val("");			
	$('#url4Menu').val("");	
	
	$('#itemTitle').val("");			
	$('#itemContent').val("");	
	$('#itemUrl').val("");			
	delete p_box.image;	
	imgFile.setImg("");
	
	if(multiFoodPriceCount > 0){
		for (var j = 1; j <= multiFoodPriceCount; j++) {
			var cmps = $('.multiClass'+j);
			for (var i = 0; i < cmps.length; i++) {
				Ext.getCmp('food_multiPrice').remove(cmps[i].getAttribute("id"));
			}
		}
	}
	Ext.getCmp('food_multiPrice').doLayout();
	multiFoodPriceCount = 0;
	
	$('input[name="systemSet"]:checked').removeAttr("checked");
	
	subscribe = false, subscribeKey = -1;
}

Ext.onReady(function(){
	$.ajax({ 
	    type : "post", 
	    url : basePath + "/wx-term/WXOperateMenu.do",
	    data : {
	    	dataSource : 'systemMenu'
	    },
	    dataType : "jsonp",//jsonp数据类型 
	    jsonp: "jsonpCallback",//服务端用于接收callback调用的function名的参数 
	    success : function(data){ 
			if(data.success){
			}
	    }, 
	    error : function(xhr){ 
	        var rt = JSON.parse(xhr.responseText);
	        var systemMenuTemplate = '<input id="{id}" type="radio" name="systemSet" value={key}><label for="{id}">{desc}</label>';
	        
	        if(rt.success){
	        	var html = [];
	        	for (var i = 0; i < rt.root.length; i++) {
	        		html.push(systemMenuTemplate.format({
	        			id : "r"+(i+1),
	        			key : rt.root[i].key,
	        			desc : rt.root[i].desc
	        		}));
				}
	        	$("#div4systemReplyBox").html(html.join(""));
			}
	    } 
	}); 
	
	
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
								};					
							}else{
								sonBtn = {
									"type" : childson.attributes.type || "click",
									"name" : childson.text,
									"key" : childson.attributes.key || -1
								};
							}
							
							btn.sub_button.list.push(sonBtn);
						});
					}else{
						if(child.attributes.type == "view"){
							btn = {
								"type" : child.attributes.type,
								"name" : child.text,
								"url" : child.attributes.url
							};						
						}else{
							btn = {
								"type" : child.attributes.type || "click",
								"name" : child.text,
								"key" : child.attributes.key || -1
							};
						}					
					}
					
					btns.push(btn);
				});
				
				menu.selfmenu_info.button = btns;
				
				$.ajax({ 
				    type : "post", 
				    async : false, 
				    url : basePath+"/wx-term/WXOperateMenu.do",
				    data : {
				    	dataSource : 'commitMenu',
				    	rid :rid,
				    	menu : JSON.stringify(menu)
				    },
				    dataType : "jsonp",		//jsonp数据类型 
				    jsonp: "jsonpCallback",	//服务端用于接收callback调用的function名的参数 
				    success : function(data){ 
						if(data.success){
							Ext.example.msg('提示', '发布成功');
						}
				    }, 
				    error:function(xhr){ 
				        JSON.parse(xhr.responseText);
				        Ext.example.msg('提示', '发布成功');
				    } 
				}); 
				
			}
		}]
	});	
	tree = new Ext.tree.TreePanel({
		title : '菜单管理',
		region : 'center',
		id : 'weixinMenuTree',
		width : 240,
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
				clearTabContent();
				
				var tn = Ext.ux.getSelNode(tree);
				centerPanel.setTitle('设置 -- ' +tn.text);
				
				if(tn.hasChildNodes()){
					tabs.disable();
					return;
				}else{
					tabs.enable();
				}
				
				if(tn.attributes.type == "view"){
					var tab = tabs.getComponent("tab_view");
					tabs.setActiveTab(tab);
					$('#url4Menu').val(tn.attributes.url);
				}else if(tn.attributes.type == "click"){
					Ext.getCmp('contentPanel');
//					p.layout.north = northPanel2;
//					p.doLayout();
//					p.add(northPanel2).doLayout();
					
					//判断key是否为数字
					if(isNaN(parseInt(tn.attributes.key))){
						var tab = tabs.getComponent("tab_system");
						tabs.setActiveTab(tab);
						
						$('input[name="systemSet"]').each(function(){
							if(this.value == tn.attributes.key){
								$(this).attr("checked","checked");
							}
						});
					}else{
						$.ajax({ 
						    type : "post", 
						    async : false, 
						    url : basePath + "/wx-term/WXOperateMenu.do",
						    dataType : "jsonp",			//jsonp数据类型 
						    jsonp: "jsonpCallback",		//服务端用于接收callback调用的function名的参数 
						    data : {
						    	dataSource : 'menuReply',
						    	rid : rid,
						    	key : tn.attributes.key
						    },
						    success : function(rt){ 
						        if(rt.success){
						        	if(rt.other){
						        		$('#menuTxtReply').val(rt.other.text);
						        		
										var tab = tabs.getComponent("tab_click");
										tabs.setActiveTab(tab);
						        	}else if(rt.root.length > 0){
						        		var item = rt.root[0];
						        		
										$('#itemTitle').val(item.title);			
										$('#itemContent').val(item.description);	
										$('#itemUrl').val(item.url);	
						        		imgFile.setImg(item.picUrl);
						        		
										var tab = tabs.getComponent("tab_image_text");
										tabs.setActiveTab(tab);
										
						        		//如果有子显示项
						        		if(rt.root.length > 1){
						        			multiFoodPriceCount = rt.root.length - 1;
						        			for (var i = 1; i < rt.root.length; i++) {
						        				var subTitleId = 'subTitle' + i,  
						        				subUrlId = 'subUrl' + i, 
						        				subImgFileId = 'subImgFile' + i;
						        				subFormId = 'subForm' + i;
						        			
							        			Ext.getCmp('food_multiPrice').add({
							        				cls : 'multiClass'+i,
							        		 		columnWidth : 1	 		
							        		 	});								
							        			
							        			Ext.getCmp('food_multiPrice').add({
							        				cls : 'multiClass'+i,
							        				columnWidth: 0.55,
							        				labelWidth : 40,
							        				defaults : {
							        					width : 300
							        				},
							        				items :[{
							        					xtype : 'textfield',
							        					id : subTitleId,
							        					fieldLabel : '标题',
							        					value : rt.root[i].title,
							        					allowBlank : false
							        				},{
							        					xtype : 'textarea',
							        					id : subUrlId,
							        					fieldLabel : '链接',
							        					value : rt.root[i].url
							        				}]
							        			});	
	
							        			var sub_box = new Ext.BoxComponent({
							        				xtype : 'box',
							        		 	    height : 55,
							        		 	    autoEl : {
							        		 	    	tag : 'img',
							        		 	    	title : '图片预览'
							        		 	    }
							        			});
							        			var sub_imgFile = Ext.ux.plugins.createImageFile({
							        				id : subImgFileId,
							        				formId : subFormId,
							        				img : sub_box,
							        				imgSize : 100,
							        				//打开图片后的操作
							        				uploadCallback : function(c){
							        					subImageOperate(c);
							        				}
							        			});
							        			
							        			var sub_form = new Ext.form.FormPanel({
							        				id : subFormId,
							        				labelWidth : 60,
							        				fileUpload : true,
							        				items : [sub_imgFile],
							        				listeners : {
							        		    		render : function(e){
							        		    			//Ext.getDom(e.getId()).setAttribute('enctype', 'multipart/form-data');
							        		 	  		}
							        		    	}
							        			});	
							        			
							        			Ext.getCmp('food_multiPrice').add({
							        				cls : 'multiClass'+i,
							        				columnWidth: 0.35, 
							        				layout : 'column',
							        				frame : true,
							        				items : [sub_box, sub_form]	 		
							        		 	});			
							        			
							        			Ext.getCmp('food_multiPrice').add({
							        				cls : 'multiClass'+i,
							        		 		columnWidth : .1,
							        		 		items : [{
							        			    	xtype : 'button',
							        			    	text : '删除',
							        			    	style:'margin-top:40px;margin-left:10px;',
							        			    	multiIndex : i,
							        			    	iconCls : 'btn_delete',
							        			    	handler : function(e){
							        			    		deleteMultiPriceHandler(e);
							        			    	}
							        		 		}] 		 		
							        		 	});	
							        			
							        			Ext.getCmp('food_multiPrice').doLayout();
							        			
							        			sub_imgFile.setImg(rt.root[i].picUrl);
											}
						        		}

						        	}
								}
						    }, 
						    error:function(xhr){ 
						        var rt = JSON.parse(xhr.responseText);
						        if(rt.success){
						        	if(rt.other){
						        		$('#menuTxtReply').val(rt.other.text);
						        		
										var tab = tabs.getComponent("tab_click");
										tabs.setActiveTab(tab);
						        	}else if(rt.root.length > 0){
						        		var item = rt.root[0];
						        		
										$('#itemTitle').val(item.title);			
										$('#itemContent').val(item.description);	
										$('#itemUrl').val(item.url);	
						        		imgFile.setImg(item.picUrl);
						        		
						        		p_box.image = item.picUrl;
						        		
										var tab = tabs.getComponent("tab_image_text");
										tabs.setActiveTab(tab);
										
						        		//如果有子显示项
						        		if(rt.root.length > 1){
						        			multiFoodPriceCount = rt.root.length - 1;
						        			for (var i = 1; i < rt.root.length; i++) {
						        				var subTitleId = 'subTitle' + i,  
						        				subUrlId = 'subUrl' + i, 
						        				subImgFileId = 'subImgFile' + i;
						        				subFormId = 'subForm' + i;
						        			
							        			Ext.getCmp('food_multiPrice').add({
							        				cls : 'multiClass'+i,
							        		 		columnWidth : 1	 		
							        		 	});								
							        			
							        			Ext.getCmp('food_multiPrice').add({
							        				cls : 'multiClass'+i,
							        				columnWidth: 0.55,
							        				labelWidth : 40,
							        				defaults : {
							        					width : 300
							        				},
							        				items :[{
							        					xtype : 'textfield',
							        					id : subTitleId,
							        					fieldLabel : '标题',
							        					value : rt.root[i].title,
							        					allowBlank : false
							        				},{
							        					xtype : 'textarea',
							        					id : subUrlId,
							        					fieldLabel : '链接',
							        					value : rt.root[i].url
							        				}]
							        			});	
	
							        			var sub_box = new Ext.BoxComponent({
							        				xtype : 'box',
							        		 	    height : 55,
							        		 	    autoEl : {
							        		 	    	tag : 'img',
							        		 	    	title : '图片预览'
							        		 	    }
							        			});
							        			var sub_imgFile = Ext.ux.plugins.createImageFile({
							        				id : subImgFileId,
							        				formId : subFormId,
							        				img : sub_box,
							        				imgSize : 100,
							        				//打开图片后的操作
							        				uploadCallback : function(c){
							        					subImageOperate(c);
							        				}
							        			});
							        			
							        			var sub_form = new Ext.form.FormPanel({
							        				id : subFormId,
							        				labelWidth : 60,
							        				fileUpload : true,
							        				items : [sub_imgFile],
							        				listeners : {
							        		    		render : function(e){
							        		    			//Ext.getDom(e.getId()).setAttribute('enctype', 'multipart/form-data');
							        		 	  		}
							        		    	}
							        			});	
							        			
							        			Ext.getCmp('food_multiPrice').add({
							        				cls : 'multiClass'+i,
							        				columnWidth: 0.35, 
							        				layout : 'column',
							        				frame : true,
							        				items : [sub_box, sub_form]	 		
							        		 	});			
							        			
							        			Ext.getCmp('food_multiPrice').add({
							        				cls : 'multiClass'+i,
							        		 		columnWidth : .1,
							        		 		items : [{
							        			    	xtype : 'button',
							        			    	text : '删除',
							        			    	style:'margin-top:40px;margin-left:10px;',
							        			    	multiIndex : i,
							        			    	iconCls : 'btn_delete',
							        			    	handler : function(e){
							        			    		deleteMultiPriceHandler(e);
							        			    	}
							        		 		}] 		 		
							        		 	});	
							        			
							        			Ext.getCmp('food_multiPrice').doLayout();
							        			
							        			sub_imgFile.setImg(rt.root[i].picUrl);
							        			
							        			sub_imgFile.image = rt.root[i].picUrl;
											}
						        		}
						        		
						        	}
								}
						    } 
						}); 						
					}
					

				}else if(typeof tn.attributes.type == "undefined"){
//					Ext.getCmp('contentPanel').removeAll();
					Ext.getCmp('contentPanel').add(northPanel);
					Ext.getCmp('contentPanel').doLayout();
				}else{
					var tab = tabs.getComponent("tab_system");
					tabs.setActiveTab(tab);
					
					$('input[name="systemSet"]').each(function(){
						if(this.value == tn.attributes.key){
							$(this).attr("checked","checked");
						}
					});
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
	
	var treePanel = new Ext.Panel({
		layout : 'border',
		width : 240,
		frame : false,
		region : 'west',
		items : [tree, new Ext.Panel({
					title : '关注回复',
					region : 'south',
					contentEl : 'divSetAutoReply'
				})
		]
	});	
	
	
	var menu_uploadMask = new Ext.LoadMask(document.body, {
		msg : '正在上传图片...'
	});
	p_box = new Ext.BoxComponent({
		xtype : 'box',
 	    columnWidth : 1,
 	    height : 200,
 	    autoEl : {
 	    	tag : 'img',
 	    	title : '图片预览'
 	    }
	});
	imgFile = Ext.ux.plugins.createImageFile({
		img : p_box,
		width : 300,
		height : 200,
		imgSize : 100,
		uploadCallback : function(){
			Ext.getCmp('btnWeixinReplyUploadImage').handler();
		}
	});
	var btnUpload = new Ext.Button({
			hidden : true,
			id : 'btnWeixinReplyUploadImage',
	        text : '上传图片',
	        listeners : {
	        	render : function(thiz){
	        		thiz.getEl().setWidth(100, true);
	        	}
	        },
	        handler : function(e){
	        	var check = true, img = '';
	        	if(Ext.isIE){
	        		Ext.getDom(imgFile.getId()).select();
	        		img = document.selection.createRange().text;
	        	}else{
	 	        	img = Ext.getDom(imgFile.getId()).value;
	        	}
	        	if(typeof(img) != 'undefined' && img.length > 0){
		 	        var type = img.substring(img.lastIndexOf('.') + 1, img.length);
		 	        check = false;
		 	        for(var i = 0; i < Ext.ux.plugins.imgTypes.length; i++){
		 	        	if(type.toLowerCase() == Ext.ux.plugins.imgTypes[i].toLowerCase()){
		 	        		check = true;
			 	           	break;
			 	        }
		 	        }
		 	        if(!check){
			 	       	Ext.example.msg('提示', '图片类型不正确.');
			 	        return;
	 	        	}
	        	}else{
	        		Ext.example.msg('提示', '未选择图片.');
	 	        	return;
	        	}
	        	menu_uploadMask.show();
	        	Ext.Ajax.request({
	        		url : '../../OperateImage.do?dataSource=upload&ossType=10&rid='+rid,
	 	   			isUpload : true,
	 	   			form : form.getForm().getEl(),
	 	   			success : function(response, options){
	 	   				menu_uploadMask.hide();
	 	   				var jr = Ext.decode(response.responseText.replace(/<\/?[^>]*>/g,''));
	 	   				if(jr.success){
		  	   				var ossImage = jr.root[0];
		  	   				p_box.image = ossImage.image;
		  	   				p_box.ossId = ossImage.imageId;	   				
	 	   				}else{
	 	   					Ext.ux.showMsg(jr);
	 	   					imgFile.setImg("");
	 	   				}

	 	   				
	 	   			},
	 	   			failure : function(response, options){
	 	   				menu_uploadMask.hide();
	 	   				Ext.ux.showMsg(Ext.decode(response.responseText.replace(/<\/?[^>]*>/g,'')));
	 	   			}
	        	});
	        }
	});
	var form = new Ext.form.FormPanel({
		columnWidth : 1,
		labelWidth : 60,
		fileUpload : true,
		items : [imgFile],
		listeners : {
    		render : function(e){
    			Ext.getDom(e.getId()).setAttribute('enctype', 'multipart/form-data');
 	  		}
    	},
    	buttonAlign : 'center',
    	buttons : [btnUpload]
	});
	

	
	var sub_box = new Ext.BoxComponent({
		xtype : 'box',
 	    height : 55,
 	    autoEl : {
 	    	tag : 'img',
 	    	title : '图片预览'
 	    }
	});
	var sub_imgFile = Ext.ux.plugins.createImageFile({
		id : 'imageFileIdTest',
		img : sub_box,
		imgSize : 100,
		uploadCallback : function(){
			//Ext.getCmp('sub_btnWeixinReplyUploadImage').handler();
		}
	});
	var sub_btnUpload = new Ext.Button({
			hidden : true,
			id : 'sub_btnWeixinReplyUploadImage',
	        text : '上传图片',
	        listeners : {
	        	render : function(thiz){
	        		thiz.getEl().setWidth(100, true);
	        	}
	        },
	        handler : function(e){
	        	var check = true, img = '';
	        	if(Ext.isIE){
	        		Ext.getDom(imgFile.getId()).select();
	        		img = document.selection.createRange().text;
	        	}else{
	 	        	img = Ext.getDom(imgFile.getId()).value;
	        	}
	        	if(typeof(img) != 'undefined' && img.length > 0){
		 	        var type = img.substring(img.lastIndexOf('.') + 1, img.length);
		 	        check = false;
		 	        for(var i = 0; i < Ext.ux.plugins.imgTypes.length; i++){
		 	        	if(type.toLowerCase() == Ext.ux.plugins.imgTypes[i].toLowerCase()){
		 	        		check = true;
			 	           	break;
			 	        }
		 	        }
		 	        if(!check){
			 	       	Ext.example.msg('提示', '图片类型不正确.');
			 	        return;
	 	        	}
	        	}else{
	        		Ext.example.msg('提示', '未选择图片.');
	 	        	return;
	        	}
	        	menu_uploadMask.show();
	        	Ext.Ajax.request({
	        		url : '../../OperateImage.do?dataSource=upload&ossType=10&rid='+rid,
	 	   			isUpload : true,
	 	   			form : form.getForm().getEl(),
	 	   			success : function(response, options){
	 	   				menu_uploadMask.hide();
	 	   				var jr = Ext.decode(response.responseText.replace(/<\/?[^>]*>/g,''));
	 	   				if(jr.success){
		  	   				var ossImage = jr.root[0];
		  	   				p_box.image = ossImage.image;
		  	   				p_box.ossId = ossImage.imageId;	   				
	 	   				}else{
	 	   					Ext.ux.showMsg(jr);
	 	   					imgFile.setImg("");
	 	   				}

	 	   				
	 	   			},
	 	   			failure : function(response, options){
	 	   				menu_uploadMask.hide();
	 	   				Ext.ux.showMsg(Ext.decode(response.responseText.replace(/<\/?[^>]*>/g,'')));
	 	   			}
	        	});
	        }
	});
	var sub_form = new Ext.form.FormPanel({
		labelWidth : 60,
		fileUpload : true,
		items : [sub_imgFile],
		listeners : {
    		render : function(e){
    			//Ext.getDom(e.getId()).setAttribute('enctype', 'multipart/form-data');
 	  		}
    	},
    	buttonAlign : 'center',
    	buttons : [sub_btnUpload]
	});	
	
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
 	    	id : 'tab_image_text',
 	        title: '图文', 
			layout : 'form',
			width : 270,
			frame : true,
			items : [{
				xtype : 'fieldset',
				title : '多图文',
				autoHeight : true,
				width : 1050,
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
						id : 'itemTitle',
						fieldLabel : '标题',
						allowBlank : false
					}, {
						layout : 'column',
						width : 320,
						frame : true,
						items : [p_box, {
							columnWidth: 1, 
							height: 5
						}, form]
					},{
						xtype : 'textfield',
						id : 'itemContent',
						fieldLabel : '内容',
						style : 'margin-top:5px'
					}, {
						xtype : 'textarea',
						id : 'itemUrl',
						fieldLabel : '链接'
					}]
				}, {
					columnWidth: 0.67,
					id : 'food_multiPrice',
					layout : 'column',
					width : 400,
					frame : true,
					defaults : {
						layout : 'form',
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
			}, {
				xtype : 'button',
				text : '保存',
				width : 100,
				height : 20,
				handler : function(){
					var tn = Ext.ux.getSelNode(tree);
					if(!tn && !subscribe){
						Ext.example.msg('提示', '操作失败, 请选中一个菜单再进行操作.');
						return;
					}		
					if(!$('#itemTitle').val() || !$('#itemContent').val()){
						Ext.example.msg('提示', '请输入内容');
						return ;
					}
					
					var subItems = '';
					
					if(multiFoodPriceCount > 0){
						for (var i = 1; i <= multiFoodPriceCount; i++) {
							var title = Ext.getCmp('subTitle'+i);
							var url = Ext.getCmp('subUrl'+i);
							var image = Ext.getCmp('subImgFile'+i);
							//过滤已经删除了的子选项
							if(title && url){
								if(subItems){
									subItems += '<ul>';
								}
								subItems += (title.getValue() + "<li>" + url.getValue() + "<li>" + (image.image?image.image:-1));						
							
							}
						}		
					}
					
					var dataSource = "insertImageText";
					
					var key = null;
					if(subscribeKey != -1){
						dataSource = "updateImageText";
						key = subscribeKey;
					}else if(tn && tn.attributes.key && !isNaN(tn.attributes.key)){
						dataSource = "updateImageText";
						key = tn.attributes.key;
					}
					
					$.ajax({ 
					    type : "post", 
					    async : false, 
					    url : basePath + "/wx-term/WXOperateMenu.do",
					    dataType : "jsonp",//jsonp数据类型 
					    jsonp: "jsonpCallback",//服务端用于接收callback调用的function名的参数 
					    data : {
					    	dataSource : dataSource,
					    	rid : rid,
					    	key : key != null ? key : "",
					    	title : $("#itemTitle").val(),
					    	image : p_box.image ? p_box.image : "",
					    	content : $("#itemContent").val(),
					    	url : $("#itemUrl").val(),
					    	subItems : subItems,
					    	subscribe : subscribe ? subscribe : ""
					    },
					    success : function(rt){ 
					        if(rt.success){
					        	if(dataSource == "insertImageText"){
					        		if(tn){
					        			tn.attributes.type = "click";
					        			tn.attributes.key = rt.other.key;
					        		}else{
					        			subscribeKey = rt.other.key;
					        			//显示删除
					        			$('#btnDeleteSubscribe').show();
					        		}
					        	}
							}
					        Ext.example.msg('提示', rt.msg);
					    }, 
					    error:function(xhr){ 
					        var rt = JSON.parse(xhr.responseText);
					        if(rt.success){
					        	if(dataSource == "insertImageText"){
					        		if(tn){
					        			tn.attributes.type = "click";
					        			tn.attributes.key = rt.other.key;
					        		}else{
					        			subscribeKey = rt.other.key;
					        			//显示删除
					        			$('#btnDeleteSubscribe').show();
					        		}
					        	}
							}
					        Ext.example.msg('提示', rt.msg);
					    } 
					}); 
				}
			}]	        
 	    },{
 	    	id : "tab_click",
 	        contentEl:'textReplyBox',
 	        title: '文字', 
 	        //iconCls : 'tab_home'
 	    },{
 	    	id : 'tab_view',
 	    	contentEl : 'urlReplyBox',
 	    	title:'连接'
 	    },{
 	    	id : 'tab_system',
 	    	contentEl : 'systemReplyBox',
 	    	title:'系统保留'
 	    }],
 	    listeners : {
 	    	tabchange : function(){
 	    		nodey = 0;
 	    	}
 	    },
 	    plugins: new Ext.ux.TabCloseMenu()
 	    
 	});
 	
 	
 	
 	centerPanel = new Ext.Panel({
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
            },treePanel,centerPanel
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
	
	//判断是否已设置自动关注
	$.ajax({ 
	    type : "post", 
	    url : basePath + "/wx-term/WXOperateMenu.do",
	    dataType : "jsonp",		//jsonp数据类型 
	    jsonp: "jsonpCallback",	//服务端用于接收callback调用的function名的参数 
	    data : {
	    	dataSource : 'subscribeReply',
	    	rid : rid
	    },
	    success : function(rt){ 
	        if(rt.success){
	        	$('#btnDeleteSubscribe').show();
	        }else{
	        	$('#btnDeleteSubscribe').hide();
	        }
	    },
	    error : function(xhr){
	    	var rt = JSON.parse(xhr.responseText);
	        if(rt.success){
	        	$('#btnDeleteSubscribe').show();
	        }else{
	        	$('#btnDeleteSubscribe').hide();
	        }
	    }
	});
	
});	


function optMultiPriceHandler(){
	++ multiFoodPriceCount; 
	var subTitleId = 'subTitle' + multiFoodPriceCount,  
		subUrlId = 'subUrl' + multiFoodPriceCount, 
		subImgFileId = 'subImgFile' + multiFoodPriceCount;
		subFormId = 'subForm' + multiFoodPriceCount;
	
	Ext.getCmp('food_multiPrice').add({
		cls : 'multiClass'+multiFoodPriceCount,
 		columnWidth : 1	 		
 	});								
	
	Ext.getCmp('food_multiPrice').add({
		cls : 'multiClass'+multiFoodPriceCount,
		columnWidth: 0.55,
		labelWidth : 40,
		defaults : {
			width : 300
		},
		items :[{
			xtype : 'textfield',
			id : subTitleId,
			fieldLabel : '标题',
			allowBlank : false
		},{
			xtype : 'textarea',
			id : subUrlId,
			fieldLabel : '链接'
		}]
	});	

	var sub_box = new Ext.BoxComponent({
		xtype : 'box',
 	    height : 55,
 	    autoEl : {
 	    	tag : 'img',
 	    	title : '图片预览'
 	    }
	});
	var sub_imgFile = Ext.ux.plugins.createImageFile({
		id : subImgFileId,
		formId : subFormId,
		img : sub_box,
		imgSize : 100,
		//打开图片后的操作
		uploadCallback : function(c){
			subImageOperate(c);
		}
	});
	
	var sub_form = new Ext.form.FormPanel({
		id : subFormId,
		labelWidth : 60,
		fileUpload : true,
		items : [sub_imgFile],
		listeners : {
    		render : function(e){
    			//Ext.getDom(e.getId()).setAttribute('enctype', 'multipart/form-data');
 	  		}
    	}
	});	
	
	Ext.getCmp('food_multiPrice').add({
		cls : 'multiClass'+multiFoodPriceCount,
		columnWidth: 0.35, 
		layout : 'column',
		frame : true,
		items : [sub_box, sub_form]	 		
 	});			
	
	Ext.getCmp('food_multiPrice').add({
		cls : 'multiClass'+multiFoodPriceCount,
 		columnWidth : .1,
 		items : [{
	    	xtype : 'button',
	    	text : '删除',
	    	style:'margin-top:40px;margin-left:10px;',
	    	multiIndex : multiFoodPriceCount,
	    	iconCls : 'btn_delete',
	    	handler : function(e){
	    		deleteMultiPriceHandler(e);
	    	}
 		}] 		 		
 	});		
	
	Ext.getCmp('food_multiPrice').doLayout();
	
	Ext.getCmp(subTitleId).focus();
	
}

function deleteMultiPriceHandler(e){
	var cmps = $('.multiClass'+Ext.getCmp(e.id).multiIndex);
	
	for (var i = 0; i < cmps.length; i++) {
		Ext.getCmp('food_multiPrice').remove(cmps[i].getAttribute("id"));
	}
	
	Ext.getCmp('food_multiPrice').doLayout();
}

function subImageOperate(c){
	var imgFile = Ext.getCmp(c.id);
	var check = true, img = '';
	if(Ext.isIE){
		Ext.getDom(imgFile.getId()).select();
		img = document.selection.createRange().text;
	}else{
     	img = Ext.getDom(c.id).value;
	}
	if(typeof(img) != 'undefined' && img.length > 0){
	        var type = img.substring(img.lastIndexOf('.') + 1, img.length);
	        check = false;
	        for(var i = 0; i < Ext.ux.plugins.imgTypes.length; i++){
	        	if(type.toLowerCase() == Ext.ux.plugins.imgTypes[i].toLowerCase()){
	        		check = true;
 	           	break;
 	        }
	        }
	        if(!check){
 	       	Ext.example.msg('提示', '图片类型不正确.');
 	        return;
     	}
	}else{
		Ext.example.msg('提示', '未选择图片.');
     	return;
	}
	var menu_uploadMask = new Ext.LoadMask(document.body, {
		msg : '正在上传图片...'
	});
	menu_uploadMask.show();
	Ext.Ajax.request({
		url : '../../OperateImage.do?dataSource=upload&ossType=10&rid='+rid,
			isUpload : true,
			form : Ext.getCmp(c.formId).getForm().getEl(),
			success : function(response, options){
				menu_uploadMask.hide();
				var jr = Ext.decode(response.responseText.replace(/<\/?[^>]*>/g,''));
				if(jr.success){
	   				var ossImage = jr.root[0];
	   				imgFile.image = ossImage.image;	   				
				}else{
					Ext.ux.showMsg(jr);
					imgFile.setImg("");
				}
			},
			failure : function(response, options){
				menu_uploadMask.hide();
				Ext.ux.showMsg(Ext.decode(response.responseText.replace(/<\/?[^>]*>/g,'')));
			}
	});	
}

function getSubscribeReply(){
	clearTabContent();
	centerPanel.setTitle('设置 -- 自动回复');
	//清除tree选中
	tree.getSelectionModel().clearSelections();
	subscribe = true;
	$.ajax({ 
	    type : "post", 
	    async : false, 
	    url : basePath + "/wx-term/WXOperateMenu.do",
	    dataType : "jsonp",		//jsonp数据类型 
	    jsonp: "jsonpCallback",	//服务端用于接收callback调用的function名的参数 
	    data : {
	    	dataSource : 'subscribeReply',
	    	rid : rid
	    },
	    success : function(rt){ 
	        if(rt.success){
	        	subscribeKey = rt.other.key;
	        	if(rt.root && rt.root.length > 0){
	    			var tab = tabs.getComponent("tab_image_text");
	    			tabs.setActiveTab(tab);
	        		
	        		var item = rt.root[0];
	        		
					$('#itemTitle').val(item.title);			
					$('#itemContent').val(item.description);	
					$('#itemUrl').val(item.url);	
	        		imgFile.setImg(item.picUrl);
	        		
	        		p_box.image = item.picUrl;
	        		
	        		//如果有子显示项
	        		if(rt.root.length > 1){
	        			multiFoodPriceCount = rt.root.length - 1;
	        			for (var i = 1; i < rt.root.length; i++) {
	        				var subTitleId = 'subTitle' + i,  
	        				subUrlId = 'subUrl' + i, 
	        				subImgFileId = 'subImgFile' + i;
	        				subFormId = 'subForm' + i;
	        			
		        			Ext.getCmp('food_multiPrice').add({
		        				cls : 'multiClass'+i,
		        		 		columnWidth : 1	 		
		        		 	});								
		        			
		        			Ext.getCmp('food_multiPrice').add({
		        				cls : 'multiClass'+i,
		        				columnWidth: 0.55,
		        				labelWidth : 40,
		        				defaults : {
		        					width : 300
		        				},
		        				items :[{
		        					xtype : 'textfield',
		        					id : subTitleId,
		        					fieldLabel : '标题',
		        					value : rt.root[i].title,
		        					allowBlank : false
		        				},{
		        					xtype : 'textarea',
		        					id : subUrlId,
		        					fieldLabel : '链接',
		        					value : rt.root[i].url
		        				}]
		        			});	

		        			var sub_box = new Ext.BoxComponent({
		        				xtype : 'box',
		        		 	    height : 55,
		        		 	    autoEl : {
		        		 	    	tag : 'img',
		        		 	    	title : '图片预览'
		        		 	    }
		        			});
		        			var sub_imgFile = Ext.ux.plugins.createImageFile({
		        				id : subImgFileId,
		        				formId : subFormId,
		        				img : sub_box,
		        				imgSize : 100,
		        				//打开图片后的操作
		        				uploadCallback : function(c){
		        					subImageOperate(c);
		        				}
		        			});
		        			
		        			var sub_form = new Ext.form.FormPanel({
		        				id : subFormId,
		        				labelWidth : 60,
		        				fileUpload : true,
		        				items : [sub_imgFile],
		        				listeners : {
		        		    		render : function(e){
		        		    			//Ext.getDom(e.getId()).setAttribute('enctype', 'multipart/form-data');
		        		 	  		}
		        		    	}
		        			});	
		        			
		        			Ext.getCmp('food_multiPrice').add({
		        				cls : 'multiClass'+i,
		        				columnWidth: 0.35, 
		        				layout : 'column',
		        				frame : true,
		        				items : [sub_box, sub_form]	 		
		        		 	});			
		        			
		        			Ext.getCmp('food_multiPrice').add({
		        				cls : 'multiClass'+i,
		        		 		columnWidth : .1,
		        		 		items : [{
		        			    	xtype : 'button',
		        			    	text : '删除',
		        			    	style:'margin-top:40px;margin-left:10px;',
		        			    	multiIndex : i,
		        			    	iconCls : 'btn_delete',
		        			    	handler : function(e){
		        			    		deleteMultiPriceHandler(e);
		        			    	}
		        		 		}] 		 		
		        		 	});	
		        			
		        			Ext.getCmp('food_multiPrice').doLayout();
		        			
		        			sub_imgFile.setImg(rt.root[i].picUrl);
		        			
		        			sub_imgFile.image = rt.root[i].picUrl;
						}
	        		}
	        		
	        	}else if(rt.other){
	    			var tab = tabs.getComponent("tab_click");
	    			tabs.setActiveTab(tab);
	    			
	        		$('#menuTxtReply').val(rt.other.text);
	        	}
			}else{
    			var tab = tabs.getComponent("tab_image_text");
    			tabs.setActiveTab(tab);
				Ext.getCmp('itemTitle').focus(true, 100);	
			}
	    }, 
	    error:function(xhr){ 
	        var rt = JSON.parse(xhr.responseText);
	        if(rt.success){
	        	subscribeKey = rt.other.key;
	        	if(rt.root && rt.root.length > 0){
	    			var tab = tabs.getComponent("tab_image_text");
	    			tabs.setActiveTab(tab);
	        		
	        		var item = rt.root[0];
	        		
					$('#itemTitle').val(item.title);			
					$('#itemContent').val(item.description);	
					$('#itemUrl').val(item.url);	
	        		imgFile.setImg(item.picUrl);
	        		
	        		p_box.image = item.picUrl;
	        		
	        		//如果有子显示项
	        		if(rt.root.length > 1){
	        			multiFoodPriceCount = rt.root.length - 1;
	        			for (var i = 1; i < rt.root.length; i++) {
	        				var subTitleId = 'subTitle' + i,  
	        				subUrlId = 'subUrl' + i, 
	        				subImgFileId = 'subImgFile' + i;
	        				subFormId = 'subForm' + i;
	        			
		        			Ext.getCmp('food_multiPrice').add({
		        				cls : 'multiClass'+i,
		        		 		columnWidth : 1	 		
		        		 	});								
		        			
		        			Ext.getCmp('food_multiPrice').add({
		        				cls : 'multiClass'+i,
		        				columnWidth: 0.55,
		        				labelWidth : 40,
		        				defaults : {
		        					width : 300
		        				},
		        				items :[{
		        					xtype : 'textfield',
		        					id : subTitleId,
		        					fieldLabel : '标题',
		        					value : rt.root[i].title,
		        					allowBlank : false
		        				},{
		        					xtype : 'textarea',
		        					id : subUrlId,
		        					fieldLabel : '链接',
		        					value : rt.root[i].url
		        				}]
		        			});	

		        			var sub_box = new Ext.BoxComponent({
		        				xtype : 'box',
		        		 	    height : 55,
		        		 	    autoEl : {
		        		 	    	tag : 'img',
		        		 	    	title : '图片预览'
		        		 	    }
		        			});
		        			var sub_imgFile = Ext.ux.plugins.createImageFile({
		        				id : subImgFileId,
		        				formId : subFormId,
		        				img : sub_box,
		        				imgSize : 100,
		        				//打开图片后的操作
		        				uploadCallback : function(c){
		        					subImageOperate(c);
		        				}
		        			});
		        			
		        			var sub_form = new Ext.form.FormPanel({
		        				id : subFormId,
		        				labelWidth : 60,
		        				fileUpload : true,
		        				items : [sub_imgFile],
		        				listeners : {
		        		    		render : function(e){
		        		    			//Ext.getDom(e.getId()).setAttribute('enctype', 'multipart/form-data');
		        		 	  		}
		        		    	}
		        			});	
		        			
		        			Ext.getCmp('food_multiPrice').add({
		        				cls : 'multiClass'+i,
		        				columnWidth: 0.35, 
		        				layout : 'column',
		        				frame : true,
		        				items : [sub_box, sub_form]	 		
		        		 	});			
		        			
		        			Ext.getCmp('food_multiPrice').add({
		        				cls : 'multiClass'+i,
		        		 		columnWidth : .1,
		        		 		items : [{
		        			    	xtype : 'button',
		        			    	text : '删除',
		        			    	style:'margin-top:40px;margin-left:10px;',
		        			    	multiIndex : i,
		        			    	iconCls : 'btn_delete',
		        			    	handler : function(e){
		        			    		deleteMultiPriceHandler(e);
		        			    	}
		        		 		}] 		 		
		        		 	});	
		        			
		        			Ext.getCmp('food_multiPrice').doLayout();
		        			
		        			sub_imgFile.setImg(rt.root[i].picUrl);
		        			
		        			sub_imgFile.image = rt.root[i].picUrl;
						}
	        		}
	        		
	        	}else if(rt.other){
	    			var tab = tabs.getComponent("tab_click");
	    			tabs.setActiveTab(tab);
	    			
	        		$('#menuTxtReply').val(rt.other.text);
	        	}
			}else{
    			var tab = tabs.getComponent("tab_image_text");
    			tabs.setActiveTab(tab);
				Ext.getCmp('itemTitle').focus(true, 100);	
			}
	    } 
	}); 	
}


function deleteSubscribeReply(){
	centerPanel.setTitle('设置 --');
	Ext.Msg.confirm(
		'提示',
		'是否删除自动回复',
		function(e){
			if(e == 'yes'){
				$.ajax({ 
				    type : "post", 
				    async:false, 
				    url : basePath+"/wx-term/WXOperateMenu.do",
				    dataType : "jsonp",		//jsonp数据类型 
				    jsonp: "jsonpCallback",	//服务端用于接收callback调用的function名的参数 
				    data : {
				    	dataSource : "deleteSubscribe",
				    	rid : rid
				    },
				    success : function(rt){ 
				    	Ext.example.msg('提示', rt.msg);
				    	if(rt.success){
				    		$('#btnDeleteSubscribe').hide();
				    	}
				    }, 
				    error:function(xhr){ 
				        var rt = JSON.parse(xhr.responseText);
				        Ext.example.msg('提示', rt.msg);
				    	if(rt.success){
				    		$('#btnDeleteSubscribe').hide();
				    	}
				    } 
				}); 	
				
				clearTabContent();
				
			}
		}
	);		
}
