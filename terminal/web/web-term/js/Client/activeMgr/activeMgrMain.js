Ext.onReady(function(){
	//1表示无优惠劵纯展示; 2表示无条件领取优惠劵
	function updatePromotionCouponPanel(promotionType){
		//优惠券
		$("#promotionPic").attr("title", "优惠券图片");
		Ext.getCmp('guide_2nd_couponDetail').show();
		Ext.getCmp('guide_2nd_CouponOption').show();
		Ext.getCmp('guide_2nd_occupy').show();
		Ext.getCmp('guide_2nd_promotionEditor').setHeight(325);
		Ext.getCmp('guide_2nd_panel').doLayout();
	}
	
	//选择优惠券类型
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
		couponName.clearInvalid();
		price.clearInvalid();
		couponName.focus();
	}
	
	//生成优惠活动头部内容
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
	
	//显示优惠活动的Body和相应的Coupon内容
	function loadPromotion(promotionId){
		Ext.Ajax.request({
			url : '../../OperatePromotion.do',
			params : {
				dataSource : 'getByCond',
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
						var begin = jr.root[0].coupon.beginExpired != '' ? jr.root[0].coupon.beginExpired : '无设置';
						var end = jr.root[0].coupon.endExpired != '' ? jr.root[0].coupon.endExpired : '无设置';
						var limitAmount = jr.root[0].coupon.limitAmount != 0 ? jr.root[0].coupon.limitAmount : '无设置';
						
						Ext.getCmp('promotionCouponPreview').body.update('<div style="text-align:left; margin: 30px 10px 10px 20px;float:left;"><img height="100"  src="' + (jr.root[0].coupon.ossImage?jr.root[0].coupon.ossImage.image:'http://digie-image-real.oss.aliyuncs.com/nophoto.jpg') + '" /></div>'
																	+ '<div style="float:left;vertical-align: middle;line-height: 25px;"><br><span style="margin-top: 15px;">' + jr.root[0].coupon.name + '</span><br><span >面额 : ' + jr.root[0].coupon.price + ' 元</span><br><span >开始时间 : ' + begin + '</span><br><span>结束时间 : '+ end +'</span>' +
																	 '<br><span>限制数量 : ' + limitAmount + '</span></div>');							
					}else{
						Ext.getCmp('promotionCouponPreview').body.update('<div style="text-align:center; margin: 30px 10px 10px 10px;"><img height="100"  src="../../images/noCouponNow.png" /></div>');
					}												
						
					
					var issueRules = {
						FREE : {val : 1, desc : "免费发券"},
						SINGLE_EXCEED : {val : 2, desc : "单次消费满"},
						WX_SUBSCRIBE : {val : 3, desc : "微信关注"},
						WX_SCAN : {val : 4, desc : '扫码发券'},
						POINT_EXCHANGE : {val : 5, desc : '积分兑换'}
					}
								
					var useRules = {
						FREE : {val : 1, desc : "免费发券"},
						SINGLE_EXCEED : {val : 2, desc : "单次消费满"}
					}

					Ext.getCmp('week_combo_activeMgr').setValue('');
					Ext.getCmp('start_combo_activeMgr').setValue('');
					Ext.getCmp('end_combo_activeMgr').setValue('');
																	
					var items=Ext.getCmp('foodMultiPrice_column_weixin').items.items;
					if(items.length > 0){
						for(var i = items.length; i > -1 ; i--){
							Ext.getCmp('foodMultiPrice_column_weixin').remove(items[i]);
						}
						Ext.getCmp('foodMultiPrice_column_weixin').doLayout();
					}
					
					for(var i = 0; i < jr.root[0].useTime.length; i++){
						
						var subTitleId = 'subTitle' + i;

						Ext.getCmp('foodMultiPrice_column_weixin').add({
							cls : 'multiClass'+i,
					 		columnWidth : 1	 		
					 	});		
						
						Ext.getCmp('foodMultiPrice_column_weixin').add({
							cls : 'multiClass'+i,
							columnWidth: 0.6,
							items :[{
								xtype : 'label',
								id : subTitleId,
								text : ''
							}]
						});	
						
						//TODO
						Ext.getCmp('foodMultiPrice_column_weixin').add({
							cls : 'multiClass'+ i,
					 		columnWidth : .3,
					 		items : [{
						    	xtype : 'button',
						    	text : '删除',
						    	multiIndex : i,
						    	iconCls : 'btn_delete',
						    	handler : function(e){
						    		deleteMultiPriceHandler(e, i, jr.root[0].useTime);
						    	}
					 		}] 		 		
					 	});	
					 	
					 	Ext.getCmp('foodMultiPrice_column_weixin').doLayout();

						
						html = "<font size='4px'>" + jr.root[0].useTime[i].weekName + "   " + jr.root[0].useTime[i].start + "  到     " + jr.root[0].useTime[i].end + "</font>";
						$('#subTitle' + i).html(html);
					}
					
					
					//加载优惠券设置信息
					if(jr.root[0].issueTrigger.issueRule){
						if(jr.root[0].issueTrigger.issueRule == issueRules.FREE.val){
							Ext.getCmp('IssueSingleMoney_numberfield_active').setValue('');
							Ext.getCmp('pointExchange_numberfield_active').setValue('');
							Ext.getCmp('issueFree_active').setValue(true);
							Ext.getCmp('issueFree_active').fireEvent('focus');
						}else if(jr.root[0].issueTrigger.issueRule == issueRules.SINGLE_EXCEED.val){
							Ext.getCmp('IssueSingleMoney_numberfield_active').setValue(jr.root[0].issueTrigger.extra);
							Ext.getCmp('pointExchange_numberfield_active').setValue('');
							Ext.getCmp('issueSingle_active').setValue(true);
							Ext.getCmp('issueSingle_active').fireEvent('focus');
						}else if(jr.root[0].issueTrigger.issueRule == issueRules.WX_SUBSCRIBE.val){
							Ext.getCmp('IssueSingleMoney_numberfield_active').setValue('');
							Ext.getCmp('pointExchange_numberfield_active').setValue('');
							Ext.getCmp('issueWx_active').setValue(true);
							Ext.getCmp('issueWx_active').fireEvent('focus');
						}else if(jr.root[0].issueTrigger.issueRule == issueRules.WX_SCAN.val){
							Ext.getCmp('IssueSingleMoney_numberfield_active').setValue('');
							Ext.getCmp('pointExchange_numberfield_active').setValue('');
							Ext.getCmp('wxScan_active').setValue(true);
							Ext.getCmp('wxScan_active').fireEvent('focus');
						}else if(jr.root[0].issueTrigger.issueRule == issueRules.POINT_EXCHANGE.val){
							Ext.getCmp('pointExchange_numberfield_active').setValue(jr.root[0].issueTrigger.extra);
							Ext.getCmp('IssueSingleMoney_numberfield_active').setValue('');
							Ext.getCmp('point_exchange').setValue(true);
							Ext.getCmp('point_exchange').fireEvent('focus');
						}
					}
					
					if(jr.root[0].useTrigger.useRule){
						if(jr.root[0].useTrigger.useRule == useRules.FREE.val){
							Ext.getCmp('useSingleMoney_numberfield_active').setValue('');
							Ext.getCmp('useRule_active').setValue(true);
							Ext.getCmp('useRule_active').fireEvent('focus');
						}else if(jr.root[0].useTrigger.useRule == useRules.SINGLE_EXCEED.val){
							Ext.getCmp('useSingleMoney_numberfield_active').setValue(jr.root[0].useTrigger.extra);
							Ext.getCmp('useSingle_active').setValue(true);
							Ext.getCmp('useSingle_active').fireEvent('focus');
						}
					}
					
					
					
				}
			},
			failure : function(res, opt){
				Ext.ux.showMsg(Ext.decode(res.responseText));
			}
		});
	}
	
	var coupon_uploadMask = new Ext.LoadMask(document.body, {
		msg : '正在上传图片...'
	});
	var couponPicBox = null;
	(function(){
		couponPicBox = new Ext.BoxComponent({
			id : 'promotionPic',
			xtype : 'box',
	 	    columnWidth : 0.6,
	 	    height : 100,
	 	    width : 100,
	 	    style : 'marginRight:5px;',
	 	    autoEl : {
	 	    	tag : 'img',
	 	    	title : '优惠券图片预览'
	 	    }
		});
	})();
	
	 //优惠活动向导完成
    function fnFinishPromotion(){
	
		var title = Ext.getCmp('guide_2nd_title');
		if(title.getValue().length == 0){
			Ext.example.msg('提示', '请设置活动标题');
			Ext.getCmp('guide_2nd_title').focus();
			return;
		}
		var couponName = Ext.getCmp('guide_2nd_couponName');
		var price = Ext.getCmp('guide_2nd_couponPrice');
		var beginExpired = Ext.getCmp('beginDate_date_couponExpired');
		var endExpired = Ext.getCmp('endDate_date_couponExpired');
	
		var params = {};
		if(createActiveWin.otype == 'insert'){
			params.dataSource = 'insert';
		}else{
			params.dataSource = 'update';
			params.id = createActiveWin.promotion.id;
			params.cId = createActiveWin.promotion.coupon.id;
		}

		if(createActiveWin.ossId){
			params.image = createActiveWin.ossId;
		}
		params.couponName = couponName.getValue();
		params.price = price.getValue();
		
		if(Ext.getCmp('beginExpired_radio_active').getValue()){
			params.beginExpired = beginExpired.getValue().format('Y-m-d');
		}else{
			params.beginExpired = '';
		}
		
		if(Ext.getCmp('endExpired_radio_active').getValue()){
			params.endExpired = endExpired.getValue().format('Y-m-d');
		}else{
			params.endExpired = '';
		}
		
		if(Ext.getCmp('limitAmount_checkbox_active').getValue()){
			params.limitAmount = Ext.getCmp('limitAmount_text_active').getValue();
		}else{
			params.limitAmount = 0;
		}
		
		params.title = title.getValue();
		
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
					createActiveWin.hide();
					
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
	
	//创建优惠活动的window
	var createActiveWin = null;
	var promotionEditor;
	var btnPreview;
	var btnClear;
	var secendStepCenter;
	var btnUpload;
	var imgFile;
	var form;
	
	function initActiveWin(c){
		if(createActiveWin){
			$('#' + createActiveWin.id).remove();
		}
		if(promotionEditor){
			$('#' + promotionEditor.id).remove();
		}
		if(btnPreview){
			$('#' + btnPreview.id).remove();
		}
		if(btnClear){
			$('#' + btnClear.id).remove();
		}
		if(secendStepCenter){
			$('#' + secendStepCenter.id).remove();
		}
		if(btnUpload){
			$('#' + btnUpload.id).remove();
		}
		if(imgFile){
			$('#' + imgFile.id).remove();
		}
		if(form){
			$('#' + form.id).remove();
		}
		promotionEditor = new Ext.form.HtmlEditor({
			id : 'guide_2nd_promotionEditor',
			hideLabel : true,
			width : 510,
			height : 325,
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
		
		
		imgFile = Ext.ux.plugins.createImageFile({
			id : 'couponTypeBox',
			img : couponPicBox,
			width : 100,
			height : 100,
			callback : function(){
				btnUpload.handler();
			}
		});	
		
		form = new Ext.form.FormPanel({
			columnWidth : 0.6,
			labelWidth : 60,
			fileUpload : true,
			items : [imgFile],
			listeners : {
		    	render : function(e){
		    		Ext.getDom(e.getId()).setAttribute('enctype', 'multipart/form-data');
	 	  		}
		    }
		});	
		
		btnUpload = new Ext.Button({
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
		 	   				createActiveWin.image = ossImage.image;
		 	   				createActiveWin.ossId = ossImage.imageId;	   				
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
		
		secendStepCenter = new Ext.Panel({
			id : 'secondStepEastBody',
			region : 'east',
			width : 360,
			height : 525,
			bodyStyle : 'word-wrap:break-word;',
			style : 'marginLeft:635px;background-color: #fff; border: 1px solid #ccc; padding: 5px 5px 5px 5px;',
			html : '&nbsp;'
		});	
		
		
		btnClear = new Ext.Button({
			text : '清空',
			listeners : {
				render : function(thiz){
					thiz.getEl().setWidth(100, true);
				}
			},
			handler : function(){
				promotionEditor.setValue();
				Ext.getCmp('secondStepEastBody').update(promotionEditor.getValue());
			}
		});
		
		
		btnPreview = new Ext.Button({
			id : 'guide_2nd_previewBtn',
			text : '预览',
			listeners : {
				render : function(thiz){
					thiz.getEl().setWidth(100, true);
				}
			},
			handler : function(thiz, promotion){
				//解决获取不到editor里面内容的问题
	//			var html = (window.frames[promotionEditor.iframe.name]).document.getElementsByTagName('body')[0].innerHTML;
				var html;
//				secendStepCenter.body.update(buildPromotionHeader(Ext.getCmp('guide_2nd_title').getValue()) + (html ? html : (promotionEditor.value ? promotionEditor.value : '')));
				
				Ext.getCmp('secondStepEastBody').getForm().setValue(buildPromotionHeader(Ext.getCmp('guide_2nd_title').getValue()) + (html ? html : (promotionEditor.value ? promotionEditor.value : '')));
			}
		});
		
		var panel1 = new Ext.Panel({
		    border : false,
		    style : {
		    	'margin' : '0 auto',
		    	'width' : '100%'
		    },
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
		                    style : {
		                    	'margin-left' : '40px'
		                    },
		                    items : [{
		                        items : [{
		                            xtype : 'label',
		                            html : '&nbsp;&nbsp;选择优惠劵:&nbsp;&nbsp;'
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
		                    },{
								xtype : 'checkboxgroup',
								columns : 3,
								items : [{
									boxLabel : '开始时间',
									inputValue : 2,
									id : 'beginExpired_radio_active',
									name : 'time',
									listeners : {
										check : function(e){
											if(Ext.getCmp('beginExpired_radio_active').checked){
												Ext.getCmp('beginDate_date_couponExpired').enable();
											}else{
												Ext.getCmp('beginDate_date_couponExpired').disable();
											}
										},
										focus : function(e){
											if(Ext.getCmp('beginExpired_radio_active').checked){
												Ext.getCmp('beginDate_date_couponExpired').enable();
											}else{
												Ext.getCmp('beginDate_date_couponExpired').disable();
											}
										}
									}
								}]
							}, {
		                        items : [{
		                            id : 'beginDate_date_couponExpired',
		                            xtype : 'datefield',
		                            width : 130,
		                            fieldLabel : '&nbsp;&nbsp;&nbsp;开始时间',
		                            format : 'Y-m-d',
		                            readOnly : false,
		                            minValue : new Date(),
		                            value : new Date(),
		                            listeners : {
		                                invalid : function(thiz){
		                                    thiz.clearInvalid();
		                                }
		                            }
		                        }]
		                    },{
								xtype : 'checkboxgroup',
								columns : 3,
								items : [{
									boxLabel : '结束时间',
									inputValue : 2,
									id : 'endExpired_radio_active',
									name : 'time',
									listeners : {
										check : function(e){
											if(Ext.getCmp('endExpired_radio_active').checked){
												Ext.getCmp('endDate_date_couponExpired').enable();
											}else{
												Ext.getCmp('endDate_date_couponExpired').disable();
											}
										},
										focus : function(e){
											if(Ext.getCmp('endExpired_radio_active').checked){
												Ext.getCmp('endDate_date_couponExpired').enable();
											}else{
												Ext.getCmp('endDate_date_couponExpired').disable();
											}
										}
									}
								}]
		                    }, {
		                        items : [{
		                            id : 'endDate_date_couponExpired',
		                            xtype : 'datefield',
		                            width : 130,
		                            fieldLabel : '&nbsp;&nbsp;&nbsp;结束时间',
		                            format : 'Y-m-d',
		                            readOnly : false,
		                            minValue : new Date(),
		                            value : new Date(),
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
		                xtype : 'container',
		                layout : 'column',
		                items : [{
		                    xtype : 'panel',
		                    width : 280,
		                    layout : 'column',
		                    style : 'marginLeft:18px;',
		                    frame : true,
		                    items : [couponPicBox, form,{
		                        items : [{
		                            xtype : 'label',
		                            style : 'width : 140px;',
		                            html : '<sapn style="font-size:13px;color:green;font-weight:bold">图片大小不能超过100K</span>'
		                        }]
		                    },btnUpload],
		                    listeners : {
		                        render : function(e){
		                            Ext.getDom(e.getId()).setAttribute('enctype', 'multipart/form-data');
		                        }
		                    }
		                }]
		
		            },new Ext.form.FormPanel({
		                width : 510,
		                height : 350,
		                title : '编辑活动内容:',
		                items : [promotionEditor],
		                style : {
							'margin-left' : '100px',
							'margin-top' : '20px'
						},
		                buttonAlign : 'center',
		                buttons : [new Ext.Button({
		                    id : 'guide_2nd_previewBtn',
		                    text : '预览',
		                    listeners : {
		                        render : function(thiz){
		                            thiz.getEl().setWidth(100, true);
		                        }
		                    },
		                    handler : function(thiz, promotion){
		                        //解决获取不到editor里面内容的问题
								var html = (window.frames[promotionEditor.iframe.name]).document.getElementsByTagName('body')[0].innerHTML;
		                    	Ext.getCmp('secondStepEastBody').update(buildPromotionHeader(Ext.getCmp('guide_2nd_title').getValue()) + (html ? html : (promotionEditor.value ? promotionEditor.value : '')));
		                    }
		                }), new Ext.Button({
		                    text : '清空',
		                    listeners : {
		                        render : function(thiz){
		                            thiz.getEl().setWidth(100, true);
		                        }
		                    },
		                    handler : function(){
		                        promotionEditor.setValue();
		                        Ext.getCmp('secondStepEastBody').update(promotionEditor.getValue());
		                    }
		                })]
		            })]
		        },new Ext.Panel({
		            id : 'secondStepEastBody',
		            region : 'east',
		            width : 360,
		            height : 525,
		            bodyStyle : 'word-wrap:break-word;',
		            style : 'background-color: #fff; border: 1px solid #ccc; padding: 5px 5px 5px 5px;',
		            html : '&nbsp;'
		        })]
		    }]
		});




		var panel2 = new Ext.Panel({
		    id : 'guide_2nd_panel',
		    style : {
		    	'margin' : '0 auto',
		    	'margin-top' : '5px',
		    	'width' : '100%'
		    },
		    border : false,
		    items : [{
		        layout : 'column',
		        frame : true,
		        border : false,
		
		        defaults : {
		            layout : 'form',
		            labelWidth : 70,
		            labelAlign : 'right',
		            columnWidth : .25
		        },
		        items : [{
		            labelWidth : 160,
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
		                width : 300,
		                fieldLabel : '&nbsp;&nbsp;&nbsp;活动标题',
		                style : 'overflow: hidden;',
		                allowBlank : false,
		                listeners : {
		                    blur : function(){
		                        Ext.getCmp('guide_2nd_previewBtn').handler();
		                    }
		                }
		            }]
		        }, {
		            columnWidth : .1,
		            items : [{
		            	xtype: "checkbox",
		            	id : 'limitAmount_checkbox_active',
		            	listeners : {
							check : function(e){
								if(Ext.getCmp('limitAmount_checkbox_active').checked){
									Ext.getCmp('limitAmount_text_active').enable();
								}else{
									Ext.getCmp('limitAmount_text_active').disable();
								}
							},
							focus : function(e){
								if(Ext.getCmp('limitAmount_checkbox_active').checked){
									Ext.getCmp('limitAmount_text_active').enable();
								}else{
									Ext.getCmp('limitAmount_text_active').disable();
								}
							}
						}
		            }]
		        }, {
		        	columnWidth : .2,
	        		style : {
						'margin-left' : '-20px'
					},
		        	items : [{
		                id : 'limitAmount_text_active',
		                xtype : 'textfield',
		                width : 100,
		                fieldLabel : '限制数量'
		        	}]
		        }]
		    }]
		});
				
				
		createActiveWin = new Ext.Window({
			title : '设置优惠活动',
			resizable : false,
			id : 'ceateActiveWindow_activeMgr',
			modal : true,
			width : 1100,
			height : 650,
			autoDestroy : true,
			items : [panel2, panel1],
			listeners : {
				show : function(thiz){
					if(thiz.otype == 'insert'){
						this.setTitle('添加优惠活动');
						//跳到第一步
						Ext.getCmp('guide_2nd_title').setValue();
						Ext.getCmp('guide_2nd_couponName').setValue();
						Ext.getCmp('guide_2nd_couponPrice').setValue();
						Ext.getCmp('beginDate_date_couponExpired').setValue(new Date());
						Ext.getCmp('endDate_date_couponExpired').setValue(new Date());
						Ext.getCmp('guide_2nd_promotionEditor').setValue();
						Ext.getCmp('beginDate_date_couponExpired').clearInvalid();
						Ext.getCmp('endDate_date_couponExpired').clearInvalid();
						
						Ext.getCmp('limitAmount_checkbox_active').setValue(false);
						Ext.getCmp('beginExpired_radio_active').setValue(true); 
						
						Ext.getCmp('endExpired_radio_active').setValue(true); 
						
						
						Ext.getDom('radioDefaultCoupon').checked = true; 
						Ext.getCmp('radioDefaultCoupon').fireEvent('check', Ext.getCmp('radioDefaultCoupon'), true);
						Ext.getCmp('couponTypeBox').setImg();
						
						Ext.getCmp('secondStepEastBody').body.update('');
						 
						createActiveWin.image = '';
						
						//TODO
					}else if(thiz.otype == 'update'){
						this.setTitle('修改优惠活动');
						
						Ext.getCmp('guide_2nd_title').setValue(thiz.promotion.title);
						Ext.getCmp('guide_2nd_couponName').setValue(thiz.promotion.coupon.name);
						Ext.getCmp('guide_2nd_couponPrice').setValue(thiz.promotion.coupon.price);
						
						if(thiz.promotion.coupon.beginExpired != ''){
							Ext.getCmp('beginDate_date_couponExpired').setValue(thiz.promotion.coupon.beginExpired);
							Ext.getCmp('beginExpired_radio_active').setValue(true); 
							
						}else{
							Ext.getCmp('beginExpired_radio_active').setValue(false); 
						}
						
						
						if(thiz.promotion.coupon.endExpired != ''){
							Ext.getCmp('endDate_date_couponExpired').setValue(thiz.promotion.coupon.endExpired);
							Ext.getCmp('endExpired_radio_active').setValue(true); 
						}else{
							Ext.getCmp('endExpired_radio_active').setValue(false); 
						}
						
						if(thiz.promotion.coupon.limitAmount > 0){
							Ext.getCmp('limitAmount_checkbox_active').setValue(true);
							Ext.getCmp('limitAmount_text_active').setValue(thiz.promotion.coupon.limitAmount);
						}else{
							Ext.getCmp('limitAmount_checkbox_active').setValue(false);
						}
						
						Ext.getCmp('beginExpired_radio_active').fireEvent('focus');
						Ext.getCmp('endExpired_radio_active').fireEvent('focus');
						Ext.getCmp('limitAmount_checkbox_active').fireEvent('focus');
						
						Ext.getCmp('guide_2nd_promotionEditor').setValue(thiz.promotion.body);
						
						Ext.getDom('radioSelfCoupon').checked = true; 
						Ext.getCmp('couponTypeBox').setImg(thiz.promotion.coupon.ossImage ? thiz.promotion.coupon.ossImage.image : 'http://digie-image-real.oss.aliyuncs.com/nophoto.jpg');
						//手动调用预览，更新显示图文
//						btnPreview.handler('' ,thiz.promotion);
						updatePromotionCouponPanel(thiz.promotion.pType);
					}
					
					
				},
				hide : function(thiz){
						//创建活动Win关闭后，清除图片的信息
	 	   				thiz.image = null;
	 	   				thiz.ossId = null;							
				},
				close : function(){
					createActiveWin.removeAll();
//					panel1.destory();
//					panel2.destory();
					createActiveWin = null;
					panel1 = null;
					panel2 = null;
				}
			},
			keys : [{
					 key : Ext.EventObject.ESC,
					 fn : function(){ 
						 createActiveWin.hide();
						 $('#ceateActiveWindow_activeMgr').remove($('#ceateActiveWindow_activeMgr'));
					 },
					 scope : this 
			 }],
			bbar : ['->', {
				text : '保存',
				id : 'btnPublish',
				iconCls : 'btn_save',
				handler : function(){
					//触发‘预览’Button的事件，更新promotion preview的内容
		        	Ext.getCmp('guide_2nd_previewBtn').handler();
		        	fnFinishPromotion();
				}
			}, {
				text : '取消',
				id : 'btnCancelPublish',
				iconCls : 'btn_close',
				handler : function(){
					createActiveWin.hide();
					$('#ceateActiveWindow_activeMgr').remove();
				}
			}]
		})
		
		createActiveWin.otype = c.type;
		if(c.promotion){
			createActiveWin.promotion = c.promotion;
		}
		
		Ext.getCmp('ceateActiveWindow_activeMgr').show();
		Ext.getCmp('guide_2nd_previewBtn').handler();
	}	
		
	function initQrcode(){
		var hostName = window.location.hostname;
		if(hostName == 'e-tones.net' || hostName == 'lb.e-tones.net'){
			hostName = 'wx.e-tones.net';
		}else if(hostName == 'localhost'){
			hostName = 'ts.e-tones.net';
		}else{
			hostName = window.location.host;
		}
	
		var scanType = {
			WX_SCAN_ISSUE_COUPON : '5'
		};
	
		var node = Ext.ux.getSelNode(promotionTree);
		if (!node || node.attributes.id == -1) {
			Ext.example.msg('提示', '操作失败, 请选择一个活动再进行操作.');
			return;
		}
		
		$.ajax({
		    type : "post",
		    url : "http://" + hostName + "/wx-term/WxOperateQrCode.do",
		    dataType : "jsonp",
		    data : {
		    	dataSource : 'qrCode',
		    	restaurantId : restaurantID,
		    	limitStr : scanType.WX_SCAN_ISSUE_COUPON + node.attributes.id
		    },
		    jsonp: "callback",//服务端用于接收callback调用的function名的参数
		    jsonpCallback:"success_jsonpCallback",//(可选)callback的function名称, 不设置时有默认的名称
			success : function(json){
				
				
				var qrCodeWindow = new Ext.Window({
					id : 'activeMgrQrCode_window_activeMgr',
					title : '区域二维码',
					closable : true,
					resizeble : false,
					modal : true,
					width : 500,
					height : 550,
					items : [{
						id : 'qrCodeView_window_activeMgr',
						xtype : 'panel',
						height : 500,
						width : 480,
						style : {
							'margin' : '2% auto'
						},
						html : '<img alt="" src="'+ json.root[0].qrCode +'" width="480px" height="480px">'
					}],
					bbar : ['->',{
						text : '下载二维码',
						iconCls : 'btn_save',
						handler : function(){
		    				
		    				var url = '../../{0}?dataSource={1}&qrcode={2}&couponName={3}';
								url = String.format(
									url,
									'DownLoadQrcode.do',
									'downLoad',
									json.root[0].qrCode,
									node.attributes.title
								)
								window.location = url;
						}
					}]
				});	
				
				qrCodeWindow.render(document.body);
				qrCodeWindow.show();
				
			},
		    error:function(){
		    	loadMask.hide();
		    }
		})
		
	
	}
	
	
	// 活动框体
	var promotionPreviewPanel = null;
	(function(){
		promotionPreviewPanel = new Ext.Panel({
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
					height : 200,
					html : '<div style="text-align:center; margin: 30px 10px 10px 10px;"><img height="160"  src="http://digie-image-real.oss-cn-hangzhou.aliyuncs.com/WxPromotion/noCoupon.png" /></div>'
				}),new Ext.Panel({
					id : 'promotionGeneral',
					region : 'center',
					height : 250,
					layout : 'border',
					items : [new Ext.Panel({
						title : '发券与用券限制条件',
						frame : true,
						id : '',
						region : 'center',
						height : 120,
						tbar : ['->', {
							text : '保存',
							iconCls : 'btn_add',
							handler : function(e){
								var issueRules = {
									FREE : {val : 1, desc : "免费发券"},
									SINGLE_EXCEED : {val : 2, desc : "单次消费满"},
									WX_SUBSCRIBE : {val : 3, desc : "微信关注"},
									WX_SCAN_ISSUE_COUPON : {val : 4, desc : '扫码发券'},
									POINT_EXCHANGE : {val : 5, desc : '积分兑换'}
								}
								
								var useRules = {
									FREE : {val : 1, desc : "免费发券"},
									SINGLE_EXCEED : {val : 2, desc : "单次消费满"}
								}
								
								//选中的优惠券
								var node = Ext.ux.getSelNode(promotionTree);
								if (!node || node.attributes.id == -1) {
									Ext.example.msg('提示', '操作失败, 请选择一个活动再进行操作.');
									return;
								}
								
								//优惠活动Id
								var promotionId = node.attributes.id;
								
								var issueRuleValue = null;     //发送规则选中的值
								var useRuleValue = null;       //用券规则选中的值
								
								if(Ext.getCmp('issueFree_active').getValue()){
									issueRuleValue = 1;
								}else if(Ext.getCmp('issueSingle_active').getValue()){
									issueRuleValue = 2;
								}else if(Ext.getCmp('issueWx_active').getValue()){
									issueRuleValue = 3;
								}else if(Ext.getCmp('wxScan_active').getValue()){
									issueRuleValue = 4;
								}else if(Ext.getCmp('point_exchange').getValue()){
									issueRuleValue = 5;
								}
								
								if(Ext.getCmp('useRule_active').getValue()){
									useRuleValue = 1;
								}else if(Ext.getCmp('useSingle_active').getValue()){
									useRuleValue = 2;
								}
								
								//设定发券规则
								var issueRule = null;
								var issueSingeMoney = null;
								var point = null;
								if(issueRuleValue == 1){
									issueRule = issueRules.FREE.val;
								}else if (issueRuleValue == 2){
									issueRule = issueRules.SINGLE_EXCEED.val;
									if(Ext.getCmp('IssueSingleMoney_numberfield_active').getValue() == ''){
										Ext.example.msg('提示', '操作失败, 发券满足金额不能为空或者只能填写数字');
										return;
									}else{
										issueSingeMoney = Ext.getCmp('IssueSingleMoney_numberfield_active').getValue();
									}
									
								}else if(issueRuleValue == 3){
									issueRule = issueRules.WX_SUBSCRIBE.val;
								}else if(issueRuleValue == 4){
									issueRule = issueRules.WX_SCAN_ISSUE_COUPON.val;
								}else if(issueRuleValue == 5){
									issueRule = issueRules.POINT_EXCHANGE.val;
									
									if(Ext.getCmp('pointExchange_numberfield_active').getValue() == ''){
										Ext.example.msg('提示', '操作失败, 发券满足金额不能为空或者只能填写数字');
										return;
									}else{
										point = Ext.getCmp('pointExchange_numberfield_active').getValue();
									}
								}
								
								//设定用券规则
								var useRule = null;
								var useSingleMoney = null;
								if(useRuleValue == 1){
									useRule = useRules.FREE.val;
								}else if(useRuleValue == 2){
									useRule = useRules.SINGLE_EXCEED.val;
									if(Ext.getCmp('useSingleMoney_numberfield_active').getValue() == ''){
										Ext.example.msg('提示', '操作失败, 用券满足金额不能为空或者只能填写数字');
										return;
									}else{
										useSingleMoney = Ext.getCmp('useSingleMoney_numberfield_active').getValue();
									}
								}
								
								Ext.Ajax.request({
									url : '../../OperatePromotion.do',
									params : {
										dataSource : 'update',
										id : promotionId,
										issueRule : issueRule,
										issueSingleMoney : issueRule == issueRules.SINGLE_EXCEED.val ? issueSingeMoney : null,
										useRule : useRule,
										useSingleMoney : useRule == issueRules.SINGLE_EXCEED.val ? useSingleMoney : null,
										point : point
									},
									success : function(res, opt){
										var jr = Ext.decode(res.responseText);
										if(jr.success){
											Ext.example.msg('提示', '设置成功');
										}else{
											Ext.example.msg('提示', '设置失败');
										}
									},
									failure : function(res, opt){
										Ext.example.msg('提示', '设置失败');
									}
								})
								
							}
						}],
						items : [{
							xtype : 'panel',
							layout : 'form',
							id : 'couponConfigRules_panel_activeMgrMain',
							autoScroll : true,
							height : (parseInt(Ext.getDom('divActive').parentElement.style.height.replace(/px/g,''))) - 400,
							frame : true,
								items : [{
								xtype : 'radiogroup',
								fieldLabel : '发券规则',
								id : 'issueRule',
								columns : 2,
								items : [{
									boxLabel : '无条件',
									inputValue : 1,
									id : 'issueFree_active',
									name : 'sendCouponRule',
									listeners : {
										check : function(e){
										},
										focus : function(e){
											Ext.getCmp('sendCouponByOrder_fieldset_activeMgr').hide();
											Ext.getCmp('createQrCode_fieldset_activeMgr').hide();
											Ext.getCmp('pointExchange_fieldset_activeMgr').hide();
										}
									}
								},{
									boxLabel : '账单发券',
									name : 'sendCouponRule',
									id : 'issueSingle_active',
									inputValue : 2,
									listeners : {
										check : function(e, check){
											
										},
										focus : function(e, check){
											Ext.getCmp('sendCouponByOrder_fieldset_activeMgr').show();
											Ext.getCmp('createQrCode_fieldset_activeMgr').hide();
											Ext.getCmp('pointExchange_fieldset_activeMgr').hide();
										}
									}
								},{
									boxLabel : '微信首次关注发券',
									name : 'sendCouponRule',
									id : 'issueWx_active',
									inputValue : 3,
									listeners : {
										check : function(e){
											
										},
										focus : function(e){
											Ext.getCmp('sendCouponByOrder_fieldset_activeMgr').hide();
											Ext.getCmp('createQrCode_fieldset_activeMgr').hide();
											Ext.getCmp('pointExchange_fieldset_activeMgr').hide();
										}
									}
								}, {
									boxLabel : '扫码发券',
									name : 'sendCouponRule',
									id : 'wxScan_active',
									inputValue : 4,
									listeners : {
										check : function(e){
										},
										focus : function(e){
											Ext.getCmp('createQrCode_fieldset_activeMgr').show();
											Ext.getCmp('sendCouponByOrder_fieldset_activeMgr').hide();
											Ext.getCmp('pointExchange_fieldset_activeMgr').hide();
										}
									}
								}, {
									boxLabel : '积分兑换',
									name : 'sendCouponRule',
									id : 'point_exchange',
									inputValue : 5,
									listeners : {
										check : function(e){
										},
										focus : function(e){
											Ext.getCmp('pointExchange_fieldset_activeMgr').show();
											Ext.getCmp('createQrCode_fieldset_activeMgr').hide();
											Ext.getCmp('sendCouponByOrder_fieldset_activeMgr').hide();
										}
									}
								}]
								}, {
									title : '账单发券配置',
									id : 'sendCouponByOrder_fieldset_activeMgr',
									xtype : 'fieldset',
									layout : 'form',
									hidden : true,
									items : [{
										xtype : 'numberfield',
										id : 'IssueSingleMoney_numberfield_active',
										fieldLabel : '发送满足金额',
										allowBlank : false,
										blankText : '发送满足金额不能为空.',
										value : ""
									}]
								}, {
									title : '扫码发券二维码',
									id : 'createQrCode_fieldset_activeMgr',
									xtype : 'fieldset',
									layout : 'form',
									hidden : false,
									items : [{
										xtype : 'button',
										id : 'createQrcode_button_activeMgr',
										text : '生成二维码',
										iconCls : 'btn_add',
										handler : function(){
											initQrcode();
										}
									}]
								}, {
									title : '积分兑换',
									id : 'pointExchange_fieldset_activeMgr',
									xtype : 'fieldset',
									layout : 'form',
									hidden : true,
									items : [{
										xtype : 'numberfield',
										id : 'pointExchange_numberfield_active',
										fieldLabel : '积分',
										allowBlank : false,
										blankText : '积分不能为空.',
										value : ""
									}]
								}, {
								xtype : 'radiogroup',
								fieldLabel : '用券规则',
								columns : 3,
								items : [{
									boxLabel : '无条件',
									inputValue : 1,
									id : 'useRule_active',
									name : 'useCouponRule',
									listeners : {
										check : function(e){
										},
										focus : function(e){
											
											Ext.getCmp('useCouponByOrder_fieldset_activeMgr').hide();
										}
									}
								},{
									boxLabel : '账单用券',
									name : 'useCouponRule',
									id : 'useSingle_active',
									inputValue : 2,
									listeners : {
										check : function(checkbox, check){
											if(check){
												Ext.getCmp('useCouponByOrder_fieldset_activeMgr').show();
											}else{
												Ext.getCmp('useCouponByOrder_fieldset_activeMgr').hide();
											}
										},
										focus : function(e){
											Ext.getCmp('useCouponByOrder_fieldset_activeMgr').show();
										}
									}
								}]
								}, {
									title : '账单用券配置',
									id : 'useCouponByOrder_fieldset_activeMgr',
									xtype : 'fieldset',
									layout : 'form',
									hidden : true,
									items : [{
										xtype : 'numberfield',
										id : 'useSingleMoney_numberfield_active',
										fieldLabel : '用券满足金额',
										allowBlank : false,
										blankText : '用券满足金额不能为空',
										value : ""
									}]
								}]
						}]
					})]
				}), new Ext.Panel({
					title : '优惠券限制使用时段',
					region : 'south',
					height : 300,
					frame : true,
					items : [{
						xtype : 'panel',
						layout : 'column',
						autoScroll : true,
						height : 300,
						frame : true, 
						items : [
							{
								columnWidth : 1,
								style :'margin-bottom:10px;',
								border : false		
							},{
								columnWidth : 1,
								xtype : 'label',
								html : '<font color="red">请选择要设置的时段,开始时间不能比结束时间要大,一天只能设置一个时段,设置完请按保存,如需继续设置,请继续上述操作</font>'
							},{
								columnWidth : 1,
								style :'margin-bottom:10px;',
								border : false		
							}, {
								columnWidth : 0.2,
								id : 'week_combo_activeMgr',
								xtype : 'combo',
								readOnly : false,
								forceSelection : true,
								width : 30,
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
										thiz.store.loadData([["2","星期一"], ["3", "星期二"], ["4", "星期三"], ["5", "星期四"], ["6", "星期五"], ["7", "星期六"], ["1", "星期日"]]);
										thiz.setValue("2");
										thiz.fireEvent('select');
									},
									select : function(){
										Ext.getCmp('start_combo_activeMgr').setValue('1');
										Ext.getCmp('end_combo_activeMgr').setValue('1');
//										
//										Ext.getCmp('useTime_label_activeMgr').html = "<font>sddsd</font>";
//										Ext.getCmp('useTime_label_activeMgr').html = "<font>sddsd123</font>";
										
									}
								}
							}, {
								columnWidth : 0.2,
								id : 'start_combo_activeMgr',
								xtype : 'combo',
								readOnly : false,
								forceSelection : true,
								width : 20,
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
											var data = [['1', '1:00' ], ['2', '2:00' ], ['3', '3:00' ], ['4', '4:00' ], ['5', '5:00'], ['6', '6:00'], ['7', '7:00'], ['8', '8:00'], ['9', '9:00'], ['10', '10:00'], ['11', '11:00'], ['12', '12:00'], 
													['13', '13:00'], ['14', '14:00'], ['15', '15:00'], ['16', '16:00'], ['17', '17:00'], ['18', '18:00'], ['19', '19:00'], ['20', '20:00'], ['21', '21:00'], ['22', '22:00'], ['23', '23:00'], ['24', '24:00']];
										thiz.store.loadData(data);
										thiz.setValue('1');
									},
									select : function(){
										
									}
								}
							},{
								xtype : 'label',
								text : '~'
							}, {
								columnWidth : 0.2,
								id : 'end_combo_activeMgr',
								xtype : 'combo',
								readOnly : false,
								forceSelection : true,
								width : 20,
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
										var data = [['1', '1:00' ], ['2', '2:00' ], ['3', '3:00' ], ['4', '4:00' ], ['5', '5:00'], ['6', '6:00'], ['7', '7:00'], ['8', '8:00'], ['9', '9:00'], ['10', '10:00'], ['11', '11:00'], ['12', '12:00'], 
													['13', '13:00'], ['14', '14:00'], ['15', '15:00'], ['16', '16:00'], ['17', '17:00'], ['18', '18:00'], ['19', '19:00'], ['20', '20:00'], ['21', '21:00'], ['22', '22:00'], ['23', '23:00'], ['24', '24:00']];
										thiz.store.loadData(data);
										thiz.setValue('1');
									},
									select : function(){
										
									}
								}
							},{
								xtype : 'button',
	        			    	text : '添加',
	        			    	style:'margin-left:2px;',
	        			    	iconCls : 'btn_add',
								handler : function(e){
									//选中的优惠券
									var node = Ext.ux.getSelNode(promotionTree);
									if (!node || node.attributes.id == -1) {
										Ext.example.msg('提示', '操作失败, 请选择一个活动再进行操作.');
										return;
									}
									
									//优惠活动Id
									var promotionId = node.attributes.id;
									
									var promotionUseTime = null;
									$.ajax({
										url : '../../OperatePromotion.do',
										data : {
											dataSource : 'getByCond',
											promotionId : promotionId
										},
										type : 'post',
										dataType : 'json',
										success : function(jr){
											if(jr.success){
												promotionUseTime = jr.root[0].useTime;
												
												var promotionTimes = [];
												if(promotionUseTime.length > 0){
													for(var i = 0; i < promotionUseTime.length; i++){
														var useTimes = [];
														useTimes.push(promotionUseTime[i].week);
														useTimes.push(promotionUseTime[i].start);
														useTimes.push(promotionUseTime[i].end);
														promotionTimes.push(useTimes.join(','));
													}
												}
												
												
												var times = [];
												
												if(Ext.getCmp('week_combo_activeMgr').getValue()){
													times.push(Ext.getCmp('week_combo_activeMgr').getValue());
												}
												
												if(Ext.getCmp('start_combo_activeMgr').getValue()){
													times.push(Ext.getCmp('start_combo_activeMgr').getValue()+":00");
												}

												if(Ext.getCmp('end_combo_activeMgr').getValue()){
													times.push(Ext.getCmp('end_combo_activeMgr').getValue()+":00");
												}
												
												promotionTimes.push(times);
												
												$.ajax({
													url : '../../OperatePromotion.do',
													type : 'post',
													dataType : 'json',
													data : {
														dataSource : 'update',
														id : promotionId,
														useTimes : promotionTimes.join('@')
													},
													success : function(jr){
														if(jr.success){
															$.ajax({
																url : '../../OperatePromotion.do',
																data : {
																	dataSource : 'getByCond',
																	promotionId : promotionId
																},
																type : 'post',
																dataType : 'json',
																success : function(jr){
																	Ext.getCmp('week_combo_activeMgr').setValue('');
																	Ext.getCmp('start_combo_activeMgr').setValue('');
																	Ext.getCmp('end_combo_activeMgr').setValue('');
																	
																	var html = "";
																	
																	var items=Ext.getCmp('foodMultiPrice_column_weixin').items.items;
																	if(items.length > 0){
																		for(var i = items.length; i > -1 ; i--){
																			Ext.getCmp('foodMultiPrice_column_weixin').remove(items[i]);
																		}
																		Ext.getCmp('foodMultiPrice_column_weixin').doLayout();
																	}
																	
																	for(var i = 0; i < jr.root[0].useTime.length; i++){
																		
																		var subTitleId = 'subTitle' + i;
		
																		Ext.getCmp('foodMultiPrice_column_weixin').add({
																			cls : 'multiClass'+i,
																	 		columnWidth : 1	 		
																	 	});		
																		
																		Ext.getCmp('foodMultiPrice_column_weixin').add({
																			cls : 'multiClass'+i,
																			columnWidth: 0.6,
																			items :[{
																				xtype : 'label',
																				id : subTitleId,
																				text : ''
																			}]
																		});	
																		
																		Ext.getCmp('foodMultiPrice_column_weixin').add({
																			cls : 'multiClass'+ i,
																	 		columnWidth : .3,
																	 		items : [{
																		    	xtype : 'button',
																		    	text : '删除',
																		    	multiIndex : i,
																		    	iconCls : 'btn_delete',
																		    	handler : function(e){
																		    		deleteMultiPriceHandler(e, i, jr.root[0].useTime);
																		    	}
																	 		}] 		 		
																	 	});	
																	 	
																	 	Ext.getCmp('foodMultiPrice_column_weixin').doLayout();
		
																		
																		html = "<font size='4px'>" + jr.root[0].useTime[i].weekName + "   " + jr.root[0].useTime[i].start + "  到     " + jr.root[0].useTime[i].end + "</font>";
																		$('#subTitle' + i).html(html);
																	}
																	
																	
																}
															});
														}
														Ext.example.msg("提示", jr.msg);
													}
												});
											}
										}
									});
								}
							},{
								columnWidth : 1,
								style :'margin-bottom:10px;',
								border : false		
							},{
								columnWidth : 1,
								border : false		
							}, {
								columnWidth: 1,
								id : 'foodMultiPrice_column_weixin',
								layout : 'column',
								width : 400,
								frame : false,
								defaults : {
									layout : 'form'
								},
								items : []
							}
						]}
					]
				})]		
			})]
		})
	})();
	
	function deleteMultiPriceHandler(e, i, promotionUseTime){
		var cmps = $('.multiClass'+Ext.getCmp(e.id).multiIndex);
		
		promotionUseTime.splice(Ext.getCmp(e.id).multiIndex, 1);
		
		for (var i = 0; i < cmps.length; i++) {
			Ext.getCmp('foodMultiPrice_column_weixin').remove(cmps[i].getAttribute("id"));
		}
		
		Ext.getCmp('foodMultiPrice_column_weixin').doLayout();
		
		//选中的优惠券
		var node = Ext.ux.getSelNode(promotionTree);
		if (!node || node.attributes.id == -1) {
			Ext.example.msg('提示', '操作失败, 请选择一个活动再进行操作.');
			return;
		}
		
		//优惠活动Id
		var promotionId = node.attributes.id;
									
		var promotionTimes = [];
		if(promotionUseTime.length > 0){
			for(var i = 0; i < promotionUseTime.length; i++){
				var useTimes = [];
				useTimes.push(promotionUseTime[i].week);
				useTimes.push(promotionUseTime[i].start);
				useTimes.push(promotionUseTime[i].end);
				promotionTimes.push(useTimes.join(','));
			}
		}
		
		$.ajax({
			url : '../../OperatePromotion.do',
			type : 'post',
			dataType : 'json',
			data : {
				dataSource : 'update',
				id : promotionId,
				useTimes : promotionTimes.join('@')
			},
			success : function(jr){
				if(jr.success){
					$.ajax({
						url : '../../OperatePromotion.do',
						data : {
							dataSource : 'getByCond',
							promotionId : promotionId
						},
						type : 'post',
						dataType : 'json',
						success : function(jr){
							var html = "";
							
							var items=Ext.getCmp('foodMultiPrice_column_weixin').items.items;
							if(items.length > 0){
								for(var i = items.length; i > -1 ; i--){
									Ext.getCmp('foodMultiPrice_column_weixin').remove(items[i]);
								}
								Ext.getCmp('foodMultiPrice_column_weixin').doLayout();
							}
							
							for(var i = 0; i < jr.root[0].useTime.length; i++){
								
								var subTitleId = 'subTitle' + i;

								Ext.getCmp('foodMultiPrice_column_weixin').add({
									cls : 'multiClass'+i,
							 		columnWidth : 1	 		
							 	});		
								
								Ext.getCmp('foodMultiPrice_column_weixin').add({
									cls : 'multiClass'+i,
									columnWidth: 0.6,
									items :[{
										xtype : 'label',
										id : subTitleId,
										text : ''
									}]
								});	
								
								Ext.getCmp('foodMultiPrice_column_weixin').add({
									cls : 'multiClass'+ i,
							 		columnWidth : .3,
							 		items : [{
								    	xtype : 'button',
								    	text : '删除',
								    	multiIndex : i,
								    	iconCls : 'btn_delete',
								    	handler : function(e){
								    		deleteMultiPriceHandler(e, i, jr.root[0].useTime);
								    	}
							 		}] 		 		
							 	});	
							 	
							 	Ext.getCmp('foodMultiPrice_column_weixin').doLayout();

								
								html = "<font size='4px'>" + jr.root[0].useTime[i].weekName + "   " + jr.root[0].useTime[i].start + "  到     " + jr.root[0].useTime[i].end + "</font>";
								$('#subTitle' + i).html(html);
							}
							
							
						}
					});
				}
				Ext.example.msg("提示", jr.msg);
			}
		});
		
		
	}
	
	
	//活动树
	var promotionTree = null;
	(function(){
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
						$('#divActiveInsert').show();
						
						initActiveWin({type : 'insert'});
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
						promotionTree.getRootNode().getUI().show();
					}
				},
				click : function(e){
		    		if(e.id < 0){
		    			return;
		    		}
					loadPromotion(e.attributes.id);
					
				}
			}
		});
	})();
	
	
	//trigger描述
	var Trigger = {
		WX_SUBSCRIBE : { type : 1, desc : '微信关注'}	
	};
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
				width : 200,
				xtype : 'panel',
				layout : 'column',
				frame : true,
				modal : true,
				items : [{
					columnWidth : 1,
					style :'margin-top:5px;',
					border : false
				},
				{
					columnWidth : 0.4,
					xtype : 'label',
					html : '&nbsp;&nbsp;&nbsp;&nbsp;结束时间:'
				},{
					columnWidth : 0.6,
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
									publishPromotionWnd.close();
									
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
						publishPromotionWnd.hide();
					}
				}],
				listeners : {
					'show' : function(thiz){
						//获取要发布的活动详情
						Ext.Ajax.request({
							url : '../../OperatePromotion.do?',
							params : {
								dataSource : 'getByCond',
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
	
	//修改活动
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
				dataSource : 'getByCond' 
			},
			success : function(res, opt){
				wx.lm.hide();
				var jr = Ext.decode(res.responseText);
				if(jr.success){
					$('#divActiveInsert').show();
					initActiveWin({ type : 'update', promotion : jr.root[0] });
				}
			},
			failure : function(res, opt){
				wx.lm.hide();
				Ext.ux.showMsg(Ext.decode(res.responseText));
			}
		});
	}
	
	//删除活动
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
	
	
	new Ext.Panel({
		renderTo : 'divActive',
		layout : 'border',
		width : parseInt(Ext.getDom('divActive').parentElement.style.width.replace(/px/g,'')),
		height : parseInt(Ext.getDom('divActive').parentElement.style.height.replace(/px/g,'')),
		items : [promotionTree, promotionPreviewPanel]
	});
	
	//TODO
//		new Ext.Panel({
//			id : 'guide_2nd_panel',
//			renderTo :'active_secendStep',
//			width :1015,
//			border : false,
//	 		items : [{
//				layout : 'column',
//				frame : true,
//				border : false,
//	
//				defaults : {
//					layout : 'form',
//					labelWidth : 70,
//					labelAlign : 'right',
//					columnWidth : .33
//				},
//				items : [{
//					labelWidth : 180,
//					items : [{
//						id : 'guide_2nd_occupy',
//						xtype : 'label',
//						style : 'color:#DFE8F6',
//						html : '单次消费积分满/累计积分满-有优惠劵'
//					}]
//				},{
//					columnWidth : .3,
//					items : [{
//						id : 'guide_2nd_title',
//						xtype : 'textfield',
//						width : 200,
//						fieldLabel : '&nbsp;&nbsp;&nbsp;活动标题',
//						style : 'overflow: hidden;',
//						allowBlank : false,
//						listeners : {
//							blur : function(){
//								Ext.getCmp('guide_2nd_previewBtn').handler();
//							}
//						}
//					}]
//				}]
//			}]
//	});
	
	
	//TODO
//	new Ext.Panel({
//		renderTo :'active_secendStep2',
//		width :1015,
//		border : false,
//		items : [{
//			layout : 'border',
//			frame : true,
//			border : false,
//			height : 545,
//			items : [{
//	 			xtype : 'panel',
//	 			region : 'center',
//	 			width :620,
//	 			height : 535,
//				layout : 'column',
//				frame : true,
//				defaults : {
//					layout : 'form',
//					labelWidth : 70,
//					labelAlign : 'right'
////					columnWidth : .33
//				},
//				items : [{
//					id : 'guide_2nd_CouponOption',
//					items : [{
//						id : 'westPanel',
//						xtype : 'panel',
//						defaults : {
//							layout : 'form',
//							labelWidth : 70,
//							labelAlign : 'right'
//						},
//						items : [{
//							items : [{
//								xtype : 'label',
//								html : '&nbsp;&nbsp;选择优惠劵:&nbsp;&nbsp;'
//							}]			
//						},{
//							items : [{
//								xtype : 'radio',
//								id : 'radioDefaultCoupon',
//								name : 'radioActiveType',
//								inputValue : 1,
//								hideLabel : true,
//								checked : true,
//								boxLabel : '10元券&nbsp;&nbsp;',
//								listeners : {
//									render : function(e){
//										e.getEl().dom.parentNode.style.paddingLeft = '10px';
//										e.getEl().dom.parentNode.style.paddingTop = '5px';
//									},
//									check : function(e){
//										if(e.getValue()){
//											changeCouponModel(1);
//										}
//									}
//								}
//							}]			
//						},{
//							items : [{
//								xtype : 'radio',
//								name : 'radioActiveType',
//								inputValue : 2,
//								hideLabel : true,
//								boxLabel : '20元券&nbsp;&nbsp;',
//								listeners : {
//									render : function(e){
//										e.getEl().dom.parentNode.style.paddingLeft = '10px';
//									},						
//									check : function(e){
//										if(e.getValue()){
//										}
//									}
//								}
//							}]			
//						},{
//							items : [{
//								xtype : 'radio',
//								id : 'radioSelfCoupon',
//								name : 'radioActiveType',
//								inputValue : 3,
//								hideLabel : true,
//								boxLabel : '自定义',
//								listeners : {
//									render : function(e){
//										e.getEl().dom.parentNode.style.paddingLeft = '10px';
//									},						
//									check : function(e){
//										if(e.getValue()){
//											changeCouponModel(3);
//										}
//									}
//								}
//							}]			
//						}]
//					}]					
//				
//			},{
//				id : 'guide_2nd_couponDetail',
//				items : [{
//					id : 'centerPanel',
//					xtype : 'panel',
//					layout : 'column',
//					width : 220,
//					style : 'paddingTop:20px;',
//					defaults : {
//						layout : 'form',
//						labelWidth : 80
////						labelAlign : 'right'
//					},
//					items : [{
//						items : [{
//							id : 'guide_2nd_couponName',
//							xtype : 'textfield',
//							fieldLabel : '优惠劵名称',
//							value : '10元优惠劵',
//							allowBlank : false
//						}]
//					},{
//						items : [{
//							id : 'guide_2nd_couponPrice',
//							xtype : 'textfield',
//							value : 10,
//							fieldLabel : '&nbsp;&nbsp;&nbsp;面额',
//							allowBlank : false
//						}]
//					}, {
//						items : [{
//							id : 'endDate_date_couponExpired',
//							xtype : 'datefield',
//							width : 130,
//							fieldLabel : '&nbsp;&nbsp;&nbsp;有效期至',
//							format : 'Y-m-d',
//							readOnly : false,
//							allowBlank : false,
//							minValue : new Date(),
//							value : new Date(),
//							blankText : '日期不能为空.',
//							listeners : {
//								invalid : function(thiz){
//									thiz.clearInvalid();
//								}
//							}				
//						}]				
//					}]
//				}]					
//				
//			},{
//				id : 'active_secendStep2CouponImg',
//				items : [{
//					xtype : 'panel',
//					width : 280,
//					layout : 'column',
//					style : 'marginLeft:18px;',
//					frame : true,
//					items : [couponPicBox, form,{
//							columnWidth : 0.6,
//							items : [{
//								xtype : 'label',
//								html : '&nbsp;&nbsp;'
//							}]						
//						},{
//							items : [{
//								xtype : 'label',
//								style : 'width : 130px;',
//								html : '<sapn style="font-size:13px;color:green;font-weight:bold">图片大小不能超过100K</span>'
//							}]							
//						},btnUpload],
//					listeners : {
//		 	    		render : function(e){
//		 	    			Ext.getDom(e.getId()).setAttribute('enctype', 'multipart/form-data');
//			 	  		}
//		 	    	}	
//				}]					
//				
//			},secendStepWest]
//			},secendStepCenter]
//		}]
//	});	
	
	
	//活动树悬浮栏
	showFloatOption({treeId : 'promotionTree', option : [{name : '发布', fn : fnPublishPromotion},
	                                                     {name : '修改', fn : fnUpdatePromotion}, 
	                                                     {name : '删除', fn : fnDeletePromotion}]});
	
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
	
	$('#beginDate_date_couponExpired').parent().width($('#beginDate_date_couponExpired').width() + $('#beginDate_date_couponExpired').next().width()+20);
	$('#endDate_date_couponExpired').parent().width($('#endDate_date_couponExpired').width() + $('#endDate_date_couponExpired').next().width()+20);
	
	$('#active_dateSearchDateBegin').parent().width($('#active_dateSearchDateBegin').width() + $('#active_dateSearchDateBegin').next().width()+20);
	$('#active_dateSearchDateEnd').parent().width($('#active_dateSearchDateEnd').width() + $('#active_dateSearchDateEnd').next().width()+20);
 	$('#active_dateSearchDateCombo').parent().width($('#active_dateSearchDateCombo').width() + $('#active_dateSearchDateCombo').next().width()+20);
 	$('#active_memberCostEqual').parent().width($('#active_memberCostEqual').width() + $('#active_memberCostEqual').next().width()+20);
 	$('#active_memberAmountEqual').parent().width($('#active_memberAmountEqual').width() + $('#active_memberAmountEqual').next().width()+20);
 	
 	$('#secondStepEastBody').children().first().children().first().css('overflow-y', 'visible');
 	$('#threeStepEastBody').children().first().children().first().css('overflow-y', 'visible');
 	$('#promotionPreviewBody').children().first().children().first().css('overflow-y', 'visible');
// 	Ext.getCmp('couponConfigRules_panel_activeMgrMain').setHeight((parseInt(Ext.getDom('divActive').parentElement.style.height.replace(/px/g,'')) - Ext.getCmp('').getHeight()) - 50);
});
