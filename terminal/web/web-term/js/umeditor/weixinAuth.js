var weixinLogoUploader;

function setRestaurantInfo() {
	wx.lm.show();
	Ext.Ajax.request({
		url : '../../OperateRestaurant.do',
		params : {
			dataSource : 'updateInfo',
			info : UM.getEditor('myEditor').getContent()
		},
		success : function(res, opt){
			wx.lm.hide();
			var jr = Ext.decode(res.responseText);
			if(jr.success){
				Ext.example.msg('提示', jr.msg);
			}else{
				Ext.ux.showMsg(jr);
			}
		},
		fialure : function(res, opt){
			wx.lm.hide();
			Ext.ux.showMsg(res.responseText);
		}
	});	
	
}

function um_getContent(){
	return UM.getEditor('myEditor').getContent();
}


function um_clear() {
    return UM.getEditor('myEditor').setContent('');
}

function linkToAuth(change){
	$('#btnWeixinAuth').hide();
	$('#btnReAuthWeixinAuth').hide();
	$('#btnWeixinAuthFinish').show();
	window.open("http://wx.e-tones.net/wx-term/weixin/order/wxAuth.html?rid="+restaurantID);
	
}

function getRestaurantInfo(){
	wx.lm.show();
	$.post('../../OperateRestaurant.do', {
		dataSource : 'restInfo',
		rid : restaurantID
	}, function(result){
		wx.lm.hide();
		if(result.success && result.root[0].isAuth){
			getRestaurantInfo.isAuth = true;
			//点击去绑定
			$('#btnWeixinAuth').hide();
			//点击完成
			$('#btnWeixinAuthFinish').hide();
			//重新绑定
			$('#btnReAuthWeixinAuth').show();
			
			var rest = result.root[0];
			$('#wxRestLogo').attr('src', rest.headImgUrl);
			$('#wxNickName').text(rest.nickName);
			
			$('#seeRestaurantByCodeTitle').html('扫一扫去您的微信餐厅');
			//显示二维码
			$('#weixinCodeToRestaurant').attr('src', rest.qrCodeUrl);
			//显示是否打印二维码
			$('#isPrintQrCodeInOrderPaper').attr('checked', rest.qrCodeStatus == 1 ? true : false);
			$('#weixinAuthRestInfo').show();
			
		}else{
			getRestaurantInfo.isAuth = false;
			//点击去绑定
			$('#btnWeixinAuth').show();
			
			$('#seeRestaurantByCodeTitle').html('请先完成第一步微信公众号绑定');
			$('#weixinCodeToRestaurant').attr('src', 'http://food-image-test.oss.aliyuncs.com/nophoto.jpg');
		}
	}, 'json');
}

//step高度
var stepH;

