package com.wireless.pojo.staffMgr;

public class Page{

	public static enum Basic{
		DISCOUNT("discountMgr", "折扣管理", "BasicManagement_Module/DiscountManagement.html", "../../images/cancelReasonMgr1.png"),
		MENU("menuMgr", "菜谱管理", "BasicManagement_Module/MenuManagement.html", "../../images/discountMgr.png"),
		TASTE("tasteMgr", "口味管理", "BasicManagement_Module/TasteManagement.html", "../../images/menuMgr.png"),
		PRINT("printMgr", "打印方案", "BasicManagement_Module/PrintScheme.html", "../../images/printerMgr.png"),
		CANCELREASON("cancelReason", "退菜原因", "", "../../images/cancelFoodReason.png"),
		REGION("regionMgr", "区域管理", "BasicManagement_Module/RegionManagement.html", "../../images/regionMgr.png");
		
		private String mgrId;
		private String desc;
		private String url;
		private String image;
		
		Basic(String mgrId, String desc, String url, String image){
			this.mgrId = mgrId;
			this.image = image;
			this.url = url;
			this.desc = desc;
		}

		public String getMgrId() {
			return mgrId;
		}

		public String getDesc() {
			return desc;
		}

		public String getUrl() {
			return url;
		}

		public String getImage() {
			return image;
		}	
		
	}
	
	
	public static enum Stock{
		SUPPLIER("supplierMgr", "供应商管理", "InventoryManagement_Module/SupplierManagement.html", "../../images/supplierMgr.png"),
		INVENTORY("inventoryBasicMgr", "原料管理", "InventoryManagement_Module/InventoryBasicManagement.html", "../../images/inventoryBasicMgr.png"),
		FOOD_MATERIAL("foodMaterial", "菜品配料", "InventoryManagement_Module/FoodMaterialManagement.html", "../../images/foodMaterialMgr.png"),
		STOCK_ACTION("stockAction", "出入库任务", "InventoryManagement_Module/StockActionManagement.html", "../../images/stockActionMgr.png"),
		STOCK_TAKE("stockTake", "盘点任务", "InventoryManagement_Module/StockTakeManagement.html", "../../images/stockTakeMgr.png"),
		STOCK_REPORT("stockReport", "进销存汇总", "InventoryManagement_Module/StockReport.html", "../../images/stockReportMgr.png"),
		STOCK_DETAIL("stockDetail", "进销存明细", "InventoryManagement_Module/StockDetailReport.html", "../../images/stockDetailReport.png"),
		STOCK_DISTRIBUTION("stockDistribution", "库存分布", "InventoryManagement_Module/StockDistributionReport.html", "../../images/stockDistributionReport.png"),
		DELTA_REPORT("deltaReport", "消耗差异表", "InventoryManagement_Module/DeltaReport.html", "../../images/deltaReport.png"),
		COST_ANALYSIS("costAnalysis", "成本分析表", "InventoryManagement_Module/CostAnalysisReport.html", "../../images/costAnalysisReport.png"),
		MONTH_SETTLE("monthSettle", "月结", "", "../../images/monthSettle.png"),
		HISTORY_STOCKACTION("historyStockAction", "历史库单", "InventoryManagement_Module/HistoryStockAction.html", "../../images/history.png");
		
		private String mgrId;
		private String desc;
		private String url;
		private String image;
		
		Stock(String mgrId, String desc, String url, String image){
			this.mgrId = mgrId;
			this.image = image;
			this.url = url;
			this.desc = desc;
		}

		public String getMgrId() {
			return mgrId;
		}

		public String getDesc() {
			return desc;
		}

		public String getUrl() {
			return url;
		}

		public String getImage() {
			return image;
		}
	}
	
	public static enum History{
		HISTORY("history", "历史账单", "History_Module/HistoryStatistics.html", "../../images/history.png"),
		BIlls("businessStat", "营业统计", "", "../../images/businessStatis.png"),
		BUSINESS_RECEIPS("businessReceiptsStatistics", "收款统计", "History_Module/BusinessReceiptsStatistics.html", "../../images/businessReceips.png"),
		SALES_SUB("salesSubStatistics", "销售统计", "History_Module/SalesSubStatistics.html", "../../images/salesStat.png"),
		CANCELLED_FOOD("cancelledFood", "退菜统计", "History_Module/CancelledFood.html", "../../images/cancelledFoodStatis.png"),
		REPAID_STATISTICS("repaidStatistics", "反结账统计", "History_Module/RepaidStatistics.html", "../../images/repaidStatistics.png"),
		COMMISSION_STATISTICS("commissionStatistics", "提成统计", "History_Module/CommissionStatistics.html", "../../images/commissionStatistics.png"),
		BUSINESS_CHART("businessChart", "营业走势图", "History_Module/BusinessChart.html", "../../images/businessChart.png");
		
