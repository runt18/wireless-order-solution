
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
		 	 },  {
		 	    columnWidth : .65,
		 	    layout : 'form',
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
		 	        		src : '../../images/nophoto.jpg',
		 	        		style : 'filter:progid:DXImageTransform.Microsoft.AlphaImageLoader(sizingMethod=scale);'
		 	        	}
		 	    	}),
		 	        {
		 	        	tag : 'div',
		 	        	height : 5
		 	        }, {
		 	    		xtype : 'textfield',
		 	    		id : 'txtImgFile',
		 	    		name : 'txtImgFile',
		 	    		fieldLabel : '选择图片',
		 	    		width : 430,
		 	    		height : 25,
		 	    		inputType : 'file',
		 	    		listeners : {
		 	    			render : function(e){
		 	    				Ext.get('txtImgFile').on('change', function(){
		 	    					if(Ext.isIE){
		 	    						var img = Ext.get('foodBasicImg').dom;
		 	    						var file = document.getElementById('txtImgFile');
		 	    						file.select();
		 	    						img.src = Ext.BLANK_IMAGE_URL;
		 	    						img.filters.item("DXImageTransform.Microsoft.AlphaImageLoader").src = document.selection.createRange().text;
		 	    					}else{
		 	    						var img = document.getElementById('foodBasicImg');
		 	    						var file = document.getElementById('txtImgFile'); 
		 	    						if(file.files && file.files[0]){
		 	    							var reader = new FileReader();
		 	    							reader.onload = function(evt){img.src = evt.target.result;};
		 	    							reader.readAsDataURL(file.files[0]);
		 	    						}
		 	    					}
		 	    				}, this);
		 	    			}
		 	    		}
		 	    	}
		 	    ]
		 	}
		]
	}]
});


function getPath(obj){
	if(obj){  
		if (window.navigator.userAgent.indexOf("MSIE")>=1)  {  
			obj.select();  
			return document.selection.createRange().text;  
		}else if(window.navigator.userAgent.indexOf("Firefox")>=1){  
//			if(obj.files){
//				return obj.files.item(0).getAsDataURL();
//			}  
//			return obj.value;
			
		}  
		return obj.value;  
    }  
} 

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
	var data = {};
	
	if(_d != null && typeof(_d) != 'undefined'){
		data = _d;
		foodAliasID.setDisabled(true);
	}else{
		data = {};
		foodAliasID.setDisabled(false);
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
//	Ext.example.msg('提示','修改菜品基础信息!');
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