$(function (){
	$("#weixin_wizard").steps({
	    headerTag: "span",
	    bodyTag: "div",
	    transitionEffect: "slideLeft",
	    
	    enableAllSteps : true,
	    labels: {
	        current: "current step:",
	        pagination: "Pagination",
	        finish: "完成",
	        next: "下一步",
	        previous: "上一步",
	        loading: "加载中 ..."
	    },
	    onInit : function (event, currentIndex) {
			if($('#weixinAuthDisplay').width() >= 660){
				$('#weixinAuthDisplayImg').attr('width', '660px');
			}
	    },
	    onStepChanged: function (event, currentIndex, priorIndex) {
	    	Ext.getCmp('weixinCardImage').render('btnOperateMenu');
	    	//0开始
	    	if(currentIndex == 1){//餐厅logo
	    		//加载微信logo uploader
	    		weixinLogoUploader.render('weixinLogoUploader');
	    	}else if(currentIndex == 2 && !um_getContent()){//餐厅简介
				wx.lm.show();
				if($('#weixinEditorDisplay').width() >= 660){
					$('#weixinEditorDisplayImg').attr('width', '660px');
				}
				Ext.Ajax.request({
					url : '../../OperateRestaurant.do',
					params : {
						dataSource : 'getInfo'
					},
					success : function(res, opt){
						wx.lm.hide();
						var jr = Ext.decode(res.responseText);
						if(jr.success){
							UM.getEditor('myEditor').setContent(jr.other.info);
						}else{
							um_clear();
							Ext.ux.showMsg(jr);
						}
					},
					fialure : function(res, opt){
						wx.lm.hide();
						Ext.ux.showMsg(res.responseText);
					}
				});    		
	    	}
/*	    	else if(currentIndex == 3){//欢迎活动
	    		getWxPromotion();
				if($('#wxActiveEditorDisplay').width() >= 660){
					$('#wxActiveEditorDisplayImg').attr('width', '660px');
				}    	
			    Ext.getCmp("wxActive_secendStepPanel").setHeight(stepH-10);
	    	}*/
	    }
	});	
	
	//先初始化step再设置参数
	setTimeout(function(){
		//获取微信餐厅,判读是否已绑定
		getRestaurantInfo();
		
	    //设置step高度自适应
	    $("#weixin_wizard .content").css({"min-height":(document.body.clientHeight - 220)+"px", "overflow":"auto"});
	    
	    //实例化编辑器
	    var um = UM.getEditor('myEditor');
	    
	    //实例化活动编辑器
//	    var wxActiveEditor = UM.getEditor('wxActiveEditor');
	    
	    //设置编辑器高度自适应
	    stepH = $("#weixin_wizard .content").height() - 40;
	    $('#myEditor').height(stepH - 70);
	    //auth
	    $('#weixinAuthDisplay').height(stepH);
	    $('#weixinAuthRest').height(stepH);
	    //餐厅简介示例
	    $('#weixinEditorDisplay').height(stepH);
	    //欢迎活动示例
//	    $('#wxActiveEditorDisplay').height(stepH);

//	    $('#wxActiveEditor').height(stepH - 275);
//	    $('#container4WxActiveEditor').height(stepH - 210);
//	    $('#container4WxActiveEditor').css("overflow-x", "hidden");
	    //上传菜品示例
	    $('#uploadFoodImgDisplay').height(stepH);
	    $('#uploadFoodImgCmp').height(stepH - 50);
	    //logo
	    $('#weixinLogoDisplay').height(stepH);
	    
	    //初始化第二步微信Logo界面
	    initWeixinLogoCmp();
	    setWeixinCardImage();
	    //初始化第四步欢迎活动界面
	    initWeixinActiveCmp();	
	}, 100);


	$('#isPrintQrCodeInOrderPaper').change(function(){
		wx.lm.show();
		$.post('../../OperateRestaurant.do', {
			dataSource: 'updateRestaurantPrintCode', 
			printCode : $('#isPrintQrCodeInOrderPaper').attr('checked') ? true:false
		}, function(rt){
			wx.lm.hide();
		}).error(function(){
			wx.lm.hide();
		});
	});
	
	
	
});

