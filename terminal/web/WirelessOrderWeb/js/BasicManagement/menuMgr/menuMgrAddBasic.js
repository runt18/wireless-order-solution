
var basicOperationPanel = new Ext.Panel({
	frame : true,
	border : false,
	layout : 'fit',
	items : [{ 
		layout : 'column',
		items : [
		    {
		    	columnWidth : .35,
		 	    layout : 'column',
		 	    defaults : {
		 	    	xtype : 'panel',
	 	    	    layout : 'form',
	 	    	    labelWidth : 30
		 	    },
		 	    items : [
		 	        {
		 	    	    
		 	    	    columnWidth : 1,
		 	    	    items : [{
		 	    	    	xtype : 'textfield',
		 	    	    	id : 'txtBasicForFoodName',
		 	    	    	fieldLabel : '菜名',
		 	    	    	allowBlank : false,
		 	    	    	width : 255
		 	    	    }]
		 	    	}, {
		 	    	    columnWidth : .5,
		 	    	    items : [{
		 	    	    	xtype : 'numberfield',
		 	    	    	id : 'numBasicForFoodAliasID',
		 	    	    	fieldLabel : '编号',
		 	    	    	maxValue : 65535,
		 	    	    	minValue : 1,
		 	    	    	allowBlank : false,
		 	    	    	width : 100,
		 	    	    	validator : function(v){
		 	    	    		if(v > 0 && v <= 65535){
		 	    	    			return true;
		 	    	    		}else{
		 	    	    			return '编号需在 1  至 65535 之间!';
		 	    	    		}
		 	    	    	}
		 	    	    }]
		 	    	}, {
		 	    	    columnWidth : .5,
		 	    	    items : [{
		 	    	    	xtype : 'textfield',
		 	    	    	id : 'txtBasicForPinyin',
		 	    	    	fieldLabel : '拼音',
		 	    	    	width : 103,
		 	    	    	validator : function(v){
		 	    	    		if(/^[a-zA-Z]+$/.test(v)){
		 	    	    			return true;
		 	    	    		}else{
		 	    	    			return '只能输入拼音,不区分大小写!';
		 	    	    		}
		 	    	    	}
		 	    	    }]
		 	    	}, {
		 	    	    columnWidth : .5,
		 	    	    items : [{
		 	    	    	xtype : 'numberfield',
		 	    	    	id : 'numBasicForPrice',
		 	    	    	style : 'text-align:right;',
		 	    	    	fieldLabel : '价格',
		 	    	    	decimalPrecision : 2,
		 	    	    	allowBlank : false,
		 	    	    	maxValue : 99999.99,
		 	    	    	minValue : 0.00,
		 	    	    	width : 100,
		 	    	    	validator : function(v){
		 	    	    		if(v >= 0.00 && v <= 99999.99){
		 	    	    			return true;
		 	    	    		}else{
		 	    	    			return '价格需在 0.00  至 99999.99 之间!';
		 	    	    		}
		 	    	    	}
		 	    	    }]
		 	    	}, {
		 	    	    columnWidth : .5,
		 	    	    items : [{
		 	    	    	xtype : 'combo',
		 	    	    	id : 'cmbBasicForKitchenAlias',
		 	    	    	fieldLabel : '厨房',
		 	    	    	width : 86,
		 	    	    	listWidth : 99,
							store : new Ext.data.SimpleStore({
								fields : [ 'value', 'text' ]
							}),
							valueField : 'value',
							displayField : 'text',
							mode : 'local',
							triggerAction : 'all',
							typeAhead : true,
							selectOnFocus : true,
							forceSelection : true,
							allowBlank : true
		 	    	    }]
		 	    	}, {
		 	    		columnWidth : 1,
		 	    		items : [{
		 	    			xtype : 'textarea',
		 	    			id : 'txtBasicForDesc',
		 	    			fieldLabel : '简介',
		 	    			width : 253,
		 	    			height : 150,
		 	    			maxLength : 500,
		 	    			maxLengthText : '简介最多输入500字!可以为空!'
		 	    		}]
		 	    	}, {
		 	    		columnWidth : .13,
		 	    		items : [{html:'状态:'}]
		 	    	}, {
		 	    		columnWidth : .2,
		 	    		items : [{
		 	    			xtype : 'checkbox',
		 	    			id : 'chbForBasicSpecial',
		 	    			hideLabel : true,
		 	    			boxLabel : '<img title="特价" src="../../images/icon_tip_te.png"></img>'
		 	    		}]
		 	    	}, {
		 	    		columnWidth : .2,
		 	    		items : [{
		 	    			xtype : 'checkbox',
		 	    			id : 'chbForBasicRecommend',
		 	    			hideLabel : true,
		 	    			boxLabel : '<img title="推荐" src="../../images/icon_tip_jian.png"></img>'
		 	    		}]
		 	    	}, {
		 	    		columnWidth : .2,
		 	    		items : [{
		 	    			xtype : 'checkbox',
		 	    			id : 'chbForBasicFree',
		 	    			hideLabel : true,
		 	    			boxLabel : '<img title="赠送" src="../../images/forFree.png"></img>'
		 	    		}]
		 	    	}, {
		 	    		columnWidth : .2,
		 	    		items : [{
		 	    			xtype : 'checkbox',
		 	    			id : 'chbForBasicStop',
		 	    			hideLabel : true,
		 	    			boxLabel : '<img title="停售" src="../../images/icon_tip_ting.png"></img>'
		 	    		}]
		 	    	}, {
		 	    		columnWidth : .13,
		 	    		items : [{html:'&nbsp;&nbsp;&nbsp;&nbsp;'}]
		 	    	}, {
		 	    		columnWidth : .2,
		 	    		items : [{
		 	    			xtype : 'checkbox',
		 	    			id : 'chbForBasicCurrPrice',
		 	    			hideLabel : true,
		 	    			boxLabel : '<img title="时价" src="../../images/currPrice.png"></img>'
		 	    		}]
		 	    	}
		 	    ]
		 	 }, {
		 		 columnWidth : .65,
		 	     labelWidth : 60,
		 	     items : [
		 	         new Ext.BoxComponent({
		 	            xtype : 'box',
		 	        	id : 'foodBasicImg',
		 	        	name : 'foodBasicImg',
		 	        	width : 555,
		 	        	height : 420,
		 	        	autoEl : {
		 	        		tag : 'img',
		 	        		title : '菜品图预览.',
		 	        		style : 'filter:progid:DXImageTransform.Microsoft.AlphaImageLoader(sizingMethod=scale); width:555; height:420; cursor:hand;'
		 	        	},
		 	        	listeners : {
		 	        		render : function(){
//		 	        			Ext.getDom('foodBasicImg').ondblclick = function(){
//		 	        				Ext.getDom('txtImgFile').click();
//		 	        			};
		 	        		}
		 	        	}
		 	    	}), {
		 	        	tag : 'div',
		 	        	height : 10
		 	        }, {
		 	        	xtype : 'panel',
		 	        	layout : 'column',
		 	        	items : [
		 	        	    {
		 	        	    	xtype : 'form',
		 	        	    	layout : 'form',
		 	        	    	labelWidth : 60,
		 	        	    	columnWidth : .7,
		 	        	    	url : '../../ImageFileUpload.do',
			 	  		 	    id : 'imgFileUploadForm',
			 	  		 	    fileUpload : true,
		 	        	    	items : [
		 	        	    	    {
										xtype : 'textfield',
										id : 'txtImgFile',
										name : 'txtImgFile',
										fieldLabel : '选择图片',
										height : 22,
										inputType : 'file',
										listeners : {
											render : function(e){
												if(Ext.isIE){
													e.el.dom.size = 45;
												}else{
													e.el.dom.size = 40;
												}
												Ext.get('txtImgFile').on('change', function(){
													try{
														if(Ext.isIE){
									 						var img = Ext.getDom('foodBasicImg');
									 						var file = Ext.getDom('txtImgFile');
									 						file.select();
									 						var imgURL = document.selection.createRange().text;
									 						if(imgURL && imgURL.length > 0){
									 							var index = imgURL.lastIndexOf('.');
								 	        	    			var type = imgURL.substring(index+1, img.length);
								 	        	    			var check = false;
								 	        	    			for(var i = 0; i < imgTypeTmp.length; i++){
								 	        	    				if(type.toLowerCase() == imgTypeTmp[i].toLowerCase()){
								 	        	    					check = true;
								 	        	    					break;
								 	        	    				}
								 	        	    			}
								 	        	    			if(check){
								 	        	    				img.src = Ext.BLANK_IMAGE_URL;
								 	        	    				img.filters.item("DXImageTransform.Microsoft.AlphaImageLoader").src = imgURL;								 	        	    				
								 	        	    			}else{
								 	        	    				file.select();
								 	        	    				document.execCommand('Delete');
								 	        	    				Ext.example.msg('提示', '操作失败,选择的图片类型不正确,请重新选择!');
								 	        	    			}
									 						}
									 					}else{
									 						var img = Ext.getDom('foodBasicImg');
									 						var file = Ext.getDom('txtImgFile'); 
									 						if(file.files && file.files[0]){
									 							var index = file.value.lastIndexOf('.');
								 	        	    			var type = file.value.substring(index+1, img.length);
								 	        	    			var check = false;
								 	        	    			for(var i = 0; i < imgTypeTmp.length; i++){
								 	        	    				if(type.toLowerCase() == imgTypeTmp[i].toLowerCase()){
								 	        	    					check = true;
								 	        	    					break;
								 	        	    				}
								 	        	    			}
								 	        	    			if(check){
								 	        	    				var reader = new FileReader();
								 	        	    				reader.onload = function(evt){img.src = evt.target.result;};
								 	        	    				reader.readAsDataURL(file.files[0]);
								 	        	    			}else{
								 	        	    				file.value = '';
								 	        	    				Ext.example.msg('提示', '操作失败,选择的图片类型不正确,请重新选择!');
								 	        	    			}
									 						}
									 					}
													} catch(e){
														Ext.example.msg('提示', '操作失败,无法获取图片信息.请换浏览器后重试.');
													}
												}, this);
											}
										}
									}
		 	        	    	],
			 	    		 	listeners : {
			 	  			 		render : function(e){
			 	  			 			Ext.getDom(e.getId()).setAttribute('enctype', 'multipart/form-data');
			 	  			 		}
			 	  			 	}
		 	        	    }, {
		 	        	    	xtype : 'panel',
		 	        	    	columnWidth : .15,
		 	        	    	items : [
		 	        	    	    {
		 	        	    	    	xtype : 'button',
		 	        	    	    	id : 'btnUploadFoodImage',
		 	        	    	    	text : '上传图片',
				 	        	    	handler : function(e){
				 	        	    		
				 	        	    		var check = true;
				 	        	    		var img = '';
				 	        	    		if(Ext.isIE){
				 	        	    			var file = Ext.getDom('txtImgFile');
				 	        	    			file.select();
				 	        	    			img = document.selection.createRange().text;
				 	        	    		}else{
				 	        	    			var file = Ext.getDom('txtImgFile'); 
				 	        	    			img = file.value;
				 	        	    		}
				 	        	    		
				 	        	    		if(typeof(img) != 'undefined' && img.length > 0){
				 	        	    			var index = img.lastIndexOf('.');
				 	        	    			var type = img.substring(index+1, img.length);
				 	        	    			check = false;
				 	        	    			for(var i = 0; i < imgTypeTmp.length; i++){
				 	        	    				if(type.toLowerCase() == imgTypeTmp[i].toLowerCase()){
				 	        	    					check = true;
				 	        	    					break;
				 	        	    				}
				 	        	    			}
				 	        	    		}else{
				 	        	    			check = false;
				 	        	    		}
				 	        	    		
				 	        	    		if(check){
				 	        	    			var selData = Ext.ux.getSelData('menuMgrGrid');
				 	        	    			selData.arrt = {
				 	        	    				type : mmObj.operation.img.upload
				 	        	    			};
				 	        	    			uploadFoodImage(selData);
				 	        	    		}else{
				 	        	    			Ext.example.msg('提示', '上传图片失败,未选择图片或图片类型正确.');
				 	        	    		}
				 	        	    	}
		 	        	    	    }
		 	        	    	]
		 	        	    }, {
		 	        	    	xtype : 'panel',
		 	        	    	columnWidth : .15,
		 	        	    	items : [{
		 	        	    		xtype : 'button',
		 	        	    		id : 'btnDeleteFoodImage',
		 	        	    		text : '删除图片',
		 	        	    		handler : function(e){
		 	        	    			var selData = Ext.ux.getSelData('menuMgrGrid');
		 	        	    			if(!selData)
		 	        	    				return;
		 	        	    			
		 	        	    			if(selData.img.indexOf('nophoto.jpg') != -1){
		 	        	    				Ext.example.msg('提示', '该菜品没有图片,无需删除.');
		 	        	    				return;
		 	        	    			}
		 	        	    			
		 	        	    			Ext.Msg.confirm('提示', '是否确定删除菜品图片?', function(e){
			 	       						if(e == 'yes'){
			 	       							selData.arrt = {
			 	       								type : mmObj.operation.img.del
			 	       							};
				 	       						uploadFoodImage(selData);
			 	       						}
			 	       					}, this);
		 	        	    		}
		 	        	    	}]
		 	        	    }
		 	        	]
		 	        }
		 	    ]
		 	}
		]
	}]
});

