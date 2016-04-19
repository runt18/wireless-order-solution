Ext.onReady(function(){
	
	var  _representId;
	var centerPanel;
	var _ossId; 		//阿里云文件存放id
	function initRepresentMsg(){
		Ext.Ajax.request({
			url : '../../OperateRepresent.do',
			params : {
				dataSource : 'getByCond'
			},
			success : function(res, opt){
				var jr = Ext.decode(res.responseText);
				if(jr.success){
					//TODO
					console.log(jr);
					_representId = jr.root[0].id;
					Ext.getCmp('activeTitle_panel_weixinRepresent').setValue(jr.root[0].title);
	    			Ext.getCmp('detail_textarea_weixinRepresent').setValue(jr.root[0].slogon);
	    			Ext.getCmp('endingDate_datefield_weixinRepresent').setValue(jr.root[0].finish.format('Y-m-d'));
	    			
	    			Ext.getCmp('representerPoint_textfield_weixinRepresent').setValue(jr.root[0].reconmendPoint);
	    			Ext.getCmp('representerExtraBalance_textfield_weixinRepresent').setValue(jr.root[0].recommendMoney);
	    			Ext.getCmp('appenderPoint_textfield_weixinRepresent').setValue(jr.root[0].subscribePoint);
	    			Ext.getCmp('appenderExtraBalance_textfield_weixinRepresent').setValue(jr.root[0].subscribeMoney);
	    			
	    			Ext.getCmp('commissionRange_numfield_weixinRepresent').setValue(jr.root[0].commissionRate ? (jr.root[0].commissionRate * 100).toFixed(2) : 0);
	    			
	    			var progressText = jr.root[0].isProgress ? '<span style="color:green;font-weight:bold;display:inline-block;margin-right:10px;">进行中</span>' : '<span style="color:red;font-weight:bold;display:inline-block;margin-right:10px;">已结束</span>'
	    			var activeTitle = '<span style="font-weight:bold;display:inline-block;margin-right:10px;">活动名称：' + jr.root[0].title + '</span>';
	    			var reperesenterReceive = '<span style="font-weight:bold;display:inline-block;margin-right:10px;">推荐人获得：' + jr.root[0].recommendMoney + '(元),' + jr.root[0].reconmendPoint + '(积分);</span>';
	    			var appenderReceive = '<span style="font-weight:bold;display:inline-block;margin-right:10px;">关注人获得：' + jr.root[0].subscribeMoney + '(元),' + jr.root[0].subscribePoint + '(积分);</span>';
	    			var appenderReceive = '<span style="font-weight:bold;display:inline-block;margin-right:10px;">佣金比率：' + (jr.root[0].commissionRate ? jr.root[0].commissionRate * 100 : 0) + '%;</span>';
	    			centerPanel.setTitle(progressText + activeTitle + reperesenterReceive + appenderReceive);
	    			
	    			var isChooseGivePoint = (jr.root[0].reconmendPoint || jr.root[0].subscribePoint) ? true : false;
	    			var isChooseGiveMoney = (jr.root[0].recommendMoney || jr.root[0].subscribeMoney) ? true : false;
	    			
	    			
	    			if(!isChooseGivePoint){
	    				Ext.getCmp('memberPoint_fieldset_weixinRepresent').collapse();
	    			}else{
	    				Ext.getCmp('memberPoint_fieldset_weixinRepresent').expand();
	    			}
	    			
	    			if(!isChooseGiveMoney){
	    				Ext.getCmp('extraBalance_fieldset_weixinRepresent').collapse();
	    			}else{
	    				Ext.getCmp('extraBalance_fieldset_weixinRepresent').expand();
	    			}
//	    			var isChooseGivePoint = !Ext.getCmp('memberPoint_fieldset_weixinRepresent').collapsed;
//	    			var isChooseGiveMoney = !Ext.getCmp('extraBalance_fieldset_weixinRepresent').collapsed;
				}else{
					Ext.MessageBox.alert('温磬提示', '数据读取失败');
				}
			},
			failure : function(res, opt){
				Ext.ux.showMsg(Ext.decode(response.responseText.replace(/<\/?[^>]*>/g,'')));
			}
		});
	}
	
	
	
 	var endingDate = new Ext.form.DateField({
 		id : 'endingDate_datefield_weixinRepresent',
		xtype : 'datefield',
		format : 'Y-m-d',
		width : '100',
		readyOnly : false,
		allowBlank : false,
		minValue : new Date(),
		columnWidth : 0.4
	});
 	
// 	xtype : 'container',
//	columnWidth : 0.24,
//	style : {
//		'margin-left' : '20px'
//	},
//	items : [{
//		xtype : 'label',
//		text : '活动结束时间设置',
//		style : {
//			'margin' : '10px',
//			'line-height' : '30px'
//		}
//	},endingDate]
 	
 	
 	var weixinRepresent_uploadMask = new Ext.LoadMask(document.body, {
 		msg : '正在上传图片...'
 	});
 	
 	var reprensentPicBox = new Ext.BoxComponent({
 		id : 'promotionPic_boxComponent_weixinRepresent',
 		xtype : 'box',
 		height : 100,
 		width : 100,
 		columnWidth : 0.3,
 		autoEl : {
 			tag : 'img',
 			title : '代言图片预览'
 		}
 	});
 	
 	var btnUpload;
 	btnUpload = new Ext.Button({
 		text : '上传图片',
 		hidden : true,
 		listeners : {
 			render : function(thiz){
 				thiz.getEl().setWidth(60, true);
 			}
 		},
 		handler : function(e){
 			
 			var check = true;
 			var img = '';
 			if(Ext.isIE){
 				Ext.getDom(imgFile.getId()).select();
 				img = document.selection.createRange().text;
 			}else{
 				img = Ext.getDom(imgFile.getId()).value;
 			}
 			if(typeof(img) != 'undefined' && img.length > 0){
 				var type = img.substring(img.lastIndexOf('.') + 1, img.length);
 				check = false;
 				for(var i = 0; i < Ext.ux.plugins.imgTypes.length;i++){
 					if(type.toLowerCase() == Ext.ux.plugins.imgTypes[i].toLowerCase()){
 						check = true;
 					}
 				}
 				
 				if(!check){
 					Ext.example.msg('提示', '图片类型不正确');
 					return;
 				}
 			}else{
 				Ext.example.msg('提示', '未选择图片');
 				return;
 			}
 			weixinRepresent_uploadMask.show();
 		
 			Ext.Ajax.request({
        		url : '../../OperateImage.do?dataSource=upload&ossType=12',  //ossType 阿里云放置图片的文件夹的值
 				isUpload : true,
 				form : form.getForm().getEl(),
 	   			success : function(response, options){
 	   				weixinRepresent_uploadMask.hide();
 	   				var jr = Ext.decode(response.responseText.replace(/<\/?[^>]*>/g,''));
 	   				if(jr.success){
 	   					_ossId = jr.root[0].imageId;
 	   				}else{
 	   					Ext.ux.showMsg(jr);
 	   					Ext.getCmp('couponTypeBox').setImg();
 	   				}

 	   				
 	   			},
 	   			failure : function(response, options){
 	   				coupon_uploadMask.hide();
 	   				Ext.ux.showMsg(Ext.decode(response.responseText.replace(/<\/?[^>]*>/g,'')));
 	   			}
        	});
 			
 		}
 	});
 	
 	var imgFile;
 	imgFile = Ext.ux.plugins.createImageFile({
 		id : 'representTypeBox',
 		img : reprensentPicBox,
 		width : 100,
 		height : 100,
 		callback : function(){
 			btnUpload.handler();
 		}
 	});
 	
 	var form = new Ext.form.FormPanel({
 		id : 'imgForm_form_weixinRepresent',
 		columnWidth : 0.6,
 		labelWidth : 60,
 		layout : 'column',
 		fieldUpload : true,
 		style : {
 			'margin' : '10px'
 		},
 		items : [imgFile],
 		listeners : {
 			render : function(thiz){
 				Ext.getDom(thiz.getId()).setAttribute('enctype', 'multipart/form-data');
 			}
 		}
 	});
 	
 	
 	var settingPanel = new Ext.Panel({
		columnWidth: 0.5, 
		layout : 'form',
		frame : true,
		labelWidth : 90,
		items : [{
			id : 'activeTitle_panel_weixinRepresent',
			xtype : 'textfield',
			fieldLabel : '<span style="line-height:30px;margin-right:10px;">活动标题</span>',
			style : {
				'width' : '380px',
				'margin-top' : '10px'
			}
//			allowBlank : false
		},{
			xtype : 'panel',
			layout : 'column',
			style : {
				'margin-top' : '10px',
			},
			items : [{
				xtype : 'label',
				html : '<span>背景设置</span><span style="margin-left:10px;">:</span>'
			},{
				layout : 'column',
				width : 386,
				height : 140,
				style : {
					'margin-left' : '34px'
				},
				frame : true,
				//TODO
				items : [form,reprensentPicBox,{
						xtype : 'label',
						style : 'width : 130px;',
						columnWidth : 0.4,
						style : {
							'margin-top' : '-65px',
							'margin-left' : '10px'
						},
						html : '<sapn style="font-size:14px;color:green;font-weight:bold;">图片大小不能超过100K</span>'
				},btnUpload]
			}]
		},{
			id : 'detail_textarea_weixinRepresent',
			xtype : 'textarea',
			fieldLabel : '<span style="line-height:30px;margin-right:10px;">活动详情</span>',
			style : {
				'margin-top' : '8px',
				'height' : '120px',
				'width' : '380px'
			}
		},{
			xtype : 'panel',
			layout : 'column',
			style : {
				'margin' : '10px 0'
			},
			items : [{
				xtype : 'label',
				text : '活动结束时间:',
				width : '95'
			},endingDate, {
				xtype : 'label',
				html : '&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;'
			}, {
				xtype : 'label',
				text : '佣金比例:'
			}, {
				id : 'commissionRange_numfield_weixinRepresent',
				xtype : 'numberfield',
				width : '50',
				allowNegative : false,
				maxText : 100
			}, {
				xtype : 'label',
				text : '%(按百分比配置)',
				style : 'color : green; line-height : 18px; font-weight : bold;'
			}, {
				
			},{
				xtype : 'label',
				hidden : true
			}]
		},{
			id : 'memberPoint_fieldset_weixinRepresent',
			xtype : 'fieldset',
			title : '赠送积分',
			checkboxToggle : true,
			checkboxName : 'memberPoint_checkBox_weixinRepresent',
			layout : 'column',
//			style : {
//				'height' : '100px'
//			},
			items : [{
				xtype : 'label',
				text : '推荐人积分:',
				columnWidth : 0.12
			},{
				id : 'representerPoint_textfield_weixinRepresent',
				xtype : 'textfield',
				columnWidth : 0.3,
				style : 'margin-right : 30px'
			},{
				xtype : 'label',
				text : '关注人积分:',
				columnWidth : 0.12
			},{
				id : 'appenderPoint_textfield_weixinRepresent',
				xtype : 'textfield',
				columnWidth : 0.3
			}]
		},{
			id : 'extraBalance_fieldset_weixinRepresent',
			xtype : 'fieldset',
			title : '赠送充额',
			checkboxToggle : true,
			layout : 'column',
//			style : {
//				'height' : '100px'
//			},
			items : [{
				xtype : 'label',
				text : '推荐人送额:',
				columnWidth : 0.12
			},{
				id : 'representerExtraBalance_textfield_weixinRepresent',
				xtype : 'textfield',
				columnWidth : 0.3,
				style : 'margin-right : 30px'
			},{
				xtype : 'label',
				text : '关注人送额:',
				columnWidth : 0.12
			},{
				id : 'appenderExtraBalance_textfield_weixinRepresent',
				xtype : 'textfield',
				columnWidth : 0.3
			}]
		
		}]
	
 	});
 	
 	var viewPanel = new Ext.Panel({

		columnWidth: 0.5,
		id : 'foodMultiPrice_column_weixin',
		layout : 'column',
		width : 400,
		frame : true,
		defaults : {
			layout : 'form'
		},
		items : [{
			id : 'representPosterShower_panel_weixinRepresent',
			xtype : 'panel',
			frame : true,
			width : '388',
			style : {
				'margin' : '8% 18%'
			},
			html : '<div style="height:100%;width:100%;" id="posterContainer_div_weixinRepresent"></div>'
		}]
	
 	});
 	
 	
 	centerPanel = new Ext.Panel({
 		title : '设置',
 	    region:'center',
 	    frame : true,
        layout : 'border',
        buttonAlign : 'center',
        items: [{
        	region:'center',
        	layout:'column',
        	defaults : {
        		'width' : '380px'
        	},
       		 items : [settingPanel, viewPanel]  
        }],
        buttons : [{
        	id : 'saveRepresentActive_button_weixinRepresent',
        	xtype : 'button',
        	text : '保存',
        	listeners : {
        		click : function(){
        			var params = {};
        			params.id = _representId;
        			params.title = Ext.getCmp('activeTitle_panel_weixinRepresent').getValue();
        			params.imageId = _ossId ? _ossId : null;
        			params.slogon = Ext.getCmp('detail_textarea_weixinRepresent').getValue();
        			params.finishDate = Ext.getCmp('endingDate_datefield_weixinRepresent').getValue();
        			
        			var reconmendPoint = Ext.getCmp('representerPoint_textfield_weixinRepresent').getValue() ? Ext.getCmp('representerPoint_textfield_weixinRepresent').getValue() : 0;
        			var recommendMoney = Ext.getCmp('representerExtraBalance_textfield_weixinRepresent').getValue() ? Ext.getCmp('representerExtraBalance_textfield_weixinRepresent').getValue() : 0;
        			var subscribePoint = Ext.getCmp('appenderPoint_textfield_weixinRepresent').getValue() ? Ext.getCmp('appenderPoint_textfield_weixinRepresent').getValue() : 0;
        			var subscribeMoney = Ext.getCmp('appenderExtraBalance_textfield_weixinRepresent').getValue() ? Ext.getCmp('appenderExtraBalance_textfield_weixinRepresent').getValue() : 0;
        			
        			var isChooseGivePoint = !Ext.getCmp('memberPoint_fieldset_weixinRepresent').collapsed;
        			var isChooseGiveMoney = !Ext.getCmp('extraBalance_fieldset_weixinRepresent').collapsed;
        			
        			params.recommendPoint = Number(isChooseGivePoint ? reconmendPoint : 0);
        			params.subscribePoint = Number(isChooseGivePoint ? subscribePoint : 0);
        			
        			params.recommendMoney = Number(isChooseGiveMoney ? recommendMoney : 0);
        			params.subscribeMoney = Number(isChooseGiveMoney ? subscribeMoney : 0);
        			
        			var commissionRate = Ext.getCmp('commissionRange_numfield_weixinRepresent').getValue() ? (Ext.getCmp('commissionRange_numfield_weixinRepresent').getValue() / 100).toFixed(2) : 0;
        			
        			params.commissionRate = commissionRate;
        			
        			params.dataSource = 'update';
        			
//        			weixinRepresent_uploadMask.show();
        			Ext.Ajax.request({
        				url : '../../OperateRepresent.do',
        				params : params,
        				success : function(res, opt){
        					var jr = Ext.decode(res.responseText);
        					if(jr.success){
        						Ext.MessageBox.alert('溫磬提示', '保存成功');
        						initRepresentMsg();
        					}else{
        						Ext.MessageBox.alert('溫磬提示', '保存失败');
        					}
        				},
        				failure : function(res, opt){
        					Ext.ux.showMsg(Ext.decode(res.responseText));
        				}
        			});
        		}
        	}
        }, {
        	xtype : 'button',
        	text : '预览',
        	listeners : {
        		click : function(){
//        			initRepresentMsg();
        		}
        	}
        }, {
        	xtype : 'button',
        	text : '重置',
        	listeners : {
        		click : function(){
        			initRepresentMsg();
        		}
        	}
        }]
 	});
	
 	initRepresentMsg();
	
	new Ext.Panel({
		renderTo : 'representConfig_div_weixinRepresent',
		width : parseInt(Ext.getDom('representConfig_div_weixinRepresent').parentElement.style.width.replace(/px/g, '')),
		height : parseInt(Ext.getDom('representConfig_div_weixinRepresent').parentElement.style.height.replace(/px/g, '')),
		layout : 'border',
        items : [centerPanel]
	});
	
	settingPanel.setHeight(centerPanel.getHeight());
	viewPanel.setHeight(centerPanel.getHeight());
	Ext.getCmp('representPosterShower_panel_weixinRepresent').setHeight(500);
	
	var host = null;
	if(window.location.hostname == 'e-tones.net'){
		host = 'wx.e-tones.net'
	}else if(window.location.hostname == 'ts.e-tones.net'){
		host = 'ts.e-tones.net';
	}else if(window.location.hostname == 'localhost'){
		host = 'localhost:8080'
	}else{
		host = window.location.hostname;
	}
	
	$('#posterContainer_div_weixinRepresent').load('http://' + host + '/wx-term/weixin/order/representCard.html', function(res, status, xhr){
		var title, imageUrl, desc; 
		if(status == 'success'){
			Ext.Ajax.request({
				url : '../../OperateRepresent.do',
				params : {
					dataSource : 'getByCond'
				},
				success : function(data, opt){
					var jr = Ext.decode(data.responseText);
					if(jr.success){
						title = jr.root[0].title;
						imageUrl = jr.root[0].image.image;
						desc = jr.root[0].slogon;
						
//						$(res).find('[id=background_div_representCard]').css({
////							'background-image' : 'url(\"' + imageUrl + '\")'
//							'background' : 'red'
//						});
//						$(res).trigger('refresh');
						var container = $(res).find('div')[0].parentNode;
						$(container).click(function(){
							console.log('container');
						});
						$(container).css({
							'background' : 'red'
						});
						$(container).find('h1').css({
							'color' : '#000'
						});
					}
				},
				failure : function(req, opt){
					Ext.ux.showMsg(Ext.decode(response.responseText.replace(/<\/?[^>]*>/g,'')));
				}
			});
		}
	});
});