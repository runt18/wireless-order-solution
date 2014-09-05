

/**
 * 修改部门信息
 */
function initCouponTypeWin(){
	operatePromotTypeWin = Ext.getCmp('operatePromotTypeWin');
	if(!operatePromotTypeWin){
		operatePromotTypeWin = new Ext.Window({
			id : 'operatePromotTypeWin',
			title : '添加优惠活动',
			closable : true,
			closeAction:'hide',
			resizable : false,
			modal : true,
			width : 1100,
			height : 720,
			bodyStyle : 'backgroundColor:#FFFFFF; border:1px solid #99BBE8;',
			contentEl : 'divActiveInsert',
			listeners : {
				hide : function(){
					$('#span_firstModel').click();
					Ext.getCmp('active_title').setValue();
					Ext.getCmp('active_beginDate').setValue();
					Ext.getCmp('active_endDate').setValue();
					Ext.getCmp('active_point').setValue();
					Ext.getCmp('active_couponName').setValue();
					Ext.getCmp('active_price').setValue();
					Ext.getCmp('active_couponExpiredDate').setValue();
					Ext.getCmp('secondStep_edit').setValue();
					
					Ext.getCmp('active_beginDate').clearInvalid();
					Ext.getCmp('active_endDate').clearInvalid();
					Ext.getCmp('active_couponExpiredDate').clearInvalid();
					
					Ext.getDom('radioDefaultCoupon').checked = true;
					Ext.getCmp('radioDefaultCoupon').fireEvent('check', Ext.getCmp('radioDefaultCoupon'), true);
					Ext.getCmp('couponTypeBox').setImg();
					
//					Ext.getCmp('secondStepEastBody').body.update('');
					
					$("#wizard").steps("previous");
					$("#wizard").steps("previous");
				}
			},
			keys : [{
				 key : Ext.EventObject.ESC,
				 fn : function(){ 
					 operatePromotTypeWin.hide();
				 },
				 scope : this 
			 }]
		});
	}
};

function chooseCouponModel(e, h){
	if(Ext.isIE)
		e.getEl().dom.parentNode.style.paddingTop = '43px';
	else
		e.getEl().dom.parentNode.style.paddingTop = h?h:'45px';
}

function changeCouponModel(type){
	var couponName = Ext.getCmp('active_couponName');
	var price = Ext.getCmp('active_price');
	if(type == 0){
		couponName.setValue('十元中秋卷');
		price.setValue(10);
	}else if(type == 1){
		couponName.setValue('二十元中秋卷');
		price.setValue(20);	
	}else if(type == 2){
		couponName.setValue();
		price.setValue();	
	}
	couponTypeId = type;
	couponName.clearInvalid();
	price.clearInvalid();
}

//1表示无优惠劵纯展示; 2表示无条件领取优惠劵
function choosePromotionModel(){
	if(promotionType == 1){
		Ext.getCmp('active_secendStep2Panel').hide();
		Ext.getCmp('secondStep_edit').setHeight(465);
		Ext.getCmp('secondStepEastBody').setHeight(525);
		Ext.getCmp('active_point').hide();
		Ext.getCmp('active_point').getEl().up('.x-form-item').setDisplayed(false);
		Ext.getCmp('hide_activeOccupy').show();
	}else{
		
		Ext.getCmp('active_secendStep2Panel').show();
		Ext.getCmp('active_point').show();
		Ext.getCmp('active_point').getEl().up('.x-form-item').setDisplayed(true);	
		Ext.getCmp('hide_activeOccupy').hide();
		Ext.getCmp('secondStep_edit').setHeight(340);
		Ext.getCmp('secondStepEastBody').setHeight(400);
		
		
	}
	Ext.getCmp('active_secendStepPanel').doLayout();
	Ext.getCmp('active_secendStep3Panel').doLayout();
}

function selectPromotionModel(thiz, type){
	$(thiz).find('input').attr('checked', 'checked');
	promotionType = type;
	choosePromotionModel();
}

function promotionRule(pType, point){
	var rule = '';
	if(pType == 3){
		rule = '单次消费积分满<font style="color: red">' + point + '</font>分即可领取优惠劵';
	}else if(pType == 4){
		rule = '累计消费积分满<font style="color: red">' + point + '</font>分即可领取优惠劵';
	}
	return '<div style="margin: 10px 10px 10px 10px; font-szie:14px;font-weight:bold;">'+ rule +'</div>';
}