//TODO
function initWeixinLogoCmp(){
//Ext.onReady(function() {
	var uploadMask = new Ext.LoadMask(document.body, {
		msg : '正在上传图片...'
	});
	var box = new Ext.BoxComponent({
		xtype : 'box',
		id : 'boxWeixinLogoImgFile',
 	    columnWidth : 1,
 	    height : 300,
 	    autoEl : {
 	    	tag : 'img',
 	    	title : '图片预览.'
 	    }
	});
	var imgFile = Ext.ux.plugins.createImageFile({
		img : box,
		width : 468,
		height : 300,
		imgSize : 100,
		uploadCallback : function(){
			Ext.getCmp('btnWeixinLogoUploadImage').handler();
		}
	});
	var btnUpload = new Ext.Button({
			hidden : true,
			id : 'btnWeixinLogoUploadImage',
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
	        	uploadMask.show();
	        	
	        	
	        	Ext.Ajax.request({
	        		url : '../../OperateImage.do?dataSource=upload&ossType=7',
	 	   			isUpload : true,
	 	   			form : form.getForm().getEl(),
	 	   			success : function(response, options){
	 	   				var jr = Ext.decode(response.responseText.replace(/<\/?[^>]*>/g,''));
						Ext.Ajax.request({
							url : '../../OperateRestaurant.do',
							params : {
								dataSource : 'updateLogo',
								logo : jr.root[0].imageId
							},
							success : function(res, opt){
								uploadMask.hide();
								var jr = Ext.decode(res.responseText);
								Ext.example.msg('提示', jr.msg);
							},
							fialure : function(res, opt){
								wx.lm.hide();
								Ext.ux.showMsg(res.responseText);
							}
						});	 	   				
	 	   				
	 	   			},
	 	   			failure : function(response, options){
	 	   				uploadMask.hide();
	 	   				Ext.ux.showMsg(Ext.decode(response.responseText.replace(/<\/?[^>]*>/g,'')));
	 	   			}
	        	});
	        }
	});
	var btnClose = new Ext.Button({
			hidden : true,
	        text : '关闭',
	        listeners : {
	        	render : function(thiz){
	        		thiz.getEl().setWidth(100, true);
	        	}
	        },
	        handler : function(e){
	        	weixinLogoWin.hide();
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
    	buttons : [btnUpload, btnClose]
	});
	
	weixinLogoUploader = new Ext.Panel({
//		renderTo : 'weixinLogoUploader',
		title : '&nbsp;',
		region : 'center',
		layout : 'column',
		width : 500,
		frame : true,
		items : [box, {
			columnWidth: 1, 
			height: 20,
			html : '<sapn style="font-size:13px;color:green;">提示: 单张图片大小不能超过100KB.</span>'
		}, form],
		listeners : {
			render : function(){
				Ext.Ajax.request({
					url : '../../OperateRestaurant.do',
					params : {
						dataSource : 'getLogo'
					},
					success : function(res, opt){
						var jr = Ext.decode(res.responseText);
						imgFile.setImg(jr.other.logo);
					},
					fialure : function(res, opt){
						wx.lm.hide();
						Ext.ux.showMsg(res.responseText);
					}
				});
			}
		}
	});	
}
//})

