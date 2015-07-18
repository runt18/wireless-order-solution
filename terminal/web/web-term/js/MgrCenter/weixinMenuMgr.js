

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
	var tn = Ext.ux.getSelNode(tree);
	Ext.Msg.confirm(
		'提示',
		'是否删除: ' + tn.text,
		function(e){
			if(e == 'yes'){
				if(tn.attributes.key && !isNaN(tn.attributes.key)){
					$.ajax({ 
					    type : "post", 
					    async:false, 
					    url : "../../OperateMenu.do",
					    data : {
					    	dataSource : "deleteMenu",
					    	rid : rid,
					    	key : tn.attributes.key
					    },
					    dataType : "json",//jsonp数据类型 
					    success : function(data){ 
					    	Ext.example.msg('提示', data.msg);
					    }, 
					    error:function(xhr){ 
					        var rt = JSON.parse(xhr.responseText);
					        Ext.example.msg('提示', rt.msg);
					    } 
					}); 	
				}
				
				tn.remove();
				
				$('#menuTxtReply').val("");			
				$('#url4Menu').val("");	
				
				$('#itemTitle').val("");			
				$('#itemContent').val("");	
				$('#itemUrl').val("");			
				delete p_box.ossId;	
				imgFile.setImg("");
				
			}
		}
	);	
	
	
}

/**
 * 保存文本回复
 */
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
	    url : "../../OperateMenu.do",
	    data : {
	    	dataSource : dataSource,
	    	rid : rid,
	    	key : tn.attributes.key,
	    	text : $('#menuTxtReply').val()
	    },
	    dataType : "json",//jsonp数据类型 
	    success : function(rt){ 
	        if(rt.success){
	        	if(dataSource == "insertMenu"){
	        		tn.attributes.type = "click";
	        		tn.attributes.key = rt.other.key;
	        	}
			}
	        Ext.example.msg('提示', rt.msg);
	    }, 
	    error:function(xhr){ 
	        var rt = JSON.parse(xhr.responseText);
	        if(rt.success){
	        	if(dataSource == "insertMenu"){
	        		tn.attributes.type = "click";
	        		tn.attributes.key = rt.other.key;
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
	    url :  basePath+"/wx-term/WXOperateMenu.do?dataSource=weixinMenu&rid="+rid,
	    jsonp: "jsonpCallback",//服务端用于接收callback调用的function名的参数 
	    dataType : "json",//jsonp数据类型
	    success : function(rt){
	    	weixinMenuLM.hide();
	    	var root ={
	 			text : 'root',
	 			children : []
	    	}
	    	
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
	    	
	    	tree.setRootNode(new Ext.tree.AsyncTreeNode(root));
	    }, 
	    error:function(xhr){
	    	weixinMenuLM.hide();
	    	var rt = JSON.parse(xhr.responseText);
	    	var root ={
	 			text : 'root',
	 			children : []
	    	}
	    	
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
	    	
	    	tree.setRootNode(new Ext.tree.AsyncTreeNode(root));
	    } 
	}); 	
}