/**
 * 
 */
resetbBasicOperation = function(_d){
	var foodName = Ext.getCmp('txtBasicForFoodName');
	var foodAliasID = Ext.getCmp('numBasicForFoodAliasID');
	var foodPinyin = Ext.getCmp('txtBasicForPinyin');
	var foodPrice = Ext.getCmp('numBasicForPrice');
	var foodKitchenAlias = Ext.getCmp('cmbBasicForKitchenAlias');
	var foodDesc = Ext.getCmp('txtBasicForDesc');
	var isSpecial = Ext.getCmp('chbForBasicSpecial');
	var isRecommend = Ext.getCmp('chbForBasicRecommend');
	var isFree = Ext.getCmp('chbForBasicFree');
	var isStop = Ext.getCmp('chbForBasicStop');
	var isCurrPrice = Ext.getCmp('chbForBasicCurrPrice');
	var img = Ext.getDom('foodBasicImg');
	var btnUploadFoodImage = Ext.getCmp('btnUploadFoodImage');
	var btnDeleteFoodImage = Ext.getCmp('btnDeleteFoodImage');
	var imgFile = Ext.getCmp('txtImgFile');
	var data = {};
	
	// 清空图片信息
	refreshFoodImageMsg();
	
	if(_d != null && typeof(_d) != 'undefined'){
		data = _d;
		foodAliasID.setDisabled(true);
		btnUploadFoodImage.setDisabled(false);
		btnDeleteFoodImage.setDisabled(false);
		imgFile.setDisabled(false);
	}else{
		data = {};
		foodAliasID.setDisabled(false);
		btnUploadFoodImage.setDisabled(true);
		btnDeleteFoodImage.setDisabled(true);
		imgFile.setDisabled(true);
	}
	
	if(foodKitchenAlias.store.getCount() == 0){
		foodKitchenAlias.store.loadData(kitchenTypeData);
	}
	
	foodName.setValue(typeof(data.foodName) == 'undefined' ? '' : data.foodName);
	foodAliasID.setValue(typeof(data.foodAliasID) == 'undefined' ? '' : data.foodAliasID);
	foodPinyin.setValue(typeof(data.pinyin) == 'undefined' ? '' : data.pinyin);
	foodPrice.setValue(typeof(data.unitPrice) == 'undefined' ? '' : data.unitPrice);
	foodKitchenAlias.setValue(typeof(data.kitchenAliasID) == 'undefined' ? kitchenTypeData[0][0] : data.kitchenAliasID);
	foodDesc.setValue(typeof(data.desc) == 'undefined' ? '' : data.desc);
	isSpecial.setValue(typeof(data.special) == 'undefined' ? false : eval(data.special));
	isRecommend.setValue(typeof(data.recommend) == 'undefined' ? false : eval(data.recommend));
	isFree.setValue(typeof(data.gift) == 'undefined' ? false : eval(data.gift));
	isStop.setValue(typeof(data.stop) == 'undefined' ? false : eval(data.stop));
	isCurrPrice.setValue(typeof(data.currPrice) == 'undefined' ? false : eval(data.currPrice));
	img.src = typeof(data.img) == 'undefined' || data.img == '' ? '../../images/nophoto.jpg' : data.img;
	
	foodName.clearInvalid();
	foodAliasID.clearInvalid();
	foodPinyin.clearInvalid();
	foodPrice.clearInvalid();
	
};