function setWeixinCardImage(){
	
		var imgFile2,btnUpload2,imageBox,btnClose2,formPanel,weixinCardImage;
		
		imageBox = new Ext.BoxComponent({
			xtype : 'box',
	 	    columnWidth : 1,
	 	    height : 300,
	 	    id : 'imageBox_box_weixinAuth',
	 	    autoEl : {
	 	    	tag : 'img',
	 	    	title : '图片预览.'
	 	    }
		});
		
		imgFile2 = Ext.ux.plugins.createImageFile({
			id : 'imageFile2_imageFile_weixinAuth',
			img : imageBox,
			width : 468,
			height : 300,
			imgSize : 100,
			uploadCallback : function(){
				btnUpload2.handler();
			}
		});
		var weixin_uploadMask = new Ext.LoadMask(document.body, {
			msg : '正在上传图片...'
		});
		btnUpload2 = new Ext.Button({
			hidden : true,
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
	 	        	img = Ext.getDom(imgFile2.getId()).value;
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
	        	
	        	weixin_uploadMask.show();
	        	
	        	Ext.Ajax.request({
	        		url : '../../OperateImage.do?dataSource=upload&ossType=13',
	 	   			isUpload : true,
	 	   			form : formPanel.getForm().getEl(),
	 	   			success : function(response, options){
	 	   				var jr = Ext.decode(response.responseText.replace(/<\/?[^>]*>/g,''));
	 	   				if(!jr.success){
 	   						Ext.ux.showMsg({code: 0,msg : jr.msg});
 	   						weixin_uploadMask.hide();
 	   						return;
 	   					}
 	   					
						Ext.Ajax.request({
							url : '../../OperateRestaurant.do',
							params : {
								dataSource : 'updateWxCard',
								wxCardImgUrl : jr.root[0].imageId
							},
							success : function(res, opt){
								weixin_uploadMask.hide();
								var jr = Ext.decode(res.responseText);
								Ext.example.msg('提示', jr.msg);
							},
							fialure : function(res, opt){
								weixin_uploadMask.hide();
								Ext.ux.showMsg(res.responseText);
							}
						});	 	   				
	 	   				
	 	   			},
	 	   			failure : function(response, options){
	 	   				weixin_uploadMask.hide();
	 	   				Ext.ux.showMsg(Ext.decode(response.responseText.replace(/<\/?[^>]*>/g,'')));
	 	   			}
	        	});
	        }
		});
		
		var btnClose2 = new Ext.Button({
			hidden : true,
	        text : '关闭',
	        listeners : {
	        	render : function(thiz){
	        		thiz.getEl().setWidth(100, true);
	        	}
	        },
	        handler : function(e){
//	        	weixinLogoWin.hide();
	        }
		});
		
		formPanel = new Ext.form.FormPanel({
			columnWidth : 1,
			labelWidth : 60,
			fileUpload : true,
			items : [imgFile2],
			listeners : {
		    		render : function(e){
		    			Ext.getDom(e.getId()).setAttribute('enctype', 'multipart/form-data');
	 	  		}
	    	},
	    	buttonAlign : 'center',
	    	buttons : [btnUpload2, btnClose2]
		});
		
		
		var weixinCardImage;
		weixinCardImage = new Ext.Panel({
			title : '&nbsp;',
			region : 'center',
			layout : 'column',
			id : 'weixinCardImage',
			width : 500,
			frame : true,
			items : [imageBox, {
				columnWidth: 1, 
				height: 20,
				html : '<sapn style="font-size:13px;color:green;">提示: 单张图片大小不能超过100KB.</span>'
			}, formPanel],
			listeners : {
				render : function(){
					Ext.Ajax.request({
						url : '../../OperateRestaurant.do',
						params : {
							dataSource : 'getWxCard'
						},
						success : function(res, opt){
							var jr = Ext.decode(res.responseText);
							if(jr.other && jr.other.wxCardUrl){
								imgFile2.setImg(jr.other.wxCardUrl);
							}else{
								
								imgFile2.setImg('../../images/member_vip.jpg');
							}
						},
						fialure : function(res, opt){
							wx.lm.hide();
							Ext.ux.showMsg(res.responseText);
						}
					});
				}
			}
		});	
//		weixinCardImage.render('btnOperateMenu');
}

/**
 * 初始化欢迎活动
 */
function initWeixinActiveCmp(){
	
	var wxCoupon_uploadMask = new Ext.LoadMask(document.body, {
		msg : '正在上传图片...'
	});
	var p_box = new Ext.BoxComponent({
		xtype : 'box',
 	    columnWidth : 0.4,
 	    height : 100,
 	    width : 100,
 	    style : 'marginRight:5px;',
 	    autoEl : {
 	    	tag : 'img',
 	    	title : '优惠券图片预览'
 	    }
	});

	var btnUpload = new Ext.Button({
		columnWidth : 0.4,
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
        	wxCoupon_uploadMask.show();
        	Ext.Ajax.request({
        		url : '../../OperateImage.do?dataSource=upload&ossType=3',
 	   			isUpload : true,
 	   			form : form.getForm().getEl(),
 	   			success : function(response, options){
 	   				wxCoupon_uploadMask.hide();
 	   				var jr = Ext.decode(response.responseText.replace(/<\/?[^>]*>/g,''));
 	   				if(jr.success){
// 	   					Ext.ux.showMsg(jr);
	  	   				var ossImage = jr.root[0];
	 	   				initWeixinActiveCmp.image = ossImage.image;
	 	   				initWeixinActiveCmp.ossId = ossImage.imageId;	   				
 	   				}else{
 	   					Ext.ux.showMsg(jr);
 	   					Ext.getCmp('wxCouponTypeBox').setImg();
 	   				}
 	   			},
 	   			failure : function(response, options){
 	   				wxCoupon_uploadMask.hide();
 	   				Ext.ux.showMsg(Ext.decode(response.responseText.replace(/<\/?[^>]*>/g,'')));
 	   			}
        	});
        }
	});	
	
	var imgFile = Ext.ux.plugins.createImageFile({
		id : 'wxCouponTypeBox',
		img : p_box,
		width : 100,
		height : 100,
		callback : function(){
			btnUpload.handler();
		}
	});		
	
	var form = new Ext.form.FormPanel({
		columnWidth : .6,
		labelWidth : 60,
		fileUpload : true,
		items : [imgFile],
		listeners : {
	    	render : function(e){
	    		Ext.getDom(e.getId()).setAttribute('enctype', 'multipart/form-data');
 	  		}
	    }
	});		
	
