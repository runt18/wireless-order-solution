function inventoryMgrOnLoad(){
	// ---------------------------------
	bindActiveEvent('supplierMgr', 
			'url(../../images/supplierMgr_select.png) no-repeat 50%',
			'url(../../images/supplierMgr.png) no-repeat 50%',
			"SupplierManagement.html?pin=" + pin + "&restaurantID=" + restaurantID
	);
	// ---------------------------------
	bindActiveEvent('inventoryBasicMgr', 
			'url(../../images/inventoryBasicMgr_select.png) no-repeat 50%',
			'url(../../images/inventoryBasicMgr.png) no-repeat 50%',
			"InventoryBasicManagement.html?pin=" + pin + "&restaurantID=" + restaurantID
	);
	// ---------------------------------
	bindActiveEvent('foodMaterialMgr', 
			'url(../../images/supplierMgr_select.png) no-repeat 50%',
			'url(../../images/supplierMgr.png) no-repeat 50%',
			"FoodMaterialManagement.html?pin=" + pin  + "&restaurantID=" + restaurantID
	);
	// ---------------------------------
	bindActiveEvent('stockMgr', 
			'url(../../images/inventoryBasicMgr_select.png) no-repeat 50%',
			'url(../../images/inventoryBasicMgr.png) no-repeat 50%',
			"StockBasicManagement.html?pin=" + pin  + "&restaurantID=" + restaurantID
	);
};