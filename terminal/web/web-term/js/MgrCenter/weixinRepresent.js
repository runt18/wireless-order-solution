Ext.onReady(function(){
	
	var  _representId;
	function initRepresentMsg(){
		Ext.Ajax.request({
			url : '../../OperateRepresent.do',
			params : {
				dataSource : 'getByCond'
			},
			success : function(res, opt){
				var jr = Ext.decode(res.responseText);
				_representId = jr.root[0].id;
				Ext.getCmp('activeTitle_panel_weixinRepresent').setValue(jr.root[0].title);
    			Ext.getCmp('detail_textarea_weixinRepresent').setValue(jr.root[0].slogon);
    			Ext.getCmp('endingDate_datefield_weixinRepresent').setValue(jr.root[0].finish.format('Y-m-d'));
    			
    			Ext.getCmp('representerPoint_textfield_weixinRepresent').setValue(jr.root[0].reconmendPoint);
    			Ext.getCmp('representerExtraBalance_textfield_weixinRepresent').setValue(jr.root[0].recommendMoney);
    			Ext.getCmp('appenderPoint_textfield_weixinRepresent').setValue(jr.root[0].subscribePoint);
    			Ext.getCmp('appenderExtraBalance_textfield_weixinRepresent').setValue(jr.root[0].subscribeMoney);
    			
    			
    			var isChooseGivePoint = (jr.root[0].reconmendPoint || jr.root[0].subscribePoint) ? true : false;
    			var isChooseGiveMoney = (jr.root[0].recommendMoney || jr.root[0].subscribeMoney) ? true : false;
    			
    			
    			if(!isChooseGivePoint){
    				Ext.getCmp('memberPoint_fieldset_weixinRepresent').collapse();
    			}
    			
    			if(!isChooseGiveMoney){
    				Ext.getCmp('extraBalance_fieldset_weixinRepresent').collapse();
    			}
//    			var isChooseGivePoint = !Ext.getCmp('memberPoint_fieldset_weixinRepresent').collapsed;
//    			var isChooseGiveMoney = !Ext.getCmp('extraBalance_fieldset_weixinRepresent').collapsed;
			},
			failure : function(res, opt){
				
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
		minValue : new Date()
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
 			title : '优惠卷图片预览'
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
// 			 
 			setTimeout(function(){
 				weixinRepresent_uploadMask.hide();
 			}, 2000);

 			
 			//FIXME
// 			console.log(Ext.getCmp('memberPoint_fieldset_weixinRepresent').collapsed);
 			
 		}
 	});
 	
 	
 	//TODO
 	var imgFile = Ext.ux.plugins.createImageFile({
 		id : 'representTypeBox',
 		img : reprensentPicBox,
 		width : 100,
 		height : 100,
 		callback : function(){
 			btnUpload.handler();
 		}
 	});
 	
 	var form = new Ext.form.FormPanel({
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
			},endingDate]
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
		title : '显示面板',
		width : 400,
		frame : true,
		defaults : {
			layout : 'form'
		},
		items : [{
			xtype : 'panel',
			width : '300',
			frame : true,
			style : {
			}
		}]
	
 	});
 	
 	
 	var centerPanel = new Ext.Panel({
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
        //TODO
        buttons : [{
        	id : 'saveRepresentActive_button_weixinRepresent',
        	xtype : 'button',
        	text : '保存',
        	listeners : {
        		click : function(){
        			var params = {};
        			params.id = _representId;
        			params.title = Ext.getCmp('activeTitle_panel_weixinRepresent').getValue();
        			params.body = '';
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
        			
        			params.dataSource = 'update';
        			
//        			weixinRepresent_uploadMask.show();
        			Ext.Ajax.request({
        				url : '../../OperateRepresent.do',
        				params : params,
        				sucess : function(res, opt){
        					Ext.MessageBox.tip('修改成功');
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
        	text : '取消'
        }]
 	});
	
 	initRepresentMsg();
	
	new Ext.Panel({
		renderTo : 'deputySet_div_weixinDeputyMgr',
		width : parseInt(Ext.getDom('deputySet_div_weixinDeputyMgr').parentElement.style.width.replace(/px/g, '')),
		height : parseInt(Ext.getDom('deputySet_div_weixinDeputyMgr').parentElement.style.height.replace(/px/g, '')),
		layout : 'border',
        items : [centerPanel]
	});
	
	settingPanel.setHeight(centerPanel.getHeight());
	viewPanel.setHeight(centerPanel.getHeight());
	
});