/*	var secendStepWest = new Ext.form.FormPanel({
		width : 615,
		height:600,
		title : '编辑活动内容:',
		items : [{
			contentEl : 'wxActive_secendStep4'
		}]
	});*/
	
/*	new Ext.Panel({
		id : 'wxActive_secendStepPanel2',
		renderTo :'wxActive_secendStep',
		width :630,
		border : false,
 		items : [{
			layout : 'column',
			frame : true,
			border : false,

			defaults : {
				layout : 'form',
				labelWidth : 70,
				labelAlign : 'right',
				columnWidth : .33
			},
			items : [{
				columnWidth : .3,
				items : [{
					id : 'wxActive_title',
					xtype : 'textfield',
					width : 150,
					fieldLabel : '&nbsp;&nbsp;&nbsp;活动标题',
					style : 'overflow: hidden;',
					allowBlank : false,
					listeners : {
						blur : function(){
							Ext.getCmp('btnSecondStepEastBody').handler();
						}
					}
				}]
			}, {
				items : [{
					id : 'wxActive_beginDate',
					xtype : 'datefield',
					width : 90,
					fieldLabel : '&nbsp;&nbsp;&nbsp;活动日期',
					format : 'Y-m-d',
					readOnly : false,
					allowBlank : false,
					minValue : new Date(),
					blankText : '日期不能为空.',
					listeners : {
						invalid : function(thiz){
							thiz.clearInvalid();
						},
						blur : function(){
							Ext.getCmp('btnSecondStepEastBody').handler();
						}						
					}					
				}]				
			}, {
				items : [{
					xtype : 'label',
					style : 'margin-top:4px;',
					html : '至&nbsp;&nbsp;'
				}]
			}, {
				layout : 'fit',
				items : [{
					id : 'wxActive_endDate',
					xtype : 'datefield',
					width : 90,
					fieldLabel : '',
					format : 'Y-m-d',
					readOnly : false,
					allowBlank : false,
					minValue : new Date(),
					blankText : '日期不能为空.',
					listeners : {
						blur : function(){
							Ext.getCmp('btnSecondStepEastBody').handler();
						}
					}				
				}]				
			},{
				items : [{
					xtype : 'checkbox',
					id : 'radioIsHasCouponActive',
					hideLabel : true,
					checked : true,
					boxLabel : '赠送优惠劵',
					listeners : {
						render : function(e){
							e.getEl().dom.parentNode.style.paddingLeft = '10px';
							e.getEl().dom.parentNode.style.paddingTop = '5px';
						},
						check : function(e){
							if(e.getValue()){
								choosePromotionModel(2);
							}else{
								choosePromotionModel(1);
							}
						}
					}
				}]
			}]
		}]
	});	*/
	
	
