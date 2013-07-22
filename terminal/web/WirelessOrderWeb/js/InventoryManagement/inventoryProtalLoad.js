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
			'url(../../images/foodMaterialMgr_select.png) no-repeat 50%',
			'url(../../images/foodMaterialMgr.png) no-repeat 50%',
			"FoodMaterialManagement.html?pin=" + pin  + "&restaurantID=" + restaurantID
	);
	// ---------------------------------
	bindActiveEvent('stockActionMgr', 
			'url(../../images/stockActionMgr_select.png) no-repeat 50%',
			'url(../../images/stockActionMgr.png) no-repeat 50%',
			"StockActionManagement.html?pin=" + pin  + "&restaurantID=" + restaurantID
	);
	// ---------------------------------
	bindActiveEvent('stockTakeMgr', 
			'url(../../images/stockTakeMgr_select.png) no-repeat 50%',
			'url(../../images/stockTakeMgr.png) no-repeat 50%',
			"StockTakeManagement.html?pin=" + pin  + "&restaurantID=" + restaurantID
	);
	// ---------------------------------
	bindActiveEvent('stockReport', 
			'url(../../images/stockReport_select.png) no-repeat 50%',
			'url(../../images/stockReport.png) no-repeat 50%',
			"StockReport.html?pin=" + pin  + "&restaurantID=" + restaurantID
	);
	// ---------------------------------
	bindActiveEvent('stockDetailReport', 
			'url(../../images/stockDetailReport_select.png) no-repeat 50%',
			'url(../../images/stockDetailReport.png) no-repeat 50%',
			"StockDetailReport.html?pin=" + pin  + "&restaurantID=" + restaurantID
	);
	// ---------------------------------
	bindActiveEvent('stockDistributionReport', 
			'url(../../images/stockDistributionReport_select.png) no-repeat 50%',
			'url(../../images/stockDistributionReport.png) no-repeat 50%',
			"StockDistributionReport.html?pin=" + pin  + "&restaurantID=" + restaurantID
	);
	// ---------------------------------
	bindActiveEvent('deltaReport', 
			'url(../../images/deltaReport_select.png) no-repeat 50%',
			'url(../../images/deltaReport.png) no-repeat 50%',
			"DeltaReport.html?pin=" + pin  + "&restaurantID=" + restaurantID
	);
	// ---------------------------------
	bindActiveEvent('costAnalyzeReport', 
			'url(../../images/costAnalyzeReport_select.png) no-repeat 50%',
			'url(../../images/costAnalyzeReport.png) no-repeat 50%',
			"CostAnalysisReport.html?pin=" + pin  + "&restaurantID=" + restaurantID
	);
	// ---------------------------------
	bindActiveEvent('monthSettle', 
			'url(../../images/monthSettle_select.png) no-repeat 50%',
			'url(../../images/monthSettle.png) no-repeat 50%',
			monthSettleHandler
	);
	
};