function loadKitchen(){
	Ext.Ajax.request({
		url : '../../QueryKitchen.do',
		params : {
			dataSource : 'normal',
			restaurantID : restaurantID,
			isPaging : false,
			pin : pin
		},
		success : function(res, opt){
			discountData = Ext.decode(res.responseText);
		}
	});
};

function discountMgrLoad(){
	loadKitchen();
};