/**
 * 添加菜品基础信息
 */
addBasicHandler = function(){
	
	basicOperationBasicHandler({
		type : mmObj.operation.insert
	});
	
};

/**
 * 修改菜品基础信息
 */
updateBasicHandler = function(c){
	c.type = mmObj.operation.update;
	basicOperationBasicHandler(c);
};

/**
 * 操作菜品基础信息,目前支持添加和修改操作
 */
basicOperationBasicHandler = function(c){
	
	if(c == null || typeof c == undefined || typeof c.type == undefined){
		return;
	}
	
	var actionURL = '';
	if(c.type == mmObj.operation.insert){
		actionURL = '../../InsertMenu.do';
	}else if(c.type == mmObj.operation.update){
		actionURL = '../../UpdateMenu.do';
	}else{
		return;
	}
	
	var foodName = Ext.getCmp('txtBasicForFoodName');
	var foodAliasID = Ext.getCmp('numBasicForFoodAliasID');
	var foodPinyin = Ext.getCmp('txtBasicForPinyin');
	var foodPrice = Ext.getCmp('numBasicForPrice');
	var foodKitchenAlias = Ext.getCmp('cmbBasicForKitchenAlias');
	var foodDesc = Ext.getCmp('txtBasicForDesc');
	var isSpecial = Ext.getCmp('chbForBasicSpecial');
	var isRecommend = Ext.getCmp('chbForBasicRecommend');
	var isFree = Ext.getCmp('chbForBasicFree');
	var isStop = Ext.getCmp('chbForBasicStop');
	var isCurrPrice = Ext.getCmp('chbForBasicCurrPrice');
	var isCombination = false;
	var comboContent = '';
	var kitchenID = '';
	var cfg = Ext.getCmp('combinationFoodGrid');
		
	if(!foodName.isValid() || !foodPinyin.isValid() || !foodAliasID.isValid() || !foodPrice.isValid() || !foodKitchenAlias.isValid()){
		Ext.getCmp('foodOperationWinTab').setActiveTab('basicOperationTab');
		return;
	}
	
	if(c.type == mmObj.operation.insert){
		if(cfg.getStore().getCount() > 0){
			isCombination = true;
			for(var i = 0; i < cfg.getStore().getCount(); i++){
				comboContent += (i > 0 ? '<split>' : '');
				comboContent += (cfg.getStore().getAt(i).get('foodID') + ',' + cfg.getStore().getAt(i).get('amount'));
			}
		}
	}else if(c.type == mmObj.operation.update){
		isCombination = (typeof(c.data) != 'undefined' && typeof(c.data.combination) != 'undefined' ? c.data.combination : false);
	}
	
	for(var i = 0; i < kitchenTypeData.length; i++){
		if(kitchenTypeData[i][0] == foodKitchenAlias.getValue()){
			kitchenID = kitchenTypeData[i][2];
			break;
		}
	}
	
	if(c.type == mmObj.operation.insert){
		Ext.getCmp('btnAddForOW').setDisabled(true);
		Ext.getCmp('btnCloseForOW').setDisabled(true);
		Ext.getCmp('btnRefreshForOW').setDisabled(true);
	}else if(c.type == mmObj.operation.update){
		setButtonStateOne(true);
	}
	
	Ext.Ajax.request({
		url : actionURL,
		params : {
			pin : pin,
			restaurantID : restaurantID,
			foodID : (typeof(c.data) != 'undefined' && typeof(c.data.foodID) != 'undefined' ? c.data.foodID : 0),
			foodName : foodName.getValue().trim(),
			foodAliasID : foodAliasID.getValue(),
			foodPinyin : foodPinyin.getValue(),
			foodPrice : foodPrice.getValue(),
			kitchenAliasID : foodKitchenAlias.getValue(),
			kitchenID : kitchenID,
			foodDesc : foodDesc.getValue().trim(),
			isSpecial : isSpecial.getValue(),
			isRecommend : isRecommend.getValue(),
			isFree : isFree.getValue(),
			isStop : isStop.getValue(),
			isCurrPrice : isCurrPrice.getValue(),
			isCombination : isCombination,
			comboContent : comboContent
		},
		success : function(res, opt){
			var jr = Ext.util.JSON.decode(res.responseText);
			
			if(jr.success == true){
				if(c.type == mmObj.operation.insert){
					Ext.Msg.confirm(jr.titile, jr.msg + '\n是否继续添加?', function(e){
						if(e == 'yes'){
							resetbBasicOperation();
							Ext.getCmp('combinationFoodGrid').getStore().removeAll();
							Ext.getCmp('txtMiniAllFoodNameSearch').setValue('');
							Ext.getCmp('btnSearchForAllFoodMiniGridTbar').handler();
						}else{
							Ext.getCmp('foodOperationWin').hide();
						}
					}, this);
				}else if(c.type == mmObj.operation.update){
					Ext.example.msg(jr.title, jr.msg);
					foodName.setValue(foodName.getValue().trim());
					foodDesc.setValue(foodDesc.getValue().trim());
					Ext.getCmp('menuMgrGrid').getStore().each(function(record){
						if(record.get('foodID') == c.data.foodID){
							record.set('foodName', foodName.getValue().trim());
							record.set('pinyin', foodPinyin.getValue());
							record.set('unitPrice', foodPrice.getValue());
							record.set('kitchenID', kitchenID);
							record.set('kitchenName', foodKitchenAlias.getRawValue());
							record.set('kitchenAliasID', foodKitchenAlias.getValue());
							record.set('desc', foodDesc.getValue().trim());
							record.set('special', isSpecial.getValue());
							record.set('recommend', isRecommend.getValue());
							record.set('gift', isFree.getValue());
							record.set('stop', isStop.getValue());
							record.set('currPrice', isCurrPrice.getValue());
							Ext.ux.formatFoodName(record);
							record.commit();
							return;
						}
					});
					if(c.hide == true){
						Ext.getCmp('foodOperationWin').hide();
					}
				}
			}else{
				Ext.ux.showMsg(jr);
			}
			
			if(c.type == mmObj.operation.insert){
				Ext.getCmp('btnAddForOW').setDisabled(false);
				Ext.getCmp('btnCloseForOW').setDisabled(false);
				Ext.getCmp('btnRefreshForOW').setDisabled(false);
			}else if(c.type == mmObj.operation.update){
				setButtonStateOne(false);
			}
			
		},
		failure : function(res, opt) {
			Ext.ux.showMsg(Ext.util.JSON.decode(res.responseText));
			if(c.type == mmObj.operation.insert){
				Ext.getCmp('btnAddForOW').setDisabled(false);
				Ext.getCmp('btnCloseForOW').setDisabled(false);
				Ext.getCmp('btnRefreshForOW').setDisabled(false);
			}else if(c.type == mmObj.operation.update){
				setButtonStateOne(false);
			}
		}
	});
	
};