function getPromotionBodyById(id){
	Ext.Ajax.request({
		url : '../../OperatePromotion.do',
		params : {promotionId : id, dataSource : 'getPromotion'},
		success : function(res, opt){
			var jr = Ext.decode(res.responseText);
			if(jr.success){
				Ext.getCmp('promotionPreviewBody').body.update('<div style="text-align:center; font-size: 30px; font-weight: bold; word-wrap:break-word; color: #D2691E;">' + jr.root[0].title + '</div>' +
																'<div style="margin: 10px 10px 10px 10px; color:#aaa; font-szie:12px;">活动日期 : ' + jr.root[0].promotionBeginDate + '&nbsp;至&nbsp;' + jr.root[0].promotionEndDate + '</div>' +
																promotionRule(jr.root[0].pType, jr.root[0].point) +
																jr.root[0].body);
			}
		},
		failure : function(res, opt){
			Ext.ux.showMsg(Ext.decode(res.responseText));
		}
	});
}

function fnPublishPromotion(){
	var sn = Ext.ux.getSelNode(promotionTree);
	Ext.Ajax.request({
		url : '../../OperatePromotion.do',
		params : {promotionId : sn.attributes.id, dataSource : 'publish'},
		success : function(res, opt){
			var jr = Ext.decode(res.responseText);
			if(jr.success){
				Ext.example.msg(jr.title, jr.msg);
				promotionTree.getRootNode().reload();
			}
		},
		failure : function(res, opt){
			Ext.ux.showMsg(Ext.decode(res.responseText));
		}
	});
}

var promotionPreviewPanel, memberCountGrid;
var operatePromotTypeWin, sendCouponWin, couponViewBillWin;
var bar = {treeId : 'promotionTree', mult : [{status : 1, option :[{name : '发布', fn : "fnPublishPromotion()"}, {name : '修改', fn : "floatBarUpdateHandler()"}, {name : '删除', fn : "floatBarDeleteHandler()"}]}, 
											{status : 2, option :[{name : '撤销', fn : "floatBarSendCouponHandler()"}]},
											{status : 3, option :[]},
											{status : 4, option :[]} ]};