		private String mgrId;
		private String desc;
		private String url;
		private String image;
		
		History(String mgrId, String desc, String url, String image){
			this.mgrId = mgrId;
			this.image = image;
			this.url = url;
			this.desc = desc;
		}

		public String getMgrId() {
			return mgrId;
		}

		public String getDesc() {
			return desc;
		}

		public String getUrl() {
			return url;
		}

		public String getImage() {
			return image;
		}
	}
	
	public static enum System{
		SHIBIE("businessHourMgr", "市别设置", "", "../../images/businessHour.png"),
		FORMATPRICE("formatPrice", "收款设置", "", "../../images/formatPrice.png"),
		RESTAURANT("resturantMgr", "餐厅管理", "", "../../images/resturantMgr.png"),
		STAFF("staffMgr", "员工管理", "System_Module/StaffManagement.html", "../../images/staffMgr.png");
		
		private String mgrId;
		private String desc;
		private String url;
		private String image;
		
		System(String mgrId, String desc, String url, String image){
			this.mgrId = mgrId;
			this.image = image;
			this.url = url;
			this.desc = desc;
		}

		public String getMgrId() {
			return mgrId;
		}

		public String getDesc() {
			return desc;
		}

		public String getUrl() {
			return url;
		}

		public String getImage() {
			return image;
		}
		
	}
	
	public static enum Weixin{
		WX_LOGO("WXLogo", "微信餐厅形象", "", "../../images/weixin.png"),
		WX_INFO("WXInfo", "微信餐厅简介", "", "../../images/resturantMgr.png"),
		WX_SALES("WXSales", "微信促销信息", "System_Module/WXSalesManagement.html", "../../images/staffMgr.png");
		
		private String mgrId;
		private String desc;
		private String url;
		private String image;
		
		Weixin(String mgrId, String desc, String url, String image){
			this.mgrId = mgrId;
			this.image = image;
			this.url = url;
			this.desc = desc;
		}

		public String getMgrId() {
			return mgrId;
		}

		public String getDesc() {
			return desc;
		}

		public String getUrl() {
			return url;
		}

		public String getImage() {
			return image;
		}
		
	}
	
	public static enum Member{
		MEMBER_TYPE("memberType", "会员类型", "Client_Module/MemberTypeManagement.html", "../../images/memberTypeMgr.png"),
		MEMBER("memberMgr", "会员管理", "Client_Module/MemberManagement.html", "../../images/memberMgr.png"),
		MEMBER_LEVEL("memberLevel", "会员等级", "Client_Module/MemberLevelManagement.html", "../../images/memberLevel.png"),
		COUPON("coupon", "优惠劵管理", "Client_Module/CouponManagement.html", "../../images/book.png" );
		
		private String mgrId;
		private String desc;
		private String url;
		private String image;
		
		Member(String mgrId, String desc, String url, String image){
			this.mgrId = mgrId;
			this.image = image;
			this.url = url;
			this.desc = desc;
		}

		public String getMgrId() {
			return mgrId;
		}

		public String getDesc() {
			return desc;
		}

		public String getUrl() {
			return url;
		}

		public String getImage() {
			return image;
		}
	}
	public static enum Sms{
		SMS_USE("SmsUse", "短信使用记录", "Sms_Module/SmsManagement.html", "../../images/smsMgr.png");
		
		private String mgrId;
		private String desc;
		private String url;
		private String image;
		
		Sms(String mgrId, String desc, String url, String image){
			this.mgrId = mgrId;
			this.image = image;
			this.url = url;
			this.desc = desc;
		}

		public String getMgrId() {
			return mgrId;
		}

		public String getDesc() {
			return desc;
		}

		public String getUrl() {
			return url;
		}

		public String getImage() {
			return image;
		}
		
	}
	
}