/*	new Ext.Panel({
		renderTo :'wxActive_secendStep3',
		width :630,
		border : false,
		items : [{
			id : 'wxActive_secendStepPanel',
			layout : 'border',
			frame : false,
			border : false,
			height : 630,
			items : [{
	 			xtype : 'panel',
	 			region : 'center',
	 			width :630,
	 			height : 623,
				layout : 'column',
				frame : true,
				defaults : {
					layout : 'form',
					labelWidth : 70,
					labelAlign : 'right'
//					columnWidth : .33
				},
				items : [{
					id : 'wxActive_secendStep2SelectCoupon',
					items : [{
						id : 'westPanel',
						xtype : 'panel',
						defaults : {
							layout : 'form',
							labelWidth : 70,
							labelAlign : 'right'
						},
						items : [{
							items : [{
								xtype : 'label',
								html : '&nbsp;&nbsp;选择优惠劵:&nbsp;&nbsp;',
								listeners : {
									render : function(e){
	//									chooseCouponModel(e, '46px');
									}
								}
							}]			
						},{
							items : [{
								xtype : 'radio',
								id : 'radioDefaultCoupon',
								name : 'radioActiveType',
								inputValue : 1,
								hideLabel : true,
								checked : true,
								boxLabel : '十元券&nbsp;&nbsp;',
								listeners : {
									render : function(e){
										e.getEl().dom.parentNode.style.paddingLeft = '10px';
										e.getEl().dom.parentNode.style.paddingTop = '5px';
									},
									check : function(e){
										if(e.getValue()){
											changeCouponModel(1);
										}
									}
								}
							}]			
						},{
							items : [{
								xtype : 'radio',
								name : 'radioActiveType',
								inputValue : 2,
								hideLabel : true,
								boxLabel : '二十元券&nbsp;&nbsp;',
								listeners : {
									render : function(e){
										e.getEl().dom.parentNode.style.paddingLeft = '10px';
									},						
									check : function(e){
										if(e.getValue()){
											changeCouponModel(2);
										}
									}
								}
							}]			
						},{
							items : [{
								xtype : 'radio',
								id : 'radioSelfWxCoupon',
								name : 'radioActiveType',
								inputValue : 3,
								hideLabel : true,
								boxLabel : '自定义',
								listeners : {
									render : function(e){
										e.getEl().dom.parentNode.style.paddingLeft = '10px';
									},						
									check : function(e){
										if(e.getValue()){
											changeCouponModel(3);
										}
									}
								}
							}]			
						}]
					}]					
				
			},{
				id : 'wxActive_secendStep2CouponDetail',
				items : [{
					id : 'centerPanel',
					xtype : 'panel',
					layout : 'column',
					width : 220,
					style : 'paddingTop:20px;',
					defaults : {
						layout : 'form',
						labelWidth : 80
//						labelAlign : 'right'
					},
					items : [{
						items : [{
							id : 'wxActive_couponName',
							xtype : 'textfield',
							fieldLabel : '优惠劵名称',
							value : '十元优惠劵',
							allowBlank : false
						}]
					},{
						items : [{
							id : 'wxActive_price',
							xtype : 'textfield',
							value : 10,
							fieldLabel : '&nbsp;&nbsp;&nbsp;面额',
							allowBlank : false
						}]
					}, {
						items : [{
							id : 'wxActive_couponExpiredDate',
							xtype : 'datefield',
							width : 130,
							fieldLabel : '&nbsp;&nbsp;&nbsp;有效期至',
							format : 'Y-m-d',
							readOnly : false,
							allowBlank : false,
							minValue : new Date(),
							blankText : '日期不能为空.',
							listeners : {
								invalid : function(thiz){
									thiz.clearInvalid();
								}
							}				
						}]				
					}]
				}]					
				
			},{
				id : 'wxActive_secendStep2CouponImg',
				items : [{
					xtype : 'panel',
					width : 280,
					layout : 'column',
					style : 'marginLeft:18px;',
					frame : true,
					items : [p_box, form,{
							columnWidth : 0.6,
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
				}]					
				
			},secendStepWest]
			}]
		}]
	});		*/
	
	//默认为优惠劵模式
	initWeixinActiveCmp.promotionType = 2;
	//默认为添加操作
	initWeixinActiveCmp.otype = 'insert';
	
	//steps.js与Ext混用时的样式修正	
	$('#wxActive_beginDate').parent().width($('#wxActive_beginDate').width() + $('#wxActive_beginDate').next().width()+20);
	$('#wxActive_endDate').parent().width($('#wxActive_endDate').width() + $('#wxActive_endDate').next().width()+20);
	$('#wxActive_couponExpiredDate').parent().width($('#wxActive_couponExpiredDate').width() + $('#wxActive_couponExpiredDate').next().width()+20);	
}