/**
 * 图片操作
 */
uploadFoodImage = function(c){
	var otype = null;
	if(typeof(c.arrt) == 'undefined' || typeof(c.arrt.type) == 'undefined'){
		Ext.example.msg('提示', '操作失败,获取图片操作类型失败,请联系客服人员.');
		return;
	}
	if(c.arrt.type == mmObj.operation.img.upload){
		otype = 0;
	}else if(c.arrt.type == mmObj.operation.img.del){
		otype = 1;
	}else{
		Ext.example.msg('提示', '操作失败,获取图片操作类型失败,请联系客服人员.');
		return;
	}
	
	var btnUploadFoodImage = Ext.getCmp('btnUploadFoodImage');
	var btnDeleteFoodImage = Ext.getCmp('btnDeleteFoodImage');
	
	btnUploadFoodImage.setDisabled(true);
	btnDeleteFoodImage.setDisabled(true);
	setButtonStateOne(true);
	Ext.getCmp('imgFileUploadForm').getForm().submit({
		url : '../../ImageFileUpload.do?restaurantID=' + c.restaurantID + '&foodID=' + c.foodID + '&otype=' + otype + '&time=' + new Date(), 
		success : function(thiz, result){
			var jr = Ext.util.JSON.decode(result.response.responseText);
			if(eval(jr.success)){
				Ext.example.msg(jr.title, jr.msg);
				Ext.getCmp('menuMgrGrid').getStore().each(function(record){
					if(record.get('foodID') == c.foodID){
						record.set('img', jr.root[0].img);
						record.commit();
						return;
					}
				});
				
				if(typeof(c.arrt) != 'undefined' && typeof(c.arrt.type) != 'undefined' && c.arrt.type == mmObj.operation.img.del)
					refreshFoodImageMsg();
				
			}else{
				Ext.ux.showMsg(jr);
			}
			btnUploadFoodImage.setDisabled(false);
			btnDeleteFoodImage.setDisabled(false);
			setButtonStateOne(false);
		},
		failure : function(thiz, result){
			btnUploadFoodImage.setDisabled(false);
			btnDeleteFoodImage.setDisabled(false);
			setButtonStateOne(false);
			Ext.ux.showMsg(Ext.util.JSON.decode(result.response.responseText));
		}
	});
};

/**
 * 
 */
refreshFoodImageMsg = function(){
	var img = Ext.getDom('foodBasicImg');
	var file = Ext.getDom('txtImgFile');
	
	img.src = '../../images/nophoto.jpg';
	if(Ext.isIE){
		file.select();
		document.execCommand('Delete');		 	        	    				
	}else{
		file.value = '';
	}
};