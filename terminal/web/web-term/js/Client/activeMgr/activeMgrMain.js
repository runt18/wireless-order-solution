
var promotionTree, promotionGuideWin;
var couponPicBox;

function buildPromotionHeader(title, endDate){
	var head = [];
	if(title){
		head.push('<div style="text-align:center; font-size: 30px; font-weight: bold; word-wrap:break-word; color: #D2691E;">' + title + '</div>');
	}else{
		head.push('<div style="text-align:center; font-size: 30px; font-weight: bold; word-wrap:break-word; color: #D2691E;">活动标题</div>');
	}
	
	if(endDate){
		head.push('<div style="margin: 10px 10px 10px 10px; color:LightSkyBlue; font-szie:12px;font-weight:bold">活动至<font color="red">&nbsp;' + endDate.format('Y-m-d') +'</font>&nbsp;结束</div>');
	}
	
	return head;
}

function fnFinishPromotion(){
	
	var title = Ext.getCmp('guide_2nd_title');
	if(title.getValue().length == 0){
		Ext.example.msg('提示', '请设置活动标题');
		Ext.getCmp('guide_2nd_title').focus();
		return;
	}
	var couponName = Ext.getCmp('guide_2nd_couponName');
	var price = Ext.getCmp('guide_2nd_couponPrice');
	var expiredDate= Ext.getCmp('guide_2nd_couponExpired');

	var params = {};
	if(promotionGuideWin.otype == 'insert'){
		params.dataSource = 'insert';
		//面向全员
		params.oriented = 1;
	}else{
		params.dataSource = 'update';
		params.id = promotionGuideWin.promotion.id;
		params.cId = promotionGuideWin.promotion.coupon.id;
	}
	
	params.pRule = promotionGuideWin.promotionType;
	params.couponName = couponName.getValue();
	params.price = price.getValue();
	params.expiredDate = expiredDate.getValue().format('Y-m-d');
	params.image = promotionGuideWin.ossId;
	params.title = title.getValue();
	//params.beginDate = beginDate.getValue().format('Y-m-d');
	//params.endDate = endDate.getValue().format('Y-m-d');
	
	params.body = Ext.getCmp('guide_2nd_promotionEditor').getValue();	
	params.entire = buildPromotionHeader(title.getValue()) + params.body;
	
	var promotionUploadMask = new Ext.LoadMask(document.body, {	msg : '正在保存活动...' });
	promotionUploadMask.show();
	Ext.Ajax.request({
		url : '../../OperatePromotion.do',
		params : params,
		success : function(res, opt){
			var jr = Ext.decode(res.responseText);
			promotionUploadMask.hide();
			if(jr.success){
				promotionGuideWin.hide();
				
				var promotionId;
				if(params.dataSource == 'insert'){
					promotionId = jr.root[0].pid;
				}else{
					promotionId = params.id;
				}
				//重新加载显示刚刚修改的活动
				promotionTree.getRootNode().reload(function(){
					for(var i in this.childNodes){
						var statusNode = this.childNodes[i];
						for(var j in statusNode.childNodes){
							var promotionNode = statusNode.childNodes[j];
							if(promotionNode.id == promotionId){
								promotionNode.select();
								promotionNode.fireEvent('click', promotionNode);
								break;
							}
						}
					}
				});
				
				Ext.example.msg(jr.title, jr.msg);
			}else{
				Ext.ux.showMsg(jr);
			}
		},
		fialure : function(res, opt){
			promotionUploadMask.hide();
			wx.lm.hide();
			Ext.ux.showMsg(res.responseText);
		}
	});	
	
};


$(function (){
    $("#wizard").steps({
        headerTag: "span",
        bodyTag: "div",
        transitionEffect: "slideLeft",
        labels: {
            current: "current step:",
            pagination: "Pagination",
            finish: "完成",
            next: "下一步",
            previous: "上一步",
            loading: "加载中 ..."
        },
        onStepChanged: function (event, currentIndex, priorIndex) { 
        	if(currentIndex == 1){
        		Ext.getCmp('guide_2nd_title').focus(true, 100);
        		Ext.getCmp('guide_2nd_previewBtn').handler();
        	}
        },
        onStepChanging : function(event, currentIndex, newIndex){
        	if(currentIndex == 1 && newIndex == 0 && promotionGuideWin.otype == 'update'){
        		Ext.example.msg('提示', '不能修改活动类型');
        		return false;
        	}else{
        		return true;
        	}
        },
        onFinished : function(event, currentIndex, newIndex){
        	//触发‘预览’Button的事件，更新promotion preview的内容
        	Ext.getCmp('guide_2nd_previewBtn').handler();
        	fnFinishPromotion();
        }
    });
});

/**
 * 初始化优惠活动编辑
 */
function showPromotionGuide(c){
	if(promotionGuideWin == null){

		promotionGuideWin = new Ext.Window({
			closable : true,
			closeAction:'hide',
			resizable : false,
			modal : true,
			width : 1100,
			height : 720,
			bodyStyle : 'backgroundColor:#FFFFFF; border:1px solid #99BBE8;',
			contentEl : 'divActiveInsert',
			listeners : {
	/*				beforehide : function(thiz){
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
				},*/
				show : function(thiz){
					
					if(thiz.otype == 'insert'){
						this.setTitle('添加优惠活动');
						
						//跳到第一步
						$("#wizard").steps("previous");
						
						Ext.getCmp('guide_2nd_title').setValue();
						Ext.getCmp('guide_2nd_couponName').setValue();
						Ext.getCmp('guide_2nd_couponPrice').setValue();
						Ext.getCmp('guide_2nd_couponExpired').setValue(new Date());
						Ext.getCmp('guide_2nd_promotionEditor').setValue();
						Ext.getCmp('guide_2nd_couponExpired').clearInvalid();
						
						Ext.getDom('radioDefaultCoupon').checked = true; 
						Ext.getCmp('radioDefaultCoupon').fireEvent('check', Ext.getCmp('radioDefaultCoupon'), true);
						Ext.getCmp('couponTypeBox').setImg();
						
						Ext.getCmp('secondStepEastBody').body.update('');
						
						promotionGuideWin.image = '';
						
					}else if(thiz.otype == 'update'){
						this.setTitle('修改优惠活动');
						
						//直接跳到第二步
						$("#wizard").steps("next");
						
						Ext.getCmp('guide_2nd_title').setValue(thiz.promotion.title);
						Ext.getCmp('guide_2nd_couponName').setValue(thiz.promotion.coupon.name);
						Ext.getCmp('guide_2nd_couponPrice').setValue(thiz.promotion.coupon.price);
						Ext.getCmp('guide_2nd_couponExpired').setValue(thiz.promotion.coupon.expiredFormat);
						Ext.getCmp('guide_2nd_promotionEditor').setValue(thiz.promotion.body);
						
						Ext.getDom('radioSelfCoupon').checked = true; 
						Ext.getCmp('couponTypeBox').setImg(thiz.promotion.coupon.ossImage ? thiz.promotion.coupon.ossImage.image : 'http://digie-image-real.oss.aliyuncs.com/nophoto.jpg');
						
						updatePromotionCouponPanel(thiz.promotion.pType);
					}
				}
			},
			keys : [{
				 key : Ext.EventObject.ESC,
				 fn : function(){ 
					 promotionGuideWin.hide();
				 },
				 scope : this 
			 }]
		});
	}
	
	promotionGuideWin.otype = c.type;
	if(c.promotion){
		promotionGuideWin.promotion = c.promotion;
		promotionGuideWin.promotionType = c.promotion.pType;
	}
	
	promotionGuideWin.show();

};

function chooseCouponModel(e, h){
	if(Ext.isIE){
		e.getEl().dom.parentNode.style.paddingTop = '43px';
	}else{
		e.getEl().dom.parentNode.style.paddingTop = h?h:'45px';
	}
}