Ext.onReady(function() {
	initCouponTypeWin();
	promotionTree = new Ext.tree.TreePanel({
		title : '活动信息',
		id : 'promotionTree',
		region : 'west',
		width : 200,
		border : true,
		rootVisible : false,
		frame : true,
		bodyStyle : 'backgroundColor:#FFFFFF; border:1px solid #99BBE8;',
		loader : new Ext.tree.TreeLoader({
			dataUrl : '../../OperatePromotion.do',
			baseParams : {
				dataSource : 'getPromotionTree'
			}
		}),
		root : new Ext.tree.AsyncTreeNode({
			expanded : true,
			text : '全部类型',
	        leaf : false,
	        border : true,
	        promotionId : -1,
	        listeners : {
	        	load : function(){
//	        		var treeRoot = promotionTree.getRootNode().childNodes;
	        		
	        	}
	        }
		}),
		tbar : [
		    '->', 
		    {
				text : '添加',
				iconCls : 'btn_add',
				handler : function(e){
					Ext.get('divActiveInsert').show();
					choosePromotionModel();
					operatePromotTypeWin.show();			
				}
			},{
				text : '刷新',
				iconCls : 'btn_refresh',
				handler : function(){
					promotionTree.getRootNode().reload();
				}
			}
		],
		listeners : {
			load : function(thiz){
				var rn = promotionTree.getRootNode().childNodes;
				if(rn.length == 0){
					promotionTree.getRootNode().getUI().hide();
				}else{
					for(var i = (rn.length - 1); i >= 0; i--){
						if(typeof rn[i].attributes.expired != 'undefined'){
							rn[i].setText('<font style="color:#808080">' + rn[i].text + '&nbsp;(已过期)</font>');
						}
					}
					promotionTree.getRootNode().getUI().show();
				}
			},
			click : function(e){
				getPromotionBodyById(e.attributes.id);
			}
		}
	});
	
	var coupon_dateCombo = new Ext.form.ComboBox({
		xtype : 'combo',
		id : 'coupon_statusCombo',
		forceSelection : true,
		width : 100,
		store : new Ext.data.SimpleStore({
			fields : ['value', 'text']
		}),
		valueField : 'value',
		displayField : 'text',
		typeAhead : true,
		mode : 'local',
		triggerAction : 'all',
		selectOnFocus : true,
		listeners : {
			render : function(thiz){
				thiz.store.loadData([[1,'已发放'], [2,'已使用'], [3,'已过期']]);					
			},
			select : function(thiz, record, index){
				Ext.getCmp('btnSearchCoupon').handler();
			}
		}
	});
	
	promotionPreviewPanel = new Ext.Panel({
		title : '活动与信息汇总',
		layout : 'border',
		region : 'center',
		items : [new Ext.Panel({
			id : 'promotionPreviewBody',
			region : 'center',
			style : 'background-color: red; border: 1px solid #ccc; padding: 5px 5px 5px 5px;',
			html : '&nbsp;'			
		})]
	});
	
	new Ext.Panel({
		renderTo : 'divActive',
		layout : 'border',
		width : parseInt(Ext.getDom('divActive').parentElement.style.width.replace(/px/g,'')),
		height : parseInt(Ext.getDom('divActive').parentElement.style.height.replace(/px/g,'')),
		items : [promotionTree, promotionPreviewPanel]
	});
	showFloatOption(bar);
	

	
	
	
	
//-----------------------------优惠活动
	
function buildPreviewHead(){
	var head = '';
	if(Ext.getCmp('active_title').getValue()){
		head +=  '<div style="text-align:center; font-size: 30px; font-weight: bold; word-wrap:break-word; color: #D2691E;">' + Ext.getCmp('active_title').getValue() + '</div>';
	}else{
		head +=  '<div style="text-align:center; font-size: 30px; font-weight: bold; word-wrap:break-word; color: #D2691E;">活动标题</div>';
	}
	
	if(Ext.getCmp('active_beginDate').getValue() && Ext.getCmp('active_endDate').getValue()){
		head += '<div style="margin: 10px 10px 10px 10px; color:#aaa; font-szie:12px;">活动日期 : ' + Ext.getCmp('active_beginDate').getValue().format('Y-m-d') + '&nbsp;至&nbsp;' + Ext.getCmp('active_endDate').getValue().format('Y-m-d') + '</div>';
	}
	
	return head;
	
					
}	
	
	var coupon_uploadMask = new Ext.LoadMask(document.body, {
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
	var imgFile = Ext.ux.plugins.createImageFile({
		id : 'couponTypeBox',
		img : p_box,
		width : 100,
		height : 100
	});	
	var btnUpload = new Ext.Button({
		columnWidth : 0.4,
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
        	var couponImgId = '';
        	if(operatePromotTypeWin.otype == 'update'){
        		couponImgId = Ext.getCmp('txtCouponTypeId').getValue();
        	}
	        	coupon_uploadMask.show();
	        	Ext.Ajax.request({
	        		url : '../../OperateCouponType.do?dataSource=updateCouponImg&couponTypeId' + couponImgId,
 	   			isUpload : true,
 	   			form : form.getForm().getEl(),
 	   			success : function(response, options){
 	   				coupon_uploadMask.hide();
 	   				var jr = Ext.decode(response.responseText.replace(/<\/?[^>]*>/g,''));
 	   				operatePromotTypeWin.image = jr.other.imagePath;
 	   				Ext.ux.showMsg(jr);
 	   			},
 	   			failure : function(response, options){
 	   				coupon_uploadMask.hide();
 	   				Ext.ux.showMsg(Ext.decode(response.responseText.replace(/<\/?[^>]*>/g,'')));
 	   			}
	        	});
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
	
	
	
	var p_edit = new Ext.form.HtmlEditor({
		id : 'secondStep_edit',
		hideLabel : true,
		width : 520,
		height : 340,
		enableAlignments: false,
        enableColors: true,
        enableFont: false,
        enableFontSize: true,
        enableFormat: true,
        enableLinks: false,
        enableLists: false,
        enableSourceEdit: true,
//        fontFamilies: ["宋体", "隶书", "黑体"],
        plugins : [new Ext.ux.plugins.HEInsertImage({
        	url : '../../WXOperateMaterial.do?dataSource=upload&time=' + new Date().getTime()
        })]
	});
	var btnPreview = new Ext.Button({
		text : '预览',
		listeners : {
			render : function(thiz){
				thiz.getEl().setWidth(100, true);
			}
		},
		handler : function(){
			secendStepCenter.body.update(buildPreviewHead()+p_edit.getValue());
		}
	});
	var btnClear = new Ext.Button({
		text : '清空',
		listeners : {
			render : function(thiz){
				thiz.getEl().setWidth(100, true);
			}
		},
		handler : function(){
			p_edit.setValue();
			secendStepCenter.body.update(p_edit.getValue());
		}
	});
	var secendStepWest = new Ext.form.FormPanel({
		region : 'center',
		width : 510,
		items : [p_edit],
		style : 'marginLeft:40px;',
		buttonAlign : 'center',
		buttons : [btnPreview, btnClear]
	});
	var secendStepCenter = new Ext.Panel({
		id : 'secondStepEastBody',
		region : 'east',
		width : 400,
		height : 400,
		style : 'marginLeft:570px;background-color: #fff; border: 1px solid #ccc; padding: 5px 5px 5px 5px;',
		html : '&nbsp;'
	});	
	
	
	
	new Ext.Panel({
		id : 'active_secendStepPanel',
		renderTo :'active_secendStep',
		width :1015,
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
				labelWidth : 180,
				items : [{
					id : 'hide_activeOccupy',
					xtype : 'label',
					style : 'color:#DFE8F6',
					html : '单次消费积分满/累计积分满-有优惠劵'
				}]
			},{
				items : [{
					id : 'active_title',
					xtype : 'textfield',
					value : '中秋月圆',
					fieldLabel : '&nbsp;&nbsp;&nbsp;活动标题'
				}]
			}, {
				items : [{
					id : 'active_beginDate',
					xtype : 'datefield',
					width : 90,
					fieldLabel : '&nbsp;&nbsp;&nbsp;活动日期',
					format : 'Y-m-d',
					value : '2014-09-10',
					readOnly : false,
					allowBlank : false,
					blankText : '日期不能为空.'				
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
					id : 'active_endDate',
					xtype : 'datefield',
					width : 90,
					fieldLabel : '',
					format : 'Y-m-d',
					value : '2014-09-30',
					readOnly : false,
					allowBlank : false,
					blankText : '日期不能为空.'				
				}]				
			},{
				labelWidth : 180,
				items : [{
					id : 'active_point',
					xtype : 'textfield',
					value : 100,
					fieldLabel : '单次消费积分满/累计积分满'
				}]
			}]
		}]
	});
	new Ext.Panel({
		id : 'active_secendStep2Panel',
		renderTo :'active_secendStep2',
		width :1015,
		border : false,
 		items : [{
 			xtype : 'panel',
			layout : 'column',
			frame : true,
			defaults : {
				layout : 'form',
				labelWidth : 70,
				labelAlign : 'right',
				columnWidth : .33
			},
			items : [{
				columnWidth : .4,
				items : [{
					id : 'westPanel',
					xtype : 'panel',
					layout : 'column',
					defaults : {
						layout : 'form',
						labelWidth : 70,
						labelAlign : 'right',
						columnWidth : .33
					},
					items : [{
						items : [{
							xtype : 'label',
							style : 'margin-top:65px;',
							html : '&nbsp;&nbsp;创建优惠劵:&nbsp;&nbsp;',
							listeners : {
								render : function(e){
									chooseCouponModel(e, '46px');
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
									chooseCouponModel(e);
								},
								check : function(e){
									if(e.getValue()){
										changeCouponModel(0);
									}
								}
							}
						}]			
					},{
						items : [{
							xtype : 'radio',
							name : 'radioActiveType',
							inputValue : 1,
							hideLabel : true,
							boxLabel : '二十元券&nbsp;&nbsp;',
							listeners : {
								render : function(e){
									chooseCouponModel(e);
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
							inputValue : 1,
							hideLabel : true,
							boxLabel : '自定义',
							listeners : {
								render : function(e){
									chooseCouponModel(e);
								},						
								check : function(e){
									if(e.getValue()){
										changeCouponModel(2);
									}
								}
							}
						}]			
					}]
				}]					
				
			},{
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
							id : 'active_couponName',
							xtype : 'textfield',
							value : '十元中秋券',
							fieldLabel : '优惠劵名称'
						}]
					},{
						items : [{
							id : 'active_price',
							xtype : 'textfield',
							value : 10,
							fieldLabel : '&nbsp;&nbsp;&nbsp;面额'
						}]
					}, {
						items : [{
							id : 'active_couponExpiredDate',
							xtype : 'datefield',
							width : 130,
							fieldLabel : '&nbsp;&nbsp;&nbsp;有效期至',
							value : '2014-10-30',
							format : 'Y-m-d',
							readOnly : false,
							allowBlank : false,
							blankText : '日期不能为空.'				
						}]				
					}]
				}]					
				
			},{
				columnWidth : .6,
				items : [{
					xtype : 'panel',
					width : 360,
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
								html : '<sapn style="font-size:13px;color:green;">提示: 单张图片大小不能超过100KB.</span>'
							}]							
						},btnUpload],
					listeners : {
		 	    		render : function(e){
		 	    			Ext.getDom(e.getId()).setAttribute('enctype', 'multipart/form-data');
			 	  		}
		 	    	}	
				}]					
				
			}]
		}]
	});	
	
	new Ext.Panel({
		id : 'active_secendStep3Panel',
		renderTo :'active_secendStep3',
		width :1015,
		border : false,
		items : [{
			layout : 'border',
			frame : true,
			border : false,
			height : 535,
			items : [secendStepWest, secendStepCenter]
		}]
	});	
	
	
	
	
	
	
	var active_member_beginDate = new Ext.form.DateField({
		xtype : 'datefield',	
		id : 'active_dateSearchDateBegin',
		format : 'Y-m-d',
		width : 100,
		maxValue : new Date(),
		readOnly : false,
		allowBlank : false
	});
	var active_member_endDate = new Ext.form.DateField({
		xtype : 'datefield',
		id : 'active_dateSearchDateEnd',
		format : 'Y-m-d',
		width : 100,
		maxValue : new Date(),
		readOnly : false,
		allowBlank : false
	});
	var active_member_dateCombo = Ext.ux.createDateCombo({
		id : 'active_dateSearchDateCombo',
		width : 75,
		data : [[3, '近一个月'], [4, '近三个月'], [9, '近半年']],
		beginDate : active_member_beginDate,
		endDate : active_member_endDate,
		callback : function(){
			if(member_searchType){
				Ext.getCmp('active_btnSearchMember').handler();
			}
		}
	});	
	var active_memberBasicGridExcavateMemberTbar = new Ext.Toolbar({
		hidden : true,
		height : 28,
		items : [
			{xtype : 'tbtext', text : '日期:&nbsp;&nbsp;'},
			{xtype : 'tbtext', text : '&nbsp;&nbsp;'},
			active_member_dateCombo,
			{xtype : 'tbtext', text : '&nbsp;'},
			active_member_beginDate,
			{
				xtype : 'label',
				hidden : false,
				html : ' 至&nbsp;&nbsp;'
			}, 
			active_member_endDate,
			{xtype : 'tbtext', text : '&nbsp;&nbsp;'},
			{xtype : 'tbtext', text : '会员类型:'},
			{
				id : 'active_memberTypeCombo',
				xtype : 'combo',
				readOnly : false,
				forceSelection : true,
				value : -1,
				width : 100,
				store : new Ext.data.SimpleStore({
					fields : ['id', 'name']
				}),
				valueField : 'id',
				displayField : 'name',
				listeners : {
					render : function(thiz){
						var data = [[-1,'全部']];
						Ext.Ajax.request({
							url : '../../QueryMemberType.do',
							params : {dataSource : 'normal'},
							success : function(res, opt){
								var jr = Ext.decode(res.responseText);
								for(var i = 0; i < jr.root.length; i++){
									data.push([jr.root[i]['id'], jr.root[i]['name']]);
								}
								thiz.store.loadData(data);
								thiz.setValue(-1);
							},
							failure : function(res, opt){
								thiz.store.loadData(data);
								thiz.setValue(-1);
							}
						});
					}
				},
				typeAhead : true,
				mode : 'local',
				triggerAction : 'all',
				selectOnFocus : true
			}
			]
		
	});
	var active_memberBasicGridExcavateMemberTbar2 = new Ext.Toolbar({
		hidden : true,
		height : 28,
		items : [{xtype : 'tbtext', text : '消费金额:'},
					{
			xtype : 'numberfield',
			id : 'active_textTotalMinMemberCost',
			width : 60
		},
		{
			xtype : 'tbtext',
			text : '&nbsp;-&nbsp;'
		},			
		{
			xtype : 'numberfield',
			id : 'active_textTotalMaxMemberCost',
			width : 60
		},
		{xtype : 'tbtext', text : '&nbsp;&nbsp;'},	
		{xtype : 'tbtext', text : '消费次数:'},
		{
			xtype : 'numberfield',
			id : 'active_textTotalMinMemberCostCount',
			width : 50
		},
		{
			xtype : 'tbtext',
			text : '&nbsp;-&nbsp;'
		},			
		{
			xtype : 'numberfield',
			id : 'active_textTotalMaxMemberCostCount',
			width : 50
		},
		{xtype : 'tbtext', text : '&nbsp;&nbsp;'},	
		{xtype : 'tbtext', text : '余额:'},
		{
			id : 'active_memberBalanceEqual',
			xtype : 'combo',
			readOnly : false,
			forceSelection : true,
			value : '=',
			width : 70,
			store : new Ext.data.SimpleStore({
				fields : ['value', 'text'],
				data : [['=', '等于'], ['>=', '大于等于'], ['<=', '小于等于']]
			}),
			valueField : 'value',
			displayField : 'text',
			typeAhead : true,
			mode : 'local',
			triggerAction : 'all',
			selectOnFocus : true
		},				
		{
			xtype : 'numberfield',
			id : 'active_textMemberBalance',
			width : 60
		}]
	});
	
	var active_memberBasicGridTbar = new Ext.Toolbar({
		items : [{
				text : '一键挖掘活跃会员',
				iconCls : 'btn_add',
				handler : function(e){
					alert(1111111)
				}
			}, {
				text : '一键挖掘沉睡会员',
				iconCls : 'btn_edit',
				handler : function(e){
					alert(222222)
				}
			}, '->', {
			text : '高级条件↓',
	    	id : 'active_member_btnHeightSearch',
	    	handler : function(){
	    		member_searchType = true;
				Ext.getCmp('active_member_btnCommonSearch').show();
				
	    		Ext.getCmp('active_member_btnHeightSearch').hide();
	    		
	    		memberBasicGrid.setHeight(memberBasicGrid.getHeight()-56);
	    		
	    		active_memberBasicGridExcavateMemberTbar.show();
	    		active_memberBasicGridExcavateMemberTbar2.show();
	    		
	    		memberBasicGrid.syncSize(); //强制计算高度
	    		memberBasicGrid.doLayout();//重新布局 	
			}
		}, {
			text : '高级条件↑',
	    	id : 'active_member_btnCommonSearch',
			hidden : true,
	    	handler : function(){
	    		member_searchType = true;
				Ext.getCmp('active_member_btnHeightSearch').show();
	    		Ext.getCmp('active_member_btnCommonSearch').hide();
	    		
	    		
	    		active_memberBasicGridExcavateMemberTbar.hide();
	    		active_memberBasicGridExcavateMemberTbar2.hide();
	    		
	    		memberBasicGrid.setHeight(memberBasicGrid.getHeight()+56);
	    		memberBasicGrid.syncSize(); //强制计算高度
	    		memberBasicGrid.doLayout();//重新布局 	
	    		
	    		member_dateCombo.setValue(4);
	    		member_dateCombo.fireEvent('select', member_dateCombo,null,4);
	    		
	    		Ext.getCmp('active_textTotalMemberCost').setValue();
	    		Ext.getCmp('active_usedBalanceEqual').setValue('=');
	    		Ext.getCmp('active_textTotalMemberCostCount').setValue();
	    		Ext.getCmp('active_consumptionAmountEqual').setValue('=');
	    		Ext.getCmp('active_numberSearchByMemberPhoneOrCard').setValue();
	    		Ext.getCmp('active_memberBalanceEqual').setValue('=');
			}
		},{xtype : 'tbtext', text : '&nbsp;&nbsp;'},	 
		{
			text : '搜索',
			id : 'active_btnSearchMember',
			iconCls : 'btn_search',
			handler : function(){
				
				var memberType = Ext.getCmp('active_memberTypeCombo');
				
				var gs = memberBasicGrid.getStore();
				
				gs.baseParams['memberType'] = memberType.getValue();
				
				gs.baseParams['MinTotalMemberCost'] = Ext.getCmp('active_textTotalMinMemberCost').getValue();
				gs.baseParams['MaxTotalMemberCost'] = Ext.getCmp('active_textTotalMaxMemberCost').getValue();
				gs.baseParams['consumptionMinAmount'] = Ext.getCmp('active_textTotalMinMemberCostCount').getValue();
				gs.baseParams['consumptionMaxAmount'] = Ext.getCmp('active_textTotalMaxMemberCostCount').getValue();
				gs.baseParams['memberBalance'] = Ext.getCmp('active_textMemberBalance').getValue();
				gs.baseParams['memberBalanceEqual'] = Ext.getCmp('active_memberBalanceEqual').getValue();
				gs.load({
					params : {
						start : 0,
						limit : 200
					}
				});
			
				gs.on('load', function(store, records, options){
					active_memberList = '';
					for (var i = 0; i < records.length; i++) {
						if(i > 0){
							active_memberList += ",";
						}
						active_memberList += records[i].get('id');
					}
				});	
			}
		}]
	});	
	var memberBasicGrid = createGridPanel(
		'active_memberBasicGrid',
		'会员信息',
		480,
		640,
		'../../QueryMember.do',
		[
			[true, false, false, true],
			['名称', 'name'],
			['类型', 'memberType.name'],
			['当前积分', 'point',,'right', 'Ext.ux.txtFormat.gridDou'],
			['账户余额', 'totalBalance',,'right', 'Ext.ux.txtFormat.gridDou']
		],
		MemberBasicRecord.getKeys(),
		[['isPaging', true], ['restaurantID', 40],  ['dataSource', 'normal']],
		200,
		'',
		[active_memberBasicGridTbar, active_memberBasicGridExcavateMemberTbar,active_memberBasicGridExcavateMemberTbar2]
	);	
	memberBasicGrid.region = 'center';
	memberBasicGrid.loadMask = null;	

	

	var threeStepEast = new Ext.Panel({
		id : 'threeStepEastBody',
		region : 'east',
		width : 350,
		height : 565,
		style : 'marginLeft:650px;background-color: #fff; border: 1px solid #ccc; padding: 5px 5px 5px 5px;overflow-y: visible;',
		bodyStyle : 'word-wrap:break-word;',
		html : ''
	});		
	
	new Ext.Panel({
		renderTo :'active_threeStep',
		width :1015,
		border : false,
		items : [{
			layout : 'border',
			frame : true,
			border : false,
			height : 570,
			items : [memberBasicGrid, threeStepEast]
		}]
	});	
	
	
	//steps.js与Ext混用时的样式修正	
	$('#active_beginDate').parent().width($('#active_beginDate').width() + $('#active_beginDate').next().width()+20);
	$('#active_endDate').parent().width($('#active_endDate').width() + $('#active_endDate').next().width()+20);
	$('#active_couponExpiredDate').parent().width($('#active_couponExpiredDate').width() + $('#active_couponExpiredDate').next().width()+20);
	
	$('#active_dateSearchDateBegin').parent().width($('#active_dateSearchDateBegin').width() + $('#active_dateSearchDateBegin').next().width()+20);
	$('#active_dateSearchDateEnd').parent().width($('#active_dateSearchDateEnd').width() + $('#active_dateSearchDateEnd').next().width()+20);
 	$('#active_dateSearchDateCombo').parent().width($('#active_dateSearchDateCombo').width() + $('#active_dateSearchDateCombo').next().width()+20);
 	$('#active_memberBalanceEqual').parent().width($('#active_memberBalanceEqual').width() + $('#active_memberBalanceEqual').next().width()+20);
 	
 	$('#secondStepEastBody').children().first().children().first().css('overflow-y', 'visible');
 	$('#threeStepEastBody').children().first().children().first().css('overflow-y', 'visible');
});
