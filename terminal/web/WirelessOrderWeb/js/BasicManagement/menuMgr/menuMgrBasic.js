var basicOperationPanel = new Ext.Panel({
	frame : true,
	border : false,
	layout : 'fit',
	items : [{
		layout : 'column',
		items : [{
			columnWidth : .35,
		 	layout : 'column',
		 	defaults : {
		 		xtype : 'panel',
		 		layout : 'form',
		 		labelWidth : 30
		 	},
		 	items : [{
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
		 	    		if(v > 0 && v <= 65535 && v.indexOf('.') == -1){
		 	    	    	return true;
		 	    	    }else{
		 	    	    	return '编号需在 1  至 65535 之间,且为整数!';
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
		 	    	store : new Ext.data.JsonStore({
						fields : [ 'aliasId', 'name' ]
					}),
					valueField : 'aliasId',
					displayField : 'name',
					mode : 'local',
					triggerAction : 'all',
					typeAhead : true,
					selectOnFocus : true,
					forceSelection : true,
					allowBlank : false,
					readOnly : true
		 	    }]
		 	}, {
		 		columnWidth : 1,
		 		xtype : 'panel',
		 		id : 'txtFoodPriceExplain',
		 		height : 20,
		 		html : '<a href="javascript:fppOperation()">说明:此价格对应当前活动的价格方案,点击查看其他方案</a>'
		 	}, {
		 		columnWidth : 1,
		 	    items : [{
		 	    	xtype : 'textarea',
		 	    	id : 'txtBasicForDesc',
		 	    	fieldLabel : '简介',
		 	    	width : 253,
		 	    	height : 150,
		 	    	maxLength : 500,
		 	    	maxLengthText : '简介最多输入500字,可以为空.'
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
		 	}, {
		 		columnWidth : .2,
		 	    items : [{
		 	    	xtype : 'checkbox',
		 	    	id : 'chbForBasicHot',
		 	    	hideLabel : true,
		 	    	boxLabel : '<img title="热销" src="../../images/hot.png"></img>'
		 	    }]
		 	}, {
		 		columnWidth : .2,
		 	    items : [{
		 	    	xtype : 'checkbox',
		 	    	id : 'chbForBasicWeight',
		 	    	hideLabel : true,
		 	    	boxLabel : '<img title="热销" src="../../images/weight.png"></img>'
		 	    }]
		 	}, {
		 		columnWidth : .5,
		 		labelWidth : 55,
		 		items : [{
		 	    	xtype : 'combo',
		 	    	id : 'comboBasicForStockStatus',
		 	    	fieldLabel : '库存管理',
		 	    	width : 66,
		 	    	listWidth : Ext.isIE ? 79 : 83,
		 	    	store : new Ext.data.SimpleStore({
						fields : ['value', 'text'],
						data : stockStatusData
					}),
					valueField : 'value',
					displayField : 'text',
					mode : 'local',
					triggerAction : 'all',
					typeAhead : true,
					selectOnFocus : true,
					forceSelection : true,
					allowBlank : false,
					readOnly : true
		 	    }]
		 	}]
		}, {
			columnWidth : .65,
			labelWidth : 60,
			items : [ new Ext.BoxComponent({
				xtype : 'box',
		 	    id : 'foodBasicImg',
		 	    name : 'foodBasicImg',
		 	    width : 555,
		 	    height : 420,
		 	    autoEl : {
		 	    	tag : 'img',
		 	    	title : '菜品图预览.',
		 	    	style : 'filter:progid:DXImageTransform.Microsoft.AlphaImageLoader(sizingMethod=scale); width:555; height:420; cursor:hand;'
		 	    }
			}), {
				tag : 'div',
		 	    height : 10
		 	}, {
		 		xtype : 'panel',
		 	    layout : 'column',
		 	    items : [{
		 	    	xtype : 'form',
		 	    	layout : 'form',
		 	    	labelWidth : 60,
		 	    	columnWidth : .7,
		 	    	url : '../../ImageFileUpload.do',
		 	    	id : 'imgFileUploadForm',
		 	    	fileUpload : true,
		 	    	items : [{}],
		 	    	listeners : {
		 	    		render : function(e){
		 	    			Ext.getDom(e.getId()).setAttribute('enctype', 'multipart/form-data');
			 	  		}
		 	    	}
		 	    }, {
		 	    	xtype : 'panel',
		 	    	columnWidth : .15,
		 	    	items : [{
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
		 	    	 }]
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
		 		}]
			}]
		}]
	}]
});

/**
 * 界面赋值
 */
function resetbBasicOperation(_d){
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
	var isHot = Ext.getCmp('chbForBasicHot');
	var isWeight = Ext.getCmp('chbForBasicWeight');
	var img = Ext.getDom('foodBasicImg');
	var btnUploadFoodImage = Ext.getCmp('btnUploadFoodImage');
	var btnDeleteFoodImage = Ext.getCmp('btnDeleteFoodImage');
	var stockStatus = Ext.getCmp('comboBasicForStockStatus');
	
	var data = {};
	
	// 清空图片信息
	refreshFoodImageMsg();
	
	var imgFile = Ext.getCmp('txtImgFile');
	
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
		foodKitchenAlias.store.loadData(kitchenData);
	}
	
	var status = typeof(data.status) == 'undefined' ? 0 : parseInt(data.status);
	
	foodName.setValue(data.name);
	foodAliasID.setValue(data.alias);
	foodPinyin.setValue(data.pinyin);
	foodPrice.setValue(data.unitPrice);
	foodKitchenAlias.setValue(data['kitchen.alias']);
	foodDesc.setValue(data.desc);
	isSpecial.setValue(Ext.ux.cfs.isSpecial(status));
	isRecommend.setValue(Ext.ux.cfs.isRecommend(status));
	isStop.setValue(Ext.ux.cfs.isStop(status));
	isFree.setValue(Ext.ux.cfs.isGift(status));
	isCurrPrice.setValue(Ext.ux.cfs.isCurrPrice(status));
	isHot.setValue(Ext.ux.cfs.isHot(status));
	isWeight.setValue(Ext.ux.cfs.isWeigh(status));
	img.src = typeof(data.img) == 'undefined' || data.img == '' ? '../../images/nophoto.jpg' : data.img;
	stockStatus.setValue(typeof(data.stockStatusValue) == 'undefined' ? 1 : data.stockStatusValue);
	
	foodName.clearInvalid();
	foodAliasID.clearInvalid();
	foodPinyin.clearInvalid();
	foodPrice.clearInvalid();
	stockStatus.clearInvalid();
	foodKitchenAlias.clearInvalid();
};

