package com.wireless.pojo.staffMgr;

public class Page{

	public static enum Basic{
		MENU("menuMgr", "菜谱管理", "BasicManagement_Module/MenuManagement.html", "../../images/discountMgr.png"),
		DISCOUNT("discountMgr", "折扣管理", "BasicManagement_Module/DiscountManagement.html", "../../images/cancelReasonMgr1.png"),
		SERVICE("serviceMgr", "服务费管理", "BasicManagement_Module/ServiceManagement.html", "../../images/regionMgr.png"),
		TASTE("tasteMgr", "口味管理", "BasicManagement_Module/TasteManagement.html", "../../images/menuMgr.png"),
		PRINT("printMgr", "打印方案", "BasicManagement_Module/PrintScheme.html", "../../images/printerMgr.png"),
		CANCELREASON("cancelReason", "退菜原因", "", "../../images/cancelFoodReason.png"),
		PAYTYPE("payTypeMgr", "付款方式", "", "../../images/payTypeMgr.png"),
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
		INIT_MATERIAL("initMaterialPanel", "期初建账", "InventoryManagement_Module/InitMaterialMgr.html", "../../images/businessHour.png"),
		SUPPLIER("supplierMgr", "供应商管理", "InventoryManagement_Module/SupplierManagement.html", "../../images/supplierMgr.png"),
		INVENTORY("inventoryBasicMgr", "原料管理", "InventoryManagement_Module/InventoryBasicManagement.html", "../../images/inventoryBasicMgr.png"),
		FOOD_MATERIAL("foodMaterial", "菜品配料", "InventoryManagement_Module/FoodMaterialManagement.html", "../../images/foodMaterialMgr.png"),
		STOCK_ACTION("stockAction", "出入库任务", "InventoryManagement_Module/StockActionManagement.html", "../../images/stockActionMgr.png"),
		STOCK_TAKE("stockTake", "盘点任务", "InventoryManagement_Module/StockTakeManagement.html", "../../images/stockTakeMgr.png"),
		STOCK_REPORT("stockReport", "进销存汇总", "InventoryManagement_Module/StockReport.html", "../../images/stockReportMgr.png"),
		STOCK_DETAIL("stockDetail", "进销存明细", "InventoryManagement_Module/StockDetailReport.html", "../../images/stockDetailReport.png"),
		SUPPLIER_STOCK_IN_STATISTICS("supplierStockInStatistics", "供应商采购统计", "InventoryManagement_Module/SupplierStockInStatistics.html", "../../images/supplierMgr.png"),
		MATERIAL_STOCK_IN_STATISTICS("materialStockStatistics", "货品采购统计", "InventoryManagement_Module/MaterialStockStatistics.html", "../../images/stockActionMgr.png"),
		STOCK_DISTRIBUTION("stockDistribution", "库存分布", "InventoryManagement_Module/StockDistributionReport.html", "../../images/stockDistributionReport.png"),
		DELTA_REPORT("deltaReport", "消耗差异表", "InventoryManagement_Module/DeltaReport.html", "../../images/deltaReport.png"),
		SALE_OF_MATERIAL("saleOfMaterial", "销售对账表", "InventoryManagement_Module/SaleOfMaterials.html", "../../images/history.png"),
		COST_ANALYSIS("costAnalysis", "成本分析表", "InventoryManagement_Module/CostAnalysisReport.html", "../../images/costAnalysisReport.png"),
//		STOCKIN_GENERAL("stockInGeneral", "采购汇总表", "InventoryManagement_Module/StockInGeneral.html", "../../images/costAnalysisReport.png"),
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
	
	public static enum Distribution{
		INIT_DISTIRBUTION("initStockDistribution", "配送初始化", "", "../../images/businessHour.png"),
		SYNC_DISTRIBUTION("syncStockDistritbuion", "配送同步", "", "../../images/businessHour.png"),
		STOCK_DISTRIBUTIONACTION("stockDistributionAction", "配送任务", "StockChainManagement_Module/StockDistributionAction.html", "../../images/supplierMgr.png"),
		DISTRIBUTION_DETAIL_REPORT("distributionDetailReport", "配送明细", "StockChainManagement_Module/DistributionDetailReport.html", "../../images/stockDetailReport.png"),
		DISTRIBUTION_DELTA("distributionDelta", "配送差异表", "StockChainManagement_Module/DistributionDelta.html", "../../images/deltaReport.png");
		
		private String mgrId;
		private String desc;
		private String url;
		private String image;
		