Ext.onReady(function(){
	$.ajax({ 
	    type : "post", 
	    url : basePath+"/wx-term/WXOperateMenu.do",
	    data : {
	    	dataSource : 'systemMenu'
	    },
	    dataType : "jsonp",//jsonp数据类型 
	    jsonp: "jsonpCallback",//服务端用于接收callback调用的function名的参数 
	    success : function(data){ 
			if(data.success){
			}
	    }, 
	    error:function(xhr){ 
	        var rt = JSON.parse(xhr.responseText);
	        var systemMenuTemplate = '<input id="{id}" type="radio" name="systemSet" value={key}><label for="{id}">{desc}</label>';
	        
	        if(rt.success){
	        	var html = [];
	        	for (var i = 0; i < rt.root.length; i++) {
	        		html.push(systemMenuTemplate.format({
	        			id : "r"+(i+1),
	        			key : rt.root[i].key,
	        			desc : rt.root[i].desc
	        		}))
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
				        Ext.example.msg('提示', '发布成功');
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
				
				$('#itemTitle').val("");			
				$('#itemContent').val("");	
				$('#itemUrl').val("");			
				delete p_box.ossId;	
				imgFile.setImg("");
				
				$('input[name="systemSet"]:checked').removeAttr("checked");
				
				var tn = Ext.ux.getSelNode(tree);
				
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
					var p = Ext.getCmp('contentPanel');
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
						    async:false, 
						    url : "../../OperateMenu.do",
						    data : {
						    	dataSource : 'menuReply',
						    	rid : rid,
						    	key : tn.attributes.key
						    },
						    dataType : "json",//jsonp数据类型 
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
						        	}
								}
						    }, 
						    error:function(xhr){ 
						        var rt = JSON.parse(xhr.responseText);
						        Ext.example.msg('提示', rt.msg);
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
	
	
	var menu_uploadMask = new Ext.LoadMask(document.body, {
		msg : '正在上传图片...'
	});
	var p_box = new Ext.BoxComponent({
		xtype : 'box',
 	    height : 200,
 	    width : 300,
 	    style : 'marginRight:5px;',
 	    autoEl : {
 	    	tag : 'img',
 	    	title : '图片预览'
 	    }
	});

	var btnUpload = new Ext.Button({
		hidden : true,
        text : '上传图片',
        listeners : {
        	render : function(thiz){
        		thiz.getEl().setWidth(60, true);
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
// 	   					Ext.ux.showMsg(jr);
	  	   				var ossImage = jr.root[0];
	  	   				p_box.image = ossImage.image;
	  	   				p_box.ossId = ossImage.imageId;	   				
 	   				}else{
 	   					Ext.ux.showMsg(jr);
 	   					Ext.getCmp('couponTypeBox').setImg();
 	   				}

 	   				
 	   			},
 	   			failure : function(response, options){
 	   				menu_uploadMask.hide();
 	   				Ext.ux.showMsg(Ext.decode(response.responseText.replace(/<\/?[^>]*>/g,'')));
 	   			}
        	});
        }
	});	
	
	var imgFile = Ext.ux.plugins.createImageFile({
		id : 'replyBox',
		img : p_box,
		width : 300,
		height : 200,
		callback : function(){
			btnUpload.handler();
		}
	});		
	
	var form = new Ext.form.FormPanel({
		labelWidth : 60,
		fileUpload : true,
		items : [imgFile],
		listeners : {
	    	render : function(e){
	    		//Ext.getDom(e.getId()).setAttribute('enctype', 'multipart/form-data');
 	  		}
	    }
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
				title : '单图文',
				autoHeight : true,
				labelWidth : 40,
				defaults : {
					width : 250
				},
				width : 360,
				items : [ {
					xtype : 'textfield',
					id : 'itemTitle',
					fieldLabel : '标题',
					value : '标题',
					allowBlank : false
				}, {
					xtype : 'panel',
					width : 320,
					layout : 'column',
					style : 'marginLeft:18px;',
					frame : true,
					items : [p_box, form,{
							items : [{
								xtype : 'label',
								html : '&nbsp;&nbsp;'
							}]						
						},{
							items : [{
								xtype : 'label',
								style : 'width : 130px;',
								html : '<sapn style="font-size:13px;color:green;font-weight:bold">图片大小不能超过100K</span>'
							}]							
						},btnUpload],
					listeners : {
		 	    		render : function(e){
		 	    			Ext.getDom(e.getId()).setAttribute('enctype', 'multipart/form-data');
			 	  		}
		 	    	}	
				},{
					xtype : 'textfield',
					id : 'itemContent',
					fieldLabel : '内容',
					value : '内容',
					style : 'margin-top:5px'
				}, {
					xtype : 'textarea',
					id : 'itemUrl',
					value : 'www.baidu.com',
					fieldLabel : '链接'
				}]  				
			}, {
				xtype : 'button',
				text : '保存',
				width : 100,
				height : 20,
				handler : function(){
					var tn = Ext.ux.getSelNode(tree);
					if(!tn){
						Ext.example.msg('提示', '操作失败, 请选中一个菜单再进行操作.');
						return;
					}		
					if(!$('#itemTitle').val() || !$('#itemContent').val() || !$('#itemUrl').val()){
						Ext.example.msg('提示', '请输入内容');
						return ;
					}
					
					var dataSource = "insertImageText";
					if(tn.attributes.key && !isNaN(tn.attributes.key)){
						dataSource = "updateImageText";
					}	
					
					
					$.ajax({ 
					    type : "post", 
					    async:false, 
					    url : "../../OperateMenu.do",
					    data : {
					    	dataSource : dataSource,
					    	rid : rid,
					    	key : tn.attributes.key,
					    	title : $("#itemTitle").val(),
					    	image : p_box.ossId ? p_box.ossId : "",
					    	content : $("#itemContent").val(),
					    	url : $("#itemUrl").val()
					    },
					    dataType : "json",//jsonp数据类型 
					    success : function(rt){ 
					        if(rt.success){
					        	if(dataSource == "insertImageText"){
					        		tn.attributes.type = "click";
					        		tn.attributes.key = rt.other.key;
					        	}
							}
					        Ext.example.msg('提示', rt.msg);
					    }, 
					    error:function(xhr){ 
					        var rt = JSON.parse(xhr.responseText);
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