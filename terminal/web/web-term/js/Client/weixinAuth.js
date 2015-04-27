var weixinLogoUploader;

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
    	console.log(111111);
    },
    onStepChanged: function (event, currentIndex, priorIndex) { 
    	//0 - 2
    	if(currentIndex == 1){
    		//加载微信logo uploader
    		weixinLogoUploader.render('weixinLogoUploader');
    	}else if(currentIndex == 2 && !um_getContent()){
			wx.lm.show();
			if($('#weixinEditorDisplay').width() >= 660){
				$('#weixinEditorDisplay').width(660);
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
    }
});

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
	$.post('../../OperateRestaurant.do', {
		dataSource : 'restInfo',
		rid : restaurantID
	}, function(result){
		if(result.success && result.root[0].isAuth){
			getRestaurantInfo.isAuth = true;
			$('#btnWeixinAuth').hide();
			$('#btnWeixinAuthFinish').hide();
			$('#btnReAuthWeixinAuth').show();
			var rest = result.root[0];
			$('#wxRestLogo').attr('src', rest.headImgUrl);
			$('#wxNickName').text(rest.nickName);
			
			$('#seeRestaurantByCodeTitle').html('扫一扫去您的微信餐厅');
			$('#weixinCodeToRestaurant').attr('src', rest.qrCodeUrl);
			$('#weixinAuthRestInfo').show();
			
		}else{
			getRestaurantInfo.isAuth = false;
			$('#btnWeixinAuth').show();
			
			$('#seeRestaurantByCodeTitle').html('请先完成第一步微信公众号绑定');
			$('#weixinCodeToRestaurant').attr('src', 'http://food-image-test.oss.aliyuncs.com/nophoto.jpg');
		}
	});
}

$(function (){
	//获取微信餐厅,判读是否已绑定
	getRestaurantInfo();
//	$('#btnWeixinAuth').show();
	
    //设置step高度自适应
    $("#weixin_wizard .content").css({"min-height":(document.body.clientHeight - 200)+"px", "overflow":"auto"});
    
    //实例化编辑器
    var um = UM.getEditor('myEditor');
    
    //设置编辑器高度自适应
    var stepH = $("#weixin_wizard .content").height() - 40;
    $('#myEditor').height(stepH - 70);
    //auth
    $('#weixinAuthDisplay').height(stepH);
    $('#weixinAuthRest').height(stepH);
    //编辑器
    $('#weixinEditorDisplay').height(stepH);
    //logo
    $('#weixinLogoDisplay').height(stepH);
    
    initWeixinLogoCmp();
});



function getWeixinRestaurnatInfo(){
	
}

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

