function changeCouponModel(type){
	var couponName = Ext.getCmp('guide_2nd_couponName');
	var price = Ext.getCmp('guide_2nd_couponPrice');
	if(type == 1){
		couponName.setValue('10元优惠劵');
		price.setValue(10);
	}else if(type == 2){
		couponName.setValue('20元优惠劵');
		price.setValue(20);	
	}else if(type == 3){
		couponName.setValue();
		price.setValue();	
	}
//	couponTypeId = type;
	couponName.clearInvalid();
	price.clearInvalid();
	couponName.focus();
}

//1表示无优惠劵纯展示; 2表示无条件领取优惠劵
function updatePromotionCouponPanel(promotionType){
	if(promotionType == 1){
		//纯展示
		$("#promotionPic").attr("title", "上传活动封面图片");
		Ext.getCmp('guide_2nd_couponDetail').hide();
		Ext.getCmp('guide_2nd_CouponOption').hide();
		Ext.getCmp('guide_2nd_promotionEditor').setHeight(440);
		Ext.getCmp('guide_2nd_occupy').show();
		
	}else if(promotionType == 2){
		//优惠券
		$("#promotionPic").attr("title", "优惠券图片");
		Ext.getCmp('guide_2nd_couponDetail').show();
		Ext.getCmp('guide_2nd_CouponOption').show();
		Ext.getCmp('guide_2nd_occupy').show();
		Ext.getCmp('guide_2nd_promotionEditor').setHeight(325);
	}
	Ext.getCmp('guide_2nd_panel').doLayout();
}

function selectPromotionModel(thiz, type){
	$(".active_mould").removeClass('active_mould_click');
	$(thiz).find('input').attr('checked', 'checked');
	$(thiz).addClass('active_mould_click');
	promotionGuideWin.promotionType = type;
	updatePromotionCouponPanel(type);
}

//显示优惠活动的Body和相应的Coupon内容
function loadPromotion(promotionId){
	Ext.Ajax.request({
		url : '../../OperatePromotion.do',
		params : {
			dataSource : 'getPromotion',
			promotionId : promotionId
		},
		success : function(res, opt){
			var jr = Ext.decode(res.responseText);
			if(jr.success){
				//显示优惠活动正文
				Ext.getCmp('promotionPreviewBody').body.update(jr.root[0].entire);
				//显示优惠活动URL
				Ext.getCmp('promotionUrl').setText('http://wx.e-tones.net/wx-term/weixin/order/sales.html?pid=' + promotionId);
				//显示优惠券内容
				if(jr.root[0].coupon){
					Ext.getCmp('promotionCouponPreview').body.update('<div style="text-align:left; margin: 10px 10px 10px 20px;float:left;"><img height="100"  src="' + (jr.root[0].coupon.ossImage?jr.root[0].coupon.ossImage.image:'http://digie-image-real.oss.aliyuncs.com/nophoto.jpg') + '" /></div>'
																+ '<div style="float:left;vertical-align: middle;line-height: 25px;"><br><span style="margin-top: 15px;">' + jr.root[0].coupon.name + '</span><br><span >面额 : ' + jr.root[0].coupon.price + ' 元</span><br><span >到期 : ' + jr.root[0].coupon.expiredFormat + '</span></div>');							
				}else{
					Ext.getCmp('promotionCouponPreview').body.update('<div style="text-align:center; margin: 10px 10px 10px 10px;"><img height="100"  src="../../images/noCouponNow.png" /></div>');
				}												
											
			}
		},
		failure : function(res, opt){
			Ext.ux.showMsg(Ext.decode(res.responseText));
		}
	});
	
	Ext.Ajax.request({
		url : '../../QueryCoupon.do',
		params : {
			dataSource : 'status',
			promotionId : promotionId 
		},
		success : function(res, opt){
			var jr = Ext.decode(res.responseText);
			if(jr.other){
				Ext.getCmp('couponPublished').setText(jr.other.couponPublished + "人参与");
				Ext.getCmp('couponDrawn').setText(jr.other.couponDrawn + "已领取");
				Ext.getCmp('couponUsed').setText(jr.other.couponUsed + "已使用");
			}
		},
		failure : function(res, opt){
			Ext.ux.showMsg(Ext.decode(res.responseText));
		}
	});	
}

//发布活动
function fnPublishPromotion(){
	var node = Ext.ux.getSelNode(promotionTree);
	if (!node || node.attributes.id == -1) {
		Ext.example.msg('提示', '操作失败, 请选择一个活动再进行操作.');
		
	}else{

		var promotionId = node.attributes.id;
		var promotionToPublish = null;
		var publishPromotionWnd = null;
		//活动发布Window
		publishPromotionWnd = new Ext.Window({
			title : '发布优惠活动',
			width : 400,
			closeAction : 'hide',
			xtype : 'panel',
			layout : 'column',
			frame : true,
			modal : true,
			items : [{
				columnWidth : 1,
				style :'margin-top:5px;',
				border : false
			},{
				columnWidth : 0.2,
				xtype : 'label',
				text : '发布对象:'
			},{
				columnWidth : 0.3,
				id : 'comboFilter4PromotionPublish',
				xtype : 'combo',
				readOnly : false,
				forceSelection : true,
				store : new Ext.data.JsonStore({
					root : 'root',
					fields : ['id', 'name']
				}),
				valueField : 'id',
				displayField : 'name',
				typeAhead : true,
				mode : 'local',
				triggerAction : 'all',
				selectOnFocus : true
			},
			{
				columnWidth : 0.2,
				xtype : 'label',
				html : '&nbsp;&nbsp;&nbsp;&nbsp;结束时间:'
			},{
				columnWidth : 0.3,
				id : 'dateField4PromotionPublish',
				xtype : 'datefield',
				fieldLabel : '&nbsp;&nbsp;&nbsp;结束日期',
				format : 'Y-m-d',
				readOnly : false,
				allowBlank : false,
				//minValue : new Date(),
				blankText : '日期不能为空.',
				listeners : {
					invalid : function(thiz){
						thiz.clearInvalid();
					}
				}					
			},{
				columnWidth : 1,
				style :'margin-bottom:5px;',
				border : false
			}
			],
			bbar : ['->',{
				text : '发布',
				id : 'btnPublish',
				iconCls : 'btn_save',
				handler : function(){
					var params = {
						dataSource : 'update',
						id : node.attributes.id 
					};

					var endDate = Ext.getCmp('dateField4PromotionPublish').getValue();
					if(endDate.length == 0){
						Ext.example.msg('提示', '请设置活动结束日期');
						return;
					}else{
						params.endDate = Ext.util.Format.date(endDate, 'Y-m-d 00:00:00');
						params.body = promotionToPublish.body;
						params.entire = buildPromotionHeader(promotionToPublish.title, endDate) + promotionToPublish.body;
					}
					
					var oriented = Ext.getCmp('comboFilter4PromotionPublish').getValue();
					if(oriented == -1){
						//不发布给任何人
						params.oriented = 3;
						
					}else if(oriented == 0){
						//发布面向所有人
						params.oriented = 1;
						
					}else if(oriented > 0){
						//发布面向特定条件的会员
						params.oriented = 2;
						params.condId = oriented;
					}
					
					var publishMask = new Ext.LoadMask(document.body, {
						msg : '正在发布活动...'
					});
					publishMask.show();
					Ext.Ajax.request({
						url : '../../OperatePromotion.do?',
						params : params,
						success : function(res, opt){
							var jr = Ext.decode(res.responseText);
							if(jr.success){
								//关闭窗体
								publishPromotionWnd.destroy();
								
								//刷新活动树
								promotionTree.getRootNode().reload(function(){
									//reload后刷新发布活动的内容
									for(var i in promotionTree.getRootNode().childNodes){
										var statusNode = promotionTree.getRootNode().childNodes[i];
										for(var j in statusNode.childNodes){
											var promotionNode = statusNode.childNodes[j];
											if(promotionNode.id == node.attributes.id){
												promotionNode.select();
												promotionNode.fireEvent('click', promotionNode);
												break;
											}
										}
									}
								});
								publishMask.hide();
								Ext.example.msg('提示', '活动发布成功');
							}else{
								Ext.example.msg('异常', '活动发布失败');
							}
						},
						failure : function(res, opt){
							publishMask.hide();
							Ext.example.msg('异常', '活动发布失败');
						}
					});

				}
			
			}, {
				text : '取消',
				id : 'btnCancelPublish',
				iconCls : 'btn_close',
				handler : function(){
					publishPromotionWnd.destroy();
				}
			}],
			listeners : {
				'show' : function(thiz){
					//获取会员分析条件
					Ext.Ajax.request({
						url : '../../OperateMemberCond.do?',
						params : {
							dataSource : 'getByCond'
						},
						success : function(res, opt){
							var jr = Ext.decode(res.responseText);
							if(jr.success){
								jr.root.unshift({id : 0, name : '全部会员'});
								jr.root.push({id : -1, name : '不发布任何人'}, {id : -2, name : '不更改'});
								Ext.getCmp('comboFilter4PromotionPublish').store.loadData(jr);
								Ext.getCmp('comboFilter4PromotionPublish').setValue(-2);
							}else{
								Ext.example.msg('异常', '会员筛选条件数据加载失败');
							}
						},
						failure : function(res, opt){
							Ext.getCmp('comboFilter4PromotionPublish').store.loadData({root : [{id : -2, name : '不更改'}]});
							Ext.getCmp('comboFilter4PromotionPublish').setValue(-1);
						}
					});
					
					//获取要发布的活动详情
					Ext.Ajax.request({
						url : '../../OperatePromotion.do?',
						params : {
							dataSource : 'getPromotion',
							promotionId : promotionId
						},
						success : function(res, opt){
							var jr = Ext.decode(res.responseText);
							if(jr.success){
								promotionToPublish = jr.root[0];
								Ext.getCmp('dateField4PromotionPublish').setValue(promotionToPublish.promotionEndDate);
								thiz.setTitle(thiz.title + '---' + promotionToPublish.title);
							}else{
								Ext.example.msg('异常', jr.msg);
							}
						},
						failure : function(res, opt){
						}
					});
				}
			},
			keys : [{
				key : Ext.EventObject.ENTER,
				scope : this,
				fn : function(){
					Ext.getCmp("btnPublish").handler();
				}
			}]
		}).show();
	}	
}

