

/**
 * 修改部门信息
 */
function initCouponTypeWin(c){
	operatePromotTypeWin = Ext.getCmp('operatePromotTypeWin');
	if(!operatePromotTypeWin){
		operatePromotTypeWin = new Ext.Window({
			id : 'operatePromotTypeWin',
			closable : true,
			closeAction:'hide',
			resizable : false,
			modal : true,
			width : 1100,
			height : 720,
			bodyStyle : 'backgroundColor:#FFFFFF; border:1px solid #99BBE8;',
			contentEl : 'divActiveInsert',
			listeners : {
				beforehide : function(thiz){
					winUnhide = false;
					//先让操作跳到第三部再关闭, 否则样式有误
					var result = false;
					if($("#wizard").steps("getCurrentIndex") == 1){
						$("#wizard").steps("next");
						var i = 0
						var task = {
							run : function(){
								if(i > 0){
									Ext.TaskMgr.stop(this);
									thiz.hide();
								}
								i ++ ;
							},
							interval: 300
						};
					
						Ext.TaskMgr.start(task);						
					}else{
						result = true;
					}
					
					return result;
				},
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
					Ext.getCmp('active_point').clearInvalid();
					
					Ext.getDom('radioDefaultCoupon').checked = true; 
					Ext.getCmp('radioDefaultCoupon').fireEvent('check', Ext.getCmp('radioDefaultCoupon'), true);
					Ext.getCmp('couponTypeBox').setImg();
					
					Ext.getCmp('secondStepEastBody').body.update('');
					operatePromotTypeWin.image = '';
					
					Ext.getCmp('active_memberBasicGrid').getStore().removeAll();
					Ext.getCmp('active_memberTypeCombo').setValue(-1);
					
					Ext.getCmp('active_textTotalMemberCost').setValue();
					Ext.getCmp('active_textTotalMemberCostCount').setValue();
					
					active_memberList = '';
					
					operatePromotTypeWin.otype = '';
					operatePromotTypeWin.pId = '';
					operatePromotTypeWin.oriented = 1;
					Ext.getCmp('active_member_btnCommonSearch').handler({noSearch:true});
					$("#chkSetWelcome").removeAttr("checked");
					
					winUnhide = true;
					
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
	operatePromotTypeWin.otype = c.type;
	operatePromotTypeWin.setTitle(c.type == 'insert'?'添加优惠活动':'修改优惠活动');
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
	couponTypeId = type;
	couponName.clearInvalid();
	price.clearInvalid();
	couponName.focus();
}

//1表示无优惠劵纯展示; 2表示无条件领取优惠劵
function choosePromotionModel(){
	if(promotionType == 1){
		Ext.getCmp('active_secendStep2CouponImg').hide();
		Ext.getCmp('active_secendStep2CouponDetail').hide();
		Ext.getCmp('active_secendStep2SelectCoupon').hide();
		Ext.getCmp('secondStep_edit').setHeight(440);
		Ext.getCmp('active_point').hide();
		Ext.getCmp('active_point').getEl().up('.x-form-item').setDisplayed(false);
		Ext.getCmp('active_pointLastText').hide();
		Ext.getCmp('hide_activeOccupy').show();
	}else if(promotionType == 2){
		Ext.getCmp('active_secendStep2CouponImg').show();
		Ext.getCmp('active_secendStep2CouponDetail').show();
		Ext.getCmp('active_secendStep2SelectCoupon').show();
		Ext.getCmp('active_point').hide();
		Ext.getCmp('active_point').setValue(0);
		Ext.getCmp('active_point').getEl().up('.x-form-item').setDisplayed(false);
		Ext.getCmp('active_pointLastText').hide();
		Ext.getCmp('hide_activeOccupy').show();
		Ext.getCmp('secondStep_edit').setHeight(325);
	}else{
		Ext.getCmp('active_secendStep2CouponImg').show();
		Ext.getCmp('active_secendStep2CouponDetail').show();
		Ext.getCmp('active_secendStep2SelectCoupon').show();
		Ext.getCmp('active_point').show();
		Ext.getCmp('active_pointLastText').show();
		Ext.getCmp('active_point').getEl().up('.x-form-item').setDisplayed(true);	
		Ext.getCmp('hide_activeOccupy').hide();
		Ext.getCmp('secondStep_edit').setHeight(325);
		
		if(promotionType == 3){
			Ext.getCmp('active_point').label.dom.innerHTML = '活动规则: 单次消费积分满'
		}else if(promotionType == 4){
			Ext.getCmp('active_point').label.dom.innerHTML = '活动规则: 累计积分满'
		}
		
		
	}
	Ext.getCmp('active_secendStepPanel').doLayout();
}

function selectPromotionModel(thiz, type){
	$(".active_mould").removeClass('active_mould_click');
	$(thiz).find('input').attr('checked', 'checked');
	$(thiz).addClass('active_mould_click');
	promotionType = type;
	choosePromotionModel();
}

function promotionRule(pType, point){
	if(point){
		var rule = '';
		if(pType == 3){
			rule = '活动规则 : 单次消费积分满<font style="color: red">' + point + '</font>分即可领取优惠劵';
		}else if(pType == 4){
			rule = '活动规则 : 累计消费积分满<font style="color: red">' + point + '</font>分即可领取优惠劵';
		}
		return '<div style="margin: 10px 10px 10px 10px;color:LightSkyBlue; font-szie:14px;font-weight:bold;">'+ rule +'</div>';	
	}else{
		return '';
	}
	

}

function getPromotionBodyById(id){
	Ext.Ajax.request({
		url : '../../OperatePromotion.do',
		params : {promotionId : id, dataSource : 'getPromotion'},
		success : function(res, opt){
			var jr = Ext.decode(res.responseText);
			if(jr.success){
				Ext.getCmp('promotionPreviewBody').body.update('<div style="text-align:center; font-size: 30px; font-weight: bold; word-wrap:break-word; color: #D2691E;">' + jr.root[0].title + '</div>' +
																'<div style="margin: 10px 10px 10px 10px; color:LightSkyBlue; font-szie:15px;font-weight:bold">活动日期 : <font color="red">' + jr.root[0].promotionBeginDate + '</font>&nbsp;至&nbsp;<font color="red">' + jr.root[0].promotionEndDate + '</font></div>' +
																promotionRule(jr.root[0].pType, jr.root[0].point) +
																jr.root[0].body);
				if(jr.root[0].coupon){
					Ext.getCmp('promotionCouponPreview').body.update('<div style="text-align:left; margin: 10px 10px 10px 20px;float:left;"><img height="160"  src="' + (jr.root[0].coupon.ossImage?jr.root[0].coupon.ossImage.image:'http://digie-image-real.oss.aliyuncs.com/nophoto.jpg') + '" /></div>'
																+ '<div style="float:left;vertical-align: middle;line-height: 40px;"><br><span style="margin-top: 15px;">' + jr.root[0].coupon.name + '</span><br><span >面额 : ' + jr.root[0].coupon.price + ' 元</span><br><span >到期 : ' + jr.root[0].coupon.expiredFormat + '</span></div>');							
				}else{
					Ext.getCmp('promotionCouponPreview').body.update('<div style="text-align:center; margin: 10px 10px 10px 10px;"><img height="160"  src="../../images/noCouponNow.png" /></div>');
				}												
											
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
			}else{
				Ext.ux.showMsg(jr);
			}
		},
		failure : function(res, opt){
			Ext.ux.showMsg(Ext.decode(res.responseText));
		}
	});
}

function fnCancelPublishPromotion(){
	var sn = Ext.ux.getSelNode(promotionTree);
	Ext.Ajax.request({
		url : '../../OperatePromotion.do',
		params : {promotionId : sn.attributes.id, dataSource : 'cancelPublish'},
		success : function(res, opt){
			var jr = Ext.decode(res.responseText);
			if(jr.success){
				Ext.example.msg(jr.title, jr.msg);
				promotionTree.getRootNode().reload();
			}else{
				Ext.ux.showMsg(jr);
			}
		},
		failure : function(res, opt){
			Ext.ux.showMsg(Ext.decode(res.responseText));
		}
	});
}

function fnDeletePromotionPromotion(){
	var node = Ext.ux.getSelNode(promotionTree);
	if (!node || node.attributes.id == -1) {
		Ext.example.msg('提示', '操作失败, 请选择一个活动再进行删除.');
		return;
	}	
	Ext.Msg.confirm(
		'提示',
		'是否删除活动:&nbsp;<font color="red">' + node.text + '</font>',
		function(e){
			if(e == 'yes'){
				Ext.Ajax.request({
					url : '../../OperatePromotion.do',
					params : {promotionId : node.attributes.id, dataSource : 'delete'},
					success : function(res, opt){
						var jr = Ext.decode(res.responseText);
						if(jr.success){
							Ext.example.msg(jr.title, jr.msg);
							promotionTree.getRootNode().reload();
						}else{
							Ext.ux.showMsg(jr);
						}
					},
					failure : function(res, opt){
						Ext.ux.showMsg(Ext.decode(res.responseText));
					}
				});
			}
		},
		this
	);
}

function fnFinishPromotion(){
	var node = Ext.ux.getSelNode(promotionTree);
	if (!node || node.attributes.id == -1) {
		Ext.example.msg('提示', '操作失败, 请选择一个活动再进行操作.');
		return;
	}	
	Ext.Msg.confirm(
		'提示',
		'是否结束活动:&nbsp;<font color="red">' + node.text + '</font>',
		function(e){
			if(e == 'yes'){
				Ext.Ajax.request({
					url : '../../OperatePromotion.do',
					params : {promotionId : node.attributes.id, dataSource : 'finish'},
					success : function(res, opt){
						var jr = Ext.decode(res.responseText);
						if(jr.success){
							Ext.example.msg(jr.title, jr.msg);
							promotionTree.getRootNode().reload();
						}else{
							Ext.ux.showMsg(jr);
						}
					},
					failure : function(res, opt){
						Ext.ux.showMsg(Ext.decode(res.responseText));
					}
				});
			}
		},
		this
	);
}

function operatePromotionData(data){
	var title = Ext.getCmp('active_title');
	var beginDate = Ext.getCmp('active_beginDate');
	var endDate = Ext.getCmp('active_endDate');
	var point = Ext.getCmp('active_point');
	var couponName = Ext.getCmp('active_couponName');
	var price = Ext.getCmp('active_price');
	var expiredDate= Ext.getCmp('active_couponExpiredDate');
	var imgBox = Ext.getCmp('p_couponImg');
	var editBody = Ext.getCmp('secondStep_edit');
	
	title.setValue(data.title);
	beginDate.setValue(data.promotionBeginDate);
	endDate.setValue(data.promotionEndDate);
	point.setValue(data.point);
	editBody.setValue(data.body);
	
	
	if(promotionType > 1){
		Ext.getDom('radioSelfCoupon').checked = true; 
		couponTypeId = data.coupon.id;
		expiredDate.setValue(data.coupon.expiredFormat);
		Ext.getCmp('couponTypeBox').setImg(data.coupon.ossImage?data.coupon.ossImage.image:'http://digie-image-real.oss.aliyuncs.com/nophoto.jpg');
	}
	
	if(data.oriented == 1){
		Ext.getCmp('rdoSendAllMember').setValue(true);
	}else{
		Ext.getCmp('rdoSendSpecificMember').setValue(true);
	}
	operatePromotTypeWin.oriented = data.oriented;
	
	if(data.members.length > 0){
		var gs = Ext.getCmp('active_memberBasicGrid').getStore();
		gs.on('load', function(store, records, options){
			active_memberList = '';
			for (var i = 0; i < records.length; i++) {
				if(i > 0){
					active_memberList += ",";
				}
				active_memberList += records[i].get('id');
			}
		});		
		gs.loadData({
			totalProperty : data.members.length,
			root : data.members.slice(0, 200)								
		});	
	}
	

}

function fnUpdatePromotion(){
	var node = Ext.ux.getSelNode(promotionTree);
	if (!node || node.attributes.id == -1) {
		Ext.example.msg('提示', '操作失败, 请选择一个活动再进行操作.');
		return;
	}	
	promotionType = node.attributes.pType;
	choosePromotionModel();
	initCouponTypeWin({type : 'update'});
	operatePromotTypeWin.pId = node.attributes.id;
	
	Ext.Ajax.request({
		url : '../../OperatePromotion.do',
		params : {promotionId : node.attributes.id, dataSource : 'getById'},
		success : function(res, opt){
			var jr = Ext.decode(res.responseText);
			if(jr.success){
				Ext.get('divActiveInsert').show();
				operatePromotionData(jr.other);
				operatePromotTypeWin.show();	
				$("#wizard").steps("next");
			}
		},
		failure : function(res, opt){
			Ext.ux.showMsg(Ext.decode(res.responseText));
		}
	});
	
}

function fnCheckHaveWelcome(){
	$.ajax({
		url : '../../OperatePromotion.do',
		type : 'post',
		async:false,
		data : {dataSource : 'HaveWelcomePage'},
		success : function(jr, status, xhr){
			if(jr.root.length > 0){
				$('#spanSetWelcome').hide();
			}else{
				$('#spanSetWelcome').show();
			}
		},
		error : function(request, status, err){
			Ext.ux.showMsg({success : false, msg : '请求失败, 请刷新页面'});
		}
	}); 
}

var promotionPreviewPanel, memberCountGrid;
var sendCouponWin, couponViewBillWin;
var member_searchType = false;
var bar = {treeId : 'promotionTree',operateTree:Ext.ux.operateTree_promotion, mult : [{status : 1, option :[{name : '发布', fn : "fnPublishPromotion()"}, {name : '修改', fn : "fnUpdatePromotion()"}, {name : '删除', fn : "fnDeletePromotionPromotion()"}]}, 
											{status : 2, option :[{name : '撤销', fn : "fnCancelPublishPromotion()"}]},
											{status : 3, option :[{name : '结束', fn : "fnFinishPromotion()"}]},
											{status : 4, option :[{name : '删除', fn : "fnDeletePromotionPromotion()"}]} ]};
Ext.onReady(function() {
	initCouponTypeWin({type:'insert'});
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
				expand : function(thiz){
					var rn = promotionTree.getRootNode().childNodes;
					var node = null;
					if(rn.length == 0){
						promotionTree.getRootNode().getUI().hide();
					}else{
						node = rn[0].childNodes[0];
					}
	        		if(node != null){
	        			node.select();
	        			node.fireEvent('click', node);
	        			node.fireEvent('dblclick', node);
	        			
					}	
				}
			}
		}),
		tbar : [
		    '->', 
		    {
				text : '创建活动',
				iconCls : 'btn_add',
				handler : function(e){
					Ext.get('divActiveInsert').show();
					choosePromotionModel();
					initCouponTypeWin({type : 'insert'});
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
				fnCheckHaveWelcome();
				console.log(111111111)
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
		layout : 'border',
		region : 'center',
		items : [new Ext.Panel({
			id : 'promotionPreviewBody',
			region : 'center',
			html : '<div style="text-align:center;background-color:#F5F5F5;height:100%"><img src="http://digie-image-real.oss-cn-hangzhou.aliyuncs.com/WxPromotion/noPromotion.png" width="100%" /></div>'			
		}), new Ext.Panel({
			region : 'east',
			width : 450,
			layout : 'border',
			items : [new Ext.Panel({
				title : '优惠劵展示',
				id : 'promotionCouponPreview',
				region : 'north',
				height : 210,
				html : '<div style="text-align:center; margin: 10px 10px 10px 10px;"><img height="160"  src="http://digie-image-real.oss-cn-hangzhou.aliyuncs.com/WxPromotion/noCoupon.png" /></div>'
			}),new Ext.Panel({
				title : '活动信息汇总',
				id : 'promotionGeneral',
				region : 'center',
				height : 350,
				html : '&nbsp;'			
			})]		
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
        	coupon_uploadMask.show();
        	Ext.Ajax.request({
        		url : '../../OperateImage.do?dataSource=upload&ossType=3',
 	   			isUpload : true,
 	   			form : form.getForm().getEl(),
 	   			success : function(response, options){
 	   				coupon_uploadMask.hide();
 	   				var jr = Ext.decode(response.responseText.replace(/<\/?[^>]*>/g,''));
 	   				if(jr.success){
// 	   					Ext.ux.showMsg(jr);
	  	   				var ossImage = jr.root[0];
	 	   				operatePromotTypeWin.image = ossImage.image;
	 	   				operatePromotTypeWin.ossId = ossImage.imageId;	   				
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
	
	var imgFile = Ext.ux.plugins.createImageFile({
		id : 'couponTypeBox',
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
	
	
	
	var p_edit = new Ext.form.HtmlEditor({
		id : 'secondStep_edit',
		hideLabel : true,
		width : 510,
		height : 325,
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
        	url : '../../OperateImage.do?dataSource=upload&ossType=1'
        })]
	});
	var btnPreview = new Ext.Button({
		id : 'btnSecondStepEastBody',
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
		width : 510,
		title : '编辑活动内容:',
		items : [p_edit],
		style : 'marginLeft:40px;',
		buttonAlign : 'center',
		buttons : [btnPreview, btnClear]
	});
	var secendStepCenter = new Ext.Panel({
		id : 'secondStepEastBody',
		region : 'east',
		width : 360,
		height : 525,
		bodyStyle : 'word-wrap:break-word;',
		style : 'marginLeft:635px;background-color: #fff; border: 1px solid #ccc; padding: 5px 5px 5px 5px;',
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
				columnWidth : .3,
				items : [{
					id : 'active_title',
					xtype : 'textfield',
					width : 200,
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
					id : 'active_beginDate',
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
					id : 'active_endDate',
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
				labelWidth : 145,
				items : [{
					id : 'active_point',
					xtype : 'numberfield',
					width : 90,
					fieldLabel : '单次消费积分满/累计积分满',
					allowBlank : false,
					listeners : {
						blur : function(){
							Ext.getCmp('btnSecondStepEastBody').handler();
						}
					}	
				}]
			},{
				labelWidth : 95,
				items : [{
					id : 'active_pointLastText',
					xtype : 'label',
					width : 90,
					text : '可领取优惠劵',
					listeners : {
						render : function(e){
							e.getEl().dom.parentNode.style.paddingTop = '3px';
						}
					}
				}]
			}]
		}]
	});
	new Ext.Panel({
		renderTo :'active_secendStep2',
		width :1015,
		border : false,
		items : [{
			layout : 'border',
			frame : true,
			border : false,
			height : 545,
			items : [{
	 			xtype : 'panel',
	 			region : 'center',
	 			width :620,
	 			height : 535,
				layout : 'column',
				frame : true,
				defaults : {
					layout : 'form',
					labelWidth : 70,
					labelAlign : 'right'
//					columnWidth : .33
				},
				items : [{
					id : 'active_secendStep2SelectCoupon',
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
								id : 'radioSelfCoupon',
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
				id : 'active_secendStep2CouponDetail',
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
							fieldLabel : '优惠劵名称',
							value : '十元优惠劵',
							allowBlank : false
						}]
					},{
						items : [{
							id : 'active_price',
							xtype : 'textfield',
							value : 10,
							fieldLabel : '&nbsp;&nbsp;&nbsp;面额',
							allowBlank : false
						}]
					}, {
						items : [{
							id : 'active_couponExpiredDate',
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
				id : 'active_secendStep2CouponImg',
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
			},secendStepCenter]
		}]
	});	
	
	var active_member_beginDate = new Ext.form.DateField({
		xtype : 'datefield',	
		id : 'active_dateSearchDateBegin',
		format : 'Y-m-d',
		width : 100,
		maxValue : new Date(),
		readOnly : false
	});
	var active_member_endDate = new Ext.form.DateField({
		xtype : 'datefield',
		id : 'active_dateSearchDateEnd',
		format : 'Y-m-d',
		width : 100,
		maxValue : new Date(),
		readOnly : false
	});
	var active_member_dateCombo = Ext.ux.createDateCombo({
		id : 'active_dateSearchDateCombo',
		width : 75,
		data : [[3, '近一个月'], [4, '近三个月'], [9, '近半年'], [10, '无限期']],
		beginDate : active_member_beginDate,
		endDate : active_member_endDate,
		callback : function(){
/*			if(member_searchType){
				Ext.getCmp('active_btnSearchMember').handler();
			}*/
		}
	});	
	var active_memberBasicGridExcavateMemberTbar = new Ext.Toolbar({
		hidden : true,
		height : 28,
		items : [
			{
				text : '活跃会员',
				iconCls : 'btn_add',
				handler : function(e){
//					Ext.getCmp('active_member_btnHeightSearch').handler();
					var gs = memberBasicGrid.getStore();
					Ext.Ajax.request({
						url : '../../QueryMember.do',
						params : {dataSource : 'active'},
						success : function(res, opt){
							var jr = Ext.decode(res.responseText);
							gs.loadData({
								totalProperty : jr.root.length,
								root : jr.root.slice(0, 200)								
							});
							
							Ext.getCmp('active_dateSearchDateBegin').setValue(jr.other.beginDate.substring(0,10));
							Ext.getCmp('active_dateSearchDateEnd').setValue(jr.other.endDate.substring(0,10));
							Ext.getCmp('active_dateSearchDateCombo').setValue(jr.other.range);
							Ext.getCmp('active_textTotalMemberCostCount').setValue(jr.other.minConsumeAmount);
							Ext.getCmp('active_memberAmountEqual').setValue(1);
							
							gs.baseParams['consumptionMinAmount'] = jr.other.minConsumeAmount;
							gs.baseParams['consumptionMaxAmount'] = '';
							gs.baseParams['beginDate'] = jr.other.beginDate;
							gs.baseParams['endDate'] = jr.other.endDate;
						},
						failure : function(res, opt){
						}
					});
				}
			}, {
				text : '沉睡会员',
				iconCls : 'btn_edit',
				handler : function(e){
//					Ext.getCmp('active_member_btnHeightSearch').handler();
					var gs = memberBasicGrid.getStore();
					Ext.Ajax.request({
						url : '../../QueryMember.do',
						params : {dataSource : 'idle'},
						success : function(res, opt){
							var jr = Ext.decode(res.responseText);
							gs.loadData({
								totalProperty : jr.root.length,
								root : jr.root.slice(0, 200)								
							});
							
							Ext.getCmp('active_dateSearchDateBegin').setValue(jr.other.beginDate.substring(0,10));
							Ext.getCmp('active_dateSearchDateEnd').setValue(jr.other.endDate.substring(0,10));
							Ext.getCmp('active_dateSearchDateCombo').setValue(jr.other.range);
							Ext.getCmp('active_textTotalMemberCostCount').setValue(jr.other.maxConsumeAmount);
							Ext.getCmp('active_memberAmountEqual').setValue(2);
							
							gs.baseParams['consumptionMaxAmount'] = jr.other.maxConsumeAmount;
							gs.baseParams['consumptionMinAmount'] = '';
							gs.baseParams['beginDate'] = jr.other.beginDate;
							gs.baseParams['endDate'] = jr.other.endDate;
						},
						failure : function(res, opt){
						}
					});
				}
			},
			{xtype : 'tbtext', text : '&nbsp;&nbsp;&nbsp;&nbsp;'},
			
			{xtype : 'tbtext', text : '日期:&nbsp;&nbsp;'},
			active_member_dateCombo,
			active_member_beginDate,
			{
				xtype : 'label',
				hidden : false,
				html : ' 至&nbsp;&nbsp;'
			}, 
			active_member_endDate,'->',	 
			{
				text : '搜索',
				id : 'active_btnSearchMember',
				iconCls : 'btn_search',
				handler : function(){
					
					var memberType = Ext.getCmp('active_memberTypeCombo');
					
					var gs = memberBasicGrid.getStore();
					
					gs.baseParams['memberType'] = memberType.getValue();
					
					if(Ext.getCmp('active_memberCostEqual').getValue() == 1){
						gs.baseParams['MinTotalMemberCost'] = Ext.getCmp('active_textTotalMemberCost').getValue();
						gs.baseParams['MaxTotalMemberCost'] = '';
						
					}else if(Ext.getCmp('active_memberCostEqual').getValue() == 2){
						gs.baseParams['MinTotalMemberCost'] = '';
						gs.baseParams['MaxTotalMemberCost'] = Ext.getCmp('active_textTotalMemberCost').getValue();				
					}else{
						gs.baseParams['MinTotalMemberCost'] = Ext.getCmp('active_textTotalMemberCost').getValue();
						gs.baseParams['MaxTotalMemberCost'] = Ext.getCmp('active_textTotalMemberCost').getValue();				
					}
					
					if(Ext.getCmp('active_memberAmountEqual').getValue() == 1){
						gs.baseParams['consumptionMinAmount'] = Ext.getCmp('active_textTotalMemberCostCount').getValue();
						gs.baseParams['consumptionMaxAmount'] = '';				
					}else if(Ext.getCmp('active_memberAmountEqual').getValue() == 2){
						gs.baseParams['consumptionMinAmount'] = '';
						gs.baseParams['consumptionMaxAmount'] = Ext.getCmp('active_textTotalMemberCostCount').getValue();					
					}else{
						gs.baseParams['consumptionMinAmount'] = Ext.getCmp('active_textTotalMemberCostCount').getValue();
						gs.baseParams['consumptionMaxAmount'] = Ext.getCmp('active_textTotalMemberCostCount').getValue();					
					}
					
					if(member_searchType){
						gs.baseParams['beginDate'] = Ext.getCmp('active_dateSearchDateBegin').getValue().format('Y-m-d 00:00:00');
						gs.baseParams['endDate'] = Ext.getCmp('active_dateSearchDateEnd').getValue().format('Y-m-d 23:59:59');					
					}else{
						gs.baseParams['beginDate'] = '';
						gs.baseParams['endDate'] = '';					
					}

					gs.load({
						params : {
							start : 0,
							limit : 200
						}
					});
				}
			}]
		
	});
	var active_memberBasicGridExcavateMemberTbar2 = new Ext.Toolbar({
		hidden : true,
		height : 28,
		items : [{xtype : 'tbtext', text : '消费金额:'},
		{
			id : 'active_memberCostEqual',
			xtype : 'combo',
			readOnly : false,
			forceSelection : true,
			value : 3,
			width : 80,
			store : new Ext.data.SimpleStore({
				fields : ['value', 'text'],
				data : [[3, '等于'], [1, '大于等于'], [2, '小于等于']]
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
			id : 'active_textTotalMemberCost',
			width : 60
		},
		{xtype : 'tbtext', text : '&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;'},	
		{xtype : 'tbtext', text : '消费次数:'},
		{
			id : 'active_memberAmountEqual',
			xtype : 'combo',
			readOnly : false,
			forceSelection : true,
			value : 3,
			width : 80,
			store : new Ext.data.SimpleStore({
				fields : ['value', 'text'],
				data : [[3, '等于'], [1, '大于等于'], [2, '小于等于']]
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
			id : 'active_textTotalMemberCostCount',
			width : 50
		},
		{xtype : 'tbtext', text : '&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;'},
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
				},
				select : function(){
					Ext.getCmp('active_member_btnHeightSearch').handler();
					Ext.getCmp('active_btnSearchMember').handler();
				}
			},
			typeAhead : true,
			mode : 'local',
			triggerAction : 'all',
			selectOnFocus : true
		}]
	});
	
	var active_memberBasicGridTbar = new Ext.Toolbar({
		items : [{xtype : 'tbtext', text : '&nbsp;&nbsp;&nbsp;&nbsp;'},
			{
				xtype : 'radio',
				id : 'rdoSendAllMember',
				name : 'rdoFormatType',
				inputValue : 1,
				boxLabel : '全部会员',
				checked : true,
				listeners : {
					render : function(e){
						Ext.getDom('rdoSendAllMember').onclick = function(){
							e.setValue(true);
							Ext.getCmp('active_member_btnCommonSearch').handler();
							operatePromotTypeWin.oriented = e.inputValue;
						};
					}
				}
			},
			{xtype : 'tbtext', text : '&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;'},
			{
				xtype : 'radio',
				id : 'rdoSendSpecificMember',
				name : 'rdoFormatType',
				inputValue : 2,
				boxLabel : '部分会员',
				listeners : {
					render : function(e){
						Ext.getDom('rdoSendSpecificMember').onclick = function(){
							e.setValue(true);
							Ext.getCmp('active_member_btnHeightSearch').handler();
							operatePromotTypeWin.oriented = e.inputValue;
						};
					}
				}
			},
			{xtype : 'tbtext', text : '&nbsp;&nbsp;'}, '->', {
			text : '高级条件↓',
	    	id : 'active_member_btnHeightSearch',
	    	handler : function(){
	    		
				Ext.getCmp('active_member_btnCommonSearch').show();
				
	    		Ext.getCmp('active_member_btnHeightSearch').hide();
	    		
	    		Ext.getCmp('rdoSendSpecificMember').setValue(true);
	    		operatePromotTypeWin.oriented = 2;
	    		
	    		if(!member_searchType){
	    			memberBasicGrid.setHeight(memberBasicGrid.getHeight()-56);
	    		}
	    		member_searchType = true;
	    		
	    		active_memberBasicGridExcavateMemberTbar.show();
	    		active_memberBasicGridExcavateMemberTbar2.show();
	    		
	    		memberBasicGrid.syncSize(); //强制计算高度
	    		memberBasicGrid.doLayout();//重新布局 	
	    		
//	    		Ext.getCmp('active_memberBasicGrid').getStore().removeAll();
			}
		}, {
			text : '高级条件↑',
	    	id : 'active_member_btnCommonSearch',
			hidden : true,
	    	handler : function(e){
	    		member_searchType = false;
				Ext.getCmp('active_member_btnHeightSearch').show();
	    		Ext.getCmp('active_member_btnCommonSearch').hide();
	    		
	    		Ext.getCmp('rdoSendAllMember').setValue(true);
	    		operatePromotTypeWin.oriented = 1;
	    		
	    		active_memberBasicGridExcavateMemberTbar.hide();
	    		active_memberBasicGridExcavateMemberTbar2.hide();
	    		
	    		memberBasicGrid.setHeight(memberBasicGrid.getHeight()+56);
	    		memberBasicGrid.syncSize(); //强制计算高度
	    		memberBasicGrid.doLayout();//重新布局 	
	    		
	    		active_member_dateCombo.setValue(4);
	    		active_member_dateCombo.fireEvent('select', active_member_dateCombo,{data : {value : 4}},4);
	    		
	    		Ext.getCmp('active_memberTypeCombo').setValue(-1);
	    		Ext.getCmp('active_textTotalMemberCost').setValue();
	    		Ext.getCmp('active_textTotalMemberCostCount').setValue();
	    		Ext.getCmp('active_memberAmountEqual').setValue(3);
	    		Ext.getCmp('active_memberCostEqual').setValue(3);
	    		
	    		if(!e || typeof e.noSearch == 'undefined'){
	    			Ext.getCmp('active_btnSearchMember').handler();
	    		}
	    		
			}
		},{xtype : 'tbtext', text : '&nbsp;&nbsp;'}]
	});	
	memberBasicGrid = createGridPanel(
		'active_memberBasicGrid',
		'选择参与活动的会员',
		480,
		640,
		'../../QueryMember.do',
		[
			[true, false, false, true],
			['名称', 'name'],
			['类型', 'memberType.name'],
			['消费次数', 'consumptionAmount',,'right', 'Ext.ux.txtFormat.gridDou'],
			['当前积分', 'point',,'right', 'Ext.ux.txtFormat.gridDou'],
			['账户余额', 'totalBalance',180,'right', 'Ext.ux.txtFormat.gridDou']
		],
		MemberBasicRecord.getKeys(),
		[['isPaging', true], ['restaurantID', 40],  ['dataSource', 'normal']],
		200,
		'',
		[active_memberBasicGridTbar, active_memberBasicGridExcavateMemberTbar,active_memberBasicGridExcavateMemberTbar2]
	);	
	memberBasicGrid.region = 'center';
	
	memberBasicGrid.store.on('load', function(store, records, options){
		active_memberList = '';
		for (var i = 0; i < records.length; i++) {
			if(i > 0){
				active_memberList += ",";
			}
			active_memberList += records[i].get('id');
		}
	});		

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
	
	fnCheckHaveWelcome();
	
	//steps.js与Ext混用时的样式修正	
	$('#active_beginDate').parent().width($('#active_beginDate').width() + $('#active_beginDate').next().width()+20);
	$('#active_endDate').parent().width($('#active_endDate').width() + $('#active_endDate').next().width()+20);
	$('#active_couponExpiredDate').parent().width($('#active_couponExpiredDate').width() + $('#active_couponExpiredDate').next().width()+20);
	
	$('#active_dateSearchDateBegin').parent().width($('#active_dateSearchDateBegin').width() + $('#active_dateSearchDateBegin').next().width()+20);
	$('#active_dateSearchDateEnd').parent().width($('#active_dateSearchDateEnd').width() + $('#active_dateSearchDateEnd').next().width()+20);
 	$('#active_dateSearchDateCombo').parent().width($('#active_dateSearchDateCombo').width() + $('#active_dateSearchDateCombo').next().width()+20);
 	$('#active_memberCostEqual').parent().width($('#active_memberCostEqual').width() + $('#active_memberCostEqual').next().width()+20);
 	$('#active_memberAmountEqual').parent().width($('#active_memberAmountEqual').width() + $('#active_memberAmountEqual').next().width()+20);
 	
 	$('#secondStepEastBody').children().first().children().first().css('overflow-y', 'visible');
 	$('#threeStepEastBody').children().first().children().first().css('overflow-y', 'visible');
 	$('#promotionPreviewBody').children().first().children().first().css('overflow-y', 'visible');
 	
});