//1表示无优惠劵纯展示; 2表示无条件领取优惠劵
/*function choosePromotionModel(promotionType){
	initWeixinActiveCmp.promotionType = promotionType
	if(promotionType == 1){
		Ext.getCmp('wxActive_secendStep2CouponImg').hide();
		Ext.getCmp('wxActive_secendStep2CouponDetail').hide();
		Ext.getCmp('wxActive_secendStep2SelectCoupon').hide();
//		Ext.getCmp('secondStep_edit').setHeight(440);
//		Ext.get('wxActiveEditor').setHeight(stepH - 160);
		$('#container4WxActiveEditor').height(stepH - 100);
		
		
	}else if(promotionType == 2){
		Ext.getCmp('wxActive_secendStep2CouponImg').show();
		Ext.getCmp('wxActive_secendStep2CouponDetail').show();
		Ext.getCmp('wxActive_secendStep2SelectCoupon').show();
		
//		Ext.get('wxActiveEditor').setHeight(stepH - 275);
		$('#container4WxActiveEditor').height(stepH - 210);
		
	}
}*/

function changeCouponModel(type){
	var couponName = Ext.getCmp('wxActive_couponName');
	var price = Ext.getCmp('wxActive_price');
	if(type == 1){
		couponName.setValue('十元优惠劵');
		price.setValue(10);
	}else if(type == 2){
		couponName.setValue('二十元优惠劵');
		price.setValue(20);	
	}else if(type == 3){
		couponName.setValue();
		price.setValue();	
	}
	couponName.clearInvalid();
	price.clearInvalid();
	couponName.focus();
}


var wxPromotion_uploadMask = new Ext.LoadMask(document.body, {
	msg : '正在保存活动...'
});
var saveWxActiveCmp = function(){
	validWxActive();
	
	var params = {};
	var title = Ext.getCmp('wxActive_title');
	var beginDate = Ext.getCmp('wxActive_beginDate');
	var endDate = Ext.getCmp('wxActive_endDate');
	var point = Ext.getCmp('wxActive_point');
	var couponName = Ext.getCmp('wxActive_couponName');
	var price = Ext.getCmp('wxActive_price');
	var expiredDate= Ext.getCmp('wxActive_couponExpiredDate');
	var entire = Ext.getCmp('threeStepEastBody');
	
	if(initWeixinActiveCmp.otype == 'insert'){
		params.dataSource = 'insert';
	}else{
		params.dataSource = 'update';
		params.id = initWeixinActiveCmp.pId;
		params.cId = initWeixinActiveCmp.couponTypeId;
	}
	
	params.pRule = initWeixinActiveCmp.promotionType;
	params.oriented = 1;//欢迎活动对象是全部会员
	params.couponName = couponName.getValue();
	params.price = price.getValue();
	params.expiredDate = expiredDate.getValue().format('Y-m-d');
	params.image = initWeixinActiveCmp.ossId;
	params.title = title.getValue();
	params.beginDate = beginDate.getValue().format('Y-m-d');
	params.endDate = endDate.getValue().format('Y-m-d');
	params.members = '';
	
//	params.body = UM.getEditor('wxActiveEditor').getContent();
//	params.entire = entire.body.dom.innerHTML;
//	params.entire = UM.getEditor('wxActiveEditor').getContent();
	params.type = true;//是欢迎活动

	wxPromotion_uploadMask.show();
	Ext.Ajax.request({
		url : '../../OperatePromotion.do',
		params : params,
		success : function(res, opt){
			var jr = Ext.decode(res.responseText);
			wxPromotion_uploadMask.hide();
			if(jr.success){
				Ext.example.msg(jr.title, jr.msg);
			}else{
				Ext.ux.showMsg(jr);
			}
		},
		fialure : function(res, opt){
			wxPromotion_uploadMask.hide();
			Ext.ux.showMsg(res.responseText);
		}
	});	
}

