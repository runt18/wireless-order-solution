
loadKitchen = function(){
	Ext.Ajax.request({
		url : '../../QueryKitchenMgr.do',
		params : {
			restaurantID : restaurantID,
			isPaging : false,
			pin : pin
		},
		success : function(res, opt){
			discountData = Ext.util.JSON.decode(res.responseText);
		}
	});
};

discountMgrLoad = function(){
	getOperatorName(pin, '../../');
	
	loadKitchen();
	
};