/**
 * 添加菜品基础信息
 */
function addBasicHandler(){
	basicOperationBasicHandler({
		type : mmObj.operation.insert
	});
};

/**
 * 修改菜品基础信息
 */
function updateBasicHandler(c){
	c.type = mmObj.operation.update;
	basicOperationBasicHandler(c);
};

/**
 * 操作菜品基础信息,目前支持添加和修改操作
 */
function basicOperationBasicHandler(c){
	
	if(c == null || typeof(c) == 'undefined' || typeof(c.type) == 'undefined'){
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
	var isHot = Ext.getCmp('chbForBasicHot');
	var isWeight = Ext.getCmp('chbForBasicWeight');
	var isCombination = false;
	var comboContent = '';
	var kitchenID = '';
	var cfg = Ext.getCmp('combinationFoodGrid');
	var stockStatus = Ext.getCmp('comboBasicForStockStatus');
		
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
	
	for(var i = 0; i < kitchenData.length; i++){
		if(kitchenData[i].alias == foodKitchenAlias.getValue()){
			kitchenID = kitchenData[i].id;
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
			foodID : (typeof(c.data) != 'undefined' && typeof(c.data.id) != 'undefined' ? c.data.id : 0),
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
			isHot : isHot.getValue(),
			isCombination : isCombination,
			isWeight : isWeight.getValue(),
			comboContent : comboContent,
			stockStatus : stockStatus.getValue()
		},
		success : function(res, opt){
			var jr = Ext.util.JSON.decode(res.responseText);
			
			if(jr.success == true){
				if(c.type == mmObj.operation.insert){
					Ext.Msg.confirm(jr.titile, jr.msg + '\n是否继续添加?', function(e){
						if(e == 'yes'){
							var faid = foodAliasID.getValue();
							var kaid = foodKitchenAlias.getValue();	
							
							resetbBasicOperation();
							Ext.getCmp('combinationFoodGrid').getStore().removeAll();
							Ext.getCmp('txtMiniAllFoodNameSearch').setValue('');
							Ext.getCmp('btnSearchForAllFoodMiniGridTbar').handler();
							
							foodAliasID.setValue(parseInt(faid+1));
							foodKitchenAlias.setValue(kaid);
						}else{
							Ext.getCmp('foodOperationWin').hide();
						}
					}, this);
				}else if(c.type == mmObj.operation.update){
					if(c.hide == true){
						Ext.getCmp('foodOperationWin').hide();
					}
					Ext.example.msg(jr.title, jr.msg);
					foodName.setValue(foodName.getValue().trim());
					foodDesc.setValue(foodDesc.getValue().trim());
					Ext.getCmp('menuMgrGrid').getStore().each(function(record){
						if(record.get('id') == c.data.foodID){
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
							record.set('hot', isHot.getValue());
							record.set('weight', isWeight.getValue());
							Ext.ux.formatFoodName(record, 'displayFoodName', 'foodName');
							record.commit();
							return;
						}
					});
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
function uploadFoodImage(c){
	var otype = null;
	if(typeof(c.arrt) == 'undefined' || typeof(c.arrt.type) == 'undefined'){
		Ext.example.msg('提示', '操作失败, 获取图片操作类型失败, 请联系客服人员.');
		return;
	}
	if(c.arrt.type == mmObj.operation.img.upload){
		otype = 0;
	}else if(c.arrt.type == mmObj.operation.img.del){
		otype = 1;
	}else{
		Ext.example.msg('提示', '操作失败, 获取图片操作类型失败, 请联系客服人员.');
		return;
	}
	
	var btnUploadFoodImage = Ext.getCmp('btnUploadFoodImage');
	var btnDeleteFoodImage = Ext.getCmp('btnDeleteFoodImage');
	
	btnUploadFoodImage.setDisabled(true);
	btnDeleteFoodImage.setDisabled(true);
	setButtonStateOne(true);
	
	if(!foodImageUpdateLoaddingMask){
		foodImageUpdateLoaddingMask = new Ext.LoadMask(document.body, {
			msg  : '图片操作中，请稍候......'
		});
	}
	foodImageUpdateLoaddingMask.show();		
	
	Ext.getCmp('imgFileUploadForm').getForm().submit({
		url : '../../ImageFileUpload.do?restaurantID=' + c.restaurantID + '&foodID=' + c.foodID + '&otype=' + otype + '&time=' + new Date(), 
		success : function(thiz, result){
			foodImageUpdateLoaddingMask.hide();
			
			var jr = Ext.decode(result.response.responseText);
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
			foodImageUpdateLoaddingMask.hide();
			btnUploadFoodImage.setDisabled(false);
			btnDeleteFoodImage.setDisabled(false);
			setButtonStateOne(false);
			Ext.ux.showMsg(Ext.decode(result.response.responseText));
		}
	});
};

/**
 * 
 */
function refreshFoodImageMsg(){
	var img = Ext.getDom('foodBasicImg');
	var imgFile = Ext.getCmp('txtImgFile');
	var imgForm = Ext.getCmp('imgFileUploadForm');
	img.src = '../../images/nophoto.jpg';
	if(imgFile){
		imgForm.remove('txtImgFile');
	}
	imgFile = getImageFile();
	imgForm.add(imgFile);
	imgForm.doLayout(true);
};

/**
 * 
 */
function getImageFile(){
	var img = new Ext.form.TextField({
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
	});
	return img;
};

//------------------------------------------------------------------
function operationFoodPricePlanData(c){
	if(c == null || c.type == null || typeof c.type == 'undefined')
		return;
	var data = {};
	var foodID = Ext.getCmp('hideFoodPricePlanID');
	var pricePlanID = Ext.getCmp('hidePricePlanID');
	var pricePlanName = Ext.getCmp('txtFoodPricePlanName');
	var unitPrice = Ext.getCmp('numFoodPricePlanUnitPrice');
	if(c.type == mmObj.operation['set']){
		data = c.data == null || typeof c.data == 'undefined' ? {} : c.data;
		foodID.setValue(data['foodId']);
		pricePlanID.setValue(data['planId']);
		pricePlanName.setValue(data['pricePlan.name']);
		unitPrice.setValue(data['unitPrice']);
	}else if(c.type == mmObj.operation['get']){
		data = {
			restaurantID : restaurantID,
			planId : pricePlanID.getValue(),
			foodId : foodID.getValue(),
			unitPrice : unitPrice.getValue()
		};
		c.data = data;
	}
	unitPrice.clearInvalid();
	return c;
};

function fppGridUpdateHandler(){
	var sd = Ext.ux.getSelData('fppGrid');
	if(!sd){
		Ext.example.msg('提示', '请选择一个方案价格再进行操作.');
		return;
	}
	Ext.getCmp('fppOperationPanel').show();
	foodPricePlanWin.doLayout();
	operationFoodPricePlanData({
		type : mmObj.operation['set'],
		data : sd
	});
};

function fppGridOperationRenderer(){
	return '<a href="javascript:fppGridUpdateHandler()">修改</>';
};

function fppOperation(){
	if(!foodPricePlanWin){
		var fppGridTbar = new Ext.Toolbar({
			height : 26,
			items : ['->', {
				text : '刷新',
				id : 'btnRefreshFPPGrid',
				iconCls : 'btn_refresh',
				handler : function(){
					var gs = fppGrid.getStore();
					gs.removeAll();
					
					oPanel.hide();
					foodPricePlanWin.doLayout();
					
					gs.baseParams['searchValue'] = foodPricePlanWin.foodData['alias'];
					gs.load();
				}
			}, {
				text : '修改',
				iconCls : 'btn_edit',
				handler : function(){
					fppGridUpdateHandler();
				}
			}]
		});
		fppGrid = createGridPanel(
			'fppGrid',
			'',
			'',
			'',
			'../../QueryFoodPricePlan.do',
			[
				[true, false, false, false], 
				['方案', 'pricePlan.name'],
				['价格', 'unitPrice', 60, 'right', 'Ext.ux.txtFormat.gridDou'],
				['操作', 'operation', 80, 'center', 'fppGridOperationRenderer']
			],
			FoodPricePlan.getKeys(),
			[['restaurantID', restaurantID], ['searchType', 1], ['searchValue', 0]],
			0,
			'',
			fppGridTbar
		);
		fppGrid.region = 'center';
		
		oPanel = new Ext.Panel({
			id : 'fppOperationPanel',
			frame : true,
			region : 'south',
			layout : 'column',
			autoHeight : true,
			defaults : {
				xtype : 'form',
				layout : 'form',
				labelWidth : 35
			},
			items : [{
				xtype : 'hidden',
				id : 'hidePricePlanID'
			}, {
				xtype : 'hidden',
				id : 'hideFoodPricePlanID'
			}, {
				columnWidth : .5,
				items : [{
					xtype : 'textfield',
					id : 'txtFoodPricePlanName',
					fieldLabel : '方案',
					width : 100,
					disabled : true
				}]
			}, {
				columnWidth : .5,
				items : [{
					xtype : 'numberfield',
					id : 'numFoodPricePlanUnitPrice',
					fieldLabel : '价格',
					width : 100,
					allowBlank : false,
					blankText : '价格不能为空.',
					validator : function(v){
						if(v >= 0 && v < 65535){
							return true;
						}else{
							return '价格不能为空.';
						}
					}
				}]
			}],
			buttonAlign : 'center',
			buttons : [{
				text : '保存',
				handler : function(){
					var unitPrice = Ext.getCmp('numFoodPricePlanUnitPrice');
					if(!unitPrice.isValid()){
						return;
					}
					var foodPricePlan = operationFoodPricePlanData({
						type : mmObj.operation['get']
					}).data;
					
					Ext.Ajax.request({
						url : '../../UpdateFoodPricePlan.do',
						params : {
							foodPricePlan : Ext.encode(foodPricePlan)
						},
						success : function(res, opt){
							var jr = Ext.util.JSON.decode(res.responseText);
							if(jr.success){
								Ext.example.msg(jr.title, jr.msg);
								Ext.getCmp('btnRefreshFPPGrid').handler();
							}else{
								Ext.ux.showMsg(jr);
							}
						},
						failure : function(res, opt){
							Ext.ux.showMsg(Ext.decode(res.responseText));
						}
					});
				}
			}, {
				text : '取消',
				id : 'btnCloseFoodPricePlanPanel',
				handler : function(){
					oPanel.hide();
					foodPricePlanWin.doLayout();
				}
			}]
		});
		
		foodPricePlanWin = new Ext.Window({
			title : '&nbsp;',
			modal : true,
			resizable : false,
			closable : false,
			draggable : false,
			width : 350,
			height : 390,
			layout : 'border',
			items : [fppGrid, oPanel],
			bbar : ['->', {
				text : '关闭',
				iconCls : 'btn_close',
				handler : function(){
					foodPricePlanWin.hide();
				}
			}],
			keys : [{
				key : Ext.EventObject.ESC,
				scope : this,
				fn : function(){
					foodPricePlanWin.hide();
				}
			}],
			listeners : {
				beforeshow : function(){
					oPanel.hide();
				},
				show : function(){
					Ext.getCmp('btnRefreshFPPGrid').handler();
				},
				hide : function(){
					fppGrid.getStore().removeAll();
				}
			}
		});
	}
	var sd = Ext.ux.getSelData(menuGrid);
	foodPricePlanWin.foodData = sd;
	foodPricePlanWin.setTitle(sd['name']+' -- 所有价格方案信息');
	foodPricePlanWin.show();
	
};