function fnDeletePromotion(){
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

function fnUpdatePromotion(){
	var node = Ext.ux.getSelNode(promotionTree);
	if (!node || node.attributes.id == -1) {
		Ext.example.msg('提示', '操作失败, 请选择一个活动再进行操作.');
		return;
	}	
	
	wx.lm.show();
	Ext.Ajax.request({
		url : '../../OperatePromotion.do',
		params : { 
			promotionId : node.attributes.id, 
			dataSource : 'getPromotion' 
		},
		success : function(res, opt){
			wx.lm.hide();
			var jr = Ext.decode(res.responseText);
			if(jr.success){
				Ext.get('divActiveInsert').show();
				showPromotionGuide({ type : 'update', promotion : jr.root[0] });
			}
		},
		failure : function(res, opt){
			wx.lm.hide();
			Ext.ux.showMsg(Ext.decode(res.responseText));
		}
	});
	
}

var memberCountGrid, memberAnalysisBasicGrid;
var sendCouponWin, couponViewBillWin;
var member_searchType = false;

/**
 * 加载参与会员信息
 * @param pid
 */
function loadMemberAnalysis(pid){
	var gs = memberAnalysisBasicGrid.getStore();
	gs.baseParams['pId'] = pid;
	gs.load({
		params : {
			start : 0,
			limit : 15
		}
	});	
}

Ext.onReady(function() {
	
	promotionTree = new Ext.tree.TreePanel({
		title : '活动信息',
		id : 'promotionTree',
		region : 'west',
		width : 250,
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
			expanded : false,
			text : '全部类型',
	        leaf : false,
	        border : true,
	        promotionId : -1
		}),
		tbar : [ 
		    {
				text : '创建活动',
				iconCls : 'btn_add',
				handler : function(e){
					Ext.get('divActiveInsert').show();
					showPromotionGuide({ type : 'insert' });
				}
			},{
				text : '刷新',
				iconCls : 'btn_refresh',
				handler : function(){
					promotionTree.getRootNode().reload();
				}
			},{
				id : '',
				xtype : 'combo',
				readOnly : false,
				forceSelection : true,
				value : -1,
				width : 80,
				store : new Ext.data.SimpleStore({
					fields : ['value', 'text'],
					data : [[-1, '全部活动'], [1, '纯展示'], [2, '优惠劵']]
				}),
				valueField : 'value',
				displayField : 'text',
				typeAhead : true,
				mode : 'local',
				triggerAction : 'all',
				selectOnFocus : true,
				listeners : {
					select : function(combo, record, index){
						var tl = promotionTree.getLoader();
						tl.baseParams['rule'] = record.data.value;
						promotionTree.getRootNode().reload();
					}
				}
			}
		],
		listeners : {
			load : function(thiz){
				var rn = promotionTree.getRootNode().childNodes;
				if(rn.length == 0){
					promotionTree.getRootNode().getUI().hide();
				}else{
/*					for(var i = (rn.length - 1); i >= 0; i--){
						if(typeof rn[i].attributes.expired != 'undefined'){
							rn[i].setText('<font style="color:#808080">' + rn[i].text + '&nbsp;(已过期)</font>');
						}
						if(rn[i].attributes.pType == 2){
							rn[i].setText('<font style="color:#808080">' + rn[i].text + '&nbsp;(欢迎活动)</font>');
						}
					}*/
					promotionTree.getRootNode().getUI().show();
				}
			},
			click : function(e){
	    		if(e.id < 0){
	    			return;
	    		}
				loadPromotion(e.attributes.id);
				loadMemberAnalysis(e.attributes.id);
			}
		}
	});
	
	//进入界面时，打开部门树，并展示第一条优惠活动的内容
	promotionTree.getRootNode().expand(true, true, function(){
		var node = null;
		if(this.childNodes == 0){
			promotionTree.getRootNode().getUI().hide();
		}else{
			node = this.childNodes[0].childNodes[0];
		}
		if(node != null){
			node.select();
			node.fireEvent('click', node);
		}
	});
	
	memberAnalysisBasicGrid = createGridPanel(
		'memberAnalysisBasicGrid',
		'',
		'',
		'',
		'../../QueryCoupon.do',
		[
			[true, false, false, true],
			['会员名称', 'member.name'],
			['手机号码', 'member.mobile', 125],
			['状态', 'statusText', 125]
		],
		['member.name', 'member.mobile','statusText'],
		[['isPaging', true],['dataSource', 'byCondtion']],
		15,
		'',
		new Ext.Toolbar({
			items : ['->',{
				text : '导出',
				iconCls : 'icon_tb_exoprt_excel',
				handler : function(){
					
					var promotion = Ext.ux.getSelNode(promotionTree);
					var url = '../../{0}?dataSource={1}&pId={2}&status={3}';
					url = String.format(
							url, 
							'ExportHistoryStatisticsToExecl.do', 
							'promotionMember', 
							promotion.id,
							Ext.getCmp('cmb_promotionMemberStatus').getValue()
						);
					window.location = url;
				}
			}]
		})
	);	
	memberAnalysisBasicGrid.region = 'center';
	memberAnalysisBasicGrid.loadMask = { msg : '数据加载中，请稍等...' };	
	
	
	var promotionPreviewPanel = new Ext.Panel({
		layout : 'border',
		region : 'center',
		items : [new Ext.Panel({
			region : 'center',
			layout : 'border',
			items : [new Ext.Panel({
				id : 'promotionPreviewBody',
				region : 'center',
				html : '<div style="text-align:center;background-color:#F5F5F5;height:100%"><img src="http://digie-image-real.oss-cn-hangzhou.aliyuncs.com/WxPromotion/noPromotion.png" width="100%" /></div>'			
			}),new Ext.Panel({
				title : '活动URL',
				height : 80,
				region : 'south',
				items : [{
					id : 'promotionUrl',
					xtype : 'label',
					text : 'wwww.baidu.com'
				}]
			})]
		}), new Ext.Panel({
			region : 'east',
			width : 450,
			layout : 'border',
			items : [new Ext.Panel({
				title : '优惠劵展示',
				id : 'promotionCouponPreview',
				region : 'north',
				height : 150,
				html : '<div style="text-align:center; margin: 10px 10px 10px 10px;"><img height="160"  src="http://digie-image-real.oss-cn-hangzhou.aliyuncs.com/WxPromotion/noCoupon.png" /></div>'
			}),new Ext.Panel({
				title : '活动信息汇总',
				id : 'promotionGeneral',
				region : 'center',
				height : 350,
				layout : 'border',
				items : [new Ext.Panel({
					title : '',
					id : '',
					region : 'north',
					height : 70,
					frame :true,
					layout : 'column',
					defaults :{
						style:'font-size:15px;margin-top:5px;',
						columnWidth : 0.33
					},
					items : [{
						id : 'couponPublished',
						xtype : 'label',
						text : '5人参与'						
					},{
						id : 'couponDrawn',
						xtype : 'label',
						text : '2人领取优惠券'						
					},{
						id : 'couponUsed',
						xtype : 'label',
						text : '2张优惠劵已使用'						
					},{
						columnWidth : 1,
						xtype : 'panel',
						layout : 'form',
						labelWidth : 40,
						items : [{
							xtype : 'combo',
							id : 'cmb_promotionMemberStatus',
							fieldLabel : '状态',
							readOnly : false,
							forceSelection : true,
							width : 100,
							store : new Ext.data.SimpleStore({
								fields : ['value', 'text'],
								data : [['', '全部'], [1, '已创建'], [3, '已领取'], [4, '已使用']]
							}),
							valueField : 'value',
							displayField : 'text',
							typeAhead : true,
							mode : 'local',
							triggerAction : 'all',
							selectOnFocus : true,
							listeners : {
								select : function(combo, record, index){
									var gs = memberAnalysisBasicGrid.getStore();
									gs.baseParams['status'] = record.data.value;
									gs.load({
										params : {
											start : 0,
											limit : 15
										}
									});	
								}
							}
						}]
					}]
				}),new Ext.Panel({
					title : '参与会员信息',
					id : '',
					region : 'center',
					layout : 'fit',
					height : 150,
					items : [memberAnalysisBasicGrid]
				})]
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
	showFloatOption({treeId : 'promotionTree', option : [{name : '发布', fn : "fnPublishPromotion()"},
	                                                     {name : '修改', fn : "fnUpdatePromotion()"}, 
	                                                     {name : '删除', fn : "fnDeletePromotion()"}]});
	

	
	
	
	
//-----------------------------优惠活动
	
	
	var coupon_uploadMask = new Ext.LoadMask(document.body, {
		msg : '正在上传图片...'
	});
	couponPicBox = new Ext.BoxComponent({
		id : 'promotionPic',
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
	 	   				promotionGuideWin.image = ossImage.image;
	 	   				promotionGuideWin.ossId = ossImage.imageId;	   				
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
		img : couponPicBox,
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
	
	    
	
	var promotionEditor = new Ext.form.HtmlEditor({
		id : 'guide_2nd_promotionEditor',
		hideLabel : true,
		width : 510,
		//value : '<div style="margin: 10px; padding: 0px; font-family: Simsun; line-height: 22px; color: rgb(135, 206, 250); font-weight: bold;"><font size="5"><span class="15" style="line-height: 18.75pt; text-indent: 0pt; font-family: 宋体; color: rgb(62, 62, 62); letter-spacing: 0pt;">一、会员充值赠</span><span class="15" style="line-height: 18.75pt; text-indent: 0pt; font-family: 宋体; color: rgb(112, 48, 160); letter-spacing: 0pt;">送</span><span class="15" style="line-height: 18.75pt; text-indent: 0pt; font-family: 宋体; color: rgb(62, 62, 62); letter-spacing: 0pt;">消费金额</span></font></div><div style="margin: 10px; padding: 0px; color: rgb(34, 34, 34); font-family: Simsun; line-height: 22px; word-wrap: break-word;"><p class="p" style="margin: 0pt; padding: 0pt; text-indent: 0pt; line-height: 18.75pt; background-color: rgb(255, 255, 255);"><font size="5"><span class="15" style="font-family: 宋体; color: rgb(62, 62, 62); letter-spacing: 0pt; font-weight: bold;">1.充值2000元</span><span class="15" style="font-family: 宋体; color: rgb(112, 48, 160); letter-spacing: 0pt; font-weight: bold;">送</span><span class="15" style="font-family: 宋体; color: rgb(62, 62, 62); letter-spacing: 0pt; font-weight: bold;">100元消费额</span><span style="font-family: 宋体; color: rgb(62, 62, 62); letter-spacing: 0pt;"><o:p></o:p></span></font></p><p class="p" style="margin: 0pt; padding: 0pt; text-indent: 0pt; line-height: 18.75pt; background-color: rgb(255, 255, 255);"><font size="5"><span class="15" style="font-family: 宋体; color: rgb(62, 62, 62); letter-spacing: 0pt; font-weight: bold;">2.</span><span class="15" style="font-family: 宋体; color: rgb(112, 48, 160); letter-spacing: 0pt; font-weight: bold;">再送</span><span class="15" style="font-family: 宋体; color: rgb(62, 62, 62); letter-spacing: 0pt; font-weight: bold;">进口红酒一支</span><span style="font-family: 宋体; color: rgb(62, 62, 62); letter-spacing: 0pt;"><o:p></o:p></span></font></p><p class="p" style="margin: 0pt; padding: 0pt; text-indent: 0pt; line-height: 18.75pt; background-color: rgb(255, 255, 255);"><span class="15" style="font-family: 宋体; color: rgb(62, 62, 62); letter-spacing: 0pt; font-weight: bold;"><font size="5">（每人每月限送一次）</font></span><span style="font-size: 18pt; font-family: 宋体; color: rgb(62, 62, 62); letter-spacing: 0pt;"><o:p></o:p></span></p><p class="MsoNormal" style="font-size: 16px; margin: 0px; padding: 0px;"><img data-s="300,640" data-type="jpeg" data-src="http://mmbiz.qpic.cn/mmbiz/fLiaKGIbUibdURya5Y1NpicnZv2iaJ9tTCgKPrvcJMc3yGTiaRN6ibiaFfUvWgibb3fRPKeD8xVllYgGeUib8ogoEb2WTFw/0?wxfmt=jpeg" data-ratio="0.6067193675889329" data-w="" src="http://mmbiz.qpic.cn/mmbiz/fLiaKGIbUibdURya5Y1NpicnZv2iaJ9tTCgKPrvcJMc3yGTiaRN6ibiaFfUvWgibb3fRPKeD8xVllYgGeUib8ogoEb2WTFw/640?wxfmt=jpeg&amp;tp=webp&amp;wxfrom=5" style="border: 0px; vertical-align: middle; margin: 0px; padding: 0px; max-width: 100%; color: rgb(62, 62, 62); font-family: \'Helvetica Neue\', Helvetica, \'Hiragino Sans GB\', \'Microsoft YaHei\', Arial, sans-serif; line-height: 25px; white-space: pre-wrap; background-color: rgb(255, 255, 255); height: auto !important; box-sizing: border-box !important; word-wrap: break-word !important; width: auto !important; visibility: visible !important;"></p><p class="p" style="margin: 0pt; padding: 0pt; text-indent: 0pt; line-height: 18.75pt; background-color: rgb(255, 255, 255);"><font size="5"><span class="15" style="font-family: 宋体; color: rgb(62, 62, 62); letter-spacing: 0pt; font-weight: bold;">二、会员充值</span><span class="15" style="font-family: 宋体; color: rgb(112, 48, 160); letter-spacing: 0pt; font-weight: bold;">送</span><span class="15" style="font-family: 宋体; color: rgb(62, 62, 62); letter-spacing: 0pt; font-weight: bold;">积分</span><span style="font-family: 宋体; color: rgb(62, 62, 62); letter-spacing: 0pt;"><o:p></o:p></span></font></p><p class="p" style="margin: 0pt; padding: 0pt; text-indent: 0pt; line-height: 18.75pt; background-color: rgb(255, 255, 255);"><font size="5"><span class="15" style="font-family: 宋体; color: rgb(62, 62, 62); letter-spacing: 0pt; font-weight: bold;">1.会员充值消费送积分（1元积1分）</span><span style="font-family: 宋体; color: rgb(62, 62, 62); letter-spacing: 0pt;"><o:p></o:p></span></font></p><p class="p" style="margin: 0pt; padding: 0pt; text-indent: 0pt; line-height: 18.75pt; background-color: rgb(255, 255, 255);"><span class="15" style="font-family: 宋体; color: rgb(62, 62, 62); letter-spacing: 0pt; font-weight: bold;"><font size="5">2.积分可以换菜式或礼品</font></span><span style="font-size: 18pt; font-family: 宋体; color: rgb(62, 62, 62); letter-spacing: 0pt;"><o:p></o:p></span></p><br><span style="height: auto !important; width: auto !important;"><img data-s="300,640" data-type="jpeg" data-src="http://mmbiz.qpic.cn/mmbiz/fLiaKGIbUibdWAC6mbFibFosPTdeltQIiavtSe3Tib7ytcBElwpRUxibArbcSjGG1TdeRQODz5BjHrCVQiaD9rpZeibUIQ/0?wx_fmt=jpeg" data-ratio="1.075098814229249" data-w="" src="http://mmbiz.qpic.cn/mmbiz/fLiaKGIbUibdWAC6mbFibFosPTdeltQIiavtSe3Tib7ytcBElwpRUxibArbcSjGG1TdeRQODz5BjHrCVQiaD9rpZeibUIQ/640?wx_fmt=jpeg&amp;tp=webp&amp;wxfrom=5" style="border: 0px; vertical-align: middle; margin: 0px; padding: 0px; max-width: 100%; color: rgb(62, 62, 62); font-family: \'Helvetica Neue\', Helvetica, \'Hiragino Sans GB\', \'Microsoft YaHei\', Arial, sans-serif; line-height: 25px; white-space: pre-wrap; background-color: rgb(255, 255, 255); height: auto !important; box-sizing: border-box !important; word-wrap: break-word !important; width: auto !important; visibility: visible !important;"></span><br style="margin: 0px; padding: 0px; max-width: 100%; color: rgb(62, 62, 62); font-family: \'Helvetica Neue\', Helvetica, \'Hiragino Sans GB\', \'Microsoft YaHei\', Arial, sans-serif; line-height: 25px; white-space: pre-wrap; background-color: rgb(255, 255, 255); box-sizing: border-box !important; word-wrap: break-word !important;"><span style="height: auto !important; width: auto !important;"><img data-s="300,640" data-type="jpeg" data-src="http://mmbiz.qpic.cn/mmbiz/fLiaKGIbUibdWAC6mbFibFosPTdeltQIiavtW2Wdn44F4f4SsjEsQuYdbibTGibSVYGbGInGydu8F5HuKGX7wIXtwsXw/0?wx_fmt=jpeg" data-ratio="0.6561264822134387" data-w="" src="http://mmbiz.qpic.cn/mmbiz/fLiaKGIbUibdWAC6mbFibFosPTdeltQIiavtW2Wdn44F4f4SsjEsQuYdbibTGibSVYGbGInGydu8F5HuKGX7wIXtwsXw/640?wx_fmt=jpeg&amp;tp=webp&amp;wxfrom=5" style="border: 0px; vertical-align: middle; margin: 0px; padding: 0px; max-width: 100%; color: rgb(62, 62, 62); font-family: \'Helvetica Neue\', Helvetica, \'Hiragino Sans GB\', \'Microsoft YaHei\', Arial, sans-serif; line-height: 25px; white-space: pre-wrap; background-color: rgb(255, 255, 255); height: auto !important; box-sizing: border-box !important; word-wrap: break-word !important; width: auto !important; visibility: visible !important;"></span><br style="margin: 0px; padding: 0px; max-width: 100%; color: rgb(62, 62, 62); font-family: \'Helvetica Neue\', Helvetica, \'Hiragino Sans GB\', \'Microsoft YaHei\', Arial, sans-serif; line-height: 25px; white-space: pre-wrap; background-color: rgb(255, 255, 255); box-sizing: border-box !important; word-wrap: break-word !important;"><span style="height: auto !important; width: auto !important;"><img data-s="300,640" data-type="jpeg" data-src="http://mmbiz.qpic.cn/mmbiz/fLiaKGIbUibdWAC6mbFibFosPTdeltQIiavtMvRJlA5g2PeQnleg4zXcQYzdSFAVbR9BlveqjavvBVVkiaicUeGKKYjQ/0?wx_fmt=jpeg" data-ratio="0.616600790513834" data-w="" src="http://mmbiz.qpic.cn/mmbiz/fLiaKGIbUibdWAC6mbFibFosPTdeltQIiavtMvRJlA5g2PeQnleg4zXcQYzdSFAVbR9BlveqjavvBVVkiaicUeGKKYjQ/640?wx_fmt=jpeg&amp;tp=webp&amp;wxfrom=5" style="border: 0px; vertical-align: middle; margin: 0px; padding: 0px; max-width: 100%; color: rgb(62, 62, 62); font-family: \'Helvetica Neue\', Helvetica, \'Hiragino Sans GB\', \'Microsoft YaHei\', Arial, sans-serif; line-height: 25px; white-space: pre-wrap; background-color: rgb(255, 255, 255); height: auto !important; box-sizing: border-box !important; word-wrap: break-word !important; width: auto !important; visibility: visible !important;"></span><br style="margin: 0px; padding: 0px; max-width: 100%; color: rgb(62, 62, 62); font-family: \'Helvetica Neue\', Helvetica, \'Hiragino Sans GB\', \'Microsoft YaHei\', Arial, sans-serif; line-height: 25px; white-space: pre-wrap; background-color: rgb(255, 255, 255); box-sizing: border-box !important; word-wrap: break-word !important;"><span style="height: auto !important; width: auto !important;"><img data-s="300,640" data-type="jpeg" data-src="http://mmbiz.qpic.cn/mmbiz/fLiaKGIbUibdWAC6mbFibFosPTdeltQIiavtDo2cKt17B2Ty9a7dTTGmnet6mzfZ4jaxJwSP4rD2bRv9ETiarSd1MzA/0?wx_fmt=jpeg" data-ratio="1.391304347826087" data-w="" src="http://mmbiz.qpic.cn/mmbiz/fLiaKGIbUibdWAC6mbFibFosPTdeltQIiavtDo2cKt17B2Ty9a7dTTGmnet6mzfZ4jaxJwSP4rD2bRv9ETiarSd1MzA/640?wx_fmt=jpeg&amp;tp=webp&amp;wxfrom=5" style="border: 0px; vertical-align: middle; margin: 0px; padding: 0px; max-width: 100%; color: rgb(62, 62, 62); font-family: \'Helvetica Neue\', Helvetica, \'Hiragino Sans GB\', \'Microsoft YaHei\', Arial, sans-serif; line-height: 25px; white-space: pre-wrap; background-color: rgb(255, 255, 255); height: auto !important; box-sizing: border-box !important; word-wrap: break-word !important; width: auto !important; visibility: visible !important;"></span><br><br><p class="p" style="margin: 0pt; padding: 0pt; text-indent: 0pt; line-height: 18.75pt; background-color: rgb(255, 255, 255);"><font size="5"><span class="15" style="font-family: 宋体; color: rgb(62, 62, 62); letter-spacing: 0pt; font-weight: bold;">三、会员专享出品优惠</span><span style="font-family: 宋体; color: rgb(62, 62, 62); letter-spacing: 0pt;"><o:p></o:p></span></font></p><p class="MsoNormal" style="margin: 0px; padding: 0px;"><font size="5"><span class="15" style="font-family: 宋体; color: rgb(62, 62, 62); letter-spacing: 0pt; font-weight: bold; background-color: rgb(255, 255, 255);">1.会员专享：点心出品</span><span class="15" style="font-family: 宋体; color: rgb(112, 48, 160); letter-spacing: 0pt; font-weight: bold; background-color: rgb(255, 255, 255);">9.5折</span></font></p><span style="height: auto !important; width: auto !important;"><font size="5"><img data-s="300,640" data-type="jpeg" data-src="http://mmbiz.qpic.cn/mmbiz/fLiaKGIbUibdWAC6mbFibFosPTdeltQIiavt129Wib5Xo4Aj1AQbaQzpOUOlIdxyknFF4EYiaX73NKicB8St0nRvv6ZPg/0?wx_fmt=jpeg" data-ratio="0.6225296442687747" data-w="" src="http://mmbiz.qpic.cn/mmbiz/fLiaKGIbUibdWAC6mbFibFosPTdeltQIiavt129Wib5Xo4Aj1AQbaQzpOUOlIdxyknFF4EYiaX73NKicB8St0nRvv6ZPg/640?wx_fmt=jpeg&amp;tp=webp&amp;wxfrom=5" style="border: 0px; vertical-align: middle; margin: 0px; padding: 0px; max-width: 100%; color: rgb(62, 62, 62); font-family: \'Helvetica Neue\', Helvetica, \'Hiragino Sans GB\', \'Microsoft YaHei\', Arial, sans-serif; line-height: 25px; white-space: pre-wrap; background-color: rgb(255, 255, 255); height: auto !important; box-sizing: border-box !important; word-wrap: break-word !important; width: auto !important; visibility: visible !important;"></font></span><br style="margin: 0px; padding: 0px; max-width: 100%; color: rgb(62, 62, 62); font-family: \'Helvetica Neue\', Helvetica, \'Hiragino Sans GB\', \'Microsoft YaHei\', Arial, sans-serif; line-height: 25px; white-space: pre-wrap; background-color: rgb(255, 255, 255); box-sizing: border-box !important; word-wrap: break-word !important;"><span style="height: auto !important; width: auto !important;"><img data-s="300,640" data-type="png" data-src="http://mmbiz.qpic.cn/mmbiz/fLiaKGIbUibdWAC6mbFibFosPTdeltQIiavtNkxvsdby99gU3DG2sq4qeQ5fQrlTH935JRyzwMyATSic0QFZCuN2shQ/0?wx_fmt=png" data-ratio="0.541501976284585" data-w="" src="http://mmbiz.qpic.cn/mmbiz/fLiaKGIbUibdWAC6mbFibFosPTdeltQIiavtNkxvsdby99gU3DG2sq4qeQ5fQrlTH935JRyzwMyATSic0QFZCuN2shQ/640?wx_fmt=png&amp;tp=webp&amp;wxfrom=5" style="border: 0px; vertical-align: middle; margin: 0px; padding: 0px; max-width: 100%; color: rgb(62, 62, 62); font-family: \'Helvetica Neue\', Helvetica, \'Hiragino Sans GB\', \'Microsoft YaHei\', Arial, sans-serif; line-height: 25px; white-space: pre-wrap; background-color: rgb(255, 255, 255); height: auto !important; box-sizing: border-box !important; word-wrap: break-word !important; width: auto !important; visibility: visible !important;"></span><br><br><p class="p" style="margin: 0pt; padding: 0pt; text-indent: 0pt; line-height: 18.75pt; background-color: rgb(255, 255, 255);"><font size="5"><span class="15" style="font-family: 宋体; color: rgb(62, 62, 62); letter-spacing: 0pt; font-weight: bold;">2.会员专享：菜式出品</span><span class="15" style="font-family: 宋体; color: rgb(112, 48, 160); letter-spacing: 0pt; font-weight: bold;">9.5折</span></font><span style="font-size: 18pt; font-family: 宋体; color: rgb(62, 62, 62); letter-spacing: 0pt;"><o:p></o:p></span></p><br><p style="font-size: 16px; margin: 0px; padding: 0px; max-width: 100%; clear: both; min-height: 1em; white-space: pre-wrap; color: rgb(62, 62, 62); font-family: \'Helvetica Neue\', Helvetica, \'Hiragino Sans GB\', \'Microsoft YaHei\', Arial, sans-serif; line-height: 25px; background-color: rgb(255, 255, 255); box-sizing: border-box !important; word-wrap: break-word !important;"><img data-s="300,640" data-type="png" data-src="http://mmbiz.qpic.cn/mmbiz/fLiaKGIbUibdWAC6mbFibFosPTdeltQIiavtPTafOpPpbOKNcDLEq9CdbYfaGjWupHltkQP3hOZY6eceRmicAGhnInA/0?wx_fmt=png" data-ratio="0.6737967914438503" data-w="374" src="http://mmbiz.qpic.cn/mmbiz/fLiaKGIbUibdWAC6mbFibFosPTdeltQIiavtPTafOpPpbOKNcDLEq9CdbYfaGjWupHltkQP3hOZY6eceRmicAGhnInA/640?wx_fmt=png&amp;tp=webp&amp;wxfrom=5" style="border: 0px; vertical-align: middle; margin: 0px; padding: 0px; max-width: 100%; height: auto !important; box-sizing: border-box !important; word-wrap: break-word !important; width: auto !important; visibility: visible !important;"></p><p style="font-size: 16px; margin: 0px; padding: 0px; max-width: 100%; clear: both; min-height: 1em; white-space: pre-wrap; color: rgb(62, 62, 62); font-family: \'Helvetica Neue\', Helvetica, \'Hiragino Sans GB\', \'Microsoft YaHei\', Arial, sans-serif; line-height: 25px; background-color: rgb(255, 255, 255); box-sizing: border-box !important; word-wrap: break-word !important;"><img data-s="300,640" data-type="png" data-src="http://mmbiz.qpic.cn/mmbiz/fLiaKGIbUibdWAC6mbFibFosPTdeltQIiavt01oxpuyAHAJBLNia8Z6joH35TIdZHAWHBrFxgDthp6FuE5ia0tXYKa0w/0?wx_fmt=png" data-ratio="0.614314115308151" data-w="503" src="http://mmbiz.qpic.cn/mmbiz/fLiaKGIbUibdWAC6mbFibFosPTdeltQIiavt01oxpuyAHAJBLNia8Z6joH35TIdZHAWHBrFxgDthp6FuE5ia0tXYKa0w/640?wx_fmt=png&amp;tp=webp&amp;wxfrom=5" style="border: 0px; vertical-align: middle; margin: 0px; padding: 0px; max-width: 100%; height: auto !important; box-sizing: border-box !important; word-wrap: break-word !important; width: auto !important; visibility: visible !important;"></p><br><p class="p" style="margin: 0pt; padding: 0pt; text-indent: 0pt; line-height: 18.75pt; background-color: rgb(255, 255, 255);"><font size="5"><span class="15" style="font-family: 宋体; color: rgb(62, 62, 62); letter-spacing: 0pt; font-weight: bold;">四、会员生日到店消费</span><span class="15" style="font-family: 宋体; color: rgb(112, 48, 160); letter-spacing: 0pt; font-weight: bold;">送</span><span class="15" style="font-family: 宋体; color: rgb(62, 62, 62); letter-spacing: 0pt; font-weight: bold;">礼包</span><span style="font-family: 宋体; color: rgb(62, 62, 62); letter-spacing: 0pt;"><o:p></o:p></span></font></p><p class="p" style="margin: 0pt; padding: 0pt; text-indent: 0pt; line-height: 18.75pt; background-color: rgb(255, 255, 255);"><font size="5"><span class="15" style="font-family: 宋体; color: rgb(62, 62, 62); letter-spacing: 0pt; font-weight: bold;">1.到店消费满500元</span><span class="15" style="font-family: 宋体; color: rgb(112, 48, 160); letter-spacing: 0pt; font-weight: bold;">送</span><span class="15" style="font-family: 宋体; color: rgb(62, 62, 62); letter-spacing: 0pt; font-weight: bold;">:红酒一瓶价值128元;</span><span style="font-family: 宋体; color: rgb(62, 62, 62); letter-spacing: 0pt;"><o:p></o:p></span></font></p><p class="p" style="margin: 0pt; padding: 0pt; text-indent: 0pt; line-height: 18.75pt; background-color: rgb(255, 255, 255);"><span style="font-family: 宋体; color: rgb(62, 62, 62); letter-spacing: 0pt;"><font size="5">&nbsp;</font></span></p><p class="p" style="margin: 0pt; padding: 0pt; text-indent: 0pt; line-height: 18.75pt; background-color: rgb(255, 255, 255);"><font size="5"><span class="15" style="font-family: 宋体; color: rgb(62, 62, 62); letter-spacing: 0pt; font-weight: bold;">2.消费满1000元</span><span class="15" style="font-family: 宋体; color: rgb(112, 48, 160); letter-spacing: 0pt; font-weight: bold;">送</span><span class="15" style="font-family: 宋体; color: rgb(62, 62, 62); letter-spacing: 0pt; font-weight: bold;">：红酒一瓶价值188元</span><span style="font-family: 宋体; color: rgb(62, 62, 62); letter-spacing: 0pt;"><o:p></o:p></span></font></p><p class="p" style="margin: 0pt; padding: 0pt; text-indent: 0pt; line-height: 18.75pt; background-color: rgb(255, 255, 255);"><font size="5"><span class="15" style="font-family: 宋体; color: rgb(62, 62, 62); letter-spacing: 0pt; font-weight: bold;">生日蛋糕一个。</span><span style="font-family: 宋体; color: rgb(62, 62, 62); letter-spacing: 0pt;"><o:p></o:p></span></font></p><p class="p" style="margin: 0pt; padding: 0pt; text-indent: 0pt; line-height: 18.75pt; background-color: rgb(255, 255, 255);"><span style="font-family: 宋体; color: rgb(62, 62, 62); letter-spacing: 0pt;"><font size="5">&nbsp;</font></span></p><p class="p" style="margin: 0pt; padding: 0pt; text-indent: 0pt; line-height: 18.75pt; background-color: rgb(255, 255, 255);"><font size="5"><span class="15" style="font-family: 宋体; color: rgb(62, 62, 62); letter-spacing: 0pt; font-weight: bold;">3.要求:提前一天订餐</span><span style="font-family: 宋体; color: rgb(62, 62, 62); letter-spacing: 0pt;"><o:p></o:p></span></font></p><br><span style="height: auto !important; width: auto !important;"><img data-s="300,640" data-type="jpeg" data-src="http://mmbiz.qpic.cn/mmbiz/fLiaKGIbUibdWAC6mbFibFosPTdeltQIiavtL2Z119LtzdHN5JSSnQR5q4VAxDvF0DzPLBtGIo932KfzTOI4OIIrHg/0?wx_fmt=jpeg" data-ratio="0.8043478260869565" data-w="" src="http://mmbiz.qpic.cn/mmbiz/fLiaKGIbUibdWAC6mbFibFosPTdeltQIiavtL2Z119LtzdHN5JSSnQR5q4VAxDvF0DzPLBtGIo932KfzTOI4OIIrHg/640?wx_fmt=jpeg&amp;tp=webp&amp;wxfrom=5" style="border: 0px; vertical-align: middle; margin: 0px; padding: 0px; max-width: 100%; color: rgb(62, 62, 62); font-family: \'Helvetica Neue\', Helvetica, \'Hiragino Sans GB\', \'Microsoft YaHei\', Arial, sans-serif; line-height: 25px; white-space: pre-wrap; background-color: rgb(255, 255, 255); height: auto !important; box-sizing: border-box !important; word-wrap: break-word !important; width: auto !important; visibility: visible !important;"></span><br><br><p class="p" style="margin: 0pt; padding: 0pt; text-indent: 0pt; line-height: 18.75pt; background-color: rgb(255, 255, 255);"><font size="5"><span class="15" style="font-family: 宋体; color: rgb(62, 62, 62); letter-spacing: 0pt; font-weight: bold;">五、会员专享茶位优惠</span><span style="font-family: 宋体; color: rgb(62, 62, 62); letter-spacing: 0pt;"><o:p></o:p></span></font></p><p class="p" style="margin: 0pt; padding: 0pt; text-indent: 0pt; line-height: 18.75pt; background-color: rgb(255, 255, 255);"><font size="5"><span class="15" style="font-family: 宋体; color: rgb(62, 62, 62); letter-spacing: 0pt; font-weight: bold;">1.会员大厅茗茶免费，非会员2元/位</span><span style="font-family: 宋体; color: rgb(62, 62, 62); letter-spacing: 0pt;"><o:p></o:p></span></font></p><p class="p" style="margin: 0pt; padding: 0pt; text-indent: 0pt; line-height: 18.75pt; background-color: rgb(255, 255, 255);"><font size="5"><span class="15" style="font-family: 宋体; color: rgb(62, 62, 62); letter-spacing: 0pt; font-weight: bold;">2.功夫茶会员4元/位，非会员6元/位</span><span style="font-family: 宋体; color: rgb(62, 62, 62); letter-spacing: 0pt;"><o:p></o:p></span></font></p><br><p style="margin: 0px; padding: 0px; max-width: 100%; clear: both; min-height: 1em; white-space: pre-wrap; color: rgb(62, 62, 62); font-family: \'Helvetica Neue\', Helvetica, \'Hiragino Sans GB\', \'Microsoft YaHei\', Arial, sans-serif; line-height: 25px; background-color: rgb(255, 255, 255); box-sizing: border-box !important; word-wrap: break-word !important;"><span style="height: auto !important; width: auto !important;"><img data-s="300,640" data-type="png" data-src="http://mmbiz.qpic.cn/mmbiz/fLiaKGIbUibdWAC6mbFibFosPTdeltQIiavtEfJicPAtHiay0eU1mNUJf9bNLDdheiauSIZnzPzALjx6cmhTKasVibPIZA/0?wx_fmt=png" data-ratio="0.7312252964426877" data-w="" src="http://mmbiz.qpic.cn/mmbiz/fLiaKGIbUibdWAC6mbFibFosPTdeltQIiavtEfJicPAtHiay0eU1mNUJf9bNLDdheiauSIZnzPzALjx6cmhTKasVibPIZA/640?wx_fmt=png&amp;tp=webp&amp;wxfrom=5" style="border: 0px; vertical-align: middle; margin: 0px; padding: 0px; max-width: 100%; height: auto !important; box-sizing: border-box !important; word-wrap: break-word !important; width: auto !important; visibility: visible !important;"></span><br style="margin: 0px; padding: 0px; max-width: 100%; box-sizing: border-box !important; word-wrap: break-word !important;"><span style="margin: 0px; padding: 0px; max-width: 100%; box-sizing: border-box !important; word-wrap: break-word !important;"><span style="margin: 0px; padding: 0px; max-width: 100%; box-sizing: border-box !important; word-wrap: break-word !important;"><font size="5"><br>六、会员专享细则说明</font></span></span></p><p style="margin: 0px; padding: 0px; max-width: 100%; clear: both; min-height: 1em; white-space: pre-wrap; color: rgb(62, 62, 62); font-family: \'Helvetica Neue\', Helvetica, \'Hiragino Sans GB\', \'Microsoft YaHei\', Arial, sans-serif; line-height: 25px; background-color: rgb(255, 255, 255); box-sizing: border-box !important; word-wrap: break-word !important;"><span style="margin: 0px; padding: 0px; max-width: 100%; box-sizing: border-box !important; word-wrap: break-word !important;"><span style="margin: 0px; padding: 0px; max-width: 100%; box-sizing: border-box !important; word-wrap: break-word !important;"><font size="5">1.要求充值消费，充值以100元为单位</font></span></span></p><p style="margin: 0px; padding: 0px; max-width: 100%; clear: both; min-height: 1em; white-space: pre-wrap; color: rgb(62, 62, 62); font-family: \'Helvetica Neue\', Helvetica, \'Hiragino Sans GB\', \'Microsoft YaHei\', Arial, sans-serif; line-height: 25px; background-color: rgb(255, 255, 255); box-sizing: border-box !important; word-wrap: break-word !important;"><font size="5"><span style="margin: 0px; padding: 0px; max-width: 100%; box-sizing: border-box !important; word-wrap: break-word !important;"><span style="margin: 0px; padding: 0px; max-width: 100%; box-sizing: border-box !important; word-wrap: break-word !important;">2.卡内的消费金额，可用于易德福（明苑）</span></span></font><span style="font-size: x-large;"> 酒家的消费结算。(即充即用) </span></p><p style="margin: 0px; padding: 0px; max-width: 100%; clear: both; min-height: 1em; white-space: pre-wrap; color: rgb(62, 62, 62); font-family: \'Helvetica Neue\', Helvetica, \'Hiragino Sans GB\', \'Microsoft YaHei\', Arial, sans-serif; line-height: 25px; background-color: rgb(255, 255, 255); box-sizing: border-box !important; word-wrap: break-word !important;"><span style="margin: 0px; padding: 0px; max-width: 100%; box-sizing: border-box !important; word-wrap: break-word !important;"><span style="margin: 0px; padding: 0px; max-width: 100%; box-sizing: border-box !important; word-wrap: break-word !important;"><font size="5">3.如有增加或更改以易德福(明苑) 酒家最新   公布为准。</font></span></span></p><br><b><font size="5">订餐（咨询）电话：0760-88777888<br></font></b><br><br><br><br><br><br><br><br><br><br><br><br><br><br><br><br><br><br><br><br><br><br><br><br><br><br><br><br><br><br><br><br><br><br><br><br><br><br><br><br><br><br><br><br><br><br><br><br><br><br><br></div>',
		enableAlignments: false,
        enableColors: true,
        enableFont: false,
        enableFontSize: true,
        enableFormat: true,
        enableLinks: false,
        enableLists: false,
        enableSourceEdit: true,
        plugins : [new Ext.ux.plugins.HEInsertImage({
        	url : '../../OperateImage.do?dataSource=upload&ossType=1'
        })]
	});
	var btnPreview = new Ext.Button({
		id : 'guide_2nd_previewBtn',
		text : '预览',
		listeners : {
			render : function(thiz){
				thiz.getEl().setWidth(100, true);
			}
		},
		handler : function(){
			secendStepCenter.body.update(buildPromotionHeader(Ext.getCmp('guide_2nd_title').getValue()) + promotionEditor.getValue());
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
			promotionEditor.setValue();
			secendStepCenter.body.update(promotionEditor.getValue());
		}
	});
	var secendStepWest = new Ext.form.FormPanel({
		width : 510,
		height : 350,
		title : '编辑活动内容:',
		items : [promotionEditor],
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
		id : 'guide_2nd_panel',
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
					id : 'guide_2nd_occupy',
					xtype : 'label',
					style : 'color:#DFE8F6',
					html : '单次消费积分满/累计积分满-有优惠劵'
				}]
			},{
				columnWidth : .3,
				items : [{
					id : 'guide_2nd_title',
					xtype : 'textfield',
					width : 200,
					fieldLabel : '&nbsp;&nbsp;&nbsp;活动标题',
					style : 'overflow: hidden;',
					allowBlank : false,
					listeners : {
						blur : function(){
							Ext.getCmp('guide_2nd_previewBtn').handler();
						}
					}
				}]
			} 
//			,{
//				items : [{
//					id : 'active_beginDate',
//					xtype : 'datefield',
//					width : 90,
//					fieldLabel : '&nbsp;&nbsp;&nbsp;活动日期',
//					format : 'Y-m-d',
//					readOnly : false,
//					allowBlank : false,
//					//minValue : new Date(),
//					blankText : '日期不能为空.',
//					listeners : {
//						invalid : function(thiz){
//							thiz.clearInvalid();
//						},
//						blur : function(){
//							Ext.getCmp('guide_2nd_previewBtn').handler();
//						}						
//					}					
//				}]				
//			}, {
//				items : [{
//					xtype : 'label',
//					style : 'margin-top:4px;',
//					html : '至&nbsp;&nbsp;'
//				}]
//			}, {
//				layout : 'fit',
//				items : [{
//					id : 'active_endDate',
//					xtype : 'datefield',
//					width : 90,
//					fieldLabel : '',
//					format : 'Y-m-d',
//					readOnly : false,
//					allowBlank : false,
//					//minValue : new Date(),
//					blankText : '日期不能为空.',
//					listeners : {
//						blur : function(){
//							Ext.getCmp('guide_2nd_previewBtn').handler();
//						}
//					}				
//				}]				
//			}
			]
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
					id : 'guide_2nd_CouponOption',
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
								boxLabel : '10元券&nbsp;&nbsp;',
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
								boxLabel : '20元券&nbsp;&nbsp;',
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
				id : 'guide_2nd_couponDetail',
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
							id : 'guide_2nd_couponName',
							xtype : 'textfield',
							fieldLabel : '优惠劵名称',
							value : '10元优惠劵',
							allowBlank : false
						}]
					},{
						items : [{
							id : 'guide_2nd_couponPrice',
							xtype : 'textfield',
							value : 10,
							fieldLabel : '&nbsp;&nbsp;&nbsp;面额',
							allowBlank : false
						}]
					}, {
						items : [{
							id : 'guide_2nd_couponExpired',
							xtype : 'datefield',
							width : 130,
							fieldLabel : '&nbsp;&nbsp;&nbsp;有效期至',
							format : 'Y-m-d',
							readOnly : false,
							allowBlank : false,
							minValue : new Date(),
							value : new Date(),
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
					items : [couponPicBox, form,{
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
	
	//steps.js与Ext混用时的样式修正	
	//$('#active_beginDate').parent().width($('#active_beginDate').width() + $('#active_beginDate').next().width()+20);
	//$('#active_endDate').parent().width($('#active_endDate').width() + $('#active_endDate').next().width()+20);
	$('#guide_2nd_couponExpired').parent().width($('#guide_2nd_couponExpired').width() + $('#guide_2nd_couponExpired').next().width()+20);
	
	$('#active_dateSearchDateBegin').parent().width($('#active_dateSearchDateBegin').width() + $('#active_dateSearchDateBegin').next().width()+20);
	$('#active_dateSearchDateEnd').parent().width($('#active_dateSearchDateEnd').width() + $('#active_dateSearchDateEnd').next().width()+20);
 	$('#active_dateSearchDateCombo').parent().width($('#active_dateSearchDateCombo').width() + $('#active_dateSearchDateCombo').next().width()+20);
 	$('#active_memberCostEqual').parent().width($('#active_memberCostEqual').width() + $('#active_memberCostEqual').next().width()+20);
 	$('#active_memberAmountEqual').parent().width($('#active_memberAmountEqual').width() + $('#active_memberAmountEqual').next().width()+20);
 	
 	$('#secondStepEastBody').children().first().children().first().css('overflow-y', 'visible');
 	$('#threeStepEastBody').children().first().children().first().css('overflow-y', 'visible');
 	$('#promotionPreviewBody').children().first().children().first().css('overflow-y', 'visible');
 	
});