/*function wxActive_clear() {
    return UM.getEditor('wxActiveEditor').setContent('');
}*/

function validWxActive(){
	var title = '提示';
	if(!Ext.getCmp('wxActive_title').isValid()){
		Ext.example.msg(title, '请填写活动标题');
		return false;
	}
	
	if(!Ext.getCmp('wxActive_endDate').isValid()){
		Ext.example.msg(title, '请填写活动时间');
		return false;
	}		
	
	if((Ext.getCmp('wxActive_beginDate').getValue().getTime() + 86000000) < (new Date()).getTime()){
		Ext.example.msg(title, '活动开始时间要大于当前时间');
		return false;
	}
	
	if((Ext.getCmp('wxActive_endDate').getValue().getTime() + 86000000) < (new Date()).getTime()){
		Ext.example.msg(title, '活动结束时间要大于当前时间');
		return false;
	}
	
	if(initWeixinActiveCmp.promotionType != 1 && !Ext.getCmp('wxActive_couponName').isValid()){
		Ext.example.msg(title, '请填写优惠劵名称');
		return false;
	}
	
	if(initWeixinActiveCmp.promotionType != 1 && Ext.getCmp('wxActive_couponExpiredDate').getValue()){
		Ext.example.msg(title, '请填写优惠劵到期时间');
		return false;
	}
	
	if(initWeixinActiveCmp.promotionType != 1 && (Ext.getCmp('wxActive_couponExpiredDate').getValue().getTime() + 86000000) < (new Date()).getTime()){
		Ext.example.msg(title, '优惠劵有效期要大于当前时间');
		return false;
	}	
	
	return true;
	
}

function getWxPromotion(){
	wx.lm.show();
	Ext.Ajax.request({
		url : '../../OperatePromotion.do',
		params : {dataSource : 'hasWelcomePage'},
		success : function(res, opt){
			wx.lm.hide();
			var jr = Ext.decode(res.responseText);
			if(jr.success && jr.root.length > 0){
				initWeixinActiveCmp.otype = "update";
				operateWxPromotionData(jr.root[0]);
			}
		},
		failure : function(res, opt){
			wx.lm.hide();
			Ext.ux.showMsg(Ext.decode(res.responseText));
		}
	});
}


function operateWxPromotionData(data){
	var title = Ext.getCmp('wxActive_title');
	var beginDate = Ext.getCmp('wxActive_beginDate');
	var endDate = Ext.getCmp('wxActive_endDate');
	var couponName = Ext.getCmp('wxActive_couponName');
	var price = Ext.getCmp('wxActive_price');
	var expiredDate= Ext.getCmp('wxActive_couponExpiredDate');
	
	title.setValue(data.title);
	beginDate.setValue(data.promotionBeginDate);
	endDate.setValue(data.promotionEndDate);
//	UM.getEditor('wxActiveEditor').setContent(data.body);
	
	
	if(data.pType == 1){
		Ext.getCmp('radioIsHasCouponActive').fireEvent('check', Ext.getCmp('radioIsHasCouponActive'), false)
	}else{
		//赠送选中
		Ext.getDom('radioIsHasCouponActive').checked = true;
		//自定义选中
		Ext.getDom('radioSelfWxCoupon').checked = true; 
		initWeixinActiveCmp.pId = data.id;
		initWeixinActiveCmp.couponTypeId = data.coupon.id;
		couponName.setValue(data.coupon.name);
		price.setValue(data.coupon.price);
		expiredDate.setValue(data.coupon.expiredFormat);
		Ext.getCmp('wxCouponTypeBox').setImg(data.coupon.ossImage?data.coupon.ossImage.image:'http://digie-image-real.oss.aliyuncs.com/nophoto.jpg');
	}
	
}

function linkToUploadImg(){
	Ext.ux.addTab("menuMgr", "菜谱管理", "BasicManagement_Module/MenuManagement.html");
}