		Distribution(String mgrId, String desc, String url, String image){
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
		SHIBIE("businessHourMgr", "市别设置", "", "../../images/businessHour.png"),
		HISTORY("history", "历史账单", "History_Module/HistoryStatistics.html", "../../images/history.png"),
		BUSINESS_STATISTICS("businessSubStatistics", "营业统计", "History_Module/BusinessSubStatistics.html", "../../images/businessChart.png"),
//		BIlls("businessStat", "营业统计", "", "../../images/businessStatis.png"),
		BUSINESS_RECEIPS("businessReceiptsStatistics", "收款统计", "History_Module/BusinessReceiptsStatistics.html", "../../images/businessReceips.png"),
		SALES_SUB("salesSubStatistics", "销售统计", "History_Module/SalesSubStatistics.html", "../../images/salesStat.png"),
		CANCELLED_FOOD("cancelledFood", "退菜统计", "History_Module/CancelledFood.html", "../../images/cancelledFoodStatis.png"),
		REPAID_STATISTICS("repaidStatistics", "反结账统计", "History_Module/RepaidStatistics.html", "../../images/commissionStatistics.png"),
		COMMISSION_STATISTICS("commissionStatistics", "提成统计", "History_Module/CommissionStatistics.html", "../../images/commissionStatistics.png"),
		DISCOUNT_STATISTICS("discountStatistics", "折扣统计", "History_Module/DiscountStatistics.html", "../../images/discountStatistics.png"),
		GIFT_STATISTICS("giftStatistics", "赠送统计", "History_Module/GiftStatistics.html", "../../images/giftStatistics.png"),
		ERASE_STATISTICS("eraseStatistics", "抹数统计", "History_Module/EraseStatistic.html", "../../images/giftStatistics.png"),
//		MEMBE_RPRICE_STATISTICS("memberPriceStatistics", "会员价统计 ", "History_Module/memberPriceStatictics.html", "../../images/giftStatistics.png"),
//		PASSENGERFLOWSTATISTICS("passengerFlowStatistics", "History_Module/PassengerFliwStatistics.html", "../../images/giftStatistics.png");
		PASSENGERFLOW_STATISTICS("passengerFlowStatistics", "客流统计", "History_Module/PassengerFlowStatistics.html", "../../images/giftStatistics.png"),
		COMBO_STATISTICS("comboStatistics", "套餐统计", "History_Module/comboStatistics.html", "../../images/giftStatistics.png");
		
		
		
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
		FORMATPRICE("formatPrice", "收款设置", "", "../../images/formatPrice.png"),
		RESTAURANT("resturantMgr", "餐厅管理", "", "../../images/resturantMgr.png"),
		STAFF("staffMgr", "员工管理", "System_Module/StaffManagement.html", "../../images/staffMgr.png"),
		VERIFY_CODE("verifyCode", "验证二维码", "", "../../images/weixinCode.jpg");
		
		
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
		WX_STEPS_BIND("WXBind", "分步", "Client_Module/WeixinAuth.html", "../../images/discountStatistics.png"),
		WX_BIND("WXBind", "微信餐厅绑定", "", "../../images/discountStatistics.png"),
		WX_LOGO("WXLogo", "微信餐厅形象", "", "../../images/weixin.png"),
		WX_INFO("WXInfo", "微信餐厅简介", "", "../../images/resturantMgr.png");
		
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
		

		public String getUrl(){
			return url;
		}

		public String getImage() {
			return image;
		}
		
	}
	
	public static enum Member{
//		MEMBER_TYPE("memberType", "会员类型", "Client_Module/MemberTypeManagement.html", "../../images/memberTypeMgr.png"),
		MEMBER("memberMgr", "会员管理", "Client_Module/MemberManagement.html", "../../images/memberMgr.png"),
		MEMBER_SUMMARY("memberSummary", "会员汇总", "Client_Module/memberSummary.html", "../../images/memberMgr.png"),
		MEMBER_CHARGE_STATISTICS("memberChargeStatistics", "充值统计", "Client_Module/memberChargeStatistics.html", "../../images/btnRechargeDetails.png"),
		MEMBER_REFUND_STATISTICS("memberRefundStatistics", "取款统计", "Client_Module/memberRefundStatistics.html", "../../images/btnTakeMoney.png"),
		MEMBER_CONSUME_STATISTICS("memberConsumeStatistics", "消费统计", "Client_Module/memberConsumeStatistics.html", "../../images/btnConsumeDetail.png"),
		MEMBER_CREATED_STATISTICS("memberCreatedStatistics", "开卡统计", "Client_Module/memberCreatedStatistics.html", "../../images/cancelReasonMgr1.png"),
		POINT_CONSUME("pointConsumeStaitstics", "积分统计", "Client_Module/pointConsumeStatistics.html", "../../images/memberMgr.png"),
		MEMBER_COND("memberCond", "会员分析", "Client_Module/memberCond.html", "../../images/history.png"),
		COUPON_STATISTICS("couponStatistics", "优惠券统计 ", "History_Module/CouponStatistics.html", "../../images/giftStatistics.png"),
		COUPON_EFFECT("couponEffect", "优惠活动效果分析", "Client_Module/couponEffectStatistics.html", "../../images/memberMgr.png"),
		ACTIVE("activeMgr", "优惠活动管理", "Client_Module/ActiveManagement.html", "../../images/memberMgr.png"),
		REPRESENT_STATISTICS("representStatistics", "代言统计", "History_Module/RepresentStatistics.html", "../../images/memberMgr.png"),
		WXCOMMISSION_STATISTICS("wxCommissionStatistics", "佣金统计", "History_Module/WxCommissionStatistics.html", "../../images/memberMgr.png"),
		DEPUTY_MENU_SET("deputyMenu", "我要代言设置", "MgrCenter_Module/weixinRepresent.html", "../../images/memberMgr.png"),
		WX_STEPS_BIND("WXBind", "微信公众号设置", "Client_Module/WeixinAuth.html", "../../images/discountStatistics.png"),
		MEMBER_MENU_SET("WxMenu", "微信菜单设置", "MgrCenter_Module/weixinMenuMgr.html", "../../images/memberMgr.png");
		
		
//		COUPON("coupon", "优惠劵管理", "Client_Module/CouponManagement.html", "../../images/book.png" );
		
		private final String mgrId;
		private final String desc;
		private final String url;
		private final String image;
		
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
