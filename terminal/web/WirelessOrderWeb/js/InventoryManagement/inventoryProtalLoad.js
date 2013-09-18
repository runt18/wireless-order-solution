function inventoryMgrOnLoad(){
	// ---------------------------------
	bindActiveEvent('supplierMgr', 
			'url(../../images/supplierMgr_select.png) no-repeat 50%',
			'url(../../images/supplierMgr.png) no-repeat 50%',
			"SupplierManagement.html?" + strEncode("restaurantID="+restaurantID, "mi")
	);
	// ---------------------------------
	bindActiveEvent('inventoryBasicMgr', 
			'url(../../images/inventoryBasicMgr_select.png) no-repeat 50%',
			'url(../../images/inventoryBasicMgr.png) no-repeat 50%',
			"InventoryBasicManagement.html?"+ strEncode('restaurantID=' + restaurantID, 'mi')
	);
	// ---------------------------------
	bindActiveEvent('foodMaterialMgr', 
			'url(../../images/foodMaterialMgr_select.png) no-repeat 50%',
			'url(../../images/foodMaterialMgr.png) no-repeat 50%',
			"FoodMaterialManagement.html?"+ strEncode('restaurantID=' + restaurantID, 'mi')
	);
	// ---------------------------------
	bindActiveEvent('stockActionMgr', 
			'url(../../images/stockActionMgr_select.png) no-repeat 50%',
			'url(../../images/stockActionMgr.png) no-repeat 50%',
			"StockActionManagement.html?"+ strEncode('restaurantID=' + restaurantID, 'mi')
	);
	// ---------------------------------
	bindActiveEvent('stockTakeMgr', 
			'url(../../images/stockTakeMgr_select.png) no-repeat 50%',
			'url(../../images/stockTakeMgr.png) no-repeat 50%',
			"StockTakeManagement.html?"+ strEncode('restaurantID=' + restaurantID, 'mi')
	);
	// ---------------------------------
	bindActiveEvent('stockReport', 
			'url(../../images/stockReport_select.png) no-repeat 50%',
			'url(../../images/stockReport.png) no-repeat 50%',
			"StockReport.html?"+ strEncode('restaurantID=' + restaurantID, 'mi')
	);
	// ---------------------------------
	bindActiveEvent('stockDetailReport', 
			'url(../../images/stockDetailReport_select.png) no-repeat 50%',
			'url(../../images/stockDetailReport.png) no-repeat 50%',
			"StockDetailReport.html?"+ strEncode('restaurantID=' + restaurantID, 'mi')
	);
	// ---------------------------------
	bindActiveEvent('stockDistributionReport', 
			'url(../../images/stockDistributionReport_select.png) no-repeat 50%',
			'url(../../images/stockDistributionReport.png) no-repeat 50%',
			"StockDistributionReport.html?"+ strEncode('restaurantID=' + restaurantID, 'mi')
	);
	// ---------------------------------
	bindActiveEvent('deltaReport', 
			'url(../../images/deltaReport_select.png) no-repeat 50%',
			'url(../../images/deltaReport.png) no-repeat 50%',
			"DeltaReport.html?"+ strEncode('restaurantID=' + restaurantID, 'mi')
	);
	// ---------------------------------
	bindActiveEvent('costAnalyzeReport', 
			'url(../../images/costAnalyzeReport_select.png) no-repeat 50%',
			'url(../../images/costAnalyzeReport.png) no-repeat 50%',
			"CostAnalysisReport.html?"+ strEncode('restaurantID=' + restaurantID, 'mi')
	);
	// ---------------------------------
	bindActiveEvent('monthSettle', 
			'url(../../images/monthSettle_select.png) no-repeat 50%',
			'url(../../images/monthSettle.png) no-repeat 50%',
			monthSettleHandler
	);
	// ---------------------------------
	bindActiveEvent('historyStockAction', 
			'url(../../images/historyStockAction_select.png) no-repeat 50%',
			'url(../../images/historyStockAction.png) no-repeat 50%',
			"HistoryStockAction.html?"+ strEncode('restaurantID=' + restaurantID, 'mi')
	);
	
};