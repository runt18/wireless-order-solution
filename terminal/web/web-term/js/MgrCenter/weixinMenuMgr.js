Ext.onReady(function(){
	
	var systemCheckbox = {
			SELF_ORDER_EVENT_KEY : {val : "self_order_event_key", text : "自助点餐", image : 'http://digie-image-real.oss-cn-hangzhou.aliyuncs.com/WxReplySample/%E8%87%AA%E5%8A%A9%E7%82%B9%E9%A4%90.png'},
			SELF_BOOK_EVENT_KEY : {val : "self_book_event_key", text : "自助预订", image : 'http://digie-image-real.oss-cn-hangzhou.aliyuncs.com/WxReplySample/%E8%87%AA%E5%8A%A9%E9%A2%84%E8%AE%A2.png'},
			INTRO_EVENT_KEY : {val : "intro_event_key", text : "餐厅简介", image : 'http://digie-image-real.oss-cn-hangzhou.aliyuncs.com/WxReplySample/%E9%A4%90%E5%8E%85%E7%AE%80%E4%BB%8B.jpg'},
			STAR_EVENT_KEY : {val : "star_event_key", text : "明星菜品", image : ''},
			NAVI_EVENT_KEY : {val : "navi_event_key", text : "餐厅导航", image : 'http://digie-image-real.oss-cn-hangzhou.aliyuncs.com/WxReplySample/%E9%A4%90%E5%8E%85%E5%AF%BC%E8%88%AA.png'},
			PROMOTION_EVENT_KEY : {val : "promotion_event_key", text : "优惠活动", image : ''},
			MEMBER_EVENT_KEY : {val : "member_event_key", text : "我的会员卡", image : ''},
			ORDER_EVENT_KEY : {val : "order_event_key", text : "我的订单", image : 'http://digie-image-real.oss-cn-hangzhou.aliyuncs.com/WxReplySample/%E6%88%91%E7%9A%84%E8%AE%A2%E5%8D%95.png'},
			MY_QRCODE_EVENT_KEY : {val : "my_qrcode_event_key", text : "我的二维码", image : 'http://digie-image-real.oss-cn-hangzhou.aliyuncs.com/WxReplySample/%E6%88%91%E7%9A%84%E4%BA%8C%E7%BB%B4%E7%A0%81.png'},
			SCAN_EVENT_KEY : {val : "scan_event_key", text : "扫一扫"}
//			SCAN_EVENT_KEY : {val : "scancode_push", text : "扫码"}
	};


	
	var rid = restaurantID;
	//FIXME
	var basePath;
	if(window.location.hostname == 'e-tones.net' || window.location.hostname == 'lb.e-tones.net'){
		basePath = 'http://wx.e-tones.net';
	}else if(window.location.hostname == 'ts.e-tones.net'){
		basePath = 'http://ts.e-tones.net';
	}else if(window.location.hostname == 'localhost'){
		basePath = 'http://localhost:8080'
	}else{
		basePath = 'http://' + window.location.hostname;
	}
	//关键字点击的标识符 	
	var isKeyword = false;
	
	//悬浮操作内容
	var multiFoodPriceCount = 0, subscribe = false, subscribeKey = -1;
	
	//界面的panel1
	var northPanel = new Ext.Panel({
		contentEl : 'menuInit_div_weixin',
		region : 'north',
		border : true	
	});
	
	//记载菜单的loading
	var weixinMenuLM = new Ext.LoadMask(document.body, {
		msg  : '正在加载菜单......'
	});
	
	
	//文字回复保存
	$('#textReplySave_input_weixin').click(function(){
		if(isKeyword){
			var tn = Ext.ux.getSelNode(keywordTree);
		}else{
			var tn = Ext.ux.getSelNode(tree);
		}
		
		if(!tn && !subscribe){
			Ext.example.msg('提示', '操作失败, 请选中一个菜单再进行操作.');
			return;
		}		
		if(!$('#menuTxtReply_textarea_weixin').val()){
			Ext.example.msg('提示', '请输入内容');
			return ;
		}
		
		var key = null;
		var dataSource = "insertText";
		if(subscribeKey != -1){
			dataSource = "updateText";
			key = subscribeKey;
		}else if(tn && tn.attributes.key && tn.attributes.key !=-1 && !isNaN(tn.attributes.key)){
			dataSource = "updateText";
			key = tn.attributes.key;
		}else if(tn && tn.attributes.actionId && tn.attributes.actionId != 0){
			dataSource = "updateText";
			key = tn.attributes.actionId;
		}	
		
		$.ajax({ 
		    type : "post", 
		    async : false, 
		    url : '../../OperateReply.do',
		    dataType : "json",		//jsonp数据类型 
		    data : {
		    	dataSource : dataSource,
		    	rid : rid,
		    	key : key != null ? key : "",
		    	text : $('#menuTxtReply_textarea_weixin').val(),
		    	subscribe : subscribe ? subscribe : ""
		    },
		    success : function(rt){ 
		        if(rt.success){
		        	if(isKeyword){
		        		$.ajax({
			        		url : '../../OperateKeyword.do',
			        		dataType : 'json',
			        		type : 'post',
			        		data : {
			        			dataSource : 'update',
			        			id : tn.attributes.keywordId,
			        			actionId : rt.other ? rt.other.key : tn.attributes.actionId
			        		},
			        		success : function(data){
			        			 keywordTree.getRootNode().reload();
			        			 Ext.example.msg('提示', rt.msg);
			        		}
			        	});
		        	}else{
		        		if(dataSource == "insertText"){
			        		if(tn){
			        			tn.attributes.type = "click";
			        			tn.attributes.key = rt.other.key;
			        		}else{
			        			subscribeKey = rt.other.key;
			        			//显示删除
			        			Ext.getCmp('delAutoReply_btn_weixin').enable();
			        			//隐藏添加
			        			Ext.getCmp('addAutoReply_btn_weixin').disable();
			        			$('#getSubscribe_a_weixin').show();
			        		}
			        	}
		        		Ext.example.msg('提示', rt.msg);
		        	}
				}
		        
		    }, 
		    error:function(xhr){  
		        var rt = JSON.parse(xhr.responseText);
		        Ext.example.msg('提示', rt.msg);
		    } 
		}); 
	});
	
	//链接回复保存
	$('#urlReply_input_weixin').click(function(){
		var tn = Ext.ux.getSelNode(tree);
		if(!tn){
			Ext.example.msg('提示', '操作失败, 请选中一个菜单再进行操作.');
			return;
		}		
		if(!$('#urlTextarea_textarea_weixin').val()){
			Ext.example.msg('提示', '请输入内容');
			return ;
		}	
		
		tn.attributes.type = "view";
		tn.attributes.url = $('#urlTextarea_textarea_weixin').val();
		
		Ext.example.msg('提示', '操作成功');
	});
	
	//添加自动回复
	$('#getSubscribe_a_weixin').click(function(){
		clearTabContent();
		isKeyword = false;
		centerPanel.setTitle('设置 -- 自动回复');
		tabs.enable();
		Ext.getCmp('tabView_tab_weixin').disable();
		Ext.getCmp('tabSystem_tab_weixin').disable();
		//清除tree选中
		tree.getSelectionModel().clearSelections();
		//清除tree选中
		keywordTree.getSelectionModel().clearSelections();
		
		subscribe = true;
		$.ajax({ 
		    type : "post", 
		    async : false, 
		    url : "../../OperateReply.do",
		    dataType : "json",		//jsonp数据类型 
		    data : {
		    	dataSource : 'subscribeReply',
		    	rid : rid
		    },
		    success : function(rt){ 
		        if(rt.success){
		        	subscribeKey = rt.other.key;
		        	if(rt.root && rt.root.length > 0){
		    			var tab = tabs.getComponent("tabImageText_tab_weixin");
		    			tabs.setActiveTab(tab);
		        		
		        		var item = rt.root[0];
		        		
						$('#itemTitle_textfield_weixin').val(item.title);			
						$('#itemContent_textfield_weixin').val(item.description);	
						$('#itemUrl_textarea_weixin').val(item.url);	
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
		        			
			        			Ext.getCmp('foodMultiPrice_column_weixin').add({
			        				cls : 'multiClass'+i,
			        		 		columnWidth : 1	 		
			        		 	});								
			        			
			        			Ext.getCmp('foodMultiPrice_column_weixin').add({
			        				cls : 'multiClass'+i,
			        				columnWidth: 0.4,
			        				labelWidth : 40,
			        				defaults : {
			        					width : 250
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
			        			
			        			var sub_btnUpload = new Ext.Button({
			        				hidden : true,
			        				id : 'sub_btnWeixinReplyUploadImage',
			        		        text : '上传图片',
			        		        listeners : {
			        		        	render : function(thiz){
			        		        		thiz.getEl().setVisible(false);
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
			        		 	   			form : sub_form.getForm().getEl(),
			        		 	   			success : function(response, options){
			        		 	   				menu_uploadMask.hide();
			        		 	   				var jr = Ext.decode(response.responseText.replace(/<\/?[^>]*>/g,''));
			        		 	   				if(jr.success){
			        			  	   				var ossImage = jr.root[0];
			        			  	   				sub_box.image = ossImage.image;
			        			  	   				sub_box.imageId = ossImage.imageId;	   				
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
			        		    			Ext.getDom(e.getId()).setAttribute('enctype', 'multipart/form-data');
			        		 	  		}
			        		    	},
			        				buttons : [sub_btnUpload]
			        			});		
			        			
			        			Ext.getCmp('foodMultiPrice_column_weixin').add({
			        				cls : 'multiClass'+i,
			        				columnWidth: 0.35,  
			        				layout : 'column',
			        				frame : true,
			        				items : [sub_box, sub_form]	 		
			        		 	});			
			        			
			        			Ext.getCmp('foodMultiPrice_column_weixin').add({
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
			        			
			        			Ext.getCmp('foodMultiPrice_column_weixin').doLayout();
			        			
			        			sub_imgFile.setImg(rt.root[i].picUrl);
			        			
			        			sub_imgFile.image = rt.root[i].picUrl;
							}
		        		}
		        		
		        	}else if(rt.other){
		    			var tab = tabs.getComponent("tabClick_tab_weixin");
		    			tabs.setActiveTab(tab);
		    			
		        		$('#menuTxtReply_textarea_weixin').val(rt.other.text);
		        	}
				}else{
	    			var tab = tabs.getComponent("tabImageText_tab_weixin");
	    			tabs.setActiveTab(tab);
					Ext.getCmp('itemTitle_textfield_weixin').focus(true, 100);	
				}
		    }, 
		    error:function(xhr){ 
		    	Ext.example.msg(xhr.responseText);
		    }
		});
	});
	
	//系统设置的保存按钮
	$('#setSystemMenu_input_weixin').click(function(){
		var tn = Ext.ux.getSelNode(tree);
		if(!tn){
			Ext.example.msg('提示', '操作失败, 请选中一个菜单再进行操作.');
			return;
		}	
		
		var key = $('input[name="systemSet"]:checked').val();
		
		tn.attributes.type = "click";
		if(key == "scan_event_key"){
//			tn.attributes.type = "scancode_waitmsg";
			tn.attributes.type = "scancode_push";
		}
		tn.attributes.key = key;
		
		Ext.example.msg('提示', '设置成功');
	});
	
	//菜单管理的悬浮条
	var menuTree_obj = {treeId : 'weixinMenuTree', option : [ {name:'修改', fn : floatBarUpdateHandler }, {name:'删除', fn : deleteMenu}]};
	
	
	//修改悬浮栏function
	function floatBarUpdateHandler(){
		var tn = Ext.ux.getSelNode(tree);
		if(!tn){
			Ext.example.msg('提示', '操作失败, 请选中一个菜单再进行操作.');
			return;
		}
		updateDeptWin.otype = 'update';
		operateDeptHandler(tn);
	}
	
	//删除function
	function deleteMenu(){
//		var s = tree.getSelectionModel().getSelectedNode();
//		tree.root.removeChild(s);
		var tn = Ext.ux.getSelNode(tree);
		Ext.Msg.confirm(
			'提示',
			'是否删除: ' + tn.text,
			function(e){
				if(e == 'yes'){
					if(tn.attributes.key != -1 && !isNaN(tn.attributes.key)){
						$.ajax({ 
						    type : "post", 
						    async : false,
						    url : "../../OperateReply.do",
						    data : {
						    	dataSource : 'delete',
							    rid : rid,
							    key : tn.attributes.key
						    },
						    dataType : "json",		//jsonp数据类型 
						    success : function(data){ 
					    		Ext.example.msg('提示', data.msg);
						    }, 
						    error : function(xhr){ 
						        var rt = JSON.parse(xhr.responseText);
						        Ext.example.msg('提示', rt.msg);
						    }
						}); 	
					}else{
						Ext.example.msg('提示', "删除成功");
					}
					tn.remove();
					clearTabContent();
				}
			}
		);	
	}
	
	
	//关键字管理的悬浮
	var keywordTree_obj = {treeId : 'weixinkeyWord_tree_weixin', option : [{name: '修改', fn : updataKeyword}, {name : '删除', fn : delKeyword}]};
	
	//修改关键字
	function updataKeyword(){
		updateKeywordWin.type = 'update';
		updateKeywordWin.setTitle('修改关键字');
		var tn = Ext.ux.getSelNode(keywordTree);
		updateKeywordWin.show();
		Ext.getCmp('txtKeywordName_textfield_weixin').setValue(tn.text);
		Ext.getCmp('keyWordID_hidden_weixin').setValue(tn.attributes.keywordId);
		Ext.getCmp('txtKeywordName_textfield_weixin').focus(true, 100);
		updateKeywordWin.center();		
	}
	
	//删除关键字
	function delKeyword(){
		var tn = Ext.ux.getSelNode(keywordTree);
		Ext.Msg.confirm(
			'提示',
			'是否删除: ' + tn.text,
			function(e){
				if(e == 'yes'){
					$.ajax({ 
					    type : "post", 
					    url : "../../OperateKeyword.do",
					    data : {
					    	dataSource : 'deleteByCond',
					    	id : tn.attributes.keywordId
					    },
					    dataType : "json",		//jsonp数据类型 
					    success : function(data){ 
					    		//刷新
							 keywordTree.getRootNode().reload();
							 if(tn.attributes.type == 2){
								 Ext.example.msg('提示', '例外回复不能删除');
							 }else{
								 Ext.example.msg('提示', data.msg);
							 }
							 
							 
					    }, 
					    error : function(xhr){ 
					        var rt = JSON.parse(xhr.responseText);
					        Ext.example.msg('提示', rt.msg);
					    }
					}); 	
				tn.remove();
				clearTabContent();
			}
		});	
	}
	
	$.ajax({ 
	    type : 'post', 
	    url : basePath + "/wx-term/WxOperateMenu.do",
	    data : {
	    	dataSource : 'systemMenu'
	    },
	    dataType : "jsonp",//jsonp数据类型 
	    jsonp: "callback",//服务端用于接收callback调用的function名的参数 
	    jsonpCallback : "jsonpCallback4SystemMenu",
	    success : function(data){ 
	    	var systemMenuTemplate = '<div style="float:left;"><input id={id} type="radio" name="systemSet" value={key}><label for={id}>{desc}</label><button style="font-size:12px;margin-left:2px;font-weight: bold;" value={key} data-type="getUrlByKey_weixinMenu">地址</button><div><br>';
	    	function format(str, args){
	    		var result = str;
	            for(var key in args) {
	                if(args[key] != undefined){
	                    var reg = new RegExp("({" + key + "})", "g");
	                    result = result.replace(reg, args[key]);
	                }
	            }
			    return result;
	    	}
	    	if(data.success){
	        	var html = [];
	        	for (var i = 0; i < data.root.length; i++) {
	        		html.push(format(systemMenuTemplate, 
	        		{
	        			id : "r"+(i+1),
	        			key : data.root[i].key,
	        			desc : data.root[i].desc
	        		}));
				}
	        	$("#systemReplyBox_th_weixin").html(html.join("")).trigger('create').trigger('refresh');
	        	
	        	
	        	$("#systemReplyBox_th_weixin").find('input[name="systemSet"]').each(function(index, element){
	        		element.onclick = function(){
	        			
	        			if($(element).attr('value') == systemCheckbox.SELF_ORDER_EVENT_KEY.val){//自助点餐
	        				$("#systemReplyImg_th_weixin").attr("style","background:url("+ systemCheckbox.SELF_ORDER_EVENT_KEY.image +") no-repeat;width:100%;height:128px;");
	        			}else if($(element).attr('value') == systemCheckbox.SELF_BOOK_EVENT_KEY.val){//自助预订
	        				$("#systemReplyImg_th_weixin").attr("style","background:url("+ systemCheckbox.SELF_BOOK_EVENT_KEY.image +") no-repeat;width:100%;height:128px;");
	        			}else if($(element).attr('value') == systemCheckbox.INTRO_EVENT_KEY.val){//餐厅简介
	        				$("#systemReplyImg_th_weixin").attr("style","background:url("+ systemCheckbox.INTRO_EVENT_KEY.image +") no-repeat;width:100%;height:128px;");
	        			}else if($(element).attr('value') == systemCheckbox.STAR_EVENT_KEY.val){//明星菜品
	        				$("#systemReplyImg_th_weixin").attr("style","background:url("+ systemCheckbox.STAR_EVENT_KEY.image +") no-repeat;width:100%;height:128px;");
	        			}else if($(element).attr('value') == systemCheckbox.NAVI_EVENT_KEY.val){//餐厅导航
	        				$("#systemReplyImg_th_weixin").attr("style","background:url("+ systemCheckbox.NAVI_EVENT_KEY.image +") no-repeat;width:100%;height:128px;");
	        			}else if($(element).attr('value') == systemCheckbox.PROMOTION_EVENT_KEY.val){//优惠活动
	        				$("#systemReplyImg_th_weixin").attr("style","background:url("+ systemCheckbox.PROMOTION_EVENT_KEY.image +") no-repeat;width:100%;height:128px;");
	        			}else if($(element).attr('value') == systemCheckbox.MEMBER_EVENT_KEY.val){//我的会员卡
	        				$("#systemReplyImg_th_weixin").attr("style","background:url("+ systemCheckbox.MEMBER_EVENT_KEY.image +") no-repeat;width:100%;height:128px;");
	        			}else if($(element).attr('value') == systemCheckbox.ORDER_EVENT_KEY.val){//我的订单
	        				$("#systemReplyImg_th_weixin").attr("style","background:url("+ systemCheckbox.ORDER_EVENT_KEY.image +") no-repeat;width:100%;height:128px;");
	        			}else if($(element).attr('value') == systemCheckbox.MY_QRCODE_EVENT_KEY.val){//我的二维码
	        				$("#systemReplyImg_th_weixin").attr("style","background:url("+ systemCheckbox.MY_QRCODE_EVENT_KEY.image +") no-repeat;width:100%;height:128px;");
	        			}else{//扫一扫
	        				$("#systemReplyImg_th_weixin").attr("style","background:url("+ systemCheckbox.SCAN_EVENT_KEY.image +") no-repeat;width:100%;height:128px;");
	        			}
	        			
	        		}
	        	});
	        	
	        	function showMsgWin(msg){
	        		var showMsgWin;
	        		showMsgWin = new Ext.Window({
	        			width : 400,
	        			height : 200,
	        			resizable : false,
						modal : true,
						closable : false,
						items : [{
							xtype : 'textarea',
							width : 400,
							height : 200,
							value : msg
						}],
						bbar : ['->', {
							text : '确认',
							iconCls : 'btn_save',
							handler : function(){
								showMsgWin.hide();
								$(showMsgWin.id).remove();
							}
						}]
	        		});
	        		showMsgWin.render(document.body);
	        		showMsgWin.show();
	        	}
	        	
	        	$('[data-type=getUrlByKey_weixinMenu]').click(function(){
//	        		alert(this.value);
	        		$.ajax({
	        			url : basePath + "/wx-term/QueryWxRediect.do",
	        			type : 'post',
	        			dataType : "jsonp",
	        			jsonpCallback : "callback",
	        			data : {
	        				key : this.value,
	        				dataSource : 'getUrlJumpByKey'
	        			},
	        			success : function(res, status, req){
	        				if(res.success){
	        					showMsgWin(res.root[0].url);
	        				}else{
	        					Ext.ux.showMsg({
	        						title : '错误提示',
	        						msg : res.msg,
	        						code : '0000'
	        					});
	        				}
	        			},
	        			error : function(req, status, err){
	        				Ext.ux.showMsg({
	        					title : '错误提示',
	        					msg : err,
	        					code : '0000'
	        				});
	        			}
	        		});
	        	});
	        	
	        }
	    }, 
	    error : function(xhr){ 
	        var rt = JSON.parse(xhr.responseText);
	        Ext.example.msg('提示', rt.msg);
	    } 
	}); 
	
	
	var programTreeTbar = new Ext.Toolbar({
		items : [{
			text : '添加父菜单',
			iconCls : 'btn_add',
			handler : function(){
				updateDeptWin.otype = 'insert';
				operateDeptHandler();
			}
		},{
			text : '添加子菜单',
			iconCls : 'btn_add',
			handler : function(){
					var tn = Ext.ux.getSelNode(tree);
					if(!tn || typeof tn.attributes.children == 'undefined'){
						Ext.example.msg('提示', '操作失败, 请选中一个父菜单再进行操作.');
						return;
						
					}
					updateDeptWin.otype = 'addChild';
					operateDeptHandler(tn);
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
					var btn;

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
							if(sonBtn){
								btn.sub_button.list.push(sonBtn);
							}
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
				    url : basePath+"/wx-term/WxOperateMenu.do",
				    data : {
				    	dataSource : 'commitMenu',
				    	rid :rid,
				    	menu : encodeURI(JSON.stringify(menu))
				    },
				    dataType : "jsonp",		//jsonp数据类型 
				    jsonp : 'callback',
				    jsonpCallback : 'jsonpCallback',
				    success : function(data){ 
						if(data.success){
							Ext.example.msg('提示', '发布成功');
						}else{
							 Ext.example.msg('提示', '发布失败');
						}
				    }, 
				    error:function(xhr, e){ 
				        JSON.parse(xhr.responseText);
				        Ext.example.msg('提示', '发布失败');
				    } 
				}); 
				
			}
		}]
	});	
	
	//菜单tree的生成
	var tree;
	tree = new Ext.tree.TreePanel({
		title : '菜单管理',
		region : 'north',
		id : 'weixinMenuTree',
		width : 270,
		height : 230,
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
 			children : []
		}) ,
		loader : new Ext.tree.TreeLoader(),
		listeners : {
			render : function(){
				weixinMenuLM.show();
				$.ajax({ 
				    type : "post", 
				    url :  basePath + '/wx-term/WxOperateMenu.do?',
				    data : {
				    	dataSource : 'weixinMenu',
				    	rid : rid
				    },
				    jsonp: "callback",	//服务端用于接收callback调用的function名的参数
				    jsonpCallback : 'jsonpCallback',
				    dataType : "jsonp",		//jsonp数据类型
				    success : function(rt){
				    	weixinMenuLM.hide();
				    	var root = {
				 			text : 'root',
				 			children : []
				    	};
				    	
				    	var menu = rt.root[0];
				    	
				    	for (var i = 0; i < menu.button.length; i++){
							var btn = {
				 				text : menu.button[i].name,
				 				type : menu.button[i].type,
				 				url : menu.button[i].url,
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
			},
			click : function(e){
				clearTabContent();
				//清除tree选中
				isKeyword = false;
				keywordTree.getSelectionModel().clearSelections();
				Ext.getCmp('tabView_tab_weixin').enable();
				Ext.getCmp('tabSystem_tab_weixin').enable();
				var tn = Ext.ux.getSelNode(tree);
				centerPanel.setTitle('设置 -- ' +tn.text);
				
				if(tn.hasChildNodes()){
					tabs.disable();
					return;
				}else{
					tabs.enable();
				}
				
				if(tn.attributes.type == "view"){
					var tab = tabs.getComponent("tabView_tab_weixin");
					tabs.setActiveTab(tab);
					$('#urlTextarea_textarea_weixin').val(tn.attributes.url);
				}else if(tn.attributes.type == "click"){
					//判断key是否为数字
					if(isNaN(parseInt(tn.attributes.key))){
						var tab = tabs.getComponent("tabSystem_tab_weixin");
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
						    url : "../../OperateReply.do",
						    dataType : "json",			//jsonp数据类型 
						    data : {
						    	dataSource : 'menuReply',
						    	rid : rid,
						    	key : tn.attributes.key
						    },
						    success : function(rt){ 
						        if(rt.success){
						        	if(rt.other){
						        		$('#menuTxtReply_textarea_weixin').val(rt.other.text);
						        		
										var tab = tabs.getComponent("tabClick_tab_weixin");
										tabs.setActiveTab(tab);
						        	}else if(rt.root.length > 0){
						        		var item = rt.root[0];
						        		
										$('#itemTitle_textfield_weixin').val(item.title);			
										$('#itemContent_textfield_weixin').val(item.description);	
										$('#itemUrl_textarea_weixin').val(item.url);	
						        		imgFile.setImg(item.picUrl);
						        		p_box.image = item.picUrl;
										var tab = tabs.getComponent("tabImageText_tab_weixin");
										tabs.setActiveTab(tab);
										
						        		//如果有子显示项
						        		if(rt.root.length > 1){
						        			multiFoodPriceCount = rt.root.length - 1;
						        			for (var i = 1; i < rt.root.length; i++) {
						        				var subTitleId = 'subTitle' + i,  
						        				subUrlId = 'subUrl' + i, 
						        				subImgFileId = 'subImgFile' + i;
						        				subFormId = 'subForm' + i;
						        			
							        			Ext.getCmp('foodMultiPrice_column_weixin').add({
							        				cls : 'multiClass'+i,
							        		 		columnWidth : 1	 		
							        		 	});								
							        			
							        			Ext.getCmp('foodMultiPrice_column_weixin').add({
							        				cls : 'multiClass'+i,
							        				columnWidth: 0.4,
							        				labelWidth : 40,
							        				defaults : {
							        					width : 250
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
							        			
							        			var sub_btnUpload = new Ext.Button({
							        				hidden : true,
							        				id : 'sub_btnWeixinReplyUploadImage',
							        		        text : '上传图片',
							        		        listeners : {
							        		        	render : function(thiz){
							        		        		thiz.getEl().setVisible(false);
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
							        		 	   			form : sub_form.getForm().getEl(),
							        		 	   			success : function(response, options){
							        		 	   				menu_uploadMask.hide();
							        		 	   				var jr = Ext.decode(response.responseText.replace(/<\/?[^>]*>/g,''));
							        		 	   				if(jr.success){
							        			  	   				var ossImage = jr.root[0];
							        			  	   				sub_box.image = ossImage.image;
							        			  	   				sub_box.imageId = ossImage.imageId;	   				
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
							        		    			Ext.getDom(e.getId()).setAttribute('enctype', 'multipart/form-data');
							        		 	  		}
							        		    	},
							        				buttons : [sub_btnUpload]
							        			});	
							        			
							        			Ext.getCmp('foodMultiPrice_column_weixin').add({
							        				cls : 'multiClass'+i,
							        				columnWidth: 0.35, 
							        				layout : 'column',
							        				frame : true,
							        				items : [sub_box, sub_form]	 		
							        		 	});			
							        			
							        			Ext.getCmp('foodMultiPrice_column_weixin').add({
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
							        			
							        			sub_imgFile.image = rt.root[i].picUrl;
							        			Ext.getCmp('foodMultiPrice_column_weixin').doLayout();
							        			
							        			sub_imgFile.setImg(rt.root[i].picUrl);
											}
						        		}

						        	}
								}
						    }, 
						    error:function(xhr){ 
						        var rt = JSON.parse(xhr.responseText);
						        Ext.example.msg('提示', rt.msg);
						    } 
						}); 						
					}
					

				}else{
					var tab = tabs.getComponent("tabSystem_tab_weixin");
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
	
	
	//关键字回复的添加关键字按钮
	var keywordTbar = new Ext.Toolbar({
		items : ['->', {
			text : '添加关键字',
			iconCls : 'btn_add',
			handler : function(){
				updateKeywordWin.type = 'insert';
				updateKeywordWin.setTitle('添加关键字');
				updateKeywordWin.show();
				Ext.getCmp('txtKeywordName_textfield_weixin').setValue('');
				Ext.getCmp('txtKeywordName_textfield_weixin').focus(true, 100);
				updateKeywordWin.center();
			}
		}
		]
	});	
	
	//关键字tree
	var keywordTree;
	keywordTree = new Ext.tree.TreePanel({ 
		title : '关键字管理',
		region : 'south',
		id : 'weixinkeyWord_tree_weixin',
		width : 270,
		height : 300,
		border : false,
		rootVisible : true,
		frame : true,
		autoScroll : true,
		bodyStyle : 'backgroundColor:#FFFFFF; border:1px solid #99BBE8;padding-left: 10px;',
		loader : new Ext.tree.TreeLoader({
			dataUrl : '../../OperateKeyword.do',
			baseParams : {
				dataSource : 'getByCond',
				tree : true
			}
		}),
		root : new Ext.tree.AsyncTreeNode({
			expanded : true,
			text : '全部类型',
	        leaf : false,
	        border : true,
	        listeners : {
	        	expand : function(thiz){
	        		var rn = keywordTree.getRootNode().childNodes;
	        		var node = null;
	        		for(var i = (rn.length - 1); i >= 0; i--){
	        			if(eval(rn[i].attributes.discountID > maxDiscountID)){
							node = rn[i];
						}
					}
	        		if(node != null){
	        			node.select();
	        			node.fireEvent('click', node);
						node.fireEvent('dblclick', node);
					}
	        	}
	        }
		}) ,
		tbar : keywordTbar,
		listeners : {
			load : function(thiz){
				keywordTree.getRootNode().getUI().show();
			},
			click : function(){
				clearTabContent();
				//清除tree选中
				tree.getSelectionModel().clearSelections();
				
				isKeyword = true;
				
				Ext.getCmp('tabView_tab_weixin').disable();
				Ext.getCmp('tabSystem_tab_weixin').disable();
				var tn = Ext.ux.getSelNode(keywordTree);
				centerPanel.setTitle('设置 -- ' +tn.text);
				
				if(tn.hasChildNodes()){
					tabs.disable();
					return;
				}else{
					tabs.enable();
				}
				
				if(tn.attributes.actionId != 0){
					$.ajax({ 
					    type : "post", 
					    async : false, 
					    url : "../../OperateReply.do",
					    dataType : "json",			//jsonp数据类型 
					    data : {
					    	dataSource : 'menuReply',
					    	rid : rid,
					    	key : tn.attributes.actionId
					    },
					    success : function(rt){ 
					        if(rt.success){
					        	if(rt.other){
					        		$('#menuTxtReply_textarea_weixin').val(rt.other.text);
					        		
									var tab = tabs.getComponent("tabClick_tab_weixin");
									tabs.setActiveTab(tab);
					        	}else if(rt.root.length > 0){
					        		var item = rt.root[0];
					        		
									$('#itemTitle_textfield_weixin').val(item.title);			
									$('#itemContent_textfield_weixin').val(item.description);	
									$('#itemUrl_textarea_weixin').val(item.url);	
					        		imgFile.setImg(item.picUrl);
					        		p_box.image = item.picUrl;
									var tab = tabs.getComponent("tabImageText_tab_weixin");
									tabs.setActiveTab(tab);
									
					        		//如果有子显示项
					        		if(rt.root.length > 1){
					        			multiFoodPriceCount = rt.root.length - 1;
					        			for (var i = 1; i < rt.root.length; i++) {
					        				var subTitleId = 'subTitle' + i,  
					        				subUrlId = 'subUrl' + i, 
					        				subImgFileId = 'subImgFile' + i;
					        				subFormId = 'subForm' + i;
					        			
						        			Ext.getCmp('foodMultiPrice_column_weixin').add({
						        				cls : 'multiClass'+i,
						        		 		columnWidth : 1	 		
						        		 	});								
						        			
						        			Ext.getCmp('foodMultiPrice_column_weixin').add({
						        				cls : 'multiClass'+i,
						        				columnWidth: 0.4,
						        				labelWidth : 40,
						        				defaults : {
						        					width : 250
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
						        			
						        			var sub_btnUpload = new Ext.Button({
						        				hidden : true,
						        				id : 'sub_btnWeixinReplyUploadImage',
						        		        text : '上传图片',
						        		        listeners : {
						        		        	render : function(thiz){
						        		        		thiz.getEl().setVisible(false);
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
						        		 	   			form : sub_form.getForm().getEl(),
						        		 	   			success : function(response, options){
						        		 	   				menu_uploadMask.hide();
						        		 	   				var jr = Ext.decode(response.responseText.replace(/<\/?[^>]*>/g,''));
						        		 	   				if(jr.success){
						        			  	   				var ossImage = jr.root[0];
						        			  	   				sub_box.image = ossImage.image;
						        			  	   				sub_box.imageId = ossImage.imageId;	   				
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
						        		    			Ext.getDom(e.getId()).setAttribute('enctype', 'multipart/form-data');
						        		 	  		}
						        		    	},
						        				buttons : [sub_btnUpload]
						        			});	
						        			
						        			Ext.getCmp('foodMultiPrice_column_weixin').add({
						        				cls : 'multiClass'+i,
						        				columnWidth: 0.35, 
						        				layout : 'column',
						        				frame : true,
						        				items : [sub_box, sub_form]	 		
						        		 	});			
						        			
						        			Ext.getCmp('foodMultiPrice_column_weixin').add({
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
						        			sub_imgFile.image = rt.root[i].picUrl;
						        			Ext.getCmp('foodMultiPrice_column_weixin').doLayout();
						        			
						        			sub_imgFile.setImg(rt.root[i].picUrl);
										}
					        		}

					        	}
							}
					    }, 
					    error:function(xhr){ 
					        var rt = JSON.parse(xhr.responseText);
					        Ext.example.msg('提示', rt.msg);
					    } 
					}); 
				}else{
					var tab = tabs.getComponent("tabImageText_tab_weixin");
					tabs.setActiveTab(tab);
				}
										
					

//				}else if(typeof tn.attributes.type == "undefined"){
////						Ext.getCmp('contentPanel').removeAll();
//					Ext.getCmp('contentPanel').add(northPanel);
//					Ext.getCmp('contentPanel').doLayout();
//				}else{
//					var tab = tabs.getComponent("tabImageText_tab_weixin");
//					tabs.setActiveTab(tab);
//					
//				}
				
			}
		}
	});
	
	//查看自动回复
	$('#getSubscribe_a_weixin').click(function(){
		clearTabContent();
		isKeyword = false;
		centerPanel.setTitle('设置 -- 自动回复');
		tabs.enable();
		Ext.getCmp('tabView_tab_weixin').disable();
		Ext.getCmp('tabSystem_tab_weixin').disable();
		//清除tree选中
		tree.getSelectionModel().clearSelections();
		//清除tree选中
		keywordTree.getSelectionModel().clearSelections();
		
		subscribe = true;
		$.ajax({ 
		    type : "post", 
		    async : false, 
		    url : "../../OperateReply.do",
		    dataType : "json",		//jsonp数据类型 
		    data : {
		    	dataSource : 'subscribeReply',
		    	rid : rid
		    },
		    success : function(rt){ 
		        if(rt.success){
		        	subscribeKey = rt.other.key;
		        	if(rt.root && rt.root.length > 0){
		    			var tab = tabs.getComponent("tabImageText_tab_weixin");
		    			tabs.setActiveTab(tab);
		        		
		        		var item = rt.root[0];
		        		
						$('#itemTitle_textfield_weixin').val(item.title);			
						$('#itemContent_textfield_weixin').val(item.description);	
						$('#itemUrl_textarea_weixin').val(item.url);	
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
		        			
			        			Ext.getCmp('foodMultiPrice_column_weixin').add({
			        				cls : 'multiClass'+i,
			        		 		columnWidth : 1	 		
			        		 	});								
			        			
			        			Ext.getCmp('foodMultiPrice_column_weixin').add({
			        				cls : 'multiClass'+i,
			        				columnWidth: 0.4,
			        				labelWidth : 40,
			        				defaults : {
			        					width : 250
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
			        			
			        			var sub_btnUpload = new Ext.Button({
			        				hidden : true,
			        				id : 'sub_btnWeixinReplyUploadImage',
			        		        text : '上传图片',
			        		        listeners : {
			        		        	render : function(thiz){
			        		        		thiz.getEl().setVisible(false);
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
			        		 	   			form : sub_form.getForm().getEl(),
			        		 	   			success : function(response, options){
			        		 	   				menu_uploadMask.hide();
			        		 	   				var jr = Ext.decode(response.responseText.replace(/<\/?[^>]*>/g,''));
			        		 	   				if(jr.success){
			        			  	   				var ossImage = jr.root[0];
			        			  	   				sub_box.image = ossImage.image;
			        			  	   				sub_box.imageId = ossImage.imageId;	   				
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
			        		    			Ext.getDom(e.getId()).setAttribute('enctype', 'multipart/form-data');
			        		 	  		}
			        		    	},
			        				buttons : [sub_btnUpload]
			        			});		
			        			
			        			Ext.getCmp('foodMultiPrice_column_weixin').add({
			        				cls : 'multiClass'+i,
			        				columnWidth: 0.35,  
			        				layout : 'column',
			        				frame : true,
			        				items : [sub_box, sub_form]	 		
			        		 	});			
			        			
			        			Ext.getCmp('foodMultiPrice_column_weixin').add({
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
			        			
			        			Ext.getCmp('foodMultiPrice_column_weixin').doLayout();
			        			
			        			sub_imgFile.setImg(rt.root[i].picUrl);
			        			
			        			sub_imgFile.image = rt.root[i].picUrl;
							}
		        		}
		        		
		        	}else if(rt.other){
		    			var tab = tabs.getComponent("tabClick_tab_weixin");
		    			tabs.setActiveTab(tab);
		    			
		        		$('#menuTxtReply_textarea_weixin').val(rt.other.text);
		        	}
				}else{
	    			var tab = tabs.getComponent("tabImageText_tab_weixin");
	    			tabs.setActiveTab(tab);
					Ext.getCmp('itemTitle_textfield_weixin').focus(true, 100);	
				}
		    }, 
		    error:function(xhr){ 
		    	Ext.example.msg(xhr.responseText);
		    }
		});
	});
	
	//关键字回复的添加关键字按钮
	var atuoRellyTbar = new Ext.Toolbar({
		items : ['->', {
			text : '添加自动回复',
			id : 'addAutoReply_btn_weixin',
			iconCls : 'btn_add',
			handler : function(){
				clearTabContent();
				isKeyword = false;
				centerPanel.setTitle('设置 -- 自动回复');
				tabs.enable();
				Ext.getCmp('tabView_tab_weixin').disable();
				Ext.getCmp('tabSystem_tab_weixin').disable();
				//清除tree选中
				tree.getSelectionModel().clearSelections();
				//清除tree选中
				keywordTree.getSelectionModel().clearSelections();
				
				subscribe = true;
				$.ajax({ 
				    type : "post", 
				    async : false, 
				    url : "../../OperateReply.do",
				    dataType : "json",		//jsonp数据类型 
				    data : {
				    	dataSource : 'subscribeReply',
				    	rid : rid
				    },
				    success : function(rt){ 
				        if(rt.success){
				        	subscribeKey = rt.other.key;
				        	if(rt.root && rt.root.length > 0){
				    			var tab = tabs.getComponent("tabImageText_tab_weixin");
				    			tabs.setActiveTab(tab);
				        		
				        		var item = rt.root[0];
				        		
								$('#itemTitle_textfield_weixin').val(item.title);			
								$('#itemContent_textfield_weixin').val(item.description);	
								$('#itemUrl_textarea_weixin').val(item.url);	
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
				        			
					        			Ext.getCmp('foodMultiPrice_column_weixin').add({
					        				cls : 'multiClass'+i,
					        		 		columnWidth : 1	 		
					        		 	});								
					        			
					        			Ext.getCmp('foodMultiPrice_column_weixin').add({
					        				cls : 'multiClass'+i,
					        				columnWidth: 0.4,
					        				labelWidth : 40,
					        				defaults : {
					        					width : 250
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
					        			
					        			var sub_btnUpload = new Ext.Button({
					        				hidden : true,
					        				id : 'sub_btnWeixinReplyUploadImage',
					        		        text : '上传图片',
					        		        listeners : {
					        		        	render : function(thiz){
					        		        		thiz.getEl().setVisible(false);
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
					        		 	   			form : sub_form.getForm().getEl(),
					        		 	   			success : function(response, options){
					        		 	   				menu_uploadMask.hide();
					        		 	   				var jr = Ext.decode(response.responseText.replace(/<\/?[^>]*>/g,''));
					        		 	   				if(jr.success){
					        			  	   				var ossImage = jr.root[0];
					        			  	   				sub_box.image = ossImage.image;
					        			  	   				sub_box.imageId = ossImage.imageId;	   				
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
					        		    			Ext.getDom(e.getId()).setAttribute('enctype', 'multipart/form-data');
					        		 	  		}
					        		    	},
					        				buttons : [sub_btnUpload]
					        			});		
					        			
					        			Ext.getCmp('foodMultiPrice_column_weixin').add({
					        				cls : 'multiClass'+i,
					        				columnWidth: 0.35,  
					        				layout : 'column',
					        				frame : true,
					        				items : [sub_box, sub_form]	 		
					        		 	});			
					        			
					        			Ext.getCmp('foodMultiPrice_column_weixin').add({
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
					        			
					        			Ext.getCmp('foodMultiPrice_column_weixin').doLayout();
					        			
					        			sub_imgFile.setImg(rt.root[i].picUrl);
					        			
					        			sub_imgFile.image = rt.root[i].picUrl;
									}
				        		}
				        		
				        	}else if(rt.other){
				    			var tab = tabs.getComponent("tabClick_tab_weixin");
				    			tabs.setActiveTab(tab);
				    			
				        		$('#menuTxtReply_textarea_weixin').val(rt.other.text);
				        	}
						}else{
			    			var tab = tabs.getComponent("tabImageText_tab_weixin");
			    			tabs.setActiveTab(tab);
							Ext.getCmp('itemTitle_textfield_weixin').focus(true, 100);	
						}
				    }, 
				    error:function(xhr){ 
				    	Ext.example.msg(xhr.responseText);
				    }
				});
			}
		},{
			text : '删除自动回复',
			id : 'delAutoReply_btn_weixin',
			iconCls : 'btn_add',
			handler : function(){
				centerPanel.setTitle('设置 --');
				Ext.Msg.confirm(
					'提示',
					'是否删除自动回复',
					function(e){
						if(e == 'yes'){
							$.ajax({ 
							    type : "post", 
							    async:false, 
							    url : "../../OperateReply.do",
							    dataType : "json",		//jsonp数据类型 
							    data : {
							    	dataSource : "deleteSubscribe",
							    	rid : rid
							    },
							    success : function(rt){ 
							    	if(rt.success){
							    		Ext.example.msg('提示', rt.msg);
							    		Ext.getCmp('delAutoReply_btn_weixin').disable();
					        			Ext.getCmp('addAutoReply_btn_weixin').enable();
							    		$('#getSubscribe_a_weixin').hide();
							    	}else{
							    		 Ext.example.msg('提示', rt.msg);
							    	}
							    }, 
							    error:function(xhr){ 
							        Ext.example.msg('提示', rt.msg);
							    	if(rt.success){
							    		Ext.getCmp('delAutoReply_btn_weixin').disable();
					        			Ext.getCmp('addAutoReply_btn_weixin').enable();
							    		$('#getSubscribe_a_weixin').hide();
							    	}
							    } 
							}); 	
							
							clearTabContent();
							
						}
					}
				);	
			}
		}
		]
	});	
	
	var autoReply = new Ext.Panel({
		title : '关注回复',
		region : 'center',
		height : 200,
		contentEl : 'divSetAutoReply_div_weixin',
		tbar : atuoRellyTbar
	})
	
	
	var treePanel = new Ext.Panel({
		layout : 'border',
		width : 270,
		frame : false,
		region : 'west',
		items : [tree, autoReply, keywordTree
//		,{
//			xtype : 'panel',
//			height : '100',
//			frame : true
//		}
		]
	});	
	
	
	var menu_uploadMask = new Ext.LoadMask(document.body, {
		msg : '正在上传图片...'
	});
	
	//图片预览
	var p_box;
	p_box = new Ext.BoxComponent({
		xtype : 'box',
 	    columnWidth : 1,
 	    height : 200,
 	    autoEl : {
 	    	tag : 'img',
 	    	title : '图片预览'
 	    }
	});
	
	//图片
	var imgFile;
	imgFile = Ext.ux.plugins.createImageFile({
		id : 'imgField_img_weixin',
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
   				
		  	   				p_box.imageId = ossImage.imageId;	   				
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
				
		  	   				p_box.imageId = ossImage.imageId;	   				
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
	
	var tabs;	
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
 	    	id : 'tabImageText_tab_weixin',
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
						id : 'itemTitle_textfield_weixin',
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
			}, {
				xtype : 'button',
				text : '保存',
				width : 100,
				height : 20,
				handler : function(){
					if(isKeyword){
						var tn = Ext.ux.getSelNode(keywordTree);
					}else{
						var tn = Ext.ux.getSelNode(tree);
					}
				
					if(!tn && !subscribe){
						Ext.example.msg('提示', '操作失败, 请选中一个菜单再进行操作.');
						return;
					}		
					if(!$('#itemTitle_textfield_weixin').val() || !$('#itemContent_textfield_weixin').val()){
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
								subItems += (title.getValue() + "<li>" + 
										 url.getValue() + "<li>" + 
											 (image.image ? image.image : -1) + "<li>" + 
											 (image.imageId ? image.imageId : -1));						
							
							}
						}		
					}
					
					var dataSource = "insertImageText";
					
					var key = null;
					if(subscribeKey != -1){
						dataSource = "updateImageText";
						key = subscribeKey;
					}else if(tn && tn.attributes.key && tn.attributes.key != -1 && !isNaN(tn.attributes.key)){
						dataSource = "updateImageText";
						key = tn.attributes.key;
					}else if(tn && tn.attributes.actionId && tn.attributes.actionId != 0){
						dataSource = "updateImageText";
						key = tn.attributes.actionId;
					}
					
					
					$.ajax({ 
					    type : "post", 
					    async : false, 
					    url : "../../OperateReply.do",
					    dataType : "json",//jsonp数据类型 
					    data : {
					    	dataSource : dataSource,
					    	rid : rid,
					    	key : key != null ? key : null,
					    	title : $("#itemTitle_textfield_weixin").val(),
					    	image : p_box.image ? p_box.image : null,
					    	imageId : p_box.imageId ? p_box.imageId : null,
					    	content : $("#itemContent_textfield_weixin").val(),
					    	url : $("#itemUrl_textarea_weixin").val(),
					    	subItems : subItems,
					    	subscribe : subscribe ? subscribe : null
					    },
					    success : function(rt){ 
					        if(rt.success){
					        	if(isKeyword){
					        		$.ajax({
						        		url : '../../OperateKeyword.do',
						        		dataType : 'json',
						        		type : 'post',
						        		data : {
						        			dataSource : 'update',
						        			id : tn.attributes.keywordId,
						        			actionId : rt.other ? rt.other.key : tn.attributes.actionId
						        		},
						        		success : function(data){
						        			 keywordTree.getRootNode().reload();
						        			 Ext.example.msg('提示', rt.msg);
						        		}
						        	});
					        	}else{
					        		if(dataSource == "insertImageText"){
						        		if(tn){
						        			tn.attributes.type = "click";
						        			tn.attributes.key = rt.other.key;
						        		}else{
						        			subscribeKey = rt.other.key;
						        			//显示删除
						        			Ext.getCmp('delAutoReply_btn_weixin').enable();
						        			//隐藏添加
						        			Ext.getCmp('addAutoReply_btn_weixin').disable();
						        			$('#getSubscribe_a_weixin').show();
						        		}
						        	}
					        		Ext.example.msg('提示', rt.msg);
					        	}
							}else{
								Ext.example.msg('提示', rt.msg);
							}
					        
					    }, 
					    error:function(xhr){ 
					        var rt = JSON.parse(xhr.responseText);
					        Ext.example.msg('提示', rt.msg);
					    } 
					}); 
				}
			}]	        
 	    },{
 	    	id : "tabClick_tab_weixin",
 	        contentEl:'textReplyBox_div_weixin',
 	        title: '文字'
 	    },{
 	    	id : 'tabView_tab_weixin',
 	    	contentEl : 'urlReplyBox_div_weixin',
 	    	title:'连接'
 	    },{
 	    	id : 'tabSystem_tab_weixin',
 	    	contentEl : 'systemReplyBox_div_weixin',
 	    	title:'系统保留'
 	    }],
 	    listeners : {
 	    	tabchange : function(){
 	    		nodey = 0;
 	    	}
 	    },
 	    plugins: new Ext.ux.TabCloseMenu()
 	    
 	});
 	
 	
 	var centerPanel;
 	centerPanel = new Ext.Panel({
 		title : '设置',
 	    region:'center',
        margins:'5 0 5 5',
        split:true,
        layout : 'border',
        items: [tabs]
 	});
	
    new Ext.Panel({
    	renderTo : 'weixinMenuWin_div_weixin',
    	width : parseInt(Ext.getDom('weixinMenuWin_div_weixin').parentElement.style.width.replace(/px/g, '')),
		height : parseInt(Ext.getDom('weixinMenuWin_div_weixin').parentElement.style.height.replace(/px/g, '')),
        layout : 'border',
        items : [treePanel,centerPanel]
    });
    
    //添加父菜单 && 添加子菜单 && 修改弹出窗口
    var updateDeptWin;
    (function deptWinInit(){
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

    })();
    
    //添加 &&　修改关键词的窗口
    var updateKeywordWin;
    (function keywordWinInit(){
    	if(!updateKeywordWin){
    		updateKeywordWin = new Ext.Window({
    			title : '添加关键字',
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
    					id : 'keyWordID_hidden_weixin'
    				}, {
    					xtype : 'textfield',
    					id : 'txtKeywordName_textfield_weixin',
    					fieldLabel : '关键字',
    					width : 130
    				}]
    			}],
    			bbar : [
    			'->',
    			{
    				text : '保存',
    				id : 'btnSaveUpdateKeyword',
    				iconCls : 'btn_save',
    				handler : function(){
    					var keywordName = Ext.getCmp('txtKeywordName_textfield_weixin');
    					var tn = Ext.ux.getSelNode(keywordTree);
    					console.log(tn);
    					if(updateKeywordWin.type == 'insert'){
    						$.ajax({
    							url : '../../OperateKeyword.do',
    							type : 'post',
    							dataType : 'json',
    							data : {
    								dataSource : 'insert',
    								keyword : keywordName.getValue()
    							},
    							success : function(data){
    								if(data.success){
    									Ext.example.msg('提示', data.msg);
    									//刷新
    									keywordTree.getRootNode().reload();
    									updateKeywordWin.hide();
    								}else{
    									Ext.example.msg('提示', data.msg);
    								}
    							},
    							error : function(data){
    								Ext.example.msg('提示', data.msg);
    							}
    						});
    					}else{
    						$.ajax({
    							url : '../../OperateKeyword.do',
    							type : 'post',
    							dataType : 'json',
    							data : {
    								dataSource : 'update',
    								keyword : keywordName.getValue(),
    								id : Ext.getCmp('keyWordID_hidden_weixin').getValue()
    							},
    							success : function(data){
    								if(data.success){
    									Ext.example.msg('提示', data.msg);
    									//刷新
    									keywordTree.getRootNode().reload();
    									updateKeywordWin.hide();
    								}else{
    									Ext.example.msg('提示', data.msg);
    								}
    							},
    							error : function(data){
    								Ext.example.msg('提示', data.msg);
    							}
    						});
    					}
    				}
    			}, {
    				text : '关闭',
    				iconCls : 'btn_close',
    				handler : function(){
    					updateKeywordWin.hide();
    				}
    			}],
    			 listeners : {
    				 
    			 }
    		});
    	}

    })();
    
    
    
	showFloatOption(menuTree_obj);	
	showFloatOption(keywordTree_obj);
	//判断是否已设置自动关注
	$.ajax({ 
	    type : "post", 
	    url : "../../OperateReply.do",
	    dataType : "json",		//jsonp数据类型 
	    data : {
	    	dataSource : 'subscribeReply',
	    	rid : rid
	    },
	    success : function(rt){ 
	        if(rt.success){
	        	//隐藏添加
    			Ext.getCmp('addAutoReply_btn_weixin').disable();
	        	Ext.getCmp('delAutoReply_btn_weixin').enable();
	        }else{
	        	Ext.getCmp('delAutoReply_btn_weixin').disable();
	        	//隐藏添加
    			Ext.getCmp('addAutoReply_btn_weixin').enable();
	        	$('#getSubscribe_a_weixin').hide();
	        }
	    },
	    error : function(xhr){
//	    	var rt = JSON.parse(xhr.responseText);
//	        if(rt.success){
//	        	$('#btnDeleteSubscribe_a_weixin').show();
//	        }else{
//	        	$('#btnDeleteSubscribe_a_weixin').hide();
//	        }
	    }
	});
	
	//添加子显示项
	function optMultiPriceHandler(){
		++ multiFoodPriceCount; 
		var subTitleId = 'subTitle' + multiFoodPriceCount,  
			subUrlId = 'subUrl' + multiFoodPriceCount, 
			subImgFileId = 'subImgFile' + multiFoodPriceCount;
			subFormId = 'subForm' + multiFoodPriceCount;
		
		Ext.getCmp('foodMultiPrice_column_weixin').add({
			cls : 'multiClass'+multiFoodPriceCount,
	 		columnWidth : 1	 		
	 	});								
		
		Ext.getCmp('foodMultiPrice_column_weixin').add({
			cls : 'multiClass'+multiFoodPriceCount,
			columnWidth: 0.40,
			labelWidth : 40,
			defaults : {
				width : 200
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
		
		var sub_btnUpload = new Ext.Button({
			hidden : true,
			id : 'sub_btnWeixinReplyUploadImage',
	        text : '上传图片',
	        listeners : {
	        	render : function(thiz){
	        		thiz.getEl().setVisible(false);
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
	 	   			form : sub_form.getForm().getEl(),
	 	   			success : function(response, options){
	 	   				menu_uploadMask.hide();
	 	   				var jr = Ext.decode(response.responseText.replace(/<\/?[^>]*>/g,''));
	 	   				if(jr.success){
		  	   				var ossImage = jr.root[0];
		  	   				sub_box.image = ossImage.image;
				
		  	   				sub_box.imageId = ossImage.imageId;	   				
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
	    			Ext.getDom(e.getId()).setAttribute('enctype', 'multipart/form-data');
	 	  		}
	    	},
			buttons : [sub_btnUpload]
		});	
		
		Ext.getCmp('foodMultiPrice_column_weixin').add({
			cls : 'multiClass'+multiFoodPriceCount,
			columnWidth: 0.35, 
			layout : 'column',
			frame : true,
			items : [sub_box, sub_form]	 		
	 	});	
		
		
		Ext.getCmp('foodMultiPrice_column_weixin').add({
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
		
		Ext.getCmp('foodMultiPrice_column_weixin').doLayout();
		
		Ext.getCmp(subTitleId).focus();
		
	}
	
	//子显示项的删除
	function deleteMultiPriceHandler(e){
		var cmps = $('.multiClass'+Ext.getCmp(e.id).multiIndex);
		
		for (var i = 0; i < cmps.length; i++) {
			Ext.getCmp('foodMultiPrice_column_weixin').remove(cmps[i].getAttribute("id"));
		}
		
		Ext.getCmp('foodMultiPrice_column_weixin').doLayout();
	}
	
	//子显示项的图片处理
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
		   				imgFile.imageId = ossImage.imageId;
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
	
	//菜单悬浮栏的操作
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
//		
//		if(tree.root.childNodes.length >= 3 && updateDeptWin.otype == 'insert'){
//			Ext.example.msg('提示', '父菜单不能超过3个.');
//		}else{
			updateDeptWin.show();
			updateDeptWin.center();
			deptName.focus(true, 100);
//		}
		
	}
	

	//清楚tabpanel内容
	function clearTabContent(){
		$('#menuTxtReply_textarea_weixin').val("");			
		$('#urlTextarea_textarea_weixin').val("");	
		
		$('#itemTitle_textfield_weixin').val("");			
		$('#itemContent_textfield_weixin').val("");	
		$('#itemUrl_textarea_weixin').val("");			
		imgFile.setImg("");
		
		if(multiFoodPriceCount > 0){
			for (var j = 1; j <= multiFoodPriceCount; j++) {
				var cmps = $('.multiClass'+j);
				for (var i = 0; i < cmps.length; i++) {
					Ext.getCmp('foodMultiPrice_column_weixin').remove(cmps[i].getAttribute("id"));
				}
			}
		}
		Ext.getCmp('foodMultiPrice_column_weixin').doLayout();
		multiFoodPriceCount = 0;
		
		$('input[name="systemSet"]:checked').removeAttr("checked");
		
		subscribe = false, subscribeKey = -1;
	}
	
});	
